# CPF_REQUEST_011: ADM 거래/오류/감사 통합 관제 UX 기준 보강 / 리포트 HTML 정본화 / 운영 추적 E2E Smoke

## 0. 이번 작업 범위 고정

이번 작업은 온라인 거래 메타와 로그 정책 runtime 검증이 일정 수준 닫힌 상태를 전제로, ADM 운영자가 거래 하나를 기준으로 거래 로그, 오류 로그, 감사 로그, 정책 감사 흐름을 추적할 수 있게 하는 운영 관제 기준을 보강하는 작업이다.

이번 작업은 아래 범위로 고정한다.

```text
선택 범위:
- CPF_STABILIZATION_REPORT.html 실제 HTML 문서 형식 정본화
- ADM 거래 로그 목록/상세 관제 흐름 보강
- ADM 오류 로그 조회/조치 상태 현황 판정 및 최소 보강
- ADM 일반 감사 로그 조회 현황 판정 및 최소 보강
- pfw_log_policy_audit 정책 감사와 adm_audit_log 일반 감사의 연결 기준 정리
- transactionGlobalId / traceId / businessTransactionId 기준 통합 추적 API 또는 query 기준 보강
- ADM 거래 상세에서 거래 로그/오류/감사/정책 감사로 이어지는 API wrapper 또는 UI marker 보강
- smoke-log-policy-runtime.ps1 또는 신규 smoke에서 거래 → 오류 → 감사 추적 흐름 확인
- README / 관리자 가이드 / 운영 매뉴얼 / SQL 가이드 / 기능 구현 매트릭스 / 리포트 반영
```

이번 작업에서 아래 항목은 구현하지 않는다.

```text
이번 실행 제외:
- ADM 브라우저 실제 클릭 자동화
- ADM Cache hit/miss/ttl/eviction/clear 전체 구현
- Redis/Kafka/MQ broker 실연동
- 배치 강제수행/lock/pause/resume 운영 정책 전체 구현
- 온디맨드 배치 구현
- 센터컷 기본 구현체 구현
- 전체 설치 SQL 신규 DB 재실행
- 전체 트랜잭션 가이드 정본화
```

요청 파일은 작업 대상으로 수정하지 않는다.
Git commit, push, branch 생성 지시는 하지 않는다.
별도 수정파일 목록 산출물은 만들지 않는다.
작업 결과는 `CPF_STABILIZATION_REPORT.html`에만 기록한다.

---

## 1. 이번 작업의 핵심 의도

직전 작업에서 아래 항목은 상당 부분 닫혔다.

```text
직전 완료 또는 완료에 가까운 항목:
- dbLogEnabled=N runtime smoke
- requestBodySaveYn=N runtime smoke
- responseBodySaveYn=N runtime smoke
- errorStackSaveYn=N runtime smoke
- active override runtime smoke
- future override 미적용 검증
- DB policy fallback 검증
- CPF default fallback 검증
- ADM 로그 조회에서 transactionGlobalId alias와 traceId 검색 확인
- 감사 로그 API HTTP 200 도달 확인
```

하지만 운영자 입장에서는 아직 아래가 부족하다.

```text
남은 핵심:
- 거래 하나를 기준으로 거래 로그, 오류, 감사, 정책 감사를 한 흐름에서 추적하는 UX/API 기준
- 오류 조치 상태와 조치 이력 관리 기준
- 일반 감사 로그와 정책 감사 로그의 역할 구분 및 연결 기준
- transactionGlobalId / traceId / businessTransactionId 검색 기준의 일관성
- 리포트 파일이 실제 HTML 형식인지 검증
```

이번 작업은 “운영자가 장애/이슈 발생 시 ADM에서 따라갈 수 있는 추적 경로”를 만드는 작업이다.

---

## 2. 먼저 현재 상태 판정

작업 시작 전 실제 소스/SQL/문서/DB 기준으로 아래를 판정한다.

