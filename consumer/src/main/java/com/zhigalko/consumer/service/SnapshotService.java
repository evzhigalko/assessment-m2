package com.zhigalko.consumer.service;

import com.zhigalko.common.domain.model.Snapshot;
import com.zhigalko.common.event.CreateCustomerEvent;

public interface SnapshotService {
	void save(Snapshot snapshot);
	Snapshot createSnapshot(CreateCustomerEvent event);
	Snapshot getSnapshotByAggregateId(Long aggregateId);
	void deleteByAggregateId(Long aggregateId);
}
