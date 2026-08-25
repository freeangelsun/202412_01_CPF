# CPF Developer GPT Handover — 2026-08-25 Runtime Pending

## Current Source

- Baseline: `CPF_FULL_SOURCE_FOR_NEXT_QA_20260825_121103.zip`
- Baseline ZIP SHA-256: `d2e89aba1841a4387a473610db905415f8565fcf09d06a56a8afa3a1b33a3a48`
- Current Product Source SHA-256: `c79be31a71c15c02665d56e29c0f51244c91ab3894183775ce311cde3dbf40df`
- Canonical Requirement: 208

## 개발 완료

Runtime 로그의 34 FAIL과 VSCode Problem에서 수집한 Root Cause를 Source/Harness/정본에 반영했다. Customer Shared Library Generator, Windows 200자 경로 Gate, Docker prerequisite auto-start/readiness/test-owned cleanup도 상위 정본 Requirement로 추가했다.

## 정적 검증

Canonical 24/24, Testing 385 pass/22 environment skip, Verification 73, DB verification 86, DB 125/2 environment skip, Generator 46/10 environment skip, Runtime tools 76/2 environment skip, Release/Open Git/Security/OpenAPI/Supply/Docker harness FAIL 0. Frontend full compile/workflow/API/golden/substitute 및 Java21 substitute compile/unit/runtime PASS.

## 다음 필수 단계

Windows Java25 + PowerShell 7 + Docker 환경에서 Full Runtime을 실행한다. Harness가 필요한 컨테이너를 자동 시작하고 readiness를 확인하며 검증기가 올린 컨테이너는 종료한다. 결과가 하나라도 FAIL/SKIP_ENV/NOT_EXECUTED/UNVERIFIED이면 QA로 넘기지 않고 같은 Requirement를 다시 개발한다.

## 성공 조건

`FAIL=0 / SKIP_ENV=0 / NOT_EXECUTED=0 / UNVERIFIED=0` + Java25 Root Build + DB3 3사 lifecycle + 2-worker kill/recovery + Browser E2E + Fresh Replay PASS.


## Overlay 전달 규칙

- Baseline 대비 실제 `ADD/MODIFY` 파일만 ZIP payload에 포함한다.
- unchanged 파일은 ZIP에 포함하지 않는다.
- Windows 200자 경로 currentization으로 제거할 기존 경로 220건은 `DELETE_MANIFEST.csv`로 적용한다.
- 현재 Source의 Java25/Docker Full Runtime이 실제 PASS하기 전에는 `Runtime Pending` 상태를 유지한다.
- Currentization time: `2026-08-25T17:48:16+09:00`

- Fresh Overlay Replay: PASS (10,462 files, missing/extra/hash diff 0, unchanged payload 0).

## 최종 변경 전용 Overlay

- 파일명: `CPF_DEVELOPER_GPT_OVERLAY_RUNTIME_PENDING_20260825_175507.zip`
- Baseline 대비 `ADD 475 / MODIFY 93 / DELETE 220`
- ZIP에는 ADD/MODIFY만 포함하며 unchanged 파일은 포함하지 않는다.
- DELETE 220건은 `cpf-docs/deliverables/DELETE_MANIFEST.csv`로만 적용한다.
- Current Product Source SHA-256: `c79be31a71c15c02665d56e29c0f51244c91ab3894183775ce311cde3dbf40df`
- 최종 live Runtime은 Java25 + PowerShell 7 + Docker 환경에서 미검증이다.

Currentized at: `2026-08-25T17:55:49+09:00`
