# R6I Developer Result SHA Binding

- Developer overlay baseline SHA: `64049044956924032360fa80be83b5e37c64f828`
- Developer package-time result SHA: `PENDING_USER_APPLY_COMMIT`
- User-applied/pushed master SHA confirmed by central review: `0427758db041d38eb0f34d88b55bd5366e2d9e47`
- Commit message: `07_01`
- Confirmed at central review: 2026-08-07

## Rule

The developer-owned historical ledger remains unchanged. QA must bind current evidence to `0427758db041d38eb0f34d88b55bd5366e2d9e47` independently rather than rewriting the developer's historical `PENDING_USER_APPLY_COMMIT` fields.

A PASS generated against `64049044956924032360fa80be83b5e37c64f828` plus an uncommitted overlay is not automatically a PASS against `0427758db041d38eb0f34d88b55bd5366e2d9e47`. Hash equality or current-SHA rerun evidence is required.
