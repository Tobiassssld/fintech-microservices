package com.example.fintech.accountservice.validator;

import com.example.fintech.common.entity.Account;
import com.example.fintech.common.exception.InvalidTransactionException;
import org.springframework.stereotype.Component;

@Component
public class AccountOwnershipValidator {

    public void validateOwnership(Account account, Long userId) {
        if (!account.getUser().getId().equals(userId)) {
            throw new SecurityException("Access denied: You don't own this account");
        }
    }

    public void validateAccess(Account account, Long userId, String operation) {
        // 验证所有权
        validateOwnership(account, userId);

        // 根据操作类型进行额外验证
        if ("WITHDRAW".equals(operation) && "FROZEN".equals(account.getStatus())) {
            throw new InvalidTransactionException("Account is frozen, withdrawal not allowed");
        }

        if ("TRANSFER".equals(operation) && "SUSPENDED".equals(account.getStatus())) {
            throw new InvalidTransactionException("Account is suspended, transfer not allowed");
        }

        if (!"ACTIVE".equals(account.getStatus()) && !"DEPOSIT".equals(operation)) {
            throw new InvalidTransactionException("Account is not active for this operation");
        }
    }
}