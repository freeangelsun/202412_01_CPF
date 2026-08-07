# Open Issues / Remaining Verification Conditions

Source·Test·Config·SQL·Frontend·Script로 구현 가능한 29개 Finding 범위는 처리했으나 다음 외부 검증 조건은 남아 있다. 따라서 QA 최종 통과를 선언하지 않는다.

1. Overlay 적용 후 생성되는 exact result commit SHA에서 Java25/Gradle9.1 full build, test, publication.
2. Node 22.18+ `npm ci`, `npm run verify`, Playwright E2E/accessibility/responsive.
3. Oracle/PostgreSQL/MariaDB live install/migration/seed/upgrade/rollback/drift/query 및 unique conflict/CAS.
4. Broker, multi-instance, split-WAS, process-kill, UNKNOWN/reconcile runtime.
5. Complete checkout에서 QA38, QA39, REV004, catalog/BOM/generator parity.
6. Codex 독립 검수 완료 Evidence와 result SHA 결속.
7. 사용자 적용 후 clean Working Tree/Commit provenance.

`QA-R5I-029`는 요청서 작성까지 완료했으나 독립 검수 자체가 없어 개발판정 `미완료`, 검증판정 `미검증`이다.
