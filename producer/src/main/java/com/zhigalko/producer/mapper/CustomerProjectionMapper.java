package com.zhigalko.producer.mapper;

import com.zhigalko.common.domain.model.Customer;
import com.zhigalko.common.projection.CustomerProjection;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CustomerProjectionMapper {
	CustomerProjection toCustomerProjection(Customer customer);
}
