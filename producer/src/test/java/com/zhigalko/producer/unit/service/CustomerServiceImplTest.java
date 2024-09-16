package com.zhigalko.producer.unit.service;

import com.zhigalko.producer.command.CreateCustomerCommand;
import com.zhigalko.producer.command.DeleteCustomerCommand;
import com.zhigalko.producer.command.UpdateCustomerAddressCommand;
import com.zhigalko.producer.command.UpdateCustomerNameCommand;
import com.zhigalko.producer.dto.CreateCustomerDto;
import com.zhigalko.producer.dto.patch.UpdateCustomerPatch;
import com.zhigalko.producer.handler.CommandHandlerDispatcher;
import com.zhigalko.producer.service.impl.CustomerServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static com.zhigalko.producer.dto.patch.UpdateCustomerPatch.REPLACE_OP;
import static com.zhigalko.producer.dto.patch.UpdateCustomerPatch.UPDATE_ADDRESS;
import static com.zhigalko.producer.dto.patch.UpdateCustomerPatch.UPDATE_NAME;
import static com.zhigalko.producer.util.TestDataUtil.getCustomer;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CustomerServiceImplTest {

	@Mock
	private CommandHandlerDispatcher commandHandlerDispatcher;

	private CustomerServiceImpl customerService;

	@BeforeEach
	void setUp() {
		customerService = new CustomerServiceImpl(commandHandlerDispatcher);
	}

	@Test
	void createCustomer() {
		CreateCustomerDto createCustomerDto = new CreateCustomerDto("Alex", "London");

		doNothing().when(commandHandlerDispatcher).dispatch(any());

		customerService.createCustomer(createCustomerDto);

		ArgumentCaptor<CreateCustomerCommand> captor = ArgumentCaptor.forClass(CreateCustomerCommand.class);

		verify(commandHandlerDispatcher).dispatch(captor.capture());
		CreateCustomerCommand capturedCommand = captor.getValue();
		assertThat(capturedCommand.getName()).isEqualTo(createCustomerDto.name());
		assertThat(capturedCommand.getAddress()).isEqualTo(createCustomerDto.address());
	}

	@Test
	void updateCustomer_name() {
		Long customerId = getCustomer().getCustomerId();
		UpdateCustomerPatch patch = new UpdateCustomerPatch();
		patch.setOp(REPLACE_OP);
		patch.setPath(UPDATE_NAME);
		patch.setValue("Tom");

		doNothing().when(commandHandlerDispatcher).dispatch(any());

		customerService.updateCustomer(customerId, patch);

		ArgumentCaptor<UpdateCustomerNameCommand> captor = ArgumentCaptor.forClass(UpdateCustomerNameCommand.class);
		verify(commandHandlerDispatcher).dispatch(captor.capture());
		UpdateCustomerNameCommand capturedCommand = captor.getValue();
		assertThat(capturedCommand.getAggregateId()).isEqualTo(customerId);
		assertThat(capturedCommand.getName()).isEqualTo(patch.getValue());
	}

	@Test
	void updateCustomer_address() {
		Long customerId = getCustomer().getCustomerId();
		UpdateCustomerPatch patch = new UpdateCustomerPatch();
		patch.setOp(REPLACE_OP);
		patch.setPath(UPDATE_ADDRESS);
		patch.setValue("Madrid");

		doNothing().when(commandHandlerDispatcher).dispatch(any());

		customerService.updateCustomer(customerId, patch);

		ArgumentCaptor<UpdateCustomerAddressCommand> captor = ArgumentCaptor.forClass(UpdateCustomerAddressCommand.class);
		verify(commandHandlerDispatcher).dispatch(captor.capture());
		UpdateCustomerAddressCommand capturedCommand = captor.getValue();
		assertThat(capturedCommand.getAggregateId()).isEqualTo(customerId);
		assertThat(capturedCommand.getAddress()).isEqualTo(patch.getValue());
	}

	@Test
	void deleteCustomer() {
		Long customerId = getCustomer().getCustomerId();

		doNothing().when(commandHandlerDispatcher).dispatch(any());

		customerService.deleteCustomer(customerId);

		ArgumentCaptor<DeleteCustomerCommand> captor = ArgumentCaptor.forClass(DeleteCustomerCommand.class);
		verify(commandHandlerDispatcher).dispatch(captor.capture());
		DeleteCustomerCommand capturedCommand = captor.getValue();
		assertThat(capturedCommand.getAggregateId()).isEqualTo(customerId);
	}
}
