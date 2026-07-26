package com.cpf.batch.control.deploy;

import com.cpf.batch.api.*;
import com.cpf.batch.runtime.SensitiveTextSanitizer;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;

@Repository
public class DeploymentExecutionRepository {
    private final JdbcTemplate jdbc; public DeploymentExecutionRepository(JdbcTemplate jdbc){this.jdbc=jdbc;}

    @Transactional
    public Optional<Map<String,Object>> begin(DeploymentRequest r) {
        try {
            jdbc.update("""
              INSERT INTO bat_deployment_execution(deployment_id,cell_id,idempotency_key,to_version,strategy_code,execution_state,
                requested_by,approved_by,reason_text,started_at,created_at)
              VALUES(?,?,?,?,?,'EXECUTING',?,?,?,CURRENT_TIMESTAMP(6),CURRENT_TIMESTAMP(6))
              """,r.deploymentId(),r.manifest().cellId(),r.idempotencyKey(),r.manifest().artifact().version(),
              r.manifest().deployment().strategy().name(),r.requestedBy(),r.approvedBy(),r.reason());
            return Optional.empty();
        } catch(DuplicateKeyException duplicate) {
            return findByIdempotency(r.idempotencyKey());
        }
    }

    public void instance(String deploymentId,int sequence,DeploymentResult.InstanceResult result) {
        jdbc.update("""
          INSERT INTO bat_deployment_instance_result(deployment_id,sequence_no,instance_id,stage_code,result_state,result_message,recorded_at)
          VALUES(?,?,?,?,?,?,CURRENT_TIMESTAMP(6))
          """,deploymentId,sequence,result.instanceId(),result.stage(),result.state().name(),SensitiveTextSanitizer.sanitize(result.message()));
    }

    public void finish(String deploymentId,CommandState state,String failureStage,String message) {
        jdbc.update("""
          UPDATE bat_deployment_execution SET execution_state=?,failure_stage=?,result_message=?,finished_at=CURRENT_TIMESTAMP(6)
           WHERE deployment_id=?
          """,state.name(),failureStage,SensitiveTextSanitizer.sanitize(message),deploymentId);
    }

    public Optional<Map<String,Object>> findByIdempotency(String key) {
        return jdbc.queryForList("SELECT * FROM bat_deployment_execution WHERE idempotency_key=?",key).stream().findFirst();
    }
}
