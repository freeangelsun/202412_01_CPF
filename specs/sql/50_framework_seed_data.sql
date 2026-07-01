-- CPF 프레임워크 초기 코드, 메시지, 응답코드, 설정 데이터입니다.
-- 대상 DB: pfwDB

USE pfwDB;

INSERT INTO pfw_code (parent_id, code_key, code_value, description, created_by, updated_by)
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

INSERT INTO pfw_code (parent_id, code_key, code_value, description, created_by, updated_by)
VALUES
    ((SELECT code_id FROM (SELECT code_id FROM pfw_code WHERE code_key = 'CODE_GROUP' AND code_value = 'MODULE') p), 'MODULE', 'PFW', '프레임워크 공통 엔진', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM pfw_code WHERE code_key = 'CODE_GROUP' AND code_value = 'MODULE') p), 'MODULE', 'CMN', '업무 공통 라이브러리', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM pfw_code WHERE code_key = 'CODE_GROUP' AND code_value = 'MODULE') p), 'MODULE', 'ADM', '관리자 운영 서비스', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM pfw_code WHERE code_key = 'CODE_GROUP' AND code_value = 'MODULE') p), 'MODULE', 'ACC', '계정 샘플 서비스', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM pfw_code WHERE code_key = 'CODE_GROUP' AND code_value = 'MODULE') p), 'MODULE', 'MBR', '회원 샘플 서비스', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM pfw_code WHERE code_key = 'CODE_GROUP' AND code_value = 'MODULE') p), 'MODULE', 'XYZ', '교육 샘플 서비스', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM pfw_code WHERE code_key = 'CODE_GROUP' AND code_value = 'REQUEST_TYPE') p), 'REQUEST_TYPE', 'NORMAL', '일반 요청', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM pfw_code WHERE code_key = 'CODE_GROUP' AND code_value = 'REQUEST_TYPE') p), 'REQUEST_TYPE', 'COMPENSATION', '보상 요청', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM pfw_code WHERE code_key = 'CODE_GROUP' AND code_value = 'REQUEST_TYPE') p), 'REQUEST_TYPE', 'RETRY', '재시도 요청', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM pfw_code WHERE code_key = 'CODE_GROUP' AND code_value = 'CHANNEL_CODE') p), 'CHANNEL_CODE', 'WEB', '웹 채널', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM pfw_code WHERE code_key = 'CODE_GROUP' AND code_value = 'CHANNEL_CODE') p), 'CHANNEL_CODE', 'MOBILE', '모바일 채널', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM pfw_code WHERE code_key = 'CODE_GROUP' AND code_value = 'CHANNEL_CODE') p), 'CHANNEL_CODE', 'BATCH', '배치 채널', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM pfw_code WHERE code_key = 'CODE_GROUP' AND code_value = 'CHANNEL_CODE') p), 'CHANNEL_CODE', 'ADM', '관리자 채널', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM pfw_code WHERE code_key = 'CODE_GROUP' AND code_value = 'RESULT_TYPE') p), 'RESULT_TYPE', 'S', '성공', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM pfw_code WHERE code_key = 'CODE_GROUP' AND code_value = 'RESULT_TYPE') p), 'RESULT_TYPE', 'E', '오류', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM pfw_code WHERE code_key = 'CODE_GROUP' AND code_value = 'MESSAGE_FORMAT_TYPE') p), 'MESSAGE_FORMAT_TYPE', 'FIXED', '고정 메시지', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM pfw_code WHERE code_key = 'CODE_GROUP' AND code_value = 'MESSAGE_FORMAT_TYPE') p), 'MESSAGE_FORMAT_TYPE', 'INDEXED', '인덱스 파라미터 메시지', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM pfw_code WHERE code_key = 'CODE_GROUP' AND code_value = 'LOG_LEVEL') p), 'LOG_LEVEL', 'TRACE', 'TRACE 로그', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM pfw_code WHERE code_key = 'CODE_GROUP' AND code_value = 'LOG_LEVEL') p), 'LOG_LEVEL', 'DEBUG', 'DEBUG 로그', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM pfw_code WHERE code_key = 'CODE_GROUP' AND code_value = 'LOG_LEVEL') p), 'LOG_LEVEL', 'INFO', 'INFO 로그', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM pfw_code WHERE code_key = 'CODE_GROUP' AND code_value = 'LOG_LEVEL') p), 'LOG_LEVEL', 'WARN', 'WARN 로그', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM pfw_code WHERE code_key = 'CODE_GROUP' AND code_value = 'LOG_LEVEL') p), 'LOG_LEVEL', 'ERROR', 'ERROR 로그', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM pfw_code WHERE code_key = 'CODE_GROUP' AND code_value = 'CACHE_NAME') p), 'CACHE_NAME', 'ALL', '전체 캐시', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM pfw_code WHERE code_key = 'CODE_GROUP' AND code_value = 'CACHE_NAME') p), 'CACHE_NAME', 'CODE', '코드 캐시', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM pfw_code WHERE code_key = 'CODE_GROUP' AND code_value = 'CACHE_NAME') p), 'CACHE_NAME', 'MESSAGE', '메시지 캐시', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM pfw_code WHERE code_key = 'CODE_GROUP' AND code_value = 'CACHE_NAME') p), 'CACHE_NAME', 'RESPONSE_CODE', '응답코드 캐시', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM pfw_code WHERE code_key = 'CODE_GROUP' AND code_value = 'CACHE_NAME') p), 'CACHE_NAME', 'CONFIG', '설정 캐시', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM pfw_code WHERE code_key = 'CODE_GROUP' AND code_value = 'BATCH_JOB_TYPE') p), 'BATCH_JOB_TYPE', 'TASKLET', 'Tasklet 배치', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM pfw_code WHERE code_key = 'CODE_GROUP' AND code_value = 'BATCH_JOB_TYPE') p), 'BATCH_JOB_TYPE', 'CHUNK', 'Chunk 배치', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM pfw_code WHERE code_key = 'CODE_GROUP' AND code_value = 'BATCH_JOB_TYPE') p), 'BATCH_JOB_TYPE', 'RETRY', '재처리 배치', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM pfw_code WHERE code_key = 'CODE_GROUP' AND code_value = 'YN') p), 'YN', 'Y', '예', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM pfw_code WHERE code_key = 'CODE_GROUP' AND code_value = 'YN') p), 'YN', 'N', '아니오', 'SYSTEM', 'SYSTEM')
