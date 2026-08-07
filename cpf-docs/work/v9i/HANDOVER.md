# CPF V9I Handover

## Baseline
- latest master: `3ed676061246c9db3e44f29e254c0393ecca3929`
- R6J QA A/B both reviewed same SHA
- current verdict: **RELEASE_BLOCKED**
- central findings: 56
- direct development rework: 34
- central requirement ledger: 93

## Next Action
1. Developer GPT receives `final-dev-request/CPF_DEVGPT_R6J_REWORK_EXECUTION_INSTRUCTION.md`
2. Developer implements direct 34 + self-discovered defects
3. Developer produces Root Overlay ZIP; GPT does not Push
4. User applies/Pushes
5. Central rechecks new master SHA
6. QA A/B scopes rotate and re-review same IDs + new findings
7. target runtime/Codex evidence must bind to new exact SHA

## Architecture
- ADM is delivered CPF Product.
- EDU is adopter-facing Public API/SPI/Extension/Integration education.
- EDU-ADM: Product 9 / Extension Sample 4 / Merge 4.
- 17/135 numeric preservation is not a completion goal.

## Transaction/Logging
Release-critical:
- same transactionId across nested/remote/async/message/batch
- ADM one-shot full timeline/tree
- DB3 standard identifiers/index/retention
- FileLog durable spool/retry/dedup/loss alert
- masking/raw permission/audit
- failure/process-kill evidence

## Collaboration
- QA/Developer discover issues beyond explicit requirements.
- A/B scopes rotate.
- opinions/disagreements are preserved and centrally adjudicated.
- no prior PASS is inherited without current-SHA evidence.
