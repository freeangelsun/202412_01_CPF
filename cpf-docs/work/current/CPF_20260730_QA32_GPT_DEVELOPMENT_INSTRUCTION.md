# CPF QA32 GPT 개발 지침

- **이 지침 파일 경로:** `cpf-docs/work/current/CPF_20260730_QA32_GPT_DEVELOPMENT_INSTRUCTION.md`
- **프로젝트 Root:** 이 파일에서 `../../../..` 상위의 Git Repository Root
- **Repository:** `freeangelsun/202412_01_CPF`
- **Source 검토 기준 SHA:** `3249581e8f01dcb546bc6601c31aee525f564d21` (`20260730_11`)

## 1. 저장 위치 원칙

- 신규 요청서·검토서·완료 보고서·Evidence·Manifest는 저장소 루트에 만들지 않는다.
- 신규 문서는 `cpf-docs/**`, 실행 스크립트는 `cpf-tools/scripts/**`에 둔다.
- `README*`, `cpf-docs/guides/**`, `cpf-tools/README.md`, `cpf-docs/assets/readme/**`, `cpf-docs/work/overlay/20260730-readme-guides/**`는 별도 AI 관리 범위이므로 이 개발 요청에서 수정하지 않는다.
- 기존 Root Source 파일인 `settings.gradle`, `build.gradle`, `gradle.properties` 등은 실제 개발상 필요할 때 수정할 수 있지만, QA 설명·Manifest·GPT 지침 같은 신규 문서를 Root에 추가하면 안 된다.

## 2. 개발 시작 전 반드시 읽을 정본 경로

아래 파일을 **표시된 순서대로 실제로 열어 읽은 후** 개발을 시작한다.

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
13. `cpf-docs/work/review/CPF_20260730_QA32_SOURCE_REVIEW_FINDINGS.md`
14. `cpf-docs/work/review/source/CPF_20260730_OTHER_AI_AUDIT_REVIEW_AND_QA_REQUIREMENTS.md`
15. `cpf-docs/work/evidence/CPF_20260730_QA32_BASELINE_SNAPSHOT.json`
16. `cpf-docs/work/handover/CPF_20260730_QA32_DEVELOPMENT_HANDOVER.md`
17. `cpf-docs/work/current/CPF_20260730_QA32_CODEX_BATCH_REVIEW_REQUEST.md`

요청 패키지 무결성 검증 스크립트:

- `cpf-tools/scripts/verify-cpf-qa32-request-integrity.ps1`

개발 결과 ZIP 생성 스크립트:

- `cpf-tools/scripts/package-cpf-qa32-development-result.ps1`

## 3. 패키지 전체 파일 경로와 용도

| 경로 | 용도 |
|---|---|
| `cpf-docs/work/current/CPF_20260730_QA32_GPT_DEVELOPMENT_INSTRUCTION.md` | GPT 개발 실행 지침 |
| `cpf-docs/work/current/CPF_20260730_QA32_PACKAGE_INDEX.md` | 패키지 규모·읽기 순서·개발 Phase |
| `cpf-docs/work/current/CPF_20260730_QA32_DEVELOPMENT_REMEDIATION_REQUEST.md` | 상세 개발·수정 본문 |
| `cpf-docs/work/current/CPF_20260730_QA32_CODEX_BATCH_REVIEW_REQUEST.md` | 완료 후 독립 검토 요청 |
| `cpf-docs/architecture/ADR_OSS_FIRST_PLATFORM_DIRECTION.md` | 확정 OSS 아키텍처 결정 |
| `cpf-docs/architecture/CPF_BUILD_VS_BUY_MATRIX.md` | 자체 구현·OSS 역할 경계 |
| `cpf-docs/governance/CPF_NO_PARTIAL_IMPLEMENTATION_COMPLETION_STANDARD.md` | 부분 구현 금지·완료 판정 기준 |
| `cpf-docs/governance/CPF_OSS_LICENSE_AND_SUPPLY_CHAIN_STANDARD.md` | OSS 라이선스·공급망 정책 |
| `cpf-docs/quality/CPF_20260730_QA32_REQUIREMENT_MATRIX.csv` | Requirement 62건 원장 |
| `cpf-docs/quality/CPF_20260730_QA32_DEFECT_REGISTER.csv` | Defect/Gap 60건 원장 |
| `cpf-docs/quality/CPF_20260730_QA32_SCENARIO_MATRIX.csv` | Mandatory Scenario 202건 원장 |
| `cpf-docs/quality/CPF_20260730_QA32_OSS_MIGRATION_MATRIX.csv` | OSS Migration 23건 원장 |
| `cpf-docs/quality/CPF_20260730_QA32_EVIDENCE_SCHEMA.md` | Evidence 필수 Schema |
| `cpf-docs/quality/CPF_20260730_QA32_REQUEST_INTEGRITY.json` | 요청 패키지 파일 Hash Manifest |
| `cpf-docs/quality/CPF_20260730_QA32_PACKAGE_FILES.sha256` | 요청 파일 SHA-256 목록 |
| `cpf-docs/evidence/templates/CPF_QA32_COMPLETION_EVIDENCE_TEMPLATE.json` | 전체 완료 Evidence Template |
| `cpf-docs/evidence/templates/CPF_QA32_REQUIREMENT_EVIDENCE_TEMPLATE.json` | Requirement별 Evidence Template |
| `cpf-docs/work/evidence/CPF_20260730_QA32_BASELINE_SNAPSHOT.json` | 최신 Source 검토 Baseline |
| `cpf-docs/work/review/CPF_20260730_QA32_SOURCE_REVIEW_FINDINGS.md` | 최신 Git Source 검토 결과 |
| `cpf-docs/work/review/source/CPF_20260730_OTHER_AI_AUDIT_REVIEW_AND_QA_REQUIREMENTS.md` | 타 AI 감사 결과 검토 원문 |
| `cpf-docs/work/handover/CPF_20260730_QA32_DEVELOPMENT_HANDOVER.md` | 개발 인수인계 기준 |
| `cpf-tools/scripts/verify-cpf-qa32-request-integrity.ps1` | 요청 원장·Hash·ID 무결성 Gate |
| `cpf-tools/scripts/package-cpf-qa32-development-result.ps1` | 완료 결과 Root-relative ZIP 생성 |

