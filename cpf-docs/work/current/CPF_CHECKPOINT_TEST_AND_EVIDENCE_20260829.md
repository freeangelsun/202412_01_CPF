# CPF C 개발/QA 관리_2_1 — CHECKPOINT TEST AND EVIDENCE — 2026-08-29

## Source

- Checkpoint Source Identity: `1ebf45cd79a528d462c150a73686505a3f2daa7ad1074e5c3bc42a387eefa032` / 8390 files / 43820019 bytes
- Baseline: `e5a652fd0d3d85b764196d066a690065224048c72efcba034e97395a9382cca4` / 8378 files

## 상태 원칙

- 세션 중 수행한 Targeted PASS는 후속 Source 변경으로 영향받을 수 있으므로 최종 Full PASS로 승계하지 않는다.
- 현재 Checkpoint는 Source/정본/검증기/테스트 변경을 보존하는 목적이다.
- 최신 Source에서 미실행/환경 의존 검증을 PASS로 기록하지 않는다.

## 이번 세션에서 실제 관찰된 대표 검증

- Unified CLI/Open Git/DB Consolidation/UTF-8/Generator 관련 targeted regression에서 반복적으로 FAIL 0 상태까지 보정한 구간이 있었음.
- Canonical verifier는 개별 Gate를 전부 PASS시킨 구간이 있었으나 이후 Source 변경이 계속됨.
- DB Python/verification/runtime-tools, Generator lifecycle, Release contract, Security/OpenAPI/Supply-chain, Audit substitute runtime 등을 실행해 결함을 수집·보정함.
- Java25/Gradle 9.1 Actual Open Git Publication은 ChatGPT Linux 실행환경의 외부 Toolchain 다운로드/네트워크 제약으로 최종 Physical PASS를 만들지 못함.
- 사용자의 Windows Local Full Runtime가 최종 Source Physical Evidence authority가 되어야 함.

## 실패/보정 이력의 핵심

- Runtime mojibake: child process encoding boundary를 P0로 재개방, verifier/test 추가.
- Physical legacy DB: active `cmnDB/admDB/batDB/refDB/bzaDB`를 current target으로 허용하지 않도록 P0 currentization.
- VS Code JDT nullness: 8개 전달 Finding과 동일 Root Cause를 Source 보정.
- Transaction DB Logging: 업무 rollback과 독립 로그 transaction + 오류 결과 추적 계약을 P0로 currentize.
- Generator verifier/test stale contract와 generated javac stub gap을 보정.
- Unified CLI: single `cpf` Java CLI + thin wrappers + PUBLIC/INTERNAL capability.
- Open Git: Binary default, Optional source allowlist, `cpf-release/` Private master 비추적, Tool 자동 Git write 0.

## 체크포인트 판정

**INCOMPLETE / CONTINUATION REQUIRED.** 최종 ZIP/QA 완료판정 금지.

## Checkpoint Package Fresh Snapshot 검증

- Overlay files applied: **131**
- Approved delete candidates: **349**
- Baseline에 실제 존재하여 삭제된 파일: **36**
- 이미 부재하여 no-op 처리된 파일: **313**
- Protected-path delete: **0**
- Replacement missing / path escape / directory delete: **0**
- 적용+삭제 후 Product Source Identity: `1ebf45cd79a528d462c150a73686505a3f2daa7ad1074e5c3bc42a387eefa032` / 8,390 files — 작업본과 **exact match**
- `verify-cpf-current-final.py`: PASS
- `verify_nxt3_hygiene.py`: PASS
- `verify_nxt3_repository_garbage.py`: PASS
- `verify-cpf-runtime-utf8-boundaries.py`: PASS (`ProcessStartInfo 29 / redirected 29 / mojibake source 0`)
- `verify-cpf-physical-db-consolidation.py`: PASS (`active legacy references 0`)
- `verify-cpf-unified-cli.py`: PASS (`PUBLIC 9 commands / INTERNAL 4 namespaces`)

이 PASS는 **Checkpoint Overlay 자체의 재현성과 저비용 Source Gate**에 대한 결과이며 Java25/DB3/One-WAS/Browser/Open Git Fresh Physical Runtime 최종 PASS를 의미하지 않는다.
