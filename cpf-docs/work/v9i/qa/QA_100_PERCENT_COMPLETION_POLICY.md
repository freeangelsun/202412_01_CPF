# [최상위 QA 선언] 이번 QA의 100% = CPF 프로젝트 자체의 최종 완성 검수

QA A/B 각각의 목표는 배정목록의 100%가 아니라 **CPF 프로젝트 전체가 상용 완료인지 100% 검증**하는 것이다.

각 QA는 자신의 Primary 영역 외에도 프로젝트 완료를 방해하는 결함을 적극적으로 찾는다.
미검수/미통과/미검증/Architecture 미결정/False-Green이 남아 있으면 제품 PASS를 선언하지 않는다.

최상위 운영정책:
`cpf-docs/work/v9i/CPF_PROJECT_FINAL_100_PERCENT_COMPLETION_MANDATE.md`

---

# CPF QA 100% 독립 검수 정책

Basis SHA at creation: `3ed676061246c9db3e44f29e254c0393ecca3929`

## 핵심 원칙

QA A/B의 Primary 영역은 **검수 시각을 다르게 하기 위한 역할 분담**일 뿐 Scope 분할이 아니다.

각 QA는 매 회차 최종적으로:
- 최상위 Requirement 전체
- 중앙 Requirement 전체
- 중앙 Finding 전체
- Developer self-found Finding 전체
- Product Source/SQL/API/Test/Config/Frontend/Generator/Script
- Consumer/호출경로
- Runtime/GA/Evidence
를 100% 검수한다.

Primary 영역은 더 깊게 보고, 나머지 영역도 누락 없이 검수한다.

## 완료 목표

QA는 “배정된 부분 완료”로 종료하지 않는다.
제품 목표는 `미검수 0 / 미통과 0 / 검증 가능한 실패 0`.

환경상 실행 불가능한 Runtime은 `미검증`으로 남길 수 있으나:
- Source/Test/Script 준비
- 환경/명령/기대 Evidence
- 재실행 조건
을 기록해야 한다.

`미검증`이 있으면 제품 100% 완료를 선언하지 않는다.

## 독립성

- Developer PASS 자동승계 금지
- 상대 QA PASS 자동승계 금지
- 과거 SHA Evidence 자동승계 금지
- Static marker/self-test만으로 PASS 금지
- Requirement 밖 신규 결함도 신규 Finding
- False-green Gate도 Finding
- Architecture 자체가 잘못되면 Requirement 재판정 요청

## A/B 순환

회차마다 Architecture/Runtime/Security/Frontend/DB/EDU/Logging 등의 Primary를 교환한다.
다만 P0, Security, Approval, Transaction/Logging, Evidence provenance, Architecture는 양 QA 모두 독립 검수한다.

## 최종 중앙 통합

A/B가 각각 100% 검수한 결과를 중앙에서 병합한다.
한쪽만 PASS면 완료가 아니다.
이견은 보존하고 실제 Source/Runtime/최상위 Requirement로 중앙 판정한다.


## Finalization 강제

QA A/B는 “이번 회차에서 일부만 검수하고 나머지는 다음 QA에서”라는 운영을 하지 않는다.

각 QA는 현재 전체 Requirement/Finding/Runtime/GA 범위를 100% 검수 대상으로 잡는다.
환경 제약으로 실제 실행 불가한 Runtime만 `미검증`으로 남길 수 있다.

미검수/미통과/부분검증/재확인 필요가 남아 있으면 QA 작업 자체는 결과 보고서를 만들 수 있어도
제품은 PASS가 아니다.

QA Report는 반드시:
- 전체 분모
- 완료/통과 분자
- 미통과
- 미검증
- 신규 Finding
- 추가 개발 필요
를 명시한다.
