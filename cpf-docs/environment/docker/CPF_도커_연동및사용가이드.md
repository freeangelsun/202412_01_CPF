# CPF Docker 연동 및 사용 가이드

상위 메뉴: [Docker 문서](README.md)

## 1. 공통 Compose 호출

```powershell
$root='C:\dev\Docker\CPF';$env='C:\dev\Docker\Secrets\cpf-runtime.env';docker compose --env-file $env -f "$root\compose.yml" -f "$root\compose.redis.yml" -f "$root\compose.kafka.yml" ps
```

실제 작업에서는 필요한 Compose 파일만 지정한다.

## 2. DB

### 시작

```powershell
docker compose --env-file C:\dev\Docker\Secrets\cpf-runtime.env -f C:\dev\Docker\CPF\compose.yml up -d mariadb
```

`mariadb` 대신 `postgresql`, `oracle`을 선택한다.

### 검증

- 전용 QA Database/Schema인지 확인
- CPF Object Count 0에서 Fresh 시작
- Install·Runtime Query·Upgrade·Recovery·Reapply·Drift·Backup/Restore 실행
- 사용자 DB를 Drop/Reset하지 않음

### 중지

```powershell
docker compose --env-file C:\dev\Docker\Secrets\cpf-runtime.env -f C:\dev\Docker\CPF\compose.yml stop mariadb
```

## 3. Redis·Kafka

각 Compose 파일과 Secret을 사용한다. Broker 기동만으로 Kafka Starter Consumer·DLT·Replay가 검증된 것은 아니다. Product Test와 Evidence를 별도로 실행한다.

## 4. RabbitMQ·JMS

```powershell
pwsh -NoProfile -ExecutionPolicy Bypass -File C:\dev\Docker\CPF\initialize-messaging-fixtures.ps1
```

Fixture:

```text
RabbitMQ vhost : /cpf
Exchange       : cpf.qa38.direct
Primary Queue  : cpf.qa38.primary
DLQ            : cpf.qa38.dlq
Artemis Queue  : cpf.qa38.primary
```

Product Client는 Named Binding을 사용한다. Kafka와 RabbitMQ/JMS가 함께 있을 때 Default Binding은 최대 하나다.

## 5. IBM MQ

IBM MQ Container는 설치 시 명시적으로 선택한 경우에만 사용한다. 기본 Queue Manager 이름은 `CPFQM`, 기본 개발 Queue는 Image 정책과 Fixture Script를 확인한다. TLS·Channel·CCDT·Reason Code·Credential Rotation 시험은 Product IBM MQ Extension과 함께 실행한다.

## 6. Toolchain

Tooling Container는 Source Build·Frontend·Browser·DB Client·Supply Chain 검증에 사용한다. Host Directory를 Mount할 때 Secret Root를 Mount하지 않는다.

## 7. 작업 종료

시작한 Service만 `stop`한다. `down -v`를 기본 종료 명령으로 사용하지 않는다. Volume 삭제는 데이터 초기화 승인과 정확한 대상이 있을 때만 수행한다.

## 8. Evidence

- Container Log는 Secret을 제거한 뒤 Hash를 기록한다.
- Image Tag뿐 아니라 Digest를 기록한다.
- Exit Code를 후속 출력 명령이 덮어쓰지 않게 저장한다.
- StopAfter 후 `docker ps`에서 CPF Running 0을 확인한다.
