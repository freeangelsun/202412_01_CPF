# CPF R3 Requirement 작성·검수 표준

## 기능 Requirement의 최소 정보

각 행은 목적, Owner, 실제 Consumer, Trigger, 선행조건, 입력, 기본값, 처리 순서, 상태전이, 출력, 오류, 동시성, Timeout, Retry, UNKNOWN, 대사·복구·보상·Rollback, 인증·권한·Data Scope·Masking·사유·승인·감사, Log·Metric·Trace, DB, API, Frontend, Generator, 문서, Acceptance, Verification, Evidence, 회귀 방지를 모두 기록한다.

## 개수 부풀리기 금지

Browser×Viewport×State 같은 검증 조합은 Scenario 원장에 둔다. Requirement는 실제 개발해야 할 계약·기능·데이터·업무행위만 센다.

## 순서 변경 금지

선행 Phase Gate 미통과 상태에서 후행 Phase의 계약을 확정하거나 완료 처리하지 않는다. 긴급 병렬 작업은 Public Contract·DB Schema·Shared Config를 변경하지 않는 경우만 허용한다.

## 완료 금지

Interface·DTO·Marker·Table·메뉴·Route·Mock·문서·정적 PASS만 존재하거나 실제 Consumer, 상태전이, 실패 처리, 복구, 권한, 감사, 최신 exact-SHA Evidence가 누락되면 완료가 아니다.

## 최종 Closure

최종 SHA에서 Repository Inventory를 다시 생성한다. 미등록 Source/API/SQL/Config/Frontend/Script/Test/Doc/Evidence가 한 건이라도 발견되면 같은 원장에 Requirement를 추가하고 개발·검수 순환을 다시 수행한다.

## 분할 파일 정본 규칙

대용량 Master는 메인 인덱스 CSV와 순서가 명시된 Part CSV 집합으로 구성할 수 있다.
논리 원장은 모든 Part를 순서대로 결합한 전체이며, 일부 Part나 메인 인덱스만으로 상태를 판정하지 않는다.

## R5 8MB 논리 원장 기준

Requirement·Scenario·Execution Sequence 메인 CSV는 삭제하지 않고 분할 인덱스로 유지한다.
실제 데이터는 각 8,000,000 bytes 미만 Part에 저장한다.
모든 Part를 `part_sequence` 순서로 결합해야 하며, 일부 Part만으로 작업 범위나 완료 상태를 판정하지 않는다.
