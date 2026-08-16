INSERT INTO bat_center_cut_claim (
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
    ?, ?, ?, ?, 'CLAIMED', 1, ?, ?, 1, 0
)
