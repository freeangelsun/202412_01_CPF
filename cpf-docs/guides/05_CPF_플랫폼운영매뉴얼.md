# CPF 플랫폼 운영 매뉴얼


## 문서 기준과 판정

- Repository: `https://github.com/freeangelsun/202412_01_CPF`
- Branch: `master`
- Source 기준 Commit: `e134c1f275c306c0e9ab4a044d9140ac4b3ca620`
- 최상위 목표 정본: `cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md`
- 문서 표준 정본: `cpf-docs/specification/CPF_DOCUMENTATION_STANDARD.md`
- 사용자 지시 적용: 요구사항에 정의되고 Source에 연결된 기능은 사용 가능한 제품 기능으로 설명한다.
- 이 문서 작업에서 직접 수행한 Runtime·DB·Browser·다중 인스턴스 검증: `미검증`
- 문서와 Source의 경로·식별자 정합성 검토: `완료`

> Runtime 미검증은 기능 절차를 생략하는 이유가 아니다. 다만 실행 결과를 직접 확인하지 않은 항목은 배포 승인 시 해당 환경의 Evidence로 다시 확인한다.


## 1. 지원 환경

| 항목 | 기준 |
|---|---|
| Java | `25` |
| Gradle | `9.1.0` |
| Spring Boot | `4.1.0` |
| Spring Dependency Management | `1.1.7` |
| Spring Cloud | `2025.1.2` |
| Spring Batch | `6.0.4` |
| Servlet | `6.1` |
| OpenFeature | `1.17.0` |
| db-scheduler | `16.7.0` |
| CycloneDX Gradle Plugin | `3.0.1` |

지원 DB는 Oracle, PostgreSQL, MariaDB다. Node 22는 ADM/BZA Frontend Build와 Browser Test에 사용한다.

## 2. Artifact 수령 검증

1. Artifact Catalog에서 Artifact ID와 Owner Path를 확인한다.
2. SHA-256을 검증한다.
3. CycloneDX SBOM과 License Review를 확인한다.
4. DB Pack은 Migration Checksum을 확인한다.
5. Frontend는 npm Lock과 Browser Test Evidence를 확인한다.
6. 기준 Commit과 Artifact Manifest의 SHA가 일치하는지 확인한다.

## 3. 계정·Directory

Runtime 전용 OS 계정, Log/Work/Temp/Backup Directory를 분리한다. Write 권한은 필요한 경로에만 부여한다. Secret 파일은 Source/Artifact Directory와 분리하고 ACL을 제한한다.

## 4. Config 관리

Config는 Profile, Capability Binding, Endpoint, Timeout, Pool, Security, Observability, Feature Flag로 분류한다. 각 Property는 다음을 기록한다.

| 항목 | 기록 내용 |
|---|---|
| Key/환경변수 | 정확한 이름 |
| Type/Default | Type과 기본값 |
| 필수/범위 | 환경별 필수 여부와 허용값 |
| Consumer | 실제 AutoConfiguration/Runtime |
| Profile | 적용 Profile |
| 재기동 | Dynamic 또는 Restart 필요 |
| Secret | Secret Reference 여부 |
| 오류 | Fail-fast/Degraded/Disabled |
| 확인 | Command·ADM Route·Metric |
| Rollback | 이전 Config/Version |

정확한 Prefix는 `cpf-starter-catalog.json`과 각 `@ConfigurationProperties` Source를 기준으로 추출한다.

## 5. Secret·Certificate

운영 Profile의 비밀번호·Token·Private Key는 평문 Property로 전달하지 않는다. Secret Provider Reference를 사용한다. 인증서는 만료, Chain, SAN, Key Usage를 사전 검증하고 교체 전후 동시 신뢰 기간을 둔다.

## 6. DB 설치

1. Vendor Pack과 Checksum을 확인한다.
2. Empty Schema 또는 Upgrade 기준 Version을 확인한다.
3. Backup Manifest를 생성한다.
4. Install/Migration을 적용한다.
5. Verify Query와 Object Count를 확인한다.
6. Application Readiness와 업무 Smoke Test를 수행한다.
7. Drift Scan 결과를 보존한다.

운영 데이터가 있는 환경에서 파괴적 Rollback을 직접 실행하지 않는다.

## 7. Messaging Runtime

Provider별 확인 항목:

- Kafka: Bootstrap, Topic, ACL, Consumer Group, Transaction/Idempotence
- RabbitMQ: VHost, Exchange/Queue/Binding, Publisher Confirm, Mandatory
- JMS/Artemis: Broker URL, Destination, Acknowledge/Transaction
- IBM MQ: Queue Manager, Channel, Queue, CCSID, Reason Code, TLS/CCDT

업무 Source는 동일한 CPF Messaging API를 사용하고 Provider는 Binding으로 선택한다.

## 8. 기동·종료

### 기동 순서

