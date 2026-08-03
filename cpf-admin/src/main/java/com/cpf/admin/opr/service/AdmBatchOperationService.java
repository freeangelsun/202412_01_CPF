package com.cpf.admin.opr.service;

import com.cpf.core.api.batch.CpfBatchOperationsPort;
import com.cpf.core.api.batch.CpfBatchOwnerUnknownResultException;
import com.cpf.core.api.batch.CpfBatchRiskCommand;
import com.cpf.core.api.data.CpfDataRow;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.Supplier;

/**
 * ADM Batch Control Plane Facade.
 *
 * <p>ADM은 batDB나 Spring Batch Repository를 직접 접근하지 않습니다. 조회/명령은 BAT Owner의
 * {@link CpfBatchOperationsPort}로 위임하며 Local/Remote topology는 Adapter가 결정합니다.
 * 위험조치는 {@link AdmBatchApprovalService}에서 승인 Snapshot을 예약한 뒤에만 호출합니다.</p>
 */
@Service
public class AdmBatchOperationService extends com.cpf.admin.common.base.AdmBaseService {
    private final CpfBatchOperationsPort operations;
    private final AdmBatchApprovalService approvals;

    public AdmBatchOperationService(
            CpfBatchOperationsPort operations,
            AdmBatchApprovalService approvals) {
        this.operations = operations;
        this.approvals = approvals;
    }

    public List<CpfDataRow> findJobs(){return operations.findJobs();}
    public CpfDataRow findJobDetail(String jobId){return operations.findJobDetail(jobId);}
    public List<CpfDataRow> findSchedules(){return operations.findSchedules();}
    public List<CpfDataRow> findExecutions(
            String jobId,String transactionId,Long springBatchJobInstanceId,
            String workerId,String serverInstanceId,int limit){
        return operations.findExecutions(jobId,transactionId,springBatchJobInstanceId,workerId,serverInstanceId,limit);
    }
    public CpfDataRow findExecutionPage(
            String jobId,String transactionId,Long springBatchJobInstanceId,
            String workerId,String serverInstanceId,String status,
            String fromDate,String toDate,int page,int size){
        return operations.findExecutionPage(
                jobId,transactionId,springBatchJobInstanceId,workerId,serverInstanceId,
                status,fromDate,toDate,page,size);
    }
    public CpfDataRow findJobPage(String query,int page,int size,String sort,String direction){
        return operations.findJobPage(query,page,size,sort,direction);
    }
    public CpfDataRow findSchedulePage(String query,int page,int size,String sort,String direction){
        return operations.findSchedulePage(query,page,size,sort,direction);
    }
    public CpfDataRow findInfrastructureSnapshot(int heartbeatTimeoutSeconds,int limit){
        return operations.findInfrastructureSnapshot(heartbeatTimeoutSeconds,limit);
    }
    public CpfDataRow findRecoverySnapshot(int heartbeatTimeoutSeconds,int limit){
        return operations.findRecoverySnapshot(heartbeatTimeoutSeconds,limit);
    }
    public CpfDataRow findExecutionDetail(long id){return operations.findExecutionDetail(id);}
    public List<CpfDataRow> findInstances(){return operations.findInstances();}
    public List<CpfDataRow> findWorkers(int timeout){return operations.findWorkers(timeout);}
    public List<CpfDataRow> findStepExecutions(Long executionId,String jobId,int limit){return operations.findStepExecutions(executionId,jobId,limit);}
    public List<CpfDataRow> findRelations(String jobId){return operations.findRelations(jobId);}
    public List<CpfDataRow> findExecutionTargets(String jobId,String status,int limit){return operations.findExecutionTargets(jobId,status,limit);}
    public List<CpfDataRow> findLocks(String jobId){return operations.findLocks(jobId);}

    /** Version-only overload is deliberately fail-closed. */
    public CpfDataRow releaseLock(String key,String user,String reason,long expectedVersion){
        throw riskMetadataRequired("releaseLock");
    }
    public CpfDataRow releaseLock(String key, CpfBatchRiskCommand command){
        command.assertOperation("releaseLock", "bat_lock", key);
        return executeRisk(command, () -> operations.releaseLock(key, command));
    }

    public List<CpfDataRow> findGhostCandidates(int timeout){return operations.findGhostCandidates(timeout);}
    public CpfDataRow actGhostExecution(long id,String action,String user,String reason,long expectedVersion){
        throw riskMetadataRequired("actGhostExecution");
    }
    public CpfDataRow actGhostExecution(long id,String action,CpfBatchRiskCommand command){
        command.assertOperation("actGhostExecution", "bat_execution", String.valueOf(id));
        return executeRisk(command, () -> operations.actGhostExecution(id, action, command));
    }

    public List<CpfDataRow> findOperationLogs(String jobId,Long executionId,int limit){return operations.findOperationLogs(jobId,executionId,limit);}
    public List<CpfDataRow> simulateSchedule(String id,String base,int days){return operations.simulateSchedule(id,base,days);}
    public CpfDataRow registerJob(String id,String name,String type,String desc,String user){return operations.registerJob(id,name,type,desc,user);}

