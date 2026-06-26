# CPF_REQUEST_010: 로그 정책 세부 효과 검증 / ADM 거래·오류·감사 통합 관제 기준 보강 / 리포트 HTML 형식 재확인

## 0. 이번 작업 범위 고정

이번 작업은 직전 작업에서 성공한 거래 메타 E2E와 `dbLogEnabled` runtime smoke를 기반으로, 로그 정책의 세부 저장 제어를 검증하고 ADM 거래/오류/감사 관제 흐름을 운영자 관점으로 정리하는 작업이다.

이번 작업은 아래 범위로 고정한다.

```text id="r7ixf8"
선택 범위:
- requestBodySaveYn=N 정책 실제 효과 검증
- responseBodySaveYn=N 정책 실제 효과 검증
- errorStackSaveYn=N 정책 실제 효과 검증
- active override / expired override / DB policy / yml default / CPF default fallback 검증
- 로그 정책 runtime smoke 세부 케이스 확대
- ADM 거래 로그, 오류 로그, 감사 로그 연결 상태 판정
- ADM 거래 상세에서 거래 로그/오류/감사로 이어지는 운영 흐름 보강
- CPF_STABILIZATION_REPORT.html 실제 HTML 구조 재확인 및 필요 시 정본화
- README / 관리자 가이드 / 운영 매뉴얼 / SQL 가이드 / 기능 구현 매트릭스 / 리포트 반영
```

이번 작업에서 아래 항목은 구현하지 않는다.

```text id="6fef3s"
이번 실행 제외:
- ADM 브라우저 실제 클릭 자동화
- ADM Cache hit/miss/ttl/eviction/clear 전체 구현
- Redis/Kafka/MQ broker 실연동
- 배치 강제수행/lock/pause/resume 운영 정책 전체 구현
- 온디맨드 배치 구현
- 센터컷 기본 구현체 구현
- 전체 트랜잭션 가이드 정본화
```

요청 파일은 작업 대상으로 수정하지 않는다.
Git commit, push, branch 생성 지시는 하지 않는다.
별도 수정파일 목록 산출물은 만들지 않는다.
작업 결과는 `CPF_STABILIZATION_REPORT.html`에만 기록한다.

---

## 1. 이번 작업의 핵심 의도

직전 작업에서 아래 항목은 닫혔다.

```text id="kpn15t"
직전 완료:
- V11/V12 PFW runtime migration 로컬 DB 적용
- V13 ADM 권한 seed 로컬 DB 적용
- ADM 거래 메타 scan 403 해소
- 거래 메타 scan → upsert → 목록 → 상세 E2E 성공
- dbLogEnabled=N override 적용 시 DB 거래 로그 미저장 검증
- override 해제 후 DB 거래 로그 저장 재개 검증
- 전체 gradlew test --offline 성공 재확인
```

하지만 로그 정책은 `dbLogEnabled`만으로 끝나지 않는다.
운영형 프레임워크 기준으로는 request body, response body, error stack 저장 여부까지 실제 DB 결과로 검증해야 한다.

이번 작업은 아래를 증명한다.

```text id="0qr9cy"
- requestBodySaveYn=N이면 request body가 DB에 저장되지 않는다.
- responseBodySaveYn=N이면 response body가 DB에 저장되지 않는다.
- errorStackSaveYn=N이면 error stack이 DB에 저장되지 않는다.
- active override 기간에는 override가 적용된다.
- expired override는 적용되지 않는다.
- DB policy가 없으면 yml 기본값으로 fallback된다.
- yml 기본값도 없으면 CPF 기본값으로 fallback된다.
- ADM에서 거래 로그, 오류, 감사 흐름을 운영자가 추적할 수 있는 기준이 문서/매트릭스에 정리된다.
```

---

## 2. 먼저 현재 상태 판정

작업 시작 전 실제 소스/SQL/문서/DB 기준으로 아래를 판정한다.

```text id="vzcprz"
현재 상태 판정 항목:
- smoke-log-policy-runtime.ps1 현재 검증 범위
- dbLogEnabled=N 검증 결과
- requestBodySaveYn=N 검증 존재 여부
- responseBodySaveYn=N 검증 존재 여부
- errorStackSaveYn=N 검증 존재 여부
- active override 검증 존재 여부
- expired override 검증 존재 여부
- DB policy fallback 검증 존재 여부
- yml default fallback 검증 존재 여부
- CPF default fallback 검증 존재 여부
- ADM 거래 로그 조회 API 상태
- ADM 오류 로그 조회 API 상태
- ADM 감사 로그 조회 API 상태
- ADM 거래 상세에서 로그/오류/감사 연결 상태
- CPF_STABILIZATION_REPORT.html 실제 HTML 구조 여부
```

상태값은 아래만 사용한다.

```text id="bcscf2"
완료
부분 구현
미구현
미검증
실패
재확인 필요
```

Codex 완료 메시지나 과거 리포트만 믿지 말고 실제 파일/DB 기준으로 확인한다.

---

## 3. 로그 정책 세부 효과 검증

