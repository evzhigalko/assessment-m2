package com.zhigalko.consumer.service.impl;

import com.zhigalko.consumer.mapper.SnapshotMapper;
import com.zhigalko.consumer.repository.SnapshotRepository;
import com.zhigalko.consumer.service.SnapshotService;
import com.zhigalko.core.domain.model.Snapshot;
import com.zhigalko.core.event.CreateCustomerEvent;
import com.zhigalko.core.exception.CustomerNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SnapshotServiceImpl implements SnapshotService {
	private final SnapshotRepository snapshotRepository;
	private final SnapshotMapper snapshotMapper;

	@Override
	public void save(Snapshot snapshot) {
		snapshotRepository.save(snapshot);
	}

	@Override
	public Snapshot createSnapshot(CreateCustomerEvent event) {
		Snapshot snapshot = snapshotMapper.toSnapshot(event);
		save(snapshot);
		return snapshot;
	}

	@Override
	public Snapshot getSnapshotByAggregateId(Long aggregateId) {
		return snapshotRepository.findByAggregateId(aggregateId).orElseThrow(CustomerNotFoundException::new);
	}

	@Override
	public void deleteByAggregateId(Long aggregateId) {
		snapshotRepository.deleteByAggregateId(aggregateId);
	}
}
