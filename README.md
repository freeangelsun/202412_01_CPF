# FPS 멀티모듈 금융 서비스 프레임워크

## 개요
이 프로젝트는 금융 서비스 백엔드를 구성하기 위한 Java 21, Spring Boot 3.4 기반 멀티모듈 애플리케이션입니다.

현재 활성 모듈은 다음과 같습니다.

- `pfw`: 거래ID, 보안 헤더, 거래 로그, 표준 예외, 파일 로그, 워크플로우 추적, WebClient 전파 등 프레임워크 인프라
- `cmn`: 개발 공통 함수, 공통 매퍼, 코드/메시지/설정값 캐시, 표준 오류 메시지, 캐시 리프레시 이벤트, 업무 공통 유틸리티
- `mbr`: 회원 관리 모듈, 금융권 표준 API 설계 적용
- `acc`: 계좌 관리 모듈, 기존 구조 유지 및 추후 표준화 예정
- `xyz`: 신규 개발자 교육용 주제영역, PFW/CMN 사용 샘플 제공

## 현재 기준
- 조회 조건과 식별자는 `/mbr/{id}` 같은 경로 변수로 받지 않습니다.
- 조회 조건은 쿼리 파라미터로 명시합니다.
- 등록/수정 같은 변경 작업은 JSON Body로 명시합니다.
- 모든 MBR 응답은 `BaseResponse` 형식으로 반환합니다.
- 요청 로그, 응답 헤더, 응답 본문은 같은 `transactionId`, `traceId`를 사용합니다.
- 표준 포맷의 `X-Transaction-Id`가 들어오면 그대로 이어받고, 없거나 포맷이 다르면 프레임워크가 새로 생성합니다.
- 거래ID 포맷은 `yyyyMMddHHmmssSSS + MODULE + WAS_ID + SEQ`입니다.
- 요청 파라미터 로그는 남기되, 토큰/비밀번호/계좌번호/카드번호 등 민감 키는 마스킹합니다.
- 분산거래/후처리 흐름은 워크플로우 ID, 스텝 ID, 실패 정책, 보상 상태로 추적합니다.
- 표준 오류는 `FpsException` 계열로 처리하고, 고객용/내부용 메시지는 CMN 메시지 테이블에서 분리 관리합니다.
- 파일 로그는 모듈 일자 로그와 거래ID별 보조 추적 로그를 함께 남기며 30일 보관합니다.
- 모든 신규 테이블은 등록자/등록시각/수정자/수정시각 감사 컬럼을 기본으로 둡니다.
- 기본 보안 헤더를 응답에 추가합니다.

## 모듈 구조
```text
root
├── pfw
│   └── src/main/java/fps/pfw
│       ├── common
│       │   ├── aop
│       │   ├── exception
│       │   ├── filter
│       │   ├── http
│       │   ├── logging
│       │   ├── web
│       │   └── workflow
│       ├── config
│       ├── mapper
│       └── service
├── cmn
│   └── src/main/java/fps/cmn
│       ├── cde       # 공통코드
│       ├── cfg       # 공통설정
│       ├── config
│       ├── msg       # 공통메시지
│       ├── ref       # 캐시 리프레시 이벤트
│       ├── smp       # 샘플 공통 기능
│       └── utils
├── mbr
│   └── src/main/java/fps/mbr
│       ├── bse       # 기본회원관리
│       │   ├── controller
│       │   ├── dto
│       │   ├── entity
│       │   ├── mapper
│       │   └── service
│       ├── common
│       │   ├── aop
│       │   ├── exception
│       │   ├── filter
│       │   ├── request
│       │   └── response
│       └── config
├── acc
    └── src/main/java/fps/acc
        ├── bse       # ACC 기본/샘플 업무
        ├── common
        ├── config
        └── tst       # 테스트/샘플 컨트롤러
└── xyz
    └── src/main/java/fps/xyz
        └── edu       # 교육용 프레임워크 사용 샘플
```

## 기술 스택
| 항목 | 기준 |
| --- | --- |
| Java | 21 |
| Spring Boot | 3.4.13 |
| Gradle | 8.11.1 |
| DB | MariaDB |
| SQL Mapper | MyBatis |
| Cache | Spring Cache + Caffeine |
| AOP | Spring AOP + AspectJ |
| Service Call | Spring WebClient |
| Logging | Logback + MDC + SiftingAppender |
| Validation | Jakarta Validation |
| API Docs | springdoc-openapi + Swagger UI + Scalar |

