UPDATE bat_deployment_plan
SET plan_state = ?,
    updated_at = SYSUTCDATETIME()
WHERE plan_id = ?
