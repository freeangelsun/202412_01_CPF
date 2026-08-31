-- CPF generated lifecycle bundle; vendor=oracle
-- Source plan: cpf-tools/db/config/database-source-plan.json

-- ===== BEGIN 50_framework_seed_data.sql =====
-- AUTO-GENERATED from cpf-tools/db/canonical/seed-model.json
-- vendor=oracle; source=50_framework_seed_data.sql
-- DERIVED compatibility input; canonical authority is cpf-tools/db/canonical/**.
-- DO NOT EDIT generated seed directly.

-- CPF_LOGICAL_DATABASE=cpfDB
MERGE INTO OPS_SYSTEM_REGISTRY tgt
USING (SELECT 'CPF' AS system_code, 'CPF Core Platform' AS system_name, 'CPF' AS domain_code, 'Y' AS enabled_yn, 'CPF core platform system' AS description, 1 AS policy_version, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.system_code=src.system_code)
WHEN MATCHED THEN UPDATE SET tgt.system_name=src.system_name, tgt.domain_code=src.domain_code, tgt.enabled_yn=src.enabled_yn, tgt.description=src.description, tgt.policy_version=src.policy_version, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (system_code, system_name, domain_code, enabled_yn, description, policy_version, created_by, updated_by) VALUES (src.system_code, src.system_name, src.domain_code, src.enabled_yn, src.description, src.policy_version, src.created_by, src.updated_by);
MERGE INTO OPS_SYSTEM_REGISTRY tgt
USING (SELECT 'CMN' AS system_code, 'CPF Common' AS system_name, 'CMN' AS domain_code, 'Y' AS enabled_yn, 'CPF mandatory common system' AS description, 1 AS policy_version, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.system_code=src.system_code)
WHEN MATCHED THEN UPDATE SET tgt.system_name=src.system_name, tgt.domain_code=src.domain_code, tgt.enabled_yn=src.enabled_yn, tgt.description=src.description, tgt.policy_version=src.policy_version, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (system_code, system_name, domain_code, enabled_yn, description, policy_version, created_by, updated_by) VALUES (src.system_code, src.system_name, src.domain_code, src.enabled_yn, src.description, src.policy_version, src.created_by, src.updated_by);
MERGE INTO OPS_SYSTEM_REGISTRY tgt
USING (SELECT 'ADM' AS system_code, 'CPF Administration' AS system_name, 'ADM' AS domain_code, 'Y' AS enabled_yn, 'CPF administration system' AS description, 1 AS policy_version, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.system_code=src.system_code)
WHEN MATCHED THEN UPDATE SET tgt.system_name=src.system_name, tgt.domain_code=src.domain_code, tgt.enabled_yn=src.enabled_yn, tgt.description=src.description, tgt.policy_version=src.policy_version, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (system_code, system_name, domain_code, enabled_yn, description, policy_version, created_by, updated_by) VALUES (src.system_code, src.system_name, src.domain_code, src.enabled_yn, src.description, src.policy_version, src.created_by, src.updated_by);
MERGE INTO OPS_SYSTEM_REGISTRY tgt
USING (SELECT 'MBW' AS system_code, 'CPF Backoffice' AS system_name, 'MBW' AS domain_code, 'Y' AS enabled_yn, 'CPF business backoffice system' AS description, 1 AS policy_version, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.system_code=src.system_code)
WHEN MATCHED THEN UPDATE SET tgt.system_name=src.system_name, tgt.domain_code=src.domain_code, tgt.enabled_yn=src.enabled_yn, tgt.description=src.description, tgt.policy_version=src.policy_version, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (system_code, system_name, domain_code, enabled_yn, description, policy_version, created_by, updated_by) VALUES (src.system_code, src.system_name, src.domain_code, src.enabled_yn, src.description, src.policy_version, src.created_by, src.updated_by);
MERGE INTO OPS_SYSTEM_REGISTRY tgt
USING (SELECT 'BAT' AS system_code, 'CPF Batch' AS system_name, 'BAT' AS domain_code, 'Y' AS enabled_yn, 'CPF batch runtime system' AS description, 1 AS policy_version, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.system_code=src.system_code)
WHEN MATCHED THEN UPDATE SET tgt.system_name=src.system_name, tgt.domain_code=src.domain_code, tgt.enabled_yn=src.enabled_yn, tgt.description=src.description, tgt.policy_version=src.policy_version, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (system_code, system_name, domain_code, enabled_yn, description, policy_version, created_by, updated_by) VALUES (src.system_code, src.system_name, src.domain_code, src.enabled_yn, src.description, src.policy_version, src.created_by, src.updated_by);
MERGE INTO OPS_SYSTEM_REGISTRY tgt
USING (SELECT 'EDU' AS system_code, 'CPF Education' AS system_name, 'EDU' AS domain_code, 'Y' AS enabled_yn, 'CPF education reference system' AS description, 1 AS policy_version, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.system_code=src.system_code)
WHEN MATCHED THEN UPDATE SET tgt.system_name=src.system_name, tgt.domain_code=src.domain_code, tgt.enabled_yn=src.enabled_yn, tgt.description=src.description, tgt.policy_version=src.policy_version, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (system_code, system_name, domain_code, enabled_yn, description, policy_version, created_by, updated_by) VALUES (src.system_code, src.system_name, src.domain_code, src.enabled_yn, src.description, src.policy_version, src.created_by, src.updated_by);
MERGE INTO OPS_CHANNEL_REGISTRY tgt
USING (SELECT 'WEB' AS channel_code, '웹' AS channel_name, 'CLIENT' AS channel_type, 'EXTERNAL' AS trust_level, 'Y' AS client_channel_yn, 'N' AS internal_channel_yn, 'Y' AS authentication_required_yn, 'N' AS signature_required_yn, 'Y' AS active_yn, '웹 브라우저 채널' AS description, 0 AS policy_version, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.channel_code=src.channel_code)
WHEN MATCHED THEN UPDATE SET tgt.channel_name=src.channel_name, tgt.channel_type=src.channel_type, tgt.trust_level=src.trust_level, tgt.client_channel_yn=src.client_channel_yn, tgt.internal_channel_yn=src.internal_channel_yn, tgt.authentication_required_yn=src.authentication_required_yn, tgt.signature_required_yn=src.signature_required_yn, tgt.active_yn=src.active_yn, tgt.description=src.description, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (channel_code, channel_name, channel_type, trust_level, client_channel_yn, internal_channel_yn, authentication_required_yn, signature_required_yn, active_yn, description, policy_version, created_by, updated_by) VALUES (src.channel_code, src.channel_name, src.channel_type, src.trust_level, src.client_channel_yn, src.internal_channel_yn, src.authentication_required_yn, src.signature_required_yn, src.active_yn, src.description, src.policy_version, src.created_by, src.updated_by);
MERGE INTO OPS_CHANNEL_REGISTRY tgt
USING (SELECT 'MOBILE' AS channel_code, '모바일' AS channel_name, 'CLIENT' AS channel_type, 'EXTERNAL' AS trust_level, 'Y' AS client_channel_yn, 'N' AS internal_channel_yn, 'Y' AS authentication_required_yn, 'N' AS signature_required_yn, 'Y' AS active_yn, '모바일 애플리케이션 채널' AS description, 0 AS policy_version, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.channel_code=src.channel_code)
WHEN MATCHED THEN UPDATE SET tgt.channel_name=src.channel_name, tgt.channel_type=src.channel_type, tgt.trust_level=src.trust_level, tgt.client_channel_yn=src.client_channel_yn, tgt.internal_channel_yn=src.internal_channel_yn, tgt.authentication_required_yn=src.authentication_required_yn, tgt.signature_required_yn=src.signature_required_yn, tgt.active_yn=src.active_yn, tgt.description=src.description, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (channel_code, channel_name, channel_type, trust_level, client_channel_yn, internal_channel_yn, authentication_required_yn, signature_required_yn, active_yn, description, policy_version, created_by, updated_by) VALUES (src.channel_code, src.channel_name, src.channel_type, src.trust_level, src.client_channel_yn, src.internal_channel_yn, src.authentication_required_yn, src.signature_required_yn, src.active_yn, src.description, src.policy_version, src.created_by, src.updated_by);
MERGE INTO OPS_CHANNEL_REGISTRY tgt
USING (SELECT 'ADM' AS channel_code, '관리자' AS channel_name, 'OPERATOR' AS channel_type, 'INTERNAL' AS trust_level, 'Y' AS client_channel_yn, 'Y' AS internal_channel_yn, 'Y' AS authentication_required_yn, 'N' AS signature_required_yn, 'Y' AS active_yn, 'ADM 운영 채널' AS description, 0 AS policy_version, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.channel_code=src.channel_code)
WHEN MATCHED THEN UPDATE SET tgt.channel_name=src.channel_name, tgt.channel_type=src.channel_type, tgt.trust_level=src.trust_level, tgt.client_channel_yn=src.client_channel_yn, tgt.internal_channel_yn=src.internal_channel_yn, tgt.authentication_required_yn=src.authentication_required_yn, tgt.signature_required_yn=src.signature_required_yn, tgt.active_yn=src.active_yn, tgt.description=src.description, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (channel_code, channel_name, channel_type, trust_level, client_channel_yn, internal_channel_yn, authentication_required_yn, signature_required_yn, active_yn, description, policy_version, created_by, updated_by) VALUES (src.channel_code, src.channel_name, src.channel_type, src.trust_level, src.client_channel_yn, src.internal_channel_yn, src.authentication_required_yn, src.signature_required_yn, src.active_yn, src.description, src.policy_version, src.created_by, src.updated_by);
MERGE INTO OPS_CHANNEL_REGISTRY tgt
USING (SELECT 'BATCH' AS channel_code, '배치' AS channel_name, 'SYSTEM' AS channel_type, 'INTERNAL' AS trust_level, 'N' AS client_channel_yn, 'Y' AS internal_channel_yn, 'N' AS authentication_required_yn, 'N' AS signature_required_yn, 'Y' AS active_yn, '배치 실행 채널' AS description, 0 AS policy_version, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.channel_code=src.channel_code)
WHEN MATCHED THEN UPDATE SET tgt.channel_name=src.channel_name, tgt.channel_type=src.channel_type, tgt.trust_level=src.trust_level, tgt.client_channel_yn=src.client_channel_yn, tgt.internal_channel_yn=src.internal_channel_yn, tgt.authentication_required_yn=src.authentication_required_yn, tgt.signature_required_yn=src.signature_required_yn, tgt.active_yn=src.active_yn, tgt.description=src.description, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (channel_code, channel_name, channel_type, trust_level, client_channel_yn, internal_channel_yn, authentication_required_yn, signature_required_yn, active_yn, description, policy_version, created_by, updated_by) VALUES (src.channel_code, src.channel_name, src.channel_type, src.trust_level, src.client_channel_yn, src.internal_channel_yn, src.authentication_required_yn, src.signature_required_yn, src.active_yn, src.description, src.policy_version, src.created_by, src.updated_by);
MERGE INTO OPS_CHANNEL_EXECUTION_POLICY tgt
USING (SELECT 'CPF.DEFAULT' AS policy_key, '*' AS operation_id, '*' AS caller_channel, 'Y' AS allowed_yn, 'N' AS authentication_required_yn, 'N' AS signature_required_yn, 0 AS max_tps, NULL AS effective_from, NULL AS effective_to, 'Y' AS active_yn, 0 AS policy_version, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.policy_key=src.policy_key)
WHEN MATCHED THEN UPDATE SET tgt.operation_id=src.operation_id, tgt.caller_channel=src.caller_channel, tgt.allowed_yn=src.allowed_yn, tgt.authentication_required_yn=src.authentication_required_yn, tgt.signature_required_yn=src.signature_required_yn, tgt.max_tps=src.max_tps, tgt.active_yn=src.active_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (policy_key, operation_id, caller_channel, allowed_yn, authentication_required_yn, signature_required_yn, max_tps, effective_from, effective_to, active_yn, policy_version, created_by, updated_by) VALUES (src.policy_key, src.operation_id, src.caller_channel, src.allowed_yn, src.authentication_required_yn, src.signature_required_yn, src.max_tps, src.effective_from, src.effective_to, src.active_yn, src.policy_version, src.created_by, src.updated_by);
MERGE INTO CMN_CODE tgt
USING (SELECT NULL AS parent_id, 'CODE_GROUP' AS code_key, 'MODULE' AS code_value, '서비스 모듈 코드 그룹' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.code_key=src.code_key AND tgt.code_value=src.code_value)
WHEN MATCHED THEN UPDATE SET tgt.description=src.description, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (parent_id, code_key, code_value, description, created_by, updated_by) VALUES (src.parent_id, src.code_key, src.code_value, src.description, src.created_by, src.updated_by);
MERGE INTO CMN_CODE tgt
USING (SELECT NULL AS parent_id, 'CODE_GROUP' AS code_key, 'REQUEST_TYPE' AS code_value, '요청 유형 코드 그룹' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.code_key=src.code_key AND tgt.code_value=src.code_value)
WHEN MATCHED THEN UPDATE SET tgt.description=src.description, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (parent_id, code_key, code_value, description, created_by, updated_by) VALUES (src.parent_id, src.code_key, src.code_value, src.description, src.created_by, src.updated_by);
MERGE INTO CMN_CODE tgt
USING (SELECT NULL AS parent_id, 'CODE_GROUP' AS code_key, 'CHANNEL_CODE' AS code_value, '채널 코드 그룹' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.code_key=src.code_key AND tgt.code_value=src.code_value)
WHEN MATCHED THEN UPDATE SET tgt.description=src.description, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (parent_id, code_key, code_value, description, created_by, updated_by) VALUES (src.parent_id, src.code_key, src.code_value, src.description, src.created_by, src.updated_by);
MERGE INTO CMN_CODE tgt
USING (SELECT NULL AS parent_id, 'CODE_GROUP' AS code_key, 'RESULT_TYPE' AS code_value, '응답 결과 유형 코드 그룹' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.code_key=src.code_key AND tgt.code_value=src.code_value)
WHEN MATCHED THEN UPDATE SET tgt.description=src.description, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (parent_id, code_key, code_value, description, created_by, updated_by) VALUES (src.parent_id, src.code_key, src.code_value, src.description, src.created_by, src.updated_by);
MERGE INTO CMN_CODE tgt
USING (SELECT NULL AS parent_id, 'CODE_GROUP' AS code_key, 'MESSAGE_FORMAT_TYPE' AS code_value, '메시지 포맷 유형 코드 그룹' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.code_key=src.code_key AND tgt.code_value=src.code_value)
WHEN MATCHED THEN UPDATE SET tgt.description=src.description, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (parent_id, code_key, code_value, description, created_by, updated_by) VALUES (src.parent_id, src.code_key, src.code_value, src.description, src.created_by, src.updated_by);
MERGE INTO CMN_CODE tgt
USING (SELECT NULL AS parent_id, 'CODE_GROUP' AS code_key, 'LOG_LEVEL' AS code_value, '동적 로그 레벨 코드 그룹' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.code_key=src.code_key AND tgt.code_value=src.code_value)
WHEN MATCHED THEN UPDATE SET tgt.description=src.description, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (parent_id, code_key, code_value, description, created_by, updated_by) VALUES (src.parent_id, src.code_key, src.code_value, src.description, src.created_by, src.updated_by);
MERGE INTO CMN_CODE tgt
USING (SELECT NULL AS parent_id, 'CODE_GROUP' AS code_key, 'CACHE_NAME' AS code_value, '캐시 이름 코드 그룹' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.code_key=src.code_key AND tgt.code_value=src.code_value)
WHEN MATCHED THEN UPDATE SET tgt.description=src.description, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (parent_id, code_key, code_value, description, created_by, updated_by) VALUES (src.parent_id, src.code_key, src.code_value, src.description, src.created_by, src.updated_by);
MERGE INTO CMN_CODE tgt
USING (SELECT NULL AS parent_id, 'CODE_GROUP' AS code_key, 'BATCH_JOB_TYPE' AS code_value, '배치 Job 유형 코드 그룹' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.code_key=src.code_key AND tgt.code_value=src.code_value)
WHEN MATCHED THEN UPDATE SET tgt.description=src.description, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (parent_id, code_key, code_value, description, created_by, updated_by) VALUES (src.parent_id, src.code_key, src.code_value, src.description, src.created_by, src.updated_by);
MERGE INTO CMN_CODE tgt
USING (SELECT NULL AS parent_id, 'CODE_GROUP' AS code_key, 'YN' AS code_value, '여부 코드 그룹' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.code_key=src.code_key AND tgt.code_value=src.code_value)
WHEN MATCHED THEN UPDATE SET tgt.description=src.description, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (parent_id, code_key, code_value, description, created_by, updated_by) VALUES (src.parent_id, src.code_key, src.code_value, src.description, src.created_by, src.updated_by);
MERGE INTO CMN_CODE tgt
USING (SELECT (SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key = 'CODE_GROUP' AND code_value = 'MODULE') p) AS parent_id, 'MODULE' AS code_key, 'CPF' AS code_value, '프레임워크 공통 엔진' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.code_key=src.code_key AND tgt.code_value=src.code_value)
WHEN MATCHED THEN UPDATE SET tgt.parent_id=src.parent_id, tgt.description=src.description, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (parent_id, code_key, code_value, description, created_by, updated_by) VALUES (src.parent_id, src.code_key, src.code_value, src.description, src.created_by, src.updated_by);
MERGE INTO CMN_CODE tgt
USING (SELECT (SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key = 'CODE_GROUP' AND code_value = 'MODULE') p) AS parent_id, 'MODULE' AS code_key, 'CMN' AS code_value, '업무 공통 라이브러리' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.code_key=src.code_key AND tgt.code_value=src.code_value)
WHEN MATCHED THEN UPDATE SET tgt.parent_id=src.parent_id, tgt.description=src.description, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (parent_id, code_key, code_value, description, created_by, updated_by) VALUES (src.parent_id, src.code_key, src.code_value, src.description, src.created_by, src.updated_by);
MERGE INTO CMN_CODE tgt
USING (SELECT (SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key = 'CODE_GROUP' AND code_value = 'MODULE') p) AS parent_id, 'MODULE' AS code_key, 'ADM' AS code_value, '관리자 운영 서비스' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.code_key=src.code_key AND tgt.code_value=src.code_value)
WHEN MATCHED THEN UPDATE SET tgt.parent_id=src.parent_id, tgt.description=src.description, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (parent_id, code_key, code_value, description, created_by, updated_by) VALUES (src.parent_id, src.code_key, src.code_value, src.description, src.created_by, src.updated_by);
MERGE INTO CMN_CODE tgt
USING (SELECT (SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key = 'CODE_GROUP' AND code_value = 'MODULE') p) AS parent_id, 'MODULE' AS code_key, 'MBW' AS code_value, '업무 백오피스 서비스' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.code_key=src.code_key AND tgt.code_value=src.code_value)
WHEN MATCHED THEN UPDATE SET tgt.parent_id=src.parent_id, tgt.description=src.description, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (parent_id, code_key, code_value, description, created_by, updated_by) VALUES (src.parent_id, src.code_key, src.code_value, src.description, src.created_by, src.updated_by);
MERGE INTO CMN_CODE tgt
USING (SELECT (SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key = 'CODE_GROUP' AND code_value = 'MODULE') p) AS parent_id, 'MODULE' AS code_key, 'BAT' AS code_value, '선택 배치 실행 서비스' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.code_key=src.code_key AND tgt.code_value=src.code_value)
WHEN MATCHED THEN UPDATE SET tgt.parent_id=src.parent_id, tgt.description=src.description, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (parent_id, code_key, code_value, description, created_by, updated_by) VALUES (src.parent_id, src.code_key, src.code_value, src.description, src.created_by, src.updated_by);
MERGE INTO CMN_CODE tgt
USING (SELECT (SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key = 'CODE_GROUP' AND code_value = 'MODULE') p) AS parent_id, 'MODULE' AS code_key, 'EDU' AS code_value, '교육 샘플 서비스' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.code_key=src.code_key AND tgt.code_value=src.code_value)
WHEN MATCHED THEN UPDATE SET tgt.parent_id=src.parent_id, tgt.description=src.description, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (parent_id, code_key, code_value, description, created_by, updated_by) VALUES (src.parent_id, src.code_key, src.code_value, src.description, src.created_by, src.updated_by);
MERGE INTO CMN_CODE tgt
USING (SELECT (SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key = 'CODE_GROUP' AND code_value = 'REQUEST_TYPE') p) AS parent_id, 'REQUEST_TYPE' AS code_key, 'NORMAL' AS code_value, '일반 요청' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.code_key=src.code_key AND tgt.code_value=src.code_value)
WHEN MATCHED THEN UPDATE SET tgt.parent_id=src.parent_id, tgt.description=src.description, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (parent_id, code_key, code_value, description, created_by, updated_by) VALUES (src.parent_id, src.code_key, src.code_value, src.description, src.created_by, src.updated_by);
MERGE INTO CMN_CODE tgt
USING (SELECT (SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key = 'CODE_GROUP' AND code_value = 'REQUEST_TYPE') p) AS parent_id, 'REQUEST_TYPE' AS code_key, 'COMPENSATION' AS code_value, '보상 요청' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.code_key=src.code_key AND tgt.code_value=src.code_value)
WHEN MATCHED THEN UPDATE SET tgt.parent_id=src.parent_id, tgt.description=src.description, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (parent_id, code_key, code_value, description, created_by, updated_by) VALUES (src.parent_id, src.code_key, src.code_value, src.description, src.created_by, src.updated_by);
MERGE INTO CMN_CODE tgt
USING (SELECT (SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key = 'CODE_GROUP' AND code_value = 'REQUEST_TYPE') p) AS parent_id, 'REQUEST_TYPE' AS code_key, 'RETRY' AS code_value, '재시도 요청' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.code_key=src.code_key AND tgt.code_value=src.code_value)
WHEN MATCHED THEN UPDATE SET tgt.parent_id=src.parent_id, tgt.description=src.description, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (parent_id, code_key, code_value, description, created_by, updated_by) VALUES (src.parent_id, src.code_key, src.code_value, src.description, src.created_by, src.updated_by);
MERGE INTO CMN_CODE tgt
USING (SELECT (SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key = 'CODE_GROUP' AND code_value = 'CHANNEL_CODE') p) AS parent_id, 'CHANNEL_CODE' AS code_key, 'WEB' AS code_value, '웹 채널' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.code_key=src.code_key AND tgt.code_value=src.code_value)
WHEN MATCHED THEN UPDATE SET tgt.parent_id=src.parent_id, tgt.description=src.description, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (parent_id, code_key, code_value, description, created_by, updated_by) VALUES (src.parent_id, src.code_key, src.code_value, src.description, src.created_by, src.updated_by);
MERGE INTO CMN_CODE tgt
USING (SELECT (SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key = 'CODE_GROUP' AND code_value = 'CHANNEL_CODE') p) AS parent_id, 'CHANNEL_CODE' AS code_key, 'MOBILE' AS code_value, '모바일 채널' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.code_key=src.code_key AND tgt.code_value=src.code_value)
WHEN MATCHED THEN UPDATE SET tgt.parent_id=src.parent_id, tgt.description=src.description, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (parent_id, code_key, code_value, description, created_by, updated_by) VALUES (src.parent_id, src.code_key, src.code_value, src.description, src.created_by, src.updated_by);
MERGE INTO CMN_CODE tgt
USING (SELECT (SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key = 'CODE_GROUP' AND code_value = 'CHANNEL_CODE') p) AS parent_id, 'CHANNEL_CODE' AS code_key, 'BATCH' AS code_value, '배치 채널' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.code_key=src.code_key AND tgt.code_value=src.code_value)
WHEN MATCHED THEN UPDATE SET tgt.parent_id=src.parent_id, tgt.description=src.description, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (parent_id, code_key, code_value, description, created_by, updated_by) VALUES (src.parent_id, src.code_key, src.code_value, src.description, src.created_by, src.updated_by);
MERGE INTO CMN_CODE tgt
USING (SELECT (SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key = 'CODE_GROUP' AND code_value = 'CHANNEL_CODE') p) AS parent_id, 'CHANNEL_CODE' AS code_key, 'ADM' AS code_value, '관리자 채널' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.code_key=src.code_key AND tgt.code_value=src.code_value)
WHEN MATCHED THEN UPDATE SET tgt.parent_id=src.parent_id, tgt.description=src.description, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (parent_id, code_key, code_value, description, created_by, updated_by) VALUES (src.parent_id, src.code_key, src.code_value, src.description, src.created_by, src.updated_by);
MERGE INTO CMN_CODE tgt
USING (SELECT (SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key = 'CODE_GROUP' AND code_value = 'RESULT_TYPE') p) AS parent_id, 'RESULT_TYPE' AS code_key, 'S' AS code_value, '성공' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.code_key=src.code_key AND tgt.code_value=src.code_value)
WHEN MATCHED THEN UPDATE SET tgt.parent_id=src.parent_id, tgt.description=src.description, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (parent_id, code_key, code_value, description, created_by, updated_by) VALUES (src.parent_id, src.code_key, src.code_value, src.description, src.created_by, src.updated_by);
MERGE INTO CMN_CODE tgt
USING (SELECT (SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key = 'CODE_GROUP' AND code_value = 'RESULT_TYPE') p) AS parent_id, 'RESULT_TYPE' AS code_key, 'E' AS code_value, '오류' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.code_key=src.code_key AND tgt.code_value=src.code_value)
WHEN MATCHED THEN UPDATE SET tgt.parent_id=src.parent_id, tgt.description=src.description, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (parent_id, code_key, code_value, description, created_by, updated_by) VALUES (src.parent_id, src.code_key, src.code_value, src.description, src.created_by, src.updated_by);
MERGE INTO CMN_CODE tgt
USING (SELECT (SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key = 'CODE_GROUP' AND code_value = 'MESSAGE_FORMAT_TYPE') p) AS parent_id, 'MESSAGE_FORMAT_TYPE' AS code_key, 'FIXED' AS code_value, '고정 메시지' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.code_key=src.code_key AND tgt.code_value=src.code_value)
WHEN MATCHED THEN UPDATE SET tgt.parent_id=src.parent_id, tgt.description=src.description, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (parent_id, code_key, code_value, description, created_by, updated_by) VALUES (src.parent_id, src.code_key, src.code_value, src.description, src.created_by, src.updated_by);
MERGE INTO CMN_CODE tgt
USING (SELECT (SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key = 'CODE_GROUP' AND code_value = 'MESSAGE_FORMAT_TYPE') p) AS parent_id, 'MESSAGE_FORMAT_TYPE' AS code_key, 'INDEXED' AS code_value, '인덱스 파라미터 메시지' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.code_key=src.code_key AND tgt.code_value=src.code_value)
WHEN MATCHED THEN UPDATE SET tgt.parent_id=src.parent_id, tgt.description=src.description, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (parent_id, code_key, code_value, description, created_by, updated_by) VALUES (src.parent_id, src.code_key, src.code_value, src.description, src.created_by, src.updated_by);
MERGE INTO CMN_CODE tgt
USING (SELECT (SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key = 'CODE_GROUP' AND code_value = 'LOG_LEVEL') p) AS parent_id, 'LOG_LEVEL' AS code_key, 'TRACE' AS code_value, 'TRACE 로그' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.code_key=src.code_key AND tgt.code_value=src.code_value)
WHEN MATCHED THEN UPDATE SET tgt.parent_id=src.parent_id, tgt.description=src.description, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (parent_id, code_key, code_value, description, created_by, updated_by) VALUES (src.parent_id, src.code_key, src.code_value, src.description, src.created_by, src.updated_by);
MERGE INTO CMN_CODE tgt
USING (SELECT (SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key = 'CODE_GROUP' AND code_value = 'LOG_LEVEL') p) AS parent_id, 'LOG_LEVEL' AS code_key, 'DEBUG' AS code_value, 'DEBUG 로그' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.code_key=src.code_key AND tgt.code_value=src.code_value)
WHEN MATCHED THEN UPDATE SET tgt.parent_id=src.parent_id, tgt.description=src.description, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (parent_id, code_key, code_value, description, created_by, updated_by) VALUES (src.parent_id, src.code_key, src.code_value, src.description, src.created_by, src.updated_by);
MERGE INTO CMN_CODE tgt
USING (SELECT (SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key = 'CODE_GROUP' AND code_value = 'LOG_LEVEL') p) AS parent_id, 'LOG_LEVEL' AS code_key, 'INFO' AS code_value, 'INFO 로그' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.code_key=src.code_key AND tgt.code_value=src.code_value)
WHEN MATCHED THEN UPDATE SET tgt.parent_id=src.parent_id, tgt.description=src.description, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (parent_id, code_key, code_value, description, created_by, updated_by) VALUES (src.parent_id, src.code_key, src.code_value, src.description, src.created_by, src.updated_by);
MERGE INTO CMN_CODE tgt
USING (SELECT (SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key = 'CODE_GROUP' AND code_value = 'LOG_LEVEL') p) AS parent_id, 'LOG_LEVEL' AS code_key, 'WARN' AS code_value, 'WARN 로그' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.code_key=src.code_key AND tgt.code_value=src.code_value)
WHEN MATCHED THEN UPDATE SET tgt.parent_id=src.parent_id, tgt.description=src.description, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (parent_id, code_key, code_value, description, created_by, updated_by) VALUES (src.parent_id, src.code_key, src.code_value, src.description, src.created_by, src.updated_by);
MERGE INTO CMN_CODE tgt
USING (SELECT (SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key = 'CODE_GROUP' AND code_value = 'LOG_LEVEL') p) AS parent_id, 'LOG_LEVEL' AS code_key, 'ERROR' AS code_value, 'ERROR 로그' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.code_key=src.code_key AND tgt.code_value=src.code_value)
WHEN MATCHED THEN UPDATE SET tgt.parent_id=src.parent_id, tgt.description=src.description, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (parent_id, code_key, code_value, description, created_by, updated_by) VALUES (src.parent_id, src.code_key, src.code_value, src.description, src.created_by, src.updated_by);
MERGE INTO CMN_CODE tgt
USING (SELECT (SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key = 'CODE_GROUP' AND code_value = 'CACHE_NAME') p) AS parent_id, 'CACHE_NAME' AS code_key, 'ALL' AS code_value, '전체 캐시' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.code_key=src.code_key AND tgt.code_value=src.code_value)
WHEN MATCHED THEN UPDATE SET tgt.parent_id=src.parent_id, tgt.description=src.description, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (parent_id, code_key, code_value, description, created_by, updated_by) VALUES (src.parent_id, src.code_key, src.code_value, src.description, src.created_by, src.updated_by);
MERGE INTO CMN_CODE tgt
USING (SELECT (SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key = 'CODE_GROUP' AND code_value = 'CACHE_NAME') p) AS parent_id, 'CACHE_NAME' AS code_key, 'CODE' AS code_value, '코드 캐시' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.code_key=src.code_key AND tgt.code_value=src.code_value)
WHEN MATCHED THEN UPDATE SET tgt.parent_id=src.parent_id, tgt.description=src.description, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (parent_id, code_key, code_value, description, created_by, updated_by) VALUES (src.parent_id, src.code_key, src.code_value, src.description, src.created_by, src.updated_by);
MERGE INTO CMN_CODE tgt
USING (SELECT (SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key = 'CODE_GROUP' AND code_value = 'CACHE_NAME') p) AS parent_id, 'CACHE_NAME' AS code_key, 'MESSAGE' AS code_value, '메시지 캐시' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.code_key=src.code_key AND tgt.code_value=src.code_value)
WHEN MATCHED THEN UPDATE SET tgt.parent_id=src.parent_id, tgt.description=src.description, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (parent_id, code_key, code_value, description, created_by, updated_by) VALUES (src.parent_id, src.code_key, src.code_value, src.description, src.created_by, src.updated_by);
MERGE INTO CMN_CODE tgt
USING (SELECT (SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key = 'CODE_GROUP' AND code_value = 'CACHE_NAME') p) AS parent_id, 'CACHE_NAME' AS code_key, 'RESPONSE_CODE' AS code_value, '응답코드 캐시' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.code_key=src.code_key AND tgt.code_value=src.code_value)
WHEN MATCHED THEN UPDATE SET tgt.parent_id=src.parent_id, tgt.description=src.description, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (parent_id, code_key, code_value, description, created_by, updated_by) VALUES (src.parent_id, src.code_key, src.code_value, src.description, src.created_by, src.updated_by);
MERGE INTO CMN_CODE tgt
USING (SELECT (SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key = 'CODE_GROUP' AND code_value = 'CACHE_NAME') p) AS parent_id, 'CACHE_NAME' AS code_key, 'CONFIG' AS code_value, '설정 캐시' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.code_key=src.code_key AND tgt.code_value=src.code_value)
WHEN MATCHED THEN UPDATE SET tgt.parent_id=src.parent_id, tgt.description=src.description, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (parent_id, code_key, code_value, description, created_by, updated_by) VALUES (src.parent_id, src.code_key, src.code_value, src.description, src.created_by, src.updated_by);
MERGE INTO CMN_CODE tgt
USING (SELECT (SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key = 'CODE_GROUP' AND code_value = 'BATCH_JOB_TYPE') p) AS parent_id, 'BATCH_JOB_TYPE' AS code_key, 'TASKLET' AS code_value, 'Tasklet 배치' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.code_key=src.code_key AND tgt.code_value=src.code_value)
WHEN MATCHED THEN UPDATE SET tgt.parent_id=src.parent_id, tgt.description=src.description, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (parent_id, code_key, code_value, description, created_by, updated_by) VALUES (src.parent_id, src.code_key, src.code_value, src.description, src.created_by, src.updated_by);
MERGE INTO CMN_CODE tgt
USING (SELECT (SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key = 'CODE_GROUP' AND code_value = 'BATCH_JOB_TYPE') p) AS parent_id, 'BATCH_JOB_TYPE' AS code_key, 'CHUNK' AS code_value, 'Chunk 배치' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.code_key=src.code_key AND tgt.code_value=src.code_value)
WHEN MATCHED THEN UPDATE SET tgt.parent_id=src.parent_id, tgt.description=src.description, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (parent_id, code_key, code_value, description, created_by, updated_by) VALUES (src.parent_id, src.code_key, src.code_value, src.description, src.created_by, src.updated_by);
MERGE INTO CMN_CODE tgt
USING (SELECT (SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key = 'CODE_GROUP' AND code_value = 'BATCH_JOB_TYPE') p) AS parent_id, 'BATCH_JOB_TYPE' AS code_key, 'RETRY' AS code_value, '재처리 배치' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.code_key=src.code_key AND tgt.code_value=src.code_value)
WHEN MATCHED THEN UPDATE SET tgt.parent_id=src.parent_id, tgt.description=src.description, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (parent_id, code_key, code_value, description, created_by, updated_by) VALUES (src.parent_id, src.code_key, src.code_value, src.description, src.created_by, src.updated_by);
MERGE INTO CMN_CODE tgt
USING (SELECT (SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key = 'CODE_GROUP' AND code_value = 'YN') p) AS parent_id, 'YN' AS code_key, 'Y' AS code_value, '예' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.code_key=src.code_key AND tgt.code_value=src.code_value)
WHEN MATCHED THEN UPDATE SET tgt.parent_id=src.parent_id, tgt.description=src.description, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (parent_id, code_key, code_value, description, created_by, updated_by) VALUES (src.parent_id, src.code_key, src.code_value, src.description, src.created_by, src.updated_by);
MERGE INTO CMN_CODE tgt
USING (SELECT (SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key = 'CODE_GROUP' AND code_value = 'YN') p) AS parent_id, 'YN' AS code_key, 'N' AS code_value, '아니오' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.code_key=src.code_key AND tgt.code_value=src.code_value)
WHEN MATCHED THEN UPDATE SET tgt.parent_id=src.parent_id, tgt.description=src.description, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (parent_id, code_key, code_value, description, created_by, updated_by) VALUES (src.parent_id, src.code_key, src.code_value, src.description, src.created_by, src.updated_by);
MERGE INTO CMN_MESSAGE tgt
USING (SELECT 'MCPF000000' AS message_code, 'ko' AS locale, 'FIXED' AS message_format_type, '정상 처리되었습니다.' AS external_message, 'CPF 공통 요청이 정상 처리되었습니다.' AS internal_message, 0 AS parameter_count, NULL AS parameter_sample, 'CPF 공통 성공 메시지' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.message_code=src.message_code AND tgt.locale=src.locale)
WHEN MATCHED THEN UPDATE SET tgt.message_format_type=src.message_format_type, tgt.external_message=src.external_message, tgt.internal_message=src.internal_message, tgt.parameter_count=src.parameter_count, tgt.parameter_sample=src.parameter_sample, tgt.description=src.description, tgt.use_yn='Y', tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (message_code, locale, message_format_type, external_message, internal_message, parameter_count, parameter_sample, description, created_by, updated_by) VALUES (src.message_code, src.locale, src.message_format_type, src.external_message, src.internal_message, src.parameter_count, src.parameter_sample, src.description, src.created_by, src.updated_by);
MERGE INTO CMN_MESSAGE tgt
USING (SELECT 'MCPF010001' AS message_code, 'ko' AS locale, 'INDEXED' AS message_format_type, '요청 값이 올바르지 않습니다.' AS external_message, '요청 파라미터 검증에 실패했습니다. field={0}, value={1}' AS internal_message, 2 AS parameter_count, '["field","invalid"]' AS parameter_sample, 'CPF 파라미터 오류 메시지' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.message_code=src.message_code AND tgt.locale=src.locale)
WHEN MATCHED THEN UPDATE SET tgt.message_format_type=src.message_format_type, tgt.external_message=src.external_message, tgt.internal_message=src.internal_message, tgt.parameter_count=src.parameter_count, tgt.parameter_sample=src.parameter_sample, tgt.description=src.description, tgt.use_yn='Y', tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (message_code, locale, message_format_type, external_message, internal_message, parameter_count, parameter_sample, description, created_by, updated_by) VALUES (src.message_code, src.locale, src.message_format_type, src.external_message, src.internal_message, src.parameter_count, src.parameter_sample, src.description, src.created_by, src.updated_by);
MERGE INTO CMN_MESSAGE tgt
USING (SELECT 'MCPF010002' AS message_code, 'ko' AS locale, 'INDEXED' AS message_format_type, '요청한 정보를 찾을 수 없습니다.' AS external_message, '조회 대상 데이터가 존재하지 않습니다. target={0}' AS internal_message, 1 AS parameter_count, '["sample-item"]' AS parameter_sample, 'CPF 미존재 메시지' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.message_code=src.message_code AND tgt.locale=src.locale)
WHEN MATCHED THEN UPDATE SET tgt.message_format_type=src.message_format_type, tgt.external_message=src.external_message, tgt.internal_message=src.internal_message, tgt.parameter_count=src.parameter_count, tgt.parameter_sample=src.parameter_sample, tgt.description=src.description, tgt.use_yn='Y', tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (message_code, locale, message_format_type, external_message, internal_message, parameter_count, parameter_sample, description, created_by, updated_by) VALUES (src.message_code, src.locale, src.message_format_type, src.external_message, src.internal_message, src.parameter_count, src.parameter_sample, src.description, src.created_by, src.updated_by);
MERGE INTO CMN_MESSAGE tgt
USING (SELECT 'MCPF010003' AS message_code, 'ko' AS locale, 'INDEXED' AS message_format_type, '이미 등록된 정보입니다.' AS external_message, '중복 데이터가 감지되었습니다. key={0}' AS internal_message, 1 AS parameter_count, '["sampleKey"]' AS parameter_sample, 'CPF 중복 메시지' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.message_code=src.message_code AND tgt.locale=src.locale)
WHEN MATCHED THEN UPDATE SET tgt.message_format_type=src.message_format_type, tgt.external_message=src.external_message, tgt.internal_message=src.internal_message, tgt.parameter_count=src.parameter_count, tgt.parameter_sample=src.parameter_sample, tgt.description=src.description, tgt.use_yn='Y', tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (message_code, locale, message_format_type, external_message, internal_message, parameter_count, parameter_sample, description, created_by, updated_by) VALUES (src.message_code, src.locale, src.message_format_type, src.external_message, src.internal_message, src.parameter_count, src.parameter_sample, src.description, src.created_by, src.updated_by);
MERGE INTO CMN_MESSAGE tgt
USING (SELECT 'MCPF010004' AS message_code, 'ko' AS locale, 'INDEXED' AS message_format_type, '입력값을 확인해 주세요.' AS external_message, 'Bean Validation 검증에 실패했습니다. field={0}' AS internal_message, 1 AS parameter_count, '["name"]' AS parameter_sample, 'CPF 검증 메시지' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.message_code=src.message_code AND tgt.locale=src.locale)
WHEN MATCHED THEN UPDATE SET tgt.message_format_type=src.message_format_type, tgt.external_message=src.external_message, tgt.internal_message=src.internal_message, tgt.parameter_count=src.parameter_count, tgt.parameter_sample=src.parameter_sample, tgt.description=src.description, tgt.use_yn='Y', tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (message_code, locale, message_format_type, external_message, internal_message, parameter_count, parameter_sample, description, created_by, updated_by) VALUES (src.message_code, src.locale, src.message_format_type, src.external_message, src.internal_message, src.parameter_count, src.parameter_sample, src.description, src.created_by, src.updated_by);
MERGE INTO CMN_MESSAGE tgt
USING (SELECT 'MCPF010005' AS message_code, 'ko' AS locale, 'FIXED' AS message_format_type, '인증이 필요합니다.' AS external_message, '인증되지 않은 요청입니다.' AS internal_message, 0 AS parameter_count, NULL AS parameter_sample, 'CPF 인증 필요 메시지' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.message_code=src.message_code AND tgt.locale=src.locale)
WHEN MATCHED THEN UPDATE SET tgt.message_format_type=src.message_format_type, tgt.external_message=src.external_message, tgt.internal_message=src.internal_message, tgt.parameter_count=src.parameter_count, tgt.parameter_sample=src.parameter_sample, tgt.description=src.description, tgt.use_yn='Y', tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (message_code, locale, message_format_type, external_message, internal_message, parameter_count, parameter_sample, description, created_by, updated_by) VALUES (src.message_code, src.locale, src.message_format_type, src.external_message, src.internal_message, src.parameter_count, src.parameter_sample, src.description, src.created_by, src.updated_by);
MERGE INTO CMN_MESSAGE tgt
USING (SELECT 'MCPF010006' AS message_code, 'ko' AS locale, 'INDEXED' AS message_format_type, '처리 권한이 없습니다.' AS external_message, '인가되지 않은 요청입니다. user={0}' AS internal_message, 1 AS parameter_count, '["guest"]' AS parameter_sample, 'CPF 권한 오류 메시지' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.message_code=src.message_code AND tgt.locale=src.locale)
WHEN MATCHED THEN UPDATE SET tgt.message_format_type=src.message_format_type, tgt.external_message=src.external_message, tgt.internal_message=src.internal_message, tgt.parameter_count=src.parameter_count, tgt.parameter_sample=src.parameter_sample, tgt.description=src.description, tgt.use_yn='Y', tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (message_code, locale, message_format_type, external_message, internal_message, parameter_count, parameter_sample, description, created_by, updated_by) VALUES (src.message_code, src.locale, src.message_format_type, src.external_message, src.internal_message, src.parameter_count, src.parameter_sample, src.description, src.created_by, src.updated_by);
MERGE INTO CMN_MESSAGE tgt
USING (SELECT 'MCPF020001' AS message_code, 'ko' AS locale, 'INDEXED' AS message_format_type, '요청을 처리할 수 없습니다.' AS external_message, '업무 규칙 위반이 발생했습니다. rule={0}' AS internal_message, 1 AS parameter_count, '["business-rule"]' AS parameter_sample, 'CPF 업무 규칙 메시지' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.message_code=src.message_code AND tgt.locale=src.locale)
WHEN MATCHED THEN UPDATE SET tgt.message_format_type=src.message_format_type, tgt.external_message=src.external_message, tgt.internal_message=src.internal_message, tgt.parameter_count=src.parameter_count, tgt.parameter_sample=src.parameter_sample, tgt.description=src.description, tgt.use_yn='Y', tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (message_code, locale, message_format_type, external_message, internal_message, parameter_count, parameter_sample, description, created_by, updated_by) VALUES (src.message_code, src.locale, src.message_format_type, src.external_message, src.internal_message, src.parameter_count, src.parameter_sample, src.description, src.created_by, src.updated_by);
MERGE INTO CMN_MESSAGE tgt
USING (SELECT 'MCPF030001' AS message_code, 'ko' AS locale, 'INDEXED' AS message_format_type, '일시적으로 처리할 수 없습니다.' AS external_message, '외부 또는 타 주제영역 연계 오류가 발생했습니다. service={0}' AS internal_message, 1 AS parameter_count, '["generated-service"]' AS parameter_sample, 'CPF 외부 연계 메시지' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.message_code=src.message_code AND tgt.locale=src.locale)
WHEN MATCHED THEN UPDATE SET tgt.message_format_type=src.message_format_type, tgt.external_message=src.external_message, tgt.internal_message=src.internal_message, tgt.parameter_count=src.parameter_count, tgt.parameter_sample=src.parameter_sample, tgt.description=src.description, tgt.use_yn='Y', tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (message_code, locale, message_format_type, external_message, internal_message, parameter_count, parameter_sample, description, created_by, updated_by) VALUES (src.message_code, src.locale, src.message_format_type, src.external_message, src.internal_message, src.parameter_count, src.parameter_sample, src.description, src.created_by, src.updated_by);
MERGE INTO CMN_MESSAGE tgt
USING (SELECT 'MCPF900001' AS message_code, 'ko' AS locale, 'INDEXED' AS message_format_type, '필수 거래 헤더가 누락되었습니다.' AS external_message, 'CPF 거래 헤더 검증에 실패했습니다. header={0}, uri={1}' AS internal_message, 2 AS parameter_count, '["X-Request-Type","/api/sample-items"]' AS parameter_sample, 'CPF 헤더 검증 메시지' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.message_code=src.message_code AND tgt.locale=src.locale)
WHEN MATCHED THEN UPDATE SET tgt.message_format_type=src.message_format_type, tgt.external_message=src.external_message, tgt.internal_message=src.internal_message, tgt.parameter_count=src.parameter_count, tgt.parameter_sample=src.parameter_sample, tgt.description=src.description, tgt.use_yn='Y', tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (message_code, locale, message_format_type, external_message, internal_message, parameter_count, parameter_sample, description, created_by, updated_by) VALUES (src.message_code, src.locale, src.message_format_type, src.external_message, src.internal_message, src.parameter_count, src.parameter_sample, src.description, src.created_by, src.updated_by);
MERGE INTO CMN_MESSAGE tgt
USING (SELECT 'MCPF900002' AS message_code, 'ko' AS locale, 'INDEXED' AS message_format_type, '거래 메타데이터 설정이 올바르지 않습니다.' AS external_message, 'CPF @CpfTransaction 메타데이터 검증에 실패했습니다. transactionId={0}' AS internal_message, 1 AS parameter_count, '["OCPFSM0001"]' AS parameter_sample, 'CPF 메타데이터 메시지' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.message_code=src.message_code AND tgt.locale=src.locale)
WHEN MATCHED THEN UPDATE SET tgt.message_format_type=src.message_format_type, tgt.external_message=src.external_message, tgt.internal_message=src.internal_message, tgt.parameter_count=src.parameter_count, tgt.parameter_sample=src.parameter_sample, tgt.description=src.description, tgt.use_yn='Y', tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (message_code, locale, message_format_type, external_message, internal_message, parameter_count, parameter_sample, description, created_by, updated_by) VALUES (src.message_code, src.locale, src.message_format_type, src.external_message, src.internal_message, src.parameter_count, src.parameter_sample, src.description, src.created_by, src.updated_by);
MERGE INTO CMN_MESSAGE tgt
USING (SELECT 'MCPF900003' AS message_code, 'ko' AS locale, 'INDEXED' AS message_format_type, '서비스 접속 정보가 없습니다.' AS external_message, 'CPF 서비스 endpoint 설정을 찾을 수 없습니다. serviceId={0}' AS internal_message, 1 AS parameter_count, '["generated-service"]' AS parameter_sample, 'CPF endpoint 메시지' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.message_code=src.message_code AND tgt.locale=src.locale)
WHEN MATCHED THEN UPDATE SET tgt.message_format_type=src.message_format_type, tgt.external_message=src.external_message, tgt.internal_message=src.internal_message, tgt.parameter_count=src.parameter_count, tgt.parameter_sample=src.parameter_sample, tgt.description=src.description, tgt.use_yn='Y', tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (message_code, locale, message_format_type, external_message, internal_message, parameter_count, parameter_sample, description, created_by, updated_by) VALUES (src.message_code, src.locale, src.message_format_type, src.external_message, src.internal_message, src.parameter_count, src.parameter_sample, src.description, src.created_by, src.updated_by);
MERGE INTO CMN_MESSAGE tgt
USING (SELECT 'MCPF900004' AS message_code, 'ko' AS locale, 'INDEXED' AS message_format_type, '동적 로그레벨 요청이 올바르지 않습니다.' AS external_message, 'CPF 동적 로그레벨 규칙 검증에 실패했습니다. reason={0}' AS internal_message, 1 AS parameter_count, '["transactionId or businessTransactionId required"]' AS parameter_sample, 'CPF 동적 로그 메시지' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.message_code=src.message_code AND tgt.locale=src.locale)
WHEN MATCHED THEN UPDATE SET tgt.message_format_type=src.message_format_type, tgt.external_message=src.external_message, tgt.internal_message=src.internal_message, tgt.parameter_count=src.parameter_count, tgt.parameter_sample=src.parameter_sample, tgt.description=src.description, tgt.use_yn='Y', tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (message_code, locale, message_format_type, external_message, internal_message, parameter_count, parameter_sample, description, created_by, updated_by) VALUES (src.message_code, src.locale, src.message_format_type, src.external_message, src.internal_message, src.parameter_count, src.parameter_sample, src.description, src.created_by, src.updated_by);
MERGE INTO CMN_MESSAGE tgt
USING (SELECT 'MCPF900005' AS message_code, 'ko' AS locale, 'INDEXED' AS message_format_type, '내부 공유 API에 접근할 수 없습니다.' AS external_message, 'CPF 내부 서비스 신원 또는 호출 경로 검증에 실패했습니다. reason={0}' AS internal_message, 1 AS parameter_count, '["service identity verification failed"]' AS parameter_sample, 'CPF 내부 공유 API 접근 거부 메시지' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.message_code=src.message_code AND tgt.locale=src.locale)
WHEN MATCHED THEN UPDATE SET tgt.message_format_type=src.message_format_type, tgt.external_message=src.external_message, tgt.internal_message=src.internal_message, tgt.parameter_count=src.parameter_count, tgt.parameter_sample=src.parameter_sample, tgt.description=src.description, tgt.use_yn='Y', tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (message_code, locale, message_format_type, external_message, internal_message, parameter_count, parameter_sample, description, created_by, updated_by) VALUES (src.message_code, src.locale, src.message_format_type, src.external_message, src.internal_message, src.parameter_count, src.parameter_sample, src.description, src.created_by, src.updated_by);
MERGE INTO CMN_MESSAGE tgt
USING (SELECT 'MCPF990000' AS message_code, 'ko' AS locale, 'INDEXED' AS message_format_type, '처리 중 오류가 발생했습니다.' AS external_message, 'CPF 내부 오류가 발생했습니다. error={0}' AS internal_message, 1 AS parameter_count, '["Exception"]' AS parameter_sample, 'CPF 내부 오류 메시지' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.message_code=src.message_code AND tgt.locale=src.locale)
WHEN MATCHED THEN UPDATE SET tgt.message_format_type=src.message_format_type, tgt.external_message=src.external_message, tgt.internal_message=src.internal_message, tgt.parameter_count=src.parameter_count, tgt.parameter_sample=src.parameter_sample, tgt.description=src.description, tgt.use_yn='Y', tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (message_code, locale, message_format_type, external_message, internal_message, parameter_count, parameter_sample, description, created_by, updated_by) VALUES (src.message_code, src.locale, src.message_format_type, src.external_message, src.internal_message, src.parameter_count, src.parameter_sample, src.description, src.created_by, src.updated_by);
MERGE INTO CMN_MESSAGE tgt
USING (SELECT 'MCPF990001' AS message_code, 'ko' AS locale, 'INDEXED' AS message_format_type, '데이터베이스 오류가 발생했습니다.' AS external_message, '데이터베이스 처리 오류가 발생했습니다. sqlState={0}' AS internal_message, 1 AS parameter_count, '["HY000"]' AS parameter_sample, 'CPF 데이터베이스 오류 메시지' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.message_code=src.message_code AND tgt.locale=src.locale)
WHEN MATCHED THEN UPDATE SET tgt.message_format_type=src.message_format_type, tgt.external_message=src.external_message, tgt.internal_message=src.internal_message, tgt.parameter_count=src.parameter_count, tgt.parameter_sample=src.parameter_sample, tgt.description=src.description, tgt.use_yn='Y', tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (message_code, locale, message_format_type, external_message, internal_message, parameter_count, parameter_sample, description, created_by, updated_by) VALUES (src.message_code, src.locale, src.message_format_type, src.external_message, src.internal_message, src.parameter_count, src.parameter_sample, src.description, src.created_by, src.updated_by);
MERGE INTO CMN_MESSAGE tgt
USING (SELECT 'MMBW000000' AS message_code, 'ko' AS locale, 'FIXED' AS message_format_type, '성공' AS external_message, 'MBW 요청이 정상 처리되었습니다.' AS internal_message, 0 AS parameter_count, NULL AS parameter_sample, 'MBW 성공 메시지' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.message_code=src.message_code AND tgt.locale=src.locale)
WHEN MATCHED THEN UPDATE SET tgt.message_format_type=src.message_format_type, tgt.external_message=src.external_message, tgt.internal_message=src.internal_message, tgt.parameter_count=src.parameter_count, tgt.parameter_sample=src.parameter_sample, tgt.description=src.description, tgt.use_yn='Y', tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (message_code, locale, message_format_type, external_message, internal_message, parameter_count, parameter_sample, description, created_by, updated_by) VALUES (src.message_code, src.locale, src.message_format_type, src.external_message, src.internal_message, src.parameter_count, src.parameter_sample, src.description, src.created_by, src.updated_by);
MERGE INTO CMN_MESSAGE tgt
USING (SELECT 'MMBW010001' AS message_code, 'ko' AS locale, 'INDEXED' AS message_format_type, '업무 요청 값이 올바르지 않습니다.' AS external_message, 'MBW 입력값 검증에 실패했습니다. field={0}' AS internal_message, 1 AS parameter_count, '["field"]' AS parameter_sample, 'MBW 입력값 오류 메시지' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.message_code=src.message_code AND tgt.locale=src.locale)
WHEN MATCHED THEN UPDATE SET tgt.message_format_type=src.message_format_type, tgt.external_message=src.external_message, tgt.internal_message=src.internal_message, tgt.parameter_count=src.parameter_count, tgt.parameter_sample=src.parameter_sample, tgt.description=src.description, tgt.use_yn='Y', tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (message_code, locale, message_format_type, external_message, internal_message, parameter_count, parameter_sample, description, created_by, updated_by) VALUES (src.message_code, src.locale, src.message_format_type, src.external_message, src.internal_message, src.parameter_count, src.parameter_sample, src.description, src.created_by, src.updated_by);
MERGE INTO CMN_MESSAGE tgt
USING (SELECT 'MMBW010002' AS message_code, 'ko' AS locale, 'FIXED' AS message_format_type, '처리 권한이 없습니다.' AS external_message, 'MBW 서버 권한 검사에 실패했습니다.' AS internal_message, 0 AS parameter_count, NULL AS parameter_sample, 'MBW 권한 오류 메시지' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.message_code=src.message_code AND tgt.locale=src.locale)
WHEN MATCHED THEN UPDATE SET tgt.message_format_type=src.message_format_type, tgt.external_message=src.external_message, tgt.internal_message=src.internal_message, tgt.parameter_count=src.parameter_count, tgt.parameter_sample=src.parameter_sample, tgt.description=src.description, tgt.use_yn='Y', tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (message_code, locale, message_format_type, external_message, internal_message, parameter_count, parameter_sample, description, created_by, updated_by) VALUES (src.message_code, src.locale, src.message_format_type, src.external_message, src.internal_message, src.parameter_count, src.parameter_sample, src.description, src.created_by, src.updated_by);
MERGE INTO CMN_MESSAGE tgt
USING (SELECT 'MEDU010001' AS message_code, 'ko' AS locale, 'INDEXED' AS message_format_type, '이미 등록된 {0}입니다.' AS external_message, '{0}={1} 값이 이미 존재합니다. duplicateCheck=EDU_SAMPLE' AS internal_message, 2 AS parameter_count, '["샘플키","SAMPLE-0001"]' AS parameter_sample, 'EDU 동적 중복 교육 메시지' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.message_code=src.message_code AND tgt.locale=src.locale)
WHEN MATCHED THEN UPDATE SET tgt.message_format_type=src.message_format_type, tgt.external_message=src.external_message, tgt.internal_message=src.internal_message, tgt.parameter_count=src.parameter_count, tgt.parameter_sample=src.parameter_sample, tgt.description=src.description, tgt.use_yn='Y', tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (message_code, locale, message_format_type, external_message, internal_message, parameter_count, parameter_sample, description, created_by, updated_by) VALUES (src.message_code, src.locale, src.message_format_type, src.external_message, src.internal_message, src.parameter_count, src.parameter_sample, src.description, src.created_by, src.updated_by);
MERGE INTO CMN_MESSAGE tgt
USING (SELECT 'MCMN000001' AS message_code, 'ko' AS locale, 'FIXED' AS message_format_type, 'CPF 교육 시스템에 오신 것을 환영합니다.' AS external_message, 'CMN education welcome message.' AS internal_message, 0 AS parameter_count, NULL AS parameter_sample, 'CMN 교육 환영 메시지' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.message_code=src.message_code AND tgt.locale=src.locale)
WHEN MATCHED THEN UPDATE SET tgt.message_format_type=src.message_format_type, tgt.external_message=src.external_message, tgt.internal_message=src.internal_message, tgt.parameter_count=src.parameter_count, tgt.parameter_sample=src.parameter_sample, tgt.description=src.description, tgt.use_yn='Y', tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (message_code, locale, message_format_type, external_message, internal_message, parameter_count, parameter_sample, description, created_by, updated_by) VALUES (src.message_code, src.locale, src.message_format_type, src.external_message, src.internal_message, src.parameter_count, src.parameter_sample, src.description, src.created_by, src.updated_by);
MERGE INTO CMN_MESSAGE tgt
USING (SELECT 'MCMN000001' AS message_code, 'en' AS locale, 'FIXED' AS message_format_type, 'Welcome to the CPF education system.' AS external_message, 'CMN education welcome message.' AS internal_message, 0 AS parameter_count, NULL AS parameter_sample, 'CMN 교육 환영 메시지' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.message_code=src.message_code AND tgt.locale=src.locale)
WHEN MATCHED THEN UPDATE SET tgt.message_format_type=src.message_format_type, tgt.external_message=src.external_message, tgt.internal_message=src.internal_message, tgt.parameter_count=src.parameter_count, tgt.parameter_sample=src.parameter_sample, tgt.description=src.description, tgt.use_yn='Y', tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (message_code, locale, message_format_type, external_message, internal_message, parameter_count, parameter_sample, description, created_by, updated_by) VALUES (src.message_code, src.locale, src.message_format_type, src.external_message, src.internal_message, src.parameter_count, src.parameter_sample, src.description, src.created_by, src.updated_by);
MERGE INTO CMN_RESPONSE_CODE tgt
USING (SELECT 'SCPF000000' AS response_code, 'MCPF000000' AS message_code, 'S' AS result_type, 'CPF' AS module_id, '00' AS response_group, '0000' AS sequence_no, 200 AS http_status, 'CPF 공통 성공' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.response_code=src.response_code)
WHEN MATCHED THEN UPDATE SET tgt.message_code=src.message_code, tgt.result_type=src.result_type, tgt.module_id=src.module_id, tgt.response_group=src.response_group, tgt.sequence_no=src.sequence_no, tgt.http_status=src.http_status, tgt.description=src.description, tgt.use_yn='Y', tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (response_code, message_code, result_type, module_id, response_group, sequence_no, http_status, description, created_by, updated_by) VALUES (src.response_code, src.message_code, src.result_type, src.module_id, src.response_group, src.sequence_no, src.http_status, src.description, src.created_by, src.updated_by);
MERGE INTO CMN_RESPONSE_CODE tgt
USING (SELECT 'ECPF010001' AS response_code, 'MCPF010001' AS message_code, 'E' AS result_type, 'CPF' AS module_id, '01' AS response_group, '0001' AS sequence_no, 400 AS http_status, '파라미터 오류' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.response_code=src.response_code)
WHEN MATCHED THEN UPDATE SET tgt.message_code=src.message_code, tgt.result_type=src.result_type, tgt.module_id=src.module_id, tgt.response_group=src.response_group, tgt.sequence_no=src.sequence_no, tgt.http_status=src.http_status, tgt.description=src.description, tgt.use_yn='Y', tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (response_code, message_code, result_type, module_id, response_group, sequence_no, http_status, description, created_by, updated_by) VALUES (src.response_code, src.message_code, src.result_type, src.module_id, src.response_group, src.sequence_no, src.http_status, src.description, src.created_by, src.updated_by);
MERGE INTO CMN_RESPONSE_CODE tgt
USING (SELECT 'ECPF010002' AS response_code, 'MCPF010002' AS message_code, 'E' AS result_type, 'CPF' AS module_id, '01' AS response_group, '0002' AS sequence_no, 404 AS http_status, '미존재 오류' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.response_code=src.response_code)
WHEN MATCHED THEN UPDATE SET tgt.message_code=src.message_code, tgt.result_type=src.result_type, tgt.module_id=src.module_id, tgt.response_group=src.response_group, tgt.sequence_no=src.sequence_no, tgt.http_status=src.http_status, tgt.description=src.description, tgt.use_yn='Y', tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (response_code, message_code, result_type, module_id, response_group, sequence_no, http_status, description, created_by, updated_by) VALUES (src.response_code, src.message_code, src.result_type, src.module_id, src.response_group, src.sequence_no, src.http_status, src.description, src.created_by, src.updated_by);
MERGE INTO CMN_RESPONSE_CODE tgt
USING (SELECT 'ECPF010003' AS response_code, 'MCPF010003' AS message_code, 'E' AS result_type, 'CPF' AS module_id, '01' AS response_group, '0003' AS sequence_no, 409 AS http_status, '중복 오류' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.response_code=src.response_code)
WHEN MATCHED THEN UPDATE SET tgt.message_code=src.message_code, tgt.result_type=src.result_type, tgt.module_id=src.module_id, tgt.response_group=src.response_group, tgt.sequence_no=src.sequence_no, tgt.http_status=src.http_status, tgt.description=src.description, tgt.use_yn='Y', tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (response_code, message_code, result_type, module_id, response_group, sequence_no, http_status, description, created_by, updated_by) VALUES (src.response_code, src.message_code, src.result_type, src.module_id, src.response_group, src.sequence_no, src.http_status, src.description, src.created_by, src.updated_by);
MERGE INTO CMN_RESPONSE_CODE tgt
USING (SELECT 'ECPF010004' AS response_code, 'MCPF010004' AS message_code, 'E' AS result_type, 'CPF' AS module_id, '01' AS response_group, '0004' AS sequence_no, 400 AS http_status, '검증 실패' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.response_code=src.response_code)
WHEN MATCHED THEN UPDATE SET tgt.message_code=src.message_code, tgt.result_type=src.result_type, tgt.module_id=src.module_id, tgt.response_group=src.response_group, tgt.sequence_no=src.sequence_no, tgt.http_status=src.http_status, tgt.description=src.description, tgt.use_yn='Y', tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (response_code, message_code, result_type, module_id, response_group, sequence_no, http_status, description, created_by, updated_by) VALUES (src.response_code, src.message_code, src.result_type, src.module_id, src.response_group, src.sequence_no, src.http_status, src.description, src.created_by, src.updated_by);
MERGE INTO CMN_RESPONSE_CODE tgt
USING (SELECT 'ECPF010005' AS response_code, 'MCPF010005' AS message_code, 'E' AS result_type, 'CPF' AS module_id, '01' AS response_group, '0005' AS sequence_no, 401 AS http_status, '인증 필요' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.response_code=src.response_code)
WHEN MATCHED THEN UPDATE SET tgt.message_code=src.message_code, tgt.result_type=src.result_type, tgt.module_id=src.module_id, tgt.response_group=src.response_group, tgt.sequence_no=src.sequence_no, tgt.http_status=src.http_status, tgt.description=src.description, tgt.use_yn='Y', tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (response_code, message_code, result_type, module_id, response_group, sequence_no, http_status, description, created_by, updated_by) VALUES (src.response_code, src.message_code, src.result_type, src.module_id, src.response_group, src.sequence_no, src.http_status, src.description, src.created_by, src.updated_by);
MERGE INTO CMN_RESPONSE_CODE tgt
USING (SELECT 'ECPF010006' AS response_code, 'MCPF010006' AS message_code, 'E' AS result_type, 'CPF' AS module_id, '01' AS response_group, '0006' AS sequence_no, 403 AS http_status, '권한 없음' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.response_code=src.response_code)
WHEN MATCHED THEN UPDATE SET tgt.message_code=src.message_code, tgt.result_type=src.result_type, tgt.module_id=src.module_id, tgt.response_group=src.response_group, tgt.sequence_no=src.sequence_no, tgt.http_status=src.http_status, tgt.description=src.description, tgt.use_yn='Y', tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (response_code, message_code, result_type, module_id, response_group, sequence_no, http_status, description, created_by, updated_by) VALUES (src.response_code, src.message_code, src.result_type, src.module_id, src.response_group, src.sequence_no, src.http_status, src.description, src.created_by, src.updated_by);
MERGE INTO CMN_RESPONSE_CODE tgt
USING (SELECT 'ECPF020001' AS response_code, 'MCPF020001' AS message_code, 'E' AS result_type, 'CPF' AS module_id, '02' AS response_group, '0001' AS sequence_no, 400 AS http_status, '업무 규칙 위반' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.response_code=src.response_code)
WHEN MATCHED THEN UPDATE SET tgt.message_code=src.message_code, tgt.result_type=src.result_type, tgt.module_id=src.module_id, tgt.response_group=src.response_group, tgt.sequence_no=src.sequence_no, tgt.http_status=src.http_status, tgt.description=src.description, tgt.use_yn='Y', tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (response_code, message_code, result_type, module_id, response_group, sequence_no, http_status, description, created_by, updated_by) VALUES (src.response_code, src.message_code, src.result_type, src.module_id, src.response_group, src.sequence_no, src.http_status, src.description, src.created_by, src.updated_by);
MERGE INTO CMN_RESPONSE_CODE tgt
USING (SELECT 'ECPF030001' AS response_code, 'MCPF030001' AS message_code, 'E' AS result_type, 'CPF' AS module_id, '03' AS response_group, '0001' AS sequence_no, 502 AS http_status, '외부 연계 오류' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.response_code=src.response_code)
WHEN MATCHED THEN UPDATE SET tgt.message_code=src.message_code, tgt.result_type=src.result_type, tgt.module_id=src.module_id, tgt.response_group=src.response_group, tgt.sequence_no=src.sequence_no, tgt.http_status=src.http_status, tgt.description=src.description, tgt.use_yn='Y', tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (response_code, message_code, result_type, module_id, response_group, sequence_no, http_status, description, created_by, updated_by) VALUES (src.response_code, src.message_code, src.result_type, src.module_id, src.response_group, src.sequence_no, src.http_status, src.description, src.created_by, src.updated_by);
MERGE INTO CMN_RESPONSE_CODE tgt
USING (SELECT 'ECPF900001' AS response_code, 'MCPF900001' AS message_code, 'E' AS result_type, 'CPF' AS module_id, '90' AS response_group, '0001' AS sequence_no, 400 AS http_status, '필수 거래 헤더 누락' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.response_code=src.response_code)
WHEN MATCHED THEN UPDATE SET tgt.message_code=src.message_code, tgt.result_type=src.result_type, tgt.module_id=src.module_id, tgt.response_group=src.response_group, tgt.sequence_no=src.sequence_no, tgt.http_status=src.http_status, tgt.description=src.description, tgt.use_yn='Y', tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (response_code, message_code, result_type, module_id, response_group, sequence_no, http_status, description, created_by, updated_by) VALUES (src.response_code, src.message_code, src.result_type, src.module_id, src.response_group, src.sequence_no, src.http_status, src.description, src.created_by, src.updated_by);
MERGE INTO CMN_RESPONSE_CODE tgt
USING (SELECT 'ECPF900002' AS response_code, 'MCPF900002' AS message_code, 'E' AS result_type, 'CPF' AS module_id, '90' AS response_group, '0002' AS sequence_no, 500 AS http_status, '거래 메타데이터 오류' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.response_code=src.response_code)
WHEN MATCHED THEN UPDATE SET tgt.message_code=src.message_code, tgt.result_type=src.result_type, tgt.module_id=src.module_id, tgt.response_group=src.response_group, tgt.sequence_no=src.sequence_no, tgt.http_status=src.http_status, tgt.description=src.description, tgt.use_yn='Y', tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (response_code, message_code, result_type, module_id, response_group, sequence_no, http_status, description, created_by, updated_by) VALUES (src.response_code, src.message_code, src.result_type, src.module_id, src.response_group, src.sequence_no, src.http_status, src.description, src.created_by, src.updated_by);
MERGE INTO CMN_RESPONSE_CODE tgt
USING (SELECT 'ECPF900003' AS response_code, 'MCPF900003' AS message_code, 'E' AS result_type, 'CPF' AS module_id, '90' AS response_group, '0003' AS sequence_no, 500 AS http_status, '서비스 endpoint 미등록' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.response_code=src.response_code)
WHEN MATCHED THEN UPDATE SET tgt.message_code=src.message_code, tgt.result_type=src.result_type, tgt.module_id=src.module_id, tgt.response_group=src.response_group, tgt.sequence_no=src.sequence_no, tgt.http_status=src.http_status, tgt.description=src.description, tgt.use_yn='Y', tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (response_code, message_code, result_type, module_id, response_group, sequence_no, http_status, description, created_by, updated_by) VALUES (src.response_code, src.message_code, src.result_type, src.module_id, src.response_group, src.sequence_no, src.http_status, src.description, src.created_by, src.updated_by);
MERGE INTO CMN_RESPONSE_CODE tgt
USING (SELECT 'ECPF900004' AS response_code, 'MCPF900004' AS message_code, 'E' AS result_type, 'CPF' AS module_id, '90' AS response_group, '0004' AS sequence_no, 400 AS http_status, '동적 로그 규칙 오류' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.response_code=src.response_code)
WHEN MATCHED THEN UPDATE SET tgt.message_code=src.message_code, tgt.result_type=src.result_type, tgt.module_id=src.module_id, tgt.response_group=src.response_group, tgt.sequence_no=src.sequence_no, tgt.http_status=src.http_status, tgt.description=src.description, tgt.use_yn='Y', tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (response_code, message_code, result_type, module_id, response_group, sequence_no, http_status, description, created_by, updated_by) VALUES (src.response_code, src.message_code, src.result_type, src.module_id, src.response_group, src.sequence_no, src.http_status, src.description, src.created_by, src.updated_by);
MERGE INTO CMN_RESPONSE_CODE tgt
USING (SELECT 'ECPF900005' AS response_code, 'MCPF900005' AS message_code, 'E' AS result_type, 'CPF' AS module_id, '90' AS response_group, '0005' AS sequence_no, 403 AS http_status, '내부 공유 API 접근 거부' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.response_code=src.response_code)
WHEN MATCHED THEN UPDATE SET tgt.message_code=src.message_code, tgt.result_type=src.result_type, tgt.module_id=src.module_id, tgt.response_group=src.response_group, tgt.sequence_no=src.sequence_no, tgt.http_status=src.http_status, tgt.description=src.description, tgt.use_yn='Y', tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (response_code, message_code, result_type, module_id, response_group, sequence_no, http_status, description, created_by, updated_by) VALUES (src.response_code, src.message_code, src.result_type, src.module_id, src.response_group, src.sequence_no, src.http_status, src.description, src.created_by, src.updated_by);
MERGE INTO CMN_RESPONSE_CODE tgt
USING (SELECT 'ECPF990000' AS response_code, 'MCPF990000' AS message_code, 'E' AS result_type, 'CPF' AS module_id, '99' AS response_group, '0000' AS sequence_no, 500 AS http_status, '내부 서버 오류' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.response_code=src.response_code)
WHEN MATCHED THEN UPDATE SET tgt.message_code=src.message_code, tgt.result_type=src.result_type, tgt.module_id=src.module_id, tgt.response_group=src.response_group, tgt.sequence_no=src.sequence_no, tgt.http_status=src.http_status, tgt.description=src.description, tgt.use_yn='Y', tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (response_code, message_code, result_type, module_id, response_group, sequence_no, http_status, description, created_by, updated_by) VALUES (src.response_code, src.message_code, src.result_type, src.module_id, src.response_group, src.sequence_no, src.http_status, src.description, src.created_by, src.updated_by);
MERGE INTO CMN_RESPONSE_CODE tgt
USING (SELECT 'ECPF990001' AS response_code, 'MCPF990001' AS message_code, 'E' AS result_type, 'CPF' AS module_id, '99' AS response_group, '0001' AS sequence_no, 500 AS http_status, '데이터베이스 오류' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.response_code=src.response_code)
WHEN MATCHED THEN UPDATE SET tgt.message_code=src.message_code, tgt.result_type=src.result_type, tgt.module_id=src.module_id, tgt.response_group=src.response_group, tgt.sequence_no=src.sequence_no, tgt.http_status=src.http_status, tgt.description=src.description, tgt.use_yn='Y', tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (response_code, message_code, result_type, module_id, response_group, sequence_no, http_status, description, created_by, updated_by) VALUES (src.response_code, src.message_code, src.result_type, src.module_id, src.response_group, src.sequence_no, src.http_status, src.description, src.created_by, src.updated_by);
MERGE INTO CMN_RESPONSE_CODE tgt
USING (SELECT 'SMBW000000' AS response_code, 'MMBW000000' AS message_code, 'S' AS result_type, 'MBW' AS module_id, '00' AS response_group, '0000' AS sequence_no, 200 AS http_status, 'MBW 성공' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.response_code=src.response_code)
WHEN MATCHED THEN UPDATE SET tgt.message_code=src.message_code, tgt.result_type=src.result_type, tgt.module_id=src.module_id, tgt.response_group=src.response_group, tgt.sequence_no=src.sequence_no, tgt.http_status=src.http_status, tgt.description=src.description, tgt.use_yn='Y', tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (response_code, message_code, result_type, module_id, response_group, sequence_no, http_status, description, created_by, updated_by) VALUES (src.response_code, src.message_code, src.result_type, src.module_id, src.response_group, src.sequence_no, src.http_status, src.description, src.created_by, src.updated_by);
MERGE INTO CMN_RESPONSE_CODE tgt
USING (SELECT 'EMBW010001' AS response_code, 'MMBW010001' AS message_code, 'E' AS result_type, 'MBW' AS module_id, '01' AS response_group, '0001' AS sequence_no, 400 AS http_status, 'MBW 입력값 오류' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.response_code=src.response_code)
WHEN MATCHED THEN UPDATE SET tgt.message_code=src.message_code, tgt.result_type=src.result_type, tgt.module_id=src.module_id, tgt.response_group=src.response_group, tgt.sequence_no=src.sequence_no, tgt.http_status=src.http_status, tgt.description=src.description, tgt.use_yn='Y', tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (response_code, message_code, result_type, module_id, response_group, sequence_no, http_status, description, created_by, updated_by) VALUES (src.response_code, src.message_code, src.result_type, src.module_id, src.response_group, src.sequence_no, src.http_status, src.description, src.created_by, src.updated_by);
MERGE INTO CMN_RESPONSE_CODE tgt
USING (SELECT 'EMBW010002' AS response_code, 'MMBW010002' AS message_code, 'E' AS result_type, 'MBW' AS module_id, '01' AS response_group, '0002' AS sequence_no, 403 AS http_status, 'MBW 권한 오류' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.response_code=src.response_code)
WHEN MATCHED THEN UPDATE SET tgt.message_code=src.message_code, tgt.result_type=src.result_type, tgt.module_id=src.module_id, tgt.response_group=src.response_group, tgt.sequence_no=src.sequence_no, tgt.http_status=src.http_status, tgt.description=src.description, tgt.use_yn='Y', tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (response_code, message_code, result_type, module_id, response_group, sequence_no, http_status, description, created_by, updated_by) VALUES (src.response_code, src.message_code, src.result_type, src.module_id, src.response_group, src.sequence_no, src.http_status, src.description, src.created_by, src.updated_by);
MERGE INTO CMN_RESPONSE_CODE tgt
USING (SELECT 'EEDU010001' AS response_code, 'MEDU010001' AS message_code, 'E' AS result_type, 'EDU' AS module_id, '01' AS response_group, '0001' AS sequence_no, 409 AS http_status, 'EDU 샘플 중복 오류' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.response_code=src.response_code)
WHEN MATCHED THEN UPDATE SET tgt.message_code=src.message_code, tgt.result_type=src.result_type, tgt.module_id=src.module_id, tgt.response_group=src.response_group, tgt.sequence_no=src.sequence_no, tgt.http_status=src.http_status, tgt.description=src.description, tgt.use_yn='Y', tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (response_code, message_code, result_type, module_id, response_group, sequence_no, http_status, description, created_by, updated_by) VALUES (src.response_code, src.message_code, src.result_type, src.module_id, src.response_group, src.sequence_no, src.http_status, src.description, src.created_by, src.updated_by);
MERGE INTO CMN_PARAMETER tgt
USING (SELECT 'CPF.CMN.CACHE.PRELOAD_ENABLED' AS config_key, 'Y' AS config_value, 'BOOLEAN' AS config_type, 'CMN 캐시 기동 시 선적재 여부' AS description, 'N' AS encrypted_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.config_key=src.config_key)
WHEN MATCHED THEN UPDATE SET tgt.config_value=src.config_value, tgt.config_type=src.config_type, tgt.description=src.description, tgt.encrypted_yn=src.encrypted_yn, tgt.use_yn='Y', tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (config_key, config_value, config_type, description, encrypted_yn, created_by, updated_by) VALUES (src.config_key, src.config_value, src.config_type, src.description, src.encrypted_yn, src.created_by, src.updated_by);
MERGE INTO CMN_PARAMETER tgt
USING (SELECT 'CPF.CMN.CACHE.FAIL_FAST_ON_STARTUP' AS config_key, 'N' AS config_value, 'BOOLEAN' AS config_type, '캐시 선적재 실패 시 기동 실패 여부' AS description, 'N' AS encrypted_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.config_key=src.config_key)
WHEN MATCHED THEN UPDATE SET tgt.config_value=src.config_value, tgt.config_type=src.config_type, tgt.description=src.description, tgt.encrypted_yn=src.encrypted_yn, tgt.use_yn='Y', tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (config_key, config_value, config_type, description, encrypted_yn, created_by, updated_by) VALUES (src.config_key, src.config_value, src.config_type, src.description, src.encrypted_yn, src.created_by, src.updated_by);
MERGE INTO CMN_PARAMETER tgt
USING (SELECT 'CPF.CMN.CACHE.REFRESH_POLL_MILLIS' AS config_key, '5000' AS config_value, 'NUMBER' AS config_type, '캐시 갱신 이벤트 polling 주기' AS description, 'N' AS encrypted_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.config_key=src.config_key)
WHEN MATCHED THEN UPDATE SET tgt.config_value=src.config_value, tgt.config_type=src.config_type, tgt.description=src.description, tgt.encrypted_yn=src.encrypted_yn, tgt.use_yn='Y', tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (config_key, config_value, config_type, description, encrypted_yn, created_by, updated_by) VALUES (src.config_key, src.config_value, src.config_type, src.description, src.encrypted_yn, src.created_by, src.updated_by);
MERGE INTO CMN_PARAMETER tgt
USING (SELECT 'CPF.CMN.MESSAGING.BROKER' AS config_key, 'IN_MEMORY' AS config_value, 'STRING' AS config_type, '기본 CMN 메시지 브로커 유형' AS description, 'N' AS encrypted_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.config_key=src.config_key)
WHEN MATCHED THEN UPDATE SET tgt.config_value=src.config_value, tgt.config_type=src.config_type, tgt.description=src.description, tgt.encrypted_yn=src.encrypted_yn, tgt.use_yn='Y', tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (config_key, config_value, config_type, description, encrypted_yn, created_by, updated_by) VALUES (src.config_key, src.config_value, src.config_type, src.description, src.encrypted_yn, src.created_by, src.updated_by);
MERGE INTO CMN_PARAMETER tgt
USING (SELECT 'CPF.HTTP.CONNECT_TIMEOUT_MS' AS config_key, '3000' AS config_value, 'NUMBER' AS config_type, 'CPF HTTP client 연결 timeout' AS description, 'N' AS encrypted_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.config_key=src.config_key)
WHEN MATCHED THEN UPDATE SET tgt.config_value=src.config_value, tgt.config_type=src.config_type, tgt.description=src.description, tgt.encrypted_yn=src.encrypted_yn, tgt.use_yn='Y', tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (config_key, config_value, config_type, description, encrypted_yn, created_by, updated_by) VALUES (src.config_key, src.config_value, src.config_type, src.description, src.encrypted_yn, src.created_by, src.updated_by);
MERGE INTO CMN_PARAMETER tgt
USING (SELECT 'CPF.HTTP.READ_TIMEOUT_MS' AS config_key, '5000' AS config_value, 'NUMBER' AS config_type, 'CPF HTTP client 읽기 timeout' AS description, 'N' AS encrypted_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.config_key=src.config_key)
WHEN MATCHED THEN UPDATE SET tgt.config_value=src.config_value, tgt.config_type=src.config_type, tgt.description=src.description, tgt.encrypted_yn=src.encrypted_yn, tgt.use_yn='Y', tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (config_key, config_value, config_type, description, encrypted_yn, created_by, updated_by) VALUES (src.config_key, src.config_value, src.config_type, src.description, src.encrypted_yn, src.created_by, src.updated_by);
MERGE INTO CMN_PARAMETER tgt
USING (SELECT 'CPF.ADM.SESSION_TTL_SECONDS' AS config_key, '3600' AS config_value, 'NUMBER' AS config_type, 'ADM 세션 TTL 초' AS description, 'N' AS encrypted_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.config_key=src.config_key)
WHEN MATCHED THEN UPDATE SET tgt.config_value=src.config_value, tgt.config_type=src.config_type, tgt.description=src.description, tgt.encrypted_yn=src.encrypted_yn, tgt.use_yn='Y', tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (config_key, config_value, config_type, description, encrypted_yn, created_by, updated_by) VALUES (src.config_key, src.config_value, src.config_type, src.description, src.encrypted_yn, src.created_by, src.updated_by);
MERGE INTO CMN_PARAMETER tgt
USING (SELECT 'CPF.ADM.PASSWORD_EXPIRE_DAYS' AS config_key, '90' AS config_value, 'NUMBER' AS config_type, 'ADM 비밀번호 만료 일수' AS description, 'N' AS encrypted_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.config_key=src.config_key)
WHEN MATCHED THEN UPDATE SET tgt.config_value=src.config_value, tgt.config_type=src.config_type, tgt.description=src.description, tgt.encrypted_yn=src.encrypted_yn, tgt.use_yn='Y', tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (config_key, config_value, config_type, description, encrypted_yn, created_by, updated_by) VALUES (src.config_key, src.config_value, src.config_type, src.description, src.encrypted_yn, src.created_by, src.updated_by);
MERGE INTO CMN_PARAMETER tgt
USING (SELECT 'CPF.ADM.PASSWORD_MIN_LENGTH' AS config_key, '10' AS config_value, 'NUMBER' AS config_type, 'ADM 비밀번호 최소 길이' AS description, 'N' AS encrypted_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.config_key=src.config_key)
WHEN MATCHED THEN UPDATE SET tgt.config_value=src.config_value, tgt.config_type=src.config_type, tgt.description=src.description, tgt.encrypted_yn=src.encrypted_yn, tgt.use_yn='Y', tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (config_key, config_value, config_type, description, encrypted_yn, created_by, updated_by) VALUES (src.config_key, src.config_value, src.config_type, src.description, src.encrypted_yn, src.created_by, src.updated_by);
MERGE INTO CMN_PARAMETER tgt
USING (SELECT 'CPF.ADM.PASSWORD_MAX_FAIL_COUNT' AS config_key, '5' AS config_value, 'NUMBER' AS config_type, 'ADM 로그인 실패 잠금 기준' AS description, 'N' AS encrypted_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.config_key=src.config_key)
WHEN MATCHED THEN UPDATE SET tgt.config_value=src.config_value, tgt.config_type=src.config_type, tgt.description=src.description, tgt.encrypted_yn=src.encrypted_yn, tgt.use_yn='Y', tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (config_key, config_value, config_type, description, encrypted_yn, created_by, updated_by) VALUES (src.config_key, src.config_value, src.config_type, src.description, src.encrypted_yn, src.created_by, src.updated_by);
MERGE INTO CMN_PARAMETER tgt
USING (SELECT 'CPF.BATCH.DEFAULT_LOCK_SECONDS' AS config_key, '3600' AS config_value, 'NUMBER' AS config_type, '배치 기본 lock 만료 초' AS description, 'N' AS encrypted_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.config_key=src.config_key)
WHEN MATCHED THEN UPDATE SET tgt.config_value=src.config_value, tgt.config_type=src.config_type, tgt.description=src.description, tgt.encrypted_yn=src.encrypted_yn, tgt.use_yn='Y', tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (config_key, config_value, config_type, description, encrypted_yn, created_by, updated_by) VALUES (src.config_key, src.config_value, src.config_type, src.description, src.encrypted_yn, src.created_by, src.updated_by);
MERGE INTO CMN_PARAMETER tgt
USING (SELECT 'CPF.FEATURE.SAMPLE_ENABLED' AS config_key, 'Y' AS config_value, 'BOOLEAN' AS config_type, '샘플 API와 교육 flow 활성화 여부' AS description, 'N' AS encrypted_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.config_key=src.config_key)
WHEN MATCHED THEN UPDATE SET tgt.config_value=src.config_value, tgt.config_type=src.config_type, tgt.description=src.description, tgt.encrypted_yn=src.encrypted_yn, tgt.use_yn='Y', tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (config_key, config_value, config_type, description, encrypted_yn, created_by, updated_by) VALUES (src.config_key, src.config_value, src.config_type, src.description, src.encrypted_yn, src.created_by, src.updated_by);
MERGE INTO CMN_CODE tgt
USING (SELECT NULL AS parent_id, 'CODE_GROUP' AS code_key, 'HTTP_METHOD' AS code_value, 'HTTP Method 코드 그룹' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.code_key=src.code_key AND tgt.code_value=src.code_value)
WHEN MATCHED THEN UPDATE SET tgt.description=src.description, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (parent_id, code_key, code_value, description, created_by, updated_by) VALUES (src.parent_id, src.code_key, src.code_value, src.description, src.created_by, src.updated_by);
MERGE INTO CMN_CODE tgt
USING (SELECT NULL AS parent_id, 'CODE_GROUP' AS code_key, 'EXECUTION_STATUS' AS code_value, '실행 상태 코드 그룹' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.code_key=src.code_key AND tgt.code_value=src.code_value)
WHEN MATCHED THEN UPDATE SET tgt.description=src.description, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (parent_id, code_key, code_value, description, created_by, updated_by) VALUES (src.parent_id, src.code_key, src.code_value, src.description, src.created_by, src.updated_by);
MERGE INTO CMN_CODE tgt
USING (SELECT NULL AS parent_id, 'CODE_GROUP' AS code_key, 'ASYNC_STATUS' AS code_value, '비동기 처리 상태 코드 그룹' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.code_key=src.code_key AND tgt.code_value=src.code_value)
WHEN MATCHED THEN UPDATE SET tgt.description=src.description, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (parent_id, code_key, code_value, description, created_by, updated_by) VALUES (src.parent_id, src.code_key, src.code_value, src.description, src.created_by, src.updated_by);
MERGE INTO CMN_CODE tgt
USING (SELECT NULL AS parent_id, 'CODE_GROUP' AS code_key, 'RETRY_STATUS' AS code_value, '재시도 상태 코드 그룹' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.code_key=src.code_key AND tgt.code_value=src.code_value)
WHEN MATCHED THEN UPDATE SET tgt.description=src.description, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (parent_id, code_key, code_value, description, created_by, updated_by) VALUES (src.parent_id, src.code_key, src.code_value, src.description, src.created_by, src.updated_by);
MERGE INTO CMN_CODE tgt
USING (SELECT NULL AS parent_id, 'CODE_GROUP' AS code_key, 'IDEMPOTENCY_STATUS' AS code_value, '멱등 처리 상태 코드 그룹' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.code_key=src.code_key AND tgt.code_value=src.code_value)
WHEN MATCHED THEN UPDATE SET tgt.description=src.description, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (parent_id, code_key, code_value, description, created_by, updated_by) VALUES (src.parent_id, src.code_key, src.code_value, src.description, src.created_by, src.updated_by);
MERGE INTO CMN_CODE tgt
USING (SELECT NULL AS parent_id, 'CODE_GROUP' AS code_key, 'HEALTH_STATUS' AS code_value, 'Health 상태 코드 그룹' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.code_key=src.code_key AND tgt.code_value=src.code_value)
WHEN MATCHED THEN UPDATE SET tgt.description=src.description, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (parent_id, code_key, code_value, description, created_by, updated_by) VALUES (src.parent_id, src.code_key, src.code_value, src.description, src.created_by, src.updated_by);
MERGE INTO CMN_CODE tgt
USING (SELECT NULL AS parent_id, 'CODE_GROUP' AS code_key, 'CIRCUIT_STATUS' AS code_value, 'Circuit Breaker 상태 코드 그룹' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.code_key=src.code_key AND tgt.code_value=src.code_value)
WHEN MATCHED THEN UPDATE SET tgt.description=src.description, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (parent_id, code_key, code_value, description, created_by, updated_by) VALUES (src.parent_id, src.code_key, src.code_value, src.description, src.created_by, src.updated_by);
MERGE INTO CMN_CODE tgt
USING (SELECT NULL AS parent_id, 'CODE_GROUP' AS code_key, 'FILE_SCAN_STATUS' AS code_value, '첨부/파일 검사 상태 코드 그룹' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.code_key=src.code_key AND tgt.code_value=src.code_value)
WHEN MATCHED THEN UPDATE SET tgt.description=src.description, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (parent_id, code_key, code_value, description, created_by, updated_by) VALUES (src.parent_id, src.code_key, src.code_value, src.description, src.created_by, src.updated_by);
MERGE INTO CMN_CODE tgt
USING (SELECT NULL AS parent_id, 'CODE_GROUP' AS code_key, 'DATA_CLASSIFICATION' AS code_value, '데이터 민감도 등급 코드 그룹' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.code_key=src.code_key AND tgt.code_value=src.code_value)
WHEN MATCHED THEN UPDATE SET tgt.description=src.description, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (parent_id, code_key, code_value, description, created_by, updated_by) VALUES (src.parent_id, src.code_key, src.code_value, src.description, src.created_by, src.updated_by);
MERGE INTO CMN_CODE tgt
USING (SELECT NULL AS parent_id, 'CODE_GROUP' AS code_key, 'APPROVAL_STATUS' AS code_value, '결재 상태 코드 그룹' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.code_key=src.code_key AND tgt.code_value=src.code_value)
WHEN MATCHED THEN UPDATE SET tgt.description=src.description, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (parent_id, code_key, code_value, description, created_by, updated_by) VALUES (src.parent_id, src.code_key, src.code_value, src.description, src.created_by, src.updated_by);
MERGE INTO CMN_CODE tgt
USING (SELECT NULL AS parent_id, 'CODE_GROUP' AS code_key, 'ERROR_CATEGORY' AS code_value, '오류 분류 코드 그룹' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.code_key=src.code_key AND tgt.code_value=src.code_value)
WHEN MATCHED THEN UPDATE SET tgt.description=src.description, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (parent_id, code_key, code_value, description, created_by, updated_by) VALUES (src.parent_id, src.code_key, src.code_value, src.description, src.created_by, src.updated_by);
MERGE INTO CMN_CODE tgt
USING (SELECT NULL AS parent_id, 'CODE_GROUP' AS code_key, 'RETENTION_ACTION' AS code_value, '보존 정책 실행 유형 코드 그룹' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.code_key=src.code_key AND tgt.code_value=src.code_value)
WHEN MATCHED THEN UPDATE SET tgt.description=src.description, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (parent_id, code_key, code_value, description, created_by, updated_by) VALUES (src.parent_id, src.code_key, src.code_value, src.description, src.created_by, src.updated_by);
MERGE INTO CMN_CODE tgt
USING (SELECT (SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='HTTP_METHOD') x) AS parent_id, 'HTTP_METHOD' AS code_key, 'GET' AS code_value, '조회' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.code_key=src.code_key AND tgt.code_value=src.code_value)
WHEN MATCHED THEN UPDATE SET tgt.parent_id=src.parent_id, tgt.description=src.description, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (parent_id, code_key, code_value, description, created_by, updated_by) VALUES (src.parent_id, src.code_key, src.code_value, src.description, src.created_by, src.updated_by);
MERGE INTO CMN_CODE tgt
USING (SELECT (SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='HTTP_METHOD') x) AS parent_id, 'HTTP_METHOD' AS code_key, 'POST' AS code_value, '등록/명령' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.code_key=src.code_key AND tgt.code_value=src.code_value)
WHEN MATCHED THEN UPDATE SET tgt.parent_id=src.parent_id, tgt.description=src.description, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (parent_id, code_key, code_value, description, created_by, updated_by) VALUES (src.parent_id, src.code_key, src.code_value, src.description, src.created_by, src.updated_by);
MERGE INTO CMN_CODE tgt
USING (SELECT (SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='HTTP_METHOD') x) AS parent_id, 'HTTP_METHOD' AS code_key, 'PUT' AS code_value, '전체 수정' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.code_key=src.code_key AND tgt.code_value=src.code_value)
WHEN MATCHED THEN UPDATE SET tgt.parent_id=src.parent_id, tgt.description=src.description, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (parent_id, code_key, code_value, description, created_by, updated_by) VALUES (src.parent_id, src.code_key, src.code_value, src.description, src.created_by, src.updated_by);
MERGE INTO CMN_CODE tgt
USING (SELECT (SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='HTTP_METHOD') x) AS parent_id, 'HTTP_METHOD' AS code_key, 'PATCH' AS code_value, '부분 수정' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.code_key=src.code_key AND tgt.code_value=src.code_value)
WHEN MATCHED THEN UPDATE SET tgt.parent_id=src.parent_id, tgt.description=src.description, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (parent_id, code_key, code_value, description, created_by, updated_by) VALUES (src.parent_id, src.code_key, src.code_value, src.description, src.created_by, src.updated_by);
MERGE INTO CMN_CODE tgt
USING (SELECT (SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='HTTP_METHOD') x) AS parent_id, 'HTTP_METHOD' AS code_key, 'DELETE' AS code_value, '삭제/회수' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.code_key=src.code_key AND tgt.code_value=src.code_value)
WHEN MATCHED THEN UPDATE SET tgt.parent_id=src.parent_id, tgt.description=src.description, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (parent_id, code_key, code_value, description, created_by, updated_by) VALUES (src.parent_id, src.code_key, src.code_value, src.description, src.created_by, src.updated_by);
MERGE INTO CMN_CODE tgt
USING (SELECT (SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='EXECUTION_STATUS') x) AS parent_id, 'EXECUTION_STATUS' AS code_key, 'READY' AS code_value, '실행 준비' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.code_key=src.code_key AND tgt.code_value=src.code_value)
WHEN MATCHED THEN UPDATE SET tgt.parent_id=src.parent_id, tgt.description=src.description, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (parent_id, code_key, code_value, description, created_by, updated_by) VALUES (src.parent_id, src.code_key, src.code_value, src.description, src.created_by, src.updated_by);
MERGE INTO CMN_CODE tgt
USING (SELECT (SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='EXECUTION_STATUS') x) AS parent_id, 'EXECUTION_STATUS' AS code_key, 'RUNNING' AS code_value, '실행 중' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.code_key=src.code_key AND tgt.code_value=src.code_value)
WHEN MATCHED THEN UPDATE SET tgt.parent_id=src.parent_id, tgt.description=src.description, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (parent_id, code_key, code_value, description, created_by, updated_by) VALUES (src.parent_id, src.code_key, src.code_value, src.description, src.created_by, src.updated_by);
MERGE INTO CMN_CODE tgt
USING (SELECT (SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='EXECUTION_STATUS') x) AS parent_id, 'EXECUTION_STATUS' AS code_key, 'SUCCESS' AS code_value, '정상 완료' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.code_key=src.code_key AND tgt.code_value=src.code_value)
WHEN MATCHED THEN UPDATE SET tgt.parent_id=src.parent_id, tgt.description=src.description, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (parent_id, code_key, code_value, description, created_by, updated_by) VALUES (src.parent_id, src.code_key, src.code_value, src.description, src.created_by, src.updated_by);
MERGE INTO CMN_CODE tgt
USING (SELECT (SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='EXECUTION_STATUS') x) AS parent_id, 'EXECUTION_STATUS' AS code_key, 'FAILED' AS code_value, '실패' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.code_key=src.code_key AND tgt.code_value=src.code_value)
WHEN MATCHED THEN UPDATE SET tgt.parent_id=src.parent_id, tgt.description=src.description, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (parent_id, code_key, code_value, description, created_by, updated_by) VALUES (src.parent_id, src.code_key, src.code_value, src.description, src.created_by, src.updated_by);
MERGE INTO CMN_CODE tgt
USING (SELECT (SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='EXECUTION_STATUS') x) AS parent_id, 'EXECUTION_STATUS' AS code_key, 'UNKNOWN_RESULT' AS code_value, '결과 미확정' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.code_key=src.code_key AND tgt.code_value=src.code_value)
WHEN MATCHED THEN UPDATE SET tgt.parent_id=src.parent_id, tgt.description=src.description, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (parent_id, code_key, code_value, description, created_by, updated_by) VALUES (src.parent_id, src.code_key, src.code_value, src.description, src.created_by, src.updated_by);
MERGE INTO CMN_CODE tgt
USING (SELECT (SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='ASYNC_STATUS') x) AS parent_id, 'ASYNC_STATUS' AS code_key, 'WAITING' AS code_value, '비동기 대기' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.code_key=src.code_key AND tgt.code_value=src.code_value)
WHEN MATCHED THEN UPDATE SET tgt.parent_id=src.parent_id, tgt.description=src.description, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (parent_id, code_key, code_value, description, created_by, updated_by) VALUES (src.parent_id, src.code_key, src.code_value, src.description, src.created_by, src.updated_by);
MERGE INTO CMN_CODE tgt
USING (SELECT (SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='ASYNC_STATUS') x) AS parent_id, 'ASYNC_STATUS' AS code_key, 'PROCESSING' AS code_value, '비동기 처리 중' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.code_key=src.code_key AND tgt.code_value=src.code_value)
WHEN MATCHED THEN UPDATE SET tgt.parent_id=src.parent_id, tgt.description=src.description, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (parent_id, code_key, code_value, description, created_by, updated_by) VALUES (src.parent_id, src.code_key, src.code_value, src.description, src.created_by, src.updated_by);
MERGE INTO CMN_CODE tgt
USING (SELECT (SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='ASYNC_STATUS') x) AS parent_id, 'ASYNC_STATUS' AS code_key, 'COMPLETED' AS code_value, '비동기 완료' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.code_key=src.code_key AND tgt.code_value=src.code_value)
WHEN MATCHED THEN UPDATE SET tgt.parent_id=src.parent_id, tgt.description=src.description, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (parent_id, code_key, code_value, description, created_by, updated_by) VALUES (src.parent_id, src.code_key, src.code_value, src.description, src.created_by, src.updated_by);
MERGE INTO CMN_CODE tgt
USING (SELECT (SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='ASYNC_STATUS') x) AS parent_id, 'ASYNC_STATUS' AS code_key, 'DLQ' AS code_value, 'Dead Letter Queue' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.code_key=src.code_key AND tgt.code_value=src.code_value)
WHEN MATCHED THEN UPDATE SET tgt.parent_id=src.parent_id, tgt.description=src.description, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (parent_id, code_key, code_value, description, created_by, updated_by) VALUES (src.parent_id, src.code_key, src.code_value, src.description, src.created_by, src.updated_by);
MERGE INTO CMN_CODE tgt
USING (SELECT (SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='RETRY_STATUS') x) AS parent_id, 'RETRY_STATUS' AS code_key, 'RETRYABLE' AS code_value, '재시도 가능' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.code_key=src.code_key AND tgt.code_value=src.code_value)
WHEN MATCHED THEN UPDATE SET tgt.parent_id=src.parent_id, tgt.description=src.description, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (parent_id, code_key, code_value, description, created_by, updated_by) VALUES (src.parent_id, src.code_key, src.code_value, src.description, src.created_by, src.updated_by);
MERGE INTO CMN_CODE tgt
USING (SELECT (SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='RETRY_STATUS') x) AS parent_id, 'RETRY_STATUS' AS code_key, 'NON_RETRYABLE' AS code_value, '재시도 금지' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.code_key=src.code_key AND tgt.code_value=src.code_value)
WHEN MATCHED THEN UPDATE SET tgt.parent_id=src.parent_id, tgt.description=src.description, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (parent_id, code_key, code_value, description, created_by, updated_by) VALUES (src.parent_id, src.code_key, src.code_value, src.description, src.created_by, src.updated_by);
MERGE INTO CMN_CODE tgt
USING (SELECT (SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='RETRY_STATUS') x) AS parent_id, 'RETRY_STATUS' AS code_key, 'EXHAUSTED' AS code_value, '재시도 소진' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.code_key=src.code_key AND tgt.code_value=src.code_value)
WHEN MATCHED THEN UPDATE SET tgt.parent_id=src.parent_id, tgt.description=src.description, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (parent_id, code_key, code_value, description, created_by, updated_by) VALUES (src.parent_id, src.code_key, src.code_value, src.description, src.created_by, src.updated_by);
MERGE INTO CMN_CODE tgt
USING (SELECT (SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='IDEMPOTENCY_STATUS') x) AS parent_id, 'IDEMPOTENCY_STATUS' AS code_key, 'PROCESSING' AS code_value, '멱등 처리 중' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.code_key=src.code_key AND tgt.code_value=src.code_value)
WHEN MATCHED THEN UPDATE SET tgt.parent_id=src.parent_id, tgt.description=src.description, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (parent_id, code_key, code_value, description, created_by, updated_by) VALUES (src.parent_id, src.code_key, src.code_value, src.description, src.created_by, src.updated_by);
MERGE INTO CMN_CODE tgt
USING (SELECT (SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='IDEMPOTENCY_STATUS') x) AS parent_id, 'IDEMPOTENCY_STATUS' AS code_key, 'COMPLETED' AS code_value, '멱등 처리 완료' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.code_key=src.code_key AND tgt.code_value=src.code_value)
WHEN MATCHED THEN UPDATE SET tgt.parent_id=src.parent_id, tgt.description=src.description, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (parent_id, code_key, code_value, description, created_by, updated_by) VALUES (src.parent_id, src.code_key, src.code_value, src.description, src.created_by, src.updated_by);
MERGE INTO CMN_CODE tgt
USING (SELECT (SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='IDEMPOTENCY_STATUS') x) AS parent_id, 'IDEMPOTENCY_STATUS' AS code_key, 'FAILED' AS code_value, '멱등 처리 실패' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.code_key=src.code_key AND tgt.code_value=src.code_value)
WHEN MATCHED THEN UPDATE SET tgt.parent_id=src.parent_id, tgt.description=src.description, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (parent_id, code_key, code_value, description, created_by, updated_by) VALUES (src.parent_id, src.code_key, src.code_value, src.description, src.created_by, src.updated_by);
MERGE INTO CMN_CODE tgt
USING (SELECT (SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='IDEMPOTENCY_STATUS') x) AS parent_id, 'IDEMPOTENCY_STATUS' AS code_key, 'UNKNOWN_RESULT' AS code_value, '멱등 결과 미확정' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.code_key=src.code_key AND tgt.code_value=src.code_value)
WHEN MATCHED THEN UPDATE SET tgt.parent_id=src.parent_id, tgt.description=src.description, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (parent_id, code_key, code_value, description, created_by, updated_by) VALUES (src.parent_id, src.code_key, src.code_value, src.description, src.created_by, src.updated_by);
MERGE INTO CMN_CODE tgt
USING (SELECT (SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='HEALTH_STATUS') x) AS parent_id, 'HEALTH_STATUS' AS code_key, 'UP' AS code_value, '정상' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.code_key=src.code_key AND tgt.code_value=src.code_value)
WHEN MATCHED THEN UPDATE SET tgt.parent_id=src.parent_id, tgt.description=src.description, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (parent_id, code_key, code_value, description, created_by, updated_by) VALUES (src.parent_id, src.code_key, src.code_value, src.description, src.created_by, src.updated_by);
MERGE INTO CMN_CODE tgt
USING (SELECT (SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='HEALTH_STATUS') x) AS parent_id, 'HEALTH_STATUS' AS code_key, 'DOWN' AS code_value, '장애' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.code_key=src.code_key AND tgt.code_value=src.code_value)
WHEN MATCHED THEN UPDATE SET tgt.parent_id=src.parent_id, tgt.description=src.description, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (parent_id, code_key, code_value, description, created_by, updated_by) VALUES (src.parent_id, src.code_key, src.code_value, src.description, src.created_by, src.updated_by);
MERGE INTO CMN_CODE tgt
USING (SELECT (SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='HEALTH_STATUS') x) AS parent_id, 'HEALTH_STATUS' AS code_key, 'DEGRADED' AS code_value, '부분 저하' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.code_key=src.code_key AND tgt.code_value=src.code_value)
WHEN MATCHED THEN UPDATE SET tgt.parent_id=src.parent_id, tgt.description=src.description, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (parent_id, code_key, code_value, description, created_by, updated_by) VALUES (src.parent_id, src.code_key, src.code_value, src.description, src.created_by, src.updated_by);
MERGE INTO CMN_CODE tgt
USING (SELECT (SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='CIRCUIT_STATUS') x) AS parent_id, 'CIRCUIT_STATUS' AS code_key, 'CLOSED' AS code_value, '정상 호출' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.code_key=src.code_key AND tgt.code_value=src.code_value)
WHEN MATCHED THEN UPDATE SET tgt.parent_id=src.parent_id, tgt.description=src.description, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (parent_id, code_key, code_value, description, created_by, updated_by) VALUES (src.parent_id, src.code_key, src.code_value, src.description, src.created_by, src.updated_by);
MERGE INTO CMN_CODE tgt
USING (SELECT (SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='CIRCUIT_STATUS') x) AS parent_id, 'CIRCUIT_STATUS' AS code_key, 'OPEN' AS code_value, '호출 차단' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.code_key=src.code_key AND tgt.code_value=src.code_value)
WHEN MATCHED THEN UPDATE SET tgt.parent_id=src.parent_id, tgt.description=src.description, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (parent_id, code_key, code_value, description, created_by, updated_by) VALUES (src.parent_id, src.code_key, src.code_value, src.description, src.created_by, src.updated_by);
MERGE INTO CMN_CODE tgt
USING (SELECT (SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='CIRCUIT_STATUS') x) AS parent_id, 'CIRCUIT_STATUS' AS code_key, 'HALF_OPEN' AS code_value, '복구 시험' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.code_key=src.code_key AND tgt.code_value=src.code_value)
WHEN MATCHED THEN UPDATE SET tgt.parent_id=src.parent_id, tgt.description=src.description, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (parent_id, code_key, code_value, description, created_by, updated_by) VALUES (src.parent_id, src.code_key, src.code_value, src.description, src.created_by, src.updated_by);
MERGE INTO CMN_CODE tgt
USING (SELECT (SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='FILE_SCAN_STATUS') x) AS parent_id, 'FILE_SCAN_STATUS' AS code_key, 'PENDING' AS code_value, '검사 대기' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.code_key=src.code_key AND tgt.code_value=src.code_value)
WHEN MATCHED THEN UPDATE SET tgt.parent_id=src.parent_id, tgt.description=src.description, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (parent_id, code_key, code_value, description, created_by, updated_by) VALUES (src.parent_id, src.code_key, src.code_value, src.description, src.created_by, src.updated_by);
MERGE INTO CMN_CODE tgt
USING (SELECT (SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='FILE_SCAN_STATUS') x) AS parent_id, 'FILE_SCAN_STATUS' AS code_key, 'CLEAN' AS code_value, '검사 정상' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.code_key=src.code_key AND tgt.code_value=src.code_value)
WHEN MATCHED THEN UPDATE SET tgt.parent_id=src.parent_id, tgt.description=src.description, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (parent_id, code_key, code_value, description, created_by, updated_by) VALUES (src.parent_id, src.code_key, src.code_value, src.description, src.created_by, src.updated_by);
MERGE INTO CMN_CODE tgt
USING (SELECT (SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='FILE_SCAN_STATUS') x) AS parent_id, 'FILE_SCAN_STATUS' AS code_key, 'INFECTED' AS code_value, '악성 탐지' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.code_key=src.code_key AND tgt.code_value=src.code_value)
WHEN MATCHED THEN UPDATE SET tgt.parent_id=src.parent_id, tgt.description=src.description, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (parent_id, code_key, code_value, description, created_by, updated_by) VALUES (src.parent_id, src.code_key, src.code_value, src.description, src.created_by, src.updated_by);
MERGE INTO CMN_CODE tgt
USING (SELECT (SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='FILE_SCAN_STATUS') x) AS parent_id, 'FILE_SCAN_STATUS' AS code_key, 'FAILED' AS code_value, '검사 실패' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.code_key=src.code_key AND tgt.code_value=src.code_value)
WHEN MATCHED THEN UPDATE SET tgt.parent_id=src.parent_id, tgt.description=src.description, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (parent_id, code_key, code_value, description, created_by, updated_by) VALUES (src.parent_id, src.code_key, src.code_value, src.description, src.created_by, src.updated_by);
MERGE INTO CMN_CODE tgt
USING (SELECT (SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='FILE_SCAN_STATUS') x) AS parent_id, 'FILE_SCAN_STATUS' AS code_key, 'QUARANTINED' AS code_value, '격리' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.code_key=src.code_key AND tgt.code_value=src.code_value)
WHEN MATCHED THEN UPDATE SET tgt.parent_id=src.parent_id, tgt.description=src.description, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (parent_id, code_key, code_value, description, created_by, updated_by) VALUES (src.parent_id, src.code_key, src.code_value, src.description, src.created_by, src.updated_by);
MERGE INTO CMN_CODE tgt
USING (SELECT (SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='DATA_CLASSIFICATION') x) AS parent_id, 'DATA_CLASSIFICATION' AS code_key, 'PUBLIC' AS code_value, '공개 정보' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.code_key=src.code_key AND tgt.code_value=src.code_value)
WHEN MATCHED THEN UPDATE SET tgt.parent_id=src.parent_id, tgt.description=src.description, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (parent_id, code_key, code_value, description, created_by, updated_by) VALUES (src.parent_id, src.code_key, src.code_value, src.description, src.created_by, src.updated_by);
MERGE INTO CMN_CODE tgt
USING (SELECT (SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='DATA_CLASSIFICATION') x) AS parent_id, 'DATA_CLASSIFICATION' AS code_key, 'INTERNAL' AS code_value, '내부 정보' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.code_key=src.code_key AND tgt.code_value=src.code_value)
WHEN MATCHED THEN UPDATE SET tgt.parent_id=src.parent_id, tgt.description=src.description, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (parent_id, code_key, code_value, description, created_by, updated_by) VALUES (src.parent_id, src.code_key, src.code_value, src.description, src.created_by, src.updated_by);
MERGE INTO CMN_CODE tgt
USING (SELECT (SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='DATA_CLASSIFICATION') x) AS parent_id, 'DATA_CLASSIFICATION' AS code_key, 'CONFIDENTIAL' AS code_value, '기밀 정보' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.code_key=src.code_key AND tgt.code_value=src.code_value)
WHEN MATCHED THEN UPDATE SET tgt.parent_id=src.parent_id, tgt.description=src.description, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (parent_id, code_key, code_value, description, created_by, updated_by) VALUES (src.parent_id, src.code_key, src.code_value, src.description, src.created_by, src.updated_by);
MERGE INTO CMN_CODE tgt
USING (SELECT (SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='DATA_CLASSIFICATION') x) AS parent_id, 'DATA_CLASSIFICATION' AS code_key, 'RESTRICTED' AS code_value, '제한/민감 정보' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.code_key=src.code_key AND tgt.code_value=src.code_value)
WHEN MATCHED THEN UPDATE SET tgt.parent_id=src.parent_id, tgt.description=src.description, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (parent_id, code_key, code_value, description, created_by, updated_by) VALUES (src.parent_id, src.code_key, src.code_value, src.description, src.created_by, src.updated_by);
MERGE INTO CMN_CODE tgt
USING (SELECT (SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='APPROVAL_STATUS') x) AS parent_id, 'APPROVAL_STATUS' AS code_key, 'DRAFT' AS code_value, '작성 중' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.code_key=src.code_key AND tgt.code_value=src.code_value)
WHEN MATCHED THEN UPDATE SET tgt.parent_id=src.parent_id, tgt.description=src.description, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (parent_id, code_key, code_value, description, created_by, updated_by) VALUES (src.parent_id, src.code_key, src.code_value, src.description, src.created_by, src.updated_by);
MERGE INTO CMN_CODE tgt
USING (SELECT (SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='APPROVAL_STATUS') x) AS parent_id, 'APPROVAL_STATUS' AS code_key, 'IN_REVIEW' AS code_value, '결재 진행' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.code_key=src.code_key AND tgt.code_value=src.code_value)
WHEN MATCHED THEN UPDATE SET tgt.parent_id=src.parent_id, tgt.description=src.description, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (parent_id, code_key, code_value, description, created_by, updated_by) VALUES (src.parent_id, src.code_key, src.code_value, src.description, src.created_by, src.updated_by);
MERGE INTO CMN_CODE tgt
USING (SELECT (SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='APPROVAL_STATUS') x) AS parent_id, 'APPROVAL_STATUS' AS code_key, 'APPROVED' AS code_value, '승인 완료' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.code_key=src.code_key AND tgt.code_value=src.code_value)
WHEN MATCHED THEN UPDATE SET tgt.parent_id=src.parent_id, tgt.description=src.description, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (parent_id, code_key, code_value, description, created_by, updated_by) VALUES (src.parent_id, src.code_key, src.code_value, src.description, src.created_by, src.updated_by);
MERGE INTO CMN_CODE tgt
USING (SELECT (SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='APPROVAL_STATUS') x) AS parent_id, 'APPROVAL_STATUS' AS code_key, 'REJECTED' AS code_value, '반려' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.code_key=src.code_key AND tgt.code_value=src.code_value)
WHEN MATCHED THEN UPDATE SET tgt.parent_id=src.parent_id, tgt.description=src.description, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (parent_id, code_key, code_value, description, created_by, updated_by) VALUES (src.parent_id, src.code_key, src.code_value, src.description, src.created_by, src.updated_by);
MERGE INTO CMN_CODE tgt
USING (SELECT (SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='APPROVAL_STATUS') x) AS parent_id, 'APPROVAL_STATUS' AS code_key, 'WITHDRAWN' AS code_value, '철회' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.code_key=src.code_key AND tgt.code_value=src.code_value)
WHEN MATCHED THEN UPDATE SET tgt.parent_id=src.parent_id, tgt.description=src.description, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (parent_id, code_key, code_value, description, created_by, updated_by) VALUES (src.parent_id, src.code_key, src.code_value, src.description, src.created_by, src.updated_by);
MERGE INTO CMN_CODE tgt
USING (SELECT (SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='APPROVAL_STATUS') x) AS parent_id, 'APPROVAL_STATUS' AS code_key, 'CANCELED' AS code_value, '취소' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.code_key=src.code_key AND tgt.code_value=src.code_value)
WHEN MATCHED THEN UPDATE SET tgt.parent_id=src.parent_id, tgt.description=src.description, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (parent_id, code_key, code_value, description, created_by, updated_by) VALUES (src.parent_id, src.code_key, src.code_value, src.description, src.created_by, src.updated_by);
MERGE INTO CMN_CODE tgt
USING (SELECT (SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='APPROVAL_STATUS') x) AS parent_id, 'APPROVAL_STATUS' AS code_key, 'EXPIRED' AS code_value, '만료' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.code_key=src.code_key AND tgt.code_value=src.code_value)
WHEN MATCHED THEN UPDATE SET tgt.parent_id=src.parent_id, tgt.description=src.description, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (parent_id, code_key, code_value, description, created_by, updated_by) VALUES (src.parent_id, src.code_key, src.code_value, src.description, src.created_by, src.updated_by);
MERGE INTO CMN_CODE tgt
USING (SELECT (SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='ERROR_CATEGORY') x) AS parent_id, 'ERROR_CATEGORY' AS code_key, 'VALIDATION' AS code_value, '입력/계약 검증 오류' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.code_key=src.code_key AND tgt.code_value=src.code_value)
WHEN MATCHED THEN UPDATE SET tgt.parent_id=src.parent_id, tgt.description=src.description, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (parent_id, code_key, code_value, description, created_by, updated_by) VALUES (src.parent_id, src.code_key, src.code_value, src.description, src.created_by, src.updated_by);
MERGE INTO CMN_CODE tgt
USING (SELECT (SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='ERROR_CATEGORY') x) AS parent_id, 'ERROR_CATEGORY' AS code_key, 'AUTHENTICATION' AS code_value, '인증 오류' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.code_key=src.code_key AND tgt.code_value=src.code_value)
WHEN MATCHED THEN UPDATE SET tgt.parent_id=src.parent_id, tgt.description=src.description, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (parent_id, code_key, code_value, description, created_by, updated_by) VALUES (src.parent_id, src.code_key, src.code_value, src.description, src.created_by, src.updated_by);
MERGE INTO CMN_CODE tgt
USING (SELECT (SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='ERROR_CATEGORY') x) AS parent_id, 'ERROR_CATEGORY' AS code_key, 'AUTHORIZATION' AS code_value, '인가 오류' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.code_key=src.code_key AND tgt.code_value=src.code_value)
WHEN MATCHED THEN UPDATE SET tgt.parent_id=src.parent_id, tgt.description=src.description, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (parent_id, code_key, code_value, description, created_by, updated_by) VALUES (src.parent_id, src.code_key, src.code_value, src.description, src.created_by, src.updated_by);
MERGE INTO CMN_CODE tgt
USING (SELECT (SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='ERROR_CATEGORY') x) AS parent_id, 'ERROR_CATEGORY' AS code_key, 'CONFLICT' AS code_value, '동시성/중복 오류' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.code_key=src.code_key AND tgt.code_value=src.code_value)
WHEN MATCHED THEN UPDATE SET tgt.parent_id=src.parent_id, tgt.description=src.description, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (parent_id, code_key, code_value, description, created_by, updated_by) VALUES (src.parent_id, src.code_key, src.code_value, src.description, src.created_by, src.updated_by);
MERGE INTO CMN_CODE tgt
USING (SELECT (SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='ERROR_CATEGORY') x) AS parent_id, 'ERROR_CATEGORY' AS code_key, 'TIMEOUT' AS code_value, 'Timeout' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.code_key=src.code_key AND tgt.code_value=src.code_value)
WHEN MATCHED THEN UPDATE SET tgt.parent_id=src.parent_id, tgt.description=src.description, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (parent_id, code_key, code_value, description, created_by, updated_by) VALUES (src.parent_id, src.code_key, src.code_value, src.description, src.created_by, src.updated_by);
MERGE INTO CMN_CODE tgt
USING (SELECT (SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='ERROR_CATEGORY') x) AS parent_id, 'ERROR_CATEGORY' AS code_key, 'TARGET_DOWN' AS code_value, '호출 대상 장애' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.code_key=src.code_key AND tgt.code_value=src.code_value)
WHEN MATCHED THEN UPDATE SET tgt.parent_id=src.parent_id, tgt.description=src.description, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (parent_id, code_key, code_value, description, created_by, updated_by) VALUES (src.parent_id, src.code_key, src.code_value, src.description, src.created_by, src.updated_by);
MERGE INTO CMN_CODE tgt
USING (SELECT (SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='ERROR_CATEGORY') x) AS parent_id, 'ERROR_CATEGORY' AS code_key, 'UNKNOWN_RESULT' AS code_value, '결과 미확정' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.code_key=src.code_key AND tgt.code_value=src.code_value)
WHEN MATCHED THEN UPDATE SET tgt.parent_id=src.parent_id, tgt.description=src.description, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (parent_id, code_key, code_value, description, created_by, updated_by) VALUES (src.parent_id, src.code_key, src.code_value, src.description, src.created_by, src.updated_by);
MERGE INTO CMN_CODE tgt
USING (SELECT (SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='RETENTION_ACTION') x) AS parent_id, 'RETENTION_ACTION' AS code_key, 'ARCHIVE' AS code_value, '보관소 이관' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.code_key=src.code_key AND tgt.code_value=src.code_value)
WHEN MATCHED THEN UPDATE SET tgt.parent_id=src.parent_id, tgt.description=src.description, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (parent_id, code_key, code_value, description, created_by, updated_by) VALUES (src.parent_id, src.code_key, src.code_value, src.description, src.created_by, src.updated_by);
MERGE INTO CMN_CODE tgt
USING (SELECT (SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='RETENTION_ACTION') x) AS parent_id, 'RETENTION_ACTION' AS code_key, 'PURGE' AS code_value, '정책 삭제' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.code_key=src.code_key AND tgt.code_value=src.code_value)
WHEN MATCHED THEN UPDATE SET tgt.parent_id=src.parent_id, tgt.description=src.description, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (parent_id, code_key, code_value, description, created_by, updated_by) VALUES (src.parent_id, src.code_key, src.code_value, src.description, src.created_by, src.updated_by);
MERGE INTO CMN_CODE tgt
USING (SELECT (SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='RETENTION_ACTION') x) AS parent_id, 'RETENTION_ACTION' AS code_key, 'LEGAL_HOLD' AS code_value, '법적 보존' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.code_key=src.code_key AND tgt.code_value=src.code_value)
WHEN MATCHED THEN UPDATE SET tgt.parent_id=src.parent_id, tgt.description=src.description, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (parent_id, code_key, code_value, description, created_by, updated_by) VALUES (src.parent_id, src.code_key, src.code_value, src.description, src.created_by, src.updated_by);
MERGE INTO CMN_MESSAGE tgt
USING (SELECT 'MCPF030002' AS message_code, 'ko' AS locale, 'FIXED' AS message_format_type, '요청 시간이 초과되었습니다.' AS external_message, '대상 호출 timeout이 발생했습니다.' AS internal_message, 0 AS parameter_count, NULL AS parameter_sample, '공통 Timeout 메시지' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.message_code=src.message_code AND tgt.locale=src.locale)
WHEN MATCHED THEN UPDATE SET tgt.message_format_type=src.message_format_type, tgt.external_message=src.external_message, tgt.internal_message=src.internal_message, tgt.parameter_count=src.parameter_count, tgt.parameter_sample=src.parameter_sample, tgt.description=src.description, tgt.use_yn='Y', tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (message_code, locale, message_format_type, external_message, internal_message, parameter_count, parameter_sample, description, created_by, updated_by) VALUES (src.message_code, src.locale, src.message_format_type, src.external_message, src.internal_message, src.parameter_count, src.parameter_sample, src.description, src.created_by, src.updated_by);
MERGE INTO CMN_MESSAGE tgt
USING (SELECT 'MCPF030003' AS message_code, 'ko' AS locale, 'FIXED' AS message_format_type, '연결 대상이 일시적으로 사용할 수 없습니다.' AS external_message, '대상 서비스가 DOWN/OPEN 상태입니다.' AS internal_message, 0 AS parameter_count, NULL AS parameter_sample, 'Target down 메시지' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.message_code=src.message_code AND tgt.locale=src.locale)
WHEN MATCHED THEN UPDATE SET tgt.message_format_type=src.message_format_type, tgt.external_message=src.external_message, tgt.internal_message=src.internal_message, tgt.parameter_count=src.parameter_count, tgt.parameter_sample=src.parameter_sample, tgt.description=src.description, tgt.use_yn='Y', tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (message_code, locale, message_format_type, external_message, internal_message, parameter_count, parameter_sample, description, created_by, updated_by) VALUES (src.message_code, src.locale, src.message_format_type, src.external_message, src.internal_message, src.parameter_count, src.parameter_sample, src.description, src.created_by, src.updated_by);
MERGE INTO CMN_MESSAGE tgt
USING (SELECT 'MCPF030004' AS message_code, 'ko' AS locale, 'FIXED' AS message_format_type, '처리 결과를 확인 중입니다.' AS external_message, '요청 결과가 UNKNOWN_RESULT로 분류되어 대사가 필요합니다.' AS internal_message, 0 AS parameter_count, NULL AS parameter_sample, '결과 미확정 메시지' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.message_code=src.message_code AND tgt.locale=src.locale)
WHEN MATCHED THEN UPDATE SET tgt.message_format_type=src.message_format_type, tgt.external_message=src.external_message, tgt.internal_message=src.internal_message, tgt.parameter_count=src.parameter_count, tgt.parameter_sample=src.parameter_sample, tgt.description=src.description, tgt.use_yn='Y', tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (message_code, locale, message_format_type, external_message, internal_message, parameter_count, parameter_sample, description, created_by, updated_by) VALUES (src.message_code, src.locale, src.message_format_type, src.external_message, src.internal_message, src.parameter_count, src.parameter_sample, src.description, src.created_by, src.updated_by);
MERGE INTO CMN_MESSAGE tgt
USING (SELECT 'MCPF020002' AS message_code, 'ko' AS locale, 'FIXED' AS message_format_type, '다른 사용자가 먼저 변경했습니다. 다시 조회해 주세요.' AS external_message, '낙관적 잠금 Version 충돌이 발생했습니다.' AS internal_message, 0 AS parameter_count, NULL AS parameter_sample, '동시성 충돌 메시지' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.message_code=src.message_code AND tgt.locale=src.locale)
WHEN MATCHED THEN UPDATE SET tgt.message_format_type=src.message_format_type, tgt.external_message=src.external_message, tgt.internal_message=src.internal_message, tgt.parameter_count=src.parameter_count, tgt.parameter_sample=src.parameter_sample, tgt.description=src.description, tgt.use_yn='Y', tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (message_code, locale, message_format_type, external_message, internal_message, parameter_count, parameter_sample, description, created_by, updated_by) VALUES (src.message_code, src.locale, src.message_format_type, src.external_message, src.internal_message, src.parameter_count, src.parameter_sample, src.description, src.created_by, src.updated_by);
MERGE INTO CMN_MESSAGE tgt
USING (SELECT 'MCPF020003' AS message_code, 'ko' AS locale, 'FIXED' AS message_format_type, '동일 요청이 이미 처리되었습니다.' AS external_message, 'Idempotency key가 이미 완료된 요청입니다.' AS internal_message, 0 AS parameter_count, NULL AS parameter_sample, '멱등 중복 메시지' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.message_code=src.message_code AND tgt.locale=src.locale)
WHEN MATCHED THEN UPDATE SET tgt.message_format_type=src.message_format_type, tgt.external_message=src.external_message, tgt.internal_message=src.internal_message, tgt.parameter_count=src.parameter_count, tgt.parameter_sample=src.parameter_sample, tgt.description=src.description, tgt.use_yn='Y', tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (message_code, locale, message_format_type, external_message, internal_message, parameter_count, parameter_sample, description, created_by, updated_by) VALUES (src.message_code, src.locale, src.message_format_type, src.external_message, src.internal_message, src.parameter_count, src.parameter_sample, src.description, src.created_by, src.updated_by);
MERGE INTO CMN_MESSAGE tgt
USING (SELECT 'MCPF040001' AS message_code, 'ko' AS locale, 'FIXED' AS message_format_type, '첨부파일 검사가 완료되지 않았습니다.' AS external_message, '첨부 다운로드는 CLEAN 상태에서만 허용됩니다.' AS internal_message, 0 AS parameter_count, NULL AS parameter_sample, '첨부 보안 메시지' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.message_code=src.message_code AND tgt.locale=src.locale)
WHEN MATCHED THEN UPDATE SET tgt.message_format_type=src.message_format_type, tgt.external_message=src.external_message, tgt.internal_message=src.internal_message, tgt.parameter_count=src.parameter_count, tgt.parameter_sample=src.parameter_sample, tgt.description=src.description, tgt.use_yn='Y', tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (message_code, locale, message_format_type, external_message, internal_message, parameter_count, parameter_sample, description, created_by, updated_by) VALUES (src.message_code, src.locale, src.message_format_type, src.external_message, src.internal_message, src.parameter_count, src.parameter_sample, src.description, src.created_by, src.updated_by);
MERGE INTO CMN_MESSAGE tgt
USING (SELECT 'MCPF040002' AS message_code, 'ko' AS locale, 'FIXED' AS message_format_type, '첨부파일이 보안 정책에 의해 격리되었습니다.' AS external_message, 'INFECTED/QUARANTINED 파일 접근이 차단되었습니다.' AS internal_message, 0 AS parameter_count, NULL AS parameter_sample, '첨부 격리 메시지' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.message_code=src.message_code AND tgt.locale=src.locale)
WHEN MATCHED THEN UPDATE SET tgt.message_format_type=src.message_format_type, tgt.external_message=src.external_message, tgt.internal_message=src.internal_message, tgt.parameter_count=src.parameter_count, tgt.parameter_sample=src.parameter_sample, tgt.description=src.description, tgt.use_yn='Y', tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (message_code, locale, message_format_type, external_message, internal_message, parameter_count, parameter_sample, description, created_by, updated_by) VALUES (src.message_code, src.locale, src.message_format_type, src.external_message, src.internal_message, src.parameter_count, src.parameter_sample, src.description, src.created_by, src.updated_by);
MERGE INTO CMN_RESPONSE_CODE tgt
USING (SELECT 'ECPF030002' AS response_code, 'MCPF030002' AS message_code, 'E' AS result_type, 'CPF' AS module_id, '03' AS response_group, '0002' AS sequence_no, 504 AS http_status, 'Timeout' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.response_code=src.response_code)
WHEN MATCHED THEN UPDATE SET tgt.message_code=src.message_code, tgt.result_type=src.result_type, tgt.module_id=src.module_id, tgt.response_group=src.response_group, tgt.sequence_no=src.sequence_no, tgt.http_status=src.http_status, tgt.description=src.description, tgt.use_yn='Y', tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (response_code, message_code, result_type, module_id, response_group, sequence_no, http_status, description, created_by, updated_by) VALUES (src.response_code, src.message_code, src.result_type, src.module_id, src.response_group, src.sequence_no, src.http_status, src.description, src.created_by, src.updated_by);
MERGE INTO CMN_RESPONSE_CODE tgt
USING (SELECT 'ECPF030003' AS response_code, 'MCPF030003' AS message_code, 'E' AS result_type, 'CPF' AS module_id, '03' AS response_group, '0003' AS sequence_no, 503 AS http_status, 'Target down' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.response_code=src.response_code)
WHEN MATCHED THEN UPDATE SET tgt.message_code=src.message_code, tgt.result_type=src.result_type, tgt.module_id=src.module_id, tgt.response_group=src.response_group, tgt.sequence_no=src.sequence_no, tgt.http_status=src.http_status, tgt.description=src.description, tgt.use_yn='Y', tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (response_code, message_code, result_type, module_id, response_group, sequence_no, http_status, description, created_by, updated_by) VALUES (src.response_code, src.message_code, src.result_type, src.module_id, src.response_group, src.sequence_no, src.http_status, src.description, src.created_by, src.updated_by);
MERGE INTO CMN_RESPONSE_CODE tgt
USING (SELECT 'ECPF030004' AS response_code, 'MCPF030004' AS message_code, 'E' AS result_type, 'CPF' AS module_id, '03' AS response_group, '0004' AS sequence_no, 202 AS http_status, 'UNKNOWN_RESULT' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.response_code=src.response_code)
WHEN MATCHED THEN UPDATE SET tgt.message_code=src.message_code, tgt.result_type=src.result_type, tgt.module_id=src.module_id, tgt.response_group=src.response_group, tgt.sequence_no=src.sequence_no, tgt.http_status=src.http_status, tgt.description=src.description, tgt.use_yn='Y', tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (response_code, message_code, result_type, module_id, response_group, sequence_no, http_status, description, created_by, updated_by) VALUES (src.response_code, src.message_code, src.result_type, src.module_id, src.response_group, src.sequence_no, src.http_status, src.description, src.created_by, src.updated_by);
MERGE INTO CMN_RESPONSE_CODE tgt
USING (SELECT 'ECPF020002' AS response_code, 'MCPF020002' AS message_code, 'E' AS result_type, 'CPF' AS module_id, '02' AS response_group, '0002' AS sequence_no, 409 AS http_status, 'Optimistic lock conflict' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.response_code=src.response_code)
WHEN MATCHED THEN UPDATE SET tgt.message_code=src.message_code, tgt.result_type=src.result_type, tgt.module_id=src.module_id, tgt.response_group=src.response_group, tgt.sequence_no=src.sequence_no, tgt.http_status=src.http_status, tgt.description=src.description, tgt.use_yn='Y', tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (response_code, message_code, result_type, module_id, response_group, sequence_no, http_status, description, created_by, updated_by) VALUES (src.response_code, src.message_code, src.result_type, src.module_id, src.response_group, src.sequence_no, src.http_status, src.description, src.created_by, src.updated_by);
MERGE INTO CMN_RESPONSE_CODE tgt
USING (SELECT 'ECPF020003' AS response_code, 'MCPF020003' AS message_code, 'E' AS result_type, 'CPF' AS module_id, '02' AS response_group, '0003' AS sequence_no, 409 AS http_status, 'Idempotency duplicate' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.response_code=src.response_code)
WHEN MATCHED THEN UPDATE SET tgt.message_code=src.message_code, tgt.result_type=src.result_type, tgt.module_id=src.module_id, tgt.response_group=src.response_group, tgt.sequence_no=src.sequence_no, tgt.http_status=src.http_status, tgt.description=src.description, tgt.use_yn='Y', tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (response_code, message_code, result_type, module_id, response_group, sequence_no, http_status, description, created_by, updated_by) VALUES (src.response_code, src.message_code, src.result_type, src.module_id, src.response_group, src.sequence_no, src.http_status, src.description, src.created_by, src.updated_by);
MERGE INTO CMN_RESPONSE_CODE tgt
USING (SELECT 'ECPF040001' AS response_code, 'MCPF040001' AS message_code, 'E' AS result_type, 'CPF' AS module_id, '04' AS response_group, '0001' AS sequence_no, 423 AS http_status, 'File scan pending' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.response_code=src.response_code)
WHEN MATCHED THEN UPDATE SET tgt.message_code=src.message_code, tgt.result_type=src.result_type, tgt.module_id=src.module_id, tgt.response_group=src.response_group, tgt.sequence_no=src.sequence_no, tgt.http_status=src.http_status, tgt.description=src.description, tgt.use_yn='Y', tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (response_code, message_code, result_type, module_id, response_group, sequence_no, http_status, description, created_by, updated_by) VALUES (src.response_code, src.message_code, src.result_type, src.module_id, src.response_group, src.sequence_no, src.http_status, src.description, src.created_by, src.updated_by);
MERGE INTO CMN_RESPONSE_CODE tgt
USING (SELECT 'ECPF040002' AS response_code, 'MCPF040002' AS message_code, 'E' AS result_type, 'CPF' AS module_id, '04' AS response_group, '0002' AS sequence_no, 403 AS http_status, 'File quarantined' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.response_code=src.response_code)
WHEN MATCHED THEN UPDATE SET tgt.message_code=src.message_code, tgt.result_type=src.result_type, tgt.module_id=src.module_id, tgt.response_group=src.response_group, tgt.sequence_no=src.sequence_no, tgt.http_status=src.http_status, tgt.description=src.description, tgt.use_yn='Y', tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (response_code, message_code, result_type, module_id, response_group, sequence_no, http_status, description, created_by, updated_by) VALUES (src.response_code, src.message_code, src.result_type, src.module_id, src.response_group, src.sequence_no, src.http_status, src.description, src.created_by, src.updated_by);
MERGE INTO CMN_PARAMETER tgt
USING (SELECT 'CPF.MBW.SECURITY.MAX_LOGIN_FAIL_COUNT' AS config_key, '5' AS config_value, 'NUMBER' AS config_type, 'MBW 로그인 실패 잠금 기준' AS description, 'N' AS encrypted_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.config_key=src.config_key)
WHEN MATCHED THEN UPDATE SET tgt.config_value=src.config_value, tgt.config_type=src.config_type, tgt.description=src.description, tgt.encrypted_yn=src.encrypted_yn, tgt.use_yn='Y', tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (config_key, config_value, config_type, description, encrypted_yn, created_by, updated_by) VALUES (src.config_key, src.config_value, src.config_type, src.description, src.encrypted_yn, src.created_by, src.updated_by);
MERGE INTO CMN_PARAMETER tgt
USING (SELECT 'CPF.MBW.SECURITY.ACCESS_TOKEN_TTL_SECONDS' AS config_key, '600' AS config_value, 'NUMBER' AS config_type, 'MBW Access Token TTL' AS description, 'N' AS encrypted_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.config_key=src.config_key)
WHEN MATCHED THEN UPDATE SET tgt.config_value=src.config_value, tgt.config_type=src.config_type, tgt.description=src.description, tgt.encrypted_yn=src.encrypted_yn, tgt.use_yn='Y', tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (config_key, config_value, config_type, description, encrypted_yn, created_by, updated_by) VALUES (src.config_key, src.config_value, src.config_type, src.description, src.encrypted_yn, src.created_by, src.updated_by);
MERGE INTO CMN_PARAMETER tgt
USING (SELECT 'CPF.MBW.SECURITY.REFRESH_TOKEN_TTL_SECONDS' AS config_key, '7200' AS config_value, 'NUMBER' AS config_type, 'MBW Refresh Token TTL' AS description, 'N' AS encrypted_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.config_key=src.config_key)
WHEN MATCHED THEN UPDATE SET tgt.config_value=src.config_value, tgt.config_type=src.config_type, tgt.description=src.description, tgt.encrypted_yn=src.encrypted_yn, tgt.use_yn='Y', tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (config_key, config_value, config_type, description, encrypted_yn, created_by, updated_by) VALUES (src.config_key, src.config_value, src.config_type, src.description, src.encrypted_yn, src.created_by, src.updated_by);
MERGE INTO CMN_PARAMETER tgt
USING (SELECT 'CPF.RETENTION.EXECUTE_ENABLED' AS config_key, 'N' AS config_value, 'BOOLEAN' AS config_type, '실제 Archive/Purge 실행 Kill Switch 기본 OFF' AS description, 'N' AS encrypted_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.config_key=src.config_key)
WHEN MATCHED THEN UPDATE SET tgt.config_value=src.config_value, tgt.config_type=src.config_type, tgt.description=src.description, tgt.encrypted_yn=src.encrypted_yn, tgt.use_yn='Y', tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (config_key, config_value, config_type, description, encrypted_yn, created_by, updated_by) VALUES (src.config_key, src.config_value, src.config_type, src.description, src.encrypted_yn, src.created_by, src.updated_by);
MERGE INTO CMN_PARAMETER tgt
USING (SELECT 'CPF.FILE.DOWNLOAD_REQUIRE_CLEAN' AS config_key, 'Y' AS config_value, 'BOOLEAN' AS config_type, '첨부 다운로드 CLEAN 상태 강제' AS description, 'N' AS encrypted_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.config_key=src.config_key)
WHEN MATCHED THEN UPDATE SET tgt.config_value=src.config_value, tgt.config_type=src.config_type, tgt.description=src.description, tgt.encrypted_yn=src.encrypted_yn, tgt.use_yn='Y', tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (config_key, config_value, config_type, description, encrypted_yn, created_by, updated_by) VALUES (src.config_key, src.config_value, src.config_type, src.description, src.encrypted_yn, src.created_by, src.updated_by);
MERGE INTO CMN_PARAMETER tgt
USING (SELECT 'CPF.HEALTH.INSTANCE_ID_REQUIRED' AS config_key, 'Y' AS config_value, 'BOOLEAN' AS config_type, '운영 Health 응답 인스턴스 식별자 필수' AS description, 'N' AS encrypted_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.config_key=src.config_key)
WHEN MATCHED THEN UPDATE SET tgt.config_value=src.config_value, tgt.config_type=src.config_type, tgt.description=src.description, tgt.encrypted_yn=src.encrypted_yn, tgt.use_yn='Y', tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (config_key, config_value, config_type, description, encrypted_yn, created_by, updated_by) VALUES (src.config_key, src.config_value, src.config_type, src.description, src.encrypted_yn, src.created_by, src.updated_by);
MERGE INTO OPS_LOG_POLICY tgt
USING (SELECT 'ONLINE_DEFAULT' AS policy_key, '온라인 거래 기본 로그 정책' AS policy_name, 'ONLINE_TRANSACTION' AS target_type, '*' AS target_id, 'INFO' AS log_level, 'Y' AS db_log_enabled_yn, 'Y' AS file_log_enabled_yn, 2 AS policy_schema_version, 'NONE' AS query_capture_mode, 'ALLOWLIST' AS request_header_capture_mode, 'ALLOWLIST' AS response_header_capture_mode, 'NONE' AS request_body_capture_mode, 'NONE' AS response_body_capture_mode, 'SUMMARY' AS error_stack_capture_mode, 'content-type,x-cpf-trace-id,x-cpf-transaction-id' AS header_allowlist, 4096 AS max_query_bytes, 8192 AS max_header_bytes, 65536 AS max_request_body_bytes, 65536 AS max_response_body_bytes, 32768 AS max_stack_bytes, 'N' AS request_body_log_yn, 'N' AS response_body_log_yn, 'Y' AS error_stack_log_yn, 'DEFAULT' AS masking_policy_key, '04aec0a6adbf48c269e1538ca571819dc54400391e33d5b497ec05406bccd445' AS policy_checksum, 90 AS retention_days, 100.00 AS sampling_rate, 100 AS priority, 'Y' AS active_yn, '온라인 Controller/API 기본 로그 정책' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.policy_key=src.policy_key)
WHEN MATCHED THEN UPDATE SET tgt.policy_name=src.policy_name, tgt.target_type=src.target_type, tgt.target_id=src.target_id, tgt.log_level=src.log_level, tgt.db_log_enabled_yn=src.db_log_enabled_yn, tgt.file_log_enabled_yn=src.file_log_enabled_yn, tgt.policy_schema_version=src.policy_schema_version, tgt.query_capture_mode=src.query_capture_mode, tgt.request_header_capture_mode=src.request_header_capture_mode, tgt.response_header_capture_mode=src.response_header_capture_mode, tgt.request_body_capture_mode=src.request_body_capture_mode, tgt.response_body_capture_mode=src.response_body_capture_mode, tgt.error_stack_capture_mode=src.error_stack_capture_mode, tgt.header_allowlist=src.header_allowlist, tgt.max_query_bytes=src.max_query_bytes, tgt.max_header_bytes=src.max_header_bytes, tgt.max_request_body_bytes=src.max_request_body_bytes, tgt.max_response_body_bytes=src.max_response_body_bytes, tgt.max_stack_bytes=src.max_stack_bytes, tgt.request_body_log_yn=src.request_body_log_yn, tgt.response_body_log_yn=src.response_body_log_yn, tgt.error_stack_log_yn=src.error_stack_log_yn, tgt.masking_policy_key=src.masking_policy_key, tgt.policy_checksum=src.policy_checksum, tgt.retention_days=src.retention_days, tgt.sampling_rate=src.sampling_rate, tgt.priority=src.priority, tgt.active_yn=src.active_yn, tgt.description=src.description, tgt.updated_by=src.updated_by
WHEN NOT MATCHED THEN INSERT (policy_key, policy_name, target_type, target_id, log_level, db_log_enabled_yn, file_log_enabled_yn, policy_schema_version, query_capture_mode, request_header_capture_mode, response_header_capture_mode, request_body_capture_mode, response_body_capture_mode, error_stack_capture_mode, header_allowlist, max_query_bytes, max_header_bytes, max_request_body_bytes, max_response_body_bytes, max_stack_bytes, request_body_log_yn, response_body_log_yn, error_stack_log_yn, masking_policy_key, policy_checksum, retention_days, sampling_rate, priority, active_yn, description, created_by, updated_by) VALUES (src.policy_key, src.policy_name, src.target_type, src.target_id, src.log_level, src.db_log_enabled_yn, src.file_log_enabled_yn, src.policy_schema_version, src.query_capture_mode, src.request_header_capture_mode, src.response_header_capture_mode, src.request_body_capture_mode, src.response_body_capture_mode, src.error_stack_capture_mode, src.header_allowlist, src.max_query_bytes, src.max_header_bytes, src.max_request_body_bytes, src.max_response_body_bytes, src.max_stack_bytes, src.request_body_log_yn, src.response_body_log_yn, src.error_stack_log_yn, src.masking_policy_key, src.policy_checksum, src.retention_days, src.sampling_rate, src.priority, src.active_yn, src.description, src.created_by, src.updated_by);
MERGE INTO OPS_LOG_POLICY tgt
USING (SELECT 'BATCH_DEFAULT' AS policy_key, '배치 기본 로그 정책' AS policy_name, 'BATCH_JOB' AS target_type, '*' AS target_id, 'INFO' AS log_level, 'Y' AS db_log_enabled_yn, 'Y' AS file_log_enabled_yn, 2 AS policy_schema_version, 'NONE' AS query_capture_mode, 'ALLOWLIST' AS request_header_capture_mode, 'ALLOWLIST' AS response_header_capture_mode, 'NONE' AS request_body_capture_mode, 'NONE' AS response_body_capture_mode, 'SUMMARY' AS error_stack_capture_mode, 'content-type,x-cpf-trace-id,x-cpf-transaction-id' AS header_allowlist, 4096 AS max_query_bytes, 8192 AS max_header_bytes, 65536 AS max_request_body_bytes, 65536 AS max_response_body_bytes, 32768 AS max_stack_bytes, 'N' AS request_body_log_yn, 'N' AS response_body_log_yn, 'Y' AS error_stack_log_yn, 'DEFAULT' AS masking_policy_key, '0eca9ff2359e55290f01c2594d399c32e4af9decd34541a6f571a4345f36ca08' AS policy_checksum, 180 AS retention_days, 100.00 AS sampling_rate, 100 AS priority, 'Y' AS active_yn, 'Spring Batch Job 기본 로그 정책' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.policy_key=src.policy_key)
WHEN MATCHED THEN UPDATE SET tgt.policy_name=src.policy_name, tgt.target_type=src.target_type, tgt.target_id=src.target_id, tgt.log_level=src.log_level, tgt.db_log_enabled_yn=src.db_log_enabled_yn, tgt.file_log_enabled_yn=src.file_log_enabled_yn, tgt.policy_schema_version=src.policy_schema_version, tgt.query_capture_mode=src.query_capture_mode, tgt.request_header_capture_mode=src.request_header_capture_mode, tgt.response_header_capture_mode=src.response_header_capture_mode, tgt.request_body_capture_mode=src.request_body_capture_mode, tgt.response_body_capture_mode=src.response_body_capture_mode, tgt.error_stack_capture_mode=src.error_stack_capture_mode, tgt.header_allowlist=src.header_allowlist, tgt.max_query_bytes=src.max_query_bytes, tgt.max_header_bytes=src.max_header_bytes, tgt.max_request_body_bytes=src.max_request_body_bytes, tgt.max_response_body_bytes=src.max_response_body_bytes, tgt.max_stack_bytes=src.max_stack_bytes, tgt.request_body_log_yn=src.request_body_log_yn, tgt.response_body_log_yn=src.response_body_log_yn, tgt.error_stack_log_yn=src.error_stack_log_yn, tgt.masking_policy_key=src.masking_policy_key, tgt.policy_checksum=src.policy_checksum, tgt.retention_days=src.retention_days, tgt.sampling_rate=src.sampling_rate, tgt.priority=src.priority, tgt.active_yn=src.active_yn, tgt.description=src.description, tgt.updated_by=src.updated_by
WHEN NOT MATCHED THEN INSERT (policy_key, policy_name, target_type, target_id, log_level, db_log_enabled_yn, file_log_enabled_yn, policy_schema_version, query_capture_mode, request_header_capture_mode, response_header_capture_mode, request_body_capture_mode, response_body_capture_mode, error_stack_capture_mode, header_allowlist, max_query_bytes, max_header_bytes, max_request_body_bytes, max_response_body_bytes, max_stack_bytes, request_body_log_yn, response_body_log_yn, error_stack_log_yn, masking_policy_key, policy_checksum, retention_days, sampling_rate, priority, active_yn, description, created_by, updated_by) VALUES (src.policy_key, src.policy_name, src.target_type, src.target_id, src.log_level, src.db_log_enabled_yn, src.file_log_enabled_yn, src.policy_schema_version, src.query_capture_mode, src.request_header_capture_mode, src.response_header_capture_mode, src.request_body_capture_mode, src.response_body_capture_mode, src.error_stack_capture_mode, src.header_allowlist, src.max_query_bytes, src.max_header_bytes, src.max_request_body_bytes, src.max_response_body_bytes, src.max_stack_bytes, src.request_body_log_yn, src.response_body_log_yn, src.error_stack_log_yn, src.masking_policy_key, src.policy_checksum, src.retention_days, src.sampling_rate, src.priority, src.active_yn, src.description, src.created_by, src.updated_by);
MERGE INTO OPS_LOG_POLICY tgt
USING (SELECT 'ADM_OPERATION_DEFAULT' AS policy_key, 'ADM 운영 기본 로그 정책' AS policy_name, 'MODULE' AS target_type, 'ADM' AS target_id, 'INFO' AS log_level, 'Y' AS db_log_enabled_yn, 'Y' AS file_log_enabled_yn, 2 AS policy_schema_version, 'NONE' AS query_capture_mode, 'ALLOWLIST' AS request_header_capture_mode, 'ALLOWLIST' AS response_header_capture_mode, 'NONE' AS request_body_capture_mode, 'NONE' AS response_body_capture_mode, 'SUMMARY' AS error_stack_capture_mode, 'content-type,x-cpf-trace-id,x-cpf-transaction-id' AS header_allowlist, 4096 AS max_query_bytes, 8192 AS max_header_bytes, 65536 AS max_request_body_bytes, 65536 AS max_response_body_bytes, 32768 AS max_stack_bytes, 'N' AS request_body_log_yn, 'N' AS response_body_log_yn, 'Y' AS error_stack_log_yn, 'DEFAULT' AS masking_policy_key, '9ea15a6d3c662bcaf9295a2512cef8fc12da0e77eea6f07b3c5e55e5fb79e705' AS policy_checksum, 365 AS retention_days, 100.00 AS sampling_rate, 50 AS priority, 'Y' AS active_yn, 'ADM 운영 API 기본 로그 정책' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.policy_key=src.policy_key)
WHEN MATCHED THEN UPDATE SET tgt.policy_name=src.policy_name, tgt.target_type=src.target_type, tgt.target_id=src.target_id, tgt.log_level=src.log_level, tgt.db_log_enabled_yn=src.db_log_enabled_yn, tgt.file_log_enabled_yn=src.file_log_enabled_yn, tgt.policy_schema_version=src.policy_schema_version, tgt.query_capture_mode=src.query_capture_mode, tgt.request_header_capture_mode=src.request_header_capture_mode, tgt.response_header_capture_mode=src.response_header_capture_mode, tgt.request_body_capture_mode=src.request_body_capture_mode, tgt.response_body_capture_mode=src.response_body_capture_mode, tgt.error_stack_capture_mode=src.error_stack_capture_mode, tgt.header_allowlist=src.header_allowlist, tgt.max_query_bytes=src.max_query_bytes, tgt.max_header_bytes=src.max_header_bytes, tgt.max_request_body_bytes=src.max_request_body_bytes, tgt.max_response_body_bytes=src.max_response_body_bytes, tgt.max_stack_bytes=src.max_stack_bytes, tgt.request_body_log_yn=src.request_body_log_yn, tgt.response_body_log_yn=src.response_body_log_yn, tgt.error_stack_log_yn=src.error_stack_log_yn, tgt.masking_policy_key=src.masking_policy_key, tgt.policy_checksum=src.policy_checksum, tgt.retention_days=src.retention_days, tgt.sampling_rate=src.sampling_rate, tgt.priority=src.priority, tgt.active_yn=src.active_yn, tgt.description=src.description, tgt.updated_by=src.updated_by
WHEN NOT MATCHED THEN INSERT (policy_key, policy_name, target_type, target_id, log_level, db_log_enabled_yn, file_log_enabled_yn, policy_schema_version, query_capture_mode, request_header_capture_mode, response_header_capture_mode, request_body_capture_mode, response_body_capture_mode, error_stack_capture_mode, header_allowlist, max_query_bytes, max_header_bytes, max_request_body_bytes, max_response_body_bytes, max_stack_bytes, request_body_log_yn, response_body_log_yn, error_stack_log_yn, masking_policy_key, policy_checksum, retention_days, sampling_rate, priority, active_yn, description, created_by, updated_by) VALUES (src.policy_key, src.policy_name, src.target_type, src.target_id, src.log_level, src.db_log_enabled_yn, src.file_log_enabled_yn, src.policy_schema_version, src.query_capture_mode, src.request_header_capture_mode, src.response_header_capture_mode, src.request_body_capture_mode, src.response_body_capture_mode, src.error_stack_capture_mode, src.header_allowlist, src.max_query_bytes, src.max_header_bytes, src.max_request_body_bytes, src.max_response_body_bytes, src.max_stack_bytes, src.request_body_log_yn, src.response_body_log_yn, src.error_stack_log_yn, src.masking_policy_key, src.policy_checksum, src.retention_days, src.sampling_rate, src.priority, src.active_yn, src.description, src.created_by, src.updated_by);
MERGE INTO SEC_JWT_KEY tgt
USING (SELECT 'local-cpf-hs256-001' AS KEY_ID, 'CPF' AS ISSUER, 'HS256' AS ALGORITHM, 'ENV:CPF_CMN_SECURITY_JWT_SECRET' AS SECRET_REF, 'Y' AS ACTIVE_YN, NULL AS EXPIRE_AT, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.KEY_ID=src.KEY_ID)
WHEN MATCHED THEN UPDATE SET tgt.ISSUER=src.ISSUER, tgt.ALGORITHM=src.ALGORITHM, tgt.SECRET_REF=src.SECRET_REF, tgt.ACTIVE_YN=src.ACTIVE_YN, tgt.EXPIRE_AT=src.EXPIRE_AT, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (KEY_ID, ISSUER, ALGORITHM, SECRET_REF, ACTIVE_YN, EXPIRE_AT, created_by, updated_by) VALUES (src.KEY_ID, src.ISSUER, src.ALGORITHM, src.SECRET_REF, src.ACTIVE_YN, src.EXPIRE_AT, src.created_by, src.updated_by);
INSERT INTO CMN_CACHE_REFRESH_EVENT (cache_name, event_type, event_key, source_was_id, published_by, created_by, updated_by)
SELECT 'ALL', 'INITIAL_LOAD', 'INITIAL_FRAMEWORK_SEED', 'SQL', 'SYSTEM', 'SYSTEM', 'SYSTEM'
WHERE NOT EXISTS (
    SELECT 1
    FROM CMN_CACHE_REFRESH_EVENT
    WHERE cache_name = 'ALL'
      AND event_type = 'INITIAL_LOAD'
      AND event_key = 'INITIAL_FRAMEWORK_SEED'
);
MERGE INTO CPF_NOTIFICATION_RULE tgt
USING (SELECT 'BATCH_EXECUTION' AS event_type, 'FAILED' AS event_sub_type, 'ADM' AS channel_code, 'BATCH_FAILED_DEFAULT' AS template_code, 'ERROR' AS severity, 'ADM_BATCH_OPERATOR' AS receiver_group, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.event_type=src.event_type AND tgt.event_sub_type=src.event_sub_type AND tgt.channel_code=src.channel_code)
WHEN MATCHED THEN UPDATE SET tgt.template_code=src.template_code, tgt.severity=src.severity, tgt.receiver_group=src.receiver_group, tgt.use_yn=src.use_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (event_type, event_sub_type, channel_code, template_code, severity, receiver_group, use_yn, created_by, updated_by) VALUES (src.event_type, src.event_sub_type, src.channel_code, src.template_code, src.severity, src.receiver_group, src.use_yn, src.created_by, src.updated_by);
MERGE INTO CPF_NOTIFICATION_RULE tgt
USING (SELECT 'SECURITY_EVENT' AS event_type, 'LOGIN_FAILURE' AS event_sub_type, 'ADM' AS channel_code, 'SECURITY_LOGIN_FAILURE' AS template_code, 'WARN' AS severity, 'ADM_SECURITY_OPERATOR' AS receiver_group, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.event_type=src.event_type AND tgt.event_sub_type=src.event_sub_type AND tgt.channel_code=src.channel_code)
WHEN MATCHED THEN UPDATE SET tgt.template_code=src.template_code, tgt.severity=src.severity, tgt.receiver_group=src.receiver_group, tgt.use_yn=src.use_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (event_type, event_sub_type, channel_code, template_code, severity, receiver_group, use_yn, created_by, updated_by) VALUES (src.event_type, src.event_sub_type, src.channel_code, src.template_code, src.severity, src.receiver_group, src.use_yn, src.created_by, src.updated_by);
INSERT INTO CMN_CODE (parent_id, code_key, code_value, description, created_by, updated_by)
SELECT NULL, 'CODE_GROUP', 'SORT_DIRECTION', '표준 정렬 방향', 'SYSTEM', 'SYSTEM'
WHERE NOT EXISTS (SELECT 1 FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='SORT_DIRECTION');
MERGE INTO CMN_CODE tgt
USING (SELECT (SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='SORT_DIRECTION') x) AS parent_id, 'SORT_DIRECTION' AS code_key, 'ASC' AS code_value, '오름차순' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.code_key=src.code_key AND tgt.code_value=src.code_value)
WHEN MATCHED THEN UPDATE SET tgt.parent_id=src.parent_id, tgt.description=src.description, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (parent_id, code_key, code_value, description, created_by, updated_by) VALUES (src.parent_id, src.code_key, src.code_value, src.description, src.created_by, src.updated_by);
MERGE INTO CMN_CODE tgt
USING (SELECT (SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='SORT_DIRECTION') x) AS parent_id, 'SORT_DIRECTION' AS code_key, 'DESC' AS code_value, '내림차순' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.code_key=src.code_key AND tgt.code_value=src.code_value)
WHEN MATCHED THEN UPDATE SET tgt.parent_id=src.parent_id, tgt.description=src.description, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (parent_id, code_key, code_value, description, created_by, updated_by) VALUES (src.parent_id, src.code_key, src.code_value, src.description, src.created_by, src.updated_by);
MERGE INTO CMN_MESSAGE tgt
USING (SELECT 'MCPF020004' AS message_code, 'ko' AS locale, 'FIXED' AS message_format_type, '요청 사용자 정보가 인증 사용자와 일치하지 않습니다.' AS external_message, 'Body requester spoofing이 차단되었습니다.' AS internal_message, 0 AS parameter_count, NULL AS parameter_sample, 'Requester spoof 차단' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.message_code=src.message_code AND tgt.locale=src.locale)
WHEN MATCHED THEN UPDATE SET tgt.message_format_type=src.message_format_type, tgt.external_message=src.external_message, tgt.internal_message=src.internal_message, tgt.description=src.description, tgt.use_yn='Y', tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (message_code, locale, message_format_type, external_message, internal_message, parameter_count, parameter_sample, description, created_by, updated_by) VALUES (src.message_code, src.locale, src.message_format_type, src.external_message, src.internal_message, src.parameter_count, src.parameter_sample, src.description, src.created_by, src.updated_by);
MERGE INTO CMN_MESSAGE tgt
USING (SELECT 'MCPF020005' AS message_code, 'ko' AS locale, 'FIXED' AS message_format_type, '이미 사용된 정책 버전은 직접 수정할 수 없습니다.' AS external_message, '사용된 Approval Policy version은 immutable입니다.' AS internal_message, 0 AS parameter_count, NULL AS parameter_sample, '정책 버전 불변성' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.message_code=src.message_code AND tgt.locale=src.locale)
WHEN MATCHED THEN UPDATE SET tgt.message_format_type=src.message_format_type, tgt.external_message=src.external_message, tgt.internal_message=src.internal_message, tgt.description=src.description, tgt.use_yn='Y', tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (message_code, locale, message_format_type, external_message, internal_message, parameter_count, parameter_sample, description, created_by, updated_by) VALUES (src.message_code, src.locale, src.message_format_type, src.external_message, src.internal_message, src.parameter_count, src.parameter_sample, src.description, src.created_by, src.updated_by);
MERGE INTO CMN_MESSAGE tgt
USING (SELECT 'MCPF020006' AS message_code, 'ko' AS locale, 'FIXED' AS message_format_type, '동일 작업 식별자가 다른 요청에 사용되었습니다.' AS external_message, 'operationId payload 충돌입니다.' AS internal_message, 0 AS parameter_count, NULL AS parameter_sample, '멱등 작업 충돌' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.message_code=src.message_code AND tgt.locale=src.locale)
WHEN MATCHED THEN UPDATE SET tgt.message_format_type=src.message_format_type, tgt.external_message=src.external_message, tgt.internal_message=src.internal_message, tgt.description=src.description, tgt.use_yn='Y', tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (message_code, locale, message_format_type, external_message, internal_message, parameter_count, parameter_sample, description, created_by, updated_by) VALUES (src.message_code, src.locale, src.message_format_type, src.external_message, src.internal_message, src.parameter_count, src.parameter_sample, src.description, src.created_by, src.updated_by);
MERGE INTO CMN_MESSAGE tgt
USING (SELECT 'MCPF020007' AS message_code, 'ko' AS locale, 'FIXED' AS message_format_type, '현재 데이터가 다른 요청에서 변경되었습니다.' AS external_message, 'expectedVersion CAS가 실패했습니다.' AS internal_message, 0 AS parameter_count, NULL AS parameter_sample, '낙관적 잠금 재조회' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.message_code=src.message_code AND tgt.locale=src.locale)
WHEN MATCHED THEN UPDATE SET tgt.message_format_type=src.message_format_type, tgt.external_message=src.external_message, tgt.internal_message=src.internal_message, tgt.description=src.description, tgt.use_yn='Y', tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (message_code, locale, message_format_type, external_message, internal_message, parameter_count, parameter_sample, description, created_by, updated_by) VALUES (src.message_code, src.locale, src.message_format_type, src.external_message, src.internal_message, src.parameter_count, src.parameter_sample, src.description, src.created_by, src.updated_by);
MERGE INTO CMN_MESSAGE tgt
USING (SELECT 'MCPF040003' AS message_code, 'ko' AS locale, 'FIXED' AS message_format_type, '보존 정책에 의해 해당 데이터는 삭제할 수 없습니다.' AS external_message, 'LEGAL_HOLD가 적용되어 destructive retention을 차단했습니다.' AS internal_message, 0 AS parameter_count, NULL AS parameter_sample, 'Legal hold' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.message_code=src.message_code AND tgt.locale=src.locale)
WHEN MATCHED THEN UPDATE SET tgt.message_format_type=src.message_format_type, tgt.external_message=src.external_message, tgt.internal_message=src.internal_message, tgt.description=src.description, tgt.use_yn='Y', tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (message_code, locale, message_format_type, external_message, internal_message, parameter_count, parameter_sample, description, created_by, updated_by) VALUES (src.message_code, src.locale, src.message_format_type, src.external_message, src.internal_message, src.parameter_count, src.parameter_sample, src.description, src.created_by, src.updated_by);
MERGE INTO CMN_MESSAGE tgt
USING (SELECT 'MCPF040004' AS message_code, 'ko' AS locale, 'FIXED' AS message_format_type, '보존 작업 실행이 비활성화되어 있습니다.' AS external_message, 'CPF.RETENTION.EXECUTE_ENABLED kill switch가 OFF입니다.' AS internal_message, 0 AS parameter_count, NULL AS parameter_sample, 'Retention kill switch' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.message_code=src.message_code AND tgt.locale=src.locale)
WHEN MATCHED THEN UPDATE SET tgt.message_format_type=src.message_format_type, tgt.external_message=src.external_message, tgt.internal_message=src.internal_message, tgt.description=src.description, tgt.use_yn='Y', tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (message_code, locale, message_format_type, external_message, internal_message, parameter_count, parameter_sample, description, created_by, updated_by) VALUES (src.message_code, src.locale, src.message_format_type, src.external_message, src.internal_message, src.parameter_count, src.parameter_sample, src.description, src.created_by, src.updated_by);
MERGE INTO CMN_MESSAGE tgt
USING (SELECT 'MCPF050001' AS message_code, 'ko' AS locale, 'FIXED' AS message_format_type, 'Secret 원문은 조회할 수 없습니다.' AS external_message, 'Secret API는 metadata/reference만 노출합니다.' AS internal_message, 0 AS parameter_count, NULL AS parameter_sample, 'Secret 비노출' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.message_code=src.message_code AND tgt.locale=src.locale)
WHEN MATCHED THEN UPDATE SET tgt.message_format_type=src.message_format_type, tgt.external_message=src.external_message, tgt.internal_message=src.internal_message, tgt.description=src.description, tgt.use_yn='Y', tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (message_code, locale, message_format_type, external_message, internal_message, parameter_count, parameter_sample, description, created_by, updated_by) VALUES (src.message_code, src.locale, src.message_format_type, src.external_message, src.internal_message, src.parameter_count, src.parameter_sample, src.description, src.created_by, src.updated_by);
MERGE INTO CMN_MESSAGE tgt
USING (SELECT 'MCPF050002' AS message_code, 'ko' AS locale, 'FIXED' AS message_format_type, '테넌트 식별정보가 필요합니다.' AS external_message, 'Tenant mode에서 resolver가 tenantId를 결정하지 못했습니다.' AS internal_message, 0 AS parameter_count, NULL AS parameter_sample, 'Tenant 필수' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.message_code=src.message_code AND tgt.locale=src.locale)
WHEN MATCHED THEN UPDATE SET tgt.message_format_type=src.message_format_type, tgt.external_message=src.external_message, tgt.internal_message=src.internal_message, tgt.description=src.description, tgt.use_yn='Y', tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (message_code, locale, message_format_type, external_message, internal_message, parameter_count, parameter_sample, description, created_by, updated_by) VALUES (src.message_code, src.locale, src.message_format_type, src.external_message, src.internal_message, src.parameter_count, src.parameter_sample, src.description, src.created_by, src.updated_by);
MERGE INTO CMN_RESPONSE_CODE tgt
USING (SELECT 'ECPF020004' AS response_code, 'MCPF020004' AS message_code, 'E' AS result_type, 'CPF' AS module_id, '02' AS response_group, '0004' AS sequence_no, 403 AS http_status, 'Requester spoof blocked' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.response_code=src.response_code)
WHEN MATCHED THEN UPDATE SET tgt.message_code=src.message_code, tgt.result_type=src.result_type, tgt.module_id=src.module_id, tgt.response_group=src.response_group, tgt.sequence_no=src.sequence_no, tgt.http_status=src.http_status, tgt.description=src.description, tgt.use_yn='Y', tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (response_code, message_code, result_type, module_id, response_group, sequence_no, http_status, description, created_by, updated_by) VALUES (src.response_code, src.message_code, src.result_type, src.module_id, src.response_group, src.sequence_no, src.http_status, src.description, src.created_by, src.updated_by);
MERGE INTO CMN_RESPONSE_CODE tgt
USING (SELECT 'ECPF020005' AS response_code, 'MCPF020005' AS message_code, 'E' AS result_type, 'CPF' AS module_id, '02' AS response_group, '0005' AS sequence_no, 409 AS http_status, 'Policy version immutable' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.response_code=src.response_code)
WHEN MATCHED THEN UPDATE SET tgt.message_code=src.message_code, tgt.result_type=src.result_type, tgt.module_id=src.module_id, tgt.response_group=src.response_group, tgt.sequence_no=src.sequence_no, tgt.http_status=src.http_status, tgt.description=src.description, tgt.use_yn='Y', tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (response_code, message_code, result_type, module_id, response_group, sequence_no, http_status, description, created_by, updated_by) VALUES (src.response_code, src.message_code, src.result_type, src.module_id, src.response_group, src.sequence_no, src.http_status, src.description, src.created_by, src.updated_by);
MERGE INTO CMN_RESPONSE_CODE tgt
USING (SELECT 'ECPF020006' AS response_code, 'MCPF020006' AS message_code, 'E' AS result_type, 'CPF' AS module_id, '02' AS response_group, '0006' AS sequence_no, 409 AS http_status, 'Operation id conflict' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.response_code=src.response_code)
WHEN MATCHED THEN UPDATE SET tgt.message_code=src.message_code, tgt.result_type=src.result_type, tgt.module_id=src.module_id, tgt.response_group=src.response_group, tgt.sequence_no=src.sequence_no, tgt.http_status=src.http_status, tgt.description=src.description, tgt.use_yn='Y', tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (response_code, message_code, result_type, module_id, response_group, sequence_no, http_status, description, created_by, updated_by) VALUES (src.response_code, src.message_code, src.result_type, src.module_id, src.response_group, src.sequence_no, src.http_status, src.description, src.created_by, src.updated_by);
MERGE INTO CMN_RESPONSE_CODE tgt
USING (SELECT 'ECPF020007' AS response_code, 'MCPF020007' AS message_code, 'E' AS result_type, 'CPF' AS module_id, '02' AS response_group, '0007' AS sequence_no, 409 AS http_status, 'Optimistic lock retry' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.response_code=src.response_code)
WHEN MATCHED THEN UPDATE SET tgt.message_code=src.message_code, tgt.result_type=src.result_type, tgt.module_id=src.module_id, tgt.response_group=src.response_group, tgt.sequence_no=src.sequence_no, tgt.http_status=src.http_status, tgt.description=src.description, tgt.use_yn='Y', tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (response_code, message_code, result_type, module_id, response_group, sequence_no, http_status, description, created_by, updated_by) VALUES (src.response_code, src.message_code, src.result_type, src.module_id, src.response_group, src.sequence_no, src.http_status, src.description, src.created_by, src.updated_by);
MERGE INTO CMN_RESPONSE_CODE tgt
USING (SELECT 'ECPF040003' AS response_code, 'MCPF040003' AS message_code, 'E' AS result_type, 'CPF' AS module_id, '04' AS response_group, '0003' AS sequence_no, 423 AS http_status, 'Legal hold' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.response_code=src.response_code)
WHEN MATCHED THEN UPDATE SET tgt.message_code=src.message_code, tgt.result_type=src.result_type, tgt.module_id=src.module_id, tgt.response_group=src.response_group, tgt.sequence_no=src.sequence_no, tgt.http_status=src.http_status, tgt.description=src.description, tgt.use_yn='Y', tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (response_code, message_code, result_type, module_id, response_group, sequence_no, http_status, description, created_by, updated_by) VALUES (src.response_code, src.message_code, src.result_type, src.module_id, src.response_group, src.sequence_no, src.http_status, src.description, src.created_by, src.updated_by);
MERGE INTO CMN_RESPONSE_CODE tgt
USING (SELECT 'ECPF040004' AS response_code, 'MCPF040004' AS message_code, 'E' AS result_type, 'CPF' AS module_id, '04' AS response_group, '0004' AS sequence_no, 403 AS http_status, 'Retention disabled' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.response_code=src.response_code)
WHEN MATCHED THEN UPDATE SET tgt.message_code=src.message_code, tgt.result_type=src.result_type, tgt.module_id=src.module_id, tgt.response_group=src.response_group, tgt.sequence_no=src.sequence_no, tgt.http_status=src.http_status, tgt.description=src.description, tgt.use_yn='Y', tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (response_code, message_code, result_type, module_id, response_group, sequence_no, http_status, description, created_by, updated_by) VALUES (src.response_code, src.message_code, src.result_type, src.module_id, src.response_group, src.sequence_no, src.http_status, src.description, src.created_by, src.updated_by);
MERGE INTO CMN_RESPONSE_CODE tgt
USING (SELECT 'ECPF050001' AS response_code, 'MCPF050001' AS message_code, 'E' AS result_type, 'CPF' AS module_id, '05' AS response_group, '0001' AS sequence_no, 403 AS http_status, 'Secret value hidden' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.response_code=src.response_code)
WHEN MATCHED THEN UPDATE SET tgt.message_code=src.message_code, tgt.result_type=src.result_type, tgt.module_id=src.module_id, tgt.response_group=src.response_group, tgt.sequence_no=src.sequence_no, tgt.http_status=src.http_status, tgt.description=src.description, tgt.use_yn='Y', tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (response_code, message_code, result_type, module_id, response_group, sequence_no, http_status, description, created_by, updated_by) VALUES (src.response_code, src.message_code, src.result_type, src.module_id, src.response_group, src.sequence_no, src.http_status, src.description, src.created_by, src.updated_by);
MERGE INTO CMN_RESPONSE_CODE tgt
USING (SELECT 'ECPF050002' AS response_code, 'MCPF050002' AS message_code, 'E' AS result_type, 'CPF' AS module_id, '05' AS response_group, '0002' AS sequence_no, 400 AS http_status, 'Tenant required' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.response_code=src.response_code)
WHEN MATCHED THEN UPDATE SET tgt.message_code=src.message_code, tgt.result_type=src.result_type, tgt.module_id=src.module_id, tgt.response_group=src.response_group, tgt.sequence_no=src.sequence_no, tgt.http_status=src.http_status, tgt.description=src.description, tgt.use_yn='Y', tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (response_code, message_code, result_type, module_id, response_group, sequence_no, http_status, description, created_by, updated_by) VALUES (src.response_code, src.message_code, src.result_type, src.module_id, src.response_group, src.sequence_no, src.http_status, src.description, src.created_by, src.updated_by);
MERGE INTO CMN_PARAMETER tgt
USING (SELECT 'CPF.PAGING.DEFAULT_SIZE' AS config_key, '20' AS config_value, 'NUMBER' AS config_type, '공통 Page 기본 크기' AS description, 'N' AS encrypted_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.config_key=src.config_key)
WHEN MATCHED THEN UPDATE SET tgt.config_value=src.config_value, tgt.config_type=src.config_type, tgt.description=src.description, tgt.encrypted_yn=src.encrypted_yn, tgt.use_yn='Y', tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (config_key, config_value, config_type, description, encrypted_yn, created_by, updated_by) VALUES (src.config_key, src.config_value, src.config_type, src.description, src.encrypted_yn, src.created_by, src.updated_by);
MERGE INTO CMN_PARAMETER tgt
USING (SELECT 'CPF.PAGING.MAX_SIZE' AS config_key, '200' AS config_value, 'NUMBER' AS config_type, '공통 Page 최대 크기' AS description, 'N' AS encrypted_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.config_key=src.config_key)
WHEN MATCHED THEN UPDATE SET tgt.config_value=src.config_value, tgt.config_type=src.config_type, tgt.description=src.description, tgt.encrypted_yn=src.encrypted_yn, tgt.use_yn='Y', tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (config_key, config_value, config_type, description, encrypted_yn, created_by, updated_by) VALUES (src.config_key, src.config_value, src.config_type, src.description, src.encrypted_yn, src.created_by, src.updated_by);
MERGE INTO CMN_PARAMETER tgt
USING (SELECT 'CPF.RETENTION.DRY_RUN_DEFAULT' AS config_key, 'Y' AS config_value, 'BOOLEAN' AS config_type, 'Retention 기본 Dry-run' AS description, 'N' AS encrypted_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.config_key=src.config_key)
WHEN MATCHED THEN UPDATE SET tgt.config_value=src.config_value, tgt.config_type=src.config_type, tgt.description=src.description, tgt.encrypted_yn=src.encrypted_yn, tgt.use_yn='Y', tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (config_key, config_value, config_type, description, encrypted_yn, created_by, updated_by) VALUES (src.config_key, src.config_value, src.config_type, src.description, src.encrypted_yn, src.created_by, src.updated_by);
MERGE INTO CMN_PARAMETER tgt
USING (SELECT 'CPF.RETENTION.EXECUTE_ENABLED' AS config_key, 'N' AS config_value, 'BOOLEAN' AS config_type, '실제 Archive/Purge 실행 Kill Switch 기본 OFF' AS description, 'N' AS encrypted_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.config_key=src.config_key)
WHEN MATCHED THEN UPDATE SET tgt.config_value=src.config_value, tgt.config_type=src.config_type, tgt.description=src.description, tgt.encrypted_yn=src.encrypted_yn, tgt.use_yn='Y', tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (config_key, config_value, config_type, description, encrypted_yn, created_by, updated_by) VALUES (src.config_key, src.config_value, src.config_type, src.description, src.encrypted_yn, src.created_by, src.updated_by);
MERGE INTO CMN_PARAMETER tgt
USING (SELECT 'CPF.SECRET.CACHE_TTL_SECONDS' AS config_key, '300' AS config_value, 'NUMBER' AS config_type, 'Secret metadata/cache 기본 TTL' AS description, 'N' AS encrypted_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.config_key=src.config_key)
WHEN MATCHED THEN UPDATE SET tgt.config_value=src.config_value, tgt.config_type=src.config_type, tgt.description=src.description, tgt.encrypted_yn=src.encrypted_yn, tgt.use_yn='Y', tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (config_key, config_value, config_type, description, encrypted_yn, created_by, updated_by) VALUES (src.config_key, src.config_value, src.config_type, src.description, src.encrypted_yn, src.created_by, src.updated_by);
MERGE INTO CMN_PARAMETER tgt
USING (SELECT 'CPF.TENANT.ENABLED' AS config_key, 'N' AS config_value, 'BOOLEAN' AS config_type, 'Tenant context 기능 기본 OFF' AS description, 'N' AS encrypted_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.config_key=src.config_key)
WHEN MATCHED THEN UPDATE SET tgt.config_value=src.config_value, tgt.config_type=src.config_type, tgt.description=src.description, tgt.encrypted_yn=src.encrypted_yn, tgt.use_yn='Y', tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (config_key, config_value, config_type, description, encrypted_yn, created_by, updated_by) VALUES (src.config_key, src.config_value, src.config_type, src.description, src.encrypted_yn, src.created_by, src.updated_by);
MERGE INTO CMN_PARAMETER tgt
USING (SELECT 'CPF.HEALTH.REMOTE_DEPENDENCY_GATES_READINESS' AS config_key, 'N' AS config_value, 'BOOLEAN' AS config_type, 'Remote owner 장애가 local readiness를 직접 차단하지 않음' AS description, 'N' AS encrypted_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.config_key=src.config_key)
WHEN MATCHED THEN UPDATE SET tgt.config_value=src.config_value, tgt.config_type=src.config_type, tgt.description=src.description, tgt.encrypted_yn=src.encrypted_yn, tgt.use_yn='Y', tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (config_key, config_value, config_type, description, encrypted_yn, created_by, updated_by) VALUES (src.config_key, src.config_value, src.config_type, src.description, src.encrypted_yn, src.created_by, src.updated_by);
MERGE INTO CMN_CODE tgt
USING (SELECT (SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='REQUEST_TYPE') x) AS parent_id, 'REQUEST_TYPE' AS code_key, 'O' AS code_value, '온라인 요청' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.code_key=src.code_key AND tgt.code_value=src.code_value)
WHEN MATCHED THEN UPDATE SET tgt.parent_id=src.parent_id, tgt.description=src.description, tgt.use_yn='Y', tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (parent_id, code_key, code_value, description, created_by, updated_by) VALUES (src.parent_id, src.code_key, src.code_value, src.description, src.created_by, src.updated_by);
MERGE INTO CMN_CODE tgt
USING (SELECT (SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='REQUEST_TYPE') x) AS parent_id, 'REQUEST_TYPE' AS code_key, 'S' AS code_value, '공유 내부 서비스 요청' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.code_key=src.code_key AND tgt.code_value=src.code_value)
WHEN MATCHED THEN UPDATE SET tgt.parent_id=src.parent_id, tgt.description=src.description, tgt.use_yn='Y', tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (parent_id, code_key, code_value, description, created_by, updated_by) VALUES (src.parent_id, src.code_key, src.code_value, src.description, src.created_by, src.updated_by);
MERGE INTO CMN_CODE tgt
USING (SELECT (SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='REQUEST_TYPE') x) AS parent_id, 'REQUEST_TYPE' AS code_key, 'B' AS code_value, '배치 요청' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.code_key=src.code_key AND tgt.code_value=src.code_value)
WHEN MATCHED THEN UPDATE SET tgt.parent_id=src.parent_id, tgt.description=src.description, tgt.use_yn='Y', tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (parent_id, code_key, code_value, description, created_by, updated_by) VALUES (src.parent_id, src.code_key, src.code_value, src.description, src.created_by, src.updated_by);
MERGE INTO CMN_CODE tgt
USING (SELECT (SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='CHANNEL_CODE') x) AS parent_id, 'CHANNEL_CODE' AS code_key, 'APP' AS code_value, '모바일 앱 채널' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.code_key=src.code_key AND tgt.code_value=src.code_value)
WHEN MATCHED THEN UPDATE SET tgt.parent_id=src.parent_id, tgt.description=src.description, tgt.use_yn='Y', tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (parent_id, code_key, code_value, description, created_by, updated_by) VALUES (src.parent_id, src.code_key, src.code_value, src.description, src.created_by, src.updated_by);
MERGE INTO CMN_CODE tgt
USING (SELECT (SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='CHANNEL_CODE') x) AS parent_id, 'CHANNEL_CODE' AS code_key, 'JUT' AS code_value, 'JUnit/자동 테스트 채널' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.code_key=src.code_key AND tgt.code_value=src.code_value)
WHEN MATCHED THEN UPDATE SET tgt.parent_id=src.parent_id, tgt.description=src.description, tgt.use_yn='Y', tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (parent_id, code_key, code_value, description, created_by, updated_by) VALUES (src.parent_id, src.code_key, src.code_value, src.description, src.created_by, src.updated_by);
MERGE INTO CMN_CODE tgt
USING (SELECT (SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='RESULT_TYPE') x) AS parent_id, 'RESULT_TYPE' AS code_key, 'W' AS code_value, '경고/부분 성공' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.code_key=src.code_key AND tgt.code_value=src.code_value)
WHEN MATCHED THEN UPDATE SET tgt.parent_id=src.parent_id, tgt.description=src.description, tgt.use_yn='Y', tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (parent_id, code_key, code_value, description, created_by, updated_by) VALUES (src.parent_id, src.code_key, src.code_value, src.description, src.created_by, src.updated_by);
MERGE INTO CMN_CODE tgt
USING (SELECT (SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='MESSAGE_FORMAT_TYPE') x) AS parent_id, 'MESSAGE_FORMAT_TYPE' AS code_key, 'PARAMETER' AS code_value, 'Named parameter 메시지' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.code_key=src.code_key AND tgt.code_value=src.code_value)
WHEN MATCHED THEN UPDATE SET tgt.parent_id=src.parent_id, tgt.description=src.description, tgt.use_yn='Y', tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (parent_id, code_key, code_value, description, created_by, updated_by) VALUES (src.parent_id, src.code_key, src.code_value, src.description, src.created_by, src.updated_by);
MERGE INTO CMN_CODE tgt
USING (SELECT (SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='ASYNC_STATUS') x) AS parent_id, 'ASYNC_STATUS' AS code_key, 'FAILED' AS code_value, '비동기 처리 실패' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.code_key=src.code_key AND tgt.code_value=src.code_value)
WHEN MATCHED THEN UPDATE SET tgt.parent_id=src.parent_id, tgt.description=src.description, tgt.use_yn='Y', tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (parent_id, code_key, code_value, description, created_by, updated_by) VALUES (src.parent_id, src.code_key, src.code_value, src.description, src.created_by, src.updated_by);
MERGE INTO CMN_CODE tgt
USING (SELECT (SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='BATCH_JOB_TYPE') x) AS parent_id, 'BATCH_JOB_TYPE' AS code_key, 'SPRING_BATCH' AS code_value, 'Spring Batch Job' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.code_key=src.code_key AND tgt.code_value=src.code_value)
WHEN MATCHED THEN UPDATE SET tgt.parent_id=src.parent_id, tgt.description=src.description, tgt.use_yn='Y', tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (parent_id, code_key, code_value, description, created_by, updated_by) VALUES (src.parent_id, src.code_key, src.code_value, src.description, src.created_by, src.updated_by);
MERGE INTO CMN_CODE tgt
USING (SELECT (SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='BATCH_JOB_TYPE') x) AS parent_id, 'BATCH_JOB_TYPE' AS code_key, 'WORKER' AS code_value, '지속 Worker' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.code_key=src.code_key AND tgt.code_value=src.code_value)
WHEN MATCHED THEN UPDATE SET tgt.parent_id=src.parent_id, tgt.description=src.description, tgt.use_yn='Y', tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (parent_id, code_key, code_value, description, created_by, updated_by) VALUES (src.parent_id, src.code_key, src.code_value, src.description, src.created_by, src.updated_by);
MERGE INTO CMN_CODE tgt
USING (SELECT (SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='BATCH_JOB_TYPE') x) AS parent_id, 'BATCH_JOB_TYPE' AS code_key, 'SCHEDULER' AS code_value, 'Scheduler Job' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.code_key=src.code_key AND tgt.code_value=src.code_value)
WHEN MATCHED THEN UPDATE SET tgt.parent_id=src.parent_id, tgt.description=src.description, tgt.use_yn='Y', tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (parent_id, code_key, code_value, description, created_by, updated_by) VALUES (src.parent_id, src.code_key, src.code_value, src.description, src.created_by, src.updated_by);
MERGE INTO CMN_CODE tgt
USING (SELECT (SELECT code_id FROM (SELECT code_id FROM CMN_CODE WHERE code_key='CODE_GROUP' AND code_value='BATCH_JOB_TYPE') x) AS parent_id, 'BATCH_JOB_TYPE' AS code_key, 'CENTER_CUT' AS code_value, 'Center-Cut 대량 처리' AS description, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.code_key=src.code_key AND tgt.code_value=src.code_value)
WHEN MATCHED THEN UPDATE SET tgt.parent_id=src.parent_id, tgt.description=src.description, tgt.use_yn='Y', tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (parent_id, code_key, code_value, description, created_by, updated_by) VALUES (src.parent_id, src.code_key, src.code_value, src.description, src.created_by, src.updated_by);
-- ===== END 50_framework_seed_data.sql =====

-- ===== BEGIN 52_standard_execution_alias_seed.sql =====
-- AUTO-GENERATED from cpf-tools/db/canonical/seed-model.json
-- vendor=oracle; source=52_standard_execution_alias_seed.sql
-- DERIVED compatibility input; canonical authority is cpf-tools/db/canonical/**.
-- DO NOT EDIT generated seed directly.

-- CPF_LOGICAL_DATABASE=cpfDB
DELETE FROM CPF_STANDARD_EXECUTION_ALIAS WHERE legacy_execution_id LIKE 'OADM-MBR-%' OR standard_execution_id LIKE 'OADMMB%';
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'BADM-RLG-EX-0001' AS legacy_execution_id, 'BADMRL0001' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'BBAT-CUT-CL-0001' AS legacy_execution_id, 'BBATCU0001' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'BBAT-OPS-FL-0001' AS legacy_execution_id, 'BBATOP0001' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'BBAT-OPS-HB-0001' AS legacy_execution_id, 'BBATOP0002' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'BBAT-OPS-SM-0001' AS legacy_execution_id, 'BBATOP0003' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'BREF-EDU-CH-0001' AS legacy_execution_id, 'BREFAA0001' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'BREF-EDU-RT-0001' AS legacy_execution_id, 'BREFAA0002' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'BREF-EDU-TS-0001' AS legacy_execution_id, 'BREFAA0003' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-BAT-01-0010' AS legacy_execution_id, 'OADMBA0010' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-BAT-01-0012' AS legacy_execution_id, 'OADMBA0012' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-BAT-01-0013' AS legacy_execution_id, 'OADMBA0013' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-BAT-01-0014' AS legacy_execution_id, 'OADMBA0014' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-BAT-01-0015' AS legacy_execution_id, 'OADMBA0015' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-BAT-01-0016' AS legacy_execution_id, 'OADMBA0016' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-BAT-01-0023' AS legacy_execution_id, 'OADMBA0023' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-BAT-01-0024' AS legacy_execution_id, 'OADMBA0024' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-BAT-01-0025' AS legacy_execution_id, 'OADMBA0025' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-BAT-01-0027' AS legacy_execution_id, 'OADMBA0027' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-BAT-01-0028' AS legacy_execution_id, 'OADMBA0028' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-BAT-01-0029' AS legacy_execution_id, 'OADMBA0029' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-BAT-01-0030' AS legacy_execution_id, 'OADMBA0030' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-BAT-01-0032' AS legacy_execution_id, 'OADMBA0032' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-BAT-01-0034' AS legacy_execution_id, 'OADMBA0034' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-BAT-02-0011' AS legacy_execution_id, 'OADMBA0011' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-BAT-02-0017' AS legacy_execution_id, 'OADMBA0017' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-BAT-02-0018' AS legacy_execution_id, 'OADMBA0018' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-BAT-02-0019' AS legacy_execution_id, 'OADMBA0019' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-BAT-02-0026' AS legacy_execution_id, 'OADMBA0026' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-BAT-03-0020' AS legacy_execution_id, 'OADMBA0020' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-BAT-03-0021' AS legacy_execution_id, 'OADMBA0021' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-BAT-03-0022' AS legacy_execution_id, 'OADMBA0022' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-BAT-03-0031' AS legacy_execution_id, 'OADMBA0031' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-BAT-03-0033' AS legacy_execution_id, 'OADMBA0033' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-CDE-01-0010' AS legacy_execution_id, 'OADMCD0010' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-CDE-01-0011' AS legacy_execution_id, 'OADMCD0011' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-CDE-02-0012' AS legacy_execution_id, 'OADMCD0012' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-CDE-03-0013' AS legacy_execution_id, 'OADMCD0013' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-CDE-04-0014' AS legacy_execution_id, 'OADMCD0014' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-CFG-01-0010' AS legacy_execution_id, 'OADMCF0010' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-CFG-01-0011' AS legacy_execution_id, 'OADMCF0011' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-CFG-02-0012' AS legacy_execution_id, 'OADMCF0012' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-CFG-03-0013' AS legacy_execution_id, 'OADMCF0013' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-CFG-04-0014' AS legacy_execution_id, 'OADMCF0014' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-CTC-01-0010' AS legacy_execution_id, 'OADMCT0010' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-CTC-01-0020' AS legacy_execution_id, 'OADMCT0020' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-CTC-01-0030' AS legacy_execution_id, 'OADMCT0030' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-CTC-01-0040' AS legacy_execution_id, 'OADMCT0040' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-CTC-01-0050' AS legacy_execution_id, 'OADMCT0050' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-CTC-01-0060' AS legacy_execution_id, 'OADMCT0060' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-CTC-01-0070' AS legacy_execution_id, 'OADMCT0070' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-DWN-01-0001' AS legacy_execution_id, 'OADMDW0001' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-DWN-01-0002' AS legacy_execution_id, 'OADMDW0002' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-DWN-02-0003' AS legacy_execution_id, 'OADMDW0003' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-EXE-01-0001' AS legacy_execution_id, 'OADMEX0001' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-EXE-01-0002' AS legacy_execution_id, 'OADMEX0002' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-LGP-01-0010' AS legacy_execution_id, 'OADMLG0010' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-LGP-01-0011' AS legacy_execution_id, 'OADMLG0011' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-LGP-01-0018' AS legacy_execution_id, 'OADMLG0018' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-LGP-01-0020' AS legacy_execution_id, 'OADMLG0020' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-LGP-01-0021' AS legacy_execution_id, 'OADMLG0021' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-LGP-03-0012' AS legacy_execution_id, 'OADMLG0012' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-LGP-03-0013' AS legacy_execution_id, 'OADMLG0013' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-LGP-03-0014' AS legacy_execution_id, 'OADMLG0014' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-LGP-03-0016' AS legacy_execution_id, 'OADMLG0016' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-LGP-03-0018' AS legacy_execution_id, 'OADMLG0019' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-LGP-04-0015' AS legacy_execution_id, 'OADMLG0015' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-LGP-04-0017' AS legacy_execution_id, 'OADMLG0017' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-LGP-04-0019' AS legacy_execution_id, 'OADMLG0022' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-MSG-01-0010' AS legacy_execution_id, 'OADMMS0010' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-MSG-01-0011' AS legacy_execution_id, 'OADMMS0011' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-MSG-02-0012' AS legacy_execution_id, 'OADMMS0012' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-MSG-03-0013' AS legacy_execution_id, 'OADMMS0013' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-MSG-04-0014' AS legacy_execution_id, 'OADMMS0014' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-NTF-01-0010' AS legacy_execution_id, 'OADMNT0010' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-NTF-01-0011' AS legacy_execution_id, 'OADMNT0011' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-NTF-01-0014' AS legacy_execution_id, 'OADMNT0014' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-NTF-02-0012' AS legacy_execution_id, 'OADMNT0012' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-NTF-02-0016' AS legacy_execution_id, 'OADMNT0016' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-NTF-03-0013' AS legacy_execution_id, 'OADMNT0013' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-NTF-03-0015' AS legacy_execution_id, 'OADMNT0015' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-OBS-01-0010' AS legacy_execution_id, 'OADMOB0010' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-OBS-01-0011' AS legacy_execution_id, 'OADMOB0011' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-OBS-01-0012' AS legacy_execution_id, 'OADMOB0012' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-OPR-01-0001' AS legacy_execution_id, 'OADMOP0001' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-OPR-01-0002' AS legacy_execution_id, 'OADMOP0002' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-OPR-01-0010' AS legacy_execution_id, 'OADMOP0010' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-OPR-01-0020' AS legacy_execution_id, 'OADMOP0020' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-OPR-01-0030' AS legacy_execution_id, 'OADMOP0030' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-OPR-01-0034' AS legacy_execution_id, 'OADMOP0034' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-OPR-01-0035' AS legacy_execution_id, 'OADMOP0035' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-OPR-01-0036' AS legacy_execution_id, 'OADMOP0036' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-OPR-01-0040' AS legacy_execution_id, 'OADMOP0040' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-OPR-01-0041' AS legacy_execution_id, 'OADMOP0041' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-OPR-01-0042' AS legacy_execution_id, 'OADMOP0042' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-OPR-01-0043' AS legacy_execution_id, 'OADMOP0043' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-OPR-01-0050' AS legacy_execution_id, 'OADMOP0050' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-OPR-02-0031' AS legacy_execution_id, 'OADMOP0031' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-OPR-02-0042' AS legacy_execution_id, 'OADMOP0044' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-OPR-03-0032' AS legacy_execution_id, 'OADMOP0032' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-OPR-03-0037' AS legacy_execution_id, 'OADMOP0037' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-OPR-03-0038' AS legacy_execution_id, 'OADMOP0038' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-OPR-03-0039' AS legacy_execution_id, 'OADMOP0039' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-OPR-03-0043' AS legacy_execution_id, 'OADMOP0045' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-OPR-03-0044' AS legacy_execution_id, 'OADMOP0046' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-OPR-03-0045' AS legacy_execution_id, 'OADMOP0047' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-OPR-04-0022' AS legacy_execution_id, 'OADMOP0022' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-OPR-04-0044' AS legacy_execution_id, 'OADMOP0048' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-OPR-05-0011' AS legacy_execution_id, 'OADMOP0011' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-OPR-05-0021' AS legacy_execution_id, 'OADMOP0021' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-OPR-06-0033' AS legacy_execution_id, 'OADMOP0033' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-OPR-06-0040' AS legacy_execution_id, 'OADMOP0049' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-OPR-06-0042' AS legacy_execution_id, 'OADMOP0051' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-PER-01-0010' AS legacy_execution_id, 'OADMPE0010' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-PER-01-0011' AS legacy_execution_id, 'OADMPE0011' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-PER-01-0014' AS legacy_execution_id, 'OADMPE0014' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-PER-01-0015' AS legacy_execution_id, 'OADMPE0015' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-PER-01-0019' AS legacy_execution_id, 'OADMPE0019' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-PER-01-0020' AS legacy_execution_id, 'OADMPE0020' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-PER-01-0024' AS legacy_execution_id, 'OADMPE0024' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-PER-01-0025' AS legacy_execution_id, 'OADMPE0025' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-PER-01-0029' AS legacy_execution_id, 'OADMPE0029' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-PER-01-0030' AS legacy_execution_id, 'OADMPE0030' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-PER-01-0034' AS legacy_execution_id, 'OADMPE0034' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-PER-02-0016' AS legacy_execution_id, 'OADMPE0016' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-PER-02-0021' AS legacy_execution_id, 'OADMPE0021' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-PER-02-0026' AS legacy_execution_id, 'OADMPE0026' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-PER-02-0031' AS legacy_execution_id, 'OADMPE0031' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-PER-03-0012' AS legacy_execution_id, 'OADMPE0012' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-PER-03-0013' AS legacy_execution_id, 'OADMPE0013' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-PER-03-0017' AS legacy_execution_id, 'OADMPE0017' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-PER-03-0018' AS legacy_execution_id, 'OADMPE0018' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-PER-03-0022' AS legacy_execution_id, 'OADMPE0022' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-PER-03-0023' AS legacy_execution_id, 'OADMPE0023' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-PER-03-0027' AS legacy_execution_id, 'OADMPE0027' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-PER-03-0028' AS legacy_execution_id, 'OADMPE0028' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-PER-03-0032' AS legacy_execution_id, 'OADMPE0032' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-PER-03-0033' AS legacy_execution_id, 'OADMPE0033' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-PER-03-0035' AS legacy_execution_id, 'OADMPE0035' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-REL-01-0001' AS legacy_execution_id, 'OADMRE0001' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-REL-01-0002' AS legacy_execution_id, 'OADMRE0002' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-REL-01-0003' AS legacy_execution_id, 'OADMRE0003' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-REL-01-0004' AS legacy_execution_id, 'OADMRE0004' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-REL-01-0006' AS legacy_execution_id, 'OADMRE0006' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-REL-01-0007' AS legacy_execution_id, 'OADMRE0007' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-REL-01-0009' AS legacy_execution_id, 'OADMRE0009' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-REL-01-0010' AS legacy_execution_id, 'OADMRE0010' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-REL-01-0011' AS legacy_execution_id, 'OADMRE0011' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-REL-05-0005' AS legacy_execution_id, 'OADMRE0005' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-REL-05-0008' AS legacy_execution_id, 'OADMRE0008' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-REL-05-0012' AS legacy_execution_id, 'OADMRE0012' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-REL-05-0013' AS legacy_execution_id, 'OADMRE0013' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-RLG-CR-0001' AS legacy_execution_id, 'OADMRL0001' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-RLG-DL-0001' AS legacy_execution_id, 'OADMRL0002' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-RLG-DL-0002' AS legacy_execution_id, 'OADMRL0003' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-RLG-DW-0001' AS legacy_execution_id, 'OADMRL0004' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-RLG-IS-0001' AS legacy_execution_id, 'OADMRL0005' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-RLG-QY-0001' AS legacy_execution_id, 'OADMRL0006' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-RLG-QY-0002' AS legacy_execution_id, 'OADMRL0007' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-RLG-QY-0003' AS legacy_execution_id, 'OADMRL0008' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-RLG-QY-0004' AS legacy_execution_id, 'OADMRL0009' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-SEC-01-0010' AS legacy_execution_id, 'OADMSE0010' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-SEC-01-0012' AS legacy_execution_id, 'OADMSE0012' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-SEC-03-0011' AS legacy_execution_id, 'OADMSE0011' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-SEC-03-0013' AS legacy_execution_id, 'OADMSE0013' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-SEC-03-0014' AS legacy_execution_id, 'OADMSE0014' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-SEC-03-0015' AS legacy_execution_id, 'OADMSE0015' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-SVC-01-0010' AS legacy_execution_id, 'OADMSV0010' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-SVC-01-0020' AS legacy_execution_id, 'OADMSV0020' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-SVC-01-0030' AS legacy_execution_id, 'OADMSV0030' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-SVC-01-0040' AS legacy_execution_id, 'OADMSV0040' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-SVC-01-0050' AS legacy_execution_id, 'OADMSV0050' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-SVC-01-0060' AS legacy_execution_id, 'OADMSV0060' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-SVC-01-0070' AS legacy_execution_id, 'OADMSV0070' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-TRG-01-0001' AS legacy_execution_id, 'OADMTR0001' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-TRG-01-0002' AS legacy_execution_id, 'OADMTR0002' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-TRG-01-0003' AS legacy_execution_id, 'OADMTR0003' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-TRG-01-0004' AS legacy_execution_id, 'OADMTR0004' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-TRG-01-0005' AS legacy_execution_id, 'OADMTR0005' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-TRG-01-0006' AS legacy_execution_id, 'OADMTR0006' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-TRN-01-0010' AS legacy_execution_id, 'OADMTR0010' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-TRN-01-0011' AS legacy_execution_id, 'OADMTR0011' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-TRN-04-0013' AS legacy_execution_id, 'OADMTR0013' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OADM-TRN-05-0012' AS legacy_execution_id, 'OADMTR0012' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OBAT-OPR-01-0003' AS legacy_execution_id, 'OBATOP0003' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OBAT-OPR-02-0002' AS legacy_execution_id, 'OBATOP0002' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OMBW-ADM-01-1001' AS legacy_execution_id, 'OMBWAD1001' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OMBW-ADM-03-1002' AS legacy_execution_id, 'OMBWAD1002' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OMBW-APR-01-0001' AS legacy_execution_id, 'OMBWAP0001' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OMBW-APR-01-0003' AS legacy_execution_id, 'OMBWAP0003' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OMBW-APR-02-0002' AS legacy_execution_id, 'OMBWAP0002' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OMBW-APR-05-0004' AS legacy_execution_id, 'OMBWAP0004' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OMBW-ATC-01-0001' AS legacy_execution_id, 'OMBWAT0001' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OMBW-ATC-02-0002' AS legacy_execution_id, 'OMBWAT0002' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OMBW-ATC-DL-0003' AS legacy_execution_id, 'OMBWAT0003' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OMBW-AUD-01-0001' AS legacy_execution_id, 'OMBWUD0001' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OMBW-AUT-01-0004' AS legacy_execution_id, 'OMBWAU0004' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OMBW-AUT-01-0005' AS legacy_execution_id, 'OMBWAU0005' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OMBW-AUT-01-0007' AS legacy_execution_id, 'OMBWAU0007' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OMBW-AUT-02-0001' AS legacy_execution_id, 'OMBWAU0001' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OMBW-AUT-02-0002' AS legacy_execution_id, 'OMBWAU0002' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OMBW-AUT-02-0003' AS legacy_execution_id, 'OMBWAU0003' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OMBW-AUT-03-0006' AS legacy_execution_id, 'OMBWAU0006' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OMBW-AUT-04-0008' AS legacy_execution_id, 'OMBWAU0008' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OMBW-CUS-01-1001' AS legacy_execution_id, 'OMBWCU1001' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OMBW-DSH-01-0001' AS legacy_execution_id, 'OMBWDS0001' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OMBW-DWN-01-0002' AS legacy_execution_id, 'OMBWDW0002' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OMBW-DWN-01-1001' AS legacy_execution_id, 'OMBWDW1001' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OMBW-EMP-01-0001' AS legacy_execution_id, 'OMBWEM0001' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OMBW-EMP-03-0002' AS legacy_execution_id, 'OMBWEM0002' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OMBW-MNU-01-1001' AS legacy_execution_id, 'OMBWMN1001' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OMBW-MNU-03-1002' AS legacy_execution_id, 'OMBWMN1002' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OMBW-MSK-02-1001' AS legacy_execution_id, 'OMBWMS1001' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OMBW-NTF-01-0001' AS legacy_execution_id, 'OMBWNT0001' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OMBW-NTF-02-0002' AS legacy_execution_id, 'OMBWNT0002' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OMBW-NTF-03-0003' AS legacy_execution_id, 'OMBWNT0003' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OMBW-ORD-01-1001' AS legacy_execution_id, 'OMBWOR1001' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OMBW-ORG-01-0001' AS legacy_execution_id, 'OMBWOR0001' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OMBW-ORG-03-0002' AS legacy_execution_id, 'OMBWOR0002' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OMBW-PER-01-0002' AS legacy_execution_id, 'OMBWPE0002' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OMBW-PER-01-0003' AS legacy_execution_id, 'OMBWPE0003' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OMBW-PER-01-1001' AS legacy_execution_id, 'OMBWPE1001' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OMBW-PER-02-0004' AS legacy_execution_id, 'OMBWPE0004' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OMBW-PER-03-1002' AS legacy_execution_id, 'OMBWPE1002' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OMBW-PRD-01-1001' AS legacy_execution_id, 'OMBWPR1001' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OMBW-ROL-01-1001' AS legacy_execution_id, 'OMBWRO1001' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OMBW-ROL-03-1002' AS legacy_execution_id, 'OMBWRO1002' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OMBW-SCH-01-0001' AS legacy_execution_id, 'OMBWSC0001' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OMBW-SCH-03-0002' AS legacy_execution_id, 'OMBWSC0002' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OMBW-SCH-04-0003' AS legacy_execution_id, 'OMBWSC0003' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OMBW-SET-01-1001' AS legacy_execution_id, 'OMBWSE1001' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OMBW-USR-QY-0000' AS legacy_execution_id, 'OMBWUS0001' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OMBW-USR-QY-0001' AS legacy_execution_id, 'OMBWUS0002' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OREF-EDU-01-0001' AS legacy_execution_id, 'OEDUAA0001' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OREF-EDU-01-0002' AS legacy_execution_id, 'OREFAA0002' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OREF-EDU-01-0003' AS legacy_execution_id, 'OREFAA0003' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OREF-EDU-01-0099' AS legacy_execution_id, 'OREFAA0099' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OREF-EDU-02-0001' AS legacy_execution_id, 'OREFAA0004' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OREF-EDU-02-0010' AS legacy_execution_id, 'OREFAA0010' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OREF-EDU-02-0020' AS legacy_execution_id, 'OREFAA0020' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OREF-EDU-02-0030' AS legacy_execution_id, 'OREFAA0030' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OREF-EDU-03-0001' AS legacy_execution_id, 'OREFAA0005' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OREF-EDU-03-0002' AS legacy_execution_id, 'OREFAA0006' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OREF-EDU-03-0003' AS legacy_execution_id, 'OREFAA0007' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OREF-EDU-04-0001' AS legacy_execution_id, 'OREFAA0008' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OREF-EDU-04-0002' AS legacy_execution_id, 'OREFAA0009' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OREF-EDU-05-0001' AS legacy_execution_id, 'OREFAA0011' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OREF-EDU-05-0002' AS legacy_execution_id, 'OREFAA0012' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OREF-EDU-05-9001' AS legacy_execution_id, 'OREFAA9001' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OREF-EDU-08-0001' AS legacy_execution_id, 'OREFAA0013' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OREF-EDU-08-0010' AS legacy_execution_id, 'OREFAA0014' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OREF-EDU-08-9001' AS legacy_execution_id, 'OREFAA9002' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OREF-EDU-09-0001' AS legacy_execution_id, 'OREFAA0015' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OREF-EDU-09-0002' AS legacy_execution_id, 'OREFAA0016' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OREF-EDU-09-0003' AS legacy_execution_id, 'OREFAA0017' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OREF-EDU-09-0004' AS legacy_execution_id, 'OREFAA0018' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OREF-EDU-09-0005' AS legacy_execution_id, 'OREFAA0019' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OREF-EDU-09-0006' AS legacy_execution_id, 'OREFAA0021' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OREF-EDU-09-0007' AS legacy_execution_id, 'OREFAA0022' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OREF-EDU-09-0008' AS legacy_execution_id, 'OREFAA0023' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OREF-EDU-09-0009' AS legacy_execution_id, 'OREFAA0024' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OREF-EDU-09-0010' AS legacy_execution_id, 'OREFAA0025' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OREF-EDU-09-0011' AS legacy_execution_id, 'OREFAA0026' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OREF-EDU-09-0012' AS legacy_execution_id, 'OREFAA0027' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OREF-EDU-09-0013' AS legacy_execution_id, 'OREFAA0028' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OREF-EDU-09-0015' AS legacy_execution_id, 'OREFAA0029' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OREF-EDU-09-0016' AS legacy_execution_id, 'OREFAA0031' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OREF-EDU-09-0017' AS legacy_execution_id, 'OREFAA0032' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OREF-EDU-09-0018' AS legacy_execution_id, 'OREFAA0033' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OREF-EDU-09-0019' AS legacy_execution_id, 'OREFAA0034' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OREF-EDU-09-0020' AS legacy_execution_id, 'OREFAA0035' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OREF-EDU-09-0030' AS legacy_execution_id, 'OREFAA0036' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OREF-EDU-09-0031' AS legacy_execution_id, 'OREFAA0037' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OREF-EDU-09-0032' AS legacy_execution_id, 'OREFAA0038' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OREF-EDU-09-0033' AS legacy_execution_id, 'OREFAA0039' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OREF-EDU-09-0034' AS legacy_execution_id, 'OREFAA0040' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OREF-EDU-09-0035' AS legacy_execution_id, 'OREFAA0041' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OREF-EDU-09-0036' AS legacy_execution_id, 'OREFAA0042' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OREF-EDU-09-0040' AS legacy_execution_id, 'OREFAA0043' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OREF-EDU-09-0051' AS legacy_execution_id, 'OREFAA0051' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OREF-EDU-09-0060' AS legacy_execution_id, 'OREFAA0060' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OREF-EDU-09-0072' AS legacy_execution_id, 'OREFAA0072' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OREF-EDU-09-0073' AS legacy_execution_id, 'OREFAA0073' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OREF-EDU-09-0080' AS legacy_execution_id, 'OREFAA0080' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OREF-EDU-12-0001' AS legacy_execution_id, 'OREFAA0044' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OREF-EDU-12-0002' AS legacy_execution_id, 'OREFAA0045' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OREF-EDU-12-0003' AS legacy_execution_id, 'OREFAA0046' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OREF-EDU-13-0001' AS legacy_execution_id, 'OREFAA0047' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OREF-EDU-13-0002' AS legacy_execution_id, 'OREFAA0048' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OREF-EDU-13-0003' AS legacy_execution_id, 'OREFAA0049' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OREF-EDU-13-0004' AS legacy_execution_id, 'OREFAA0050' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OREF-EDU-13-0005' AS legacy_execution_id, 'OREFAA0052' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OREF-EDU-13-0006' AS legacy_execution_id, 'OREFAA0053' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OREF-EDU-13-0007' AS legacy_execution_id, 'OREFAA0054' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OREF-EDU-13-0008' AS legacy_execution_id, 'OREFAA0055' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OREF-EDU-14-0001' AS legacy_execution_id, 'OREFAA0056' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OREF-EDU-15-0001' AS legacy_execution_id, 'OREFAA0057' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OREF-EDU-16-0001' AS legacy_execution_id, 'OREFAA0058' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OREF-EDU-16-0002' AS legacy_execution_id, 'OREFAA0059' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OREF-EDU-16-0003' AS legacy_execution_id, 'OREFAA0061' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OREF-EDU-16-0004' AS legacy_execution_id, 'OREFAA0062' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OREF-EDU-16-0005' AS legacy_execution_id, 'OREFAA0063' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OREF-EDU-16-0006' AS legacy_execution_id, 'OREFAA0064' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OREF-EDU-17-0001' AS legacy_execution_id, 'OREFAA0065' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OREF-EDU-17-0002' AS legacy_execution_id, 'OREFAA0066' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OREF-QRY-01-0001' AS legacy_execution_id, 'OREFQR0001' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OREF-QRY-01-0002' AS legacy_execution_id, 'OREFQR0002' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OREF-QRY-01-0003' AS legacy_execution_id, 'OREFQR0003' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OREF-QRY-01-0004' AS legacy_execution_id, 'OREFQR0004' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
MERGE INTO CPF_STANDARD_EXECUTION_ALIAS tgt
USING (SELECT 'OREF-QRY-01-0005' AS legacy_execution_id, 'OREFQR0005' AS standard_execution_id, 'CPF O/S/B 10자리 표준 전환' AS migration_reason, 'CPF_SEED' AS created_by, 'CPF_SEED' AS updated_by FROM dual) src
ON (tgt.legacy_execution_id=src.legacy_execution_id)
WHEN MATCHED THEN UPDATE SET tgt.standard_execution_id=src.standard_execution_id, tgt.migration_reason=src.migration_reason, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (legacy_execution_id, standard_execution_id, migration_reason, created_by, updated_by) VALUES (src.legacy_execution_id, src.standard_execution_id, src.migration_reason, src.created_by, src.updated_by);
-- ===== END 52_standard_execution_alias_seed.sql =====

-- ===== BEGIN 53_runtime_service_registry_seed.sql =====
-- AUTO-GENERATED from cpf-tools/db/canonical/seed-model.json
-- vendor=oracle; source=53_runtime_service_registry_seed.sql
-- DERIVED compatibility input; canonical authority is cpf-tools/db/canonical/**.
-- DO NOT EDIT generated seed directly.

-- CPF_LOGICAL_DATABASE=cpfDB
MERGE INTO OPS_SERVICE tgt
USING (SELECT 'MBW' AS service_id, '업무 백오피스 서비스' AS service_name, 'INTERNAL' AS service_type, 'MBW' AS owner_module_code, 'CPF 업무 운영 백오피스 서비스 호출 대상' AS description, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.service_id=src.service_id)
WHEN MATCHED THEN UPDATE SET tgt.service_name=src.service_name, tgt.service_type=src.service_type, tgt.owner_module_code=src.owner_module_code, tgt.description=src.description, tgt.use_yn=src.use_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (service_id, service_name, service_type, owner_module_code, description, use_yn, created_by, updated_by) VALUES (src.service_id, src.service_name, src.service_type, src.owner_module_code, src.description, src.use_yn, src.created_by, src.updated_by);
MERGE INTO OPS_SERVICE tgt
USING (SELECT 'EDU' AS service_id, '온라인 교육 서비스' AS service_name, 'INTERNAL' AS service_type, 'EDU' AS owner_module_code, 'CPF 온라인 교육 및 검증 서비스 호출 대상' AS description, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.service_id=src.service_id)
WHEN MATCHED THEN UPDATE SET tgt.service_name=src.service_name, tgt.service_type=src.service_type, tgt.owner_module_code=src.owner_module_code, tgt.description=src.description, tgt.use_yn=src.use_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (service_id, service_name, service_type, owner_module_code, description, use_yn, created_by, updated_by) VALUES (src.service_id, src.service_name, src.service_type, src.owner_module_code, src.description, src.use_yn, src.created_by, src.updated_by);
MERGE INTO OPS_SERVICE tgt
USING (SELECT 'BAT' AS service_id, '배치 Worker 서비스' AS service_name, 'INTERNAL' AS service_type, 'BAT' AS owner_module_code, 'CPF 배치 Worker 서비스 호출 대상' AS description, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.service_id=src.service_id)
WHEN MATCHED THEN UPDATE SET tgt.service_name=src.service_name, tgt.service_type=src.service_type, tgt.owner_module_code=src.owner_module_code, tgt.description=src.description, tgt.use_yn=src.use_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (service_id, service_name, service_type, owner_module_code, description, use_yn, created_by, updated_by) VALUES (src.service_id, src.service_name, src.service_type, src.owner_module_code, src.description, src.use_yn, src.created_by, src.updated_by);
MERGE INTO OPS_SERVICE tgt
USING (SELECT 'ADM' AS service_id, '운영 콘솔 서비스' AS service_name, 'INTERNAL' AS service_type, 'ADM' AS owner_module_code, 'CPF 운영 콘솔 서비스 호출 대상' AS description, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.service_id=src.service_id)
WHEN MATCHED THEN UPDATE SET tgt.service_name=src.service_name, tgt.service_type=src.service_type, tgt.owner_module_code=src.owner_module_code, tgt.description=src.description, tgt.use_yn=src.use_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (service_id, service_name, service_type, owner_module_code, description, use_yn, created_by, updated_by) VALUES (src.service_id, src.service_name, src.service_type, src.owner_module_code, src.description, src.use_yn, src.created_by, src.updated_by);
MERGE INTO OPS_SERVICE tgt
USING (SELECT 'CEC' AS service_id, '센터컷 실행 서비스' AS service_name, 'INTERNAL' AS service_type, 'CEC' AS owner_module_code, 'CPF 센터컷 Runner 서비스 호출 대상' AS description, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.service_id=src.service_id)
WHEN MATCHED THEN UPDATE SET tgt.service_name=src.service_name, tgt.service_type=src.service_type, tgt.owner_module_code=src.owner_module_code, tgt.description=src.description, tgt.use_yn=src.use_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (service_id, service_name, service_type, owner_module_code, description, use_yn, created_by, updated_by) VALUES (src.service_id, src.service_name, src.service_type, src.owner_module_code, src.description, src.use_yn, src.created_by, src.updated_by);
MERGE INTO OPS_SERVICE_ENDPOINT tgt
USING (SELECT 'MBW_API' AS endpoint_code, 'MBW' AS service_id, 'MBW API Endpoint' AS endpoint_name, 'HTTP' AS endpoint_type, 'http://cpf-backoffice' AS base_url, '/api/v1/backoffice' AS context_path, 3000 AS default_timeout_ms, 0 AS default_retry_count, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.endpoint_code=src.endpoint_code)
WHEN MATCHED THEN UPDATE SET tgt.service_id=src.service_id, tgt.endpoint_name=src.endpoint_name, tgt.endpoint_type=src.endpoint_type, tgt.base_url=src.base_url, tgt.context_path=src.context_path, tgt.default_timeout_ms=src.default_timeout_ms, tgt.default_retry_count=src.default_retry_count, tgt.use_yn=src.use_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (endpoint_code, service_id, endpoint_name, endpoint_type, base_url, context_path, default_timeout_ms, default_retry_count, use_yn, created_by, updated_by) VALUES (src.endpoint_code, src.service_id, src.endpoint_name, src.endpoint_type, src.base_url, src.context_path, src.default_timeout_ms, src.default_retry_count, src.use_yn, src.created_by, src.updated_by);
MERGE INTO OPS_SERVICE_ENDPOINT tgt
USING (SELECT 'EDU_API' AS endpoint_code, 'EDU' AS service_id, 'EDU API Endpoint' AS endpoint_name, 'HTTP' AS endpoint_type, 'http://cpf-education' AS base_url, '/education' AS context_path, 3000 AS default_timeout_ms, 0 AS default_retry_count, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.endpoint_code=src.endpoint_code)
WHEN MATCHED THEN UPDATE SET tgt.service_id=src.service_id, tgt.endpoint_name=src.endpoint_name, tgt.endpoint_type=src.endpoint_type, tgt.base_url=src.base_url, tgt.context_path=src.context_path, tgt.default_timeout_ms=src.default_timeout_ms, tgt.default_retry_count=src.default_retry_count, tgt.use_yn=src.use_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (endpoint_code, service_id, endpoint_name, endpoint_type, base_url, context_path, default_timeout_ms, default_retry_count, use_yn, created_by, updated_by) VALUES (src.endpoint_code, src.service_id, src.endpoint_name, src.endpoint_type, src.base_url, src.context_path, src.default_timeout_ms, src.default_retry_count, src.use_yn, src.created_by, src.updated_by);
MERGE INTO OPS_SERVICE_ENDPOINT tgt
USING (SELECT 'BAT_API' AS endpoint_code, 'BAT' AS service_id, 'BAT API Endpoint' AS endpoint_name, 'HTTP' AS endpoint_type, 'http://cpf-batch' AS base_url, '/bat' AS context_path, 5000 AS default_timeout_ms, 0 AS default_retry_count, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.endpoint_code=src.endpoint_code)
WHEN MATCHED THEN UPDATE SET tgt.service_id=src.service_id, tgt.endpoint_name=src.endpoint_name, tgt.endpoint_type=src.endpoint_type, tgt.base_url=src.base_url, tgt.context_path=src.context_path, tgt.default_timeout_ms=src.default_timeout_ms, tgt.default_retry_count=src.default_retry_count, tgt.use_yn=src.use_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (endpoint_code, service_id, endpoint_name, endpoint_type, base_url, context_path, default_timeout_ms, default_retry_count, use_yn, created_by, updated_by) VALUES (src.endpoint_code, src.service_id, src.endpoint_name, src.endpoint_type, src.base_url, src.context_path, src.default_timeout_ms, src.default_retry_count, src.use_yn, src.created_by, src.updated_by);
MERGE INTO OPS_SERVICE_ENDPOINT tgt
USING (SELECT 'ADM_API' AS endpoint_code, 'ADM' AS service_id, 'ADM API Endpoint' AS endpoint_name, 'HTTP' AS endpoint_type, 'http://cpf-admin' AS base_url, '/adm' AS context_path, 3000 AS default_timeout_ms, 0 AS default_retry_count, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.endpoint_code=src.endpoint_code)
WHEN MATCHED THEN UPDATE SET tgt.service_id=src.service_id, tgt.endpoint_name=src.endpoint_name, tgt.endpoint_type=src.endpoint_type, tgt.base_url=src.base_url, tgt.context_path=src.context_path, tgt.default_timeout_ms=src.default_timeout_ms, tgt.default_retry_count=src.default_retry_count, tgt.use_yn=src.use_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (endpoint_code, service_id, endpoint_name, endpoint_type, base_url, context_path, default_timeout_ms, default_retry_count, use_yn, created_by, updated_by) VALUES (src.endpoint_code, src.service_id, src.endpoint_name, src.endpoint_type, src.base_url, src.context_path, src.default_timeout_ms, src.default_retry_count, src.use_yn, src.created_by, src.updated_by);
MERGE INTO OPS_SERVICE_ENDPOINT tgt
USING (SELECT 'CEC_API' AS endpoint_code, 'CEC' AS service_id, 'CEC API Endpoint' AS endpoint_name, 'HTTP' AS endpoint_type, 'http://cpf-batch-center-cut' AS base_url, '/cec' AS context_path, 5000 AS default_timeout_ms, 0 AS default_retry_count, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.endpoint_code=src.endpoint_code)
WHEN MATCHED THEN UPDATE SET tgt.service_id=src.service_id, tgt.endpoint_name=src.endpoint_name, tgt.endpoint_type=src.endpoint_type, tgt.base_url=src.base_url, tgt.context_path=src.context_path, tgt.default_timeout_ms=src.default_timeout_ms, tgt.default_retry_count=src.default_retry_count, tgt.use_yn=src.use_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (endpoint_code, service_id, endpoint_name, endpoint_type, base_url, context_path, default_timeout_ms, default_retry_count, use_yn, created_by, updated_by) VALUES (src.endpoint_code, src.service_id, src.endpoint_name, src.endpoint_type, src.base_url, src.context_path, src.default_timeout_ms, src.default_retry_count, src.use_yn, src.created_by, src.updated_by);
MERGE INTO OPS_SERVICE_ROUTING_POLICY tgt
USING (SELECT 'MBW' AS service_id, 'MBW_API' AS endpoint_code, 'PRIMARY' AS routing_mode, 'WEIGHT' AS load_balance_type, 'Y' AS failover_enabled_yn, 'Y' AS health_check_required_yn, 'Y' AS active_yn, 100 AS priority, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.service_id=src.service_id AND tgt.endpoint_code=src.endpoint_code AND tgt.priority=src.priority)
WHEN MATCHED THEN UPDATE SET tgt.routing_mode=src.routing_mode, tgt.load_balance_type=src.load_balance_type, tgt.failover_enabled_yn=src.failover_enabled_yn, tgt.health_check_required_yn=src.health_check_required_yn, tgt.active_yn=src.active_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (service_id, endpoint_code, routing_mode, load_balance_type, failover_enabled_yn, health_check_required_yn, active_yn, priority, created_by, updated_by) VALUES (src.service_id, src.endpoint_code, src.routing_mode, src.load_balance_type, src.failover_enabled_yn, src.health_check_required_yn, src.active_yn, src.priority, src.created_by, src.updated_by);
MERGE INTO OPS_SERVICE_ROUTING_POLICY tgt
USING (SELECT 'EDU' AS service_id, 'EDU_API' AS endpoint_code, 'PRIMARY' AS routing_mode, 'WEIGHT' AS load_balance_type, 'Y' AS failover_enabled_yn, 'Y' AS health_check_required_yn, 'Y' AS active_yn, 100 AS priority, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.service_id=src.service_id AND tgt.endpoint_code=src.endpoint_code AND tgt.priority=src.priority)
WHEN MATCHED THEN UPDATE SET tgt.routing_mode=src.routing_mode, tgt.load_balance_type=src.load_balance_type, tgt.failover_enabled_yn=src.failover_enabled_yn, tgt.health_check_required_yn=src.health_check_required_yn, tgt.active_yn=src.active_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (service_id, endpoint_code, routing_mode, load_balance_type, failover_enabled_yn, health_check_required_yn, active_yn, priority, created_by, updated_by) VALUES (src.service_id, src.endpoint_code, src.routing_mode, src.load_balance_type, src.failover_enabled_yn, src.health_check_required_yn, src.active_yn, src.priority, src.created_by, src.updated_by);
MERGE INTO OPS_SERVICE_ROUTING_POLICY tgt
USING (SELECT 'BAT' AS service_id, 'BAT_API' AS endpoint_code, 'PRIMARY' AS routing_mode, 'WEIGHT' AS load_balance_type, 'Y' AS failover_enabled_yn, 'Y' AS health_check_required_yn, 'Y' AS active_yn, 100 AS priority, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.service_id=src.service_id AND tgt.endpoint_code=src.endpoint_code AND tgt.priority=src.priority)
WHEN MATCHED THEN UPDATE SET tgt.routing_mode=src.routing_mode, tgt.load_balance_type=src.load_balance_type, tgt.failover_enabled_yn=src.failover_enabled_yn, tgt.health_check_required_yn=src.health_check_required_yn, tgt.active_yn=src.active_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (service_id, endpoint_code, routing_mode, load_balance_type, failover_enabled_yn, health_check_required_yn, active_yn, priority, created_by, updated_by) VALUES (src.service_id, src.endpoint_code, src.routing_mode, src.load_balance_type, src.failover_enabled_yn, src.health_check_required_yn, src.active_yn, src.priority, src.created_by, src.updated_by);
MERGE INTO OPS_SERVICE_ROUTING_POLICY tgt
USING (SELECT 'ADM' AS service_id, 'ADM_API' AS endpoint_code, 'PRIMARY' AS routing_mode, 'WEIGHT' AS load_balance_type, 'Y' AS failover_enabled_yn, 'Y' AS health_check_required_yn, 'Y' AS active_yn, 100 AS priority, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.service_id=src.service_id AND tgt.endpoint_code=src.endpoint_code AND tgt.priority=src.priority)
WHEN MATCHED THEN UPDATE SET tgt.routing_mode=src.routing_mode, tgt.load_balance_type=src.load_balance_type, tgt.failover_enabled_yn=src.failover_enabled_yn, tgt.health_check_required_yn=src.health_check_required_yn, tgt.active_yn=src.active_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (service_id, endpoint_code, routing_mode, load_balance_type, failover_enabled_yn, health_check_required_yn, active_yn, priority, created_by, updated_by) VALUES (src.service_id, src.endpoint_code, src.routing_mode, src.load_balance_type, src.failover_enabled_yn, src.health_check_required_yn, src.active_yn, src.priority, src.created_by, src.updated_by);
MERGE INTO OPS_SERVICE_ROUTING_POLICY tgt
USING (SELECT 'CEC' AS service_id, 'CEC_API' AS endpoint_code, 'PRIMARY' AS routing_mode, 'WEIGHT' AS load_balance_type, 'Y' AS failover_enabled_yn, 'Y' AS health_check_required_yn, 'Y' AS active_yn, 100 AS priority, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.service_id=src.service_id AND tgt.endpoint_code=src.endpoint_code AND tgt.priority=src.priority)
WHEN MATCHED THEN UPDATE SET tgt.routing_mode=src.routing_mode, tgt.load_balance_type=src.load_balance_type, tgt.failover_enabled_yn=src.failover_enabled_yn, tgt.health_check_required_yn=src.health_check_required_yn, tgt.active_yn=src.active_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (service_id, endpoint_code, routing_mode, load_balance_type, failover_enabled_yn, health_check_required_yn, active_yn, priority, created_by, updated_by) VALUES (src.service_id, src.endpoint_code, src.routing_mode, src.load_balance_type, src.failover_enabled_yn, src.health_check_required_yn, src.active_yn, src.priority, src.created_by, src.updated_by);
-- ===== END 53_runtime_service_registry_seed.sql =====

-- ===== BEGIN 56_backoffice_product_seed.sql =====
-- AUTO-GENERATED from cpf-tools/db/canonical/seed-model.json
-- vendor=oracle; source=56_backoffice_product_seed.sql
-- DERIVED compatibility input; canonical authority is cpf-tools/db/canonical/**.
-- DO NOT EDIT generated seed directly.

-- CPF_LOGICAL_DATABASE=mbwDB
MERGE INTO MBW_ROLE tgt
USING (SELECT 'MBW_ADMIN' AS role_code, '업무 관리자' AS role_name, 'Y' AS write_allowed_yn, 'ALL' AS data_scope, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_code=src.role_code)
WHEN MATCHED THEN UPDATE SET tgt.role_name=src.role_name, tgt.write_allowed_yn=src.write_allowed_yn, tgt.data_scope=src.data_scope, tgt.use_yn=src.use_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP(3)
WHEN NOT MATCHED THEN INSERT (role_code, role_name, write_allowed_yn, data_scope, use_yn, created_by, updated_by) VALUES (src.role_code, src.role_name, src.write_allowed_yn, src.data_scope, src.use_yn, src.created_by, src.updated_by);
MERGE INTO MBW_ROLE tgt
USING (SELECT 'MBW_OPERATOR' AS role_code, '업무 운영자' AS role_name, 'Y' AS write_allowed_yn, 'ORGANIZATION' AS data_scope, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_code=src.role_code)
WHEN MATCHED THEN UPDATE SET tgt.role_name=src.role_name, tgt.write_allowed_yn=src.write_allowed_yn, tgt.data_scope=src.data_scope, tgt.use_yn=src.use_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP(3)
WHEN NOT MATCHED THEN INSERT (role_code, role_name, write_allowed_yn, data_scope, use_yn, created_by, updated_by) VALUES (src.role_code, src.role_name, src.write_allowed_yn, src.data_scope, src.use_yn, src.created_by, src.updated_by);
MERGE INTO MBW_ROLE tgt
USING (SELECT 'MBW_APPROVER' AS role_code, '업무 결재자' AS role_name, 'Y' AS write_allowed_yn, 'ORGANIZATION' AS data_scope, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_code=src.role_code)
WHEN MATCHED THEN UPDATE SET tgt.role_name=src.role_name, tgt.write_allowed_yn=src.write_allowed_yn, tgt.data_scope=src.data_scope, tgt.use_yn=src.use_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP(3)
WHEN NOT MATCHED THEN INSERT (role_code, role_name, write_allowed_yn, data_scope, use_yn, created_by, updated_by) VALUES (src.role_code, src.role_name, src.write_allowed_yn, src.data_scope, src.use_yn, src.created_by, src.updated_by);
MERGE INTO MBW_ROLE tgt
USING (SELECT 'MBW_VIEWER' AS role_code, '업무 조회자' AS role_name, 'N' AS write_allowed_yn, 'ORGANIZATION' AS data_scope, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_code=src.role_code)
WHEN MATCHED THEN UPDATE SET tgt.role_name=src.role_name, tgt.write_allowed_yn=src.write_allowed_yn, tgt.data_scope=src.data_scope, tgt.use_yn=src.use_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP(3)
WHEN NOT MATCHED THEN INSERT (role_code, role_name, write_allowed_yn, data_scope, use_yn, created_by, updated_by) VALUES (src.role_code, src.role_name, src.write_allowed_yn, src.data_scope, src.use_yn, src.created_by, src.updated_by);
MERGE INTO MBW_MENU tgt
USING (SELECT 'MBW_DASHBOARD' AS menu_code, '업무 관리자 대시보드' AS menu_name, NULL AS parent_menu_code, 'MBW' AS module_code, '/backoffice' AS route_path, 'dashboard' AS icon_code, 'ALL' AS environment_code, '/api/v1/backoffice/dashboard' AS api_path, 10 AS sort_order, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.menu_code=src.menu_code)
WHEN MATCHED THEN UPDATE SET tgt.menu_name=src.menu_name, tgt.parent_menu_code=src.parent_menu_code, tgt.module_code=src.module_code, tgt.route_path=src.route_path, tgt.icon_code=src.icon_code, tgt.environment_code=src.environment_code, tgt.api_path=src.api_path, tgt.sort_order=src.sort_order, tgt.use_yn=src.use_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP(3)
WHEN NOT MATCHED THEN INSERT (menu_code, menu_name, parent_menu_code, module_code, route_path, icon_code, environment_code, api_path, sort_order, use_yn, created_by, updated_by) VALUES (src.menu_code, src.menu_name, src.parent_menu_code, src.module_code, src.route_path, src.icon_code, src.environment_code, src.api_path, src.sort_order, src.use_yn, src.created_by, src.updated_by);
MERGE INTO MBW_MENU tgt
USING (SELECT 'MBW_ORGANIZATION' AS menu_code, '조직 관리' AS menu_name, NULL AS parent_menu_code, 'MBW' AS module_code, '/backoffice/organizations' AS route_path, 'organization' AS icon_code, 'ALL' AS environment_code, '/api/v1/backoffice/organizations' AS api_path, 20 AS sort_order, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.menu_code=src.menu_code)
WHEN MATCHED THEN UPDATE SET tgt.menu_name=src.menu_name, tgt.parent_menu_code=src.parent_menu_code, tgt.module_code=src.module_code, tgt.route_path=src.route_path, tgt.icon_code=src.icon_code, tgt.environment_code=src.environment_code, tgt.api_path=src.api_path, tgt.sort_order=src.sort_order, tgt.use_yn=src.use_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP(3)
WHEN NOT MATCHED THEN INSERT (menu_code, menu_name, parent_menu_code, module_code, route_path, icon_code, environment_code, api_path, sort_order, use_yn, created_by, updated_by) VALUES (src.menu_code, src.menu_name, src.parent_menu_code, src.module_code, src.route_path, src.icon_code, src.environment_code, src.api_path, src.sort_order, src.use_yn, src.created_by, src.updated_by);
MERGE INTO MBW_MENU tgt
USING (SELECT 'MBW_EMPLOYEE' AS menu_code, '직원·소속 관리' AS menu_name, NULL AS parent_menu_code, 'MBW' AS module_code, '/backoffice/employees' AS route_path, 'employee' AS icon_code, 'ALL' AS environment_code, '/api/v1/backoffice/employees' AS api_path, 30 AS sort_order, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.menu_code=src.menu_code)
WHEN MATCHED THEN UPDATE SET tgt.menu_name=src.menu_name, tgt.parent_menu_code=src.parent_menu_code, tgt.module_code=src.module_code, tgt.route_path=src.route_path, tgt.icon_code=src.icon_code, tgt.environment_code=src.environment_code, tgt.api_path=src.api_path, tgt.sort_order=src.sort_order, tgt.use_yn=src.use_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP(3)
WHEN NOT MATCHED THEN INSERT (menu_code, menu_name, parent_menu_code, module_code, route_path, icon_code, environment_code, api_path, sort_order, use_yn, created_by, updated_by) VALUES (src.menu_code, src.menu_name, src.parent_menu_code, src.module_code, src.route_path, src.icon_code, src.environment_code, src.api_path, src.sort_order, src.use_yn, src.created_by, src.updated_by);
MERGE INTO MBW_MENU tgt
USING (SELECT 'MBW_AUTHORIZATION' AS menu_code, '업무 권한 관리' AS menu_name, NULL AS parent_menu_code, 'MBW' AS module_code, '/backoffice/authorization' AS route_path, 'shield' AS icon_code, 'ALL' AS environment_code, '/api/v1/backoffice/authorization' AS api_path, 40 AS sort_order, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.menu_code=src.menu_code)
WHEN MATCHED THEN UPDATE SET tgt.menu_name=src.menu_name, tgt.parent_menu_code=src.parent_menu_code, tgt.module_code=src.module_code, tgt.route_path=src.route_path, tgt.icon_code=src.icon_code, tgt.environment_code=src.environment_code, tgt.api_path=src.api_path, tgt.sort_order=src.sort_order, tgt.use_yn=src.use_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP(3)
WHEN NOT MATCHED THEN INSERT (menu_code, menu_name, parent_menu_code, module_code, route_path, icon_code, environment_code, api_path, sort_order, use_yn, created_by, updated_by) VALUES (src.menu_code, src.menu_name, src.parent_menu_code, src.module_code, src.route_path, src.icon_code, src.environment_code, src.api_path, src.sort_order, src.use_yn, src.created_by, src.updated_by);
MERGE INTO MBW_MENU tgt
USING (SELECT 'MBW_APPROVAL' AS menu_code, '업무 결재 관리' AS menu_name, NULL AS parent_menu_code, 'MBW' AS module_code, '/backoffice/approvals' AS route_path, 'approval' AS icon_code, 'ALL' AS environment_code, '/api/v1/backoffice/approvals' AS api_path, 50 AS sort_order, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.menu_code=src.menu_code)
WHEN MATCHED THEN UPDATE SET tgt.menu_name=src.menu_name, tgt.parent_menu_code=src.parent_menu_code, tgt.module_code=src.module_code, tgt.route_path=src.route_path, tgt.icon_code=src.icon_code, tgt.environment_code=src.environment_code, tgt.api_path=src.api_path, tgt.sort_order=src.sort_order, tgt.use_yn=src.use_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP(3)
WHEN NOT MATCHED THEN INSERT (menu_code, menu_name, parent_menu_code, module_code, route_path, icon_code, environment_code, api_path, sort_order, use_yn, created_by, updated_by) VALUES (src.menu_code, src.menu_name, src.parent_menu_code, src.module_code, src.route_path, src.icon_code, src.environment_code, src.api_path, src.sort_order, src.use_yn, src.created_by, src.updated_by);
MERGE INTO MBW_MENU tgt
USING (SELECT 'MBW_AUDIT' AS menu_code, '업무 감사 조회' AS menu_name, NULL AS parent_menu_code, 'MBW' AS module_code, '/backoffice/audits' AS route_path, 'audit' AS icon_code, 'ALL' AS environment_code, '/api/v1/backoffice/audits' AS api_path, 60 AS sort_order, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.menu_code=src.menu_code)
WHEN MATCHED THEN UPDATE SET tgt.menu_name=src.menu_name, tgt.parent_menu_code=src.parent_menu_code, tgt.module_code=src.module_code, tgt.route_path=src.route_path, tgt.icon_code=src.icon_code, tgt.environment_code=src.environment_code, tgt.api_path=src.api_path, tgt.sort_order=src.sort_order, tgt.use_yn=src.use_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP(3)
WHEN NOT MATCHED THEN INSERT (menu_code, menu_name, parent_menu_code, module_code, route_path, icon_code, environment_code, api_path, sort_order, use_yn, created_by, updated_by) VALUES (src.menu_code, src.menu_name, src.parent_menu_code, src.module_code, src.route_path, src.icon_code, src.environment_code, src.api_path, src.sort_order, src.use_yn, src.created_by, src.updated_by);
MERGE INTO MBW_MENU tgt
USING (SELECT 'MBW_ATTACHMENT' AS menu_code, '첨부 관리' AS menu_name, NULL AS parent_menu_code, 'MBW' AS module_code, '/backoffice/attachments' AS route_path, 'attachment' AS icon_code, 'ALL' AS environment_code, '/api/v1/backoffice/attachments' AS api_path, 70 AS sort_order, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.menu_code=src.menu_code)
WHEN MATCHED THEN UPDATE SET tgt.menu_name=src.menu_name, tgt.parent_menu_code=src.parent_menu_code, tgt.module_code=src.module_code, tgt.route_path=src.route_path, tgt.icon_code=src.icon_code, tgt.environment_code=src.environment_code, tgt.api_path=src.api_path, tgt.sort_order=src.sort_order, tgt.use_yn=src.use_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP(3)
WHEN NOT MATCHED THEN INSERT (menu_code, menu_name, parent_menu_code, module_code, route_path, icon_code, environment_code, api_path, sort_order, use_yn, created_by, updated_by) VALUES (src.menu_code, src.menu_name, src.parent_menu_code, src.module_code, src.route_path, src.icon_code, src.environment_code, src.api_path, src.sort_order, src.use_yn, src.created_by, src.updated_by);
MERGE INTO MBW_MENU tgt
USING (SELECT 'MBW_SETTING' AS menu_code, '업무 관리자 설정' AS menu_name, NULL AS parent_menu_code, 'MBW' AS module_code, '/backoffice/settings' AS route_path, 'setting' AS icon_code, 'ALL' AS environment_code, '/api/v1/backoffice/settings' AS api_path, 80 AS sort_order, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.menu_code=src.menu_code)
WHEN MATCHED THEN UPDATE SET tgt.menu_name=src.menu_name, tgt.parent_menu_code=src.parent_menu_code, tgt.module_code=src.module_code, tgt.route_path=src.route_path, tgt.icon_code=src.icon_code, tgt.environment_code=src.environment_code, tgt.api_path=src.api_path, tgt.sort_order=src.sort_order, tgt.use_yn=src.use_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP(3)
WHEN NOT MATCHED THEN INSERT (menu_code, menu_name, parent_menu_code, module_code, route_path, icon_code, environment_code, api_path, sort_order, use_yn, created_by, updated_by) VALUES (src.menu_code, src.menu_name, src.parent_menu_code, src.module_code, src.route_path, src.icon_code, src.environment_code, src.api_path, src.sort_order, src.use_yn, src.created_by, src.updated_by);
MERGE INTO MBW_PERMISSION tgt
USING (SELECT 'MBW_ADMIN' AS role_code, menu_code AS menu_code, 'ALL' AS button_code, 'API' AS permission_type, '*' AS http_method, CONCAT(api_path, '/**') AS api_pattern, NULL AS domain_code, environment_code AS environment_code, 'ALL' AS data_scope, 'Y' AS allow_yn, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM MBW_MENU
WHERE use_yn = 'Y') src
ON (tgt.role_code=src.role_code AND tgt.menu_code=src.menu_code AND tgt.button_code=src.button_code AND tgt.permission_type=src.permission_type AND tgt.environment_code=src.environment_code)
WHEN MATCHED THEN UPDATE SET tgt.http_method=src.http_method, tgt.api_pattern=src.api_pattern, tgt.data_scope=src.data_scope, tgt.allow_yn=src.allow_yn, tgt.use_yn=src.use_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP(3)
WHEN NOT MATCHED THEN INSERT (role_code, menu_code, button_code, permission_type, http_method, api_pattern, domain_code, environment_code, data_scope, allow_yn, use_yn, created_by, updated_by) VALUES (src.role_code, src.menu_code, src.button_code, src.permission_type, src.http_method, src.api_pattern, src.domain_code, src.environment_code, src.data_scope, src.allow_yn, src.use_yn, src.created_by, src.updated_by);
MERGE INTO MBW_PERMISSION tgt
USING (SELECT 'MBW_OPERATOR' AS role_code, 'MBW_DASHBOARD' AS menu_code, 'READ' AS button_code, 'API' AS permission_type, 'GET' AS http_method, '/api/v1/backoffice/dashboard/**' AS api_pattern, NULL AS domain_code, 'ALL' AS environment_code, 'ORGANIZATION' AS data_scope, 'Y' AS allow_yn, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_code=src.role_code AND tgt.menu_code=src.menu_code AND tgt.button_code=src.button_code AND tgt.permission_type=src.permission_type AND tgt.environment_code=src.environment_code)
WHEN MATCHED THEN UPDATE SET tgt.http_method=src.http_method, tgt.api_pattern=src.api_pattern, tgt.data_scope=src.data_scope, tgt.allow_yn=src.allow_yn, tgt.use_yn=src.use_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP(3)
WHEN NOT MATCHED THEN INSERT (role_code, menu_code, button_code, permission_type, http_method, api_pattern, domain_code, environment_code, data_scope, allow_yn, use_yn, created_by, updated_by) VALUES (src.role_code, src.menu_code, src.button_code, src.permission_type, src.http_method, src.api_pattern, src.domain_code, src.environment_code, src.data_scope, src.allow_yn, src.use_yn, src.created_by, src.updated_by);
MERGE INTO MBW_PERMISSION tgt
USING (SELECT 'MBW_OPERATOR' AS role_code, 'MBW_ORGANIZATION' AS menu_code, 'READ' AS button_code, 'API' AS permission_type, 'GET' AS http_method, '/api/v1/backoffice/organizations/**' AS api_pattern, NULL AS domain_code, 'ALL' AS environment_code, 'ORGANIZATION' AS data_scope, 'Y' AS allow_yn, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_code=src.role_code AND tgt.menu_code=src.menu_code AND tgt.button_code=src.button_code AND tgt.permission_type=src.permission_type AND tgt.environment_code=src.environment_code)
WHEN MATCHED THEN UPDATE SET tgt.http_method=src.http_method, tgt.api_pattern=src.api_pattern, tgt.data_scope=src.data_scope, tgt.allow_yn=src.allow_yn, tgt.use_yn=src.use_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP(3)
WHEN NOT MATCHED THEN INSERT (role_code, menu_code, button_code, permission_type, http_method, api_pattern, domain_code, environment_code, data_scope, allow_yn, use_yn, created_by, updated_by) VALUES (src.role_code, src.menu_code, src.button_code, src.permission_type, src.http_method, src.api_pattern, src.domain_code, src.environment_code, src.data_scope, src.allow_yn, src.use_yn, src.created_by, src.updated_by);
MERGE INTO MBW_PERMISSION tgt
USING (SELECT 'MBW_OPERATOR' AS role_code, 'MBW_EMPLOYEE' AS menu_code, 'READ' AS button_code, 'API' AS permission_type, 'GET' AS http_method, '/api/v1/backoffice/employees/**' AS api_pattern, NULL AS domain_code, 'ALL' AS environment_code, 'ORGANIZATION' AS data_scope, 'Y' AS allow_yn, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_code=src.role_code AND tgt.menu_code=src.menu_code AND tgt.button_code=src.button_code AND tgt.permission_type=src.permission_type AND tgt.environment_code=src.environment_code)
WHEN MATCHED THEN UPDATE SET tgt.http_method=src.http_method, tgt.api_pattern=src.api_pattern, tgt.data_scope=src.data_scope, tgt.allow_yn=src.allow_yn, tgt.use_yn=src.use_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP(3)
WHEN NOT MATCHED THEN INSERT (role_code, menu_code, button_code, permission_type, http_method, api_pattern, domain_code, environment_code, data_scope, allow_yn, use_yn, created_by, updated_by) VALUES (src.role_code, src.menu_code, src.button_code, src.permission_type, src.http_method, src.api_pattern, src.domain_code, src.environment_code, src.data_scope, src.allow_yn, src.use_yn, src.created_by, src.updated_by);
MERGE INTO MBW_PERMISSION tgt
USING (SELECT 'MBW_APPROVER' AS role_code, 'MBW_APPROVAL' AS menu_code, 'READ' AS button_code, 'API' AS permission_type, 'GET' AS http_method, '/api/v1/backoffice/approvals/**' AS api_pattern, NULL AS domain_code, 'ALL' AS environment_code, 'ORGANIZATION' AS data_scope, 'Y' AS allow_yn, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_code=src.role_code AND tgt.menu_code=src.menu_code AND tgt.button_code=src.button_code AND tgt.permission_type=src.permission_type AND tgt.environment_code=src.environment_code)
WHEN MATCHED THEN UPDATE SET tgt.http_method=src.http_method, tgt.api_pattern=src.api_pattern, tgt.data_scope=src.data_scope, tgt.allow_yn=src.allow_yn, tgt.use_yn=src.use_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP(3)
WHEN NOT MATCHED THEN INSERT (role_code, menu_code, button_code, permission_type, http_method, api_pattern, domain_code, environment_code, data_scope, allow_yn, use_yn, created_by, updated_by) VALUES (src.role_code, src.menu_code, src.button_code, src.permission_type, src.http_method, src.api_pattern, src.domain_code, src.environment_code, src.data_scope, src.allow_yn, src.use_yn, src.created_by, src.updated_by);
MERGE INTO MBW_PERMISSION tgt
USING (SELECT 'MBW_APPROVER' AS role_code, 'MBW_APPROVAL' AS menu_code, 'DECIDE' AS button_code, 'API' AS permission_type, 'POST' AS http_method, '/api/v1/backoffice/approvals/*/decisions' AS api_pattern, NULL AS domain_code, 'ALL' AS environment_code, 'ORGANIZATION' AS data_scope, 'Y' AS allow_yn, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_code=src.role_code AND tgt.menu_code=src.menu_code AND tgt.button_code=src.button_code AND tgt.permission_type=src.permission_type AND tgt.environment_code=src.environment_code)
WHEN MATCHED THEN UPDATE SET tgt.http_method=src.http_method, tgt.api_pattern=src.api_pattern, tgt.data_scope=src.data_scope, tgt.allow_yn=src.allow_yn, tgt.use_yn=src.use_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP(3)
WHEN NOT MATCHED THEN INSERT (role_code, menu_code, button_code, permission_type, http_method, api_pattern, domain_code, environment_code, data_scope, allow_yn, use_yn, created_by, updated_by) VALUES (src.role_code, src.menu_code, src.button_code, src.permission_type, src.http_method, src.api_pattern, src.domain_code, src.environment_code, src.data_scope, src.allow_yn, src.use_yn, src.created_by, src.updated_by);
MERGE INTO MBW_PERMISSION tgt
USING (SELECT 'MBW_VIEWER' AS role_code, 'MBW_DASHBOARD' AS menu_code, 'READ' AS button_code, 'API' AS permission_type, 'GET' AS http_method, '/api/v1/backoffice/dashboard/**' AS api_pattern, NULL AS domain_code, 'ALL' AS environment_code, 'ORGANIZATION' AS data_scope, 'Y' AS allow_yn, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_code=src.role_code AND tgt.menu_code=src.menu_code AND tgt.button_code=src.button_code AND tgt.permission_type=src.permission_type AND tgt.environment_code=src.environment_code)
WHEN MATCHED THEN UPDATE SET tgt.http_method=src.http_method, tgt.api_pattern=src.api_pattern, tgt.data_scope=src.data_scope, tgt.allow_yn=src.allow_yn, tgt.use_yn=src.use_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP(3)
WHEN NOT MATCHED THEN INSERT (role_code, menu_code, button_code, permission_type, http_method, api_pattern, domain_code, environment_code, data_scope, allow_yn, use_yn, created_by, updated_by) VALUES (src.role_code, src.menu_code, src.button_code, src.permission_type, src.http_method, src.api_pattern, src.domain_code, src.environment_code, src.data_scope, src.allow_yn, src.use_yn, src.created_by, src.updated_by);
MERGE INTO MBW_PERMISSION tgt
USING (SELECT 'MBW_VIEWER' AS role_code, 'MBW_AUDIT' AS menu_code, 'READ' AS button_code, 'API' AS permission_type, 'GET' AS http_method, '/api/v1/backoffice/audits/**' AS api_pattern, NULL AS domain_code, 'ALL' AS environment_code, 'ORGANIZATION' AS data_scope, 'Y' AS allow_yn, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_code=src.role_code AND tgt.menu_code=src.menu_code AND tgt.button_code=src.button_code AND tgt.permission_type=src.permission_type AND tgt.environment_code=src.environment_code)
WHEN MATCHED THEN UPDATE SET tgt.http_method=src.http_method, tgt.api_pattern=src.api_pattern, tgt.data_scope=src.data_scope, tgt.allow_yn=src.allow_yn, tgt.use_yn=src.use_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP(3)
WHEN NOT MATCHED THEN INSERT (role_code, menu_code, button_code, permission_type, http_method, api_pattern, domain_code, environment_code, data_scope, allow_yn, use_yn, created_by, updated_by) VALUES (src.role_code, src.menu_code, src.button_code, src.permission_type, src.http_method, src.api_pattern, src.domain_code, src.environment_code, src.data_scope, src.allow_yn, src.use_yn, src.created_by, src.updated_by);
MERGE INTO MBW_PROJECT_SETTING tgt
USING (SELECT 'MBW.APPROVAL.SELF_APPROVAL_ALLOWED' AS setting_key, 'N' AS setting_value, '기본 자기승인 차단 정책' AS description, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.setting_key=src.setting_key)
WHEN MATCHED THEN UPDATE SET tgt.setting_value=src.setting_value, tgt.description=src.description, tgt.use_yn=src.use_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP(3)
WHEN NOT MATCHED THEN INSERT (setting_key, setting_value, description, use_yn, created_by, updated_by) VALUES (src.setting_key, src.setting_value, src.description, src.use_yn, src.created_by, src.updated_by);
MERGE INTO MBW_PROJECT_SETTING tgt
USING (SELECT 'MBW.APPROVAL.DEFAULT_DUE_HOURS' AS setting_key, '24' AS setting_value, '기본 결재 SLA 시간' AS description, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.setting_key=src.setting_key)
WHEN MATCHED THEN UPDATE SET tgt.setting_value=src.setting_value, tgt.description=src.description, tgt.use_yn=src.use_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP(3)
WHEN NOT MATCHED THEN INSERT (setting_key, setting_value, description, use_yn, created_by, updated_by) VALUES (src.setting_key, src.setting_value, src.description, src.use_yn, src.created_by, src.updated_by);
MERGE INTO MBW_PROJECT_SETTING tgt
USING (SELECT 'MBW.APPROVAL.REQUIRE_PAYLOAD_HASH' AS setting_key, 'Y' AS setting_value, '결재 대상 Payload 변조 검증용 SHA-256 사용' AS description, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.setting_key=src.setting_key)
WHEN MATCHED THEN UPDATE SET tgt.setting_value=src.setting_value, tgt.description=src.description, tgt.use_yn=src.use_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP(3)
WHEN NOT MATCHED THEN INSERT (setting_key, setting_value, description, use_yn, created_by, updated_by) VALUES (src.setting_key, src.setting_value, src.description, src.use_yn, src.created_by, src.updated_by);
MERGE INTO MBW_PROJECT_SETTING tgt
USING (SELECT 'MBW.AUDIT.HASH_CHAIN_ENABLED' AS setting_key, 'Y' AS setting_value, '업무 감사 로그 hash-chain 검증 사용' AS description, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.setting_key=src.setting_key)
WHEN MATCHED THEN UPDATE SET tgt.setting_value=src.setting_value, tgt.description=src.description, tgt.use_yn=src.use_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP(3)
WHEN NOT MATCHED THEN INSERT (setting_key, setting_value, description, use_yn, created_by, updated_by) VALUES (src.setting_key, src.setting_value, src.description, src.use_yn, src.created_by, src.updated_by);
MERGE INTO MBW_PROJECT_SETTING tgt
USING (SELECT 'MBW.ATTACHMENT.SECURITY_SCAN_REQUIRED' AS setting_key, 'Y' AS setting_value, '첨부 보안검사 완료 후 사용 허용' AS description, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.setting_key=src.setting_key)
WHEN MATCHED THEN UPDATE SET tgt.setting_value=src.setting_value, tgt.description=src.description, tgt.use_yn=src.use_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP(3)
WHEN NOT MATCHED THEN INSERT (setting_key, setting_value, description, use_yn, created_by, updated_by) VALUES (src.setting_key, src.setting_value, src.description, src.use_yn, src.created_by, src.updated_by);
MERGE INTO MBW_PROJECT_SETTING tgt
USING (SELECT 'MBW.ATTACHMENT.DEFAULT_RETENTION_DAYS' AS setting_key, '365' AS setting_value, '첨부 기본 보존일수' AS description, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.setting_key=src.setting_key)
WHEN MATCHED THEN UPDATE SET tgt.setting_value=src.setting_value, tgt.description=src.description, tgt.use_yn=src.use_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP(3)
WHEN NOT MATCHED THEN INSERT (setting_key, setting_value, description, use_yn, created_by, updated_by) VALUES (src.setting_key, src.setting_value, src.description, src.use_yn, src.created_by, src.updated_by);
MERGE INTO MBW_PERMISSION tgt
USING (SELECT 'MBW_ADMIN' AS role_code, 'MBW_AUTHORIZATION' AS menu_code, 'SIMULATE' AS button_code, 'API' AS permission_type, 'GET' AS http_method, '/api/v1/backoffice/permissions/effective' AS api_pattern, NULL AS domain_code, 'ALL' AS environment_code, 'ALL' AS data_scope, 'Y' AS allow_yn, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_code=src.role_code AND tgt.menu_code=src.menu_code AND tgt.button_code=src.button_code AND tgt.permission_type=src.permission_type AND tgt.environment_code=src.environment_code)
WHEN MATCHED THEN UPDATE SET tgt.http_method=src.http_method, tgt.api_pattern=src.api_pattern, tgt.domain_code=src.domain_code, tgt.data_scope=src.data_scope, tgt.allow_yn=src.allow_yn, tgt.use_yn=src.use_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_code, menu_code, button_code, permission_type, http_method, api_pattern, domain_code, environment_code, data_scope, allow_yn, use_yn, created_by, updated_by) VALUES (src.role_code, src.menu_code, src.button_code, src.permission_type, src.http_method, src.api_pattern, src.domain_code, src.environment_code, src.data_scope, src.allow_yn, src.use_yn, src.created_by, src.updated_by);
MERGE INTO MBW_PERMISSION tgt
USING (SELECT 'MBW_ADMIN' AS role_code, 'MBW_EMPLOYEE' AS menu_code, 'PII_RAW' AS button_code, 'API' AS permission_type, 'POST' AS http_method, '/api/v1/backoffice/employees/*/contacts/raw' AS api_pattern, NULL AS domain_code, 'ALL' AS environment_code, 'ALL' AS data_scope, 'Y' AS allow_yn, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_code=src.role_code AND tgt.menu_code=src.menu_code AND tgt.button_code=src.button_code AND tgt.permission_type=src.permission_type AND tgt.environment_code=src.environment_code)
WHEN MATCHED THEN UPDATE SET tgt.http_method=src.http_method, tgt.api_pattern=src.api_pattern, tgt.domain_code=src.domain_code, tgt.data_scope=src.data_scope, tgt.allow_yn=src.allow_yn, tgt.use_yn=src.use_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_code, menu_code, button_code, permission_type, http_method, api_pattern, domain_code, environment_code, data_scope, allow_yn, use_yn, created_by, updated_by) VALUES (src.role_code, src.menu_code, src.button_code, src.permission_type, src.http_method, src.api_pattern, src.domain_code, src.environment_code, src.data_scope, src.allow_yn, src.use_yn, src.created_by, src.updated_by);
MERGE INTO MBW_PERMISSION tgt
USING (SELECT 'MBW_OPERATOR' AS role_code, 'MBW_AUTHORIZATION' AS menu_code, 'SIMULATE' AS button_code, 'API' AS permission_type, 'GET' AS http_method, '/api/v1/backoffice/permissions/effective' AS api_pattern, NULL AS domain_code, 'ALL' AS environment_code, 'ORGANIZATION' AS data_scope, 'Y' AS allow_yn, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_code=src.role_code AND tgt.menu_code=src.menu_code AND tgt.button_code=src.button_code AND tgt.permission_type=src.permission_type AND tgt.environment_code=src.environment_code)
WHEN MATCHED THEN UPDATE SET tgt.http_method=src.http_method, tgt.api_pattern=src.api_pattern, tgt.domain_code=src.domain_code, tgt.data_scope=src.data_scope, tgt.allow_yn=src.allow_yn, tgt.use_yn=src.use_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_code, menu_code, button_code, permission_type, http_method, api_pattern, domain_code, environment_code, data_scope, allow_yn, use_yn, created_by, updated_by) VALUES (src.role_code, src.menu_code, src.button_code, src.permission_type, src.http_method, src.api_pattern, src.domain_code, src.environment_code, src.data_scope, src.allow_yn, src.use_yn, src.created_by, src.updated_by);
MERGE INTO MBW_PERMISSION tgt
USING (SELECT 'MBW_APPROVER' AS role_code, 'MBW_APPROVAL' AS menu_code, 'DECIDE' AS button_code, 'API' AS permission_type, 'POST' AS http_method, '/api/v1/backoffice/approvals/*/decisions' AS api_pattern, NULL AS domain_code, 'ALL' AS environment_code, 'ORGANIZATION' AS data_scope, 'Y' AS allow_yn, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_code=src.role_code AND tgt.menu_code=src.menu_code AND tgt.button_code=src.button_code AND tgt.permission_type=src.permission_type AND tgt.environment_code=src.environment_code)
WHEN MATCHED THEN UPDATE SET tgt.http_method=src.http_method, tgt.api_pattern=src.api_pattern, tgt.domain_code=src.domain_code, tgt.data_scope=src.data_scope, tgt.allow_yn=src.allow_yn, tgt.use_yn=src.use_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_code, menu_code, button_code, permission_type, http_method, api_pattern, domain_code, environment_code, data_scope, allow_yn, use_yn, created_by, updated_by) VALUES (src.role_code, src.menu_code, src.button_code, src.permission_type, src.http_method, src.api_pattern, src.domain_code, src.environment_code, src.data_scope, src.allow_yn, src.use_yn, src.created_by, src.updated_by);
-- ===== END 56_backoffice_product_seed.sql =====

-- ===== BEGIN 60_adm_seed_data.sql =====
-- AUTO-GENERATED from cpf-tools/db/canonical/seed-model.json
-- vendor=oracle; source=60_adm_seed_data.sql
-- DERIVED compatibility input; canonical authority is cpf-tools/db/canonical/**.
-- DO NOT EDIT generated seed directly.

-- CPF_LOGICAL_DATABASE=cpfDB
MERGE INTO ADM_ROLE tgt
USING (SELECT 'ADM_ADMIN' AS ROLE_ID, '프레임워크 관리자' AS ROLE_NAME, 'ADMIN' AS ROLE_TYPE, '모든 ADM 메뉴와 운영 작업을 관리합니다.' AS DESCRIPTION, 'Y' AS USE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.ROLE_ID=src.ROLE_ID)
WHEN MATCHED THEN UPDATE SET tgt.ROLE_NAME=src.ROLE_NAME, tgt.ROLE_TYPE=src.ROLE_TYPE, tgt.DESCRIPTION=src.DESCRIPTION, tgt.USE_YN=src.USE_YN, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (ROLE_ID, ROLE_NAME, ROLE_TYPE, DESCRIPTION, USE_YN, created_by, updated_by) VALUES (src.ROLE_ID, src.ROLE_NAME, src.ROLE_TYPE, src.DESCRIPTION, src.USE_YN, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE tgt
USING (SELECT 'ADM_DEV_OPERATOR' AS ROLE_ID, '개발자 운영자' AS ROLE_NAME, 'DEVELOPER_OPERATOR' AS ROLE_TYPE, '로그, 캐시, 코드, 메시지, 설정, 배치 관제를 운영합니다.' AS DESCRIPTION, 'Y' AS USE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.ROLE_ID=src.ROLE_ID)
WHEN MATCHED THEN UPDATE SET tgt.ROLE_NAME=src.ROLE_NAME, tgt.ROLE_TYPE=src.ROLE_TYPE, tgt.DESCRIPTION=src.DESCRIPTION, tgt.USE_YN=src.USE_YN, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (ROLE_ID, ROLE_NAME, ROLE_TYPE, DESCRIPTION, USE_YN, created_by, updated_by) VALUES (src.ROLE_ID, src.ROLE_NAME, src.ROLE_TYPE, src.DESCRIPTION, src.USE_YN, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE tgt
USING (SELECT 'ADM_BIZ_OPERATOR' AS ROLE_ID, '업무 운영자' AS ROLE_NAME, 'BUSINESS_OPERATOR' AS ROLE_TYPE, '회원, 거래 로그, 배치, 캐시 같은 업무 운영 기능을 수행합니다.' AS DESCRIPTION, 'Y' AS USE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.ROLE_ID=src.ROLE_ID)
WHEN MATCHED THEN UPDATE SET tgt.ROLE_NAME=src.ROLE_NAME, tgt.ROLE_TYPE=src.ROLE_TYPE, tgt.DESCRIPTION=src.DESCRIPTION, tgt.USE_YN=src.USE_YN, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (ROLE_ID, ROLE_NAME, ROLE_TYPE, DESCRIPTION, USE_YN, created_by, updated_by) VALUES (src.ROLE_ID, src.ROLE_NAME, src.ROLE_TYPE, src.DESCRIPTION, src.USE_YN, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE tgt
USING (SELECT 'ADM_VIEWER' AS ROLE_ID, '조회 전용 운영자' AS ROLE_NAME, 'VIEWER' AS ROLE_TYPE, '운영 정보를 조회만 할 수 있습니다.' AS DESCRIPTION, 'Y' AS USE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.ROLE_ID=src.ROLE_ID)
WHEN MATCHED THEN UPDATE SET tgt.ROLE_NAME=src.ROLE_NAME, tgt.ROLE_TYPE=src.ROLE_TYPE, tgt.DESCRIPTION=src.DESCRIPTION, tgt.USE_YN=src.USE_YN, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (ROLE_ID, ROLE_NAME, ROLE_TYPE, DESCRIPTION, USE_YN, created_by, updated_by) VALUES (src.ROLE_ID, src.ROLE_NAME, src.ROLE_TYPE, src.DESCRIPTION, src.USE_YN, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE tgt
USING (SELECT 'ADM_OPERATOR' AS ROLE_ID, '운영자 호환 역할' AS ROLE_NAME, 'DEVELOPER_OPERATOR' AS ROLE_TYPE, '기존 ADM_OPERATOR 호환을 위한 역할입니다.' AS DESCRIPTION, 'Y' AS USE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.ROLE_ID=src.ROLE_ID)
WHEN MATCHED THEN UPDATE SET tgt.ROLE_NAME=src.ROLE_NAME, tgt.ROLE_TYPE=src.ROLE_TYPE, tgt.DESCRIPTION=src.DESCRIPTION, tgt.USE_YN=src.USE_YN, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (ROLE_ID, ROLE_NAME, ROLE_TYPE, DESCRIPTION, USE_YN, created_by, updated_by) VALUES (src.ROLE_ID, src.ROLE_NAME, src.ROLE_TYPE, src.DESCRIPTION, src.USE_YN, src.created_by, src.updated_by);
MERGE INTO ADM_MENU tgt
USING (SELECT 'DASHBOARD' AS MENU_ID, NULL AS PARENT_MENU_ID, '대시보드' AS MENU_NAME, '/adm' AS MENU_PATH, 10 AS SORT_ORDER, 'Y' AS USE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.MENU_ID=src.MENU_ID)
WHEN MATCHED THEN UPDATE SET tgt.PARENT_MENU_ID=src.PARENT_MENU_ID, tgt.MENU_NAME=src.MENU_NAME, tgt.MENU_PATH=src.MENU_PATH, tgt.SORT_ORDER=src.SORT_ORDER, tgt.USE_YN=src.USE_YN, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (MENU_ID, PARENT_MENU_ID, MENU_NAME, MENU_PATH, SORT_ORDER, USE_YN, created_by, updated_by) VALUES (src.MENU_ID, src.PARENT_MENU_ID, src.MENU_NAME, src.MENU_PATH, src.SORT_ORDER, src.USE_YN, src.created_by, src.updated_by);
MERGE INTO ADM_MENU tgt
USING (SELECT 'CAPABILITY_FLEET' AS MENU_ID, NULL AS PARENT_MENU_ID, 'CPF Capability' AS MENU_NAME, '/adm#capabilities' AS MENU_PATH, 15 AS SORT_ORDER, 'Y' AS USE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.MENU_ID=src.MENU_ID)
WHEN MATCHED THEN UPDATE SET tgt.PARENT_MENU_ID=src.PARENT_MENU_ID, tgt.MENU_NAME=src.MENU_NAME, tgt.MENU_PATH=src.MENU_PATH, tgt.SORT_ORDER=src.SORT_ORDER, tgt.USE_YN=src.USE_YN, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (MENU_ID, PARENT_MENU_ID, MENU_NAME, MENU_PATH, SORT_ORDER, USE_YN, created_by, updated_by) VALUES (src.MENU_ID, src.PARENT_MENU_ID, src.MENU_NAME, src.MENU_PATH, src.SORT_ORDER, src.USE_YN, src.created_by, src.updated_by);
MERGE INTO ADM_MENU tgt
USING (SELECT 'LOG_LIST' AS MENU_ID, NULL AS PARENT_MENU_ID, '온라인 거래 로그' AS MENU_NAME, '/adm#logs' AS MENU_PATH, 20 AS SORT_ORDER, 'Y' AS USE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.MENU_ID=src.MENU_ID)
WHEN MATCHED THEN UPDATE SET tgt.PARENT_MENU_ID=src.PARENT_MENU_ID, tgt.MENU_NAME=src.MENU_NAME, tgt.MENU_PATH=src.MENU_PATH, tgt.SORT_ORDER=src.SORT_ORDER, tgt.USE_YN=src.USE_YN, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (MENU_ID, PARENT_MENU_ID, MENU_NAME, MENU_PATH, SORT_ORDER, USE_YN, created_by, updated_by) VALUES (src.MENU_ID, src.PARENT_MENU_ID, src.MENU_NAME, src.MENU_PATH, src.SORT_ORDER, src.USE_YN, src.created_by, src.updated_by);
MERGE INTO ADM_MENU tgt
USING (SELECT 'STANDARD_EXECUTION' AS MENU_ID, NULL AS PARENT_MENU_ID, '표준 실행 카탈로그' AS MENU_NAME, '/adm#standard-executions' AS MENU_PATH, 23 AS SORT_ORDER, 'Y' AS USE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.MENU_ID=src.MENU_ID)
WHEN MATCHED THEN UPDATE SET tgt.PARENT_MENU_ID=src.PARENT_MENU_ID, tgt.MENU_NAME=src.MENU_NAME, tgt.MENU_PATH=src.MENU_PATH, tgt.SORT_ORDER=src.SORT_ORDER, tgt.USE_YN=src.USE_YN, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (MENU_ID, PARENT_MENU_ID, MENU_NAME, MENU_PATH, SORT_ORDER, USE_YN, created_by, updated_by) VALUES (src.MENU_ID, src.PARENT_MENU_ID, src.MENU_NAME, src.MENU_PATH, src.SORT_ORDER, src.USE_YN, src.created_by, src.updated_by);
MERGE INTO ADM_MENU tgt
USING (SELECT 'CHANNEL_POLICY' AS MENU_ID, NULL AS PARENT_MENU_ID, '채널 정책' AS MENU_NAME, '/adm#channel-policy' AS MENU_PATH, 24 AS SORT_ORDER, 'Y' AS USE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.MENU_ID=src.MENU_ID)
WHEN MATCHED THEN UPDATE SET tgt.PARENT_MENU_ID=src.PARENT_MENU_ID, tgt.MENU_NAME=src.MENU_NAME, tgt.MENU_PATH=src.MENU_PATH, tgt.SORT_ORDER=src.SORT_ORDER, tgt.USE_YN=src.USE_YN, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (MENU_ID, PARENT_MENU_ID, MENU_NAME, MENU_PATH, SORT_ORDER, USE_YN, created_by, updated_by) VALUES (src.MENU_ID, src.PARENT_MENU_ID, src.MENU_NAME, src.MENU_PATH, src.SORT_ORDER, src.USE_YN, src.created_by, src.updated_by);
MERGE INTO ADM_MENU tgt
USING (SELECT 'REMOTE_LOG' AS MENU_ID, NULL AS PARENT_MENU_ID, '원격 로그 관리' AS MENU_NAME, '/adm#remote-logs' AS MENU_PATH, 25 AS SORT_ORDER, 'Y' AS USE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.MENU_ID=src.MENU_ID)
WHEN MATCHED THEN UPDATE SET tgt.PARENT_MENU_ID=src.PARENT_MENU_ID, tgt.MENU_NAME=src.MENU_NAME, tgt.MENU_PATH=src.MENU_PATH, tgt.SORT_ORDER=src.SORT_ORDER, tgt.USE_YN=src.USE_YN, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (MENU_ID, PARENT_MENU_ID, MENU_NAME, MENU_PATH, SORT_ORDER, USE_YN, created_by, updated_by) VALUES (src.MENU_ID, src.PARENT_MENU_ID, src.MENU_NAME, src.MENU_PATH, src.SORT_ORDER, src.USE_YN, src.created_by, src.updated_by);
MERGE INTO ADM_MENU tgt
USING (SELECT 'TRANSACTION_META' AS MENU_ID, NULL AS PARENT_MENU_ID, '거래 메타' AS MENU_NAME, '/adm#transactions' AS MENU_PATH, 25 AS SORT_ORDER, 'Y' AS USE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.MENU_ID=src.MENU_ID)
WHEN MATCHED THEN UPDATE SET tgt.PARENT_MENU_ID=src.PARENT_MENU_ID, tgt.MENU_NAME=src.MENU_NAME, tgt.MENU_PATH=src.MENU_PATH, tgt.SORT_ORDER=src.SORT_ORDER, tgt.USE_YN=src.USE_YN, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (MENU_ID, PARENT_MENU_ID, MENU_NAME, MENU_PATH, SORT_ORDER, USE_YN, created_by, updated_by) VALUES (src.MENU_ID, src.PARENT_MENU_ID, src.MENU_NAME, src.MENU_PATH, src.SORT_ORDER, src.USE_YN, src.created_by, src.updated_by);
MERGE INTO ADM_MENU tgt
USING (SELECT 'AUDIT_LOG' AS MENU_ID, NULL AS PARENT_MENU_ID, '감사 로그' AS MENU_NAME, '/adm#audit-logs' AS MENU_PATH, 30 AS SORT_ORDER, 'Y' AS USE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.MENU_ID=src.MENU_ID)
WHEN MATCHED THEN UPDATE SET tgt.PARENT_MENU_ID=src.PARENT_MENU_ID, tgt.MENU_NAME=src.MENU_NAME, tgt.MENU_PATH=src.MENU_PATH, tgt.SORT_ORDER=src.SORT_ORDER, tgt.USE_YN=src.USE_YN, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (MENU_ID, PARENT_MENU_ID, MENU_NAME, MENU_PATH, SORT_ORDER, USE_YN, created_by, updated_by) VALUES (src.MENU_ID, src.PARENT_MENU_ID, src.MENU_NAME, src.MENU_PATH, src.SORT_ORDER, src.USE_YN, src.created_by, src.updated_by);
MERGE INTO ADM_MENU tgt
USING (SELECT 'BATCH' AS MENU_ID, NULL AS PARENT_MENU_ID, '배치 관제' AS MENU_NAME, '/adm#batch' AS MENU_PATH, 50 AS SORT_ORDER, 'Y' AS USE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.MENU_ID=src.MENU_ID)
WHEN MATCHED THEN UPDATE SET tgt.PARENT_MENU_ID=src.PARENT_MENU_ID, tgt.MENU_NAME=src.MENU_NAME, tgt.MENU_PATH=src.MENU_PATH, tgt.SORT_ORDER=src.SORT_ORDER, tgt.USE_YN=src.USE_YN, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (MENU_ID, PARENT_MENU_ID, MENU_NAME, MENU_PATH, SORT_ORDER, USE_YN, created_by, updated_by) VALUES (src.MENU_ID, src.PARENT_MENU_ID, src.MENU_NAME, src.MENU_PATH, src.SORT_ORDER, src.USE_YN, src.created_by, src.updated_by);
MERGE INTO ADM_MENU tgt
USING (SELECT 'RELIABILITY' AS MENU_ID, NULL AS PARENT_MENU_ID, '신뢰성 처리 관제' AS MENU_NAME, '/adm#reliability' AS MENU_PATH, 52 AS SORT_ORDER, 'Y' AS USE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.MENU_ID=src.MENU_ID)
WHEN MATCHED THEN UPDATE SET tgt.PARENT_MENU_ID=src.PARENT_MENU_ID, tgt.MENU_NAME=src.MENU_NAME, tgt.MENU_PATH=src.MENU_PATH, tgt.SORT_ORDER=src.SORT_ORDER, tgt.USE_YN=src.USE_YN, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (MENU_ID, PARENT_MENU_ID, MENU_NAME, MENU_PATH, SORT_ORDER, USE_YN, created_by, updated_by) VALUES (src.MENU_ID, src.PARENT_MENU_ID, src.MENU_NAME, src.MENU_PATH, src.SORT_ORDER, src.USE_YN, src.created_by, src.updated_by);
MERGE INTO ADM_MENU tgt
USING (SELECT 'NOTIFICATION' AS MENU_ID, NULL AS PARENT_MENU_ID, '알림 관리' AS MENU_NAME, '/adm#notifications' AS MENU_PATH, 55 AS SORT_ORDER, 'Y' AS USE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.MENU_ID=src.MENU_ID)
WHEN MATCHED THEN UPDATE SET tgt.PARENT_MENU_ID=src.PARENT_MENU_ID, tgt.MENU_NAME=src.MENU_NAME, tgt.MENU_PATH=src.MENU_PATH, tgt.SORT_ORDER=src.SORT_ORDER, tgt.USE_YN=src.USE_YN, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (MENU_ID, PARENT_MENU_ID, MENU_NAME, MENU_PATH, SORT_ORDER, USE_YN, created_by, updated_by) VALUES (src.MENU_ID, src.PARENT_MENU_ID, src.MENU_NAME, src.MENU_PATH, src.SORT_ORDER, src.USE_YN, src.created_by, src.updated_by);
MERGE INTO ADM_MENU tgt
USING (SELECT 'DOWNLOAD' AS MENU_ID, NULL AS PARENT_MENU_ID, '다운로드 감사' AS MENU_NAME, '/adm#downloads' AS MENU_PATH, 58 AS SORT_ORDER, 'Y' AS USE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.MENU_ID=src.MENU_ID)
WHEN MATCHED THEN UPDATE SET tgt.PARENT_MENU_ID=src.PARENT_MENU_ID, tgt.MENU_NAME=src.MENU_NAME, tgt.MENU_PATH=src.MENU_PATH, tgt.SORT_ORDER=src.SORT_ORDER, tgt.USE_YN=src.USE_YN, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (MENU_ID, PARENT_MENU_ID, MENU_NAME, MENU_PATH, SORT_ORDER, USE_YN, created_by, updated_by) VALUES (src.MENU_ID, src.PARENT_MENU_ID, src.MENU_NAME, src.MENU_PATH, src.SORT_ORDER, src.USE_YN, src.created_by, src.updated_by);
MERGE INTO ADM_MENU tgt
USING (SELECT 'CACHE' AS MENU_ID, NULL AS PARENT_MENU_ID, '캐시 관리' AS MENU_NAME, '/adm#cache' AS MENU_PATH, 60 AS SORT_ORDER, 'Y' AS USE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.MENU_ID=src.MENU_ID)
WHEN MATCHED THEN UPDATE SET tgt.PARENT_MENU_ID=src.PARENT_MENU_ID, tgt.MENU_NAME=src.MENU_NAME, tgt.MENU_PATH=src.MENU_PATH, tgt.SORT_ORDER=src.SORT_ORDER, tgt.USE_YN=src.USE_YN, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (MENU_ID, PARENT_MENU_ID, MENU_NAME, MENU_PATH, SORT_ORDER, USE_YN, created_by, updated_by) VALUES (src.MENU_ID, src.PARENT_MENU_ID, src.MENU_NAME, src.MENU_PATH, src.SORT_ORDER, src.USE_YN, src.created_by, src.updated_by);
MERGE INTO ADM_MENU tgt
USING (SELECT 'FILE_JOB' AS MENU_ID, NULL AS PARENT_MENU_ID, '대량파일 Job' AS MENU_NAME, '/adm#file-jobs' AS MENU_PATH, 61 AS SORT_ORDER, 'Y' AS USE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.MENU_ID=src.MENU_ID)
WHEN MATCHED THEN UPDATE SET tgt.PARENT_MENU_ID=src.PARENT_MENU_ID, tgt.MENU_NAME=src.MENU_NAME, tgt.MENU_PATH=src.MENU_PATH, tgt.SORT_ORDER=src.SORT_ORDER, tgt.USE_YN=src.USE_YN, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (MENU_ID, PARENT_MENU_ID, MENU_NAME, MENU_PATH, SORT_ORDER, USE_YN, created_by, updated_by) VALUES (src.MENU_ID, src.PARENT_MENU_ID, src.MENU_NAME, src.MENU_PATH, src.SORT_ORDER, src.USE_YN, src.created_by, src.updated_by);
MERGE INTO ADM_MENU tgt
USING (SELECT 'MESSAGE' AS MENU_ID, NULL AS PARENT_MENU_ID, '메시지 관리' AS MENU_NAME, '/adm#messages' AS MENU_PATH, 70 AS SORT_ORDER, 'Y' AS USE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.MENU_ID=src.MENU_ID)
WHEN MATCHED THEN UPDATE SET tgt.PARENT_MENU_ID=src.PARENT_MENU_ID, tgt.MENU_NAME=src.MENU_NAME, tgt.MENU_PATH=src.MENU_PATH, tgt.SORT_ORDER=src.SORT_ORDER, tgt.USE_YN=src.USE_YN, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (MENU_ID, PARENT_MENU_ID, MENU_NAME, MENU_PATH, SORT_ORDER, USE_YN, created_by, updated_by) VALUES (src.MENU_ID, src.PARENT_MENU_ID, src.MENU_NAME, src.MENU_PATH, src.SORT_ORDER, src.USE_YN, src.created_by, src.updated_by);
MERGE INTO ADM_MENU tgt
USING (SELECT 'CODE' AS MENU_ID, NULL AS PARENT_MENU_ID, '코드 관리' AS MENU_NAME, '/adm#codes' AS MENU_PATH, 80 AS SORT_ORDER, 'Y' AS USE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.MENU_ID=src.MENU_ID)
WHEN MATCHED THEN UPDATE SET tgt.PARENT_MENU_ID=src.PARENT_MENU_ID, tgt.MENU_NAME=src.MENU_NAME, tgt.MENU_PATH=src.MENU_PATH, tgt.SORT_ORDER=src.SORT_ORDER, tgt.USE_YN=src.USE_YN, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (MENU_ID, PARENT_MENU_ID, MENU_NAME, MENU_PATH, SORT_ORDER, USE_YN, created_by, updated_by) VALUES (src.MENU_ID, src.PARENT_MENU_ID, src.MENU_NAME, src.MENU_PATH, src.SORT_ORDER, src.USE_YN, src.created_by, src.updated_by);
MERGE INTO ADM_MENU tgt
USING (SELECT 'RESPONSE_CODE' AS MENU_ID, NULL AS PARENT_MENU_ID, '응답코드 관리' AS MENU_NAME, '/adm#response-codes' AS MENU_PATH, 90 AS SORT_ORDER, 'Y' AS USE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.MENU_ID=src.MENU_ID)
WHEN MATCHED THEN UPDATE SET tgt.PARENT_MENU_ID=src.PARENT_MENU_ID, tgt.MENU_NAME=src.MENU_NAME, tgt.MENU_PATH=src.MENU_PATH, tgt.SORT_ORDER=src.SORT_ORDER, tgt.USE_YN=src.USE_YN, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (MENU_ID, PARENT_MENU_ID, MENU_NAME, MENU_PATH, SORT_ORDER, USE_YN, created_by, updated_by) VALUES (src.MENU_ID, src.PARENT_MENU_ID, src.MENU_NAME, src.MENU_PATH, src.SORT_ORDER, src.USE_YN, src.created_by, src.updated_by);
MERGE INTO ADM_MENU tgt
USING (SELECT 'CONFIG' AS MENU_ID, NULL AS PARENT_MENU_ID, '설정 관리' AS MENU_NAME, '/adm#configs' AS MENU_PATH, 100 AS SORT_ORDER, 'Y' AS USE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.MENU_ID=src.MENU_ID)
WHEN MATCHED THEN UPDATE SET tgt.PARENT_MENU_ID=src.PARENT_MENU_ID, tgt.MENU_NAME=src.MENU_NAME, tgt.MENU_PATH=src.MENU_PATH, tgt.SORT_ORDER=src.SORT_ORDER, tgt.USE_YN=src.USE_YN, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (MENU_ID, PARENT_MENU_ID, MENU_NAME, MENU_PATH, SORT_ORDER, USE_YN, created_by, updated_by) VALUES (src.MENU_ID, src.PARENT_MENU_ID, src.MENU_NAME, src.MENU_PATH, src.SORT_ORDER, src.USE_YN, src.created_by, src.updated_by);
MERGE INTO ADM_MENU tgt
USING (SELECT 'DYNAMIC_LOG' AS MENU_ID, NULL AS PARENT_MENU_ID, '동적 로그 레벨' AS MENU_NAME, '/adm#log-level' AS MENU_PATH, 110 AS SORT_ORDER, 'Y' AS USE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.MENU_ID=src.MENU_ID)
WHEN MATCHED THEN UPDATE SET tgt.PARENT_MENU_ID=src.PARENT_MENU_ID, tgt.MENU_NAME=src.MENU_NAME, tgt.MENU_PATH=src.MENU_PATH, tgt.SORT_ORDER=src.SORT_ORDER, tgt.USE_YN=src.USE_YN, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (MENU_ID, PARENT_MENU_ID, MENU_NAME, MENU_PATH, SORT_ORDER, USE_YN, created_by, updated_by) VALUES (src.MENU_ID, src.PARENT_MENU_ID, src.MENU_NAME, src.MENU_PATH, src.SORT_ORDER, src.USE_YN, src.created_by, src.updated_by);
MERGE INTO ADM_MENU tgt
USING (SELECT 'LOG_POLICY' AS MENU_ID, NULL AS PARENT_MENU_ID, '로그 정책' AS MENU_NAME, '/adm#log-policies' AS MENU_PATH, 115 AS SORT_ORDER, 'Y' AS USE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.MENU_ID=src.MENU_ID)
WHEN MATCHED THEN UPDATE SET tgt.PARENT_MENU_ID=src.PARENT_MENU_ID, tgt.MENU_NAME=src.MENU_NAME, tgt.MENU_PATH=src.MENU_PATH, tgt.SORT_ORDER=src.SORT_ORDER, tgt.USE_YN=src.USE_YN, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (MENU_ID, PARENT_MENU_ID, MENU_NAME, MENU_PATH, SORT_ORDER, USE_YN, created_by, updated_by) VALUES (src.MENU_ID, src.PARENT_MENU_ID, src.MENU_NAME, src.MENU_PATH, src.SORT_ORDER, src.USE_YN, src.created_by, src.updated_by);
MERGE INTO ADM_MENU tgt
USING (SELECT 'PASSWORD' AS MENU_ID, NULL AS PARENT_MENU_ID, '비밀번호 관리' AS MENU_NAME, '/adm#password' AS MENU_PATH, 120 AS SORT_ORDER, 'Y' AS USE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.MENU_ID=src.MENU_ID)
WHEN MATCHED THEN UPDATE SET tgt.PARENT_MENU_ID=src.PARENT_MENU_ID, tgt.MENU_NAME=src.MENU_NAME, tgt.MENU_PATH=src.MENU_PATH, tgt.SORT_ORDER=src.SORT_ORDER, tgt.USE_YN=src.USE_YN, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (MENU_ID, PARENT_MENU_ID, MENU_NAME, MENU_PATH, SORT_ORDER, USE_YN, created_by, updated_by) VALUES (src.MENU_ID, src.PARENT_MENU_ID, src.MENU_NAME, src.MENU_PATH, src.SORT_ORDER, src.USE_YN, src.created_by, src.updated_by);
MERGE INTO ADM_MENU tgt
USING (SELECT 'SECURITY' AS MENU_ID, NULL AS PARENT_MENU_ID, '보안 운영' AS MENU_NAME, '/adm#security' AS MENU_PATH, 130 AS SORT_ORDER, 'Y' AS USE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.MENU_ID=src.MENU_ID)
WHEN MATCHED THEN UPDATE SET tgt.PARENT_MENU_ID=src.PARENT_MENU_ID, tgt.MENU_NAME=src.MENU_NAME, tgt.MENU_PATH=src.MENU_PATH, tgt.SORT_ORDER=src.SORT_ORDER, tgt.USE_YN=src.USE_YN, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (MENU_ID, PARENT_MENU_ID, MENU_NAME, MENU_PATH, SORT_ORDER, USE_YN, created_by, updated_by) VALUES (src.MENU_ID, src.PARENT_MENU_ID, src.MENU_NAME, src.MENU_PATH, src.SORT_ORDER, src.USE_YN, src.created_by, src.updated_by);
MERGE INTO ADM_MENU tgt
USING (SELECT 'PERMISSION' AS MENU_ID, NULL AS PARENT_MENU_ID, '권한 관리' AS MENU_NAME, '/adm#permissions' AS MENU_PATH, 140 AS SORT_ORDER, 'Y' AS USE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.MENU_ID=src.MENU_ID)
WHEN MATCHED THEN UPDATE SET tgt.PARENT_MENU_ID=src.PARENT_MENU_ID, tgt.MENU_NAME=src.MENU_NAME, tgt.MENU_PATH=src.MENU_PATH, tgt.SORT_ORDER=src.SORT_ORDER, tgt.USE_YN=src.USE_YN, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (MENU_ID, PARENT_MENU_ID, MENU_NAME, MENU_PATH, SORT_ORDER, USE_YN, created_by, updated_by) VALUES (src.MENU_ID, src.PARENT_MENU_ID, src.MENU_NAME, src.MENU_PATH, src.SORT_ORDER, src.USE_YN, src.created_by, src.updated_by);
MERGE INTO ADM_MENU tgt
USING (SELECT 'SECRET' AS MENU_ID, NULL AS PARENT_MENU_ID, 'Secret / Key 관리' AS MENU_NAME, '/adm#secrets' AS MENU_PATH, 145 AS SORT_ORDER, 'Y' AS USE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.MENU_ID=src.MENU_ID)
WHEN MATCHED THEN UPDATE SET tgt.PARENT_MENU_ID=src.PARENT_MENU_ID, tgt.MENU_NAME=src.MENU_NAME, tgt.MENU_PATH=src.MENU_PATH, tgt.SORT_ORDER=src.SORT_ORDER, tgt.USE_YN=src.USE_YN, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (MENU_ID, PARENT_MENU_ID, MENU_NAME, MENU_PATH, SORT_ORDER, USE_YN, created_by, updated_by) VALUES (src.MENU_ID, src.PARENT_MENU_ID, src.MENU_NAME, src.MENU_PATH, src.SORT_ORDER, src.USE_YN, src.created_by, src.updated_by);
MERGE INTO ADM_MENU tgt
USING (SELECT 'OPERATOR' AS MENU_ID, NULL AS PARENT_MENU_ID, '운영자 관리' AS MENU_NAME, '/adm#operators' AS MENU_PATH, 150 AS SORT_ORDER, 'Y' AS USE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.MENU_ID=src.MENU_ID)
WHEN MATCHED THEN UPDATE SET tgt.PARENT_MENU_ID=src.PARENT_MENU_ID, tgt.MENU_NAME=src.MENU_NAME, tgt.MENU_PATH=src.MENU_PATH, tgt.SORT_ORDER=src.SORT_ORDER, tgt.USE_YN=src.USE_YN, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (MENU_ID, PARENT_MENU_ID, MENU_NAME, MENU_PATH, SORT_ORDER, USE_YN, created_by, updated_by) VALUES (src.MENU_ID, src.PARENT_MENU_ID, src.MENU_NAME, src.MENU_PATH, src.SORT_ORDER, src.USE_YN, src.created_by, src.updated_by);
MERGE INTO ADM_MENU tgt
USING (SELECT 'GATEWAY_DASHBOARD' AS MENU_ID, NULL AS PARENT_MENU_ID, 'Gateway 대시보드' AS MENU_NAME, '/adm#gateway-dashboard' AS MENU_PATH, 300 AS SORT_ORDER, 'Y' AS USE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.MENU_ID=src.MENU_ID)
WHEN MATCHED THEN UPDATE SET tgt.PARENT_MENU_ID=src.PARENT_MENU_ID, tgt.MENU_NAME=src.MENU_NAME, tgt.MENU_PATH=src.MENU_PATH, tgt.SORT_ORDER=src.SORT_ORDER, tgt.USE_YN=src.USE_YN, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (MENU_ID, PARENT_MENU_ID, MENU_NAME, MENU_PATH, SORT_ORDER, USE_YN, created_by, updated_by) VALUES (src.MENU_ID, src.PARENT_MENU_ID, src.MENU_NAME, src.MENU_PATH, src.SORT_ORDER, src.USE_YN, src.created_by, src.updated_by);
MERGE INTO ADM_MENU tgt
USING (SELECT 'GATEWAY_SERVERS' AS MENU_ID, 'GATEWAY_DASHBOARD' AS PARENT_MENU_ID, 'Gateway 연동 서버' AS MENU_NAME, '/adm#gateway-servers' AS MENU_PATH, 301 AS SORT_ORDER, 'Y' AS USE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.MENU_ID=src.MENU_ID)
WHEN MATCHED THEN UPDATE SET tgt.PARENT_MENU_ID=src.PARENT_MENU_ID, tgt.MENU_NAME=src.MENU_NAME, tgt.MENU_PATH=src.MENU_PATH, tgt.SORT_ORDER=src.SORT_ORDER, tgt.USE_YN=src.USE_YN, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (MENU_ID, PARENT_MENU_ID, MENU_NAME, MENU_PATH, SORT_ORDER, USE_YN, created_by, updated_by) VALUES (src.MENU_ID, src.PARENT_MENU_ID, src.MENU_NAME, src.MENU_PATH, src.SORT_ORDER, src.USE_YN, src.created_by, src.updated_by);
MERGE INTO ADM_MENU tgt
USING (SELECT 'GATEWAY_GROUPS' AS MENU_ID, 'GATEWAY_DASHBOARD' AS PARENT_MENU_ID, 'Gateway 서버 그룹' AS MENU_NAME, '/adm#gateway-groups' AS MENU_PATH, 302 AS SORT_ORDER, 'Y' AS USE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.MENU_ID=src.MENU_ID)
WHEN MATCHED THEN UPDATE SET tgt.PARENT_MENU_ID=src.PARENT_MENU_ID, tgt.MENU_NAME=src.MENU_NAME, tgt.MENU_PATH=src.MENU_PATH, tgt.SORT_ORDER=src.SORT_ORDER, tgt.USE_YN=src.USE_YN, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (MENU_ID, PARENT_MENU_ID, MENU_NAME, MENU_PATH, SORT_ORDER, USE_YN, created_by, updated_by) VALUES (src.MENU_ID, src.PARENT_MENU_ID, src.MENU_NAME, src.MENU_PATH, src.SORT_ORDER, src.USE_YN, src.created_by, src.updated_by);
MERGE INTO ADM_MENU tgt
USING (SELECT 'GATEWAY_ROUTES' AS MENU_ID, 'GATEWAY_DASHBOARD' AS PARENT_MENU_ID, 'Gateway 경로·라우팅' AS MENU_NAME, '/adm#gateway-routes' AS MENU_PATH, 303 AS SORT_ORDER, 'Y' AS USE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.MENU_ID=src.MENU_ID)
WHEN MATCHED THEN UPDATE SET tgt.PARENT_MENU_ID=src.PARENT_MENU_ID, tgt.MENU_NAME=src.MENU_NAME, tgt.MENU_PATH=src.MENU_PATH, tgt.SORT_ORDER=src.SORT_ORDER, tgt.USE_YN=src.USE_YN, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (MENU_ID, PARENT_MENU_ID, MENU_NAME, MENU_PATH, SORT_ORDER, USE_YN, created_by, updated_by) VALUES (src.MENU_ID, src.PARENT_MENU_ID, src.MENU_NAME, src.MENU_PATH, src.SORT_ORDER, src.USE_YN, src.created_by, src.updated_by);
MERGE INTO ADM_MENU tgt
USING (SELECT 'GATEWAY_SECURITY' AS MENU_ID, 'GATEWAY_DASHBOARD' AS PARENT_MENU_ID, 'Gateway 보안·제한' AS MENU_NAME, '/adm#gateway-security' AS MENU_PATH, 304 AS SORT_ORDER, 'Y' AS USE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.MENU_ID=src.MENU_ID)
WHEN MATCHED THEN UPDATE SET tgt.PARENT_MENU_ID=src.PARENT_MENU_ID, tgt.MENU_NAME=src.MENU_NAME, tgt.MENU_PATH=src.MENU_PATH, tgt.SORT_ORDER=src.SORT_ORDER, tgt.USE_YN=src.USE_YN, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (MENU_ID, PARENT_MENU_ID, MENU_NAME, MENU_PATH, SORT_ORDER, USE_YN, created_by, updated_by) VALUES (src.MENU_ID, src.PARENT_MENU_ID, src.MENU_NAME, src.MENU_PATH, src.SORT_ORDER, src.USE_YN, src.created_by, src.updated_by);
MERGE INTO ADM_MENU tgt
USING (SELECT 'GATEWAY_HEALTH' AS MENU_ID, 'GATEWAY_DASHBOARD' AS PARENT_MENU_ID, 'Gateway Health·연결시험' AS MENU_NAME, '/adm#gateway-health' AS MENU_PATH, 305 AS SORT_ORDER, 'Y' AS USE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.MENU_ID=src.MENU_ID)
WHEN MATCHED THEN UPDATE SET tgt.PARENT_MENU_ID=src.PARENT_MENU_ID, tgt.MENU_NAME=src.MENU_NAME, tgt.MENU_PATH=src.MENU_PATH, tgt.SORT_ORDER=src.SORT_ORDER, tgt.USE_YN=src.USE_YN, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (MENU_ID, PARENT_MENU_ID, MENU_NAME, MENU_PATH, SORT_ORDER, USE_YN, created_by, updated_by) VALUES (src.MENU_ID, src.PARENT_MENU_ID, src.MENU_NAME, src.MENU_PATH, src.SORT_ORDER, src.USE_YN, src.created_by, src.updated_by);
MERGE INTO ADM_MENU tgt
USING (SELECT 'GATEWAY_TRANSACTIONS' AS MENU_ID, 'GATEWAY_DASHBOARD' AS PARENT_MENU_ID, 'Gateway 거래 조회' AS MENU_NAME, '/adm#gateway-transactions' AS MENU_PATH, 306 AS SORT_ORDER, 'Y' AS USE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.MENU_ID=src.MENU_ID)
WHEN MATCHED THEN UPDATE SET tgt.PARENT_MENU_ID=src.PARENT_MENU_ID, tgt.MENU_NAME=src.MENU_NAME, tgt.MENU_PATH=src.MENU_PATH, tgt.SORT_ORDER=src.SORT_ORDER, tgt.USE_YN=src.USE_YN, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (MENU_ID, PARENT_MENU_ID, MENU_NAME, MENU_PATH, SORT_ORDER, USE_YN, created_by, updated_by) VALUES (src.MENU_ID, src.PARENT_MENU_ID, src.MENU_NAME, src.MENU_PATH, src.SORT_ORDER, src.USE_YN, src.created_by, src.updated_by);
MERGE INTO ADM_MENU tgt
USING (SELECT 'GATEWAY_LOG_POLICY' AS MENU_ID, 'GATEWAY_DASHBOARD' AS PARENT_MENU_ID, 'Gateway 로그 정책' AS MENU_NAME, '/adm#gateway-log-policies' AS MENU_PATH, 307 AS SORT_ORDER, 'Y' AS USE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.MENU_ID=src.MENU_ID)
WHEN MATCHED THEN UPDATE SET tgt.PARENT_MENU_ID=src.PARENT_MENU_ID, tgt.MENU_NAME=src.MENU_NAME, tgt.MENU_PATH=src.MENU_PATH, tgt.SORT_ORDER=src.SORT_ORDER, tgt.USE_YN=src.USE_YN, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (MENU_ID, PARENT_MENU_ID, MENU_NAME, MENU_PATH, SORT_ORDER, USE_YN, created_by, updated_by) VALUES (src.MENU_ID, src.PARENT_MENU_ID, src.MENU_NAME, src.MENU_PATH, src.SORT_ORDER, src.USE_YN, src.created_by, src.updated_by);
MERGE INTO ADM_MENU tgt
USING (SELECT 'GATEWAY_APPLY_STATUS' AS MENU_ID, 'GATEWAY_DASHBOARD' AS PARENT_MENU_ID, 'Gateway 적용 상태·이력' AS MENU_NAME, '/adm#gateway-apply-status' AS MENU_PATH, 308 AS SORT_ORDER, 'Y' AS USE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.MENU_ID=src.MENU_ID)
WHEN MATCHED THEN UPDATE SET tgt.PARENT_MENU_ID=src.PARENT_MENU_ID, tgt.MENU_NAME=src.MENU_NAME, tgt.MENU_PATH=src.MENU_PATH, tgt.SORT_ORDER=src.SORT_ORDER, tgt.USE_YN=src.USE_YN, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (MENU_ID, PARENT_MENU_ID, MENU_NAME, MENU_PATH, SORT_ORDER, USE_YN, created_by, updated_by) VALUES (src.MENU_ID, src.PARENT_MENU_ID, src.MENU_NAME, src.MENU_PATH, src.SORT_ORDER, src.USE_YN, src.created_by, src.updated_by);
MERGE INTO ADM_MENU tgt
USING (SELECT 'BATCH_OVERVIEW' AS MENU_ID, 'BATCH' AS PARENT_MENU_ID, 'Batch Overview' AS MENU_NAME, '/adm#batch-overview' AS MENU_PATH, 501 AS SORT_ORDER, 'Y' AS USE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.MENU_ID=src.MENU_ID)
WHEN MATCHED THEN UPDATE SET tgt.PARENT_MENU_ID=src.PARENT_MENU_ID, tgt.MENU_NAME=src.MENU_NAME, tgt.MENU_PATH=src.MENU_PATH, tgt.SORT_ORDER=src.SORT_ORDER, tgt.USE_YN=src.USE_YN, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (MENU_ID, PARENT_MENU_ID, MENU_NAME, MENU_PATH, SORT_ORDER, USE_YN, created_by, updated_by) VALUES (src.MENU_ID, src.PARENT_MENU_ID, src.MENU_NAME, src.MENU_PATH, src.SORT_ORDER, src.USE_YN, src.created_by, src.updated_by);
MERGE INTO ADM_MENU tgt
USING (SELECT 'BATCH_RUNTIME' AS MENU_ID, 'BATCH' AS PARENT_MENU_ID, 'Runtime Topology' AS MENU_NAME, '/adm#batch-runtime' AS MENU_PATH, 502 AS SORT_ORDER, 'Y' AS USE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.MENU_ID=src.MENU_ID)
WHEN MATCHED THEN UPDATE SET tgt.PARENT_MENU_ID=src.PARENT_MENU_ID, tgt.MENU_NAME=src.MENU_NAME, tgt.MENU_PATH=src.MENU_PATH, tgt.SORT_ORDER=src.SORT_ORDER, tgt.USE_YN=src.USE_YN, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (MENU_ID, PARENT_MENU_ID, MENU_NAME, MENU_PATH, SORT_ORDER, USE_YN, created_by, updated_by) VALUES (src.MENU_ID, src.PARENT_MENU_ID, src.MENU_NAME, src.MENU_PATH, src.SORT_ORDER, src.USE_YN, src.created_by, src.updated_by);
MERGE INTO ADM_MENU tgt
USING (SELECT 'BATCH_INSTANCES' AS MENU_ID, 'BATCH' AS PARENT_MENU_ID, 'Runtime Instances' AS MENU_NAME, '/adm#batch-instances' AS MENU_PATH, 503 AS SORT_ORDER, 'Y' AS USE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.MENU_ID=src.MENU_ID)
WHEN MATCHED THEN UPDATE SET tgt.PARENT_MENU_ID=src.PARENT_MENU_ID, tgt.MENU_NAME=src.MENU_NAME, tgt.MENU_PATH=src.MENU_PATH, tgt.SORT_ORDER=src.SORT_ORDER, tgt.USE_YN=src.USE_YN, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (MENU_ID, PARENT_MENU_ID, MENU_NAME, MENU_PATH, SORT_ORDER, USE_YN, created_by, updated_by) VALUES (src.MENU_ID, src.PARENT_MENU_ID, src.MENU_NAME, src.MENU_PATH, src.SORT_ORDER, src.USE_YN, src.created_by, src.updated_by);
MERGE INTO ADM_MENU tgt
USING (SELECT 'BATCH_SCHEDULER' AS MENU_ID, 'BATCH' AS PARENT_MENU_ID, 'Scheduler HA' AS MENU_NAME, '/adm#batch-scheduler' AS MENU_PATH, 504 AS SORT_ORDER, 'Y' AS USE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.MENU_ID=src.MENU_ID)
WHEN MATCHED THEN UPDATE SET tgt.PARENT_MENU_ID=src.PARENT_MENU_ID, tgt.MENU_NAME=src.MENU_NAME, tgt.MENU_PATH=src.MENU_PATH, tgt.SORT_ORDER=src.SORT_ORDER, tgt.USE_YN=src.USE_YN, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (MENU_ID, PARENT_MENU_ID, MENU_NAME, MENU_PATH, SORT_ORDER, USE_YN, created_by, updated_by) VALUES (src.MENU_ID, src.PARENT_MENU_ID, src.MENU_NAME, src.MENU_PATH, src.SORT_ORDER, src.USE_YN, src.created_by, src.updated_by);
MERGE INTO ADM_MENU tgt
USING (SELECT 'BATCH_WORKER_POOLS' AS MENU_ID, 'BATCH' AS PARENT_MENU_ID, 'Worker Pools' AS MENU_NAME, '/adm#batch-worker-pools' AS MENU_PATH, 505 AS SORT_ORDER, 'Y' AS USE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.MENU_ID=src.MENU_ID)
WHEN MATCHED THEN UPDATE SET tgt.PARENT_MENU_ID=src.PARENT_MENU_ID, tgt.MENU_NAME=src.MENU_NAME, tgt.MENU_PATH=src.MENU_PATH, tgt.SORT_ORDER=src.SORT_ORDER, tgt.USE_YN=src.USE_YN, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (MENU_ID, PARENT_MENU_ID, MENU_NAME, MENU_PATH, SORT_ORDER, USE_YN, created_by, updated_by) VALUES (src.MENU_ID, src.PARENT_MENU_ID, src.MENU_NAME, src.MENU_PATH, src.SORT_ORDER, src.USE_YN, src.created_by, src.updated_by);
MERGE INTO ADM_MENU tgt
USING (SELECT 'BATCH_CENTER_CUT' AS MENU_ID, 'BATCH' AS PARENT_MENU_ID, 'Center-Cut' AS MENU_NAME, '/adm#batch-center-cut' AS MENU_PATH, 506 AS SORT_ORDER, 'Y' AS USE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.MENU_ID=src.MENU_ID)
WHEN MATCHED THEN UPDATE SET tgt.PARENT_MENU_ID=src.PARENT_MENU_ID, tgt.MENU_NAME=src.MENU_NAME, tgt.MENU_PATH=src.MENU_PATH, tgt.SORT_ORDER=src.SORT_ORDER, tgt.USE_YN=src.USE_YN, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (MENU_ID, PARENT_MENU_ID, MENU_NAME, MENU_PATH, SORT_ORDER, USE_YN, created_by, updated_by) VALUES (src.MENU_ID, src.PARENT_MENU_ID, src.MENU_NAME, src.MENU_PATH, src.SORT_ORDER, src.USE_YN, src.created_by, src.updated_by);
MERGE INTO ADM_MENU tgt
USING (SELECT 'BATCH_AGENTS' AS MENU_ID, 'BATCH' AS PARENT_MENU_ID, 'Host Agents' AS MENU_NAME, '/adm#batch-agents' AS MENU_PATH, 507 AS SORT_ORDER, 'Y' AS USE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.MENU_ID=src.MENU_ID)
WHEN MATCHED THEN UPDATE SET tgt.PARENT_MENU_ID=src.PARENT_MENU_ID, tgt.MENU_NAME=src.MENU_NAME, tgt.MENU_PATH=src.MENU_PATH, tgt.SORT_ORDER=src.SORT_ORDER, tgt.USE_YN=src.USE_YN, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (MENU_ID, PARENT_MENU_ID, MENU_NAME, MENU_PATH, SORT_ORDER, USE_YN, created_by, updated_by) VALUES (src.MENU_ID, src.PARENT_MENU_ID, src.MENU_NAME, src.MENU_PATH, src.SORT_ORDER, src.USE_YN, src.created_by, src.updated_by);
MERGE INTO ADM_MENU tgt
USING (SELECT 'BATCH_JOB_PACKS' AS MENU_ID, 'BATCH' AS PARENT_MENU_ID, 'Job Packs' AS MENU_NAME, '/adm#batch-job-packs' AS MENU_PATH, 508 AS SORT_ORDER, 'Y' AS USE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.MENU_ID=src.MENU_ID)
WHEN MATCHED THEN UPDATE SET tgt.PARENT_MENU_ID=src.PARENT_MENU_ID, tgt.MENU_NAME=src.MENU_NAME, tgt.MENU_PATH=src.MENU_PATH, tgt.SORT_ORDER=src.SORT_ORDER, tgt.USE_YN=src.USE_YN, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (MENU_ID, PARENT_MENU_ID, MENU_NAME, MENU_PATH, SORT_ORDER, USE_YN, created_by, updated_by) VALUES (src.MENU_ID, src.PARENT_MENU_ID, src.MENU_NAME, src.MENU_PATH, src.SORT_ORDER, src.USE_YN, src.created_by, src.updated_by);
MERGE INTO ADM_MENU tgt
USING (SELECT 'BATCH_EXECUTIONS' AS MENU_ID, 'BATCH' AS PARENT_MENU_ID, 'Executions' AS MENU_NAME, '/adm#batch-executions' AS MENU_PATH, 509 AS SORT_ORDER, 'Y' AS USE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.MENU_ID=src.MENU_ID)
WHEN MATCHED THEN UPDATE SET tgt.PARENT_MENU_ID=src.PARENT_MENU_ID, tgt.MENU_NAME=src.MENU_NAME, tgt.MENU_PATH=src.MENU_PATH, tgt.SORT_ORDER=src.SORT_ORDER, tgt.USE_YN=src.USE_YN, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (MENU_ID, PARENT_MENU_ID, MENU_NAME, MENU_PATH, SORT_ORDER, USE_YN, created_by, updated_by) VALUES (src.MENU_ID, src.PARENT_MENU_ID, src.MENU_NAME, src.MENU_PATH, src.SORT_ORDER, src.USE_YN, src.created_by, src.updated_by);
MERGE INTO ADM_MENU tgt
USING (SELECT 'BATCH_DEPLOYMENT' AS MENU_ID, 'BATCH' AS PARENT_MENU_ID, 'Deployment / Rollback' AS MENU_NAME, '/adm#batch-deployment' AS MENU_PATH, 510 AS SORT_ORDER, 'Y' AS USE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.MENU_ID=src.MENU_ID)
WHEN MATCHED THEN UPDATE SET tgt.PARENT_MENU_ID=src.PARENT_MENU_ID, tgt.MENU_NAME=src.MENU_NAME, tgt.MENU_PATH=src.MENU_PATH, tgt.SORT_ORDER=src.SORT_ORDER, tgt.USE_YN=src.USE_YN, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (MENU_ID, PARENT_MENU_ID, MENU_NAME, MENU_PATH, SORT_ORDER, USE_YN, created_by, updated_by) VALUES (src.MENU_ID, src.PARENT_MENU_ID, src.MENU_NAME, src.MENU_PATH, src.SORT_ORDER, src.USE_YN, src.created_by, src.updated_by);
MERGE INTO ADM_MENU tgt
USING (SELECT 'BATCH_RECOVERY' AS MENU_ID, 'BATCH' AS PARENT_MENU_ID, 'Recovery / Unknown' AS MENU_NAME, '/adm#batch-recovery' AS MENU_PATH, 511 AS SORT_ORDER, 'Y' AS USE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.MENU_ID=src.MENU_ID)
WHEN MATCHED THEN UPDATE SET tgt.PARENT_MENU_ID=src.PARENT_MENU_ID, tgt.MENU_NAME=src.MENU_NAME, tgt.MENU_PATH=src.MENU_PATH, tgt.SORT_ORDER=src.SORT_ORDER, tgt.USE_YN=src.USE_YN, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (MENU_ID, PARENT_MENU_ID, MENU_NAME, MENU_PATH, SORT_ORDER, USE_YN, created_by, updated_by) VALUES (src.MENU_ID, src.PARENT_MENU_ID, src.MENU_NAME, src.MENU_PATH, src.SORT_ORDER, src.USE_YN, src.created_by, src.updated_by);
MERGE INTO ADM_MENU tgt
USING (SELECT 'BATCH_LEASES' AS MENU_ID, 'BATCH' AS PARENT_MENU_ID, 'Lease / Fencing' AS MENU_NAME, '/adm#batch-leases' AS MENU_PATH, 512 AS SORT_ORDER, 'Y' AS USE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.MENU_ID=src.MENU_ID)
WHEN MATCHED THEN UPDATE SET tgt.PARENT_MENU_ID=src.PARENT_MENU_ID, tgt.MENU_NAME=src.MENU_NAME, tgt.MENU_PATH=src.MENU_PATH, tgt.SORT_ORDER=src.SORT_ORDER, tgt.USE_YN=src.USE_YN, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (MENU_ID, PARENT_MENU_ID, MENU_NAME, MENU_PATH, SORT_ORDER, USE_YN, created_by, updated_by) VALUES (src.MENU_ID, src.PARENT_MENU_ID, src.MENU_NAME, src.MENU_PATH, src.SORT_ORDER, src.USE_YN, src.created_by, src.updated_by);
MERGE INTO ADM_MENU tgt
USING (SELECT 'BATCH_ALERTS' AS MENU_ID, 'BATCH' AS PARENT_MENU_ID, 'Batch Alerts' AS MENU_NAME, '/adm#batch-alerts' AS MENU_PATH, 513 AS SORT_ORDER, 'Y' AS USE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.MENU_ID=src.MENU_ID)
WHEN MATCHED THEN UPDATE SET tgt.PARENT_MENU_ID=src.PARENT_MENU_ID, tgt.MENU_NAME=src.MENU_NAME, tgt.MENU_PATH=src.MENU_PATH, tgt.SORT_ORDER=src.SORT_ORDER, tgt.USE_YN=src.USE_YN, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (MENU_ID, PARENT_MENU_ID, MENU_NAME, MENU_PATH, SORT_ORDER, USE_YN, created_by, updated_by) VALUES (src.MENU_ID, src.PARENT_MENU_ID, src.MENU_NAME, src.MENU_PATH, src.SORT_ORDER, src.USE_YN, src.created_by, src.updated_by);
MERGE INTO ADM_MENU tgt
USING (SELECT 'BATCH_AUDIT' AS MENU_ID, 'BATCH' AS PARENT_MENU_ID, 'Audit / Evidence' AS MENU_NAME, '/adm#batch-audit' AS MENU_PATH, 514 AS SORT_ORDER, 'Y' AS USE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.MENU_ID=src.MENU_ID)
WHEN MATCHED THEN UPDATE SET tgt.PARENT_MENU_ID=src.PARENT_MENU_ID, tgt.MENU_NAME=src.MENU_NAME, tgt.MENU_PATH=src.MENU_PATH, tgt.SORT_ORDER=src.SORT_ORDER, tgt.USE_YN=src.USE_YN, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (MENU_ID, PARENT_MENU_ID, MENU_NAME, MENU_PATH, SORT_ORDER, USE_YN, created_by, updated_by) VALUES (src.MENU_ID, src.PARENT_MENU_ID, src.MENU_NAME, src.MENU_PATH, src.SORT_ORDER, src.USE_YN, src.created_by, src.updated_by);
MERGE INTO ADM_MENU tgt
USING (SELECT 'APPROVAL' AS MENU_ID, NULL AS PARENT_MENU_ID, '위험조치 승인' AS MENU_NAME, '/adm#approvals' AS MENU_PATH, 524 AS SORT_ORDER, 'Y' AS USE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.MENU_ID=src.MENU_ID)
WHEN MATCHED THEN UPDATE SET tgt.PARENT_MENU_ID=src.PARENT_MENU_ID, tgt.MENU_NAME=src.MENU_NAME, tgt.MENU_PATH=src.MENU_PATH, tgt.SORT_ORDER=src.SORT_ORDER, tgt.USE_YN=src.USE_YN, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (MENU_ID, PARENT_MENU_ID, MENU_NAME, MENU_PATH, SORT_ORDER, USE_YN, created_by, updated_by) VALUES (src.MENU_ID, src.PARENT_MENU_ID, src.MENU_NAME, src.MENU_PATH, src.SORT_ORDER, src.USE_YN, src.created_by, src.updated_by);
MERGE INTO ADM_MENU tgt
USING (SELECT 'BREAK_GLASS' AS MENU_ID, NULL AS PARENT_MENU_ID, 'Break-glass' AS MENU_NAME, '/adm#breakGlass' AS MENU_PATH, 534 AS SORT_ORDER, 'Y' AS USE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.MENU_ID=src.MENU_ID)
WHEN MATCHED THEN UPDATE SET tgt.PARENT_MENU_ID=src.PARENT_MENU_ID, tgt.MENU_NAME=src.MENU_NAME, tgt.MENU_PATH=src.MENU_PATH, tgt.SORT_ORDER=src.SORT_ORDER, tgt.USE_YN=src.USE_YN, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (MENU_ID, PARENT_MENU_ID, MENU_NAME, MENU_PATH, SORT_ORDER, USE_YN, created_by, updated_by) VALUES (src.MENU_ID, src.PARENT_MENU_ID, src.MENU_NAME, src.MENU_PATH, src.SORT_ORDER, src.USE_YN, src.created_by, src.updated_by);
MERGE INTO ADM_MENU tgt
USING (SELECT 'BUSINESS_CALENDAR' AS MENU_ID, NULL AS PARENT_MENU_ID, '영업일 · 휴일' AS MENU_NAME, '/adm#businessCalendar' AS MENU_PATH, 544 AS SORT_ORDER, 'Y' AS USE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.MENU_ID=src.MENU_ID)
WHEN MATCHED THEN UPDATE SET tgt.PARENT_MENU_ID=src.PARENT_MENU_ID, tgt.MENU_NAME=src.MENU_NAME, tgt.MENU_PATH=src.MENU_PATH, tgt.SORT_ORDER=src.SORT_ORDER, tgt.USE_YN=src.USE_YN, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (MENU_ID, PARENT_MENU_ID, MENU_NAME, MENU_PATH, SORT_ORDER, USE_YN, created_by, updated_by) VALUES (src.MENU_ID, src.PARENT_MENU_ID, src.MENU_NAME, src.MENU_PATH, src.SORT_ORDER, src.USE_YN, src.created_by, src.updated_by);
MERGE INTO ADM_MENU tgt
USING (SELECT 'CAPACITY' AS MENU_ID, NULL AS PARENT_MENU_ID, 'Online Runtime Diagnostics' AS MENU_NAME, '/adm#capacity' AS MENU_PATH, 554 AS SORT_ORDER, 'Y' AS USE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.MENU_ID=src.MENU_ID)
WHEN MATCHED THEN UPDATE SET tgt.PARENT_MENU_ID=src.PARENT_MENU_ID, tgt.MENU_NAME=src.MENU_NAME, tgt.MENU_PATH=src.MENU_PATH, tgt.SORT_ORDER=src.SORT_ORDER, tgt.USE_YN=src.USE_YN, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (MENU_ID, PARENT_MENU_ID, MENU_NAME, MENU_PATH, SORT_ORDER, USE_YN, created_by, updated_by) VALUES (src.MENU_ID, src.PARENT_MENU_ID, src.MENU_NAME, src.MENU_PATH, src.SORT_ORDER, src.USE_YN, src.created_by, src.updated_by);
MERGE INTO ADM_MENU tgt
USING (SELECT 'FEATURE_FLAG' AS MENU_ID, NULL AS PARENT_MENU_ID, 'Feature Flag' AS MENU_NAME, '/adm#featureFlags' AS MENU_PATH, 564 AS SORT_ORDER, 'Y' AS USE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.MENU_ID=src.MENU_ID)
WHEN MATCHED THEN UPDATE SET tgt.PARENT_MENU_ID=src.PARENT_MENU_ID, tgt.MENU_NAME=src.MENU_NAME, tgt.MENU_PATH=src.MENU_PATH, tgt.SORT_ORDER=src.SORT_ORDER, tgt.USE_YN=src.USE_YN, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (MENU_ID, PARENT_MENU_ID, MENU_NAME, MENU_PATH, SORT_ORDER, USE_YN, created_by, updated_by) VALUES (src.MENU_ID, src.PARENT_MENU_ID, src.MENU_NAME, src.MENU_PATH, src.SORT_ORDER, src.USE_YN, src.created_by, src.updated_by);
MERGE INTO ADM_MENU tgt
USING (SELECT 'TOPOLOGY' AS MENU_ID, NULL AS PARENT_MENU_ID, '서비스 토폴로지' AS MENU_NAME, '/adm#topology' AS MENU_PATH, 574 AS SORT_ORDER, 'Y' AS USE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.MENU_ID=src.MENU_ID)
WHEN MATCHED THEN UPDATE SET tgt.PARENT_MENU_ID=src.PARENT_MENU_ID, tgt.MENU_NAME=src.MENU_NAME, tgt.MENU_PATH=src.MENU_PATH, tgt.SORT_ORDER=src.SORT_ORDER, tgt.USE_YN=src.USE_YN, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (MENU_ID, PARENT_MENU_ID, MENU_NAME, MENU_PATH, SORT_ORDER, USE_YN, created_by, updated_by) VALUES (src.MENU_ID, src.PARENT_MENU_ID, src.MENU_NAME, src.MENU_PATH, src.SORT_ORDER, src.USE_YN, src.created_by, src.updated_by);
MERGE INTO ADM_MENU tgt
USING (SELECT 'INCIDENT' AS MENU_ID, NULL AS PARENT_MENU_ID, 'Error·Unknown Result' AS MENU_NAME, '/adm#incidents' AS MENU_PATH, 584 AS SORT_ORDER, 'Y' AS USE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.MENU_ID=src.MENU_ID)
WHEN MATCHED THEN UPDATE SET tgt.PARENT_MENU_ID=src.PARENT_MENU_ID, tgt.MENU_NAME=src.MENU_NAME, tgt.MENU_PATH=src.MENU_PATH, tgt.SORT_ORDER=src.SORT_ORDER, tgt.USE_YN=src.USE_YN, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (MENU_ID, PARENT_MENU_ID, MENU_NAME, MENU_PATH, SORT_ORDER, USE_YN, created_by, updated_by) VALUES (src.MENU_ID, src.PARENT_MENU_ID, src.MENU_NAME, src.MENU_PATH, src.SORT_ORDER, src.USE_YN, src.created_by, src.updated_by);
MERGE INTO ADM_MENU tgt
USING (SELECT 'INTEGRATION_CLOSURE' AS MENU_ID, NULL AS PARENT_MENU_ID, '통합 운영 정정 승인' AS MENU_NAME, '/adm#integrationClosure' AS MENU_PATH, 594 AS SORT_ORDER, 'Y' AS USE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.MENU_ID=src.MENU_ID)
WHEN MATCHED THEN UPDATE SET tgt.PARENT_MENU_ID=src.PARENT_MENU_ID, tgt.MENU_NAME=src.MENU_NAME, tgt.MENU_PATH=src.MENU_PATH, tgt.SORT_ORDER=src.SORT_ORDER, tgt.USE_YN=src.USE_YN, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (MENU_ID, PARENT_MENU_ID, MENU_NAME, MENU_PATH, SORT_ORDER, USE_YN, created_by, updated_by) VALUES (src.MENU_ID, src.PARENT_MENU_ID, src.MENU_NAME, src.MENU_PATH, src.SORT_ORDER, src.USE_YN, src.created_by, src.updated_by);
MERGE INTO ADM_MENU tgt
USING (SELECT 'MAINTENANCE' AS MENU_ID, NULL AS PARENT_MENU_ID, '점검·Drain' AS MENU_NAME, '/adm#maintenance' AS MENU_PATH, 604 AS SORT_ORDER, 'Y' AS USE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.MENU_ID=src.MENU_ID)
WHEN MATCHED THEN UPDATE SET tgt.PARENT_MENU_ID=src.PARENT_MENU_ID, tgt.MENU_NAME=src.MENU_NAME, tgt.MENU_PATH=src.MENU_PATH, tgt.SORT_ORDER=src.SORT_ORDER, tgt.USE_YN=src.USE_YN, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (MENU_ID, PARENT_MENU_ID, MENU_NAME, MENU_PATH, SORT_ORDER, USE_YN, created_by, updated_by) VALUES (src.MENU_ID, src.PARENT_MENU_ID, src.MENU_NAME, src.MENU_PATH, src.SORT_ORDER, src.USE_YN, src.created_by, src.updated_by);
MERGE INTO ADM_MENU tgt
USING (SELECT 'OPENAPI_OPERATIONS' AS MENU_ID, NULL AS PARENT_MENU_ID, 'OpenAPI 운영' AS MENU_NAME, '/adm#openApiOperations' AS MENU_PATH, 614 AS SORT_ORDER, 'Y' AS USE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.MENU_ID=src.MENU_ID)
WHEN MATCHED THEN UPDATE SET tgt.PARENT_MENU_ID=src.PARENT_MENU_ID, tgt.MENU_NAME=src.MENU_NAME, tgt.MENU_PATH=src.MENU_PATH, tgt.SORT_ORDER=src.SORT_ORDER, tgt.USE_YN=src.USE_YN, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (MENU_ID, PARENT_MENU_ID, MENU_NAME, MENU_PATH, SORT_ORDER, USE_YN, created_by, updated_by) VALUES (src.MENU_ID, src.PARENT_MENU_ID, src.MENU_NAME, src.MENU_PATH, src.SORT_ORDER, src.USE_YN, src.created_by, src.updated_by);
MERGE INTO ADM_MENU tgt
USING (SELECT 'OPS_GOVERNANCE' AS MENU_ID, NULL AS PARENT_MENU_ID, '운영 정책·SLO' AS MENU_NAME, '/adm#operations-governance' AS MENU_PATH, 624 AS SORT_ORDER, 'Y' AS USE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.MENU_ID=src.MENU_ID)
WHEN MATCHED THEN UPDATE SET tgt.PARENT_MENU_ID=src.PARENT_MENU_ID, tgt.MENU_NAME=src.MENU_NAME, tgt.MENU_PATH=src.MENU_PATH, tgt.SORT_ORDER=src.SORT_ORDER, tgt.USE_YN=src.USE_YN, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (MENU_ID, PARENT_MENU_ID, MENU_NAME, MENU_PATH, SORT_ORDER, USE_YN, created_by, updated_by) VALUES (src.MENU_ID, src.PARENT_MENU_ID, src.MENU_NAME, src.MENU_PATH, src.SORT_ORDER, src.USE_YN, src.created_by, src.updated_by);
MERGE INTO ADM_MENU tgt
USING (SELECT 'RECOVERY_CENTER' AS MENU_ID, NULL AS PARENT_MENU_ID, '복구 센터' AS MENU_NAME, '/adm#recoveryCenter' AS MENU_PATH, 634 AS SORT_ORDER, 'Y' AS USE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.MENU_ID=src.MENU_ID)
WHEN MATCHED THEN UPDATE SET tgt.PARENT_MENU_ID=src.PARENT_MENU_ID, tgt.MENU_NAME=src.MENU_NAME, tgt.MENU_PATH=src.MENU_PATH, tgt.SORT_ORDER=src.SORT_ORDER, tgt.USE_YN=src.USE_YN, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (MENU_ID, PARENT_MENU_ID, MENU_NAME, MENU_PATH, SORT_ORDER, USE_YN, created_by, updated_by) VALUES (src.MENU_ID, src.PARENT_MENU_ID, src.MENU_NAME, src.MENU_PATH, src.SORT_ORDER, src.USE_YN, src.created_by, src.updated_by);
MERGE INTO ADM_MENU tgt
USING (SELECT 'RESILIENCE_POLICY' AS MENU_ID, NULL AS PARENT_MENU_ID, 'Resilience 정책' AS MENU_NAME, '/adm#resiliencePolicies' AS MENU_PATH, 644 AS SORT_ORDER, 'Y' AS USE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.MENU_ID=src.MENU_ID)
WHEN MATCHED THEN UPDATE SET tgt.PARENT_MENU_ID=src.PARENT_MENU_ID, tgt.MENU_NAME=src.MENU_NAME, tgt.MENU_PATH=src.MENU_PATH, tgt.SORT_ORDER=src.SORT_ORDER, tgt.USE_YN=src.USE_YN, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (MENU_ID, PARENT_MENU_ID, MENU_NAME, MENU_PATH, SORT_ORDER, USE_YN, created_by, updated_by) VALUES (src.MENU_ID, src.PARENT_MENU_ID, src.MENU_NAME, src.MENU_PATH, src.SORT_ORDER, src.USE_YN, src.created_by, src.updated_by);
MERGE INTO ADM_MENU tgt
USING (SELECT 'RUNTIME_CONTROL' AS MENU_ID, NULL AS PARENT_MENU_ID, 'Deployment·Promotion·Rollback' AS MENU_NAME, '/adm#runtimeControl' AS MENU_PATH, 654 AS SORT_ORDER, 'Y' AS USE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.MENU_ID=src.MENU_ID)
WHEN MATCHED THEN UPDATE SET tgt.PARENT_MENU_ID=src.PARENT_MENU_ID, tgt.MENU_NAME=src.MENU_NAME, tgt.MENU_PATH=src.MENU_PATH, tgt.SORT_ORDER=src.SORT_ORDER, tgt.USE_YN=src.USE_YN, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (MENU_ID, PARENT_MENU_ID, MENU_NAME, MENU_PATH, SORT_ORDER, USE_YN, created_by, updated_by) VALUES (src.MENU_ID, src.PARENT_MENU_ID, src.MENU_NAME, src.MENU_PATH, src.SORT_ORDER, src.USE_YN, src.created_by, src.updated_by);
MERGE INTO ADM_MENU tgt
USING (SELECT 'SERVICE_REGISTRY' AS MENU_ID, NULL AS PARENT_MENU_ID, '서비스 레지스트리' AS MENU_NAME, '/adm#serviceRegistry' AS MENU_PATH, 664 AS SORT_ORDER, 'Y' AS USE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.MENU_ID=src.MENU_ID)
WHEN MATCHED THEN UPDATE SET tgt.PARENT_MENU_ID=src.PARENT_MENU_ID, tgt.MENU_NAME=src.MENU_NAME, tgt.MENU_PATH=src.MENU_PATH, tgt.SORT_ORDER=src.SORT_ORDER, tgt.USE_YN=src.USE_YN, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (MENU_ID, PARENT_MENU_ID, MENU_NAME, MENU_PATH, SORT_ORDER, USE_YN, created_by, updated_by) VALUES (src.MENU_ID, src.PARENT_MENU_ID, src.MENU_NAME, src.MENU_PATH, src.SORT_ORDER, src.USE_YN, src.created_by, src.updated_by);
MERGE INTO ADM_MENU tgt
USING (SELECT 'WORKER' AS MENU_ID, NULL AS PARENT_MENU_ID, 'Agent / Worker' AS MENU_NAME, '/adm#workers' AS MENU_PATH, 674 AS SORT_ORDER, 'Y' AS USE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.MENU_ID=src.MENU_ID)
WHEN MATCHED THEN UPDATE SET tgt.PARENT_MENU_ID=src.PARENT_MENU_ID, tgt.MENU_NAME=src.MENU_NAME, tgt.MENU_PATH=src.MENU_PATH, tgt.SORT_ORDER=src.SORT_ORDER, tgt.USE_YN=src.USE_YN, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (MENU_ID, PARENT_MENU_ID, MENU_NAME, MENU_PATH, SORT_ORDER, USE_YN, created_by, updated_by) VALUES (src.MENU_ID, src.PARENT_MENU_ID, src.MENU_NAME, src.MENU_PATH, src.SORT_ORDER, src.USE_YN, src.created_by, src.updated_by);
MERGE INTO ADM_BUTTON tgt
USING (SELECT 'CAPABILITY_FLEET_READ' AS BUTTON_ID, 'CAPABILITY_FLEET' AS MENU_ID, 'READ' AS ACTION_CODE, 'CPF Capability 조회' AS BUTTON_NAME, 'GET' AS HTTP_METHOD, '/adm/api/capability-management/**' AS API_PATTERN, 10 AS SORT_ORDER, 'Y' AS USE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.BUTTON_ID=src.BUTTON_ID)
WHEN MATCHED THEN UPDATE SET tgt.MENU_ID=src.MENU_ID, tgt.ACTION_CODE=src.ACTION_CODE, tgt.BUTTON_NAME=src.BUTTON_NAME, tgt.HTTP_METHOD=src.HTTP_METHOD, tgt.API_PATTERN=src.API_PATTERN, tgt.SORT_ORDER=src.SORT_ORDER, tgt.USE_YN=src.USE_YN, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (BUTTON_ID, MENU_ID, ACTION_CODE, BUTTON_NAME, HTTP_METHOD, API_PATTERN, SORT_ORDER, USE_YN, created_by, updated_by) VALUES (src.BUTTON_ID, src.MENU_ID, src.ACTION_CODE, src.BUTTON_NAME, src.HTTP_METHOD, src.API_PATTERN, src.SORT_ORDER, src.USE_YN, src.created_by, src.updated_by);
MERGE INTO ADM_BUTTON tgt
USING (SELECT 'LOG_LIST_READ' AS BUTTON_ID, 'LOG_LIST' AS MENU_ID, 'READ' AS ACTION_CODE, '조회' AS BUTTON_NAME, 'GET' AS HTTP_METHOD, '/adm/api/logs/**' AS API_PATTERN, 10 AS SORT_ORDER, 'Y' AS USE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.BUTTON_ID=src.BUTTON_ID)
WHEN MATCHED THEN UPDATE SET tgt.MENU_ID=src.MENU_ID, tgt.ACTION_CODE=src.ACTION_CODE, tgt.BUTTON_NAME=src.BUTTON_NAME, tgt.HTTP_METHOD=src.HTTP_METHOD, tgt.API_PATTERN=src.API_PATTERN, tgt.SORT_ORDER=src.SORT_ORDER, tgt.USE_YN=src.USE_YN, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (BUTTON_ID, MENU_ID, ACTION_CODE, BUTTON_NAME, HTTP_METHOD, API_PATTERN, SORT_ORDER, USE_YN, created_by, updated_by) VALUES (src.BUTTON_ID, src.MENU_ID, src.ACTION_CODE, src.BUTTON_NAME, src.HTTP_METHOD, src.API_PATTERN, src.SORT_ORDER, src.USE_YN, src.created_by, src.updated_by);
MERGE INTO ADM_BUTTON tgt
USING (SELECT 'LOG_LIST_DETAIL' AS BUTTON_ID, 'LOG_LIST' AS MENU_ID, 'DETAIL' AS ACTION_CODE, '상세 조회' AS BUTTON_NAME, 'GET' AS HTTP_METHOD, '/adm/api/logs/**' AS API_PATTERN, 20 AS SORT_ORDER, 'Y' AS USE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.BUTTON_ID=src.BUTTON_ID)
WHEN MATCHED THEN UPDATE SET tgt.MENU_ID=src.MENU_ID, tgt.ACTION_CODE=src.ACTION_CODE, tgt.BUTTON_NAME=src.BUTTON_NAME, tgt.HTTP_METHOD=src.HTTP_METHOD, tgt.API_PATTERN=src.API_PATTERN, tgt.SORT_ORDER=src.SORT_ORDER, tgt.USE_YN=src.USE_YN, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (BUTTON_ID, MENU_ID, ACTION_CODE, BUTTON_NAME, HTTP_METHOD, API_PATTERN, SORT_ORDER, USE_YN, created_by, updated_by) VALUES (src.BUTTON_ID, src.MENU_ID, src.ACTION_CODE, src.BUTTON_NAME, src.HTTP_METHOD, src.API_PATTERN, src.SORT_ORDER, src.USE_YN, src.created_by, src.updated_by);
MERGE INTO ADM_BUTTON tgt
USING (SELECT 'LOG_LIST_DOWNLOAD' AS BUTTON_ID, 'LOG_LIST' AS MENU_ID, 'DOWNLOAD' AS ACTION_CODE, '다운로드' AS BUTTON_NAME, 'GET' AS HTTP_METHOD, '/adm/api/logs/**' AS API_PATTERN, 30 AS SORT_ORDER, 'Y' AS USE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.BUTTON_ID=src.BUTTON_ID)
WHEN MATCHED THEN UPDATE SET tgt.MENU_ID=src.MENU_ID, tgt.ACTION_CODE=src.ACTION_CODE, tgt.BUTTON_NAME=src.BUTTON_NAME, tgt.HTTP_METHOD=src.HTTP_METHOD, tgt.API_PATTERN=src.API_PATTERN, tgt.SORT_ORDER=src.SORT_ORDER, tgt.USE_YN=src.USE_YN, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (BUTTON_ID, MENU_ID, ACTION_CODE, BUTTON_NAME, HTTP_METHOD, API_PATTERN, SORT_ORDER, USE_YN, created_by, updated_by) VALUES (src.BUTTON_ID, src.MENU_ID, src.ACTION_CODE, src.BUTTON_NAME, src.HTTP_METHOD, src.API_PATTERN, src.SORT_ORDER, src.USE_YN, src.created_by, src.updated_by);
MERGE INTO ADM_BUTTON tgt
USING (SELECT 'STANDARD_EXECUTION_READ' AS BUTTON_ID, 'STANDARD_EXECUTION' AS MENU_ID, 'READ' AS ACTION_CODE, '표준 실행 조회' AS BUTTON_NAME, 'GET' AS HTTP_METHOD, '/adm/api/standard-executions/**' AS API_PATTERN, 10 AS SORT_ORDER, 'Y' AS USE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.BUTTON_ID=src.BUTTON_ID)
WHEN MATCHED THEN UPDATE SET tgt.MENU_ID=src.MENU_ID, tgt.ACTION_CODE=src.ACTION_CODE, tgt.BUTTON_NAME=src.BUTTON_NAME, tgt.HTTP_METHOD=src.HTTP_METHOD, tgt.API_PATTERN=src.API_PATTERN, tgt.SORT_ORDER=src.SORT_ORDER, tgt.USE_YN=src.USE_YN, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (BUTTON_ID, MENU_ID, ACTION_CODE, BUTTON_NAME, HTTP_METHOD, API_PATTERN, SORT_ORDER, USE_YN, created_by, updated_by) VALUES (src.BUTTON_ID, src.MENU_ID, src.ACTION_CODE, src.BUTTON_NAME, src.HTTP_METHOD, src.API_PATTERN, src.SORT_ORDER, src.USE_YN, src.created_by, src.updated_by);
MERGE INTO ADM_BUTTON tgt
USING (SELECT 'CHANNEL_POLICY_READ' AS BUTTON_ID, 'CHANNEL_POLICY' AS MENU_ID, 'READ' AS ACTION_CODE, '채널 정책 조회' AS BUTTON_NAME, 'GET' AS HTTP_METHOD, '/adm/api/channels/**' AS API_PATTERN, 10 AS SORT_ORDER, 'Y' AS USE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.BUTTON_ID=src.BUTTON_ID)
WHEN MATCHED THEN UPDATE SET tgt.MENU_ID=src.MENU_ID, tgt.ACTION_CODE=src.ACTION_CODE, tgt.BUTTON_NAME=src.BUTTON_NAME, tgt.HTTP_METHOD=src.HTTP_METHOD, tgt.API_PATTERN=src.API_PATTERN, tgt.SORT_ORDER=src.SORT_ORDER, tgt.USE_YN=src.USE_YN, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (BUTTON_ID, MENU_ID, ACTION_CODE, BUTTON_NAME, HTTP_METHOD, API_PATTERN, SORT_ORDER, USE_YN, created_by, updated_by) VALUES (src.BUTTON_ID, src.MENU_ID, src.ACTION_CODE, src.BUTTON_NAME, src.HTTP_METHOD, src.API_PATTERN, src.SORT_ORDER, src.USE_YN, src.created_by, src.updated_by);
MERGE INTO ADM_BUTTON tgt
USING (SELECT 'CHANNEL_POLICY_WRITE' AS BUTTON_ID, 'CHANNEL_POLICY' AS MENU_ID, 'WRITE' AS ACTION_CODE, '채널·거래 정책 변경' AS BUTTON_NAME, 'PUT' AS HTTP_METHOD, '/adm/api/channels/**' AS API_PATTERN, 20 AS SORT_ORDER, 'Y' AS USE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.BUTTON_ID=src.BUTTON_ID)
WHEN MATCHED THEN UPDATE SET tgt.MENU_ID=src.MENU_ID, tgt.ACTION_CODE=src.ACTION_CODE, tgt.BUTTON_NAME=src.BUTTON_NAME, tgt.HTTP_METHOD=src.HTTP_METHOD, tgt.API_PATTERN=src.API_PATTERN, tgt.SORT_ORDER=src.SORT_ORDER, tgt.USE_YN=src.USE_YN, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (BUTTON_ID, MENU_ID, ACTION_CODE, BUTTON_NAME, HTTP_METHOD, API_PATTERN, SORT_ORDER, USE_YN, created_by, updated_by) VALUES (src.BUTTON_ID, src.MENU_ID, src.ACTION_CODE, src.BUTTON_NAME, src.HTTP_METHOD, src.API_PATTERN, src.SORT_ORDER, src.USE_YN, src.created_by, src.updated_by);
MERGE INTO ADM_BUTTON tgt
USING (SELECT 'CHANNEL_POLICY_REFRESH' AS BUTTON_ID, 'CHANNEL_POLICY' AS MENU_ID, 'REFRESH' AS ACTION_CODE, '채널 정책 스냅샷 갱신' AS BUTTON_NAME, 'POST' AS HTTP_METHOD, '/adm/api/channels/refresh' AS API_PATTERN, 30 AS SORT_ORDER, 'Y' AS USE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.BUTTON_ID=src.BUTTON_ID)
WHEN MATCHED THEN UPDATE SET tgt.MENU_ID=src.MENU_ID, tgt.ACTION_CODE=src.ACTION_CODE, tgt.BUTTON_NAME=src.BUTTON_NAME, tgt.HTTP_METHOD=src.HTTP_METHOD, tgt.API_PATTERN=src.API_PATTERN, tgt.SORT_ORDER=src.SORT_ORDER, tgt.USE_YN=src.USE_YN, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (BUTTON_ID, MENU_ID, ACTION_CODE, BUTTON_NAME, HTTP_METHOD, API_PATTERN, SORT_ORDER, USE_YN, created_by, updated_by) VALUES (src.BUTTON_ID, src.MENU_ID, src.ACTION_CODE, src.BUTTON_NAME, src.HTTP_METHOD, src.API_PATTERN, src.SORT_ORDER, src.USE_YN, src.created_by, src.updated_by);
MERGE INTO ADM_BUTTON tgt
USING (SELECT 'CHANNEL_POLICY_IMPORT' AS BUTTON_ID, 'CHANNEL_POLICY' AS MENU_ID, 'IMPORT' AS ACTION_CODE, '채널 정책 패키지 반입' AS BUTTON_NAME, 'POST' AS HTTP_METHOD, '/adm/api/channels/package/import' AS API_PATTERN, 40 AS SORT_ORDER, 'Y' AS USE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.BUTTON_ID=src.BUTTON_ID)
WHEN MATCHED THEN UPDATE SET tgt.MENU_ID=src.MENU_ID, tgt.ACTION_CODE=src.ACTION_CODE, tgt.BUTTON_NAME=src.BUTTON_NAME, tgt.HTTP_METHOD=src.HTTP_METHOD, tgt.API_PATTERN=src.API_PATTERN, tgt.SORT_ORDER=src.SORT_ORDER, tgt.USE_YN=src.USE_YN, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (BUTTON_ID, MENU_ID, ACTION_CODE, BUTTON_NAME, HTTP_METHOD, API_PATTERN, SORT_ORDER, USE_YN, created_by, updated_by) VALUES (src.BUTTON_ID, src.MENU_ID, src.ACTION_CODE, src.BUTTON_NAME, src.HTTP_METHOD, src.API_PATTERN, src.SORT_ORDER, src.USE_YN, src.created_by, src.updated_by);
MERGE INTO ADM_BUTTON tgt
USING (SELECT 'REMOTE_LOG_READ' AS BUTTON_ID, 'REMOTE_LOG' AS MENU_ID, 'READ' AS ACTION_CODE, '로그 아티팩트 조회' AS BUTTON_NAME, 'GET' AS HTTP_METHOD, '/adm/api/remote-logs/**' AS API_PATTERN, 10 AS SORT_ORDER, 'Y' AS USE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.BUTTON_ID=src.BUTTON_ID)
WHEN MATCHED THEN UPDATE SET tgt.MENU_ID=src.MENU_ID, tgt.ACTION_CODE=src.ACTION_CODE, tgt.BUTTON_NAME=src.BUTTON_NAME, tgt.HTTP_METHOD=src.HTTP_METHOD, tgt.API_PATTERN=src.API_PATTERN, tgt.SORT_ORDER=src.SORT_ORDER, tgt.USE_YN=src.USE_YN, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (BUTTON_ID, MENU_ID, ACTION_CODE, BUTTON_NAME, HTTP_METHOD, API_PATTERN, SORT_ORDER, USE_YN, created_by, updated_by) VALUES (src.BUTTON_ID, src.MENU_ID, src.ACTION_CODE, src.BUTTON_NAME, src.HTTP_METHOD, src.API_PATTERN, src.SORT_ORDER, src.USE_YN, src.created_by, src.updated_by);
MERGE INTO ADM_BUTTON tgt
USING (SELECT 'REMOTE_LOG_DOWNLOAD' AS BUTTON_ID, 'REMOTE_LOG' AS MENU_ID, 'DOWNLOAD' AS ACTION_CODE, '로그 아티팩트 다운로드' AS BUTTON_NAME, 'GET' AS HTTP_METHOD, '/adm/api/remote-logs/*/download' AS API_PATTERN, 20 AS SORT_ORDER, 'Y' AS USE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.BUTTON_ID=src.BUTTON_ID)
WHEN MATCHED THEN UPDATE SET tgt.MENU_ID=src.MENU_ID, tgt.ACTION_CODE=src.ACTION_CODE, tgt.BUTTON_NAME=src.BUTTON_NAME, tgt.HTTP_METHOD=src.HTTP_METHOD, tgt.API_PATTERN=src.API_PATTERN, tgt.SORT_ORDER=src.SORT_ORDER, tgt.USE_YN=src.USE_YN, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (BUTTON_ID, MENU_ID, ACTION_CODE, BUTTON_NAME, HTTP_METHOD, API_PATTERN, SORT_ORDER, USE_YN, created_by, updated_by) VALUES (src.BUTTON_ID, src.MENU_ID, src.ACTION_CODE, src.BUTTON_NAME, src.HTTP_METHOD, src.API_PATTERN, src.SORT_ORDER, src.USE_YN, src.created_by, src.updated_by);
MERGE INTO ADM_BUTTON tgt
USING (SELECT 'REMOTE_LOG_BUNDLE_DOWNLOAD' AS BUTTON_ID, 'REMOTE_LOG' AS MENU_ID, 'BUNDLE_DOWNLOAD' AS ACTION_CODE, '동기 로그 ZIP 다운로드' AS BUTTON_NAME, 'POST' AS HTTP_METHOD, '/adm/api/remote-logs/bundles' AS API_PATTERN, 30 AS SORT_ORDER, 'Y' AS USE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.BUTTON_ID=src.BUTTON_ID)
WHEN MATCHED THEN UPDATE SET tgt.MENU_ID=src.MENU_ID, tgt.ACTION_CODE=src.ACTION_CODE, tgt.BUTTON_NAME=src.BUTTON_NAME, tgt.HTTP_METHOD=src.HTTP_METHOD, tgt.API_PATTERN=src.API_PATTERN, tgt.SORT_ORDER=src.SORT_ORDER, tgt.USE_YN=src.USE_YN, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (BUTTON_ID, MENU_ID, ACTION_CODE, BUTTON_NAME, HTTP_METHOD, API_PATTERN, SORT_ORDER, USE_YN, created_by, updated_by) VALUES (src.BUTTON_ID, src.MENU_ID, src.ACTION_CODE, src.BUTTON_NAME, src.HTTP_METHOD, src.API_PATTERN, src.SORT_ORDER, src.USE_YN, src.created_by, src.updated_by);
MERGE INTO ADM_BUTTON tgt
USING (SELECT 'REMOTE_LOG_BUNDLE_CREATE' AS BUTTON_ID, 'REMOTE_LOG' AS MENU_ID, 'CREATE' AS ACTION_CODE, '비동기 로그 ZIP 작업 등록' AS BUTTON_NAME, 'POST' AS HTTP_METHOD, '/adm/api/remote-logs/bundle-jobs' AS API_PATTERN, 40 AS SORT_ORDER, 'Y' AS USE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.BUTTON_ID=src.BUTTON_ID)
WHEN MATCHED THEN UPDATE SET tgt.MENU_ID=src.MENU_ID, tgt.ACTION_CODE=src.ACTION_CODE, tgt.BUTTON_NAME=src.BUTTON_NAME, tgt.HTTP_METHOD=src.HTTP_METHOD, tgt.API_PATTERN=src.API_PATTERN, tgt.SORT_ORDER=src.SORT_ORDER, tgt.USE_YN=src.USE_YN, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (BUTTON_ID, MENU_ID, ACTION_CODE, BUTTON_NAME, HTTP_METHOD, API_PATTERN, SORT_ORDER, USE_YN, created_by, updated_by) VALUES (src.BUTTON_ID, src.MENU_ID, src.ACTION_CODE, src.BUTTON_NAME, src.HTTP_METHOD, src.API_PATTERN, src.SORT_ORDER, src.USE_YN, src.created_by, src.updated_by);
MERGE INTO ADM_BUTTON tgt
USING (SELECT 'REMOTE_LOG_BUNDLE_TOKEN' AS BUTTON_ID, 'REMOTE_LOG' AS MENU_ID, 'ISSUE' AS ACTION_CODE, '로그 ZIP 다운로드 token 발급' AS BUTTON_NAME, 'POST' AS HTTP_METHOD, '/adm/api/remote-logs/bundle-jobs/*/download-tokens' AS API_PATTERN, 50 AS SORT_ORDER, 'Y' AS USE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.BUTTON_ID=src.BUTTON_ID)
WHEN MATCHED THEN UPDATE SET tgt.MENU_ID=src.MENU_ID, tgt.ACTION_CODE=src.ACTION_CODE, tgt.BUTTON_NAME=src.BUTTON_NAME, tgt.HTTP_METHOD=src.HTTP_METHOD, tgt.API_PATTERN=src.API_PATTERN, tgt.SORT_ORDER=src.SORT_ORDER, tgt.USE_YN=src.USE_YN, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (BUTTON_ID, MENU_ID, ACTION_CODE, BUTTON_NAME, HTTP_METHOD, API_PATTERN, SORT_ORDER, USE_YN, created_by, updated_by) VALUES (src.BUTTON_ID, src.MENU_ID, src.ACTION_CODE, src.BUTTON_NAME, src.HTTP_METHOD, src.API_PATTERN, src.SORT_ORDER, src.USE_YN, src.created_by, src.updated_by);
MERGE INTO ADM_BUTTON tgt
USING (SELECT 'REMOTE_LOG_JOB_DOWNLOAD' AS BUTTON_ID, 'REMOTE_LOG' AS MENU_ID, 'JOB_DOWNLOAD' AS ACTION_CODE, '비동기 로그 ZIP 다운로드' AS BUTTON_NAME, 'GET' AS HTTP_METHOD, '/adm/api/remote-logs/bundle-jobs/*/download' AS API_PATTERN, 60 AS SORT_ORDER, 'Y' AS USE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.BUTTON_ID=src.BUTTON_ID)
WHEN MATCHED THEN UPDATE SET tgt.MENU_ID=src.MENU_ID, tgt.ACTION_CODE=src.ACTION_CODE, tgt.BUTTON_NAME=src.BUTTON_NAME, tgt.HTTP_METHOD=src.HTTP_METHOD, tgt.API_PATTERN=src.API_PATTERN, tgt.SORT_ORDER=src.SORT_ORDER, tgt.USE_YN=src.USE_YN, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (BUTTON_ID, MENU_ID, ACTION_CODE, BUTTON_NAME, HTTP_METHOD, API_PATTERN, SORT_ORDER, USE_YN, created_by, updated_by) VALUES (src.BUTTON_ID, src.MENU_ID, src.ACTION_CODE, src.BUTTON_NAME, src.HTTP_METHOD, src.API_PATTERN, src.SORT_ORDER, src.USE_YN, src.created_by, src.updated_by);
MERGE INTO ADM_BUTTON tgt
USING (SELECT 'TRANSACTION_META_READ' AS BUTTON_ID, 'TRANSACTION_META' AS MENU_ID, 'READ' AS ACTION_CODE, '거래 메타 조회' AS BUTTON_NAME, 'GET' AS HTTP_METHOD, '/adm/api/transactions/**' AS API_PATTERN, 10 AS SORT_ORDER, 'Y' AS USE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.BUTTON_ID=src.BUTTON_ID)
WHEN MATCHED THEN UPDATE SET tgt.MENU_ID=src.MENU_ID, tgt.ACTION_CODE=src.ACTION_CODE, tgt.BUTTON_NAME=src.BUTTON_NAME, tgt.HTTP_METHOD=src.HTTP_METHOD, tgt.API_PATTERN=src.API_PATTERN, tgt.SORT_ORDER=src.SORT_ORDER, tgt.USE_YN=src.USE_YN, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (BUTTON_ID, MENU_ID, ACTION_CODE, BUTTON_NAME, HTTP_METHOD, API_PATTERN, SORT_ORDER, USE_YN, created_by, updated_by) VALUES (src.BUTTON_ID, src.MENU_ID, src.ACTION_CODE, src.BUTTON_NAME, src.HTTP_METHOD, src.API_PATTERN, src.SORT_ORDER, src.USE_YN, src.created_by, src.updated_by);
MERGE INTO ADM_BUTTON tgt
USING (SELECT 'TRANSACTION_META_WRITE' AS BUTTON_ID, 'TRANSACTION_META' AS MENU_ID, 'WRITE' AS ACTION_CODE, '거래 메타 비활성화' AS BUTTON_NAME, 'POST' AS HTTP_METHOD, '/adm/api/transactions/*/inactive' AS API_PATTERN, 30 AS SORT_ORDER, 'Y' AS USE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.BUTTON_ID=src.BUTTON_ID)
WHEN MATCHED THEN UPDATE SET tgt.MENU_ID=src.MENU_ID, tgt.ACTION_CODE=src.ACTION_CODE, tgt.BUTTON_NAME=src.BUTTON_NAME, tgt.HTTP_METHOD=src.HTTP_METHOD, tgt.API_PATTERN=src.API_PATTERN, tgt.SORT_ORDER=src.SORT_ORDER, tgt.USE_YN=src.USE_YN, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (BUTTON_ID, MENU_ID, ACTION_CODE, BUTTON_NAME, HTTP_METHOD, API_PATTERN, SORT_ORDER, USE_YN, created_by, updated_by) VALUES (src.BUTTON_ID, src.MENU_ID, src.ACTION_CODE, src.BUTTON_NAME, src.HTTP_METHOD, src.API_PATTERN, src.SORT_ORDER, src.USE_YN, src.created_by, src.updated_by);
MERGE INTO ADM_BUTTON tgt
USING (SELECT 'AUDIT_LOG_READ' AS BUTTON_ID, 'AUDIT_LOG' AS MENU_ID, 'READ' AS ACTION_CODE, '조회' AS BUTTON_NAME, 'GET' AS HTTP_METHOD, '/adm/api/audit-logs/**' AS API_PATTERN, 10 AS SORT_ORDER, 'Y' AS USE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.BUTTON_ID=src.BUTTON_ID)
WHEN MATCHED THEN UPDATE SET tgt.MENU_ID=src.MENU_ID, tgt.ACTION_CODE=src.ACTION_CODE, tgt.BUTTON_NAME=src.BUTTON_NAME, tgt.HTTP_METHOD=src.HTTP_METHOD, tgt.API_PATTERN=src.API_PATTERN, tgt.SORT_ORDER=src.SORT_ORDER, tgt.USE_YN=src.USE_YN, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (BUTTON_ID, MENU_ID, ACTION_CODE, BUTTON_NAME, HTTP_METHOD, API_PATTERN, SORT_ORDER, USE_YN, created_by, updated_by) VALUES (src.BUTTON_ID, src.MENU_ID, src.ACTION_CODE, src.BUTTON_NAME, src.HTTP_METHOD, src.API_PATTERN, src.SORT_ORDER, src.USE_YN, src.created_by, src.updated_by);
MERGE INTO ADM_BUTTON tgt
USING (SELECT 'BATCH_READ' AS BUTTON_ID, 'BATCH' AS MENU_ID, 'READ' AS ACTION_CODE, '조회' AS BUTTON_NAME, 'GET' AS HTTP_METHOD, '/adm/api/batch/**' AS API_PATTERN, 10 AS SORT_ORDER, 'Y' AS USE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.BUTTON_ID=src.BUTTON_ID)
WHEN MATCHED THEN UPDATE SET tgt.MENU_ID=src.MENU_ID, tgt.ACTION_CODE=src.ACTION_CODE, tgt.BUTTON_NAME=src.BUTTON_NAME, tgt.HTTP_METHOD=src.HTTP_METHOD, tgt.API_PATTERN=src.API_PATTERN, tgt.SORT_ORDER=src.SORT_ORDER, tgt.USE_YN=src.USE_YN, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (BUTTON_ID, MENU_ID, ACTION_CODE, BUTTON_NAME, HTTP_METHOD, API_PATTERN, SORT_ORDER, USE_YN, created_by, updated_by) VALUES (src.BUTTON_ID, src.MENU_ID, src.ACTION_CODE, src.BUTTON_NAME, src.HTTP_METHOD, src.API_PATTERN, src.SORT_ORDER, src.USE_YN, src.created_by, src.updated_by);
MERGE INTO ADM_BUTTON tgt
USING (SELECT 'BATCH_REGISTER' AS BUTTON_ID, 'BATCH' AS MENU_ID, 'REGISTER' AS ACTION_CODE, '배치 등록' AS BUTTON_NAME, 'POST' AS HTTP_METHOD, '/adm/api/batch/jobs' AS API_PATTERN, 20 AS SORT_ORDER, 'Y' AS USE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.BUTTON_ID=src.BUTTON_ID)
WHEN MATCHED THEN UPDATE SET tgt.MENU_ID=src.MENU_ID, tgt.ACTION_CODE=src.ACTION_CODE, tgt.BUTTON_NAME=src.BUTTON_NAME, tgt.HTTP_METHOD=src.HTTP_METHOD, tgt.API_PATTERN=src.API_PATTERN, tgt.SORT_ORDER=src.SORT_ORDER, tgt.USE_YN=src.USE_YN, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (BUTTON_ID, MENU_ID, ACTION_CODE, BUTTON_NAME, HTTP_METHOD, API_PATTERN, SORT_ORDER, USE_YN, created_by, updated_by) VALUES (src.BUTTON_ID, src.MENU_ID, src.ACTION_CODE, src.BUTTON_NAME, src.HTTP_METHOD, src.API_PATTERN, src.SORT_ORDER, src.USE_YN, src.created_by, src.updated_by);
MERGE INTO ADM_BUTTON tgt
USING (SELECT 'BATCH_EXECUTE' AS BUTTON_ID, 'BATCH' AS MENU_ID, 'EXECUTE' AS ACTION_CODE, '수동 실행' AS BUTTON_NAME, 'POST' AS HTTP_METHOD, '/adm/api/batch/*/run' AS API_PATTERN, 30 AS SORT_ORDER, 'Y' AS USE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.BUTTON_ID=src.BUTTON_ID)
WHEN MATCHED THEN UPDATE SET tgt.MENU_ID=src.MENU_ID, tgt.ACTION_CODE=src.ACTION_CODE, tgt.BUTTON_NAME=src.BUTTON_NAME, tgt.HTTP_METHOD=src.HTTP_METHOD, tgt.API_PATTERN=src.API_PATTERN, tgt.SORT_ORDER=src.SORT_ORDER, tgt.USE_YN=src.USE_YN, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (BUTTON_ID, MENU_ID, ACTION_CODE, BUTTON_NAME, HTTP_METHOD, API_PATTERN, SORT_ORDER, USE_YN, created_by, updated_by) VALUES (src.BUTTON_ID, src.MENU_ID, src.ACTION_CODE, src.BUTTON_NAME, src.HTTP_METHOD, src.API_PATTERN, src.SORT_ORDER, src.USE_YN, src.created_by, src.updated_by);
MERGE INTO ADM_BUTTON tgt
USING (SELECT 'BATCH_RETRY' AS BUTTON_ID, 'BATCH' AS MENU_ID, 'RETRY' AS ACTION_CODE, '실패 재수행' AS BUTTON_NAME, 'POST' AS HTTP_METHOD, '/adm/api/batch/executions/*/retry' AS API_PATTERN, 40 AS SORT_ORDER, 'Y' AS USE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.BUTTON_ID=src.BUTTON_ID)
WHEN MATCHED THEN UPDATE SET tgt.MENU_ID=src.MENU_ID, tgt.ACTION_CODE=src.ACTION_CODE, tgt.BUTTON_NAME=src.BUTTON_NAME, tgt.HTTP_METHOD=src.HTTP_METHOD, tgt.API_PATTERN=src.API_PATTERN, tgt.SORT_ORDER=src.SORT_ORDER, tgt.USE_YN=src.USE_YN, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (BUTTON_ID, MENU_ID, ACTION_CODE, BUTTON_NAME, HTTP_METHOD, API_PATTERN, SORT_ORDER, USE_YN, created_by, updated_by) VALUES (src.BUTTON_ID, src.MENU_ID, src.ACTION_CODE, src.BUTTON_NAME, src.HTTP_METHOD, src.API_PATTERN, src.SORT_ORDER, src.USE_YN, src.created_by, src.updated_by);
MERGE INTO ADM_BUTTON tgt
USING (SELECT 'BATCH_STOP' AS BUTTON_ID, 'BATCH' AS MENU_ID, 'STOP' AS ACTION_CODE, '실행 중지' AS BUTTON_NAME, 'POST' AS HTTP_METHOD, '/adm/api/batch/executions/*/stop' AS API_PATTERN, 50 AS SORT_ORDER, 'Y' AS USE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.BUTTON_ID=src.BUTTON_ID)
WHEN MATCHED THEN UPDATE SET tgt.MENU_ID=src.MENU_ID, tgt.ACTION_CODE=src.ACTION_CODE, tgt.BUTTON_NAME=src.BUTTON_NAME, tgt.HTTP_METHOD=src.HTTP_METHOD, tgt.API_PATTERN=src.API_PATTERN, tgt.SORT_ORDER=src.SORT_ORDER, tgt.USE_YN=src.USE_YN, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (BUTTON_ID, MENU_ID, ACTION_CODE, BUTTON_NAME, HTTP_METHOD, API_PATTERN, SORT_ORDER, USE_YN, created_by, updated_by) VALUES (src.BUTTON_ID, src.MENU_ID, src.ACTION_CODE, src.BUTTON_NAME, src.HTTP_METHOD, src.API_PATTERN, src.SORT_ORDER, src.USE_YN, src.created_by, src.updated_by);
MERGE INTO ADM_BUTTON tgt
USING (SELECT 'BATCH_SCHEDULE' AS BUTTON_ID, 'BATCH' AS MENU_ID, 'SCHEDULE' AS ACTION_CODE, '스케줄 변경' AS BUTTON_NAME, 'POST' AS HTTP_METHOD, '/adm/api/batch/schedules/**' AS API_PATTERN, 60 AS SORT_ORDER, 'Y' AS USE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.BUTTON_ID=src.BUTTON_ID)
WHEN MATCHED THEN UPDATE SET tgt.MENU_ID=src.MENU_ID, tgt.ACTION_CODE=src.ACTION_CODE, tgt.BUTTON_NAME=src.BUTTON_NAME, tgt.HTTP_METHOD=src.HTTP_METHOD, tgt.API_PATTERN=src.API_PATTERN, tgt.SORT_ORDER=src.SORT_ORDER, tgt.USE_YN=src.USE_YN, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (BUTTON_ID, MENU_ID, ACTION_CODE, BUTTON_NAME, HTTP_METHOD, API_PATTERN, SORT_ORDER, USE_YN, created_by, updated_by) VALUES (src.BUTTON_ID, src.MENU_ID, src.ACTION_CODE, src.BUTTON_NAME, src.HTTP_METHOD, src.API_PATTERN, src.SORT_ORDER, src.USE_YN, src.created_by, src.updated_by);
MERGE INTO ADM_BUTTON tgt
USING (SELECT 'BATCH_CALENDAR_SAVE' AS BUTTON_ID, 'BATCH' AS MENU_ID, 'CALENDAR_SAVE' AS ACTION_CODE, '영업일 저장' AS BUTTON_NAME, 'POST' AS HTTP_METHOD, '/adm/api/batch/calendar' AS API_PATTERN, 70 AS SORT_ORDER, 'Y' AS USE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.BUTTON_ID=src.BUTTON_ID)
WHEN MATCHED THEN UPDATE SET tgt.MENU_ID=src.MENU_ID, tgt.ACTION_CODE=src.ACTION_CODE, tgt.BUTTON_NAME=src.BUTTON_NAME, tgt.HTTP_METHOD=src.HTTP_METHOD, tgt.API_PATTERN=src.API_PATTERN, tgt.SORT_ORDER=src.SORT_ORDER, tgt.USE_YN=src.USE_YN, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (BUTTON_ID, MENU_ID, ACTION_CODE, BUTTON_NAME, HTTP_METHOD, API_PATTERN, SORT_ORDER, USE_YN, created_by, updated_by) VALUES (src.BUTTON_ID, src.MENU_ID, src.ACTION_CODE, src.BUTTON_NAME, src.HTTP_METHOD, src.API_PATTERN, src.SORT_ORDER, src.USE_YN, src.created_by, src.updated_by);
MERGE INTO ADM_BUTTON tgt
USING (SELECT 'BATCH_SIMULATION' AS BUTTON_ID, 'BATCH' AS MENU_ID, 'SIMULATION' AS ACTION_CODE, '수행 시뮬레이션' AS BUTTON_NAME, 'GET' AS HTTP_METHOD, '/adm/api/batch/schedules/*/simulation' AS API_PATTERN, 80 AS SORT_ORDER, 'Y' AS USE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.BUTTON_ID=src.BUTTON_ID)
WHEN MATCHED THEN UPDATE SET tgt.MENU_ID=src.MENU_ID, tgt.ACTION_CODE=src.ACTION_CODE, tgt.BUTTON_NAME=src.BUTTON_NAME, tgt.HTTP_METHOD=src.HTTP_METHOD, tgt.API_PATTERN=src.API_PATTERN, tgt.SORT_ORDER=src.SORT_ORDER, tgt.USE_YN=src.USE_YN, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (BUTTON_ID, MENU_ID, ACTION_CODE, BUTTON_NAME, HTTP_METHOD, API_PATTERN, SORT_ORDER, USE_YN, created_by, updated_by) VALUES (src.BUTTON_ID, src.MENU_ID, src.ACTION_CODE, src.BUTTON_NAME, src.HTTP_METHOD, src.API_PATTERN, src.SORT_ORDER, src.USE_YN, src.created_by, src.updated_by);
MERGE INTO ADM_BUTTON tgt
USING (SELECT 'BATCH_RELATION_READ' AS BUTTON_ID, 'BATCH' AS MENU_ID, 'RELATION_READ' AS ACTION_CODE, '배치 관계 조회' AS BUTTON_NAME, 'GET' AS HTTP_METHOD, '/adm/api/batch/relations' AS API_PATTERN, 90 AS SORT_ORDER, 'Y' AS USE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.BUTTON_ID=src.BUTTON_ID)
WHEN MATCHED THEN UPDATE SET tgt.MENU_ID=src.MENU_ID, tgt.ACTION_CODE=src.ACTION_CODE, tgt.BUTTON_NAME=src.BUTTON_NAME, tgt.HTTP_METHOD=src.HTTP_METHOD, tgt.API_PATTERN=src.API_PATTERN, tgt.SORT_ORDER=src.SORT_ORDER, tgt.USE_YN=src.USE_YN, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (BUTTON_ID, MENU_ID, ACTION_CODE, BUTTON_NAME, HTTP_METHOD, API_PATTERN, SORT_ORDER, USE_YN, created_by, updated_by) VALUES (src.BUTTON_ID, src.MENU_ID, src.ACTION_CODE, src.BUTTON_NAME, src.HTTP_METHOD, src.API_PATTERN, src.SORT_ORDER, src.USE_YN, src.created_by, src.updated_by);
MERGE INTO ADM_BUTTON tgt
USING (SELECT 'BATCH_TARGET_READ' AS BUTTON_ID, 'BATCH' AS MENU_ID, 'TARGET_READ' AS ACTION_CODE, '수행 대상 조회' AS BUTTON_NAME, 'GET' AS HTTP_METHOD, '/adm/api/batch/execution-targets' AS API_PATTERN, 100 AS SORT_ORDER, 'Y' AS USE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.BUTTON_ID=src.BUTTON_ID)
WHEN MATCHED THEN UPDATE SET tgt.MENU_ID=src.MENU_ID, tgt.ACTION_CODE=src.ACTION_CODE, tgt.BUTTON_NAME=src.BUTTON_NAME, tgt.HTTP_METHOD=src.HTTP_METHOD, tgt.API_PATTERN=src.API_PATTERN, tgt.SORT_ORDER=src.SORT_ORDER, tgt.USE_YN=src.USE_YN, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (BUTTON_ID, MENU_ID, ACTION_CODE, BUTTON_NAME, HTTP_METHOD, API_PATTERN, SORT_ORDER, USE_YN, created_by, updated_by) VALUES (src.BUTTON_ID, src.MENU_ID, src.ACTION_CODE, src.BUTTON_NAME, src.HTTP_METHOD, src.API_PATTERN, src.SORT_ORDER, src.USE_YN, src.created_by, src.updated_by);
MERGE INTO ADM_BUTTON tgt
USING (SELECT 'BATCH_SCHEDULER_RUN' AS BUTTON_ID, 'BATCH' AS MENU_ID, 'SCHEDULER_RUN' AS ACTION_CODE, '스케줄러 1회 실행' AS BUTTON_NAME, 'POST' AS HTTP_METHOD, '/adm/api/batch/scheduler/run-once' AS API_PATTERN, 110 AS SORT_ORDER, 'Y' AS USE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.BUTTON_ID=src.BUTTON_ID)
WHEN MATCHED THEN UPDATE SET tgt.MENU_ID=src.MENU_ID, tgt.ACTION_CODE=src.ACTION_CODE, tgt.BUTTON_NAME=src.BUTTON_NAME, tgt.HTTP_METHOD=src.HTTP_METHOD, tgt.API_PATTERN=src.API_PATTERN, tgt.SORT_ORDER=src.SORT_ORDER, tgt.USE_YN=src.USE_YN, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (BUTTON_ID, MENU_ID, ACTION_CODE, BUTTON_NAME, HTTP_METHOD, API_PATTERN, SORT_ORDER, USE_YN, created_by, updated_by) VALUES (src.BUTTON_ID, src.MENU_ID, src.ACTION_CODE, src.BUTTON_NAME, src.HTTP_METHOD, src.API_PATTERN, src.SORT_ORDER, src.USE_YN, src.created_by, src.updated_by);
MERGE INTO ADM_BUTTON tgt
USING (SELECT 'BATCH_JOB_DETAIL' AS BUTTON_ID, 'BATCH' AS MENU_ID, 'DETAIL' AS ACTION_CODE, 'Job 상세 조회' AS BUTTON_NAME, 'GET' AS HTTP_METHOD, '/adm/api/batch/jobs/*' AS API_PATTERN, 120 AS SORT_ORDER, 'Y' AS USE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.BUTTON_ID=src.BUTTON_ID)
WHEN MATCHED THEN UPDATE SET tgt.MENU_ID=src.MENU_ID, tgt.ACTION_CODE=src.ACTION_CODE, tgt.BUTTON_NAME=src.BUTTON_NAME, tgt.HTTP_METHOD=src.HTTP_METHOD, tgt.API_PATTERN=src.API_PATTERN, tgt.SORT_ORDER=src.SORT_ORDER, tgt.USE_YN=src.USE_YN, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (BUTTON_ID, MENU_ID, ACTION_CODE, BUTTON_NAME, HTTP_METHOD, API_PATTERN, SORT_ORDER, USE_YN, created_by, updated_by) VALUES (src.BUTTON_ID, src.MENU_ID, src.ACTION_CODE, src.BUTTON_NAME, src.HTTP_METHOD, src.API_PATTERN, src.SORT_ORDER, src.USE_YN, src.created_by, src.updated_by);
MERGE INTO ADM_BUTTON tgt
USING (SELECT 'BATCH_STEP_READ' AS BUTTON_ID, 'BATCH' AS MENU_ID, 'STEP_READ' AS ACTION_CODE, 'Step 이력 조회' AS BUTTON_NAME, 'GET' AS HTTP_METHOD, '/adm/api/batch/steps' AS API_PATTERN, 130 AS SORT_ORDER, 'Y' AS USE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.BUTTON_ID=src.BUTTON_ID)
WHEN MATCHED THEN UPDATE SET tgt.MENU_ID=src.MENU_ID, tgt.ACTION_CODE=src.ACTION_CODE, tgt.BUTTON_NAME=src.BUTTON_NAME, tgt.HTTP_METHOD=src.HTTP_METHOD, tgt.API_PATTERN=src.API_PATTERN, tgt.SORT_ORDER=src.SORT_ORDER, tgt.USE_YN=src.USE_YN, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (BUTTON_ID, MENU_ID, ACTION_CODE, BUTTON_NAME, HTTP_METHOD, API_PATTERN, SORT_ORDER, USE_YN, created_by, updated_by) VALUES (src.BUTTON_ID, src.MENU_ID, src.ACTION_CODE, src.BUTTON_NAME, src.HTTP_METHOD, src.API_PATTERN, src.SORT_ORDER, src.USE_YN, src.created_by, src.updated_by);
MERGE INTO ADM_BUTTON tgt
USING (SELECT 'BATCH_WORKER_READ' AS BUTTON_ID, 'BATCH' AS MENU_ID, 'WORKER_READ' AS ACTION_CODE, 'Worker 상태 조회' AS BUTTON_NAME, 'GET' AS HTTP_METHOD, '/adm/api/batch/workers' AS API_PATTERN, 140 AS SORT_ORDER, 'Y' AS USE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.BUTTON_ID=src.BUTTON_ID)
WHEN MATCHED THEN UPDATE SET tgt.MENU_ID=src.MENU_ID, tgt.ACTION_CODE=src.ACTION_CODE, tgt.BUTTON_NAME=src.BUTTON_NAME, tgt.HTTP_METHOD=src.HTTP_METHOD, tgt.API_PATTERN=src.API_PATTERN, tgt.SORT_ORDER=src.SORT_ORDER, tgt.USE_YN=src.USE_YN, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (BUTTON_ID, MENU_ID, ACTION_CODE, BUTTON_NAME, HTTP_METHOD, API_PATTERN, SORT_ORDER, USE_YN, created_by, updated_by) VALUES (src.BUTTON_ID, src.MENU_ID, src.ACTION_CODE, src.BUTTON_NAME, src.HTTP_METHOD, src.API_PATTERN, src.SORT_ORDER, src.USE_YN, src.created_by, src.updated_by);
MERGE INTO ADM_BUTTON tgt
USING (SELECT 'BATCH_LOCK_READ' AS BUTTON_ID, 'BATCH' AS MENU_ID, 'LOCK_READ' AS ACTION_CODE, 'Lock 조회' AS BUTTON_NAME, 'GET' AS HTTP_METHOD, '/adm/api/batch/locks' AS API_PATTERN, 150 AS SORT_ORDER, 'Y' AS USE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.BUTTON_ID=src.BUTTON_ID)
WHEN MATCHED THEN UPDATE SET tgt.MENU_ID=src.MENU_ID, tgt.ACTION_CODE=src.ACTION_CODE, tgt.BUTTON_NAME=src.BUTTON_NAME, tgt.HTTP_METHOD=src.HTTP_METHOD, tgt.API_PATTERN=src.API_PATTERN, tgt.SORT_ORDER=src.SORT_ORDER, tgt.USE_YN=src.USE_YN, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (BUTTON_ID, MENU_ID, ACTION_CODE, BUTTON_NAME, HTTP_METHOD, API_PATTERN, SORT_ORDER, USE_YN, created_by, updated_by) VALUES (src.BUTTON_ID, src.MENU_ID, src.ACTION_CODE, src.BUTTON_NAME, src.HTTP_METHOD, src.API_PATTERN, src.SORT_ORDER, src.USE_YN, src.created_by, src.updated_by);
MERGE INTO ADM_BUTTON tgt
USING (SELECT 'BATCH_LOCK_RELEASE' AS BUTTON_ID, 'BATCH' AS MENU_ID, 'LOCK_RELEASE' AS ACTION_CODE, 'Lock 강제 해제' AS BUTTON_NAME, 'POST' AS HTTP_METHOD, '/adm/api/batch/locks/release' AS API_PATTERN, 160 AS SORT_ORDER, 'Y' AS USE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.BUTTON_ID=src.BUTTON_ID)
WHEN MATCHED THEN UPDATE SET tgt.MENU_ID=src.MENU_ID, tgt.ACTION_CODE=src.ACTION_CODE, tgt.BUTTON_NAME=src.BUTTON_NAME, tgt.HTTP_METHOD=src.HTTP_METHOD, tgt.API_PATTERN=src.API_PATTERN, tgt.SORT_ORDER=src.SORT_ORDER, tgt.USE_YN=src.USE_YN, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (BUTTON_ID, MENU_ID, ACTION_CODE, BUTTON_NAME, HTTP_METHOD, API_PATTERN, SORT_ORDER, USE_YN, created_by, updated_by) VALUES (src.BUTTON_ID, src.MENU_ID, src.ACTION_CODE, src.BUTTON_NAME, src.HTTP_METHOD, src.API_PATTERN, src.SORT_ORDER, src.USE_YN, src.created_by, src.updated_by);
MERGE INTO ADM_BUTTON tgt
USING (SELECT 'BATCH_GHOST_READ' AS BUTTON_ID, 'BATCH' AS MENU_ID, 'GHOST_READ' AS ACTION_CODE, 'Ghost 후보 조회' AS BUTTON_NAME, 'GET' AS HTTP_METHOD, '/adm/api/batch/ghost-candidates' AS API_PATTERN, 170 AS SORT_ORDER, 'Y' AS USE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.BUTTON_ID=src.BUTTON_ID)
WHEN MATCHED THEN UPDATE SET tgt.MENU_ID=src.MENU_ID, tgt.ACTION_CODE=src.ACTION_CODE, tgt.BUTTON_NAME=src.BUTTON_NAME, tgt.HTTP_METHOD=src.HTTP_METHOD, tgt.API_PATTERN=src.API_PATTERN, tgt.SORT_ORDER=src.SORT_ORDER, tgt.USE_YN=src.USE_YN, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (BUTTON_ID, MENU_ID, ACTION_CODE, BUTTON_NAME, HTTP_METHOD, API_PATTERN, SORT_ORDER, USE_YN, created_by, updated_by) VALUES (src.BUTTON_ID, src.MENU_ID, src.ACTION_CODE, src.BUTTON_NAME, src.HTTP_METHOD, src.API_PATTERN, src.SORT_ORDER, src.USE_YN, src.created_by, src.updated_by);
MERGE INTO ADM_BUTTON tgt
USING (SELECT 'BATCH_GHOST_ACTION' AS BUTTON_ID, 'BATCH' AS MENU_ID, 'GHOST_ACTION' AS ACTION_CODE, 'Ghost 조치' AS BUTTON_NAME, 'POST' AS HTTP_METHOD, '/adm/api/batch/ghost-candidates/*/actions' AS API_PATTERN, 180 AS SORT_ORDER, 'Y' AS USE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.BUTTON_ID=src.BUTTON_ID)
WHEN MATCHED THEN UPDATE SET tgt.MENU_ID=src.MENU_ID, tgt.ACTION_CODE=src.ACTION_CODE, tgt.BUTTON_NAME=src.BUTTON_NAME, tgt.HTTP_METHOD=src.HTTP_METHOD, tgt.API_PATTERN=src.API_PATTERN, tgt.SORT_ORDER=src.SORT_ORDER, tgt.USE_YN=src.USE_YN, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (BUTTON_ID, MENU_ID, ACTION_CODE, BUTTON_NAME, HTTP_METHOD, API_PATTERN, SORT_ORDER, USE_YN, created_by, updated_by) VALUES (src.BUTTON_ID, src.MENU_ID, src.ACTION_CODE, src.BUTTON_NAME, src.HTTP_METHOD, src.API_PATTERN, src.SORT_ORDER, src.USE_YN, src.created_by, src.updated_by);
MERGE INTO ADM_BUTTON tgt
USING (SELECT 'BATCH_OPERATION_READ' AS BUTTON_ID, 'BATCH' AS MENU_ID, 'OPERATION_READ' AS ACTION_CODE, '운영 작업 로그 조회' AS BUTTON_NAME, 'GET' AS HTTP_METHOD, '/adm/api/batch/operations' AS API_PATTERN, 190 AS SORT_ORDER, 'Y' AS USE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.BUTTON_ID=src.BUTTON_ID)
WHEN MATCHED THEN UPDATE SET tgt.MENU_ID=src.MENU_ID, tgt.ACTION_CODE=src.ACTION_CODE, tgt.BUTTON_NAME=src.BUTTON_NAME, tgt.HTTP_METHOD=src.HTTP_METHOD, tgt.API_PATTERN=src.API_PATTERN, tgt.SORT_ORDER=src.SORT_ORDER, tgt.USE_YN=src.USE_YN, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (BUTTON_ID, MENU_ID, ACTION_CODE, BUTTON_NAME, HTTP_METHOD, API_PATTERN, SORT_ORDER, USE_YN, created_by, updated_by) VALUES (src.BUTTON_ID, src.MENU_ID, src.ACTION_CODE, src.BUTTON_NAME, src.HTTP_METHOD, src.API_PATTERN, src.SORT_ORDER, src.USE_YN, src.created_by, src.updated_by);
MERGE INTO ADM_BUTTON tgt
USING (SELECT 'RELIABILITY_READ' AS BUTTON_ID, 'RELIABILITY' AS MENU_ID, 'READ' AS ACTION_CODE, '신뢰성 처리 조회' AS BUTTON_NAME, 'GET' AS HTTP_METHOD, '/adm/api/reliability/**' AS API_PATTERN, 10 AS SORT_ORDER, 'Y' AS USE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.BUTTON_ID=src.BUTTON_ID)
WHEN MATCHED THEN UPDATE SET tgt.MENU_ID=src.MENU_ID, tgt.ACTION_CODE=src.ACTION_CODE, tgt.BUTTON_NAME=src.BUTTON_NAME, tgt.HTTP_METHOD=src.HTTP_METHOD, tgt.API_PATTERN=src.API_PATTERN, tgt.SORT_ORDER=src.SORT_ORDER, tgt.USE_YN=src.USE_YN, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (BUTTON_ID, MENU_ID, ACTION_CODE, BUTTON_NAME, HTTP_METHOD, API_PATTERN, SORT_ORDER, USE_YN, created_by, updated_by) VALUES (src.BUTTON_ID, src.MENU_ID, src.ACTION_CODE, src.BUTTON_NAME, src.HTTP_METHOD, src.API_PATTERN, src.SORT_ORDER, src.USE_YN, src.created_by, src.updated_by);
MERGE INTO ADM_BUTTON tgt
USING (SELECT 'RELIABILITY_REPLAY' AS BUTTON_ID, 'RELIABILITY' AS MENU_ID, 'REPLAY' AS ACTION_CODE, 'DLQ 재처리' AS BUTTON_NAME, 'POST' AS HTTP_METHOD, '/adm/api/reliability/broker/dlq/*/replay' AS API_PATTERN, 20 AS SORT_ORDER, 'Y' AS USE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.BUTTON_ID=src.BUTTON_ID)
WHEN MATCHED THEN UPDATE SET tgt.MENU_ID=src.MENU_ID, tgt.ACTION_CODE=src.ACTION_CODE, tgt.BUTTON_NAME=src.BUTTON_NAME, tgt.HTTP_METHOD=src.HTTP_METHOD, tgt.API_PATTERN=src.API_PATTERN, tgt.SORT_ORDER=src.SORT_ORDER, tgt.USE_YN=src.USE_YN, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (BUTTON_ID, MENU_ID, ACTION_CODE, BUTTON_NAME, HTTP_METHOD, API_PATTERN, SORT_ORDER, USE_YN, created_by, updated_by) VALUES (src.BUTTON_ID, src.MENU_ID, src.ACTION_CODE, src.BUTTON_NAME, src.HTTP_METHOD, src.API_PATTERN, src.SORT_ORDER, src.USE_YN, src.created_by, src.updated_by);
MERGE INTO ADM_BUTTON tgt
USING (SELECT 'RELIABILITY_RESOLVE' AS BUTTON_ID, 'RELIABILITY' AS MENU_ID, 'RESOLVE' AS ACTION_CODE, '결과 미확정 수동 처리' AS BUTTON_NAME, 'POST' AS HTTP_METHOD, '/adm/api/reliability/unknown-results/*/resolve' AS API_PATTERN, 30 AS SORT_ORDER, 'Y' AS USE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.BUTTON_ID=src.BUTTON_ID)
WHEN MATCHED THEN UPDATE SET tgt.MENU_ID=src.MENU_ID, tgt.ACTION_CODE=src.ACTION_CODE, tgt.BUTTON_NAME=src.BUTTON_NAME, tgt.HTTP_METHOD=src.HTTP_METHOD, tgt.API_PATTERN=src.API_PATTERN, tgt.SORT_ORDER=src.SORT_ORDER, tgt.USE_YN=src.USE_YN, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (BUTTON_ID, MENU_ID, ACTION_CODE, BUTTON_NAME, HTTP_METHOD, API_PATTERN, SORT_ORDER, USE_YN, created_by, updated_by) VALUES (src.BUTTON_ID, src.MENU_ID, src.ACTION_CODE, src.BUTTON_NAME, src.HTTP_METHOD, src.API_PATTERN, src.SORT_ORDER, src.USE_YN, src.created_by, src.updated_by);
MERGE INTO ADM_BUTTON tgt
USING (SELECT 'RELIABILITY_RECOVERY_RUN' AS BUTTON_ID, 'RELIABILITY' AS MENU_ID, 'RECOVERY_RUN' AS ACTION_CODE, 'DB 거래 로그 복구 실행' AS BUTTON_NAME, 'POST' AS HTTP_METHOD, '/adm/api/reliability/transaction-log-recovery/run' AS API_PATTERN, 40 AS SORT_ORDER, 'Y' AS USE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.BUTTON_ID=src.BUTTON_ID)
WHEN MATCHED THEN UPDATE SET tgt.MENU_ID=src.MENU_ID, tgt.ACTION_CODE=src.ACTION_CODE, tgt.BUTTON_NAME=src.BUTTON_NAME, tgt.HTTP_METHOD=src.HTTP_METHOD, tgt.API_PATTERN=src.API_PATTERN, tgt.SORT_ORDER=src.SORT_ORDER, tgt.USE_YN=src.USE_YN, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (BUTTON_ID, MENU_ID, ACTION_CODE, BUTTON_NAME, HTTP_METHOD, API_PATTERN, SORT_ORDER, USE_YN, created_by, updated_by) VALUES (src.BUTTON_ID, src.MENU_ID, src.ACTION_CODE, src.BUTTON_NAME, src.HTTP_METHOD, src.API_PATTERN, src.SORT_ORDER, src.USE_YN, src.created_by, src.updated_by);
MERGE INTO ADM_BUTTON tgt
USING (SELECT 'NOTIFICATION_READ' AS BUTTON_ID, 'NOTIFICATION' AS MENU_ID, 'READ' AS ACTION_CODE, '알림 조회' AS BUTTON_NAME, 'GET' AS HTTP_METHOD, '/adm/api/notifications/**' AS API_PATTERN, 10 AS SORT_ORDER, 'Y' AS USE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.BUTTON_ID=src.BUTTON_ID)
WHEN MATCHED THEN UPDATE SET tgt.MENU_ID=src.MENU_ID, tgt.ACTION_CODE=src.ACTION_CODE, tgt.BUTTON_NAME=src.BUTTON_NAME, tgt.HTTP_METHOD=src.HTTP_METHOD, tgt.API_PATTERN=src.API_PATTERN, tgt.SORT_ORDER=src.SORT_ORDER, tgt.USE_YN=src.USE_YN, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (BUTTON_ID, MENU_ID, ACTION_CODE, BUTTON_NAME, HTTP_METHOD, API_PATTERN, SORT_ORDER, USE_YN, created_by, updated_by) VALUES (src.BUTTON_ID, src.MENU_ID, src.ACTION_CODE, src.BUTTON_NAME, src.HTTP_METHOD, src.API_PATTERN, src.SORT_ORDER, src.USE_YN, src.created_by, src.updated_by);
MERGE INTO ADM_BUTTON tgt
USING (SELECT 'NOTIFICATION_WRITE' AS BUTTON_ID, 'NOTIFICATION' AS MENU_ID, 'WRITE' AS ACTION_CODE, '알림 등록/수정' AS BUTTON_NAME, 'POST' AS HTTP_METHOD, '/adm/api/notifications/**' AS API_PATTERN, 20 AS SORT_ORDER, 'Y' AS USE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.BUTTON_ID=src.BUTTON_ID)
WHEN MATCHED THEN UPDATE SET tgt.MENU_ID=src.MENU_ID, tgt.ACTION_CODE=src.ACTION_CODE, tgt.BUTTON_NAME=src.BUTTON_NAME, tgt.HTTP_METHOD=src.HTTP_METHOD, tgt.API_PATTERN=src.API_PATTERN, tgt.SORT_ORDER=src.SORT_ORDER, tgt.USE_YN=src.USE_YN, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (BUTTON_ID, MENU_ID, ACTION_CODE, BUTTON_NAME, HTTP_METHOD, API_PATTERN, SORT_ORDER, USE_YN, created_by, updated_by) VALUES (src.BUTTON_ID, src.MENU_ID, src.ACTION_CODE, src.BUTTON_NAME, src.HTTP_METHOD, src.API_PATTERN, src.SORT_ORDER, src.USE_YN, src.created_by, src.updated_by);
MERGE INTO ADM_BUTTON tgt
USING (SELECT 'NOTIFICATION_DISABLE' AS BUTTON_ID, 'NOTIFICATION' AS MENU_ID, 'DISABLE' AS ACTION_CODE, '알림 비활성화' AS BUTTON_NAME, 'PUT' AS HTTP_METHOD, '/adm/api/notifications/rules/*/disable' AS API_PATTERN, 30 AS SORT_ORDER, 'Y' AS USE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.BUTTON_ID=src.BUTTON_ID)
WHEN MATCHED THEN UPDATE SET tgt.MENU_ID=src.MENU_ID, tgt.ACTION_CODE=src.ACTION_CODE, tgt.BUTTON_NAME=src.BUTTON_NAME, tgt.HTTP_METHOD=src.HTTP_METHOD, tgt.API_PATTERN=src.API_PATTERN, tgt.SORT_ORDER=src.SORT_ORDER, tgt.USE_YN=src.USE_YN, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (BUTTON_ID, MENU_ID, ACTION_CODE, BUTTON_NAME, HTTP_METHOD, API_PATTERN, SORT_ORDER, USE_YN, created_by, updated_by) VALUES (src.BUTTON_ID, src.MENU_ID, src.ACTION_CODE, src.BUTTON_NAME, src.HTTP_METHOD, src.API_PATTERN, src.SORT_ORDER, src.USE_YN, src.created_by, src.updated_by);
MERGE INTO ADM_BUTTON tgt
USING (SELECT 'NOTIFICATION_TEST_SEND' AS BUTTON_ID, 'NOTIFICATION' AS MENU_ID, 'TEST_SEND' AS ACTION_CODE, '알림 테스트 발송' AS BUTTON_NAME, 'POST' AS HTTP_METHOD, '/adm/api/notifications/rules/*/test-send' AS API_PATTERN, 40 AS SORT_ORDER, 'Y' AS USE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.BUTTON_ID=src.BUTTON_ID)
WHEN MATCHED THEN UPDATE SET tgt.MENU_ID=src.MENU_ID, tgt.ACTION_CODE=src.ACTION_CODE, tgt.BUTTON_NAME=src.BUTTON_NAME, tgt.HTTP_METHOD=src.HTTP_METHOD, tgt.API_PATTERN=src.API_PATTERN, tgt.SORT_ORDER=src.SORT_ORDER, tgt.USE_YN=src.USE_YN, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (BUTTON_ID, MENU_ID, ACTION_CODE, BUTTON_NAME, HTTP_METHOD, API_PATTERN, SORT_ORDER, USE_YN, created_by, updated_by) VALUES (src.BUTTON_ID, src.MENU_ID, src.ACTION_CODE, src.BUTTON_NAME, src.HTTP_METHOD, src.API_PATTERN, src.SORT_ORDER, src.USE_YN, src.created_by, src.updated_by);
MERGE INTO ADM_BUTTON tgt
USING (SELECT 'NOTIFICATION_RETRY' AS BUTTON_ID, 'NOTIFICATION' AS MENU_ID, 'RETRY' AS ACTION_CODE, '알림 발송 재시도' AS BUTTON_NAME, 'POST' AS HTTP_METHOD, '/adm/api/notifications/delivery-logs/*/retry' AS API_PATTERN, 50 AS SORT_ORDER, 'Y' AS USE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.BUTTON_ID=src.BUTTON_ID)
WHEN MATCHED THEN UPDATE SET tgt.MENU_ID=src.MENU_ID, tgt.ACTION_CODE=src.ACTION_CODE, tgt.BUTTON_NAME=src.BUTTON_NAME, tgt.HTTP_METHOD=src.HTTP_METHOD, tgt.API_PATTERN=src.API_PATTERN, tgt.SORT_ORDER=src.SORT_ORDER, tgt.USE_YN=src.USE_YN, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (BUTTON_ID, MENU_ID, ACTION_CODE, BUTTON_NAME, HTTP_METHOD, API_PATTERN, SORT_ORDER, USE_YN, created_by, updated_by) VALUES (src.BUTTON_ID, src.MENU_ID, src.ACTION_CODE, src.BUTTON_NAME, src.HTTP_METHOD, src.API_PATTERN, src.SORT_ORDER, src.USE_YN, src.created_by, src.updated_by);
MERGE INTO ADM_BUTTON tgt
USING (SELECT 'NOTIFICATION_CANCEL' AS BUTTON_ID, 'NOTIFICATION' AS MENU_ID, 'CANCEL' AS ACTION_CODE, '알림 발송 취소' AS BUTTON_NAME, 'POST' AS HTTP_METHOD, '/adm/api/notifications/delivery-logs/*/cancel' AS API_PATTERN, 60 AS SORT_ORDER, 'Y' AS USE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.BUTTON_ID=src.BUTTON_ID)
WHEN MATCHED THEN UPDATE SET tgt.MENU_ID=src.MENU_ID, tgt.ACTION_CODE=src.ACTION_CODE, tgt.BUTTON_NAME=src.BUTTON_NAME, tgt.HTTP_METHOD=src.HTTP_METHOD, tgt.API_PATTERN=src.API_PATTERN, tgt.SORT_ORDER=src.SORT_ORDER, tgt.USE_YN=src.USE_YN, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (BUTTON_ID, MENU_ID, ACTION_CODE, BUTTON_NAME, HTTP_METHOD, API_PATTERN, SORT_ORDER, USE_YN, created_by, updated_by) VALUES (src.BUTTON_ID, src.MENU_ID, src.ACTION_CODE, src.BUTTON_NAME, src.HTTP_METHOD, src.API_PATTERN, src.SORT_ORDER, src.USE_YN, src.created_by, src.updated_by);
MERGE INTO ADM_BUTTON tgt
USING (SELECT 'DOWNLOAD_READ' AS BUTTON_ID, 'DOWNLOAD' AS MENU_ID, 'READ' AS ACTION_CODE, '다운로드 감사 조회' AS BUTTON_NAME, 'GET' AS HTTP_METHOD, '/adm/api/downloads/**' AS API_PATTERN, 10 AS SORT_ORDER, 'Y' AS USE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.BUTTON_ID=src.BUTTON_ID)
WHEN MATCHED THEN UPDATE SET tgt.MENU_ID=src.MENU_ID, tgt.ACTION_CODE=src.ACTION_CODE, tgt.BUTTON_NAME=src.BUTTON_NAME, tgt.HTTP_METHOD=src.HTTP_METHOD, tgt.API_PATTERN=src.API_PATTERN, tgt.SORT_ORDER=src.SORT_ORDER, tgt.USE_YN=src.USE_YN, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (BUTTON_ID, MENU_ID, ACTION_CODE, BUTTON_NAME, HTTP_METHOD, API_PATTERN, SORT_ORDER, USE_YN, created_by, updated_by) VALUES (src.BUTTON_ID, src.MENU_ID, src.ACTION_CODE, src.BUTTON_NAME, src.HTTP_METHOD, src.API_PATTERN, src.SORT_ORDER, src.USE_YN, src.created_by, src.updated_by);
MERGE INTO ADM_BUTTON tgt
USING (SELECT 'DOWNLOAD_EXECUTE' AS BUTTON_ID, 'DOWNLOAD' AS MENU_ID, 'DOWNLOAD' AS ACTION_CODE, 'CSV 다운로드' AS BUTTON_NAME, 'POST' AS HTTP_METHOD, '/adm/api/downloads/csv' AS API_PATTERN, 20 AS SORT_ORDER, 'Y' AS USE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.BUTTON_ID=src.BUTTON_ID)
WHEN MATCHED THEN UPDATE SET tgt.MENU_ID=src.MENU_ID, tgt.ACTION_CODE=src.ACTION_CODE, tgt.BUTTON_NAME=src.BUTTON_NAME, tgt.HTTP_METHOD=src.HTTP_METHOD, tgt.API_PATTERN=src.API_PATTERN, tgt.SORT_ORDER=src.SORT_ORDER, tgt.USE_YN=src.USE_YN, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (BUTTON_ID, MENU_ID, ACTION_CODE, BUTTON_NAME, HTTP_METHOD, API_PATTERN, SORT_ORDER, USE_YN, created_by, updated_by) VALUES (src.BUTTON_ID, src.MENU_ID, src.ACTION_CODE, src.BUTTON_NAME, src.HTTP_METHOD, src.API_PATTERN, src.SORT_ORDER, src.USE_YN, src.created_by, src.updated_by);
MERGE INTO ADM_BUTTON tgt
USING (SELECT 'CACHE_READ' AS BUTTON_ID, 'CACHE' AS MENU_ID, 'READ' AS ACTION_CODE, '조회' AS BUTTON_NAME, 'GET' AS HTTP_METHOD, '/adm/api/cache/**' AS API_PATTERN, 10 AS SORT_ORDER, 'Y' AS USE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.BUTTON_ID=src.BUTTON_ID)
WHEN MATCHED THEN UPDATE SET tgt.MENU_ID=src.MENU_ID, tgt.ACTION_CODE=src.ACTION_CODE, tgt.BUTTON_NAME=src.BUTTON_NAME, tgt.HTTP_METHOD=src.HTTP_METHOD, tgt.API_PATTERN=src.API_PATTERN, tgt.SORT_ORDER=src.SORT_ORDER, tgt.USE_YN=src.USE_YN, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (BUTTON_ID, MENU_ID, ACTION_CODE, BUTTON_NAME, HTTP_METHOD, API_PATTERN, SORT_ORDER, USE_YN, created_by, updated_by) VALUES (src.BUTTON_ID, src.MENU_ID, src.ACTION_CODE, src.BUTTON_NAME, src.HTTP_METHOD, src.API_PATTERN, src.SORT_ORDER, src.USE_YN, src.created_by, src.updated_by);
MERGE INTO ADM_BUTTON tgt
USING (SELECT 'CACHE_REFRESH' AS BUTTON_ID, 'CACHE' AS MENU_ID, 'REFRESH' AS ACTION_CODE, '캐시 갱신' AS BUTTON_NAME, 'POST' AS HTTP_METHOD, '/adm/api/cache/**' AS API_PATTERN, 20 AS SORT_ORDER, 'Y' AS USE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.BUTTON_ID=src.BUTTON_ID)
WHEN MATCHED THEN UPDATE SET tgt.MENU_ID=src.MENU_ID, tgt.ACTION_CODE=src.ACTION_CODE, tgt.BUTTON_NAME=src.BUTTON_NAME, tgt.HTTP_METHOD=src.HTTP_METHOD, tgt.API_PATTERN=src.API_PATTERN, tgt.SORT_ORDER=src.SORT_ORDER, tgt.USE_YN=src.USE_YN, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (BUTTON_ID, MENU_ID, ACTION_CODE, BUTTON_NAME, HTTP_METHOD, API_PATTERN, SORT_ORDER, USE_YN, created_by, updated_by) VALUES (src.BUTTON_ID, src.MENU_ID, src.ACTION_CODE, src.BUTTON_NAME, src.HTTP_METHOD, src.API_PATTERN, src.SORT_ORDER, src.USE_YN, src.created_by, src.updated_by);
MERGE INTO ADM_BUTTON tgt
USING (SELECT 'CACHE_EVICT_KEY' AS BUTTON_ID, 'CACHE' AS MENU_ID, 'EVICT_KEY' AS ACTION_CODE, '단일 Cache 제거' AS BUTTON_NAME, 'POST' AS HTTP_METHOD, '/adm/api/cache/evict-key' AS API_PATTERN, 30 AS SORT_ORDER, 'Y' AS USE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.BUTTON_ID=src.BUTTON_ID)
WHEN MATCHED THEN UPDATE SET tgt.MENU_ID=src.MENU_ID, tgt.ACTION_CODE=src.ACTION_CODE, tgt.BUTTON_NAME=src.BUTTON_NAME, tgt.HTTP_METHOD=src.HTTP_METHOD, tgt.API_PATTERN=src.API_PATTERN, tgt.SORT_ORDER=src.SORT_ORDER, tgt.USE_YN=src.USE_YN, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (BUTTON_ID, MENU_ID, ACTION_CODE, BUTTON_NAME, HTTP_METHOD, API_PATTERN, SORT_ORDER, USE_YN, created_by, updated_by) VALUES (src.BUTTON_ID, src.MENU_ID, src.ACTION_CODE, src.BUTTON_NAME, src.HTTP_METHOD, src.API_PATTERN, src.SORT_ORDER, src.USE_YN, src.created_by, src.updated_by);
MERGE INTO ADM_BUTTON tgt
USING (SELECT 'CACHE_EVICT_NAMESPACE' AS BUTTON_ID, 'CACHE' AS MENU_ID, 'EVICT_NAMESPACE' AS ACTION_CODE, 'Namespace Cache 제거' AS BUTTON_NAME, 'POST' AS HTTP_METHOD, '/adm/api/cache/evict-namespace' AS API_PATTERN, 40 AS SORT_ORDER, 'Y' AS USE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.BUTTON_ID=src.BUTTON_ID)
WHEN MATCHED THEN UPDATE SET tgt.MENU_ID=src.MENU_ID, tgt.ACTION_CODE=src.ACTION_CODE, tgt.BUTTON_NAME=src.BUTTON_NAME, tgt.HTTP_METHOD=src.HTTP_METHOD, tgt.API_PATTERN=src.API_PATTERN, tgt.SORT_ORDER=src.SORT_ORDER, tgt.USE_YN=src.USE_YN, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (BUTTON_ID, MENU_ID, ACTION_CODE, BUTTON_NAME, HTTP_METHOD, API_PATTERN, SORT_ORDER, USE_YN, created_by, updated_by) VALUES (src.BUTTON_ID, src.MENU_ID, src.ACTION_CODE, src.BUTTON_NAME, src.HTTP_METHOD, src.API_PATTERN, src.SORT_ORDER, src.USE_YN, src.created_by, src.updated_by);
MERGE INTO ADM_BUTTON tgt
USING (SELECT 'CACHE_RECONCILE' AS BUTTON_ID, 'CACHE' AS MENU_ID, 'RECONCILE' AS ACTION_CODE, 'Cache Durable 재조정' AS BUTTON_NAME, 'POST' AS HTTP_METHOD, '/adm/api/cache/reconcile' AS API_PATTERN, 50 AS SORT_ORDER, 'Y' AS USE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.BUTTON_ID=src.BUTTON_ID)
WHEN MATCHED THEN UPDATE SET tgt.MENU_ID=src.MENU_ID, tgt.ACTION_CODE=src.ACTION_CODE, tgt.BUTTON_NAME=src.BUTTON_NAME, tgt.HTTP_METHOD=src.HTTP_METHOD, tgt.API_PATTERN=src.API_PATTERN, tgt.SORT_ORDER=src.SORT_ORDER, tgt.USE_YN=src.USE_YN, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (BUTTON_ID, MENU_ID, ACTION_CODE, BUTTON_NAME, HTTP_METHOD, API_PATTERN, SORT_ORDER, USE_YN, created_by, updated_by) VALUES (src.BUTTON_ID, src.MENU_ID, src.ACTION_CODE, src.BUTTON_NAME, src.HTTP_METHOD, src.API_PATTERN, src.SORT_ORDER, src.USE_YN, src.created_by, src.updated_by);
MERGE INTO ADM_BUTTON tgt
USING (SELECT 'FILE_JOB_READ' AS BUTTON_ID, 'FILE_JOB' AS MENU_ID, 'READ' AS ACTION_CODE, 'File Job 조회' AS BUTTON_NAME, 'GET' AS HTTP_METHOD, '/adm/api/file-jobs/**' AS API_PATTERN, 10 AS SORT_ORDER, 'Y' AS USE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.BUTTON_ID=src.BUTTON_ID)
WHEN MATCHED THEN UPDATE SET tgt.MENU_ID=src.MENU_ID, tgt.ACTION_CODE=src.ACTION_CODE, tgt.BUTTON_NAME=src.BUTTON_NAME, tgt.HTTP_METHOD=src.HTTP_METHOD, tgt.API_PATTERN=src.API_PATTERN, tgt.SORT_ORDER=src.SORT_ORDER, tgt.USE_YN=src.USE_YN, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (BUTTON_ID, MENU_ID, ACTION_CODE, BUTTON_NAME, HTTP_METHOD, API_PATTERN, SORT_ORDER, USE_YN, created_by, updated_by) VALUES (src.BUTTON_ID, src.MENU_ID, src.ACTION_CODE, src.BUTTON_NAME, src.HTTP_METHOD, src.API_PATTERN, src.SORT_ORDER, src.USE_YN, src.created_by, src.updated_by);
MERGE INTO ADM_BUTTON tgt
USING (SELECT 'FILE_JOB_UPLOAD' AS BUTTON_ID, 'FILE_JOB' AS MENU_ID, 'UPLOAD' AS ACTION_CODE, 'Upload 접수' AS BUTTON_NAME, 'POST' AS HTTP_METHOD, '/adm/api/file-jobs/uploads' AS API_PATTERN, 20 AS SORT_ORDER, 'Y' AS USE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.BUTTON_ID=src.BUTTON_ID)
WHEN MATCHED THEN UPDATE SET tgt.MENU_ID=src.MENU_ID, tgt.ACTION_CODE=src.ACTION_CODE, tgt.BUTTON_NAME=src.BUTTON_NAME, tgt.HTTP_METHOD=src.HTTP_METHOD, tgt.API_PATTERN=src.API_PATTERN, tgt.SORT_ORDER=src.SORT_ORDER, tgt.USE_YN=src.USE_YN, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (BUTTON_ID, MENU_ID, ACTION_CODE, BUTTON_NAME, HTTP_METHOD, API_PATTERN, SORT_ORDER, USE_YN, created_by, updated_by) VALUES (src.BUTTON_ID, src.MENU_ID, src.ACTION_CODE, src.BUTTON_NAME, src.HTTP_METHOD, src.API_PATTERN, src.SORT_ORDER, src.USE_YN, src.created_by, src.updated_by);
MERGE INTO ADM_BUTTON tgt
USING (SELECT 'FILE_JOB_APPLY' AS BUTTON_ID, 'FILE_JOB' AS MENU_ID, 'APPLY' AS ACTION_CODE, '검증 Job 적용' AS BUTTON_NAME, 'POST' AS HTTP_METHOD, '/adm/api/file-jobs/*/apply' AS API_PATTERN, 30 AS SORT_ORDER, 'Y' AS USE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.BUTTON_ID=src.BUTTON_ID)
WHEN MATCHED THEN UPDATE SET tgt.MENU_ID=src.MENU_ID, tgt.ACTION_CODE=src.ACTION_CODE, tgt.BUTTON_NAME=src.BUTTON_NAME, tgt.HTTP_METHOD=src.HTTP_METHOD, tgt.API_PATTERN=src.API_PATTERN, tgt.SORT_ORDER=src.SORT_ORDER, tgt.USE_YN=src.USE_YN, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (BUTTON_ID, MENU_ID, ACTION_CODE, BUTTON_NAME, HTTP_METHOD, API_PATTERN, SORT_ORDER, USE_YN, created_by, updated_by) VALUES (src.BUTTON_ID, src.MENU_ID, src.ACTION_CODE, src.BUTTON_NAME, src.HTTP_METHOD, src.API_PATTERN, src.SORT_ORDER, src.USE_YN, src.created_by, src.updated_by);
MERGE INTO ADM_BUTTON tgt
USING (SELECT 'FILE_JOB_RETRY' AS BUTTON_ID, 'FILE_JOB' AS MENU_ID, 'RETRY' AS ACTION_CODE, 'File Job 재시도' AS BUTTON_NAME, 'POST' AS HTTP_METHOD, '/adm/api/file-jobs/*/retry' AS API_PATTERN, 40 AS SORT_ORDER, 'Y' AS USE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.BUTTON_ID=src.BUTTON_ID)
WHEN MATCHED THEN UPDATE SET tgt.MENU_ID=src.MENU_ID, tgt.ACTION_CODE=src.ACTION_CODE, tgt.BUTTON_NAME=src.BUTTON_NAME, tgt.HTTP_METHOD=src.HTTP_METHOD, tgt.API_PATTERN=src.API_PATTERN, tgt.SORT_ORDER=src.SORT_ORDER, tgt.USE_YN=src.USE_YN, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (BUTTON_ID, MENU_ID, ACTION_CODE, BUTTON_NAME, HTTP_METHOD, API_PATTERN, SORT_ORDER, USE_YN, created_by, updated_by) VALUES (src.BUTTON_ID, src.MENU_ID, src.ACTION_CODE, src.BUTTON_NAME, src.HTTP_METHOD, src.API_PATTERN, src.SORT_ORDER, src.USE_YN, src.created_by, src.updated_by);
MERGE INTO ADM_BUTTON tgt
USING (SELECT 'FILE_JOB_CANCEL' AS BUTTON_ID, 'FILE_JOB' AS MENU_ID, 'CANCEL' AS ACTION_CODE, 'File Job 취소' AS BUTTON_NAME, 'POST' AS HTTP_METHOD, '/adm/api/file-jobs/*/cancel' AS API_PATTERN, 50 AS SORT_ORDER, 'Y' AS USE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.BUTTON_ID=src.BUTTON_ID)
WHEN MATCHED THEN UPDATE SET tgt.MENU_ID=src.MENU_ID, tgt.ACTION_CODE=src.ACTION_CODE, tgt.BUTTON_NAME=src.BUTTON_NAME, tgt.HTTP_METHOD=src.HTTP_METHOD, tgt.API_PATTERN=src.API_PATTERN, tgt.SORT_ORDER=src.SORT_ORDER, tgt.USE_YN=src.USE_YN, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (BUTTON_ID, MENU_ID, ACTION_CODE, BUTTON_NAME, HTTP_METHOD, API_PATTERN, SORT_ORDER, USE_YN, created_by, updated_by) VALUES (src.BUTTON_ID, src.MENU_ID, src.ACTION_CODE, src.BUTTON_NAME, src.HTTP_METHOD, src.API_PATTERN, src.SORT_ORDER, src.USE_YN, src.created_by, src.updated_by);
MERGE INTO ADM_BUTTON tgt
USING (SELECT 'FILE_JOB_ROLLBACK' AS BUTTON_ID, 'FILE_JOB' AS MENU_ID, 'ROLLBACK' AS ACTION_CODE, 'File Job Rollback' AS BUTTON_NAME, 'POST' AS HTTP_METHOD, '/adm/api/file-jobs/*/rollback' AS API_PATTERN, 60 AS SORT_ORDER, 'Y' AS USE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.BUTTON_ID=src.BUTTON_ID)
WHEN MATCHED THEN UPDATE SET tgt.MENU_ID=src.MENU_ID, tgt.ACTION_CODE=src.ACTION_CODE, tgt.BUTTON_NAME=src.BUTTON_NAME, tgt.HTTP_METHOD=src.HTTP_METHOD, tgt.API_PATTERN=src.API_PATTERN, tgt.SORT_ORDER=src.SORT_ORDER, tgt.USE_YN=src.USE_YN, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (BUTTON_ID, MENU_ID, ACTION_CODE, BUTTON_NAME, HTTP_METHOD, API_PATTERN, SORT_ORDER, USE_YN, created_by, updated_by) VALUES (src.BUTTON_ID, src.MENU_ID, src.ACTION_CODE, src.BUTTON_NAME, src.HTTP_METHOD, src.API_PATTERN, src.SORT_ORDER, src.USE_YN, src.created_by, src.updated_by);
MERGE INTO ADM_BUTTON tgt
USING (SELECT 'FILE_JOB_DOWNLOAD' AS BUTTON_ID, 'FILE_JOB' AS MENU_ID, 'DOWNLOAD' AS ACTION_CODE, 'Artifact 다운로드' AS BUTTON_NAME, 'GET' AS HTTP_METHOD, '/adm/api/file-jobs/*/artifact' AS API_PATTERN, 70 AS SORT_ORDER, 'Y' AS USE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.BUTTON_ID=src.BUTTON_ID)
WHEN MATCHED THEN UPDATE SET tgt.MENU_ID=src.MENU_ID, tgt.ACTION_CODE=src.ACTION_CODE, tgt.BUTTON_NAME=src.BUTTON_NAME, tgt.HTTP_METHOD=src.HTTP_METHOD, tgt.API_PATTERN=src.API_PATTERN, tgt.SORT_ORDER=src.SORT_ORDER, tgt.USE_YN=src.USE_YN, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (BUTTON_ID, MENU_ID, ACTION_CODE, BUTTON_NAME, HTTP_METHOD, API_PATTERN, SORT_ORDER, USE_YN, created_by, updated_by) VALUES (src.BUTTON_ID, src.MENU_ID, src.ACTION_CODE, src.BUTTON_NAME, src.HTTP_METHOD, src.API_PATTERN, src.SORT_ORDER, src.USE_YN, src.created_by, src.updated_by);
MERGE INTO ADM_BUTTON tgt
USING (SELECT 'FILE_JOB_RESOLVE' AS BUTTON_ID, 'FILE_JOB' AS MENU_ID, 'RESOLVE' AS ACTION_CODE, '결과 불명 확정' AS BUTTON_NAME, 'POST' AS HTTP_METHOD, '/adm/api/file-jobs/*/resolve-unknown' AS API_PATTERN, 80 AS SORT_ORDER, 'Y' AS USE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.BUTTON_ID=src.BUTTON_ID)
WHEN MATCHED THEN UPDATE SET tgt.MENU_ID=src.MENU_ID, tgt.ACTION_CODE=src.ACTION_CODE, tgt.BUTTON_NAME=src.BUTTON_NAME, tgt.HTTP_METHOD=src.HTTP_METHOD, tgt.API_PATTERN=src.API_PATTERN, tgt.SORT_ORDER=src.SORT_ORDER, tgt.USE_YN=src.USE_YN, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (BUTTON_ID, MENU_ID, ACTION_CODE, BUTTON_NAME, HTTP_METHOD, API_PATTERN, SORT_ORDER, USE_YN, created_by, updated_by) VALUES (src.BUTTON_ID, src.MENU_ID, src.ACTION_CODE, src.BUTTON_NAME, src.HTTP_METHOD, src.API_PATTERN, src.SORT_ORDER, src.USE_YN, src.created_by, src.updated_by);
MERGE INTO ADM_BUTTON tgt
USING (SELECT 'MESSAGE_READ' AS BUTTON_ID, 'MESSAGE' AS MENU_ID, 'READ' AS ACTION_CODE, '조회' AS BUTTON_NAME, 'GET' AS HTTP_METHOD, '/adm/api/messages/**' AS API_PATTERN, 10 AS SORT_ORDER, 'Y' AS USE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.BUTTON_ID=src.BUTTON_ID)
WHEN MATCHED THEN UPDATE SET tgt.MENU_ID=src.MENU_ID, tgt.ACTION_CODE=src.ACTION_CODE, tgt.BUTTON_NAME=src.BUTTON_NAME, tgt.HTTP_METHOD=src.HTTP_METHOD, tgt.API_PATTERN=src.API_PATTERN, tgt.SORT_ORDER=src.SORT_ORDER, tgt.USE_YN=src.USE_YN, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (BUTTON_ID, MENU_ID, ACTION_CODE, BUTTON_NAME, HTTP_METHOD, API_PATTERN, SORT_ORDER, USE_YN, created_by, updated_by) VALUES (src.BUTTON_ID, src.MENU_ID, src.ACTION_CODE, src.BUTTON_NAME, src.HTTP_METHOD, src.API_PATTERN, src.SORT_ORDER, src.USE_YN, src.created_by, src.updated_by);
MERGE INTO ADM_BUTTON tgt
USING (SELECT 'MESSAGE_WRITE' AS BUTTON_ID, 'MESSAGE' AS MENU_ID, 'WRITE' AS ACTION_CODE, '등록/수정' AS BUTTON_NAME, 'POST' AS HTTP_METHOD, '/adm/api/messages/**' AS API_PATTERN, 20 AS SORT_ORDER, 'Y' AS USE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.BUTTON_ID=src.BUTTON_ID)
WHEN MATCHED THEN UPDATE SET tgt.MENU_ID=src.MENU_ID, tgt.ACTION_CODE=src.ACTION_CODE, tgt.BUTTON_NAME=src.BUTTON_NAME, tgt.HTTP_METHOD=src.HTTP_METHOD, tgt.API_PATTERN=src.API_PATTERN, tgt.SORT_ORDER=src.SORT_ORDER, tgt.USE_YN=src.USE_YN, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (BUTTON_ID, MENU_ID, ACTION_CODE, BUTTON_NAME, HTTP_METHOD, API_PATTERN, SORT_ORDER, USE_YN, created_by, updated_by) VALUES (src.BUTTON_ID, src.MENU_ID, src.ACTION_CODE, src.BUTTON_NAME, src.HTTP_METHOD, src.API_PATTERN, src.SORT_ORDER, src.USE_YN, src.created_by, src.updated_by);
MERGE INTO ADM_BUTTON tgt
USING (SELECT 'MESSAGE_DISABLE' AS BUTTON_ID, 'MESSAGE' AS MENU_ID, 'DISABLE' AS ACTION_CODE, '비활성' AS BUTTON_NAME, 'DELETE' AS HTTP_METHOD, '/adm/api/messages/**' AS API_PATTERN, 30 AS SORT_ORDER, 'Y' AS USE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.BUTTON_ID=src.BUTTON_ID)
WHEN MATCHED THEN UPDATE SET tgt.MENU_ID=src.MENU_ID, tgt.ACTION_CODE=src.ACTION_CODE, tgt.BUTTON_NAME=src.BUTTON_NAME, tgt.HTTP_METHOD=src.HTTP_METHOD, tgt.API_PATTERN=src.API_PATTERN, tgt.SORT_ORDER=src.SORT_ORDER, tgt.USE_YN=src.USE_YN, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (BUTTON_ID, MENU_ID, ACTION_CODE, BUTTON_NAME, HTTP_METHOD, API_PATTERN, SORT_ORDER, USE_YN, created_by, updated_by) VALUES (src.BUTTON_ID, src.MENU_ID, src.ACTION_CODE, src.BUTTON_NAME, src.HTTP_METHOD, src.API_PATTERN, src.SORT_ORDER, src.USE_YN, src.created_by, src.updated_by);
MERGE INTO ADM_BUTTON tgt
USING (SELECT 'CODE_READ' AS BUTTON_ID, 'CODE' AS MENU_ID, 'READ' AS ACTION_CODE, '조회' AS BUTTON_NAME, 'GET' AS HTTP_METHOD, '/adm/api/codes/**' AS API_PATTERN, 10 AS SORT_ORDER, 'Y' AS USE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.BUTTON_ID=src.BUTTON_ID)
WHEN MATCHED THEN UPDATE SET tgt.MENU_ID=src.MENU_ID, tgt.ACTION_CODE=src.ACTION_CODE, tgt.BUTTON_NAME=src.BUTTON_NAME, tgt.HTTP_METHOD=src.HTTP_METHOD, tgt.API_PATTERN=src.API_PATTERN, tgt.SORT_ORDER=src.SORT_ORDER, tgt.USE_YN=src.USE_YN, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (BUTTON_ID, MENU_ID, ACTION_CODE, BUTTON_NAME, HTTP_METHOD, API_PATTERN, SORT_ORDER, USE_YN, created_by, updated_by) VALUES (src.BUTTON_ID, src.MENU_ID, src.ACTION_CODE, src.BUTTON_NAME, src.HTTP_METHOD, src.API_PATTERN, src.SORT_ORDER, src.USE_YN, src.created_by, src.updated_by);
MERGE INTO ADM_BUTTON tgt
USING (SELECT 'CODE_WRITE' AS BUTTON_ID, 'CODE' AS MENU_ID, 'WRITE' AS ACTION_CODE, '등록/수정' AS BUTTON_NAME, 'POST' AS HTTP_METHOD, '/adm/api/codes/**' AS API_PATTERN, 20 AS SORT_ORDER, 'Y' AS USE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.BUTTON_ID=src.BUTTON_ID)
WHEN MATCHED THEN UPDATE SET tgt.MENU_ID=src.MENU_ID, tgt.ACTION_CODE=src.ACTION_CODE, tgt.BUTTON_NAME=src.BUTTON_NAME, tgt.HTTP_METHOD=src.HTTP_METHOD, tgt.API_PATTERN=src.API_PATTERN, tgt.SORT_ORDER=src.SORT_ORDER, tgt.USE_YN=src.USE_YN, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (BUTTON_ID, MENU_ID, ACTION_CODE, BUTTON_NAME, HTTP_METHOD, API_PATTERN, SORT_ORDER, USE_YN, created_by, updated_by) VALUES (src.BUTTON_ID, src.MENU_ID, src.ACTION_CODE, src.BUTTON_NAME, src.HTTP_METHOD, src.API_PATTERN, src.SORT_ORDER, src.USE_YN, src.created_by, src.updated_by);
MERGE INTO ADM_BUTTON tgt
USING (SELECT 'CODE_DISABLE' AS BUTTON_ID, 'CODE' AS MENU_ID, 'DISABLE' AS ACTION_CODE, '비활성' AS BUTTON_NAME, 'DELETE' AS HTTP_METHOD, '/adm/api/codes/**' AS API_PATTERN, 30 AS SORT_ORDER, 'Y' AS USE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.BUTTON_ID=src.BUTTON_ID)
WHEN MATCHED THEN UPDATE SET tgt.MENU_ID=src.MENU_ID, tgt.ACTION_CODE=src.ACTION_CODE, tgt.BUTTON_NAME=src.BUTTON_NAME, tgt.HTTP_METHOD=src.HTTP_METHOD, tgt.API_PATTERN=src.API_PATTERN, tgt.SORT_ORDER=src.SORT_ORDER, tgt.USE_YN=src.USE_YN, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (BUTTON_ID, MENU_ID, ACTION_CODE, BUTTON_NAME, HTTP_METHOD, API_PATTERN, SORT_ORDER, USE_YN, created_by, updated_by) VALUES (src.BUTTON_ID, src.MENU_ID, src.ACTION_CODE, src.BUTTON_NAME, src.HTTP_METHOD, src.API_PATTERN, src.SORT_ORDER, src.USE_YN, src.created_by, src.updated_by);
MERGE INTO ADM_BUTTON tgt
USING (SELECT 'RESPONSE_CODE_READ' AS BUTTON_ID, 'RESPONSE_CODE' AS MENU_ID, 'READ' AS ACTION_CODE, '조회' AS BUTTON_NAME, 'GET' AS HTTP_METHOD, '/adm/api/response-codes/**' AS API_PATTERN, 10 AS SORT_ORDER, 'Y' AS USE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.BUTTON_ID=src.BUTTON_ID)
WHEN MATCHED THEN UPDATE SET tgt.MENU_ID=src.MENU_ID, tgt.ACTION_CODE=src.ACTION_CODE, tgt.BUTTON_NAME=src.BUTTON_NAME, tgt.HTTP_METHOD=src.HTTP_METHOD, tgt.API_PATTERN=src.API_PATTERN, tgt.SORT_ORDER=src.SORT_ORDER, tgt.USE_YN=src.USE_YN, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (BUTTON_ID, MENU_ID, ACTION_CODE, BUTTON_NAME, HTTP_METHOD, API_PATTERN, SORT_ORDER, USE_YN, created_by, updated_by) VALUES (src.BUTTON_ID, src.MENU_ID, src.ACTION_CODE, src.BUTTON_NAME, src.HTTP_METHOD, src.API_PATTERN, src.SORT_ORDER, src.USE_YN, src.created_by, src.updated_by);
MERGE INTO ADM_BUTTON tgt
USING (SELECT 'RESPONSE_CODE_WRITE' AS BUTTON_ID, 'RESPONSE_CODE' AS MENU_ID, 'WRITE' AS ACTION_CODE, '등록/수정' AS BUTTON_NAME, 'POST' AS HTTP_METHOD, '/adm/api/response-codes/**' AS API_PATTERN, 20 AS SORT_ORDER, 'Y' AS USE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.BUTTON_ID=src.BUTTON_ID)
WHEN MATCHED THEN UPDATE SET tgt.MENU_ID=src.MENU_ID, tgt.ACTION_CODE=src.ACTION_CODE, tgt.BUTTON_NAME=src.BUTTON_NAME, tgt.HTTP_METHOD=src.HTTP_METHOD, tgt.API_PATTERN=src.API_PATTERN, tgt.SORT_ORDER=src.SORT_ORDER, tgt.USE_YN=src.USE_YN, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (BUTTON_ID, MENU_ID, ACTION_CODE, BUTTON_NAME, HTTP_METHOD, API_PATTERN, SORT_ORDER, USE_YN, created_by, updated_by) VALUES (src.BUTTON_ID, src.MENU_ID, src.ACTION_CODE, src.BUTTON_NAME, src.HTTP_METHOD, src.API_PATTERN, src.SORT_ORDER, src.USE_YN, src.created_by, src.updated_by);
MERGE INTO ADM_BUTTON tgt
USING (SELECT 'CONFIG_READ' AS BUTTON_ID, 'CONFIG' AS MENU_ID, 'READ' AS ACTION_CODE, '조회' AS BUTTON_NAME, 'GET' AS HTTP_METHOD, '/adm/api/configs/**' AS API_PATTERN, 10 AS SORT_ORDER, 'Y' AS USE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.BUTTON_ID=src.BUTTON_ID)
WHEN MATCHED THEN UPDATE SET tgt.MENU_ID=src.MENU_ID, tgt.ACTION_CODE=src.ACTION_CODE, tgt.BUTTON_NAME=src.BUTTON_NAME, tgt.HTTP_METHOD=src.HTTP_METHOD, tgt.API_PATTERN=src.API_PATTERN, tgt.SORT_ORDER=src.SORT_ORDER, tgt.USE_YN=src.USE_YN, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (BUTTON_ID, MENU_ID, ACTION_CODE, BUTTON_NAME, HTTP_METHOD, API_PATTERN, SORT_ORDER, USE_YN, created_by, updated_by) VALUES (src.BUTTON_ID, src.MENU_ID, src.ACTION_CODE, src.BUTTON_NAME, src.HTTP_METHOD, src.API_PATTERN, src.SORT_ORDER, src.USE_YN, src.created_by, src.updated_by);
MERGE INTO ADM_BUTTON tgt
USING (SELECT 'CONFIG_WRITE' AS BUTTON_ID, 'CONFIG' AS MENU_ID, 'WRITE' AS ACTION_CODE, '수정' AS BUTTON_NAME, 'POST' AS HTTP_METHOD, '/adm/api/configs/**' AS API_PATTERN, 20 AS SORT_ORDER, 'Y' AS USE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.BUTTON_ID=src.BUTTON_ID)
WHEN MATCHED THEN UPDATE SET tgt.MENU_ID=src.MENU_ID, tgt.ACTION_CODE=src.ACTION_CODE, tgt.BUTTON_NAME=src.BUTTON_NAME, tgt.HTTP_METHOD=src.HTTP_METHOD, tgt.API_PATTERN=src.API_PATTERN, tgt.SORT_ORDER=src.SORT_ORDER, tgt.USE_YN=src.USE_YN, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (BUTTON_ID, MENU_ID, ACTION_CODE, BUTTON_NAME, HTTP_METHOD, API_PATTERN, SORT_ORDER, USE_YN, created_by, updated_by) VALUES (src.BUTTON_ID, src.MENU_ID, src.ACTION_CODE, src.BUTTON_NAME, src.HTTP_METHOD, src.API_PATTERN, src.SORT_ORDER, src.USE_YN, src.created_by, src.updated_by);
MERGE INTO ADM_BUTTON tgt
USING (SELECT 'DYNAMIC_LOG_READ' AS BUTTON_ID, 'DYNAMIC_LOG' AS MENU_ID, 'READ' AS ACTION_CODE, '조회' AS BUTTON_NAME, 'GET' AS HTTP_METHOD, '/adm/api/log-level/**' AS API_PATTERN, 10 AS SORT_ORDER, 'Y' AS USE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.BUTTON_ID=src.BUTTON_ID)
WHEN MATCHED THEN UPDATE SET tgt.MENU_ID=src.MENU_ID, tgt.ACTION_CODE=src.ACTION_CODE, tgt.BUTTON_NAME=src.BUTTON_NAME, tgt.HTTP_METHOD=src.HTTP_METHOD, tgt.API_PATTERN=src.API_PATTERN, tgt.SORT_ORDER=src.SORT_ORDER, tgt.USE_YN=src.USE_YN, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (BUTTON_ID, MENU_ID, ACTION_CODE, BUTTON_NAME, HTTP_METHOD, API_PATTERN, SORT_ORDER, USE_YN, created_by, updated_by) VALUES (src.BUTTON_ID, src.MENU_ID, src.ACTION_CODE, src.BUTTON_NAME, src.HTTP_METHOD, src.API_PATTERN, src.SORT_ORDER, src.USE_YN, src.created_by, src.updated_by);
MERGE INTO ADM_BUTTON tgt
USING (SELECT 'DYNAMIC_LOG_WRITE' AS BUTTON_ID, 'DYNAMIC_LOG' AS MENU_ID, 'WRITE' AS ACTION_CODE, '적용' AS BUTTON_NAME, 'POST' AS HTTP_METHOD, '/adm/api/log-level/**' AS API_PATTERN, 20 AS SORT_ORDER, 'Y' AS USE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.BUTTON_ID=src.BUTTON_ID)
WHEN MATCHED THEN UPDATE SET tgt.MENU_ID=src.MENU_ID, tgt.ACTION_CODE=src.ACTION_CODE, tgt.BUTTON_NAME=src.BUTTON_NAME, tgt.HTTP_METHOD=src.HTTP_METHOD, tgt.API_PATTERN=src.API_PATTERN, tgt.SORT_ORDER=src.SORT_ORDER, tgt.USE_YN=src.USE_YN, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (BUTTON_ID, MENU_ID, ACTION_CODE, BUTTON_NAME, HTTP_METHOD, API_PATTERN, SORT_ORDER, USE_YN, created_by, updated_by) VALUES (src.BUTTON_ID, src.MENU_ID, src.ACTION_CODE, src.BUTTON_NAME, src.HTTP_METHOD, src.API_PATTERN, src.SORT_ORDER, src.USE_YN, src.created_by, src.updated_by);
MERGE INTO ADM_BUTTON tgt
USING (SELECT 'LOG_POLICY_READ' AS BUTTON_ID, 'LOG_POLICY' AS MENU_ID, 'READ' AS ACTION_CODE, '조회' AS BUTTON_NAME, 'GET' AS HTTP_METHOD, '/adm/api/log-policies/**' AS API_PATTERN, 10 AS SORT_ORDER, 'Y' AS USE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.BUTTON_ID=src.BUTTON_ID)
WHEN MATCHED THEN UPDATE SET tgt.MENU_ID=src.MENU_ID, tgt.ACTION_CODE=src.ACTION_CODE, tgt.BUTTON_NAME=src.BUTTON_NAME, tgt.HTTP_METHOD=src.HTTP_METHOD, tgt.API_PATTERN=src.API_PATTERN, tgt.SORT_ORDER=src.SORT_ORDER, tgt.USE_YN=src.USE_YN, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (BUTTON_ID, MENU_ID, ACTION_CODE, BUTTON_NAME, HTTP_METHOD, API_PATTERN, SORT_ORDER, USE_YN, created_by, updated_by) VALUES (src.BUTTON_ID, src.MENU_ID, src.ACTION_CODE, src.BUTTON_NAME, src.HTTP_METHOD, src.API_PATTERN, src.SORT_ORDER, src.USE_YN, src.created_by, src.updated_by);
MERGE INTO ADM_BUTTON tgt
USING (SELECT 'LOG_POLICY_WRITE' AS BUTTON_ID, 'LOG_POLICY' AS MENU_ID, 'WRITE' AS ACTION_CODE, '등록/수정' AS BUTTON_NAME, 'POST' AS HTTP_METHOD, '/adm/api/log-policies/**' AS API_PATTERN, 20 AS SORT_ORDER, 'Y' AS USE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.BUTTON_ID=src.BUTTON_ID)
WHEN MATCHED THEN UPDATE SET tgt.MENU_ID=src.MENU_ID, tgt.ACTION_CODE=src.ACTION_CODE, tgt.BUTTON_NAME=src.BUTTON_NAME, tgt.HTTP_METHOD=src.HTTP_METHOD, tgt.API_PATTERN=src.API_PATTERN, tgt.SORT_ORDER=src.SORT_ORDER, tgt.USE_YN=src.USE_YN, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (BUTTON_ID, MENU_ID, ACTION_CODE, BUTTON_NAME, HTTP_METHOD, API_PATTERN, SORT_ORDER, USE_YN, created_by, updated_by) VALUES (src.BUTTON_ID, src.MENU_ID, src.ACTION_CODE, src.BUTTON_NAME, src.HTTP_METHOD, src.API_PATTERN, src.SORT_ORDER, src.USE_YN, src.created_by, src.updated_by);
MERGE INTO ADM_BUTTON tgt
USING (SELECT 'LOG_POLICY_OVERRIDE' AS BUTTON_ID, 'LOG_POLICY' AS MENU_ID, 'OVERRIDE' AS ACTION_CODE, '임시 override' AS BUTTON_NAME, 'POST' AS HTTP_METHOD, '/adm/api/log-policies/overrides' AS API_PATTERN, 30 AS SORT_ORDER, 'Y' AS USE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.BUTTON_ID=src.BUTTON_ID)
WHEN MATCHED THEN UPDATE SET tgt.MENU_ID=src.MENU_ID, tgt.ACTION_CODE=src.ACTION_CODE, tgt.BUTTON_NAME=src.BUTTON_NAME, tgt.HTTP_METHOD=src.HTTP_METHOD, tgt.API_PATTERN=src.API_PATTERN, tgt.SORT_ORDER=src.SORT_ORDER, tgt.USE_YN=src.USE_YN, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (BUTTON_ID, MENU_ID, ACTION_CODE, BUTTON_NAME, HTTP_METHOD, API_PATTERN, SORT_ORDER, USE_YN, created_by, updated_by) VALUES (src.BUTTON_ID, src.MENU_ID, src.ACTION_CODE, src.BUTTON_NAME, src.HTTP_METHOD, src.API_PATTERN, src.SORT_ORDER, src.USE_YN, src.created_by, src.updated_by);
MERGE INTO ADM_BUTTON tgt
USING (SELECT 'LOG_POLICY_CACHE_REFRESH' AS BUTTON_ID, 'LOG_POLICY' AS MENU_ID, 'CACHE_REFRESH' AS ACTION_CODE, '정책 캐시 새로고침' AS BUTTON_NAME, 'POST' AS HTTP_METHOD, '/adm/api/log-policies/cache/refresh' AS API_PATTERN, 40 AS SORT_ORDER, 'Y' AS USE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.BUTTON_ID=src.BUTTON_ID)
WHEN MATCHED THEN UPDATE SET tgt.MENU_ID=src.MENU_ID, tgt.ACTION_CODE=src.ACTION_CODE, tgt.BUTTON_NAME=src.BUTTON_NAME, tgt.HTTP_METHOD=src.HTTP_METHOD, tgt.API_PATTERN=src.API_PATTERN, tgt.SORT_ORDER=src.SORT_ORDER, tgt.USE_YN=src.USE_YN, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (BUTTON_ID, MENU_ID, ACTION_CODE, BUTTON_NAME, HTTP_METHOD, API_PATTERN, SORT_ORDER, USE_YN, created_by, updated_by) VALUES (src.BUTTON_ID, src.MENU_ID, src.ACTION_CODE, src.BUTTON_NAME, src.HTTP_METHOD, src.API_PATTERN, src.SORT_ORDER, src.USE_YN, src.created_by, src.updated_by);
MERGE INTO ADM_BUTTON tgt
USING (SELECT 'LOG_POLICY_CACHE_CLEAR' AS BUTTON_ID, 'LOG_POLICY' AS MENU_ID, 'CACHE_CLEAR' AS ACTION_CODE, '정책 캐시 전체 삭제' AS BUTTON_NAME, 'POST' AS HTTP_METHOD, '/adm/api/log-policies/cache/clear' AS API_PATTERN, 50 AS SORT_ORDER, 'Y' AS USE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.BUTTON_ID=src.BUTTON_ID)
WHEN MATCHED THEN UPDATE SET tgt.MENU_ID=src.MENU_ID, tgt.ACTION_CODE=src.ACTION_CODE, tgt.BUTTON_NAME=src.BUTTON_NAME, tgt.HTTP_METHOD=src.HTTP_METHOD, tgt.API_PATTERN=src.API_PATTERN, tgt.SORT_ORDER=src.SORT_ORDER, tgt.USE_YN=src.USE_YN, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (BUTTON_ID, MENU_ID, ACTION_CODE, BUTTON_NAME, HTTP_METHOD, API_PATTERN, SORT_ORDER, USE_YN, created_by, updated_by) VALUES (src.BUTTON_ID, src.MENU_ID, src.ACTION_CODE, src.BUTTON_NAME, src.HTTP_METHOD, src.API_PATTERN, src.SORT_ORDER, src.USE_YN, src.created_by, src.updated_by);
MERGE INTO ADM_BUTTON tgt
USING (SELECT 'PASSWORD_READ' AS BUTTON_ID, 'PASSWORD' AS MENU_ID, 'READ' AS ACTION_CODE, '정책 조회' AS BUTTON_NAME, 'GET' AS HTTP_METHOD, '/adm/api/operators/password-policy/**' AS API_PATTERN, 10 AS SORT_ORDER, 'Y' AS USE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.BUTTON_ID=src.BUTTON_ID)
WHEN MATCHED THEN UPDATE SET tgt.MENU_ID=src.MENU_ID, tgt.ACTION_CODE=src.ACTION_CODE, tgt.BUTTON_NAME=src.BUTTON_NAME, tgt.HTTP_METHOD=src.HTTP_METHOD, tgt.API_PATTERN=src.API_PATTERN, tgt.SORT_ORDER=src.SORT_ORDER, tgt.USE_YN=src.USE_YN, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (BUTTON_ID, MENU_ID, ACTION_CODE, BUTTON_NAME, HTTP_METHOD, API_PATTERN, SORT_ORDER, USE_YN, created_by, updated_by) VALUES (src.BUTTON_ID, src.MENU_ID, src.ACTION_CODE, src.BUTTON_NAME, src.HTTP_METHOD, src.API_PATTERN, src.SORT_ORDER, src.USE_YN, src.created_by, src.updated_by);
MERGE INTO ADM_BUTTON tgt
USING (SELECT 'PASSWORD_RESET' AS BUTTON_ID, 'PASSWORD' AS MENU_ID, 'RESET_PASSWORD' AS ACTION_CODE, '비밀번호 초기화' AS BUTTON_NAME, 'POST' AS HTTP_METHOD, '/adm/api/operators/*/password/reset' AS API_PATTERN, 20 AS SORT_ORDER, 'Y' AS USE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.BUTTON_ID=src.BUTTON_ID)
WHEN MATCHED THEN UPDATE SET tgt.MENU_ID=src.MENU_ID, tgt.ACTION_CODE=src.ACTION_CODE, tgt.BUTTON_NAME=src.BUTTON_NAME, tgt.HTTP_METHOD=src.HTTP_METHOD, tgt.API_PATTERN=src.API_PATTERN, tgt.SORT_ORDER=src.SORT_ORDER, tgt.USE_YN=src.USE_YN, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (BUTTON_ID, MENU_ID, ACTION_CODE, BUTTON_NAME, HTTP_METHOD, API_PATTERN, SORT_ORDER, USE_YN, created_by, updated_by) VALUES (src.BUTTON_ID, src.MENU_ID, src.ACTION_CODE, src.BUTTON_NAME, src.HTTP_METHOD, src.API_PATTERN, src.SORT_ORDER, src.USE_YN, src.created_by, src.updated_by);
MERGE INTO ADM_BUTTON tgt
USING (SELECT 'PASSWORD_UNLOCK' AS BUTTON_ID, 'PASSWORD' AS MENU_ID, 'UNLOCK' AS ACTION_CODE, '잠금 해제' AS BUTTON_NAME, 'POST' AS HTTP_METHOD, '/adm/api/operators/*/unlock' AS API_PATTERN, 30 AS SORT_ORDER, 'Y' AS USE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.BUTTON_ID=src.BUTTON_ID)
WHEN MATCHED THEN UPDATE SET tgt.MENU_ID=src.MENU_ID, tgt.ACTION_CODE=src.ACTION_CODE, tgt.BUTTON_NAME=src.BUTTON_NAME, tgt.HTTP_METHOD=src.HTTP_METHOD, tgt.API_PATTERN=src.API_PATTERN, tgt.SORT_ORDER=src.SORT_ORDER, tgt.USE_YN=src.USE_YN, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (BUTTON_ID, MENU_ID, ACTION_CODE, BUTTON_NAME, HTTP_METHOD, API_PATTERN, SORT_ORDER, USE_YN, created_by, updated_by) VALUES (src.BUTTON_ID, src.MENU_ID, src.ACTION_CODE, src.BUTTON_NAME, src.HTTP_METHOD, src.API_PATTERN, src.SORT_ORDER, src.USE_YN, src.created_by, src.updated_by);
MERGE INTO ADM_BUTTON tgt
USING (SELECT 'PASSWORD_SESSION_REVOKE' AS BUTTON_ID, 'PASSWORD' AS MENU_ID, 'REVOKE_SESSION' AS ACTION_CODE, '세션 강제 종료' AS BUTTON_NAME, 'POST' AS HTTP_METHOD, '/adm/api/operators/sessions/*/revoke' AS API_PATTERN, 40 AS SORT_ORDER, 'Y' AS USE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.BUTTON_ID=src.BUTTON_ID)
WHEN MATCHED THEN UPDATE SET tgt.MENU_ID=src.MENU_ID, tgt.ACTION_CODE=src.ACTION_CODE, tgt.BUTTON_NAME=src.BUTTON_NAME, tgt.HTTP_METHOD=src.HTTP_METHOD, tgt.API_PATTERN=src.API_PATTERN, tgt.SORT_ORDER=src.SORT_ORDER, tgt.USE_YN=src.USE_YN, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (BUTTON_ID, MENU_ID, ACTION_CODE, BUTTON_NAME, HTTP_METHOD, API_PATTERN, SORT_ORDER, USE_YN, created_by, updated_by) VALUES (src.BUTTON_ID, src.MENU_ID, src.ACTION_CODE, src.BUTTON_NAME, src.HTTP_METHOD, src.API_PATTERN, src.SORT_ORDER, src.USE_YN, src.created_by, src.updated_by);
MERGE INTO ADM_BUTTON tgt
USING (SELECT 'SECURITY_READ' AS BUTTON_ID, 'SECURITY' AS MENU_ID, 'READ' AS ACTION_CODE, '조회' AS BUTTON_NAME, 'GET' AS HTTP_METHOD, '/adm/api/security/**' AS API_PATTERN, 10 AS SORT_ORDER, 'Y' AS USE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.BUTTON_ID=src.BUTTON_ID)
WHEN MATCHED THEN UPDATE SET tgt.MENU_ID=src.MENU_ID, tgt.ACTION_CODE=src.ACTION_CODE, tgt.BUTTON_NAME=src.BUTTON_NAME, tgt.HTTP_METHOD=src.HTTP_METHOD, tgt.API_PATTERN=src.API_PATTERN, tgt.SORT_ORDER=src.SORT_ORDER, tgt.USE_YN=src.USE_YN, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (BUTTON_ID, MENU_ID, ACTION_CODE, BUTTON_NAME, HTTP_METHOD, API_PATTERN, SORT_ORDER, USE_YN, created_by, updated_by) VALUES (src.BUTTON_ID, src.MENU_ID, src.ACTION_CODE, src.BUTTON_NAME, src.HTTP_METHOD, src.API_PATTERN, src.SORT_ORDER, src.USE_YN, src.created_by, src.updated_by);
MERGE INTO ADM_BUTTON tgt
USING (SELECT 'SECURITY_WRITE' AS BUTTON_ID, 'SECURITY' AS MENU_ID, 'WRITE' AS ACTION_CODE, '보안 설정 변경' AS BUTTON_NAME, 'POST' AS HTTP_METHOD, '/adm/api/security/**' AS API_PATTERN, 20 AS SORT_ORDER, 'Y' AS USE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.BUTTON_ID=src.BUTTON_ID)
WHEN MATCHED THEN UPDATE SET tgt.MENU_ID=src.MENU_ID, tgt.ACTION_CODE=src.ACTION_CODE, tgt.BUTTON_NAME=src.BUTTON_NAME, tgt.HTTP_METHOD=src.HTTP_METHOD, tgt.API_PATTERN=src.API_PATTERN, tgt.SORT_ORDER=src.SORT_ORDER, tgt.USE_YN=src.USE_YN, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (BUTTON_ID, MENU_ID, ACTION_CODE, BUTTON_NAME, HTTP_METHOD, API_PATTERN, SORT_ORDER, USE_YN, created_by, updated_by) VALUES (src.BUTTON_ID, src.MENU_ID, src.ACTION_CODE, src.BUTTON_NAME, src.HTTP_METHOD, src.API_PATTERN, src.SORT_ORDER, src.USE_YN, src.created_by, src.updated_by);
MERGE INTO ADM_BUTTON tgt
USING (SELECT 'PERMISSION_READ' AS BUTTON_ID, 'PERMISSION' AS MENU_ID, 'READ' AS ACTION_CODE, '조회' AS BUTTON_NAME, 'GET' AS HTTP_METHOD, '/adm/api/permissions/**' AS API_PATTERN, 10 AS SORT_ORDER, 'Y' AS USE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.BUTTON_ID=src.BUTTON_ID)
WHEN MATCHED THEN UPDATE SET tgt.MENU_ID=src.MENU_ID, tgt.ACTION_CODE=src.ACTION_CODE, tgt.BUTTON_NAME=src.BUTTON_NAME, tgt.HTTP_METHOD=src.HTTP_METHOD, tgt.API_PATTERN=src.API_PATTERN, tgt.SORT_ORDER=src.SORT_ORDER, tgt.USE_YN=src.USE_YN, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (BUTTON_ID, MENU_ID, ACTION_CODE, BUTTON_NAME, HTTP_METHOD, API_PATTERN, SORT_ORDER, USE_YN, created_by, updated_by) VALUES (src.BUTTON_ID, src.MENU_ID, src.ACTION_CODE, src.BUTTON_NAME, src.HTTP_METHOD, src.API_PATTERN, src.SORT_ORDER, src.USE_YN, src.created_by, src.updated_by);
MERGE INTO ADM_BUTTON tgt
USING (SELECT 'PERMISSION_WRITE' AS BUTTON_ID, 'PERMISSION' AS MENU_ID, 'WRITE' AS ACTION_CODE, '권한 변경' AS BUTTON_NAME, 'POST' AS HTTP_METHOD, '/adm/api/permissions/**' AS API_PATTERN, 20 AS SORT_ORDER, 'Y' AS USE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.BUTTON_ID=src.BUTTON_ID)
WHEN MATCHED THEN UPDATE SET tgt.MENU_ID=src.MENU_ID, tgt.ACTION_CODE=src.ACTION_CODE, tgt.BUTTON_NAME=src.BUTTON_NAME, tgt.HTTP_METHOD=src.HTTP_METHOD, tgt.API_PATTERN=src.API_PATTERN, tgt.SORT_ORDER=src.SORT_ORDER, tgt.USE_YN=src.USE_YN, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (BUTTON_ID, MENU_ID, ACTION_CODE, BUTTON_NAME, HTTP_METHOD, API_PATTERN, SORT_ORDER, USE_YN, created_by, updated_by) VALUES (src.BUTTON_ID, src.MENU_ID, src.ACTION_CODE, src.BUTTON_NAME, src.HTTP_METHOD, src.API_PATTERN, src.SORT_ORDER, src.USE_YN, src.created_by, src.updated_by);
MERGE INTO ADM_BUTTON tgt
USING (SELECT 'OPERATOR_READ' AS BUTTON_ID, 'OPERATOR' AS MENU_ID, 'READ' AS ACTION_CODE, '조회' AS BUTTON_NAME, 'GET' AS HTTP_METHOD, '/adm/api/operators/**' AS API_PATTERN, 10 AS SORT_ORDER, 'Y' AS USE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.BUTTON_ID=src.BUTTON_ID)
WHEN MATCHED THEN UPDATE SET tgt.MENU_ID=src.MENU_ID, tgt.ACTION_CODE=src.ACTION_CODE, tgt.BUTTON_NAME=src.BUTTON_NAME, tgt.HTTP_METHOD=src.HTTP_METHOD, tgt.API_PATTERN=src.API_PATTERN, tgt.SORT_ORDER=src.SORT_ORDER, tgt.USE_YN=src.USE_YN, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (BUTTON_ID, MENU_ID, ACTION_CODE, BUTTON_NAME, HTTP_METHOD, API_PATTERN, SORT_ORDER, USE_YN, created_by, updated_by) VALUES (src.BUTTON_ID, src.MENU_ID, src.ACTION_CODE, src.BUTTON_NAME, src.HTTP_METHOD, src.API_PATTERN, src.SORT_ORDER, src.USE_YN, src.created_by, src.updated_by);
MERGE INTO ADM_BUTTON tgt
USING (SELECT 'OPERATOR_CREATE' AS BUTTON_ID, 'OPERATOR' AS MENU_ID, 'CREATE' AS ACTION_CODE, '운영자 등록' AS BUTTON_NAME, 'POST' AS HTTP_METHOD, '/adm/api/operators' AS API_PATTERN, 20 AS SORT_ORDER, 'Y' AS USE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.BUTTON_ID=src.BUTTON_ID)
WHEN MATCHED THEN UPDATE SET tgt.MENU_ID=src.MENU_ID, tgt.ACTION_CODE=src.ACTION_CODE, tgt.BUTTON_NAME=src.BUTTON_NAME, tgt.HTTP_METHOD=src.HTTP_METHOD, tgt.API_PATTERN=src.API_PATTERN, tgt.SORT_ORDER=src.SORT_ORDER, tgt.USE_YN=src.USE_YN, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (BUTTON_ID, MENU_ID, ACTION_CODE, BUTTON_NAME, HTTP_METHOD, API_PATTERN, SORT_ORDER, USE_YN, created_by, updated_by) VALUES (src.BUTTON_ID, src.MENU_ID, src.ACTION_CODE, src.BUTTON_NAME, src.HTTP_METHOD, src.API_PATTERN, src.SORT_ORDER, src.USE_YN, src.created_by, src.updated_by);
MERGE INTO ADM_BUTTON tgt
USING (SELECT 'OPERATOR_ROLE_UPDATE' AS BUTTON_ID, 'OPERATOR' AS MENU_ID, 'ROLE_UPDATE' AS ACTION_CODE, '역할 부여' AS BUTTON_NAME, 'PUT' AS HTTP_METHOD, '/adm/api/operators/*/roles' AS API_PATTERN, 30 AS SORT_ORDER, 'Y' AS USE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.BUTTON_ID=src.BUTTON_ID)
WHEN MATCHED THEN UPDATE SET tgt.MENU_ID=src.MENU_ID, tgt.ACTION_CODE=src.ACTION_CODE, tgt.BUTTON_NAME=src.BUTTON_NAME, tgt.HTTP_METHOD=src.HTTP_METHOD, tgt.API_PATTERN=src.API_PATTERN, tgt.SORT_ORDER=src.SORT_ORDER, tgt.USE_YN=src.USE_YN, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (BUTTON_ID, MENU_ID, ACTION_CODE, BUTTON_NAME, HTTP_METHOD, API_PATTERN, SORT_ORDER, USE_YN, created_by, updated_by) VALUES (src.BUTTON_ID, src.MENU_ID, src.ACTION_CODE, src.BUTTON_NAME, src.HTTP_METHOD, src.API_PATTERN, src.SORT_ORDER, src.USE_YN, src.created_by, src.updated_by);
MERGE INTO ADM_BUTTON tgt
USING (SELECT 'OPERATOR_STATUS_UPDATE' AS BUTTON_ID, 'OPERATOR' AS MENU_ID, 'STATUS_UPDATE' AS ACTION_CODE, '계정 상태 변경' AS BUTTON_NAME, 'PUT' AS HTTP_METHOD, '/adm/api/operators/*/status' AS API_PATTERN, 40 AS SORT_ORDER, 'Y' AS USE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.BUTTON_ID=src.BUTTON_ID)
WHEN MATCHED THEN UPDATE SET tgt.MENU_ID=src.MENU_ID, tgt.ACTION_CODE=src.ACTION_CODE, tgt.BUTTON_NAME=src.BUTTON_NAME, tgt.HTTP_METHOD=src.HTTP_METHOD, tgt.API_PATTERN=src.API_PATTERN, tgt.SORT_ORDER=src.SORT_ORDER, tgt.USE_YN=src.USE_YN, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (BUTTON_ID, MENU_ID, ACTION_CODE, BUTTON_NAME, HTTP_METHOD, API_PATTERN, SORT_ORDER, USE_YN, created_by, updated_by) VALUES (src.BUTTON_ID, src.MENU_ID, src.ACTION_CODE, src.BUTTON_NAME, src.HTTP_METHOD, src.API_PATTERN, src.SORT_ORDER, src.USE_YN, src.created_by, src.updated_by);
MERGE INTO ADM_BUTTON tgt
USING (SELECT 'OPERATOR_CONTACT_UPDATE' AS BUTTON_ID, 'OPERATOR' AS MENU_ID, 'CONTACT_UPDATE' AS ACTION_CODE, '연락처 변경' AS BUTTON_NAME, 'PUT' AS HTTP_METHOD, '/adm/api/operators/*/contacts' AS API_PATTERN, 50 AS SORT_ORDER, 'Y' AS USE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.BUTTON_ID=src.BUTTON_ID)
WHEN MATCHED THEN UPDATE SET tgt.MENU_ID=src.MENU_ID, tgt.ACTION_CODE=src.ACTION_CODE, tgt.BUTTON_NAME=src.BUTTON_NAME, tgt.HTTP_METHOD=src.HTTP_METHOD, tgt.API_PATTERN=src.API_PATTERN, tgt.SORT_ORDER=src.SORT_ORDER, tgt.USE_YN=src.USE_YN, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (BUTTON_ID, MENU_ID, ACTION_CODE, BUTTON_NAME, HTTP_METHOD, API_PATTERN, SORT_ORDER, USE_YN, created_by, updated_by) VALUES (src.BUTTON_ID, src.MENU_ID, src.ACTION_CODE, src.BUTTON_NAME, src.HTTP_METHOD, src.API_PATTERN, src.SORT_ORDER, src.USE_YN, src.created_by, src.updated_by);
MERGE INTO ADM_BUTTON tgt
USING (SELECT 'OPERATOR_PII_RAW' AS BUTTON_ID, 'OPERATOR' AS MENU_ID, 'PII_RAW' AS ACTION_CODE, '연락처 원문 조회' AS BUTTON_NAME, 'POST' AS HTTP_METHOD, '/adm/api/operators/*/contacts/raw' AS API_PATTERN, 60 AS SORT_ORDER, 'Y' AS USE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.BUTTON_ID=src.BUTTON_ID)
WHEN MATCHED THEN UPDATE SET tgt.MENU_ID=src.MENU_ID, tgt.ACTION_CODE=src.ACTION_CODE, tgt.BUTTON_NAME=src.BUTTON_NAME, tgt.HTTP_METHOD=src.HTTP_METHOD, tgt.API_PATTERN=src.API_PATTERN, tgt.SORT_ORDER=src.SORT_ORDER, tgt.USE_YN=src.USE_YN, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (BUTTON_ID, MENU_ID, ACTION_CODE, BUTTON_NAME, HTTP_METHOD, API_PATTERN, SORT_ORDER, USE_YN, created_by, updated_by) VALUES (src.BUTTON_ID, src.MENU_ID, src.ACTION_CODE, src.BUTTON_NAME, src.HTTP_METHOD, src.API_PATTERN, src.SORT_ORDER, src.USE_YN, src.created_by, src.updated_by);
MERGE INTO ADM_PASSWORD_POLICY tgt
USING (SELECT 'DEFAULT' AS POLICY_ID, 12 AS MIN_LENGTH, 'Y' AS REQUIRE_UPPER_YN, 'Y' AS REQUIRE_LOWER_YN, 'Y' AS REQUIRE_DIGIT_YN, 'Y' AS REQUIRE_SPECIAL_YN, 5 AS MAX_FAIL_COUNT, 90 AS EXPIRE_DAYS, 5 AS HISTORY_LIMIT, 'Y' AS USE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.POLICY_ID=src.POLICY_ID)
WHEN MATCHED THEN UPDATE SET tgt.MIN_LENGTH=src.MIN_LENGTH, tgt.REQUIRE_UPPER_YN=src.REQUIRE_UPPER_YN, tgt.REQUIRE_LOWER_YN=src.REQUIRE_LOWER_YN, tgt.REQUIRE_DIGIT_YN=src.REQUIRE_DIGIT_YN, tgt.REQUIRE_SPECIAL_YN=src.REQUIRE_SPECIAL_YN, tgt.MAX_FAIL_COUNT=src.MAX_FAIL_COUNT, tgt.EXPIRE_DAYS=src.EXPIRE_DAYS, tgt.HISTORY_LIMIT=src.HISTORY_LIMIT, tgt.USE_YN=src.USE_YN, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (POLICY_ID, MIN_LENGTH, REQUIRE_UPPER_YN, REQUIRE_LOWER_YN, REQUIRE_DIGIT_YN, REQUIRE_SPECIAL_YN, MAX_FAIL_COUNT, EXPIRE_DAYS, HISTORY_LIMIT, USE_YN, created_by, updated_by) VALUES (src.POLICY_ID, src.MIN_LENGTH, src.REQUIRE_UPPER_YN, src.REQUIRE_LOWER_YN, src.REQUIRE_DIGIT_YN, src.REQUIRE_SPECIAL_YN, src.MAX_FAIL_COUNT, src.EXPIRE_DAYS, src.HISTORY_LIMIT, src.USE_YN, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_MENU tgt
USING (SELECT 'ADM_ADMIN' AS ROLE_ID, MENU_ID AS MENU_ID, 'Y' AS READ_YN, 'Y' AS WRITE_YN, 'Y' AS DELETE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM ADM_MENU) src
ON (tgt.ROLE_ID=src.ROLE_ID AND tgt.MENU_ID=src.MENU_ID)
WHEN MATCHED THEN UPDATE SET tgt.READ_YN=src.READ_YN, tgt.WRITE_YN=src.WRITE_YN, tgt.DELETE_YN=src.DELETE_YN, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (ROLE_ID, MENU_ID, READ_YN, WRITE_YN, DELETE_YN, created_by, updated_by) VALUES (src.ROLE_ID, src.MENU_ID, src.READ_YN, src.WRITE_YN, src.DELETE_YN, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_MENU tgt
USING (SELECT 'ADM_DEV_OPERATOR' AS ROLE_ID, MENU_ID AS MENU_ID, 'Y' AS READ_YN, CASE WHEN MENU_ID IN ('TRANSACTION_META', 'CHANNEL_POLICY', 'REMOTE_LOG', 'BATCH', 'RELIABILITY', 'NOTIFICATION', 'DOWNLOAD', 'CACHE', 'FILE_JOB', 'MESSAGE', 'CODE', 'RESPONSE_CODE', 'CONFIG', 'DYNAMIC_LOG', 'LOG_POLICY') THEN 'Y' ELSE 'N' END AS WRITE_YN, CASE WHEN MENU_ID IN ('TRANSACTION_META', 'MESSAGE', 'CODE', 'DYNAMIC_LOG', 'LOG_POLICY') THEN 'Y' ELSE 'N' END AS DELETE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM ADM_MENU
WHERE MENU_ID NOT IN ('OPERATOR', 'PERMISSION', 'PASSWORD', 'SECURITY')) src
ON (tgt.ROLE_ID=src.ROLE_ID AND tgt.MENU_ID=src.MENU_ID)
WHEN MATCHED THEN UPDATE SET tgt.READ_YN=src.READ_YN, tgt.WRITE_YN=src.WRITE_YN, tgt.DELETE_YN=src.DELETE_YN, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (ROLE_ID, MENU_ID, READ_YN, WRITE_YN, DELETE_YN, created_by, updated_by) VALUES (src.ROLE_ID, src.MENU_ID, src.READ_YN, src.WRITE_YN, src.DELETE_YN, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_MENU tgt
USING (SELECT 'ADM_BIZ_OPERATOR' AS ROLE_ID, MENU_ID AS MENU_ID, 'Y' AS READ_YN, CASE WHEN MENU_ID IN ('BATCH', 'DOWNLOAD', 'CACHE', 'FILE_JOB') THEN 'Y' ELSE 'N' END AS WRITE_YN, 'N' AS DELETE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM ADM_MENU
WHERE MENU_ID IN ('DASHBOARD', 'LOG_LIST', 'STANDARD_EXECUTION', 'CHANNEL_POLICY', 'REMOTE_LOG', 'TRANSACTION_META', 'AUDIT_LOG', 'BATCH', 'RELIABILITY', 'NOTIFICATION', 'DOWNLOAD', 'CACHE', 'FILE_JOB', 'MESSAGE', 'CODE')) src
ON (tgt.ROLE_ID=src.ROLE_ID AND tgt.MENU_ID=src.MENU_ID)
WHEN MATCHED THEN UPDATE SET tgt.READ_YN=src.READ_YN, tgt.WRITE_YN=src.WRITE_YN, tgt.DELETE_YN=src.DELETE_YN, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (ROLE_ID, MENU_ID, READ_YN, WRITE_YN, DELETE_YN, created_by, updated_by) VALUES (src.ROLE_ID, src.MENU_ID, src.READ_YN, src.WRITE_YN, src.DELETE_YN, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_MENU tgt
USING (SELECT 'ADM_VIEWER' AS ROLE_ID, MENU_ID AS MENU_ID, 'Y' AS READ_YN, 'N' AS WRITE_YN, 'N' AS DELETE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM ADM_MENU
WHERE MENU_ID IN ('DASHBOARD', 'CAPABILITY_FLEET', 'LOG_LIST', 'STANDARD_EXECUTION', 'CHANNEL_POLICY', 'REMOTE_LOG', 'TRANSACTION_META', 'AUDIT_LOG', 'BATCH', 'RELIABILITY', 'NOTIFICATION', 'DOWNLOAD', 'CACHE', 'FILE_JOB', 'MESSAGE', 'CODE', 'RESPONSE_CODE', 'CONFIG', 'LOG_POLICY')) src
ON (tgt.ROLE_ID=src.ROLE_ID AND tgt.MENU_ID=src.MENU_ID)
WHEN MATCHED THEN UPDATE SET tgt.READ_YN=src.READ_YN, tgt.WRITE_YN=src.WRITE_YN, tgt.DELETE_YN=src.DELETE_YN, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (ROLE_ID, MENU_ID, READ_YN, WRITE_YN, DELETE_YN, created_by, updated_by) VALUES (src.ROLE_ID, src.MENU_ID, src.READ_YN, src.WRITE_YN, src.DELETE_YN, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_MENU tgt
USING (SELECT 'ADM_OPERATOR' AS ROLE_ID, MENU_ID AS MENU_ID, READ_YN AS READ_YN, WRITE_YN AS WRITE_YN, DELETE_YN AS DELETE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM ADM_ROLE_MENU
WHERE ROLE_ID = 'ADM_DEV_OPERATOR') src
ON (tgt.ROLE_ID=src.ROLE_ID AND tgt.MENU_ID=src.MENU_ID)
WHEN MATCHED THEN UPDATE SET tgt.READ_YN=src.READ_YN, tgt.WRITE_YN=src.WRITE_YN, tgt.DELETE_YN=src.DELETE_YN, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (ROLE_ID, MENU_ID, READ_YN, WRITE_YN, DELETE_YN, created_by, updated_by) VALUES (src.ROLE_ID, src.MENU_ID, src.READ_YN, src.WRITE_YN, src.DELETE_YN, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_BUTTON tgt
USING (SELECT 'ADM_ADMIN' AS ROLE_ID, BUTTON_ID AS BUTTON_ID, 'Y' AS ALLOW_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM ADM_BUTTON) src
ON (tgt.ROLE_ID=src.ROLE_ID AND tgt.BUTTON_ID=src.BUTTON_ID)
WHEN MATCHED THEN UPDATE SET tgt.ALLOW_YN=src.ALLOW_YN, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (ROLE_ID, BUTTON_ID, ALLOW_YN, created_by, updated_by) VALUES (src.ROLE_ID, src.BUTTON_ID, src.ALLOW_YN, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_BUTTON tgt
USING (SELECT 'ADM_DEV_OPERATOR' AS ROLE_ID, BUTTON_ID AS BUTTON_ID, CASE WHEN MENU_ID IN ('OPERATOR', 'PERMISSION', 'PASSWORD', 'SECURITY') THEN 'N' ELSE 'Y' END AS ALLOW_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM ADM_BUTTON) src
ON (tgt.ROLE_ID=src.ROLE_ID AND tgt.BUTTON_ID=src.BUTTON_ID)
WHEN MATCHED THEN UPDATE SET tgt.ALLOW_YN=src.ALLOW_YN, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (ROLE_ID, BUTTON_ID, ALLOW_YN, created_by, updated_by) VALUES (src.ROLE_ID, src.BUTTON_ID, src.ALLOW_YN, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_BUTTON tgt
USING (SELECT 'ADM_BIZ_OPERATOR' AS ROLE_ID, BUTTON_ID AS BUTTON_ID, CASE
           WHEN BUTTON_ID IN ('BATCH_EXECUTE', 'BATCH_RETRY', 'BATCH_SIMULATION', 'BATCH_RELATION_READ', 'BATCH_TARGET_READ', 'BATCH_SCHEDULER_RUN', 'DOWNLOAD_EXECUTE', 'CACHE_REFRESH', 'FILE_JOB_UPLOAD', 'FILE_JOB_APPLY', 'FILE_JOB_DOWNLOAD') THEN 'Y'
           WHEN ACTION_CODE IN ('READ', 'DETAIL') AND MENU_ID IN ('LOG_LIST', 'REMOTE_LOG', 'TRANSACTION_META', 'AUDIT_LOG', 'BATCH', 'RELIABILITY', 'NOTIFICATION', 'DOWNLOAD', 'CACHE', 'FILE_JOB', 'MESSAGE', 'CODE', 'LOG_POLICY') THEN 'Y'
           ELSE 'N'
       END AS ALLOW_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM ADM_BUTTON) src
ON (tgt.ROLE_ID=src.ROLE_ID AND tgt.BUTTON_ID=src.BUTTON_ID)
WHEN MATCHED THEN UPDATE SET tgt.ALLOW_YN=src.ALLOW_YN, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (ROLE_ID, BUTTON_ID, ALLOW_YN, created_by, updated_by) VALUES (src.ROLE_ID, src.BUTTON_ID, src.ALLOW_YN, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_BUTTON tgt
USING (SELECT 'ADM_VIEWER' AS ROLE_ID, BUTTON_ID AS BUTTON_ID, CASE WHEN ACTION_CODE IN ('READ', 'DETAIL') THEN 'Y' ELSE 'N' END AS ALLOW_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM ADM_BUTTON) src
ON (tgt.ROLE_ID=src.ROLE_ID AND tgt.BUTTON_ID=src.BUTTON_ID)
WHEN MATCHED THEN UPDATE SET tgt.ALLOW_YN=src.ALLOW_YN, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (ROLE_ID, BUTTON_ID, ALLOW_YN, created_by, updated_by) VALUES (src.ROLE_ID, src.BUTTON_ID, src.ALLOW_YN, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_BUTTON tgt
USING (SELECT 'ADM_OPERATOR' AS ROLE_ID, BUTTON_ID AS BUTTON_ID, ALLOW_YN AS ALLOW_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM ADM_ROLE_BUTTON
WHERE ROLE_ID = 'ADM_DEV_OPERATOR') src
ON (tgt.ROLE_ID=src.ROLE_ID AND tgt.BUTTON_ID=src.BUTTON_ID)
WHEN MATCHED THEN UPDATE SET tgt.ALLOW_YN=src.ALLOW_YN, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (ROLE_ID, BUTTON_ID, ALLOW_YN, created_by, updated_by) VALUES (src.ROLE_ID, src.BUTTON_ID, src.ALLOW_YN, src.created_by, src.updated_by);
MERGE INTO ADM_API_PERMISSION tgt
USING (SELECT CONCAT('API_', BUTTON_ID) AS API_PERMISSION_ID, MENU_ID AS API_GROUP_CODE, COALESCE(HTTP_METHOD, 'ANY') AS HTTP_METHOD, API_PATTERN AS API_PATH, BUTTON_NAME AS API_NAME, ACTION_CODE AS PERMISSION_CODE, MENU_ID AS MENU_ID, BUTTON_ID AS BUTTON_ID, USE_YN AS USE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM (
    SELECT b.*,
           ROW_NUMBER() OVER (
               PARTITION BY COALESCE(HTTP_METHOD, 'ANY'), API_PATTERN
               ORDER BY SORT_ORDER, BUTTON_ID
           ) AS CPF_ROUTE_OWNER_RANK
    FROM ADM_BUTTON b
    WHERE API_PATTERN IS NOT NULL
) route_owner
WHERE CPF_ROUTE_OWNER_RANK = 1) src
ON (tgt.API_PERMISSION_ID=src.API_PERMISSION_ID)
WHEN MATCHED THEN UPDATE SET tgt.API_GROUP_CODE=src.API_GROUP_CODE, tgt.HTTP_METHOD=src.HTTP_METHOD, tgt.API_PATH=src.API_PATH, tgt.API_NAME=src.API_NAME, tgt.PERMISSION_CODE=src.PERMISSION_CODE, tgt.MENU_ID=src.MENU_ID, tgt.BUTTON_ID=src.BUTTON_ID, tgt.USE_YN=src.USE_YN, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (API_PERMISSION_ID, API_GROUP_CODE, HTTP_METHOD, API_PATH, API_NAME, PERMISSION_CODE, MENU_ID, BUTTON_ID, USE_YN, created_by, updated_by) VALUES (src.API_PERMISSION_ID, src.API_GROUP_CODE, src.HTTP_METHOD, src.API_PATH, src.API_NAME, src.PERMISSION_CODE, src.MENU_ID, src.BUTTON_ID, src.USE_YN, src.created_by, src.updated_by);
MERGE INTO ADM_API_PERMISSION tgt
USING (SELECT 'API_PERMISSION_WRITE_PUT' AS API_PERMISSION_ID, 'PERMISSION' AS API_GROUP_CODE, 'PUT' AS HTTP_METHOD, '/adm/api/permissions/**' AS API_PATH, '권한 변경' AS API_NAME, 'WRITE' AS PERMISSION_CODE, 'PERMISSION' AS MENU_ID, 'PERMISSION_WRITE' AS BUTTON_ID, 'Y' AS USE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.API_PERMISSION_ID=src.API_PERMISSION_ID)
WHEN MATCHED THEN UPDATE SET tgt.API_GROUP_CODE=src.API_GROUP_CODE, tgt.HTTP_METHOD=src.HTTP_METHOD, tgt.API_PATH=src.API_PATH, tgt.API_NAME=src.API_NAME, tgt.PERMISSION_CODE=src.PERMISSION_CODE, tgt.MENU_ID=src.MENU_ID, tgt.BUTTON_ID=src.BUTTON_ID, tgt.USE_YN=src.USE_YN, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (API_PERMISSION_ID, API_GROUP_CODE, HTTP_METHOD, API_PATH, API_NAME, PERMISSION_CODE, MENU_ID, BUTTON_ID, USE_YN, created_by, updated_by) VALUES (src.API_PERMISSION_ID, src.API_GROUP_CODE, src.HTTP_METHOD, src.API_PATH, src.API_NAME, src.PERMISSION_CODE, src.MENU_ID, src.BUTTON_ID, src.USE_YN, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_API_PERMISSION tgt
USING (SELECT rb.ROLE_ID AS ROLE_ID, ap.API_PERMISSION_ID AS API_PERMISSION_ID, CASE WHEN MAX(rb.ALLOW_YN) = 'Y' THEN 'Y' ELSE 'N' END AS ALLOW_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM ADM_ROLE_BUTTON rb
JOIN ADM_BUTTON b ON b.BUTTON_ID = rb.BUTTON_ID
JOIN ADM_API_PERMISSION ap
  ON ap.HTTP_METHOD = COALESCE(b.HTTP_METHOD, 'ANY')
 AND ap.API_PATH = b.API_PATTERN
WHERE b.API_PATTERN IS NOT NULL
GROUP BY rb.ROLE_ID, ap.API_PERMISSION_ID) src
ON (tgt.ROLE_ID=src.ROLE_ID AND tgt.API_PERMISSION_ID=src.API_PERMISSION_ID)
WHEN MATCHED THEN UPDATE SET tgt.ALLOW_YN=src.ALLOW_YN, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (ROLE_ID, API_PERMISSION_ID, ALLOW_YN, created_by, updated_by) VALUES (src.ROLE_ID, src.API_PERMISSION_ID, src.ALLOW_YN, src.created_by, src.updated_by);
MERGE INTO ADM_BUTTON tgt
USING (SELECT 'AUDIT_LOG_RETRY' AS BUTTON_ID, 'AUDIT_LOG' AS MENU_ID, 'WRITE' AS ACTION_CODE, '감사 전달 재처리' AS BUTTON_NAME, 'POST' AS HTTP_METHOD, '/adm/api/audit-logs/deliveries/*/retry' AS API_PATTERN, 20 AS SORT_ORDER, 'Y' AS USE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.BUTTON_ID=src.BUTTON_ID)
WHEN MATCHED THEN UPDATE SET tgt.ACTION_CODE=src.ACTION_CODE, tgt.BUTTON_NAME=src.BUTTON_NAME, tgt.HTTP_METHOD=src.HTTP_METHOD, tgt.API_PATTERN=src.API_PATTERN, tgt.USE_YN=src.USE_YN, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (BUTTON_ID, MENU_ID, ACTION_CODE, BUTTON_NAME, HTTP_METHOD, API_PATTERN, SORT_ORDER, USE_YN, created_by, updated_by) VALUES (src.BUTTON_ID, src.MENU_ID, src.ACTION_CODE, src.BUTTON_NAME, src.HTTP_METHOD, src.API_PATTERN, src.SORT_ORDER, src.USE_YN, src.created_by, src.updated_by);
UPDATE ADM_ROLE_MENU SET WRITE_YN='Y',updated_by='SYSTEM',updated_at=CURRENT_TIMESTAMP WHERE MENU_ID='AUDIT_LOG' AND ROLE_ID IN ('ADM_ADMIN','ADM_DEV_OPERATOR');
MERGE INTO ADM_ROLE_BUTTON tgt
USING (SELECT ROLE_ID AS ROLE_ID, 'AUDIT_LOG_RETRY' AS BUTTON_ID, 'Y' AS ALLOW_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM ADM_ROLE WHERE ROLE_ID IN ('ADM_ADMIN','ADM_DEV_OPERATOR')) src
ON (tgt.ROLE_ID=src.ROLE_ID AND tgt.BUTTON_ID=src.BUTTON_ID)
WHEN MATCHED THEN UPDATE SET tgt.ALLOW_YN='Y', tgt.updated_by='SYSTEM', tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (ROLE_ID, BUTTON_ID, ALLOW_YN, created_by, updated_by) VALUES (src.ROLE_ID, src.BUTTON_ID, src.ALLOW_YN, src.created_by, src.updated_by);
MERGE INTO ADM_API_PERMISSION tgt
USING (SELECT 'API_AUDIT_LOG_RETRY' AS API_PERMISSION_ID, 'AUDIT_LOG' AS API_GROUP_CODE, 'POST' AS HTTP_METHOD, '/adm/api/audit-logs/deliveries/*/retry' AS API_PATH, '감사 전달 재처리' AS API_NAME, 'WRITE' AS PERMISSION_CODE, 'AUDIT_LOG' AS MENU_ID, 'AUDIT_LOG_RETRY' AS BUTTON_ID, 'Y' AS USE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.API_PERMISSION_ID=src.API_PERMISSION_ID)
WHEN MATCHED THEN UPDATE SET tgt.HTTP_METHOD=src.HTTP_METHOD, tgt.API_PATH=src.API_PATH, tgt.PERMISSION_CODE=src.PERMISSION_CODE, tgt.BUTTON_ID=src.BUTTON_ID, tgt.USE_YN=src.USE_YN, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (API_PERMISSION_ID, API_GROUP_CODE, HTTP_METHOD, API_PATH, API_NAME, PERMISSION_CODE, MENU_ID, BUTTON_ID, USE_YN, created_by, updated_by) VALUES (src.API_PERMISSION_ID, src.API_GROUP_CODE, src.HTTP_METHOD, src.API_PATH, src.API_NAME, src.PERMISSION_CODE, src.MENU_ID, src.BUTTON_ID, src.USE_YN, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_API_PERMISSION tgt
USING (SELECT ROLE_ID AS ROLE_ID, 'API_AUDIT_LOG_RETRY' AS API_PERMISSION_ID, 'Y' AS ALLOW_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM ADM_ROLE WHERE ROLE_ID IN ('ADM_ADMIN','ADM_DEV_OPERATOR')) src
ON (tgt.ROLE_ID=src.ROLE_ID AND tgt.API_PERMISSION_ID=src.API_PERMISSION_ID)
WHEN MATCHED THEN UPDATE SET tgt.ALLOW_YN='Y', tgt.updated_by='SYSTEM', tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (ROLE_ID, API_PERMISSION_ID, ALLOW_YN, created_by, updated_by) VALUES (src.ROLE_ID, src.API_PERMISSION_ID, src.ALLOW_YN, src.created_by, src.updated_by);
MERGE INTO ADM_BUTTON tgt
USING (SELECT 'SECRET_READ' AS BUTTON_ID, 'SECRET' AS MENU_ID, 'READ' AS ACTION_CODE, 'Secret Metadata 조회' AS BUTTON_NAME, 'GET' AS HTTP_METHOD, '/adm/api/secrets/**' AS API_PATTERN, 10 AS SORT_ORDER, 'Y' AS USE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.BUTTON_ID=src.BUTTON_ID)
WHEN MATCHED THEN UPDATE SET tgt.ACTION_CODE=src.ACTION_CODE, tgt.BUTTON_NAME=src.BUTTON_NAME, tgt.HTTP_METHOD=src.HTTP_METHOD, tgt.API_PATTERN=src.API_PATTERN, tgt.USE_YN='Y', tgt.updated_by='SYSTEM', tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (BUTTON_ID, MENU_ID, ACTION_CODE, BUTTON_NAME, HTTP_METHOD, API_PATTERN, SORT_ORDER, USE_YN, created_by, updated_by) VALUES (src.BUTTON_ID, src.MENU_ID, src.ACTION_CODE, src.BUTTON_NAME, src.HTTP_METHOD, src.API_PATTERN, src.SORT_ORDER, src.USE_YN, src.created_by, src.updated_by);
MERGE INTO ADM_BUTTON tgt
USING (SELECT 'SECRET_ROTATE' AS BUTTON_ID, 'SECRET' AS MENU_ID, 'ROTATE' AS ACTION_CODE, 'Secret Rotation' AS BUTTON_NAME, 'POST' AS HTTP_METHOD, '/adm/api/secrets/rotate' AS API_PATTERN, 20 AS SORT_ORDER, 'Y' AS USE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.BUTTON_ID=src.BUTTON_ID)
WHEN MATCHED THEN UPDATE SET tgt.ACTION_CODE=src.ACTION_CODE, tgt.BUTTON_NAME=src.BUTTON_NAME, tgt.HTTP_METHOD=src.HTTP_METHOD, tgt.API_PATTERN=src.API_PATTERN, tgt.USE_YN='Y', tgt.updated_by='SYSTEM', tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (BUTTON_ID, MENU_ID, ACTION_CODE, BUTTON_NAME, HTTP_METHOD, API_PATTERN, SORT_ORDER, USE_YN, created_by, updated_by) VALUES (src.BUTTON_ID, src.MENU_ID, src.ACTION_CODE, src.BUTTON_NAME, src.HTTP_METHOD, src.API_PATTERN, src.SORT_ORDER, src.USE_YN, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_MENU tgt
USING (SELECT 'ADM_ADMIN' AS ROLE_ID, 'SECRET' AS MENU_ID, 'Y' AS READ_YN, 'Y' AS WRITE_YN, 'N' AS DELETE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.ROLE_ID=src.ROLE_ID AND tgt.MENU_ID=src.MENU_ID)
WHEN MATCHED THEN UPDATE SET tgt.READ_YN=src.READ_YN, tgt.WRITE_YN=src.WRITE_YN, tgt.DELETE_YN=src.DELETE_YN, tgt.updated_by='SYSTEM', tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (ROLE_ID, MENU_ID, READ_YN, WRITE_YN, DELETE_YN, created_by, updated_by) VALUES (src.ROLE_ID, src.MENU_ID, src.READ_YN, src.WRITE_YN, src.DELETE_YN, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_MENU tgt
USING (SELECT 'ADM_DEV_OPERATOR' AS ROLE_ID, 'SECRET' AS MENU_ID, 'Y' AS READ_YN, 'N' AS WRITE_YN, 'N' AS DELETE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.ROLE_ID=src.ROLE_ID AND tgt.MENU_ID=src.MENU_ID)
WHEN MATCHED THEN UPDATE SET tgt.READ_YN=src.READ_YN, tgt.WRITE_YN=src.WRITE_YN, tgt.DELETE_YN=src.DELETE_YN, tgt.updated_by='SYSTEM', tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (ROLE_ID, MENU_ID, READ_YN, WRITE_YN, DELETE_YN, created_by, updated_by) VALUES (src.ROLE_ID, src.MENU_ID, src.READ_YN, src.WRITE_YN, src.DELETE_YN, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_MENU tgt
USING (SELECT 'ADM_OPERATOR' AS ROLE_ID, 'SECRET' AS MENU_ID, 'Y' AS READ_YN, 'N' AS WRITE_YN, 'N' AS DELETE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.ROLE_ID=src.ROLE_ID AND tgt.MENU_ID=src.MENU_ID)
WHEN MATCHED THEN UPDATE SET tgt.READ_YN=src.READ_YN, tgt.WRITE_YN=src.WRITE_YN, tgt.DELETE_YN=src.DELETE_YN, tgt.updated_by='SYSTEM', tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (ROLE_ID, MENU_ID, READ_YN, WRITE_YN, DELETE_YN, created_by, updated_by) VALUES (src.ROLE_ID, src.MENU_ID, src.READ_YN, src.WRITE_YN, src.DELETE_YN, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_MENU tgt
USING (SELECT 'ADM_VIEWER' AS ROLE_ID, 'SECRET' AS MENU_ID, 'N' AS READ_YN, 'N' AS WRITE_YN, 'N' AS DELETE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.ROLE_ID=src.ROLE_ID AND tgt.MENU_ID=src.MENU_ID)
WHEN MATCHED THEN UPDATE SET tgt.READ_YN=src.READ_YN, tgt.WRITE_YN=src.WRITE_YN, tgt.DELETE_YN=src.DELETE_YN, tgt.updated_by='SYSTEM', tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (ROLE_ID, MENU_ID, READ_YN, WRITE_YN, DELETE_YN, created_by, updated_by) VALUES (src.ROLE_ID, src.MENU_ID, src.READ_YN, src.WRITE_YN, src.DELETE_YN, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_MENU tgt
USING (SELECT 'ADM_BIZ_OPERATOR' AS ROLE_ID, 'SECRET' AS MENU_ID, 'N' AS READ_YN, 'N' AS WRITE_YN, 'N' AS DELETE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.ROLE_ID=src.ROLE_ID AND tgt.MENU_ID=src.MENU_ID)
WHEN MATCHED THEN UPDATE SET tgt.READ_YN=src.READ_YN, tgt.WRITE_YN=src.WRITE_YN, tgt.DELETE_YN=src.DELETE_YN, tgt.updated_by='SYSTEM', tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (ROLE_ID, MENU_ID, READ_YN, WRITE_YN, DELETE_YN, created_by, updated_by) VALUES (src.ROLE_ID, src.MENU_ID, src.READ_YN, src.WRITE_YN, src.DELETE_YN, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_BUTTON tgt
USING (SELECT 'ADM_ADMIN' AS ROLE_ID, 'SECRET_READ' AS BUTTON_ID, 'Y' AS ALLOW_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.ROLE_ID=src.ROLE_ID AND tgt.BUTTON_ID=src.BUTTON_ID)
WHEN MATCHED THEN UPDATE SET tgt.ALLOW_YN=src.ALLOW_YN, tgt.updated_by='SYSTEM', tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (ROLE_ID, BUTTON_ID, ALLOW_YN, created_by, updated_by) VALUES (src.ROLE_ID, src.BUTTON_ID, src.ALLOW_YN, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_BUTTON tgt
USING (SELECT 'ADM_ADMIN' AS ROLE_ID, 'SECRET_ROTATE' AS BUTTON_ID, 'Y' AS ALLOW_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.ROLE_ID=src.ROLE_ID AND tgt.BUTTON_ID=src.BUTTON_ID)
WHEN MATCHED THEN UPDATE SET tgt.ALLOW_YN=src.ALLOW_YN, tgt.updated_by='SYSTEM', tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (ROLE_ID, BUTTON_ID, ALLOW_YN, created_by, updated_by) VALUES (src.ROLE_ID, src.BUTTON_ID, src.ALLOW_YN, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_BUTTON tgt
USING (SELECT 'ADM_DEV_OPERATOR' AS ROLE_ID, 'SECRET_READ' AS BUTTON_ID, 'Y' AS ALLOW_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.ROLE_ID=src.ROLE_ID AND tgt.BUTTON_ID=src.BUTTON_ID)
WHEN MATCHED THEN UPDATE SET tgt.ALLOW_YN=src.ALLOW_YN, tgt.updated_by='SYSTEM', tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (ROLE_ID, BUTTON_ID, ALLOW_YN, created_by, updated_by) VALUES (src.ROLE_ID, src.BUTTON_ID, src.ALLOW_YN, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_BUTTON tgt
USING (SELECT 'ADM_DEV_OPERATOR' AS ROLE_ID, 'SECRET_ROTATE' AS BUTTON_ID, 'N' AS ALLOW_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.ROLE_ID=src.ROLE_ID AND tgt.BUTTON_ID=src.BUTTON_ID)
WHEN MATCHED THEN UPDATE SET tgt.ALLOW_YN=src.ALLOW_YN, tgt.updated_by='SYSTEM', tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (ROLE_ID, BUTTON_ID, ALLOW_YN, created_by, updated_by) VALUES (src.ROLE_ID, src.BUTTON_ID, src.ALLOW_YN, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_BUTTON tgt
USING (SELECT 'ADM_OPERATOR' AS ROLE_ID, 'SECRET_READ' AS BUTTON_ID, 'Y' AS ALLOW_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.ROLE_ID=src.ROLE_ID AND tgt.BUTTON_ID=src.BUTTON_ID)
WHEN MATCHED THEN UPDATE SET tgt.ALLOW_YN=src.ALLOW_YN, tgt.updated_by='SYSTEM', tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (ROLE_ID, BUTTON_ID, ALLOW_YN, created_by, updated_by) VALUES (src.ROLE_ID, src.BUTTON_ID, src.ALLOW_YN, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_BUTTON tgt
USING (SELECT 'ADM_OPERATOR' AS ROLE_ID, 'SECRET_ROTATE' AS BUTTON_ID, 'N' AS ALLOW_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.ROLE_ID=src.ROLE_ID AND tgt.BUTTON_ID=src.BUTTON_ID)
WHEN MATCHED THEN UPDATE SET tgt.ALLOW_YN=src.ALLOW_YN, tgt.updated_by='SYSTEM', tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (ROLE_ID, BUTTON_ID, ALLOW_YN, created_by, updated_by) VALUES (src.ROLE_ID, src.BUTTON_ID, src.ALLOW_YN, src.created_by, src.updated_by);
MERGE INTO ADM_API_PERMISSION tgt
USING (SELECT 'API_SECRET_READ' AS API_PERMISSION_ID, 'SECRET' AS API_GROUP_CODE, 'GET' AS HTTP_METHOD, '/adm/api/secrets/**' AS API_PATH, 'Secret Metadata 조회' AS API_NAME, 'READ' AS PERMISSION_CODE, 'SECRET' AS MENU_ID, 'SECRET_READ' AS BUTTON_ID, 'Y' AS USE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.API_PERMISSION_ID=src.API_PERMISSION_ID)
WHEN MATCHED THEN UPDATE SET tgt.API_PATH=src.API_PATH, tgt.API_NAME=src.API_NAME, tgt.PERMISSION_CODE=src.PERMISSION_CODE, tgt.BUTTON_ID=src.BUTTON_ID, tgt.USE_YN='Y', tgt.updated_by='SYSTEM', tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (API_PERMISSION_ID, API_GROUP_CODE, HTTP_METHOD, API_PATH, API_NAME, PERMISSION_CODE, MENU_ID, BUTTON_ID, USE_YN, created_by, updated_by) VALUES (src.API_PERMISSION_ID, src.API_GROUP_CODE, src.HTTP_METHOD, src.API_PATH, src.API_NAME, src.PERMISSION_CODE, src.MENU_ID, src.BUTTON_ID, src.USE_YN, src.created_by, src.updated_by);
MERGE INTO ADM_API_PERMISSION tgt
USING (SELECT 'API_SECRET_ROTATE' AS API_PERMISSION_ID, 'SECRET' AS API_GROUP_CODE, 'POST' AS HTTP_METHOD, '/adm/api/secrets/rotate' AS API_PATH, 'Secret Rotation' AS API_NAME, 'ROTATE' AS PERMISSION_CODE, 'SECRET' AS MENU_ID, 'SECRET_ROTATE' AS BUTTON_ID, 'Y' AS USE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.API_PERMISSION_ID=src.API_PERMISSION_ID)
WHEN MATCHED THEN UPDATE SET tgt.API_PATH=src.API_PATH, tgt.API_NAME=src.API_NAME, tgt.PERMISSION_CODE=src.PERMISSION_CODE, tgt.BUTTON_ID=src.BUTTON_ID, tgt.USE_YN='Y', tgt.updated_by='SYSTEM', tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (API_PERMISSION_ID, API_GROUP_CODE, HTTP_METHOD, API_PATH, API_NAME, PERMISSION_CODE, MENU_ID, BUTTON_ID, USE_YN, created_by, updated_by) VALUES (src.API_PERMISSION_ID, src.API_GROUP_CODE, src.HTTP_METHOD, src.API_PATH, src.API_NAME, src.PERMISSION_CODE, src.MENU_ID, src.BUTTON_ID, src.USE_YN, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_API_PERMISSION tgt
USING (SELECT 'ADM_ADMIN' AS ROLE_ID, 'API_SECRET_READ' AS API_PERMISSION_ID, 'Y' AS ALLOW_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.ROLE_ID=src.ROLE_ID AND tgt.API_PERMISSION_ID=src.API_PERMISSION_ID)
WHEN MATCHED THEN UPDATE SET tgt.ALLOW_YN=src.ALLOW_YN, tgt.updated_by='SYSTEM', tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (ROLE_ID, API_PERMISSION_ID, ALLOW_YN, created_by, updated_by) VALUES (src.ROLE_ID, src.API_PERMISSION_ID, src.ALLOW_YN, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_API_PERMISSION tgt
USING (SELECT 'ADM_ADMIN' AS ROLE_ID, 'API_SECRET_ROTATE' AS API_PERMISSION_ID, 'Y' AS ALLOW_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.ROLE_ID=src.ROLE_ID AND tgt.API_PERMISSION_ID=src.API_PERMISSION_ID)
WHEN MATCHED THEN UPDATE SET tgt.ALLOW_YN=src.ALLOW_YN, tgt.updated_by='SYSTEM', tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (ROLE_ID, API_PERMISSION_ID, ALLOW_YN, created_by, updated_by) VALUES (src.ROLE_ID, src.API_PERMISSION_ID, src.ALLOW_YN, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_API_PERMISSION tgt
USING (SELECT 'ADM_DEV_OPERATOR' AS ROLE_ID, 'API_SECRET_READ' AS API_PERMISSION_ID, 'Y' AS ALLOW_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.ROLE_ID=src.ROLE_ID AND tgt.API_PERMISSION_ID=src.API_PERMISSION_ID)
WHEN MATCHED THEN UPDATE SET tgt.ALLOW_YN=src.ALLOW_YN, tgt.updated_by='SYSTEM', tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (ROLE_ID, API_PERMISSION_ID, ALLOW_YN, created_by, updated_by) VALUES (src.ROLE_ID, src.API_PERMISSION_ID, src.ALLOW_YN, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_API_PERMISSION tgt
USING (SELECT 'ADM_DEV_OPERATOR' AS ROLE_ID, 'API_SECRET_ROTATE' AS API_PERMISSION_ID, 'N' AS ALLOW_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.ROLE_ID=src.ROLE_ID AND tgt.API_PERMISSION_ID=src.API_PERMISSION_ID)
WHEN MATCHED THEN UPDATE SET tgt.ALLOW_YN=src.ALLOW_YN, tgt.updated_by='SYSTEM', tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (ROLE_ID, API_PERMISSION_ID, ALLOW_YN, created_by, updated_by) VALUES (src.ROLE_ID, src.API_PERMISSION_ID, src.ALLOW_YN, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_API_PERMISSION tgt
USING (SELECT 'ADM_OPERATOR' AS ROLE_ID, 'API_SECRET_READ' AS API_PERMISSION_ID, 'Y' AS ALLOW_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.ROLE_ID=src.ROLE_ID AND tgt.API_PERMISSION_ID=src.API_PERMISSION_ID)
WHEN MATCHED THEN UPDATE SET tgt.ALLOW_YN=src.ALLOW_YN, tgt.updated_by='SYSTEM', tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (ROLE_ID, API_PERMISSION_ID, ALLOW_YN, created_by, updated_by) VALUES (src.ROLE_ID, src.API_PERMISSION_ID, src.ALLOW_YN, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_API_PERMISSION tgt
USING (SELECT 'ADM_OPERATOR' AS ROLE_ID, 'API_SECRET_ROTATE' AS API_PERMISSION_ID, 'N' AS ALLOW_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.ROLE_ID=src.ROLE_ID AND tgt.API_PERMISSION_ID=src.API_PERMISSION_ID)
WHEN MATCHED THEN UPDATE SET tgt.ALLOW_YN=src.ALLOW_YN, tgt.updated_by='SYSTEM', tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (ROLE_ID, API_PERMISSION_ID, ALLOW_YN, created_by, updated_by) VALUES (src.ROLE_ID, src.API_PERMISSION_ID, src.ALLOW_YN, src.created_by, src.updated_by);
MERGE INTO ADM_BUTTON tgt
USING (SELECT 'BAT_RUNTIME_VIEW' AS BUTTON_ID, 'BATCH_RUNTIME' AS MENU_ID, 'RUNTIME_VIEW' AS ACTION_CODE, 'Runtime 조회' AS BUTTON_NAME, 'GET' AS HTTP_METHOD, '/adm/api/batch-runtime/**' AS API_PATTERN, 10 AS SORT_ORDER, 'Y' AS USE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.BUTTON_ID=src.BUTTON_ID)
WHEN MATCHED THEN UPDATE SET tgt.MENU_ID=src.MENU_ID, tgt.ACTION_CODE=src.ACTION_CODE, tgt.BUTTON_NAME=src.BUTTON_NAME, tgt.HTTP_METHOD=src.HTTP_METHOD, tgt.API_PATTERN=src.API_PATTERN, tgt.SORT_ORDER=src.SORT_ORDER, tgt.USE_YN='Y', tgt.updated_by='SYSTEM', tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (BUTTON_ID, MENU_ID, ACTION_CODE, BUTTON_NAME, HTTP_METHOD, API_PATTERN, SORT_ORDER, USE_YN, created_by, updated_by) VALUES (src.BUTTON_ID, src.MENU_ID, src.ACTION_CODE, src.BUTTON_NAME, src.HTTP_METHOD, src.API_PATTERN, src.SORT_ORDER, src.USE_YN, src.created_by, src.updated_by);
MERGE INTO ADM_BUTTON tgt
USING (SELECT 'BAT_RUNTIME_OPERATE' AS BUTTON_ID, 'BATCH_INSTANCES' AS MENU_ID, 'RUNTIME_OPERATE' AS ACTION_CODE, 'Runtime Start/Stop/Drain' AS BUTTON_NAME, 'POST' AS HTTP_METHOD, '/adm/api/approvals/**' AS API_PATTERN, 20 AS SORT_ORDER, 'Y' AS USE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.BUTTON_ID=src.BUTTON_ID)
WHEN MATCHED THEN UPDATE SET tgt.MENU_ID=src.MENU_ID, tgt.ACTION_CODE=src.ACTION_CODE, tgt.BUTTON_NAME=src.BUTTON_NAME, tgt.HTTP_METHOD=src.HTTP_METHOD, tgt.API_PATTERN=src.API_PATTERN, tgt.SORT_ORDER=src.SORT_ORDER, tgt.USE_YN='Y', tgt.updated_by='SYSTEM', tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (BUTTON_ID, MENU_ID, ACTION_CODE, BUTTON_NAME, HTTP_METHOD, API_PATTERN, SORT_ORDER, USE_YN, created_by, updated_by) VALUES (src.BUTTON_ID, src.MENU_ID, src.ACTION_CODE, src.BUTTON_NAME, src.HTTP_METHOD, src.API_PATTERN, src.SORT_ORDER, src.USE_YN, src.created_by, src.updated_by);
MERGE INTO ADM_BUTTON tgt
USING (SELECT 'BAT_JOB_OPERATE' AS BUTTON_ID, 'BATCH_EXECUTIONS' AS MENU_ID, 'JOB_OPERATE' AS ACTION_CODE, 'Job 실행/중지/재처리' AS BUTTON_NAME, 'POST' AS HTTP_METHOD, '/adm/api/batch/**' AS API_PATTERN, 30 AS SORT_ORDER, 'Y' AS USE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.BUTTON_ID=src.BUTTON_ID)
WHEN MATCHED THEN UPDATE SET tgt.MENU_ID=src.MENU_ID, tgt.ACTION_CODE=src.ACTION_CODE, tgt.BUTTON_NAME=src.BUTTON_NAME, tgt.HTTP_METHOD=src.HTTP_METHOD, tgt.API_PATTERN=src.API_PATTERN, tgt.SORT_ORDER=src.SORT_ORDER, tgt.USE_YN='Y', tgt.updated_by='SYSTEM', tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (BUTTON_ID, MENU_ID, ACTION_CODE, BUTTON_NAME, HTTP_METHOD, API_PATTERN, SORT_ORDER, USE_YN, created_by, updated_by) VALUES (src.BUTTON_ID, src.MENU_ID, src.ACTION_CODE, src.BUTTON_NAME, src.HTTP_METHOD, src.API_PATTERN, src.SORT_ORDER, src.USE_YN, src.created_by, src.updated_by);
MERGE INTO ADM_BUTTON tgt
USING (SELECT 'BAT_SCHEDULE_OPERATE' AS BUTTON_ID, 'BATCH_SCHEDULER' AS MENU_ID, 'SCHEDULE_OPERATE' AS ACTION_CODE, 'Scheduler 운영' AS BUTTON_NAME, 'POST' AS HTTP_METHOD, '/adm/api/batch/**' AS API_PATTERN, 40 AS SORT_ORDER, 'Y' AS USE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.BUTTON_ID=src.BUTTON_ID)
WHEN MATCHED THEN UPDATE SET tgt.MENU_ID=src.MENU_ID, tgt.ACTION_CODE=src.ACTION_CODE, tgt.BUTTON_NAME=src.BUTTON_NAME, tgt.HTTP_METHOD=src.HTTP_METHOD, tgt.API_PATTERN=src.API_PATTERN, tgt.SORT_ORDER=src.SORT_ORDER, tgt.USE_YN='Y', tgt.updated_by='SYSTEM', tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (BUTTON_ID, MENU_ID, ACTION_CODE, BUTTON_NAME, HTTP_METHOD, API_PATTERN, SORT_ORDER, USE_YN, created_by, updated_by) VALUES (src.BUTTON_ID, src.MENU_ID, src.ACTION_CODE, src.BUTTON_NAME, src.HTTP_METHOD, src.API_PATTERN, src.SORT_ORDER, src.USE_YN, src.created_by, src.updated_by);
MERGE INTO ADM_BUTTON tgt
USING (SELECT 'BAT_WORKER_OPERATE' AS BUTTON_ID, 'BATCH_WORKER_POOLS' AS MENU_ID, 'WORKER_OPERATE' AS ACTION_CODE, 'Worker Pool 운영' AS BUTTON_NAME, 'POST' AS HTTP_METHOD, '/adm/api/approvals/**' AS API_PATTERN, 50 AS SORT_ORDER, 'Y' AS USE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.BUTTON_ID=src.BUTTON_ID)
WHEN MATCHED THEN UPDATE SET tgt.MENU_ID=src.MENU_ID, tgt.ACTION_CODE=src.ACTION_CODE, tgt.BUTTON_NAME=src.BUTTON_NAME, tgt.HTTP_METHOD=src.HTTP_METHOD, tgt.API_PATTERN=src.API_PATTERN, tgt.SORT_ORDER=src.SORT_ORDER, tgt.USE_YN='Y', tgt.updated_by='SYSTEM', tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (BUTTON_ID, MENU_ID, ACTION_CODE, BUTTON_NAME, HTTP_METHOD, API_PATTERN, SORT_ORDER, USE_YN, created_by, updated_by) VALUES (src.BUTTON_ID, src.MENU_ID, src.ACTION_CODE, src.BUTTON_NAME, src.HTTP_METHOD, src.API_PATTERN, src.SORT_ORDER, src.USE_YN, src.created_by, src.updated_by);
MERGE INTO ADM_BUTTON tgt
USING (SELECT 'BAT_CENTER_CUT_OPERATE' AS BUTTON_ID, 'BATCH_CENTER_CUT' AS MENU_ID, 'CENTER_CUT_OPERATE' AS ACTION_CODE, 'Center-Cut 재처리/조정' AS BUTTON_NAME, 'POST' AS HTTP_METHOD, '/adm/api/batch-runtime/**' AS API_PATTERN, 60 AS SORT_ORDER, 'Y' AS USE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.BUTTON_ID=src.BUTTON_ID)
WHEN MATCHED THEN UPDATE SET tgt.MENU_ID=src.MENU_ID, tgt.ACTION_CODE=src.ACTION_CODE, tgt.BUTTON_NAME=src.BUTTON_NAME, tgt.HTTP_METHOD=src.HTTP_METHOD, tgt.API_PATTERN=src.API_PATTERN, tgt.SORT_ORDER=src.SORT_ORDER, tgt.USE_YN='Y', tgt.updated_by='SYSTEM', tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (BUTTON_ID, MENU_ID, ACTION_CODE, BUTTON_NAME, HTTP_METHOD, API_PATTERN, SORT_ORDER, USE_YN, created_by, updated_by) VALUES (src.BUTTON_ID, src.MENU_ID, src.ACTION_CODE, src.BUTTON_NAME, src.HTTP_METHOD, src.API_PATTERN, src.SORT_ORDER, src.USE_YN, src.created_by, src.updated_by);
MERGE INTO ADM_BUTTON tgt
USING (SELECT 'BAT_AGENT_OPERATE' AS BUTTON_ID, 'BATCH_AGENTS' AS MENU_ID, 'AGENT_OPERATE' AS ACTION_CODE, 'Host Agent 운영' AS BUTTON_NAME, 'POST' AS HTTP_METHOD, '/adm/api/approvals/**' AS API_PATTERN, 70 AS SORT_ORDER, 'Y' AS USE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.BUTTON_ID=src.BUTTON_ID)
WHEN MATCHED THEN UPDATE SET tgt.MENU_ID=src.MENU_ID, tgt.ACTION_CODE=src.ACTION_CODE, tgt.BUTTON_NAME=src.BUTTON_NAME, tgt.HTTP_METHOD=src.HTTP_METHOD, tgt.API_PATTERN=src.API_PATTERN, tgt.SORT_ORDER=src.SORT_ORDER, tgt.USE_YN='Y', tgt.updated_by='SYSTEM', tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (BUTTON_ID, MENU_ID, ACTION_CODE, BUTTON_NAME, HTTP_METHOD, API_PATTERN, SORT_ORDER, USE_YN, created_by, updated_by) VALUES (src.BUTTON_ID, src.MENU_ID, src.ACTION_CODE, src.BUTTON_NAME, src.HTTP_METHOD, src.API_PATTERN, src.SORT_ORDER, src.USE_YN, src.created_by, src.updated_by);
MERGE INTO ADM_BUTTON tgt
USING (SELECT 'BAT_DEPLOY_PLAN' AS BUTTON_ID, 'BATCH_DEPLOYMENT' AS MENU_ID, 'DEPLOY_PLAN' AS ACTION_CODE, 'Deployment Plan 생성' AS BUTTON_NAME, 'POST' AS HTTP_METHOD, '/adm/api/batch-runtime/deployment-plans' AS API_PATTERN, 80 AS SORT_ORDER, 'Y' AS USE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.BUTTON_ID=src.BUTTON_ID)
WHEN MATCHED THEN UPDATE SET tgt.MENU_ID=src.MENU_ID, tgt.ACTION_CODE=src.ACTION_CODE, tgt.BUTTON_NAME=src.BUTTON_NAME, tgt.HTTP_METHOD=src.HTTP_METHOD, tgt.API_PATTERN=src.API_PATTERN, tgt.SORT_ORDER=src.SORT_ORDER, tgt.USE_YN='Y', tgt.updated_by='SYSTEM', tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (BUTTON_ID, MENU_ID, ACTION_CODE, BUTTON_NAME, HTTP_METHOD, API_PATTERN, SORT_ORDER, USE_YN, created_by, updated_by) VALUES (src.BUTTON_ID, src.MENU_ID, src.ACTION_CODE, src.BUTTON_NAME, src.HTTP_METHOD, src.API_PATTERN, src.SORT_ORDER, src.USE_YN, src.created_by, src.updated_by);
MERGE INTO ADM_BUTTON tgt
USING (SELECT 'BAT_DEPLOY_APPROVE' AS BUTTON_ID, 'BATCH_DEPLOYMENT' AS MENU_ID, 'DEPLOY_APPROVE' AS ACTION_CODE, 'Deployment 승인' AS BUTTON_NAME, 'POST' AS HTTP_METHOD, '/adm/api/approvals/**' AS API_PATTERN, 90 AS SORT_ORDER, 'Y' AS USE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.BUTTON_ID=src.BUTTON_ID)
WHEN MATCHED THEN UPDATE SET tgt.MENU_ID=src.MENU_ID, tgt.ACTION_CODE=src.ACTION_CODE, tgt.BUTTON_NAME=src.BUTTON_NAME, tgt.HTTP_METHOD=src.HTTP_METHOD, tgt.API_PATTERN=src.API_PATTERN, tgt.SORT_ORDER=src.SORT_ORDER, tgt.USE_YN='Y', tgt.updated_by='SYSTEM', tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (BUTTON_ID, MENU_ID, ACTION_CODE, BUTTON_NAME, HTTP_METHOD, API_PATTERN, SORT_ORDER, USE_YN, created_by, updated_by) VALUES (src.BUTTON_ID, src.MENU_ID, src.ACTION_CODE, src.BUTTON_NAME, src.HTTP_METHOD, src.API_PATTERN, src.SORT_ORDER, src.USE_YN, src.created_by, src.updated_by);
MERGE INTO ADM_BUTTON tgt
USING (SELECT 'BAT_DEPLOY_EXECUTE' AS BUTTON_ID, 'BATCH_DEPLOYMENT' AS MENU_ID, 'DEPLOY_EXECUTE' AS ACTION_CODE, 'Deployment 실행' AS BUTTON_NAME, 'POST' AS HTTP_METHOD, '/adm/api/approvals/**' AS API_PATTERN, 100 AS SORT_ORDER, 'Y' AS USE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.BUTTON_ID=src.BUTTON_ID)
WHEN MATCHED THEN UPDATE SET tgt.MENU_ID=src.MENU_ID, tgt.ACTION_CODE=src.ACTION_CODE, tgt.BUTTON_NAME=src.BUTTON_NAME, tgt.HTTP_METHOD=src.HTTP_METHOD, tgt.API_PATTERN=src.API_PATTERN, tgt.SORT_ORDER=src.SORT_ORDER, tgt.USE_YN='Y', tgt.updated_by='SYSTEM', tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (BUTTON_ID, MENU_ID, ACTION_CODE, BUTTON_NAME, HTTP_METHOD, API_PATTERN, SORT_ORDER, USE_YN, created_by, updated_by) VALUES (src.BUTTON_ID, src.MENU_ID, src.ACTION_CODE, src.BUTTON_NAME, src.HTTP_METHOD, src.API_PATTERN, src.SORT_ORDER, src.USE_YN, src.created_by, src.updated_by);
MERGE INTO ADM_BUTTON tgt
USING (SELECT 'BAT_ROLLBACK_EXECUTE' AS BUTTON_ID, 'BATCH_DEPLOYMENT' AS MENU_ID, 'ROLLBACK_EXECUTE' AS ACTION_CODE, 'Rollback 실행' AS BUTTON_NAME, 'POST' AS HTTP_METHOD, '/adm/api/approvals/**' AS API_PATTERN, 110 AS SORT_ORDER, 'Y' AS USE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.BUTTON_ID=src.BUTTON_ID)
WHEN MATCHED THEN UPDATE SET tgt.MENU_ID=src.MENU_ID, tgt.ACTION_CODE=src.ACTION_CODE, tgt.BUTTON_NAME=src.BUTTON_NAME, tgt.HTTP_METHOD=src.HTTP_METHOD, tgt.API_PATTERN=src.API_PATTERN, tgt.SORT_ORDER=src.SORT_ORDER, tgt.USE_YN='Y', tgt.updated_by='SYSTEM', tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (BUTTON_ID, MENU_ID, ACTION_CODE, BUTTON_NAME, HTTP_METHOD, API_PATTERN, SORT_ORDER, USE_YN, created_by, updated_by) VALUES (src.BUTTON_ID, src.MENU_ID, src.ACTION_CODE, src.BUTTON_NAME, src.HTTP_METHOD, src.API_PATTERN, src.SORT_ORDER, src.USE_YN, src.created_by, src.updated_by);
MERGE INTO ADM_BUTTON tgt
USING (SELECT 'BAT_RECOVERY_OPERATE' AS BUTTON_ID, 'BATCH_RECOVERY' AS MENU_ID, 'RECOVERY_OPERATE' AS ACTION_CODE, 'UNKNOWN_RESULT 조정' AS BUTTON_NAME, 'POST' AS HTTP_METHOD, '/adm/api/batch-runtime/**' AS API_PATTERN, 120 AS SORT_ORDER, 'Y' AS USE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.BUTTON_ID=src.BUTTON_ID)
WHEN MATCHED THEN UPDATE SET tgt.MENU_ID=src.MENU_ID, tgt.ACTION_CODE=src.ACTION_CODE, tgt.BUTTON_NAME=src.BUTTON_NAME, tgt.HTTP_METHOD=src.HTTP_METHOD, tgt.API_PATTERN=src.API_PATTERN, tgt.SORT_ORDER=src.SORT_ORDER, tgt.USE_YN='Y', tgt.updated_by='SYSTEM', tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (BUTTON_ID, MENU_ID, ACTION_CODE, BUTTON_NAME, HTTP_METHOD, API_PATTERN, SORT_ORDER, USE_YN, created_by, updated_by) VALUES (src.BUTTON_ID, src.MENU_ID, src.ACTION_CODE, src.BUTTON_NAME, src.HTTP_METHOD, src.API_PATTERN, src.SORT_ORDER, src.USE_YN, src.created_by, src.updated_by);
MERGE INTO ADM_BUTTON tgt
USING (SELECT 'BAT_SECURITY_AUDIT' AS BUTTON_ID, 'BATCH_AUDIT' AS MENU_ID, 'SECURITY_AUDIT' AS ACTION_CODE, 'BAT 보안·감사 조회' AS BUTTON_NAME, 'GET' AS HTTP_METHOD, '/adm/api/batch-runtime/views/audit' AS API_PATTERN, 130 AS SORT_ORDER, 'Y' AS USE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.BUTTON_ID=src.BUTTON_ID)
WHEN MATCHED THEN UPDATE SET tgt.MENU_ID=src.MENU_ID, tgt.ACTION_CODE=src.ACTION_CODE, tgt.BUTTON_NAME=src.BUTTON_NAME, tgt.HTTP_METHOD=src.HTTP_METHOD, tgt.API_PATTERN=src.API_PATTERN, tgt.SORT_ORDER=src.SORT_ORDER, tgt.USE_YN='Y', tgt.updated_by='SYSTEM', tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (BUTTON_ID, MENU_ID, ACTION_CODE, BUTTON_NAME, HTTP_METHOD, API_PATTERN, SORT_ORDER, USE_YN, created_by, updated_by) VALUES (src.BUTTON_ID, src.MENU_ID, src.ACTION_CODE, src.BUTTON_NAME, src.HTTP_METHOD, src.API_PATTERN, src.SORT_ORDER, src.USE_YN, src.created_by, src.updated_by);
MERGE INTO ADM_BUTTON tgt
USING (SELECT 'BAT_EVIDENCE_DOWNLOAD' AS BUTTON_ID, 'BATCH_AUDIT' AS MENU_ID, 'EVIDENCE_DOWNLOAD' AS ACTION_CODE, 'BAT Evidence 다운로드' AS BUTTON_NAME, 'GET' AS HTTP_METHOD, '/adm/api/downloads/**' AS API_PATTERN, 140 AS SORT_ORDER, 'Y' AS USE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.BUTTON_ID=src.BUTTON_ID)
WHEN MATCHED THEN UPDATE SET tgt.MENU_ID=src.MENU_ID, tgt.ACTION_CODE=src.ACTION_CODE, tgt.BUTTON_NAME=src.BUTTON_NAME, tgt.HTTP_METHOD=src.HTTP_METHOD, tgt.API_PATTERN=src.API_PATTERN, tgt.SORT_ORDER=src.SORT_ORDER, tgt.USE_YN='Y', tgt.updated_by='SYSTEM', tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (BUTTON_ID, MENU_ID, ACTION_CODE, BUTTON_NAME, HTTP_METHOD, API_PATTERN, SORT_ORDER, USE_YN, created_by, updated_by) VALUES (src.BUTTON_ID, src.MENU_ID, src.ACTION_CODE, src.BUTTON_NAME, src.HTTP_METHOD, src.API_PATTERN, src.SORT_ORDER, src.USE_YN, src.created_by, src.updated_by);
MERGE INTO ADM_BUTTON tgt
USING (SELECT 'BAT_RETENTION_VIEW' AS BUTTON_ID, 'BATCH_RUNTIME' AS MENU_ID, 'RETENTION_VIEW' AS ACTION_CODE, 'Retention 조회' AS BUTTON_NAME, 'GET' AS HTTP_METHOD, '/adm/api/batch-runtime/retention/**' AS API_PATTERN, 150 AS SORT_ORDER, 'Y' AS USE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.BUTTON_ID=src.BUTTON_ID)
WHEN MATCHED THEN UPDATE SET tgt.MENU_ID=src.MENU_ID, tgt.ACTION_CODE=src.ACTION_CODE, tgt.BUTTON_NAME=src.BUTTON_NAME, tgt.HTTP_METHOD=src.HTTP_METHOD, tgt.API_PATTERN=src.API_PATTERN, tgt.SORT_ORDER=src.SORT_ORDER, tgt.USE_YN='Y', tgt.updated_by='SYSTEM', tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (BUTTON_ID, MENU_ID, ACTION_CODE, BUTTON_NAME, HTTP_METHOD, API_PATTERN, SORT_ORDER, USE_YN, created_by, updated_by) VALUES (src.BUTTON_ID, src.MENU_ID, src.ACTION_CODE, src.BUTTON_NAME, src.HTTP_METHOD, src.API_PATTERN, src.SORT_ORDER, src.USE_YN, src.created_by, src.updated_by);
MERGE INTO ADM_BUTTON tgt
USING (SELECT 'BAT_RETENTION_PREVIEW' AS BUTTON_ID, 'BATCH_RUNTIME' AS MENU_ID, 'RETENTION_PREVIEW' AS ACTION_CODE, 'Retention Preview' AS BUTTON_NAME, 'POST' AS HTTP_METHOD, '/adm/api/batch-runtime/retention/preview' AS API_PATTERN, 160 AS SORT_ORDER, 'Y' AS USE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.BUTTON_ID=src.BUTTON_ID)
WHEN MATCHED THEN UPDATE SET tgt.MENU_ID=src.MENU_ID, tgt.ACTION_CODE=src.ACTION_CODE, tgt.BUTTON_NAME=src.BUTTON_NAME, tgt.HTTP_METHOD=src.HTTP_METHOD, tgt.API_PATTERN=src.API_PATTERN, tgt.SORT_ORDER=src.SORT_ORDER, tgt.USE_YN='Y', tgt.updated_by='SYSTEM', tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (BUTTON_ID, MENU_ID, ACTION_CODE, BUTTON_NAME, HTTP_METHOD, API_PATTERN, SORT_ORDER, USE_YN, created_by, updated_by) VALUES (src.BUTTON_ID, src.MENU_ID, src.ACTION_CODE, src.BUTTON_NAME, src.HTTP_METHOD, src.API_PATTERN, src.SORT_ORDER, src.USE_YN, src.created_by, src.updated_by);
MERGE INTO ADM_BUTTON tgt
USING (SELECT 'BAT_RETENTION_POLICY_REQUEST' AS BUTTON_ID, 'BATCH_RUNTIME' AS MENU_ID, 'RETENTION_POLICY_REQUEST' AS ACTION_CODE, 'Retention 정책 변경 승인요청' AS BUTTON_NAME, 'POST' AS HTTP_METHOD, '/adm/api/batch-runtime/retention/policies' AS API_PATTERN, 170 AS SORT_ORDER, 'Y' AS USE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.BUTTON_ID=src.BUTTON_ID)
WHEN MATCHED THEN UPDATE SET tgt.MENU_ID=src.MENU_ID, tgt.ACTION_CODE=src.ACTION_CODE, tgt.BUTTON_NAME=src.BUTTON_NAME, tgt.HTTP_METHOD=src.HTTP_METHOD, tgt.API_PATTERN=src.API_PATTERN, tgt.SORT_ORDER=src.SORT_ORDER, tgt.USE_YN='Y', tgt.updated_by='SYSTEM', tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (BUTTON_ID, MENU_ID, ACTION_CODE, BUTTON_NAME, HTTP_METHOD, API_PATTERN, SORT_ORDER, USE_YN, created_by, updated_by) VALUES (src.BUTTON_ID, src.MENU_ID, src.ACTION_CODE, src.BUTTON_NAME, src.HTTP_METHOD, src.API_PATTERN, src.SORT_ORDER, src.USE_YN, src.created_by, src.updated_by);
MERGE INTO ADM_BUTTON tgt
USING (SELECT 'BAT_RETENTION_RUN_REQUEST' AS BUTTON_ID, 'BATCH_RUNTIME' AS MENU_ID, 'RETENTION_RUN_REQUEST' AS ACTION_CODE, 'Retention 수동 실행 승인요청' AS BUTTON_NAME, 'POST' AS HTTP_METHOD, '/adm/api/batch-runtime/retention/policies/*/run' AS API_PATTERN, 180 AS SORT_ORDER, 'Y' AS USE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.BUTTON_ID=src.BUTTON_ID)
WHEN MATCHED THEN UPDATE SET tgt.MENU_ID=src.MENU_ID, tgt.ACTION_CODE=src.ACTION_CODE, tgt.BUTTON_NAME=src.BUTTON_NAME, tgt.HTTP_METHOD=src.HTTP_METHOD, tgt.API_PATTERN=src.API_PATTERN, tgt.SORT_ORDER=src.SORT_ORDER, tgt.USE_YN='Y', tgt.updated_by='SYSTEM', tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (BUTTON_ID, MENU_ID, ACTION_CODE, BUTTON_NAME, HTTP_METHOD, API_PATTERN, SORT_ORDER, USE_YN, created_by, updated_by) VALUES (src.BUTTON_ID, src.MENU_ID, src.ACTION_CODE, src.BUTTON_NAME, src.HTTP_METHOD, src.API_PATTERN, src.SORT_ORDER, src.USE_YN, src.created_by, src.updated_by);
MERGE INTO ADM_BUTTON tgt
USING (SELECT 'BAT_RETENTION_RUN_PAUSE' AS BUTTON_ID, 'BATCH_RUNTIME' AS MENU_ID, 'RETENTION_RUN_PAUSE' AS ACTION_CODE, 'Retention Run 안전 일시정지' AS BUTTON_NAME, 'POST' AS HTTP_METHOD, '/adm/api/batch-runtime/retention/runs/*/pause' AS API_PATTERN, 190 AS SORT_ORDER, 'Y' AS USE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.BUTTON_ID=src.BUTTON_ID)
WHEN MATCHED THEN UPDATE SET tgt.MENU_ID=src.MENU_ID, tgt.ACTION_CODE=src.ACTION_CODE, tgt.BUTTON_NAME=src.BUTTON_NAME, tgt.HTTP_METHOD=src.HTTP_METHOD, tgt.API_PATTERN=src.API_PATTERN, tgt.SORT_ORDER=src.SORT_ORDER, tgt.USE_YN='Y', tgt.updated_by='SYSTEM', tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (BUTTON_ID, MENU_ID, ACTION_CODE, BUTTON_NAME, HTTP_METHOD, API_PATTERN, SORT_ORDER, USE_YN, created_by, updated_by) VALUES (src.BUTTON_ID, src.MENU_ID, src.ACTION_CODE, src.BUTTON_NAME, src.HTTP_METHOD, src.API_PATTERN, src.SORT_ORDER, src.USE_YN, src.created_by, src.updated_by);
MERGE INTO ADM_BUTTON tgt
USING (SELECT 'BAT_RETENTION_RUN_RESUME' AS BUTTON_ID, 'BATCH_RUNTIME' AS MENU_ID, 'RETENTION_RUN_RESUME' AS ACTION_CODE, 'Retention Run 재개 승인요청' AS BUTTON_NAME, 'POST' AS HTTP_METHOD, '/adm/api/batch-runtime/retention/runs/*/resume' AS API_PATTERN, 200 AS SORT_ORDER, 'Y' AS USE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.BUTTON_ID=src.BUTTON_ID)
WHEN MATCHED THEN UPDATE SET tgt.MENU_ID=src.MENU_ID, tgt.ACTION_CODE=src.ACTION_CODE, tgt.BUTTON_NAME=src.BUTTON_NAME, tgt.HTTP_METHOD=src.HTTP_METHOD, tgt.API_PATTERN=src.API_PATTERN, tgt.SORT_ORDER=src.SORT_ORDER, tgt.USE_YN='Y', tgt.updated_by='SYSTEM', tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (BUTTON_ID, MENU_ID, ACTION_CODE, BUTTON_NAME, HTTP_METHOD, API_PATTERN, SORT_ORDER, USE_YN, created_by, updated_by) VALUES (src.BUTTON_ID, src.MENU_ID, src.ACTION_CODE, src.BUTTON_NAME, src.HTTP_METHOD, src.API_PATTERN, src.SORT_ORDER, src.USE_YN, src.created_by, src.updated_by);
MERGE INTO ADM_BUTTON tgt
USING (SELECT 'BAT_RETENTION_POLICY_PAUSE' AS BUTTON_ID, 'BATCH_RUNTIME' AS MENU_ID, 'RETENTION_POLICY_PAUSE' AS ACTION_CODE, 'Retention 정책 안전 일시정지' AS BUTTON_NAME, 'POST' AS HTTP_METHOD, '/adm/api/batch-runtime/retention/policies/*/pause' AS API_PATTERN, 210 AS SORT_ORDER, 'Y' AS USE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.BUTTON_ID=src.BUTTON_ID)
WHEN MATCHED THEN UPDATE SET tgt.MENU_ID=src.MENU_ID, tgt.ACTION_CODE=src.ACTION_CODE, tgt.BUTTON_NAME=src.BUTTON_NAME, tgt.HTTP_METHOD=src.HTTP_METHOD, tgt.API_PATTERN=src.API_PATTERN, tgt.SORT_ORDER=src.SORT_ORDER, tgt.USE_YN='Y', tgt.updated_by='SYSTEM', tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (BUTTON_ID, MENU_ID, ACTION_CODE, BUTTON_NAME, HTTP_METHOD, API_PATTERN, SORT_ORDER, USE_YN, created_by, updated_by) VALUES (src.BUTTON_ID, src.MENU_ID, src.ACTION_CODE, src.BUTTON_NAME, src.HTTP_METHOD, src.API_PATTERN, src.SORT_ORDER, src.USE_YN, src.created_by, src.updated_by);
MERGE INTO ADM_BUTTON tgt
USING (SELECT 'BAT_RETENTION_POLICY_RESUME' AS BUTTON_ID, 'BATCH_RUNTIME' AS MENU_ID, 'RETENTION_POLICY_RESUME' AS ACTION_CODE, 'Retention 정책 재개 승인요청' AS BUTTON_NAME, 'POST' AS HTTP_METHOD, '/adm/api/batch-runtime/retention/policies/*/resume' AS API_PATTERN, 220 AS SORT_ORDER, 'Y' AS USE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.BUTTON_ID=src.BUTTON_ID)
WHEN MATCHED THEN UPDATE SET tgt.MENU_ID=src.MENU_ID, tgt.ACTION_CODE=src.ACTION_CODE, tgt.BUTTON_NAME=src.BUTTON_NAME, tgt.HTTP_METHOD=src.HTTP_METHOD, tgt.API_PATTERN=src.API_PATTERN, tgt.SORT_ORDER=src.SORT_ORDER, tgt.USE_YN='Y', tgt.updated_by='SYSTEM', tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (BUTTON_ID, MENU_ID, ACTION_CODE, BUTTON_NAME, HTTP_METHOD, API_PATTERN, SORT_ORDER, USE_YN, created_by, updated_by) VALUES (src.BUTTON_ID, src.MENU_ID, src.ACTION_CODE, src.BUTTON_NAME, src.HTTP_METHOD, src.API_PATTERN, src.SORT_ORDER, src.USE_YN, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_MENU tgt
USING (SELECT r.ROLE_ID AS ROLE_ID, m.MENU_ID AS MENU_ID, 'Y' AS READ_YN, CASE WHEN r.ROLE_ID IN ('ADM_ADMIN','ADM_DEV_OPERATOR','ADM_OPERATOR') THEN 'Y' ELSE 'N' END AS WRITE_YN, 'N' AS DELETE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM ADM_ROLE r JOIN ADM_MENU m ON m.PARENT_MENU_ID='BATCH'
WHERE r.ROLE_ID IN ('ADM_ADMIN','ADM_DEV_OPERATOR','ADM_OPERATOR','ADM_BIZ_OPERATOR','ADM_VIEWER')) src
ON (tgt.ROLE_ID=src.ROLE_ID AND tgt.MENU_ID=src.MENU_ID)
WHEN MATCHED THEN UPDATE SET tgt.READ_YN=src.READ_YN, tgt.WRITE_YN=src.WRITE_YN, tgt.DELETE_YN=src.DELETE_YN, tgt.updated_by='SYSTEM', tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (ROLE_ID, MENU_ID, READ_YN, WRITE_YN, DELETE_YN, created_by, updated_by) VALUES (src.ROLE_ID, src.MENU_ID, src.READ_YN, src.WRITE_YN, src.DELETE_YN, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_BUTTON tgt
USING (SELECT r.ROLE_ID AS ROLE_ID, b.BUTTON_ID AS BUTTON_ID, CASE
         WHEN r.ROLE_ID='ADM_ADMIN' THEN 'Y'
         WHEN r.ROLE_ID IN ('ADM_DEV_OPERATOR','ADM_OPERATOR') AND b.BUTTON_ID NOT IN ('BAT_DEPLOY_APPROVE','BAT_DEPLOY_EXECUTE','BAT_ROLLBACK_EXECUTE') THEN 'Y'
         WHEN r.ROLE_ID='ADM_BIZ_OPERATOR' AND b.BUTTON_ID IN ('BAT_RUNTIME_VIEW','BAT_JOB_OPERATE','BAT_WORKER_OPERATE','BAT_CENTER_CUT_OPERATE','BAT_SECURITY_AUDIT','BAT_EVIDENCE_DOWNLOAD') THEN 'Y'
         WHEN r.ROLE_ID='ADM_VIEWER' AND b.BUTTON_ID IN ('BAT_RUNTIME_VIEW','BAT_SECURITY_AUDIT') THEN 'Y'
         ELSE 'N' END AS ALLOW_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM ADM_ROLE r JOIN ADM_BUTTON b ON b.BUTTON_ID LIKE 'BAT_%'
WHERE r.ROLE_ID IN ('ADM_ADMIN','ADM_DEV_OPERATOR','ADM_OPERATOR','ADM_BIZ_OPERATOR','ADM_VIEWER')) src
ON (tgt.ROLE_ID=src.ROLE_ID AND tgt.BUTTON_ID=src.BUTTON_ID)
WHEN MATCHED THEN UPDATE SET tgt.ALLOW_YN=src.ALLOW_YN, tgt.updated_by='SYSTEM', tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (ROLE_ID, BUTTON_ID, ALLOW_YN, created_by, updated_by) VALUES (src.ROLE_ID, src.BUTTON_ID, src.ALLOW_YN, src.created_by, src.updated_by);
MERGE INTO ADM_API_PERMISSION tgt
USING (SELECT CONCAT('API_', BUTTON_ID) AS API_PERMISSION_ID, MENU_ID AS API_GROUP_CODE, COALESCE(HTTP_METHOD, 'ANY') AS HTTP_METHOD, API_PATTERN AS API_PATH, BUTTON_NAME AS API_NAME, ACTION_CODE AS PERMISSION_CODE, MENU_ID AS MENU_ID, BUTTON_ID AS BUTTON_ID, 'Y' AS USE_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM (
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
  )) src
ON (tgt.API_PERMISSION_ID=src.API_PERMISSION_ID)
WHEN MATCHED THEN UPDATE SET tgt.API_GROUP_CODE=src.API_GROUP_CODE, tgt.HTTP_METHOD=src.HTTP_METHOD, tgt.API_PATH=src.API_PATH, tgt.API_NAME=src.API_NAME, tgt.PERMISSION_CODE=src.PERMISSION_CODE, tgt.MENU_ID=src.MENU_ID, tgt.BUTTON_ID=src.BUTTON_ID, tgt.USE_YN='Y', tgt.updated_by='SYSTEM', tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (API_PERMISSION_ID, API_GROUP_CODE, HTTP_METHOD, API_PATH, API_NAME, PERMISSION_CODE, MENU_ID, BUTTON_ID, USE_YN, created_by, updated_by) VALUES (src.API_PERMISSION_ID, src.API_GROUP_CODE, src.HTTP_METHOD, src.API_PATH, src.API_NAME, src.PERMISSION_CODE, src.MENU_ID, src.BUTTON_ID, src.USE_YN, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_API_PERMISSION tgt
USING (SELECT rb.ROLE_ID AS ROLE_ID, ap.API_PERMISSION_ID AS API_PERMISSION_ID, CASE WHEN MAX(rb.ALLOW_YN) = 'Y' THEN 'Y' ELSE 'N' END AS ALLOW_YN, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM ADM_ROLE_BUTTON rb
JOIN ADM_BUTTON b ON b.BUTTON_ID = rb.BUTTON_ID
JOIN ADM_API_PERMISSION ap
  ON ap.HTTP_METHOD = COALESCE(b.HTTP_METHOD, 'ANY')
 AND ap.API_PATH = b.API_PATTERN
WHERE rb.BUTTON_ID LIKE 'BAT_%'
  AND b.API_PATTERN IS NOT NULL
GROUP BY rb.ROLE_ID, ap.API_PERMISSION_ID) src
ON (tgt.ROLE_ID=src.ROLE_ID AND tgt.API_PERMISSION_ID=src.API_PERMISSION_ID)
WHEN MATCHED THEN UPDATE SET tgt.ALLOW_YN=src.ALLOW_YN, tgt.updated_by='SYSTEM', tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (ROLE_ID, API_PERMISSION_ID, ALLOW_YN, created_by, updated_by) VALUES (src.ROLE_ID, src.API_PERMISSION_ID, src.ALLOW_YN, src.created_by, src.updated_by);
-- ===== END 60_adm_seed_data.sql =====

-- ===== BEGIN 61_adm_gateway_seed.sql =====
-- AUTO-GENERATED from cpf-tools/db/canonical/seed-model.json
-- vendor=oracle; source=61_adm_gateway_seed.sql
-- DERIVED compatibility input; canonical authority is cpf-tools/db/canonical/**.
-- DO NOT EDIT generated seed directly.

-- CPF_LOGICAL_DATABASE=cpfDB
MERGE INTO ADM_BUTTON tgt
USING (SELECT 'GATEWAY_READ' AS button_id, 'GATEWAY_DASHBOARD' AS menu_id, 'READ' AS action_code, 'Gateway 운영 조회' AS button_name, 'GET' AS http_method, '/adm/api/gateway-registry/**' AS api_pattern, 10 AS sort_order, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.button_id=src.button_id)
WHEN MATCHED THEN UPDATE SET tgt.menu_id=src.menu_id, tgt.action_code=src.action_code, tgt.button_name=src.button_name, tgt.http_method=src.http_method, tgt.api_pattern=src.api_pattern, tgt.sort_order=src.sort_order, tgt.use_yn=src.use_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (button_id, menu_id, action_code, button_name, http_method, api_pattern, sort_order, use_yn, created_by, updated_by) VALUES (src.button_id, src.menu_id, src.action_code, src.button_name, src.http_method, src.api_pattern, src.sort_order, src.use_yn, src.created_by, src.updated_by);
MERGE INTO ADM_BUTTON tgt
USING (SELECT 'GATEWAY_GROUP_WRITE' AS button_id, 'GATEWAY_GROUPS' AS menu_id, 'WRITE' AS action_code, 'Server Group 저장' AS button_name, 'POST' AS http_method, '/adm/api/gateway-registry/server-groups' AS api_pattern, 20 AS sort_order, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.button_id=src.button_id)
WHEN MATCHED THEN UPDATE SET tgt.menu_id=src.menu_id, tgt.action_code=src.action_code, tgt.button_name=src.button_name, tgt.http_method=src.http_method, tgt.api_pattern=src.api_pattern, tgt.sort_order=src.sort_order, tgt.use_yn=src.use_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (button_id, menu_id, action_code, button_name, http_method, api_pattern, sort_order, use_yn, created_by, updated_by) VALUES (src.button_id, src.menu_id, src.action_code, src.button_name, src.http_method, src.api_pattern, src.sort_order, src.use_yn, src.created_by, src.updated_by);
MERGE INTO ADM_BUTTON tgt
USING (SELECT 'GATEWAY_GROUP_DELETE' AS button_id, 'GATEWAY_GROUPS' AS menu_id, 'DELETE' AS action_code, 'Server Group 폐기' AS button_name, 'DELETE' AS http_method, '/adm/api/gateway-registry/server-groups/*' AS api_pattern, 30 AS sort_order, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.button_id=src.button_id)
WHEN MATCHED THEN UPDATE SET tgt.menu_id=src.menu_id, tgt.action_code=src.action_code, tgt.button_name=src.button_name, tgt.http_method=src.http_method, tgt.api_pattern=src.api_pattern, tgt.sort_order=src.sort_order, tgt.use_yn=src.use_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (button_id, menu_id, action_code, button_name, http_method, api_pattern, sort_order, use_yn, created_by, updated_by) VALUES (src.button_id, src.menu_id, src.action_code, src.button_name, src.http_method, src.api_pattern, src.sort_order, src.use_yn, src.created_by, src.updated_by);
MERGE INTO ADM_BUTTON tgt
USING (SELECT 'GATEWAY_ROUTE_WRITE' AS button_id, 'GATEWAY_ROUTES' AS menu_id, 'WRITE' AS action_code, 'Gateway Binding 저장' AS button_name, 'POST' AS http_method, '/adm/api/gateway-registry/bindings' AS api_pattern, 40 AS sort_order, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.button_id=src.button_id)
WHEN MATCHED THEN UPDATE SET tgt.menu_id=src.menu_id, tgt.action_code=src.action_code, tgt.button_name=src.button_name, tgt.http_method=src.http_method, tgt.api_pattern=src.api_pattern, tgt.sort_order=src.sort_order, tgt.use_yn=src.use_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (button_id, menu_id, action_code, button_name, http_method, api_pattern, sort_order, use_yn, created_by, updated_by) VALUES (src.button_id, src.menu_id, src.action_code, src.button_name, src.http_method, src.api_pattern, src.sort_order, src.use_yn, src.created_by, src.updated_by);
MERGE INTO ADM_BUTTON tgt
USING (SELECT 'GATEWAY_ROUTE_STATE' AS button_id, 'GATEWAY_ROUTES' AS menu_id, 'CONTROL' AS action_code, 'Gateway Binding 상태 변경' AS button_name, 'POST' AS http_method, '/adm/api/gateway-registry/bindings/*/state' AS api_pattern, 50 AS sort_order, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.button_id=src.button_id)
WHEN MATCHED THEN UPDATE SET tgt.menu_id=src.menu_id, tgt.action_code=src.action_code, tgt.button_name=src.button_name, tgt.http_method=src.http_method, tgt.api_pattern=src.api_pattern, tgt.sort_order=src.sort_order, tgt.use_yn=src.use_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (button_id, menu_id, action_code, button_name, http_method, api_pattern, sort_order, use_yn, created_by, updated_by) VALUES (src.button_id, src.menu_id, src.action_code, src.button_name, src.http_method, src.api_pattern, src.sort_order, src.use_yn, src.created_by, src.updated_by);
MERGE INTO ADM_BUTTON tgt
USING (SELECT 'GATEWAY_ROUTE_DELETE' AS button_id, 'GATEWAY_ROUTES' AS menu_id, 'DELETE' AS action_code, 'Gateway Binding 폐기' AS button_name, 'DELETE' AS http_method, '/adm/api/gateway-registry/bindings/*' AS api_pattern, 60 AS sort_order, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.button_id=src.button_id)
WHEN MATCHED THEN UPDATE SET tgt.menu_id=src.menu_id, tgt.action_code=src.action_code, tgt.button_name=src.button_name, tgt.http_method=src.http_method, tgt.api_pattern=src.api_pattern, tgt.sort_order=src.sort_order, tgt.use_yn=src.use_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (button_id, menu_id, action_code, button_name, http_method, api_pattern, sort_order, use_yn, created_by, updated_by) VALUES (src.button_id, src.menu_id, src.action_code, src.button_name, src.http_method, src.api_pattern, src.sort_order, src.use_yn, src.created_by, src.updated_by);
MERGE INTO ADM_BUTTON tgt
USING (SELECT 'GATEWAY_CONNECTION_TEST' AS button_id, 'GATEWAY_HEALTH' AS menu_id, 'TEST' AS action_code, 'Gateway 연결시험 요청' AS button_name, 'POST' AS http_method, '/adm/api/gateway-registry/bindings/*/connection-tests' AS api_pattern, 70 AS sort_order, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.button_id=src.button_id)
WHEN MATCHED THEN UPDATE SET tgt.menu_id=src.menu_id, tgt.action_code=src.action_code, tgt.button_name=src.button_name, tgt.http_method=src.http_method, tgt.api_pattern=src.api_pattern, tgt.sort_order=src.sort_order, tgt.use_yn=src.use_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (button_id, menu_id, action_code, button_name, http_method, api_pattern, sort_order, use_yn, created_by, updated_by) VALUES (src.button_id, src.menu_id, src.action_code, src.button_name, src.http_method, src.api_pattern, src.sort_order, src.use_yn, src.created_by, src.updated_by);
MERGE INTO ADM_BUTTON tgt
USING (SELECT 'GATEWAY_TEST_CONTROL' AS button_id, 'GATEWAY_HEALTH' AS menu_id, 'CONTROL' AS action_code, 'Gateway 연결시험 취소·재검증' AS button_name, 'POST' AS http_method, '/adm/api/gateway-registry/connection-test-operations/*/**' AS api_pattern, 80 AS sort_order, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.button_id=src.button_id)
WHEN MATCHED THEN UPDATE SET tgt.menu_id=src.menu_id, tgt.action_code=src.action_code, tgt.button_name=src.button_name, tgt.http_method=src.http_method, tgt.api_pattern=src.api_pattern, tgt.sort_order=src.sort_order, tgt.use_yn=src.use_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (button_id, menu_id, action_code, button_name, http_method, api_pattern, sort_order, use_yn, created_by, updated_by) VALUES (src.button_id, src.menu_id, src.action_code, src.button_name, src.http_method, src.api_pattern, src.sort_order, src.use_yn, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_MENU tgt
USING (SELECT 'ADM_ADMIN' AS role_id, 'GATEWAY_DASHBOARD' AS menu_id, 'Y' AS read_yn, 'Y' AS write_yn, 'Y' AS delete_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_id=src.role_id AND tgt.menu_id=src.menu_id)
WHEN MATCHED THEN UPDATE SET tgt.read_yn=src.read_yn, tgt.write_yn=src.write_yn, tgt.delete_yn=src.delete_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_id, menu_id, read_yn, write_yn, delete_yn, created_by, updated_by) VALUES (src.role_id, src.menu_id, src.read_yn, src.write_yn, src.delete_yn, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_MENU tgt
USING (SELECT 'ADM_ADMIN' AS role_id, 'GATEWAY_SERVERS' AS menu_id, 'Y' AS read_yn, 'Y' AS write_yn, 'Y' AS delete_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_id=src.role_id AND tgt.menu_id=src.menu_id)
WHEN MATCHED THEN UPDATE SET tgt.read_yn=src.read_yn, tgt.write_yn=src.write_yn, tgt.delete_yn=src.delete_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_id, menu_id, read_yn, write_yn, delete_yn, created_by, updated_by) VALUES (src.role_id, src.menu_id, src.read_yn, src.write_yn, src.delete_yn, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_MENU tgt
USING (SELECT 'ADM_ADMIN' AS role_id, 'GATEWAY_GROUPS' AS menu_id, 'Y' AS read_yn, 'Y' AS write_yn, 'Y' AS delete_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_id=src.role_id AND tgt.menu_id=src.menu_id)
WHEN MATCHED THEN UPDATE SET tgt.read_yn=src.read_yn, tgt.write_yn=src.write_yn, tgt.delete_yn=src.delete_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_id, menu_id, read_yn, write_yn, delete_yn, created_by, updated_by) VALUES (src.role_id, src.menu_id, src.read_yn, src.write_yn, src.delete_yn, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_MENU tgt
USING (SELECT 'ADM_ADMIN' AS role_id, 'GATEWAY_ROUTES' AS menu_id, 'Y' AS read_yn, 'Y' AS write_yn, 'Y' AS delete_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_id=src.role_id AND tgt.menu_id=src.menu_id)
WHEN MATCHED THEN UPDATE SET tgt.read_yn=src.read_yn, tgt.write_yn=src.write_yn, tgt.delete_yn=src.delete_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_id, menu_id, read_yn, write_yn, delete_yn, created_by, updated_by) VALUES (src.role_id, src.menu_id, src.read_yn, src.write_yn, src.delete_yn, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_MENU tgt
USING (SELECT 'ADM_ADMIN' AS role_id, 'GATEWAY_SECURITY' AS menu_id, 'Y' AS read_yn, 'Y' AS write_yn, 'Y' AS delete_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_id=src.role_id AND tgt.menu_id=src.menu_id)
WHEN MATCHED THEN UPDATE SET tgt.read_yn=src.read_yn, tgt.write_yn=src.write_yn, tgt.delete_yn=src.delete_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_id, menu_id, read_yn, write_yn, delete_yn, created_by, updated_by) VALUES (src.role_id, src.menu_id, src.read_yn, src.write_yn, src.delete_yn, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_MENU tgt
USING (SELECT 'ADM_ADMIN' AS role_id, 'GATEWAY_HEALTH' AS menu_id, 'Y' AS read_yn, 'Y' AS write_yn, 'Y' AS delete_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_id=src.role_id AND tgt.menu_id=src.menu_id)
WHEN MATCHED THEN UPDATE SET tgt.read_yn=src.read_yn, tgt.write_yn=src.write_yn, tgt.delete_yn=src.delete_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_id, menu_id, read_yn, write_yn, delete_yn, created_by, updated_by) VALUES (src.role_id, src.menu_id, src.read_yn, src.write_yn, src.delete_yn, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_MENU tgt
USING (SELECT 'ADM_ADMIN' AS role_id, 'GATEWAY_TRANSACTIONS' AS menu_id, 'Y' AS read_yn, 'Y' AS write_yn, 'Y' AS delete_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_id=src.role_id AND tgt.menu_id=src.menu_id)
WHEN MATCHED THEN UPDATE SET tgt.read_yn=src.read_yn, tgt.write_yn=src.write_yn, tgt.delete_yn=src.delete_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_id, menu_id, read_yn, write_yn, delete_yn, created_by, updated_by) VALUES (src.role_id, src.menu_id, src.read_yn, src.write_yn, src.delete_yn, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_MENU tgt
USING (SELECT 'ADM_ADMIN' AS role_id, 'GATEWAY_LOG_POLICY' AS menu_id, 'Y' AS read_yn, 'Y' AS write_yn, 'Y' AS delete_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_id=src.role_id AND tgt.menu_id=src.menu_id)
WHEN MATCHED THEN UPDATE SET tgt.read_yn=src.read_yn, tgt.write_yn=src.write_yn, tgt.delete_yn=src.delete_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_id, menu_id, read_yn, write_yn, delete_yn, created_by, updated_by) VALUES (src.role_id, src.menu_id, src.read_yn, src.write_yn, src.delete_yn, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_MENU tgt
USING (SELECT 'ADM_ADMIN' AS role_id, 'GATEWAY_APPLY_STATUS' AS menu_id, 'Y' AS read_yn, 'Y' AS write_yn, 'Y' AS delete_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_id=src.role_id AND tgt.menu_id=src.menu_id)
WHEN MATCHED THEN UPDATE SET tgt.read_yn=src.read_yn, tgt.write_yn=src.write_yn, tgt.delete_yn=src.delete_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_id, menu_id, read_yn, write_yn, delete_yn, created_by, updated_by) VALUES (src.role_id, src.menu_id, src.read_yn, src.write_yn, src.delete_yn, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_MENU tgt
USING (SELECT 'ADM_DEV_OPERATOR' AS role_id, 'GATEWAY_DASHBOARD' AS menu_id, 'Y' AS read_yn, 'Y' AS write_yn, 'N' AS delete_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_id=src.role_id AND tgt.menu_id=src.menu_id)
WHEN MATCHED THEN UPDATE SET tgt.read_yn=src.read_yn, tgt.write_yn=src.write_yn, tgt.delete_yn=src.delete_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_id, menu_id, read_yn, write_yn, delete_yn, created_by, updated_by) VALUES (src.role_id, src.menu_id, src.read_yn, src.write_yn, src.delete_yn, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_MENU tgt
USING (SELECT 'ADM_DEV_OPERATOR' AS role_id, 'GATEWAY_SERVERS' AS menu_id, 'Y' AS read_yn, 'Y' AS write_yn, 'N' AS delete_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_id=src.role_id AND tgt.menu_id=src.menu_id)
WHEN MATCHED THEN UPDATE SET tgt.read_yn=src.read_yn, tgt.write_yn=src.write_yn, tgt.delete_yn=src.delete_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_id, menu_id, read_yn, write_yn, delete_yn, created_by, updated_by) VALUES (src.role_id, src.menu_id, src.read_yn, src.write_yn, src.delete_yn, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_MENU tgt
USING (SELECT 'ADM_DEV_OPERATOR' AS role_id, 'GATEWAY_GROUPS' AS menu_id, 'Y' AS read_yn, 'Y' AS write_yn, 'N' AS delete_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_id=src.role_id AND tgt.menu_id=src.menu_id)
WHEN MATCHED THEN UPDATE SET tgt.read_yn=src.read_yn, tgt.write_yn=src.write_yn, tgt.delete_yn=src.delete_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_id, menu_id, read_yn, write_yn, delete_yn, created_by, updated_by) VALUES (src.role_id, src.menu_id, src.read_yn, src.write_yn, src.delete_yn, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_MENU tgt
USING (SELECT 'ADM_DEV_OPERATOR' AS role_id, 'GATEWAY_ROUTES' AS menu_id, 'Y' AS read_yn, 'Y' AS write_yn, 'N' AS delete_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_id=src.role_id AND tgt.menu_id=src.menu_id)
WHEN MATCHED THEN UPDATE SET tgt.read_yn=src.read_yn, tgt.write_yn=src.write_yn, tgt.delete_yn=src.delete_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_id, menu_id, read_yn, write_yn, delete_yn, created_by, updated_by) VALUES (src.role_id, src.menu_id, src.read_yn, src.write_yn, src.delete_yn, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_MENU tgt
USING (SELECT 'ADM_DEV_OPERATOR' AS role_id, 'GATEWAY_SECURITY' AS menu_id, 'Y' AS read_yn, 'Y' AS write_yn, 'N' AS delete_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_id=src.role_id AND tgt.menu_id=src.menu_id)
WHEN MATCHED THEN UPDATE SET tgt.read_yn=src.read_yn, tgt.write_yn=src.write_yn, tgt.delete_yn=src.delete_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_id, menu_id, read_yn, write_yn, delete_yn, created_by, updated_by) VALUES (src.role_id, src.menu_id, src.read_yn, src.write_yn, src.delete_yn, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_MENU tgt
USING (SELECT 'ADM_DEV_OPERATOR' AS role_id, 'GATEWAY_HEALTH' AS menu_id, 'Y' AS read_yn, 'Y' AS write_yn, 'N' AS delete_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_id=src.role_id AND tgt.menu_id=src.menu_id)
WHEN MATCHED THEN UPDATE SET tgt.read_yn=src.read_yn, tgt.write_yn=src.write_yn, tgt.delete_yn=src.delete_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_id, menu_id, read_yn, write_yn, delete_yn, created_by, updated_by) VALUES (src.role_id, src.menu_id, src.read_yn, src.write_yn, src.delete_yn, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_MENU tgt
USING (SELECT 'ADM_DEV_OPERATOR' AS role_id, 'GATEWAY_TRANSACTIONS' AS menu_id, 'Y' AS read_yn, 'Y' AS write_yn, 'N' AS delete_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_id=src.role_id AND tgt.menu_id=src.menu_id)
WHEN MATCHED THEN UPDATE SET tgt.read_yn=src.read_yn, tgt.write_yn=src.write_yn, tgt.delete_yn=src.delete_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_id, menu_id, read_yn, write_yn, delete_yn, created_by, updated_by) VALUES (src.role_id, src.menu_id, src.read_yn, src.write_yn, src.delete_yn, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_MENU tgt
USING (SELECT 'ADM_DEV_OPERATOR' AS role_id, 'GATEWAY_LOG_POLICY' AS menu_id, 'Y' AS read_yn, 'Y' AS write_yn, 'N' AS delete_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_id=src.role_id AND tgt.menu_id=src.menu_id)
WHEN MATCHED THEN UPDATE SET tgt.read_yn=src.read_yn, tgt.write_yn=src.write_yn, tgt.delete_yn=src.delete_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_id, menu_id, read_yn, write_yn, delete_yn, created_by, updated_by) VALUES (src.role_id, src.menu_id, src.read_yn, src.write_yn, src.delete_yn, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_MENU tgt
USING (SELECT 'ADM_DEV_OPERATOR' AS role_id, 'GATEWAY_APPLY_STATUS' AS menu_id, 'Y' AS read_yn, 'Y' AS write_yn, 'N' AS delete_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_id=src.role_id AND tgt.menu_id=src.menu_id)
WHEN MATCHED THEN UPDATE SET tgt.read_yn=src.read_yn, tgt.write_yn=src.write_yn, tgt.delete_yn=src.delete_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_id, menu_id, read_yn, write_yn, delete_yn, created_by, updated_by) VALUES (src.role_id, src.menu_id, src.read_yn, src.write_yn, src.delete_yn, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_MENU tgt
USING (SELECT 'ADM_OPERATOR' AS role_id, 'GATEWAY_DASHBOARD' AS menu_id, 'Y' AS read_yn, 'N' AS write_yn, 'N' AS delete_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_id=src.role_id AND tgt.menu_id=src.menu_id)
WHEN MATCHED THEN UPDATE SET tgt.read_yn=src.read_yn, tgt.write_yn=src.write_yn, tgt.delete_yn=src.delete_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_id, menu_id, read_yn, write_yn, delete_yn, created_by, updated_by) VALUES (src.role_id, src.menu_id, src.read_yn, src.write_yn, src.delete_yn, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_MENU tgt
USING (SELECT 'ADM_OPERATOR' AS role_id, 'GATEWAY_SERVERS' AS menu_id, 'Y' AS read_yn, 'N' AS write_yn, 'N' AS delete_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_id=src.role_id AND tgt.menu_id=src.menu_id)
WHEN MATCHED THEN UPDATE SET tgt.read_yn=src.read_yn, tgt.write_yn=src.write_yn, tgt.delete_yn=src.delete_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_id, menu_id, read_yn, write_yn, delete_yn, created_by, updated_by) VALUES (src.role_id, src.menu_id, src.read_yn, src.write_yn, src.delete_yn, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_MENU tgt
USING (SELECT 'ADM_OPERATOR' AS role_id, 'GATEWAY_GROUPS' AS menu_id, 'Y' AS read_yn, 'N' AS write_yn, 'N' AS delete_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_id=src.role_id AND tgt.menu_id=src.menu_id)
WHEN MATCHED THEN UPDATE SET tgt.read_yn=src.read_yn, tgt.write_yn=src.write_yn, tgt.delete_yn=src.delete_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_id, menu_id, read_yn, write_yn, delete_yn, created_by, updated_by) VALUES (src.role_id, src.menu_id, src.read_yn, src.write_yn, src.delete_yn, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_MENU tgt
USING (SELECT 'ADM_OPERATOR' AS role_id, 'GATEWAY_ROUTES' AS menu_id, 'Y' AS read_yn, 'N' AS write_yn, 'N' AS delete_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_id=src.role_id AND tgt.menu_id=src.menu_id)
WHEN MATCHED THEN UPDATE SET tgt.read_yn=src.read_yn, tgt.write_yn=src.write_yn, tgt.delete_yn=src.delete_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_id, menu_id, read_yn, write_yn, delete_yn, created_by, updated_by) VALUES (src.role_id, src.menu_id, src.read_yn, src.write_yn, src.delete_yn, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_MENU tgt
USING (SELECT 'ADM_OPERATOR' AS role_id, 'GATEWAY_SECURITY' AS menu_id, 'Y' AS read_yn, 'N' AS write_yn, 'N' AS delete_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_id=src.role_id AND tgt.menu_id=src.menu_id)
WHEN MATCHED THEN UPDATE SET tgt.read_yn=src.read_yn, tgt.write_yn=src.write_yn, tgt.delete_yn=src.delete_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_id, menu_id, read_yn, write_yn, delete_yn, created_by, updated_by) VALUES (src.role_id, src.menu_id, src.read_yn, src.write_yn, src.delete_yn, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_MENU tgt
USING (SELECT 'ADM_OPERATOR' AS role_id, 'GATEWAY_HEALTH' AS menu_id, 'Y' AS read_yn, 'N' AS write_yn, 'N' AS delete_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_id=src.role_id AND tgt.menu_id=src.menu_id)
WHEN MATCHED THEN UPDATE SET tgt.read_yn=src.read_yn, tgt.write_yn=src.write_yn, tgt.delete_yn=src.delete_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_id, menu_id, read_yn, write_yn, delete_yn, created_by, updated_by) VALUES (src.role_id, src.menu_id, src.read_yn, src.write_yn, src.delete_yn, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_MENU tgt
USING (SELECT 'ADM_OPERATOR' AS role_id, 'GATEWAY_TRANSACTIONS' AS menu_id, 'Y' AS read_yn, 'N' AS write_yn, 'N' AS delete_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_id=src.role_id AND tgt.menu_id=src.menu_id)
WHEN MATCHED THEN UPDATE SET tgt.read_yn=src.read_yn, tgt.write_yn=src.write_yn, tgt.delete_yn=src.delete_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_id, menu_id, read_yn, write_yn, delete_yn, created_by, updated_by) VALUES (src.role_id, src.menu_id, src.read_yn, src.write_yn, src.delete_yn, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_MENU tgt
USING (SELECT 'ADM_OPERATOR' AS role_id, 'GATEWAY_LOG_POLICY' AS menu_id, 'Y' AS read_yn, 'N' AS write_yn, 'N' AS delete_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_id=src.role_id AND tgt.menu_id=src.menu_id)
WHEN MATCHED THEN UPDATE SET tgt.read_yn=src.read_yn, tgt.write_yn=src.write_yn, tgt.delete_yn=src.delete_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_id, menu_id, read_yn, write_yn, delete_yn, created_by, updated_by) VALUES (src.role_id, src.menu_id, src.read_yn, src.write_yn, src.delete_yn, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_MENU tgt
USING (SELECT 'ADM_OPERATOR' AS role_id, 'GATEWAY_APPLY_STATUS' AS menu_id, 'Y' AS read_yn, 'N' AS write_yn, 'N' AS delete_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_id=src.role_id AND tgt.menu_id=src.menu_id)
WHEN MATCHED THEN UPDATE SET tgt.read_yn=src.read_yn, tgt.write_yn=src.write_yn, tgt.delete_yn=src.delete_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_id, menu_id, read_yn, write_yn, delete_yn, created_by, updated_by) VALUES (src.role_id, src.menu_id, src.read_yn, src.write_yn, src.delete_yn, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_MENU tgt
USING (SELECT 'ADM_VIEWER' AS role_id, 'GATEWAY_DASHBOARD' AS menu_id, 'Y' AS read_yn, 'N' AS write_yn, 'N' AS delete_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_id=src.role_id AND tgt.menu_id=src.menu_id)
WHEN MATCHED THEN UPDATE SET tgt.read_yn=src.read_yn, tgt.write_yn=src.write_yn, tgt.delete_yn=src.delete_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_id, menu_id, read_yn, write_yn, delete_yn, created_by, updated_by) VALUES (src.role_id, src.menu_id, src.read_yn, src.write_yn, src.delete_yn, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_MENU tgt
USING (SELECT 'ADM_VIEWER' AS role_id, 'GATEWAY_SERVERS' AS menu_id, 'Y' AS read_yn, 'N' AS write_yn, 'N' AS delete_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_id=src.role_id AND tgt.menu_id=src.menu_id)
WHEN MATCHED THEN UPDATE SET tgt.read_yn=src.read_yn, tgt.write_yn=src.write_yn, tgt.delete_yn=src.delete_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_id, menu_id, read_yn, write_yn, delete_yn, created_by, updated_by) VALUES (src.role_id, src.menu_id, src.read_yn, src.write_yn, src.delete_yn, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_MENU tgt
USING (SELECT 'ADM_VIEWER' AS role_id, 'GATEWAY_GROUPS' AS menu_id, 'Y' AS read_yn, 'N' AS write_yn, 'N' AS delete_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_id=src.role_id AND tgt.menu_id=src.menu_id)
WHEN MATCHED THEN UPDATE SET tgt.read_yn=src.read_yn, tgt.write_yn=src.write_yn, tgt.delete_yn=src.delete_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_id, menu_id, read_yn, write_yn, delete_yn, created_by, updated_by) VALUES (src.role_id, src.menu_id, src.read_yn, src.write_yn, src.delete_yn, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_MENU tgt
USING (SELECT 'ADM_VIEWER' AS role_id, 'GATEWAY_ROUTES' AS menu_id, 'Y' AS read_yn, 'N' AS write_yn, 'N' AS delete_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_id=src.role_id AND tgt.menu_id=src.menu_id)
WHEN MATCHED THEN UPDATE SET tgt.read_yn=src.read_yn, tgt.write_yn=src.write_yn, tgt.delete_yn=src.delete_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_id, menu_id, read_yn, write_yn, delete_yn, created_by, updated_by) VALUES (src.role_id, src.menu_id, src.read_yn, src.write_yn, src.delete_yn, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_MENU tgt
USING (SELECT 'ADM_VIEWER' AS role_id, 'GATEWAY_SECURITY' AS menu_id, 'Y' AS read_yn, 'N' AS write_yn, 'N' AS delete_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_id=src.role_id AND tgt.menu_id=src.menu_id)
WHEN MATCHED THEN UPDATE SET tgt.read_yn=src.read_yn, tgt.write_yn=src.write_yn, tgt.delete_yn=src.delete_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_id, menu_id, read_yn, write_yn, delete_yn, created_by, updated_by) VALUES (src.role_id, src.menu_id, src.read_yn, src.write_yn, src.delete_yn, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_MENU tgt
USING (SELECT 'ADM_VIEWER' AS role_id, 'GATEWAY_HEALTH' AS menu_id, 'Y' AS read_yn, 'N' AS write_yn, 'N' AS delete_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_id=src.role_id AND tgt.menu_id=src.menu_id)
WHEN MATCHED THEN UPDATE SET tgt.read_yn=src.read_yn, tgt.write_yn=src.write_yn, tgt.delete_yn=src.delete_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_id, menu_id, read_yn, write_yn, delete_yn, created_by, updated_by) VALUES (src.role_id, src.menu_id, src.read_yn, src.write_yn, src.delete_yn, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_MENU tgt
USING (SELECT 'ADM_VIEWER' AS role_id, 'GATEWAY_TRANSACTIONS' AS menu_id, 'Y' AS read_yn, 'N' AS write_yn, 'N' AS delete_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_id=src.role_id AND tgt.menu_id=src.menu_id)
WHEN MATCHED THEN UPDATE SET tgt.read_yn=src.read_yn, tgt.write_yn=src.write_yn, tgt.delete_yn=src.delete_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_id, menu_id, read_yn, write_yn, delete_yn, created_by, updated_by) VALUES (src.role_id, src.menu_id, src.read_yn, src.write_yn, src.delete_yn, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_MENU tgt
USING (SELECT 'ADM_VIEWER' AS role_id, 'GATEWAY_LOG_POLICY' AS menu_id, 'Y' AS read_yn, 'N' AS write_yn, 'N' AS delete_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_id=src.role_id AND tgt.menu_id=src.menu_id)
WHEN MATCHED THEN UPDATE SET tgt.read_yn=src.read_yn, tgt.write_yn=src.write_yn, tgt.delete_yn=src.delete_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_id, menu_id, read_yn, write_yn, delete_yn, created_by, updated_by) VALUES (src.role_id, src.menu_id, src.read_yn, src.write_yn, src.delete_yn, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_MENU tgt
USING (SELECT 'ADM_VIEWER' AS role_id, 'GATEWAY_APPLY_STATUS' AS menu_id, 'Y' AS read_yn, 'N' AS write_yn, 'N' AS delete_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_id=src.role_id AND tgt.menu_id=src.menu_id)
WHEN MATCHED THEN UPDATE SET tgt.read_yn=src.read_yn, tgt.write_yn=src.write_yn, tgt.delete_yn=src.delete_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_id, menu_id, read_yn, write_yn, delete_yn, created_by, updated_by) VALUES (src.role_id, src.menu_id, src.read_yn, src.write_yn, src.delete_yn, src.created_by, src.updated_by);
MERGE INTO ADM_API_PERMISSION tgt
USING (SELECT 'API_GATEWAY_READ' AS api_permission_id, 'GATEWAY' AS api_group_code, 'GET' AS http_method, '/adm/api/gateway-registry/**' AS api_path, 'Gateway 운영 조회' AS api_name, 'READ' AS permission_code, 'GATEWAY_DASHBOARD' AS menu_id, 'GATEWAY_READ' AS button_id, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.api_permission_id=src.api_permission_id)
WHEN MATCHED THEN UPDATE SET tgt.api_group_code=src.api_group_code, tgt.http_method=src.http_method, tgt.api_path=src.api_path, tgt.api_name=src.api_name, tgt.permission_code=src.permission_code, tgt.menu_id=src.menu_id, tgt.button_id=src.button_id, tgt.use_yn=src.use_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (api_permission_id, api_group_code, http_method, api_path, api_name, permission_code, menu_id, button_id, use_yn, created_by, updated_by) VALUES (src.api_permission_id, src.api_group_code, src.http_method, src.api_path, src.api_name, src.permission_code, src.menu_id, src.button_id, src.use_yn, src.created_by, src.updated_by);
MERGE INTO ADM_API_PERMISSION tgt
USING (SELECT 'API_GATEWAY_GROUP_WRITE' AS api_permission_id, 'GATEWAY' AS api_group_code, 'POST' AS http_method, '/adm/api/gateway-registry/server-groups' AS api_path, 'Server Group 저장' AS api_name, 'WRITE' AS permission_code, 'GATEWAY_GROUPS' AS menu_id, 'GATEWAY_GROUP_WRITE' AS button_id, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.api_permission_id=src.api_permission_id)
WHEN MATCHED THEN UPDATE SET tgt.api_group_code=src.api_group_code, tgt.http_method=src.http_method, tgt.api_path=src.api_path, tgt.api_name=src.api_name, tgt.permission_code=src.permission_code, tgt.menu_id=src.menu_id, tgt.button_id=src.button_id, tgt.use_yn=src.use_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (api_permission_id, api_group_code, http_method, api_path, api_name, permission_code, menu_id, button_id, use_yn, created_by, updated_by) VALUES (src.api_permission_id, src.api_group_code, src.http_method, src.api_path, src.api_name, src.permission_code, src.menu_id, src.button_id, src.use_yn, src.created_by, src.updated_by);
MERGE INTO ADM_API_PERMISSION tgt
USING (SELECT 'API_GATEWAY_GROUP_DELETE' AS api_permission_id, 'GATEWAY' AS api_group_code, 'DELETE' AS http_method, '/adm/api/gateway-registry/server-groups/*' AS api_path, 'Server Group 폐기' AS api_name, 'DELETE' AS permission_code, 'GATEWAY_GROUPS' AS menu_id, 'GATEWAY_GROUP_DELETE' AS button_id, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.api_permission_id=src.api_permission_id)
WHEN MATCHED THEN UPDATE SET tgt.api_group_code=src.api_group_code, tgt.http_method=src.http_method, tgt.api_path=src.api_path, tgt.api_name=src.api_name, tgt.permission_code=src.permission_code, tgt.menu_id=src.menu_id, tgt.button_id=src.button_id, tgt.use_yn=src.use_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (api_permission_id, api_group_code, http_method, api_path, api_name, permission_code, menu_id, button_id, use_yn, created_by, updated_by) VALUES (src.api_permission_id, src.api_group_code, src.http_method, src.api_path, src.api_name, src.permission_code, src.menu_id, src.button_id, src.use_yn, src.created_by, src.updated_by);
MERGE INTO ADM_API_PERMISSION tgt
USING (SELECT 'API_GATEWAY_ROUTE_WRITE' AS api_permission_id, 'GATEWAY' AS api_group_code, 'POST' AS http_method, '/adm/api/gateway-registry/bindings' AS api_path, 'Gateway Binding 저장' AS api_name, 'WRITE' AS permission_code, 'GATEWAY_ROUTES' AS menu_id, 'GATEWAY_ROUTE_WRITE' AS button_id, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.api_permission_id=src.api_permission_id)
WHEN MATCHED THEN UPDATE SET tgt.api_group_code=src.api_group_code, tgt.http_method=src.http_method, tgt.api_path=src.api_path, tgt.api_name=src.api_name, tgt.permission_code=src.permission_code, tgt.menu_id=src.menu_id, tgt.button_id=src.button_id, tgt.use_yn=src.use_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (api_permission_id, api_group_code, http_method, api_path, api_name, permission_code, menu_id, button_id, use_yn, created_by, updated_by) VALUES (src.api_permission_id, src.api_group_code, src.http_method, src.api_path, src.api_name, src.permission_code, src.menu_id, src.button_id, src.use_yn, src.created_by, src.updated_by);
MERGE INTO ADM_API_PERMISSION tgt
USING (SELECT 'API_GATEWAY_ROUTE_STATE' AS api_permission_id, 'GATEWAY' AS api_group_code, 'POST' AS http_method, '/adm/api/gateway-registry/bindings/*/state' AS api_path, 'Gateway Binding 상태 변경' AS api_name, 'CONTROL' AS permission_code, 'GATEWAY_ROUTES' AS menu_id, 'GATEWAY_ROUTE_STATE' AS button_id, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.api_permission_id=src.api_permission_id)
WHEN MATCHED THEN UPDATE SET tgt.api_group_code=src.api_group_code, tgt.http_method=src.http_method, tgt.api_path=src.api_path, tgt.api_name=src.api_name, tgt.permission_code=src.permission_code, tgt.menu_id=src.menu_id, tgt.button_id=src.button_id, tgt.use_yn=src.use_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (api_permission_id, api_group_code, http_method, api_path, api_name, permission_code, menu_id, button_id, use_yn, created_by, updated_by) VALUES (src.api_permission_id, src.api_group_code, src.http_method, src.api_path, src.api_name, src.permission_code, src.menu_id, src.button_id, src.use_yn, src.created_by, src.updated_by);
MERGE INTO ADM_API_PERMISSION tgt
USING (SELECT 'API_GATEWAY_ROUTE_DELETE' AS api_permission_id, 'GATEWAY' AS api_group_code, 'DELETE' AS http_method, '/adm/api/gateway-registry/bindings/*' AS api_path, 'Gateway Binding 폐기' AS api_name, 'DELETE' AS permission_code, 'GATEWAY_ROUTES' AS menu_id, 'GATEWAY_ROUTE_DELETE' AS button_id, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.api_permission_id=src.api_permission_id)
WHEN MATCHED THEN UPDATE SET tgt.api_group_code=src.api_group_code, tgt.http_method=src.http_method, tgt.api_path=src.api_path, tgt.api_name=src.api_name, tgt.permission_code=src.permission_code, tgt.menu_id=src.menu_id, tgt.button_id=src.button_id, tgt.use_yn=src.use_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (api_permission_id, api_group_code, http_method, api_path, api_name, permission_code, menu_id, button_id, use_yn, created_by, updated_by) VALUES (src.api_permission_id, src.api_group_code, src.http_method, src.api_path, src.api_name, src.permission_code, src.menu_id, src.button_id, src.use_yn, src.created_by, src.updated_by);
MERGE INTO ADM_API_PERMISSION tgt
USING (SELECT 'API_GATEWAY_CONNECTION_TEST' AS api_permission_id, 'GATEWAY' AS api_group_code, 'POST' AS http_method, '/adm/api/gateway-registry/bindings/*/connection-tests' AS api_path, 'Gateway 연결시험 요청' AS api_name, 'TEST' AS permission_code, 'GATEWAY_HEALTH' AS menu_id, 'GATEWAY_CONNECTION_TEST' AS button_id, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.api_permission_id=src.api_permission_id)
WHEN MATCHED THEN UPDATE SET tgt.api_group_code=src.api_group_code, tgt.http_method=src.http_method, tgt.api_path=src.api_path, tgt.api_name=src.api_name, tgt.permission_code=src.permission_code, tgt.menu_id=src.menu_id, tgt.button_id=src.button_id, tgt.use_yn=src.use_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (api_permission_id, api_group_code, http_method, api_path, api_name, permission_code, menu_id, button_id, use_yn, created_by, updated_by) VALUES (src.api_permission_id, src.api_group_code, src.http_method, src.api_path, src.api_name, src.permission_code, src.menu_id, src.button_id, src.use_yn, src.created_by, src.updated_by);
MERGE INTO ADM_API_PERMISSION tgt
USING (SELECT 'API_GATEWAY_TEST_CONTROL' AS api_permission_id, 'GATEWAY' AS api_group_code, 'POST' AS http_method, '/adm/api/gateway-registry/connection-test-operations/*/**' AS api_path, 'Gateway 연결시험 취소·재검증' AS api_name, 'CONTROL' AS permission_code, 'GATEWAY_HEALTH' AS menu_id, 'GATEWAY_TEST_CONTROL' AS button_id, 'Y' AS use_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.api_permission_id=src.api_permission_id)
WHEN MATCHED THEN UPDATE SET tgt.api_group_code=src.api_group_code, tgt.http_method=src.http_method, tgt.api_path=src.api_path, tgt.api_name=src.api_name, tgt.permission_code=src.permission_code, tgt.menu_id=src.menu_id, tgt.button_id=src.button_id, tgt.use_yn=src.use_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (api_permission_id, api_group_code, http_method, api_path, api_name, permission_code, menu_id, button_id, use_yn, created_by, updated_by) VALUES (src.api_permission_id, src.api_group_code, src.http_method, src.api_path, src.api_name, src.permission_code, src.menu_id, src.button_id, src.use_yn, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_BUTTON tgt
USING (SELECT 'ADM_ADMIN' AS role_id, 'GATEWAY_READ' AS button_id, 'Y' AS allow_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_id=src.role_id AND tgt.button_id=src.button_id)
WHEN MATCHED THEN UPDATE SET tgt.allow_yn=src.allow_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_id, button_id, allow_yn, created_by, updated_by) VALUES (src.role_id, src.button_id, src.allow_yn, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_BUTTON tgt
USING (SELECT 'ADM_ADMIN' AS role_id, 'GATEWAY_GROUP_WRITE' AS button_id, 'Y' AS allow_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_id=src.role_id AND tgt.button_id=src.button_id)
WHEN MATCHED THEN UPDATE SET tgt.allow_yn=src.allow_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_id, button_id, allow_yn, created_by, updated_by) VALUES (src.role_id, src.button_id, src.allow_yn, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_BUTTON tgt
USING (SELECT 'ADM_ADMIN' AS role_id, 'GATEWAY_GROUP_DELETE' AS button_id, 'Y' AS allow_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_id=src.role_id AND tgt.button_id=src.button_id)
WHEN MATCHED THEN UPDATE SET tgt.allow_yn=src.allow_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_id, button_id, allow_yn, created_by, updated_by) VALUES (src.role_id, src.button_id, src.allow_yn, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_BUTTON tgt
USING (SELECT 'ADM_ADMIN' AS role_id, 'GATEWAY_ROUTE_WRITE' AS button_id, 'Y' AS allow_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_id=src.role_id AND tgt.button_id=src.button_id)
WHEN MATCHED THEN UPDATE SET tgt.allow_yn=src.allow_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_id, button_id, allow_yn, created_by, updated_by) VALUES (src.role_id, src.button_id, src.allow_yn, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_BUTTON tgt
USING (SELECT 'ADM_ADMIN' AS role_id, 'GATEWAY_ROUTE_STATE' AS button_id, 'Y' AS allow_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_id=src.role_id AND tgt.button_id=src.button_id)
WHEN MATCHED THEN UPDATE SET tgt.allow_yn=src.allow_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_id, button_id, allow_yn, created_by, updated_by) VALUES (src.role_id, src.button_id, src.allow_yn, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_BUTTON tgt
USING (SELECT 'ADM_ADMIN' AS role_id, 'GATEWAY_ROUTE_DELETE' AS button_id, 'Y' AS allow_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_id=src.role_id AND tgt.button_id=src.button_id)
WHEN MATCHED THEN UPDATE SET tgt.allow_yn=src.allow_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_id, button_id, allow_yn, created_by, updated_by) VALUES (src.role_id, src.button_id, src.allow_yn, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_BUTTON tgt
USING (SELECT 'ADM_ADMIN' AS role_id, 'GATEWAY_CONNECTION_TEST' AS button_id, 'Y' AS allow_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_id=src.role_id AND tgt.button_id=src.button_id)
WHEN MATCHED THEN UPDATE SET tgt.allow_yn=src.allow_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_id, button_id, allow_yn, created_by, updated_by) VALUES (src.role_id, src.button_id, src.allow_yn, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_BUTTON tgt
USING (SELECT 'ADM_ADMIN' AS role_id, 'GATEWAY_TEST_CONTROL' AS button_id, 'Y' AS allow_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_id=src.role_id AND tgt.button_id=src.button_id)
WHEN MATCHED THEN UPDATE SET tgt.allow_yn=src.allow_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_id, button_id, allow_yn, created_by, updated_by) VALUES (src.role_id, src.button_id, src.allow_yn, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_BUTTON tgt
USING (SELECT 'ADM_DEV_OPERATOR' AS role_id, 'GATEWAY_READ' AS button_id, 'Y' AS allow_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_id=src.role_id AND tgt.button_id=src.button_id)
WHEN MATCHED THEN UPDATE SET tgt.allow_yn=src.allow_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_id, button_id, allow_yn, created_by, updated_by) VALUES (src.role_id, src.button_id, src.allow_yn, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_BUTTON tgt
USING (SELECT 'ADM_DEV_OPERATOR' AS role_id, 'GATEWAY_GROUP_WRITE' AS button_id, 'Y' AS allow_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_id=src.role_id AND tgt.button_id=src.button_id)
WHEN MATCHED THEN UPDATE SET tgt.allow_yn=src.allow_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_id, button_id, allow_yn, created_by, updated_by) VALUES (src.role_id, src.button_id, src.allow_yn, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_BUTTON tgt
USING (SELECT 'ADM_DEV_OPERATOR' AS role_id, 'GATEWAY_GROUP_DELETE' AS button_id, 'N' AS allow_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_id=src.role_id AND tgt.button_id=src.button_id)
WHEN MATCHED THEN UPDATE SET tgt.allow_yn=src.allow_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_id, button_id, allow_yn, created_by, updated_by) VALUES (src.role_id, src.button_id, src.allow_yn, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_BUTTON tgt
USING (SELECT 'ADM_DEV_OPERATOR' AS role_id, 'GATEWAY_ROUTE_WRITE' AS button_id, 'Y' AS allow_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_id=src.role_id AND tgt.button_id=src.button_id)
WHEN MATCHED THEN UPDATE SET tgt.allow_yn=src.allow_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_id, button_id, allow_yn, created_by, updated_by) VALUES (src.role_id, src.button_id, src.allow_yn, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_BUTTON tgt
USING (SELECT 'ADM_DEV_OPERATOR' AS role_id, 'GATEWAY_ROUTE_STATE' AS button_id, 'Y' AS allow_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_id=src.role_id AND tgt.button_id=src.button_id)
WHEN MATCHED THEN UPDATE SET tgt.allow_yn=src.allow_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_id, button_id, allow_yn, created_by, updated_by) VALUES (src.role_id, src.button_id, src.allow_yn, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_BUTTON tgt
USING (SELECT 'ADM_DEV_OPERATOR' AS role_id, 'GATEWAY_ROUTE_DELETE' AS button_id, 'N' AS allow_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_id=src.role_id AND tgt.button_id=src.button_id)
WHEN MATCHED THEN UPDATE SET tgt.allow_yn=src.allow_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_id, button_id, allow_yn, created_by, updated_by) VALUES (src.role_id, src.button_id, src.allow_yn, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_BUTTON tgt
USING (SELECT 'ADM_DEV_OPERATOR' AS role_id, 'GATEWAY_CONNECTION_TEST' AS button_id, 'Y' AS allow_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_id=src.role_id AND tgt.button_id=src.button_id)
WHEN MATCHED THEN UPDATE SET tgt.allow_yn=src.allow_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_id, button_id, allow_yn, created_by, updated_by) VALUES (src.role_id, src.button_id, src.allow_yn, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_BUTTON tgt
USING (SELECT 'ADM_DEV_OPERATOR' AS role_id, 'GATEWAY_TEST_CONTROL' AS button_id, 'Y' AS allow_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_id=src.role_id AND tgt.button_id=src.button_id)
WHEN MATCHED THEN UPDATE SET tgt.allow_yn=src.allow_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_id, button_id, allow_yn, created_by, updated_by) VALUES (src.role_id, src.button_id, src.allow_yn, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_BUTTON tgt
USING (SELECT 'ADM_OPERATOR' AS role_id, 'GATEWAY_READ' AS button_id, 'Y' AS allow_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_id=src.role_id AND tgt.button_id=src.button_id)
WHEN MATCHED THEN UPDATE SET tgt.allow_yn=src.allow_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_id, button_id, allow_yn, created_by, updated_by) VALUES (src.role_id, src.button_id, src.allow_yn, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_BUTTON tgt
USING (SELECT 'ADM_OPERATOR' AS role_id, 'GATEWAY_GROUP_WRITE' AS button_id, 'N' AS allow_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_id=src.role_id AND tgt.button_id=src.button_id)
WHEN MATCHED THEN UPDATE SET tgt.allow_yn=src.allow_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_id, button_id, allow_yn, created_by, updated_by) VALUES (src.role_id, src.button_id, src.allow_yn, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_BUTTON tgt
USING (SELECT 'ADM_OPERATOR' AS role_id, 'GATEWAY_GROUP_DELETE' AS button_id, 'N' AS allow_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_id=src.role_id AND tgt.button_id=src.button_id)
WHEN MATCHED THEN UPDATE SET tgt.allow_yn=src.allow_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_id, button_id, allow_yn, created_by, updated_by) VALUES (src.role_id, src.button_id, src.allow_yn, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_BUTTON tgt
USING (SELECT 'ADM_OPERATOR' AS role_id, 'GATEWAY_ROUTE_WRITE' AS button_id, 'N' AS allow_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_id=src.role_id AND tgt.button_id=src.button_id)
WHEN MATCHED THEN UPDATE SET tgt.allow_yn=src.allow_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_id, button_id, allow_yn, created_by, updated_by) VALUES (src.role_id, src.button_id, src.allow_yn, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_BUTTON tgt
USING (SELECT 'ADM_OPERATOR' AS role_id, 'GATEWAY_ROUTE_STATE' AS button_id, 'N' AS allow_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_id=src.role_id AND tgt.button_id=src.button_id)
WHEN MATCHED THEN UPDATE SET tgt.allow_yn=src.allow_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_id, button_id, allow_yn, created_by, updated_by) VALUES (src.role_id, src.button_id, src.allow_yn, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_BUTTON tgt
USING (SELECT 'ADM_OPERATOR' AS role_id, 'GATEWAY_ROUTE_DELETE' AS button_id, 'N' AS allow_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_id=src.role_id AND tgt.button_id=src.button_id)
WHEN MATCHED THEN UPDATE SET tgt.allow_yn=src.allow_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_id, button_id, allow_yn, created_by, updated_by) VALUES (src.role_id, src.button_id, src.allow_yn, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_BUTTON tgt
USING (SELECT 'ADM_OPERATOR' AS role_id, 'GATEWAY_CONNECTION_TEST' AS button_id, 'Y' AS allow_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_id=src.role_id AND tgt.button_id=src.button_id)
WHEN MATCHED THEN UPDATE SET tgt.allow_yn=src.allow_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_id, button_id, allow_yn, created_by, updated_by) VALUES (src.role_id, src.button_id, src.allow_yn, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_BUTTON tgt
USING (SELECT 'ADM_OPERATOR' AS role_id, 'GATEWAY_TEST_CONTROL' AS button_id, 'N' AS allow_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_id=src.role_id AND tgt.button_id=src.button_id)
WHEN MATCHED THEN UPDATE SET tgt.allow_yn=src.allow_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_id, button_id, allow_yn, created_by, updated_by) VALUES (src.role_id, src.button_id, src.allow_yn, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_BUTTON tgt
USING (SELECT 'ADM_VIEWER' AS role_id, 'GATEWAY_READ' AS button_id, 'Y' AS allow_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_id=src.role_id AND tgt.button_id=src.button_id)
WHEN MATCHED THEN UPDATE SET tgt.allow_yn=src.allow_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_id, button_id, allow_yn, created_by, updated_by) VALUES (src.role_id, src.button_id, src.allow_yn, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_BUTTON tgt
USING (SELECT 'ADM_VIEWER' AS role_id, 'GATEWAY_GROUP_WRITE' AS button_id, 'N' AS allow_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_id=src.role_id AND tgt.button_id=src.button_id)
WHEN MATCHED THEN UPDATE SET tgt.allow_yn=src.allow_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_id, button_id, allow_yn, created_by, updated_by) VALUES (src.role_id, src.button_id, src.allow_yn, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_BUTTON tgt
USING (SELECT 'ADM_VIEWER' AS role_id, 'GATEWAY_GROUP_DELETE' AS button_id, 'N' AS allow_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_id=src.role_id AND tgt.button_id=src.button_id)
WHEN MATCHED THEN UPDATE SET tgt.allow_yn=src.allow_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_id, button_id, allow_yn, created_by, updated_by) VALUES (src.role_id, src.button_id, src.allow_yn, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_BUTTON tgt
USING (SELECT 'ADM_VIEWER' AS role_id, 'GATEWAY_ROUTE_WRITE' AS button_id, 'N' AS allow_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_id=src.role_id AND tgt.button_id=src.button_id)
WHEN MATCHED THEN UPDATE SET tgt.allow_yn=src.allow_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_id, button_id, allow_yn, created_by, updated_by) VALUES (src.role_id, src.button_id, src.allow_yn, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_BUTTON tgt
USING (SELECT 'ADM_VIEWER' AS role_id, 'GATEWAY_ROUTE_STATE' AS button_id, 'N' AS allow_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_id=src.role_id AND tgt.button_id=src.button_id)
WHEN MATCHED THEN UPDATE SET tgt.allow_yn=src.allow_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_id, button_id, allow_yn, created_by, updated_by) VALUES (src.role_id, src.button_id, src.allow_yn, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_BUTTON tgt
USING (SELECT 'ADM_VIEWER' AS role_id, 'GATEWAY_ROUTE_DELETE' AS button_id, 'N' AS allow_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_id=src.role_id AND tgt.button_id=src.button_id)
WHEN MATCHED THEN UPDATE SET tgt.allow_yn=src.allow_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_id, button_id, allow_yn, created_by, updated_by) VALUES (src.role_id, src.button_id, src.allow_yn, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_BUTTON tgt
USING (SELECT 'ADM_VIEWER' AS role_id, 'GATEWAY_CONNECTION_TEST' AS button_id, 'N' AS allow_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_id=src.role_id AND tgt.button_id=src.button_id)
WHEN MATCHED THEN UPDATE SET tgt.allow_yn=src.allow_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_id, button_id, allow_yn, created_by, updated_by) VALUES (src.role_id, src.button_id, src.allow_yn, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_BUTTON tgt
USING (SELECT 'ADM_VIEWER' AS role_id, 'GATEWAY_TEST_CONTROL' AS button_id, 'N' AS allow_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_id=src.role_id AND tgt.button_id=src.button_id)
WHEN MATCHED THEN UPDATE SET tgt.allow_yn=src.allow_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_id, button_id, allow_yn, created_by, updated_by) VALUES (src.role_id, src.button_id, src.allow_yn, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_API_PERMISSION tgt
USING (SELECT 'ADM_ADMIN' AS role_id, 'API_GATEWAY_READ' AS api_permission_id, 'Y' AS allow_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_id=src.role_id AND tgt.api_permission_id=src.api_permission_id)
WHEN MATCHED THEN UPDATE SET tgt.allow_yn=src.allow_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_id, api_permission_id, allow_yn, created_by, updated_by) VALUES (src.role_id, src.api_permission_id, src.allow_yn, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_API_PERMISSION tgt
USING (SELECT 'ADM_ADMIN' AS role_id, 'API_GATEWAY_GROUP_WRITE' AS api_permission_id, 'Y' AS allow_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_id=src.role_id AND tgt.api_permission_id=src.api_permission_id)
WHEN MATCHED THEN UPDATE SET tgt.allow_yn=src.allow_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_id, api_permission_id, allow_yn, created_by, updated_by) VALUES (src.role_id, src.api_permission_id, src.allow_yn, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_API_PERMISSION tgt
USING (SELECT 'ADM_ADMIN' AS role_id, 'API_GATEWAY_GROUP_DELETE' AS api_permission_id, 'Y' AS allow_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_id=src.role_id AND tgt.api_permission_id=src.api_permission_id)
WHEN MATCHED THEN UPDATE SET tgt.allow_yn=src.allow_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_id, api_permission_id, allow_yn, created_by, updated_by) VALUES (src.role_id, src.api_permission_id, src.allow_yn, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_API_PERMISSION tgt
USING (SELECT 'ADM_ADMIN' AS role_id, 'API_GATEWAY_ROUTE_WRITE' AS api_permission_id, 'Y' AS allow_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_id=src.role_id AND tgt.api_permission_id=src.api_permission_id)
WHEN MATCHED THEN UPDATE SET tgt.allow_yn=src.allow_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_id, api_permission_id, allow_yn, created_by, updated_by) VALUES (src.role_id, src.api_permission_id, src.allow_yn, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_API_PERMISSION tgt
USING (SELECT 'ADM_ADMIN' AS role_id, 'API_GATEWAY_ROUTE_STATE' AS api_permission_id, 'Y' AS allow_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_id=src.role_id AND tgt.api_permission_id=src.api_permission_id)
WHEN MATCHED THEN UPDATE SET tgt.allow_yn=src.allow_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_id, api_permission_id, allow_yn, created_by, updated_by) VALUES (src.role_id, src.api_permission_id, src.allow_yn, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_API_PERMISSION tgt
USING (SELECT 'ADM_ADMIN' AS role_id, 'API_GATEWAY_ROUTE_DELETE' AS api_permission_id, 'Y' AS allow_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_id=src.role_id AND tgt.api_permission_id=src.api_permission_id)
WHEN MATCHED THEN UPDATE SET tgt.allow_yn=src.allow_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_id, api_permission_id, allow_yn, created_by, updated_by) VALUES (src.role_id, src.api_permission_id, src.allow_yn, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_API_PERMISSION tgt
USING (SELECT 'ADM_ADMIN' AS role_id, 'API_GATEWAY_CONNECTION_TEST' AS api_permission_id, 'Y' AS allow_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_id=src.role_id AND tgt.api_permission_id=src.api_permission_id)
WHEN MATCHED THEN UPDATE SET tgt.allow_yn=src.allow_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_id, api_permission_id, allow_yn, created_by, updated_by) VALUES (src.role_id, src.api_permission_id, src.allow_yn, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_API_PERMISSION tgt
USING (SELECT 'ADM_ADMIN' AS role_id, 'API_GATEWAY_TEST_CONTROL' AS api_permission_id, 'Y' AS allow_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_id=src.role_id AND tgt.api_permission_id=src.api_permission_id)
WHEN MATCHED THEN UPDATE SET tgt.allow_yn=src.allow_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_id, api_permission_id, allow_yn, created_by, updated_by) VALUES (src.role_id, src.api_permission_id, src.allow_yn, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_API_PERMISSION tgt
USING (SELECT 'ADM_DEV_OPERATOR' AS role_id, 'API_GATEWAY_READ' AS api_permission_id, 'Y' AS allow_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_id=src.role_id AND tgt.api_permission_id=src.api_permission_id)
WHEN MATCHED THEN UPDATE SET tgt.allow_yn=src.allow_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_id, api_permission_id, allow_yn, created_by, updated_by) VALUES (src.role_id, src.api_permission_id, src.allow_yn, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_API_PERMISSION tgt
USING (SELECT 'ADM_DEV_OPERATOR' AS role_id, 'API_GATEWAY_GROUP_WRITE' AS api_permission_id, 'Y' AS allow_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_id=src.role_id AND tgt.api_permission_id=src.api_permission_id)
WHEN MATCHED THEN UPDATE SET tgt.allow_yn=src.allow_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_id, api_permission_id, allow_yn, created_by, updated_by) VALUES (src.role_id, src.api_permission_id, src.allow_yn, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_API_PERMISSION tgt
USING (SELECT 'ADM_DEV_OPERATOR' AS role_id, 'API_GATEWAY_GROUP_DELETE' AS api_permission_id, 'N' AS allow_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_id=src.role_id AND tgt.api_permission_id=src.api_permission_id)
WHEN MATCHED THEN UPDATE SET tgt.allow_yn=src.allow_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_id, api_permission_id, allow_yn, created_by, updated_by) VALUES (src.role_id, src.api_permission_id, src.allow_yn, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_API_PERMISSION tgt
USING (SELECT 'ADM_DEV_OPERATOR' AS role_id, 'API_GATEWAY_ROUTE_WRITE' AS api_permission_id, 'Y' AS allow_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_id=src.role_id AND tgt.api_permission_id=src.api_permission_id)
WHEN MATCHED THEN UPDATE SET tgt.allow_yn=src.allow_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_id, api_permission_id, allow_yn, created_by, updated_by) VALUES (src.role_id, src.api_permission_id, src.allow_yn, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_API_PERMISSION tgt
USING (SELECT 'ADM_DEV_OPERATOR' AS role_id, 'API_GATEWAY_ROUTE_STATE' AS api_permission_id, 'Y' AS allow_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_id=src.role_id AND tgt.api_permission_id=src.api_permission_id)
WHEN MATCHED THEN UPDATE SET tgt.allow_yn=src.allow_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_id, api_permission_id, allow_yn, created_by, updated_by) VALUES (src.role_id, src.api_permission_id, src.allow_yn, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_API_PERMISSION tgt
USING (SELECT 'ADM_DEV_OPERATOR' AS role_id, 'API_GATEWAY_ROUTE_DELETE' AS api_permission_id, 'N' AS allow_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_id=src.role_id AND tgt.api_permission_id=src.api_permission_id)
WHEN MATCHED THEN UPDATE SET tgt.allow_yn=src.allow_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_id, api_permission_id, allow_yn, created_by, updated_by) VALUES (src.role_id, src.api_permission_id, src.allow_yn, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_API_PERMISSION tgt
USING (SELECT 'ADM_DEV_OPERATOR' AS role_id, 'API_GATEWAY_CONNECTION_TEST' AS api_permission_id, 'Y' AS allow_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_id=src.role_id AND tgt.api_permission_id=src.api_permission_id)
WHEN MATCHED THEN UPDATE SET tgt.allow_yn=src.allow_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_id, api_permission_id, allow_yn, created_by, updated_by) VALUES (src.role_id, src.api_permission_id, src.allow_yn, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_API_PERMISSION tgt
USING (SELECT 'ADM_DEV_OPERATOR' AS role_id, 'API_GATEWAY_TEST_CONTROL' AS api_permission_id, 'Y' AS allow_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_id=src.role_id AND tgt.api_permission_id=src.api_permission_id)
WHEN MATCHED THEN UPDATE SET tgt.allow_yn=src.allow_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_id, api_permission_id, allow_yn, created_by, updated_by) VALUES (src.role_id, src.api_permission_id, src.allow_yn, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_API_PERMISSION tgt
USING (SELECT 'ADM_OPERATOR' AS role_id, 'API_GATEWAY_READ' AS api_permission_id, 'Y' AS allow_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_id=src.role_id AND tgt.api_permission_id=src.api_permission_id)
WHEN MATCHED THEN UPDATE SET tgt.allow_yn=src.allow_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_id, api_permission_id, allow_yn, created_by, updated_by) VALUES (src.role_id, src.api_permission_id, src.allow_yn, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_API_PERMISSION tgt
USING (SELECT 'ADM_OPERATOR' AS role_id, 'API_GATEWAY_GROUP_WRITE' AS api_permission_id, 'N' AS allow_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_id=src.role_id AND tgt.api_permission_id=src.api_permission_id)
WHEN MATCHED THEN UPDATE SET tgt.allow_yn=src.allow_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_id, api_permission_id, allow_yn, created_by, updated_by) VALUES (src.role_id, src.api_permission_id, src.allow_yn, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_API_PERMISSION tgt
USING (SELECT 'ADM_OPERATOR' AS role_id, 'API_GATEWAY_GROUP_DELETE' AS api_permission_id, 'N' AS allow_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_id=src.role_id AND tgt.api_permission_id=src.api_permission_id)
WHEN MATCHED THEN UPDATE SET tgt.allow_yn=src.allow_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_id, api_permission_id, allow_yn, created_by, updated_by) VALUES (src.role_id, src.api_permission_id, src.allow_yn, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_API_PERMISSION tgt
USING (SELECT 'ADM_OPERATOR' AS role_id, 'API_GATEWAY_ROUTE_WRITE' AS api_permission_id, 'N' AS allow_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_id=src.role_id AND tgt.api_permission_id=src.api_permission_id)
WHEN MATCHED THEN UPDATE SET tgt.allow_yn=src.allow_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_id, api_permission_id, allow_yn, created_by, updated_by) VALUES (src.role_id, src.api_permission_id, src.allow_yn, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_API_PERMISSION tgt
USING (SELECT 'ADM_OPERATOR' AS role_id, 'API_GATEWAY_ROUTE_STATE' AS api_permission_id, 'N' AS allow_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_id=src.role_id AND tgt.api_permission_id=src.api_permission_id)
WHEN MATCHED THEN UPDATE SET tgt.allow_yn=src.allow_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_id, api_permission_id, allow_yn, created_by, updated_by) VALUES (src.role_id, src.api_permission_id, src.allow_yn, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_API_PERMISSION tgt
USING (SELECT 'ADM_OPERATOR' AS role_id, 'API_GATEWAY_ROUTE_DELETE' AS api_permission_id, 'N' AS allow_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_id=src.role_id AND tgt.api_permission_id=src.api_permission_id)
WHEN MATCHED THEN UPDATE SET tgt.allow_yn=src.allow_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_id, api_permission_id, allow_yn, created_by, updated_by) VALUES (src.role_id, src.api_permission_id, src.allow_yn, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_API_PERMISSION tgt
USING (SELECT 'ADM_OPERATOR' AS role_id, 'API_GATEWAY_CONNECTION_TEST' AS api_permission_id, 'Y' AS allow_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_id=src.role_id AND tgt.api_permission_id=src.api_permission_id)
WHEN MATCHED THEN UPDATE SET tgt.allow_yn=src.allow_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_id, api_permission_id, allow_yn, created_by, updated_by) VALUES (src.role_id, src.api_permission_id, src.allow_yn, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_API_PERMISSION tgt
USING (SELECT 'ADM_OPERATOR' AS role_id, 'API_GATEWAY_TEST_CONTROL' AS api_permission_id, 'N' AS allow_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_id=src.role_id AND tgt.api_permission_id=src.api_permission_id)
WHEN MATCHED THEN UPDATE SET tgt.allow_yn=src.allow_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_id, api_permission_id, allow_yn, created_by, updated_by) VALUES (src.role_id, src.api_permission_id, src.allow_yn, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_API_PERMISSION tgt
USING (SELECT 'ADM_VIEWER' AS role_id, 'API_GATEWAY_READ' AS api_permission_id, 'Y' AS allow_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_id=src.role_id AND tgt.api_permission_id=src.api_permission_id)
WHEN MATCHED THEN UPDATE SET tgt.allow_yn=src.allow_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_id, api_permission_id, allow_yn, created_by, updated_by) VALUES (src.role_id, src.api_permission_id, src.allow_yn, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_API_PERMISSION tgt
USING (SELECT 'ADM_VIEWER' AS role_id, 'API_GATEWAY_GROUP_WRITE' AS api_permission_id, 'N' AS allow_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_id=src.role_id AND tgt.api_permission_id=src.api_permission_id)
WHEN MATCHED THEN UPDATE SET tgt.allow_yn=src.allow_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_id, api_permission_id, allow_yn, created_by, updated_by) VALUES (src.role_id, src.api_permission_id, src.allow_yn, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_API_PERMISSION tgt
USING (SELECT 'ADM_VIEWER' AS role_id, 'API_GATEWAY_GROUP_DELETE' AS api_permission_id, 'N' AS allow_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_id=src.role_id AND tgt.api_permission_id=src.api_permission_id)
WHEN MATCHED THEN UPDATE SET tgt.allow_yn=src.allow_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_id, api_permission_id, allow_yn, created_by, updated_by) VALUES (src.role_id, src.api_permission_id, src.allow_yn, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_API_PERMISSION tgt
USING (SELECT 'ADM_VIEWER' AS role_id, 'API_GATEWAY_ROUTE_WRITE' AS api_permission_id, 'N' AS allow_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_id=src.role_id AND tgt.api_permission_id=src.api_permission_id)
WHEN MATCHED THEN UPDATE SET tgt.allow_yn=src.allow_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_id, api_permission_id, allow_yn, created_by, updated_by) VALUES (src.role_id, src.api_permission_id, src.allow_yn, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_API_PERMISSION tgt
USING (SELECT 'ADM_VIEWER' AS role_id, 'API_GATEWAY_ROUTE_STATE' AS api_permission_id, 'N' AS allow_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_id=src.role_id AND tgt.api_permission_id=src.api_permission_id)
WHEN MATCHED THEN UPDATE SET tgt.allow_yn=src.allow_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_id, api_permission_id, allow_yn, created_by, updated_by) VALUES (src.role_id, src.api_permission_id, src.allow_yn, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_API_PERMISSION tgt
USING (SELECT 'ADM_VIEWER' AS role_id, 'API_GATEWAY_ROUTE_DELETE' AS api_permission_id, 'N' AS allow_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_id=src.role_id AND tgt.api_permission_id=src.api_permission_id)
WHEN MATCHED THEN UPDATE SET tgt.allow_yn=src.allow_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_id, api_permission_id, allow_yn, created_by, updated_by) VALUES (src.role_id, src.api_permission_id, src.allow_yn, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_API_PERMISSION tgt
USING (SELECT 'ADM_VIEWER' AS role_id, 'API_GATEWAY_CONNECTION_TEST' AS api_permission_id, 'N' AS allow_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_id=src.role_id AND tgt.api_permission_id=src.api_permission_id)
WHEN MATCHED THEN UPDATE SET tgt.allow_yn=src.allow_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_id, api_permission_id, allow_yn, created_by, updated_by) VALUES (src.role_id, src.api_permission_id, src.allow_yn, src.created_by, src.updated_by);
MERGE INTO ADM_ROLE_API_PERMISSION tgt
USING (SELECT 'ADM_VIEWER' AS role_id, 'API_GATEWAY_TEST_CONTROL' AS api_permission_id, 'N' AS allow_yn, 'SYSTEM' AS created_by, 'SYSTEM' AS updated_by FROM dual) src
ON (tgt.role_id=src.role_id AND tgt.api_permission_id=src.api_permission_id)
WHEN MATCHED THEN UPDATE SET tgt.allow_yn=src.allow_yn, tgt.updated_by=src.updated_by, tgt.updated_at=CURRENT_TIMESTAMP
WHEN NOT MATCHED THEN INSERT (role_id, api_permission_id, allow_yn, created_by, updated_by) VALUES (src.role_id, src.api_permission_id, src.allow_yn, src.created_by, src.updated_by);
-- ===== END 61_adm_gateway_seed.sql =====
