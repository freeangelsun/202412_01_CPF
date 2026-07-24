-- CPF generated SQL bundle: 00_product_seed.sql
-- 목적: 제품 필수 기준정보만 idempotent 반영
-- 정본은 specs/sql의 번호별 분리 SQL입니다.
-- 분리 SQL 변경 후 pwsh -File scripts/build-all-install-sql.ps1 로 재생성합니다.
-- ============================================================================
-- specs/sql/50_framework_seed_data.sql
-- ============================================================================
-- CPF 프레임워크 초기 코드, 메시지, 응답코드, 설정 데이터입니다.
-- 대상 DB: cpfDB(core), batDB(batch runtime)

USE cpfDB;

INSERT INTO cpf_channel_registry (
    channel_code, channel_name, channel_type, trust_level, client_channel_yn, internal_channel_yn,
    authentication_required_yn, signature_required_yn, active_yn, description,
    policy_version, created_by, updated_by
) VALUES
    ('ANY', '전체 채널', 'SYSTEM', 'INTERNAL', 'N', 'Y', 'N', 'N', 'Y', '정책 와일드카드 전용 채널', 0, 'SYSTEM', 'SYSTEM'),
    ('WEB', '웹', 'CLIENT', 'EXTERNAL', 'Y', 'N', 'Y', 'N', 'Y', '웹 브라우저 채널', 0, 'SYSTEM', 'SYSTEM'),
    ('MOBILE', '모바일', 'CLIENT', 'EXTERNAL', 'Y', 'N', 'Y', 'N', 'Y', '모바일 애플리케이션 채널', 0, 'SYSTEM', 'SYSTEM'),
    ('ADM', '관리자', 'OPERATOR', 'INTERNAL', 'Y', 'Y', 'Y', 'N', 'Y', 'ADM 운영 채널', 0, 'SYSTEM', 'SYSTEM'),
    ('BATCH', '배치', 'SYSTEM', 'INTERNAL', 'N', 'Y', 'N', 'N', 'Y', '배치 실행 채널', 0, 'SYSTEM', 'SYSTEM')
ON DUPLICATE KEY UPDATE
    channel_name = VALUES(channel_name),
    channel_type = VALUES(channel_type),
    trust_level = VALUES(trust_level),
    client_channel_yn = VALUES(client_channel_yn),
    internal_channel_yn = VALUES(internal_channel_yn),
    authentication_required_yn = VALUES(authentication_required_yn),
    signature_required_yn = VALUES(signature_required_yn),
    active_yn = VALUES(active_yn),
    description = VALUES(description),
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO cpf_channel_execution_policy (
    policy_key, standard_execution_id, original_channel_code, caller_channel_code, request_type,
    allowed_yn, authentication_required_yn, signature_required_yn, max_tps,
    effective_from, effective_to, active_yn, policy_version, created_by, updated_by
) VALUES (
    'CPF.DEFAULT', '*', 'ANY', 'ANY', '*', 'Y', 'N', 'N', 0,
    NULL, NULL, 'Y', 0, 'SYSTEM', 'SYSTEM'
)
ON DUPLICATE KEY UPDATE
    standard_execution_id = VALUES(standard_execution_id),
    original_channel_code = VALUES(original_channel_code),
    caller_channel_code = VALUES(caller_channel_code),
    request_type = VALUES(request_type),
    allowed_yn = VALUES(allowed_yn),
    authentication_required_yn = VALUES(authentication_required_yn),
    signature_required_yn = VALUES(signature_required_yn),
    max_tps = VALUES(max_tps),
    active_yn = VALUES(active_yn),
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO cpf_code (parent_id, code_key, code_value, description, created_by, updated_by)
VALUES
    (NULL, 'CODE_GROUP', 'MODULE', '서비스 모듈 코드 그룹', 'SYSTEM', 'SYSTEM'),
    (NULL, 'CODE_GROUP', 'REQUEST_TYPE', '요청 유형 코드 그룹', 'SYSTEM', 'SYSTEM'),
    (NULL, 'CODE_GROUP', 'CHANNEL_CODE', '채널 코드 그룹', 'SYSTEM', 'SYSTEM'),
    (NULL, 'CODE_GROUP', 'RESULT_TYPE', '응답 결과 유형 코드 그룹', 'SYSTEM', 'SYSTEM'),
    (NULL, 'CODE_GROUP', 'MESSAGE_FORMAT_TYPE', '메시지 포맷 유형 코드 그룹', 'SYSTEM', 'SYSTEM'),
    (NULL, 'CODE_GROUP', 'LOG_LEVEL', '동적 로그 레벨 코드 그룹', 'SYSTEM', 'SYSTEM'),
    (NULL, 'CODE_GROUP', 'CACHE_NAME', '캐시 이름 코드 그룹', 'SYSTEM', 'SYSTEM'),
    (NULL, 'CODE_GROUP', 'BATCH_JOB_TYPE', '배치 Job 유형 코드 그룹', 'SYSTEM', 'SYSTEM'),
    (NULL, 'CODE_GROUP', 'YN', '여부 코드 그룹', 'SYSTEM', 'SYSTEM')
ON DUPLICATE KEY UPDATE
    description = VALUES(description),
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO cpf_code (parent_id, code_key, code_value, description, created_by, updated_by)
VALUES
    ((SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key = 'CODE_GROUP' AND code_value = 'MODULE') p), 'MODULE', 'CPF', '프레임워크 공통 엔진', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key = 'CODE_GROUP' AND code_value = 'MODULE') p), 'MODULE', 'CMN', '업무 공통 라이브러리', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key = 'CODE_GROUP' AND code_value = 'MODULE') p), 'MODULE', 'ADM', '관리자 운영 서비스', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key = 'CODE_GROUP' AND code_value = 'MODULE') p), 'MODULE', 'BZA', '업무 백오피스 서비스', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key = 'CODE_GROUP' AND code_value = 'MODULE') p), 'MODULE', 'BAT', '선택 배치 실행 서비스', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key = 'CODE_GROUP' AND code_value = 'MODULE') p), 'MODULE', 'MBR', '회원 샘플 서비스', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key = 'CODE_GROUP' AND code_value = 'MODULE') p), 'MODULE', 'REF', '교육 샘플 서비스', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key = 'CODE_GROUP' AND code_value = 'REQUEST_TYPE') p), 'REQUEST_TYPE', 'NORMAL', '일반 요청', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key = 'CODE_GROUP' AND code_value = 'REQUEST_TYPE') p), 'REQUEST_TYPE', 'COMPENSATION', '보상 요청', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key = 'CODE_GROUP' AND code_value = 'REQUEST_TYPE') p), 'REQUEST_TYPE', 'RETRY', '재시도 요청', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key = 'CODE_GROUP' AND code_value = 'CHANNEL_CODE') p), 'CHANNEL_CODE', 'WEB', '웹 채널', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key = 'CODE_GROUP' AND code_value = 'CHANNEL_CODE') p), 'CHANNEL_CODE', 'MOBILE', '모바일 채널', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key = 'CODE_GROUP' AND code_value = 'CHANNEL_CODE') p), 'CHANNEL_CODE', 'BATCH', '배치 채널', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key = 'CODE_GROUP' AND code_value = 'CHANNEL_CODE') p), 'CHANNEL_CODE', 'ADM', '관리자 채널', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key = 'CODE_GROUP' AND code_value = 'RESULT_TYPE') p), 'RESULT_TYPE', 'S', '성공', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key = 'CODE_GROUP' AND code_value = 'RESULT_TYPE') p), 'RESULT_TYPE', 'E', '오류', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key = 'CODE_GROUP' AND code_value = 'MESSAGE_FORMAT_TYPE') p), 'MESSAGE_FORMAT_TYPE', 'FIXED', '고정 메시지', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key = 'CODE_GROUP' AND code_value = 'MESSAGE_FORMAT_TYPE') p), 'MESSAGE_FORMAT_TYPE', 'INDEXED', '인덱스 파라미터 메시지', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key = 'CODE_GROUP' AND code_value = 'LOG_LEVEL') p), 'LOG_LEVEL', 'TRACE', 'TRACE 로그', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key = 'CODE_GROUP' AND code_value = 'LOG_LEVEL') p), 'LOG_LEVEL', 'DEBUG', 'DEBUG 로그', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key = 'CODE_GROUP' AND code_value = 'LOG_LEVEL') p), 'LOG_LEVEL', 'INFO', 'INFO 로그', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key = 'CODE_GROUP' AND code_value = 'LOG_LEVEL') p), 'LOG_LEVEL', 'WARN', 'WARN 로그', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key = 'CODE_GROUP' AND code_value = 'LOG_LEVEL') p), 'LOG_LEVEL', 'ERROR', 'ERROR 로그', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key = 'CODE_GROUP' AND code_value = 'CACHE_NAME') p), 'CACHE_NAME', 'ALL', '전체 캐시', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key = 'CODE_GROUP' AND code_value = 'CACHE_NAME') p), 'CACHE_NAME', 'CODE', '코드 캐시', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key = 'CODE_GROUP' AND code_value = 'CACHE_NAME') p), 'CACHE_NAME', 'MESSAGE', '메시지 캐시', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key = 'CODE_GROUP' AND code_value = 'CACHE_NAME') p), 'CACHE_NAME', 'RESPONSE_CODE', '응답코드 캐시', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key = 'CODE_GROUP' AND code_value = 'CACHE_NAME') p), 'CACHE_NAME', 'CONFIG', '설정 캐시', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key = 'CODE_GROUP' AND code_value = 'BATCH_JOB_TYPE') p), 'BATCH_JOB_TYPE', 'TASKLET', 'Tasklet 배치', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key = 'CODE_GROUP' AND code_value = 'BATCH_JOB_TYPE') p), 'BATCH_JOB_TYPE', 'CHUNK', 'Chunk 배치', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key = 'CODE_GROUP' AND code_value = 'BATCH_JOB_TYPE') p), 'BATCH_JOB_TYPE', 'RETRY', '재처리 배치', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key = 'CODE_GROUP' AND code_value = 'YN') p), 'YN', 'Y', '예', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key = 'CODE_GROUP' AND code_value = 'YN') p), 'YN', 'N', '아니오', 'SYSTEM', 'SYSTEM')
ON DUPLICATE KEY UPDATE
    parent_id = VALUES(parent_id),
    description = VALUES(description),
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO cpf_message (
    message_code, locale, message_format_type, external_message, internal_message,
    parameter_count, parameter_sample, description, created_by, updated_by
) VALUES
    ('MCPF000000', 'ko', 'FIXED', '정상 처리되었습니다.', 'CPF 공통 요청이 정상 처리되었습니다.', 0, NULL, 'CPF 공통 성공 메시지', 'SYSTEM', 'SYSTEM'),
    ('MCPF010001', 'ko', 'INDEXED', '요청 값이 올바르지 않습니다.', '요청 파라미터 검증에 실패했습니다. field={0}, value={1}', 2, '["memberId","abc"]', 'CPF 파라미터 오류 메시지', 'SYSTEM', 'SYSTEM'),
    ('MCPF010002', 'ko', 'INDEXED', '요청한 정보를 찾을 수 없습니다.', '조회 대상 데이터가 존재하지 않습니다. target={0}', 1, '["member"]', 'CPF 미존재 메시지', 'SYSTEM', 'SYSTEM'),
    ('MCPF010003', 'ko', 'INDEXED', '이미 등록된 정보입니다.', '중복 데이터가 감지되었습니다. key={0}', 1, '["memberNo"]', 'CPF 중복 메시지', 'SYSTEM', 'SYSTEM'),
    ('MCPF010004', 'ko', 'INDEXED', '입력값을 확인해 주세요.', 'Bean Validation 검증에 실패했습니다. field={0}', 1, '["name"]', 'CPF 검증 메시지', 'SYSTEM', 'SYSTEM'),
    ('MCPF010005', 'ko', 'FIXED', '인증이 필요합니다.', '인증되지 않은 요청입니다.', 0, NULL, 'CPF 인증 필요 메시지', 'SYSTEM', 'SYSTEM'),
    ('MCPF010006', 'ko', 'INDEXED', '처리 권한이 없습니다.', '인가되지 않은 요청입니다. user={0}', 1, '["guest"]', 'CPF 권한 오류 메시지', 'SYSTEM', 'SYSTEM'),
    ('MCPF020001', 'ko', 'INDEXED', '요청을 처리할 수 없습니다.', '업무 규칙 위반이 발생했습니다. rule={0}', 1, '["business-rule"]', 'CPF 업무 규칙 메시지', 'SYSTEM', 'SYSTEM'),
    ('MCPF030001', 'ko', 'INDEXED', '일시적으로 처리할 수 없습니다.', '외부 또는 타 주제영역 연계 오류가 발생했습니다. service={0}', 1, '["mbr"]', 'CPF 외부 연계 메시지', 'SYSTEM', 'SYSTEM'),
    ('MCPF900001', 'ko', 'INDEXED', '필수 거래 헤더가 누락되었습니다.', 'CPF 거래 헤더 검증에 실패했습니다. header={0}, uri={1}', 2, '["X-Request-Type","/mbr/list"]', 'CPF 헤더 검증 메시지', 'SYSTEM', 'SYSTEM'),
    ('MCPF900002', 'ko', 'INDEXED', '거래 메타데이터 설정이 올바르지 않습니다.', 'CPF @CpfTransaction 메타데이터 검증에 실패했습니다. transactionId={0}', 1, '["MBR01BSE0001"]', 'CPF 메타데이터 메시지', 'SYSTEM', 'SYSTEM'),
    ('MCPF900003', 'ko', 'INDEXED', '서비스 접속 정보가 없습니다.', 'CPF 서비스 endpoint 설정을 찾을 수 없습니다. serviceId={0}', 1, '["mbr"]', 'CPF endpoint 메시지', 'SYSTEM', 'SYSTEM'),
    ('MCPF900004', 'ko', 'INDEXED', '동적 로그레벨 요청이 올바르지 않습니다.', 'CPF 동적 로그레벨 규칙 검증에 실패했습니다. reason={0}', 1, '["transactionId or businessTransactionId required"]', 'CPF 동적 로그 메시지', 'SYSTEM', 'SYSTEM'),
    ('MCPF900005', 'ko', 'INDEXED', '내부 공유 API에 접근할 수 없습니다.', 'CPF 내부 서비스 신원 또는 호출 경로 검증에 실패했습니다. reason={0}', 1, '["service identity verification failed"]', 'CPF 내부 공유 API 접근 거부 메시지', 'SYSTEM', 'SYSTEM'),
    ('MCPF990000', 'ko', 'INDEXED', '처리 중 오류가 발생했습니다.', 'CPF 내부 오류가 발생했습니다. error={0}', 1, '["Exception"]', 'CPF 내부 오류 메시지', 'SYSTEM', 'SYSTEM'),
    ('MCPF990001', 'ko', 'INDEXED', '데이터베이스 오류가 발생했습니다.', '데이터베이스 처리 오류가 발생했습니다. sqlState={0}', 1, '["HY000"]', 'CPF 데이터베이스 오류 메시지', 'SYSTEM', 'SYSTEM'),
    ('MBZA000000', 'ko', 'FIXED', '성공', 'BZA 요청이 정상 처리되었습니다.', 0, NULL, 'BZA 성공 메시지', 'SYSTEM', 'SYSTEM'),
    ('MBZA010001', 'ko', 'INDEXED', '업무 요청 값이 올바르지 않습니다.', 'BZA 입력값 검증에 실패했습니다. field={0}', 1, '["field"]', 'BZA 입력값 오류 메시지', 'SYSTEM', 'SYSTEM'),
    ('MBZA010002', 'ko', 'FIXED', '처리 권한이 없습니다.', 'BZA 서버 권한 검사에 실패했습니다.', 0, NULL, 'BZA 권한 오류 메시지', 'SYSTEM', 'SYSTEM'),
    ('MMBR000000', 'ko', 'FIXED', '성공', 'MBR 요청이 정상 처리되었습니다.', 0, NULL, 'MBR 성공 메시지', 'SYSTEM', 'SYSTEM'),
    ('MMBR010001', 'ko', 'FIXED', '회원이 생성되었습니다.', 'MBR 회원 데이터가 생성되었습니다.', 0, NULL, 'MBR 생성 메시지', 'SYSTEM', 'SYSTEM'),
    ('MMBR010002', 'ko', 'FIXED', '회원이 수정되었습니다.', 'MBR 회원 데이터가 수정되었습니다.', 0, NULL, 'MBR 수정 메시지', 'SYSTEM', 'SYSTEM'),
    ('MMBR010003', 'ko', 'FIXED', '회원이 삭제되었습니다.', 'MBR 회원 데이터가 삭제되었습니다.', 0, NULL, 'MBR 삭제 메시지', 'SYSTEM', 'SYSTEM'),
    ('MMBR010101', 'ko', 'FIXED', '회원 요청 형식이 올바르지 않습니다.', 'MBR 요청 형식이 올바르지 않습니다.', 0, NULL, 'MBR bad request 메시지', 'SYSTEM', 'SYSTEM'),
    ('MMBR010102', 'ko', 'INDEXED', '유효하지 않은 회원 파라미터입니다.', 'MBR 파라미터 검증에 실패했습니다. field={0}', 1, '["memberId"]', 'MBR 파라미터 오류 메시지', 'SYSTEM', 'SYSTEM'),
    ('MMBR010103', 'ko', 'INDEXED', '회원 정보를 찾을 수 없습니다.', 'MBR 조회 대상이 없습니다. target={0}', 1, '["member"]', 'MBR 미존재 메시지', 'SYSTEM', 'SYSTEM'),
    ('MMBR010104', 'ko', 'INDEXED', '중복된 회원 데이터가 있습니다.', 'MBR 중복 데이터가 감지되었습니다. key={0}', 1, '["memberNo"]', 'MBR 중복 메시지', 'SYSTEM', 'SYSTEM'),
    ('MMBR010105', 'ko', 'INDEXED', '회원 입력값 검증에 실패했습니다.', 'MBR 입력값 검증에 실패했습니다. field={0}', 1, '["name"]', 'MBR 검증 메시지', 'SYSTEM', 'SYSTEM'),
    ('MMBR990000', 'ko', 'INDEXED', '회원 처리 중 오류가 발생했습니다.', 'MBR 내부 서버 오류가 발생했습니다. error={0}', 1, '["Exception"]', 'MBR 내부 오류 메시지', 'SYSTEM', 'SYSTEM'),
    ('MREF090001', 'ko', 'INDEXED', '이미 등록된 {0}입니다.', '{0}={1} 값이 이미 존재합니다. duplicateCheck=REF_EDU_SAMPLE', 2, '["회원번호","M0001"]', 'REF 동적 중복 교육 메시지', 'SYSTEM', 'SYSTEM'),
    ('MCMN000001', 'ko', 'FIXED', 'CPF 교육 시스템에 오신 것을 환영합니다.', 'CMN education welcome message.', 0, NULL, 'CMN 교육 환영 메시지', 'SYSTEM', 'SYSTEM'),
    ('MCMN000001', 'en', 'FIXED', 'Welcome to the CPF education system.', 'CMN education welcome message.', 0, NULL, 'CMN 교육 환영 메시지', 'SYSTEM', 'SYSTEM')
