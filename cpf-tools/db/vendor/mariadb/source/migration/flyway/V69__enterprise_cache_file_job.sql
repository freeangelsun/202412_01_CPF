-- V69 CPF Enterprise Cache / Async File Job
CREATE TABLE IF NOT EXISTS cpf_cache_invalidation_event (
    event_id BIGINT NOT NULL AUTO_INCREMENT,
    event_key VARCHAR(100) NOT NULL,
    tenant_id VARCHAR(80) NOT NULL,
    namespace_cd VARCHAR(80) NOT NULL,
    cache_key VARCHAR(512) NOT NULL DEFAULT '',
    event_version BIGINT NOT NULL DEFAULT 0,
    reason VARCHAR(500) NOT NULL,
    requested_by VARCHAR(100) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (event_id),
    UNIQUE KEY uk_cpf_cache_inv_event_key (event_key),
    KEY ix_cpf_cache_inv_scope (tenant_id, namespace_cd, event_id)
);

CREATE TABLE IF NOT EXISTS cpf_cache_invalidation_checkpoint (
    consumer_id VARCHAR(120) NOT NULL,
    last_event_id BIGINT NOT NULL DEFAULT 0,
    updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (consumer_id)
);

CREATE TABLE IF NOT EXISTS adm_file_job (
    job_id VARCHAR(36) NOT NULL,
    operation_id VARCHAR(100) NOT NULL,
    request_hash VARCHAR(64) NOT NULL,
    job_type VARCHAR(20) NOT NULL,
    template_code VARCHAR(100) NOT NULL,
    template_version INT NOT NULL,
    file_format VARCHAR(10) NOT NULL,
    job_state VARCHAR(30) NOT NULL,
    dry_run CHAR(1) NOT NULL,
    rollback_supported CHAR(1) NOT NULL,
    source_path VARCHAR(1000),
    result_path VARCHAR(1000),
    source_sha256 VARCHAR(64),
    result_sha256 VARCHAR(64),
    total_rows BIGINT NOT NULL DEFAULT 0,
    success_rows BIGINT NOT NULL DEFAULT 0,
    failed_rows BIGINT NOT NULL DEFAULT 0,
    lease_owner VARCHAR(100),
    fencing_token BIGINT NOT NULL DEFAULT 0,
    lease_until TIMESTAMP(6) NULL,
    retention_until TIMESTAMP(6) NOT NULL,
    requested_by VARCHAR(100) NOT NULL,
    reason VARCHAR(500) NOT NULL,
    client_ip VARCHAR(64),
    error_code VARCHAR(80),
    error_message VARCHAR(1000),
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (job_id),
    UNIQUE KEY uk_adm_file_job_operation (operation_id),
    KEY ix_adm_file_job_claim (job_state, lease_until, created_at),
    KEY ix_adm_file_job_retention (retention_until, job_state)
);

CREATE TABLE IF NOT EXISTS adm_file_job_row (
    job_id VARCHAR(36) NOT NULL,
    row_no BIGINT NOT NULL,
    row_state VARCHAR(30) NOT NULL,
    business_key VARCHAR(200),
    payload_json LONGTEXT NOT NULL,
    error_code VARCHAR(80),
    error_message VARCHAR(1000),
    rollback_token VARCHAR(1000),
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (job_id,row_no),
    CONSTRAINT fk_adm_file_job_row_job FOREIGN KEY (job_id) REFERENCES adm_file_job(job_id)
);

INSERT INTO adm_menu (MENU_ID,PARENT_MENU_ID,MENU_NAME,MENU_PATH,SORT_ORDER,USE_YN,created_by,updated_by)
VALUES ('FILE_JOB',NULL,'대량파일 Job','/adm#file-jobs',61,'Y','SYSTEM','SYSTEM')
ON DUPLICATE KEY UPDATE MENU_NAME=VALUES(MENU_NAME),MENU_PATH=VALUES(MENU_PATH),USE_YN='Y',updated_by='SYSTEM',updated_at=CURRENT_TIMESTAMP;

INSERT INTO adm_button (BUTTON_ID,MENU_ID,ACTION_CODE,BUTTON_NAME,HTTP_METHOD,API_PATTERN,SORT_ORDER,USE_YN,created_by,updated_by) VALUES
 ('FILE_JOB_READ','FILE_JOB','READ','File Job 조회','GET','/adm/api/file-jobs/**',10,'Y','SYSTEM','SYSTEM'),
 ('FILE_JOB_UPLOAD','FILE_JOB','UPLOAD','Upload 접수','POST','/adm/api/file-jobs/uploads',20,'Y','SYSTEM','SYSTEM'),
 ('FILE_JOB_APPLY','FILE_JOB','APPLY','검증 Job 적용','POST','/adm/api/file-jobs/*/apply',30,'Y','SYSTEM','SYSTEM'),
 ('FILE_JOB_RETRY','FILE_JOB','RETRY','File Job 재시도','POST','/adm/api/file-jobs/*/retry',40,'Y','SYSTEM','SYSTEM'),
 ('FILE_JOB_CANCEL','FILE_JOB','CANCEL','File Job 취소','POST','/adm/api/file-jobs/*/cancel',50,'Y','SYSTEM','SYSTEM'),
 ('FILE_JOB_ROLLBACK','FILE_JOB','ROLLBACK','File Job Rollback','POST','/adm/api/file-jobs/*/rollback',60,'Y','SYSTEM','SYSTEM'),
 ('FILE_JOB_DOWNLOAD','FILE_JOB','DOWNLOAD','Artifact 다운로드','GET','/adm/api/file-jobs/*/artifact',70,'Y','SYSTEM','SYSTEM'),
 ('FILE_JOB_RESOLVE','FILE_JOB','RESOLVE','결과 불명 확정','POST','/adm/api/file-jobs/*/resolve-unknown',80,'Y','SYSTEM','SYSTEM'),
 ('CACHE_EVICT_KEY','CACHE','EVICT_KEY','단일 Cache 제거','POST','/adm/api/cache/evict-key',30,'Y','SYSTEM','SYSTEM'),
 ('CACHE_EVICT_NAMESPACE','CACHE','EVICT_NAMESPACE','Namespace Cache 제거','POST','/adm/api/cache/evict-namespace',40,'Y','SYSTEM','SYSTEM'),
 ('CACHE_RECONCILE','CACHE','RECONCILE','Cache Durable 재조정','POST','/adm/api/cache/reconcile',50,'Y','SYSTEM','SYSTEM')
