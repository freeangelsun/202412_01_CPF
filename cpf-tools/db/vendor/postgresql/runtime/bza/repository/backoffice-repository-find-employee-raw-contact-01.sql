SELECT employee_no AS employeeNo,
       email,
       mobile_no AS mobileNo,
       office_phone_no AS officePhoneNo
  FROM bza_employee
 WHERE employee_no = :employeeNo
   AND use_yn = 'Y';
