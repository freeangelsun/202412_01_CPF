INSERT INTO BAT_RUNTIME_HEARTBEAT(
    instance_id, heartbeat_at, ready_yn, available_capacity, queue_depth, draining_yn,
    current_execution_count, active_lease_count, last_error_code, deployment_version
)
VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
