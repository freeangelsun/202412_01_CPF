package com.cpf.data.persistence.mybatis.logging;

import com.cpf.platform.operations.observability.spi.logging.lineage.CpfTransactionLineageRecord;
import com.cpf.platform.operations.observability.spi.logging.lineage.CpfTransactionLineageProjectionPort;
import java.sql.Timestamp;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;

/** DB3-portable idempotent projection writer; detailed domain stores remain authoritative sources. */
public final class JdbcTransactionLineageProjectionAdapter implements CpfTransactionLineageProjectionPort {
    private final JdbcTemplate jdbc;
    public JdbcTransactionLineageProjectionAdapter(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    @Override public void upsert(CpfTransactionLineageRecord r) {
        int changed = jdbc.update("UPDATE cpf_transaction_lineage SET lifecycle_state=?, failure_stage=?, unknown_yn=?, reconcile_state=?, freshness_at=?, payload_hash=?, archived_at=? WHERE lineage_id=? AND occurred_at=?",
                r.lifecycleState(), r.failureStage(), r.unknown()?"Y":"N", r.reconcileState(), ts(r.freshnessAt()), r.payloadHash(), ts(r.archivedAt()), r.lineageId(), ts(r.occurredAt()));
        if (changed > 0) return;
        try {
            jdbc.update("INSERT INTO cpf_transaction_lineage (lineage_id,transaction_id,segment_id,parent_segment_id,attempt_no,trace_id,span_id,request_id,idempotency_key,tenant_id,channel_code,actor_id_masked,instance_id,was_id,agent_id,worker_id,remote_system,operation_id,message_id,consumer_group,dlq_id,batch_job_instance_id,batch_job_execution_id,batch_step_execution_id,partition_id,file_id,source_type,source_ref_id,lifecycle_state,failure_stage,unknown_yn,reconcile_state,occurred_at,freshness_at,payload_hash,archived_at) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                    r.lineageId(),r.transactionId(),r.segmentId(),r.parentSegmentId(),r.attemptNo(),r.traceId(),r.spanId(),r.requestId(),r.idempotencyKey(),r.tenantId(),r.channelCode(),r.actorIdMasked(),r.instanceId(),r.wasId(),r.agentId(),r.workerId(),r.remoteSystem(),r.operationId(),r.messageId(),r.consumerGroup(),r.dlqId(),r.batchJobInstanceId(),r.batchJobExecutionId(),r.batchStepExecutionId(),r.partitionId(),r.fileId(),r.sourceType(),r.sourceRefId(),r.lifecycleState(),r.failureStage(),r.unknown()?"Y":"N",r.reconcileState(),ts(r.occurredAt()),ts(r.freshnessAt()),r.payloadHash(),ts(r.archivedAt()));
        } catch (DuplicateKeyException race) {
            jdbc.update("UPDATE cpf_transaction_lineage SET lifecycle_state=?, failure_stage=?, unknown_yn=?, reconcile_state=?, freshness_at=?, payload_hash=?, archived_at=? WHERE lineage_id=? AND occurred_at=?",
                    r.lifecycleState(), r.failureStage(), r.unknown()?"Y":"N", r.reconcileState(), ts(r.freshnessAt()), r.payloadHash(), ts(r.archivedAt()), r.lineageId(), ts(r.occurredAt()));
        }
    }
    private static Timestamp ts(java.time.LocalDateTime v) { return v == null ? null : Timestamp.valueOf(v); }
}
