# CPF Starter Quick Select

> 처음에는 **Profile 하나 + 필요한 Provider만** 고릅니다. Internal Starter는 직접 선택하지 않습니다.

## 1. 시작점 선택

| 하고 싶은 일 | 먼저 선택 | 설명 |
|---|---|---|
| 일반 Web API | `cpf-starter-web-api` | 일반 업무 API 기본 Profile |
| 인증·인가가 필요한 Web API | `cpf-starter-secure-api` | Security가 포함된 API Profile |
| BFF | `cpf-starter-bff` | UI/BFF 용도 Profile |
| Event 중심 서비스 | `cpf-starter-event` | Messaging/Event 중심 Profile |
| Batch | `cpf-starter-batch` | Batch Runtime 개발 Profile |

공통코드·메시지·파라미터·영업일 기능이 필요하면 `cpf-starter-common`을 사용합니다. Provider는 아래에서 필요한 것만 추가합니다.

## 2. Public Starter 전체

### Data

| Level | Artifact | 언제 선택 | 기본 선택? |
|---|---|---|---|
| capability | `cpf-starter-cache-caffeine` | 단일 인스턴스 로컬 Cache | 아니오 |
| capability | `cpf-starter-cache-redis` | Redis 기반 다중 인스턴스 Cache | 아니오 |
| capability | `cpf-starter-cache-valkey` | Valkey 기반 다중 인스턴스 Cache | 아니오 |
| capability | `cpf-starter-data-jdbc` | Spring JDBC 기반 데이터 접근 | 아니오 |
| capability | `cpf-starter-data-jpa` | JPA 기반 영속성 | 아니오 |
| capability | `cpf-starter-data-mybatis` | MyBatis 기반 SQL 업무 개발 | 아니오 |
| capability | `cpf-starter-lock-valkey` | Valkey 기반 분산 Lock | 아니오 |

### File

| Level | Artifact | 언제 선택 | 기본 선택? |
|---|---|---|---|
| capability | `cpf-starter-file-attachment` | 첨부파일 저장·조회·다운로드 기능 | 아니오 |
| capability | `cpf-starter-object-storage-s3` | S3 호환 Object Storage 사용 | 아니오 |

### Integration

| Level | Artifact | 언제 선택 | 기본 선택? |
|---|---|---|---|
| capability | `cpf-starter-integration-fixed-length` | 대외 고정길이 전문 송수신 | 아니오 |
| capability | `cpf-starter-graphql` | GraphQL Endpoint/Client 사용 | 아니오 |
| capability | `cpf-starter-realtime` | 실시간 SSE 통신 | 아니오 |

### Messaging

| Level | Artifact | 언제 선택 | 기본 선택? |
|---|---|---|---|
| capability | `cpf-starter-messaging-ibm-mq` | IBM MQ 연계 | 아니오 |
| capability | `cpf-starter-messaging-jms` | 표준 JMS Broker 사용 | 아니오 |
| capability | `cpf-starter-messaging-kafka` | Kafka Producer/Consumer 사용 | 아니오 |
| capability | `cpf-starter-messaging-rabbitmq` | RabbitMQ Producer/Consumer 사용 | 아니오 |

### Profile

| Level | Artifact | 언제 선택 | 기본 선택? |
|---|---|---|---|
| golden | `cpf-starter-batch` | Batch Runtime을 사용하는 프로젝트 구성 | 예 |
| golden | `cpf-starter-bff` | 브라우저 전용 Backend-for-Frontend | 예 |
| golden | `cpf-starter-event` | 이벤트/메시징 중심 서비스 | 예 |
| golden | `cpf-starter-secure-api` | 인증·인가가 필요한 API 서비스 | 예 |
| golden | `cpf-starter-web-api` | 일반 REST/Web API 서비스 | 예 |

### Security

| Level | Artifact | 언제 선택 | 기본 선택? |
|---|---|---|---|
| capability | `cpf-starter-oidc` | OIDC 기반 사용자 로그인 | 아니오 |
| capability | `cpf-starter-session-jdbc` | DB 기반 공유 Session | 아니오 |
| capability | `cpf-starter-session-valkey` | Valkey 기반 공유 Session | 아니오 |

### 공통

| Level | Artifact | 언제 선택 | 기본 선택? |
|---|---|---|---|
| golden | `cpf-starter-common` | 공통코드·파라미터·메시지·영업일 등 업무 공통 기능 | 예 |

### 기본

| Level | Artifact | 언제 선택 | 기본 선택? |
|---|---|---|---|
| golden | `cpf-starter` | 모든 CPF 애플리케이션의 공통 시작점 | 예 |

## 3. 선택 기준

- **golden**: 일반 개발자가 처음 보는 시작점입니다. Profile/Base/Common입니다.
- **capability**: Cache, DB, Messaging, Security, Integration처럼 필요할 때 추가합니다.
- **advanced/internal**: 이 문서에서 직접 dependency로 권장하지 않습니다.
- 같은 기능의 Provider는 운영환경에 맞는 **하나의 Public Provider**를 선택하고 Internal leaf를 직접 의존하지 않습니다.

## 4. 개발 중 검증

```text
빠른 확인       ./gradlew cpfVerifyFast
변경영향 확인   ./gradlew cpfVerifyTargeted -PcpfTargetCapabilities=cache,messaging
최종 로컬 검증  ./gradlew cpfVerifyFullLocal
```

Public API는 `CPF_PUBLIC_FUNCTION_TOP_100.md`, 전체 개발 흐름은 `CPF_DEVELOPER_GOLDEN_PATH.md`에서 이어서 확인합니다.
