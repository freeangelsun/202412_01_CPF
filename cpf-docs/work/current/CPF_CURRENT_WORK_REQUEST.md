# CPF Current Work Request — R12 이후

## 기준
- Base master: `90130d3f34a8483718b4222b57b3618e8fffc919` (`20260726_01`)
- R12 overlay 적용/cleanup 후 사용자가 Commit/Push할 예정이며 새 SHA는 아직 미정이다.
- 최상위 정본: `cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md`

## 현재 개발 Backlog
R12 QA 11개와 작업 중 발견된 직접 연관 Source 결함은 overlay 기준 구현을 완료했다. **현재 알려진 R12 Source 개발 Backlog는 없다.**

다만 Release 완료 판정은 아직 금지한다. 다음 작업은 새 기능 개발이 아니라 최신 사용자 Push SHA를 기준으로 하는 통합검증이다.

## 반드시 먼저 할 일
1. `cpf-tools/scripts/cleanup-r12-obsolete.ps1`로 obsolete lifecycle `V6__bizadm_exs_transaction_identity.sql`을 정확히 삭제한다.
2. R12 overlay 전체를 적용한 뒤 `check-r12-product-hardening.ps1`, `check-migration-checksums.ps1`, `git diff --check`를 실행한다.
3. 사용자 Commit/Push 후 새 master SHA를 기준으로 `CPF_INTEGRATED_VERIFICATION_PLAN.md`를 수행한다.

## 통합검증에서 남은 미검증
- Gradle clean/test/assemble 및 관련 module tests
- ADM/BZA npm test/build, Browser 권한/Calendar/Audit recovery UX
- MariaDB fresh install/migration/upgrade/rollback 및 V6/V29 PRE-GA canonical repair 실제 적용
- mandatory Audit DB down/retry/restart/multi-instance relay
- BAT Ghost race/null-owner/same-job-other-lock/event-insert failure
- Generated Domain 실제 생성/compile/test/local boot 및 memory production block
- Gateway header propagation/E2E
- Multi-instance Calendar/cache propagation
- Jenkins selected/full module build
- Release `verify-full-product.ps1 -RequireAll`

실행하지 않은 항목은 `미검증`이며 과거 Evidence로 승계하지 않는다.
