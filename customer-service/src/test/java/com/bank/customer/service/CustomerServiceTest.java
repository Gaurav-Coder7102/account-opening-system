package com.bank.customer.service;

import com.bank.customer.dto.CreateCustomerRequest;
import com.bank.customer.dto.CustomerResponse;
import com.bank.customer.dto.UpdateKycRequest;
import com.bank.customer.entity.Customer;
import com.bank.customer.exception.CustomerAlreadyExistsException;
import com.bank.customer.exception.CustomerNotFoundException;
import com.bank.customer.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CustomerService Tests")
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private CustomerService customerService;

    private CreateCustomerRequest validRequest;
    private Customer savedCustomer;

    @BeforeEach
    void setUp() {
        validRequest = new CreateCustomerRequest();
        validRequest.setFirstName("Gaurav");
        validRequest.setLastName("Kumar");
        validRequest.setEmail("gaurav@example.com");
        validRequest.setPhoneNumber("9876543210");
        validRequest.setDateOfBirth(LocalDate.of(1995, 5, 15));
        validRequest.setAddressLine1("123 MG Road");
        validRequest.setCity("Pune");
        validRequest.setState("Maharashtra");
        validRequest.setPincode("411001");
        validRequest.setPanNumber("ABCDE1234F");
        validRequest.setAadhaarNumber("234567890123");

        savedCustomer = Customer.builder()
                .id(1L)
                .userId("user-uuid-123")
                .firstName("Gaurav")
                .lastName("Kumar")
                .email("gaurav@example.com")
                .phoneNumber("9876543210")
                .dateOfBirth(LocalDate.of(1995, 5, 15))
                .addressLine1("123 MG Road")
                .city("Pune")
                .state("Maharashtra")
                .pincode("411001")
                .panNumber("ABCDE1234F")
                .aadhaarNumber("234567890123")
                .kycStatus(Customer.KycStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Should create customer profile successfully")
    void shouldCreateCustomerSuccessfully() {
        when(customerRepository.existsByUserId(anyString())).thenReturn(false);
        when(customerRepository.existsByEmail(anyString())).thenReturn(false);
        when(customerRepository.existsByPanNumber(anyString())).thenReturn(false);
        when(customerRepository.existsByAadhaarNumber(anyString())).thenReturn(false);
        when(customerRepository.save(any(Customer.class))).thenReturn(savedCustomer);

        CustomerResponse response = customerService.createCustomer(validRequest, "user-uuid-123", "gaurav@example.com");

        assertThat(response).isNotNull();
        assertThat(response.getFirstName()).isEqualTo("Gaurav");
        assertThat(response.getKycStatus()).isEqualTo(Customer.KycStatus.PENDING);
        assertThat(response.getAadhaarNumberMasked()).startsWith("XXXX-XXXX-");
        verify(customerRepository, times(1)).save(any(Customer.class));
    }

    @Test
    @DisplayName("Should throw CustomerAlreadyExistsException when userId already exists")
    void shouldThrowExceptionWhenUserIdExists() {
        when(customerRepository.existsByUserId("user-uuid-123")).thenReturn(true);

        assertThatThrownBy(() -> customerService.createCustomer(validRequest, "user-uuid-123", "gaurav@example.com"))
                .isInstanceOf(CustomerAlreadyExistsException.class)
                .hasMessageContaining("already exists for this user account");
    }

    @Test
    @DisplayName("Should throw CustomerAlreadyExistsException when PAN already exists")
    void shouldThrowExceptionWhenPanExists() {
        when(customerRepository.existsByUserId(anyString())).thenReturn(false);
        when(customerRepository.existsByEmail(anyString())).thenReturn(false);
        when(customerRepository.existsByPanNumber("ABCDE1234F")).thenReturn(true);

        assertThatThrownBy(() -> customerService.createCustomer(validRequest, "user-uuid-456", "other@example.com"))
                .isInstanceOf(CustomerAlreadyExistsException.class)
                .hasMessageContaining("PAN");
    }

    @Test
    @DisplayName("Should return customer profile for getMyProfile")
    void shouldReturnMyProfile() {
        when(customerRepository.findByUserId("user-uuid-123")).thenReturn(Optional.of(savedCustomer));

        CustomerResponse response = customerService.getMyProfile("user-uuid-123");

        assertThat(response).isNotNull();
        assertThat(response.getUserId()).isEqualTo("user-uuid-123");
    }

    @Test
    @DisplayName("Should throw CustomerNotFoundException for unknown userId")
    void shouldThrowNotFoundForUnknownUserId() {
        when(customerRepository.findByUserId("unknown-id")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> customerService.getMyProfile("unknown-id"))
                .isInstanceOf(CustomerNotFoundException.class);
    }

    @Test
    @DisplayName("Should update KYC status to VERIFIED")
    void shouldUpdateKycStatus() {
        UpdateKycRequest kycRequest = new UpdateKycRequest();
        kycRequest.setKycStatus(Customer.KycStatus.VERIFIED);
        kycRequest.setKycRemarks("All documents verified");

        Customer verifiedCustomer = savedCustomer;
        verifiedCustomer.setKycStatus(Customer.KycStatus.VERIFIED);
        verifiedCustomer.setKycRemarks("All documents verified");

        when(customerRepository.findById(1L)).thenReturn(Optional.of(savedCustomer));
        when(customerRepository.save(any(Customer.class))).thenReturn(verifiedCustomer);

        CustomerResponse response = customerService.updateKycStatus(1L, kycRequest);

        assertThat(response.getKycStatus()).isEqualTo(Customer.KycStatus.VERIFIED);
        assertThat(response.getKycRemarks()).isEqualTo("All documents verified");
    }
}