## MBR API
MBR 모듈은 명시 파라미터 기반 API를 사용합니다.

```text
GET    /mbr/list
GET    /mbr/detail?memberId=1
GET    /mbr/search?name=김
POST   /mbr/create
PUT    /mbr/update
DELETE /mbr/delete?memberId=1
```

사용하지 않는 방식:

```text
GET /mbr/{id}
```

위 방식은 회원번호 같은 식별자가 경로에 묻히고, 향후 감사 로그/접근 제어/보안 정책 적용 시 의미가 불명확해질 수 있어 사용하지 않습니다.

## MBR 요청 검증
컨트롤러 진입 단계에서 Bean Validation을 적용합니다.

- `memberId`: 필수, 양수
- `name`: 필수, 최대 100자
- `memberName`: 필수, 최대 100자
- `description`: 최대 255자

검증 실패, 필수 파라미터 누락, 파라미터 타입 오류, JSON Body 파싱 오류는 모두 `BaseResponse` 형식으로 응답합니다.

## 표준 예외와 메시지
PFW는 `FpsValidationException`, `FpsNotFoundException`, `FpsBusinessException`, `FpsExternalServiceException`, `FpsSystemException`을 제공합니다. 업무 개발자는 개별 컨트롤러에서 오류 응답을 직접 만들지 않고 표준 예외를 던집니다.

표준 예외는 `FpsGlobalExceptionHandler`에서 공통 오류 응답과 오류 헤더로 변환됩니다. 고객에게 내려보낼 메시지는 `cmnDB.message_table`의 `message_type=EXTERNAL`, 내부 추적 메시지는 `message_type=INTERNAL`로 관리하고, `{fieldName}` 같은 동적 메시지 인자는 PFW가 응답과 `TRAN_LOG`에 동일하게 치환합니다.

오류코드는 업무 케이스마다 계속 늘리지 않고 대표 코드를 재사용합니다. 업무 오류는 `ERR.*`, PFW 코어 오류는 `PFW.*`로 분리하고, 세부 문구는 `FpsDynamicErrorCode`와 메시지 인자로 조립합니다.

샘플:

```text
GET /acc/tran/standard-exception?type=validation
GET /acc/tran/standard-exception?type=business
GET /acc/tran/standard-exception?type=external
GET /xyz/edu/exception/dynamic-message?fieldName=회원번호&fieldValue=M0001
```

## 공통 응답 형식
```json
{
  "messageId": "MSG_1717020600000",
  "transactionId": "20260611141234567ACCaccAP010000001",
  "traceId": "550e8400-e29b-41d4-a716-446655440000",
  "statusCode": "1000",
  "message": "성공",
  "data": {},
  "errorDetail": null,
  "timestamp": "2026-06-11T13:00:00"
}
```

`transactionId`는 업무 거래 전체 추적용 ID이고, `traceId`는 기술 로그 추적용 ID입니다. 공통 `TransactionContextFilter`가 요청 단위로 만들거나 외부 헤더를 이어받고, `BaseResponse`와 로그가 같은 값을 재사용합니다.

거래ID 생성 규칙:

```text
yyyyMMddHHmmssSSS + MODULE + WAS_ID + SEQ
예: 20260611141234567ACCaccAP010000001
```

- `yyyyMMddHHmmssSSS`: 생성 일시, 밀리초 포함
- `MODULE`: 생성 주제영역 3자리, 예: `ACC`, `MBR`, `CMN`
- `WAS_ID`: 생성 WAS 식별자, 예: `accAP01`. 운영에서는 WAS별로 반드시 유일해야 합니다.
- `SEQ`: WAS별 일련번호, 기본 7자리

## 거래 헤더 표준
컨트롤러 업무 거래로 진입하는 요청은 PFW 공통 인터셉터에서 필수 헤더를 검증합니다. `X-Transaction-Id`는 외부에서 표준 포맷으로 들어오면 그대로 쓰고, 없거나 비표준이면 `pfw`가 생성해서 컨트롤러 진입 시 항상 존재하게 합니다.

