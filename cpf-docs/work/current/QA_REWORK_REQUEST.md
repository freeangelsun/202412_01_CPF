# QA 재개발/재검수 요청 준비 — 2026-08-27

Developer GPT 개발 처리는 `127/127` 완료했다. 그러나 QA 최종 재검수 요청은 아직 제출 상태가 아니다. 필수 실환경 Acceptance가 `미검증`이기 때문이다.

- Current Product Source SHA-256: `79264c2975bd0b8504a0e2f8ec375070c08699ebcb512e26323d90d7e39490fb`
- Canonical Product Requirements: `208`
- Developer Closure: `127/127 완료`
- Verification: `7 완료 / 120 미검증`
- 재현 가능한 Static/Contract FAIL: `0`

QA 재검수 전 반드시 다음 Evidence가 필요하다.

1. Java25/Gradle9.1 clean Root Build/Test/Publication/SBOM + Generated Domain.
2. VSCode Fresh Import/JDT Problems Error 0 / Warning 0.
3. Oracle/PostgreSQL/MariaDB physical Fresh/Upgrade/Rollback/Reapply/Fault/Cleanup/Fresh Replay.
4. Kafka-free Batch 5-role + Worker×2 + kill/takeover/fencing/UNKNOWN/reconcile/Center-Cut/Gateway.
5. One-WAS 실거래 + File/DB/Trace/ADM 로그 상관관계.
6. ADM/Backoffice Runtime OpenAPI + Node22.18+ frontend build/test + Browser E2E/a11y.
7. signed Working Tree SHA-256 attestation 기반 Performance Live + required load/soak.
8. Actual Open Git Fresh Binary Release + Golden Path.
9. Full Runtime와 동일 Source Fresh Replay.
10. Codex 독립 중간점검에서 신규/재발 Finding 0 또는 모두 보정/재검증.

필수 `FAIL / SKIP / NOT_EXECUTED / UNKNOWN / Source·Managed drift / Evidence mismatch`가 남으면 QA 완료로 올리지 않는다.
