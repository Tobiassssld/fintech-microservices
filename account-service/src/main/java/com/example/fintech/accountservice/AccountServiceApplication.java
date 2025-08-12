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
        "com.example.fintech.common.config"
})
public class AccountServiceApplication {

    public static void main(String[] args){
        SpringApplication.run(AccountServiceApplication.class, args);
    }

    // 添加这个Bean来检查RabbitMQ相关Bean是否被创建
    @Bean
    public CommandLineRunner checkBeans(ApplicationContext ctx) {
        return args -> {
            System.out.println("=== Checking for RabbitMQ Beans ===");

            // 检查RabbitMQConfig是否被加载
            try {
                Object rabbitConfig = ctx.getBean("rabbitMQConfig");
                System.out.println("✅ RabbitMQConfig bean found: " + rabbitConfig);
            } catch (Exception e) {
                System.out.println("❌ RabbitMQConfig bean NOT found: " + e.getMessage());
            }

            // 检查EventPublisher是否被加载
            try {
                Object eventPublisher = ctx.getBean("eventPublisher");
                System.out.println("✅ EventPublisher bean found: " + eventPublisher);
            } catch (Exception e) {
                System.out.println("❌ EventPublisher bean NOT found: " + e.getMessage());
            }

            // 检查RabbitTemplate是否被加载
            try {
                Object rabbitTemplate = ctx.getBean("rabbitTemplate");
                System.out.println("✅ RabbitTemplate bean found: " + rabbitTemplate);
            } catch (Exception e) {
                System.out.println("❌ RabbitTemplate bean NOT found: " + e.getMessage());
            }

            // 列出所有包含rabbit的Bean
            System.out.println("=== All Rabbit-related beans ===");
            String[] beanNames = ctx.getBeanNamesForType(Object.class);
            for (String beanName : beanNames) {
                if (beanName.toLowerCase().contains("rabbit")) {
                    System.out.println("Found rabbit bean: " + beanName);
                }
            }
        };
    }
}