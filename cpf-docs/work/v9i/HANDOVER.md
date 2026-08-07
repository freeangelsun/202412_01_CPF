# CPF V9I Handover

## Current baseline
- latest master confirmed: `0427758db041d38eb0f34d88b55bd5366e2d9e47`
- R6I development baseline: `64049044956924032360fa80be83b5e37c64f828`
- developer result: implementation 77/77, verified 26, unverified 51
- final QA: **not passed yet**

## Next action
1. QA A executes `qa/r6j/a/QA_A_EXECUTION_INSTRUCTION.md`
2. QA B executes `qa/r6j/b/QA_B_EXECUTION_INSTRUCTION.md`
3. user returns both QA ZIP/reports
4. central manager merges findings/opinions/disagreements
5. cross-review is requested where necessary
6. central manager creates next exact Developer GPT requirement

## Mandatory continuity
- QA/Developer must proactively discover issues beyond assigned requirements.
- A/B QA Primary scopes rotate by round.
- Developer, QA A, QA B opinions are preserved and centrally adjudicated.
- ADM is a CPF product; EDU is for Public API/SPI/Extension/Integration consumer education. EDU-ADM/135 reclassification requires QA decision before canonical change.
- Logging/transaction tracing is a release-critical QA axis.
- ADM must support transactionId one-shot end-to-end timeline/tree.
- File/DB logs must follow standard structure, retention, loss detection, masking and recovery.
- GPTs never Commit/Push; they provide ZIP + hash to the user.
