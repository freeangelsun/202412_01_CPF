UPDATE OPS_SYSTEM_REGISTRY
SET domain_code = ?,
    last_seen_at = ?,
    last_instance_id = ?,
    updated_by = 'CPF_RUNTIME',
    updated_at = ?
WHERE system_code = ?
