param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..").Path,
    [string] $MappingEvidence = "specs/evidence/20260716_01/standard-execution-id-migration-apply.sanitized.json"
)

$ErrorActionPreference = "Stop"
$Root = (Resolve-Path -LiteralPath $Root).Path
$mappingPath = Join-Path $Root $MappingEvidence
$mapping = @(Get-Content -Raw -Encoding UTF8 $mappingPath | ConvertFrom-Json | Select-Object -ExpandProperty mappings)
if ($mapping.Count -eq 0) {
    throw "표준 실행 ID 매핑 evidence가 비어 있습니다: $MappingEvidence"
}

$mapValues = ($mapping | ForEach-Object {
        "    ('$($_.legacyId)', '$($_.currentId)')"
    }) -join ",`n"
$seedValues = ($mapping | ForEach-Object {
        "    ('$($_.legacyId)', '$($_.currentId)', 'CPF O/S/B 10자리 표준 전환', 'PFW_SEED', 'PFW_SEED')"
    }) -join ",`n"

# 기존 데이터는 임시 매핑으로 먼저 갱신한 뒤 제약과 컬럼 길이를 줄여 데이터 절단을 차단합니다.
$migration = @"
-- 기존 실행 카탈로그 데이터를 명시적 매핑으로 전환한 뒤 O/S/B 10자리 규격을 적용합니다.
-- 알려지지 않은 구형 ID가 있으면 신규 CHECK 제약 추가 단계에서 실패해 임의 절단을 방지합니다.
USE pfwDB;

ALTER TABLE pfw_standard_execution DROP CONSTRAINT IF EXISTS ck_pfw_standard_execution_id;
ALTER TABLE pfw_standard_execution DROP CONSTRAINT IF EXISTS ck_pfw_standard_execution_type;

CREATE TEMPORARY TABLE tmp_pfw_standard_execution_id_map (
    legacy_execution_id VARCHAR(32) NOT NULL,
    standard_execution_id CHAR(10) NOT NULL,
    PRIMARY KEY (legacy_execution_id),
    UNIQUE KEY uk_tmp_pfw_standard_execution_id_map_current (standard_execution_id)
) ENGINE=InnoDB;

INSERT INTO tmp_pfw_standard_execution_id_map (legacy_execution_id, standard_execution_id) VALUES
$mapValues;

UPDATE pfw_standard_execution current_execution
JOIN tmp_pfw_standard_execution_id_map id_map
  ON id_map.legacy_execution_id = current_execution.standard_execution_id
SET current_execution.standard_execution_id = id_map.standard_execution_id,
    current_execution.execution_type = CASE LEFT(id_map.standard_execution_id, 1)
        WHEN 'O' THEN 'ONLINE'
        WHEN 'S' THEN 'SHARED'
        ELSE 'BATCH'
    END,
    current_execution.updated_by = 'FLYWAY_V32',
    current_execution.updated_at = CURRENT_TIMESTAMP;

-- 길이 축소 전에 실행하므로 미매핑 구형 ID가 있으면 migration을 즉시 중단합니다.
ALTER TABLE pfw_standard_execution
    ADD CONSTRAINT ck_pfw_standard_execution_id CHECK (
        standard_execution_id REGEXP '^[OSB][A-Z]{3}[A-Z0-9]{2}[0-9]{4}$'
        AND RIGHT(standard_execution_id, 4) <> '0000'
    );

