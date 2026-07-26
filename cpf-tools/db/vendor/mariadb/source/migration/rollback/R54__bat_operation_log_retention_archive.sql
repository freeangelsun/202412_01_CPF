-- R14 V54 guarded rollback. 보관된 운영 로그가 있으면 테이블 삭제를 거부한다.
USE batDB;
DELIMITER $$
CREATE PROCEDURE cpf_r54_guarded_rollback()
BEGIN
    DECLARE archived_rows BIGINT DEFAULT 0;
    SELECT COUNT(*) INTO archived_rows FROM bat_operation_log_archive;
    IF archived_rows > 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='R54 rollback blocked: archived BAT operation logs exist';
    END IF;
    DROP TABLE IF EXISTS bat_operation_log_archive;
END$$
DELIMITER ;
CALL cpf_r54_guarded_rollback();
DROP PROCEDURE cpf_r54_guarded_rollback;
