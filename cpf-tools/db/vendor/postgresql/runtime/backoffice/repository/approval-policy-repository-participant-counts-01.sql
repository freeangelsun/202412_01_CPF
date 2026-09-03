SELECT COUNT(*) AS participantCount,
       SUM(CASE WHEN decision_status IN ('APPROVED','AGREED') THEN 1 ELSE 0 END) AS approvedCount,
       SUM(CASE WHEN decision_status='REJECTED' THEN 1 ELSE 0 END) AS rejectedCount
  FROM MBW_APPROVAL_PARTICIPANT WHERE approval_line_id=:lineId
