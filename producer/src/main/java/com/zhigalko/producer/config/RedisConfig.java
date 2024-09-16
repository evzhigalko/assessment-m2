package com.zhigalko.producer.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@EnableCaching
@ConfigurationProperties("redis")
@Getter
@Setter
public class RedisConfig {
	private Properties properties;

	@Bean
	public LettuceConnectionFactory connectionFactory(RedisProperties redisProperties) {
		return new LettuceConnectionFactory(redisProperties.getHost(), redisProperties.getPort());
	}

	@Bean
	public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory connectionFactory) {
		RedisTemplate<String, String> template = new RedisTemplate<>();
		template.setConnectionFactory(connectionFactory);
		StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();
		template.setKeySerializer(stringRedisSerializer);
		template.setValueSerializer(stringRedisSerializer);
		template.setHashKeySerializer(stringRedisSerializer);
		template.setHashValueSerializer(stringRedisSerializer);
		return template;
	}

	@Getter
	@Setter
	public static class Properties {
		private Long cacheTtl;
		private String cacheName;
	}
}
