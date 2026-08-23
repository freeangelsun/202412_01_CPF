INSERT INTO OPS_OPERATION_CALLER_POLICY (
    operation_id, caller_system_code, allowed_yn, policy_version, seed_source,
    seed_revision, seeded_at, change_reason, created_by, created_at, updated_by,
    updated_at
) VALUES (?, ?, 'Y', 1, ?, ?, ?, ?, 'CPF_SEED', ?, 'CPF_SEED', ?)
