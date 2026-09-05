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
- transient `generation-state`가 이전 template hash를 가졌더라도 실제 Generated 파일이 current
  Generator template과 canonical LF text 기준으로 동일하면 source overwrite 없이 state만 재결속할 수 있다.
  Windows Git CRLF checkout은 같은 text로 정규화한다. 내용이 한 글자라도 다르면 사용자 수정으로
  fail-closed 하며, template-equivalent state reconcile을 사용자 수정 overwrite의 예외로 확대하면 FAIL이다.
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
- Fresh Consumer의 `config/cpf-workspace.properties`에 적힌 `cpf.version`이 Runtime/Gradle의
  유일한 version 정본이다. 상위 Shell의 `CPF_VERSION`은 동일 값인지 검증할 수만 있으며,
  다른 값으로 public version을 바꾸려 하면 bootstrap은 즉시 FAIL 해야 한다.

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
- 매 Release 시작 시 exact `<CPF_ROOT>/cpf-release`의 이전 managed 결과를 안전하게 제거하고 0부터 Fresh
  candidate를 생성한다. candidate는 `work/` 아래에서 Build/Publication/Fresh Clone/검증을 마친 뒤에만
  `binary-repository/`, `open-git/`, `reports/`로 한 세트 승격한다.
- 이전 Release 파일을 merge / copy-over / build input으로 재사용하지 않는다. `reports/`와 적정 크기의
  binary metadata는 검증 완료 뒤 Master Current Result가 될 수 있지만, 그 사실이 다음 Fresh Build 입력권한을
  만들지는 않는다.
- `.gitignore`는 `work/`, `logs/`, `open-git/` 같은 transient만 제외한다. `cpf-release/` 전체 제외는
  POM/checksum/manifest/SBOM/report까지 숨기므로 금지한다.
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

## 26. Runtime 실패 수정 전 Ownership/제품 성격 판정 (필수)

Runtime 기동 실패를 Bean/Property 수준에서 바로 고치지 않는다. 반드시 아래 순서로 판정한 뒤
고칠 계층을 정한다.

```
Product Contract / Ownership
  -> mandatory vs optional capability
  -> Public/Admin Route Contract
  -> Port
  -> Provider / Composition Owner
  -> Config
  -> Runtime
  -> Test / Evidence
```

### 26.1 모듈 성격을 같은 모델로 취급하지 않는다

- **CPF Platform 기능(ADM / Gateway / Batch / Backoffice)** — CPF가 제공하는 Framework/Platform
  관리 기능이다. 업무 Domain이 아니다.
- **Generated Business Domain** — 프레임워크를 사용해 개발하는 쪽이다. Starter를 선언해 opt-in 한다.
- **optional Starter capability** — 선언하지 않으면 없는 기능이다.

ADM을 업무 Domain처럼 취급해 Runtime YAML로 `enabled=true`를 넣거나, EnvironmentPostProcessor로
같은 값을 주입하는 것은 모두 같은 오류다. 조립하는 Runtime마다 ADM의 내부 요구사항을 알아야 하는
구조가 되기 때문이다.

### 26.2 mandatory route가 opt-in Provider를 소비하면 Composition 계약을 먼저 의심한다

`CANONICAL_PRODUCT_REQUIREMENTS.csv`의 owner_scope/lifecycle과
`CPF_ADM_UI_FUNCTION_REQUIREMENTS.csv`의 route_id/canonical_ref로 mandatory 여부를 판정한다.
optional capability는 CSV에 명시적으로 표기된다(예: `CORE-MESSAGE`).

mandatory route가 요구하는 Port의 Provider가 opt-in이면 다음 두 가지를 하지 마라.

- Consumer(Controller)에 같은 속성 조건을 붙여 숨기는 것 — **Route Contract 축소**다.
- Runtime YAML/EPP로 임의 `enabled=true`를 넣는 것 — **ADM을 업무 Domain으로 취급**하는 것이다.

올바른 판정은 "Composition이 무조건 공급해야 하는가"이며, 그렇다면 capability owner의
AutoConfiguration을 `matchIfMissing = true`로 두어 **모듈을 Composition에 선언하는 행위 자체를
opt-in**으로 삼고, 속성은 끄기 위한 수단으로만 남긴다.

기본 제공으로 전환할 때는 그 AutoConfiguration이 만드는 Bean의 무자격 인프라 주입
(`DataSource`/`PlatformTransactionManager`/`Clock`)을 함께 canonical role로 해소해야 한다.
합성 Runtime에서는 후보가 여럿이라 켜는 즉시 다음 실패로 이동한다.

Validator: `cpf-tools/verification/tests/test_cpf_mandatory_route_provider_contract.py`

### 26.3 동일 의미 설정 키를 복제하지 않는다

같은 의미의 값을 여러 정본이 소유하게 두지 않는다. Canonical Config Owner를 먼저 확정하고
Consumer가 그것을 따르게 한다.

- Log Root의 Canonical Config Owner는 `cpf.logging.root`
  (`CpfApplicationLoggingProperties`, `@ConfigurationProperties("cpf.logging")`, 기본 `logs`)다.
- `cpf.logging.file.base-path`는 같은 네임스페이스에 얹혔지만 그 properties 클래스의 필드가 아닌
  중복 철자다. **절대경로를 강제**하는 `CpfLogPathPolicy`가 아직 이 키를 읽으므로 일괄 치환은
  파일 로그 경로 의미를 바꾼다. 은퇴는 그 의미 충돌을 해소한 뒤 별도로 수행한다.

### 26.4 Full Runtime을 결함 탐색기로 사용하지 않는다

Full Runtime 163단계를 작은 수정마다 반복하며 같은 Root Cause 계열을 한 겹씩 발견하는 방식은
금지한다. 실제로 RUN36~RUN40이 동일 계열(multi-bean 주입 모호성)을 다섯 번에 걸쳐 한 건씩
드러냈고, 매 사이클이 40분씩 소모됐다.

동일 계열 결함은 다음 순서로 **먼저** 닫는다.

```
repo-wide 후보 상한 추출
  -> 실제 결함/정상 분류
  -> 일괄 수정
  -> Validator
  -> Negative Mutation
  -> Targeted Test(compile -> unit/contract -> pytest tree -> 영향 Runtime 기동 -> health/transaction -> error path)
  -> Full Runtime
```

후보 추출은 조건(`@Conditional*`)이나 모듈 closure를 정교하게 모델링하려 하지 말고 **상한으로 넓게**
뽑은 뒤 분류한다. 조건 모델링을 먼저 넣으면 과소검출로 계열을 놓친다(실제로 Runtime closure를
모델링했다가 scheduler의 Clock 후보를 0건으로 오판했다).

### 26.5 Validator는 발견 사례만 통과시키는 부분 게이트로 만들지 않는다

Validator 작성 전에 Architecture Surface를 먼저 정의한다.

- constructor injection **과** `@Bean` method parameter
- direct dependency **와** transitive dependency
- 단일 Runtime **과** aggregate Runtime(1-WAS)
- Generated Domain **과** ADM/Gateway/Batch/Backoffice direct consumer

실제로 이 세 가지를 각각 놓쳐 같은 계열을 Full Runtime에서 다시 발견했다.

- 검사 타입을 목록으로 고정 → `Clock` 누락
- constructor만 검사 → `@Bean` parameter 누락
- Generated Domain만 검사 → batch/gateway direct consumer 누락

게이트 추가 시 "이 규칙이 놓칠 수 있는 형태"를 먼저 나열하고 각각을 negative mutation으로 확인한다.

### 26.6 Root Cause Family 전수 종결 기록 (2026-09-02)

| Family | 후보 | 실제 결함 | Validator |
| --- | --- | --- | --- |
| RCF-1 multi-bean 주입 모호성 | 86 | 0 (수정 완료) | `test_cpf_infrastructure_injection_resolvable.py` |
| RCF-2 mandatory route vs opt-in provider | 7 route | 3 (수정 완료) | `test_cpf_mandatory_route_provider_contract.py` |
| RCF-3 app-class 전용 등록 | boot app 15 | 1 (ADM, 수정 완료) | — batch는 역할별 독립 Context라 해당 없음 |
| RCF-4 default-on 전환 영향 | 3 모듈 | 3 (guard 추가) | `test_cpf_default_on_capability_scope.py` |

RCF-1 분류 근거: `ObjectMapper`는 공급자가 `CpfJackson2AutoConfiguration` 하나뿐이고,
batch/gateway는 `@Primary`(`batDataSource`/`batTransactionManager`/`batJdbcTemplate`)를 가지며,
1-WAS는 `CpfLocalRuntimePlatformDataSourcePrimary`가 `cpfPlatformDataSource`에 primary를 지정한다.
따라서 후보 86건 중 실제 해소 불가는 `Clock`/`PlatformTransactionManager` 계열뿐이었고 모두 닫았다.

### 26.7 VS Code Problems 0건은 절대 규칙이다

이 저장소의 IDE(Buildship/JDT) classpath는 각 project의 Gradle `build/classes/java/main` 과
`build/libs/*.jar` 를 직접 참조한다. JDT compiler output은 별도 `build/ide/classes`에 있어야 한다.
따라서 Gradle build 산출물을 지우는 순간 VS Code Problems 에
`code 964 missing required library` 가 수백 건 발생한다.

- **`gradlew clean` 을 실행하지 않는다.** 빌드 캐시 측정 같은 목적이라도 금지한다.
- `cleanup-cpf-generated-garbage.ps1` 처럼 산출물을 지우는 작업 뒤에는 반드시
  `cpfPrepareIdeClasspath` 로 복구한다. 이 task 는 compileJava 뿐 아니라 **jar 까지** 만든다.
- `cpfVerifyIdeClasspathReady` 는 compile output 과 **jar 산출물**을 함께 검사한다
  (`scope=all-java-projects+jar-artifacts`). compile output 만 보던 초판은 jar 이 없는 상태에서도
  PASS 를 냈고, 그래서 같은 오류가 반복해서 되살아났다.
- Gradle Java project 는 Gradle compiler output `build/classes/java/{sourceSet}`과 JDT compiler output을
  **공유하지 않는다**. 공식 Eclipse/Buildship model의 source output은 `build/ide/classes/{sourceSet}`,
  default output은 `build/ide/classes/default`로 고정한다. JDT 기본값인 `bin/main`, `bin/test`,
  `bin/default` 는 사용하지 않는다. CPF의 `bin/` 은 Batch launcher/config 등 추적 Product Source일 수 있어
  Java workspace clean이 이를 지우게 하면 즉시 FAIL이다. 또한 JDT와 Gradle이 `build/classes/java`를 공유하면
  비동기 JDT compile이 Gradle test의 classpath를 교체해 존재하는 class의 `NoClassDefFoundError`를 만들 수 있으므로
  즉시 FAIL이다. `cpfPrepareIdeClasspath`은 source-empty project를 포함해 isolated JDT output directory를
  materialize하고, `cpfVerifyIdeClasspathModel`/`cpfVerifyIdeClasspathReady`는 Gradle output, jar, Eclipse model,
  isolated JDT output을 모두 fail-closed로 검사한다. Isolation 전 JDT가 Gradle `classes/java/main`에 남긴 resource
  copy는 archive 직전에 **동일 hash인 `resources/main` 소유본일 때만** 제거한다. `DuplicatesStrategy.EXCLUDE`로
  다른 source의 중복을 숨기지 않으며 서로 다른 byte의 duplicate는 archive가 FAIL해야 한다.
- 현재 VS Code Gradle Build Server는 named-pipe 연결 실패 뒤 Eclipse model을 무시하고 기본 `bin/*`을
  강제하는 결함이 실측되었으므로 사용하지 않는다(`java.gradle.buildServer.enabled: off`). 이것은 compiler/JDT
  진단을 끄는 설정이 아니라 official Gradle Eclipse model을 실제로 반영하는 importer 선택이다. Build Server를
  다시 켜려면 fresh Java workspace에서 `bin/*` output 0, tracked Source 변화 0, Problems Error/Warning 0을
  자동 검증하는 회귀 증적을 먼저 추가해야 한다.

### 26.8 Runtime 검증기는 "대상 조립에 실제로 존재하는 거래"를 구동한다

Runtime smoke는 자기가 가리키는 Runtime의 **Composition**을 근거로 구동 대상을 정해야 한다.
어떤 Runtime에서 통했던 경로를 다른 Runtime의 Base URL에 그대로 붙이면, 업무 단정에 도달하기도
전에 "경로 없음/헤더 거절/500"으로 끝나고 원인은 검증기 밖에 있는 것처럼 보인다.

판정 순서는 다음과 같다.

1. 대상 Runtime의 Module Catalog를 먼저 읽는다. 1-WAS는 `CpfLocalRuntimeModules`가 정본이며
   core / common / gateway / admin / backoffice만 조립한다. Generated Domain과 EDU는 조립하지
   않는다(`cpf-tools/runtime/cpf-local-runtime/build.gradle`이 Root-owned optional project만
   조립한다고 명시한다).
2. 표준 거래 File Log는 `@CpfOnlineTransaction`(또는 `CpfDomainOperation.invoke`)에만 남는다
   (`LoggingAspect`). 일반 Controller 호출로는 `transactions/` 파일 자체가 생기지 않는다.
3. §26.1대로 **ADM은 Platform 기능이지 업무 Domain이 아니다.** 그래서 ADM에는
   `@CpfOnlineTransaction`이 없는 것이 정상이며, ADM 경로로 "표준 업무 거래 로그" 증적을
   만들려 하면 안 된다. ADM에 업무 거래를 새로 만들어 넣는 것은 §26.1 위반이다.
