package com.cpf.batch.worker;

import com.cpf.batch.api.*;
import com.cpf.batch.runtime.JobPackCatalog;
import com.cpf.batch.runtime.LogContext;
import com.cpf.batch.runtime.SensitiveTextSanitizer;
import com.cpf.batch.worker.internal.JdbcWorkerExecutionRepository;
import com.cpf.batch.worker.internal.JdbcWorkerLeaseRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.stereotype.Component;

import java.time.*;
import java.util.*;

/** Job Pack의 Spring Batch/승인 Shell/File Executor를 동일 Lease/Fencing 경계에서 실행합니다. */
@Component
public class JobPackDispatcher {
    private final JobPackCatalog catalog;
    private final JobOperator jobOperator;
    private final JdbcWorkerExecutionRepository executions;
    private final JdbcWorkerLeaseRepository leases;
    private final ObjectMapper objectMapper;
    private final ApprovedShellExecutor shellExecutor;
    private final ApprovedFileExecutor fileExecutor;

    public JobPackDispatcher(JobPackCatalog catalog, JobOperator jobOperator,
                             JdbcWorkerExecutionRepository executions, JdbcWorkerLeaseRepository leases,
                             ObjectMapper objectMapper, ApprovedShellExecutor shellExecutor,
                             ApprovedFileExecutor fileExecutor) {
        this.catalog=catalog; this.jobOperator=jobOperator; this.executions=executions; this.leases=leases;
        this.objectMapper=objectMapper; this.shellExecutor=shellExecutor; this.fileExecutor=fileExecutor;
    }

    public void execute(JdbcWorkerLeaseRepository.Lease lease) {
        JdbcWorkerExecutionRepository.Work work=executions.load(lease.executionId());
        boolean terminalExecutionPersisted=false;
        try (LogContext ignored=LogContext.open(Map.of(
                "transactionId",Objects.toString(work.transactionId(),""),
                "segmentId",Objects.toString(work.segmentId(),""),
                "executionId",Long.toString(work.executionId()),"jobId",work.jobId()))) {
            if (!executions.markRunning(lease)) {
                throw new IllegalStateException("Worker lease expired or was fenced before business execution");
            }
            var provider=catalog.providerFor(work.jobId());
            var definition=provider.manifest().jobs().stream().filter(j->j.jobId().equals(work.jobId())).findFirst().orElseThrow();
            Map<String,Object> raw=parse(work.parametersJson());
            String status;
            String message=null;

            switch (definition.executorType()) {
                case SPRING_BATCH -> {
                    Object resolved=provider.resolveJob(work.jobId());
                    if (!(resolved instanceof Job job)) throw new IllegalStateException("Job Pack returned non-Spring Batch Job");
                    JobParameters parameters=buildParameters(definition,raw,work,lease);
                    JobExecution launched=jobOperator.start(job,parameters);
                    if (!executions.recordSpringExecution(
                            lease, launched.getId(),
                            launched.getJobInstance()==null?null:launched.getJobInstance().getId())) {
                        throw new IllegalStateException("Worker lease expired or was fenced while the job was running");
                    }
                    status=switch(launched.getStatus()) {
                        case COMPLETED -> "COMPLETED";
                        case FAILED, ABANDONED -> "FAILED";
                        case STOPPED -> "STOPPED";
                        default -> "UNKNOWN_RESULT";
                    };
                    message=launched.getAllFailureExceptions().stream().map(Throwable::getMessage).filter(Objects::nonNull).findFirst().orElse(null);
                }
                case APPROVED_SHELL -> {
                    var result=shellExecutor.execute(definition.executorKey(),raw);
                    status=result.success()?"COMPLETED":"FAILED"; message=result.output();
                }
                case FILE_TRANSFER -> {
                    fileExecutor.transfer(required(raw,"sourceAlias"),required(raw,"sourcePath"),required(raw,"targetAlias"),required(raw,"targetPath"),Boolean.parseBoolean(Objects.toString(raw.get("overwrite"),"false")));
                    status="COMPLETED";
                }
                case FILE_PROCESS -> {
                    fileExecutor.claimForProcess(required(raw,"sourceAlias"),required(raw,"sourcePath"),required(raw,"processingAlias"));
                    status="COMPLETED";
                }
                case FILE_WATCH -> {
                    fileExecutor.await(required(raw,"watchAlias"),required(raw,"watchPath"),definition.executionPolicy().timeout());
                    status="COMPLETED";
                }
                default -> throw new IllegalStateException("Unsupported executor type: "+definition.executorType());
            }
            if (!executions.finish(lease,status,message)) {
                throw new IllegalStateException("Worker completion rejected because its lease expired or was fenced");
            }
            terminalExecutionPersisted=true;
            leases.complete(lease,status,message);
        } catch (Exception e) {
            String failureMessage = SensitiveTextSanitizer.sanitize(e.getMessage());
            if(terminalExecutionPersisted) {
                throw new IllegalStateException(
                        "Batch result was persisted but lease finalization is unresolved; recovery is required. "
                                +failureMessage,
                        e);
            }
            RuntimeException finalizationFailure = null;
            try {
                if (executions.finish(lease, "FAILED", failureMessage)) {
                    try {
                        leases.complete(lease, "FAILED", failureMessage);
                    } catch (RuntimeException releaseFailure) {
                        finalizationFailure = releaseFailure;
                    }
                }
            } catch (RuntimeException persistenceFailure) {
                finalizationFailure = persistenceFailure;
            }
            if (finalizationFailure != null) {
                e.addSuppressed(finalizationFailure);
                throw new IllegalStateException(
                        "Batch execution failed and lease finalization is unresolved; recovery is required. "
                                + SensitiveTextSanitizer.sanitize(finalizationFailure.getMessage()),
                        e);
            }
            throw new IllegalStateException("Batch execution failed: "+failureMessage,e);
        }
    }

