# OPEN ISSUES — C 개발/QA 관리_22 Final External Acceptance

Source/static implementable work is closed on Fresh Replay V3. There are no remaining implementable source/static Findings. The following are mandatory external/live acceptance items and are deliberately not reported as PASS.

## BLOCKED-EXTERNAL-001 — Java25 full Gradle and Public Binary

- State: `BLOCKED_EXTERNAL / 미검증`
- Required: final applied Source on Java25; root `clean build` with Generated Domains included; publication/isolated public consumer against reachable artifact repository.
- Pass: `BUILD SUCCESSFUL`, ExitCode `0`, failed task `0`, failed test `0`, public consumer resolves without `mavenLocal` or private Source.
- Re-run command: see `TEST_AND_EVIDENCE.md`.

## BLOCKED-EXTERNAL-002 — Live DB3 / Multi-instance / Browser Runtime

- State: `BLOCKED_EXTERNAL / 미검증`
- DB3: Oracle/PostgreSQL/MariaDB fresh install → migration → seed → runtime query → upgrade → rollback; include mixed-vendor Domain DB binding/Public Workspace provisioning.
- Runtime: same-host two-process explicit instance identities, collision fail-close, process kill, lease expiry, restart/reconcile and UNKNOWN recovery.
- Browser: ADM/Backoffice Chromium/Firefox/WebKit; responsive widths; login/session restore/RBAC/approval/audit/logout and 401/403/404/409/429/500/503 paths.

No source implementation is deferred to these external items. If external execution exposes a source defect, reopen the same Root Cause Work Package or create a new Finding and re-run the Canonical Final Gate.
