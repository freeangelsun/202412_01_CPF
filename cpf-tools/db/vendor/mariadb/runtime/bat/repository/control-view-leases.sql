SELECT 'WORKER' lease_type, CAST(execution_id AS CHAR) target_id, worker_id owner_id,
       lease_status state, lease_until, fencing_token
FROM bat_execution_lease
WHERE lease_status <> 'RELEASED'
UNION ALL
SELECT 'CENTER_CUT', CAST(center_cut_item_id AS CHAR), runner_id, claim_status, lease_until, fencing_token
FROM bat_center_cut_claim
WHERE claim_status <> 'RELEASED'
UNION ALL
SELECT 'SCHEDULER', scheduler_key, owner_instance_id, 'LEADER', lease_until, fencing_token
FROM bat_scheduler_lease
