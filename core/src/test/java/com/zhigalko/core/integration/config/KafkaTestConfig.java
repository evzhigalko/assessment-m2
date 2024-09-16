package com.zhigalko.core.integration.config;

import io.confluent.kafka.serializers.KafkaAvroSerializerConfig;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import static com.zhigalko.core.integration.KafkaProducerIT.SCHEMA_REGISTRY;

@TestConfiguration
@RequiredArgsConstructor
public class KafkaTestConfig {
	private final ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory;
	private final KafkaTemplate<String, Object> kafkaTemplate;

	@Bean
	public ConcurrentKafkaListenerContainerFactory<String, Object> containerFactory(KafkaProperties kafkaProperties) {
		kafkaListenerContainerFactory.setConsumerFactory(consumerFactory(kafkaProperties));
		return kafkaListenerContainerFactory;
	}

	@Bean
	public ConsumerFactory<String, Object> consumerFactory(KafkaProperties kafkaProperties) {
		return new DefaultKafkaConsumerFactory<>(consumerConfigs(kafkaProperties));
	}

	@Bean
	public Map<String, Object> consumerConfigs(KafkaProperties kafkaProperties) {
		Map<String, Object> props = kafkaProperties.buildConsumerProperties(null);
		props.put(KafkaAvroSerializerConfig.SCHEMA_REGISTRY_URL_CONFIG, "http://" + SCHEMA_REGISTRY.getHost() + ":" + SCHEMA_REGISTRY.getFirstMappedPort());
		return props;
	}

	@Bean
	public KafkaTemplate<String, Object> testKafkaTemplate() {
		ProducerFactory<String, Object> producerFactory = kafkaTemplate.getProducerFactory();
		final Map<String, Object> props = new HashMap<>();
		props.put(KafkaAvroSerializerConfig.SCHEMA_REGISTRY_URL_CONFIG, "http://" + SCHEMA_REGISTRY.getHost() + ":" + SCHEMA_REGISTRY.getFirstMappedPort());
		producerFactory.updateConfigs(props);
		return kafkaTemplate;
	}
}