필수 헤더:

| 헤더 | 설명 |
| --- | --- |
| `X-Transaction-Id` | 글로벌 거래ID. 없으면 프레임워크가 생성 |
| `X-Request-Type` | 요청 구분. 예: `INQUIRY`, `CREATE`, `UPDATE`, `DELETE` |
| `X-Original-Channel-Code` | 최초 전송 채널 코드 |
| `X-Channel-Code` | 현재 전송 채널 코드 |

선택 헤더:

| 헤더 | 설명 |
| --- | --- |
| `X-Member-No` | 회원번호. 있으면 로그 검색 컬럼에 적재 |
| `X-Customer-No` | 고객번호. 있으면 로그 검색 컬럼에 적재 |
| `X-User-Id` | 수행 사용자 ID |
| `X-Screen-Id` | 화면 ID |
| `X-Device-Id` | 디바이스 ID |
| `X-Client-Request-Time` | 클라이언트 요청 일시 |
| `X-Client-IP` | 클라이언트 IP. 있으면 `CLIENT_IP`에 우선 적재 |
| `X-Reserved-Field-1` | 확장 예약 필드 1 |
| `X-Reserved-Field-2` | 확장 예약 필드 2 |
| `X-Reserved-Field-3` | 확장 예약 필드 3 |
| `X-Reserved-Field-4` | 확장 예약 필드 4 |
| `X-Reserved-Field-5` | 확장 예약 필드 5 |

컨트롤러 개발 시 업무 거래ID와 거래명을 함께 선언합니다.

```java
@GetMapping("/detail")
@FpsTransaction(id = "MBR01BSE0002", name = "회원기본상세조회")
public ResponseEntity<BaseResponse<MbrDTO>> getDetail(...) {
    ...
}
```

`@FpsTransaction.id`는 `{주제영역3}{거래유형2}{중간도메인3}{일련번호4}` 형식의 12자리입니다. 예: `MBR01BSE0002`. 이 값은 로그의 `BUSINESS_TRANSACTION_ID`, 논리명은 `BUSINESS_TRANSACTION_NAME`에 적재됩니다.

분산거래/후처리/보상 추적이 필요한 컨트롤러는 워크플로우를 함께 선언합니다.

```java
@FpsTransaction(id = "ACC08BSE0001", name = "MBR회원상세조회연계샘플")
@FpsWorkflow(id = "ACC08BSE9001", name = "MBR회원상세조회연계워크플로우")
@FpsWorkflowStep(name = "MBR회원상세조회호출", failurePolicy = FpsWorkflowFailurePolicy.VERIFY)
public ResponseEntity<Map<String, Object>> getMbrMemberDetail(...) {
    ...
}
```

실패 정책은 `FAIL`, `RETRY`, `COMPENSATE`, `PENDING`, `VERIFY`, `MANUAL`, `IGNORE` 중 선택합니다. 로그에는 `WORKFLOW_INSTANCE_ID`, `WORKFLOW_STEP_ID`, `WORKFLOW_STATUS`, `COMPENSATION_YN`, 실제 실행 패키지/클래스/메서드가 함께 남습니다.

## 보안 및 감사 기본값
현재 공통 프레임워크에 적용된 기본값입니다.

- `TransactionContextFilter`
  - `X-Transaction-Id`
  - `X-Trace-Id`
  - `X-Span-Id`
  - `X-Content-Type-Options: nosniff`
  - `X-Frame-Options: DENY`
  - `X-Permitted-Cross-Domain-Policies: none`
  - `Referrer-Policy: no-referrer`
  - `Cache-Control: no-store, no-cache, must-revalidate, max-age=0`
  - `Pragma: no-cache`
  - `Content-Security-Policy: default-src 'none'; frame-ancestors 'none'`
- `fps.pfw.common.logging.TransactionIdGenerator`
  - 공통 거래ID 생성 메소드 제공
  - `fps.framework.was-id`, `fps.framework.transaction-id.sequence-digits` 설정 사용
  - 표준 포맷의 inbound 거래ID만 이어받고, 비표준 값은 새 거래ID로 교체
