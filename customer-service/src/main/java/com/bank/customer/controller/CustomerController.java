package com.bank.customer.controller;

import com.bank.common.dto.ApiResponse;
import com.bank.customer.dto.CreateCustomerRequest;
import com.bank.customer.dto.CustomerResponse;
import com.bank.customer.dto.UpdateKycRequest;
import com.bank.customer.service.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
@Tag(name = "Customer Management", description = "APIs for managing customer profiles and KYC")
@SecurityRequirement(name = "bearerAuth")
public class CustomerController {

    private final CustomerService customerService;

    /**
     * POST /api/customers
     * Creates a customer profile for the currently authenticated CUSTOMER user.
     * userId is extracted from the JWT credential (set by JwtAuthenticationFilter).
     */
    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Create customer profile", description = "Creates a new customer profile for the logged-in user")
    public ResponseEntity<ApiResponse<CustomerResponse>> createCustomer(
            @Valid @RequestBody CreateCustomerRequest request,
            Authentication authentication) {

        // username = email/username from token principal; userId = from token credential
        String userId = (String) authentication.getCredentials();
        String userEmail = authentication.getName();

        CustomerResponse response = customerService.createCustomer(request, userId, userEmail);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Customer profile created successfully", response));
    }

    /**
     * GET /api/customers/me
     * Returns the profile of the currently authenticated CUSTOMER.
     */
    @GetMapping("/me")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Get my profile", description = "Returns the profile of the currently logged-in customer")
    public ResponseEntity<ApiResponse<CustomerResponse>> getMyProfile(Authentication authentication) {
        String userId = (String) authentication.getCredentials();
        CustomerResponse response = customerService.getMyProfile(userId);
        return ResponseEntity.ok(ApiResponse.success("Customer profile fetched successfully", response));
    }

    /**
     * GET /api/customers/{id}
     * Returns any customer's profile by ID. Accessible to BANK_EMPLOYEE and ADMIN.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('BANK_EMPLOYEE', 'ADMIN')")
    @Operation(summary = "Get customer by ID", description = "Returns customer profile by ID (BANK_EMPLOYEE / ADMIN only)")
    public ResponseEntity<ApiResponse<CustomerResponse>> getCustomerById(@PathVariable Long id) {
        CustomerResponse response = customerService.getCustomerById(id);
        return ResponseEntity.ok(ApiResponse.success("Customer profile fetched successfully", response));
    }

    /**
     * PUT /api/customers/{id}/kyc
     * Updates the KYC status of a customer. BANK_EMPLOYEE and ADMIN only.
     */
    @PutMapping("/{id}/kyc")
    @PreAuthorize("hasAnyRole('BANK_EMPLOYEE', 'ADMIN')")
    @Operation(summary = "Update KYC status", description = "Updates the KYC verification status (BANK_EMPLOYEE / ADMIN only)")
    public ResponseEntity<ApiResponse<CustomerResponse>> updateKycStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateKycRequest request) {

        CustomerResponse response = customerService.updateKycStatus(id, request);
        return ResponseEntity.ok(ApiResponse.success("KYC status updated successfully", response));
    }
}
