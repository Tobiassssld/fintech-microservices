package com.example.fintech.accountservice.service;

import com.example.fintech.common.entity.Account;
import com.example.fintech.common.event.AccountBalanceEvent;
import com.example.fintech.common.event.NotificationEvent;
import com.example.fintech.common.exception.AccountNotFoundException;
import com.example.fintech.common.exception.InsufficientFundsException;
import com.example.fintech.common.exception.InvalidTransactionException;
import com.example.fintech.common.service.EventPublisher;
import com.example.fintech.accountservice.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Service
@Transactional
public class AccountServiceImpl implements AccountService {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private EventPublisher eventPublisher;

    @Override
    public Account createAccount(Long userId, String username, String accountType, String currency) {
        // 检查是否已存在相同类型的账户
        List<Account> existingAccounts = accountRepository.findAllByUserId(userId);

        boolean hasSameType = existingAccounts.stream()
                .anyMatch(acc -> acc.getAccountType().equals(accountType)
                        && acc.getCurrency().equals(currency));

        if (hasSameType) {
            throw new RuntimeException("User already has a " + accountType + " account in " + currency);
        }

        Account account = new Account();
        account.setUserId(userId);
        account.setUsername(username);
        account.setAccountNumber(generateAccountNumber());
        account.setBalance(BigDecimal.ZERO);
        account.setAccountType(accountType);
        account.setCurrency(currency);
        account.setStatus("ACTIVE");

        Account savedAccount = accountRepository.save(account);

        // 发布账户创建通知
        publishAccountCreatedNotification(userId, savedAccount);

        return savedAccount;
    }

    @Override
    public Account getAccountByUserId(Long userId) {
        // 返回用户的第一个账户（向后兼容）
        List<Account> accounts = accountRepository.findAllByUserId(userId);
        return accounts.isEmpty() ? null : accounts.get(0);
    }

    @Override
    public Account getAccountByAccountNumber(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException("Account not found: " + accountNumber));
    }

    @Override
    public List<Account> getAccountsByUserId(Long userId) {
        return accountRepository.findAllByUserId(userId);
    }

    @Override
    public BigDecimal getBalance(String accountNumber) {
        Account account = getAccountByAccountNumber(accountNumber);
        return account.getBalance();
    }

    @Override
    public boolean deposit(String accountNumber, BigDecimal amount, Long userId) {
        validateAmount(amount);

        Account account = getAccountByAccountNumber(accountNumber);
        validateOwnership(account, userId);
        validateAccountStatus(account, "deposit");

        BigDecimal oldBalance = account.getBalance();
        BigDecimal newBalance = oldBalance.add(amount);

        account.setBalance(newBalance);
        accountRepository.save(account);

        publishBalanceChangeEvent(account, oldBalance, newBalance, "DEPOSIT");

        return true;
    }

    @Override
    public boolean withdraw(String accountNumber, BigDecimal amount, Long userId) {
        validateAmount(amount);

        Account account = getAccountByAccountNumber(accountNumber);
        validateOwnership(account, userId);
        validateAccountStatus(account, "withdraw");

        BigDecimal oldBalance = account.getBalance();
        if (oldBalance.compareTo(amount) < 0) {
            throw new InsufficientFundsException("Insufficient funds");
        }

        BigDecimal newBalance = oldBalance.subtract(amount);
        account.setBalance(newBalance);
        accountRepository.save(account);

        publishBalanceChangeEvent(account, oldBalance, newBalance, "WITHDRAWAL");

        return true;
    }

    @Override
    public boolean transfer(String fromAccountNumber, String toAccountNumber,
                            BigDecimal amount, Long userId) {
        validateAmount(amount);

        Account fromAccount = getAccountByAccountNumber(fromAccountNumber);
        Account toAccount = getAccountByAccountNumber(toAccountNumber);

        validateOwnership(fromAccount, userId);
        validateAccountStatus(fromAccount, "transfer");
        validateAccountStatus(toAccount, "receive");

        if (fromAccount.getBalance().compareTo(amount) < 0) {
            throw new InsufficientFundsException("Insufficient funds for transfer");
        }

        // 执行转账
        BigDecimal fromOldBalance = fromAccount.getBalance();
        BigDecimal toOldBalance = toAccount.getBalance();

        fromAccount.setBalance(fromOldBalance.subtract(amount));
        toAccount.setBalance(toOldBalance.add(amount));

        accountRepository.save(fromAccount);
        accountRepository.save(toAccount);

        // 发布事件
        publishBalanceChangeEvent(fromAccount, fromOldBalance, fromAccount.getBalance(), "TRANSFER_OUT");
        publishBalanceChangeEvent(toAccount, toOldBalance, toAccount.getBalance(), "TRANSFER_IN");

        return true;
    }

    @Override
    public String generateAccountNumber() {
        String accountNumber;
        do {
            accountNumber = "ACC" + String.format("%010d", new Random().nextInt(1000000000));
        } while (accountRepository.existsByAccountNumber(accountNumber));
        return accountNumber;
    }

    // 私有辅助方法
    private void validateAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidTransactionException("Amount must be positive");
        }
    }

    private void validateOwnership(Account account, Long userId) {
        if (!account.getUserId().equals(userId)) {
            throw new SecurityException("Access denied: You don't own this account");
        }
    }

    private void validateAccountStatus(Account account, String operation) {
        if (!"ACTIVE".equals(account.getStatus())) {
            throw new InvalidTransactionException("Account is not active for " + operation);
        }
    }

    private void publishBalanceChangeEvent(Account account, BigDecimal oldBalance,
                                           BigDecimal newBalance, String operation) {
        AccountBalanceEvent event = new AccountBalanceEvent(
                account.getAccountNumber(),
                oldBalance,
                newBalance,
                operation,
                LocalDateTime.now()
        );
        eventPublisher.publishAccountBalanceEvent(event);
    }

    private void publishAccountCreatedNotification(Long userId, Account account) {
        NotificationEvent event = new NotificationEvent(
                userId.toString(),
                "Account Created",
                "Your " + account.getAccountType() + " account has been created. Number: " + account.getAccountNumber(),
                "EMAIL",
                LocalDateTime.now()
        );
        eventPublisher.publishNotificationEvent(event);
    }
}