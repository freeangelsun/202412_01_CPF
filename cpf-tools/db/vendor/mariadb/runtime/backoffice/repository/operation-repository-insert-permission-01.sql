INSERT INTO mbw_permission(role_code,menu_code,button_code,permission_type,http_method,api_pattern,domain_code,environment_code,data_scope,allow_yn,use_yn,version_no,created_by,updated_by)
VALUES(:roleCode,:menuCode,:buttonCode,:permissionType,:httpMethod,:apiPattern,:domainCode,:environmentCode,:dataScope,:allowYn,:useYn,0,:requestUser,:requestUser)
