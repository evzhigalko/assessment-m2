package com.zhigalko.producer.integration.listener;

import com.redis.testcontainers.RedisContainer;
import com.zhigalko.common.domain.model.Customer;
import com.zhigalko.common.projection.CustomerProjection;
import com.zhigalko.common.schema.CustomerViewAvroEvent;
import com.zhigalko.common.service.KafkaProducer;
import com.zhigalko.common.util.KafkaCustomProperties;
import com.zhigalko.producer.integration.KafkaIntegrationTest;
import com.zhigalko.producer.projector.CustomerProjector;
import com.zhigalko.producer.repository.CustomerRepository;
import com.zhigalko.producer.service.CacheService;
import com.zhigalko.producer.service.CustomerQueryService;
import java.time.Duration;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.StreamSupport;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.utility.DockerImageName;
import static com.zhigalko.common.domain.EventType.DELETE_CUSTOMER_VIEW;
import static com.zhigalko.common.domain.EventType.UPDATE_CUSTOMER_ADDRESS_VIEW;
import static com.zhigalko.common.domain.EventType.UPDATE_CUSTOMER_NAME_VIEW;
import static com.zhigalko.common.util.Util.fromJsonToCustomerProjection;
import static com.zhigalko.producer.constants.CommonConstant.CACHE_KEY;
import static com.zhigalko.producer.util.TestDataUtil.getCustomer;
import static com.zhigalko.producer.util.TestDataUtil.getCustomerViewAvroEvent;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

public class KafkaConsumerIT extends KafkaIntegrationTest {
	private static final Duration POLL_INTERVAL = Duration.ofSeconds(3);
	private static final Duration MAX_DURATION = Duration.ofSeconds(12);

	@Container
	private static final MongoDBContainer MONGO_DB_CONTAINER = new MongoDBContainer(DockerImageName.parse("mongo:latest"))
			.withNetwork(NETWORK);

	@Container
	public static final RedisContainer REDIS_CONTAINER = new RedisContainer(DockerImageName.parse("redis:latest"))
			.withNetwork(NETWORK);

