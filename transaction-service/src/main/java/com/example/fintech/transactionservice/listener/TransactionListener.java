package com.example.fintech.transactionservice.listener;

import com.example.fintech.common.config.RabbitMQConfig;
import com.example.fintech.common.event.AccountBalanceEvent;
import com.example.fintech.common.event.TransactionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class TransactionListener {

    private static final Logger logger = LoggerFactory.getLogger(TransactionListener.class);

    @RabbitListener(queues = RabbitMQConfig.TRANSACTION_QUEUE)
    public void handleTransactionEvent(TransactionEvent event) {
        logger.info("=== Processing Transaction Event ===");
        logger.info("Transaction ID: {}", event.getTransactionId());
        logger.info("From Account: {}", event.getFromAccount());
        logger.info("To Account: {}", event.getToAccount());
        logger.info("Amount: {}", event.getAmount());
        logger.info("Type: {}", event.getTransactionType());
        logger.info("Status: {}", event.getStatus());

        // 这里可以添加事务后处理逻辑
        // 比如风控检查、合规检查等

        logger.info("Transaction event processed successfully");
    }

    @RabbitListener(queues = RabbitMQConfig.ACCOUNT_BALANCE_QUEUE)
    public void handleAccountBalanceEvent(AccountBalanceEvent event) {
        logger.info("=== Processing Account Balance Event ===");
        logger.info("Account: {}", event.getAccountNumber());
        logger.info("Operation: {}", event.getOperation());
        logger.info("Old Balance: {}", event.getOldBalance());
        logger.info("New Balance: {}", event.getNewBalance());
        logger.info("Timestamp: {}", event.getTimestamp());

        // 这里可以添加余额变更后处理逻辑
        // 比如余额告警、报表更新等

        logger.info("Balance event processed successfully");
    }

    @RabbitListener(queues = RabbitMQConfig.AUDIT_QUEUE)
    public void handleAuditEvent(TransactionEvent event) {
        logger.info("=== Audit Log ===");
        logger.info("Auditing transaction: {} - {} - {}",
                event.getTransactionId(),
                event.getTransactionType(),
                event.getAmount());

        // 这里可以记录审计日志

        logger.info("Audit log recorded");
    }
}