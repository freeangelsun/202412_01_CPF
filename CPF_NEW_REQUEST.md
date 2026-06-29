# CPF_NEXT_PLAN_REQUIREMENTS_REVISED: 표준 헤더/DB 표준/EDU/운영형 프레임워크 완성 플랜

## 0. 공통 작업 원칙

앞으로 모든 CPF 작업은 아래 원칙을 따른다.

```text id="jdpptx"
공통 원칙:
- 요청 파일은 확인용으로만 읽고 작업 대상으로 수정하지 않는다.
- Git commit, push, branch 생성 지시는 하지 않는다.
- 별도 수정파일 목록 산출물은 만들지 않는다.
- 작업 결과는 CPF_STABILIZATION_REPORT.html에 기록한다.
- 실행하지 않은 검증은 성공으로 기록하지 않는다.
- 실패/미구현/미검증은 숨기지 않고 분리 기록한다.
- 문서만 작성하고 구현이 없으면 완료로 기록하지 않는다.
- Controller만 있고 Service/Repository/DTO/SQL/테스트가 없으면 완료로 기록하지 않는다.
- 기존 소스/SQL/문서/매트릭스와 불일치하면 완료로 기록하지 않는다.
- CPF_NEW_REQUEST.md 등 요청 파일은 작업 대상으로 수정하지 않는다.
```

모든 요청은 필요한 경우 아래 문서를 갱신한다.

```text id="lpdb9k"
기본 갱신 문서:
- README.md
- specs/index.html
- specs/프레임워크_구성_가이드.html
- specs/아키텍처_가이드.html, 신규 또는 기존 가이드 연결
- specs/표준_헤더_가이드.html
- specs/DB_표준_가이드.html
- specs/개발_가이드.html
- specs/트랜잭션_가이드.html
- specs/배치_개발_가이드.html
- specs/관리자_가이드.html
- specs/운영_매뉴얼.html
- specs/SQL_가이드.html
- specs/기능_구현_매트릭스.html
- CPF_STABILIZATION_REPORT.html
```

모든 검증은 가능한 범위에서 아래를 수행한다.

```powershell id="k6052q"
.\gradlew.bat :pfw:test --offline
.\gradlew.bat :adm:test --offline
.\gradlew.bat :bat:test --offline
.\gradlew.bat test --offline
.\gradlew.bat qualityGate --offline

.\gradlew.bat :adm:bootJar --offline
.\gradlew.bat :bat:bootJar --offline

powershell -NoProfile -ExecutionPolicy Bypass -File scripts\smoke-adm-runtime.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts\smoke-log-policy-runtime.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-utf8.ps1 -CheckMojibake
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-html-docs.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-feature-evidence.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-sql-standard.ps1
```

---

# CPF_REQUEST_012: 표준 헤더 가이드 / DB 표준 가이드 / 소스 주석 기준 / EDU 조회 샘플 설계

## 목적

CPF 표준 헤더, DB 테이블/컬럼 표준, 소스 주석 기준, EDU 조회 샘플 방향을 정본화한다.

이번 요청은 **표준 수립과 점검이 중심**이다.
DB 테이블은 바로 대량 생성하지 않는다.

## 범위

```text id="wntz5h"
- specs/표준_헤더_가이드.html 신규 작성
- specs/DB_표준_가이드.html 신규 작성
- specs/아키텍처_가이드.html 신규 작성 또는 프레임워크_구성_가이드에서 상위 AA 기준 정리
- 표준 헤더 자동 검증/조회/수정/전파 구조 설계
- outbound 자동 헤더 전파 설계
- ADM inbound/resolved/outbound 헤더 표시 기준 설계
- 소스 주석 작성 기준 수립
- EDU 온라인 조회 샘플 설계
- 기존 TransactionContext / TransactionHeader / LoggingAspect / ADM 표시 구조 현황 판정
- 기존 DB 테이블/컬럼 명명 규칙 현황 판정
- 기능 매트릭스에 표준 문서 상태 반영
```

## 표준 헤더 가이드 필수 내용

표준 헤더는 단순 목록이 아니라 아래를 모두 포함한다.

```text id="cfeavv"
- 헤더 설계 원칙
- REQUIRED / RECOMMENDED / OPTIONAL / INTERNAL_ONLY / FORBIDDEN_TO_LOG_RAW 분류
- 거래/호출 식별 헤더
- trace/span/W3C traceparent 헤더
- 최초 채널 / 현재 채널 / 세부 채널 헤더
- 사용자 / 운영자 / 고객 / 회원 / 테넌트 / 조직 헤더
- IP / 국가 / 위치 헤더
- 호출자 / 클라이언트 앱 헤더
- 보안 / 서명 헤더
- 수신 자동 검증 규칙
- 현재 거래 context 조회 API 기준
- 허용된 헤더 값 보정/추가 API 기준
- outbound 자동 전파 규칙
- WebClient/RestClient wrapper 기준
- 응답 헤더 규칙
- 로그 저장 규칙
- 마스킹 규칙
- ADM inbound/resolved/outbound 표시 위치
- DB 저장 컬럼/길이 기준
- 예시 요청/응답
```

