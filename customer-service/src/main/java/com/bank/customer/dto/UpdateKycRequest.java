package com.bank.customer.dto;

import com.bank.customer.entity.Customer.KycStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateKycRequest {

    @NotNull(message = "KYC status is required")
    private KycStatus kycStatus;

    private String kycRemarks;
}
