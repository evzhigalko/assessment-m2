package com.zhigalko.producer.unit.listener;

import com.zhigalko.core.schema.CustomerViewAvroEvent;
import com.zhigalko.producer.listener.KafkaConsumer;
import com.zhigalko.producer.projector.CustomerProjector;
import java.util.UUID;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static com.zhigalko.producer.util.TestDataUtil.getCustomerViewAvroEvent;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class KafkaConsumerTest {

	private KafkaConsumer consumer;

	@Mock
	private CustomerProjector customerProjector;

	@BeforeEach
	void setUp() {
		consumer = new KafkaConsumer(customerProjector);
	}

	@Test
	void listenCustomerViewTopic() {
		CustomerViewAvroEvent event = getCustomerViewAvroEvent();
		ConsumerRecord<String, CustomerViewAvroEvent> kafkaRecord = new ConsumerRecord<>(
				"create-customer-topic",
				2,
				1L,
				UUID.randomUUID().toString(),
				event
		);

		doNothing().when(customerProjector).project(kafkaRecord.value());

		consumer.listenCustomerViewTopic(kafkaRecord);
		ArgumentCaptor<CustomerViewAvroEvent> captor = ArgumentCaptor.forClass(CustomerViewAvroEvent.class);
		verify(customerProjector).project(captor.capture());

		CustomerViewAvroEvent capturedEvent = captor.getValue();

		assertThat(capturedEvent).isEqualTo(event);
	}
}