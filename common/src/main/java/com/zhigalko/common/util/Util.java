package com.zhigalko.common.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhigalko.common.projection.CustomerProjection;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Util {
	public static final ObjectMapper MAPPER = new ObjectMapper();

	public static String getCurrentDateTime() {
		return Instant.now().toString();
	}

	@SneakyThrows
	public static String toJson(Object value) {
		return MAPPER.writeValueAsString(value);
	}

	@SneakyThrows
	public static CustomerProjection fromJsonToCustomerProjection(String value) {
		return MAPPER.readValue(String.valueOf(value), CustomerProjection.class);
	}
}