4. File Log 경로의 module segment는 앱 이름이 아니라 **실행 Runtime의 module code**다
   (`CpfLogPathPolicy.instanceRoot`). 1-WAS는 `<env>/local-runtime/<instance>/transactions/...`로
   남는다. 검증기가 `edu` 같은 다른 module을 뒤지면 candidate 0건으로 끝난다.

실제 사례: `LOCAL_FILE_LOG_STANDARD` / `LOCAL_INTEGRATED_LOG_CORRELATION`이
`http://127.0.0.1:8080`(1-WAS)에 EDU 경로 `/edu/online/member-processing`을 호출하고 있었다.
1-WAS에 EDU가 없으므로 항상 실패하는 잠복 결함이었고, 앞 단계가 먼저 실패해 오래 드러나지 않았다.

따라서 검증기는 구동 대상(경로 / operationId / 본문 / 추가 Header / File Log module)을
**호출자가 지정하는 파라미터**로 노출하고, Runtime별 호출 지점이 그 Runtime의 조립에 맞는 값을
넘긴다. §27의 조립성 원칙과 같은 이유다.

### 26.9 ADM Route도 External CPF protocol Header 계약을 그대로 요구한다

`Authorization`만 보내는 검증기 호출은 `X-Transaction-Id` / `X-Target-Operation-Id` 부재로
`ECPF900002` 400이 된다(`CpfHttpInboundContextAdapter.requireExternal`). health/liveness/readiness도
예외가 아니다. 이 때문에 "이미 떠 있는 1-WAS"를 인식하지 못하고 검증기가 자기 Runtime을 다시
기동하려다 `ADM boot jar was not found`로 끝난 사례가 있다.

공개로 선언한 경로는 **모든 계층에서 같은 범위로** 공개해야 한다. `AdmApiAuthFilter`가
GET/HEAD 3개 경로를 공개로 선언했는데 Security Chain이 그 앞에서 401로 잘라내면 그 선언은
실현되지 않는다. 한쪽 계층만 고치면 같은 증상이 다시 나온다.

### 26.10 Named parameter 이름은 Query Pack 정본이 소유한다

Repository가 record 필드 이름을 그대로 `addValue`에 쓰면 Query Pack의 `:param`과 조용히
어긋난다. 실제로 `auth-repository-insert-login-history-01`이 `:moduleId` / `:wasId`를 선언했는데
`BackofficeAuthRepository`가 `systemCode` / `application`으로 넘겨,
`No value supplied for the SQL parameter 'moduleId'`로 **MBW 로그인 실패가 401이 아니라
500(ECPF990000)** 으로 나갔다. 정상 경로에서는 호출되지 않는 실패 기록 경로라 늦게 발견됐다.

정본은 `cpf-tools/db/metadata/platform-runtime-query-contract.json`의 `parameters`와
`cpf-tools/db/runtime-template/**`의 `:param`이다. Java는 그 이름을 따른다.

## 27. 사용자 대면 Shell/Command 사용성 계약 (Mandatory)

사용자 Steering(2026-09-03): "1-WAS 구동 Shell을 포함해 개발자·운영자·사용자가 직접 쓰는 모든
Shell/명령은 조립형·가독성·수정 용이성을 기준으로 정리한다."

동작 여부만 보는 검수는 이 계약을 만족하지 못한다. 사용성·가독성·유지보수성·수정 편의성까지 본다.

### 27.1 실행 항목은 한 줄 주석으로 넣고 뺄 수 있어야 한다

Domain / Module / 기능 단위 실행 항목은 **한 줄을 주석 처리하는 것만으로** 포함·제외할 수 있어야
한다. 여러 곳을 동시에 고쳐야 하나를 빼는 구조는 이 계약 위반이다.

### 27.2 긴 단일 명령에 모든 기능을 묶지 않는다

의미 있는 실행 단계와 옵션을 분리해 **어디를 고쳐야 하는지 즉시 보이는 구조**로 만든다.
한 줄에 전체 기동 명령을 이어 붙이면 수정 지점을 찾을 수 없다.

- 사용자 진입 Shell의 한 줄은 200자를 넘지 않는다.
- 한 줄에 `;` 로 4개 이상의 문장을 잇지 않는다.
- 게이트: `cpf-tools/verification/tests/test_cpf_user_facing_shell_usability.py`

### 27.3 자주 고치는 값은 한 곳에 모은다

주요 변수, Domain 목록, Profile, Port, DB, 실행 대상처럼 자주 바뀌는 값은 파일 상단 또는
명확히 구분된 설정 영역에 모은다.

### 27.4 한글 주석으로 켜고 끄는 의미를 설명한다

각 설정과 실행 단계에는 짧고 명확한 한글 주석을 단다. 무엇을 켜고 끄는지, 고치면 무엇이
달라지는지 읽는 사람이 바로 알 수 있어야 한다.

단, Windows `cmd.exe`가 파싱하는 `.bat`는 UTF-8 한글 `rem` 주석의 일부를 명령으로 오인할 수
있으므로 **실행 파일 본문과 주석을 ASCII-safe로 유지**한다. 같은 한글 설명은 README 또는
PowerShell launcher에 둔다. code page 변경으로 이 예외를 우회하지 않으며, public `.bat`의
non-ASCII 재유입은 regression test에서 FAIL해야 한다.

Windows wrapper의 기본 출력은 ambient `DEBUG` 같은 Host 변수에 따라 달라지면 안 된다.
명령 trace가 필요할 때만 CPF 소유 `CPF_GRADLE_DEBUG=1`을 명시적으로 설정한다. 기본 실행에서
내부 `set`/`for` 명령이 노출되면 사용자 실행 가독성 결함으로 FAIL이다.

### 27.5 기본값만으로 동작해야 한다

선택 기능을 넣고 빼도 전체 스크립트를 다시 쓰지 않아야 한다. 기본값 실행은 언제나 정상 동작한다.

### 27.6 적용 범위

1-WAS 구동 Shell뿐 아니라 Build / Test / Start / Stop / Reset / DB / Generator / Runtime /
Validation / Open Git 관련 사용자 진입 Shell과 명령 전체에 동일 기준을 적용한다.

## 28. 관측·보안 정책은 운영자가 구성한다 (Mandatory)

사용자 Steering(2026-09-03): "마스킹은 임의로 하지 마라. 마스킹 항목은 ADM 사용자 설정에
연결해 DB든 파일이든 사용자가 항목을 선택하도록 한다." / "로그 항목도 정해진 것만 남기지 말고
사용자가 ADM에서 추가·삭제·수정할 수 있게 한다."

### 28.1 마스킹 대상은 코드에 고정하지 않는다

민감정보 마스킹은 **운영자가 ADM에서 선택한 항목**에만 적용한다. 값 패턴 규칙(주민번호, 전화번호,
이메일, 장문 숫자 식별자, JWT, 개인키, Bearer 토큰 등)을 코드에 하드코딩해 무조건 적용하지 않는다.
적용 범위는 DB 로그와 File 로그가 동일해야 한다.

**실제 증상 근거**: `CpfMaskingRuntime.LONG_ACCOUNT_PATTERN`(10~19자리 숫자)이 항상 켜져 있어,
정본 CPF 거래ID `20260903001256762BATS1JCXLU0000001` 의 앞 17자리 timestamp가 계좌번호로 오인되어
`***6762BATS1JCXLU0000001` 로 훼손됐다. 그 결과 File/DB/ADM 통합 로그를 잇는 **상관관계 키 자체가
사라져** Batch→Domain 응답유실 검증의 lineage 대조가 실패했다. 같은 값이 파일명·DB·ADM에는 원문으로
남으므로 본문만 가리는 것은 보호 효과가 없고 계약만 깨뜨린다.

### 28.2 CPF가 발급한 추적 식별자는 마스킹 대상이 아니다

`transactionId` / `traceId` / `segmentId` 처럼 CPF가 스스로 발급한 상관관계 식별자는 사용자
민감정보가 아니다. 운영자 선택과 무관하게 마스킹하지 않는다. 정본 규격은
`com.cpf.core.api.transaction.CpfTransactionIds` 가 소유한다.

### 28.3 로그 항목은 운영자가 추가·삭제·수정한다

남길 로그 항목(필드)을 코드에 고정하지 않는다. ADM에서 항목을 추가·삭제·수정할 수 있어야 하며,
DB 로그와 File 로그에 같은 구성이 적용되어야 한다.

### 28.4 ADM Route는 필수 기능이다

위 두 설정은 CPF가 제공하는 Framework/Platform 관리 기능이므로 ADM capability registry에 등록된
**mandatory Route**로 제공한다. §26.2에 따라 조건부 Provider로 축소하지 않는다.

## 29. Open Git Release 사용자 Task 계약 (Mandatory)

사용자 Steering(2026-09-03): "VS Code Gradle Projects 에서 개발자가 Open Git Release 생성·검증·
Commit·Push 절차를 쉽게 찾고 실행할 수 있도록 `70. cpf 오픈깃 릴리즈` 사용자 Task 그룹을 신설한다."

Open Git Release 는 최종 사용자가 직접 수행하는 공식 lifecycle 이다. `90~99 내부` 영역으로 숨기지
않고 §27 의 사용자 Task 체계(`00 시작` … `60 배포`)와 같은 원칙으로 `70` 에 배치한다.

### 29.1 책임 경계를 Task 로 분리한다

| Task | 책임 | Git Write |
| --- | --- | --- |
| `cpfOpenGitBuild` | 공개 Release 산출물 생성 | 없음 |
| `cpfOpenGitVerify` | 공개 Release 검증(Binary/Sources/Javadoc/POM/SBOM/Checksum, Public API/CLI/Generator/Generated Sample, Leakage 0, Fresh Consumer) | 없음 |
| `cpfOpenGitPrepare` | Build → Verify 순차 | **없음** |
| `cpfOpenGitCommit` | 검증 완료 Working Tree Commit | 있음 |
| `cpfOpenGitPush` | 검증 완료 Commit 을 remote 로 Push | 있음 |
| `cpfOpenGitCommitAndPush` | Commit → Push 순차 | 있음 |

### 29.2 Gradle 에 Release 로직을 복제하지 않는다

Gradle Task 는 정본 Open Git 진입점(`cpf-tools/release/open-git/cpf_open_git.py`)을 호출하는
**wrapper** 여야 한다. CLI / PowerShell / Gradle Task 가 서로 다른 구현을 가지면 안 된다.

```
VS Code Gradle Task → cpf-open-git.ps1 → cpf_open_git.py → 동일 Release/Verification lifecycle
```

### 29.3 Git Write 는 fail-closed 다

Commit/Push 전에 다음을 모두 확인하고, 하나라도 실패하면 **중단**한다. 중간 실패를 전체 PASS 로
처리하지 않는다.

- 대상이 Development Master 가 아니라 Open Git 작업 Repository 인가
- Open Git remote 가 정본 Repository 와 일치하는가
- 현재 branch 가 허용 대상인가
- Source / Release identity 확인
- Release Build PASS / Open Git Verification PASS / Leakage = 0
- Commit 대상 변경 파일 표시, Push 전 current SHA / branch / remote 표시

또한 명시적 Git Write 승인값 `-PconfirmGitWrite=true` 가 없으면 FAIL 한다.

### 29.4 자동 Git Write 를 금지한다

Build / Verify / Prepare / 일반 Build·Test·Release Task / VS Code Import / IDE Sync 실행만으로
Commit·Push 가 수행되어서는 안 된다. `cpfOpenGitPrepare` 도 **생성 + 검증까지만** 한다.

### 29.5 검증은 Task 존재 확인으로 끝내지 않는다

`Gradle Task 노출 → canonical script 연결 → Release 생성 → Verify → Git Write 보호 →
잘못된 Repository/remote/branch/승인 누락 Negative Test → 정상 Commit/Push 진입 조건` 까지 확인한다.
실제 Commit/Push 가 사용자 승인 없이 필요한 경우에는 **Git Write 를 수행하지 말고 직전
fail-closed 경계까지** 검증한다.

게이트: `cpf-tools/verification/tests/test_cpf_open_git_task_contract.py`

## 30. System Identity / Channel Identity / instanceId 계약 (Mandatory)

사용자 Steering(2026-09-04)으로 확정한 정본이다. 이 장이 SystemCode·ChannelCode·instanceId·
Same-JVM topology에 대한 유일한 정본이며, Source 주석이나 개별 Runtime 설정이 이 장과
어긋나면 **Source가 아니라 이 장이 우선**이다.

### 30.1 SystemCode는 논리 업무 Domain(또는 정본이 System Identity를 부여한 Runtime)의 식별자다

정본 예: Member `MBR`, External `EXS`, Backoffice Business Domain `MBW`, 일반 Batch `BAT`,
Center-Cut `CEC`.

- SystemCode에는 **임의 Default/Fallback을 두지 않는다.** `LOCAL` / `DEFAULT` / `CPF` /
  `UNKNOWN` 으로 누락된 System Identity를 자동 보정하지 않는다.
- Source / Generator / Runtime Configuration에서 정본 Identity가 명확히 결정되지 않으면
  **fail-closed** 한다. 결정 소유자는 `CpfRuntimeSystemCode.resolve` 이며 이미 값이 없으면
  던진다 — 그 계약을 우회하는 기본값을 Runtime YAML에 두지 않는다.
- 3자리 issuer가 필요할 때 **등록되지 않은 코드를 앞 3자리로 자르거나 `X`로 채워 만들지
  않는다.** 축약은 서로 다른 System을 같은 issuer로 뭉개는 암묵적 fallback이다.

### 30.2 SystemCode와 ChannelCode는 역할이 다르지만, 내부 Domain 거래에서는 같은 canonical 값을 쓴다

