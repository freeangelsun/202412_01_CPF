-- CPF generated lifecycle bundle; vendor=oracle
-- Source plan: cpf-tools/config/database-source-plan.json

-- ===== BEGIN 50_framework_seed_data.sql =====
-- AUTO-GENERATED from cpf-tools/db/canonical/seed-model.json
-- vendor=oracle; source=50_framework_seed_data.sql
-- DO NOT EDIT generated seed directly.

-- CPF_LOGICAL_DATABASE=cpfDB
MERGE INTO cpf_channel_registry tgt USING (
SELECT 'ANY' channel_code, '전체 채널' channel_name, 'SYSTEM' channel_type, 'INTERNAL' trust_level, 'N' client_channel_yn, 'Y' internal_channel_yn, 'N' authentication_required_yn, 'N' signature_required_yn, 'Y' active_yn, '정책 와일드카드 전용 채널' description, 0 policy_version, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'WEB' channel_code, '웹' channel_name, 'CLIENT' channel_type, 'EXTERNAL' trust_level, 'Y' client_channel_yn, 'N' internal_channel_yn, 'Y' authentication_required_yn, 'N' signature_required_yn, 'Y' active_yn, '웹 브라우저 채널' description, 0 policy_version, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'MOBILE' channel_code, '모바일' channel_name, 'CLIENT' channel_type, 'EXTERNAL' trust_level, 'Y' client_channel_yn, 'N' internal_channel_yn, 'Y' authentication_required_yn, 'N' signature_required_yn, 'Y' active_yn, '모바일 애플리케이션 채널' description, 0 policy_version, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'ADM' channel_code, '관리자' channel_name, 'OPERATOR' channel_type, 'INTERNAL' trust_level, 'Y' client_channel_yn, 'Y' internal_channel_yn, 'Y' authentication_required_yn, 'N' signature_required_yn, 'Y' active_yn, 'ADM 운영 채널' description, 0 policy_version, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'BATCH' channel_code, '배치' channel_name, 'SYSTEM' channel_type, 'INTERNAL' trust_level, 'N' client_channel_yn, 'Y' internal_channel_yn, 'N' authentication_required_yn, 'N' signature_required_yn, 'Y' active_yn, '배치 실행 채널' description, 0 policy_version, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
) src ON (tgt.channel_code = src.channel_code)
WHEN MATCHED THEN UPDATE SET tgt.channel_name = src.channel_name, tgt.channel_type = src.channel_type, tgt.trust_level = src.trust_level, tgt.client_channel_yn = src.client_channel_yn, tgt.internal_channel_yn = src.internal_channel_yn, tgt.authentication_required_yn = src.authentication_required_yn, tgt.signature_required_yn = src.signature_required_yn, tgt.active_yn = src.active_yn, tgt.description = src.description, tgt.updated_by = src.updated_by, tgt.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (channel_code, channel_name, channel_type, trust_level, client_channel_yn, internal_channel_yn, authentication_required_yn, signature_required_yn, active_yn, description, policy_version, created_by, updated_by) VALUES (src.channel_code, src.channel_name, src.channel_type, src.trust_level, src.client_channel_yn, src.internal_channel_yn, src.authentication_required_yn, src.signature_required_yn, src.active_yn, src.description, src.policy_version, src.created_by, src.updated_by);
MERGE INTO cpf_channel_execution_policy tgt USING (
SELECT 'CPF.DEFAULT' policy_key, '*' standard_execution_id, 'ANY' original_channel_code, 'ANY' caller_channel_code, '*' request_type, 'Y' allowed_yn, 'N' authentication_required_yn, 'N' signature_required_yn, 0 max_tps, NULL effective_from, NULL effective_to, 'Y' active_yn, 0 policy_version, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
) src ON (tgt.policy_key = src.policy_key)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id = src.standard_execution_id, tgt.original_channel_code = src.original_channel_code, tgt.caller_channel_code = src.caller_channel_code, tgt.request_type = src.request_type, tgt.allowed_yn = src.allowed_yn, tgt.authentication_required_yn = src.authentication_required_yn, tgt.signature_required_yn = src.signature_required_yn, tgt.max_tps = src.max_tps, tgt.active_yn = src.active_yn, tgt.updated_by = src.updated_by, tgt.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (policy_key, standard_execution_id, original_channel_code, caller_channel_code, request_type, allowed_yn, authentication_required_yn, signature_required_yn, max_tps, effective_from, effective_to, active_yn, policy_version, created_by, updated_by) VALUES (src.policy_key, src.standard_execution_id, src.original_channel_code, src.caller_channel_code, src.request_type, src.allowed_yn, src.authentication_required_yn, src.signature_required_yn, src.max_tps, src.effective_from, src.effective_to, src.active_yn, src.policy_version, src.created_by, src.updated_by);
MERGE INTO cpf_code tgt USING (
SELECT NULL parent_id, 'CODE_GROUP' code_key, 'MODULE' code_value, '서비스 모듈 코드 그룹' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT NULL parent_id, 'CODE_GROUP' code_key, 'REQUEST_TYPE' code_value, '요청 유형 코드 그룹' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT NULL parent_id, 'CODE_GROUP' code_key, 'CHANNEL_CODE' code_value, '채널 코드 그룹' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT NULL parent_id, 'CODE_GROUP' code_key, 'RESULT_TYPE' code_value, '응답 결과 유형 코드 그룹' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT NULL parent_id, 'CODE_GROUP' code_key, 'MESSAGE_FORMAT_TYPE' code_value, '메시지 포맷 유형 코드 그룹' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT NULL parent_id, 'CODE_GROUP' code_key, 'LOG_LEVEL' code_value, '동적 로그 레벨 코드 그룹' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT NULL parent_id, 'CODE_GROUP' code_key, 'CACHE_NAME' code_value, '캐시 이름 코드 그룹' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT NULL parent_id, 'CODE_GROUP' code_key, 'BATCH_JOB_TYPE' code_value, '배치 Job 유형 코드 그룹' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT NULL parent_id, 'CODE_GROUP' code_key, 'YN' code_value, '여부 코드 그룹' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
) src ON (tgt.code_key = src.code_key AND tgt.code_value = src.code_value)
WHEN MATCHED THEN UPDATE SET tgt.description = src.description, tgt.updated_by = src.updated_by, tgt.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (parent_id, code_key, code_value, description, created_by, updated_by) VALUES (src.parent_id, src.code_key, src.code_value, src.description, src.created_by, src.updated_by);
MERGE INTO cpf_code tgt USING (
SELECT (SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key = 'CODE_GROUP' AND code_value = 'MODULE') p) parent_id, 'MODULE' code_key, 'CPF' code_value, '프레임워크 공통 엔진' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT (SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key = 'CODE_GROUP' AND code_value = 'MODULE') p) parent_id, 'MODULE' code_key, 'CMN' code_value, '업무 공통 라이브러리' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT (SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key = 'CODE_GROUP' AND code_value = 'MODULE') p) parent_id, 'MODULE' code_key, 'ADM' code_value, '관리자 운영 서비스' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT (SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key = 'CODE_GROUP' AND code_value = 'MODULE') p) parent_id, 'MODULE' code_key, 'BZA' code_value, '업무 백오피스 서비스' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT (SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key = 'CODE_GROUP' AND code_value = 'MODULE') p) parent_id, 'MODULE' code_key, 'BAT' code_value, '선택 배치 실행 서비스' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT (SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key = 'CODE_GROUP' AND code_value = 'MODULE') p) parent_id, 'MODULE' code_key, 'REF' code_value, '교육 샘플 서비스' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT (SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key = 'CODE_GROUP' AND code_value = 'REQUEST_TYPE') p) parent_id, 'REQUEST_TYPE' code_key, 'NORMAL' code_value, '일반 요청' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT (SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key = 'CODE_GROUP' AND code_value = 'REQUEST_TYPE') p) parent_id, 'REQUEST_TYPE' code_key, 'COMPENSATION' code_value, '보상 요청' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT (SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key = 'CODE_GROUP' AND code_value = 'REQUEST_TYPE') p) parent_id, 'REQUEST_TYPE' code_key, 'RETRY' code_value, '재시도 요청' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT (SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key = 'CODE_GROUP' AND code_value = 'CHANNEL_CODE') p) parent_id, 'CHANNEL_CODE' code_key, 'WEB' code_value, '웹 채널' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT (SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key = 'CODE_GROUP' AND code_value = 'CHANNEL_CODE') p) parent_id, 'CHANNEL_CODE' code_key, 'MOBILE' code_value, '모바일 채널' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT (SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key = 'CODE_GROUP' AND code_value = 'CHANNEL_CODE') p) parent_id, 'CHANNEL_CODE' code_key, 'BATCH' code_value, '배치 채널' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT (SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key = 'CODE_GROUP' AND code_value = 'CHANNEL_CODE') p) parent_id, 'CHANNEL_CODE' code_key, 'ADM' code_value, '관리자 채널' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT (SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key = 'CODE_GROUP' AND code_value = 'RESULT_TYPE') p) parent_id, 'RESULT_TYPE' code_key, 'S' code_value, '성공' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT (SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key = 'CODE_GROUP' AND code_value = 'RESULT_TYPE') p) parent_id, 'RESULT_TYPE' code_key, 'E' code_value, '오류' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT (SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key = 'CODE_GROUP' AND code_value = 'MESSAGE_FORMAT_TYPE') p) parent_id, 'MESSAGE_FORMAT_TYPE' code_key, 'FIXED' code_value, '고정 메시지' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT (SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key = 'CODE_GROUP' AND code_value = 'MESSAGE_FORMAT_TYPE') p) parent_id, 'MESSAGE_FORMAT_TYPE' code_key, 'INDEXED' code_value, '인덱스 파라미터 메시지' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT (SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key = 'CODE_GROUP' AND code_value = 'LOG_LEVEL') p) parent_id, 'LOG_LEVEL' code_key, 'TRACE' code_value, 'TRACE 로그' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT (SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key = 'CODE_GROUP' AND code_value = 'LOG_LEVEL') p) parent_id, 'LOG_LEVEL' code_key, 'DEBUG' code_value, 'DEBUG 로그' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT (SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key = 'CODE_GROUP' AND code_value = 'LOG_LEVEL') p) parent_id, 'LOG_LEVEL' code_key, 'INFO' code_value, 'INFO 로그' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT (SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key = 'CODE_GROUP' AND code_value = 'LOG_LEVEL') p) parent_id, 'LOG_LEVEL' code_key, 'WARN' code_value, 'WARN 로그' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT (SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key = 'CODE_GROUP' AND code_value = 'LOG_LEVEL') p) parent_id, 'LOG_LEVEL' code_key, 'ERROR' code_value, 'ERROR 로그' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT (SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key = 'CODE_GROUP' AND code_value = 'CACHE_NAME') p) parent_id, 'CACHE_NAME' code_key, 'ALL' code_value, '전체 캐시' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT (SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key = 'CODE_GROUP' AND code_value = 'CACHE_NAME') p) parent_id, 'CACHE_NAME' code_key, 'CODE' code_value, '코드 캐시' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT (SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key = 'CODE_GROUP' AND code_value = 'CACHE_NAME') p) parent_id, 'CACHE_NAME' code_key, 'MESSAGE' code_value, '메시지 캐시' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT (SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key = 'CODE_GROUP' AND code_value = 'CACHE_NAME') p) parent_id, 'CACHE_NAME' code_key, 'RESPONSE_CODE' code_value, '응답코드 캐시' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT (SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key = 'CODE_GROUP' AND code_value = 'CACHE_NAME') p) parent_id, 'CACHE_NAME' code_key, 'CONFIG' code_value, '설정 캐시' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT (SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key = 'CODE_GROUP' AND code_value = 'BATCH_JOB_TYPE') p) parent_id, 'BATCH_JOB_TYPE' code_key, 'TASKLET' code_value, 'Tasklet 배치' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT (SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key = 'CODE_GROUP' AND code_value = 'BATCH_JOB_TYPE') p) parent_id, 'BATCH_JOB_TYPE' code_key, 'CHUNK' code_value, 'Chunk 배치' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT (SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key = 'CODE_GROUP' AND code_value = 'BATCH_JOB_TYPE') p) parent_id, 'BATCH_JOB_TYPE' code_key, 'RETRY' code_value, '재처리 배치' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT (SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key = 'CODE_GROUP' AND code_value = 'YN') p) parent_id, 'YN' code_key, 'Y' code_value, '예' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT (SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key = 'CODE_GROUP' AND code_value = 'YN') p) parent_id, 'YN' code_key, 'N' code_value, '아니오' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
) src ON (tgt.code_key = src.code_key AND tgt.code_value = src.code_value)
WHEN MATCHED THEN UPDATE SET tgt.parent_id = src.parent_id, tgt.description = src.description, tgt.updated_by = src.updated_by, tgt.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (parent_id, code_key, code_value, description, created_by, updated_by) VALUES (src.parent_id, src.code_key, src.code_value, src.description, src.created_by, src.updated_by);
MERGE INTO cpf_message tgt USING (
SELECT 'MCPF000000' message_code, 'ko' locale, 'FIXED' message_format_type, '정상 처리되었습니다.' external_message, 'CPF 공통 요청이 정상 처리되었습니다.' internal_message, 0 parameter_count, NULL parameter_sample, 'CPF 공통 성공 메시지' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'MCPF010001' message_code, 'ko' locale, 'INDEXED' message_format_type, '요청 값이 올바르지 않습니다.' external_message, '요청 파라미터 검증에 실패했습니다. field={0}, value={1}' internal_message, 2 parameter_count, '["field","invalid"]' parameter_sample, 'CPF 파라미터 오류 메시지' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'MCPF010002' message_code, 'ko' locale, 'INDEXED' message_format_type, '요청한 정보를 찾을 수 없습니다.' external_message, '조회 대상 데이터가 존재하지 않습니다. target={0}' internal_message, 1 parameter_count, '["sample-item"]' parameter_sample, 'CPF 미존재 메시지' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'MCPF010003' message_code, 'ko' locale, 'INDEXED' message_format_type, '이미 등록된 정보입니다.' external_message, '중복 데이터가 감지되었습니다. key={0}' internal_message, 1 parameter_count, '["sampleKey"]' parameter_sample, 'CPF 중복 메시지' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'MCPF010004' message_code, 'ko' locale, 'INDEXED' message_format_type, '입력값을 확인해 주세요.' external_message, 'Bean Validation 검증에 실패했습니다. field={0}' internal_message, 1 parameter_count, '["name"]' parameter_sample, 'CPF 검증 메시지' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'MCPF010005' message_code, 'ko' locale, 'FIXED' message_format_type, '인증이 필요합니다.' external_message, '인증되지 않은 요청입니다.' internal_message, 0 parameter_count, NULL parameter_sample, 'CPF 인증 필요 메시지' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'MCPF010006' message_code, 'ko' locale, 'INDEXED' message_format_type, '처리 권한이 없습니다.' external_message, '인가되지 않은 요청입니다. user={0}' internal_message, 1 parameter_count, '["guest"]' parameter_sample, 'CPF 권한 오류 메시지' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'MCPF020001' message_code, 'ko' locale, 'INDEXED' message_format_type, '요청을 처리할 수 없습니다.' external_message, '업무 규칙 위반이 발생했습니다. rule={0}' internal_message, 1 parameter_count, '["business-rule"]' parameter_sample, 'CPF 업무 규칙 메시지' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'MCPF030001' message_code, 'ko' locale, 'INDEXED' message_format_type, '일시적으로 처리할 수 없습니다.' external_message, '외부 또는 타 주제영역 연계 오류가 발생했습니다. service={0}' internal_message, 1 parameter_count, '["generated-service"]' parameter_sample, 'CPF 외부 연계 메시지' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'MCPF900001' message_code, 'ko' locale, 'INDEXED' message_format_type, '필수 거래 헤더가 누락되었습니다.' external_message, 'CPF 거래 헤더 검증에 실패했습니다. header={0}, uri={1}' internal_message, 2 parameter_count, '["X-Request-Type","/api/sample-items"]' parameter_sample, 'CPF 헤더 검증 메시지' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'MCPF900002' message_code, 'ko' locale, 'INDEXED' message_format_type, '거래 메타데이터 설정이 올바르지 않습니다.' external_message, 'CPF &&CpfTransaction 메타데이터 검증에 실패했습니다. transactionId={0}' internal_message, 1 parameter_count, '["OCPFSM0001"]' parameter_sample, 'CPF 메타데이터 메시지' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'MCPF900003' message_code, 'ko' locale, 'INDEXED' message_format_type, '서비스 접속 정보가 없습니다.' external_message, 'CPF 서비스 endpoint 설정을 찾을 수 없습니다. serviceId={0}' internal_message, 1 parameter_count, '["generated-service"]' parameter_sample, 'CPF endpoint 메시지' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'MCPF900004' message_code, 'ko' locale, 'INDEXED' message_format_type, '동적 로그레벨 요청이 올바르지 않습니다.' external_message, 'CPF 동적 로그레벨 규칙 검증에 실패했습니다. reason={0}' internal_message, 1 parameter_count, '["transactionId or businessTransactionId required"]' parameter_sample, 'CPF 동적 로그 메시지' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'MCPF900005' message_code, 'ko' locale, 'INDEXED' message_format_type, '내부 공유 API에 접근할 수 없습니다.' external_message, 'CPF 내부 서비스 신원 또는 호출 경로 검증에 실패했습니다. reason={0}' internal_message, 1 parameter_count, '["service identity verification failed"]' parameter_sample, 'CPF 내부 공유 API 접근 거부 메시지' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'MCPF990000' message_code, 'ko' locale, 'INDEXED' message_format_type, '처리 중 오류가 발생했습니다.' external_message, 'CPF 내부 오류가 발생했습니다. error={0}' internal_message, 1 parameter_count, '["Exception"]' parameter_sample, 'CPF 내부 오류 메시지' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'MCPF990001' message_code, 'ko' locale, 'INDEXED' message_format_type, '데이터베이스 오류가 발생했습니다.' external_message, '데이터베이스 처리 오류가 발생했습니다. sqlState={0}' internal_message, 1 parameter_count, '["HY000"]' parameter_sample, 'CPF 데이터베이스 오류 메시지' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'MBZA000000' message_code, 'ko' locale, 'FIXED' message_format_type, '성공' external_message, 'BZA 요청이 정상 처리되었습니다.' internal_message, 0 parameter_count, NULL parameter_sample, 'BZA 성공 메시지' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'MBZA010001' message_code, 'ko' locale, 'INDEXED' message_format_type, '업무 요청 값이 올바르지 않습니다.' external_message, 'BZA 입력값 검증에 실패했습니다. field={0}' internal_message, 1 parameter_count, '["field"]' parameter_sample, 'BZA 입력값 오류 메시지' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'MBZA010002' message_code, 'ko' locale, 'FIXED' message_format_type, '처리 권한이 없습니다.' external_message, 'BZA 서버 권한 검사에 실패했습니다.' internal_message, 0 parameter_count, NULL parameter_sample, 'BZA 권한 오류 메시지' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'MREF090001' message_code, 'ko' locale, 'INDEXED' message_format_type, '이미 등록된 {0}입니다.' external_message, '{0}={1} 값이 이미 존재합니다. duplicateCheck=REF_EDU_SAMPLE' internal_message, 2 parameter_count, '["샘플키","SAMPLE-0001"]' parameter_sample, 'REF 동적 중복 교육 메시지' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'MCMN000001' message_code, 'ko' locale, 'FIXED' message_format_type, 'CPF 교육 시스템에 오신 것을 환영합니다.' external_message, 'CMN education welcome message.' internal_message, 0 parameter_count, NULL parameter_sample, 'CMN 교육 환영 메시지' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'MCMN000001' message_code, 'en' locale, 'FIXED' message_format_type, 'Welcome to the CPF education system.' external_message, 'CMN education welcome message.' internal_message, 0 parameter_count, NULL parameter_sample, 'CMN 교육 환영 메시지' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
) src ON (tgt.message_code = src.message_code AND tgt.locale = src.locale)
WHEN MATCHED THEN UPDATE SET tgt.message_format_type = src.message_format_type, tgt.external_message = src.external_message, tgt.internal_message = src.internal_message, tgt.parameter_count = src.parameter_count, tgt.parameter_sample = src.parameter_sample, tgt.description = src.description, tgt.use_yn = 'Y', tgt.updated_by = src.updated_by, tgt.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (message_code, locale, message_format_type, external_message, internal_message, parameter_count, parameter_sample, description, created_by, updated_by) VALUES (src.message_code, src.locale, src.message_format_type, src.external_message, src.internal_message, src.parameter_count, src.parameter_sample, src.description, src.created_by, src.updated_by);
MERGE INTO cpf_response_code tgt USING (
SELECT 'SCPF000000' response_code, 'MCPF000000' message_code, 'S' result_type, 'CPF' module_id, '00' response_group, '0000' sequence_no, 200 http_status, 'CPF 공통 성공' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'ECPF010001' response_code, 'MCPF010001' message_code, 'E' result_type, 'CPF' module_id, '01' response_group, '0001' sequence_no, 400 http_status, '파라미터 오류' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'ECPF010002' response_code, 'MCPF010002' message_code, 'E' result_type, 'CPF' module_id, '01' response_group, '0002' sequence_no, 404 http_status, '미존재 오류' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'ECPF010003' response_code, 'MCPF010003' message_code, 'E' result_type, 'CPF' module_id, '01' response_group, '0003' sequence_no, 409 http_status, '중복 오류' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'ECPF010004' response_code, 'MCPF010004' message_code, 'E' result_type, 'CPF' module_id, '01' response_group, '0004' sequence_no, 400 http_status, '검증 실패' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'ECPF010005' response_code, 'MCPF010005' message_code, 'E' result_type, 'CPF' module_id, '01' response_group, '0005' sequence_no, 401 http_status, '인증 필요' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'ECPF010006' response_code, 'MCPF010006' message_code, 'E' result_type, 'CPF' module_id, '01' response_group, '0006' sequence_no, 403 http_status, '권한 없음' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'ECPF020001' response_code, 'MCPF020001' message_code, 'E' result_type, 'CPF' module_id, '02' response_group, '0001' sequence_no, 400 http_status, '업무 규칙 위반' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'ECPF030001' response_code, 'MCPF030001' message_code, 'E' result_type, 'CPF' module_id, '03' response_group, '0001' sequence_no, 502 http_status, '외부 연계 오류' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'ECPF900001' response_code, 'MCPF900001' message_code, 'E' result_type, 'CPF' module_id, '90' response_group, '0001' sequence_no, 400 http_status, '필수 거래 헤더 누락' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'ECPF900002' response_code, 'MCPF900002' message_code, 'E' result_type, 'CPF' module_id, '90' response_group, '0002' sequence_no, 500 http_status, '거래 메타데이터 오류' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'ECPF900003' response_code, 'MCPF900003' message_code, 'E' result_type, 'CPF' module_id, '90' response_group, '0003' sequence_no, 500 http_status, '서비스 endpoint 미등록' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'ECPF900004' response_code, 'MCPF900004' message_code, 'E' result_type, 'CPF' module_id, '90' response_group, '0004' sequence_no, 400 http_status, '동적 로그 규칙 오류' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'ECPF900005' response_code, 'MCPF900005' message_code, 'E' result_type, 'CPF' module_id, '90' response_group, '0005' sequence_no, 403 http_status, '내부 공유 API 접근 거부' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'ECPF990000' response_code, 'MCPF990000' message_code, 'E' result_type, 'CPF' module_id, '99' response_group, '0000' sequence_no, 500 http_status, '내부 서버 오류' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'ECPF990001' response_code, 'MCPF990001' message_code, 'E' result_type, 'CPF' module_id, '99' response_group, '0001' sequence_no, 500 http_status, '데이터베이스 오류' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'SBZA000000' response_code, 'MBZA000000' message_code, 'S' result_type, 'BZA' module_id, '00' response_group, '0000' sequence_no, 200 http_status, 'BZA 성공' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'EBZA010001' response_code, 'MBZA010001' message_code, 'E' result_type, 'BZA' module_id, '01' response_group, '0001' sequence_no, 400 http_status, 'BZA 입력값 오류' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'EBZA010002' response_code, 'MBZA010002' message_code, 'E' result_type, 'BZA' module_id, '01' response_group, '0002' sequence_no, 403 http_status, 'BZA 권한 오류' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
) src ON (tgt.response_code = src.response_code)
WHEN MATCHED THEN UPDATE SET tgt.message_code = src.message_code, tgt.result_type = src.result_type, tgt.module_id = src.module_id, tgt.response_group = src.response_group, tgt.sequence_no = src.sequence_no, tgt.http_status = src.http_status, tgt.description = src.description, tgt.use_yn = 'Y', tgt.updated_by = src.updated_by, tgt.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (response_code, message_code, result_type, module_id, response_group, sequence_no, http_status, description, created_by, updated_by) VALUES (src.response_code, src.message_code, src.result_type, src.module_id, src.response_group, src.sequence_no, src.http_status, src.description, src.created_by, src.updated_by);
MERGE INTO cpf_config tgt USING (
SELECT 'CPF.CMN.CACHE.PRELOAD_ENABLED' config_key, 'Y' config_value, 'BOOLEAN' config_type, 'CMN 캐시 기동 시 선적재 여부' description, 'N' encrypted_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'CPF.CMN.CACHE.FAIL_FAST_ON_STARTUP' config_key, 'N' config_value, 'BOOLEAN' config_type, '캐시 선적재 실패 시 기동 실패 여부' description, 'N' encrypted_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'CPF.CMN.CACHE.REFRESH_POLL_MILLIS' config_key, '5000' config_value, 'NUMBER' config_type, '캐시 갱신 이벤트 polling 주기' description, 'N' encrypted_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'CPF.CMN.MESSAGING.BROKER' config_key, 'IN_MEMORY' config_value, 'STRING' config_type, '기본 CMN 메시지 브로커 유형' description, 'N' encrypted_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'CPF.HTTP.CONNECT_TIMEOUT_MS' config_key, '3000' config_value, 'NUMBER' config_type, 'CPF HTTP client 연결 timeout' description, 'N' encrypted_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'CPF.HTTP.READ_TIMEOUT_MS' config_key, '5000' config_value, 'NUMBER' config_type, 'CPF HTTP client 읽기 timeout' description, 'N' encrypted_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'CPF.ADM.SESSION_TTL_SECONDS' config_key, '3600' config_value, 'NUMBER' config_type, 'ADM 세션 TTL 초' description, 'N' encrypted_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'CPF.ADM.PASSWORD_EXPIRE_DAYS' config_key, '90' config_value, 'NUMBER' config_type, 'ADM 비밀번호 만료 일수' description, 'N' encrypted_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'CPF.ADM.PASSWORD_MIN_LENGTH' config_key, '10' config_value, 'NUMBER' config_type, 'ADM 비밀번호 최소 길이' description, 'N' encrypted_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'CPF.ADM.PASSWORD_MAX_FAIL_COUNT' config_key, '5' config_value, 'NUMBER' config_type, 'ADM 로그인 실패 잠금 기준' description, 'N' encrypted_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'CPF.BATCH.DEFAULT_LOCK_SECONDS' config_key, '3600' config_value, 'NUMBER' config_type, '배치 기본 lock 만료 초' description, 'N' encrypted_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'CPF.FEATURE.SAMPLE_ENABLED' config_key, 'Y' config_value, 'BOOLEAN' config_type, '샘플 API와 교육 flow 활성화 여부' description, 'N' encrypted_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
) src ON (tgt.config_key = src.config_key)
WHEN MATCHED THEN UPDATE SET tgt.config_value = src.config_value, tgt.config_type = src.config_type, tgt.description = src.description, tgt.encrypted_yn = src.encrypted_yn, tgt.use_yn = 'Y', tgt.updated_by = src.updated_by, tgt.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (config_key, config_value, config_type, description, encrypted_yn, created_by, updated_by) VALUES (src.config_key, src.config_value, src.config_type, src.description, src.encrypted_yn, src.created_by, src.updated_by);
MERGE INTO cpf_code tgt USING (
SELECT NULL parent_id, 'CODE_GROUP' code_key, 'HTTP_METHOD' code_value, 'HTTP Method 코드 그룹' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT NULL parent_id, 'CODE_GROUP' code_key, 'EXECUTION_STATUS' code_value, '실행 상태 코드 그룹' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT NULL parent_id, 'CODE_GROUP' code_key, 'ASYNC_STATUS' code_value, '비동기 처리 상태 코드 그룹' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT NULL parent_id, 'CODE_GROUP' code_key, 'RETRY_STATUS' code_value, '재시도 상태 코드 그룹' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT NULL parent_id, 'CODE_GROUP' code_key, 'IDEMPOTENCY_STATUS' code_value, '멱등 처리 상태 코드 그룹' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT NULL parent_id, 'CODE_GROUP' code_key, 'HEALTH_STATUS' code_value, 'Health 상태 코드 그룹' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT NULL parent_id, 'CODE_GROUP' code_key, 'CIRCUIT_STATUS' code_value, 'Circuit Breaker 상태 코드 그룹' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT NULL parent_id, 'CODE_GROUP' code_key, 'FILE_SCAN_STATUS' code_value, '첨부/파일 검사 상태 코드 그룹' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT NULL parent_id, 'CODE_GROUP' code_key, 'DATA_CLASSIFICATION' code_value, '데이터 민감도 등급 코드 그룹' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT NULL parent_id, 'CODE_GROUP' code_key, 'APPROVAL_STATUS' code_value, '결재 상태 코드 그룹' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT NULL parent_id, 'CODE_GROUP' code_key, 'ERROR_CATEGORY' code_value, '오류 분류 코드 그룹' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT NULL parent_id, 'CODE_GROUP' code_key, 'RETENTION_ACTION' code_value, '보존 정책 실행 유형 코드 그룹' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
) src ON (tgt.code_key = src.code_key AND tgt.code_value = src.code_value)
WHEN MATCHED THEN UPDATE SET tgt.description = src.description, tgt.updated_by = src.updated_by, tgt.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (parent_id, code_key, code_value, description, created_by, updated_by) VALUES (src.parent_id, src.code_key, src.code_value, src.description, src.created_by, src.updated_by);
MERGE INTO cpf_code tgt USING (
SELECT (SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key='CODE_GROUP' AND code_value='HTTP_METHOD') x) parent_id, 'HTTP_METHOD' code_key, 'GET' code_value, '조회' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT (SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key='CODE_GROUP' AND code_value='HTTP_METHOD') x) parent_id, 'HTTP_METHOD' code_key, 'POST' code_value, '등록/명령' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT (SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key='CODE_GROUP' AND code_value='HTTP_METHOD') x) parent_id, 'HTTP_METHOD' code_key, 'PUT' code_value, '전체 수정' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT (SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key='CODE_GROUP' AND code_value='HTTP_METHOD') x) parent_id, 'HTTP_METHOD' code_key, 'PATCH' code_value, '부분 수정' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT (SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key='CODE_GROUP' AND code_value='HTTP_METHOD') x) parent_id, 'HTTP_METHOD' code_key, 'DELETE' code_value, '삭제/회수' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT (SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key='CODE_GROUP' AND code_value='EXECUTION_STATUS') x) parent_id, 'EXECUTION_STATUS' code_key, 'READY' code_value, '실행 준비' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT (SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key='CODE_GROUP' AND code_value='EXECUTION_STATUS') x) parent_id, 'EXECUTION_STATUS' code_key, 'RUNNING' code_value, '실행 중' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT (SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key='CODE_GROUP' AND code_value='EXECUTION_STATUS') x) parent_id, 'EXECUTION_STATUS' code_key, 'SUCCESS' code_value, '정상 완료' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT (SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key='CODE_GROUP' AND code_value='EXECUTION_STATUS') x) parent_id, 'EXECUTION_STATUS' code_key, 'FAILED' code_value, '실패' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT (SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key='CODE_GROUP' AND code_value='EXECUTION_STATUS') x) parent_id, 'EXECUTION_STATUS' code_key, 'UNKNOWN_RESULT' code_value, '결과 미확정' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT (SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key='CODE_GROUP' AND code_value='ASYNC_STATUS') x) parent_id, 'ASYNC_STATUS' code_key, 'WAITING' code_value, '비동기 대기' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT (SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key='CODE_GROUP' AND code_value='ASYNC_STATUS') x) parent_id, 'ASYNC_STATUS' code_key, 'PROCESSING' code_value, '비동기 처리 중' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT (SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key='CODE_GROUP' AND code_value='ASYNC_STATUS') x) parent_id, 'ASYNC_STATUS' code_key, 'COMPLETED' code_value, '비동기 완료' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT (SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key='CODE_GROUP' AND code_value='ASYNC_STATUS') x) parent_id, 'ASYNC_STATUS' code_key, 'DLQ' code_value, 'Dead Letter Queue' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT (SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key='CODE_GROUP' AND code_value='RETRY_STATUS') x) parent_id, 'RETRY_STATUS' code_key, 'RETRYABLE' code_value, '재시도 가능' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT (SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key='CODE_GROUP' AND code_value='RETRY_STATUS') x) parent_id, 'RETRY_STATUS' code_key, 'NON_RETRYABLE' code_value, '재시도 금지' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT (SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key='CODE_GROUP' AND code_value='RETRY_STATUS') x) parent_id, 'RETRY_STATUS' code_key, 'EXHAUSTED' code_value, '재시도 소진' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT (SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key='CODE_GROUP' AND code_value='IDEMPOTENCY_STATUS') x) parent_id, 'IDEMPOTENCY_STATUS' code_key, 'PROCESSING' code_value, '멱등 처리 중' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT (SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key='CODE_GROUP' AND code_value='IDEMPOTENCY_STATUS') x) parent_id, 'IDEMPOTENCY_STATUS' code_key, 'COMPLETED' code_value, '멱등 처리 완료' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT (SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key='CODE_GROUP' AND code_value='IDEMPOTENCY_STATUS') x) parent_id, 'IDEMPOTENCY_STATUS' code_key, 'FAILED' code_value, '멱등 처리 실패' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT (SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key='CODE_GROUP' AND code_value='IDEMPOTENCY_STATUS') x) parent_id, 'IDEMPOTENCY_STATUS' code_key, 'UNKNOWN_RESULT' code_value, '멱등 결과 미확정' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT (SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key='CODE_GROUP' AND code_value='HEALTH_STATUS') x) parent_id, 'HEALTH_STATUS' code_key, 'UP' code_value, '정상' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT (SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key='CODE_GROUP' AND code_value='HEALTH_STATUS') x) parent_id, 'HEALTH_STATUS' code_key, 'DOWN' code_value, '장애' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT (SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key='CODE_GROUP' AND code_value='HEALTH_STATUS') x) parent_id, 'HEALTH_STATUS' code_key, 'DEGRADED' code_value, '부분 저하' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT (SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key='CODE_GROUP' AND code_value='CIRCUIT_STATUS') x) parent_id, 'CIRCUIT_STATUS' code_key, 'CLOSED' code_value, '정상 호출' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT (SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key='CODE_GROUP' AND code_value='CIRCUIT_STATUS') x) parent_id, 'CIRCUIT_STATUS' code_key, 'OPEN' code_value, '호출 차단' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT (SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key='CODE_GROUP' AND code_value='CIRCUIT_STATUS') x) parent_id, 'CIRCUIT_STATUS' code_key, 'HALF_OPEN' code_value, '복구 시험' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT (SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key='CODE_GROUP' AND code_value='FILE_SCAN_STATUS') x) parent_id, 'FILE_SCAN_STATUS' code_key, 'PENDING' code_value, '검사 대기' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT (SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key='CODE_GROUP' AND code_value='FILE_SCAN_STATUS') x) parent_id, 'FILE_SCAN_STATUS' code_key, 'CLEAN' code_value, '검사 정상' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT (SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key='CODE_GROUP' AND code_value='FILE_SCAN_STATUS') x) parent_id, 'FILE_SCAN_STATUS' code_key, 'INFECTED' code_value, '악성 탐지' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT (SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key='CODE_GROUP' AND code_value='FILE_SCAN_STATUS') x) parent_id, 'FILE_SCAN_STATUS' code_key, 'FAILED' code_value, '검사 실패' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT (SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key='CODE_GROUP' AND code_value='FILE_SCAN_STATUS') x) parent_id, 'FILE_SCAN_STATUS' code_key, 'QUARANTINED' code_value, '격리' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT (SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key='CODE_GROUP' AND code_value='DATA_CLASSIFICATION') x) parent_id, 'DATA_CLASSIFICATION' code_key, 'PUBLIC' code_value, '공개 정보' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT (SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key='CODE_GROUP' AND code_value='DATA_CLASSIFICATION') x) parent_id, 'DATA_CLASSIFICATION' code_key, 'INTERNAL' code_value, '내부 정보' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT (SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key='CODE_GROUP' AND code_value='DATA_CLASSIFICATION') x) parent_id, 'DATA_CLASSIFICATION' code_key, 'CONFIDENTIAL' code_value, '기밀 정보' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT (SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key='CODE_GROUP' AND code_value='DATA_CLASSIFICATION') x) parent_id, 'DATA_CLASSIFICATION' code_key, 'RESTRICTED' code_value, '제한/민감 정보' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT (SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key='CODE_GROUP' AND code_value='APPROVAL_STATUS') x) parent_id, 'APPROVAL_STATUS' code_key, 'DRAFT' code_value, '작성 중' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT (SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key='CODE_GROUP' AND code_value='APPROVAL_STATUS') x) parent_id, 'APPROVAL_STATUS' code_key, 'IN_REVIEW' code_value, '결재 진행' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT (SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key='CODE_GROUP' AND code_value='APPROVAL_STATUS') x) parent_id, 'APPROVAL_STATUS' code_key, 'APPROVED' code_value, '승인 완료' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT (SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key='CODE_GROUP' AND code_value='APPROVAL_STATUS') x) parent_id, 'APPROVAL_STATUS' code_key, 'REJECTED' code_value, '반려' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT (SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key='CODE_GROUP' AND code_value='APPROVAL_STATUS') x) parent_id, 'APPROVAL_STATUS' code_key, 'WITHDRAWN' code_value, '철회' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT (SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key='CODE_GROUP' AND code_value='APPROVAL_STATUS') x) parent_id, 'APPROVAL_STATUS' code_key, 'CANCELED' code_value, '취소' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT (SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key='CODE_GROUP' AND code_value='APPROVAL_STATUS') x) parent_id, 'APPROVAL_STATUS' code_key, 'EXPIRED' code_value, '만료' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT (SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key='CODE_GROUP' AND code_value='ERROR_CATEGORY') x) parent_id, 'ERROR_CATEGORY' code_key, 'VALIDATION' code_value, '입력/계약 검증 오류' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT (SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key='CODE_GROUP' AND code_value='ERROR_CATEGORY') x) parent_id, 'ERROR_CATEGORY' code_key, 'AUTHENTICATION' code_value, '인증 오류' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT (SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key='CODE_GROUP' AND code_value='ERROR_CATEGORY') x) parent_id, 'ERROR_CATEGORY' code_key, 'AUTHORIZATION' code_value, '인가 오류' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT (SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key='CODE_GROUP' AND code_value='ERROR_CATEGORY') x) parent_id, 'ERROR_CATEGORY' code_key, 'CONFLICT' code_value, '동시성/중복 오류' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT (SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key='CODE_GROUP' AND code_value='ERROR_CATEGORY') x) parent_id, 'ERROR_CATEGORY' code_key, 'TIMEOUT' code_value, 'Timeout' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT (SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key='CODE_GROUP' AND code_value='ERROR_CATEGORY') x) parent_id, 'ERROR_CATEGORY' code_key, 'TARGET_DOWN' code_value, '호출 대상 장애' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT (SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key='CODE_GROUP' AND code_value='ERROR_CATEGORY') x) parent_id, 'ERROR_CATEGORY' code_key, 'UNKNOWN_RESULT' code_value, '결과 미확정' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT (SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key='CODE_GROUP' AND code_value='RETENTION_ACTION') x) parent_id, 'RETENTION_ACTION' code_key, 'ARCHIVE' code_value, '보관소 이관' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT (SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key='CODE_GROUP' AND code_value='RETENTION_ACTION') x) parent_id, 'RETENTION_ACTION' code_key, 'PURGE' code_value, '정책 삭제' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT (SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key='CODE_GROUP' AND code_value='RETENTION_ACTION') x) parent_id, 'RETENTION_ACTION' code_key, 'LEGAL_HOLD' code_value, '법적 보존' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
) src ON (tgt.code_key = src.code_key AND tgt.code_value = src.code_value)
WHEN MATCHED THEN UPDATE SET tgt.parent_id = src.parent_id, tgt.description = src.description, tgt.updated_by = src.updated_by, tgt.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (parent_id, code_key, code_value, description, created_by, updated_by) VALUES (src.parent_id, src.code_key, src.code_value, src.description, src.created_by, src.updated_by);
MERGE INTO cpf_message tgt USING (
SELECT 'MCPF030002' message_code, 'ko' locale, 'FIXED' message_format_type, '요청 시간이 초과되었습니다.' external_message, '대상 호출 timeout이 발생했습니다.' internal_message, 0 parameter_count, NULL parameter_sample, '공통 Timeout 메시지' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'MCPF030003' message_code, 'ko' locale, 'FIXED' message_format_type, '연결 대상이 일시적으로 사용할 수 없습니다.' external_message, '대상 서비스가 DOWN/OPEN 상태입니다.' internal_message, 0 parameter_count, NULL parameter_sample, 'Target down 메시지' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'MCPF030004' message_code, 'ko' locale, 'FIXED' message_format_type, '처리 결과를 확인 중입니다.' external_message, '요청 결과가 UNKNOWN_RESULT로 분류되어 대사가 필요합니다.' internal_message, 0 parameter_count, NULL parameter_sample, '결과 미확정 메시지' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'MCPF020002' message_code, 'ko' locale, 'FIXED' message_format_type, '다른 사용자가 먼저 변경했습니다. 다시 조회해 주세요.' external_message, '낙관적 잠금 Version 충돌이 발생했습니다.' internal_message, 0 parameter_count, NULL parameter_sample, '동시성 충돌 메시지' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'MCPF020003' message_code, 'ko' locale, 'FIXED' message_format_type, '동일 요청이 이미 처리되었습니다.' external_message, 'Idempotency key가 이미 완료된 요청입니다.' internal_message, 0 parameter_count, NULL parameter_sample, '멱등 중복 메시지' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'MCPF040001' message_code, 'ko' locale, 'FIXED' message_format_type, '첨부파일 검사가 완료되지 않았습니다.' external_message, '첨부 다운로드는 CLEAN 상태에서만 허용됩니다.' internal_message, 0 parameter_count, NULL parameter_sample, '첨부 보안 메시지' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'MCPF040002' message_code, 'ko' locale, 'FIXED' message_format_type, '첨부파일이 보안 정책에 의해 격리되었습니다.' external_message, 'INFECTED/QUARANTINED 파일 접근이 차단되었습니다.' internal_message, 0 parameter_count, NULL parameter_sample, '첨부 격리 메시지' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
) src ON (tgt.message_code = src.message_code AND tgt.locale = src.locale)
WHEN MATCHED THEN UPDATE SET tgt.message_format_type = src.message_format_type, tgt.external_message = src.external_message, tgt.internal_message = src.internal_message, tgt.parameter_count = src.parameter_count, tgt.parameter_sample = src.parameter_sample, tgt.description = src.description, tgt.use_yn = 'Y', tgt.updated_by = src.updated_by, tgt.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (message_code, locale, message_format_type, external_message, internal_message, parameter_count, parameter_sample, description, created_by, updated_by) VALUES (src.message_code, src.locale, src.message_format_type, src.external_message, src.internal_message, src.parameter_count, src.parameter_sample, src.description, src.created_by, src.updated_by);
MERGE INTO cpf_response_code tgt USING (
SELECT 'ECPF030002' response_code, 'MCPF030002' message_code, 'E' result_type, 'CPF' module_id, '03' response_group, '0002' sequence_no, 504 http_status, 'Timeout' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'ECPF030003' response_code, 'MCPF030003' message_code, 'E' result_type, 'CPF' module_id, '03' response_group, '0003' sequence_no, 503 http_status, 'Target down' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'ECPF030004' response_code, 'MCPF030004' message_code, 'E' result_type, 'CPF' module_id, '03' response_group, '0004' sequence_no, 202 http_status, 'UNKNOWN_RESULT' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'ECPF020002' response_code, 'MCPF020002' message_code, 'E' result_type, 'CPF' module_id, '02' response_group, '0002' sequence_no, 409 http_status, 'Optimistic lock conflict' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'ECPF020003' response_code, 'MCPF020003' message_code, 'E' result_type, 'CPF' module_id, '02' response_group, '0003' sequence_no, 409 http_status, 'Idempotency duplicate' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'ECPF040001' response_code, 'MCPF040001' message_code, 'E' result_type, 'CPF' module_id, '04' response_group, '0001' sequence_no, 423 http_status, 'File scan pending' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'ECPF040002' response_code, 'MCPF040002' message_code, 'E' result_type, 'CPF' module_id, '04' response_group, '0002' sequence_no, 403 http_status, 'File quarantined' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
) src ON (tgt.response_code = src.response_code)
WHEN MATCHED THEN UPDATE SET tgt.message_code = src.message_code, tgt.result_type = src.result_type, tgt.module_id = src.module_id, tgt.response_group = src.response_group, tgt.sequence_no = src.sequence_no, tgt.http_status = src.http_status, tgt.description = src.description, tgt.use_yn = 'Y', tgt.updated_by = src.updated_by, tgt.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (response_code, message_code, result_type, module_id, response_group, sequence_no, http_status, description, created_by, updated_by) VALUES (src.response_code, src.message_code, src.result_type, src.module_id, src.response_group, src.sequence_no, src.http_status, src.description, src.created_by, src.updated_by);
MERGE INTO cpf_config tgt USING (
SELECT 'CPF.BZA.SECURITY.MAX_LOGIN_FAIL_COUNT' config_key, '5' config_value, 'NUMBER' config_type, 'BZA 로그인 실패 잠금 기준' description, 'N' encrypted_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'CPF.BZA.SECURITY.ACCESS_TOKEN_TTL_SECONDS' config_key, '600' config_value, 'NUMBER' config_type, 'BZA Access Token TTL' description, 'N' encrypted_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'CPF.BZA.SECURITY.REFRESH_TOKEN_TTL_SECONDS' config_key, '7200' config_value, 'NUMBER' config_type, 'BZA Refresh Token TTL' description, 'N' encrypted_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'CPF.RETENTION.EXECUTE_ENABLED' config_key, 'N' config_value, 'BOOLEAN' config_type, '실제 Archive/Purge 실행 Kill Switch 기본 OFF' description, 'N' encrypted_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'CPF.FILE.DOWNLOAD_REQUIRE_CLEAN' config_key, 'Y' config_value, 'BOOLEAN' config_type, '첨부 다운로드 CLEAN 상태 강제' description, 'N' encrypted_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'CPF.HEALTH.INSTANCE_ID_REQUIRED' config_key, 'Y' config_value, 'BOOLEAN' config_type, '운영 Health 응답 인스턴스 식별자 필수' description, 'N' encrypted_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
) src ON (tgt.config_key = src.config_key)
WHEN MATCHED THEN UPDATE SET tgt.config_value = src.config_value, tgt.config_type = src.config_type, tgt.description = src.description, tgt.encrypted_yn = src.encrypted_yn, tgt.use_yn = 'Y', tgt.updated_by = src.updated_by, tgt.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (config_key, config_value, config_type, description, encrypted_yn, created_by, updated_by) VALUES (src.config_key, src.config_value, src.config_type, src.description, src.encrypted_yn, src.created_by, src.updated_by);
MERGE INTO cpf_log_policy tgt USING (
SELECT 'ONLINE_DEFAULT' policy_key, '온라인 거래 기본 로그 정책' policy_name, 'ONLINE_TRANSACTION' target_type, '*' target_id, 'INFO' log_level, 'Y' db_log_enabled_yn, 'Y' file_log_enabled_yn, 2 policy_schema_version, 'NONE' query_capture_mode, 'ALLOWLIST' request_header_capture_mode, 'ALLOWLIST' response_header_capture_mode, 'NONE' request_body_capture_mode, 'NONE' response_body_capture_mode, 'SUMMARY' error_stack_capture_mode, 'content-type,x-cpf-trace-id,x-cpf-transaction-id' header_allowlist, 4096 max_query_bytes, 8192 max_header_bytes, 65536 max_request_body_bytes, 65536 max_response_body_bytes, 32768 max_stack_bytes, 'N' request_body_log_yn, 'N' response_body_log_yn, 'Y' error_stack_log_yn, 'DEFAULT' masking_policy_key, '04aec0a6adbf48c269e1538ca571819dc54400391e33d5b497ec05406bccd445' policy_checksum, 90 retention_days, 100.00 sampling_rate, 100 priority, 'Y' active_yn, '온라인 Controller/API 기본 로그 정책' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'BATCH_DEFAULT' policy_key, '배치 기본 로그 정책' policy_name, 'BATCH_JOB' target_type, '*' target_id, 'INFO' log_level, 'Y' db_log_enabled_yn, 'Y' file_log_enabled_yn, 2 policy_schema_version, 'NONE' query_capture_mode, 'ALLOWLIST' request_header_capture_mode, 'ALLOWLIST' response_header_capture_mode, 'NONE' request_body_capture_mode, 'NONE' response_body_capture_mode, 'SUMMARY' error_stack_capture_mode, 'content-type,x-cpf-trace-id,x-cpf-transaction-id' header_allowlist, 4096 max_query_bytes, 8192 max_header_bytes, 65536 max_request_body_bytes, 65536 max_response_body_bytes, 32768 max_stack_bytes, 'N' request_body_log_yn, 'N' response_body_log_yn, 'Y' error_stack_log_yn, 'DEFAULT' masking_policy_key, '0eca9ff2359e55290f01c2594d399c32e4af9decd34541a6f571a4345f36ca08' policy_checksum, 180 retention_days, 100.00 sampling_rate, 100 priority, 'Y' active_yn, 'Spring Batch Job 기본 로그 정책' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'ADM_OPERATION_DEFAULT' policy_key, 'ADM 운영 기본 로그 정책' policy_name, 'MODULE' target_type, 'ADM' target_id, 'INFO' log_level, 'Y' db_log_enabled_yn, 'Y' file_log_enabled_yn, 2 policy_schema_version, 'NONE' query_capture_mode, 'ALLOWLIST' request_header_capture_mode, 'ALLOWLIST' response_header_capture_mode, 'NONE' request_body_capture_mode, 'NONE' response_body_capture_mode, 'SUMMARY' error_stack_capture_mode, 'content-type,x-cpf-trace-id,x-cpf-transaction-id' header_allowlist, 4096 max_query_bytes, 8192 max_header_bytes, 65536 max_request_body_bytes, 65536 max_response_body_bytes, 32768 max_stack_bytes, 'N' request_body_log_yn, 'N' response_body_log_yn, 'Y' error_stack_log_yn, 'DEFAULT' masking_policy_key, '9ea15a6d3c662bcaf9295a2512cef8fc12da0e77eea6f07b3c5e55e5fb79e705' policy_checksum, 365 retention_days, 100.00 sampling_rate, 50 priority, 'Y' active_yn, 'ADM 운영 API 기본 로그 정책' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
) src ON (tgt.policy_key = src.policy_key)
WHEN MATCHED THEN UPDATE SET tgt.policy_name = src.policy_name, tgt.target_type = src.target_type, tgt.target_id = src.target_id, tgt.log_level = src.log_level, tgt.db_log_enabled_yn = src.db_log_enabled_yn, tgt.file_log_enabled_yn = src.file_log_enabled_yn, tgt.policy_schema_version = src.policy_schema_version, tgt.query_capture_mode = src.query_capture_mode, tgt.request_header_capture_mode = src.request_header_capture_mode, tgt.response_header_capture_mode = src.response_header_capture_mode, tgt.request_body_capture_mode = src.request_body_capture_mode, tgt.response_body_capture_mode = src.response_body_capture_mode, tgt.error_stack_capture_mode = src.error_stack_capture_mode, tgt.header_allowlist = src.header_allowlist, tgt.max_query_bytes = src.max_query_bytes, tgt.max_header_bytes = src.max_header_bytes, tgt.max_request_body_bytes = src.max_request_body_bytes, tgt.max_response_body_bytes = src.max_response_body_bytes, tgt.max_stack_bytes = src.max_stack_bytes, tgt.request_body_log_yn = src.request_body_log_yn, tgt.response_body_log_yn = src.response_body_log_yn, tgt.error_stack_log_yn = src.error_stack_log_yn, tgt.masking_policy_key = src.masking_policy_key, tgt.policy_checksum = src.policy_checksum, tgt.retention_days = src.retention_days, tgt.sampling_rate = src.sampling_rate, tgt.priority = src.priority, tgt.active_yn = src.active_yn, tgt.description = src.description, tgt.updated_by = src.updated_by
WHEN NOT MATCHED THEN INSERT (policy_key, policy_name, target_type, target_id, log_level, db_log_enabled_yn, file_log_enabled_yn, policy_schema_version, query_capture_mode, request_header_capture_mode, response_header_capture_mode, request_body_capture_mode, response_body_capture_mode, error_stack_capture_mode, header_allowlist, max_query_bytes, max_header_bytes, max_request_body_bytes, max_response_body_bytes, max_stack_bytes, request_body_log_yn, response_body_log_yn, error_stack_log_yn, masking_policy_key, policy_checksum, retention_days, sampling_rate, priority, active_yn, description, created_by, updated_by) VALUES (src.policy_key, src.policy_name, src.target_type, src.target_id, src.log_level, src.db_log_enabled_yn, src.file_log_enabled_yn, src.policy_schema_version, src.query_capture_mode, src.request_header_capture_mode, src.response_header_capture_mode, src.request_body_capture_mode, src.response_body_capture_mode, src.error_stack_capture_mode, src.header_allowlist, src.max_query_bytes, src.max_header_bytes, src.max_request_body_bytes, src.max_response_body_bytes, src.max_stack_bytes, src.request_body_log_yn, src.response_body_log_yn, src.error_stack_log_yn, src.masking_policy_key, src.policy_checksum, src.retention_days, src.sampling_rate, src.priority, src.active_yn, src.description, src.created_by, src.updated_by);
MERGE INTO cpf_security_jwt_key tgt USING (
SELECT 'local-cpf-hs256-001' KEY_ID, 'CPF' ISSUER, 'HS256' ALGORITHM, 'ENV:CPF_CMN_SECURITY_JWT_SECRET' SECRET_REF, 'Y' ACTIVE_YN, NULL EXPIRE_AT, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
) src ON (tgt.KEY_ID = src.KEY_ID)
WHEN MATCHED THEN UPDATE SET tgt.ISSUER = src.ISSUER, tgt.ALGORITHM = src.ALGORITHM, tgt.SECRET_REF = src.SECRET_REF, tgt.ACTIVE_YN = src.ACTIVE_YN, tgt.EXPIRE_AT = src.EXPIRE_AT, tgt.updated_by = src.updated_by, tgt.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (KEY_ID, ISSUER, ALGORITHM, SECRET_REF, ACTIVE_YN, EXPIRE_AT, created_by, updated_by) VALUES (src.KEY_ID, src.ISSUER, src.ALGORITHM, src.SECRET_REF, src.ACTIVE_YN, src.EXPIRE_AT, src.created_by, src.updated_by);
INSERT INTO cpf_cache_refresh_event (cache_name, event_type, event_key, source_was_id, published_by, created_by, updated_by) SELECT 'ALL', 'INITIAL_LOAD', 'INITIAL_FRAMEWORK_SEED', 'SQL', 'SYSTEM', 'SYSTEM', 'SYSTEM'
WHERE NOT EXISTS (
    SELECT 1
    FROM cpf_cache_refresh_event
    WHERE cache_name = 'ALL'
      AND event_type = 'INITIAL_LOAD'
      AND event_key = 'INITIAL_FRAMEWORK_SEED'
);
MERGE INTO cpf_notification_rule tgt USING (
SELECT 'BATCH_EXECUTION' event_type, 'FAILED' event_sub_type, 'ADM' channel_code, 'BATCH_FAILED_DEFAULT' template_code, 'ERROR' severity, 'ADM_BATCH_OPERATOR' receiver_group, 'Y' use_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'SECURITY_EVENT' event_type, 'LOGIN_FAILURE' event_sub_type, 'ADM' channel_code, 'SECURITY_LOGIN_FAILURE' template_code, 'WARN' severity, 'ADM_SECURITY_OPERATOR' receiver_group, 'Y' use_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
) src ON (tgt.event_type = src.event_type AND tgt.event_sub_type = src.event_sub_type AND tgt.channel_code = src.channel_code)
WHEN MATCHED THEN UPDATE SET tgt.template_code = src.template_code, tgt.severity = src.severity, tgt.receiver_group = src.receiver_group, tgt.use_yn = src.use_yn, tgt.updated_by = src.updated_by, tgt.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (event_type, event_sub_type, channel_code, template_code, severity, receiver_group, use_yn, created_by, updated_by) VALUES (src.event_type, src.event_sub_type, src.channel_code, src.template_code, src.severity, src.receiver_group, src.use_yn, src.created_by, src.updated_by);
INSERT INTO cpf_code (parent_id, code_key, code_value, description, created_by, updated_by) SELECT NULL, 'CODE_GROUP', 'SORT_DIRECTION', '표준 정렬 방향', 'SYSTEM', 'SYSTEM'
WHERE NOT EXISTS (SELECT 1 FROM cpf_code WHERE code_key='CODE_GROUP' AND code_value='SORT_DIRECTION');
MERGE INTO cpf_code tgt USING (
SELECT (SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key='CODE_GROUP' AND code_value='SORT_DIRECTION') x) parent_id, 'SORT_DIRECTION' code_key, 'ASC' code_value, '오름차순' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT (SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key='CODE_GROUP' AND code_value='SORT_DIRECTION') x) parent_id, 'SORT_DIRECTION' code_key, 'DESC' code_value, '내림차순' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
) src ON (tgt.code_key = src.code_key AND tgt.code_value = src.code_value)
WHEN MATCHED THEN UPDATE SET tgt.parent_id = src.parent_id, tgt.description = src.description, tgt.updated_by = src.updated_by, tgt.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (parent_id, code_key, code_value, description, created_by, updated_by) VALUES (src.parent_id, src.code_key, src.code_value, src.description, src.created_by, src.updated_by);
MERGE INTO cpf_message tgt USING (
SELECT 'MCPF020004' message_code, 'ko' locale, 'FIXED' message_format_type, '요청 사용자 정보가 인증 사용자와 일치하지 않습니다.' external_message, 'Body requester spoofing이 차단되었습니다.' internal_message, 0 parameter_count, NULL parameter_sample, 'Requester spoof 차단' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'MCPF020005' message_code, 'ko' locale, 'FIXED' message_format_type, '이미 사용된 정책 버전은 직접 수정할 수 없습니다.' external_message, '사용된 Approval Policy version은 immutable입니다.' internal_message, 0 parameter_count, NULL parameter_sample, '정책 버전 불변성' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'MCPF020006' message_code, 'ko' locale, 'FIXED' message_format_type, '동일 작업 식별자가 다른 요청에 사용되었습니다.' external_message, 'operationId payload 충돌입니다.' internal_message, 0 parameter_count, NULL parameter_sample, '멱등 작업 충돌' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'MCPF020007' message_code, 'ko' locale, 'FIXED' message_format_type, '현재 데이터가 다른 요청에서 변경되었습니다.' external_message, 'expectedVersion CAS가 실패했습니다.' internal_message, 0 parameter_count, NULL parameter_sample, '낙관적 잠금 재조회' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'MCPF040003' message_code, 'ko' locale, 'FIXED' message_format_type, '보존 정책에 의해 해당 데이터는 삭제할 수 없습니다.' external_message, 'LEGAL_HOLD가 적용되어 destructive retention을 차단했습니다.' internal_message, 0 parameter_count, NULL parameter_sample, 'Legal hold' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'MCPF040004' message_code, 'ko' locale, 'FIXED' message_format_type, '보존 작업 실행이 비활성화되어 있습니다.' external_message, 'CPF.RETENTION.EXECUTE_ENABLED kill switch가 OFF입니다.' internal_message, 0 parameter_count, NULL parameter_sample, 'Retention kill switch' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'MCPF050001' message_code, 'ko' locale, 'FIXED' message_format_type, 'Secret 원문은 조회할 수 없습니다.' external_message, 'Secret API는 metadata/reference만 노출합니다.' internal_message, 0 parameter_count, NULL parameter_sample, 'Secret 비노출' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'MCPF050002' message_code, 'ko' locale, 'FIXED' message_format_type, '테넌트 식별정보가 필요합니다.' external_message, 'Tenant mode에서 resolver가 tenantId를 결정하지 못했습니다.' internal_message, 0 parameter_count, NULL parameter_sample, 'Tenant 필수' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
) src ON (tgt.message_code = src.message_code AND tgt.locale = src.locale)
WHEN MATCHED THEN UPDATE SET tgt.message_format_type = src.message_format_type, tgt.external_message = src.external_message, tgt.internal_message = src.internal_message, tgt.description = src.description, tgt.use_yn = 'Y', tgt.updated_by = src.updated_by, tgt.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (message_code, locale, message_format_type, external_message, internal_message, parameter_count, parameter_sample, description, created_by, updated_by) VALUES (src.message_code, src.locale, src.message_format_type, src.external_message, src.internal_message, src.parameter_count, src.parameter_sample, src.description, src.created_by, src.updated_by);
MERGE INTO cpf_response_code tgt USING (
SELECT 'ECPF020004' response_code, 'MCPF020004' message_code, 'E' result_type, 'CPF' module_id, '02' response_group, '0004' sequence_no, 403 http_status, 'Requester spoof blocked' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'ECPF020005' response_code, 'MCPF020005' message_code, 'E' result_type, 'CPF' module_id, '02' response_group, '0005' sequence_no, 409 http_status, 'Policy version immutable' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'ECPF020006' response_code, 'MCPF020006' message_code, 'E' result_type, 'CPF' module_id, '02' response_group, '0006' sequence_no, 409 http_status, 'Operation id conflict' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'ECPF020007' response_code, 'MCPF020007' message_code, 'E' result_type, 'CPF' module_id, '02' response_group, '0007' sequence_no, 409 http_status, 'Optimistic lock retry' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'ECPF040003' response_code, 'MCPF040003' message_code, 'E' result_type, 'CPF' module_id, '04' response_group, '0003' sequence_no, 423 http_status, 'Legal hold' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'ECPF040004' response_code, 'MCPF040004' message_code, 'E' result_type, 'CPF' module_id, '04' response_group, '0004' sequence_no, 403 http_status, 'Retention disabled' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'ECPF050001' response_code, 'MCPF050001' message_code, 'E' result_type, 'CPF' module_id, '05' response_group, '0001' sequence_no, 403 http_status, 'Secret value hidden' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'ECPF050002' response_code, 'MCPF050002' message_code, 'E' result_type, 'CPF' module_id, '05' response_group, '0002' sequence_no, 400 http_status, 'Tenant required' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
) src ON (tgt.response_code = src.response_code)
WHEN MATCHED THEN UPDATE SET tgt.message_code = src.message_code, tgt.result_type = src.result_type, tgt.module_id = src.module_id, tgt.response_group = src.response_group, tgt.sequence_no = src.sequence_no, tgt.http_status = src.http_status, tgt.description = src.description, tgt.use_yn = 'Y', tgt.updated_by = src.updated_by, tgt.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (response_code, message_code, result_type, module_id, response_group, sequence_no, http_status, description, created_by, updated_by) VALUES (src.response_code, src.message_code, src.result_type, src.module_id, src.response_group, src.sequence_no, src.http_status, src.description, src.created_by, src.updated_by);
MERGE INTO cpf_config tgt USING (
SELECT 'CPF.PAGING.DEFAULT_SIZE' config_key, '20' config_value, 'NUMBER' config_type, '공통 Page 기본 크기' description, 'N' encrypted_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'CPF.PAGING.MAX_SIZE' config_key, '200' config_value, 'NUMBER' config_type, '공통 Page 최대 크기' description, 'N' encrypted_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'CPF.RETENTION.DRY_RUN_DEFAULT' config_key, 'Y' config_value, 'BOOLEAN' config_type, 'Retention 기본 Dry-run' description, 'N' encrypted_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'CPF.RETENTION.EXECUTE_ENABLED' config_key, 'N' config_value, 'BOOLEAN' config_type, '실제 Archive/Purge 실행 Kill Switch 기본 OFF' description, 'N' encrypted_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'CPF.SECRET.CACHE_TTL_SECONDS' config_key, '300' config_value, 'NUMBER' config_type, 'Secret metadata/cache 기본 TTL' description, 'N' encrypted_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'CPF.TENANT.ENABLED' config_key, 'N' config_value, 'BOOLEAN' config_type, 'Tenant context 기능 기본 OFF' description, 'N' encrypted_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'CPF.HEALTH.REMOTE_DEPENDENCY_GATES_READINESS' config_key, 'N' config_value, 'BOOLEAN' config_type, 'Remote owner 장애가 local readiness를 직접 차단하지 않음' description, 'N' encrypted_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
) src ON (tgt.config_key = src.config_key)
WHEN MATCHED THEN UPDATE SET tgt.config_value = src.config_value, tgt.config_type = src.config_type, tgt.description = src.description, tgt.encrypted_yn = src.encrypted_yn, tgt.use_yn = 'Y', tgt.updated_by = src.updated_by, tgt.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (config_key, config_value, config_type, description, encrypted_yn, created_by, updated_by) VALUES (src.config_key, src.config_value, src.config_type, src.description, src.encrypted_yn, src.created_by, src.updated_by);
MERGE INTO cpf_code tgt USING (
SELECT (SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key='CODE_GROUP' AND code_value='REQUEST_TYPE') x) parent_id, 'REQUEST_TYPE' code_key, 'O' code_value, '온라인 요청' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT (SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key='CODE_GROUP' AND code_value='REQUEST_TYPE') x) parent_id, 'REQUEST_TYPE' code_key, 'S' code_value, '공유 내부 서비스 요청' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT (SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key='CODE_GROUP' AND code_value='REQUEST_TYPE') x) parent_id, 'REQUEST_TYPE' code_key, 'B' code_value, '배치 요청' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT (SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key='CODE_GROUP' AND code_value='CHANNEL_CODE') x) parent_id, 'CHANNEL_CODE' code_key, 'APP' code_value, '모바일 앱 채널' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT (SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key='CODE_GROUP' AND code_value='CHANNEL_CODE') x) parent_id, 'CHANNEL_CODE' code_key, 'JUT' code_value, 'JUnit/자동 테스트 채널' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT (SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key='CODE_GROUP' AND code_value='RESULT_TYPE') x) parent_id, 'RESULT_TYPE' code_key, 'W' code_value, '경고/부분 성공' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT (SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key='CODE_GROUP' AND code_value='MESSAGE_FORMAT_TYPE') x) parent_id, 'MESSAGE_FORMAT_TYPE' code_key, 'PARAMETER' code_value, 'Named parameter 메시지' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT (SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key='CODE_GROUP' AND code_value='ASYNC_STATUS') x) parent_id, 'ASYNC_STATUS' code_key, 'FAILED' code_value, '비동기 처리 실패' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT (SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key='CODE_GROUP' AND code_value='BATCH_JOB_TYPE') x) parent_id, 'BATCH_JOB_TYPE' code_key, 'SPRING_BATCH' code_value, 'Spring Batch Job' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT (SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key='CODE_GROUP' AND code_value='BATCH_JOB_TYPE') x) parent_id, 'BATCH_JOB_TYPE' code_key, 'WORKER' code_value, '지속 Worker' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT (SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key='CODE_GROUP' AND code_value='BATCH_JOB_TYPE') x) parent_id, 'BATCH_JOB_TYPE' code_key, 'SCHEDULER' code_value, 'Scheduler Job' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT (SELECT code_id FROM (SELECT code_id FROM cpf_code WHERE code_key='CODE_GROUP' AND code_value='BATCH_JOB_TYPE') x) parent_id, 'BATCH_JOB_TYPE' code_key, 'CENTER_CUT' code_value, 'Center-Cut 대량 처리' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
) src ON (tgt.code_key = src.code_key AND tgt.code_value = src.code_value)
WHEN MATCHED THEN UPDATE SET tgt.parent_id = src.parent_id, tgt.description = src.description, tgt.use_yn = 'Y', tgt.updated_by = src.updated_by, tgt.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (parent_id, code_key, code_value, description, created_by, updated_by) VALUES (src.parent_id, src.code_key, src.code_value, src.description, src.created_by, src.updated_by);

