package com.bank.customer.repository;

import com.bank.customer.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    Optional<Customer> findByUserId(String userId);

    boolean existsByEmail(String email);

    boolean existsByPanNumber(String panNumber);

    boolean existsByAadhaarNumber(String aadhaarNumber);

    boolean existsByUserId(String userId);
}
