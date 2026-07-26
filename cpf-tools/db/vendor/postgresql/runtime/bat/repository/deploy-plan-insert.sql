INSERT INTO bat_deployment_plan(
    plan_id, cell_id, manifest_json, manifest_hash, requested_by, reason_text, plan_state, created_at
)
VALUES (?, ?, ?, ?, ?, ?, 'PLANNED', CURRENT_TIMESTAMP(6))
