# QA Rework Request / Runtime Handoff

- 입력 baseline provenance: `4b6f96796c3bf26b1c3324cc4d9b701bd9415acd`
- 결과 Content SHA-1: `470ce244d05cdd2674385eb743630e2537f2963c`
- 결과 Content SHA-256: `f049bf01a59cf57bc823ef59656516c867db9cab2aed6262abc26c4d840d2618`
- 결과 형태: 미커밋 Root-relative Overlay
- 개발 GPT 판정: **Source/Static/독립 재개발·재검수 범위 PASS, Runtime/Windows/exact-post-commit-SHA 미완료**
- QA 최종 판정: **개발 GPT가 변경하지 않음**.

## QA-V41 재개발 반영

1. Performance mixed profile 선택/환경확장 순서 수정 및 3 workload dry-run 회귀 PASS.
2. baseline provenance와 result content identity 분리. Git-independent content SHA-1/SHA-256 사용.
3. Evidence Integrity를 Manifest↔SHA256SUMS↔CHANGE_MANIFEST 실물 교차검증 + corruption negative fail-closed로 강화.
4. 최종 package bytes 기준 CHANGE_MANIFEST/SHA256SUMS 재생성.
5. 개발 전달 문서/31행 Projection baseline provenance를 `4b6f96796c3bf26b1c3324cc4d9b701bd9415acd`로 정렬. 과거 QA/Codex history SHA는 변경하지 않음.
6. QA-B3 25건을 Finding별 exact command + 전용 Evidence로 분리. 현재 개발 GPT 22 완료 / 3 미완료.
7. FullLocal은 전체 단계 수집/ZIP 생성 후 FAIL>0이면 non-zero(strict exit).
8. managed-state를 전체 product tree before/after로 확대.
9. 이번 재개발은 Full Source ZIP을 입력 정본으로 수행.

## Codex 검수 범위 중 FullLocal에 흡수한 항목

- Transaction/Header, Fixed-Length, Approval/위험조치, Security/OIDC.
- Cache correctness/durable/live, Redis/Valkey failure/reconnect.
- Messaging/Kafka produce/consume/restart persistence 및 DLQ 계약.
- Batch UNKNOWN/Ghost/Fencing, 2-worker crash/UNKNOWN, Process Kill/Reconcile.
- Generated Domain/DB3 static/lifecycle, Gateway/Topology, Runtime OpenAPI.
- ADM/BZA generated consumer, Browser E2E/A11y.
- Gradle projects/help/build/test/QA34/publication, Deployment, Performance/backpressure.

조건이 없는 환경에서는 SKIP_ENV/NOT_EXECUTED로 기록하며 거짓 PASS하지 않는다.


## 사용자 Full Source 재검수 추가 반영

- QA-B3 Evidence를 ignored `.log`에서 package-safe `.txt`로 전환하고 비허용 Evidence 확장자를 integrity Gate에서 fail-closed한다.
- 사용자 Windows CRLF Source byte를 기준으로 result content identity를 재계산한다.
- ADM/BZA tracked pre-runtime OpenAPI를 Controller Source와 currentize하고 FullLocal에 source↔tracked parity Gate를 추가한다.
- 보호 `cpf-docs/deliverables/**` 날짜 archive는 Windows version-directory naming 위반에서만 제외하되 full-path budget 검사는 계속 적용한다.
- 현재 제공물에는 별도 FullLocal result ZIP이 없어 실제 Java25/PowerShell7/Docker/Browser 단계는 미검증을 유지한다.

## QA 재검수 전 남은 실제 환경 조건

- Java25 / PowerShell7 / Gradle 전체 lifecycle.
- DB3/Redis/Valkey/Kafka/Batch Docker live.
- Local 1-WAS/Gateway/topology/process-kill/UNKNOWN/Reconcile.
- ADM/BZA Browser E2E/A11y.
- Security adversarial, fresh deployment, performance/backpressure.
- Windows fresh extract/path/build.
- 적용/commit 이후 exact Git SHA와 QA 최종 판정.

QA/Codex 컬럼과 최종 통과 여부는 개발 GPT가 수정하지 않는다.

## 2026-08-16 12:40 로컬 재개발 추가

- `CPF_LOCAL_VALIDATION_20260816_124024.zip`에서 `[01]~[05] PASS` 후 `[06] NXT3_22` 중 결과 log directory 소실로 `Add-Content`가 연쇄 실패하는 FullLocal shell 결함을 확인·수정했다.
- Generated Domain의 `.gradle` 및 Repository-local Python venv/`__pycache__`, root `.pytest_cache`를 업무 IA/Garbage로 오인하던 runtime-generated-cache false failure를 수정했다. 동일 조건 재현 후 NXT3 22/22 PASS.
- FullLocal 1-WAS에 FileLog standard, DB log policy, FileLog↔DB↔ADM integrated correlation Runtime stage를 추가했다. transactionId/traceId 상관관계, FileLog recovery loss, process fatal log, secret raw leak를 검증한다.
- QA-V41-001~009를 수정 영향도로 재검수했으며 focused 34/34와 performance 3 workload dry-run RC=0을 확인했다. QA 최종 상태는 변경하지 않는다.
- 실제 Windows Java25/PowerShell7/MariaDB/1-WAS logging correlation은 다음 FullLocal에서 재검수해야 하며 미실행 상태를 PASS로 기록하지 않는다.
