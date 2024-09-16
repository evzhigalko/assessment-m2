package com.zhigalko.producer.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.lang3.StringUtils;

public class EnumValidator implements ConstraintValidator<Enum, String> {
	private List<String> possibleValues;

	@Override
	public void initialize(Enum constraintAnnotation) {
		this.possibleValues = Arrays.asList(constraintAnnotation.anyOf());
	}

	@Override
	public boolean isValid(String value, ConstraintValidatorContext context) {
		return StringUtils.isBlank(value) || possibleValues.contains(value);
	}
}
