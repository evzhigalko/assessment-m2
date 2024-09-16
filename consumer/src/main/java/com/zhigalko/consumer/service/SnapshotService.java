package com.zhigalko.consumer.service;

import com.zhigalko.core.domain.model.Snapshot;
import com.zhigalko.core.event.CreateCustomerEvent;

public interface SnapshotService {
	void save(Snapshot snapshot);
	Snapshot createSnapshot(CreateCustomerEvent event);
	Snapshot getSnapshotByAggregateId(Long aggregateId);
	void deleteByAggregateId(Long aggregateId);
}
