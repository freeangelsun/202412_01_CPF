# QA Requirement Detail — CPF-SELF-DEV-S4-003

## 판정

- QA 결과: `미통과`
- 기준 SHA: `2903de14eb9cd6cfcccf8e4d2a3489ee2e4193ca`
- Requirement: Core/Admin/BAT Owner 경계 fail-closed Gate
- QA 회차: `QA-DEV-R1`

## 실제 확인 파일

1. `settings.gradle`
2. `cpf-tools/governance/cpf-product-surface-policy.json`
3. `cpf-tools/scripts/verify-cpf-owner-boundaries.py`
4. `cpf-tools/scripts/verify-cpf-owner-boundaries.ps1`
5. `cpf-tools/scripts/check-core-owner-boundary.ps1`
6. `cpf-tools/scripts/tests/test_verify_cpf_owner_boundaries.py`
7. `cpf-core/build.gradle`
8. `cpf-common/build.gradle`
9. `cpf-admin/build.gradle`
10. `cpf-biz-admin/build.gradle`
11. `cpf-batch/contract/build.gradle`
12. `cpf-batch/runtime-common/build.gradle`
13. `cpf-batch/execution-runtime/build.gradle`
14. `cpf-batch/control-server/build.gradle`
15. `cpf-batch/scheduler/build.gradle`
16. `cpf-batch/worker/build.gradle`
17. `cpf-batch/center-cut-runner/build.gradle`
18. `cpf-batch/host-agent/build.gradle`
19. `cpf-batch/testkit/build.gradle`
20. `cpf-docs/work/development/p00-p05-session4/ENVIRONMENT_VALIDATION_HANDOFF.csv`
21. `cpf-docs/work/development/p00-p05-session4/REQUIREMENT_STATUS.csv`

## 공식 구조 확인

- 공식 Top Level Owner:
  - cpf-core
  - cpf-common
  - cpf-admin
  - cpf-biz-admin
  - cpf-batch
- Settings의 Batch Module:
  - contract
  - runtime-common
  - execution-runtime
  - control-server
  - scheduler
  - worker
  - center-cut-runner
  - host-agent
  - testkit
- Product Surface Policy에는 Public `.api.`, SPI `.spi.`, Internal `.internal.` 경계가 정의돼 있다.

## 현재 Gate가 검사하는 항목

- cpf-admin Java의 일부 타 Owner DB 문자열
- 8개 Batch Source Root 존재
- 일부 과거 Core Batch Runtime import
- Core legacy opt-in token
- `cpf-core/build.gradle`의 직접 cpf-batch dependency 일부

## 미통과 근거

1. Settings에는 Batch 9개 Module이 있으나 Gate 필수 Component는 testkit을 제외한 8개다.
2. Product Surface Policy를 읽지 않는다.
3. 모든 Module `build.gradle` dependency graph를 파싱하지 않는다.
4. 순환·역방향 의존을 계산하지 않는다.
5. Public API/SPI/Internal Package 참조를 검사하지 않는다.
6. cpf-common/cpf-admin/cpf-biz-admin/starter/gateway/reference의 Owner 경계를 전수검사하지 않는다.
7. Consumer 없는 Public 추상화를 검사하지 않는다.
8. Test는 제한된 4개 위반 패턴만 검증한다.
9. 전체 Repository Scan을 개발에서 실행하지 않고 검증 이관했는데 개발 전체 상태를 완료로 기록했다.

## 재개발 요청

- Settings와 모든 Build를 읽어 Project Dependency Graph 생성
- 공식 Owner Policy와 실제 Root/Package 매핑
- Public/SPI/Internal 참조 전수검사
- 역방향·순환·ownerless common 검출
- 9개 Batch Module 포함
- 실제 Consumer 없는 Public Contract 검출
- 모든 위반 유형 Negative Test
- 최신 후보 SHA 전체 Repository Scan

## 성공 기대 결과

- Module graph와 Package import graph Finding 0
- 공식 Owner Root 누락 0
- Internal 외부 참조 0
- 역방향·순환 의존 0
- Consumer 없는 신규 Public Contract 0