ON DUPLICATE KEY UPDATE
    message_format_type = VALUES(message_format_type),
    external_message = VALUES(external_message),
    internal_message = VALUES(internal_message),
    parameter_count = VALUES(parameter_count),
    parameter_sample = VALUES(parameter_sample),
    description = VALUES(description),
    use_yn = 'Y',
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO cpf_response_code (
    response_code, message_code, result_type, module_id, response_group, sequence_no,
    http_status, description, created_by, updated_by
) VALUES
    ('SCPF000000', 'MCPF000000', 'S', 'CPF', '00', '0000', 200, 'CPF 공통 성공', 'SYSTEM', 'SYSTEM'),
    ('ECPF010001', 'MCPF010001', 'E', 'CPF', '01', '0001', 400, '파라미터 오류', 'SYSTEM', 'SYSTEM'),
    ('ECPF010002', 'MCPF010002', 'E', 'CPF', '01', '0002', 404, '미존재 오류', 'SYSTEM', 'SYSTEM'),
    ('ECPF010003', 'MCPF010003', 'E', 'CPF', '01', '0003', 409, '중복 오류', 'SYSTEM', 'SYSTEM'),
    ('ECPF010004', 'MCPF010004', 'E', 'CPF', '01', '0004', 400, '검증 실패', 'SYSTEM', 'SYSTEM'),
    ('ECPF010005', 'MCPF010005', 'E', 'CPF', '01', '0005', 401, '인증 필요', 'SYSTEM', 'SYSTEM'),
    ('ECPF010006', 'MCPF010006', 'E', 'CPF', '01', '0006', 403, '권한 없음', 'SYSTEM', 'SYSTEM'),
    ('ECPF020001', 'MCPF020001', 'E', 'CPF', '02', '0001', 400, '업무 규칙 위반', 'SYSTEM', 'SYSTEM'),
    ('ECPF030001', 'MCPF030001', 'E', 'CPF', '03', '0001', 502, '외부 연계 오류', 'SYSTEM', 'SYSTEM'),
    ('ECPF900001', 'MCPF900001', 'E', 'CPF', '90', '0001', 400, '필수 거래 헤더 누락', 'SYSTEM', 'SYSTEM'),
    ('ECPF900002', 'MCPF900002', 'E', 'CPF', '90', '0002', 500, '거래 메타데이터 오류', 'SYSTEM', 'SYSTEM'),
    ('ECPF900003', 'MCPF900003', 'E', 'CPF', '90', '0003', 500, '서비스 endpoint 미등록', 'SYSTEM', 'SYSTEM'),
    ('ECPF900004', 'MCPF900004', 'E', 'CPF', '90', '0004', 400, '동적 로그 규칙 오류', 'SYSTEM', 'SYSTEM'),
    ('ECPF900005', 'MCPF900005', 'E', 'CPF', '90', '0005', 403, '내부 공유 API 접근 거부', 'SYSTEM', 'SYSTEM'),
    ('ECPF990000', 'MCPF990000', 'E', 'CPF', '99', '0000', 500, '내부 서버 오류', 'SYSTEM', 'SYSTEM'),
    ('ECPF990001', 'MCPF990001', 'E', 'CPF', '99', '0001', 500, '데이터베이스 오류', 'SYSTEM', 'SYSTEM'),
    ('SBZA000000', 'MBZA000000', 'S', 'BZA', '00', '0000', 200, 'BZA 성공', 'SYSTEM', 'SYSTEM'),
    ('EBZA010001', 'MBZA010001', 'E', 'BZA', '01', '0001', 400, 'BZA 입력값 오류', 'SYSTEM', 'SYSTEM'),
    ('EBZA010002', 'MBZA010002', 'E', 'BZA', '01', '0002', 403, 'BZA 권한 오류', 'SYSTEM', 'SYSTEM'),
    ('SMBR000000', 'MMBR000000', 'S', 'MBR', '00', '0000', 200, 'MBR 성공', 'SYSTEM', 'SYSTEM'),
    ('SMBR010001', 'MMBR010001', 'S', 'MBR', '01', '0001', 200, 'MBR 생성 성공', 'SYSTEM', 'SYSTEM'),
    ('SMBR010002', 'MMBR010002', 'S', 'MBR', '01', '0002', 200, 'MBR 수정 성공', 'SYSTEM', 'SYSTEM'),
    ('SMBR010003', 'MMBR010003', 'S', 'MBR', '01', '0003', 200, 'MBR 삭제 성공', 'SYSTEM', 'SYSTEM'),
    ('EMBR010001', 'MMBR010101', 'E', 'MBR', '01', '0001', 400, 'MBR 요청 형식 오류', 'SYSTEM', 'SYSTEM'),
    ('EMBR010002', 'MMBR010102', 'E', 'MBR', '01', '0002', 400, 'MBR 파라미터 오류', 'SYSTEM', 'SYSTEM'),
    ('EMBR010003', 'MMBR010103', 'E', 'MBR', '01', '0003', 404, 'MBR 미존재', 'SYSTEM', 'SYSTEM'),
    ('EMBR010004', 'MMBR010104', 'E', 'MBR', '01', '0004', 409, 'MBR 중복', 'SYSTEM', 'SYSTEM'),
    ('EMBR010005', 'MMBR010105', 'E', 'MBR', '01', '0005', 400, 'MBR 검증 실패', 'SYSTEM', 'SYSTEM'),
    ('EMBR990000', 'MMBR990000', 'E', 'MBR', '99', '0000', 500, 'MBR 내부 오류', 'SYSTEM', 'SYSTEM')
ON DUPLICATE KEY UPDATE
    message_code = VALUES(message_code),
    result_type = VALUES(result_type),
    module_id = VALUES(module_id),
    response_group = VALUES(response_group),
    sequence_no = VALUES(sequence_no),
    http_status = VALUES(http_status),
    description = VALUES(description),
    use_yn = 'Y',
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO cpf_config (config_key, config_value, config_type, description, encrypted_yn, created_by, updated_by)
VALUES
    ('CPF.CMN.CACHE.PRELOAD_ENABLED', 'Y', 'BOOLEAN', 'CMN 캐시 기동 시 선적재 여부', 'N', 'SYSTEM', 'SYSTEM'),
    ('CPF.CMN.CACHE.FAIL_FAST_ON_STARTUP', 'N', 'BOOLEAN', '캐시 선적재 실패 시 기동 실패 여부', 'N', 'SYSTEM', 'SYSTEM'),
    ('CPF.CMN.CACHE.REFRESH_POLL_MILLIS', '5000', 'NUMBER', '캐시 갱신 이벤트 polling 주기', 'N', 'SYSTEM', 'SYSTEM'),
    ('CPF.CMN.MESSAGING.BROKER', 'IN_MEMORY', 'STRING', '기본 CMN 메시지 브로커 유형', 'N', 'SYSTEM', 'SYSTEM'),
    ('CPF.HTTP.CONNECT_TIMEOUT_MS', '3000', 'NUMBER', 'CPF HTTP client 연결 timeout', 'N', 'SYSTEM', 'SYSTEM'),
    ('CPF.HTTP.READ_TIMEOUT_MS', '5000', 'NUMBER', 'CPF HTTP client 읽기 timeout', 'N', 'SYSTEM', 'SYSTEM'),
    ('CPF.ADM.SESSION_TTL_SECONDS', '3600', 'NUMBER', 'ADM 세션 TTL 초', 'N', 'SYSTEM', 'SYSTEM'),
    ('CPF.ADM.PASSWORD_EXPIRE_DAYS', '90', 'NUMBER', 'ADM 비밀번호 만료 일수', 'N', 'SYSTEM', 'SYSTEM'),
    ('CPF.ADM.PASSWORD_MIN_LENGTH', '10', 'NUMBER', 'ADM 비밀번호 최소 길이', 'N', 'SYSTEM', 'SYSTEM'),
    ('CPF.ADM.PASSWORD_MAX_FAIL_COUNT', '5', 'NUMBER', 'ADM 로그인 실패 잠금 기준', 'N', 'SYSTEM', 'SYSTEM'),
    ('CPF.BATCH.DEFAULT_LOCK_SECONDS', '3600', 'NUMBER', '배치 기본 lock 만료 초', 'N', 'SYSTEM', 'SYSTEM'),
    ('CPF.FEATURE.SAMPLE_ENABLED', 'Y', 'BOOLEAN', '샘플 API와 교육 flow 활성화 여부', 'N', 'SYSTEM', 'SYSTEM')
ON DUPLICATE KEY UPDATE
    config_value = VALUES(config_value),
    config_type = VALUES(config_type),
    description = VALUES(description),
    encrypted_yn = VALUES(encrypted_yn),
    use_yn = 'Y',
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO cpf_log_policy (
    policy_key, policy_name, target_type, target_id, log_level,
    db_log_enabled_yn, file_log_enabled_yn, request_body_log_yn, response_body_log_yn,
    error_stack_log_yn, retention_days, sampling_rate, priority, active_yn,
    description, created_by, updated_by
) VALUES
    ('ONLINE_DEFAULT', '온라인 거래 기본 로그 정책', 'ONLINE_TRANSACTION', '*', 'INFO', 'Y', 'Y', 'N', 'N', 'Y', 90, 100.00, 100, 'Y', '온라인 Controller/API 기본 로그 정책', 'SYSTEM', 'SYSTEM'),
    ('BATCH_DEFAULT', '배치 기본 로그 정책', 'BATCH_JOB', '*', 'INFO', 'Y', 'Y', 'N', 'N', 'Y', 180, 100.00, 100, 'Y', 'Spring Batch Job 기본 로그 정책', 'SYSTEM', 'SYSTEM'),
    ('ADM_OPERATION_DEFAULT', 'ADM 운영 기본 로그 정책', 'MODULE', 'ADM', 'INFO', 'Y', 'Y', 'N', 'N', 'Y', 365, 100.00, 50, 'Y', 'ADM 운영 API 기본 로그 정책', 'SYSTEM', 'SYSTEM')
ON DUPLICATE KEY UPDATE
    policy_name = VALUES(policy_name),
    target_type = VALUES(target_type),
    target_id = VALUES(target_id),
    log_level = VALUES(log_level),
    db_log_enabled_yn = VALUES(db_log_enabled_yn),
    file_log_enabled_yn = VALUES(file_log_enabled_yn),
    request_body_log_yn = VALUES(request_body_log_yn),
    response_body_log_yn = VALUES(response_body_log_yn),
    error_stack_log_yn = VALUES(error_stack_log_yn),
    retention_days = VALUES(retention_days),
    sampling_rate = VALUES(sampling_rate),
    priority = VALUES(priority),
    active_yn = VALUES(active_yn),
    description = VALUES(description),
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO cpf_security_jwt_key (
    KEY_ID, ISSUER, ALGORITHM, SECRET_REF, ACTIVE_YN, EXPIRE_AT, created_by, updated_by
) VALUES (
    'local-cpf-hs256-001',
    'CPF',
    'HS256',
    'ENV:CPF_CMN_SECURITY_JWT_SECRET',
    'Y',
    NULL,
    'SYSTEM',
    'SYSTEM'
)
ON DUPLICATE KEY UPDATE
    ISSUER = VALUES(ISSUER),
    ALGORITHM = VALUES(ALGORITHM),
    SECRET_REF = VALUES(SECRET_REF),
    ACTIVE_YN = VALUES(ACTIVE_YN),
    EXPIRE_AT = VALUES(EXPIRE_AT),
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO cpf_cache_refresh_event (
    cache_name, event_type, event_key, source_was_id, published_by, created_by, updated_by
)
SELECT 'ALL', 'INITIAL_LOAD', 'INITIAL_FRAMEWORK_SEED', 'SQL', 'SYSTEM', 'SYSTEM', 'SYSTEM'
WHERE NOT EXISTS (
    SELECT 1
    FROM cpf_cache_refresh_event
    WHERE cache_name = 'ALL'
      AND event_type = 'INITIAL_LOAD'
      AND event_key = 'INITIAL_FRAMEWORK_SEED'
);

