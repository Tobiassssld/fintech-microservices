package com.example.fintech.accountservice.repository;

import com.example.fintech.common.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {
    Optional<Account> findByAccountNumber(String accountNumber);

    @Query("SELECT a FROM Account a WHERE a.user.id = :userId")
    Optional<Account> findByUserId(@Param("userId") Long userId);

    // 添加这个方法 - 返回用户的所有账户
    @Query("SELECT a FROM Account a WHERE a.user.id = :userId")
    List<Account> findAllByUserId(@Param("userId") Long userId);

    boolean existsByAccountNumber(String accountNumber);

    @Modifying
    @Query("UPDATE Account a SET a.balance = a.balance + :amount WHERE a.id = :accountId")
    int updateBalance(@Param("accountId") Long accountId, @Param("amount") BigDecimal amount);
}