package com.zhigalko.consumer.integration.listener;

import com.zhigalko.consumer.repository.EventRepository;
import com.zhigalko.consumer.repository.SnapshotRepository;
import com.zhigalko.core.annotation.IT;
import com.zhigalko.core.config.KafkaProducerConfig;
import com.zhigalko.core.domain.model.Snapshot;
import com.zhigalko.core.event.CreateCustomerEvent;
import com.zhigalko.core.event.DeleteCustomerEvent;
import com.zhigalko.core.event.Event;
import com.zhigalko.core.event.UpdateCustomerAddressEvent;
import com.zhigalko.core.event.UpdateCustomerNameEvent;
import com.zhigalko.core.schema.CreateCustomerAvroEvent;
import com.zhigalko.core.schema.CustomerViewAvroEvent;
import com.zhigalko.core.schema.DeleteCustomerAvroEvent;
import com.zhigalko.core.schema.UpdateCustomerAddressAvroEvent;
import com.zhigalko.core.schema.UpdateCustomerNameAvroEvent;
import com.zhigalko.core.service.KafkaProducer;
import com.zhigalko.core.util.KafkaCustomProperties;
import io.confluent.kafka.serializers.KafkaAvroSerializerConfig;
import jakarta.annotation.PostConstruct;
import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.StreamSupport;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.utility.DockerImageName;
import static com.zhigalko.consumer.util.TestDataUtil.getCreateCustomerAvroEvent;
import static com.zhigalko.consumer.util.TestDataUtil.getCreateCustomerEvent;
import static com.zhigalko.consumer.util.TestDataUtil.getDeleteCustomerAvroEvent;
import static com.zhigalko.consumer.util.TestDataUtil.getSnapshot;
import static com.zhigalko.consumer.util.TestDataUtil.getUpdateCustomerAddressAvroEvent;
import static com.zhigalko.consumer.util.TestDataUtil.getUpdateCustomerNameAvroEvent;
import static com.zhigalko.core.domain.EventType.CREATE_CUSTOMER_VIEW;
import static com.zhigalko.core.domain.EventType.DELETE_CUSTOMER_VIEW;
import static com.zhigalko.core.domain.EventType.UPDATE_CUSTOMER_ADDRESS_VIEW;
import static com.zhigalko.core.domain.EventType.UPDATE_CUSTOMER_NAME_VIEW;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.awaitility.Awaitility.await;

@IT
public class KafkaListenerIT {
	private static final Network NETWORK = Network.newNetwork();
	public static final Duration POLL_INTERVAL = Duration.ofSeconds(3);
	public static final Duration MAX_DURATION = Duration.ofSeconds(12);

	@Container
	public static final KafkaContainer KAFKA_CONTAINER = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:latest"))
			.withNetwork(NETWORK);

	@Container
	private static final MongoDBContainer MONGO_DB_CONTAINER = new MongoDBContainer(DockerImageName.parse("mongo:latest"))
			.withNetwork(NETWORK);

	@Container
	public static final GenericContainer<?> SCHEMA_REGISTRY =
			new GenericContainer<>(DockerImageName.parse("confluentinc/cp-schema-registry:latest"))
		.withNetwork(NETWORK)
				.withExposedPorts(8081)
				.withEnv("SCHEMA_REGISTRY_HOST_NAME", "schema-registry")
				.withEnv("SCHEMA_REGISTRY_LISTENERS", "http://0.0.0.0:8081")
				.withEnv("SCHEMA_REGISTRY_KAFKASTORE_BOOTSTRAP_SERVERS",
						"PLAINTEXT://" + KAFKA_CONTAINER.getNetworkAliases().get(0) + ":9092")
					.dependsOn(KAFKA_CONTAINER)
				.waitingFor(Wait.forHttp("/subjects").forStatusCode(200));

