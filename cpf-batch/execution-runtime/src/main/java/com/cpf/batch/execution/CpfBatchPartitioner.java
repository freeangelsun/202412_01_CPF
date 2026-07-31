package com.cpf.batch.execution;

import com.cpf.batch.api.BatchStepDefinition;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.infrastructure.item.ExecutionContext;

/** Partition index·Step 정의를 ExecutionContext에 저장하여 원격 재시작 가능한 분할을 만듭니다. */
public final class CpfBatchPartitioner implements Partitioner {
    private final BatchStepDefinition step;
    private final int partitionCount;

    public CpfBatchPartitioner(BatchStepDefinition step, int maxPartitionCount) {
        this.step = step;
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
            CpfRemoteStepDefinition.write(context, step);
            result.put("partition-" + index, context);
        }
        return result;
    }
}
