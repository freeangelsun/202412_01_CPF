# CPF Codex 최종 독립 검수 요청서

> 이 문서는 ChatGPT 개발 완료 및 사용자 Push 후 `<FINAL_SHA>`로 교체하여 사용한다.

## 기준

- Repository: `https://github.com/freeangelsun/202412_01_CPF`
- Branch: `master`
- 검수 SHA: `<FINAL_SHA>`
- 역할: 독립 검수, 회귀·False Green·Evidence 무효 탐지
- 원칙: 대규모 신규 개발을 다시 수행하지 않고 실패의 Root Cause와 재현 명령을 반환한다.

## P0-1 이전 Codex 중단 복구

- Runtime Query `PORTABLE_ONLY`
- UTF-8/BOM Gate
- QA Ledger Closure
- Final Completion Gate
- 과거 WIP 이후 Source Diff와 회귀
- Current/Handover/Evidence exact-SHA

## P0-2 신규 개발 영향도

- ARCH/API: Public API/SPI/Internal, Gateway/Batch Ownership
- DB/GEN: V74~V76, Canonical/3 Vendor/Generator parity
- RUNTIME: LB/Health/Failover/Unknown/Recovery
- SEC/OPS: Approval/Reason/Audit/Masking/Durable ACK
- UI: ADM 5개 IA, Gateway/Log/Batch, BZA Menu Tree
- FILE/BAT: File Claim/Fencing, Approved Shell Process Tree/Secret

## P0-3 검수 순서

1. Local HEAD, origin/master, Working Tree Clean
2. Work/Handover/Evidence exact-SHA Gate
3. Public Boundary·Ownership·Runtime SQL·Security Static Gate
4. Targeted Gateway/Batch/ADM/BZA Tests
5. Canonical DB·3 Vendor·Generator
6. Frontend Typecheck/Lint/Test/Build
7. Root Clean Test/Assemble/QualityGate
8. DB/Redis/Multi-instance/Browser Runtime
9. 405+90 Matrix와 2,715 Ledger exact-SHA 폐쇄

## 완료 금지

- `<FINAL_SHA>` 미치환
- Build/Test/Gate 실패
- 미검증을 PASS로 기록
- 하나의 Vendor 결과를 3종으로 승계
- Raw JSON/Map 계약/Consumer 없는 Interface
- Test 삭제·Assertion 약화·Gate 우회
- Current/Handover/Evidence SHA 불일치
- Matrix/Ledger의 `부분 구현`, `미구현`, `미검증`, `실패`, `재확인 필요`
