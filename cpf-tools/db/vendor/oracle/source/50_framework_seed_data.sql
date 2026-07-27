-- AUTO-GENERATED from cpf-tools/db/canonical/seed-model.json
-- vendor=oracle; source=50_framework_seed_data.sql
-- USE lines are CPF packaging directives and are stripped by the vendor executor.



-- CPF_LOGICAL_DATABASE=cpfDB
-- CPF_USE_LOGICAL_DATABASE=cpfDB
MERGE INTO cpf_channel_registry tgt
USING (
  SELECT 'ANY' AS channel_code, '전체 채널' AS channel_name, 'SYSTEM' AS channel_type, 'INTERNAL' AS trust_level, 'N' AS client_channel_yn, 'Y' AS internal_channel_yn, 'N' AS authentication_required_yn, 'N' AS signature_required_yn, 'Y' AS active_yn, '정책 와일드카드 전용 채널' AS description, 0 AS policy_version, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT 'WEB' AS channel_code, '웹' AS channel_name, 'CLIENT' AS channel_type, 'EXTERNAL' AS trust_level, 'Y' AS client_channel_yn, 'N' AS internal_channel_yn, 'Y' AS authentication_required_yn, 'N' AS signature_required_yn, 'Y' AS active_yn, '웹 브라우저 채널' AS description, 0 AS policy_version, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT 'MOBILE' AS channel_code, '모바일' AS channel_name, 'CLIENT' AS channel_type, 'EXTERNAL' AS trust_level, 'Y' AS client_channel_yn, 'N' AS internal_channel_yn, 'Y' AS authentication_required_yn, 'N' AS signature_required_yn, 'Y' AS active_yn, '모바일 애플리케이션 채널' AS description, 0 AS policy_version, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT 'ADM' AS channel_code, '관리자' AS channel_name, 'OPERATOR' AS channel_type, 'INTERNAL' AS trust_level, 'Y' AS client_channel_yn, 'Y' AS internal_channel_yn, 'Y' AS authentication_required_yn, 'N' AS signature_required_yn, 'Y' AS active_yn, 'ADM 운영 채널' AS description, 0 AS policy_version, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT 'BATCH' AS channel_code, '배치' AS channel_name, 'SYSTEM' AS channel_type, 'INTERNAL' AS trust_level, 'N' AS client_channel_yn, 'Y' AS internal_channel_yn, 'N' AS authentication_required_yn, 'N' AS signature_required_yn, 'Y' AS active_yn, '배치 실행 채널' AS description, 0 AS policy_version, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
) src
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
USING (
  SELECT 'CPF.DEFAULT' AS policy_key, '*' AS standard_execution_id, 'ANY' AS original_channel_code, 'ANY' AS caller_channel_code, '*' AS request_type, 'Y' AS allowed_yn, 'N' AS authentication_required_yn, 'N' AS signature_required_yn, 0 AS max_tps, NULL AS effective_from, NULL AS effective_to, 'Y' AS active_yn, 0 AS policy_version, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
) src
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
USING (
  SELECT NULL AS parent_id, 'CODE_GROUP' AS code_key, 'MODULE' AS code_value, '서비스 모듈 코드 그룹' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT NULL AS parent_id, 'CODE_GROUP' AS code_key, 'REQUEST_TYPE' AS code_value, '요청 유형 코드 그룹' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT NULL AS parent_id, 'CODE_GROUP' AS code_key, 'CHANNEL_CODE' AS code_value, '채널 코드 그룹' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT NULL AS parent_id, 'CODE_GROUP' AS code_key, 'RESULT_TYPE' AS code_value, '응답 결과 유형 코드 그룹' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT NULL AS parent_id, 'CODE_GROUP' AS code_key, 'MESSAGE_FORMAT_TYPE' AS code_value, '메시지 포맷 유형 코드 그룹' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT NULL AS parent_id, 'CODE_GROUP' AS code_key, 'LOG_LEVEL' AS code_value, '동적 로그 레벨 코드 그룹' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT NULL AS parent_id, 'CODE_GROUP' AS code_key, 'CACHE_NAME' AS code_value, '캐시 이름 코드 그룹' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT NULL AS parent_id, 'CODE_GROUP' AS code_key, 'BATCH_JOB_TYPE' AS code_value, '배치 Job 유형 코드 그룹' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT NULL AS parent_id, 'CODE_GROUP' AS code_key, 'YN' AS code_value, '여부 코드 그룹' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
) src
ON (tgt.code_key = src.code_key AND tgt.code_value = src.code_value)
WHEN MATCHED THEN UPDATE SET
  tgt.description = src.description,
  tgt.updated_by = src.updated_by,
  tgt.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (parent_id, code_key, code_value, description, created_by, updated_by)
VALUES (src.parent_id, src.code_key, src.code_value, src.description, src.created_by, src.updated_by);

MERGE INTO cpf_code tgt
USING (
  SELECT (SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key = 'CODE_GROUP' AND code_value = 'MODULE') p) AS parent_id, 'MODULE' AS code_key, 'CPF' AS code_value, '프레임워크 공통 엔진' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT (SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key = 'CODE_GROUP' AND code_value = 'MODULE') p) AS parent_id, 'MODULE' AS code_key, 'CMN' AS code_value, '업무 공통 라이브러리' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT (SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key = 'CODE_GROUP' AND code_value = 'MODULE') p) AS parent_id, 'MODULE' AS code_key, 'ADM' AS code_value, '관리자 운영 서비스' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT (SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key = 'CODE_GROUP' AND code_value = 'MODULE') p) AS parent_id, 'MODULE' AS code_key, 'BZA' AS code_value, '업무 백오피스 서비스' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT (SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key = 'CODE_GROUP' AND code_value = 'MODULE') p) AS parent_id, 'MODULE' AS code_key, 'BAT' AS code_value, '선택 배치 실행 서비스' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT (SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key = 'CODE_GROUP' AND code_value = 'MODULE') p) AS parent_id, 'MODULE' AS code_key, 'MBR' AS code_value, '회원 샘플 서비스' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT (SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key = 'CODE_GROUP' AND code_value = 'MODULE') p) AS parent_id, 'MODULE' AS code_key, 'REF' AS code_value, '교육 샘플 서비스' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT (SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key = 'CODE_GROUP' AND code_value = 'REQUEST_TYPE') p) AS parent_id, 'REQUEST_TYPE' AS code_key, 'NORMAL' AS code_value, '일반 요청' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT (SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key = 'CODE_GROUP' AND code_value = 'REQUEST_TYPE') p) AS parent_id, 'REQUEST_TYPE' AS code_key, 'COMPENSATION' AS code_value, '보상 요청' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT (SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key = 'CODE_GROUP' AND code_value = 'REQUEST_TYPE') p) AS parent_id, 'REQUEST_TYPE' AS code_key, 'RETRY' AS code_value, '재시도 요청' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT (SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key = 'CODE_GROUP' AND code_value = 'CHANNEL_CODE') p) AS parent_id, 'CHANNEL_CODE' AS code_key, 'WEB' AS code_value, '웹 채널' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT (SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key = 'CODE_GROUP' AND code_value = 'CHANNEL_CODE') p) AS parent_id, 'CHANNEL_CODE' AS code_key, 'MOBILE' AS code_value, '모바일 채널' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT (SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key = 'CODE_GROUP' AND code_value = 'CHANNEL_CODE') p) AS parent_id, 'CHANNEL_CODE' AS code_key, 'BATCH' AS code_value, '배치 채널' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT (SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key = 'CODE_GROUP' AND code_value = 'CHANNEL_CODE') p) AS parent_id, 'CHANNEL_CODE' AS code_key, 'ADM' AS code_value, '관리자 채널' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT (SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key = 'CODE_GROUP' AND code_value = 'RESULT_TYPE') p) AS parent_id, 'RESULT_TYPE' AS code_key, 'S' AS code_value, '성공' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT (SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key = 'CODE_GROUP' AND code_value = 'RESULT_TYPE') p) AS parent_id, 'RESULT_TYPE' AS code_key, 'E' AS code_value, '오류' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT (SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key = 'CODE_GROUP' AND code_value = 'MESSAGE_FORMAT_TYPE') p) AS parent_id, 'MESSAGE_FORMAT_TYPE' AS code_key, 'FIXED' AS code_value, '고정 메시지' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT (SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key = 'CODE_GROUP' AND code_value = 'MESSAGE_FORMAT_TYPE') p) AS parent_id, 'MESSAGE_FORMAT_TYPE' AS code_key, 'INDEXED' AS code_value, '인덱스 파라미터 메시지' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT (SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key = 'CODE_GROUP' AND code_value = 'LOG_LEVEL') p) AS parent_id, 'LOG_LEVEL' AS code_key, 'TRACE' AS code_value, 'TRACE 로그' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT (SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key = 'CODE_GROUP' AND code_value = 'LOG_LEVEL') p) AS parent_id, 'LOG_LEVEL' AS code_key, 'DEBUG' AS code_value, 'DEBUG 로그' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT (SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key = 'CODE_GROUP' AND code_value = 'LOG_LEVEL') p) AS parent_id, 'LOG_LEVEL' AS code_key, 'INFO' AS code_value, 'INFO 로그' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT (SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key = 'CODE_GROUP' AND code_value = 'LOG_LEVEL') p) AS parent_id, 'LOG_LEVEL' AS code_key, 'WARN' AS code_value, 'WARN 로그' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT (SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key = 'CODE_GROUP' AND code_value = 'LOG_LEVEL') p) AS parent_id, 'LOG_LEVEL' AS code_key, 'ERROR' AS code_value, 'ERROR 로그' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT (SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key = 'CODE_GROUP' AND code_value = 'CACHE_NAME') p) AS parent_id, 'CACHE_NAME' AS code_key, 'ALL' AS code_value, '전체 캐시' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT (SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key = 'CODE_GROUP' AND code_value = 'CACHE_NAME') p) AS parent_id, 'CACHE_NAME' AS code_key, 'CODE' AS code_value, '코드 캐시' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT (SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key = 'CODE_GROUP' AND code_value = 'CACHE_NAME') p) AS parent_id, 'CACHE_NAME' AS code_key, 'MESSAGE' AS code_value, '메시지 캐시' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT (SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key = 'CODE_GROUP' AND code_value = 'CACHE_NAME') p) AS parent_id, 'CACHE_NAME' AS code_key, 'RESPONSE_CODE' AS code_value, '응답코드 캐시' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT (SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key = 'CODE_GROUP' AND code_value = 'CACHE_NAME') p) AS parent_id, 'CACHE_NAME' AS code_key, 'CONFIG' AS code_value, '설정 캐시' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT (SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key = 'CODE_GROUP' AND code_value = 'BATCH_JOB_TYPE') p) AS parent_id, 'BATCH_JOB_TYPE' AS code_key, 'TASKLET' AS code_value, 'Tasklet 배치' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT (SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key = 'CODE_GROUP' AND code_value = 'BATCH_JOB_TYPE') p) AS parent_id, 'BATCH_JOB_TYPE' AS code_key, 'CHUNK' AS code_value, 'Chunk 배치' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT (SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key = 'CODE_GROUP' AND code_value = 'BATCH_JOB_TYPE') p) AS parent_id, 'BATCH_JOB_TYPE' AS code_key, 'RETRY' AS code_value, '재처리 배치' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT (SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key = 'CODE_GROUP' AND code_value = 'YN') p) AS parent_id, 'YN' AS code_key, 'Y' AS code_value, '예' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT (SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key = 'CODE_GROUP' AND code_value = 'YN') p) AS parent_id, 'YN' AS code_key, 'N' AS code_value, '아니오' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
) src
ON (tgt.code_key = src.code_key AND tgt.code_value = src.code_value)
WHEN MATCHED THEN UPDATE SET
  tgt.parent_id = src.parent_id,
  tgt.description = src.description,
  tgt.updated_by = src.updated_by,
  tgt.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (parent_id, code_key, code_value, description, created_by, updated_by)
