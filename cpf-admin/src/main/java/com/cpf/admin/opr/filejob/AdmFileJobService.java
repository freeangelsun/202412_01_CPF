package com.cpf.admin.opr.filejob;

import com.cpf.file.tabular.api.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import com.cpf.foundation.annotation.CpfService;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.multipart.MultipartFile;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.time.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

/** 비동기 Upload/Download, Dry-run, 행별 결과, Retry, Cancel, Rollback, Retention을 처리합니다. */
@CpfService
public class AdmFileJobService {
    private static final Set<String> APPLY_UNKNOWN_STATES=Set.of("DISPATCHING","UNKNOWN_RESULT");
    private static final Set<String> ROLLBACK_UNKNOWN_STATES=Set.of("ROLLBACK_DISPATCHING","ROLLBACK_UNKNOWN_RESULT");
    private final AdmFileJobRepository repository;
    private final AdmFileTemplateRegistry registry;
    private final AdmFileArtifactStore artifacts;
    private final List<CpfTabularReader> readers;
    private final TransactionTemplate transactions;
    private final TransactionTemplate leaseTransactions;
    private final Duration lease;
    private final Duration dispatchLease;
    private final Duration retention;
    private final String workerId=UUID.randomUUID().toString();

    public AdmFileJobService(AdmFileJobRepository repository, AdmFileTemplateRegistry registry,
            AdmFileArtifactStore artifacts, List<CpfTabularReader> readers,
            org.springframework.transaction.PlatformTransactionManager transactionManager,
            @Value("${cpf.admin.file-job.lease:PT30S}") Duration lease,
            @Value("${cpf.admin.file-job.dispatch-lease:PT15M}") Duration dispatchLease,
            @Value("${cpf.admin.file-job.retention:P7D}") Duration retention){
        this.repository=Objects.requireNonNull(repository,"repository");
        this.registry=Objects.requireNonNull(registry,"registry");
        this.artifacts=Objects.requireNonNull(artifacts,"artifacts");
        this.readers=List.copyOf(Objects.requireNonNull(readers,"readers"));
        this.transactions=new TransactionTemplate(Objects.requireNonNull(transactionManager,"transactionManager"));
        this.leaseTransactions=new TransactionTemplate(transactionManager);
        this.leaseTransactions.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        if(lease==null||lease.isNegative()||lease.isZero()||lease.compareTo(Duration.ofSeconds(5))<0)
            throw new IllegalArgumentException("File Job lease는 5초 이상이어야 합니다.");
        if(dispatchLease==null||dispatchLease.compareTo(lease)<0)
            throw new IllegalArgumentException("File Job dispatch lease는 일반 lease 이상이어야 합니다.");
        if(retention==null||retention.isNegative()||retention.isZero())
            throw new IllegalArgumentException("File Job retention은 양수여야 합니다.");
        this.lease=lease;this.dispatchLease=dispatchLease;this.retention=retention;
    }

    public AdmFileJobResponse upload(String operationId,String templateCode,int version,CpfTabularFormat format,
            boolean dryRun,MultipartFile file,String operator,String reason,String clientIp){
        requireText(operationId,"operationId");requireText(operator,"operator");requireText(reason,"reason");
        Objects.requireNonNull(format,"format");
        AdmFileTemplateRegistry.Template template=registry.require(templateCode,version);
        String jobId=UUID.randomUUID().toString();
        AdmFileArtifactStore.Stored stored=artifacts.storeUpload(jobId,file,format.name().toLowerCase(Locale.ROOT));
        String requestHash=sha256(operationId+"|"+templateCode+"|"+version+"|"+format+"|"+dryRun+"|"+stored.sha256());
        Instant now=Instant.now();
        var job=new AdmFileJobRepository.Job(jobId,operationId,requestHash,AdmFileJobType.UPLOAD,templateCode,version,
                format.name(),AdmFileJobState.RECEIVED,dryRun,template.rollbackSupported(),stored.path(),null,
                stored.sha256(),null,0,0,0,null,0,null,now.plus(retention),operator,reason,clientIp,null,null,
                null,null,null,null,null,null,now,now);
        try{
            var persisted=repository.insert(job);
            if(!persisted.jobId().equals(jobId))artifacts.delete(stored.path());
            return persisted.response();
        }catch(RuntimeException error){artifacts.delete(stored.path());throw error;}
    }

