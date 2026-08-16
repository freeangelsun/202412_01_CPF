package com.cpf.batch.execution;

import com.cpf.batch.api.BatchJobDefinition;
import com.cpf.batch.api.annotation.CpfBatchJob;
import com.cpf.batch.api.annotation.CpfBatchStep;
import com.cpf.batch.spi.BatchStepHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.aop.support.AopUtils;
import org.springframework.context.ApplicationContext;

/**
 * {@link CpfBatchJob}/{@link CpfBatchStep}을 기존 BatchStepHandler 실행 경로에 연결합니다.
 * 중복 ID와 잘못된 signature는 startup 시 fail-closed 합니다.
 */
public final class CpfAnnotatedBatchStepHandler implements BatchStepHandler {
    public static final String REFERENCE_PREFIX = "cpf-annotation:";
    private final Map<String, Invoker> invokers;
    private final List<JobDescriptor> jobs;

    public CpfAnnotatedBatchStepHandler(ApplicationContext context) {
        Map<String, Invoker> found = new LinkedHashMap<>();
        List<JobDescriptor> descriptors = new ArrayList<>();
        for (Object bean : context.getBeansWithAnnotation(CpfBatchJob.class).values()) {
            Class<?> type = AopUtils.getTargetClass(bean);
            CpfBatchJob job = type.getAnnotation(CpfBatchJob.class);
            if (job == null) continue;
            String jobId = requireId(job.value(), "jobId");
            if (job.maxConcurrentExecutions() <= 0) {
                throw new IllegalStateException("CPF_BATCH_ANNOTATION_INVALID_CONCURRENCY:" + jobId);
            }
            List<StepDescriptor> steps = new ArrayList<>();
            for (Method method : type.getMethods()) {
                CpfBatchStep step = method.getAnnotation(CpfBatchStep.class);
                if (step == null) continue;
                validateSignature(method);
                String stepId = requireId(step.value(), "stepId");
                String reference = reference(jobId, stepId);
                Invoker previous = found.putIfAbsent(reference, new Invoker(bean, method));
                if (previous != null) throw new IllegalStateException("CPF_BATCH_ANNOTATION_DUPLICATE_STEP:" + reference);
                steps.add(new StepDescriptor(stepId, step.order(), step.idempotent(), reference));
            }
            if (steps.isEmpty()) throw new IllegalStateException("CPF_BATCH_ANNOTATION_NO_STEP:" + jobId);
            steps.sort(Comparator.comparingInt(StepDescriptor::order).thenComparing(StepDescriptor::stepId));
            if (descriptors.stream().anyMatch(existing -> existing.jobId().equals(jobId))) {
                throw new IllegalStateException("CPF_BATCH_ANNOTATION_DUPLICATE_JOB:" + jobId);
            }
            descriptors.add(new JobDescriptor(jobId, job.restartable(), job.maxConcurrentExecutions(), List.copyOf(steps)));
        }
        descriptors.sort(Comparator.comparing(JobDescriptor::jobId));
        this.invokers = Map.copyOf(found);
        this.jobs = List.copyOf(descriptors);
    }

    public static String reference(String jobId, String stepId) {
        return REFERENCE_PREFIX + requireId(jobId, "jobId") + ":" + requireId(stepId, "stepId");
    }

    public List<JobDescriptor> jobs() { return jobs; }

    @Override
    public boolean supports(BatchJobDefinition.ExecutorType executorType, String executorReference) {
        return executorType == BatchJobDefinition.ExecutorType.SERVICE_CALL
                && executorReference != null && invokers.containsKey(executorReference);
    }

    @Override
    public BatchStepResult execute(BatchStepCommand command) throws Exception {
        if (command == null || command.step() == null) throw new IllegalArgumentException("batch command/step is required");
        String reference = command.step().executorReference();
        Invoker invoker = invokers.get(reference);
        if (invoker == null) throw new IllegalStateException("CPF_BATCH_ANNOTATION_STEP_UNAVAILABLE:" + reference);
        try {
            Object result = invoker.method().getParameterCount() == 0
                    ? invoker.method().invoke(invoker.bean())
                    : invoker.method().invoke(invoker.bean(), command);
            if (result == null) return BatchStepResult.completed("", 0, 0, Map.of());
            if (result instanceof BatchStepResult batchResult) return batchResult;
            throw new IllegalStateException("CPF_BATCH_ANNOTATION_RESULT_TYPE:" + invoker.method());
        } catch (InvocationTargetException failure) {
            Throwable cause = failure.getCause();
            if (cause instanceof Exception exception) throw exception;
            if (cause instanceof Error error) throw error;
            throw new IllegalStateException(cause);
        }
    }

    private static void validateSignature(Method method) {
        if (!Modifier.isPublic(method.getModifiers())) throw new IllegalStateException("CPF_BATCH_ANNOTATION_STEP_NOT_PUBLIC:" + method);
        if (method.getParameterCount() > 1
                || (method.getParameterCount() == 1 && method.getParameterTypes()[0] != BatchStepCommand.class)) {
            throw new IllegalStateException("CPF_BATCH_ANNOTATION_STEP_SIGNATURE:" + method);
        }
        if (method.getReturnType() != Void.TYPE && method.getReturnType() != BatchStepResult.class) {
            throw new IllegalStateException("CPF_BATCH_ANNOTATION_STEP_RESULT:" + method);
        }
    }

    private static String requireId(String value, String name) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(name + " is required");
        String id = value.trim();
        if (!id.matches("[A-Za-z0-9][A-Za-z0-9._-]{0,79}")) throw new IllegalArgumentException(name + " format invalid: " + id);
        return id;
    }

    private record Invoker(Object bean, Method method) { }
    public record JobDescriptor(String jobId, boolean restartable, int maxConcurrentExecutions, List<StepDescriptor> steps) { }
    public record StepDescriptor(String stepId, int order, boolean idempotent, String executorReference) { }
}