VALUES (src.parent_id, src.code_key, src.code_value, src.description, src.created_by, src.updated_by);

MERGE INTO cpf_message tgt
USING (
  SELECT 'MCPF000000' AS message_code, 'ko' AS locale, 'FIXED' AS message_format_type, '정상 처리되었습니다.' AS external_message, 'CPF 공통 요청이 정상 처리되었습니다.' AS internal_message, 0 AS parameter_count, NULL AS parameter_sample, 'CPF 공통 성공 메시지' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT 'MCPF010001' AS message_code, 'ko' AS locale, 'INDEXED' AS message_format_type, '요청 값이 올바르지 않습니다.' AS external_message, '요청 파라미터 검증에 실패했습니다. field={0}, value={1}' AS internal_message, 2 AS parameter_count, '["memberId","abc"]' AS parameter_sample, 'CPF 파라미터 오류 메시지' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT 'MCPF010002' AS message_code, 'ko' AS locale, 'INDEXED' AS message_format_type, '요청한 정보를 찾을 수 없습니다.' AS external_message, '조회 대상 데이터가 존재하지 않습니다. target={0}' AS internal_message, 1 AS parameter_count, '["member"]' AS parameter_sample, 'CPF 미존재 메시지' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT 'MCPF010003' AS message_code, 'ko' AS locale, 'INDEXED' AS message_format_type, '이미 등록된 정보입니다.' AS external_message, '중복 데이터가 감지되었습니다. key={0}' AS internal_message, 1 AS parameter_count, '["memberNo"]' AS parameter_sample, 'CPF 중복 메시지' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT 'MCPF010004' AS message_code, 'ko' AS locale, 'INDEXED' AS message_format_type, '입력값을 확인해 주세요.' AS external_message, 'Bean Validation 검증에 실패했습니다. field={0}' AS internal_message, 1 AS parameter_count, '["name"]' AS parameter_sample, 'CPF 검증 메시지' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT 'MCPF010005' AS message_code, 'ko' AS locale, 'FIXED' AS message_format_type, '인증이 필요합니다.' AS external_message, '인증되지 않은 요청입니다.' AS internal_message, 0 AS parameter_count, NULL AS parameter_sample, 'CPF 인증 필요 메시지' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT 'MCPF010006' AS message_code, 'ko' AS locale, 'INDEXED' AS message_format_type, '처리 권한이 없습니다.' AS external_message, '인가되지 않은 요청입니다. user={0}' AS internal_message, 1 AS parameter_count, '["guest"]' AS parameter_sample, 'CPF 권한 오류 메시지' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT 'MCPF020001' AS message_code, 'ko' AS locale, 'INDEXED' AS message_format_type, '요청을 처리할 수 없습니다.' AS external_message, '업무 규칙 위반이 발생했습니다. rule={0}' AS internal_message, 1 AS parameter_count, '["business-rule"]' AS parameter_sample, 'CPF 업무 규칙 메시지' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT 'MCPF030001' AS message_code, 'ko' AS locale, 'INDEXED' AS message_format_type, '일시적으로 처리할 수 없습니다.' AS external_message, '외부 또는 타 주제영역 연계 오류가 발생했습니다. service={0}' AS internal_message, 1 AS parameter_count, '["mbr"]' AS parameter_sample, 'CPF 외부 연계 메시지' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT 'MCPF900001' AS message_code, 'ko' AS locale, 'INDEXED' AS message_format_type, '필수 거래 헤더가 누락되었습니다.' AS external_message, 'CPF 거래 헤더 검증에 실패했습니다. header={0}, uri={1}' AS internal_message, 2 AS parameter_count, '["X-Request-Type","/mbr/list"]' AS parameter_sample, 'CPF 헤더 검증 메시지' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT 'MCPF900002' AS message_code, 'ko' AS locale, 'INDEXED' AS message_format_type, '거래 메타데이터 설정이 올바르지 않습니다.' AS external_message, 'CPF @CpfTransaction 메타데이터 검증에 실패했습니다. transactionId={0}' AS internal_message, 1 AS parameter_count, '["MBR01BSE0001"]' AS parameter_sample, 'CPF 메타데이터 메시지' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT 'MCPF900003' AS message_code, 'ko' AS locale, 'INDEXED' AS message_format_type, '서비스 접속 정보가 없습니다.' AS external_message, 'CPF 서비스 endpoint 설정을 찾을 수 없습니다. serviceId={0}' AS internal_message, 1 AS parameter_count, '["mbr"]' AS parameter_sample, 'CPF endpoint 메시지' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT 'MCPF900004' AS message_code, 'ko' AS locale, 'INDEXED' AS message_format_type, '동적 로그레벨 요청이 올바르지 않습니다.' AS external_message, 'CPF 동적 로그레벨 규칙 검증에 실패했습니다. reason={0}' AS internal_message, 1 AS parameter_count, '["transactionId or businessTransactionId required"]' AS parameter_sample, 'CPF 동적 로그 메시지' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT 'MCPF900005' AS message_code, 'ko' AS locale, 'INDEXED' AS message_format_type, '내부 공유 API에 접근할 수 없습니다.' AS external_message, 'CPF 내부 서비스 신원 또는 호출 경로 검증에 실패했습니다. reason={0}' AS internal_message, 1 AS parameter_count, '["service identity verification failed"]' AS parameter_sample, 'CPF 내부 공유 API 접근 거부 메시지' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT 'MCPF990000' AS message_code, 'ko' AS locale, 'INDEXED' AS message_format_type, '처리 중 오류가 발생했습니다.' AS external_message, 'CPF 내부 오류가 발생했습니다. error={0}' AS internal_message, 1 AS parameter_count, '["Exception"]' AS parameter_sample, 'CPF 내부 오류 메시지' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT 'MCPF990001' AS message_code, 'ko' AS locale, 'INDEXED' AS message_format_type, '데이터베이스 오류가 발생했습니다.' AS external_message, '데이터베이스 처리 오류가 발생했습니다. sqlState={0}' AS internal_message, 1 AS parameter_count, '["HY000"]' AS parameter_sample, 'CPF 데이터베이스 오류 메시지' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT 'MBZA000000' AS message_code, 'ko' AS locale, 'FIXED' AS message_format_type, '성공' AS external_message, 'BZA 요청이 정상 처리되었습니다.' AS internal_message, 0 AS parameter_count, NULL AS parameter_sample, 'BZA 성공 메시지' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT 'MBZA010001' AS message_code, 'ko' AS locale, 'INDEXED' AS message_format_type, '업무 요청 값이 올바르지 않습니다.' AS external_message, 'BZA 입력값 검증에 실패했습니다. field={0}' AS internal_message, 1 AS parameter_count, '["field"]' AS parameter_sample, 'BZA 입력값 오류 메시지' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT 'MBZA010002' AS message_code, 'ko' AS locale, 'FIXED' AS message_format_type, '처리 권한이 없습니다.' AS external_message, 'BZA 서버 권한 검사에 실패했습니다.' AS internal_message, 0 AS parameter_count, NULL AS parameter_sample, 'BZA 권한 오류 메시지' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT 'MMBR000000' AS message_code, 'ko' AS locale, 'FIXED' AS message_format_type, '성공' AS external_message, 'MBR 요청이 정상 처리되었습니다.' AS internal_message, 0 AS parameter_count, NULL AS parameter_sample, 'MBR 성공 메시지' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT 'MMBR010001' AS message_code, 'ko' AS locale, 'FIXED' AS message_format_type, '회원이 생성되었습니다.' AS external_message, 'MBR 회원 데이터가 생성되었습니다.' AS internal_message, 0 AS parameter_count, NULL AS parameter_sample, 'MBR 생성 메시지' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT 'MMBR010002' AS message_code, 'ko' AS locale, 'FIXED' AS message_format_type, '회원이 수정되었습니다.' AS external_message, 'MBR 회원 데이터가 수정되었습니다.' AS internal_message, 0 AS parameter_count, NULL AS parameter_sample, 'MBR 수정 메시지' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT 'MMBR010003' AS message_code, 'ko' AS locale, 'FIXED' AS message_format_type, '회원이 삭제되었습니다.' AS external_message, 'MBR 회원 데이터가 삭제되었습니다.' AS internal_message, 0 AS parameter_count, NULL AS parameter_sample, 'MBR 삭제 메시지' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT 'MMBR010101' AS message_code, 'ko' AS locale, 'FIXED' AS message_format_type, '회원 요청 형식이 올바르지 않습니다.' AS external_message, 'MBR 요청 형식이 올바르지 않습니다.' AS internal_message, 0 AS parameter_count, NULL AS parameter_sample, 'MBR bad request 메시지' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT 'MMBR010102' AS message_code, 'ko' AS locale, 'INDEXED' AS message_format_type, '유효하지 않은 회원 파라미터입니다.' AS external_message, 'MBR 파라미터 검증에 실패했습니다. field={0}' AS internal_message, 1 AS parameter_count, '["memberId"]' AS parameter_sample, 'MBR 파라미터 오류 메시지' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT 'MMBR010103' AS message_code, 'ko' AS locale, 'INDEXED' AS message_format_type, '회원 정보를 찾을 수 없습니다.' AS external_message, 'MBR 조회 대상이 없습니다. target={0}' AS internal_message, 1 AS parameter_count, '["member"]' AS parameter_sample, 'MBR 미존재 메시지' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT 'MMBR010104' AS message_code, 'ko' AS locale, 'INDEXED' AS message_format_type, '중복된 회원 데이터가 있습니다.' AS external_message, 'MBR 중복 데이터가 감지되었습니다. key={0}' AS internal_message, 1 AS parameter_count, '["memberNo"]' AS parameter_sample, 'MBR 중복 메시지' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT 'MMBR010105' AS message_code, 'ko' AS locale, 'INDEXED' AS message_format_type, '회원 입력값 검증에 실패했습니다.' AS external_message, 'MBR 입력값 검증에 실패했습니다. field={0}' AS internal_message, 1 AS parameter_count, '["name"]' AS parameter_sample, 'MBR 검증 메시지' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT 'MMBR990000' AS message_code, 'ko' AS locale, 'INDEXED' AS message_format_type, '회원 처리 중 오류가 발생했습니다.' AS external_message, 'MBR 내부 서버 오류가 발생했습니다. error={0}' AS internal_message, 1 AS parameter_count, '["Exception"]' AS parameter_sample, 'MBR 내부 오류 메시지' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT 'MREF090001' AS message_code, 'ko' AS locale, 'INDEXED' AS message_format_type, '이미 등록된 {0}입니다.' AS external_message, '{0}={1} 값이 이미 존재합니다. duplicateCheck=REF_EDU_SAMPLE' AS internal_message, 2 AS parameter_count, '["회원번호","M0001"]' AS parameter_sample, 'REF 동적 중복 교육 메시지' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT 'MCMN000001' AS message_code, 'ko' AS locale, 'FIXED' AS message_format_type, 'CPF 교육 시스템에 오신 것을 환영합니다.' AS external_message, 'CMN education welcome message.' AS internal_message, 0 AS parameter_count, NULL AS parameter_sample, 'CMN 교육 환영 메시지' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT 'MCMN000001' AS message_code, 'en' AS locale, 'FIXED' AS message_format_type, 'Welcome to the CPF education system.' AS external_message, 'CMN education welcome message.' AS internal_message, 0 AS parameter_count, NULL AS parameter_sample, 'CMN 교육 환영 메시지' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
) src
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
USING (
  SELECT 'SCPF000000' AS response_code, 'MCPF000000' AS message_code, 'S' AS result_type, 'CPF' AS module_id, '00' AS response_group, '0000' AS sequence_no, 200 AS http_status, 'CPF 공통 성공' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT 'ECPF010001' AS response_code, 'MCPF010001' AS message_code, 'E' AS result_type, 'CPF' AS module_id, '01' AS response_group, '0001' AS sequence_no, 400 AS http_status, '파라미터 오류' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT 'ECPF010002' AS response_code, 'MCPF010002' AS message_code, 'E' AS result_type, 'CPF' AS module_id, '01' AS response_group, '0002' AS sequence_no, 404 AS http_status, '미존재 오류' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT 'ECPF010003' AS response_code, 'MCPF010003' AS message_code, 'E' AS result_type, 'CPF' AS module_id, '01' AS response_group, '0003' AS sequence_no, 409 AS http_status, '중복 오류' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT 'ECPF010004' AS response_code, 'MCPF010004' AS message_code, 'E' AS result_type, 'CPF' AS module_id, '01' AS response_group, '0004' AS sequence_no, 400 AS http_status, '검증 실패' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT 'ECPF010005' AS response_code, 'MCPF010005' AS message_code, 'E' AS result_type, 'CPF' AS module_id, '01' AS response_group, '0005' AS sequence_no, 401 AS http_status, '인증 필요' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT 'ECPF010006' AS response_code, 'MCPF010006' AS message_code, 'E' AS result_type, 'CPF' AS module_id, '01' AS response_group, '0006' AS sequence_no, 403 AS http_status, '권한 없음' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT 'ECPF020001' AS response_code, 'MCPF020001' AS message_code, 'E' AS result_type, 'CPF' AS module_id, '02' AS response_group, '0001' AS sequence_no, 400 AS http_status, '업무 규칙 위반' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT 'ECPF030001' AS response_code, 'MCPF030001' AS message_code, 'E' AS result_type, 'CPF' AS module_id, '03' AS response_group, '0001' AS sequence_no, 502 AS http_status, '외부 연계 오류' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT 'ECPF900001' AS response_code, 'MCPF900001' AS message_code, 'E' AS result_type, 'CPF' AS module_id, '90' AS response_group, '0001' AS sequence_no, 400 AS http_status, '필수 거래 헤더 누락' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT 'ECPF900002' AS response_code, 'MCPF900002' AS message_code, 'E' AS result_type, 'CPF' AS module_id, '90' AS response_group, '0002' AS sequence_no, 500 AS http_status, '거래 메타데이터 오류' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT 'ECPF900003' AS response_code, 'MCPF900003' AS message_code, 'E' AS result_type, 'CPF' AS module_id, '90' AS response_group, '0003' AS sequence_no, 500 AS http_status, '서비스 endpoint 미등록' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT 'ECPF900004' AS response_code, 'MCPF900004' AS message_code, 'E' AS result_type, 'CPF' AS module_id, '90' AS response_group, '0004' AS sequence_no, 400 AS http_status, '동적 로그 규칙 오류' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT 'ECPF900005' AS response_code, 'MCPF900005' AS message_code, 'E' AS result_type, 'CPF' AS module_id, '90' AS response_group, '0005' AS sequence_no, 403 AS http_status, '내부 공유 API 접근 거부' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT 'ECPF990000' AS response_code, 'MCPF990000' AS message_code, 'E' AS result_type, 'CPF' AS module_id, '99' AS response_group, '0000' AS sequence_no, 500 AS http_status, '내부 서버 오류' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT 'ECPF990001' AS response_code, 'MCPF990001' AS message_code, 'E' AS result_type, 'CPF' AS module_id, '99' AS response_group, '0001' AS sequence_no, 500 AS http_status, '데이터베이스 오류' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT 'SBZA000000' AS response_code, 'MBZA000000' AS message_code, 'S' AS result_type, 'BZA' AS module_id, '00' AS response_group, '0000' AS sequence_no, 200 AS http_status, 'BZA 성공' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT 'EBZA010001' AS response_code, 'MBZA010001' AS message_code, 'E' AS result_type, 'BZA' AS module_id, '01' AS response_group, '0001' AS sequence_no, 400 AS http_status, 'BZA 입력값 오류' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT 'EBZA010002' AS response_code, 'MBZA010002' AS message_code, 'E' AS result_type, 'BZA' AS module_id, '01' AS response_group, '0002' AS sequence_no, 403 AS http_status, 'BZA 권한 오류' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT 'SMBR000000' AS response_code, 'MMBR000000' AS message_code, 'S' AS result_type, 'MBR' AS module_id, '00' AS response_group, '0000' AS sequence_no, 200 AS http_status, 'MBR 성공' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT 'SMBR010001' AS response_code, 'MMBR010001' AS message_code, 'S' AS result_type, 'MBR' AS module_id, '01' AS response_group, '0001' AS sequence_no, 200 AS http_status, 'MBR 생성 성공' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT 'SMBR010002' AS response_code, 'MMBR010002' AS message_code, 'S' AS result_type, 'MBR' AS module_id, '01' AS response_group, '0002' AS sequence_no, 200 AS http_status, 'MBR 수정 성공' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT 'SMBR010003' AS response_code, 'MMBR010003' AS message_code, 'S' AS result_type, 'MBR' AS module_id, '01' AS response_group, '0003' AS sequence_no, 200 AS http_status, 'MBR 삭제 성공' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT 'EMBR010001' AS response_code, 'MMBR010101' AS message_code, 'E' AS result_type, 'MBR' AS module_id, '01' AS response_group, '0001' AS sequence_no, 400 AS http_status, 'MBR 요청 형식 오류' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT 'EMBR010002' AS response_code, 'MMBR010102' AS message_code, 'E' AS result_type, 'MBR' AS module_id, '01' AS response_group, '0002' AS sequence_no, 400 AS http_status, 'MBR 파라미터 오류' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT 'EMBR010003' AS response_code, 'MMBR010103' AS message_code, 'E' AS result_type, 'MBR' AS module_id, '01' AS response_group, '0003' AS sequence_no, 404 AS http_status, 'MBR 미존재' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT 'EMBR010004' AS response_code, 'MMBR010104' AS message_code, 'E' AS result_type, 'MBR' AS module_id, '01' AS response_group, '0004' AS sequence_no, 409 AS http_status, 'MBR 중복' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT 'EMBR010005' AS response_code, 'MMBR010105' AS message_code, 'E' AS result_type, 'MBR' AS module_id, '01' AS response_group, '0005' AS sequence_no, 400 AS http_status, 'MBR 검증 실패' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT 'EMBR990000' AS response_code, 'MMBR990000' AS message_code, 'E' AS result_type, 'MBR' AS module_id, '99' AS response_group, '0000' AS sequence_no, 500 AS http_status, 'MBR 내부 오류' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
) src
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
USING (
  SELECT 'CPF.CMN.CACHE.PRELOAD_ENABLED' AS config_key, 'Y' AS config_value, 'BOOLEAN' AS config_type, 'CMN 캐시 기동 시 선적재 여부' AS description, 'N' AS encrypted_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT 'CPF.CMN.CACHE.FAIL_FAST_ON_STARTUP' AS config_key, 'N' AS config_value, 'BOOLEAN' AS config_type, '캐시 선적재 실패 시 기동 실패 여부' AS description, 'N' AS encrypted_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT 'CPF.CMN.CACHE.REFRESH_POLL_MILLIS' AS config_key, '5000' AS config_value, 'NUMBER' AS config_type, '캐시 갱신 이벤트 polling 주기' AS description, 'N' AS encrypted_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT 'CPF.CMN.MESSAGING.BROKER' AS config_key, 'IN_MEMORY' AS config_value, 'STRING' AS config_type, '기본 CMN 메시지 브로커 유형' AS description, 'N' AS encrypted_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT 'CPF.HTTP.CONNECT_TIMEOUT_MS' AS config_key, '3000' AS config_value, 'NUMBER' AS config_type, 'CPF HTTP client 연결 timeout' AS description, 'N' AS encrypted_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT 'CPF.HTTP.READ_TIMEOUT_MS' AS config_key, '5000' AS config_value, 'NUMBER' AS config_type, 'CPF HTTP client 읽기 timeout' AS description, 'N' AS encrypted_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT 'CPF.ADM.SESSION_TTL_SECONDS' AS config_key, '3600' AS config_value, 'NUMBER' AS config_type, 'ADM 세션 TTL 초' AS description, 'N' AS encrypted_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT 'CPF.ADM.PASSWORD_EXPIRE_DAYS' AS config_key, '90' AS config_value, 'NUMBER' AS config_type, 'ADM 비밀번호 만료 일수' AS description, 'N' AS encrypted_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT 'CPF.ADM.PASSWORD_MIN_LENGTH' AS config_key, '10' AS config_value, 'NUMBER' AS config_type, 'ADM 비밀번호 최소 길이' AS description, 'N' AS encrypted_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT 'CPF.ADM.PASSWORD_MAX_FAIL_COUNT' AS config_key, '5' AS config_value, 'NUMBER' AS config_type, 'ADM 로그인 실패 잠금 기준' AS description, 'N' AS encrypted_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT 'CPF.BATCH.DEFAULT_LOCK_SECONDS' AS config_key, '3600' AS config_value, 'NUMBER' AS config_type, '배치 기본 lock 만료 초' AS description, 'N' AS encrypted_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT 'CPF.FEATURE.SAMPLE_ENABLED' AS config_key, 'Y' AS config_value, 'BOOLEAN' AS config_type, '샘플 API와 교육 flow 활성화 여부' AS description, 'N' AS encrypted_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
) src
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
USING (
  SELECT NULL AS parent_id, 'CODE_GROUP' AS code_key, 'HTTP_METHOD' AS code_value, 'HTTP Method 코드 그룹' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT NULL AS parent_id, 'CODE_GROUP' AS code_key, 'EXECUTION_STATUS' AS code_value, '실행 상태 코드 그룹' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT NULL AS parent_id, 'CODE_GROUP' AS code_key, 'ASYNC_STATUS' AS code_value, '비동기 처리 상태 코드 그룹' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT NULL AS parent_id, 'CODE_GROUP' AS code_key, 'RETRY_STATUS' AS code_value, '재시도 상태 코드 그룹' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT NULL AS parent_id, 'CODE_GROUP' AS code_key, 'IDEMPOTENCY_STATUS' AS code_value, '멱등 처리 상태 코드 그룹' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT NULL AS parent_id, 'CODE_GROUP' AS code_key, 'HEALTH_STATUS' AS code_value, 'Health 상태 코드 그룹' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT NULL AS parent_id, 'CODE_GROUP' AS code_key, 'CIRCUIT_STATUS' AS code_value, 'Circuit Breaker 상태 코드 그룹' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT NULL AS parent_id, 'CODE_GROUP' AS code_key, 'FILE_SCAN_STATUS' AS code_value, '첨부/파일 검사 상태 코드 그룹' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT NULL AS parent_id, 'CODE_GROUP' AS code_key, 'DATA_CLASSIFICATION' AS code_value, '데이터 민감도 등급 코드 그룹' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT NULL AS parent_id, 'CODE_GROUP' AS code_key, 'APPROVAL_STATUS' AS code_value, '결재 상태 코드 그룹' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT NULL AS parent_id, 'CODE_GROUP' AS code_key, 'ERROR_CATEGORY' AS code_value, '오류 분류 코드 그룹' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT NULL AS parent_id, 'CODE_GROUP' AS code_key, 'RETENTION_ACTION' AS code_value, '보존 정책 실행 유형 코드 그룹' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
) src
ON (tgt.code_key = src.code_key AND tgt.code_value = src.code_value)
WHEN MATCHED THEN UPDATE SET
  tgt.description = src.description,
  tgt.updated_by = src.updated_by,
  tgt.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (parent_id, code_key, code_value, description, created_by, updated_by)
