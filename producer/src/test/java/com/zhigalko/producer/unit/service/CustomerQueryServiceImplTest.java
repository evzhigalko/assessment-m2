package com.zhigalko.producer.unit.service;

import com.zhigalko.common.domain.model.Customer;
import com.zhigalko.common.exception.CustomerNotFoundException;
import com.zhigalko.common.projection.CustomerProjection;
import com.zhigalko.common.query.GetCustomerById;
import com.zhigalko.producer.mapper.CustomerProjectionMapper;
import com.zhigalko.producer.repository.CustomerRepository;
import com.zhigalko.producer.service.CacheService;
import com.zhigalko.producer.service.impl.CustomerQueryServiceImpl;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static com.zhigalko.common.util.Util.toJson;
import static com.zhigalko.producer.constants.CommonConstant.CACHE_KEY;
import static com.zhigalko.producer.util.TestDataUtil.getCustomer;
import static com.zhigalko.producer.util.TestDataUtil.getProjection;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class CustomerQueryServiceImplTest {

	@Mock
	private CustomerRepository customerRepository;

	@Mock
	private CacheService cacheService;

	@Mock
	private CustomerProjectionMapper customerProjectionMapper;

	@Captor
	private ArgumentCaptor<String> stringArgumentCaptor;

	@Captor
	private ArgumentCaptor<Long> longArgumentCaptor;

	@Captor
	private ArgumentCaptor<Customer> customerArgumentCaptor;

	private CustomerQueryServiceImpl customerQueryService;

	@BeforeEach
	void setUp() {
		customerQueryService = new CustomerQueryServiceImpl(customerRepository, cacheService, customerProjectionMapper);
	}

	@Test
	void getCustomerProjection_noValueInCache_customerDoesNotExist() {
		Customer customer = getCustomer();
		Long customerId = customer.getCustomerId();
		GetCustomerById query = new GetCustomerById(customerId);
		String key = CACHE_KEY.apply(String.valueOf(customerId));

		doReturn(null).when(cacheService).get(key);
		doThrow(new CustomerNotFoundException()).when(customerRepository).findByCustomerId(customerId);

		assertThatExceptionOfType(CustomerNotFoundException.class)
				.isThrownBy(
						() -> customerQueryService.getCustomerProjection(query));

		verify(cacheService).get(stringArgumentCaptor.capture());
		String capturedKey = stringArgumentCaptor.getValue();
		assertThat(capturedKey).isEqualTo(key);

		verify(customerRepository).findByCustomerId(longArgumentCaptor.capture());
		Long capturedCustomerId = longArgumentCaptor.getValue();
		assertThat(capturedCustomerId).isEqualTo(customerId);
	}

	@Test
	void getCustomerProjection_noValueInCache() {
		Customer customer = getCustomer();
		Long customerId = customer.getCustomerId();
		GetCustomerById query = new GetCustomerById(customerId);
		CustomerProjection customerProjection = getProjection();
		String key = CACHE_KEY.apply(String.valueOf(customerId));

		doReturn(null).when(cacheService).get(key);
		doReturn(Optional.of(customerProjection)).when(customerRepository).findByCustomerId(customerId);
		doNothing().when(cacheService).save(key, toJson(customerProjection));

		CustomerProjection foundProjection = customerQueryService.getCustomerProjection(query);

		assertThat(foundProjection).isEqualTo(customerProjection);

		verify(cacheService).get(stringArgumentCaptor.capture());
		String capturedKey = stringArgumentCaptor.getValue();
		assertThat(capturedKey).isEqualTo(key);

		verify(customerRepository).findByCustomerId(longArgumentCaptor.capture());
		Long capturedCustomerId = longArgumentCaptor.getValue();
		assertThat(capturedCustomerId).isEqualTo(customerId);

		verify(cacheService).save(stringArgumentCaptor.capture(), stringArgumentCaptor.capture());
		List<String> capturedValues = stringArgumentCaptor.getAllValues();
		String capturedKeyForSave = capturedValues.get(1);
		assertThat(capturedKeyForSave).isEqualTo(key);
		String capturedJsonProjection = capturedValues.get(2);
		assertThat(capturedJsonProjection).isEqualTo(toJson(customerProjection));
	}

	@Test
	void getCustomerProjection() {
		Customer customer = getCustomer();
		Long customerId = customer.getCustomerId();
		GetCustomerById query = new GetCustomerById(customerId);
		CustomerProjection customerProjection = getProjection();
		String key = CACHE_KEY.apply(String.valueOf(customerId));

		doReturn(toJson(customerProjection)).when(cacheService).get(key);

		CustomerProjection foundProjection = customerQueryService.getCustomerProjection(query);

		assertThat(foundProjection).isEqualTo(customerProjection);

		verify(cacheService).get(stringArgumentCaptor.capture());
		String capturedKey = stringArgumentCaptor.getValue();
		assertThat(capturedKey).isEqualTo(key);

		verifyNoInteractions(customerRepository);
		verify(cacheService, times(0)).save(key, toJson(customerProjection));
	}

	@Test
	void saveCustomerProjection() {
		Customer customer = getCustomer();
		CustomerProjection customerProjection = getProjection();
		Long customerId = customer.getCustomerId();
		String key = CACHE_KEY.apply(String.valueOf(customerId));

		doReturn(customer).when(customerRepository).save(customer);
		doReturn(customerProjection).when(customerProjectionMapper).toCustomerProjection(customer);
		doNothing().when(cacheService).save(key, toJson(customerProjection));

		customerQueryService.saveCustomerProjection(customer);

		verify(customerRepository).save(customerArgumentCaptor.capture());
		Customer capturedCustomer = customerArgumentCaptor.getValue();
		assertThat(capturedCustomer).isEqualTo(customer);

		verify(customerProjectionMapper).toCustomerProjection(customerArgumentCaptor.capture());
		Customer capturedCustomerInMapper = customerArgumentCaptor.getValue();
		assertThat(capturedCustomerInMapper).isEqualTo(customer);

		verify(cacheService).save(stringArgumentCaptor.capture(), stringArgumentCaptor.capture());
		List<String> capturedStringValues = stringArgumentCaptor.getAllValues();
		assertThat(capturedStringValues.get(0)).isEqualTo(key);
		assertThat(capturedStringValues.get(1)).isEqualTo(toJson(customerProjection));
	}

	@Test
	void deleteCustomerProjection() {
		Customer customer = getCustomer();
		Long customerId = customer.getCustomerId();
		String key = CACHE_KEY.apply(String.valueOf(customerId));

		doNothing().when(customerRepository).deleteById(customerId);
		doNothing().when(cacheService).delete(key);

		customerQueryService.deleteCustomerProjection(customerId);

		verify(customerRepository).deleteById(longArgumentCaptor.capture());
		Long capturedCustomerId = longArgumentCaptor.getValue();
		assertThat(capturedCustomerId).isEqualTo(customerId);

		verify(cacheService).delete(stringArgumentCaptor.capture());
		String capturedKey = stringArgumentCaptor.getValue();
		assertThat(capturedKey).isEqualTo(key);
	}
}
