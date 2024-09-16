package com.zhigalko.producer.unit.handler;

import com.zhigalko.core.domain.EventType;
import com.zhigalko.core.schema.DeleteCustomerAvroEvent;
import com.zhigalko.core.service.KafkaProducer;
import com.zhigalko.core.util.KafkaCustomProperties;
import com.zhigalko.producer.command.DeleteCustomerCommand;
import com.zhigalko.producer.handler.impl.DeleteCustomerCommandHandler;
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
class DeleteCustomerCommandHandlerTest {

	@Mock
	private KafkaProducer kafkaProducer;

	@Mock
	private KafkaCustomProperties kafkaCustomProperties;

	@Captor
	private ArgumentCaptor<DeleteCustomerAvroEvent> captor;

	private DeleteCustomerCommandHandler commandHandler;

	@BeforeEach
	void setUp() {
		commandHandler = new DeleteCustomerCommandHandler(kafkaProducer, kafkaCustomProperties);
	}

	@Test
	void handle() {
		DeleteCustomerCommand deleteCustomerCommand = new DeleteCustomerCommand();
		deleteCustomerCommand.setAggregateId(1L);
		KafkaCustomProperties.Topic topic = new KafkaCustomProperties.Topic();
		topic.setName("test");
		topic.setPartitions(1);
		topic.setReplicationFactor((short) 1);

		doNothing().when(kafkaProducer).sendMessage(any(), any());
		doReturn(topic).when(kafkaCustomProperties).getDeleteCustomerEventTopic();

		commandHandler.handle(deleteCustomerCommand);
		verify(kafkaProducer).sendMessage(captor.capture(), eq(topic.getName()));
		DeleteCustomerAvroEvent capturedEvent = captor.getValue();

		assertThat(capturedEvent.getAggregateId()).isEqualTo(deleteCustomerCommand.getAggregateId());
		assertThat(capturedEvent.getEventType()).isEqualTo(EventType.DELETE_CUSTOMER.getName());
		assertThat(capturedEvent.getId()).isNotBlank();
		assertThat(capturedEvent.getTimestamp()).isNotBlank();
	}

	@Test
	void getCommandClass() {
		Class<DeleteCustomerCommand> commandClass = commandHandler.getCommandClass();

		assertThat(commandClass).isSameAs(DeleteCustomerCommand.class);
	}
}