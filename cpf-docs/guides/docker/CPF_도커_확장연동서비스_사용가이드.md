# CPF 도커 확장 연동 서비스 사용 가이드

상위 메뉴: [CPF Docker 가이드](README.md)

## 1. Kafka가 CPF의 MQ다

CPF의 공식 Messaging Primary는 Kafka다. Event, Outbox, Batch Remote Partition/Chunk, Retry Topic, DLT, Consumer Group, Rebalance와 Broker 장애 검증은 `cpf-kafka`에서 수행한다.

RabbitMQ·ActiveMQ·IBM MQ는 현재 공식 Adapter와 실제 Product Consumer가 없으므로 설치하지 않는다. 특정 고객 연계로 필요해질 때는 고객 Adapter Owner, Starter, Dependency, Security, 운영 화면과 Runtime Evidence를 함께 승인한 뒤 별도 추가한다.

## 2. 확장 서비스 요약

| Service | Container | 직접 Port | 용도 |
|---|---|---:|---|
| WireMock | `cpf-wiremock` | 18080 | 외부 REST 성공·503·지연·Connection Reset |
| SFTP | `cpf-sftp` | 2222 | 파일 송수신, ACK/NACK, 재처리, 보관 |
| Vault | `cpf-vault` | 8200 | Secret Provider, Rotation·Revocation 연결 |
| Keycloak | `cpf-keycloak` | 18081 | OIDC/OAuth2/JWT, 운영자·Service 인증 |

모든 Port는 `127.0.0.1`에만 Bind한다. 모든 Container는 `restart: no`다.

## 3. 시작·중지

전체 확장 Service 시작:

```powershell
pwsh -NoProfile -File "C:\dev\Docker\CPF\cpf-env.ps1" -Action up -Target external
```

전체 확장 Service 중지:

```powershell
pwsh -NoProfile -File "C:\dev\Docker\CPF\cpf-env.ps1" -Action stop -Target external
```

개별 시작 Target:

```text
wiremock
sftp
vault
identity
```

`cpf-env.ps1`은 선택한 Group만 시작·중지한다. 다른 DB나 Service를 광역 중지하지 않는다.

## 4. WireMock

기본 Endpoint:

| Endpoint | 결과 |
|---|---|
| `GET /cpf-test/health` | 200, Fixture 상태 |
| `POST /cpf-test/transactions` | 200, 요청 ID Echo |
| `GET /cpf-test/unavailable` | 503, Retry-After |
| `GET /cpf-test/slow` | 3초 지연 후 200 |
| `GET /cpf-test/connection-reset` | 연결 Reset |

정상 확인:

```powershell
Invoke-RestMethod -Uri "http://127.0.0.1:18080/cpf-test/health"
```

CPF 외부연계 Client의 timeout, retry 가능 여부, unknown-result, audit와 masking을 실제 Fixture에 연결해 확인한다.

## 5. SFTP

계정명:

```text
cpf-sftp
```

비밀번호:

```text
C:\dev\Docker\Secrets\sftp-password.txt
```

교환 경로:

```text
/exchange/inbound
/exchange/outbound
/exchange/ack
/exchange/error
/exchange/archive
```

Host 연결:

```text
Host: 127.0.0.1
Port: 2222
Protocol: SFTP
```

Secret 값을 화면에 출력한 결과를 로그나 Evidence에 복사하지 않는다. `initialize-integration-fixtures.ps1`은 통합 Runner에서 실제 업로드·다운로드와 Content 비교를 수행한다.

## 6. Vault

Endpoint:

```text
http://127.0.0.1:8200
```

Dev Token:

```text
C:\dev\Docker\Secrets\vault-token.txt
```

이 Vault는 로컬 연결·오류·회전 시나리오용 Dev Fixture다. 운영 저장소, Production Seal, HA Evidence로 사용할 수 없다. 기본 초기화 Script는 `secret/cpf/test`에 민감하지 않은 상태값만 기록한다.

## 7. Keycloak

Realm:

```text
cpf-test
```

계정·Client:

