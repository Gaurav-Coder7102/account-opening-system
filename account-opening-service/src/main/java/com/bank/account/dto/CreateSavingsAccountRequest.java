package com.bank.account.dto;

import com.bank.account.entity.AccountType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO used by account-opening-service to request savings account provisioning
 * from savings-account-service via OpenFeign.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateSavingsAccountRequest {

    private Long customerId;
    private String userId;
    private Long applicationId;
    private String accountType; // maps to SavingsAccountType in savings-account-service
    private BigDecimal initialBalance;
    private String remarks;
}
