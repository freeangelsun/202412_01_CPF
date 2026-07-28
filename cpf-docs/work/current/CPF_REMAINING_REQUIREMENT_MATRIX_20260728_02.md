# CPF Remaining Requirement Matrix — 20260728_02

## 판정 기준

- `완료`: 이번 Overlay에서 구현 및 정적/독립 행동 검증 완료
- `미검증`: 구현은 존재하나 실제 통합 환경 실행 Evidence가 없음

| 영역 | 상태 | 이번 보완 | 남은 완료 조건 |
|---|---|---|---|
| Runtime State 계약 | 완료 | Java Enum·Catalog·생성자 검증·DB constraint parity | 전체 build 회귀 확인 |
| ADM Runtime Change Center | 완료 | 변경/preview/조회/cancel/rollback/audit/group/member UI·Route·권한 SQL | Browser·권한별 E2E는 미검증 |
| Gateway 대용량 전송 | 완료 | 메모리 임계치 이후 secure spool, replayable retry body, response stream, Range/conditional/query 전달 | 실제 대용량·multipart·timeout E2E는 미검증 |
| Gateway 인증·인가 | 완료 | Principal 생성 결함 수정, exact authority 검사, 회귀 Test | 실제 인증 Provider 연동은 미검증 |
| Service Call transport 분류 | 완료 | retryable HTTP·timeout·unknown-result metadata/exception 계약 | target-down/failover Runtime은 미검증 |
| Batch Runtime Consumer | 완료 | Scheduler·Worker·Business Calendar·Center-Cut·Host-Agent 실제 실행 객체 연결, 가짜 `BATCH_PARTITION` 제거, Agent 로그수집 정책 적용 | 분산 실행·재시작·부분 실패 E2E는 미검증 |
| External Institution Runtime | 완료 | endpoint/layout/timeout 실제 registry 적용 및 행동 검증 | 실제 대외 호출 E2E는 미검증 |
| Generator 계약 | 완료 | metadata schema·central template contract·create-domain 사전검증 보강 | ACC/MBR/EXS 생성·빌드 실행은 미검증 |
| 3개 DB 권한/메뉴 | 완료 | Oracle/PostgreSQL/MariaDB migration·rollback·checksum parity | 실DB install/upgrade/rollback은 미검증 |
| 전체 QA 재판정 | 미검증 | 기존 Inventory/Scenario 정본 유지 | 최신 적용 Commit 기준 전수 재실행 필요 |
| 전체 Java/Frontend Build | 미검증 | 독립 javac·TypeScript 구문·정적 Gate PASS | Java 25/Gradle 9.1 전체 build/test 필요 |
