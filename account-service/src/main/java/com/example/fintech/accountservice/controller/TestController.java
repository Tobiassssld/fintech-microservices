package com.example.fintech.accountservice.controller;

import com.example.fintech.common.event.NotificationEvent;
import com.example.fintech.common.event.AccountBalanceEvent;
import com.example.fintech.common.service.EventPublisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
public class TestController {

    @Autowired
    private EventPublisher eventPublisher;

    @GetMapping("/send-notification")
    public ResponseEntity<?> testNotification() {
        NotificationEvent event = new NotificationEvent(
                "user123",
                "Test Account Notification",
                "This is a test message from Account Service",
                "EMAIL",
                LocalDateTime.now()
        );

        eventPublisher.publishNotificationEvent(event);
        return ResponseEntity.ok(Map.of(
                "message", "Notification sent from Account Service!",
                "timestamp", LocalDateTime.now()
        ));
    }
    @GetMapping("/send-balance-event")
    public ResponseEntity<?> testBalanceEvent() {
        AccountBalanceEvent event = new AccountBalanceEvent(
                "ACC1234567890",
                new BigDecimal("1000.00"),
                new BigDecimal("1500.00"),
                "DEPOSIT",
                LocalDateTime.now()
        );

        eventPublisher.publishAccountBalanceEvent(event);
        return ResponseEntity.ok(Map.of(
                "message", "Balance event sent from Account Service!",
                "timestamp", LocalDateTime.now()
        ));
    }
}