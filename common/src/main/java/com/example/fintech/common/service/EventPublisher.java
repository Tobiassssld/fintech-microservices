package com.example.fintech.common.service;

import com.example.fintech.common.config.RabbitMQConfig;
import com.example.fintech.common.event.NotificationEvent;
import com.example.fintech.common.event.AccountBalanceEvent;
import com.example.fintech.common.event.TransactionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.ReturnedMessage;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.connection.CorrelationData;

import javax.annotation.PostConstruct;
import java.util.UUID;

@Service
public class EventPublisher {

    private static final Logger logger = LoggerFactory.getLogger(EventPublisher.class);
    private static final int MAX_RETRY_ATTEMPTS = 3;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @PostConstruct
    public void init() {
        rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
            if (ack) {
                logger.info("Message confirmed: {}", correlationData.getId());
            } else {
                logger.error("Message not confirmed: {}, cause: {}", correlationData.getId(), cause);
                // Handle failed message confirmation
                handleMessageConfirmationFailure(correlationData, cause);
            }
        });

        rabbitTemplate.setReturnsCallback(returned -> {
            logger.error("Message returned: {}, reply: {}",
                    returned.getMessage(), returned.getReplyText());
            // Handle returned message
            handleReturnedMessage(returned);
        });
    }

    public void publishTransactionEvent(TransactionEvent event) {
        publishEventWithRetry(() -> {
            CorrelationData correlationData = new CorrelationData(UUID.randomUUID().toString());
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.TRANSACTION_EXCHANGE,
                    RabbitMQConfig.TRANSACTION_ROUTING_KEY,
                    event,
                    correlationData
            );

            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.TRANSACTION_EXCHANGE,
                    RabbitMQConfig.TRANSACTION_ROUTING_KEY,
                    event,
                    correlationData
            );

            logger.info("Transaction event published: {}", event.getTransactionId());
        }, "Transaction event for: " + event.getTransactionId());
    }

    public void publishAccountBalanceEvent(AccountBalanceEvent event) {
        publishEventWithRetry(() -> {
            CorrelationData correlationData = new CorrelationData(UUID.randomUUID().toString());

            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.ACCOUNT_EXCHANGE,
                    RabbitMQConfig.BALANCE_ROUTING_KEY,
                    event,
                    correlationData
            );

            logger.info("Balance event published for account: {}", event.getAccountNumber());
        }, "Balance event for account: " + event.getAccountNumber());
    }

    public void publishNotificationEvent(NotificationEvent event) {
        publishEventWithRetry(() -> {
            CorrelationData correlationData = new CorrelationData(UUID.randomUUID().toString());

            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.NOTIFICATION_EXCHANGE,
                    RabbitMQConfig.NOTIFICATION_ROUTING_KEY,
                    event,
                    correlationData
            );

            logger.info("Notification event published for user: {}", event.getUserId());
        }, "Notification event for user: " + event.getUserId());
    }

    private void publishEventWithRetry(Runnable publishAction, String eventDescription) {
        int attempts = 0;
        Exception lastException = null;

        while (attempts < MAX_RETRY_ATTEMPTS) {
            try {
                publishAction.run();
                return; // Success
            } catch (Exception e) {
                lastException = e;
                attempts++;
                logger.warn("Failed to publish event (attempt {}/{}): {}",
                        attempts, MAX_RETRY_ATTEMPTS, eventDescription);

                if (attempts < MAX_RETRY_ATTEMPTS) {
                    try {
                        Thread.sleep(1000 * attempts); // Exponential backoff
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("Event publishing interrupted", ie);
                    }
                }
            }
        }

        logger.error("Failed to publish event after {} attempts: {}", MAX_RETRY_ATTEMPTS, eventDescription);
        throw new RuntimeException("Event publishing failed after retries", lastException);
    }

    private void handleMessageConfirmationFailure(CorrelationData correlationData, String cause) {
        // Could implement dead letter queue or retry logic here
        logger.error("Implementing failure handling for message: {}", correlationData.getId());
    }

    private void handleReturnedMessage(ReturnedMessage returned) {
        // Could implement message reprocessing or dead letter handling
        logger.error("Implementing returned message handling");
    }
}