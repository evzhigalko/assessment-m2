package com.zhigalko.consumer.config;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;

@Configuration
@EnableKafka
@RequiredArgsConstructor
public class KafkaConfig {

	@Bean
	public ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory(KafkaProperties kafkaProperties) {
		ConcurrentKafkaListenerContainerFactory<String, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();
		factory.setConsumerFactory(defaultConsumerFactory(kafkaProperties));
		return factory;
	}

	private ConsumerFactory<String, Object> defaultConsumerFactory(KafkaProperties kafkaProperties) {
		Map<String, Object> consumerProps = kafkaProperties.buildConsumerProperties(null);
		return new DefaultKafkaConsumerFactory<>(consumerProps);
	}
}
