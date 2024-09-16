package com.zhigalko.consumer.mapper;

import com.zhigalko.core.domain.model.Snapshot;
import com.zhigalko.core.event.CreateCustomerEvent;
import java.util.UUID;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", imports = UUID.class, uses = BaseMapper.class)
public interface SnapshotMapper extends BaseMapper {

	@Mapping(expression = "java(UUID.randomUUID().toString())", target = "id")
	@Mapping(source = "event.aggregateId", target = "aggregateId")
	@Mapping(expression = "java(updateVersion())", target = "version")
	@Mapping(expression = "java(toInstant(event.getTimestamp().toString()))", target = "timestamp")
	@Mapping(source = "event.payload.name", target = "payload.name")
	@Mapping(source = "event.payload.address", target = "payload.address")
	Snapshot toSnapshot(CreateCustomerEvent event);

	default long updateVersion() {
		long newVersion = 0L;
		return ++newVersion;
	}
}