VALUES (src.parent_id, src.code_key, src.code_value, src.description, src.created_by, src.updated_by);

MERGE INTO cpf_code tgt
USING (
  SELECT (SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key='CODE_GROUP' AND code_value='HTTP_METHOD') x) AS parent_id, 'HTTP_METHOD' AS code_key, 'GET' AS code_value, '조회' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT (SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key='CODE_GROUP' AND code_value='HTTP_METHOD') x) AS parent_id, 'HTTP_METHOD' AS code_key, 'POST' AS code_value, '등록/명령' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT (SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key='CODE_GROUP' AND code_value='HTTP_METHOD') x) AS parent_id, 'HTTP_METHOD' AS code_key, 'PUT' AS code_value, '전체 수정' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT (SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key='CODE_GROUP' AND code_value='HTTP_METHOD') x) AS parent_id, 'HTTP_METHOD' AS code_key, 'PATCH' AS code_value, '부분 수정' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT (SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key='CODE_GROUP' AND code_value='HTTP_METHOD') x) AS parent_id, 'HTTP_METHOD' AS code_key, 'DELETE' AS code_value, '삭제/회수' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT (SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key='CODE_GROUP' AND code_value='EXECUTION_STATUS') x) AS parent_id, 'EXECUTION_STATUS' AS code_key, 'READY' AS code_value, '실행 준비' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT (SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key='CODE_GROUP' AND code_value='EXECUTION_STATUS') x) AS parent_id, 'EXECUTION_STATUS' AS code_key, 'RUNNING' AS code_value, '실행 중' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT (SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key='CODE_GROUP' AND code_value='EXECUTION_STATUS') x) AS parent_id, 'EXECUTION_STATUS' AS code_key, 'SUCCESS' AS code_value, '정상 완료' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT (SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key='CODE_GROUP' AND code_value='EXECUTION_STATUS') x) AS parent_id, 'EXECUTION_STATUS' AS code_key, 'FAILED' AS code_value, '실패' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT (SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key='CODE_GROUP' AND code_value='EXECUTION_STATUS') x) AS parent_id, 'EXECUTION_STATUS' AS code_key, 'UNKNOWN_RESULT' AS code_value, '결과 미확정' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT (SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key='CODE_GROUP' AND code_value='ASYNC_STATUS') x) AS parent_id, 'ASYNC_STATUS' AS code_key, 'WAITING' AS code_value, '비동기 대기' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT (SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key='CODE_GROUP' AND code_value='ASYNC_STATUS') x) AS parent_id, 'ASYNC_STATUS' AS code_key, 'PROCESSING' AS code_value, '비동기 처리 중' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT (SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key='CODE_GROUP' AND code_value='ASYNC_STATUS') x) AS parent_id, 'ASYNC_STATUS' AS code_key, 'COMPLETED' AS code_value, '비동기 완료' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT (SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key='CODE_GROUP' AND code_value='ASYNC_STATUS') x) AS parent_id, 'ASYNC_STATUS' AS code_key, 'DLQ' AS code_value, 'Dead Letter Queue' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT (SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key='CODE_GROUP' AND code_value='RETRY_STATUS') x) AS parent_id, 'RETRY_STATUS' AS code_key, 'RETRYABLE' AS code_value, '재시도 가능' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT (SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key='CODE_GROUP' AND code_value='RETRY_STATUS') x) AS parent_id, 'RETRY_STATUS' AS code_key, 'NON_RETRYABLE' AS code_value, '재시도 금지' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT (SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key='CODE_GROUP' AND code_value='RETRY_STATUS') x) AS parent_id, 'RETRY_STATUS' AS code_key, 'EXHAUSTED' AS code_value, '재시도 소진' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT (SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key='CODE_GROUP' AND code_value='IDEMPOTENCY_STATUS') x) AS parent_id, 'IDEMPOTENCY_STATUS' AS code_key, 'PROCESSING' AS code_value, '멱등 처리 중' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT (SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key='CODE_GROUP' AND code_value='IDEMPOTENCY_STATUS') x) AS parent_id, 'IDEMPOTENCY_STATUS' AS code_key, 'COMPLETED' AS code_value, '멱등 처리 완료' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT (SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key='CODE_GROUP' AND code_value='IDEMPOTENCY_STATUS') x) AS parent_id, 'IDEMPOTENCY_STATUS' AS code_key, 'FAILED' AS code_value, '멱등 처리 실패' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT (SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key='CODE_GROUP' AND code_value='IDEMPOTENCY_STATUS') x) AS parent_id, 'IDEMPOTENCY_STATUS' AS code_key, 'UNKNOWN_RESULT' AS code_value, '멱등 결과 미확정' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT (SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key='CODE_GROUP' AND code_value='HEALTH_STATUS') x) AS parent_id, 'HEALTH_STATUS' AS code_key, 'UP' AS code_value, '정상' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT (SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key='CODE_GROUP' AND code_value='HEALTH_STATUS') x) AS parent_id, 'HEALTH_STATUS' AS code_key, 'DOWN' AS code_value, '장애' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT (SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key='CODE_GROUP' AND code_value='HEALTH_STATUS') x) AS parent_id, 'HEALTH_STATUS' AS code_key, 'DEGRADED' AS code_value, '부분 저하' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT (SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key='CODE_GROUP' AND code_value='CIRCUIT_STATUS') x) AS parent_id, 'CIRCUIT_STATUS' AS code_key, 'CLOSED' AS code_value, '정상 호출' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT (SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key='CODE_GROUP' AND code_value='CIRCUIT_STATUS') x) AS parent_id, 'CIRCUIT_STATUS' AS code_key, 'OPEN' AS code_value, '호출 차단' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT (SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key='CODE_GROUP' AND code_value='CIRCUIT_STATUS') x) AS parent_id, 'CIRCUIT_STATUS' AS code_key, 'HALF_OPEN' AS code_value, '복구 시험' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT (SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key='CODE_GROUP' AND code_value='FILE_SCAN_STATUS') x) AS parent_id, 'FILE_SCAN_STATUS' AS code_key, 'PENDING' AS code_value, '검사 대기' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT (SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key='CODE_GROUP' AND code_value='FILE_SCAN_STATUS') x) AS parent_id, 'FILE_SCAN_STATUS' AS code_key, 'CLEAN' AS code_value, '검사 정상' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT (SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key='CODE_GROUP' AND code_value='FILE_SCAN_STATUS') x) AS parent_id, 'FILE_SCAN_STATUS' AS code_key, 'INFECTED' AS code_value, '악성 탐지' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT (SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key='CODE_GROUP' AND code_value='FILE_SCAN_STATUS') x) AS parent_id, 'FILE_SCAN_STATUS' AS code_key, 'FAILED' AS code_value, '검사 실패' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT (SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key='CODE_GROUP' AND code_value='FILE_SCAN_STATUS') x) AS parent_id, 'FILE_SCAN_STATUS' AS code_key, 'QUARANTINED' AS code_value, '격리' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT (SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key='CODE_GROUP' AND code_value='DATA_CLASSIFICATION') x) AS parent_id, 'DATA_CLASSIFICATION' AS code_key, 'PUBLIC' AS code_value, '공개 정보' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT (SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key='CODE_GROUP' AND code_value='DATA_CLASSIFICATION') x) AS parent_id, 'DATA_CLASSIFICATION' AS code_key, 'INTERNAL' AS code_value, '내부 정보' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT (SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key='CODE_GROUP' AND code_value='DATA_CLASSIFICATION') x) AS parent_id, 'DATA_CLASSIFICATION' AS code_key, 'CONFIDENTIAL' AS code_value, '기밀 정보' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT (SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key='CODE_GROUP' AND code_value='DATA_CLASSIFICATION') x) AS parent_id, 'DATA_CLASSIFICATION' AS code_key, 'RESTRICTED' AS code_value, '제한/민감 정보' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT (SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key='CODE_GROUP' AND code_value='APPROVAL_STATUS') x) AS parent_id, 'APPROVAL_STATUS' AS code_key, 'DRAFT' AS code_value, '작성 중' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT (SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key='CODE_GROUP' AND code_value='APPROVAL_STATUS') x) AS parent_id, 'APPROVAL_STATUS' AS code_key, 'IN_REVIEW' AS code_value, '결재 진행' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT (SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key='CODE_GROUP' AND code_value='APPROVAL_STATUS') x) AS parent_id, 'APPROVAL_STATUS' AS code_key, 'APPROVED' AS code_value, '승인 완료' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT (SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key='CODE_GROUP' AND code_value='APPROVAL_STATUS') x) AS parent_id, 'APPROVAL_STATUS' AS code_key, 'REJECTED' AS code_value, '반려' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT (SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key='CODE_GROUP' AND code_value='APPROVAL_STATUS') x) AS parent_id, 'APPROVAL_STATUS' AS code_key, 'WITHDRAWN' AS code_value, '철회' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT (SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key='CODE_GROUP' AND code_value='APPROVAL_STATUS') x) AS parent_id, 'APPROVAL_STATUS' AS code_key, 'CANCELED' AS code_value, '취소' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT (SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key='CODE_GROUP' AND code_value='APPROVAL_STATUS') x) AS parent_id, 'APPROVAL_STATUS' AS code_key, 'EXPIRED' AS code_value, '만료' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT (SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key='CODE_GROUP' AND code_value='ERROR_CATEGORY') x) AS parent_id, 'ERROR_CATEGORY' AS code_key, 'VALIDATION' AS code_value, '입력/계약 검증 오류' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT (SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key='CODE_GROUP' AND code_value='ERROR_CATEGORY') x) AS parent_id, 'ERROR_CATEGORY' AS code_key, 'AUTHENTICATION' AS code_value, '인증 오류' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT (SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key='CODE_GROUP' AND code_value='ERROR_CATEGORY') x) AS parent_id, 'ERROR_CATEGORY' AS code_key, 'AUTHORIZATION' AS code_value, '인가 오류' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT (SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key='CODE_GROUP' AND code_value='ERROR_CATEGORY') x) AS parent_id, 'ERROR_CATEGORY' AS code_key, 'CONFLICT' AS code_value, '동시성/중복 오류' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT (SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key='CODE_GROUP' AND code_value='ERROR_CATEGORY') x) AS parent_id, 'ERROR_CATEGORY' AS code_key, 'TIMEOUT' AS code_value, 'Timeout' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT (SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key='CODE_GROUP' AND code_value='ERROR_CATEGORY') x) AS parent_id, 'ERROR_CATEGORY' AS code_key, 'TARGET_DOWN' AS code_value, '호출 대상 장애' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT (SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key='CODE_GROUP' AND code_value='ERROR_CATEGORY') x) AS parent_id, 'ERROR_CATEGORY' AS code_key, 'UNKNOWN_RESULT' AS code_value, '결과 미확정' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT (SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key='CODE_GROUP' AND code_value='RETENTION_ACTION') x) AS parent_id, 'RETENTION_ACTION' AS code_key, 'ARCHIVE' AS code_value, '보관소 이관' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT (SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key='CODE_GROUP' AND code_value='RETENTION_ACTION') x) AS parent_id, 'RETENTION_ACTION' AS code_key, 'PURGE' AS code_value, '정책 삭제' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT (SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key='CODE_GROUP' AND code_value='RETENTION_ACTION') x) AS parent_id, 'RETENTION_ACTION' AS code_key, 'LEGAL_HOLD' AS code_value, '법적 보존' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
) src
ON (tgt.code_key = src.code_key AND tgt.code_value = src.code_value)
WHEN MATCHED THEN UPDATE SET
  tgt.parent_id = src.parent_id,
  tgt.description = src.description,
  tgt.updated_by = src.updated_by,
  tgt.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (parent_id, code_key, code_value, description, created_by, updated_by)
