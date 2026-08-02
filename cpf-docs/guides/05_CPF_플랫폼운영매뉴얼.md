# CPF 플랫폼 운영 매뉴얼 — 설치·설정·배포·관측·정상화

> **주 독자**: 인프라 운영자, DBA, 배포 담당자, 보안 담당자, 관측 담당자, 재해복구 담당자
> **완료 결과**: CPF Runtime과 선택 제품을 설치하고, Artifact·Property·Secret·DB·메시지 브로커·배포·관측·백업·장애 대응·Rollback을 역할별 절차와 판정 기준에 따라 수행한다.

## 문서 기준

- Repository: `freeangelsun/202412_01_CPF`
- Branch: `master`
- 기준 Commit: `3b600702502e53877e30cbac594987b371e2186b` (`20260802_08`)
- 최상위 요구 정본: `cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md`
- 활성 개발 요구: `cpf-docs/work/current/CPF_QA38_FINAL_DEVELOPMENT_REQUIREMENTS.md`
- 활성 Matrix: `cpf-docs/quality/CPF_QA38_FINAL_REQUIREMENT_MATRIX.csv`, `cpf-docs/quality/CPF_QA38_FINAL_SCENARIO_MATRIX.csv`
- 실제 Source·SQL·Config·Script·Test가 문서보다 우선한다.
- 직접 실행하지 않은 DB·Runtime·Browser·다중 인스턴스·장애 시험은 `미검증`으로 표시한다.

## 1. 문서 사용 순서와 역할 경계

| 역할 | 이 문서에서 수행하는 일 | 다른 매뉴얼로 이동하는 일 |
|---|---|---|
| 플랫폼 운영자 | 설치, 기동·종료, 배포, 관측, 용량, 정상화 | 업무 API 개발은 01 매뉴얼 |
| DBA | 계정, Schema, Migration, Drift, Backup·Restore | 업무 SQL 설계는 DB 표준서 |
| 보안 담당자 | Secret, Certificate, TLS, 권한 분리, 감사 | 업무 Permission 설계는 01·03·95 |
| 배포 담당자 | Artifact, Checksum, Manifest, Rollback | Starter 선택은 90 매뉴얼 |
| Docker 환경 운영자 | 통합시험 Runtime 설치·선택 기동 | 상세 명령은 `cpf-docs/environment/docker/` |

운영자는 CPF가 소유하지 않는 업무 원장을 직접 수정하지 않는다. 상태 불일치는 Owner API, Operation, Outbox·Inbox, Batch Metadata, Audit와 대사하여 정상화한다.

## 2. 기준 환경과 지원 범위

지원 환경은 문서의 목표 목록이 아니라 기준 Commit의 Build 설정, Docker Source, 실제 실행 결과가 함께 확인된 범위로 판정한다. 운영체제·Java·Gradle·DB·Container Runtime 조합을 직접 실행하지 않았다면 `미검증`으로 기록한다.

### 2.1 기준 Commit에서 확인된 범위

| 구분 | 기준 Source | 현재 판정 |
|---|---|---|
| Java·Spring Stack | `gradle/cpf-stack.properties` | Source 확인 필요, 실행 미검증 |
| 공식 DB Vendor | MariaDB·PostgreSQL·Oracle | Source 존재, 3 Vendor Runtime 미검증 |
| 공개 Starter | `settings.gradle`, `cpf-starters/*` 7개 | 부분 구현 |
| 로컬 Runtime | `cpf-tools/runtime/cpf-local-runtime`, `cpf-local-batch-runtime` | Source 존재, 최신 SHA 실행 미검증 |
| Docker 개발·시험 환경 | `cpf-tools/environment/docker-development-test/` | 부분 구현 |
| Artifact Mode | `LOCAL_DEV`, `REMOTE`, `OFFLINE` | 설정 계약 확인 |

### 2.2 Docker 개발·시험 Runtime

현재 Compose Source에서 확인된 서비스는 다음과 같다.

