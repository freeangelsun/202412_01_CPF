# Runtime 미검증 범위

이번 작업 환경에서는 문서·DOCX·PDF·링크·Bookmark·페이지 Render·정적 Source 대조를 수행했다. 다음은 직접 실행하지 않았으므로 성공으로 기록하지 않는다.

- Java/Gradle 전체 clean build 및 publication gate
- npm ci / npm verify / Playwright Browser E2E
- MariaDB·PostgreSQL·Oracle DB3 Live Lifecycle
- Kafka·다중 Process·Process Kill·Lease/Fencing 장애 주입
- 실제 Rolling·Blue-Green·Canary 배포
- Backup Restore·DR Failover/Failback 실제 환경 수행

Repository의 `run-r6-release-gates.ps1`는 Browser·DB3 Live·Multiprocess를 Switch로 분리한다. 실제 고객 환경에서 해당 Switch를 실행하지 않은 결과는 `미검증` 상태를 유지한다.
