DELIMITER $$
CREATE PROCEDURE cpf_rb_v86_guard()
BEGIN
    IF EXISTS (SELECT 1 FROM cpf_bff_credential_vault WHERE refresh_expires_at > CURRENT_TIMESTAMP(6)) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'R86 denied: active BFF credentials exist';
    END IF;
END$$
CALL cpf_rb_v86_guard()$$
DROP PROCEDURE cpf_rb_v86_guard$$
DROP TABLE cpf_bff_credential_vault$$
DELIMITER ;
