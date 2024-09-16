package com.zhigalko.consumer.unit.listener;

import com.zhigalko.consumer.listener.KafkaConsumer;
import com.zhigalko.consumer.service.EventService;
import com.zhigalko.core.schema.CreateCustomerAvroEvent;
import com.zhigalko.core.schema.DeleteCustomerAvroEvent;
import com.zhigalko.core.schema.UpdateCustomerAddressAvroEvent;
import com.zhigalko.core.schema.UpdateCustomerNameAvroEvent;
import java.util.UUID;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static com.zhigalko.consumer.util.TestDataUtil.getCreateCustomerAvroEvent;
import static com.zhigalko.consumer.util.TestDataUtil.getDeleteCustomerAvroEvent;
import static com.zhigalko.consumer.util.TestDataUtil.getUpdateCustomerAddressAvroEvent;
import static com.zhigalko.consumer.util.TestDataUtil.getUpdateCustomerNameAvroEvent;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class KafkaConsumerTest {
	private KafkaConsumer consumer;

	@Mock
	private EventService eventService;

	@BeforeEach
	void setUp() {
		consumer = new KafkaConsumer(eventService);
	}

	@Test
	void listenCreateCustomerTopic() {
		CreateCustomerAvroEvent event = getCreateCustomerAvroEvent();
		ConsumerRecord<String, CreateCustomerAvroEvent> kafkaRecord = new ConsumerRecord<>(
				"create-customer-topic",
				2,
				1L,
				UUID.randomUUID().toString(),
				event
		);

		doNothing().when(eventService).createCustomer(kafkaRecord.value());

		consumer.listenCreateCustomerTopic(kafkaRecord);
		ArgumentCaptor<CreateCustomerAvroEvent> captor = ArgumentCaptor.forClass(CreateCustomerAvroEvent.class);
		verify(eventService).createCustomer(captor.capture());

		CreateCustomerAvroEvent capturedEvent = captor.getValue();

		assertThat(capturedEvent).isEqualTo(event);
	}

	@Test
	void listenUpdateCustomerNameTopic() {
		UpdateCustomerNameAvroEvent event = getUpdateCustomerNameAvroEvent();
		ConsumerRecord<String, UpdateCustomerNameAvroEvent> kafkaRecord = new ConsumerRecord<>(
				"update-customer-name-topic",
				2,
				1L,
				UUID.randomUUID().toString(),
				event
		);

		doNothing().when(eventService).updateCustomerName(kafkaRecord.value());

		consumer.listenUpdateCustomerNameTopic(kafkaRecord);
		ArgumentCaptor<UpdateCustomerNameAvroEvent> captor = ArgumentCaptor.forClass(UpdateCustomerNameAvroEvent.class);
		verify(eventService).updateCustomerName(captor.capture());

		UpdateCustomerNameAvroEvent capturedEvent = captor.getValue();

		assertThat(capturedEvent).isEqualTo(event);
	}

	@Test
	void listenUpdateCustomerAddressTopic() {
		UpdateCustomerAddressAvroEvent event = getUpdateCustomerAddressAvroEvent();
		ConsumerRecord<String, UpdateCustomerAddressAvroEvent> kafkaRecord = new ConsumerRecord<>(
				"update-customer-address-topic",
				2,
				1L,
				UUID.randomUUID().toString(),
				event
		);

		doNothing().when(eventService).updateCustomerAddress(kafkaRecord.value());

		consumer.listenUpdateCustomerAddressTopic(kafkaRecord);
		ArgumentCaptor<UpdateCustomerAddressAvroEvent> captor = ArgumentCaptor.forClass(UpdateCustomerAddressAvroEvent.class);
		verify(eventService).updateCustomerAddress(captor.capture());

		UpdateCustomerAddressAvroEvent capturedEvent = captor.getValue();

		assertThat(capturedEvent).isEqualTo(event);
	}

	@Test
	void listenDeleteCustomerTopic() {
		DeleteCustomerAvroEvent event = getDeleteCustomerAvroEvent();
		ConsumerRecord<String, DeleteCustomerAvroEvent> kafkaRecord = new ConsumerRecord<>(
				"delete-customer-topic",
				2,
				1L,
				UUID.randomUUID().toString(),
				event
		);

		doNothing().when(eventService).deleteCustomer(kafkaRecord.value());

		consumer.listenDeleteCustomerTopic(kafkaRecord);
		ArgumentCaptor<DeleteCustomerAvroEvent> captor = ArgumentCaptor.forClass(DeleteCustomerAvroEvent.class);
		verify(eventService).deleteCustomer(captor.capture());

		DeleteCustomerAvroEvent capturedEvent = captor.getValue();

		assertThat(capturedEvent).isEqualTo(event);
	}
}
