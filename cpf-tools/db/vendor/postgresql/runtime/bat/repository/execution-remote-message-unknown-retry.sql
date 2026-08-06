UPDATE bat_remote_message_ledger
   SET status_cd = 'FAILED',
       owner_id = 'RECONCILED',
       lease_until = CURRENT_TIMESTAMP(6),
       last_error_cd = NULL,
       updated_at = CURRENT_TIMESTAMP(6),
       version_no = version_no + 1
 WHERE direction_cd = ?
   AND message_id = ?
   AND status_cd = 'UNKNOWN'
   AND attempt_no = ?
   AND version_no = ?
