# CPF Gateway 운영 매뉴얼 작성 지침

## 1. 문서 목적

Gateway 운영 매뉴얼은 **Gateway를 처음 접하는 운영자도 현재 상태를 확인하고, 설정을 조회하고, 요청 흐름을 추적하고, 이상 상황의 원인을 좁혀 필요한 조치를 수행할 수 있도록 하는 실무 문서**로 작성한다.

개발 구조를 설명하는 기술 명세서가 아니라 실제 운영 과정에서 다음 질문에 바로 답할 수 있어야 한다.

- Gateway가 현재 정상 동작 중인지 어떻게 확인하는가?
- 어떤 요청이 Gateway를 통과하는가?
- 특정 API가 어느 서비스로 전달되는가?
- Route가 실제로 적용되어 있는가?
- 인증·인가 실패인지 Gateway 자체 문제인지 어떻게 구분하는가?
- 특정 요청이 왜 차단되었는가?
- Timeout, 4xx, 5xx가 발생했을 때 어디부터 확인하는가?
- 어떤 인스턴스가 요청을 처리했는가?
- 설정 변경이 실제 Runtime에 반영되었는가?
- 운영 중 변경 가능한 항목과 재기동이 필요한 항목은 무엇인가?
- Gateway를 재기동하거나 여러 인스턴스를 순차적으로 조작하려면 어떻게 하는가?
- 잘못된 Route나 설정을 원래 상태로 되돌리려면 어떻게 하는가?
- 로그에서 사용자 개인정보나 인증정보가 노출되지 않는가?
- 운영자가 실행하면 안 되는 명령이나 조치는 무엇인가?

운영자가 소스코드를 열어보지 않아도 기본적인 운영 업무를 수행할 수 있는 수준을 목표로 한다.

---

# 2. 문서 작성 기본 원칙

## 2.1 기능 설명보다 운영 행동 중심으로 작성

다음과 같이 작성하지 않는다.

> Gateway는 Spring Cloud Gateway 기반으로 Route Predicate와 Filter를 사용한다.

운영 매뉴얼에서는 다음처럼 연결한다.

> `/api/order/**` 요청이 정상적으로 전달되지 않을 경우 Route 조회 화면 또는 관리 API에서 현재 적용 Route를 확인한다.
> 대상 Route ID, Path 조건, 대상 URI, 활성 상태를 확인한 후 Gateway 로그의 Route ID와 비교한다.

즉 모든 설명은 가능한 한 다음 흐름을 따른다.

**상황 → 확인 위치 → 확인 값 → 정상 기준 → 이상 기준 → 조치 → 결과 확인**

---

## 2.2 화면·API·명령어를 함께 제공

같은 작업이 여러 방법으로 가능한 경우 다음을 모두 표시한다.

| 작업권장 방법대체 방법사용 권한 |                 |                 |       |
| ----------------- | --------------- | --------------- | ----- |
| Gateway 상태 확인     | ADM 화면          | Health API      | 운영 조회 |
| Route 조회          | ADM Gateway 메뉴  | 관리 API          | 운영 조회 |
| Route 변경          | ADM 관리 기능       | 관리 API          | 운영 변경 |
| Instance 상태 확인    | ADM Instance 조회 | Actuator/관리 API | 운영 조회 |
| 로그 확인             | 로그 수집 시스템       | 서버 로그           | 로그 조회 |

운영자가 어떤 접근 방법을 사용할 수 있는지 즉시 판단할 수 있어야 한다.

---

# 3. 문서 전체 구성

## 3.1 Gateway 개요

최소 다음 내용을 포함한다.

### Gateway 역할

Gateway가 담당하는 기능을 운영자 관점에서 설명한다.

예:

- 외부 요청 진입점
- 요청 Route 결정
- Backend 서비스 전달
- 인증 정보 전달 또는 검증 연계
- 요청 Header 처리
- 공통 Filter 적용
- Request/Response Logging
- Timeout 처리
- 오류 응답 변환
- Trace 정보 생성·전달
- 필요 시 Rate Limit 또는 접근 제어

실제 CPF 구현에 존재하는 기능만 최종 문서에 남긴다.

---

## 3.2 Gateway 처리 흐름

운영자가 요청이 어디를 거쳐 가는지 이해할 수 있도록 흐름을 제공한다.

예:

```
Client
  ↓
Load Balancer / Reverse Proxy
  ↓
CPF Gateway
  ├─ Request ID / Trace ID 생성·확인
  ├─ 인증/인가 연계
  ├─ Route 선택
  ├─ Gateway Filter
  ├─ 요청 Header 처리
  ↓
Target Service
  ↓
Gateway Response Filter
  ↓
Client

```

각 단계마다 다음 정보를 연결한다.

| 단계운영 확인 대상 |                       |
| ---------- | --------------------- |
| Gateway 진입 | Access Log            |
| 요청 식별      | Request ID / Trace ID |
| 인증         | 인증 결과 및 오류 코드         |
| Route 선택   | Route ID              |
| Backend 전달 | Target Service / URI  |
| Backend 응답 | HTTP Status           |
| 최종 응답      | Gateway 응답 코드 및 처리시간  |

