SELECT approval_id AS approvalId
  FROM MBW_APPROVAL_DOCUMENT
 WHERE approval_status='IN_REVIEW' AND due_at IS NOT NULL AND due_at <= :now
 ORDER BY due_at, approval_id
 OFFSET 0 ROWS FETCH NEXT :limit ROWS ONLY
