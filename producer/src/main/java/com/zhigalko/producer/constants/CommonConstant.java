package com.zhigalko.producer.constants;

import java.util.function.UnaryOperator;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CommonConstant {
	public static final UnaryOperator<String> CACHE_KEY = id -> "customers | " + id;

	@NoArgsConstructor(access = AccessLevel.PRIVATE)
	public static class ValidationConstant {
		public static final String BLANK_CUSTOMER_NAME = "Customer name can not be blank";
		public static final String BLANK_CUSTOMER_ADDRESS = "Customer address can not be blank";
	}
}
