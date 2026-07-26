INSERT INTO bat_scheduler_lease (
    scheduler_key,
    owner_instance_id,
    fencing_token,
    lease_until,
    last_heartbeat_at
) VALUES (
    ?, ?, 1, ?, ?
)
