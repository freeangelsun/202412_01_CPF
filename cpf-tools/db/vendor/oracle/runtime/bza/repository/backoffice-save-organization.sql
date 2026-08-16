MERGE INTO bza_organization target
USING (
    SELECT :organizationCode organization_code, :parentOrganizationCode parent_organization_code,
           :organizationName organization_name, :organizationType organization_type,
           :sortOrder sort_order, :useYn use_yn, :requestUser request_user
    FROM dual
) source
ON (target.organization_code = source.organization_code)
WHEN MATCHED THEN UPDATE SET
    target.parent_organization_code = source.parent_organization_code,
    target.organization_name = source.organization_name, target.organization_type = source.organization_type,
    target.sort_order = source.sort_order, target.use_yn = source.use_yn,
    target.updated_by = source.request_user, target.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (
    organization_code, parent_organization_code, organization_name,
    organization_type, sort_order, use_yn, created_by, updated_by
) VALUES (
    source.organization_code, source.parent_organization_code, source.organization_name,
    source.organization_type, source.sort_order, source.use_yn, source.request_user, source.request_user
)
