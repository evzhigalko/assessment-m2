package com.zhigalko.consumer;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.core.KafkaAdmin;

@SpringBootTest
class ConsumerApplicationTests {

	@MockBean
	private KafkaAdmin kafkaAdmin;

	@Test
	void contextLoads() {
	}

}
