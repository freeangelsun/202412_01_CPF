CPF_NEXT_REQUEST_20260630_05 — 표준 헤더 E2E 소스 정합성 및 ADM runtime smoke 준비 요청

## 1. 작업 목적

이번 작업의 목적은 CPF 핵심 표준인 “표준 헤더 수신 → 거래 context 생성 → 로그/마스킹 → outbound 자동 전파 → ADM 확인 가능 구조”가 실제 소스 기준으로 연결되어 있는지 확인하고, 부족한 부분을 최소 보강하는 것이다.

이번 작업은 문서 포맷 정리나 DB 전체 설치 검증이 아니다.
문서는 현재 작업자가 참조 가능한 수준으로만 유지하고, 최종 문서 정본화는 마지막 단계에서 별도 진행한다.

## 2. 이번 범위

이번 범위는 아래 2개 축으로 한정한다.

### 2.1 표준 헤더 E2E 소스 정합성 확인 및 최소 보강

대상 기능 흐름:

1. inbound 표준 헤더 수신
2. 필수/권장/금지/민감 헤더 검증
3. transaction context 생성
4. current context 조회 API 또는 내부 조회 수단
5. 허용된 헤더 보정 mutator
6. 민감 헤더 마스킹
7. 거래 로그/header snapshot/detail 저장 구조
8. outbound 자동 전파
9. `CpfWebClient` / `CpfRestClient` 또는 wrapper/interceptor 기준 하위 호출
10. ADM에서 inbound/resolved/outbound/response 헤더를 확인할 수 있는 구조
11. EDU 또는 XYZ 샘플에서 표준 사용 예시

### 2.2 ADM runtime/OpenAPI smoke 준비

이번 작업에서 실제 ADM runtime 기동이나 브라우저 클릭을 완료로 만들지 않는다.
대신 다음 작업에서 바로 runtime smoke를 실행할 수 있도록 대상 API, 스크립트, 확인 기준을 정리한다.

대상:

* ADM health 또는 actuator 접근
* ADM OpenAPI JSON 접근
* 거래 로그 조회 API
* 오류 로그 조회 API
* 감사 로그 조회 API
* 배치 관제 API
* 캐시/로그 정책 API
* 표준 헤더/거래 context 확인용 API가 있으면 포함

## 3. 제외 범위

이번 작업에서 아래는 하지 않는다.

* Git commit / push / branch 생성
* 문서 포맷 변경
* HTML 디자인/CSS/레이아웃 개선
* Markdown/AsciiDoc/YAML 전환
* 개발가이드/운영가이드 대규모 상세화
* MariaDB 신규 빈 DB 전체 설치
* split SQL/Flyway/all_install 최종 검증
* ADM 브라우저 클릭 검증
* Redis/Kafka/MQ 실 broker 검증
* BAT ghost/center-cut 신규 구현
* 대규모 리팩토링

DB 관련 문서는 이번 작업에서 깊게 보강하지 않는다. DB는 자주 바뀔 수 있으므로, 이번에는 표준 헤더/ADM runtime 준비에 필요한 최소 evidence만 확인한다.

## 4. 문서 작업 제한

문서는 다음 목적에 한정해서만 수정한다.

* 실제 소스와 다른 evidence 경로 수정
* 상태값 과장 방지
* `완료 / 부분 구현 / 미구현 / 미검증 / 실패 / 재확인 필요` 상태값 유지
* 표준 헤더 E2E와 ADM runtime 준비에 필요한 누락 항목 추가
* 실행하지 않은 항목을 완료로 기록하지 않기

이번 작업에서 아래는 하지 않는다.

* 문서 문장 품질 개선
* 수십 페이지급 개발가이드 작성
* 문서 통합/분리 재설계
* HTML 포맷 품질 정리
* 문서 디자인 개선

문서 정본화와 상세화는 최종 마무리 단계에서 별도 요청으로 진행한다.

## 5. 작업 시작 전 기준 기록

작업 시작 전에 아래 명령을 실행하고 `CPF_STABILIZATION_REPORT.html`에 기록한다.

