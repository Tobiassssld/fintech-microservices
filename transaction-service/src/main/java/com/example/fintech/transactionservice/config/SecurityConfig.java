package com.example.fintech.transactionservice.config;

import com.example.fintech.common.config.BaseSecurityConfig;
import com.example.fintech.transactionservice.filter.UserContextFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends BaseSecurityConfig {

    @Autowired
    private UserContextFilter userContextFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        configureBaseSecurity(http)
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints first - highest priority
                        .requestMatchers(getPublicEndpoints()).permitAll()
                        // Test endpoints - allow without authentication
                        .requestMatchers("/api/test/**").permitAll()
                        // All other requests require authentication
                        .anyRequest().authenticated()
                )
                .addFilterBefore(userContextFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}