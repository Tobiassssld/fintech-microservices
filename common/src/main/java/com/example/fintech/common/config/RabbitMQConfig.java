package com.example.fintech.common.config;

import com.rabbitmq.client.AMQP;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // Exchange names
    public static final String TRANSACTION_EXCHANGE = "transaction.exchange";
    public static final String ACCOUNT_EXCHANGE = "account.exchange";
    public static final String NOTIFICATION_EXCHANGE = "notification.exchange";

    // Dead Letter Queue names
    public static final String TRANSACTION_DLQ = "transaction.dlq";
    public static final String ACCOUNT_BALANCE_DLQ = "account.balance.dlq";
    public static final String NOTIFICATION_DLQ = "notification.dlq";

    // Queue names
    public static final String TRANSACTION_QUEUE = "transaction.queue";
    public static final String ACCOUNT_BALANCE_QUEUE = "account.balance.queue";
    public static final String NOTIFICATION_QUEUE = "notification.queue";
    public static final String AUDIT_QUEUE = "audit.queue";

    // Routing keys
    public static final String TRANSACTION_ROUTING_KEY = "transaction.created";
    public static final String BALANCE_ROUTING_KEY = "account.balance.updated";
    public static final String NOTIFICATION_ROUTING_KEY = "notification.send";

    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
        RabbitAdmin admin = new RabbitAdmin(connectionFactory);
        admin.setAutoStartup(true);
        return admin;
    }

    // Exchanges
    @Bean
    public TopicExchange transactionExchange() {
        return new TopicExchange(TRANSACTION_EXCHANGE, true, false);
    }

    @Bean
    public TopicExchange accountExchange() {
        return new TopicExchange(ACCOUNT_EXCHANGE, true, false);
    }

    @Bean
    public TopicExchange notificationExchange() {
        return new TopicExchange(NOTIFICATION_EXCHANGE, true, false);
    }


    // Queues
    @Bean
    public Queue transactionQueue() {
        return QueueBuilder.durable(TRANSACTION_QUEUE)
                .withArgument("x-dead-letter-exchange", TRANSACTION_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", "transaction.failed")
                .withArgument("x-message-ttl", 300000) // 5 minutes TTL
                .build();
    }

    @Bean
    public Queue accountBalanceQueue() {
        return QueueBuilder.durable(ACCOUNT_BALANCE_QUEUE)
                .withArgument("x-dead-letter-exchange", ACCOUNT_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", "account.failed")
                .withArgument("x-message-ttl",300000)
                .build();
    }

    @Bean
    public Queue notificationQueue() {
        return QueueBuilder.durable(NOTIFICATION_QUEUE)
                .withArgument("x-dead-letter-exchange", NOTIFICATION_EXCHANGE)  // 必须是 NOTIFICATION_EXCHANGE
                .withArgument("x-dead-letter-routing-key", "notification.failed")  // 必须是 notification.failed
                .withArgument("x-message-ttl", 300000)
                .build();
    }

    
    @Bean
    public Queue auditQueue() {
        return QueueBuilder.durable(AUDIT_QUEUE).build();
    }

    // Dead Letter Queues
    @Bean
    public Queue transactionDeadLetterQueue() {
        System.out.println("=== Creating Transaction DLQ Bean ===");
        return QueueBuilder.durable(TRANSACTION_DLQ).build();
    }

    @Bean
    public Queue accountBalanceDeadLetterQueue() {
        System.out.println("=== Creating AccountBalance DLQ Bean ===");
        return QueueBuilder.durable(ACCOUNT_BALANCE_DLQ).build();
    }

    @Bean
    public Queue notificationDeadLetterQueue() {
        System.out.println("=== Creating Notification DLQ Bean ===");
        return QueueBuilder.durable(NOTIFICATION_DLQ).build();
    }

    // Bindings
    @Bean
    public Binding transactionBinding() {
        return BindingBuilder
                .bind(transactionQueue())
                .to(transactionExchange())
                .with(TRANSACTION_ROUTING_KEY);
    }

    @Bean
    public Binding balanceBinding() {
        return BindingBuilder
                .bind(accountBalanceQueue())
                .to(accountExchange())
                .with(BALANCE_ROUTING_KEY);
    }

    @Bean
    public Binding notificationBinding() {
        return BindingBuilder
                .bind(notificationQueue())
                .to(notificationExchange())
                .with(NOTIFICATION_ROUTING_KEY);
    }

    @Bean
    public Binding auditBinding() {
        return BindingBuilder
                .bind(auditQueue())
                .to(transactionExchange())
                .with("transaction.*");
    }

//    @Bean
//    public Declarables additionalDeclarables() {
//        System.out.println("=== Creating Declarables Bean ===");  // ← 添加这行
//        System.out.println("Creating DLQ: " + TRANSACTION_DLQ);    // ← 添加这行
//        return new Declarables(
//                transactionDeadLetterQueue(),
//                accountBalanceDeadLetterQueue(),
//                notificationDeadLetterQueue(),
//
//                transactionDlqBinding(),
//                accountBalanceDlqBinding(),
//                notificationDlqBinding()
//        );
//    }

    @Bean
    public Binding transactionDlqBinding() {
        return BindingBuilder
                .bind(transactionDeadLetterQueue())
                .to(transactionExchange())
                .with("transaction.failed");
    }

    @Bean
    public Binding accountBalanceDlqBinding() {
        return BindingBuilder
                .bind(accountBalanceDeadLetterQueue())
                .to(accountExchange())
                .with("account.failed");
    }

    @Bean
    public Binding notificationDlqBinding() {
        return BindingBuilder
                .bind(notificationDeadLetterQueue())
                .to(notificationExchange())
                .with("notification.failed");
    }

    // JSON converter
    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    // RabbitTemplate with JSON converter
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }

    // Listener container factory
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter());
        return factory;
    }
}