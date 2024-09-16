package com.zhigalko.consumer.listener;

import com.zhigalko.consumer.service.EventService;
import com.zhigalko.core.schema.CreateCustomerAvroEvent;
import com.zhigalko.core.schema.DeleteCustomerAvroEvent;
import com.zhigalko.core.schema.UpdateCustomerAddressAvroEvent;
import com.zhigalko.core.schema.UpdateCustomerNameAvroEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaConsumer {
	public static final String RECEIVED_EVENT_LOG_MESSAGE = "Received event: {}";
	private final EventService eventService;

	@KafkaListener(topics = "${kafka.topics.create-customer-event.name}", containerFactory = "kafkaListenerContainerFactory", groupId = "${spring.kafka.consumer.group-id}")
	public void listenCreateCustomerTopic(ConsumerRecord<String, CreateCustomerAvroEvent> consumerRecord) {
		log.info(RECEIVED_EVENT_LOG_MESSAGE, consumerRecord.toString());
		eventService.createCustomer(consumerRecord.value());
	}

	@KafkaListener(topics = "${kafka.topics.update-customer-name-event.name}", containerFactory = "kafkaListenerContainerFactory", groupId = "${spring.kafka.consumer.group-id}")
	public void listenUpdateCustomerNameTopic(ConsumerRecord<String, UpdateCustomerNameAvroEvent> consumerRecord) {
		log.info(RECEIVED_EVENT_LOG_MESSAGE, consumerRecord.toString());
		eventService.updateCustomerName(consumerRecord.value());
	}

	@KafkaListener(topics = "${kafka.topics.update-customer-address-event.name}", containerFactory = "kafkaListenerContainerFactory", groupId = "${spring.kafka.consumer.group-id}")
	public void listenUpdateCustomerAddressTopic(ConsumerRecord<String, UpdateCustomerAddressAvroEvent> consumerRecord) {
		log.info(RECEIVED_EVENT_LOG_MESSAGE, consumerRecord.toString());
		eventService.updateCustomerAddress(consumerRecord.value());
	}

	@KafkaListener(topics = "${kafka.topics.delete-customer-event.name}", containerFactory = "kafkaListenerContainerFactory", groupId = "${spring.kafka.consumer.group-id}")
	public void listenDeleteCustomerTopic(ConsumerRecord<String, DeleteCustomerAvroEvent> consumerRecord) {
		log.info(RECEIVED_EVENT_LOG_MESSAGE, consumerRecord.toString());
		eventService.deleteCustomer(consumerRecord.value());
	}
}
