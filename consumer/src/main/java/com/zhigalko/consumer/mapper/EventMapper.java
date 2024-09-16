package com.zhigalko.consumer.mapper;

import com.zhigalko.core.event.CreateCustomerEvent;
import com.zhigalko.core.event.DeleteCustomerEvent;
import com.zhigalko.core.event.UpdateCustomerAddressEvent;
import com.zhigalko.core.event.UpdateCustomerNameEvent;
import com.zhigalko.core.schema.CreateCustomerAvroEvent;
import com.zhigalko.core.schema.DeleteCustomerAvroEvent;
import com.zhigalko.core.schema.UpdateCustomerAddressAvroEvent;
import com.zhigalko.core.schema.UpdateCustomerNameAvroEvent;
import java.time.Instant;
import java.time.ZoneId;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

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
