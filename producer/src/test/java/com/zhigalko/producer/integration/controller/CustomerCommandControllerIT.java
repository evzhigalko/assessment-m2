package com.zhigalko.producer.integration.controller;

import com.zhigalko.common.schema.CreateCustomerAvroEvent;
import com.zhigalko.common.schema.DeleteCustomerAvroEvent;
import com.zhigalko.common.schema.UpdateCustomerAddressAvroEvent;
import com.zhigalko.common.schema.UpdateCustomerNameAvroEvent;
import com.zhigalko.common.util.KafkaCustomProperties;
import com.zhigalko.producer.dto.CreateCustomerDto;
import com.zhigalko.producer.dto.patch.UpdateCustomerPatch;
import com.zhigalko.producer.integration.KafkaIntegrationTest;
import java.time.Duration;
import java.util.Collections;
import java.util.function.UnaryOperator;
import java.util.stream.StreamSupport;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.utility.DockerImageName;
import static com.zhigalko.common.domain.EventType.CREATE_CUSTOMER;
import static com.zhigalko.common.domain.EventType.DELETE_CUSTOMER;
import static com.zhigalko.common.domain.EventType.UPDATE_CUSTOMER_ADDRESS;
import static com.zhigalko.common.domain.EventType.UPDATE_CUSTOMER_NAME;
import static com.zhigalko.common.util.Util.toJson;
import static com.zhigalko.producer.dto.patch.UpdateCustomerPatch.REPLACE_OP;
import static com.zhigalko.producer.dto.patch.UpdateCustomerPatch.UPDATE_ADDRESS;
import static com.zhigalko.producer.dto.patch.UpdateCustomerPatch.UPDATE_NAME;
import static com.zhigalko.producer.util.TestDataUtil.getCustomer;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
class CustomerCommandControllerIT extends KafkaIntegrationTest {
	private static final String BASE_URI = "/api/v1/customers";
	private static final UnaryOperator<String> URI = id -> BASE_URI + "/" + id;
	private static final Duration POLL_INTERVAL = Duration.ofSeconds(3);
	private static final Duration MAX_DURATION = Duration.ofSeconds(25);

	@Container
	private static final MongoDBContainer MONGO_DB_CONTAINER = new MongoDBContainer(DockerImageName.parse("mongo:latest"))
			.withNetwork(NETWORK);

