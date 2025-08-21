package com.example.fintech.gateway.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import java.time.Duration;

@Component
public class UserServiceClient {

    private final WebClient webClient;
    private final String userServiceUrl;

    public UserServiceClient(WebClient.Builder webClientBuilder,
                             @Value("${user.service.url:http://localhost:8081}") String userServiceUrl) {
        this.userServiceUrl = userServiceUrl;
        this.webClient = webClientBuilder
                .baseUrl(userServiceUrl)
                .defaultHeader("Content-Type", "application/json")
                .build();

        System.out.println("UserServiceClient initialized with URL: " + userServiceUrl);
    }

    public Mono<UserInfo> getUserInfo(String username) {
        String uri = "/api/users/internal/info/" + username;
        System.out.println("Fetching user info from: " + userServiceUrl + uri);

        return webClient.get()
                .uri(uri)
                .retrieve()
                .bodyToMono(UserInfo.class)
                .timeout(Duration.ofSeconds(5))
                .doOnSuccess(userInfo -> {
                    System.out.println("Successfully retrieved user info: " + userInfo);
                })
                .doOnError(WebClientResponseException.class, error -> {
                    System.err.println("WebClient error: " + error.getStatusCode() + " - " + error.getResponseBodyAsString());
                })
                .doOnError(Exception.class, error -> {
                    System.err.println("General error fetching user info: " + error.getMessage());
                    error.printStackTrace();
                })
                .onErrorResume(error -> {
                    System.err.println("Returning empty Mono due to error: " + error.getMessage());
                    return Mono.empty();
                });
    }

    // UserInfo内部类
    public static class UserInfo {
        private Long userId;
        private String username;

        // 默认构造函数
        public UserInfo() {}

        // 带参数构造函数
        public UserInfo(Long userId, String username) {
            this.userId = userId;
            this.username = username;
        }

        // Getters and Setters
        public Long getUserId() {
            return userId;
        }

        public void setUserId(Long userId) {
            this.userId = userId;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        @Override
        public String toString() {
            return "UserInfo{userId=" + userId + ", username='" + username + "'}";
        }
    }
}