SELECT di.agent_base_url, dc.service_id
FROM BAT_DEPLOYMENT_INSTANCE di
JOIN BAT_DEPLOYMENT_CELL dc ON dc.cell_id = di.cell_id
WHERE di.instance_id = ?