## 헤더 상세 항목

아래 헤더는 반드시 개별 항목으로 정의한다.

```text id="qzv9jd"
거래/호출 식별:
- X-Transaction-Id
- X-Parent-Transaction-Id
- X-Original-Transaction-Id
- X-Request-Id
- X-External-Request-Id
- X-Correlation-Id
- X-Idempotency-Key
- Idempotency-Key

추적/Span:
- X-Trace-Id
- X-Span-Id
- X-Parent-Span-Id
- traceparent
- tracestate

채널:
- X-Original-Channel-Code
- X-Channel-Code
- X-Channel-Detail-Code

사용자/고객/회원/조직:
- X-User-Id
- X-Operator-Id
- X-Customer-No
- X-Member-No
- X-Tenant-Id
- X-Organization-Code
- X-Branch-Code

IP/국가/위치:
- X-Client-IP
- X-Forwarded-For
- Forwarded
- X-Real-IP
- X-Client-Country-Code
- X-Client-Region-Code
- X-Client-Timezone

호출자/클라이언트:
- X-Client-App-Id
- X-Client-Version
- X-Caller-Service
- X-Caller-Instance-Id
- User-Agent

보안/서명:
- Authorization
- X-Api-Key
- X-Request-Signature
- X-Request-Timestamp
- X-Nonce
```

각 헤더는 아래 컬럼으로 정리한다.

```text id="s1kths"
- 헤더명
- 분류
- 의미
- 생성 주체
- 검증 위치
- 전파 여부
- 응답 헤더 포함 여부
- DB 저장 컬럼
- 권장 DB 길이
- 로그 저장 여부
- 마스킹 여부
- ADM 표시 위치
- 비고/주의사항
```

## 표준 헤더 핵심 기준

```text id="fpbq12"
X-Transaction-Id:
- CPF transactionGlobalId다.
- 현재 34자리 구조를 유지한다.
- DB는 VARCHAR(64) 이상 여유를 둔다.

X-Parent-Transaction-Id:
- 상위 거래 ID다.
- 내부 서비스 호출, EXS 호출, 배치 연계 시 parent/child 추적에 사용한다.

X-Original-Transaction-Id:
- 최초 유입 거래 ID다.
- 여러 시스템을 거쳐도 최초 요청 식별용으로 유지한다.

X-Request-Id:
- 호출자 기준 요청 번호다.

X-External-Request-Id:
- 외부기관/제휴사/대외계가 준 요청 번호다.

X-Correlation-Id:
- 여러 거래를 하나의 업무 흐름으로 묶는 ID다.

X-Idempotency-Key / Idempotency-Key:
- 중복 요청 방지/재처리 식별자다.
- 둘 다 수용할지, 충돌 시 우선순위를 문서화한다.

X-User-Id:
- 고객번호가 아니다.
- 로그인 사용자, 운영자, 직원, 서비스 계정, 일반 사용자 계정 식별자다.

X-Operator-Id:
- ADM 운영 조치자다.

X-Customer-No:
- 고객번호다.
- 고객 또는 계약 주체 식별자다.

X-Member-No:
- 회원번호다.
- MBR 도메인의 회원 식별자다.

X-Original-Channel-Code:
- 최초 유입 채널이다.

X-Channel-Code:
- 현재 처리 채널이다.

X-Channel-Detail-Code:
- 세부 채널이다.

X-Client-IP:
- client가 직접 보낸 값을 그대로 신뢰하지 않는다.
- trusted proxy/gateway/LB/WAF 기준으로 산정한다.

X-Client-Country-Code:
- ISO 3166-1 alpha-2 기준 국가코드다.
- gateway/WAF/LB 또는 서버 GeoIP 산정값을 우선한다.
- client가 직접 보낸 값은 참고값으로만 사용한다.

Authorization / X-Api-Key / token류:
- 원문 로그 저장 금지다.
- ADM 표시 시 마스킹 또는 저장 제외한다.
```

## 표준 헤더 자동 처리 설계

문서에는 아래 컴포넌트 설계가 포함되어야 한다.

