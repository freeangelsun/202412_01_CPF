package com.cpf.admin.opr.batch;

import com.cpf.core.api.batch.CpfBatchOperationsPort;
import com.cpf.core.api.batch.CpfBatchOwnerUnknownResultException;
import com.cpf.core.api.servicecall.CpfServiceCaller;
import com.cpf.core.api.servicecall.CpfServiceRequest;
import com.cpf.core.api.servicecall.CpfServiceResult;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

/**
 * 분리 WAS topology에서 ADM이 BAT Owner 계약을 호출하는 Remote Adapter.
 * CPF 공개 ServiceCall API를 거치므로 Registry/Timeout/Retry/Failover/Trace/Unknown 규칙을 재사용합니다.
 */
public class RemoteCpfBatchOperationsAdapter implements CpfBatchOperationsPort {
    private static final String SERVICE_ID = "BAT";
    private static final String ENDPOINT_CODE = "SBATOP0001";
    private final CpfServiceCaller caller;
    private final WebClient webClient;

    public RemoteCpfBatchOperationsAdapter(CpfServiceCaller caller, WebClient.Builder webClientBuilder) {
        this.caller = caller;
        this.webClient = webClientBuilder.build();
    }

    private Object invoke(String operation, Map<String,Object> payload) {
        String path = "/bat/internal/operations/" + operation;
        CpfServiceRequest request = CpfServiceRequest.builder(SERVICE_ID)
                .endpointCode(ENDPOINT_CODE).httpMethod("POST").requestPath(path)
                .attribute("ownerDomain", "BAT").attribute("callerDomain", "ADM").build();
        CpfServiceResult<Object> result = caller.invoke(request, target -> webClient.post()
                .uri(join(target.baseUrl(), path))
                .bodyValue(payload == null ? Map.of() : payload)
                .retrieve().bodyToMono(Object.class).block());
        if (result.unknown()) {
            throw new CpfBatchOwnerUnknownResultException(
                    result.failureCode(),
                    "BAT Owner 호출 결과를 확정할 수 없습니다. reconciliation 필요. code="
                            + result.failureCode() + ", message=" + result.failureMessage());
        }
        if (!result.success()) {
            throw new IllegalStateException("BAT Owner 호출 실패 status=" + result.status()
                    + ", code=" + result.failureCode() + ", message=" + result.failureMessage());
        }
        return result.responseBody();
    }

    private String join(String base, String path) {
        if (base == null || base.isBlank()) throw new IllegalStateException("BAT service baseUrl을 확인할 수 없습니다.");
        return base.endsWith("/") ? base.substring(0, base.length()-1) + path : base + path;
    }

    @SuppressWarnings("unchecked") private List<Map<String,Object>> list(Object v){return v==null?List.of():(List<Map<String,Object>>)v;}
    @SuppressWarnings("unchecked") private Map<String,Object> map(Object v){return v==null?Map.of():(Map<String,Object>)v;}
    private Map<String,Object> p(Object... kv){java.util.LinkedHashMap<String,Object> m=new java.util.LinkedHashMap<>();for(int i=0;i<kv.length;i+=2)if(kv[i+1]!=null)m.put(String.valueOf(kv[i]),kv[i+1]);return m;}

    public List<Map<String,Object>> findJobs(){return list(invoke("findJobs",Map.of()));}
    public Map<String,Object> findJobDetail(String jobId){return map(invoke("findJobDetail",p("jobId",jobId)));}
    public List<Map<String,Object>> findSchedules(){return list(invoke("findSchedules",Map.of()));}
    public List<Map<String,Object>> findExecutions(
            String jobId,String transactionId,Long springBatchJobInstanceId,
            String workerId,String serverInstanceId,int limit){
        return findExecutions(jobId, transactionId, springBatchJobInstanceId, workerId, serverInstanceId, null, null, limit);
    }
    public List<Map<String,Object>> findExecutions(
            String jobId,String transactionId,Long springBatchJobInstanceId,
            String workerId,String serverInstanceId,String fromDate,String toDate,int limit){
        return list(invoke("findExecutions",p(
                "jobId",jobId,
                "transactionId",transactionId,
                "springBatchJobInstanceId",springBatchJobInstanceId,
                "workerId",workerId,
                "serverInstanceId",serverInstanceId,
                "fromDate",fromDate,
                "toDate",toDate,
                "limit",limit)));
    }
    public Map<String,Object> findExecutionDetail(long executionId){return map(invoke("findExecutionDetail",p("executionId",executionId)));}
    public List<Map<String,Object>> findInstances(){return list(invoke("findInstances",Map.of()));}
    public List<Map<String,Object>> findWorkers(int timeout){return list(invoke("findWorkers",p("heartbeatTimeoutSeconds",timeout)));}
    public List<Map<String,Object>> findStepExecutions(Long executionId,String jobId,int limit){return list(invoke("findStepExecutions",p("executionId",executionId,"jobId",jobId,"limit",limit)));}
    public List<Map<String,Object>> findRelations(String jobId){return list(invoke("findRelations",p("jobId",jobId)));}
    public List<Map<String,Object>> findExecutionTargets(String jobId,String status,int limit){return list(invoke("findExecutionTargets",p("jobId",jobId,"dispatchStatus",status,"limit",limit)));}
    public List<Map<String,Object>> findLocks(String jobId){return list(invoke("findLocks",p("jobId",jobId)));}
    public Map<String,Object> releaseLock(String key,String user,String reason){return map(invoke("releaseLock",p("lockKey",key,"requestUser",user,"reason",reason)));}
    public List<Map<String,Object>> findGhostCandidates(int timeout){return list(invoke("findGhostCandidates",p("heartbeatTimeoutSeconds",timeout)));}
    public Map<String,Object> actGhostExecution(long id,String action,String user,String reason){return map(invoke("actGhostExecution",p("executionId",id,"actionType",action,"requestUser",user,"reason",reason)));}
    public List<Map<String,Object>> findOperationLogs(String jobId,Long executionId,int limit){return list(invoke("findOperationLogs",p("jobId",jobId,"executionId",executionId,"limit",limit)));}
    public List<Map<String,Object>> simulateSchedule(String id,String base,int days){return list(invoke("simulateSchedule",p("scheduleId",id,"baseDate",base,"days",days)));}
    public Map<String,Object> registerJob(String id,String name,String type,String desc,String user){return map(invoke("registerJob",p("jobId",id,"jobName",name,"jobType",type,"description",desc,"requestUser",user)));}
    public Map<String,Object> requestRun(String jobId,String params,String user,String reason){return map(invoke("requestRun",p("jobId",jobId,"jobParameters",params,"requestUser",user,"reason",reason)));}
    public Map<String,Object> requestScheduledRun(String schedule,String job,String params,String user,String reason){return map(invoke("requestScheduledRun",p("scheduleId",schedule,"jobId",job,"jobParameters",params,"requestUser",user,"reason",reason)));}
    public Map<String,Object> requestRetry(long id,String user,String reason){return map(invoke("requestRetry",p("executionId",id,"requestUser",user,"reason",reason)));}
    public Map<String,Object> requestStop(long id,String user,String reason){return map(invoke("requestStop",p("executionId",id,"requestUser",user,"reason",reason)));}
    public Map<String,Object> updateScheduleEnabled(String id,boolean enabled,String user,String reason){return map(invoke("updateScheduleEnabled",p("scheduleId",id,"enabled",enabled,"requestUser",user,"reason",reason)));}
    public List<Map<String,Object>> runSchedulerOnce(String user){return list(invoke("runSchedulerOnce",p("requestUser",user)));}
}
