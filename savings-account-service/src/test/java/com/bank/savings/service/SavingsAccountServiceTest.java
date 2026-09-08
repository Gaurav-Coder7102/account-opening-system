package com.bank.savings.service;

import com.bank.savings.dto.*;
import com.bank.savings.entity.AccountStatus;
import com.bank.savings.entity.SavingsAccount;
import com.bank.savings.entity.SavingsAccountType;
import com.bank.savings.exception.DuplicateAccountException;
import com.bank.savings.exception.SavingsAccountNotFoundException;
import com.bank.savings.repository.SavingsAccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SavingsAccountServiceTest {

    @Mock
    private SavingsAccountRepository repository;

    @InjectMocks
    private SavingsAccountService service;

    private SavingsAccount activeSavingsAccount;

    @BeforeEach
    void setUp() {
        activeSavingsAccount = SavingsAccount.builder()
                .id(1L)
                .accountNumber("ACC-0100000001")
                .customerId(100L)
                .userId("user-uuid-1")
                .applicationId(200L)
                .accountType(SavingsAccountType.SAVINGS)
                .status(AccountStatus.ACTIVE)
                .balance(new BigDecimal("5000.00"))
                .build();
        activeSavingsAccount.setCreatedAt(LocalDateTime.now());
        activeSavingsAccount.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    void testCreateAccount_Success() {
        CreateSavingsAccountRequest request = CreateSavingsAccountRequest.builder()
                .customerId(100L)
                .userId("user-uuid-1")
                .applicationId(200L)
                .accountType(SavingsAccountType.SAVINGS)
                .initialBalance(new BigDecimal("5000.00"))
                .build();

        when(repository.existsByApplicationId(200L)).thenReturn(false);
        when(repository.generateNextAccountNumber()).thenReturn("ACC-0100000001");
        when(repository.save(any(SavingsAccount.class))).thenAnswer(inv -> {
            SavingsAccount acc = inv.getArgument(0);
            acc.setId(1L);
            acc.setCreatedAt(LocalDateTime.now());
            acc.setUpdatedAt(LocalDateTime.now());
            return acc;
        });

        SavingsAccountResponse response = service.createAccount(request);

        assertNotNull(response);
        assertEquals("ACC-0100000001", response.getAccountNumber());
        assertEquals(AccountStatus.ACTIVE, response.getStatus());
        assertEquals(new BigDecimal("5000.00"), response.getBalance());
        verify(repository, times(1)).generateNextAccountNumber();
        verify(repository, times(1)).save(any());
    }

    @Test
    void testCreateAccount_DuplicateApplicationId_ThrowsException() {
        CreateSavingsAccountRequest request = CreateSavingsAccountRequest.builder()
                .customerId(100L)
                .userId("user-uuid-1")
                .applicationId(200L)
                .accountType(SavingsAccountType.SAVINGS)
                .initialBalance(new BigDecimal("5000.00"))
                .build();

        when(repository.existsByApplicationId(200L)).thenReturn(true);

        assertThrows(DuplicateAccountException.class, () -> service.createAccount(request));
        verify(repository, never()).generateNextAccountNumber();
        verify(repository, never()).save(any());
    }

    @Test
    void testGetAccountById_Success() {
        when(repository.findById(1L)).thenReturn(Optional.of(activeSavingsAccount));

        SavingsAccountResponse response = service.getAccountById(1L);

        assertNotNull(response);
        assertEquals("ACC-0100000001", response.getAccountNumber());
        assertEquals(AccountStatus.ACTIVE, response.getStatus());
    }

    @Test
    void testGetAccountById_NotFound_ThrowsException() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(SavingsAccountNotFoundException.class, () -> service.getAccountById(99L));
    }

    @Test
    void testGetByCustomerId_Success() {
        when(repository.findByCustomerId(100L)).thenReturn(List.of(activeSavingsAccount));

        List<SavingsAccountResponse> result = service.getAccountsByCustomerId(100L);

        assertEquals(1, result.size());
        assertEquals("ACC-0100000001", result.get(0).getAccountNumber());
    }

    @Test
    void testUpdateStatus_ActiveToDormant_Success() {
        when(repository.findById(1L)).thenReturn(Optional.of(activeSavingsAccount));
        when(repository.save(any(SavingsAccount.class))).thenAnswer(inv -> inv.getArgument(0));

        UpdateAccountStatusRequest request = UpdateAccountStatusRequest.builder()
                .targetStatus(AccountStatus.DORMANT)
                .remarks("Account dormant due to inactivity")
                .build();

        SavingsAccountResponse response = service.updateStatus(1L, request);

        assertEquals(AccountStatus.DORMANT, response.getStatus());
        assertEquals("Account dormant due to inactivity", response.getRemarks());
    }

    @Test
    void testUpdateBalance_Credit_Success() {
        when(repository.findById(1L)).thenReturn(Optional.of(activeSavingsAccount));
        when(repository.save(any(SavingsAccount.class))).thenAnswer(inv -> inv.getArgument(0));

        BalanceUpdateRequest request = BalanceUpdateRequest.builder()
                .operation(BalanceUpdateRequest.Operation.CREDIT)
                .amount(new BigDecimal("1000.00"))
                .build();

        SavingsAccountResponse response = service.updateBalance(1L, request);

        assertEquals(new BigDecimal("6000.00"), response.getBalance());
    }

    @Test
    void testUpdateBalance_Debit_Success() {
        when(repository.findById(1L)).thenReturn(Optional.of(activeSavingsAccount));
        when(repository.save(any(SavingsAccount.class))).thenAnswer(inv -> inv.getArgument(0));

        BalanceUpdateRequest request = BalanceUpdateRequest.builder()
                .operation(BalanceUpdateRequest.Operation.DEBIT)
                .amount(new BigDecimal("2000.00"))
                .build();

        SavingsAccountResponse response = service.updateBalance(1L, request);

        assertEquals(new BigDecimal("3000.00"), response.getBalance());
    }

    @Test
    void testUpdateBalance_InsufficientFunds_ThrowsException() {
        when(repository.findById(1L)).thenReturn(Optional.of(activeSavingsAccount));

        BalanceUpdateRequest request = BalanceUpdateRequest.builder()
                .operation(BalanceUpdateRequest.Operation.DEBIT)
                .amount(new BigDecimal("99999.00"))
                .build();

        assertThrows(IllegalArgumentException.class, () -> service.updateBalance(1L, request));
    }

    @Test
    void testUpdateBalance_ClosedAccount_ThrowsException() {
        activeSavingsAccount.setStatus(AccountStatus.CLOSED);
        when(repository.findById(1L)).thenReturn(Optional.of(activeSavingsAccount));

        BalanceUpdateRequest request = BalanceUpdateRequest.builder()
                .operation(BalanceUpdateRequest.Operation.CREDIT)
                .amount(new BigDecimal("500.00"))
                .build();

        assertThrows(IllegalArgumentException.class, () -> service.updateBalance(1L, request));
    }
}
