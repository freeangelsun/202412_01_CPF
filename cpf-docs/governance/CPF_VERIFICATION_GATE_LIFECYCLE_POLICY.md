# CPF Verification Gate Lifecycle 정책

- 기준 SHA: `1eda8e12fe123281748a4388938c62f11819da1e`
- 목적: QA 회차별 Gate가 영구 누적돼 최종 검증 경로를 파편화하지 않도록 한다.

## 1. 안정된 진입점

최종 검증은 날짜·QA 번호가 없는 안정된 진입점으로 수렴한다.

- CI: `.github/workflows/cpf-final-source-gates.yml`
- Local Final: `cpf-tools/scripts/verify-cpf-final-completion.ps1`
- Evidence Wrapper: `cpf-tools/scripts/invoke-cpf-final-closure.ps1`
- Product Full Verify: `cpf-tools/scripts/verify-full-product.ps1`

## 2. QA·날짜 Gate 처리

QA 번호나 날짜가 붙은 Gate는 다음 중 하나로 분류한다.

| 상태 | 의미 |
|---|---|
| ACTIVE_DEPENDENCY | 현재 안정 진입점이나 Workflow가 직접 호출하므로 삭제 금지 |
| MIGRATION_REQUIRED | 현재 호출은 있으나 안정된 이름으로 이관해야 함 |
| COMPATIBILITY_WRAPPER | 한 Release 동안 새 Gate로 위임하는 얇은 Wrapper |
| HISTORICAL_ONLY | 실행 경로에서 제거됐고 Git History로 충분 |
| DELETE_CANDIDATE | 참조 0, Consumer 0, Evidence 의존 0 확인 후 삭제 가능 |

## 3. 현재 확인된 Active Legacy

- `verify-cpf-final-completion.ps1`은 QA30·QA31 Gate를 직접 호출한다.
- `.github/workflows/cpf-final-source-gates.yml`은 `cpf-tools/verification/20260729_04/**`를 직접 호출한다.
- `.github/workflows/cpf-qa37-source-closure.yml`은 활성 QA37 검수 Workflow다.

따라서 이름이 오래됐다는 이유만으로 지금 삭제하면 Final Gate가 깨진다.

## 4. 이관 절차

1. 기존 Gate가 검증하는 Requirement와 실패 조건을 Inventory로 만든다.
2. 중복 Gate를 하나의 안정된 Gate로 병합한다.
3. 기존 경로는 새 Gate를 호출하는 Compatibility Wrapper로 전환한다.
4. CI·Local·Codex·Guide Consumer를 새 경로로 이관한다.
5. 한 Release 동안 결과 동등성을 검증한다.
6. 참조 0과 Evidence 독립성을 확인한 뒤 Delete Manifest에 넣는다.

## 5. 금지

- QA 회차마다 새 Final Gate 진입점 추가
- 과거 Gate를 새 Gate가 호출하는 다단 Wrapper Chain
- Marker·파일 존재만 확인하고 Runtime Closure로 판정
- 활성 Workflow가 호출하는 날짜 Folder를 가비지로 삭제
