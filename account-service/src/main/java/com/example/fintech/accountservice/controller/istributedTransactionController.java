package com.example.fintech.accountservice.controller;

import com.example.fintech.accountservice.service.DistributedTransferService;
import com.example.fintech.common.dto.TransferRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/accounts/distributed")
public class istributedTransactionController {

    @Autowired
    private DistributedTransferService distributedTransferService;

    @PostMapping("/transfer")
    public ResponseEntity<?> distributedTransfer(@RequestBody TransferRequest request) {
        try {
            distributedTransferService.executeDistributedTransfer(
                    request.getFromAccount(),
                    request.getToAccount(),
                    request.getAmount()
            );

            return ResponseEntity.ok(Map.of(
                    "message", "Distributed transfer completed susccessfully",
                    "type", "SAGA_PATTERN"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Distributed transfer failed",
                    "message", e.getMessage()
            ));
        }
    }
}
