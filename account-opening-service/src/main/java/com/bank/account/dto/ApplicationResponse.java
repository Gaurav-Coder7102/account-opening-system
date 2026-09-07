package com.bank.account.dto;

import com.bank.account.entity.AccountType;
import com.bank.account.entity.ApplicationStatus;
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
public class ApplicationResponse {
    private Long id;
    private String applicationNumber;
    private Long customerId;
    private String userId;
    private AccountType accountType;
    private ApplicationStatus status;
    private BigDecimal initialDeposit;
    private String remarks;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
