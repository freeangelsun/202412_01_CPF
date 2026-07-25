package com.cpf.admin.opr.service;

import com.cpf.core.api.batch.CpfBatchOperationsPort;
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

    public List<Map<String,Object>> findJobs(){return operations.findJobs();}
    public Map<String,Object> findJobDetail(String jobId){return operations.findJobDetail(jobId);}
    public List<Map<String,Object>> findSchedules(){return operations.findSchedules();}
    public List<Map<String,Object>> findExecutions(
            String jobId,String transactionId,Long springBatchJobInstanceId,
            String workerId,String serverInstanceId,int limit){
        return operations.findExecutions(jobId,transactionId,springBatchJobInstanceId,workerId,serverInstanceId,limit);
    }
    public Map<String,Object> findExecutionDetail(long id){return operations.findExecutionDetail(id);}
    public List<Map<String,Object>> findInstances(){return operations.findInstances();}
    public List<Map<String,Object>> findWorkers(int timeout){return operations.findWorkers(timeout);}
    public List<Map<String,Object>> findStepExecutions(Long executionId,String jobId,int limit){return operations.findStepExecutions(executionId,jobId,limit);}
    public List<Map<String,Object>> findRelations(String jobId){return operations.findRelations(jobId);}
    public List<Map<String,Object>> findExecutionTargets(String jobId,String status,int limit){return operations.findExecutionTargets(jobId,status,limit);}
    public List<Map<String,Object>> findLocks(String jobId){return operations.findLocks(jobId);}
    public Map<String,Object> releaseLock(String key,String user,String reason){return operations.releaseLock(key,user,reason);}
    public List<Map<String,Object>> findGhostCandidates(int timeout){return operations.findGhostCandidates(timeout);}
    public Map<String,Object> actGhostExecution(long id,String action,String user,String reason){return operations.actGhostExecution(id,action,user,reason);}
    public List<Map<String,Object>> findOperationLogs(String jobId,Long executionId,int limit){return operations.findOperationLogs(jobId,executionId,limit);}
    public List<Map<String,Object>> simulateSchedule(String id,String base,int days){return operations.simulateSchedule(id,base,days);}
    public Map<String,Object> registerJob(String id,String name,String type,String desc,String user){return operations.registerJob(id,name,type,desc,user);}
    public Map<String,Object> requestRun(String jobId,String params,String user,String reason){return operations.requestRun(jobId,params,user,reason);}
    public Map<String,Object> requestScheduledRun(String schedule,String job,String params,String user,String reason){return operations.requestScheduledRun(schedule,job,params,user,reason);}
    public Map<String,Object> requestRetry(long id,String user,String reason){return operations.requestRetry(id,user,reason);}
    public Map<String,Object> requestStop(long id,String user,String reason){return operations.requestStop(id,user,reason);}
    public Map<String,Object> updateScheduleEnabled(String id,boolean enabled,String user,String reason){return operations.updateScheduleEnabled(id,enabled,user,reason);}
    public List<Map<String,Object>> runSchedulerOnce(String user){return operations.runSchedulerOnce(user);}
}
