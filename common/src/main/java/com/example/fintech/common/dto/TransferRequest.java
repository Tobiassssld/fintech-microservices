package com.example.fintech.common.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.PrimitiveIterator;

@Data
public class TransferRequest {
    private String fromAccount;
    private String toAccount;
    private BigDecimal amount;
    private String description;

}

