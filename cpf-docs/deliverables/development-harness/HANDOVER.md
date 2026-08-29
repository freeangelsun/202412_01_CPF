# CPF Development / QA Next Session Handover — Harness Only

## 유일 개발 진입점
`cpf-docs/governance/development-harness/CPF_DEVELOPMENT_HARNESS.md`

다음 세션은 과거 개발 기본지침/Closure/Review 원장을 별도 정본으로 사용하지 않는다. Product Contract, Canonical Registry/Trace, Current Work/Status, Role/Test/Control Ledger만 Authority로 사용한다. Generated Projection과 Historical Provenance는 현재 PASS 근거가 아니다.

## Source
- Input: `CPF_FULL_SOURCE_FOR_NEXT_QA_20260829_211247.zip`
- ZIP SHA-256: `E6E343947AB4D829996107833AD20CD056D35BAC340013F58E0B2068C9694B30`
- Harness Source Identity: `bf5c09a165e9bd460b7a51fa135a75c2f09c8a3f3ea1f42b3c6da6467bf1657f` / 8386 files
- Git write/commit/push/delete는 사용자 승인 범위 밖에서 수행하지 않는다.

## Current
- Canonical 218, Trace 218, Bridge 46, Work 394, Role 1182, Test 788
- Product OPEN GAP 11건
- Legacy migration 265: delete eligible 246, protected retained 19
- Negative Mutation 21/21 PASS

## 다음 세션 실행 원칙
1. Harness Gate와 Source Identity 확인.
2. Current OPEN Work부터 Root Cause 단위로 Source→Consumer→Test/Runtime→Evidence까지 완결.
3. Source 수정 시 Codex/Claude 방향을 별도 우회 구현으로 뒤집지 말고 동일 Architecture/QA Requirement 기준으로 정렬.
4. Physical 미검증/VS Code 비0/Runtime mojibake/mandatory SKIP·NOT_EXECUTED·UNKNOWN은 완료 금지.
5. Product GAP과 새 Finding은 Current Work에 즉시 병합.
6. 최종 QA 완료는 QA만 확정.

## 명령
적용/Legacy 삭제/Harness 검증은 `cpf-docs/deliverables/development-harness/COMMANDS.md`의 3개 한 줄 명령만 사용한다.
