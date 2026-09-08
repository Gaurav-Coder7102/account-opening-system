package com.bank.savings.dto;

import com.bank.savings.entity.AccountStatus;
import com.bank.savings.entity.SavingsAccountType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SavingsAccountResponse {
    private Long id;
    private String accountNumber;
    private Long customerId;
    private String userId;
    private Long applicationId;
    private SavingsAccountType accountType;
    private AccountStatus status;
    private BigDecimal balance;
    private String remarks;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
