package com.example.fintech.accountservice.controller;

import com.example.fintech.common.context.UserContext;
import com.example.fintech.common.dto.*;
import com.example.fintech.common.entity.Account;
import com.example.fintech.accountservice.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    @Autowired
    private AccountService accountService;

    @PostMapping("/create")
    public ResponseEntity<?> createAccount(
            @RequestParam("accountType") String accountType,
            @RequestParam(value = "currency", defaultValue = "USD") String currency) {
        try {
            UserContext.UserInfo userInfo = UserContext.getUser();
            if (userInfo == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("User context not found");
            }

            Account account = accountService.createAccount(
                    userInfo.getUserId(),
                    userInfo.getUsername(),
                    accountType,
                    currency
            );

            return ResponseEntity.ok(Map.of(
                    "accountNumber", account.getAccountNumber(),
                    "accountType", account.getAccountType(),
                    "currency", account.getCurrency(),
                    "balance", account.getBalance()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping("/my-accounts")
    public ResponseEntity<?> getMyAccounts() {
        UserContext.UserInfo userInfo = UserContext.getUser();
        if (userInfo == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("User context not found");
        }

        List<Account> accounts = accountService.getAccountsByUserId(userInfo.getUserId());
        return ResponseEntity.ok(accounts);
    }

    @GetMapping("/balance/{accountNumber}")
    public ResponseEntity<?> getAccountBalance(@PathVariable String accountNumber) {
        UserContext.UserInfo userInfo = UserContext.getUser();
        if (userInfo == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("User context not found");
        }

        try {
            Account account = accountService.getAccountByAccountNumber(accountNumber);

            // Verify ownership using userId stored in account
            if (!account.getUserId().equals(userInfo.getUserId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("Access denied");
            }

            return ResponseEntity.ok(Map.of(
                    "accountNumber", account.getAccountNumber(),
                    "balance", account.getBalance(),
                    "currency", account.getCurrency()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PostMapping("/deposit")
    public ResponseEntity<?> deposit(@RequestBody DepositRequest request) {
        UserContext.UserInfo userInfo = UserContext.getUser();
        if (userInfo == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("User context not found");
        }

        try {
            accountService.deposit(
                    request.getAccountNumber(),
                    request.getAmount(),
                    userInfo.getUserId()
            );
            return ResponseEntity.ok(Map.of("message", "Deposit successful"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PostMapping("/withdraw")
    public ResponseEntity<?> withdraw(@RequestBody WithdrawRequest request) {
        UserContext.UserInfo userInfo = UserContext.getUser();
        if (userInfo == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("User context not found");
        }

        try {
            accountService.withdraw(
                    request.getAccountNumber(),
                    request.getAmount(),
                    userInfo.getUserId()
            );
            return ResponseEntity.ok(Map.of("message", "Withdrawal successful"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PostMapping("/transfer")
    public ResponseEntity<?> transfer(@RequestBody TransferRequest request) {
        UserContext.UserInfo userInfo = UserContext.getUser();
        if (userInfo == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("User context not found");
        }

        try {
            accountService.transfer(
                    request.getFromAccount(),
                    request.getToAccount(),
                    request.getAmount(),
                    userInfo.getUserId()
            );
            return ResponseEntity.ok(Map.of("message", "Transfer successful"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}