```powershell id="9fah1b"
git branch --show-current
git remote -v
git rev-parse HEAD
git rev-parse origin/master
git status --short
git log -1 --oneline
```

주의:

* commit / push / branch 생성은 하지 않는다.
* `CPF_NEW_REQUEST.md`가 사용자 갱신분으로 수정 상태라면 작업 대상에서 제외하고 리포트에 별도 기록한다.
* Codex 로컬 변경과 GitHub master 반영 가능 여부를 구분한다.

## 6. 확인 대상 파일 목록

먼저 실제 파일 존재 여부를 확인하고, 존재하지 않는 파일은 있다고 쓰지 않는다.

### 6.1 PFW 표준 헤더 후보

아래 후보를 실제 repo 기준으로 확인한다.

```text id="7csb40"
pfw/src/main/java/**/CpfHeaderNames*
pfw/src/main/java/**/CpfHeaderSpecs*
pfw/src/main/java/**/CpfInboundHeaderValidator*
pfw/src/main/java/**/TransactionHeaderValidationInterceptor*
pfw/src/main/java/**/CpfHeaderExtractor*
pfw/src/main/java/**/CpfTrustedProxyPolicy*
pfw/src/main/java/**/CpfTransactionContext*
pfw/src/main/java/**/CpfHeaderMutator*
pfw/src/main/java/**/CpfHeaderSnapshot*
pfw/src/main/java/**/CpfHeaderMasker*
pfw/src/main/java/**/CpfHeaderPropagator*
pfw/src/main/java/**/CpfWebClient*
pfw/src/main/java/**/CpfRestClient*
pfw/src/main/java/**/RestClient*
pfw/src/main/java/**/WebClient*
pfw/src/main/java/**/LoggingAspect*
```

### 6.2 ADM 표준 헤더/로그 후보

```text id="gy1qyb"
adm/src/main/java/**/AdmLogController*
adm/src/main/java/**/AdmLogQueryService*
adm/src/main/java/**/AdmObservabilityController*
adm/src/main/java/**/AdmAuditLogController*
adm/src/main/java/**/AdmLogPolicyAuditController*
adm/src/main/resources/static/adm/**
scripts/smoke-adm-runtime.ps1
scripts/smoke-openapi.ps1
scripts/smoke-log-policy-runtime.ps1
scripts/smoke-adm-ui.ps1
```

### 6.3 EDU/XYZ 샘플 후보

```text id="gav3sa"
xyz/src/main/java/cpf/xyz/edu/**
xyz/src/test/java/cpf/xyz/edu/**
xyz/src/test/resources/sql/xyz_edu_query_fixture.sql
```

### 6.4 테스트 후보

```text id="c3vtl9"
pfw/src/test/java/**/*
adm/src/test/java/**/*
xyz/src/test/java/**/*
```

## 7. 구현 / 보강 기준

### 7.1 먼저 gap 표를 만들 것

바로 구현하지 말고, 먼저 소스 기준으로 아래 표를 `CPF_STABILIZATION_REPORT.html`에 기록한다.

```text id="0kxli0"
항목
확인한 파일
현재 상태
부족한 점
이번 작업에서 보강 여부
완료로 기록하지 않는 이유
```

대상 항목:

```text id="o5r0bm"
표준 헤더 항목 정의
REQUIRED/RECOMMENDED/OPTIONAL/INTERNAL_ONLY/FORBIDDEN_TO_LOG_RAW 분류
inbound 자동 검증
trusted proxy 기반 client IP 산정
transaction context 생성
current context 조회
허용된 mutator
민감 헤더 마스킹
거래 로그/header detail 저장
outbound 자동 전파
CpfWebClient/CpfRestClient 사용 구조
ADM inbound/resolved/outbound/response 표시 구조
EDU/XYZ 샘플
단위 테스트
통합/E2E 테스트
runtime smoke
```

### 7.2 최소 보강 원칙

부족한 부분이 있으면 이번 범위 안에서 작은 보강만 한다.

허용되는 보강:

