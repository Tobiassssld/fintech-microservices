package com.example.fintech.common.listener;

import com.example.fintech.common.config.RabbitMQConfig;
import com.example.fintech.common.event.AccountBalanceEvent;
import com.example.fintech.common.event.NotificationEvent;
import com.example.fintech.common.event.TransactionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;


// common/src/main/java/com/example/fintech/common/listener/DeadLetterQueueProcessor.java
@Component
public class DeadLetterQueueProcessor {

    private static final Logger logger = LoggerFactory.getLogger(DeadLetterQueueProcessor.class);

    @RabbitListener(queues = RabbitMQConfig.TRANSACTION_DLQ)
    public void handleFailedTransactionEvent(TransactionEvent event) {
        logger.error("DEAD LETTER: Transaction event failed permanently: {}", event.getTransactionId());
        // 实现补偿逻辑、告警通知等
    }

    @RabbitListener(queues = RabbitMQConfig.ACCOUNT_BALANCE_DLQ)
    public void handleFailedBalanceEvent(AccountBalanceEvent event) {
        logger.error("DEAD LETTER: Balance event failed permanently: {}", event.getAccountNumber());
        // 实现补偿逻辑
    }

    @RabbitListener(queues = RabbitMQConfig.NOTIFICATION_DLQ)
    public void handleFailedNotificationEvent(NotificationEvent event) {
        logger.error("DEAD LETTER: Notification event failed permanently for user: {}", event.getUserId());
        // 实现告警或重试逻辑
    }
}