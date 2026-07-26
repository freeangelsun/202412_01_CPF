INSERT INTO bat_deployment_instance(
    cell_id, instance_id, host_alias, port_no, profile_name, zone_id, pool_id,
    agent_base_url, config_ref, desired_state
)
VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