## 4. 개발 시작 규칙

1. 실제 `HEAD`, branch/ref, remote URL, clean 여부를 기록한다.
2. 이 요청 패키지가 Push된 Commit을 개발 시작 SHA로 사용하되, Source 변경이 같이 섞였는지 먼저 확인한다.
3. QA31과 과거 문서의 `완료`, `PASS`, `WORKTREE-OVERLAY`, 이전 SHA 기록을 완료 증적으로 승계하지 않는다.
4. 최신 exact SHA에서 Requirement 62건, Defect 60건, Scenario 202건, OSS Migration 23건을 다시 판정한다.
5. 사용자의 명시 승인 전 Commit, Push, Branch, Tag, PR, Release를 생성하지 않는다.

시작 직후 실행할 요청 무결성 Gate:

```powershell
pwsh -NoProfile -File cpf-tools/scripts/verify-cpf-qa32-request-integrity.ps1 -Root .
```

## 5. 최상위 개발 원칙

1. **OSS-first 전면 교체를 최상위 작업으로 처리한다.**
2. Dependency나 Interface만 추가하지 말고 모든 실제 Consumer를 Primary Path로 이관한다.
3. OSS와 기존 자체 구현을 Release 시점에 동시에 Primary로 남기지 않는다.
4. 기존 기능·데이터·권한·승인·감사·실패·복구·성능을 동일하거나 더 높은 수준으로 유지한다.
5. 발견된 특정 파일만 고치지 말고 동일·변형 패턴을 저장소 전체 Source·Script·Generator·Generated Source·Runtime·SQL·Test에 적용한다.
6. 실행하지 않은 검증은 `PASS`나 `완료`로 기록하지 않는다.
7. `부분 구현`, `미검증`, `재확인 필요`, `실패`가 한 건이라도 남으면 전체 완료를 선언하지 않는다.

## 6. 확정 OSS 방향

- UI: Element Plus + TanStack Table
- Frontend State/API: Vue Router + Pinia + TanStack Vue Query + Zod
- ADM/BZA API Client: Orval
- Browser Security: ADM/BZA BFF + Spring Security + Spring Session JDBC
- Gateway: Spring Cloud Gateway Server Web MVC + Embedded Tomcat, 단일 `cpf-gateway.jar`
- 제외: `cpf-gateway-webflux.jar`, WebFlux Gateway, Envoy
- Messaging: Kafka + In-memory Unit Test Adapter + Testcontainers Kafka Integration Test
- Resilience: Spring Cloud CircuitBreaker + Resilience4j
- DB Migration: Flyway OSS Core 허용 Artifact만 사용
- Observability: Micrometer Observation + OpenTelemetry OTLP
- Local Cache: Caffeine; Valkey는 선택 Adapter
- Scheduler: db-scheduler 기본, Quartz는 고급 요구 ADR 통과 시만
- Supply Chain: CycloneDX + ORT + Final Artifact Syft + Grype
- Browser E2E: Playwright
- 현재 제외: PrimeVue 최신, Artemis/RabbitMQ Primary, Redis/Vault Server 기본 번들, Flyway 유료 기능, 근거 없는 Flowable 도입

