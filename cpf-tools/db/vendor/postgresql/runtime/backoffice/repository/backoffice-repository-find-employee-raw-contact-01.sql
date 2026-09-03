SELECT employee_no AS employeeNo,
       email AS email,
       mobile_no AS mobileNo,
       office_phone_no AS officePhoneNo
  FROM MBW_EMPLOYEE
 WHERE employee_no = :employeeNo
   AND use_yn = 'Y'
