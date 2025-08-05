package com.example.fintech.transactionservice.controller;

import com.example.fintech.common.entity.Transaction;
import com.example.fintech.common.enums.TransactionType;
import com.example.fintech.transactionservice.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    // 注意：移除了用户认证逻辑，改为直接接收accountId参数

    @GetMapping("/history")
    public ResponseEntity<?> getTransactionHistory(
            @RequestParam Long accountId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Page<Transaction> transactions = transactionService.getTransactionsByAccountId(
                    accountId, PageRequest.of(page, size));
            return ResponseEntity.ok(transactions);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/history/type/{type}")
    public ResponseEntity<?> getTransactionsByType(
            @RequestParam Long accountId,
            @PathVariable TransactionType type) {
        try {
            List<Transaction> transactions = transactionService.getTransactionsByAccountAndType(
                    accountId, type);
            return ResponseEntity.ok(transactions);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/history/date-range")
    public ResponseEntity<?> getTransactionsByDateRange(
            @RequestParam Long accountId,
            @RequestParam String startDate,
            @RequestParam String endDate) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDateTime start = LocalDate.parse(startDate, formatter).atStartOfDay();
            LocalDateTime end = LocalDate.parse(endDate, formatter).atTime(23, 59, 59);

            List<Transaction> transactions = transactionService.getTransactionsByDateRange(
                    accountId, start, end);
            return ResponseEntity.ok(transactions);
        } catch (Exception e) {
            System.out.println("Date range query error: " + e.getMessage());
            return ResponseEntity.badRequest().body("Date format error. Use yyyy-MM-dd format. Error: " + e.getMessage());
        }
    }

    @GetMapping("/{transactionId}")
    public ResponseEntity<?> getTransactionDetails(@PathVariable String transactionId) {
        try {
            System.out.println("Looking for transaction: " + transactionId);
            Transaction transaction = transactionService.getTransactionById(transactionId);
            return ResponseEntity.ok(transaction);
        } catch (Exception e) {
            System.out.println("Transaction not found: " + transactionId + ", Error: " + e.getMessage());
            return ResponseEntity.badRequest().body("Transaction not found: " + transactionId);
        }
    }
}
