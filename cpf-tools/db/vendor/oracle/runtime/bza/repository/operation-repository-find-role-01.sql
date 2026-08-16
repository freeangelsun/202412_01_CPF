SELECT role_code AS roleCode,role_name AS roleName,write_allowed_yn AS writeAllowedYn,data_scope AS dataScope,use_yn AS useYn,version_no AS versionNo
FROM bza_role WHERE role_code=:code
