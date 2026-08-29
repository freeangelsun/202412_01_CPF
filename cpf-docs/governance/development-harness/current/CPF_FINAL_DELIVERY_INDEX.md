# CPF Final Delivery Index — Current

- Baseline ZIP: `CPF_FULL_SOURCE_FOR_NEXT_QA_20260829_224746.zip`
- Baseline ZIP SHA-256: `b1daa68a3508cd5dddec90cae25f8aeaaca636bae5703b83a322974c7f5938dc`
- Baseline Product Source Identity: `f046266a8802c129762516742a5c0406f23b0039a0b1c94ee5be3fde31e3e545` / 8,698 files
- Current Product Source Identity: `9f98c57f5708b61433d1fbc8c3c50c21fb1437b61a48c0bb085b5a394ec44d7c` / 8,448 files
- Canonical Product Requirements: **218**
- Requirement/Tracking Work: **394**
- Root Cause Execution WP: **16**
- Current Work Items: **410**
- Role Ledger: **1,230**
- Test Ledger: **820**
- Migration Semantic Ledger: **265**
- Delete eligible: **246**
- Protected retain: **19**
- Change Manifest: **ADD 146 / MODIFY 133 / DELETE 0**
- Root-relative changed payload: **279 files**
- Package integrity files: `CHANGE_MANIFEST.csv`, `SHA256SUMS.txt`
- Overlay archive payload: **281 files**
- Overlay archive: `CPF_C_DEV_QA_2_5_SOURCE_OVERLAY_20260830_011006.zip`

## Current Validation

- Development Harness Final Gate: **PASS**
- Negative Mutation / False Green: **27/27 PASS**
- CPF tools Fresh pytest: **973 PASS / 37 SKIP / 0 FAIL / 15 subtests PASS**
- Affected final regression: **4/4 PASS**
- NXT3 Layout: **87/87 PASS**
- NXT3 Garbage/Hygiene: **PASS**
- Migration Semantic Closure: **PASS**
- Product Conformance: **PASS / findings 0**

## Physical Acceptance

전체 CPF 완료는 아니다. 현재 실행환경의 Java 21 / PowerShell·Docker·Frontend dependency 제약 때문에 아래 필수 항목은 `BLOCKED_EXTERNAL`, `NOT_EXECUTED` 또는 `VERIFICATION_PENDING` 상태를 유지한다.

- Java25 Root Build/Test/Publication/SBOM
- Fresh VS Code/Gradle Buildship Error=0 Warning=0
- Oracle/PostgreSQL/MariaDB DB3 Physical Full Lifecycle
- Windows/Linux Unified CLI/Generator Physical Lifecycle
- Batch 5-role + Worker×2 + kill/takeover/fencing/UNKNOWN/reconcile
- One-WAS transaction/log correlation/runtime OpenAPI
- Frontend npm lint/typecheck/test/build + Browser E2E/a11y/error states
- Performance live/load/soak
- Actual Open Git Fresh Release + Fresh Consumer + Leakage 0
- Same Source Full Runtime/Fresh Replay
- Codex/Claude Independent Review
- QA Final Acceptance

## Current Authority

다음 세션부터 별도 개발정본을 만들지 않고 `cpf-docs/governance/development-harness/`의 Current Development Harness만 단일 실행 정본으로 사용한다. 상세 상태는 `CURRENT_WORK_ITEM_REGISTRY.csv`, `CURRENT_DEVELOPMENT_STATUS.csv`, `ROLE_EXECUTION_LEDGER.csv`, `TEST_EXECUTION_LEDGER.csv`, `CONTROL_EXECUTION_LEDGER.csv`, `TEST_AND_EVIDENCE.md`, `OPEN_ISSUES.md`, `CPF_DEVELOPMENT_HANDOVER.md`를 따른다.