	@DynamicPropertySource
	private static void registerKafkaAndMongoProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.kafka.bootstrap-servers", KAFKA_CONTAINER::getBootstrapServers);
		registry.add("spring.kafka.properties.schema.registry.url",
				() -> "http://" + SCHEMA_REGISTRY.getHost() + ":" + SCHEMA_REGISTRY.getFirstMappedPort());
		registry.add("spring.data.mongodb.uri", MONGO_DB_CONTAINER::getReplicaSetUrl);
	}

	@Autowired
	private KafkaProducer kafkaProducer;

	@Autowired
	private SnapshotRepository snapshotRepository;

	@Autowired
	private EventRepository eventRepository;

	@Autowired
	private KafkaCustomProperties kafkaCustomProperties;

	@Autowired
	private ConcurrentKafkaListenerContainerFactory<String, Object> containerFactory;

	@Test
	void listenCreateCustomerTopic() {
		CreateCustomerAvroEvent createCustomerAvroEvent = getCreateCustomerAvroEvent();
		kafkaProducer.sendMessage(createCustomerAvroEvent, kafkaCustomProperties.getCreateCustomerEventTopic().getName());

		await()
				.pollInterval(POLL_INTERVAL)
				.atMost(MAX_DURATION)
				.untilAsserted(() -> {
					Event abstractEvent = eventRepository.findById(createCustomerAvroEvent.getId().toString())
							.orElseThrow();
					CreateCustomerEvent event = (CreateCustomerEvent) abstractEvent;
					assertThat(event.getAggregateId()).isEqualTo(1L);
					assertThat(event.getEventType()).isEqualTo(createCustomerAvroEvent.getEventType());
					assertThat(event.getTimestamp()).isNotNull();
					assertThat(event.getPayload().getName()).isEqualTo(createCustomerAvroEvent.getName());
					assertThat(event.getPayload().getAddress()).isEqualTo(createCustomerAvroEvent.getAddress());
					Snapshot snapshot = snapshotRepository.findByAggregateId(1L)
							.orElseThrow();
					assertThat(snapshot.getAggregateId()).isEqualTo(1L);
					assertThat(snapshot.getPayload().getName()).isEqualTo(createCustomerAvroEvent.getName());
					assertThat(snapshot.getPayload().getAddress()).isEqualTo(createCustomerAvroEvent.getAddress());
					assertThat(snapshot.getVersion()).isEqualTo(1L);
					assertThat(snapshot.getTimestamp()).isNotNull();
				});

		await()
				.pollInterval(POLL_INTERVAL)
				.atMost(MAX_DURATION)
				.untilAsserted(() -> {
					try (KafkaConsumer<String, CustomerViewAvroEvent> consumer = new KafkaConsumer<>(containerFactory.getConsumerFactory().getConfigurationProperties())) {
						consumer.subscribe(Collections.singletonList(kafkaCustomProperties.getCustomerViewEventTopic().getName()));
						ConsumerRecords<String, CustomerViewAvroEvent> records = consumer.poll(Duration.ofSeconds(10));
						if (records.isEmpty()) {
							fail("event was not received");
						}
						StreamSupport.stream(records.spliterator(), false)
								.filter(record -> CREATE_CUSTOMER_VIEW.getName().contentEquals(record.value().getEventType()))
								.forEach(record -> {
									CustomerViewAvroEvent consumedEvent = record.value();
									assertThat(consumedEvent.getId()).isNotBlank();
									assertThat(consumedEvent.getEventType().toString()).hasToString(CREATE_CUSTOMER_VIEW.getName());
									assertThat(consumedEvent.getTimestamp()).isNotBlank();
									assertThat(consumedEvent.getAggregateId()).isEqualTo(1L);
									assertThat(consumedEvent.getName().toString()).hasToString(createCustomerAvroEvent.getName().toString());
									assertThat(consumedEvent.getAddress().toString()).hasToString(createCustomerAvroEvent.getAddress().toString());
								});
						consumer.unsubscribe();
					}
				});
	}

	@Test
	void listenUpdateCustomerNameTopic() {
		eventRepository.save(getCreateCustomerEvent());
		snapshotRepository.save(getSnapshot());
		UpdateCustomerNameAvroEvent updateCustomerNameAvroEvent = getUpdateCustomerNameAvroEvent();
		kafkaProducer.sendMessage(updateCustomerNameAvroEvent, kafkaCustomProperties.getUpdateCustomerNameEventTopic().getName());

		await()
				.pollInterval(POLL_INTERVAL)
				.atMost(MAX_DURATION)
				.untilAsserted(() -> {
					Event abstractEvent = eventRepository.findById(updateCustomerNameAvroEvent.getId().toString())
							.orElseThrow();
					UpdateCustomerNameEvent event = (UpdateCustomerNameEvent) abstractEvent;
					assertThat(event.getAggregateId()).isEqualTo(1L);
					assertThat(event.getEventType()).isEqualTo(updateCustomerNameAvroEvent.getEventType());
					assertThat(event.getTimestamp()).isNotNull();
					assertThat(event.getPayload().getName()).isEqualTo(updateCustomerNameAvroEvent.getName());
					Snapshot snapshot = snapshotRepository.findByAggregateId(1L)
							.orElseThrow();
					assertThat(snapshot.getAggregateId()).isEqualTo(1L);
					assertThat(snapshot.getPayload().getName()).isEqualTo(updateCustomerNameAvroEvent.getName());
					assertThat(snapshot.getVersion()).isEqualTo(2L);
					assertThat(snapshot.getTimestamp()).isNotNull();
				});
		await()
				.pollInterval(POLL_INTERVAL)
				.atMost(MAX_DURATION)
				.untilAsserted(() -> {
					try (KafkaConsumer<String, CustomerViewAvroEvent> consumer = new KafkaConsumer<>(containerFactory.getConsumerFactory().getConfigurationProperties())) {
						consumer.subscribe(Collections.singletonList(kafkaCustomProperties.getCustomerViewEventTopic().getName()));
						ConsumerRecords<String, CustomerViewAvroEvent> records = consumer.poll(Duration.ofSeconds(10));
						if (records.isEmpty()) {
							fail("event was not received");
						}
						StreamSupport.stream(records.spliterator(), false)
								.filter(record -> UPDATE_CUSTOMER_NAME_VIEW.getName().contentEquals(record.value().getEventType()))
								.forEach(record -> {
									CustomerViewAvroEvent consumedEvent = record.value();
									assertThat(consumedEvent.getId()).isNotBlank();
									assertThat(consumedEvent.getEventType().toString()).hasToString(UPDATE_CUSTOMER_NAME_VIEW.getName());
									assertThat(consumedEvent.getTimestamp()).isNotBlank();
									assertThat(consumedEvent.getAggregateId()).isEqualTo(1L);
									assertThat(consumedEvent.getName().toString()).hasToString(updateCustomerNameAvroEvent.getName().toString());
								});
						consumer.unsubscribe();
					}
				});
	}

	@Test
	void listenUpdateCustomerAddressTopic() {
		eventRepository.save(getCreateCustomerEvent());
		snapshotRepository.save(getSnapshot());
		UpdateCustomerAddressAvroEvent updateCustomerAddressAvroEvent = getUpdateCustomerAddressAvroEvent();
		kafkaProducer.sendMessage(updateCustomerAddressAvroEvent, kafkaCustomProperties.getUpdateCustomerAddressEventTopic().getName());

		await()
				.pollInterval(POLL_INTERVAL)
				.atMost(MAX_DURATION)
				.untilAsserted(() -> {
					Event abstractEvent = eventRepository.findById(updateCustomerAddressAvroEvent.getId().toString())
							.orElseThrow();
					UpdateCustomerAddressEvent event = (UpdateCustomerAddressEvent) abstractEvent;
					assertThat(event.getAggregateId()).isEqualTo(1L);
					assertThat(event.getEventType()).isEqualTo(updateCustomerAddressAvroEvent.getEventType());
					assertThat(event.getTimestamp()).isNotNull();
					assertThat(event.getPayload().getAddress()).isEqualTo(updateCustomerAddressAvroEvent.getAddress());
					Snapshot snapshot = snapshotRepository.findByAggregateId(1L)
							.orElseThrow();
					assertThat(snapshot.getAggregateId()).isEqualTo(1L);
					assertThat(snapshot.getPayload().getAddress()).isEqualTo(updateCustomerAddressAvroEvent.getAddress());
					assertThat(snapshot.getVersion()).isEqualTo(2L);
					assertThat(snapshot.getTimestamp()).isNotNull();
				});
		await()
				.pollInterval(POLL_INTERVAL)
				.atMost(MAX_DURATION)
				.untilAsserted(() -> {
					try (KafkaConsumer<String, CustomerViewAvroEvent> consumer = new KafkaConsumer<>(containerFactory.getConsumerFactory().getConfigurationProperties())) {
						consumer.subscribe(Collections.singletonList(kafkaCustomProperties.getCustomerViewEventTopic().getName()));
						ConsumerRecords<String, CustomerViewAvroEvent> records = consumer.poll(Duration.ofSeconds(10));
						if (records.isEmpty()) {
							fail("event was not received");
						}
						StreamSupport.stream(records.spliterator(), false)
								.filter(record -> UPDATE_CUSTOMER_ADDRESS_VIEW.getName().contentEquals(record.value().getEventType()))
								.forEach(record -> {
									CustomerViewAvroEvent consumedEvent = record.value();
									assertThat(consumedEvent.getId()).isNotBlank();
									assertThat(consumedEvent.getEventType().toString()).hasToString(UPDATE_CUSTOMER_ADDRESS_VIEW.getName());
									assertThat(consumedEvent.getTimestamp()).isNotBlank();
									assertThat(consumedEvent.getAggregateId()).isEqualTo(1L);
									assertThat(consumedEvent.getAddress().toString()).hasToString(updateCustomerAddressAvroEvent.getAddress().toString());
								});
						consumer.unsubscribe();
					}
				});
	}

	@Test
	void listenDeleteCustomerTopic() {
		eventRepository.save(getCreateCustomerEvent());
		snapshotRepository.save(getSnapshot());
		DeleteCustomerAvroEvent deleteCustomerAvroEvent = getDeleteCustomerAvroEvent();
		kafkaProducer.sendMessage(deleteCustomerAvroEvent, kafkaCustomProperties.getDeleteCustomerEventTopic().getName());

		await()
				.pollInterval(POLL_INTERVAL)
				.atMost(MAX_DURATION)
				.untilAsserted(() -> {
					Event abstractEvent = eventRepository.findById(deleteCustomerAvroEvent.getId().toString())
							.orElseThrow();
					DeleteCustomerEvent event = (DeleteCustomerEvent) abstractEvent;
					assertThat(event.getAggregateId()).isEqualTo(1L);
					assertThat(event.getEventType()).isEqualTo(deleteCustomerAvroEvent.getEventType());
					assertThat(event.getTimestamp()).isNotNull();
					Optional<Snapshot> snapshot = snapshotRepository.findByAggregateId(1L);
					assertThat(snapshot).isEmpty();
				});
		await()
				.pollInterval(POLL_INTERVAL)
				.atMost(MAX_DURATION)
				.untilAsserted(() -> {
					try (KafkaConsumer<String, CustomerViewAvroEvent> consumer = new KafkaConsumer<>(containerFactory.getConsumerFactory().getConfigurationProperties())) {
						consumer.subscribe(Collections.singletonList(kafkaCustomProperties.getCustomerViewEventTopic().getName()));
						ConsumerRecords<String, CustomerViewAvroEvent> records = consumer.poll(Duration.ofSeconds(5));
						if (records.isEmpty()) {
							fail("event was not received");
						}
						StreamSupport.stream(records.spliterator(), false)
								.filter(record -> DELETE_CUSTOMER_VIEW.getName().contentEquals(record.value().getEventType()))
								.forEach(record -> {
									CustomerViewAvroEvent consumedEvent = record.value();
									assertThat(consumedEvent.getId()).isNotBlank();
									assertThat(consumedEvent.getEventType().toString()).hasToString(DELETE_CUSTOMER_VIEW.getName());
									assertThat(consumedEvent.getTimestamp()).isNotBlank();
									assertThat(consumedEvent.getAggregateId()).isEqualTo(1L);
								});
						consumer.unsubscribe();
					}
				});
	}

	@AfterEach
	void tearDown() {
		eventRepository.deleteAll();
		snapshotRepository.deleteAll();
	}

	@TestConfiguration
	@RequiredArgsConstructor
	@Import({KafkaProducerConfig.class})
	static class Config {
		private final ConcurrentKafkaListenerContainerFactory<String, Object> containerFactory;
		private final KafkaTemplate<String, Object> kafkaTemplate;

		@PostConstruct
		void initialize() {
			String schemaRegistryUrl = "http://" + SCHEMA_REGISTRY.getHost() + ":" + SCHEMA_REGISTRY.getFirstMappedPort();
			ProducerFactory<String, Object> producerFactory = kafkaTemplate.getProducerFactory();
			final Map<String, Object> producerProps = new HashMap<>();
			producerProps.put(KafkaAvroSerializerConfig.SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryUrl);
			producerFactory.updateConfigs(producerProps);
			ConsumerFactory<? super String, ? super Object> consumerFactory = containerFactory.getConsumerFactory();
			final Map<String, Object> consumerProps = new HashMap<>();
			consumerProps.put(KafkaAvroSerializerConfig.SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryUrl);
			consumerFactory.updateConfigs(consumerProps);
		}
	}
}
