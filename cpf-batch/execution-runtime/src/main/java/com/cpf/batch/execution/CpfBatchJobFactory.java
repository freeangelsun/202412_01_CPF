package com.cpf.batch.execution;

import com.cpf.batch.api.BatchExecutionPlan;
import com.cpf.batch.api.BatchExecutionTopology;
import com.cpf.batch.api.BatchStepDefinition;
import com.cpf.batch.spi.BatchFencingPort;
import java.util.ArrayList;
import java.util.List;
import java.util.LinkedHashMap;
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
import org.springframework.batch.integration.chunk.RemoteChunkingManagerStepBuilder;
import org.springframework.batch.integration.partition.RemotePartitioningManagerStepBuilder;
import org.springframework.batch.integration.remote.RemoteStep;
import org.springframework.batch.infrastructure.item.support.ListItemReader;
import org.springframework.core.task.TaskExecutor;
import org.springframework.integration.core.MessagingTemplate;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.PollableChannel;
import org.springframework.transaction.PlatformTransactionManager;

/** 승인된 CPF Plan을 Spring Batch Job/Step graph로 materialize합니다. */
public final class CpfBatchJobFactory {
    private final JobRepository repository;
    private final PlatformTransactionManager transactionManager;
    private final TaskExecutor taskExecutor;
    private final CpfBatchStepHandlerRegistry handlers;
    private final BatchFencingPort fencing;
    private final CpfBatchExecutionListener listener;
    private final CpfBatchExecutionProperties properties;
    private final MessageChannel remoteRequests;
    private final PollableChannel remoteReplies;
    private final MessagingTemplate remoteMessagingTemplate;
    private final Map<String, Job> cache;

    public CpfBatchJobFactory(
            JobRepository repository,
            PlatformTransactionManager transactionManager,
            TaskExecutor taskExecutor,
            CpfBatchStepHandlerRegistry handlers,
            BatchFencingPort fencing,
            CpfBatchExecutionListener listener,
            CpfBatchExecutionProperties properties,
            MessageChannel remoteRequests,
            PollableChannel remoteReplies,
            MessagingTemplate remoteMessagingTemplate) {
        this.repository = repository;
        this.transactionManager = transactionManager;
        this.taskExecutor = taskExecutor;
        this.handlers = handlers;
        this.fencing = fencing;
        this.listener = listener;
        this.properties = properties;
        this.remoteRequests = remoteRequests;
        this.remoteReplies = remoteReplies;
        this.remoteMessagingTemplate = remoteMessagingTemplate;
        this.cache = new LinkedHashMap<>(128, 0.75f, true) {
            @Override protected boolean removeEldestEntry(Map.Entry<String, Job> eldest) {
                return size() > properties.maxMaterializedJobs();
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
        Map<String, Step> byId = new java.util.LinkedHashMap<>();
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

    private Flow flow(Step step) {
        return new FlowBuilder<Flow>(step.getName() + ".flow").start(step).build();
    }

    private Step step(BatchExecutionTopology topology, BatchStepDefinition definition) {
        Step worker = taskletStep(definition.stepId() + ".worker", definition);
        return switch (topology) {
            case LOCAL, PARALLEL_STEPS -> worker;
            case LOCAL_PARTITION -> localPartition(definition, worker);
            case REMOTE_PARTITION -> remotePartition(definition);
            case REMOTE_CHUNK -> remoteChunk(definition);
            case REMOTE_STEP -> remoteStep(definition);
        };
    }

    private Step taskletStep(String name, BatchStepDefinition definition) {
        return new StepBuilder(name, repository)
                .tasklet(new CpfBatchTasklet(definition, handlers, fencing), transactionManager)
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

    private Step remotePartition(BatchStepDefinition definition) {
        String workerName = "cpfRemotePartitionWorkerStep";
        return new RemotePartitioningManagerStepBuilder(definition.stepId() + ".remote-manager", repository)
                .partitioner(workerName, new CpfBatchPartitioner(definition, properties.maxPartitionCount()))
                .gridSize(definition.partitionCount())
                .outputChannel(remoteRequests)
                .inputChannel(remoteReplies)
                .pollInterval(properties.remotePollIntervalMs())
                .timeout(properties.remoteTimeoutMs())
                .listener(listener)
                .build();
    }

    private Step remoteChunk(BatchStepDefinition definition) {
        List<Map<String, Object>> items = new ArrayList<>();
        for (int index = 0; index < definition.partitionCount(); index++) {
            items.add(Map.of(
                    "stepId", definition.stepId(),
                    "executorType", definition.executorType().name(),
                    "itemIndex", index,
                    "executorReference", definition.executorReference(),
                    "parameters", definition.parameters(),
                    "partitionCount", definition.partitionCount(),
                    "restartable", definition.restartable()));
        }
        return new RemoteChunkingManagerStepBuilder<Map<String, Object>, Map<String, Object>>(
                definition.stepId() + ".chunk-manager", repository)
                .chunk(properties.defaultChunkSize())
                .reader(new ListItemReader<>(items))
                .transactionManager(transactionManager)
                .outputChannel(remoteRequests)
                .inputChannel(remoteReplies)
                .maxWaitTimeouts(properties.remoteChunkMaxWaitTimeouts())
                .throttleLimit(properties.remoteChunkThrottleLimit())
                .listener(listener)
                .build();
    }

    private Step remoteStep(BatchStepDefinition definition) {
        RemoteStep step = new RemoteStep(
                definition.stepId() + ".remote",
                "cpfRemoteStepWorker",
                repository,
                remoteMessagingTemplate);
        step.setMessageChannel(remoteRequests);
        step.setPollInterval(properties.remotePollIntervalMs());
        step.setTimeout(properties.remoteTimeoutMs());
        step.registerStepExecutionListener(new CpfRemoteStepDefinitionListener(definition));
        step.registerStepExecutionListener(listener);
        return step;
    }
}
