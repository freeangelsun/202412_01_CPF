# CPF Final Control Index

## Purpose
이 경로는 Final QA A/B 독립 전수검수 결과, 중앙 Merge, 최종 Product Source 개발지침을 Repository에 영속 보존하기 위한 정본 진입점이다.

## Basis
- QA basis SHA: `3aa1dd12f8a5938d33feb6ed598b3dd2442bf2e2` (`07_05`)
- Canonical project requirement denominator: **169**
- Previous development ledger 93 / previous finding 56 are input ledgers, not project-completion denominator.
- QA A new findings: 25
- QA B new findings: 8
- Central normalized new actions: **31 (P0 22 / P1 9)**

## Files
- `CENTRAL_QA_MERGE_REPORT.md`
- `CENTRAL_FINAL_ACTIONS.csv`
- `../final-dev-request/CPF_DEVGPT_FINAL_SOURCE_COMPLETION.md`
- `../qa/final-a/**`
- `../qa/final-b/**`

## Role Boundary
Product Developer GPT must not modify:
- `README.md`
- `cpf-docs/guides/**`
- `cpf-docs/deliverables/**`
- `cpf-docs/assets/manuals/**`
- `cpf-docs/assets/readme/**`
- `cpf-docs/specification/CPF_DOCUMENTATION_STANDARD.md`

README/manual/deliverable visual/editorial finalization is a separate Documentation Finalization role after Product Source finalization.

## Final Flow
1. Preserve this control package in master.
2. Start Product Developer GPT from the successor exact SHA.
3. Product Developer completes CPF Product Source against Canonical 169 + all findings + self-found defects.
4. Apply/push Product overlay.
5. Run separate Documentation Finalization.
6. Apply/push documentation overlay.
7. QA A and QA B independently perform the same full-source final audit on the one final SHA.
8. Central merge and final release adjudication.
