package com.zhigalko.producer.service;

import com.zhigalko.common.domain.model.Customer;
import com.zhigalko.common.projection.CustomerProjection;
import com.zhigalko.common.query.GetCustomerById;

public interface CustomerQueryService {
	CustomerProjection getCustomerProjection(GetCustomerById query);
	void saveCustomerProjection(Customer customer);
	void deleteCustomerProjection(Long customerId);
}
