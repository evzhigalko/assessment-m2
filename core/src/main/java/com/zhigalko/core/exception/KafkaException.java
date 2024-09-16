package com.zhigalko.core.exception;

public class KafkaException extends RuntimeException {
	public KafkaException() {
	}

	public KafkaException(String message) {
		super(message);
	}
}
