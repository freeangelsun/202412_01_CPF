# CPF 개발자 매뉴얼 — 업무 기능을 설계·구현·검증·인계하는 절차

> **주 독자**: 온라인 업무 개발자, 메시지·파일·외부연계 개발자, 기술 리더
> **완료 결과**: 업무 기능의 API·상태·DB·권한·연계·시험·운영 인계를 하나의 추적 가능한 단위로 완성한다.
> **Source 기준**: `freeangelsun/202412_01_CPF`, `master`, `3b600702502e53877e30cbac594987b371e2186b`

## 1. 이 매뉴얼의 범위

이 문서는 CPF 자체 제품을 새로 개발하는 절차가 아니라, CPF의 공개 계약과 도구를 이용해 조직 고유 업무를 만드는 절차다.

포함 범위:

- 개발환경과 Build
- Generator와 신규 업무 영역
- API·Application·Domain·Persistence
- Transaction·동시성·멱등성·결과 미확정
- 동일 JVM·원격 호출
- 메시지 브로커·Outbox·Inbox
- 파일·SFTP·외부 REST·전문 연계
- Security·Masking·Audit
- DB Migration·Upgrade·Rollback
- OpenAPI·JavaDoc
- Unit·Contract·Integration·Fault Test
- ADM 연결과 운영 인계

별도 제품과 도구의 절차는 [03 CPF ADM 매뉴얼](03_CPF_ADM매뉴얼.md), [05 CPF 플랫폼 운영 매뉴얼](05_CPF_플랫폼운영매뉴얼.md), [90 CPF Starters 매뉴얼](90_CPF_Starters_매뉴얼.md), [91 CPF Tools 매뉴얼](91_CPF_Tools_매뉴얼.md), [92 CPF Gateway 매뉴얼](92_CPF_Gateway_매뉴얼.md), [95 CPF BZA 매뉴얼](95_CPF_BZA_매뉴얼.md)을 사용한다.

## 2. 시작 전 점검

```powershell
$repo='C:\dev\projects\jck\202412_01_CPF'
if(-not(Test-Path -LiteralPath $repo -PathType Container)){throw "Repository가 없습니다: $repo"}
git -C $repo remote -v
git -C $repo branch --show-current
git -C $repo fetch origin master
git -C $repo rev-parse HEAD
git -C $repo rev-parse origin/master
git -C $repo status --short
java -version
pwsh --version
```

판정:

- `origin/master`의 exact SHA를 개발·검증 기록에 남긴다.
- 다른 작업자의 변경은 삭제·복원하지 않는다.
- 실행하지 않은 Test를 성공으로 기록하지 않는다.
- Source에 없는 API·Class·Property·Route·Permission·상태를 문서나 코드 예제로 만들지 않는다.

## 3. 현재 Architecture와 QA38 목표

### 3.1 기준 Commit의 확인 상태

| 항목 | 현재 상태 | 개발 시 행동 |
|---|---|---|
| `cpf-core` | MyBatis·AspectJ·Validation·Archive·OpenAPI UI 등 선택 Runtime이 남음 | Core 의존성을 기술 중립으로 가정하지 말고 실제 POM 확인 |
| `cpf-common` | MyBatis·Cache·POI·Validation Runtime이 남음 | 업무 공통과 기술 Runtime 경계를 검토 |
| 공개 Starter | 7개 등록 | 실제 Consumer·AutoConfiguration·Properties를 확인해 선택 |
| Profile·Aggregate | 미구현 | 현재 Dependency로 작성하지 않음 |
| Generator Profile Lock | 미구현 | 생성 후 Build 의존성과 Manifest를 수동 대조 |
| 신규 Messaging/TCP/Notification Starter | QA38 목표, 기준 Commit 미등록 | 구현 전 현재 사용 가능한 기능으로 안내하지 않음 |

### 3.2 개발자가 지켜야 할 경계

```text
API Adapter
  ↓
Application Use Case
  ↓
Domain Rule
  ↓
Port
  ↓
Persistence·Messaging·File·Remote Adapter
```

- Controller는 업무 규칙과 SQL을 직접 소유하지 않는다.
- Application은 사용 사례와 Transaction 경계를 소유한다.
- Domain은 업무 상태와 불변식을 소유한다.
- Adapter는 Provider SDK와 CPF 공개 SPI를 연결한다.
- 다른 모듈의 `internal` Package를 직접 참조하지 않는다.
- ADM·BZA·Gateway는 업무 상태를 직접 변경하지 않고 Owner의 Command 계약을 호출한다.

## 4. 신규 업무 영역 생성

### 4.1 Dry Run

```powershell
$repo='C:\dev\projects\jck\202412_01_CPF'
pwsh -NoProfile -ExecutionPolicy Bypass -File (Join-Path $repo 'cpf-tools\generator\create-domain.ps1') `
  -DomainName payment `
  -SystemCode PAY `
  -DatabaseVendor postgresql `
  -DependencyModel root-project `
  -DryRun
