package com.zhigalko.producer.service.impl;

import com.zhigalko.core.domain.model.Customer;
import com.zhigalko.core.exception.CustomerNotFoundException;
import com.zhigalko.core.projection.CustomerProjection;
import com.zhigalko.core.query.GetCustomerById;
import com.zhigalko.producer.mapper.CustomerProjectionMapper;
import com.zhigalko.producer.repository.CustomerRepository;
import com.zhigalko.producer.service.CacheService;
import com.zhigalko.producer.service.CustomerQueryService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import static com.zhigalko.core.util.Util.fromJsonToCustomerProjection;
import static com.zhigalko.core.util.Util.toJson;
import static com.zhigalko.producer.constants.CommonConstant.CACHE_KEY;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerQueryServiceImpl implements CustomerQueryService {
	private final CustomerRepository customerRepository;
	private final CacheService cacheService;
	private final CustomerProjectionMapper customerProjectionMapper;

	@Override
	@SneakyThrows
	@Transactional(readOnly = true)
	public CustomerProjection getCustomerProjection(GetCustomerById query) {
		Long customerId = query.id();
		String key = CACHE_KEY.apply(String.valueOf(customerId));
		String cachedValue = cacheService.get(key);
		if (isNotBlank(cachedValue)) {
			log.info("Getting from cache customer by id: {}", customerId);
			return fromJsonToCustomerProjection(cachedValue);
		}
		CustomerProjection customerProjection = customerRepository.findByCustomerId(customerId)
				.orElseThrow(CustomerNotFoundException::new);
		cacheService.save(key, toJson(customerProjection));
		log.info("Saved in cached value: {}", customerProjection);
		return customerProjection;
	}

	@Override
	@Transactional
	public void saveCustomerProjection(Customer customer) {
		customerRepository.save(customer);
		log.info("Customer projection was stored: id - {}, name - {}", customer.getCustomerId(), customer.getName());
		CustomerProjection customerProjection = customerProjectionMapper.toCustomerProjection(customer);
		String key = CACHE_KEY.apply(String.valueOf(customerProjection.id()));
		cacheService.save(key, toJson(customerProjection));
		log.info("Saved in cached value: {}", customerProjection);
	}

	@Override
	@Transactional
	public void deleteCustomerProjection(Long customerId) {
		customerRepository.deleteById(customerId);
		log.info("Customer projection was removed: id - {}", customerId);
		cacheService.delete(CACHE_KEY.apply(String.valueOf(customerId)));
		log.info("Customer projection was removed from cache: id - {}", customerId);
	}
}
