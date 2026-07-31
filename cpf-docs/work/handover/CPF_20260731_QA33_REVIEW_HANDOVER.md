# CPF QA33 검수 인수인계

- Package `CPF-20260731-QA33-INDEPENDENT-SOURCE-RUNTIME-CLOSURE`
- Review SHA `1536a0d59004ebade7dcb29383cbe2e758547f8e`
- 작성 `2026-07-31T11:12:55+09:00`

## 핵심
QA32는 `Source 완료 / Runtime 미검증` 상태가 아니다. Gradle·npm·TypeScript blocker와 Architecture·Security·Batch·Gateway semantic 결함이 존재한다.

## 첫 확인
1. latest master SHA
2. settings.gradle Included Build target
3. ADM/BZA/Gateway Project path
4. package-lock/generated client
5. exact-SHA Evidence와 .gitignore
6. Core/Common 선택 Runtime dependency
7. BFF session/token leak
8. Batch idempotency/fencing/unknown
9. Kafka manager reply correlation
10. Gateway async completion/trusted header/retry
11. 3DB/Browser/Kafka/Agent/Supply-chain runtime

## 보호할 방향
Spring Batch, SCG MVC, Pinia/Router/Query, server session, Kafka, Starter 분리 방향은 유지하되 실제 Consumer와 failure/recovery를 완성한다. Archive extract 보호와 Artifact 기본 signature/size 구조를 회귀시키지 않는다.

## 금지
Marker용 빈 파일, compile 실패를 환경 미비로 분류, Runtime 미검증을 Source 완료로 처리, 사용자 승인 없는 Git write.
