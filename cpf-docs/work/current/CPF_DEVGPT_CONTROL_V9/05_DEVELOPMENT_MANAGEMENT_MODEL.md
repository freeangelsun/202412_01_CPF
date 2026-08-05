# 개발 관리 상태 모델

## 관리 Universe

- `WORK_PACKAGE`: 775
- `BASELINE_STABILIZATION`: 28
- `REQUIREMENT_GAP`: 24
- 총 개발 관리 항목: 827

## 개발GPT 작업대상 상태

| 상태 | 개발 요청 | 의미 |
|---|---:|---|
| 작업 대상 | 포함 | 미수행 또는 자체검수 미완료 |
| 재개발 대상 | 포함 | QA 결함 또는 구현 무효화 |
| 재검수 대상 | 포함 | 구현은 남아 있으나 Evidence/회귀/영향 재검증 필요 |
| 완료 스킵 | 제외 | 개발GPT 수행·자체검수·Evidence·SHA 충족. QA 전체 완료와 별개 |
| 해당 없음 스킵 | 제외 | 제품 범위와 무관하며 근거·재개 조건 존재 |
| 소유권 검토 | 보류 | Owner/Consumer/Canonical 통합 결정 필요 |
| 외부환경 차단 | 보류 | 외부 권한·Secret·장비·운영 승인 필요 |

`DEVELOPMENT_ITEM_STATE.csv`에는 개발GPT 소유 컬럼만 둔다. QA와 Codex 상태는 QA 지정 단일 원장에서 관리하며, V8에는 `QA_REOPEN_FEED_TEMPLATE.csv` 형식으로 재개방 결과만 입력한다.

## 완료 스킵 계산

다음 조건을 모두 충족해야 한다.

- `개발GPT_수행상태=완료`
- `개발GPT_자체검수상태=완료`
- `evidence_valid=true`
- `개발GPT_완료기준SHA` 존재
- `qa_reopen_action` 없음
- `impact_invalidated=false`

하나라도 깨지면 다음 개발 요청에 다시 포함한다.
