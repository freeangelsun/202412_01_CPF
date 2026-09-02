INSERT INTO BAT_CENTER_CUT_CLAIM (
    center_cut_item_id,
    runner_id,
    pool_id,
    claim_token,
    claim_status,
    fencing_token,
    lease_until,
    last_heartbeat_at,
    attempt_no,
    takeover_count
) VALUES (
    ?, ?, ?, ?, 'CLAIMED', 1, SYS_EXTRACT_UTC(SYSTIMESTAMP) + NUMTODSINTERVAL(? / 1000000, 'SECOND'), SYS_EXTRACT_UTC(SYSTIMESTAMP), 1, 0
)
