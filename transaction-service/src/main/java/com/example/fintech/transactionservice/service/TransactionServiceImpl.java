package com.example.fintech.transactionservice.service;

import com.example.fintech.common.entity.Account;
import com.example.fintech.common.entity.Transaction;
import com.example.fintech.common.enums.TransactionStatus;
import com.example.fintech.common.enums.TransactionType;
import com.example.fintech.common.event.TransactionEvent;
import com.example.fintech.common.exception.AccountNotFoundException;
import com.example.fintech.common.exception.InvalidTransactionException;
import com.example.fintech.common.service.EventPublisher;
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

    @Autowired
    private EventPublisher eventPublisher;

    public Transaction createTransaction(Long fromAccountId, Long toAccountId,
                                         BigDecimal amount, TransactionType type, String description) {

        // Pre-transaction validation
        validateTransactionRequest(fromAccountId, toAccountId, amount, type);

        Account fromAccount = null;
        Account toAccount = null;

        if (fromAccountId != null) {
            fromAccount = accountRepository.findById(fromAccountId)
                    .orElseThrow(() -> new AccountNotFoundException("From account not found: " + fromAccountId));
        }

        if (toAccountId != null) {
            toAccount = accountRepository.findById(toAccountId)
                    .orElseThrow(() -> new AccountNotFoundException("To account not found: " + toAccountId));
        }

        // Create transaction with PENDING status initially
        Transaction transaction = createPendingTransaction(fromAccount, toAccount, amount, type, description);

        try {
            // Save transaction first
            Transaction savedTransaction = transactionRepository.save(transaction);

            // Publish transaction event for processing
            publishTransactionCreatedEvent(savedTransaction);

            // Update status to COMPLETED (in real-world, this would be async)
            savedTransaction.setStatus(TransactionStatus.COMPLETED);
            return transactionRepository.save(savedTransaction);

        } catch (Exception e) {
            transaction.setStatus(TransactionStatus.FAILED);
            transactionRepository.save(transaction);
            publishTransactionFailedEvent(transaction, e.getMessage());
            throw new InvalidTransactionException("Transaction processing failed: " + e.getMessage());
        }
    }

    private void validateTransactionRequest(Long fromAccountId, Long toAccountId,
                                            BigDecimal amount, TransactionType type) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidTransactionException("Transaction amount must be positive");
        }

        if (type == TransactionType.TRANSFER && (fromAccountId == null || toAccountId == null)) {
            throw new InvalidTransactionException("Transfer requires both from and to accounts");
        }

        if (type == TransactionType.DEPOSIT && toAccountId == null) {
            throw new InvalidTransactionException("Deposit requires to account");
        }

        if (type == TransactionType.WITHDRAWAL && fromAccountId == null) {
            throw new InvalidTransactionException("Withdrawal requires from account");
        }
    }

    private Transaction createPendingTransaction(Account fromAccount, Account toAccount,
                                                 BigDecimal amount, TransactionType type, String description) {
        Transaction transaction = new Transaction();
        transaction.setTransactionId(generateTransactionId());
        transaction.setFromAccount(fromAccount);
        transaction.setToAccount(toAccount);
        transaction.setAmount(amount);
        transaction.setType(type);
        transaction.setStatus(TransactionStatus.PENDING);
        transaction.setDescription(description);

        // Record balance snapshots
        if (fromAccount != null) {
            transaction.setFromAccountBalanceBefore(fromAccount.getBalance());
        }
        if (toAccount != null) {
            transaction.setToAccountBalanceBefore(toAccount.getBalance());
        }

        return transaction;
    }

    private void publishTransactionCreatedEvent(Transaction transaction) {
        TransactionEvent event = new TransactionEvent();
        event.setTransactionId(transaction.getTransactionId());
        event.setFromAccount(transaction.getFromAccount() != null ?
                transaction.getFromAccount().getAccountNumber() : null);
        event.setToAccount(transaction.getToAccount() != null ?
                transaction.getToAccount().getAccountNumber() : null);
        event.setAmount(transaction.getAmount());
        event.setTransactionType(transaction.getType().toString());
        event.setStatus(transaction.getStatus().toString());
        event.setTimestamp(LocalDateTime.now());
        event.setDescription(transaction.getDescription());

        eventPublisher.publishTransactionEvent(event);
    }

    private void publishTransactionFailedEvent(Transaction transaction, String errorMessage) {
        TransactionEvent event = new TransactionEvent();
        event.setTransactionId(transaction.getTransactionId());
        event.setTransactionType(transaction.getType().toString());
        event.setStatus("FAILED");
        event.setTimestamp(LocalDateTime.now());
        event.setDescription("Transaction failed: " + errorMessage);

        eventPublisher.publishTransactionEvent(event);
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