- `fps.pfw.common.logging.LoggingAspect`
  - 거래ID, TraceId, SpanId 로깅
  - 요청 URI, HTTP Method, 명시 파라미터, Body 로깅
  - 응답 코드, 응답 결과, 성공/실패, 처리 시간 로깅
  - 워크플로우/보상 상태와 실제 실행 패키지/클래스/메서드 로깅
  - `TRAN_LOG`, `TRAN_LOG_DTL` DB 적재 이벤트 발행
- `fps.pfw.common.workflow`
  - `@FpsWorkflow`, `@FpsWorkflowStep` 선언 제공
  - 워크플로우/보상 헤더를 하위 서비스로 자동 전파
- `fps.mbr.common.aop.LoggingAspect`
  - 서비스 메서드 지연 로깅
  - 민감 키 마스킹
- `logback`
  - 모든 파일/콘솔 로그 패턴에 `transactionId`, `traceId`, `spanId` 포함
  - 거래ID별 보조 추적 파일을 `transactions/yyyy-MM-dd/{transactionId}.0.log` 형태로 생성
  - 기본 보관 기간 30일
- 추후 인증/인가가 붙으면 현재 `REQUESTER_ID = SYSTEM` 영역을 SecurityContext 기반으로 교체합니다.

`mbr`, `acc` 같은 주제영역은 `pfw`를 스캔하면 거래 컨텍스트 생성, 보안 헤더, 컨트롤러 거래 로그 적재가 자동 적용됩니다. `cmn`은 개발자가 명시적으로 재사용하는 개발 공통 기능으로 둡니다.

## 서비스 간 호출 표준
`pfw`는 주제영역 간 HTTP 호출을 위한 `FpsWebClient`를 제공합니다. 호출 시 현재 요청의 거래 헤더와 워크플로우 헤더를 자동으로 전파하므로 `ACC -> MBR -> 기타 주제영역` 흐름을 같은 글로벌 거래ID와 워크플로우 인스턴스로 추적할 수 있습니다.

공통 설정:

```yaml
fps:
  http-client:
    connect-timeout-millis: 3000
    read-timeout-millis: 5000
    max-in-memory-size-kb: 2048
  services:
    acc:
      base-url: ${FPS_SERVICE_ACC_URL:http://localhost:8080}
    mbr:
      base-url: ${FPS_SERVICE_MBR_URL:http://localhost:8081}
    cmn:
      base-url: ${FPS_SERVICE_CMN_URL:http://localhost:8082}
```

운영에서는 IP를 직접 쓰기보다 DNS, VIP, Kubernetes Service 이름, 또는 Config Server 값을 권장합니다. 예: `FPS_SERVICE_MBR_URL=http://mbr-service:8081`

ACC에서 MBR 회원 상세조회를 호출하는 샘플:

```text
GET /members/mbr/detail?memberId=1
```

이 요청이 ACC로 들어올 때의 필수 거래 헤더와 워크플로우 헤더가 MBR 호출에도 자동 전달됩니다. 샘플 구현은 `fps.acc.bse.service.MbrMemberClientService`와 `fps.acc.bse.controller.MemberController`를 확인하세요.

## 공통 관리 기능
CMN은 코드, 메시지, 설정값 3종을 기본 공통 관리 대상으로 제공합니다. ACC 샘플 API는 `/acc/cmn` 하위에 있으며 조회/등록/수정/삭제/수동 리프레시를 제공합니다.

```text
GET    /acc/cmn/codes
POST   /acc/cmn/codes
PUT    /acc/cmn/codes?codeId=1
DELETE /acc/cmn/codes?codeId=1
POST   /acc/cmn/codes/refresh

GET    /acc/cmn/messages
POST   /acc/cmn/messages
PUT    /acc/cmn/messages?messageId=1
DELETE /acc/cmn/messages?messageId=1
POST   /acc/cmn/messages/refresh

GET    /acc/cmn/configs
POST   /acc/cmn/configs
PUT    /acc/cmn/configs?configId=1
DELETE /acc/cmn/configs?configId=1
POST   /acc/cmn/configs/refresh
```

CRUD가 성공하면 현재 WAS 캐시는 즉시 갱신되고, `cmnDB.cache_refresh_event`를 통해 다른 WAS도 기본 5초 주기로 변경 이벤트를 감지합니다. 개발 기준은 [specs/개발_가이드.md](specs/개발_가이드.md), 운영 기준은 [specs/관리자_가이드.md](specs/관리자_가이드.md)를 확인하세요.

