package com.cpf.batch.execution;

import com.cpf.batch.api.BatchExecutionPlan;
import com.cpf.batch.api.BatchExecutionTopology;
import com.cpf.batch.api.BatchStepDefinition;
import com.cpf.batch.spi.BatchFencingPort;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.job.builder.SimpleJobBuilder;
import org.springframework.batch.core.job.flow.Flow;
import org.springframework.batch.core.partition.support.TaskExecutorPartitionHandler;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.core.task.TaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

/** 승인된 CPF Plan을 Kafka/Broker 없이 Spring Batch Job/Step graph로 materialize합니다. */
public final class CpfBatchJobFactory implements AutoCloseable {
    private final JobRepository repository;
    private final PlatformTransactionManager transactionManager;
    private final TaskExecutor taskExecutor;
    private final CpfBatchStepHandlerRegistry handlers;
    private final BatchFencingPort fencing;
    private final CpfBatchExecutionListener listener;
    private final CpfBatchExecutionProperties properties;
    private final CpfBatchExecutionContextSupport contextSupport;
    private final Map<String, Job> cache;

    public CpfBatchJobFactory(
            JobRepository repository,
            PlatformTransactionManager transactionManager,
            TaskExecutor taskExecutor,
            CpfBatchStepHandlerRegistry handlers,
            BatchFencingPort fencing,
            CpfBatchExecutionListener listener,
            CpfBatchExecutionProperties properties,
            CpfBatchExecutionContextSupport contextSupport) {
        this.repository = repository;
        this.transactionManager = transactionManager;
        this.taskExecutor = taskExecutor;
        this.handlers = handlers;
        this.fencing = fencing;
        this.listener = listener;
        this.properties = properties;
        this.contextSupport = contextSupport;
        this.cache = new LinkedHashMap<>(128, 0.75f, true) {
            @Override protected boolean removeEldestEntry(Map.Entry<String, Job> eldest) {
                return size() > CpfBatchJobFactory.this.properties.maxMaterializedJobs();
            }
        };
    }

    public synchronized Job materialize(BatchExecutionPlan plan) {
        plan.verifyIntegrity();
        String cacheKey = plan.planId() + "@" + plan.planVersion() + ":" + plan.checksum();
        Job existing = cache.get(cacheKey);
        if (existing != null) return existing;
        Job created = build(plan);
        cache.put(cacheKey, created);
        return created;
    }

    @Override public synchronized void close() { cache.clear(); }

    public static String jobName(String planId, long planVersion, String checksum) {
        if (checksum == null || !checksum.matches("[0-9a-f]{64}")) {
            throw new IllegalArgumentException("plan checksum must be canonical SHA-256");
        }
        return planId + "_V" + planVersion + "_" + checksum.substring(0, 16);
    }

    private Job build(BatchExecutionPlan plan) {
        String jobName = jobName(plan.planId(), plan.planVersion(), plan.checksum());
        List<Step> steps = plan.steps().stream().map(step -> step(plan.topology(), step)).toList();
        JobBuilder builder = new JobBuilder(jobName, repository).listener(listener);
        if (plan.topology() == BatchExecutionTopology.PARALLEL_STEPS) {
            Flow[] flows = steps.stream().map(this::flow).toArray(Flow[]::new);
            Flow split = new FlowBuilder<Flow>(jobName + ".parallel").split(taskExecutor).add(flows).build();
            return builder.start(split).end().build();
        }
        boolean conditional = plan.steps().stream().anyMatch(step ->
                !step.nextOnSuccess().isBlank() || !step.nextOnFailure().isBlank());
        if (conditional) return conditionalJob(builder, jobName, plan, steps);
        SimpleJobBuilder sequence = builder.start(steps.getFirst());
        for (int index = 1; index < steps.size(); index++) sequence = sequence.next(steps.get(index));
        return sequence.build();
    }

    private Job conditionalJob(JobBuilder builder, String jobName, BatchExecutionPlan plan, List<Step> steps) {
        Map<String, Step> byId = new LinkedHashMap<>();
        for (int index = 0; index < plan.steps().size(); index++) byId.put(plan.steps().get(index).stepId(), steps.get(index));
        FlowBuilder<Flow> flow = new FlowBuilder<Flow>(jobName + ".conditional").start(steps.getFirst());
        for (int index = 0; index < plan.steps().size(); index++) {
            BatchStepDefinition definition = plan.steps().get(index);
            Step current = steps.get(index);
            String success = definition.nextOnSuccess();
            String failure = definition.nextOnFailure();
            if (!success.isBlank()) {
                if ("END".equals(success)) flow.from(current).on("COMPLETED").end();
                else flow.from(current).on("COMPLETED").to(byId.get(success));
            } else if (index + 1 < steps.size()) {
                flow.from(current).on("COMPLETED").to(steps.get(index + 1));
            } else {
                flow.from(current).on("COMPLETED").end();
            }
            if (!failure.isBlank()) {
                if ("END".equals(failure)) flow.from(current).on("*").end();
                else flow.from(current).on("*").to(byId.get(failure));
            } else {
                flow.from(current).on("*").fail();
            }
        }
        return builder.start(flow.build()).end().build();
    }

    private Flow flow(Step step) { return new FlowBuilder<Flow>(step.getName() + ".flow").start(step).build(); }

    private Step step(BatchExecutionTopology topology, BatchStepDefinition definition) {
        Step worker = taskletStep(definition.stepId() + ".worker", definition);
        return switch (topology) {
            case LOCAL, PARALLEL_STEPS -> worker;
            case LOCAL_PARTITION -> localPartition(definition, worker);
        };
    }

    private Step taskletStep(String name, BatchStepDefinition definition) {
        return new StepBuilder(name, repository)
                .tasklet(new CpfBatchTasklet(definition, handlers, fencing, contextSupport), transactionManager)
                .listener(listener)
                .startLimit(definition.restartable() ? Integer.MAX_VALUE : 1)
                .build();
    }

    private Step localPartition(BatchStepDefinition definition, Step worker) {
        TaskExecutorPartitionHandler handler = new TaskExecutorPartitionHandler();
        handler.setTaskExecutor(taskExecutor);
        handler.setStep(worker);
        handler.setGridSize(definition.partitionCount());
        return new StepBuilder(definition.stepId() + ".manager", repository)
                .partitioner(worker.getName(), new CpfBatchPartitioner(definition, properties.maxPartitionCount()))
                .partitionHandler(handler)
                .gridSize(definition.partitionCount())
                .listener(listener)
                .build();
    }
}
