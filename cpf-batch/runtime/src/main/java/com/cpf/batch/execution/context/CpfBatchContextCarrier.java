package com.cpf.batch.execution.context;

import com.cpf.batch.context.CpfBatchContext;
import com.cpf.batch.context.CpfBatchContextBundle;
import com.cpf.batch.context.CpfBatchLaunchMode;
import com.cpf.core.api.context.CpfContext;
import com.cpf.core.api.context.CpfContextSnapshot;
import com.cpf.foundation.id.spi.CpfExecutionIdGenerator;
import java.time.Instant;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/** Bounded remote manager/worker carrier. Core와 Batch Owner metadata를 명시적으로 분리하여 전달합니다. */
public final class CpfBatchContextCarrier {
    private final CpfExecutionIdGenerator executionIds;
    public CpfBatchContextCarrier(CpfExecutionIdGenerator executionIds) { this.executionIds=Objects.requireNonNull(executionIds); }

    public Map<String,String> inject(CpfBatchContextBundle bundle) {
        Objects.requireNonNull(bundle,"bundle"); CpfContext e=bundle.snapshot().context(); CpfBatchContext b=bundle.batch();
        Map<String,String> m=new LinkedHashMap<>();
        put(m,"cpfCtxTx",e.transaction().transactionId()); put(m,"cpfCtxRootTx",e.transaction().rootTransactionId());
        put(m,"cpfCtxCorrelation",e.transaction().correlationId()); put(m,"cpfCtxBusinessDate",e.transaction().businessDate().toString());
        put(m,"cpfCtxTxStarted",e.transaction().startedAt().toString()); put(m,"cpfCtxStandardExec",e.execution().standardExecutionId());
        put(m,"cpfCtxRootExec",e.execution().rootExecutionId()); put(m,"cpfCtxParentExec",e.execution().executionId());
        put(m,"cpfCtxParentSeg",e.execution().segmentId()); put(m,"cpfCtxAttempt",Integer.toString(e.execution().attempt()));
        if(e.execution().deadline()!=null) put(m,"cpfCtxDeadline",e.execution().deadline().toString());
        if(e.tenant()!=null) put(m,"cpfCtxTenant",e.tenant().tenantId());
        put(m,"cpfCtxBatchJob",b.jobName()); put(m,"cpfCtxBatchJobExec",b.jobExecutionId());
        put(m,"cpfCtxBatchOriginalJobExec",b.originalJobExecutionId()); put(m,"cpfCtxBatchStep",b.stepName());
        put(m,"cpfCtxBatchStepExec",b.stepExecutionId()); put(m,"cpfCtxBatchPartition",b.partitionId());
        put(m,"cpfCtxBatchCheckpoint",b.checkpointId()); put(m,"cpfCtxBatchWorker",b.workerId());
        put(m,"cpfCtxBatchWorkerGroup",b.workerGroup()); put(m,"cpfCtxBatchRecovery",b.recoveryId());
        put(m,"cpfCtxBatchUnknown",b.unknownOutcomeId()); put(m,"cpfCtxBatchFencing",Long.toString(b.fencingToken()));
        if(m.size()>32) throw new IllegalStateException("batch context carrier exceeds key budget");
        int bytes=m.entrySet().stream().mapToInt(x->x.getKey().length()+x.getValue().length()).sum();
        if(bytes>4096) throw new IllegalStateException("batch context carrier exceeds byte budget");
        return Map.copyOf(m);
    }

    public CpfBatchContextBundle restore(Map<String,String> m) {
        Objects.requireNonNull(m,"carrier"); String tx=req(m,"cpfCtxTx"); LocalDate businessDate=LocalDate.parse(req(m,"cpfCtxBusinessDate"));
        int attempt=parsePositive(m.get("cpfCtxAttempt"),1); Instant deadline=instant(m.get("cpfCtxDeadline"));
        String executionId=executionIds.newExecutionId(); String rootExec=text(m.get("cpfCtxRootExec")); if(rootExec==null) rootExec=executionId;
        CpfContext.CpfTenantContext tenant=text(m.get("cpfCtxTenant"))==null?null:new CpfContext.CpfTenantContext(m.get("cpfCtxTenant"));
        CpfContext core=new CpfContext(
                new CpfContext.CpfTransactionContext(tx,defaultText(m.get("cpfCtxRootTx"),tx),null,m.get("cpfCtxCorrelation"),businessDate,
                        defaultInstant(m.get("cpfCtxTxStarted"),Instant.now()),CpfContext.CpfTransactionOriginKind.BATCH,"cpf-batch-remote",null),
                new CpfContext.CpfExecutionContext(m.get("cpfCtxStandardExec"),executionId,rootExec,m.get("cpfCtxParentExec"),executionIds.newSegmentId(),m.get("cpfCtxParentSeg"),
                        CpfContext.CpfExecutionType.BATCH,attempt,1,Instant.now(),deadline,CpfContext.CpfCancellationMode.DEADLINE_ENFORCED),
                null,null,tenant);
        CpfBatchContext batch=new CpfBatchContext(defaultText(m.get("cpfCtxBatchJob"),"remote-batch"),defaultText(m.get("cpfCtxBatchJob"),"remote-batch"),1,null,
                m.get("cpfCtxBatchJobExec"),m.get("cpfCtxBatchOriginalJobExec"),m.get("cpfCtxBatchStep"),m.get("cpfCtxBatchStepExec"),null,null,
                CpfBatchLaunchMode.REMOTE_PARTITION,businessDate,0,attempt,m.get("cpfCtxBatchPartition"),null,null,null,m.get("cpfCtxBatchWorker"),m.get("cpfCtxBatchWorkerGroup"),null,
                m.get("cpfCtxBatchCheckpoint"),null,m.get("cpfCtxBatchRecovery"),m.get("cpfCtxBatchUnknown"),parseLong(m.get("cpfCtxBatchFencing"),0L),Instant.now());
        return new CpfBatchContextBundle(CpfContextSnapshot.capture(core),batch);
    }

    public static Map<String,String> fromWireHeaders(Map<String,Object> headers){Map<String,String> r=new LinkedHashMap<>();if(headers!=null)headers.forEach((k,v)->{if(k!=null&&k.startsWith("cpfCtx")&&v!=null)r.put(k,String.valueOf(v));});return Map.copyOf(r);}
    private static void put(Map<String,String> m,String k,String v){if(text(v)!=null)m.put(k,v.trim());}
    private static String req(Map<String,String> m,String k){String v=text(m.get(k));if(v==null)throw new IllegalArgumentException("missing "+k);return v;}
    private static String text(String s){return s==null||s.isBlank()?null:s.trim();}
    private static String defaultText(String v,String d){String x=text(v);return x==null?d:x;}
    private static Instant instant(String v){return text(v)==null?null:Instant.parse(v);}
    private static Instant defaultInstant(String v,Instant d){Instant x=instant(v);return x==null?d:x;}
    private static int parsePositive(String v,int d){try{return Math.max(1,Integer.parseInt(v));}catch(Exception e){return d;}}
    private static long parseLong(String v,long d){try{return Long.parseLong(v);}catch(Exception e){return d;}}
}
