package com.example.fintech.gateway.filter;

import com.example.fintech.gateway.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import com.example.fintech.gateway.client.UserServiceClient;
import reactor.core.publisher.Mono;

@Component
public class JwtAuthenticationGatewayFilter implements GatewayFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserServiceClient userServiceClient;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String token = extractToken(request);

        if (token == null) {
            return handleUnauthorized(exchange);
        }

        try {
            String username = jwtUtil.extractUsername(token);
            if (username != null && jwtUtil.validateToken(token, username)) {
                // Get user ID from user service
                return userServiceClient.getUserInfo(username)
                        .flatMap(userInfo -> {
                            ServerHttpRequest modifiedRequest = request.mutate()
                                    .header("X-User-Id", String.valueOf(userInfo.getUserId()))
                                    .header("X-Username", userInfo.getUsername())
                                    .header("X-User-Roles", "USER")
                                    .build();
                            return chain.filter(exchange.mutate().request(modifiedRequest).build());
                        })
                        .onErrorResume(e -> handleUnauthorized(exchange));
            }
        } catch (Exception e) {
            return handleUnauthorized(exchange);
        }

        return handleUnauthorized(exchange);
    }

    private String extractToken(ServerHttpRequest request) {
        String authHeader = request.getHeaders().getFirst("AUTHORIZATION");
        return (authHeader != null && authHeader.startsWith("Bearer "))
                ? authHeader.substring(7) : null;
    }

    private Mono<Void> handleUnauthorized(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }
}