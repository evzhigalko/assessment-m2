package com.zhigalko.producer.command;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class DeleteCustomerCommand extends Command {

	public DeleteCustomerCommand(Long aggregateId) {
		super(aggregateId);
	}
}
