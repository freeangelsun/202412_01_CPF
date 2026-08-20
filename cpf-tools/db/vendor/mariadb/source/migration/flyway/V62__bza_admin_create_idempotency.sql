-- V62: MBW 최초 관리자 생성 operationId 멱등 계약
USE backofficeDB;
ALTER TABLE mbw_admin_user
    ADD COLUMN IF NOT EXISTS create_operation_id VARCHAR(100) NULL COMMENT '관리자 생성 멱등 Operation ID' AFTER version_no,
    ADD UNIQUE INDEX IF NOT EXISTS uk_mbw_admin_user_create_operation (create_operation_id);
