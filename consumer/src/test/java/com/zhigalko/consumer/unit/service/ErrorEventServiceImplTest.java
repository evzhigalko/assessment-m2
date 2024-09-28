package com.zhigalko.consumer.unit.service;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.zhigalko.common.event.ErrorEvent;
import com.zhigalko.consumer.mapper.ErrorEventMapper;
import com.zhigalko.consumer.repository.ErrorEventRepository;
import com.zhigalko.consumer.service.impl.ErrorEventServiceImpl;
import java.time.Instant;
import java.util.UUID;
import org.apache.avro.generic.GenericRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static com.zhigalko.common.domain.EventType.CREATE_CUSTOMER_VIEW;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ErrorEventServiceImplTest {

	@Mock
	private ErrorEventMapper errorEventMapper;

	@Mock
	private ErrorEventRepository errorEventRepository;

	@Mock
	private GenericRecord genericRecord;

	@Captor
	private ArgumentCaptor<ErrorEvent> captor;

	private ErrorEventServiceImpl errorEventService;

	@BeforeEach
	void setUp() {
		errorEventService = new ErrorEventServiceImpl(errorEventMapper, errorEventRepository);
	}

	@Test
	void saveDltEvent() {
		ErrorEvent errorEvent = new ErrorEvent();
		errorEvent.setId(UUID.randomUUID().toString());
		errorEvent.setOffset(13L);
		errorEvent.setAggregateId(1L);
		errorEvent.setTopicName("test-topic");
		errorEvent.setTimestamp(Instant.now());
		errorEvent.setEventType(CREATE_CUSTOMER_VIEW.getName());
		JsonObject json = new JsonObject();
		json.addProperty("name", "Alex");
		json.addProperty("address", "London");
		errorEvent.setPayload(new Gson().toJson(json));
		doReturn(errorEvent).when(errorEventRepository).save(any(ErrorEvent.class));
		doReturn(errorEvent).when(errorEventMapper).mapToErrorEvent(any(GenericRecord.class), anyString(), anyLong());

		errorEventService.saveDltEvent(genericRecord, "test-topic", 13L);

		verify(errorEventMapper).mapToErrorEvent(any(GenericRecord.class), anyString(), anyLong());
		verify(errorEventRepository).save(captor.capture());
		ErrorEvent capturedEvent = captor.getValue();

		assertThat(capturedEvent).isEqualTo(errorEvent);
	}
}
