# CROSS REVIEW REQUEST — QA A → QA B

Basis SHA: `3ed676061246c9db3e44f29e254c0393ecca3929`

QA B should independently re-run and attempt to falsify these results:

1. **Release workflow:** `CPF_FRONTEND_URL` vs preflight `CPF_ADM_FRONTEND_URL`.
2. **Frontend permission mutation:** make `ApprovalsPage.canAction()` unconditional and verify whether QA B's gate/browser matrix catches it.
3. **Observability:** use a fake self-attesting probe and confirm current qualifier false-greens.
4. **EDU security:** forge `X-Cpf-Actor-Id`, `X-Cpf-Roles`, `X-Cpf-Data-Scope` against actual authenticated runtime.
5. **EDU-ADM parity:** confirm 17/17 catalog role `CPF_ADM_OPERATOR` vs handler `CPF_REFERENCE_PLATFORM_OPERATOR`.
6. **EDU architecture:** challenge each QA A classification, naming the exact Public API/SPI/Extension contract for every retained EDU.
7. **Transaction one-shot:** trace UI → generated client → Controller → Service → query providers and prove message/DLQ/batch/file/audit source aggregation plus partial/stale indicators.
8. **Approval durable UNKNOWN:** inject DB outage exactly after successful owner side effect and prove recovery survives database unavailability.
9. **OpenAPI 422:** prove runtime OpenAPI/generated client/UI behavior matches backend `UNPROCESSABLE_ENTITY`.
10. **Authenticated browsers:** Chromium/Firefox/WebKit with real Session grants, denial paths, CAS/conflict, double click and response loss.

Do not normalize disagreement. Record `DISAGREEMENT` and exact evidence if QA B reaches a different conclusion.
