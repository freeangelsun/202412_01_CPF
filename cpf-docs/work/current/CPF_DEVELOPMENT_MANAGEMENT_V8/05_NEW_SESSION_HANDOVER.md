# CPF Development Management V8 신규 세션 인수인계

## 1. 현재 기준

- 정식 명칭: **Core Platform Framework**
- Repository: `https://github.com/freeangelsun/202412_01_CPF`
- Branch: `master`
- 최상위 정본: `cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md`
- 개발 관리 정본: `cpf-docs/work/current/CPF_DEVELOPMENT_MANAGEMENT_V8/`
- 마지막 확인 SHA: `be03808e0bed7aa02c2978c5aa73b315359bbead`
- 마지막 확인 Commit: `04_07`
- V8 Package 생성 기준: `faedf43a7baffdad456bf40f8e46d622db9cfc76` (`04_06`)
- QA 혼합 제품 Patch 비교 시작점: `f97655c1299936a1101bc3ec10239265ec3b502e` (`04-03`)
- 확인 기준일: 2026-08-05, Asia/Seoul

과거 대화·완료 표시·이전 SHA를 자동 승계하지 않는다. 시작 시 최신 `origin/master`, exact SHA와 Working Tree를 다시 확인한다.

## 2. Repository 최상위 불변 규칙

**Repository 최상위에는 신규 파일이나 신규 디렉터리를 만들지 않는다.**

```text
문서·원장·Evidence → cpf-docs/**
관리·검증 Script → cpf-tools/**
제품 Source·Test·Config → 공식 Owner Module/**
Session 결과 → cpf-docs/work/current/development-session-results/**
```

Root Overlay는 Repository Root에서 해제할 수 있어야 하지만, ZIP 내부 항목은 반드시 기존 승인 디렉터리로 시작해야 한다.

루트 README, 임시 Manifest, 임시 Evidence, 작업용 디렉터리를 생성하지 않는다.

## 3. 현재 관리 Universe

```text
Canonical Requirement: 169
상세 Requirement 기대 수: 30,558
Scenario 기대 수: 40,763
실행 Work Package: 775
Baseline Stabilization: 28
Requirement Gap 관리항목: 24
개발 관리 대상 합계: 827
```

V8은 Canonical Requirement를 대체하지 않는다.

```text
Canonical
→ CPF-FR
→ CPF-SC
→ Work Package
→ Development Session
→ Test·Evidence
→ QA
```

## 4. 개발 GPT가 작업할 항목

다음 상태만 작업한다.

```text
작업 대상
재개발 대상
재검수 대상
```

다음 상태는 배정 범위에 들어 있어도 자동 제외하고 건드리지 않는다.

```text
완료 스킵
해당 없음 스킵
소유권 검토
외부환경 차단
```

`완료 스킵` 조건:

```text
개발GPT 수행 완료
개발GPT 자체검수 완료
Evidence 유효
완료 기준 SHA 기록
QA 재개방 없음
후속 영향 무효화 없음
```

`완료 스킵`은 다음 개발 요청에서만 제외된다는 뜻이다. QA 통과나 CPF 전체 완료가 아니다.

## 5. QA 재개방

개발 완료 후 QA는 최신 통합 Git에서 제가 수행하고, QA 결과 Root Overlay를 사용자에게 제공한다. 사용자가 Push하면 다음 개발 요청 생성 전에 QA Reopen Feed를 반영한다.

```text
REDEVELOP → 재개발 대상
REREVIEW → 재검수 대상
INVALIDATE_IMPACT → Evidence 무효화 후 재검수 대상
REOPEN_OWNER → 소유권 검토
EXTERNAL_BLOCK → 외부환경 차단
```

QA가 재개방하지 않은 `완료 스킵` 항목은 반복 개발·재검수하지 않는다.

## 6. 덮어쓰기와 파일 유실 방지

개발 GPT가 작업을 끝내고 Push한 뒤에도 과거 Campaign·Session 결과와 관리 정본이 사라지면 안 된다.

강제 규칙:

1. Campaign 요청 경로는 `generated/campaigns/<campaign-id>/REV-<nnn>/` 고유 경로를 사용한다.
2. 동일 경로가 이미 존재하고 비어 있지 않으면 생성기는 실패한다. 덮어쓰지 않는다.
3. Session 결과는 `cpf-docs/work/current/development-session-results/<campaign-id>/<session-id>/REV-<nnn>/`에만 추가한다.
4. 중앙 `DEVELOPMENT_ITEM_STATE.csv`, Requirement/Scenario Master, Governance 문서는 Session이 직접 수정하지 않는다.
5. 기존 제품 파일 수정 전 Baseline SHA와 대상 파일 사전 Hash를 Manifest에 기록한다.
6. Baseline 이후 다른 작업이 같은 파일을 변경했으면 해당 파일을 덮어쓰지 않고 Integration Owner에게 충돌 요청을 제출한다.
7. Public API/SPI, Root Build, DB Canonical, Generator, OpenAPI Source와 Generated Output은 전용 Integration Owner 정책을 따른다.
8. 같은 파일을 여러 Session이 수정하도록 배정하지 않는다.
9. 다른 세션 산출물이나 이전 Revision 파일을 삭제·이름 변경·이동하지 않는다.
10. 통합 후에도 Session Result와 Evidence는 Retention 정책이 승인될 때까지 보존한다.