VALUES (src.parent_id, src.code_key, src.code_value, src.description, src.created_by, src.updated_by);

MERGE INTO cpf_message tgt
USING (
  SELECT 'MCPF030002' AS message_code, 'ko' AS locale, 'FIXED' AS message_format_type, '요청 시간이 초과되었습니다.' AS external_message, '대상 호출 timeout이 발생했습니다.' AS internal_message, 0 AS parameter_count, NULL AS parameter_sample, '공통 Timeout 메시지' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT 'MCPF030003' AS message_code, 'ko' AS locale, 'FIXED' AS message_format_type, '연결 대상이 일시적으로 사용할 수 없습니다.' AS external_message, '대상 서비스가 DOWN/OPEN 상태입니다.' AS internal_message, 0 AS parameter_count, NULL AS parameter_sample, 'Target down 메시지' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT 'MCPF030004' AS message_code, 'ko' AS locale, 'FIXED' AS message_format_type, '처리 결과를 확인 중입니다.' AS external_message, '요청 결과가 UNKNOWN_RESULT로 분류되어 대사가 필요합니다.' AS internal_message, 0 AS parameter_count, NULL AS parameter_sample, '결과 미확정 메시지' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT 'MCPF020002' AS message_code, 'ko' AS locale, 'FIXED' AS message_format_type, '다른 사용자가 먼저 변경했습니다. 다시 조회해 주세요.' AS external_message, '낙관적 잠금 Version 충돌이 발생했습니다.' AS internal_message, 0 AS parameter_count, NULL AS parameter_sample, '동시성 충돌 메시지' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT 'MCPF020003' AS message_code, 'ko' AS locale, 'FIXED' AS message_format_type, '동일 요청이 이미 처리되었습니다.' AS external_message, 'Idempotency key가 이미 완료된 요청입니다.' AS internal_message, 0 AS parameter_count, NULL AS parameter_sample, '멱등 중복 메시지' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT 'MCPF040001' AS message_code, 'ko' AS locale, 'FIXED' AS message_format_type, '첨부파일 검사가 완료되지 않았습니다.' AS external_message, '첨부 다운로드는 CLEAN 상태에서만 허용됩니다.' AS internal_message, 0 AS parameter_count, NULL AS parameter_sample, '첨부 보안 메시지' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT 'MCPF040002' AS message_code, 'ko' AS locale, 'FIXED' AS message_format_type, '첨부파일이 보안 정책에 의해 격리되었습니다.' AS external_message, 'INFECTED/QUARANTINED 파일 접근이 차단되었습니다.' AS internal_message, 0 AS parameter_count, NULL AS parameter_sample, '첨부 격리 메시지' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
) src
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
USING (
  SELECT 'ECPF030002' AS response_code, 'MCPF030002' AS message_code, 'E' AS result_type, 'CPF' AS module_id, '03' AS response_group, '0002' AS sequence_no, 504 AS http_status, 'Timeout' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT 'ECPF030003' AS response_code, 'MCPF030003' AS message_code, 'E' AS result_type, 'CPF' AS module_id, '03' AS response_group, '0003' AS sequence_no, 503 AS http_status, 'Target down' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT 'ECPF030004' AS response_code, 'MCPF030004' AS message_code, 'E' AS result_type, 'CPF' AS module_id, '03' AS response_group, '0004' AS sequence_no, 202 AS http_status, 'UNKNOWN_RESULT' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT 'ECPF020002' AS response_code, 'MCPF020002' AS message_code, 'E' AS result_type, 'CPF' AS module_id, '02' AS response_group, '0002' AS sequence_no, 409 AS http_status, 'Optimistic lock conflict' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT 'ECPF020003' AS response_code, 'MCPF020003' AS message_code, 'E' AS result_type, 'CPF' AS module_id, '02' AS response_group, '0003' AS sequence_no, 409 AS http_status, 'Idempotency duplicate' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT 'ECPF040001' AS response_code, 'MCPF040001' AS message_code, 'E' AS result_type, 'CPF' AS module_id, '04' AS response_group, '0001' AS sequence_no, 423 AS http_status, 'File scan pending' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT 'ECPF040002' AS response_code, 'MCPF040002' AS message_code, 'E' AS result_type, 'CPF' AS module_id, '04' AS response_group, '0002' AS sequence_no, 403 AS http_status, 'File quarantined' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
) src
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
USING (
  SELECT 'CPF.BZA.SECURITY.MAX_LOGIN_FAIL_COUNT' AS config_key, '5' AS config_value, 'NUMBER' AS config_type, 'BZA 로그인 실패 잠금 기준' AS description, 'N' AS encrypted_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT 'CPF.BZA.SECURITY.ACCESS_TOKEN_TTL_SECONDS' AS config_key, '600' AS config_value, 'NUMBER' AS config_type, 'BZA Access Token TTL' AS description, 'N' AS encrypted_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT 'CPF.BZA.SECURITY.REFRESH_TOKEN_TTL_SECONDS' AS config_key, '7200' AS config_value, 'NUMBER' AS config_type, 'BZA Refresh Token TTL' AS description, 'N' AS encrypted_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT 'CPF.RETENTION.EXECUTE_ENABLED' AS config_key, 'N' AS config_value, 'BOOLEAN' AS config_type, '실제 Archive/Purge 실행 Kill Switch 기본 OFF' AS description, 'N' AS encrypted_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT 'CPF.FILE.DOWNLOAD_REQUIRE_CLEAN' AS config_key, 'Y' AS config_value, 'BOOLEAN' AS config_type, '첨부 다운로드 CLEAN 상태 강제' AS description, 'N' AS encrypted_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT 'CPF.HEALTH.INSTANCE_ID_REQUIRED' AS config_key, 'Y' AS config_value, 'BOOLEAN' AS config_type, '운영 Health 응답 인스턴스 식별자 필수' AS description, 'N' AS encrypted_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
) src
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
USING (
  SELECT 'ONLINE_DEFAULT' AS policy_key, '온라인 거래 기본 로그 정책' AS policy_name, 'ONLINE_TRANSACTION' AS target_type, '*' AS target_id, 'INFO' AS log_level, 'Y' AS db_log_enabled_yn, 'Y' AS file_log_enabled_yn, 'N' AS request_body_log_yn, 'N' AS response_body_log_yn, 'Y' AS error_stack_log_yn, 90 AS retention_days, 100.00 AS sampling_rate, 100 AS priority, 'Y' AS active_yn, '온라인 Controller/API 기본 로그 정책' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT 'BATCH_DEFAULT' AS policy_key, '배치 기본 로그 정책' AS policy_name, 'BATCH_JOB' AS target_type, '*' AS target_id, 'INFO' AS log_level, 'Y' AS db_log_enabled_yn, 'Y' AS file_log_enabled_yn, 'N' AS request_body_log_yn, 'N' AS response_body_log_yn, 'Y' AS error_stack_log_yn, 180 AS retention_days, 100.00 AS sampling_rate, 100 AS priority, 'Y' AS active_yn, 'Spring Batch Job 기본 로그 정책' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT 'ADM_OPERATION_DEFAULT' AS policy_key, 'ADM 운영 기본 로그 정책' AS policy_name, 'MODULE' AS target_type, 'ADM' AS target_id, 'INFO' AS log_level, 'Y' AS db_log_enabled_yn, 'Y' AS file_log_enabled_yn, 'N' AS request_body_log_yn, 'N' AS response_body_log_yn, 'Y' AS error_stack_log_yn, 365 AS retention_days, 100.00 AS sampling_rate, 50 AS priority, 'Y' AS active_yn, 'ADM 운영 API 기본 로그 정책' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
) src
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
USING (
  SELECT 'local-cpf-hs256-001' AS KEY_ID, 'CPF' AS ISSUER, 'HS256' AS ALGORITHM, 'ENV:CPF_CMN_SECURITY_JWT_SECRET' AS SECRET_REF, 'Y' AS ACTIVE_YN, NULL AS EXPIRE_AT, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
) src
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
SELECT 'ALL', 'INITIAL_LOAD', 'INITIAL_FRAMEWORK_SEED', 'SQL', 'SYSTEM', 'SYSTEM', 'SYSTEM' FROM dual WHERE NOT EXISTS (
    SELECT 1
    FROM cpf_cache_refresh_event
    WHERE cache_name = 'ALL'
      AND event_type = 'INITIAL_LOAD'
      AND event_key = 'INITIAL_FRAMEWORK_SEED'
);

