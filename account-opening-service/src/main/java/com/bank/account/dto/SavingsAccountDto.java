package com.bank.account.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO representing a provisioned savings account returned by savings-account-service.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SavingsAccountDto {
    private Long id;
    private String accountNumber;
    private Long customerId;
    private String userId;
    private Long applicationId;
    private String accountType;
    private String status;
    private BigDecimal balance;
    private LocalDateTime createdAt;
}
