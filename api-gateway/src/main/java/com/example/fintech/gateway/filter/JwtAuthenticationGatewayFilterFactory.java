package com.example.fintech.gateway.filter;

import com.example.fintech.gateway.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.io.ObjectInputFilter;
import java.util.List;


@Component
public class JwtAuthenticationGatewayFilterFactory
        extends AbstractGatewayFilterFactory<JwtAuthenticationGatewayFilterFactory.Config>{

    @Autowired
    private JwtUtil jwtutil;

    public JwtAuthenticationGatewayFilterFactory(){
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config){
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();

            if (isPublicPath(request.getPath().toString())){
                return chain.filter(exchange);
            }

            List<String> authHeaders = request.getHeaders().get(HttpHeaders.AUTHORIZATION);
            if (authHeaders == null || authHeaders.isEmpty()){
                return onError(exchange, "Missing Authorization header", HttpStatus.UNAUTHORIZED);
            }

            String authHeader = authHeaders.get(0);
            if (!authHeader.startsWith("Bearer ")){
                return onError(exchange, "Invaild Authorization Header format", HttpStatus.UNAUTHORIZED);
            }

            String token = authHeader.substring(7);

            try{
                String username = jwtutil.extractUsername(token);
                if (username != null && jwtutil.validateToken(token, username)){
                    ServerHttpRequest modifiedRequest = request.mutate()
                            .header("X-User-Id", username)
                            .build();
                    return chain.filter(exchange.mutate().request(modifiedRequest).build());
                }
            } catch (Exception e) {
                return onError(exchange, "Invalid Jwt Token", HttpStatus.UNAUTHORIZED);
            }

            return onError(exchange, "Invalid Jwt Token", HttpStatus.UNAUTHORIZED);
        };
    }

    private boolean isPublicPath(String path){
        return path.contains("/api/users/register") ||
               path.contains("/api/users/login");
    }

    private Mono<Void> onError(ServerWebExchange exchange, String message, HttpStatus status) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        return response.setComplete();
    }

    public static class Config {
        // configuration properties if needed
    }

}