-- ===== END 50_framework_seed_data.sql =====

-- ===== BEGIN 52_standard_execution_alias_seed.sql =====
-- AUTO-GENERATED from cpf-tools/db/canonical/seed-model.json
-- vendor=oracle; source=52_standard_execution_alias_seed.sql
-- DO NOT EDIT generated seed directly.

-- CPF_LOGICAL_DATABASE=cpfDB
DELETE FROM cpf_standard_execution_alias WHERE legacy_execution_id LIKE 'OADM-MBR-%' OR standard_execution_id LIKE 'OADMMB%';
MERGE INTO cpf_standard_execution_alias tgt USING (
SELECT 'BADM-RLG-EX-0001' legacy_execution_id, 'BADMRL0001' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'BBAT-CUT-CL-0001' legacy_execution_id, 'BBATCU0001' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'BBAT-OPS-FL-0001' legacy_execution_id, 'BBATOP0001' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'BBAT-OPS-HB-0001' legacy_execution_id, 'BBATOP0002' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'BBAT-OPS-SM-0001' legacy_execution_id, 'BBATOP0003' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'BREF-EDU-CH-0001' legacy_execution_id, 'BREFAA0001' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'BREF-EDU-RT-0001' legacy_execution_id, 'BREFAA0002' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'BREF-EDU-TS-0001' legacy_execution_id, 'BREFAA0003' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-BAT-01-0010' legacy_execution_id, 'OADMBA0010' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-BAT-01-0012' legacy_execution_id, 'OADMBA0012' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-BAT-01-0013' legacy_execution_id, 'OADMBA0013' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-BAT-01-0014' legacy_execution_id, 'OADMBA0014' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-BAT-01-0015' legacy_execution_id, 'OADMBA0015' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-BAT-01-0016' legacy_execution_id, 'OADMBA0016' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-BAT-01-0023' legacy_execution_id, 'OADMBA0023' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-BAT-01-0024' legacy_execution_id, 'OADMBA0024' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-BAT-01-0025' legacy_execution_id, 'OADMBA0025' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-BAT-01-0027' legacy_execution_id, 'OADMBA0027' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-BAT-01-0028' legacy_execution_id, 'OADMBA0028' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-BAT-01-0029' legacy_execution_id, 'OADMBA0029' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-BAT-01-0030' legacy_execution_id, 'OADMBA0030' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-BAT-01-0032' legacy_execution_id, 'OADMBA0032' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-BAT-01-0034' legacy_execution_id, 'OADMBA0034' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-BAT-02-0011' legacy_execution_id, 'OADMBA0011' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-BAT-02-0017' legacy_execution_id, 'OADMBA0017' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-BAT-02-0018' legacy_execution_id, 'OADMBA0018' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-BAT-02-0019' legacy_execution_id, 'OADMBA0019' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-BAT-02-0026' legacy_execution_id, 'OADMBA0026' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-BAT-03-0020' legacy_execution_id, 'OADMBA0020' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-BAT-03-0021' legacy_execution_id, 'OADMBA0021' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-BAT-03-0022' legacy_execution_id, 'OADMBA0022' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-BAT-03-0031' legacy_execution_id, 'OADMBA0031' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-BAT-03-0033' legacy_execution_id, 'OADMBA0033' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-CDE-01-0010' legacy_execution_id, 'OADMCD0010' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-CDE-01-0011' legacy_execution_id, 'OADMCD0011' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-CDE-02-0012' legacy_execution_id, 'OADMCD0012' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-CDE-03-0013' legacy_execution_id, 'OADMCD0013' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-CDE-04-0014' legacy_execution_id, 'OADMCD0014' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-CFG-01-0010' legacy_execution_id, 'OADMCF0010' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-CFG-01-0011' legacy_execution_id, 'OADMCF0011' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-CFG-02-0012' legacy_execution_id, 'OADMCF0012' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-CFG-03-0013' legacy_execution_id, 'OADMCF0013' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-CFG-04-0014' legacy_execution_id, 'OADMCF0014' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-CTC-01-0010' legacy_execution_id, 'OADMCT0010' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-CTC-01-0020' legacy_execution_id, 'OADMCT0020' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-CTC-01-0030' legacy_execution_id, 'OADMCT0030' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-CTC-01-0040' legacy_execution_id, 'OADMCT0040' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-CTC-01-0050' legacy_execution_id, 'OADMCT0050' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-CTC-01-0060' legacy_execution_id, 'OADMCT0060' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-CTC-01-0070' legacy_execution_id, 'OADMCT0070' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-DWN-01-0001' legacy_execution_id, 'OADMDW0001' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-DWN-01-0002' legacy_execution_id, 'OADMDW0002' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-DWN-02-0003' legacy_execution_id, 'OADMDW0003' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-EXE-01-0001' legacy_execution_id, 'OADMEX0001' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-EXE-01-0002' legacy_execution_id, 'OADMEX0002' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-LGP-01-0010' legacy_execution_id, 'OADMLG0010' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-LGP-01-0011' legacy_execution_id, 'OADMLG0011' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-LGP-01-0018' legacy_execution_id, 'OADMLG0018' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-LGP-01-0020' legacy_execution_id, 'OADMLG0020' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-LGP-01-0021' legacy_execution_id, 'OADMLG0021' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-LGP-03-0012' legacy_execution_id, 'OADMLG0012' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-LGP-03-0013' legacy_execution_id, 'OADMLG0013' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-LGP-03-0014' legacy_execution_id, 'OADMLG0014' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-LGP-03-0016' legacy_execution_id, 'OADMLG0016' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-LGP-03-0018' legacy_execution_id, 'OADMLG0019' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-LGP-04-0015' legacy_execution_id, 'OADMLG0015' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-LGP-04-0017' legacy_execution_id, 'OADMLG0017' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-LGP-04-0019' legacy_execution_id, 'OADMLG0022' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-MSG-01-0010' legacy_execution_id, 'OADMMS0010' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-MSG-01-0011' legacy_execution_id, 'OADMMS0011' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-MSG-02-0012' legacy_execution_id, 'OADMMS0012' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-MSG-03-0013' legacy_execution_id, 'OADMMS0013' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-MSG-04-0014' legacy_execution_id, 'OADMMS0014' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-NTF-01-0010' legacy_execution_id, 'OADMNT0010' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-NTF-01-0011' legacy_execution_id, 'OADMNT0011' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-NTF-01-0014' legacy_execution_id, 'OADMNT0014' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-NTF-02-0012' legacy_execution_id, 'OADMNT0012' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-NTF-02-0016' legacy_execution_id, 'OADMNT0016' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-NTF-03-0013' legacy_execution_id, 'OADMNT0013' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-NTF-03-0015' legacy_execution_id, 'OADMNT0015' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-OBS-01-0010' legacy_execution_id, 'OADMOB0010' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-OBS-01-0011' legacy_execution_id, 'OADMOB0011' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-OBS-01-0012' legacy_execution_id, 'OADMOB0012' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-OPR-01-0001' legacy_execution_id, 'OADMOP0001' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-OPR-01-0002' legacy_execution_id, 'OADMOP0002' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-OPR-01-0010' legacy_execution_id, 'OADMOP0010' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-OPR-01-0020' legacy_execution_id, 'OADMOP0020' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-OPR-01-0030' legacy_execution_id, 'OADMOP0030' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-OPR-01-0034' legacy_execution_id, 'OADMOP0034' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-OPR-01-0035' legacy_execution_id, 'OADMOP0035' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-OPR-01-0036' legacy_execution_id, 'OADMOP0036' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-OPR-01-0040' legacy_execution_id, 'OADMOP0040' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-OPR-01-0041' legacy_execution_id, 'OADMOP0041' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-OPR-01-0042' legacy_execution_id, 'OADMOP0042' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-OPR-01-0043' legacy_execution_id, 'OADMOP0043' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-OPR-01-0050' legacy_execution_id, 'OADMOP0050' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-OPR-02-0031' legacy_execution_id, 'OADMOP0031' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-OPR-02-0042' legacy_execution_id, 'OADMOP0044' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-OPR-03-0032' legacy_execution_id, 'OADMOP0032' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-OPR-03-0037' legacy_execution_id, 'OADMOP0037' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-OPR-03-0038' legacy_execution_id, 'OADMOP0038' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-OPR-03-0039' legacy_execution_id, 'OADMOP0039' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-OPR-03-0043' legacy_execution_id, 'OADMOP0045' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-OPR-03-0044' legacy_execution_id, 'OADMOP0046' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-OPR-03-0045' legacy_execution_id, 'OADMOP0047' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-OPR-04-0022' legacy_execution_id, 'OADMOP0022' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-OPR-04-0044' legacy_execution_id, 'OADMOP0048' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-OPR-05-0011' legacy_execution_id, 'OADMOP0011' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-OPR-05-0021' legacy_execution_id, 'OADMOP0021' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-OPR-06-0033' legacy_execution_id, 'OADMOP0033' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-OPR-06-0040' legacy_execution_id, 'OADMOP0049' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-OPR-06-0042' legacy_execution_id, 'OADMOP0051' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-PER-01-0010' legacy_execution_id, 'OADMPE0010' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-PER-01-0011' legacy_execution_id, 'OADMPE0011' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-PER-01-0014' legacy_execution_id, 'OADMPE0014' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-PER-01-0015' legacy_execution_id, 'OADMPE0015' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-PER-01-0019' legacy_execution_id, 'OADMPE0019' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-PER-01-0020' legacy_execution_id, 'OADMPE0020' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-PER-01-0024' legacy_execution_id, 'OADMPE0024' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-PER-01-0025' legacy_execution_id, 'OADMPE0025' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-PER-01-0029' legacy_execution_id, 'OADMPE0029' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-PER-01-0030' legacy_execution_id, 'OADMPE0030' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-PER-01-0034' legacy_execution_id, 'OADMPE0034' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-PER-02-0016' legacy_execution_id, 'OADMPE0016' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-PER-02-0021' legacy_execution_id, 'OADMPE0021' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-PER-02-0026' legacy_execution_id, 'OADMPE0026' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-PER-02-0031' legacy_execution_id, 'OADMPE0031' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-PER-03-0012' legacy_execution_id, 'OADMPE0012' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-PER-03-0013' legacy_execution_id, 'OADMPE0013' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-PER-03-0017' legacy_execution_id, 'OADMPE0017' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-PER-03-0018' legacy_execution_id, 'OADMPE0018' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-PER-03-0022' legacy_execution_id, 'OADMPE0022' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-PER-03-0023' legacy_execution_id, 'OADMPE0023' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-PER-03-0027' legacy_execution_id, 'OADMPE0027' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-PER-03-0028' legacy_execution_id, 'OADMPE0028' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-PER-03-0032' legacy_execution_id, 'OADMPE0032' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-PER-03-0033' legacy_execution_id, 'OADMPE0033' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-PER-03-0035' legacy_execution_id, 'OADMPE0035' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-REL-01-0001' legacy_execution_id, 'OADMRE0001' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-REL-01-0002' legacy_execution_id, 'OADMRE0002' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-REL-01-0003' legacy_execution_id, 'OADMRE0003' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-REL-01-0004' legacy_execution_id, 'OADMRE0004' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-REL-01-0006' legacy_execution_id, 'OADMRE0006' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-REL-01-0007' legacy_execution_id, 'OADMRE0007' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-REL-01-0009' legacy_execution_id, 'OADMRE0009' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-REL-01-0010' legacy_execution_id, 'OADMRE0010' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-REL-01-0011' legacy_execution_id, 'OADMRE0011' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-REL-05-0005' legacy_execution_id, 'OADMRE0005' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-REL-05-0008' legacy_execution_id, 'OADMRE0008' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-REL-05-0012' legacy_execution_id, 'OADMRE0012' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-REL-05-0013' legacy_execution_id, 'OADMRE0013' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-RLG-CR-0001' legacy_execution_id, 'OADMRL0001' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-RLG-DL-0001' legacy_execution_id, 'OADMRL0002' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-RLG-DL-0002' legacy_execution_id, 'OADMRL0003' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-RLG-DW-0001' legacy_execution_id, 'OADMRL0004' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-RLG-IS-0001' legacy_execution_id, 'OADMRL0005' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-RLG-QY-0001' legacy_execution_id, 'OADMRL0006' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-RLG-QY-0002' legacy_execution_id, 'OADMRL0007' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-RLG-QY-0003' legacy_execution_id, 'OADMRL0008' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-RLG-QY-0004' legacy_execution_id, 'OADMRL0009' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-SEC-01-0010' legacy_execution_id, 'OADMSE0010' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-SEC-01-0012' legacy_execution_id, 'OADMSE0012' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-SEC-03-0011' legacy_execution_id, 'OADMSE0011' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-SEC-03-0013' legacy_execution_id, 'OADMSE0013' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-SEC-03-0014' legacy_execution_id, 'OADMSE0014' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-SEC-03-0015' legacy_execution_id, 'OADMSE0015' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-SVC-01-0010' legacy_execution_id, 'OADMSV0010' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-SVC-01-0020' legacy_execution_id, 'OADMSV0020' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-SVC-01-0030' legacy_execution_id, 'OADMSV0030' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-SVC-01-0040' legacy_execution_id, 'OADMSV0040' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-SVC-01-0050' legacy_execution_id, 'OADMSV0050' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-SVC-01-0060' legacy_execution_id, 'OADMSV0060' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-SVC-01-0070' legacy_execution_id, 'OADMSV0070' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-TRG-01-0001' legacy_execution_id, 'OADMTR0001' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-TRG-01-0002' legacy_execution_id, 'OADMTR0002' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-TRG-01-0003' legacy_execution_id, 'OADMTR0003' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-TRG-01-0004' legacy_execution_id, 'OADMTR0004' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-TRG-01-0005' legacy_execution_id, 'OADMTR0005' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-TRG-01-0006' legacy_execution_id, 'OADMTR0006' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-TRN-01-0010' legacy_execution_id, 'OADMTR0010' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-TRN-01-0011' legacy_execution_id, 'OADMTR0011' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-TRN-04-0013' legacy_execution_id, 'OADMTR0013' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OADM-TRN-05-0012' legacy_execution_id, 'OADMTR0012' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OBAT-OPR-01-0003' legacy_execution_id, 'OBATOP0003' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OBAT-OPR-02-0002' legacy_execution_id, 'OBATOP0002' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OBZA-ADM-01-1001' legacy_execution_id, 'OBZAAD1001' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OBZA-ADM-03-1002' legacy_execution_id, 'OBZAAD1002' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OBZA-APR-01-0001' legacy_execution_id, 'OBZAAP0001' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OBZA-APR-01-0003' legacy_execution_id, 'OBZAAP0003' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OBZA-APR-02-0002' legacy_execution_id, 'OBZAAP0002' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OBZA-APR-05-0004' legacy_execution_id, 'OBZAAP0004' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OBZA-ATC-01-0001' legacy_execution_id, 'OBZAAT0001' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OBZA-ATC-02-0002' legacy_execution_id, 'OBZAAT0002' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OBZA-ATC-DL-0003' legacy_execution_id, 'OBZAAT0003' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OBZA-AUD-01-0001' legacy_execution_id, 'OBZAUD0001' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OBZA-AUT-01-0004' legacy_execution_id, 'OBZAAU0004' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OBZA-AUT-01-0005' legacy_execution_id, 'OBZAAU0005' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OBZA-AUT-01-0007' legacy_execution_id, 'OBZAAU0007' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OBZA-AUT-02-0001' legacy_execution_id, 'OBZAAU0001' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OBZA-AUT-02-0002' legacy_execution_id, 'OBZAAU0002' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OBZA-AUT-02-0003' legacy_execution_id, 'OBZAAU0003' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OBZA-AUT-03-0006' legacy_execution_id, 'OBZAAU0006' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OBZA-AUT-04-0008' legacy_execution_id, 'OBZAAU0008' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OBZA-CUS-01-1001' legacy_execution_id, 'OBZACU1001' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OBZA-DSH-01-0001' legacy_execution_id, 'OBZADS0001' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OBZA-DWN-01-0002' legacy_execution_id, 'OBZADW0002' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OBZA-DWN-01-1001' legacy_execution_id, 'OBZADW1001' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OBZA-EMP-01-0001' legacy_execution_id, 'OBZAEM0001' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OBZA-EMP-03-0002' legacy_execution_id, 'OBZAEM0002' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OBZA-MNU-01-1001' legacy_execution_id, 'OBZAMN1001' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OBZA-MNU-03-1002' legacy_execution_id, 'OBZAMN1002' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OBZA-MSK-02-1001' legacy_execution_id, 'OBZAMS1001' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OBZA-NTF-01-0001' legacy_execution_id, 'OBZANT0001' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OBZA-NTF-02-0002' legacy_execution_id, 'OBZANT0002' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OBZA-NTF-03-0003' legacy_execution_id, 'OBZANT0003' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OBZA-ORD-01-1001' legacy_execution_id, 'OBZAOR1001' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OBZA-ORG-01-0001' legacy_execution_id, 'OBZAOR0001' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OBZA-ORG-03-0002' legacy_execution_id, 'OBZAOR0002' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OBZA-PER-01-0002' legacy_execution_id, 'OBZAPE0002' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OBZA-PER-01-0003' legacy_execution_id, 'OBZAPE0003' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OBZA-PER-01-1001' legacy_execution_id, 'OBZAPE1001' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OBZA-PER-02-0004' legacy_execution_id, 'OBZAPE0004' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OBZA-PER-03-1002' legacy_execution_id, 'OBZAPE1002' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OBZA-PRD-01-1001' legacy_execution_id, 'OBZAPR1001' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OBZA-ROL-01-1001' legacy_execution_id, 'OBZARO1001' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OBZA-ROL-03-1002' legacy_execution_id, 'OBZARO1002' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OBZA-SCH-01-0001' legacy_execution_id, 'OBZASC0001' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OBZA-SCH-03-0002' legacy_execution_id, 'OBZASC0002' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OBZA-SCH-04-0003' legacy_execution_id, 'OBZASC0003' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OBZA-SET-01-1001' legacy_execution_id, 'OBZASE1001' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OBZA-USR-QY-0000' legacy_execution_id, 'OBZAUS0001' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OBZA-USR-QY-0001' legacy_execution_id, 'OBZAUS0002' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OREF-EDU-01-0001' legacy_execution_id, 'OREFAA0001' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OREF-EDU-01-0002' legacy_execution_id, 'OREFAA0002' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OREF-EDU-01-0003' legacy_execution_id, 'OREFAA0003' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OREF-EDU-01-0099' legacy_execution_id, 'OREFAA0099' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OREF-EDU-02-0001' legacy_execution_id, 'OREFAA0004' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OREF-EDU-02-0010' legacy_execution_id, 'OREFAA0010' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OREF-EDU-02-0020' legacy_execution_id, 'OREFAA0020' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OREF-EDU-02-0030' legacy_execution_id, 'OREFAA0030' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OREF-EDU-03-0001' legacy_execution_id, 'OREFAA0005' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OREF-EDU-03-0002' legacy_execution_id, 'OREFAA0006' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OREF-EDU-03-0003' legacy_execution_id, 'OREFAA0007' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OREF-EDU-04-0001' legacy_execution_id, 'OREFAA0008' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OREF-EDU-04-0002' legacy_execution_id, 'OREFAA0009' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OREF-EDU-05-0001' legacy_execution_id, 'OREFAA0011' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OREF-EDU-05-0002' legacy_execution_id, 'OREFAA0012' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OREF-EDU-05-9001' legacy_execution_id, 'OREFAA9001' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OREF-EDU-08-0001' legacy_execution_id, 'OREFAA0013' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OREF-EDU-08-0010' legacy_execution_id, 'OREFAA0014' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OREF-EDU-08-9001' legacy_execution_id, 'OREFAA9002' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OREF-EDU-09-0001' legacy_execution_id, 'OREFAA0015' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OREF-EDU-09-0002' legacy_execution_id, 'OREFAA0016' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OREF-EDU-09-0003' legacy_execution_id, 'OREFAA0017' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OREF-EDU-09-0004' legacy_execution_id, 'OREFAA0018' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OREF-EDU-09-0005' legacy_execution_id, 'OREFAA0019' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OREF-EDU-09-0006' legacy_execution_id, 'OREFAA0021' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OREF-EDU-09-0007' legacy_execution_id, 'OREFAA0022' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OREF-EDU-09-0008' legacy_execution_id, 'OREFAA0023' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OREF-EDU-09-0009' legacy_execution_id, 'OREFAA0024' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OREF-EDU-09-0010' legacy_execution_id, 'OREFAA0025' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OREF-EDU-09-0011' legacy_execution_id, 'OREFAA0026' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OREF-EDU-09-0012' legacy_execution_id, 'OREFAA0027' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OREF-EDU-09-0013' legacy_execution_id, 'OREFAA0028' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OREF-EDU-09-0015' legacy_execution_id, 'OREFAA0029' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OREF-EDU-09-0016' legacy_execution_id, 'OREFAA0031' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OREF-EDU-09-0017' legacy_execution_id, 'OREFAA0032' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OREF-EDU-09-0018' legacy_execution_id, 'OREFAA0033' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OREF-EDU-09-0019' legacy_execution_id, 'OREFAA0034' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OREF-EDU-09-0020' legacy_execution_id, 'OREFAA0035' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OREF-EDU-09-0030' legacy_execution_id, 'OREFAA0036' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OREF-EDU-09-0031' legacy_execution_id, 'OREFAA0037' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OREF-EDU-09-0032' legacy_execution_id, 'OREFAA0038' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OREF-EDU-09-0033' legacy_execution_id, 'OREFAA0039' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OREF-EDU-09-0034' legacy_execution_id, 'OREFAA0040' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OREF-EDU-09-0035' legacy_execution_id, 'OREFAA0041' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OREF-EDU-09-0036' legacy_execution_id, 'OREFAA0042' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OREF-EDU-09-0040' legacy_execution_id, 'OREFAA0043' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OREF-EDU-09-0051' legacy_execution_id, 'OREFAA0051' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OREF-EDU-09-0060' legacy_execution_id, 'OREFAA0060' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OREF-EDU-09-0072' legacy_execution_id, 'OREFAA0072' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OREF-EDU-09-0073' legacy_execution_id, 'OREFAA0073' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OREF-EDU-09-0080' legacy_execution_id, 'OREFAA0080' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OREF-EDU-12-0001' legacy_execution_id, 'OREFAA0044' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OREF-EDU-12-0002' legacy_execution_id, 'OREFAA0045' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OREF-EDU-12-0003' legacy_execution_id, 'OREFAA0046' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OREF-EDU-13-0001' legacy_execution_id, 'OREFAA0047' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OREF-EDU-13-0002' legacy_execution_id, 'OREFAA0048' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OREF-EDU-13-0003' legacy_execution_id, 'OREFAA0049' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OREF-EDU-13-0004' legacy_execution_id, 'OREFAA0050' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OREF-EDU-13-0005' legacy_execution_id, 'OREFAA0052' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OREF-EDU-13-0006' legacy_execution_id, 'OREFAA0053' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OREF-EDU-13-0007' legacy_execution_id, 'OREFAA0054' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OREF-EDU-13-0008' legacy_execution_id, 'OREFAA0055' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OREF-EDU-14-0001' legacy_execution_id, 'OREFAA0056' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OREF-EDU-15-0001' legacy_execution_id, 'OREFAA0057' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OREF-EDU-16-0001' legacy_execution_id, 'OREFAA0058' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OREF-EDU-16-0002' legacy_execution_id, 'OREFAA0059' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OREF-EDU-16-0003' legacy_execution_id, 'OREFAA0061' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OREF-EDU-16-0004' legacy_execution_id, 'OREFAA0062' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OREF-EDU-16-0005' legacy_execution_id, 'OREFAA0063' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OREF-EDU-16-0006' legacy_execution_id, 'OREFAA0064' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OREF-EDU-17-0001' legacy_execution_id, 'OREFAA0065' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OREF-EDU-17-0002' legacy_execution_id, 'OREFAA0066' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OREF-QRY-01-0001' legacy_execution_id, 'OREFQR0001' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OREF-QRY-01-0002' legacy_execution_id, 'OREFQR0002' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OREF-QRY-01-0003' legacy_execution_id, 'OREFQR0003' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OREF-QRY-01-0004' legacy_execution_id, 'OREFQR0004' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
UNION ALL
SELECT 'OREF-QRY-01-0005' legacy_execution_id, 'OREFQR0005' standard_execution_id, 'CPF O/S/B 10자리 표준 전환' migration_reason, 'CPF_SEED' created_by, 'CPF_SEED' updated_by FROM dual
) src ON (tgt.legacy_execution_id = src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id = src.standard_execution_id, tgt.migration_reason = src.migration_reason, tgt.updated_by = src.updated_by, tgt.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);

