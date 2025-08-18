package com.example.fintech.accountservice.service;

import com.example.fintech.common.saga.SagaOrchestrator;
import com.example.fintech.common.saga.SagaStep;
import com.example.fintech.accountservice.saga.TransferSagaSteps;
import com.example.fintech.accountservice.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

@Service
public class DistributedTransferService {

    @Autowired
    private SagaOrchestrator sagaOrchestrator;

    @Autowired
    private AccountRepository accountRepository;

    @Transactional
    public void executeDistributedTransfer(String fromAccount, String toAccount, BigDecimal amount) {

        SagaStep reserveStep = new TransferSagaSteps.ReserveFromAccountStep(
                accountRepository, fromAccount, amount);

        SagaStep creditStep = new TransferSagaSteps.CreditToAccountStep(
                accountRepository, toAccount, amount);

        List<SagaStep> steps = Arrays.asList(reserveStep, creditStep);

        sagaOrchestrator.executeSaga(steps);
    }
}
