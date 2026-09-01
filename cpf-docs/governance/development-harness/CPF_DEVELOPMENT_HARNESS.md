# CPF Development Harness — Current

> **단일 개발/QA 실행 정본**. CPF 개발 진행, 검수, 보완, Runtime, Evidence, 역할 상태, 세부 리뷰, 인수인계는 이 Harness만 사용한다. 과거 분산 개발 정본·원장·Evidence는 Harness에 의미 손실 없이 흡수한 뒤 exact Delete Manifest로 제거한다.

## 1. 목적

CPF(Core Platform Framework)를 금융권을 포함한 업무 시스템의 구축·운영·감사·확장·검증·배포·상용화가 가능한 Business Platform 품질로 유지하기 위한 **실행 가능한 개발 통제 체계**다. Harness는 개발자의 기억이나 대화 문맥 대신 Registry·Policy·Validator·Evidence로 요구사항과 완료조건을 보존한다.

Harness 도입은 CPF Architecture, Owner, Header, 연동, DB, Generator, Starter, Generated Domain, Frontend, Batch, Gateway 규격을 바꾸지 않는다. 기존 Product Contract를 내부 `product/CPF_PRODUCT_ARCHITECTURE_AND_REQUIREMENTS.md`로 흡수하고 더 강하게 추적·검증한다.

## 2. 읽기 순서와 단일 작업대상 정본

1. `CPF_DEVELOPMENT_HARNESS.md` — 실행 진입점과 Merge Control State
2. `current/CURRENT_WORK_ITEM_REGISTRY.csv` — **사람이 직접 관리하는 유일한 작업대상/상태 정본**
3. `standards/CPF_RULE_MODEL_AND_IMPACT_SEARCH_STANDARD.md` — Common Rule / Feature Rule / 검색·영향도 추적
4. `standards/CPF_WORK_ITEM_SESSION_MERGE_AND_REPORT_STANDARD.md` — sessionKey / 개별 Evidence / 자동 Merge Preflight / Final Self Review
5. `standards/DEVELOPMENT_EXECUTION_CORE_POLICY.md` — 개발 기본지침 전체 + 비협상 규칙
6. `product/CPF_PRODUCT_ARCHITECTURE_AND_REQUIREMENTS.md` — 제품 Architecture/Requirement 계약
7. `contracts/contract-registry.json` + `contracts/harness-control-registry.csv` — 코드가 읽는 규칙/통제 Registry
8. `current/CANONICAL_PRODUCT_REQUIREMENTS.csv` — Product Requirement Registry
9. `current/ROLE_EXECUTION_LEDGER.csv`, `current/TEST_EXECUTION_LEDGER.csv` — 역할/Test 실제 수행 근거
10. `evidence/<role>/current/sessions/<sessionKey>/**` — exact-source 세션 실행근거

`CURRENT_DEVELOPMENT_STATUS.csv`, Role/Test Ledger, Review 문서, 세션 Report는 별도의 작업대상 목록이 아니다. `CURRENT_WORK_ITEM_REGISTRY.csv` 자체가 DevGPT/Independent Reviewer/QA/Runtime/overall 상태와 `current_action`을 한 화면에서 판단할 수 있도록 Projection을 제공해야 하며, 상세 명령·로그는 보조 원장/Evidence로 Drill-down한다. 신규 작업·Finding·Defect를 어디에 등록할지 애매하면 항상 `CURRENT_WORK_ITEM_REGISTRY.csv` 한 곳에서 시작한다. 상위 제목만 추가하지 않고 실제 개발·검증 가능한 세부항목까지 같은 파일의 행으로 분해한다.

## 3. 비협상 완료 모델

`Requirement 등록 → Root Cause/WP 세분화 → 변경 전 영향도 → Source/Consumer 구현 → Targeted → Side Effect/Regression → 최대강도 Runtime → Evidence → 역할별 검수 → 모든 필수 Finding CLOSED → Canonical Final Gate PASS → Fresh Replay → QA 최종 PASS`.

다음은 **완료가 아니다**: Interface/DTO/Mock/Sample/Swagger/메뉴/Route/문서만 존재, 일부 Test만 PASS, 필수 Runtime 미실행, 일부 DB Vendor만 PASS, UNKNOWN/SKIP/NOT_EXECUTED/UNVERIFIED 잔존, evidence/source identity 불일치, Consumer 없는 추상화, 구현 후 영향도 재검증 누락.

## 4. 역할

- **DEVGPT**: 개발·보완·자체검수·개발 Evidence. 개발 가능한 범위를 끝까지 닫는다.
- **INDEPENDENT_REVIEWER**: **Codex와 Claude를 동일 역할**로 취급. 독립 검수·필요 보완·독립 Evidence. DevGPT 판정을 자동 승계하지 않는다.
- **QA**: 최종 Acceptance Owner. 재개발/재검수/최종 완료 판정.

역할별 PASS는 `current/ROLE_EXECUTION_LEDGER.csv` 필수 근거가 모두 있을 때만 허용한다.

## 5. Hardcoding 금지

