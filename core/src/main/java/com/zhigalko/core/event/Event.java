package com.zhigalko.core.event;

import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("events")
@Getter
@Setter
@NoArgsConstructor
@ToString
public abstract class Event {

	@Transient
	public static final String SEQUENCE_NAME = "customers_sequence";

	@Id
	private String id;
	private Instant timestamp;
	private String eventType;
	private Long aggregateId;

	protected Event(Long aggregateId, String eventType) {
		this.eventType = eventType;
		this.aggregateId = aggregateId;
	}
}
