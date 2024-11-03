package com.zhigalko.consumer.service.impl;

import com.zhigalko.common.domain.model.Snapshot;
import com.zhigalko.common.event.CreateCustomerEvent;
import com.zhigalko.common.event.DeleteCustomerEvent;
import com.zhigalko.common.event.Event;
import com.zhigalko.common.event.UpdateCustomerAddressEvent;
import com.zhigalko.common.event.UpdateCustomerNameEvent;
import com.zhigalko.common.schema.CreateCustomerAvroEvent;
import com.zhigalko.common.schema.CustomerViewAvroEvent;
import com.zhigalko.common.schema.DeleteCustomerAvroEvent;
import com.zhigalko.common.schema.UpdateCustomerAddressAvroEvent;
import com.zhigalko.common.schema.UpdateCustomerNameAvroEvent;
import com.zhigalko.common.service.KafkaProducer;
import com.zhigalko.common.util.KafkaCustomProperties;
import com.zhigalko.consumer.mapper.EventMapper;
import com.zhigalko.consumer.repository.EventRepository;
import com.zhigalko.consumer.service.EventService;
import com.zhigalko.consumer.service.SnapshotService;
import com.zhigalko.consumer.util.SequenceGenerator;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import static com.zhigalko.common.domain.EventType.CREATE_CUSTOMER_VIEW;
import static com.zhigalko.common.domain.EventType.DELETE_CUSTOMER_VIEW;
import static com.zhigalko.common.domain.EventType.UPDATE_CUSTOMER_ADDRESS_VIEW;
import static com.zhigalko.common.domain.EventType.UPDATE_CUSTOMER_NAME_VIEW;
import static com.zhigalko.common.util.Util.getCurrentDateTime;
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
		Snapshot snapshot = snapshotService.getSnapshotByAggregateId(event.getAggregateId());
		snapshot.getPayload().setName(event.getPayload().getName());
		snapshot.setTimestamp(Instant.now());
		long version = snapshot.getVersion();
		version++;
		snapshot.setVersion(version);
		saveSnapshot(snapshot);
		sendUpdateCustomerEvent(snapshot, UPDATE_CUSTOMER_NAME_VIEW.getName());
	}

	@Override
	@Transactional
	public void updateCustomerAddress(UpdateCustomerAddressAvroEvent updateCustomerAddressAvroEvent) {
		UpdateCustomerAddressEvent event = eventMapper.toUpdateCustomerAddressEvent(updateCustomerAddressAvroEvent);
		save(event);
		Snapshot snapshot = snapshotService.getSnapshotByAggregateId(event.getAggregateId());
		snapshot.getPayload().setAddress(event.getPayload().getAddress());
		snapshot.setTimestamp(Instant.now());
		long version = snapshot.getVersion();
		version++;
		snapshot.setVersion(version);
		saveSnapshot(snapshot);
		sendUpdateCustomerEvent(snapshot, UPDATE_CUSTOMER_ADDRESS_VIEW.getName());
	}

	@Override
	@Transactional
	public void deleteCustomer(DeleteCustomerAvroEvent deleteCustomerEvent) {
		DeleteCustomerEvent event = eventMapper.toDeleteCustomerEvent(deleteCustomerEvent);
		save(event);
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
		log.info(EVENT_SAVED_LOG_MESSAGE, event.getClass(), event);
	}

	private void saveSnapshot(Snapshot snapshot) {
		snapshotService.save(snapshot);
		log.info(SNAPSHOT_UPDATED_LOG_MESSAGE,
				snapshot.getAggregateId(),
				snapshot.getVersion(),
				snapshot.getPayload().getName());
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
