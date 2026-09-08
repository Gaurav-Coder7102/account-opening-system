package com.bank.savings.dto;

import com.bank.savings.entity.SavingsAccountType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateSavingsAccountRequest {

    @NotNull(message = "Customer ID is required")
    private Long customerId;

    @NotNull(message = "User ID is required")
    private String userId;

    @NotNull(message = "Application ID is required")
    private Long applicationId;

    @NotNull(message = "Account type is required")
    private SavingsAccountType accountType;

    @NotNull(message = "Initial balance is required")
    @DecimalMin(value = "0.00", message = "Initial balance cannot be negative")
    private BigDecimal initialBalance;

    private String remarks;
}
