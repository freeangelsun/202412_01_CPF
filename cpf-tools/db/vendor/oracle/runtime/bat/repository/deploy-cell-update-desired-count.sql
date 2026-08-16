UPDATE bat_deployment_cell
SET desired_instance_count = ?,
    updated_at = CURRENT_TIMESTAMP(6)
WHERE cell_id = ?
