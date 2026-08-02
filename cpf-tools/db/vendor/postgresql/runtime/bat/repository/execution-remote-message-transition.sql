UPDATE bat_remote_message_ledger
   SET status_cd = ?,
       last_error_cd = ?,
       lease_until = ?,
       updated_at = ?,
       version_no = version_no + 1
 WHERE direction_cd = ?
   AND message_id = ?
   AND owner_id = ?
   AND status_cd = 'PROCESSING'
