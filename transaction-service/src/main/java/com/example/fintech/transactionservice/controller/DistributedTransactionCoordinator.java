package com.example.fintech.transactionservice.controller;

import com.example.fintech.common.entity.DistributedTransaction;
import com.example.fintech.common.enums.DistributedTransactionStatus;
import com.example.fintech.common.event.TransactionEvent;
import com.example.fintech.common.service.EventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;


@Service
public class DistributedTransactionCoordinator {

    private static final Logger logger = LoggerFactory.getLogger(DistributedTransactionCoordinator.class);
    private static final int TIMEOUT_SECONDS = 30;

    @Autowired
    private EventPublisher eventPublisher;

    public String initiateDistributedTransfer(String fromAccount,  String toAccount, BigDecimal amount) {
        String transactionId = generateTransactionId();

        try {
            //phase 1: prepare
            logger.info("initiating distributed transaction : {}", transactionId);

            DistributedTransaction dtx = createDistributedTransaction(transactionId, fromAccount, toAccount, amount);

            // Send prepare messages to participants
            CompletableFuture<Boolean> prepareResult = sendPreparePhase(dtx);

            // Wait for prepare responses with timeout
            Boolean prepared = prepareResult.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);

            if (prepared){
                return commitPhase(dtx);
            } else {
                return abortPhase(dtx);
            }
        } catch (Exception e) {
            logger.error("distributed transaction failed : {}", transactionId, e);
            //handle timeout or failure
            abortTransaction(transactionId);
            throw new RuntimeException("distributed transaction failed", e);
        }
    }

    private CompletableFuture<Boolean> sendPreparePhase(DistributedTransaction dtx) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                //send prepare event
                TransactionEvent prepareEvent = createPrepareEvent(dtx);
                eventPublisher.publishTransactionEvent(prepareEvent);

                //simulate participant responses
                Thread.sleep(1000);

                //for demo purpose, assume success
                return true;

            } catch (Exception e) {
                logger.error("Prepare phase failed for transaction: {}", dtx.getTransactionId(), e);
                return false;
            }
        });
    }

    private String commitPhase(DistributedTransaction dtx) {
        logger.info("committing distributed transaction : {}", dtx.getTransactionId());

        dtx.setStatus(DistributedTransactionStatus.COMMITTING);

        // Send commit event
        TransactionEvent commitEvent = createCommitEvent(dtx);
        eventPublisher.publishTransactionEvent(commitEvent);

        dtx.setStatus(DistributedTransactionStatus.COMMITTED);
        return dtx.getTransactionId();
    }

    private String abortPhase(DistributedTransaction dtx) {
        logger.info("aborting distributed transaction: {}", dtx.getTransactionId());

        dtx.setStatus(DistributedTransactionStatus.ABORTING);


        //send abort event
        TransactionEvent abortEvent = createAbortEvent(dtx);
        eventPublisher.publishTransactionEvent(abortEvent);

        dtx.setStatus(DistributedTransactionStatus.ABORTED);
        return dtx.getTransactionId();
    }

    private void abortTransaction(String transactionId) {
        logger.warn("Aborting transaction due to timeout : {} ", transactionId);
        //clean up
    }

    private DistributedTransaction createDistributedTransaction(String transactionId,
                                                                String fromAccount, String toAccount, BigDecimal amount) {
        DistributedTransaction dtx = new DistributedTransaction();
        dtx.setTransactionId(transactionId);
        dtx.setFromAccountNumber(toAccount);
        dtx.setToAccountNumber(toAccount);
        dtx.setAmount(amount);
        dtx.setStatus(DistributedTransactionStatus.INITATED);
        return dtx;
    }

    private TransactionEvent createPrepareEvent(DistributedTransaction dtx) {
        TransactionEvent event = new TransactionEvent();
        event.setTransactionId(dtx.getTransactionId());
        event.setFromAccount(dtx.getFromAccountNumber());
        event.setToAccount(dtx.getToAccountNumber());
        event.setAmount(dtx.getAmount());
        event.setTransactionType("DISTRIBUTED_TRANSFER_PREPARE");
        event.setStatus("PREPARING");
        event.setTimestamp(LocalDateTime.now());
        return event;
    }

    private TransactionEvent createCommitEvent(DistributedTransaction dtx) {
        TransactionEvent event = new TransactionEvent();
        event.setTransactionId(dtx.getTransactionId());
        event.setTransactionType("DISTRIBUTED_TRANSFER_COMMIT");
        event.setStatus("COMMITTING");
        event.setTimestamp(LocalDateTime.now());
        return event;
    }

    private TransactionEvent createAbortEvent(DistributedTransaction dtx) {
        TransactionEvent event = new TransactionEvent();
        event.setTransactionId(dtx.getTransactionId());
        event.setTransactionType("DISTRIBUTED_TRANSFER_ABORT");
        event.setStatus("ABORTING");
        event.setTimestamp(LocalDateTime.now());
        return event;
    }

    private String generateTransactionId() {
        return "DTX-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
