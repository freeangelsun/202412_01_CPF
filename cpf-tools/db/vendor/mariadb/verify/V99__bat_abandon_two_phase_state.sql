-- Verify CPF BAT abandon two-phase state after V99.
SELECT CASE WHEN EXISTS (
  SELECT 1 FROM information_schema.table_constraints tc
  JOIN information_schema.check_constraints cc
    ON cc.constraint_schema = tc.constraint_schema
   AND cc.constraint_name = tc.constraint_name
 WHERE tc.table_schema = DATABASE()
   AND tc.table_name = 'cpf_batch_execution_control'
   AND tc.constraint_name = 'ck_cpf_bat_control_status'
   AND UPPER(cc.check_clause) LIKE '%ABANDONING%'
) THEN 1 ELSE CAST('CPF-BAT-V99-VERIFY-FAILED' AS UNSIGNED) END AS cpf_bat_v99_verified;