Validator와 Script는 Requirement 개수, Profile, DB Vendor, Header, 상태 enum, canonical path를 자체 literal로 복제하지 않고 `contracts/*.json`과 current registry/source discovery를 읽는다. Product 계약상 고정값은 `contract-registry.json` 한 곳에서만 선언하고 변경 시 Product Contract/Consumer/Test를 함께 currentize한다. "현재 값에 맞춘 expected count"로 오류를 숨기는 수정은 False Green이다.

## 6. 개발 영향도와 세부 리뷰

모든 수정은 `standards/CPF_IMPACT_ANALYSIS_AND_CHANGE_CLOSURE_STANDARD.md`를 적용한다. 사용자가 리뷰를 요청하면 `standards/CPF_REVIEW_OUTPUT_STANDARD.md`대로 **실제 Current Work Item Registry에 존재하는 전체 항목을 개수 하드코딩 없이 세부항목별 1:1** 리뷰할 수 있어야 한다. `validators/generate_detailed_review.py`로 뼈대를 재생성한다.

## 7. 테스트/환경

`standards/CPF_MAX_INTENSITY_TEST_AND_RUNTIME_STANDARD.md`가 모든 역할과 사용자 로컬 Test 요청의 기본 강도다. 환경 부족 시 smoke로 축소하지 않고 `BLOCKED_EXTERNAL` + Windows/Linux 최고강도 실행명령 + prerequisite + PASS/FAIL 기준 + Evidence 요구를 남긴다.

실행 명령의 Java/Node/npm/Python/PowerShell/Docker/DB/Browser 등 prerequisite는 **대화 기억, 과거 Evidence, 사용자 PC의 현재 설치값에 맞춰 임의 작성하지 않는다.** 실행 직전 Current Source의 canonical bootstrap, verifier, package metadata, toolchain contract, lock/config에서 required 값을 다시 읽고 `required / actual / prerequisite source`를 기록한다. Source가 요구하는 값과 환경이 다르면 Product Contract를 로컬 환경에 맞춰 낮추지 않고 환경을 교정하거나 정확한 `BLOCKED_EXTERNAL`로 남긴다. 자세한 규칙은 `standards/CPF_RULE_MODEL_AND_IMPACT_SEARCH_STANDARD.md`의 Current Prerequisite 규칙을 따른다. Host Toolchain은 capability-first를 기본으로 하며 특정 patch/minor 고정을 개발 편의 때문에 추가하지 않는다. 설치된 버전이 실제 Source가 요구하는 기능/API/언어·바이너리 target을 수행할 수 있으면 그대로 사용하고, exact pin은 Gradle Wrapper·npm lock·CPF-owned Container/Image 등 Project가 소유하는 재현성 경계에만 허용한다. Java는 Java 25 target을 유지하되 Host JDK를 정확히 25로 고정하지 않고 `javac --release 25`와 실제 Gradle Build capability로 판정하며, 다른 Host Tool도 기술적으로 증명된 hard compatibility 경계가 아니면 버전 숫자만으로 차단하지 않는다.

## 8. Profile·YAML·JavaDoc·UTF-8

- Runtime profile: `local/dev/stg/test/prod` 전 세트.
- YAML: 사람이 관리하는 설정값에 인접 한글 설명 주석.
- Java: Public API/SPI/Annotation/Configuration 및 중요 Runtime은 JavaDoc 생성 가능한 상세 설명, 핵심 의도/복구/동시성/보안 한국어 주석.
- Text: UTF-8 + NFC, mojibake/control-char fail-closed.

현재 Source의 미준수는 Harness 완료를 속여 PASS시키지 않고 `PRODUCT_CONFORMANCE_FINDINGS.csv`에 등록해 후속 개발 Requirement로 관리한다.

## 9. Standalone·Windows/Linux

Standalone process는 run/start/stop/status/verify의 Windows PowerShell/Linux shell parity를 갖는다. 기존 Source의 canonical CLI/Runtime script를 재사용하고 Engine 복제 Wrapper를 만들지 않는다. OS 한쪽만 구현하면 완료가 아니다.

## 10. 제품 완성도

기능뿐 아니라 사용자/개발자 DX, 가독성, 오류 메시지, 보안, 접근성, 운영성, 관찰 가능성, 성능, 설치/배포/업그레이드/롤백, Generator, Sample/EDU, OpenAPI/Frontend, Public Release까지 `standards/CPF_PRODUCT_COMPLETENESS_AND_USER_QUALITY_STANDARD.md`로 검수한다.

## 11. Current-only

Development Harness는 이 디렉터리 **현행본 하나만** 유지한다. Harness 버전별 폴더나 과거 정본/세션/Checkpoint/RERUN 복제본을 두지 않는다. 구형 분산 정본은 `CANONICAL_MIGRATION_MAP.csv`의 unmapped=0과 Harness Gate PASS 후 `DELETE_MANIFEST.csv` exact allowlist로 사용자만 삭제한다.

## 12. 실행

Harness 문서가 실제 Source에 존재하지 않는 Wrapper를 정본처럼 안내해서는 안 된다. 현재 Harness Self Gate의 실제 진입점은 다음이다.

