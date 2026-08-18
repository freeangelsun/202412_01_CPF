SELECT CASE WHEN COUNT(*) = 2 THEN 1 ELSE 0 END
FROM information_schema.tables
WHERE table_schema = current_schema()
  AND UPPER(table_name) IN ('OPS_OPERATION_CATALOG','OPS_OPERATION_POLICY')
