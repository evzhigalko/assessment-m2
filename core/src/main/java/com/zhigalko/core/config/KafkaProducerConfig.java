package com.zhigalko.core.config;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;

@Configuration
@EnableKafka
@RequiredArgsConstructor
public class KafkaProducerConfig {

	@Bean
	public KafkaTemplate<String, Object> kafkaTemplate(KafkaProperties kafkaProperties) {
		Map<String, Object> kafkaPropertiesMap = kafkaProperties.buildProducerProperties(null);
		return new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(kafkaPropertiesMap));
	}
}
