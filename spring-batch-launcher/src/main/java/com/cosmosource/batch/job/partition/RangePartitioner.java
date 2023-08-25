package com.cosmosource.batch.job.partition;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class RangePartitioner implements Partitioner {

  /**
   * 最小分区数据量
   */
  private static final int MIN_CHUNK_SIZE = 1000;

  @Autowired
  private  JdbcTemplate jdbcTemplate;

  @Autowired
  private  DataSource dataSource;

  @Override
  public Map<String, ExecutionContext> partition(int gridSize) {
    log.info("partition called gridsize= " + gridSize);
    jdbcTemplate.setDataSource(dataSource);
    int totalRowCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM test", Integer.class);

    int dynamicGridSize = Math.min(gridSize, (int) Math.ceil((double) totalRowCount / MIN_CHUNK_SIZE));

    Map<String, ExecutionContext> result = new HashMap<String, ExecutionContext>();

    int range = 10000;
    int fromId = 1;
    int toId = range;

    for (int i = 1; i <= dynamicGridSize; i++) {
      ExecutionContext value = new ExecutionContext();

      System.out.println("\nStarting : Thread" + i);
      System.out.println("fromId : " + fromId);
      System.out.println("toId : " + toId);

      value.putInt("fromId", fromId);
      value.putInt("toId", toId);

      // give each thread a name, thread 1,2,3
      value.putString("name", "Thread" + i);

      result.put("partition" + i, value);

      fromId = toId + 1;
      toId += range;

    }
    return result;
  }
}
