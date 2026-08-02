# CPF Starters 매뉴얼 — 필요한 실행 기능을 선택하고 검증하는 절차

> **주 독자**: 업무 개발자, 플랫폼 개발자, 아키텍트, 빌드·배포 담당자, 검수자
> **완료 결과**: 필요한 Starter만 선택하고 Build·설정·Runtime·시험·운영 인계를 확인하며, 선택하지 않은 기술이 전이되지 않았음을 판정한다.

## 문서 기준

- Repository: `freeangelsun/202412_01_CPF`
- Branch: `master`
- 기준 Commit: `3b600702502e53877e30cbac594987b371e2186b` (`20260802_08`)
- 활성 개발 요구: `cpf-docs/work/current/CPF_QA38_FINAL_DEVELOPMENT_REQUIREMENTS.md`
- 실제 Source·SQL·API·Config·Frontend·Script·Test가 설명보다 우선한다.
- 실행하지 않은 Build·DB·Runtime·Browser·다중 인스턴스·장애 시험은 `미검증`이다.


## 1. CPF Starter의 역할

CPF Starter는 특정 실행 기술을 CPF 공개 계약에 연결하는 **선택형 라이브러리**다. 독립 서버가 아니며 Starter를 선택한 업무 서비스, ADM, BZA, Gateway 또는 Batch 실행 파일에 포함된다.

Starter가 소유하는 범위:

- Provider Runtime과 자동 설정
- Typed Configuration과 Validation
- CPF 공개 API·SPI 연결
- 오류 분류와 실패 경계
- Health·Metric·Trace·구조화 Log
- 운영 조회·조치 연결
- Provider별 Test와 Artifact Metadata

Starter가 소유하지 않는 범위:

- 업무 상태와 업무 원장
- 조직별 승인·보상·대사 정책
- Product Owner의 Route·Batch·결재 원장
- 업무 화면과 업무 데이터 의미

## 2. 처음 사용하는 개발자의 선택 순서

1. 만들 업무 결과와 필요한 실행 기능을 적는다.
2. 동일 기능의 Provider가 여러 개인지 확인한다.
3. 현재 등록된 Leaf Starter와 QA38 목표 Starter를 구분한다.
4. 현재 사용할 수 있는 Starter만 Build에 선언한다.
5. `@ConfigurationProperties` Source에서 실제 Key·Type·Default·Validation을 확인한다.
6. 필요한 Bean·Client·Provider가 하나만 활성화되는지 확인한다.
7. 선택하지 않은 Starter와 Provider JAR이 Runtime Classpath에 없는지 확인한다.
8. Unit·Contract·Integration·Negative Test를 실행한다.
9. JAR·POM·SBOM·Sources·JavaDoc를 확인한다.
10. 운영 담당자에게 설정·Secret·Health·Metric·Rollback을 인계한다.

## 3. 기준 Commit의 공개 Starter

`settings.gradle`에 등록된 공개 Starter는 다음 7개다.

| Gradle 프로젝트 | 물리 경로 | 현재 직접 전이 | 현재 판정 |
|---|---|---|---|
| `:cpf-starter-security` | `cpf-starters/security` | Core, Spring Security, JDBC, Session JDBC | 부분 구현 |
| `:cpf-starter-messaging-kafka` | `cpf-starters/messaging-kafka` | Core, Spring Kafka | 부분 구현 |
| `:cpf-starter-cache` | `cpf-starters/cache` | Common, Caffeine, Redis | 부분 구현 |
| `:cpf-starter-observability` | `cpf-starters/observability` | Core, Micrometer, OTel Bridge·Exporter | 부분 구현 |
| `:cpf-starter-resilience` | `cpf-starters/resilience` | Core, Resilience4j Circuit Breaker | 부분 구현 |
| `:cpf-starter-featureflag` | `cpf-starters/featureflag` | Core, OpenFeature SDK | 부분 구현 |
| `:cpf-starter-secret` | `cpf-starters/secret` | Core, AutoConfiguration | 부분 구현 |

`cpf-starters/`는 물리 컨테이너이며 Gradle 프로젝트 자체가 아니다. `:cpf-starters:security`처럼 경로를 프로젝트 이름으로 사용하지 않는다.

## 4. 현재 사용 가능한 등록 방법

### 4.1 같은 Root Build에서 사용

```groovy
implementation project(':cpf-starter-security')
implementation project(':cpf-starter-observability')
```

