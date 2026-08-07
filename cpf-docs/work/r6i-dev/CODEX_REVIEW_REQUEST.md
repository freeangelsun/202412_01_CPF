# Codex 독립 검수 요청

## 기준
- baseline master SHA: `64049044956924032360fa80be83b5e37c64f828`
- result SHA: Overlay 적용 후 새 SHA로 치환 필요
- QA source: `source-qa/QA_FINDINGS.csv`, `source-qa/QA_REQUIREMENT_STATUS.csv`, `source-qa/QA_MANAGER_HARDENING_REQUIREMENTS.csv`
- 개발 원장: `REQUIREMENT_STATUS.csv`

## 필수 독립검수
- 40 QA Finding을 exact ID로 하나씩 Source/Consumer/호출경로/실패복구 기준 재검증
- FDEV-001~025 및 HARDEN-001~012 독립 검산
- ADM/BZA full operation consumer closure; retired 410 waiver 2건의 Backend 근거 확인
- Approval 4D exact tuple, lease/fence/UNKNOWN, nonce single-use, persistent DQ CAS
- Oracle/PostgreSQL/MariaDB live lifecycle
- real authenticated browser role/error matrix
- process-kill/network/broker/DB-outage distributed tests
- generator/artifact consumer/supply-chain/DR/performance/observability/security negative
- generated diff zero, clean tree, exact result SHA Evidence hash

미실행 항목을 PASS로 승계하지 말고 실제 실행 결과를 기록한다.
