INSERT INTO BAT_SCHEDULER_LEASE (
    scheduler_key,
    owner_instance_id,
    fencing_token,
    lease_until,
    last_heartbeat_at
) VALUES (
    ?, ?, 1, (CURRENT_TIMESTAMP(6) AT TIME ZONE 'UTC') + (? * INTERVAL '1 microsecond'), (CURRENT_TIMESTAMP(6) AT TIME ZONE 'UTC')
)
