# QA Rework Request / Runtime Handoff

- 입력 baseline provenance: `4b6f96796c3bf26b1c3324cc4d9b701bd9415acd`
- 결과 Content SHA-1: `9f7a088a4282a6b8ff6f1f05adf6b1a744756975`
- 결과 Content SHA-256: `06ef019f7cd01a2007313e292fd4e3dcc9f1875a831c2b938df7de1fc2663129`
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

## QA 재검수 전 남은 실제 환경 조건

- Java25 / PowerShell7 / Gradle 전체 lifecycle.
- DB3/Redis/Valkey/Kafka/Batch Docker live.
- Local 1-WAS/Gateway/topology/process-kill/UNKNOWN/Reconcile.
- ADM/BZA Browser E2E/A11y.
- Security adversarial, fresh deployment, performance/backpressure.
- Windows fresh extract/path/build.
- 적용/commit 이후 exact Git SHA와 QA 최종 판정.

QA/Codex 컬럼과 최종 통과 여부는 개발 GPT가 수정하지 않는다.