```text id="q2vb07"
CpfHeaderNames:
- 표준 헤더명 상수
- header name 하드코딩 방지

CpfHeaderSpec:
- 헤더 분류/의미/필수여부/마스킹여부 메타

CpfInboundHeaderValidator:
- 수신 요청 필수 헤더 검증
- 값 형식 검증

CpfHeaderExtractor:
- HttpServletRequest에서 표준 헤더 추출
- trusted proxy 기준 clientIp 산정
- client country code 산정/수집

CpfTransactionHeader:
- 현재 거래 헤더 값 객체

CpfTransactionContext:
- 현재 거래 정보 조회

CpfHeaderMutator:
- 허용된 헤더 값 보정/추가
- customerNo/memberNo/tenantId/organizationCode 등 보정 가능
- transactionId/originalTransactionId/traceId/originalChannelCode는 임의 변경 제한

CpfHeaderPropagator:
- outbound 호출용 헤더 생성
- parent/original/trace/span 처리

CpfWebClientCustomizer:
- WebClient 요청마다 표준 헤더 자동 추가

CpfRestClientInterceptor:
- RestClient/RestTemplate 사용 시 표준 헤더 자동 추가

CpfHeaderMasker:
- Authorization, API key, token류 원문 로그 금지

CpfHeaderAuditLogger:
- 필요 시 중요 헤더 변경/보정 이력 기록
```

## 개발자 사용성 기준

개발자는 outbound 호출 시 매번 header를 직접 넣지 않아야 한다.

권장 사용 예:

```text id="oz4cxs"
1. 현재 거래 context에 필요한 값만 보정한다.
   예: customerNo, memberNo, tenantId

2. CpfWebClient 또는 CpfRestClient를 사용한다.

3. body만 넘기면 프레임워크가 표준 헤더를 자동 추가한다.
```

완료 불인정:

```text id="ijwwxi"
- 개발자가 모든 outbound header를 직접 넣어야 함
- header name 문자열을 각 서비스에서 하드코딩
- parent/original transactionId 전파 기준 없음
- trace/span이 outbound 호출마다 끊김
```

## ADM 헤더 표시 기준

ADM에서는 헤더를 단순 하나의 JSON으로만 보지 않고 아래로 구분할 수 있어야 한다.

```text id="rr33lb"
Inbound Headers:
- 외부/상위 서비스에서 들어온 헤더

Resolved Headers:
- CPF가 산정/보정한 헤더
- client IP
- country code
- generated trace/span
- authenticated user/operator

Outbound Headers:
- CPF가 하위 서비스 호출 시 붙인 헤더
- parent transaction
- new span
- caller service/instance
```

이번 요청에서 전체 구현이 아니면 최소 설계/현황 판정/다음 요청 후보로 기록한다.

## DB 표준 가이드 필수 내용

```text id="f8j7yn"
- 주제영역별 테이블 prefix
- 테이블 물리명/논리명 규칙
- 컬럼 물리명/논리명 규칙
- ID / NO / CODE / TYPE / STATUS / YN / AT / BY 구분
- 공통 감사 컬럼
- COMMENT 필수 기준
- PK/FK/INDEX 명명 규칙
- 이력 테이블 규칙
- 로그 테이블 규칙
- 감사 테이블 규칙
- 마스킹 대상 컬럼 규칙
- 코드/상태 컬럼 규칙
- VARCHAR 길이 표준
- JSON/LONGTEXT 사용 기준
- Flyway migration 작성 규칙
- all_install 반영 규칙
- SQL 검증 체크리스트
```

## DB 표준 주의

```text id="o4g8kk"
이번 요청에서는 DB 테이블을 바로 만들지 않는다.

해야 할 것:
- 표준 규칙 작성
- 기존 테이블/컬럼과 표준 비교
- 불일치 목록을 CPF_STABILIZATION_REPORT.html에 기록
- 보정 후보를 다음 요청으로 분리

하지 말 것:
- 대량 DDL 생성
- 기존 테이블명/컬럼명 일괄 변경
- 검증 없는 SQL 변경
```

## 소스 주석 기준

```text id="mvq4re"
필수 주석 대상:
- public class 역할
- public method 역할
- framework 핵심 흐름
- transaction/header/log/policy/batch/cache/security 흐름
- fallback/override/cache/권한/감사/마스킹 기준
- EDU 샘플 코드

불필요한 주석:
- 코드만 봐도 알 수 있는 단순 반복 설명
```

## EDU 조회 샘플 설계 기준

```text id="m80tsa"
필수 샘플:
- 단건 조회
- 목록 조회
- offset 기반 페이지 조회
- keyset 기반 페이지 조회
- 검색 조건 포함 목록 조회
- 정렬 조건 포함 목록 조회
- 다른 주제영역 Facade 연동 조회
- 권한/마스킹/감사 적용 위치
- CpfWebClient/CpfRestClient outbound header 자동 전파 샘플
```

## 완료 기준

