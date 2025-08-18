package com.example.fintech.accountservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.ApplicationContext;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EntityScan("com.example.fintech.common.entity")
@EnableJpaRepositories("com.example.fintech.accountservice.repository")
@ComponentScan(basePackages = {
        "com.example.fintech.accountservice",
        "com.example.fintech.common.service",
        "com.example.fintech.common.config",
        "com.example.fintech.common.saga"
})
public class AccountServiceApplication {

    public static void main(String[] args){
        SpringApplication.run(AccountServiceApplication.class, args);
    }
}