package com.zhigalko.consumer.config;

import com.mongodb.MongoCommandException;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.common.errors.SerializationException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.retrytopic.DeadLetterPublishingRecovererFactory;
import org.springframework.kafka.retrytopic.RetryTopicConfigurationSupport;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@EnableKafka
@Configuration
@RequiredArgsConstructor
public class KafkaConsumerConfig extends RetryTopicConfigurationSupport {

	@Override
	protected void manageNonBlockingFatalExceptions(List<Class<? extends Throwable>> nonBlockingFatalExceptions) {
		nonBlockingFatalExceptions.addAll(Arrays.asList(
				SerializationException.class,
				NullPointerException.class,
				MongoCommandException.class
		));
	}

	@Override
	protected Consumer<DeadLetterPublishingRecovererFactory> configureDeadLetterPublishingContainerFactory() {
		return dlprf -> {
			dlprf.setRetainAllRetryHeaderValues(false);
			dlprf.setPartitionResolver(((consumerRecord, s) -> null));
		};
	}

	@Bean
	public TaskScheduler scheduler() {
		return new ThreadPoolTaskScheduler();
	}
}
