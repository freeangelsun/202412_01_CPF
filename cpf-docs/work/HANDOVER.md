# CPF 현재 개발·검수 Handover

## 기준

- 입력 baseline provenance: `4b6f96796c3bf26b1c3324cc4d9b701bd9415acd`
- 이번 Overlay 적용 대상 Content SHA-1: `f566e3865fc7cab3d60916c0db6955f5740bef3a`
- 이번 Overlay 적용 대상 Content SHA-256: `06b63e5fefca33eb7095b524cf7ad50b1d1caf4a9faf1bf4fcb2c9969fa3e820`
- 결과 Content SHA-1: `470ce244d05cdd2674385eb743630e2537f2963c`
- 결과 Content SHA-256: `f049bf01a59cf57bc823ef59656516c867db9cab2aed6262abc26c4d840d2618`
- Git/GitHub 조회·쓰기: 사용자 별도 요청이 없어 수행하지 않음.
- 현재 판정: 개발 GPT Source/Static/독립 재검수 PASS 범위 완료, **Windows FullLocal/QA 최종 완료는 아직 아님**.

## 이번 작업 핵심

- Gradle logical project path를 실제 `apps / runtime / framework / starters / internal` 계층으로 전환. 물리 directory 및 Maven artifact 좌표 유지.
- logical path에 맞춰 settings/project dependency/catalog/generator/publication/verifier를 currentize.
- 일반 개발 진입점을 `cpfHelp / cpfBuild / cpfTest / cpfVerifyFast / cpfVerifyFullLocal / cpfRunLocal / cpfRunBatch`로 정리.
- FullLocal에 Codex 자동화 가능 검수까지 최대 흡수: Transaction/Header, Fixed-Length, Approval/Security, Cache, Messaging/Kafka, Batch crash/UNKNOWN, DB3/Generator, Gateway/Topology, Runtime OpenAPI, Frontend/Browser, Deployment/Performance.
- Source identity/managed-state를 Git-independent content digest로 전환하고 before/after mutation 검증 추가.
- QA-V41-001~009 재개발 요구를 Source/Verifier/Evidence/FullLocal에 반영. Runtime 필요 항목은 미검증으로 유지.
- QA-B3 25건은 전용 Evidence로 재실행: **22 완료 / 3 미완료(008/010/011)**.


- 사용자 적용 후 전체 Source ZIP을 다시 독립 검수해 QA-B3 Evidence를 ignored `.log`에서 package-safe `.txt`로 전환했다.
- ADM/BZA Controller Source ↔ tracked OpenAPI drift를 currentize하고 FullLocal parity stage를 추가했다.
- `cpf-docs/deliverables/**` 날짜 archive가 Windows Path Gate에서 false FAIL하던 규칙을 보호경로 한정 예외로 보정했으며 path-length 검사는 그대로 유지한다.
- 현재 첨부에는 FullLocal 결과 ZIP 자체가 없으므로 실제 Java25/Docker/Browser Runtime closure는 다음 1회 FullLocal 결과로 닫는다.

## 2026-08-16 12:40 FullLocal 재개발 반영

- 사용자 Windows FullLocal은 `[01] JAVA25_ENV`, `[02] PYTHON_ENV`, `[03] SOURCE_IDENTITY`, `[04] MANAGED_STATE_BEFORE`, `[05] RESOURCE_POLICY`까지 PASS 후 `[06] NXT3_22`에서 결과 `logs` directory가 사라져 `Add-Content`가 연쇄 실패했다. 제품 Gate 실패와 오케스트레이터 실패를 분리해 재개발했다.
- FullLocal의 진행 중 로그/Evidence는 사용자 Downloads가 아니라 OS TEMP scratch에 기록하고 완료 시 결과 디렉터리/ZIP으로 복사한다. stage 기록 시 결과 directory를 재보장한다.
- Python validation venv는 Repository `build/**` 밖의 사용자 local/temp cache로 이동해 Garbage/Hygiene와 분리했다. `cpf-tools/build/**`는 계속 제품 Source로 검사한다.
- Generated Domain IA는 `.gradle`, `.pytest_cache`, `build`, `out`, `node_modules` 등 실행 생성 cache를 업무 IA로 세지 않는다. 실제 업무 폴더/Generated Domain 정책 검사는 유지한다. 동일 실패 조건 재현 후 NXT3 22/22 PASS.
- FullLocal 1-WAS runtime에 `LOCAL_FILE_LOG_STANDARD`, `LOCAL_DB_LOG_POLICY_RUNTIME`, `LOCAL_INTEGRATED_LOG_CORRELATION`을 추가했다. FileLog/DB Log/ADM Timeline의 동일 transactionId·traceId 상관관계, recovery pending/quarantine/terminal-loss, process fatal log, secret raw leak를 fail-closed로 확인한다.
- ADM 비밀번호는 stage command 인자로 전달하지 않고 process environment로만 전달하여 실행 로그에 secret이 노출되지 않게 했다.
- 통합로그 static closure 및 관련 verification regression PASS. 실제 FileLog↔DB↔ADM runtime correlation은 다음 Windows Java25/PowerShell7/Docker FullLocal에서만 PASS 판정한다.
- QA-V41-001~009를 이번 FullLocal 수정 영향도로 다시 열어 focused regression 34/34와 performance 3 workload actual dry-run RC=0을 재확인했다.

## 현재 재검수 결과

- NXT3 **22/22 PASS**.
- Testing Tools **366 PASS / 22 SKIP**.
- DB **86/86**, DB verification **75/75**.
- Generator **27 PASS / 10 SKIP**.
- Runtime/Security/Release/OpenAPI **108 PASS / 2 SKIP**.
- Verification **45/45**, Docker fixture **6/6**.
- Gradle logical tree/Starter truth/dependency/admin/owner/supply-chain/zero-footprint PASS.
- 사용자 Windows root path projection **PASS(max 213/240)**, 상대경로 160 초과 26건 warning.

## 다음 1회 로컬 검증

프로젝트 Root에서:

```powershell
pwsh -NoProfile -ExecutionPolicy Bypass -File .\cpf-tools\verification\tools\run-cpf-local-full-validation.ps1 -ResourceProfile local -OutputRoot "$HOME\Downloads" -FullLocal
```

- PowerShell 7.x / Java25 기준.
- Docker Desktop/Engine만 준비하면 필요한 서비스는 단계별로 순차 사용.
- 검증기가 직접 소유한 container만 restart/cleanup.
- 중간 FAIL에도 다음 독립 단계 계속.
- 최종 `CPF_LOCAL_VALIDATION_<timestamp>.zip` 하나 생성.
- FullLocal은 strict exit가 적용되어 FAIL이 남으면 최종 exit code non-zero.

## 남은 미완료

- post-commit exact Git SHA(`QA-B3-008`).
- Java25/DB3/Process Kill/Browser/Topology/Security/Deployment/Performance Runtime Closure(`QA-B3-010`).
- Windows fresh extract + Java25 lifecycle(`QA-B3-011`).
- QA 최종 통과/Canonical Requirement 완료 판정.

## 안전

- 이번 원본 Full Source 대비 삭제 대상 0건.
- 사용자 승인 없는 Git write/delete/history rewrite 없음.
- 기존 Delete Manifest는 history/governance로만 보존하며 이번 Overlay 적용 시 자동 삭제하지 않는다.
- `cpf-tools/build/**`는 logical grouping build source/BOM/plugin을 포함하는 제품 Source로 유지한다.
