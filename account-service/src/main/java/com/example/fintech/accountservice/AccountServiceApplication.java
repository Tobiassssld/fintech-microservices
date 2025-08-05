package com.example.fintech.accountservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan("com.example.fintech.common.entity")
@EnableJpaRepositories("com.example.fintech.accountservice.repository")
public class AccountServiceApplication {
    public static void main(String[] args){
        SpringApplication.run(AccountServiceApplication.class, args);
    }
}
