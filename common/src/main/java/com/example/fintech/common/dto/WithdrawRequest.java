package com.example.fintech.common.dto;

import lombok.Data;
import java.math.BigDecimal;


@Data
public class WithdrawRequest {
    private String accountNumber;
    private BigDecimal amount;

}