---

# 4. Gateway 구성 정보

## 4.1 배포 구성

실제 환경별 구성을 표로 제공한다.

| 항목DEVTESTPROD |      |      |      |
| ------------- | ---- | ---- | ---- |
| Gateway 주소    | 실제 값 | 실제 값 | 실제 값 |
| 관리 주소         | 실제 값 | 실제 값 | 실제 값 |
| Instance 수    | 실제 값 | 실제 값 | 실제 값 |
| Port          | 실제 값 | 실제 값 | 실제 값 |
| 설정 위치         | 실제 값 | 실제 값 | 실제 값 |
| 로그 위치         | 실제 값 | 실제 값 | 실제 값 |

운영 주소, 관리 주소, 내부 Endpoint를 혼동하지 않도록 구분한다.

---

## 4.2 Instance 구성

다중 Instance 환경이면 반드시 설명한다.

예:

```
Gateway
 ├─ gateway-01
 ├─ gateway-02
 └─ gateway-03

```

다음 항목을 제공한다.

- Instance ID
- Host
- Port
- 상태
- 시작 시간
- Version
- Build 정보
- Commit 정보
- 적용 Configuration Version
- Traffic 수신 여부

특정 Instance만 다른 설정으로 실행되는 상황을 확인할 수 있어야 한다.

---

# 5. 운영 메뉴 구성

실제 ADM 화면이 존재한다면 메뉴 단위로 작성한다.

예:

```
ADM
 └─ Gateway 관리
      ├─ Gateway 현황
      ├─ Instance 현황
      ├─ Route 조회
      ├─ Route 상세
      ├─ Filter 조회
      ├─ 요청 추적
      ├─ 오류 조회
      ├─ 설정 조회
      └─ 변경 이력

```

실제 제품 메뉴와 정확하게 일치시킨다.

---

# 6. Gateway 현황

첫 화면에서 운영자가 전체 상황을 판단할 수 있도록 한다.

권장 표시 항목:

| 항목설명         |                    |
| ------------ | ------------------ |
| 전체 Instance  | 등록된 Gateway 수      |
| 정상 Instance  | 현재 응답 가능한 Instance |
| 요청 건수        | 기준 시간 요청량          |
| 성공 건수        | 정상 처리 요청           |
| 4xx          | Client 계열 오류       |
| 5xx          | Server 계열 오류       |
| 평균 응답시간      | Gateway 처리시간       |
| 최대 응답시간      | 가장 오래 걸린 요청        |
| Timeout      | Timeout 발생 건수      |
| Active Route | 활성 Route 수         |

각 값에 대해 정상/주의 기준이 존재하면 함께 적는다.

---

# 7. 상태 확인

## 7.1 기본 Health 확인

반드시 실제 명령 예제를 제공한다.

```
curl <gateway-health-url>

```

예상 결과 예:

```
{
  "status": "UP"
}

```

그리고 결과 판정 기준을 작성한다.

| 결과판단               |                          |
| ------------------ | ------------------------ |
| HTTP 200 / UP      | 정상                       |
| Connection Refused | Process 또는 Network 확인    |
| Timeout            | Network 또는 Gateway 응답 확인 |
| HTTP 5xx           | Gateway 내부 상태 확인         |
| DOWN               | Dependency 상태 확인         |

---

## 7.2 Instance별 확인

Load Balancer 주소뿐 아니라 개별 Gateway Instance 확인 방법도 제공한다.

```
gateway-01 → 정상
gateway-02 → 정상
gateway-03 → 응답 없음

```

전체 주소가 정상이라고 개별 Instance도 정상이라고 판단하지 않는다.

---

# 8. Route 운영

Gateway 운영 매뉴얼에서 가장 중요한 부분 중 하나로 상세히 작성한다.

## 8.1 Route 목록

Route 조회 화면 또는 API에서 최소 다음 항목을 제공한다.

| 항목설명       |                |
| ---------- | -------------- |
| Route ID   | Route 고유 식별자   |
| Path       | 요청 Path 조건     |
| Method     | 허용 HTTP Method |
| Target     | 전달 대상          |
| Predicate  | Route 조건       |
| Filter     | 적용 Filter      |
| Order      | 적용 우선순위        |
| Enabled    | 활성 여부          |
| Source     | Route 정의 출처    |
| Updated At | 마지막 변경 시간      |

---

## 8.2 Route 확인 방법

특정 URL이 어디로 전달되는지 확인하는 방법을 예제로 설명한다.

예:

```
요청
GET /api/order/orders/100

확인
1. Route 목록에서 /api/order/** 검색
2. Route ID 확인
3. Target Service 확인
4. Filter 확인
5. 실제 Backend Endpoint 확인

```

---

## 8.3 Route 우선순위

겹치는 Route가 존재할 경우 반드시 설명한다.

예:

```
/api/**
/api/order/**

```

운영자가 다음을 알 수 있어야 한다.