```

확인 항목:

- Project 경로와 이름
- Java Package
- System Code
- Port 충돌
- Schema·Table Prefix
- DB Vendor
- 선택 기능
- 생성·수정 대상
- 사용자 수정 영역 보호

### 4.2 Apply 전 결정값

| 결정값 | 예 | 소유자 | 오류 시 |
|---|---|---|---|
| `DomainName` | `payment` | 업무 개발팀 | 영문 규칙·기존 경로 충돌 검사 |
| `SystemCode` | `PAY` | Architecture | 3자리 계약 위반 시 중단 |
| `DatabaseVendor` | `mariadb`·`postgresql`·`oracle` | DBA·개발팀 | 지원 Vendor 외 값 금지 |
| `DependencyModel` | `root-project`·`published-artifact` | Build Owner | Artifact 공급 방식 불일치 시 중단 |
| `Capabilities` | Generator가 허용한 값 | 개발팀 | 미지원 기능을 임의 문자열로 추가 금지 |
| `ProductionProfile` | `Y`·`N` | 운영팀 | 운영 Secret·DB 준비 전 활성화 금지 |

### 4.3 생성 후 필수 확인

```powershell
$repo='C:\dev\projects\jck\202412_01_CPF'
& (Join-Path $repo 'gradlew.bat') projects
& (Join-Path $repo 'gradlew.bat') :cpf-payment:compileJava
& (Join-Path $repo 'gradlew.bat') :cpf-payment:test
```

- 생성된 `build.gradle`에 실제 필요한 의존성이 있는지 확인한다.
- 기준 Commit의 Generator는 QA38의 `resolvedStarters`·Profile Version·Starter Lock을 제공한다고 단정하지 않는다.
- 필요한 Starter를 직접 선언했다면 JAR/WAR, Runtime Classpath와 SBOM에서 선택 결과를 확인한다.
- 생성 SQL은 중앙 Vendor Pack 계약과 일치해야 한다.

## 5. 기능 설계 카드

모든 기능은 코딩 전에 아래 항목을 작성한다.

| 항목 | 작성 내용 |
|---|---|
| 기능 ID | 변경되지 않는 추적 ID |
| 업무 결과 | 사용자가 얻게 되는 상태·데이터 |
| Owner Module | 상태와 DB를 소유하는 모듈 |
| Consumer | API·Batch·ADM·BZA·Gateway 등 |
| 입력 | 필수·선택·기본값·범위 |
| 업무 상태 | Source에 존재하는 상태만 |
| Transaction | 시작·Commit·Rollback 경계 |
| 멱등성 | Key 구성, 요청 Hash, Replay 정책 |
| 동시성 | Version·Lock·Lease·Fencing |
| Timeout | 전체 시간 예산과 하위 호출 배분 |
| 결과 미확정 | 판정 기준과 Reconcile |
| 권한 | Permission·Data Scope·Masking |
| 감사 | 요청자·승인자·사유·이전/이후 값 |
| 정상화 | Retry·Restart·Reprocess·Compensation |
| Test | 정상·오류·경계·Fault |
| 운영 인계 | Log·Metric·Trace·ADM 확인점 |

## 6. API 개발

### 6.1 요청 계약

요청에는 필요한 범위에서 다음 식별자를 사용한다.

- `transactionId`
- `operationId`
- `businessKey`
- `idempotencyKey`
- `traceId`
- `expectedVersion`
- `deadline`

실제 Header 이름과 DTO는 Source의 공개 계약을 확인한다. 임의 Alias를 만들지 않는다.

### 6.2 Controller

Controller의 책임:

1. 인증 주체와 권한 문맥을 받는다.
2. 요청 형식과 필수값을 검증한다.
3. Application Use Case를 호출한다.
4. CPF 표준 오류 모델로 응답한다.
5. 업무 규칙·SQL·재시도 Loop를 직접 구현하지 않는다.

### 6.3 응답

응답에는 최소한 다음 판정 정보를 포함한다.

- 최종 또는 현재 상태
- Operation 식별자
- Version
- 결과가 확정되지 않았을 때 조회 방법
- 재시도 가능 여부
- 오류 분류와 사용자 조치

## 7. Application과 Transaction

### 7.1 기본 순서

1. 멱등성 기록을 조회하거나 획득한다.
2. 현재 업무 상태와 Version을 읽는다.
3. 권한·Data Scope·업무 전이 조건을 검증한다.
4. 업무 원장을 변경한다.
5. 외부 효과가 필요하면 Outbox 또는 시도 원장을 기록한다.
6. Audit을 기록한다.
7. Commit 후 비동기 Worker가 외부 효과를 수행한다.
8. 결과를 Operation 조회로 확인한다.

### 7.2 금지

- DB Transaction 안에서 무제한 원격 재시도
- 응답을 못 받았다는 이유로 같은 업무를 새 Key로 재실행
- `UNKNOWN_RESULT`를 실패로 임의 변경
- 일부 대상 성공 후 전체를 처음부터 재실행
- UI나 ADM에서 DB 직접 수정
- Runtime Exception 문자열로 업무 상태 판정

## 8. Domain과 상태 전이

Domain은 다음을 명시한다.

- 허용 상태
- 상태별 허용 Command
- 불변식
- Version 증가 규칙
- 취소·보상 조건
- 재처리 대상 판정
- 최종 상태와 보존 기간

상태 전이 Test:

```text
정상 전이
허용되지 않은 전이
동일 요청 Replay
Expected Version 충돌
동시 요청
삭제·정지 상태 접근
부분 성공 후 재처리
```

## 9. Persistence와 DB

### 9.1 공식 Vendor

- MariaDB
- PostgreSQL
- Oracle

### 9.2 개발 순서

```text
업무 요구와 Data Model
→ Canonical Metadata
→ Generator Query
→ Vendor Pack
→ Migration·Rollback
→ Mapper·Repository
→ Service·API
→ Test·Evidence
```

Vendor SQL부터 직접 수정하지 않는다. 한 Vendor만 수정하고 나머지를 미루지 않는다.

### 9.3 Transaction·Lock

| 상황 | 권장 계약 |
|---|---|
| 단일 Row 상태 변경 | `expectedVersion` 기반 낙관적 잠금 |
| 작업 소유권 | Lease·Claim·Fencing |
| 중복 요청 | Idempotency Ledger |
| 외부 발행 | Outbox |
| 외부 수신 | Inbox·Dedup |
| 장시간 처리 | 짧은 DB Transaction + 상태 원장 |
| 다중 대상 | 대상별 상태와 집계 상태 분리 |

### 9.4 Migration

각 변경은 다음을 제공한다.

- 설치 전제 조건
- Upgrade Script
- Backfill
- Index·Lock 영향
- Mixed Version 허용 범위
- Rollback 또는 Forward Recovery
- 재적용과 다른 Hash 충돌 판정
- Vendor 3종 확인 명령

## 10. 동일 JVM과 원격 호출

### 10.1 계약 동등성

Local과 Remote 구현은 다음 의미가 같아야 한다.

- 입력·오류
- 권한 문맥
- Deadline
- 멱등성
- Version
- 결과 미확정
- Audit
- Trace

### 10.2 Timeout Budget

```text
전체 요청 Deadline
- Controller·Queue 지연
- 하위 서비스 호출
- DB
- 결과 기록
- 응답 여유
```

하위 호출 Timeout의 합이 전체 Deadline을 초과하지 않도록 한다. Timeout 후 실제 효과가 발생할 수 있으면 `UNKNOWN_RESULT`로 판정하고 조회·대사 절차를 제공한다.

## 11. 비동기 메시지 처리

상위 매뉴얼에서는 메시지 브로커라는 기능명으로 설명하고, Build·Config·운영 절차에서는 실제 Provider를 명시한다.

### 11.1 공통 흐름

1. 업무 Transaction에서 업무 원장과 Outbox를 함께 Commit한다.
2. Worker가 Claim·Lease·Fencing으로 전송 대상을 획득한다.
3. Provider Adapter가 메시지를 발행한다.
4. ACK 또는 결과 미확정을 기록한다.
5. Consumer는 Inbox·Dedup 후 업무 Command를 실행한다.
6. 실패는 제한된 Retry와 Dead Letter 정책으로 분류한다.
7. 운영자는 Lag·Backlog·Replay·Audit을 확인한다.

### 11.2 기준 Commit 상태

- Kafka Starter 프로젝트는 등록돼 있다.
- 기준 Commit의 실제 제품 Consumer와 Consume·Retry·DLT·Rebalance·Process Kill Closure는 다시 확인해야 한다.
- RabbitMQ·Jakarta JMS·IBM MQ Starter는 QA38 목표지만 `settings.gradle` 기준 미등록이다.
- 미구현 Provider를 현재 사용 가능한 의존성으로 안내하지 않는다.

### 11.3 Multi-provider 목표

복수 Provider 사용 시:

- Named Binding
- Default Binding 최대 하나
- Destination별 Routing
- 이름 없는 Client가 복수이면 Fail-closed
- Correlation·멱등성·결과 미확정 유지

이 계약은 QA38 목표이며 실제 Source가 구현되기 전에는 `미구현`이다.

## 12. 파일·SFTP·외부 REST·전문

### 12.1 파일·Attachment 공통

- 허용 확장자·크기·MIME
- 저장 위치와 Ownership
- Attachment ID와 업무 원장의 참조 관계
- 업로드 주체·다운로드 권한·Data Scope·Masking
- Checksum
- 암호화·가림·Virus Policy
- Partial Upload
- 중복 파일
- 보존·삭제·법적 보류·Audit

Attachment는 업무 원장의 상태를 대신하지 않는다. 파일 저장은 성공했지만 업무 등록이 실패한 경우 고아 파일을 식별하고, 업무 등록은 성공했지만 파일 전송 결과가 불명확한 경우 Attachment Ledger와 저장소 Checksum을 대사한다.

### 12.2 SFTP

제품 Runtime이 제공해야 할 범위:

- Upload·Download·List·Move·Delete
- Atomic Rename
- Resume
- Checksum
- Transfer Ledger
- Credential Expiry
- Network Loss
- Reconcile

Docker Fixture의 전송 성공은 제품 Starter와 실제 Consumer 검증을 대신하지 않는다.

### 12.3 외부 REST

- Base URL과 SSRF 허용 목록
- TLS·mTLS
- Credential·Secret Provider
- Connect·Read·Overall Timeout
- Retry 대상
- Circuit Breaker
- 요청·응답 가림
- 응답 유실 후 결과 조회

### 12.4 TCP·ISO8583

QA38 목표에는 TCP Client·Server, Framing, Encoding, Correlation, Heartbeat, Half-open, TLS, 결과 미확정, Reconcile과 ISO8583 확장이 포함된다. 기준 Commit에서 공식 Starter 등록을 확인하지 못했으므로 `미구현`으로 취급한다.

## 13. Security·Masking·Audit

각 API·Command는 다음을 정의한다.

| 항목 | 개발 기준 |
|---|---|
| 인증 | Session·Token·Service Identity 중 실제 Runtime |
| Permission | 기능 조치 단위 |
| Data Scope | 조직·업무·소유자 범위 |
| Masking | 화면·API·Log·Export 일관성 |
| Reason | 위험 조치 필수 사유 |
| Approval | 요청자·승인자 분리, 만료·정책 버전 |
| Audit | 누가·언제·무엇을·왜·이전/이후 값 |
| Secret | Source·Config·Log에 원문 저장 금지 |

권한이 없을 때 단순히 버튼만 숨기지 않는다. Backend에서 같은 Permission과 Scope를 검증한다.

## 14. Starter 선택

### 14.1 현재 직접 선택 예

```groovy
implementation project(':cpf-starter-security')
implementation project(':cpf-starter-observability')
```

게시 Artifact를 사용하는 경우 BOM은 Version만 정렬한다. BOM을 추가했다고 기능이 포함되는 것은 아니다.

### 14.2 선택 검증

- Build Dependency
- Runtime Classpath
- AutoConfiguration Report
- Property Binding
- 실행 Artifact
- POM·BOM
- SBOM
- Optional Removal Compile·Runtime
- Generator Manifest

### 14.3 현재 금지

- 구현되지 않은 Profile 이름을 Gradle 좌표로 사용
- `all`·`full`·`everything` 성격의 Mega Starter
- 두 Provider가 이름 없이 동시에 활성화
- 업무 정책을 범용 Starter에 포함
- Starter가 업무 원장·승인·보상을 소유

## 15. OpenAPI·JavaDoc

- Public API만 문서화한다.
- Internal Class를 사용 예로 노출하지 않는다.
- 오류·권한·Idempotency·Version·Deadline을 명시한다.
- Generated Client는 OpenAPI와 같은 Commit에서 생성한다.
- Breaking Change는 Version·Migration·Compatibility 범위를 기록한다.
- Build 결과의 OpenAPI·JavaDoc Artifact를 Hash로 확인한다.

## 16. Test 전략

| 단계 | 필수 범위 |
|---|---|
| Unit | Domain 불변식·상태 전이·값 검증 |
| Contract | Public API·SPI·오류·Property |
| Integration | DB·Provider·AutoConfiguration |
| Negative | 권한·잘못된 설정·중복·충돌 |
| Multi-instance | Claim·Lease·Fencing·Duplicate |
| Fault | Timeout·Connection Loss·Process Kill |
| Reconcile | 결과 미확정·부분 성공·재처리 |
| Optional Removal | 미선택 Starter 제거 Compile·Runtime |
| Supply Chain | POM·BOM·SBOM·Checksum·Provenance |

직접 실행한 명령, exact SHA, Tool·Image Version, Exit Code와 Log Hash를 남긴다.

## 17. ADM 연결과 운영 인계

업무 개발자는 ADM 자체를 개발하지 않는다. 다음 계약을 제공해 ADM에서 업무 상태를 이용할 수 있게 한다.

- Query: 목록·상세·상태·Version
- Command: 재처리·정지·재개·대사 등 허용 조치
- Permission·Data Scope
- Reason·Approval
- Expected Version
- Idempotency
- Operation 조회
- Audit
- Timeout·결과 미확정 Reconcile

인계 항목:

```text
기능 ID
Owner Module
API·Port
Permission
상태
Operation 조회 방법
Log·Metric·Trace
DB 대사 Query
재시도·재처리 조건
Rollback·보상
담당자
```

## 18. EDU 실습 구조

각 실습은 다음을 갖는다.

1. 선행 과정과 Source 경로
2. 전체 Config·Migration
3. 실행 명령
4. 정상 요청·응답·DB·Audit
5. 오류 재현
6. Fault Injection
7. 재시도·대사·정상화
8. ADM 확인
9. Test
10. CPF 관리 영역과 업무 개발팀 수정 영역

Repository에 실제 EDU가 없으면 가상의 성공 결과를 쓰지 않고 `미구현` 또는 `재확인 필요`로 기록한다.

### 18.1 기준 Commit에서 확인한 EDU 범위

- `EDU-DEV-05`는 실제 Handler, 필수 입력, JDBC Command Consumer Binding과 업무 원장 계약이 확인됐다.
- 공통 EDU 실행 API와 장애 지점 계약은 개발자 매뉴얼의 기존 정본에서 확인됐다.
- 전체 EDU의 Handler·Resource Contract·실제 Consumer·Test Assertion·DB·Message Broker·File·외부 연계·ADM Evidence를 같은 Commit에서 전수 실행한 결과는 확인되지 않았다.
- 따라서 개별 EDU는 Source·Consumer·Test·Runtime을 확인한 단위로 판정하고, 전체 EDU를 일괄 `완료`로 표시하지 않는다.
- 전수 확인 요구는 `산출물목록.md`의 `EDU-001` 개발 검토 항목으로 전달한다.

### 18.2 온라인·연계 EDU 45개 전수표

아래 표는 교육 식별자를 빠뜨리지 않기 위한 전수 목록이다. **표에 존재한다는 사실만으로 실행 성공을 뜻하지 않는다.** 실행 전 `GET /api/reference/edu-capabilities`에서 해당 ID, `requiredFields`, `requiredRole`, `failurePoints`, `sourcePath`, `tests`를 확인하고, 실행 결과와 DB·Target·Outbox·Audit를 대사한다.

| 교육 ID | 확인할 기능 | 활성 조건 | Source 판정 | Runtime 판정 |
|---|---|---|---|---|
| `EDU-DEV-01` | 생성 도구 기반 신규 업무 영역 생성 | 기본 기능 또는 기능 정의의 `configurationKey` | 카탈로그·Handler 전수 대조 재확인 필요 | 미검증 |
| `EDU-DEV-02` | 권한·범위가 적용된 목록·상세 조회 | 기본 기능 또는 기능 정의의 `configurationKey` | 카탈로그·Handler 전수 대조 재확인 필요 | 미검증 |
| `EDU-DEV-03` | 등록·수정·상태 변경과 Audit | 기본 기능 또는 기능 정의의 `configurationKey` | 카탈로그·Handler 전수 대조 재확인 필요 | 미검증 |
| `EDU-DEV-04` | 동시 수정과 Expected Version 충돌 | 기본 기능 또는 기능 정의의 `configurationKey` | 카탈로그·Handler 전수 대조 재확인 필요 | 미검증 |
| `EDU-DEV-05` | 지급 등록 멱등성·응답 유실·결과 대사 | 기본 기능 또는 기능 정의의 `configurationKey` | Handler·JDBC Command Consumer Binding 정적 확인 | 미검증 |
| `EDU-DEV-06` | Same-JVM·Remote 호출 동등성 | 기본 기능 또는 기능 정의의 `configurationKey` | 카탈로그·Handler 전수 대조 재확인 필요 | 미검증 |
| `EDU-DEV-07` | 메시지 Outbox·Inbox·중복 소비·재처리 | 기본 기능 또는 기능 정의의 `configurationKey` | 카탈로그·Handler 전수 대조 재확인 필요 | 미검증 |
| `EDU-DEV-08` | 파일 Upload·검사·Attachment·Download | 기본 기능 또는 기능 정의의 `configurationKey` | 카탈로그·Handler 전수 대조 재확인 필요 | 미검증 |
| `EDU-DEV-09` | 외부 REST 조회와 UNKNOWN_RESULT | 기본 기능 또는 기능 정의의 `configurationKey` | 카탈로그·Handler 전수 대조 재확인 필요 | 미검증 |
| `EDU-DEV-10` | 고정길이 전문 기관 이체 | 기본 기능 또는 기능 정의의 `configurationKey` | 카탈로그·Handler 전수 대조 재확인 필요 | 미검증 |
| `EDU-DEV-11` | Permission·Data Scope·Masking·Audit | 기본 기능 또는 기능 정의의 `configurationKey` | 카탈로그·Handler 전수 대조 재확인 필요 | 미검증 |
| `EDU-DEV-12` | Cache·Feature Flag·Secret Rotation | 기본 기능 또는 기능 정의의 `configurationKey` | 카탈로그·Handler 전수 대조 재확인 필요 | 미검증 |
| `EDU-DEV-13` | Notification·비동기 Export·Download Audit | 기본 기능 또는 기능 정의의 `configurationKey` | 카탈로그·Handler 전수 대조 재확인 필요 | 미검증 |
| `EDU-DEV-14` | Oracle·PostgreSQL·MariaDB DB Migration 의미 일치 | 기본 기능 또는 기능 정의의 `configurationKey` | 카탈로그·Handler 전수 대조 재확인 필요 | 미검증 |
| `EDU-DEV-15` | 업무 장애 주입·정상화·운영 인계 | 기본 기능 또는 기능 정의의 `configurationKey` | 카탈로그·Handler 전수 대조 재확인 필요 | 미검증 |
| `EDU-DEV-16` | 대용량 목록 검색·정렬·Cursor Pagination | 기본 기능 또는 기능 정의의 `configurationKey` | 카탈로그·Handler 전수 대조 재확인 필요 | 미검증 |
| `EDU-DEV-17` | 대량 등록 Preview·부분 오류·재업로드 | 기본 기능 또는 기능 정의의 `configurationKey` | 카탈로그·Handler 전수 대조 재확인 필요 | 미검증 |
| `EDU-DEV-18` | 논리 삭제·복원·Retention 만료 | 기본 기능 또는 기능 정의의 `configurationKey` | 카탈로그·Handler 전수 대조 재확인 필요 | 미검증 |
| `EDU-DEV-19` | 기준일·유효기간이 있는 기준정보 | 기본 기능 또는 기능 정의의 `configurationKey` | 카탈로그·Handler 전수 대조 재확인 필요 | 미검증 |
| `EDU-DEV-20` | 다단계 업무 State Machine과 취소·재개 | 기본 기능 또는 기능 정의의 `configurationKey` | 카탈로그·Handler 전수 대조 재확인 필요 | 미검증 |
| `EDU-DEV-21` | Transactional Outbox 게시 지연·재시작 | 기본 기능 또는 기능 정의의 `configurationKey` | 카탈로그·Handler 전수 대조 재확인 필요 | 미검증 |
| `EDU-DEV-22` | 서비스 간 Saga Compensation·수동 확정 | 기본 기능 또는 기능 정의의 `configurationKey` | 카탈로그·Handler 전수 대조 재확인 필요 | 미검증 |
| `EDU-DEV-23` | 공통 Validation·Error Contract·OpenAPI 일치 | 기본 기능 또는 기능 정의의 `configurationKey` | 카탈로그·Handler 전수 대조 재확인 필요 | 미검증 |
| `EDU-DEV-24` | 장시간 비동기 Operation 조회·취소 | 기본 기능 또는 기능 정의의 `configurationKey` | 카탈로그·Handler 전수 대조 재확인 필요 | 미검증 |
| `EDU-DEV-25` | Webhook 서명·재전송·Replay 방지 | 기본 기능 또는 기능 정의의 `configurationKey` | 카탈로그·Handler 전수 대조 재확인 필요 | 미검증 |
| `EDU-DEV-26` | SFTP 수신·송신·완료 파일 원자 처리 | 기본 기능 또는 기능 정의의 `configurationKey` | 카탈로그·Handler 전수 대조 재확인 필요 | 미검증 |
| `EDU-DEV-27` | SOAP·XML 외부기관 연계와 장애 처리 | 기본 기능 또는 기능 정의의 `configurationKey` | 카탈로그·Handler 전수 대조 재확인 필요 | 미검증 |
| `EDU-DEV-28` | 대용량 Multipart Upload·중단 재개 | 기본 기능 또는 기능 정의의 `configurationKey` | 카탈로그·Handler 전수 대조 재확인 필요 | 미검증 |
| `EDU-DEV-29` | 악성코드 검사·격리·승인 해제 | 기본 기능 또는 기능 정의의 `configurationKey` | 카탈로그·Handler 전수 대조 재확인 필요 | 미검증 |
| `EDU-DEV-30` | Object Storage 보존·Version·Legal Hold | 기본 기능 또는 기능 정의의 `configurationKey` | 카탈로그·Handler 전수 대조 재확인 필요 | 미검증 |
| `EDU-DEV-31` | 다중 채널 Notification 선호·Retry·대체 채널 | 기본 기능 또는 기능 정의의 `configurationKey` | 카탈로그·Handler 전수 대조 재확인 필요 | 미검증 |
| `EDU-DEV-32` | 개인정보 암호화·Tokenization·Key Rotation | 기본 기능 또는 기능 정의의 `configurationKey` | 카탈로그·Handler 전수 대조 재확인 필요 | 미검증 |
| `EDU-DEV-33` | 인증 Token 만료·갱신·폐기·Session 강제 종료 | 기본 기능 또는 기능 정의의 `configurationKey` | 카탈로그·Handler 전수 대조 재확인 필요 | 미검증 |
| `EDU-DEV-34` | API Rate Limit·호출 주체별 Quota·초과 처리 | 기본 기능 또는 기능 정의의 `configurationKey` | 카탈로그·Handler 전수 대조 재확인 필요 | 미검증 |
| `EDU-DEV-35` | Feature Flag Canary·Kill Switch·사용자 Segment | 기본 기능 또는 기능 정의의 `configurationKey` | 카탈로그·Handler 전수 대조 재확인 필요 | 미검증 |
| `EDU-DEV-36` | Cache Stampede·Negative Cache·원본 정합성 | 기본 기능 또는 기능 정의의 `configurationKey` | 카탈로그·Handler 전수 대조 재확인 필요 | 미검증 |
| `EDU-DEV-37` | 온라인 Distributed Lease·Fencing·소유권 상실 | 기본 기능 또는 기능 정의의 `configurationKey` | 카탈로그·Handler 전수 대조 재확인 필요 | 미검증 |
| `EDU-DEV-38` | Multi-tenant 격리·설정·Data Scope | 기본 기능 또는 기능 정의의 `configurationKey` | 카탈로그·Handler 전수 대조 재확인 필요 | 미검증 |
| `EDU-DEV-39` | 업무일자·Timezone·Holiday Calendar | 기본 기능 또는 기능 정의의 `configurationKey` | 카탈로그·Handler 전수 대조 재확인 필요 | 미검증 |
| `EDU-DEV-40` | 금액·통화·Rounding·환율 Version | 기본 기능 또는 기능 정의의 `configurationKey` | 카탈로그·Handler 전수 대조 재확인 필요 | 미검증 |
| `EDU-DEV-41` | Audit Evidence Export·무결성 Hash·검증 | 기본 기능 또는 기능 정의의 `configurationKey` | 카탈로그·Handler 전수 대조 재확인 필요 | 미검증 |
| `EDU-DEV-42` | Log·Metric·Trace Correlation과 Sampling | 기본 기능 또는 기능 정의의 `configurationKey` | 카탈로그·Handler 전수 대조 재확인 필요 | 미검증 |
| `EDU-DEV-43` | API Version 전환·하위 호환·폐기 | 기본 기능 또는 기능 정의의 `configurationKey` | 카탈로그·Handler 전수 대조 재확인 필요 | 미검증 |
| `EDU-DEV-44` | Event Schema 진화·Compatibility·DLQ | 기본 기능 또는 기능 정의의 `configurationKey` | 카탈로그·Handler 전수 대조 재확인 필요 | 미검증 |
| `EDU-DEV-45` | 조회 Model·Search Index Eventual Consistency | 기본 기능 또는 기능 정의의 `configurationKey` | 카탈로그·Handler 전수 대조 재확인 필요 | 미검증 |

판정 규칙:

1. `Source 판정`이 **재확인 필요**이면 Handler·Resource Contract·Consumer·Test를 같은 Commit에서 대조한다.
2. `Runtime 판정`은 실제 실행 명령, 종료 코드, DB·외부 기반, Browser/ADM 결과가 없으면 `미검증`으로 유지한다.
3. 실행 API가 2xx여도 업무 원장·Target·Outbox·Audit가 같은 `operationId`를 가리키지 않으면 실패로 판정한다.
4. `UNKNOWN_RESULT`와 `PARTIAL_SUCCESS`는 신규 요청을 만들지 않고 기존 Operation에서 Reconciliation한다.

## 19. 완료 점검표

- [ ] 기능 Owner와 Consumer가 명확하다.
- [ ] 상태·권한·Data Scope·Audit이 Source와 일치한다.
- [ ] MariaDB·PostgreSQL·Oracle 영향이 반영됐다.
- [ ] 멱등성·동시성·Timeout·응답 유실을 시험했다.
- [ ] 결과 미확정과 부분 성공의 대사 절차가 있다.
- [ ] Local·Remote 계약이 일치한다.
- [ ] 선택 Starter와 미선택 제거를 확인했다.
- [ ] OpenAPI·JavaDoc·Generated Client가 같은 Commit이다.
- [ ] ADM에서 Query·Command·Audit을 확인했다.
- [ ] 운영 인계와 Rollback 기준을 전달했다.
- [ ] 실행하지 않은 항목은 미검증으로 표시했다.

## 20. 배포 인계 절차

개발 완료 표시는 Source 작성 시점이 아니라 운영 인계가 검수된 시점에 판단한다.

### 20.1 인계 묶음

| 항목 | 필수 내용 |
|---|---|
| Source 기준 | Repository·Branch·exact Commit |
| Artifact | 이름·Version·SHA-256·SBOM |
| API | OpenAPI·오류·권한·Timeout·Idempotency |
| DB | Vendor별 Migration·Verify·Rollback/Recovery |
| Config | Key·환경변수·Default·필수·Secret·재기동 |
| Messaging | Binding·Destination·Schema·Retry·DLQ·대사 |
| File·외부 연계 | Endpoint·Checksum·Timeout·Receipt·보존 |
| 관측 | Log·Metric·Trace·Audit와 상관 식별자 |
| 운영 조치 | 조회·재시도·재처리·Reconcile·Rollback |
| 검증 | 실행 명령·환경·Exit Code·Sanitized Evidence |
| 제한 | 미구현·미검증·재확인 필요 |

### 20.2 배포 전 확인 명령

```powershell
$repo='C:\dev\projects\jck\202412_01_CPF'
git -C $repo rev-parse HEAD
git -C $repo status --short
& (Join-Path $repo 'gradlew.bat') clean test assemble qualityGate --no-daemon
```

실행하지 않은 명령을 성공으로 기록하지 않는다. 실패하면 최초 실패 Task·관련 Source·재현 조건을 기록한다.

## 21. 오류·부분 실패·정상화 결정표

| 상황 | 금지 | 우선 행동 | 종료 판정 |
|---|---|---|---|
| Version 충돌 | 최신 Row 덮어쓰기 | 현재 상태 재조회·의도 병합 | Expected Version 일치 |
| DB Commit 후 응답 유실 | 신규 업무 생성 | Idempotency·업무 원장·Operation 조회 | 실제 결과 확정 |
| 외부 전송 후 Timeout | 무조건 재전송 | Attempt·Receipt·상대 조회 | 중복 없음·결과 확정 |
| 일부 대상 성공 | 전체 재실행 | 성공 대상 유지·실패 대상만 처리 | Target별 결과 대사 |
| Broker Consumer 실패 | Message 삭제 | Retry·DLQ·Inbox·업무 원장 확인 | Backlog·DLQ·업무 대사 |
| File 일부 처리 | 원본 덮어쓰기 | Checksum·Checkpoint·행별 결과 확인 | 건수·금액·Hash 대사 |
| Config 부분 적용 | 신규 변경 겹침 | Target Version·Checksum 수집 | Drift 0 |

## 22. EDU 전수 검증 규칙

EDU ID가 문서나 Catalog에 존재하는 것만으로 개발 상태를 `완료`로 표시하지 않는다. 각 EDU는 다음 일곱 근거를 연결한다.

```text
Definition·Resource Contract
→ Handler·Owner Package
→ 실제 Consumer Binding
→ Config·Migration
→ 정상·오류·Fault Test
→ Runtime Operation·Target·Audit
→ ADM·Log·Metric·Trace Evidence
```

### 22.1 실행 점검 예

```powershell
$repo='C:\dev\projects\jck\202412_01_CPF'
pwsh -NoProfile -ExecutionPolicy Bypass -File (Join-Path $repo 'cpf-tools\scripts\start-cpf-local.ps1') -Mode integration
# EDU API 호출은 실제 Port·Role·필수 입력을 Capability 조회 결과에서 가져온다.
pwsh -NoProfile -ExecutionPolicy Bypass -File (Join-Path $repo 'cpf-tools\scripts\status-cpf-local.ps1')
```

EDU 종료 후 업무 원장·Operation·Target·Outbox·Audit의 식별자와 건수·금액·Hash를 대사한다.

## 23. 개발 검토 요청으로 전환할 조건

다음 중 하나라도 해당하면 문서에 임의 대안을 만들지 않고 개발 검토 요청으로 전달한다.

- Public API·SPI가 있으나 실제 Consumer가 없다.
- Sample·Marker만 있고 업무 효과가 없다.
- Property가 있으나 Consumer가 읽지 않는다.
- DB Migration은 있으나 Verify·Rollback/Recovery가 없다.
- Frontend Button은 있으나 Backend Permission이 없다.
- Timeout 후 결과 조회·Reconcile 경로가 없다.
- Provider Container는 있으나 Starter·Adapter·Test가 없다.
- EDU ID는 있으나 Handler·Consumer·Assertion이 없다.
- Generator가 파일을 만들지만 Build·DB·OpenAPI 검증을 연결하지 않는다.

요청에는 Requirement ID, Owner, 실제 경로, Expected, Actual, 재현 명령, 영향 범위와 필요한 시험을 포함한다.
