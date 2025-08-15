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
            createExchanges();

            // 创建主队列
            createMainQueues();

            // 创建死信队列
            createDeadLetterQueues();

            // 创建 Bindings
            createBindings();

            System.out.println("RabbitMQ Infrastructure initialized successfully!");

            // 验证所有队列
            verifyAllQueues();

        } catch (Exception e) {
            System.err.println("Failed to initialize RabbitMQ Infrastructure: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void createExchanges() {
        System.out.println("Creating exchanges...");
        rabbitAdmin.declareExchange(new TopicExchange(RabbitMQConfig.TRANSACTION_EXCHANGE, true, false));
        rabbitAdmin.declareExchange(new TopicExchange(RabbitMQConfig.ACCOUNT_EXCHANGE, true, false));
        rabbitAdmin.declareExchange(new TopicExchange(RabbitMQConfig.NOTIFICATION_EXCHANGE, true, false));
    }

    private void createMainQueues() {
        System.out.println("Creating main queues...");

        // 主队列需要配置死信交换机
        rabbitAdmin.declareQueue(
                QueueBuilder.durable(RabbitMQConfig.TRANSACTION_QUEUE)
                        .withArgument("x-dead-letter-exchange", RabbitMQConfig.TRANSACTION_EXCHANGE)
                        .withArgument("x-dead-letter-routing-key", "transaction.failed")
                        .withArgument("x-message-ttl", 300000) // 5 minutes TTL
                        .build()
        );

        rabbitAdmin.declareQueue(
                QueueBuilder.durable(RabbitMQConfig.ACCOUNT_BALANCE_QUEUE)
                        .withArgument("x-dead-letter-exchange", RabbitMQConfig.ACCOUNT_EXCHANGE)
                        .withArgument("x-dead-letter-routing-key", "account.failed")
                        .withArgument("x-message-ttl", 300000)
                        .build()
        );

        rabbitAdmin.declareQueue(
                QueueBuilder.durable(RabbitMQConfig.NOTIFICATION_QUEUE)
                        .withArgument("x-dead-letter-exchange", RabbitMQConfig.NOTIFICATION_EXCHANGE)
                        .withArgument("x-dead-letter-routing-key", "notification.failed")
                        .withArgument("x-message-ttl", 300000)
                        .build()
        );

        rabbitAdmin.declareQueue(
                QueueBuilder.durable(RabbitMQConfig.AUDIT_QUEUE).build()
        );
    }

    private void createDeadLetterQueues() {
        System.out.println("Creating dead letter queues...");

        // 创建死信队列
        Queue transactionDLQ = QueueBuilder.durable(RabbitMQConfig.TRANSACTION_DLQ).build();
        Queue accountBalanceDLQ = QueueBuilder.durable(RabbitMQConfig.ACCOUNT_BALANCE_DLQ).build();
        Queue notificationDLQ = QueueBuilder.durable(RabbitMQConfig.NOTIFICATION_DLQ).build();

        rabbitAdmin.declareQueue(transactionDLQ);
        rabbitAdmin.declareQueue(accountBalanceDLQ);
        rabbitAdmin.declareQueue(notificationDLQ);

        System.out.println("Dead letter queues created: ");
        System.out.println("  - " + RabbitMQConfig.TRANSACTION_DLQ);
        System.out.println("  - " + RabbitMQConfig.ACCOUNT_BALANCE_DLQ);
        System.out.println("  - " + RabbitMQConfig.NOTIFICATION_DLQ);
    }

    private void createBindings() {
        System.out.println("Creating bindings...");

        // 主队列绑定
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

        // 死信队列绑定
        rabbitAdmin.declareBinding(
                BindingBuilder.bind(new Queue(RabbitMQConfig.TRANSACTION_DLQ))
                        .to(new TopicExchange(RabbitMQConfig.TRANSACTION_EXCHANGE))
                        .with("transaction.failed")
        );

        rabbitAdmin.declareBinding(
                BindingBuilder.bind(new Queue(RabbitMQConfig.ACCOUNT_BALANCE_DLQ))
                        .to(new TopicExchange(RabbitMQConfig.ACCOUNT_EXCHANGE))
                        .with("account.failed")
        );

        rabbitAdmin.declareBinding(
                BindingBuilder.bind(new Queue(RabbitMQConfig.NOTIFICATION_DLQ))
                        .to(new TopicExchange(RabbitMQConfig.NOTIFICATION_EXCHANGE))
                        .with("notification.failed")
        );

        System.out.println("All bindings created");
    }

    private void verifyAllQueues() {
        System.out.println("\nVerifying all queues:");

        // 验证主队列
        System.out.println("Main Queues:");
        verifyQueue(RabbitMQConfig.TRANSACTION_QUEUE);
        verifyQueue(RabbitMQConfig.ACCOUNT_BALANCE_QUEUE);
        verifyQueue(RabbitMQConfig.NOTIFICATION_QUEUE);
        verifyQueue(RabbitMQConfig.AUDIT_QUEUE);

        // 验证死信队列
        System.out.println("\nDead Letter Queues:");
        verifyQueue(RabbitMQConfig.TRANSACTION_DLQ);
        verifyQueue(RabbitMQConfig.ACCOUNT_BALANCE_DLQ);
        verifyQueue(RabbitMQConfig.NOTIFICATION_DLQ);
    }

    private void verifyQueue(String queueName) {
        try {
            QueueInformation info = rabbitAdmin.getQueueInfo(queueName);
            if (info != null) {
                System.out.println("  ✓ " + queueName + " (messages: " + info.getMessageCount() + ")");
            } else {
                System.out.println("  ✗ " + queueName + " NOT FOUND!");
            }
        } catch (Exception e) {
            System.out.println("  ✗ " + queueName + " - Error: " + e.getMessage());
        }
    }
}