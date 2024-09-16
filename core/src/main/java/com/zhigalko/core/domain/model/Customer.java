package com.zhigalko.core.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("customers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Customer implements Aggregate {

	@Id
	private Long customerId;
	private String name;
	private String address;
	private Long version;

	@Override
	public Long getId() {
		return customerId;
	}

	@Override
	public void setId(Long id) {
		this.customerId = id;
	}
}
