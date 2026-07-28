SELECT operation_id AS operationId,
       admin_user_id AS adminUserId,
       admin_login_id AS loginId,
       request_hash AS requestHash,
       operation_status AS status,
       result_access_token_enc AS resultAccessTokenEnc,
       result_refresh_token_enc AS resultRefreshTokenEnc,
       result_refresh_expires_at AS resultRefreshExpiresAt,
       result_expires_at AS resultExpiresAt,
       failure_code AS failureCode,
       failure_message AS failureMessage
  FROM bza_login_operation
 WHERE operation_id = :operationId
 FOR UPDATE
