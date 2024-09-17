package com.zhigalko.consumer.repository;

import com.zhigalko.common.domain.model.Snapshot;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SnapshotRepository extends MongoRepository<Snapshot, String> {
	Optional<Snapshot> findByAggregateId(Long aggregateId);
	void deleteByAggregateId(Long aggregateId);
}