### 4.2 게시 Artifact로 사용

```groovy
implementation platform('com.cpf:cpf-platform-bom:<platform-version>')
implementation 'com.cpf.starter:cpf-starter-security'
implementation 'com.cpf.starter:cpf-starter-observability'
```

BOM은 Version만 정렬한다. BOM을 선언했다고 Starter 기능이 포함되거나 활성화되는 것은 아니다.

## 5. 현재 Starter별 사용 기준

### 5.1 Security

사용 시점:

- Browser Session과 BFF 보안
- JDBC Session 또는 Resource Server 연결
- 인증 주체·Permission·Data Scope를 CPF 계약에 전달

현재 주의:

- 기준 Commit의 단일 Security Starter가 Resource Server와 Session JDBC 책임을 함께 전이한다.
- QA38 목표는 `security-resource-server`, `security-session-jdbc`, `security-service-identity` 분리다.
- Session DB와 암호화 Key가 필요하지 않은 서비스에 JDBC Session이 강제되지 않는지 확인한다.

정상 판정:

- 인증이 없거나 잘못된 Audience이면 Fail-closed
- Frontend 숨김과 별도로 Backend Permission 검증
- Session·Credential·Secret 원문이 Log·Audit에 없음

### 5.2 Messaging

사용 시점:

- 비동기 메시지 발행·소비
- Outbox·Inbox·중복 소비 방지
- ACK·Retry·Dead Letter·Replay 운영

현재 주의:

- 기준 Commit에는 Kafka Provider Starter만 등록돼 있다.
- RabbitMQ·Jakarta JMS·IBM MQ는 QA38 목표이며 현재 Gradle 좌표로 안내하지 않는다.
- Provider 이름을 상위 제품 설명에 고정하지 않고 Build·Config·운영 절차에서만 명시한다.

정상 판정:

- Producer ACK와 업무 Transaction 경계가 정의됨
- Consumer Dedup과 실패 보관·재처리 경로가 있음
- Process 종료·Rebalance·응답 유실 후 결과를 대사할 수 있음

### 5.3 Cache

사용 시점:

- Process Local Cache
- 분산 Cache·Invalidation·Lock

현재 주의:

- 기준 Commit의 하나의 Starter가 Caffeine과 Redis Runtime을 함께 전이한다.
- QA38 목표는 Provider별 Leaf Starter 분리와 `cpf-common`의 기술 Runtime 역전이 제거다.

정상 판정:

- Provider 하나만 활성화
- Key Namespace·TTL·Negative Cache·Invalidation 계약 존재
- 원본 조회 실패와 Cache 장애 시 업무 결과가 명확함

### 5.4 Observability

사용 시점:

- 공통 Trace·Metric·Log Correlation
- OTLP Export가 필요한 Runtime

현재 주의:

- Provider-neutral 관측 계약과 OTLP Exporter Runtime 분리가 목표다.
- Collector 장애가 업무 Transaction을 임의 실패시키거나 민감정보를 유출하지 않는지 확인한다.

### 5.5 Resilience

사용 시점:

- 원격 호출 Deadline·Circuit Breaker·Bulkhead
- 제한된 Retry와 결과 미확정 처리

정상 판정:

- Local Transaction에 Circuit·Retry를 오용하지 않음
- 전체 Deadline 안에서 하위 Timeout이 배분됨
- 비멱등 요청은 Attempt Ledger 없이 Retry하지 않음

### 5.6 Feature Flag

사용 시점:

- 기능 단계 적용·중지·대상 구간 평가

현재 주의:

- 실제 Provider·Consumer·변경 승인·감사 연결을 확인한 뒤 사용한다.
- Provider가 없을 때 Default 동작과 Fail-closed 범위를 명시한다.

### 5.7 Secret

사용 시점:

- Secret Provider Registry
- Rotation·Revocation·Health 확인

정상 판정:

- Provider가 필요한 Profile에서 Provider 0개면 기동 실패 또는 기능 Fail-closed
- Secret 원문이 Property Dump·Log·Metric·Evidence에 없음
- Rotation 전후 연결과 Rollback 절차가 있음

## 6. QA38 목표 Starter 구조

다음은 목표 구조이며 기준 Commit에서 모두 사용할 수 있는 상태가 아니다.

