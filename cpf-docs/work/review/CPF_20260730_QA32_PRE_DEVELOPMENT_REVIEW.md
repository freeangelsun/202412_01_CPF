# CPF QA32 개발 전 검토 결과

- 기준 Repository: `freeangelsun/202412_01_CPF`
- 기준 Branch: `master`
- 개발 Base SHA: `d31bd127aa12bb9368933216642a5a9d25bd0bfd` (`20260731_02`)
- 작업 형태: 사용자 승인 전 Git Write가 없는 Root-relative Overlay
- README/Guide 제외 범위: 수정하지 않음

## 직접 검수 결론

기존 QA31 Overlay와 상태 문서는 최신 Git과 불일치했고, QA32 초안은 Dependency/Wrapper 중심이라 전체 완료본으로 사용할 수 없었다. 따라서 최신 Source를 기준으로 OSS Primary Consumer, Legacy 실행 경로, DB Migration, Frontend/BFF, Gateway, Batch, 운영·보안·공급망을 다시 대조했다.

최상위 방향 정정에 따라 Spring Batch의 기존 `ADOPT_SCOPED` 해석을 폐기하고 전체 Batch 실행의 `ADOPT_NOW` Primary Engine으로 확정했다. Scheduler는 Trigger만 소유하고 Center-Cut/Worker/File/Shell/API/Message 실행은 Spring Batch `JobOperator`·`JobRepository`·`ExecutionContext`·표준 Remote 확장으로 연결한다.

## 개발 묶음

1. Build/Starter/OSS Governance
2. Spring Batch Primary Engine 및 Legacy 제거
3. Spring Cloud Gateway MVC Data Plane
4. ADM/BZA Vue Router·Pinia·Query·Zod·Orval·BFF Session
5. Kafka·Resilience4j·Observability·Caffeine·OpenFeature·Secret Provider
6. Bootstrap·Deployment·Probe·Artifact·Archive·Attachment 보안
7. Oracle/PostgreSQL/MariaDB Migration/Rollback/Checksum
8. Supply-chain·Generator·Repository-wide Gate·Evidence

## 완료 판정 원칙

Source 구현과 정적 Gate는 현재 Overlay에서 검증한다. Java 25 전체 Build, 3개 DB, Kafka, Playwright, 다중 Process 장애·복구는 해당 Runtime이 있는 적용·Push 후 exact SHA에서만 완료로 판정하며, 미실행을 PASS로 기록하지 않는다.
