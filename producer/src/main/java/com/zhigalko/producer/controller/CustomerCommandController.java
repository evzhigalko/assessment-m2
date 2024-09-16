package com.zhigalko.producer.controller;

import com.zhigalko.producer.dto.CreateCustomerDto;
import com.zhigalko.producer.dto.patch.UpdateCustomerPatch;
import com.zhigalko.producer.service.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/customers")
public class CustomerCommandController {
	private final CustomerService customerService;

	@PostMapping
	public ResponseEntity<Void> createCustomer(@Valid @RequestBody CreateCustomerDto createCustomerDto) {
		customerService.createCustomer(createCustomerDto);
		return new ResponseEntity<>(HttpStatus.CREATED);
	}

	@PatchMapping(value = "/{customerId}", consumes = "application/json-patch+json")
	public ResponseEntity<Void> updateCustomer(@PathVariable Long customerId,
	                                           @Valid @RequestBody UpdateCustomerPatch patch) {
		customerService.updateCustomer(customerId, patch);
		return new ResponseEntity<>(HttpStatus.ACCEPTED);
	}

	@DeleteMapping("/{customerId}")
	public ResponseEntity<Void> deleteCustomer(@PathVariable Long customerId) {
		customerService.deleteCustomer(customerId);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}
}
