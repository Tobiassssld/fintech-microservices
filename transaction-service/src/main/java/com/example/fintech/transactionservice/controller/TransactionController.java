package com.example.fintech.transactionservice.controller;

import com.example.fintech.common.entity.Transaction;
import com.example.fintech.common.enums.TransactionType;
import com.example.fintech.transactionservice.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    @PostMapping("/create")
    public ResponseEntity<?> createTransaction(@RequestBody Map<String, Object> transactionData) {
        try {
            Long fromAccountId = transactionData.get("fromAccountId") != null ?
                    Long.valueOf(transactionData.get("fromAccountId").toString()) : null;
            Long toAccountId = transactionData.get("toAccountId") != null ?
                    Long.valueOf(transactionData.get("toAccountId").toString()) : null;
            BigDecimal amount = new BigDecimal(transactionData.get("amount").toString());
            String typeStr = transactionData.get("type").toString();
            String description = transactionData.get("description").toString();

            TransactionType type = TransactionType.valueOf(typeStr);

            System.out.println("Creating transaction: " + type + " - " + amount);
            Transaction transaction = transactionService.createTransaction(
                    fromAccountId, toAccountId, amount, type, description);

            return ResponseEntity.ok(transaction);
        } catch (Exception e) {
            System.err.println("Error creating transaction: " + e.getMessage());
            return ResponseEntity.badRequest().body("Error creating transaction: " + e.getMessage());
        }
    }

    @GetMapping("/history")
    public ResponseEntity<?> getTransactionHistory(
            @RequestParam("accountId") Long accountId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        try {
            System.out.println("Transaction Service - Getting history for accountId: " + accountId + ", page: " + page + ", size: " + size);
            Page<Transaction> transactions = transactionService.getTransactionsByAccountId(
                    accountId, PageRequest.of(page, size));
            return ResponseEntity.ok(transactions);
        } catch (Exception e) {
            System.err.println("Error getting transaction history: " + e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/history/type/{type}")
    public ResponseEntity<?> getTransactionsByType(
            @RequestParam("accountId") Long accountId,
            @PathVariable("type") TransactionType type) {
        try {
            System.out.println("Transaction Service - Getting transactions by type: " + type + " for accountId: " + accountId);
            List<Transaction> transactions = transactionService.getTransactionsByAccountAndType(
                    accountId, type);
            return ResponseEntity.ok(transactions);
        } catch (Exception e) {
            System.err.println("Error getting transactions by type: " + e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/history/date-range")
    public ResponseEntity<?> getTransactionsByDateRange(
            @RequestParam("accountId") Long accountId,
            @RequestParam("startDate") String startDate,
            @RequestParam("endDate") String endDate) {
        try {
            System.out.println("Transaction Service - Getting transactions for date range: " + startDate + " to " + endDate);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDateTime start = LocalDate.parse(startDate, formatter).atStartOfDay();
            LocalDateTime end = LocalDate.parse(endDate, formatter).atTime(23, 59, 59);

            List<Transaction> transactions = transactionService.getTransactionsByDateRange(
                    accountId, start, end);
            return ResponseEntity.ok(transactions);
        } catch (Exception e) {
            System.err.println("Date range query error: " + e.getMessage());
            return ResponseEntity.badRequest().body("Date format error. Use yyyy-MM-dd format. Error: " + e.getMessage());
        }
    }

    @GetMapping("/{transactionId}")
    public ResponseEntity<?> getTransactionDetails(@PathVariable("transactionId") String transactionId) {
        try {
            System.out.println("Transaction Service - Looking for transaction: " + transactionId);
            Transaction transaction = transactionService.getTransactionById(transactionId);
            return ResponseEntity.ok(transaction);
        } catch (Exception e) {
            System.err.println("Transaction not found: " + transactionId + ", Error: " + e.getMessage());
            return ResponseEntity.badRequest().body("Transaction not found: " + transactionId);
        }
    }
}