package com.zhigalko.producer.integration.service;

import com.redis.testcontainers.RedisContainer;
import com.zhigalko.common.projection.CustomerProjection;
import com.zhigalko.producer.config.RedisConfig;
import com.zhigalko.producer.integration.BaseIntegrationTest;
import com.zhigalko.producer.service.CacheService;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.utility.DockerImageName;
import static com.zhigalko.common.util.Util.fromJsonToCustomerProjection;
import static com.zhigalko.common.util.Util.toJson;
import static com.zhigalko.producer.constants.CommonConstant.CACHE_KEY;
import static com.zhigalko.producer.util.TestDataUtil.getProjection;
import static org.assertj.core.api.Assertions.assertThat;

public class CacheServiceIT extends BaseIntegrationTest {

	@Container
	public static final RedisContainer REDIS_CONTAINER = new RedisContainer(DockerImageName.parse("redis:latest"));

	@DynamicPropertySource
	private static void registerKafkaAndMongoProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.data.redis.host", REDIS_CONTAINER::getHost);
		registry.add("spring.data.redis.port", REDIS_CONTAINER::getFirstMappedPort);
	}

	@Autowired
	private CacheService cacheService;

	@Autowired
	private RedisTemplate<String, String> redisTemplate;

	@Autowired
	private RedisConfig redisConfig;

	@Test
	void save() {
		CustomerProjection customerProjection = getProjection();
		String key = CACHE_KEY.apply(customerProjection.id().toString());
		String value = toJson(customerProjection);

		cacheService.save(key, value);

		String foundCachedValue = redisTemplate.opsForValue().get(key);
		CustomerProjection projectionFromCache = fromJsonToCustomerProjection(foundCachedValue);

		assertThat(projectionFromCache).isEqualTo(customerProjection);
	}

	@Test
	void get() {
		CustomerProjection customerProjection = getProjection();
		String key = CACHE_KEY.apply(customerProjection.id().toString());
		String value = toJson(customerProjection);
		Long cacheTtl = redisConfig.getProperties().getCacheTtl();
		redisTemplate.opsForValue().set(key, value, Duration.of(cacheTtl, ChronoUnit.SECONDS));

		String foundCachedValue = cacheService.get(key);
		CustomerProjection projectionFromCache = fromJsonToCustomerProjection(foundCachedValue);

		assertThat(projectionFromCache).isEqualTo(customerProjection);
	}

	@Test
	void delete() {
		CustomerProjection customerProjection = getProjection();
		String key = CACHE_KEY.apply(customerProjection.id().toString());
		String value = toJson(customerProjection);
		Long cacheTtl = redisConfig.getProperties().getCacheTtl();
		redisTemplate.opsForValue().set(key, value, Duration.of(cacheTtl, ChronoUnit.SECONDS));

		cacheService.delete(key);

		String foundValue = redisTemplate.opsForValue().get(key);

		assertThat(foundValue).isNull();
	}

	@AfterEach
	void tearDown() {
		Objects.requireNonNull(redisTemplate.getConnectionFactory()).getConnection().serverCommands().flushDb();
	}
}