`smoke-log-policy-runtime.ps1` 또는 별도 smoke/test를 보강해 아래 케이스를 검증한다.

### 3.1 requestBodySaveYn=N

필수 검증:

```text id="y4sce0"
- ONLINE_TRANSACTION 정책 또는 override에서 requestBodySaveYn=N 설정
- request body가 있는 API 호출
- pfw_transaction_log_detail 또는 동등 상세 테이블에 request body가 저장되지 않는지 확인
- response body와 기본 거래 로그 저장 여부는 정책에 따라 별도 확인
```

### 3.2 responseBodySaveYn=N

필수 검증:

```text id="m18lyt"
- responseBodySaveYn=N 설정
- 응답 body가 있는 API 호출
- pfw_transaction_log_detail 또는 동등 상세 테이블에 response body가 저장되지 않는지 확인
```

### 3.3 errorStackSaveYn=N

필수 검증:

```text id="s9doky"
- errorStackSaveYn=N 설정
- 오류 발생 테스트 API 또는 기존 오류 케이스 호출
- error stack 또는 internal error detail이 저장되지 않는지 확인
- 표준 오류 응답은 유지되는지 확인
```

### 3.4 dbLogEnabled=N 재확인

기존 smoke가 이미 검증했더라도 이번 세부 smoke와 같이 다시 확인한다.

```text id="fvo91a"
- dbLogEnabled=N이면 pfw_transaction_log row 증가 없음
- override 해제 후 row 증가 재개
```

완료 불인정:

```text id="e828p7"
- 정책 source만 확인하고 DB 결과를 확인하지 않음
- request/response/error 저장 제외를 단위 테스트만으로 처리하고 runtime smoke 또는 DB 확인이 없음
- dbLogEnabled만 검증하고 세부 저장 정책은 완료로 기록
```

---

## 4. Override / fallback 검증

로그 정책 우선순위는 아래 기준을 따른다.

```text id="e7q65q"
1. ADM 활성 override
2. DB 운영 정책
3. application.yml 기본값
4. CPF 기본값
```

필수 검증:

```text id="xovv9w"
active override:
- 현재 시각이 effectiveStartAt ~ effectiveEndAt 사이면 override 적용

expired override:
- effectiveEndAt이 지난 override는 무시
- DB policy 또는 기본값으로 fallback

future override:
- effectiveStartAt이 미래이면 아직 적용하지 않음

DB policy fallback:
- active override가 없으면 pfw_log_policy 적용

yml default fallback:
- target별 DB policy가 없으면 yml 기본값 적용

CPF default fallback:
- yml 기본값도 없으면 CPF 기본값 적용
```

검증 방식:

```text id="hkk09z"
허용:
- 단위 테스트
- 통합 테스트
- runtime smoke
- DB row count 확인

불허:
- resolver 소스만 보고 완료 처리
```

---

## 5. ADM 거래/오류/감사 통합 관제 기준 보강

이번 작업에서 대형 UX를 완성하지는 않는다.
다만 운영자가 거래 하나를 기준으로 로그, 오류, 감사 흐름을 추적할 수 있는 최소 기준을 정리하고, 가능한 API wrapper를 보강한다.

필수 판정/보강 항목:

```text id="jnn3fh"
- ADM 거래 로그 목록 API
- ADM 거래 로그 상세 API
- ADM 오류 로그 조회 API
- ADM 감사 로그 조회 API
- transactionGlobalId 기준 조회 가능 여부
- businessTransactionId 기준 조회 가능 여부
- traceId 기준 조회 가능 여부
- 거래 메타 상세에서 최근 거래 로그로 이동 가능한 기준
- 거래 로그 상세에서 오류/감사 로그로 이동 가능한 기준
- 오류 조치 상태와 조치 이력 존재 여부
- 감사 로그에서 operatorId, reason, before/after 값 확인 여부
```

이번 작업에서 실제 UI 클릭은 제외한다.
다만 정적 UI marker 또는 API wrapper가 있으면 매트릭스에 반영한다.

완료 불인정:

```text id="cu0up9"
- 거래 로그만 있고 오류/감사 연결 기준 없음
- transactionGlobalId/traceId 기준 추적 불가
- 문서에만 “연결 가능”이라고 쓰고 API/DTO/검색 조건 없음
```

---

## 6. CPF_STABILIZATION_REPORT.html 실제 HTML 구조 재확인

`CPF_STABILIZATION_REPORT.html`은 실제 HTML 문서여야 한다.

필수 기준:

```text id="b9oycg"
- <!DOCTYPE html> 또는 <!doctype html>
- <html lang="ko">
- <head>
- <meta charset="UTF-8"> 또는 <meta charset="utf-8">
- <title>
- <body>
- h1/h2/h3 또는 section 구조
- table 또는 상태 목록 구조
```

완료 불인정:

```text id="65pi7k"
- .html 확장자지만 Markdown 문서
- 첫 줄이 # CPF 안정화 리포트 형태
- 브라우저로 열 때 HTML 구조가 없음
```

