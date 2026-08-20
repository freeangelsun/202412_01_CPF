-- R14 V53 guarded rollback. 이력 또는 체인 head를 조용히 소실하지 않는다.
USE backofficeDB;
DELIMITER $$
CREATE PROCEDURE cpf_r53_guarded_rollback()
BEGIN
    DECLARE duplicate_pairs BIGINT DEFAULT 0;
    DECLARE chain_records BIGINT DEFAULT 0;

    SELECT COUNT(*) INTO duplicate_pairs
      FROM (
          SELECT admin_user_id, role_code
            FROM mbw_user_role
           GROUP BY admin_user_id, role_code
          HAVING COUNT(*) > 1
      ) d;
    IF duplicate_pairs > 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='R53 rollback blocked: role regrant history would be lost';
    END IF;

    SELECT COUNT(*) INTO chain_records
      FROM mbw_audit_chain_lock
     WHERE current_hash IS NOT NULL OR last_audit_id IS NOT NULL;
    IF chain_records > 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='R53 rollback blocked: audit chain head is in use';
    END IF;

    DROP TABLE IF EXISTS mbw_audit_chain_lock;
    DROP INDEX IF EXISTS ix_mbw_permission_scope ON mbw_permission;
    CREATE UNIQUE INDEX IF NOT EXISTS uk_mbw_permission ON mbw_permission(role_code, menu_code, button_code);

    ALTER TABLE mbw_user_role
        DROP INDEX uk_mbw_user_role_operation,
        DROP INDEX ix_mbw_user_role_user,
        DROP PRIMARY KEY,
        DROP COLUMN user_role_id,
        DROP COLUMN grant_reason,
        DROP COLUMN operation_id,
        DROP COLUMN version_no,
        ADD PRIMARY KEY (admin_user_id, role_code);

    ALTER TABLE mbw_menu DROP COLUMN IF EXISTS version_no;
    ALTER TABLE mbw_role DROP COLUMN IF EXISTS version_no;
    ALTER TABLE mbw_permission DROP COLUMN IF EXISTS version_no;
    ALTER TABLE mbw_organization DROP COLUMN IF EXISTS version_no;
    ALTER TABLE mbw_position DROP COLUMN IF EXISTS version_no;
    ALTER TABLE mbw_job_title DROP COLUMN IF EXISTS version_no;
    ALTER TABLE mbw_employee DROP COLUMN IF EXISTS version_no;
    ALTER TABLE mbw_employee_assignment DROP COLUMN IF EXISTS version_no;
    ALTER TABLE mbw_organization_responsibility DROP COLUMN IF EXISTS version_no;
END$$
DELIMITER ;
CALL cpf_r53_guarded_rollback();
DROP PROCEDURE cpf_r53_guarded_rollback;