ON DUPLICATE KEY UPDATE
    parent_id = VALUES(parent_id),
    description = VALUES(description),
    updated_by = VALUES(updated_by),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO pfw_message (
    message_code, locale, message_format_type, external_message, internal_message,
    parameter_count, parameter_sample, description, created_by, updated_by
) VALUES
    ('MPFW000000', 'ko', 'FIXED', '정상 처리되었습니다.', 'PFW 공통 요청이 정상 처리되었습니다.', 0, NULL, 'PFW 공통 성공 메시지', 'SYSTEM', 'SYSTEM'),
    ('MPFW010001', 'ko', 'INDEXED', '요청 값이 올바르지 않습니다.', '요청 파라미터 검증에 실패했습니다. field={0}, value={1}', 2, '["memberId","abc"]', 'PFW 파라미터 오류 메시지', 'SYSTEM', 'SYSTEM'),
    ('MPFW010002', 'ko', 'INDEXED', '요청한 정보를 찾을 수 없습니다.', '조회 대상 데이터가 존재하지 않습니다. target={0}', 1, '["member"]', 'PFW 미존재 메시지', 'SYSTEM', 'SYSTEM'),
    ('MPFW010003', 'ko', 'INDEXED', '이미 등록된 정보입니다.', '중복 데이터가 감지되었습니다. key={0}', 1, '["memberNo"]', 'PFW 중복 메시지', 'SYSTEM', 'SYSTEM'),
    ('MPFW010004', 'ko', 'INDEXED', '입력값을 확인해 주세요.', 'Bean Validation 검증에 실패했습니다. field={0}', 1, '["name"]', 'PFW 검증 메시지', 'SYSTEM', 'SYSTEM'),
    ('MPFW010005', 'ko', 'FIXED', '인증이 필요합니다.', '인증되지 않은 요청입니다.', 0, NULL, 'PFW 인증 필요 메시지', 'SYSTEM', 'SYSTEM'),
    ('MPFW010006', 'ko', 'INDEXED', '처리 권한이 없습니다.', '인가되지 않은 요청입니다. user={0}', 1, '["guest"]', 'PFW 권한 오류 메시지', 'SYSTEM', 'SYSTEM'),
    ('MPFW020001', 'ko', 'INDEXED', '요청을 처리할 수 없습니다.', '업무 규칙 위반이 발생했습니다. rule={0}', 1, '["business-rule"]', 'PFW 업무 규칙 메시지', 'SYSTEM', 'SYSTEM'),
    ('MPFW030001', 'ko', 'INDEXED', '일시적으로 처리할 수 없습니다.', '외부 또는 타 주제영역 연계 오류가 발생했습니다. service={0}', 1, '["mbr"]', 'PFW 외부 연계 메시지', 'SYSTEM', 'SYSTEM'),
    ('MPFW900001', 'ko', 'INDEXED', '필수 거래 헤더가 누락되었습니다.', 'PFW 거래 헤더 검증에 실패했습니다. header={0}, uri={1}', 2, '["X-Request-Type","/mbr/list"]', 'PFW 헤더 검증 메시지', 'SYSTEM', 'SYSTEM'),
    ('MPFW900002', 'ko', 'INDEXED', '거래 메타데이터 설정이 올바르지 않습니다.', 'PFW @CpfTransaction 메타데이터 검증에 실패했습니다. transactionId={0}', 1, '["MBR01BSE0001"]', 'PFW 메타데이터 메시지', 'SYSTEM', 'SYSTEM'),
    ('MPFW900003', 'ko', 'INDEXED', '서비스 접속 정보가 없습니다.', 'PFW 서비스 endpoint 설정을 찾을 수 없습니다. serviceId={0}', 1, '["mbr"]', 'PFW endpoint 메시지', 'SYSTEM', 'SYSTEM'),
    ('MPFW900004', 'ko', 'INDEXED', '동적 로그레벨 요청이 올바르지 않습니다.', 'PFW 동적 로그레벨 규칙 검증에 실패했습니다. reason={0}', 1, '["transactionId or businessTransactionId required"]', 'PFW 동적 로그 메시지', 'SYSTEM', 'SYSTEM'),
    ('MPFW990000', 'ko', 'INDEXED', '처리 중 오류가 발생했습니다.', 'PFW 내부 오류가 발생했습니다. error={0}', 1, '["Exception"]', 'PFW 내부 오류 메시지', 'SYSTEM', 'SYSTEM'),
    ('MPFW990001', 'ko', 'INDEXED', '데이터베이스 오류가 발생했습니다.', '데이터베이스 처리 오류가 발생했습니다. sqlState={0}', 1, '["HY000"]', 'PFW 데이터베이스 오류 메시지', 'SYSTEM', 'SYSTEM'),
    ('MACC000000', 'ko', 'FIXED', '성공', 'ACC 요청이 정상 처리되었습니다.', 0, NULL, 'ACC 성공 메시지', 'SYSTEM', 'SYSTEM'),
    ('MACC010001', 'ko', 'INDEXED', '계정 요청 값이 올바르지 않습니다.', 'ACC 파라미터 검증에 실패했습니다. field={0}', 1, '["accountId"]', 'ACC 파라미터 오류 메시지', 'SYSTEM', 'SYSTEM'),
    ('MACC010002', 'ko', 'INDEXED', '계정 정보를 찾을 수 없습니다.', 'ACC 조회 대상이 없습니다. target={0}', 1, '["account"]', 'ACC 미존재 메시지', 'SYSTEM', 'SYSTEM'),
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
    ('MXYZ090001', 'ko', 'INDEXED', '이미 등록된 {0}입니다.', '{0}={1} 값이 이미 존재합니다. duplicateCheck=XYZ_EDU_SAMPLE', 2, '["회원번호","M0001"]', 'XYZ 동적 중복 교육 메시지', 'SYSTEM', 'SYSTEM'),
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