역할이 같다는 뜻이 **아니다**. 다만 Business Domain 사이의 내부 거래 Hop에서는 그 Domain의
canonical SystemCode 값을 그대로 ChannelCode 값으로 사용한다.

```text
MBR Domain → EXS Domain
  호출 Domain SystemCode = MBR      호출 ChannelCode = MBR
  대상 Domain SystemCode = EXS      대상 ChannelCode = EXS
```

- 별도의 System→Channel mapping 표나 중복 설정을 만들지 않는다.
- 금지 대상은 의미를 바꾸는 **임의 변환**이다(`MBR → MEMBER`, `EXS → EXTERNAL` 등).
- `systemCode=MBR` Domain이 내부 거래 Channel에서 `MBR`을 쓰는 것은 허용이 아니라 **canonical
  동작**이다. "System과 Channel은 별도 개념이므로 alias로 쓰지 않는다"를 *같은 값을 쓰면 안 된다*로
  읽지 않는다.

### 30.3 Front Channel은 자기 정본 ChannelCode를 소유한다

- Frontend / Channel Application에서 시작한 거래는 그 Channel의 정본 ChannelCode를 Header
  Channel 값으로 설정한다. Front Channel을 업무 Domain SystemCode로 임의 변환하지 않는다.
- Browser는 Protected Header를 스스로 작성하지 않는다. 신뢰 Channel/BFF 경계가 정본 Channel
  Identity를 설정한다(`CanonicalHeaderOwnershipFilter`).
- lineage 규칙:

```text
최초 Channel  = Front Channel의 정본 ChannelCode
이후 내부 Hop = 각 Business Domain의 canonical SystemCode 값을 ChannelCode로 사용
```

Original / Current / Caller / Target Channel이 이 규칙으로 일관되게 유지되어야 한다.

### 30.4 Gateway는 자체 Business SystemCode를 가지지 않는다

Gateway는 Edge/Trust/Route 중계 Platform Component다.

- `GWY` 같은 가상 SystemCode를 만들어 **거래 당사자로 삽입하지 않는다.**
- Gateway가 가질 수 있는 것은 자기 Runtime/운영 Identity뿐이다: `instanceId`, `routeId`,
  `routeVersion`, ingress type.
- Gateway를 통과해도 거래의 원래 Channel/Domain lineage가 보존되어야 한다. 경유/Direct 결과가
  달라지면 결함이다.

### 30.5 1-WAS는 System이 아니라 Same-JVM topology다

- `systemCode = LOCAL` 같은 가상 Identity를 만들지 않는다.
- 같은 JVM 안에서도 각 Business Domain의 canonical SystemCode/ChannelCode를 그대로 유지한다.

```text
instanceId = was01 (하나의 실행 Instance)
  같은 JVM 안에서 MBR → MBR, EXS → EXS, MBW → MBW Identity 유지
```

Same-JVM이라는 이유로 모든 거래 Identity를 하나로 덮어쓰지 않는다. 따라서 Operation Catalog와
수신자 검증은 **Process의 systemCode가 아니라 그 거래를 소유한 Domain**을 기준으로 해야 한다.

### 30.6 instanceId는 System/Channel과 완전히 분리된다

```text
명시 instanceId → 없으면 실제 Hostname
```

`was01`, `server-a`, hostname 등은 실행 Process/WAS Instance 식별자다. 거래 Channel/System
Identity로 사용하지 않는다. instanceId의 Hostname fallback이 허용된다는 사실을 SystemCode의
fallback 허용 근거로 쓰지 않는다.

### 30.7 TransactionId issuer의 source는 최초 신뢰 거래 기동점의 canonical ChannelCode다

사용자 확정(2026-09-04). issuer를 위한 **새 Identity 축을 만들지 않는다.** "발급 주체 기술
metadata Identity" 같은 별도 namespace도 만들지 않는다.

```text
Front Channel 이 최초 기동      -> 그 Channel 의 canonical ChannelCode
내부 Business/Generated Domain 이 최초 기동
                               -> 그 Domain 의 SystemCode 값(= 내부 hop ChannelCode 값)
ADM 운영 거래                   -> ADM 운영 ChannelCode (ADM SystemCode 를 만들지 않는다)
```

금지 사항:

- `issuer == SystemCode` 또는 `issuer == X-Original-System-Code` 를 **universal 계약으로 강제**하는 것
- issuer를 만들기 위한 substring / truncate (`LOCAL → LOC` 등)
- blank/unknown에 대한 fallback(`CPF` 등)

TransactionId issuer 규격이 3자리라면, **거래를 기동할 수 있는 canonical ChannelCode 자체가 그
규격을 만족하도록 Product Contract에서 검증**한다. issuer 전용 Identity Namespace를 신설하지 않는다.

기존 `DefaultCpfTransactionIdGenerator` 의 `issuer = normalize(systemCode)` 계열,
`CpfSystemCodes.requireIssuerCode`, smoke 의 `Get-CpfIssuerCode` 는 이 전제에 서 있으므로 제거·재정렬
대상이다. Generator → Inbound Validation → Log/Trace → Test 까지 함께 닫는다.

### 30.8 이 장에 어긋나는 것을 발견하면 Harness부터 고친 뒤 Consumer를 일괄 정렬한다

Source만 예외 처리하지 않는다. 순서는 다음과 같다.

```text
Product Contract → Harness Rule → Validator → Negative Mutation → Current Registry → Test/Runtime
```

### 30.9 재발방지 Gate(필수)

- SystemCode 누락 → fail-closed
- SystemCode default/fallback 존재 → FAIL
- 1-WAS `LOCAL` SystemCode → FAIL
- Gateway `GWY` Business SystemCode → FAIL
- 임의 System→Channel mapping → FAIL
- 내부 Domain `MBR → EXS` Same-JVM/Remote Channel lineage parity
- Front Channel → MBR → EXS 거래 lineage
- Gateway 경유/Direct parity
- BAT/BAT, CEC/CEC
- instanceId 독립성
- TransactionId / Log / Trace / DB Timeline 동일성

검증기를 현재 잘못된 Source 동작에 맞춰 완화하지 않는다.

### 30.10 ADM은 Platform Control Plane이며 Business SystemCode를 가지지 않는다

사용자 Steering(2026-09-04) 확정. `cpf-admin`은 Runtime/Health, Trace/Log/Observability, Config,
Incident/Recovery, Deployment, Batch·Center-Cut·Agent 운영, Gateway/Route 운영,
Security/Session/Permission, 운영 Audit/Approval의 **Owner**다. 고객 업무 Domain이 아니며 업무
Transaction Owner도 아니다. `MBR` / `EXS` / `MBW` 와 같은 System으로 취급하지 않는다.

금지 사항 — `ADM` 을 다음 용도로 쓰지 않는다.

- ADM 자신의 systemCode (`system-code: ${CPF_SYSTEM_CODE:ADM}` 포함)
- Business transaction issuer용 systemCode
- `OPS_SYSTEM_REGISTRY` 의 Business/Logical System identity
- System6 검증을 통과시키기 위한 가상 System identity
- Runtime Registry의 systemCode 필수조건을 만족시키기 위한 placeholder

SystemCode가 없다는 이유로 `CPF` / `LOCAL` / `DEFAULT` / `UNKNOWN` 을 대신 넣는 것도 금지한다.

### 30.11 ADM SystemCode 부재는 ADM Channel/Module/Service Identity 삭제를 뜻하지 않는다

정본 조사 결과(`cpf-tools/db/vendor/*/source/50_framework_seed_data.sql`):

```text
OPS_CHANNEL_REGISTRY : WEB(CLIENT/EXTERNAL), MOBILE(CLIENT/EXTERNAL),
                       ADM(OPERATOR/INTERNAL, "관리자"), BATCH(SYSTEM/INTERNAL)
```

즉 **ADM 운영 ChannelCode `ADM` 은 이미 정본에 존재한다.** 운영자가 시작하는 거래의 Channel
lineage는 이 정본 ChannelCode를 쓰며, 이를 ADM SystemCode로 변환하지 않는다.

```text
ADM SystemCode       = 없음
ADM 운영 ChannelCode = ADM (OPS_CHANNEL_REGISTRY 정본)
ADM Module/Service   = OPS_SERVICE / Routing / Health Registry의 Service·Module Code (유지)
ADM instanceId       = 실행 Instance 식별자 (유지, §30.6)
```

Module / Service / Runtime / Channel / System Identity는 **각각 분리해서** 다룬다. SystemCode를
제거한다는 이유로 Service·Module·Channel 행을 함께 지우지 않는다.

### 30.12 ADM이 관리 대상의 systemCode를 소비하는 것은 정상이다

System Registry 조회, Runtime 상태 조회, 거래 검색, Operation Policy, Caller/Target System
Policy, Log/Trace Timeline 등에서 `systemCode` 필드가 나타나는 것은 **관리 대상 System의
Identity** 이기 때문이다. 이를 근거로 ADM 자신에게 `ADM` SystemCode를 부여하지 않는다.
반대로, 관리 대상의 `systemCode` 필드를 ADM SystemCode 제거와 함께 지우는 것도 결함이다.

### 30.13 Runtime Identity 계약은 Architecture Role별로 구분한다

`{systemCode, instanceId}` 를 모든 Runtime의 보편 Instance Identity로 강제하면 ADM/Gateway처럼
SystemCode가 없는 Component에 가짜 SystemCode가 필요해진다. 따라서 **`{systemCode, instanceId}`
규칙은 SystemCode를 가지는 Runtime에만 적용**한다(최종 통합 Steering §11).

Role별 SystemCode 보유 여부는 §30.16 표가 유일한 정본이다. Runtime Identity는 다음과 같이 읽는다.

| Architecture Role | Runtime Identity |
| --- | --- |
| `BUSINESS_DOMAIN` / `REFERENCE_RUNTIME` / `BATCH_RUNTIME` | systemCode + instanceId |
| `PLATFORM_CONTROL_PLANE` | module/application + runtimeRole + instanceId |
| `GATEWAY` | instanceId + routeId/routeVersion/ingress |
| `CHANNEL_FRONT` | 정본 ChannelCode + instanceId |
| `TOPOLOGY`(1-WAS) | instanceId. 내부 Runtime 각자의 Identity를 그대로 보존한다 |

Platform Runtime은 이미 존재하는 canonical identity로 식별하며 **새 Platform Identity 축을 만들지
않는다**. Same-JVM에 SystemCode를 가진 Runtime이 여럿 있으면 동일 `instanceId` 를 공유하면서 각자의
logical System Runtime identity를 유지한다.

```text
MBR + instanceId=was01
EXS + instanceId=was01
MBW + instanceId=was01
EDU + instanceId=was01
```

1-WAS 자체를 `LOCAL` System Registry Row로 등록하지 않는다.

### 30.14 ADM Header/Context 계약

ADM 관리 API나 ADM이 다른 Component를 제어하는 호출을 Runtime Test 통과 목적으로
`X-System-Code=ADM` 으로 만들지 않는다. SystemCode가 없는 Platform Component의 Header/Context
처리 방식은 Product Contract에서 명시적으로 정의한다. 정본이 모호하면
`Harness/Product Contract 명확화 → Header/Context Source → Consumer → Runtime Test` 순으로 닫는다.
Verifier/Smoke에 `ADM` SystemCode를 하드코딩해 통과시키는 것은 금지한다.

### 30.15 ADM 재발방지 Gate(필수)

- `cpf-admin` 에 `CPF_SYSTEM_CODE` 또는 ADM 자기 `system-code` 재도입 → FAIL
- `ADM` 을 자기 System identity로 `OPS_SYSTEM_REGISTRY` 재등록 → FAIL
- ADM SystemCode fallback/placeholder → FAIL
- ADM 관리 대상의 `systemCode` 필드를 잘못 제거 → FAIL
- ADM ChannelCode를 SystemCode와 함께 삭제/통합 → FAIL
- Service/Module/instanceId를 SystemCode로 오인 → FAIL
- Runtime/Smoke가 `X-System-Code=ADM` 을 하드코딩해 False Green → FAIL

수정 후 검증 범위는 Compile이 아니라
`ADM Startup → Login/Session/CSRF → Control Plane API → 대상 System 조회/제어 → Log/Trace/Audit
→ Runtime Registry → Browser E2E → DB3 → One-WAS/분리 WAS` 까지다.

ADM Server Session BFF의 Login 검증은 session-id 회전 뒤 response body가 기록되기 **전에** 새
`XSRF-TOKEN` cookie가 발급되는지와, Browser가 최신 cookie 값으로 보낸 다음 state-changing 요청만
통과하는지를 실제 Runtime에서 확인한다. 이전 token의 403은 보안 거절 원인을 판별할 수 있어야 한다.
response 완료 뒤 filter에서 token을 교체하거나, stale cookie를 다시 보내는 Smoke는 False Green으로
FAIL 처리한다.

### 30.16 Module Architecture Role 정본과 계약 경계 판정

사용자 확정(2026-09-04, 최종 통합 Steering §23~§24). Architecture Role은 **새 Identity 축이 아니라
기존 Module의 분류 metadata**다. 정본 위치는 `cpf-tools/governance/cpf-product-surface-policy.json`
의 `moduleOwners[].architectureRole` 이며 정의는 같은 파일의 `architectureRoles` 가 소유한다.
중복 정본을 새로 만들지 않는다. Role을 필요 이상으로 늘리지 않는다.

