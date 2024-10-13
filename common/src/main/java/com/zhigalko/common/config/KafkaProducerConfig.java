package com.zhigalko.common.config;

import java.util.Map;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;

@Configuration
@EnableKafka
public class KafkaProducerConfig {

	@Bean
	public KafkaTemplate<String, Object> kafkaTemplate(KafkaProperties kafkaProperties) {
		Map<String, Object> kafkaPropertiesMap = kafkaProperties.buildProducerProperties(null);
		return new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(kafkaPropertiesMap));
	}
}
