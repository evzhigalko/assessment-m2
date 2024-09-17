package com.zhigalko.consumer.unit.mapper;

import com.zhigalko.consumer.mapper.EventMapper;
import com.zhigalko.consumer.mapper.EventMapperImpl;
import com.zhigalko.common.event.CreateCustomerEvent;
import com.zhigalko.common.event.DeleteCustomerEvent;
import com.zhigalko.common.event.UpdateCustomerAddressEvent;
import com.zhigalko.common.event.UpdateCustomerNameEvent;
import com.zhigalko.common.schema.CreateCustomerAvroEvent;
import com.zhigalko.common.schema.DeleteCustomerAvroEvent;
import com.zhigalko.common.schema.UpdateCustomerAddressAvroEvent;
import com.zhigalko.common.schema.UpdateCustomerNameAvroEvent;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import static com.zhigalko.consumer.util.TestDataUtil.getCreateCustomerAvroEvent;
import static com.zhigalko.consumer.util.TestDataUtil.getDeleteCustomerAvroEvent;
import static com.zhigalko.consumer.util.TestDataUtil.getUpdateCustomerAddressAvroEvent;
import static com.zhigalko.consumer.util.TestDataUtil.getUpdateCustomerNameAvroEvent;
import static org.assertj.core.api.Assertions.assertThat;

class EventMapperTest {
	private final EventMapper eventMapper = new EventMapperImpl();

	@Test
	void toCreateCustomerEvent_null() {
		CreateCustomerEvent event = eventMapper.toCreateCustomerEvent(null);

		assertThat(event).isNull();
	}

	@Test
	void toCreateCustomerEvent() {
		CreateCustomerAvroEvent createCustomerAvroEvent = getCreateCustomerAvroEvent();

		CreateCustomerEvent event = eventMapper.toCreateCustomerEvent(createCustomerAvroEvent);

		assertThat(event.getId()).isEqualTo(createCustomerAvroEvent.getId());
		assertThat(event.getTimestamp()).isEqualTo(Instant.parse(createCustomerAvroEvent.getTimestamp()));
		assertThat(event.getEventType()).isEqualTo(createCustomerAvroEvent.getEventType());
		assertThat(event.getPayload().getName()).isEqualTo(createCustomerAvroEvent.getName());
		assertThat(event.getPayload().getAddress()).isEqualTo(createCustomerAvroEvent.getAddress());
	}

	@Test
	void toUpdateCustomerNameEvent_null() {
		UpdateCustomerNameEvent event = eventMapper.toUpdateCustomerNameEvent(null);

		assertThat(event).isNull();
	}

	@Test
	void toUpdateCustomerNameEvent() {
		UpdateCustomerNameAvroEvent updateCustomerNameAvroEvent = getUpdateCustomerNameAvroEvent();

		UpdateCustomerNameEvent event = eventMapper.toUpdateCustomerNameEvent(updateCustomerNameAvroEvent);

		assertThat(event.getId()).isEqualTo(updateCustomerNameAvroEvent.getId());
		assertThat(event.getTimestamp()).isEqualTo(Instant.parse(updateCustomerNameAvroEvent.getTimestamp()));
		assertThat(event.getEventType()).isEqualTo(updateCustomerNameAvroEvent.getEventType());
		assertThat(event.getPayload().getName()).isEqualTo(updateCustomerNameAvroEvent.getName());
		assertThat(event.getAggregateId()).isEqualTo(updateCustomerNameAvroEvent.getAggregateId());
	}

	@Test
	void toUpdateCustomerAddressEvent_null() {
		UpdateCustomerAddressEvent event = eventMapper.toUpdateCustomerAddressEvent(null);

		assertThat(event).isNull();
	}

	@Test
	void toUpdateCustomerAddressEvent() {
		UpdateCustomerAddressAvroEvent updateCustomerAddressAvroEvent = getUpdateCustomerAddressAvroEvent();

		UpdateCustomerAddressEvent event = eventMapper.toUpdateCustomerAddressEvent(updateCustomerAddressAvroEvent);

		assertThat(event.getId()).isEqualTo(updateCustomerAddressAvroEvent.getId());
		assertThat(event.getTimestamp()).isEqualTo(Instant.parse(updateCustomerAddressAvroEvent.getTimestamp()));
		assertThat(event.getEventType()).isEqualTo(updateCustomerAddressAvroEvent.getEventType());
		assertThat(event.getPayload().getAddress()).isEqualTo(updateCustomerAddressAvroEvent.getAddress());
		assertThat(event.getAggregateId()).isEqualTo(updateCustomerAddressAvroEvent.getAggregateId());
	}

	@Test
	void toDeleteCustomerEvent_null() {
		DeleteCustomerEvent event = eventMapper.toDeleteCustomerEvent(null);

		assertThat(event).isNull();
	}

	@Test
	void toDeleteCustomerEvent() {
		DeleteCustomerAvroEvent deleteCustomerAvroEvent = getDeleteCustomerAvroEvent();

		DeleteCustomerEvent event = eventMapper.toDeleteCustomerEvent(deleteCustomerAvroEvent);

		assertThat(event.getId()).isEqualTo(deleteCustomerAvroEvent.getId());
		assertThat(event.getTimestamp()).isEqualTo(Instant.parse(deleteCustomerAvroEvent.getTimestamp()));
		assertThat(event.getEventType()).isEqualTo(deleteCustomerAvroEvent.getEventType());
		assertThat(event.getAggregateId()).isEqualTo(deleteCustomerAvroEvent.getAggregateId());
	}
}