| Role | 의미 | SystemCode |
| --- | --- | --- |
| `BUSINESS_DOMAIN` | 고객 업무 Domain (MBW / MBR / EXS / 모든 Generated Domain) | 보유 |
| `REFERENCE_RUNTIME` | CPF 공식 Education/Reference Runtime (EDU) | 보유 |
| `BATCH_RUNTIME` | Batch 실행 Runtime (BAT / CEC) | 보유 |
| `PLATFORM_CONTROL_PLANE` | CPF 운영 Control Plane (ADM) | **없음** |
| `GATEWAY` | Edge/Trust/Route 중계 | **없음** |
| `CHANNEL_FRONT` | Front/외부 Channel Application | **없음**(정본 ChannelCode 보유) |
| `FRAMEWORK_INTERNAL` | Framework/Starter/도구/거버넌스 자산 | **없음** |
| `TOPOLOGY` | Same-JVM 배치 topology(1-WAS) | **없음** |

계약 경계는 다음 한 가지 경로로만 판정한다.

```text
Operation → canonical Owner Component/Domain → architectureRole → 적용 Transaction Contract
```

- `BUSINESS_DOMAIN` / `REFERENCE_RUNTIME` / `BATCH_RUNTIME` 소유 Operation → **Business 거래 계약**.
- `PLATFORM_CONTROL_PLANE` / `GATEWAY` 소유 Operation(관리 API) → Business System 계약을 강제하지
  않는다. 정본 ChannelCode 기반 Channel 계약을 적용한다.
- 금지: URL path 판정, Java package 추론, `OPS_SYSTEM_REGISTRY` 등록 여부 판정, 산문 `owner_scope`
  판정, `@CpfOnlineTransaction` 에 Role 선언. Operation Catalog 등록 사실만으로 Business Domain 이라고
  해석하지 않는다.
- prefix 판정은 **가장 긴 prefix 우선**이다(`cpf-tools/runtime/cpf-local-runtime/` 가
  `cpf-tools/` 보다 우선).

#### 30.16.1 System 을 키로 하는 계약은 SystemCode 보유 Runtime 에만 적용한다

`X-System-Code` / `X-Target-System-Code` 대조, Operation Access Policy, Domain Operation 소유 대조,
Operation Catalog 등록처럼 **target System 을 키로 삼는 계약**은 **요청 Operation의 canonical
Owner**가 SystemCode 보유 Role(`BUSINESS_DOMAIN` / `REFERENCE_RUNTIME` / `BATCH_RUNTIME`)일 때
적용한다. 단일 Runtime에서는 Runtime SystemCode와 Owner가 같아야 하지만, Same-JVM 1-WAS에서는
Topology 자신이 SystemCode가 없어도 `MBR`/`EXS`/`MBW`/`EDU` Operation의 Owner SystemCode로 반드시
검증·등록한다.

SystemCode 가 없는 Component(ADM Platform Control Plane / Gateway / Channel Front /
Batch Control Plane / 1-WAS topology **자체**)의 관리 Operation에는 그 계약을 적용하지 않는다.
이는 1-WAS 안에 조립된 Business Domain Operation의 검증을 생략한다는 뜻이 아니다. 값이 없다는
이유로 가상 SystemCode 를 만들거나 `NullPointerException` 으로 500 을 내지 않는다. 관리 Component의
접근통제·lineage 는 자기 계층이 소유한다(예: ADM 은 세션/권한 Filter, Channel Front 는 정본
ChannelCode).

1-WAS의 Business Operation Owner는 `META-INF/cpf/generated-domain.properties`(Generated Domain) 또는
`META-INF/cpf/runtime-component.properties`(Product Runtime)의 **명시 descriptor**가 소유한다. Descriptor의
`systemCode` / `domainCode` / `scanPackage`가 정본이고, Runtime은 handler package를 이 명시 목록에서
선택하는 데만 쓴다. Operation ID, URL, Module ID, DB Prefix, Application 이름에서 SystemCode를 새로
추론하는 것은 금지다. 같은 handler에 더 긴 `scanPackage` descriptor가 있으면 그것이 우선이고, 같은
길이의 서로 다른 Owner는 fail-closed 한다.

새로 System 을 키로 하는 계약을 추가할 때는 **먼저 이 범위 규칙을 적용**한다. 같은 결함이
`CpfHttpInboundContextAdapter` / `CpfControllerContextInterceptor` / `CpfDomainOperationAccessGuard` /
`CpfOperationCatalogBootstrap` 에서 반복 발생했다.

### 30.17 EDU는 Education/Reference Runtime의 canonical SystemCode다

사용자 확정(2026-09-04).

- `cpf-education` 은 고객 Business Domain이 **아니다**. 그러나 CPF가 Online 20 + Batch 15의 실제
  거래/실행 예제를 제공·검증하는 **공식 Education/Reference Runtime** 이며 그 Runtime Identity가
  `EDU` 다.
- `EDU` SystemCode 유지, `OPS_SYSTEM_REGISTRY` 의 `EDU` 유지, Transaction/Header/Runtime
  Registry/Log/Trace에서 `EDU` Identity를 정상 사용한다.
- 1-WAS에 EDU가 조립되어도 `LOCAL` 이 아니라 **EDU 자신의 Identity** 를 유지한다.
- "Reference Runtime이므로 SystemCode가 없다" 거나 "Sample이므로 Registry에서 제외한다" 는 방향으로
  바꾸지 않는다. EDU를 Sample-only 또는 System Identity 미확정으로 설명하는 stale 문서/Test/Fixture는
  Current-only 원칙으로 교정·제거한다.

### 30.18 OPS_SYSTEM_REGISTRY 구성 정본

사용자 확정(2026-09-04, 최종 통합 Steering §21). `OPS_` 는 Operations DB의 물리 Prefix이며
Registry에는 **실제 canonical SystemCode만** 저장한다.

- 제거: `CPF`, `CMN`, `ADM`, `GWY`, `LOCAL` — System Identity가 아니다.
- 유지: `MBW`, `EDU`, `BAT`.
- 추가: `CEC`(Center-Cut). 누락되어 있다.
- Generated Domain: `MBR` / `EXS` 를 하드코딩하지 않는다. **Generator/Domain lifecycle이 신규
  Generated Domain의 canonical SystemCode를 Registry lifecycle에 연결**해야 한다.

Seed 한 줄만 고치지 않는다. 정본은 `cpf-tools/db/canonical/**` 이며 벤더 파일은 파생물이다
(`DO NOT EDIT generated seed directly`). §38 대로
`Canonical Schema → Oracle/PostgreSQL/MariaDB → Migration → Seed → Upgrade → Rollback/Recovery →
Runtime Query → Test/Evidence` 를 하나의 변경 단위로 닫는다. `CPF`/`CMN`/`ADM` 의 Registry 존재를
PASS 조건으로 고정한 Test/Fixture는 **False Green** 이므로 함께 교정한다.

### 30.19 DB Physical Prefix와 System Identity는 다른 Namespace다

사용자 확정(2026-09-04, 최종 통합 Steering §4). 다음은 DB Schema/Table/Object의 **물리
Namespace**이며 SystemCode가 아니다.

```text
CPF_*   CMN_*   ADM_*   GWY_*   OPS_*   BAT_*
```

- `ADM_*` 테이블 존재 ≠ `ADM` SystemCode 존재
- `GWY_*` 테이블 존재 ≠ `GWY` SystemCode 존재
- `BAT_*` DB Prefix 와 `BAT` SystemCode 는 **문자열만 같고 Namespace가 다르다**
- Center-Cut은 `SystemCode = CEC` 이면서 Batch 원장을 소비하므로 `BAT_*` DB를 쓸 수 있다

> 문자열 동일 ≠ Identity 동일

DB Prefix / Module 이름 / Package 이름에서 SystemCode를 **추론하지 않는다**.

### 30.20 SystemCode는 정본의 canonical 고정값이며 Runtime이 선택하지 않는다

사용자 확정(2026-09-04). SystemCode는 Runtime이 고르는 설정값이 아니라 **정본에서 결정되는 logical
identity** 다.

- **모든 Generated Business Domain은 생성 시점부터 canonical SystemCode를 필수로 가진다.** Source of
  Truth는 Generator input 의 `domain.systemCode` 이며, 생성물에는 **literal canonical value** 로
  기록한다.
- Product Runtime(EDU / MBW / BAT / CEC)의 canonical Identity는 Product Contract가 소유하며 역시
  고정값으로 기록한다.
- `${CPF_SYSTEM_CODE:XXX}` (default 포함)도, `${CPF_SYSTEM_CODE}` (Runtime 선택)도 **쓰지 않는다.**
- 외부 override가 존재한다면 정본 Identity를 **바꾸는 용도가 아니라 정본값과의 일치 검증용**으로만
  허용하고, 불일치하면 **fail-closed** 한다.
- SystemCode를 결정할 수 없으면 Generator/Runtime 모두 fail-closed 한다.

#### 30.20.1 SystemCode source 경로에 Module/Prefix/Application 이름을 쓰지 않는다

`CpfRuntimeSystemCode.resolve` 가 `cpf.framework.module-id` 를 SystemCode source로 사용하는 현재
구조는 **Namespace 위반**이다(§30.19). 다음을 SystemCode fallback으로 쓰지 않는다.

```text
Module ID      DB Prefix      Application 이름      Package 이름      Profile 이름
```

Role별 동작은 다음과 같다.

- SystemCode를 가지는 Role(`BUSINESS_DOMAIN` / `REFERENCE_RUNTIME` / `BATCH_RUNTIME`) →
  canonical source가 없으면 **FAIL**.
- SystemCode가 없는 Role(`PLATFORM_CONTROL_PLANE` / `GATEWAY` / `CHANNEL_FRONT` /
  `BATCH_CONTROL_PLANE` / `FRAMEWORK_INTERNAL` / `TOPOLOGY`) → **systemCode 없음을 정상 지원**한다.
  없다는 이유로 가상 값을 만들지 않는다.

### 30.21 CpfSystemCodes의 Namespace 혼합은 해체한다

사용자 확정(2026-09-04, 최종 통합 Steering §6). 현재 `CpfSystemCodes` 는 Module Name / DB Prefix /
Component Code / Business SystemCode / TransactionId issuer / Logging moduleId 책임을 한 곳에서
처리한다. 이는 잘못된 Namespace 혼합이며 해체 대상이다.

제거할 동작:

```text
core → CPF        common → CMN      admin → ADM
gateway → GWY     reference → REF
unknown → 앞 3자리 truncate          blank → CPF fallback
inferFromTypeName(package/class → SystemCode)
```

Business SystemCode는 **정본에서 읽을 뿐 추론하지 않는다.**


### 30.22 Identity Anti-pattern 과 Negative Rule

이 절은 세션 이력이 아니라 **앞으로도 적용되는 현재 규칙**이다. 아래 대응은 모두 금지이며 Validator가
차단한다. 이력·경위는 Evidence/Registry/Handover가 소유한다.

| # | 증상 | 금지된 대응 | 왜 잘못인가 | canonical 판정 | 재발방지 Gate |
| --- | --- | --- | --- | --- | --- |
| A-1 | 수신 Runtime과 Header의 System Code가 불일치 | Runtime 이름을 앞 3자리로 잘라 issuer/Code 생성 | 축약은 서로 다른 System을 같은 값으로 뭉개는 암묵적 fallback이다 | issuer의 source는 최초 신뢰 기동점의 canonical ChannelCode(§30.7) | `test_cpf_system_identity_contract` |
| A-2 | 검증기가 필수 Header 부재로 거절 | 검증기에 System Code 값을 하드코딩 | 잘못된 모델을 통과시키는 False Green | 해당 Component의 architectureRole이 정한 계약을 적용(§30.16) | `test_cpf_system_identity_contract` |
| A-3 | Bean/자동설정이 없어 기동 실패 | Framework가 그 Bean을 직접 소유 | 상위 프레임워크가 **모듈을 분리**한 것을 "기능 부재"로 오인 | 의존성 조립 누락은 Starter 선언으로 닫는다 | 기동 계약 Test |
| A-4 | 정본 검증기가 값을 거부 | 검증기 규칙을 완화 | 정본이 아니라 검증기가 outlier일 수 있다 | 정본 대조 후 outlier를 고치고 실제 결함만 차단 | 계약 Gate |
| A-5 | 문서/계약 parity 실패 | 운영 endpoint를 삭제 | 공개 계약 미노출과 Route 부재는 다른 문제다 | 문서에서만 제외(`@Hidden`), Route는 유지 | OpenAPI coverage Gate |
| A-6 | 소유 Domain을 알 수 없음 | Java package / URL path / Registry 등록 여부로 추론 | 구현 배치 구조는 Architecture Role 정본이 아니다 | Owner Component의 `architectureRole`로 판정, 판정 불가 시 fail-closed(§30.16) | `test_cpf_system_identity_contract` |
| A-7 | SystemCode가 없어 실패 | `LOCAL`/`CPF`/`DEFAULT`/`UNKNOWN` 등 default 주입 | SystemCode는 Runtime이 고르는 설정값이 아니다 | 정본 canonical 고정값, 없으면 fail-closed(§30.20) | `test_cpf_system_identity_contract` |
| A-8 | Module/DB Prefix 이름이 SystemCode와 같아 보임 | 그 값을 SystemCode로 승격 | 문자열 동일 ≠ Identity 동일(§30.19) | Module/DB namespace는 `moduleCode`로 표기하고 System namespace와 분리 | `test_cpf_system_identity_contract` |

공통 Root Cause:

> **Identity Namespace(SystemCode / ChannelCode / Module Code / DB Prefix / instanceId / issuer)가 서로
> 다른 계약으로 혼재하고, Runtime을 통과시키려고 그 경계를 넘나드는 보정을 넣는 것.**

판정 순서(필수):

1. 실패한 Component의 **Architecture Role**과 Identity 보유 여부를 §30.16 표에서 먼저 확인한다.
   Role이 SystemCode를 갖지 않는데 SystemCode를 요구받는다면 **계약 적용 범위가 틀린 것**이다.
