package com.zhigalko.common.event;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class UpdateCustomerNameEvent extends Event {
	private Payload payload;

	@Getter
	@Setter
	@ToString
	public static class Payload {
		private String name;
	}

	public UpdateCustomerNameEvent(Long aggregateId, String eventType, Payload payload) {
		super(aggregateId, eventType);
		this.payload = payload;
	}
}