```text
cpf-starter-base
cpf-starter-persistence-jdbc
cpf-starter-persistence-mybatis
cpf-starter-aop-service-access
cpf-starter-openapi-webmvc
cpf-starter-http-client
cpf-starter-validation
cpf-starter-tabular-poi
cpf-starter-file-archive
cpf-starter-security-resource-server
cpf-starter-security-session-jdbc
cpf-starter-security-service-identity
cpf-starter-observability
cpf-starter-observability-otlp
cpf-starter-resilience
cpf-starter-secret
cpf-starter-featureflag
cpf-starter-messaging-reliability-jdbc
cpf-starter-messaging-kafka
cpf-starter-messaging-rabbitmq
cpf-starter-messaging-jms
cpf-starter-messaging-ibm-mq
cpf-starter-channel-registry-jdbc
cpf-integration-fixedlength-core
cpf-starter-integration-fixedlength
cpf-starter-integration-tcp
cpf-starter-integration-iso8583
cpf-starter-integration-sftp
cpf-starter-notification
cpf-starter-notification-email
cpf-notification-sms-spi
cpf-starter-scheduler-quartz
```

목표 이름이 문서에 존재하는 것과 실제 Gradle Project·게시 Artifact·Consumer가 존재하는 것은 구분한다.

## 7. Capability Profile과 Aggregate Starter

### 7.1 목표

Capability Profile은 업무 요구를 승인된 Leaf Starter 목록으로 해석한다.

```yaml
profile: DOMAIN_EVENT_RABBITMQ
profileVersion: 1
bindings:
  default:
    provider: rabbitmq
```

생성 결과에는 최소한 다음이 있어야 한다.

```json
{
  "profile": "DOMAIN_EVENT_RABBITMQ",
  "profileVersion": 1,
  "resolvedStarters": [
    "cpf-starter-base",
    "cpf-starter-messaging-reliability-jdbc",
    "cpf-starter-messaging-rabbitmq",
    "cpf-starter-observability"
  ],
  "starterVersionLock": "<hash>"
}
```

### 7.2 현재 판정

기준 Commit의 생성 도구는 `Capabilities`, `Messaging`, `External`, `File`, `SecurityAudit` 등의 입력을 받지만 Versioned Profile·`resolvedStarters`·Version Lock을 제공하는 상태는 확인되지 않았다. 현재는 필요한 공개 Starter를 개별 선언한다.

### 7.3 Aggregate Starter 규칙

Aggregate Starter는 안정된 Leaf 조합의 전이 Dependency만 제공한다.

금지:

- 고유 Bean·AutoConfiguration
- 업무 정책
- 모든 기능을 포함하는 `all`, `full`, `everything` 묶음
- 개별 Leaf 선택을 막는 구조

## 8. 설정 확인 절차

1. Starter의 `@ConfigurationProperties` Class를 찾는다.
2. Prefix·Key·Type·Default·Validation을 기록한다.
3. 환경변수 Binding을 확인한다.
4. Secret 값인지 구분한다.
5. Profile별 활성 조건을 확인한다.
6. 설정 누락·잘못된 값·복수 Provider 충돌을 시험한다.
7. 재기동 필요 여부와 Rollback 값을 운영 인계에 기록한다.

Source에 없는 Property 이름을 매뉴얼 예제로 만들지 않는다.

## 9. AutoConfiguration 검증

확인 항목:

- 조건이 충족될 때만 Bean 생성
- 사용자 Bean이 허용되는 위치에서 Backoff
- 필수 Provider가 없으면 Fail-closed
- 같은 Capability의 Primary Bean이 하나
- 선택하지 않은 Provider Bean이 없음
- AutoConfiguration Report로 활성·비활성 이유 확인

## 10. 의존성·제거 시험

```powershell
$repo='C:\dev\projects\jck\202412_01_CPF'
& (Join-Path $repo 'gradlew.bat') :<consumer-project>:dependencies --configuration runtimeClasspath
& (Join-Path $repo 'gradlew.bat') :<consumer-project>:dependencyInsight --dependency <artifact>
```

검증:

- 선택한 Starter와 Provider만 포함
- 제외한 Starter의 JAR·AutoConfiguration·Property·SQL 없음
- Starter 제거 후 Compile 또는 기능 Test가 예상한 방식으로 실패하거나 대체 경로로 동작
- 순환 의존과 Product→Starter 역방향 의존 없음

## 11. Test Matrix

