package com.bank.savings;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.bank.savings", "com.bank.common"})
public class SavingsAccountServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(SavingsAccountServiceApplication.class, args);
    }
}
