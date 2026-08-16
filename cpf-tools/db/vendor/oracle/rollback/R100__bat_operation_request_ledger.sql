-- Fail closed: the BAT dangerous-operation ledger is audit/idempotency evidence.
-- Export/reconcile all rows before rolling V100 back.
DECLARE
  v_row_count NUMBER;
BEGIN
  SELECT COUNT(*) INTO v_row_count
    FROM bat_operation_request
   WHERE ROWNUM = 1;
  IF v_row_count > 0 THEN
    RAISE_APPLICATION_ERROR(-20996,
      'CPF-BAT-R100-ROLLBACK-NONEMPTY: export/reconcile bat_operation_request before rollback');
  END IF;
END;
/
DROP TABLE bat_operation_request;
