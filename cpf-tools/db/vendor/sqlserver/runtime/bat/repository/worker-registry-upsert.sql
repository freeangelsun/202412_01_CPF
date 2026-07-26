MERGE INTO bat_worker WITH (HOLDLOCK) AS target
USING (
    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
) AS source (
    worker_id,
    server_instance_id,
    host_name,
    process_id,
    worker_version,
    capabilities_json,
    max_concurrency,
    control_status,
    worker_status,
    current_execution_id
)
ON target.worker_id = source.worker_id
WHEN MATCHED THEN UPDATE
   SET server_instance_id = source.server_instance_id,
       host_name = source.host_name,
       process_id = source.process_id,
       worker_version = source.worker_version,
       capabilities_json = source.capabilities_json,
       max_concurrency = source.max_concurrency,
       control_status = source.control_status,
       worker_status = source.worker_status,
       active_yn = 'Y',
       last_heartbeat_at = SYSUTCDATETIME(),
       current_execution_id = source.current_execution_id,
       updated_by = 'BAT',
       updated_at = SYSUTCDATETIME()
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
    SYSUTCDATETIME(),
    source.current_execution_id,
    'BAT',
    'BAT'
);
