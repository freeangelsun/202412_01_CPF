UPDATE bza_refresh_token
SET revoked_yn = 'Y', revoked_at = NOW(), updated_by = :updatedBy, updated_at = NOW()
WHERE refresh_token_id = :sessionId AND admin_user_id = :adminUserId
  AND revoked_yn = 'N' AND expire_at > NOW()
