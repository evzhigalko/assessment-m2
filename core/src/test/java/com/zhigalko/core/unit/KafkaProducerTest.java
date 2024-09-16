package com.zhigalko.core.unit;

import com.zhigalko.core.schema.CreateCustomerAvroEvent;
import com.zhigalko.core.service.KafkaProducer;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import static com.zhigalko.core.domain.EventType.CREATE_CUSTOMER_VIEW;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatRuntimeException;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class KafkaProducerTest {

	@Mock
	private KafkaTemplate<String, Object> kafkaTemplate;

	private KafkaProducer kafkaProducer;

	@BeforeEach
	void setUp() {
		kafkaProducer = new KafkaProducer(kafkaTemplate);
	}

	@SuppressWarnings("unchecked")
	@Test
	void sendMessage() {
		CreateCustomerAvroEvent event = getCreateCustomerAvroEvent();
		ProducerRecord<String, Object> producerRecord = new ProducerRecord<>("test-topic", event);
		SendResult<String, Object> result = new SendResult<>(producerRecord, new RecordMetadata(null, 0, 0, 0, 0, 0));
		doReturn(CompletableFuture.completedFuture(result)).when(kafkaTemplate).send(any(ProducerRecord.class));

		kafkaProducer.sendMessage(event, "test-topic");

		ArgumentCaptor<ProducerRecord<String, Object>> captor = ArgumentCaptor.forClass(ProducerRecord.class);

		verify(kafkaTemplate).send(captor.capture());

		ProducerRecord<String, Object> capturedRecord = captor.getValue();
		CreateCustomerAvroEvent capturedEvent = (CreateCustomerAvroEvent) capturedRecord.value();
		assertThat(capturedEvent.getId()).isEqualTo(event.getId());
		assertThat(capturedEvent.getEventType()).isEqualTo(event.getEventType());
		assertThat(capturedEvent.getTimestamp()).isEqualTo(event.getTimestamp());
		assertThat(capturedEvent.getName()).isEqualTo(event.getName());
		assertThat(capturedEvent.getAddress()).isEqualTo(event.getAddress());
	}

	@SuppressWarnings("unchecked")
	@Test
	void sendMessage_throwsException() {
		CreateCustomerAvroEvent event = getCreateCustomerAvroEvent();
		doThrow(RuntimeException.class).when(kafkaTemplate).send(any(ProducerRecord.class));

		assertThatRuntimeException().isThrownBy(() -> kafkaProducer.sendMessage(event, "test-topic"));

		ArgumentCaptor<ProducerRecord<String, Object>> captor = ArgumentCaptor.forClass(ProducerRecord.class);

		verify(kafkaTemplate).send(captor.capture());

		ProducerRecord<String, Object> capturedRecord = captor.getValue();
		CreateCustomerAvroEvent capturedEvent = (CreateCustomerAvroEvent) capturedRecord.value();
		assertThat(capturedEvent.getId()).isEqualTo(event.getId());
		assertThat(capturedEvent.getEventType()).isEqualTo(event.getEventType());
		assertThat(capturedEvent.getTimestamp()).isEqualTo(event.getTimestamp());
		assertThat(capturedEvent.getName()).isEqualTo(event.getName());
		assertThat(capturedEvent.getAddress()).isEqualTo(event.getAddress());
	}

	public CreateCustomerAvroEvent getCreateCustomerAvroEvent() {
		return new CreateCustomerAvroEvent(
				UUID.randomUUID().toString(),
				"Alex",
				"New York",
				Instant.now().toString(),
				CREATE_CUSTOMER_VIEW.getName()
		);
	}
}