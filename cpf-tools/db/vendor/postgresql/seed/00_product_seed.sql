-- CPF generated lifecycle bundle; vendor=postgresql
-- Source of truth: cpf-tools/db/vendor/postgresql/source + database-source-plan.json

-- ===== BEGIN 50_framework_seed_data.sql =====
-- AUTO-GENERATED from cpf-tools/db/canonical/seed-model.json
-- vendor=postgresql; source=50_framework_seed_data.sql
-- USE lines are CPF packaging directives and are stripped by the vendor executor.



-- CPF_LOGICAL_DATABASE=cpfDB
-- CPF_USE_LOGICAL_DATABASE=cpfDB
MERGE INTO cpf_channel_registry tgt
USING (VALUES
  ('ANY', '전체 채널', 'SYSTEM', 'INTERNAL', 'N', 'Y', 'N', 'N', 'Y', '정책 와일드카드 전용 채널', 0, 'SYSTEM', 'SYSTEM'),
  ('WEB', '웹', 'CLIENT', 'EXTERNAL', 'Y', 'N', 'Y', 'N', 'Y', '웹 브라우저 채널', 0, 'SYSTEM', 'SYSTEM'),
  ('MOBILE', '모바일', 'CLIENT', 'EXTERNAL', 'Y', 'N', 'Y', 'N', 'Y', '모바일 애플리케이션 채널', 0, 'SYSTEM', 'SYSTEM'),
  ('ADM', '관리자', 'OPERATOR', 'INTERNAL', 'Y', 'Y', 'Y', 'N', 'Y', 'ADM 운영 채널', 0, 'SYSTEM', 'SYSTEM'),
  ('BATCH', '배치', 'SYSTEM', 'INTERNAL', 'N', 'Y', 'N', 'N', 'Y', '배치 실행 채널', 0, 'SYSTEM', 'SYSTEM')
) AS src(channel_code, channel_name, channel_type, trust_level, client_channel_yn, internal_channel_yn, authentication_required_yn, signature_required_yn, active_yn, description, policy_version, created_by, updated_by)
ON (tgt.channel_code = src.channel_code)
WHEN MATCHED THEN UPDATE SET
  tgt.channel_name = src.channel_name,
  tgt.channel_type = src.channel_type,
  tgt.trust_level = src.trust_level,
  tgt.client_channel_yn = src.client_channel_yn,
  tgt.internal_channel_yn = src.internal_channel_yn,
  tgt.authentication_required_yn = src.authentication_required_yn,
  tgt.signature_required_yn = src.signature_required_yn,
  tgt.active_yn = src.active_yn,
  tgt.description = src.description,
  tgt.updated_by = src.updated_by,
  tgt.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (channel_code, channel_name, channel_type, trust_level, client_channel_yn, internal_channel_yn, authentication_required_yn, signature_required_yn, active_yn, description, policy_version, created_by, updated_by)
VALUES (src.channel_code, src.channel_name, src.channel_type, src.trust_level, src.client_channel_yn, src.internal_channel_yn, src.authentication_required_yn, src.signature_required_yn, src.active_yn, src.description, src.policy_version, src.created_by, src.updated_by);

MERGE INTO cpf_channel_execution_policy tgt
USING (VALUES
  ('CPF.DEFAULT', '*', 'ANY', 'ANY', '*', 'Y', 'N', 'N', 0, NULL, NULL, 'Y', 0, 'SYSTEM', 'SYSTEM')
) AS src(policy_key, standard_execution_id, original_channel_code, caller_channel_code, request_type, allowed_yn, authentication_required_yn, signature_required_yn, max_tps, effective_from, effective_to, active_yn, policy_version, created_by, updated_by)
ON (tgt.policy_key = src.policy_key)
WHEN MATCHED THEN UPDATE SET
  tgt.standard_execution_id = src.standard_execution_id,
  tgt.original_channel_code = src.original_channel_code,
  tgt.caller_channel_code = src.caller_channel_code,
  tgt.request_type = src.request_type,
  tgt.allowed_yn = src.allowed_yn,
  tgt.authentication_required_yn = src.authentication_required_yn,
  tgt.signature_required_yn = src.signature_required_yn,
  tgt.max_tps = src.max_tps,
  tgt.active_yn = src.active_yn,
  tgt.updated_by = src.updated_by,
  tgt.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (policy_key, standard_execution_id, original_channel_code, caller_channel_code, request_type, allowed_yn, authentication_required_yn, signature_required_yn, max_tps, effective_from, effective_to, active_yn, policy_version, created_by, updated_by)
VALUES (src.policy_key, src.standard_execution_id, src.original_channel_code, src.caller_channel_code, src.request_type, src.allowed_yn, src.authentication_required_yn, src.signature_required_yn, src.max_tps, src.effective_from, src.effective_to, src.active_yn, src.policy_version, src.created_by, src.updated_by);

MERGE INTO cpf_code tgt
USING (VALUES
  (NULL, 'CODE_GROUP', 'MODULE', '서비스 모듈 코드 그룹', 'SYSTEM', 'SYSTEM'),
  (NULL, 'CODE_GROUP', 'REQUEST_TYPE', '요청 유형 코드 그룹', 'SYSTEM', 'SYSTEM'),
  (NULL, 'CODE_GROUP', 'CHANNEL_CODE', '채널 코드 그룹', 'SYSTEM', 'SYSTEM'),
  (NULL, 'CODE_GROUP', 'RESULT_TYPE', '응답 결과 유형 코드 그룹', 'SYSTEM', 'SYSTEM'),
  (NULL, 'CODE_GROUP', 'MESSAGE_FORMAT_TYPE', '메시지 포맷 유형 코드 그룹', 'SYSTEM', 'SYSTEM'),
  (NULL, 'CODE_GROUP', 'LOG_LEVEL', '동적 로그 레벨 코드 그룹', 'SYSTEM', 'SYSTEM'),
  (NULL, 'CODE_GROUP', 'CACHE_NAME', '캐시 이름 코드 그룹', 'SYSTEM', 'SYSTEM'),
  (NULL, 'CODE_GROUP', 'BATCH_JOB_TYPE', '배치 Job 유형 코드 그룹', 'SYSTEM', 'SYSTEM'),
  (NULL, 'CODE_GROUP', 'YN', '여부 코드 그룹', 'SYSTEM', 'SYSTEM')
) AS src(parent_id, code_key, code_value, description, created_by, updated_by)
ON (tgt.code_key = src.code_key AND tgt.code_value = src.code_value)
WHEN MATCHED THEN UPDATE SET
  tgt.description = src.description,
  tgt.updated_by = src.updated_by,
  tgt.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (parent_id, code_key, code_value, description, created_by, updated_by)
VALUES (src.parent_id, src.code_key, src.code_value, src.description, src.created_by, src.updated_by);

MERGE INTO cpf_code tgt
USING (VALUES
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
) AS src(parent_id, code_key, code_value, description, created_by, updated_by)
ON (tgt.code_key = src.code_key AND tgt.code_value = src.code_value)
WHEN MATCHED THEN UPDATE SET
  tgt.parent_id = src.parent_id,
  tgt.description = src.description,
  tgt.updated_by = src.updated_by,
  tgt.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (parent_id, code_key, code_value, description, created_by, updated_by)
VALUES (src.parent_id, src.code_key, src.code_value, src.description, src.created_by, src.updated_by);

MERGE INTO cpf_message tgt
USING (VALUES
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
) AS src(message_code, locale, message_format_type, external_message, internal_message, parameter_count, parameter_sample, description, created_by, updated_by)
ON (tgt.message_code = src.message_code AND tgt.locale = src.locale)
WHEN MATCHED THEN UPDATE SET
  tgt.message_format_type = src.message_format_type,
  tgt.external_message = src.external_message,
  tgt.internal_message = src.internal_message,
  tgt.parameter_count = src.parameter_count,
  tgt.parameter_sample = src.parameter_sample,
  tgt.description = src.description,
  tgt.use_yn = 'Y',
  tgt.updated_by = src.updated_by,
  tgt.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (message_code, locale, message_format_type, external_message, internal_message, parameter_count, parameter_sample, description, created_by, updated_by)
VALUES (src.message_code, src.locale, src.message_format_type, src.external_message, src.internal_message, src.parameter_count, src.parameter_sample, src.description, src.created_by, src.updated_by);

MERGE INTO cpf_response_code tgt
USING (VALUES
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
) AS src(response_code, message_code, result_type, module_id, response_group, sequence_no, http_status, description, created_by, updated_by)
ON (tgt.response_code = src.response_code)
WHEN MATCHED THEN UPDATE SET
  tgt.message_code = src.message_code,
  tgt.result_type = src.result_type,
  tgt.module_id = src.module_id,
  tgt.response_group = src.response_group,
  tgt.sequence_no = src.sequence_no,
  tgt.http_status = src.http_status,
  tgt.description = src.description,
  tgt.use_yn = 'Y',
  tgt.updated_by = src.updated_by,
  tgt.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (response_code, message_code, result_type, module_id, response_group, sequence_no, http_status, description, created_by, updated_by)
VALUES (src.response_code, src.message_code, src.result_type, src.module_id, src.response_group, src.sequence_no, src.http_status, src.description, src.created_by, src.updated_by);

MERGE INTO cpf_config tgt
USING (VALUES
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
) AS src(config_key, config_value, config_type, description, encrypted_yn, created_by, updated_by)
ON (tgt.config_key = src.config_key)
WHEN MATCHED THEN UPDATE SET
  tgt.config_value = src.config_value,
  tgt.config_type = src.config_type,
  tgt.description = src.description,
  tgt.encrypted_yn = src.encrypted_yn,
  tgt.use_yn = 'Y',
  tgt.updated_by = src.updated_by,
  tgt.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (config_key, config_value, config_type, description, encrypted_yn, created_by, updated_by)
VALUES (src.config_key, src.config_value, src.config_type, src.description, src.encrypted_yn, src.created_by, src.updated_by);

MERGE INTO cpf_code tgt
USING (VALUES
  (NULL, 'CODE_GROUP', 'HTTP_METHOD', 'HTTP Method 코드 그룹', 'SYSTEM', 'SYSTEM'),
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
) AS src(parent_id, code_key, code_value, description, created_by, updated_by)
ON (tgt.code_key = src.code_key AND tgt.code_value = src.code_value)
WHEN MATCHED THEN UPDATE SET
  tgt.description = src.description,
  tgt.updated_by = src.updated_by,
  tgt.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (parent_id, code_key, code_value, description, created_by, updated_by)
VALUES (src.parent_id, src.code_key, src.code_value, src.description, src.created_by, src.updated_by);

