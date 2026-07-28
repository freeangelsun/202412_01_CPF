# CPF 20260728_02 Final Completion Package Manifest

## 기준

- Repository: `https://github.com/freeangelsun/202412_01_CPF`
- Branch: `master`
- 적용 기준 Commit: `ecaddd581a88ede22b63116effd61313744b3fbe`
- Package: `CPF_20260728_02_FINAL_COMPLETION_ROOT_OVERLAY.zip`
- Commit/Push/Branch: 수행하지 않음

## 포함 범위

- Runtime Control 상태 계약 및 실제 Consumer 연결
- ADM Runtime Change Center UI/API/권한 SQL
- Gateway replayable spool/stream transport 및 인증·인가 보완
- Batch 실제 Consumer 5개 Runtime 정책
- External Institution Runtime 적용
- Generator 계약 및 3개 공식 DB migration/rollback/checksum
- 적용·검증 스크립트, Current/Next Request, Remaining Matrix, Handover, Evidence

## 직접 검증

- Overlay static validator PASS
- Runtime API/state independent compile/behavior PASS
- Gateway transport/spool/security behavior PASS
- Batch 5 actual-consumer policies behavior PASS
- External Institution applier behavior PASS
- ADM TypeScript syntax PASS
- DB source/runtime/checksum parity PASS
- Targeted secret scan PASS

## 미검증

- Java 25 / Gradle 9.1 전체 build/test
- Oracle/PostgreSQL/MariaDB 실DB install/upgrade/rollback
- ADM Browser와 Gateway 실제 Runtime E2E
- 다중 인스턴스·부분 실패·재시작 복구
- ACC/MBR/EXS Generator 실행 parity
- QA Inventory 1,214건 / Scenario 201건 최신 Commit 재판정

실행하지 않은 검증은 성공으로 기록하지 않았다.