INSERT INTO pfw_response_code (
    response_code, message_code, result_type, module_id, response_group, sequence_no,
    http_status, description, created_by, updated_by
) VALUES
    ('SPFW000000', 'MPFW000000', 'S', 'PFW', '00', '0000', 200, 'PFW 공통 성공', 'SYSTEM', 'SYSTEM'),
    ('EPFW010001', 'MPFW010001', 'E', 'PFW', '01', '0001', 400, '파라미터 오류', 'SYSTEM', 'SYSTEM'),
    ('EPFW010002', 'MPFW010002', 'E', 'PFW', '01', '0002', 404, '미존재 오류', 'SYSTEM', 'SYSTEM'),
    ('EPFW010003', 'MPFW010003', 'E', 'PFW', '01', '0003', 409, '중복 오류', 'SYSTEM', 'SYSTEM'),
    ('EPFW010004', 'MPFW010004', 'E', 'PFW', '01', '0004', 400, '검증 실패', 'SYSTEM', 'SYSTEM'),
    ('EPFW010005', 'MPFW010005', 'E', 'PFW', '01', '0005', 401, '인증 필요', 'SYSTEM', 'SYSTEM'),
    ('EPFW010006', 'MPFW010006', 'E', 'PFW', '01', '0006', 403, '권한 없음', 'SYSTEM', 'SYSTEM'),
    ('EPFW020001', 'MPFW020001', 'E', 'PFW', '02', '0001', 400, '업무 규칙 위반', 'SYSTEM', 'SYSTEM'),
    ('EPFW030001', 'MPFW030001', 'E', 'PFW', '03', '0001', 502, '외부 연계 오류', 'SYSTEM', 'SYSTEM'),
    ('EPFW900001', 'MPFW900001', 'E', 'PFW', '90', '0001', 400, '필수 거래 헤더 누락', 'SYSTEM', 'SYSTEM'),
    ('EPFW900002', 'MPFW900002', 'E', 'PFW', '90', '0002', 500, '거래 메타데이터 오류', 'SYSTEM', 'SYSTEM'),
    ('EPFW900003', 'MPFW900003', 'E', 'PFW', '90', '0003', 500, '서비스 endpoint 미등록', 'SYSTEM', 'SYSTEM'),
    ('EPFW900004', 'MPFW900004', 'E', 'PFW', '90', '0004', 400, '동적 로그 규칙 오류', 'SYSTEM', 'SYSTEM'),
    ('EPFW990000', 'MPFW990000', 'E', 'PFW', '99', '0000', 500, '내부 서버 오류', 'SYSTEM', 'SYSTEM'),
    ('EPFW990001', 'MPFW990001', 'E', 'PFW', '99', '0001', 500, '데이터베이스 오류', 'SYSTEM', 'SYSTEM'),
    ('SACC000000', 'MACC000000', 'S', 'ACC', '00', '0000', 200, 'ACC 성공', 'SYSTEM', 'SYSTEM'),
    ('EACC010001', 'MACC010001', 'E', 'ACC', '01', '0001', 400, 'ACC 파라미터 오류', 'SYSTEM', 'SYSTEM'),
    ('EACC010002', 'MACC010002', 'E', 'ACC', '01', '0002', 404, 'ACC 미존재', 'SYSTEM', 'SYSTEM'),
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

