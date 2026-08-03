package com.cpf.admin.opr.batch;

import com.cpf.admin.opr.context.AdmAuthenticatedOperatorContext;
import com.cpf.core.api.batch.CpfBatchOperationsPort;
import com.cpf.core.api.batch.CpfBatchRiskCommand;
import com.cpf.core.api.data.CpfDataRow;
import com.cpf.core.api.batch.CpfBatchOwnerUnknownResultException;
import com.cpf.core.api.servicecall.CpfServiceCaller;
import com.cpf.core.api.servicecall.CpfServiceRequest;
import com.cpf.core.api.servicecall.CpfServiceResult;
import com.cpf.core.api.util.CpfHeaders;
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
    private static final String CALLER_SERVICE = "ADM";
    private final CpfServiceCaller caller;
    private final WebClient webClient;
    private final AdmAuthenticatedOperatorContext operatorContext;
    private final String callerInstanceId;

    public RemoteCpfBatchOperationsAdapter(
            CpfServiceCaller caller,
            WebClient.Builder webClientBuilder,
            AdmAuthenticatedOperatorContext operatorContext,
            String callerInstanceId) {
        this.caller = caller;
        this.webClient = webClientBuilder.build();
        this.operatorContext = operatorContext;
        this.callerInstanceId = requireOperator(callerInstanceId);
    }

    private Object invokeRead(String operation, Map<String, Object> payload) {
        return invoke(operation, payload, operatorContext.currentOperatorId());
    }

    private Object invoke(String operation, Map<String,Object> payload, String operatorId) {
        return invoke(operation, payload, operatorId, null);
    }

    private Object invokeRisk(String operation, Map<String,Object> payload, CpfBatchRiskCommand command) {
        java.util.LinkedHashMap<String,Object> body = new java.util.LinkedHashMap<>();
        if (payload != null) body.putAll(payload);
        body.put("operation", command.operation());
        body.put("targetType", command.targetType());
        body.put("targetId", command.targetId());
        body.put("actionType", command.actionType());
        body.put("requestUser", command.requestUser());
        body.put("reason", command.reason());
        body.put("approvalRequestId", command.approvalRequestId());
        body.put("idempotencyKey", command.idempotencyKey());
        body.put("expectedVersion", command.expectedVersion());
        body.put("payload", command.payload());
        body.put("requestHash", command.fingerprint());
        return invoke(operation, body, command.requestUser(), command);
    }

    private Object invoke(
            String operation,
            Map<String,Object> payload,
            String operatorId,
            CpfBatchRiskCommand riskCommand) {
        String verifiedOperator = requireOperator(operatorId);
        String path = "/bat/internal/operations/" + operation;
        CpfServiceRequest.Builder requestBuilder = CpfServiceRequest.builder(SERVICE_ID)
                .endpointCode(ENDPOINT_CODE).httpMethod("POST").requestPath(path)
                .header(CpfHeaders.callerService(), CALLER_SERVICE)
                .header(CpfHeaders.callerInstanceId(), callerInstanceId)
                .header(CpfHeaders.operatorId(), verifiedOperator);
        if (riskCommand != null) {
            requestBuilder.header(CpfHeaders.idempotencyKey(), riskCommand.idempotencyKey())
                    .header(CpfHeaders.approvalRequestId(), riskCommand.approvalRequestId())
                    .header(CpfHeaders.approvalRequesterId(), riskCommand.requestUser());
        }
        CpfServiceRequest request = requestBuilder
                .attribute("ownerDomain", "BAT").attribute("callerDomain", "ADM").build();
        CpfServiceResult<Object> result = caller.invoke(request, target -> webClient.post()
                .uri(join(target.baseUrl(), path))
                .headers(headers -> {
                    headers.set(CpfHeaders.callerService(), CALLER_SERVICE);
                    headers.set(CpfHeaders.callerInstanceId(), callerInstanceId);
                    headers.set(CpfHeaders.operatorId(), verifiedOperator);
                    if (riskCommand != null) {
                        headers.set(CpfHeaders.idempotencyKey(), riskCommand.idempotencyKey());
                        headers.set(CpfHeaders.approvalRequestId(), riskCommand.approvalRequestId());
                        headers.set(CpfHeaders.approvalRequesterId(), riskCommand.requestUser());
                    }
                })
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

    private static String requireOperator(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("BAT Owner 호출에는 검증된 ADM operator가 필요합니다.");
        }
        return value.trim();
    }

    private List<CpfDataRow> list(Object value) {
        if (value == null) {
            throw new IllegalStateException("BAT Owner 목록 응답 본문이 없습니다.");
        }
        return CpfDataRow.copyRows(value);
    }
    private CpfDataRow map(Object value) {
        if (value == null) {
            throw new IllegalStateException("BAT Owner 상세 응답 본문이 없습니다.");
        }
        return CpfDataRow.copyOf(value);
    }
    private Map<String,Object> p(Object... kv){java.util.LinkedHashMap<String,Object> m=new java.util.LinkedHashMap<>();for(int i=0;i<kv.length;i+=2)if(kv[i+1]!=null)m.put(String.valueOf(kv[i]),kv[i+1]);return m;}

    public List<CpfDataRow> findJobs(){return list(invokeRead("findJobs",Map.of()));}
    public CpfDataRow findJobDetail(String jobId){return map(invokeRead("findJobDetail",p("jobId",jobId)));}
    public List<CpfDataRow> findSchedules(){return list(invokeRead("findSchedules",Map.of()));}
    public List<CpfDataRow> findExecutions(
            String jobId,String transactionId,Long springBatchJobInstanceId,
            String workerId,String serverInstanceId,int limit){
        return findExecutions(jobId, transactionId, springBatchJobInstanceId, workerId, serverInstanceId, null, null, limit);
    }
    public List<CpfDataRow> findExecutions(
            String jobId,String transactionId,Long springBatchJobInstanceId,
            String workerId,String serverInstanceId,String fromDate,String toDate,int limit){
        return list(invokeRead("findExecutions",p(
                "jobId",jobId,
                "transactionId",transactionId,
                "springBatchJobInstanceId",springBatchJobInstanceId,
                "workerId",workerId,
                "serverInstanceId",serverInstanceId,
                "fromDate",fromDate,
                "toDate",toDate,
                "limit",limit)));
    }
    public CpfDataRow findExecutionPage(
            String jobId,String transactionId,Long springBatchJobInstanceId,
            String workerId,String serverInstanceId,String status,
            String fromDate,String toDate,int page,int size){
        return map(invokeRead("findExecutionPage",p(
                "jobId",jobId,
                "transactionId",transactionId,
                "springBatchJobInstanceId",springBatchJobInstanceId,
                "workerId",workerId,
                "serverInstanceId",serverInstanceId,
                "status",status,
                "fromDate",fromDate,
                "toDate",toDate,
                "page",page,
                "size",size)));
    }
    public CpfDataRow findExecutionDetail(long executionId){return map(invokeRead("findExecutionDetail",p("executionId",executionId)));}
    public List<CpfDataRow> findInstances(){return list(invokeRead("findInstances",Map.of()));}
    public List<CpfDataRow> findWorkers(int timeout){return list(invokeRead("findWorkers",p("heartbeatTimeoutSeconds",timeout)));}
    public List<CpfDataRow> findStepExecutions(Long executionId,String jobId,int limit){return list(invokeRead("findStepExecutions",p("executionId",executionId,"jobId",jobId,"limit",limit)));}
    public List<CpfDataRow> findRelations(String jobId){return list(invokeRead("findRelations",p("jobId",jobId)));}
    public List<CpfDataRow> findExecutionTargets(String jobId,String status,int limit){return list(invokeRead("findExecutionTargets",p("jobId",jobId,"dispatchStatus",status,"limit",limit)));}
    public List<CpfDataRow> findLocks(String jobId){return list(invokeRead("findLocks",p("jobId",jobId)));}
    public CpfDataRow releaseLock(String key,String user,String reason){throw expectedVersionRequired("releaseLock");}
    public CpfDataRow releaseLock(String key,String user,String reason,long expectedVersion){throw riskMetadataRequired("releaseLock");}
    public CpfDataRow releaseLock(String key,CpfBatchRiskCommand command){return map(invokeRisk("releaseLock",p("lockKey",key),command));}
    public List<CpfDataRow> findGhostCandidates(int timeout){return list(invokeRead("findGhostCandidates",p("heartbeatTimeoutSeconds",timeout)));}
    public CpfDataRow actGhostExecution(long id,String action,String user,String reason){throw expectedVersionRequired("actGhostExecution");}
    public CpfDataRow actGhostExecution(long id,String action,String user,String reason,long expectedVersion){throw riskMetadataRequired("actGhostExecution");}
    public CpfDataRow actGhostExecution(long id,String action,CpfBatchRiskCommand command){return map(invokeRisk("actGhostExecution",p("executionId",id,"actionType",action),command));}
    public List<CpfDataRow> findOperationLogs(String jobId,Long executionId,int limit){return list(invokeRead("findOperationLogs",p("jobId",jobId,"executionId",executionId,"limit",limit)));}
    public List<CpfDataRow> simulateSchedule(String id,String base,int days){return list(invokeRead("simulateSchedule",p("scheduleId",id,"baseDate",base,"days",days)));}
    public CpfDataRow registerJob(String id,String name,String type,String desc,String user){return map(invoke("registerJob",p("jobId",id,"jobName",name,"jobType",type,"description",desc,"requestUser",user),user));}
    public CpfDataRow requestRun(String jobId,String params,String user,String reason){throw riskMetadataRequired("requestRun");}
    public CpfDataRow requestScheduledRun(String schedule,String job,String params,String user,String reason){return map(invoke("requestScheduledRun",p("scheduleId",schedule,"jobId",job,
            "jobParameters",params,"requestUser",user,"reason",reason),user));}
    public CpfDataRow requestRetry(long id,String user,String reason){throw expectedVersionRequired("requestRetry");}
    public CpfDataRow requestRetry(long id,String user,String reason,long expectedVersion){throw riskMetadataRequired("requestRetry");}
    public CpfDataRow requestRetry(long id,CpfBatchRiskCommand command){return map(invokeRisk("requestRetry",p("executionId",id),command));}
    public CpfDataRow requestStop(long id,String user,String reason){throw expectedVersionRequired("requestStop");}
    public CpfDataRow requestStop(long id,String user,String reason,long expectedVersion){throw riskMetadataRequired("requestStop");}
    public CpfDataRow requestStop(long id,CpfBatchRiskCommand command){return map(invokeRisk("requestStop",p("executionId",id),command));}
    public CpfDataRow updateScheduleEnabled(String id,boolean enabled,String user,String reason){throw expectedVersionRequired("updateScheduleEnabled");}
    public CpfDataRow updateScheduleEnabled(String id,boolean enabled,String user,String reason,long expectedVersion){throw riskMetadataRequired("updateScheduleEnabled");}
    public CpfDataRow updateScheduleEnabled(String id,boolean enabled,CpfBatchRiskCommand command){return map(invokeRisk("updateScheduleEnabled",p("scheduleId",id,"enabled",enabled),command));}
    public List<CpfDataRow> runSchedulerOnce(String user){throw riskMetadataRequired("runSchedulerOnce");}
    public List<CpfDataRow> runSchedulerOnce(CpfBatchRiskCommand command){return list(invokeRisk("runSchedulerOnce",Map.of(),command));}
    public CpfDataRow requestRun(String jobId,String params,CpfBatchRiskCommand command){return map(invokeRisk("requestRun",p("jobId",jobId,"jobParameters",params),command));}
    private static IllegalArgumentException expectedVersionRequired(String operation){
        return new IllegalArgumentException("expectedVersion is required for BAT operation: "+operation);
    }
    private static IllegalArgumentException riskMetadataRequired(String operation){
        return new IllegalArgumentException("approval/idempotency risk command is required for BAT operation: "+operation);
    }
}
