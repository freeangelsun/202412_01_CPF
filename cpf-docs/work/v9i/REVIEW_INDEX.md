# CPF V9I Review Index

## Current Control
- Latest master / QA basis: `3ed676061246c9db3e44f29e254c0393ecca3929` (`07_02`)
- R6I developer baseline: `64049044956924032360fa80be83b5e37c64f828`
- Developer historical result: 77/77 implementation claim / 26 verified / 51 unverified
- QA A R6J: 50 findings, fully closed 0
- QA B Deep R6J: 48 findings, static pass 7 / fail 41
- Central unique findings: **56 (P0 44 / P1 11 / P2 1)**
- Central requirement ledger: **93**
- Current verdict: **미통과 — RELEASE_BLOCKED**
- Current stage: **R6J QA merge complete → Developer R6J rework request ready**

## QA R6J
- `qa/r6j/CENTRAL_MERGED_QA_REPORT.md`
- `qa/r6j/CENTRAL_INTEGRATED_FINDINGS.csv`
- `qa/r6j/CENTRAL_REQUIREMENT_STATUS.csv`
- `qa/r6j/EDU_ADM_ARCHITECTURE_DECISION.csv`
- `qa/r6j/CENTRAL_RUNTIME_QUALIFICATION_MATRIX.csv`
- `qa/r6j/QA_CROSS_REVIEW_DISPOSITION.md`
- `qa/r6j/QA_RESULT_PACKAGE_STATUS.md`
- QA A source result: `qa/r6j/a/**`
- QA B Deep source result: `qa/r6j/b/**`

## Development
- Current exact request: `final-dev-request/CPF_DEVGPT_R6J_FULL_100_PERCENT_EXECUTION_INSTRUCTION.md`
- Direct 34-row ledger: `final-dev-request/DEVELOPMENT_REWORK_REQUIREMENTS_R6J.csv`
- Transaction/Logging scope: `final-dev-request/R6J_TRANSACTION_LOGGING_REWORK.md`
- Common policy: `final-dev-request/CPF_DEVGPT_NEXT_CYCLE_EXECUTION_POLICY.md`

## Central Policies
- `post-r6i/LOGGING_TRANSACTION_QA_STANDARD.md`
- `post-r6i/CROSS_AGENT_COLLABORATION_POLICY.md`

## Historical
- `qa/r6i/**`
- `qa/r5i/**`
- `../../r6i-dev/**`

Do not use the superseded QA B fast result for verdict.

## Scope Rule
- Developer scope: 93/93 + 56/56 + full canonical + runtime/GA + self-discovered defects, 100% goal.
- QA A/B scope: each reviews entire product 100%; Primary is depth emphasis only.

## Mandatory Completion Policy
- `CPF_100_PERCENT_FINALIZATION_MANDATE.md` — Developer/QA/Central all target remaining 100%, no planned partial completion.

## Highest Finalization Policy
- `CPF_PROJECT_FINAL_100_PERCENT_COMPLETION_MANDATE.md` — 100% means project completion, not assigned-list completion.
