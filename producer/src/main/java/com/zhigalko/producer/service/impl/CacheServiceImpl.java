package com.zhigalko.producer.service.impl;

import com.zhigalko.producer.config.RedisConfig;
import com.zhigalko.producer.service.CacheService;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import static com.zhigalko.producer.constants.CommonConstant.CACHE_KEY;

@Slf4j
@Service
@RequiredArgsConstructor
public class CacheServiceImpl implements CacheService {
	private final RedisTemplate<String, String> redisTemplate;
	private final RedisConfig redisConfig;

	@Override
	public void save(String key, String value) {
		Long cacheTtl = redisConfig.getProperties().getCacheTtl();
		redisTemplate.opsForValue().set(key, value, Duration.of(cacheTtl, ChronoUnit.SECONDS));
	}

	@Override
	public String get(String key) {
		return redisTemplate.opsForValue().get(key);
	}

	@Override
	public void delete(String key) {
		redisTemplate.opsForValue().getAndDelete(key);
	}
}
