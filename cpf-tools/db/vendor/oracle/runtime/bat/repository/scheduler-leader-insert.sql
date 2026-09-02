INSERT INTO BAT_SCHEDULER_LEASE (
    scheduler_key,
    owner_instance_id,
    fencing_token,
    lease_until,
    last_heartbeat_at
) VALUES (
    ?, ?, 1, SYS_EXTRACT_UTC(SYSTIMESTAMP) + NUMTODSINTERVAL(? / 1000000, 'SECOND'), SYS_EXTRACT_UTC(SYSTIMESTAMP)
)
