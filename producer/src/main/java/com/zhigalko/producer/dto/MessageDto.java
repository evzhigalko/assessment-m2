package com.zhigalko.producer.dto;

import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MessageDto {
	private UUID traceId = UUID.randomUUID();
	private String message;

	public MessageDto(String message) {
		this.message = message;
	}
}
