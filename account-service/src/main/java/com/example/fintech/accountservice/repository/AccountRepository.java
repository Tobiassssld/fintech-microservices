package com.example.fintech.accountservice.repository;

import com.example.fintech.common.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    Optional<Account> findByAccountNumber(String accountNumber);

    @Query("SELECT a FROM Account a WHERE a.userId = :userId")
    List<Account> findAllByUserId(@Param("userId") Long userId);

    boolean existsByAccountNumber(String accountNumber);
}