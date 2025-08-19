package com.example.fintech.userservice.controller;

import com.example.fintech.common.entity.User;
import com.example.fintech.userservice.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users/internal")
public class InternalUserController {

    @Autowired
    private UserService userService;

    @GetMapping("/info/{username}")
    public Map<String, Object> getUserInfo(@PathVariable String username) {
        User user = userService.findByUsername(username);
        if (user == null) {
            throw new RuntimeException("User not found");
        }
        return Map.of(
                "userId", user.getId(),
                "username", user.getUsername()
        );
    }
}