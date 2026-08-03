# CPF Starters 매뉴얼


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


## 1. 선택 원칙

고객 개발자는 6개 공개 Profile을 선택한다. 7개 Capability Group의 내부 Provider Artifact는 Generator Binding과 Resolved Lock으로 결정하며 직접 조합하지 않는다.

## 2. 공개 Profile

| Profile | Artifact | Config Prefix | 적용 결과 |
|---|---|---|---|
| minimal-domain | `cpf-starter-profile-minimal-domain` | `cpf.profile.minimal-domain` | 최소 Domain Foundation |
| web-api | `cpf-starter-profile-web-api` | `cpf.profile.web-api` | Web API/OpenAPI |
| secure-api | `cpf-starter-profile-secure-api` | `cpf.profile.secure-api` | 인증 API |
| browser-bff | `cpf-starter-profile-browser-bff` | `cpf.profile.browser-bff` | Session/BFF |
| event-service | `cpf-starter-profile-event-service` | `cpf.profile.event-service` | Messaging/Reliability |
| batch-service | `cpf-starter-profile-batch-service` | `cpf.profile.batch-service` | Batch/Scheduler/Worker |

## 3. Capability Group과 내부 Artifact

### Data

- `cpf-starter-data-persistence-jdbc`
- `cpf-starter-data-persistence-mybatis`
- `cpf-starter-data-cache-caffeine`
- `cpf-starter-data-cache-valkey`

### Messaging

- `cpf-starter-messaging-reliability-jdbc`
- `cpf-starter-messaging-kafka`
- `cpf-starter-messaging-rabbitmq`
- `cpf-starter-messaging-jms`
- `cpf-starter-messaging-ibm-mq`

### Integration

- `cpf-starter-integration-http-client`
- `cpf-starter-integration-resilience`
- `cpf-starter-integration-tcp`
- `cpf-starter-integration-fixedlength-core`
- `cpf-starter-integration-fixedlength`
- `cpf-starter-integration-iso8583`

### File

- Attachment, Archive, Tabular POI, SFTP 내부 Artifact

### Notification

- Notification Runtime, Email Provider, SMS SPI/Provider Binding

### Security

- Resource Server, Session JDBC, Service Identity, Secret 내부 Artifact

### Platform Operations

- Observability, OTLP, Runtime Control, Channel Registry, Feature Flag 내부 Artifact

정확한 Artifact ID/Owner Path/Prefix는 `cpf-tools/generator/contracts/cpf-starter-catalog.json`이 정본이다.

## 4. 적용 절차

1. 업무 목적에 맞는 Profile을 선택한다.
2. Capability Binding과 Provider를 선택한다.
3. Generator Dry Run을 실행한다.
4. `resolved-starter-lock.json`에서 내부 Artifact와 Version을 확인한다.
5. Dependency Graph에서 선택하지 않은 Provider가 없는지 확인한다.
6. Application Context, Health, Public API Consumer Test를 실행한다.
7. Artifact Catalog/SBOM에서 Runtime Classpath를 확인한다.

## 5. 활성·비활성 조건

Profile/Provider는 Property, Classpath, Single Binding, 필수 Bean과 Secret 조건이 충족될 때 활성화된다. Default Provider가 없거나 둘 이상이면 Fail-closed한다. 선택하지 않은 기능은 Bean, SQL, Health, Secret 요구가 없어야 한다.

## 6. Aggregate·Leaf 규칙

Profile은 전이 Dependency만 제공하고 고유 업무 Bean을 갖지 않는다. 내부 Provider는 하나의 Owner와 AutoConfiguration을 가진다. Security/Cache의 모든 Provider를 동시에 끌어오던 공개 Aggregate는 사용하지 않는다.

## 7. Provider 교체

예: Kafka → RabbitMQ

1. 업무 Java Source diff가 없는지 확인한다.
2. Binding과 Resolved Lock을 변경한다.
3. 기존 Provider Config/Secret을 제거한다.
4. 신규 Provider Contract/Runtime Test를 실행한다.
5. Outbox/Inbox/DLQ 상태 호환과 운영 화면을 확인한다.
6. Rollback 시 이전 Lock/Config를 복원한다.

## 8. Migration

Legacy Artifact ID는 Catalog의 `legacyArtifactIds`로 추적한다. 예:

- `cpf-starter-base` → `cpf-starter-foundation-base`
- `cpf-starter-persistence-jdbc` → `cpf-starter-data-persistence-jdbc`
- `cpf-starter-http-client` → `cpf-starter-integration-http-client`

수동 문자열 치환만 하지 않고 settings, BOM, Catalog, Generator, Consumer, Test, 문서를 함께 변경한다.

## 9. Health·Metric·Log

Health는 실제 Dependency 상태를 반영한다. Provider별 Connection, Queue/Topic, DB, Secret, Endpoint 상태와 Degraded/Unknown 원인을 표시한다. Metric은 Operation/Binding/Provider별 성공·실패·UNKNOWN·Latency·Retry를 제공한다.

## 10. 제거 절차

1. 실제 Consumer가 없는지 확인한다.
2. Profile/Binding에서 제거한다.
3. Resolved Lock을 재생성한다.
4. Config/Secret/SQL/Health/Metric 참조를 제거한다.
5. Dependency Graph와 SBOM에서 Artifact가 사라졌는지 확인한다.
6. Runtime Smoke Test 후 Rollback Lock을 보존한다.

## 11. 충돌 조합

- 동일 Capability의 Default Provider 2개
- `browser-bff`와 Resource Server 정책의 무승인 혼합
- Local Cache와 Distributed Lock을 동일 의미로 사용
- Profile가 포함한 내부 Artifact를 Consumer가 다시 직접 선언
- 제거된 AOP/Validation 얇은 Starter를 재도입

## 12. 검증

```powershell
./gradlew.bat checkQa39CanonicalStarterClosure checkCpfProviderConformance --no-daemon
./gradlew.bat checkQa39FinalCanonical checkQa39NamingSteering --no-daemon
./gradlew.bat dependencies --configuration runtimeClasspath
```