| 서비스 | Compose | Host Port | 용도 | 상태 판정 |
|---|---|---:|---|---|
| MariaDB | `compose.yml` | 3306 | DB Vendor Lifecycle | Source 존재 |
| PostgreSQL | `compose.yml` | 5432 | DB Vendor Lifecycle | Source 존재 |
| Oracle Free | `compose.yml` | 1521 | DB Vendor Lifecycle | Source 존재 |
| Redis | `compose.redis.yml` | 6379 | Cache·Lock·Invalidation | Source 존재 |
| Kafka | `compose.kafka.yml` | 9092 | 메시지 Provider Fixture | Source 존재 |
| WireMock | `compose.integration.yml` | 18080 | REST 지연·오류 Fixture | Source 존재 |
| SFTP | `compose.integration.yml` | 2222 | 파일 송수신 Fixture | Source 존재 |
| Vault | `compose.integration.yml` | 8200 | Secret Provider Fixture | Source 존재 |
| Keycloak | `compose.integration.yml` | 18081 | OIDC/OAuth2 Fixture | Source 존재 |
| Toxiproxy | `compose.tooling.yml` | 파일 기준 | 네트워크 장애 주입 | Source 존재 |
| OTel Collector | `compose.tooling.yml` | 4317·4318·8888 | Trace·Metric 수집 | Source 존재 |

RabbitMQ·Jakarta JMS·IBM MQ·TCP 전용 Simulator·Notification Provider는 최신 `settings.gradle`에 정식 Starter가 등록되지 않은 상태이므로, Container만 추가해 지원 기능으로 판정하지 않는다.

## 3. 설치 전 점검

어느 폴더에서 실행해도 되도록 Repository 경로를 명시한다.

```powershell
$repo='C:\dev\projects\jck\202412_01_CPF'
if(-not(Test-Path -LiteralPath $repo -PathType Container)){throw "Repository가 없습니다: $repo"}
git -C $repo remote get-url origin
git -C $repo branch --show-current
git -C $repo rev-parse HEAD
git -C $repo rev-parse origin/master
git -C $repo status --short
git -C $repo diff --name-status
git -C $repo ls-files --others --exclude-standard
```

확인 기준:

1. Branch가 `master`인지 확인한다.
2. Local 변경과 미추적 파일을 Owner별로 분류한다.
3. Ahead·Behind·Diverged이면 임의 정리하지 않는다.
4. 배포 대상 Commit, Artifact Version, SHA-256을 기록한다.
5. 대상 Topology, Instance 수, DB Vendor, Message Provider, Secret Provider를 확정한다.
6. Port·DNS·TLS·Proxy·Firewall·시간 동기화를 확인한다.
7. Backup ID, Rollback Version, 작업 중단 기준과 승인자를 기록한다.

## 4. Artifact·Checksum·Manifest

### 4.1 필수 인계 항목

| 항목 | 판정 기준 |
|---|---|
| Artifact 이름·Version | 배포 요청과 일치 |
| Git Commit | Source 기준과 일치 |
| SHA-256 | 전송 전·후 동일 |
| Build Manifest | 모듈·의존성·Build 도구 Version 기록 |
| SBOM | 배포 Artifact와 같은 Build에서 생성 |
| DB Migration Pack | 대상 Vendor와 Version 일치 |
| Config Manifest | Profile·Property Version·Checksum 기록 |
| Rollback Artifact | 실제 저장 위치와 Hash 확인 |

### 4.2 확인 명령 예

```powershell
$artifact='C:\path\to\cpf-artifact.jar'
if(-not(Test-Path -LiteralPath $artifact -PathType Leaf)){throw "Artifact가 없습니다: $artifact"}
Get-Item -LiteralPath $artifact | Select-Object FullName,Length,LastWriteTime
Get-FileHash -LiteralPath $artifact -Algorithm SHA256
```

Artifact 이름만 같고 Hash가 다르면 배포하지 않는다. 재빌드 Artifact는 같은 Version을 덮어쓰지 않고 새 식별자 또는 Build Metadata로 구분한다.

## 5. 계정·Directory·파일 권한

| 대상 | 권장 분리 | 금지 또는 주의 |
|---|---|---|
| OS·Container User | Runtime 전용 계정 | 관리자 계정 상시 실행 |
| DB 계정 | Admin·Migration·Runtime·ReadOnly | Runtime DDL 권한 |
| Artifact Directory | 읽기 전용 배포본·별도 작업 Directory | 실행 중 Artifact 덮어쓰기 |
| Log Directory | Runtime 쓰기·운영 읽기 | Secret·개인정보 원문 기록 |
| Temp Directory | 용량·보존·정리 기준 | 광역 재귀 삭제 |
| Backup Directory | 운영계와 분리 | 복원 시험 없는 보관 |
| Secret Root | Repository 밖 접근 제한 | Git·ZIP·Evidence 포함 |

