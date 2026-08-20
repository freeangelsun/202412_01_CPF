SELECT COUNT(*) FROM mbw_employee WHERE (:organizationCode IS NULL OR organization_code=:organizationCode) AND (:status IS NULL OR employment_status=:status)
