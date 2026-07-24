INSERT INTO bza_organization (
    organization_code, parent_organization_code, organization_name,
    organization_type, sort_order, use_yn, created_by, updated_by
) VALUES (
    :organizationCode, :parentOrganizationCode, :organizationName,
    :organizationType, :sortOrder, :useYn, :requestUser, :requestUser
)
ON CONFLICT (organization_code) DO UPDATE SET
    parent_organization_code = EXCLUDED.parent_organization_code,
    organization_name = EXCLUDED.organization_name,
    organization_type = EXCLUDED.organization_type,
    sort_order = EXCLUDED.sort_order, use_yn = EXCLUDED.use_yn,
    updated_by = EXCLUDED.updated_by, updated_at = CURRENT_TIMESTAMP
