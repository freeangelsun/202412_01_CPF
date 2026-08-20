UPDATE mbw_organization SET parent_organization_code=:parentOrganizationCode,organization_name=:organizationName,organization_type=:organizationType,
       sort_order=:sortOrder,effective_from=:effectiveFrom,effective_to=:effectiveTo,use_yn=:useYn,version_no=version_no+1,updated_by=:requestUser,updated_at=CURRENT_TIMESTAMP(3)
 WHERE organization_code=:organizationCode AND version_no=:expectedVersion
