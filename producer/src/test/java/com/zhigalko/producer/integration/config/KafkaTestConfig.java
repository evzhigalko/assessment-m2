package com.zhigalko.producer.integration.config;

import io.confluent.kafka.serializers.KafkaAvroSerializerConfig;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import static com.zhigalko.producer.integration.KafkaIntegrationTest.SCHEMA_REGISTRY;

@TestConfiguration
@RequiredArgsConstructor
public class KafkaTestConfig {
	private final ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory;

	@Primary
	@Bean
	public ConcurrentKafkaListenerContainerFactory<String, Object> containerFactory(KafkaProperties kafkaProperties) {
		kafkaListenerContainerFactory.setConsumerFactory(consumerFactory(kafkaProperties));
		return kafkaListenerContainerFactory;
	}

	@Primary
	@Bean
	public ConsumerFactory<String, Object> consumerFactory(KafkaProperties kafkaProperties) {
		return new DefaultKafkaConsumerFactory<>(consumerConfigs(kafkaProperties));
	}

	@Primary
	@Bean
	public Map<String, Object> consumerConfigs(KafkaProperties kafkaProperties) {
		Map<String, Object> props = kafkaProperties.buildConsumerProperties(null);
		props.put(KafkaAvroSerializerConfig.SCHEMA_REGISTRY_URL_CONFIG, "http://localhost:" + SCHEMA_REGISTRY.getFirstMappedPort());
		return props;
	}

	@Primary
	@Bean
	public KafkaTemplate<String, Object> kafkaTemplate(KafkaProperties kafkaProperties) {
		Map<String, Object> kafkaPropertiesMap = kafkaProperties.buildProducerProperties(null);
		kafkaPropertiesMap.put(KafkaAvroSerializerConfig.SCHEMA_REGISTRY_URL_CONFIG, "http://localhost:" + SCHEMA_REGISTRY.getFirstMappedPort());
		return new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(kafkaPropertiesMap));
	}
}
