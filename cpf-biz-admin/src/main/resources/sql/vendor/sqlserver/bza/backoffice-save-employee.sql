MERGE INTO bza_employee AS target
USING (
    SELECT :employeeNo employee_no, :adminUserId admin_user_id, :organizationCode organization_code,
           :employeeName employee_name, :positionCode position_code, :jobTitleCode job_title_code,
           :managerEmployeeNo manager_employee_no, :employmentStatus employment_status,
           :joinDate join_date, :leaveDate leave_date, :email email, :mobileNo mobile_no,
           :delegatedApproverNo delegated_approver_no, :absenceFrom absence_from,
           :absenceTo absence_to, :useYn use_yn, :requestUser request_user
) AS source
ON (target.employee_no = source.employee_no)
WHEN MATCHED THEN UPDATE SET
    target.admin_user_id = source.admin_user_id, target.organization_code = source.organization_code,
    target.employee_name = source.employee_name, target.position_code = source.position_code,
    target.job_title_code = source.job_title_code, target.manager_employee_no = source.manager_employee_no,
    target.employment_status = source.employment_status, target.join_date = source.join_date,
    target.leave_date = source.leave_date, target.email = source.email, target.mobile_no = source.mobile_no,
    target.delegated_approver_no = source.delegated_approver_no, target.absence_from = source.absence_from,
    target.absence_to = source.absence_to, target.use_yn = source.use_yn,
    target.updated_by = source.request_user, target.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (
    employee_no, admin_user_id, organization_code, employee_name, position_code, job_title_code,
    manager_employee_no, employment_status, join_date, leave_date, email, mobile_no,
    delegated_approver_no, absence_from, absence_to, use_yn, created_by, updated_by
) VALUES (
    source.employee_no, source.admin_user_id, source.organization_code, source.employee_name,
    source.position_code, source.job_title_code, source.manager_employee_no, source.employment_status,
    source.join_date, source.leave_date, source.email, source.mobile_no, source.delegated_approver_no,
    source.absence_from, source.absence_to, source.use_yn, source.request_user, source.request_user
);