- 어떤 Route가 먼저 평가되는가
- 우선순위 값의 의미
- 동일 Pattern 충돌 시 처리 방식
- 잘못된 Route 설정 시 나타나는 현상

---

# 9. Route 변경

실제 운영 환경에서 Dynamic Route를 지원하는 경우에만 작성한다.

## 변경 전 확인

- 현재 Route
- 변경 대상 Route
- 영향 API
- 대상 Backend
- 현재 요청량
- 변경 권한
- 이전 설정
- 원복 방법

---

## 변경 절차

```
1. 현재 설정 조회
2. 변경 전 값 기록
3. 변경 내용 입력
4. 변경 내용 검토
5. 적용
6. Runtime 반영 상태 확인
7. Test 요청
8. Gateway 로그 확인
9. Backend 수신 확인

```

---

## 절대 즉시 변경하면 안 되는 항목

실제 CPF 정책에 맞추어 다음과 같은 항목을 표시한다.

- 전체 요청을 포괄하는 Path
- 인증 관련 Filter
- Default Route
- 공통 Header Filter
- Timeout 공통 설정
- CORS 공통 설정
- 전체 Traffic 대상 Rate Limit

---

# 10. 설정 반영 방식

Gateway 설정별 반영 방식을 표로 만든다.

| 설정즉시 반영Refresh재기동비고 |     |     |     |       |
| ------------------- | --- | --- | --- | ----- |
| Route               | O/X | O/X | O/X | 실제 구현 |
| Timeout             |     |     |     |       |
| CORS                |     |     |     |       |
| Filter              |     |     |     |       |
| Logging             |     |     |     |       |
| Rate Limit          |     |     |     |       |

운영자가 설정을 바꾸고도 반영 방식 때문에 혼동하지 않도록 한다.

---

# 11. 인증 및 인가 관련 운영

Gateway가 인증·인가 과정에 참여한다면 반드시 별도 장으로 구성한다.

운영자에게 다음 구분법을 제공한다.

| 증상주요 확인 대상 |                    |
| ---------- | ------------------ |
| 401        | 인증 정보              |
| 403        | 권한                 |
| 404        | Route / Backend    |
| 429        | 요청 제한              |
| 500        | Gateway 또는 Backend |
| 502        | Backend 연결         |
| 503        | Backend 가용 상태      |
| 504        | Timeout            |

단, 실제 CPF Gateway 응답 규격과 일치하도록 작성한다.

---

# 12. Header 운영

Gateway가 추가·삭제·변환하는 Header를 정리한다.

| Header생성 주체Gateway 처리Backend 전달로그 출력 |                 |          |      |          |
| ------------------------------------ | --------------- | -------- | ---- | -------- |
| Authorization                        | Client          | 검증/전달 여부 | 정책   | 원문 출력 금지 |
| Request-ID                           | Client/Gateway  | 유지/생성    | O    | O        |
| Trace-ID                             | Gateway/Tracing | 유지/생성    | O    | O        |
| Client-IP                            | Proxy           | 정규화      | 필요 시 | 정책       |
| User 정보                              | 인증 계층           | 정책       | 정책   | Masking  |

민감 Header의 원문 Logging 금지 여부를 명시한다.

---

# 13. 요청 추적

특정 사용자 요청 문제를 찾는 대표적인 운영 절차를 제공한다.

### 운영자가 확보할 값

우선순위:

1. 발생 시간
2. Request ID
3. Trace ID
4. URL
5. HTTP Method
6. 응답 코드
7. 사용자 또는 시스템 식별값
8. Gateway Instance

이후 다음 순서로 추적한다.

```
Client Request
 ↓
Gateway Access Log
 ↓
Route ID
 ↓
Gateway Filter
 ↓
Target Service
 ↓
Backend Log
 ↓
Response

```

---

# 14. 로그 조회

## 로그 종류

최소 다음 기준으로 구분한다.

| 로그목적            |               |
| --------------- | ------------- |
| Access Log      | 요청/응답 확인      |
| Application Log | Gateway 내부 처리 |
| Error Log       | Exception 확인  |
| Audit Log       | 운영 변경 확인      |
| Security Log    | 인증/접근 관련 확인   |

---

## 로그 검색 예

```
requestId=<REQUEST_ID>
traceId=<TRACE_ID>
routeId=<ROUTE_ID>
status=500

```

단순 grep 예제뿐 아니라 중앙 로그 시스템 사용 방법이 있다면 함께 제공한다.

---

# 15. 로그에 기록하면 안 되는 정보

운영 매뉴얼에 명시적으로 작성한다.

예:

- Password
- Authorization Token 원문
- Access Token
- Refresh Token
- Session ID 원문
- 주민번호
- 계좌번호 전체
- 카드번호 전체
- Secret
- API Key
- Cookie 원문

실제 CPF Masking 정책을 연결한다.

---

# 16. Timeout 운영

Timeout은 종류별로 구분한다.

예:

