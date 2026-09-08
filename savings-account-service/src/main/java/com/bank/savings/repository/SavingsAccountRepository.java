package com.bank.savings.repository;

import com.bank.savings.entity.AccountStatus;
import com.bank.savings.entity.SavingsAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SavingsAccountRepository extends JpaRepository<SavingsAccount, Long> {

    Optional<SavingsAccount> findByAccountNumber(String accountNumber);

    List<SavingsAccount> findByCustomerId(Long customerId);

    List<SavingsAccount> findByUserId(String userId);

    Optional<SavingsAccount> findByApplicationId(Long applicationId);

    List<SavingsAccount> findByStatus(AccountStatus status);

    boolean existsByApplicationId(Long applicationId);

    /**
     * Generates the next unique account number using PostgreSQL's atomic sequence.
     * Thread-safe: PostgreSQL nextval() guarantees no duplicates under concurrent access.
     * Format: ACC-XXXXXXXXXX (zero-padded to 10 digits)
     */
    @Query(value = "SELECT 'ACC-' || LPAD(nextval('savings_db.account_number_seq')::TEXT, 10, '0')", nativeQuery = true)
    String generateNextAccountNumber();
}
