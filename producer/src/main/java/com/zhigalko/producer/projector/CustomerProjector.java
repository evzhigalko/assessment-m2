package com.zhigalko.producer.projector;

import com.zhigalko.core.domain.model.Customer;
import com.zhigalko.core.schema.CustomerViewAvroEvent;
import com.zhigalko.producer.mapper.CustomerMapper;
import com.zhigalko.producer.service.CustomerQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import static com.zhigalko.core.domain.EventType.DELETE_CUSTOMER_VIEW;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerProjector {
	private final CustomerQueryService customerQueryService;
	private final CustomerMapper customerMapper;

	public void project(CustomerViewAvroEvent event) {
		if (DELETE_CUSTOMER_VIEW.getName().contentEquals(event.getEventType())) {
			long customerId = event.getAggregateId();
			customerQueryService.deleteCustomerProjection(customerId);
			log.info("Deleted customer projection for: {}", customerId);
			return;
		}
		Customer customer = customerMapper.toCustomer(event);
		log.info("Updated customer projection for: {}", customer.getCustomerId());
		customerQueryService.saveCustomerProjection(customer);
	}
}
