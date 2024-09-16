package com.zhigalko.producer.controller;

import com.zhigalko.core.projection.CustomerProjection;
import com.zhigalko.core.query.GetCustomerById;
import com.zhigalko.producer.service.CustomerQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/customers")
public class CustomerQueryController {
	private final CustomerQueryService customerQueryService;

	@GetMapping("/{customerId}")
	public ResponseEntity<CustomerProjection> getCustomerById(@PathVariable Long customerId) {
		GetCustomerById query = new GetCustomerById(customerId);
		return ResponseEntity.ok(customerQueryService.getCustomerProjection(query));
	}
}
