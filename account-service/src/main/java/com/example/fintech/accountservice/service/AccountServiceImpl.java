package com.example.fintech.accountservice.service;

import com.example.fintech.common.entity.Account;
import com.example.fintech.common.entity.User;
import com.example.fintech.common.event.AccountBalanceEvent;
import com.example.fintech.common.event.NotificationEvent;
import com.example.fintech.common.exception.AccountNotFoundException;
import com.example.fintech.common.exception.InsufficientFundsException;
import com.example.fintech.common.exception.InvalidTransactionException;
import com.example.fintech.common.service.EventPublisher;
import com.example.fintech.accountservice.repository.AccountRepository;
import com.example.fintech.accountservice.repository.UserRepository;
import com.example.fintech.accountservice.validator.AccountOwnershipValidator;
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
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Service
@Transactional
public class AccountServiceImpl implements AccountService {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private EventPublisher eventPublisher;

    @Autowired
    private AccountOwnershipValidator ownershipValidator;

    @Override
    public Account createAccount(Long userId, String accountType, String currency) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 允许用户拥有多个账户
        List<Account> existingAccounts = accountRepository.findAllByUserId(userId);

        // 检查是否已有相同类型和货币的账户
        boolean hasSameType = existingAccounts.stream()
                .anyMatch(acc -> acc.getAccountType().equals(accountType)
                        && acc.getCurrency().equals(currency));

        if (hasSameType) {
            throw new RuntimeException("User already has a " + accountType + " account in " + currency);
        }

        Account account = new Account();
        account.setUser(user);
        account.setAccountNumber(generateAccountNumber());
        account.setBalance(BigDecimal.ZERO);
        account.setAccountType(accountType);
        account.setCurrency(currency);
        account.setStatus("ACTIVE");

        Account savedAccount = accountRepository.save(account);

        // 发送通知
        NotificationEvent notificationEvent = new NotificationEvent(
                userId.toString(),
                "Account Created",
                "Your " + accountType + " account has been created successfully. Account number: " + savedAccount.getAccountNumber(),
                "EMAIL",
                LocalDateTime.now()
        );
        eventPublisher.publishNotificationEvent(notificationEvent);