    public List<AdmFileJobResponse> list(int limit){return repository.list(limit).stream().map(value -> value.response()).toList();}
    public AdmFileJobResponse get(String jobId){return repository.get(jobId).response();}
    public List<AdmFileJobRowResponse> rows(String jobId){
        repository.get(jobId);
        return repository.rows(jobId).stream().map(row->new AdmFileJobRowResponse(row.rowNumber(),row.state(),
                row.businessKey(),row.errorCode(),row.message())).toList();
    }
    public Path artifact(String jobId){
        var job=repository.get(jobId);
        String path=job.resultPath()!=null?job.resultPath():job.sourcePath();
        if(path==null)throw new IllegalStateException("다운로드 가능한 Artifact가 없습니다.");
        return artifacts.require(path);
    }

    AdmFileJobResponse apply(String jobId,String operator,String reason,String approvalId){
        requireText(operator,"operator");requireText(reason,"reason");
        var job=repository.get(jobId);
        var template=registry.require(job.templateCode(),job.templateVersion());
        requireApproval(job,template,operator,approvalId);
        if(!(job.state()==AdmFileJobState.VALIDATED||job.state()==AdmFileJobState.READY_TO_APPLY))
            throw new IllegalStateException("검증 완료 Job만 적용할 수 있습니다.");
        if(!repository.transitionControl(jobId,job.state(),AdmFileJobState.APPLYING,operator,reason,approvalId,
                AdmFileJobRepository.ControlActor.APPLIED_BY,null,null))
            throw new IllegalStateException("File Job 상태가 변경되었습니다.");
        return repository.get(jobId).response();
    }

    AdmFileJobResponse retry(String jobId,String operator,String reason,String approvalId){
        requireText(operator,"operator");requireText(reason,"reason");requireText(approvalId,"approvalId");
        var job=repository.get(jobId);
        if(!(job.state()==AdmFileJobState.FAILED||job.state()==AdmFileJobState.PARTIAL_FAILED))
            throw new IllegalStateException("확정 실패 Job만 재시도할 수 있습니다. 결과 불명은 먼저 운영 확인해야 합니다.");
        if(job.errorCode()!=null&&(job.errorCode().startsWith("ROLLBACK_")||job.errorCode().startsWith("COMPENSATION_")))
            throw new IllegalStateException("보상 미완료 Job은 재시도가 아니라 Rollback을 실행해야 합니다.");
        if(repository.rows(jobId).stream().anyMatch(row->APPLY_UNKNOWN_STATES.contains(row.state())||ROLLBACK_UNKNOWN_STATES.contains(row.state())))
            throw new IllegalStateException("결과 불명 행이 남아 있어 재시도할 수 없습니다.");
        AdmFileJobState next=job.successRows()>0?AdmFileJobState.APPLYING:AdmFileJobState.RECEIVED;
        if(!repository.transitionControl(jobId,job.state(),next,operator,reason,approvalId,
                AdmFileJobRepository.ControlActor.NONE,null,null))
            throw new IllegalStateException("File Job 상태가 변경되었습니다.");
        return repository.get(jobId).response();
    }

    AdmFileJobResponse cancel(String jobId,String operator,String reason,String approvalId){
        requireText(operator,"operator");requireText(reason,"reason");requireText(approvalId,"approvalId");
        var job=repository.get(jobId);
        if(!(job.state()==AdmFileJobState.RECEIVED||job.state()==AdmFileJobState.VALIDATED
                ||job.state()==AdmFileJobState.READY_TO_APPLY))
            throw new IllegalStateException("대기 또는 검증 완료 상태에서만 취소할 수 있습니다.");
        if(!repository.transitionControl(jobId,job.state(),AdmFileJobState.CANCELLED,operator,reason,approvalId,
                AdmFileJobRepository.ControlActor.NONE,null,null))
            throw new IllegalStateException("File Job 상태가 변경되었습니다.");
        return repository.get(jobId).response();
    }