    public CpfDataRow requestRun(String jobId,String params,String user,String reason){
        throw riskMetadataRequired("requestRun");
    }
    public CpfDataRow requestRun(String jobId,String params,CpfBatchRiskCommand command){
        command.assertOperation("requestRun", "bat_job", jobId);
        return executeRisk(command, () -> operations.requestRun(jobId, params, command));
    }

    public CpfDataRow requestScheduledRun(String schedule,String job,String params,String user,String reason){
        return operations.requestScheduledRun(schedule,job,params,user,reason);
    }

    public CpfDataRow requestRetry(long id,String user,String reason,long expectedVersion){
        throw riskMetadataRequired("requestRetry");
    }
    public CpfDataRow requestRetry(long id,CpfBatchRiskCommand command){
        command.assertOperation("requestRetry", "bat_execution", String.valueOf(id));
        return executeRisk(command, () -> operations.requestRetry(id, command));
    }

    public CpfDataRow requestStop(long id,String user,String reason,long expectedVersion){
        throw riskMetadataRequired("requestStop");
    }
    public CpfDataRow requestStop(long id,CpfBatchRiskCommand command){
        command.assertOperation("requestStop", "bat_execution", String.valueOf(id));
        return executeRisk(command, () -> operations.requestStop(id, command));
    }

    public CpfDataRow updateScheduleEnabled(String id,boolean enabled,String user,String reason,long expectedVersion){
        throw riskMetadataRequired("updateScheduleEnabled");
    }
    public CpfDataRow updateScheduleEnabled(String id,boolean enabled,CpfBatchRiskCommand command){
        command.assertOperation("updateScheduleEnabled", "bat_schedule", id);
        return executeRisk(command, () -> operations.updateScheduleEnabled(id, enabled, command));
    }

    public List<CpfDataRow> runSchedulerOnce(String user){
        throw riskMetadataRequired("runSchedulerOnce");
    }
    public List<CpfDataRow> runSchedulerOnce(CpfBatchRiskCommand command){
        command.assertOperation("runSchedulerOnce", "bat_schedule", "DUE_SCHEDULES");
        return executeRisk(command, () -> operations.runSchedulerOnce(command));
    }

    private <T> T executeRisk(CpfBatchRiskCommand command, Supplier<T> ownerCall) {
        AdmBatchApprovalService.Reservation reservation = approvals.reserve(command);
        String priorState = reservation.executionStatus().toUpperCase(java.util.Locale.ROOT);
        if (reservation.replay()) {
            if (priorState.equals("RUNNING") || priorState.equals("UNKNOWN")) {
                throw new CpfBatchOwnerUnknownResultException(
                        "ADM_APPROVAL_RECONCILIATION_REQUIRED",
                        "The approved BAT command has an unresolved prior execution; automatic replay is blocked");
            }
            if (priorState.equals("FAILED")) {
                throw new IllegalStateException(
                        "The approved BAT command already failed; create a new approval request before retrying");
            }
            if (!priorState.equals("SUCCEEDED") && !priorState.equals("COMPLETED")) {
                throw new CpfBatchOwnerUnknownResultException(
                        "ADM_APPROVAL_STATE_UNKNOWN",
                        "Unsupported prior approval execution state: " + priorState);
            }
            // Completed commands may call BAT once more only to retrieve the BAT idempotency-ledger replay result.
        }
        try {
            T result = ownerCall.get();
            if (!reservation.replay()) {
                try {
                    approvals.complete(reservation, command.requestUser());
                } catch (RuntimeException completionFailure) {
                    throw new CpfBatchOwnerUnknownResultException(
                            "ADM_APPROVAL_COMPLETION_UNKNOWN",
                            "BAT Owner command completed but ADM approval completion could not be persisted: "
                                    + completionFailure.getMessage());
                }
            }
            return result;
        } catch (CpfBatchOwnerUnknownResultException unknown) {
            if (!reservation.replay()) {
                try {
                    approvals.unknown(
                            reservation, command.requestUser(),
                            unknown.getClass().getSimpleName(), unknown.getMessage());
                } catch (RuntimeException ledgerFailure) {
                    unknown.addSuppressed(ledgerFailure);
                }
            }
            throw unknown;
        } catch (RuntimeException failure) {
            if (!reservation.replay()) {
                try {
                    approvals.fail(
                            reservation, command.requestUser(),
                            failure.getClass().getSimpleName(), failure.getMessage());
                } catch (RuntimeException ledgerFailure) {
                    CpfBatchOwnerUnknownResultException unknown = new CpfBatchOwnerUnknownResultException(
                            "ADM_APPROVAL_FAILURE_PERSIST_UNKNOWN",
                            "BAT Owner command failed but ADM could not persist the final failure state");
                    unknown.addSuppressed(failure);
                    unknown.addSuppressed(ledgerFailure);
                    throw unknown;
                }
            }
            throw failure;
        }
    }

    private static IllegalArgumentException riskMetadataRequired(String operation) {
        return new IllegalArgumentException(
                "approvalRequestId, idempotencyKey and canonical risk command are required: " + operation);
    }
}
