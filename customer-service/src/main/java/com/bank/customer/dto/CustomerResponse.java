package com.bank.customer.dto;

import com.bank.customer.entity.Customer.KycStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class CustomerResponse {
    private Long id;
    private String userId;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private LocalDate dateOfBirth;
    private String addressLine1;
    private String addressLine2;
    private String city;
    private String state;
    private String pincode;
    private String panNumber;
    // Aadhaar is masked for privacy: only last 4 digits shown
    private String aadhaarNumberMasked;
    private KycStatus kycStatus;
    private String kycRemarks;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
