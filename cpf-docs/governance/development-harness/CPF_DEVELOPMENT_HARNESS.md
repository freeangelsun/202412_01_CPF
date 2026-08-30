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

실행 명령의 Java/Node/npm/Python/PowerShell/Docker/DB/Browser 등 prerequisite는 **대화 기억, 과거 Evidence, 사용자 PC의 현재 설치값에 맞춰 임의 작성하지 않는다.** 실행 직전 Current Source의 canonical bootstrap, verifier, package metadata, toolchain contract, lock/config에서 required 값을 다시 읽고 `required / actual / prerequisite source`를 기록한다. Source가 요구하는 값과 환경이 다르면 Product Contract를 로컬 환경에 맞춰 낮추지 않고 환경을 교정하거나 정확한 `BLOCKED_EXTERNAL`로 남긴다. 자세한 규칙은 `standards/CPF_RULE_MODEL_AND_IMPACT_SEARCH_STANDARD.md`의 Current Prerequisite 규칙을 따른다.

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

아래 Block은 **상태 기록 영역**이며 규칙 설명 영역과 구분한다. Harness 적용 직후 또는 새 Session 시작 시 먼저 currentize한다.

| Field | Current Value | 해석 |
| --- | --- | --- |
| merge_protocol_version | `1` | Session Merge 규약 버전 |
| merge_baseline_source_identity | `CURRENTIZE_REQUIRED` | 적용 시점 Current Product Source Identity로 교체해야 함 |
| last_merged_session_key | `PRE_PROTOCOL_BASELINE` | 편의용 Watermark. 이것만으로 Merge 완전성을 판정하지 않음 |
| merged_session_set_digest | `DISCOVERY_REQUIRED` | 전체 merged sessionKey 집합의 정렬 digest |
| pending_session_keys | `DISCOVERY_REQUIRED` | 세션 전체 검색 후 정확한 목록으로 갱신 |
| conflict_session_keys | `DISCOVERY_REQUIRED` | 충돌 없음이면 `NONE` |
| last_merge_review_at | `NOT_INITIALIZED` | 마지막 실제 Merge 검토 시각 |
| last_merge_reviewer_session_key | `NOT_INITIALIZED` | 마지막 Merge를 검증한 sessionKey |

`CURRENTIZE_REQUIRED/DISCOVERY_REQUIRED/NOT_INITIALIZED`가 남은 상태는 **신규 개발 시작 허용 상태가 아니다**. 최초 다음 작업자는 세션 전체를 검색해 이 Block을 실제 값으로 currentize하고, Mandatory Pending/Conflict=0을 확인한 뒤 자신의 신규 개발로 넘어간다.

## 21. 역할별 Merge 권한과 QA

중앙 담당자를 매번 수동 지정하지 않는다. 대신 **다음 세션 작업자가 이전 미Merge 세션을 먼저 정리**한다. 다만 한 시점에 `CURRENT_WORK_ITEM_REGISTRY.csv`를 쓰는 Merge writer는 하나만 허용하고, writer는 Registry SHA 기반 compare-before-write로 병렬 변경 유실을 차단한다. 쓰기 직전 SHA가 달라지면 덮어쓰지 않고 최신 Registry에서 Merge를 다시 계산한다.

Developer/Claude/Codex는 자신의 실제 개발·검수 사실을 Merge할 수 있지만 다른 역할의 PASS를 대신 만들 수 없다. QA는 최종 Acceptance 전에 Session Manifest 전체와 Current Registry를 다시 대조해 Pending/Conflict=0, Evidence/Source Identity 일치, 역할별 근거 완전성을 확인한다.

## 22. 최종 전체 Self Review Hard Gate

최종 완료 전에는 모든 Mandatory Work Item을 **한 건씩** 리뷰한다. `전체 410건 완료`처럼 집계만 작성하는 Final Report를 금지한다.

각 Work Item은 `원 Requirement → Root Cause → Owner → Source → Consumer/호출경로 → Config/DB/Generator/API/Frontend → 오류/복구 → Test → Runtime → Regression → Evidence/SHA → Source Identity → 역할별 상태 → 완료/미완료 사유` 순서로 독립 검증한다.

Evidence 없는 CLOSED/PASS, 여러 Work Item의 일괄 완료, 일괄 SKIP, 과거 Source PASS 자동 승계, mandatory NOT_EXECUTED/UNKNOWN/VERIFICATION_PENDING/BLOCKED_EXTERNAL, 미Merge Session, MERGE_CONFLICT가 하나라도 있으면 Final Gate는 PASS가 아니다.

