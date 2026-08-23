-- GENERATED FILE. DO NOT EDIT.
-- Source: cpf-tools/db/canonical/seed-model.json
-- Vendor: postgresql
-- Role: CPF_PLATFORM_DB

INSERT INTO OPS_SYSTEM_REGISTRY (system_code, system_name, domain_code, enabled_yn, description, policy_version, created_by, updated_by)
VALUES ('CPF', 'CPF Core Platform', 'CPF', 'Y', 'CPF core platform system', 1, 'SYSTEM', 'SYSTEM'),
    ('CMN', 'CPF Common', 'CMN', 'Y', 'CPF mandatory common system', 1, 'SYSTEM', 'SYSTEM'),
    ('ADM', 'CPF Administration', 'ADM', 'Y', 'CPF administration system', 1, 'SYSTEM', 'SYSTEM'),
    ('MBW', 'CPF Backoffice', 'MBW', 'Y', 'CPF business backoffice system', 1, 'SYSTEM', 'SYSTEM'),
    ('BAT', 'CPF Batch', 'BAT', 'Y', 'CPF batch runtime system', 1, 'SYSTEM', 'SYSTEM'),
    ('EDU', 'CPF Education', 'EDU', 'Y', 'CPF education reference system', 1, 'SYSTEM', 'SYSTEM')
ON CONFLICT (system_code) DO UPDATE SET system_name=EXCLUDED.system_name, domain_code=EXCLUDED.domain_code, enabled_yn=EXCLUDED.enabled_yn, description=EXCLUDED.description, policy_version=EXCLUDED.policy_version, updated_by=EXCLUDED.updated_by, updated_at=CURRENT_TIMESTAMP;

INSERT INTO OPS_CHANNEL_REGISTRY (channel_code, channel_name, channel_type, trust_level, client_channel_yn, internal_channel_yn, authentication_required_yn, signature_required_yn, active_yn, description, policy_version, created_by, updated_by)
VALUES ('WEB', '웹', 'CLIENT', 'EXTERNAL', 'Y', 'N', 'Y', 'N', 'Y', '웹 브라우저 채널', 0, 'SYSTEM', 'SYSTEM'),
    ('MOBILE', '모바일', 'CLIENT', 'EXTERNAL', 'Y', 'N', 'Y', 'N', 'Y', '모바일 애플리케이션 채널', 0, 'SYSTEM', 'SYSTEM'),
    ('ADM', '관리자', 'OPERATOR', 'INTERNAL', 'Y', 'Y', 'Y', 'N', 'Y', 'ADM 운영 채널', 0, 'SYSTEM', 'SYSTEM'),
    ('BATCH', '배치', 'SYSTEM', 'INTERNAL', 'N', 'Y', 'N', 'N', 'Y', '배치 실행 채널', 0, 'SYSTEM', 'SYSTEM')
ON CONFLICT (channel_code) DO UPDATE SET channel_name=EXCLUDED.channel_name, channel_type=EXCLUDED.channel_type, trust_level=EXCLUDED.trust_level, client_channel_yn=EXCLUDED.client_channel_yn, internal_channel_yn=EXCLUDED.internal_channel_yn, authentication_required_yn=EXCLUDED.authentication_required_yn, signature_required_yn=EXCLUDED.signature_required_yn, active_yn=EXCLUDED.active_yn, description=EXCLUDED.description, updated_by=EXCLUDED.updated_by, updated_at=CURRENT_TIMESTAMP;

INSERT INTO OPS_CHANNEL_EXECUTION_POLICY (policy_key, operation_id, caller_channel, allowed_yn, authentication_required_yn, signature_required_yn, max_tps, effective_from, effective_to, active_yn, policy_version, created_by, updated_by)
VALUES (
    'CPF.DEFAULT', '*', '*', 'Y', 'N', 'N', 0,
    NULL, NULL, 'Y', 0, 'SYSTEM', 'SYSTEM'
)
ON CONFLICT (policy_key) DO UPDATE SET operation_id=EXCLUDED.operation_id, caller_channel=EXCLUDED.caller_channel, allowed_yn=EXCLUDED.allowed_yn, authentication_required_yn=EXCLUDED.authentication_required_yn, signature_required_yn=EXCLUDED.signature_required_yn, max_tps=EXCLUDED.max_tps, active_yn=EXCLUDED.active_yn, updated_by=EXCLUDED.updated_by, updated_at=CURRENT_TIMESTAMP;

INSERT INTO CMN_CODE (parent_id, code_key, code_value, description, created_by, updated_by)
VALUES (NULL, 'CODE_GROUP', 'MODULE', '서비스 모듈 코드 그룹', 'SYSTEM', 'SYSTEM'),
    (NULL, 'CODE_GROUP', 'REQUEST_TYPE', '요청 유형 코드 그룹', 'SYSTEM', 'SYSTEM'),
    (NULL, 'CODE_GROUP', 'CHANNEL_CODE', '채널 코드 그룹', 'SYSTEM', 'SYSTEM'),
    (NULL, 'CODE_GROUP', 'RESULT_TYPE', '응답 결과 유형 코드 그룹', 'SYSTEM', 'SYSTEM'),
    (NULL, 'CODE_GROUP', 'MESSAGE_FORMAT_TYPE', '메시지 포맷 유형 코드 그룹', 'SYSTEM', 'SYSTEM'),
    (NULL, 'CODE_GROUP', 'LOG_LEVEL', '동적 로그 레벨 코드 그룹', 'SYSTEM', 'SYSTEM'),
    (NULL, 'CODE_GROUP', 'CACHE_NAME', '캐시 이름 코드 그룹', 'SYSTEM', 'SYSTEM'),
    (NULL, 'CODE_GROUP', 'BATCH_JOB_TYPE', '배치 Job 유형 코드 그룹', 'SYSTEM', 'SYSTEM'),
    (NULL, 'CODE_GROUP', 'YN', '여부 코드 그룹', 'SYSTEM', 'SYSTEM')
ON CONFLICT (code_key, code_value) DO UPDATE SET description=EXCLUDED.description, updated_by=EXCLUDED.updated_by, updated_at=CURRENT_TIMESTAMP;

INSERT INTO CMN_CODE (parent_id, code_key, code_value, description, created_by, updated_by)
VALUES ((SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key = 'CODE_GROUP' AND code_value = 'MODULE') p), 'MODULE', 'CPF', '프레임워크 공통 엔진', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key = 'CODE_GROUP' AND code_value = 'MODULE') p), 'MODULE', 'CMN', '업무 공통 라이브러리', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key = 'CODE_GROUP' AND code_value = 'MODULE') p), 'MODULE', 'ADM', '관리자 운영 서비스', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key = 'CODE_GROUP' AND code_value = 'MODULE') p), 'MODULE', 'MBW', '업무 백오피스 서비스', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key = 'CODE_GROUP' AND code_value = 'MODULE') p), 'MODULE', 'BAT', '선택 배치 실행 서비스', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key = 'CODE_GROUP' AND code_value = 'MODULE') p), 'MODULE', 'EDU', '교육 샘플 서비스', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key = 'CODE_GROUP' AND code_value = 'REQUEST_TYPE') p), 'REQUEST_TYPE', 'NORMAL', '일반 요청', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key = 'CODE_GROUP' AND code_value = 'REQUEST_TYPE') p), 'REQUEST_TYPE', 'COMPENSATION', '보상 요청', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key = 'CODE_GROUP' AND code_value = 'REQUEST_TYPE') p), 'REQUEST_TYPE', 'RETRY', '재시도 요청', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key = 'CODE_GROUP' AND code_value = 'CHANNEL_CODE') p), 'CHANNEL_CODE', 'WEB', '웹 채널', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key = 'CODE_GROUP' AND code_value = 'CHANNEL_CODE') p), 'CHANNEL_CODE', 'MOBILE', '모바일 채널', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key = 'CODE_GROUP' AND code_value = 'CHANNEL_CODE') p), 'CHANNEL_CODE', 'BATCH', '배치 채널', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key = 'CODE_GROUP' AND code_value = 'CHANNEL_CODE') p), 'CHANNEL_CODE', 'ADM', '관리자 채널', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key = 'CODE_GROUP' AND code_value = 'RESULT_TYPE') p), 'RESULT_TYPE', 'S', '성공', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key = 'CODE_GROUP' AND code_value = 'RESULT_TYPE') p), 'RESULT_TYPE', 'E', '오류', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key = 'CODE_GROUP' AND code_value = 'MESSAGE_FORMAT_TYPE') p), 'MESSAGE_FORMAT_TYPE', 'FIXED', '고정 메시지', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key = 'CODE_GROUP' AND code_value = 'MESSAGE_FORMAT_TYPE') p), 'MESSAGE_FORMAT_TYPE', 'INDEXED', '인덱스 파라미터 메시지', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key = 'CODE_GROUP' AND code_value = 'LOG_LEVEL') p), 'LOG_LEVEL', 'TRACE', 'TRACE 로그', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key = 'CODE_GROUP' AND code_value = 'LOG_LEVEL') p), 'LOG_LEVEL', 'DEBUG', 'DEBUG 로그', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key = 'CODE_GROUP' AND code_value = 'LOG_LEVEL') p), 'LOG_LEVEL', 'INFO', 'INFO 로그', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key = 'CODE_GROUP' AND code_value = 'LOG_LEVEL') p), 'LOG_LEVEL', 'WARN', 'WARN 로그', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key = 'CODE_GROUP' AND code_value = 'LOG_LEVEL') p), 'LOG_LEVEL', 'ERROR', 'ERROR 로그', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key = 'CODE_GROUP' AND code_value = 'CACHE_NAME') p), 'CACHE_NAME', 'ALL', '전체 캐시', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key = 'CODE_GROUP' AND code_value = 'CACHE_NAME') p), 'CACHE_NAME', 'CODE', '코드 캐시', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key = 'CODE_GROUP' AND code_value = 'CACHE_NAME') p), 'CACHE_NAME', 'MESSAGE', '메시지 캐시', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key = 'CODE_GROUP' AND code_value = 'CACHE_NAME') p), 'CACHE_NAME', 'RESPONSE_CODE', '응답코드 캐시', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key = 'CODE_GROUP' AND code_value = 'CACHE_NAME') p), 'CACHE_NAME', 'CONFIG', '설정 캐시', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key = 'CODE_GROUP' AND code_value = 'BATCH_JOB_TYPE') p), 'BATCH_JOB_TYPE', 'TASKLET', 'Tasklet 배치', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key = 'CODE_GROUP' AND code_value = 'BATCH_JOB_TYPE') p), 'BATCH_JOB_TYPE', 'CHUNK', 'Chunk 배치', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key = 'CODE_GROUP' AND code_value = 'BATCH_JOB_TYPE') p), 'BATCH_JOB_TYPE', 'RETRY', '재처리 배치', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key = 'CODE_GROUP' AND code_value = 'YN') p), 'YN', 'Y', '예', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key = 'CODE_GROUP' AND code_value = 'YN') p), 'YN', 'N', '아니오', 'SYSTEM', 'SYSTEM')
ON CONFLICT (code_key, code_value) DO UPDATE SET parent_id=EXCLUDED.parent_id, description=EXCLUDED.description, updated_by=EXCLUDED.updated_by, updated_at=CURRENT_TIMESTAMP;

INSERT INTO CMN_MESSAGE (message_code, locale, message_format_type, external_message, internal_message, parameter_count, parameter_sample, description, created_by, updated_by)
VALUES ('MCPF000000', 'ko', 'FIXED', '정상 처리되었습니다.', 'CPF 공통 요청이 정상 처리되었습니다.', 0, NULL, 'CPF 공통 성공 메시지', 'SYSTEM', 'SYSTEM'),
    ('MCPF010001', 'ko', 'INDEXED', '요청 값이 올바르지 않습니다.', '요청 파라미터 검증에 실패했습니다. field={0}, value={1}', 2, '["field","invalid"]', 'CPF 파라미터 오류 메시지', 'SYSTEM', 'SYSTEM'),
    ('MCPF010002', 'ko', 'INDEXED', '요청한 정보를 찾을 수 없습니다.', '조회 대상 데이터가 존재하지 않습니다. target={0}', 1, '["sample-item"]', 'CPF 미존재 메시지', 'SYSTEM', 'SYSTEM'),
    ('MCPF010003', 'ko', 'INDEXED', '이미 등록된 정보입니다.', '중복 데이터가 감지되었습니다. key={0}', 1, '["sampleKey"]', 'CPF 중복 메시지', 'SYSTEM', 'SYSTEM'),
    ('MCPF010004', 'ko', 'INDEXED', '입력값을 확인해 주세요.', 'Bean Validation 검증에 실패했습니다. field={0}', 1, '["name"]', 'CPF 검증 메시지', 'SYSTEM', 'SYSTEM'),
    ('MCPF010005', 'ko', 'FIXED', '인증이 필요합니다.', '인증되지 않은 요청입니다.', 0, NULL, 'CPF 인증 필요 메시지', 'SYSTEM', 'SYSTEM'),
    ('MCPF010006', 'ko', 'INDEXED', '처리 권한이 없습니다.', '인가되지 않은 요청입니다. user={0}', 1, '["guest"]', 'CPF 권한 오류 메시지', 'SYSTEM', 'SYSTEM'),
    ('MCPF020001', 'ko', 'INDEXED', '요청을 처리할 수 없습니다.', '업무 규칙 위반이 발생했습니다. rule={0}', 1, '["business-rule"]', 'CPF 업무 규칙 메시지', 'SYSTEM', 'SYSTEM'),
    ('MCPF030001', 'ko', 'INDEXED', '일시적으로 처리할 수 없습니다.', '외부 또는 타 주제영역 연계 오류가 발생했습니다. service={0}', 1, '["generated-service"]', 'CPF 외부 연계 메시지', 'SYSTEM', 'SYSTEM'),
    ('MCPF900001', 'ko', 'INDEXED', '필수 거래 헤더가 누락되었습니다.', 'CPF 거래 헤더 검증에 실패했습니다. header={0}, uri={1}', 2, '["X-Request-Type","/api/sample-items"]', 'CPF 헤더 검증 메시지', 'SYSTEM', 'SYSTEM'),
    ('MCPF900002', 'ko', 'INDEXED', '거래 메타데이터 설정이 올바르지 않습니다.', 'CPF @CpfTransaction 메타데이터 검증에 실패했습니다. transactionId={0}', 1, '["OCPFSM0001"]', 'CPF 메타데이터 메시지', 'SYSTEM', 'SYSTEM'),
    ('MCPF900003', 'ko', 'INDEXED', '서비스 접속 정보가 없습니다.', 'CPF 서비스 endpoint 설정을 찾을 수 없습니다. serviceId={0}', 1, '["generated-service"]', 'CPF endpoint 메시지', 'SYSTEM', 'SYSTEM'),
    ('MCPF900004', 'ko', 'INDEXED', '동적 로그레벨 요청이 올바르지 않습니다.', 'CPF 동적 로그레벨 규칙 검증에 실패했습니다. reason={0}', 1, '["transactionId or businessTransactionId required"]', 'CPF 동적 로그 메시지', 'SYSTEM', 'SYSTEM'),
    ('MCPF900005', 'ko', 'INDEXED', '내부 공유 API에 접근할 수 없습니다.', 'CPF 내부 서비스 신원 또는 호출 경로 검증에 실패했습니다. reason={0}', 1, '["service identity verification failed"]', 'CPF 내부 공유 API 접근 거부 메시지', 'SYSTEM', 'SYSTEM'),
    ('MCPF990000', 'ko', 'INDEXED', '처리 중 오류가 발생했습니다.', 'CPF 내부 오류가 발생했습니다. error={0}', 1, '["Exception"]', 'CPF 내부 오류 메시지', 'SYSTEM', 'SYSTEM'),
    ('MCPF990001', 'ko', 'INDEXED', '데이터베이스 오류가 발생했습니다.', '데이터베이스 처리 오류가 발생했습니다. sqlState={0}', 1, '["HY000"]', 'CPF 데이터베이스 오류 메시지', 'SYSTEM', 'SYSTEM'),
    ('MMBW000000', 'ko', 'FIXED', '성공', 'MBW 요청이 정상 처리되었습니다.', 0, NULL, 'MBW 성공 메시지', 'SYSTEM', 'SYSTEM'),
    ('MMBW010001', 'ko', 'INDEXED', '업무 요청 값이 올바르지 않습니다.', 'MBW 입력값 검증에 실패했습니다. field={0}', 1, '["field"]', 'MBW 입력값 오류 메시지', 'SYSTEM', 'SYSTEM'),
    ('MMBW010002', 'ko', 'FIXED', '처리 권한이 없습니다.', 'MBW 서버 권한 검사에 실패했습니다.', 0, NULL, 'MBW 권한 오류 메시지', 'SYSTEM', 'SYSTEM'),
    ('MEDU010001', 'ko', 'INDEXED', '이미 등록된 {0}입니다.', '{0}={1} 값이 이미 존재합니다. duplicateCheck=EDU_SAMPLE', 2, '["샘플키","SAMPLE-0001"]', 'EDU 동적 중복 교육 메시지', 'SYSTEM', 'SYSTEM'),
    ('MCMN000001', 'ko', 'FIXED', 'CPF 교육 시스템에 오신 것을 환영합니다.', 'CMN education welcome message.', 0, NULL, 'CMN 교육 환영 메시지', 'SYSTEM', 'SYSTEM'),
    ('MCMN000001', 'en', 'FIXED', 'Welcome to the CPF education system.', 'CMN education welcome message.', 0, NULL, 'CMN 교육 환영 메시지', 'SYSTEM', 'SYSTEM')
ON CONFLICT (message_code, locale) DO UPDATE SET message_format_type=EXCLUDED.message_format_type, external_message=EXCLUDED.external_message, internal_message=EXCLUDED.internal_message, parameter_count=EXCLUDED.parameter_count, parameter_sample=EXCLUDED.parameter_sample, description=EXCLUDED.description, use_yn='Y', updated_by=EXCLUDED.updated_by, updated_at=CURRENT_TIMESTAMP;

INSERT INTO CMN_RESPONSE_CODE (response_code, message_code, result_type, module_id, response_group, sequence_no, http_status, description, created_by, updated_by)
VALUES ('SCPF000000', 'MCPF000000', 'S', 'CPF', '00', '0000', 200, 'CPF 공통 성공', 'SYSTEM', 'SYSTEM'),
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
    ('SMBW000000', 'MMBW000000', 'S', 'MBW', '00', '0000', 200, 'MBW 성공', 'SYSTEM', 'SYSTEM'),
    ('EMBW010001', 'MMBW010001', 'E', 'MBW', '01', '0001', 400, 'MBW 입력값 오류', 'SYSTEM', 'SYSTEM'),
    ('EMBW010002', 'MMBW010002', 'E', 'MBW', '01', '0002', 403, 'MBW 권한 오류', 'SYSTEM', 'SYSTEM'),
    ('EEDU010001', 'MEDU010001', 'E', 'EDU', '01', '0001', 409, 'EDU 샘플 중복 오류', 'SYSTEM', 'SYSTEM')
ON CONFLICT (response_code) DO UPDATE SET message_code=EXCLUDED.message_code, result_type=EXCLUDED.result_type, module_id=EXCLUDED.module_id, response_group=EXCLUDED.response_group, sequence_no=EXCLUDED.sequence_no, http_status=EXCLUDED.http_status, description=EXCLUDED.description, use_yn='Y', updated_by=EXCLUDED.updated_by, updated_at=CURRENT_TIMESTAMP;

INSERT INTO CMN_PARAMETER (config_key, config_value, config_type, description, encrypted_yn, created_by, updated_by)
VALUES ('CPF.CMN.CACHE.PRELOAD_ENABLED', 'Y', 'BOOLEAN', 'CMN 캐시 기동 시 선적재 여부', 'N', 'SYSTEM', 'SYSTEM'),
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
ON CONFLICT (config_key) DO UPDATE SET config_value=EXCLUDED.config_value, config_type=EXCLUDED.config_type, description=EXCLUDED.description, encrypted_yn=EXCLUDED.encrypted_yn, use_yn='Y', updated_by=EXCLUDED.updated_by, updated_at=CURRENT_TIMESTAMP;