```text id="kdzey4"
- 표준_헤더_가이드.html이 작성되어 있다.
- DB_표준_가이드.html이 작성되어 있다.
- 표준 헤더 상세 항목이 누락 없이 정의되어 있다.
- 수신 자동 검증/현재 context 조회/헤더 보정/outbound 자동 전파 설계가 있다.
- X-User-Id / X-Customer-No / X-Member-No / X-Operator-Id가 분리 정의되어 있다.
- 최초 채널과 현재 채널이 분리 정의되어 있다.
- client IP / country code 신뢰 기준이 정의되어 있다.
- DB 명명/논리명/COMMENT/공통 컬럼 기준이 정의되어 있다.
- 소스 주석 기준이 정의되어 있다.
- EDU 조회 샘플 설계가 문서화되어 있다.
- 기존 구현과 표준의 차이가 리포트에 기록되어 있다.
```

---

# CPF_REQUEST_013: 표준 헤더 자동 검증 / Context API / Outbound 자동 전파 구현

## 목적

REQUEST_012에서 정본화한 표준 헤더를 실제 PFW 구현으로 연결한다.
핵심은 **개발자가 header.add(...)를 직접 반복하지 않아도 되게 하는 것**이다.

## 범위

```text id="be1o6x"
- CpfHeaderNames 구현 또는 기존 상수 정리
- CpfHeaderSpec 구현 또는 문서/enum 기반 메타 정리
- CpfInboundHeaderValidator 구현 또는 기존 Interceptor 보강
- CpfHeaderExtractor 구현
- CpfTransactionHeader 정리
- CpfTransactionContext 조회 API 보강
- CpfHeaderMutator 구현
- CpfHeaderPropagator 구현
- CpfWebClientCustomizer 또는 CpfWebClient wrapper 구현
- CpfRestClientInterceptor 또는 RestClient/RestTemplate interceptor 구현
- SensitiveDataMasker header masking 보강
- LoggingAspect inbound/resolved header 저장 정리
- outbound header 자동 전파 테스트
```

## 수신 자동 검증 기준

```text id="nph06y"
- X-Transaction-Id 필수 검증
- X-Request-Type 필수 검증
- X-Original-Channel-Code 필수 검증
- X-Channel-Code 필수 검증
- X-Transaction-Id 34자리 형식 검증
- @CpfTransaction id/name 검증
- 권장 헤더는 누락 시 생성 또는 N/A 처리
- 누락/오류 시 표준 오류 반환
```

## Context 조회 API 기준

개발자는 현재 거래 정보를 쉽게 조회할 수 있어야 한다.

```text id="q3najd"
필수 메서드 예:
- transactionId()
- parentTransactionId()
- originalTransactionId()
- requestId()
- externalRequestId()
- correlationId()
- traceId()
- spanId()
- parentSpanId()
- originalChannelCode()
- channelCode()
- channelDetailCode()
- userId()
- operatorId()
- customerNo()
- memberNo()
- tenantId()
- organizationCode()
- branchCode()
- clientIp()
- clientCountryCode()
- clientTimezone()
```

## Header Mutator 기준

허용된 값만 보정 가능하게 한다.

```text id="3hgk4h"
수정 가능:
- customerNo
- memberNo
- tenantId
- organizationCode
- branchCode
- screenId
- channelDetailCode
- business attributes

수정 제한:
- transactionId
- originalTransactionId
- traceId
- originalChannelCode
```

변경 시 기준:

```text id="p55ojn"
- overwrite 허용 여부 명확화
- null/blank 처리 기준
- 중요 값 변경 시 debug/audit 후보 로그
```

## Outbound 자동 전파 기준

CpfWebClient/CpfRestClient를 사용하면 아래 헤더가 자동으로 붙어야 한다.

```text id="zuix6g"
자동 전파:
- X-Transaction-Id
- X-Parent-Transaction-Id
- X-Original-Transaction-Id
- X-Trace-Id
- X-Parent-Span-Id
- X-Span-Id
- X-Correlation-Id
- X-Original-Channel-Code
- X-Channel-Code
- X-Channel-Detail-Code
- X-User-Id
- X-Operator-Id
- X-Customer-No
- X-Member-No
- X-Tenant-Id
- X-Client-IP
- X-Client-Country-Code
- X-Caller-Service
- X-Caller-Instance-Id
- X-Idempotency-Key, 필요 시
```

완료 불인정:

```text id="vez3er"
- 개발자가 모든 outbound header를 직접 넣어야 함
- header name 문자열이 서비스 코드에 하드코딩됨
- trace/span이 outbound 호출마다 끊김
- parent/original transactionId 전파 기준 없음
- 민감 헤더 원문 저장
```

## ADM 저장 기준

```text id="m505qa"
- inbound headers 저장
- resolved headers 저장
- outbound headers 저장은 가능하면 구현
- 이번에 어렵다면 outbound propagation test와 다음 보강 후보로 기록
```

