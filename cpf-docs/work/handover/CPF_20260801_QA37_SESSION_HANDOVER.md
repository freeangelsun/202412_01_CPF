# CPF QA37 Session Handover

- Overlay baseline: `1edd96c6dcc69b0b4d6e9e22a0709d910d7cfb04`
- Branch: `master`
- 전체 판정: `미검증`
- Source 판정: `재확인 필요`
- Package 목적: Codex Verification Ready
- Git 쓰기: 수행하지 않음

## 적용 전제

- 동일 SHA의 Clean Working Tree에 적용한다.
- 적용 Script는 merged Source Gate 실패 시 외부 Backup으로 Rollback한다.
- Delete Manifest 50개는 자동 삭제하지 않는다.

## Architecture

- `cpf-reference` single module, central `refDB`
- 생성형 도메인·제품 BZA 의존 없음
- 기능 Package: online/batch/platform/optional
- Batch DB: V94/U94 `CPF_REF_BAT_*`, Batch Off에서 미적용
- Core DB: V93/U93 `CPF_EDU_*`
- Generator는 REF Table 생성 금지

## Codex 진입점

1. `python .\cpf-tools\scripts\verify-cpf-qa37-source-closure.py --root .`
2. `cpf-docs/work/codex/qa37/CPF_CODEX_QA37_FINAL_INDEPENDENT_VERIFICATION_REQUEST.md`
3. Java25 Lifecycle 한 번
4. Optional Pack Removal Matrix 한 번
5. Frontend 각 한 번
6. 3DB V93/V94 및 Batch Off 각 Vendor 한 번
7. Runtime/Browser/Supply-chain 그룹 실행
8. exact result SHA Evidence

Overlay Evidence를 Runtime Evidence로 승계하지 않는다.