경로 예:

```text
Repository   : C:\dev\projects\jck\202412_01_CPF
Docker Root  : C:\dev\Docker\CPF
Secret Root  : C:\dev\Docker\Secrets
```

## 6. Property·환경변수·Profile 관리

### 6.1 Property Catalog 필수 열

```text
Key / Environment Variable / Type / Default / Required / Range
Consumer / Profile / Restart Required / Secret
Failure Symptom / Verify Command / Expected Result / Rollback
```

### 6.2 기준 Source에서 확인된 공통 설정

| 설정 | 환경변수·대체 입력 | Type·기본값 | Consumer | 변경 영향 |
|---|---|---|---|---|
| `cpfArtifactMode` | `CPF_ARTIFACT_MODE` | Enum, 기본 `LOCAL_DEV` | `settings.gradle` Plugin Resolution | Build 재실행 |
| `cpfArtifactRepositoryUrl` | `CPF_ARTIFACT_REPOSITORY_URL` | URL, REMOTE에서 필수 | Build Plugin·Dependency Resolution | Build 재실행 |
| Local Artifact Repository | `CPF_LOCAL_ARTIFACT_REPOSITORY` | 경로, 기본 사용자 홈 `.cpf/repository` | Local Build | Build 재실행 |
| Offline Artifact Repository | `CPF_OFFLINE_ARTIFACT_REPOSITORY` | 경로, OFFLINE에서 필수 | Offline Build | Build 재실행 |
| Repository User | `CPF_ARTIFACT_REPOSITORY_USER` | 문자열 | REMOTE Repository | Build 재실행 |
| Repository Password | `CPF_ARTIFACT_REPOSITORY_PASSWORD` | Secret | REMOTE Repository | Build 재실행 |
| Reference DB Vendor | `REF_DATABASE_VENDOR` 또는 Gradle `cpfDbVendor` | `mariadb`·`postgresql`·`oracle`, 기본 `mariadb` | `cpf-reference` Test·Migration 선택 | Test·Runtime 재기동 |
| Reference Feature Flags | `cpf.reference.features.<name>.enabled` | Boolean, 기준 Source 기본 `true` | `cpf-reference` Source Set | Build 재실행 |

Property가 문서에 없더라도 Source에 존재하면 Catalog 누락이다. 반대로 Source에 없는 Key를 문서에서 만들지 않는다.

### 6.3 변경 절차

1. 변경 목적·영향 Consumer·대상 Profile을 기록한다.
2. Secret 여부와 원문 노출 가능성을 확인한다.
3. Type·Range·Default·필수 여부를 검증한다.
4. 사전 Validation 또는 Dry Run을 실행한다.
5. Property Version·Checksum을 생성한다.
6. 제한 Target에 적용하고 ACK·NACK를 수집한다.
7. Health뿐 아니라 실제 업무 Probe를 확인한다.
8. 일부 Target만 적용되면 `PARTIAL_SUCCESS`로 기록하고 신규 변경을 중지한다.
9. Reconcile 또는 이전 Config Version으로 Rollback한다.

## 7. Secret·Certificate

### 7.1 Secret 원칙

- Secret 원문을 Git, Markdown, CSV, JSON Evidence, 명령 이력, 화면 캡처에 저장하지 않는다.
- Secret은 파일 경로·Reference ID·Version·만료일만 기록한다.
- 필수 Secret이 없거나 만료되면 운영 Profile Readiness를 열지 않는다.
- Rotation은 Old/New 중첩 시간, Target별 적용 결과, Rollback 조건을 포함한다.

### 7.2 Docker Secret Source 예

현재 Compose Source가 참조하는 파일 예:

```text
C:\dev\Docker\Secrets\redis-password.txt
C:\dev\Docker\Secrets\sftp-password.txt
C:\dev\Docker\Secrets\vault-token.txt
C:\dev\Docker\Secrets\keycloak-admin-password.txt
C:\dev\Docker\Secrets\keycloak-test-password.txt
C:\dev\Docker\Secrets\keycloak-service-client-secret.txt
```

