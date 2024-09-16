package com.zhigalko.core.domain.model;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("snapshots")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Snapshot {

	@Id
	private String id;
	private Long aggregateId;
	private long version;
	private Instant timestamp;
	private Payload payload;

	@Getter
	@Setter
	@Builder
	public static class Payload {
		private String name;
		private String address;
	}
}
