package com.example.fintech.gateway.filter;

import com.example.fintech.gateway.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class JwtAuthenticationGatewayFilter implements GatewayFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        // extract JWT token
        String token = extractToken(request);
        if (token == null) {
            return handleUnauthorized(exchange);
        }

        try {
            // 验证token
            String username = jwtUtil.extractUsername(token);
            if (username != null && jwtUtil.validateToken(token, username)) {
                // 将用户信息添加到请求头，传递给后端服务
                ServerHttpRequest modifiedRequest = request.mutate()
                        .header("X-User-Id", username)
                        .header("X-User-Roles", "USER") // 可以从token中提取角色
                        .build();

                return chain.filter(exchange.mutate().request(modifiedRequest).build());
            }
        } catch (Exception e) {
            // 如果有日志记录需求，可以添加日志
            System.err.println("JWT validation failed: " + e.getMessage());

        }

        return handleUnauthorized(exchange);
    }

    private String extractToken(ServerHttpRequest request) {
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }

    private Mono<Void> handleUnauthorized(ServerWebExchange exchange) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        return response.setComplete();
    }
}