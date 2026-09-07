package com.bank.account;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(scanBasePackages = {"com.bank.account", "com.bank.common"})
@EnableFeignClients
public class AccountOpeningServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AccountOpeningServiceApplication.class, args);
    }
}
