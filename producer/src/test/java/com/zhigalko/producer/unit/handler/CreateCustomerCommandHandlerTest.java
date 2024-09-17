package com.zhigalko.producer.unit.handler;

import com.zhigalko.common.domain.EventType;
import com.zhigalko.common.schema.CreateCustomerAvroEvent;
import com.zhigalko.common.service.KafkaProducer;
import com.zhigalko.common.util.KafkaCustomProperties;
import com.zhigalko.producer.command.CreateCustomerCommand;
import com.zhigalko.producer.handler.impl.CreateCustomerCommandHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CreateCustomerCommandHandlerTest {

	@Mock
	private KafkaProducer kafkaProducer;

	@Mock
	private KafkaCustomProperties kafkaCustomProperties;

	@Captor
	private ArgumentCaptor<CreateCustomerAvroEvent> captor;

	private CreateCustomerCommandHandler commandHandler;

	@BeforeEach
	void setUp() {
		commandHandler = new CreateCustomerCommandHandler(kafkaProducer, kafkaCustomProperties);
	}

	@Test
	void handle() {
		CreateCustomerCommand createCustomerCommand = new CreateCustomerCommand();
		createCustomerCommand.setName("Alex");
		createCustomerCommand.setAddress("London");
		KafkaCustomProperties.Topic topic = new KafkaCustomProperties.Topic();
		topic.setName("test");
		topic.setPartitions(1);
		topic.setReplicationFactor((short) 1);

		doNothing().when(kafkaProducer).sendMessage(any(), any());
		doReturn(topic).when(kafkaCustomProperties).getCreateCustomerEventTopic();

		commandHandler.handle(createCustomerCommand);
		verify(kafkaProducer).sendMessage(captor.capture(), eq(topic.getName()));
		CreateCustomerAvroEvent capturedEvent = captor.getValue();
		assertThat(capturedEvent.getName()).isEqualTo(createCustomerCommand.getName());
		assertThat(capturedEvent.getAddress()).isEqualTo(createCustomerCommand.getAddress());
		assertThat(capturedEvent.getEventType()).isEqualTo(EventType.CREATE_CUSTOMER.getName());
		assertThat(capturedEvent.getId()).isNotBlank();
		assertThat(capturedEvent.getTimestamp()).isNotBlank();
	}

	@Test
	void getCommandClass() {
		Class<CreateCustomerCommand> commandClass = commandHandler.getCommandClass();

		assertThat(commandClass).isSameAs(CreateCustomerCommand.class);
	}
}