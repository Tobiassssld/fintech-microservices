package com.example.fintech.gateway.config;

import com.example.fintech.gateway.filter.JwtAuthenticationGatewayFilterFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    @Autowired
    private JwtAuthenticationGatewayFilterFactory jwtAuthFilter;

    @Bean
    public RouteLocator routes(RouteLocatorBuilder builder) {
        return builder.routes()
                // User Service Routes - 部分需要认证
                .route("user-register", r -> r
                        .path("/api/users/register")
                        .uri("http://localhost:8081"))

                .route("user-login", r -> r
                        .path("/api/users/login")
                        .uri("http://localhost:8081"))

                .route("user-service-secured", r -> r
                        .path("/api/users/**")
                        .filters(f -> f.filter(jwtAuthFilter.apply(new JwtAuthenticationGatewayFilterFactory.Config())))
                        .uri("http://localhost:8081"))

                // Account Service Routes - 需要认证
                .route("account-service", r -> r
                        .path("/api/accounts/**")
                        .filters(f -> f.filter(jwtAuthFilter.apply(new JwtAuthenticationGatewayFilterFactory.Config())))
                        .uri("http://localhost:8082"))

                // Transaction Service Routes - 需要认证
                .route("transaction-service", r -> r
                        .path("/api/transactions/**")
                        .filters(f -> f.filter(jwtAuthFilter.apply(new JwtAuthenticationGatewayFilterFactory.Config())))
                        .uri("http://localhost:8083"))

                .build();
    }
}