## 테스트 기준

```text id="wc2zse"
- 수신 필수 헤더 누락 시 표준 오류
- 필수 헤더 정상 시 통과
- X-Transaction-Id 형식 오류 시 표준 오류
- context 조회 API 값 확인
- mutator로 customerNo/memberNo 보정
- 제한 필드 변경 차단
- WebClient outbound 자동 헤더 부여
- RestClient outbound 자동 헤더 부여
- Authorization/API key 원문 미저장
```

가능하면 smoke 추가:

```powershell id="a6v6bb"
powershell -NoProfile -ExecutionPolicy Bypass -File scripts\smoke-transaction-header-runtime.ps1
```

## 완료 기준

```text id="v1rhll"
- 표준 헤더명이 상수화되어 있다.
- 수신 거래 필수 헤더가 자동 검증된다.
- 현재 거래 헤더를 조회하는 공통 API가 있다.
- 허용된 헤더 값을 보정/추가하는 공통 API가 있다.
- outbound 호출 시 표준 헤더가 자동 전파된다.
- 개발자가 매번 header.add(...) 하지 않아도 된다.
- WebClient 또는 RestClient wrapper/interceptor가 있다.
- parentTransactionId/originalTransactionId/trace/span 전파 기준이 구현되어 있다.
- Authorization/API key/token 원문은 자동 마스킹 또는 저장 제외된다.
- ADM 로그 상세에서 inbound/resolved headers가 구분된다.
- 테스트/EDU 샘플에서 body만 넘겨도 header 자동 전파가 되는 예시가 있다.
```

---

# CPF_REQUEST_014: ADM 헤더·데이터 분리 표시 / Inbound·Resolved·Outbound 관제 Smoke

## 목적

표준 헤더가 실제 ADM에서 운영자가 보기 좋게 분리되어 보이는지 검증한다.

## 범위

```text id="clx6vj"
- AdmLogQueryService headers/request/response/error/details/formattedDetails 정리
- inboundHeaders detail
- resolvedHeaders detail
- outboundHeaders detail, 가능하면 구현
- headers formatType JSON 유지
- Authorization/API key/token 마스킹 확인
- transactionGlobalId/traceId/businessTransactionId 기준 상세 조회
- smoke-adm-runtime 또는 smoke-transaction-header-runtime 보강
```

## 완료 기준

```text id="tnv4dx"
- ADM 로그 상세 API가 summary/headers/request/response/error를 분리 반환한다.
- inbound/resolved/outbound 구분 기준이 있다.
- X-User-Id, X-Customer-No, X-Member-No가 분리 표시된다.
- X-Client-IP, X-Client-Country-Code가 표시된다.
- 민감 헤더 원문이 표시되지 않는다.
- 브라우저 클릭은 미검증이면 미검증으로 기록한다.
```

---

# CPF_REQUEST_015: DB 표준 규칙 기반 기존 SQL/테이블 점검 / 보정 후보 리포트

## 목적

REQUEST_012에서 만든 DB 표준을 기준으로 기존 SQL/Flyway/all_install/Mapper를 점검한다.

이번 요청도 **즉시 대량 변경하지 않고 점검과 보정 후보 분류가 핵심**이다.

## 범위

```text id="a92xnb"
- 주제영역별 prefix 점검
- 테이블 논리명 COMMENT 점검
- 컬럼 논리명 COMMENT 점검
- 공통 감사 컬럼 점검
- PK/FK/INDEX 명명 규칙 점검
- ID/NO/CODE/STATUS/TYPE/YN/AT/BY 컬럼 규칙 점검
- VARCHAR 길이 표준 점검
- JSON/LONGTEXT 사용 기준 점검
- Flyway와 all_install 정합성 점검
- check-sql-standard.ps1 보강 후보 도출
```

## 완료 기준

```text id="zh6d1k"
- 기존 DB 객체 표준 적합/부적합 목록이 리포트에 있다.
- 바로 고칠 항목과 보류할 항목이 분리되어 있다.
- 대량 DDL 변경은 하지 않는다.
- SQL 가이드와 DB 표준 가이드가 연결되어 있다.
```

---

# CPF_REQUEST_016: EDU 온라인 조회 패턴 샘플 구현

## 목적

개발자가 CPF 문서와 EDU 샘플만 보고 신규 온라인 조회 기능을 만들 수 있도록 실전형 조회 예제를 만든다.

## 범위

```text id="c6zk32"
- EDU 단건 조회 샘플
- EDU 목록 조회 샘플
- EDU offset page 조회 샘플
- EDU keyset page 조회 샘플
- 검색 조건 DTO
- 정렬 조건 DTO
- PageResponse
- KeysetResponse
- MyBatis Mapper
- Service/Repository/DTO/Controller
- Swagger
- 테스트
- 주석이 충분한 샘플 코드
- 표준 헤더 자동 검증/전파 샘플
```

