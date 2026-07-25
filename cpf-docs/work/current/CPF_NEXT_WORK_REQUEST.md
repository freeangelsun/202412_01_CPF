# CPF Next Work Request — R12 Integrated Verification

## 목표
R12 overlay 적용/cleanup/사용자 Push 후 최신 master SHA 하나를 기준으로 **Source/SQL/API/Test/Runtime/Evidence 일치 여부를 통합 검증**한다. 새 기능 개발을 먼저 시작하지 않는다. 검증에서 실제 결함이 발견된 경우에만 원인 패턴을 전수 조사해 보정한다.

## 선행 조건
- 최신 master SHA 확인
- `CPF_FINAL_TARGET_REQUIREMENTS.md`, `CPF_CURRENT_WORK_REQUEST.md`, `CPF_R12_HANDOVER.md`, R12 Review 확인
- worktree clean 확인

## 필수 검증
1. `check-r12-product-hardening.ps1`
2. `check-migration-checksums.ps1`
3. `gradlew.bat clean test assemble --no-daemon`
4. ADM/BZA npm test/build
5. MariaDB clean install + migration/upgrade/rollback + verify
6. Audit 정상/DB down/retry/FAILED/manual retry/restart/multi-instance
7. BAT Ghost null owner/other lock/race/event failure + scheduler/worker/center-cut
8. Generated Domain create/compile/test/local boot + product memory fail-fast + public boundary
9. Calendar create/version/delete conflict + product cmnDB fail-fast + DB-less read-only + multi-instance change propagation
10. Gateway header propagation/local/remote E2E
11. Jenkins selected/full module build
12. `verify-full-product.ps1 -WithDatabase -WithGeneratorLifecycle -WithBrowser -RequireAll`

## Evidence
commit SHA, command, profile, environment, start/end, requirement/QA, PASS/FAIL/SKIPPED, sanitized raw logs를 현재 commit 기준 경로에 보존한다. SKIPPED가 하나라도 있으면 Release PASS 금지.
