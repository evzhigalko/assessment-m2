package com.zhigalko.producer.listener;

import com.zhigalko.core.schema.CustomerViewAvroEvent;
import com.zhigalko.core.schema.UpdateCustomerNameAvroEvent;
import com.zhigalko.producer.projector.CustomerProjector;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaConsumer {
	private final CustomerProjector customerProjector;

	@KafkaListener(topics = "${kafka.topics.customer-view-event.name}", containerFactory = "kafkaListenerContainerFactory", groupId = "${spring.kafka.consumer.group-id}")
	public void listenCustomerViewTopic(ConsumerRecord<String, CustomerViewAvroEvent> consumerRecord, Acknowledgment acknowledgment) {
		log.info("Received event: {}", consumerRecord.toString());
		customerProjector.project(consumerRecord.value());
		acknowledgment.acknowledge();
	}
}
