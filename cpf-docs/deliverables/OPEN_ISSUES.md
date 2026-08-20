# OPEN ISSUES — Canonical Target Alignment

정본 현행화 자체의 미해결 문서 충돌은 0건이다. 아래 항목은 **정본을 기준으로 Source/QA 관리자산이 따라와야 할 Gap**이며 QA 완료로 처리하지 않는다.

## CANON-GAP-001 — P0 — Ownership
- Target: `cpf-common`
- Current Gap: Current source has no `cpf-common/` root while target assigns customer business common ownership to cpf-common.
- Required Development: Implement/restore canonical cpf-common business-common owner; separate from technical cpf-starter-common; migrate common code/message/calendar/template consumers and tests without moving them into cpf-core.

## CANON-GAP-002 — P0 — Generated Domain
- Target: `cpf-member/cpf-external`
- Current Gap: Canonical target requires root source-controlled `cpf-domain.yaml`; current definitions live under cpf-tools/generator/definitions and generated roots have no root definition.
- Required Development: Currentize generator create/setup/sync to own root logical definition, migrate reference domains, keep environment DB binding separate, protect user-owned source.

## CANON-GAP-003 — P0 — EDU
- Target: `cpf-education`
- Current Gap: Canonical Online groups are exactly 20; current physical Online group count is 19 and transaction patterns are consolidated.
- Required Development: Split/organize canonical REQUIRED and REQUIRES_NEW groups without creating duplicate micro samples; keep Batch 15; rerun count/catalog/runtime tests.

## CANON-GAP-004 — P0 — Starter/Common
- Target: `cpf-starters/common`
- Current Gap: Current source treats technical common starter surface as owner for common product services.
- Required Development: Refactor ownership so cpf-common owns business-common contracts/services and cpf-starter-common owns Spring Boot composition/autoconfiguration only.

## CANON-GAP-005 — P1 — Generator lifecycle
- Target: `cpf-tools/generator`
- Current Gap: Current generator retains tool-side definition/lock lifecycle model that conflicts with root logical definition target.
- Required Development: Replace competing lifecycle truth with root cpf-domain.yaml plus framework-owned template metadata; retain deterministic diff/sync and user-owned protection.

## CANON-GAP-006 — P1 — System6/Operation
- Target: `runtime/verifiers/docs`
- Current Gap: Previously currentized source needs revalidation against the now explicit all-six remote serialization, Caller System policy, separate optional Channel policy and discovery lifecycle.
- Required Development: Run repository-wide stale Channel6/operation callerChannel scan and update runtime/verifiers/EDU/docs if any current behavior differs.

## CANON-GAP-007 — P1 — Instance identity
- Target: `runtime/registry`
- Current Gap: Target adds explicit same-host same-system multi-process readiness collision rule.
- Required Development: Add duplicate active identity readiness/registry gate and runtime test if not already present.

## CANON-GAP-008 — P1 — Public distribution
- Target: `release/public workspace`
- Current Gap: Target makes Public Binary Repository and isolated-cache clean consumer independent acceptance items.
- Required Development: Verify publication tooling/BOM/repository metadata and clean consumer without mavenLocal/private repo; implement missing paths.

## CANON-GAP-009 — P1 — Local Bootstrap
- Target: `cpf-tools/environment/bootstrap`
- Current Gap: Target defines bootstrap as shared engine with progress/timeouts, selected DB lifecycle, stop≠reset, rediscovery.
- Required Development: Audit existing Windows/Linux bootstrap and implement missing behavior/runtime tests.

## CANON-GAP-010 — P1 — Documentation governance
- Target: `cpf-docs`
- Current Gap: Multiple historical governance/ADR/handover/review documents remain in current tree and can compete with current target.
- Required Development: Apply canonical delete manifest after absorbing current requirements; update remaining links/gates so deleted history is not required.

## CANON-GAP-011 — P0 — Requirement Status
- Target: `cpf-docs/work/REQUIREMENT_STATUS.csv`
- Current Gap: Current status ledger has 36 rows and does not contain the 11 newly materialized canonical Requirement IDs. Developer GPT must not fabricate QA state rows.
- Required Development: QA/central management must add/reopen affected IDs and preserve role-column ownership; development then updates only authorized 개발GPT_* fields and new evidence.

## CANON-GAP-012 — P1 — Derived Requirement Dataset
- Target: `CPF_REQUIREMENT_MASTER / scenario / execution datasets`
- Current Gap: Large derived datasets were generated from the previous canonical structure and may contain stale document paths/ownership assumptions.
- Required Development: Regenerate/validate derived datasets from the 205 Current Canonical catalog using the designated decomposition pipeline; do not treat stale derived rows as higher priority than Final Target.

## CANON-GAP-013 — P1 — Official Developer Docs
- Target: `GENERATOR_GUIDE and affected Architecture/Developer/EDU guides`
- Current Gap: Generator Guide still documents tool-side definitions and metadata-free generated roots, conflicting with the new root source-controlled cpf-domain.yaml target.
- Required Development: When implementing GEN-SETUP/DB-BINDING, currentize official guides, examples and commands to the root logical definition/environment-binding model and rerun link/command QA.
