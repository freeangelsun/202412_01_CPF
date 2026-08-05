# Gateway Trust·Resilience·Rate Limit 운영 Runbook

## 기준

- Baseline: `09dd686c5ae0826594b9c5e1f871d95d95d3ce1c`
- Owner Runtime: `cpf-gateway`
- Data Plane Consumer: `CpfScgPrimaryHandler`
- 정책 적용 Consumer: `CpfGatewayRuntimeApplier`
- Rate Limit Counter SPI: `CpfGatewayRateLimitCounterPort`
- 정식 운영 Counter: JDBC 또는 고객사 제공 distributed Provider

## 1. Trust 경계

Gateway는 외부 요청의 `Forwarded`, `X-Forwarded-*`, 내부 Principal·Authority Header를 신뢰하지 않는다. 요청 진입 시 외부 전달값을 제거하고 Gateway가 인증·권한 판정 후 다시 생성한다. Trusted Header가 여러 값으로 전달되면 모호한 첫 값 선택을 하지 않고 요청을 거부한다.

운영 확인:

1. 인증 실패와 권한 실패가 각각 401·403으로 분리되는지 확인한다.
2. `X-Forwarded-For`를 외부에서 주입해도 Backend에 원문이 전달되지 않는지 확인한다.
3. 동일 trusted context Header가 복수 값이면 fail-closed 되는지 확인한다.
4. Target URI가 TLS·Port·CIDR/DNS allowlist를 벗어나면 Proxy 이전에 차단되는지 확인한다.
5. Audit·Ledger·Trace에는 raw Credential과 원문 client/tenant 식별자가 남지 않는지 확인한다.

## 2. Resilience·UNKNOWN

Retry는 replay-safe Method 또는 Idempotency Key가 있는 요청으로 제한한다. 연결 확립 전 실패는 definitive failure이며 UNKNOWN으로 저장하지 않는다. 요청 전송 이후 응답 유실·timeout은 UNKNOWN 후보로 보존하고 Ledger·Reconcile 경로에서 최종 상태를 확인한다.

운영 확인:

1. connect failure가 UNKNOWN 증가로 기록되지 않는지 확인한다.
2. POST/PUT/PATCH가 Idempotency Key 없이 자동 재시도되지 않는지 확인한다.
3. 다중 Target failover 시 attempt 순서·Target·latency·결과가 Ledger에 연결되는지 확인한다.
4. Ledger/Audit 저장 실패 시 durable recovery spool이 생성되고 재기동 후 drain 되는지 확인한다.
5. Circuit OPEN·HALF_OPEN·CLOSED와 Target Health가 정책 Version과 함께 조회되는지 확인한다.

## 3. Rate Limit 정책

정책 Scope는 API, CLIENT, CHANNEL, TENANT다. 한 요청에 여러 Scope가 적용되면 Counter Provider가 하나의 원자 Transaction으로 판정해야 한다. 앞 Scope를 먼저 소비한 뒤 뒤 Scope에서 거부하는 부분 소비는 금지한다.

운영 정책 입력:

- `quota`: Window 기본 허용량
- `burst`: Window 추가 순간 허용량
- `windowMillis`: 1초 이상 24시간 이하
- `abuseThreshold`: 연속 거부 누적 임계치
- `blockMillis`: Abuse 차단 기간
- `failClosed`: Counter 장애 시 거부 여부
- `version`: 단조 증가 정책 Version

보안 규칙:

- Counter 중복 요청 Key는 Gateway가 생성한 operation/transaction ID를 사용한다.
- Client가 전달한 거래 ID를 dedupe Key로 신뢰하지 않는다.
- Counter key·운영 상태에는 clientId·tenantId 원문 대신 SHA-256 기반 opaque ID를 사용한다.
- 동일 Version의 다른 Payload와 stale Version은 409 성격 충돌로 거부한다.

## 4. 429 응답

Rate Limit 거부 응답은 다음을 제공한다.

- HTTP 429
- `Retry-After`
- Rate Limit Policy opaque ID
- 남은 허용량
- Reset 시각
- 제한 Scope
- Counter degraded 여부

Response allowlist가 활성화되어도 실제 Gateway Rate Limit 거부 응답의 표준 Header는 보존한다. 정상 Upstream 응답이 같은 Header를 위조하면 제거한다.

## 5. 장애 Severity·Routing

| 조건 | Severity | Routing | Dedup Key |
|---|---|---|---|
| distributed Counter DOWN + failClosed | Critical | Gateway On-call·DB On-call | environment+counter-provider+policy-version |
| distributed Counter DOWN + explicit bypass | Critical | Gateway On-call·Security | environment+counter-provider+bypass |
| 429 비율 급증 | High | Gateway On-call·업무 Owner | route+tenant-opaque+window |
| Abuse block 급증 | High | Security SOC·Gateway On-call | route+client-opaque+window |
| stale/same-version conflict | Medium | Platform Control Plane | policy-type+version |
| Ledger/Recovery spool backlog | High | Gateway On-call·DB On-call | instance+spool-type |
| trusted header spoof 시도 | High | Security SOC | source-network+header-family |

## 6. 대응 절차

1. Counter `Health.ready`, `distributed`, active counter 수, Policy Version을 확인한다.
2. 실패가 특정 Scope/Route인지 전체 Provider인지 분리한다.
3. Policy Payload Hash와 Version 충돌을 확인한다.
4. failClosed를 임의 해제하지 않는다. 우회가 필요하면 승인·사유·만료·감사 경로를 사용한다.
5. DB Provider 장애이면 Transaction·row lock·unique request journal·CAS conflict를 확인한다.
6. 복구 후 동일 operation ID로 재실행하여 duplicate 결과가 동일한지 확인한다.
7. 429·abuse·counter error Metric이 정상 범위로 회복됐는지 확인한다.

## 7. 종결 기준

- Counter Health가 READY이고 운영 환경에서 distributed=true다.
- 동일 request ID 재실행 결과가 결정적이다.
- 복합 Scope 거부 시 다른 Scope 사용량이 변하지 않는다.
- stale/same-version conflict가 재현되지 않는다.
- 429 응답에 Retry-After가 있고 raw 식별자가 없다.
- Ledger/Audit recovery backlog가 0이다.
- 대응 사유·승인·결과·transactionId가 Append-only Audit에 연결됐다.
