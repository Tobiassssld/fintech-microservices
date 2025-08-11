package com.example.fintech.userservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.context.annotation.Import;
import com.example.fintech.common.config.RabbitMQConfig;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EntityScan("com.example.fintech.common.entity")
@EnableJpaRepositories("com.example.fintech.userservice.repository")
@ComponentScan(basePackages = {
        "com.example.fintech.userservice",
        "com.example.fintech.common.service",
        "com.example.fintech.common.config"
})
@Import(RabbitMQConfig.class)
public class UserServiceApplication {
    public static void main(String[] args){
        SpringApplication.run(UserServiceApplication.class, args);
    }
}