	@DynamicPropertySource
	private static void registerDbProperties(DynamicPropertyRegistry registry) {
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

	@Autowired
	private ConcurrentKafkaListenerContainerFactory<String, Object> containerFactory;

	@SpyBean
	private CustomerProjector customerProjector;

	@Test
	void containersAreRun() {
		assertThat(KAFKA_CONTAINER.isRunning()).isTrue();
		assertThat(MONGO_DB_CONTAINER.isRunning()).isTrue();
		assertThat(SCHEMA_REGISTRY.isRunning()).isTrue();
		assertThat(REDIS_CONTAINER.isRunning()).isTrue();
	}

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

	@Test
	void listenCustomerViewTopic_retry() {
		CustomerViewAvroEvent customerViewAvroEvent = getCustomerViewAvroEvent();
		customerViewAvroEvent.setAggregateId(customerViewAvroEvent.getAggregateId() + 1);
		String customerViewTopicName = kafkaCustomProperties.getCustomerViewEventTopic().getName();
		kafkaProducer.sendMessage(customerViewAvroEvent, customerViewTopicName);

		doThrow(new IllegalArgumentException())
				.doCallRealMethod()
				.when(customerProjector).project(any());

		await()
				.pollInterval(POLL_INTERVAL)
				.atMost(Duration.ofSeconds(6))
				.untilAsserted(() -> {
					Optional<CustomerProjection> projection = customerRepository.findByCustomerId(customerViewAvroEvent.getAggregateId());
					assertThat(projection).isEmpty();
					String cachedValue = cacheService.get(CACHE_KEY.apply(String.valueOf(customerViewAvroEvent.getAggregateId())));
					assertThat(cachedValue).isNull();
				});

		await()
				.pollInterval(POLL_INTERVAL)
				.atMost(Duration.ofSeconds(30))
				.untilAsserted(() -> {
					try (KafkaConsumer<String, CustomerViewAvroEvent> consumer = new KafkaConsumer<>(containerFactory.getConsumerFactory().getConfigurationProperties())) {
						String customerRetryTopicName = customerViewTopicName + "-retry";
						consumer.subscribe(Collections.singletonList(customerRetryTopicName));
						ConsumerRecords<String, CustomerViewAvroEvent> records = consumer.poll(Duration.ofSeconds(10));
						if (records.isEmpty()) {
							fail("event was not received");
						}
						StreamSupport.stream(records.spliterator(), false)
								.filter(consumerRecord -> consumerRecord.value().getId().equals(customerViewAvroEvent.getId()))
								.forEach(consumerRecord -> {
									assertThat(consumerRecord.topic()).isEqualTo(customerRetryTopicName);
									CustomerProjection projection = customerRepository.findByCustomerId(customerViewAvroEvent.getAggregateId())
											.orElseThrow();
									assertThat(projection).isNotNull();
									assertThat(projection.name()).isEqualTo(customerViewAvroEvent.getName());
									assertThat(projection.address()).isEqualTo(customerViewAvroEvent.getAddress());
									String cachedValue = cacheService.get(CACHE_KEY.apply(String.valueOf(projection.id())));
									CustomerProjection cachedProjection = fromJsonToCustomerProjection(cachedValue);
									assertThat(cachedProjection).isEqualTo(projection);
								});
						consumer.unsubscribe();
					}
				});
	}

	@Test
	void listenCustomerViewTopic_retry_dlt() {
		CustomerViewAvroEvent customerViewAvroEvent = getCustomerViewAvroEvent();
		customerViewAvroEvent.setAggregateId(customerViewAvroEvent.getAggregateId() + 2);
		String customerViewTopicName = kafkaCustomProperties.getCustomerViewEventTopic().getName();
		kafkaProducer.sendMessage(customerViewAvroEvent, customerViewTopicName);

		doThrow(new IllegalArgumentException())
				.when(customerProjector).project(any());

		await()
				.pollInterval(POLL_INTERVAL)
				.atMost(Duration.ofSeconds(6))
				.untilAsserted(() -> {
					Optional<CustomerProjection> projection = customerRepository.findByCustomerId(customerViewAvroEvent.getAggregateId());
					assertThat(projection).isEmpty();
					String cachedValue = cacheService.get(CACHE_KEY.apply(String.valueOf(customerViewAvroEvent.getAggregateId())));
					assertThat(cachedValue).isNull();
				});

		await()
				.pollInterval(POLL_INTERVAL)
				.atMost(Duration.ofSeconds(60))
				.untilAsserted(() -> {
					try (KafkaConsumer<String, CustomerViewAvroEvent> consumer = new KafkaConsumer<>(containerFactory.getConsumerFactory().getConfigurationProperties())) {
						String customerRetryTopicName = customerViewTopicName + "-retry";
						consumer.subscribe(Collections.singletonList(customerRetryTopicName));
						ConsumerRecords<String, CustomerViewAvroEvent> records = consumer.poll(Duration.ofSeconds(10));
						assertThat(records.records(customerRetryTopicName)).hasSize(1);
						Optional<CustomerProjection> customerProjection = customerRepository.findByCustomerId(customerViewAvroEvent.getAggregateId());
						assertThat(customerProjection).isEmpty();
						String cachedValue = cacheService.get(CACHE_KEY.apply(String.valueOf(customerViewAvroEvent.getAggregateId())));
						assertThat(cachedValue).isNull();
						consumer.unsubscribe();
					}
				});
		await()
				.pollInterval(POLL_INTERVAL)
				.atMost(Duration.ofSeconds(60))
				.untilAsserted(() -> {
					try (KafkaConsumer<String, CustomerViewAvroEvent> consumer = new KafkaConsumer<>(containerFactory.getConsumerFactory().getConfigurationProperties())) {
						String createCustomerDltTopicName = customerViewTopicName + "-dlt";
						consumer.subscribe(Collections.singletonList(createCustomerDltTopicName));
						ConsumerRecords<String, CustomerViewAvroEvent> records = consumer.poll(Duration.ofSeconds(10));
						StreamSupport.stream(records.spliterator(), false)
								.filter(consumerRecord -> consumerRecord.value().getId().equals(customerViewAvroEvent.getId()))
								.forEach(consumerRecord -> {
									assertThat(consumerRecord.topic()).isEqualTo(createCustomerDltTopicName);
									CustomerProjection projection = customerRepository.findByCustomerId(customerViewAvroEvent.getAggregateId())
											.orElseThrow();
									assertThat(projection).isNotNull();
									assertThat(projection.name()).isEqualTo(customerViewAvroEvent.getName());
									assertThat(projection.address()).isEqualTo(customerViewAvroEvent.getAddress());
									String cachedValue = cacheService.get(CACHE_KEY.apply(String.valueOf(projection.id())));
									CustomerProjection cachedProjection = fromJsonToCustomerProjection(cachedValue);
									assertThat(cachedProjection).isEqualTo(projection);
								});
						consumer.unsubscribe();
					}
				});
	}

	@AfterEach
	void tearDown() {
		Objects.requireNonNull(redisTemplate.getConnectionFactory()).getConnection().serverCommands().flushDb();
	}
}
