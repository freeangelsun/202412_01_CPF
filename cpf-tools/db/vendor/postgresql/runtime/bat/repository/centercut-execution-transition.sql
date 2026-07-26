UPDATE bat_center_cut_execution
SET execution_state = ?,
    updated_at = CURRENT_TIMESTAMP(6)
WHERE center_cut_execution_id = ?
  AND execution_state = ?
