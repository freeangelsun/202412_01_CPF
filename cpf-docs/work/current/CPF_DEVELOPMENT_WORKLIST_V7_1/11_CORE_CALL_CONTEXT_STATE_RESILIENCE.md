# CPF V7 상세 개발 Work Package — Core Call·Context·State·Lock·Resilience

- Canonical Requirement: 16개
- 실행 Work Package: 74개
- Canonical IDs: `CPF-CALL`, `CPF-CONTEXT`, `CPF-DEADLINE`, `CPF-ERROR`, `CPF-HEADER`, `CPF-HEALTH`, `CPF-IDEMP`, `CPF-LOCK`, `CPF-REGISTRY`, `CPF-RESILIENCE`, `CPF-ROLE`, `CPF-ROUTING`, `CPF-SCHED`, `CPF-STATE`, `CPF-TXID`, `CPF-VALID`
- 각 Work Package는 독립 개발 단위이며, 동일 호출 경로를 숫자 때문에 중간 절단하지 않는다.
- 구현 제안은 비강제이고 CPF 정본·표준·기존 Architecture가 우선한다.

## AI/Git 분할 Index

이 영역은 74개 Work Package를 2개 파일로 분할했다. 내용은 축소하지 않았으며 `WORK_ITEM_INDEX.csv`에서 Work Item별 정확한 Part를 찾는다.

| Part | Work Package | 범위 | 크기 |
|---|---:|---|---:|
| `11_CORE_CALL_CONTEXT_STATE_RESILIENCE_PART_01.md` | 52 | `CPF-WP-CPF-CALL-01-CONTRACT_OWNERSHIP` → `CPF-WP-CPF-IDEMP-04-FAILURE_RECOVERY` | 206,493B |
| `11_CORE_CALL_CONTEXT_STATE_RESILIENCE_PART_02.md` | 22 | `CPF-WP-CPF-STATE-01-CONTRACT_OWNERSHIP` → `CPF-WP-CPF-SCHED-04-FAILURE_RECOVERY` | 86,928B |
