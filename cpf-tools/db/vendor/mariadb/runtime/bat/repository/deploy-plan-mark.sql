UPDATE bat_deployment_plan
SET plan_state = ?,
    updated_at = CURRENT_TIMESTAMP(6)
WHERE plan_id = ?
