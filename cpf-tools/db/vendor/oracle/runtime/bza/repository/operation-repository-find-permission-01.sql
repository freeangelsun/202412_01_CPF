SELECT permission_id AS permissionId,role_code AS roleCode,menu_code AS menuCode,button_code AS buttonCode,permission_type AS permissionType,
       http_method AS httpMethod,api_pattern AS apiPattern,domain_code AS domainCode,environment_code AS environmentCode,data_scope AS dataScope,
       allow_yn AS allowYn,use_yn AS useYn,version_no AS versionNo FROM bza_permission WHERE permission_id=:id
