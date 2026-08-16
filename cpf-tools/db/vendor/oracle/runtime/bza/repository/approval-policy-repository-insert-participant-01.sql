INSERT INTO bza_approval_participant (
    approval_id, approval_line_id, step_no, approver_employee_no,
    approver_name_snapshot, organization_code_snapshot, position_code_snapshot,
    job_title_code_snapshot, delegated_from_employee_no, resolution_source,
    decision_status, created_by, updated_by
)
SELECT :approvalId, :approvalLineId, :stepNo, :approverEmployeeNo,
       e.employee_name, :organizationCode, :positionCode,
       :jobTitleCode, :delegatedFrom, :resolutionSource,
       'WAITING', :operatorId, :operatorId
  FROM bza_employee e WHERE e.employee_no=:approverEmployeeNo