## 필수 주석

```text id="apyxf4"
- @CpfTransaction을 왜 붙이는지
- 표준 헤더가 왜 필요한지
- 단건/목록/page/keyset 차이
- offset pagination의 비용
- keyset pagination의 장점과 제약
- limit + 1 조회로 hasNext 판단하는 이유
- 안정적인 정렬키가 필요한 이유
- 대량 조회 제한 기준
- 마스킹/권한/감사 위치
- CpfWebClient/CpfRestClient를 쓰면 헤더가 자동 전파되는 이유
```

## 완료 기준

```text id="llmw7c"
- 단건 조회가 있다.
- 목록 조회가 있다.
- offset page 조회가 있다.
- keyset page 조회가 있다.
- 각 샘플에 Controller/Service/Repository/Mapper/DTO/Validation/Swagger/Test가 있다.
- 개발 가이드와 EDU 문서에서 연결된다.
```

---

# CPF_REQUEST_017: EDU 타 주제영역 Facade 연동 조회 샘플

## 목적

다른 주제영역 테이블을 직접 JOIN하지 않고 Facade/API wrapper를 통해 연동하는 개발 표준을 EDU로 보여준다.

## 범위

```text id="z4le0f"
- EDU 주문 단건 + MBR 회원 요약 조회
- EDU 주문 목록 + MBR 회원 요약 batch 조회
- EDU 주문 목록 + 공통 코드명 변환
- EDU 주문 상세 + EXS 외부 상태 mock 조회
- N+1 방지 샘플
- 직접 JOIN 금지 주석
- Facade 연동 실패 시 fallback/부분응답 기준
- CpfWebClient/CpfRestClient outbound header 자동 전파 샘플
```

## 금지 예시

```text id="i7ujjr"
- EDU Mapper에서 mbr_member 직접 JOIN
- EDU Mapper에서 exs_* 직접 JOIN
- for-loop 안에서 회원 단건 조회 반복 호출
- outbound 호출 시 표준 헤더 수동 하드코딩
```

## 권장 예시

```text id="p8oi7k"
- 주문 목록 조회
- memberNos 추출
- MbrMemberFacade.findMemberSummaries(memberNos)
- Map으로 조립
- 누락 회원은 N/A 처리
- CpfWebClient/CpfRestClient로 호출하고 헤더는 자동 전파
```

## 완료 기준

```text id="p9w0jw"
- 타 주제영역 직접 DB 접근 금지 기준이 코드 주석과 문서에 있다.
- Facade 연동 샘플이 있다.
- N+1 방지 샘플이 있다.
- outbound header 자동 전파 샘플이 있다.
- 테스트가 있다.
```

---

# CPF_REQUEST_018: ADM 브라우저 실제 클릭 자동화 1차

## 목적

ADM 정적 UI marker와 API smoke를 넘어서 실제 브라우저 기준으로 메뉴 진입/조회/상세를 검증한다.

## 범위

```text id="notahc"
- ADM 로그인
- 메뉴 진입
- 거래 메타 목록
- 거래 메타 상세
- 거래 로그 목록
- 거래 로그 상세
- 헤더/요청/응답/오류 탭 확인
- 로그 정책 목록
- 로그 정책 상세
- 캐시 summary
- 배치 관제 목록
- 권한별 버튼 노출 확인
```

## 완료 기준

```text id="zqx7mf"
- 브라우저로 실제 메뉴 진입이 검증된다.
- 조회/상세 화면이 검증된다.
- 헤더부와 데이터부 탭이 확인된다.
- 실패하면 실패 화면/사유가 리포트에 남는다.
```

---

# CPF_REQUEST_019: ADM Cache 관제 / hit-miss / ttl / eviction / clear / 감사

## 목적

ADM Cache 관제를 운영자가 실제로 사용할 수 있는 수준으로 확장한다.

## 범위

```text id="qbffux"
- cache manager 목록
- cache name 목록
- key count
- estimated size
- hit/miss/eviction
- ttl/expire 정책
- cache refresh
- cache clear by name
- clear all 제한
- 운영 사유 필수
- 권한 체크
- 감사 로그
- 다중 인스턴스 반영 기준 문서화
```

## 완료 기준

```text id="tjm4o9"
- ADM cache API가 확장되어 있다.
- refresh/clear에 감사 로그가 있다.
- 다중 인스턴스는 local only / DB event / broker 중 실제 상태가 명확히 기록된다.
```

---

# CPF_REQUEST_020: Redis/Kafka/MQ mock/fallback 및 broker 전파 기준

## 목적

