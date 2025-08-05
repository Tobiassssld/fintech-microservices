package com.example.fintech.transactionservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan("com.example.fintech.common.entity")
@EnableJpaRepositories("com.example.fintech.transactionservice.repository")
public class TransactionServiceApllication {
    public static void main(String[] args){
        SpringApplication.run(TransactionServiceApllication.class, args);
    }
}