    /** Rollback은 Scheduler Worker가 Lease/Fencing을 획득한 뒤 수행합니다. */
    AdmFileJobResponse rollback(String jobId,String operator,String reason,String approvalId){
        requireText(operator,"operator");requireText(reason,"reason");requireText(approvalId,"approvalId");
        var job=repository.get(jobId);
        if(operator.equals(job.requestedBy()))throw new IllegalArgumentException("요청자와 Rollback 운영자는 분리해야 합니다.");
        if(!job.rollbackSupported()||!(job.state()==AdmFileJobState.COMPLETED||job.state()==AdmFileJobState.PARTIAL_FAILED))
            throw new IllegalStateException("Rollback 가능한 완료 Job이 아닙니다.");
        if(!repository.transitionControl(jobId,job.state(),AdmFileJobState.ROLLING_BACK,operator,reason,approvalId,
                AdmFileJobRepository.ControlActor.NONE,null,null))
            throw new IllegalStateException("File Job 상태가 변경되었습니다.");
        return repository.get(jobId).response();
    }

    AdmFileJobResponse resolveUnknown(String jobId,long rowNumber,UnknownResolution resolution,
            String businessKey,String rollbackToken,String operator,String reason,String approvalId){
        requireText(operator,"operator");requireText(reason,"reason");requireText(approvalId,"approvalId");
        Objects.requireNonNull(resolution,"resolution");
        AdmFileJobResponse result=transactions.execute(status->{
            var job=repository.get(jobId);
            if(operator.equals(job.requestedBy()))throw new IllegalArgumentException("요청자와 결과 확정 운영자는 분리해야 합니다.");
            if(job.state()!=AdmFileJobState.UNKNOWN_RESULT)throw new IllegalStateException("결과 불명 Job만 확정할 수 있습니다.");
            var row=repository.rows(jobId).stream().filter(r->r.rowNumber()==rowNumber).findFirst()
                    .orElseThrow(()->new IllegalArgumentException("대상 행을 찾을 수 없습니다."));
            boolean rollbackUnknown=ROLLBACK_UNKNOWN_STATES.contains(row.state());
            boolean applyUnknown=APPLY_UNKNOWN_STATES.contains(row.state());
            if(!rollbackUnknown&&!applyUnknown)throw new IllegalStateException("결과 불명 행이 아닙니다.");
            String nextRowState;
            String nextBusinessKey=blank(businessKey)?row.businessKey():businessKey.trim();
            String nextRollbackToken=blank(rollbackToken)?row.rollbackToken():rollbackToken.trim();
            String resolutionCode;
            switch(resolution){
                case SIDE_EFFECT_NOT_APPLIED -> {
                    nextRowState=rollbackUnknown?"APPLIED":"FAILED";
                    resolutionCode=rollbackUnknown?"OPERATOR_CONFIRMED_ROLLBACK_NOT_APPLIED":"OPERATOR_CONFIRMED_NOT_APPLIED";
                }
                case SIDE_EFFECT_APPLIED -> {
                    nextRowState=rollbackUnknown?"ROLLED_BACK":"APPLIED";
                    if(!rollbackUnknown)requireText(nextRollbackToken,"rollbackToken");
                    resolutionCode=rollbackUnknown?"OPERATOR_CONFIRMED_ROLLBACK_APPLIED":"OPERATOR_CONFIRMED_APPLIED";
                }
                case SIDE_EFFECT_COMPENSATED -> {
                    nextRowState="ROLLED_BACK";
                    resolutionCode="OPERATOR_CONFIRMED_COMPENSATED";
                }
                default -> throw new IllegalStateException("지원하지 않는 결과 불명 확정 유형입니다.");
            }
            repository.resolveUnknownRow(jobId,rowNumber,row.state(),nextRowState,nextBusinessKey,
                    resolutionCode,safe(reason),nextRollbackToken);
            var rows=repository.rows(jobId);
            long success=count(rows,"APPLIED");
            long failed=count(rows,"FAILED");
            boolean unresolved=rows.stream().anyMatch(r->APPLY_UNKNOWN_STATES.contains(r.state())||ROLLBACK_UNKNOWN_STATES.contains(r.state()));
            boolean rollbackContext=rollbackUnknown||"ROLLBACK_RESULT_UNKNOWN".equals(job.errorCode())
                    ||"COMPENSATION_RESULT_UNKNOWN".equals(job.errorCode());
            AdmFileJobState next;
            String errorCode=null;String errorMessage=null;
            if(unresolved){
                next=AdmFileJobState.UNKNOWN_RESULT;errorCode="UNKNOWN_RESULT_REMAINS";errorMessage="확정되지 않은 행이 남아 있습니다.";
            }else if(rollbackContext){
                boolean allRolledBack=rows.stream().allMatch(r->"ROLLED_BACK".equals(r.state())||"FAILED".equals(r.state()));
                next=allRolledBack?AdmFileJobState.ROLLED_BACK:AdmFileJobState.PARTIAL_FAILED;
                if(!allRolledBack){errorCode="ROLLBACK_NOT_COMPLETED";errorMessage="Rollback 미완료 행이 남아 재실행 또는 보상이 필요합니다.";}
            }else{
                next=failed>0||success<rows.size()?AdmFileJobState.PARTIAL_FAILED:AdmFileJobState.COMPLETED;
                if(next==AdmFileJobState.PARTIAL_FAILED){errorCode="APPLY_PARTIAL_FAILED";errorMessage="적용 실패 행이 남아 있습니다.";}
            }
            repository.resolveUnknownJob(jobId,success,failed,next,errorCode,errorMessage,operator,reason,approvalId);
            return repository.get(jobId).response();
        });
        if(result==null)throw new IllegalStateException("결과 불명 확정 Transaction이 완료되지 않았습니다.");
        return result;
    }

