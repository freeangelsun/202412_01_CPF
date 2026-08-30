# CPF Documentation 2.15.4 Handover

## Authority

- Source ZIP: `CPF_FULL_SOURCE_FOR_NEXT_QA_20260829_220256.zip`
- Source SHA-256: `6AEC7A50D69F140B30968EAD21B7242E1D2A6252446DAC0D5A27CC4C4566D7DC`
- Documentation Harness: **2.15.4**
- Current-only: use only `cpf-docs/governance/documentation-harness/` and the current `cpf-docs/deliverables/documentation/` evidence set.

## Final artifact state

- README brochure: **14 major H2 sections / 13 product visuals**, with the original core visual set **9/9 retained** and current SHA-bound 900/1200/1440 full-page review.
- The whole-platform Architecture is the first major visual after the opening Hero/product definition, followed by capability, development/generator, invocation/transaction, integration/batch/gateway, DB3/security/operations flows.
- 11 DOCX / 11 PDF / 128 pages; current DOCX render review, PDF openability/preflight/page-count and accessibility re-run complete.
- A11Y: High 0 / Medium 0 / Low 0. PDF preflight: warning 0 / error 0.
- Harness Negative Fixtures: **126/126 PASS**. Final Acceptance: **59 required gates / 12 target artifacts PASS**.
- User findings for brochure regression, whole Architecture visibility, visual retention, product coverage, false-green evidence, page composition and user utility are closed with current Evidence.

## Next session rule

Do not inherit PASS after Source or artifact SHA changes. Re-run the Current Harness from Source currentization through rendered/manual review and Final Acceptance. Independent QA remains the final acceptance owner.