- Gateway Connection Timeout
- Gateway Response Timeout
- Backend Timeout
- Load Balancer Timeout
- Client Timeout

운영자는 Timeout이라는 결과만 보고 Gateway 문제라고 판단하면 안 된다.

확인 순서:

```
Client Timeout
 ↓
LB Timeout 확인
 ↓
Gateway Timeout 확인
 ↓
Backend 응답시간 확인
 ↓
Backend 내부 처리 확인

```

---

# 17. HTTP 오류 코드 대응표

빠른 검색을 위한 별도 표를 제공한다.

| HTTP일반적 의미Gateway 확인Backend 확인 |         |                   |               |
| ------------------------------ | ------- | ----------------- | ------------- |
| 400                            | 잘못된 요청  | Filter/Validation | 요청 규격         |
| 401                            | 인증 실패   | 인증 처리             | 인증 서비스        |
| 403                            | 권한 없음   | 권한 정책             | 서비스 권한        |
| 404                            | 대상 없음   | Route             | Backend URL   |
| 429                            | 요청 제한   | Rate Limit        | -             |
| 500                            | 내부 오류   | Gateway Error     | Backend Error |
| 502                            | 연결 실패   | Target            | Backend 상태    |
| 503                            | 이용 불가   | Service Discovery | Backend 상태    |
| 504                            | Timeout | Timeout 설정        | Backend 처리시간  |

실제 응답 정책이 다르면 실제 구현을 기준으로 수정한다.

---

# 18. Backend 연결 확인

Gateway가 살아 있어도 Backend 연결이 실패할 수 있으므로 분리한다.

확인 항목:

- DNS
- Service Discovery
- Target IP
- Target Port
- TLS
- Connection Timeout
- Backend Health
- Network
- Connection Pool

운영자가 Gateway Process부터 재기동하는 식으로 접근하지 않도록 확인 순서를 제공한다.

---

# 19. Service Discovery 사용 시

사용 중인 경우 다음을 포함한다.

- Service 이름
- Instance 등록 상태
- Instance 수
- Instance 상태
- 마지막 Heartbeat
- 선택 알고리즘
- 제외된 Instance 확인 방법

예:

```
order-service
 ├─ order-01 : UP
 ├─ order-02 : UP
 └─ order-03 : DOWN

```

---

# 20. Load Balancing

Gateway가 Backend Load Balancing을 담당한다면 설명한다.

다음 항목을 명시한다.

- Load Balancing 방식
- Instance 선택 기준
- 비정상 Instance 제외 조건
- 신규 Instance 반영 시점
- Instance 종료 시 처리
- Sticky Session 사용 여부

---

# 21. Circuit Breaker

실제 기능이 존재하는 경우에만 포함한다.

운영자가 알아야 할 내용:

- 어떤 Route에 적용되는가
- Open 조건
- Open 상태 확인 방법
- Half-Open 전환
- 정상 복귀 조건
- Fallback 처리
- 수동 Reset 가능 여부

---

# 22. Retry

Retry 기능이 존재한다면 매우 주의해서 작성한다.

| 항목설명      |             |
| --------- | ----------- |
| Retry 대상  | 어떤 오류인가     |
| 최대 횟수     | 몇 회인가       |
| Interval  | 대기 시간       |
| 대상 Method | GET/POST 등  |
| 제외 API    | Retry 금지 대상 |

특히 상태 변경 API에 자동 Retry가 적용되는지 반드시 명확하게 한다.

---

# 23. Rate Limit

지원한다면 다음을 작성한다.

- 적용 대상
- 기준 단위
- 제한 값
- 초과 시 응답
- 현재 사용량 확인 방법
- 설정 변경 방법
- 변경 이력
- 특정 Client 예외 처리 방식

---

# 24. CORS

운영 중 자주 발생하는 문제이므로 별도 설명을 권장한다.

확인 항목:

- Allowed Origin
- Allowed Method
- Allowed Header
- Exposed Header
- Credential 설정
- Preflight OPTIONS

브라우저 문제와 Backend 문제를 구분하는 방법을 설명한다.

---

# 25. TLS / HTTPS

Gateway가 TLS Endpoint라면 포함한다.

운영자가 확인할 수 있어야 하는 값:

- 인증서 Subject
- 발급자
- 적용 Host
- 유효 기간
- 만료 예정일
- 인증서 Chain
- 적용 위치

인증서 교체 절차도 실제 운영 범위에 있다면 작성한다.

---

# 26. Gateway 시작 및 종료

실제 배포 방식에 따라 명령을 제공한다.

예:

```
<실제 Gateway 시작 명령>

```

```
<실제 Gateway 종료 명령>

```

다음 항목을 함께 적는다.

- 실행 사용자
- 실행 위치
- 필요한 환경 변수
- 성공 확인 방법
- 실패 확인 방법

---

# 27. 재기동 절차

운영자가 가장 자주 찾는 절차 중 하나다.

다중 Instance라면 전체 동시 재기동을 기본 절차로 제시하지 않는다.

예:

