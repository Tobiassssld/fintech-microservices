package com.example.fintech.accountservice.service;

import com.example.fintech.common.entity.Account;
import java.math.BigDecimal;
import java.util.List;

public interface AccountService {
    Account createAccount(Long userId, String username, String accountType, String currency);  // 添加username参数
    Account getAccountByUserId(Long userId);
    Account getAccountByAccountNumber(String accountNumber);
    List<Account> getAccountsByUserId(Long userId);
    BigDecimal getBalance(String accountNumber);
    boolean deposit(String accountNumber, BigDecimal amount, Long userId);
    boolean withdraw(String accountNumber, BigDecimal amount, Long userId);
    boolean transfer(String fromAccount, String toAccount, BigDecimal amount, Long userId);
    String generateAccountNumber();
}