INSERT INTO mbw_role(role_code,role_name,write_allowed_yn,data_scope,use_yn,version_no,created_by,updated_by)
VALUES(:roleCode,:roleName,:writeAllowedYn,:dataScope,:useYn,0,:requestUser,:requestUser)
