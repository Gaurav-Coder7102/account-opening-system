package com.bank.account.repository;

import com.bank.account.entity.AccountApplication;
import com.bank.account.entity.ApplicationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountApplicationRepository extends JpaRepository<AccountApplication, Long> {

    Optional<AccountApplication> findByApplicationNumber(String applicationNumber);

    List<AccountApplication> findByCustomerId(Long customerId);

    List<AccountApplication> findByUserId(String userId);

    List<AccountApplication> findByStatus(ApplicationStatus status);
}