ON DUPLICATE KEY UPDATE MENU_ID=VALUES(MENU_ID),ACTION_CODE=VALUES(ACTION_CODE),BUTTON_NAME=VALUES(BUTTON_NAME),
HTTP_METHOD=VALUES(HTTP_METHOD),API_PATTERN=VALUES(API_PATTERN),USE_YN='Y',updated_by='SYSTEM',updated_at=CURRENT_TIMESTAMP;

INSERT INTO adm_role_menu(ROLE_ID,MENU_ID,READ_YN,WRITE_YN,DELETE_YN,created_by,updated_by)
SELECT ROLE_ID,'FILE_JOB','Y',CASE WHEN ROLE_ID IN ('ADM_ADMIN','ADM_DEV_OPERATOR','ADM_OPERATOR','ADM_BIZ_OPERATOR') THEN 'Y' ELSE 'N' END,
       'N','SYSTEM','SYSTEM'
FROM adm_role WHERE ROLE_ID IN ('ADM_ADMIN','ADM_DEV_OPERATOR','ADM_OPERATOR','ADM_BIZ_OPERATOR','ADM_VIEWER')
ON DUPLICATE KEY UPDATE READ_YN=VALUES(READ_YN),WRITE_YN=VALUES(WRITE_YN),updated_by='SYSTEM',updated_at=CURRENT_TIMESTAMP;

INSERT INTO adm_role_button(ROLE_ID,BUTTON_ID,ALLOW_YN,created_by,updated_by)
SELECT r.ROLE_ID,b.BUTTON_ID,
 CASE
   WHEN r.ROLE_ID='ADM_ADMIN' THEN 'Y'
   WHEN r.ROLE_ID IN ('ADM_DEV_OPERATOR','ADM_OPERATOR') AND b.BUTTON_ID NOT IN ('FILE_JOB_ROLLBACK','FILE_JOB_RESOLVE','CACHE_EVICT_NAMESPACE') THEN 'Y'
   WHEN r.ROLE_ID='ADM_BIZ_OPERATOR' AND b.BUTTON_ID IN ('FILE_JOB_READ','FILE_JOB_UPLOAD','FILE_JOB_APPLY','FILE_JOB_DOWNLOAD') THEN 'Y'
   WHEN r.ROLE_ID='ADM_VIEWER' AND b.BUTTON_ID IN ('FILE_JOB_READ') THEN 'Y'
   ELSE 'N' END,'SYSTEM','SYSTEM'
FROM adm_role r JOIN adm_button b ON b.BUTTON_ID LIKE 'FILE_JOB_%' OR b.BUTTON_ID LIKE 'CACHE_EVICT_%' OR b.BUTTON_ID='CACHE_RECONCILE'
WHERE r.ROLE_ID IN ('ADM_ADMIN','ADM_DEV_OPERATOR','ADM_OPERATOR','ADM_BIZ_OPERATOR','ADM_VIEWER')
ON DUPLICATE KEY UPDATE ALLOW_YN=VALUES(ALLOW_YN),updated_by='SYSTEM',updated_at=CURRENT_TIMESTAMP;

INSERT INTO adm_api_permission(API_PERMISSION_ID,API_GROUP_CODE,HTTP_METHOD,API_PATH,API_NAME,PERMISSION_CODE,MENU_ID,BUTTON_ID,USE_YN,created_by,updated_by)
SELECT CONCAT('API_',BUTTON_ID),MENU_ID,HTTP_METHOD,API_PATTERN,BUTTON_NAME,ACTION_CODE,MENU_ID,BUTTON_ID,'Y','SYSTEM','SYSTEM'
FROM adm_button WHERE BUTTON_ID LIKE 'FILE_JOB_%' OR BUTTON_ID LIKE 'CACHE_EVICT_%' OR BUTTON_ID='CACHE_RECONCILE'
ON DUPLICATE KEY UPDATE API_PATH=VALUES(API_PATH),API_NAME=VALUES(API_NAME),PERMISSION_CODE=VALUES(PERMISSION_CODE),
BUTTON_ID=VALUES(BUTTON_ID),USE_YN='Y',updated_by='SYSTEM',updated_at=CURRENT_TIMESTAMP;

INSERT INTO adm_role_api_permission(ROLE_ID,API_PERMISSION_ID,ALLOW_YN,created_by,updated_by)
SELECT rb.ROLE_ID,ap.API_PERMISSION_ID,rb.ALLOW_YN,'SYSTEM','SYSTEM'
FROM adm_role_button rb JOIN adm_api_permission ap ON ap.BUTTON_ID=rb.BUTTON_ID
WHERE rb.BUTTON_ID LIKE 'FILE_JOB_%' OR rb.BUTTON_ID LIKE 'CACHE_EVICT_%' OR rb.BUTTON_ID='CACHE_RECONCILE'
ON DUPLICATE KEY UPDATE ALLOW_YN=VALUES(ALLOW_YN),updated_by='SYSTEM',updated_at=CURRENT_TIMESTAMP;