INSERT INTO pfw_config (config_key, config_value, config_type, description, encrypted_yn, created_by, updated_by)
VALUES
    ('CPF.CMN.CACHE.PRELOAD_ENABLED', 'Y', 'BOOLEAN', 'CMN 캐시 기동 시 선적재 여부', 'N', 'SYSTEM', 'SYSTEM'),
    ('CPF.CMN.CACHE.FAIL_FAST_ON_STARTUP', 'N', 'BOOLEAN', '캐시 선적재 실패 시 기동 실패 여부', 'N', 'SYSTEM', 'SYSTEM'),
    ('CPF.CMN.CACHE.REFRESH_POLL_MILLIS', '5000', 'NUMBER', '캐시 갱신 이벤트 polling 주기', 'N', 'SYSTEM', 'SYSTEM'),
    ('CPF.CMN.MESSAGING.BROKER', 'IN_MEMORY', 'STRING', '기본 CMN 메시지 브로커 유형', 'N', 'SYSTEM', 'SYSTEM'),
    ('CPF.HTTP.CONNECT_TIMEOUT_MS', '3000', 'NUMBER', 'PFW HTTP client 연결 timeout', 'N', 'SYSTEM', 'SYSTEM'),
    ('CPF.HTTP.READ_TIMEOUT_MS', '5000', 'NUMBER', 'PFW HTTP client 읽기 timeout', 'N', 'SYSTEM', 'SYSTEM'),
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