INSERT INTO CMN_CODE (parent_id, code_key, code_value, description, created_by, updated_by)
VALUES (NULL, 'CODE_GROUP', 'HTTP_METHOD', 'HTTP Method 코드 그룹', 'SYSTEM', 'SYSTEM'),
    (NULL, 'CODE_GROUP', 'EXECUTION_STATUS', '실행 상태 코드 그룹', 'SYSTEM', 'SYSTEM'),
    (NULL, 'CODE_GROUP', 'ASYNC_STATUS', '비동기 처리 상태 코드 그룹', 'SYSTEM', 'SYSTEM'),
    (NULL, 'CODE_GROUP', 'RETRY_STATUS', '재시도 상태 코드 그룹', 'SYSTEM', 'SYSTEM'),
    (NULL, 'CODE_GROUP', 'IDEMPOTENCY_STATUS', '멱등 처리 상태 코드 그룹', 'SYSTEM', 'SYSTEM'),
    (NULL, 'CODE_GROUP', 'HEALTH_STATUS', 'Health 상태 코드 그룹', 'SYSTEM', 'SYSTEM'),
    (NULL, 'CODE_GROUP', 'CIRCUIT_STATUS', 'Circuit Breaker 상태 코드 그룹', 'SYSTEM', 'SYSTEM'),
    (NULL, 'CODE_GROUP', 'FILE_SCAN_STATUS', '첨부/파일 검사 상태 코드 그룹', 'SYSTEM', 'SYSTEM'),
    (NULL, 'CODE_GROUP', 'DATA_CLASSIFICATION', '데이터 민감도 등급 코드 그룹', 'SYSTEM', 'SYSTEM'),
    (NULL, 'CODE_GROUP', 'APPROVAL_STATUS', '결재 상태 코드 그룹', 'SYSTEM', 'SYSTEM'),
    (NULL, 'CODE_GROUP', 'ERROR_CATEGORY', '오류 분류 코드 그룹', 'SYSTEM', 'SYSTEM'),
    (NULL, 'CODE_GROUP', 'RETENTION_ACTION', '보존 정책 실행 유형 코드 그룹', 'SYSTEM', 'SYSTEM')
ON CONFLICT (code_key, code_value) DO UPDATE SET description=EXCLUDED.description, updated_by=EXCLUDED.updated_by, updated_at=CURRENT_TIMESTAMP;

INSERT INTO CMN_CODE (parent_id, code_key, code_value, description, created_by, updated_by)
VALUES ((SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='HTTP_METHOD') x), 'HTTP_METHOD', 'GET', '조회', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='HTTP_METHOD') x), 'HTTP_METHOD', 'POST', '등록/명령', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='HTTP_METHOD') x), 'HTTP_METHOD', 'PUT', '전체 수정', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='HTTP_METHOD') x), 'HTTP_METHOD', 'PATCH', '부분 수정', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='HTTP_METHOD') x), 'HTTP_METHOD', 'DELETE', '삭제/회수', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='EXECUTION_STATUS') x), 'EXECUTION_STATUS', 'READY', '실행 준비', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='EXECUTION_STATUS') x), 'EXECUTION_STATUS', 'RUNNING', '실행 중', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='EXECUTION_STATUS') x), 'EXECUTION_STATUS', 'SUCCESS', '정상 완료', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='EXECUTION_STATUS') x), 'EXECUTION_STATUS', 'FAILED', '실패', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='EXECUTION_STATUS') x), 'EXECUTION_STATUS', 'UNKNOWN_RESULT', '결과 미확정', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='ASYNC_STATUS') x), 'ASYNC_STATUS', 'WAITING', '비동기 대기', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='ASYNC_STATUS') x), 'ASYNC_STATUS', 'PROCESSING', '비동기 처리 중', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='ASYNC_STATUS') x), 'ASYNC_STATUS', 'COMPLETED', '비동기 완료', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='ASYNC_STATUS') x), 'ASYNC_STATUS', 'DLQ', 'Dead Letter Queue', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='RETRY_STATUS') x), 'RETRY_STATUS', 'RETRYABLE', '재시도 가능', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='RETRY_STATUS') x), 'RETRY_STATUS', 'NON_RETRYABLE', '재시도 금지', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='RETRY_STATUS') x), 'RETRY_STATUS', 'EXHAUSTED', '재시도 소진', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='IDEMPOTENCY_STATUS') x), 'IDEMPOTENCY_STATUS', 'PROCESSING', '멱등 처리 중', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='IDEMPOTENCY_STATUS') x), 'IDEMPOTENCY_STATUS', 'COMPLETED', '멱등 처리 완료', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='IDEMPOTENCY_STATUS') x), 'IDEMPOTENCY_STATUS', 'FAILED', '멱등 처리 실패', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='IDEMPOTENCY_STATUS') x), 'IDEMPOTENCY_STATUS', 'UNKNOWN_RESULT', '멱등 결과 미확정', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='HEALTH_STATUS') x), 'HEALTH_STATUS', 'UP', '정상', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='HEALTH_STATUS') x), 'HEALTH_STATUS', 'DOWN', '장애', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='HEALTH_STATUS') x), 'HEALTH_STATUS', 'DEGRADED', '부분 저하', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='CIRCUIT_STATUS') x), 'CIRCUIT_STATUS', 'CLOSED', '정상 호출', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='CIRCUIT_STATUS') x), 'CIRCUIT_STATUS', 'OPEN', '호출 차단', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='CIRCUIT_STATUS') x), 'CIRCUIT_STATUS', 'HALF_OPEN', '복구 시험', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='FILE_SCAN_STATUS') x), 'FILE_SCAN_STATUS', 'PENDING', '검사 대기', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='FILE_SCAN_STATUS') x), 'FILE_SCAN_STATUS', 'CLEAN', '검사 정상', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='FILE_SCAN_STATUS') x), 'FILE_SCAN_STATUS', 'INFECTED', '악성 탐지', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='FILE_SCAN_STATUS') x), 'FILE_SCAN_STATUS', 'FAILED', '검사 실패', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='FILE_SCAN_STATUS') x), 'FILE_SCAN_STATUS', 'QUARANTINED', '격리', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='DATA_CLASSIFICATION') x), 'DATA_CLASSIFICATION', 'PUBLIC', '공개 정보', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='DATA_CLASSIFICATION') x), 'DATA_CLASSIFICATION', 'INTERNAL', '내부 정보', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='DATA_CLASSIFICATION') x), 'DATA_CLASSIFICATION', 'CONFIDENTIAL', '기밀 정보', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='DATA_CLASSIFICATION') x), 'DATA_CLASSIFICATION', 'RESTRICTED', '제한/민감 정보', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='APPROVAL_STATUS') x), 'APPROVAL_STATUS', 'DRAFT', '작성 중', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='APPROVAL_STATUS') x), 'APPROVAL_STATUS', 'IN_REVIEW', '결재 진행', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='APPROVAL_STATUS') x), 'APPROVAL_STATUS', 'APPROVED', '승인 완료', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='APPROVAL_STATUS') x), 'APPROVAL_STATUS', 'REJECTED', '반려', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='APPROVAL_STATUS') x), 'APPROVAL_STATUS', 'WITHDRAWN', '철회', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='APPROVAL_STATUS') x), 'APPROVAL_STATUS', 'CANCELED', '취소', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='APPROVAL_STATUS') x), 'APPROVAL_STATUS', 'EXPIRED', '만료', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='ERROR_CATEGORY') x), 'ERROR_CATEGORY', 'VALIDATION', '입력/계약 검증 오류', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='ERROR_CATEGORY') x), 'ERROR_CATEGORY', 'AUTHENTICATION', '인증 오류', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='ERROR_CATEGORY') x), 'ERROR_CATEGORY', 'AUTHORIZATION', '인가 오류', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='ERROR_CATEGORY') x), 'ERROR_CATEGORY', 'CONFLICT', '동시성/중복 오류', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='ERROR_CATEGORY') x), 'ERROR_CATEGORY', 'TIMEOUT', 'Timeout', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='ERROR_CATEGORY') x), 'ERROR_CATEGORY', 'TARGET_DOWN', '호출 대상 장애', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='ERROR_CATEGORY') x), 'ERROR_CATEGORY', 'UNKNOWN_RESULT', '결과 미확정', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='RETENTION_ACTION') x), 'RETENTION_ACTION', 'ARCHIVE', '보관소 이관', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='RETENTION_ACTION') x), 'RETENTION_ACTION', 'PURGE', '정책 삭제', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='RETENTION_ACTION') x), 'RETENTION_ACTION', 'LEGAL_HOLD', '법적 보존', 'SYSTEM', 'SYSTEM')
ON CONFLICT (code_key, code_value) DO UPDATE SET parent_id=EXCLUDED.parent_id, description=EXCLUDED.description, updated_by=EXCLUDED.updated_by, updated_at=CURRENT_TIMESTAMP;

INSERT INTO CMN_MESSAGE (message_code, locale, message_format_type, external_message, internal_message, parameter_count, parameter_sample, description, created_by, updated_by)
VALUES ('MCPF030002','ko','FIXED','요청 시간이 초과되었습니다.','대상 호출 timeout이 발생했습니다.',0,NULL,'공통 Timeout 메시지','SYSTEM','SYSTEM'),
    ('MCPF030003','ko','FIXED','연결 대상이 일시적으로 사용할 수 없습니다.','대상 서비스가 DOWN/OPEN 상태입니다.',0,NULL,'Target down 메시지','SYSTEM','SYSTEM'),
    ('MCPF030004','ko','FIXED','처리 결과를 확인 중입니다.','요청 결과가 UNKNOWN_RESULT로 분류되어 대사가 필요합니다.',0,NULL,'결과 미확정 메시지','SYSTEM','SYSTEM'),
    ('MCPF020002','ko','FIXED','다른 사용자가 먼저 변경했습니다. 다시 조회해 주세요.','낙관적 잠금 Version 충돌이 발생했습니다.',0,NULL,'동시성 충돌 메시지','SYSTEM','SYSTEM'),
    ('MCPF020003','ko','FIXED','동일 요청이 이미 처리되었습니다.','Idempotency key가 이미 완료된 요청입니다.',0,NULL,'멱등 중복 메시지','SYSTEM','SYSTEM'),
    ('MCPF040001','ko','FIXED','첨부파일 검사가 완료되지 않았습니다.','첨부 다운로드는 CLEAN 상태에서만 허용됩니다.',0,NULL,'첨부 보안 메시지','SYSTEM','SYSTEM'),
    ('MCPF040002','ko','FIXED','첨부파일이 보안 정책에 의해 격리되었습니다.','INFECTED/QUARANTINED 파일 접근이 차단되었습니다.',0,NULL,'첨부 격리 메시지','SYSTEM','SYSTEM')
ON CONFLICT (message_code, locale) DO UPDATE SET message_format_type=EXCLUDED.message_format_type, external_message=EXCLUDED.external_message, internal_message=EXCLUDED.internal_message, parameter_count=EXCLUDED.parameter_count, parameter_sample=EXCLUDED.parameter_sample, description=EXCLUDED.description, use_yn='Y', updated_by=EXCLUDED.updated_by, updated_at=CURRENT_TIMESTAMP;

INSERT INTO CMN_RESPONSE_CODE (response_code, message_code, result_type, module_id, response_group, sequence_no, http_status, description, created_by, updated_by)
VALUES ('ECPF030002','MCPF030002','E','CPF','03','0002',504,'Timeout','SYSTEM','SYSTEM'),
    ('ECPF030003','MCPF030003','E','CPF','03','0003',503,'Target down','SYSTEM','SYSTEM'),
    ('ECPF030004','MCPF030004','E','CPF','03','0004',202,'UNKNOWN_RESULT','SYSTEM','SYSTEM'),
    ('ECPF020002','MCPF020002','E','CPF','02','0002',409,'Optimistic lock conflict','SYSTEM','SYSTEM'),
    ('ECPF020003','MCPF020003','E','CPF','02','0003',409,'Idempotency duplicate','SYSTEM','SYSTEM'),
    ('ECPF040001','MCPF040001','E','CPF','04','0001',423,'File scan pending','SYSTEM','SYSTEM'),
    ('ECPF040002','MCPF040002','E','CPF','04','0002',403,'File quarantined','SYSTEM','SYSTEM')
ON CONFLICT (response_code) DO UPDATE SET message_code=EXCLUDED.message_code, result_type=EXCLUDED.result_type, module_id=EXCLUDED.module_id, response_group=EXCLUDED.response_group, sequence_no=EXCLUDED.sequence_no, http_status=EXCLUDED.http_status, description=EXCLUDED.description, use_yn='Y', updated_by=EXCLUDED.updated_by, updated_at=CURRENT_TIMESTAMP;

INSERT INTO CMN_PARAMETER (config_key, config_value, config_type, description, encrypted_yn, created_by, updated_by)
VALUES ('CPF.MBW.SECURITY.MAX_LOGIN_FAIL_COUNT','5','NUMBER','MBW 로그인 실패 잠금 기준','N','SYSTEM','SYSTEM'),
    ('CPF.MBW.SECURITY.ACCESS_TOKEN_TTL_SECONDS','600','NUMBER','MBW Access Token TTL','N','SYSTEM','SYSTEM'),
    ('CPF.MBW.SECURITY.REFRESH_TOKEN_TTL_SECONDS','7200','NUMBER','MBW Refresh Token TTL','N','SYSTEM','SYSTEM'),
    ('CPF.RETENTION.EXECUTE_ENABLED','N','BOOLEAN','실제 Archive/Purge 실행 Kill Switch 기본 OFF','N','SYSTEM','SYSTEM'),
    ('CPF.FILE.DOWNLOAD_REQUIRE_CLEAN','Y','BOOLEAN','첨부 다운로드 CLEAN 상태 강제','N','SYSTEM','SYSTEM'),
    ('CPF.HEALTH.INSTANCE_ID_REQUIRED','Y','BOOLEAN','운영 Health 응답 인스턴스 식별자 필수','N','SYSTEM','SYSTEM')
ON CONFLICT (config_key) DO UPDATE SET config_value=EXCLUDED.config_value, config_type=EXCLUDED.config_type, description=EXCLUDED.description, encrypted_yn=EXCLUDED.encrypted_yn, use_yn='Y', updated_by=EXCLUDED.updated_by, updated_at=CURRENT_TIMESTAMP;

INSERT INTO OPS_LOG_POLICY (policy_key, policy_name, target_type, target_id, log_level, db_log_enabled_yn, file_log_enabled_yn, policy_schema_version, query_capture_mode, request_header_capture_mode, response_header_capture_mode, request_body_capture_mode, response_body_capture_mode, error_stack_capture_mode, header_allowlist, max_query_bytes, max_header_bytes, max_request_body_bytes, max_response_body_bytes, max_stack_bytes, request_body_log_yn, response_body_log_yn, error_stack_log_yn, masking_policy_key, policy_checksum, retention_days, sampling_rate, priority, active_yn, description, created_by, updated_by)
VALUES ('ONLINE_DEFAULT', '온라인 거래 기본 로그 정책', 'ONLINE_TRANSACTION', '*', 'INFO', 'Y', 'Y', 2, 'NONE', 'ALLOWLIST', 'ALLOWLIST', 'NONE', 'NONE', 'SUMMARY', 'content-type,x-cpf-trace-id,x-cpf-transaction-id', 4096, 8192, 65536, 65536, 32768, 'N', 'N', 'Y', 'DEFAULT', '04aec0a6adbf48c269e1538ca571819dc54400391e33d5b497ec05406bccd445', 90, 100.00, 100, 'Y', '온라인 Controller/API 기본 로그 정책', 'SYSTEM', 'SYSTEM'),
    ('BATCH_DEFAULT', '배치 기본 로그 정책', 'BATCH_JOB', '*', 'INFO', 'Y', 'Y', 2, 'NONE', 'ALLOWLIST', 'ALLOWLIST', 'NONE', 'NONE', 'SUMMARY', 'content-type,x-cpf-trace-id,x-cpf-transaction-id', 4096, 8192, 65536, 65536, 32768, 'N', 'N', 'Y', 'DEFAULT', '0eca9ff2359e55290f01c2594d399c32e4af9decd34541a6f571a4345f36ca08', 180, 100.00, 100, 'Y', 'Spring Batch Job 기본 로그 정책', 'SYSTEM', 'SYSTEM'),
    ('ADM_OPERATION_DEFAULT', 'ADM 운영 기본 로그 정책', 'MODULE', 'ADM', 'INFO', 'Y', 'Y', 2, 'NONE', 'ALLOWLIST', 'ALLOWLIST', 'NONE', 'NONE', 'SUMMARY', 'content-type,x-cpf-trace-id,x-cpf-transaction-id', 4096, 8192, 65536, 65536, 32768, 'N', 'N', 'Y', 'DEFAULT', '9ea15a6d3c662bcaf9295a2512cef8fc12da0e77eea6f07b3c5e55e5fb79e705', 365, 100.00, 50, 'Y', 'ADM 운영 API 기본 로그 정책', 'SYSTEM', 'SYSTEM')
ON CONFLICT (policy_key) DO UPDATE SET policy_name=EXCLUDED.policy_name, target_type=EXCLUDED.target_type, target_id=EXCLUDED.target_id, log_level=EXCLUDED.log_level, db_log_enabled_yn=EXCLUDED.db_log_enabled_yn, file_log_enabled_yn=EXCLUDED.file_log_enabled_yn, policy_schema_version=EXCLUDED.policy_schema_version, query_capture_mode=EXCLUDED.query_capture_mode, request_header_capture_mode=EXCLUDED.request_header_capture_mode, response_header_capture_mode=EXCLUDED.response_header_capture_mode, request_body_capture_mode=EXCLUDED.request_body_capture_mode, response_body_capture_mode=EXCLUDED.response_body_capture_mode, error_stack_capture_mode=EXCLUDED.error_stack_capture_mode, header_allowlist=EXCLUDED.header_allowlist, max_query_bytes=EXCLUDED.max_query_bytes, max_header_bytes=EXCLUDED.max_header_bytes, max_request_body_bytes=EXCLUDED.max_request_body_bytes, max_response_body_bytes=EXCLUDED.max_response_body_bytes, max_stack_bytes=EXCLUDED.max_stack_bytes, request_body_log_yn=EXCLUDED.request_body_log_yn, response_body_log_yn=EXCLUDED.response_body_log_yn, error_stack_log_yn=EXCLUDED.error_stack_log_yn, masking_policy_key=EXCLUDED.masking_policy_key, policy_checksum=EXCLUDED.policy_checksum, retention_days=EXCLUDED.retention_days, sampling_rate=EXCLUDED.sampling_rate, priority=EXCLUDED.priority, active_yn=EXCLUDED.active_yn, description=EXCLUDED.description, updated_by=EXCLUDED.updated_by;

INSERT INTO SEC_JWT_KEY (KEY_ID, ISSUER, ALGORITHM, SECRET_REF, ACTIVE_YN, EXPIRE_AT, created_by, updated_by)
VALUES (
    'local-cpf-hs256-001',
    'CPF',
    'HS256',
    'ENV:CPF_CMN_SECURITY_JWT_SECRET',
    'Y',
    NULL,
    'SYSTEM',
    'SYSTEM'
)
ON CONFLICT (KEY_ID) DO UPDATE SET ISSUER=EXCLUDED.ISSUER, ALGORITHM=EXCLUDED.ALGORITHM, SECRET_REF=EXCLUDED.SECRET_REF, ACTIVE_YN=EXCLUDED.ACTIVE_YN, EXPIRE_AT=EXCLUDED.EXPIRE_AT, updated_by=EXCLUDED.updated_by, updated_at=CURRENT_TIMESTAMP;

INSERT INTO CMN_CACHE_REFRESH_EVENT (cache_name, event_type, event_key, source_was_id, published_by, created_by, updated_by)
SELECT 'ALL', 'INITIAL_LOAD', 'INITIAL_FRAMEWORK_SEED', 'SQL', 'SYSTEM', 'SYSTEM', 'SYSTEM'
WHERE NOT EXISTS (
    SELECT 1
    FROM CMN_CACHE_REFRESH_EVENT
    WHERE cache_name = 'ALL'
      AND event_type = 'INITIAL_LOAD'
      AND event_key = 'INITIAL_FRAMEWORK_SEED'
);

INSERT INTO CPF_NOTIFICATION_RULE (event_type, event_sub_type, channel_code, template_code, severity, receiver_group, use_yn, created_by, updated_by)
VALUES ('BATCH_EXECUTION', 'FAILED', 'ADM', 'BATCH_FAILED_DEFAULT', 'ERROR', 'ADM_BATCH_OPERATOR', 'Y', 'SYSTEM', 'SYSTEM'),
    ('SECURITY_EVENT', 'LOGIN_FAILURE', 'ADM', 'SECURITY_LOGIN_FAILURE', 'WARN', 'ADM_SECURITY_OPERATOR', 'Y', 'SYSTEM', 'SYSTEM')
ON CONFLICT (event_type, event_sub_type, channel_code) DO UPDATE SET template_code=EXCLUDED.template_code, severity=EXCLUDED.severity, receiver_group=EXCLUDED.receiver_group, use_yn=EXCLUDED.use_yn, updated_by=EXCLUDED.updated_by, updated_at=CURRENT_TIMESTAMP;

INSERT INTO CMN_CODE (parent_id, code_key, code_value, description, created_by, updated_by)
SELECT NULL, 'CODE_GROUP', 'SORT_DIRECTION', '표준 정렬 방향', 'SYSTEM', 'SYSTEM'
WHERE NOT EXISTS (SELECT 1 FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='SORT_DIRECTION');

INSERT INTO CMN_CODE (parent_id, code_key, code_value, description, created_by, updated_by)
VALUES ((SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='SORT_DIRECTION') x), 'SORT_DIRECTION', 'ASC', '오름차순', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='SORT_DIRECTION') x), 'SORT_DIRECTION', 'DESC', '내림차순', 'SYSTEM', 'SYSTEM')
ON CONFLICT (code_key, code_value) DO UPDATE SET parent_id=EXCLUDED.parent_id, description=EXCLUDED.description, updated_by=EXCLUDED.updated_by, updated_at=CURRENT_TIMESTAMP;

