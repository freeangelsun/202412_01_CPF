# CPF Docker 확장 연동 서비스 사용 가이드

> **주 독자**: 외부 REST·파일·Secret·인증·전문·알림 개발자와 QA
> **완료 결과**: 확장 Fixture를 실제 CPF Adapter·Ledger·Fault Test와 연결한다.
> **기준 Repository**: `freeangelsun/202412_01_CPF` / `master` / `54bcc10887a83b933685bff462c0b0d7df824923` (`20260802_10`)

<!-- CPF-TOC:START -->
## 전체 목차

- [1. WireMock](#1-wiremock)
- [2. SFTP](#2-sftp)
- [3. Vault](#3-vault)
- [4. Keycloak](#4-keycloak)
- [5. RabbitMQ·Artemis·IBM MQ 추가 원칙](#5-rabbitmqartemisibm-mq-추가-원칙)
- [6. TCP·ISO8583 Simulator](#6-tcpiso8583-simulator)
- [7. Notification Fixture](#7-notification-fixture)
- [8. 확장 Fixture 완료 Gate](#8-확장-fixture-완료-gate)

<!-- CPF-TOC:END -->

## 1. WireMock

- 정상 2xx, 4xx 업무 오류, 5xx 일시 오류, 지연, 연결 종료, Malformed, Response Loss를 Stub으로 분리한다.
- 요청 Header·Body Hash·Idempotency·Signature를 검증한다.
- Side Effect 후 Response Loss 시 신규 요청을 보내지 않고 조회 Stub과 Attempt Ledger를 대사한다.

## 2. SFTP

- Known Host·User·Secret·Root Directory를 Fixture Manifest로 고정한다.
- `.part` Upload→Checksum→Atomic Rename을 확인한다.
- 연결 중단 뒤 Resume Offset과 Transfer Ledger를 대사한다.
- Oversize·Traversal·Duplicate File·Checksum mismatch를 거부한다.

## 3. Vault

- Secret Path·Version·Policy·Token TTL을 분리한다.
- Application에는 Secret Reference만 전달한다.
- Rotation은 신규 Version→Consumer Reload/Restart→이전 Revoke 순서로 시험한다.
- Vault Down·Permission Denied·Expired Token을 Fail-closed하고 원문을 Log하지 않는다.

## 4. Keycloak

- Realm·Public Client·Service Client·User·Role·Audience를 Fixture로 초기화한다.
- Browser Login/Logout/Session Expiry/CSRF와 Resource Server Token/Audience/Scope를 시험한다.
- Service Identity와 사용자 Token을 혼용하지 않는다.

## 5. RabbitMQ·Artemis·IBM MQ 추가 원칙

현재 설치 Script에 없는 Provider는 Compose 파일·Image Lock·Secret·Health·Fixture·Toxiproxy Proxy·검증 Script를 같은 변경으로 추가한다. Container만 추가하지 않고 실제 Starter Consumer·Reliability Ledger·ADM 운영 조회를 연결한다.

## 6. TCP·ISO8583 Simulator

Simulator는 Frame4종, Fragment/Merge, DLE Escaping, Oversize, Delay, Disconnect, Half-open, Duplicate/Orphan Response, TLS/mTLS, Secondary Bitmap·MAC를 지원해야 한다. Correlation ID와 기관 조회 응답을 제공해 UNKNOWN_RESULT 대사를 시험한다.

## 7. Notification Fixture

Email/SMS Fixture는 Accept와 실제 Delivery/Receipt를 구분한다. Timeout·Bounce/Reject·Duplicate·Quiet Hours·Preference·Provider Failover를 제공하고 Notification Outbox·Receipt·Audit와 연결한다.

## 8. 확장 Fixture 완료 Gate

| 항목 | 필수 |
|---|---|
| Compose | 고정 Image/Port/Network/Health/Restart=no |
| Secret | Repository 밖 Reference |
| Fixture | 정상·오류·Timeout·부분 실패 |
| Consumer | 실제 Product/Domain Adapter |
| Ledger | Attempt/Transfer/Outbox/Inbox/Receipt |
| Fault | Toxiproxy/Process Kill/Response Loss |
| Operations | Health·Metric·ADM·Reconcile |
| Cleanup | 정확한 Container/Volume/Data |
