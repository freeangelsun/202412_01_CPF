# CPF Codex Continuity State

최종 갱신: 2026-08-14 17:35:00 +09:00

이 문서는 PC·대화 세션과 무관한 현재 작업 인수인계 상태만 기록한다. 상세 실행 원장은 `cpf-docs/work/current/CODEX_FINAL_VALIDATION_RESULT.md`를 따른다.

## 기준

- Branch: `master`
- 기준 HEAD: `0566f41d18a61d657304ba41c9fb5210e7bcc3ef`
- `origin/master`: `0566f41d18a61d657304ba41c9fb5210e7bcc3ef`
- 작업 환경: `BOOK-JDJDKA9AHA`, Windows, PowerShell 7.6.4, OpenJDK 25.0.3, Docker Engine 29.6.2
- Worktree: 대규모 승인된 WIP. Commit/Push 전이며 reset/restore/stash/clean 금지

## 현재 작업 단계

- 상태: `부분 구현`
- 단계: Final one-pass Source/Build 보완 후 Generated Domain DB3, Platform DB3, Runtime, Browser, Root Gate를 실제 실행하는 단계
- 현재 단일 Gradle 실행 소유자: EDU/Gateway closure 작업. 다른 Gradle/clean 동시 실행 금지

## 완료한 작업

- `완료`: Baseline HEAD/origin/master 확인 및 dirty WIP 보존
- `완료`: 중앙 Vendor Pack, Generated Domain canonical template/Data-owned runtime dialect 구조 정비
- `완료`: MBR/EXS canonical Generator parity와 Java 정적/Javac 검증. 최신 BatchStepResult 변경 후 재생성은 아직 필요
- `완료`: BAT runtime role canonicalization과 V116/R116 MariaDB/PostgreSQL 격리 lifecycle 검증
- `완료`: Redis 8.8.1/Valkey 9.1.1 실제 Docker Provider 기능·장애·복구 검증
- `완료`: `cpf-testkit` compile/test 2 suites, 5 tests
- `완료`: BAT runtime 12 suites, 36 tests 및 Batch/Web/Secure Profile focused Gradle 검증
- `완료`: Platform Seed의 Generated `REF` 종속을 `EDU` 소유 `MEDU010001/EEDU010001`로 canonical-first 보정하고 3-Vendor Seed 재생성
- `완료`: Vendor source/lifecycle Seed mirror 동기화 회귀 테스트 15건 및 seed dialect 48 files 검증

## 진행 중인 작업

- `부분 구현`: `cpf-education`/`cpf-gateway`/S3 Boot 4 compile/test closure
- `부분 구현`: Generated Domain federation/jobpack stale contract 제거 및 external CUSTOMER_BUSINESS_DB installer closure
- `부분 구현`: Canonical Seed 보정의 공식 Profile Gate 재실행. 현재 stale Generated principal gate를 보완 중

## 아직 시작하지 않은 작업

- `미검증`: 최신 Generator로 MBR/EXS 재생성 후 MariaDB/PostgreSQL/Oracle composite test
- `미검증`: 격리 MariaDB/PostgreSQL/Oracle Platform Fresh Install→Product Seed→Verify, Upgrade→Rollback→Reapply
- `미검증`: Local integrated/distributed Runtime, Kafka/Redis/DB 연계, Browser journey
- `미검증`: Frontend exact npm 10.9.2 build/test
- `미검증`: 안정화된 최종 SHA의 Preflight, Root quality/build/publication/qa34, hygiene, Commit/Push

## 변경 중인 주요 영역

- `cpf-tools/db/canonical`, `cpf-tools/db/vendor`, `cpf-tools/db/generated`, DB lifecycle/gates/tests
- `cpf-tools/generator`, `cpf-member`, `cpf-external`
- `cpf-starters`, `cpf-batch`, `cpf-gateway`, `cpf-education`, `cpf-tools/testing`
- Runtime/Deployment role metadata와 Guides/Evidence/Result ledger

## 실제 검증 상태

- `완료`: Redis/Valkey Docker live fixture; evidence는 현재 WIP HEAD 전용이며 최종 Commit SHA에서 재실행 필요
- `완료`: BAT runtime/profile focused Gradle
- `완료`: DB canonical seed render, source/lifecycle mirror tests 15/15, dialect lint 48/48
- `부분 구현`: D-025 DB3 official harness 정적/계획 검증. Live 3-Vendor lifecycle 미실행
- `미검증`: 최종 Root 전체 build/test/runtime/browser

## DB/Runtime 현재 상태

- 제품 로컬 DB는 작업 시작 조건상 초기 상태이며 기존 사용자 DB를 파괴하지 않는다.
- D-009 전용 ephemeral MariaDB/PostgreSQL containers는 검증 후 제거됨.
- Redis/Valkey 전용 ephemeral containers/volumes/network는 검증 후 제거됨.
- Oracle은 정적 계약만 검증된 항목이 있으며 실제 Oracle lifecycle은 `미검증`.
- CPF local application runtime은 현재 정지 상태이며 최종 compile closure 후 기동 예정.

## Blocker와 미검증

- Source blocker는 발견 즉시 수정 중이며 후속으로 넘기지 않는다.
- 환경 blocker로 완료 처리한 항목 없음.
- 현재 Profile DB Gate는 stale Generated principal/provision consumer 보완 완료 전까지 `부분 구현`.
- 최종 SHA가 바뀌므로 과거 SHA/dirty fingerprint의 PASS는 최종 PASS로 승계하지 않는다.

## 다음 정확한 작업 순서

1. EDU/Gateway Gradle closure를 직렬 완료한다.
2. Generated DB installer의 stale provision/principal consumer를 canonical CUSTOMER_BUSINESS_DB 계약으로 정리하고 Profile Gate를 통과시킨다.
3. 공식 Generator로 MBR/EXS를 재생성하고 3-Vendor composite를 순서대로 통과시킨다.
4. 격리 DB3 Fresh/Upgrade/Rollback/Reapply를 공식 harness로 실제 실행한다.
5. Runtime·Browser·Frontend를 실제 검증한다.
6. 최종 Root Gate/Hygiene 실행 후 문서·Evidence를 최종 SHA로 갱신한다.
7. 실패 0 및 clean hygiene 확인 후에만 Commit/Push한다.

## 다시 수행하면 안 되는 작업 / 확정 사항

- Historical migration을 수정하거나 checksum을 재작성하지 않는다. 변경은 forward migration으로만 수행한다.
- Vendor 선택을 위해 Java 업무 Source 또는 tracked resource를 덮어쓰지 않는다.
- MBR/EXS 등 SystemCode 고정 switch를 Generator/DB Tool에 추가하지 않는다.
- Generated Domain이 물리 DB·principal을 생성하지 않는다. 기존 `CUSTOMER_BUSINESS_DB`에 prefix 기반으로 설치한다.
- 승인된 Generated module-local Mapper XML 2건은 중앙 selected-Vendor overlay로 대체되었으므로 복구하지 않는다.
- Federation canonicalization에서 소비자 0을 증명하고 Delete/Garbage 원장에 등록한 stale contract 3건도 복구하지 않는다.
- Gradle을 동시에 실행하거나 shared build output에 병렬 clean을 수행하지 않는다.

