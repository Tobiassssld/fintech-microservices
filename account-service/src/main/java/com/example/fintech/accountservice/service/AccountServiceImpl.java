package com.example.fintech.accountservice.service;

import com.example.fintech.common.entity.Account;
import com.example.fintech.common.entity.User;
import com.example.fintech.common.enums.TransactionType;
import com.example.fintech.accountservice.exception.AccountNotFoundException;
import com.example.fintech.accountservice.exception.InsufficientFundsException;
import com.example.fintech.accountservice.exception.InvalidTransactionException;
import com.example.fintech.accountservice.repository.AccountRepository;
import com.example.fintech.accountservice.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Random;

@Service
@Transactional
public class AccountServiceImpl implements AccountService{

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private UserRepository userRepository;

    //@Autowired
    //private TransactionService transactionService;

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
    public boolean deposit(String accountNumber, BigDecimal amount){
        if (amount.compareTo(BigDecimal.ZERO) <= 0){
            throw new RuntimeException("Deposit amount must be positive");
        }

        Account account = getAccountByAccountNumber(accountNumber);
        account.setBalance(account.getBalance().add(amount));
        accountRepository.save(account);

        //transactionService.createTransaction(null, account.getId(), amount,
        //        TransactionType.DEPOSIT, "Cash deposit");

        return true;
    }

    @Override
    public boolean withdraw(String accountNumber, BigDecimal amount){
        if (amount.compareTo(BigDecimal.ZERO) <= 0){
            throw new InvalidTransactionException("withdrawal amount must be positive");
        }

        Account account = getAccountByAccountNumber(accountNumber);
        if (account.getBalance().compareTo(amount) < 0){
            throw new InsufficientFundsException(
                    String.format("Insufficient funds. Available: %s, Requested: %s",
                            account.getBalance(), amount));
        }

        account.setBalance(account.getBalance().subtract(amount));
        accountRepository.save(account);

        //transactionService.createTransaction(account.getId(), null, amount,
        //       TransactionType.WITHDRAWAL, "Cash withdrawal");
        return true;
    }


    @Override
    public boolean transfer(String fromAccount, String toAccount, BigDecimal amount){
        if (amount.compareTo(BigDecimal.ZERO) <= 0){
            throw new AccountNotFoundException("Transfer amount must be positive.");
        }

        Account fromAcc = getAccountByAccountNumber(fromAccount);
        Account toAcc = getAccountByAccountNumber(toAccount);

        //atomic operation
        withdraw(fromAccount, amount);
        deposit(toAccount, amount);

        //transactionService.createTransaction(fromAcc.getId(), toAcc.getId(), amount,
        //        TransactionType.TRANSFER, "Transfer between accounts");

        return true;
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

