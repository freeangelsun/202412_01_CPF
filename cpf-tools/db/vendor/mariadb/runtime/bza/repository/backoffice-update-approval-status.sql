UPDATE bza_approval_document
SET approval_status = :status, current_step_no = :currentStep,
    version_no = version_no + 1, updated_by = :actor, updated_at = CURRENT_TIMESTAMP
WHERE approval_id = :approvalId AND version_no = :expectedVersion