DB/Secret/Identity → Broker/File/External Dependency → CPF Runtime → Gateway → ADM/BZA Frontend.

Readiness는 Dependency 연결과 필수 Migration을 확인한 뒤 UP이 되어야 한다. 설정값 존재만으로 UP을 반환하지 않는다.

### 종료 순서

신규 Traffic 차단 → Drain → Worker Claim 중지 → In-flight 완료/Checkpoint → Runtime 종료 → Dependency 종료.

## 9. Health

- Liveness: Process 자체 상태
- Readiness: 요청 수락 가능 여부
- Dependency Health: DB/Broker/SFTP/OTLP 등 실제 연결
- Degraded: 일부 기능만 제한
- Unknown: 상태 확인 실패

ADM Dashboard, `/topology`, `/capacity`에서 Instance와 Dependency를 확인한다.

## 10. 배포

### Rolling

Instance별 Drain → 배포 → Readiness → Traffic 복귀. Mixed Version 호환성을 확인한다.

### Blue-Green

Green 설치·Migration 호환·Smoke → Traffic 전환 → 관찰 → Blue 보존/종료.

### Canary

대상 비율과 Tenant를 제한하고 Error, Latency, UNKNOWN, DLQ, DB 부하를 비교한다.

## 11. Config Partial Apply

Runtime Control에서 Target Snapshot과 Preview Hash를 확인한다. Apply 후 ACK/NACK와 Observed Hash를 대조한다. 일부 실패 시 성공 Target을 중복 적용하지 않고 실패 Target만 재시도하거나 LKG로 Rollback한다.

## 12. Log·Metric·Trace

필수 Metric: 요청 성공/실패/UNKNOWN, Latency, Retry, Circuit, Pool, Queue Depth, Outbox/DLQ, Worker Lease, Batch Throughput, File Transfer, Notification Delivery.

Trace Sampling과 Dynamic Log Level은 승인된 Runtime Change로 수행하고 종료 시 원복한다.

## 13. Capacity

CPU/Memory/GC/Thread/Connection Pool, Broker Lag, DB Lock/IO, Disk, File Descriptor, Network, Worker Queue를 함께 본다. 단일 Metric 임계치만으로 Scale 결정을 내리지 않는다.

## 14. Backup·Restore

Backup Manifest에 DB, Artifact, Config Hash, Secret Reference, Certificate, Migration Version, Queue/Topic 정책을 기록한다. Restore 후 Schema/Row Count, Identity/Sequence, Scheduler Lock, Outbox/Inbox, Audit, Readiness를 확인한다.

## 15. Upgrade·Rollback

Upgrade 전 호환 Matrix와 DB Forward/Backward 호환성을 확인한다. Schema 변경이 비가역이면 Rollback 대신 Forward Recovery를 준비한다. Artifact Rollback은 Config·DB·Frontend Version을 함께 맞춘다.

## 16. DR

RPO/RTO, Backup 주기, Replication, DNS/Route 전환, Broker Offset/Queue, Secret/Certificate, Audit 보존을 정의한다. Failover 후 Split Brain과 Stale Writer를 차단하고 Failback 전에 데이터 대사를 수행한다.

## 17. 장애 Runbook

### DB

Readiness 차단 → Connection/Lock/Replication 확인 → Write 차단 여부 결정 → 복구 → Transaction/Outbox 대사.

### Broker

Publish/Consume 중지 범위 확인 → Lag/Queue/ACL/TLS 확인 → UNKNOWN 보존 → 복구 후 Reconcile.

### Instance

Traffic 제거 → In-flight/Lease 확인 → Restart → Readiness → Stale Claim/Reclaim 확인.

### Disk/Memory

신규 작업 제한 → Log/Temp/Queue 증가 원인 확인 → 임의 파일 삭제 금지 → 승인된 Cleanup/Scale.

### Network

DNS/TLS/Route/Firewall/Toxiproxy 상태 확인 → Timeout Budget과 Retry 폭주 차단 → Dependency 복구 후 Reconcile.

### Certificate

만료/Chain/SAN 확인 → 신·구 인증서 동시 신뢰 → 교체 → Handshake/Client 검증 → 구 인증서 제거.

## 18. Docker 개발·시험 환경

`cpf-tools/environment/docker-development-test/**`는 필요할 때만 수동 기동한다. QA39 환경은 RabbitMQ, Artemis, IBM MQ, TCP Simulator, Mailpit, WireMock, Toxiproxy, OTel Collector를 포함하며 Container Restart Policy는 `no`다. Secret은 Repository 밖에서 공급하고 검증 종료 후 Container를 중지한다.

## 19. 운영 검증

```powershell
./gradlew.bat qualityGate --no-daemon --max-workers=1 --stacktrace
./cpf-tools/verification/qa39/invoke-qa39-final-validation.ps1 -Root . <환경별 인자>
```

Self-hosted DB/Runtime 환경이 없으면 해당 검증은 `미검증`으로 기록한다.