파일 존재 여부만 확인하고 내용은 출력하지 않는다.

```powershell
$secretRoot='C:\dev\Docker\Secrets'
$names=@('redis-password.txt','sftp-password.txt','vault-token.txt','keycloak-admin-password.txt','keycloak-test-password.txt','keycloak-service-client-secret.txt')
foreach($n in $names){$p=Join-Path $secretRoot $n;[pscustomobject]@{Name=$n;Exists=Test-Path -LiteralPath $p -PathType Leaf}}
```

### 7.3 Certificate 점검

- Subject·SAN·Issuer·Chain
- NotBefore·NotAfter
- TLS Protocol·Cipher
- mTLS Client Certificate 매핑
- Revocation·Rotation 계획
- Clock Skew
- 이전 인증서 Rollback 가능 시간

## 8. DB 설치·Migration·Drift

### 8.1 계정 분리

| 계정 | 허용 | 금지 |
|---|---|---|
| Admin | 계정·DB·Schema 준비 | Application 상시 사용 |
| Migration | DDL·Migration 기록 | 업무 Runtime 사용 |
| Runtime | DML·필요 Sequence·Procedure 실행 | 임의 DDL |
| ReadOnly | 조회·검수 | 변경 |

### 8.2 Lifecycle

```text
Empty Environment
→ Fresh Install
→ Verify
→ Runtime Query
→ Upgrade
→ Verify
→ Rollback 또는 Forward Recovery
→ Reapply
→ Drift Detection
→ Backup
→ Isolated Restore
→ Application Query
```

### 8.3 설치 명령 시작점

```powershell
$repo='C:\dev\projects\jck\202412_01_CPF'
pwsh -NoProfile -ExecutionPolicy Bypass -File (Join-Path $repo 'cpf-tools\scripts\initialize-cpf-database.ps1') -All -RequireRun
```

실제 Script Parameter는 `Get-Help -Full`로 확인한다.

```powershell
$repo='C:\dev\projects\jck\202412_01_CPF'
Get-Help (Join-Path $repo 'cpf-tools\scripts\initialize-cpf-database.ps1') -Full
```

### 8.4 정상 결과

- 설치 전 CPF Object Count 0 또는 승인된 Baseline
- 대상 Vendor용 Migration Pack 선택
- Migration History 중복·누락 없음
- Verify Script Exit Code 0
- Runtime 계정 Query 성공
- Upgrade 후 기존 데이터 의미 보존
- Rollback/Forward Recovery 후 Application Query 성공
- Drift 0 또는 승인된 예외 목록

### 8.5 실패·부분 실패

- DDL 일부 적용 후 실패하면 동일 Script 무조건 재실행 전에 Migration History와 실제 Object를 대조한다.
- Vendor별 SQL 의미가 다르면 한 Vendor 성공을 전체 성공으로 승계하지 않는다.
- Rollback SQL이 데이터 손실을 유발하면 Forward Recovery를 사용하고 승인·Backup ID를 기록한다.

## 9. 메시지 브로커 운영

### 9.1 현재 사용 가능 범위

기준 Commit의 정식 공개 메시징 Starter는 Kafka 계열 하나다. RabbitMQ·Jakarta JMS·IBM MQ는 QA38 목표이나 정식 Starter·실제 Consumer·Operations·Test가 모두 확인되기 전에는 지원 Provider로 표시하지 않는다.

### 9.2 운영 점검표

```text
Broker/Cluster Health
Topic·Queue·Subscription Definition
Partition·Replication·Durability
Producer Receipt·Confirm
Consumer Group·Instance·Assignment
Backlog·Lag·Oldest Age
Retry·DLQ/DLT·Poison Message
Schema·Header·Idempotency
Credential·TLS·Certificate
Replay Approval·Audit
```

### 9.3 결과 불명과 대사

Publisher가 전송 후 Receipt를 받지 못하면 신규 업무 요청을 만들지 않는다. Outbox·Attempt·Broker 상태·Consumer 업무 원장을 조회하여 `UNKNOWN_RESULT`를 해소한다.

## 10. 기동·Readiness·종료

