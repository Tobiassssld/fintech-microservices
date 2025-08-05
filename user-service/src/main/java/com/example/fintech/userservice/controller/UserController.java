package com.example.fintech.userservice.controller;

import com.example.fintech.common.dto.ChangePasswordRequest;
import com.example.fintech.common.dto.LoginRequest;
import com.example.fintech.common.dto.LoginResponse;
import com.example.fintech.common.entity.User;
import com.example.fintech.userservice.security.JwtUtil;
import com.example.fintech.userservice.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/user")

public class UserController {
    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody User user){
        try{
            userService.register(user);
            return ResponseEntity.ok("registeration successful!");
        }catch (RuntimeException e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request){
        boolean success = userService.login(request.getUsername(), request.getPassword());
        if (success){
            String token = jwtUtil.generateToken(request.getUsername());
            return ResponseEntity.ok(new LoginResponse(token, "login successful"));
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid crefential");
        }
    }


    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(){
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.findByUsername(username);
        if (user != null){
            return ResponseEntity.ok(user);
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout() {
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok("Logged out successfully.");
    }


    @PostMapping("/change-password")
    public ResponseEntity<String> changePassword(@RequestBody ChangePasswordRequest request){
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        boolean result = userService.changePassword(username, request.getOldPassword(), request.getNewPassword());
        if (result){
            return ResponseEntity.ok("password changed successfully.");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("old password incorrect.");
        }
    }
}

