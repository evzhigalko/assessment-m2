package com.zhigalko.producer.handler.impl;

import com.zhigalko.common.schema.DeleteCustomerAvroEvent;
import com.zhigalko.common.service.KafkaProducer;
import com.zhigalko.common.util.KafkaCustomProperties;
import com.zhigalko.producer.command.DeleteCustomerCommand;
import com.zhigalko.producer.handler.CommandHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import static com.zhigalko.common.domain.EventType.DELETE_CUSTOMER;
import static com.zhigalko.common.util.Util.getCurrentDateTime;
import static java.util.UUID.randomUUID;

@Component
@RequiredArgsConstructor
public class DeleteCustomerCommandHandler implements CommandHandler<DeleteCustomerCommand> {
	private final KafkaProducer kafkaProducer;
	private final KafkaCustomProperties kafkaCustomProperties;

	@Override
	public void handle(DeleteCustomerCommand command) {
		DeleteCustomerAvroEvent event = new DeleteCustomerAvroEvent(
				randomUUID().toString(),
				getCurrentDateTime(),
				DELETE_CUSTOMER.getName(),
				command.getAggregateId());
		kafkaProducer.sendMessage(event, kafkaCustomProperties.getDeleteCustomerEventTopic().getName());
	}

	@Override
	public Class<DeleteCustomerCommand> getCommandClass() {
		return DeleteCustomerCommand.class;
	}
}
