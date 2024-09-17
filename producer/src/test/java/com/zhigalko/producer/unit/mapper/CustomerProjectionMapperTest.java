package com.zhigalko.producer.unit.mapper;

import com.zhigalko.common.domain.model.Customer;
import com.zhigalko.common.projection.CustomerProjection;
import com.zhigalko.producer.mapper.CustomerProjectionMapper;
import com.zhigalko.producer.mapper.CustomerProjectionMapperImpl;
import org.junit.jupiter.api.Test;
import static com.zhigalko.producer.util.TestDataUtil.getCustomer;
import static org.assertj.core.api.Assertions.assertThat;

class CustomerProjectionMapperTest {
	private final CustomerProjectionMapper mapper = new CustomerProjectionMapperImpl();

	@Test
	void toCustomerProjection() {
		Customer customer = getCustomer();

		CustomerProjection customerProjection = mapper.toCustomerProjection(customer);

		assertThat(customerProjection.id()).isEqualTo(customer.getCustomerId());
		assertThat(customerProjection.name()).isEqualTo(customer.getName());
		assertThat(customerProjection.address()).isEqualTo(customer.getAddress());
	}

	@Test
	void toCustomerProjection_null() {
		CustomerProjection customerProjection = mapper.toCustomerProjection(null);

		assertThat(customerProjection).isNull();
	}
}
