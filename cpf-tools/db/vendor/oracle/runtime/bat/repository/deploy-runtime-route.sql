SELECT di.agent_base_url, dc.service_id
FROM bat_deployment_instance di
JOIN bat_deployment_cell dc ON dc.cell_id = di.cell_id
WHERE di.instance_id = ?