### 10.1 기동 순서

1. DB·Secret Provider·메시지 브로커·Collector를 준비한다.
2. Migration Version·Drift·Backup ID를 확인한다.
3. Control Plane·Owner Service·Worker·Edge 순으로 기동한다.
4. Liveness가 아니라 Readiness와 실제 Synthetic Probe를 확인한다.
5. 제한된 Traffic을 열고 오류율·지연·Backlog·업무 대사를 관찰한다.

로컬 Runtime 시작점:

```powershell
$repo='C:\dev\projects\jck\202412_01_CPF'
pwsh -NoProfile -ExecutionPolicy Bypass -File (Join-Path $repo 'cpf-tools\scripts\start-cpf-local.ps1')
pwsh -NoProfile -ExecutionPolicy Bypass -File (Join-Path $repo 'cpf-tools\scripts\status-cpf-local.ps1')
```

### 10.2 종료 순서

1. 신규 Traffic과 Schedule을 중지한다.
2. Worker 신규 Claim을 차단한다.
3. 진행 Transaction·Outbox·Checkpoint·File Transfer를 Drain한다.
4. Runtime을 종료한다.
5. 필요 서비스만 중지하고 Volume은 보존한다.

```powershell
$repo='C:\dev\projects\jck\202412_01_CPF'
pwsh -NoProfile -ExecutionPolicy Bypass -File (Join-Path $repo 'cpf-tools\scripts\stop-cpf-local.ps1')
```

## 11. 배포 전략

### 11.1 Rolling

사용 조건:

- Mixed Version API 호환
- DB Expand/Contract 순서 검증
- Message Schema 하위 호환
- Worker·Scheduler 중복 실행 통제

### 11.2 Blue-Green

- DB·Broker·File Store 공유 시 이중 처리 방지
- 전환 전 Green Synthetic Probe
- 전환 후 Blue의 Worker·Scheduler 중지
- Rollback Traffic 경로와 Session 영향 확인

### 11.3 Canary

- 대상 Instance·Route·사용자 구간 정의
- 오류율·지연·Backlog·업무 건수·금액 대사 기준 설정
- 중단 임계치 도달 시 확대 중지와 Rollback

### 11.4 Partial Apply

Target별 Version·Checksum·ACK/NACK를 저장한다. 일부 Target만 성공하면 신규 배포를 겹치지 않고 성공 Target 유지 또는 이전 Version 복귀를 결정한다.

## 12. Log·Metric·Trace·Audit

### 12.1 Log

최소 상관 식별자:

```text
requestId / traceId / transactionId / operationId
businessKey / idempotencyKey / attempt
state / failureClass / owner / target
```

Payload·Password·Token·개인정보 원문을 기록하지 않는다.

### 12.2 Metric

- Request·Error·Latency
- Retry·Reconcile·UNKNOWN_RESULT
- Broker Backlog·DLQ/DLT
- DB Pool·Lock·Slow Query
- Lease Conflict·Fencing Rejection
- Batch Read·Write·Skip·Rollback
- File Size·Transfer·Checksum Failure
- OTel Export Backpressure

### 12.3 Trace

Gateway→Owner→DB·Broker·외부 Provider의 전체 Timeout Budget과 Attempt를 연결한다. Sampling으로 누락되는 위험 거래는 Audit·Operation으로 보완한다.

### 12.4 Audit

운영 변경은 Actor, Role, Permission, Data Scope, Reason, Approval, Expected Version, Before/After, Target Result를 남긴다.

## 13. Capacity 관리

| 자원 | 관측 | 임계 시 행동 |
|---|---|---|
| CPU·Memory·Heap·GC | 사용률·Pause·OOM | Traffic·Concurrency 제한 |
| Thread·Connection Pool | Active·Queue·Timeout | 원인 제거 전 무조건 증설 금지 |
| DB | Session·Lock·IO·Disk | 신규 작업 제한·Slow Query 분석 |
| Broker | Partition·Queue·Lag·Oldest Age | Consumer·Poison·Provider 상태 확인 |
| Batch | Chunk·Partition·Worker·Commit | 분할·Commit Interval 조정 |
| File | Size·Concurrency·Disk | 신규 수락 제한·보존 정책 실행 |
| Log·Trace | Queue·Drop·Exporter Latency | Sampling·Buffer·Collector 점검 |

