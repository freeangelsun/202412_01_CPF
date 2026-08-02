# CPF Docker 확장 연동 서비스 사용 가이드

상위 메뉴: [Docker 문서](README.md)

> **주 독자**: 외부 연계 개발자, 보안 담당자, 통합시험·장애시험 검수자
> **완료 결과**: WireMock·SFTP·Vault·Keycloak·Toxiproxy·OpenTelemetry Collector를 Product Consumer와 연결해 정상·오류·보안·정상화 시나리오를 수행한다.
> **Source 기준**: `freeangelsun/202412_01_CPF`, `master`, `3b600702502e53877e30cbac594987b371e2186b`

## 1. 공통 변수

```powershell
$root='C:\dev\Docker\CPF'
$secretRoot='C:\dev\Docker\Secrets'
$envFile=Join-Path $secretRoot 'cpf-runtime.env'
```

## 2. WireMock

### 용도

```text
정상 응답
4xx·5xx
지연·Timeout
Connection Reset
잘못된 Header·Schema
중복·순서 변경 응답
```

### WireMock 시작

```powershell
docker compose --env-file $envFile -f (Join-Path $root 'compose.integration.yml') up -d wiremock
```

### Product 검증

- Connect·Read·Total Timeout
- Retry 대상 Failure Class
- Circuit Breaker·Bulkhead
- Idempotency·Attempt Ledger
- 응답 유실 후 `UNKNOWN_RESULT`
- Schema Validation
- Trace·Audit

Mock 성공은 실제 외부기관 계약 검증을 대신하지 않는다.

## 3. SFTP

### SFTP 시작

```powershell
docker compose --env-file $envFile -f (Join-Path $root 'compose.integration.yml') up -d sftp
```

### SFTP 검증

- 인증 실패·권한
- Put·Get·내용 비교
- 임시 파일·Atomic Rename
- Checksum
- 중단·Resume
- 중복 파일·동일 이름·Version
- 파일 크기·동시 전송
- Retention·격리·Audit

Password 원문을 명령이나 Log에 출력하지 않는다.

## 4. Vault

### Vault 시작

```powershell
docker compose --env-file $envFile -f (Join-Path $root 'compose.integration.yml') up -d vault
```

### Vault 검증

- Secret Reference 조회
- Provider 없음·Token 만료
- 잘못된 권한
- Secret Version·Rotation
- Network Loss
- Readiness Fail-closed
- Audit·Masking

Dev Fixture는 운영 Vault의 Seal·HA·Policy·Backup 구성을 대신하지 않는다.

## 5. Keycloak

### Keycloak 시작

```powershell
docker compose --env-file $envFile -f (Join-Path $root 'compose.integration.yml') up -d keycloak
```

### Keycloak 검증

- Realm Import
- 사용자 로그인·실패·잠금
- Client Credentials
- Issuer·Audience·Scope·Role
- Token Expiry·Refresh·Key Rotation
- Session 강제 종료
- Clock Skew
- Backend Permission·Data Scope

Fixture ID·Password는 실제 `cpf-test-realm.json`과 Secret 파일을 확인한다. 문서에 Secret 값을 기록하지 않는다.

## 6. Toxiproxy

### Toxiproxy 시작

```powershell
docker compose --env-file $envFile -f (Join-Path $root 'compose.tooling.yml') up -d toxiproxy
```

### 대상

```text
DB / Redis / Kafka
REST / SFTP / Vault / Keycloak
OTel Export
```

### Fault

```text
latency / timeout / bandwidth
connection reset / half-open / downstream close
```

한 번에 하나의 Fault를 적용하고 Proxy 이름·대상·시작·제거 시각을 기록한다. Fault를 제거하지 않은 상태에서 다른 시험을 계속하지 않는다.

## 7. OpenTelemetry Collector

### OpenTelemetry Collector 시작

```powershell
docker compose --env-file $envFile -f (Join-Path $root 'compose.tooling.yml') up -d otel-collector
```

### OpenTelemetry Collector 검증

- OTLP gRPC·HTTP 수신
- Trace·Metric Export
- Export 실패 Backpressure
- Queue·Drop·Retry
- Service·Environment·Instance Resource Attribute
- `requestId`·`traceId`·`operationId` 상관관계
- 개인정보·Message Payload·Secret Attribute 금지

## 8. 정상화 순서

1. Fault를 제거한다.
2. Product Connection Pool·Consumer·Exporter가 다시 연결되는지 확인한다.
3. Backlog·Retry·DLQ·UNKNOWN_RESULT를 대사한다.
4. Runtime의 Readiness와 실제 업무 Probe를 확인한다.
5. 이번 시험에서 시작한 Fixture만 중지한다.
6. Running CPF Container와 Volume 보존을 확인한다.

## 9. 완료 판정

- Fixture Health와 Product Contract가 모두 성공
- 오류가 예상 Failure Class로 분류
- Retry가 Deadline·멱등성 범위 안에서 수행
- 결과 불명·부분 실패 대사 가능
- Log·Metric·Trace·Audit 연결
- Secret 원문 노출 없음
- Fault 제거와 정상화 결과 기록
