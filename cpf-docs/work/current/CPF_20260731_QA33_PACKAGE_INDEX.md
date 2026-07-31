# CPF QA33 독립 Source·Runtime Closure 패키지

- Package ID: `CPF-20260731-QA33-INDEPENDENT-SOURCE-RUNTIME-CLOSURE`
- Repository: `freeangelsun/202412_01_CPF`
- Branch: `master`
- Review baseline: `1536a0d59004ebade7dcb29383cbe2e758547f8e`
- 작성 시각: `2026-07-31T11:12:55+09:00`
- 목적: QA32 완료 보고를 최신 Source로 독립 재검수하고, 다른 AI/작업자가 수정과 검증을 독립 수행하도록 상세 원장을 제공한다.
- 사용자 승인 없는 Git write 금지

## 규모
- Defect 113
- Requirement 138
- Mandatory Scenario 414
- Source Inspection 115
- Evidence 28

## 정본 읽기 순서
1. `cpf-docs/work/current/CPF_20260731_QA33_GPT_DEVELOPMENT_INSTRUCTION.md`
2. `cpf-docs/work/current/CPF_20260731_QA33_DEVELOPMENT_AND_VERIFICATION_REQUEST.md`
3. `cpf-docs/work/review/CPF_20260731_QA32_INDEPENDENT_SOURCE_REVIEW.md`
4. `cpf-docs/quality/CPF_20260731_QA33_DEFECT_REGISTER.csv`
5. `cpf-docs/quality/CPF_20260731_QA33_REQUIREMENT_MATRIX.csv`
6. `cpf-docs/quality/CPF_20260731_QA33_SCENARIO_MATRIX.csv`
7. `cpf-docs/quality/CPF_20260731_QA33_SOURCE_INSPECTION_MATRIX.csv`
8. `cpf-docs/quality/CPF_20260731_QA33_EVIDENCE_MATRIX.csv`
9. `cpf-docs/work/handover/CPF_20260731_QA33_REVIEW_HANDOVER.md`
10. `cpf-docs/work/manifest/CPF_20260731_QA33_QA_REQUIREMENTS_MANIFEST.json`

## 시작 Gate
```powershell
git fetch origin master
git rev-parse HEAD
git rev-parse origin/master
git status --porcelain=v1
python cpf-tools/scripts/verify-cpf-qa33-request-integrity.py --root .
```

최신 master가 Review baseline보다 진행됐으면 새 SHA를 고정하고 모든 Source finding을 다시 확인한다.