MERGE INTO cpf_code tgt
USING (VALUES
  ((SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key='CODE_GROUP' AND code_value='HTTP_METHOD') x), 'HTTP_METHOD', 'GET', '조회', 'SYSTEM', 'SYSTEM'),
  ((SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key='CODE_GROUP' AND code_value='HTTP_METHOD') x), 'HTTP_METHOD', 'POST', '등록/명령', 'SYSTEM', 'SYSTEM'),
  ((SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key='CODE_GROUP' AND code_value='HTTP_METHOD') x), 'HTTP_METHOD', 'PUT', '전체 수정', 'SYSTEM', 'SYSTEM'),
  ((SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key='CODE_GROUP' AND code_value='HTTP_METHOD') x), 'HTTP_METHOD', 'PATCH', '부분 수정', 'SYSTEM', 'SYSTEM'),
  ((SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key='CODE_GROUP' AND code_value='HTTP_METHOD') x), 'HTTP_METHOD', 'DELETE', '삭제/회수', 'SYSTEM', 'SYSTEM'),
  ((SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key='CODE_GROUP' AND code_value='EXECUTION_STATUS') x), 'EXECUTION_STATUS', 'READY', '실행 준비', 'SYSTEM', 'SYSTEM'),
  ((SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key='CODE_GROUP' AND code_value='EXECUTION_STATUS') x), 'EXECUTION_STATUS', 'RUNNING', '실행 중', 'SYSTEM', 'SYSTEM'),
  ((SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key='CODE_GROUP' AND code_value='EXECUTION_STATUS') x), 'EXECUTION_STATUS', 'SUCCESS', '정상 완료', 'SYSTEM', 'SYSTEM'),
  ((SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key='CODE_GROUP' AND code_value='EXECUTION_STATUS') x), 'EXECUTION_STATUS', 'FAILED', '실패', 'SYSTEM', 'SYSTEM'),
  ((SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key='CODE_GROUP' AND code_value='EXECUTION_STATUS') x), 'EXECUTION_STATUS', 'UNKNOWN_RESULT', '결과 미확정', 'SYSTEM', 'SYSTEM'),
  ((SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key='CODE_GROUP' AND code_value='ASYNC_STATUS') x), 'ASYNC_STATUS', 'WAITING', '비동기 대기', 'SYSTEM', 'SYSTEM'),
  ((SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key='CODE_GROUP' AND code_value='ASYNC_STATUS') x), 'ASYNC_STATUS', 'PROCESSING', '비동기 처리 중', 'SYSTEM', 'SYSTEM'),
  ((SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key='CODE_GROUP' AND code_value='ASYNC_STATUS') x), 'ASYNC_STATUS', 'COMPLETED', '비동기 완료', 'SYSTEM', 'SYSTEM'),
  ((SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key='CODE_GROUP' AND code_value='ASYNC_STATUS') x), 'ASYNC_STATUS', 'DLQ', 'Dead Letter Queue', 'SYSTEM', 'SYSTEM'),
  ((SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key='CODE_GROUP' AND code_value='RETRY_STATUS') x), 'RETRY_STATUS', 'RETRYABLE', '재시도 가능', 'SYSTEM', 'SYSTEM'),
  ((SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key='CODE_GROUP' AND code_value='RETRY_STATUS') x), 'RETRY_STATUS', 'NON_RETRYABLE', '재시도 금지', 'SYSTEM', 'SYSTEM'),
  ((SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key='CODE_GROUP' AND code_value='RETRY_STATUS') x), 'RETRY_STATUS', 'EXHAUSTED', '재시도 소진', 'SYSTEM', 'SYSTEM'),
  ((SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key='CODE_GROUP' AND code_value='IDEMPOTENCY_STATUS') x), 'IDEMPOTENCY_STATUS', 'PROCESSING', '멱등 처리 중', 'SYSTEM', 'SYSTEM'),
  ((SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key='CODE_GROUP' AND code_value='IDEMPOTENCY_STATUS') x), 'IDEMPOTENCY_STATUS', 'COMPLETED', '멱등 처리 완료', 'SYSTEM', 'SYSTEM'),
  ((SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key='CODE_GROUP' AND code_value='IDEMPOTENCY_STATUS') x), 'IDEMPOTENCY_STATUS', 'FAILED', '멱등 처리 실패', 'SYSTEM', 'SYSTEM'),
  ((SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key='CODE_GROUP' AND code_value='IDEMPOTENCY_STATUS') x), 'IDEMPOTENCY_STATUS', 'UNKNOWN_RESULT', '멱등 결과 미확정', 'SYSTEM', 'SYSTEM'),
  ((SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key='CODE_GROUP' AND code_value='HEALTH_STATUS') x), 'HEALTH_STATUS', 'UP', '정상', 'SYSTEM', 'SYSTEM'),
  ((SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key='CODE_GROUP' AND code_value='HEALTH_STATUS') x), 'HEALTH_STATUS', 'DOWN', '장애', 'SYSTEM', 'SYSTEM'),
  ((SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key='CODE_GROUP' AND code_value='HEALTH_STATUS') x), 'HEALTH_STATUS', 'DEGRADED', '부분 저하', 'SYSTEM', 'SYSTEM'),
  ((SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key='CODE_GROUP' AND code_value='CIRCUIT_STATUS') x), 'CIRCUIT_STATUS', 'CLOSED', '정상 호출', 'SYSTEM', 'SYSTEM'),
  ((SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key='CODE_GROUP' AND code_value='CIRCUIT_STATUS') x), 'CIRCUIT_STATUS', 'OPEN', '호출 차단', 'SYSTEM', 'SYSTEM'),
  ((SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key='CODE_GROUP' AND code_value='CIRCUIT_STATUS') x), 'CIRCUIT_STATUS', 'HALF_OPEN', '복구 시험', 'SYSTEM', 'SYSTEM'),
  ((SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key='CODE_GROUP' AND code_value='FILE_SCAN_STATUS') x), 'FILE_SCAN_STATUS', 'PENDING', '검사 대기', 'SYSTEM', 'SYSTEM'),
  ((SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key='CODE_GROUP' AND code_value='FILE_SCAN_STATUS') x), 'FILE_SCAN_STATUS', 'CLEAN', '검사 정상', 'SYSTEM', 'SYSTEM'),
  ((SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key='CODE_GROUP' AND code_value='FILE_SCAN_STATUS') x), 'FILE_SCAN_STATUS', 'INFECTED', '악성 탐지', 'SYSTEM', 'SYSTEM'),
  ((SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key='CODE_GROUP' AND code_value='FILE_SCAN_STATUS') x), 'FILE_SCAN_STATUS', 'FAILED', '검사 실패', 'SYSTEM', 'SYSTEM'),
  ((SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key='CODE_GROUP' AND code_value='FILE_SCAN_STATUS') x), 'FILE_SCAN_STATUS', 'QUARANTINED', '격리', 'SYSTEM', 'SYSTEM'),
  ((SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key='CODE_GROUP' AND code_value='DATA_CLASSIFICATION') x), 'DATA_CLASSIFICATION', 'PUBLIC', '공개 정보', 'SYSTEM', 'SYSTEM'),
  ((SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key='CODE_GROUP' AND code_value='DATA_CLASSIFICATION') x), 'DATA_CLASSIFICATION', 'INTERNAL', '내부 정보', 'SYSTEM', 'SYSTEM'),
  ((SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key='CODE_GROUP' AND code_value='DATA_CLASSIFICATION') x), 'DATA_CLASSIFICATION', 'CONFIDENTIAL', '기밀 정보', 'SYSTEM', 'SYSTEM'),
  ((SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key='CODE_GROUP' AND code_value='DATA_CLASSIFICATION') x), 'DATA_CLASSIFICATION', 'RESTRICTED', '제한/민감 정보', 'SYSTEM', 'SYSTEM'),
  ((SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key='CODE_GROUP' AND code_value='APPROVAL_STATUS') x), 'APPROVAL_STATUS', 'DRAFT', '작성 중', 'SYSTEM', 'SYSTEM'),
  ((SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key='CODE_GROUP' AND code_value='APPROVAL_STATUS') x), 'APPROVAL_STATUS', 'IN_REVIEW', '결재 진행', 'SYSTEM', 'SYSTEM'),
  ((SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key='CODE_GROUP' AND code_value='APPROVAL_STATUS') x), 'APPROVAL_STATUS', 'APPROVED', '승인 완료', 'SYSTEM', 'SYSTEM'),
  ((SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key='CODE_GROUP' AND code_value='APPROVAL_STATUS') x), 'APPROVAL_STATUS', 'REJECTED', '반려', 'SYSTEM', 'SYSTEM'),
  ((SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key='CODE_GROUP' AND code_value='APPROVAL_STATUS') x), 'APPROVAL_STATUS', 'WITHDRAWN', '철회', 'SYSTEM', 'SYSTEM'),
  ((SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key='CODE_GROUP' AND code_value='APPROVAL_STATUS') x), 'APPROVAL_STATUS', 'CANCELED', '취소', 'SYSTEM', 'SYSTEM'),
  ((SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key='CODE_GROUP' AND code_value='APPROVAL_STATUS') x), 'APPROVAL_STATUS', 'EXPIRED', '만료', 'SYSTEM', 'SYSTEM'),
  ((SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key='CODE_GROUP' AND code_value='ERROR_CATEGORY') x), 'ERROR_CATEGORY', 'VALIDATION', '입력/계약 검증 오류', 'SYSTEM', 'SYSTEM'),
  ((SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key='CODE_GROUP' AND code_value='ERROR_CATEGORY') x), 'ERROR_CATEGORY', 'AUTHENTICATION', '인증 오류', 'SYSTEM', 'SYSTEM'),
  ((SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key='CODE_GROUP' AND code_value='ERROR_CATEGORY') x), 'ERROR_CATEGORY', 'AUTHORIZATION', '인가 오류', 'SYSTEM', 'SYSTEM'),
  ((SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key='CODE_GROUP' AND code_value='ERROR_CATEGORY') x), 'ERROR_CATEGORY', 'CONFLICT', '동시성/중복 오류', 'SYSTEM', 'SYSTEM'),
  ((SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key='CODE_GROUP' AND code_value='ERROR_CATEGORY') x), 'ERROR_CATEGORY', 'TIMEOUT', 'Timeout', 'SYSTEM', 'SYSTEM'),
  ((SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key='CODE_GROUP' AND code_value='ERROR_CATEGORY') x), 'ERROR_CATEGORY', 'TARGET_DOWN', '호출 대상 장애', 'SYSTEM', 'SYSTEM'),
  ((SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key='CODE_GROUP' AND code_value='ERROR_CATEGORY') x), 'ERROR_CATEGORY', 'UNKNOWN_RESULT', '결과 미확정', 'SYSTEM', 'SYSTEM'),
  ((SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key='CODE_GROUP' AND code_value='RETENTION_ACTION') x), 'RETENTION_ACTION', 'ARCHIVE', '보관소 이관', 'SYSTEM', 'SYSTEM'),
  ((SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key='CODE_GROUP' AND code_value='RETENTION_ACTION') x), 'RETENTION_ACTION', 'PURGE', '정책 삭제', 'SYSTEM', 'SYSTEM'),
  ((SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key='CODE_GROUP' AND code_value='RETENTION_ACTION') x), 'RETENTION_ACTION', 'LEGAL_HOLD', '법적 보존', 'SYSTEM', 'SYSTEM')
) AS src(parent_id, code_key, code_value, description, created_by, updated_by)
ON (tgt.code_key = src.code_key AND tgt.code_value = src.code_value)
WHEN MATCHED THEN UPDATE SET
  tgt.parent_id = src.parent_id,
  tgt.description = src.description,
  tgt.updated_by = src.updated_by,
  tgt.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (parent_id, code_key, code_value, description, created_by, updated_by)
VALUES (src.parent_id, src.code_key, src.code_value, src.description, src.created_by, src.updated_by);

MERGE INTO cpf_message tgt
USING (VALUES
  ('MCPF030002', 'ko', 'FIXED', '요청 시간이 초과되었습니다.', '대상 호출 timeout이 발생했습니다.', 0, NULL, '공통 Timeout 메시지', 'SYSTEM', 'SYSTEM'),
  ('MCPF030003', 'ko', 'FIXED', '연결 대상이 일시적으로 사용할 수 없습니다.', '대상 서비스가 DOWN/OPEN 상태입니다.', 0, NULL, 'Target down 메시지', 'SYSTEM', 'SYSTEM'),
  ('MCPF030004', 'ko', 'FIXED', '처리 결과를 확인 중입니다.', '요청 결과가 UNKNOWN_RESULT로 분류되어 대사가 필요합니다.', 0, NULL, '결과 미확정 메시지', 'SYSTEM', 'SYSTEM'),
  ('MCPF020002', 'ko', 'FIXED', '다른 사용자가 먼저 변경했습니다. 다시 조회해 주세요.', '낙관적 잠금 Version 충돌이 발생했습니다.', 0, NULL, '동시성 충돌 메시지', 'SYSTEM', 'SYSTEM'),
  ('MCPF020003', 'ko', 'FIXED', '동일 요청이 이미 처리되었습니다.', 'Idempotency key가 이미 완료된 요청입니다.', 0, NULL, '멱등 중복 메시지', 'SYSTEM', 'SYSTEM'),
  ('MCPF040001', 'ko', 'FIXED', '첨부파일 검사가 완료되지 않았습니다.', '첨부 다운로드는 CLEAN 상태에서만 허용됩니다.', 0, NULL, '첨부 보안 메시지', 'SYSTEM', 'SYSTEM'),
  ('MCPF040002', 'ko', 'FIXED', '첨부파일이 보안 정책에 의해 격리되었습니다.', 'INFECTED/QUARANTINED 파일 접근이 차단되었습니다.', 0, NULL, '첨부 격리 메시지', 'SYSTEM', 'SYSTEM')
) AS src(message_code, locale, message_format_type, external_message, internal_message, parameter_count, parameter_sample, description, created_by, updated_by)
ON (tgt.message_code = src.message_code AND tgt.locale = src.locale)
WHEN MATCHED THEN UPDATE SET
  tgt.message_format_type = src.message_format_type,
  tgt.external_message = src.external_message,
  tgt.internal_message = src.internal_message,
  tgt.parameter_count = src.parameter_count,
  tgt.parameter_sample = src.parameter_sample,
  tgt.description = src.description,
  tgt.use_yn = 'Y',
  tgt.updated_by = src.updated_by,
  tgt.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (message_code, locale, message_format_type, external_message, internal_message, parameter_count, parameter_sample, description, created_by, updated_by)
VALUES (src.message_code, src.locale, src.message_format_type, src.external_message, src.internal_message, src.parameter_count, src.parameter_sample, src.description, src.created_by, src.updated_by);

MERGE INTO cpf_response_code tgt
USING (VALUES
  ('ECPF030002', 'MCPF030002', 'E', 'CPF', '03', '0002', 504, 'Timeout', 'SYSTEM', 'SYSTEM'),
  ('ECPF030003', 'MCPF030003', 'E', 'CPF', '03', '0003', 503, 'Target down', 'SYSTEM', 'SYSTEM'),
  ('ECPF030004', 'MCPF030004', 'E', 'CPF', '03', '0004', 202, 'UNKNOWN_RESULT', 'SYSTEM', 'SYSTEM'),
  ('ECPF020002', 'MCPF020002', 'E', 'CPF', '02', '0002', 409, 'Optimistic lock conflict', 'SYSTEM', 'SYSTEM'),
  ('ECPF020003', 'MCPF020003', 'E', 'CPF', '02', '0003', 409, 'Idempotency duplicate', 'SYSTEM', 'SYSTEM'),
  ('ECPF040001', 'MCPF040001', 'E', 'CPF', '04', '0001', 423, 'File scan pending', 'SYSTEM', 'SYSTEM'),
  ('ECPF040002', 'MCPF040002', 'E', 'CPF', '04', '0002', 403, 'File quarantined', 'SYSTEM', 'SYSTEM')
) AS src(response_code, message_code, result_type, module_id, response_group, sequence_no, http_status, description, created_by, updated_by)
ON (tgt.response_code = src.response_code)
WHEN MATCHED THEN UPDATE SET
  tgt.message_code = src.message_code,
  tgt.result_type = src.result_type,
  tgt.module_id = src.module_id,
  tgt.response_group = src.response_group,
  tgt.sequence_no = src.sequence_no,
  tgt.http_status = src.http_status,
  tgt.description = src.description,
  tgt.use_yn = 'Y',
  tgt.updated_by = src.updated_by,
  tgt.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (response_code, message_code, result_type, module_id, response_group, sequence_no, http_status, description, created_by, updated_by)
VALUES (src.response_code, src.message_code, src.result_type, src.module_id, src.response_group, src.sequence_no, src.http_status, src.description, src.created_by, src.updated_by);

