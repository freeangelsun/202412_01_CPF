# R19 Runtime Unverified

다음은 문서 제작/정적 Source 대조와 별개이며 이번 작업에서 실행하지 않았으므로 `미검증`이다.

- R6J QA A/B independent runtime verification 및 최종 통합 판정
- 전체 Java/Gradle build/publication 및 공급 Artifact 설치 검증
- ADM/BZA Playwright 전체 Browser E2E와 실제 권한 계정 Matrix
- MariaDB/PostgreSQL/Oracle 신규 설치·Migration·Upgrade·Rollback·Drift 실DB 검증
- Kafka 다중 인스턴스, process-kill, lease/fencing, spool/replay 장애 주입
- Gateway 다중 Instance ACK/NACK/Partial Apply/LKG/Rollback Runtime
- Rolling/Blue-Green/Canary 실제 배포 및 Backup/Restore/DR failover/failback
- 사용자 Local Working Tree의 `git status/diff/untracked` 상태

문서에는 위 항목을 성공으로 기록하지 않는다.
