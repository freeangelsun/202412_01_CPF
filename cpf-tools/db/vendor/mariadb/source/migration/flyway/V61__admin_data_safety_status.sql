-- V61: ADM/BZA 데이터 안전성 상태·버전 모델
USE admDB;

ALTER TABLE adm_operator
    ADD COLUMN IF NOT EXISTS ACCOUNT_STATUS VARCHAR(30) NOT NULL DEFAULT 'PENDING_ACTIVATION' COMMENT '계정 상태' AFTER PASSWORD_HASH,
    ADD COLUMN IF NOT EXISTS VERSION_NO BIGINT NOT NULL DEFAULT 0 COMMENT '낙관적 잠금 버전' AFTER ACCOUNT_STATUS,
    ADD COLUMN IF NOT EXISTS CREATE_OPERATION_ID VARCHAR(100) NULL COMMENT '운영자 생성 멱등 Operation ID' AFTER VERSION_NO;

UPDATE adm_operator
   SET ACCOUNT_STATUS = CASE
       WHEN USE_YN <> 'Y' THEN 'DISABLED'
       WHEN LOCKED_YN = 'Y' THEN 'LOCKED'
       ELSE 'ACTIVE'
   END
 WHERE ACCOUNT_STATUS IS NULL OR ACCOUNT_STATUS = 'PENDING_ACTIVATION';

ALTER TABLE adm_operator
    ADD UNIQUE INDEX IF NOT EXISTS uk_adm_operator_create_operation (CREATE_OPERATION_ID),
    ADD INDEX IF NOT EXISTS ix_adm_operator_status (ACCOUNT_STATUS, USE_YN),
    DROP CONSTRAINT IF EXISTS ck_adm_operator_status,
    ADD CONSTRAINT ck_adm_operator_status CHECK (ACCOUNT_STATUS IN ('PENDING_ACTIVATION','ACTIVE','LOCKED','SUSPENDED','DISABLED'));

ALTER TABLE adm_operator_profile
    ADD COLUMN IF NOT EXISTS DISPLAY_NAME VARCHAR(100) NULL COMMENT 'Directory/Profile 표시 이름' AFTER OPERATOR_ID,
    ADD COLUMN IF NOT EXISTS VERSION_NO BIGINT NOT NULL DEFAULT 0 COMMENT 'Profile 낙관적 잠금 버전' AFTER EFFECTIVE_TO;

UPDATE adm_operator_profile p
JOIN adm_operator u ON u.OPERATOR_ID = p.OPERATOR_ID
   SET p.DISPLAY_NAME = COALESCE(p.DISPLAY_NAME, u.OPERATOR_NAME);

USE bzaDB;

ALTER TABLE bza_admin_user
    MODIFY COLUMN role_code VARCHAR(50) NULL COMMENT '호환용 대표 역할 코드; 실제 권한은 bza_user_role 정본',
    ADD COLUMN IF NOT EXISTS account_status VARCHAR(30) NOT NULL DEFAULT 'PENDING_ACTIVATION' COMMENT '계정 상태' AFTER role_code,
    ADD COLUMN IF NOT EXISTS version_no BIGINT NOT NULL DEFAULT 0 COMMENT '낙관적 잠금 버전' AFTER account_status;

UPDATE bza_admin_user
   SET account_status = CASE
       WHEN use_yn <> 'Y' THEN 'DISABLED'
       WHEN lock_yn = 'Y' THEN 'LOCKED'
       ELSE 'ACTIVE'
   END
 WHERE account_status IS NULL OR account_status = 'PENDING_ACTIVATION';

ALTER TABLE bza_admin_user
    ADD INDEX IF NOT EXISTS ix_bza_admin_user_status (account_status, use_yn),
    DROP CONSTRAINT IF EXISTS ck_bza_admin_user_status,
    ADD CONSTRAINT ck_bza_admin_user_status CHECK (account_status IN ('PENDING_ACTIVATION','ACTIVE','LOCKED','SUSPENDED','DISABLED'));

-- V60 이전 ACTIVE 혼재값은 과거 재직 상태 alias로 간주하여 EMPLOYED로 정규화합니다.
UPDATE bza_employee SET employment_status = 'EMPLOYED' WHERE employment_status = 'ACTIVE';

ALTER TABLE bza_employee
    DROP CONSTRAINT IF EXISTS ck_bza_employee_status,
    ADD CONSTRAINT ck_bza_employee_status CHECK (employment_status IN ('EMPLOYED','ON_LEAVE','SECONDMENT','DISPATCHED','RETIRED','TERMINATED'));
