package com.zhigalko.producer.dto;

import jakarta.validation.constraints.NotBlank;
import static com.zhigalko.producer.constants.CommonConstant.ValidationConstant.BLANK_CUSTOMER_ADDRESS;
import static com.zhigalko.producer.constants.CommonConstant.ValidationConstant.BLANK_CUSTOMER_NAME;

public record CreateCustomerDto(@NotBlank(message = BLANK_CUSTOMER_NAME) String name,
                                @NotBlank(message = BLANK_CUSTOMER_ADDRESS) String address) {}
