package com.zhigalko.consumer.listener;

import com.zhigalko.common.schema.CreateCustomerAvroEvent;
import com.zhigalko.common.schema.DeleteCustomerAvroEvent;
import com.zhigalko.common.schema.UpdateCustomerAddressAvroEvent;
import com.zhigalko.common.schema.UpdateCustomerNameAvroEvent;
import com.zhigalko.consumer.service.ErrorEventService;
import com.zhigalko.consumer.service.EventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaConsumer {
	public static final String RECEIVED_EVENT_LOG_MESSAGE = "Received event: {}, from topic: {}";
	private final EventService eventService;
	private final ErrorEventService errorEventService;

	@RetryableTopic(attempts = "2",	backoff = @Backoff(delay = 3000))
	@KafkaListener(topics = "${kafka.topics.create-customer-event.name}", containerFactory = "kafkaListenerContainerFactory", groupId = "${spring.kafka.consumer.group-id}")
	public void listenCreateCustomerTopic(ConsumerRecord<String, CreateCustomerAvroEvent> consumerRecord,
	                                      @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
	                                      Acknowledgment acknowledgment)  {
		logReceivedEvent(consumerRecord.toString(), topic);
		eventService.createCustomer(consumerRecord.value());
		acknowledgment.acknowledge();
	}

	@RetryableTopic(attempts = "2",	backoff = @Backoff(delay = 3000))
	@KafkaListener(topics = "${kafka.topics.update-customer-name-event.name}", containerFactory = "kafkaListenerContainerFactory", groupId = "${spring.kafka.consumer.group-id}")
	public void listenUpdateCustomerNameTopic(ConsumerRecord<String, UpdateCustomerNameAvroEvent> consumerRecord,
	                                          @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
	                                          Acknowledgment acknowledgment) {
		logReceivedEvent(consumerRecord.toString(), topic);
		eventService.updateCustomerName(consumerRecord.value());
		acknowledgment.acknowledge();
	}

	@RetryableTopic(attempts = "2",	backoff = @Backoff(delay = 3000))
	@KafkaListener(topics = "${kafka.topics.update-customer-address-event.name}", containerFactory = "kafkaListenerContainerFactory", groupId = "${spring.kafka.consumer.group-id}")
	public void listenUpdateCustomerAddressTopic(ConsumerRecord<String, UpdateCustomerAddressAvroEvent> consumerRecord,
	                                             @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
	                                             Acknowledgment acknowledgment) {
		logReceivedEvent(consumerRecord.toString(), topic);
		eventService.updateCustomerAddress(consumerRecord.value());
		acknowledgment.acknowledge();
	}

	@RetryableTopic(attempts = "2",	backoff = @Backoff(delay = 3000))
	@KafkaListener(topics = "${kafka.topics.delete-customer-event.name}", containerFactory = "kafkaListenerContainerFactory", groupId = "${spring.kafka.consumer.group-id}")
	public void listenDeleteCustomerTopic(ConsumerRecord<String, DeleteCustomerAvroEvent> consumerRecord,
	                                      @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
	                                      Acknowledgment acknowledgment) {
		logReceivedEvent(consumerRecord.toString(), topic);
		eventService.deleteCustomer(consumerRecord.value());
		acknowledgment.acknowledge();
	}

	@DltHandler
	@KafkaListener(topics = "CustomerViewEventTopic-dlt", containerFactory = "kafkaListenerContainerFactory", groupId = "${spring.kafka.consumer.group-id}")
	public void handleDltEvents(ConsumerRecord<String, GenericRecord> consumerRecord,
	                           @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
	                           @Header(KafkaHeaders.OFFSET) long offset,
	                           Acknowledgment acknowledgment) {
		GenericRecord event = consumerRecord.value();
		log.info("DLT: " + RECEIVED_EVENT_LOG_MESSAGE + ", offset: {}", event, topic, offset);
		errorEventService.saveDltEvent(consumerRecord.value(), topic, offset);
		acknowledgment.acknowledge();
	}

	private void logReceivedEvent(String value, String topicName) {
		log.info(RECEIVED_EVENT_LOG_MESSAGE, value, topicName);
	}
}
