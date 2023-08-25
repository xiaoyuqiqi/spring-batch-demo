package com.cosmosource.batch.processor;

import com.cosmosource.batch.entity.TestData;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

/**
 * 
 */
@Component
public class TestDataFilterItemProcessor implements ItemProcessor<TestData, TestData> {
    @Override
    public TestData process(TestData item) {
        // 返回null，会过滤掉这条数据
        return "".equals(item.getField3()) ? null : item;
    }
}
