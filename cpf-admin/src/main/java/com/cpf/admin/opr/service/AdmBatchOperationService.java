package com.cpf.admin.opr.service;

import com.cpf.core.api.batch.CpfBatchOperationsPort;
import com.cpf.core.api.data.CpfDataRow;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * ADM Batch Control Plane Facade.
 *
 * <p>ADM은 batDB나 Spring Batch Repository를 직접 접근하지 않습니다. 조회/명령은 BAT Owner의
 * {@link CpfBatchOperationsPort}로 위임하며 Local/Remote topology는 Adapter가 결정합니다.</p>
 */
@Service
public class AdmBatchOperationService extends com.cpf.admin.common.base.AdmBaseService {
    private final CpfBatchOperationsPort operations;
    public AdmBatchOperationService(CpfBatchOperationsPort operations){this.operations=operations;}

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
    public CpfDataRow releaseLock(String key,String user,String reason){return operations.releaseLock(key,user,reason);}
    public List<CpfDataRow> findGhostCandidates(int timeout){return operations.findGhostCandidates(timeout);}
    public CpfDataRow actGhostExecution(long id,String action,String user,String reason){return operations.actGhostExecution(id,action,user,reason);}
    public List<CpfDataRow> findOperationLogs(String jobId,Long executionId,int limit){return operations.findOperationLogs(jobId,executionId,limit);}
    public List<CpfDataRow> simulateSchedule(String id,String base,int days){return operations.simulateSchedule(id,base,days);}
    public CpfDataRow registerJob(String id,String name,String type,String desc,String user){return operations.registerJob(id,name,type,desc,user);}
    public CpfDataRow requestRun(String jobId,String params,String user,String reason){return operations.requestRun(jobId,params,user,reason);}
    public CpfDataRow requestScheduledRun(String schedule,String job,String params,String user,String reason){return operations.requestScheduledRun(schedule,job,params,user,reason);}
    public CpfDataRow requestRetry(long id,String user,String reason){return operations.requestRetry(id,user,reason);}
    public CpfDataRow requestStop(long id,String user,String reason){return operations.requestStop(id,user,reason);}
    public CpfDataRow updateScheduleEnabled(String id,boolean enabled,String user,String reason){return operations.updateScheduleEnabled(id,enabled,user,reason);}
    public List<CpfDataRow> runSchedulerOnce(String user){return operations.runSchedulerOnce(user);}
}
