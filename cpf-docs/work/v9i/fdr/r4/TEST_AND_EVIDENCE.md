# REV-004 R4 Test and Evidence

## Developer conclusion

The implementation and independent self-review scope for `FDEV-003`, `FDEV-005`, `FDEV-014`, `FDEV-016`, and `FDEV-025` is complete. This is **not** a QA completion statement. The overlay still requires exact-SHA clean-checkout application and target-runtime execution.

## Executed and passed

- Approval canonical SHA-256 normalization, invalid-hash masking, fail-closed legacy path, atomic single mutation, optimistic version conflict and owner-side mutation smoke.
- ADM stable idempotency runtime: same draft/key reuse, payload change/key rotation, confirmation lock, no raw payload storage, normalized duplicate-key fail-close.
- Eight-operation parity across Controller, OpenAPI, generated contract, current route registry and Vue consumer.
- OpenAPI unique operations: **332**; integration operations: **8**; security: Session Cookie plus CSRF; declared errors: 400/401/403/404/409/429/500/503.
- OpenAPI generation initially exposed stale source/marker drift. The current source was regenerated and then executed twice with no byte change. The initial failure is retained as evidence and the final pass supersedes it.
- DB3 wrapper static/protocol checks: exact HEAD, git root, three-vendor preflight, password over stdin, redaction, vendor result/version/exit preservation.
- Starter complete synthetic closure and QA34 contract: active 39 = public 6 + internal 33; retained inactive legacy 1.
- Python/JSON/CSV/Node syntax, conflict marker, trailing whitespace, NUL, secret, path-length and bytecode hygiene.

## Not executed; not PASS

- Java 25 full root Gradle configuration, clean/check/test/publication.
- Full clean-checkout QA38 and QA39 gates. Overlay-only logs are diagnostic partial results, not product failures or passes.
- Node >=22.18.0 `npm ci`, lint, full typecheck, Vitest, production build and Playwright.
- PowerShell/Pester execution.
- Live Oracle/PostgreSQL/MariaDB install, upgrade, rollback, runtime query, failure and recovery execution.
- GitHub Advanced Security secret scan because the repository feature is not enabled.

## Evidence directory

`cpf-docs/work/v9i/fdr/r4/e`

See `TEST_EXECUTION_LEDGER.csv` for each command class and status.

Baseline: `a8be27a34bdac0b7c075e06d6e86571244c96421`
Recorded: `2026-08-06T19:33:22+09:00`
