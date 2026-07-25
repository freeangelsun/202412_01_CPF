# CPF Current Work Request — R11 적용 후 통합 검증

## 0. 기준

- Repository: `https://github.com/freeangelsun/202412_01_CPF`
- Branch: `master`
- R11 Source 작업 기준 SHA: `b6db56f5ee745558a59ce511ad681216004b9672`
- 최상위 목표 정본: `cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md`
- 구현 결과: `cpf-docs/work/review/20260725_05/CPF_R11_IMPLEMENTATION_REPORT.md`
- 정적 검증: `cpf-docs/work/review/20260725_05/CPF_R11_STATIC_VALIDATION.md`
- 인수인계: `cpf-docs/work/state/CPF_R11_HANDOVER.md`
- 통합 검증 계획: `cpf-docs/work/current/CPF_INTEGRATED_VERIFICATION_PLAN.md`

## 1. 현재 해야 할 일

R9/R10/R11의 개발 지시를 이름만 바꾸어 반복하지 않는다. R11 Overlay 적용과 cleanup 이후 **현재 master 전체를 통합 검증**한다.

1. Worktree/HEAD/overlay 적용 상태 확인
2. R11 Source/Product Gate 실행
3. Gradle 전체 및 선택 Module build/test
4. ADM/BZA npm test/typecheck/build
5. DB empty install / reinstall / upgrade / rollback
6. Generator arbitrary domain + EXS create/verify/remove/regenerate parity
7. Local/Remote ServiceCall, Header/transactionId, AOP/Execution Catalog
8. Batch/Scheduler/Worker/Center-Cut standalone 및 다중 인스턴스
9. timeout/retry/target-down/failover/UNKNOWN_RESULT/재처리
10. Code/Config/Message/Calendar 변경의 다중 인스턴스 반영
11. ADM/BZA 권한/메뉴/공통 Code Select/기간/UI Browser E2E
12. Gateway 외부/내부 호출 경계
13. Evidence sanitize 및 Repository Hygiene

## 2. 실패 시 처리

- 검증 실패가 나오면 해당 Requirement를 `실패` 또는 `부분 구현`으로 다시 승격하고 실제 Source/SQL/API/Test를 수정한다.
- 단순 환경 미구성은 `미검증`으로 기록하고 성공으로 바꾸지 않는다.
- 검증에서 새 Source Gap이 확인되기 전에는 과거 R9/R10 완료 작업을 다시 구현하지 않는다.

## 3. 보호할 성공 구조

- Generated Domain → `com.cpf.core.api.*` / `com.cpf.core.spi.*`
- BAT Owner의 Batch/Center-Cut Runtime
- `UNKNOWN_RESULT` 명시 상태
- ADM/BZA fail-closed 권한
- 중앙 DB Vendor Pack / Generator Golden Template
- 표준 Header/transactionId/segment, ServiceCall, Broker Reliability, Fixed-Length, Idempotency/Reconciliation
- EXS는 고정 Module이 아니라 Generated Domain

## 4. 완료 금지 조건

- 실행하지 않은 Gradle/DB/Browser/다중인스턴스 테스트를 PASS로 기록
- `cpf-common.utils` Consumer가 남은 상태에서 legacy package 강제 삭제
- Generated Domain에 `com.cpf.core.common.*` import 재도입
- ADM이 Owner DB를 직접 접근하는 신규 코드 추가
- 결과불명을 성공/실패로 추정
- Evidence 없이 과거 다른 PC의 성공 결과를 현 Commit 성공으로 승계

현재 요청서에는 새로운 기능 backlog를 두지 않는다. **통합 검증 결과에서 확인된 실제 실패만 다음 수정 대상으로 만든다.**
