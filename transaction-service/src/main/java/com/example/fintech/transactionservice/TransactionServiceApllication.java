package com.example.fintech.transactionservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.context.annotation.Import;
import com.example.fintech.common.config.RabbitMQConfig;
import org.springframework.context.annotation.ComponentScan;


@SpringBootApplication
@EntityScan("com.example.fintech.common.entity")
@EnableJpaRepositories("com.example.fintech.transactionservice.repository")
@ComponentScan(basePackages = {
        "com.example.fintech.transactionservice",
        "com.example.fintech.common.service",
        "com.example.fintech.common.config",
        "com.example.fintech.common.saga"
})
@Import(RabbitMQConfig.class)
public class TransactionServiceApllication {
    public static void main(String[] args){
        SpringApplication.run(TransactionServiceApllication.class, args);
    }
}
