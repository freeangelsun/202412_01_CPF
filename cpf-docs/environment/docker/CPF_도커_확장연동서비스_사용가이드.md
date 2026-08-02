# CPF Docker 확장 연동 서비스 사용 가이드

상위 메뉴: [Docker 문서](README.md)

## 1. WireMock

용도:

```text
정상 응답
503
지연
Connection Reset
Schema 오류
```

Product Client의 Timeout·Retry·Circuit Breaker·UNKNOWN_RESULT를 검증한다. Mock 성공만으로 실제 기관 연계를 완료 처리하지 않는다.

## 2. SFTP

- 실제 Put·Get·내용 비교를 수행한다.
- 임시 파일·Checksum·Atomic Rename·Resume를 Product Test에서 확인한다.
- Password는 `sftp-password.txt`를 사용하고 Log에 출력하지 않는다.

## 3. Vault

- Dev Fixture이며 운영 Vault 보안 구성을 대신하지 않는다.
- Provider 없음·Token 만료·Secret Rotation·Network Loss를 시험한다.
- Root Token 원문을 Evidence에 기록하지 않는다.

## 4. Keycloak

Fixture:

```text
Realm        : cpf-test
Admin        : cpf-admin
Test User    : cpf-reviewer
Service      : cpf-service-local
```

Password Grant와 Client Credentials를 확인한다. Required Action·Temporary Password·Brute Force 상태가 남지 않았는지 점검한다.

## 5. Toxiproxy

대상:

```text
DB
Redis
Kafka/RabbitMQ/JMS
REST/SFTP
OTel Export
```

Fault 유형:

```text
latency
timeout
bandwidth
connection reset
half-open
```

한 번에 하나의 Fault를 적용하고 Proxy Config와 정상화 명령을 기록한다.

## 6. OpenTelemetry Collector

Trace·Metric 수신과 Export 실패 Backpressure를 확인한다. 개인정보·Message Payload·Secret이 Attribute에 포함되지 않는지 점검한다.

## 7. 정상화

1. Fault를 제거한다.
2. Connection Pool과 Consumer가 회복됐는지 확인한다.
3. Backlog·Retry·DLQ·UNKNOWN_RESULT를 대사한다.
4. Service를 중지한다.
5. Running CPF Container 0을 확인한다.
