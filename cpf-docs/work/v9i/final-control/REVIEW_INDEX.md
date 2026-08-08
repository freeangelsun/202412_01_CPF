# CPF Final Control Index

## Current Status

- Currentization basis SHA: `4870b20733875c3955f93846307fa5041e6f6c22` (`07_06`)
- Operational basis after apply: successor `master` exact SHA
- Canonical denominator: **169**
- Legacy aliases: **8** (duplicate count prohibited)
- Previous development ledger: 93 (input only)
- Previous central findings: 56 (input only)
- QA A new findings: 25
- QA B new findings: 8
- Central normalized new actions: **31 (P0 22 / P1 9)**
- Release status: **RELEASE_BLOCKED until Product Finalization + Documentation Finalization + final QA A/B pass**

## Read Order

1. `cpf-docs/governance/CPF_DOCUMENT_CANONICAL_INDEX.md`
2. `cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md`
3. `CENTRAL_QA_MERGE_REPORT.md`
4. `CENTRAL_FINAL_ACTIONS.csv`
5. `ROLE_BOUNDARY.md`
6. `../final-dev-request/CPF_DEVGPT_FINAL_SOURCE_COMPLETION.md`
7. `../qa/final-a/**`
8. `../qa/final-b/**`
9. current Product Source and exact-SHA Evidence

## TransactionId Canonical Decision

정식 거래 기동 Channel/System은 최초 CPF transactionId를 생성할 수 있다.
이후 동일 거래의 Local/Remote/Gateway/Message/Async/Retry/Batch/File/UNKNOWN/Reconcile은 동일 transactionId를 유지한다.
비신뢰 주체의 spoof/replay/manipulation만 trust boundary에서 차단한다.
모든 inbound transactionId 일괄 재생성은 금지한다.

## Role Boundary

- Project Canonical/Control: 중앙 관리자
- Product Source: Developer GPT
- Independent audit: QA A / QA B
- README/Guide/PDF/DOCX: Documentation Finalization

## Garbage Control

과거 QA38/QA39, V7/V9 Control, 완료된 Session Handover/Review는 current canonical이 아니다.
유효 결론이 현재 정본에 흡수된 파일은 `PROJECT_CONTROL_DELETE_MANIFEST.csv`와
`PROJECT_CONTROL_EMPTY_DIR_MANIFEST.csv`를 기준으로 사용자 승인 실행으로 제거한다.

## Final Flow

1. 중앙 Project Control currentization/cleanup 적용·Push
2. Product Developer가 successor master를 확인하고 Final Source Completion 지속
3. Product Overlay 적용·Push
4. 별도 Documentation Finalization
5. Documentation Overlay 적용·Push
6. 최종 하나의 SHA에서 QA A/B 동일 전체 전수검수
7. 중앙 Merge 및 Release 최종판정
