package com.zhigalko.consumer.service.impl;

import com.zhigalko.consumer.mapper.EventMapper;
import com.zhigalko.consumer.repository.EventRepository;
import com.zhigalko.consumer.service.EventService;
import com.zhigalko.consumer.service.SnapshotService;
import com.zhigalko.consumer.util.SequenceGenerator;
import com.zhigalko.core.domain.model.Snapshot;
import com.zhigalko.core.event.CreateCustomerEvent;
import com.zhigalko.core.event.DeleteCustomerEvent;
import com.zhigalko.core.event.Event;
import com.zhigalko.core.event.UpdateCustomerAddressEvent;
import com.zhigalko.core.event.UpdateCustomerNameEvent;
import com.zhigalko.core.schema.CreateCustomerAvroEvent;
import com.zhigalko.core.schema.CustomerViewAvroEvent;
import com.zhigalko.core.schema.DeleteCustomerAvroEvent;
import com.zhigalko.core.schema.UpdateCustomerAddressAvroEvent;
import com.zhigalko.core.schema.UpdateCustomerNameAvroEvent;
import com.zhigalko.core.service.KafkaProducer;
import com.zhigalko.core.util.KafkaCustomProperties;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import static com.zhigalko.core.domain.EventType.CREATE_CUSTOMER_VIEW;
import static com.zhigalko.core.domain.EventType.DELETE_CUSTOMER_VIEW;
import static com.zhigalko.core.domain.EventType.UPDATE_CUSTOMER_ADDRESS_VIEW;
import static com.zhigalko.core.domain.EventType.UPDATE_CUSTOMER_NAME_VIEW;
import static com.zhigalko.core.util.Util.getCurrentDateTime;
import static java.util.UUID.randomUUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {
	public static final String EVENT_SAVED_LOG_MESSAGE = "Event was saved: class: {}, payload: {}";
	public static final String SNAPSHOT_UPDATED_LOG_MESSAGE = "Snapshot was updated for id: {}, version: {}, changed: {}";
	private final EventRepository eventRepository;
	private final SequenceGenerator sequenceGenerator;
	private final KafkaProducer kafkaProducer;
	private final KafkaCustomProperties kafkaCustomProperties;
	private final SnapshotService snapshotService;
	private final EventMapper eventMapper;

	@Override
	@Transactional
	public void createCustomer(CreateCustomerAvroEvent createCustomerAvroEvent) {
		CreateCustomerEvent event = eventMapper.toCreateCustomerEvent(createCustomerAvroEvent);
		long generatedCustomerId = sequenceGenerator.generateSequence(Event.SEQUENCE_NAME);
		event.setAggregateId(generatedCustomerId);
		save(event);
		log.info(EVENT_SAVED_LOG_MESSAGE, event.getClass(), event);
		Snapshot snapshot = snapshotService.createSnapshot(event);
		log.info("Snapshot was created for id: {}", snapshot.getAggregateId());
		CustomerViewAvroEvent customerViewAvroEvent = new CustomerViewAvroEvent(
				randomUUID().toString(),
				snapshot.getPayload().getName(),
				snapshot.getPayload().getAddress(),
				getCurrentDateTime(),
				CREATE_CUSTOMER_VIEW.getName(),
				snapshot.getAggregateId(),
				snapshot.getVersion()
		);
		kafkaProducer.sendMessage(customerViewAvroEvent,
				kafkaCustomProperties.getCustomerViewEventTopic().getName());
	}

	@Override
	@Transactional
	public void updateCustomerName(UpdateCustomerNameAvroEvent updateCustomerNameAvroEvent) {
		UpdateCustomerNameEvent event = eventMapper.toUpdateCustomerNameEvent(updateCustomerNameAvroEvent);
		save(event);
		log.info(EVENT_SAVED_LOG_MESSAGE, event.getClass(), event);
		Snapshot snapshot = snapshotService.getSnapshotByAggregateId(event.getAggregateId());
		snapshot.getPayload().setName(event.getPayload().getName());
		snapshot.setTimestamp(Instant.now());
		long version = snapshot.getVersion();
		version++;
		snapshot.setVersion(version);
		saveSnapshot(snapshot);
		log.info(SNAPSHOT_UPDATED_LOG_MESSAGE, snapshot.getAggregateId(), version, snapshot.getPayload().getName());
		sendUpdateCustomerEvent(snapshot, UPDATE_CUSTOMER_NAME_VIEW.getName());
	}

	@Override
	@Transactional
	public void updateCustomerAddress(UpdateCustomerAddressAvroEvent updateCustomerAddressAvroEvent) {
		UpdateCustomerAddressEvent event = eventMapper.toUpdateCustomerAddressEvent(updateCustomerAddressAvroEvent);
		save(event);
		log.info(EVENT_SAVED_LOG_MESSAGE, event.getClass(), event);
		Snapshot snapshot = snapshotService.getSnapshotByAggregateId(event.getAggregateId());
		snapshot.getPayload().setAddress(event.getPayload().getAddress());
		snapshot.setTimestamp(Instant.now());
		long version = snapshot.getVersion();
		version++;
		snapshot.setVersion(version);
		saveSnapshot(snapshot);
		log.info(SNAPSHOT_UPDATED_LOG_MESSAGE, snapshot.getAggregateId(), version, snapshot.getPayload().getAddress());
		sendUpdateCustomerEvent(snapshot, UPDATE_CUSTOMER_ADDRESS_VIEW.getName());
	}

	@Override
	@Transactional
	public void deleteCustomer(DeleteCustomerAvroEvent deleteCustomerEvent) {
		DeleteCustomerEvent event = eventMapper.toDeleteCustomerEvent(deleteCustomerEvent);
		save(event);
		log.info(EVENT_SAVED_LOG_MESSAGE, event.getClass(), event);
		snapshotService.deleteByAggregateId(deleteCustomerEvent.getAggregateId());
		log.info("Snapshot was deleted for id: {}", event.getAggregateId());
		CustomerViewAvroEvent customerViewAvroEvent = new CustomerViewAvroEvent(
				randomUUID().toString(),
				"",
				"",
				getCurrentDateTime(),
				DELETE_CUSTOMER_VIEW.getName(),
				event.getAggregateId(),
				0L
		);

		kafkaProducer.sendMessage(customerViewAvroEvent, kafkaCustomProperties.getCustomerViewEventTopic().getName());
	}

	private void save(Event event) {
		eventRepository.save(event);
	}

	private void saveSnapshot(Snapshot snapshot) {
		snapshotService.save(snapshot);
	}

	private void sendUpdateCustomerEvent(Snapshot snapshot, String eventType) {
		CustomerViewAvroEvent event = new CustomerViewAvroEvent(
				randomUUID().toString(),
				snapshot.getPayload().getName(),
				snapshot.getPayload().getAddress(),
				getCurrentDateTime(),
				eventType,
				snapshot.getAggregateId(),
				snapshot.getVersion());

		kafkaProducer.sendMessage(event,
				kafkaCustomProperties.getCustomerViewEventTopic().getName());
	}
}
