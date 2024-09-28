package com.zhigalko.common.event;

import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("error_events")
@Getter
@Setter
@NoArgsConstructor
@ToString
public class ErrorEvent {

	@Id
	private String id;
	private Instant timestamp;
	private String eventType;
	private Long aggregateId;
	private String payload;
	private String topicName;
	private Long offset;
}
