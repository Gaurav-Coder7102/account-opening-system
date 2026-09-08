package com.bank.savings.service;

import com.bank.savings.dto.*;
import com.bank.savings.entity.AccountStatus;
import com.bank.savings.entity.SavingsAccount;
import com.bank.savings.exception.DuplicateAccountException;
import com.bank.savings.exception.SavingsAccountNotFoundException;
import com.bank.savings.repository.SavingsAccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SavingsAccountService {

    private final SavingsAccountRepository repository;

    /**
     * Provisions a new savings account from an approved account opening application.
     * Account number is generated using a thread-safe PostgreSQL sequence via
     * {@link SavingsAccountRepository#generateNextAccountNumber()}.
     * Prevents duplicate provisioning: if an account for the same applicationId already exists, throws DuplicateAccountException.
     */
    @Transactional
    public SavingsAccountResponse createAccount(CreateSavingsAccountRequest request) {
        log.info("Provisioning savings account for applicationId: {}, customerId: {}", request.getApplicationId(), request.getCustomerId());

        // Guard: Prevent duplicate account provisioning for same application
        if (repository.existsByApplicationId(request.getApplicationId())) {
            throw new DuplicateAccountException(
                    "A savings account has already been provisioned for applicationId: " + request.getApplicationId()
            );
        }

        // Generate unique, thread-safe account number via PostgreSQL sequence
        String accountNumber = repository.generateNextAccountNumber();
        log.info("Generated account number '{}' for applicationId: {}", accountNumber, request.getApplicationId());

        SavingsAccount account = SavingsAccount.builder()
                .accountNumber(accountNumber)
                .customerId(request.getCustomerId())
                .userId(request.getUserId())
                .applicationId(request.getApplicationId())
                .accountType(request.getAccountType())
                .status(AccountStatus.ACTIVE)
                .balance(request.getInitialBalance())
                .remarks(request.getRemarks())
                .build();

        SavingsAccount saved = repository.save(account);
        log.info("Savings account '{}' provisioned successfully (id: {})", accountNumber, saved.getId());
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public SavingsAccountResponse getAccountById(Long id) {
        return mapToResponse(findByIdOrThrow(id));
    }

    @Transactional(readOnly = true)
    public SavingsAccountResponse getAccountByNumber(String accountNumber) {
        SavingsAccount account = repository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new SavingsAccountNotFoundException("Savings account not found with number: " + accountNumber));
        return mapToResponse(account);
    }

    @Transactional(readOnly = true)
    public List<SavingsAccountResponse> getAccountsByCustomerId(Long customerId) {
        return repository.findByCustomerId(customerId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<SavingsAccountResponse> getAccountsByUserId(String userId) {
        return repository.findByUserId(userId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<SavingsAccountResponse> getAllAccounts() {
        return repository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Updates account status (ACTIVE → DORMANT → CLOSED).
     * Only admins should be allowed to invoke this endpoint.
     */
    @Transactional
    public SavingsAccountResponse updateStatus(Long id, UpdateAccountStatusRequest request) {
        SavingsAccount account = findByIdOrThrow(id);
        log.info("Updating status of account '{}' from {} to {}", account.getAccountNumber(), account.getStatus(), request.getTargetStatus());

        account.setStatus(request.getTargetStatus());
        if (request.getRemarks() != null && !request.getRemarks().isBlank()) {
            account.setRemarks(request.getRemarks());
        }

        return mapToResponse(repository.save(account));
    }

    /**
     * Credits or debits the account balance.
     * Validates that balance won't go negative on a debit operation.
     */
    @Transactional
    public SavingsAccountResponse updateBalance(Long id, BalanceUpdateRequest request) {
        SavingsAccount account = findByIdOrThrow(id);

        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new IllegalArgumentException(
                    "Cannot perform balance operation on account with status: " + account.getStatus()
            );
        }

        BigDecimal newBalance;
        if (request.getOperation() == BalanceUpdateRequest.Operation.CREDIT) {
            newBalance = account.getBalance().add(request.getAmount());
            log.info("Crediting {} to account '{}'. New balance: {}", request.getAmount(), account.getAccountNumber(), newBalance);
        } else {
            newBalance = account.getBalance().subtract(request.getAmount());
            if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException(
                        "Insufficient balance. Current balance: " + account.getBalance() + ", Debit amount: " + request.getAmount()
                );
            }
            log.info("Debiting {} from account '{}'. New balance: {}", request.getAmount(), account.getAccountNumber(), newBalance);
        }

        account.setBalance(newBalance);
        if (request.getRemarks() != null && !request.getRemarks().isBlank()) {
            account.setRemarks(request.getRemarks());
        }

        return mapToResponse(repository.save(account));
    }

    private SavingsAccount findByIdOrThrow(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new SavingsAccountNotFoundException("Savings account not found with id: " + id));
    }

    private SavingsAccountResponse mapToResponse(SavingsAccount account) {
        return SavingsAccountResponse.builder()
                .id(account.getId())
                .accountNumber(account.getAccountNumber())
                .customerId(account.getCustomerId())
                .userId(account.getUserId())
                .applicationId(account.getApplicationId())
                .accountType(account.getAccountType())
                .status(account.getStatus())
                .balance(account.getBalance())
                .remarks(account.getRemarks())
                .createdAt(account.getCreatedAt())
                .updatedAt(account.getUpdatedAt())
                .build();
    }
}