    private JobParameters buildParameters(JobPackManifest.JobDefinition definition,Map<String,Object> raw,
                                          JdbcWorkerExecutionRepository.Work work,JdbcWorkerLeaseRepository.Lease lease) {
        JobParametersBuilder builder=new JobParametersBuilder();
        for (BatchParameterDefinition p:definition.parameters()) {
            Object value=raw.containsKey(p.name())?raw.get(p.name()):p.defaultValue();
            if (p.required() && (value==null || String.valueOf(value).isBlank())) throw new IllegalArgumentException("Required batch parameter missing: "+p.name());
            if (value==null) continue; add(builder,p,String.valueOf(value));
        }
        builder.addLong("cpfExecutionId",work.executionId(),true);
        builder.addLong("cpfFencingToken",lease.fencingToken(),true);
        if(work.businessDate()!=null)builder.addLocalDate("businessDate",work.businessDate(),true);
        return builder.toJobParameters();
    }

    private Map<String,Object> parse(String json) throws Exception {
        if(json==null||json.isBlank())return Map.of();
        return objectMapper.readValue(json,new TypeReference<>(){});
    }
    private static String required(Map<String,Object> raw,String key) {
        String value=Objects.toString(raw.get(key),"").trim(); if(value.isEmpty())throw new IllegalArgumentException(key+" is required"); return value;
    }
    private void add(JobParametersBuilder b,BatchParameterDefinition p,String value) {
        boolean id=p.identifying();
        switch(p.type().toUpperCase(Locale.ROOT)) {
            case "LONG" -> b.addLong(p.name(),Long.parseLong(value),id);
            case "DOUBLE" -> b.addDouble(p.name(),Double.parseDouble(value),id);
            case "LOCAL_DATE" -> b.addLocalDate(p.name(),LocalDate.parse(value),id);
            case "LOCAL_DATE_TIME" -> b.addLocalDateTime(p.name(),LocalDateTime.parse(value),id);
            default -> b.addString(p.name(),value,id);
        }
    }
}
