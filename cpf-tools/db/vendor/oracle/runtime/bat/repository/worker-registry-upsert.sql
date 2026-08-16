MERGE INTO bat_worker target
USING (
    SELECT ? AS worker_id,
           ? AS server_instance_id,
           ? AS host_name,
           ? AS process_id,
           ? AS worker_version,
           ? AS capabilities_json,
           ? AS max_concurrency,
           ? AS control_status,
           ? AS worker_status,
           ? AS current_execution_id
      FROM dual
) source
ON (target.worker_id = source.worker_id)
WHEN MATCHED THEN UPDATE
   SET target.server_instance_id = source.server_instance_id,
       target.host_name = source.host_name,
       target.process_id = source.process_id,
       target.worker_version = source.worker_version,
       target.capabilities_json = source.capabilities_json,
       target.max_concurrency = source.max_concurrency,
       target.control_status = source.control_status,
       target.worker_status = source.worker_status,
       target.active_yn = 'Y',
       target.last_heartbeat_at = CURRENT_TIMESTAMP(3),
       target.current_execution_id = source.current_execution_id,
       target.updated_by = 'BAT',
       target.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (
    worker_id,
    server_instance_id,
    host_name,
    process_id,
    worker_version,
    capabilities_json,
    max_concurrency,
    queue_capacity,
    control_status,
    worker_status,
    active_yn,
    last_heartbeat_at,
    current_execution_id,
    created_by,
    updated_by
) VALUES (
    source.worker_id,
    source.server_instance_id,
    source.host_name,
    source.process_id,
    source.worker_version,
    source.capabilities_json,
    source.max_concurrency,
    1,
    source.control_status,
    source.worker_status,
    'Y',
    CURRENT_TIMESTAMP(3),
    source.current_execution_id,
    'BAT',
    'BAT'
)
