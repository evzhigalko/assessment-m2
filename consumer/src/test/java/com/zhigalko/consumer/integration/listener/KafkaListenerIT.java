package com.zhigalko.consumer.integration.listener;

import com.zhigalko.common.domain.model.Snapshot;
import com.zhigalko.common.event.CreateCustomerEvent;
import com.zhigalko.common.event.DeleteCustomerEvent;
import com.zhigalko.common.event.ErrorEvent;
import com.zhigalko.common.event.Event;
import com.zhigalko.common.event.UpdateCustomerAddressEvent;
import com.zhigalko.common.event.UpdateCustomerNameEvent;
import com.zhigalko.common.schema.CreateCustomerAvroEvent;
import com.zhigalko.common.schema.CustomerViewAvroEvent;
import com.zhigalko.common.schema.DeleteCustomerAvroEvent;
import com.zhigalko.common.schema.UpdateCustomerAddressAvroEvent;
import com.zhigalko.common.schema.UpdateCustomerNameAvroEvent;
import com.zhigalko.common.service.KafkaProducer;
import com.zhigalko.common.util.KafkaCustomProperties;
import com.zhigalko.consumer.integration.BaseIntegrationTest;
import com.zhigalko.consumer.integration.listener.config.KafkaTestConfig;
import com.zhigalko.consumer.repository.ErrorEventRepository;
import com.zhigalko.consumer.repository.EventRepository;
import com.zhigalko.consumer.repository.SnapshotRepository;
import com.zhigalko.consumer.service.EventService;
import java.time.Duration;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.StreamSupport;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.utility.DockerImageName;
import static com.zhigalko.common.domain.EventType.CREATE_CUSTOMER;
import static com.zhigalko.common.domain.EventType.CREATE_CUSTOMER_VIEW;
import static com.zhigalko.common.domain.EventType.DELETE_CUSTOMER_VIEW;
import static com.zhigalko.common.domain.EventType.UPDATE_CUSTOMER_ADDRESS_VIEW;
import static com.zhigalko.common.domain.EventType.UPDATE_CUSTOMER_NAME_VIEW;
import static com.zhigalko.consumer.util.TestDataUtil.getCreateCustomerAvroEvent;
import static com.zhigalko.consumer.util.TestDataUtil.getCreateCustomerEvent;
import static com.zhigalko.consumer.util.TestDataUtil.getDeleteCustomerAvroEvent;
import static com.zhigalko.consumer.util.TestDataUtil.getSnapshot;
import static com.zhigalko.consumer.util.TestDataUtil.getUpdateCustomerAddressAvroEvent;
import static com.zhigalko.consumer.util.TestDataUtil.getUpdateCustomerNameAvroEvent;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doThrow;

@Import(KafkaTestConfig.class)
public class KafkaListenerIT extends BaseIntegrationTest {
	public static final Duration POLL_INTERVAL = Duration.ofSeconds(3);
	public static final Duration MAX_DURATION = Duration.ofSeconds(25);
	private static final Network NETWORK = Network.newNetwork();

	@Container
	public static final KafkaContainer KAFKA_CONTAINER = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:latest"))
			.withNetwork(NETWORK);

