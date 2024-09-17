package com.zhigalko.producer.unit.handler;

import com.zhigalko.common.domain.EventType;
import com.zhigalko.common.schema.UpdateCustomerNameAvroEvent;
import com.zhigalko.common.service.KafkaProducer;
import com.zhigalko.common.util.KafkaCustomProperties;
import com.zhigalko.producer.command.UpdateCustomerNameCommand;
import com.zhigalko.producer.handler.impl.UpdateCustomerNameCommandHandler;
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
class UpdateCustomerNameCommandHandlerTest {

	@Mock
	private KafkaProducer kafkaProducer;

	@Mock
	private KafkaCustomProperties kafkaCustomProperties;

	@Captor
	private ArgumentCaptor<UpdateCustomerNameAvroEvent> captor;

	private UpdateCustomerNameCommandHandler commandHandler;

	@BeforeEach
	void setUp() {
		commandHandler = new UpdateCustomerNameCommandHandler(kafkaProducer, kafkaCustomProperties);
	}

	@Test
	void handle() {
		UpdateCustomerNameCommand updateCustomerNameCommand = new UpdateCustomerNameCommand();
		updateCustomerNameCommand.setAggregateId(1L);
		updateCustomerNameCommand.setName("Tom");

		KafkaCustomProperties.Topic topic = new KafkaCustomProperties.Topic();
		topic.setName("test");
		topic.setPartitions(1);
		topic.setReplicationFactor((short) 1);

		doNothing().when(kafkaProducer).sendMessage(any(), any());
		doReturn(topic).when(kafkaCustomProperties).getUpdateCustomerNameEventTopic();

		commandHandler.handle(updateCustomerNameCommand);
		verify(kafkaProducer).sendMessage(captor.capture(), eq(topic.getName()));
		UpdateCustomerNameAvroEvent capturedEvent = captor.getValue();

		assertThat(capturedEvent.getAggregateId()).isEqualTo(updateCustomerNameCommand.getAggregateId());
		assertThat(capturedEvent.getEventType()).isEqualTo(EventType.UPDATE_CUSTOMER_NAME.getName());
		assertThat(capturedEvent.getId()).isNotBlank();
		assertThat(capturedEvent.getTimestamp()).isNotBlank();
	}

	@Test
	void getCommandClass() {
		Class<UpdateCustomerNameCommand> commandClass = commandHandler.getCommandClass();

		assertThat(commandClass).isSameAs(UpdateCustomerNameCommand.class);
	}
}
