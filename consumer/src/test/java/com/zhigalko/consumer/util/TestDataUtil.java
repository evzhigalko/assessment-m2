package com.zhigalko.consumer.util;

import com.zhigalko.core.domain.model.Snapshot;
import com.zhigalko.core.event.CreateCustomerEvent;
import com.zhigalko.core.event.DeleteCustomerEvent;
import com.zhigalko.core.event.UpdateCustomerAddressEvent;
import com.zhigalko.core.event.UpdateCustomerNameEvent;
import com.zhigalko.core.schema.CreateCustomerAvroEvent;
import com.zhigalko.core.schema.DeleteCustomerAvroEvent;
import com.zhigalko.core.schema.UpdateCustomerAddressAvroEvent;
import com.zhigalko.core.schema.UpdateCustomerNameAvroEvent;
import java.time.Instant;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import static com.zhigalko.core.domain.EventType.CREATE_CUSTOMER;
import static com.zhigalko.core.domain.EventType.CREATE_CUSTOMER_VIEW;
import static com.zhigalko.core.domain.EventType.DELETE_CUSTOMER_VIEW;
import static com.zhigalko.core.domain.EventType.UPDATE_CUSTOMER_ADDRESS;
import static com.zhigalko.core.domain.EventType.UPDATE_CUSTOMER_ADDRESS_VIEW;
import static com.zhigalko.core.domain.EventType.UPDATE_CUSTOMER_NAME;
import static com.zhigalko.core.domain.EventType.UPDATE_CUSTOMER_NAME_VIEW;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TestDataUtil {
	public static Snapshot getSnapshot() {
		return Snapshot.builder()
				.id(UUID.randomUUID().toString())
				.aggregateId(1L)
				.timestamp(Instant.now())
				.version(1L)
				.payload(Snapshot.Payload.builder()
						.name("Alex")
						.address("New York")
						.build())
				.build();
	}

	public static CreateCustomerEvent getCreateCustomerEvent() {
		CreateCustomerEvent.Payload payload = new CreateCustomerEvent.Payload();
		payload.setName("Alex");
		payload.setAddress("New York");
		CreateCustomerEvent event = new CreateCustomerEvent(
				1L,
				CREATE_CUSTOMER.getName(),
				payload
		);
		event.setId(UUID.randomUUID().toString());
		event.setTimestamp(Instant.now());
		return event;
	}

	public static CreateCustomerAvroEvent getCreateCustomerAvroEvent() {
		return new CreateCustomerAvroEvent(
				UUID.randomUUID().toString(),
				"Alex",
				"New York",
				Instant.now().toString(),
				CREATE_CUSTOMER_VIEW.getName()
		);
	}

	public static UpdateCustomerNameAvroEvent getUpdateCustomerNameAvroEvent() {
		return new UpdateCustomerNameAvroEvent(
				UUID.randomUUID().toString(),
				"Tom",
				Instant.now().toString(),
				UPDATE_CUSTOMER_NAME.getName(),
				1L
		);
	}

	public static UpdateCustomerNameEvent getUpdateCustomerNameEvent() {
		UpdateCustomerNameEvent.Payload payload = new UpdateCustomerNameEvent.Payload();
		payload.setName("Tom");
		UpdateCustomerNameEvent event = new UpdateCustomerNameEvent(
				1L,
				UPDATE_CUSTOMER_NAME_VIEW.getName(),
				payload
		);
		event.setId(UUID.randomUUID().toString());
		event.setTimestamp(Instant.now());
		return event;
	}

	public static UpdateCustomerAddressAvroEvent getUpdateCustomerAddressAvroEvent() {
		return new UpdateCustomerAddressAvroEvent(
				UUID.randomUUID().toString(),
				"New York",
				Instant.now().toString(),
				UPDATE_CUSTOMER_ADDRESS.getName(),
				1L
		);
	}

	public static UpdateCustomerAddressEvent getUpdateCustomerAddressEvent() {
		UpdateCustomerAddressEvent.Payload payload = new UpdateCustomerAddressEvent.Payload();
		payload.setAddress("London");
		UpdateCustomerAddressEvent event = new UpdateCustomerAddressEvent(
				1L,
				UPDATE_CUSTOMER_ADDRESS_VIEW.getName(),
				payload
		);
		event.setId(UUID.randomUUID().toString());
		event.setTimestamp(Instant.now());
		return event;
	}

	public static DeleteCustomerAvroEvent getDeleteCustomerAvroEvent() {
		return new DeleteCustomerAvroEvent(
				UUID.randomUUID().toString(),
				Instant.now().toString(),
				DELETE_CUSTOMER_VIEW.getName(),
				1L
		);
	}

	public static DeleteCustomerEvent getDeleteCustomerEvent() {
		DeleteCustomerEvent event = new DeleteCustomerEvent(
				1L,
				DELETE_CUSTOMER_VIEW.getName()
		);
		event.setId(UUID.randomUUID().toString());
		event.setTimestamp(Instant.now());
		return event;
	}
 }
