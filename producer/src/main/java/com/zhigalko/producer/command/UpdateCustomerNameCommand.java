package com.zhigalko.producer.command;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UpdateCustomerNameCommand extends Command {
	private String name;

	public UpdateCustomerNameCommand(Long aggregateId, String name) {
		super(aggregateId);
		this.name = name;
	}
}