## 7. 세션이 반드시 남길 파일

세션 전용 고유 경로에 다음을 생성한다.

```text
SESSION_ARTIFACT_MANIFEST.csv
SESSION_CLEANUP_COMMAND.ps1
DEVELOPMENT_SESSION_RESULT.csv
DEVELOPMENT_REQUIREMENT_RESULT.csv
DEVELOPMENT_SCENARIO_RESULT.csv
TEST_AND_EVIDENCE.md
OPEN_ISSUES.md
HANDOVER.md
```

`SESSION_ARTIFACT_MANIFEST.csv`는 세션이 생성·수정한 모든 경로를 기록한다.

필수 분류:

```text
PRODUCT_REQUIRED
EVIDENCE_RETAINED
SESSION_TEMPORARY
GENERATED_REGENERABLE
REJECTED_ARTIFACT
```

필수 정보:

```text
exact path
생성 또는 수정
기준 파일 존재 여부
사전 SHA-256
최종 SHA-256
통합 필요 여부
정리 가능 여부
정리 사유
정리 명령
```

## 8. 세션 종료 시 삭제 명령 의무

모든 개발 GPT 세션 인수인계에는 **그 세션 때문에 만들어진 정리 가능 파일을 한 번에 삭제하는 exact-path PowerShell 한 줄 명령**을 포함한다.

예시 구조:

```powershell
$paths=@("cpf-docs/work/current/development-session-results/<campaign>/<session>/REV-001/temp-a","cpf-docs/work/current/development-session-results/<campaign>/<session>/REV-001/temp-b"); $paths | ForEach-Object { if (Test-Path -LiteralPath $_) { Remove-Item -LiteralPath $_ -Recurse -Force } }
```

규칙:

- `SESSION_TEMPORARY`, `GENERATED_REGENERABLE`, `REJECTED_ARTIFACT` 중 사용자 승인 대상만 포함
- `PRODUCT_REQUIRED`, 통합된 Source, 필수 Test, 승인된 Evidence는 포함 금지
- 중앙 관리 원장, 다른 Campaign·Session 결과, Requirement·Scenario Master는 포함 금지
- 삭제 후보가 없으면 `정리 대상 없음`이라고 명시
- 제품 변경을 되돌려야 하면 삭제 명령이 아니라 `REVERSE_CHANGE_MANIFEST.csv`와 사용자 승인을 사용

## 9. 중앙 원장 충돌 방지

개발 세션은 중앙 상태 원장을 직접 수정하지 않는다.

```text
Session Result
→ merge-development-results
→ DEVELOPMENT_ITEM_STATE.csv
→ ACTIVE_DEVELOPMENT_SCOPE.csv
→ COMPLETED_SKIP_SCOPE.csv
```

Campaign 요청 생성도 동일 Campaign/Revision을 재사용하지 않는다. 기존 Assignment가 남아 있으면 새 Campaign 생성 전에 통합·종료 상태를 확인한다.

## 10. 전수 Mapping과 개발 시작

`FULL_ASSIGNMENT_VALIDATION.json=PASS` 전에는 30,558 Requirement와 40,763 Scenario의 전수 배정이 완료됐다고 보고하지 않는다.

```powershell
powershell -ExecutionPolicy Bypass -File cpf-tools/scripts/development-management/initialize-development-management.ps1
powershell -ExecutionPolicy Bypass -File cpf-tools/scripts/development-management/validate-development-management.ps1 -RequireFullAssignment
```

개발 요청 생성:

```powershell
powershell -ExecutionPolicy Bypass -File cpf-tools/scripts/development-management/generate-development-requests.ps1 `
  -CampaignId DEV-YYYYMMDD-R01 `
  -AssignmentRevision 1 `
  -MaxItemsPerSession 8
```

## 11. Git과 삭제 안전

사용자 승인 없이 Commit, Push, Branch, Tag, PR, Release, Reset, Restore, Stash, 삭제, Working Tree 정리를 하지 않는다.

실행·제안 금지:

```text
git clean
git reset --hard
git restore .
```

삭제는 exact path Manifest와 사용자 승인 후에만 수행한다.

## 12. 완료 경계

개발 GPT 완료 후보는 QA 통과가 아니다.

```text
개발 GPT 구현·자체검수
→ 사용자 통합·Push
→ QA 최신 Git 검수
→ QA 재개방 시 관련 항목만 다시 개발
→ QA 통과 후 최종 완료 판정
```

실제 Runtime 검증이 남아 있으면 전체 완료가 아니다.
