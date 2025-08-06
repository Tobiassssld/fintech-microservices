package com.example.fintech.transactionservice.service;

import com.example.fintech.common.entity.Account;
import com.example.fintech.common.entity.Transaction;
import com.example.fintech.common.enums.TransactionStatus;
import com.example.fintech.common.enums.TransactionType;
import com.example.fintech.transactionservice.repository.AccountRepository;
import com.example.fintech.transactionservice.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;


@Service
@Transactional
public class TransactionServiceImpl implements TransactionService{

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Override
    public Transaction createTransaction(Long fromAccountId, Long toAccountId,
                                         BigDecimal amount, TransactionType type, String description) {

        Account fromAccount = null;
        Account toAccount = null;

        // 修复：正确处理Optional
        if (fromAccountId != null) {
            fromAccount = accountRepository.findById(fromAccountId).orElse(null);
        }

        if (toAccountId != null) {
            toAccount = accountRepository.findById(toAccountId).orElse(null);
        }

        Transaction transaction = new Transaction();
        transaction.setTransactionId(generateTransactionId());
        transaction.setFromAccount(fromAccount);
        transaction.setToAccount(toAccount);
        transaction.setAmount(amount);
        transaction.setType(type);
        transaction.setStatus(TransactionStatus.COMPLETED);
        transaction.setDescription(description);

        // 记录余额快照
        if (fromAccount != null) {
            transaction.setFromAccountBalanceBefore(fromAccount.getBalance());
            // 计算操作后余额
            if (type == TransactionType.WITHDRAWAL || type == TransactionType.TRANSFER) {
                transaction.setFromAccountBalanceAfter(fromAccount.getBalance().subtract(amount));
            }
        }

        if (toAccount != null) {
            transaction.setToAccountBalanceBefore(toAccount.getBalance());
            // 计算操作后余额
            if (type == TransactionType.DEPOSIT || type == TransactionType.TRANSFER) {
                transaction.setToAccountBalanceAfter(toAccount.getBalance().add(amount));
            }
        }

        Transaction savedTransaction = transactionRepository.save(transaction);
        return savedTransaction;
    }

    @Override
    public Transaction getTransactionById(String transactionId) {
        return transactionRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new RuntimeException("Transaction not found."));
    }

    @Override
    public Page<Transaction> getTransactionsByAccountId(Long accountId, Pageable pageable){
        return transactionRepository.findByAccountId(accountId, pageable);
    }

    @Override
    public List<Transaction> getTransactionsByAccountAndType(Long accountId, TransactionType type) {
        return transactionRepository.findByAccountIdAndType(accountId, type);
    }

    @Override
    public List<Transaction> getTransactionsByDateRange(Long accountId, LocalDateTime startDate, LocalDateTime endDate){
        return transactionRepository.findByAccountIdAndDateRange(accountId, startDate, endDate);
    }

    @Override
    public String generateTransactionId(){
        String transactionId;
        do {
            String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            String randomPart = String.format("%06d", new Random().nextInt(1000000));
            transactionId = "TXN" + datePart + randomPart;
        } while (transactionRepository.existsByTransactionId(transactionId));
        return transactionId;
    }
}

