package com.zhigalko.common.event;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class UpdateCustomerAddressEvent extends Event {
	private Payload payload;

	@Getter
	@Setter
	@ToString
	public static class Payload {
		private String address;
	}

	public UpdateCustomerAddressEvent(Long aggregateId, String eventType, Payload payload) {
		super(aggregateId, eventType);
		this.payload = payload;
	}
}
