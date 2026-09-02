INSERT INTO BAT_SCHEDULER_LEASE (
    scheduler_key,
    owner_instance_id,
    fencing_token,
    lease_until,
    last_heartbeat_at
) VALUES (
    ?, ?, 1, TIMESTAMPADD(MICROSECOND, ?, UTC_TIMESTAMP(6)), UTC_TIMESTAMP(6)
)
