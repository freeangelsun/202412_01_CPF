# CPF AI 개발·QA·세션 연속성 표준

- Currentization source/basis SHA: `d50b8468094a412923ab4a3d63013216eeb88e31` (`10_13`)
- 목적: Developer GPT, Codex, QA와 후속 세션이 같은 정본·같은 Source Closure 기준으로 작업하게 한다.

## 1. 시작 기준

모든 개발/검수 세션은 최신 `origin/master` exact SHA와 Working Tree를 먼저 확인하고 `CPF_FINAL_TARGET_REQUIREMENTS.md → CPF_DOCUMENT_CANONICAL_INDEX.md → CPF_CURRENT_WORK_REQUEST.md → 역할별 지침 → 실제 Source` 순서로 읽는다. 과거 대화·완료·Evidence를 자동 승계하지 않는다.

## 2. Requirement-by-Requirement Source Review 강제

각 Requirement는 반드시 다음 순서로 직접 확인한다.

`Requirement → Owner → Source/FQCN → Public API/SPI/Internal → Consumer → 호출 경로 → Config/SQL → 정상/오류/경계/UNKNOWN → 동시성/재시도/복구 → Security/Audit → Generator/Frontend 영향 → Test → exact-SHA Evidence`

Interface/DTO/Sample/Test 파일 존재만으로 완료하지 않는다. 첫 오류만 수정하지 않고 동일 원인의 잠복 결함을 Repository 전체에서 찾아 일괄 수정한다.

## 3. Developer GPT 진행률

Developer GPT는 작업 시작 후 완료까지 **최소 5분 간격**으로 화면에 `전체 진행률 %, 현재 Requirement, 완료/미완료 건수, 현재 Gate`를 표시한다. 진행률은 파일 수가 아니라 Requirement의 Source/Consumer/Test/Evidence 완료율로 계산한다. 진행 보고 직후 작업을 계속하며 보고는 중단점이 아니다.

## 4. 역할

- Developer GPT: Source/SQL/API/Test/Config/Frontend/Generator 구현, 자체검수, `개발GPT_*` 상태/Evidence. Canonical Requirement/Architecture/Current Instruction은 수정 금지.
- Codex: 최신 SHA 독립 검수·보완·실환경 재실행.
- QA: 최종 전체 상태와 QA 판정.

자기 역할 밖의 판정 컬럼을 임의 변경하지 않는다.

## 5. 완료 판정

Source 구현과 Runtime 검증 상태를 분리한다. 실제 실행되지 않은 DB3/Redis/Valkey/Broker/S3/Browser/Process-kill 등의 항목은 `미검증`이고 PASS가 아니다. QA 통과 전 Release 완료라고 표현하지 않는다.

## 6. Architecture 결정권

정본이 Owner/Path/Artifact/DB/Provider를 확정했으면 Developer GPT가 A/B를 다시 선택하지 않는다. 충돌이 발견되면 기능 Acceptance를 약화하지 않고 정본 우선순위와 명시적 Supersession을 적용한다. 기술/보안상 불가능한 경우 근거와 영향을 기록하되 임의 대안을 정본처럼 확정하지 않는다.

## 7. Current Target 핵심

- `cpf-core` = 최소 Kernel. Capability `*/core` 금지.
- Common Product Service = `cpf-starters/common` + `cpf-starter-common`.
- CPF Platform 관계형 DB = `CPF_PLATFORM_DB(cpfDB)` 기본. BZA/Customer Business DB 분리.
- Generated Domain = Customer Repository + Business DB Role + Domain Table Prefix.
- Public Starter = 직관적 `cpf-starter-...` 명칭; Internal leaf 직접 소비 금지.
- Error = Business/Validation/System 최소 taxonomy + DB-driven Error/Response/Message Catalog.
- Redis와 Valkey는 명시적 공식 Provider로 병존.

## 8. Git·삭제 안전

사용자 승인 없이 Commit/Push/Branch/Tag/PR/Reset/Restore/Stash/Clean/Delete를 수행하지 않는다. 삭제는 exact-path Manifest와 사용자 실행 one-line을 사용하며 보호 경로를 fail-closed한다.


## Developer GPT Canonical Write Protection — 영구 규칙

Developer GPT는 **개발요건 정본·Architecture 정본·Current 실행지침 자체를 수정하지 않는다.**

Developer GPT 쓰기 금지 대상은 최소 다음을 포함한다.

```text
cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md
cpf-docs/governance/CPF_DOCUMENT_CANONICAL_INDEX.md
cpf-docs/governance/CPF_DOCUMENT_CONTROL_POLICY.md
cpf-docs/governance/CPF_AI_DEVELOPMENT_QA_CONTINUITY_STANDARD.md
cpf-docs/governance/CPF_STARTER_ARCHITECTURE_AND_LIFECYCLE_POLICY.md
cpf-docs/governance/CPF_GENERATED_DOMAIN_LIFECYCLE_POLICY.md
cpf-docs/governance/CPF_CANONICAL_PATH_AND_ROLE_MAP.md
cpf-docs/governance/CPF_REPOSITORY_SURFACE_INDEX.md
cpf-docs/architecture/ARCHITECTURE_GUIDE.md
cpf-docs/work/CPF_CURRENT_WORK_REQUEST.md
cpf-docs/work/current/CPF_DEVELOPER_GPT_NEXT_WORK_INSTRUCTION.md
cpf-docs/work/current/CPF_REQUIREMENT_MASTER.csv
cpf-docs/work/current/CPF_REQUIREMENT_MASTER.parts/**
cpf-docs/work/current/CPF_EXECUTION_SEQUENCE.csv
cpf-docs/work/current/CPF_EXECUTION_SEQUENCE.parts/**
cpf-docs/work/current/CPF_REQUIREMENT_CONTROL_REGISTER.xlsx
```

정본 오류·충돌·stale를 발견한 Developer GPT는 정본을 직접 고치지 않고 다음을 수행한다.

1. 최신 `origin/master`와 실제 Source/Catalog/settings를 재확인한다.
2. 충돌 경로, 원문, 영향, 필요한 정정안을 `cpf-docs/work/OPEN_ISSUES.md` 및 해당 Evidence에 기록한다.
3. 자신의 구현 Source/Test/Config/Generator와 `REQUIREMENT_STATUS.csv`의 `개발GPT_*` 컬럼 범위 작업은 계속한다.
4. 정본 변경 필요를 작업 중단 사유로 사용하지 않는다.
5. 사용자/중앙관리/QA가 정본을 현행화한 successor master를 받은 뒤 다시 읽고 계속한다.

Developer GPT가 정본을 편의상 고쳐 자신의 구현과 맞추는 행위는 **False Green 및 정본 오염**으로 간주한다.

## Currentization SHA 의미

- `currentization_source_sha`: `d50b8468094a412923ab4a3d63013216eeb88e31` (`10_13`) — 본 문서 현행화 시 비교 기준으로 사용한 Source.
- `execution_source_sha`: 각 Developer/Codex/QA 세션 시작 시 최신 `origin/master`에서 동적으로 확인한다.
- `verified_sha`: Build/Test/Runtime/Evidence를 실제 실행한 exact SHA다.

`currentization_source_sha`를 이후 세션의 영구적인 "현재 master"로 해석하지 않는다. 완료 판정과 Evidence는 `verified_sha`를 기준으로 한다.