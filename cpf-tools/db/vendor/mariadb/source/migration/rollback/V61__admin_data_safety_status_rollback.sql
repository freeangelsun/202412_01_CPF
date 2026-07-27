-- V61 safe rollback: V61에서 추가한 상태/버전/멱등 컬럼과 제약을 제거합니다.
-- ACTIVE -> EMPLOYED 정규화는 의미 보존 데이터 변경이므로 역변환하지 않습니다.
-- role_code는 V61 이후 Role 미부여 PENDING 계정이 존재할 수 있으므로 nullable을 유지합니다.
-- nullable 완화는 V60 코드와 호환되며 가짜 Role 주입이나 사용자 삭제를 하지 않습니다.
USE bzaDB;
ALTER TABLE bza_employee DROP CONSTRAINT IF EXISTS ck_bza_employee_status;
ALTER TABLE bza_admin_user DROP CONSTRAINT IF EXISTS ck_bza_admin_user_status;
ALTER TABLE bza_admin_user DROP INDEX IF EXISTS ix_bza_admin_user_status;
ALTER TABLE bza_admin_user
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