INSERT INTO pfw_log_policy (
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

INSERT INTO pfw_security_jwt_key (
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

INSERT INTO pfw_cache_refresh_event (
    cache_name, event_type, event_key, source_was_id, published_by, created_by, updated_by
)
SELECT 'ALL', 'INITIAL_LOAD', 'INITIAL_FRAMEWORK_SEED', 'SQL', 'SYSTEM', 'SYSTEM', 'SYSTEM'
WHERE NOT EXISTS (
    SELECT 1
    FROM pfw_cache_refresh_event
    WHERE cache_name = 'ALL'
      AND event_type = 'INITIAL_LOAD'
      AND event_key = 'INITIAL_FRAMEWORK_SEED'
);

INSERT INTO BATCH_JOB_SEQ (ID)
SELECT 0
WHERE NOT EXISTS (SELECT 1 FROM BATCH_JOB_SEQ);

INSERT INTO BATCH_JOB_EXECUTION_SEQ (ID)
SELECT 0
WHERE NOT EXISTS (SELECT 1 FROM BATCH_JOB_EXECUTION_SEQ);

INSERT INTO BATCH_STEP_EXECUTION_SEQ (ID)
SELECT 0
WHERE NOT EXISTS (SELECT 1 FROM BATCH_STEP_EXECUTION_SEQ);

INSERT INTO pfw_batch_instance (
    instance_id, instance_name, host_name, server_port, active_yn, last_heartbeat_at, description, created_by, updated_by
) VALUES (
    'local-batch-01',
    '로컬 배치 인스턴스',
    'localhost',
    8099,
    'Y',
    NOW(3),
    'XYZ EDU 배치와 ADM 관제 연동을 확인하는 로컬 인스턴스',
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

INSERT INTO pfw_batch_worker (
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

INSERT INTO pfw_batch_job (
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

INSERT INTO pfw_batch_schedule (
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

INSERT INTO pfw_batch_job_relation (
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

INSERT INTO pfw_batch_execution (
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
    '20260615120000000XYZlocal010000001',
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
    FROM pfw_batch_execution
    WHERE job_id = 'CPF_EDU_TASKLET_JOB'
      AND requested_by = 'SYSTEM'
      AND job_parameters = '{"edu":true}'
);

SET @cpf_edu_execution_id = (
    SELECT execution_id
    FROM pfw_batch_execution
    WHERE job_id = 'CPF_EDU_TASKLET_JOB'
      AND requested_by = 'SYSTEM'
      AND job_parameters = '{"edu":true}'
    ORDER BY execution_id
    LIMIT 1
);

INSERT INTO pfw_batch_step_execution (
    execution_id, spring_batch_step_execution_id, worker_id, step_name, execution_status,
    start_time, end_time, read_count, write_count, skip_count, step_log, created_by, updated_by
)
SELECT @cpf_edu_execution_id, NULL, 'local-batch-01', 'CPF_EDU_TASKLET_STEP', 'COMPLETED', DATE_SUB(NOW(3), INTERVAL 10 MINUTE), DATE_SUB(NOW(3), INTERVAL 9 MINUTE), 1, 1, 0, 'Tasklet 교육 실행 정상 완료', 'SYSTEM', 'SYSTEM'
WHERE @cpf_edu_execution_id IS NOT NULL
  AND NOT EXISTS (
      SELECT 1
      FROM pfw_batch_step_execution
      WHERE execution_id = @cpf_edu_execution_id
        AND step_name = 'CPF_EDU_TASKLET_STEP'
  );

INSERT INTO pfw_batch_execution_target (
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
      FROM pfw_batch_execution_target
      WHERE job_id = 'CPF_EDU_TASKLET_JOB'
        AND business_date = CURRENT_DATE
        AND target_instance_id = 'local-batch-01'
  );

INSERT INTO pfw_business_day_calendar (
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

INSERT INTO pfw_notification_rule (
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

INSERT INTO pfw_notification_delivery_log (
    rule_id, event_type, target_type, target_id, receiver, delivery_status, delivery_message, created_by, updated_by
)
SELECT
    rule_id,
    'BATCH_EXECUTION',
    'pfw_batch_execution',
    CAST(@cpf_edu_execution_id AS CHAR),
    'ADM_BATCH_OPERATOR',
    'SKIPPED',
    '로컬 seed 알림 발송 로그 샘플입니다.',
    'SYSTEM',
    'SYSTEM'
FROM pfw_notification_rule
WHERE event_type = 'BATCH_EXECUTION'
  AND event_sub_type = 'FAILED'
  AND @cpf_edu_execution_id IS NOT NULL
  AND NOT EXISTS (
      SELECT 1
      FROM pfw_notification_delivery_log
      WHERE event_type = 'BATCH_EXECUTION'
        AND target_id = CAST(@cpf_edu_execution_id AS CHAR)
        AND receiver = 'ADM_BATCH_OPERATOR'
  )
LIMIT 1;

INSERT INTO pfw_batch_job (
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

INSERT INTO pfw_center_cut_job (
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
    'PFW 표준 center-cut 계약과 BAT 기본 구현체를 검증하는 1차 모수입니다.',
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

INSERT INTO pfw_center_cut_parameter (
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
