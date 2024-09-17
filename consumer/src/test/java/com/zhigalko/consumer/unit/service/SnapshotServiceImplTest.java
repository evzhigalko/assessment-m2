package com.zhigalko.consumer.unit.service;

import com.zhigalko.consumer.mapper.SnapshotMapper;
import com.zhigalko.consumer.repository.SnapshotRepository;
import com.zhigalko.consumer.service.impl.SnapshotServiceImpl;
import com.zhigalko.common.domain.model.Snapshot;
import com.zhigalko.common.event.CreateCustomerEvent;
import com.zhigalko.common.exception.CustomerNotFoundException;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import static com.zhigalko.consumer.util.TestDataUtil.getCreateCustomerEvent;
import static com.zhigalko.consumer.util.TestDataUtil.getSnapshot;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class SnapshotServiceImplTest {

	@Spy
	private SnapshotMapper snapshotMapper;

	@Mock
	private SnapshotRepository snapshotRepository;

	private SnapshotServiceImpl snapshotService;

	@BeforeEach
	void setUp() {
		snapshotService = new SnapshotServiceImpl(snapshotRepository, snapshotMapper);
	}

	@Test
	void save() {
		Snapshot snapshot = getSnapshot();
		doReturn(snapshot).when(snapshotRepository).save(snapshot);

		snapshotService.save(snapshot);

		ArgumentCaptor<Snapshot> argumentCaptor = ArgumentCaptor.forClass(Snapshot.class);

		verify(snapshotRepository).save(argumentCaptor.capture());
		Snapshot captured = argumentCaptor.getValue();

		assertThat(captured).isEqualTo(snapshot);
	}

	@Test
	void save_throwsException() {
		Snapshot snapshot = getSnapshot();
		doThrow(RuntimeException.class).when(snapshotRepository).save(snapshot);

		assertThatExceptionOfType(RuntimeException.class)
				.isThrownBy(() -> snapshotService.save(snapshot));

		ArgumentCaptor<Snapshot> argumentCaptor = ArgumentCaptor.forClass(Snapshot.class);
		verify(snapshotRepository).save(argumentCaptor.capture());
		Snapshot captured = argumentCaptor.getValue();

		assertThat(captured).isEqualTo(snapshot);
	}

	@Test
	void createSnapshot() {
		CreateCustomerEvent event = getCreateCustomerEvent();
		Snapshot snapshot = getSnapshot();

		doReturn(snapshot).when(snapshotMapper).toSnapshot(event);
		doReturn(snapshot).when(snapshotRepository).save(snapshot);

		Snapshot savedSnapshot = snapshotService.createSnapshot(event);

		assertThat(savedSnapshot).isEqualTo(snapshot);

		ArgumentCaptor<Snapshot> snapshotArgumentCaptor = ArgumentCaptor.forClass(Snapshot.class);
		ArgumentCaptor<CreateCustomerEvent> createCustomerEventArgumentCaptor = ArgumentCaptor.forClass(CreateCustomerEvent.class);

		verify(snapshotRepository).save(snapshotArgumentCaptor.capture());
		Snapshot capturedSnapshot = snapshotArgumentCaptor.getValue();
		assertThat(capturedSnapshot).isEqualTo(snapshot);

		verify(snapshotMapper).toSnapshot(createCustomerEventArgumentCaptor.capture());
		CreateCustomerEvent capturedEvent = createCustomerEventArgumentCaptor.getValue();
		assertThat(capturedEvent).isEqualTo(event);
	}


	@Test
	void createSnapshot_mapperThrowsException() {
		CreateCustomerEvent event = getCreateCustomerEvent();

		doThrow(RuntimeException.class).when(snapshotMapper).toSnapshot(event);

		assertThatExceptionOfType(RuntimeException.class)
				.isThrownBy(() -> snapshotService.createSnapshot(event));

		verifyNoInteractions(snapshotRepository);

		ArgumentCaptor<CreateCustomerEvent> createCustomerEventArgumentCaptor = ArgumentCaptor.forClass(CreateCustomerEvent.class);
		verify(snapshotMapper).toSnapshot(createCustomerEventArgumentCaptor.capture());
		CreateCustomerEvent capturedEvent = createCustomerEventArgumentCaptor.getValue();
		assertThat(capturedEvent).isEqualTo(event);
	}

	@Test
	void createSnapshot_repositoryThrowsException() {
		CreateCustomerEvent event = getCreateCustomerEvent();
		Snapshot snapshot = getSnapshot();

		doReturn(snapshot).when(snapshotMapper).toSnapshot(event);
		doReturn(RuntimeException.class).when(snapshotRepository).save(snapshot);

		assertThatExceptionOfType(RuntimeException.class)
				.isThrownBy(() -> snapshotService.createSnapshot(event));

		ArgumentCaptor<Snapshot> snapshotArgumentCaptor = ArgumentCaptor.forClass(Snapshot.class);
		verify(snapshotRepository).save(snapshotArgumentCaptor.capture());
		Snapshot capturedSnapshot = snapshotArgumentCaptor.getValue();
		assertThat(capturedSnapshot).isEqualTo(snapshot);

		ArgumentCaptor<CreateCustomerEvent> createCustomerEventArgumentCaptor = ArgumentCaptor.forClass(CreateCustomerEvent.class);
		verify(snapshotMapper).toSnapshot(createCustomerEventArgumentCaptor.capture());
		CreateCustomerEvent capturedEvent = createCustomerEventArgumentCaptor.getValue();
		assertThat(capturedEvent).isEqualTo(event);
	}

	@Test
	void getSnapshotByAggregateId() {
		Snapshot snapshot = getSnapshot();
		long aggregateId = 1L;

		doReturn(Optional.of(snapshot)).when(snapshotRepository).findByAggregateId(aggregateId);

		Snapshot foundSnapshot = snapshotService.getSnapshotByAggregateId(aggregateId);

		assertThat(foundSnapshot).isEqualTo(snapshot);

		ArgumentCaptor<Long> longArgumentCaptor = ArgumentCaptor.forClass(Long.class);
		verify(snapshotRepository).findByAggregateId(longArgumentCaptor.capture());

		Long capturedAggregateId = longArgumentCaptor.getValue();
		assertThat(capturedAggregateId).isEqualTo(aggregateId);
	}

	@Test
	void getSnapshotByAggregateId_throwsException() {
		long aggregateId = 1L;

		doReturn(Optional.empty()).when(snapshotRepository).findByAggregateId(aggregateId);

		assertThatExceptionOfType(CustomerNotFoundException.class)
				.isThrownBy(() -> snapshotService.getSnapshotByAggregateId(aggregateId));

		ArgumentCaptor<Long> longArgumentCaptor = ArgumentCaptor.forClass(Long.class);
		verify(snapshotRepository).findByAggregateId(longArgumentCaptor.capture());

		Long capturedAggregateId = longArgumentCaptor.getValue();
		assertThat(capturedAggregateId).isEqualTo(aggregateId);
	}

	@Test
	void deleteByAggregateId() {
		long aggregateId = 1L;

		doNothing().when(snapshotRepository).deleteByAggregateId(aggregateId);

		snapshotService.deleteByAggregateId(aggregateId);

		ArgumentCaptor<Long> longArgumentCaptor = ArgumentCaptor.forClass(Long.class);
		verify(snapshotRepository).deleteByAggregateId(longArgumentCaptor.capture());

		Long capturedAggregateId = longArgumentCaptor.getValue();
		assertThat(capturedAggregateId).isEqualTo(aggregateId);
	}

	@Test
	void deleteByAggregateId_throwsException() {
		long aggregateId = 1L;

		doThrow(RuntimeException.class).when(snapshotRepository).deleteByAggregateId(aggregateId);

		assertThatExceptionOfType(RuntimeException.class)
				.isThrownBy(() -> snapshotService.deleteByAggregateId(aggregateId));

		ArgumentCaptor<Long> longArgumentCaptor = ArgumentCaptor.forClass(Long.class);
		verify(snapshotRepository).deleteByAggregateId(longArgumentCaptor.capture());

		Long capturedAggregateId = longArgumentCaptor.getValue();
		assertThat(capturedAggregateId).isEqualTo(aggregateId);
	}
}
