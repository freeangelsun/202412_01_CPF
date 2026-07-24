-- CPF 표준 온라인·배치 실행 카탈로그를 추가합니다.
-- 기존 migration 체크섬을 보존하기 위해 신규 버전에서만 생성합니다.

USE cpfDB;

CREATE TABLE IF NOT EXISTS cpf_standard_execution (
    standard_execution_id VARCHAR(20) NOT NULL COMMENT 'CPF 온라인·배치 표준 실행 ID',
    execution_name VARCHAR(150) NOT NULL COMMENT '표준 실행명',
    execution_type VARCHAR(20) NOT NULL COMMENT '실행 유형 ONLINE 또는 BATCH',
    owner_domain VARCHAR(20) NOT NULL COMMENT '실행 소유 주제영역',
    source_module VARCHAR(20) NOT NULL COMMENT '발견 소스 모듈',
    source_class VARCHAR(255) NOT NULL COMMENT '선언 클래스명',
    source_method VARCHAR(150) NOT NULL COMMENT '선언 메서드명',
    endpoint VARCHAR(500) NULL COMMENT '연결 API 또는 배치 endpoint',
    operation_id VARCHAR(150) NULL COMMENT '연결 OpenAPI operation ID',
    source_version VARCHAR(100) NOT NULL COMMENT '소스 버전 또는 빌드 식별자',
    registration_status VARCHAR(30) NOT NULL DEFAULT 'REGISTERED' COMMENT '등록 상태',
    first_registered_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '최초 등록일시',
    last_discovered_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '최근 발견일시',
    created_by VARCHAR(100) NOT NULL DEFAULT 'CPF' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'CPF' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (standard_execution_id),
    INDEX ix_cpf_standard_execution_type (execution_type, registration_status),
    INDEX ix_cpf_standard_execution_owner (owner_domain, source_module),
    INDEX ix_cpf_standard_execution_source (source_class, source_method),
    CONSTRAINT ck_cpf_standard_execution_id CHECK (
        standard_execution_id REGEXP '^[OB][A-Z]{3}-[A-Z0-9]{3}-[A-Z0-9]{2}-[0-9]{4}$'
        AND RIGHT(standard_execution_id, 4) <> '0000'
    ),
    CONSTRAINT ck_cpf_standard_execution_type CHECK (execution_type IN ('ONLINE', 'BATCH'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='CPF 표준 온라인·배치 실행 카탈로그';
