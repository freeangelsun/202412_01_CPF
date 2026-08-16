# CPF 문서·디렉터리 통제 정책

- Currentization source/basis: 실행 시 최신 `origin/master` exact SHA를 동적으로 확인한다. 고정 과거 SHA를 Current 기준으로 사용하지 않는다.
- 목적: CPF Repository가 항상 하나의 Current-State 정책만 표현하고 과거 세션/요청/Gate/이관 Script가 다음 작업의 정본으로 오인되지 않게 한다.

## 1. 정본 우선순위

1. `cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md`
2. Architecture/Starter/Generated Domain/Canonical Path 정책
3. `cpf-docs/work/CPF_CURRENT_WORK_REQUEST.md`
4. `cpf-docs/work/current/CPF_DEVELOPER_GPT_NEXT_WORK_INSTRUCTION.md`
5. Requirement/Source Finding/Status/Evidence Current files
6. 실제 Source/SQL/Config/Generator/Test

과거 REV/SESSION/Checkpoint/QA 회차 파일은 위 정본을 대체하지 않는다.

## 2. Current 문서 원칙

- 동일 목적 문서는 새 이름으로 복제하지 않고 기존 Canonical/Current 파일을 직접 현행화한다.
- `_R1`, `_R2`, `_REV`, `_SESSION`, `_YYYYMMDD`, `_FINAL_FINAL`, Checkpoint별 결과 파일을 Current 문서로 생성하지 않는다.
- 정책 변경 시 `기존 정책 → RETAIN/CORRECT/MERGE/SUPERSEDE → 신규 정책 → Source/DB/Generator 영향 → 완료 Gate`를 기록한다.
- 새 정책 한 줄만 추가하고 충돌하는 옛 정책을 활성 문장으로 남기지 않는다.
- Working Tree에는 현재 상태만 둔다. Repository는 Git History 보존을 전제로 하지 않으므로 삭제 전 필요한 Requirement/Decision/Evidence를 Canonical/Current 파일에 병합한다.

## 3. Architecture 정책 전파

Common→Starter, cpfDB 통합, Starter 공개명 변경, Generated Domain DB 정책, Error Catalog처럼 Architecture를 바꾸는 결정은 최소 다음 표면을 함께 currentize한다.

`Final Target → Architecture/Starter Policy → Developer/Generator/EDU Guide → Current Work/Instruction → Requirement Status/Source Coverage → Catalog/BOM/settings/publication → DB canonical/vendor3 → Generator/Golden → ADM/BZA/Reference → Test/Evidence/Handover/Codex`

Repository-wide stale-policy 검색에서 충돌 표현이 남으면 문서 현행화 완료가 아니다. Canonical Requirement/기능 Architecture 현행화의 Owner는 사용자/중앙관리/QA다. 단, 사용자가 **산출물 영역의 명시적 전권**을 위임한 문서 작업에서는 문서 정본·문서 표준·문서 목록/역할·README/Guide/설계 산출물·문서 Evidence를 산출물 리드가 직접 현행화할 수 있다. 이 예외는 기능 Requirement/QA 판정 원장까지 확장되지 않는다.

## 4. 역할과 상태

Developer GPT는 자기 `개발GPT_*` 상태/Evidence와 개발 Source만 갱신한다. Canonical Requirement/Architecture/Current Instruction 및 파생 Requirement Dataset은 직접 수정하지 않는다. QA/Codex 고유 판정을 임의 변경하지 않는다. 실행하지 않은 Runtime은 `미검증`이며 과거 SHA의 PASS를 최신 SHA 성공으로 승계하지 않는다.

## 5. 보호 경로

다음 경로는 명시 승인 없는 삭제/이동 대상이 아니다.

```text
cpf-docs/deliverables/**
cpf-docs/guides/**
cpf-docs/environment/docker/**
cpf-tools/environment/docker-development-test/**
```

## 6. Garbage와 삭제

- 삭제는 Root 상대 exact path `cpf-docs/work/CPF_DELETE_MANIFEST.csv`로만 관리한다.
- `READY_TO_DELETE`는 successor/흡수 근거가 확인된 항목만 허용한다.
- `MIGRATE_THEN_DELETE`는 Source 이관 완료 전 자동 삭제하지 않는다.
- wildcard recursive delete, `git clean`, `git reset --hard`, `git restore .`를 금지한다.
- 실행 전 보호 경로와 Manifest를 검증하고 실행 후 stale reference, broken link, 빈 directory와 `git status --short`를 확인한다.
- Commit/Push는 사용자만 수행한다.

## 7. 다음 개발 필수 Currentization Gate

다음 정책에 대한 활성 구문구 0을 증명한다.

- 독립 `cpf-common` Product Root
- `cmnDB/admDB/batDB` 별도 Target Physical DB
- Generated Domain별 `mbrDB/accDB/...`
- Public `cpf-starter-profile-*` Coordinate
- Capability별 `*/core` Module
- Product Root의 `cpf-member` Generated Domain
- DB Error/Message를 Java enum/source 추가로만 확장하는 구조


## 7.1 산출물 영역 명시 위임 예외

사용자가 산출물 영역의 전체 수정 권한을 명시적으로 부여한 경우 다음은 **문서 품질·현행성·탐색성·Publication 정합성**을 위해 직접 수정할 수 있다.

```text
README.md
cpf-docs/specification/CPF_DOCUMENTATION_STANDARD.md
cpf-docs/governance/CPF_DOCUMENT_CANONICAL_INDEX.md
cpf-docs/governance/CPF_DOCUMENT_CONTROL_POLICY.md  # 산출물 통제 절에 한함
cpf-docs/guides/**
cpf-docs/deliverables/**
cpf-docs/assets/**                           # 문서/README 전용 Asset
문서 관련 Architecture/Developer/Operations Guide의 표현·링크·사용 절차
```

허용 범위에서도 다음은 지킨다.

1. 기능 Requirement/QA 상태를 문서 편의를 위해 완화하거나 재판정하지 않는다.
2. Public API/Owner/DB/Generator/Frontend 계약은 실제 Source와 정본을 먼저 확인한다.
3. 문서 작성 중 Source 결함을 수정하면 Consumer/Test/Config/SQL/Generator/Frontend 영향도를 함께 검토한다.
4. 사용자 문서에는 개발 회차·임시 SHA·미검증 기능을 완료된 기능처럼 적지 않는다.
5. 삭제는 Current replacement·참조 0건을 확인한 exact root-relative manifest로만 수행한다.

## Developer GPT Canonical Write Protection — 영구 규칙

Developer GPT는 **개발요건 정본·기능 Architecture 정본·Current 실행지침 자체를 수정하지 않는다.** 다만 위 `7.1 산출물 영역 명시 위임 예외`가 적용된 세션에서는 문서 정본 3종과 산출물 Publication Surface를 직접 현행화할 수 있다.

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

- `currentization_source_sha`: 문서 현행화 시 비교한 당시 Source SHA이며 영구 현재값이 아니다. 현재 실행은 항상 최신 `origin/master`를 다시 확인한다.
- `execution_source_sha`: 각 Developer/Codex/QA 세션 시작 시 최신 `origin/master`에서 동적으로 확인한다.
- `verified_sha`: Build/Test/Runtime/Evidence를 실제 실행한 exact SHA다.

`currentization_source_sha`를 이후 세션의 영구적인 "현재 master"로 해석하지 않는다. 완료 판정과 Evidence는 `verified_sha`를 기준으로 한다.