INSERT INTO batDB.bat_instance (
    instance_id, instance_name, host_name, server_port, active_yn, last_heartbeat_at, description, created_by, updated_by
) VALUES (
    'local-batch-01',
    '로컬 배치 인스턴스',
    'localhost',
    8099,
    'Y',
    NOW(3),
    'REF EDU 배치와 ADM 관제 연동을 확인하는 로컬 인스턴스',
    'SYSTEM',
    'SYSTEM'
)
ON DUPLICATE KEY UPDATE
    instance_name = VALUES(instance_name),
    host_name = VALUES(host_name),
    server_port = VALUES(server_port),
    active_yn = VALUES(active_yn),
    last_heartbeat_at = VALUES(last_heartbeat_at),
    description = VALUES(description),
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO batDB.bat_worker (
    worker_id, server_instance_id, host_name, process_id, thread_name, worker_status,
    active_yn, last_heartbeat_at, current_job_id, current_execution_id, description, created_by, updated_by
) VALUES (
    'local-batch-01',
    'local-batch-01',
    'localhost',
    'seed',
    'seed-main',
    'IDLE',
    'Y',
    NOW(3),
    NULL,
    NULL,
    '로컬 smoke 검증용 배치 worker heartbeat',
    'SYSTEM',
    'SYSTEM'
)
ON DUPLICATE KEY UPDATE
    server_instance_id = VALUES(server_instance_id),
    host_name = VALUES(host_name),
    process_id = VALUES(process_id),
    thread_name = VALUES(thread_name),
    worker_status = VALUES(worker_status),
    active_yn = VALUES(active_yn),
    last_heartbeat_at = VALUES(last_heartbeat_at),
    current_job_id = VALUES(current_job_id),
    current_execution_id = VALUES(current_execution_id),
    description = VALUES(description),
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO batDB.bat_job (
    job_id, job_name, job_type, description, restartable_yn, use_yn, created_by, updated_by
) VALUES
    ('CPF_EDU_TASKLET_JOB', 'CPF 교육 Tasklet Job', 'TASKLET', '배치 관제 수동 실행 샘플을 위한 Tasklet Job입니다.', 'Y', 'Y', 'SYSTEM', 'SYSTEM'),
    ('CPF_EDU_CHUNK_JOB', 'CPF 교육 Chunk Job', 'CHUNK', '대용량 읽기/처리/쓰기 샘플을 위한 Chunk Job입니다.', 'Y', 'Y', 'SYSTEM', 'SYSTEM'),
    ('CPF_EDU_RETRY_JOB', 'CPF 교육 재처리 Job', 'RETRY', '실패 재처리와 checkpoint/restart 교육을 위한 Job입니다.', 'Y', 'Y', 'SYSTEM', 'SYSTEM')
ON DUPLICATE KEY UPDATE
    job_name = VALUES(job_name),
    job_type = VALUES(job_type),
    description = VALUES(description),
    restartable_yn = VALUES(restartable_yn),
    use_yn = VALUES(use_yn),
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO batDB.bat_schedule (
    schedule_id, job_id, cron_expression, calendar_id, business_day_only_yn,
    holiday_policy, available_start_time, available_end_time, run_date_pattern,
    timezone, enabled_yn, created_by, updated_by
) VALUES
    ('CPF_EDU_TASKLET_DAILY', 'CPF_EDU_TASKLET_JOB', '0 0 2 * * *', 'DEFAULT', 'Y', 'SKIP', '02:00:00', '04:00:00', 'D+0', 'Asia/Seoul', 'N', 'SYSTEM', 'SYSTEM'),
    ('CPF_EDU_CHUNK_DAILY', 'CPF_EDU_CHUNK_JOB', '0 30 2 * * *', 'DEFAULT', 'Y', 'SKIP', '02:30:00', '05:30:00', 'D+0', 'Asia/Seoul', 'N', 'SYSTEM', 'SYSTEM')
ON DUPLICATE KEY UPDATE
    job_id = VALUES(job_id),
    cron_expression = VALUES(cron_expression),
    calendar_id = VALUES(calendar_id),
    business_day_only_yn = VALUES(business_day_only_yn),
    holiday_policy = VALUES(holiday_policy),
    available_start_time = VALUES(available_start_time),
    available_end_time = VALUES(available_end_time),
    run_date_pattern = VALUES(run_date_pattern),
    timezone = VALUES(timezone),
    enabled_yn = VALUES(enabled_yn),
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO batDB.bat_job_relation (
    job_id, related_job_id, relation_type, trigger_condition, required_status, sort_order, use_yn, created_by, updated_by
) VALUES
    ('CPF_EDU_CHUNK_JOB', 'CPF_EDU_TASKLET_JOB', 'PREDECESSOR', 'COMPLETED', 'COMPLETED', 10, 'Y', 'SYSTEM', 'SYSTEM'),
    ('CPF_EDU_TASKLET_JOB', 'CPF_EDU_CHUNK_JOB', 'TRIGGER', 'COMPLETED', 'COMPLETED', 20, 'Y', 'SYSTEM', 'SYSTEM')
ON DUPLICATE KEY UPDATE
    trigger_condition = VALUES(trigger_condition),
    required_status = VALUES(required_status),
    sort_order = VALUES(sort_order),
    use_yn = VALUES(use_yn),
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO batDB.bat_execution (
    job_id, schedule_id, job_parameters, execution_status, batch_instance_id, server_instance_id,
    worker_id, transaction_global_id, start_time, end_time,
    read_count, write_count, skip_count, requested_by, created_by, updated_by
)
SELECT
    'CPF_EDU_TASKLET_JOB',
    'CPF_EDU_TASKLET_DAILY',
    '{"edu":true}',
    'COMPLETED',
    'local-batch-01',
    'local-batch-01',
    'local-batch-01',
    '20260615120000000REFlocal010000001',
    DATE_SUB(NOW(3), INTERVAL 10 MINUTE),
    DATE_SUB(NOW(3), INTERVAL 9 MINUTE),
    1,
    1,
    0,
    'SYSTEM',
    'SYSTEM',
    'SYSTEM'
WHERE NOT EXISTS (
    SELECT 1
    FROM batDB.bat_execution
    WHERE job_id = 'CPF_EDU_TASKLET_JOB'
      AND requested_by = 'SYSTEM'
      AND job_parameters = '{"edu":true}'
);

SET @cpf_edu_execution_id = (
    SELECT execution_id
    FROM batDB.bat_execution
    WHERE job_id = 'CPF_EDU_TASKLET_JOB'
      AND requested_by = 'SYSTEM'
      AND job_parameters = '{"edu":true}'
    ORDER BY execution_id
    LIMIT 1
);

INSERT INTO batDB.bat_step_execution (
    execution_id, spring_batch_step_execution_id, worker_id, step_name, execution_status,
    start_time, end_time, read_count, write_count, skip_count, step_log, created_by, updated_by
)
SELECT @cpf_edu_execution_id, NULL, 'local-batch-01', 'CPF_EDU_TASKLET_STEP', 'COMPLETED', DATE_SUB(NOW(3), INTERVAL 10 MINUTE), DATE_SUB(NOW(3), INTERVAL 9 MINUTE), 1, 1, 0, 'Tasklet 교육 실행 정상 완료', 'SYSTEM', 'SYSTEM'
WHERE @cpf_edu_execution_id IS NOT NULL
  AND NOT EXISTS (
      SELECT 1
      FROM batDB.bat_step_execution
      WHERE execution_id = @cpf_edu_execution_id
        AND step_name = 'CPF_EDU_TASKLET_STEP'
  );

INSERT INTO batDB.bat_execution_target (
    execution_id, job_id, schedule_id, target_instance_id, business_date, planned_run_at,
    dispatch_status, dispatch_reason, created_by, updated_by
)
SELECT
    @cpf_edu_execution_id,
    'CPF_EDU_TASKLET_JOB',
    'CPF_EDU_TASKLET_DAILY',
    'local-batch-01',
    CURRENT_DATE,
    CAST(CONCAT(CURRENT_DATE, ' 02:00:00') AS DATETIME),
    'DONE',
    '로컬 smoke 검증용 완료 대상',
    'SYSTEM',
    'SYSTEM'
WHERE @cpf_edu_execution_id IS NOT NULL
  AND NOT EXISTS (
      SELECT 1
      FROM batDB.bat_execution_target
      WHERE job_id = 'CPF_EDU_TASKLET_JOB'
        AND business_date = CURRENT_DATE
        AND target_instance_id = 'local-batch-01'
  );

INSERT INTO batDB.bat_business_day_calendar (
    calendar_id, business_date, holiday_yn, business_day_yn, description, created_by, updated_by
) VALUES
    ('DEFAULT', CURRENT_DATE, 'N', 'Y', '로컬 smoke 검증용 기본 영업일', 'SYSTEM', 'SYSTEM'),
    ('DEFAULT', DATE_ADD(CURRENT_DATE, INTERVAL 1 DAY), 'N', 'Y', '로컬 smoke 검증용 다음 영업일', 'SYSTEM', 'SYSTEM')
ON DUPLICATE KEY UPDATE
    holiday_yn = VALUES(holiday_yn),
    business_day_yn = VALUES(business_day_yn),
    description = VALUES(description),
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO cpf_notification_rule (
    event_type, event_sub_type, channel_code, template_code, severity, receiver_group, use_yn, created_by, updated_by
) VALUES
    ('BATCH_EXECUTION', 'FAILED', 'ADM', 'BATCH_FAILED_DEFAULT', 'ERROR', 'ADM_BATCH_OPERATOR', 'Y', 'SYSTEM', 'SYSTEM'),
    ('SECURITY_EVENT', 'LOGIN_FAILURE', 'ADM', 'SECURITY_LOGIN_FAILURE', 'WARN', 'ADM_SECURITY_OPERATOR', 'Y', 'SYSTEM', 'SYSTEM')
ON DUPLICATE KEY UPDATE
    template_code = VALUES(template_code),
    severity = VALUES(severity),
    receiver_group = VALUES(receiver_group),
    use_yn = VALUES(use_yn),
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO cpf_notification_delivery_log (
    rule_id, event_type, target_type, target_id, receiver, delivery_status, delivery_message, created_by, updated_by
)
SELECT
    rule_id,
    'BATCH_EXECUTION',
    'bat_execution',
    CAST(@cpf_edu_execution_id AS CHAR),
    'ADM_BATCH_OPERATOR',
    'SKIPPED',
    '로컬 seed 알림 발송 로그 샘플입니다.',
    'SYSTEM',
    'SYSTEM'
FROM cpf_notification_rule
WHERE event_type = 'BATCH_EXECUTION'
  AND event_sub_type = 'FAILED'
  AND @cpf_edu_execution_id IS NOT NULL
  AND NOT EXISTS (
      SELECT 1
      FROM cpf_notification_delivery_log
      WHERE event_type = 'BATCH_EXECUTION'
        AND target_id = CAST(@cpf_edu_execution_id AS CHAR)
        AND receiver = 'ADM_BATCH_OPERATOR'
  )
LIMIT 1;

INSERT INTO batDB.bat_job (
    job_id, job_name, job_type, description, restartable_yn, use_yn, created_by, updated_by
) VALUES (
    'CPF_BAT_CENTER_CUT_JOB',
    'CPF BAT 센터컷 smoke Job',
    'TASKLET',
    'BAT standalone에서 center-cut provider/handler 기본 흐름을 검증하는 Job입니다.',
    'Y',
    'Y',
    'SYSTEM',
    'SYSTEM'
)
ON DUPLICATE KEY UPDATE
    job_name = VALUES(job_name),
    job_type = VALUES(job_type),
    description = VALUES(description),
    restartable_yn = VALUES(restartable_yn),
    use_yn = VALUES(use_yn),
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO batDB.bat_job (
    job_id, job_name, job_type, description, restartable_yn, use_yn, created_by, updated_by
) VALUES (
    'CPF_REF_CENTER_CUT_SAMPLE_JOB',
    'CPF REF 업무 DB 센터컷 샘플 Job',
    'TASKLET',
    'REF 업무 DB adapter를 통해 center-cut target/result 흐름을 검증하는 Job입니다.',
    'Y',
    'Y',
    'SYSTEM',
    'SYSTEM'
)
ON DUPLICATE KEY UPDATE
    job_name = VALUES(job_name),
    job_type = VALUES(job_type),
    description = VALUES(description),
    restartable_yn = VALUES(restartable_yn),
    use_yn = VALUES(use_yn),
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO batDB.bat_center_cut_job (
    center_cut_job_id, batch_job_id, center_cut_job_name, provider_key, handler_key,
    chunk_size, retry_limit, use_yn, description, created_by, updated_by
) VALUES (
    'CPF_BAT_CENTER_CUT_JOB',
    'CPF_BAT_CENTER_CUT_JOB',
    'CPF BAT 센터컷 smoke Job',
    'batCenterCutSampleTargetProvider',
    'batCenterCutSampleHandler',
    10,
    3,
    'Y',
    'CPF 표준 center-cut 계약과 BAT 기본 구현체를 검증하는 1차 모수입니다.',
    'SYSTEM',
    'SYSTEM'
)
ON DUPLICATE KEY UPDATE
    batch_job_id = VALUES(batch_job_id),
    center_cut_job_name = VALUES(center_cut_job_name),
    provider_key = VALUES(provider_key),
    handler_key = VALUES(handler_key),
    chunk_size = VALUES(chunk_size),
    retry_limit = VALUES(retry_limit),
    use_yn = VALUES(use_yn),
    description = VALUES(description),
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO batDB.bat_center_cut_job (
    center_cut_job_id, batch_job_id, center_cut_job_name, provider_key, handler_key,
    chunk_size, retry_limit, use_yn, description, created_by, updated_by
) VALUES (
    'CPF_REF_CENTER_CUT_SAMPLE_JOB',
    'CPF_REF_CENTER_CUT_SAMPLE_JOB',
    'CPF REF 업무 DB 센터컷 샘플 Job',
    'refCenterCutTargetProvider',
    'refCenterCutHandler',
    10,
    3,
    'Y',
    'CPF 표준 계약과 REF 업무 DB adapter를 연결하는 center-cut 샘플 모수입니다.',
    'SYSTEM',
    'SYSTEM'
)
ON DUPLICATE KEY UPDATE
    batch_job_id = VALUES(batch_job_id),
    center_cut_job_name = VALUES(center_cut_job_name),
    provider_key = VALUES(provider_key),
    handler_key = VALUES(handler_key),
    chunk_size = VALUES(chunk_size),
    retry_limit = VALUES(retry_limit),
    use_yn = VALUES(use_yn),
    description = VALUES(description),
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO batDB.bat_center_cut_parameter (
    center_cut_job_id, parameter_key, parameter_value, encrypted_yn, use_yn, created_by, updated_by
) VALUES
    ('CPF_BAT_CENTER_CUT_JOB', 'businessDatePattern', 'D+0', 'N', 'Y', 'SYSTEM', 'SYSTEM'),
    ('CPF_BAT_CENTER_CUT_JOB', 'defaultLimit', '10', 'N', 'Y', 'SYSTEM', 'SYSTEM')
ON DUPLICATE KEY UPDATE
    parameter_value = VALUES(parameter_value),
    encrypted_yn = VALUES(encrypted_yn),
    use_yn = VALUES(use_yn),
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO cpf_service (
    service_id, service_name, service_type, owner_module_code, description, use_yn, created_by, updated_by
) VALUES
    ('BZA', '업무 백오피스 서비스', 'INTERNAL', 'BZA', 'CPF 업무 운영 백오피스 서비스 호출 대상', 'Y', 'SYSTEM', 'SYSTEM'),
    ('MBR', '회원 서비스', 'INTERNAL', 'MBR', 'CPF 회원 업무 모듈 서비스 호출 대상', 'Y', 'SYSTEM', 'SYSTEM'),
    ('REF', '온라인 교육 서비스', 'INTERNAL', 'REF', 'CPF 온라인 교육 및 검증 서비스 호출 대상', 'Y', 'SYSTEM', 'SYSTEM'),
    ('BAT', '배치 Worker 서비스', 'INTERNAL', 'BAT', 'CPF 배치 Worker 서비스 호출 대상', 'Y', 'SYSTEM', 'SYSTEM'),
    ('ACC', '계정 Reference 서비스', 'INTERNAL', 'ACC', '생성기 검증과 MBR 연계에 사용하는 계정 reference 서비스', 'Y', 'SYSTEM', 'SYSTEM'),
    ('ADM', '운영 콘솔 서비스', 'INTERNAL', 'ADM', 'CPF 운영 콘솔 서비스 호출 대상', 'Y', 'SYSTEM', 'SYSTEM')