	@DynamicPropertySource
	private static void registerKafkaMongoProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.data.mongodb.uri", MONGO_DB_CONTAINER::getReplicaSetUrl);
	}

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ConcurrentKafkaListenerContainerFactory<String, Object> containerFactory;

	@Autowired
	private KafkaCustomProperties kafkaCustomProperties;

	@Test
	void containersAreRun() {
		assertThat(KAFKA_CONTAINER.isRunning()).isTrue();
		assertThat(MONGO_DB_CONTAINER.isRunning()).isTrue();
		assertThat(SCHEMA_REGISTRY.isRunning()).isTrue();
	}

	@Test
	void createCustomer() throws Exception {
		CreateCustomerDto createCustomerDto = new CreateCustomerDto("Alex", "London");

		mockMvc.perform(post(BASE_URI)
						.contentType(MediaType.APPLICATION_JSON)
						.content(toJson(createCustomerDto)))
				.andExpectAll(status().isCreated());

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
								.filter(consumerRecord -> CREATE_CUSTOMER.getName().contentEquals(consumerRecord.value().getEventType()))
								.forEach(consumerRecord -> {
									CreateCustomerAvroEvent consumedEvent = consumerRecord.value();
									assertThat(consumedEvent.getId()).isNotBlank();
									assertThat(consumedEvent.getEventType()).hasToString(CREATE_CUSTOMER.getName());
									assertThat(consumedEvent.getTimestamp()).isNotBlank();
									assertThat(consumedEvent.getName()).hasToString(createCustomerDto.name());
									assertThat(consumedEvent.getAddress()).hasToString(createCustomerDto.address());
								});
						consumer.unsubscribe();
					}
				});
	}

	@Test
	void createCustomer_nullName() throws Exception {
		CreateCustomerDto createCustomerDto = new CreateCustomerDto(null, "New York");

		mockMvc.perform(post(BASE_URI)
						.contentType(MediaType.APPLICATION_JSON)
						.content(toJson(createCustomerDto)))
				.andExpectAll(status().isBadRequest(),
						jsonPath("$.message", is("Customer name can not be blank")),
						jsonPath("$.traceId").isNotEmpty());
	}

	@Test
	void createCustomer_nullAddress() throws Exception {
		CreateCustomerDto createCustomerDto = new CreateCustomerDto("Alex", null);

		mockMvc.perform(post(BASE_URI)
						.contentType(MediaType.APPLICATION_JSON)
						.content(toJson(createCustomerDto)))
				.andExpectAll(status().isBadRequest(),
						jsonPath("$.message", is("Customer address can not be blank")),
						jsonPath("$.traceId").isNotEmpty());
	}

	@Test
	void resourceNotFound_invalidUri() throws Exception {
		CreateCustomerDto createCustomerDto = new CreateCustomerDto("Alex", null);

		mockMvc.perform(post(BASE_URI  + "dsf")
						.contentType(MediaType.APPLICATION_JSON)
						.content(toJson(createCustomerDto)))
				.andExpectAll(status().isNotFound(),
						jsonPath("$.message", is("Such resource not found. Kindly check your request details.")),
						jsonPath("$.traceId").isNotEmpty());
	}

	@Test
	void updateCustomerName() throws Exception {
		Long customerId = getCustomer().getCustomerId();
		UpdateCustomerPatch patch = new UpdateCustomerPatch();
		patch.setOp(REPLACE_OP);
		patch.setPath(UPDATE_NAME);
		patch.setValue("Tom");

		mockMvc.perform(patch(URI.apply(String.valueOf(customerId)))
						.contentType(MediaType.parseMediaType("application/json-patch+json"))
						.content(toJson(patch)))
				.andExpectAll(status().isAccepted());

		await()
				.pollInterval(POLL_INTERVAL)
				.atMost(MAX_DURATION)
				.untilAsserted(() -> {
					try (KafkaConsumer<String, UpdateCustomerNameAvroEvent> consumer = new KafkaConsumer<>(containerFactory.getConsumerFactory().getConfigurationProperties())) {
						consumer.subscribe(Collections.singletonList(kafkaCustomProperties.getUpdateCustomerNameEventTopic().getName()));
						ConsumerRecords<String, UpdateCustomerNameAvroEvent> records = consumer.poll(Duration.ofSeconds(10));
						if (records.isEmpty()) {
							fail("event was not received");
						}
						StreamSupport.stream(records.spliterator(), false)
								.filter(consumerRecord -> UPDATE_CUSTOMER_NAME.getName().contentEquals(consumerRecord.value().getEventType()))
								.forEach(consumerRecord -> {
									UpdateCustomerNameAvroEvent consumedEvent = consumerRecord.value();
									assertThat(consumedEvent.getId()).isNotBlank();
									assertThat(consumedEvent.getAggregateId()).isEqualTo(customerId);
									assertThat(consumedEvent.getEventType()).hasToString(UPDATE_CUSTOMER_NAME.getName());
									assertThat(consumedEvent.getTimestamp()).isNotBlank();
								});
						consumer.unsubscribe();
					}
				});
	}

	@Test
	void updateCustomerAddress() throws Exception {
		Long customerId = getCustomer().getCustomerId();
		UpdateCustomerPatch patch = new UpdateCustomerPatch();
		patch.setOp(REPLACE_OP);
		patch.setPath(UPDATE_ADDRESS);
		patch.setValue("Tokio");

		mockMvc.perform(patch(URI.apply(String.valueOf(customerId)))
						.contentType(MediaType.parseMediaType("application/json-patch+json"))
						.content(toJson(patch)))
				.andExpectAll(status().isAccepted());

		await()
				.pollInterval(POLL_INTERVAL)
				.atMost(MAX_DURATION)
				.untilAsserted(() -> {
					try (KafkaConsumer<String, UpdateCustomerAddressAvroEvent> consumer = new KafkaConsumer<>(containerFactory.getConsumerFactory().getConfigurationProperties())) {
						consumer.subscribe(Collections.singletonList(kafkaCustomProperties.getUpdateCustomerAddressEventTopic().getName()));
						ConsumerRecords<String, UpdateCustomerAddressAvroEvent> records = consumer.poll(Duration.ofSeconds(10));
						if (records.isEmpty()) {
							fail("event was not received");
						}
						StreamSupport.stream(records.spliterator(), false)
								.filter(consumerRecord -> UPDATE_CUSTOMER_ADDRESS.getName().contentEquals(consumerRecord.value().getEventType()))
								.forEach(consumerRecord -> {
									UpdateCustomerAddressAvroEvent consumedEvent = consumerRecord.value();
									assertThat(consumedEvent.getId()).isNotBlank();
									assertThat(consumedEvent.getAggregateId()).isEqualTo(customerId);
									assertThat(consumedEvent.getEventType()).hasToString(UPDATE_CUSTOMER_ADDRESS.getName());
									assertThat(consumedEvent.getTimestamp()).isNotBlank();
								});
						consumer.unsubscribe();
					}
				});
	}

	@Test
	void deleteCustomer() throws Exception {
		Long customerId = getCustomer().getCustomerId();

		mockMvc.perform(delete(URI.apply(String.valueOf(customerId)))
						.contentType(MediaType.APPLICATION_JSON))
				.andExpectAll(status().isNoContent());

		await()
				.pollInterval(POLL_INTERVAL)
				.atMost(MAX_DURATION)
				.untilAsserted(() -> {
					try (KafkaConsumer<String, DeleteCustomerAvroEvent> consumer = new KafkaConsumer<>(containerFactory.getConsumerFactory().getConfigurationProperties())) {
						consumer.subscribe(Collections.singletonList(kafkaCustomProperties.getDeleteCustomerEventTopic().getName()));
						ConsumerRecords<String, DeleteCustomerAvroEvent> records = consumer.poll(Duration.ofSeconds(10));
						if (records.isEmpty()) {
							fail("event was not received");
						}
						StreamSupport.stream(records.spliterator(), false)
								.filter(consumerRecord -> DELETE_CUSTOMER.getName().contentEquals(consumerRecord.value().getEventType()))
								.forEach(consumerRecord -> {
									DeleteCustomerAvroEvent consumedEvent = consumerRecord.value();
									assertThat(consumedEvent.getId()).isNotBlank();
									assertThat(consumedEvent.getAggregateId()).isEqualTo(customerId);
									assertThat(consumedEvent.getEventType()).hasToString(DELETE_CUSTOMER.getName());
									assertThat(consumedEvent.getTimestamp()).isNotBlank();
								});
						consumer.unsubscribe();
					}
				});
	}
}