### Windows PowerShell
```powershell
python .\cpf-docs\governance\development-harness\validators\run_all_gates.py
python .\cpf-docs\governance\development-harness\validators\show_status.py
```

### Linux
```bash
python3 ./cpf-docs/governance/development-harness/validators/run_all_gates.py
python3 ./cpf-docs/governance/development-harness/validators/show_status.py
```

삭제는 `DELETE_LEGACY_CANONICAL.ps1/.sh`의 exact Delete Manifest 계약을 따르며, 제품 최대강도 Runtime은 Current Work Item과 `current/CPF_REQUIRED_FULL_RUNTIME_REQUEST.md`에서 **현재 Source에 실제 존재하는 canonical entrypoint**를 확인한 뒤 실행한다. 문서에 적힌 과거 경로가 Source와 불일치하면 Source를 억지로 맞추지 말고 Harness Finding으로 currentize한다.

## 13. 최종 판정

Harness 자체 `HARNESS_SELF_ACCEPTANCE=PASS`와 Product `PRODUCT_CONFORMANCE`는 분리한다. Harness가 잘 만들어졌다는 사실은 현재 Product Runtime이 검증됐다는 의미가 아니다. Product 전체 완료는 QA가 모든 mandatory Closure와 Runtime/Fresh Replay Evidence를 확인한 뒤에만 선언한다.

## 14. Harness Control Registry

`contracts/harness-control-registry.csv`는 개발/검수 품질축을 Machine-readable Control로 고정한다. Harness 변경 시 Control을 삭제·약화하거나 enforcement를 제거하면 Self Gate가 FAIL한다. Codex/Claude Source 수정 시 VS Code 규칙은 `standards/CPF_INDEPENDENT_REVIEWER_VSCODE_ZERO_DIAGNOSTIC_STANDARD.md`를 추가로 적용한다.


## 15. Test Execution Ledger

역할 원장과 별도로 `current/TEST_EXECUTION_LEDGER.csv`를 사용한다. Test 존재와 Test 실행을 분리하며 실제 수행 명령·환경·시작/종료·ExitCode·관찰 결과·Evidence SHA·Source Identity·완료/미완료 사유가 없는 PASS를 금지한다.

## 16. Review 요청 처리

사용자가 개발/검수/보완/완료 리뷰를 요청하면 `validators/generate_detailed_review.py`로 **모든 Current Work Item을 하나도 생략하지 않고** 동일 인덱스 순서로 출력한다. 요약만 반환하지 않는다. 원 Requirement, Root Cause/Observation, 영향 Source·Consumer, 개발 범위, 실제 변경, Side Effect/Regression, Static/Runtime Acceptance, 역할별 수행/미수행, Test Ledger, Evidence, 완료/미완료 사유를 1:1로 보여준다.

## 17. Harness 자체 최종 리뷰

Harness를 수정할 때도 제품 개발과 같은 규칙을 적용한다. 변경 영향도 → Self Gate → Negative Mutation → Fresh Replay → Legacy Delete Replay → stale reference 0 → Package hash → ZIP 재추출 Replay 순서가 모두 PASS해야 현행 Harness로 전달한다. Harness의 이전 버전/backup/history/checkpoint/rerun 파일은 남기지 않는다.

- Garbage/Delete 의사결정 정본: `current/CURRENT_GARBAGE_DECISIONS.csv` + `current/DELETE_MANIFEST.csv`

## 18. 최종 전달·인수인계
최종 전달은 `standards/CPF_FINAL_DELIVERY_AND_HANDOVER_STANDARD.md`를 따른다. ZIP/SHA/재추출 Replay, 한 줄 Apply/Delete/Verify/Windows·Linux 최대강도 Runtime/Git Status, 빈 폴더 정리, 상세 Handover가 하나라도 빠지면 완료 보고를 금지한다.

## 19. Common Rule / Feature Rule 모델

모든 Work Item은 `standards/CPF_RULE_MODEL_AND_IMPACT_SEARCH_STANDARD.md`의 Common Rule 전체를 자동 적용받는다. 기능별 Rule은 추가 Acceptance일 뿐 Common Rule을 면제하지 않는다.

예를 들어 UTF-8은 Logging만의 규칙이 아니라 모든 Source/Text/Runtime/Evidence에 적용되는 Common Rule이고, DB3·Open Git·VS Code/JDT·Batch·Logging은 각각 자신의 Feature Rule을 추가로 적용한다. `refDB/referenceFixture` 같은 retired 명칭은 문자열 존재만으로 판단하지 않고 ACTIVE_CURRENT / RETIRED_PROHIBITION / IMMUTABLE_PROVENANCE / NEGATIVE_TEST / FINDING_EVIDENCE로 분류한다. Current 실행경로로 재생성되는 흔적은 Mandatory Finding이며, immutable released provenance는 임의 변조하지 않는다.

## 20. Session Report와 자동 Merge Preflight