MERGE INTO cpf_config tgt
USING (VALUES
  ('CPF.BZA.SECURITY.MAX_LOGIN_FAIL_COUNT', '5', 'NUMBER', 'BZA 로그인 실패 잠금 기준', 'N', 'SYSTEM', 'SYSTEM'),
  ('CPF.BZA.SECURITY.ACCESS_TOKEN_TTL_SECONDS', '600', 'NUMBER', 'BZA Access Token TTL', 'N', 'SYSTEM', 'SYSTEM'),
  ('CPF.BZA.SECURITY.REFRESH_TOKEN_TTL_SECONDS', '7200', 'NUMBER', 'BZA Refresh Token TTL', 'N', 'SYSTEM', 'SYSTEM'),
  ('CPF.RETENTION.EXECUTE_ENABLED', 'N', 'BOOLEAN', '실제 Archive/Purge 실행 Kill Switch 기본 OFF', 'N', 'SYSTEM', 'SYSTEM'),
  ('CPF.FILE.DOWNLOAD_REQUIRE_CLEAN', 'Y', 'BOOLEAN', '첨부 다운로드 CLEAN 상태 강제', 'N', 'SYSTEM', 'SYSTEM'),
  ('CPF.HEALTH.INSTANCE_ID_REQUIRED', 'Y', 'BOOLEAN', '운영 Health 응답 인스턴스 식별자 필수', 'N', 'SYSTEM', 'SYSTEM')
) AS src(config_key, config_value, config_type, description, encrypted_yn, created_by, updated_by)
ON (tgt.config_key = src.config_key)
WHEN MATCHED THEN UPDATE SET
  tgt.config_value = src.config_value,
  tgt.config_type = src.config_type,
  tgt.description = src.description,
  tgt.encrypted_yn = src.encrypted_yn,
  tgt.use_yn = 'Y',
  tgt.updated_by = src.updated_by,
  tgt.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (config_key, config_value, config_type, description, encrypted_yn, created_by, updated_by)
VALUES (src.config_key, src.config_value, src.config_type, src.description, src.encrypted_yn, src.created_by, src.updated_by);

MERGE INTO cpf_log_policy tgt
USING (VALUES
  ('ONLINE_DEFAULT', '온라인 거래 기본 로그 정책', 'ONLINE_TRANSACTION', '*', 'INFO', 'Y', 'Y', 'N', 'N', 'Y', 90, 100.00, 100, 'Y', '온라인 Controller/API 기본 로그 정책', 'SYSTEM', 'SYSTEM'),
  ('BATCH_DEFAULT', '배치 기본 로그 정책', 'BATCH_JOB', '*', 'INFO', 'Y', 'Y', 'N', 'N', 'Y', 180, 100.00, 100, 'Y', 'Spring Batch Job 기본 로그 정책', 'SYSTEM', 'SYSTEM'),
  ('ADM_OPERATION_DEFAULT', 'ADM 운영 기본 로그 정책', 'MODULE', 'ADM', 'INFO', 'Y', 'Y', 'N', 'N', 'Y', 365, 100.00, 50, 'Y', 'ADM 운영 API 기본 로그 정책', 'SYSTEM', 'SYSTEM')
) AS src(policy_key, policy_name, target_type, target_id, log_level, db_log_enabled_yn, file_log_enabled_yn, request_body_log_yn, response_body_log_yn, error_stack_log_yn, retention_days, sampling_rate, priority, active_yn, description, created_by, updated_by)
ON (tgt.policy_key = src.policy_key)
WHEN MATCHED THEN UPDATE SET
  tgt.policy_name = src.policy_name,
  tgt.target_type = src.target_type,
  tgt.target_id = src.target_id,
  tgt.log_level = src.log_level,
  tgt.db_log_enabled_yn = src.db_log_enabled_yn,
  tgt.file_log_enabled_yn = src.file_log_enabled_yn,
  tgt.request_body_log_yn = src.request_body_log_yn,
  tgt.response_body_log_yn = src.response_body_log_yn,
  tgt.error_stack_log_yn = src.error_stack_log_yn,
  tgt.retention_days = src.retention_days,
  tgt.sampling_rate = src.sampling_rate,
  tgt.priority = src.priority,
  tgt.active_yn = src.active_yn,
  tgt.description = src.description,
  tgt.updated_by = src.updated_by,
  tgt.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (policy_key, policy_name, target_type, target_id, log_level, db_log_enabled_yn, file_log_enabled_yn, request_body_log_yn, response_body_log_yn, error_stack_log_yn, retention_days, sampling_rate, priority, active_yn, description, created_by, updated_by)
VALUES (src.policy_key, src.policy_name, src.target_type, src.target_id, src.log_level, src.db_log_enabled_yn, src.file_log_enabled_yn, src.request_body_log_yn, src.response_body_log_yn, src.error_stack_log_yn, src.retention_days, src.sampling_rate, src.priority, src.active_yn, src.description, src.created_by, src.updated_by);

MERGE INTO cpf_security_jwt_key tgt
USING (VALUES
  ('local-cpf-hs256-001', 'CPF', 'HS256', 'ENV:CPF_CMN_SECURITY_JWT_SECRET', 'Y', NULL, 'SYSTEM', 'SYSTEM')
) AS src(KEY_ID, ISSUER, ALGORITHM, SECRET_REF, ACTIVE_YN, EXPIRE_AT, created_by, updated_by)
ON (tgt.KEY_ID = src.KEY_ID)
WHEN MATCHED THEN UPDATE SET
  tgt.ISSUER = src.ISSUER,
  tgt.ALGORITHM = src.ALGORITHM,
  tgt.SECRET_REF = src.SECRET_REF,
  tgt.ACTIVE_YN = src.ACTIVE_YN,
  tgt.EXPIRE_AT = src.EXPIRE_AT,
  tgt.updated_by = src.updated_by,
  tgt.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (KEY_ID, ISSUER, ALGORITHM, SECRET_REF, ACTIVE_YN, EXPIRE_AT, created_by, updated_by)
VALUES (src.KEY_ID, src.ISSUER, src.ALGORITHM, src.SECRET_REF, src.ACTIVE_YN, src.EXPIRE_AT, src.created_by, src.updated_by);

INSERT INTO cpf_cache_refresh_event (cache_name, event_type, event_key, source_was_id, published_by, created_by, updated_by)
SELECT 'ALL', 'INITIAL_LOAD', 'INITIAL_FRAMEWORK_SEED', 'SQL', 'SYSTEM', 'SYSTEM', 'SYSTEM'
WHERE NOT EXISTS (
    SELECT 1
    FROM cpf_cache_refresh_event
    WHERE cache_name = 'ALL'
      AND event_type = 'INITIAL_LOAD'
      AND event_key = 'INITIAL_FRAMEWORK_SEED'
);

MERGE INTO cpf_notification_rule tgt
USING (VALUES
  ('BATCH_EXECUTION', 'FAILED', 'ADM', 'BATCH_FAILED_DEFAULT', 'ERROR', 'ADM_BATCH_OPERATOR', 'Y', 'SYSTEM', 'SYSTEM'),
  ('SECURITY_EVENT', 'LOGIN_FAILURE', 'ADM', 'SECURITY_LOGIN_FAILURE', 'WARN', 'ADM_SECURITY_OPERATOR', 'Y', 'SYSTEM', 'SYSTEM')
) AS src(event_type, event_sub_type, channel_code, template_code, severity, receiver_group, use_yn, created_by, updated_by)
ON (tgt.event_type = src.event_type AND tgt.event_sub_type = src.event_sub_type AND tgt.channel_code = src.channel_code)
WHEN MATCHED THEN UPDATE SET
  tgt.template_code = src.template_code,
  tgt.severity = src.severity,
  tgt.receiver_group = src.receiver_group,
  tgt.use_yn = src.use_yn,
  tgt.updated_by = src.updated_by,
  tgt.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (event_type, event_sub_type, channel_code, template_code, severity, receiver_group, use_yn, created_by, updated_by)
VALUES (src.event_type, src.event_sub_type, src.channel_code, src.template_code, src.severity, src.receiver_group, src.use_yn, src.created_by, src.updated_by);

INSERT INTO cpf_code (parent_id, code_key, code_value, description, created_by, updated_by)
SELECT NULL, 'CODE_GROUP', 'SORT_DIRECTION', '표준 정렬 방향', 'SYSTEM', 'SYSTEM'
WHERE NOT EXISTS (SELECT 1 FROM cpf_code WHERE code_key='CODE_GROUP' AND code_value='SORT_DIRECTION');

MERGE INTO cpf_code tgt
USING (VALUES
  ((SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key='CODE_GROUP' AND code_value='SORT_DIRECTION') x), 'SORT_DIRECTION', 'ASC', '오름차순', 'SYSTEM', 'SYSTEM'),
  ((SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key='CODE_GROUP' AND code_value='SORT_DIRECTION') x), 'SORT_DIRECTION', 'DESC', '내림차순', 'SYSTEM', 'SYSTEM')
) AS src(parent_id, code_key, code_value, description, created_by, updated_by)
ON (tgt.code_key = src.code_key AND tgt.code_value = src.code_value)
WHEN MATCHED THEN UPDATE SET
  tgt.parent_id = src.parent_id,
  tgt.description = src.description,
  tgt.updated_by = src.updated_by,
  tgt.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (parent_id, code_key, code_value, description, created_by, updated_by)
VALUES (src.parent_id, src.code_key, src.code_value, src.description, src.created_by, src.updated_by);

MERGE INTO cpf_message tgt
USING (VALUES
  ('MCPF020004', 'ko', 'FIXED', '요청 사용자 정보가 인증 사용자와 일치하지 않습니다.', 'Body requester spoofing이 차단되었습니다.', 0, NULL, 'Requester spoof 차단', 'SYSTEM', 'SYSTEM'),
  ('MCPF020005', 'ko', 'FIXED', '이미 사용된 정책 버전은 직접 수정할 수 없습니다.', '사용된 Approval Policy version은 immutable입니다.', 0, NULL, '정책 버전 불변성', 'SYSTEM', 'SYSTEM'),
  ('MCPF020006', 'ko', 'FIXED', '동일 작업 식별자가 다른 요청에 사용되었습니다.', 'operationId payload 충돌입니다.', 0, NULL, '멱등 작업 충돌', 'SYSTEM', 'SYSTEM'),
  ('MCPF020007', 'ko', 'FIXED', '현재 데이터가 다른 요청에서 변경되었습니다.', 'expectedVersion CAS가 실패했습니다.', 0, NULL, '낙관적 잠금 재조회', 'SYSTEM', 'SYSTEM'),
  ('MCPF040003', 'ko', 'FIXED', '보존 정책에 의해 해당 데이터는 삭제할 수 없습니다.', 'LEGAL_HOLD가 적용되어 destructive retention을 차단했습니다.', 0, NULL, 'Legal hold', 'SYSTEM', 'SYSTEM'),
  ('MCPF040004', 'ko', 'FIXED', '보존 작업 실행이 비활성화되어 있습니다.', 'CPF.RETENTION.EXECUTE_ENABLED kill switch가 OFF입니다.', 0, NULL, 'Retention kill switch', 'SYSTEM', 'SYSTEM'),
  ('MCPF050001', 'ko', 'FIXED', 'Secret 원문은 조회할 수 없습니다.', 'Secret API는 metadata/reference만 노출합니다.', 0, NULL, 'Secret 비노출', 'SYSTEM', 'SYSTEM'),
  ('MCPF050002', 'ko', 'FIXED', '테넌트 식별정보가 필요합니다.', 'Tenant mode에서 resolver가 tenantId를 결정하지 못했습니다.', 0, NULL, 'Tenant 필수', 'SYSTEM', 'SYSTEM')
) AS src(message_code, locale, message_format_type, external_message, internal_message, parameter_count, parameter_sample, description, created_by, updated_by)
ON (tgt.message_code = src.message_code AND tgt.locale = src.locale)
WHEN MATCHED THEN UPDATE SET
  tgt.message_format_type = src.message_format_type,
  tgt.external_message = src.external_message,
  tgt.internal_message = src.internal_message,
  tgt.description = src.description,
  tgt.use_yn = 'Y',
  tgt.updated_by = src.updated_by,
  tgt.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (message_code, locale, message_format_type, external_message, internal_message, parameter_count, parameter_sample, description, created_by, updated_by)
VALUES (src.message_code, src.locale, src.message_format_type, src.external_message, src.internal_message, src.parameter_count, src.parameter_sample, src.description, src.created_by, src.updated_by);

MERGE INTO cpf_response_code tgt
USING (VALUES
  ('ECPF020004', 'MCPF020004', 'E', 'CPF', '02', '0004', 403, 'Requester spoof blocked', 'SYSTEM', 'SYSTEM'),
  ('ECPF020005', 'MCPF020005', 'E', 'CPF', '02', '0005', 409, 'Policy version immutable', 'SYSTEM', 'SYSTEM'),
  ('ECPF020006', 'MCPF020006', 'E', 'CPF', '02', '0006', 409, 'Operation id conflict', 'SYSTEM', 'SYSTEM'),
  ('ECPF020007', 'MCPF020007', 'E', 'CPF', '02', '0007', 409, 'Optimistic lock retry', 'SYSTEM', 'SYSTEM'),
  ('ECPF040003', 'MCPF040003', 'E', 'CPF', '04', '0003', 423, 'Legal hold', 'SYSTEM', 'SYSTEM'),
  ('ECPF040004', 'MCPF040004', 'E', 'CPF', '04', '0004', 403, 'Retention disabled', 'SYSTEM', 'SYSTEM'),
  ('ECPF050001', 'MCPF050001', 'E', 'CPF', '05', '0001', 403, 'Secret value hidden', 'SYSTEM', 'SYSTEM'),
  ('ECPF050002', 'MCPF050002', 'E', 'CPF', '05', '0002', 400, 'Tenant required', 'SYSTEM', 'SYSTEM')
) AS src(response_code, message_code, result_type, module_id, response_group, sequence_no, http_status, description, created_by, updated_by)
ON (tgt.response_code = src.response_code)
WHEN MATCHED THEN UPDATE SET
  tgt.message_code = src.message_code,
  tgt.result_type = src.result_type,
  tgt.module_id = src.module_id,
  tgt.response_group = src.response_group,
  tgt.sequence_no = src.sequence_no,
  tgt.http_status = src.http_status,
  tgt.description = src.description,
  tgt.use_yn = 'Y',
  tgt.updated_by = src.updated_by,
  tgt.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (response_code, message_code, result_type, module_id, response_group, sequence_no, http_status, description, created_by, updated_by)
