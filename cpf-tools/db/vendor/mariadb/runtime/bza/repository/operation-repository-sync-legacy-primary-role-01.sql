UPDATE bza_admin_user SET role_code=:roleCode,updated_by=:actor,updated_at=CURRENT_TIMESTAMP WHERE admin_login_id=:loginId