모든 개발·검수·QA/Harness 작업은 sessionKey를 발급하고 `standards/CPF_WORK_ITEM_SESSION_MERGE_AND_REPORT_STANDARD.md`를 따른다.

새 작업자는 사용자에게 별도 Merge 지시를 기다리지 않는다. 작업 시작 전에 모든 `evidence/*/current/sessions/*/SESSION_MANIFEST.json`을 검색해 미Merge/PARTIAL/CONFLICT 세션을 찾고, Work Item별 Evidence를 검증해 `CURRENT_WORK_ITEM_REGISTRY.csv`에 순차 Merge한 뒤 새 작업을 시작한다.

세션 Report는 여러 Work Item을 `일괄 완료/동일 PASS/일괄 SKIP`으로 작성할 수 없다. Work Item마다 독립 Evidence Block을 갖고, 같은 실제 로그를 공유하더라도 그 로그의 어떤 assertion/transaction/query가 해당 Work Item을 증명하는지 별도로 연결한다. `SESSION_MANIFEST.json`은 Work Item/Evidence/merge 상태를 구조화하며, 작업 Agent 스스로 자신의 Report를 `MERGED`로 확정하지 않는다.

### Current Merge Control State

가변 Merge 상태는 Product Source Identity 순환변경을 방지하기 위해 `current/CURRENT_MERGE_CONTROL_STATE.json` 한 파일이 소유한다. 이 본체는 상태값 자체가 아니라 규칙과 필수 Field 계약만 소유한다. Validator는 해당 Current state를 읽어 전체 Session discovery 결과와 `pending/conflict/merged set digest`를 exact 비교한다.

필수 Field: `merge_protocol_version`, `merge_baseline_source_identity`, `last_merged_session_key`, `merged_session_set_digest`, `pending_session_keys`, `conflict_session_keys`, `last_merge_review_at`, `last_merge_reviewer_session_key`.

## 21. 역할별 Merge 권한과 QA

중앙 담당자를 매번 수동 지정하지 않는다. 대신 **다음 세션 작업자가 이전 미Merge 세션을 먼저 정리**한다. 다만 한 시점에 `CURRENT_WORK_ITEM_REGISTRY.csv`를 쓰는 Merge writer는 하나만 허용하고, writer는 Registry SHA 기반 compare-before-write로 병렬 변경 유실을 차단한다. 쓰기 직전 SHA가 달라지면 덮어쓰지 않고 최신 Registry에서 Merge를 다시 계산한다.

Developer/Claude/Codex는 자신의 실제 개발·검수 사실을 Merge할 수 있지만 다른 역할의 PASS를 대신 만들 수 없다. QA는 최종 Acceptance 전에 Session Manifest 전체와 Current Registry를 다시 대조해 Pending/Conflict=0, Evidence/Source Identity 일치, 역할별 근거 완전성을 확인한다.

## 22. 최종 전체 Self Review Hard Gate

최종 완료 전에는 모든 Mandatory Work Item을 **한 건씩** 리뷰한다. `전체 410건 완료`처럼 집계만 작성하는 Final Report를 금지한다.

각 Work Item은 `원 Requirement → Root Cause → Owner → Source → Consumer/호출경로 → Config/DB/Generator/API/Frontend → 오류/복구 → Test → Runtime → Regression → Evidence/SHA → Source Identity → 역할별 상태 → 완료/미완료 사유` 순서로 독립 검증한다.

Evidence 없는 CLOSED/PASS, 여러 Work Item의 일괄 완료, 일괄 SKIP, 과거 Source PASS 자동 승계, mandatory NOT_EXECUTED/UNKNOWN/VERIFICATION_PENDING/BLOCKED_EXTERNAL, 미Merge Session, MERGE_CONFLICT가 하나라도 있으면 Final Gate는 PASS가 아니다.


## 23. Gradle Developer UX / Dynamic Component Lifecycle (Mandatory)

일반 개발자는 내부 Gradle 구조를 몰라도 전체/개발/개별 작업을 바로 실행할 수 있어야 한다.
아래는 `cpf-tools/build/cpf-root-conventions.gradle` 이 단일 Owner 로 지키는 계약이며,
회귀는 `cpf-tools/verification/tests/test_cpf_gradle_task_group_readability.py` 와
`cpf-tools/verification/tests/test_cpf_developer_shell_contract.py` 가 fail-closed 로 막는다.

### 23.1 사용자 Task 그룹

- canonical 사용자 그룹은 `00. CPF 시작` / `10. CPF 빌드` / `15. CPF 테스트` / `20. CPF 검증` /
  `30. CPF 실행` / `40. CPF 구성` / `50. CPF 설정` / `60. CPF 배포` 뿐이다.
- 내부 orchestration/Gate/원시 명령은 `90~96`, 예전 이름 호환은 `98`, Gradle/Plugin 원시 명령은 `99`.
- 내부 Task 를 삭제하지 말고 사용자 그룹에서 분리한다. Task ID rename/remove 는 Consumer 계약 위반이다.
- 사용자 Task description 은 `[전체] [개발] [온라인] [배치] [개별] [선택] [조회] [안내]` 중 하나로 시작한다.

