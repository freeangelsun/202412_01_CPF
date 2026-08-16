-- V61 exact rollback to the V60-compatible schema.
-- 운영 순서: V60 호환 Binary 배포 -> 본 DB rollback -> V60 smoke. 현재 V61 Binary 상태에서 DB만 rollback하는 것은 금지합니다.
-- role_code NULL 계정이 있으면 NOT NULL 복원에서 명시적으로 실패합니다. 임의 Role 주입/계정 삭제로 우회하지 않습니다.
USE bzaDB;
ALTER TABLE bza_employee DROP CONSTRAINT IF EXISTS ck_bza_employee_status;
ALTER TABLE bza_employee
    ADD CONSTRAINT ck_bza_employee_status CHECK (employment_status IN ('ACTIVE','EMPLOYED','ON_LEAVE','SECONDMENT','DISPATCHED','RETIRED','TERMINATED'));
ALTER TABLE bza_admin_user DROP CONSTRAINT IF EXISTS ck_bza_admin_user_status;
ALTER TABLE bza_admin_user DROP INDEX IF EXISTS ix_bza_admin_user_status;
ALTER TABLE bza_admin_user
    MODIFY COLUMN role_code VARCHAR(50) NOT NULL COMMENT '호환용 기본 역할 코드; 실제 권한은 bza_user_role 다중 매핑 정본',
    DROP COLUMN IF EXISTS version_no,
    DROP COLUMN IF EXISTS account_status;

USE admDB;
ALTER TABLE adm_operator DROP CONSTRAINT IF EXISTS ck_adm_operator_status;
ALTER TABLE adm_operator DROP INDEX IF EXISTS ix_adm_operator_status;
ALTER TABLE adm_operator DROP INDEX IF EXISTS uk_adm_operator_create_operation;
ALTER TABLE adm_operator_profile
    DROP COLUMN IF EXISTS VERSION_NO,
    DROP COLUMN IF EXISTS DISPLAY_NAME;
ALTER TABLE adm_operator
    DROP COLUMN IF EXISTS CREATE_OPERATION_ID,
    DROP COLUMN IF EXISTS VERSION_NO,
    DROP COLUMN IF EXISTS ACCOUNT_STATUS;
