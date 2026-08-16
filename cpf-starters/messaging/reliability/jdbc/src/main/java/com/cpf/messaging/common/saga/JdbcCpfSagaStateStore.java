package com.cpf.messaging.common.saga;

import com.cpf.messaging.reliability.saga.*;

import org.springframework.jdbc.core.JdbcTemplate;
import java.util.*;

/** cpfDB 기반 Saga durable state 기본 구현. */
public class JdbcCpfSagaStateStore implements CpfSagaStateStore {
    private final JdbcTemplate jdbc;
    public JdbcCpfSagaStateStore(JdbcTemplate jdbc){this.jdbc=Objects.requireNonNull(jdbc,"jdbc");}

    @Override public CpfSagaSnapshot create(CpfSagaContext c){
        jdbc.update("""
            INSERT INTO cpf_saga_execution(saga_id,saga_type,business_key,transaction_id,saga_status,version,started_at,updated_at)
            VALUES(?,?,?,?, 'RUNNING',0,CURRENT_TIMESTAMP(3),CURRENT_TIMESTAMP(3))
            """,c.sagaId(),c.sagaType(),c.businessKey(),c.transactionId());
        return find(c.sagaId()).orElseThrow();
    }
    @Override public Optional<CpfSagaSnapshot> find(String sagaId){
        List<Map<String,Object>> rows=jdbc.queryForList("""
            SELECT saga_id,saga_type,business_key,transaction_id,saga_status,version,error_message
              FROM cpf_saga_execution WHERE saga_id=?
            """,sagaId);
        if(rows.isEmpty())return Optional.empty();Map<String,Object> r=rows.get(0);
        List<CpfSagaStepSnapshot> steps=jdbc.queryForList("""
            SELECT step_no,step_id,step_status,result_code,result_snapshot,error_message,execute_attempts,compensation_attempts
              FROM cpf_saga_step_execution WHERE saga_id=? ORDER BY step_no
            """,sagaId).stream().map(this::step).toList();
        return Optional.of(new CpfSagaSnapshot(str(r,"saga_id"),str(r,"saga_type"),str(r,"business_key"),str(r,"transaction_id"),
                CpfSagaStatus.valueOf(str(r,"saga_status")),num(r,"version"),str(r,"error_message"),steps));
    }
    @Override public void markSaga(String sagaId,CpfSagaStatus status,String errorMessage){
        jdbc.update("""
            UPDATE cpf_saga_execution
               SET saga_status=?,
                   error_message=?,
                   version=version+1,
                   completed_at=CASE WHEN ? IN ('COMPLETED','COMPENSATED','MANUALLY_RESOLVED')
                                     THEN CURRENT_TIMESTAMP(3) ELSE completed_at END,
                   updated_at=CURRENT_TIMESTAMP(3)
             WHERE saga_id=?
            """, status.name(), trim(errorMessage), status.name(), sagaId);
    }
    @Override public void markStep(String sagaId,int stepNo,String stepId,CpfSagaStepStatus status,CpfSagaStepResult result,String errorMessage,boolean compensationAttempt){
        int executeAttemptIncrement = !compensationAttempt && status == CpfSagaStepStatus.RUNNING ? 1 : 0;
        int compensationAttemptIncrement = compensationAttempt && status == CpfSagaStepStatus.COMPENSATING ? 1 : 0;
        int changed=jdbc.update("""
            UPDATE cpf_saga_step_execution SET step_id=?,step_status=?,result_code=?,result_snapshot=?,error_message=?,
                   execute_attempts=execute_attempts+?,compensation_attempts=compensation_attempts+?,updated_at=CURRENT_TIMESTAMP(3)
             WHERE saga_id=? AND step_no=?
            """,stepId,status.name(),result==null?null:result.resultCode(),result==null?null:result.resultSnapshot(),trim(errorMessage),
                executeAttemptIncrement,compensationAttemptIncrement,sagaId,stepNo);
        if(changed==0){jdbc.update("""
            INSERT INTO cpf_saga_step_execution(saga_id,step_no,step_id,step_status,result_code,result_snapshot,error_message,execute_attempts,compensation_attempts,created_at,updated_at)
            VALUES(?,?,?,?,?,?,?,?,?,CURRENT_TIMESTAMP(3),CURRENT_TIMESTAMP(3))
            """,sagaId,stepNo,stepId,status.name(),result==null?null:result.resultCode(),result==null?null:result.resultSnapshot(),trim(errorMessage),executeAttemptIncrement,compensationAttemptIncrement);}
    }
    @Override public void auditManualAction(String sagaId,String actionType,String operatorId,String reason,String beforeStatus,String afterStatus){
        jdbc.update("""
            INSERT INTO cpf_saga_manual_action(action_id,saga_id,action_type,operator_id,reason,before_status,after_status,created_at)
            VALUES(?,?,?,?,?,?,?,CURRENT_TIMESTAMP(3))
            """,UUID.randomUUID().toString(),sagaId,actionType,operatorId,reason,beforeStatus,afterStatus);
    }
    private CpfSagaStepSnapshot step(Map<String,Object> r){return new CpfSagaStepSnapshot(num(r,"step_no"),str(r,"step_id"),CpfSagaStepStatus.valueOf(str(r,"step_status")),str(r,"result_code"),str(r,"result_snapshot"),str(r,"error_message"),num(r,"execute_attempts"),num(r,"compensation_attempts"));}
    private static String str(Map<String,Object> r,String k){Object v=r.get(k);return v==null?null:String.valueOf(v);} private static int num(Map<String,Object> r,String k){Object v=r.get(k);return v instanceof Number n?n.intValue():v==null?0:Integer.parseInt(String.valueOf(v));}
    private static String trim(String v){return v==null?null:v.substring(0,Math.min(2000,v.length()));}
}
