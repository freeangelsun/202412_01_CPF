UPDATE BAT_DEPLOYMENT_PLAN
SET plan_state = ?,
    updated_at = CURRENT_TIMESTAMP(6)
WHERE plan_id = ?
