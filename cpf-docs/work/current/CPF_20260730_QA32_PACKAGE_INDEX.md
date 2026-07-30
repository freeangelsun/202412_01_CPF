# CPF QA32 OSS-first 전면 교체·결함 수정 개발 요청 패키지

- Package ID: `CPF-20260730-QA32-OSS-FIRST-FULL-REMEDIATION`
- Repository: `freeangelsun/202412_01_CPF`
- Source review baseline: `3249581e8f01dcb546bc6601c31aee525f564d21` (`20260730_11`)
- Prior immutable baseline: QA31
- 생성 시각: `2026-07-30 23:50 Asia/Seoul`
- 수정 Revision: `2` (`2026-07-31 00:21 Asia/Seoul`) — Root 신규 지침 파일 제거 및 경로 명시 보강
- 요청 원칙: **OSS 기반 전면 교체를 최상위 개발축으로 실행하고, 기존 44개 공통 결함 요건과 QA31 미해결 23건을 모두 재검증한다.**
- Commit·Push·Branch·Tag·PR: 사용자가 직접 수행. 개발 AI는 별도 명시 승인 전 수행 금지.

## 패키지 규모

- Requirement: **62건** — P0 16, P1 41, P2 5
- Defect/Gap: **60건** — QA31 Carry-over 23 + QA32 추가 37
- Mandatory Scenario: **202건**
- OSS Migration Decision: **23건**
- Request package files: **23개** (Root-relative overlay)

## 정본 읽기 순서

1. `cpf-docs/work/current/CPF_20260730_QA32_GPT_DEVELOPMENT_INSTRUCTION.md`
2. `cpf-docs/work/current/CPF_20260730_QA32_PACKAGE_INDEX.md`
3. `cpf-docs/work/current/CPF_20260730_QA32_DEVELOPMENT_REMEDIATION_REQUEST.md`
4. `cpf-docs/architecture/ADR_OSS_FIRST_PLATFORM_DIRECTION.md`
5. `cpf-docs/architecture/CPF_BUILD_VS_BUY_MATRIX.md`
6. `cpf-docs/quality/CPF_20260730_QA32_OSS_MIGRATION_MATRIX.csv`
7. `cpf-docs/quality/CPF_20260730_QA32_REQUIREMENT_MATRIX.csv`
8. `cpf-docs/quality/CPF_20260730_QA32_DEFECT_REGISTER.csv`
9. `cpf-docs/quality/CPF_20260730_QA32_SCENARIO_MATRIX.csv`
10. `cpf-docs/governance/CPF_NO_PARTIAL_IMPLEMENTATION_COMPLETION_STANDARD.md`
11. `cpf-docs/governance/CPF_OSS_LICENSE_AND_SUPPLY_CHAIN_STANDARD.md`
12. `cpf-docs/quality/CPF_20260730_QA32_EVIDENCE_SCHEMA.md`
13. `cpf-docs/work/handover/CPF_20260730_QA32_DEVELOPMENT_HANDOVER.md`

## 개발 우선순위

### Phase 0 — 시작 SHA·원장·무결성 정리

- 최신 `master`와 개발 시작 exact SHA 기록
- 이 패키지를 Push한 Commit이 Source baseline 뒤에 문서만 추가했는지 확인
- QA31 Active 문서의 이전 SHA·`WORKING_TREE_NOT_COMMITTED` 상태를 그대로 완료 증적으로 사용 금지
- `verify-cpf-qa32-request-integrity.ps1` 통과
- Pre-development Review 작성

### Phase 1 — OSS-first Architecture Foundation

- `OSS-GOV-001`, `OSS-ARCH-001`
- Core/Common API·SPI·Autoconfigure·기능별 Starter 분리
- Dependency/BOM/Lock/License/SBOM 정본화
- 이후 OSS 전환 작업이 이 경계를 사용하도록 선행

### Phase 2 — 사용자 확정 OSS 전환

- Element Plus + TanStack Table
- Vue Router + Pinia + TanStack Vue Query + Zod
- ADM/BZA Orval
- ADM/BZA BFF + Server-side Session
- Spring Cloud Gateway Server Web MVC + Embedded Tomcat 단일 `cpf-gateway.jar`
- Kafka + In-memory Unit Adapter + Testcontainers Kafka
- CycloneDX + ORT + Final Artifact Syft + Grype

### Phase 3 — 나머지 승인된 OSS 방향

- Spring Cloud CircuitBreaker + Resilience4j
- Spring Batch 제한 범위
- db-scheduler 기본·Quartz 고급 선택 Adapter
- Flyway OSS Core
- Micrometer Observation + OpenTelemetry OTLP
- Caffeine + 선택형 Valkey Provider
- OpenFeature + CPF Provider
- SecretProvider SPI
- Playwright/접근성
- Flowable/WebFlux는 현재 구현 범위에서 제외 또는 ADR 조건부

### Phase 4 — 기존 44개 공통 결함 요건

Build·Artifact·Generator·Provenance·Source Security·File·HTTP·SQL·Resource·Deployment·Probe 등 전체 원장을 저장소 전역에 적용한다.

### Phase 5 — 실행 검증과 Legacy 제거

- Java 25 전체 Gradle
- ADM/BZA npm ci/lint/typecheck/unit/build/Playwright
- MariaDB/PostgreSQL/Oracle lifecycle
- Gateway scale-out/fault/load
- Kafka Testcontainers와 실제 환경
- Batch/Scheduler/Worker/Agent
- Multi-instance, process kill, response loss, disk full, network block, security negative corpus
- 모든 legacy consumer/import/dependency/artifact 제거

### Phase 6 — 최종 결과 ZIP

개발 완료 후 `cpf-tools/scripts/package-cpf-qa32-development-result.ps1` 또는 동일 규칙으로 **프로젝트 Root 상대경로 ZIP**을 생성하고 사용자에게 다운로드 링크를 제공한다.

## 완료 선언 금지

- Dependency·Interface·DTO·Adapter·화면 파일만 추가
- OSS와 Legacy가 동시에 Primary
- 일부 화면/모듈만 이관
- Compile 또는 Static Gate만 통과
- 필수 환경이 없어 실행하지 않은 검증을 PASS로 기록
- 최신 exact SHA가 아닌 Evidence
- `부분 구현`, `미검증`, `재확인 필요`를 완료 수에 포함
