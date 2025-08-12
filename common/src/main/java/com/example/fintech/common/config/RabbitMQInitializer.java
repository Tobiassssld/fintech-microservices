package com.example.fintech.common.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(1)
public class RabbitMQInitializer implements ApplicationRunner {

    @Autowired
    private RabbitAdmin rabbitAdmin;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        System.out.println("=== Initializing RabbitMQ Infrastructure ===");

        try {
            // 创建 Exchanges
            rabbitAdmin.declareExchange(new TopicExchange(RabbitMQConfig.TRANSACTION_EXCHANGE, true, false));
            rabbitAdmin.declareExchange(new TopicExchange(RabbitMQConfig.ACCOUNT_EXCHANGE, true, false));
            rabbitAdmin.declareExchange(new TopicExchange(RabbitMQConfig.NOTIFICATION_EXCHANGE, true, false));

            // 创建 Queues
            rabbitAdmin.declareQueue(QueueBuilder.durable(RabbitMQConfig.TRANSACTION_QUEUE).build());
            rabbitAdmin.declareQueue(QueueBuilder.durable(RabbitMQConfig.ACCOUNT_BALANCE_QUEUE).build());
            rabbitAdmin.declareQueue(QueueBuilder.durable(RabbitMQConfig.NOTIFICATION_QUEUE).build());
            rabbitAdmin.declareQueue(QueueBuilder.durable(RabbitMQConfig.AUDIT_QUEUE).build());

            // 创建 Bindings
            rabbitAdmin.declareBinding(
                    BindingBuilder.bind(new Queue(RabbitMQConfig.TRANSACTION_QUEUE))
                            .to(new TopicExchange(RabbitMQConfig.TRANSACTION_EXCHANGE))
                            .with(RabbitMQConfig.TRANSACTION_ROUTING_KEY)
            );

            rabbitAdmin.declareBinding(
                    BindingBuilder.bind(new Queue(RabbitMQConfig.ACCOUNT_BALANCE_QUEUE))
                            .to(new TopicExchange(RabbitMQConfig.ACCOUNT_EXCHANGE))
                            .with(RabbitMQConfig.BALANCE_ROUTING_KEY)
            );

            rabbitAdmin.declareBinding(
                    BindingBuilder.bind(new Queue(RabbitMQConfig.NOTIFICATION_QUEUE))
                            .to(new TopicExchange(RabbitMQConfig.NOTIFICATION_EXCHANGE))
                            .with(RabbitMQConfig.NOTIFICATION_ROUTING_KEY)
            );

            rabbitAdmin.declareBinding(
                    BindingBuilder.bind(new Queue(RabbitMQConfig.AUDIT_QUEUE))
                            .to(new TopicExchange(RabbitMQConfig.TRANSACTION_EXCHANGE))
                            .with("transaction.*")
            );

            System.out.println("RabbitMQ Infrastructure initialized successfully!");


            System.out.println("Verifying queues...");
            System.out.println("- Transaction Queue: " + rabbitAdmin.getQueueInfo(RabbitMQConfig.TRANSACTION_QUEUE));
            System.out.println("- Account Balance Queue: " + rabbitAdmin.getQueueInfo(RabbitMQConfig.ACCOUNT_BALANCE_QUEUE));
            System.out.println("- Notification Queue: " + rabbitAdmin.getQueueInfo(RabbitMQConfig.NOTIFICATION_QUEUE));
            System.out.println("- Audit Queue: " + rabbitAdmin.getQueueInfo(RabbitMQConfig.AUDIT_QUEUE));

        } catch (Exception e) {
            System.err.println("Failed to initialize RabbitMQ Infrastructure: " + e.getMessage());
            e.printStackTrace();
        }
    }
}