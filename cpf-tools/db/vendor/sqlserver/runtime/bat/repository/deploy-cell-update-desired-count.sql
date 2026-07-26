UPDATE bat_deployment_cell
SET desired_instance_count = ?,
    updated_at = SYSUTCDATETIME()
WHERE cell_id = ?
