# CPF 20260729_04 QA 200 시나리오 안전 체크포인트 보고서

## 1. 체크포인트 성격

이 산출물은 세션 연속성 보호를 위한 **중간 Root Overlay**다. 기준 Commit은 `b8941577b99535ff3e64a4fad99b74bafa544227`이며 사용자 승인 없이 Commit·Push·Branch를 만들지 않았다.

- QA Master Ledger: Requirement 2,328개 + Scenario 387개
- 이번 체크포인트: Scenario **200개 검토·Owner/Source/Gate/환경/남은 개발 연결 완료**
- 전체 Scenario 387개: Validation Backlog에 모두 등록
- 최종 목표: Canonical Requirement 162개 전체 개발 및 최신 SHA 검증

`검토 완료`는 시나리오를 분석하고 개발·검증 경로를 연결했다는 의미다. Java25 전체 Build, 3개 DB Lifecycle, Browser, Redis/Kafka, 다중 인스턴스를 실행했다는 뜻이 아니다.

## 2. 이번 Overlay의 주요 개발 변경

- BZA 조직·메뉴 재귀 Tree, 고아/순환 탐지, Effective Permission UI, Action Manifest와 Backend Filter
- Cache Public API/SPI, Local/Redis Provider, Cache Aside, Single-flight, Durable Invalidation, ADM Cache 운영 API/UI
- CSV/XLSX Streaming 계약과 Adapter, Formula/Macro/Zip Bomb/행·셀 상한
- ADM 비동기 File Job, Template, Artifact, 행별 결과, Retry/Cancel/Rollback, Notification Rule Import Consumer
- Local Web/Batch Runtime Profile과 Safety Guard
- Runtime Control 기존 Durable 원장 보강 파일과 ADM UI
- Oracle/PostgreSQL/MariaDB V69/V70 및 R69/R70
- CI/Evidence/Architecture/Hygiene Gate

## 3. 이번 체크포인트에서 실제 확인한 사항

- 프로젝트 Root 상대경로 Overlay 구성
- `createAdmState.ts` 잘못된 `app` 경로 제거 및 `state` 경로 복구
- 기존 `CmnRuntimeControlAutoConfiguration`을 유지하면서 Cache/Tabular AutoConfiguration 추가
- Canonical `seed-model.json` 포함
- QA 200개 ID 중복 0, 전체 387개 Backlog 누락 0
- JSON/CSV parse, Java package/path, 3 Vendor Migration 파일 parity, Hygiene, 직접 Client/내부 import 경계 검사

## 4. 숨기지 않은 미완료 개발

- Runtime Control Public API/ADM Controller에 `Map<String,Object>` 계약이 잔존한다.
- ADM Auth/Notification 일부 공개 응답과 Service에 Raw Map이 잔존한다.
- Generator Golden Template 실제 Source 변경은 이번 Overlay에 포함되지 않았다.
- V69/V70을 Canonical Install/Verify/Checksum 생성 흐름에 실제 편입하고 재생성해야 한다.
- 전체 Gradle/Frontend Compile 및 실제 Runtime 실행이 필요하다.

따라서 이 ZIP은 **최종 완료본이 아니라 안전 체크포인트**이며, 다음 ChatGPT 개발 세션이 위 항목을 완료해야 한다.
