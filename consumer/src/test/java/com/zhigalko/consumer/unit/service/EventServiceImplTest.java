package com.zhigalko.consumer.unit.service;

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
import com.zhigalko.consumer.service.SnapshotService;
import com.zhigalko.consumer.service.impl.EventServiceImpl;
import com.zhigalko.consumer.util.SequenceGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static com.zhigalko.common.domain.EventType.CREATE_CUSTOMER_VIEW;
import static com.zhigalko.common.domain.EventType.DELETE_CUSTOMER_VIEW;
import static com.zhigalko.common.domain.EventType.UPDATE_CUSTOMER_ADDRESS_VIEW;
import static com.zhigalko.common.domain.EventType.UPDATE_CUSTOMER_NAME_VIEW;
import static com.zhigalko.consumer.util.TestDataUtil.getCreateCustomerAvroEvent;
import static com.zhigalko.consumer.util.TestDataUtil.getCreateCustomerEvent;
import static com.zhigalko.consumer.util.TestDataUtil.getDeleteCustomerAvroEvent;
import static com.zhigalko.consumer.util.TestDataUtil.getDeleteCustomerEvent;
import static com.zhigalko.consumer.util.TestDataUtil.getSnapshot;
import static com.zhigalko.consumer.util.TestDataUtil.getUpdateCustomerAddressAvroEvent;
import static com.zhigalko.consumer.util.TestDataUtil.getUpdateCustomerAddressEvent;
import static com.zhigalko.consumer.util.TestDataUtil.getUpdateCustomerNameAvroEvent;
import static com.zhigalko.consumer.util.TestDataUtil.getUpdateCustomerNameEvent;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EventServiceImplTest {

	@Mock
	private KafkaCustomProperties kafkaCustomProperties;

	@Mock
	private EventMapper eventMapper;

	@Mock
	private EventRepository eventRepository;

	@Mock
	private SequenceGenerator sequenceGenerator;

	@Mock
	private KafkaProducer kafkaProducer;

	@Mock
	private SnapshotService snapshotService;

	private EventServiceImpl eventService;

	@BeforeEach
	void setUp() {
		eventService = new EventServiceImpl(eventRepository,
				sequenceGenerator,
				kafkaProducer,
				kafkaCustomProperties,
				snapshotService,
				eventMapper);
	}

	@Test
	void createCustomer() {
		CreateCustomerAvroEvent createCustomerAvroEvent = getCreateCustomerAvroEvent();
		CreateCustomerEvent event = getCreateCustomerEvent();
		Snapshot snapshot = getSnapshot();
		KafkaCustomProperties.Topic topic = getKafkaTopic();

		doReturn(event).when(eventMapper).toCreateCustomerEvent(createCustomerAvroEvent);
		doReturn(2L).when(sequenceGenerator).generateSequence(Event.SEQUENCE_NAME);
		doReturn(event).when(eventRepository).save(event);
		doReturn(snapshot).when(snapshotService).createSnapshot(event);
		doReturn(topic).when(kafkaCustomProperties).getCustomerViewEventTopic();
		doNothing().when(kafkaProducer).sendMessage(any(), anyString());

		eventService.createCustomer(createCustomerAvroEvent);

		verify(eventMapper).toCreateCustomerEvent(createCustomerAvroEvent);
		verify(sequenceGenerator).generateSequence(Event.SEQUENCE_NAME);
		verify(eventRepository).save(event);
		verify(snapshotService).createSnapshot(event);

		ArgumentCaptor<CustomerViewAvroEvent> customerViewAvroEventArgumentCaptor = forClass(CustomerViewAvroEvent.class);
		verify(kafkaProducer).sendMessage(customerViewAvroEventArgumentCaptor.capture(), eq(topic.getName()));
		CustomerViewAvroEvent capturedEventToKafka = customerViewAvroEventArgumentCaptor.getValue();

		assertThat(capturedEventToKafka.getId()).isNotBlank();
		assertThat(capturedEventToKafka.getAggregateId()).isEqualTo(snapshot.getAggregateId());
		assertThat(capturedEventToKafka.getVersion()).isEqualTo(snapshot.getVersion());
		assertThat(capturedEventToKafka.getTimestamp()).isNotBlank();
		assertThat(capturedEventToKafka.getEventType()).isEqualTo(CREATE_CUSTOMER_VIEW.getName());
		assertThat(capturedEventToKafka.getName()).isEqualTo(snapshot.getPayload().getName());
		assertThat(capturedEventToKafka.getAddress()).isEqualTo(snapshot.getPayload().getAddress());
	}

	@Test
	void updateCustomerName() {
		UpdateCustomerNameAvroEvent updateCustomerNameAvroEvent = getUpdateCustomerNameAvroEvent();
		UpdateCustomerNameEvent event = getUpdateCustomerNameEvent();
		Snapshot snapshot = getSnapshot();
		snapshot.getPayload().setName(event.getPayload().getName());
		KafkaCustomProperties.Topic topic = getKafkaTopic();

		doReturn(event).when(eventMapper).toUpdateCustomerNameEvent(updateCustomerNameAvroEvent);
		doReturn(event).when(eventRepository).save(event);
		doReturn(snapshot).when(snapshotService).getSnapshotByAggregateId(event.getAggregateId());
		doNothing().when(snapshotService).save(snapshot);
		doReturn(topic).when(kafkaCustomProperties).getCustomerViewEventTopic();
		doNothing().when(kafkaProducer).sendMessage(any(), anyString());

		eventService.updateCustomerName(updateCustomerNameAvroEvent);

		verify(eventMapper).toUpdateCustomerNameEvent(updateCustomerNameAvroEvent);
		verify(eventRepository).save(event);
		verify(snapshotService).getSnapshotByAggregateId(event.getAggregateId());
		verify(snapshotService).save(snapshot);
		verify(kafkaCustomProperties).getCustomerViewEventTopic();

		ArgumentCaptor<CustomerViewAvroEvent> customerViewAvroEventArgumentCaptor = forClass(CustomerViewAvroEvent.class);
		verify(kafkaProducer).sendMessage(customerViewAvroEventArgumentCaptor.capture(), eq(topic.getName()));
		CustomerViewAvroEvent capturedEventToKafka = customerViewAvroEventArgumentCaptor.getValue();

		assertThat(capturedEventToKafka.getId()).isNotBlank();
		assertThat(capturedEventToKafka.getAggregateId()).isEqualTo(snapshot.getAggregateId());
		assertThat(capturedEventToKafka.getVersion()).isEqualTo(snapshot.getVersion());
		assertThat(capturedEventToKafka.getTimestamp()).isNotBlank();
		assertThat(capturedEventToKafka.getEventType()).isEqualTo(UPDATE_CUSTOMER_NAME_VIEW.getName());
		assertThat(capturedEventToKafka.getName()).isEqualTo(snapshot.getPayload().getName());
		assertThat(capturedEventToKafka.getAddress()).isEqualTo(snapshot.getPayload().getAddress());
	}

	@Test
	void updateCustomerAddress() {
		UpdateCustomerAddressAvroEvent updateCustomerAddressAvroEvent = getUpdateCustomerAddressAvroEvent();
		UpdateCustomerAddressEvent event = getUpdateCustomerAddressEvent();
		Snapshot snapshot = getSnapshot();
		snapshot.getPayload().setAddress(event.getPayload().getAddress());
		KafkaCustomProperties.Topic topic = getKafkaTopic();

		doReturn(event).when(eventMapper).toUpdateCustomerAddressEvent(updateCustomerAddressAvroEvent);
		doReturn(event).when(eventRepository).save(event);
		doReturn(snapshot).when(snapshotService).getSnapshotByAggregateId(event.getAggregateId());
		doNothing().when(snapshotService).save(snapshot);
		doReturn(topic).when(kafkaCustomProperties).getCustomerViewEventTopic();
		doNothing().when(kafkaProducer).sendMessage(any(), anyString());

		eventService.updateCustomerAddress(updateCustomerAddressAvroEvent);

		verify(eventMapper).toUpdateCustomerAddressEvent(updateCustomerAddressAvroEvent);
		verify(eventRepository).save(event);
		verify(snapshotService).getSnapshotByAggregateId(event.getAggregateId());
		verify(snapshotService).save(snapshot);
		verify(kafkaCustomProperties).getCustomerViewEventTopic();

		ArgumentCaptor<CustomerViewAvroEvent> customerViewAvroEventArgumentCaptor = forClass(CustomerViewAvroEvent.class);
		verify(kafkaProducer).sendMessage(customerViewAvroEventArgumentCaptor.capture(), eq(topic.getName()));
		CustomerViewAvroEvent capturedEventToKafka = customerViewAvroEventArgumentCaptor.getValue();

		assertThat(capturedEventToKafka.getId()).isNotBlank();
		assertThat(capturedEventToKafka.getAggregateId()).isEqualTo(snapshot.getAggregateId());
		assertThat(capturedEventToKafka.getVersion()).isEqualTo(snapshot.getVersion());
		assertThat(capturedEventToKafka.getTimestamp()).isNotBlank();
		assertThat(capturedEventToKafka.getEventType()).isEqualTo(UPDATE_CUSTOMER_ADDRESS_VIEW.getName());
		assertThat(capturedEventToKafka.getName()).isEqualTo(snapshot.getPayload().getName());
		assertThat(capturedEventToKafka.getAddress()).isEqualTo(snapshot.getPayload().getAddress());
	}

	@Test
	void deleteCustomer() {
		DeleteCustomerAvroEvent deleteCustomerAvroEvent = getDeleteCustomerAvroEvent();
		DeleteCustomerEvent event = getDeleteCustomerEvent();
		Snapshot snapshot = getSnapshot();
		KafkaCustomProperties.Topic topic = getKafkaTopic();

		doReturn(event).when(eventMapper).toDeleteCustomerEvent(deleteCustomerAvroEvent);
		doReturn(event).when(eventRepository).save(event);
		doNothing().when(snapshotService).deleteByAggregateId(event.getAggregateId());
		doReturn(topic).when(kafkaCustomProperties).getCustomerViewEventTopic();
		doNothing().when(kafkaProducer).sendMessage(any(), anyString());

		eventService.deleteCustomer(deleteCustomerAvroEvent);

		verify(eventMapper).toDeleteCustomerEvent(deleteCustomerAvroEvent);
		verify(eventRepository).save(event);
		verify(snapshotService).deleteByAggregateId(event.getAggregateId());
		verify(kafkaCustomProperties).getCustomerViewEventTopic();

		ArgumentCaptor<CustomerViewAvroEvent> customerViewAvroEventArgumentCaptor = forClass(CustomerViewAvroEvent.class);
		verify(kafkaProducer).sendMessage(customerViewAvroEventArgumentCaptor.capture(), eq(topic.getName()));
		CustomerViewAvroEvent capturedEventToKafka = customerViewAvroEventArgumentCaptor.getValue();

		assertThat(capturedEventToKafka.getId()).isNotBlank();
		assertThat(capturedEventToKafka.getAggregateId()).isEqualTo(snapshot.getAggregateId());
		assertThat(capturedEventToKafka.getTimestamp()).isNotBlank();
		assertThat(capturedEventToKafka.getEventType()).isEqualTo(DELETE_CUSTOMER_VIEW.getName());
		assertThat(capturedEventToKafka.getName()).isBlank();
		assertThat(capturedEventToKafka.getAddress()).isBlank();
	}

	private KafkaCustomProperties.Topic getKafkaTopic() {
		KafkaCustomProperties.Topic topic = new KafkaCustomProperties.Topic();
		topic.setName("customer-view-event");
		topic.setPartitions(2);
		topic.setReplicationFactor((short) 2);
		return topic;
	}
}
