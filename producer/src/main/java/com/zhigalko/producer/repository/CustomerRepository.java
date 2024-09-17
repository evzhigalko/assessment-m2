package com.zhigalko.producer.repository;

import com.zhigalko.common.domain.model.Customer;
import com.zhigalko.common.projection.CustomerProjection;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerRepository extends MongoRepository<Customer, Long> {
	Optional<CustomerProjection> findByCustomerId(Long id);
}
