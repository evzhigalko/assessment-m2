package com.zhigalko.consumer.util;

import com.zhigalko.common.event.Counter;
import com.zhigalko.common.event.Event;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
class SequenceGeneratorTest {
	private SequenceGenerator sequenceGenerator;

	@Mock
	private MongoOperations mongoOperations;

	@BeforeEach
	void setUp() {
		sequenceGenerator = new SequenceGenerator(mongoOperations);
	}

	@Test
	void generateSequence_null() {
		doReturn(null).when(mongoOperations).findAndModify(any(Query.class), any(Update.class), any(FindAndModifyOptions.class), any(Class.class));

		Long sequence = sequenceGenerator.generateSequence(Event.SEQUENCE_NAME);

		assertThat(sequence).isNotNull()
				.isEqualTo(1L);
	}

	@Test
	void generateSequence_nonNull() {
		Counter counter = new Counter();
		counter.setId(UUID.randomUUID().toString());
		counter.setSequenceValue(12L);

		doReturn(counter).when(mongoOperations).findAndModify(any(Query.class), any(Update.class), any(FindAndModifyOptions.class), any(Class.class));

		Long sequence = sequenceGenerator.generateSequence(Event.SEQUENCE_NAME);

		assertThat(sequence).isNotNull()
				.isEqualTo(counter.getSequenceValue());
	}
}
