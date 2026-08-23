INSERT INTO OPS_SYSTEM_REGISTRY (
    system_code, system_name, domain_code, enabled_yn, description, policy_version,
    first_seen_at, last_seen_at, last_instance_id, created_by, created_at,
    updated_by, updated_at
) VALUES (?, ?, ?, 'Y', ?, 1, ?, ?, ?, 'CPF_RUNTIME', ?, 'CPF_RUNTIME', ?)
