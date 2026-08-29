# CPF Development Harness — Current

> **단일 개발/QA 실행 정본**. CPF 개발 진행, 검수, 보완, Runtime, Evidence, 역할 상태, 세부 리뷰, 인수인계는 이 Harness만 사용한다. 과거 분산 개발 정본·원장·Evidence는 Harness에 의미 손실 없이 흡수한 뒤 exact Delete Manifest로 제거한다.

## 1. 목적

CPF(Core Platform Framework)를 금융권을 포함한 업무 시스템의 구축·운영·감사·확장·검증·배포·상용화가 가능한 Business Platform 품질로 유지하기 위한 **실행 가능한 개발 통제 체계**다. Harness는 개발자의 기억이나 대화 문맥 대신 Registry·Policy·Validator·Evidence로 요구사항과 완료조건을 보존한다.

Harness 도입은 CPF Architecture, Owner, Header, 연동, DB, Generator, Starter, Generated Domain, Frontend, Batch, Gateway 규격을 바꾸지 않는다. 기존 Product Contract를 내부 `product/CPF_PRODUCT_ARCHITECTURE_AND_REQUIREMENTS.md`로 흡수하고 더 강하게 추적·검증한다.

## 2. 읽기 순서

1. `CPF_DEVELOPMENT_HARNESS.md` — 실행 진입점
2. `standards/DEVELOPMENT_EXECUTION_CORE_POLICY.md` — 기존 개발 기본지침 전체 + 추가 비협상 규칙
3. `product/CPF_PRODUCT_ARCHITECTURE_AND_REQUIREMENTS.md` — 제품 Architecture/Requirement 계약
4. `contracts/contract-registry.json` + `contracts/harness-control-registry.csv` — 코드가 읽는 단일 규칙/통제 Registry
5. `current/CANONICAL_PRODUCT_REQUIREMENTS.csv` — Product Requirement Registry
6. `current/CURRENT_WORK_ITEM_REGISTRY.csv` — inherited Closure + 신규 Harness Finding 통합 실행 원장
7. `current/CURRENT_DEVELOPMENT_STATUS.csv` — 역할/검증 포함 현재 상태
8. `current/ROLE_EXECUTION_LEDGER.csv` — DevGPT / Independent Reviewer(Codex=Claude) / QA 역할 실행 원장
9. `evidence/<role>/current/**` — exact-source 실행 근거
10. `PACKAGE_MANIFEST.json`, `SHA256SUMS.txt`, `TEST_AND_EVIDENCE.md` — Harness 자체 검증/패키지 근거

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

모든 수정은 `standards/CPF_IMPACT_ANALYSIS_AND_CHANGE_CLOSURE_STANDARD.md`를 적용한다. 사용자가 리뷰를 요청하면 `standards/CPF_REVIEW_OUTPUT_STANDARD.md`대로 **실제 Current Work Item Registry에 존재하는 전체 항목을 개수 하드코딩 없이 세부항목별 1:1** 리뷰할 수 있어야 한다. `tools/generate_detailed_review.py`로 뼈대를 재생성한다.

## 7. 테스트/환경

`standards/CPF_MAX_INTENSITY_TEST_AND_RUNTIME_STANDARD.md`가 모든 역할과 사용자 로컬 Test 요청의 기본 강도다. 환경 부족 시 smoke로 축소하지 않고 `BLOCKED_EXTERNAL` + Windows/Linux 최고강도 실행명령 + prerequisite + PASS/FAIL 기준 + Evidence 요구를 남긴다.

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

### Windows
```powershell
pwsh .\cpf-docs\governance\development-harness\bin\validate.ps1
pwsh .\cpf-docs\governance\development-harness\bin\status.ps1
pwsh .\cpf-docs\governance\development-harness\bin\review.ps1
pwsh .\cpf-docs\governance\development-harness\bin\full-verify.ps1
```

### Linux
```bash
bash ./cpf-docs/governance/development-harness/bin/validate.sh
bash ./cpf-docs/governance/development-harness/bin/status.sh
bash ./cpf-docs/governance/development-harness/bin/review.sh
bash ./cpf-docs/governance/development-harness/bin/full-verify.sh
```

`DELETE_LEGACY_CANONICAL.ps1`과 `DELETE_LEGACY_CANONICAL.sh`는 새 Current Harness Self Gate가 PASS한 뒤에만 exact Delete Manifest를 실행하며 Windows/Linux 동일 의미를 유지한다.

`validate`는 Harness 구조/원장/역할/Evidence/Migration/UTF-8/current-only를 검사한다. `full-verify`는 제품 최대강도 검증 진입점이며 환경 요구로 실행되지 않은 필수 단계는 PASS로 만들지 않는다.

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
