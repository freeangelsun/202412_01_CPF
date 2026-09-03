UPDATE MBW_ROLE SET role_name=:roleName,write_allowed_yn=:writeAllowedYn,data_scope=:dataScope,use_yn=:useYn,
       version_no=version_no+1,updated_by=:requestUser,updated_at=CURRENT_TIMESTAMP
 WHERE role_code=:roleCode AND version_no=:expectedVersion
