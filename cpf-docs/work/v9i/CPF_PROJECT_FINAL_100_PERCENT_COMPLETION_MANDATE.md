# CPF PROJECT FINAL 100% COMPLETION MANDATE

- Project: Core Platform Framework (CPF)
- Repository: `freeangelsun/202412_01_CPF`
- Basis SHA at policy creation: `3ed676061246c9db3e44f29e254c0393ecca3929`
- Applies to: Central Manager / Developer GPT / QA A / QA B / Codex Review / Handover
- Completion target: **CPF PROJECT 100% COMMERCIAL-READY COMPLETION**

## 1. “100%”의 정의

이 문서와 이후 모든 CPF 개발·QA 지침에서 `100%`는
“지정된 몇 건의 Requirement를 100% 처리”한다는 뜻이 아니다.

**CPF 프로젝트 자체를 이번 요청/회차에서 완성한다는 뜻이다.**

즉 각 작업자는 자신의 역할에서 다음 질문으로 작업한다.

> “이 작업 이후 CPF를 상용 Framework 완료라고 판단하는 데 무엇이 더 남아 있는가?”

남아 있는 것이 있으면 그것도 현재 작업 대상이다.
명시 Requirement에 없다는 이유로 남겨두지 않는다.

## 2. 프로젝트 완료 기준

CPF 프로젝트 100% 완료 후보는 최신 master exact SHA에서 다음이 모두 만족되는 상태다.

- 최상위 Requirement 전체 충족
- 현재 Requirement 전체 완료
- 현재 Finding 전체 해결
- 새로 발견한 Finding 전체 해결
- 부분 구현 0
- 미구현 0
- Consumer 없는 기능 0
- 끊긴 호출경로 0
- False-Green Gate 0
- 잘못된 Ownership 0
- Dead/Stale/Duplicate/Dual-primary 문제 0
- 검증 가능한 실패 0
- Source/SQL/API/Test/Config/Frontend/Script/Generator 정합
- ADM/BZA/Gateway/Batch/EDU/Generated Domain 정합
- Oracle/PostgreSQL/MariaDB 정합
- 정상/오류/경계/부분실패/UNKNOWN/Recovery 정합
- Security/Permission/SoD/Secret/Masking/Audit 정합
- Transaction/Logging/Trace/Batch/External linkage 정합
- Multi-instance/Process Kill/Retry/Reconcile 정합
- Runtime/Performance/Observability/DR/Artifact/Supply-chain 검증
- Documentation/Evidence/Manifest/Hash/current SHA 정합
- QA A/B 독립 전체 검수 통과
- Release Qualification 통과

외부 환경 때문에 실제 실행할 수 없는 Runtime이 있다면 제품 100% 완료 선언은 보류한다.
그러나 현재 환경에서 구현 가능한 Source/Test/Script/Config/SQL/Frontend/Generator/Evidence는 모두 끝낸다.

## 3. Developer GPT의 프로젝트 완료 책임

Developer GPT는 “QA가 준 항목을 수정하는 사람”으로만 행동하지 않는다.

이번 개발 요청을 **CPF 프로젝트를 끝내는 마지막 개발 요청이라고 가정**한다.

Developer GPT는:
1. 현재 전체 Requirement/Finding을 처리한다.
2. 최상위 Requirement 전체를 다시 대조한다.
3. Repository 전체를 검토하여 누락된 구현을 찾는다.
4. 새 결함을 발견하면 즉시 자체 Requirement로 추가한다.
5. 동일 Root Cause의 잠복 결함을 Repository 전체에서 찾는다.
6. 필요한 Source/Test/SQL/API/Config/Frontend/Generator/Script/Evidence를 함께 수정한다.
7. 구현 가능한 항목을 “다음 개발”로 이월하지 않는다.
8. 사용자 승인 필요한 Git 쓰기·삭제만 별도 요청으로 남긴다.

### Developer GPT가 사용하면 안 되는 종료 논리

- “요청받은 34건은 끝났다.”
- “93행은 검토했다.”
- “내 배정 범위는 끝났다.”
- “다음 회차에서 나머지를 처리하면 된다.”
- “QA에서 새로 잡히면 그때 하겠다.”
- “현재 Sprint 범위 밖이다.”

이런 판단은 CPF Finalization 지침 위반이다.

Developer GPT의 종료 질문은 하나다.

