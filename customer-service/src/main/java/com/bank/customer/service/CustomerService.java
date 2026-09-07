package com.bank.customer.service;

import com.bank.customer.dto.CreateCustomerRequest;
import com.bank.customer.dto.CustomerResponse;
import com.bank.customer.dto.UpdateKycRequest;
import com.bank.customer.entity.Customer;
import com.bank.customer.exception.CustomerAlreadyExistsException;
import com.bank.customer.exception.CustomerNotFoundException;
import com.bank.customer.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerService {

    private final CustomerRepository customerRepository;

    /**
     * Creates a new customer profile linked to the authenticated user.
     *
     * @param request  the create request with all customer details
     * @param userId   the ID of the authenticated user (from JWT credential)
     * @param userEmail the email of the authenticated user
     * @return CustomerResponse with the created customer data
     */
    @Transactional
    public CustomerResponse createCustomer(CreateCustomerRequest request, String userId, String userEmail) {
        log.info("Creating customer profile for userId: {}", userId);

        // Duplicate checks
        if (customerRepository.existsByUserId(userId)) {
            throw new CustomerAlreadyExistsException("A customer profile already exists for this user account.");
        }
        if (customerRepository.existsByEmail(request.getEmail())) {
            throw new CustomerAlreadyExistsException("A customer profile with email '" + request.getEmail() + "' already exists.");
        }
        if (customerRepository.existsByPanNumber(request.getPanNumber().toUpperCase())) {
            throw new CustomerAlreadyExistsException("A customer profile with PAN '" + request.getPanNumber() + "' already exists.");
        }
        if (customerRepository.existsByAadhaarNumber(request.getAadhaarNumber())) {
            throw new CustomerAlreadyExistsException("A customer profile with this Aadhaar number already exists.");
        }

        Customer customer = Customer.builder()
                .userId(userId)
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .dateOfBirth(request.getDateOfBirth())
                .addressLine1(request.getAddressLine1())
                .addressLine2(request.getAddressLine2())
                .city(request.getCity())
                .state(request.getState())
                .pincode(request.getPincode())
                .panNumber(request.getPanNumber().toUpperCase())
                .aadhaarNumber(request.getAadhaarNumber())
                .kycStatus(Customer.KycStatus.PENDING)
                .build();

        Customer saved = customerRepository.save(customer);
        log.info("Customer profile created with id: {} for userId: {}", saved.getId(), userId);
        return toResponse(saved);
    }

    /**
     * Returns the customer profile of the currently authenticated user.
     */
    @Transactional(readOnly = true)
    public CustomerResponse getMyProfile(String userId) {
        log.info("Fetching profile for userId: {}", userId);
        Customer customer = customerRepository.findByUserId(userId)
                .orElseThrow(() -> new CustomerNotFoundException("No customer profile found for this user. Please create your profile first."));
        return toResponse(customer);
    }

    /**
     * Returns a customer profile by its database ID. Accessible to BANK_EMPLOYEE and ADMIN.
     */
    @Transactional(readOnly = true)
    public CustomerResponse getCustomerById(Long customerId) {
        log.info("Fetching customer by id: {}", customerId);
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with id: " + customerId));
        return toResponse(customer);
    }

    /**
     * Updates the KYC status of a customer. Only BANK_EMPLOYEE or ADMIN can call this.
     */
    @Transactional
    public CustomerResponse updateKycStatus(Long customerId, UpdateKycRequest request) {
        log.info("Updating KYC status for customerId: {} to {}", customerId, request.getKycStatus());
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with id: " + customerId));

        customer.setKycStatus(request.getKycStatus());
        customer.setKycRemarks(request.getKycRemarks());

        Customer updated = customerRepository.save(customer);
        log.info("KYC status updated for customerId: {} to {}", customerId, updated.getKycStatus());
        return toResponse(updated);
    }

    /**
     * Maps a Customer entity to a CustomerResponse DTO, masking the Aadhaar number.
     */
    private CustomerResponse toResponse(Customer customer) {
        String maskedAadhaar = "XXXX-XXXX-" + customer.getAadhaarNumber().substring(8);
        return CustomerResponse.builder()
                .id(customer.getId())
                .userId(customer.getUserId())
                .firstName(customer.getFirstName())
                .lastName(customer.getLastName())
                .email(customer.getEmail())
                .phoneNumber(customer.getPhoneNumber())
                .dateOfBirth(customer.getDateOfBirth())
                .addressLine1(customer.getAddressLine1())
                .addressLine2(customer.getAddressLine2())
                .city(customer.getCity())
                .state(customer.getState())
                .pincode(customer.getPincode())
                .panNumber(customer.getPanNumber())
                .aadhaarNumberMasked(maskedAadhaar)
                .kycStatus(customer.getKycStatus())
                .kycRemarks(customer.getKycRemarks())
                .createdAt(customer.getCreatedAt())
                .updatedAt(customer.getUpdatedAt())
                .build();
    }
}