    @Scheduled(fixedDelayString="${cpf.admin.file-job.poll-delay:PT2S}")
    public void work(){
        repository.claim(new LinkedHashSet<>(List.of(AdmFileJobState.VALIDATING,AdmFileJobState.ROLLING_BACK,
                AdmFileJobState.APPLYING,AdmFileJobState.RECEIVED)),workerId,lease).ifPresent(job->{
            if(job.state()==AdmFileJobState.RECEIVED||job.state()==AdmFileJobState.VALIDATING)validate(job);
            else if(job.state()==AdmFileJobState.APPLYING)applyClaimed(job);
            else rollbackClaimed(job);
        });
    }

    @Scheduled(fixedDelayString="${cpf.admin.file-job.cleanup-delay:PT1H}")
    public void cleanup(){
        for(var job:repository.expired(100)){
            if(!repository.beginExpiry(job.jobId()))continue;
            artifacts.delete(job.sourcePath());artifacts.delete(job.resultPath());repository.finalizeExpiry(job.jobId());
        }
    }

    private void validate(AdmFileJobRepository.Job job){
        if(job.state()==AdmFileJobState.RECEIVED&&!repository.transition(job.jobId(),AdmFileJobState.RECEIVED,AdmFileJobState.VALIDATING,null,null))return;
        var claimed=repository.get(job.jobId());
        var template=registry.require(job.templateCode(),job.templateVersion());
        CpfTabularReader reader=readers.stream().filter(r->r.supports(CpfTabularFormat.valueOf(job.format()))).findFirst()
                .orElseThrow(()->new IllegalStateException("지원하는 Tabular Reader가 없습니다: "+job.format()));
        repository.deleteRowsFenced(job.jobId(),claimed.leaseOwner(),claimed.fencingToken());
        try(InputStream in=Files.newInputStream(artifacts.require(job.sourcePath()))){
            AtomicLong rows=new AtomicLong();AtomicLong lastHeartbeat=new AtomicLong(System.nanoTime());
            CpfTabularReadResult result=reader.read(new CpfTabularReadRequest(CpfTabularFormat.valueOf(job.format()),
                    template.schema(),in,true,true),row->{
                repository.addRow(job.jobId(),row.rowNumber(),"VALIDATED",null,row.values(),null,null,null);
                long count=rows.incrementAndGet();
                if(count%250==0||Duration.ofNanos(System.nanoTime()-lastHeartbeat.get()).compareTo(lease.dividedBy(3))>=0){
                    heartbeat(claimed);lastHeartbeat.set(System.nanoTime());
                }
            });
            if(!MessageDigest.isEqual(hex(job.sourceSha256()),hex(result.sha256())))
                throw new SecurityException("SOURCE_CHECKSUM_MISMATCH");
            Path errorPath=null;String errorSha=null;
            if(!result.errors().isEmpty()){errorPath=writeErrorArtifact(job,result.errors());errorSha=sha256File(errorPath);}
            if(job.resultPath()!=null&&!job.resultPath().equals(errorPath==null?null:errorPath.toString()))artifacts.delete(job.resultPath());
            AdmFileJobState state=result.rejectedRows()>0?AdmFileJobState.PARTIAL_FAILED:
                    job.dryRun()?AdmFileJobState.VALIDATED:AdmFileJobState.READY_TO_APPLY;
            heartbeat(claimed);
            repository.complete(job.jobId(),claimed.leaseOwner(),claimed.fencingToken(),state,
                    result.acceptedRows()+result.rejectedRows(),0,result.rejectedRows(),result.sha256(),
                    errorPath==null?null:errorPath.toString(),errorSha);
        }catch(Exception error){
            failClaimed(claimed,"VALIDATION_FAILED",safe(error.getMessage()));
        }
    }

