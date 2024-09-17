package com.zhigalko.consumer.mapper;

import com.zhigalko.common.event.CreateCustomerEvent;
import com.zhigalko.common.event.DeleteCustomerEvent;
import com.zhigalko.common.event.UpdateCustomerAddressEvent;
import com.zhigalko.common.event.UpdateCustomerNameEvent;
import com.zhigalko.common.schema.CreateCustomerAvroEvent;
import com.zhigalko.common.schema.DeleteCustomerAvroEvent;
import com.zhigalko.common.schema.UpdateCustomerAddressAvroEvent;
import com.zhigalko.common.schema.UpdateCustomerNameAvroEvent;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface EventMapper extends BaseMapper {

	@Mapping(source = "event.id", target = "id", qualifiedByName = "toString")
	@Mapping(source = "event.timestamp", target = "timestamp", qualifiedByName = "toInstant")
	@Mapping(source = "event.eventType", target = "eventType", qualifiedByName = "toString")
	@Mapping(source = "event.name", target = "payload.name", qualifiedByName = "toString")
	@Mapping(source = "event.address", target = "payload.address", qualifiedByName = "toString")
	CreateCustomerEvent toCreateCustomerEvent(CreateCustomerAvroEvent event);

	@Mapping(source = "event.id", target = "id", qualifiedByName = "toString")
	@Mapping(source = "event.timestamp", target = "timestamp", qualifiedByName = "toInstant")
	@Mapping(source = "event.eventType", target = "eventType", qualifiedByName = "toString")
	@Mapping(source = "event.name", target = "payload.name", qualifiedByName = "toString")
	UpdateCustomerNameEvent toUpdateCustomerNameEvent(UpdateCustomerNameAvroEvent event);

	@Mapping(source = "event.id", target = "id", qualifiedByName = "toString")
	@Mapping(source = "event.timestamp", target = "timestamp", qualifiedByName = "toInstant")
	@Mapping(source = "event.eventType", target = "eventType", qualifiedByName = "toString")
	@Mapping(source = "event.address", target = "payload.address", qualifiedByName = "toString")
	UpdateCustomerAddressEvent toUpdateCustomerAddressEvent(UpdateCustomerAddressAvroEvent event);

	@Mapping(source = "event.id", target = "id", qualifiedByName = "toString")
	@Mapping(source = "event.timestamp", target = "timestamp", qualifiedByName = "toInstant")
	@Mapping(source = "event.eventType", target = "eventType", qualifiedByName = "toString")
	@Mapping(source = "event.aggregateId", target = "aggregateId")
	DeleteCustomerEvent toDeleteCustomerEvent(DeleteCustomerAvroEvent event);
}
