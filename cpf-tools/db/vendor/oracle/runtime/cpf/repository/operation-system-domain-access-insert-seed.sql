INSERT INTO OPS_SYSTEM_DOMAIN_ACCESS (
    caller_system_code, target_system_code, allowed_yn, policy_version,
    change_reason, created_by, created_at, updated_by, updated_at
) VALUES (?, ?, 'Y', 1, ?, 'CPF_SEED', ?, 'CPF_SEED', ?)
