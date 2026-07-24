INSERT INTO bza_employee (
    employee_no, admin_user_id, organization_code, employee_name, position_code, job_title_code,
    manager_employee_no, employment_status, join_date, leave_date, email, mobile_no,
    delegated_approver_no, absence_from, absence_to, use_yn, created_by, updated_by
) VALUES (
    :employeeNo, :adminUserId, :organizationCode, :employeeName, :positionCode, :jobTitleCode,
    :managerEmployeeNo, :employmentStatus, :joinDate, :leaveDate, :email, :mobileNo,
    :delegatedApproverNo, :absenceFrom, :absenceTo, :useYn, :requestUser, :requestUser
)
ON DUPLICATE KEY UPDATE
    admin_user_id = VALUES(admin_user_id), organization_code = VALUES(organization_code),
    employee_name = VALUES(employee_name), position_code = VALUES(position_code),
    job_title_code = VALUES(job_title_code), manager_employee_no = VALUES(manager_employee_no),
    employment_status = VALUES(employment_status), join_date = VALUES(join_date),
    leave_date = VALUES(leave_date), email = VALUES(email), mobile_no = VALUES(mobile_no),
    delegated_approver_no = VALUES(delegated_approver_no), absence_from = VALUES(absence_from),
    absence_to = VALUES(absence_to), use_yn = VALUES(use_yn),
    updated_by = VALUES(updated_by), updated_at = CURRENT_TIMESTAMP
