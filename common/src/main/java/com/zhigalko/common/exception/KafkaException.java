package com.zhigalko.common.exception;

public class KafkaException extends RuntimeException {
	public KafkaException() {
	}

	public KafkaException(String message) {
		super(message);
	}
}