### 23.2 Canonical Target Catalog

- `ALL` / `DEV` / `ONLINE` 은 `cpfTargetCatalog` 한 곳에서만 정의한다.
- Build/Test/Run 은 같은 `cpfResolveTarget` 결과(또는 같은 Catalog 의 `runtimeMode`)를 소비한다.
  축마다 Component 목록을 따로 들고 있으면 같은 이름이 다른 대상을 뜻하게 된다.
- Runtime 은 Local Runtime 이 이미 소유한 모듈 구성(`application-local-<mode>.yml`)을 재사용한다.
  `ALL=full`, `DEV=standard`, `ONLINE=minimal`.
- `DEV` 는 `ALL` 과 실제 대상이 달라야 한다(현재 계약: Gateway 제외).
- `cpfTargets` 로 각 Target 의 실제 포함/제외 Component 를 사용자 의미로 확인할 수 있어야 한다.

### 23.3 Generated Domain 자동 Lifecycle

- Domain 이름을 Gradle Source 에 하드코딩하지 않는다. `cpf.domain.contractVersion=1` 계약과
  `settings.gradle` / 실제 module 디렉터리로만 발견한다.
- 생성하면 Build/Test/Run/개별 Task/ALL/DEV 에 자동 포함되고, 삭제하면 자동으로 빠져야 한다.
  사람이 Gradle Source 를 고쳐야 하면 계약 위반이다.
- Capability 기준으로만 투영한다. `batch` 를 선언하지 않은 Domain 에 Batch Task 를 만들지 않는다.
- Domain 0개도 정상 상태다.

### 23.4 App / Backoffice Component Lifecycle

- 개별 App 은 구조 규칙(최상위 `cpf-*` + Spring Boot 실행 계약)으로 발견한다.
  `cpf-tools/runtime/**` 통합 Runtime 과 Domain 소유 module 은 구조상 제외되어 축이 중복되지 않는다.
- Backoffice 는 Generated Domain 이 아니라 Optional Component 다. Source 에 존재하는 동안
  DEV/ALL 기본 구성이며, 삭제하면 자동으로 빠진다.
- Backoffice Domain 과 Backoffice Web 은 독립 Component 다. 한쪽 삭제가 다른 쪽 Source 를
  자동으로 지우면 안 된다.
- Optional Component 가 부재해도 root 진입점은 "정상 부재"를 알려야 한다(absence-safe).

### 23.5 배포 UX

- `60. CPF 배포` 에는 `[전체]` 진입점과 `[개별]` 대상만 노출한다.
- staging/isolated local publication/verified platform 조립 등 publication orchestration 은
  `94. CPF 내부 배포` 로 분리하고, 예전 이름은 `98` 호환 별칭으로 계속 지원한다.

## 24. Open Git Public Product Distribution (Mandatory)

Open Git 은 고객이 CPF 를 개발하고 **실행하는** 배포 채널이다. Binary 를 제공했는데 실행할 수
없거나, 문서 없이 Artifact 만 있거나, 외부 Repository 주소를 사용자가 직접 채워야 하면 Public
Release 로 인정하지 않는다.

### 24.1 Final Tree 는 Allowlist fail-closed projection

- Staging Maven repository 를 `copytree` 로 승격하지 않는다. 허용된 유형만 복사한다.
- 기본 허용: **Main JAR + POM**.
- 기본 제외: `.md5` / `.sha1` / `.sha256` / `.sha512` sidecar, sources JAR, javadoc JAR,
  timestamped SNAPSHOT artifact, staging 부산물, 이전 version artifact.
- 조건부(`.module`, `maven-metadata.xml`)는 격리 Fresh Consumer 실증으로만 결정한다.
  "Gradle/Maven 이 만들었다"는 포함 근거가 아니다.
- Allowlist 방식이므로 대상 artifact 없는 orphan sidecar 는 구조적으로 발생하지 않는다.

### 24.2 Public Release Version 은 immutable

- Development/Staging 은 `-SNAPSHOT` 을 쓸 수 있다.
- Final Public Artifact 파일명에는 SNAPSHOT/날짜/시각/timestamp/build sequence/session id 를
  넣지 않는다. `artifactId + immutable public version + 승인된 classifier` 로만 결정한다.
- 동일 Source + 동일 Public Version 을 Fresh Release 하면 상대경로와 파일명이 같아야 한다.
- Validator 는 Public Tree 에서 SNAPSHOT/timestamp/build sequence 파일명을 발견하면 FAIL 한다.

### 24.3 Bundled Public Binary Repository

- Final Open Git Tree 안에 Public Binary Repository 가 실제로 존재해야 한다.
- README 에 `<cpf-binary-repository-url>` 같은 placeholder 를 남기지 않는다. bundled repository
  가 기본값이고 외부 URL 은 선택 override 다.
- checkout 만으로 resolve → build → generator → runtime 이 동작해야 한다.

### 24.4 Public Runtime 은 실행까지 계약이다