	@Container
	public static final MongoDBContainer MONGO_DB_CONTAINER = new MongoDBContainer(DockerImageName.parse("mongo:latest"))
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
				() -> "http://localhost:" + SCHEMA_REGISTRY.getFirstMappedPort());
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

	@SpyBean
	private EventService eventService;

	@Autowired
	private ErrorEventRepository errorEventRepository;

	@Test
	void containersAreRun() {
		assertThat(KAFKA_CONTAINER.isRunning()).isTrue();
		assertThat(MONGO_DB_CONTAINER.isRunning()).isTrue();
		assertThat(SCHEMA_REGISTRY.isRunning()).isTrue();
	}

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
								.filter(consumerRecord -> CREATE_CUSTOMER_VIEW.getName().contentEquals(consumerRecord.value().getEventType()))
								.forEach(consumerRecord -> {
									CustomerViewAvroEvent consumedEvent = consumerRecord.value();
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
								.filter(consumerRecord -> UPDATE_CUSTOMER_NAME_VIEW.getName().contentEquals(consumerRecord.value().getEventType()))
								.forEach(consumerRecord -> {
									CustomerViewAvroEvent consumedEvent = consumerRecord.value();
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
								.filter(consumerRecord -> UPDATE_CUSTOMER_ADDRESS_VIEW.getName().contentEquals(consumerRecord.value().getEventType()))
								.forEach(consumerRecord -> {
									CustomerViewAvroEvent consumedEvent = consumerRecord.value();
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
								.filter(consumerRecord -> DELETE_CUSTOMER_VIEW.getName().contentEquals(consumerRecord.value().getEventType()))
								.forEach(consumerRecord -> {
									CustomerViewAvroEvent consumedEvent = consumerRecord.value();
									assertThat(consumedEvent.getId()).isNotBlank();
									assertThat(consumedEvent.getEventType().toString()).hasToString(DELETE_CUSTOMER_VIEW.getName());
									assertThat(consumedEvent.getTimestamp()).isNotBlank();
									assertThat(consumedEvent.getAggregateId()).isEqualTo(1L);
								});
						consumer.unsubscribe();
					}
				});
	}

	@Test
	void listenCreateCustomerTopic_retry() {
		CreateCustomerAvroEvent createCustomerAvroEvent = getCreateCustomerAvroEvent();
		String createCustomerTopicName = kafkaCustomProperties.getCreateCustomerEventTopic().getName();
		kafkaProducer.sendMessage(createCustomerAvroEvent, createCustomerTopicName);

		doThrow(new IllegalArgumentException())
				.doCallRealMethod()
				.when(eventService).createCustomer(any());

		await()
				.pollInterval(Duration.ofSeconds(3))
				.atMost(Duration.ofSeconds(6))
				.untilAsserted(() -> {
					Optional<Event> event = eventRepository.findById(createCustomerAvroEvent.getId().toString());
					assertThat(event).isEmpty();
					Optional<Snapshot> snapshot = snapshotRepository.findByAggregateId(1L);
					assertThat(snapshot).isEmpty();
				});

		await()
				.pollInterval(POLL_INTERVAL)
				.atMost(Duration.ofSeconds(30))
				.untilAsserted(() -> {
					try (KafkaConsumer<String, CreateCustomerAvroEvent> consumer = new KafkaConsumer<>(containerFactory.getConsumerFactory().getConfigurationProperties())) {
						String createCustomerRetryTopicName = createCustomerTopicName + "-retry";
						consumer.subscribe(Collections.singletonList(createCustomerRetryTopicName));
						ConsumerRecords<String, CreateCustomerAvroEvent> records = consumer.poll(Duration.ofSeconds(10));
						if (records.isEmpty()) {
							fail("event was not received");
						}
						StreamSupport.stream(records.spliterator(), false)
								.filter(consumerRecord -> consumerRecord.value().getId().equals(createCustomerAvroEvent.getId()))
								.forEach(consumerRecord -> {
									assertThat(consumerRecord.topic()).isEqualTo(createCustomerRetryTopicName);
									Optional<Event> event = eventRepository.findById(createCustomerAvroEvent.getId().toString());
									assertThat(event).isNotEmpty();
									Optional<Snapshot> snapshot = snapshotRepository.findByAggregateId(1L);
									assertThat(snapshot).isNotEmpty();
								});
						consumer.unsubscribe();
					}
				});
	}

	@Test
	void listenCreateCustomerTopic_retry_dlt() {
		CreateCustomerAvroEvent createCustomerAvroEvent = getCreateCustomerAvroEvent();
		String createCustomerTopicName = kafkaCustomProperties.getCreateCustomerEventTopic().getName();
		kafkaProducer.sendMessage(createCustomerAvroEvent, createCustomerTopicName);

		doThrow(new IllegalArgumentException()).when(eventService).createCustomer(any());

		await()
				.pollInterval(Duration.ofSeconds(3))
				.atMost(Duration.ofSeconds(6))
				.untilAsserted(() -> {
					Optional<Event> event = eventRepository.findById(createCustomerAvroEvent.getId().toString());
					assertThat(event).isEmpty();
					Optional<Snapshot> snapshot = snapshotRepository.findByAggregateId(1L);
					assertThat(snapshot).isEmpty();
				});

		await()
				.pollInterval(POLL_INTERVAL)
				.atMost(Duration.ofSeconds(30))
				.untilAsserted(() -> {
					try (KafkaConsumer<String, CreateCustomerAvroEvent> consumer = new KafkaConsumer<>(containerFactory.getConsumerFactory().getConfigurationProperties())) {
						String createCustomerRetryTopicName = createCustomerTopicName + "-retry";
						consumer.subscribe(Collections.singletonList(createCustomerRetryTopicName));
						ConsumerRecords<String, CreateCustomerAvroEvent> records = consumer.poll(Duration.ofSeconds(10));
						assertThat(records.records(createCustomerRetryTopicName)).hasSize(1);
						Optional<Event> event = eventRepository.findById(createCustomerAvroEvent.getId().toString());
						assertThat(event).isEmpty();
						Optional<Snapshot> snapshot = snapshotRepository.findByAggregateId(1L);
						assertThat(snapshot).isEmpty();
					}
				});

		await()
				.pollInterval(POLL_INTERVAL)
				.atMost(Duration.ofSeconds(60))
				.untilAsserted(() -> {
					try (KafkaConsumer<String, CreateCustomerAvroEvent> consumer = new KafkaConsumer<>(containerFactory.getConsumerFactory().getConfigurationProperties())) {
						String createCustomerDltTopicName = createCustomerTopicName + "-dlt";
						consumer.subscribe(Collections.singletonList(createCustomerDltTopicName));
						ConsumerRecords<String, CreateCustomerAvroEvent> records = consumer.poll(Duration.ofSeconds(5));
						StreamSupport.stream(records.spliterator(), false)
								.filter(consumerRecord -> CREATE_CUSTOMER.getName().contentEquals(consumerRecord.value().getEventType()))
								.forEach(consumerRecord -> {
									assertThat(consumerRecord.topic()).isEqualTo(createCustomerDltTopicName);
									Optional<ErrorEvent> optionalErrorEvent = errorEventRepository.findById(createCustomerAvroEvent.getId().toString());
									assertThat(optionalErrorEvent).isNotEmpty();
									ErrorEvent errorEvent = optionalErrorEvent.get();
									assertThat(errorEvent.getId()).isEqualTo(createCustomerAvroEvent.getId());
									assertThat(errorEvent.getEventType()).isEqualTo(createCustomerAvroEvent.getEventType());
									assertThat(errorEvent.getTimestamp()).isNotNull();
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
}
