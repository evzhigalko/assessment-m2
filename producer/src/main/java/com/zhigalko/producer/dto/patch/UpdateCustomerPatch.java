package com.zhigalko.producer.dto.patch;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.zhigalko.producer.validation.Enum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class UpdateCustomerPatch {
	public static final String UPDATE_NAME = "/name";
	public static final String UPDATE_ADDRESS = "/address";
	public static final String REPLACE_OP = "replace";

	@Enum(anyOf = REPLACE_OP)
	private String op;

	@Enum(anyOf = {UPDATE_NAME, UPDATE_ADDRESS})
	private String path;
	private Object value;
}
