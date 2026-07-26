package com.cpf.batch.control.internal;

import com.cpf.batch.api.*;
import com.cpf.batch.runtime.SensitiveTextSanitizer;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.*;

@Repository
public class JdbcRuntimeCommandRepository {
    private final JdbcTemplate jdbc; public JdbcRuntimeCommandRepository(JdbcTemplate jdbc){this.jdbc=jdbc;}

    @Transactional
    public Map<String,Object> create(RuntimeCommand c) {
        try {
            jdbc.update("""
              INSERT INTO bat_runtime_command(command_id,idempotency_key,command_type,target_type,target_snapshot,target_snapshot_hash,
                expected_version,requested_by,reason_text,approval_policy_version,approval_request_id,approved_by,command_state,
                execution_attempt,requested_at,expires_at,result_text,failure_stage,before_state,after_state,transaction_id,evidence_ref)
              VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)
              """,c.commandId(),c.idempotencyKey(),c.commandType(),c.targetType(),SensitiveTextSanitizer.sanitize(c.targetSnapshot()),
              c.targetSnapshotHash(),c.expectedVersion(),c.requestedBy(),c.reason(),c.approvalPolicyVersion(),c.approvalRequestId(),c.approvedBy(),
              c.executionState().name(),c.executionAttempt(),Timestamp.from(c.requestedAt()),c.expiresAt()==null?null:Timestamp.from(c.expiresAt()),
              SensitiveTextSanitizer.sanitize(c.result()),c.failureStage(),SensitiveTextSanitizer.sanitize(c.beforeState()),
              SensitiveTextSanitizer.sanitize(c.afterState()),c.transactionId(),c.evidenceRef());
        } catch(DuplicateKeyException duplicate){return find(c.idempotencyKey()).orElseThrow();}
        return find(c.idempotencyKey()).orElseThrow();
    }

    public Optional<Map<String,Object>> find(String idempotencyKey){
        return jdbc.queryForList("SELECT * FROM bat_runtime_command WHERE idempotency_key=?",idempotencyKey).stream().findFirst();
    }

    /** Only one caller may move an approved/requested command into EXECUTING. */
    public boolean beginExecution(String commandId){
        return jdbc.update("""
          UPDATE bat_runtime_command
             SET command_state='EXECUTING',execution_attempt=execution_attempt+1,updated_at=CURRENT_TIMESTAMP(6)
           WHERE command_id=? AND command_state IN ('REQUESTED','APPROVED','PLANNED')
          """,commandId)==1;
    }

    public void transition(String commandId,CommandState state,String failureStage,String result){
        jdbc.update("""
          UPDATE bat_runtime_command SET command_state=?,failure_stage=?,result_text=?,updated_at=CURRENT_TIMESTAMP(6)
           WHERE command_id=? AND command_state<>'SUCCEEDED'
          """,state.name(),failureStage,SensitiveTextSanitizer.sanitize(result),commandId);
    }

    public void recordAttempt(String commandId,int attempt,String instanceId,String stage,CommandState state,String message){
        jdbc.update("""
          INSERT INTO bat_runtime_command_attempt(command_id,attempt_no,instance_id,stage_code,attempt_state,result_message,started_at,finished_at)
          VALUES(?,?,?,?,?,?,CURRENT_TIMESTAMP(6),CURRENT_TIMESTAMP(6))
          """,commandId,attempt,instanceId,stage,state.name(),SensitiveTextSanitizer.sanitize(message));
    }
}
