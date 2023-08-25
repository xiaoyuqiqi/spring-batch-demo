package com.cosmosource.batch.job;

import com.cosmosource.batch.job.entity.TestData;
import com.cosmosource.batch.job.partition.RangePartitioner;
import com.cosmosource.batch.job.processor.UserProcessor;
import com.cosmosource.batch.job.tasklet.DummyTasklet;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.partition.PartitionHandler;
import org.springframework.batch.core.partition.support.TaskExecutorPartitionHandler;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.PagingQueryProvider;
import org.springframework.batch.item.database.support.SqlPagingQueryProviderFactoryBean;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.jdbc.core.BeanPropertyRowMapper;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Configuration
@EnableBatchProcessing
@RequiredArgsConstructor
public class PartitionerJob {

  private final JobBuilderFactory jobBuilderFactory;
  private final StepBuilderFactory stepBuilderFactory;
  private final DataSource dataSource;

  @Bean(name = "partitionJob")
  public Job PartitionJob() {
    return jobBuilderFactory.get("partitionJob").incrementer(new RunIdIncrementer())
        .start(masterStep()).next(step2()).build();
  }

  @Bean
  public Step step2() {
    return stepBuilderFactory.get("step2").tasklet(dummyTask()).build();
  }

  @Bean
  public DummyTasklet dummyTask() {
    return new DummyTasklet();
  }

  @Bean
  public Step masterStep() {
    return stepBuilderFactory.get("masterStep").partitioner(slave().getName(), rangePartitioner())
        .partitionHandler(masterSlaveHandler()).build();
  }

  @Bean
  public PartitionHandler masterSlaveHandler() {
    TaskExecutorPartitionHandler handler = new TaskExecutorPartitionHandler();
    handler.setGridSize(10);
    handler.setTaskExecutor(taskExecutor());
    handler.setStep(slave());
    try {
      handler.afterPropertiesSet();
    } catch (Exception e) {
      e.printStackTrace();
    }
    return handler;
  }

  @Bean(name = "slave")
  public Step slave() {
    log.info("...........called slave .........");

    return stepBuilderFactory.get("slave").<TestData, TestData>chunk(100)
        .reader(slaveReader(null, null, null))
        .processor((ItemProcessor<? super TestData, ? extends TestData>) slaveProcessor(null)).writer(slaveWriter(null, null)).build();
  }

  @Bean
  public RangePartitioner rangePartitioner() {
    return new RangePartitioner();
  }

  @Bean
  public SimpleAsyncTaskExecutor taskExecutor() {
    return new SimpleAsyncTaskExecutor();
  }

  @Bean
  @StepScope
  public UserProcessor slaveProcessor(@Value("#{stepExecutionContext[name]}") String name) {
    log.info("********called slave processor **********");
    UserProcessor userProcessor = new UserProcessor();
    userProcessor.setThreadName(name);
    return userProcessor;
  }

  @Bean
  @StepScope
  public JdbcPagingItemReader<TestData> slaveReader(
      @Value("#{stepExecutionContext[fromId]}") final String fromId,
      @Value("#{stepExecutionContext[toId]}") final String toId,
      @Value("#{stepExecutionContext[name]}") final String name) {
    log.info("slaveReader start " + fromId + " " + toId);
    JdbcPagingItemReader<TestData> reader = new JdbcPagingItemReader<>();
    reader.setDataSource(dataSource);
    reader.setQueryProvider(queryProvider());
    Map<String, Object> parameterValues = new HashMap<>();
    parameterValues.put("fromId", fromId);
    parameterValues.put("toId", toId);
    log.info("Parameter Value " + name + " " + parameterValues);
    reader.setParameterValues(parameterValues);
    reader.setPageSize(1000);
    reader.setRowMapper(new BeanPropertyRowMapper<TestData>() {{
      setMappedClass(TestData.class);
    }});
    log.info("slaveReader end " + fromId + " " + toId);
    return reader;
  }

  @Bean
  public PagingQueryProvider queryProvider() {
    log.info("queryProvider start ");
    SqlPagingQueryProviderFactoryBean provider = new SqlPagingQueryProviderFactoryBean();
    provider.setDataSource(dataSource);
    provider.setSelectClause("select id, field1, field2, field3");
    provider.setFromClause("from test");
//    provider.setWhereClause("where id >= :fromId and id <= :toId");
    provider.setWhereClause("where id >= :fromId and id <= :toId");
    provider.setSortKey("id");
    log.info("queryProvider end ");
    try {
      return provider.getObject();
    } catch (Exception e) {
      log.info("queryProvider exception ");
      e.printStackTrace();
    }

    return null;
  }

  @Bean
  @StepScope
  public FlatFileItemWriter<TestData> slaveWriter(
      @Value("#{stepExecutionContext[fromId]}") final String fromId,
      @Value("#{stepExecutionContext[toId]}") final String toId) {
    FlatFileItemWriter<TestData> reader = new FlatFileItemWriter<>();
    reader.setResource(new FileSystemResource(
        "csv/outputs/users.processed" + fromId + "-" + toId + ".csv"));
    //reader.setAppendAllowed(false);
    reader.setLineAggregator(new DelimitedLineAggregator<TestData>() {{
      setDelimiter(",");
      setFieldExtractor(new BeanWrapperFieldExtractor<TestData>() {{
        setNames(new String[]{"id", "field1", "field2", "field3"});
      }});
    }});
    return reader;
  }
}
