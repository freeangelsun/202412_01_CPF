# CPF C 개발/QA 관리_1_8 — 최종 전달 인덱스 — 2026-08-27

## 1. 먼저 볼 파일

1. `CPF_DEVELOPMENT_COMPLETION_REVIEW_20260827.md` — 이번 개발/검수 결과 전체 리뷰.
2. `CPF_FINAL_LOCAL_APPLY_RUNTIME_COMMANDS_20260827.md` — Overlay 적용, Delete Manifest, 저비용 Gate, Full Runtime/Fresh Replay 한 줄.
3. `CPF_NEXT_SESSION_HANDOVER_C_DEV_QA_1_8_20260827.md` — 다음 DevGPT 세션 상세 인수인계.
4. `CODEX_NEXT_WORK_INSTRUCTION_20260827.md` — 현재 Codex turn에 이어서 넣을 다음 작업지침.
5. `CODEX_RESULT_TO_NEXT_WORK_TRACE_20260827.md` — Codex 결과 승계 Trace.

## 2. Canonical Developer 정본

- `CPF_CURRENT_WORK_REQUEST.md`
- `CPF_CANONICAL_DEVELOPMENT_CLOSURE_INVENTORY.csv` — 169행
- `REQUIREMENT_STATUS.csv` — 169행 mirror
- `TEST_AND_EVIDENCE.md`
- `OPEN_ISSUES.md`
- `QA_REWORK_REQUEST.md`
- `CPF_DEVELOPMENT_HANDOVER.md`
- `CPF_NEXT_SESSION_HANDOVER_LONG_TURN_20260827.md`

## 3. Package / Garbage / Integrity

- `CHANGE_MANIFEST.csv` — Baseline 대비 변경 파일.
- `DELETE_MANIFEST.csv` — 승인/선행조건 기반 Root-relative 삭제 원장.
- `PACKAGE_MANIFEST.json` — Source Identity/검증/패키지 상태.
- `SHA256SUMS.txt` — Overlay target 파일 SHA-256.
- `cpf-docs/work/evidence/current/DEVGPT_C1_8_VALIDATION_EVIDENCE_20260827.log` — Current-only DevGPT 실행 증적.

## 4. 실행 순서

Overlay ZIP+sidecar SHA 검증 → Overlay 적용 → approved Delete Manifest → 저비용 Gate + Batch Shell Git trackability → Fresh VS Code Problems 0/0 생성 → Required Full Runtime → 동일 Source Fresh Replay → Codex current turn continuation → Source 수정 발생 시 Final Runtime/Fresh Replay 재실행 → mandatory PASS 이후 QA 재검수.

## 5. 현재 상태

- Current Product Source Identity: `b41abc892e6652ef8461ece1b3daa1057acfbf153185fe8e67ea70a6e20de4af` / 8,340 files.
- Source/Static/Contract 개발 보정: 완료.
- Mandatory Windows physical Final Runtime/Fresh Replay: 미완료.
- Overall QA: **NOT_COMPLETE**.