| 용도 | 식별자 | Secret 위치 |
|---|---|---|
| 관리자 | `cpf-admin` | `keycloak-admin-password.txt` |
| 테스트 사용자 | `cpf-reviewer` | `keycloak-test-password.txt` |
| Browser Client | `cpf-admin-local` | Public Client, Secret 없음 |
| Service Client | `cpf-service-local` | `keycloak-service-client-secret.txt` |

OIDC Discovery:

```text
http://127.0.0.1:18081/realms/cpf-test/.well-known/openid-configuration
```

관리 Console:

```text
http://127.0.0.1:18081/admin/
```

Keycloak 내부 개발 저장소는 CPF 공식 DB Vendor Evidence가 아니다. CPF Application의 Session·Owner DB는 Oracle·PostgreSQL·MariaDB 기준으로 따로 확인한다.

## 8. Fixture 초기화

```powershell
pwsh -NoProfile -ExecutionPolicy Bypass -File "C:\dev\Docker\CPF\initialize-integration-fixtures.ps1"
```

수행 내용:

- WireMock Health·정상 응답·503·3초 지연·Connection Reset 호출
- Vault KV 쓰기·읽기
- Keycloak OIDC Discovery 확인
- Keycloak 테스트 사용자·Realm Role 구성
- Keycloak Browser Client의 로컬 Password Grant Token 확인
- Keycloak Service Client의 Client Credentials Token 확인
- SFTP 실제 업로드·다운로드·파일 비교
- Secret 값 미출력
- Sanitized JSON 결과 생성

Password Grant는 로컬 Identity Fixture의 계정·Client 연결 확인에만 사용한다. 운영 인증 흐름의 권장 방식이나 Production 완료 Evidence로 사용하지 않는다.

종료 후 Service도 중지하려면 `-StopAfter`를 사용한다.

## 9. 장애 주입 Proxy

| 대상 | 직접 Port | Proxy Port |
|---|---:|---:|
| WireMock | 18080 | 18090 |
| SFTP | 2222 | 12222 |
| Vault | 8200 | 18200 |
| Keycloak | 18081 | 18091 |

시작:

```powershell
pwsh -NoProfile -File "C:\dev\Docker\CPF\cpf-tooling.ps1" -Action up -Target fault-external
```

Toxiproxy API로 latency, timeout, bandwidth, reset 조건을 추가한 뒤 CPF Source의 retry·unknown-result·recovery·audit를 확인한다. 종료 전 `reset-faults`로 장애 조건을 제거한다.

## 10. Secret 조회 원칙

로컬 작업자가 반드시 확인해야 할 때만 해당 Secret 파일을 직접 연다. 값은 채팅, 이슈, 스크린샷, 로그, Evidence에 붙여넣지 않는다.

```powershell
Get-ChildItem "C:\dev\Docker\Secrets" | Select-Object Name,Length,LastWriteTime
```

위 명령은 값이 아니라 파일 존재만 확인한다.

## 11. 계정·Secret 변경 시 동작

- 증분 설치 Script는 기존 Secret 파일을 덮어쓰지 않고 누락 파일만 생성한다.
- SFTP 비밀번호는 `cpf-sftp` Container 시작 시 적용되므로 파일 변경 후 해당 Container를 재시작하고 송수신을 다시 확인한다.
- Vault Dev Token은 Container 시작 시 적용되므로 파일 변경 후 `cpf-vault`를 재시작하고 KV 연결을 다시 확인한다.
- Keycloak 테스트 사용자 비밀번호와 Service Client Secret은 초기화 Script가 Secret 파일 값으로 동기화한다.
- Keycloak 관리자 비밀번호는 최초 Bootstrap 후 단순 파일 교체만으로 변경되지 않는다. 관리자 Credential 변경 또는 `cpf-keycloak-data` 초기화가 필요하며, 정확한 대상과 영향 확인 및 승인 없이 Volume을 삭제하지 않는다.
- Secret 값을 확인하거나 변경한 화면·명령·로그를 Evidence에 포함하지 않는다.
