package com.zhigalko.core.event;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class DeleteCustomerEvent extends Event {

	public DeleteCustomerEvent(Long aggregateId, String eventType) {
		super(aggregateId, eventType);
	}
}
