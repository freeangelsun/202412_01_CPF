INSERT INTO bat_execution_lease (
    execution_id,
    worker_id,
    lease_token,
    lease_status,
    claimed_at,
    lease_until,
    last_heartbeat_at,
    attempt_no,
    takeover_count,
    fencing_token
) VALUES (
    ?, ?, ?, 'CLAIMED', ?, ?, ?, 1, 0, 1
)
