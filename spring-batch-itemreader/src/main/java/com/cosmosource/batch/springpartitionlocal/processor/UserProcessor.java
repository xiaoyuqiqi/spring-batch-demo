package com.cosmosource.batch.springpartitionlocal.processor;

import com.cosmosource.batch.entity.TestData;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Component
public class UserProcessor implements ItemProcessor<TestData, TestData> {

  private String threadName;

  public String getThreadName() {
    return threadName;
  }

  public void setThreadName(String threadName) {
    this.threadName = threadName;
  }

  @Override
  public TestData process(TestData item) throws Exception {
    System.out.println(threadName + " processing : "
        + item.getId() + " : " + item.getField1());
    return item;
  }
}
