# CODEX REVALIDATION REQUEST — C 개발/QA 관리_21 Final Applied Source

Independently revalidate the **final applied DEV21 source**. Do not inherit historical PASS, pre-fix source identity, retired BZA assumptions, or the old 9-failure Gradle result as current success.

## 1. Basis

- Development baseline ZIP: `CPF_FULL_SOURCE_FOR_NEXT_QA_20260820_122758(1).zip`
- Baseline SHA-256: `f73988097aef77a1bcc795ba66394326dd5a9f875a2d1b530e2c99e315cf5ceb`
- GitHub `master` observed at development start: `9922ca8c3c7dceeb18a9b41b2b923f564bbf29de`
- Canonical target: `cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md` (`205` Current Requirements)
- Developer ledger: `cpf-docs/work/REQUIREMENT_STATUS.csv`
- Requirement-by-requirement development review: `cpf-docs/work/current/CPF_DEVELOPMENT_REQUIREMENT_REVIEW.csv`
- Current evidence: `cpf-docs/deliverables/TEST_AND_EVIDENCE.md`
- Open environment acceptance: `cpf-docs/deliverables/OPEN_ISSUES.md`
- Delete lifecycle: `cpf-docs/deliverables/DELETE_MANIFEST.csv`; current existing protected deletion must be `0`.

Codex must not modify Developer-GPT or QA-owned columns. Runtime not executed remains `미검증`.

## 2. Priority independent rechecks

1. **Ownership:** `cpf-common` is the Product Owner for business-common contracts/services/SQL/tests and `cpf-starter-common` is Runtime/AutoConfiguration only; duplicate FQCN and reverse dependency are zero.
2. **Generated Domain:** MBR/EXS root `cpf-domain.yaml`, ownership lock, stale-generated handling, user-source preservation, externalClients actual generated consumers, DB Binding separation, same canonical schema/template across domains.
3. **DB3:** Oracle/PostgreSQL/MariaDB only; canonical seed/source/runtime bundle parity, migration/seed/upgrade/rollback contracts and no vendor folder leakage into generated business source.
4. **System6:** Transaction, Original System, Current System, Caller System, Target System, Target Operation remain canonical; Channel is separate optional ingress/policy/security context.
5. **Operation:** `@CpfOnlineTransaction.operationId`/OpenAPI/Domain Client/ADM/Log/Trace identity remains aligned; operationId and executionId are not conflated.
6. **Runtime Instance:** explicit instanceId→hostname fallback; invalid host fails closed; same-host same-system different active process collision is rejected before READY; explicit MBR01/MBR02 is allowed.
7. **Runtime/Recovery:** UNKNOWN, Drain, Reconcile, Retry, lease/fencing, process failure/recovery paths retain fail-closed/idempotent semantics.
8. **Messaging/Integration:** JMS checked exception boundary, IBM MQ invalid header provider-before-call rejection, Kafka/RabbitMQ regression, Domain Call InvocationMetadata, AI UNKNOWN public exception, ISO8583 explicit Charset, Retry/Timeout contracts.
9. **Backoffice/MBW:** retired BZA product identity is absent from Current surfaces; Backend OpenAPI/controller `96/96`; web consumer/routes remain current; no business master ownership duplication.
10. **ADM Session/Security:** Public JDBC Session Starter provides HttpOnly session cookie, CSRF, fixation rotation, encrypted internal credential bridge, multi-WAS capable store; browser-readable bearer primary path is absent.
11. **ADM RBAC:** 68 routes / 64 canonical menus / missing 0; Menu/Button/API Permission/OpenAPI Operation identities are separated and Backend permission remains authoritative.
12. **ADM System6/Commercial UI:** Logs/Trace/Transaction show System6 as primary identity and Channel as secondary context; Commercial Page capability/error handling and approval-gated dangerous actions are complete.
13. **ADM Generated Consumer:** OpenAPI/controller `337/337`, generated consumer closure `337/337`, waiver `0`; raw REST is restricted to justified protocol cases.
14. **EDU:** physical/executable Online `20` + Batch `15` = `35`, internal import `0`, no nested counting False Green or retired micro-sample duplication.
15. **Public Distribution/Bootstrap:** empty staging, default-deny classification, Java shared bootstrap engine, thin Windows/Linux wrappers, stop≠reset, isolated Public Binary consumer/no `mavenLocal`/private source.
16. **Requirement/Evidence:** 205 Developer ledger ↔ 30,605 logical requirement projection; Current-only doc roles are unique; Delete/Garbage lifecycle covers all current deletions; stale evidence is not current PASS.
17. **Source quality:** Java syntax, Spring/IDE hygiene, request mapping uniqueness, dependency cycles/internal imports, secrets, cache/build garbage and protected delete remain zero-failure.

## 3. Runtime revalidation required when environment supports it

- Java25 full Gradle build/publication.
- Oracle/PostgreSQL/MariaDB live install → migration → seed → runtime query → upgrade → rollback.
- Same-host multi-process, Multi-WAS, process kill, lease expiry, restart/reconcile.
- Public Binary Repository end-to-end isolated consumer.
- Windows PowerShell 5.1 apply/delete/verification.
- ADM/Backoffice Browser E2E in Chromium/Firefox/WebKit and responsive widths, including 401/403/404/409/429/500/503.

Do not convert an unexecuted runtime stage to PASS. Record Codex findings/evidence only in Codex-owned areas and return any source defect to the same Canonical Requirement ID.
