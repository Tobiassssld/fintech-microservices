package com.example.fintech.accountservice.controller;

import com.example.fintech.common.dto.*;
import com.example.fintech.common.entity.Account;
import com.example.fintech.accountservice.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    @Autowired
    private AccountService accountService;

    @PostMapping("/create")
    public ResponseEntity<?> createAccount(@RequestParam("userId") Long userId,
                                           @RequestParam("accountType") String accountType,
                                           @RequestParam(value = "currency", defaultValue = "USD") String currency) {
        try {
            System.out.println("Creating account for userId: " + userId + ", type: " + accountType + ", currency: " + currency);
            Account account = accountService.createAccount(userId, accountType, currency);
            return ResponseEntity.ok(account);
        } catch (RuntimeException e) {
            System.err.println("Error creating account: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping("/balance")
    public ResponseEntity<?> getBalance(@RequestParam("userId") Long userId) {
        try {
            System.out.println("Getting balance for userId: " + userId);
            Account account = accountService.getAccountByUserId(userId);
            return ResponseEntity.ok(account);
        } catch (RuntimeException e) {
            System.err.println("Error getting balance: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @PostMapping("/deposit")
    public ResponseEntity<String> deposit(@RequestBody DepositRequest request) {
        try {
            System.out.println("Deposit request: " + request.getAccountNumber() + ", amount: " + request.getAmount());
            accountService.deposit(request.getAccountNumber(), request.getAmount());
            return ResponseEntity.ok("Deposit successful");
        } catch (RuntimeException e) {
            System.err.println("Deposit error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PostMapping("/withdraw")
    public ResponseEntity<String> withdraw(@RequestBody WithdrawRequest request) {
        try {
            accountService.withdraw(request.getAccountNumber(), request.getAmount());
            return ResponseEntity.ok("Withdrawal successful");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PostMapping("/transfer")
    public ResponseEntity<String> transfer(@RequestBody TransferRequest request) {
        try {
            accountService.transfer(request.getFromAccount(), request.getToAccount(), request.getAmount());
            return ResponseEntity.ok("Transfer successful");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}