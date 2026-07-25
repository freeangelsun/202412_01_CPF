-- CPF R9: scoped break-glass session with TTL and post review
CREATE TABLE IF NOT EXISTS adm_break_glass_session (
    session_id CHAR(36) NOT NULL, operator_id VARCHAR(100) NOT NULL, scope_type VARCHAR(60) NOT NULL,
    scope_value VARCHAR(200) NOT NULL, reason VARCHAR(1000) NOT NULL, status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    expires_at DATETIME(3) NOT NULL, closed_at DATETIME(3) NULL, close_reason VARCHAR(1000) NULL,
    post_review_status VARCHAR(20) NOT NULL DEFAULT 'PENDING', reviewed_by VARCHAR(100) NULL,
    reviewed_at DATETIME(3) NULL, review_reason VARCHAR(1000) NULL, created_by VARCHAR(100) NOT NULL,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3), updated_by VARCHAR(100) NOT NULL,
    updated_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY(session_id), INDEX ix_adm_break_glass_operator(operator_id,status,expires_at),
    INDEX ix_adm_break_glass_scope(scope_type,scope_value,status), INDEX ix_adm_break_glass_review(post_review_status,closed_at),
    CONSTRAINT ck_adm_break_glass_status CHECK(status IN ('ACTIVE','CLOSED','EXPIRED')),
    CONSTRAINT ck_adm_break_glass_review CHECK(post_review_status IN ('PENDING','APPROVED','REJECTED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