MERGE INTO cpf_notification_rule tgt
USING (
  SELECT 'BATCH_EXECUTION' AS event_type, 'FAILED' AS event_sub_type, 'ADM' AS channel_code, 'BATCH_FAILED_DEFAULT' AS template_code, 'ERROR' AS severity, 'ADM_BATCH_OPERATOR' AS receiver_group, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT 'SECURITY_EVENT' AS event_type, 'LOGIN_FAILURE' AS event_sub_type, 'ADM' AS channel_code, 'SECURITY_LOGIN_FAILURE' AS template_code, 'WARN' AS severity, 'ADM_SECURITY_OPERATOR' AS receiver_group, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
) src
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
SELECT NULL, 'CODE_GROUP', 'SORT_DIRECTION', '표준 정렬 방향', 'SYSTEM', 'SYSTEM' FROM dual WHERE NOT EXISTS (SELECT 1 FROM cpf_code WHERE code_key='CODE_GROUP' AND code_value='SORT_DIRECTION');

MERGE INTO cpf_code tgt
USING (
  SELECT (SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key='CODE_GROUP' AND code_value='SORT_DIRECTION') x) AS parent_id, 'SORT_DIRECTION' AS code_key, 'ASC' AS code_value, '오름차순' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT (SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key='CODE_GROUP' AND code_value='SORT_DIRECTION') x) AS parent_id, 'SORT_DIRECTION' AS code_key, 'DESC' AS code_value, '내림차순' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
) src
ON (tgt.code_key = src.code_key AND tgt.code_value = src.code_value)
WHEN MATCHED THEN UPDATE SET
  tgt.parent_id = src.parent_id,
  tgt.description = src.description,
  tgt.updated_by = src.updated_by,
  tgt.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (parent_id, code_key, code_value, description, created_by, updated_by)
