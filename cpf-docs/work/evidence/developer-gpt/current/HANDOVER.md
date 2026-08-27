# CPF Development Handover — C 개발/QA 관리_1_7 — 2026-08-27

## 현재 상태

- Baseline ZIP: `CPF_FULL_SOURCE_FOR_NEXT_QA_20260826_195052.zip`
- Baseline ZIP SHA-256: `00abb643557a9562ff3aa40f088c8791af4e01d0cfb056e5509f70d146b90ec0`
- Current Product Source SHA-256: `79264c2975bd0b8504a0e2f8ec375070c08699ebcb512e26323d90d7e39490fb`
- Product Source: `8,334` files / `38,599,665` bytes
- Canonical Requirements: `208`
- Developer Closure: **127/127 완료**
- Verification: **7 완료 / 120 미검증**
- QA 최종 완료: **아님**

## 이번 개발 핵심

- Compile/JDT Root Cause 보정.
- Logging canonical lineage + 실제 EDU probe currentization.
- Security Context/Execution Scope False Red 제거.
- Generator MBR/EXS idempotency owner fix.
- Batch 5-role standalone Shell 20 + profile 15.
- DB lifecycle Working Tree SHA-256 provenance.
- Performance Live 64-hex signed source identity trust.
- ADM/Backoffice route/consumer/approval static closure.
- generated cache/.class garbage 정리 및 Delete Manifest 보강.

## 실환경 필수 재검증

Java25/Gradle9.1/VSCode, DB3, Batch 2-worker kill/UNKNOWN/reconcile, One-WAS, File↔DB↔ADM logging, Runtime OpenAPI, Node22.18+ Browser/a11y, Performance Live, Actual Open Git Fresh Release, Full Runtime/Fresh Replay.

## 다음 순서

1. 사용자 Local Working Tree에 Overlay 적용 + Delete Manifest 적용.
2. Source Identity 재계산.
3. Java25/Windows 최대강도 Full Runtime 실행.
4. 결과 `CPF_REQUIRED_FULL_RUNTIME_*.log` + `CPF_LOCAL_VALIDATION_*.zip`을 기준으로 FAIL 전체를 Root Cause별 재개방.
5. Codex는 `CODEX_MID_REVIEW_INSTRUCTION.md`로 독립 검수·직접 보정.
6. Codex 수정 시 Source + 관련 문서 + Codex Evidence 동시 currentization.
7. 다시 Full Runtime/Fresh Replay.
8. 모든 mandatory PASS 후 QA 재검수 요청.

Git은 RT-02 provenance read-only 외 사용하지 않는다. 사용자 승인 없이 Commit/Push/Delete/History 변경을 수행하지 않는다.
