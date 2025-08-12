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

import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.connection.CorrelationData;

import javax.annotation.PostConstruct;
import java.util.UUID;

@Service
public class EventPublisher {

    private static final Logger logger = LoggerFactory.getLogger(EventPublisher.class);

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @PostConstruct
    public void init() {
        // 启用发布确认
        rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
            if (ack) {
                logger.info(" Message confirmed: {}", correlationData);
            } else {
                logger.error(" Message not confirmed: {}, cause: {}", correlationData, cause);
            }
        });

        // 设置返回回调（当消息无法路由时）
        rabbitTemplate.setReturnsCallback(returned -> {
            logger.error(" Message returned: {}", returned.getMessage());
        });
    }

    public void publishTransactionEvent(TransactionEvent event) {
        try {
            logger.info(" Publishing transaction event: {}", event.getTransactionId());
            logger.debug("Event details: {}", event);

            CorrelationData correlationData = new CorrelationData(UUID.randomUUID().toString());

            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.TRANSACTION_EXCHANGE,
                    RabbitMQConfig.TRANSACTION_ROUTING_KEY,
                    event,
                    correlationData
            );

            logger.info(" Transaction event sent successfully to exchange: {}, routing key: {}",
                    RabbitMQConfig.TRANSACTION_EXCHANGE, RabbitMQConfig.TRANSACTION_ROUTING_KEY);
        } catch (AmqpException e) {
            logger.error(" Failed to publish transaction event", e);
            throw e;
        }
    }

    public void publishAccountBalanceEvent(AccountBalanceEvent event) {
        try {
            logger.info(" Publishing balance event for account: {}", event.getAccountNumber());
            logger.debug("Event details: {}", event);

            CorrelationData correlationData = new CorrelationData(UUID.randomUUID().toString());

            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.ACCOUNT_EXCHANGE,
                    RabbitMQConfig.BALANCE_ROUTING_KEY,
                    event,
                    correlationData
            );

            logger.info("Balance event sent successfully");
        } catch (AmqpException e) {
            logger.error(" Failed to publish balance event", e);
            throw e;
        }
    }

    public void publishNotificationEvent(NotificationEvent event) {
        try {
            logger.info("Publishing notification event for user: {}", event.getUserId());
            logger.debug("Event details: {}", event);

            CorrelationData correlationData = new CorrelationData(UUID.randomUUID().toString());

            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.NOTIFICATION_EXCHANGE,
                    RabbitMQConfig.NOTIFICATION_ROUTING_KEY,
                    event,
                    correlationData
            );

            logger.info("Notification event sent successfully");
        } catch (AmqpException e) {
            logger.error("Failed to publish notification event", e);
            throw e;
        }
    }
}