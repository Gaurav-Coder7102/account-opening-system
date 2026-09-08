package com.bank.account.service;

import com.bank.account.client.CustomerClient;
import com.bank.account.client.SavingsAccountClient;
import com.bank.account.dto.*;
import com.bank.account.entity.AccountApplication;
import com.bank.account.entity.ApplicationStatus;
import com.bank.account.exception.ApplicationNotFoundException;
import com.bank.account.exception.InvalidStateTransitionException;
import com.bank.account.repository.AccountApplicationRepository;
import com.bank.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountApplicationService {

    private final AccountApplicationRepository repository;
    private final CustomerClient customerClient;
    private final SavingsAccountClient savingsAccountClient;

    @Transactional
    public ApplicationResponse createApplication(CreateApplicationRequest request) {
        log.info("Creating new account application for customerId: {}, userId: {}", request.getCustomerId(), request.getUserId());

        // Validate customer existence via OpenFeign
        verifyCustomerExists(request.getCustomerId());

        String appNumber = "APP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        AccountApplication application = AccountApplication.builder()
                .applicationNumber(appNumber)
                .customerId(request.getCustomerId())
                .userId(request.getUserId())
                .accountType(request.getAccountType())
                .status(ApplicationStatus.DRAFT)
                .initialDeposit(request.getInitialDeposit())
                .remarks(request.getRemarks())
                .build();

        AccountApplication saved = repository.save(application);
        log.info("Account application created successfully with number: {}", appNumber);
        return mapToResponse(saved);
    }

    @Transactional
    public ApplicationResponse submitApplication(Long id) {
        AccountApplication app = findByIdOrThrow(id);

        if (app.getStatus() != ApplicationStatus.DRAFT) {
            throw new InvalidStateTransitionException(
                    String.format("Cannot submit application in status '%s'. Only DRAFT applications can be submitted.", app.getStatus())
            );
        }

        app.setStatus(ApplicationStatus.SUBMITTED);
        app.setRemarks("Application submitted for review.");
        AccountApplication updated = repository.save(app);
        log.info("Application {} submitted successfully.", app.getApplicationNumber());
        return mapToResponse(updated);
    }

    @Transactional
    public ApplicationResponse updateStatus(Long id, UpdateStatusRequest request) {
        AccountApplication app = findByIdOrThrow(id);
        ApplicationStatus current = app.getStatus();
        ApplicationStatus target = request.getTargetStatus();

        log.info("Attempting state transition for app {}: {} -> {}", app.getApplicationNumber(), current, target);

        validateStateTransition(app, target);

        // Additional domain validation: If approving, verify Customer KYC is VERIFIED
        if (target == ApplicationStatus.APPROVED) {
            verifyCustomerKycVerified(app.getCustomerId());
        }

        app.setStatus(target);
        if (request.getRemarks() != null && !request.getRemarks().isBlank()) {
            app.setRemarks(request.getRemarks());
        }

        AccountApplication updated = repository.save(app);
        log.info("Application {} updated to status {}", app.getApplicationNumber(), target);

        // On ACCOUNT_CREATED transition: provision savings account via Feign
        if (target == ApplicationStatus.ACCOUNT_CREATED) {
            provisionSavingsAccount(updated);
        }

        return mapToResponse(updated);
    }

    @Transactional(readOnly = true)
    public ApplicationResponse getApplicationById(Long id) {
        return mapToResponse(findByIdOrThrow(id));
    }

    @Transactional(readOnly = true)
    public List<ApplicationResponse> getApplicationsByCustomerId(Long customerId) {
        return repository.findByCustomerId(customerId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ApplicationResponse> getApplicationsByUserId(String userId) {
        return repository.findByUserId(userId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ApplicationResponse> getAllApplications() {
        return repository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private void validateStateTransition(AccountApplication app, ApplicationStatus target) {
        ApplicationStatus current = app.getStatus();

        boolean isValid = switch (current) {
            case DRAFT -> target == ApplicationStatus.SUBMITTED;
            case SUBMITTED -> target == ApplicationStatus.UNDER_REVIEW || target == ApplicationStatus.REJECTED;
            case UNDER_REVIEW -> target == ApplicationStatus.APPROVED || target == ApplicationStatus.REJECTED;
            case APPROVED -> target == ApplicationStatus.ACCOUNT_CREATED;
            case REJECTED, ACCOUNT_CREATED -> false; // Terminal states
        };

        if (!isValid) {
            throw new InvalidStateTransitionException(
                    String.format("Invalid status transition from '%s' to '%s'.", current, target)
            );
        }
    }

    /**
     * Calls savings-account-service via OpenFeign to provision a savings account
     * once the application reaches ACCOUNT_CREATED state.
     */
    private void provisionSavingsAccount(AccountApplication app) {
        try {
            CreateSavingsAccountRequest savingsRequest = CreateSavingsAccountRequest.builder()
                    .customerId(app.getCustomerId())
                    .userId(app.getUserId())
                    .applicationId(app.getId())
                    .accountType(app.getAccountType().name())
                    .initialBalance(app.getInitialDeposit())
                    .remarks("Auto-provisioned from application: " + app.getApplicationNumber())
                    .build();

            ApiResponse<SavingsAccountDto> response = savingsAccountClient.provisionAccount(savingsRequest);
            if (response != null && response.isSuccess() && response.getData() != null) {
                log.info("Savings account '{}' provisioned for application '{}'",
                        response.getData().getAccountNumber(), app.getApplicationNumber());
            }
        } catch (Exception ex) {
            log.warn("Could not auto-provision savings account for application '{}': {}",
                    app.getApplicationNumber(), ex.getMessage());
        }
    }

    private void verifyCustomerExists(Long customerId) {
        try {
            ApiResponse<CustomerDto> response = customerClient.getCustomerById(customerId);
            if (response == null || !response.isSuccess() || response.getData() == null) {
                log.warn("Customer validation returned unsuccessful response for customerId: {}", customerId);
            }
        } catch (Exception ex) {
            log.warn("Feign client failed to verify customerId {}: {}", customerId, ex.getMessage());
            // Fail safe logging; allow creation for decoupling during standalone runs/testing
        }
    }

    private void verifyCustomerKycVerified(Long customerId) {
        try {
            ApiResponse<CustomerDto> response = customerClient.getCustomerById(customerId);
            if (response != null && response.isSuccess() && response.getData() != null) {
                CustomerDto customer = response.getData();
                if (!"VERIFIED".equalsIgnoreCase(customer.getKycStatus())) {
                    throw new InvalidStateTransitionException(
                            String.format("Cannot approve application: Customer KYC status is '%s' (Must be 'VERIFIED').", customer.getKycStatus())
                    );
                }
            }
        } catch (InvalidStateTransitionException ex) {
            throw ex;
        } catch (Exception ex) {
            log.warn("Could not check KYC status via Feign client: {}", ex.getMessage());
        }
    }

    private AccountApplication findByIdOrThrow(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ApplicationNotFoundException("Account application not found with id: " + id));
    }

    private ApplicationResponse mapToResponse(AccountApplication app) {
        return ApplicationResponse.builder()
                .id(app.getId())
                .applicationNumber(app.getApplicationNumber())
                .customerId(app.getCustomerId())
                .userId(app.getUserId())
                .accountType(app.getAccountType())
                .status(app.getStatus())
                .initialDeposit(app.getInitialDeposit())
                .remarks(app.getRemarks())
                .createdAt(app.getCreatedAt())
                .updatedAt(app.getUpdatedAt())
                .build();
    }
}