VALUES (src.response_code, src.message_code, src.result_type, src.module_id, src.response_group, src.sequence_no, src.http_status, src.description, src.created_by, src.updated_by);

MERGE INTO cpf_config tgt
USING (VALUES
  ('CPF.PAGING.DEFAULT_SIZE', '20', 'NUMBER', '공통 Page 기본 크기', 'N', 'SYSTEM', 'SYSTEM'),
  ('CPF.PAGING.MAX_SIZE', '200', 'NUMBER', '공통 Page 최대 크기', 'N', 'SYSTEM', 'SYSTEM'),
  ('CPF.RETENTION.DRY_RUN_DEFAULT', 'Y', 'BOOLEAN', 'Retention 기본 Dry-run', 'N', 'SYSTEM', 'SYSTEM'),
  ('CPF.RETENTION.EXECUTE_ENABLED', 'N', 'BOOLEAN', '실제 Archive/Purge 실행 Kill Switch 기본 OFF', 'N', 'SYSTEM', 'SYSTEM'),
  ('CPF.SECRET.CACHE_TTL_SECONDS', '300', 'NUMBER', 'Secret metadata/cache 기본 TTL', 'N', 'SYSTEM', 'SYSTEM'),
  ('CPF.TENANT.ENABLED', 'N', 'BOOLEAN', 'Tenant context 기능 기본 OFF', 'N', 'SYSTEM', 'SYSTEM'),
  ('CPF.HEALTH.REMOTE_DEPENDENCY_GATES_READINESS', 'N', 'BOOLEAN', 'Remote owner 장애가 local readiness를 직접 차단하지 않음', 'N', 'SYSTEM', 'SYSTEM')
) AS src(config_key, config_value, config_type, description, encrypted_yn, created_by, updated_by)
ON (tgt.config_key = src.config_key)
WHEN MATCHED THEN UPDATE SET
  tgt.config_value = src.config_value,
  tgt.config_type = src.config_type,
  tgt.description = src.description,
  tgt.encrypted_yn = src.encrypted_yn,
  tgt.use_yn = 'Y',
  tgt.updated_by = src.updated_by,
  tgt.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (config_key, config_value, config_type, description, encrypted_yn, created_by, updated_by)
VALUES (src.config_key, src.config_value, src.config_type, src.description, src.encrypted_yn, src.created_by, src.updated_by);

MERGE INTO cpf_code tgt
USING (VALUES
  ((SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key='CODE_GROUP' AND code_value='REQUEST_TYPE') x), 'REQUEST_TYPE', 'O', '온라인 요청', 'SYSTEM', 'SYSTEM'),
  ((SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key='CODE_GROUP' AND code_value='REQUEST_TYPE') x), 'REQUEST_TYPE', 'S', '공유 내부 서비스 요청', 'SYSTEM', 'SYSTEM'),
  ((SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key='CODE_GROUP' AND code_value='REQUEST_TYPE') x), 'REQUEST_TYPE', 'B', '배치 요청', 'SYSTEM', 'SYSTEM'),
  ((SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key='CODE_GROUP' AND code_value='CHANNEL_CODE') x), 'CHANNEL_CODE', 'APP', '모바일 앱 채널', 'SYSTEM', 'SYSTEM'),
  ((SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key='CODE_GROUP' AND code_value='CHANNEL_CODE') x), 'CHANNEL_CODE', 'JUT', 'JUnit/자동 테스트 채널', 'SYSTEM', 'SYSTEM'),
  ((SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key='CODE_GROUP' AND code_value='RESULT_TYPE') x), 'RESULT_TYPE', 'W', '경고/부분 성공', 'SYSTEM', 'SYSTEM'),
  ((SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key='CODE_GROUP' AND code_value='MESSAGE_FORMAT_TYPE') x), 'MESSAGE_FORMAT_TYPE', 'PARAMETER', 'Named parameter 메시지', 'SYSTEM', 'SYSTEM'),
  ((SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key='CODE_GROUP' AND code_value='ASYNC_STATUS') x), 'ASYNC_STATUS', 'FAILED', '비동기 처리 실패', 'SYSTEM', 'SYSTEM'),
  ((SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key='CODE_GROUP' AND code_value='BATCH_JOB_TYPE') x), 'BATCH_JOB_TYPE', 'SPRING_BATCH', 'Spring Batch Job', 'SYSTEM', 'SYSTEM'),
  ((SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key='CODE_GROUP' AND code_value='BATCH_JOB_TYPE') x), 'BATCH_JOB_TYPE', 'WORKER', '지속 Worker', 'SYSTEM', 'SYSTEM'),
  ((SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key='CODE_GROUP' AND code_value='BATCH_JOB_TYPE') x), 'BATCH_JOB_TYPE', 'SCHEDULER', 'Scheduler Job', 'SYSTEM', 'SYSTEM'),
  ((SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key='CODE_GROUP' AND code_value='BATCH_JOB_TYPE') x), 'BATCH_JOB_TYPE', 'CENTER_CUT', 'Center-Cut 대량 처리', 'SYSTEM', 'SYSTEM')
) AS src(parent_id, code_key, code_value, description, created_by, updated_by)
ON (tgt.code_key = src.code_key AND tgt.code_value = src.code_value)
WHEN MATCHED THEN UPDATE SET
  tgt.parent_id = src.parent_id,
  tgt.description = src.description,
  tgt.use_yn = 'Y',
  tgt.updated_by = src.updated_by,
  tgt.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (parent_id, code_key, code_value, description, created_by, updated_by)
VALUES (src.parent_id, src.code_key, src.code_value, src.description, src.created_by, src.updated_by);

-- ===== END 50_framework_seed_data.sql =====

-- ===== BEGIN 52_standard_execution_alias_seed.sql =====
-- AUTO-GENERATED from cpf-tools/db/canonical/seed-model.json
-- vendor=postgresql; source=52_standard_execution_alias_seed.sql
-- USE lines are CPF packaging directives and are stripped by the vendor executor.



-- CPF_LOGICAL_DATABASE=cpfDB
-- CPF_USE_LOGICAL_DATABASE=cpfDB
MERGE INTO cpf_standard_execution_alias tgt
USING (VALUES
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
) AS src(legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by)
ON (tgt.legacy_execution_id = src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET
  tgt.standard_execution_id = src.standard_execution_id,
  tgt.migration_reason = src.migration_reason,
  tgt.updated_by = src.updated_by,
  tgt.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by)
VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);

-- ===== END 52_standard_execution_alias_seed.sql =====

-- ===== BEGIN 56_bza_product_seed.sql =====
-- AUTO-GENERATED from cpf-tools/db/canonical/seed-model.json
-- vendor=postgresql; source=56_bza_product_seed.sql
-- USE lines are CPF packaging directives and are stripped by the vendor executor.



-- CPF_LOGICAL_DATABASE=bzaDB
-- CPF_USE_LOGICAL_DATABASE=bzaDB
MERGE INTO bza_role tgt
USING (VALUES
  ('BZA_ADMIN', '업무 관리자', 'Y', 'ALL', 'Y', 'SYSTEM', 'SYSTEM'),
  ('BZA_OPERATOR', '업무 운영자', 'Y', 'ORGANIZATION', 'Y', 'SYSTEM', 'SYSTEM'),
  ('BZA_APPROVER', '업무 결재자', 'Y', 'ORGANIZATION', 'Y', 'SYSTEM', 'SYSTEM'),
  ('BZA_VIEWER', '업무 조회자', 'N', 'ORGANIZATION', 'Y', 'SYSTEM', 'SYSTEM')
) AS src(role_code, role_name, write_allowed_yn, data_scope, use_yn, created_by, updated_by)
ON (tgt.role_code = src.role_code)
WHEN MATCHED THEN UPDATE SET
  tgt.role_name = src.role_name,
  tgt.write_allowed_yn = src.write_allowed_yn,
  tgt.data_scope = src.data_scope,
  tgt.use_yn = src.use_yn,
  tgt.updated_by = src.updated_by,
  tgt.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_code, role_name, write_allowed_yn, data_scope, use_yn, created_by, updated_by)
VALUES (src.role_code, src.role_name, src.write_allowed_yn, src.data_scope, src.use_yn, src.created_by, src.updated_by);

MERGE INTO bza_menu tgt
USING (VALUES
  ('BZA_DASHBOARD', '업무 관리자 대시보드', NULL, 'BZA', '/bza', 'dashboard', 'ALL', '/api/bza/dashboard', 10, 'Y', 'SYSTEM', 'SYSTEM'),
  ('BZA_ORGANIZATION', '조직 관리', NULL, 'BZA', '/bza/organizations', 'organization', 'ALL', '/api/bza/organizations', 20, 'Y', 'SYSTEM', 'SYSTEM'),
  ('BZA_EMPLOYEE', '직원·소속 관리', NULL, 'BZA', '/bza/employees', 'employee', 'ALL', '/api/bza/employees', 30, 'Y', 'SYSTEM', 'SYSTEM'),
  ('BZA_AUTHORIZATION', '업무 권한 관리', NULL, 'BZA', '/bza/authorization', 'shield', 'ALL', '/api/bza/authorization', 40, 'Y', 'SYSTEM', 'SYSTEM'),
  ('BZA_APPROVAL', '업무 결재 관리', NULL, 'BZA', '/bza/approvals', 'approval', 'ALL', '/api/bza/approvals', 50, 'Y', 'SYSTEM', 'SYSTEM'),
  ('BZA_AUDIT', '업무 감사 조회', NULL, 'BZA', '/bza/audits', 'audit', 'ALL', '/api/bza/audits', 60, 'Y', 'SYSTEM', 'SYSTEM'),
  ('BZA_ATTACHMENT', '첨부 관리', NULL, 'BZA', '/bza/attachments', 'attachment', 'ALL', '/api/bza/attachments', 70, 'Y', 'SYSTEM', 'SYSTEM'),
  ('BZA_SETTING', '업무 관리자 설정', NULL, 'BZA', '/bza/settings', 'setting', 'ALL', '/api/bza/settings', 80, 'Y', 'SYSTEM', 'SYSTEM')
) AS src(menu_code, menu_name, parent_menu_code, module_code, route_path, icon_code, environment_code, api_path, sort_order, use_yn, created_by, updated_by)
ON (tgt.menu_code = src.menu_code)
WHEN MATCHED THEN UPDATE SET
  tgt.menu_name = src.menu_name,
  tgt.parent_menu_code = src.parent_menu_code,
  tgt.module_code = src.module_code,
  tgt.route_path = src.route_path,
  tgt.icon_code = src.icon_code,
  tgt.environment_code = src.environment_code,
  tgt.api_path = src.api_path,
  tgt.sort_order = src.sort_order,
  tgt.use_yn = src.use_yn,
  tgt.updated_by = src.updated_by,
  tgt.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (menu_code, menu_name, parent_menu_code, module_code, route_path, icon_code, environment_code, api_path, sort_order, use_yn, created_by, updated_by)
VALUES (src.menu_code, src.menu_name, src.parent_menu_code, src.module_code, src.route_path, src.icon_code, src.environment_code, src.api_path, src.sort_order, src.use_yn, src.created_by, src.updated_by);

MERGE INTO bza_permission tgt
USING (
  SELECT 'BZA_ADMIN' AS role_code, menu_code AS menu_code, 'ALL' AS button_code, 'API' AS permission_type, '*' AS http_method, (api_path || '/**') AS api_pattern, NULL AS domain_code, environment_code AS environment_code, 'ALL' AS data_scope, 'Y' AS allow_yn, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by
  FROM bza_menu
  WHERE use_yn = 'Y'
) src
ON (tgt.role_code = src.role_code AND tgt.menu_code = src.menu_code AND tgt.button_code = src.button_code AND tgt.permission_type = src.permission_type AND tgt.environment_code = src.environment_code)
WHEN MATCHED THEN UPDATE SET
  tgt.http_method = src.http_method,
  tgt.api_pattern = src.api_pattern,
  tgt.data_scope = src.data_scope,
  tgt.allow_yn = src.allow_yn,
  tgt.use_yn = src.use_yn,
  tgt.updated_by = src.updated_by,
  tgt.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_code, menu_code, button_code, permission_type, http_method, api_pattern, domain_code, environment_code, data_scope, allow_yn, use_yn, created_by, updated_by)
VALUES (src.role_code, src.menu_code, src.button_code, src.permission_type, src.http_method, src.api_pattern, src.domain_code, src.environment_code, src.data_scope, src.allow_yn, src.use_yn, src.created_by, src.updated_by);

