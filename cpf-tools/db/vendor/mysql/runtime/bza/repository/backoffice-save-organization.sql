INSERT INTO bza_organization (
    organization_code, parent_organization_code, organization_name,
    organization_type, sort_order, use_yn, created_by, updated_by
) VALUES (
    :organizationCode, :parentOrganizationCode, :organizationName,
    :organizationType, :sortOrder, :useYn, :requestUser, :requestUser
)
ON DUPLICATE KEY UPDATE
    parent_organization_code = VALUES(parent_organization_code),
    organization_name = VALUES(organization_name), organization_type = VALUES(organization_type),
    sort_order = VALUES(sort_order), use_yn = VALUES(use_yn),
    updated_by = VALUES(updated_by), updated_at = CURRENT_TIMESTAMP
