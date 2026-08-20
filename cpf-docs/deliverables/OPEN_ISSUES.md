# OPEN ISSUES — Final Environment Revalidation

All source/static Canonical gaps tracked in the 2026-08-20 development cycle were implemented and rechecked on the fresh applied snapshot. The remaining items are environment/runtime acceptance or product-policy decisions; they are not hidden as source PASS.

## OPEN-RUNTIME-001 — Java25 final full build
- State: `미검증`
- Reason: the sandbox cannot download the Gradle 9.1 distribution; the latest user-local full build is the pre-fix 9-failure log.
- Re-run: use the Tee-Object command in `TEST_AND_EVIDENCE.md`.
- Pass: `BUILD SUCCESSFUL`, ExitCode `0`, Failed Tasks `0`.

## OPEN-RUNTIME-002 — DB3 live lifecycle
- State: `미검증`
- Static/render status: Oracle/PostgreSQL/MariaDB canonical source/bundle parity PASS.
- Required live evidence: fresh install, migration, seed, runtime query, upgrade and rollback for each official vendor.

## OPEN-RUNTIME-003 — Multi-instance failure/recovery
- State: `미검증`
- Source/static status: instance fencing, lease/reconcile/UNKNOWN/recovery contracts are implemented and statically tested.
- Required live evidence: same-host two process, explicit MBR01/MBR02, duplicate implicit identity fail-close, process kill, lease expiry, restart/reconcile.

## OPEN-RUNTIME-004 — Browser E2E
- State: `미검증`
- Source/static status: ADM generated client/consumer, route/menu/RBAC/System6/commercial-page contracts PASS.
- Required live evidence: Chromium/Firefox/WebKit, 390/768/1280+ widths, login/session restore, role menu/API permission, query/mutation/approval/audit/logout and 401/403/404/409/429/500/503 paths.

## OPEN-RUNTIME-005 — Public Binary end-to-end resolution
- State: `미검증`
- Source/static status: empty Public Workspace, public templates, isolated consumer harness and publication contracts PASS.
- Required live evidence: reachable binary repository + isolated `GRADLE_USER_HOME`, no `mavenLocal`, no private source/repository.

## OPEN-RUNTIME-006 — Windows PowerShell runtime
- State: `미검증`
- Source/static status: PowerShell 5.1 `Path.GetRelativePath` incompatibility was removed from delete-manifest utility; cross-platform source contracts pass.
- Required evidence: execute final apply/delete/verification commands in Windows PowerShell 5.1 target host.

## OPEN-POLICY-001 — Commercial GA policy
- State: `재확인 필요`
- This is not a source defect. Final GA/edition/license/support policy requires the designated product/legal/QA decision and must not be fabricated by Developer GPT.
