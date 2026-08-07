# R20 현재 제품 상태

기준 master: `cd5baccb02245a980e5998aa0dc9bac579fc019f` (`07_04`).

R6J 중앙 QA의 제품 판정은 `미통과 — RELEASE_BLOCKED`다. 통합 Finding은 56개이며, 34개는 현재 확인된 직접 Source/Contract/Gate 재개발 항목이다. `07_04` 정책에 따라 34개를 전체 작업 범위로 보지 않는다.

현재 프로젝트 완료 후보 범위는 Requirement 93/93, Finding 56/56, 최상위 Requirement 전체, Runtime/GA, 개발·검수 중 새로 발견되는 결함까지 포함한다.

주요 미결 축:
- exact-SHA Release workflow와 Evidence provenance
- transactionId multi-source timeline과 DB3 transaction logging
- FileLog durable spool/retry/dedup/loss recovery
- Approval durable UNKNOWN 및 Owner observation reconcile
- ADM/BZA high-risk action permission과 Browser role matrix
- OpenAPI/Generated Client 422/error taxonomy parity
- EDU Product/Extension 경계, authenticated context, Process isolation
- Frontend/Observability false-green qualification 제거
- Java25/Gradle9.1, DB3 live, authenticated browser, distributed/process-kill, security, observability, performance, DR, artifact/supply-chain, Codex target runtime

후속 Product Source가 변경되면 공식 문서는 그 successor exact SHA에 다시 맞춰야 한다.