* 누락된 테스트 추가
* existing class에 작은 검증 추가
* evidence script에 표준 헤더 핵심 파일 누락 검사 추가
* EDU/XYZ 샘플에서 표준 헤더 사용 예시 보강
* ADM runtime smoke 대상 API 목록 정리
* 리포트/기능 매트릭스 상태값 정정

허용하지 않는 보강:

* 대규모 구조 변경
* DB schema 대폭 변경
* ADM UI 대규모 개편
* 문서 수십 페이지 작성
* runtime 성공으로 포장
* broker 성공으로 포장

## 8. 표준 헤더 E2E 테스트 기준

가능하면 아래 중 하나를 구현한다.

### 8.1 우선순위 1 — 단위/통합 테스트 기반 E2E 흐름

테스트명 예시:

```text id="zoytie"
CpfStandardHeaderE2eTest
CpfHeaderPropagationE2eTest
CpfTransactionContextHeaderE2eTest
```

검증 흐름:

```text id="kr1boz"
1. 표준 inbound header set 구성
2. validator 통과 확인
3. transaction context 생성 확인
4. 민감 헤더가 masking되는지 확인
5. outbound 전파 시 허용 헤더만 전달되는지 확인
6. Authorization, X-Api-Key, token류 원문 미전파/미로그 확인
7. X-User-Id, X-Customer-No, X-Member-No, X-Operator-Id 의미가 섞이지 않는지 확인
8. X-Original-Channel-Code와 X-Channel-Code 구분 확인
9. X-Client-IP는 trusted proxy 기준 산정 확인
```

### 8.2 우선순위 2 — EDU/XYZ 샘플 기반 smoke 준비

실제 runtime을 띄우지 못하면, XYZ/EDU 샘플에 표준 헤더 사용 예시와 테스트를 추가한다.

단, 이것은 E2E 완료가 아니라 `부분 구현` 또는 `재확인 필요`로 기록한다.

## 9. ADM runtime/OpenAPI smoke 준비 기준

이번 작업에서 실제 runtime을 완료로 만들지 않는다.
다만 다음 작업을 위해 아래를 준비한다.

### 9.1 smoke 대상 API 목록 정리

`CPF_STABILIZATION_REPORT.html`에 아래 형식으로 기록한다.

```text id="0fsptx"
API 이름
HTTP method/path
확인할 내용
필요한 표준 헤더
예상 응답
미검증 사유
```

대상 후보:

```text id="zjizqg"
ADM health
OpenAPI JSON
거래 로그 목록
거래 로그 상세
오류 로그 목록
감사 로그 목록
로그 정책 조회
배치 목록
배치 실행/중지/재실행 후보 API
캐시 상태 조회
```

### 9.2 smoke script 확인

아래 스크립트가 있으면 실제 내용을 확인한다.

```text id="6z13eh"
scripts/smoke-adm-runtime.ps1
scripts/smoke-openapi.ps1
scripts/smoke-log-policy-runtime.ps1
scripts/smoke-adm-ui.ps1
```

없으면 없다고 기록한다.
있으면 실제 API path와 맞는지 확인한다.
실행하지 않았으면 `미검증`으로 둔다.

## 10. 품질 게이트 보강

반복 오류 방지를 위해 필요한 경우 아래만 보강한다.

```text id="9vd46h"
scripts/check-feature-evidence.ps1
scripts/check-html-docs.ps1
```

보강 기준:

* 표준 헤더 핵심 파일이 문서에만 있고 소스가 없으면 실패 또는 경고
* CpfWebClient/CpfRestClient 관련 evidence 누락 시 경고
* ADM runtime smoke script 존재 여부 확인
* 표준 헤더 E2E 테스트 파일 존재 여부 확인

단, evidence script 성공을 runtime/E2E 성공으로 기록하지 않는다.

## 11. 검증 명령

가능한 범위에서 아래를 실행한다.

```powershell id="nzqfyh"
.\gradlew.bat :pfw:test --offline
.\gradlew.bat :adm:test --offline
.\gradlew.bat :xyz:test --offline
.\gradlew.bat qualityGate --offline
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\check-feature-evidence.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\check-html-docs.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\check-utf8.ps1 -CheckMojibake
```

