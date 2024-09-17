package com.zhigalko.consumer.service;

import com.zhigalko.common.schema.CreateCustomerAvroEvent;
import com.zhigalko.common.schema.DeleteCustomerAvroEvent;
import com.zhigalko.common.schema.UpdateCustomerAddressAvroEvent;
import com.zhigalko.common.schema.UpdateCustomerNameAvroEvent;

public interface EventService {
	void createCustomer(CreateCustomerAvroEvent event);
	void updateCustomerName(UpdateCustomerNameAvroEvent event);
	void updateCustomerAddress(UpdateCustomerAddressAvroEvent updateCustomerAddressAvroEvent);
	void deleteCustomer(DeleteCustomerAvroEvent event);
}