## 7. 완료 금지 사례

- Dependency만 추가하고 실제 Consumer를 이관하지 않음
- DTO·Port·Adapter Skeleton만 추가
- 화면·메뉴만 존재하고 Backend·DB·Runtime이 연결되지 않음
- Static Scanner·Compile·Mock Test만 통과
- In-memory Test만 통과하고 실제 Kafka Testcontainers를 실행하지 않음
- Java 25 전체 Gradle, npm verify, Browser E2E, DB Lifecycle, Runtime Failure/Recovery 중 필요한 검증을 실행하지 않음
- Legacy Import·Bean·Route·Dependency·Artifact가 남음
- 오류를 catch-all로 숨기거나 결과를 근거 없이 `UNKNOWN`으로 축약
- 최신 exact SHA가 아닌 Evidence
- Result Matrix 상태를 근거 없이 일괄 `완료`로 변경

## 8. 개발 후 반드시 생성할 결과 파일 경로

아래 파일은 **저장소 루트가 아니라 지정된 `cpf-docs/**` 경로**에 작성한다.

- `cpf-docs/work/review/CPF_20260730_QA32_PRE_DEVELOPMENT_REVIEW.md`
- `cpf-docs/work/review/CPF_20260730_QA32_DEVELOPMENT_COMPLETION_REPORT.md`
- `cpf-docs/quality/CPF_20260730_QA32_RESULT_MATRIX.csv`
- `cpf-docs/quality/CPF_20260730_QA32_UNRESOLVED_REGISTER.csv`
- `cpf-docs/quality/CPF_20260730_QA32_OSS_MIGRATION_RESULT.csv`
- `cpf-docs/work/handover/CPF_20260730_QA32_DEVELOPMENT_HANDOVER_RESULT.md`
- `cpf-docs/work/current/CPF_20260730_QA32_CODEX_REVIEW_READY.md`
- `cpf-docs/evidence/current/CPF_20260730_QA32_COMPLETION_EVIDENCE.json`
- `cpf-docs/evidence/current/requirements/<REQUIREMENT_ID>.json`
- `cpf-docs/evidence/current/scenarios/<SCENARIO_ID>.json`
- `cpf-docs/work/manifest/CPF_20260730_QA32_DEVELOPMENT_RESULT_MANIFEST.json`

모든 Requirement 62건과 Mandatory Scenario 202건의 ID Coverage가 있어야 한다.

## 9. 사용자 전달 ZIP 규칙

개발 완료 후 변경 Source와 결과 문서를 **프로젝트 Root 상대경로 Overlay ZIP**으로 생성한다.

- 신규 설명 문서나 Manifest를 ZIP 최상위에 만들지 않는다.
- 결과 Manifest는 `cpf-docs/work/manifest/CPF_20260730_QA32_DEVELOPMENT_RESULT_MANIFEST.json`에 둔다.
- 실제로 수정된 기존 Root Source 파일(`settings.gradle`, `build.gradle` 등)은 ZIP에 포함할 수 있다.
- `.git`, `node_modules`, Gradle/npm Cache, Build Output, 실제 Secret, Private Key, Credential, DB Dump는 제외한다.
- ZIP 내부 각 파일은 Repository Root에서의 상대경로를 그대로 유지한다.
- ZIP SHA-256, ZIP 파일 수, Base SHA, Head SHA를 출력한다.
- 사용자에게 실제 다운로드 가능한 ZIP 링크를 제공한다.
- 결과 ZIP 생성 후에도 Commit·Push는 하지 않는다.

권장 명령:

```powershell
pwsh -NoProfile -File cpf-tools/scripts/package-cpf-qa32-development-result.ps1 `
  -Root . `
  -BaseSha <DEVELOPMENT_START_SHA> `
  -OutputZip <OUTPUT_ZIP_PATH>
```

## 10. 완료 응답 형식

최종 응답에는 최소한 다음을 포함한다.

1. 구현 요약과 Legacy 제거 결과
2. Requirement/Defect/Scenario/OSS Migration 상태 집계
3. 실제 실행한 명령·환경·Exit Code·Evidence 경로
4. 실행하지 못한 검증과 그 이유 — 미실행을 PASS로 기록 금지
5. Root-relative 개발 결과 ZIP 다운로드 링크
6. ZIP SHA-256과 파일 수
7. `cpf-docs/work/review/CPF_20260730_QA32_DEVELOPMENT_COMPLETION_REPORT.md` 경로
8. Commit·Push를 하지 않았다는 사실
