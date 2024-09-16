package com.zhigalko.producer.handler.impl;

import com.zhigalko.core.schema.UpdateCustomerNameAvroEvent;
import com.zhigalko.core.service.KafkaProducer;
import com.zhigalko.core.util.KafkaCustomProperties;
import com.zhigalko.producer.command.UpdateCustomerNameCommand;
import com.zhigalko.producer.handler.CommandHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import static com.zhigalko.core.domain.EventType.UPDATE_CUSTOMER_NAME;
import static com.zhigalko.core.util.Util.getCurrentDateTime;
import static java.util.UUID.randomUUID;

@Component
@RequiredArgsConstructor
public class UpdateCustomerNameCommandHandler implements CommandHandler<UpdateCustomerNameCommand> {
	private final KafkaProducer kafkaProducer;
	private final KafkaCustomProperties kafkaCustomProperties;

	@Override
	public void handle(UpdateCustomerNameCommand command) {
		UpdateCustomerNameAvroEvent event = new UpdateCustomerNameAvroEvent(
				randomUUID().toString(),
				command.getName(),
				getCurrentDateTime(),
				UPDATE_CUSTOMER_NAME.getName(),
				command.getAggregateId());
		kafkaProducer.sendMessage(event, kafkaCustomProperties.getUpdateCustomerNameEventTopic().getName());
	}

	@Override
	public Class<UpdateCustomerNameCommand> getCommandClass() {
		return UpdateCustomerNameCommand.class;
	}
}