```text
현재 상태 판정 항목:
- CPF_STABILIZATION_REPORT.html이 실제 HTML 구조인지
- check-html-docs.ps1이 리포트 HTML 구조를 제대로 검증하는지
- ADM 거래 로그 목록 API 상태
- ADM 거래 로그 상세 API 상태
- ADM 오류 로그 API 존재 여부
- ADM 오류 조치 상태 변경 API 존재 여부
- ADM 오류 조치 이력 테이블/API 존재 여부
- ADM 일반 감사 로그 API 상태
- pfw_log_policy_audit 조회 API 상태
- adm_audit_log와 pfw_log_policy_audit의 역할 구분 상태
- transactionGlobalId 검색 지원 여부
- traceId 검색 지원 여부
- businessTransactionId 검색 지원 여부
- 거래 상세에서 오류/감사/정책 감사로 이동 가능한 기준
- ADM UI marker 또는 API wrapper 상태
```

상태값은 아래만 사용한다.

```text
완료
부분 구현
미구현
미검증
실패
재확인 필요
```

Codex 완료 메시지나 과거 리포트만 믿지 말고 실제 파일/DB 기준으로 확인한다.

---

## 3. CPF_STABILIZATION_REPORT.html 실제 HTML 정본화

`CPF_STABILIZATION_REPORT.html`은 실제 HTML 문서여야 한다.

필수 기준:

```text
- <!DOCTYPE html> 또는 <!doctype html>
- <html lang="ko">
- <head>
- <meta charset="UTF-8"> 또는 <meta charset="utf-8">
- <title>
- <body>
- h1/h2/h3 또는 section 구조
- table 또는 상태 목록 구조
- 실행 명령과 실제 결과 구분
- 미구현/미검증/실패 구분
```

`check-html-docs.ps1`도 아래를 검증해야 한다.

````text
- 루트 CPF_STABILIZATION_REPORT.html 포함
- <!DOCTYPE 또는 <html 존재
- <head> 존재
- <body> 존재
- <title> 존재
- Markdown heading 잔재 "# "로 시작하면 실패
- ``` code fence 잔재 있으면 실패
````

완료 불인정:

```text
- .html 확장자지만 Markdown 문서
- 첫 줄이 "# CPF 안정화 리포트" 형태
- check-html-docs가 리포트 HTML 오류를 잡지 못함
```

---

## 4. ADM 거래 로그 관제 보강

ADM 거래 로그는 운영자가 가장 먼저 보는 진입점이다.

필수 기준:

```text
- 거래 로그 목록 조회
- 거래 로그 상세 조회
- businessTransactionId 검색
- transactionGlobalId 검색
- traceId 검색
- logType 검색
- moduleCode 또는 module 검색
- 기간 검색
- 상세에서 request/response/error detail 확인
- 마스킹 적용 여부 확인
```

가능하면 DTO 또는 응답에 아래 연결 키를 명확히 포함한다.

```text
- transactionGlobalId
- businessTransactionId
- traceId
- spanId
- logIdx
- errorId 또는 errorLogId
- auditId 또는 auditLogId
```

---

## 5. ADM 오류 로그 관제 기준

별도 `pfw_error_log` 테이블이 없고 `pfw_transaction_log` 기반 오류 로그만 있으면 그렇게 명확히 판정한다.

필수 판정/보강 항목:

```text
- 오류 로그 전용 테이블 존재 여부
- 오류 로그 전용 API 존재 여부
- 거래 로그에서 FAILURE / ERROR logType으로 오류 조회 가능한지
- transactionGlobalId 기준 오류 조회 가능 여부
- traceId 기준 오류 조회 가능 여부
- 오류 조치 상태 컬럼 존재 여부
- 오류 조치 이력 존재 여부
- 조치자 / 조치 사유 / 조치 시각 기록 가능 여부
```

이번 작업에서 오류 조치 기능을 완성하지 못하면 아래처럼 명확히 기록한다.

```text
허용:
- 오류 로그는 거래 로그 기반으로 조회 가능
- 오류 조치 상태/이력은 미구현 또는 부분 구현으로 기록
- 다음 요청 후보로 분리

불허:
- 오류 조치 기능이 없는데 완료로 기록
```

---

## 6. ADM 감사 로그 관제 기준

일반 감사와 정책 감사의 역할을 구분한다.

```text
adm_audit_log:
- ADM 운영 조치 감사
- cache refresh
- 거래 메타 scan/inactive
- 로그 정책 변경 조치
- 운영자 action 기록

