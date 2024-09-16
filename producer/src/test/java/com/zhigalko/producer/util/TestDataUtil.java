package com.zhigalko.producer.util;

import com.zhigalko.core.domain.model.Customer;
import com.zhigalko.core.event.DeleteCustomerEvent;
import com.zhigalko.core.event.UpdateCustomerAddressEvent;
import com.zhigalko.core.event.UpdateCustomerNameEvent;
import com.zhigalko.core.projection.CustomerProjection;
import com.zhigalko.core.schema.CustomerViewAvroEvent;
import com.zhigalko.core.schema.DeleteCustomerAvroEvent;
import com.zhigalko.core.schema.UpdateCustomerAddressAvroEvent;
import com.zhigalko.core.schema.UpdateCustomerNameAvroEvent;
import java.time.Instant;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.testcontainers.shaded.org.checkerframework.checker.units.qual.C;
import static com.zhigalko.core.domain.EventType.CREATE_CUSTOMER_VIEW;
import static com.zhigalko.core.domain.EventType.DELETE_CUSTOMER_VIEW;
import static com.zhigalko.core.domain.EventType.UPDATE_CUSTOMER_ADDRESS;
import static com.zhigalko.core.domain.EventType.UPDATE_CUSTOMER_ADDRESS_VIEW;
import static com.zhigalko.core.domain.EventType.UPDATE_CUSTOMER_NAME;
import static com.zhigalko.core.domain.EventType.UPDATE_CUSTOMER_NAME_VIEW;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TestDataUtil {

	public static CustomerViewAvroEvent getCustomerViewAvroEvent() {
		return new CustomerViewAvroEvent(
				UUID.randomUUID().toString(),
				"Alex",
				"New York",
				Instant.now().toString(),
				CREATE_CUSTOMER_VIEW.getName(),
				1L,
				1L
		);
	}

	public static CustomerViewAvroEvent getCustomerViewAvroEvent(String eventType) {
		return new CustomerViewAvroEvent(
				UUID.randomUUID().toString(),
				"Alex",
				"New York",
				Instant.now().toString(),
				eventType,
				1L,
				1L
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

	public static CustomerProjection getProjection() {
		return new CustomerProjection(1L, "Alex", "New York");
	}

	public static Customer getCustomer() {
		return new Customer(1L, "Alex", "New York", 1L);
	}
}
