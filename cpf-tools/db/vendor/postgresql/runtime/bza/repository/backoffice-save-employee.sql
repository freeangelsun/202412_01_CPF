INSERT INTO bza_employee (
    employee_no, admin_user_id, organization_code, employee_name, position_code, job_title_code,
    manager_employee_no, employment_status, join_date, leave_date, email, mobile_no,
    delegated_approver_no, absence_from, absence_to, use_yn, created_by, updated_by
) VALUES (
    :employeeNo, :adminUserId, :organizationCode, :employeeName, :positionCode, :jobTitleCode,
    :managerEmployeeNo, :employmentStatus, :joinDate, :leaveDate, :email, :mobileNo,
    :delegatedApproverNo, :absenceFrom, :absenceTo, :useYn, :requestUser, :requestUser
)
ON CONFLICT (employee_no) DO UPDATE SET
    admin_user_id = EXCLUDED.admin_user_id, organization_code = EXCLUDED.organization_code,
    employee_name = EXCLUDED.employee_name, position_code = EXCLUDED.position_code,
    job_title_code = EXCLUDED.job_title_code, manager_employee_no = EXCLUDED.manager_employee_no,
    employment_status = EXCLUDED.employment_status, join_date = EXCLUDED.join_date,
    leave_date = EXCLUDED.leave_date, email = EXCLUDED.email, mobile_no = EXCLUDED.mobile_no,
    delegated_approver_no = EXCLUDED.delegated_approver_no, absence_from = EXCLUDED.absence_from,
    absence_to = EXCLUDED.absence_to, use_yn = EXCLUDED.use_yn,
    updated_by = EXCLUDED.updated_by, updated_at = CURRENT_TIMESTAMP
