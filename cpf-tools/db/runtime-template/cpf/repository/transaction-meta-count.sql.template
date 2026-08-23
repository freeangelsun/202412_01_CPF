SELECT COUNT(*)
  FROM OPS_OPERATION_CATALOG c
  JOIN OPS_OPERATION_POLICY p ON p.operation_id = c.operation_id
 WHERE (? IS NULL OR c.system_code = ?)
   AND (? IS NULL OR p.enabled_yn = ?)
   AND (? IS NULL OR c.operation_id LIKE ?)
