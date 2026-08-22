# OPEN ISSUES — C 개발/QA 관리_1 Final External Acceptance

Source-side development is closed (`13/13 CLOSED`) on Source Identity `4572fd3659d076f230cbe2aa0284a5835a4f914e1f0a0cb4823b20c53b724886`. No implementable Source/static Finding is intentionally deferred. Overall product completion remains blocked by mandatory live/external acceptance.

## EA-01 — Mandatory external/live acceptance

State: `BLOCKED_EXTERNAL / 미검증`

### A. Java25 Root Gradle / Publication
- Execute final applied Source with Java25.
- `clean build --continue --stacktrace -PcpfIncludeGeneratedDomains=true`.
- PASS: `BUILD SUCCESSFUL`, ExitCode 0, failed task 0, failed test 0.
- Current assistant environment is Java 21.0.11 and cannot download Gradle 9.1.0 because `services.gradle.org` DNS is unavailable.

### B. Public Binary isolated consumer
- Resolve CPF artifacts from the intended reachable artifact repository.
- No `mavenLocal`, private Source composite or accidental workspace leakage.

### C. Live DB3
- Oracle/PostgreSQL/MariaDB: fresh install → migration → seed → runtime query → upgrade → rollback.
- Include mixed-vendor Domain DB binding/Public Workspace provisioning.

### D. Multi-instance / process failure
- Same-host two-process explicit identities.
- collision fail-close, process kill, lease expiry, restart/reconcile and UNKNOWN recovery.

### E. Browser
- ADM/Backoffice Chromium/Firefox/WebKit.
- responsive widths, login/session restore, RBAC, approval, audit, logout and 401/403/404/409/429/500/503.

### F. Windows DX
- PowerShell supported-version entrypoints.
- VS Code fresh Gradle import, Java index, generated source index and Problems.

If any external execution exposes a Source defect, reopen the same Root Cause Work Package (or create a new one), implement the fix, and repeat Development Final Gate + Fresh Replay.
