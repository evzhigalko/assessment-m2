package com.zhigalko.consumer.mapper;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.zhigalko.common.event.ErrorEvent;
import com.zhigalko.consumer.util.JsonConverter;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.apache.avro.generic.GenericRecord;
import org.springframework.stereotype.Component;
import static java.util.Objects.nonNull;

@Component
@RequiredArgsConstructor
public class ErrorEventMapper {
	private final Gson gson;

	public ErrorEvent mapToErrorEvent(GenericRecord genericRecord, String topicName, Long offset) {
		JsonObject jsonObject = JsonConverter.toJsonObject(genericRecord);
		ErrorEvent errorEvent = new ErrorEvent();
		errorEvent.setId(jsonObject.get("id").getAsString());
		errorEvent.setEventType(jsonObject.get("eventType").getAsString());
		errorEvent.setTimestamp(Instant.parse(jsonObject.get("timestamp").getAsString()));
		errorEvent.setTopicName(topicName);
		JsonElement aggregateId = jsonObject.get("aggregateId");
		if (nonNull(aggregateId)) {
			errorEvent.setAggregateId(aggregateId.getAsLong());
		}
		errorEvent.setOffset(offset);
		JsonObject payload = new JsonObject();
		JsonElement name = jsonObject.get("name");
		if (nonNull(name)) {
			payload.addProperty("name", name.getAsString());
		}
		JsonElement address = jsonObject.get("address");
		if (nonNull(address)) {
			payload.addProperty("address", address.getAsString());
		}
		errorEvent.setPayload(gson.toJson(payload));
		return errorEvent;
	}
}
