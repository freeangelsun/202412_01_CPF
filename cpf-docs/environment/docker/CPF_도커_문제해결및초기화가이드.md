# CPF Docker 문제 해결 및 초기화 가이드

상위 메뉴: [Docker 문서](README.md)

## 1. 진단 순서

```text
Docker Engine
→ Compose config
→ Image
→ Container State/Exit Code
→ Health/Readiness
→ Port/Network/TLS
→ Secret/Permission
→ Product Client
→ Ledger/Reconcile
```

## 2. 실패 위치

Script 오류에는 단계 이름과 Exit Code를 포함한다.

```text
RABBITMQ_HEALTH
RABBITMQ_PUBLISH
ARTEMIS_HEALTH
ARTEMIS_SEND_RECEIVE
IBM_MQ_HEALTH
SFTP_TRANSFER
VAULT_SECRET_WRITE
KEYCLOAK_PASSWORD_GRANT
```

Secret 인자를 오류 메시지에 포함하지 않는다.

## 3. 흔한 문제

### Port 충돌

`docker ps`, `Get-NetTCPConnection`으로 소유 Process를 확인한다. 다른 CPF Compose를 동시에 실행하지 않는다.

### Container Exit 143

StopAfter의 정상 SIGTERM인지, OOM·Health 실패 전 종료인지 Log와 Script 순서로 판정한다.

### RabbitMQ 인증

Secret 파일과 `messaging-runtime.env`의 사용자·Password를 확인한다. 값을 화면에 출력하지 않는다.

### Artemis Queue 없음

Initializer의 Queue 생성 단계와 Broker Log를 확인한다. Product가 다른 Address/Queue 이름을 사용하지 않는지 확인한다.

### IBM MQ License

`-AcceptIbmMqDeveloperLicense`를 명시하지 않으면 설치하지 않는다. 개발자 License 사용 범위와 재배포 제한을 확인한다.

### SFTP 인증 전 종료

Batch Mode와 Password 인증 옵션, OpenSSH 지원 옵션, `sshd -t` 결과를 확인한다.

## 4. 데이터 초기화

기본 금지:

```text
docker system prune
Docker Factory Reset
docker volume prune
전체 Image 삭제
사용자 DB Drop/Reset
```

초기화가 필요하면 Service, Volume, 영향 데이터, Backup, 재생성 절차를 먼저 기록하고 정확한 대상만 처리한다.

## 5. 정상화 판정

- Container Health/Readiness
- 실제 Put/Get 또는 Publish/Receive
- Retry/DLQ/Receipt 상태
- Product Operation과 Broker/DB 대사
- Fault 제거
- Running Container 0
- Secret 미노출
