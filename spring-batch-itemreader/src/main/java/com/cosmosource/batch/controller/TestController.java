package com.cosmosource.batch.controller;


import com.cosmosource.batch.entity.TestData;
import com.cosmosource.batch.springpartitionlocal.partition.RangePartitioner;
import com.cosmosource.batch.springpartitionlocal.processor.UserProcessor;
import com.cosmosource.batch.springpartitionlocal.tasklet.DummyTasklet;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.partition.PartitionHandler;
import org.springframework.batch.core.partition.support.TaskExecutorPartitionHandler;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.PagingQueryProvider;
import org.springframework.batch.item.database.support.SqlPagingQueryProviderFactoryBean;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;


@Slf4j
@EnableBatchProcessing
@RestController
@RequestMapping("/test")
@RequiredArgsConstructor
public class TestController {
    private final JobLauncher jobLauncher;
    private final JobBuilderFactory jobBuilderFactory;

    private final StepBuilderFactory stepBuilderFactory;
    private final DataSource dataSource;

    /**
     * 测试批处理
     *
     * @throws Exception
     */
    @PostMapping(value = "/start")
    public void test() throws Exception {
        Job partitionJob = jobBuilderFactory.get("partitionJob").incrementer(new RunIdIncrementer())
                .start(masterStep()).next(step2()).build();
        jobLauncher.run(partitionJob, new JobParameters());
    }


    public Step step2() {
        return stepBuilderFactory.get("step2").tasklet(dummyTask()).build();
    }

    public DummyTasklet dummyTask() {
        return new DummyTasklet();
    }

    public Step masterStep() {
        return stepBuilderFactory.get("masterStep").partitioner("slave", rangePartitioner())
                .partitionHandler(masterSlaveHandler()).build();
    }

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

    public Step slave() {
        log.info("...........called slave .........");

        return stepBuilderFactory.get("slave").<TestData, TestData>chunk(100)
                .reader(slaveReader(null, null, null))
                .processor(slaveProcessor(null)).writer(System.out::println).build();
    }

    public RangePartitioner rangePartitioner() {
        return new RangePartitioner();
    }

    public SimpleAsyncTaskExecutor taskExecutor() {
        return new SimpleAsyncTaskExecutor();
    }

    @StepScope
    public UserProcessor slaveProcessor(@Value("#{stepExecutionContext[name]}") String name) {
        log.info("********called slave processor **********");
        UserProcessor userProcessor = new UserProcessor();
        userProcessor.setThreadName(name);
        return userProcessor;
    }

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

    public PagingQueryProvider queryProvider() {
        log.info("queryProvider start ");
        SqlPagingQueryProviderFactoryBean provider = new SqlPagingQueryProviderFactoryBean();
        provider.setDataSource(dataSource);
        provider.setSelectClause("select id, field1, field2, field3");
        provider.setFromClause("from test");
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

}