ON DUPLICATE KEY UPDATE
    service_name = VALUES(service_name),
    service_type = VALUES(service_type),
    owner_module_code = VALUES(owner_module_code),
    description = VALUES(description),
    use_yn = VALUES(use_yn),
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO cpf_service_endpoint (
    endpoint_code, service_id, endpoint_name, endpoint_type, base_url, context_path,
    default_timeout_ms, default_retry_count, use_yn, created_by, updated_by
) VALUES
    ('BZA_API', 'BZA', 'BZA API Endpoint', 'HTTP', 'http://localhost:8091', '/api/bza', 3000, 0, 'Y', 'SYSTEM', 'SYSTEM'),
    ('MBR_API', 'MBR', 'MBR API Endpoint', 'HTTP', 'http://localhost:8081', '/mbr', 3000, 0, 'Y', 'SYSTEM', 'SYSTEM'),
    ('REF_API', 'REF', 'REF API Endpoint', 'HTTP', 'http://localhost:8099', '/ref', 3000, 0, 'Y', 'SYSTEM', 'SYSTEM'),
    ('BAT_API', 'BAT', 'BAT API Endpoint', 'HTTP', 'http://localhost:8093', '/bat', 5000, 0, 'Y', 'SYSTEM', 'SYSTEM'),
    ('ACC_API', 'ACC', 'ACC API Endpoint', 'HTTP', 'http://localhost:8082', '/internal/api/v1/accounts', 3000, 1, 'Y', 'SYSTEM', 'SYSTEM'),
    ('ADM_API', 'ADM', 'ADM API Endpoint', 'HTTP', 'http://localhost:8090', '/adm', 3000, 0, 'Y', 'SYSTEM', 'SYSTEM')
ON DUPLICATE KEY UPDATE
    service_id = VALUES(service_id),
    endpoint_name = VALUES(endpoint_name),
    endpoint_type = VALUES(endpoint_type),
    base_url = VALUES(base_url),
    context_path = VALUES(context_path),
    default_timeout_ms = VALUES(default_timeout_ms),
    default_retry_count = VALUES(default_retry_count),
    use_yn = VALUES(use_yn),
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO cpf_service_instance (
    instance_id, service_id, endpoint_code, instance_name, base_url, host_name,
    port_no, instance_status, weight, active_yn, last_heartbeat_at, created_by, updated_by
) VALUES
    ('BZA-local-01', 'BZA', 'BZA_API', 'BZA local instance', 'http://localhost:8091', 'localhost', 8091, 'UP', 100, 'Y', CURRENT_TIMESTAMP(3), 'SYSTEM', 'SYSTEM'),
    ('MBR-local-01', 'MBR', 'MBR_API', 'MBR local instance', 'http://localhost:8081', 'localhost', 8081, 'UP', 100, 'Y', CURRENT_TIMESTAMP(3), 'SYSTEM', 'SYSTEM'),
    ('REF-local-01', 'REF', 'REF_API', 'REF local instance', 'http://localhost:8099', 'localhost', 8099, 'UP', 100, 'Y', CURRENT_TIMESTAMP(3), 'SYSTEM', 'SYSTEM'),
    ('BAT-local-01', 'BAT', 'BAT_API', 'BAT local instance', 'http://localhost:8093', 'localhost', 8093, 'UP', 100, 'Y', CURRENT_TIMESTAMP(3), 'SYSTEM', 'SYSTEM'),
    ('ACC-local-01', 'ACC', 'ACC_API', 'ACC local instance', 'http://localhost:8082', 'localhost', 8082, 'UP', 100, 'Y', CURRENT_TIMESTAMP(3), 'SYSTEM', 'SYSTEM'),
    ('ADM-local-01', 'ADM', 'ADM_API', 'ADM local instance', 'http://localhost:8090', 'localhost', 8090, 'UP', 100, 'Y', CURRENT_TIMESTAMP(3), 'SYSTEM', 'SYSTEM')
ON DUPLICATE KEY UPDATE
    service_id = VALUES(service_id),
    endpoint_code = VALUES(endpoint_code),
    instance_name = VALUES(instance_name),
    base_url = VALUES(base_url),
    host_name = VALUES(host_name),
    port_no = VALUES(port_no),
    instance_status = VALUES(instance_status),
    weight = VALUES(weight),
    active_yn = VALUES(active_yn),
    last_heartbeat_at = VALUES(last_heartbeat_at),
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO cpf_service_routing_policy (
    service_id, endpoint_code, routing_mode, load_balance_type, failover_enabled_yn,
    health_check_required_yn, active_yn, priority, created_by, updated_by
) VALUES
    ('BZA', 'BZA_API', 'PRIMARY', 'WEIGHT', 'Y', 'Y', 'Y', 100, 'SYSTEM', 'SYSTEM'),
    ('MBR', 'MBR_API', 'PRIMARY', 'WEIGHT', 'Y', 'Y', 'Y', 100, 'SYSTEM', 'SYSTEM'),
    ('REF', 'REF_API', 'PRIMARY', 'WEIGHT', 'Y', 'Y', 'Y', 100, 'SYSTEM', 'SYSTEM'),
    ('BAT', 'BAT_API', 'PRIMARY', 'WEIGHT', 'Y', 'Y', 'Y', 100, 'SYSTEM', 'SYSTEM'),
    ('ACC', 'ACC_API', 'PRIMARY', 'WEIGHT', 'Y', 'Y', 'Y', 100, 'SYSTEM', 'SYSTEM'),
    ('ADM', 'ADM_API', 'PRIMARY', 'WEIGHT', 'Y', 'Y', 'Y', 100, 'SYSTEM', 'SYSTEM')
ON DUPLICATE KEY UPDATE
    routing_mode = VALUES(routing_mode),
    load_balance_type = VALUES(load_balance_type),
    failover_enabled_yn = VALUES(failover_enabled_yn),
    health_check_required_yn = VALUES(health_check_required_yn),
    active_yn = VALUES(active_yn),
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO cpf_service_circuit_state (
    service_id, endpoint_code, instance_id, circuit_state, failure_count, success_count, closed_at, created_by, updated_by
) VALUES
    ('BZA', 'BZA_API', 'BZA-local-01', 'CLOSED', 0, 0, CURRENT_TIMESTAMP(3), 'SYSTEM', 'SYSTEM'),
    ('MBR', 'MBR_API', 'MBR-local-01', 'CLOSED', 0, 0, CURRENT_TIMESTAMP(3), 'SYSTEM', 'SYSTEM'),
    ('REF', 'REF_API', 'REF-local-01', 'CLOSED', 0, 0, CURRENT_TIMESTAMP(3), 'SYSTEM', 'SYSTEM'),
    ('BAT', 'BAT_API', 'BAT-local-01', 'CLOSED', 0, 0, CURRENT_TIMESTAMP(3), 'SYSTEM', 'SYSTEM'),
    ('ACC', 'ACC_API', 'ACC-local-01', 'CLOSED', 0, 0, CURRENT_TIMESTAMP(3), 'SYSTEM', 'SYSTEM'),
    ('ADM', 'ADM_API', 'ADM-local-01', 'CLOSED', 0, 0, CURRENT_TIMESTAMP(3), 'SYSTEM', 'SYSTEM')
ON DUPLICATE KEY UPDATE
    circuit_state = VALUES(circuit_state),
    failure_count = VALUES(failure_count),
    success_count = VALUES(success_count),
    closed_at = VALUES(closed_at),
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO cpf_service_health_status (
    service_id, endpoint_code, instance_id, health_status, http_status,
    response_time_ms, failure_message, checked_at, created_by, updated_by
)
SELECT 'BZA', 'BZA_API', 'BZA-local-01', 'UP', 200, 0, NULL, CURRENT_TIMESTAMP(3), 'SYSTEM', 'SYSTEM'
WHERE NOT EXISTS (
    SELECT 1 FROM cpf_service_health_status
    WHERE service_id = 'BZA' AND endpoint_code = 'BZA_API' AND instance_id = 'BZA-local-01' AND created_by = 'SYSTEM'
);

INSERT INTO cpf_service_health_status (
    service_id, endpoint_code, instance_id, health_status, http_status,
    response_time_ms, failure_message, checked_at, created_by, updated_by
)
SELECT 'MBR', 'MBR_API', 'MBR-local-01', 'UP', 200, 0, NULL, CURRENT_TIMESTAMP(3), 'SYSTEM', 'SYSTEM'
WHERE NOT EXISTS (
    SELECT 1 FROM cpf_service_health_status
    WHERE service_id = 'MBR' AND endpoint_code = 'MBR_API' AND instance_id = 'MBR-local-01' AND created_by = 'SYSTEM'
);

INSERT INTO cpf_service_health_status (
    service_id, endpoint_code, instance_id, health_status, http_status,
    response_time_ms, failure_message, checked_at, created_by, updated_by
)
SELECT 'REF', 'REF_API', 'REF-local-01', 'UP', 200, 0, NULL, CURRENT_TIMESTAMP(3), 'SYSTEM', 'SYSTEM'
WHERE NOT EXISTS (
    SELECT 1 FROM cpf_service_health_status
    WHERE service_id = 'REF' AND endpoint_code = 'REF_API' AND instance_id = 'REF-local-01' AND created_by = 'SYSTEM'
);

INSERT INTO cpf_service_health_status (
    service_id, endpoint_code, instance_id, health_status, http_status,
    response_time_ms, failure_message, checked_at, created_by, updated_by
)
SELECT 'BAT', 'BAT_API', 'BAT-local-01', 'UP', 200, 0, NULL, CURRENT_TIMESTAMP(3), 'SYSTEM', 'SYSTEM'
WHERE NOT EXISTS (
    SELECT 1 FROM cpf_service_health_status
    WHERE service_id = 'BAT' AND endpoint_code = 'BAT_API' AND instance_id = 'BAT-local-01' AND created_by = 'SYSTEM'
);

INSERT INTO cpf_service_health_status (
    service_id, endpoint_code, instance_id, health_status, http_status,
    response_time_ms, failure_message, checked_at, created_by, updated_by
)
SELECT 'ACC', 'ACC_API', 'ACC-local-01', 'UP', 200, 0, NULL, CURRENT_TIMESTAMP(3), 'SYSTEM', 'SYSTEM'
WHERE NOT EXISTS (
    SELECT 1 FROM cpf_service_health_status
    WHERE service_id = 'ACC' AND endpoint_code = 'ACC_API' AND instance_id = 'ACC-local-01' AND created_by = 'SYSTEM'
);

INSERT INTO cpf_service_health_status (
    service_id, endpoint_code, instance_id, health_status, http_status,
    response_time_ms, failure_message, checked_at, created_by, updated_by
)
SELECT 'ADM', 'ADM_API', 'ADM-local-01', 'UP', 200, 0, NULL, CURRENT_TIMESTAMP(3), 'SYSTEM', 'SYSTEM'
WHERE NOT EXISTS (
    SELECT 1 FROM cpf_service_health_status
    WHERE service_id = 'ADM' AND endpoint_code = 'ADM_API' AND instance_id = 'ADM-local-01' AND created_by = 'SYSTEM'
);

INSERT INTO batDB.bat_center_cut_parameter (
    center_cut_job_id, parameter_key, parameter_value, encrypted_yn, use_yn, created_by, updated_by
) VALUES
    ('CPF_REF_CENTER_CUT_SAMPLE_JOB', 'businessDatePattern', 'D+0', 'N', 'Y', 'SYSTEM', 'SYSTEM'),
    ('CPF_REF_CENTER_CUT_SAMPLE_JOB', 'defaultLimit', '10', 'N', 'Y', 'SYSTEM', 'SYSTEM'),
    ('CPF_REF_CENTER_CUT_SAMPLE_JOB', 'targetTable', 'ref_center_cut_sample_target', 'N', 'Y', 'SYSTEM', 'SYSTEM'),
    ('CPF_REF_CENTER_CUT_SAMPLE_JOB', 'resultTable', 'ref_center_cut_sample_result', 'N', 'Y', 'SYSTEM', 'SYSTEM')
ON DUPLICATE KEY UPDATE
    parameter_value = VALUES(parameter_value),
    encrypted_yn = VALUES(encrypted_yn),
    use_yn = VALUES(use_yn),
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP;
-- ============================================================================
-- specs/sql/52_standard_execution_alias_seed.sql
-- ============================================================================
-- 신규 설치에서도 구형 실행 ID 조회 호환 정보를 제공하는 정본 seed입니다.
USE cpfDB;

