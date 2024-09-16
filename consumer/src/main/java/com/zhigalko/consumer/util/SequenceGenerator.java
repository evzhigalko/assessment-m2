package com.zhigalko.consumer.util;

import com.zhigalko.core.event.Counter;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;
import static org.springframework.data.mongodb.core.FindAndModifyOptions.options;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

@Component
@RequiredArgsConstructor
public class SequenceGenerator {
	private final MongoOperations mongoOperations;

	public long generateSequence(String seqName) {
		Counter counter = mongoOperations.findAndModify(query(where("_id").is(seqName)),
				new Update().inc("sequenceValue",1), options().returnNew(true).upsert(true),
				Counter.class);
		return Objects.nonNull(counter) ? counter.getSequenceValue() : 1L;
	}
}