-- ===== END 52_standard_execution_alias_seed.sql =====

-- ===== BEGIN 56_bza_product_seed.sql =====
-- AUTO-GENERATED from cpf-tools/db/canonical/seed-model.json
-- vendor=oracle; source=56_bza_product_seed.sql
-- DO NOT EDIT generated seed directly.

-- CPF_LOGICAL_DATABASE=bzaDB
MERGE INTO bza_role tgt USING (
SELECT 'BZA_ADMIN' role_code, '업무 관리자' role_name, 'Y' write_allowed_yn, 'ALL' data_scope, 'Y' use_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'BZA_OPERATOR' role_code, '업무 운영자' role_name, 'Y' write_allowed_yn, 'ORGANIZATION' data_scope, 'Y' use_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'BZA_APPROVER' role_code, '업무 결재자' role_name, 'Y' write_allowed_yn, 'ORGANIZATION' data_scope, 'Y' use_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'BZA_VIEWER' role_code, '업무 조회자' role_name, 'N' write_allowed_yn, 'ORGANIZATION' data_scope, 'Y' use_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
) src ON (tgt.role_code = src.role_code)
WHEN MATCHED THEN UPDATE SET tgt.role_name = src.role_name, tgt.write_allowed_yn = src.write_allowed_yn, tgt.data_scope = src.data_scope, tgt.use_yn = src.use_yn, tgt.updated_by = src.updated_by, tgt.updated_at = SYSTIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_code, role_name, write_allowed_yn, data_scope, use_yn, created_by, updated_by) VALUES (src.role_code, src.role_name, src.write_allowed_yn, src.data_scope, src.use_yn, src.created_by, src.updated_by);
MERGE INTO bza_menu tgt USING (
SELECT 'BZA_DASHBOARD' menu_code, '업무 관리자 대시보드' menu_name, NULL parent_menu_code, 'BZA' module_code, '/bza' route_path, 'dashboard' icon_code, 'ALL' environment_code, '/api/bza/dashboard' api_path, 10 sort_order, 'Y' use_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'BZA_ORGANIZATION' menu_code, '조직 관리' menu_name, NULL parent_menu_code, 'BZA' module_code, '/bza/organizations' route_path, 'organization' icon_code, 'ALL' environment_code, '/api/bza/organizations' api_path, 20 sort_order, 'Y' use_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'BZA_EMPLOYEE' menu_code, '직원·소속 관리' menu_name, NULL parent_menu_code, 'BZA' module_code, '/bza/employees' route_path, 'employee' icon_code, 'ALL' environment_code, '/api/bza/employees' api_path, 30 sort_order, 'Y' use_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'BZA_AUTHORIZATION' menu_code, '업무 권한 관리' menu_name, NULL parent_menu_code, 'BZA' module_code, '/bza/authorization' route_path, 'shield' icon_code, 'ALL' environment_code, '/api/bza/authorization' api_path, 40 sort_order, 'Y' use_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'BZA_APPROVAL' menu_code, '업무 결재 관리' menu_name, NULL parent_menu_code, 'BZA' module_code, '/bza/approvals' route_path, 'approval' icon_code, 'ALL' environment_code, '/api/bza/approvals' api_path, 50 sort_order, 'Y' use_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'BZA_AUDIT' menu_code, '업무 감사 조회' menu_name, NULL parent_menu_code, 'BZA' module_code, '/bza/audits' route_path, 'audit' icon_code, 'ALL' environment_code, '/api/bza/audits' api_path, 60 sort_order, 'Y' use_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'BZA_ATTACHMENT' menu_code, '첨부 관리' menu_name, NULL parent_menu_code, 'BZA' module_code, '/bza/attachments' route_path, 'attachment' icon_code, 'ALL' environment_code, '/api/bza/attachments' api_path, 70 sort_order, 'Y' use_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'BZA_SETTING' menu_code, '업무 관리자 설정' menu_name, NULL parent_menu_code, 'BZA' module_code, '/bza/settings' route_path, 'setting' icon_code, 'ALL' environment_code, '/api/bza/settings' api_path, 80 sort_order, 'Y' use_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
) src ON (tgt.menu_code = src.menu_code)
WHEN MATCHED THEN UPDATE SET tgt.menu_name = src.menu_name, tgt.parent_menu_code = src.parent_menu_code, tgt.module_code = src.module_code, tgt.route_path = src.route_path, tgt.icon_code = src.icon_code, tgt.environment_code = src.environment_code, tgt.api_path = src.api_path, tgt.sort_order = src.sort_order, tgt.use_yn = src.use_yn, tgt.updated_by = src.updated_by, tgt.updated_at = SYSTIMESTAMP
WHEN NOT MATCHED THEN INSERT (menu_code, menu_name, parent_menu_code, module_code, route_path, icon_code, environment_code, api_path, sort_order, use_yn, created_by, updated_by) VALUES (src.menu_code, src.menu_name, src.parent_menu_code, src.module_code, src.route_path, src.icon_code, src.environment_code, src.api_path, src.sort_order, src.use_yn, src.created_by, src.updated_by);
MERGE INTO bza_permission tgt USING (
SELECT 'BZA_ADMIN' role_code, menu_code menu_code, 'ALL' button_code, 'API' permission_type, '*' http_method, (api_path || '/**') api_pattern, NULL domain_code, environment_code environment_code, 'ALL' data_scope, 'Y' allow_yn, 'Y' use_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM bza_menu
WHERE use_yn = 'Y'
) src ON (tgt.role_code = src.role_code AND tgt.menu_code = src.menu_code AND tgt.button_code = src.button_code AND tgt.permission_type = src.permission_type AND tgt.environment_code = src.environment_code)
WHEN MATCHED THEN UPDATE SET tgt.permission_type = src.permission_type, tgt.http_method = src.http_method, tgt.api_pattern = src.api_pattern, tgt.environment_code = src.environment_code, tgt.data_scope = src.data_scope, tgt.allow_yn = src.allow_yn, tgt.use_yn = src.use_yn, tgt.updated_by = src.updated_by, tgt.updated_at = SYSTIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_code, menu_code, button_code, permission_type, http_method, api_pattern, domain_code, environment_code, data_scope, allow_yn, use_yn, created_by, updated_by) VALUES (src.role_code, src.menu_code, src.button_code, src.permission_type, src.http_method, src.api_pattern, src.domain_code, src.environment_code, src.data_scope, src.allow_yn, src.use_yn, src.created_by, src.updated_by);
MERGE INTO bza_permission tgt USING (
SELECT 'BZA_OPERATOR' role_code, 'BZA_DASHBOARD' menu_code, 'READ' button_code, 'API' permission_type, 'GET' http_method, '/api/bza/dashboard/**' api_pattern, NULL domain_code, 'ALL' environment_code, 'ORGANIZATION' data_scope, 'Y' allow_yn, 'Y' use_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'BZA_OPERATOR' role_code, 'BZA_ORGANIZATION' menu_code, 'READ' button_code, 'API' permission_type, 'GET' http_method, '/api/bza/organizations/**' api_pattern, NULL domain_code, 'ALL' environment_code, 'ORGANIZATION' data_scope, 'Y' allow_yn, 'Y' use_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'BZA_OPERATOR' role_code, 'BZA_EMPLOYEE' menu_code, 'READ' button_code, 'API' permission_type, 'GET' http_method, '/api/bza/employees/**' api_pattern, NULL domain_code, 'ALL' environment_code, 'ORGANIZATION' data_scope, 'Y' allow_yn, 'Y' use_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'BZA_APPROVER' role_code, 'BZA_APPROVAL' menu_code, 'READ' button_code, 'API' permission_type, 'GET' http_method, '/api/bza/approvals/**' api_pattern, NULL domain_code, 'ALL' environment_code, 'ORGANIZATION' data_scope, 'Y' allow_yn, 'Y' use_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'BZA_APPROVER' role_code, 'BZA_APPROVAL' menu_code, 'DECIDE' button_code, 'API' permission_type, 'POST' http_method, '/api/bza/approvals/*/decisions' api_pattern, NULL domain_code, 'ALL' environment_code, 'ORGANIZATION' data_scope, 'Y' allow_yn, 'Y' use_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'BZA_VIEWER' role_code, 'BZA_DASHBOARD' menu_code, 'READ' button_code, 'API' permission_type, 'GET' http_method, '/api/bza/dashboard/**' api_pattern, NULL domain_code, 'ALL' environment_code, 'ORGANIZATION' data_scope, 'Y' allow_yn, 'Y' use_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'BZA_VIEWER' role_code, 'BZA_AUDIT' menu_code, 'READ' button_code, 'API' permission_type, 'GET' http_method, '/api/bza/audits/**' api_pattern, NULL domain_code, 'ALL' environment_code, 'ORGANIZATION' data_scope, 'Y' allow_yn, 'Y' use_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
) src ON (tgt.role_code = src.role_code AND tgt.menu_code = src.menu_code AND tgt.button_code = src.button_code AND tgt.permission_type = src.permission_type AND tgt.environment_code = src.environment_code)
WHEN MATCHED THEN UPDATE SET tgt.permission_type = src.permission_type, tgt.http_method = src.http_method, tgt.api_pattern = src.api_pattern, tgt.environment_code = src.environment_code, tgt.data_scope = src.data_scope, tgt.allow_yn = src.allow_yn, tgt.use_yn = src.use_yn, tgt.updated_by = src.updated_by, tgt.updated_at = SYSTIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_code, menu_code, button_code, permission_type, http_method, api_pattern, domain_code, environment_code, data_scope, allow_yn, use_yn, created_by, updated_by) VALUES (src.role_code, src.menu_code, src.button_code, src.permission_type, src.http_method, src.api_pattern, src.domain_code, src.environment_code, src.data_scope, src.allow_yn, src.use_yn, src.created_by, src.updated_by);
MERGE INTO bza_project_setting tgt USING (
SELECT 'BZA.APPROVAL.SELF_APPROVAL_ALLOWED' setting_key, 'N' setting_value, '기본 자기승인 차단 정책' description, 'Y' use_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'BZA.APPROVAL.DEFAULT_DUE_HOURS' setting_key, '24' setting_value, '기본 결재 SLA 시간' description, 'Y' use_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'BZA.APPROVAL.REQUIRE_PAYLOAD_HASH' setting_key, 'Y' setting_value, '결재 대상 Payload 변조 검증용 SHA-256 사용' description, 'Y' use_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'BZA.AUDIT.HASH_CHAIN_ENABLED' setting_key, 'Y' setting_value, '업무 감사 로그 hash-chain 검증 사용' description, 'Y' use_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'BZA.ATTACHMENT.SECURITY_SCAN_REQUIRED' setting_key, 'Y' setting_value, '첨부 보안검사 완료 후 사용 허용' description, 'Y' use_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'BZA.ATTACHMENT.DEFAULT_RETENTION_DAYS' setting_key, '365' setting_value, '첨부 기본 보존일수' description, 'Y' use_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
) src ON (tgt.setting_key = src.setting_key)
WHEN MATCHED THEN UPDATE SET tgt.setting_value = src.setting_value, tgt.description = src.description, tgt.use_yn = src.use_yn, tgt.updated_by = src.updated_by, tgt.updated_at = SYSTIMESTAMP
WHEN NOT MATCHED THEN INSERT (setting_key, setting_value, description, use_yn, created_by, updated_by) VALUES (src.setting_key, src.setting_value, src.description, src.use_yn, src.created_by, src.updated_by);
MERGE INTO bza_permission tgt USING (
SELECT 'BZA_ADMIN' role_code, 'BZA_AUTHORIZATION' menu_code, 'SIMULATE' button_code, 'API' permission_type, 'GET' http_method, '/api/bza/backoffice/permissions/effective' api_pattern, NULL domain_code, 'ALL' environment_code, 'ALL' data_scope, 'Y' allow_yn, 'Y' use_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'BZA_ADMIN' role_code, 'BZA_EMPLOYEE' menu_code, 'PII_RAW' button_code, 'API' permission_type, 'POST' http_method, '/api/bza/backoffice/employees/*/contacts/raw' api_pattern, NULL domain_code, 'ALL' environment_code, 'ALL' data_scope, 'Y' allow_yn, 'Y' use_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'BZA_OPERATOR' role_code, 'BZA_AUTHORIZATION' menu_code, 'SIMULATE' button_code, 'API' permission_type, 'GET' http_method, '/api/bza/backoffice/permissions/effective' api_pattern, NULL domain_code, 'ALL' environment_code, 'ORGANIZATION' data_scope, 'Y' allow_yn, 'Y' use_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'BZA_APPROVER' role_code, 'BZA_APPROVAL' menu_code, 'DECIDE' button_code, 'API' permission_type, 'POST' http_method, '/api/bza/backoffice/approvals/*/actions' api_pattern, NULL domain_code, 'ALL' environment_code, 'ORGANIZATION' data_scope, 'Y' allow_yn, 'Y' use_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'BZA_APPROVER' role_code, 'BZA_APPROVAL' menu_code, 'DECIDE' button_code, 'API' permission_type, 'POST' http_method, '/api/bza/approvals/*/decisions' api_pattern, NULL domain_code, 'ALL' environment_code, 'ORGANIZATION' data_scope, 'Y' allow_yn, 'Y' use_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
) src ON (tgt.role_code = src.role_code AND tgt.menu_code = src.menu_code AND tgt.button_code = src.button_code AND tgt.permission_type = src.permission_type AND tgt.environment_code = src.environment_code)
WHEN MATCHED THEN UPDATE SET tgt.http_method = src.http_method, tgt.api_pattern = src.api_pattern, tgt.domain_code = src.domain_code, tgt.data_scope = src.data_scope, tgt.allow_yn = src.allow_yn, tgt.use_yn = src.use_yn, tgt.updated_by = src.updated_by, tgt.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_code, menu_code, button_code, permission_type, http_method, api_pattern, domain_code, environment_code, data_scope, allow_yn, use_yn, created_by, updated_by) VALUES (src.role_code, src.menu_code, src.button_code, src.permission_type, src.http_method, src.api_pattern, src.domain_code, src.environment_code, src.data_scope, src.allow_yn, src.use_yn, src.created_by, src.updated_by);

