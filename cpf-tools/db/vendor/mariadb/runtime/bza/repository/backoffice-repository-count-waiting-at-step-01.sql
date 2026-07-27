SELECT COUNT(*) FROM bza_approval_line
 WHERE approval_id = :approvalId AND step_no = :stepNo AND decision_status = 'WAITING'
