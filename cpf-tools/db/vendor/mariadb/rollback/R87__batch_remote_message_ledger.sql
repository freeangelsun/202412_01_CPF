DELIMITER $$
CREATE PROCEDURE cpf_rb_v87_guard()
BEGIN
    IF EXISTS (SELECT 1 FROM bat_remote_message_ledger WHERE status_cd = 'PROCESSING' AND lease_until > CURRENT_TIMESTAMP(6)) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'R87 denied: active remote messages exist';
    END IF;
END$$
CALL cpf_rb_v87_guard()$$
DROP PROCEDURE cpf_rb_v87_guard$$
DROP TABLE bat_remote_message_ledger$$
DELIMITER ;
