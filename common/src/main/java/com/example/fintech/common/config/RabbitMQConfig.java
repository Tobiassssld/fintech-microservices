package com.example.fintech.common.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.management.Query;

@Configuration
public class RabbitMQConfig {

    //exchange
    public static final String TRANSACTION_EXCHANGE = "transaction.exchange";
    public static final String ACCOUNT_EXCHANGE = "accoount.exchange";
    public static final String NOTIFICATION_EXCHANGE = "notification.exchange";

    //queue
    public static final String TRANSACTION_QUEUE = "transaction.queue";
    public static final String ACCOUNT_BALANCE_QUEUE = "account.balance.queue";
    public static final String NOTIFICATION_QUEUE = "notification.queue";
    public static final String AUDIT_QUEUE = "audit.queue";

    //routing

    public static final String TRANSACTION_ROUTING_KEY = "transaction.created";
    public static final String BALANCE_ROUTING_KEY = "account.balance.updated";
    public static final String NOTIFICATION_ROUTING_KEY = "notification.send";

    //exchanges

    @Bean
    public TopicExchange transactionExchange(){
        return new TopicExchange(TRANSACTION_EXCHANGE);
    }

    @Bean
    public TopicExchange accountExchange(){
        return new TopicExchange(ACCOUNT_EXCHANGE);
    }

    @Bean
    public TopicExchange notificationExchange(){
        return new TopicExchange(NOTIFICATION_EXCHANGE);
    }

    //QUEUES

    @Bean
    public Queue transactionQueue(){
        return QueueBuilder.durable(TRANSACTION_QUEUE).build();
    }

    @Bean
    public Queue accountQueue(){
        return QueueBuilder.durable(ACCOUNT_BALANCE_QUEUE).build();
    }

    @Bean
    public Queue notificationQueue(){
        return QueueBuilder.durable(NOTIFICATION_QUEUE).build();
    }

    @Bean
    public Queue auditQueue(){
        return QueueBuilder.durable(AUDIT_QUEUE).build();
    }

    //binding

    @Bean public Binding transactionBinding() {
        return BindingBuilder
                .bind(transactionQueue())
                .to(transactionExchange())
                .with(TRANSACTION_ROUTING_KEY);
    }

    @Bean public Binding balanceBinding() {
        return BindingBuilder
                .bind(notificationQueue())
                .to(notificationExchange())
                .with(NOTIFICATION_ROUTING_KEY);
    }

    @Bean public Binding auditBinding() {
        return BindingBuilder
                .bind(auditQueue())
                .to(transactionExchange())
                .with("transaction.*");
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter());
        return factory;
    }


}