- executable Runtime 의 `publicationClass` 를 미분류로 남기지 않는다.
- Product Contract 상 Public 인데 Binary 가 없으면 **Publication 누락 Finding** 으로 처리한다.
  현재 미게시를 근거로 PRIVATE 로 재분류하지 않는다(순환논리 금지).
- Public Binary 로 제공하는 Runtime 은 Windows/Linux launcher 와
  start/stop/status/health/restart/log lifecycle 까지 연결한다.
- Binary 없는 Launcher Target, Launcher 없는 Public Binary 는 모두 FAIL 이다.

### 24.5 Canonical Runtime Target Catalog

- Gradle Task, Public CLI, Windows/Linux launcher 는 하나의 canonical Target Catalog 를 읽는다.
  Target 이름/Domain/App 목록을 두 곳에서 관리하면 FAIL 이다.
- Generated Domain 과 Backoffice 는 이름을 catalog/launcher 에 박지 않고 discovery 규칙으로만
  표현한다. Domain 추가/삭제에 launcher 수정이 필요하면 Architecture 위반이다.
- Backoffice Domain 과 Web 은 독립 Component 다. 한쪽 삭제가 다른 쪽 Target 을 지우면 FAIL 이다.
- 서로 다른 Target 의 기본 port 중복은 0 이어야 한다. 같은 Target 이 profile 마다 같은 port 를
  반복하는 것은 중복이 아니다.
- Runtime capability 에 없는 health endpoint 를 억지로 만들지 않는다. server/worker/one-shot 별
  readiness 계약을 쓰되 CLI UX 는 동일하게 제공한다.

### 24.6 Public Documentation 도 Allowlist

- Final Tree 에 root `README.md` 와 공개 `cpf-docs/` 가 있어야 한다.
- `cpf-docs/**` 전체 복사는 금지한다. governance / work / development / environment / brand /
  internal deliverable 은 Leakage 다.
- 공개 문서는 현행본을 그대로 projection 한다. Open Git 작업을 이유로 공식 문서를 새로 쓰거나
  품질을 낮추지 않는다. README 는 placeholder/실행 안내 누락만 증분 보완한다.
- README link, 문서→이미지 link 가 Final Tree 기준으로 실제 존재해야 한다.

### 24.7 Package Manifest 와 무결성

- Release Root 에 canonical Package Manifest 하나를 둔다. 최소 group / artifactId / module /
  version / classifier / type / relativePath / fileSize / SHA-256 / publicationType /
  classification / Source Identity 를 기록한다.
- Manifest 에 없는 binary, Manifest 가 가리키는데 없는 파일, SHA 불일치는 FAIL 이다.
- 개별 checksum sidecar 를 Public Tree 에 두지 않더라도 Release 과정의 SHA-256 검증은 유지한다.
  Mandatory SBOM 은 checksum sidecar 와 다르므로 최소화 작업에서 제거하지 않는다.

### 24.8 Fresh Consumer 가 최종 판정자

- 파일 수를 줄였다는 이유로 PASS 하지 않는다.
- Final Open Git Tree 만으로 격리 dependency cache 에서 resolve → transitive → build → test →
  CLI → Generator → Generated Sample → Runtime → cleanup → Fresh Replay 를 수행한다.
- Development Master 의 module/source/local Maven cache 에 의존하면 FAIL 이다.
- Windows 와 Linux 를 모두 수행한다. 한쪽만 실행하면 Public Runtime Final PASS 가 아니다.

### 24.9 Negative Mutation

다음 재유입이 Release Gate 에서 FAIL 해야 한다.

- Public Binary/POM 누락, Manifest 미등록 binary, Manifest SHA 변조
- SNAPSHOT/timestamped Public Artifact, checksum sidecar, orphan sidecar
- 미승인 `.module` / `maven-metadata.xml`, stale version artifact
- Binary 는 있는데 Launcher Target 없음 / Launcher Target 은 있는데 Binary 없음
- Windows/Linux Target parity 파괴, Generated Domain/Backoffice stale Target
- README placeholder 재도입, 필수 Public 문서 누락, broken link, governance leakage
- 서로 다른 Runtime 의 기본 port 중복

### 24.10 Git write 경계

- Release Tool 은 `git add`/commit/push/tag/release publish 를 수행하지 않는다.
- Runtime 최종 결과, Final Tree, Package Manifest, Fresh Consumer 결과, Leakage 0,
  Source Identity, exact diff 를 보고하고 `OPEN GIT PUSH READY` 로 대기한다.
- 사용자 명시 승인 후에만 Git write 를 수행한다.

### 24.11 Release Generated Root 는 Current-only

- `clean` 은 Source build clean 만이 아니다. Release generated root 자체가 Current-only 다.
- 매 Release 시작 시 exact `<CPF_ROOT>/cpf-release` 전체(`open-git/`, `binary-repository/`,
  `reports/`, `logs/`, `work/`)를 안전하게 제거하고 0부터 Fresh 생성한다.
