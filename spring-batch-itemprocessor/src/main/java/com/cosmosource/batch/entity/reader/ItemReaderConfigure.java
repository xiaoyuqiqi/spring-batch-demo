package com.cosmosource.batch.entity.reader;

import com.cosmosource.batch.entity.TestData;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 */
@Configuration
public class ItemReaderConfigure {

    @Bean
    public ListItemReader<TestData> simpleReader() {
        List<TestData> data = new ArrayList<>();
        TestData testData1 = new TestData();
        testData1.setId(1);
        testData1.setField1("11");
        testData1.setField2("12");
        testData1.setField3("13");
        data.add(testData1);
        TestData testData2 = new TestData();
        testData2.setId(2);
        testData2.setField1("21");
        testData2.setField2("22");
        testData2.setField3("23");
        data.add(testData2);
        TestData testData3 = new TestData();
        testData3.setId(3);
        testData3.setField1("31");
        testData3.setField2("32");
        testData3.setField3("");
        data.add(testData3);
        return new ListItemReader<>(data);
    }
}
