-- V37 공식 REF/EXS 확장의 논리 rollback입니다.
-- 데이터 손실을 막기 위해 신규 테이블과 컬럼은 보존하고 운영 진입점만 비활성화합니다.

USE cpfDB;

UPDATE cpf_service SET use_yn = 'N', updated_by = 'ROLLBACK', updated_at = CURRENT_TIMESTAMP WHERE service_id = 'REF';
UPDATE cpf_service_endpoint SET use_yn = 'N', updated_by = 'ROLLBACK', updated_at = CURRENT_TIMESTAMP WHERE service_id = 'REF';
UPDATE cpf_service_instance
   SET active_yn = 'N', instance_status = 'DOWN', updated_by = 'ROLLBACK', updated_at = CURRENT_TIMESTAMP
 WHERE service_id = 'REF';
UPDATE cpf_service_routing_policy
   SET active_yn = 'N', updated_by = 'ROLLBACK', updated_at = CURRENT_TIMESTAMP
 WHERE service_id = 'REF';

UPDATE bat_center_cut_job
   SET use_yn = 'N', updated_by = 'ROLLBACK', updated_at = CURRENT_TIMESTAMP
 WHERE center_cut_job_id = 'CPF_REF_CENTER_CUT_SAMPLE_JOB';
UPDATE cpf_batch_job
   SET use_yn = 'N', updated_by = 'ROLLBACK', updated_at = CURRENT_TIMESTAMP
 WHERE job_id = 'CPF_REF_CENTER_CUT_SAMPLE_JOB';

USE exsDB;

UPDATE exs_control_policy
   SET enabled_yn = 'N', reason = 'REF 전환 rollback으로 송신 중지',
       updated_by = 'ROLLBACK', updated_at = CURRENT_TIMESTAMP(3)
 WHERE institution_code = 'CPF-REF' AND control_type IN ('ALL', 'SEND');

-- 물리 컬럼·테이블 제거는 백업 및 별도 승인 후 contract migration에서 수행합니다.