-- ===== END 56_bza_product_seed.sql =====

-- ===== BEGIN 60_adm_seed_data.sql =====
-- AUTO-GENERATED from cpf-tools/db/canonical/seed-model.json
-- vendor=oracle; source=60_adm_seed_data.sql
-- DO NOT EDIT generated seed directly.

-- CPF_LOGICAL_DATABASE=admDB
MERGE INTO adm_role tgt USING (
SELECT 'ADM_ADMIN' ROLE_ID, '프레임워크 관리자' ROLE_NAME, 'ADMIN' ROLE_TYPE, '모든 ADM 메뉴와 운영 작업을 관리합니다.' DESCRIPTION, 'Y' USE_YN, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'ADM_DEV_OPERATOR' ROLE_ID, '개발자 운영자' ROLE_NAME, 'DEVELOPER_OPERATOR' ROLE_TYPE, '로그, 캐시, 코드, 메시지, 설정, 배치 관제를 운영합니다.' DESCRIPTION, 'Y' USE_YN, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'ADM_BIZ_OPERATOR' ROLE_ID, '업무 운영자' ROLE_NAME, 'BUSINESS_OPERATOR' ROLE_TYPE, '회원, 거래 로그, 배치, 캐시 같은 업무 운영 기능을 수행합니다.' DESCRIPTION, 'Y' USE_YN, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'ADM_VIEWER' ROLE_ID, '조회 전용 운영자' ROLE_NAME, 'VIEWER' ROLE_TYPE, '운영 정보를 조회만 할 수 있습니다.' DESCRIPTION, 'Y' USE_YN, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'ADM_OPERATOR' ROLE_ID, '운영자 호환 역할' ROLE_NAME, 'DEVELOPER_OPERATOR' ROLE_TYPE, '기존 ADM_OPERATOR 호환을 위한 역할입니다.' DESCRIPTION, 'Y' USE_YN, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
) src ON (tgt.ROLE_ID = src.ROLE_ID)
WHEN MATCHED THEN UPDATE SET tgt.ROLE_NAME = src.ROLE_NAME, tgt.ROLE_TYPE = src.ROLE_TYPE, tgt.DESCRIPTION = src.DESCRIPTION, tgt.USE_YN = src.USE_YN, tgt.updated_by = src.updated_by, tgt.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (ROLE_ID, ROLE_NAME, ROLE_TYPE, DESCRIPTION, USE_YN, created_by, updated_by) VALUES (src.ROLE_ID, src.ROLE_NAME, src.ROLE_TYPE, src.DESCRIPTION, src.USE_YN, src.created_by, src.updated_by);
MERGE INTO adm_menu tgt USING (
SELECT 'DASHBOARD' MENU_ID, NULL PARENT_MENU_ID, '대시보드' MENU_NAME, '/adm' MENU_PATH, 10 SORT_ORDER, 'Y' USE_YN, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'LOG_LIST' MENU_ID, NULL PARENT_MENU_ID, '온라인 거래 로그' MENU_NAME, '/adm#logs' MENU_PATH, 20 SORT_ORDER, 'Y' USE_YN, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'STANDARD_EXECUTION' MENU_ID, NULL PARENT_MENU_ID, '표준 실행 카탈로그' MENU_NAME, '/adm#standard-executions' MENU_PATH, 23 SORT_ORDER, 'Y' USE_YN, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'CHANNEL_POLICY' MENU_ID, NULL PARENT_MENU_ID, '채널 정책' MENU_NAME, '/adm#channel-policy' MENU_PATH, 24 SORT_ORDER, 'Y' USE_YN, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'REMOTE_LOG' MENU_ID, NULL PARENT_MENU_ID, '원격 로그 관리' MENU_NAME, '/adm#remote-logs' MENU_PATH, 25 SORT_ORDER, 'Y' USE_YN, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'TRANSACTION_META' MENU_ID, NULL PARENT_MENU_ID, '거래 메타' MENU_NAME, '/adm#transactions' MENU_PATH, 25 SORT_ORDER, 'Y' USE_YN, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'AUDIT_LOG' MENU_ID, NULL PARENT_MENU_ID, '감사 로그' MENU_NAME, '/adm#audit-logs' MENU_PATH, 30 SORT_ORDER, 'Y' USE_YN, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'BATCH' MENU_ID, NULL PARENT_MENU_ID, '배치 관제' MENU_NAME, '/adm#batch' MENU_PATH, 50 SORT_ORDER, 'Y' USE_YN, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'RELIABILITY' MENU_ID, NULL PARENT_MENU_ID, '신뢰성 처리 관제' MENU_NAME, '/adm#reliability' MENU_PATH, 52 SORT_ORDER, 'Y' USE_YN, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'NOTIFICATION' MENU_ID, NULL PARENT_MENU_ID, '알림 관리' MENU_NAME, '/adm#notifications' MENU_PATH, 55 SORT_ORDER, 'Y' USE_YN, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'DOWNLOAD' MENU_ID, NULL PARENT_MENU_ID, '다운로드 감사' MENU_NAME, '/adm#downloads' MENU_PATH, 58 SORT_ORDER, 'Y' USE_YN, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'CACHE' MENU_ID, NULL PARENT_MENU_ID, '캐시 관리' MENU_NAME, '/adm#cache' MENU_PATH, 60 SORT_ORDER, 'Y' USE_YN, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'FILE_JOB' MENU_ID, NULL PARENT_MENU_ID, '대량파일 Job' MENU_NAME, '/adm#file-jobs' MENU_PATH, 61 SORT_ORDER, 'Y' USE_YN, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'MESSAGE' MENU_ID, NULL PARENT_MENU_ID, '메시지 관리' MENU_NAME, '/adm#messages' MENU_PATH, 70 SORT_ORDER, 'Y' USE_YN, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'CODE' MENU_ID, NULL PARENT_MENU_ID, '코드 관리' MENU_NAME, '/adm#codes' MENU_PATH, 80 SORT_ORDER, 'Y' USE_YN, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'RESPONSE_CODE' MENU_ID, NULL PARENT_MENU_ID, '응답코드 관리' MENU_NAME, '/adm#response-codes' MENU_PATH, 90 SORT_ORDER, 'Y' USE_YN, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'CONFIG' MENU_ID, NULL PARENT_MENU_ID, '설정 관리' MENU_NAME, '/adm#configs' MENU_PATH, 100 SORT_ORDER, 'Y' USE_YN, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'DYNAMIC_LOG' MENU_ID, NULL PARENT_MENU_ID, '동적 로그 레벨' MENU_NAME, '/adm#log-level' MENU_PATH, 110 SORT_ORDER, 'Y' USE_YN, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'LOG_POLICY' MENU_ID, NULL PARENT_MENU_ID, '로그 정책' MENU_NAME, '/adm#log-policies' MENU_PATH, 115 SORT_ORDER, 'Y' USE_YN, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'PASSWORD' MENU_ID, NULL PARENT_MENU_ID, '비밀번호 관리' MENU_NAME, '/adm#password' MENU_PATH, 120 SORT_ORDER, 'Y' USE_YN, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'SECURITY' MENU_ID, NULL PARENT_MENU_ID, '보안 운영' MENU_NAME, '/adm#security' MENU_PATH, 130 SORT_ORDER, 'Y' USE_YN, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'PERMISSION' MENU_ID, NULL PARENT_MENU_ID, '권한 관리' MENU_NAME, '/adm#permissions' MENU_PATH, 140 SORT_ORDER, 'Y' USE_YN, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'OPERATOR' MENU_ID, NULL PARENT_MENU_ID, '운영자 관리' MENU_NAME, '/adm#operators' MENU_PATH, 150 SORT_ORDER, 'Y' USE_YN, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
) src ON (tgt.MENU_ID = src.MENU_ID)
WHEN MATCHED THEN UPDATE SET tgt.PARENT_MENU_ID = src.PARENT_MENU_ID, tgt.MENU_NAME = src.MENU_NAME, tgt.MENU_PATH = src.MENU_PATH, tgt.SORT_ORDER = src.SORT_ORDER, tgt.USE_YN = src.USE_YN, tgt.updated_by = src.updated_by, tgt.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (MENU_ID, PARENT_MENU_ID, MENU_NAME, MENU_PATH, SORT_ORDER, USE_YN, created_by, updated_by) VALUES (src.MENU_ID, src.PARENT_MENU_ID, src.MENU_NAME, src.MENU_PATH, src.SORT_ORDER, src.USE_YN, src.created_by, src.updated_by);
MERGE INTO adm_button tgt USING (
SELECT 'LOG_LIST_READ' BUTTON_ID, 'LOG_LIST' MENU_ID, 'READ' ACTION_CODE, '조회' BUTTON_NAME, 'GET' HTTP_METHOD, '/adm/api/logs/**' API_PATTERN, 10 SORT_ORDER, 'Y' USE_YN, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'LOG_LIST_DETAIL' BUTTON_ID, 'LOG_LIST' MENU_ID, 'DETAIL' ACTION_CODE, '상세 조회' BUTTON_NAME, 'GET' HTTP_METHOD, '/adm/api/logs/**' API_PATTERN, 20 SORT_ORDER, 'Y' USE_YN, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'LOG_LIST_DOWNLOAD' BUTTON_ID, 'LOG_LIST' MENU_ID, 'DOWNLOAD' ACTION_CODE, '다운로드' BUTTON_NAME, 'GET' HTTP_METHOD, '/adm/api/logs/**' API_PATTERN, 30 SORT_ORDER, 'Y' USE_YN, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'STANDARD_EXECUTION_READ' BUTTON_ID, 'STANDARD_EXECUTION' MENU_ID, 'READ' ACTION_CODE, '표준 실행 조회' BUTTON_NAME, 'GET' HTTP_METHOD, '/adm/api/standard-executions/**' API_PATTERN, 10 SORT_ORDER, 'Y' USE_YN, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'CHANNEL_POLICY_READ' BUTTON_ID, 'CHANNEL_POLICY' MENU_ID, 'READ' ACTION_CODE, '채널 정책 조회' BUTTON_NAME, 'GET' HTTP_METHOD, '/adm/api/channels/**' API_PATTERN, 10 SORT_ORDER, 'Y' USE_YN, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'CHANNEL_POLICY_WRITE' BUTTON_ID, 'CHANNEL_POLICY' MENU_ID, 'WRITE' ACTION_CODE, '채널·거래 정책 변경' BUTTON_NAME, 'PUT' HTTP_METHOD, '/adm/api/channels/**' API_PATTERN, 20 SORT_ORDER, 'Y' USE_YN, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'CHANNEL_POLICY_REFRESH' BUTTON_ID, 'CHANNEL_POLICY' MENU_ID, 'REFRESH' ACTION_CODE, '채널 정책 스냅샷 갱신' BUTTON_NAME, 'POST' HTTP_METHOD, '/adm/api/channels/refresh' API_PATTERN, 30 SORT_ORDER, 'Y' USE_YN, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'CHANNEL_POLICY_IMPORT' BUTTON_ID, 'CHANNEL_POLICY' MENU_ID, 'IMPORT' ACTION_CODE, '채널 정책 패키지 반입' BUTTON_NAME, 'POST' HTTP_METHOD, '/adm/api/channels/package/import' API_PATTERN, 40 SORT_ORDER, 'Y' USE_YN, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'REMOTE_LOG_READ' BUTTON_ID, 'REMOTE_LOG' MENU_ID, 'READ' ACTION_CODE, '로그 아티팩트 조회' BUTTON_NAME, 'GET' HTTP_METHOD, '/adm/api/remote-logs/**' API_PATTERN, 10 SORT_ORDER, 'Y' USE_YN, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'REMOTE_LOG_DOWNLOAD' BUTTON_ID, 'REMOTE_LOG' MENU_ID, 'DOWNLOAD' ACTION_CODE, '로그 아티팩트 다운로드' BUTTON_NAME, 'GET' HTTP_METHOD, '/adm/api/remote-logs/*/download' API_PATTERN, 20 SORT_ORDER, 'Y' USE_YN, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'REMOTE_LOG_BUNDLE_DOWNLOAD' BUTTON_ID, 'REMOTE_LOG' MENU_ID, 'DOWNLOAD' ACTION_CODE, '동기 로그 ZIP 다운로드' BUTTON_NAME, 'POST' HTTP_METHOD, '/adm/api/remote-logs/bundles' API_PATTERN, 30 SORT_ORDER, 'Y' USE_YN, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'REMOTE_LOG_BUNDLE_CREATE' BUTTON_ID, 'REMOTE_LOG' MENU_ID, 'CREATE' ACTION_CODE, '비동기 로그 ZIP 작업 등록' BUTTON_NAME, 'POST' HTTP_METHOD, '/adm/api/remote-logs/bundle-jobs' API_PATTERN, 40 SORT_ORDER, 'Y' USE_YN, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'REMOTE_LOG_BUNDLE_TOKEN' BUTTON_ID, 'REMOTE_LOG' MENU_ID, 'ISSUE' ACTION_CODE, '로그 ZIP 다운로드 token 발급' BUTTON_NAME, 'POST' HTTP_METHOD, '/adm/api/remote-logs/bundle-jobs/*/download-tokens' API_PATTERN, 50 SORT_ORDER, 'Y' USE_YN, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'REMOTE_LOG_JOB_DOWNLOAD' BUTTON_ID, 'REMOTE_LOG' MENU_ID, 'DOWNLOAD' ACTION_CODE, '비동기 로그 ZIP 다운로드' BUTTON_NAME, 'GET' HTTP_METHOD, '/adm/api/remote-logs/bundle-jobs/*/download' API_PATTERN, 60 SORT_ORDER, 'Y' USE_YN, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'TRANSACTION_META_READ' BUTTON_ID, 'TRANSACTION_META' MENU_ID, 'READ' ACTION_CODE, '거래 메타 조회' BUTTON_NAME, 'GET' HTTP_METHOD, '/adm/api/transactions/**' API_PATTERN, 10 SORT_ORDER, 'Y' USE_YN, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'TRANSACTION_META_SCAN' BUTTON_ID, 'TRANSACTION_META' MENU_ID, 'SCAN' ACTION_CODE, '거래 메타 스캔' BUTTON_NAME, 'POST' HTTP_METHOD, '/adm/api/transactions/scan' API_PATTERN, 20 SORT_ORDER, 'Y' USE_YN, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'TRANSACTION_META_WRITE' BUTTON_ID, 'TRANSACTION_META' MENU_ID, 'WRITE' ACTION_CODE, '거래 메타 비활성화' BUTTON_NAME, 'POST' HTTP_METHOD, '/adm/api/transactions/*/inactive' API_PATTERN, 30 SORT_ORDER, 'Y' USE_YN, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'AUDIT_LOG_READ' BUTTON_ID, 'AUDIT_LOG' MENU_ID, 'READ' ACTION_CODE, '조회' BUTTON_NAME, 'GET' HTTP_METHOD, '/adm/api/audit-logs/**' API_PATTERN, 10 SORT_ORDER, 'Y' USE_YN, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'BATCH_READ' BUTTON_ID, 'BATCH' MENU_ID, 'READ' ACTION_CODE, '조회' BUTTON_NAME, 'GET' HTTP_METHOD, '/adm/api/batch/**' API_PATTERN, 10 SORT_ORDER, 'Y' USE_YN, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'BATCH_REGISTER' BUTTON_ID, 'BATCH' MENU_ID, 'REGISTER' ACTION_CODE, '배치 등록' BUTTON_NAME, 'POST' HTTP_METHOD, '/adm/api/batch/jobs' API_PATTERN, 20 SORT_ORDER, 'Y' USE_YN, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'BATCH_EXECUTE' BUTTON_ID, 'BATCH' MENU_ID, 'EXECUTE' ACTION_CODE, '수동 실행' BUTTON_NAME, 'POST' HTTP_METHOD, '/adm/api/batch/*/run' API_PATTERN, 30 SORT_ORDER, 'Y' USE_YN, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'BATCH_RETRY' BUTTON_ID, 'BATCH' MENU_ID, 'RETRY' ACTION_CODE, '실패 재수행' BUTTON_NAME, 'POST' HTTP_METHOD, '/adm/api/batch/executions/*/retry' API_PATTERN, 40 SORT_ORDER, 'Y' USE_YN, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'BATCH_STOP' BUTTON_ID, 'BATCH' MENU_ID, 'STOP' ACTION_CODE, '실행 중지' BUTTON_NAME, 'POST' HTTP_METHOD, '/adm/api/batch/executions/*/stop' API_PATTERN, 50 SORT_ORDER, 'Y' USE_YN, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'BATCH_SCHEDULE' BUTTON_ID, 'BATCH' MENU_ID, 'SCHEDULE' ACTION_CODE, '스케줄 변경' BUTTON_NAME, 'POST' HTTP_METHOD, '/adm/api/batch/schedules/**' API_PATTERN, 60 SORT_ORDER, 'Y' USE_YN, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'BATCH_CALENDAR_SAVE' BUTTON_ID, 'BATCH' MENU_ID, 'CALENDAR_SAVE' ACTION_CODE, '영업일 저장' BUTTON_NAME, 'POST' HTTP_METHOD, '/adm/api/batch/calendar' API_PATTERN, 70 SORT_ORDER, 'Y' USE_YN, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'BATCH_SIMULATION' BUTTON_ID, 'BATCH' MENU_ID, 'SIMULATION' ACTION_CODE, '수행 시뮬레이션' BUTTON_NAME, 'GET' HTTP_METHOD, '/adm/api/batch/schedules/*/simulation' API_PATTERN, 80 SORT_ORDER, 'Y' USE_YN, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'BATCH_RELATION_READ' BUTTON_ID, 'BATCH' MENU_ID, 'RELATION_READ' ACTION_CODE, '배치 관계 조회' BUTTON_NAME, 'GET' HTTP_METHOD, '/adm/api/batch/relations' API_PATTERN, 90 SORT_ORDER, 'Y' USE_YN, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'BATCH_TARGET_READ' BUTTON_ID, 'BATCH' MENU_ID, 'TARGET_READ' ACTION_CODE, '수행 대상 조회' BUTTON_NAME, 'GET' HTTP_METHOD, '/adm/api/batch/execution-targets' API_PATTERN, 100 SORT_ORDER, 'Y' USE_YN, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'BATCH_SCHEDULER_RUN' BUTTON_ID, 'BATCH' MENU_ID, 'SCHEDULER_RUN' ACTION_CODE, '스케줄러 1회 실행' BUTTON_NAME, 'POST' HTTP_METHOD, '/adm/api/batch/scheduler/run-once' API_PATTERN, 110 SORT_ORDER, 'Y' USE_YN, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'BATCH_JOB_DETAIL' BUTTON_ID, 'BATCH' MENU_ID, 'DETAIL' ACTION_CODE, 'Job 상세 조회' BUTTON_NAME, 'GET' HTTP_METHOD, '/adm/api/batch/jobs/*' API_PATTERN, 120 SORT_ORDER, 'Y' USE_YN, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'BATCH_STEP_READ' BUTTON_ID, 'BATCH' MENU_ID, 'STEP_READ' ACTION_CODE, 'Step 이력 조회' BUTTON_NAME, 'GET' HTTP_METHOD, '/adm/api/batch/steps' API_PATTERN, 130 SORT_ORDER, 'Y' USE_YN, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'BATCH_WORKER_READ' BUTTON_ID, 'BATCH' MENU_ID, 'WORKER_READ' ACTION_CODE, 'Worker 상태 조회' BUTTON_NAME, 'GET' HTTP_METHOD, '/adm/api/batch/workers' API_PATTERN, 140 SORT_ORDER, 'Y' USE_YN, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'BATCH_LOCK_READ' BUTTON_ID, 'BATCH' MENU_ID, 'LOCK_READ' ACTION_CODE, 'Lock 조회' BUTTON_NAME, 'GET' HTTP_METHOD, '/adm/api/batch/locks' API_PATTERN, 150 SORT_ORDER, 'Y' USE_YN, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'BATCH_LOCK_RELEASE' BUTTON_ID, 'BATCH' MENU_ID, 'LOCK_RELEASE' ACTION_CODE, 'Lock 강제 해제' BUTTON_NAME, 'POST' HTTP_METHOD, '/adm/api/batch/locks/release' API_PATTERN, 160 SORT_ORDER, 'Y' USE_YN, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'BATCH_GHOST_READ' BUTTON_ID, 'BATCH' MENU_ID, 'GHOST_READ' ACTION_CODE, 'Ghost 후보 조회' BUTTON_NAME, 'GET' HTTP_METHOD, '/adm/api/batch/ghost-candidates' API_PATTERN, 170 SORT_ORDER, 'Y' USE_YN, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'BATCH_GHOST_ACTION' BUTTON_ID, 'BATCH' MENU_ID, 'GHOST_ACTION' ACTION_CODE, 'Ghost 조치' BUTTON_NAME, 'POST' HTTP_METHOD, '/adm/api/batch/ghost-candidates/*/actions' API_PATTERN, 180 SORT_ORDER, 'Y' USE_YN, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'BATCH_OPERATION_READ' BUTTON_ID, 'BATCH' MENU_ID, 'OPERATION_READ' ACTION_CODE, '운영 작업 로그 조회' BUTTON_NAME, 'GET' HTTP_METHOD, '/adm/api/batch/operations' API_PATTERN, 190 SORT_ORDER, 'Y' USE_YN, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'RELIABILITY_READ' BUTTON_ID, 'RELIABILITY' MENU_ID, 'READ' ACTION_CODE, '신뢰성 처리 조회' BUTTON_NAME, 'GET' HTTP_METHOD, '/adm/api/reliability/**' API_PATTERN, 10 SORT_ORDER, 'Y' USE_YN, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'RELIABILITY_REPLAY' BUTTON_ID, 'RELIABILITY' MENU_ID, 'REPLAY' ACTION_CODE, 'DLQ 재처리' BUTTON_NAME, 'POST' HTTP_METHOD, '/adm/api/reliability/broker/dlq/*/replay' API_PATTERN, 20 SORT_ORDER, 'Y' USE_YN, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'RELIABILITY_RESOLVE' BUTTON_ID, 'RELIABILITY' MENU_ID, 'RESOLVE' ACTION_CODE, '결과 미확정 수동 처리' BUTTON_NAME, 'POST' HTTP_METHOD, '/adm/api/reliability/unknown-results/*/resolve' API_PATTERN, 30 SORT_ORDER, 'Y' USE_YN, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'RELIABILITY_RECOVERY_RUN' BUTTON_ID, 'RELIABILITY' MENU_ID, 'RECOVERY_RUN' ACTION_CODE, 'DB 거래 로그 복구 실행' BUTTON_NAME, 'POST' HTTP_METHOD, '/adm/api/reliability/transaction-log-recovery/run' API_PATTERN, 40 SORT_ORDER, 'Y' USE_YN, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'NOTIFICATION_READ' BUTTON_ID, 'NOTIFICATION' MENU_ID, 'READ' ACTION_CODE, '알림 조회' BUTTON_NAME, 'GET' HTTP_METHOD, '/adm/api/notifications/**' API_PATTERN, 10 SORT_ORDER, 'Y' USE_YN, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'NOTIFICATION_WRITE' BUTTON_ID, 'NOTIFICATION' MENU_ID, 'WRITE' ACTION_CODE, '알림 등록/수정' BUTTON_NAME, 'POST' HTTP_METHOD, '/adm/api/notifications/**' API_PATTERN, 20 SORT_ORDER, 'Y' USE_YN, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'NOTIFICATION_DISABLE' BUTTON_ID, 'NOTIFICATION' MENU_ID, 'DISABLE' ACTION_CODE, '알림 비활성화' BUTTON_NAME, 'PUT' HTTP_METHOD, '/adm/api/notifications/rules/*/disable' API_PATTERN, 30 SORT_ORDER, 'Y' USE_YN, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'NOTIFICATION_TEST_SEND' BUTTON_ID, 'NOTIFICATION' MENU_ID, 'TEST_SEND' ACTION_CODE, '알림 테스트 발송' BUTTON_NAME, 'POST' HTTP_METHOD, '/adm/api/notifications/rules/*/test-send' API_PATTERN, 40 SORT_ORDER, 'Y' USE_YN, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'NOTIFICATION_RETRY' BUTTON_ID, 'NOTIFICATION' MENU_ID, 'RETRY' ACTION_CODE, '알림 발송 재시도' BUTTON_NAME, 'POST' HTTP_METHOD, '/adm/api/notifications/delivery-logs/*/retry' API_PATTERN, 50 SORT_ORDER, 'Y' USE_YN, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'NOTIFICATION_CANCEL' BUTTON_ID, 'NOTIFICATION' MENU_ID, 'CANCEL' ACTION_CODE, '알림 발송 취소' BUTTON_NAME, 'POST' HTTP_METHOD, '/adm/api/notifications/delivery-logs/*/cancel' API_PATTERN, 60 SORT_ORDER, 'Y' USE_YN, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'DOWNLOAD_READ' BUTTON_ID, 'DOWNLOAD' MENU_ID, 'READ' ACTION_CODE, '다운로드 감사 조회' BUTTON_NAME, 'GET' HTTP_METHOD, '/adm/api/downloads/**' API_PATTERN, 10 SORT_ORDER, 'Y' USE_YN, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'DOWNLOAD_EXECUTE' BUTTON_ID, 'DOWNLOAD' MENU_ID, 'DOWNLOAD' ACTION_CODE, 'CSV 다운로드' BUTTON_NAME, 'POST' HTTP_METHOD, '/adm/api/downloads/csv' API_PATTERN, 20 SORT_ORDER, 'Y' USE_YN, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'CACHE_READ' BUTTON_ID, 'CACHE' MENU_ID, 'READ' ACTION_CODE, '조회' BUTTON_NAME, 'GET' HTTP_METHOD, '/adm/api/cache/**' API_PATTERN, 10 SORT_ORDER, 'Y' USE_YN, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'CACHE_REFRESH' BUTTON_ID, 'CACHE' MENU_ID, 'REFRESH' ACTION_CODE, '캐시 갱신' BUTTON_NAME, 'POST' HTTP_METHOD, '/adm/api/cache/**' API_PATTERN, 20 SORT_ORDER, 'Y' USE_YN, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'CACHE_EVICT_KEY' BUTTON_ID, 'CACHE' MENU_ID, 'EVICT_KEY' ACTION_CODE, '단일 Cache 제거' BUTTON_NAME, 'POST' HTTP_METHOD, '/adm/api/cache/evict-key' API_PATTERN, 30 SORT_ORDER, 'Y' USE_YN, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'CACHE_EVICT_NAMESPACE' BUTTON_ID, 'CACHE' MENU_ID, 'EVICT_NAMESPACE' ACTION_CODE, 'Namespace Cache 제거' BUTTON_NAME, 'POST' HTTP_METHOD, '/adm/api/cache/evict-namespace' API_PATTERN, 40 SORT_ORDER, 'Y' USE_YN, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'CACHE_RECONCILE' BUTTON_ID, 'CACHE' MENU_ID, 'RECONCILE' ACTION_CODE, 'Cache Durable 재조정' BUTTON_NAME, 'POST' HTTP_METHOD, '/adm/api/cache/reconcile' API_PATTERN, 50 SORT_ORDER, 'Y' USE_YN, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'FILE_JOB_READ' BUTTON_ID, 'FILE_JOB' MENU_ID, 'READ' ACTION_CODE, 'File Job 조회' BUTTON_NAME, 'GET' HTTP_METHOD, '/adm/api/file-jobs/**' API_PATTERN, 10 SORT_ORDER, 'Y' USE_YN, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'FILE_JOB_UPLOAD' BUTTON_ID, 'FILE_JOB' MENU_ID, 'UPLOAD' ACTION_CODE, 'Upload 접수' BUTTON_NAME, 'POST' HTTP_METHOD, '/adm/api/file-jobs/uploads' API_PATTERN, 20 SORT_ORDER, 'Y' USE_YN, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'FILE_JOB_APPLY' BUTTON_ID, 'FILE_JOB' MENU_ID, 'APPLY' ACTION_CODE, '검증 Job 적용' BUTTON_NAME, 'POST' HTTP_METHOD, '/adm/api/file-jobs/*/apply' API_PATTERN, 30 SORT_ORDER, 'Y' USE_YN, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'FILE_JOB_RETRY' BUTTON_ID, 'FILE_JOB' MENU_ID, 'RETRY' ACTION_CODE, 'File Job 재시도' BUTTON_NAME, 'POST' HTTP_METHOD, '/adm/api/file-jobs/*/retry' API_PATTERN, 40 SORT_ORDER, 'Y' USE_YN, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'FILE_JOB_CANCEL' BUTTON_ID, 'FILE_JOB' MENU_ID, 'CANCEL' ACTION_CODE, 'File Job 취소' BUTTON_NAME, 'POST' HTTP_METHOD, '/adm/api/file-jobs/*/cancel' API_PATTERN, 50 SORT_ORDER, 'Y' USE_YN, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'FILE_JOB_ROLLBACK' BUTTON_ID, 'FILE_JOB' MENU_ID, 'ROLLBACK' ACTION_CODE, 'File Job Rollback' BUTTON_NAME, 'POST' HTTP_METHOD, '/adm/api/file-jobs/*/rollback' API_PATTERN, 60 SORT_ORDER, 'Y' USE_YN, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'FILE_JOB_DOWNLOAD' BUTTON_ID, 'FILE_JOB' MENU_ID, 'DOWNLOAD' ACTION_CODE, 'Artifact 다운로드' BUTTON_NAME, 'GET' HTTP_METHOD, '/adm/api/file-jobs/*/artifact' API_PATTERN, 70 SORT_ORDER, 'Y' USE_YN, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'FILE_JOB_RESOLVE' BUTTON_ID, 'FILE_JOB' MENU_ID, 'RESOLVE' ACTION_CODE, '결과 불명 확정' BUTTON_NAME, 'POST' HTTP_METHOD, '/adm/api/file-jobs/*/resolve-unknown' API_PATTERN, 80 SORT_ORDER, 'Y' USE_YN, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'MESSAGE_READ' BUTTON_ID, 'MESSAGE' MENU_ID, 'READ' ACTION_CODE, '조회' BUTTON_NAME, 'GET' HTTP_METHOD, '/adm/api/messages/**' API_PATTERN, 10 SORT_ORDER, 'Y' USE_YN, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'MESSAGE_WRITE' BUTTON_ID, 'MESSAGE' MENU_ID, 'WRITE' ACTION_CODE, '등록/수정' BUTTON_NAME, 'POST' HTTP_METHOD, '/adm/api/messages/**' API_PATTERN, 20 SORT_ORDER, 'Y' USE_YN, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'MESSAGE_DISABLE' BUTTON_ID, 'MESSAGE' MENU_ID, 'DISABLE' ACTION_CODE, '비활성' BUTTON_NAME, 'DELETE' HTTP_METHOD, '/adm/api/messages/**' API_PATTERN, 30 SORT_ORDER, 'Y' USE_YN, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'CODE_READ' BUTTON_ID, 'CODE' MENU_ID, 'READ' ACTION_CODE, '조회' BUTTON_NAME, 'GET' HTTP_METHOD, '/adm/api/codes/**' API_PATTERN, 10 SORT_ORDER, 'Y' USE_YN, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'CODE_WRITE' BUTTON_ID, 'CODE' MENU_ID, 'WRITE' ACTION_CODE, '등록/수정' BUTTON_NAME, 'POST' HTTP_METHOD, '/adm/api/codes/**' API_PATTERN, 20 SORT_ORDER, 'Y' USE_YN, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'CODE_DISABLE' BUTTON_ID, 'CODE' MENU_ID, 'DISABLE' ACTION_CODE, '비활성' BUTTON_NAME, 'DELETE' HTTP_METHOD, '/adm/api/codes/**' API_PATTERN, 30 SORT_ORDER, 'Y' USE_YN, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'RESPONSE_CODE_READ' BUTTON_ID, 'RESPONSE_CODE' MENU_ID, 'READ' ACTION_CODE, '조회' BUTTON_NAME, 'GET' HTTP_METHOD, '/adm/api/response-codes/**' API_PATTERN, 10 SORT_ORDER, 'Y' USE_YN, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'RESPONSE_CODE_WRITE' BUTTON_ID, 'RESPONSE_CODE' MENU_ID, 'WRITE' ACTION_CODE, '등록/수정' BUTTON_NAME, 'POST' HTTP_METHOD, '/adm/api/response-codes/**' API_PATTERN, 20 SORT_ORDER, 'Y' USE_YN, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'CONFIG_READ' BUTTON_ID, 'CONFIG' MENU_ID, 'READ' ACTION_CODE, '조회' BUTTON_NAME, 'GET' HTTP_METHOD, '/adm/api/configs/**' API_PATTERN, 10 SORT_ORDER, 'Y' USE_YN, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'CONFIG_WRITE' BUTTON_ID, 'CONFIG' MENU_ID, 'WRITE' ACTION_CODE, '수정' BUTTON_NAME, 'POST' HTTP_METHOD, '/adm/api/configs/**' API_PATTERN, 20 SORT_ORDER, 'Y' USE_YN, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'DYNAMIC_LOG_READ' BUTTON_ID, 'DYNAMIC_LOG' MENU_ID, 'READ' ACTION_CODE, '조회' BUTTON_NAME, 'GET' HTTP_METHOD, '/adm/api/log-level/**' API_PATTERN, 10 SORT_ORDER, 'Y' USE_YN, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'DYNAMIC_LOG_WRITE' BUTTON_ID, 'DYNAMIC_LOG' MENU_ID, 'WRITE' ACTION_CODE, '적용' BUTTON_NAME, 'POST' HTTP_METHOD, '/adm/api/log-level/**' API_PATTERN, 20 SORT_ORDER, 'Y' USE_YN, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'LOG_POLICY_READ' BUTTON_ID, 'LOG_POLICY' MENU_ID, 'READ' ACTION_CODE, '조회' BUTTON_NAME, 'GET' HTTP_METHOD, '/adm/api/log-policies/**' API_PATTERN, 10 SORT_ORDER, 'Y' USE_YN, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'LOG_POLICY_WRITE' BUTTON_ID, 'LOG_POLICY' MENU_ID, 'WRITE' ACTION_CODE, '등록/수정' BUTTON_NAME, 'POST' HTTP_METHOD, '/adm/api/log-policies/**' API_PATTERN, 20 SORT_ORDER, 'Y' USE_YN, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'LOG_POLICY_OVERRIDE' BUTTON_ID, 'LOG_POLICY' MENU_ID, 'OVERRIDE' ACTION_CODE, '임시 override' BUTTON_NAME, 'POST' HTTP_METHOD, '/adm/api/log-policies/overrides' API_PATTERN, 30 SORT_ORDER, 'Y' USE_YN, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'LOG_POLICY_CACHE_REFRESH' BUTTON_ID, 'LOG_POLICY' MENU_ID, 'CACHE_REFRESH' ACTION_CODE, '정책 캐시 새로고침' BUTTON_NAME, 'POST' HTTP_METHOD, '/adm/api/log-policies/cache/refresh' API_PATTERN, 40 SORT_ORDER, 'Y' USE_YN, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'LOG_POLICY_CACHE_CLEAR' BUTTON_ID, 'LOG_POLICY' MENU_ID, 'CACHE_CLEAR' ACTION_CODE, '정책 캐시 전체 삭제' BUTTON_NAME, 'POST' HTTP_METHOD, '/adm/api/log-policies/cache/clear' API_PATTERN, 50 SORT_ORDER, 'Y' USE_YN, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'PASSWORD_READ' BUTTON_ID, 'PASSWORD' MENU_ID, 'READ' ACTION_CODE, '정책 조회' BUTTON_NAME, 'GET' HTTP_METHOD, '/adm/api/operators/password-policy/**' API_PATTERN, 10 SORT_ORDER, 'Y' USE_YN, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'PASSWORD_RESET' BUTTON_ID, 'PASSWORD' MENU_ID, 'RESET_PASSWORD' ACTION_CODE, '비밀번호 초기화' BUTTON_NAME, 'POST' HTTP_METHOD, '/adm/api/operators/*/password/reset' API_PATTERN, 20 SORT_ORDER, 'Y' USE_YN, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'PASSWORD_UNLOCK' BUTTON_ID, 'PASSWORD' MENU_ID, 'UNLOCK' ACTION_CODE, '잠금 해제' BUTTON_NAME, 'POST' HTTP_METHOD, '/adm/api/operators/*/unlock' API_PATTERN, 30 SORT_ORDER, 'Y' USE_YN, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'PASSWORD_SESSION_REVOKE' BUTTON_ID, 'PASSWORD' MENU_ID, 'REVOKE_SESSION' ACTION_CODE, '세션 강제 종료' BUTTON_NAME, 'POST' HTTP_METHOD, '/adm/api/operators/sessions/*/revoke' API_PATTERN, 40 SORT_ORDER, 'Y' USE_YN, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'SECURITY_READ' BUTTON_ID, 'SECURITY' MENU_ID, 'READ' ACTION_CODE, '조회' BUTTON_NAME, 'GET' HTTP_METHOD, '/adm/api/security/**' API_PATTERN, 10 SORT_ORDER, 'Y' USE_YN, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'SECURITY_WRITE' BUTTON_ID, 'SECURITY' MENU_ID, 'WRITE' ACTION_CODE, '보안 설정 변경' BUTTON_NAME, 'POST' HTTP_METHOD, '/adm/api/security/**' API_PATTERN, 20 SORT_ORDER, 'Y' USE_YN, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'PERMISSION_READ' BUTTON_ID, 'PERMISSION' MENU_ID, 'READ' ACTION_CODE, '조회' BUTTON_NAME, 'GET' HTTP_METHOD, '/adm/api/permissions/**' API_PATTERN, 10 SORT_ORDER, 'Y' USE_YN, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'PERMISSION_WRITE' BUTTON_ID, 'PERMISSION' MENU_ID, 'WRITE' ACTION_CODE, '권한 변경' BUTTON_NAME, 'POST' HTTP_METHOD, '/adm/api/permissions/**' API_PATTERN, 20 SORT_ORDER, 'Y' USE_YN, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'OPERATOR_READ' BUTTON_ID, 'OPERATOR' MENU_ID, 'READ' ACTION_CODE, '조회' BUTTON_NAME, 'GET' HTTP_METHOD, '/adm/api/operators/**' API_PATTERN, 10 SORT_ORDER, 'Y' USE_YN, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'OPERATOR_CREATE' BUTTON_ID, 'OPERATOR' MENU_ID, 'CREATE' ACTION_CODE, '운영자 등록' BUTTON_NAME, 'POST' HTTP_METHOD, '/adm/api/operators' API_PATTERN, 20 SORT_ORDER, 'Y' USE_YN, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'OPERATOR_ROLE_UPDATE' BUTTON_ID, 'OPERATOR' MENU_ID, 'ROLE_UPDATE' ACTION_CODE, '역할 부여' BUTTON_NAME, 'PUT' HTTP_METHOD, '/adm/api/operators/*/roles' API_PATTERN, 30 SORT_ORDER, 'Y' USE_YN, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'OPERATOR_STATUS_UPDATE' BUTTON_ID, 'OPERATOR' MENU_ID, 'STATUS_UPDATE' ACTION_CODE, '계정 상태 변경' BUTTON_NAME, 'PUT' HTTP_METHOD, '/adm/api/operators/*/status' API_PATTERN, 40 SORT_ORDER, 'Y' USE_YN, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'OPERATOR_CONTACT_UPDATE' BUTTON_ID, 'OPERATOR' MENU_ID, 'CONTACT_UPDATE' ACTION_CODE, '연락처 변경' BUTTON_NAME, 'PUT' HTTP_METHOD, '/adm/api/operators/*/contacts' API_PATTERN, 50 SORT_ORDER, 'Y' USE_YN, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'OPERATOR_PII_RAW' BUTTON_ID, 'OPERATOR' MENU_ID, 'PII_RAW' ACTION_CODE, '연락처 원문 조회' BUTTON_NAME, 'POST' HTTP_METHOD, '/adm/api/operators/*/contacts/raw' API_PATTERN, 60 SORT_ORDER, 'Y' USE_YN, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
) src ON (tgt.BUTTON_ID = src.BUTTON_ID)
WHEN MATCHED THEN UPDATE SET tgt.MENU_ID = src.MENU_ID, tgt.ACTION_CODE = src.ACTION_CODE, tgt.BUTTON_NAME = src.BUTTON_NAME, tgt.HTTP_METHOD = src.HTTP_METHOD, tgt.API_PATTERN = src.API_PATTERN, tgt.SORT_ORDER = src.SORT_ORDER, tgt.USE_YN = src.USE_YN, tgt.updated_by = src.updated_by, tgt.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (BUTTON_ID, MENU_ID, ACTION_CODE, BUTTON_NAME, HTTP_METHOD, API_PATTERN, SORT_ORDER, USE_YN, created_by, updated_by) VALUES (src.BUTTON_ID, src.MENU_ID, src.ACTION_CODE, src.BUTTON_NAME, src.HTTP_METHOD, src.API_PATTERN, src.SORT_ORDER, src.USE_YN, src.created_by, src.updated_by);
MERGE INTO adm_password_policy tgt USING (
SELECT 'DEFAULT' POLICY_ID, 12 MIN_LENGTH, 'Y' REQUIRE_UPPER_YN, 'Y' REQUIRE_LOWER_YN, 'Y' REQUIRE_DIGIT_YN, 'Y' REQUIRE_SPECIAL_YN, 5 MAX_FAIL_COUNT, 90 EXPIRE_DAYS, 5 HISTORY_LIMIT, 'Y' USE_YN, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
) src ON (tgt.POLICY_ID = src.POLICY_ID)
WHEN MATCHED THEN UPDATE SET tgt.MIN_LENGTH = src.MIN_LENGTH, tgt.REQUIRE_UPPER_YN = src.REQUIRE_UPPER_YN, tgt.REQUIRE_LOWER_YN = src.REQUIRE_LOWER_YN, tgt.REQUIRE_DIGIT_YN = src.REQUIRE_DIGIT_YN, tgt.REQUIRE_SPECIAL_YN = src.REQUIRE_SPECIAL_YN, tgt.MAX_FAIL_COUNT = src.MAX_FAIL_COUNT, tgt.EXPIRE_DAYS = src.EXPIRE_DAYS, tgt.HISTORY_LIMIT = src.HISTORY_LIMIT, tgt.USE_YN = src.USE_YN, tgt.updated_by = src.updated_by, tgt.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (POLICY_ID, MIN_LENGTH, REQUIRE_UPPER_YN, REQUIRE_LOWER_YN, REQUIRE_DIGIT_YN, REQUIRE_SPECIAL_YN, MAX_FAIL_COUNT, EXPIRE_DAYS, HISTORY_LIMIT, USE_YN, created_by, updated_by) VALUES (src.POLICY_ID, src.MIN_LENGTH, src.REQUIRE_UPPER_YN, src.REQUIRE_LOWER_YN, src.REQUIRE_DIGIT_YN, src.REQUIRE_SPECIAL_YN, src.MAX_FAIL_COUNT, src.EXPIRE_DAYS, src.HISTORY_LIMIT, src.USE_YN, src.created_by, src.updated_by);
MERGE INTO adm_role_menu tgt USING (
SELECT 'ADM_ADMIN' ROLE_ID, MENU_ID MENU_ID, 'Y' READ_YN, 'Y' WRITE_YN, 'Y' DELETE_YN, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM adm_menu
) src ON (tgt.ROLE_ID = src.ROLE_ID AND tgt.MENU_ID = src.MENU_ID)
WHEN MATCHED THEN UPDATE SET tgt.READ_YN = src.READ_YN, tgt.WRITE_YN = src.WRITE_YN, tgt.DELETE_YN = src.DELETE_YN, tgt.updated_by = src.updated_by, tgt.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (ROLE_ID, MENU_ID, READ_YN, WRITE_YN, DELETE_YN, created_by, updated_by) VALUES (src.ROLE_ID, src.MENU_ID, src.READ_YN, src.WRITE_YN, src.DELETE_YN, src.created_by, src.updated_by);
MERGE INTO adm_role_menu tgt USING (
SELECT 'ADM_DEV_OPERATOR' ROLE_ID, MENU_ID MENU_ID, 'Y' READ_YN, CASE WHEN MENU_ID IN ('TRANSACTION_META', 'CHANNEL_POLICY', 'REMOTE_LOG', 'BATCH', 'RELIABILITY', 'NOTIFICATION', 'DOWNLOAD', 'CACHE', 'FILE_JOB', 'MESSAGE', 'CODE', 'RESPONSE_CODE', 'CONFIG', 'DYNAMIC_LOG', 'LOG_POLICY') THEN 'Y' ELSE 'N' END WRITE_YN, CASE WHEN MENU_ID IN ('TRANSACTION_META', 'MESSAGE', 'CODE', 'DYNAMIC_LOG', 'LOG_POLICY') THEN 'Y' ELSE 'N' END DELETE_YN, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM adm_menu
WHERE MENU_ID NOT IN ('OPERATOR', 'PERMISSION', 'PASSWORD', 'SECURITY')
) src ON (tgt.ROLE_ID = src.ROLE_ID AND tgt.MENU_ID = src.MENU_ID)
WHEN MATCHED THEN UPDATE SET tgt.READ_YN = src.READ_YN, tgt.WRITE_YN = src.WRITE_YN, tgt.DELETE_YN = src.DELETE_YN, tgt.updated_by = src.updated_by, tgt.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (ROLE_ID, MENU_ID, READ_YN, WRITE_YN, DELETE_YN, created_by, updated_by) VALUES (src.ROLE_ID, src.MENU_ID, src.READ_YN, src.WRITE_YN, src.DELETE_YN, src.created_by, src.updated_by);
MERGE INTO adm_role_menu tgt USING (
SELECT 'ADM_BIZ_OPERATOR' ROLE_ID, MENU_ID MENU_ID, 'Y' READ_YN, CASE WHEN MENU_ID IN ('BATCH', 'DOWNLOAD', 'CACHE', 'FILE_JOB') THEN 'Y' ELSE 'N' END WRITE_YN, 'N' DELETE_YN, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM adm_menu
WHERE MENU_ID IN ('DASHBOARD', 'LOG_LIST', 'STANDARD_EXECUTION', 'CHANNEL_POLICY', 'REMOTE_LOG', 'TRANSACTION_META', 'AUDIT_LOG', 'BATCH', 'RELIABILITY', 'NOTIFICATION', 'DOWNLOAD', 'CACHE', 'FILE_JOB', 'MESSAGE', 'CODE')
) src ON (tgt.ROLE_ID = src.ROLE_ID AND tgt.MENU_ID = src.MENU_ID)
WHEN MATCHED THEN UPDATE SET tgt.READ_YN = src.READ_YN, tgt.WRITE_YN = src.WRITE_YN, tgt.DELETE_YN = src.DELETE_YN, tgt.updated_by = src.updated_by, tgt.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (ROLE_ID, MENU_ID, READ_YN, WRITE_YN, DELETE_YN, created_by, updated_by) VALUES (src.ROLE_ID, src.MENU_ID, src.READ_YN, src.WRITE_YN, src.DELETE_YN, src.created_by, src.updated_by);
MERGE INTO adm_role_menu tgt USING (
SELECT 'ADM_VIEWER' ROLE_ID, MENU_ID MENU_ID, 'Y' READ_YN, 'N' WRITE_YN, 'N' DELETE_YN, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM adm_menu
WHERE MENU_ID IN ('DASHBOARD', 'LOG_LIST', 'STANDARD_EXECUTION', 'CHANNEL_POLICY', 'REMOTE_LOG', 'TRANSACTION_META', 'AUDIT_LOG', 'BATCH', 'RELIABILITY', 'NOTIFICATION', 'DOWNLOAD', 'CACHE', 'FILE_JOB', 'MESSAGE', 'CODE', 'RESPONSE_CODE', 'CONFIG', 'LOG_POLICY')
) src ON (tgt.ROLE_ID = src.ROLE_ID AND tgt.MENU_ID = src.MENU_ID)
WHEN MATCHED THEN UPDATE SET tgt.READ_YN = src.READ_YN, tgt.WRITE_YN = src.WRITE_YN, tgt.DELETE_YN = src.DELETE_YN, tgt.updated_by = src.updated_by, tgt.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (ROLE_ID, MENU_ID, READ_YN, WRITE_YN, DELETE_YN, created_by, updated_by) VALUES (src.ROLE_ID, src.MENU_ID, src.READ_YN, src.WRITE_YN, src.DELETE_YN, src.created_by, src.updated_by);
MERGE INTO adm_role_menu tgt USING (
SELECT 'ADM_OPERATOR' ROLE_ID, MENU_ID MENU_ID, READ_YN READ_YN, WRITE_YN WRITE_YN, DELETE_YN DELETE_YN, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM adm_role_menu
WHERE ROLE_ID = 'ADM_DEV_OPERATOR'
) src ON (tgt.ROLE_ID = src.ROLE_ID AND tgt.MENU_ID = src.MENU_ID)
WHEN MATCHED THEN UPDATE SET tgt.READ_YN = src.READ_YN, tgt.WRITE_YN = src.WRITE_YN, tgt.DELETE_YN = src.DELETE_YN, tgt.updated_by = src.updated_by, tgt.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (ROLE_ID, MENU_ID, READ_YN, WRITE_YN, DELETE_YN, created_by, updated_by) VALUES (src.ROLE_ID, src.MENU_ID, src.READ_YN, src.WRITE_YN, src.DELETE_YN, src.created_by, src.updated_by);
MERGE INTO adm_role_button tgt USING (
SELECT 'ADM_ADMIN' ROLE_ID, BUTTON_ID BUTTON_ID, 'Y' ALLOW_YN, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM adm_button
) src ON (tgt.ROLE_ID = src.ROLE_ID AND tgt.BUTTON_ID = src.BUTTON_ID)
WHEN MATCHED THEN UPDATE SET tgt.ALLOW_YN = src.ALLOW_YN, tgt.updated_by = src.updated_by, tgt.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (ROLE_ID, BUTTON_ID, ALLOW_YN, created_by, updated_by) VALUES (src.ROLE_ID, src.BUTTON_ID, src.ALLOW_YN, src.created_by, src.updated_by);
MERGE INTO adm_role_button tgt USING (
SELECT 'ADM_DEV_OPERATOR' ROLE_ID, BUTTON_ID BUTTON_ID, CASE WHEN MENU_ID IN ('OPERATOR', 'PERMISSION', 'PASSWORD', 'SECURITY') THEN 'N' ELSE 'Y' END ALLOW_YN, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM adm_button
) src ON (tgt.ROLE_ID = src.ROLE_ID AND tgt.BUTTON_ID = src.BUTTON_ID)
WHEN MATCHED THEN UPDATE SET tgt.ALLOW_YN = src.ALLOW_YN, tgt.updated_by = src.updated_by, tgt.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (ROLE_ID, BUTTON_ID, ALLOW_YN, created_by, updated_by) VALUES (src.ROLE_ID, src.BUTTON_ID, src.ALLOW_YN, src.created_by, src.updated_by);
MERGE INTO adm_role_button tgt USING (
SELECT 'ADM_BIZ_OPERATOR' ROLE_ID, BUTTON_ID BUTTON_ID, CASE
           WHEN BUTTON_ID IN ('BATCH_EXECUTE', 'BATCH_RETRY', 'BATCH_SIMULATION', 'BATCH_RELATION_READ', 'BATCH_TARGET_READ', 'BATCH_SCHEDULER_RUN', 'DOWNLOAD_EXECUTE', 'CACHE_REFRESH', 'FILE_JOB_UPLOAD', 'FILE_JOB_APPLY', 'FILE_JOB_DOWNLOAD') THEN 'Y'
           WHEN ACTION_CODE IN ('READ', 'DETAIL') AND MENU_ID IN ('LOG_LIST', 'REMOTE_LOG', 'TRANSACTION_META', 'AUDIT_LOG', 'BATCH', 'RELIABILITY', 'NOTIFICATION', 'DOWNLOAD', 'CACHE', 'FILE_JOB', 'MESSAGE', 'CODE', 'LOG_POLICY') THEN 'Y'
           ELSE 'N'
       END ALLOW_YN, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM adm_button
) src ON (tgt.ROLE_ID = src.ROLE_ID AND tgt.BUTTON_ID = src.BUTTON_ID)
WHEN MATCHED THEN UPDATE SET tgt.ALLOW_YN = src.ALLOW_YN, tgt.updated_by = src.updated_by, tgt.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (ROLE_ID, BUTTON_ID, ALLOW_YN, created_by, updated_by) VALUES (src.ROLE_ID, src.BUTTON_ID, src.ALLOW_YN, src.created_by, src.updated_by);
MERGE INTO adm_role_button tgt USING (
SELECT 'ADM_VIEWER' ROLE_ID, BUTTON_ID BUTTON_ID, CASE WHEN ACTION_CODE IN ('READ', 'DETAIL') THEN 'Y' ELSE 'N' END ALLOW_YN, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM adm_button
) src ON (tgt.ROLE_ID = src.ROLE_ID AND tgt.BUTTON_ID = src.BUTTON_ID)
WHEN MATCHED THEN UPDATE SET tgt.ALLOW_YN = src.ALLOW_YN, tgt.updated_by = src.updated_by, tgt.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (ROLE_ID, BUTTON_ID, ALLOW_YN, created_by, updated_by) VALUES (src.ROLE_ID, src.BUTTON_ID, src.ALLOW_YN, src.created_by, src.updated_by);
MERGE INTO adm_role_button tgt USING (
SELECT 'ADM_OPERATOR' ROLE_ID, BUTTON_ID BUTTON_ID, ALLOW_YN ALLOW_YN, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM adm_role_button
WHERE ROLE_ID = 'ADM_DEV_OPERATOR'
) src ON (tgt.ROLE_ID = src.ROLE_ID AND tgt.BUTTON_ID = src.BUTTON_ID)
WHEN MATCHED THEN UPDATE SET tgt.ALLOW_YN = src.ALLOW_YN, tgt.updated_by = src.updated_by, tgt.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (ROLE_ID, BUTTON_ID, ALLOW_YN, created_by, updated_by) VALUES (src.ROLE_ID, src.BUTTON_ID, src.ALLOW_YN, src.created_by, src.updated_by);
MERGE INTO adm_api_permission tgt USING (
SELECT ('API_' || BUTTON_ID) API_PERMISSION_ID, MENU_ID API_GROUP_CODE, COALESCE(HTTP_METHOD, 'ANY') HTTP_METHOD, API_PATTERN API_PATH, BUTTON_NAME API_NAME, ACTION_CODE PERMISSION_CODE, MENU_ID MENU_ID, BUTTON_ID BUTTON_ID, USE_YN USE_YN, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM adm_button
WHERE API_PATTERN IS NOT NULL
) src ON (tgt.API_PERMISSION_ID = src.API_PERMISSION_ID)
WHEN MATCHED THEN UPDATE SET tgt.API_GROUP_CODE = src.API_GROUP_CODE, tgt.HTTP_METHOD = src.HTTP_METHOD, tgt.API_PATH = src.API_PATH, tgt.API_NAME = src.API_NAME, tgt.PERMISSION_CODE = src.PERMISSION_CODE, tgt.MENU_ID = src.MENU_ID, tgt.BUTTON_ID = src.BUTTON_ID, tgt.USE_YN = src.USE_YN, tgt.updated_by = src.updated_by, tgt.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (API_PERMISSION_ID, API_GROUP_CODE, HTTP_METHOD, API_PATH, API_NAME, PERMISSION_CODE, MENU_ID, BUTTON_ID, USE_YN, created_by, updated_by) VALUES (src.API_PERMISSION_ID, src.API_GROUP_CODE, src.HTTP_METHOD, src.API_PATH, src.API_NAME, src.PERMISSION_CODE, src.MENU_ID, src.BUTTON_ID, src.USE_YN, src.created_by, src.updated_by);
MERGE INTO adm_api_permission tgt USING (
SELECT 'API_PERMISSION_WRITE_PUT' API_PERMISSION_ID, 'PERMISSION' API_GROUP_CODE, 'PUT' HTTP_METHOD, '/adm/api/permissions/**' API_PATH, '권한 변경' API_NAME, 'WRITE' PERMISSION_CODE, 'PERMISSION' MENU_ID, 'PERMISSION_WRITE' BUTTON_ID, 'Y' USE_YN, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
) src ON (tgt.API_PERMISSION_ID = src.API_PERMISSION_ID)
WHEN MATCHED THEN UPDATE SET tgt.API_GROUP_CODE = src.API_GROUP_CODE, tgt.HTTP_METHOD = src.HTTP_METHOD, tgt.API_PATH = src.API_PATH, tgt.API_NAME = src.API_NAME, tgt.PERMISSION_CODE = src.PERMISSION_CODE, tgt.MENU_ID = src.MENU_ID, tgt.BUTTON_ID = src.BUTTON_ID, tgt.USE_YN = src.USE_YN, tgt.updated_by = src.updated_by, tgt.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (API_PERMISSION_ID, API_GROUP_CODE, HTTP_METHOD, API_PATH, API_NAME, PERMISSION_CODE, MENU_ID, BUTTON_ID, USE_YN, created_by, updated_by) VALUES (src.API_PERMISSION_ID, src.API_GROUP_CODE, src.HTTP_METHOD, src.API_PATH, src.API_NAME, src.PERMISSION_CODE, src.MENU_ID, src.BUTTON_ID, src.USE_YN, src.created_by, src.updated_by);
MERGE INTO adm_role_api_permission tgt USING (
SELECT rb.ROLE_ID ROLE_ID, ap.API_PERMISSION_ID API_PERMISSION_ID, rb.ALLOW_YN ALLOW_YN, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM adm_role_button rb
JOIN adm_api_permission ap ON ap.BUTTON_ID = rb.BUTTON_ID
) src ON (tgt.ROLE_ID = src.ROLE_ID AND tgt.API_PERMISSION_ID = src.API_PERMISSION_ID)
WHEN MATCHED THEN UPDATE SET tgt.ALLOW_YN = src.ALLOW_YN, tgt.updated_by = src.updated_by, tgt.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (ROLE_ID, API_PERMISSION_ID, ALLOW_YN, created_by, updated_by) VALUES (src.ROLE_ID, src.API_PERMISSION_ID, src.ALLOW_YN, src.created_by, src.updated_by);
MERGE INTO adm_button tgt USING (
SELECT 'AUDIT_LOG_RETRY' BUTTON_ID, 'AUDIT_LOG' MENU_ID, 'WRITE' ACTION_CODE, '감사 전달 재처리' BUTTON_NAME, 'POST' HTTP_METHOD, '/adm/api/audit-logs/deliveries/*/retry' API_PATTERN, 20 SORT_ORDER, 'Y' USE_YN, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
) src ON (tgt.BUTTON_ID = src.BUTTON_ID)
WHEN MATCHED THEN UPDATE SET tgt.ACTION_CODE = src.ACTION_CODE, tgt.BUTTON_NAME = src.BUTTON_NAME, tgt.HTTP_METHOD = src.HTTP_METHOD, tgt.API_PATTERN = src.API_PATTERN, tgt.USE_YN = src.USE_YN, tgt.updated_by = src.updated_by, tgt.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (BUTTON_ID, MENU_ID, ACTION_CODE, BUTTON_NAME, HTTP_METHOD, API_PATTERN, SORT_ORDER, USE_YN, created_by, updated_by) VALUES (src.BUTTON_ID, src.MENU_ID, src.ACTION_CODE, src.BUTTON_NAME, src.HTTP_METHOD, src.API_PATTERN, src.SORT_ORDER, src.USE_YN, src.created_by, src.updated_by);
UPDATE adm_role_menu SET WRITE_YN='Y',updated_by='SYSTEM',updated_at=CURRENT_TIMESTAMP WHERE MENU_ID='AUDIT_LOG' AND ROLE_ID IN ('ADM_ADMIN','ADM_DEV_OPERATOR');
MERGE INTO adm_role_button tgt USING (
SELECT ROLE_ID ROLE_ID, 'AUDIT_LOG_RETRY' BUTTON_ID, 'Y' ALLOW_YN, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM adm_role WHERE ROLE_ID IN ('ADM_ADMIN','ADM_DEV_OPERATOR')
) src ON (tgt.ROLE_ID = src.ROLE_ID AND tgt.BUTTON_ID = src.BUTTON_ID)
WHEN MATCHED THEN UPDATE SET tgt.ALLOW_YN = 'Y', tgt.updated_by = 'SYSTEM', tgt.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (ROLE_ID, BUTTON_ID, ALLOW_YN, created_by, updated_by) VALUES (src.ROLE_ID, src.BUTTON_ID, src.ALLOW_YN, src.created_by, src.updated_by);
MERGE INTO adm_api_permission tgt USING (
SELECT 'API_AUDIT_LOG_RETRY' API_PERMISSION_ID, 'AUDIT_LOG' API_GROUP_CODE, 'POST' HTTP_METHOD, '/adm/api/audit-logs/deliveries/*/retry' API_PATH, '감사 전달 재처리' API_NAME, 'WRITE' PERMISSION_CODE, 'AUDIT_LOG' MENU_ID, 'AUDIT_LOG_RETRY' BUTTON_ID, 'Y' USE_YN, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
) src ON (tgt.API_PERMISSION_ID = src.API_PERMISSION_ID)
WHEN MATCHED THEN UPDATE SET tgt.HTTP_METHOD = src.HTTP_METHOD, tgt.API_PATH = src.API_PATH, tgt.PERMISSION_CODE = src.PERMISSION_CODE, tgt.BUTTON_ID = src.BUTTON_ID, tgt.USE_YN = src.USE_YN, tgt.updated_by = src.updated_by, tgt.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (API_PERMISSION_ID, API_GROUP_CODE, HTTP_METHOD, API_PATH, API_NAME, PERMISSION_CODE, MENU_ID, BUTTON_ID, USE_YN, created_by, updated_by) VALUES (src.API_PERMISSION_ID, src.API_GROUP_CODE, src.HTTP_METHOD, src.API_PATH, src.API_NAME, src.PERMISSION_CODE, src.MENU_ID, src.BUTTON_ID, src.USE_YN, src.created_by, src.updated_by);
MERGE INTO adm_role_api_permission tgt USING (
SELECT ROLE_ID ROLE_ID, 'API_AUDIT_LOG_RETRY' API_PERMISSION_ID, 'Y' ALLOW_YN, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM adm_role WHERE ROLE_ID IN ('ADM_ADMIN','ADM_DEV_OPERATOR')
) src ON (tgt.ROLE_ID = src.ROLE_ID AND tgt.API_PERMISSION_ID = src.API_PERMISSION_ID)
WHEN MATCHED THEN UPDATE SET tgt.ALLOW_YN = 'Y', tgt.updated_by = 'SYSTEM', tgt.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (ROLE_ID, API_PERMISSION_ID, ALLOW_YN, created_by, updated_by) VALUES (src.ROLE_ID, src.API_PERMISSION_ID, src.ALLOW_YN, src.created_by, src.updated_by);
MERGE INTO adm_menu tgt USING (
SELECT 'SECRET' MENU_ID, NULL PARENT_MENU_ID, 'Secret / Key 관리' MENU_NAME, '/adm#secrets' MENU_PATH, 145 SORT_ORDER, 'Y' USE_YN, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
) src ON (tgt.MENU_ID = src.MENU_ID)
WHEN MATCHED THEN UPDATE SET tgt.MENU_NAME = src.MENU_NAME, tgt.MENU_PATH = src.MENU_PATH, tgt.SORT_ORDER = src.SORT_ORDER, tgt.USE_YN = 'Y', tgt.updated_by = 'SYSTEM', tgt.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (MENU_ID, PARENT_MENU_ID, MENU_NAME, MENU_PATH, SORT_ORDER, USE_YN, created_by, updated_by) VALUES (src.MENU_ID, src.PARENT_MENU_ID, src.MENU_NAME, src.MENU_PATH, src.SORT_ORDER, src.USE_YN, src.created_by, src.updated_by);
MERGE INTO adm_button tgt USING (
SELECT 'SECRET_READ' BUTTON_ID, 'SECRET' MENU_ID, 'READ' ACTION_CODE, 'Secret Metadata 조회' BUTTON_NAME, 'GET' HTTP_METHOD, '/adm/api/secrets/**' API_PATTERN, 10 SORT_ORDER, 'Y' USE_YN, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'SECRET_ROTATE' BUTTON_ID, 'SECRET' MENU_ID, 'ROTATE' ACTION_CODE, 'Secret Rotation' BUTTON_NAME, 'POST' HTTP_METHOD, '/adm/api/secrets/rotate' API_PATTERN, 20 SORT_ORDER, 'Y' USE_YN, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
) src ON (tgt.BUTTON_ID = src.BUTTON_ID)
WHEN MATCHED THEN UPDATE SET tgt.ACTION_CODE = src.ACTION_CODE, tgt.BUTTON_NAME = src.BUTTON_NAME, tgt.HTTP_METHOD = src.HTTP_METHOD, tgt.API_PATTERN = src.API_PATTERN, tgt.USE_YN = 'Y', tgt.updated_by = 'SYSTEM', tgt.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (BUTTON_ID, MENU_ID, ACTION_CODE, BUTTON_NAME, HTTP_METHOD, API_PATTERN, SORT_ORDER, USE_YN, created_by, updated_by) VALUES (src.BUTTON_ID, src.MENU_ID, src.ACTION_CODE, src.BUTTON_NAME, src.HTTP_METHOD, src.API_PATTERN, src.SORT_ORDER, src.USE_YN, src.created_by, src.updated_by);
MERGE INTO adm_role_menu tgt USING (
SELECT 'ADM_ADMIN' ROLE_ID, 'SECRET' MENU_ID, 'Y' READ_YN, 'Y' WRITE_YN, 'N' DELETE_YN, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'ADM_DEV_OPERATOR' ROLE_ID, 'SECRET' MENU_ID, 'Y' READ_YN, 'N' WRITE_YN, 'N' DELETE_YN, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'ADM_OPERATOR' ROLE_ID, 'SECRET' MENU_ID, 'Y' READ_YN, 'N' WRITE_YN, 'N' DELETE_YN, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'ADM_VIEWER' ROLE_ID, 'SECRET' MENU_ID, 'N' READ_YN, 'N' WRITE_YN, 'N' DELETE_YN, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'ADM_BIZ_OPERATOR' ROLE_ID, 'SECRET' MENU_ID, 'N' READ_YN, 'N' WRITE_YN, 'N' DELETE_YN, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
) src ON (tgt.ROLE_ID = src.ROLE_ID AND tgt.MENU_ID = src.MENU_ID)
WHEN MATCHED THEN UPDATE SET tgt.READ_YN = src.READ_YN, tgt.WRITE_YN = src.WRITE_YN, tgt.DELETE_YN = src.DELETE_YN, tgt.updated_by = 'SYSTEM', tgt.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (ROLE_ID, MENU_ID, READ_YN, WRITE_YN, DELETE_YN, created_by, updated_by) VALUES (src.ROLE_ID, src.MENU_ID, src.READ_YN, src.WRITE_YN, src.DELETE_YN, src.created_by, src.updated_by);
MERGE INTO adm_role_button tgt USING (
SELECT 'ADM_ADMIN' ROLE_ID, 'SECRET_READ' BUTTON_ID, 'Y' ALLOW_YN, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'ADM_ADMIN' ROLE_ID, 'SECRET_ROTATE' BUTTON_ID, 'Y' ALLOW_YN, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'ADM_DEV_OPERATOR' ROLE_ID, 'SECRET_READ' BUTTON_ID, 'Y' ALLOW_YN, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'ADM_DEV_OPERATOR' ROLE_ID, 'SECRET_ROTATE' BUTTON_ID, 'N' ALLOW_YN, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'ADM_OPERATOR' ROLE_ID, 'SECRET_READ' BUTTON_ID, 'Y' ALLOW_YN, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'ADM_OPERATOR' ROLE_ID, 'SECRET_ROTATE' BUTTON_ID, 'N' ALLOW_YN, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
) src ON (tgt.ROLE_ID = src.ROLE_ID AND tgt.BUTTON_ID = src.BUTTON_ID)
WHEN MATCHED THEN UPDATE SET tgt.ALLOW_YN = src.ALLOW_YN, tgt.updated_by = 'SYSTEM', tgt.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (ROLE_ID, BUTTON_ID, ALLOW_YN, created_by, updated_by) VALUES (src.ROLE_ID, src.BUTTON_ID, src.ALLOW_YN, src.created_by, src.updated_by);
MERGE INTO adm_api_permission tgt USING (
SELECT 'API_SECRET_READ' API_PERMISSION_ID, 'SECRET' API_GROUP_CODE, 'GET' HTTP_METHOD, '/adm/api/secrets/**' API_PATH, 'Secret Metadata 조회' API_NAME, 'READ' PERMISSION_CODE, 'SECRET' MENU_ID, 'SECRET_READ' BUTTON_ID, 'Y' USE_YN, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'API_SECRET_ROTATE' API_PERMISSION_ID, 'SECRET' API_GROUP_CODE, 'POST' HTTP_METHOD, '/adm/api/secrets/rotate' API_PATH, 'Secret Rotation' API_NAME, 'ROTATE' PERMISSION_CODE, 'SECRET' MENU_ID, 'SECRET_ROTATE' BUTTON_ID, 'Y' USE_YN, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
) src ON (tgt.API_PERMISSION_ID = src.API_PERMISSION_ID)
WHEN MATCHED THEN UPDATE SET tgt.API_PATH = src.API_PATH, tgt.API_NAME = src.API_NAME, tgt.PERMISSION_CODE = src.PERMISSION_CODE, tgt.BUTTON_ID = src.BUTTON_ID, tgt.USE_YN = 'Y', tgt.updated_by = 'SYSTEM', tgt.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (API_PERMISSION_ID, API_GROUP_CODE, HTTP_METHOD, API_PATH, API_NAME, PERMISSION_CODE, MENU_ID, BUTTON_ID, USE_YN, created_by, updated_by) VALUES (src.API_PERMISSION_ID, src.API_GROUP_CODE, src.HTTP_METHOD, src.API_PATH, src.API_NAME, src.PERMISSION_CODE, src.MENU_ID, src.BUTTON_ID, src.USE_YN, src.created_by, src.updated_by);
MERGE INTO adm_role_api_permission tgt USING (
SELECT 'ADM_ADMIN' ROLE_ID, 'API_SECRET_READ' API_PERMISSION_ID, 'Y' ALLOW_YN, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'ADM_ADMIN' ROLE_ID, 'API_SECRET_ROTATE' API_PERMISSION_ID, 'Y' ALLOW_YN, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'ADM_DEV_OPERATOR' ROLE_ID, 'API_SECRET_READ' API_PERMISSION_ID, 'Y' ALLOW_YN, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'ADM_DEV_OPERATOR' ROLE_ID, 'API_SECRET_ROTATE' API_PERMISSION_ID, 'N' ALLOW_YN, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'ADM_OPERATOR' ROLE_ID, 'API_SECRET_READ' API_PERMISSION_ID, 'Y' ALLOW_YN, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'ADM_OPERATOR' ROLE_ID, 'API_SECRET_ROTATE' API_PERMISSION_ID, 'N' ALLOW_YN, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
) src ON (tgt.ROLE_ID = src.ROLE_ID AND tgt.API_PERMISSION_ID = src.API_PERMISSION_ID)
WHEN MATCHED THEN UPDATE SET tgt.ALLOW_YN = src.ALLOW_YN, tgt.updated_by = 'SYSTEM', tgt.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (ROLE_ID, API_PERMISSION_ID, ALLOW_YN, created_by, updated_by) VALUES (src.ROLE_ID, src.API_PERMISSION_ID, src.ALLOW_YN, src.created_by, src.updated_by);
MERGE INTO adm_menu tgt USING (
SELECT 'BATCH_OVERVIEW' MENU_ID, 'BATCH' PARENT_MENU_ID, 'Batch Overview' MENU_NAME, '/adm#batch-overview' MENU_PATH, 501 SORT_ORDER, 'Y' USE_YN, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'BATCH_RUNTIME' MENU_ID, 'BATCH' PARENT_MENU_ID, 'Runtime Topology' MENU_NAME, '/adm#batch-runtime' MENU_PATH, 502 SORT_ORDER, 'Y' USE_YN, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'BATCH_INSTANCES' MENU_ID, 'BATCH' PARENT_MENU_ID, 'Runtime Instances' MENU_NAME, '/adm#batch-instances' MENU_PATH, 503 SORT_ORDER, 'Y' USE_YN, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'BATCH_SCHEDULER' MENU_ID, 'BATCH' PARENT_MENU_ID, 'Scheduler HA' MENU_NAME, '/adm#batch-scheduler' MENU_PATH, 504 SORT_ORDER, 'Y' USE_YN, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'BATCH_WORKER_POOLS' MENU_ID, 'BATCH' PARENT_MENU_ID, 'Worker Pools' MENU_NAME, '/adm#batch-worker-pools' MENU_PATH, 505 SORT_ORDER, 'Y' USE_YN, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'BATCH_CENTER_CUT' MENU_ID, 'BATCH' PARENT_MENU_ID, 'Center-Cut' MENU_NAME, '/adm#batch-center-cut' MENU_PATH, 506 SORT_ORDER, 'Y' USE_YN, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'BATCH_AGENTS' MENU_ID, 'BATCH' PARENT_MENU_ID, 'Host Agents' MENU_NAME, '/adm#batch-agents' MENU_PATH, 507 SORT_ORDER, 'Y' USE_YN, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'BATCH_JOB_PACKS' MENU_ID, 'BATCH' PARENT_MENU_ID, 'Job Packs' MENU_NAME, '/adm#batch-job-packs' MENU_PATH, 508 SORT_ORDER, 'Y' USE_YN, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'BATCH_EXECUTIONS' MENU_ID, 'BATCH' PARENT_MENU_ID, 'Executions' MENU_NAME, '/adm#batch-executions' MENU_PATH, 509 SORT_ORDER, 'Y' USE_YN, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'BATCH_DEPLOYMENT' MENU_ID, 'BATCH' PARENT_MENU_ID, 'Deployment / Rollback' MENU_NAME, '/adm#batch-deployment' MENU_PATH, 510 SORT_ORDER, 'Y' USE_YN, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'BATCH_RECOVERY' MENU_ID, 'BATCH' PARENT_MENU_ID, 'Recovery / Unknown' MENU_NAME, '/adm#batch-recovery' MENU_PATH, 511 SORT_ORDER, 'Y' USE_YN, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'BATCH_LEASES' MENU_ID, 'BATCH' PARENT_MENU_ID, 'Lease / Fencing' MENU_NAME, '/adm#batch-leases' MENU_PATH, 512 SORT_ORDER, 'Y' USE_YN, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'BATCH_ALERTS' MENU_ID, 'BATCH' PARENT_MENU_ID, 'Batch Alerts' MENU_NAME, '/adm#batch-alerts' MENU_PATH, 513 SORT_ORDER, 'Y' USE_YN, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'BATCH_AUDIT' MENU_ID, 'BATCH' PARENT_MENU_ID, 'Audit / Evidence' MENU_NAME, '/adm#batch-audit' MENU_PATH, 514 SORT_ORDER, 'Y' USE_YN, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
) src ON (tgt.MENU_ID = src.MENU_ID)
WHEN MATCHED THEN UPDATE SET tgt.PARENT_MENU_ID = src.PARENT_MENU_ID, tgt.MENU_NAME = src.MENU_NAME, tgt.MENU_PATH = src.MENU_PATH, tgt.SORT_ORDER = src.SORT_ORDER, tgt.USE_YN = 'Y', tgt.updated_by = 'SYSTEM', tgt.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (MENU_ID, PARENT_MENU_ID, MENU_NAME, MENU_PATH, SORT_ORDER, USE_YN, created_by, updated_by) VALUES (src.MENU_ID, src.PARENT_MENU_ID, src.MENU_NAME, src.MENU_PATH, src.SORT_ORDER, src.USE_YN, src.created_by, src.updated_by);
MERGE INTO adm_button tgt USING (
SELECT 'BAT_RUNTIME_VIEW' BUTTON_ID, 'BATCH_RUNTIME' MENU_ID, 'RUNTIME_VIEW' ACTION_CODE, 'Runtime 조회' BUTTON_NAME, 'GET' HTTP_METHOD, '/adm/api/batch-runtime/**' API_PATTERN, 10 SORT_ORDER, 'Y' USE_YN, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'BAT_RUNTIME_OPERATE' BUTTON_ID, 'BATCH_INSTANCES' MENU_ID, 'RUNTIME_OPERATE' ACTION_CODE, 'Runtime Start/Stop/Drain' BUTTON_NAME, 'POST' HTTP_METHOD, '/adm/api/approvals/**' API_PATTERN, 20 SORT_ORDER, 'Y' USE_YN, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'BAT_JOB_OPERATE' BUTTON_ID, 'BATCH_EXECUTIONS' MENU_ID, 'JOB_OPERATE' ACTION_CODE, 'Job 실행/중지/재처리' BUTTON_NAME, 'POST' HTTP_METHOD, '/adm/api/batch/**' API_PATTERN, 30 SORT_ORDER, 'Y' USE_YN, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'BAT_SCHEDULE_OPERATE' BUTTON_ID, 'BATCH_SCHEDULER' MENU_ID, 'SCHEDULE_OPERATE' ACTION_CODE, 'Scheduler 운영' BUTTON_NAME, 'POST' HTTP_METHOD, '/adm/api/batch/**' API_PATTERN, 40 SORT_ORDER, 'Y' USE_YN, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'BAT_WORKER_OPERATE' BUTTON_ID, 'BATCH_WORKER_POOLS' MENU_ID, 'WORKER_OPERATE' ACTION_CODE, 'Worker Pool 운영' BUTTON_NAME, 'POST' HTTP_METHOD, '/adm/api/approvals/**' API_PATTERN, 50 SORT_ORDER, 'Y' USE_YN, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'BAT_CENTER_CUT_OPERATE' BUTTON_ID, 'BATCH_CENTER_CUT' MENU_ID, 'CENTER_CUT_OPERATE' ACTION_CODE, 'Center-Cut 재처리/조정' BUTTON_NAME, 'POST' HTTP_METHOD, '/adm/api/batch-runtime/**' API_PATTERN, 60 SORT_ORDER, 'Y' USE_YN, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'BAT_AGENT_OPERATE' BUTTON_ID, 'BATCH_AGENTS' MENU_ID, 'AGENT_OPERATE' ACTION_CODE, 'Host Agent 운영' BUTTON_NAME, 'POST' HTTP_METHOD, '/adm/api/approvals/**' API_PATTERN, 70 SORT_ORDER, 'Y' USE_YN, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'BAT_DEPLOY_PLAN' BUTTON_ID, 'BATCH_DEPLOYMENT' MENU_ID, 'DEPLOY_PLAN' ACTION_CODE, 'Deployment Plan 생성' BUTTON_NAME, 'POST' HTTP_METHOD, '/adm/api/batch-runtime/deployment-plans' API_PATTERN, 80 SORT_ORDER, 'Y' USE_YN, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'BAT_DEPLOY_APPROVE' BUTTON_ID, 'BATCH_DEPLOYMENT' MENU_ID, 'DEPLOY_APPROVE' ACTION_CODE, 'Deployment 승인' BUTTON_NAME, 'POST' HTTP_METHOD, '/adm/api/approvals/**' API_PATTERN, 90 SORT_ORDER, 'Y' USE_YN, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'BAT_DEPLOY_EXECUTE' BUTTON_ID, 'BATCH_DEPLOYMENT' MENU_ID, 'DEPLOY_EXECUTE' ACTION_CODE, 'Deployment 실행' BUTTON_NAME, 'POST' HTTP_METHOD, '/adm/api/approvals/**' API_PATTERN, 100 SORT_ORDER, 'Y' USE_YN, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'BAT_ROLLBACK_EXECUTE' BUTTON_ID, 'BATCH_DEPLOYMENT' MENU_ID, 'ROLLBACK_EXECUTE' ACTION_CODE, 'Rollback 실행' BUTTON_NAME, 'POST' HTTP_METHOD, '/adm/api/approvals/**' API_PATTERN, 110 SORT_ORDER, 'Y' USE_YN, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'BAT_RECOVERY_OPERATE' BUTTON_ID, 'BATCH_RECOVERY' MENU_ID, 'RECOVERY_OPERATE' ACTION_CODE, 'UNKNOWN_RESULT 조정' BUTTON_NAME, 'POST' HTTP_METHOD, '/adm/api/batch-runtime/**' API_PATTERN, 120 SORT_ORDER, 'Y' USE_YN, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'BAT_SECURITY_AUDIT' BUTTON_ID, 'BATCH_AUDIT' MENU_ID, 'SECURITY_AUDIT' ACTION_CODE, 'BAT 보안·감사 조회' BUTTON_NAME, 'GET' HTTP_METHOD, '/adm/api/batch-runtime/views/audit' API_PATTERN, 130 SORT_ORDER, 'Y' USE_YN, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'BAT_EVIDENCE_DOWNLOAD' BUTTON_ID, 'BATCH_AUDIT' MENU_ID, 'EVIDENCE_DOWNLOAD' ACTION_CODE, 'BAT Evidence 다운로드' BUTTON_NAME, 'GET' HTTP_METHOD, '/adm/api/downloads/**' API_PATTERN, 140 SORT_ORDER, 'Y' USE_YN, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
) src ON (tgt.BUTTON_ID = src.BUTTON_ID)
WHEN MATCHED THEN UPDATE SET tgt.MENU_ID = src.MENU_ID, tgt.ACTION_CODE = src.ACTION_CODE, tgt.BUTTON_NAME = src.BUTTON_NAME, tgt.HTTP_METHOD = src.HTTP_METHOD, tgt.API_PATTERN = src.API_PATTERN, tgt.SORT_ORDER = src.SORT_ORDER, tgt.USE_YN = 'Y', tgt.updated_by = 'SYSTEM', tgt.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (BUTTON_ID, MENU_ID, ACTION_CODE, BUTTON_NAME, HTTP_METHOD, API_PATTERN, SORT_ORDER, USE_YN, created_by, updated_by) VALUES (src.BUTTON_ID, src.MENU_ID, src.ACTION_CODE, src.BUTTON_NAME, src.HTTP_METHOD, src.API_PATTERN, src.SORT_ORDER, src.USE_YN, src.created_by, src.updated_by);
MERGE INTO adm_role_menu tgt USING (
SELECT r.ROLE_ID ROLE_ID, m.MENU_ID MENU_ID, 'Y' READ_YN, CASE WHEN r.ROLE_ID IN ('ADM_ADMIN','ADM_DEV_OPERATOR','ADM_OPERATOR') THEN 'Y' ELSE 'N' END WRITE_YN, 'N' DELETE_YN, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM adm_role r JOIN adm_menu m ON m.PARENT_MENU_ID='BATCH'
WHERE r.ROLE_ID IN ('ADM_ADMIN','ADM_DEV_OPERATOR','ADM_OPERATOR','ADM_BIZ_OPERATOR','ADM_VIEWER')
) src ON (tgt.ROLE_ID = src.ROLE_ID AND tgt.MENU_ID = src.MENU_ID)
WHEN MATCHED THEN UPDATE SET tgt.READ_YN = src.READ_YN, tgt.WRITE_YN = src.WRITE_YN, tgt.DELETE_YN = src.DELETE_YN, tgt.updated_by = 'SYSTEM', tgt.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (ROLE_ID, MENU_ID, READ_YN, WRITE_YN, DELETE_YN, created_by, updated_by) VALUES (src.ROLE_ID, src.MENU_ID, src.READ_YN, src.WRITE_YN, src.DELETE_YN, src.created_by, src.updated_by);
MERGE INTO adm_role_button tgt USING (
SELECT r.ROLE_ID ROLE_ID, b.BUTTON_ID BUTTON_ID, CASE
         WHEN r.ROLE_ID='ADM_ADMIN' THEN 'Y'
         WHEN r.ROLE_ID IN ('ADM_DEV_OPERATOR','ADM_OPERATOR') AND b.BUTTON_ID NOT IN ('BAT_DEPLOY_APPROVE','BAT_DEPLOY_EXECUTE','BAT_ROLLBACK_EXECUTE') THEN 'Y'
         WHEN r.ROLE_ID='ADM_BIZ_OPERATOR' AND b.BUTTON_ID IN ('BAT_RUNTIME_VIEW','BAT_JOB_OPERATE','BAT_WORKER_OPERATE','BAT_CENTER_CUT_OPERATE','BAT_SECURITY_AUDIT','BAT_EVIDENCE_DOWNLOAD') THEN 'Y'
         WHEN r.ROLE_ID='ADM_VIEWER' AND b.BUTTON_ID IN ('BAT_RUNTIME_VIEW','BAT_SECURITY_AUDIT') THEN 'Y'
         ELSE 'N' END ALLOW_YN, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM adm_role r JOIN adm_button b ON b.BUTTON_ID LIKE 'BAT_%'
WHERE r.ROLE_ID IN ('ADM_ADMIN','ADM_DEV_OPERATOR','ADM_OPERATOR','ADM_BIZ_OPERATOR','ADM_VIEWER')
) src ON (tgt.ROLE_ID = src.ROLE_ID AND tgt.BUTTON_ID = src.BUTTON_ID)
WHEN MATCHED THEN UPDATE SET tgt.ALLOW_YN = src.ALLOW_YN, tgt.updated_by = 'SYSTEM', tgt.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (ROLE_ID, BUTTON_ID, ALLOW_YN, created_by, updated_by) VALUES (src.ROLE_ID, src.BUTTON_ID, src.ALLOW_YN, src.created_by, src.updated_by);
MERGE INTO adm_api_permission tgt USING (
SELECT ('API_' || BUTTON_ID) API_PERMISSION_ID, MENU_ID API_GROUP_CODE, COALESCE(HTTP_METHOD,'ANY') HTTP_METHOD, API_PATTERN API_PATH, BUTTON_NAME API_NAME, ACTION_CODE PERMISSION_CODE, MENU_ID MENU_ID, BUTTON_ID BUTTON_ID, 'Y' USE_YN, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM adm_button WHERE BUTTON_ID LIKE 'BAT_%' AND API_PATTERN IS NOT NULL
) src ON (tgt.API_PERMISSION_ID = src.API_PERMISSION_ID)
WHEN MATCHED THEN UPDATE SET tgt.API_GROUP_CODE = src.API_GROUP_CODE, tgt.HTTP_METHOD = src.HTTP_METHOD, tgt.API_PATH = src.API_PATH, tgt.API_NAME = src.API_NAME, tgt.PERMISSION_CODE = src.PERMISSION_CODE, tgt.MENU_ID = src.MENU_ID, tgt.BUTTON_ID = src.BUTTON_ID, tgt.USE_YN = 'Y', tgt.updated_by = 'SYSTEM', tgt.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (API_PERMISSION_ID, API_GROUP_CODE, HTTP_METHOD, API_PATH, API_NAME, PERMISSION_CODE, MENU_ID, BUTTON_ID, USE_YN, created_by, updated_by) VALUES (src.API_PERMISSION_ID, src.API_GROUP_CODE, src.HTTP_METHOD, src.API_PATH, src.API_NAME, src.PERMISSION_CODE, src.MENU_ID, src.BUTTON_ID, src.USE_YN, src.created_by, src.updated_by);
MERGE INTO adm_role_api_permission tgt USING (
SELECT rb.ROLE_ID ROLE_ID, ap.API_PERMISSION_ID API_PERMISSION_ID, rb.ALLOW_YN ALLOW_YN, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM adm_role_button rb JOIN adm_api_permission ap ON ap.BUTTON_ID=rb.BUTTON_ID
WHERE rb.BUTTON_ID LIKE 'BAT_%'
) src ON (tgt.ROLE_ID = src.ROLE_ID AND tgt.API_PERMISSION_ID = src.API_PERMISSION_ID)
WHEN MATCHED THEN UPDATE SET tgt.ALLOW_YN = src.ALLOW_YN, tgt.updated_by = 'SYSTEM', tgt.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (ROLE_ID, API_PERMISSION_ID, ALLOW_YN, created_by, updated_by) VALUES (src.ROLE_ID, src.API_PERMISSION_ID, src.ALLOW_YN, src.created_by, src.updated_by);

-- ===== END 60_adm_seed_data.sql =====
