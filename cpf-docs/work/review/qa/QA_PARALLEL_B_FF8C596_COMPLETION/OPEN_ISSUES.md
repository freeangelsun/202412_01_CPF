# Open Issues

1. **P0 — Canonical Parts 미확보:** full Current ledgers와 QA-B Requirement별 linked Scenario mapping을 현재 실행환경에서 생성하지 못했습니다.
2. **P0 — QA-B 개별 검수 미완료:** 9,962건은 배정/Query integrity만 검증됐으며 모두 `미검수`입니다.
3. **P0 — BAT UNKNOWN Runtime:** 실제 JDBC transaction failure, HTTP error mapper, reconcile 운영 경로가 미검증입니다.
4. **P0 — DB Runtime:** MariaDB retry lifecycle 및 PostgreSQL/Oracle install/upgrade/rollback/drift가 미검증입니다.
5. **P0 — Product Gate:** Java 25, 전체 Gradle, Browser E2E, multi-instance, 71,321행 completion gate가 미실행입니다.
6. **Cross-review:** 개발GPT, Codex, 독립 QA 검수 상태는 모두 열려 있습니다.