| 계층 | 필수 확인 |
|---|---|
| Unit | Property Validation·Mapping·Error Class |
| AutoConfiguration | 조건·Backoff·Fail-closed·복수 Provider 충돌 |
| Contract | CPF API/SPI 의미와 Provider Mapping |
| Integration | 실제 Provider 연결·인증·권한·Timeout |
| Negative | 누락 설정·잘못된 Secret·Provider Down |
| Multi-instance | Claim·Lease·Fencing·동시 실행 |
| Fault | Process Kill·Network Loss·응답 유실·부분 실패 |
| Operations | Health·Metric·Trace·Audit·재처리·대사 |
| Packaging | JAR·POM·BOM·Sources·JavaDoc·SBOM |
| Compatibility | Upgrade·Rollback·Mixed Version |

실행하지 않은 계층은 `미검증`으로 남긴다.

## 12. Artifact와 배포 확인

각 Starter별 확인:

- Artifact ID와 Group
- Version
- POM Dependency Scope
- BOM 정렬
- Sources JAR
- JavaDoc JAR
- SBOM
- License·취약점 결과
- Checksum
- 게시 Repository
- Consumer Build 재현

LOCAL_DEV·REMOTE·OFFLINE 모드에서 같은 Version 계약이 유지되는지 확인한다.

## 13. 운영 인계표

| 항목 | 기록 내용 |
|---|---|
| Starter·Version | 실제 좌표·Commit |
| Consumer | Module·Artifact |
| Provider | 이름·Version·Topology |
| Config | Key·Default·필수·범위 |
| Secret | Reference·Rotation·권한 |
| Health | Liveness·Readiness·Dependency |
| Metric·Trace | 이름·Label·Alert |
| Failure | Timeout·Retry·결과 미확정 |
| Operations | 조회·재처리·대사·Rollback |
| Test | 실행 명령·환경·결과 |
| 미검증 | 남은 Runtime·Fault 조건 |

## 14. 현재 상태와 개발 검토 요청

| 항목 | 개발 상태 | 검증 상태 | 검토 요청 |
|---|---|---|---|
| 공개 7개 Starter 등록 | 완료 | 전체 게시·소비 미검증 | 동일 Version Publish/Consume Gate |
| Core·Common 경량화 | 부분 구현 | 미검증 | 선택 Runtime Owner 이동 |
| Security·Cache·Observability 분리 | 부분 구현 | 미검증 | Provider별 Leaf와 제거 시험 |
| Capability Profile | 미구현 | 미검증 | Versioned Profile·resolved lock |
| Aggregate Starter | 미구현 | 미검증 | 승인 조합만 구현 |
| 복수 Messaging Provider | 미구현 | 미검증 | RabbitMQ·JMS·IBM MQ·실제 Consumer |
| TCP·SFTP·Notification | 미구현 또는 재확인 필요 | 미검증 | Source·Consumer·Docker·Fault Closure |

## 15. 완료 점검표

- [ ] 업무 결과에서 필요한 Capability를 선택했다.
- [ ] 현재 Starter와 목표 Starter를 구분했다.
- [ ] Build와 Runtime Classpath가 일치한다.
- [ ] 실제 Property·Default·Validation을 Source에서 확인했다.
- [ ] 복수 Provider 충돌이 Fail-closed한다.
- [ ] 선택하지 않은 기술이 전이되지 않는다.
- [ ] 실제 Consumer가 있다.
- [ ] 정상·오류·부분 실패·Process Kill·대사를 시험했다.
- [ ] JAR·POM·BOM·Sources·JavaDoc·SBOM을 확인했다.
- [ ] Upgrade·Rollback과 운영 인계를 기록했다.

## 16. Starter 설치·적용 절차

### 16.1 같은 Root Build에서 적용

1. `settings.gradle`에 실제 논리 프로젝트가 등록돼 있는지 확인한다.
2. Consumer의 `build.gradle`에 필요한 Starter만 선언한다.
3. `dependencies`와 `dependencyInsight`로 전이 의존성을 확인한다.
4. 필수 Property·Secret·Provider를 준비한다.
5. AutoConfiguration Report와 Bean 수를 확인한다.
6. 실제 Provider Contract Test를 실행한다.
7. Starter 제거 후 미선택 기술이 Classpath·Bean·Property·SQL에 남지 않는지 확인한다.

