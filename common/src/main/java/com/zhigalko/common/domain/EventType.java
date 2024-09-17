package com.zhigalko.common.domain;

import lombok.Getter;

@Getter
public enum EventType {
	CREATE_CUSTOMER("CreateCustomerEvent"),
	CREATE_CUSTOMER_VIEW("CreateCustomerViewEvent"),
	UPDATE_CUSTOMER_NAME("UpdateCustomerNameEvent"),
	UPDATE_CUSTOMER_NAME_VIEW("UpdateCustomerNameViewEvent"),
	UPDATE_CUSTOMER_ADDRESS("UpdateCustomerAddressEvent"),
	UPDATE_CUSTOMER_ADDRESS_VIEW("UpdateCustomerAddressViewEvent"),
	DELETE_CUSTOMER("DeleteCustomerEvent"),
	DELETE_CUSTOMER_VIEW("DeleteCustomerViewEvent");

	private final String name;

	EventType(String name) {
		this.name = name;
	}
}
