package com.example.fintech.gateway.client;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class UserServiceClient {

    private final WebClient webClient;

    public UserServiceClient(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder
                .baseUrl("http://USER-SERVICE")
                .build();
    }

    public Mono<UserInfo> getUserInfo(String username) {
        return webClient.get()
                .uri("/api/users/internal/info/{username}", username)
                .retrieve()
                .bodyToMono(UserInfo.class);
    }

    public static class UserInfo {
        private Long userId;
        private String username;

        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
    }
}