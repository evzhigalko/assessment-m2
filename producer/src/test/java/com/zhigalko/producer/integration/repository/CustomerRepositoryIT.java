package com.zhigalko.producer.integration.repository;

import com.zhigalko.core.domain.model.Customer;
import com.zhigalko.core.projection.CustomerProjection;
import com.zhigalko.producer.repository.CustomerRepository;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import static com.zhigalko.producer.util.TestDataUtil.getCustomer;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
class CustomerRepositoryIT {

	@Container
	private static final MongoDBContainer MONGO_DB_CONTAINER = new MongoDBContainer(DockerImageName.parse("mongo:latest"));

	@DynamicPropertySource
	private static void registerKafkaAndMongoProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.data.mongodb.uri", MONGO_DB_CONTAINER::getReplicaSetUrl);
	}

	@Autowired
	private CustomerRepository customerRepository;

	@Test
	void findByCustomerId() {
		Customer customer = getCustomer();
		customerRepository.save(customer);

		Optional<CustomerProjection> optionalCustomerProjection = customerRepository.findByCustomerId(customer.getCustomerId());

		assertThat(optionalCustomerProjection).isNotEmpty();
		CustomerProjection customerProjection = optionalCustomerProjection.get();
		assertThat(customerProjection.id()).isEqualTo(customer.getCustomerId());
		assertThat(customerProjection.name()).isEqualTo(customer.getName());
		assertThat(customerProjection.address()).isEqualTo(customer.getAddress());
	}

	@Test
	void findByCustomerId_notFound() {
		Customer customer = getCustomer();

		Optional<CustomerProjection> optionalCustomerProjection = customerRepository.findByCustomerId(customer.getCustomerId());

		assertThat(optionalCustomerProjection).isEmpty();
	}

	@AfterEach
	void tearDown() {
		customerRepository.deleteAll();
	}
}