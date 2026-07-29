SELECT admin_user_id,
       admin_login_id,
       admin_name,
       password_hash,
       role_code,
       account_status,
       use_yn,
       lock_yn,
       login_fail_count,
       password_change_required_yn,
       password_expire_at,
       last_login_at
  FROM bza_admin_user
 WHERE admin_login_id = :loginId
