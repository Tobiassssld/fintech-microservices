package com.example.fintech.accountservice.service;

import com.example.fintech.common.entity.Account;
import java.math.BigDecimal;

public interface AccountService {
    Account createAccount(Long userId, String accountType, String currency);
    Account getAccountByUserId(Long userId);
    Account getAccountByAccountNumber(String accountNumber);
    BigDecimal getBalance(String accountNumber);
    boolean deposit(String accountNumber, BigDecimal amount);
    boolean withdraw(String accountNumber, BigDecimal amount);
    boolean transfer(String fromAccount, String toAccount, BigDecimal amount);
    String generateAccountNumber();
}

