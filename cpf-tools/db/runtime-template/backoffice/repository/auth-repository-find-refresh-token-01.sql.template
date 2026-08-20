SELECT rt.refresh_token_hash,
       rt.admin_user_id,
       rt.login_domain,
       rt.expire_at,
       rt.revoked_yn,
       rt.transaction_id,
       u.admin_login_id
  FROM mbw_refresh_token rt
  JOIN mbw_admin_user u
    ON u.admin_user_id = rt.admin_user_id
 WHERE rt.refresh_token_hash = :refreshTokenHash
