package com.example.fintech.accountservice.config;

import com.example.fintech.accountservice.filter.UserContextFilter;
import com.example.fintech.common.config.BaseSecurityConfig;
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
        return configureBaseSecurity(http)
                .authorizeHttpRequests(auth -> auth
                        // 公共端点
                        .requestMatchers(getPublicEndpoints()).permitAll()
                        // 测试端点 - 最高优先级
                        .requestMatchers("/api/test/**").permitAll()
                        // 其他请求需要认证
                        .anyRequest().authenticated()
                )
                .addFilterBefore(userContextFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}