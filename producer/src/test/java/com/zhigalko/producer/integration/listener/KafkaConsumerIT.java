package com.zhigalko.producer.integration.listener;

import com.redis.testcontainers.RedisContainer;
import com.zhigalko.core.config.KafkaProducerConfig;
import com.zhigalko.core.domain.model.Customer;
import com.zhigalko.core.projection.CustomerProjection;
import com.zhigalko.core.schema.CustomerViewAvroEvent;
import com.zhigalko.core.service.KafkaProducer;
import com.zhigalko.core.util.KafkaCustomProperties;
import com.zhigalko.producer.repository.CustomerRepository;
import com.zhigalko.producer.service.CacheService;
import com.zhigalko.producer.service.CustomerQueryService;
import io.confluent.kafka.serializers.KafkaAvroSerializerConfig;
import jakarta.annotation.PostConstruct;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisTemplate;
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
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import static com.zhigalko.core.domain.EventType.DELETE_CUSTOMER_VIEW;
import static com.zhigalko.core.domain.EventType.UPDATE_CUSTOMER_ADDRESS_VIEW;
import static com.zhigalko.core.domain.EventType.UPDATE_CUSTOMER_NAME_VIEW;
import static com.zhigalko.core.util.Util.fromJsonToCustomerProjection;
import static com.zhigalko.producer.constants.CommonConstant.CACHE_KEY;
import static com.zhigalko.producer.util.TestDataUtil.getCustomer;
import static com.zhigalko.producer.util.TestDataUtil.getCustomerViewAvroEvent;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest
@Testcontainers
@Import({KafkaConsumerIT.Config.class})
public class KafkaConsumerIT {

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
	public static final RedisContainer REDIS_CONTAINER = new RedisContainer(DockerImageName.parse("redis:latest"))
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
	private static void registerKafkaMongoProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.kafka.bootstrap-servers", KAFKA_CONTAINER::getBootstrapServers);
		registry.add("spring.kafka.properties.schema.registry.url",
				() -> "http://" + SCHEMA_REGISTRY.getHost() + ":" + SCHEMA_REGISTRY.getFirstMappedPort());
		registry.add("spring.data.mongodb.uri", MONGO_DB_CONTAINER::getReplicaSetUrl);
		registry.add("spring.data.redis.host", REDIS_CONTAINER::getHost);
		registry.add("spring.data.redis.port", REDIS_CONTAINER::getFirstMappedPort);
	}

	@Autowired
	private KafkaProducer kafkaProducer;

	@Autowired
	private KafkaCustomProperties kafkaCustomProperties;

	@Autowired
	private CustomerRepository customerRepository;

	@Autowired
	private CacheService cacheService;

	@Autowired
	private RedisTemplate<String, String> redisTemplate;

	@Autowired
	private CustomerQueryService customerQueryService;

	@Test
	void listenCustomerViewTopic_create() {
		CustomerViewAvroEvent customerViewAvroEvent = getCustomerViewAvroEvent();
		kafkaProducer.sendMessage(customerViewAvroEvent, kafkaCustomProperties.getCustomerViewEventTopic().getName());

		await()
				.pollInterval(POLL_INTERVAL)
				.atMost(MAX_DURATION)
				.untilAsserted(() -> {
					CustomerProjection projection = customerRepository.findByCustomerId(customerViewAvroEvent.getAggregateId())
							.orElseThrow();
					assertThat(projection).isNotNull();
					assertThat(projection.name()).isEqualTo(customerViewAvroEvent.getName());
					assertThat(projection.address()).isEqualTo(customerViewAvroEvent.getAddress());
					String cachedValue = cacheService.get(CACHE_KEY.apply(String.valueOf(projection.id())));
					CustomerProjection cachedProjection = fromJsonToCustomerProjection(cachedValue);
					assertThat(cachedProjection).isEqualTo(projection);
				});
	}

	@Test
	void listenCustomerViewTopic_updateName() {
		Customer customer = getCustomer();
		customerQueryService.saveCustomerProjection(customer);

		CustomerViewAvroEvent customerViewAvroEvent = getCustomerViewAvroEvent();
		customerViewAvroEvent.setName("Tom");
		customerViewAvroEvent.setEventType(UPDATE_CUSTOMER_NAME_VIEW.getName());
		kafkaProducer.sendMessage(customerViewAvroEvent, kafkaCustomProperties.getCustomerViewEventTopic().getName());

		await()
				.pollInterval(POLL_INTERVAL)
				.atMost(MAX_DURATION)
				.untilAsserted(() -> {
					CustomerProjection foundProjection = customerRepository.findByCustomerId(customerViewAvroEvent.getAggregateId())
							.orElseThrow();
					assertThat(foundProjection).isNotNull();
					assertThat(foundProjection.name()).isEqualTo(customerViewAvroEvent.getName());
					assertThat(foundProjection.address()).isEqualTo(customerViewAvroEvent.getAddress());
					String cachedValue = cacheService.get(CACHE_KEY.apply(String.valueOf(foundProjection.id())));
					CustomerProjection cachedProjection = fromJsonToCustomerProjection(cachedValue);
					assertThat(cachedProjection).isEqualTo(foundProjection);
				});
	}

	@Test
	void listenCustomerViewTopic_updateAddress() {
		Customer customer = getCustomer();
		customerQueryService.saveCustomerProjection(customer);

		CustomerViewAvroEvent customerViewAvroEvent = getCustomerViewAvroEvent();
		customerViewAvroEvent.setAddress("London");
		customerViewAvroEvent.setEventType(UPDATE_CUSTOMER_ADDRESS_VIEW.getName());
		kafkaProducer.sendMessage(customerViewAvroEvent, kafkaCustomProperties.getCustomerViewEventTopic().getName());

		await()
				.pollInterval(POLL_INTERVAL)
				.atMost(MAX_DURATION)
				.untilAsserted(() -> {
					CustomerProjection foundProjection = customerRepository.findByCustomerId(customerViewAvroEvent.getAggregateId())
							.orElseThrow();
					assertThat(foundProjection).isNotNull();
					assertThat(foundProjection.name()).isEqualTo(customerViewAvroEvent.getName());
					assertThat(foundProjection.address()).isEqualTo(customerViewAvroEvent.getAddress());
					String cachedValue = cacheService.get(CACHE_KEY.apply(String.valueOf(foundProjection.id())));
					CustomerProjection cachedProjection = fromJsonToCustomerProjection(cachedValue);
					assertThat(cachedProjection).isEqualTo(foundProjection);
				});
	}

	@Test
	void listenCustomerViewTopic_delete() {
		Customer customer = getCustomer();
		customerQueryService.saveCustomerProjection(customer);

		CustomerViewAvroEvent customerViewAvroEvent = getCustomerViewAvroEvent();
		customerViewAvroEvent.setEventType(DELETE_CUSTOMER_VIEW.getName());
		kafkaProducer.sendMessage(customerViewAvroEvent, kafkaCustomProperties.getCustomerViewEventTopic().getName());

		await()
				.pollInterval(POLL_INTERVAL)
				.atMost(MAX_DURATION)
				.untilAsserted(() -> {
					Optional<CustomerProjection> customerProjection = customerRepository.findByCustomerId(customerViewAvroEvent.getAggregateId());
					assertThat(customerProjection).isEmpty();
					String cachedValue = cacheService.get(CACHE_KEY.apply(String.valueOf(customerViewAvroEvent.getAggregateId())));
					assertThat(cachedValue).isBlank();
				});
	}

	@AfterEach
	void tearDown() {
		Objects.requireNonNull(redisTemplate.getConnectionFactory()).getConnection().serverCommands().flushDb();
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
