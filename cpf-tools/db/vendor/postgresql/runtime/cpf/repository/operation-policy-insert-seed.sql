INSERT INTO OPS_OPERATION_POLICY (
    operation_id, enabled_yn, all_callers_yn, channel_policy_required_yn,
    policy_version, seed_source, seed_revision, seeded_at, change_reason,
    created_by, created_at, updated_by, updated_at
) VALUES (?, 'Y', ?, 'N', 1, ?, ?, ?, ?, 'CPF_SEED', ?, 'CPF_SEED', ?)
