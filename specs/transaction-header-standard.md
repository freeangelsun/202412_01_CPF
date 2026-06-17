# CPF 거래 헤더 표준

CPF는 모든 업무 API에서 동일한 거래 헤더를 사용합니다. 필수 헤더는 최소 운영 추적을 보장하고, 표준 선택 헤더는 클라이언트 버전, 호출자, 멱등성, locale 정보를 가능한 범위에서 함께 주고받기 위한 규격입니다.

## 필수 헤더

| 헤더 | 설명 | 예시 |
| --- | --- | --- |
| `X-Request-Type` | 요청 유형 | `INQUIRY`, `CREATE`, `UPDATE`, `DELETE`, `PROCESS` |
| `X-Original-Channel-Code` | 최초 유입 채널 | `WEB`, `MOB`, `BATCH` |
| `X-Channel-Code` | 현재 처리 채널 | `WEB`, `MOB`, `API` |

## CPF가 생성하거나 전파하는 추적 헤더

| 헤더 | 설명 |
| --- | --- |
| `X-Transaction-Id` | 전역 거래 ID입니다. 없으면 CPF가 생성합니다. |
| `X-Trace-Id` | 분산 추적 ID입니다. 없으면 CPF가 생성합니다. |
| `X-Span-Id` | 현재 호출 span ID입니다. 응답 헤더로 내려갑니다. |
| `X-Parent-Span-Id` | 상위 호출 span ID입니다. 서비스 간 호출 시 전파합니다. |

## 표준 선택 헤더

| 헤더 | 설명 | 로그 컬럼 |
| --- | --- | --- |
| `X-Api-Version` | 호출 API 버전 | `API_VERSION` |
| `X-Client-App-Id` | 클라이언트 앱 또는 제휴 시스템 ID | `CLIENT_APP_ID` |
| `X-Client-Version` | 클라이언트 앱/SDK 버전 | `CLIENT_VERSION` |
| `X-Caller-Service` | 호출한 내부 서비스 또는 배치명 | `CALLER_SERVICE` |
| `X-Caller-Instance-Id` | 호출 인스턴스, WAS, pod ID | `CALLER_INSTANCE_ID` |
| `X-Correlation-Id` | 외부 시스템과 대조하는 상관관계 ID | `CORRELATION_ID` |
| `X-Idempotency-Key` | 중복 요청 방지를 위한 멱등키 | `IDEMPOTENCY_KEY` |
| `X-Locale` | 클라이언트 언어/국가 코드 | `LOCALE` |
| `X-Timezone` | 클라이언트 시간대 | `TIMEZONE` |

## 업무 추적 헤더

| 헤더 | 설명 |
| --- | --- |
| `X-Member-No` | 회원 번호 |
| `X-Customer-No` | 고객 번호 |
| `X-User-Id` | 사용자 또는 운영자 ID |
| `X-Screen-Id` | 화면 또는 메뉴 ID |
| `X-Device-Id` | 디바이스 ID |
| `X-Client-Request-Time` | 클라이언트 요청 생성 시각 |
| `X-Client-IP` | 클라이언트 IP |

## 적용 기준

- Controller 요청은 `TransactionHeaderValidationInterceptor`에서 필수 헤더와 `@FpsTransaction` 메타를 검증합니다.
- `TransactionContextFilter`는 모든 표준 헤더를 `TransactionHeader`에 수집합니다.
- `TransactionContext.propagationHeaders()`는 외부 API 호출 또는 내부 서비스 호출 시 전파할 헤더 맵을 제공합니다.
- `LoggingAspect`와 `TransactionLogMapper.xml`은 표준 선택 헤더를 `pfw_transaction_log`에 저장합니다.
- Swagger/OpenAPI에는 모든 표준 헤더가 공통 header parameter로 노출됩니다.

## 운영 권장

- 모바일/웹/제휴 채널은 배포 버전 추적을 위해 `X-Client-App-Id`, `X-Client-Version`을 항상 전달합니다.
- 내부 서비스 호출은 장애 추적을 위해 `X-Caller-Service`, `X-Caller-Instance-Id`를 전달합니다.
- 결제, 가입, 주문처럼 중복 처리 위험이 있는 API는 `X-Idempotency-Key`를 업무 멱등 처리와 연결합니다.
- 외부 기관 연계는 기관이 제공하는 추적 ID를 `X-Correlation-Id`에 보관합니다.
