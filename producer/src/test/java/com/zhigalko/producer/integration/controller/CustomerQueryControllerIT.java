package com.zhigalko.producer.integration.controller;

import com.redis.testcontainers.RedisContainer;
import com.zhigalko.common.domain.model.Customer;
import com.zhigalko.common.projection.CustomerProjection;
import com.zhigalko.common.query.GetCustomerById;
import com.zhigalko.common.util.Util;
import com.zhigalko.producer.integration.DatabaseIntegrationTest;
import com.zhigalko.producer.service.CacheService;
import com.zhigalko.producer.service.CustomerQueryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.utility.DockerImageName;
import static com.zhigalko.producer.constants.CommonConstant.CACHE_KEY;
import static com.zhigalko.producer.util.TestDataUtil.getCustomer;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
class CustomerQueryControllerIT extends DatabaseIntegrationTest {
	private static final String GET_QUERY_URI = "/api/v1/customers/";

	@Container
	private static final MongoDBContainer MONGO_DB_CONTAINER = new MongoDBContainer(DockerImageName.parse("mongo:latest"));

	@Container
	public static final RedisContainer REDIS_CONTAINER = new RedisContainer(DockerImageName.parse("redis:latest"));

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private CustomerQueryService customerQueryService;

	@Autowired
	private CacheService cacheService;

	private Customer customer;

	@DynamicPropertySource
	private static void registerRedisAndMongoProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.data.mongodb.uri", MONGO_DB_CONTAINER::getReplicaSetUrl);
		registry.add("spring.data.redis.host", REDIS_CONTAINER::getHost);
		registry.add("spring.data.redis.port", REDIS_CONTAINER::getFirstMappedPort);
	}

	@BeforeEach
	void setUp() {
		customer = getCustomer();
		customerQueryService.saveCustomerProjection(customer);
	}

	@Test
	void containersAreRun() {
		assertThat(MONGO_DB_CONTAINER.isRunning()).isTrue();
		assertThat(REDIS_CONTAINER.isRunning()).isTrue();
	}

	@Test
	void getCustomerById_getFromCache() throws Exception {
		GetCustomerById query = new GetCustomerById(1L);

		mockMvc.perform(get(GET_QUERY_URI + query.id()))
				.andExpectAll(status().isOk(),
						jsonPath("$.id", is(1)),
						jsonPath("$.name", is("Alex")),
						jsonPath("$.address", is("New York")));

		CustomerProjection customerProjection = customerQueryService.getCustomerProjection(query);
		assertThat(customerProjection.id()).isEqualTo(customer.getCustomerId());
		assertThat(customerProjection.name()).isEqualTo(customer.getName());
		assertThat(customerProjection.address()).isEqualTo(customer.getAddress());

		String key = CACHE_KEY.apply(customer.getCustomerId().toString());
		assertThat(key).isNotBlank();

		String cachedProjectionJson = cacheService.get(key);
		CustomerProjection cachedProjection = Util.fromJsonToCustomerProjection(cachedProjectionJson);
		assertThat(cachedProjection.id()).isEqualTo(customer.getCustomerId());
		assertThat(cachedProjection.name()).isEqualTo(customer.getName());
		assertThat(cachedProjection.address()).isEqualTo(customer.getAddress());
	}

	@Test
	void getCustomerById_getFromDb() throws Exception {
		GetCustomerById query = new GetCustomerById(1L);
		String key = CACHE_KEY.apply(customer.getCustomerId().toString());
		cacheService.delete(key);

		mockMvc.perform(get(GET_QUERY_URI + query.id()))
				.andExpectAll(status().isOk(),
						jsonPath("$.id", is(1)),
						jsonPath("$.name", is("Alex")),
						jsonPath("$.address", is("New York")));

		CustomerProjection customerProjection = customerQueryService.getCustomerProjection(query);
		assertThat(customerProjection.id()).isEqualTo(customer.getCustomerId());
		assertThat(customerProjection.name()).isEqualTo(customer.getName());
		assertThat(customerProjection.address()).isEqualTo(customer.getAddress());

		assertThat(key).isNotBlank();

		String cachedProjectionJson = cacheService.get(key);
		CustomerProjection cachedProjection = Util.fromJsonToCustomerProjection(cachedProjectionJson);
		assertThat(cachedProjection.id()).isEqualTo(customer.getCustomerId());
		assertThat(cachedProjection.name()).isEqualTo(customer.getName());
		assertThat(cachedProjection.address()).isEqualTo(customer.getAddress());
	}

	@Test
	void getCustomerById_getFromDb_throwsException() throws Exception {
		GetCustomerById query = new GetCustomerById(2L);
		mockMvc.perform(get(GET_QUERY_URI + query.id()))
				.andExpectAll(status().isNotFound(),
						jsonPath("$.message", is("Customer not found")),
						jsonPath("$.traceId").isNotEmpty());
	}
}
