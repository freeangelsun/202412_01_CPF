UPDATE bat_remote_message_ledger
   SET status_cd = 'PROCESSING',
       owner_id = ?,
       lease_until = ?,
       attempt_no = attempt_no + 1,
       updated_at = ?,
       version_no = version_no + 1
 WHERE direction_cd = ?
   AND message_id = ?
   AND version_no = ?