INSERT INTO cpf_standard_execution_alias (
    legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by
) VALUES
    ('BADM-RLG-EX-0001', 'BADMRL0001', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('BBAT-CUT-CL-0001', 'BBATCU0001', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('BBAT-OPS-FL-0001', 'BBATOP0001', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('BBAT-OPS-HB-0001', 'BBATOP0002', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('BBAT-OPS-SM-0001', 'BBATOP0003', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('BREF-EDU-CH-0001', 'BREFAA0001', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('BREF-EDU-RT-0001', 'BREFAA0002', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('BREF-EDU-TS-0001', 'BREFAA0003', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-BAT-01-0010', 'OADMBA0010', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-BAT-01-0012', 'OADMBA0012', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-BAT-01-0013', 'OADMBA0013', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-BAT-01-0014', 'OADMBA0014', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-BAT-01-0015', 'OADMBA0015', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-BAT-01-0016', 'OADMBA0016', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-BAT-01-0023', 'OADMBA0023', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-BAT-01-0024', 'OADMBA0024', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-BAT-01-0025', 'OADMBA0025', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-BAT-01-0027', 'OADMBA0027', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-BAT-01-0028', 'OADMBA0028', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-BAT-01-0029', 'OADMBA0029', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-BAT-01-0030', 'OADMBA0030', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-BAT-01-0032', 'OADMBA0032', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-BAT-01-0034', 'OADMBA0034', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-BAT-02-0011', 'OADMBA0011', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-BAT-02-0017', 'OADMBA0017', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-BAT-02-0018', 'OADMBA0018', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-BAT-02-0019', 'OADMBA0019', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-BAT-02-0026', 'OADMBA0026', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-BAT-03-0020', 'OADMBA0020', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-BAT-03-0021', 'OADMBA0021', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-BAT-03-0022', 'OADMBA0022', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-BAT-03-0031', 'OADMBA0031', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-BAT-03-0033', 'OADMBA0033', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-CDE-01-0010', 'OADMCD0010', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-CDE-01-0011', 'OADMCD0011', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-CDE-02-0012', 'OADMCD0012', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-CDE-03-0013', 'OADMCD0013', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-CDE-04-0014', 'OADMCD0014', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-CFG-01-0010', 'OADMCF0010', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-CFG-01-0011', 'OADMCF0011', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-CFG-02-0012', 'OADMCF0012', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-CFG-03-0013', 'OADMCF0013', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-CFG-04-0014', 'OADMCF0014', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-CTC-01-0010', 'OADMCT0010', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-CTC-01-0020', 'OADMCT0020', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-CTC-01-0030', 'OADMCT0030', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-CTC-01-0040', 'OADMCT0040', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-CTC-01-0050', 'OADMCT0050', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-CTC-01-0060', 'OADMCT0060', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-CTC-01-0070', 'OADMCT0070', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-DWN-01-0001', 'OADMDW0001', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-DWN-01-0002', 'OADMDW0002', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-DWN-02-0003', 'OADMDW0003', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-EXE-01-0001', 'OADMEX0001', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-EXE-01-0002', 'OADMEX0002', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-LGP-01-0010', 'OADMLG0010', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-LGP-01-0011', 'OADMLG0011', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-LGP-01-0018', 'OADMLG0018', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-LGP-01-0020', 'OADMLG0020', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-LGP-01-0021', 'OADMLG0021', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-LGP-03-0012', 'OADMLG0012', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-LGP-03-0013', 'OADMLG0013', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-LGP-03-0014', 'OADMLG0014', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-LGP-03-0016', 'OADMLG0016', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-LGP-03-0018', 'OADMLG0019', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-LGP-04-0015', 'OADMLG0015', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-LGP-04-0017', 'OADMLG0017', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-LGP-04-0019', 'OADMLG0022', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-MBR-01-0010', 'OADMMB0010', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-MBR-01-0011', 'OADMMB0011', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-MBR-02-0012', 'OADMMB0012', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-MBR-02-0015', 'OADMMB0015', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-MBR-03-0013', 'OADMMB0013', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-MBR-03-0014', 'OADMMB0014', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-MBR-04-0016', 'OADMMB0016', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-MSG-01-0010', 'OADMMS0010', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-MSG-01-0011', 'OADMMS0011', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-MSG-02-0012', 'OADMMS0012', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-MSG-03-0013', 'OADMMS0013', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-MSG-04-0014', 'OADMMS0014', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-NTF-01-0010', 'OADMNT0010', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-NTF-01-0011', 'OADMNT0011', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-NTF-01-0014', 'OADMNT0014', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-NTF-02-0012', 'OADMNT0012', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-NTF-02-0016', 'OADMNT0016', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-NTF-03-0013', 'OADMNT0013', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-NTF-03-0015', 'OADMNT0015', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-OBS-01-0010', 'OADMOB0010', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-OBS-01-0011', 'OADMOB0011', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-OBS-01-0012', 'OADMOB0012', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-OPR-01-0001', 'OADMOP0001', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-OPR-01-0002', 'OADMOP0002', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-OPR-01-0010', 'OADMOP0010', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-OPR-01-0020', 'OADMOP0020', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-OPR-01-0030', 'OADMOP0030', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-OPR-01-0034', 'OADMOP0034', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-OPR-01-0035', 'OADMOP0035', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-OPR-01-0036', 'OADMOP0036', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-OPR-01-0040', 'OADMOP0040', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-OPR-01-0041', 'OADMOP0041', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-OPR-01-0042', 'OADMOP0042', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-OPR-01-0043', 'OADMOP0043', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-OPR-01-0050', 'OADMOP0050', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-OPR-02-0031', 'OADMOP0031', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-OPR-02-0042', 'OADMOP0044', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-OPR-03-0032', 'OADMOP0032', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-OPR-03-0037', 'OADMOP0037', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-OPR-03-0038', 'OADMOP0038', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-OPR-03-0039', 'OADMOP0039', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-OPR-03-0043', 'OADMOP0045', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-OPR-03-0044', 'OADMOP0046', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-OPR-03-0045', 'OADMOP0047', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-OPR-04-0022', 'OADMOP0022', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-OPR-04-0044', 'OADMOP0048', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-OPR-05-0011', 'OADMOP0011', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-OPR-05-0021', 'OADMOP0021', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-OPR-06-0033', 'OADMOP0033', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-OPR-06-0040', 'OADMOP0049', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-OPR-06-0042', 'OADMOP0051', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-PER-01-0010', 'OADMPE0010', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-PER-01-0011', 'OADMPE0011', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-PER-01-0014', 'OADMPE0014', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-PER-01-0015', 'OADMPE0015', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-PER-01-0019', 'OADMPE0019', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-PER-01-0020', 'OADMPE0020', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-PER-01-0024', 'OADMPE0024', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-PER-01-0025', 'OADMPE0025', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-PER-01-0029', 'OADMPE0029', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-PER-01-0030', 'OADMPE0030', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-PER-01-0034', 'OADMPE0034', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-PER-02-0016', 'OADMPE0016', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-PER-02-0021', 'OADMPE0021', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-PER-02-0026', 'OADMPE0026', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-PER-02-0031', 'OADMPE0031', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-PER-03-0012', 'OADMPE0012', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-PER-03-0013', 'OADMPE0013', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-PER-03-0017', 'OADMPE0017', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-PER-03-0018', 'OADMPE0018', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-PER-03-0022', 'OADMPE0022', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-PER-03-0023', 'OADMPE0023', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-PER-03-0027', 'OADMPE0027', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-PER-03-0028', 'OADMPE0028', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-PER-03-0032', 'OADMPE0032', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-PER-03-0033', 'OADMPE0033', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-PER-03-0035', 'OADMPE0035', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-REL-01-0001', 'OADMRE0001', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-REL-01-0002', 'OADMRE0002', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-REL-01-0003', 'OADMRE0003', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-REL-01-0004', 'OADMRE0004', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-REL-01-0006', 'OADMRE0006', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-REL-01-0007', 'OADMRE0007', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-REL-01-0009', 'OADMRE0009', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-REL-01-0010', 'OADMRE0010', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-REL-01-0011', 'OADMRE0011', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-REL-05-0005', 'OADMRE0005', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-REL-05-0008', 'OADMRE0008', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-REL-05-0012', 'OADMRE0012', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-REL-05-0013', 'OADMRE0013', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-RLG-CR-0001', 'OADMRL0001', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-RLG-DL-0001', 'OADMRL0002', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-RLG-DL-0002', 'OADMRL0003', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-RLG-DW-0001', 'OADMRL0004', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-RLG-IS-0001', 'OADMRL0005', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-RLG-QY-0001', 'OADMRL0006', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-RLG-QY-0002', 'OADMRL0007', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-RLG-QY-0003', 'OADMRL0008', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-RLG-QY-0004', 'OADMRL0009', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-SEC-01-0010', 'OADMSE0010', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-SEC-01-0012', 'OADMSE0012', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-SEC-03-0011', 'OADMSE0011', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-SEC-03-0013', 'OADMSE0013', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-SEC-03-0014', 'OADMSE0014', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-SEC-03-0015', 'OADMSE0015', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-SVC-01-0010', 'OADMSV0010', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-SVC-01-0020', 'OADMSV0020', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-SVC-01-0030', 'OADMSV0030', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-SVC-01-0040', 'OADMSV0040', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-SVC-01-0050', 'OADMSV0050', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-SVC-01-0060', 'OADMSV0060', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-SVC-01-0070', 'OADMSV0070', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-TRG-01-0001', 'OADMTR0001', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-TRG-01-0002', 'OADMTR0002', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-TRG-01-0003', 'OADMTR0003', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-TRG-01-0004', 'OADMTR0004', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-TRG-01-0005', 'OADMTR0005', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-TRG-01-0006', 'OADMTR0006', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-TRN-01-0010', 'OADMTR0010', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-TRN-01-0011', 'OADMTR0011', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-TRN-04-0013', 'OADMTR0013', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OADM-TRN-05-0012', 'OADMTR0012', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OBAT-OPR-01-0003', 'OBATOP0003', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OBAT-OPR-02-0002', 'OBATOP0002', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OBZA-ADM-01-1001', 'OBZAAD1001', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OBZA-ADM-03-1002', 'OBZAAD1002', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OBZA-APR-01-0001', 'OBZAAP0001', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OBZA-APR-01-0003', 'OBZAAP0003', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OBZA-APR-02-0002', 'OBZAAP0002', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OBZA-APR-05-0004', 'OBZAAP0004', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OBZA-ATC-01-0001', 'OBZAAT0001', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OBZA-ATC-02-0002', 'OBZAAT0002', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OBZA-ATC-DL-0003', 'OBZAAT0003', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OBZA-AUD-01-0001', 'OBZAUD0001', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OBZA-AUT-01-0004', 'OBZAAU0004', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OBZA-AUT-01-0005', 'OBZAAU0005', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OBZA-AUT-01-0007', 'OBZAAU0007', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OBZA-AUT-02-0001', 'OBZAAU0001', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OBZA-AUT-02-0002', 'OBZAAU0002', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OBZA-AUT-02-0003', 'OBZAAU0003', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OBZA-AUT-03-0006', 'OBZAAU0006', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OBZA-AUT-04-0008', 'OBZAAU0008', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OBZA-CUS-01-1001', 'OBZACU1001', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OBZA-DSH-01-0001', 'OBZADS0001', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OBZA-DWN-01-0002', 'OBZADW0002', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OBZA-DWN-01-1001', 'OBZADW1001', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OBZA-EMP-01-0001', 'OBZAEM0001', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OBZA-EMP-03-0002', 'OBZAEM0002', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OBZA-MNU-01-1001', 'OBZAMN1001', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OBZA-MNU-03-1002', 'OBZAMN1002', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OBZA-MSK-02-1001', 'OBZAMS1001', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OBZA-NTF-01-0001', 'OBZANT0001', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OBZA-NTF-02-0002', 'OBZANT0002', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OBZA-NTF-03-0003', 'OBZANT0003', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OBZA-ORD-01-1001', 'OBZAOR1001', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OBZA-ORG-01-0001', 'OBZAOR0001', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OBZA-ORG-03-0002', 'OBZAOR0002', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OBZA-PER-01-0002', 'OBZAPE0002', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OBZA-PER-01-0003', 'OBZAPE0003', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OBZA-PER-01-1001', 'OBZAPE1001', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OBZA-PER-02-0004', 'OBZAPE0004', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OBZA-PER-03-1002', 'OBZAPE1002', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OBZA-PRD-01-1001', 'OBZAPR1001', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OBZA-ROL-01-1001', 'OBZARO1001', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OBZA-ROL-03-1002', 'OBZARO1002', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OBZA-SCH-01-0001', 'OBZASC0001', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OBZA-SCH-03-0002', 'OBZASC0002', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OBZA-SCH-04-0003', 'OBZASC0003', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OBZA-SET-01-1001', 'OBZASE1001', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OBZA-USR-QY-0000', 'OBZAUS0001', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OBZA-USR-QY-0001', 'OBZAUS0002', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OMBR-AUT-01-0004', 'OMBRAU0004', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OMBR-AUT-01-0005', 'OMBRAU0005', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OMBR-AUT-02-0001', 'OMBRAU0001', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OMBR-AUT-02-0002', 'OMBRAU0002', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OMBR-AUT-02-0003', 'OMBRAU0003', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OMBR-BSE-01-0001', 'OMBRMB0001', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OMBR-BSE-01-0002', 'OMBRMB0002', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OMBR-BSE-01-0003', 'OMBRMB0003', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OMBR-BSE-02-0001', 'OMBRMB0004', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OMBR-BSE-03-0001', 'OMBRMB0005', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OMBR-BSE-04-0001', 'OMBRMB0006', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OREF-EDU-01-0001', 'OREFAA0001', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OREF-EDU-01-0002', 'OREFAA0002', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OREF-EDU-01-0003', 'OREFAA0003', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OREF-EDU-01-0099', 'OREFAA0099', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OREF-EDU-02-0001', 'OREFAA0004', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OREF-EDU-02-0010', 'OREFAA0010', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OREF-EDU-02-0020', 'OREFAA0020', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OREF-EDU-02-0030', 'OREFAA0030', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OREF-EDU-03-0001', 'OREFAA0005', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OREF-EDU-03-0002', 'OREFAA0006', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OREF-EDU-03-0003', 'OREFAA0007', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OREF-EDU-04-0001', 'OREFAA0008', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OREF-EDU-04-0002', 'OREFAA0009', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OREF-EDU-05-0001', 'OREFAA0011', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OREF-EDU-05-0002', 'OREFAA0012', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OREF-EDU-05-9001', 'OREFAA9001', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OREF-EDU-08-0001', 'OREFAA0013', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OREF-EDU-08-0010', 'OREFAA0014', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OREF-EDU-08-9001', 'OREFAA9002', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OREF-EDU-09-0001', 'OREFAA0015', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OREF-EDU-09-0002', 'OREFAA0016', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OREF-EDU-09-0003', 'OREFAA0017', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OREF-EDU-09-0004', 'OREFAA0018', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OREF-EDU-09-0005', 'OREFAA0019', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OREF-EDU-09-0006', 'OREFAA0021', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OREF-EDU-09-0007', 'OREFAA0022', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OREF-EDU-09-0008', 'OREFAA0023', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OREF-EDU-09-0009', 'OREFAA0024', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OREF-EDU-09-0010', 'OREFAA0025', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OREF-EDU-09-0011', 'OREFAA0026', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OREF-EDU-09-0012', 'OREFAA0027', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OREF-EDU-09-0013', 'OREFAA0028', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OREF-EDU-09-0015', 'OREFAA0029', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OREF-EDU-09-0016', 'OREFAA0031', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OREF-EDU-09-0017', 'OREFAA0032', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OREF-EDU-09-0018', 'OREFAA0033', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OREF-EDU-09-0019', 'OREFAA0034', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OREF-EDU-09-0020', 'OREFAA0035', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OREF-EDU-09-0030', 'OREFAA0036', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OREF-EDU-09-0031', 'OREFAA0037', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OREF-EDU-09-0032', 'OREFAA0038', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OREF-EDU-09-0033', 'OREFAA0039', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OREF-EDU-09-0034', 'OREFAA0040', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OREF-EDU-09-0035', 'OREFAA0041', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OREF-EDU-09-0036', 'OREFAA0042', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OREF-EDU-09-0040', 'OREFAA0043', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OREF-EDU-09-0051', 'OREFAA0051', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OREF-EDU-09-0060', 'OREFAA0060', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OREF-EDU-09-0072', 'OREFAA0072', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OREF-EDU-09-0073', 'OREFAA0073', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OREF-EDU-09-0080', 'OREFAA0080', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OREF-EDU-12-0001', 'OREFAA0044', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OREF-EDU-12-0002', 'OREFAA0045', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OREF-EDU-12-0003', 'OREFAA0046', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OREF-EDU-13-0001', 'OREFAA0047', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OREF-EDU-13-0002', 'OREFAA0048', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OREF-EDU-13-0003', 'OREFAA0049', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OREF-EDU-13-0004', 'OREFAA0050', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OREF-EDU-13-0005', 'OREFAA0052', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OREF-EDU-13-0006', 'OREFAA0053', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OREF-EDU-13-0007', 'OREFAA0054', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OREF-EDU-13-0008', 'OREFAA0055', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OREF-EDU-14-0001', 'OREFAA0056', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OREF-EDU-15-0001', 'OREFAA0057', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OREF-EDU-16-0001', 'OREFAA0058', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OREF-EDU-16-0002', 'OREFAA0059', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OREF-EDU-16-0003', 'OREFAA0061', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OREF-EDU-16-0004', 'OREFAA0062', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OREF-EDU-16-0005', 'OREFAA0063', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OREF-EDU-16-0006', 'OREFAA0064', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OREF-EDU-17-0001', 'OREFAA0065', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OREF-EDU-17-0002', 'OREFAA0066', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OREF-QRY-01-0001', 'OREFQR0001', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OREF-QRY-01-0002', 'OREFQR0002', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OREF-QRY-01-0003', 'OREFQR0003', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OREF-QRY-01-0004', 'OREFQR0004', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OREF-QRY-01-0005', 'OREFQR0005', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED')
ON DUPLICATE KEY UPDATE
    standard_execution_id = VALUES(standard_execution_id),
    migration_reason = VALUES(migration_reason),
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP;
-- ============================================================================
-- specs/sql/57_external_seed_data.sql
-- ============================================================================
-- EXS 로컬 검증용 기관·endpoint와 CPF 서비스 레지스트리 seed입니다.
-- 운영 환경에서는 기관별 실제 URI와 인증 프로파일을 별도 승인 절차로 등록합니다.

USE exsDB;

INSERT INTO exs_institution (
    institution_code, institution_name, enabled_yn, created_by, updated_by
) VALUES (
    'CPF-REF', 'CPF 참조 대외 시뮬레이터', 'Y', 'SEED', 'SEED'
) ON DUPLICATE KEY UPDATE
    institution_name = VALUES(institution_name),
    enabled_yn = VALUES(enabled_yn),
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP(3);

INSERT INTO exs_channel (
    institution_code, channel_code, direction, enabled_yn, created_by, updated_by
) VALUES (
    'CPF-REF', 'REST', 'BIDIRECTIONAL', 'Y', 'SEED', 'SEED'
) ON DUPLICATE KEY UPDATE
    direction = VALUES(direction),
    enabled_yn = VALUES(enabled_yn),
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP(3);

INSERT INTO exs_endpoint (
    endpoint_code, institution_code, service_id, http_method, endpoint_uri,
    result_query_uri, timeout_ms, retry_count, enabled_yn, created_by, updated_by
) VALUES (
    'REF-EXTERNAL-SIMULATOR', 'CPF-REF', 'REF', 'POST',
    '/api/reference/external-simulator/executions',
    '/api/reference/external-simulator/results/{externalRequestId}',
    3000, 0, 'Y', 'SEED', 'SEED'
) ON DUPLICATE KEY UPDATE
    service_id = VALUES(service_id),
    http_method = VALUES(http_method),
    endpoint_uri = VALUES(endpoint_uri),
    result_query_uri = VALUES(result_query_uri),
    timeout_ms = VALUES(timeout_ms),
    retry_count = VALUES(retry_count),
    enabled_yn = VALUES(enabled_yn),
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP(3);

INSERT INTO exs_control_policy (
    institution_code, control_type, enabled_yn, reason, created_by, updated_by
) VALUES (
    'CPF-REF', 'SEND', 'Y', '로컬 대외 시뮬레이터 송신 허용', 'SEED', 'SEED'
) ON DUPLICATE KEY UPDATE
    enabled_yn = VALUES(enabled_yn),
    reason = VALUES(reason),
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP(3);

USE cpfDB;

INSERT INTO cpf_service (
    service_id, service_name, service_type, owner_module_code, description, use_yn, created_by, updated_by
) VALUES (
    'REF', 'CPF 참조 서비스', 'INTERNAL', 'REF', 'EXS 로컬 결과 불명 검증 대상', 'Y', 'SEED', 'SEED'
) ON DUPLICATE KEY UPDATE
    service_name = VALUES(service_name),
    owner_module_code = VALUES(owner_module_code),
    description = VALUES(description),
    use_yn = VALUES(use_yn),
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP(3);

INSERT INTO cpf_service_endpoint (
    endpoint_code, service_id, endpoint_name, endpoint_type, base_url, context_path,
    default_timeout_ms, default_retry_count, use_yn, created_by, updated_by
) VALUES (
    'REF-EXTERNAL-SIMULATOR', 'REF', 'REF 대외 시뮬레이터', 'HTTP',
    'http://127.0.0.1:8099', '', 3000, 0, 'Y', 'SEED', 'SEED'
) ON DUPLICATE KEY UPDATE
    service_id = VALUES(service_id),
    endpoint_name = VALUES(endpoint_name),
    endpoint_type = VALUES(endpoint_type),
    base_url = VALUES(base_url),
    context_path = VALUES(context_path),
    default_timeout_ms = VALUES(default_timeout_ms),
    default_retry_count = VALUES(default_retry_count),
    use_yn = VALUES(use_yn),
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP(3);

INSERT INTO cpf_service_instance (
    instance_id, service_id, endpoint_code, instance_name, base_url, host_name,
    port_no, instance_status, weight, active_yn, last_heartbeat_at, created_by, updated_by
) VALUES (
    'REF-EXS-local-01', 'REF', 'REF-EXTERNAL-SIMULATOR', 'REF 대외 시뮬레이터 인스턴스',
    'http://127.0.0.1:8099', 'localhost', 8099, 'UP', 100, 'Y', CURRENT_TIMESTAMP(3), 'SEED', 'SEED'
) ON DUPLICATE KEY UPDATE
    service_id = VALUES(service_id),
    endpoint_code = VALUES(endpoint_code),
    instance_name = VALUES(instance_name),
    base_url = VALUES(base_url),
    host_name = VALUES(host_name),
    port_no = VALUES(port_no),
    instance_status = VALUES(instance_status),
    active_yn = VALUES(active_yn),
    last_heartbeat_at = VALUES(last_heartbeat_at),
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP(3);

INSERT INTO cpf_service_routing_policy (
    service_id, endpoint_code, routing_mode, load_balance_type, failover_enabled_yn,
    health_check_required_yn, active_yn, priority, created_by, updated_by
) VALUES (
    'REF', 'REF-EXTERNAL-SIMULATOR', 'PRIMARY', 'WEIGHT', 'N', 'N', 'Y', 100, 'SEED', 'SEED'
) ON DUPLICATE KEY UPDATE
    routing_mode = VALUES(routing_mode),
    load_balance_type = VALUES(load_balance_type),
    failover_enabled_yn = VALUES(failover_enabled_yn),
    health_check_required_yn = VALUES(health_check_required_yn),
    active_yn = VALUES(active_yn),
    priority = VALUES(priority),
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP(3);
-- ============================================================================
-- specs/sql/60_adm_seed_data.sql
-- ============================================================================
-- ADM 초기 역할, 메뉴, 버튼 권한, 보안 정책, 로컬 계정 데이터입니다.
-- 대상 DB: admDB

USE admDB;

INSERT INTO adm_role (ROLE_ID, ROLE_NAME, ROLE_TYPE, DESCRIPTION, USE_YN, created_by, updated_by)
VALUES
    ('ADM_ADMIN', '프레임워크 관리자', 'ADMIN', '모든 ADM 메뉴와 운영 작업을 관리합니다.', 'Y', 'SYSTEM', 'SYSTEM'),
    ('ADM_DEV_OPERATOR', '개발자 운영자', 'DEVELOPER_OPERATOR', '로그, 캐시, 코드, 메시지, 설정, 배치 관제를 운영합니다.', 'Y', 'SYSTEM', 'SYSTEM'),
    ('ADM_BIZ_OPERATOR', '업무 운영자', 'BUSINESS_OPERATOR', '회원, 거래 로그, 배치, 캐시 같은 업무 운영 기능을 수행합니다.', 'Y', 'SYSTEM', 'SYSTEM'),
    ('ADM_VIEWER', '조회 전용 운영자', 'VIEWER', '운영 정보를 조회만 할 수 있습니다.', 'Y', 'SYSTEM', 'SYSTEM'),
    ('ADM_OPERATOR', '운영자 호환 역할', 'DEVELOPER_OPERATOR', '기존 ADM_OPERATOR 호환을 위한 역할입니다.', 'Y', 'SYSTEM', 'SYSTEM')
ON DUPLICATE KEY UPDATE
    ROLE_NAME = VALUES(ROLE_NAME),
    ROLE_TYPE = VALUES(ROLE_TYPE),
    DESCRIPTION = VALUES(DESCRIPTION),
    USE_YN = VALUES(USE_YN),
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO adm_menu (MENU_ID, PARENT_MENU_ID, MENU_NAME, MENU_PATH, SORT_ORDER, USE_YN, created_by, updated_by)
VALUES
    ('DASHBOARD', NULL, '대시보드', '/adm', 10, 'Y', 'SYSTEM', 'SYSTEM'),
    ('LOG_LIST', NULL, '온라인 거래 로그', '/adm#logs', 20, 'Y', 'SYSTEM', 'SYSTEM'),
    ('STANDARD_EXECUTION', NULL, '표준 실행 카탈로그', '/adm#standard-executions', 23, 'Y', 'SYSTEM', 'SYSTEM'),
    ('CHANNEL_POLICY', NULL, '채널 정책', '/adm#channel-policy', 24, 'Y', 'SYSTEM', 'SYSTEM'),
    ('REMOTE_LOG', NULL, '원격 로그 관리', '/adm#remote-logs', 25, 'Y', 'SYSTEM', 'SYSTEM'),
    ('TRANSACTION_META', NULL, '거래 메타', '/adm#transactions', 25, 'Y', 'SYSTEM', 'SYSTEM'),
    ('AUDIT_LOG', NULL, '감사 로그', '/adm#audit-logs', 30, 'Y', 'SYSTEM', 'SYSTEM'),
    ('MEMBER', NULL, '회원 관리', '/adm#members', 40, 'Y', 'SYSTEM', 'SYSTEM'),
    ('BATCH', NULL, '배치 관제', '/adm#batch', 50, 'Y', 'SYSTEM', 'SYSTEM'),
    ('RELIABILITY', NULL, '신뢰성 처리 관제', '/adm#reliability', 52, 'Y', 'SYSTEM', 'SYSTEM'),
    ('NOTIFICATION', NULL, '알림 관리', '/adm#notifications', 55, 'Y', 'SYSTEM', 'SYSTEM'),
    ('DOWNLOAD', NULL, '다운로드 감사', '/adm#downloads', 58, 'Y', 'SYSTEM', 'SYSTEM'),
    ('CACHE', NULL, '캐시 관리', '/adm#cache', 60, 'Y', 'SYSTEM', 'SYSTEM'),
    ('MESSAGE', NULL, '메시지 관리', '/adm#messages', 70, 'Y', 'SYSTEM', 'SYSTEM'),
    ('CODE', NULL, '코드 관리', '/adm#codes', 80, 'Y', 'SYSTEM', 'SYSTEM'),
    ('RESPONSE_CODE', NULL, '응답코드 관리', '/adm#response-codes', 90, 'Y', 'SYSTEM', 'SYSTEM'),
    ('CONFIG', NULL, '설정 관리', '/adm#configs', 100, 'Y', 'SYSTEM', 'SYSTEM'),
    ('DYNAMIC_LOG', NULL, '동적 로그 레벨', '/adm#log-level', 110, 'Y', 'SYSTEM', 'SYSTEM'),
    ('LOG_POLICY', NULL, '로그 정책', '/adm#log-policies', 115, 'Y', 'SYSTEM', 'SYSTEM'),
    ('PASSWORD', NULL, '비밀번호 관리', '/adm#password', 120, 'Y', 'SYSTEM', 'SYSTEM'),
    ('SECURITY', NULL, '보안 운영', '/adm#security', 130, 'Y', 'SYSTEM', 'SYSTEM'),
    ('PERMISSION', NULL, '권한 관리', '/adm#permissions', 140, 'Y', 'SYSTEM', 'SYSTEM'),
    ('OPERATOR', NULL, '운영자 관리', '/adm#operators', 150, 'Y', 'SYSTEM', 'SYSTEM')
ON DUPLICATE KEY UPDATE
    PARENT_MENU_ID = VALUES(PARENT_MENU_ID),
    MENU_NAME = VALUES(MENU_NAME),
    MENU_PATH = VALUES(MENU_PATH),
    SORT_ORDER = VALUES(SORT_ORDER),
    USE_YN = VALUES(USE_YN),
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO adm_button (BUTTON_ID, MENU_ID, ACTION_CODE, BUTTON_NAME, HTTP_METHOD, API_PATTERN, SORT_ORDER, USE_YN, created_by, updated_by)
VALUES
    ('LOG_LIST_READ', 'LOG_LIST', 'READ', '조회', 'GET', '/adm/api/logs/**', 10, 'Y', 'SYSTEM', 'SYSTEM'),
    ('LOG_LIST_DETAIL', 'LOG_LIST', 'DETAIL', '상세 조회', 'GET', '/adm/api/logs/**', 20, 'Y', 'SYSTEM', 'SYSTEM'),
    ('LOG_LIST_DOWNLOAD', 'LOG_LIST', 'DOWNLOAD', '다운로드', 'GET', '/adm/api/logs/**', 30, 'Y', 'SYSTEM', 'SYSTEM'),
    ('STANDARD_EXECUTION_READ', 'STANDARD_EXECUTION', 'READ', '표준 실행 조회', 'GET', '/adm/api/standard-executions/**', 10, 'Y', 'SYSTEM', 'SYSTEM'),
    ('CHANNEL_POLICY_READ', 'CHANNEL_POLICY', 'READ', '채널 정책 조회', 'GET', '/adm/api/channels/**', 10, 'Y', 'SYSTEM', 'SYSTEM'),
    ('CHANNEL_POLICY_WRITE', 'CHANNEL_POLICY', 'WRITE', '채널·거래 정책 변경', 'PUT', '/adm/api/channels/**', 20, 'Y', 'SYSTEM', 'SYSTEM'),
    ('CHANNEL_POLICY_REFRESH', 'CHANNEL_POLICY', 'REFRESH', '채널 정책 스냅샷 갱신', 'POST', '/adm/api/channels/refresh', 30, 'Y', 'SYSTEM', 'SYSTEM'),
    ('CHANNEL_POLICY_IMPORT', 'CHANNEL_POLICY', 'IMPORT', '채널 정책 패키지 반입', 'POST', '/adm/api/channels/package/import', 40, 'Y', 'SYSTEM', 'SYSTEM'),
    ('REMOTE_LOG_READ', 'REMOTE_LOG', 'READ', '로그 아티팩트 조회', 'GET', '/adm/api/remote-logs/**', 10, 'Y', 'SYSTEM', 'SYSTEM'),
    ('REMOTE_LOG_DOWNLOAD', 'REMOTE_LOG', 'DOWNLOAD', '로그 아티팩트 다운로드', 'GET', '/adm/api/remote-logs/*/download', 20, 'Y', 'SYSTEM', 'SYSTEM'),
    ('REMOTE_LOG_BUNDLE_DOWNLOAD', 'REMOTE_LOG', 'DOWNLOAD', '동기 로그 ZIP 다운로드', 'POST', '/adm/api/remote-logs/bundles', 30, 'Y', 'SYSTEM', 'SYSTEM'),
    ('REMOTE_LOG_BUNDLE_CREATE', 'REMOTE_LOG', 'CREATE', '비동기 로그 ZIP 작업 등록', 'POST', '/adm/api/remote-logs/bundle-jobs', 40, 'Y', 'SYSTEM', 'SYSTEM'),
    ('REMOTE_LOG_BUNDLE_TOKEN', 'REMOTE_LOG', 'ISSUE', '로그 ZIP 다운로드 token 발급', 'POST', '/adm/api/remote-logs/bundle-jobs/*/download-tokens', 50, 'Y', 'SYSTEM', 'SYSTEM'),
    ('REMOTE_LOG_JOB_DOWNLOAD', 'REMOTE_LOG', 'DOWNLOAD', '비동기 로그 ZIP 다운로드', 'GET', '/adm/api/remote-logs/bundle-jobs/*/download', 60, 'Y', 'SYSTEM', 'SYSTEM'),
    ('TRANSACTION_META_READ', 'TRANSACTION_META', 'READ', '거래 메타 조회', 'GET', '/adm/api/transactions/**', 10, 'Y', 'SYSTEM', 'SYSTEM'),
    ('TRANSACTION_META_SCAN', 'TRANSACTION_META', 'SCAN', '거래 메타 스캔', 'POST', '/adm/api/transactions/scan', 20, 'Y', 'SYSTEM', 'SYSTEM'),
    ('TRANSACTION_META_WRITE', 'TRANSACTION_META', 'WRITE', '거래 메타 비활성화', 'POST', '/adm/api/transactions/*/inactive', 30, 'Y', 'SYSTEM', 'SYSTEM'),
    ('AUDIT_LOG_READ', 'AUDIT_LOG', 'READ', '조회', 'GET', '/adm/api/audit-logs/**', 10, 'Y', 'SYSTEM', 'SYSTEM'),
    ('MEMBER_READ', 'MEMBER', 'READ', '회원 조회', 'GET', '/adm/api/members/**', 10, 'Y', 'SYSTEM', 'SYSTEM'),
    ('MEMBER_CREATE', 'MEMBER', 'CREATE', '회원 등록', 'POST', '/adm/api/members', 20, 'Y', 'SYSTEM', 'SYSTEM'),
    ('MEMBER_UPDATE', 'MEMBER', 'UPDATE', '회원 수정', 'PUT', '/adm/api/members/*', 30, 'Y', 'SYSTEM', 'SYSTEM'),
    ('MEMBER_STATUS', 'MEMBER', 'STATUS', '회원 상태 변경', 'PUT', '/adm/api/members/*/status', 40, 'Y', 'SYSTEM', 'SYSTEM'),
    ('MEMBER_ROLE_GRANT', 'MEMBER', 'ROLE_GRANT', '회원 권한 부여', 'POST', '/adm/api/members/*/roles', 50, 'Y', 'SYSTEM', 'SYSTEM'),
    ('MEMBER_ROLE_REVOKE', 'MEMBER', 'ROLE_REVOKE', '회원 권한 회수', 'DELETE', '/adm/api/members/*/roles/*', 60, 'Y', 'SYSTEM', 'SYSTEM'),
    ('BATCH_READ', 'BATCH', 'READ', '조회', 'GET', '/adm/api/batch/**', 10, 'Y', 'SYSTEM', 'SYSTEM'),
    ('BATCH_REGISTER', 'BATCH', 'REGISTER', '배치 등록', 'POST', '/adm/api/batch/jobs', 20, 'Y', 'SYSTEM', 'SYSTEM'),
    ('BATCH_EXECUTE', 'BATCH', 'EXECUTE', '수동 실행', 'POST', '/adm/api/batch/*/run', 30, 'Y', 'SYSTEM', 'SYSTEM'),
    ('BATCH_RETRY', 'BATCH', 'RETRY', '실패 재수행', 'POST', '/adm/api/batch/executions/*/retry', 40, 'Y', 'SYSTEM', 'SYSTEM'),
    ('BATCH_STOP', 'BATCH', 'STOP', '실행 중지', 'POST', '/adm/api/batch/executions/*/stop', 50, 'Y', 'SYSTEM', 'SYSTEM'),
    ('BATCH_SCHEDULE', 'BATCH', 'SCHEDULE', '스케줄 변경', 'POST', '/adm/api/batch/schedules/**', 60, 'Y', 'SYSTEM', 'SYSTEM'),
    ('BATCH_CALENDAR_SAVE', 'BATCH', 'CALENDAR_SAVE', '영업일 저장', 'POST', '/adm/api/batch/calendar', 70, 'Y', 'SYSTEM', 'SYSTEM'),
    ('BATCH_SIMULATION', 'BATCH', 'SIMULATION', '수행 시뮬레이션', 'GET', '/adm/api/batch/schedules/*/simulation', 80, 'Y', 'SYSTEM', 'SYSTEM'),
    ('BATCH_RELATION_READ', 'BATCH', 'RELATION_READ', '배치 관계 조회', 'GET', '/adm/api/batch/relations', 90, 'Y', 'SYSTEM', 'SYSTEM'),
    ('BATCH_TARGET_READ', 'BATCH', 'TARGET_READ', '수행 대상 조회', 'GET', '/adm/api/batch/execution-targets', 100, 'Y', 'SYSTEM', 'SYSTEM'),
    ('BATCH_SCHEDULER_RUN', 'BATCH', 'SCHEDULER_RUN', '스케줄러 1회 실행', 'POST', '/adm/api/batch/scheduler/run-once', 110, 'Y', 'SYSTEM', 'SYSTEM'),
    ('BATCH_JOB_DETAIL', 'BATCH', 'DETAIL', 'Job 상세 조회', 'GET', '/adm/api/batch/jobs/*', 120, 'Y', 'SYSTEM', 'SYSTEM'),
    ('BATCH_STEP_READ', 'BATCH', 'STEP_READ', 'Step 이력 조회', 'GET', '/adm/api/batch/steps', 130, 'Y', 'SYSTEM', 'SYSTEM'),
    ('BATCH_WORKER_READ', 'BATCH', 'WORKER_READ', 'Worker 상태 조회', 'GET', '/adm/api/batch/workers', 140, 'Y', 'SYSTEM', 'SYSTEM'),
    ('BATCH_LOCK_READ', 'BATCH', 'LOCK_READ', 'Lock 조회', 'GET', '/adm/api/batch/locks', 150, 'Y', 'SYSTEM', 'SYSTEM'),
    ('BATCH_LOCK_RELEASE', 'BATCH', 'LOCK_RELEASE', 'Lock 강제 해제', 'POST', '/adm/api/batch/locks/release', 160, 'Y', 'SYSTEM', 'SYSTEM'),
    ('BATCH_GHOST_READ', 'BATCH', 'GHOST_READ', 'Ghost 후보 조회', 'GET', '/adm/api/batch/ghost-candidates', 170, 'Y', 'SYSTEM', 'SYSTEM'),
    ('BATCH_GHOST_ACTION', 'BATCH', 'GHOST_ACTION', 'Ghost 조치', 'POST', '/adm/api/batch/ghost-candidates/*/actions', 180, 'Y', 'SYSTEM', 'SYSTEM'),
    ('BATCH_OPERATION_READ', 'BATCH', 'OPERATION_READ', '운영 작업 로그 조회', 'GET', '/adm/api/batch/operations', 190, 'Y', 'SYSTEM', 'SYSTEM'),
    ('RELIABILITY_READ', 'RELIABILITY', 'READ', '신뢰성 처리 조회', 'GET', '/adm/api/reliability/**', 10, 'Y', 'SYSTEM', 'SYSTEM'),
    ('RELIABILITY_REPLAY', 'RELIABILITY', 'REPLAY', 'DLQ 재처리', 'POST', '/adm/api/reliability/broker/dlq/*/replay', 20, 'Y', 'SYSTEM', 'SYSTEM'),
    ('RELIABILITY_RESOLVE', 'RELIABILITY', 'RESOLVE', '결과 미확정 수동 처리', 'POST', '/adm/api/reliability/unknown-results/*/resolve', 30, 'Y', 'SYSTEM', 'SYSTEM'),
    ('RELIABILITY_RECOVERY_RUN', 'RELIABILITY', 'RECOVERY_RUN', 'DB 거래 로그 복구 실행', 'POST', '/adm/api/reliability/transaction-log-recovery/run', 40, 'Y', 'SYSTEM', 'SYSTEM'),
    ('NOTIFICATION_READ', 'NOTIFICATION', 'READ', '알림 조회', 'GET', '/adm/api/notifications/**', 10, 'Y', 'SYSTEM', 'SYSTEM'),
    ('NOTIFICATION_WRITE', 'NOTIFICATION', 'WRITE', '알림 등록/수정', 'POST', '/adm/api/notifications/**', 20, 'Y', 'SYSTEM', 'SYSTEM'),
    ('NOTIFICATION_DISABLE', 'NOTIFICATION', 'DISABLE', '알림 비활성화', 'PUT', '/adm/api/notifications/rules/*/disable', 30, 'Y', 'SYSTEM', 'SYSTEM'),
    ('NOTIFICATION_TEST_SEND', 'NOTIFICATION', 'TEST_SEND', '알림 테스트 발송', 'POST', '/adm/api/notifications/rules/*/test-send', 40, 'Y', 'SYSTEM', 'SYSTEM'),
    ('DOWNLOAD_READ', 'DOWNLOAD', 'READ', '다운로드 감사 조회', 'GET', '/adm/api/downloads/**', 10, 'Y', 'SYSTEM', 'SYSTEM'),
    ('DOWNLOAD_EXECUTE', 'DOWNLOAD', 'DOWNLOAD', 'CSV 다운로드', 'POST', '/adm/api/downloads/csv', 20, 'Y', 'SYSTEM', 'SYSTEM'),
    ('CACHE_READ', 'CACHE', 'READ', '조회', 'GET', '/adm/api/cache/**', 10, 'Y', 'SYSTEM', 'SYSTEM'),
    ('CACHE_REFRESH', 'CACHE', 'REFRESH', '캐시 갱신', 'POST', '/adm/api/cache/**', 20, 'Y', 'SYSTEM', 'SYSTEM'),
    ('MESSAGE_READ', 'MESSAGE', 'READ', '조회', 'GET', '/adm/api/messages/**', 10, 'Y', 'SYSTEM', 'SYSTEM'),
    ('MESSAGE_WRITE', 'MESSAGE', 'WRITE', '등록/수정', 'POST', '/adm/api/messages/**', 20, 'Y', 'SYSTEM', 'SYSTEM'),
    ('MESSAGE_DISABLE', 'MESSAGE', 'DISABLE', '비활성', 'DELETE', '/adm/api/messages/**', 30, 'Y', 'SYSTEM', 'SYSTEM'),
    ('CODE_READ', 'CODE', 'READ', '조회', 'GET', '/adm/api/codes/**', 10, 'Y', 'SYSTEM', 'SYSTEM'),
    ('CODE_WRITE', 'CODE', 'WRITE', '등록/수정', 'POST', '/adm/api/codes/**', 20, 'Y', 'SYSTEM', 'SYSTEM'),
    ('CODE_DISABLE', 'CODE', 'DISABLE', '비활성', 'DELETE', '/adm/api/codes/**', 30, 'Y', 'SYSTEM', 'SYSTEM'),
    ('RESPONSE_CODE_READ', 'RESPONSE_CODE', 'READ', '조회', 'GET', '/adm/api/response-codes/**', 10, 'Y', 'SYSTEM', 'SYSTEM'),
    ('RESPONSE_CODE_WRITE', 'RESPONSE_CODE', 'WRITE', '등록/수정', 'POST', '/adm/api/response-codes/**', 20, 'Y', 'SYSTEM', 'SYSTEM'),
    ('CONFIG_READ', 'CONFIG', 'READ', '조회', 'GET', '/adm/api/configs/**', 10, 'Y', 'SYSTEM', 'SYSTEM'),
    ('CONFIG_WRITE', 'CONFIG', 'WRITE', '수정', 'POST', '/adm/api/configs/**', 20, 'Y', 'SYSTEM', 'SYSTEM'),
    ('DYNAMIC_LOG_READ', 'DYNAMIC_LOG', 'READ', '조회', 'GET', '/adm/api/log-level/**', 10, 'Y', 'SYSTEM', 'SYSTEM'),
    ('DYNAMIC_LOG_WRITE', 'DYNAMIC_LOG', 'WRITE', '적용', 'POST', '/adm/api/log-level/**', 20, 'Y', 'SYSTEM', 'SYSTEM'),
    ('LOG_POLICY_READ', 'LOG_POLICY', 'READ', '조회', 'GET', '/adm/api/log-policies/**', 10, 'Y', 'SYSTEM', 'SYSTEM'),
    ('LOG_POLICY_WRITE', 'LOG_POLICY', 'WRITE', '등록/수정', 'POST', '/adm/api/log-policies/**', 20, 'Y', 'SYSTEM', 'SYSTEM'),
    ('LOG_POLICY_OVERRIDE', 'LOG_POLICY', 'OVERRIDE', '임시 override', 'POST', '/adm/api/log-policies/overrides', 30, 'Y', 'SYSTEM', 'SYSTEM'),
    ('LOG_POLICY_CACHE_REFRESH', 'LOG_POLICY', 'CACHE_REFRESH', '정책 캐시 새로고침', 'POST', '/adm/api/log-policies/cache/refresh', 40, 'Y', 'SYSTEM', 'SYSTEM'),
    ('LOG_POLICY_CACHE_CLEAR', 'LOG_POLICY', 'CACHE_CLEAR', '정책 캐시 전체 삭제', 'POST', '/adm/api/log-policies/cache/clear', 50, 'Y', 'SYSTEM', 'SYSTEM'),
    ('PASSWORD_READ', 'PASSWORD', 'READ', '정책 조회', 'GET', '/adm/api/operators/password-policy/**', 10, 'Y', 'SYSTEM', 'SYSTEM'),
    ('PASSWORD_RESET', 'PASSWORD', 'RESET_PASSWORD', '비밀번호 초기화', 'POST', '/adm/api/operators/*/password/reset', 20, 'Y', 'SYSTEM', 'SYSTEM'),
    ('PASSWORD_UNLOCK', 'PASSWORD', 'UNLOCK', '잠금 해제', 'POST', '/adm/api/operators/*/unlock', 30, 'Y', 'SYSTEM', 'SYSTEM'),
    ('PASSWORD_SESSION_REVOKE', 'PASSWORD', 'REVOKE_SESSION', '세션 강제 종료', 'POST', '/adm/api/operators/sessions/*/revoke', 40, 'Y', 'SYSTEM', 'SYSTEM'),
    ('SECURITY_READ', 'SECURITY', 'READ', '조회', 'GET', '/adm/api/security/**', 10, 'Y', 'SYSTEM', 'SYSTEM'),
    ('SECURITY_WRITE', 'SECURITY', 'WRITE', '보안 설정 변경', 'POST', '/adm/api/security/**', 20, 'Y', 'SYSTEM', 'SYSTEM'),
    ('PERMISSION_READ', 'PERMISSION', 'READ', '조회', 'GET', '/adm/api/permissions/**', 10, 'Y', 'SYSTEM', 'SYSTEM'),
    ('PERMISSION_WRITE', 'PERMISSION', 'WRITE', '권한 변경', 'POST', '/adm/api/permissions/**', 20, 'Y', 'SYSTEM', 'SYSTEM'),
    ('OPERATOR_READ', 'OPERATOR', 'READ', '조회', 'GET', '/adm/api/operators/**', 10, 'Y', 'SYSTEM', 'SYSTEM'),
    ('OPERATOR_CREATE', 'OPERATOR', 'CREATE', '운영자 등록', 'POST', '/adm/api/operators', 20, 'Y', 'SYSTEM', 'SYSTEM'),
    ('OPERATOR_ROLE_UPDATE', 'OPERATOR', 'ROLE_UPDATE', '역할 부여', 'PUT', '/adm/api/operators/*/roles', 30, 'Y', 'SYSTEM', 'SYSTEM')
ON DUPLICATE KEY UPDATE
    MENU_ID = VALUES(MENU_ID),
    ACTION_CODE = VALUES(ACTION_CODE),
    BUTTON_NAME = VALUES(BUTTON_NAME),
    HTTP_METHOD = VALUES(HTTP_METHOD),
    API_PATTERN = VALUES(API_PATTERN),
    SORT_ORDER = VALUES(SORT_ORDER),
    USE_YN = VALUES(USE_YN),
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO adm_password_policy (
    POLICY_ID, MIN_LENGTH, REQUIRE_UPPER_YN, REQUIRE_LOWER_YN, REQUIRE_DIGIT_YN,
    REQUIRE_SPECIAL_YN, MAX_FAIL_COUNT, EXPIRE_DAYS, HISTORY_LIMIT, USE_YN, created_by, updated_by
) VALUES (
    'DEFAULT', 12, 'Y', 'Y', 'Y', 'Y', 5, 90, 5, 'Y', 'SYSTEM', 'SYSTEM'
)
ON DUPLICATE KEY UPDATE
    MIN_LENGTH = VALUES(MIN_LENGTH),
    REQUIRE_UPPER_YN = VALUES(REQUIRE_UPPER_YN),
    REQUIRE_LOWER_YN = VALUES(REQUIRE_LOWER_YN),
    REQUIRE_DIGIT_YN = VALUES(REQUIRE_DIGIT_YN),
    REQUIRE_SPECIAL_YN = VALUES(REQUIRE_SPECIAL_YN),
    MAX_FAIL_COUNT = VALUES(MAX_FAIL_COUNT),
    EXPIRE_DAYS = VALUES(EXPIRE_DAYS),
    HISTORY_LIMIT = VALUES(HISTORY_LIMIT),
    USE_YN = VALUES(USE_YN),
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP;

-- 최초 운영자는 CPF_ADM_BOOTSTRAP_* 환경변수를 명시한 애플리케이션 bootstrap에서만 생성합니다.
-- SQL seed에는 재사용 가능한 비밀번호 해시나 자동 활성 관리자 계정을 두지 않습니다.

INSERT INTO adm_role_menu (ROLE_ID, MENU_ID, READ_YN, WRITE_YN, DELETE_YN, created_by, updated_by)
SELECT 'ADM_ADMIN', MENU_ID, 'Y', 'Y', 'Y', 'SYSTEM', 'SYSTEM'
FROM adm_menu
ON DUPLICATE KEY UPDATE
    READ_YN = VALUES(READ_YN),
    WRITE_YN = VALUES(WRITE_YN),
    DELETE_YN = VALUES(DELETE_YN),
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO adm_role_menu (ROLE_ID, MENU_ID, READ_YN, WRITE_YN, DELETE_YN, created_by, updated_by)
SELECT 'ADM_DEV_OPERATOR', MENU_ID, 'Y',
       CASE WHEN MENU_ID IN ('TRANSACTION_META', 'CHANNEL_POLICY', 'REMOTE_LOG', 'BATCH', 'RELIABILITY', 'NOTIFICATION', 'DOWNLOAD', 'CACHE', 'MESSAGE', 'CODE', 'RESPONSE_CODE', 'CONFIG', 'DYNAMIC_LOG', 'LOG_POLICY') THEN 'Y' ELSE 'N' END,
       CASE WHEN MENU_ID IN ('TRANSACTION_META', 'MESSAGE', 'CODE', 'DYNAMIC_LOG', 'LOG_POLICY') THEN 'Y' ELSE 'N' END,
       'SYSTEM', 'SYSTEM'
FROM adm_menu
WHERE MENU_ID NOT IN ('OPERATOR', 'PERMISSION', 'PASSWORD', 'SECURITY')
ON DUPLICATE KEY UPDATE
    READ_YN = VALUES(READ_YN),
    WRITE_YN = VALUES(WRITE_YN),
    DELETE_YN = VALUES(DELETE_YN),
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO adm_role_menu (ROLE_ID, MENU_ID, READ_YN, WRITE_YN, DELETE_YN, created_by, updated_by)
SELECT 'ADM_BIZ_OPERATOR', MENU_ID, 'Y',
       CASE WHEN MENU_ID IN ('MEMBER', 'BATCH', 'DOWNLOAD', 'CACHE') THEN 'Y' ELSE 'N' END,
       CASE WHEN MENU_ID = 'MEMBER' THEN 'Y' ELSE 'N' END,
       'SYSTEM', 'SYSTEM'
FROM adm_menu
WHERE MENU_ID IN ('DASHBOARD', 'LOG_LIST', 'STANDARD_EXECUTION', 'CHANNEL_POLICY', 'REMOTE_LOG', 'TRANSACTION_META', 'AUDIT_LOG', 'MEMBER', 'BATCH', 'RELIABILITY', 'NOTIFICATION', 'DOWNLOAD', 'CACHE', 'MESSAGE', 'CODE')
ON DUPLICATE KEY UPDATE
    READ_YN = VALUES(READ_YN),
    WRITE_YN = VALUES(WRITE_YN),
    DELETE_YN = VALUES(DELETE_YN),
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO adm_role_menu (ROLE_ID, MENU_ID, READ_YN, WRITE_YN, DELETE_YN, created_by, updated_by)
SELECT 'ADM_VIEWER', MENU_ID, 'Y', 'N', 'N', 'SYSTEM', 'SYSTEM'
FROM adm_menu
WHERE MENU_ID IN ('DASHBOARD', 'LOG_LIST', 'STANDARD_EXECUTION', 'CHANNEL_POLICY', 'REMOTE_LOG', 'TRANSACTION_META', 'AUDIT_LOG', 'MEMBER', 'BATCH', 'RELIABILITY', 'NOTIFICATION', 'DOWNLOAD', 'CACHE', 'MESSAGE', 'CODE', 'RESPONSE_CODE', 'CONFIG', 'LOG_POLICY')
ON DUPLICATE KEY UPDATE
    READ_YN = VALUES(READ_YN),
    WRITE_YN = VALUES(WRITE_YN),
    DELETE_YN = VALUES(DELETE_YN),
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO adm_role_menu (ROLE_ID, MENU_ID, READ_YN, WRITE_YN, DELETE_YN, created_by, updated_by)
SELECT 'ADM_OPERATOR', MENU_ID, READ_YN, WRITE_YN, DELETE_YN, 'SYSTEM', 'SYSTEM'
FROM adm_role_menu
WHERE ROLE_ID = 'ADM_DEV_OPERATOR'
ON DUPLICATE KEY UPDATE
    READ_YN = VALUES(READ_YN),
    WRITE_YN = VALUES(WRITE_YN),
    DELETE_YN = VALUES(DELETE_YN),
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO adm_role_button (ROLE_ID, BUTTON_ID, ALLOW_YN, created_by, updated_by)
SELECT 'ADM_ADMIN', BUTTON_ID, 'Y', 'SYSTEM', 'SYSTEM'
FROM adm_button
ON DUPLICATE KEY UPDATE
    ALLOW_YN = VALUES(ALLOW_YN),
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO adm_role_button (ROLE_ID, BUTTON_ID, ALLOW_YN, created_by, updated_by)
SELECT 'ADM_DEV_OPERATOR', BUTTON_ID,
       CASE WHEN MENU_ID IN ('OPERATOR', 'PERMISSION', 'PASSWORD', 'SECURITY') THEN 'N' ELSE 'Y' END,
       'SYSTEM', 'SYSTEM'
FROM adm_button
ON DUPLICATE KEY UPDATE
    ALLOW_YN = VALUES(ALLOW_YN),
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO adm_role_button (ROLE_ID, BUTTON_ID, ALLOW_YN, created_by, updated_by)
SELECT 'ADM_BIZ_OPERATOR', BUTTON_ID,
       CASE
           WHEN BUTTON_ID IN ('MEMBER_CREATE', 'MEMBER_UPDATE', 'MEMBER_STATUS', 'MEMBER_ROLE_GRANT', 'MEMBER_ROLE_REVOKE', 'BATCH_EXECUTE', 'BATCH_RETRY', 'BATCH_SIMULATION', 'BATCH_RELATION_READ', 'BATCH_TARGET_READ', 'BATCH_SCHEDULER_RUN', 'DOWNLOAD_EXECUTE', 'CACHE_REFRESH') THEN 'Y'
           WHEN ACTION_CODE IN ('READ', 'DETAIL') AND MENU_ID IN ('LOG_LIST', 'REMOTE_LOG', 'TRANSACTION_META', 'AUDIT_LOG', 'MEMBER', 'BATCH', 'RELIABILITY', 'NOTIFICATION', 'DOWNLOAD', 'CACHE', 'MESSAGE', 'CODE', 'LOG_POLICY') THEN 'Y'
           ELSE 'N'
       END,
       'SYSTEM', 'SYSTEM'
FROM adm_button
ON DUPLICATE KEY UPDATE
    ALLOW_YN = VALUES(ALLOW_YN),
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO adm_role_button (ROLE_ID, BUTTON_ID, ALLOW_YN, created_by, updated_by)
SELECT 'ADM_VIEWER', BUTTON_ID,
       CASE WHEN ACTION_CODE IN ('READ', 'DETAIL') THEN 'Y' ELSE 'N' END,
       'SYSTEM', 'SYSTEM'
FROM adm_button
ON DUPLICATE KEY UPDATE
    ALLOW_YN = VALUES(ALLOW_YN),
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO adm_role_button (ROLE_ID, BUTTON_ID, ALLOW_YN, created_by, updated_by)
SELECT 'ADM_OPERATOR', BUTTON_ID, ALLOW_YN, 'SYSTEM', 'SYSTEM'
FROM adm_role_button
WHERE ROLE_ID = 'ADM_DEV_OPERATOR'
ON DUPLICATE KEY UPDATE
    ALLOW_YN = VALUES(ALLOW_YN),
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP;

-- ADM API 권한은 버튼/행위 권한을 실제 Controller path와 연결하기 위한 서버 권한검사 메타입니다.
INSERT INTO adm_api_permission (
    API_PERMISSION_ID,
    API_GROUP_CODE,
    HTTP_METHOD,
    API_PATH,
    API_NAME,
    PERMISSION_CODE,
    MENU_ID,
    BUTTON_ID,
    USE_YN,
    created_by,
    updated_by
)
SELECT
    CONCAT('API_', BUTTON_ID),
    MENU_ID,
    COALESCE(HTTP_METHOD, 'ANY'),
    API_PATTERN,
    BUTTON_NAME,
    ACTION_CODE,
    MENU_ID,
    BUTTON_ID,
    USE_YN,
    'SYSTEM',
    'SYSTEM'
FROM adm_button
WHERE API_PATTERN IS NOT NULL
ON DUPLICATE KEY UPDATE
    API_GROUP_CODE = VALUES(API_GROUP_CODE),
    HTTP_METHOD = VALUES(HTTP_METHOD),
    API_PATH = VALUES(API_PATH),
    API_NAME = VALUES(API_NAME),
    PERMISSION_CODE = VALUES(PERMISSION_CODE),
    MENU_ID = VALUES(MENU_ID),
    BUTTON_ID = VALUES(BUTTON_ID),
    USE_YN = VALUES(USE_YN),
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO adm_api_permission (
    API_PERMISSION_ID, API_GROUP_CODE, HTTP_METHOD, API_PATH, API_NAME, PERMISSION_CODE,
    MENU_ID, BUTTON_ID, USE_YN, created_by, updated_by
) VALUES (
    'API_PERMISSION_WRITE_PUT', 'PERMISSION', 'PUT', '/adm/api/permissions/**', '권한 변경', 'WRITE',
    'PERMISSION', 'PERMISSION_WRITE', 'Y', 'SYSTEM', 'SYSTEM'
)
ON DUPLICATE KEY UPDATE
    API_GROUP_CODE = VALUES(API_GROUP_CODE),
    HTTP_METHOD = VALUES(HTTP_METHOD),
    API_PATH = VALUES(API_PATH),
    API_NAME = VALUES(API_NAME),
    PERMISSION_CODE = VALUES(PERMISSION_CODE),
    MENU_ID = VALUES(MENU_ID),
    BUTTON_ID = VALUES(BUTTON_ID),
    USE_YN = VALUES(USE_YN),
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO adm_role_api_permission (ROLE_ID, API_PERMISSION_ID, ALLOW_YN, created_by, updated_by)
SELECT rb.ROLE_ID, ap.API_PERMISSION_ID, rb.ALLOW_YN, 'SYSTEM', 'SYSTEM'
FROM adm_role_button rb
JOIN adm_api_permission ap ON ap.BUTTON_ID = rb.BUTTON_ID
ON DUPLICATE KEY UPDATE
    ALLOW_YN = VALUES(ALLOW_YN),
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO adm_ip_allowlist (IP_PATTERN, DESCRIPTION, USE_YN, created_by, updated_by)
VALUES ('127.0.0.1', '로컬 개발 PC', 'Y', 'SYSTEM', 'SYSTEM')
ON DUPLICATE KEY UPDATE
    DESCRIPTION = VALUES(DESCRIPTION),
    USE_YN = VALUES(USE_YN),
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO adm_audit_log (
    TRANSACTION_ID,
    TRACE_ID,
    OPERATOR_ID,
    MENU_ID,
    ACTION_TYPE,
    TARGET_TYPE,
    TARGET_ID,
    REASON,
    REQUEST_BODY,
    CLIENT_IP,
    created_by,
    updated_by
) SELECT
    'INITIAL_SQL_SEED',
    'INITIAL_SQL_SEED',
    'admin',
    'DASHBOARD',
    'SEED',
    'ADM',
    'INITIAL_DATA',
    'ADM 초기 데이터 등록',
    NULL,
    '127.0.0.1',
    'SYSTEM',
    'SYSTEM'
WHERE NOT EXISTS (
    SELECT 1
    FROM adm_audit_log
    WHERE TRANSACTION_ID = 'INITIAL_SQL_SEED'
      AND OPERATOR_ID = 'admin'
      AND ACTION_TYPE = 'SEED'
      AND TARGET_TYPE = 'ADM'
      AND TARGET_ID = 'INITIAL_DATA'
);

-- Product Seed의 마지막 단계까지 성공한 CPF 소유 Schema만 공식 Baseline으로 기록합니다.
INSERT INTO cpfDB.cpf_schema_installation (
    schema_name,
    system_code,
    database_vendor,
    product_version,
    baseline_key,
    install_state,
    created_by,
    updated_by
) VALUES
    ('cpfDB', 'CPF', 'MARIADB', '1.0.0-SNAPSHOT', 'CPF_MARIADB_EMPTY_INSTALL_V1', 'PRODUCT_SEEDED', 'CPF_INSTALLER', 'CPF_INSTALLER'),
    ('cmnDB', 'CMN', 'MARIADB', '1.0.0-SNAPSHOT', 'CPF_MARIADB_EMPTY_INSTALL_V1', 'PRODUCT_SEEDED', 'CPF_INSTALLER', 'CPF_INSTALLER'),
    ('admDB', 'ADM', 'MARIADB', '1.0.0-SNAPSHOT', 'CPF_MARIADB_EMPTY_INSTALL_V1', 'PRODUCT_SEEDED', 'CPF_INSTALLER', 'CPF_INSTALLER'),
    ('bzaDB', 'BZA', 'MARIADB', '1.0.0-SNAPSHOT', 'CPF_MARIADB_EMPTY_INSTALL_V1', 'PRODUCT_SEEDED', 'CPF_INSTALLER', 'CPF_INSTALLER'),
    ('batDB', 'BAT', 'MARIADB', '1.0.0-SNAPSHOT', 'CPF_MARIADB_EMPTY_INSTALL_V1', 'PRODUCT_SEEDED', 'CPF_INSTALLER', 'CPF_INSTALLER'),
    ('mbrDB', 'MBR', 'MARIADB', '1.0.0-SNAPSHOT', 'CPF_MARIADB_EMPTY_INSTALL_V1', 'PRODUCT_SEEDED', 'CPF_INSTALLER', 'CPF_INSTALLER'),
    ('accDB', 'ACC', 'MARIADB', '1.0.0-SNAPSHOT', 'CPF_MARIADB_EMPTY_INSTALL_V1', 'PRODUCT_SEEDED', 'CPF_INSTALLER', 'CPF_INSTALLER'),
    ('refDB', 'REF', 'MARIADB', '1.0.0-SNAPSHOT', 'CPF_MARIADB_EMPTY_INSTALL_V1', 'PRODUCT_SEEDED', 'CPF_INSTALLER', 'CPF_INSTALLER'),
    ('exsDB', 'EXS', 'MARIADB', '1.0.0-SNAPSHOT', 'CPF_MARIADB_EMPTY_INSTALL_V1', 'PRODUCT_SEEDED', 'CPF_INSTALLER', 'CPF_INSTALLER')
ON DUPLICATE KEY UPDATE
    system_code = VALUES(system_code),
    database_vendor = VALUES(database_vendor),
    product_version = VALUES(product_version),
    baseline_key = VALUES(baseline_key),
    install_state = VALUES(install_state),
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP(3);