```
1. gateway-01 Traffic 제외
2. Active Request 확인
3. gateway-01 종료
4. gateway-01 시작
5. Health 확인
6. Route 확인
7. Traffic 복귀
8. 다음 Instance 진행

```

실제 Load Balancer 제어 방식과 Gateway 운영 구조에 맞춰 작성한다.

---

# 28. 배포 후 확인

Gateway 신규 Version 배포 후 최소 확인 항목을 제공한다.

```
□ Process 정상
□ Health 정상
□ Version 정상
□ Commit SHA 정상
□ Configuration Version 정상
□ Route 정상
□ Backend 연결 정상
□ 인증 API 정상
□ 대표 GET API 정상
□ 대표 POST API 정상
□ 4xx 처리 정상
□ 로그 오류 없음
□ 전체 Instance Version 동일

```

---

# 29. 설정 변경 후 확인

다음 체크리스트를 제공한다.

```
□ 변경 값이 저장되었는가
□ 변경한 사용자와 시간이 기록되었는가
□ Runtime에 적용되었는가
□ 전체 Instance에 동일하게 적용되었는가
□ 기존 Route가 영향을 받지 않았는가
□ 대표 API가 정상 동작하는가
□ 로그에 신규 오류가 발생하지 않는가

```

---

# 30. 원복 절차

변경 기능을 설명했다면 반드시 원복도 설명한다.

다음 정보를 남긴다.

- 변경 전 값
- 변경 후 값
- 변경 시간
- 변경자
- 원복 방법
- 원복 완료 확인 방법

잘못된 설정을 삭제해서 해결하는 식의 위험한 절차보다 이전 검증 설정으로 복원하는 방식을 설명한다.

---

# 31. 운영 변경 이력

Gateway의 운영 변경을 조회할 수 있다면 다음 항목을 제공한다.

| 항목설명  |                |
| ----- | -------------- |
| 변경 시간 | 적용 시각          |
| 변경자   | 수행 사용자         |
| 대상    | Route/Config 등 |
| 이전 값  | 변경 이전          |
| 변경 값  | 변경 이후          |
| 사유    | 변경 이유          |
| 결과    | 성공/실패          |

---

# 32. 권한

운영 기능별 권한 Matrix를 제공한다.

| 기능조회 운영자변경 운영자관리자 |   |    |   |
| ----------------- | - | -- | - |
| 상태 조회             | O | O  | O |
| Route 조회          | O | O  | O |
| Route 변경          | X | O  | O |
| 설정 변경             | X | 제한 | O |
| 변경 이력             | O | O  | O |

실제 CPF 권한 모델과 일치시킨다.

---

# 33. 운영자 위험 조작

위험도가 높은 기능은 일반 기능과 분리해 표시한다.

예:

- Route 전체 비활성화
- Default Route 변경
- 인증 Filter 변경
- 전체 Traffic 제한
- Backend Target 변경
- 공통 Timeout 변경
- 전체 Gateway 재기동

각 기능에는 다음을 표시한다.

```
영향 범위
필요 권한
사전 확인
실행 방법
결과 확인
원복 방법

```

---

# 34. 문제 상황별 빠른 대응

운영자가 가장 많이 활용하게 될 부분이다.

## Gateway 접속 불가

```
1. Gateway 주소 확인
2. Load Balancer 확인
3. Instance Health 확인
4. Process 확인
5. Port 확인
6. Gateway Log 확인

```

---

## 일부 API만 404

```
1. 요청 URL 확인
2. Route 검색
3. Route 활성 상태 확인
4. Predicate 확인
5. Route 우선순위 확인
6. Target Service 확인

```

---

## 전체 API 401

```
1. 인증 Header 확인
2. Gateway 인증 Filter 확인
3. 인증 서비스 상태 확인
4. 인증 설정 확인
5. 최근 설정 변경 확인

```

---

## 일부 API만 500

```
1. Request ID 확보
2. Gateway 로그 조회
3. Route ID 확인
4. Backend Service 확인
5. Backend 로그 연결 조회

```

---

## 502 발생

```
1. Route Target 확인
2. DNS/Service Discovery 확인
3. Backend Instance 확인
4. Target Port 확인
5. 연결 오류 로그 확인

```

---

## 504 발생

```
1. 요청 처리시간 확인
2. Gateway Timeout 확인
3. Backend 응답시간 확인
4. 동일 API 반복 여부 확인
5. Backend 로그 확인

```

---

## 특정 Instance에서만 오류

```
1. 요청 처리 Instance 확인
2. Instance별 Version 비교
3. Config Version 비교
4. Route 상태 비교
5. 환경 변수 비교
6. 해당 Instance 로그 확인

```

---

# 35. 증상 → 확인 위치 표

매뉴얼 앞부분 또는 부록에 반드시 넣는 것을 권장한다.