MERGE INTO bza_permission tgt
USING (VALUES
  ('BZA_OPERATOR', 'BZA_DASHBOARD', 'READ', 'API', 'GET', '/api/bza/dashboard/**', NULL, 'ALL', 'ORGANIZATION', 'Y', 'Y', 'SYSTEM', 'SYSTEM'),
  ('BZA_OPERATOR', 'BZA_ORGANIZATION', 'READ', 'API', 'GET', '/api/bza/organizations/**', NULL, 'ALL', 'ORGANIZATION', 'Y', 'Y', 'SYSTEM', 'SYSTEM'),
  ('BZA_OPERATOR', 'BZA_EMPLOYEE', 'READ', 'API', 'GET', '/api/bza/employees/**', NULL, 'ALL', 'ORGANIZATION', 'Y', 'Y', 'SYSTEM', 'SYSTEM'),
  ('BZA_APPROVER', 'BZA_APPROVAL', 'READ', 'API', 'GET', '/api/bza/approvals/**', NULL, 'ALL', 'ORGANIZATION', 'Y', 'Y', 'SYSTEM', 'SYSTEM'),
  ('BZA_APPROVER', 'BZA_APPROVAL', 'DECIDE', 'API', 'POST', '/api/bza/approvals/*/decisions', NULL, 'ALL', 'ORGANIZATION', 'Y', 'Y', 'SYSTEM', 'SYSTEM'),
  ('BZA_VIEWER', 'BZA_DASHBOARD', 'READ', 'API', 'GET', '/api/bza/dashboard/**', NULL, 'ALL', 'ORGANIZATION', 'Y', 'Y', 'SYSTEM', 'SYSTEM'),
  ('BZA_VIEWER', 'BZA_AUDIT', 'READ', 'API', 'GET', '/api/bza/audits/**', NULL, 'ALL', 'ORGANIZATION', 'Y', 'Y', 'SYSTEM', 'SYSTEM')
) AS src(role_code, menu_code, button_code, permission_type, http_method, api_pattern, domain_code, environment_code, data_scope, allow_yn, use_yn, created_by, updated_by)
ON (tgt.role_code = src.role_code AND tgt.menu_code = src.menu_code AND tgt.button_code = src.button_code AND tgt.permission_type = src.permission_type AND tgt.environment_code = src.environment_code)
WHEN MATCHED THEN UPDATE SET
  tgt.http_method = src.http_method,
  tgt.api_pattern = src.api_pattern,
  tgt.data_scope = src.data_scope,
  tgt.allow_yn = src.allow_yn,
  tgt.use_yn = src.use_yn,
  tgt.updated_by = src.updated_by,
  tgt.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_code, menu_code, button_code, permission_type, http_method, api_pattern, domain_code, environment_code, data_scope, allow_yn, use_yn, created_by, updated_by)
VALUES (src.role_code, src.menu_code, src.button_code, src.permission_type, src.http_method, src.api_pattern, src.domain_code, src.environment_code, src.data_scope, src.allow_yn, src.use_yn, src.created_by, src.updated_by);

MERGE INTO bza_project_setting tgt
USING (VALUES
  ('BZA.APPROVAL.SELF_APPROVAL_ALLOWED', 'N', '기본 자기승인 차단 정책', 'Y', 'SYSTEM', 'SYSTEM'),
  ('BZA.APPROVAL.DEFAULT_DUE_HOURS', '24', '기본 결재 SLA 시간', 'Y', 'SYSTEM', 'SYSTEM'),
  ('BZA.APPROVAL.REQUIRE_PAYLOAD_HASH', 'Y', '결재 대상 Payload 변조 검증용 SHA-256 사용', 'Y', 'SYSTEM', 'SYSTEM'),
  ('BZA.AUDIT.HASH_CHAIN_ENABLED', 'Y', '업무 감사 로그 hash-chain 검증 사용', 'Y', 'SYSTEM', 'SYSTEM'),
  ('BZA.ATTACHMENT.SECURITY_SCAN_REQUIRED', 'Y', '첨부 보안검사 완료 후 사용 허용', 'Y', 'SYSTEM', 'SYSTEM'),
  ('BZA.ATTACHMENT.DEFAULT_RETENTION_DAYS', '365', '첨부 기본 보존일수', 'Y', 'SYSTEM', 'SYSTEM')
) AS src(setting_key, setting_value, description, use_yn, created_by, updated_by)
ON (tgt.setting_key = src.setting_key)
WHEN MATCHED THEN UPDATE SET
  tgt.setting_value = src.setting_value,
  tgt.description = src.description,
  tgt.use_yn = src.use_yn,
  tgt.updated_by = src.updated_by,
  tgt.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (setting_key, setting_value, description, use_yn, created_by, updated_by)
VALUES (src.setting_key, src.setting_value, src.description, src.use_yn, src.created_by, src.updated_by);

-- ===== END 56_bza_product_seed.sql =====

-- ===== BEGIN 60_adm_seed_data.sql =====
-- AUTO-GENERATED from cpf-tools/db/canonical/seed-model.json
-- vendor=postgresql; source=60_adm_seed_data.sql
-- USE lines are CPF packaging directives and are stripped by the vendor executor.



-- CPF_LOGICAL_DATABASE=admDB
-- CPF_USE_LOGICAL_DATABASE=admDB
MERGE INTO adm_role tgt
USING (VALUES
  ('ADM_ADMIN', '프레임워크 관리자', 'ADMIN', '모든 ADM 메뉴와 운영 작업을 관리합니다.', 'Y', 'SYSTEM', 'SYSTEM'),
  ('ADM_DEV_OPERATOR', '개발자 운영자', 'DEVELOPER_OPERATOR', '로그, 캐시, 코드, 메시지, 설정, 배치 관제를 운영합니다.', 'Y', 'SYSTEM', 'SYSTEM'),
  ('ADM_BIZ_OPERATOR', '업무 운영자', 'BUSINESS_OPERATOR', '회원, 거래 로그, 배치, 캐시 같은 업무 운영 기능을 수행합니다.', 'Y', 'SYSTEM', 'SYSTEM'),
  ('ADM_VIEWER', '조회 전용 운영자', 'VIEWER', '운영 정보를 조회만 할 수 있습니다.', 'Y', 'SYSTEM', 'SYSTEM'),
  ('ADM_OPERATOR', '운영자 호환 역할', 'DEVELOPER_OPERATOR', '기존 ADM_OPERATOR 호환을 위한 역할입니다.', 'Y', 'SYSTEM', 'SYSTEM')
) AS src(ROLE_ID, ROLE_NAME, ROLE_TYPE, DESCRIPTION, USE_YN, created_by, updated_by)
ON (tgt.ROLE_ID = src.ROLE_ID)
WHEN MATCHED THEN UPDATE SET
  tgt.ROLE_NAME = src.ROLE_NAME,
  tgt.ROLE_TYPE = src.ROLE_TYPE,
  tgt.DESCRIPTION = src.DESCRIPTION,
  tgt.USE_YN = src.USE_YN,
  tgt.updated_by = src.updated_by,
  tgt.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (ROLE_ID, ROLE_NAME, ROLE_TYPE, DESCRIPTION, USE_YN, created_by, updated_by)
VALUES (src.ROLE_ID, src.ROLE_NAME, src.ROLE_TYPE, src.DESCRIPTION, src.USE_YN, src.created_by, src.updated_by);

MERGE INTO adm_menu tgt
USING (VALUES
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
) AS src(MENU_ID, PARENT_MENU_ID, MENU_NAME, MENU_PATH, SORT_ORDER, USE_YN, created_by, updated_by)
ON (tgt.MENU_ID = src.MENU_ID)
WHEN MATCHED THEN UPDATE SET
  tgt.PARENT_MENU_ID = src.PARENT_MENU_ID,
  tgt.MENU_NAME = src.MENU_NAME,
  tgt.MENU_PATH = src.MENU_PATH,
  tgt.SORT_ORDER = src.SORT_ORDER,
  tgt.USE_YN = src.USE_YN,
  tgt.updated_by = src.updated_by,
  tgt.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (MENU_ID, PARENT_MENU_ID, MENU_NAME, MENU_PATH, SORT_ORDER, USE_YN, created_by, updated_by)
VALUES (src.MENU_ID, src.PARENT_MENU_ID, src.MENU_NAME, src.MENU_PATH, src.SORT_ORDER, src.USE_YN, src.created_by, src.updated_by);

MERGE INTO adm_button tgt
USING (VALUES
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
  ('OPERATOR_ROLE_UPDATE', 'OPERATOR', 'ROLE_UPDATE', '역할 부여', 'PUT', '/adm/api/operators/*/roles', 30, 'Y', 'SYSTEM', 'SYSTEM'),
  ('OPERATOR_STATUS_UPDATE', 'OPERATOR', 'STATUS_UPDATE', '계정 상태 변경', 'PUT', '/adm/api/operators/*/status', 40, 'Y', 'SYSTEM', 'SYSTEM'),
  ('OPERATOR_CONTACT_UPDATE', 'OPERATOR', 'CONTACT_UPDATE', '연락처 변경', 'PUT', '/adm/api/operators/*/contacts', 50, 'Y', 'SYSTEM', 'SYSTEM'),
  ('OPERATOR_PII_RAW', 'OPERATOR', 'PII_RAW', '연락처 원문 조회', 'POST', '/adm/api/operators/*/contacts/raw', 60, 'Y', 'SYSTEM', 'SYSTEM')
) AS src(BUTTON_ID, MENU_ID, ACTION_CODE, BUTTON_NAME, HTTP_METHOD, API_PATTERN, SORT_ORDER, USE_YN, created_by, updated_by)
ON (tgt.BUTTON_ID = src.BUTTON_ID)
WHEN MATCHED THEN UPDATE SET
  tgt.MENU_ID = src.MENU_ID,
  tgt.ACTION_CODE = src.ACTION_CODE,
  tgt.BUTTON_NAME = src.BUTTON_NAME,
  tgt.HTTP_METHOD = src.HTTP_METHOD,
  tgt.API_PATTERN = src.API_PATTERN,
  tgt.SORT_ORDER = src.SORT_ORDER,
  tgt.USE_YN = src.USE_YN,
  tgt.updated_by = src.updated_by,
  tgt.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (BUTTON_ID, MENU_ID, ACTION_CODE, BUTTON_NAME, HTTP_METHOD, API_PATTERN, SORT_ORDER, USE_YN, created_by, updated_by)
VALUES (src.BUTTON_ID, src.MENU_ID, src.ACTION_CODE, src.BUTTON_NAME, src.HTTP_METHOD, src.API_PATTERN, src.SORT_ORDER, src.USE_YN, src.created_by, src.updated_by);

MERGE INTO adm_password_policy tgt
USING (VALUES
  ('DEFAULT', 12, 'Y', 'Y', 'Y', 'Y', 5, 90, 5, 'Y', 'SYSTEM', 'SYSTEM')
) AS src(POLICY_ID, MIN_LENGTH, REQUIRE_UPPER_YN, REQUIRE_LOWER_YN, REQUIRE_DIGIT_YN, REQUIRE_SPECIAL_YN, MAX_FAIL_COUNT, EXPIRE_DAYS, HISTORY_LIMIT, USE_YN, created_by, updated_by)
ON (tgt.POLICY_ID = src.POLICY_ID)
WHEN MATCHED THEN UPDATE SET
  tgt.MIN_LENGTH = src.MIN_LENGTH,
  tgt.REQUIRE_UPPER_YN = src.REQUIRE_UPPER_YN,
  tgt.REQUIRE_LOWER_YN = src.REQUIRE_LOWER_YN,
  tgt.REQUIRE_DIGIT_YN = src.REQUIRE_DIGIT_YN,
  tgt.REQUIRE_SPECIAL_YN = src.REQUIRE_SPECIAL_YN,
  tgt.MAX_FAIL_COUNT = src.MAX_FAIL_COUNT,
  tgt.EXPIRE_DAYS = src.EXPIRE_DAYS,
  tgt.HISTORY_LIMIT = src.HISTORY_LIMIT,
  tgt.USE_YN = src.USE_YN,
  tgt.updated_by = src.updated_by,
  tgt.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (POLICY_ID, MIN_LENGTH, REQUIRE_UPPER_YN, REQUIRE_LOWER_YN, REQUIRE_DIGIT_YN, REQUIRE_SPECIAL_YN, MAX_FAIL_COUNT, EXPIRE_DAYS, HISTORY_LIMIT, USE_YN, created_by, updated_by)
VALUES (src.POLICY_ID, src.MIN_LENGTH, src.REQUIRE_UPPER_YN, src.REQUIRE_LOWER_YN, src.REQUIRE_DIGIT_YN, src.REQUIRE_SPECIAL_YN, src.MAX_FAIL_COUNT, src.EXPIRE_DAYS, src.HISTORY_LIMIT, src.USE_YN, src.created_by, src.updated_by);

MERGE INTO adm_role_menu tgt
USING (
  SELECT 'ADM_ADMIN' AS ROLE_ID, MENU_ID AS MENU_ID, 'Y' AS READ_YN, 'Y' AS WRITE_YN, 'Y' AS DELETE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by
  FROM adm_menu
) src
ON (tgt.ROLE_ID = src.ROLE_ID AND tgt.MENU_ID = src.MENU_ID)
WHEN MATCHED THEN UPDATE SET
  tgt.READ_YN = src.READ_YN,
  tgt.WRITE_YN = src.WRITE_YN,
  tgt.DELETE_YN = src.DELETE_YN,
  tgt.updated_by = src.updated_by,
  tgt.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (ROLE_ID, MENU_ID, READ_YN, WRITE_YN, DELETE_YN, created_by, updated_by)
VALUES (src.ROLE_ID, src.MENU_ID, src.READ_YN, src.WRITE_YN, src.DELETE_YN, src.created_by, src.updated_by);

