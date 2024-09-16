package com.zhigalko.producer.handler.impl;

import com.zhigalko.core.schema.UpdateCustomerAddressAvroEvent;
import com.zhigalko.core.service.KafkaProducer;
import com.zhigalko.core.util.KafkaCustomProperties;
import com.zhigalko.producer.command.UpdateCustomerAddressCommand;
import com.zhigalko.producer.handler.CommandHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import static com.zhigalko.core.domain.EventType.UPDATE_CUSTOMER_ADDRESS;
import static com.zhigalko.core.util.Util.getCurrentDateTime;
import static java.util.UUID.randomUUID;

@Component
@RequiredArgsConstructor
public class UpdateCustomerAddressCommandHandler implements CommandHandler<UpdateCustomerAddressCommand> {
	private final KafkaProducer kafkaProducer;
	private final KafkaCustomProperties kafkaCustomProperties;

	@Override
	public void handle(UpdateCustomerAddressCommand command) {
		UpdateCustomerAddressAvroEvent event = new UpdateCustomerAddressAvroEvent(
				randomUUID().toString(),
				command.getAddress(),
				getCurrentDateTime(),
				UPDATE_CUSTOMER_ADDRESS.getName(),
				command.getAggregateId());
		kafkaProducer.sendMessage(event, kafkaCustomProperties.getUpdateCustomerAddressEventTopic().getName());
	}

	@Override
	public Class<UpdateCustomerAddressCommand> getCommandClass() {
		return UpdateCustomerAddressCommand.class;
	}
}