| 증상첫 확인 위치다음 확인   |             |                  |
| ---------------- | ----------- | ---------------- |
| Gateway 전체 접속 불가 | Health      | LB / Process     |
| 일부 API 404       | Route       | Backend Path     |
| 401              | 인증 로그       | 인증 시스템           |
| 403              | 권한 로그       | 권한 설정            |
| 429              | Rate Limit  | Client 요청량       |
| 500              | Trace Log   | Backend          |
| 502              | Target      | Backend Instance |
| 503              | Instance 상태 | Discovery        |
| 504              | Timeout     | Backend 처리시간     |
| 특정 Instance 오류   | Instance 현황 | Version/Config   |

---

# 36. 운영 명령어 Quick Reference

운영자가 문서를 처음부터 읽지 않아도 명령을 찾을 수 있도록 별도 Summary를 만든다.

예:

| 목적명령/API권한결과 |             |    |             |
| ------------ | ----------- | -- | ----------- |
| Health       | `<command>` | 조회 | UP/DOWN     |
| Version      | `<command>` | 조회 | Version     |
| Route 조회     | `<command>` | 조회 | Route 목록    |
| Instance 조회  | `<command>` | 조회 | Instance 목록 |
| Log 조회       | `<command>` | 조회 | Log         |
| 설정 조회        | `<command>` | 조회 | 현재 설정       |

본문의 상세 절차로 링크를 연결한다.

---

# 37. 주요 URL Quick Reference

| 목적URL      |         |
| ---------- | ------- |
| Gateway    | `<url>` |
| Gateway 관리 | `<url>` |
| Health     | `<url>` |
| ADM        | `<url>` |
| API 문서     | `<url>` |
| 로그 조회      | `<url>` |
| 모니터링       | `<url>` |

환경별 값이 다르면 DEV/TEST/PROD를 분리한다.

---

# 38. 운영 API 목록

Gateway 운영 API가 있다면 전체 목록을 제공한다.

| APIMethod설명권한변경 여부 |      |          |    |   |
| ------------------ | ---- | -------- | -- | - |
| `/...`             | GET  | 상태 조회    | 조회 | X |
| `/...`             | GET  | Route 조회 | 조회 | X |
| `/...`             | POST | Route 변경 | 변경 | O |

API 이름만 적지 말고 대표 Request/Response와 실패 응답을 본문에 제공한다.

---

# 39. 운영 화면 설명 작성 규칙

각 화면 설명은 동일한 형식으로 작성한다.

### 화면명

**목적**

해당 화면으로 무엇을 확인하는지 설명한다.

**접근 경로**

```
ADM > Gateway > Route 관리

```

**필요 권한**

실제 권한 명칭을 표시한다.

**검색 조건**

| 항목설명     |     |
| -------- | --- |
| Route ID | ... |
| Path     | ... |
| 상태       | ... |

**조회 결과**

각 Column의 의미를 설명한다.

**주요 조작**

버튼 또는 Action의 실제 동작을 설명한다.

**주의 사항**

운영상 중요한 제한을 작성한다.

---

# 40. Runtime 상태와 설정 상태 구분

운영자가 가장 혼동하기 쉬운 부분이므로 반드시 구분한다.

예:

```
DB에 저장된 Route
        ↓
Gateway가 읽은 Route
        ↓
현재 Runtime에서 Active 상태인 Route

```

DB 값이 변경됐다는 사실만으로 Runtime 적용 완료라고 판단하지 않는다.

화면이나 API가 제공된다면 다음 값을 구분해서 보여준다.

- Stored Configuration
- Effective Configuration
- Runtime Configuration

---

# 41. 다중 Instance 정합성

Gateway가 여러 Instance라면 반드시 별도 운영 절차가 필요하다.

비교 대상:

| 항목             |
| -------------- |
| Version        |
| Commit SHA     |
| Config Version |
| Route Version  |
| 시작 시간          |
| Profile        |
| Active Route 수 |

Instance별 값이 다른 경우의 대응 절차를 제공한다.

---

# 42. Monitoring

Gateway에서 제공되는 실제 Metric을 정리한다.

예:

- Request Count
- Success Count
- 4xx Count
- 5xx Count
- Response Time
- Active Connection
- Timeout Count
- Route별 요청량

Metric 이름 자체와 운영 의미를 함께 설명한다.

---

# 43. 알림이 존재하는 경우

| 알림의미첫 확인         |                |              |
| ---------------- | -------------- | ------------ |
| Gateway Down     | Instance 응답 없음 | Health       |
| 5xx 증가           | 오류 증가          | Route별 오류    |
| Response Time 증가 | 응답 지연          | Backend      |
| Timeout 증가       | Timeout 증가     | Backend 처리시간 |

실제 Monitoring/Alert 설정이 존재할 때만 문서화한다.

---

# 44. 작업별 사전/사후 확인표

## Route 변경

**사전**

```
□ 대상 Route 확인
□ 영향 API 확인
□ 변경 전 값 확보
□ 변경 권한 확인
□ 원복 방법 확인

```

**사후**

```
□ Runtime 반영 확인
□ 전체 Instance 확인
□ Test 호출 성공
□ Backend 전달 확인
□ 로그 오류 확인

```

---

# 45. 하지 말아야 할 운영 방식

