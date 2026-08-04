-- Fail closed: the BAT dangerous-operation ledger is audit/idempotency evidence.
-- Export/reconcile all rows before rolling V100 back.
DELIMITER //
CREATE PROCEDURE cpf_assert_empty_bat_operation_request_r100()
BEGIN
  IF EXISTS (SELECT 1 FROM bat_operation_request LIMIT 1) THEN
    SIGNAL SQLSTATE '45000'
      SET MESSAGE_TEXT = 'CPF-BAT-R100-ROLLBACK-NONEMPTY: export/reconcile bat_operation_request before rollback';
  END IF;
END//
CALL cpf_assert_empty_bat_operation_request_r100()//
DROP PROCEDURE cpf_assert_empty_bat_operation_request_r100//
DELIMITER ;
DROP TABLE bat_operation_request;
