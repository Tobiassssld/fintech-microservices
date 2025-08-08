package com.example.fintech.gateway.config;

import com.example.fintech.gateway.filter.JwtAuthenticationGatewayFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;

@Configuration
public class GatewayConfig {

    @Autowired
    private JwtAuthenticationGatewayFilter jwtAuthFilter;

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // User Service - 公共路由（不需要认证）
                .route("user-public", r -> r
                        .path("/api/users/register", "/api/users/login")
                        .and()
                        .method(HttpMethod.POST)
                        .uri("lb://USER-SERVICE"))

                // User Service - 需要认证的路由
                .route("user-protected", r -> r
                        .path("/api/users/**")
                        .filters(f -> f.filter(jwtAuthFilter))
                        .uri("lb://USER-SERVICE"))

                // Account Service - 全部需要认证
                .route("account-service", r -> r
                        .path("/api/accounts/**")
                        .filters(f -> f.filter(jwtAuthFilter))
                        .uri("lb://ACCOUNT-SERVICE"))

                // Transaction Service - 全部需要认证
                .route("transaction-service", r -> r
                        .path("/api/transactions/**")
                        .filters(f -> f.filter(jwtAuthFilter))
                        .uri("lb://TRANSACTION-SERVICE"))

                .build();
    }
}