MERGE INTO adm_role_menu tgt
USING (
  SELECT 'ADM_DEV_OPERATOR' AS ROLE_ID, MENU_ID AS MENU_ID, 'Y' AS READ_YN, CASE WHEN MENU_ID IN ('TRANSACTION_META', 'CHANNEL_POLICY', 'REMOTE_LOG', 'BATCH', 'RELIABILITY', 'NOTIFICATION', 'DOWNLOAD', 'CACHE', 'MESSAGE', 'CODE', 'RESPONSE_CODE', 'CONFIG', 'DYNAMIC_LOG', 'LOG_POLICY') THEN 'Y' ELSE 'N' END AS WRITE_YN, CASE WHEN MENU_ID IN ('TRANSACTION_META', 'MESSAGE', 'CODE', 'DYNAMIC_LOG', 'LOG_POLICY') THEN 'Y' ELSE 'N' END AS DELETE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by
  FROM adm_menu
  WHERE MENU_ID NOT IN ('OPERATOR', 'PERMISSION', 'PASSWORD', 'SECURITY')
) src
ON (tgt.ROLE_ID = src.ROLE_ID AND tgt.MENU_ID = src.MENU_ID)
WHEN MATCHED THEN UPDATE SET
  tgt.READ_YN = src.READ_YN,
  tgt.WRITE_YN = src.WRITE_YN,
  tgt.DELETE_YN = src.DELETE_YN,
  tgt.updated_by = src.updated_by,
  tgt.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (ROLE_ID, MENU_ID, READ_YN, WRITE_YN, DELETE_YN, created_by, updated_by)
VALUES (src.ROLE_ID, src.MENU_ID, src.READ_YN, src.WRITE_YN, src.DELETE_YN, src.created_by, src.updated_by);

MERGE INTO adm_role_menu tgt
USING (
  SELECT 'ADM_BIZ_OPERATOR' AS ROLE_ID, MENU_ID AS MENU_ID, 'Y' AS READ_YN, CASE WHEN MENU_ID IN ('MEMBER', 'BATCH', 'DOWNLOAD', 'CACHE') THEN 'Y' ELSE 'N' END AS WRITE_YN, CASE WHEN MENU_ID = 'MEMBER' THEN 'Y' ELSE 'N' END AS DELETE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by
  FROM adm_menu
  WHERE MENU_ID IN ('DASHBOARD', 'LOG_LIST', 'STANDARD_EXECUTION', 'CHANNEL_POLICY', 'REMOTE_LOG', 'TRANSACTION_META', 'AUDIT_LOG', 'MEMBER', 'BATCH', 'RELIABILITY', 'NOTIFICATION', 'DOWNLOAD', 'CACHE', 'MESSAGE', 'CODE')
) src
ON (tgt.ROLE_ID = src.ROLE_ID AND tgt.MENU_ID = src.MENU_ID)
WHEN MATCHED THEN UPDATE SET
  tgt.READ_YN = src.READ_YN,
  tgt.WRITE_YN = src.WRITE_YN,
  tgt.DELETE_YN = src.DELETE_YN,
  tgt.updated_by = src.updated_by,
  tgt.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (ROLE_ID, MENU_ID, READ_YN, WRITE_YN, DELETE_YN, created_by, updated_by)
VALUES (src.ROLE_ID, src.MENU_ID, src.READ_YN, src.WRITE_YN, src.DELETE_YN, src.created_by, src.updated_by);

MERGE INTO adm_role_menu tgt
USING (
  SELECT 'ADM_VIEWER' AS ROLE_ID, MENU_ID AS MENU_ID, 'Y' AS READ_YN, 'N' AS WRITE_YN, 'N' AS DELETE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by
  FROM adm_menu
  WHERE MENU_ID IN ('DASHBOARD', 'LOG_LIST', 'STANDARD_EXECUTION', 'CHANNEL_POLICY', 'REMOTE_LOG', 'TRANSACTION_META', 'AUDIT_LOG', 'MEMBER', 'BATCH', 'RELIABILITY', 'NOTIFICATION', 'DOWNLOAD', 'CACHE', 'MESSAGE', 'CODE', 'RESPONSE_CODE', 'CONFIG', 'LOG_POLICY')
) src
ON (tgt.ROLE_ID = src.ROLE_ID AND tgt.MENU_ID = src.MENU_ID)
WHEN MATCHED THEN UPDATE SET
  tgt.READ_YN = src.READ_YN,
  tgt.WRITE_YN = src.WRITE_YN,
  tgt.DELETE_YN = src.DELETE_YN,
  tgt.updated_by = src.updated_by,
  tgt.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (ROLE_ID, MENU_ID, READ_YN, WRITE_YN, DELETE_YN, created_by, updated_by)
VALUES (src.ROLE_ID, src.MENU_ID, src.READ_YN, src.WRITE_YN, src.DELETE_YN, src.created_by, src.updated_by);

MERGE INTO adm_role_menu tgt
USING (
  SELECT 'ADM_OPERATOR' AS ROLE_ID, MENU_ID AS MENU_ID, READ_YN AS READ_YN, WRITE_YN AS WRITE_YN, DELETE_YN AS DELETE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by
  FROM adm_role_menu
  WHERE ROLE_ID = 'ADM_DEV_OPERATOR'
) src
ON (tgt.ROLE_ID = src.ROLE_ID AND tgt.MENU_ID = src.MENU_ID)
WHEN MATCHED THEN UPDATE SET
  tgt.READ_YN = src.READ_YN,
  tgt.WRITE_YN = src.WRITE_YN,
  tgt.DELETE_YN = src.DELETE_YN,
  tgt.updated_by = src.updated_by,
  tgt.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (ROLE_ID, MENU_ID, READ_YN, WRITE_YN, DELETE_YN, created_by, updated_by)
VALUES (src.ROLE_ID, src.MENU_ID, src.READ_YN, src.WRITE_YN, src.DELETE_YN, src.created_by, src.updated_by);

MERGE INTO adm_role_button tgt
USING (
  SELECT 'ADM_ADMIN' AS ROLE_ID, BUTTON_ID AS BUTTON_ID, 'Y' AS ALLOW_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by
  FROM adm_button
) src
ON (tgt.ROLE_ID = src.ROLE_ID AND tgt.BUTTON_ID = src.BUTTON_ID)
WHEN MATCHED THEN UPDATE SET
  tgt.ALLOW_YN = src.ALLOW_YN,
  tgt.updated_by = src.updated_by,
  tgt.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (ROLE_ID, BUTTON_ID, ALLOW_YN, created_by, updated_by)
VALUES (src.ROLE_ID, src.BUTTON_ID, src.ALLOW_YN, src.created_by, src.updated_by);

MERGE INTO adm_role_button tgt
USING (
  SELECT 'ADM_DEV_OPERATOR' AS ROLE_ID, BUTTON_ID AS BUTTON_ID, CASE WHEN MENU_ID IN ('OPERATOR', 'PERMISSION', 'PASSWORD', 'SECURITY') THEN 'N' ELSE 'Y' END AS ALLOW_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by
  FROM adm_button
) src
ON (tgt.ROLE_ID = src.ROLE_ID AND tgt.BUTTON_ID = src.BUTTON_ID)
WHEN MATCHED THEN UPDATE SET
  tgt.ALLOW_YN = src.ALLOW_YN,
  tgt.updated_by = src.updated_by,
  tgt.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (ROLE_ID, BUTTON_ID, ALLOW_YN, created_by, updated_by)
VALUES (src.ROLE_ID, src.BUTTON_ID, src.ALLOW_YN, src.created_by, src.updated_by);

MERGE INTO adm_role_button tgt
USING (
  SELECT 'ADM_BIZ_OPERATOR' AS ROLE_ID, BUTTON_ID AS BUTTON_ID, CASE
             WHEN BUTTON_ID IN ('MEMBER_CREATE', 'MEMBER_UPDATE', 'MEMBER_STATUS', 'MEMBER_ROLE_GRANT', 'MEMBER_ROLE_REVOKE', 'BATCH_EXECUTE', 'BATCH_RETRY', 'BATCH_SIMULATION', 'BATCH_RELATION_READ', 'BATCH_TARGET_READ', 'BATCH_SCHEDULER_RUN', 'DOWNLOAD_EXECUTE', 'CACHE_REFRESH') THEN 'Y'
             WHEN ACTION_CODE IN ('READ', 'DETAIL') AND MENU_ID IN ('LOG_LIST', 'REMOTE_LOG', 'TRANSACTION_META', 'AUDIT_LOG', 'MEMBER', 'BATCH', 'RELIABILITY', 'NOTIFICATION', 'DOWNLOAD', 'CACHE', 'MESSAGE', 'CODE', 'LOG_POLICY') THEN 'Y'
             ELSE 'N'
         END AS ALLOW_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by
  FROM adm_button
) src
ON (tgt.ROLE_ID = src.ROLE_ID AND tgt.BUTTON_ID = src.BUTTON_ID)
WHEN MATCHED THEN UPDATE SET
  tgt.ALLOW_YN = src.ALLOW_YN,
  tgt.updated_by = src.updated_by,
  tgt.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (ROLE_ID, BUTTON_ID, ALLOW_YN, created_by, updated_by)
VALUES (src.ROLE_ID, src.BUTTON_ID, src.ALLOW_YN, src.created_by, src.updated_by);

MERGE INTO adm_role_button tgt
USING (
  SELECT 'ADM_VIEWER' AS ROLE_ID, BUTTON_ID AS BUTTON_ID, CASE WHEN ACTION_CODE IN ('READ', 'DETAIL') THEN 'Y' ELSE 'N' END AS ALLOW_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by
  FROM adm_button
) src
ON (tgt.ROLE_ID = src.ROLE_ID AND tgt.BUTTON_ID = src.BUTTON_ID)
WHEN MATCHED THEN UPDATE SET
  tgt.ALLOW_YN = src.ALLOW_YN,
  tgt.updated_by = src.updated_by,
  tgt.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (ROLE_ID, BUTTON_ID, ALLOW_YN, created_by, updated_by)
VALUES (src.ROLE_ID, src.BUTTON_ID, src.ALLOW_YN, src.created_by, src.updated_by);

MERGE INTO adm_role_button tgt
USING (
  SELECT 'ADM_OPERATOR' AS ROLE_ID, BUTTON_ID AS BUTTON_ID, ALLOW_YN AS ALLOW_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by
  FROM adm_role_button
  WHERE ROLE_ID = 'ADM_DEV_OPERATOR'
) src
ON (tgt.ROLE_ID = src.ROLE_ID AND tgt.BUTTON_ID = src.BUTTON_ID)
WHEN MATCHED THEN UPDATE SET
  tgt.ALLOW_YN = src.ALLOW_YN,
  tgt.updated_by = src.updated_by,
  tgt.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (ROLE_ID, BUTTON_ID, ALLOW_YN, created_by, updated_by)
VALUES (src.ROLE_ID, src.BUTTON_ID, src.ALLOW_YN, src.created_by, src.updated_by);

MERGE INTO adm_api_permission tgt
USING (
  SELECT ('API_' || BUTTON_ID) AS API_PERMISSION_ID, MENU_ID AS API_GROUP_CODE, COALESCE(HTTP_METHOD, 'ANY') AS HTTP_METHOD, API_PATTERN AS API_PATH, BUTTON_NAME AS API_NAME, ACTION_CODE AS PERMISSION_CODE, MENU_ID AS MENU_ID, BUTTON_ID AS BUTTON_ID, USE_YN AS USE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by
  FROM adm_button
  WHERE API_PATTERN IS NOT NULL
) src
ON (tgt.API_PERMISSION_ID = src.API_PERMISSION_ID)
WHEN MATCHED THEN UPDATE SET
  tgt.API_GROUP_CODE = src.API_GROUP_CODE,
  tgt.HTTP_METHOD = src.HTTP_METHOD,
  tgt.API_PATH = src.API_PATH,
  tgt.API_NAME = src.API_NAME,
  tgt.PERMISSION_CODE = src.PERMISSION_CODE,
  tgt.MENU_ID = src.MENU_ID,
  tgt.BUTTON_ID = src.BUTTON_ID,
  tgt.USE_YN = src.USE_YN,
  tgt.updated_by = src.updated_by,
  tgt.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (API_PERMISSION_ID, API_GROUP_CODE, HTTP_METHOD, API_PATH, API_NAME, PERMISSION_CODE, MENU_ID, BUTTON_ID, USE_YN, created_by, updated_by)
VALUES (src.API_PERMISSION_ID, src.API_GROUP_CODE, src.HTTP_METHOD, src.API_PATH, src.API_NAME, src.PERMISSION_CODE, src.MENU_ID, src.BUTTON_ID, src.USE_YN, src.created_by, src.updated_by);

MERGE INTO adm_api_permission tgt
USING (VALUES
  ('API_PERMISSION_WRITE_PUT', 'PERMISSION', 'PUT', '/adm/api/permissions/**', '권한 변경', 'WRITE', 'PERMISSION', 'PERMISSION_WRITE', 'Y', 'SYSTEM', 'SYSTEM')
) AS src(API_PERMISSION_ID, API_GROUP_CODE, HTTP_METHOD, API_PATH, API_NAME, PERMISSION_CODE, MENU_ID, BUTTON_ID, USE_YN, created_by, updated_by)
ON (tgt.API_PERMISSION_ID = src.API_PERMISSION_ID)
WHEN MATCHED THEN UPDATE SET
  tgt.API_GROUP_CODE = src.API_GROUP_CODE,
  tgt.HTTP_METHOD = src.HTTP_METHOD,
  tgt.API_PATH = src.API_PATH,
  tgt.API_NAME = src.API_NAME,
  tgt.PERMISSION_CODE = src.PERMISSION_CODE,
  tgt.MENU_ID = src.MENU_ID,
  tgt.BUTTON_ID = src.BUTTON_ID,
  tgt.USE_YN = src.USE_YN,
  tgt.updated_by = src.updated_by,
  tgt.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (API_PERMISSION_ID, API_GROUP_CODE, HTTP_METHOD, API_PATH, API_NAME, PERMISSION_CODE, MENU_ID, BUTTON_ID, USE_YN, created_by, updated_by)
VALUES (src.API_PERMISSION_ID, src.API_GROUP_CODE, src.HTTP_METHOD, src.API_PATH, src.API_NAME, src.PERMISSION_CODE, src.MENU_ID, src.BUTTON_ID, src.USE_YN, src.created_by, src.updated_by);

