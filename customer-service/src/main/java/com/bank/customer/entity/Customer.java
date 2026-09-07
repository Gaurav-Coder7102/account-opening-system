package com.bank.customer.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "customers", schema = "customer_db",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_customers_email", columnNames = "email"),
                @UniqueConstraint(name = "uk_customers_pan", columnNames = "pan_number"),
                @UniqueConstraint(name = "uk_customers_aadhaar", columnNames = "aadhaar_number"),
                @UniqueConstraint(name = "uk_customers_user_id", columnNames = "user_id")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Links to the user account in authentication-service (by userId stored in JWT).
     */
    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(name = "email", nullable = false, length = 150)
    private String email;

    @Column(name = "phone_number", nullable = false, length = 15)
    private String phoneNumber;

    @Column(name = "date_of_birth", nullable = false)
    private LocalDate dateOfBirth;

    // --- Address ---
    @Column(name = "address_line1", nullable = false)
    private String addressLine1;

    @Column(name = "address_line2")
    private String addressLine2;

    @Column(name = "city", nullable = false, length = 100)
    private String city;

    @Column(name = "state", nullable = false, length = 100)
    private String state;

    @Column(name = "pincode", nullable = false, length = 10)
    private String pincode;

    // --- KYC ---
    @Column(name = "pan_number", nullable = false, length = 10)
    private String panNumber;

    @Column(name = "aadhaar_number", nullable = false, length = 12)
    private String aadhaarNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "kyc_status", nullable = false, length = 20)
    @Builder.Default
    private KycStatus kycStatus = KycStatus.PENDING;

    @Column(name = "kyc_remarks")
    private String kycRemarks;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public enum KycStatus {
        PENDING, VERIFIED, REJECTED
    }
}