    private void applyClaimed(AdmFileJobRepository.Job job){
        var template=registry.require(job.templateCode(),job.templateVersion());
        var rows=repository.rows(job.jobId());
        if(rows.stream().anyMatch(row->APPLY_UNKNOWN_STATES.contains(row.state()))){
            completeUnknown(job,rows,"UNKNOWN_RESULT","이전 Worker가 Side Effect 결과를 확정하지 못했습니다.");return;
        }
        for(var row:rows){
            if("APPLIED".equals(row.state())||"ROLLED_BACK".equals(row.state()))continue;
            heartbeat(job,dispatchLease);
            leaseTransactions.executeWithoutResult(status->repository.markRowDispatching(job.jobId(),row.rowNumber(),
                    job.leaseOwner(),job.fencingToken(),row.state(),"DISPATCHING"));
            try{
                AdmFileJobConsumer.ApplyResult result=transactions.execute(status->template.consumer().apply(
                        new AdmFileJobConsumer.ApplyCommand(rowOperationId(job,row,"apply"),row.payload(),controlOperator(job),
                                controlReason(job),job.clientIp())));
                if(result==null)throw AdmFileJobDispatchException.unknown("Consumer 결과가 null입니다.",null);
                leaseTransactions.executeWithoutResult(status->repository.updateRowFenced(job.jobId(),row.rowNumber(),
                        job.leaseOwner(),job.fencingToken(),"DISPATCHING","APPLIED",result.businessKey(),null,
                        result.message(),result.rollbackToken()));
            }catch(RuntimeException error){
                AdmFileJobDispatchException.Certainty certainty=certainty(error);
                if(certainty==AdmFileJobDispatchException.Certainty.UNKNOWN){
                    markUnknown(job,row,"DISPATCHING","UNKNOWN_RESULT","SIDE_EFFECT_RESULT_UNKNOWN",error);
                    completeUnknown(job,repository.rows(job.jobId()),"SIDE_EFFECT_RESULT_UNKNOWN",
                            "운영 확인 전 자동 재시도를 금지합니다.");return;
                }
                leaseTransactions.executeWithoutResult(status->repository.updateRowFenced(job.jobId(),row.rowNumber(),
                        job.leaseOwner(),job.fencingToken(),"DISPATCHING","FAILED",row.businessKey(),"SIDE_EFFECT_NOT_APPLIED",
                        safe(error.getMessage()),row.rollbackToken()));
                if(template.atomicApply()){
                    CompensationOutcome outcome=compensateApplied(job,template.consumer());
                    var current=repository.rows(job.jobId());
                    if(outcome==CompensationOutcome.UNKNOWN){
                        completeUnknown(job,current,"COMPENSATION_RESULT_UNKNOWN","자동 보상 결과를 확정할 수 없습니다.");return;
                    }
                    AdmFileJobState state=outcome==CompensationOutcome.COMPLETED?AdmFileJobState.FAILED:AdmFileJobState.PARTIAL_FAILED;
                    String code=outcome==CompensationOutcome.COMPLETED?"APPLY_COMPENSATED":"COMPENSATION_FAILED";
                    repository.complete(job.jobId(),job.leaseOwner(),job.fencingToken(),state,current.size(),0,
                            count(current,"FAILED"),job.sourceSha256(),job.resultPath(),job.resultSha256(),code,
                            outcome==CompensationOutcome.COMPLETED?"적용 실패 후 선행 행을 자동 보상했습니다.":"일부 선행 행의 자동 보상이 실패했습니다.");
                    return;
                }
            }
        }
        var current=repository.rows(job.jobId());
        long failed=count(current,"FAILED");
        AdmFileJobState state=failed==0?AdmFileJobState.COMPLETED:AdmFileJobState.PARTIAL_FAILED;
        heartbeat(job);
        repository.complete(job.jobId(),job.leaseOwner(),job.fencingToken(),state,current.size(),count(current,"APPLIED"),failed,
                job.sourceSha256(),job.resultPath(),job.resultSha256(),failed==0?null:"APPLY_PARTIAL_FAILED",
                failed==0?null:"적용 실패 행이 남아 있습니다.");
    }

