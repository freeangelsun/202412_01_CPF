# CPF Development Management V8 — Review Index

## 기준

- Repository: `freeangelsun/202412_01_CPF`
- Branch: `master`
- 확인한 최신 `origin/master`: `faedf43a7baffdad456bf40f8e46d622db9cfc76` (`04_06`)
- 최상위 정본: `cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md`
- 이 패키지의 상태: **개발 관리·실행 패키지 생성 완료 / Repository 대형 원장 전수 Bootstrap 미실행**

## 이 패키지가 해결하는 것

1. 기존 169 Canonical Requirement를 대체하지 않고 775 Work Package로 실행한다.
2. 30,558 Requirement와 40,763 Scenario를 Repository의 실제 Split Part에서 읽어 Work Package에 전수 배정한다.
3. 28 Baseline Stabilization과 24 REQ-GAP 항목도 동일한 개발 상태 모델에서 관리한다.
4. 개발 완료 후보는 다음 개발 요청에서 `완료 스킵`하되, QA의 재개발·재검수 요청이나 영향 무효화가 들어오면 자동 재포함한다.
5. 개발 세션 수는 고정하지 않는다. Active Scope와 Owner/Dependency/충돌 경계를 기준으로 동적 생성한다.
6. 각 개발 세션은 중앙 State 원장을 직접 수정하지 않고 Session Result를 제출한다.
7. Architecture·Specification·Security·DB·Evidence 기준은 필수다. Worklist의 클래스·메서드·Library·세부 알고리즘은 구현 제안이며 동등 이상 대안을 허용한다.

## 실행 순서

```powershell
# 1. Root Overlay 적용 후 최신 master·Working Tree 확인
git fetch origin
git rev-parse origin/master
git status --short

# 2. 30,558/40,763 전수 Mapping과 검증
powershell -ExecutionPolicy Bypass -File cpf-tools/scripts/development-management/initialize-development-management.ps1

# 3. 동적 개발 요청 생성
powershell -ExecutionPolicy Bypass -File cpf-tools/scripts/development-management/generate-development-requests.ps1 `
  -CampaignId DEV-YYYYMMDD-R01 -MaxItemsPerSession 8

# 4. Session 결과 병합
powershell -ExecutionPolicy Bypass -File cpf-tools/scripts/development-management/merge-development-results.ps1 `
  -Results <session-result.csv>

# 5. QA 재개방 반영
powershell -ExecutionPolicy Bypass -File cpf-tools/scripts/development-management/apply-qa-reopen-feed.ps1 `
  -Feed <qa-reopen-feed.csv>
```

## 핵심 파일

- `DEVELOPMENT_ITEM_INDEX.csv`: 827개 개발 관리 항목 정본 Index
- `DEVELOPMENT_ITEM_STATE.csv`: 개발GPT 전용 상태 원장
- `ACTIVE_DEVELOPMENT_SCOPE.csv`: 현재 개발 요청 포함 View
- `COMPLETED_SKIP_SCOPE.csv`: 개발 요청 제외 View. 전체 QA 완료를 의미하지 않음
- `WORK_ITEM_SCOPE_SUMMARY.csv`: Requirement/Scenario 수량·검토 잔량·배정 Session 집계
- `FILE_OWNERSHIP_AND_CHANGE_POLICY.csv`: 병렬 세션 충돌 방지 규칙
- `WORK_ITEM_DEPENDENCY_GRAPH.csv`: 실행 순서와 통합 의존성
- `PROPOSED_REQUIREMENT_ADDITIONS.csv`: 24개 Gap 결정
- `REQUIRED_CANONICAL_PATCHES.csv`: Final Target 통제 변경 후보
- `DELETE_MANIFEST.csv`: 승인 전 삭제 금지 대상
- `cpf-tools/scripts/development-management/*`: 실제 생성·병합·재개방·검증 도구

## 판정 경계

- `완료 스킵`은 **개발GPT 수행·자체검수·Evidence·기준 SHA가 갖춰져 다음 개발 요청에서만 제외됨**을 뜻한다.
- QA `통과` 전에는 전체 `development_status=완료`, `verification_status=완료`로 판정하지 않는다.
- `FULL_ASSIGNMENT_VALIDATION.json`이 `PASS`가 되기 전에는 30,558/40,763 전수 배정 완료로 보고하지 않는다.
