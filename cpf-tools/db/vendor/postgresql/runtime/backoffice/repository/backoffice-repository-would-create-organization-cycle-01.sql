WITH RECURSIVE descendants (organization_code) AS (
    SELECT organization_code FROM MBW_ORGANIZATION WHERE parent_organization_code=:organizationCode
    UNION ALL
    SELECT o.organization_code FROM MBW_ORGANIZATION o JOIN descendants d ON o.parent_organization_code=d.organization_code
)
SELECT COUNT(*) FROM descendants WHERE organization_code=:parentCode