명확한 금지 사항을 둔다.

예:

- 오류 원인을 확인하지 않고 모든 Gateway를 동시에 재기동하지 않는다.
- 운영 환경에서 검증되지 않은 Route를 즉시 등록하지 않는다.
- 인증 Token을 로그나 화면 캡처에 남기지 않는다.
- 한 Instance의 상태만 보고 전체 Gateway 상태를 판단하지 않는다.
- 설정 저장 성공만으로 Runtime 반영 완료라고 판단하지 않는다.
- Backend 문제를 확인하지 않은 상태에서 Gateway 설정을 임의 변경하지 않는다.
- HTTP 500을 모두 Gateway 오류로 판단하지 않는다.
- 운영자가 직접 DB 값을 임의 수정하는 방식을 정상 운영 절차로 사용하지 않는다.

---

# 46. FAQ

실제 운영 중 반복적으로 발생할 질문을 모은다.

예:

### Q. Gateway는 정상인데 API가 호출되지 않는다.

Route, Backend 상태, 인증 상태 순으로 확인한다.

### Q. Route를 변경했는데 이전 경로로 전달된다.

Runtime Route와 Instance별 Config Version을 확인한다.

### Q. 한 사용자만 401이 발생한다.

전체 인증 시스템 문제보다 해당 요청의 Token 및 인증 정보를 우선 확인한다.

### Q. 504면 Gateway 문제인가?

반드시 그렇지는 않다. Gateway Timeout과 Backend 처리시간을 함께 확인한다.

---

# 47. 용어집

Gateway 관련 용어를 짧게 정리한다.

| 용어설명       |                       |
| ---------- | --------------------- |
| Route      | 요청 전달 규칙              |
| Predicate  | Route 선택 조건           |
| Filter     | 요청/응답 처리 규칙           |
| Target     | 실제 전달 대상              |
| Instance   | 실행 중인 Gateway Process |
| Request ID | 요청 식별값                |
| Trace ID   | 시스템 간 요청 추적값          |
| 4xx        | 요청/인증 등 Client 계열 응답  |
| 5xx        | 서버 처리 계열 응답           |

---

# 48. 운영 시나리오

단순 기능 설명 외에 실제 시나리오를 최소 다음 수준으로 제공한다.

1. Gateway 전체 정상 여부 확인
2. 특정 API Route 확인
3. 특정 요청 Trace 추적
4. 401 원인 확인
5. 403 원인 확인
6. 404 Route 오류 확인
7. 500 Backend 오류 확인
8. 502 연결 실패 확인
9. 504 Timeout 확인
10. 특정 Gateway Instance 이상 확인
11. Route 변경 및 적용 확인
12. Route 원복
13. Gateway Instance 순차 재기동
14. 신규 Gateway Version 배포 후 확인
15. Instance 간 Configuration 불일치 확인

각 시나리오는 다음 구조로 작성한다.

```
상황
→ 준비 정보
→ 확인 순서
→ 실행 명령/화면
→ 정상 결과
→ 비정상 결과
→ 후속 조치

```

---

# 49. 문서 앞부분 Quick Start

전체 매뉴얼이 길어지므로 앞부분에는 최소한 다음 내용을 1\~2페이지 수준으로 제공한다.

## Gateway 이상 발생 시 기본 순서

```
1. 발생 시간 확보
2. Request ID / Trace ID 확보
3. HTTP Status 확인
4. Gateway Instance 확인
5. Route 확인
6. Gateway Log 확인
7. Target Service 확인
8. Backend Log 확인

```

## 기본 상태 확인

```
Health
→ Instance
→ Route
→ Backend

```

## 오류 코드 빠른 판단

```
401 → 인증
403 → 권한
404 → Route/Path
429 → 요청 제한
500 → Gateway/Backend Error
502 → Backend 연결
503 → Backend 가용성
504 → Timeout

```

---

# 50. 문서의 각 절차에 반드시 포함할 정보

모든 운영 절차는 가능하면 다음 항목을 빠짐없이 갖춘다.

| 항목필수 내용   |                 |
| --------- | --------------- |
| 목적        | 왜 수행하는가         |
| 대상 환경     | DEV/TEST/PROD   |
| 필요 권한     | 어떤 운영 권한인가      |
| 사전 조건     | 무엇이 준비되어야 하는가   |
| 실행 위치     | ADM/API/Host 등  |
| 명령 또는 화면  | 실제 실행 방법        |
| Parameter | 입력값             |
| 정상 결과     | 성공 판단 기준        |
| 실패 결과     | 실패 판단 기준        |
| 후속 확인     | 무엇을 추가 확인하는가    |
| 영향 범위     | 실행 시 영향         |
| 원복        | 변경 작업인 경우 원복 방법 |
| 관련 로그     | 어느 로그에서 확인하는가   |

---

# 51. 최종 문서 작성 시 반드시 실제 CPF 구현에서 추출해야 하는 정보

문서를 임의로 작성하지 않고 실제 Source·Config·API·Frontend를 기준으로 다음을 조사해 채운다.

