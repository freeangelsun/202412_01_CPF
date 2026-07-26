# CPF Current Work Request

기준 master: `9253097086322c0eacc00c005e944b132e31ae06` (`20260726_04`)

R15/R16/R17 제품 구현 Overlay는 `CPF_FINAL_COMPLETION_20260726` 패키지에 정리되었다.
현재 작업 요청은 **새 기능 개발이 아니라 적용된 최종 SHA 기준 통합 검증과 Evidence 확정**이다.

필수:
- Java 25 전체 clean/test + 5 BAT bootJar/3 Library
- MariaDB fresh/install/upgrade/rollback/reapply/drift
- Control 2 / Scheduler 2 / Worker N / Center-Cut N / Agent
- Leader kill/takeover/old leader fencing
- Worker claim/takeover/duplicate execution 0
- Center-Cut pause/drain/unknown/reconcile/failed-only retry
- Host Agent mTLS/signature/tamper/path/shell rejection
- Remote install/start/health/upgrade/rollback
- ADM Approval/권한/자기승인 차단/UNKNOWN UX Browser E2E
- Domain standalone create/build/remove/regenerate parity
- SBOM/License/CVE/Signature/Provenance/Can-Deploy

실행하지 않은 검증은 PASS 처리하지 않는다. 검증 중 발견된 구현 결함은 즉시 수정하고 동일 시나리오를 재실행한다.
