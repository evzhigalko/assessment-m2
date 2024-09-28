package com.zhigalko.consumer.service.impl;

import com.zhigalko.common.event.ErrorEvent;
import com.zhigalko.consumer.mapper.ErrorEventMapper;
import com.zhigalko.consumer.repository.ErrorEventRepository;
import com.zhigalko.consumer.service.ErrorEventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.generic.GenericRecord;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ErrorEventServiceImpl implements ErrorEventService {
	private final ErrorEventMapper errorEventMapper;
	private final ErrorEventRepository errorEventRepository;

	@Override
	public void saveDltEvent(GenericRecord genericRecord, String topicName, Long offset) {
		ErrorEvent errorEvent = errorEventMapper.mapToErrorEvent(genericRecord, topicName, offset);
		errorEventRepository.save(errorEvent);
		log.info("DLT - SAVED ERROR EVENT: {}:", errorEvent);
	}
}
