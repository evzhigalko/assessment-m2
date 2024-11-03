package com.zhigalko.producer.config;

import com.zhigalko.common.util.KafkaCustomProperties;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.TopicPartition;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;

@Configuration
@EnableKafka
@RequiredArgsConstructor
public class KafkaConfig {
	private final KafkaCustomProperties kafkaCustomProperties;

	@Bean
	public List<NewTopic> topics() {
		List<NewTopic> topics = createTopics("");
		List<NewTopic> retryableTopics = createTopics("-retry");
		List<NewTopic> dltTopics = createTopics("-dlt");
		topics.addAll(retryableTopics);
		topics.addAll(dltTopics);
		return topics;
	}

	private List<NewTopic> createTopics(String suffix) {
		return kafkaCustomProperties.getTopics().values().stream()
				.map(topic -> TopicBuilder
						.name(topic.getName() + suffix)
						.partitions(topic.getPartitions())
						.replicas(topic.getReplicationFactor())
						.build())
				.toList();
	}

	@Bean
	public KafkaAdmin kafkaAdmin(@Value("${spring.kafka.bootstrap-servers}") String bootstrapServers) {
		KafkaAdmin kafkaAdmin = new KafkaAdmin(Map.of("bootstrap.servers", bootstrapServers));
		topics().forEach(kafkaAdmin::createOrModifyTopics);
		return kafkaAdmin;
	}

	@Bean
	public DeadLetterPublishingRecoverer deadLetterPublishingRecoverer(KafkaTemplate<String, Object> template) {
		return new DeadLetterPublishingRecoverer(template, (genericRecord, ex) ->
				new TopicPartition(genericRecord.topic() + "-dlt", genericRecord.partition()));
	}
}
