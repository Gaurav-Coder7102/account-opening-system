package com.bank.account.client;

import com.bank.account.dto.CustomerDto;
import com.bank.common.dto.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "customer-service", url = "${services.customer-service.url:http://localhost:8082}")
public interface CustomerClient {

    @GetMapping("/api/v1/customers/{id}")
    ApiResponse<CustomerDto> getCustomerById(@PathVariable("id") Long id);

    @GetMapping("/api/v1/customers/user/{userId}")
    ApiResponse<CustomerDto> getCustomerByUserId(@PathVariable("userId") String userId);
}
