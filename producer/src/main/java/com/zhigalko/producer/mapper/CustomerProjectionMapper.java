package com.zhigalko.producer.mapper;

import com.zhigalko.core.domain.model.Customer;
import com.zhigalko.core.projection.CustomerProjection;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CustomerProjectionMapper {
	CustomerProjection toCustomerProjection(Customer customer);
}
