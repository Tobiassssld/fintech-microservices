package com.example.fintech.common.saga;

public interface SagaStep {
    void execute();
    void compensate();
    String getStepName();
}
