# ARCHITECTURE DECISION REQUIRED

## A-001 — Special Review `CPF-RV-0045` vs current EDU-ADM architecture

The 1,000-point checklist says “EDU 135 전체가 executable consumer로 존재” as an acceptance statement.
Current central architecture intentionally makes 13 `PRODUCT_ADM/MERGE_EDU` EDU-ADM artifacts non-executable redirect metadata and keeps only 02/03/04/07 as extension samples.

QA will **not** reactivate Product-duplicating handlers just to satisfy the checklist wording. Central must align the special review wording with the canonical architecture:
- either clarify that all 135 must be fully adjudicated, while Product/Merge rows are explicitly non-executable redirects,
- or formally change the canonical architecture with full impact analysis.

Until aligned, `CPF-RV-0045` remains `재확인 필요`.

## A-002 — persistence-mybatis package ownership

Canonical starter packageBase is `com.cpf.starter.data.persistence.mybatis`.
Current downstream implementation includes `com.cpf.core.*` and `com.cpf.starter.persistence.mybatis.*`.
QA recommends preserving core ownership and moving provider implementation under canonical starter namespace, not weakening the package-owner contract.
