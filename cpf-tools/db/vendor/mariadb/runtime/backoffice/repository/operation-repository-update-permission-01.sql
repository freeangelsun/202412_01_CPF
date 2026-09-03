UPDATE MBW_PERMISSION SET role_code=:roleCode,menu_code=:menuCode,button_code=:buttonCode,permission_type=:permissionType,http_method=:httpMethod,
       api_pattern=:apiPattern,domain_code=:domainCode,environment_code=:environmentCode,data_scope=:dataScope,allow_yn=:allowYn,use_yn=:useYn,
       version_no=version_no+1,updated_by=:requestUser,updated_at=CURRENT_TIMESTAMP
 WHERE permission_id=:permissionId AND version_no=:expectedVersion