2. 값이 없어서 실패하면 **값을 만들어내지 않는다.** 축약·패딩·default·placeholder는 모두 금지다.
3. 검증기가 막으면 **완화하기 전에 정본을 확인**한다. 정본이 모호하면 정본부터 고친다.
4. 작업자 영역을 이유로 Finding만 남기지 않는다. Current Source와 Current Harness가 유일한 정본이다.

### 30.23 UTF-8은 자식 프로세스까지 강제한다

이 저장소의 표준 인코딩은 UTF-8이다. 정식 진입점(`cpf-tools/conftest.py`,
`run-cpf-pytest.py`, 각 `.ps1`)은 이미 자기 stdout/stderr를 UTF-8로 고정한다. 그러나 Windows에서
**ad-hoc으로 띄운 자식 프로세스**는 콘솔 기본값 cp949(ms949)를 물려받아 한글 진단 메시지가 깨진다.

- 사람이든 자동화든 `python` / `pwsh` 를 임시로 실행할 때는 반드시 `PYTHONUTF8=1`,
  `PYTHONIOENCODING=utf-8` 을 먼저 설정한다.
- 파일 입출력은 항상 `encoding='utf-8'` 을 명시한다. 기본 인코딩에 의존하지 않는다.
- **출력이 깨져 보이면 먼저 실행 환경을 의심한다.** 파일 바이트를 직접 확인하기 전에 "파일이
  손상됐다"고 판단하지 않는다. 멀쩡한 정본(seed/OpenAPI 등)을 콘솔 렌더링 때문에 손상으로 오인해
  고치는 것이 실제 위험이다.
- 한글을 출력하는 Python 진입점은 자기 스트림을 UTF-8로 고정해야 한다
  (`test_cpf_python_console_utf8.py` 가 강제한다).

### 30.24 하드코딩 금지 — 정본은 하나이고 코드는 그것을 읽는다

하드코딩은 "값을 코드에 적는 것" 전부가 아니라 **정본이 따로 있는 값을 코드가 다시 적는 것**이다.
같은 값이 두 곳에 있으면 정본이 둘이 되고, 정본을 바꿔도 복제본이 옛 값을 지켜 조용히 어긋난다.

#### 절대 금지

| 대상 | 금지 예 | 올바른 방식 |
| --- | --- | --- |
| 환경 경로 | 절대경로, PC/사용자 이름, IDE·Gradle 캐시·Java 설치 경로, 고정 workspace 경로 | Root 상대경로, 환경변수, 정본 설정 |
| Identity | SystemCode / ChannelCode / Module Code / Domain 이름을 코드·검증기에 직접 기재 | 정본 카탈로그·Contract에서 읽는다(§30.16, §30.20) |
| 분류 정본 | Role 목록, "SystemCode 보유 여부", 금지 코드 목록을 검증기에 복제 | `cpf-tools/governance/cpf-product-surface-policy.json` 에서 읽는다 |
| Registry 기대값 | 검증기가 기대 코드 목록을 직접 나열 | 실제 선언(Runtime config / Generator input)에서 유도한다 |
| 운영값 | timeout·retry·port·pool 크기를 Source에 상수로 매설 | 설정/정책으로 노출한다(`nxt3ConfigGate` 가 차단) |
| 비밀 | 비밀번호·키·토큰을 Source/명령행/로그에 기재 | 자식 프로세스 환경변수로만 전달 |
| 검증기 통과용 값 | 게이트를 통과시키려고 기대값을 코드에 심기 | 정본을 고치거나 결함을 고친다 |

#### 판정 기준

1. **이 값의 정본이 어디인가?** 정본이 있으면 코드는 읽기만 한다.
2. 정본이 없으면 **정본을 먼저 만든다.** 코드에 적고 나중에 정리하지 않는다.
3. 검증기가 값을 알아야 하면 **정본에서 유도**한다. 유도할 수 없으면 그 값은 정본이 아니라
   검증기 자체의 계약이며, 그 사실을 주석으로 남긴다.
4. 값이 같아 보여도 **Namespace가 다르면 다른 값**이다(§30.19). 한쪽을 다른 쪽의 근거로 쓰지 않는다.

#### 재발방지

- 정본 복제가 발견되면 복제본을 지우고 정본 읽기로 바꾼다. 두 값을 동기화하는 코드를 만들지 않는다.
- 새 검증기를 추가할 때 "이 목록의 정본은 어디인가"를 먼저 적는다. 답이 없으면 게이트를 만들기 전에
  정본을 만든다.

---

## 31. Evidence 영속성 계약 (Mandatory)

### 31.1 Manifest가 참조하는 증적은 저장소가 실을 수 있는 경로에만 둔다

Session Manifest / Change Manifest 가 참조하는 Mandatory Evidence 는 **승인된 canonical Evidence
root** 에 영속 저장한다. 저장이 차단되는 경로(예: 전역 제외 패턴에 걸리는 확장자, 임시 디렉터리,
runtime 작업 디렉터리)를 참조하면 fresh clone 에서 그 증적은 항상 존재하지 않는다.

증적이 없는 Manifest 는 "그 검증을 수행했다"는 주장만 남기고 근거를 남기지 않는다. 이는 검증
기록이 아니라 서술이다.

### 31.2 Manifest 생성 시 참조 파일의 존재와 hash를 함께 검증한다

Manifest 를 쓰는 시점에 참조 파일의 존재 여부와 hash 를 확인한다. 나중에 확인하는 구조는
이미 늦다. 파일이 없으면 Manifest 를 완성하지 않는다.

### 31.3 증적 부재는 PASS가 아니라 명시적 NOT_AVAILABLE이다

참조한 증적이 존재하지 않으면 기본은 fail-closed 다. 부재를 인정하려면 각 참조 항목에 다음을
모두 명시한다.

| 필드 | 의미 |
| --- | --- |
| `evidence_status` | `NOT_AVAILABLE` |
| `reason` | 왜 남아 있지 않은지의 사실 근거 |
| `reproducibility` | `UNAVAILABLE_FOR_ORIGINAL_SOURCE_IDENTITY` 또는 `REPRODUCIBLE` |
| `acceptanceInheritance` | `NOT_INHERITED` |

**NOT_AVAILABLE 은 PASS 가 아니다.** 그 증적에 의존하던 과거 PASS/완료 판정은 현재 Acceptance
근거로 승계되지 않는다. 현재 Mandatory Work Item 에 여전히 필요한 검증이면 Current Source
Identity 에서 Fresh 재실행하여 새 Evidence 로 닫는다.

항목을 **삭제해서** 원래 증적 요구가 없었던 것처럼 만드는 것도 금지한다. 요구는 남기고 상태를
정확히 기록한다.

### 31.4 Cleanup은 Manifest 참조를 먼저 검사한다

Evidence 파일이 정리 대상이 되면 어떤 Manifest 가 그 파일을 참조 중인지 먼저 검사하고, 참조가
있으면 삭제를 차단한다. 참조를 남긴 채 파일만 지우면 위 §31.3 상태가 사후에 만들어진다.

### 31.5 Negative Mutation

- Manifest 가 참조하는 증적 파일을 지우면 게이트가 FAIL 해야 한다.
- `evidence_status=NOT_AVAILABLE` 만 적고 `reason`/`reproducibility`/`acceptanceInheritance` 를
  갖추지 않은 선언은 FAIL 해야 한다.
- 저장이 차단되는 경로를 참조하는 살아 있는 증적은 FAIL 해야 한다.

### 31.6 Anti-pattern

| 코드 | 안티패턴 | 실제 증상 |
| --- | --- | --- |
| E-1 | Evidence 예외 규칙이 이동/삭제된 옛 경로를 계속 가리킨다 | 예외가 아무 것도 매치하지 않아 전역 제외만 살아남고, 증적이 저장소에 하나도 실리지 않는다 |
| E-2 | Manifest 는 증적을 참조하는데 그 확장자가 전역 제외 대상이다 | fresh clone 마다 동일한 증적 부재가 재발한다 |
| E-3 | 증적이 없다는 이유로 참조 항목을 지운다 | 검증 요구 자체가 사라져 이후 누구도 그 검증이 필요했다는 사실을 모른다 |
| E-4 | 증적 없이 과거 PASS 를 현재 Acceptance 로 승계한다 | 근거 없는 완료가 최종 판정까지 전파된다 |

---

## 32. 공개 배포본 Consumer 실행 계약 (Mandatory)

### 32.1 배포는 실행까지가 계약이다

Binary Repository 와 Generator 가 PASS 해도 처음 사용하는 고객이 화면을 열지 못하면 Release 가
아니다. 공개 Repository 하나만 Fresh Clone 해서 공식 bootstrap 경로로 prerequisite 를 준비하고,
공개 lifecycle launcher 만으로 운영 콘솔과 업무 채널을 실제로 기동할 수 있어야 한다.

다음이면 FAIL 이다.

- 사용자가 frontend 디렉터리에서 npm 명령을 직접 쳐야 화면이 나온다.
- 내부 Gradle project path, Development Master script, private source 를 알아야 실행된다.
- Runtime 은 뜨는데 production frontend bundle 이 실행물에 없어 화면이 없다.

### 32.2 Consumer 실행 표면

| 대상 | 성격 | 공급 방식 |
| --- | --- | --- |
| 운영 콘솔(ADM) | Platform Control Plane | Binary Repository 의 실행물 |
| 업무 Domain(MBW) | Business Domain | 공개 Source |
| Backoffice Web | **MBW 의 Channel Front** (ADM 아님) | 공개 Source |

Backoffice Web 은 ADM 과 역할·위치가 다르다. 반드시 MBW 와 함께 Consumer E2E 로 검증한다.

### 32.3 Target 해석은 공급 방식이 정한다

공개 checkout 은 내부 tooling 경로를 포함하지 않으므로 Runtime Target Catalog 를 공개 위치로
투영한다. Target 존재 판정은 provision 이 정한다.

- `provision: source` — 그 Component 의 Source 가 checkout 에 있어야 한다.
- `provision: binary` — 실행물이 Binary Repository 에 있어야 한다. **Source 디렉터리 존재로
  판정하지 않는다.** 그렇게 하면 Binary 로만 배포되는 Runtime 이 공개 배포본에서 영구히
  보이지 않는다(jar 는 있는데 실행 진입점이 끊긴다).

### 32.3.1 Local DB 재실행은 Runtime credential까지 수렴시킨다

공개 Consumer의 `bootstrap → runtime start`는 local Docker volume이 이미 존재하는 경우에도
성립해야 한다. bootstrap이 만든 local-only secret과 DB에 남아 있는 migration/runtime account의
credential이 다르면, schema 단계가 PASS여도 실제 JVM의 TCP JDBC 연결은 실패한다.

- local 관리 대상 Vendor(PostgreSQL/MariaDB/Oracle)는 `CREATE USER/ROLE IF NOT EXISTS`만으로
  끝내지 않고, 각 bootstrap에서 canonical local secret으로 migration/runtime credential을
  명시적으로 reconcile한다.
- 이는 local generated secret과 `cpf-public-*` container에만 적용한다. external/non-local DB는
  credential을 추측·변경하지 않고 explicit configuration이 없으면 fail-closed 한다.
- Consumer Runtime Gate는 bootstrap PASS 뒤 실제 Binary/Source Runtime JDBC 연결까지 확인한다.
  DB container health, schema insert, Unix-socket probe만으로 JDBC Runtime PASS를 대체하지 않는다.

Negative mutation: PostgreSQL `ALTER ROLE`, MariaDB `ALTER USER`, Oracle `ALTER USER` 중 하나라도
제거한 구현은 local volume 재사용 후 Runtime authentication failure를 재발시키므로 FAIL이다.

### 32.3.2 Local DB bootstrap은 schema privilege까지 수렴시킨다

Runtime account는 migration account와 달리 schema owner가 아니다. 따라서 DB 접속 credential이
정상이어도 새로 추가된 Common/Platform table에 DML 권한이 없으면 실제 Runtime은 startup에서
종료한다. 이는 health/container 상태나 migration 성공으로 대체할 수 없는 Consumer defect다.

- PostgreSQL은 bootstrap이 migration 후 existing table/sequence와 future migration default
  privilege를 runtime account에 함께 reconcile한다. `GRANT USAGE ON SCHEMA`만으로 완료 처리하면 FAIL이다.
- MariaDB는 database-wide runtime DML grant가 현재와 이후 table을 모두 포괄해야 한다.
- Oracle은 migration schema의 table/sequence/program unit grant를 runtime account에 매 bootstrap
  수렴시킨다. account에 `CREATE SESSION`만 있으면 FAIL이다.
- Vendor별 실제 Runtime JDBC startup이 Common durable-cache checkpoint의 SELECT/INSERT/UPDATE를
  수행해 확인해야 하며, PostgreSQL PASS를 MariaDB/Oracle PASS로 승계하지 않는다.

Negative mutation: PostgreSQL `ALL TABLES`/`ALTER DEFAULT PRIVILEGES` 또는 Oracle object-grant
reconcile 중 하나라도 제거하면 Runtime consumer startup이 fail-closed 해야 한다.

### 32.3.3 Platform runtime은 canonical DB vendor를 반드시 투영한다

ADM/Gateway/Batch 같은 Platform consumer는 Generated Domain의 datasource prefix가 아니라
공통 MyBatis Vendor SQL consumer를 함께 쓴다. local bootstrap이 Platform datasource URL만 만들고
canonical `CPF_DB_VENDOR`를 process environment에 투영하지 않으면 `cpf.db.vendor`가 비어 startup이
중단된다. 이 mandatory consumer를 조건부 제거하거나 vendor default로 숨기지 않는다.