- 이전 Release 파일을 merge / copy-over / currentize 해서 재사용하지 않는다.
- Open Git fresh clone 에서도 staging 에 없는 과거 Working Tree 파일은 0 이어야 한다.
- `.git/**` 은 Open Git Repository history 이므로 Release Artifact garbage 로 취급하거나
  삭제하지 않는다.
- cleanup 은 승인된 generated root 안에서만 수행한다. Private Source 와 `.git` 은 절대
  삭제하지 않는다.
- legacy publisher 는 독립 Release root(`CPF_PUBLIC_RELEASE_<timestamp>`)를 소유하지 않는다.
  canonical engine 의 staging backend 역할로만 제한하고, 출력 경로 없는 호출은 fail-closed 다.
- Release 전/후 generated-root inventory 를 Evidence 로 남긴다. 최소 previous canonical
  release residue 0 / legacy timestamp release 0 / stale open-git working-tree files 0 /
  stale binary version 0 / current release only PASS 를 검증한다.

## 25. Runtime Bean 해석 계약 (Mandatory)

컴파일과 단위테스트를 모두 통과하고도 Runtime 기동에서만 드러나는 결함이 반복 발생했다. 아래
계약은 그 결함들을 정적 게이트로 고정한 것이며, 각 항목은 실제 기동 실패 근거를 가진다.

### 25.1 CPF stereotype Type은 proxy-safe여야 한다

`@CpfRepository` / `@CpfService` / `@CpfController` / `@CpfRestController`가 붙은 Business Type은
`final`일 수 없다. 제품 자신이 이 규칙을 명시한다 — `CpfCapabilityUsageAspect.proxySafeBusinessType()`
은 final Type을 proxy-unsafe로 판정한다. Advisor가 매칭되면 CGLIB subclass 생성이 불가능해
`Cannot subclass final class`로 기동이 실패한다.

Gate: `cpf-tools/verification/tests/test_cpf_proxied_stereotype_not_final.py`

### 25.2 `@AutoConfiguration`은 반드시 등록한다

Spring Boot는 `AutoConfiguration.imports`에 나열된 클래스만 활성화한다. 등록하지 않으면 Bean이
조용히 사라지고 소비자는 `required a bean ... that could not be found`로만 실패한다. 등록이
의도적으로 없는 항목은 게이트의 `KNOWN_INACTIVE`에 근거와 함께 남기고, 등록되면 목록에서 뺀다.

`@ConditionalOnBean`을 쓰는 AutoConfiguration은 대상 Bean을 등록하는 AutoConfiguration 다음에
평가되도록 `after`/`afterName`으로 순서를 명시한다. 순서를 생략하면 Bean이 있어도 조건이 거짓이
될 수 있다.

Gate: `cpf-tools/verification/tests/test_cpf_autoconfiguration_registration.py`

### 25.3 생성자가 여럿이면 주입 대상을 명시한다

운영 생성자와 테스트 seam을 함께 두는 것은 정상 설계다. 다만 생성자가 둘 이상이고 기본 생성자가
없으면 Spring은 어느 것을 쓸지 정하지 못하고 `No default constructor found`로 기동이 실패한다.
운영 주입 생성자에 `@Autowired`를 붙인다(완전수식 표기도 유효).

Gate: `cpf-tools/verification/tests/test_cpf_injection_constructor_unambiguous.py`

### 25.4 동일 타입 Bean이 여럿인 자리는 해석 근거를 명시한다

- **DataSource**: Runtime에는 업무 Domain DataSource가 함께 존재한다. 타입만으로 주입하지 말고
  `CpfDataSourceRegistry.require(CpfDatabaseRole.CPF_PLATFORM_DB)`로 Role을 명시한다.
- **Clock**: `cpfStarterClock`(foundation)과 `cpfCommonClock`(common)이 의도적으로 공존한다.
  파라미터 이름을 `cpfStarterClock`으로 맞추거나 `@Qualifier`로 명시한다.

### 25.5 Spring Boot 4에서 사라진 자동설정은 CPF가 소유한다

Spring Boot 4는 Boot 3이 제공하던 Bean 일부를 더 이상 auto-configure하지 않는다.

- `com.fasterxml.jackson.databind.ObjectMapper` — Boot 4는 Jackson 3 `tools.jackson...JsonMapper`를
  만든다. `CpfJackson2AutoConfiguration`이 소유하며, 다른 스타터의 대체 Bean이 먼저 등록되어
  foundation이 back-off하지 않도록 순서를 명시한다.
- `WebClient.Builder` — Boot 4에 `WebClientAutoConfiguration`이 존재하지 않는다. HTTP Integration
  스타터가 CPF 표준 규약(timeout, codec 상한, 통합 로그 필터)을 담아 **prototype scope**로 소유한다.
  Builder는 가변 객체이므로 싱글턴 공유는 소비자 설정을 서로 오염시킨다.

`required a bean of type` 실패가 나오면 먼저 Boot 4 자동설정 제거 여부를 확인한다.

### 25.6 Provider의 선택성과 소비자의 요구는 일치해야 한다

