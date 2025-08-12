package com.example.fintech.accountservice.controller;

import com.example.fintech.common.event.NotificationEvent;
import com.example.fintech.common.event.AccountBalanceEvent;
import com.example.fintech.common.service.EventPublisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/test")
public class TestController {

    @Autowired
    private EventPublisher eventPublisher;

    @GetMapping("/send-notification")
    public String testNotification() {
        NotificationEvent event = new NotificationEvent(
                "user123",
                "Test Notification",
                "This is a test message",
                "EMAIL",
                LocalDateTime.now()
        );

        eventPublisher.publishNotificationEvent(event);
        return "Notification sent! Check RabbitMQ Management UI";
    }

    @GetMapping("/send-balance-event")
    public String testBalanceEvent() {
        AccountBalanceEvent event = new AccountBalanceEvent(
                "ACC1234567890",
                new BigDecimal("1000.00"),
                new BigDecimal("1500.00"),
                "DEPOSIT",
                LocalDateTime.now()
        );

        eventPublisher.publishAccountBalanceEvent(event);
        return "Balance event sent! Check RabbitMQ Management UI";
    }
}