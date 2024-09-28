package com.zhigalko.producer.service.impl;

import com.zhigalko.producer.command.Command;
import com.zhigalko.producer.command.CreateCustomerCommand;
import com.zhigalko.producer.command.DeleteCustomerCommand;
import com.zhigalko.producer.command.UpdateCustomerAddressCommand;
import com.zhigalko.producer.command.UpdateCustomerNameCommand;
import com.zhigalko.producer.dto.CreateCustomerDto;
import com.zhigalko.producer.dto.patch.UpdateCustomerPatch;
import com.zhigalko.producer.handler.CommandHandlerDispatcher;
import com.zhigalko.producer.service.CustomerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import static com.zhigalko.producer.dto.patch.UpdateCustomerPatch.UPDATE_NAME;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {
	public static final String RECEIVED_COMMAND_LOG_MESSAGE = "Received command: {}";
	private final CommandHandlerDispatcher commandHandlerDispatcher;

	@Override
	public void createCustomer(CreateCustomerDto createCustomerDto) {
		CreateCustomerCommand createCustomerCommand = new CreateCustomerCommand(createCustomerDto.name(), createCustomerDto.address());
		logReceivedCommand(createCustomerCommand);
		commandHandlerDispatcher.dispatch(createCustomerCommand);
	}

	@Override
	public void updateCustomer(Long customerId, UpdateCustomerPatch patch) {
		String value = (String) patch.getValue();
		if (UPDATE_NAME.equals(patch.getPath())) {
			updateCustomerName(customerId, value);
			return;
		}
		updateCustomerAddress(customerId, value);
	}

	@Override
	public void deleteCustomer(Long customerId) {
		DeleteCustomerCommand deleteCustomerCommand = new DeleteCustomerCommand(customerId);
		logReceivedCommand(deleteCustomerCommand);
		commandHandlerDispatcher.dispatch(deleteCustomerCommand);
	}

	private void updateCustomerName(Long customerId, String name) {
		UpdateCustomerNameCommand updateCustomerNameCommand = new UpdateCustomerNameCommand(customerId, name);
		logReceivedCommand(updateCustomerNameCommand);
		commandHandlerDispatcher.dispatch(updateCustomerNameCommand);
	}

	private void updateCustomerAddress(Long customerId, String address) {
		UpdateCustomerAddressCommand updateCustomerAddressCommand = new UpdateCustomerAddressCommand(customerId, address);
		logReceivedCommand(updateCustomerAddressCommand);
		commandHandlerDispatcher.dispatch(updateCustomerAddressCommand);
	}

	private void logReceivedCommand(Command command) {
		log.info(RECEIVED_COMMAND_LOG_MESSAGE, command);
	}
}
