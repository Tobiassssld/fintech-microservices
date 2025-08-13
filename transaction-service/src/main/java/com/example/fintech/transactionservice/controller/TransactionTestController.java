package com.example.fintech.transactionservice.controller;

// transaction-service/src/main/java/com/example/fintech/transactionservice/controller/TransactionTestController.java

import com.example.fintech.common.entity.Transaction;
import com.example.fintech.common.enums.TransactionType;
import com.example.fintech.common.event.TransactionEvent;
import com.example.fintech.common.service.EventPublisher;
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
@RequestMapping("/api/test/transactions")
public class TransactionTestController {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private EventPublisher eventPublisher;

    @PostMapping("/create-test-transaction")
    public ResponseEntity<?> createTestTransaction() {
        try {
            // Create a test transaction to verify event flow
            Transaction transaction = transactionService.createTransaction(
                    1L, 2L,
                    new BigDecimal("100.00"),
                    TransactionType.TRANSFER,
                    "Test transaction for event verification"
            );

            return ResponseEntity.ok(Map.of(
                    "message", "Test transaction created",
                    "transactionId", transaction.getTransactionId(),
                    "status", transaction.getStatus()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Failed to create test transaction",
                    "message", e.getMessage()
            ));
        }
    }

    @PostMapping("/publish-test-event")
    public ResponseEntity<?> publishTestEvent() {
        TransactionEvent event = new TransactionEvent();
        event.setTransactionId("TEST-" + System.currentTimeMillis());
        event.setAmount(new BigDecimal("50.00"));
        event.setTransactionType("TEST");
        event.setStatus("PENDING");
        event.setTimestamp(LocalDateTime.now());
        event.setDescription("Test event for verification");

        eventPublisher.publishTransactionEvent(event);

        return ResponseEntity.ok(Map.of(
                "message", "Test event published",
                "eventId", event.getTransactionId()
        ));
    }
}
