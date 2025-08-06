package com.example.fintech.accountservice.repository;

import com.example.fintech.common.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // Account Service只需要基本的用户查询功能
}