INSERT INTO CMN_MESSAGE (message_code, locale, message_format_type, external_message, internal_message, parameter_count, parameter_sample, description, created_by, updated_by)
VALUES ('MCPF020004','ko','FIXED','요청 사용자 정보가 인증 사용자와 일치하지 않습니다.','Body requester spoofing이 차단되었습니다.',0,NULL,'Requester spoof 차단','SYSTEM','SYSTEM'),
    ('MCPF020005','ko','FIXED','이미 사용된 정책 버전은 직접 수정할 수 없습니다.','사용된 Approval Policy version은 immutable입니다.',0,NULL,'정책 버전 불변성','SYSTEM','SYSTEM'),
    ('MCPF020006','ko','FIXED','동일 작업 식별자가 다른 요청에 사용되었습니다.','operationId payload 충돌입니다.',0,NULL,'멱등 작업 충돌','SYSTEM','SYSTEM'),
    ('MCPF020007','ko','FIXED','현재 데이터가 다른 요청에서 변경되었습니다.','expectedVersion CAS가 실패했습니다.',0,NULL,'낙관적 잠금 재조회','SYSTEM','SYSTEM'),
    ('MCPF040003','ko','FIXED','보존 정책에 의해 해당 데이터는 삭제할 수 없습니다.','LEGAL_HOLD가 적용되어 destructive retention을 차단했습니다.',0,NULL,'Legal hold','SYSTEM','SYSTEM'),
    ('MCPF040004','ko','FIXED','보존 작업 실행이 비활성화되어 있습니다.','CPF.RETENTION.EXECUTE_ENABLED kill switch가 OFF입니다.',0,NULL,'Retention kill switch','SYSTEM','SYSTEM'),
    ('MCPF050001','ko','FIXED','Secret 원문은 조회할 수 없습니다.','Secret API는 metadata/reference만 노출합니다.',0,NULL,'Secret 비노출','SYSTEM','SYSTEM'),
    ('MCPF050002','ko','FIXED','테넌트 식별정보가 필요합니다.','Tenant mode에서 resolver가 tenantId를 결정하지 못했습니다.',0,NULL,'Tenant 필수','SYSTEM','SYSTEM')
ON CONFLICT (message_code, locale) DO UPDATE SET message_format_type=EXCLUDED.message_format_type, external_message=EXCLUDED.external_message, internal_message=EXCLUDED.internal_message, description=EXCLUDED.description, use_yn='Y', updated_by=EXCLUDED.updated_by, updated_at=CURRENT_TIMESTAMP;

INSERT INTO CMN_RESPONSE_CODE (response_code, message_code, result_type, module_id, response_group, sequence_no, http_status, description, created_by, updated_by)
VALUES ('ECPF020004','MCPF020004','E','CPF','02','0004',403,'Requester spoof blocked','SYSTEM','SYSTEM'),
    ('ECPF020005','MCPF020005','E','CPF','02','0005',409,'Policy version immutable','SYSTEM','SYSTEM'),
    ('ECPF020006','MCPF020006','E','CPF','02','0006',409,'Operation id conflict','SYSTEM','SYSTEM'),
    ('ECPF020007','MCPF020007','E','CPF','02','0007',409,'Optimistic lock retry','SYSTEM','SYSTEM'),
    ('ECPF040003','MCPF040003','E','CPF','04','0003',423,'Legal hold','SYSTEM','SYSTEM'),
    ('ECPF040004','MCPF040004','E','CPF','04','0004',403,'Retention disabled','SYSTEM','SYSTEM'),
    ('ECPF050001','MCPF050001','E','CPF','05','0001',403,'Secret value hidden','SYSTEM','SYSTEM'),
    ('ECPF050002','MCPF050002','E','CPF','05','0002',400,'Tenant required','SYSTEM','SYSTEM')
ON CONFLICT (response_code) DO UPDATE SET message_code=EXCLUDED.message_code, result_type=EXCLUDED.result_type, module_id=EXCLUDED.module_id, response_group=EXCLUDED.response_group, sequence_no=EXCLUDED.sequence_no, http_status=EXCLUDED.http_status, description=EXCLUDED.description, use_yn='Y', updated_by=EXCLUDED.updated_by, updated_at=CURRENT_TIMESTAMP;

INSERT INTO CMN_PARAMETER (config_key, config_value, config_type, description, encrypted_yn, created_by, updated_by)
VALUES ('CPF.PAGING.DEFAULT_SIZE','20','NUMBER','공통 Page 기본 크기','N','SYSTEM','SYSTEM'),
    ('CPF.PAGING.MAX_SIZE','200','NUMBER','공통 Page 최대 크기','N','SYSTEM','SYSTEM'),
    ('CPF.RETENTION.DRY_RUN_DEFAULT','Y','BOOLEAN','Retention 기본 Dry-run','N','SYSTEM','SYSTEM'),
    ('CPF.RETENTION.EXECUTE_ENABLED','N','BOOLEAN','실제 Archive/Purge 실행 Kill Switch 기본 OFF','N','SYSTEM','SYSTEM'),
    ('CPF.SECRET.CACHE_TTL_SECONDS','300','NUMBER','Secret metadata/cache 기본 TTL','N','SYSTEM','SYSTEM'),
    ('CPF.TENANT.ENABLED','N','BOOLEAN','Tenant context 기능 기본 OFF','N','SYSTEM','SYSTEM'),
    ('CPF.HEALTH.REMOTE_DEPENDENCY_GATES_READINESS','N','BOOLEAN','Remote owner 장애가 local readiness를 직접 차단하지 않음','N','SYSTEM','SYSTEM')
ON CONFLICT (config_key) DO UPDATE SET config_value=EXCLUDED.config_value, config_type=EXCLUDED.config_type, description=EXCLUDED.description, encrypted_yn=EXCLUDED.encrypted_yn, use_yn='Y', updated_by=EXCLUDED.updated_by, updated_at=CURRENT_TIMESTAMP;

INSERT INTO CMN_CODE (parent_id, code_key, code_value, description, created_by, updated_by)
VALUES ((SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='REQUEST_TYPE') x), 'REQUEST_TYPE', 'O', '온라인 요청', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='REQUEST_TYPE') x), 'REQUEST_TYPE', 'S', '공유 내부 서비스 요청', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='REQUEST_TYPE') x), 'REQUEST_TYPE', 'B', '배치 요청', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='CHANNEL_CODE') x), 'CHANNEL_CODE', 'APP', '모바일 앱 채널', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='CHANNEL_CODE') x), 'CHANNEL_CODE', 'JUT', 'JUnit/자동 테스트 채널', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='RESULT_TYPE') x), 'RESULT_TYPE', 'W', '경고/부분 성공', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='MESSAGE_FORMAT_TYPE') x), 'MESSAGE_FORMAT_TYPE', 'PARAMETER', 'Named parameter 메시지', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='ASYNC_STATUS') x), 'ASYNC_STATUS', 'FAILED', '비동기 처리 실패', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='BATCH_JOB_TYPE') x), 'BATCH_JOB_TYPE', 'SPRING_BATCH', 'Spring Batch Job', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='BATCH_JOB_TYPE') x), 'BATCH_JOB_TYPE', 'WORKER', '지속 Worker', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='BATCH_JOB_TYPE') x), 'BATCH_JOB_TYPE', 'SCHEDULER', 'Scheduler Job', 'SYSTEM', 'SYSTEM'),
    ((SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='BATCH_JOB_TYPE') x), 'BATCH_JOB_TYPE', 'CENTER_CUT', 'Center-Cut 대량 처리', 'SYSTEM', 'SYSTEM')
ON CONFLICT (code_key, code_value) DO UPDATE SET parent_id=EXCLUDED.parent_id, description=EXCLUDED.description, use_yn='Y', updated_by=EXCLUDED.updated_by, updated_at=CURRENT_TIMESTAMP;

DELETE FROM CPF_STANDARD_EXECUTION_ALIAS WHERE legacy_execution_id LIKE 'OADM-MBR-%' OR standard_execution_id LIKE 'OADMMB%';