VALUES (src.parent_id, src.code_key, src.code_value, src.description, src.created_by, src.updated_by);

MERGE INTO cpf_message tgt
USING (
  SELECT 'MCPF020004' AS message_code, 'ko' AS locale, 'FIXED' AS message_format_type, '요청 사용자 정보가 인증 사용자와 일치하지 않습니다.' AS external_message, 'Body requester spoofing이 차단되었습니다.' AS internal_message, 0 AS parameter_count, NULL AS parameter_sample, 'Requester spoof 차단' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT 'MCPF020005' AS message_code, 'ko' AS locale, 'FIXED' AS message_format_type, '이미 사용된 정책 버전은 직접 수정할 수 없습니다.' AS external_message, '사용된 Approval Policy version은 immutable입니다.' AS internal_message, 0 AS parameter_count, NULL AS parameter_sample, '정책 버전 불변성' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT 'MCPF020006' AS message_code, 'ko' AS locale, 'FIXED' AS message_format_type, '동일 작업 식별자가 다른 요청에 사용되었습니다.' AS external_message, 'operationId payload 충돌입니다.' AS internal_message, 0 AS parameter_count, NULL AS parameter_sample, '멱등 작업 충돌' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT 'MCPF020007' AS message_code, 'ko' AS locale, 'FIXED' AS message_format_type, '현재 데이터가 다른 요청에서 변경되었습니다.' AS external_message, 'expectedVersion CAS가 실패했습니다.' AS internal_message, 0 AS parameter_count, NULL AS parameter_sample, '낙관적 잠금 재조회' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT 'MCPF040003' AS message_code, 'ko' AS locale, 'FIXED' AS message_format_type, '보존 정책에 의해 해당 데이터는 삭제할 수 없습니다.' AS external_message, 'LEGAL_HOLD가 적용되어 destructive retention을 차단했습니다.' AS internal_message, 0 AS parameter_count, NULL AS parameter_sample, 'Legal hold' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT 'MCPF040004' AS message_code, 'ko' AS locale, 'FIXED' AS message_format_type, '보존 작업 실행이 비활성화되어 있습니다.' AS external_message, 'CPF.RETENTION.EXECUTE_ENABLED kill switch가 OFF입니다.' AS internal_message, 0 AS parameter_count, NULL AS parameter_sample, 'Retention kill switch' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT 'MCPF050001' AS message_code, 'ko' AS locale, 'FIXED' AS message_format_type, 'Secret 원문은 조회할 수 없습니다.' AS external_message, 'Secret API는 metadata/reference만 노출합니다.' AS internal_message, 0 AS parameter_count, NULL AS parameter_sample, 'Secret 비노출' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT 'MCPF050002' AS message_code, 'ko' AS locale, 'FIXED' AS message_format_type, '테넌트 식별정보가 필요합니다.' AS external_message, 'Tenant mode에서 resolver가 tenantId를 결정하지 못했습니다.' AS internal_message, 0 AS parameter_count, NULL AS parameter_sample, 'Tenant 필수' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
) src
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
USING (
  SELECT 'ECPF020004' AS response_code, 'MCPF020004' AS message_code, 'E' AS result_type, 'CPF' AS module_id, '02' AS response_group, '0004' AS sequence_no, 403 AS http_status, 'Requester spoof blocked' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT 'ECPF020005' AS response_code, 'MCPF020005' AS message_code, 'E' AS result_type, 'CPF' AS module_id, '02' AS response_group, '0005' AS sequence_no, 409 AS http_status, 'Policy version immutable' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT 'ECPF020006' AS response_code, 'MCPF020006' AS message_code, 'E' AS result_type, 'CPF' AS module_id, '02' AS response_group, '0006' AS sequence_no, 409 AS http_status, 'Operation id conflict' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT 'ECPF020007' AS response_code, 'MCPF020007' AS message_code, 'E' AS result_type, 'CPF' AS module_id, '02' AS response_group, '0007' AS sequence_no, 409 AS http_status, 'Optimistic lock retry' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT 'ECPF040003' AS response_code, 'MCPF040003' AS message_code, 'E' AS result_type, 'CPF' AS module_id, '04' AS response_group, '0003' AS sequence_no, 423 AS http_status, 'Legal hold' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT 'ECPF040004' AS response_code, 'MCPF040004' AS message_code, 'E' AS result_type, 'CPF' AS module_id, '04' AS response_group, '0004' AS sequence_no, 403 AS http_status, 'Retention disabled' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT 'ECPF050001' AS response_code, 'MCPF050001' AS message_code, 'E' AS result_type, 'CPF' AS module_id, '05' AS response_group, '0001' AS sequence_no, 403 AS http_status, 'Secret value hidden' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT 'ECPF050002' AS response_code, 'MCPF050002' AS message_code, 'E' AS result_type, 'CPF' AS module_id, '05' AS response_group, '0002' AS sequence_no, 400 AS http_status, 'Tenant required' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
) src
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
USING (
  SELECT 'CPF.PAGING.DEFAULT_SIZE' AS config_key, '20' AS config_value, 'NUMBER' AS config_type, '공통 Page 기본 크기' AS description, 'N' AS encrypted_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT 'CPF.PAGING.MAX_SIZE' AS config_key, '200' AS config_value, 'NUMBER' AS config_type, '공통 Page 최대 크기' AS description, 'N' AS encrypted_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT 'CPF.RETENTION.DRY_RUN_DEFAULT' AS config_key, 'Y' AS config_value, 'BOOLEAN' AS config_type, 'Retention 기본 Dry-run' AS description, 'N' AS encrypted_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT 'CPF.RETENTION.EXECUTE_ENABLED' AS config_key, 'N' AS config_value, 'BOOLEAN' AS config_type, '실제 Archive/Purge 실행 Kill Switch 기본 OFF' AS description, 'N' AS encrypted_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT 'CPF.SECRET.CACHE_TTL_SECONDS' AS config_key, '300' AS config_value, 'NUMBER' AS config_type, 'Secret metadata/cache 기본 TTL' AS description, 'N' AS encrypted_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT 'CPF.TENANT.ENABLED' AS config_key, 'N' AS config_value, 'BOOLEAN' AS config_type, 'Tenant context 기능 기본 OFF' AS description, 'N' AS encrypted_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT 'CPF.HEALTH.REMOTE_DEPENDENCY_GATES_READINESS' AS config_key, 'N' AS config_value, 'BOOLEAN' AS config_type, 'Remote owner 장애가 local readiness를 직접 차단하지 않음' AS description, 'N' AS encrypted_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
) src
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
USING (
  SELECT (SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key='CODE_GROUP' AND code_value='REQUEST_TYPE') x) AS parent_id, 'REQUEST_TYPE' AS code_key, 'O' AS code_value, '온라인 요청' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT (SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key='CODE_GROUP' AND code_value='REQUEST_TYPE') x) AS parent_id, 'REQUEST_TYPE' AS code_key, 'S' AS code_value, '공유 내부 서비스 요청' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT (SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key='CODE_GROUP' AND code_value='REQUEST_TYPE') x) AS parent_id, 'REQUEST_TYPE' AS code_key, 'B' AS code_value, '배치 요청' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT (SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key='CODE_GROUP' AND code_value='CHANNEL_CODE') x) AS parent_id, 'CHANNEL_CODE' AS code_key, 'APP' AS code_value, '모바일 앱 채널' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT (SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key='CODE_GROUP' AND code_value='CHANNEL_CODE') x) AS parent_id, 'CHANNEL_CODE' AS code_key, 'JUT' AS code_value, 'JUnit/자동 테스트 채널' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT (SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key='CODE_GROUP' AND code_value='RESULT_TYPE') x) AS parent_id, 'RESULT_TYPE' AS code_key, 'W' AS code_value, '경고/부분 성공' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT (SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key='CODE_GROUP' AND code_value='MESSAGE_FORMAT_TYPE') x) AS parent_id, 'MESSAGE_FORMAT_TYPE' AS code_key, 'PARAMETER' AS code_value, 'Named parameter 메시지' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT (SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key='CODE_GROUP' AND code_value='ASYNC_STATUS') x) AS parent_id, 'ASYNC_STATUS' AS code_key, 'FAILED' AS code_value, '비동기 처리 실패' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT (SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key='CODE_GROUP' AND code_value='BATCH_JOB_TYPE') x) AS parent_id, 'BATCH_JOB_TYPE' AS code_key, 'SPRING_BATCH' AS code_value, 'Spring Batch Job' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT (SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key='CODE_GROUP' AND code_value='BATCH_JOB_TYPE') x) AS parent_id, 'BATCH_JOB_TYPE' AS code_key, 'WORKER' AS code_value, '지속 Worker' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT (SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key='CODE_GROUP' AND code_value='BATCH_JOB_TYPE') x) AS parent_id, 'BATCH_JOB_TYPE' AS code_key, 'SCHEDULER' AS code_value, 'Scheduler Job' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
  UNION ALL
  SELECT (SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key='CODE_GROUP' AND code_value='BATCH_JOB_TYPE') x) AS parent_id, 'BATCH_JOB_TYPE' AS code_key, 'CENTER_CUT' AS code_value, 'Center-Cut 대량 처리' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual
) src
ON (tgt.code_key = src.code_key AND tgt.code_value = src.code_value)
WHEN MATCHED THEN UPDATE SET
  tgt.parent_id = src.parent_id,
  tgt.description = src.description,
  tgt.use_yn = 'Y',
  tgt.updated_by = src.updated_by,
  tgt.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (parent_id, code_key, code_value, description, created_by, updated_by)
VALUES (src.parent_id, src.code_key, src.code_value, src.description, src.created_by, src.updated_by);