    private CompensationOutcome compensateApplied(AdmFileJobRepository.Job job,AdmFileJobConsumer consumer){
        var rows=new ArrayList<>(repository.rows(job.jobId()));Collections.reverse(rows);
        boolean failed=false;
        for(var row:rows){
            if(!"APPLIED".equals(row.state())||blank(row.rollbackToken()))continue;
            heartbeat(job,dispatchLease);
            leaseTransactions.executeWithoutResult(status->repository.markRowDispatching(job.jobId(),row.rowNumber(),
                    job.leaseOwner(),job.fencingToken(),"APPLIED","ROLLBACK_DISPATCHING"));
            try{
                transactions.executeWithoutResult(status->consumer.rollback(new AdmFileJobConsumer.RollbackCommand(
                        rowOperationId(job,row,"compensate"),row.rollbackToken(),controlOperator(job),controlReason(job),job.clientIp())));
                leaseTransactions.executeWithoutResult(status->repository.updateRowFenced(job.jobId(),row.rowNumber(),
                        job.leaseOwner(),job.fencingToken(),"ROLLBACK_DISPATCHING","ROLLED_BACK",row.businessKey(),null,
                        "자동 보상 완료",row.rollbackToken()));
            }catch(RuntimeException error){
                if(certainty(error)==AdmFileJobDispatchException.Certainty.UNKNOWN){
                    markUnknown(job,row,"ROLLBACK_DISPATCHING","ROLLBACK_UNKNOWN_RESULT","COMPENSATION_RESULT_UNKNOWN",error);
                    return CompensationOutcome.UNKNOWN;
                }
                failed=true;
                leaseTransactions.executeWithoutResult(status->repository.updateRowFenced(job.jobId(),row.rowNumber(),
                        job.leaseOwner(),job.fencingToken(),"ROLLBACK_DISPATCHING","APPLIED",row.businessKey(),
                        "COMPENSATION_NOT_APPLIED",safe(error.getMessage()),row.rollbackToken()));
            }
        }
        return failed?CompensationOutcome.PARTIAL_FAILED:CompensationOutcome.COMPLETED;
    }

