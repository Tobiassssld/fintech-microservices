package com.example.fintech.common.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class DepositRequest {
    private String accountNumber;
    private BigDecimal amount;

}
