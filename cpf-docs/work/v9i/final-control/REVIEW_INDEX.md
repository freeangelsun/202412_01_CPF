# CPF Final Control Index

## Current Central State

- Latest QA basis SHA: `b4b6b18b43e9ff83436ceb8b1816b31594e8d6eb` (`07_09`)
- Product Source baseline: `f0aa49f29cba3cfd6ae12b0ddd4e118d05fff16c` (`07_08`)
- `07_08 → 07_09` Product/Gate Source change: **0**
- QA A Final Full-Scope: **completed / FAIL / REDEVELOPMENT REQUIRED**
- QA B Final Full-Scope: **completed / FAIL / REDEVELOPMENT REQUIRED**
- Release: **UNVERIFIED / RELEASE_BLOCKED**
- Canonical denominator: **169** + Legacy Alias 8
- Special Review: **1,000 mandatory points**
- Current Central Actions: **36** (original 31 + 5 newly normalized actions)

## Current Merged Denominators

- Special 1,000: **FAIL 87 / 미검증 548 / 재확인 필요 365 / PASS 0**
- Canonical 169: **FAIL 20 / 미검증 87 / 재확인 필요 62 / PASS 0**
- Existing Central 31 merged: **FAIL 14 / 미검증 17**
- Central Actions after normalization: **FAIL 19 / 미검증 17**
- Runtime Qualification 13: **FAIL 4 / 미검증 9**
- Raw new QA findings: **12 (QA A 5 + QA B 7)**
- Cross-QA duplicate root cause: **1 (Javadoc)**
- Normalized current roots: **11**
- Of normalized roots: **6 mapped to existing Central Actions, 5 added as CENTRAL-FINAL-032~036**

## Read Order

1. `cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md`
2. `REVIEW_INDEX.md`
3. `CENTRAL_QA_MERGE_REPORT.md`
4. `CENTRAL_FINAL_ACTIONS.csv`
5. `SPECIAL_REVIEW_1000.csv`
6. `../qa/final-a/QA_FINAL_RESULT.md`
7. `../qa/final-a/NEW_FINDINGS.csv`
8. `../qa/final-b/QA_FINAL_RESULT.md`
9. `../qa/final-b/NEW_FINDINGS.csv`
10. `../dev-final/**`
11. Product Source / SQL / API / Test / Config / Frontend / Script

## Current Decision

Developer-rework is required. The Developer must not treat the prior `169/169`, `31/31`, `56/56`, or `21/21 PASS` as current QA PASS.

The next Product cycle must:
- repair every Central FAIL,
- preserve prior source-resolved fixes,
- address all 11 normalized current QA root causes,
- update Special 1000 through actual implementation,
- avoid false exact-SHA evidence claims before the user-created successor commit exists,
- leave unavailable Runtime as `미검증`,
- produce a current `dev-final/**` result for successor QA.

## QA Independence Result

QA A and QA B audited the same full scope in opposite directions. They converged on Release Block while finding different failure classes:
- QA A was stronger on integrated Online/Batch executable samples and release-target false-green authority.
- QA B was stronger on exact-SHA provenance, timeline failure classification, transactionId ingress wiring, FileLog fairness, package ownership and typed client enforcement.
- Javadoc incompleteness was independently found by both.

## Next Sequence

1. Apply/push this central merge Currentization.
2. Developer GPT rework from latest successor `master`.
3. User applies/pushes Product rework.
4. QA A/B independently re-audit the same successor SHA.
5. Central merges again; same IDs remain open until Acceptance is satisfied.
6. Codex later focuses especially on environment-dependent Runtime axes.
7. Release remains blocked until required Runtime, exact-SHA evidence and P0/P1 blockers are closed.
