package com.zhigalko.consumer.service;

import com.zhigalko.core.schema.CreateCustomerAvroEvent;
import com.zhigalko.core.schema.DeleteCustomerAvroEvent;
import com.zhigalko.core.schema.UpdateCustomerAddressAvroEvent;
import com.zhigalko.core.schema.UpdateCustomerNameAvroEvent;

public interface EventService {
	void createCustomer(CreateCustomerAvroEvent event);
	void updateCustomerName(UpdateCustomerNameAvroEvent event);
	void updateCustomerAddress(UpdateCustomerAddressAvroEvent updateCustomerAddressAvroEvent);
	void deleteCustomer(DeleteCustomerAvroEvent event);
}
