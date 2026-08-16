USE bzaDB;

CREATE TABLE IF NOT EXISTS bza_login_operation (
    operation_id VARCHAR(100) NOT NULL,
    admin_user_id BIGINT NOT NULL,
    admin_login_id VARCHAR(80) NOT NULL,
    operation_status VARCHAR(20) NOT NULL DEFAULT 'PROCESSING',
    created_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (operation_id),
    INDEX ix_bza_login_operation_user_time (admin_user_id, created_at),
    CONSTRAINT fk_bza_login_operation_user FOREIGN KEY (admin_user_id)
        REFERENCES bza_admin_user(admin_user_id) ON DELETE CASCADE,
    CONSTRAINT ck_bza_login_operation_status CHECK (operation_status IN ('PROCESSING','SUCCESS'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

ALTER TABLE bza_refresh_token
    ADD COLUMN login_operation_id VARCHAR(100) NULL AFTER transaction_id,
    ADD INDEX ix_bza_refresh_token_login_operation (login_operation_id, revoked_yn);