Negative mutation: Platform runtime binding에서 `CPF_DB_VENDOR` projection을 제거하면 Fresh
Consumer ADM startup이 fail-closed 해야 한다.

### 32.4 문서도 실행 계약의 일부다

README 는 Windows/Linux 진입점, 기본/변경 가능 Port, Profile, 초기 운영자 credential 절차,
기동 URL 을 **그대로 복사해 실행할 수 있는 수준**으로 담는다. 초기 비밀번호는 파일이나 셸
히스토리에 남기지 않는 방식으로 안내한다.

게이트는 이 내용을 Runtime Target Catalog 에서 파생해 확인한다. 문서에 목록을 복제하지 않는다.

### 32.5 Negative Mutation

- 공개 Catalog 를 지우면 FAIL 해야 한다(사용자가 어떤 Target 도 해석하지 못한다).
- Binary Runtime 의 실행물을 지우면 FAIL 해야 한다.
- Channel Front Source 를 지우면 FAIL 해야 한다.

### 32.6 Anti-pattern

| 코드 | 안티패턴 | 실제 증상 |
| --- | --- | --- |
| C-1 | 구조 검증만 하고 실행을 확인하지 않는다 | Binary/Generator 는 PASS 인데 사용자는 아무것도 띄우지 못한다 |
| C-2 | Source 존재로 Binary Runtime 의 가용성을 판정한다 | 실행물이 있는데 Target 이 안 보인다 |
| C-3 | 화면 번들을 빌드에 연결하지 않는다 | Runtime 은 뜨고 API 는 되는데 화면이 없다 |
| C-4 | Channel Front 를 Control Plane 과 같은 것으로 취급한다 | 개발/운영이 잘못된 대상(포트·권한)을 바라본다 |
| C-5 | 선택 Component 라는 이유로 배포에서 빼고 게이트는 통과시킨다 | 배포본에 업무 채널이 통째로 없다 |

---

## 33. 공개 배포본 Build/Runtime Surface 경계 (Mandatory)

### 33.1 Build 권한은 Source 를 공개하는 Surface 에만 있다

Open Git Public Distribution 에서 Build/Test Capability 는 **Public Source Development Surface**
에만 허용한다. ADM / Gateway / Framework Internal / Batch Internal 처럼 Product Contract 상
Binary-only 인 Platform Component 는 공개 Consumer 가 **실행·구성·검증할 수 있으나**, 그
Component 의 Source / Module Build / Test / Publication Task 는 Public Release 에 존재해서는 안 된다.

| Surface | 공급 | 허용 Capability |
| --- | --- | --- |
| `PUBLIC_SOURCE_DEVELOPMENT` | Source | BUILD / TEST / VERIFY / RUN / STATUS / STOP / CONFIGURE |
| `PUBLIC_BINARY_RUNTIME` | 검증된 Binary | RUN / STATUS / STOP / CONFIGURE / VERIFY_RUNTIME |
| `PUBLIC_DOCUMENTATION` | 문서 Allowlist | 없음(Component graph 아님) |
| `PRIVATE_INTERNAL` | 비공개 | 없음 |

정본은 `cpf-tools/governance/cpf-product-surface-policy.json` 의 `publicDistributionSurfaces` 와
각 `moduleOwners[].publicDistributionSurface` 다. 새 Component 가 분류를 선언하지 않으면
**Release Gate 가 fail-closed** 한다. 분류 없이 공개 Build Surface 로 새어 들어갈 수 없다.

이 분류는 새 Identity 축이 아니다. `architectureRole`(역할), SystemCode(Identity)와 별개의
**Public Distribution metadata** 다.

### 33.2 내부 그룹으로 숨기는 것은 충족이 아니다

Binary-only Component 의 Task 를 `90. 내부 빌드` 같은 그룹으로 재분류하는 것만으로는 Public
Boundary 를 충족한 것으로 보지 않는다. 해당 **private project / task / source graph 자체가**
Public Release 에 없어야 한다.

검사 대상은 파일 경로만이 아니다. Gradle Included Build, Gradle Project path, Gradle Task 이름,
publication metadata, CLI capability, script entrypoint 를 포함한다. private source 가 없어도
`:apps:admin:build` 같은 graph 가 공개되면 Architecture Leakage 로 FAIL 한다.

### 33.3 cpfBuildAll / cpfTestAll 의 의미

공개 트리의 `cpfBuildAll` / `cpfTestAll` / `cpfVerifyAll` 은 **CPF Framework 전체를 Source 에서
다시 Build 한다는 뜻이 아니다.** 현재 Public Workspace 에 포함된 **Public Source Development
Surface 전체**를 의미한다. dependency 가 ADM / Gateway / Batch internal / Framework private
implementation 으로 확장되면 FAIL 한다.

`cpfVerifyAll` 이 Binary Component 를 검증할 수는 있으나 방법은 binary presence / manifest·checksum /
public API / config contract / runnable / health / runtime E2E 여야 한다. **Source compile 호출은 금지**한다.

### 33.4 Publisher Task 는 Development Master 전용

`cpfOpenGit*`(Build/Verify/Status/Prepare/Commit/Push/CommitAndPush)는 공개 Release 를 만드는
Publisher-side Task 다. 공개 Consumer 에게 제공하지 않는다. 공개 사용자가 또 다른 공개 Release 를
생성하거나 공식 공개 Repository 에 publish 하는 구조를 만들지 않는다.

### 33.5 ADM 과 Channel Front 를 같은 정책으로 묶지 않는다

"웹 화면이 있다"는 공통점만으로 배포/Build 정책을 동일하게 만들지 않는다.

- ADM = CPF Platform Control Plane = Binary-only Runtime
- Backoffice Web = 고객 MBW Channel Front = 공개 Customer Development Surface 가능
- MBW = Business Domain (SystemCode MBW)

ADM 화면을 실행하려고 사용자가 ADM Source 를 compile 하거나 frontend 를 직접 빌드해야 하는
구조는 Public Product Boundary 위반이다. ADM production backend/frontend 는 Release 에서 이미
검증된 Runtime Artifact 로 공급한다. Gateway 도 같다.

### 33.6 Negative Mutation

- 공개 트리에 Binary-only Component 의 Source Root 가 생기면 FAIL
- 공개 `settings.gradle` 에 그 Component 의 `includeBuild`/project 항목이 생기면 FAIL
- 공개 `build.gradle` 에 `cpfOpenGit*` Task 가 생기면 FAIL
- moduleOwner 가 `publicDistributionSurface` 를 선언하지 않으면 FAIL

### 33.7 Anti-pattern

| 코드 | 안티패턴 | 실제 증상 |
| --- | --- | --- |
| B-1 | 내부 Component Task 를 내부 그룹으로 옮기고 "숨겼다"고 종료 | graph 는 그대로 공개되어 Boundary 가 깨진다 |
| B-2 | 공개 여부를 경로 금지 규칙의 부수효과에 의존 | 새 Component 가 분류 없이 공개 Build 대상이 된다 |
| B-3 | `All` 이라는 이름 때문에 Framework 전체 Build 로 해석 | 사용자가 내부 구현을 빌드하려 하고 실패한다 |
| B-4 | 화면이 있다는 이유로 ADM 과 Channel Front 를 같은 정책으로 취급 | Control Plane Source 가 공개되거나 Channel Front 가 불필요하게 잠긴다 |
| B-5 | Publisher Task 를 공개 배포본에 포함 | 공개 사용자가 공식 Release 를 재생성/publish 할 수 있게 된다 |

---

## 34. 동일 Profile Initial Operator Bootstrap 계약 (Mandatory)

### 34.1 하나의 Product/Security 계약

CPF의 `local/dev/stg/test/prod` Profile은 서로 다른 제품이 아니다. 인증, 승인, 감사, 최초 운영자
생성, credential lifecycle의 **의미**를 Profile에 따라 바꾸지 않는다. Profile은 DB vendor/endpoint,
port, resource, observability destination, secret reference/value만 바꿀 수 있다.

Fresh 환경의 Initial Operator Bootstrap은 `cpf bootstrap` Golden Path의 one-time 단계다. 최초 운영자가
없을 때 strong secret ENV를 소비해 계정을 만들고 durable audit을 남긴 뒤 capability를 끝낸다. 재실행은
기존 credential을 덮어쓰지 않으며, Bootstrap 완료 뒤의 일반 운영자 변경은 모든 Profile에서 maker/checker
approval-token contract로만 수행한다.

### 34.2 Canonical Owner와 Secret 경계

- Product owner: `CPF_PRODUCT_ARCHITECTURE_AND_REQUIREMENTS.md` §12.1
- Machine-readable owner: `cpf-tools/governance/cpf-product-surface-policy.json`의
  `initialOperatorBootstrapContract`
- MBW initial secret: `CPF_MBW_BOOTSTRAP_PASSWORD`; ADM initial secret:
  `CPF_ADM_BOOTSTRAP_PASSWORD`
- Secret 값은 source/YAML/CLI argument/stdout/stderr/application log/audit/evidence/manifest/handover에
  0건이어야 하며 `bootstrapSecretProvided=true` 같은 존재 여부만 evidence에 허용한다.

### 34.3 Required Consumer / Runtime closure

Open Git Fresh Consumer Gate는 반드시 `Fresh Clone → cpf bootstrap → ADM Initial Operator → ADM actual
login/authenticated operation → MBW Initial Operator → MBW + Backoffice Web actual login → session/CSRF →
authenticated business transaction → DB/File/Transaction Log/Trace/Audit correlation → status/stop/cleanup →
Fresh Replay`를 실제로 수행한다. 401/403 또는 health/SPA만 확인한 결과는 PASS가 아니다.

### 34.4 Negative Mutation

- `if local`, `if dev`, `if prod`, `if test`로 bootstrap/auth/approval/audit 의미를 분기하면 FAIL
- profile별 fixed password, auth/CSRF disable, local automatic admin, prod-only approval이면 FAIL
- secret을 Spring YAML binding, log/audit/evidence, command argument로 넣으면 FAIL
- 최초 account가 있는데 bootstrap이 password/role을 다시 쓰거나, Initial 이전 normal approval runner가
  operator를 만들면 FAIL
- Open Git verifier가 성공 login/BFF cookie/CSRF/authenticated business transaction을 생략하면 FAIL

### 34.5 Required validator

`cpf-tools/verification/verify_initial_operator_bootstrap_contract.py`와 mutation tests는 Product/Harness/
machine policy/MBW/ADM/Open Git Consumer verifier를 함께 읽는다. 어느 한쪽만 문서화하거나 Source만
수정해 계약이 어긋나면 fail-closed한다.

## 35. 발행 Runtime 단독 기동 계약 (Mandatory)

### 35.1 근거

Fresh Open Git Consumer 가 공개 launcher 로 ADM 을 기동하자 연속으로 다른 원인에 걸려 죽었다.
`logging.config` 가 저장소에 없는 자원을 가리켰고, `CPF_PLATFORM_DB` role 선언이 없었고, Vendor SQL
Pack 이 배포본에 없었고, `com.cpf.security.common` 이 scan 범위 밖이었고, 생성자가 둘인 Bean 이
주입 대상 표시 없이 남아 있었고, Service Identity 가 소문자 application 이름으로 등록됐고,
JDBC table 존재 검사가 대문자 식별자를 고정으로 넘겼고, ApplicationRunner 가 CPF 실행 Context 없이
감사 기록을 남겼고, Liveness/Readiness 가 업무 거래 Header 를 요구했다.

원인은 하나다. **Platform Runtime 을 One-WAS 통합 Runtime 으로만 기동해 왔기 때문에 단독 기동 경로가
한 번도 성립한 적이 없었다.** 통합 Runtime 이 자기 모듈에서 채워 주던 설정·Bean·scan 범위가 단독
기동에는 존재하지 않는다는 사실이 검증되지 않았다.

### 35.2 계약

1. Runtime Target Catalog 가 선언한 모든 Runtime 은 **발행된 실행물만으로** 기동 가능해야 한다.
   통합 Runtime 이 채워 주는 설정에 의존하는 Runtime 은 미완성으로 본다.
2. Runtime 이 요구하는 논리 DB role 은 그 Runtime 자신의 설정으로 해석돼야 한다. Generated Domain 은
   `cpf.generated-domain.database-role` 선언으로, Platform Runtime 은 `role-datasources` 선언으로 해석한다.
3. `logging.config` 처럼 classpath 자원을 가리키는 선언은 그 자원이 실제로 존재해야 한다.
4. Runtime 이 기동에 요구하는 local 전용 Secret 은 공개 bootstrap 이 생성해 자식 환경으로만 전달한다.
   Source 나 Release 산출물에는 남기지 않는다. 값이 환경에 이미 있으면 생성하지 않는다.
5. Vendor SQL Pack 은 공개 배포본의 Runtime 자산이다. Runtime 은 module-local fallback 을 쓰지 않으므로
   Pack 이 없으면 fail-closed 한다.
6. Service Identity 는 SystemCode 와 다른 축이다. SystemCode 를 갖지 않는 Platform Runtime 은 중앙
   Registry 의 canonical module code 를 `service-id` 로 명시한다. application 이름으로 추론하지 않는다.
7. JDBC metadata 로 table 존재를 확인할 때는 Driver 가 알려 주는 식별자 저장 규칙을 쓰거나 대소문자
   변형을 모두 시도한다. 고정 대문자 이름은 PostgreSQL 에서 항상 실패한다.
8. HTTP 경계 밖(ApplicationRunner/Scheduler)에서 감사·거래 기록을 남기는 코드는 관리 실행 Context 를
   연 뒤 수행한다.
9. Liveness/Readiness 는 인프라 probe 다. 업무 거래 Header 를 요구해서는 안 된다. 인증/보안 필터는
   그대로 적용한다.
