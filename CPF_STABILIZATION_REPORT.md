# CPF Stabilization Report

## 기준

- Repository: `freeangelsun/202412_01_CPF`
- Branch: `master`
- 기준 Commit: `a63380e6c736fa9c5ae7e425d0e301d21ef3b848`
- 상태: 대규모 Module·Package·DB 표준화 중단 지점의 WIP
- 직접 실행: 이 문서 작성 환경에서는 Build·MariaDB·Runtime·Browser 직접 실행 미수행

## 종합 판정

**부분 구현**

최신 Git에는 Module 표준화, 광범위한 `PFW → CPF`·Package 변경, Generator·Evidence·SQL·ADM/BZA·Batch 관련 구현이 존재한다. 그러나 Empty MariaDB 설치, 최종 DB Ownership, 전체 Module Runtime, Center-Cut, Agent Failover, ADM/BZA Browser, 독립 배포, Upgrade·Rollback과 최신 Evidence가 확인되지 않아 완료로 판정할 수 없다.

## 사용자 확인 환경 상태

- 회사 PC에서 기존 CPF Schema와 Table은 이미 삭제됨
- 집 PC는 신규 MariaDB이며 CPF Object 없음
- 다음 작업은 빈 DB 정본 재구축부터 수행

## 최우선 안정화 항목

1. 최종 Schema·Table·Constraint·Index·Meta Seed·Service User 표준 확정
2. Install·Reset·Provision·Migration·Rollback·Seed·Verify 분리
3. `cmnDB` 최소 Sample Table 1개 정책 적용
4. 고정길이 전문 Core Ownership과 External 기관별 Adapter 분리
5. Common 채번 제거 및 BZA 선택형 Customization Sample 전환
6. Batch Physical Ownership·`batDB`·CenterCutRunner·Agent/Worker 정합성
7. Source·SQL·Config·API·UI·Generator·Test·Guide 물리명 일치
8. Empty Install부터 Runtime·Browser·Upgrade·Rollback Evidence 재생성

## 완료 금지

정적 검색, Class·Table 존재, 과거 Evidence, 일부 Test 통과 또는 Codex 보고만으로 완료 처리하지 않는다. 직접 확인하지 못한 항목은 `미검증` 또는 `재확인 필요`로 유지한다.
