# CPF Verification Gate Lifecycle 정책

- 중앙 정책 현행화 기준 SHA: `4870b20733875c3955f93846307fa5041e6f6c22` (`07_06`)
- 목적: QA 회차별 Gate가 영구 누적돼 최종 검증 경로를 파편화하지 않도록 한다.

## 1. 현재 검증 진입점

Final 검증은 날짜·QA 번호가 없는 안정된 진입점으로 수렴한다.

현재 Repository에서 확인된 Workflow:

- Static/Build/Frontend/DB/Supply-chain Required Gate: `.github/workflows/cpf-final-source-gates.yml`
- R6 exact-SHA Release Qualification: `.github/workflows/cpf-r6-release-gates.yml`
- Legacy current workflow: `.github/workflows/cpf-qa39-final-required.yml`

Local stable scripts:

- `cpf-tools/scripts/verify-cpf-final-completion.ps1`
- `cpf-tools/scripts/invoke-cpf-final-closure.ps1`
- `cpf-tools/scripts/verify-full-product.ps1`

Final Release 판정은 한 Workflow의 성공만으로 완료하지 않는다.
현재 Final Control의 Runtime 13축과 exact-SHA Evidence를 함께 만족해야 한다.

## 2. Legacy Gate 분류

QA 번호나 날짜가 붙은 Gate는 다음 중 하나로 분류한다.

| 상태 | 의미 |
|---|---|
| ACTIVE_DEPENDENCY | 현재 안정 진입점/Workflow가 직접 호출하므로 삭제 금지 |
| MIGRATION_REQUIRED | 현재 호출은 있으나 안정된 이름으로 이관해야 함 |
| COMPATIBILITY_WRAPPER | 안정 Gate로 위임하는 임시 호환 Wrapper |
| HISTORICAL_ONLY | 실행 경로에서 제거됐고 Git History로 충분 |
| DELETE_CANDIDATE | 참조 0, Consumer 0, Evidence 의존 0 확인 후 삭제 가능 |

## 3. current SHA에서 확인된 Legacy 의존

- `cpf-final-source-gates.yml`은 `cpf-tools/verification/qa39/**`와 `cpf-tools/verification/20260729_04/**`를 아직 직접 호출한다.
- `cpf-qa39-final-required.yml`은 QA39 Gate와 QA34 DB Runtime Matrix를 아직 직접 호출한다.
- `cpf-r6-release-gates.yml`은 `cpf-tools/verification/final-dev/run-r6-release-gates.ps1`을 exact SHA와 함께 호출한다.
- 과거 정책에 기재된 `.github/workflows/cpf-qa37-source-closure.yml`은 현재 Workflow 목록에 없으므로 Current 진입점으로 사용하지 않는다.

따라서 이름이 오래됐다는 이유만으로 Product Verification Script를 삭제하지 않는다.
현재 Consumer를 stable Final Gate로 이관하고 동일성 검증 후 exact Delete Manifest에 넣는다.

## 4. 이관 절차

1. 기존 Gate가 검증하는 Requirement와 실패 조건을 Inventory로 만든다.
2. 중복 Gate를 하나의 안정된 Gate로 병합한다.
3. 기존 경로는 필요한 경우에만 Compatibility Wrapper로 전환한다.
4. CI·Local·Codex와 Product Consumer를 stable 경로로 이관한다.
5. 기존/신규 Gate의 결과 동등성과 False-Green mutation을 검증한다.
6. 참조 0, Consumer 0, Evidence 독립성을 확인한 뒤 중앙/제품 Owner Delete Manifest에 넣는다.
7. Final Developer는 현재 Final Source Completion에서 가능한 Legacy Gate consolidation을 수행하고 결과를 자기 Evidence에 기록한다.

## 5. 금지

- QA 회차마다 새 Final Gate 진입점 추가
- 과거 Gate를 새 Gate가 호출하는 다단 Wrapper Chain
- Marker·파일 존재·boolean self-attestation만 확인하고 Runtime Closure로 판정
- 활성 Workflow가 호출하는 날짜/QA Folder를 가비지로 임의 삭제
- 현재 exact SHA와 무관한 과거 Evidence를 PASS로 승계
