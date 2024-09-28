package com.zhigalko.consumer.service;

import org.apache.avro.generic.GenericRecord;

public interface ErrorEventService {
	void saveDltEvent(GenericRecord genericRecord, String topicName, Long offset);
}
