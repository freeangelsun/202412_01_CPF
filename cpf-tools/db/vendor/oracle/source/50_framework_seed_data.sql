-- AUTO-GENERATED from cpf-tools/db/canonical/seed-model.json
-- vendor=oracle; source=50_framework_seed_data.sql
-- DO NOT EDIT generated seed directly.

-- CPF_LOGICAL_DATABASE=cpfDB
MERGE INTO OPS_CHANNEL_REGISTRY tgt USING (
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
MERGE INTO OPS_CHANNEL_EXECUTION_POLICY tgt USING (
SELECT 'CPF.DEFAULT' policy_key, '*' standard_execution_id, 'ANY' original_channel_code, 'ANY' caller_channel_code, '*' request_type, 'Y' allowed_yn, 'N' authentication_required_yn, 'N' signature_required_yn, 0 max_tps, NULL effective_from, NULL effective_to, 'Y' active_yn, 0 policy_version, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
) src ON (tgt.policy_key = src.policy_key)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id = src.standard_execution_id, tgt.original_channel_code = src.original_channel_code, tgt.caller_channel_code = src.caller_channel_code, tgt.request_type = src.request_type, tgt.allowed_yn = src.allowed_yn, tgt.authentication_required_yn = src.authentication_required_yn, tgt.signature_required_yn = src.signature_required_yn, tgt.max_tps = src.max_tps, tgt.active_yn = src.active_yn, tgt.updated_by = src.updated_by, tgt.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (policy_key, standard_execution_id, original_channel_code, caller_channel_code, request_type, allowed_yn, authentication_required_yn, signature_required_yn, max_tps, effective_from, effective_to, active_yn, policy_version, created_by, updated_by) VALUES (src.policy_key, src.standard_execution_id, src.original_channel_code, src.caller_channel_code, src.request_type, src.allowed_yn, src.authentication_required_yn, src.signature_required_yn, src.max_tps, src.effective_from, src.effective_to, src.active_yn, src.policy_version, src.created_by, src.updated_by);
MERGE INTO CMN_CODE tgt USING (
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
MERGE INTO CMN_CODE tgt USING (
SELECT (SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key = 'CODE_GROUP' AND code_value = 'MODULE') p) parent_id, 'MODULE' code_key, 'CPF' code_value, '프레임워크 공통 엔진' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT (SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key = 'CODE_GROUP' AND code_value = 'MODULE') p) parent_id, 'MODULE' code_key, 'CMN' code_value, '업무 공통 라이브러리' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT (SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key = 'CODE_GROUP' AND code_value = 'MODULE') p) parent_id, 'MODULE' code_key, 'ADM' code_value, '관리자 운영 서비스' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT (SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key = 'CODE_GROUP' AND code_value = 'MODULE') p) parent_id, 'MODULE' code_key, 'BZA' code_value, '업무 백오피스 서비스' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT (SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key = 'CODE_GROUP' AND code_value = 'MODULE') p) parent_id, 'MODULE' code_key, 'BAT' code_value, '선택 배치 실행 서비스' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT (SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key = 'CODE_GROUP' AND code_value = 'MODULE') p) parent_id, 'MODULE' code_key, 'EDU' code_value, '교육 샘플 서비스' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT (SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key = 'CODE_GROUP' AND code_value = 'REQUEST_TYPE') p) parent_id, 'REQUEST_TYPE' code_key, 'NORMAL' code_value, '일반 요청' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT (SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key = 'CODE_GROUP' AND code_value = 'REQUEST_TYPE') p) parent_id, 'REQUEST_TYPE' code_key, 'COMPENSATION' code_value, '보상 요청' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT (SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key = 'CODE_GROUP' AND code_value = 'REQUEST_TYPE') p) parent_id, 'REQUEST_TYPE' code_key, 'RETRY' code_value, '재시도 요청' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT (SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key = 'CODE_GROUP' AND code_value = 'CHANNEL_CODE') p) parent_id, 'CHANNEL_CODE' code_key, 'WEB' code_value, '웹 채널' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT (SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key = 'CODE_GROUP' AND code_value = 'CHANNEL_CODE') p) parent_id, 'CHANNEL_CODE' code_key, 'MOBILE' code_value, '모바일 채널' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT (SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key = 'CODE_GROUP' AND code_value = 'CHANNEL_CODE') p) parent_id, 'CHANNEL_CODE' code_key, 'BATCH' code_value, '배치 채널' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT (SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key = 'CODE_GROUP' AND code_value = 'CHANNEL_CODE') p) parent_id, 'CHANNEL_CODE' code_key, 'ADM' code_value, '관리자 채널' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT (SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key = 'CODE_GROUP' AND code_value = 'RESULT_TYPE') p) parent_id, 'RESULT_TYPE' code_key, 'S' code_value, '성공' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT (SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key = 'CODE_GROUP' AND code_value = 'RESULT_TYPE') p) parent_id, 'RESULT_TYPE' code_key, 'E' code_value, '오류' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT (SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key = 'CODE_GROUP' AND code_value = 'MESSAGE_FORMAT_TYPE') p) parent_id, 'MESSAGE_FORMAT_TYPE' code_key, 'FIXED' code_value, '고정 메시지' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT (SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key = 'CODE_GROUP' AND code_value = 'MESSAGE_FORMAT_TYPE') p) parent_id, 'MESSAGE_FORMAT_TYPE' code_key, 'INDEXED' code_value, '인덱스 파라미터 메시지' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT (SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key = 'CODE_GROUP' AND code_value = 'LOG_LEVEL') p) parent_id, 'LOG_LEVEL' code_key, 'TRACE' code_value, 'TRACE 로그' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT (SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key = 'CODE_GROUP' AND code_value = 'LOG_LEVEL') p) parent_id, 'LOG_LEVEL' code_key, 'DEBUG' code_value, 'DEBUG 로그' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT (SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key = 'CODE_GROUP' AND code_value = 'LOG_LEVEL') p) parent_id, 'LOG_LEVEL' code_key, 'INFO' code_value, 'INFO 로그' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT (SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key = 'CODE_GROUP' AND code_value = 'LOG_LEVEL') p) parent_id, 'LOG_LEVEL' code_key, 'WARN' code_value, 'WARN 로그' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT (SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key = 'CODE_GROUP' AND code_value = 'LOG_LEVEL') p) parent_id, 'LOG_LEVEL' code_key, 'ERROR' code_value, 'ERROR 로그' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT (SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key = 'CODE_GROUP' AND code_value = 'CACHE_NAME') p) parent_id, 'CACHE_NAME' code_key, 'ALL' code_value, '전체 캐시' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT (SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key = 'CODE_GROUP' AND code_value = 'CACHE_NAME') p) parent_id, 'CACHE_NAME' code_key, 'CODE' code_value, '코드 캐시' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT (SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key = 'CODE_GROUP' AND code_value = 'CACHE_NAME') p) parent_id, 'CACHE_NAME' code_key, 'MESSAGE' code_value, '메시지 캐시' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT (SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key = 'CODE_GROUP' AND code_value = 'CACHE_NAME') p) parent_id, 'CACHE_NAME' code_key, 'RESPONSE_CODE' code_value, '응답코드 캐시' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT (SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key = 'CODE_GROUP' AND code_value = 'CACHE_NAME') p) parent_id, 'CACHE_NAME' code_key, 'CONFIG' code_value, '설정 캐시' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT (SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key = 'CODE_GROUP' AND code_value = 'BATCH_JOB_TYPE') p) parent_id, 'BATCH_JOB_TYPE' code_key, 'TASKLET' code_value, 'Tasklet 배치' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT (SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key = 'CODE_GROUP' AND code_value = 'BATCH_JOB_TYPE') p) parent_id, 'BATCH_JOB_TYPE' code_key, 'CHUNK' code_value, 'Chunk 배치' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT (SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key = 'CODE_GROUP' AND code_value = 'BATCH_JOB_TYPE') p) parent_id, 'BATCH_JOB_TYPE' code_key, 'RETRY' code_value, '재처리 배치' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT (SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key = 'CODE_GROUP' AND code_value = 'YN') p) parent_id, 'YN' code_key, 'Y' code_value, '예' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT (SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key = 'CODE_GROUP' AND code_value = 'YN') p) parent_id, 'YN' code_key, 'N' code_value, '아니오' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
) src ON (tgt.code_key = src.code_key AND tgt.code_value = src.code_value)
WHEN MATCHED THEN UPDATE SET tgt.parent_id = src.parent_id, tgt.description = src.description, tgt.updated_by = src.updated_by, tgt.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (parent_id, code_key, code_value, description, created_by, updated_by) VALUES (src.parent_id, src.code_key, src.code_value, src.description, src.created_by, src.updated_by);
MERGE INTO CMN_MESSAGE tgt USING (
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
SELECT 'MEDU010001' message_code, 'ko' locale, 'INDEXED' message_format_type, '이미 등록된 {0}입니다.' external_message, '{0}={1} 값이 이미 존재합니다. duplicateCheck=EDU_SAMPLE' internal_message, 2 parameter_count, '["샘플키","SAMPLE-0001"]' parameter_sample, 'EDU 동적 중복 교육 메시지' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'MCMN000001' message_code, 'ko' locale, 'FIXED' message_format_type, 'CPF 교육 시스템에 오신 것을 환영합니다.' external_message, 'CMN education welcome message.' internal_message, 0 parameter_count, NULL parameter_sample, 'CMN 교육 환영 메시지' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'MCMN000001' message_code, 'en' locale, 'FIXED' message_format_type, 'Welcome to the CPF education system.' external_message, 'CMN education welcome message.' internal_message, 0 parameter_count, NULL parameter_sample, 'CMN 교육 환영 메시지' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
) src ON (tgt.message_code = src.message_code AND tgt.locale = src.locale)
WHEN MATCHED THEN UPDATE SET tgt.message_format_type = src.message_format_type, tgt.external_message = src.external_message, tgt.internal_message = src.internal_message, tgt.parameter_count = src.parameter_count, tgt.parameter_sample = src.parameter_sample, tgt.description = src.description, tgt.use_yn = 'Y', tgt.updated_by = src.updated_by, tgt.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (message_code, locale, message_format_type, external_message, internal_message, parameter_count, parameter_sample, description, created_by, updated_by) VALUES (src.message_code, src.locale, src.message_format_type, src.external_message, src.internal_message, src.parameter_count, src.parameter_sample, src.description, src.created_by, src.updated_by);
MERGE INTO CMN_RESPONSE_CODE tgt USING (
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
UNION ALL
SELECT 'EEDU010001' response_code, 'MEDU010001' message_code, 'E' result_type, 'EDU' module_id, '01' response_group, '0001' sequence_no, 409 http_status, 'EDU 샘플 중복 오류' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
) src ON (tgt.response_code = src.response_code)
WHEN MATCHED THEN UPDATE SET tgt.message_code = src.message_code, tgt.result_type = src.result_type, tgt.module_id = src.module_id, tgt.response_group = src.response_group, tgt.sequence_no = src.sequence_no, tgt.http_status = src.http_status, tgt.description = src.description, tgt.use_yn = 'Y', tgt.updated_by = src.updated_by, tgt.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (response_code, message_code, result_type, module_id, response_group, sequence_no, http_status, description, created_by, updated_by) VALUES (src.response_code, src.message_code, src.result_type, src.module_id, src.response_group, src.sequence_no, src.http_status, src.description, src.created_by, src.updated_by);
MERGE INTO CMN_PARAMETER tgt USING (
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
MERGE INTO CMN_CODE tgt USING (
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
MERGE INTO CMN_CODE tgt USING (
SELECT (SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='HTTP_METHOD') x) parent_id, 'HTTP_METHOD' code_key, 'GET' code_value, '조회' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT (SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='HTTP_METHOD') x) parent_id, 'HTTP_METHOD' code_key, 'POST' code_value, '등록/명령' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT (SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='HTTP_METHOD') x) parent_id, 'HTTP_METHOD' code_key, 'PUT' code_value, '전체 수정' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT (SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='HTTP_METHOD') x) parent_id, 'HTTP_METHOD' code_key, 'PATCH' code_value, '부분 수정' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT (SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='HTTP_METHOD') x) parent_id, 'HTTP_METHOD' code_key, 'DELETE' code_value, '삭제/회수' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT (SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='EXECUTION_STATUS') x) parent_id, 'EXECUTION_STATUS' code_key, 'READY' code_value, '실행 준비' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT (SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='EXECUTION_STATUS') x) parent_id, 'EXECUTION_STATUS' code_key, 'RUNNING' code_value, '실행 중' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT (SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='EXECUTION_STATUS') x) parent_id, 'EXECUTION_STATUS' code_key, 'SUCCESS' code_value, '정상 완료' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT (SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='EXECUTION_STATUS') x) parent_id, 'EXECUTION_STATUS' code_key, 'FAILED' code_value, '실패' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT (SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='EXECUTION_STATUS') x) parent_id, 'EXECUTION_STATUS' code_key, 'UNKNOWN_RESULT' code_value, '결과 미확정' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT (SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='ASYNC_STATUS') x) parent_id, 'ASYNC_STATUS' code_key, 'WAITING' code_value, '비동기 대기' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT (SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='ASYNC_STATUS') x) parent_id, 'ASYNC_STATUS' code_key, 'PROCESSING' code_value, '비동기 처리 중' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT (SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='ASYNC_STATUS') x) parent_id, 'ASYNC_STATUS' code_key, 'COMPLETED' code_value, '비동기 완료' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT (SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='ASYNC_STATUS') x) parent_id, 'ASYNC_STATUS' code_key, 'DLQ' code_value, 'Dead Letter Queue' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT (SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='RETRY_STATUS') x) parent_id, 'RETRY_STATUS' code_key, 'RETRYABLE' code_value, '재시도 가능' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT (SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='RETRY_STATUS') x) parent_id, 'RETRY_STATUS' code_key, 'NON_RETRYABLE' code_value, '재시도 금지' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT (SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='RETRY_STATUS') x) parent_id, 'RETRY_STATUS' code_key, 'EXHAUSTED' code_value, '재시도 소진' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT (SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='IDEMPOTENCY_STATUS') x) parent_id, 'IDEMPOTENCY_STATUS' code_key, 'PROCESSING' code_value, '멱등 처리 중' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT (SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='IDEMPOTENCY_STATUS') x) parent_id, 'IDEMPOTENCY_STATUS' code_key, 'COMPLETED' code_value, '멱등 처리 완료' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT (SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='IDEMPOTENCY_STATUS') x) parent_id, 'IDEMPOTENCY_STATUS' code_key, 'FAILED' code_value, '멱등 처리 실패' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT (SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='IDEMPOTENCY_STATUS') x) parent_id, 'IDEMPOTENCY_STATUS' code_key, 'UNKNOWN_RESULT' code_value, '멱등 결과 미확정' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT (SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='HEALTH_STATUS') x) parent_id, 'HEALTH_STATUS' code_key, 'UP' code_value, '정상' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT (SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='HEALTH_STATUS') x) parent_id, 'HEALTH_STATUS' code_key, 'DOWN' code_value, '장애' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT (SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='HEALTH_STATUS') x) parent_id, 'HEALTH_STATUS' code_key, 'DEGRADED' code_value, '부분 저하' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT (SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='CIRCUIT_STATUS') x) parent_id, 'CIRCUIT_STATUS' code_key, 'CLOSED' code_value, '정상 호출' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT (SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='CIRCUIT_STATUS') x) parent_id, 'CIRCUIT_STATUS' code_key, 'OPEN' code_value, '호출 차단' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT (SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='CIRCUIT_STATUS') x) parent_id, 'CIRCUIT_STATUS' code_key, 'HALF_OPEN' code_value, '복구 시험' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT (SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='FILE_SCAN_STATUS') x) parent_id, 'FILE_SCAN_STATUS' code_key, 'PENDING' code_value, '검사 대기' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT (SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='FILE_SCAN_STATUS') x) parent_id, 'FILE_SCAN_STATUS' code_key, 'CLEAN' code_value, '검사 정상' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT (SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='FILE_SCAN_STATUS') x) parent_id, 'FILE_SCAN_STATUS' code_key, 'INFECTED' code_value, '악성 탐지' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT (SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='FILE_SCAN_STATUS') x) parent_id, 'FILE_SCAN_STATUS' code_key, 'FAILED' code_value, '검사 실패' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT (SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='FILE_SCAN_STATUS') x) parent_id, 'FILE_SCAN_STATUS' code_key, 'QUARANTINED' code_value, '격리' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT (SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='DATA_CLASSIFICATION') x) parent_id, 'DATA_CLASSIFICATION' code_key, 'PUBLIC' code_value, '공개 정보' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT (SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='DATA_CLASSIFICATION') x) parent_id, 'DATA_CLASSIFICATION' code_key, 'INTERNAL' code_value, '내부 정보' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT (SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='DATA_CLASSIFICATION') x) parent_id, 'DATA_CLASSIFICATION' code_key, 'CONFIDENTIAL' code_value, '기밀 정보' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT (SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='DATA_CLASSIFICATION') x) parent_id, 'DATA_CLASSIFICATION' code_key, 'RESTRICTED' code_value, '제한/민감 정보' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT (SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='APPROVAL_STATUS') x) parent_id, 'APPROVAL_STATUS' code_key, 'DRAFT' code_value, '작성 중' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT (SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='APPROVAL_STATUS') x) parent_id, 'APPROVAL_STATUS' code_key, 'IN_REVIEW' code_value, '결재 진행' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT (SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='APPROVAL_STATUS') x) parent_id, 'APPROVAL_STATUS' code_key, 'APPROVED' code_value, '승인 완료' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT (SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='APPROVAL_STATUS') x) parent_id, 'APPROVAL_STATUS' code_key, 'REJECTED' code_value, '반려' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT (SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='APPROVAL_STATUS') x) parent_id, 'APPROVAL_STATUS' code_key, 'WITHDRAWN' code_value, '철회' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT (SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='APPROVAL_STATUS') x) parent_id, 'APPROVAL_STATUS' code_key, 'CANCELED' code_value, '취소' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT (SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='APPROVAL_STATUS') x) parent_id, 'APPROVAL_STATUS' code_key, 'EXPIRED' code_value, '만료' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT (SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='ERROR_CATEGORY') x) parent_id, 'ERROR_CATEGORY' code_key, 'VALIDATION' code_value, '입력/계약 검증 오류' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT (SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='ERROR_CATEGORY') x) parent_id, 'ERROR_CATEGORY' code_key, 'AUTHENTICATION' code_value, '인증 오류' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT (SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='ERROR_CATEGORY') x) parent_id, 'ERROR_CATEGORY' code_key, 'AUTHORIZATION' code_value, '인가 오류' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT (SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='ERROR_CATEGORY') x) parent_id, 'ERROR_CATEGORY' code_key, 'CONFLICT' code_value, '동시성/중복 오류' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT (SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='ERROR_CATEGORY') x) parent_id, 'ERROR_CATEGORY' code_key, 'TIMEOUT' code_value, 'Timeout' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT (SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='ERROR_CATEGORY') x) parent_id, 'ERROR_CATEGORY' code_key, 'TARGET_DOWN' code_value, '호출 대상 장애' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT (SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='ERROR_CATEGORY') x) parent_id, 'ERROR_CATEGORY' code_key, 'UNKNOWN_RESULT' code_value, '결과 미확정' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT (SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='RETENTION_ACTION') x) parent_id, 'RETENTION_ACTION' code_key, 'ARCHIVE' code_value, '보관소 이관' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT (SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='RETENTION_ACTION') x) parent_id, 'RETENTION_ACTION' code_key, 'PURGE' code_value, '정책 삭제' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT (SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='RETENTION_ACTION') x) parent_id, 'RETENTION_ACTION' code_key, 'LEGAL_HOLD' code_value, '법적 보존' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
) src ON (tgt.code_key = src.code_key AND tgt.code_value = src.code_value)
WHEN MATCHED THEN UPDATE SET tgt.parent_id = src.parent_id, tgt.description = src.description, tgt.updated_by = src.updated_by, tgt.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (parent_id, code_key, code_value, description, created_by, updated_by) VALUES (src.parent_id, src.code_key, src.code_value, src.description, src.created_by, src.updated_by);
MERGE INTO CMN_MESSAGE tgt USING (
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
MERGE INTO CMN_RESPONSE_CODE tgt USING (
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
MERGE INTO CMN_PARAMETER tgt USING (
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
MERGE INTO OPS_LOG_POLICY tgt USING (
SELECT 'ONLINE_DEFAULT' policy_key, '온라인 거래 기본 로그 정책' policy_name, 'ONLINE_TRANSACTION' target_type, '*' target_id, 'INFO' log_level, 'Y' db_log_enabled_yn, 'Y' file_log_enabled_yn, 2 policy_schema_version, 'NONE' query_capture_mode, 'ALLOWLIST' request_header_capture_mode, 'ALLOWLIST' response_header_capture_mode, 'NONE' request_body_capture_mode, 'NONE' response_body_capture_mode, 'SUMMARY' error_stack_capture_mode, 'content-type,x-cpf-trace-id,x-cpf-transaction-id' header_allowlist, 4096 max_query_bytes, 8192 max_header_bytes, 65536 max_request_body_bytes, 65536 max_response_body_bytes, 32768 max_stack_bytes, 'N' request_body_log_yn, 'N' response_body_log_yn, 'Y' error_stack_log_yn, 'DEFAULT' masking_policy_key, '04aec0a6adbf48c269e1538ca571819dc54400391e33d5b497ec05406bccd445' policy_checksum, 90 retention_days, 100.00 sampling_rate, 100 priority, 'Y' active_yn, '온라인 Controller/API 기본 로그 정책' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'BATCH_DEFAULT' policy_key, '배치 기본 로그 정책' policy_name, 'BATCH_JOB' target_type, '*' target_id, 'INFO' log_level, 'Y' db_log_enabled_yn, 'Y' file_log_enabled_yn, 2 policy_schema_version, 'NONE' query_capture_mode, 'ALLOWLIST' request_header_capture_mode, 'ALLOWLIST' response_header_capture_mode, 'NONE' request_body_capture_mode, 'NONE' response_body_capture_mode, 'SUMMARY' error_stack_capture_mode, 'content-type,x-cpf-trace-id,x-cpf-transaction-id' header_allowlist, 4096 max_query_bytes, 8192 max_header_bytes, 65536 max_request_body_bytes, 65536 max_response_body_bytes, 32768 max_stack_bytes, 'N' request_body_log_yn, 'N' response_body_log_yn, 'Y' error_stack_log_yn, 'DEFAULT' masking_policy_key, '0eca9ff2359e55290f01c2594d399c32e4af9decd34541a6f571a4345f36ca08' policy_checksum, 180 retention_days, 100.00 sampling_rate, 100 priority, 'Y' active_yn, 'Spring Batch Job 기본 로그 정책' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'ADM_OPERATION_DEFAULT' policy_key, 'ADM 운영 기본 로그 정책' policy_name, 'MODULE' target_type, 'ADM' target_id, 'INFO' log_level, 'Y' db_log_enabled_yn, 'Y' file_log_enabled_yn, 2 policy_schema_version, 'NONE' query_capture_mode, 'ALLOWLIST' request_header_capture_mode, 'ALLOWLIST' response_header_capture_mode, 'NONE' request_body_capture_mode, 'NONE' response_body_capture_mode, 'SUMMARY' error_stack_capture_mode, 'content-type,x-cpf-trace-id,x-cpf-transaction-id' header_allowlist, 4096 max_query_bytes, 8192 max_header_bytes, 65536 max_request_body_bytes, 65536 max_response_body_bytes, 32768 max_stack_bytes, 'N' request_body_log_yn, 'N' response_body_log_yn, 'Y' error_stack_log_yn, 'DEFAULT' masking_policy_key, '9ea15a6d3c662bcaf9295a2512cef8fc12da0e77eea6f07b3c5e55e5fb79e705' policy_checksum, 365 retention_days, 100.00 sampling_rate, 50 priority, 'Y' active_yn, 'ADM 운영 API 기본 로그 정책' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
) src ON (tgt.policy_key = src.policy_key)
WHEN MATCHED THEN UPDATE SET tgt.policy_name = src.policy_name, tgt.target_type = src.target_type, tgt.target_id = src.target_id, tgt.log_level = src.log_level, tgt.db_log_enabled_yn = src.db_log_enabled_yn, tgt.file_log_enabled_yn = src.file_log_enabled_yn, tgt.policy_schema_version = src.policy_schema_version, tgt.query_capture_mode = src.query_capture_mode, tgt.request_header_capture_mode = src.request_header_capture_mode, tgt.response_header_capture_mode = src.response_header_capture_mode, tgt.request_body_capture_mode = src.request_body_capture_mode, tgt.response_body_capture_mode = src.response_body_capture_mode, tgt.error_stack_capture_mode = src.error_stack_capture_mode, tgt.header_allowlist = src.header_allowlist, tgt.max_query_bytes = src.max_query_bytes, tgt.max_header_bytes = src.max_header_bytes, tgt.max_request_body_bytes = src.max_request_body_bytes, tgt.max_response_body_bytes = src.max_response_body_bytes, tgt.max_stack_bytes = src.max_stack_bytes, tgt.request_body_log_yn = src.request_body_log_yn, tgt.response_body_log_yn = src.response_body_log_yn, tgt.error_stack_log_yn = src.error_stack_log_yn, tgt.masking_policy_key = src.masking_policy_key, tgt.policy_checksum = src.policy_checksum, tgt.retention_days = src.retention_days, tgt.sampling_rate = src.sampling_rate, tgt.priority = src.priority, tgt.active_yn = src.active_yn, tgt.description = src.description, tgt.updated_by = src.updated_by
WHEN NOT MATCHED THEN INSERT (policy_key, policy_name, target_type, target_id, log_level, db_log_enabled_yn, file_log_enabled_yn, policy_schema_version, query_capture_mode, request_header_capture_mode, response_header_capture_mode, request_body_capture_mode, response_body_capture_mode, error_stack_capture_mode, header_allowlist, max_query_bytes, max_header_bytes, max_request_body_bytes, max_response_body_bytes, max_stack_bytes, request_body_log_yn, response_body_log_yn, error_stack_log_yn, masking_policy_key, policy_checksum, retention_days, sampling_rate, priority, active_yn, description, created_by, updated_by) VALUES (src.policy_key, src.policy_name, src.target_type, src.target_id, src.log_level, src.db_log_enabled_yn, src.file_log_enabled_yn, src.policy_schema_version, src.query_capture_mode, src.request_header_capture_mode, src.response_header_capture_mode, src.request_body_capture_mode, src.response_body_capture_mode, src.error_stack_capture_mode, src.header_allowlist, src.max_query_bytes, src.max_header_bytes, src.max_request_body_bytes, src.max_response_body_bytes, src.max_stack_bytes, src.request_body_log_yn, src.response_body_log_yn, src.error_stack_log_yn, src.masking_policy_key, src.policy_checksum, src.retention_days, src.sampling_rate, src.priority, src.active_yn, src.description, src.created_by, src.updated_by);
MERGE INTO SEC_JWT_KEY tgt USING (
SELECT 'local-cpf-hs256-001' KEY_ID, 'CPF' ISSUER, 'HS256' ALGORITHM, 'ENV:CPF_CMN_SECURITY_JWT_SECRET' SECRET_REF, 'Y' ACTIVE_YN, NULL EXPIRE_AT, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
) src ON (tgt.KEY_ID = src.KEY_ID)
WHEN MATCHED THEN UPDATE SET tgt.ISSUER = src.ISSUER, tgt.ALGORITHM = src.ALGORITHM, tgt.SECRET_REF = src.SECRET_REF, tgt.ACTIVE_YN = src.ACTIVE_YN, tgt.EXPIRE_AT = src.EXPIRE_AT, tgt.updated_by = src.updated_by, tgt.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (KEY_ID, ISSUER, ALGORITHM, SECRET_REF, ACTIVE_YN, EXPIRE_AT, created_by, updated_by) VALUES (src.KEY_ID, src.ISSUER, src.ALGORITHM, src.SECRET_REF, src.ACTIVE_YN, src.EXPIRE_AT, src.created_by, src.updated_by);
INSERT INTO CMN_CACHE_REFRESH_EVENT (cache_name, event_type, event_key, source_was_id, published_by, created_by, updated_by) SELECT 'ALL', 'INITIAL_LOAD', 'INITIAL_FRAMEWORK_SEED', 'SQL', 'SYSTEM', 'SYSTEM', 'SYSTEM'
WHERE NOT EXISTS (
    SELECT 1
    FROM CMN_CACHE_REFRESH_EVENT
    WHERE cache_name = 'ALL'
      AND event_type = 'INITIAL_LOAD'
      AND event_key = 'INITIAL_FRAMEWORK_SEED'
);
MERGE INTO CPF_NOTIFICATION_RULE tgt USING (
SELECT 'BATCH_EXECUTION' event_type, 'FAILED' event_sub_type, 'ADM' channel_code, 'BATCH_FAILED_DEFAULT' template_code, 'ERROR' severity, 'ADM_BATCH_OPERATOR' receiver_group, 'Y' use_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT 'SECURITY_EVENT' event_type, 'LOGIN_FAILURE' event_sub_type, 'ADM' channel_code, 'SECURITY_LOGIN_FAILURE' template_code, 'WARN' severity, 'ADM_SECURITY_OPERATOR' receiver_group, 'Y' use_yn, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
) src ON (tgt.event_type = src.event_type AND tgt.event_sub_type = src.event_sub_type AND tgt.channel_code = src.channel_code)
WHEN MATCHED THEN UPDATE SET tgt.template_code = src.template_code, tgt.severity = src.severity, tgt.receiver_group = src.receiver_group, tgt.use_yn = src.use_yn, tgt.updated_by = src.updated_by, tgt.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (event_type, event_sub_type, channel_code, template_code, severity, receiver_group, use_yn, created_by, updated_by) VALUES (src.event_type, src.event_sub_type, src.channel_code, src.template_code, src.severity, src.receiver_group, src.use_yn, src.created_by, src.updated_by);
INSERT INTO CMN_CODE (parent_id, code_key, code_value, description, created_by, updated_by) SELECT NULL, 'CODE_GROUP', 'SORT_DIRECTION', '표준 정렬 방향', 'SYSTEM', 'SYSTEM'
WHERE NOT EXISTS (SELECT 1 FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='SORT_DIRECTION');
MERGE INTO CMN_CODE tgt USING (
SELECT (SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='SORT_DIRECTION') x) parent_id, 'SORT_DIRECTION' code_key, 'ASC' code_value, '오름차순' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT (SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='SORT_DIRECTION') x) parent_id, 'SORT_DIRECTION' code_key, 'DESC' code_value, '내림차순' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
) src ON (tgt.code_key = src.code_key AND tgt.code_value = src.code_value)
WHEN MATCHED THEN UPDATE SET tgt.parent_id = src.parent_id, tgt.description = src.description, tgt.updated_by = src.updated_by, tgt.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (parent_id, code_key, code_value, description, created_by, updated_by) VALUES (src.parent_id, src.code_key, src.code_value, src.description, src.created_by, src.updated_by);
MERGE INTO CMN_MESSAGE tgt USING (
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
MERGE INTO CMN_RESPONSE_CODE tgt USING (
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
MERGE INTO CMN_PARAMETER tgt USING (
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
MERGE INTO CMN_CODE tgt USING (
SELECT (SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='REQUEST_TYPE') x) parent_id, 'REQUEST_TYPE' code_key, 'O' code_value, '온라인 요청' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT (SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='REQUEST_TYPE') x) parent_id, 'REQUEST_TYPE' code_key, 'S' code_value, '공유 내부 서비스 요청' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT (SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='REQUEST_TYPE') x) parent_id, 'REQUEST_TYPE' code_key, 'B' code_value, '배치 요청' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT (SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='CHANNEL_CODE') x) parent_id, 'CHANNEL_CODE' code_key, 'APP' code_value, '모바일 앱 채널' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT (SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='CHANNEL_CODE') x) parent_id, 'CHANNEL_CODE' code_key, 'JUT' code_value, 'JUnit/자동 테스트 채널' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT (SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='RESULT_TYPE') x) parent_id, 'RESULT_TYPE' code_key, 'W' code_value, '경고/부분 성공' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT (SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='MESSAGE_FORMAT_TYPE') x) parent_id, 'MESSAGE_FORMAT_TYPE' code_key, 'PARAMETER' code_value, 'Named parameter 메시지' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT (SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='ASYNC_STATUS') x) parent_id, 'ASYNC_STATUS' code_key, 'FAILED' code_value, '비동기 처리 실패' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT (SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='BATCH_JOB_TYPE') x) parent_id, 'BATCH_JOB_TYPE' code_key, 'SPRING_BATCH' code_value, 'Spring Batch Job' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT (SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='BATCH_JOB_TYPE') x) parent_id, 'BATCH_JOB_TYPE' code_key, 'WORKER' code_value, '지속 Worker' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT (SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='BATCH_JOB_TYPE') x) parent_id, 'BATCH_JOB_TYPE' code_key, 'SCHEDULER' code_value, 'Scheduler Job' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
UNION ALL
SELECT (SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='BATCH_JOB_TYPE') x) parent_id, 'BATCH_JOB_TYPE' code_key, 'CENTER_CUT' code_value, 'Center-Cut 대량 처리' description, 'SYSTEM' created_by, 'SYSTEM' updated_by FROM dual
) src ON (tgt.code_key = src.code_key AND tgt.code_value = src.code_value)
WHEN MATCHED THEN UPDATE SET tgt.parent_id = src.parent_id, tgt.description = src.description, tgt.use_yn = 'Y', tgt.updated_by = src.updated_by, tgt.updated_at = CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (parent_id, code_key, code_value, description, created_by, updated_by) VALUES (src.parent_id, src.code_key, src.code_value, src.description, src.created_by, src.updated_by);