pfw_log_policy_audit:
- 로그 정책 도메인 변경 감사
- policy create/update
- override create/disable
- before/after value
- reason
```

필수 기준:

```text
- 일반 감사 로그 조회 API 상태 확인
- 정책 감사 로그 조회 API 상태 확인
- transactionGlobalId 기준 감사 조회 가능 여부 확인
- operatorId 기준 감사 조회 가능 여부 확인
- actionType 기준 감사 조회 가능 여부 확인
- reason 필드 존재 여부 확인
- beforeValue / afterValue 존재 여부 확인
```

이번 작업에서 통합 UX가 어렵다면 최소한 아래를 한다.

```text
- ADM 거래 상세에서 일반 감사와 정책 감사의 조회 기준을 문서화
- 기능 매트릭스에 일반 감사/정책 감사 상태를 분리
- API가 없으면 미구현으로 명확히 기록
```

---

## 7. 통합 추적 API 또는 API wrapper 기준

가능하면 ADM에 통합 추적 API를 추가한다.

권장 API:

```text
GET /adm/api/observability/transactions/{transactionGlobalId}
GET /adm/api/observability/traces/{traceId}
GET /adm/api/observability/business-transactions/{businessTransactionId}
```

응답은 최소 아래를 포함한다.

```text
- transaction summary
- transaction logs
- error logs 또는 failure logs
- audit logs
- policy audit logs
- related batch executions 있으면 링크 정보
```

이번 작업에서 통합 API 구현이 크면 다음 방식으로 축소 가능하다.

```text
허용 축소:
- 기존 로그/감사 API에 검색 조건 보강
- ADM UI wrapper에서 각 API 호출 경로만 연결
- 운영 매뉴얼에 추적 순서 문서화

불허:
- 문서에만 통합 관제 가능이라고 쓰고 API/검색 조건 없음
```

---

## 8. Runtime smoke 기준

기존 `scripts/smoke-log-policy-runtime.ps1` 또는 신규 smoke를 보강한다.

필수 확인:

```text
- 성공 거래 로그 조회
- 실패 거래 로그 조회
- transactionGlobalId 검색
- traceId 검색
- businessTransactionId 검색
- 거래 로그 상세 조회
- 일반 감사 로그 API 도달
- 정책 감사 로그 API 도달
- 오류 로그 또는 failure log 조회
```

가능하면 결과 JSON에 아래를 남긴다.

```text
admObservability:
- transactionLogList
- transactionLogDetail
- transactionGlobalIdAlias
- traceSearch
- businessTransactionSearch
- errorLogQuery
- auditLogQuery
- policyAuditQuery
```

---

## 9. SQL / Flyway / all_install 기준

이번 작업에서 SQL 변경이 있으면 아래를 모두 반영한다.

```text
- Flyway migration 추가
- all_install SQL 반영
- smoke SQL 반영
- table comment / column comment
- 공통 감사 컬럼
- FK/index
- SQL 가이드 반영
- 기능 매트릭스 반영
- CPF_STABILIZATION_REPORT.html 반영
```

전체 설치 SQL 재실행은 이번 요청 필수 범위가 아니다.
실행하지 않으면 미검증으로 기록한다.

---

## 10. 문서 반영 기준

아래 문서를 실제 결과에 맞게 갱신한다.

```text
README.md
specs/관리자_가이드.html
specs/운영_매뉴얼.html
specs/SQL_가이드.html
specs/기능_구현_매트릭스.html
CPF_STABILIZATION_REPORT.html
```

문서에는 아래를 분리해서 기록한다.

```text
- 완료
- 부분 구현
- 미구현
- 미검증
- 실패
- 다음 보강
```

---

## 11. 테스트 / 검증 명령

가능한 범위에서 아래 명령을 실행한다.

```powershell
.\gradlew.bat :pfw:test --offline
.\gradlew.bat :adm:test --offline
.\gradlew.bat test --offline
.\gradlew.bat qualityGate --offline

