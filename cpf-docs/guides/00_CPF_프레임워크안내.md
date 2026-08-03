# CPF 프레임워크 안내 — 도입과 구성 선택


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


## 1. CPF로 만들 수 있는 시스템

CPF를 사용하면 REST API 업무, 인증 API, 브라우저 BFF, 이벤트 서비스, 정기·대량 배치, 외부 전문·파일 연계, ADM/BZA 운영 기능과 Gateway를 공통 계약으로 구축할 수 있다.

## 2. 도입 판단 질문

1. 동일 기능을 Modular Monolith와 MSA에서 같은 계약으로 운영해야 하는가?
2. 중복 요청, Timeout, 응답 유실과 결과 불명을 업무 상태로 관리해야 하는가?
3. Oracle/PostgreSQL/MariaDB 중 고객 환경에 맞는 Vendor가 필요한가?
4. 운영자가 조회·조치·승인·감사·복구를 ADM에서 수행해야 하는가?
5. Provider를 Kafka/RabbitMQ/JMS/IBM MQ 사이에서 바꿀 가능성이 있는가?
6. Generator로 신규 업무 Domain을 반복 생성하고 표준 상속을 검증해야 하는가?

둘 이상이 필요하면 CPF의 Profile·Capability·운영 제품을 함께 적용한다.

## 3. 공개 Profile 선택

| Profile | 선택 상황 | 대표 결과 |
|---|---|---|
| `minimal-domain` | 최소 업무 Domain | `cpf-starters/profiles/minimal-domain` | `cpf-starter-profile-minimal-domain` |
| `web-api` | REST API와 OpenAPI | `cpf-starters/profiles/web-api` | `cpf-starter-profile-web-api` |
| `secure-api` | 인증·인가 API | `cpf-starters/profiles/secure-api` | `cpf-starter-profile-secure-api` |
| `browser-bff` | 브라우저 세션·BFF | `cpf-starters/profiles/browser-bff` | `cpf-starter-profile-browser-bff` |
| `event-service` | 메시지 기반 서비스 | `cpf-starters/profiles/event-service` | `cpf-starter-profile-event-service` |
| `batch-service` | 배치·Worker·Scheduler | `cpf-starters/profiles/batch-service` | `cpf-starter-profile-batch-service` |

`minimal-domain`에서 시작해 필요한 Profile을 하나 선택하고, Provider는 Capability Binding으로 결정한다. Profile와 내부 Provider Artifact를 동시에 직접 선언하지 않는다.

## 4. Capability 선택

| Capability | 고객이 만드는 결과 | 내부 Provider 예 |
|---|---|---|
| Data | 조회·등록·상태 변경·Cache/Lock | JDBC, MyBatis, Caffeine, Valkey |
| Messaging | 이벤트 발행·소비·재처리 | Kafka, RabbitMQ, JMS, IBM MQ |
| Integration | REST·TCP·전문 연계 | HTTP, TCP, Fixed-length, ISO8583 |
| File | 첨부·압축·표·SFTP | Local/Customer Storage, SFTP |
| Notification | Email/SMS 알림 | SMTP, 고객 SMS Provider |
| Security | 인증·권한·Session·Secret | Resource Server, JDBC Session |
| Platform Operations | 관측·Runtime 변경·Feature Flag | OTLP, Runtime Control, OpenFeature |

## 5. 책임 경계

고객 업무는 업무 규칙, 상태, 데이터, 권한과 운영 기준을 결정한다. CPF는 표준 Context, 오류, 멱등성, Provider 계약, 복구 상태, 관측·감사, Generator와 검증 Gate를 제공한다.

고객이 임의로 수정하지 않을 영역:

- `cpf-core` Public Contract
- 내부 Starter AutoConfiguration
- DB Pack Checksum
- Generated Domain Policy Runtime Verifier
- Artifact Catalog/BOM 생성 규칙

고객이 선언하는 영역:

- Domain ID·System Code·Package
- Profile와 Capability Binding
- 업무 Entity·API·Permission·Data Scope
- 외부 Endpoint·Topic/Queue Binding·File Path Policy
- 승인·보존·복구 기준

## 6. 지원 Topology

- Embedded Boot JAR
- External WAS WAR
- Modular Monolith
- 독립 Microservice
- ADM/BZA Backend + Static Frontend
- Gateway 독립 Runtime
- Batch Control/Scheduler/Worker/Runner/Agent
- Multi-instance와 Multi-zone
- Rolling, Canary, Blue-Green

Topology가 달라도 DTO, Header, Validation, Error, Security, Audit, Idempotency, Timeout, UNKNOWN과 Recovery 의미는 유지한다.

## 7. 도입 순서

1. 대상 업무를 온라인, 이벤트, 배치, 연계, 파일, 운영으로 분류한다.
2. Profile 하나와 필요한 Capability Group을 선택한다.
3. Generator 입력과 Provider Binding을 결정한다.
4. `domain-manifest.json`과 `resolved-starter-lock.json`을 검토한다.
5. 3 Vendor 중 DB Vendor와 Lifecycle을 결정한다.
6. Permission, Data Scope, Masking, Approval 정책을 정의한다.
7. ADM/BZA/Gateway 연결 범위를 결정한다.
8. Fault Scenario와 운영 Runbook을 작성한다.
9. Build, Browser, DB, Runtime, Supply Chain Gate를 실행한다.
10. Artifact Hash, SBOM, Migration Checksum과 운영 인계서를 전달한다.

## 8. 대표 업무 여정

### 온라인 신청

`web-api` 또는 `secure-api` → Data Capability → 상태/Version 저장 → 필요 시 Outbox → ADM 거래·감사 조회.

### 이벤트 처리

`event-service` → Broker Binding → Outbox/Inbox → Provider 전송 → ACK 유실 시 UNKNOWN → Recovery Center Reconcile.

### 대량 처리

`batch-service` → Job/Step → Scheduler → Worker Claim/Lease → Checkpoint → 장애 시 Restart/Reprocess → ADM Batch 화면 확인.

### 외부 전문

Integration Capability → Target/Timeout/Correlation → TCP/Fixed-length/ISO8583 → 응답 유실 시 원 거래 조회·Reversal·Reconcile.

## 9. 역할별 문서

- 온라인·연계 개발: `01_CPF_개발자매뉴얼.md`
- 배치: `02_CPF_배치개발매뉴얼.md`
- ADM 연동·운영: `03_CPF_ADM매뉴얼.md`
- 설치·운영·DR: `05_CPF_플랫폼운영매뉴얼.md`
- Profile/Capability: `90_CPF_Starters_매뉴얼.md`
- Generator/Build/DB/검증: `91_CPF_Tools_매뉴얼.md`
- Gateway: `92_CPF_Gateway_매뉴얼.md`
- BZA: `95_CPF_BZA_매뉴얼.md`

## 10. 도입 완료 판정

선택한 Profile·Capability가 Manifest와 Lock에 고정되고, 실제 Consumer가 CPF Public API만 사용하며, 정상·오류·UNKNOWN·복구·권한·감사 절차와 환경별 Evidence가 준비되면 도입 범위를 승인한다.