```powershell
$repo='C:\dev\projects\jck\202412_01_CPF'
& (Join-Path $repo 'gradlew.bat') projects
& (Join-Path $repo 'gradlew.bat') :<consumer-project>:dependencies --configuration runtimeClasspath
& (Join-Path $repo 'gradlew.bat') :<consumer-project>:dependencyInsight --dependency <artifact>
```

### 16.2 게시 Artifact로 적용

1. Platform BOM Version을 고정한다.
2. 승인된 Repository와 Credential Reference를 설정한다.
3. POM·Checksum·SBOM을 확인한다.
4. Consumer의 Lock·Resolved Dependency를 기록한다.
5. Offline Repository에서도 같은 Artifact가 해석되는지 확인한다.

### 16.3 적용 실패

| 실패 | 원인 후보 | 확인 |
|---|---|---|
| Project Not Found | `settings.gradle` 미등록·논리명 오류 | `gradlew projects` |
| Artifact Not Found | 게시 누락·Repository Mode 오류 | Artifact Mode·URL·Version |
| Bean 없음 | Condition·Property·Provider 누락 | AutoConfiguration Report |
| Bean 중복 | 복수 Provider·Primary 충돌 | Bean Definition·Named Binding |
| Runtime Class 없음 | POM Scope·전이 의존성 누락 | `runtimeClasspath` |
| 운영 기동 실패 | Secret·DB·Broker·TLS 누락 | Readiness·Failure Class |

## 17. Starter별 Consumer 검증 규칙

Starter가 존재해도 실제 Consumer가 없으면 제품 기능으로 완료 처리하지 않는다.

```text
Starter Project
→ Public AutoConfiguration·Properties
→ Provider Adapter
→ Consumer build.gradle
→ Consumer Config
→ 정상·오류·Fault Test
→ Health·Metric·Trace·Audit
→ 제거 시험
```

Consumer 검증표에는 Module, 사용 기능, Binding Name, Provider, Property Prefix, Test 경로, 운영 확인 방법을 기록한다.

## 18. Generator와 Starter 선택 연결

Generator가 업무 영역을 만들 때 선택한 Capability와 실제 `build.gradle` 의존성이 일치해야 한다. 생성 계획에는 요청 Capability, 해석된 Starter, Version, 제외된 Provider, 필요한 Property·Secret을 기록하고, 생성 후에는 `runtimeClasspath`와 Resolved Dependency Lock으로 결과를 확인한다.

기준 Commit의 생성 도구가 Capability Profile이나 Aggregate Starter를 실제로 펼치지 못하면 다음과 같이 처리한다.

1. Generator Dry Run 결과에 자동 선택된 것으로 기록하지 않는다.
2. 현재 등록된 Leaf Starter를 업무 개발자가 명시적으로 선택한다.
3. 선택 이유와 Consumer를 개발 인계표에 기록한다.
4. 자동 해석 기능은 `미구현`으로 남기고 별도 개발 요청으로 전달한다.

## 19. Starter EDU — 선택·적용·제거

1. 기능 요구에서 필요한 Capability를 하나 선택한다.
2. 기준 Commit에 실제 Starter가 있는지 확인한다.
3. Consumer에 Starter를 추가한다.
4. 필수 Property·Secret을 누락시켜 Fail-closed를 확인한다.
5. 정상 Provider를 연결하고 Contract Test를 실행한다.
6. Provider를 중단해 Timeout·Retry·Metric을 확인한다.
7. Starter를 제거하고 Classpath·Bean·Config·SQL이 함께 제거되는지 확인한다.
8. POM·BOM·SBOM·Checksum과 운영 인계표를 작성한다.

## 20. 개발 검토 요청 조건

- Starter 이름은 있으나 Gradle 프로젝트·게시 좌표가 없다.
- AutoConfiguration이 무조건 활성화된다.
- 복수 Provider가 동시에 Primary로 등록된다.
- Property가 문서와 Source에서 다르다.
- Provider SDK가 Core·Common 또는 업무 Domain에 직접 노출된다.
- 실제 Consumer·Fault Test·Operations가 없다.
- Starter 제거 후 관련 기술 의존성·Bean·SQL이 남는다.
- Capability Profile이 실제 Leaf 목록·Version Lock을 생성하지 않는다.

발견 시 Starter·Consumer·Dependency Graph·Property·Bean Report·실행 결과를 `산출물목록.md`의 개발 검토 항목으로 전달한다.