## XYZ 교육용 주제영역
`xyz`는 실제 업무가 아니라 신규 개발자 교육과 테스트 개발을 위한 샘플 주제영역입니다.

```text
GET    /xyz/edu/samples
GET    /xyz/edu/samples/detail?sampleId=1
POST   /xyz/edu/samples
PUT    /xyz/edu/samples?sampleId=1
DELETE /xyz/edu/samples?sampleId=1
GET    /xyz/edu/cache
POST   /xyz/edu/cache/refresh
GET    /xyz/edu/exception?type=validation
GET    /xyz/edu/exception/dynamic-message
GET    /xyz/edu/utils
POST   /xyz/edu/transaction/single
POST   /xyz/edu/transaction/separated
PUT    /xyz/edu/admin/log-level
GET    /xyz/edu/admin/log-level
DELETE /xyz/edu/admin/log-level
```

샘플은 목록/단건/등록, 캐시 조회/리프레시, 표준 예외, 정의 오류코드 기반 동적 메시지, 단일/분리 트랜잭션, 특정 거래 동적 로그레벨을 보여줍니다.

## API 문서
각 업무 서비스는 PFW 공통 OpenAPI 설정을 통해 Swagger UI와 Scalar 문서를 제공합니다.

| 서비스 | Swagger UI | Scalar |
| --- | --- | --- |
| ACC | `http://localhost:8080/swagger-ui.html` | `http://localhost:8080/scalar` |
| MBR | `http://localhost:8081/swagger-ui.html` | `http://localhost:8081/scalar` |
| XYZ | `http://localhost:8099/swagger-ui.html` | `http://localhost:8099/scalar` |

OpenAPI JSON은 `/v3/api-docs`, YAML은 `/v3/api-docs.yaml`에서 확인합니다. API 그룹, 거래 헤더 자동 문서화, 운영 비활성화 기준은 [specs/개발_가이드.md](specs/개발_가이드.md)를 확인하세요.

## MBR 설정 파일
MBR은 Spring Boot 표준 `application.yml`을 진입점으로 사용합니다.

```text
mbr/src/main/resources/application.yml
mbr/src/main/resources/application-mbr.yml
mbr/src/main/resources/application-mbr-local.yml
```

`application.yml`은 다음 설정을 가져옵니다.

- `application-pfw.yml`
- `application-cmn.yml`
- `application-mbr.yml`
- `application-mbr-local.yml`

MBR 데이터소스와 MyBatis는 명시 Bean으로 구성합니다.

- `mbrDataSource`
- `mbrTransactionManager`
- `mbrSqlSessionFactory`
- `mbrSqlSessionTemplate`
- `mbrMemberMapper`

## DB 기준
로컬 개발 기준 DB 접속 정보:

```text
host: localhost
port: 3306
user: root
password: test
```

주요 테이블:

- `cmnDB.member`: MBR 회원 API
- `cmnDB.cmn_member`: 기존 CMN 회원 조회
- `cmnDB.code_table`: 공통 코드 캐시
- `cmnDB.message_table`: 공통 메시지 캐시
- `cmnDB.config_table`: 공통 설정값 캐시
- `cmnDB.cache_refresh_event`: 다중 WAS 공통 캐시 리프레시 이벤트
- `pfwDB.TRAN_LOG`, `pfwDB.TRAN_LOG_DTL`: 프레임워크 필수 거래 로그 테이블
- `accDB.acc_member`: ACC 회원/계좌 샘플
- `admDB.operator_*`: 추후 운영 UI용 운영자/권한/감사 테이블 기준

개발 표준, 패키지/거래ID, 표준 예외/메시지, OpenAPI, XYZ 교육용 샘플 기준은 [specs/개발_가이드.md](specs/개발_가이드.md)를 확인하세요.
아키텍처, 배포, DB, 프레임워크 필수 로그 테이블, 파일 로그, 운영자 관리 기준은 [specs/관리자_가이드.md](specs/관리자_가이드.md)를 확인하세요.

## 빌드와 테스트
일반 빌드:

```powershell
.\gradlew.bat build
```

오프라인 검증:

```powershell
.\gradlew.bat build --offline
```

