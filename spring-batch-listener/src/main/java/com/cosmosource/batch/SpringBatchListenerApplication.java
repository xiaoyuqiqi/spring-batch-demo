package com.cosmosource.batch;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableBatchProcessing
public class SpringBatchListenerApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringBatchListenerApplication.class, args);
    }

}
