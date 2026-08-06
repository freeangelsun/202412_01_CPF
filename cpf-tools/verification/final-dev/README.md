# Target Runtime Execution Package

Baseline: `2929163b3bb40159e22e1f57e79b6cd070abf7ad`

These scripts are fail-fast target-runtime entry points. They do not convert an unavailable environment into PASS.

- `run-java25-gradle91.ps1 -ExpectedHead <post-overlay-commit-sha>`: committed clean SHA, Java 25, Gradle 9.1, build/test/publication.
- `run-db3-lifecycle.ps1`: canonical static lifecycle first, then approved Oracle/PostgreSQL/MariaDB runner through `CPF_DB_LIFECYCLE_RUNNER`.
- `run-multiprocess-chaos.ps1`: approved Broker/Multi-process/Split-WAS/Process-kill harness through `CPF_MULTIPROCESS_CHAOS_RUNNER`.
- `run-browser-matrix.ps1`: reproducible npm install and Chromium/Firefox/WebKit Playwright matrix.

Secrets are passed through environment variables and must not be copied into Evidence. Target runners must mask URLs, usernames, tokens, and passwords.
