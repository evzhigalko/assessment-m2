package com.zhigalko.producer.config;

import com.zhigalko.common.util.KafkaCustomProperties;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.TopicPartition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;

@Configuration
@EnableKafka
@RequiredArgsConstructor
public class KafkaConfig {
	private final KafkaCustomProperties kafkaCustomProperties;

	@Bean
	public List<NewTopic> createTopics() {
		Map<String, KafkaCustomProperties.Topic> topics = kafkaCustomProperties.getTopics();
		List<NewTopic> kafkaTopics = new ArrayList<>();
		topics.values()
				.forEach(topic ->
						kafkaTopics.add(
							new NewTopic(
								topic.getName(),
								topic.getPartitions(),
								topic.getReplicationFactor()
							)
						)
				);
		return kafkaTopics;
	}

	@Bean
	public DeadLetterPublishingRecoverer deadLetterPublishingRecoverer(KafkaTemplate<String, Object> template) {
		return new DeadLetterPublishingRecoverer(template, (genericRecord, ex) ->
				new TopicPartition(genericRecord.topic() + "-dlt", genericRecord.partition()));
	}
}
