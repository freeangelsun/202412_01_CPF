SELECT 'WORKER' lease_type, CAST(execution_id AS VARCHAR(64)) target_id, worker_id owner_id,
       lease_status state, lease_until, fencing_token
FROM BAT_EXECUTION_LEASE
WHERE lease_status <> 'RELEASED'
UNION ALL
SELECT 'CENTER_CUT', CAST(center_cut_item_id AS VARCHAR(64)), runner_id, claim_status, lease_until, fencing_token
FROM BAT_CENTER_CUT_CLAIM
WHERE claim_status <> 'RELEASED'
UNION ALL
SELECT 'SCHEDULER', scheduler_key, owner_instance_id, 'LEADER', lease_until, fencing_token
FROM BAT_SCHEDULER_LEASE
