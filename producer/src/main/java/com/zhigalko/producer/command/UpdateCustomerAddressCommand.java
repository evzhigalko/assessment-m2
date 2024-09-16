package com.zhigalko.producer.command;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UpdateCustomerAddressCommand extends Command {
	private String address;

	public UpdateCustomerAddressCommand(Long aggregateId, String address) {
		super(aggregateId);
		this.address = address;
	}
}
