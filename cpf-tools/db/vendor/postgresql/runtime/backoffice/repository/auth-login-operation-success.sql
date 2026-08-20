UPDATE mbw_login_operation
   SET operation_status = 'SUCCESS',
       result_access_token_enc = :resultAccessTokenEnc,
       result_refresh_token_enc = :resultRefreshTokenEnc,
       result_refresh_expires_at = :resultRefreshExpiresAt,
       result_expires_at = :resultExpiresAt,
       failure_code = NULL,
       failure_message = NULL,
       updated_by = 'MBW_LOGIN',
       updated_at = CURRENT_TIMESTAMP
 WHERE operation_id = :operationId
   AND operation_status = 'PROCESSING'
