package com.zhigalko.producer.unit.mapper;

import com.zhigalko.core.domain.model.Customer;
import com.zhigalko.core.schema.CustomerViewAvroEvent;
import com.zhigalko.producer.mapper.CustomerMapper;
import com.zhigalko.producer.mapper.CustomerMapperImpl;
import com.zhigalko.producer.util.TestDataUtil;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class CustomerMapperTest {
	private final CustomerMapper mapper = new CustomerMapperImpl();

	@Test
	void toCustomer() {
		CustomerViewAvroEvent event = TestDataUtil.getCustomerViewAvroEvent();

		Customer customer = mapper.toCustomer(event);

		assertThat(customer.getCustomerId()).isEqualTo(event.getAggregateId());
		assertThat(customer.getName()).isEqualTo(event.getName());
		assertThat(customer.getAddress()).isEqualTo(event.getAddress());
	}

	@Test
	void toCustomer_null() {
		Customer customer = mapper.toCustomer(null);

		assertThat(customer).isNull();
	}

	@Test
	void testToString() {
		String testValue = "test";

		String string = mapper.toString(testValue);

		assertThat(string).hasToString(testValue);
	}
}
