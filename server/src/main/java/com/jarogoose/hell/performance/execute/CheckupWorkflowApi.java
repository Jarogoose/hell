package com.jarogoose.hell.performance.execute;

import static com.jarogoose.hell.performance.execute.CollectionWorkflowMapper.toConfigurationKey;
import static com.jarogoose.hell.performance.execute.CollectionWorkflowMapper.toFactory;

import com.jarogoose.hell.performance.control.request.CheckupConfigurationModel;
import com.jarogoose.hell.performance.persist.ExecutionStorage;
import com.jarogoose.hell.performance.persist.data.ExecutionTable;
import com.jarogoose.hell.performance.persist.data.MeasurementData;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CheckupWorkflowApi {

  private ExecutionStorage storage;
  private ExecutorService executor;

  @Autowired
  public CheckupWorkflowApi(ExecutionStorage storage, ExecutorService executor) {
    this.storage = storage;
    this.executor = executor;
  }

  public void measureCollectionPerformance(CheckupConfigurationModel params) {
    Callable<ExecutionTable> task = () -> {
      Collection<MeasurementData> summary = run(params.times(), toFactory(params));
      ExecutionTable execution = new ExecutionTable(toConfigurationKey(params), summary);
      storage.save(execution);
      return execution;
    };
    executor.submit(task);
  }

  private Collection<MeasurementData> run(int times, MeasurementFactory measure) {
    Collection<MeasurementData> measures = new ArrayList<>();

    for (int i = 0; i < times; i++) {
      measures.add(measure(measure));
    }
    return measures;
  }

  private MeasurementData measure(MeasurementFactory measure) {
    final long genTime = measure.generation();
    final long sortTime = measure.sorting();
    final long addTime = measure.inserting();
    final long deleteTime = measure.deleting();
    final long retrieveTime = measure.retrieving();

    final MeasurementData data = new MeasurementData();
    data.setGenerateTimeNanos(genTime);
    data.setSortTimeNanos(sortTime);
    data.setAddTimeNanos(addTime);
    data.setDeleteTimeNanos(deleteTime);
    data.setRetrieveTimeNanos(retrieveTime);

    return data;
  }
}
