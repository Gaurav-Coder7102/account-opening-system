package com.bank.account.client;

import com.bank.account.dto.CreateSavingsAccountRequest;
import com.bank.account.dto.SavingsAccountDto;
import com.bank.common.dto.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "savings-account-service", url = "${services.savings-account-service.url:http://localhost:8084}")
public interface SavingsAccountClient {

    @PostMapping("/api/v1/savings-accounts")
    ApiResponse<SavingsAccountDto> provisionAccount(@RequestBody CreateSavingsAccountRequest request);
}
