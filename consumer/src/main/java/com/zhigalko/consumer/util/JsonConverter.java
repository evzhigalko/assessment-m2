package com.zhigalko.consumer.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.ByteArrayOutputStream;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.Encoder;
import org.apache.avro.io.EncoderFactory;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class JsonConverter {

	@SneakyThrows
	public static JsonObject toJsonObject(GenericRecord genericRecord) {
		try(ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
			Encoder jsonEncoder = EncoderFactory.get().jsonEncoder(genericRecord.getSchema(), outputStream);
			DatumWriter<GenericRecord> datumWriter = new GenericDatumWriter<>(genericRecord.getSchema());
			datumWriter.write(genericRecord, jsonEncoder);
			jsonEncoder.flush();
			return JsonParser.parseString(outputStream.toString()).getAsJsonObject();
		}
	}
}
