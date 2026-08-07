-- Runtime one-shot transactionId query. Bind :transaction_id using the framework JDBC layer.
SELECT transaction_id, segment_id, parent_segment_id, attempt_no, trace_id, span_id, request_id,
       idempotency_key, tenant_id, channel_code, actor_id_masked, instance_id, was_id, agent_id, worker_id,
       remote_system, operation_id, message_id, consumer_group, dlq_id, batch_job_instance_id,
       batch_job_execution_id, batch_step_execution_id, partition_id, file_id, source_type, source_ref_id,
       lifecycle_state, failure_stage, unknown_yn, reconcile_state, occurred_at, freshness_at
  FROM cpf_transaction_lineage
 WHERE transaction_id = :transaction_id
 ORDER BY occurred_at, segment_id, attempt_no;
