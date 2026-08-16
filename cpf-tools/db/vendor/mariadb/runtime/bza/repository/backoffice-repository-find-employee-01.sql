SELECT employee_id AS employeeId,employee_no AS employeeNo,admin_user_id AS adminUserId,
       organization_code AS organizationCode,employee_name AS employeeName,position_code AS positionCode,
       job_title_code AS jobTitleCode,manager_employee_no AS managerEmployeeNo,employment_status AS employmentStatus,
       join_date AS joinDate,leave_date AS leaveDate,email,mobile_no AS mobileNo,office_phone_no AS officePhoneNo,
       use_yn AS useYn,version_no AS versionNo,created_at AS createdAt,updated_at AS updatedAt
  FROM bza_employee WHERE employee_no=:employeeNo