부하 시험은 정상 처리량뿐 아니라 Provider 장애 중 Backlog 증가율과 정상화 시간을 측정한다.

## 14. Backup·Restore

### 14.1 범위

- DB Data·Migration History
- Broker Metadata·필요 Message Snapshot
- Config Version·Checksum
- Certificate·Secret Reference Metadata
- Gateway Route·Policy·LKG
- BZA 조직·권한·결재 정책 Version
- Artifact Manifest·SBOM

### 14.2 Restore 판정

격리 환경에서 Restore 후 Runtime을 기동하고 실제 조회·권한·결재·배치·메시지 Probe를 수행한다. Backup 파일 존재만으로 Restore 성공으로 기록하지 않는다.

## 15. Upgrade·Rollback

```text
Compatibility Matrix
→ Backup·Restore 확인
→ DB Pre-check
→ Artifact·Config Validation
→ 제한 Target 적용
→ ACK·Readiness·업무 대사
→ 확대 또는 중단
→ Rollback/LKG/Forward Recovery
→ Drift 0 확인
```

Rollback 기준:

- 오류율·지연 임계 초과
- Readiness 실패
- DB·Message Schema 비호환
- Target Partial Apply
- 권한·Masking·Audit 회귀
- 업무 건수·금액 대사 불일치

## 16. 장애 Runbook

### 16.1 DB 연결·Lock·Drift

1. 연결 오류와 인증·네트워크·Pool 고갈을 구분한다.
2. Lock Owner·대기 Transaction·업무 Operation을 확인한다.
3. 직접 Row 수정 전에 Owner Service와 대사한다.
4. Drift는 승인된 Migration Pack과 실제 Object를 비교한다.
5. 정상화 후 Runtime Query·업무 대사·Audit를 확인한다.

### 16.2 메시지 브로커

1. Broker Health, Producer Receipt, Consumer Assignment를 분리 확인한다.
2. Backlog·Oldest Age·Poison Message를 확인한다.
3. 중복 가능성이 있으면 Idempotency·Inbox를 확인한다.
4. Replay는 승인·범위·시작점·종료점·Audit를 기록한다.
5. 업무 원장과 Consumer Result를 대사한다.

### 16.3 Instance Crash·OOM·Stuck

- 마지막 Operation·Lease·Fencing Token
- Thread Dump·Heap·GC·Container Exit Code
- Readiness·Traffic 제외 여부
- 재기동 후 과거 Owner가 상태를 덮어쓰지 않는지 확인

### 16.4 Network·TLS

DNS, TLS Chain, Clock, Connection Reset, Latency, Half-open, Packet Loss를 분리한다. Toxiproxy Fault를 사용했다면 적용·제거 시간을 기록한다.

### 16.5 Disk·Memory 임계

신규 작업 수락을 제한하고 정확한 대상 파일·보존기간만 정리한다. `docker system prune`, `docker volume prune`, Repository 전체 미추적 파일 삭제를 사용하지 않는다.

### 16.6 설정 부분 적용

Target별 Version·Checksum을 수집하고 성공 Target을 다시 변경하지 않는다. NACK 원인을 제거한 뒤 실패 Target만 Reconcile하거나 전체를 LKG로 되돌린다.

## 17. DR

- 전환 조건·승인자·선언 시각
- DNS·Route·Certificate·Secret 전환
- DB·Broker·Object/File Restore 순서
- Scheduler·Worker 중복 기동 차단
- Operation·Outbox·Receipt·Batch Metadata 대사
- RPO·RTO Actual 기록
- 원복 조건과 Data Reconciliation

## 18. Docker 개발·시험 환경

상세 문서: [`cpf-docs/environment/docker/`](../environment/docker/README.md)

기본 원칙:

- Container 자동 시작 금지, `restart: "no"`
- 필요한 서비스만 시작
- 작업 종료 시 이번 작업에서 시작한 서비스만 중지
- 사용자 DB·Volume·Image·Secret 임의 삭제 금지
- Runtime Source와 실행본 Hash 비교
- Container Health와 Product Consumer 시험을 분리

## 19. 플랫폼 운영 EDU

### EDU-OPS-01 — 신규 설치와 정상 종료

