# CPF Documentation Codex Revalidation Request

## Basis
- Repository: `freeangelsun/202412_01_CPF`
- Branch: `master`
- Baseline SHA: `8670f6c9b675e3d210576c843d826898c781f9f0`
- Harness: v1.1.3
- Evidence: `cpf-docs/deliverables/documentation/TEST_AND_EVIDENCE.md`

## Required independent checks
1. 과거 README/DOCX/PDF/Documentation 전용 Asset을 정본으로 승계하지 않았는지 확인한다.
2. README는 목차 없이 번호형 대제목, 제품 가치/차별점, 전체 Architecture Map, 실제 Source/CLI를 사용했는지 확인한다.
3. 11 DOCX/PDF가 `scope.json`과 각 Profile의 H1/H2, Orientation, Hard Page Limit을 만족하는지 확인한다.
4. Table 제목/목적, Cell 과밀, 자동개행, Indentation, Heading 고립, Figure/Caption 분리 문제를 전페이지 검사한다.
5. Source/API/CLI/DB3/Owner/Consumer 의미 오매핑과 반복 문장을 공격적으로 재검수한다.
6. Accessibility High/Medium/Low 0, PDF openable/not-encrypted/not-scan-only, broken glyph/blank page 0을 재확인한다.
7. Delete Manifest가 exact Root-relative allowlist인지, 보호 경로를 상위 wildcard로 삭제하지 않는지 확인한다.
8. Clean Snapshot에서 delete -> overlay -> validator -> hash 순으로 재현 가능한지 확인한다.

Codex는 Codex-owned Evidence/Status만 갱신한다.
