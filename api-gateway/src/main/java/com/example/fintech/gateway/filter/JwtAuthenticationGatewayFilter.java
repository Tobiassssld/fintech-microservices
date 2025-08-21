package com.example.fintech.gateway.filter;

import com.example.fintech.gateway.security.JwtUtil;
import com.example.fintech.gateway.client.UserServiceClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
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
        String path = request.getPath().value();

        // 调试日志
        System.out.println("=== JWT Filter Processing ===");
        System.out.println("Request Path: " + path);
        System.out.println("Request Method: " + request.getMethod());

        // 获取Authorization header（兼容大小写）
        String authHeader = request.getHeaders().getFirst("Authorization");
        if (authHeader == null) {
            authHeader = request.getHeaders().getFirst("authorization");
        }

        System.out.println("Auth Header: " + authHeader);

        String token = extractToken(authHeader);
        System.out.println("Extracted Token: " + (token != null ? token.substring(0, Math.min(20, token.length())) + "..." : "null"));

        if (token == null) {
            System.out.println("No token found, returning 401");
            return handleUnauthorized(exchange);
        }

        try {
            // 提取用户名
            String username = jwtUtil.extractUsername(token);
            System.out.println("Extracted Username: " + username);

            if (username != null && jwtUtil.validateToken(token, username)) {
                System.out.println("Token is valid, fetching user info for: " + username);

                // 获取用户信息并添加到请求头
                return userServiceClient.getUserInfo(username)
                        .flatMap(userInfo -> {
                            System.out.println("User info retrieved - ID: " + userInfo.getUserId() + ", Username: " + userInfo.getUsername());

                            // 修改请求，添加用户信息到header
                            ServerHttpRequest modifiedRequest = request.mutate()
                                    .header("X-User-Id", String.valueOf(userInfo.getUserId()))
                                    .header("X-Username", userInfo.getUsername())
                                    .header("X-User-Roles", "USER")
                                    .build();

                            System.out.println("Headers added, forwarding request");
                            return chain.filter(exchange.mutate().request(modifiedRequest).build());
                        })
                        .onErrorResume(error -> {
                            System.err.println("Error fetching user info: " + error.getMessage());
                            error.printStackTrace();
                            return handleUnauthorized(exchange);
                        });
            } else {
                System.out.println("Token validation failed");
                return handleUnauthorized(exchange);
            }
        } catch (Exception e) {
            System.err.println("JWT processing error: " + e.getMessage());
            e.printStackTrace();
            return handleUnauthorized(exchange);
        }
    }

    private String extractToken(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        if (authHeader != null && authHeader.startsWith("bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }

    private Mono<Void> handleUnauthorized(ServerWebExchange exchange) {
        System.out.println("Returning 401 Unauthorized");
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }
}