# Open Issues

Baseline: `09dd686c5ae0826594b9c5e1f871d95d95d3ce1c`

## External target runtime

Java 25, PowerShell, Oracle/PostgreSQL/MariaDB native clients and servers, Docker, browser, process-kill, multi-instance and mixed-version target runtime are unavailable in the current environment; static/semantic substitute verification was executed and is not promoted to native runtime PASS.

## Cross-session ownership

`CROSS_SESSION_CHANGE_REQUEST.csv` contains **108 OPEN** exact-ID requests for Java owner modules, ADM/BZA frontend, security/auth and generator consumers. 6E delivered provider contracts, SQL/scripts, OpenAPI and tests but did not edit those owners' paths.

## Status interpretation

All assigned IDs are reviewed and judged. Development is complete for 34 Work Items and incomplete for 26 because of target-runtime or cross-owner completion conditions. Verification remains incomplete until native target execution.
