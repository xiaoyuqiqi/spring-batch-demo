package com.cosmosource.batch.listener;

import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.AfterStep;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.stereotype.Component;

/**
 * 
 */
@Component
public class MyStepExecutionListener {

    @BeforeStep
    public void breforeStep(StepExecution stepExecution) {
        System.out.println("before step execute: " + stepExecution.getStepName());
    }

    @AfterStep
    public void afterStep(StepExecution stepExecution) {
        System.out.println("after step execute: " + stepExecution.getStepName());
    }
}
