SELECT MIN(step_no) FROM bza_approval_line
 WHERE approval_id = :approvalId AND step_no > :currentStep
