# CPF 100% Finalization Mandate

- Basis SHA at policy creation: `3ed676061246c9db3e44f29e254c0393ecca3929`
- Scope: Developer GPT / QA A / QA B / Central Integration / Handover / Next Session
- Goal: **다음 개발·검수 사이클에서 남은 미완료 전체를 100% 종료 대상으로 취급**

## 1. 기본 전제

모든 개발·QA 지침은 다음 문장을 기본 전제로 한다.

> 이번 회차는 부분 구현을 위한 회차가 아니다.
> 현재 확인 가능한 미완료·미구현·부분 구현·미검증·실패·재확인 필요 항목을
> 모두 현재 작업 범위로 가져와 100% 완료 상태를 목표로 수행한다.
> 다음 회차로 남겨두는 것을 계획에 포함하지 않는다.

## 2. 개발GPT Scope

개발GPT는 현재 명시된 Requirement/Finding만이 아니라 다음 전체를 배정받는다.

- 최신 최상위 Requirement 전체
- 중앙 Requirement 원장 전체
- 중앙 Finding 전체
- 이전 QA의 미통과/미검증/부분 구현 전체
- Source 개선 후 Runtime/Evidence가 남은 전체
- Runtime/GA Gate 전체
- 개발 중 새로 발견한 결함 전체
- Architecture/Ownership/Consumer/DB/Frontend/Generator/Logging/Security/Recovery/Artifact/Evidence 전체 영향

**“현재 알려진 직접 재개발 N건”은 Scope 제한이 아니다.**
그 숫자는 즉시 수정이 확정된 Known Defect 수일 뿐이다.

## 3. 개발GPT 종료 금지 상태

다음 상태가 남아 있으면 개발GPT는 정상 종료했다고 표현하지 않는다.

- `부분 구현`
- `미구현`
- 구현 가능한데 `미검증`
- Consumer 미연결
- 호출경로 미완성
- SQL/DB3 영향 미반영
- Generator/Generated Domain 미반영
- ADM/BZA/Frontend/OpenAPI 미반영
- 정상 동작만 있고 오류/UNKNOWN/Recovery 미완성
- Security/Audit/Masking 미완성
- 실행 가능한 Test/Gate 미수행
- Evidence 경로/Hash 불일치
- False-Green Gate
- Dead/Stale/Duplicate Source
- 현재 SHA와 무관한 과거 Evidence

외부 환경 없이는 실행할 수 없는 Runtime만 예외적으로 `미검증`으로 남길 수 있다.
그러나 그 경우에도 Source/Test/Script/Config/SQL/Frontend/Generator/Evidence 준비는 100% 완료해야 한다.

## 4. 개발GPT 작업 방식

개발 중 새 문제를 발견하면 “다음 개발요건”으로 넘기지 않는다.

1. Root Cause 확인
2. 동일 원인의 잠복 결함 Repository-wide 검색
3. Source/Test/SQL/API/Config/Frontend/Generator/Script 동시 수정
4. 자체 Requirement 등록
5. 가능한 Runtime 재실행
6. Evidence/Manifest/원장 갱신

사용자 승인이 필요한 Git 쓰기·삭제·보호경로 변경만 예외다.

## 5. QA A/B Scope

QA A와 QA B는 각각 **CPF 전체 100%를 독립 검수**한다.

Primary 영역은:
- 더 깊게 볼 관점
- 중복 검수 피로를 줄이기 위한 순서
- 서로 다른 시각을 확보하기 위한 회전 배정

일 뿐 Scope 분할이 아니다.

QA A도 전체, QA B도 전체를 검수한다.

## 6. QA 종료 금지 상태

QA는 다음 중 하나라도 남으면 제품 PASS를 선언하지 않는다.

- 미검수 Requirement
- 미검수 Finding
- 미확인 Consumer
- 미확인 오류/Recovery
- 실행 가능한데 미수행 Runtime
- Evidence provenance 불명
- current SHA와 불일치
- Architecture 미결정
- False-Green 의심
- P0/P1 미통과
- 신규 결함 미처리
- Transaction/Logging 추적 단절
- Security/Permission/Audit 미검증
- DB3 lifecycle 미검증
- Browser/Multi-instance/Process-kill 등 필수 GA Gate 미검증

환경 때문에 실행할 수 없는 Runtime은 `미검증`으로 기록하지만 전체 제품 완료를 선언하지 않는다.

## 7. QA 자율 Finding

QA는 Requirement에 적힌 것만 확인하지 않는다.

Source/Runtime 검수 중:
- 추가 결함
- 누락
- Architecture 오류
- 잘못된 Ownership
- Consumer 단절
- Dead/Stale/Duplicate
- Security/Recovery/Logging/DB/Frontend/Generator 문제
- Gate false-green
를 발견하면 신규 Finding으로 등록한다.

“내 배정 영역이 아니다”라는 이유로 제외하지 않는다.

## 8. 중앙 통합

중앙 통합자는 개발GPT/QA A/QA B 결과를 단순 병합하지 않는다.

- 더 엄격한 Acceptance 적용
- 이견 보존
- actual Source/Runtime/최상위 Requirement로 판정
- QA가 놓친 결함 추가
- Requirement 자체 오류 수정
- 다음 개발 Scope를 다시 **남은 전체 100%**로 생성

다음 개발지침은 일부 항목만 골라서 전달하지 않는다.

## 9. 진행률

진행률은 항상 전체 종료 기준으로 계산한다.

예:
- Requirement closed x/N
- Finding closed x/N
- Runtime gate PASS x/N
- ADM route x/N
- BZA route x/N
- EDU architecture x/N
- Transaction/Logging acceptance x/N
- Self-found defect closed x/N
- GA canonical axis x/N

특정 Subset 100%를 전체 100%처럼 표현하지 않는다.

## 10. Checkpoint

Checkpoint는 세션 한계 대응용 저장점일 뿐이다.

Checkpoint 상태:
- 최종 완료 아님
- 다음 회차로 계획 이월하는 수단 아님
- 동일 전체 Scope를 이어가기 위한 상태 보존

최종 결과물은 Checkpoint ZIP들을 나열하는 방식이 아니라 모든 변경을 다시 합친 Root Overlay ZIP 하나다.

## 11. 최종 완료 의미

CPF 100% 완료 후보는 다음을 모두 만족해야 한다.

- 현재 Requirement 100%
- 현재 Finding 100%
- 신규 Self Finding 100%
- 구현 가능한 Source/SQL/API/Test/Config/Frontend/Generator/Script 100%
- Consumer/호출경로 100%
- 정상/오류/경계/UNKNOWN/Recovery 100%
- Security/Audit/Masking 100%
- DB3 100%
- Runtime/GA 실행 가능한 항목 100% PASS
- Evidence current-SHA provenance 100%
- False-Green 0
- 미구현 0
- 부분 구현 0
- 검증 가능한 실패 0

외부 Runtime 미검증이 남아 있으면 제품 전체 100% 완료가 아니다.

## 12. 세션 인수인계 강제 문구

모든 CPF 세션 Handover에는 다음을 포함한다.

> 다음 세션은 남은 일부를 처리하는 세션이 아니라,
> 현재 미완료 전체를 100% 종료시키기 위한 연속 세션이다.
> 이전 세션의 부분완료를 완료로 승계하지 않으며,
> 최신 master exact SHA에서 남은 Requirement/Finding/Runtime/자체발견 결함 전체를 다시 가져간다.