1. 별도 QA DB와 Secret Root를 준비한다.
2. Artifact·Hash·Manifest를 확인한다.
3. DB Fresh Install·Verify를 실행한다.
4. 필요한 Docker Service만 기동한다.
5. Runtime을 기동하고 Readiness·Synthetic Probe를 확인한다.
6. Log·Metric·Trace·Audit 상관 식별자를 확인한다.
7. 신규 작업 수락을 중지하고 Drain 후 종료한다.
8. Running CPF Container 0과 Volume 보존을 확인한다.

### EDU-OPS-02 — 응답 유실과 결과 대사

1. 외부 Provider 지연 또는 연결 Reset을 주입한다.
2. 호출 Timeout과 Operation 상태를 확인한다.
3. 신규 요청을 만들지 않고 Attempt·Outbox·Target 결과를 대사한다.
4. `UNKNOWN_RESULT` 해소 후 업무 건수·Audit를 확인한다.

### EDU-OPS-03 — 부분 적용과 Rollback

1. 다중 Target 중 하나에 잘못된 Config를 적용해 NACK를 재현한다.
2. Traffic 확대를 중지한다.
3. Target별 Version·Checksum을 수집한다.
4. 실패 Target Reconcile 또는 LKG Rollback을 수행한다.
5. Drift 0과 업무 Probe를 확인한다.

직접 실행한 환경·명령·Exit Code·시작/종료 시각·Sanitized Evidence Hash를 남긴다.

### 19.4 플랫폼 운영 EDU 15개 전수표

`EDU-OPS-01~15`는 전체 135개 EDU 체계의 플랫폼 운영 영역이다. 일부 항목은 격리 Process Script로 실행할 수 있지만, 격리 실행 성공을 실제 DB·Broker·배포·DR·보안 사고 시험으로 대체하지 않는다.

| 교육 ID | 확인할 기능 | 역할 | Source 판정 | Runtime 판정 |
|---|---|---|---|---|
| `EDU-OPS-01` | 신규 환경 설치·Artifact·Checksum 검증 | `CPF_PLATFORM_OPERATOR` | Catalog·Process Consumer·Script·Evidence 전수 대조 재확인 필요 | 미검증 |
| `EDU-OPS-02` | Profile·환경변수·Property 전체 검증 | `CPF_PLATFORM_OPERATOR` | Catalog·Process Consumer·Script·Evidence 전수 대조 재확인 필요 | 미검증 |
| `EDU-OPS-03` | Secret·Certificate 배포·Rotation·만료 대응 | `CPF_PLATFORM_OPERATOR` | Catalog·Process Consumer·Script·Evidence 전수 대조 재확인 필요 | 미검증 |
| `EDU-OPS-04` | DB 3종 Fresh·Migration·Drift·Rollback | `CPF_PLATFORM_OPERATOR` | Catalog·Process Consumer·Script·Evidence 전수 대조 재확인 필요 | 미검증 |
| `EDU-OPS-05` | 메시지 Broker Topic·ACL·Consumer Group Lifecycle | `CPF_PLATFORM_OPERATOR` | Catalog·Process Consumer·Script·Evidence 전수 대조 재확인 필요 | 미검증 |
| `EDU-OPS-06` | 기동·종료·Health·의존 대상 순서 | `CPF_PLATFORM_OPERATOR` | Catalog·Process Consumer·Script·Evidence 전수 대조 재확인 필요 | 미검증 |
| `EDU-OPS-07` | Rolling 배포·Session·Connection Drain | `CPF_PLATFORM_OPERATOR` | Catalog·Process Consumer·Script·Evidence 전수 대조 재확인 필요 | 미검증 |
| `EDU-OPS-08` | Blue-Green·Canary 전환·Rollback | `CPF_PLATFORM_OPERATOR` | Catalog·Process Consumer·Script·Evidence 전수 대조 재확인 필요 | 미검증 |
| `EDU-OPS-09` | Config 변경 Partial Apply·Reconciliation | `CPF_PLATFORM_OPERATOR` | Catalog·Process Consumer·Script·Evidence 전수 대조 재확인 필요 | 미검증 |
| `EDU-OPS-10` | Log·Metric·Trace 수집 장애·Retention·Capacity | `CPF_PLATFORM_OPERATOR` | Catalog·Process Consumer·Script·Evidence 전수 대조 재확인 필요 | 미검증 |
| `EDU-OPS-11` | Backup·Restore·Point-in-time Recovery·대사 | `CPF_PLATFORM_OPERATOR` | Catalog·Process Consumer·Script·Evidence 전수 대조 재확인 필요 | 미검증 |
| `EDU-OPS-12` | DR 전환·복귀·Split-brain 방지 | `CPF_PLATFORM_OPERATOR` | Catalog·Process Consumer·Script·Evidence 전수 대조 재확인 필요 | 미검증 |
| `EDU-OPS-13` | Disk·Memory·Network·DB 장애 Runbook | `CPF_PLATFORM_OPERATOR` | Catalog·Process Consumer·Script·Evidence 전수 대조 재확인 필요 | 미검증 |
| `EDU-OPS-14` | 보안 사고·계정·Key·Session 긴급 차단 | `CPF_PLATFORM_OPERATOR` | Catalog·Process Consumer·Script·Evidence 전수 대조 재확인 필요 | 미검증 |
| `EDU-OPS-15` | Upgrade·DB 호환·Application Rollback | `CPF_PLATFORM_OPERATOR` | Catalog·Process Consumer·Script·Evidence 전수 대조 재확인 필요 | 미검증 |

