package com.cosmosource.batch.entity.job;

import com.cosmosource.batch.entity.TestData;
import com.cosmosource.batch.processor.TestDataFilterItemProcessor;
import com.cosmosource.batch.processor.TestDataTransformItemPorcessor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.support.CompositeItemProcessor;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * 
 */
@Component
public class CompositeItemProcessorDemo {

    @Autowired
    private JobBuilderFactory jobBuilderFactory;
    @Autowired
    private StepBuilderFactory stepBuilderFactory;
    @Autowired
    private ListItemReader<TestData> simpleReader;
    @Autowired
    private TestDataFilterItemProcessor testDataFilterItemProcessor;
    @Autowired
    private TestDataTransformItemPorcessor testDataTransformItemPorcessor;

    @Bean
    public Job compositeItemProcessorJob() {
        return jobBuilderFactory.get("compositeItemProcessorJob")
                .start(step())
                .build();
    }

    private Step step() {
        return stepBuilderFactory.get("step")
                .<TestData, TestData>chunk(2)
                .reader(simpleReader)
                .processor(compositeItemProcessor())
                .writer(list -> list.forEach(System.out::println))
                .build();
    }

    // CompositeItemProcessor组合多种中间处理器
    private CompositeItemProcessor<TestData, TestData> compositeItemProcessor() {
        CompositeItemProcessor<TestData, TestData> processor = new CompositeItemProcessor<>();
        List<ItemProcessor<TestData, TestData>> processors = Arrays.asList(testDataFilterItemProcessor, testDataTransformItemPorcessor);
        // 代理两个processor
        processor.setDelegates(processors);
        return processor;
    }
}
