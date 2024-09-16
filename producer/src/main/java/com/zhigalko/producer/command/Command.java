package com.zhigalko.producer.command;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public abstract class Command {
	private Long aggregateId;

	protected Command(Long aggregateId) {
		this.aggregateId = aggregateId;
	}
}