10. 공개 launcher 가 띄운 Runtime 은 launcher 명령이 끝난 뒤에도 계속 실행돼야 한다.

### 35.3 Required validator

`cpf-tools/verification/tests/test_cpf_runtime_standalone_startability_contract.py` 와
`cpf-docs/governance/development-harness/tests/test_negative_fixtures.py` 의 startability mutation 이
이 계약을 강제한다. 실제 증명은 Open Git Fresh Consumer Runtime Gate 의 물리 기동이다.

## 36. 공개 Workspace Gradle 반복 실행 계약 (Mandatory)

### 36.1 근거

공개 배포본에서 `cpf bootstrap`(내부적으로 `gradlew cpfVerify`)을 연속으로 실행하면 두 번째가 반드시
실패했다. `compileJava` 는 UP-TO-DATE 인데 `build/classes` 가 사라져 `package ... does not exist` 로 죽었고,
회차마다 희생되는 Included Build 가 바뀌었다. 공개 launcher 가 `--project-cache-dir` 로 Composite 전체에
하나의 project cache 를 강제했기 때문에 Included Build 들이 실행 이력과 stale-output registry 를 공유했고,
Gradle 의 OutputsCleaner 가 서로의 산출물을 등록되지 않은 것으로 보고 지웠다.

### 36.2 계약

1. 공개 Workspace 의 Gradle 명령은 **반복 실행에 대해 idempotent** 해야 한다. 같은 명령을 두 번 실행해서
   실패하면 결함이다.
2. 공개 launcher 와 settings 는 Composite 전체에 하나의 project cache 를 강제하지 않는다. 각 Build 는
   자기 기본 위치를 쓴다.
3. 검증은 한 번 실행으로 끝내지 않는다. 최소 2회 연속 실행으로 확인한다.
4. **하나의 Build Tree 에는 하나의 project cache 만 둔다.** 같은 Source Tree 를 IDE 와 CLI 가 서로 다른
   `--project-cache-dir` 로 빌드하면 stale-output registry 가 두 벌 생기고, 각 registry 가 상대의
   `build/classes` 를 등록되지 않은 산출물로 보고 지운다. 내부 개발 환경에서 이 형태가
   VS Code `code 964 missing required library` 재발의 원인이었다. 사용자/머신에 묶인 절대경로와 legacy
   작업 경로를 금지하는 규칙은 그대로 유지하되, 그 해법으로 별도 cache 를 만들지 않는다.

### 36.3 Required validator

`cpf-tools/verification/tests/test_cpf_public_gradle_workspace_idempotency_contract.py` 가 공개 Workspace
계약을, `cpf-tools/verification/tests/test_cpf_developer_shell_contract.py` 의
`test_vscode_gradle_import_shares_the_single_project_cache` 가 내부 개발 환경 계약을 강제한다. 각각 대응
negative mutation 을 둔다.

## 37. 공개 Binary Repository 생성 경로 계약 (Mandatory)

### 37.1 근거

공개 배포본이 소비하는 Binary Repository 를 최신 Source 로 맞추려고 Gradle 발행 태스크
(`cpfPublishAllVerifiedLocalPlatformArtifacts`)를 **공개 저장소 경로에 직접** 실행한 적이 있다.
그 태스크는 개발 좌표(`1.0.0-SNAPSHOT`)로 발행하므로, 릴리즈가 공개 좌표(`1.0.0`)로 투영해 둔
Artifact 가 대체되고 Generator 배포본(`com.cpf.tooling`)이 사라졌다. 공개 Consumer 는 그 순간부터
어떤 의존성도 해석하지 못한다.

릴리즈 파이프라인은 두 단계다. 개발 좌표로 staging repository 에 발행하는 단계와, allowlist
fail-closed 로 공개 좌표에 투영하는 단계다. 이 둘을 분리해 실행하면 공개 저장소가 깨진다.

### 37.2 계약

1. 공개 Binary Repository 는 **투영 단계의 산출물로만** 만든다. Gradle 발행 태스크의 출력 경로를
   공개 저장소로 직접 지정하지 않는다.
2. 발행은 항상 별도 staging repository 에 한다. 공개 저장소는 staging 을 입력으로 하는 투영
   함수(`sanitize_binary_repository`)의 출력으로만 갱신한다.
3. `cpf-release/`의 transient subtree는 저장소 추적 대상이 아니며, Current Verified Result의 보존 여부는
   Release Asset Policy가 정한다. 어느 경우에도 Fresh 재생성 경로가 항상 성립해야 하며, 재생성에는
   Generator OS matrix(windows-x64 / linux-x64) 재빌드가 포함된다.
4. 부분 재생성으로 검증을 이어갈 수 있으나, 최종 Gate 는 승인된 remote 를 clone 한 실제 Fresh
   Consumer 배포본에서 다시 수행한다. staging 트리 검증은 최종 Gate 를 대체하지 않는다.

### 37.3 Required validator

`cpf-tools/release/open-git/cpf_open_git.py` 의 stage 5/8 분리와
`cpf-tools/release/public/publish-cpf-public-repository.py` 의 staging repository 인자가 이 계약의
구현이다. 공개 저장소 경로를 발행 대상으로 넘기는 명령은 문서/스크립트/증적 어디에도 남기지 않는다.

## 38. Runtime Lifecycle CLI 계약 (Mandatory)

### 38.1 배경

Runtime 운영이 OS별 script, Gradle Project, 내부 Module 구조에 흩어져 있으면 사용자는 "어떤 파일을
실행하지"를 먼저 풀어야 한다. Batch 처럼 구성요소가 여러 개인 Runtime 은 이름을 전부 외워야 하고,
Generated Domain 은 만들 때마다 launcher 를 고쳐야 한다. 이것은 기능 부족이 아니라 Product UX 결함이다.

### 38.2 계약

1. CPF Public Runtime 은 **전체 / 논리 그룹 / 개별 Target** 세 수준에서 일관된 Lifecycle CLI 를
   제공한다. 사용자는 내부 Module, Gradle Project, Private Source, OS-specific wrapper 구조를 알 필요
   없이 canonical `cpf` 명령으로 `start` `stop` `restart` `status` `health` `log` 를 수행할 수 있어야 한다.
2. Runtime Group 과 Generated Domain Target 은 **canonical machine-readable authority** 에서 파생한다.
   대상 목록을 CLI Source, launcher, README, help 텍스트에 복제하지 않는다. 정본은
   `cpf-tools/runtime/cpf-runtime-target-catalog.json` 의 `runtimes` / `dynamicRuntimes` /
   `runtimeGroups` / `startOrderContract` 다.
3. Group 은 **대상 집합만** 정하고 `dependsOn` 은 그 집합 안의 실행 순서만 정한다. Group 이 의존
   대상을 자동으로 끌어오면 사용자가 요청하지 않은 Runtime 이 올라간다. 순환 의존은 fail-closed 한다.
   정지는 기동 순서의 역순이다.
4. Group 이름과 Runtime target 이름은 같은 문자열을 쓰지 않는다. 하나의 이름이 두 의미를 가지면
   사용자와 검증기가 서로 다른 것을 가리킨다. Runtime 이 하나뿐인 대상에는 Group 을 만들지 않는다.
5. 특정 Target 에서 의미 없는 Capability 는 조용히 무시하지 않고 `UNSUPPORTED` 로 알린다. Group 실행
   결과는 Target 별로 표시하고, 일부 실패를 전체 성공으로 숨기지 않는다(`OVERALL FAIL`).
   상태 집계는 pid 존재만으로 HEALTHY 로 판정하지 않으며 부분 장애는 `DEGRADED` 다.
6. Windows 와 Linux 는 같은 Target 이름, 같은 옵션, 같은 Lifecycle, 같은 Group 을 쓴다. OS별 wrapper 는
   canonical entrypoint 로 위임하는 thin wrapper 이며 **자체 명령 해석을 갖지 않는다.**
7. Public Source Development Surface 와 Binary-only Runtime Surface 를 구분한다. Binary-only Runtime 은
   실행 가능하지만 Private Source Build/Test Task 는 Public Distribution 에 존재해서는 안 된다.
8. Profile(local/dev/stg/test/prod)은 동일한 Lifecycle CLI 계약을 쓴다. 환경 차이는 endpoint, resource,
   secret, infrastructure config 에 한정하며 명령이나 Group 구성이 달라지지 않는다.
9. timeout, retry, port, host, URL, runtime enablement, target membership 은 Source 에 하드코딩하지
   않고 canonical 설정에서 읽는다.

### 38.3 사용자 Steering 반영 계약

사용자가 개발/Architecture/QA/Product Contract/운영 UX 에 대한 새 Steering 을 주면 해당 Source 수정에
그치지 않는다. 다음까지 닫아야 완료다.

```
User Steering
→ Current Harness Rule
→ Product / Runtime / CLI Contract
→ Machine-readable Canonical Authority
→ Validator
→ Negative Mutation
→ CURRENT_WORK_ITEM_REGISTRY.csv
→ Consumer → Test → Runtime → Evidence
```

Rule 문장만 추가하고 Validator/Negative Mutation/Registry 연결이 없으면 완료가 아니다. 다음 세션이나
다른 Agent 가 과거 대화를 기억하지 못해도 Current Harness 만 읽으면 같은 Product Contract 를 재현할 수
있어야 한다.

### 38.4 Required validator

`cpf-tools/verification/tests/test_cpf_runtime_lifecycle_cli_contract.py` 가 이 계약의 구현이다.
자동 검증 범위: canonical Group 존재, Group/Target 이름 충돌 없음, Group 중복 없음, metadata 파생,
Runtime 별 Group metadata 존재, dependency 실재와 순환 없음, Channel Front 기동 순서, Lifecycle
Capability 계약, Generated Domain 동적 발견, CLI Source 의 Target/Group 이름 분기·목록 복제 없음,
공개 명령 집합, 위치 인자 selector, 잘못된 Target 안내, Group 결과 집계와 부분 실패 노출, 역순 정지,
pid 단독 판정 금지, dependency cycle fail-closed, Windows/Linux wrapper 의미 일치, wrapper 자체 명령
해석 없음, README/help parity, Harness Rule 존재, Registry 관계 존재.

## 39. Release Asset 보존 / Open Git Fresh 재생성 계약 (Mandatory)

### 39.1 배경과 핵심 분리

두 질문을 반드시 따로 판단한다.

- **QUESTION A** — 이 Asset 을 Development Master Git 에 보존할 것인가? (`masterTracked`)
- **QUESTION B** — 다음 Open Git Release 를 만들 때 다시 Fresh 생성해야 하는가? (`freshRegenerationRequired`)

둘은 독립이다. 다음 단순화는 전부 틀렸다.

```
generated 니까 Git 저장 금지        (X)
Git 에 있으니까 Release 에서 재사용  (X)
binary 니까 무조건 Git 제외          (X)
text 니까 무조건 Git 포함            (X)
```

`cpf-release/open-git` 은 정본이 아니다. Development Master 의 canonical source 가 정본이고 Open Git
tree 는 그 **projection 결과**다.

### 39.2 영구 Rule

1. Development Master 는 용량/운영성/보안상 **실질적인 문제가 없는 Current Verified Release Asset 을
   가능한 범위에서 함께 보존한다.**
2. **Master Git 보존 여부와 Open Git Fresh 재생성 여부는 독립된 계약이다.**
3. Master Git 에 보존된 Generated/Release Artifact 는 **다음 Fresh Release 의 Build/Generation 입력으로
   사용하지 않는다.**
4. Open Git Release 는 항상 **Clean Isolated Release Workspace** 에서 Canonical Source 기준으로 다시
   Build/Generate/Projection 하여 생성한다.
5. Release 의 Clean 은 `git clean` / `git reset --hard` / `git restore .` 가 아니라 **승인된 generated
   release root 또는 isolated staging** 을 대상으로 한다. Canonical Source, `.git`, Harness, Product
   Source 는 건드리지 않는다.
6. **실측된 문제가 확인된 Asset 만** machine-readable exception 으로 Master tracking 에서 제외한다.
   binary/generated 라는 이유만으로 일괄 제외하지 않으며, 임의 용량 Threshold 를 코드나 정책에
   숫자로 박지 않는다.
7. Tracked Current Release Result 는 Audit/Diff/Review/Current Deliverable 보존용이며
   **Fresh Build Cache 가 아니다.**
8. 사용자 Steering 은 Source 에만 적용하지 않고 Harness/Validator/Negative Mutation/Registry 에
   영구 반영한다.

### 39.3 Asset 분류

정본 metadata 는 `cpf-tools/release/open-git/open-git-surface-policy.json` 의 `releaseAssetPolicy` 다.
분류는 **파일명/확장자/폴더명이 아니라 투영 규칙의 metadata** 에서 파생한다.

| 부류 | masterTracked | publicRelease | releaseInputAuthority | freshRegenerationRequired |
| --- | --- | --- | --- | --- |
| `CANONICAL_RELEASE_SOURCE` | true | Surface Policy | **true** | false (대신 매 Release Fresh Projection) |
| `TRACKED_VERIFIED_RELEASE_RESULT` | true | Surface Policy | **false** | true |
| `UNTRACKED_RELEASE_RESULT` | false (사유 필수) | true | false | true |
| `TRANSIENT_RELEASE_OUTPUT` | false | false | false | true |

- **CANONICAL_RELEASE_SOURCE** — 사람이 지속 관리하는 정본(`bin/cpf*`, 공개 Gradle workspace,
  공개 config/schema/template, Runtime/Product Catalog, Release Policy/Engine). Release 엔진이 이 본문을
  문자열로 생성하지 않는다. 다시 코드 생성(RE-GENERATE)은 하지 않지만, **지난 Release 출력을 이어
  쓰지도 않는다.** Clean Workspace 로 Canonical Source 에서 **Fresh Projection** 한다.
