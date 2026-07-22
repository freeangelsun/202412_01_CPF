# CPF Stabilization Report

## 1. 기준 상태

- Repository: `freeangelsun/202412_01_CPF`
- Branch: `master`
- 정리 검수 기준 Commit: `8da2e6f395d72ce032e52ea16fef0d1d5f490c5b`
- 적용 후 실제 작업 기준: 문서 Overlay Push 이후 최신 HEAD
- DB: 회사·집 PC 모두 CPF Schema/Table이 없는 초기 상태
- 종합 판정: **부분 구현**

## 2. 확인된 보존 범위

- 공식 `cpf-*` Module과 Java/Frontend Source는 정리 Commit에서 삭제되지 않음
- 활성 설치 SQL, Flyway, Rollback과 Script는 보존됨
- 핵심 요청·목표·Gap·검수 문서는 보존됨
- Stale Evidence, Generated Matrix, 조기 DOCX, 중복 README, 조기 Release Notes는 의도적으로 제거됨

## 3. 즉시 안정화 대상

- README UTF-8와 Link 정상화
- 역할이 다른 Guide의 중복 내용 분리와 상세 내용 복구
- 문서 기준 Commit과 최신 HEAD 정렬
- 삭제 산출물을 참조하는 Quality Gate 재설계
- PowerShell 7 우선과 UTF-8 보존
- `package-lock.json`, Flyway Checksum, Hash/Binary Integrity
- Rename 후 Package/Config/SQL/Route/Seed 전수 확인
- 빈 MariaDB Empty Install과 Product Meta 재생성

## 4. DB 안정화 계약

기존 DB 잔존 상태에 의존하지 않는다. 정본 설치 구조로 다음을 생성한다.

- 정확한 CPF Schema Allowlist
- Service User와 최소 Grant
- Table, Column, PK/FK/UK/Check/Index/Constraint
- Mandatory Product Meta
- Optional EDU/Sample/Test Seed 분리
- Verification Query
- Reinstall, Upgrade, Rollback과 Recovery

`cmnDB`는 Sample Table 1개만 유지하고, 고정길이 전문은 Core, 기관별 Adapter는 External, 업무 채번 Sample은 BZA가 담당한다.

## 5. 현재 미검증 범위

- Clean Full Build와 Test
- Frontend npm ci/lint/typecheck/test/build
- Empty MariaDB Install
- 전체 Module Boot와 API E2E
- Center-Cut/Agent Multi-instance와 Failure Recovery
- ADM/BZA Browser E2E
- Split Deploy, Upgrade와 Rollback

실행 전에는 모두 `미검증`으로 유지한다.

## 6. 다음 완료 기준

`CPF_CURRENT_WORK_REQUEST.md`의 Phase와 완료 금지 조건을 모두 적용하고, 최종 검수에서 Source·SQL·API·Test·Guide·Evidence가 같은 Commit을 가리킬 때만 안정화 완료로 판정한다.
