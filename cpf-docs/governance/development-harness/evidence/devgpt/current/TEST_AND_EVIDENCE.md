# CPF Developer GPT Test & Evidence — 2026-08-27

## Source Identity

- Baseline ZIP: `CPF_FULL_SOURCE_FOR_NEXT_QA_20260826_195052.zip`
- Baseline ZIP SHA-256: `00abb643557a9562ff3aa40f088c8791af4e01d0cfb056e5509f70d146b90ec0`
- Current Product Source SHA-256: `79264c2975bd0b8504a0e2f8ec375070c08699ebcb512e26323d90d7e39490fb`
- Product Source: `8,334` files / `38,599,665` bytes
- Canonical Product Requirement: `208`
- Developer Closure Inventory: `127/127 development_status=완료`
- Verification: `7 완료 / 120 미검증` — 필수 실환경 Acceptance를 정적 PASS로 대체하지 않음.

## 실제 실행 결과

| 검증 | 실제 결과 | 판정 |
|---|---:|---|
| DB pytest | 132 passed / 2 environment skipped | PASS_WITH_ENV_SKIPS |
| Verification tests split | 44 passed + 36 passed = 80 passed | PASS |
| Testing Tools split | 392 passed / 22 environment skipped / 2 subtests passed | PASS_WITH_ENV_SKIPS |
| Release/Open Git tests | 53 passed | PASS |
| Generator tests | 47 passed / 10 environment skipped / 6 subtests passed | PASS_WITH_ENV_SKIPS |
| Migration lifecycle unit regression | 41 passed | PASS |
| Logging/Batch new regressions | 5 passed + signed trust 7 passed | PASS |
| Performance broker/batch/resource self-test | 3/3 | PASS |
| NXT3 stage-equivalent | 23 stages individually PASS | PASS_STAGE_EQUIVALENT |
| NXT3 Layout | 87 checks / 0 fail | PASS |
| NXT3 Garbage Sweep | decision 1129 / delete 301 at gate time / failures 0 | PASS |
| NXT3 Hygiene | protected delete 0 / directory delete 0 | PASS |
| `cpf verify all` | member/external/backoffice verification PASS | PASS |
| Clean Source Tree | files 10,154 / garbage 0 / empty dirs 0 | PASS |
| Windows Path | max relative 199 / max full 222 / failures 0 | PASS |
| Spring Java Hygiene | 2,449 main Java / failures 0 | PASS |
| VSCode Source Model | source-model errors 0 | PASS_STATIC_ONLY |
| Integrated Logging Closure | failures 0 | PASS_STATIC_ONLY |
| ADM E2E contract | routes 68 / browsers 5 / statuses 7 | PASS_STATIC_ONLY |
| ADM interaction | 80 capabilities / coverage 80 | PASS_STATIC_ONLY |
| ADM route consumers | routes 68 / missing components 0 / operations 336 | PASS_STATIC_ONLY |
| Frontend Consumer Closure | 566 files / 797 imports / findings 0 | PASS_STATIC_ONLY |
| ADM dangerous approval | 18/18 | PASS_STATIC_ONLY |
| Batch no-remote-Kafka | scanned 3,681 / errors 0 | PASS_STATIC_ONLY |
| Batch approval/fencing/executor/ghost/UNKNOWN | all invoked gates PASS | PASS_STATIC_ONLY |
| Batch standalone/profile | 5 roles / 20 shells / 15 profiles / 35 checks | PASS_STATIC_ONLY |
| Generator Full Contract | profiles 5 / DB vendors 3 / supported bindings PASS | PASS_STATIC_ONLY |
| Generator Lifecycle | fresh/idempotent/user-owned protection/remove plan/direct delete reject/restore/upgrade PASS | PASS_STATIC_ONLY |
| DB Migration Lifecycle | migrations 290 / rollback 235 / forward recovery 55 / sourceSha UNAVAILABLE / Working Tree SHA-256 bound | PASS_STATIC_ONLY |
| Release target signed trust | 64-hex canonical PASS; mismatch/missing/tamper/artifact mismatch fail-closed; legacy 40 explicit compatibility PASS | PASS |

### 통합 Runner Timeout 처리

`pytest` 전체 통합 호출과 NXT3 monolithic 호출은 assistant executor 240초 제한으로 중간 종료된 적이 있다. 이를 PASS로 기록하지 않았다. 동일 범위를 작은 독립 묶음으로 다시 실행하여 위 실제 결과를 수집했다.

## 이번에 닫은 주요 False Green / Root Cause

- Java compile 선행 blocker가 JDT missing type를 연쇄 증폭하던 문제를 실제 Source 원인과 Build-path 2차 증상으로 분리.
- Logging MDC key와 File pattern/structured writer lineage 불일치 수정.
- 존재하지 않는 EDU logging Runtime probe 제거.
- Security verifier의 과거 변수명 고정 False Red 제거.
- Execution Scope의 external Temp Evidence `relative_to(root)` False Red 제거.
- Generator Template stale import로 MBR/EXS idempotency가 깨지던 문제를 Generator Owner에서 수정.
- DB lifecycle Git HEAD 강제를 Working Tree SHA-256 authoritative provenance로 보정.
- Performance Live가 64-hex Working Tree SHA-256을 40-hex Git HEAD 규칙으로 거부하던 결함 수정 및 signed attestation 공격 테스트 추가.
- Product Source Identity가 Canonical Batch role `bin/` Shell을 누락하지 않도록 source-state 정책 회귀 추가.

## BLOCKED_EXTERNAL / 필수 미검증

### BE-01 — Java25 / Gradle9.1 / VSCode
- 현재 실행환경: Java21, Gradle 9.1 distribution cache 없음, Windows VSCode 없음.
- 필수: clean root build/test/publication/SBOM, generated domain build, Fresh VSCode Gradle/JDT Sync, Problems Error 0 / Warning 0.

### BE-02 — DB3 / Batch / One-WAS / Logging / Browser / Performance
- 현재 실행환경: Windows PowerShell7/Docker/Oracle/PostgreSQL/MariaDB/Browser live environment 없음.
- 필수: DB3 physical lifecycle, 5 Batch roles + Worker×2 kill/UNKNOWN/reconcile, One-WAS, File↔DB↔ADM real correlation, Runtime OpenAPI, Browser E2E/a11y, signed Performance Live, Fresh Replay.

### BE-03 — Open Git Actual Fresh Binary Release
- Projection과 정책/테스트는 PASS했으나 Java25 Fresh framework publication + fresh remote clone-equivalent Golden Path는 미검증.

미실행 결과는 PASS로 기록하지 않는다.