### Source

- Gateway Main Application
- Route 처리 구현
- Filter 구현
- 인증 연계
- Exception Handler
- Logging
- Trace 처리
- Timeout
- Retry
- Circuit Breaker
- Rate Limit

### Config

- application 설정
- Profile 설정
- Route 설정
- Timeout
- CORS
- TLS
- Logging
- Service Discovery

### API

- Health
- Management
- Route
- Instance
- Config
- Audit

### Frontend

- ADM Gateway 메뉴
- 조회 화면
- 상세 화면
- 변경 화면
- 오류 화면
- 권한 처리

### 운영 Script

- Start
- Stop
- Status
- Deploy
- Health Check

---

# 52. 개발자 가이드와 Gateway 운영 매뉴얼의 경계

두 문서는 중복 작성하지 않는다.

### Gateway 개발자 가이드

개발자가 알아야 하는 내용:

- Gateway Architecture
- Route 개발
- Predicate 개발
- Filter 구현
- 확장 SPI
- 인증 연계 개발
- 설정 구조
- Test
- API 개발
- 코드 예제

### Gateway 운영 매뉴얼

운영자가 알아야 하는 내용:

- Gateway 상태 확인
- Route 조회
- 요청 추적
- 오류 분석
- Instance 운영
- 설정 적용 확인
- 시작/종료/재기동
- 변경 및 원복
- Monitoring
- 권한과 운영 주의사항

즉,

**개발자 가이드 = 어떻게 구현하는가**

**운영 매뉴얼 = 실행 중인 Gateway를 어떻게 확인하고 조작하고 문제를 추적하는가**

로 경계를 유지한다.

---

# 53. 최종 품질 기준

Gateway 운영 매뉴얼 완성 후 다음 질문을 실제 문서만 보고 답할 수 있어야 한다.

```
□ Gateway 주소는 어디인가?
□ Gateway가 살아 있는지 어떻게 확인하는가?
□ 전체 Instance는 어떻게 확인하는가?
□ 특정 요청을 처리한 Instance를 어떻게 찾는가?
□ 특정 URL의 Route를 어떻게 찾는가?
□ Backend Target을 어떻게 확인하는가?
□ Route 변경이 Runtime에 적용됐는지 어떻게 확인하는가?
□ Request ID로 요청을 어떻게 추적하는가?
□ 401과 403을 어떻게 구분하는가?
□ 404가 Gateway Route 문제인지 어떻게 확인하는가?
□ 500이 Gateway인지 Backend인지 어떻게 찾는가?
□ 502가 발생하면 어디부터 확인하는가?
□ 504 발생 시 어떤 Timeout을 확인하는가?
□ 특정 Instance만 설정이 다른지 어떻게 확인하는가?
□ Gateway를 어떻게 시작하고 종료하는가?
□ 다중 Instance를 어떻게 순차 재기동하는가?
□ Route 변경 전 무엇을 확인하는가?
□ 잘못 변경한 Route를 어떻게 원복하는가?
□ 운영 변경 이력은 어디서 확인하는가?
□ 민감정보가 로그에 남지 않는지 무엇을 확인하는가?

```

하나라도 문서에서 찾을 수 없다면 해당 항목을 보완한다.

---

# 54. 권장 최종 목차

```
1. 문서 소개
2. Gateway 개요
3. Gateway 구성
4. 요청 처리 흐름
5. 운영 환경 및 접속 정보
6. Gateway 운영 메뉴
7. Gateway 현황
8. Instance 현황
9. Health 확인
10. Route 조회
11. Route 상세
12. Route 우선순위
13. Route 변경
14. Configuration 적용
15. 인증 및 인가
16. Header 처리
17. Request / Trace 추적
18. Gateway 로그
19. Backend 연결 확인
20. Service Discovery
21. Load Balancing
22. Timeout
23. Retry
24. Circuit Breaker
25. Rate Limit
26. CORS
27. TLS / HTTPS
28. Monitoring
29. 오류 조회
30. HTTP 오류 코드별 대응
31. Gateway 시작
32. Gateway 종료
33. Gateway 재기동
34. 다중 Instance 운영
35. 배포 후 확인
36. 설정 변경 후 확인
37. 원복
38. 변경 이력
39. 운영 권한
40. 위험 작업
41. 문제 상황별 대응
42. 운영 시나리오
43. FAQ
44. 명령어 Quick Reference
45. 주요 URL Quick Reference
46. 운영 API Reference
47. 점검 Checklist
48. 용어집
49. 관련 문서
50. 변경 이력

```

이 정도 수준으로 작성하면 **Gateway 운영 매뉴얼을 Batch 운영 매뉴얼과 별도 문서로 두는 의미가 충분하다.**

특히 Gateway는 `Route·인증·외부 요청·Backend 연결·Timeout·HTTP 오류·다중 Instance·Trace`가 운영의 핵심이므로 일반 운영자 매뉴얼 안에 짧게 넣기보다 독립 문서로 관리하는 편이 찾기 쉽다.