Provider AutoConfiguration이 `@ConditionalOnProperty`로 opt-in인데 소비자가 그 Bean을 필수 주입하면,
기능을 쓰지 않는 Runtime이 기동조차 못 한다. 소비자는 `ObjectProvider`로 받고 기능이 없으면
`supports()`가 false를 반환해야 한다. 없는 기능을 지원한다고 보고하지 않는다.

소비자가 취할 수 있는 해법은 둘이다. 둘 중 하나는 반드시 적용한다.

1. `ObjectProvider`로 받고 기능이 없으면 `supports()`가 false를 반환한다(Owner Command Adapter).
2. Provider와 **같은 속성 조건**을 붙여 기능이 꺼지면 소비자도 함께 사라진다(Controller/Service).

반대로 소비자가 조건 없이 요구하는 Port는 Provider 모듈이 Runtime classpath에 반드시 있어야 한다.
ADM은 internal Provider를 직접 의존하지 않는다 — 선택은 `:apps:admin-runtime` composition 모듈이
단독으로 소유하고, 기대 집합은 `verify_admin_dependency_boundaries.py`가 고정한다.

같은 `changeType`을 두 Applier가 등록하면 `CpfRuntimeControlAgent`가 fail-closed로 기동을 막는다.
소유자를 canonical Bean 이름으로 선언해 다른 Provider의 `@ConditionalOnMissingBean(name=...)`
back-off가 동작하게 한다.

Gate: `cpf-tools/verification/tests/test_cpf_optional_provider_consumer_contract.py`

### 25.6.1 소비하는 설정 속성의 기본값은 소비자가 소유한다

`@Value("${...}")`에 기본값이 없으면 그 속성을 주지 않는 Runtime은 placeholder 미해석으로 기동조차
못 한다. harness가 명령행으로 넘겨주는 것에 의존하면, 같은 Bean을 쓰는 다른 Runtime(1-WAS 등)이
그대로 실패한다. 소비 모듈의 `application.yml`이 `${ENV:default}` 형태로 기본값을 소유하고, 값의
유효성은 사용 시점에 fail-closed로 다시 검증한다.

### 25.6.2 Runtime 데이터 전제는 Verifier가 만든다

Runtime Agent 등록(`OPS_SERVICE` / `OPS_SERVICE_ENDPOINT`), Center-Cut Job 정의처럼 CPF가 seed로
배포하지 않는 운영 데이터를 Runtime이 fail-closed로 요구하는 경우가 있다. 이런 전제는 Verifier가
자기 시나리오 안에서 직접 등록하고 등록 결과를 검증한다. Canonical seed 분류를 바꾸거나 sample
행을 product seed로 승격시키지 않는다. `INSERT IGNORE`처럼 FK 위반까지 삼키는 구문은 쓰지 않는다.

### 25.7 Query Pack은 두 계열을 함께 갱신한다

`cpf-tools/db/runtime-template/**`(템플릿)과 `cpf-tools/db/vendor/<vendor>/runtime/**`(Runtime이
실제 실행하는 Pack)이 병존하며 자동 동기화가 없다. 한쪽만 고치면 Runtime은 여전히 구 SQL을 실행해
`Unknown column`으로 실패한다. Migration으로 컬럼을 바꾸면 두 계열과 `platform-runtime-query-contract.json`의
`parameterCount`를 함께 맞춘다. Migration 파일 자체는 불변 이력이므로 수정하지 않는다.

Gate: `cpf-tools/db/tests/test_query_template_schema_columns.py` (템플릿 + vendor Pack 양쪽 검사)

### 25.8 검증 도구는 Source Tree를 오염시키지 않는다

Source Tree의 형제 모듈을 import하는 도구는 그 옆에 `__pycache__/*.pyc`를 만들고, `clean-source`
게이트가 이를 garbage로 판정한다. 즉 검증을 실행할 때마다 검증이 깨진다. 해당 도구는 호출자의
`-B` 여부에 의존하지 말고 스스로 `sys.dont_write_bytecode = True`를 설정한다. pytest는
`-p no:cacheprovider`로 실행한다.

Gate: `cpf-tools/verification/tests/test_cpf_source_tree_bytecode_hygiene.py`

### 25.9 Full Runtime 실행 중 금지 행위

- **Gradle 병행 실행 금지.** 실행 이력과 산출물이 불일치해 `clean` 이후에도 `UP-TO-DATE` 오판이
  발생하고, 다른 프로젝트의 클래스 파일이 사라진 것처럼 보인다.
- **Source/Managed 파일 편집 금지.** `[162] SOURCE_STATE_AFTER` / `[163] MANAGED_STATE_AFTER`가
  전후 SHA-256 동일성을 요구한다.
- **실행 중인 Gradle의 `--project-cache-dir` 삭제 금지.** 부분 삭제된 execution history는 위와 같은
  오판을 만든다.
- `build/`는 `clean-source` 게이트 검사 대상이 아니다. 삭제할 필요가 없으며, 삭제하면 IDE
  classpath가 깨진다. 정리 후에는 `cpfPrepareIdeClasspath` + `cpfVerifyIdeClasspathReady`로 복구해
  VS Code Problems를 0으로 유지한다.
