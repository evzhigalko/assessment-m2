package com.zhigalko.consumer.repository;

import com.zhigalko.common.event.ErrorEvent;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ErrorEventRepository extends MongoRepository<ErrorEvent, String> {
}
