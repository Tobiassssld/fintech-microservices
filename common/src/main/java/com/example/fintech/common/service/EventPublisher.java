package com.example.fintech.common.service;

import com.example.fintech.common.config.RabbitMQConfig;
import com.example.fintech.common.event.NotificationEvent;
import com.example.fintech.common.event.AccountBalanceEvent;
import com.example.fintech.common.event.TransactionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

public class EventPublisher {

    private static final Logger logger = LoggerFactory.getLogger(EventPublisher.class);

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void publishTransactionEvent(TransactionEvent event) {
        try {
            logger.info("publishing transaction event : {}", event.getTransactionId());
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.TRANSACTION_EXCHANGE,
                    RabbitMQConfig.TRANSACTION_ROUTING_KEY,
                    event
            );
            logger.info("Transaction event published successfully");
        } catch (Exception e) {
            logger.error("failed to publish transaction event", e);
        }
    }

    public void publishAccountBalanceEvent(AccountBalanceEvent event) {
        try {
            logger.info("publishing balance event for account : {}", event.getAccountNumber());
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.ACCOUNT_EXCHANGE,
                    RabbitMQConfig.BALANCE_ROUTING_KEY,
                    event
            );
            logger.info("Balance event published successfully");
        } catch (Exception e) {
            logger.error("failed to publish balance event", e);
        }
    }

    public void publishNotificationEvent(NotificationEvent event) {
        try {
            logger.info("Publishing notification event for user: {}", event.getUserId());
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.NOTIFICATION_EXCHANGE,
                    RabbitMQConfig.NOTIFICATION_ROUTING_KEY,
                    event
            );
            logger.info("Notification event published successfully");
        } catch (Exception e) {
            logger.error("Failed to publish notification event", e);
        }
    }
}
