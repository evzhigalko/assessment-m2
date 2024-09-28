package com.zhigalko.common.integration;

import com.zhigalko.common.integration.config.KafkaTestConfig;
import com.zhigalko.common.schema.CreateCustomerAvroEvent;
import com.zhigalko.common.service.KafkaProducer;
import com.zhigalko.common.util.KafkaCustomProperties;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.UUID;
import java.util.stream.StreamSupport;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import static com.zhigalko.common.domain.EventType.CREATE_CUSTOMER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.awaitility.Awaitility.await;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
@Import({KafkaTestConfig.class})
public class KafkaProducerIT {
	private static final Network NETWORK = Network.newNetwork();
	public static final Duration POLL_INTERVAL = Duration.ofSeconds(3);
	public static final Duration MAX_DURATION = Duration.ofSeconds(12);

	@Container
	public static final KafkaContainer KAFKA_CONTAINER = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:latest"))
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
	}

	@Autowired
	private KafkaProducer kafkaProducer;

	@Autowired
	private KafkaCustomProperties kafkaCustomProperties;

	@Autowired
	private ConcurrentKafkaListenerContainerFactory<String, Object> containerFactory;

	@Test
	void sendMessage() {
		CreateCustomerAvroEvent createCustomerAvroEvent = getCreateCustomerAvroEvent();
		kafkaProducer.sendMessage(createCustomerAvroEvent, kafkaCustomProperties.getCreateCustomerEventTopic().getName());

		await()
				.pollInterval(POLL_INTERVAL)
				.atMost(MAX_DURATION)
				.untilAsserted(() -> {
					try (KafkaConsumer<String, CreateCustomerAvroEvent> consumer = new KafkaConsumer<>(containerFactory.getConsumerFactory().getConfigurationProperties())) {
						consumer.subscribe(Collections.singletonList(kafkaCustomProperties.getCreateCustomerEventTopic().getName()));
						ConsumerRecords<String, CreateCustomerAvroEvent> records = consumer.poll(Duration.ofSeconds(10));
						if (records.isEmpty()) {
							fail("event was not received");
						}
						StreamSupport.stream(records.spliterator(), false)
								.filter(record -> CREATE_CUSTOMER.getName().contentEquals(record.value().getEventType()))
								.forEach(record -> {
									CreateCustomerAvroEvent consumedEvent = record.value();
									assertThat(consumedEvent.getId()).isNotBlank();
									assertThat(consumedEvent.getEventType().toString()).hasToString(CREATE_CUSTOMER.getName());
									assertThat(consumedEvent.getTimestamp()).isNotBlank();
									assertThat(consumedEvent.getName().toString()).hasToString(createCustomerAvroEvent.getName().toString());
									assertThat(consumedEvent.getAddress().toString()).hasToString(createCustomerAvroEvent.getAddress().toString());
								});
						consumer.unsubscribe();
					}
				});
	}

	public CreateCustomerAvroEvent getCreateCustomerAvroEvent() {
		return new CreateCustomerAvroEvent(
				UUID.randomUUID().toString(),
				"Alex",
				"New York",
				Instant.now().toString(),
				CREATE_CUSTOMER.getName()
		);
	}
}
