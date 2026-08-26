package com.cpf.batch.execution;

import com.cpf.batch.api.BatchStepDefinition;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.batch.core.partition.Partitioner;
import org.springframework.batch.infrastructure.item.ExecutionContext;

/** LOCAL_PARTITION의 partition index/key를 ExecutionContext에 기록합니다. */
public final class CpfBatchPartitioner implements Partitioner {
    private final int partitionCount;

    public CpfBatchPartitioner(BatchStepDefinition step, int maxPartitionCount) {
        this.partitionCount = step.partitionCount();
        if (partitionCount <= 0 || partitionCount > maxPartitionCount) {
            throw new IllegalArgumentException("partitionCount out of product range: " + partitionCount);
        }
    }

    @Override
    public Map<String, ExecutionContext> partition(int gridSize) {
        Map<String, ExecutionContext> result = new LinkedHashMap<>();
        for (int index = 0; index < partitionCount; index++) {
            ExecutionContext context = new ExecutionContext();
            context.putInt("cpf.partition.index", index);
            context.putInt("cpf.partition.count", partitionCount);
            context.putString("cpf.partition.key", "partition-" + index);
            result.put("partition-" + index, context);
        }
        return result;
    }
}
