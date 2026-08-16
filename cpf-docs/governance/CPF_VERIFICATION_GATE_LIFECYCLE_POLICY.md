# CPF Verification Gate Lifecycle 정책

## 1. 목적

검증 경로가 날짜·QA 회차·개발 캠페인 이름으로 계속 증식하지 않도록 **안정된 current entrypoint와 실제 Consumer**를 기준으로 관리한다. current Repository에는 지금 실행되는 검증과 현재 SHA Evidence만 남긴다. History 보존에 의존하지 않고 삭제 전 필요한 실패/결정/재검증 조건을 Current Evidence와 Handover에 흡수한다.

## 2. Current stable entrypoint

### CI

- `.github/workflows/cpf-final-source-gates.yml`
  - Current static/source/config/generator/query/hygiene
  - Java 25 build/test
  - ADM/BZA frontend verify
  - DB static/vendor lifecycle
  - Supply-chain
  - exact checkout/self-dirty protection

### Local / Release

- `cpf-tools/verification/nxt3/run_nxt3_final_all.py` — 저비용·독립 정적 Gate 누적 실행
- `cpf-tools/verification/tools/verify-full-product.ps1` — Build/Test/DB/Generator/Frontend/Browser 선택 통합
- `cpf-tools/verification/tools/verify-cpf-final-completion.ps1` — clean exact-SHA Final Source/Runtime Evidence 집계
- `cpf-tools/release/tools/verify-cpf-release-completion.ps1` — 3 DB + Browser exact-SHA Evidence를 포함하는 Release 완료 판정

Runtime/DB/Browser/Fault 검증을 실행하지 않은 경우 `SKIPPED/READY`를 PASS로 변환하지 않는다.

## 3. Helper Consumer 규칙

개별 verification/helper는 다음 중 하나 이상의 실제 Consumer를 가져야 한다.

- current GitHub Workflow
- Root Gradle task
- stable local integration script
- 공식 Runbook/Developer workflow
- 독립 Runtime/fault harness

Consumer가 없고 동일 검증이 stable Gate에 병합된 날짜/QA/Revision/migration/currentizer는 `REMOVE_CANDIDATE`다.

## 4. Tool Hygiene 판정

| 상태 | 의미 |
|---|---|
| `KEEP_CANONICAL_GATE` | current stable entrypoint 또는 독립적으로 필요한 검증 |
| `MERGE_INTO_CANONICAL_GATE` | 기능은 유효하나 stable Gate로 통합할 대상 |
| `RENAME_CURRENT` | 기능은 유효하지만 과거 캠페인 이름이 current 의미를 왜곡 |
| `REMOVE_CANDIDATE` | Consumer 0 / 대체 Gate 존재 / 필요한 현재 의미가 Canonical Gate·Evidence에 흡수됨 |

삭제 전에 현재 Consumer를 stable 경로로 전환하고 stale-reference 0을 확인한다.

## 5. Generated Customer Domain Gate 원칙

Generated Project 내부의 영구 ownership/manifest/lock을 lifecycle 정본으로 검사하지 않는다.

- 입력 정본: `cpf-tools/generator/definitions/*/cpf-domain.yaml` 또는 명시 `--file`
- Engine: `cpf-tools/generator/engine/cpf_domain_generator.py`
- CLI: `cpf-tools/runtime/cli/cpf.py`
- lifecycle verifier: `cpf-tools/generator/verification/verify-cpf-generator-lifecycle.py`
- 통합 Generator Gate: `cpf-tools/verification/nxt3/cpf_nxt3_generator_gate.py`
- transient state: `build/domain-generator/verification/**`

과거 Generated Project manifest를 요구하는 Gate는 current contract로 이관하거나 제거한다.

## 6. 금지

- QA 회차/날짜마다 새 Final Gate entrypoint 추가
- 과거 Gate를 새 Gate가 다시 호출하는 Wrapper chain
- 존재/Marker/boolean self-attestation만으로 Runtime Closure 판정
- 삭제된 과거 Gate를 Workflow/Guide가 계속 호출
- 현재 exact SHA와 무관한 과거 Evidence PASS 승계
- verification/currentizer가 제품 Source를 자동 변환하여 partial state를 만드는 방식
