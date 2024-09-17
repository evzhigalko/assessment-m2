package com.zhigalko.producer.handler.impl;

import com.zhigalko.common.schema.CreateCustomerAvroEvent;
import com.zhigalko.common.service.KafkaProducer;
import com.zhigalko.common.util.KafkaCustomProperties;
import com.zhigalko.producer.command.CreateCustomerCommand;
import com.zhigalko.producer.handler.CommandHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import static com.zhigalko.common.domain.EventType.CREATE_CUSTOMER;
import static com.zhigalko.common.util.Util.getCurrentDateTime;
import static java.util.UUID.randomUUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class CreateCustomerCommandHandler implements CommandHandler<CreateCustomerCommand> {
	private final KafkaProducer kafkaProducer;
	private final KafkaCustomProperties kafkaCustomProperties;

	@Override
	public void handle(CreateCustomerCommand command) {
		CreateCustomerAvroEvent event = new CreateCustomerAvroEvent(
				randomUUID().toString(),
				command.getName(),
				command.getAddress(),
				getCurrentDateTime(),
				CREATE_CUSTOMER.getName());
		log.info("Avro event created with event type - {}", event.getEventType());
		kafkaProducer.sendMessage(event, kafkaCustomProperties.getCreateCustomerEventTopic().getName());
	}

	@Override
	public Class<CreateCustomerCommand> getCommandClass() {
		return CreateCustomerCommand.class;
	}
}
