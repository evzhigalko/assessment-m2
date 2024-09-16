package com.zhigalko.producer.service;

import com.zhigalko.core.domain.model.Customer;
import com.zhigalko.core.projection.CustomerProjection;
import com.zhigalko.core.query.GetCustomerById;

public interface CustomerQueryService {
	CustomerProjection getCustomerProjection(GetCustomerById query);
	void saveCustomerProjection(Customer customer);
	void deleteCustomerProjection(Long customerId);
}