        return savedAccount;
    }

    @Override
    public List<Account> getAccountsByUserId(Long userId) {
        return accountRepository.findAllByUserId(userId);
    }

    @Override
    public Account getAccountByUserId(Long userId) {
        return accountRepository.findByUserId(userId).orElse(null);
    }

    @Override
    public Account getAccountByAccountNumber(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException("Account not found: " + accountNumber));
    }

    @Override
    public BigDecimal getBalance(String accountNumber) {
        Account account = getAccountByAccountNumber(accountNumber);
        return account.getBalance();
    }

    @Override
    public boolean deposit(String accountNumber, BigDecimal amount, Long userId) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidTransactionException("Deposit amount must be positive");
        }

        Account account = getAccountByAccountNumber(accountNumber);

        // 验证账户所有权
        ownershipValidator.validateOwnership(account, userId);

        // 检查账户状态
        if (!"ACTIVE".equals(account.getStatus())) {
            throw new InvalidTransactionException("Account is not active");
        }

        BigDecimal oldBalance = account.getBalance();
        BigDecimal newBalance = oldBalance.add(amount);

        account.setBalance(newBalance);
        accountRepository.save(account);

        // 发布余额变更事件
        AccountBalanceEvent balanceEvent = new AccountBalanceEvent(
                accountNumber,
                oldBalance,
                newBalance,
                "DEPOSIT",
                LocalDateTime.now()
        );
        eventPublisher.publishAccountBalanceEvent(balanceEvent);

        // 发送通知
        NotificationEvent notificationEvent = new NotificationEvent(
                userId.toString(),
                "Deposit Successful",
                String.format("Deposit of %s %s completed. New Balance: %s",
                        amount, account.getCurrency(), newBalance),
                "EMAIL",
                LocalDateTime.now()
        );
        eventPublisher.publishNotificationEvent(notificationEvent);

        // 创建交易记录
        createTransactionRecord(null, account.getId(), amount, "DEPOSIT", "Cash deposit");

        return true;
    }

    @Override
    public boolean withdraw(String accountNumber, BigDecimal amount, Long userId) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidTransactionException("Withdrawal amount must be positive");
        }

        Account account = getAccountByAccountNumber(accountNumber);

        // 验证账户所有权
        ownershipValidator.validateOwnership(account, userId);

        // 检查账户状态
        if (!"ACTIVE".equals(account.getStatus())) {
            throw new InvalidTransactionException("Account is not active");
        }

        BigDecimal oldBalance = account.getBalance();

        if (oldBalance.compareTo(amount) < 0) {
            throw new InsufficientFundsException(
                    String.format("Insufficient funds. Available: %s, Requested: %s",
                            oldBalance, amount));
        }

        BigDecimal newBalance = oldBalance.subtract(amount);
        account.setBalance(newBalance);
        accountRepository.save(account);

        // 发布余额变更事件
        AccountBalanceEvent balanceEvent = new AccountBalanceEvent(
                accountNumber,
                oldBalance,
                newBalance,
                "WITHDRAWAL",
                LocalDateTime.now()
        );
        eventPublisher.publishAccountBalanceEvent(balanceEvent);

        // 发送通知
        NotificationEvent notificationEvent = new NotificationEvent(
                userId.toString(),
                "Withdrawal Successful",
                String.format("Withdrawal of %s %s completed. New Balance: %s",
                        amount, account.getCurrency(), newBalance),
                "EMAIL",
                LocalDateTime.now()
        );
        eventPublisher.publishNotificationEvent(notificationEvent);

        // 创建交易记录
        createTransactionRecord(account.getId(), null, amount, "WITHDRAWAL", "Cash withdrawal");

        return true;
    }

    @Override
    public boolean transfer(String fromAccountNumber, String toAccountNumber, BigDecimal amount, Long userId) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidTransactionException("Transfer amount must be positive");
        }

        Account fromAccount = getAccountByAccountNumber(fromAccountNumber);
        Account toAccount = getAccountByAccountNumber(toAccountNumber);

        // 验证源账户所有权
        ownershipValidator.validateOwnership(fromAccount, userId);

        // 检查账户状态
        if (!"ACTIVE".equals(fromAccount.getStatus())) {
            throw new InvalidTransactionException("Source account is not active");
        }

        if (!"ACTIVE".equals(toAccount.getStatus())) {
            throw new InvalidTransactionException("Destination account is not active");
        }

        // 检查余额
        if (fromAccount.getBalance().compareTo(amount) < 0) {
            throw new InsufficientFundsException("Insufficient funds for transfer");
        }

        BigDecimal fromOldBalance = fromAccount.getBalance();
        BigDecimal toOldBalance = toAccount.getBalance();

        // 执行转账
        fromAccount.setBalance(fromOldBalance.subtract(amount));
        toAccount.setBalance(toOldBalance.add(amount));

        accountRepository.save(fromAccount);
        accountRepository.save(toAccount);

        // 发布余额变更事件 - 源账户
        AccountBalanceEvent fromBalanceEvent = new AccountBalanceEvent(
                fromAccountNumber,
                fromOldBalance,
                fromAccount.getBalance(),
                "TRANSFER_OUT",
                LocalDateTime.now()
        );
        eventPublisher.publishAccountBalanceEvent(fromBalanceEvent);

        // 发布余额变更事件 - 目标账户
        AccountBalanceEvent toBalanceEvent = new AccountBalanceEvent(
                toAccountNumber,
                toOldBalance,
                toAccount.getBalance(),
                "TRANSFER_IN",
                LocalDateTime.now()
        );
        eventPublisher.publishAccountBalanceEvent(toBalanceEvent);

        // 发送通知给转出方
        NotificationEvent fromNotification = new NotificationEvent(
                fromAccount.getUser().getId().toString(),
                "Transfer Sent",
                String.format("Transfer of %s %s sent to %s. New Balance: %s",
                        amount, fromAccount.getCurrency(), toAccountNumber, fromAccount.getBalance()),
                "EMAIL",
                LocalDateTime.now()
        );
        eventPublisher.publishNotificationEvent(fromNotification);

        // 发送通知给接收方
        NotificationEvent toNotification = new NotificationEvent(
                toAccount.getUser().getId().toString(),
                "Transfer Received",
                String.format("Received %s %s from %s. New Balance: %s",
                        amount, toAccount.getCurrency(), fromAccountNumber, toAccount.getBalance()),
                "EMAIL",
                LocalDateTime.now()
        );
        eventPublisher.publishNotificationEvent(toNotification);

        // 创建交易记录
        createTransactionRecord(fromAccount.getId(), toAccount.getId(), amount, "TRANSFER",
                "Transfer from " + fromAccountNumber + " to " + toAccountNumber);

        return true;
    }

    private void createTransactionRecord(Long fromAccountId, Long toAccountId,
                                         BigDecimal amount, String type, String description) {
        try {
            Map<String, Object> transactionData = new HashMap<>();
            transactionData.put("fromAccountId", fromAccountId);
            transactionData.put("toAccountId", toAccountId);
            transactionData.put("amount", amount);
            transactionData.put("type", type);
            transactionData.put("description", description);

            String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-User-Id", currentUser);
            headers.set("X-User-Roles", "USER");

            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(transactionData, headers);

            String transactionServiceUrl = "http://TRANSACTION-SERVICE/api/transactions/create";
            ResponseEntity<String> response = restTemplate.postForEntity(transactionServiceUrl, requestEntity, String.class);

            System.out.println("Transaction record created successfully: " + type + " - " + amount);
        } catch (Exception e) {
            System.err.println("Failed to create transaction record: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public String generateAccountNumber() {
        String accountNumber;
        do {
            accountNumber = "ACC" + String.format("%010d", new Random().nextInt(1000000000));
        } while (accountRepository.existsByAccountNumber(accountNumber));
        return accountNumber;
    }
}