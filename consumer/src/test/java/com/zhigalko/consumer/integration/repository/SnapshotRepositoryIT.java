package com.zhigalko.consumer.integration.repository;

import com.zhigalko.common.domain.model.Snapshot;
import com.zhigalko.consumer.repository.SnapshotRepository;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import static com.zhigalko.consumer.util.TestDataUtil.getSnapshot;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
class SnapshotRepositoryIT {

	@Container
	private static final MongoDBContainer MONGO_DB_CONTAINER = new MongoDBContainer(DockerImageName.parse("mongo:latest"));

	@DynamicPropertySource
	private static void registerKafkaAndMongoProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.data.mongodb.uri", MONGO_DB_CONTAINER::getReplicaSetUrl);
	}

	@Autowired
	private SnapshotRepository snapshotRepository;

	@Test
	void containerIsRun() {
		assertThat(MONGO_DB_CONTAINER.isRunning()).isTrue();
	}

	@Test
	void findByAggregateId() {
		Snapshot snapshot = getSnapshot();
		snapshotRepository.save(snapshot);

		Optional<Snapshot> optionalSnapshot = snapshotRepository.findByAggregateId(snapshot.getAggregateId());

		assertThat(optionalSnapshot).isNotEmpty();
		Snapshot foundSnapshot = optionalSnapshot.get();

		assertThat(foundSnapshot.getId()).isEqualTo(snapshot.getId());
		assertThat(foundSnapshot.getAggregateId()).isEqualTo(snapshot.getAggregateId());
		assertThat(foundSnapshot.getTimestamp()).isNotNull();
		assertThat(foundSnapshot.getVersion()).isEqualTo(snapshot.getVersion());
		assertThat(foundSnapshot.getPayload().getName()).isEqualTo(snapshot.getPayload().getName());
		assertThat(foundSnapshot.getPayload().getAddress()).isEqualTo(snapshot.getPayload().getAddress());
	}

	@Test
	void findByAggregateId_notFound() {
		Snapshot snapshot = getSnapshot();

		Optional<Snapshot> optionalSnapshot = snapshotRepository.findByAggregateId(snapshot.getAggregateId());

		assertThat(optionalSnapshot).isEmpty();
	}

	@Test
	void deleteByAggregateId() {
		Snapshot snapshot = getSnapshot();
		snapshotRepository.save(snapshot);

		snapshotRepository.deleteByAggregateId(snapshot.getAggregateId());

		Optional<Snapshot> optionalSnapshot = snapshotRepository.findByAggregateId(snapshot.getAggregateId());

		assertThat(optionalSnapshot).isEmpty();
	}

	@Test
	void deleteByAggregateId_notFound() {
		Snapshot snapshot = getSnapshot();

		snapshotRepository.deleteByAggregateId(snapshot.getAggregateId());

		Optional<Snapshot> optionalSnapshot = snapshotRepository.findByAggregateId(snapshot.getAggregateId());

		assertThat(optionalSnapshot).isEmpty();
	}

	@AfterEach
	void tearDown() {
		snapshotRepository.deleteAll();
	}
}