MBR 모듈 검증:

```powershell
.\gradlew.bat :mbr:clean :mbr:build --offline
```

참고:

- `mbr` 테스트는 활성화되어 있습니다.
- `pfw`, `acc`, `cmn` 테스트는 현재 빌드 설정에서 비활성화되어 있습니다.
- Windows에서 `clean build` 실행 시 `pfw/build/libs/*.jar` 또는 `cmn/build/libs/*.jar`가 잠겨 삭제 실패할 수 있습니다. 이 경우 Gradle 데몬/IDE 잠금을 해제한 뒤 다시 실행하거나, 코드 검증은 `build --offline`으로 진행합니다.

## 로컬 실행
로컬에서 전체 주제영역을 한 번에 실행할 때는 실제 서버 애플리케이션인 `mbr`, `acc`, `xyz`를 실행합니다. `pfw`, `cmn`은 각 실행 JAR 안에 라이브러리로 포함되므로 별도 포트나 WAS로 띄우지 않습니다.

전체 주제영역 실행:

```powershell
.\gradlew.bat runLocalServices
```

VS Code나 IntelliJ에서는 Gradle 창에서 `Tasks > application > runLocalServices`를 더블클릭하면 됩니다.

MBR 실행:

```powershell
.\gradlew.bat :mbr:bootRun
```

로컬 기본 포트는 `8081`입니다.

ACC 실행:

```powershell
.\gradlew.bat :acc:bootRun
```

로컬 기본 포트는 `8080`입니다.

XYZ 실행:

```powershell
.\gradlew.bat :xyz:bootRun
```

로컬 기본 포트는 `8099`입니다.

## 서버 배포
서버 배포 단위는 실제 서비스 모듈인 `mbr`, `acc`입니다. `pfw`, `cmn`은 공통 라이브러리 모듈이므로 각 서비스 JAR에 자동 포함되고 별도 WAS로 배포하지 않습니다.

MBR 배포 산출물 생성:

```powershell
.\gradlew.bat :mbr:clean :mbr:bootJar
```

ACC 배포 산출물 생성:

```powershell
.\gradlew.bat :acc:clean :acc:bootJar
```

Jenkins에서는 배포 대상 모듈만 선택해 `bootJar`를 만들고, 생성된 `mbr/build/libs/*.jar` 또는 `acc/build/libs/*.jar`를 대상 WAS/서버에 반영합니다. 상세 절차와 운영 확인 항목은 [specs/관리자_가이드.md](specs/관리자_가이드.md)를 기준으로 관리합니다.

## 문서 관리 원칙
소스 변경 시 관련 문서를 함께 수정합니다.

- API 경로/파라미터 변경: `README.md`, `specs/개발_가이드.md`
- DB 스키마 변경: `README.md`, `specs/관리자_가이드.md`
- 프레임워크 필수 테이블 변경: `README.md`, `specs/관리자_가이드.md`
- 테이블 공통 컬럼/인덱스 기준 변경: `specs/관리자_가이드.md`
- 보안/로깅/응답/워크플로우 규격 변경: `README.md`, `specs/개발_가이드.md`, `specs/관리자_가이드.md`
- 표준 예외/메시지/오류 응답 변경: `README.md`, `specs/개발_가이드.md`, `specs/관리자_가이드.md`
- OpenAPI/Swagger/Scalar API 문서 변경: `README.md`, `specs/개발_가이드.md`
- 파일 로그 정책 변경: `README.md`, `specs/관리자_가이드.md`
- 트랜잭션 관리 기준 변경: `README.md`, `specs/개발_가이드.md`, `specs/관리자_가이드.md`
- 운영 UI/운영자 관리 기준 변경: `README.md`, `specs/관리자_가이드.md`
- 서비스 간 호출/공통 WebClient 변경: `README.md`, `specs/개발_가이드.md`, `specs/관리자_가이드.md`
- 코드/메시지/설정값 공통관리 변경: `README.md`, `specs/개발_가이드.md`, `specs/관리자_가이드.md`
- 빌드/실행/배포 방법 변경: `README.md`, `specs/개발_가이드.md`, `specs/관리자_가이드.md`
- 패키지/업무 도메인/거래ID 규칙 변경: `README.md`, `specs/개발_가이드.md`
