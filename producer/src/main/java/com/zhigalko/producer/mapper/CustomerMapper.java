package com.zhigalko.producer.mapper;

import com.zhigalko.core.domain.model.Customer;
import com.zhigalko.core.schema.CustomerViewAvroEvent;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface CustomerMapper {

	@Mapping(source = "event.aggregateId", target = "customerId")
	@Mapping(source = "event.version", target = "version")
	@Mapping(source = "event.name", target = "name", qualifiedByName = "toString")
	@Mapping(source = "event.address", target = "address", qualifiedByName = "toString")
	@Mapping(source = "event.id", target = "id", ignore = true)
	Customer toCustomer(CustomerViewAvroEvent event);

	@Named("toString")
	default String toString(CharSequence charSequence) {
		return charSequence.toString();
	}
}
