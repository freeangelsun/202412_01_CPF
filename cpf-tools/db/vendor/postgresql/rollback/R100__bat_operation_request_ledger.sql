-- Fail closed: the BAT dangerous-operation ledger is audit/idempotency evidence.
-- Export/reconcile all rows before rolling V100 back.
DO $$
BEGIN
  IF EXISTS (SELECT 1 FROM bat_operation_request LIMIT 1) THEN
    RAISE EXCEPTION 'CPF-BAT-R100-ROLLBACK-NONEMPTY: export/reconcile bat_operation_request before rollback';
  END IF;
END
$$;
DROP TABLE bat_operation_request;