MERGE INTO adm_role_api_permission tgt
USING (
  SELECT rb.ROLE_ID AS ROLE_ID, ap.API_PERMISSION_ID AS API_PERMISSION_ID, rb.ALLOW_YN AS ALLOW_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by
  FROM adm_role_button rb
  JOIN adm_api_permission ap ON ap.BUTTON_ID = rb.BUTTON_ID
) src
ON (tgt.ROLE_ID = src.ROLE_ID AND tgt.API_PERMISSION_ID = src.API_PERMISSION_ID)
WHEN MATCHED THEN UPDATE SET
  tgt.ALLOW_YN = src.ALLOW_YN,
  tgt.updated_by = src.updated_by,
  tgt.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (ROLE_ID, API_PERMISSION_ID, ALLOW_YN, created_by, updated_by)
VALUES (src.ROLE_ID, src.API_PERMISSION_ID, src.ALLOW_YN, src.created_by, src.updated_by);

MERGE INTO adm_button tgt
USING (VALUES
  ('AUDIT_LOG_RETRY', 'AUDIT_LOG', 'WRITE', '감사 전달 재처리', 'POST', '/adm/api/audit-logs/deliveries/*/retry', 20, 'Y', 'SYSTEM', 'SYSTEM')
) AS src(BUTTON_ID, MENU_ID, ACTION_CODE, BUTTON_NAME, HTTP_METHOD, API_PATTERN, SORT_ORDER, USE_YN, created_by, updated_by)
ON (tgt.BUTTON_ID = src.BUTTON_ID)
WHEN MATCHED THEN UPDATE SET
  tgt.ACTION_CODE = src.ACTION_CODE,
  tgt.BUTTON_NAME = src.BUTTON_NAME,
  tgt.HTTP_METHOD = src.HTTP_METHOD,
  tgt.API_PATTERN = src.API_PATTERN,
  tgt.USE_YN = src.USE_YN,
  tgt.updated_by = src.updated_by,
  tgt.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (BUTTON_ID, MENU_ID, ACTION_CODE, BUTTON_NAME, HTTP_METHOD, API_PATTERN, SORT_ORDER, USE_YN, created_by, updated_by)
VALUES (src.BUTTON_ID, src.MENU_ID, src.ACTION_CODE, src.BUTTON_NAME, src.HTTP_METHOD, src.API_PATTERN, src.SORT_ORDER, src.USE_YN, src.created_by, src.updated_by);

UPDATE adm_role_menu SET WRITE_YN='Y',updated_by='SYSTEM',updated_at=CURRENT_TIMESTAMP WHERE MENU_ID='AUDIT_LOG' AND ROLE_ID IN ('ADM_ADMIN','ADM_DEV_OPERATOR');

MERGE INTO adm_role_button tgt
USING (
  SELECT ROLE_ID AS ROLE_ID, 'AUDIT_LOG_RETRY' AS BUTTON_ID, 'Y' AS ALLOW_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by
  FROM adm_role WHERE ROLE_ID IN ('ADM_ADMIN','ADM_DEV_OPERATOR')
) src
ON (tgt.ROLE_ID = src.ROLE_ID AND tgt.BUTTON_ID = src.BUTTON_ID)
WHEN MATCHED THEN UPDATE SET
  tgt.ALLOW_YN = 'Y',
  tgt.updated_by = 'SYSTEM',
  tgt.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (ROLE_ID, BUTTON_ID, ALLOW_YN, created_by, updated_by)
VALUES (src.ROLE_ID, src.BUTTON_ID, src.ALLOW_YN, src.created_by, src.updated_by);

MERGE INTO adm_api_permission tgt
USING (VALUES
  ('API_AUDIT_LOG_RETRY', 'AUDIT_LOG', 'POST', '/adm/api/audit-logs/deliveries/*/retry', '감사 전달 재처리', 'WRITE', 'AUDIT_LOG', 'AUDIT_LOG_RETRY', 'Y', 'SYSTEM', 'SYSTEM')
) AS src(API_PERMISSION_ID, API_GROUP_CODE, HTTP_METHOD, API_PATH, API_NAME, PERMISSION_CODE, MENU_ID, BUTTON_ID, USE_YN, created_by, updated_by)
ON (tgt.API_PERMISSION_ID = src.API_PERMISSION_ID)
WHEN MATCHED THEN UPDATE SET
  tgt.HTTP_METHOD = src.HTTP_METHOD,
  tgt.API_PATH = src.API_PATH,
  tgt.PERMISSION_CODE = src.PERMISSION_CODE,
  tgt.BUTTON_ID = src.BUTTON_ID,
  tgt.USE_YN = src.USE_YN,
  tgt.updated_by = src.updated_by,
  tgt.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (API_PERMISSION_ID, API_GROUP_CODE, HTTP_METHOD, API_PATH, API_NAME, PERMISSION_CODE, MENU_ID, BUTTON_ID, USE_YN, created_by, updated_by)
VALUES (src.API_PERMISSION_ID, src.API_GROUP_CODE, src.HTTP_METHOD, src.API_PATH, src.API_NAME, src.PERMISSION_CODE, src.MENU_ID, src.BUTTON_ID, src.USE_YN, src.created_by, src.updated_by);

MERGE INTO adm_role_api_permission tgt
USING (
  SELECT ROLE_ID AS ROLE_ID, 'API_AUDIT_LOG_RETRY' AS API_PERMISSION_ID, 'Y' AS ALLOW_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by
  FROM adm_role WHERE ROLE_ID IN ('ADM_ADMIN','ADM_DEV_OPERATOR')
) src
ON (tgt.ROLE_ID = src.ROLE_ID AND tgt.API_PERMISSION_ID = src.API_PERMISSION_ID)
WHEN MATCHED THEN UPDATE SET
  tgt.ALLOW_YN = 'Y',
  tgt.updated_by = 'SYSTEM',
  tgt.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (ROLE_ID, API_PERMISSION_ID, ALLOW_YN, created_by, updated_by)
VALUES (src.ROLE_ID, src.API_PERMISSION_ID, src.ALLOW_YN, src.created_by, src.updated_by);

MERGE INTO adm_menu tgt
USING (VALUES
  ('SECRET', NULL, 'Secret / Key 관리', '/adm#secrets', 145, 'Y', 'SYSTEM', 'SYSTEM')
) AS src(MENU_ID, PARENT_MENU_ID, MENU_NAME, MENU_PATH, SORT_ORDER, USE_YN, created_by, updated_by)
ON (tgt.MENU_ID = src.MENU_ID)
WHEN MATCHED THEN UPDATE SET
  tgt.MENU_NAME = src.MENU_NAME,
  tgt.MENU_PATH = src.MENU_PATH,
  tgt.SORT_ORDER = src.SORT_ORDER,
  tgt.USE_YN = 'Y',
  tgt.updated_by = 'SYSTEM',
  tgt.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (MENU_ID, PARENT_MENU_ID, MENU_NAME, MENU_PATH, SORT_ORDER, USE_YN, created_by, updated_by)
VALUES (src.MENU_ID, src.PARENT_MENU_ID, src.MENU_NAME, src.MENU_PATH, src.SORT_ORDER, src.USE_YN, src.created_by, src.updated_by);

MERGE INTO adm_button tgt
USING (VALUES
  ('SECRET_READ', 'SECRET', 'READ', 'Secret Metadata 조회', 'GET', '/adm/api/secrets/**', 10, 'Y', 'SYSTEM', 'SYSTEM'),
  ('SECRET_ROTATE', 'SECRET', 'ROTATE', 'Secret Rotation', 'POST', '/adm/api/secrets/rotate', 20, 'Y', 'SYSTEM', 'SYSTEM')
) AS src(BUTTON_ID, MENU_ID, ACTION_CODE, BUTTON_NAME, HTTP_METHOD, API_PATTERN, SORT_ORDER, USE_YN, created_by, updated_by)
ON (tgt.BUTTON_ID = src.BUTTON_ID)
WHEN MATCHED THEN UPDATE SET
  tgt.ACTION_CODE = src.ACTION_CODE,
  tgt.BUTTON_NAME = src.BUTTON_NAME,
  tgt.HTTP_METHOD = src.HTTP_METHOD,
  tgt.API_PATTERN = src.API_PATTERN,
  tgt.USE_YN = 'Y',
  tgt.updated_by = 'SYSTEM',
  tgt.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (BUTTON_ID, MENU_ID, ACTION_CODE, BUTTON_NAME, HTTP_METHOD, API_PATTERN, SORT_ORDER, USE_YN, created_by, updated_by)
VALUES (src.BUTTON_ID, src.MENU_ID, src.ACTION_CODE, src.BUTTON_NAME, src.HTTP_METHOD, src.API_PATTERN, src.SORT_ORDER, src.USE_YN, src.created_by, src.updated_by);

MERGE INTO adm_role_menu tgt
USING (VALUES
  ('ADM_ADMIN', 'SECRET', 'Y', 'Y', 'N', 'SYSTEM', 'SYSTEM'),
  ('ADM_DEV_OPERATOR', 'SECRET', 'Y', 'N', 'N', 'SYSTEM', 'SYSTEM'),
  ('ADM_OPERATOR', 'SECRET', 'Y', 'N', 'N', 'SYSTEM', 'SYSTEM'),
  ('ADM_VIEWER', 'SECRET', 'N', 'N', 'N', 'SYSTEM', 'SYSTEM'),
  ('ADM_BIZ_OPERATOR', 'SECRET', 'N', 'N', 'N', 'SYSTEM', 'SYSTEM')
) AS src(ROLE_ID, MENU_ID, READ_YN, WRITE_YN, DELETE_YN, created_by, updated_by)
ON (tgt.ROLE_ID = src.ROLE_ID AND tgt.MENU_ID = src.MENU_ID)
WHEN MATCHED THEN UPDATE SET
  tgt.READ_YN = src.READ_YN,
  tgt.WRITE_YN = src.WRITE_YN,
  tgt.DELETE_YN = src.DELETE_YN,
  tgt.updated_by = 'SYSTEM',
  tgt.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (ROLE_ID, MENU_ID, READ_YN, WRITE_YN, DELETE_YN, created_by, updated_by)
VALUES (src.ROLE_ID, src.MENU_ID, src.READ_YN, src.WRITE_YN, src.DELETE_YN, src.created_by, src.updated_by);

MERGE INTO adm_role_button tgt
USING (VALUES
  ('ADM_ADMIN', 'SECRET_READ', 'Y', 'SYSTEM', 'SYSTEM'),
  ('ADM_ADMIN', 'SECRET_ROTATE', 'Y', 'SYSTEM', 'SYSTEM'),
  ('ADM_DEV_OPERATOR', 'SECRET_READ', 'Y', 'SYSTEM', 'SYSTEM'),
  ('ADM_DEV_OPERATOR', 'SECRET_ROTATE', 'N', 'SYSTEM', 'SYSTEM'),
  ('ADM_OPERATOR', 'SECRET_READ', 'Y', 'SYSTEM', 'SYSTEM'),
  ('ADM_OPERATOR', 'SECRET_ROTATE', 'N', 'SYSTEM', 'SYSTEM')
) AS src(ROLE_ID, BUTTON_ID, ALLOW_YN, created_by, updated_by)
ON (tgt.ROLE_ID = src.ROLE_ID AND tgt.BUTTON_ID = src.BUTTON_ID)
WHEN MATCHED THEN UPDATE SET
  tgt.ALLOW_YN = src.ALLOW_YN,
  tgt.updated_by = 'SYSTEM',
  tgt.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (ROLE_ID, BUTTON_ID, ALLOW_YN, created_by, updated_by)
VALUES (src.ROLE_ID, src.BUTTON_ID, src.ALLOW_YN, src.created_by, src.updated_by);

MERGE INTO adm_api_permission tgt
USING (VALUES
  ('API_SECRET_READ', 'SECRET', 'GET', '/adm/api/secrets/**', 'Secret Metadata 조회', 'READ', 'SECRET', 'SECRET_READ', 'Y', 'SYSTEM', 'SYSTEM'),
  ('API_SECRET_ROTATE', 'SECRET', 'POST', '/adm/api/secrets/rotate', 'Secret Rotation', 'ROTATE', 'SECRET', 'SECRET_ROTATE', 'Y', 'SYSTEM', 'SYSTEM')
) AS src(API_PERMISSION_ID, API_GROUP_CODE, HTTP_METHOD, API_PATH, API_NAME, PERMISSION_CODE, MENU_ID, BUTTON_ID, USE_YN, created_by, updated_by)
ON (tgt.API_PERMISSION_ID = src.API_PERMISSION_ID)
WHEN MATCHED THEN UPDATE SET
  tgt.API_PATH = src.API_PATH,
  tgt.API_NAME = src.API_NAME,
  tgt.PERMISSION_CODE = src.PERMISSION_CODE,
  tgt.BUTTON_ID = src.BUTTON_ID,
  tgt.USE_YN = 'Y',
  tgt.updated_by = 'SYSTEM',
  tgt.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (API_PERMISSION_ID, API_GROUP_CODE, HTTP_METHOD, API_PATH, API_NAME, PERMISSION_CODE, MENU_ID, BUTTON_ID, USE_YN, created_by, updated_by)
VALUES (src.API_PERMISSION_ID, src.API_GROUP_CODE, src.HTTP_METHOD, src.API_PATH, src.API_NAME, src.PERMISSION_CODE, src.MENU_ID, src.BUTTON_ID, src.USE_YN, src.created_by, src.updated_by);

