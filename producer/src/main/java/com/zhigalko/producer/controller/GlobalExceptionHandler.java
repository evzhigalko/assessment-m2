package com.zhigalko.producer.controller;

import com.zhigalko.core.exception.CustomerNotFoundException;
import com.zhigalko.core.exception.KafkaException;
import com.zhigalko.producer.dto.MessageDto;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

	@ExceptionHandler({KafkaException.class, Exception.class})
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public MessageDto internalError(final Exception exception) {
		log.error("Internal Server Error: ", exception);
		return new MessageDto("Internal Server Error");
	}

	@ExceptionHandler(CustomerNotFoundException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public MessageDto customerNotFound(final CustomerNotFoundException exception) {
		log.error("Customer not found: ", exception);
		return new MessageDto("Customer not found");
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public MessageDto nullParameter(final MethodArgumentNotValidException exception) {
		log.error("Parameter has not been passed: ", exception);
		String message = exception.getBindingResult().getAllErrors().stream()
				.map(DefaultMessageSourceResolvable::getDefaultMessage)
				.filter(Objects::nonNull)
				.collect(Collectors.joining("; "));
		return new MessageDto(message);
	}

	@ExceptionHandler(NoResourceFoundException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public MessageDto resourceNotFound(final NoResourceFoundException exception) {
		log.error("Resource not found: ", exception);
		return new MessageDto("Such resource not found. Kindly check your request details.");
	}
}
