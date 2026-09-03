UPDATE MBW_APPROVAL_LINE
   SET decision_status=:status, decision_comment=:comment,
       decided_at=CASE WHEN :status='WAITING' THEN NULL ELSE CURRENT_TIMESTAMP END,
       updated_by=:operatorId
 WHERE approval_line_id=:lineId
