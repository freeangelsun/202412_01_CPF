# CPF Development 22 External Validation Blockers

개발 환경에서 Source/Test/Verifier/Script 구현 및 정적·계약 검증은 완료했지만 다음 항목은 실제 외부 Runtime 없이는 PASS로 기록하지 않는다.

1. QA4 `CPF-QA-230143-002`: Fresh Public Workspace에서 서로 다른 Domain DB binding(PostgreSQL/Oracle/MariaDB)을 실제 provisioning → migration → seed → runtime health로 검증.
2. STD `STD-QA-011`: Java25 root Gradle full build/test, live DB3 lifecycle, Multi-WAS/process-kill/recovery, Browser E2E, Public Binary live resolution.

이 항목은 `BLOCKED_EXTERNAL`이며 `CLOSED`로 계산하지 않는다.
