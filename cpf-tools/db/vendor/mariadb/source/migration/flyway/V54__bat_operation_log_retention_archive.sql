-- R14 BAT operation log retention archive.
USE batDB;
CREATE TABLE IF NOT EXISTS bat_operation_log_archive (
    operation_id BIGINT NOT NULL COMMENT '원본 배치 운영 로그 순번',
    job_id VARCHAR(100) NOT NULL COMMENT '배치 Job ID',
    execution_id BIGINT NULL COMMENT '배치 실행 순번',
    operation_type VARCHAR(30) NOT NULL COMMENT '운영 작업 유형',
    operator_id VARCHAR(100) NOT NULL COMMENT '운영자 ID',
    reason VARCHAR(500) NOT NULL COMMENT '운영 사유',
    before_data LONGTEXT NULL COMMENT '작업 전 데이터',
    after_data LONGTEXT NULL COMMENT '작업 후 데이터',
    result_type CHAR(1) NOT NULL DEFAULT 'S' COMMENT '결과 유형',
    result_message VARCHAR(1000) NULL COMMENT '결과 메시지',
    created_by VARCHAR(100) NOT NULL DEFAULT 'BAT' COMMENT '원본 등록자',
    created_at DATETIME NOT NULL COMMENT '원본 등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'BAT' COMMENT '원본 수정자',
    updated_at DATETIME NOT NULL COMMENT '원본 수정일시',
    archived_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '보관 일시',
    archived_by VARCHAR(100) NOT NULL COMMENT '보관 수행자',
    archive_reason VARCHAR(500) NOT NULL COMMENT '보관 사유',
    PRIMARY KEY (operation_id),
    INDEX ix_bat_operation_archive_job_time (job_id, created_at),
    INDEX ix_bat_operation_archive_archived (archived_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='BAT 운영 로그 보관소';
