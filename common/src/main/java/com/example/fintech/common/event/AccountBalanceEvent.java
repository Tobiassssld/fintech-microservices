package com.example.fintech.common.event;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AccountBalanceEvent {
    private String accountNumber;
    private BigDecimal oldBalance;
    private BigDecimal newBalance;
    private String operation;
    private LocalDateTime timestamp;
}