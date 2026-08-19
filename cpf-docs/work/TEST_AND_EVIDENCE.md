# CPF TEST AND EVIDENCE

- Baseline full-source ZIP SHA-256: `b5573c0ab545597563846d0fd31e8669e5b7fec6df73393fed70f17b5f0b6850`
- Baseline file count: `8,440`
- Desired-state file count: `8,399`
- Source identity (Git-independent, evidence-metadata excluded): `762343a5d08d11a7cfc9990236761f5a380e4f92e0f4bfd54a98d52095da2a64`
- Git exact SHA: `UNVERIFIED_SOURCE_ZIP_HAS_NO_DOT_GIT`
- Development status: **SOURCE/STATIC/PACKAGE CLOSURE PASS IN ASSISTANT ENVIRONMENT**
- Verification status: **ENVIRONMENT-DEPENDENT RUNTIME REVALIDATION REQUIRED**

## Current PASS evidence

| Gate | Result | Current evidence |
|---|---|---|
| BZA boundary | PASS | backend OpenAPI 96 = Channel routes 96; Reference routes 4; DB-less=1; CPF Java dependency=0 |
| Frontend consumer closure | PASS | 563 files / 790 imports / findings=0 |
| Frontend source syntax | PASS | 590 files / errors=0 |
| Frontend Golden Path | PASS | ADM live generated source + external BZA reference source |
| BZA/ADM reference workflow runtime | PASS | Node/TypeScript harness 10 checks + 4 reference pages contract |
| Education active surface | PASS | Online 20 / Batch 15 / role groups 20+15 / catalog 35 |
| Education executable coverage | PASS | 35/35 tests mapped; flat/numeric/internal import 0 |
| Common Product Service DX | PASS | services=5 / goldenPath=5 / educationConsumer PASS / managementBoundary PASS |
| Optional surface | PASS | source-removable applications=3 / selectable Starters=25 |
| Public staging static | PASS | 116 classified files / private implementation path leakage 0 |
| Public release tool tests | PASS | 4/4 |
| Focused release/BZA/frontend/evidence Python tests | PASS | 22/22 |
| NXT3 final all | PASS | 23/23 checks / failed=0 / unverified=0 (static gate scope) |
| ADM route/generated client | PASS | 68 routes / 329 explicit route operations / 337 generated OpenAPI operations |
| Evidence integrity | PASS | 36 developer requirements / 25 QA findings / current source identity / file-level package hashes fail-closed |
| Java source syntax | PASS | 2,820 files / errors=0 on final desired-state static rerun |

## Current structural closures

- Canonical transaction wire is System-based 6 headers. Channel identity is a separate optional policy/context axis.
- Runtime instance identity uses the Foundation single provider and fails closed on invalid fallback values.
- Pre-controller canonical-header rejection produces sanitized durable transaction evidence through the Observability owner.
- Subject time search uses original transaction start time; first subject discovery time remains provenance.
- Common active runtime is cpfDB + cpfCommonTransactionManager; legacy cmn runtime has no active main-source consumer.
- `cpf-biz-admin` is an Optional Prebuilt Business Administration Domain; business master data remains owned by the corresponding Business Domain.
- `cpf-biz-channel` is DB-less Pure Spring Boot and does not depend on CPF Java/BOM/Starter/Internal API.
- `cpf-biz-frontend` consumes the Channel only and exposes four representative reference feature routes.
- Generated-domain-like public contracts are reused inside `cpf-biz-admin`; Generator does not create BZA.
- Optional Source applications may be physically absent without breaking Root configuration; optional DB/deployment surfaces are skipped when owner source is absent.
- Public Git staging is default-deny and every staged file requires a public classification. Private/internal paths and secret-like content fail closed.

## Delete candidates — not physically deleted

`cpf-docs/deliverables/DELETE_MANIFEST.csv` currently contains `273` root-relative file candidates. It includes 29 superseded Education flat sources, 239 legacy embedded BZA Frontend files, and 4 stale BZA frontend fixtures. Protected-path deletion count is 0.

## Unverified runtime — NOT PASS

- Java 25 Root Gradle configuration/compile/test/build/publication/SBOM
- Live Oracle/PostgreSQL/MariaDB Fresh/Upgrade/Runtime/Rollback
- Redis/Valkey live reconnect/failover
- Multi-WAS policy/identity/lease contention
- process kill/restart/redeploy recovery
- external BZA Channel + Frontend live HTTP/browser E2E
- PowerShell runtime/parse execution in this Linux assistant environment
- Public Git real remote clone/commit/push

These remain `미검증`; static or previous-SHA evidence is not substituted.
