package com.zhigalko.core.event;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class CreateCustomerEvent extends Event {
	private Payload payload;

	@Getter
	@Setter
	@ToString
	public static class Payload {
		private String name;
		private String address;
	}

	public CreateCustomerEvent(Long aggregateId, String eventType, Payload payload) {
		super(aggregateId, eventType);
		this.payload = payload;
	}
}
