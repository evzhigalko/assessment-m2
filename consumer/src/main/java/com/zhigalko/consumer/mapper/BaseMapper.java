package com.zhigalko.consumer.mapper;

import java.time.Instant;
import org.mapstruct.Mapper;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface BaseMapper {

	@Named("toInstant")
	default Instant toInstant(CharSequence charSequence) {
		return Instant.parse(charSequence);
	}

	@Named("toString")
	default String toString(CharSequence charSequence) {
		return charSequence.toString();
	}
}
