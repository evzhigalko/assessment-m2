package com.zhigalko.core.util;

import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties("kafka")
public class KafkaCustomProperties {
	private Map<String, Topic> topics = new HashMap<>(5);

	@Getter
	@Setter
	public static class Topic {
		private String name;
		private int partitions;
		private short replicationFactor;
	}

	public Topic getCreateCustomerEventTopic() {
		return topics.get("create-customer-event");
	}

	public Topic getCustomerViewEventTopic() {
		return topics.get("customer-view-event");
	}

	public Topic getUpdateCustomerNameEventTopic() {
		return topics.get("update-customer-name-event");
	}

	public Topic getUpdateCustomerAddressEventTopic() {
		return topics.get("update-customer-address-event");
	}

	public Topic getDeleteCustomerEventTopic() {
		return topics.get("delete-customer-event");
	}
}