    private void rollbackClaimed(AdmFileJobRepository.Job job){
        var consumer=registry.require(job.templateCode(),job.templateVersion()).consumer();
        var rows=new ArrayList<>(repository.rows(job.jobId()));Collections.reverse(rows);
        if(rows.stream().anyMatch(row->ROLLBACK_UNKNOWN_STATES.contains(row.state()))){
            completeUnknown(job,rows,"ROLLBACK_RESULT_UNKNOWN","이전 Worker가 Rollback 결과를 확정하지 못했습니다.");return;
        }
        boolean knownFailure=false;
        for(var row:rows){
            if(!"APPLIED".equals(row.state())||blank(row.rollbackToken()))continue;
            heartbeat(job,dispatchLease);
            leaseTransactions.executeWithoutResult(status->repository.markRowDispatching(job.jobId(),row.rowNumber(),
                    job.leaseOwner(),job.fencingToken(),"APPLIED","ROLLBACK_DISPATCHING"));
            try{
                transactions.executeWithoutResult(status->consumer.rollback(new AdmFileJobConsumer.RollbackCommand(
                        rowOperationId(job,row,"rollback"),row.rollbackToken(),controlOperator(job),controlReason(job),job.clientIp())));
                leaseTransactions.executeWithoutResult(status->repository.updateRowFenced(job.jobId(),row.rowNumber(),
                        job.leaseOwner(),job.fencingToken(),"ROLLBACK_DISPATCHING","ROLLED_BACK",row.businessKey(),null,
                        "Rollback 완료",row.rollbackToken()));
            }catch(RuntimeException error){
                if(certainty(error)==AdmFileJobDispatchException.Certainty.UNKNOWN){
                    markUnknown(job,row,"ROLLBACK_DISPATCHING","ROLLBACK_UNKNOWN_RESULT","ROLLBACK_RESULT_UNKNOWN",error);
                    completeUnknown(job,repository.rows(job.jobId()),"ROLLBACK_RESULT_UNKNOWN",
                            "운영 확인 전 Rollback 재실행을 금지합니다.");return;
                }
                knownFailure=true;
                leaseTransactions.executeWithoutResult(status->repository.updateRowFenced(job.jobId(),row.rowNumber(),
                        job.leaseOwner(),job.fencingToken(),"ROLLBACK_DISPATCHING","APPLIED",row.businessKey(),
                        "ROLLBACK_NOT_APPLIED",safe(error.getMessage()),row.rollbackToken()));
            }
        }
        var current=repository.rows(job.jobId());
        AdmFileJobState state=knownFailure?AdmFileJobState.PARTIAL_FAILED:AdmFileJobState.ROLLED_BACK;
        heartbeat(job);
        repository.complete(job.jobId(),job.leaseOwner(),job.fencingToken(),state,current.size(),count(current,"APPLIED"),
                count(current,"FAILED"),job.sourceSha256(),job.resultPath(),job.resultSha256(),
                knownFailure?"ROLLBACK_NOT_COMPLETED":null,knownFailure?"Rollback 미완료 행이 남아 있습니다.":null);
    }