MERGE INTO adm_role_api_permission tgt
USING (VALUES
  ('ADM_ADMIN', 'API_SECRET_READ', 'Y', 'SYSTEM', 'SYSTEM'),
  ('ADM_ADMIN', 'API_SECRET_ROTATE', 'Y', 'SYSTEM', 'SYSTEM'),
  ('ADM_DEV_OPERATOR', 'API_SECRET_READ', 'Y', 'SYSTEM', 'SYSTEM'),
  ('ADM_DEV_OPERATOR', 'API_SECRET_ROTATE', 'N', 'SYSTEM', 'SYSTEM'),
  ('ADM_OPERATOR', 'API_SECRET_READ', 'Y', 'SYSTEM', 'SYSTEM'),
  ('ADM_OPERATOR', 'API_SECRET_ROTATE', 'N', 'SYSTEM', 'SYSTEM')
) AS src(ROLE_ID, API_PERMISSION_ID, ALLOW_YN, created_by, updated_by)
ON (tgt.ROLE_ID = src.ROLE_ID AND tgt.API_PERMISSION_ID = src.API_PERMISSION_ID)
WHEN MATCHED THEN UPDATE SET
  tgt.ALLOW_YN = src.ALLOW_YN,
  tgt.updated_by = 'SYSTEM',
  tgt.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (ROLE_ID, API_PERMISSION_ID, ALLOW_YN, created_by, updated_by)
VALUES (src.ROLE_ID, src.API_PERMISSION_ID, src.ALLOW_YN, src.created_by, src.updated_by);

MERGE INTO adm_menu tgt
USING (VALUES
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
  ('BATCH_AUDIT', 'BATCH', 'Audit / Evidence', '/adm#batch-audit', 514, 'Y', 'SYSTEM', 'SYSTEM')
) AS src(MENU_ID, PARENT_MENU_ID, MENU_NAME, MENU_PATH, SORT_ORDER, USE_YN, created_by, updated_by)
ON (tgt.MENU_ID = src.MENU_ID)
WHEN MATCHED THEN UPDATE SET
  tgt.PARENT_MENU_ID = src.PARENT_MENU_ID,
  tgt.MENU_NAME = src.MENU_NAME,
  tgt.MENU_PATH = src.MENU_PATH,
  tgt.SORT_ORDER = src.SORT_ORDER,
  tgt.USE_YN = 'Y',
  tgt.updated_by = 'SYSTEM',
  tgt.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (MENU_ID, PARENT_MENU_ID, MENU_NAME, MENU_PATH, SORT_ORDER, USE_YN, created_by, updated_by)
VALUES (src.MENU_ID, src.PARENT_MENU_ID, src.MENU_NAME, src.MENU_PATH, src.SORT_ORDER, src.USE_YN, src.created_by, src.updated_by);

MERGE INTO adm_button tgt
USING (VALUES
  ('BAT_RUNTIME_VIEW', 'BATCH_RUNTIME', 'RUNTIME_VIEW', 'Runtime 조회', 'GET', '/adm/api/batch-runtime/**', 10, 'Y', 'SYSTEM', 'SYSTEM'),
  ('BAT_RUNTIME_OPERATE', 'BATCH_INSTANCES', 'RUNTIME_OPERATE', 'Runtime Start/Stop/Drain', 'POST', '/adm/api/approvals/**', 20, 'Y', 'SYSTEM', 'SYSTEM'),
  ('BAT_JOB_OPERATE', 'BATCH_EXECUTIONS', 'JOB_OPERATE', 'Job 실행/중지/재처리', 'POST', '/adm/api/batch/**', 30, 'Y', 'SYSTEM', 'SYSTEM'),
  ('BAT_SCHEDULE_OPERATE', 'BATCH_SCHEDULER', 'SCHEDULE_OPERATE', 'Scheduler 운영', 'POST', '/adm/api/batch/**', 40, 'Y', 'SYSTEM', 'SYSTEM'),
  ('BAT_WORKER_OPERATE', 'BATCH_WORKER_POOLS', 'WORKER_OPERATE', 'Worker Pool 운영', 'POST', '/adm/api/approvals/**', 50, 'Y', 'SYSTEM', 'SYSTEM'),
  ('BAT_CENTER_CUT_OPERATE', 'BATCH_CENTER_CUT', 'CENTER_CUT_OPERATE', 'Center-Cut 재처리/조정', 'POST', '/adm/api/batch-runtime/**', 60, 'Y', 'SYSTEM', 'SYSTEM'),
  ('BAT_AGENT_OPERATE', 'BATCH_AGENTS', 'AGENT_OPERATE', 'Host Agent 운영', 'POST', '/adm/api/approvals/**', 70, 'Y', 'SYSTEM', 'SYSTEM'),
  ('BAT_DEPLOY_PLAN', 'BATCH_DEPLOYMENT', 'DEPLOY_PLAN', 'Deployment Plan 생성', 'POST', '/adm/api/batch-runtime/deployment-plans', 80, 'Y', 'SYSTEM', 'SYSTEM'),
  ('BAT_DEPLOY_APPROVE', 'BATCH_DEPLOYMENT', 'DEPLOY_APPROVE', 'Deployment 승인', 'POST', '/adm/api/approvals/**', 90, 'Y', 'SYSTEM', 'SYSTEM'),
  ('BAT_DEPLOY_EXECUTE', 'BATCH_DEPLOYMENT', 'DEPLOY_EXECUTE', 'Deployment 실행', 'POST', '/adm/api/approvals/**', 100, 'Y', 'SYSTEM', 'SYSTEM'),
  ('BAT_ROLLBACK_EXECUTE', 'BATCH_DEPLOYMENT', 'ROLLBACK_EXECUTE', 'Rollback 실행', 'POST', '/adm/api/approvals/**', 110, 'Y', 'SYSTEM', 'SYSTEM'),
  ('BAT_RECOVERY_OPERATE', 'BATCH_RECOVERY', 'RECOVERY_OPERATE', 'UNKNOWN_RESULT 조정', 'POST', '/adm/api/batch-runtime/**', 120, 'Y', 'SYSTEM', 'SYSTEM'),
  ('BAT_SECURITY_AUDIT', 'BATCH_AUDIT', 'SECURITY_AUDIT', 'BAT 보안·감사 조회', 'GET', '/adm/api/batch-runtime/views/audit', 130, 'Y', 'SYSTEM', 'SYSTEM'),
  ('BAT_EVIDENCE_DOWNLOAD', 'BATCH_AUDIT', 'EVIDENCE_DOWNLOAD', 'BAT Evidence 다운로드', 'GET', '/adm/api/downloads/**', 140, 'Y', 'SYSTEM', 'SYSTEM')
) AS src(BUTTON_ID, MENU_ID, ACTION_CODE, BUTTON_NAME, HTTP_METHOD, API_PATTERN, SORT_ORDER, USE_YN, created_by, updated_by)
ON (tgt.BUTTON_ID = src.BUTTON_ID)
WHEN MATCHED THEN UPDATE SET
  tgt.MENU_ID = src.MENU_ID,
  tgt.ACTION_CODE = src.ACTION_CODE,
  tgt.BUTTON_NAME = src.BUTTON_NAME,
  tgt.HTTP_METHOD = src.HTTP_METHOD,
  tgt.API_PATTERN = src.API_PATTERN,
  tgt.SORT_ORDER = src.SORT_ORDER,
  tgt.USE_YN = 'Y',
  tgt.updated_by = 'SYSTEM',
  tgt.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (BUTTON_ID, MENU_ID, ACTION_CODE, BUTTON_NAME, HTTP_METHOD, API_PATTERN, SORT_ORDER, USE_YN, created_by, updated_by)
VALUES (src.BUTTON_ID, src.MENU_ID, src.ACTION_CODE, src.BUTTON_NAME, src.HTTP_METHOD, src.API_PATTERN, src.SORT_ORDER, src.USE_YN, src.created_by, src.updated_by);

MERGE INTO adm_role_menu tgt
USING (
  SELECT r.ROLE_ID AS ROLE_ID, m.MENU_ID AS MENU_ID, 'Y' AS READ_YN, CASE WHEN r.ROLE_ID IN ('ADM_ADMIN','ADM_DEV_OPERATOR','ADM_OPERATOR') THEN 'Y' ELSE 'N' END AS WRITE_YN, 'N' AS DELETE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by
  FROM adm_role r JOIN adm_menu m ON m.PARENT_MENU_ID='BATCH'
  WHERE r.ROLE_ID IN ('ADM_ADMIN','ADM_DEV_OPERATOR','ADM_OPERATOR','ADM_BIZ_OPERATOR','ADM_VIEWER')
) src
ON (tgt.ROLE_ID = src.ROLE_ID AND tgt.MENU_ID = src.MENU_ID)
WHEN MATCHED THEN UPDATE SET
  tgt.READ_YN = src.READ_YN,
  tgt.WRITE_YN = src.WRITE_YN,
  tgt.DELETE_YN = src.DELETE_YN,
  tgt.updated_by = 'SYSTEM',
  tgt.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (ROLE_ID, MENU_ID, READ_YN, WRITE_YN, DELETE_YN, created_by, updated_by)
VALUES (src.ROLE_ID, src.MENU_ID, src.READ_YN, src.WRITE_YN, src.DELETE_YN, src.created_by, src.updated_by);

MERGE INTO adm_role_button tgt
USING (
  SELECT r.ROLE_ID AS ROLE_ID, b.BUTTON_ID AS BUTTON_ID, CASE
           WHEN r.ROLE_ID='ADM_ADMIN' THEN 'Y'
           WHEN r.ROLE_ID IN ('ADM_DEV_OPERATOR','ADM_OPERATOR') AND b.BUTTON_ID NOT IN ('BAT_DEPLOY_APPROVE','BAT_DEPLOY_EXECUTE','BAT_ROLLBACK_EXECUTE') THEN 'Y'
           WHEN r.ROLE_ID='ADM_BIZ_OPERATOR' AND b.BUTTON_ID IN ('BAT_RUNTIME_VIEW','BAT_JOB_OPERATE','BAT_WORKER_OPERATE','BAT_CENTER_CUT_OPERATE','BAT_SECURITY_AUDIT','BAT_EVIDENCE_DOWNLOAD') THEN 'Y'
           WHEN r.ROLE_ID='ADM_VIEWER' AND b.BUTTON_ID IN ('BAT_RUNTIME_VIEW','BAT_SECURITY_AUDIT') THEN 'Y'
           ELSE 'N' END AS ALLOW_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by
  FROM adm_role r JOIN adm_button b ON b.BUTTON_ID LIKE 'BAT_%'
  WHERE r.ROLE_ID IN ('ADM_ADMIN','ADM_DEV_OPERATOR','ADM_OPERATOR','ADM_BIZ_OPERATOR','ADM_VIEWER')
) src
ON (tgt.ROLE_ID = src.ROLE_ID AND tgt.BUTTON_ID = src.BUTTON_ID)
WHEN MATCHED THEN UPDATE SET
  tgt.ALLOW_YN = src.ALLOW_YN,
  tgt.updated_by = 'SYSTEM',
  tgt.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (ROLE_ID, BUTTON_ID, ALLOW_YN, created_by, updated_by)
VALUES (src.ROLE_ID, src.BUTTON_ID, src.ALLOW_YN, src.created_by, src.updated_by);

MERGE INTO adm_api_permission tgt
USING (
  SELECT ('API_' || BUTTON_ID) AS API_PERMISSION_ID, MENU_ID AS API_GROUP_CODE, COALESCE(HTTP_METHOD,'ANY') AS HTTP_METHOD, API_PATTERN AS API_PATH, BUTTON_NAME AS API_NAME, ACTION_CODE AS PERMISSION_CODE, MENU_ID AS MENU_ID, BUTTON_ID AS BUTTON_ID, 'Y' AS USE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by
  FROM adm_button WHERE BUTTON_ID LIKE 'BAT_%' AND API_PATTERN IS NOT NULL
) src
ON (tgt.API_PERMISSION_ID = src.API_PERMISSION_ID)
WHEN MATCHED THEN UPDATE SET
  tgt.API_GROUP_CODE = src.API_GROUP_CODE,
  tgt.HTTP_METHOD = src.HTTP_METHOD,
  tgt.API_PATH = src.API_PATH,
  tgt.API_NAME = src.API_NAME,
  tgt.PERMISSION_CODE = src.PERMISSION_CODE,
  tgt.MENU_ID = src.MENU_ID,
  tgt.BUTTON_ID = src.BUTTON_ID,
  tgt.USE_YN = 'Y',
  tgt.updated_by = 'SYSTEM',
  tgt.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (API_PERMISSION_ID, API_GROUP_CODE, HTTP_METHOD, API_PATH, API_NAME, PERMISSION_CODE, MENU_ID, BUTTON_ID, USE_YN, created_by, updated_by)
VALUES (src.API_PERMISSION_ID, src.API_GROUP_CODE, src.HTTP_METHOD, src.API_PATH, src.API_NAME, src.PERMISSION_CODE, src.MENU_ID, src.BUTTON_ID, src.USE_YN, src.created_by, src.updated_by);

MERGE INTO adm_role_api_permission tgt
USING (
  SELECT rb.ROLE_ID AS ROLE_ID, ap.API_PERMISSION_ID AS API_PERMISSION_ID, rb.ALLOW_YN AS ALLOW_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by
  FROM adm_role_button rb JOIN adm_api_permission ap ON ap.BUTTON_ID=rb.BUTTON_ID
  WHERE rb.BUTTON_ID LIKE 'BAT_%'
) src
ON (tgt.ROLE_ID = src.ROLE_ID AND tgt.API_PERMISSION_ID = src.API_PERMISSION_ID)
WHEN MATCHED THEN UPDATE SET
  tgt.ALLOW_YN = src.ALLOW_YN,
  tgt.updated_by = 'SYSTEM',
  tgt.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (ROLE_ID, API_PERMISSION_ID, ALLOW_YN, created_by, updated_by)
VALUES (src.ROLE_ID, src.API_PERMISSION_ID, src.ALLOW_YN, src.created_by, src.updated_by);

-- ===== END 60_adm_seed_data.sql =====
