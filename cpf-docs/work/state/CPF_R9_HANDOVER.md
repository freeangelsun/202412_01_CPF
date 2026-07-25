# CPF R9 작업 인수인계

## 기준
- Repository: `https://github.com/freeangelsun/202412_01_CPF`
- Branch: `master`
- 작업 시작 기준 SHA: `f1d85cf087e2a16038b21f6c53ac29204d164124`
- R9는 ChatGPT가 만든 overlay/apply package이며 commit/push를 수행하지 않는다.
- 사용자가 적용 후 직접 commit/push한다.

## 반드시 유지할 제품 정책
1. EXS는 fixed platform module이 아니라 `external/EXS` Generated Domain이다.
2. Batch/Scheduler/Agent/Worker/Center-Cut Runtime Owner는 `cpf-batch`다.
3. Core에는 topology-independent API/SPI/primitive만 둔다.
4. ADM은 Owner Module DB를 직접 변경하지 않는다.
5. 개발단계이므로 잘못된 Legacy는 대안 구현 후 물리 제거한다. 잘못된 migration도 GA 전에는 정본 기준으로 교정한다.
6. ADM/BZA frontend는 feature 단위 lazy package + local runtime assets만 사용한다.
7. 실행하지 않은 검증은 완료로 기록하지 않는다.

## R9 핵심 변경
- Core Batch/CenterCut legacy Runtime/AutoConfig/Test 물리 정리 및 API/SPI 이동.
- BAT Runtime 누락 import/helper 교정.
- Generated Domain remote Center-Cut 표준 handler/transport 추가.
- ADM Incident/Maintenance/Break-glass Control Plane 및 제품형 UI.
- BZA 조직/결재 제품형 UI + withdraw/cancel/resubmit/expire lifecycle.
- CMN Calendar/Template DB-less extension.
- Tenant/DB read-write routing/Lineage/Self-healing/Capability 기반.
- ServiceCallEngine Lineage actual hook.
- DB canonical source old→vendor merge 후 old source 삭제.
- V6 fixed EXS/BIZADM migration 제거/rename, V29 current BZA baseline 재생성, V42~V44 추가.
- Root compose 및 coarse frontend garbage 제거.

## 검증 상태
- Static syntax/structure 검사는 작업 중 반복 수행.
- PowerShell APPLY는 현재 ChatGPT container에 pwsh가 없어 직접 실행하지 못했다.
- Gradle/npm/MariaDB/runtime/browser/multi-instance/fault/backup-restore는 사용자 요청에 따라 아직 실행하지 않았다.
- 따라서 R9 Requirement는 `완료`로 올리지 않는다.

## 다음 작업 방식
1. R9 ZIP 적용.
2. `git status --short`로 삭제/이동/생성 포함 확인.
3. checkpoint push.
4. 필요하면 남은 P0 구현을 추가 누적.
5. 구현 범위를 충분히 닫은 뒤 `verify-full-product.ps1 -RequireAll` 한 번으로 통합 실행.
6. 실패 항목은 Requirement ID별 원인/수정/Evidence로 연결.
7. 최종 PASS 뒤에만 완료 상태 재산정.

## 최종 통합 검증 핵심 대상
- canonical DB source migration과 R6/V29/V42~44
- Core Batch legacy 0 / BAT owner runtime compile
- ADM/BZA frontend build/browser
- EXS Generator lifecycle
- local/remote service call and remote Center-Cut
- UNKNOWN/reconciliation/Saga/DLQ recovery
- multi-instance Batch/Worker/CenterCut
- security/break-glass/approval lifecycle
- evidence SHA/profile/time/sanitization
