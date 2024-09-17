package com.zhigalko.producer.unit.handler;

import com.zhigalko.common.schema.UpdateCustomerAddressAvroEvent;
import com.zhigalko.common.service.KafkaProducer;
import com.zhigalko.common.util.KafkaCustomProperties;
import com.zhigalko.producer.command.UpdateCustomerAddressCommand;
import com.zhigalko.producer.handler.impl.UpdateCustomerAddressCommandHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static com.zhigalko.common.domain.EventType.UPDATE_CUSTOMER_ADDRESS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UpdateCustomerAddressCommandHandlerTest {

	@Mock
	private KafkaProducer kafkaProducer;

	@Mock
	private KafkaCustomProperties kafkaCustomProperties;

	@Captor
	private ArgumentCaptor<UpdateCustomerAddressAvroEvent> captor;

	private UpdateCustomerAddressCommandHandler commandHandler;

	@BeforeEach
	void setUp() {
		commandHandler = new UpdateCustomerAddressCommandHandler(kafkaProducer, kafkaCustomProperties);
	}

	@Test
	void handle() {
		UpdateCustomerAddressCommand updateCustomerAddressCommand = new UpdateCustomerAddressCommand();
		updateCustomerAddressCommand.setAggregateId(1L);
		updateCustomerAddressCommand.setAddress("Madrid");

		KafkaCustomProperties.Topic topic = new KafkaCustomProperties.Topic();
		topic.setName("test");
		topic.setPartitions(1);
		topic.setReplicationFactor((short) 1);

		doNothing().when(kafkaProducer).sendMessage(any(), any());
		doReturn(topic).when(kafkaCustomProperties).getUpdateCustomerAddressEventTopic();

		commandHandler.handle(updateCustomerAddressCommand);
		verify(kafkaProducer).sendMessage(captor.capture(), eq(topic.getName()));
		UpdateCustomerAddressAvroEvent capturedEvent = captor.getValue();

		assertThat(capturedEvent.getAggregateId()).isEqualTo(updateCustomerAddressCommand.getAggregateId());
		assertThat(capturedEvent.getEventType()).isEqualTo(UPDATE_CUSTOMER_ADDRESS.getName());
		assertThat(capturedEvent.getId()).isNotBlank();
		assertThat(capturedEvent.getTimestamp()).isNotBlank();
	}

	@Test
	void getCommandClass() {
		Class<UpdateCustomerAddressCommand> commandClass = commandHandler.getCommandClass();

		assertThat(commandClass).isSameAs(UpdateCustomerAddressCommand.class);
	}
}
