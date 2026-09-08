package com.bank.savings.controller;

import com.bank.common.dto.ApiResponse;
import com.bank.savings.dto.*;
import com.bank.savings.service.SavingsAccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/savings-accounts")
@RequiredArgsConstructor
@Tag(name = "Savings Accounts", description = "Endpoints for savings account provisioning and management")
public class SavingsAccountController {

    private final SavingsAccountService service;

    @PostMapping
    @Operation(summary = "Provision a new savings account from an approved application")
    public ResponseEntity<ApiResponse<SavingsAccountResponse>> createAccount(
            @Valid @RequestBody CreateSavingsAccountRequest request) {
        SavingsAccountResponse response = service.createAccount(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Savings account provisioned successfully.", response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get savings account by ID")
    public ResponseEntity<ApiResponse<SavingsAccountResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Savings account retrieved.", service.getAccountById(id)));
    }

    @GetMapping("/number/{accountNumber}")
    @Operation(summary = "Get savings account by account number")
    public ResponseEntity<ApiResponse<SavingsAccountResponse>> getByAccountNumber(@PathVariable String accountNumber) {
        return ResponseEntity.ok(ApiResponse.success("Savings account retrieved.", service.getAccountByNumber(accountNumber)));
    }

    @GetMapping("/customer/{customerId}")
    @Operation(summary = "Get all savings accounts for a customer")
    public ResponseEntity<ApiResponse<List<SavingsAccountResponse>>> getByCustomerId(@PathVariable Long customerId) {
        return ResponseEntity.ok(ApiResponse.success("Customer accounts retrieved.", service.getAccountsByCustomerId(customerId)));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get all savings accounts for a user")
    public ResponseEntity<ApiResponse<List<SavingsAccountResponse>>> getByUserId(@PathVariable String userId) {
        return ResponseEntity.ok(ApiResponse.success("User accounts retrieved.", service.getAccountsByUserId(userId)));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all savings accounts (Admin only)")
    public ResponseEntity<ApiResponse<List<SavingsAccountResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success("All accounts retrieved.", service.getAllAccounts()));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update account status (Admin only)")
    public ResponseEntity<ApiResponse<SavingsAccountResponse>> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateAccountStatusRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Account status updated.", service.updateStatus(id, request)));
    }

    @PatchMapping("/{id}/balance")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Credit or debit account balance (Admin only)")
    public ResponseEntity<ApiResponse<SavingsAccountResponse>> updateBalance(
            @PathVariable Long id,
            @Valid @RequestBody BalanceUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Balance updated successfully.", service.updateBalance(id, request)));
    }
}
