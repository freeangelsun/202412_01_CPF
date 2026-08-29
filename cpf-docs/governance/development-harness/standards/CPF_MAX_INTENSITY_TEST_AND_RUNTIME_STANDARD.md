# CPF 최대강도 Test·Runtime 검증 표준

## 원칙
개발 자체검수, Codex/Claude 독립검수, QA 검수, 사용자에게 요청하는 로컬 Runtime 검증 모두 동일하게 **대상 기능이 요구하는 최고 강도**를 사용한다. 빠른 Targeted Gate는 개발 중 피드백용일 뿐 최종 Acceptance 대체물이 아니다.

## 공통 Lifecycle
1. Source Identity / prerequisite / port / runtime dependency / secret availability 확인.
2. 필요한 경우 Fresh clean-room/격리 환경 준비.
3. Build/compile/unit/integration/contract/static/security 검증.
4. 실제 Runtime 기동 및 readiness.
5. 정상 E2E 거래와 결과/DB/로그/trace 확인.
6. 오류·경계·invalid input·permission·rate/timeout·dependency failure.
7. UNKNOWN, retry safety, probe/reconcile/recovery/compensation/rollback.
8. idempotency·concurrency·duplicate·multi-instance·process-kill/failover/takeover/fencing.
9. DB 영향 시 Oracle/PostgreSQL/MariaDB 각각 Fresh→Initializer/Migration→Seed→거래→Query/Constraint/Index/History→Upgrade→Rollback/Recovery→Reapply→재거래→Cleanup.
10. Generator 영향 시 create/setup/sync/remove/recreate, generated build/test/runtime, user-owned 보호, deterministic/idempotent diff.
11. API/Frontend 영향 시 OpenAPI sync/generated client, Browser E2E, 401/403/404/409/429/500/503, a11y, responsive, loading/empty/error/retry.
12. Batch 영향 시 5-role, 2-worker, kill/takeover/fencing, UNKNOWN/reconcile, restart/rerun/reprocess.
13. 성능 영향 시 warm-up/load/soak, resource leak, backpressure, timeout, error budget와 signed source identity.
14. 배포 영향 시 install/upgrade/rollback/public package/Open Git fresh release/leakage/SBOM/provenance.
15. Cleanup 후 **Fresh Replay**로 동일 결과 재현.

## 환경 부족
환경 부족은 검증을 약화하는 사유가 아니다. `BLOCKED_EXTERNAL`로 기록하고 Windows/Linux에서 동일 최대강도 Lifecycle을 실행할 명령, prerequisite, 예상 PASS/FAIL 기준, 필요한 Evidence를 생성한다. 필수 Runtime이 남으면 전체 완료 금지.

## Evidence 최소 필드
`work_item_id, role, source_identity, command, os, java, db/vendor, runtime_topology, started_at, ended_at, exit_code, pass_fail, fail_stage, observed_result, evidence_path, evidence_sha256, rerun_condition`.
