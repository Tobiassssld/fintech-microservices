package com.example.fintech.common.saga;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

@Component
public class SagaOrchestrator {
    private static final Logger logger = LoggerFactory.getLogger(SagaOrchestrator.class);

    public void executeSaga(List<SagaStep> steps) {
        Stack<SagaStep> executedSteps =  new Stack<>();

        try {
            for (SagaStep step : steps) {
                logger.info("Executing saga step: {}", step.getStepName());
                step.execute();
                executedSteps.push(step);
            }
            logger.info("Saga completed successfully");
        } catch (Exception e) {
            logger.error("Saga failed, starting compensation", e);
            compensate(executedSteps);
            throw new RuntimeException("Saga execution failed", e);
        }
    }

    private void compensate(Stack<SagaStep> executedSteps) {
        while (!executedSteps.isEmpty()) {
            SagaStep step = executedSteps.pop();
            try {
                logger.info("compensating step: {}", step.getStepName());
                step.compensate();
            } catch (Exception e) {
                logger.error("Compensation failed for step: {}", step.getStepName());
            }
        }
    }
}
