<div align="center">

<img src="cpf-docs/assets/brand/cpf-hero.svg" alt="Core Platform Framework" width="100%" />

# Core Platform Framework

**업무 시스템의 개발, 실행, 연계, 배치, 운영, 보안, 감사, 확장과 배포를 하나의 제품 구조로 연결하는 상용 업무 플랫폼 프레임워크**

동일 JVM · 분리 WAS · 모듈형 단일체 · 마이크로서비스 · 다중 인스턴스 · 운영 통제 · 생성형 업무영역

[5분 시작](#5분-시작)
· [구조 이해](#제품-구조)
· [개발자 가이드](cpf-docs/guides/CPF_DEVELOPER_GUIDE.md)
· [운영자 가이드](cpf-docs/guides/CPF_ADMIN_OPERATOR_GUIDE.md)
· [Generator](cpf-docs/guides/CPF_GENERATOR_TOOL_GUIDE.md)
· [설치·업그레이드](cpf-docs/guides/CPF_INSTALL_UPGRADE_ROLLBACK_GUIDE.md)

</div>

---

## CPF란 무엇인가

CPF의 정식 명칭은 **Core Platform Framework**다.

CPF는 공통 유틸리티 모음이나 예제 프로젝트가 아니다. 금융권을 포함한 업무 시스템을 장기간 구축·운영할 수 있도록 다음 영역을 하나의 제품 규칙으로 제공한다.

| 영역 | CPF가 제공하는 제품 계약 |
|---|---|
| 구조 | 모듈형 단일체와 마이크로서비스를 같은 Public Contract로 구성 |
| 호출 | 동일 JVM 호출과 분리 WAS 호출의 Header, 오류, 추적, 재시도 의미 통일 |
| 안정성 | 시간 제한, 재시도, 회로 차단, 멱등성, 결과 불명, 대사, 보상 |
| 실행 | 온라인, 비동기, 메시징, 파일, 외부 연계, Batch, Worker, Agent, Center-Cut |
| 운영 | 서비스·인스턴스·거래·로그·Batch·배포 상태 조회와 안전한 운영 명령 |
| 보안 | 인증, 권한, 민감정보 마스킹, 비밀값 참조, 승인, 감사 |
| 데이터 | Oracle, PostgreSQL, MariaDB 설치·이관·업그레이드·되돌리기·복구 |
| 확장 | 하나의 Golden Template을 사용하는 신규 업무영역 Generator |
| 검증 | Source, API, SQL, Test, 문서와 실행 Evidence의 양방향 추적 |
| 공급 | 로컬 개발, 사내 저장소, 폐쇄망 배포를 위한 버전·Hash 기반 산출물 |

---

## 제품 구조

CPF는 네 개의 제품 영역으로 구성된다.

```text
┌──────────────────────────────────────────────────────────────────┐
│                    업무·운영 애플리케이션 영역                   │
│  cpf-admin · cpf-biz-admin · cpf-member · cpf-<generated-domain> │
└──────────────────────────────┬───────────────────────────────────┘
                               │ Public API / SPI
┌──────────────────────────────▼───────────────────────────────────┐
│                         Framework 기반                           │
│                       cpf-core · cpf-common                      │
└──────────────────────────────┬───────────────────────────────────┘
                               │
┌──────────────────────────────▼───────────────────────────────────┐
│                     선택 실행·진입 제품                          │
│                 cpf-gateway · cpf-batch 제품군                   │
└──────────────────────────────┬───────────────────────────────────┘
                               │
┌──────────────────────────────▼───────────────────────────────────┐
│                        도구·공급·검증                             │
│        Generator · DB Tool · Quality Gate · Artifact Supply      │
└──────────────────────────────────────────────────────────────────┘
```

### 공식 Module

| Module | SystemCode | 책임 |
|---|---:|---|
| `cpf-core` | `CPF` | topology-independent 기술 계약, Public API/SPI, 실행 문맥, 오류, 추적 |
| `cpf-common` | `CMN` | 고객 업무 공통 정책과 선택형 공통 기능 |
| `cpf-admin` | `ADM` | 플랫폼 운영, 관제, 보안, 감사, 승인과 제어 |
| `cpf-biz-admin` | `BZA` | 조직, 사용자, Role, Permission, 결재, 알림, 첨부 |
| `cpf-gateway` | `GWY` | 선택형 외부 진입, Routing, 정책 집행, 연결시험, 적용 상태 |
| `cpf-batch` | `BAT` | Scheduler, Worker, Agent, Center-Cut와 실행 Control Plane |
| `cpf-member` | `MBR` | Generator Golden Reference Instance |
| `cpf-reference` | `REF` | 실제 Public API를 사용하는 교육·참조 구현 |
| `cpf-<domain>` | 3자리 코드 | Generator로 생성하는 독립 업무영역 |

### Public API, SPI, Internal

```text
com.cpf.core.api       고객·업무 개발자가 사용하는 안정 계약
com.cpf.core.spi       고객 Adapter와 업무 확장을 위한 Port
com.cpf.core.internal  CPF 제품 내부 구현
```

업무 Module은 내부 구현 Package에 직접 의존하지 않는다. 공통 계약이 필요하면 Public API 또는 SPI로 제공하고, 실제 Consumer와 기본 구현, 오류·복구·운영 경로를 함께 갖춘다.

---

## 배포 구성

CPF는 배포 방식 때문에 업무 계약이 달라지지 않도록 설계한다.

### 직접 진입과 선택형 Gateway

```text
직접 진입
Client / Channel ───────────────→ Business Domain

공통 진입 정책 사용
Client / Channel → cpf-gateway → Business Domain
```

Gateway는 필수 중앙 경유지가 아니다. 공통 인증, Header 정규화, Routing, Rate Limit, 장애 격리 또는 외부 공개 통제가 필요한 경우 선택한다.

### 동일 JVM과 분리 WAS

```text
동일 JVM
Domain A → Local Adapter → Domain B Public Contract

분리 WAS
Domain A → Remote Adapter → Domain B Public API
```

두 방식 모두 다음을 동일하게 유지한다.

- 표준 Header와 `transactionId`
- 인증·권한 문맥
- 오류 분류와 응답 계약
- 시간 제한과 재시도 정책
- 멱등성 키와 결과 불명 처리
- Trace와 운영 Timeline

### 지원 실행 형태

- Embedded Boot JAR
- 외부 WAS용 WAR
- 모듈형 단일체
- 독립 마이크로서비스
- 로컬 통합 Runtime
- Scheduler·Worker·Agent 독립 Process
- 다중 인스턴스
- Rolling, Canary, Blue-Green와 재해복구 구성

---

## 핵심 실행 모델

### 거래 식별과 추적

모든 주요 실행 흐름은 동일한 식별 체계를 사용한다.

```text
transactionId
└─ segmentId
   ├─ online service call
   ├─ remote attempt
   ├─ async event
   ├─ batch execution
   └─ compensation / reconciliation
```

운영자는 시스템, 인스턴스, 거래, 실행, Job과 재처리 이력을 같은 식별자로 연결해 조회할 수 있다.

### 결과 불명

상대 시스템이 처리했는지 확정할 수 없는 상태를 단순 실패로 바꾸지 않는다.

```text
요청 전송
→ 시간 제한 또는 응답 유실
→ UNKNOWN_RESULT
→ 결과 조회 / 대사 / 운영 확인
→ 최종 성공·실패 확정
→ 필요 시 재처리 또는 보상
```

### 멱등성

반복될 수 있는 Command는 다음을 구분한다.

- 같은 멱등성 키 + 같은 정규화 요청: 최초 처리 의미 재사용
- 같은 멱등성 키 + 다른 요청: 충돌로 거부
- 재시도 가능 오류와 재시도 금지 오류 구분
- 처리 결과와 감사 이력 보존

### 다중 인스턴스

Scheduler, Worker, Agent, 배포 Consumer와 운영 명령은 Lease, Claim, Fencing Token, Version과 Optimistic Lock을 사용한다. 소유권을 잃은 이전 실행자가 늦게 결과를 반영하지 못하도록 한다.

---

## 온라인·비동기·외부 연계

### 온라인 서비스 호출

- Local/Remote Adapter 선택
- Service Registry와 Instance 선택
- Timeout Budget
- Retry와 Backoff
- Circuit Breaker와 Bulkhead
- Header·Trace 전달
- 오류 변환
- 결과 불명과 대사

### 비동기·메시징

- Transactional Outbox
- Inbox와 Idempotent Consumer
- 재시도·지연·독성 메시지 분리
- DLQ와 Replay
- Correlation Trace
- Schema Version과 호환성
- 운영 재처리와 감사

### 파일·첨부·전문

- 업로드 크기·형식·경로 검증
- 악성 파일 검사
- 격리와 다운로드 통제
- Checksum과 중복 방지
- 파일 전송 이력과 대사
- 고정길이 전문 Layout과 Encoding 검증
- 민감정보 마스킹

상세 내용은 [비동기·메시징·보상 가이드](cpf-docs/guides/CPF_ASYNC_MESSAGING_AND_COMPENSATION_GUIDE.md)를 참고한다.

---

## Batch와 Center-Cut

CPF Batch는 업무 정의와 실행 Runtime을 분리한다.

```text
ADM / BAT Control Plane
→ 작업정의 작성
→ 검증
→ 작성자·승인자 분리
→ 배포
→ 실행용 Projection
→ Scheduler
→ Worker / Agent / Center-Cut Runner
→ 실행 이력·Checkpoint·재처리
```

제품 구성:

| Project | 역할 |
|---|---|
| `:cpf-batch:contract` | 업무 Job과 Runtime 사이의 Public Contract |
| `:cpf-batch:runtime-common` | Lease, Fencing, 실행 문맥 등 공통 구현 |
| `:cpf-batch:control-server` | 작업정의, 실행, 배포, 상태 조회와 명령 |
| `:cpf-batch:scheduler` | Trigger, Calendar, Misfire와 HA Scheduling |
| `:cpf-batch:worker` | Spring Batch Job·Step·Tasklet 실행 |
| `:cpf-batch:center-cut-runner` | 대량 Partition·Claim·재처리 |
| `:cpf-batch:host-agent` | 원격 Host의 승인 Artifact 실행 |
| `:cpf-batch:testkit` | 업무 Job Pack과 장애 시나리오 검증 |

---

## 운영과 통제

### ADM

`cpf-admin`은 운영자가 장애를 분석하고 안전하게 조치하기 위한 Control Plane이다.

- 서비스, Endpoint, Instance, Health와 Topology
- Gateway Registry, Binding, 배포, ACK와 구성 불일치
- 거래, Timeline, Log와 Trace
- Batch 작업정의, 실행, Worker, Agent와 Center-Cut
- 재시도, Replay, 대사, 보상과 결과 불명 처리
- 설정, Cache, 동적 Log Level과 Runtime Policy
- Secret Metadata, 인증서, 보안 운영
- 승인, 비상 권한, 사유와 감사
- Incident, Runbook, 알림과 조치 결과

### BZA

`cpf-biz-admin`은 고객 업무 운영을 위한 Backoffice다.

- 사용자와 계정 상태
- Role, Permission과 유효기간
- 조직, 직원, 직위, 직책과 Assignment
- 결재 정책, 단계, 대리결재와 Snapshot
- 알림과 읽음 상태
- 첨부, 검사, 격리와 다운로드
- 업무 운영 감사와 Hash Chain

### 위험 조치

위험한 운영 명령은 다음 공통 절차를 따른다.

```text
대상 조회
→ 현재 상태 Snapshot
→ 권한 확인
→ 사유 입력
→ 필요 시 승인
→ Version / Idempotency 확인
→ 실행
→ 결과 확인
→ 대사 또는 되돌리기
→ 감사와 Evidence
```

---

## 보안

CPF는 안전한 기본값을 사용한다.

- 인증되지 않은 요청 기본 거부
- 서버 권한 검증을 UI 표시보다 우선
- 외부 공개 기본 거부
- Secret 원문 대신 Reference 전달
- 로그, API, 감사, Evidence의 재귀 마스킹
- 작성자와 승인자 분리
- 비상 권한 사용 시 사유·범위·만료·감사
- 다운로드, 원문 보기, 설정 변경의 별도 권한
- 악성 파일 검사와 격리
- 승인 Artifact의 Hash와 전자서명 검증
- 보안 설정 미구성 시 안전 차단

상세 내용은 [보안·재해복구·보존 가이드](cpf-docs/guides/CPF_SECURITY_DR_RETENTION_GUIDE.md)를 참고한다.

---

## 데이터베이스

공식 지원 Vendor:

| Vendor | 식별자 |
|---|---|
| Oracle | `oracle` |
| PostgreSQL | `postgresql` |
| MariaDB | `mariadb` |

모든 Vendor는 동일한 제품 생명주기를 제공한다.

```text
Provision
→ Empty Install
→ Product Seed
→ Verify
→ Upgrade
→ Compatibility Check
→ Rollback
→ Reapply
→ Backup
→ Restore
→ DR Verify
```

DB Artifact는 Canonical Source에서 생성한다. Column, Type, Default, PK, FK, Index, Identity, Comment와 Logical DB Ownership을 Vendor 간 비교한다. 기존 Schema가 다르면 조용히 건너뛰지 않고 정본 불일치 또는 Migration 문제로 처리한다.

---

## Generator 기반 업무영역 확장

Generator는 `DomainName`과 3자리 `SystemCode`를 받아 독립 업무영역을 만든다.

```powershell
pwsh -File .\cpf-tools\generator\create-domain.ps1 `
  -DomainName "payment" `
  -SystemCode "PAY" `
  -DatabaseVendor "postgresql" `
  -DryRun
```

생성 범위:

- Module과 Package
- API, Application, Domain, Adapter 계층
- 표준 Header, 오류와 Paging
- Local/Remote 호출 계약
- DB Source, Migration, Rollback와 Verify
- Oracle, PostgreSQL, MariaDB 산출물
- Test와 Test Fixture
- OpenAPI, JavaDoc와 EDU
- Runtime Profile과 Route
- Service Registry 등록 정보
- 외부 공개 기본 거부 정책
- Generator 소유 영역과 고객 수정 영역 분리

---

## 5분 시작

### 준비 환경

- JDK 25
- Git
- Gradle Wrapper
- PowerShell 7
- Node.js와 npm
- Oracle, PostgreSQL 또는 MariaDB 중 사용할 DB

### 전체 Build

Linux/macOS:

```bash
./gradlew clean build
```

Windows:

```powershell
.\gradlew.bat clean build
```

### 로컬 Runtime

```powershell
pwsh -File .\cpf-tools\scripts\start-cpf-local.ps1
pwsh -File .\cpf-tools\scripts\status-cpf-local.ps1
pwsh -File .\cpf-tools\scripts\stop-cpf-local.ps1
```

### 로컬 분산 Batch Runtime

```powershell
pwsh -File .\cpf-tools\scripts\start-bat-local-distributed.ps1
pwsh -File .\cpf-tools\scripts\stop-bat-local-distributed.ps1
```

### Database 설치

```powershell
pwsh -File .\cpf-tools\scripts\initialize-cpf-database.ps1 -All -RequireRun
```

### ADM/BZA Frontend

```bash
cd cpf-admin/frontend
npm ci
npm run lint
npm run typecheck
npm run test
npm run build
```

`cpf-biz-admin/frontend`도 같은 절차를 사용한다.

---

## 품질과 Evidence

CPF는 파일 존재나 일부 Test 통과만으로 기능을 완료 처리하지 않는다.

```text
Requirement
→ Source / API / SQL / Config
→ Unit / Integration / Runtime / Browser Test
→ 실행 결과
→ 민감정보를 제거한 Evidence
```

대표 검증:

```powershell
pwsh -File .\cpf-tools\scripts\check-architecture-ownership.ps1
pwsh -File .\cpf-tools\scripts\check-repository-hygiene.ps1
pwsh -File .\cpf-tools\scripts\check-document-links.ps1
pwsh -File .\cpf-tools\scripts\verify-full-product.ps1
```

Evidence에는 최소한 다음을 기록한다.

- 기준 Source Commit
- 정확한 실행 명령
- 환경과 Profile
- 시작·종료 시각
- 종료 코드
- 관련 Requirement
- 실제 결과
- 원본 Log 또는 Query 결과
- 민감정보 제거 여부

---

## 문서 안내

### 시작과 개발

| 주제 | 문서 |
|---|---|
| 개발 표준 | [CPF_DEVELOPER_GUIDE.md](cpf-docs/guides/CPF_DEVELOPER_GUIDE.md) |
| Foundation API | [CPF_FOUNDATION_API_GUIDE.md](cpf-docs/guides/CPF_FOUNDATION_API_GUIDE.md) |
| Public API와 Generated Domain | [CPF_PUBLIC_API_AND_GENERATED_DOMAIN_GUIDE.md](cpf-docs/guides/CPF_PUBLIC_API_AND_GENERATED_DOMAIN_GUIDE.md) |
| Generator | [CPF_GENERATOR_TOOL_GUIDE.md](cpf-docs/guides/CPF_GENERATOR_TOOL_GUIDE.md) |
| EDU | [CPF_EDU_COVERAGE_GUIDE.md](cpf-docs/guides/CPF_EDU_COVERAGE_GUIDE.md) |

### 운영과 실행

| 주제 | 문서 |
|---|---|
| ADM 운영 | [CPF_ADMIN_OPERATOR_GUIDE.md](cpf-docs/guides/CPF_ADMIN_OPERATOR_GUIDE.md) |
| BZA 운영 | [CPF_BIZ_ADMIN_GUIDE.md](cpf-docs/guides/CPF_BIZ_ADMIN_GUIDE.md) |
| ADM/BZA 화면 표준 | [CPF_ADMIN_BZA_UI_STANDARD_GUIDE.md](cpf-docs/guides/CPF_ADMIN_BZA_UI_STANDARD_GUIDE.md) |
| Gateway | [CPF_GATEWAY_OPERATIONS_GUIDE.md](cpf-docs/guides/CPF_GATEWAY_OPERATIONS_GUIDE.md) |
| Batch Runtime과 Agent | [CPF_BATCH_RUNTIME_AND_REMOTE_AGENT_GUIDE.md](cpf-docs/guides/CPF_BATCH_RUNTIME_AND_REMOTE_AGENT_GUIDE.md) |
| Scheduler와 실행 생명주기 | [CPF_BATCH_SCHEDULER_INSTANCE_LIFECYCLE_GUIDE.md](cpf-docs/guides/CPF_BATCH_SCHEDULER_INSTANCE_LIFECYCLE_GUIDE.md) |
| 비동기·메시징·보상 | [CPF_ASYNC_MESSAGING_AND_COMPENSATION_GUIDE.md](cpf-docs/guides/CPF_ASYNC_MESSAGING_AND_COMPENSATION_GUIDE.md) |
| 관측·장애대응 | [CPF_OBSERVABILITY_INCIDENT_AND_RECOVERY_GUIDE.md](cpf-docs/guides/CPF_OBSERVABILITY_INCIDENT_AND_RECOVERY_GUIDE.md) |
| Health와 Registry | [CPF_HEALTH_AND_SERVICE_REGISTRY_GUIDE.md](cpf-docs/guides/CPF_HEALTH_AND_SERVICE_REGISTRY_GUIDE.md) |
| Runtime 설정·정책 배포 | [CPF_CONFIGURATION_AND_RUNTIME_POLICY_GUIDE.md](cpf-docs/guides/CPF_CONFIGURATION_AND_RUNTIME_POLICY_GUIDE.md) |

### 설치·공급·복구

| 주제 | 문서 |
|---|---|
| 설치·업그레이드·되돌리기 | [CPF_INSTALL_UPGRADE_ROLLBACK_GUIDE.md](cpf-docs/guides/CPF_INSTALL_UPGRADE_ROLLBACK_GUIDE.md) |
| Database Tool | [CPF_DATABASE_TOOL_GUIDE.md](cpf-docs/guides/CPF_DATABASE_TOOL_GUIDE.md) |
| DB Profile과 업무영역 DB | [DATABASE_PROFILE_AND_DOMAIN_DB_GUIDE.md](cpf-docs/guides/DATABASE_PROFILE_AND_DOMAIN_DB_GUIDE.md) |
| 보안·재해복구·보존 | [CPF_SECURITY_DR_RETENTION_GUIDE.md](cpf-docs/guides/CPF_SECURITY_DR_RETENTION_GUIDE.md) |
| 산출물 공급과 CI/CD | [CPF_ARTIFACT_SUPPLY_AND_CICD_GUIDE.md](cpf-docs/guides/CPF_ARTIFACT_SUPPLY_AND_CICD_GUIDE.md) |
| Tool 개요 | [CPF_TOOLS_GUIDE.md](cpf-docs/guides/CPF_TOOLS_GUIDE.md) |
| Tool 상세 참조 | [CPF_TOOL_REFERENCE.md](cpf-docs/guides/CPF_TOOL_REFERENCE.md) |
| Test와 Evidence | [CPF_TEST_AND_EVIDENCE_GUIDE.md](cpf-docs/guides/CPF_TEST_AND_EVIDENCE_GUIDE.md) |

---

## 기여 원칙

기능을 추가하거나 변경할 때 다음 순서로 판단한다.

1. 해결할 Requirement와 Owner Module을 확인한다.
2. Public API, SPI와 Internal 경계를 정한다.
3. 실제 Consumer와 Runtime 연결을 확인한다.
4. 동일 JVM과 분리 WAS에서 같은 계약이 성립하는지 확인한다.
5. 다중 인스턴스, 부분 실패와 복구 영향을 확인한다.
6. 보안, 권한, 감사와 운영 제어를 확인한다.
7. DB Vendor, Migration과 Generator 영향을 확인한다.
8. Source, SQL, API, Test, Guide와 Evidence를 함께 변경한다.
9. 기존 성공 기능의 회귀와 Repository Hygiene를 확인한다.

> CPF는 기능 목록이 아니라 장기간 유지되는 제품 구조다. 새로운 추상화는 실제 Consumer, 기본 구현, 오류·복구·운영 경로와 검증 근거를 함께 가져야 한다.
