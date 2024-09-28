package com.zhigalko.consumer;

import com.zhigalko.common.service.KafkaProducer;
import com.zhigalko.common.util.KafkaCustomProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication
@ConfigurationPropertiesScan("com.zhigalko.consumer.util")
@Import({KafkaProducer.class,
		KafkaCustomProperties.class})
@EnableMongoRepositories
public class ConsumerApplication {

	public static void main(String[] args) {
		SpringApplication.run(ConsumerApplication.class, args);
	}

}