INSERT INTO CPF_STANDARD_EXECUTION_ALIAS (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by)
VALUES ('BADM-RLG-EX-0001', 'BADMRL0001', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
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
    ('OMBW-ADM-01-1001', 'OMBWAD1001', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OMBW-ADM-03-1002', 'OMBWAD1002', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OMBW-APR-01-0001', 'OMBWAP0001', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OMBW-APR-01-0003', 'OMBWAP0003', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OMBW-APR-02-0002', 'OMBWAP0002', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OMBW-APR-05-0004', 'OMBWAP0004', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OMBW-ATC-01-0001', 'OMBWAT0001', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OMBW-ATC-02-0002', 'OMBWAT0002', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OMBW-ATC-DL-0003', 'OMBWAT0003', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OMBW-AUD-01-0001', 'OMBWUD0001', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OMBW-AUT-01-0004', 'OMBWAU0004', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OMBW-AUT-01-0005', 'OMBWAU0005', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OMBW-AUT-01-0007', 'OMBWAU0007', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OMBW-AUT-02-0001', 'OMBWAU0001', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OMBW-AUT-02-0002', 'OMBWAU0002', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OMBW-AUT-02-0003', 'OMBWAU0003', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OMBW-AUT-03-0006', 'OMBWAU0006', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OMBW-AUT-04-0008', 'OMBWAU0008', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OMBW-CUS-01-1001', 'OMBWCU1001', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OMBW-DSH-01-0001', 'OMBWDS0001', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OMBW-DWN-01-0002', 'OMBWDW0002', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OMBW-DWN-01-1001', 'OMBWDW1001', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OMBW-EMP-01-0001', 'OMBWEM0001', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OMBW-EMP-03-0002', 'OMBWEM0002', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OMBW-MNU-01-1001', 'OMBWMN1001', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OMBW-MNU-03-1002', 'OMBWMN1002', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OMBW-MSK-02-1001', 'OMBWMS1001', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OMBW-NTF-01-0001', 'OMBWNT0001', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OMBW-NTF-02-0002', 'OMBWNT0002', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OMBW-NTF-03-0003', 'OMBWNT0003', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OMBW-ORD-01-1001', 'OMBWOR1001', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OMBW-ORG-01-0001', 'OMBWOR0001', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OMBW-ORG-03-0002', 'OMBWOR0002', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OMBW-PER-01-0002', 'OMBWPE0002', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OMBW-PER-01-0003', 'OMBWPE0003', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OMBW-PER-01-1001', 'OMBWPE1001', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OMBW-PER-02-0004', 'OMBWPE0004', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OMBW-PER-03-1002', 'OMBWPE1002', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OMBW-PRD-01-1001', 'OMBWPR1001', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OMBW-ROL-01-1001', 'OMBWRO1001', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OMBW-ROL-03-1002', 'OMBWRO1002', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OMBW-SCH-01-0001', 'OMBWSC0001', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OMBW-SCH-03-0002', 'OMBWSC0002', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OMBW-SCH-04-0003', 'OMBWSC0003', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OMBW-SET-01-1001', 'OMBWSE1001', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OMBW-USR-QY-0000', 'OMBWUS0001', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OMBW-USR-QY-0001', 'OMBWUS0002', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
    ('OREF-EDU-01-0001', 'OEDUAA0001', 'CPF O/S/B 10자리 표준 전환', 'CPF_SEED', 'CPF_SEED'),
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
ON CONFLICT (legacy_execution_id) DO UPDATE SET standard_execution_id=EXCLUDED.standard_execution_id, migration_reason=EXCLUDED.migration_reason, updated_by=EXCLUDED.updated_by, updated_at=CURRENT_TIMESTAMP;

INSERT INTO ADM_ROLE (ROLE_ID, ROLE_NAME, ROLE_TYPE, DESCRIPTION, USE_YN, created_by, updated_by)
VALUES ('ADM_ADMIN', '프레임워크 관리자', 'ADMIN', '모든 ADM 메뉴와 운영 작업을 관리합니다.', 'Y', 'SYSTEM', 'SYSTEM'),
    ('ADM_DEV_OPERATOR', '개발자 운영자', 'DEVELOPER_OPERATOR', '로그, 캐시, 코드, 메시지, 설정, 배치 관제를 운영합니다.', 'Y', 'SYSTEM', 'SYSTEM'),
    ('ADM_BIZ_OPERATOR', '업무 운영자', 'BUSINESS_OPERATOR', '회원, 거래 로그, 배치, 캐시 같은 업무 운영 기능을 수행합니다.', 'Y', 'SYSTEM', 'SYSTEM'),
    ('ADM_VIEWER', '조회 전용 운영자', 'VIEWER', '운영 정보를 조회만 할 수 있습니다.', 'Y', 'SYSTEM', 'SYSTEM'),
    ('ADM_OPERATOR', '운영자 호환 역할', 'DEVELOPER_OPERATOR', '기존 ADM_OPERATOR 호환을 위한 역할입니다.', 'Y', 'SYSTEM', 'SYSTEM')
ON CONFLICT (ROLE_ID) DO UPDATE SET ROLE_NAME=EXCLUDED.ROLE_NAME, ROLE_TYPE=EXCLUDED.ROLE_TYPE, DESCRIPTION=EXCLUDED.DESCRIPTION, USE_YN=EXCLUDED.USE_YN, updated_by=EXCLUDED.updated_by, updated_at=CURRENT_TIMESTAMP;

INSERT INTO ADM_MENU (MENU_ID, PARENT_MENU_ID, MENU_NAME, MENU_PATH, SORT_ORDER, USE_YN, created_by, updated_by)
VALUES ('DASHBOARD', NULL, '대시보드', '/adm', 10, 'Y', 'SYSTEM', 'SYSTEM'),
    ('CAPABILITY_FLEET', NULL, 'CPF Capability', '/adm#capabilities', 15, 'Y', 'SYSTEM', 'SYSTEM'),
    ('LOG_LIST', NULL, '온라인 거래 로그', '/adm#logs', 20, 'Y', 'SYSTEM', 'SYSTEM'),
    ('STANDARD_EXECUTION', NULL, '표준 실행 카탈로그', '/adm#standard-executions', 23, 'Y', 'SYSTEM', 'SYSTEM'),
    ('CHANNEL_POLICY', NULL, '채널 정책', '/adm#channel-policy', 24, 'Y', 'SYSTEM', 'SYSTEM'),
    ('REMOTE_LOG', NULL, '원격 로그 관리', '/adm#remote-logs', 25, 'Y', 'SYSTEM', 'SYSTEM'),
    ('TRANSACTION_META', NULL, '거래 메타', '/adm#transactions', 25, 'Y', 'SYSTEM', 'SYSTEM'),
    ('AUDIT_LOG', NULL, '감사 로그', '/adm#audit-logs', 30, 'Y', 'SYSTEM', 'SYSTEM'),
    ('BATCH', NULL, '배치 관제', '/adm#batch', 50, 'Y', 'SYSTEM', 'SYSTEM'),
    ('RELIABILITY', NULL, '신뢰성 처리 관제', '/adm#reliability', 52, 'Y', 'SYSTEM', 'SYSTEM'),
    ('NOTIFICATION', NULL, '알림 관리', '/adm#notifications', 55, 'Y', 'SYSTEM', 'SYSTEM'),
    ('DOWNLOAD', NULL, '다운로드 감사', '/adm#downloads', 58, 'Y', 'SYSTEM', 'SYSTEM'),
    ('CACHE', NULL, '캐시 관리', '/adm#cache', 60, 'Y', 'SYSTEM', 'SYSTEM'),
    ('FILE_JOB', NULL, '대량파일 Job', '/adm#file-jobs', 61, 'Y', 'SYSTEM', 'SYSTEM'),
    ('MESSAGE', NULL, '메시지 관리', '/adm#messages', 70, 'Y', 'SYSTEM', 'SYSTEM'),
    ('CODE', NULL, '코드 관리', '/adm#codes', 80, 'Y', 'SYSTEM', 'SYSTEM'),
    ('RESPONSE_CODE', NULL, '응답코드 관리', '/adm#response-codes', 90, 'Y', 'SYSTEM', 'SYSTEM'),
    ('CONFIG', NULL, '설정 관리', '/adm#configs', 100, 'Y', 'SYSTEM', 'SYSTEM'),
    ('DYNAMIC_LOG', NULL, '동적 로그 레벨', '/adm#log-level', 110, 'Y', 'SYSTEM', 'SYSTEM'),
    ('LOG_POLICY', NULL, '로그 정책', '/adm#log-policies', 115, 'Y', 'SYSTEM', 'SYSTEM'),
    ('PASSWORD', NULL, '비밀번호 관리', '/adm#password', 120, 'Y', 'SYSTEM', 'SYSTEM'),
    ('SECURITY', NULL, '보안 운영', '/adm#security', 130, 'Y', 'SYSTEM', 'SYSTEM'),
    ('PERMISSION', NULL, '권한 관리', '/adm#permissions', 140, 'Y', 'SYSTEM', 'SYSTEM'),
    ('SECRET', NULL, 'Secret / Key 관리', '/adm#secrets', 145, 'Y', 'SYSTEM', 'SYSTEM'),
    ('OPERATOR', NULL, '운영자 관리', '/adm#operators', 150, 'Y', 'SYSTEM', 'SYSTEM'),
    ('GATEWAY_DASHBOARD', NULL, 'Gateway 대시보드', '/adm#gateway-dashboard', 300, 'Y', 'SYSTEM', 'SYSTEM'),
    ('GATEWAY_SERVERS', 'GATEWAY_DASHBOARD', 'Gateway 연동 서버', '/adm#gateway-servers', 301, 'Y', 'SYSTEM', 'SYSTEM'),
    ('GATEWAY_GROUPS', 'GATEWAY_DASHBOARD', 'Gateway 서버 그룹', '/adm#gateway-groups', 302, 'Y', 'SYSTEM', 'SYSTEM'),
    ('GATEWAY_ROUTES', 'GATEWAY_DASHBOARD', 'Gateway 경로·라우팅', '/adm#gateway-routes', 303, 'Y', 'SYSTEM', 'SYSTEM'),
    ('GATEWAY_SECURITY', 'GATEWAY_DASHBOARD', 'Gateway 보안·제한', '/adm#gateway-security', 304, 'Y', 'SYSTEM', 'SYSTEM'),
    ('GATEWAY_HEALTH', 'GATEWAY_DASHBOARD', 'Gateway Health·연결시험', '/adm#gateway-health', 305, 'Y', 'SYSTEM', 'SYSTEM'),
    ('GATEWAY_TRANSACTIONS', 'GATEWAY_DASHBOARD', 'Gateway 거래 조회', '/adm#gateway-transactions', 306, 'Y', 'SYSTEM', 'SYSTEM'),
    ('GATEWAY_LOG_POLICY', 'GATEWAY_DASHBOARD', 'Gateway 로그 정책', '/adm#gateway-log-policies', 307, 'Y', 'SYSTEM', 'SYSTEM'),
    ('GATEWAY_APPLY_STATUS', 'GATEWAY_DASHBOARD', 'Gateway 적용 상태·이력', '/adm#gateway-apply-status', 308, 'Y', 'SYSTEM', 'SYSTEM'),
    ('BATCH_OVERVIEW', 'BATCH', 'Batch Overview', '/adm#batch-overview', 501, 'Y', 'SYSTEM', 'SYSTEM'),
    ('BATCH_RUNTIME', 'BATCH', 'Runtime Topology', '/adm#batch-runtime', 502, 'Y', 'SYSTEM', 'SYSTEM'),
    ('BATCH_INSTANCES', 'BATCH', 'Runtime Instances', '/adm#batch-instances', 503, 'Y', 'SYSTEM', 'SYSTEM'),
    ('BATCH_SCHEDULER', 'BATCH', 'Scheduler HA', '/adm#batch-scheduler', 504, 'Y', 'SYSTEM', 'SYSTEM'),
    ('BATCH_WORKER_POOLS', 'BATCH', 'Worker Pools', '/adm#batch-worker-pools', 505, 'Y', 'SYSTEM', 'SYSTEM'),
    ('BATCH_CENTER_CUT', 'BATCH', 'Center-Cut', '/adm#batch-center-cut', 506, 'Y', 'SYSTEM', 'SYSTEM'),
    ('BATCH_AGENTS', 'BATCH', 'Host Agents', '/adm#batch-agents', 507, 'Y', 'SYSTEM', 'SYSTEM'),
    ('BATCH_JOB_PACKS', 'BATCH', 'Job Packs', '/adm#batch-job-packs', 508, 'Y', 'SYSTEM', 'SYSTEM'),
    ('BATCH_EXECUTIONS', 'BATCH', 'Executions', '/adm#batch-executions', 509, 'Y', 'SYSTEM', 'SYSTEM'),
    ('BATCH_DEPLOYMENT', 'BATCH', 'Deployment / Rollback', '/adm#batch-deployment', 510, 'Y', 'SYSTEM', 'SYSTEM'),
    ('BATCH_RECOVERY', 'BATCH', 'Recovery / Unknown', '/adm#batch-recovery', 511, 'Y', 'SYSTEM', 'SYSTEM'),
    ('BATCH_LEASES', 'BATCH', 'Lease / Fencing', '/adm#batch-leases', 512, 'Y', 'SYSTEM', 'SYSTEM'),
    ('BATCH_ALERTS', 'BATCH', 'Batch Alerts', '/adm#batch-alerts', 513, 'Y', 'SYSTEM', 'SYSTEM'),
    ('BATCH_AUDIT', 'BATCH', 'Audit / Evidence', '/adm#batch-audit', 514, 'Y', 'SYSTEM', 'SYSTEM'),
    ('APPROVAL', NULL, '위험조치 승인', '/adm#approvals', 524, 'Y', 'SYSTEM', 'SYSTEM'),
    ('BREAK_GLASS', NULL, 'Break-glass', '/adm#breakGlass', 534, 'Y', 'SYSTEM', 'SYSTEM'),
    ('BUSINESS_CALENDAR', NULL, '영업일 · 휴일', '/adm#businessCalendar', 544, 'Y', 'SYSTEM', 'SYSTEM'),
    ('CAPACITY', NULL, 'Online Runtime Diagnostics', '/adm#capacity', 554, 'Y', 'SYSTEM', 'SYSTEM'),
    ('FEATURE_FLAG', NULL, 'Feature Flag', '/adm#featureFlags', 564, 'Y', 'SYSTEM', 'SYSTEM'),
    ('TOPOLOGY', NULL, '서비스 토폴로지', '/adm#topology', 574, 'Y', 'SYSTEM', 'SYSTEM'),
    ('INCIDENT', NULL, 'Error·Unknown Result', '/adm#incidents', 584, 'Y', 'SYSTEM', 'SYSTEM'),
    ('INTEGRATION_CLOSURE', NULL, '통합 운영 정정 승인', '/adm#integrationClosure', 594, 'Y', 'SYSTEM', 'SYSTEM'),
    ('MAINTENANCE', NULL, '점검·Drain', '/adm#maintenance', 604, 'Y', 'SYSTEM', 'SYSTEM'),
    ('OPENAPI_OPERATIONS', NULL, 'OpenAPI 운영', '/adm#openApiOperations', 614, 'Y', 'SYSTEM', 'SYSTEM'),
    ('OPS_GOVERNANCE', NULL, '운영 정책·SLO', '/adm#operations-governance', 624, 'Y', 'SYSTEM', 'SYSTEM'),
    ('RECOVERY_CENTER', NULL, '복구 센터', '/adm#recoveryCenter', 634, 'Y', 'SYSTEM', 'SYSTEM'),
    ('RESILIENCE_POLICY', NULL, 'Resilience 정책', '/adm#resiliencePolicies', 644, 'Y', 'SYSTEM', 'SYSTEM'),
    ('RUNTIME_CONTROL', NULL, 'Deployment·Promotion·Rollback', '/adm#runtimeControl', 654, 'Y', 'SYSTEM', 'SYSTEM'),
    ('SERVICE_REGISTRY', NULL, '서비스 레지스트리', '/adm#serviceRegistry', 664, 'Y', 'SYSTEM', 'SYSTEM'),
    ('WORKER', NULL, 'Agent / Worker', '/adm#workers', 674, 'Y', 'SYSTEM', 'SYSTEM')
ON CONFLICT (MENU_ID) DO UPDATE SET PARENT_MENU_ID=EXCLUDED.PARENT_MENU_ID, MENU_NAME=EXCLUDED.MENU_NAME, MENU_PATH=EXCLUDED.MENU_PATH, SORT_ORDER=EXCLUDED.SORT_ORDER, USE_YN=EXCLUDED.USE_YN, updated_by=EXCLUDED.updated_by, updated_at=CURRENT_TIMESTAMP;

INSERT INTO ADM_BUTTON (BUTTON_ID, MENU_ID, ACTION_CODE, BUTTON_NAME, HTTP_METHOD, API_PATTERN, SORT_ORDER, USE_YN, created_by, updated_by)
VALUES ('CAPABILITY_FLEET_READ', 'CAPABILITY_FLEET', 'READ', 'CPF Capability 조회', 'GET', '/adm/api/capability-management/**', 10, 'Y', 'SYSTEM', 'SYSTEM'),
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
    ('REMOTE_LOG_BUNDLE_DOWNLOAD', 'REMOTE_LOG', 'BUNDLE_DOWNLOAD', '동기 로그 ZIP 다운로드', 'POST', '/adm/api/remote-logs/bundles', 30, 'Y', 'SYSTEM', 'SYSTEM'),
    ('REMOTE_LOG_BUNDLE_CREATE', 'REMOTE_LOG', 'CREATE', '비동기 로그 ZIP 작업 등록', 'POST', '/adm/api/remote-logs/bundle-jobs', 40, 'Y', 'SYSTEM', 'SYSTEM'),
    ('REMOTE_LOG_BUNDLE_TOKEN', 'REMOTE_LOG', 'ISSUE', '로그 ZIP 다운로드 token 발급', 'POST', '/adm/api/remote-logs/bundle-jobs/*/download-tokens', 50, 'Y', 'SYSTEM', 'SYSTEM'),
    ('REMOTE_LOG_JOB_DOWNLOAD', 'REMOTE_LOG', 'JOB_DOWNLOAD', '비동기 로그 ZIP 다운로드', 'GET', '/adm/api/remote-logs/bundle-jobs/*/download', 60, 'Y', 'SYSTEM', 'SYSTEM'),
    ('TRANSACTION_META_READ', 'TRANSACTION_META', 'READ', '거래 메타 조회', 'GET', '/adm/api/transactions/**', 10, 'Y', 'SYSTEM', 'SYSTEM'),
    ('TRANSACTION_META_WRITE', 'TRANSACTION_META', 'WRITE', '거래 메타 비활성화', 'POST', '/adm/api/transactions/*/inactive', 30, 'Y', 'SYSTEM', 'SYSTEM'),
    ('AUDIT_LOG_READ', 'AUDIT_LOG', 'READ', '조회', 'GET', '/adm/api/audit-logs/**', 10, 'Y', 'SYSTEM', 'SYSTEM'),
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
    ('NOTIFICATION_RETRY', 'NOTIFICATION', 'RETRY', '알림 발송 재시도', 'POST', '/adm/api/notifications/delivery-logs/*/retry', 50, 'Y', 'SYSTEM', 'SYSTEM'),
    ('NOTIFICATION_CANCEL', 'NOTIFICATION', 'CANCEL', '알림 발송 취소', 'POST', '/adm/api/notifications/delivery-logs/*/cancel', 60, 'Y', 'SYSTEM', 'SYSTEM'),
    ('DOWNLOAD_READ', 'DOWNLOAD', 'READ', '다운로드 감사 조회', 'GET', '/adm/api/downloads/**', 10, 'Y', 'SYSTEM', 'SYSTEM'),
    ('DOWNLOAD_EXECUTE', 'DOWNLOAD', 'DOWNLOAD', 'CSV 다운로드', 'POST', '/adm/api/downloads/csv', 20, 'Y', 'SYSTEM', 'SYSTEM'),
    ('CACHE_READ', 'CACHE', 'READ', '조회', 'GET', '/adm/api/cache/**', 10, 'Y', 'SYSTEM', 'SYSTEM'),
    ('CACHE_REFRESH', 'CACHE', 'REFRESH', '캐시 갱신', 'POST', '/adm/api/cache/**', 20, 'Y', 'SYSTEM', 'SYSTEM'),
    ('CACHE_EVICT_KEY', 'CACHE', 'EVICT_KEY', '단일 Cache 제거', 'POST', '/adm/api/cache/evict-key', 30, 'Y', 'SYSTEM', 'SYSTEM'),
    ('CACHE_EVICT_NAMESPACE', 'CACHE', 'EVICT_NAMESPACE', 'Namespace Cache 제거', 'POST', '/adm/api/cache/evict-namespace', 40, 'Y', 'SYSTEM', 'SYSTEM'),
    ('CACHE_RECONCILE', 'CACHE', 'RECONCILE', 'Cache Durable 재조정', 'POST', '/adm/api/cache/reconcile', 50, 'Y', 'SYSTEM', 'SYSTEM'),
    ('FILE_JOB_READ', 'FILE_JOB', 'READ', 'File Job 조회', 'GET', '/adm/api/file-jobs/**', 10, 'Y', 'SYSTEM', 'SYSTEM'),
    ('FILE_JOB_UPLOAD', 'FILE_JOB', 'UPLOAD', 'Upload 접수', 'POST', '/adm/api/file-jobs/uploads', 20, 'Y', 'SYSTEM', 'SYSTEM'),
    ('FILE_JOB_APPLY', 'FILE_JOB', 'APPLY', '검증 Job 적용', 'POST', '/adm/api/file-jobs/*/apply', 30, 'Y', 'SYSTEM', 'SYSTEM'),
    ('FILE_JOB_RETRY', 'FILE_JOB', 'RETRY', 'File Job 재시도', 'POST', '/adm/api/file-jobs/*/retry', 40, 'Y', 'SYSTEM', 'SYSTEM'),
    ('FILE_JOB_CANCEL', 'FILE_JOB', 'CANCEL', 'File Job 취소', 'POST', '/adm/api/file-jobs/*/cancel', 50, 'Y', 'SYSTEM', 'SYSTEM'),
    ('FILE_JOB_ROLLBACK', 'FILE_JOB', 'ROLLBACK', 'File Job Rollback', 'POST', '/adm/api/file-jobs/*/rollback', 60, 'Y', 'SYSTEM', 'SYSTEM'),
    ('FILE_JOB_DOWNLOAD', 'FILE_JOB', 'DOWNLOAD', 'Artifact 다운로드', 'GET', '/adm/api/file-jobs/*/artifact', 70, 'Y', 'SYSTEM', 'SYSTEM'),
    ('FILE_JOB_RESOLVE', 'FILE_JOB', 'RESOLVE', '결과 불명 확정', 'POST', '/adm/api/file-jobs/*/resolve-unknown', 80, 'Y', 'SYSTEM', 'SYSTEM'),
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
    ('OPERATOR_ROLE_UPDATE', 'OPERATOR', 'ROLE_UPDATE', '역할 부여', 'PUT', '/adm/api/operators/*/roles', 30, 'Y', 'SYSTEM', 'SYSTEM'),
    ('OPERATOR_STATUS_UPDATE', 'OPERATOR', 'STATUS_UPDATE', '계정 상태 변경', 'PUT', '/adm/api/operators/*/status', 40, 'Y', 'SYSTEM', 'SYSTEM'),
    ('OPERATOR_CONTACT_UPDATE', 'OPERATOR', 'CONTACT_UPDATE', '연락처 변경', 'PUT', '/adm/api/operators/*/contacts', 50, 'Y', 'SYSTEM', 'SYSTEM'),
    ('OPERATOR_PII_RAW', 'OPERATOR', 'PII_RAW', '연락처 원문 조회', 'POST', '/adm/api/operators/*/contacts/raw', 60, 'Y', 'SYSTEM', 'SYSTEM')
ON CONFLICT (BUTTON_ID) DO UPDATE SET MENU_ID=EXCLUDED.MENU_ID, ACTION_CODE=EXCLUDED.ACTION_CODE, BUTTON_NAME=EXCLUDED.BUTTON_NAME, HTTP_METHOD=EXCLUDED.HTTP_METHOD, API_PATTERN=EXCLUDED.API_PATTERN, SORT_ORDER=EXCLUDED.SORT_ORDER, USE_YN=EXCLUDED.USE_YN, updated_by=EXCLUDED.updated_by, updated_at=CURRENT_TIMESTAMP;

INSERT INTO ADM_PASSWORD_POLICY (POLICY_ID, MIN_LENGTH, REQUIRE_UPPER_YN, REQUIRE_LOWER_YN, REQUIRE_DIGIT_YN, REQUIRE_SPECIAL_YN, MAX_FAIL_COUNT, EXPIRE_DAYS, HISTORY_LIMIT, USE_YN, created_by, updated_by)
VALUES (
    'DEFAULT', 12, 'Y', 'Y', 'Y', 'Y', 5, 90, 5, 'Y', 'SYSTEM', 'SYSTEM'
)
ON CONFLICT (POLICY_ID) DO UPDATE SET MIN_LENGTH=EXCLUDED.MIN_LENGTH, REQUIRE_UPPER_YN=EXCLUDED.REQUIRE_UPPER_YN, REQUIRE_LOWER_YN=EXCLUDED.REQUIRE_LOWER_YN, REQUIRE_DIGIT_YN=EXCLUDED.REQUIRE_DIGIT_YN, REQUIRE_SPECIAL_YN=EXCLUDED.REQUIRE_SPECIAL_YN, MAX_FAIL_COUNT=EXCLUDED.MAX_FAIL_COUNT, EXPIRE_DAYS=EXCLUDED.EXPIRE_DAYS, HISTORY_LIMIT=EXCLUDED.HISTORY_LIMIT, USE_YN=EXCLUDED.USE_YN, updated_by=EXCLUDED.updated_by, updated_at=CURRENT_TIMESTAMP;

INSERT INTO ADM_ROLE_MENU (ROLE_ID, MENU_ID, READ_YN, WRITE_YN, DELETE_YN, created_by, updated_by)
SELECT 'ADM_ADMIN', MENU_ID, 'Y', 'Y', 'Y', 'SYSTEM', 'SYSTEM'
FROM ADM_MENU
ON CONFLICT (ROLE_ID, MENU_ID) DO UPDATE SET READ_YN=EXCLUDED.READ_YN, WRITE_YN=EXCLUDED.WRITE_YN, DELETE_YN=EXCLUDED.DELETE_YN, updated_by=EXCLUDED.updated_by, updated_at=CURRENT_TIMESTAMP;

INSERT INTO ADM_ROLE_MENU (ROLE_ID, MENU_ID, READ_YN, WRITE_YN, DELETE_YN, created_by, updated_by)
SELECT 'ADM_DEV_OPERATOR', MENU_ID, 'Y',
       CASE WHEN MENU_ID IN ('TRANSACTION_META', 'CHANNEL_POLICY', 'REMOTE_LOG', 'BATCH', 'RELIABILITY', 'NOTIFICATION', 'DOWNLOAD', 'CACHE', 'FILE_JOB', 'MESSAGE', 'CODE', 'RESPONSE_CODE', 'CONFIG', 'DYNAMIC_LOG', 'LOG_POLICY') THEN 'Y' ELSE 'N' END,
       CASE WHEN MENU_ID IN ('TRANSACTION_META', 'MESSAGE', 'CODE', 'DYNAMIC_LOG', 'LOG_POLICY') THEN 'Y' ELSE 'N' END,
       'SYSTEM', 'SYSTEM'
FROM ADM_MENU
WHERE MENU_ID NOT IN ('OPERATOR', 'PERMISSION', 'PASSWORD', 'SECURITY')
ON CONFLICT (ROLE_ID, MENU_ID) DO UPDATE SET READ_YN=EXCLUDED.READ_YN, WRITE_YN=EXCLUDED.WRITE_YN, DELETE_YN=EXCLUDED.DELETE_YN, updated_by=EXCLUDED.updated_by, updated_at=CURRENT_TIMESTAMP;

INSERT INTO ADM_ROLE_MENU (ROLE_ID, MENU_ID, READ_YN, WRITE_YN, DELETE_YN, created_by, updated_by)
SELECT 'ADM_BIZ_OPERATOR', MENU_ID, 'Y',
       CASE WHEN MENU_ID IN ('BATCH', 'DOWNLOAD', 'CACHE', 'FILE_JOB') THEN 'Y' ELSE 'N' END,
       'N',
       'SYSTEM', 'SYSTEM'
FROM ADM_MENU
WHERE MENU_ID IN ('DASHBOARD', 'LOG_LIST', 'STANDARD_EXECUTION', 'CHANNEL_POLICY', 'REMOTE_LOG', 'TRANSACTION_META', 'AUDIT_LOG', 'BATCH', 'RELIABILITY', 'NOTIFICATION', 'DOWNLOAD', 'CACHE', 'FILE_JOB', 'MESSAGE', 'CODE')
ON CONFLICT (ROLE_ID, MENU_ID) DO UPDATE SET READ_YN=EXCLUDED.READ_YN, WRITE_YN=EXCLUDED.WRITE_YN, DELETE_YN=EXCLUDED.DELETE_YN, updated_by=EXCLUDED.updated_by, updated_at=CURRENT_TIMESTAMP;

INSERT INTO ADM_ROLE_MENU (ROLE_ID, MENU_ID, READ_YN, WRITE_YN, DELETE_YN, created_by, updated_by)
SELECT 'ADM_VIEWER', MENU_ID, 'Y', 'N', 'N', 'SYSTEM', 'SYSTEM'
FROM ADM_MENU
WHERE MENU_ID IN ('DASHBOARD', 'CAPABILITY_FLEET', 'LOG_LIST', 'STANDARD_EXECUTION', 'CHANNEL_POLICY', 'REMOTE_LOG', 'TRANSACTION_META', 'AUDIT_LOG', 'BATCH', 'RELIABILITY', 'NOTIFICATION', 'DOWNLOAD', 'CACHE', 'FILE_JOB', 'MESSAGE', 'CODE', 'RESPONSE_CODE', 'CONFIG', 'LOG_POLICY')
ON CONFLICT (ROLE_ID, MENU_ID) DO UPDATE SET READ_YN=EXCLUDED.READ_YN, WRITE_YN=EXCLUDED.WRITE_YN, DELETE_YN=EXCLUDED.DELETE_YN, updated_by=EXCLUDED.updated_by, updated_at=CURRENT_TIMESTAMP;

INSERT INTO ADM_ROLE_MENU (ROLE_ID, MENU_ID, READ_YN, WRITE_YN, DELETE_YN, created_by, updated_by)
SELECT 'ADM_OPERATOR', MENU_ID, READ_YN, WRITE_YN, DELETE_YN, 'SYSTEM', 'SYSTEM'
FROM ADM_ROLE_MENU
WHERE ROLE_ID = 'ADM_DEV_OPERATOR'
ON CONFLICT (ROLE_ID, MENU_ID) DO UPDATE SET READ_YN=EXCLUDED.READ_YN, WRITE_YN=EXCLUDED.WRITE_YN, DELETE_YN=EXCLUDED.DELETE_YN, updated_by=EXCLUDED.updated_by, updated_at=CURRENT_TIMESTAMP;

INSERT INTO ADM_ROLE_BUTTON (ROLE_ID, BUTTON_ID, ALLOW_YN, created_by, updated_by)
SELECT 'ADM_ADMIN', BUTTON_ID, 'Y', 'SYSTEM', 'SYSTEM'
FROM ADM_BUTTON
ON CONFLICT (ROLE_ID, BUTTON_ID) DO UPDATE SET ALLOW_YN=EXCLUDED.ALLOW_YN, updated_by=EXCLUDED.updated_by, updated_at=CURRENT_TIMESTAMP;

INSERT INTO ADM_ROLE_BUTTON (ROLE_ID, BUTTON_ID, ALLOW_YN, created_by, updated_by)
SELECT 'ADM_DEV_OPERATOR', BUTTON_ID,
       CASE WHEN MENU_ID IN ('OPERATOR', 'PERMISSION', 'PASSWORD', 'SECURITY') THEN 'N' ELSE 'Y' END,
       'SYSTEM', 'SYSTEM'
FROM ADM_BUTTON
ON CONFLICT (ROLE_ID, BUTTON_ID) DO UPDATE SET ALLOW_YN=EXCLUDED.ALLOW_YN, updated_by=EXCLUDED.updated_by, updated_at=CURRENT_TIMESTAMP;

INSERT INTO ADM_ROLE_BUTTON (ROLE_ID, BUTTON_ID, ALLOW_YN, created_by, updated_by)
SELECT 'ADM_BIZ_OPERATOR', BUTTON_ID,
       CASE
           WHEN BUTTON_ID IN ('BATCH_EXECUTE', 'BATCH_RETRY', 'BATCH_SIMULATION', 'BATCH_RELATION_READ', 'BATCH_TARGET_READ', 'BATCH_SCHEDULER_RUN', 'DOWNLOAD_EXECUTE', 'CACHE_REFRESH', 'FILE_JOB_UPLOAD', 'FILE_JOB_APPLY', 'FILE_JOB_DOWNLOAD') THEN 'Y'
           WHEN ACTION_CODE IN ('READ', 'DETAIL') AND MENU_ID IN ('LOG_LIST', 'REMOTE_LOG', 'TRANSACTION_META', 'AUDIT_LOG', 'BATCH', 'RELIABILITY', 'NOTIFICATION', 'DOWNLOAD', 'CACHE', 'FILE_JOB', 'MESSAGE', 'CODE', 'LOG_POLICY') THEN 'Y'
           ELSE 'N'
       END,
       'SYSTEM', 'SYSTEM'
FROM ADM_BUTTON
ON CONFLICT (ROLE_ID, BUTTON_ID) DO UPDATE SET ALLOW_YN=EXCLUDED.ALLOW_YN, updated_by=EXCLUDED.updated_by, updated_at=CURRENT_TIMESTAMP;

INSERT INTO ADM_ROLE_BUTTON (ROLE_ID, BUTTON_ID, ALLOW_YN, created_by, updated_by)
SELECT 'ADM_VIEWER', BUTTON_ID,
       CASE WHEN ACTION_CODE IN ('READ', 'DETAIL') THEN 'Y' ELSE 'N' END,
       'SYSTEM', 'SYSTEM'
FROM ADM_BUTTON
ON CONFLICT (ROLE_ID, BUTTON_ID) DO UPDATE SET ALLOW_YN=EXCLUDED.ALLOW_YN, updated_by=EXCLUDED.updated_by, updated_at=CURRENT_TIMESTAMP;

INSERT INTO ADM_ROLE_BUTTON (ROLE_ID, BUTTON_ID, ALLOW_YN, created_by, updated_by)
SELECT 'ADM_OPERATOR', BUTTON_ID, ALLOW_YN, 'SYSTEM', 'SYSTEM'
FROM ADM_ROLE_BUTTON
WHERE ROLE_ID = 'ADM_DEV_OPERATOR'
ON CONFLICT (ROLE_ID, BUTTON_ID) DO UPDATE SET ALLOW_YN=EXCLUDED.ALLOW_YN, updated_by=EXCLUDED.updated_by, updated_at=CURRENT_TIMESTAMP;

INSERT INTO ADM_API_PERMISSION (API_PERMISSION_ID, API_GROUP_CODE, HTTP_METHOD, API_PATH, API_NAME, PERMISSION_CODE, MENU_ID, BUTTON_ID, USE_YN, created_by, updated_by)
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
FROM (
    SELECT b.*,
           ROW_NUMBER() OVER (
               PARTITION BY COALESCE(HTTP_METHOD, 'ANY'), API_PATTERN
               ORDER BY SORT_ORDER, BUTTON_ID
           ) AS CPF_ROUTE_OWNER_RANK
    FROM ADM_BUTTON b
    WHERE API_PATTERN IS NOT NULL
) route_owner
WHERE CPF_ROUTE_OWNER_RANK = 1
ON CONFLICT (API_PERMISSION_ID) DO UPDATE SET API_GROUP_CODE=EXCLUDED.API_GROUP_CODE, HTTP_METHOD=EXCLUDED.HTTP_METHOD, API_PATH=EXCLUDED.API_PATH, API_NAME=EXCLUDED.API_NAME, PERMISSION_CODE=EXCLUDED.PERMISSION_CODE, MENU_ID=EXCLUDED.MENU_ID, BUTTON_ID=EXCLUDED.BUTTON_ID, USE_YN=EXCLUDED.USE_YN, updated_by=EXCLUDED.updated_by, updated_at=CURRENT_TIMESTAMP;

INSERT INTO ADM_API_PERMISSION (API_PERMISSION_ID, API_GROUP_CODE, HTTP_METHOD, API_PATH, API_NAME, PERMISSION_CODE, MENU_ID, BUTTON_ID, USE_YN, created_by, updated_by)
VALUES (
    'API_PERMISSION_WRITE_PUT', 'PERMISSION', 'PUT', '/adm/api/permissions/**', '권한 변경', 'WRITE',
    'PERMISSION', 'PERMISSION_WRITE', 'Y', 'SYSTEM', 'SYSTEM'
)
ON CONFLICT (API_PERMISSION_ID) DO UPDATE SET API_GROUP_CODE=EXCLUDED.API_GROUP_CODE, HTTP_METHOD=EXCLUDED.HTTP_METHOD, API_PATH=EXCLUDED.API_PATH, API_NAME=EXCLUDED.API_NAME, PERMISSION_CODE=EXCLUDED.PERMISSION_CODE, MENU_ID=EXCLUDED.MENU_ID, BUTTON_ID=EXCLUDED.BUTTON_ID, USE_YN=EXCLUDED.USE_YN, updated_by=EXCLUDED.updated_by, updated_at=CURRENT_TIMESTAMP;

INSERT INTO ADM_ROLE_API_PERMISSION (ROLE_ID, API_PERMISSION_ID, ALLOW_YN, created_by, updated_by)
SELECT rb.ROLE_ID,
       ap.API_PERMISSION_ID,
       CASE WHEN MAX(rb.ALLOW_YN) = 'Y' THEN 'Y' ELSE 'N' END,
       'SYSTEM',
       'SYSTEM'
FROM ADM_ROLE_BUTTON rb
JOIN ADM_BUTTON b ON b.BUTTON_ID = rb.BUTTON_ID
JOIN ADM_API_PERMISSION ap
  ON ap.HTTP_METHOD = COALESCE(b.HTTP_METHOD, 'ANY')
 AND ap.API_PATH = b.API_PATTERN
WHERE b.API_PATTERN IS NOT NULL
GROUP BY rb.ROLE_ID, ap.API_PERMISSION_ID
ON CONFLICT (ROLE_ID, API_PERMISSION_ID) DO UPDATE SET ALLOW_YN=EXCLUDED.ALLOW_YN, updated_by=EXCLUDED.updated_by, updated_at=CURRENT_TIMESTAMP;

INSERT INTO ADM_BUTTON (BUTTON_ID, MENU_ID, ACTION_CODE, BUTTON_NAME, HTTP_METHOD, API_PATTERN, SORT_ORDER, USE_YN, created_by, updated_by)
VALUES ('AUDIT_LOG_RETRY','AUDIT_LOG','WRITE','감사 전달 재처리','POST','/adm/api/audit-logs/deliveries/*/retry',20,'Y','SYSTEM','SYSTEM')
ON CONFLICT (BUTTON_ID) DO UPDATE SET ACTION_CODE=EXCLUDED.ACTION_CODE, BUTTON_NAME=EXCLUDED.BUTTON_NAME, HTTP_METHOD=EXCLUDED.HTTP_METHOD, API_PATTERN=EXCLUDED.API_PATTERN, USE_YN=EXCLUDED.USE_YN, updated_by=EXCLUDED.updated_by, updated_at=CURRENT_TIMESTAMP;

UPDATE ADM_ROLE_MENU SET WRITE_YN='Y',updated_by='SYSTEM',updated_at=CURRENT_TIMESTAMP WHERE MENU_ID='AUDIT_LOG' AND ROLE_ID IN ('ADM_ADMIN','ADM_DEV_OPERATOR');

INSERT INTO ADM_ROLE_BUTTON (ROLE_ID, BUTTON_ID, ALLOW_YN, created_by, updated_by)
SELECT ROLE_ID,'AUDIT_LOG_RETRY','Y','SYSTEM','SYSTEM' FROM ADM_ROLE WHERE ROLE_ID IN ('ADM_ADMIN','ADM_DEV_OPERATOR')
ON CONFLICT (ROLE_ID, BUTTON_ID) DO UPDATE SET ALLOW_YN='Y', updated_by='SYSTEM', updated_at=CURRENT_TIMESTAMP;

INSERT INTO ADM_API_PERMISSION (API_PERMISSION_ID, API_GROUP_CODE, HTTP_METHOD, API_PATH, API_NAME, PERMISSION_CODE, MENU_ID, BUTTON_ID, USE_YN, created_by, updated_by)
VALUES ('API_AUDIT_LOG_RETRY','AUDIT_LOG','POST','/adm/api/audit-logs/deliveries/*/retry','감사 전달 재처리','WRITE','AUDIT_LOG','AUDIT_LOG_RETRY','Y','SYSTEM','SYSTEM')
ON CONFLICT (API_PERMISSION_ID) DO UPDATE SET HTTP_METHOD=EXCLUDED.HTTP_METHOD, API_PATH=EXCLUDED.API_PATH, PERMISSION_CODE=EXCLUDED.PERMISSION_CODE, BUTTON_ID=EXCLUDED.BUTTON_ID, USE_YN=EXCLUDED.USE_YN, updated_by=EXCLUDED.updated_by, updated_at=CURRENT_TIMESTAMP;

INSERT INTO ADM_ROLE_API_PERMISSION (ROLE_ID, API_PERMISSION_ID, ALLOW_YN, created_by, updated_by)
SELECT ROLE_ID,'API_AUDIT_LOG_RETRY','Y','SYSTEM','SYSTEM' FROM ADM_ROLE WHERE ROLE_ID IN ('ADM_ADMIN','ADM_DEV_OPERATOR')
ON CONFLICT (ROLE_ID, API_PERMISSION_ID) DO UPDATE SET ALLOW_YN='Y', updated_by='SYSTEM', updated_at=CURRENT_TIMESTAMP;

INSERT INTO ADM_BUTTON (BUTTON_ID, MENU_ID, ACTION_CODE, BUTTON_NAME, HTTP_METHOD, API_PATTERN, SORT_ORDER, USE_YN, created_by, updated_by)
VALUES ('SECRET_READ','SECRET','READ','Secret Metadata 조회','GET','/adm/api/secrets/**',10,'Y','SYSTEM','SYSTEM'),
 ('SECRET_ROTATE','SECRET','ROTATE','Secret Rotation','POST','/adm/api/secrets/rotate',20,'Y','SYSTEM','SYSTEM')
ON CONFLICT (BUTTON_ID) DO UPDATE SET ACTION_CODE=EXCLUDED.ACTION_CODE, BUTTON_NAME=EXCLUDED.BUTTON_NAME, HTTP_METHOD=EXCLUDED.HTTP_METHOD, API_PATTERN=EXCLUDED.API_PATTERN, USE_YN='Y', updated_by='SYSTEM', updated_at=CURRENT_TIMESTAMP;

INSERT INTO ADM_ROLE_MENU (ROLE_ID, MENU_ID, READ_YN, WRITE_YN, DELETE_YN, created_by, updated_by)
VALUES ('ADM_ADMIN','SECRET','Y','Y','N','SYSTEM','SYSTEM'),
 ('ADM_DEV_OPERATOR','SECRET','Y','N','N','SYSTEM','SYSTEM'),
 ('ADM_OPERATOR','SECRET','Y','N','N','SYSTEM','SYSTEM'),
 ('ADM_VIEWER','SECRET','N','N','N','SYSTEM','SYSTEM'),
 ('ADM_BIZ_OPERATOR','SECRET','N','N','N','SYSTEM','SYSTEM')
ON CONFLICT (ROLE_ID, MENU_ID) DO UPDATE SET READ_YN=EXCLUDED.READ_YN, WRITE_YN=EXCLUDED.WRITE_YN, DELETE_YN=EXCLUDED.DELETE_YN, updated_by='SYSTEM', updated_at=CURRENT_TIMESTAMP;

INSERT INTO ADM_ROLE_BUTTON (ROLE_ID, BUTTON_ID, ALLOW_YN, created_by, updated_by)
VALUES ('ADM_ADMIN','SECRET_READ','Y','SYSTEM','SYSTEM'),('ADM_ADMIN','SECRET_ROTATE','Y','SYSTEM','SYSTEM'),
 ('ADM_DEV_OPERATOR','SECRET_READ','Y','SYSTEM','SYSTEM'),('ADM_DEV_OPERATOR','SECRET_ROTATE','N','SYSTEM','SYSTEM'),
 ('ADM_OPERATOR','SECRET_READ','Y','SYSTEM','SYSTEM'),('ADM_OPERATOR','SECRET_ROTATE','N','SYSTEM','SYSTEM')
ON CONFLICT (ROLE_ID, BUTTON_ID) DO UPDATE SET ALLOW_YN=EXCLUDED.ALLOW_YN, updated_by='SYSTEM', updated_at=CURRENT_TIMESTAMP;

INSERT INTO ADM_API_PERMISSION (API_PERMISSION_ID, API_GROUP_CODE, HTTP_METHOD, API_PATH, API_NAME, PERMISSION_CODE, MENU_ID, BUTTON_ID, USE_YN, created_by, updated_by)
VALUES ('API_SECRET_READ','SECRET','GET','/adm/api/secrets/**','Secret Metadata 조회','READ','SECRET','SECRET_READ','Y','SYSTEM','SYSTEM'),
 ('API_SECRET_ROTATE','SECRET','POST','/adm/api/secrets/rotate','Secret Rotation','ROTATE','SECRET','SECRET_ROTATE','Y','SYSTEM','SYSTEM')
ON CONFLICT (API_PERMISSION_ID) DO UPDATE SET API_PATH=EXCLUDED.API_PATH, API_NAME=EXCLUDED.API_NAME, PERMISSION_CODE=EXCLUDED.PERMISSION_CODE, BUTTON_ID=EXCLUDED.BUTTON_ID, USE_YN='Y', updated_by='SYSTEM', updated_at=CURRENT_TIMESTAMP;

INSERT INTO ADM_ROLE_API_PERMISSION (ROLE_ID, API_PERMISSION_ID, ALLOW_YN, created_by, updated_by)
VALUES ('ADM_ADMIN','API_SECRET_READ','Y','SYSTEM','SYSTEM'),('ADM_ADMIN','API_SECRET_ROTATE','Y','SYSTEM','SYSTEM'),
 ('ADM_DEV_OPERATOR','API_SECRET_READ','Y','SYSTEM','SYSTEM'),('ADM_DEV_OPERATOR','API_SECRET_ROTATE','N','SYSTEM','SYSTEM'),
 ('ADM_OPERATOR','API_SECRET_READ','Y','SYSTEM','SYSTEM'),('ADM_OPERATOR','API_SECRET_ROTATE','N','SYSTEM','SYSTEM')
ON CONFLICT (ROLE_ID, API_PERMISSION_ID) DO UPDATE SET ALLOW_YN=EXCLUDED.ALLOW_YN, updated_by='SYSTEM', updated_at=CURRENT_TIMESTAMP;

INSERT INTO ADM_BUTTON (BUTTON_ID, MENU_ID, ACTION_CODE, BUTTON_NAME, HTTP_METHOD, API_PATTERN, SORT_ORDER, USE_YN, created_by, updated_by)
VALUES ('BAT_RUNTIME_VIEW','BATCH_RUNTIME','RUNTIME_VIEW','Runtime 조회','GET','/adm/api/batch-runtime/**',10,'Y','SYSTEM','SYSTEM'),
 ('BAT_RUNTIME_OPERATE','BATCH_INSTANCES','RUNTIME_OPERATE','Runtime Start/Stop/Drain','POST','/adm/api/approvals/**',20,'Y','SYSTEM','SYSTEM'),
 ('BAT_JOB_OPERATE','BATCH_EXECUTIONS','JOB_OPERATE','Job 실행/중지/재처리','POST','/adm/api/batch/**',30,'Y','SYSTEM','SYSTEM'),
 ('BAT_SCHEDULE_OPERATE','BATCH_SCHEDULER','SCHEDULE_OPERATE','Scheduler 운영','POST','/adm/api/batch/**',40,'Y','SYSTEM','SYSTEM'),
 ('BAT_WORKER_OPERATE','BATCH_WORKER_POOLS','WORKER_OPERATE','Worker Pool 운영','POST','/adm/api/approvals/**',50,'Y','SYSTEM','SYSTEM'),
 ('BAT_CENTER_CUT_OPERATE','BATCH_CENTER_CUT','CENTER_CUT_OPERATE','Center-Cut 재처리/조정','POST','/adm/api/batch-runtime/**',60,'Y','SYSTEM','SYSTEM'),
 ('BAT_AGENT_OPERATE','BATCH_AGENTS','AGENT_OPERATE','Host Agent 운영','POST','/adm/api/approvals/**',70,'Y','SYSTEM','SYSTEM'),
 ('BAT_DEPLOY_PLAN','BATCH_DEPLOYMENT','DEPLOY_PLAN','Deployment Plan 생성','POST','/adm/api/batch-runtime/deployment-plans',80,'Y','SYSTEM','SYSTEM'),
 ('BAT_DEPLOY_APPROVE','BATCH_DEPLOYMENT','DEPLOY_APPROVE','Deployment 승인','POST','/adm/api/approvals/**',90,'Y','SYSTEM','SYSTEM'),
 ('BAT_DEPLOY_EXECUTE','BATCH_DEPLOYMENT','DEPLOY_EXECUTE','Deployment 실행','POST','/adm/api/approvals/**',100,'Y','SYSTEM','SYSTEM'),
 ('BAT_ROLLBACK_EXECUTE','BATCH_DEPLOYMENT','ROLLBACK_EXECUTE','Rollback 실행','POST','/adm/api/approvals/**',110,'Y','SYSTEM','SYSTEM'),
 ('BAT_RECOVERY_OPERATE','BATCH_RECOVERY','RECOVERY_OPERATE','UNKNOWN_RESULT 조정','POST','/adm/api/batch-runtime/**',120,'Y','SYSTEM','SYSTEM'),
 ('BAT_SECURITY_AUDIT','BATCH_AUDIT','SECURITY_AUDIT','BAT 보안·감사 조회','GET','/adm/api/batch-runtime/views/audit',130,'Y','SYSTEM','SYSTEM'),
 ('BAT_EVIDENCE_DOWNLOAD','BATCH_AUDIT','EVIDENCE_DOWNLOAD','BAT Evidence 다운로드','GET','/adm/api/downloads/**',140,'Y','SYSTEM','SYSTEM'),
 ('BAT_RETENTION_VIEW','BATCH_RUNTIME','RETENTION_VIEW','Retention 조회','GET','/adm/api/batch-runtime/retention/**',150,'Y','SYSTEM','SYSTEM'),
 ('BAT_RETENTION_PREVIEW','BATCH_RUNTIME','RETENTION_PREVIEW','Retention Preview','POST','/adm/api/batch-runtime/retention/preview',160,'Y','SYSTEM','SYSTEM'),
 ('BAT_RETENTION_POLICY_REQUEST','BATCH_RUNTIME','RETENTION_POLICY_REQUEST','Retention 정책 변경 승인요청','POST','/adm/api/batch-runtime/retention/policies',170,'Y','SYSTEM','SYSTEM'),
 ('BAT_RETENTION_RUN_REQUEST','BATCH_RUNTIME','RETENTION_RUN_REQUEST','Retention 수동 실행 승인요청','POST','/adm/api/batch-runtime/retention/policies/*/run',180,'Y','SYSTEM','SYSTEM'),
 ('BAT_RETENTION_RUN_PAUSE','BATCH_RUNTIME','RETENTION_RUN_PAUSE','Retention Run 안전 일시정지','POST','/adm/api/batch-runtime/retention/runs/*/pause',190,'Y','SYSTEM','SYSTEM'),
 ('BAT_RETENTION_RUN_RESUME','BATCH_RUNTIME','RETENTION_RUN_RESUME','Retention Run 재개 승인요청','POST','/adm/api/batch-runtime/retention/runs/*/resume',200,'Y','SYSTEM','SYSTEM'),
 ('BAT_RETENTION_POLICY_PAUSE','BATCH_RUNTIME','RETENTION_POLICY_PAUSE','Retention 정책 안전 일시정지','POST','/adm/api/batch-runtime/retention/policies/*/pause',210,'Y','SYSTEM','SYSTEM'),
 ('BAT_RETENTION_POLICY_RESUME','BATCH_RUNTIME','RETENTION_POLICY_RESUME','Retention 정책 재개 승인요청','POST','/adm/api/batch-runtime/retention/policies/*/resume',220,'Y','SYSTEM','SYSTEM')
ON CONFLICT (BUTTON_ID) DO UPDATE SET MENU_ID=EXCLUDED.MENU_ID, ACTION_CODE=EXCLUDED.ACTION_CODE, BUTTON_NAME=EXCLUDED.BUTTON_NAME, HTTP_METHOD=EXCLUDED.HTTP_METHOD, API_PATTERN=EXCLUDED.API_PATTERN, SORT_ORDER=EXCLUDED.SORT_ORDER, USE_YN='Y', updated_by='SYSTEM', updated_at=CURRENT_TIMESTAMP;

INSERT INTO ADM_ROLE_MENU (ROLE_ID, MENU_ID, READ_YN, WRITE_YN, DELETE_YN, created_by, updated_by)
SELECT r.ROLE_ID,m.MENU_ID,'Y',
       CASE WHEN r.ROLE_ID IN ('ADM_ADMIN','ADM_DEV_OPERATOR','ADM_OPERATOR') THEN 'Y' ELSE 'N' END,
       'N','SYSTEM','SYSTEM'
FROM ADM_ROLE r JOIN ADM_MENU m ON m.PARENT_MENU_ID='BATCH'
WHERE r.ROLE_ID IN ('ADM_ADMIN','ADM_DEV_OPERATOR','ADM_OPERATOR','ADM_BIZ_OPERATOR','ADM_VIEWER')
ON CONFLICT (ROLE_ID, MENU_ID) DO UPDATE SET READ_YN=EXCLUDED.READ_YN, WRITE_YN=EXCLUDED.WRITE_YN, DELETE_YN=EXCLUDED.DELETE_YN, updated_by='SYSTEM', updated_at=CURRENT_TIMESTAMP;

INSERT INTO ADM_ROLE_BUTTON (ROLE_ID, BUTTON_ID, ALLOW_YN, created_by, updated_by)
SELECT r.ROLE_ID,b.BUTTON_ID,
       CASE
         WHEN r.ROLE_ID='ADM_ADMIN' THEN 'Y'
         WHEN r.ROLE_ID IN ('ADM_DEV_OPERATOR','ADM_OPERATOR') AND b.BUTTON_ID NOT IN ('BAT_DEPLOY_APPROVE','BAT_DEPLOY_EXECUTE','BAT_ROLLBACK_EXECUTE') THEN 'Y'
         WHEN r.ROLE_ID='ADM_BIZ_OPERATOR' AND b.BUTTON_ID IN ('BAT_RUNTIME_VIEW','BAT_JOB_OPERATE','BAT_WORKER_OPERATE','BAT_CENTER_CUT_OPERATE','BAT_SECURITY_AUDIT','BAT_EVIDENCE_DOWNLOAD') THEN 'Y'
         WHEN r.ROLE_ID='ADM_VIEWER' AND b.BUTTON_ID IN ('BAT_RUNTIME_VIEW','BAT_SECURITY_AUDIT') THEN 'Y'
         ELSE 'N' END,
       'SYSTEM','SYSTEM'
FROM ADM_ROLE r JOIN ADM_BUTTON b ON b.BUTTON_ID LIKE 'BAT_%'
WHERE r.ROLE_ID IN ('ADM_ADMIN','ADM_DEV_OPERATOR','ADM_OPERATOR','ADM_BIZ_OPERATOR','ADM_VIEWER')
ON CONFLICT (ROLE_ID, BUTTON_ID) DO UPDATE SET ALLOW_YN=EXCLUDED.ALLOW_YN, updated_by='SYSTEM', updated_at=CURRENT_TIMESTAMP;

INSERT INTO ADM_API_PERMISSION (API_PERMISSION_ID, API_GROUP_CODE, HTTP_METHOD, API_PATH, API_NAME, PERMISSION_CODE, MENU_ID, BUTTON_ID, USE_YN, created_by, updated_by)
SELECT
    CONCAT('API_', BUTTON_ID),
    MENU_ID,
    COALESCE(HTTP_METHOD, 'ANY'),
    API_PATTERN,
    BUTTON_NAME,
    ACTION_CODE,
    MENU_ID,
    BUTTON_ID,
    'Y',
    'SYSTEM',
    'SYSTEM'
FROM (
    SELECT b.*,
           ROW_NUMBER() OVER (
               PARTITION BY COALESCE(HTTP_METHOD, 'ANY'), API_PATTERN
               ORDER BY SORT_ORDER, BUTTON_ID
           ) AS CPF_ROUTE_OWNER_RANK
    FROM ADM_BUTTON b
    WHERE BUTTON_ID LIKE 'BAT_%'
      AND API_PATTERN IS NOT NULL
) route_owner
WHERE CPF_ROUTE_OWNER_RANK = 1
  AND NOT EXISTS (
      SELECT 1
      FROM ADM_API_PERMISSION existing
      WHERE existing.HTTP_METHOD = COALESCE(route_owner.HTTP_METHOD, 'ANY')
        AND existing.API_PATH = route_owner.API_PATTERN
  )
ON CONFLICT (API_PERMISSION_ID) DO UPDATE SET API_GROUP_CODE=EXCLUDED.API_GROUP_CODE, HTTP_METHOD=EXCLUDED.HTTP_METHOD, API_PATH=EXCLUDED.API_PATH, API_NAME=EXCLUDED.API_NAME, PERMISSION_CODE=EXCLUDED.PERMISSION_CODE, MENU_ID=EXCLUDED.MENU_ID, BUTTON_ID=EXCLUDED.BUTTON_ID, USE_YN='Y', updated_by='SYSTEM', updated_at=CURRENT_TIMESTAMP;

INSERT INTO ADM_ROLE_API_PERMISSION (ROLE_ID, API_PERMISSION_ID, ALLOW_YN, created_by, updated_by)
SELECT rb.ROLE_ID,
       ap.API_PERMISSION_ID,
       CASE WHEN MAX(rb.ALLOW_YN) = 'Y' THEN 'Y' ELSE 'N' END,
       'SYSTEM',
       'SYSTEM'
FROM ADM_ROLE_BUTTON rb
JOIN ADM_BUTTON b ON b.BUTTON_ID = rb.BUTTON_ID
JOIN ADM_API_PERMISSION ap
  ON ap.HTTP_METHOD = COALESCE(b.HTTP_METHOD, 'ANY')
 AND ap.API_PATH = b.API_PATTERN
WHERE rb.BUTTON_ID LIKE 'BAT_%'
  AND b.API_PATTERN IS NOT NULL
GROUP BY rb.ROLE_ID, ap.API_PERMISSION_ID
ON CONFLICT (ROLE_ID, API_PERMISSION_ID) DO UPDATE SET ALLOW_YN=EXCLUDED.ALLOW_YN, updated_by='SYSTEM', updated_at=CURRENT_TIMESTAMP;

INSERT INTO OPS_SERVICE (service_id, service_name, service_type, owner_module_code, description, use_yn, created_by, updated_by)
VALUES (
    'EDU', 'CPF 참조 서비스', 'INTERNAL', 'EDU', 'EDU 대외연계 결과 불명 검증 대상', 'Y', 'SEED', 'SEED'
)
ON CONFLICT (service_id) DO UPDATE SET service_name=EXCLUDED.service_name, owner_module_code=EXCLUDED.owner_module_code, description=EXCLUDED.description, use_yn=EXCLUDED.use_yn, updated_by=EXCLUDED.updated_by, updated_at=CURRENT_TIMESTAMP(3);

INSERT INTO OPS_SERVICE_ENDPOINT (endpoint_code, service_id, endpoint_name, endpoint_type, base_url, context_path, default_timeout_ms, default_retry_count, use_yn, created_by, updated_by)
VALUES (
    'EDU-EXTERNAL-SIMULATOR', 'EDU', 'EDU 대외 시뮬레이터', 'HTTP',
    'http://127.0.0.1:8099', '', 3000, 0, 'Y', 'SEED', 'SEED'
)
ON CONFLICT (endpoint_code) DO UPDATE SET service_id=EXCLUDED.service_id, endpoint_name=EXCLUDED.endpoint_name, endpoint_type=EXCLUDED.endpoint_type, base_url=EXCLUDED.base_url, context_path=EXCLUDED.context_path, default_timeout_ms=EXCLUDED.default_timeout_ms, default_retry_count=EXCLUDED.default_retry_count, use_yn=EXCLUDED.use_yn, updated_by=EXCLUDED.updated_by, updated_at=CURRENT_TIMESTAMP(3);

INSERT INTO OPS_SERVICE_INSTANCE (instance_id, service_id, endpoint_code, instance_name, base_url, host_name, port_no, instance_status, weight, active_yn, last_heartbeat_at, created_by, updated_by)
VALUES (
    'EDU-EXT-SIM-local-01', 'EDU', 'EDU-EXTERNAL-SIMULATOR', 'EDU 대외 시뮬레이터 인스턴스',
    'http://127.0.0.1:8099', 'localhost', 8099, 'UP', 100, 'Y', CURRENT_TIMESTAMP, 'SEED', 'SEED'
)
ON CONFLICT (instance_id) DO UPDATE SET service_id=EXCLUDED.service_id, endpoint_code=EXCLUDED.endpoint_code, instance_name=EXCLUDED.instance_name, base_url=EXCLUDED.base_url, host_name=EXCLUDED.host_name, port_no=EXCLUDED.port_no, instance_status=EXCLUDED.instance_status, active_yn=EXCLUDED.active_yn, last_heartbeat_at=EXCLUDED.last_heartbeat_at, updated_by=EXCLUDED.updated_by, updated_at=CURRENT_TIMESTAMP(3);

INSERT INTO OPS_SERVICE_ROUTING_POLICY (service_id, endpoint_code, routing_mode, load_balance_type, failover_enabled_yn, health_check_required_yn, active_yn, priority, created_by, updated_by)
VALUES (
    'EDU', 'EDU-EXTERNAL-SIMULATOR', 'PRIMARY', 'WEIGHT', 'N', 'N', 'Y', 100, 'SEED', 'SEED'
)
ON CONFLICT (service_id, endpoint_code, priority) DO UPDATE SET routing_mode=EXCLUDED.routing_mode, load_balance_type=EXCLUDED.load_balance_type, failover_enabled_yn=EXCLUDED.failover_enabled_yn, health_check_required_yn=EXCLUDED.health_check_required_yn, active_yn=EXCLUDED.active_yn, priority=EXCLUDED.priority, updated_by=EXCLUDED.updated_by, updated_at=CURRENT_TIMESTAMP(3);

INSERT INTO BAT_INSTANCE (instance_id, instance_name, host_name, server_port, active_yn, last_heartbeat_at, description, created_by, updated_by)
VALUES (
    'local-batch-01',
    '로컬 배치 인스턴스',
    'localhost',
    8099,
    'Y',
    CURRENT_TIMESTAMP,
    'EDU 배치와 ADM 관제 연동을 확인하는 로컬 인스턴스',
    'SYSTEM',
    'SYSTEM'
)
ON CONFLICT (instance_id) DO UPDATE SET instance_name=EXCLUDED.instance_name, host_name=EXCLUDED.host_name, server_port=EXCLUDED.server_port, active_yn=EXCLUDED.active_yn, last_heartbeat_at=EXCLUDED.last_heartbeat_at, description=EXCLUDED.description, updated_by=EXCLUDED.updated_by, updated_at=CURRENT_TIMESTAMP;

INSERT INTO BAT_WORKER (worker_id, instance_id, host_name, process_id, thread_name, worker_status, active_yn, last_heartbeat_at, current_job_id, current_execution_id, description, created_by, updated_by)
VALUES (
    'local-batch-01',
    'local-batch-01',
    'localhost',
    'seed',
    'seed-main',
    'IDLE',
    'Y',
    CURRENT_TIMESTAMP,
    NULL,
    NULL,
    '로컬 smoke 검증용 배치 worker heartbeat',
    'SYSTEM',
    'SYSTEM'
)
ON CONFLICT (worker_id) DO UPDATE SET instance_id=EXCLUDED.instance_id, host_name=EXCLUDED.host_name, process_id=EXCLUDED.process_id, thread_name=EXCLUDED.thread_name, worker_status=EXCLUDED.worker_status, active_yn=EXCLUDED.active_yn, last_heartbeat_at=EXCLUDED.last_heartbeat_at, current_job_id=EXCLUDED.current_job_id, current_execution_id=EXCLUDED.current_execution_id, description=EXCLUDED.description, updated_by=EXCLUDED.updated_by, updated_at=CURRENT_TIMESTAMP;

INSERT INTO BAT_JOB (job_id, job_name, job_type, description, restartable_yn, use_yn, created_by, updated_by)
VALUES ('CPF_EDU_TASKLET_JOB', 'CPF 교육 Tasklet Job', 'TASKLET', '배치 관제 수동 실행 샘플을 위한 Tasklet Job입니다.', 'Y', 'Y', 'SYSTEM', 'SYSTEM'),
    ('CPF_EDU_CHUNK_JOB', 'CPF 교육 Chunk Job', 'CHUNK', '대용량 읽기/처리/쓰기 샘플을 위한 Chunk Job입니다.', 'Y', 'Y', 'SYSTEM', 'SYSTEM'),
    ('CPF_EDU_RETRY_JOB', 'CPF 교육 재처리 Job', 'RETRY', '실패 재처리와 checkpoint/restart 교육을 위한 Job입니다.', 'Y', 'Y', 'SYSTEM', 'SYSTEM')
ON CONFLICT (job_id) DO UPDATE SET job_name=EXCLUDED.job_name, job_type=EXCLUDED.job_type, description=EXCLUDED.description, restartable_yn=EXCLUDED.restartable_yn, use_yn=EXCLUDED.use_yn, updated_by=EXCLUDED.updated_by, updated_at=CURRENT_TIMESTAMP;

INSERT INTO BAT_SCHEDULE (schedule_id, job_id, cron_expression, calendar_id, business_day_only_yn, holiday_policy, available_start_time, available_end_time, run_date_pattern, timezone, enabled_yn, created_by, updated_by)
VALUES ('CPF_EDU_TASKLET_DAILY', 'CPF_EDU_TASKLET_JOB', '0 0 2 * * *', 'DEFAULT', 'Y', 'SKIP', '02:00:00', '04:00:00', 'D+0', 'Asia/Seoul', 'N', 'SYSTEM', 'SYSTEM'),
    ('CPF_EDU_CHUNK_DAILY', 'CPF_EDU_CHUNK_JOB', '0 30 2 * * *', 'DEFAULT', 'Y', 'SKIP', '02:30:00', '05:30:00', 'D+0', 'Asia/Seoul', 'N', 'SYSTEM', 'SYSTEM')
ON CONFLICT (schedule_id) DO UPDATE SET job_id=EXCLUDED.job_id, cron_expression=EXCLUDED.cron_expression, calendar_id=EXCLUDED.calendar_id, business_day_only_yn=EXCLUDED.business_day_only_yn, holiday_policy=EXCLUDED.holiday_policy, available_start_time=EXCLUDED.available_start_time, available_end_time=EXCLUDED.available_end_time, run_date_pattern=EXCLUDED.run_date_pattern, timezone=EXCLUDED.timezone, enabled_yn=EXCLUDED.enabled_yn, updated_by=EXCLUDED.updated_by, updated_at=CURRENT_TIMESTAMP;

INSERT INTO BAT_JOB_RELATION (job_id, related_job_id, relation_type, trigger_condition, required_status, sort_order, use_yn, created_by, updated_by)
VALUES ('CPF_EDU_CHUNK_JOB', 'CPF_EDU_TASKLET_JOB', 'PREDECESSOR', 'COMPLETED', 'COMPLETED', 10, 'Y', 'SYSTEM', 'SYSTEM'),
    ('CPF_EDU_TASKLET_JOB', 'CPF_EDU_CHUNK_JOB', 'TRIGGER', 'COMPLETED', 'COMPLETED', 20, 'Y', 'SYSTEM', 'SYSTEM')
ON CONFLICT (job_id, related_job_id, relation_type) DO UPDATE SET trigger_condition=EXCLUDED.trigger_condition, required_status=EXCLUDED.required_status, sort_order=EXCLUDED.sort_order, use_yn=EXCLUDED.use_yn, updated_by=EXCLUDED.updated_by, updated_at=CURRENT_TIMESTAMP;

INSERT INTO BAT_EXECUTION (job_id, schedule_id, job_parameters, execution_status, batch_instance_id, instance_id, worker_id, transaction_id, start_time, end_time, read_count, write_count, skip_count, requested_by, created_by, updated_by)
SELECT
    'CPF_EDU_TASKLET_JOB',
    'CPF_EDU_TASKLET_DAILY',
    '{"edu":true}',
    'COMPLETED',
    'local-batch-01',
    'local-batch-01',
    'local-batch-01',
    '20260615120000000REFlocal010000001',
    (CURRENT_TIMESTAMP - INTERVAL '10 minute'),
    (CURRENT_TIMESTAMP - INTERVAL '9 minute'),
    1,
    1,
    0,
    'SYSTEM',
    'SYSTEM',
    'SYSTEM'
WHERE NOT EXISTS (
    SELECT 1
    FROM BAT_EXECUTION
    WHERE job_id = 'CPF_EDU_TASKLET_JOB'
      AND requested_by = 'SYSTEM'
      AND job_parameters = '{"edu":true}'
);

-- CPF_SEED_INLINE_VARIABLE cpf_edu_execution_id

INSERT INTO BAT_STEP_EXECUTION (execution_id, spring_batch_step_execution_id, worker_id, step_name, execution_status, start_time, end_time, read_count, write_count, skip_count, step_log, created_by, updated_by)
SELECT (
    SELECT execution_id
    FROM BAT_EXECUTION
    WHERE job_id = 'CPF_EDU_TASKLET_JOB'
      AND requested_by = 'SYSTEM'
      AND job_parameters = '{"edu":true}'
    ORDER BY execution_id
    FETCH FIRST 1 ROW ONLY
), NULL, 'local-batch-01', 'CPF_EDU_TASKLET_STEP', 'COMPLETED', (CURRENT_TIMESTAMP - INTERVAL '10 minute'), (CURRENT_TIMESTAMP - INTERVAL '9 minute'), 1, 1, 0, 'Tasklet 교육 실행 정상 완료', 'SYSTEM', 'SYSTEM'
WHERE (
    SELECT execution_id
    FROM BAT_EXECUTION
    WHERE job_id = 'CPF_EDU_TASKLET_JOB'
      AND requested_by = 'SYSTEM'
      AND job_parameters = '{"edu":true}'
    ORDER BY execution_id
    FETCH FIRST 1 ROW ONLY
) IS NOT NULL
  AND NOT EXISTS (
      SELECT 1
      FROM BAT_STEP_EXECUTION
      WHERE execution_id = (
    SELECT execution_id
    FROM BAT_EXECUTION
    WHERE job_id = 'CPF_EDU_TASKLET_JOB'
      AND requested_by = 'SYSTEM'
      AND job_parameters = '{"edu":true}'
    ORDER BY execution_id
    FETCH FIRST 1 ROW ONLY
)
        AND step_name = 'CPF_EDU_TASKLET_STEP'
  );

INSERT INTO BAT_EXECUTION_TARGET (execution_id, job_id, schedule_id, target_instance_id, business_date, planned_run_at, dispatch_status, dispatch_reason, created_by, updated_by)
SELECT
    (
    SELECT execution_id
    FROM BAT_EXECUTION
    WHERE job_id = 'CPF_EDU_TASKLET_JOB'
      AND requested_by = 'SYSTEM'
      AND job_parameters = '{"edu":true}'
    ORDER BY execution_id
    FETCH FIRST 1 ROW ONLY
),
    'CPF_EDU_TASKLET_JOB',
    'CPF_EDU_TASKLET_DAILY',
    'local-batch-01',
    CURRENT_DATE,
    CURRENT_DATE + TIME '02:00:00',
    'DONE',
    '로컬 smoke 검증용 완료 대상',
    'SYSTEM',
    'SYSTEM'
WHERE (
    SELECT execution_id
    FROM BAT_EXECUTION
    WHERE job_id = 'CPF_EDU_TASKLET_JOB'
      AND requested_by = 'SYSTEM'
      AND job_parameters = '{"edu":true}'
    ORDER BY execution_id
    FETCH FIRST 1 ROW ONLY
) IS NOT NULL
  AND NOT EXISTS (
      SELECT 1
      FROM BAT_EXECUTION_TARGET
      WHERE job_id = 'CPF_EDU_TASKLET_JOB'
        AND business_date = CURRENT_DATE
        AND target_instance_id = 'local-batch-01'
  );

INSERT INTO CMN_BUSINESS_CALENDAR_DAY (calendar_id, business_date, business_day_yn, day_type, institution_code, reason, created_by, updated_by)
VALUES ('DEFAULT', CURRENT_DATE, 'Y', 'BUSINESS', NULL, '로컬 smoke 검증용 기본 영업일', 'SYSTEM', 'SYSTEM'),
    ('DEFAULT', (CURRENT_DATE + INTERVAL '1 day'), 'Y', 'BUSINESS', NULL, '로컬 smoke 검증용 다음 영업일', 'SYSTEM', 'SYSTEM')
ON CONFLICT (calendar_id, business_date) DO UPDATE SET business_day_yn=EXCLUDED.business_day_yn, day_type=EXCLUDED.day_type, institution_code=EXCLUDED.institution_code, reason=EXCLUDED.reason, updated_by=EXCLUDED.updated_by, updated_at=CURRENT_TIMESTAMP;

INSERT INTO BAT_JOB (job_id, job_name, job_type, description, restartable_yn, use_yn, created_by, updated_by)
VALUES (
    'CPF_BAT_CENTER_CUT_JOB',
    'CPF BAT 센터컷 smoke Job',
    'TASKLET',
    'BAT standalone에서 center-cut provider/handler 기본 흐름을 검증하는 Job입니다.',
    'Y',
    'Y',
    'SYSTEM',
    'SYSTEM'
)
ON CONFLICT (job_id) DO UPDATE SET job_name=EXCLUDED.job_name, job_type=EXCLUDED.job_type, description=EXCLUDED.description, restartable_yn=EXCLUDED.restartable_yn, use_yn=EXCLUDED.use_yn, updated_by=EXCLUDED.updated_by, updated_at=CURRENT_TIMESTAMP;

INSERT INTO BAT_JOB (job_id, job_name, job_type, description, restartable_yn, use_yn, created_by, updated_by)
VALUES (
    'CPF_REF_CENTER_CUT_SAMPLE_JOB',
    'CPF REF 업무 DB 센터컷 샘플 Job',
    'TASKLET',
    'REF 업무 DB adapter를 통해 center-cut target/result 흐름을 검증하는 Job입니다.',
    'Y',
    'Y',
    'SYSTEM',
    'SYSTEM'
)
ON CONFLICT (job_id) DO UPDATE SET job_name=EXCLUDED.job_name, job_type=EXCLUDED.job_type, description=EXCLUDED.description, restartable_yn=EXCLUDED.restartable_yn, use_yn=EXCLUDED.use_yn, updated_by=EXCLUDED.updated_by, updated_at=CURRENT_TIMESTAMP;

INSERT INTO BAT_CENTER_CUT_JOB (center_cut_job_id, batch_job_id, center_cut_job_name, provider_key, handler_key, chunk_size, retry_limit, use_yn, description, created_by, updated_by)
VALUES (
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
ON CONFLICT (center_cut_job_id) DO UPDATE SET batch_job_id=EXCLUDED.batch_job_id, center_cut_job_name=EXCLUDED.center_cut_job_name, provider_key=EXCLUDED.provider_key, handler_key=EXCLUDED.handler_key, chunk_size=EXCLUDED.chunk_size, retry_limit=EXCLUDED.retry_limit, use_yn=EXCLUDED.use_yn, description=EXCLUDED.description, updated_by=EXCLUDED.updated_by, updated_at=CURRENT_TIMESTAMP;

INSERT INTO BAT_CENTER_CUT_JOB (center_cut_job_id, batch_job_id, center_cut_job_name, provider_key, handler_key, chunk_size, retry_limit, use_yn, description, created_by, updated_by)
VALUES (
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
ON CONFLICT (center_cut_job_id) DO UPDATE SET batch_job_id=EXCLUDED.batch_job_id, center_cut_job_name=EXCLUDED.center_cut_job_name, provider_key=EXCLUDED.provider_key, handler_key=EXCLUDED.handler_key, chunk_size=EXCLUDED.chunk_size, retry_limit=EXCLUDED.retry_limit, use_yn=EXCLUDED.use_yn, description=EXCLUDED.description, updated_by=EXCLUDED.updated_by, updated_at=CURRENT_TIMESTAMP;

INSERT INTO BAT_CENTER_CUT_PARAMETER (center_cut_job_id, parameter_key, parameter_value, encrypted_yn, use_yn, created_by, updated_by)
VALUES ('CPF_BAT_CENTER_CUT_JOB', 'businessDatePattern', 'D+0', 'N', 'Y', 'SYSTEM', 'SYSTEM'),
    ('CPF_BAT_CENTER_CUT_JOB', 'defaultLimit', '10', 'N', 'Y', 'SYSTEM', 'SYSTEM')
ON CONFLICT (center_cut_job_id, parameter_key) DO UPDATE SET parameter_value=EXCLUDED.parameter_value, encrypted_yn=EXCLUDED.encrypted_yn, use_yn=EXCLUDED.use_yn, updated_by=EXCLUDED.updated_by, updated_at=CURRENT_TIMESTAMP;

INSERT INTO OPS_SERVICE (service_id, service_name, service_type, owner_module_code, description, use_yn, created_by, updated_by)
VALUES ('MBW', '업무 백오피스 서비스', 'INTERNAL', 'MBW', 'CPF 업무 운영 백오피스 서비스 호출 대상', 'Y', 'SYSTEM', 'SYSTEM'),
    ('EDU', '온라인 교육 서비스', 'INTERNAL', 'EDU', 'CPF 온라인 교육 및 검증 서비스 호출 대상', 'Y', 'SYSTEM', 'SYSTEM'),
    ('BAT', '배치 Worker 서비스', 'INTERNAL', 'BAT', 'CPF 배치 Worker 서비스 호출 대상', 'Y', 'SYSTEM', 'SYSTEM'),
    ('ADM', '운영 콘솔 서비스', 'INTERNAL', 'ADM', 'CPF 운영 콘솔 서비스 호출 대상', 'Y', 'SYSTEM', 'SYSTEM')
ON CONFLICT (service_id) DO UPDATE SET service_name=EXCLUDED.service_name, service_type=EXCLUDED.service_type, owner_module_code=EXCLUDED.owner_module_code, description=EXCLUDED.description, use_yn=EXCLUDED.use_yn, updated_by=EXCLUDED.updated_by, updated_at=CURRENT_TIMESTAMP;

INSERT INTO OPS_SERVICE_ENDPOINT (endpoint_code, service_id, endpoint_name, endpoint_type, base_url, context_path, default_timeout_ms, default_retry_count, use_yn, created_by, updated_by)
VALUES ('MBW_API', 'MBW', 'MBW API Endpoint', 'HTTP', 'http://cpf-backoffice', '/api/v1/backoffice', 3000, 0, 'Y', 'SYSTEM', 'SYSTEM'),
    ('EDU_API', 'EDU', 'EDU API Endpoint', 'HTTP', 'http://cpf-education', '/education', 3000, 0, 'Y', 'SYSTEM', 'SYSTEM'),
    ('BAT_API', 'BAT', 'BAT API Endpoint', 'HTTP', 'http://cpf-batch', '/bat', 5000, 0, 'Y', 'SYSTEM', 'SYSTEM'),
    ('ADM_API', 'ADM', 'ADM API Endpoint', 'HTTP', 'http://cpf-admin', '/adm', 3000, 0, 'Y', 'SYSTEM', 'SYSTEM')
ON CONFLICT (endpoint_code) DO UPDATE SET service_id=EXCLUDED.service_id, endpoint_name=EXCLUDED.endpoint_name, endpoint_type=EXCLUDED.endpoint_type, base_url=EXCLUDED.base_url, context_path=EXCLUDED.context_path, default_timeout_ms=EXCLUDED.default_timeout_ms, default_retry_count=EXCLUDED.default_retry_count, use_yn=EXCLUDED.use_yn, updated_by=EXCLUDED.updated_by, updated_at=CURRENT_TIMESTAMP;

INSERT INTO OPS_SERVICE_INSTANCE (instance_id, service_id, endpoint_code, instance_name, base_url, host_name, port_no, instance_status, weight, active_yn, last_heartbeat_at, created_by, updated_by)
VALUES ('MBW-local-01', 'MBW', 'MBW_API', 'MBW local instance', 'http://localhost:8091', 'localhost', 8091, 'UP', 100, 'Y', CURRENT_TIMESTAMP, 'SYSTEM', 'SYSTEM'),
    ('EDU-local-01', 'EDU', 'EDU_API', 'EDU local instance', 'http://localhost:8099', 'localhost', 8099, 'UP', 100, 'Y', CURRENT_TIMESTAMP, 'SYSTEM', 'SYSTEM'),
    ('BAT-local-01', 'BAT', 'BAT_API', 'BAT local instance', 'http://localhost:8093', 'localhost', 8093, 'UP', 100, 'Y', CURRENT_TIMESTAMP, 'SYSTEM', 'SYSTEM'),
    ('ADM-local-01', 'ADM', 'ADM_API', 'ADM local instance', 'http://localhost:8090', 'localhost', 8090, 'UP', 100, 'Y', CURRENT_TIMESTAMP, 'SYSTEM', 'SYSTEM')
ON CONFLICT (instance_id) DO UPDATE SET service_id=EXCLUDED.service_id, endpoint_code=EXCLUDED.endpoint_code, instance_name=EXCLUDED.instance_name, base_url=EXCLUDED.base_url, host_name=EXCLUDED.host_name, port_no=EXCLUDED.port_no, instance_status=EXCLUDED.instance_status, weight=EXCLUDED.weight, active_yn=EXCLUDED.active_yn, last_heartbeat_at=EXCLUDED.last_heartbeat_at, updated_by=EXCLUDED.updated_by, updated_at=CURRENT_TIMESTAMP;

INSERT INTO OPS_SERVICE_ROUTING_POLICY (service_id, endpoint_code, routing_mode, load_balance_type, failover_enabled_yn, health_check_required_yn, active_yn, priority, created_by, updated_by)
VALUES ('MBW', 'MBW_API', 'PRIMARY', 'WEIGHT', 'Y', 'Y', 'Y', 100, 'SYSTEM', 'SYSTEM'),
    ('EDU', 'EDU_API', 'PRIMARY', 'WEIGHT', 'Y', 'Y', 'Y', 100, 'SYSTEM', 'SYSTEM'),
    ('BAT', 'BAT_API', 'PRIMARY', 'WEIGHT', 'Y', 'Y', 'Y', 100, 'SYSTEM', 'SYSTEM'),
    ('ADM', 'ADM_API', 'PRIMARY', 'WEIGHT', 'Y', 'Y', 'Y', 100, 'SYSTEM', 'SYSTEM')
ON CONFLICT (service_id, endpoint_code, priority) DO UPDATE SET routing_mode=EXCLUDED.routing_mode, load_balance_type=EXCLUDED.load_balance_type, failover_enabled_yn=EXCLUDED.failover_enabled_yn, health_check_required_yn=EXCLUDED.health_check_required_yn, active_yn=EXCLUDED.active_yn, updated_by=EXCLUDED.updated_by, updated_at=CURRENT_TIMESTAMP;

INSERT INTO OPS_SERVICE_CIRCUIT_STATE (service_id, endpoint_code, instance_id, circuit_state, failure_count, success_count, closed_at, created_by, updated_by)
VALUES ('MBW', 'MBW_API', 'MBW-local-01', 'CLOSED', 0, 0, CURRENT_TIMESTAMP, 'SYSTEM', 'SYSTEM'),
    ('EDU', 'EDU_API', 'EDU-local-01', 'CLOSED', 0, 0, CURRENT_TIMESTAMP, 'SYSTEM', 'SYSTEM'),
    ('BAT', 'BAT_API', 'BAT-local-01', 'CLOSED', 0, 0, CURRENT_TIMESTAMP, 'SYSTEM', 'SYSTEM'),
    ('ADM', 'ADM_API', 'ADM-local-01', 'CLOSED', 0, 0, CURRENT_TIMESTAMP, 'SYSTEM', 'SYSTEM')
ON CONFLICT (service_id, endpoint_code, instance_id) DO UPDATE SET circuit_state=EXCLUDED.circuit_state, failure_count=EXCLUDED.failure_count, success_count=EXCLUDED.success_count, closed_at=EXCLUDED.closed_at, updated_by=EXCLUDED.updated_by, updated_at=CURRENT_TIMESTAMP;

INSERT INTO OPS_SERVICE_HEALTH_STATUS (service_id, endpoint_code, instance_id, health_status, http_status, response_time_ms, failure_message, checked_at, created_by, updated_by)
SELECT 'MBW', 'MBW_API', 'MBW-local-01', 'UP', 200, 0, NULL, CURRENT_TIMESTAMP, 'SYSTEM', 'SYSTEM'
WHERE NOT EXISTS (
    SELECT 1 FROM OPS_SERVICE_HEALTH_STATUS
    WHERE service_id = 'MBW' AND endpoint_code = 'MBW_API' AND instance_id = 'MBW-local-01' AND created_by = 'SYSTEM'
);

INSERT INTO OPS_SERVICE_HEALTH_STATUS (service_id, endpoint_code, instance_id, health_status, http_status, response_time_ms, failure_message, checked_at, created_by, updated_by)
SELECT 'EDU', 'EDU_API', 'EDU-local-01', 'UP', 200, 0, NULL, CURRENT_TIMESTAMP, 'SYSTEM', 'SYSTEM'
WHERE NOT EXISTS (
    SELECT 1 FROM OPS_SERVICE_HEALTH_STATUS
    WHERE service_id = 'EDU' AND endpoint_code = 'EDU_API' AND instance_id = 'EDU-local-01' AND created_by = 'SYSTEM'
);

INSERT INTO OPS_SERVICE_HEALTH_STATUS (service_id, endpoint_code, instance_id, health_status, http_status, response_time_ms, failure_message, checked_at, created_by, updated_by)
SELECT 'BAT', 'BAT_API', 'BAT-local-01', 'UP', 200, 0, NULL, CURRENT_TIMESTAMP, 'SYSTEM', 'SYSTEM'
WHERE NOT EXISTS (
    SELECT 1 FROM OPS_SERVICE_HEALTH_STATUS
    WHERE service_id = 'BAT' AND endpoint_code = 'BAT_API' AND instance_id = 'BAT-local-01' AND created_by = 'SYSTEM'
);

INSERT INTO OPS_SERVICE_HEALTH_STATUS (service_id, endpoint_code, instance_id, health_status, http_status, response_time_ms, failure_message, checked_at, created_by, updated_by)
SELECT 'ADM', 'ADM_API', 'ADM-local-01', 'UP', 200, 0, NULL, CURRENT_TIMESTAMP, 'SYSTEM', 'SYSTEM'
WHERE NOT EXISTS (
    SELECT 1 FROM OPS_SERVICE_HEALTH_STATUS
    WHERE service_id = 'ADM' AND endpoint_code = 'ADM_API' AND instance_id = 'ADM-local-01' AND created_by = 'SYSTEM'
);

INSERT INTO BAT_CENTER_CUT_PARAMETER (center_cut_job_id, parameter_key, parameter_value, encrypted_yn, use_yn, created_by, updated_by)
VALUES ('CPF_REF_CENTER_CUT_SAMPLE_JOB', 'businessDatePattern', 'D+0', 'N', 'Y', 'SYSTEM', 'SYSTEM'),
    ('CPF_REF_CENTER_CUT_SAMPLE_JOB', 'defaultLimit', '10', 'N', 'Y', 'SYSTEM', 'SYSTEM'),
    ('CPF_REF_CENTER_CUT_SAMPLE_JOB', 'targetTable', 'REF_CENTER_CUT_SAMPLE_TARGET', 'N', 'Y', 'SYSTEM', 'SYSTEM'),
    ('CPF_REF_CENTER_CUT_SAMPLE_JOB', 'resultTable', 'REF_CENTER_CUT_SAMPLE_RESULT', 'N', 'Y', 'SYSTEM', 'SYSTEM')
ON CONFLICT (center_cut_job_id, parameter_key) DO UPDATE SET parameter_value=EXCLUDED.parameter_value, encrypted_yn=EXCLUDED.encrypted_yn, use_yn=EXCLUDED.use_yn, updated_by=EXCLUDED.updated_by, updated_at=CURRENT_TIMESTAMP;

INSERT INTO ADM_IP_ALLOWLIST (IP_PATTERN, DESCRIPTION, USE_YN, created_by, updated_by)
VALUES ('127.0.0.1', '로컬 개발 PC', 'Y', 'SYSTEM', 'SYSTEM')
ON CONFLICT (IP_PATTERN) DO UPDATE SET DESCRIPTION=EXCLUDED.DESCRIPTION, USE_YN=EXCLUDED.USE_YN, updated_by=EXCLUDED.updated_by, updated_at=CURRENT_TIMESTAMP;

INSERT INTO ADM_AUDIT_LOG (TRANSACTION_ID, TRACE_ID, OPERATOR_ID, MENU_ID, ACTION_TYPE, TARGET_TYPE, TARGET_ID, REASON, REQUEST_BODY, CLIENT_IP, created_by, updated_by)
SELECT
    '20260724120000000ADMadmUI010000001',
    '20260724120000000ADMadmUI010000001',
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
    FROM ADM_AUDIT_LOG
    WHERE TRANSACTION_ID = '20260724120000000ADMadmUI010000001'
      AND OPERATOR_ID = 'admin'
      AND ACTION_TYPE = 'SEED'
      AND TARGET_TYPE = 'ADM'
      AND TARGET_ID = 'INITIAL_DATA'
);

-- CPF_SEED_INLINE_VARIABLE sample_transaction_id

-- CPF_SEED_INLINE_VARIABLE sample_start_time

-- CPF_SEED_INLINE_VARIABLE sample_end_time

INSERT INTO CPF_TRANSACTION_LOG (LOG_DATE, TRANSACTION_ID, TRACE_ID, SPAN_ID, SEQUENCE_NO, MODULE_ID, BUSINESS_TRANSACTION_ID, BUSINESS_TRANSACTION_NAME, LOG_TYPE, API_VERSION, CLIENT_ID, CLIENT_VERSION, CALLER_CHANNEL, TARGET_CHANNEL, TARGET_OPERATION_ID, CALLER_INSTANCE_ID, CORRELATION_ID, IDEMPOTENCY_KEY, LOCALE, TIMEZONE, REQUEST_TYPE, ORIGINAL_CHANNEL, CURRENT_CHANNEL, MEMBER_NO, CUSTOMER_NO, SCREEN_ID, DEVICE_ID, WAS_ID, INSTANCE_ID, HOST_NAME, HOST_IP, PROCESS_ID, THREAD_NAME, HTTP_METHOD, URI, CONTROLLER, EXECUTION_PACKAGE, EXECUTION_CLASS, EXECUTION_METHOD, EXECUTION_SIGNATURE, PARAMETERS, REQUEST_BODY, RESPONSE, HTTP_STATUS, RESPONSE_CODE, EXEC_USER, CLIENT_IP, USER_AGENT, START_TIME, END_TIME, DURATION_MS, created_by, updated_by)
SELECT
    DATE(('2026-06-15 12:00:00.000')),
    ('20260615120000000MBRlocal010000001'),
    'trace-sample-001',
    'span-sample-001',
    1,
    'EDU',
    'OEDUAA0001',
    'EDU 표준 거래 샘플',
    'SUCCESS',
    'v1',
    'cpf-edu-web',
    '1.0.0',
    'EDU',
    'EDU',
    'educationCrudList',
    'local-dev',
    'corr-sample-001',
    'idem-sample-001',
    'ko-KR',
    'Asia/Seoul',
    'NORMAL',
    'EDU',
    'EDU',
    'M000000001',
    'C000000001',
    'EDU_SAMPLE_LIST',
    'LOCAL_BROWSER',
    'local01',
    'local-dev:sql-seed',
    'local-dev',
    '127.0.0.1',
    'sql-seed',
    'sql-smoke',
    'GET',
    '/edu/api/education/crud',
    'com.cpf.education.web.crud.controller.EducationCrudEducationController',
    'com.cpf.education.web.crud.controller',
    'EducationCrudEducationController',
    'list',
    'EducationCrudEducationController.list()',
    '{}',
    '{"sampleKey":"REF-SAMPLE-001","secret":"masked"}',
    '{"code":"SCPF000000","message":"정상 처리되었습니다."}',
    200,
    'SCPF000000',
    'SYSTEM',
    '127.0.0.1',
    'SQL-SEED',
    ('2026-06-15 12:00:00.000'),
    ('2026-06-15 12:00:00.012'),
    12,
    'SYSTEM',
    'SYSTEM'
WHERE NOT EXISTS (
    SELECT 1
    FROM CPF_TRANSACTION_LOG
    WHERE TRANSACTION_ID = ('20260615120000000MBRlocal010000001')
      AND BUSINESS_TRANSACTION_ID = 'OEDUAA0001'
);

-- CPF_SEED_INLINE_VARIABLE sample_log_idx

INSERT INTO CPF_TRANSACTION_LOG_DETAIL (LOG_IDX, DETAIL_KEY, DETAIL_VALUE, created_by, updated_by)
SELECT (
    SELECT LOG_IDX
    FROM CPF_TRANSACTION_LOG
    WHERE TRANSACTION_ID = ('20260615120000000MBRlocal010000001')
      AND BUSINESS_TRANSACTION_ID = 'OEDUAA0001'
    ORDER BY LOG_IDX
    FETCH FIRST 1 ROW ONLY
), 'headers', '{"X-Current-Channel":"WEB","X-Request-Type":"NORMAL","X-Client-Version":"1.0.0"}', 'SYSTEM', 'SYSTEM'
WHERE (
    SELECT LOG_IDX
    FROM CPF_TRANSACTION_LOG
    WHERE TRANSACTION_ID = ('20260615120000000MBRlocal010000001')
      AND BUSINESS_TRANSACTION_ID = 'OEDUAA0001'
    ORDER BY LOG_IDX
    FETCH FIRST 1 ROW ONLY
) IS NOT NULL
  AND NOT EXISTS (
      SELECT 1
      FROM CPF_TRANSACTION_LOG_DETAIL
      WHERE LOG_IDX = (
    SELECT LOG_IDX
    FROM CPF_TRANSACTION_LOG
    WHERE TRANSACTION_ID = ('20260615120000000MBRlocal010000001')
      AND BUSINESS_TRANSACTION_ID = 'OEDUAA0001'
    ORDER BY LOG_IDX
    FETCH FIRST 1 ROW ONLY
)
        AND DETAIL_KEY = 'headers'
  );

INSERT INTO CPF_TRANSACTION_LOG_DETAIL (LOG_IDX, DETAIL_KEY, DETAIL_VALUE, created_by, updated_by)
SELECT (
    SELECT LOG_IDX
    FROM CPF_TRANSACTION_LOG
    WHERE TRANSACTION_ID = ('20260615120000000MBRlocal010000001')
      AND BUSINESS_TRANSACTION_ID = 'OEDUAA0001'
    ORDER BY LOG_IDX
    FETCH FIRST 1 ROW ONLY
), 'fixedTelegram', 'S000000001샘플1              000000010000Y20260617', 'SYSTEM', 'SYSTEM'
WHERE (
    SELECT LOG_IDX
    FROM CPF_TRANSACTION_LOG
    WHERE TRANSACTION_ID = ('20260615120000000MBRlocal010000001')
      AND BUSINESS_TRANSACTION_ID = 'OEDUAA0001'
    ORDER BY LOG_IDX
    FETCH FIRST 1 ROW ONLY
) IS NOT NULL
  AND NOT EXISTS (
      SELECT 1
      FROM CPF_TRANSACTION_LOG_DETAIL
      WHERE LOG_IDX = (
    SELECT LOG_IDX
    FROM CPF_TRANSACTION_LOG
    WHERE TRANSACTION_ID = ('20260615120000000MBRlocal010000001')
      AND BUSINESS_TRANSACTION_ID = 'OEDUAA0001'
    ORDER BY LOG_IDX
    FETCH FIRST 1 ROW ONLY
)
        AND DETAIL_KEY = 'fixedTelegram'
  );

INSERT INTO CPF_TRANSACTION_LOG_DETAIL (LOG_IDX, DETAIL_KEY, DETAIL_VALUE, created_by, updated_by)
SELECT (
    SELECT LOG_IDX
    FROM CPF_TRANSACTION_LOG
    WHERE TRANSACTION_ID = ('20260615120000000MBRlocal010000001')
      AND BUSINESS_TRANSACTION_ID = 'OEDUAA0001'
    ORDER BY LOG_IDX
    FETCH FIRST 1 ROW ONLY
), 'memo', 'ADM 로그 화면 smoke 검증용 거래 로그입니다.', 'SYSTEM', 'SYSTEM'
WHERE (
    SELECT LOG_IDX
    FROM CPF_TRANSACTION_LOG
    WHERE TRANSACTION_ID = ('20260615120000000MBRlocal010000001')
      AND BUSINESS_TRANSACTION_ID = 'OEDUAA0001'
    ORDER BY LOG_IDX
    FETCH FIRST 1 ROW ONLY
) IS NOT NULL
  AND NOT EXISTS (
      SELECT 1
      FROM CPF_TRANSACTION_LOG_DETAIL
      WHERE LOG_IDX = (
    SELECT LOG_IDX
    FROM CPF_TRANSACTION_LOG
    WHERE TRANSACTION_ID = ('20260615120000000MBRlocal010000001')
      AND BUSINESS_TRANSACTION_ID = 'OEDUAA0001'
    ORDER BY LOG_IDX
    FETCH FIRST 1 ROW ONLY
)
        AND DETAIL_KEY = 'memo'
  );

INSERT INTO ADM_DYNAMIC_LOG_LEVEL_RULE (RULE_ID, TRANSACTION_ID, BUSINESS_TRANSACTION_ID, MODULE_ID, LOG_LEVEL, EXPIRE_AT, REASON, USE_YN, created_by, updated_by)
VALUES (
    'sample-rule-001',
    NULL,
    'OEDUAA0001',
    'EDU',
    'DEBUG',
    (CURRENT_TIMESTAMP + INTERVAL '30 minute'),
    'ADM 화면 smoke 검증용 동적 로그 규칙입니다.',
    'Y',
    'SYSTEM',
    'SYSTEM'
)
ON CONFLICT (RULE_ID) DO UPDATE SET BUSINESS_TRANSACTION_ID=EXCLUDED.BUSINESS_TRANSACTION_ID, MODULE_ID=EXCLUDED.MODULE_ID, LOG_LEVEL=EXCLUDED.LOG_LEVEL, EXPIRE_AT=EXCLUDED.EXPIRE_AT, REASON=EXCLUDED.REASON, USE_YN=EXCLUDED.USE_YN, updated_by=EXCLUDED.updated_by, updated_at=CURRENT_TIMESTAMP;

INSERT INTO ADM_BUTTON (button_id, menu_id, action_code, button_name, http_method, api_pattern, sort_order, use_yn, created_by, updated_by)
VALUES ('GATEWAY_READ','GATEWAY_DASHBOARD','READ','Gateway 운영 조회','GET','/adm/api/gateway-registry/**',10,'Y','SYSTEM','SYSTEM'),
('GATEWAY_GROUP_WRITE','GATEWAY_GROUPS','WRITE','Server Group 저장','POST','/adm/api/gateway-registry/server-groups',20,'Y','SYSTEM','SYSTEM'),
('GATEWAY_GROUP_DELETE','GATEWAY_GROUPS','DELETE','Server Group 폐기','DELETE','/adm/api/gateway-registry/server-groups/*',30,'Y','SYSTEM','SYSTEM'),
('GATEWAY_ROUTE_WRITE','GATEWAY_ROUTES','WRITE','Gateway Binding 저장','POST','/adm/api/gateway-registry/bindings',40,'Y','SYSTEM','SYSTEM'),
('GATEWAY_ROUTE_STATE','GATEWAY_ROUTES','CONTROL','Gateway Binding 상태 변경','POST','/adm/api/gateway-registry/bindings/*/state',50,'Y','SYSTEM','SYSTEM'),
('GATEWAY_ROUTE_DELETE','GATEWAY_ROUTES','DELETE','Gateway Binding 폐기','DELETE','/adm/api/gateway-registry/bindings/*',60,'Y','SYSTEM','SYSTEM'),
('GATEWAY_CONNECTION_TEST','GATEWAY_HEALTH','TEST','Gateway 연결시험 요청','POST','/adm/api/gateway-registry/bindings/*/connection-tests',70,'Y','SYSTEM','SYSTEM'),
('GATEWAY_TEST_CONTROL','GATEWAY_HEALTH','CONTROL','Gateway 연결시험 취소·재검증','POST','/adm/api/gateway-registry/connection-test-operations/*/**',80,'Y','SYSTEM','SYSTEM')
ON CONFLICT (button_id) DO UPDATE SET menu_id=EXCLUDED.menu_id, action_code=EXCLUDED.action_code, button_name=EXCLUDED.button_name, http_method=EXCLUDED.http_method, api_pattern=EXCLUDED.api_pattern, sort_order=EXCLUDED.sort_order, use_yn=EXCLUDED.use_yn, updated_by=EXCLUDED.updated_by, updated_at=EXCLUDED.updated_at;

INSERT INTO ADM_ROLE_MENU (role_id, menu_id, read_yn, write_yn, delete_yn, created_by, updated_by)
VALUES ('ADM_ADMIN','GATEWAY_DASHBOARD','Y','Y','Y','SYSTEM','SYSTEM'),
('ADM_ADMIN','GATEWAY_SERVERS','Y','Y','Y','SYSTEM','SYSTEM'),
('ADM_ADMIN','GATEWAY_GROUPS','Y','Y','Y','SYSTEM','SYSTEM'),
('ADM_ADMIN','GATEWAY_ROUTES','Y','Y','Y','SYSTEM','SYSTEM'),
('ADM_ADMIN','GATEWAY_SECURITY','Y','Y','Y','SYSTEM','SYSTEM'),
('ADM_ADMIN','GATEWAY_HEALTH','Y','Y','Y','SYSTEM','SYSTEM'),
('ADM_ADMIN','GATEWAY_TRANSACTIONS','Y','Y','Y','SYSTEM','SYSTEM'),
('ADM_ADMIN','GATEWAY_LOG_POLICY','Y','Y','Y','SYSTEM','SYSTEM'),
('ADM_ADMIN','GATEWAY_APPLY_STATUS','Y','Y','Y','SYSTEM','SYSTEM'),
('ADM_DEV_OPERATOR','GATEWAY_DASHBOARD','Y','Y','N','SYSTEM','SYSTEM'),
('ADM_DEV_OPERATOR','GATEWAY_SERVERS','Y','Y','N','SYSTEM','SYSTEM'),
('ADM_DEV_OPERATOR','GATEWAY_GROUPS','Y','Y','N','SYSTEM','SYSTEM'),
('ADM_DEV_OPERATOR','GATEWAY_ROUTES','Y','Y','N','SYSTEM','SYSTEM'),
('ADM_DEV_OPERATOR','GATEWAY_SECURITY','Y','Y','N','SYSTEM','SYSTEM'),
('ADM_DEV_OPERATOR','GATEWAY_HEALTH','Y','Y','N','SYSTEM','SYSTEM'),
('ADM_DEV_OPERATOR','GATEWAY_TRANSACTIONS','Y','Y','N','SYSTEM','SYSTEM'),
('ADM_DEV_OPERATOR','GATEWAY_LOG_POLICY','Y','Y','N','SYSTEM','SYSTEM'),
('ADM_DEV_OPERATOR','GATEWAY_APPLY_STATUS','Y','Y','N','SYSTEM','SYSTEM'),
('ADM_OPERATOR','GATEWAY_DASHBOARD','Y','N','N','SYSTEM','SYSTEM'),
('ADM_OPERATOR','GATEWAY_SERVERS','Y','N','N','SYSTEM','SYSTEM'),
('ADM_OPERATOR','GATEWAY_GROUPS','Y','N','N','SYSTEM','SYSTEM'),
('ADM_OPERATOR','GATEWAY_ROUTES','Y','N','N','SYSTEM','SYSTEM'),
('ADM_OPERATOR','GATEWAY_SECURITY','Y','N','N','SYSTEM','SYSTEM'),
('ADM_OPERATOR','GATEWAY_HEALTH','Y','N','N','SYSTEM','SYSTEM'),
('ADM_OPERATOR','GATEWAY_TRANSACTIONS','Y','N','N','SYSTEM','SYSTEM'),
('ADM_OPERATOR','GATEWAY_LOG_POLICY','Y','N','N','SYSTEM','SYSTEM'),
('ADM_OPERATOR','GATEWAY_APPLY_STATUS','Y','N','N','SYSTEM','SYSTEM'),
('ADM_VIEWER','GATEWAY_DASHBOARD','Y','N','N','SYSTEM','SYSTEM'),
('ADM_VIEWER','GATEWAY_SERVERS','Y','N','N','SYSTEM','SYSTEM'),
('ADM_VIEWER','GATEWAY_GROUPS','Y','N','N','SYSTEM','SYSTEM'),
('ADM_VIEWER','GATEWAY_ROUTES','Y','N','N','SYSTEM','SYSTEM'),
('ADM_VIEWER','GATEWAY_SECURITY','Y','N','N','SYSTEM','SYSTEM'),
('ADM_VIEWER','GATEWAY_HEALTH','Y','N','N','SYSTEM','SYSTEM'),
('ADM_VIEWER','GATEWAY_TRANSACTIONS','Y','N','N','SYSTEM','SYSTEM'),
('ADM_VIEWER','GATEWAY_LOG_POLICY','Y','N','N','SYSTEM','SYSTEM'),
('ADM_VIEWER','GATEWAY_APPLY_STATUS','Y','N','N','SYSTEM','SYSTEM')
ON CONFLICT (role_id, menu_id) DO UPDATE SET read_yn=EXCLUDED.read_yn, write_yn=EXCLUDED.write_yn, delete_yn=EXCLUDED.delete_yn, updated_by=EXCLUDED.updated_by, updated_at=EXCLUDED.updated_at;

INSERT INTO ADM_API_PERMISSION (api_permission_id, api_group_code, http_method, api_path, api_name, permission_code, menu_id, button_id, use_yn, created_by, updated_by)
VALUES ('API_GATEWAY_READ','GATEWAY','GET','/adm/api/gateway-registry/**','Gateway 운영 조회','READ','GATEWAY_DASHBOARD','GATEWAY_READ','Y','SYSTEM','SYSTEM'),
('API_GATEWAY_GROUP_WRITE','GATEWAY','POST','/adm/api/gateway-registry/server-groups','Server Group 저장','WRITE','GATEWAY_GROUPS','GATEWAY_GROUP_WRITE','Y','SYSTEM','SYSTEM'),
('API_GATEWAY_GROUP_DELETE','GATEWAY','DELETE','/adm/api/gateway-registry/server-groups/*','Server Group 폐기','DELETE','GATEWAY_GROUPS','GATEWAY_GROUP_DELETE','Y','SYSTEM','SYSTEM'),
('API_GATEWAY_ROUTE_WRITE','GATEWAY','POST','/adm/api/gateway-registry/bindings','Gateway Binding 저장','WRITE','GATEWAY_ROUTES','GATEWAY_ROUTE_WRITE','Y','SYSTEM','SYSTEM'),
('API_GATEWAY_ROUTE_STATE','GATEWAY','POST','/adm/api/gateway-registry/bindings/*/state','Gateway Binding 상태 변경','CONTROL','GATEWAY_ROUTES','GATEWAY_ROUTE_STATE','Y','SYSTEM','SYSTEM'),
('API_GATEWAY_ROUTE_DELETE','GATEWAY','DELETE','/adm/api/gateway-registry/bindings/*','Gateway Binding 폐기','DELETE','GATEWAY_ROUTES','GATEWAY_ROUTE_DELETE','Y','SYSTEM','SYSTEM'),
('API_GATEWAY_CONNECTION_TEST','GATEWAY','POST','/adm/api/gateway-registry/bindings/*/connection-tests','Gateway 연결시험 요청','TEST','GATEWAY_HEALTH','GATEWAY_CONNECTION_TEST','Y','SYSTEM','SYSTEM'),
('API_GATEWAY_TEST_CONTROL','GATEWAY','POST','/adm/api/gateway-registry/connection-test-operations/*/**','Gateway 연결시험 취소·재검증','CONTROL','GATEWAY_HEALTH','GATEWAY_TEST_CONTROL','Y','SYSTEM','SYSTEM')
ON CONFLICT (api_permission_id) DO UPDATE SET api_group_code=EXCLUDED.api_group_code, http_method=EXCLUDED.http_method, api_path=EXCLUDED.api_path, api_name=EXCLUDED.api_name, permission_code=EXCLUDED.permission_code, menu_id=EXCLUDED.menu_id, button_id=EXCLUDED.button_id, use_yn=EXCLUDED.use_yn, updated_by=EXCLUDED.updated_by, updated_at=EXCLUDED.updated_at;

INSERT INTO ADM_ROLE_BUTTON (role_id, button_id, allow_yn, created_by, updated_by)
VALUES ('ADM_ADMIN','GATEWAY_READ','Y','SYSTEM','SYSTEM'),
('ADM_ADMIN','GATEWAY_GROUP_WRITE','Y','SYSTEM','SYSTEM'),
('ADM_ADMIN','GATEWAY_GROUP_DELETE','Y','SYSTEM','SYSTEM'),
('ADM_ADMIN','GATEWAY_ROUTE_WRITE','Y','SYSTEM','SYSTEM'),
('ADM_ADMIN','GATEWAY_ROUTE_STATE','Y','SYSTEM','SYSTEM'),
('ADM_ADMIN','GATEWAY_ROUTE_DELETE','Y','SYSTEM','SYSTEM'),
('ADM_ADMIN','GATEWAY_CONNECTION_TEST','Y','SYSTEM','SYSTEM'),
('ADM_ADMIN','GATEWAY_TEST_CONTROL','Y','SYSTEM','SYSTEM'),
('ADM_DEV_OPERATOR','GATEWAY_READ','Y','SYSTEM','SYSTEM'),
('ADM_DEV_OPERATOR','GATEWAY_GROUP_WRITE','Y','SYSTEM','SYSTEM'),
('ADM_DEV_OPERATOR','GATEWAY_GROUP_DELETE','N','SYSTEM','SYSTEM'),
('ADM_DEV_OPERATOR','GATEWAY_ROUTE_WRITE','Y','SYSTEM','SYSTEM'),
('ADM_DEV_OPERATOR','GATEWAY_ROUTE_STATE','Y','SYSTEM','SYSTEM'),
('ADM_DEV_OPERATOR','GATEWAY_ROUTE_DELETE','N','SYSTEM','SYSTEM'),
('ADM_DEV_OPERATOR','GATEWAY_CONNECTION_TEST','Y','SYSTEM','SYSTEM'),
('ADM_DEV_OPERATOR','GATEWAY_TEST_CONTROL','Y','SYSTEM','SYSTEM'),
('ADM_OPERATOR','GATEWAY_READ','Y','SYSTEM','SYSTEM'),
('ADM_OPERATOR','GATEWAY_GROUP_WRITE','N','SYSTEM','SYSTEM'),
('ADM_OPERATOR','GATEWAY_GROUP_DELETE','N','SYSTEM','SYSTEM'),
('ADM_OPERATOR','GATEWAY_ROUTE_WRITE','N','SYSTEM','SYSTEM'),
('ADM_OPERATOR','GATEWAY_ROUTE_STATE','N','SYSTEM','SYSTEM'),
('ADM_OPERATOR','GATEWAY_ROUTE_DELETE','N','SYSTEM','SYSTEM'),
('ADM_OPERATOR','GATEWAY_CONNECTION_TEST','Y','SYSTEM','SYSTEM'),
('ADM_OPERATOR','GATEWAY_TEST_CONTROL','N','SYSTEM','SYSTEM'),
('ADM_VIEWER','GATEWAY_READ','Y','SYSTEM','SYSTEM'),
('ADM_VIEWER','GATEWAY_GROUP_WRITE','N','SYSTEM','SYSTEM'),
('ADM_VIEWER','GATEWAY_GROUP_DELETE','N','SYSTEM','SYSTEM'),
('ADM_VIEWER','GATEWAY_ROUTE_WRITE','N','SYSTEM','SYSTEM'),
('ADM_VIEWER','GATEWAY_ROUTE_STATE','N','SYSTEM','SYSTEM'),
('ADM_VIEWER','GATEWAY_ROUTE_DELETE','N','SYSTEM','SYSTEM'),
('ADM_VIEWER','GATEWAY_CONNECTION_TEST','N','SYSTEM','SYSTEM'),
('ADM_VIEWER','GATEWAY_TEST_CONTROL','N','SYSTEM','SYSTEM')
ON CONFLICT (role_id, button_id) DO UPDATE SET allow_yn=EXCLUDED.allow_yn, updated_by=EXCLUDED.updated_by, updated_at=EXCLUDED.updated_at;

INSERT INTO ADM_ROLE_API_PERMISSION (role_id, api_permission_id, allow_yn, created_by, updated_by)
VALUES ('ADM_ADMIN','API_GATEWAY_READ','Y','SYSTEM','SYSTEM'),
('ADM_ADMIN','API_GATEWAY_GROUP_WRITE','Y','SYSTEM','SYSTEM'),
('ADM_ADMIN','API_GATEWAY_GROUP_DELETE','Y','SYSTEM','SYSTEM'),
('ADM_ADMIN','API_GATEWAY_ROUTE_WRITE','Y','SYSTEM','SYSTEM'),
('ADM_ADMIN','API_GATEWAY_ROUTE_STATE','Y','SYSTEM','SYSTEM'),
('ADM_ADMIN','API_GATEWAY_ROUTE_DELETE','Y','SYSTEM','SYSTEM'),
('ADM_ADMIN','API_GATEWAY_CONNECTION_TEST','Y','SYSTEM','SYSTEM'),
('ADM_ADMIN','API_GATEWAY_TEST_CONTROL','Y','SYSTEM','SYSTEM'),
('ADM_DEV_OPERATOR','API_GATEWAY_READ','Y','SYSTEM','SYSTEM'),
('ADM_DEV_OPERATOR','API_GATEWAY_GROUP_WRITE','Y','SYSTEM','SYSTEM'),
('ADM_DEV_OPERATOR','API_GATEWAY_GROUP_DELETE','N','SYSTEM','SYSTEM'),
('ADM_DEV_OPERATOR','API_GATEWAY_ROUTE_WRITE','Y','SYSTEM','SYSTEM'),
('ADM_DEV_OPERATOR','API_GATEWAY_ROUTE_STATE','Y','SYSTEM','SYSTEM'),
('ADM_DEV_OPERATOR','API_GATEWAY_ROUTE_DELETE','N','SYSTEM','SYSTEM'),
('ADM_DEV_OPERATOR','API_GATEWAY_CONNECTION_TEST','Y','SYSTEM','SYSTEM'),
('ADM_DEV_OPERATOR','API_GATEWAY_TEST_CONTROL','Y','SYSTEM','SYSTEM'),
('ADM_OPERATOR','API_GATEWAY_READ','Y','SYSTEM','SYSTEM'),
('ADM_OPERATOR','API_GATEWAY_GROUP_WRITE','N','SYSTEM','SYSTEM'),
('ADM_OPERATOR','API_GATEWAY_GROUP_DELETE','N','SYSTEM','SYSTEM'),
('ADM_OPERATOR','API_GATEWAY_ROUTE_WRITE','N','SYSTEM','SYSTEM'),
('ADM_OPERATOR','API_GATEWAY_ROUTE_STATE','N','SYSTEM','SYSTEM'),
('ADM_OPERATOR','API_GATEWAY_ROUTE_DELETE','N','SYSTEM','SYSTEM'),
('ADM_OPERATOR','API_GATEWAY_CONNECTION_TEST','Y','SYSTEM','SYSTEM'),
('ADM_OPERATOR','API_GATEWAY_TEST_CONTROL','N','SYSTEM','SYSTEM'),
('ADM_VIEWER','API_GATEWAY_READ','Y','SYSTEM','SYSTEM'),
('ADM_VIEWER','API_GATEWAY_GROUP_WRITE','N','SYSTEM','SYSTEM'),
('ADM_VIEWER','API_GATEWAY_GROUP_DELETE','N','SYSTEM','SYSTEM'),
('ADM_VIEWER','API_GATEWAY_ROUTE_WRITE','N','SYSTEM','SYSTEM'),
('ADM_VIEWER','API_GATEWAY_ROUTE_STATE','N','SYSTEM','SYSTEM'),
('ADM_VIEWER','API_GATEWAY_ROUTE_DELETE','N','SYSTEM','SYSTEM'),
('ADM_VIEWER','API_GATEWAY_CONNECTION_TEST','N','SYSTEM','SYSTEM'),
('ADM_VIEWER','API_GATEWAY_TEST_CONTROL','N','SYSTEM','SYSTEM')
ON CONFLICT (role_id, api_permission_id) DO UPDATE SET allow_yn=EXCLUDED.allow_yn, updated_by=EXCLUDED.updated_by, updated_at=EXCLUDED.updated_at;
