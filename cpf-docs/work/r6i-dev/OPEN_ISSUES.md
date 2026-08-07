# OPEN ISSUES / External Verification

Source/Test/Gate 구현 후 현재 실행 환경 부재로 남은 검증만 기록한다.

1. Java 25 / Gradle 9.1 full build, test, publication — 현재 환경 Java 21이며 Gradle 9.1 runtime 없음.
2. Oracle/PostgreSQL/MariaDB live lifecycle — credential/runtime 미제공.
3. ADM/BZA authenticated Chromium/Firefox/WebKit — 실제 backend/auth-state runtime 미제공.
4. Multi-instance/process-kill/network/broker/DB-outage — 분산 runtime 미제공.
5. Performance/observability/security-negative/DR — target endpoints/corpus 미제공.
6. Artifact consumer REMOTE/OFFLINE/LOCAL real repositories and generator DB3 lifecycle — external repository/DB runtime 필요.
7. ADM full 332-operation source closure — container full clone DNS 차단으로 full frontend source projection을 만들 수 없어 changed-scope만 PASS. Release workflow에서는 full scope를 non-optional로 실행한다.
8. Result exact SHA — Overlay 적용/Commit 전이므로 `PENDING_USER_APPLY_COMMIT`. 적용 후 해당 SHA에서 Release Gate를 재실행해야 한다.
9. Codex independent review — 개발GPT 자체검수와 독립적으로 수행 필요.

위 항목은 구현 보류가 아니라 **verification_status=미검증**이다.
