# Codex Independent Review Request

## Fixed baseline

`faedf43a7baffdad456bf40f8e46d622db9cfc76`

## Required review

1. Apply only `DEVGPT-6B` paths from `CHANGE_MANIFEST.csv` plus the manifest itself.
2. Confirm stale baseline/session Evidence, duplicate Log Policy contracts and 6E-owned DB files are absent from the Overlay.
3. Run Java 25 repository Gradle configuration, targeted tests, full build/test and publication checks.
4. Re-run the 63 product Gates or equivalent repository tests.
5. Verify actual Consumer paths for State, Lock, Resilience/Deadline, DB/File Log, Recovery, Trace, Masking, Masking Policy and Log Policy Version.
6. Verify UNKNOWN, idempotency, concurrency, fencing, retry/backoff, process kill, symlink, audit failure, approval/SoD and bounded-resource cases.
7. Review exact IDs in `CROSS_SESSION_CHANGE_REQUEST.csv`; do not close owner rows without their Source and runtime Evidence.
8. Record results only in Codex-owned columns/Evidence.
