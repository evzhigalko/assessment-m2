package com.zhigalko.producer.service;

public interface CacheService {
	void save(String key, String value);

	String get(String key);

	void delete(String key);
}
