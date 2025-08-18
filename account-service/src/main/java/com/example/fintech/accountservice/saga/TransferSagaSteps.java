package com.example.fintech.accountservice.saga;

import com.example.fintech.common.saga.SagaStep;
import com.example.fintech.common.entity.Account;
import com.example.fintech.accountservice.repository.AccountRepository;
import com.example.fintech.common.exception.InsufficientFundsException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class TransferSagaSteps {

    @Autowired
    private AccountRepository accountRepository;

    public static class ReserveFromAccountStep implements SagaStep {
        private final AccountRepository accountRepository;
        private final String fromAccountNumber;
        private final BigDecimal amount;
        private Account fromAccount;

        public ReserveFromAccountStep(AccountRepository accountRepository,
                                      String fromAccountNumber, BigDecimal amount) {
            this.accountRepository = accountRepository;
            this.fromAccountNumber = fromAccountNumber;
            this.amount = amount;
        }

        @Override
        public void execute() {
            fromAccount = accountRepository.findByAccountNumber(fromAccountNumber)
                    .orElseThrow(() -> new RuntimeException("from account not found"));

            if (fromAccount.getBalance().compareTo(amount) < 0) {
                throw new InsufficientFundsException("insufficient funds");
            }

            fromAccount.setBalance(fromAccount.getBalance().subtract(amount));
            accountRepository.save(fromAccount);
        }

        @Override
        public void compensate() {
            if (fromAccount != null) {
                fromAccount.setBalance(fromAccount.getBalance().add(amount));
                accountRepository.save(fromAccount);
            }
        }

        @Override
        public String getStepName() {
            return "ReserveFromAccount";
        }
    }

    public static class CreditToAccountStep implements SagaStep {
        private final AccountRepository accountRepository;
        private final String toAccountNumber;
        private final BigDecimal amount;
        private Account toAccount;

        public CreditToAccountStep(AccountRepository accountRepository,
                                   String toAccountNumber, BigDecimal amount) {
            this.accountRepository = accountRepository;
            this.toAccountNumber = toAccountNumber;
            this.amount = amount;
        }

        @Override
        public void execute() {
            toAccount = accountRepository.findByAccountNumber(toAccountNumber)
                    .orElseThrow(() -> new RuntimeException("To account not found"));

            // Credit amount to account
            toAccount.setBalance(toAccount.getBalance().add(amount));
            accountRepository.save(toAccount);
        }

        @Override
        public void compensate() {
            if (toAccount != null) {
                // Reverse the credit
                toAccount.setBalance(toAccount.getBalance().subtract(amount));
                accountRepository.save(toAccount);
            }
        }

        @Override
        public String getStepName() {
            return "CreditToAccount";
        }
    }
}