`check-html-docs.ps1`가 이 기준을 제대로 검증하지 못한다면 해당 스크립트를 보강한다.

---

## 7. 테스트 기준

필수 테스트 또는 smoke를 보강한다.

```text id="as6d7j"
로그 정책 테스트:
- dbLogEnabled=N
- requestBodySaveYn=N
- responseBodySaveYn=N
- errorStackSaveYn=N
- active override
- expired override
- future override
- DB policy fallback
- yml default fallback
- CPF default fallback

ADM 관제 테스트:
- 거래 로그 목록
- 거래 로그 상세
- transactionGlobalId 검색
- traceId 검색
- 감사 로그 조회
- 오류 로그 조회 또는 미구현 판정
```

가능하면 `scripts/smoke-log-policy-runtime.ps1`에 아래 옵션 또는 케이스를 추가한다.

```text id="ofh88n"
- -CheckRequestBodyPolicy
- -CheckResponseBodyPolicy
- -CheckErrorStackPolicy
- -CheckOverrideFallback
```

옵션 이름은 프로젝트 컨벤션에 맞춰 조정 가능하다.

---

## 8. 검증 명령

가능한 범위에서 아래 명령을 실행한다.

```powershell id="ifrxv6"
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

## 9. 문서 반영 기준

아래 문서를 실제 결과에 맞게 최소 갱신한다.

```text id="b10tfp"
README.md
specs/관리자_가이드.html
specs/운영_매뉴얼.html
specs/SQL_가이드.html
specs/기능_구현_매트릭스.html
CPF_STABILIZATION_REPORT.html
```

문서에는 아래를 분리해서 기록한다.

```text id="d5qq4v"
- 완료
- 부분 구현
- 미구현
- 미검증
- 실패
- 다음 보강
```

---

## 10. CPF_STABILIZATION_REPORT.html 기록 기준

리포트에는 아래 형식으로 기록한다.

```text id="yml4no"
[로그 정책 세부 효과 검증 / ADM 거래·오류·감사 통합 관제 기준 보강 / 리포트 HTML 형식 재확인]

현재 상태 판정:
- dbLogEnabled:
- requestBodySaveYn:
- responseBodySaveYn:
- errorStackSaveYn:
- active override:
- expired override:
- fallback:
- ADM 거래 로그:
- ADM 오류 로그:
- ADM 감사 로그:
- 리포트 HTML 구조:

검증 결과:
- 로그 정책 smoke:
- ADM runtime smoke:
- Gradle test:
- qualityGate:
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

## 11. 완료 기준

아래가 모두 충족되어야 이번 작업을 완료로 기록한다.

```text id="r7ybvh"
- dbLogEnabled=N 검증이 유지된다.
- requestBodySaveYn=N 실제 저장 제외가 검증된다.
- responseBodySaveYn=N 실제 저장 제외가 검증된다.
- errorStackSaveYn=N 실제 저장 제외가 검증된다.
- active override 적용이 검증된다.
- expired override 미적용이 검증된다.
- DB policy fallback이 검증된다.
- yml default 또는 CPF default fallback이 검증된다.
- ADM 거래 로그 목록/상세 조회 기준이 확인된다.
- ADM 오류 로그/감사 로그 연결 상태가 실제 기준으로 판정된다.
- CPF_STABILIZATION_REPORT.html이 실제 HTML 구조다.
- README/specs/기능 매트릭스/리포트가 실제 결과와 일치한다.
- 실행하지 않은 검증을 성공으로 기록하지 않는다.
```

---

## 12. 완료 불인정 기준

아래 중 하나라도 해당하면 완료로 기록하지 않는다.

```text id="pwpnrr"
- dbLogEnabled만 검증하고 request/response/errorStack 정책을 완료로 기록
- override active만 검증하고 expired/fallback을 완료로 기록
- 소스만 확인하고 DB 결과를 확인하지 않음
- 거래 로그만 있고 오류/감사 연결 상태를 판정하지 않음
- CPF_STABILIZATION_REPORT.html이 Markdown 형식
- check-html-docs가 리포트 HTML 구조를 잡지 못함
- 실행하지 않은 검증을 성공으로 기록
```

---

## 13. 다음 보강 후보로만 기록할 항목

이번 작업 이후 다음 보강 후보는 실제 결과를 기준으로 정렬한다.

기본 후보:

```text id="4mxxfd"
1. ADM 브라우저 실제 클릭 자동화
2. ADM Cache 관제 / hit-miss / ttl / eviction / clear / 다중 인스턴스 전파
3. 로그 정책 Redis/Kafka/MQ broker 전파
4. ADM 배치 운영 정책 / 강제수행 / lock / 파라미터 / BAT EDU
5. ghost 자동 감지 scheduler와 장애 시나리오 테스트
6. 온디맨드 배치
7. 센터컷 기본 구현체 + 업무별 커스텀 모수/item/result 테이블 연동
8. 전체 설치 SQL 신규 DB 재검증
9. 트랜잭션 가이드 정본화
```