- **TRACKED_VERIFIED_RELEASE_RESULT** — 현재 Release 상태 보존 가치가 있어 Master 에 함께 두는 결과
  (JAR, native generator, POM, Sources/Javadoc, SBOM, Checksum, Manifest, 검증된 binary repository 등).
  보존하되 **다음 Release 의 원재료로 쓰지 않는다.** 승격은 `GENERATED → STAGED → VERIFIED →
  PROMOTED_CURRENT_RELEASE` 이며 currentize 는 `generate → verify → promote → currentize` 순서다.
  논리 Artifact Set 단위로 원자적으로 갱신한다(새 JAR + 옛 SBOM 혼합 금지).
- **UNTRACKED_RELEASE_RESULT** — 실측된 Repository 운영 문제로 Master 에 두지 않는 공개 자산.
  `trackingExceptionReason` 과 실측 근거(현재 크기, Release Set 크기, repository 성장, clone/pull 영향,
  hosting 제약, 대안)를 남긴다. **Master 미보존이 공개 제외를 뜻하지 않는다.**
- **TRANSIENT_RELEASE_OUTPUT** — build/tmp/cache/staging/temporary clone/PID/failed output 등. 제품
  자산이 아니다.

`.gitignore` 는 형식이 아니라 용도로 만든다. `cpf-release/**`, `*.jar`, `*.zip` 처럼 Current Verified
Release Artifact 까지 일괄 제외하는 규칙을 두지 않는다.

### 39.4 Gate 분리

개발 중에는 **영향도 기반 Targeted/Fast Gate**(`developmentGate=IMPACT_TARGETED`)를 쓴다. UNKNOWN 을
NO_IMPACT 로 자동 처리하지 않는다. Source 가 안정된 뒤 Final Release Candidate 에서만 전체를
**한 번**(`finalReleaseCandidateGate=FULL_FRESH_ONCE`) 수행한다.

```
Source Freeze → Current Source Identity → Clean Release Workspace
→ Fresh Build → Fresh Test → Fresh Publication → Fresh Generator → Fresh Generated Source
→ Canonical Public Source Fresh Projection → Fresh Binary Repository → POM/Sources/Javadoc
→ Fresh SBOM → Fresh Checksum → Fresh Manifest → Leakage 0 → Release Candidate
→ Tracked Result 비교/현행화 → Fresh Consumer → Evidence
```

발행 경로는 `isolatedStaging → verification → promotionOrProjection` 이다. Publisher 의 출력 경로를
tracked release tree 나 Open Git working tree 로 **직접 지정하지 않는다**(§37 과 같은 계약).

### 39.5 Acceptance

다음 두 문장이 **동시에** 참이어야 한다.

- A. Development Master checkout 만으로 현재 CPF Release 구조와 적정 크기의 Current Verified
  Deliverable 을 충분히 확인할 수 있다.
- B. Master 에 보존된 이전 Release Result 를 Fresh Release 입력에서 완전히 배제해도 Canonical Source
  만으로 Open Git Release 를 Clean/Fresh 재생성할 수 있다.

하나라도 거짓이면 미완료다.

Working Tree 에는 Current Release 하나만 둔다. `release-old/`, `release-previous/`, `backup-release/`
같은 과거 사본을 누적하지 않는다. 과거 Release 는 Git History 가 담당한다.

### 39.6 Release Size Finding 은 payload composition 으로 판정한다

Release 산출물이 크다는 사실만으로 "대용량 바이너리"라고 판정하지 않는다. 크기를 근거로 어떤 결정을
내리기 전에 byte 를 다음으로 분해해 보고한다.

1. CPF 자체 개발 Artifact
2. OSS dependency 의 별도 repository 복제본
3. executable/fat JAR 내부에 포함된 OSS dependency
4. 동일 dependency 의 Runtime 별 중복
5. Sources/Javadoc/POM/metadata
6. Release 에 실제 필요한 파일
7. Fresh Consumer 에만 필요한 파일
8. Offline Consumer 를 위해 필요한 파일

보고 항목은 다음 이름을 쓴다.

```
CPF authored binary bytes
OSS separately vendored bytes
OSS embedded-in-fat-jar bytes
duplicate embedded dependency estimated bytes
metadata/docs bytes
required public payload bytes
avoidable bytes
```

원칙:

1. Maven Central 등 공개 OSS repository 에서 정상 resolve 되는 dependency 를 CPF 공개
   binary-repository 에 **불필요하게 중복 저장하지 않는다.**
2. 다만 Product Contract 가 완전 Offline/Fresh Consumer 를 요구해 dependency bundling 이 필요하면
   **임의로 제거하지 않는다.** Offline 요구 여부는 공개 Workspace 의 repository 선언으로 확인한다.
3. fat JAR 내부 dependency 와 OSS JAR 별도 복제는 **구분해서 계산**한다. 둘은 다른 문제다.
4. CPF 자체 코드 크기와 OSS 포함 크기를 **분리해서** 보고한다.
5. 같은 OSS dependency 가 여러 fat JAR 에 반복 포함되어 용량을 키우는지 **측정**한다.
6. 분석만으로 thin JAR / fat JAR Architecture 를 **바꾸지 않는다.** 포장 방식 변경은 Product Contract
   결정이며 사용자 판단 사항이다.

측정 도구는 `cpf-tools/release/open-git/report_release_payload_composition.py` 이고 결과는
`current/RELEASE_PAYLOAD_COMPOSITION.json` 이다. tracking 예외를 논의할 때는 이 결과를 함께 제시한다.

### 39.7 잔여물 없음의 증명 방식

이전 Release 잔여물이 새 Release 에 섞이지 않는다는 것은 두 가지로 증명한다.

1. **구조적 증명** — Release 엔진은 어떤 생성보다 먼저 Release Root 를 비운다. 이 순서를 계약으로
   고정하고, 순서를 뒤집는 negative mutation 이 FAIL 하는 것을 확인한다. 전제조건 확인은 그 삭제보다도
   앞에 온다(§39.2 Rule 5, CRF-51).
2. **실행 증명** — Fresh Consumer 가 실제로 생성된 배포본만으로 bootstrap/기동/업무거래/정지를 수행한다.

매 mutation 마다 전체 Release 를 반복 실행하지 않는다. 그것은 검증 강도가 아니라 시간만 늘린다.

### 39.8 Required validator

`cpf-tools/verification/tests/test_cpf_release_asset_freshness_contract.py` 가 이 계약을 강제하고,
`cpf-tools/release/open-git/report_release_asset_inventory.py` 가 전수 분류(`current/RELEASE_ASSET_INVENTORY.csv`)와
tracking 상태를 산출한다. 대응 negative mutation group 은 `RELEASE_ASSET` 이다. 자동 검증 범위: 4분류와 4축 선언, 규칙별 부류 파생, 경로/확장자 분류
금지, 축 독립성, tracked 결과의 입력 권한 부재, 공개 여부의 Master 비추론, tracking exception 실측 근거,
용량 Threshold 하드코딩 금지, Canonical Source 의 Fresh Projection 과 이전 출력 재사용 금지, Release
엔진의 launcher 본문 생성 금지와 확장자 기반 판단 금지, 생성 입력 실재, 승격 순서와 원자적 현행화,
Clean 대상/금지 명령/보호 경로/잔여물 금지/전제조건 선행, 발행 경로, `.gitignore` 일괄 제외 금지,
Gate 분리와 최종 순서, 두 Acceptance 동시 성립, Current-only.

## 40. Service Registry Provisioning 계약 (Mandatory)

### 40.1 배경

Runtime Control Agent 는 자기 `service_id` 가 중앙 Registry(`OPS_SERVICE`)에 등록되어 있어야 기동한다.
이 fail-closed 계약 자체는 옳다. 그러나 **사용자가 만든 Generated Domain 의 service 를 누가 등록하는지**가
정해져 있지 않았다. Platform seed 는 ADM/BAT/CEC/EDU/MBW 만 담고 있었고, Backoffice(MBW)는 seed 에
들어 있어서 우연히 통과했을 뿐이다. 그 결과 `cpf domain-new` 로 Domain 을 만든 사용자는
`Runtime Agent service가 중앙 Registry에 등록되어 있지 않습니다: <SystemCode>` 로 **영원히 기동할 수 없었다.**

### 40.2 계약

1. **등록의 실행 주체는 `cpf bootstrap` 의 Platform DB provisioning lifecycle** 이다. 등록 **규칙의 Owner** 는
   canonical Service Registry provisioning 계약이다. Generator, ADM, Runtime 어느 한 곳의 임시 부가기능으로
   구현하지 않는다.
2. **Domain 생성(`cpf domain-new`)은 Platform DB 가용성에 의존하지 않는다.** 오프라인/신규 개발환경에서도
   Domain Source 생성은 가능해야 하며, 생성 시점에 DB 접속이나 등록을 강제하지 않는다.
3. `cpf bootstrap` 은 Workspace 의 Generated Domain 을 **canonical Domain Catalog/Contract 에서 동적으로
   발견**한다. Domain 이름이나 SystemCode 를 계약·코드 어디에도 복제하지 않는다. 신규 Domain 도 Source
   수정 없이 자동 포함된다.
4. Platform DB provisioning 단계에서 각 Generated Domain 의 canonical service metadata 를 Registry 와
   **reconcile** 한다. 순서는 `validate → reconcile → fail-closed` 다.
   - 없으면 **등록**한다.
   - 같은 계약으로 이미 있으면 **idempotent PASS** 다. 재실행이 중복 INSERT 를 만들지 않는다.
   - 같은 key 인데 소유/종류가 다르면 **덮어쓰지 않고 fail-closed** 한다.
   - 운영자가 내려둔 상태(`use_yn='N'`)를 provisioning 이 조용히 다시 켜지 않는다. fail-closed 다.
   - 표시용 값(`service_name`, `description`)이 다르면 운영자가 바꾼 것이다. 덮어쓰지 않는다.
5. **Runtime 자가 등록은 금지한다.** "Registry 에 등록되어 있어야 기동 가능하다" 는 fail-closed 계약을
   유지하며, Runtime 이 자기 `service_id` 가 없다고 Registry 를 임의로 바꾸지 않는다.
6. **ADM 수동 등록은 Golden Path 가 아니다.** ADM 은 조회/운영/승인 UI 일 수 있으나, 신규 Generated
   Domain 을 실행하기 위해 사용자가 매번 ADM 에 먼저 접속해 등록하게 하지 않는다.
7. `cpf domain-new` 에서 즉시 등록하지 않는다. Source 생성과 Platform DB provisioning lifecycle 을 분리한다.
8. `service_id` 와 ownership 값은 **canonical Domain/Service Contract 에서 그대로** 가져온다. 이름
   truncation / inference / fallback / 임시 문자열 조합을 금지하고, 특정 Domain 전용 분기를 두지 않는다.
9. local/dev/stg/test/prod 가 **같은 lifecycle 계약**을 쓴다. 특정 profile 에서만 auto-register 하거나
   prod 에서만 수동 등록하는 기능 차이를 만들지 않는다. Profile 차이는 DB endpoint/credential/resource 뿐이다.
10. provisioning 기능을 bootstrap 전용 임시 함수로 만들지 않는다. SQL 정본은 vendor pack 하나이고, 실행
    코드는 그 SQL 을 읽어 쓴다. 중복 SQL / 중복 등록 로직을 두지 않는다.

### 40.3 정본 흐름

```
cpf domain-new
→ Canonical Generated Domain Source/metadata 생성
→ cpf bootstrap
→ Workspace Domain discovery
→ Platform DB provisioning
→ OPS_SERVICE validate / reconcile / register
→ Domain DB/schema provisioning
→ Runtime prerequisite verification
→ Runtime start
```

### 40.4 정본 위치

| 대상 | 정본 |
| --- | --- |
| 등록 규칙 | `cpf-tools/db/canonical/service-registry-provisioning.json` |
| SQL | `cpf-tools/db/vendor/{vendor}/runtime/cpf/repository/service-registry-*.sql` (3 vendor) |
| 실행 | `cpf-tools/runtime/bootstrap/CpfBootstrap.java#reconcileServiceRegistry` |
| 공개 투영 | `config/service-registry-provisioning.json`, `deploy/local/db/vendor/{vendor}/**` |

### 40.5 Required validator

`cpf-tools/verification/tests/test_cpf_service_registry_provisioning_contract.py` 가 이 계약을 강제한다.
자동 검증 범위: 실행 주체/Owner 분리, 대상 table·key 의 schema 일치, 기본값 없는 NOT NULL column 전량 공급,
값 출처의 정본성과 변환 금지, identity/ownership 의 SystemCode 유래, 대상 집합의 동적 발견과 이름 비복제,
reconcile 순서와 등록/idempotent/충돌 fail-closed, 운영자 편집값 비덮어쓰기, 비활성 행 fail-closed,
Runtime 자가 등록 금지, profile 무차별, vendor 3종 SQL 실재와 이름 붙은 parameter, 실행 코드의 SQL 비복제,
DB Lifecycle 내 실행 순서, Domain 이름 비하드코딩, 공개 배포본 투영. 대응 negative mutation group 은
`SERVICE_REGISTRY` 다.

### 40.6 실행 검증

기존 Domain, 신규 Generated Domain, 최초 등록, bootstrap 재실행 idempotency, duplicate/conflict negative,
Registry 누락 상태의 Runtime fail-closed, Runtime 자가 등록 불가, DB3(Oracle/PostgreSQL/MariaDB) 영향,
Fresh Consumer bootstrap → Runtime start 까지 확인한다.