    private void markUnknown(AdmFileJobRepository.Job job,AdmFileJobRepository.Row row,String expected,String next,
                             String code,RuntimeException error){
        leaseTransactions.executeWithoutResult(status->repository.updateRowFenced(job.jobId(),row.rowNumber(),
                job.leaseOwner(),job.fencingToken(),expected,next,row.businessKey(),code,safe(error.getMessage()),row.rollbackToken()));
    }
    private void completeUnknown(AdmFileJobRepository.Job job,List<AdmFileJobRepository.Row> rows,String code,String message){
        heartbeat(job);
        repository.complete(job.jobId(),job.leaseOwner(),job.fencingToken(),AdmFileJobState.UNKNOWN_RESULT,
                rows.size(),count(rows,"APPLIED"),count(rows,"FAILED"),job.sourceSha256(),job.resultPath(),job.resultSha256(),code,message);
    }
    private void failClaimed(AdmFileJobRepository.Job job,String code,String message){
        try{
            heartbeat(job);
            repository.complete(job.jobId(),job.leaseOwner(),job.fencingToken(),AdmFileJobState.FAILED,
                    0,0,0,job.sourceSha256(),null,null,code,message);
        }catch(RuntimeException leaseLost){
            throw new IllegalStateException("File Job 실패 상태 기록 중 lease/fencing을 잃었습니다.",leaseLost);
        }
    }
    private void heartbeat(AdmFileJobRepository.Job job){heartbeat(job,lease);}
    private void heartbeat(AdmFileJobRepository.Job job,Duration duration){
        leaseTransactions.executeWithoutResult(status->repository.heartbeat(job.jobId(),job.leaseOwner(),job.fencingToken(),duration));
    }
    private void requireApproval(AdmFileJobRepository.Job job,AdmFileTemplateRegistry.Template template,String operator,String approvalId){
        if(!template.approvalRequired())return;
        requireText(approvalId,"approvalId");
        if(operator.equals(job.requestedBy()))throw new IllegalArgumentException("요청자와 적용 운영자는 분리해야 합니다.");
    }
    private AdmFileJobDispatchException.Certainty certainty(RuntimeException error){
        for(Throwable current=error;current!=null;current=current.getCause()){
            if(current instanceof AdmFileJobDispatchException dispatch)return dispatch.certainty();
        }
        return AdmFileJobDispatchException.Certainty.UNKNOWN;
    }
    private long count(List<AdmFileJobRepository.Row> rows,String state){return rows.stream().filter(r->state.equals(r.state())).count();}
    private Path writeErrorArtifact(AdmFileJobRepository.Job job,List<CpfTabularReadResult.RowError> errors)throws IOException{
        Path path=artifacts.createResult(job.jobId(),"errors.csv");
        try(BufferedWriter writer=Files.newBufferedWriter(path,StandardCharsets.UTF_8,StandardOpenOption.TRUNCATE_EXISTING)){
            writer.write("row,column,code,message");writer.newLine();
            for(var error:errors){writer.write(error.rowNumber()+","+csv(error.column())+","+csv(error.code())+","+csv(safe(error.message())));writer.newLine();}
        }return path;
    }
    private String rowOperationId(AdmFileJobRepository.Job job,AdmFileJobRepository.Row row,String phase){
        return job.operationId()+":"+row.rowNumber()+":"+phase;
    }
    private String controlOperator(AdmFileJobRepository.Job job){return blank(job.controlBy())?job.requestedBy():job.controlBy();}
    private String controlReason(AdmFileJobRepository.Job job){return blank(job.controlReason())?job.reason():job.controlReason();}
    private String csv(String value){String v=value==null?"":value.replace("\"","\"\"");return "\""+v+"\"";}
    private String safe(String value){
        if(value==null)return "";
        String sanitized=value
                .replaceAll("(?i)(password|passwd|pwd|token|secret|authorization|api[-_]?key)\\s*[:=]\\s*([^,;\\s}]+)","$1=[REDACTED]")
                .replaceAll("(?i)bearer\\s+[A-Za-z0-9._~+/=-]+","Bearer [REDACTED]")
                .replaceAll("[\\r\\n\\t]+"," ");
        return sanitized.substring(0,Math.min(sanitized.length(),1000));
    }
    private String sha256(String value){try{return java.util.HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)));}catch(Exception e){throw new
            IllegalStateException(e);}}
    private String sha256File(Path path){
        try(InputStream raw=Files.newInputStream(path)){
            MessageDigest digest=MessageDigest.getInstance("SHA-256");
            try(DigestInputStream in=new DigestInputStream(raw,digest)){in.transferTo(OutputStream.nullOutputStream());}
            return java.util.HexFormat.of().formatHex(digest.digest());
        }catch(Exception e){throw new IllegalStateException(e);}
    }
    private byte[] hex(String value){try{return java.util.HexFormat.of().parseHex(value);}catch(Exception e){throw new IllegalArgumentException("SHA-256 형식이 올바르지 않습니다.",e);}}
    private void requireText(String value,String name){if(value==null||value.isBlank())throw new IllegalArgumentException(name+"는 필수입니다.");}
    private boolean blank(String value){return value==null||value.isBlank();}
    private enum CompensationOutcome { COMPLETED, PARTIAL_FAILED, UNKNOWN }
    public enum UnknownResolution {SIDE_EFFECT_NOT_APPLIED,SIDE_EFFECT_APPLIED,SIDE_EFFECT_COMPENSATED}
}
