package com.zhigalko.producer.unit.projector;

import com.zhigalko.core.domain.model.Customer;
import com.zhigalko.core.schema.CustomerViewAvroEvent;
import com.zhigalko.producer.mapper.CustomerMapper;
import com.zhigalko.producer.projector.CustomerProjector;
import com.zhigalko.producer.service.CustomerQueryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static com.zhigalko.core.domain.EventType.DELETE_CUSTOMER_VIEW;
import static com.zhigalko.core.domain.EventType.UPDATE_CUSTOMER_ADDRESS_VIEW;
import static com.zhigalko.core.domain.EventType.UPDATE_CUSTOMER_NAME_VIEW;
import static com.zhigalko.producer.util.TestDataUtil.getCustomer;
import static com.zhigalko.producer.util.TestDataUtil.getCustomerViewAvroEvent;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class CustomerProjectorTest {

	@Mock
	private CustomerQueryService customerQueryService;

	@Mock
	private CustomerMapper customerMapper;

	private CustomerProjector customerProjector;

	@BeforeEach
	void setUp() {
		customerProjector = new CustomerProjector(customerQueryService, customerMapper);
	}

	@Test
	void project_createCustomerProjection() {
		CustomerViewAvroEvent event = getCustomerViewAvroEvent();
		Customer customer = getCustomer();

		doReturn(customer).when(customerMapper).toCustomer(event);
		doNothing().when(customerQueryService).saveCustomerProjection(customer);

		customerProjector.project(event);

		ArgumentCaptor<CustomerViewAvroEvent> eventArgumentCaptor = ArgumentCaptor.forClass(CustomerViewAvroEvent.class);
		verify(customerMapper).toCustomer(eventArgumentCaptor.capture());
		CustomerViewAvroEvent capturedEvent = eventArgumentCaptor.getValue();
		assertThat(capturedEvent).isEqualTo(event);

		ArgumentCaptor<Customer> customerArgumentCaptor = ArgumentCaptor.forClass(Customer.class);
		verify(customerQueryService).saveCustomerProjection(customerArgumentCaptor.capture());
		Customer capturedCustomer = customerArgumentCaptor.getValue();
		assertThat(capturedCustomer).isEqualTo(customer);
	}

	@Test
	void project_updateName() {
		CustomerViewAvroEvent event = getCustomerViewAvroEvent();
		String newName = "Tom";
		event.setName(newName);
		event.setEventType(UPDATE_CUSTOMER_NAME_VIEW.getName());

		Customer customer = getCustomer();
		customer.setName(newName);

		doReturn(customer).when(customerMapper).toCustomer(event);
		doNothing().when(customerQueryService).saveCustomerProjection(customer);

		customerProjector.project(event);

		ArgumentCaptor<CustomerViewAvroEvent> eventArgumentCaptor = ArgumentCaptor.forClass(CustomerViewAvroEvent.class);
		verify(customerMapper).toCustomer(eventArgumentCaptor.capture());
		CustomerViewAvroEvent capturedEvent = eventArgumentCaptor.getValue();
		assertThat(capturedEvent).isEqualTo(event);

		ArgumentCaptor<Customer> customerArgumentCaptor = ArgumentCaptor.forClass(Customer.class);
		verify(customerQueryService).saveCustomerProjection(customerArgumentCaptor.capture());
		Customer capturedCustomer = customerArgumentCaptor.getValue();
		assertThat(capturedCustomer).isEqualTo(customer);
	}

	@Test
	void project_updateAddress() {
		CustomerViewAvroEvent event = getCustomerViewAvroEvent();
		String newAddress = "Madrid";
		event.setAddress(newAddress);
		event.setEventType(UPDATE_CUSTOMER_ADDRESS_VIEW.getName());

		Customer customer = getCustomer();
		customer.setAddress(newAddress);

		doReturn(customer).when(customerMapper).toCustomer(event);
		doNothing().when(customerQueryService).saveCustomerProjection(customer);

		customerProjector.project(event);

		ArgumentCaptor<CustomerViewAvroEvent> eventArgumentCaptor = ArgumentCaptor.forClass(CustomerViewAvroEvent.class);
		verify(customerMapper).toCustomer(eventArgumentCaptor.capture());
		CustomerViewAvroEvent capturedEvent = eventArgumentCaptor.getValue();
		assertThat(capturedEvent).isEqualTo(event);

		ArgumentCaptor<Customer> customerArgumentCaptor = ArgumentCaptor.forClass(Customer.class);
		verify(customerQueryService).saveCustomerProjection(customerArgumentCaptor.capture());
		Customer capturedCustomer = customerArgumentCaptor.getValue();
		assertThat(capturedCustomer).isEqualTo(customer);
	}

	@Test
	void project_delete() {
		CustomerViewAvroEvent event = getCustomerViewAvroEvent();
		event.setAddress(null);
		event.setName(null);
		event.setEventType(DELETE_CUSTOMER_VIEW.getName());

		Customer customer = getCustomer();

		doNothing().when(customerQueryService).deleteCustomerProjection(event.getAggregateId());

		customerProjector.project(event);

		ArgumentCaptor<Long> longArgumentCaptor = ArgumentCaptor.forClass(Long.class);
		verify(customerQueryService).deleteCustomerProjection(longArgumentCaptor.capture());
		Long capturedAggregateId = longArgumentCaptor.getValue();
		assertThat(capturedAggregateId).isEqualTo(customer.getCustomerId());

		verifyNoInteractions(customerMapper);
		verify(customerQueryService, times(0)).saveCustomerProjection(customer);
	}
}
