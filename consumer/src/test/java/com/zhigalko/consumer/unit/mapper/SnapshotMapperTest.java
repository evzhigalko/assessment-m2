package com.zhigalko.consumer.unit.mapper;

import com.zhigalko.consumer.mapper.SnapshotMapper;
import com.zhigalko.consumer.mapper.SnapshotMapperImpl;
import com.zhigalko.core.domain.model.Snapshot;
import com.zhigalko.core.event.CreateCustomerEvent;
import org.junit.jupiter.api.Test;
import static com.zhigalko.consumer.util.TestDataUtil.getCreateCustomerEvent;
import static org.assertj.core.api.Assertions.assertThat;

class SnapshotMapperTest {
	private final SnapshotMapper snapshotMapper = new SnapshotMapperImpl();

	@Test
	void toSnapshot_null() {
		Snapshot snapshot = snapshotMapper.toSnapshot(null);
		assertThat(snapshot).isNull();
	}

	@Test
	void toSnapshot() {
		CreateCustomerEvent event = getCreateCustomerEvent();

		Snapshot snapshot = snapshotMapper.toSnapshot(event);
		assertThat(snapshot).isNotNull();
		assertThat(snapshot.getId()).isNotNull();
		assertThat(snapshot.getAggregateId()).isEqualTo(event.getAggregateId());
		assertThat(snapshot.getVersion()).isEqualTo(1L);
		assertThat(snapshot.getTimestamp()).isNotNull();
		assertThat(snapshot.getPayload()).isNotNull();
		assertThat(snapshot.getPayload().getName()).isEqualTo(event.getPayload().getName());
		assertThat(snapshot.getPayload().getAddress()).isEqualTo(event.getPayload().getAddress());
	}

	@Test
	void updateVersion() {
		long version = snapshotMapper.updateVersion();

		assertThat(version).isEqualTo(1L);
	}
}