package com.bank.account.controller;

import com.bank.account.dto.ApplicationResponse;
import com.bank.account.dto.CreateApplicationRequest;
import com.bank.account.dto.UpdateStatusRequest;
import com.bank.account.service.AccountApplicationService;
import com.bank.common.dto.ApiResponse;
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
@RequestMapping("/api/v1/applications")
@RequiredArgsConstructor
@Tag(name = "Account Applications", description = "Endpoints for orchestrating account opening applications and state machine workflow")
public class AccountApplicationController {

    private final AccountApplicationService service;

    @PostMapping
    @Operation(summary = "Create a new account application (DRAFT state)")
    public ResponseEntity<ApiResponse<ApplicationResponse>> createApplication(@Valid @RequestBody CreateApplicationRequest request) {
        ApplicationResponse response = service.createApplication(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Account application created successfully in DRAFT state.", response));
    }

    @PostMapping("/{id}/submit")
    @Operation(summary = "Submit an application (DRAFT -> SUBMITTED)")
    public ResponseEntity<ApiResponse<ApplicationResponse>> submitApplication(@PathVariable("id") Long id) {
        ApplicationResponse response = service.submitApplication(id);
        return ResponseEntity.ok(ApiResponse.success("Application submitted successfully.", response));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update application status (Admin only - review, approve, reject)")
    public ResponseEntity<ApiResponse<ApplicationResponse>> updateStatus(
            @PathVariable("id") Long id,
            @Valid @RequestBody UpdateStatusRequest request) {
        ApplicationResponse response = service.updateStatus(id, request);
        return ResponseEntity.ok(ApiResponse.success("Application status updated successfully.", response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get application details by ID")
    public ResponseEntity<ApiResponse<ApplicationResponse>> getById(@PathVariable("id") Long id) {
        ApplicationResponse response = service.getApplicationById(id);
        return ResponseEntity.ok(ApiResponse.success("Application details retrieved.", response));
    }

    @GetMapping("/customer/{customerId}")
    @Operation(summary = "Get applications for a customer")
    public ResponseEntity<ApiResponse<List<ApplicationResponse>>> getByCustomerId(@PathVariable("customerId") Long customerId) {
        List<ApplicationResponse> response = service.getApplicationsByCustomerId(customerId);
        return ResponseEntity.ok(ApiResponse.success("Customer applications retrieved.", response));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get applications for a user")
    public ResponseEntity<ApiResponse<List<ApplicationResponse>>> getByUserId(@PathVariable("userId") String userId) {
        List<ApplicationResponse> response = service.getApplicationsByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success("User applications retrieved.", response));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all applications (Admin only)")
    public ResponseEntity<ApiResponse<List<ApplicationResponse>>> getAll() {
        List<ApplicationResponse> response = service.getAllApplications();
        return ResponseEntity.ok(ApiResponse.success("All applications retrieved.", response));
    }
}