실 Redis/Kafka/MQ가 없더라도 CPF가 mock/fallback profile에서 정상 기동하고, 향후 실 broker 전파로 확장 가능한 구조를 갖춘다.

## 범위

```text id="wsb1dc"
- Redis adapter interface
- Kafka adapter interface
- MQ adapter interface
- disabled profile
- mock profile
- fallback behavior
- cache invalidation event 구조
- log policy propagation event 구조
- header propagation event가 필요한지 검토
- broker 미사용 시 local fallback
- 실 broker 미검증 절차 문서화
```

## 완료 기준

```text id="zvcnpm"
- 실 broker 없이 앱이 정상 기동한다.
- mock/fallback 테스트가 있다.
- 실 broker 연동 성공으로 과장하지 않는다.
```

---

# CPF_REQUEST_021: ADM 배치 운영 정책 / 강제수행 / lock / 파라미터 / BAT EDU

## 목적

ADM 배치 관제를 실제 운영자가 사용할 수 있는 수준으로 확장한다.

## 범위

```text id="vkkuwf"
- 배치 Job 목록/상세
- 실행 이력
- Step 이력
- 수동 실행
- 강제 수행
- 중지
- 재실행
- 파라미터 snapshot
- lock 상태
- worker 상태
- ghost 상태
- BAT EDU Job 샘플
- 권한/감사
```

## 완료 기준

```text id="m45uzf"
- ADM에서 Job 실행 흐름을 추적할 수 있다.
- 강제수행/중지/재실행 기준이 문서화되어 있다.
- BAT EDU 샘플이 있다.
```

---

# CPF_REQUEST_022: Ghost 자동 감지 Scheduler / 장애 시나리오 테스트

## 목적

서버 급중단, deploy/restart 중 RUNNING 배치가 ghost로 남는 상황을 자동 감지하고 운영자가 조치할 수 있게 한다.

## 범위

```text id="mpmftl"
- worker heartbeat timeout
- ghost candidate detection
- scheduler
- lock release candidate
- operator action
- action audit
- 재실행 가능 여부 판단
- 장애 시나리오 smoke
```

## 완료 기준

```text id="amwaqm"
- ghost 후보가 자동 감지된다.
- 운영자 조치 API가 있다.
- 조치 감사가 남는다.
- 장애 시나리오 테스트가 있다.
```

---

# CPF_REQUEST_023: 온디맨드 배치

## 목적

온라인/ADM/API 요청 기반으로 배치를 실행하는 온디맨드 배치 표준을 만든다.

## 범위

```text id="wjwhak"
- 온디맨드 배치 요청 API
- 파라미터 validation
- 요청 이력
- 실행 이력
- 권한
- 감사
- 실패/재실행
- BAT 연동
- EDU 샘플
```

## 완료 기준

```text id="mh4n2d"
- ADM/API에서 온디맨드 배치를 요청할 수 있다.
- 요청/실행/결과가 추적된다.
- 권한/감사가 적용된다.
```

---

# CPF_REQUEST_024: 센터컷 기본 구현체 / 업무별 커스텀 모수·item·result 연동

## 목적

대량 대상 처리 표준인 센터컷을 BAT 기본 구현체와 업무별 adapter 구조로 구현한다.

## 범위

```text id="s68i0o"
- BAT 기본 center-cut request/item/result 테이블
- worker claim/lock
- idempotency
- retry
- failed item 재처리
- metering
- worker heartbeat
- 업무별 TargetProvider
- 업무별 Handler
- 업무별 ResultProvider
- ADM 관제
```

## 주의

```text id="5ftp6i"
- PFW는 센터컷 대량 모수/item/result 테이블을 직접 소유하지 않는다.
- BAT는 기본 구현체를 제공한다.
- 업무 주제영역은 커스텀 테이블과 adapter로 연동 가능해야 한다.
```

## 완료 기준

```text id="qblx3d"
- scale-out worker 처리가 가능하다.
- item claim/lock이 있다.
- 중복 처리 방지 기준이 있다.
- ADM에서 상태를 볼 수 있다.
```

---

# CPF_REQUEST_025: 전체 설치 SQL 신규 DB 재검증

## 목적

누적 Flyway/all_install/seed가 신규 DB에서도 재현 가능한지 확인한다.

## 범위

```text id="f3ur8a"
- 신규 DB drop/create
- 00_all_install_and_smoke.sql 실행
- Flyway migration 정합성 확인
- seed idempotent 확인
- app 계정 DDL 차단
- migration/root 계정 DDL 허용
- 기본 smoke 실행
```

## 완료 기준

```text id="rb3o2q"
- 신규 DB 설치가 성공한다.
- seed 재실행이 안전하다.
- 권한 분리가 검증된다.
- 실패 시 SQL/문서/리포트에 기록된다.
```

