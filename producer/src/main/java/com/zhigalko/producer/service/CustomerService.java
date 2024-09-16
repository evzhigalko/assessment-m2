package com.zhigalko.producer.service;

import com.zhigalko.producer.dto.CreateCustomerDto;
import com.zhigalko.producer.dto.patch.UpdateCustomerPatch;

public interface CustomerService {
	void createCustomer(CreateCustomerDto createCustomerDto) ;
	void updateCustomer(Long aggregateId, UpdateCustomerPatch patch);
	void deleteCustomer(Long customerId);
}
