package com.example.fintech.transactionservice.processor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.example.fintech.common.config.RabbitMQConfig;
import com.example.fintech.common.event.TransactionEvent;
import com.example.fintech.transactionservice.repository.TransactionRepository;
import com.example.fintech.common.service.EventPublisher;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;

@Component
public class TransactionEventProcessor {

    private static final Logger logger =  LoggerFactory.getLogger(TransactionEventProcessor.class);
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(TransactionEventProcessor.class);

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private EventPublisher eventPublisher;

    @RabbitListener(queues = RabbitMQConfig.TRANSACTION_QUEUE)
    public void processTransactionEvent(TransactionEvent event) {
        logger.info("processing transaction event:{}", event.getTransactionId());

        try {
            switch (event.getStatus()) {
                case "PENDING" :
                    handlePendingTransaction(event);
                case "COMPLETED":
                    handleCompletedTransaction(event);
                case "FAILED" :
                    handleFailedTransaction(event);
                    break;
                default:
                    logger.warn("unknown transaction status: {}", event.getStatus());
            }
        } catch (Exception e) {
            logger.error("Error processing transavtion: {}", event.getTransactionId());
            handleTransactionProcessingError(event, e);
        }
    }

    private void handlePendingTransaction(TransactionEvent event) {
        logger.info("handling pending transaction: {}", event.getTransactionId());

        logger.info("Transaction {} is being processed", event.getTransactionId());
    }

    private void handleCompletedTransaction(TransactionEvent event) {
        logger.info("Transaction {} completed successfully", event.getTransactionId());

        // Update any derived data or analytics
        // Send completion notifications
        publishTransactionCompletionNotification(event);
    }

    private void handleFailedTransaction(TransactionEvent event) {
        logger.error("Transaction {} failed: {}", event.getTransactionId(), event.getDescription());
    }

    private void handleTransactionProcessingError(TransactionEvent event, Exception e) {
        logger.error("critical error processing transaction {} : {}",
                event.getTransactionId(), e.getMessage());
    }

    private void publishTransactionCompletionNotification(TransactionEvent event) {
        // This could trigger notifications to users about completed transactions
        logger.info("Publishing completion notification for transaction: {}", event.getTransactionId());
    }

}
