package com.cosmosource.batch.processor;

import com.cosmosource.batch.entity.TestData;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

/**
 * 
 */
@Component
public class TestDataTransformItemPorcessor implements ItemProcessor<TestData, TestData> {
    @Override
    public TestData process(TestData item) {
        // field1值拼接 hello
        item.setField1(item.getField1() + " hello");
        return item;
    }
}
