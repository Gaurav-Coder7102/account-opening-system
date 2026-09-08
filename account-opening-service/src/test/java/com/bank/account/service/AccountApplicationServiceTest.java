package com.bank.account.service;

import com.bank.account.client.CustomerClient;
import com.bank.account.client.SavingsAccountClient;
import com.bank.account.dto.*;
import com.bank.account.entity.AccountApplication;
import com.bank.account.entity.AccountType;
import com.bank.account.entity.ApplicationStatus;
import com.bank.account.exception.ApplicationNotFoundException;
import com.bank.account.exception.InvalidStateTransitionException;
import com.bank.account.repository.AccountApplicationRepository;
import com.bank.common.dto.ApiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountApplicationServiceTest {

    @Mock
    private AccountApplicationRepository repository;

    @Mock
    private CustomerClient customerClient;

    @Mock
    private SavingsAccountClient savingsAccountClient;

    @InjectMocks
    private AccountApplicationService service;

    private AccountApplication draftApplication;

    @BeforeEach
    void setUp() {
        draftApplication = AccountApplication.builder()
                .id(1L)
                .applicationNumber("APP-12345678")
                .customerId(100L)
                .userId("user-uuid-1")
                .accountType(AccountType.SAVINGS)
                .status(ApplicationStatus.DRAFT)
                .initialDeposit(new BigDecimal("1000.00"))
                .remarks("Initial Draft")
                .build();
    }

    @Test
    void testCreateApplication_Success() {
        CreateApplicationRequest request = CreateApplicationRequest.builder()
                .customerId(100L)
                .userId("user-uuid-1")
                .accountType(AccountType.SAVINGS)
                .initialDeposit(new BigDecimal("1000.00"))
                .remarks("New Application")
                .build();

        CustomerDto customerDto = CustomerDto.builder()
                .id(100L)
                .userId("user-uuid-1")
                .kycStatus("VERIFIED")
                .build();

        when(customerClient.getCustomerById(100L)).thenReturn(ApiResponse.success("Success", customerDto));
        when(repository.save(any(AccountApplication.class))).thenAnswer(invocation -> {
            AccountApplication app = invocation.getArgument(0);
            app.setId(1L);
            return app;
        });

        ApplicationResponse response = service.createApplication(request);

        assertNotNull(response);
        assertEquals(ApplicationStatus.DRAFT, response.getStatus());
        assertEquals(AccountType.SAVINGS, response.getAccountType());
        verify(repository, times(1)).save(any());
    }

    @Test
    void testSubmitApplication_Success() {
        when(repository.findById(1L)).thenReturn(Optional.of(draftApplication));
        when(repository.save(any(AccountApplication.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ApplicationResponse response = service.submitApplication(1L);

        assertEquals(ApplicationStatus.SUBMITTED, response.getStatus());
        verify(repository, times(1)).save(draftApplication);
    }

    @Test
    void testSubmitApplication_InvalidStatus_ThrowsException() {
        draftApplication.setStatus(ApplicationStatus.SUBMITTED);
        when(repository.findById(1L)).thenReturn(Optional.of(draftApplication));

        assertThrows(InvalidStateTransitionException.class, () -> service.submitApplication(1L));
        verify(repository, never()).save(any());
    }

    @Test
    void testUpdateStatus_SubmittedToUnderReview_Success() {
        draftApplication.setStatus(ApplicationStatus.SUBMITTED);
        when(repository.findById(1L)).thenReturn(Optional.of(draftApplication));
        when(repository.save(any(AccountApplication.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UpdateStatusRequest request = UpdateStatusRequest.builder()
                .targetStatus(ApplicationStatus.UNDER_REVIEW)
                .remarks("Under Review by Admin")
                .build();

        ApplicationResponse response = service.updateStatus(1L, request);

        assertEquals(ApplicationStatus.UNDER_REVIEW, response.getStatus());
        assertEquals("Under Review by Admin", response.getRemarks());
    }

    @Test
    void testUpdateStatus_ApproveWithVerifiedKyc_Success() {
        draftApplication.setStatus(ApplicationStatus.UNDER_REVIEW);
        when(repository.findById(1L)).thenReturn(Optional.of(draftApplication));
        when(repository.save(any(AccountApplication.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CustomerDto verifiedCustomer = CustomerDto.builder()
                .id(100L)
                .kycStatus("VERIFIED")
                .build();
        when(customerClient.getCustomerById(100L)).thenReturn(ApiResponse.success("Success", verifiedCustomer));

        UpdateStatusRequest request = UpdateStatusRequest.builder()
                .targetStatus(ApplicationStatus.APPROVED)
                .remarks("Application Approved")
                .build();

        ApplicationResponse response = service.updateStatus(1L, request);

        assertEquals(ApplicationStatus.APPROVED, response.getStatus());
    }

    @Test
    void testUpdateStatus_ApproveWithPendingKyc_ThrowsException() {
        draftApplication.setStatus(ApplicationStatus.UNDER_REVIEW);
        when(repository.findById(1L)).thenReturn(Optional.of(draftApplication));

        CustomerDto pendingCustomer = CustomerDto.builder()
                .id(100L)
                .kycStatus("PENDING")
                .build();
        when(customerClient.getCustomerById(100L)).thenReturn(ApiResponse.success("Success", pendingCustomer));

        UpdateStatusRequest request = UpdateStatusRequest.builder()
                .targetStatus(ApplicationStatus.APPROVED)
                .build();

        assertThrows(InvalidStateTransitionException.class, () -> service.updateStatus(1L, request));
    }

    @Test
    void testUpdateStatus_InvalidTransition_DraftToApproved_ThrowsException() {
        when(repository.findById(1L)).thenReturn(Optional.of(draftApplication));

        UpdateStatusRequest request = UpdateStatusRequest.builder()
                .targetStatus(ApplicationStatus.APPROVED)
                .build();

        assertThrows(InvalidStateTransitionException.class, () -> service.updateStatus(1L, request));
    }

    @Test
    void testGetApplicationById_NotFound_ThrowsException() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ApplicationNotFoundException.class, () -> service.getApplicationById(99L));
    }
}
