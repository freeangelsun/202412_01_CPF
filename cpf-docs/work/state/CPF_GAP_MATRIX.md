# CPF GAP Matrix — R8 적용 대기 상태 (2026-07-25)

기준: master `512e5f2c7f32ba21ef6be570b2efa3dbcbd7a482` + R8 overlay. 실제 Runtime/Evidence 없는 항목은 완료 처리하지 않는다.

| 우선순위 | Gap | 상태 | R8 조치 | 다음 완료조건 |
|---|---|---|---|---|
| P0 | R7 cleanup/move 미반영 | 부분 구현 | APPLY로 old `db/source`, Root compose, coarse UI, legacy ADM Member/Scheduler 잔재 삭제·이동 | APPLY 후 `check-r8-cleanup` + `git diff --check` PASS |
| P0 | ADM cross-owner Batch/Center DB | 부분 구현 | `CpfBatchOperationsPort`, `CpfCenterCutOperationsPort`, BAT Owner Facade, ADM Remote Adapter, REF Extension 경계 | Local/Remote runtime + failure/UNKNOWN Evidence |
| P0 | Core Batch Runtime ownership | 부분 구현 | BAT runtime/scheduler/CenterCut Runner 추가, BAT consumer import 전환, Core legacy auto-config default OFF | 전체 consumer compile 후 Core legacy runtime class 물리 제거 여부 확정 |
| P0 | DB migration integrity | 실패 | V39/V40/V41 checksum ledger 정리, 신규 migration만 정상화; historical V6/V29 미수정 | Git history/적용이력 기준 원인 확정 후 reviewed migration policy |
| P1 | ADM Approval | 부분 구현 | policy/participant snapshot/ALL·ANY·N_OF_M/idempotency/Owner Command/UNKNOWN/UI, actor fail-closed | break-glass/expiry/recovery + concurrency/runtime/browser Evidence |
| P1 | BZA Org/Approval | 부분 구현 | position/job-title/multi-assignment/responsibility/multi-role/policy/delegation/simulation/snapshot/UI | 조직개편/부재/취소/재상신/만료/상향 + browser Evidence |
| P1 | Saga compensation/manual | 부분 구현 | durable forward/compensation/manual retry/manual resolution/audit + V40 + unit test | Spring/JDBC integration + fault/unknown recovery Evidence |
| P1 | Center-Cut Runner | 부분 구현 | BAT registry/runner/same-job lock/stop/rate/last-run + Owner query facade | lease/fencing/global TPS/multi-instance/failure/reprocess Evidence |
| P1 | Generated EXS lifecycle | 부분 구현 | fixed residue 제거 후 Golden Generator `external/EXS` create/verify | DB/bootstrap/build/runtime/remove/regenerate parity Evidence |
| P1 | Multi Vendor | 부분 구현 | canonical ownership을 `vendor/<vendor>` 계약으로 통일, unsupported vendor fail-closed | MySQL/PostgreSQL/Oracle/SQL Server platform install/migration/runtime parity |
| P1 | Evidence | 미검증 | Full verifier가 command/start-end/SHA/profile/output/results를 sanitized Evidence로 저장 | 사용자 환경 `-RequireAll` PASS Evidence commit |
| P1 | REF/Generated Center-Cut remote extension | 부분 구현 | REF가 자기 DB를 읽는 Extension SPI | 분리 WAS에서 generic extension registration/remote routing 구현·Evidence |
| P2 | Multi Tenant | 미구현 | 이번 R8 직접 구현 없음 | tenant isolation/ops/security/migration architecture + implementation |
