-- BZA 운영 편의 기능의 알림·첨부·저장 검색·다운로드 감사 테이블을 추가합니다.
USE bzaDB;

CREATE TABLE IF NOT EXISTS bza_notification (
    notification_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '업무 알림 순번',
    recipient_login_id VARCHAR(100) NOT NULL COMMENT '수신 BZA 로그인 ID',
    notification_type VARCHAR(40) NOT NULL COMMENT '업무 알림 유형',
    title VARCHAR(200) NOT NULL COMMENT '업무 알림 제목',
    message_body VARCHAR(2000) NOT NULL COMMENT '업무 알림 내용',
    reference_type VARCHAR(80) NULL COMMENT '참조 업무 유형',
    reference_id VARCHAR(120) NULL COMMENT '참조 업무 ID',
    read_yn CHAR(1) NOT NULL DEFAULT 'N' COMMENT '읽음 여부',
    read_at DATETIME NULL COMMENT '읽음 일시',
    use_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '사용 여부',
    created_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (notification_id),
    INDEX ix_bza_notification_recipient (recipient_login_id, read_yn, use_yn, created_at),
    INDEX ix_bza_notification_reference (reference_type, reference_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='BZA 업무 알림';

CREATE TABLE IF NOT EXISTS bza_attachment (
    attachment_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '첨부파일 순번',
    attachment_group_id VARCHAR(80) NOT NULL COMMENT '첨부파일 그룹 ID',
    original_file_name VARCHAR(255) NOT NULL COMMENT '원본 파일명',
    stored_file_name VARCHAR(255) NOT NULL COMMENT '저장 파일명',
    storage_key VARCHAR(500) NOT NULL COMMENT '저장소 상대 key',
    content_type VARCHAR(120) NOT NULL COMMENT '파일 Content-Type',
    file_size BIGINT NOT NULL COMMENT '파일 크기 byte',
    checksum_sha256 CHAR(64) NOT NULL COMMENT '파일 SHA-256 checksum',
    scan_status VARCHAR(40) NOT NULL DEFAULT 'PENDING' COMMENT '보안 검사 상태',
    use_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '사용 여부',
    created_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (attachment_id),
    UNIQUE KEY uk_bza_attachment_storage_key (storage_key),
    INDEX ix_bza_attachment_group (attachment_group_id, use_yn, created_at),
    INDEX ix_bza_attachment_checksum (checksum_sha256)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='BZA 첨부파일 메타';

CREATE TABLE IF NOT EXISTS bza_saved_search (
    saved_search_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '저장 검색 순번',
    owner_login_id VARCHAR(100) NOT NULL COMMENT '저장 검색 소유 로그인 ID',
    screen_code VARCHAR(80) NOT NULL COMMENT '적용 화면 코드',
    search_name VARCHAR(120) NOT NULL COMMENT '저장 검색명',
    criteria_json LONGTEXT NOT NULL COMMENT '검색 조건 JSON',
    shared_yn CHAR(1) NOT NULL DEFAULT 'N' COMMENT '공유 여부',
    use_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '사용 여부',
    created_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (saved_search_id),
    UNIQUE KEY uk_bza_saved_search_owner (owner_login_id, screen_code, search_name),
    INDEX ix_bza_saved_search_screen (screen_code, shared_yn, use_yn)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='BZA 저장 검색';

CREATE TABLE IF NOT EXISTS bza_download_audit (
    download_audit_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '다운로드 감사 순번',
    actor_id VARCHAR(100) NOT NULL COMMENT '다운로드 처리 로그인 ID',
    download_code VARCHAR(80) NOT NULL COMMENT '다운로드 기능 코드',
    reason VARCHAR(500) NOT NULL COMMENT '다운로드 사유',
    filter_json LONGTEXT NULL COMMENT '다운로드 검색 조건 JSON',
    row_count BIGINT NOT NULL DEFAULT 0 COMMENT '다운로드 결과 건수',
    result_status VARCHAR(40) NOT NULL COMMENT '다운로드 결과 상태',
    file_name VARCHAR(255) NULL COMMENT '다운로드 파일명',
    masking_applied_yn CHAR(1) NOT NULL DEFAULT 'N' COMMENT '마스킹 적용 여부',
    transaction_global_id VARCHAR(100) NULL COMMENT 'CPF 전역 거래 ID',
    created_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'SYSTEM' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (download_audit_id),
    INDEX ix_bza_download_audit_actor (actor_id, created_at),
    INDEX ix_bza_download_audit_transaction (transaction_global_id),
    INDEX ix_bza_download_audit_status (result_status, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='BZA 다운로드 감사';
