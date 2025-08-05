package com.example.fintech.userservice.service;

import com.example.fintech.common.entity.User;
import jakarta.servlet.http.HttpSession;

public interface UserService {
    void register(User user);
    boolean login(String username, String password);
    User findByUsername(String username);
    void logout(HttpSession session);
    boolean changePassword(String username, String oldPassword, String newPassword);
}
