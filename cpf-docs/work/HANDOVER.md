# CPF 현재 개발·검수 Handover

## 기준

- 입력 baseline provenance: `4b6f96796c3bf26b1c3324cc4d9b701bd9415acd`
- 결과 Content SHA-1: `9f7a088a4282a6b8ff6f1f05adf6b1a744756975`
- 결과 Content SHA-256: `06ef019f7cd01a2007313e292fd4e3dcc9f1875a831c2b938df7de1fc2663129`
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

## 현재 재검수 결과

- NXT3 **22/22 PASS**.
- Testing Tools **366 PASS / 22 SKIP**.
- DB **86/86**, DB verification **75/75**.
- Generator **27 PASS / 10 SKIP**.
- Runtime/Security/Release/OpenAPI **108 PASS / 2 SKIP**.
- Verification **35/35**, Docker fixture **6/6**.
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
