UPDATE bza_admin_user
SET password_hash = :newHash, updated_by = :updatedBy, updated_at = NOW()
WHERE admin_user_id = :adminUserId AND password_hash = :previousHash
