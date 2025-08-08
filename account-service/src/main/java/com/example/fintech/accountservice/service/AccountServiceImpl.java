package com.example.fintech.accountservice.service;

import com.example.fintech.common.entity.Account;
import com.example.fintech.common.entity.User;
import com.example.fintech.common.exception.AccountNotFoundException;
import com.example.fintech.common.exception.InsufficientFundsException;
import com.example.fintech.common.exception.InvalidTransactionException;
import com.example.fintech.accountservice.repository.AccountRepository;
import com.example.fintech.accountservice.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
@Transactional
public class AccountServiceImpl implements AccountService{

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RestTemplate restTemplate;

    @Override
    public Account createAccount(Long userId, String accountType, String currency) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));


        if (accountRepository.findByUserId(userId).isPresent()) {
            throw new RuntimeException("user already has an account");
        }

        Account account = new Account();
        account.setUser(user);
        account.setAccountNumber(generateAccountNumber());
        account.setBalance(BigDecimal.ZERO);
        account.setAccountType(accountType);
        account.setCurrency(currency);

        return accountRepository.save(account);
    }

    @Override
    public Account getAccountByUserId(Long userId){
        return accountRepository.findByUserId(userId)
                .orElse(null);
    }

    @Override
    public  Account getAccountByAccountNumber(String accountNumber){
        return accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException("Account not found."));
    }

    @Override
    public  BigDecimal getBalance(String accountNumber){
        Account account = getAccountByAccountNumber(accountNumber);
        return account.getBalance();
    }

    @Override
    public boolean deposit(String accountNumber, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Deposit amount must be positive");
        }

        Account account = getAccountByAccountNumber(accountNumber);
        account.setBalance(account.getBalance().add(amount));
        accountRepository.save(account);

        // 创建交易记录
        createTransactionRecord(null, account.getId(), amount, "DEPOSIT", "Cash deposit");

        return true;
    }

    @Override
    public boolean withdraw(String accountNumber, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidTransactionException("withdrawal amount must be positive");
        }

        Account account = getAccountByAccountNumber(accountNumber);
        if (account.getBalance().compareTo(amount) < 0) {
            throw new InsufficientFundsException(
                    String.format("Insufficient funds. Available: %s, Requested: %s",
                            account.getBalance(), amount));
        }

        account.setBalance(account.getBalance().subtract(amount));
        accountRepository.save(account);

        // 创建交易记录
        createTransactionRecord(account.getId(), null, amount, "WITHDRAWAL", "Cash withdrawal");

        return true;
    }

    @Override
    public boolean transfer(String fromAccount, String toAccount, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new AccountNotFoundException("Transfer amount must be positive.");
        }

        Account fromAcc = getAccountByAccountNumber(fromAccount);
        Account toAcc = getAccountByAccountNumber(toAccount);

        // 原子操作
        withdraw(fromAccount, amount);
        deposit(toAccount, amount);

        // 创建转账交易记录
        createTransactionRecord(fromAcc.getId(), toAcc.getId(), amount, "TRANSFER", "Transfer between accounts");

        return true;
    }

    // 新增方法：创建交易记录
    private void createTransactionRecord(Long fromAccountId, Long toAccountId,
                                         BigDecimal amount, String type, String description) {
        try {
            System.out.println("=== Creating Transaction Record ===");
            System.out.println("From Account ID: " + fromAccountId);
            System.out.println("To Account ID: " + toAccountId);
            System.out.println("Amount: " + amount);
            System.out.println("Type: " + type);

            // 构建请求数据
            Map<String, Object> transactionData = new HashMap<>();
            transactionData.put("fromAccountId", fromAccountId);
            transactionData.put("toAccountId", toAccountId);
            transactionData.put("amount", amount);
            transactionData.put("type", type);
            transactionData.put("description", description);

            // 获取当前用户信息（从Security Context）
            String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();
            System.out.println("Current authenticated user: " + currentUser);

            // 构建请求头，传递用户信息
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-User-Id", currentUser);
            headers.set("X-User-Roles", "USER");

            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(transactionData, headers);

            // 使用服务名调用
            String transactionServiceUrl = "http://TRANSACTION-SERVICE/api/transactions/create";
            System.out.println("Calling URL: " + transactionServiceUrl);

            ResponseEntity<String> response = restTemplate.postForEntity(transactionServiceUrl, requestEntity, String.class);
            System.out.println("Transaction Service Response: " + response.getBody());

            System.out.println("Transaction record created successfully: " + type + " - " + amount);
        } catch (Exception e) {
            System.err.println("Failed to create transaction record: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public String generateAccountNumber(){
        String accountNumber;
        do {
            accountNumber = "ACC" + String.format("%010d", new Random().nextInt(1000000000));
        }while (accountRepository.existsByAccountNumber(accountNumber));
        return accountNumber;
    }


}

