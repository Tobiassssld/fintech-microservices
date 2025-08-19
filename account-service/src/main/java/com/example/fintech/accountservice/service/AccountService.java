package com.example.fintech.accountservice.service;

import com.example.fintech.common.entity.Account;
import java.math.BigDecimal;
import java.util.List;

public interface AccountService {
    Account createAccount(Long userId, String accountType, String currency);
    Account getAccountByUserId(Long userId);
    Account getAccountByAccountNumber(String accountNumber);
    List<Account> getAccountsByUserId(Long userId);  // 添加这个
    BigDecimal getBalance(String accountNumber);
    boolean deposit(String accountNumber, BigDecimal amount, Long userId);  // 添加userId参数
    boolean withdraw(String accountNumber, BigDecimal amount, Long userId);  // 添加userId参数
    boolean transfer(String fromAccount, String toAccount, BigDecimal amount, Long userId);  // 添加userId参数
    String generateAccountNumber();
}