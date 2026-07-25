# CPF R10 Cross-PC Handover

기준 원격 Commit: `7dcccafe4445c10a148a7f45473de25c396aebd3`

## 1. 이 문서의 역할
이 파일은 R10 checkpoint 인수인계다. 제품 최종 정본은 `CPF_FINAL_TARGET_REQUIREMENTS.md`, 현재 작업 지시는 `CPF_CURRENT_WORK_REQUEST.md`, 장기 결정은 Decision Log가 우선한다. 회사 PC/집 PC/Codex/ChatGPT는 채팅 기억만으로 이어서 작업하지 않는다.

## 2. R9 실제 Git과 보고 차이
- R9 보고에는 Core Batch Legacy 물리 제거가 기록됐지만 실제 master에는 `cpf-core/common/batch`와 Batch/Center-Cut AutoConfiguration이 남아 있었다.
- ADM lazy route는 `features/logs/LogsPage.vue`를 참조했지만 실제 파일이 누락돼 있었다.
- `BatBatchScheduleService`와 ADM Batch API가 BAT 자체 영업일 저장소/계약을 계속 사용하고 있었다.
- R10 APPLY가 package move/import rewrite/test move/legacy delete와 위 회귀를 실제로 교정한다.

## 3. R10 구현 범위
### Generated Domain / EXS
- EXS baseline-free. 검증 때 공식 Generator로 `external/EXS` create→verify→remove.
- `sync-generated-domain-artifacts.ps1`는 `Database`와 `AllGeneratorOwned` Scope를 제공한다.
- 사용자 수정 generator-owned 파일은 자동 덮어쓰지 않고 fail-closed.
- Golden Generator의 Paging 예제는 Core `CpfPageRequest`/`CpfSlice`를 사용하도록 변경.

### Core Foundation
- 목적별 `Cpf*` Utility, immutable Page/Slice/Cursor/Sort 계약 추가.
- HMAC-SHA256 `CpfCursorCodec/CpfHmacCursorCodec`으로 외부 Cursor 위변조 검증 기반 제공.
- transactionId Public API와 34자리 canonical validation 제공. sequence는 7자리로 fail-fast.
- Header literal 재생산 대신 `CpfHeaders`/Core Header Engine 사용.

### CMN Business Calendar
- 제품 Owner는 `cpf-common`.
- MariaDB canonical table: `cmn_business_calendar_day`.
- ADM `/adm/api/business-calendars` + 관리 UI 제공.
- BAT Scheduler/Simulation은 `CmnBusinessCalendar`만 소비하고 BAT 전용 Calendar API/Table은 제거.
- MySQL/PostgreSQL/Oracle/SQL Server도 동일 logical DDL contract를 보유하되 미지원 Platform pack은 지원 완료로 오판하지 않는다.

### Logging / Batch Trace
- 기존 File Log 경로 `<env>/<module>/<instance>/transactions/<date>/<transactionId>` 정책을 유지.
- ADM DB Log에서 transactionId/module/WAS/serverInstance/host/trace를 교차 조회.
- DB 내부 오류 원문은 ADM 응답에 노출하지 않는다.
- Batch 실행 API/UI에서 Job/transactionId/Spring Job Instance/Worker/Server Instance를 교차 검색.
- `cpf_transaction_log`와 `bat_execution`에 운영 검색용 instance index 추가.

### Core→BAT Ownership
- R10 APPLY가 Core `common.batch` Runtime을 물리 삭제한다.
- topology-independent Batch/Center-Cut DTO/Port는 `core.api`/`core.spi`로 이동.
- Runtime class/test는 BAT가 소유.
- Legacy package/import가 남으면 R10 Gate 실패.

## 4. 문서/검증/가비지 정책
- 작업 시작: `check-work-context.ps1`로 Final Target→Continuity Ledger→Current Request→Decision Log→Continuity State→latest Handover를 확인.
- DB/Generator 변경: canonical source + migration/rollback + all vendor contract + existing Generated Domain drift까지 같이 처리.
- 통합 검증: `CPF_INTEGRATED_VERIFICATION_PLAN.md`에 누적 후 `verify-full-product.ps1` 한 번 실행.
- 실행하지 않은 Gradle/MariaDB/Browser/Multi-instance는 `미검증`.
- Root log/zip/tmp/bak, stale UI/package, `cpf-external`, legacy DB source는 cleanup gate 대상.

## 5. 다음 PC 첫 명령
`pwsh -ExecutionPolicy Bypass -File .\cpf-tools\scripts\check-work-context.ps1 -Root "C:\dev\projects\jck\202412_01_CPF"`

R10 ZIP 적용 후에는 즉시 commit/push하지 말고 `git status --short`, `git diff --check`, R10 Static Gate를 확인한다. 통합 Runtime 검증은 사용자 요청에 따라 개발 누적 후 한 번 수행한다.