.\gradlew.bat :adm:bootJar --offline

powershell -NoProfile -ExecutionPolicy Bypass -File scripts\smoke-adm-runtime.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts\smoke-log-policy-runtime.ps1

powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-utf8.ps1 -CheckMojibake
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-html-docs.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-feature-evidence.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-sql-standard.ps1
```

실행하지 않은 검증은 성공으로 기록하지 않는다.

---

## 12. CPF_STABILIZATION_REPORT.html 기록 기준

리포트에는 아래 형식으로 기록한다.

```text
[ADM 거래/오류/감사 통합 관제 UX 기준 보강 / 리포트 HTML 정본화 / 운영 추적 E2E Smoke]

현재 상태 판정:
- 리포트 HTML 구조:
- 거래 로그:
- 오류 로그:
- 일반 감사:
- 정책 감사:
- transactionGlobalId 검색:
- traceId 검색:
- businessTransactionId 검색:
- 통합 추적 API/UI wrapper:

개발/보강:
- 리포트 HTML:
- check-html-docs:
- ADM 로그 API:
- ADM 오류 API:
- ADM 감사 API:
- 정책 감사 API:
- smoke script:
- 문서:
- 기능 매트릭스:

검증 결과:
- Gradle test:
- qualityGate:
- ADM runtime smoke:
- log policy smoke:
- check scripts:

미구현:
- 미구현 항목:

미검증:
- 미검증 항목:
- 미검증 사유:

실패:
- 실패 항목:
- 실패 원인:

다음 조치:
- 다음 요청 후보:

최종 판정:
- 완료 / 부분 구현 / 미구현 / 미검증 / 실패 / 재확인 필요
```

---

## 13. 완료 기준

아래가 모두 충족되어야 완료로 기록한다.

```text
- CPF_STABILIZATION_REPORT.html이 실제 HTML 구조다.
- check-html-docs가 리포트 HTML 구조를 검증한다.
- ADM 거래 로그 목록/상세 조회 기준이 확인된다.
- businessTransactionId 검색이 확인된다.
- transactionGlobalId 검색이 확인된다.
- traceId 검색이 확인된다.
- 실패 거래 또는 오류 로그 조회 기준이 확인된다.
- 일반 감사 로그 API 상태가 확인된다.
- 정책 감사 로그 API 상태가 확인된다.
- 거래 상세에서 오류/감사/정책 감사로 추적하는 기준이 문서/매트릭스에 반영된다.
- 미구현 기능은 미구현으로 기록한다.
- 실행하지 않은 검증은 성공으로 기록하지 않는다.
```

---

## 14. 완료 불인정 기준

아래 중 하나라도 해당하면 완료로 기록하지 않는다.

```text
- CPF_STABILIZATION_REPORT.html이 Markdown 형식
- check-html-docs가 Markdown 형식 리포트를 통과시킴
- 거래 로그만 있고 오류/감사 연결 상태를 판정하지 않음
- transactionGlobalId/traceId 검색 검증 없음
- 감사 로그 API가 없는데 완료로 기록
- 정책 감사와 일반 감사를 구분하지 않음
- 문서에만 통합 관제 가능이라고 쓰고 API/검색 조건 없음
- 실행하지 않은 검증을 성공으로 기록
```

---

## 15. 다음 보강 후보로만 기록할 항목

이번 작업 이후 다음 보강 후보는 실제 결과를 기준으로 정렬한다.

기본 후보:

```text
1. ADM 브라우저 실제 클릭 자동화
2. ADM Cache 관제 / hit-miss / ttl / eviction / clear / 다중 인스턴스 전파
3. Redis/Kafka/MQ broker 기반 정책 전파 검증
4. ADM 배치 운영 정책 / 강제수행 / lock / 파라미터 / BAT EDU
5. ghost 자동 감지 scheduler와 장애 시나리오 테스트
6. 온디맨드 배치
7. 센터컷 기본 구현체 + 업무별 커스텀 모수/item/result 테이블 연동
8. 전체 설치 SQL 신규 DB 재검증
9. 트랜잭션 가이드 정본화
```