---

# CPF_REQUEST_026: 트랜잭션 가이드 정본화

## 목적

온라인, 배치, 센터컷, EXS, 감사 로그 트랜잭션 기준을 개발자가 그대로 따라 할 수 있게 정본화한다.

## 범위

```text id="qve3ft"
- 온라인 트랜잭션
- 배치 chunk 트랜잭션
- 센터컷 item 트랜잭션
- EXS 수신/송신 트랜잭션
- 감사 로그 트랜잭션
- rollback/skip/retry 기준
- parent/child transactionGlobalId 기준
- 표준 헤더와 transaction context 연결
- 예제 코드
```

## 완료 기준

```text id="mkaqyi"
- specs/트랜잭션_가이드.html이 독립 문서로 존재한다.
- 온라인/배치/센터컷/EXS/감사 기준이 분리되어 있다.
- EDU 샘플과 연결된다.
```

---

# CPF_REQUEST_027: ADM 거래/오류/감사 통합 관제 UX 완성

## 목적

운영자가 ADM에서 “이 거래가 왜 실패했는지, 어떤 정책이 적용됐는지, 누가 조치했는지”를 한 흐름에서 추적할 수 있게 한다.

## 범위

```text id="bzldoq"
- 거래 메타 상세
- 거래 로그 목록/상세
- 오류 로그 조회
- 오류 조치 상태
- 오류 조치 이력
- 일반 감사 로그
- 정책 감사 로그
- transactionGlobalId 기반 통합 조회
- traceId 기반 통합 조회
- businessTransactionId 기반 통합 조회
- API wrapper/UI marker/browser smoke
```

## 완료 기준

```text id="tb9zt0"
- 거래 기준으로 로그/오류/감사/정책감사를 추적할 수 있다.
- 조치 상태와 이력이 남는다.
- ADM 브라우저 또는 runtime smoke로 검증된다.
```

---

# CPF_REQUEST_028: 최종 기능 매트릭스 정밀화 / 완료 판정 재검수

## 목적

현재까지의 기능을 완료/부분 구현/미구현/미검증/실패/재확인 필요 기준으로 재분류한다.

## 범위

```text id="hkeft1"
- 기능 구현 매트릭스 전체 재검토
- 대분류별 세부 기능 행 분리
- Controller/API
- Service
- Repository/Mapper
- DTO
- DB 테이블
- ADM UI/API wrapper
- Swagger
- EDU
- 테스트
- 검증 명령
- 실제 결과
- 미검증 사유
- 다음 보완
```

## 완료 기준

```text id="w77wk2"
- 완료는 증거가 있는 항목만 표시한다.
- 뭉뚱그린 행을 세부 행으로 분리한다.
- 다음 작업자가 매트릭스만 보고 남은 일을 알 수 있다.
```

---

# 권장 실행 순서

```text id="ussixh"
1. CPF_REQUEST_012
   표준 헤더 가이드 / DB 표준 가이드 / 소스 주석 기준 / EDU 조회 샘플 설계

2. CPF_REQUEST_013
   표준 헤더 자동 검증 / Context API / Outbound 자동 전파 구현

3. CPF_REQUEST_014
   ADM 헤더·데이터 분리 표시 / Inbound·Resolved·Outbound 관제 Smoke

4. CPF_REQUEST_015
   DB 표준 규칙 기반 기존 SQL/테이블 점검 / 보정 후보 리포트

5. CPF_REQUEST_016
   EDU 온라인 조회 패턴 샘플 구현

6. CPF_REQUEST_017
   EDU 타 주제영역 Facade 연동 조회 샘플

7. CPF_REQUEST_018
   ADM 브라우저 실제 클릭 자동화 1차

8. CPF_REQUEST_019
   ADM Cache 관제 / hit-miss / ttl / eviction / clear / 감사

9. CPF_REQUEST_020
   Redis/Kafka/MQ mock/fallback 및 broker 전파 기준

10. CPF_REQUEST_021
    ADM 배치 운영 정책 / 강제수행 / lock / 파라미터 / BAT EDU

11. CPF_REQUEST_022
    Ghost 자동 감지 Scheduler / 장애 시나리오 테스트

12. CPF_REQUEST_023
    온디맨드 배치

13. CPF_REQUEST_024
    센터컷 기본 구현체 / 업무별 커스텀 모수·item·result 연동

14. CPF_REQUEST_025
    전체 설치 SQL 신규 DB 재검증

15. CPF_REQUEST_026
    트랜잭션 가이드 정본화

16. CPF_REQUEST_027
    ADM 거래/오류/감사 통합 관제 UX 완성

17. CPF_REQUEST_028
    최종 기능 매트릭스 정밀화 / 완료 판정 재검수
```
