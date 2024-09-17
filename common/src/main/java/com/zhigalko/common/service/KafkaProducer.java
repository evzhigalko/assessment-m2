package com.zhigalko.common.service;

import com.zhigalko.common.exception.KafkaException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaProducer {

	private final KafkaTemplate<String, Object> kafkaTemplate;

	public void sendMessage(Object event, String topicName) {
		ProducerRecord<String, Object> producerRecord = new ProducerRecord<>(topicName, event);
		kafkaTemplate.send(producerRecord)
				.thenAccept(result -> log.info("Kafka message successfully sent on topic {} and value {}", topicName, result.getProducerRecord().value().toString()))
				.exceptionally(ex -> {
					log.error("An error occurred while sending kafka message for event with value {}", producerRecord);
					throw new KafkaException("Error during sending event tot kafka for: %s".formatted(producerRecord));
				});
		log.info("Sending kafka message on topic {}", topicName);
	}
}