> “현재 환경에서 내가 구현할 수 있는 CPF 미비점이 더 남아 있는가?”

남아 있으면 계속 개발한다.

## 4. QA A의 프로젝트 완료 책임

QA A는 특정 영역 QA가 아니다.
Primary 영역이 무엇이든 **CPF 전체 제품을 100% 독립 검수**한다.

QA A는:
- 개발 결과를 신뢰하지 않고 actual Source/Runtime/Evidence를 확인
- 현재 Requirement/Finding뿐 아니라 최상위 Requirement 전체 대조
- 미지정 결함도 신규 Finding
- 다른 QA가 볼 것이라는 이유로 생략 금지
- 제품 완료를 막는 모든 결함을 현재 QA 결과에 포함

QA A의 종료 질문:

> “이 SHA를 상용 CPF 완료로 승인하지 못하게 하는 이유가 하나라도 더 있는가?”

있으면 Finding으로 남긴다.

## 5. QA B의 프로젝트 완료 책임

QA B도 QA A와 동일하게 **CPF 전체 100% 독립 검수**한다.

QA B의 Primary 영역은 관점 차이를 만들기 위한 것일 뿐 검수 Scope 제한이 아니다.

QA B는:
- QA A의 PASS 자동 승계 금지
- Developer PASS 자동 승계 금지
- 실제 Source/Consumer/Runtime/Evidence 재검증
- Architecture/Ownership/보안/복구/Logging/DB/Frontend/Generator/Artifact 등 전체 검수
- 새로운 결함을 적극 탐색

QA B의 종료 질문:

> “QA A가 놓쳤더라도 내가 추가로 찾아야 할 프로젝트 완료 방해 요소가 남아 있는가?”

있으면 Finding으로 남긴다.

## 6. 중앙 통합자의 프로젝트 완료 책임

중앙은 Developer/QA 결과를 전달만 하지 않는다.

중앙은:
- Developer, QA A, QA B 의견과 이견 통합
- 실제 Source와 최상위 Requirement 기준으로 재판정
- 중복 Finding 병합
- QA 누락 보완
- Requirement 자체 오류 수정
- Architecture 방향 확정
- 다음 개발지침에 **남은 프로젝트 전체 미비점** 포함
- “몇 건만 다음에” 식으로 Scope를 축소하지 않음

다음 개발지침을 만들 때 항상:

> “이 개발 결과로 CPF 프로젝트 자체를 끝낸다.”

를 최상위 목표로 둔다.

## 7. QA A/B 영역 순환

A/B Primary 영역은 회차마다 바꾼다.
하지만 양쪽 모두 전체 제품을 본다.

예:
- A Primary: Runtime/Release/Logging
- B Primary: Architecture/ADM/EDU

이 경우에도:
- A는 Architecture/ADM/EDU를 검수한다.
- B는 Runtime/Release/Logging을 검수한다.

P0/Security/Approval/Transaction-Logging/Evidence/Architecture는 항상 양쪽 독립검수한다.

## 8. 중간 산출물

Checkpoint/중간 ZIP/부분 원장은 세션 한계 대응용이다.
프로젝트 Scope를 다음 회차로 계획적으로 분할하는 수단이 아니다.

세션이 끊기면 동일한 **프로젝트 100% 완료 Scope**를 그대로 이어받는다.

## 9. 최종 보고

Developer/QA가 결과 보고서를 만들 때 반드시 다음을 구분한다.

- 프로젝트 완료 여부
- 구현 완료 여부
- Runtime 검증 완료 여부
- 미구현
- 부분 구현
- 미검증
- 실패
- 신규 결함
- 외부 환경 제약
- 사용자 승인 필요 사항

“작업 문서를 만들었으므로 완료”라고 표현하지 않는다.

## 10. 세션 인수인계 강제 문구

모든 다음 CPF 세션 Handover 첫 부분에 다음 의미를 포함한다.

> 이 세션은 CPF의 일부 범위를 처리하는 세션이 아니다.
> CPF 프로젝트 자체를 100% 완료시키기 위한 연속 Finalization 세션이다.
> 각 역할은 자신에게 주어진 목록만 처리하지 않고,
> 프로젝트 완료를 방해하는 모든 미비점을 적극적으로 찾아 현재 역할 범위에서 끝까지 처리한다.
> 부분 구현·미구현을 계획적으로 남기지 않는다.