ALTER TABLE pfw_standard_execution
    MODIFY standard_execution_id CHAR(10) NOT NULL COMMENT 'CPF O·S·B 10자리 표준 실행 ID',
    MODIFY execution_type VARCHAR(20) NOT NULL COMMENT '실행 유형 ONLINE, SHARED 또는 BATCH',
    ADD COLUMN IF NOT EXISTS http_method VARCHAR(10) NULL COMMENT 'HTTP 진입 method' AFTER source_method,
    ADD COLUMN IF NOT EXISTS description VARCHAR(1000) NULL COMMENT '실행 기능 설명' AFTER operation_id,
    ADD COLUMN IF NOT EXISTS required_permission VARCHAR(150) NULL COMMENT '필수 실행 권한 코드' AFTER description,
    ADD COLUMN IF NOT EXISTS audit_reason_required_yn CHAR(1) NOT NULL DEFAULT 'N' COMMENT '감사 사유 필수 여부' AFTER required_permission,
    ADD COLUMN IF NOT EXISTS visibility VARCHAR(20) NOT NULL DEFAULT 'INTERNAL' COMMENT 'PUBLIC 또는 INTERNAL 노출 범위' AFTER audit_reason_required_yn,
    ADD COLUMN IF NOT EXISTS direct_allowed_yn CHAR(1) NOT NULL DEFAULT 'Y' COMMENT '업무 URL 직접 호출 허용 여부' AFTER visibility,
    ADD COLUMN IF NOT EXISTS gateway_allowed_yn CHAR(1) NOT NULL DEFAULT 'N' COMMENT '공개 PFW Gateway 호출 허용 여부' AFTER direct_allowed_yn,
    ADD CONSTRAINT ck_pfw_standard_execution_type CHECK (execution_type IN ('ONLINE', 'SHARED', 'BATCH'));

CREATE TABLE IF NOT EXISTS pfw_standard_execution_alias (
    legacy_execution_id VARCHAR(32) NOT NULL COMMENT '조회 호환용 구형 실행 ID',
    standard_execution_id CHAR(10) NOT NULL COMMENT '현재 10자리 표준 실행 ID',
    migration_reason VARCHAR(300) NOT NULL COMMENT 'ID 전환 사유',
    retired_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '구형 ID 사용 종료일시',
    created_by VARCHAR(100) NOT NULL DEFAULT 'PFW' COMMENT '등록자',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '등록일시',
    updated_by VARCHAR(100) NOT NULL DEFAULT 'PFW' COMMENT '수정자',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시',
    PRIMARY KEY (legacy_execution_id),
    UNIQUE KEY uk_pfw_standard_execution_alias_current (standard_execution_id, legacy_execution_id),
    CONSTRAINT ck_pfw_standard_execution_alias_current CHECK (
        standard_execution_id REGEXP '^[OSB][A-Z]{3}[A-Z0-9]{2}[0-9]{4}$'
    )
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='PFW 구형 실행 ID 조회 호환 이력';

INSERT INTO pfw_standard_execution_alias (
    legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by
)
SELECT legacy_execution_id, standard_execution_id, 'CPF O/S/B 10자리 표준 전환', 'FLYWAY_V32', 'FLYWAY_V32'
FROM tmp_pfw_standard_execution_id_map
ON DUPLICATE KEY UPDATE
    standard_execution_id = VALUES(standard_execution_id),
    migration_reason = VALUES(migration_reason),
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP;

DROP TEMPORARY TABLE tmp_pfw_standard_execution_id_map;
"@

$seed = @"
-- 신규 설치에서도 구형 실행 ID 조회 호환 정보를 제공하는 정본 seed입니다.
USE pfwDB;

INSERT INTO pfw_standard_execution_alias (
    legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by
) VALUES
$seedValues
ON DUPLICATE KEY UPDATE
    standard_execution_id = VALUES(standard_execution_id),
    migration_reason = VALUES(migration_reason),
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP;
"@

$utf8NoBom = [Text.UTF8Encoding]::new($false)
[IO.File]::WriteAllText(
    (Join-Path $Root "specs/sql/migration/flyway/V32__standard_execution_id_v2.sql"),
    $migration,
    $utf8NoBom)
[IO.File]::WriteAllText(
    (Join-Path $Root "specs/sql/52_standard_execution_alias_seed.sql"),
    $seed,
    $utf8NoBom)

Write-Host "표준 실행 ID migration/seed 생성 완료: $($mapping.Count)건"