전수 판정 절차:

1. `cpf-reference/src/main/scripts/edu/invoke-reference-edu.ps1`의 허용 ID와 기능 Catalog를 대조한다.
2. Process Consumer가 생성한 결과 경로와 SHA-256을 확인한다.
3. 실제 DB·Broker·Container·Application Instance를 사용한 시험은 별도 실행한다.
4. Backup·Restore·DR·Security 항목은 복원 후 업무 합계·Version·Permission·Audit까지 대사한다.
5. 실행하지 않은 환경은 `미검증`으로 유지한다.

## 20. 운영 인계표

```text
Git Commit / Artifact Version / SHA-256 / SBOM
Topology / Instance / Port / DNS / TLS
Property Version / Environment Variable / Profile
Secret Reference / Certificate Expiry
DB Vendor / Migration Version / Drift / Backup ID
Broker / Destination / Consumer / Retry / DLQ
Health / Readiness / Synthetic Probe
Log / Metric / Trace / Audit Dashboard
Capacity / Threshold / Alert Owner
Deployment / Rollback / LKG
Fault / DR Runbook
미검증 / 제한사항 / 재확인 필요
```

## 21. 현재 상태와 Owner 작업 요청

| ID | 항목 | 판정 | Owner 작업 |
|---|---|---|---|
| `PLAT-001` | Repository 전체 Property Catalog 자동 추출·Consumer 대조 | 재확인 필요 | Config Source와 Generated Catalog Gate 제공 |
| `PLAT-002` | 3 Vendor Fresh·Upgrade·Rollback·Restore 실행 | 미검증 | Vendor별 Runtime Evidence 생성 |
| `MSG-001` | RabbitMQ·JMS·IBM MQ Product Starter·Consumer | 미구현 | Starter·Consumer·Operations·Fault Test 동시 구현 |
| `DOCKER-001` | 신규 Module 전체 검증 Runtime | 부분 구현 | 실제 Source가 생긴 Provider부터 증분 편입 |
| `OPS-001` | Rolling·Blue-Green·Canary 다중 Instance 실행 | 미검증 | 최신 SHA에서 Fault·Rollback Scenario 실행 |

## 22. 완료 점검표

- [ ] 기준 Commit과 Artifact Hash가 기록됐다.
- [ ] Property·환경변수·Profile·Secret Catalog가 실제 Consumer와 일치한다.
- [ ] DB Vendor별 설치·변경·대사·Restore가 확인됐다.
- [ ] 기동·Readiness·Synthetic Probe·종료 결과가 기록됐다.
- [ ] Target별 배포·Config ACK와 Partial Apply 처리가 확인됐다.
- [ ] Log·Metric·Trace·Audit가 같은 Operation을 가리킨다.
- [ ] Backup·Restore·Upgrade·Rollback·DR 절차가 실행 기록과 연결된다.
- [ ] 직접 실행하지 않은 항목은 `미검증`으로 남았다.
