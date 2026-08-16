-- 로그 정책 런타임 적용 기준을 보강하고 온라인 거래 대상 유형을 표준 명칭으로 정리합니다.
USE cpfDB;

UPDATE cpf_log_policy
SET target_type = 'ONLINE_TRANSACTION',
    updated_by = 'SYSTEM',
    updated_at = CURRENT_TIMESTAMP
WHERE policy_key = 'ONLINE_DEFAULT'
  AND target_type = 'TRANSACTION';

UPDATE cpf_log_policy_override
SET target_type = 'ONLINE_TRANSACTION',
    updated_by = 'SYSTEM',
    updated_at = CURRENT_TIMESTAMP
WHERE target_type = 'TRANSACTION';