전체 테스트는 가능하면 실행한다.

```powershell id="qrq010"
.\gradlew.bat test --offline
```

단, 실행하지 못하면 성공으로 기록하지 말고 `미검증`으로 기록한다.

## 12. 완료 기준

이번 작업의 완료 기준은 아래다.

* 표준 헤더 E2E 관련 소스 파일 목록을 실제 파일 기준으로 확인
* 누락/부분 구현/미검증 항목을 리포트에 분리 기록
* 가능한 범위의 단위/통합 테스트를 추가 또는 보강
* 민감 헤더 원문 로그 금지 기준을 테스트 또는 evidence로 확인
* outbound 자동 전파 구조가 소스 기준으로 확인됨
* ADM runtime smoke 대상 API 목록이 실제 controller/path 기준으로 정리됨
* 실행한 Gradle/check 명령 결과가 리포트에 기록됨
* 실행하지 않은 runtime/OpenAPI/browser/broker 검증은 `미검증`으로 남김
* 문서는 포맷/디자인이 아니라 evidence/상태값/중요 누락만 보정

## 13. 완료 불인정 기준

아래 중 하나라도 해당하면 완료로 인정하지 않는다.

* 문서만 보강하고 소스/evidence 확인 없음
* 표준 헤더 E2E를 실제 흐름 없이 완료로 기록
* 단위 테스트만으로 ADM runtime 성공이라고 기록
* ADM 정적 UI marker를 브라우저 클릭 성공으로 기록
* OpenAPI 설정 파일 존재만으로 runtime 접근 성공 처리
* 민감 헤더 원문 저장 금지 확인 없이 완료 처리
* X-User-Id를 고객번호로 설명
* X-Customer-No와 X-Member-No 의미 혼동
* X-Original-Channel-Code와 X-Channel-Code 의미 혼동
* X-Client-IP를 client 입력값 그대로 신뢰하는 구조
* Authorization, X-Api-Key, token류 원문 로그 저장
* MariaDB Mapper fixture 성공을 MariaDB 전체 설치 성공으로 확대
* 문서 포맷/디자인 정리로 시간을 소모
* Git commit / push / branch 생성 수행

## 14. 완료 보고 양식

완료 보고는 아래 양식으로 작성한다.

```text id="aud9ha"
1. 기준 정보
- 작업 시작 branch:
- 작업 시작 HEAD SHA:
- 작업 시작 origin/master SHA:
- 작업 종료 HEAD SHA:
- 작업 종료 status --short:
- GitHub master 확인 가능 여부:

2. 확인한 표준 헤더 파일
- 파일:
- 확인 내용:
- 상태:

3. 표준 헤더 E2E gap
- 항목:
- 현재 상태:
- 보강 여부:
- 남은 미검증:

4. ADM runtime/OpenAPI smoke 준비
- Controller/API:
- path:
- smoke script:
- 상태:
- 미검증 사유:

5. 변경 파일
- 파일:
- 변경 목적:
- 구현/테스트/문서/evidence 구분:

6. 실행 명령 결과
- 명령:
- 결과:
- tests:
- skipped:
- failures:
- errors:
- 미실행 사유:

7. 남은 미검증
- 대상:
- 사유:
- 완료로 기록하지 않은 이유:

8. 다음 추천 작업
- ADM runtime smoke
- ADM browser click
- 표준 헤더 E2E runtime 검증
- BAT ghost/center-cut
```

## 15. 다음 보강 후보

이번 작업이 끝나면 다음 순서는 아래를 우선 검토한다.

1. ADM runtime/OpenAPI smoke 실제 실행
2. ADM 브라우저 클릭 검증
3. 표준 헤더 E2E runtime 검증
4. BAT standalone / heartbeat / ghost
5. center-cut 기본 구현체와 EDU 샘플
6. DB/Flyway/all_install 신규 설치 검증
7. 문서 정본화 및 상세화
