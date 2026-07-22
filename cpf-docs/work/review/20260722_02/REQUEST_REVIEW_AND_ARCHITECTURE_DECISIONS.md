# 요청 리뷰와 확정 아키텍처 결정

## 1. 기존 요청서 리뷰

기존 `CPF_CURRENT_WORK_REQUEST.md`는 다음 장점이 있다.

- 중간 Push를 완료로 믿지 않는 원칙
- 공식 Module·Package 전환
- Generator, Batch, External, Frontend, SQL, Security, EDU를 한 번에 묶은 범위
- Evidence와 완료 금지 조건
- Commit·Push 금지 가드레일

그러나 최신 WIP와 이후 합의를 반영하지 못한다.

- 기준 Commit이 과거 `e35b7a0`
- `cpf-core` SystemCode를 PFW 유지 권장
- `cmnDB`와 `cpf-common` 최소화 정책 없음
- Fixed-Length Core Ownership 없음
- 업무 채번 BZA Sample 정책 없음
- CenterCutRunner·Agent Failover·Failed-only Reprocess가 상세하지 않음
- Core Developer Experience와 표준 자료형이 부족
- Vue Web Server/WAS 분리 배포가 부족
- Install과 Reset 분리 요구가 부족
- Guide·README의 지속 정본화 운영이 부족

따라서 기존 요청의 장점은 보존하고, 충돌 정책을 제거하며, 최신 합의를 독립 실행 가능한 차기 요청서로 재작성했다.

## 2. 확정 제품 정책

### 2.1 공식 명칭

- 제품명: `Core Platform Framework`
- Quality 지향 표현: `Business Platform`
- Root Project: `cpf-core-platform-framework`
- 공식 Module은 `cpf-` Prefix
- Java Package는 `com.cpf.<domain>`
- 내부 식별자만 3자리 대문자 SystemCode

### 2.2 Module과 SystemCode

| Module | SystemCode | Owner |
|---|---:|---|
| cpf-core | CPF | 기술 공통 API·SPI·Runtime |
| cpf-common | CMN | 고객사 업무 공통 Extension |
| cpf-admin | ADM | 플랫폼 운영 Control Plane |
| cpf-biz-admin | BZA | 고객 업무 관리자 |
| cpf-batch | BAT | Batch·Scheduler·Agent·Runner·Worker·Center-Cut |
| cpf-gateway | GWY | 외부 진입·라우팅·정책 집행 |
| cpf-member | MBR | 생성형 업무 예제 |
| cpf-account | ACC | 생성형 업무 예제 |
| cpf-reference | REF | 참조·EDU |
| cpf-external | EXS | 기관별 대외연계 업무 |

### 2.3 의존성 방향

```text
업무 Module
→ cpf-common
→ cpf-core

cpf-gateway
→ cpf-core

cpf-batch
→ cpf-core + 업무 Contract

cpf-admin
→ 공통 운영 Contract
→ 특정 구현 Class 직접 의존 금지

cpf-biz-admin
→ 업무 API
→ cpf-core 내부 Runtime 직접 제어 금지
```

금지:

- `cpf-core -> cpf-common/업무 Module`
- 업무 Module -> `cpf-core.internal`
- 다른 업무 Module DB 직접 접근
- ADM/BZA가 DB 상태를 직접 변경
- BZA가 온라인 업무 Runtime 필수 의존점이 됨

## 3. `cpf-common`과 `cmnDB`

### 3.1 최종 목표

```text
cmnDB
└─ cmn_sample_item  # 실제 최종 명칭은 DB Naming 표준에 맞춰 확정
```

목적:

- DB 연결
- Migration
- CRUD
- Validation
- 단건·목록
- Offset Paging
- Slice
- Keyset/Cursor
- 검색·정렬
- Optimistic Lock
- Transaction Rollback
- Audit 기본 Column

기본 CPF 설치에는 추정성 업무 공통 Table을 넣지 않는다.

### 3.2 제거·이동 대상

- `cmn_sequence*`: 제거
- `cmn_fixed_length_*`: Core/External로 재소유
- `cmn_notification_log`: 실제 Owner 검수 후 이동/삭제
- `cmn_business_log`: 모호한 통합 Log 금지
- `cmn_edu_query_item`: 단일 Sample Table로 통합
- 소비자 없는 Mapper·Repository·Service·Seed·Guide도 함께 제거

## 4. 업무 채번 결정

### 4.1 프레임워크 기본 기능 아님

CPF는 고객번호·접수번호·계약번호 같은 업무 채번 Engine을 Core/Common 기본 기능으로 제공하지 않는다.

Core가 제공하는 것은 기술 식별자뿐이다.

- transactionGlobalId
- traceId
- segmentId
- idempotencyKey
- UUID/ULID 등 범용 도구

### 4.2 BZA Customization Sample

BZA는 개발·EDU 또는 선택 설치에서 다음 참조 기능을 제공한다.

- 규칙 목록·상세·등록·수정
- Prefix·Suffix·날짜·Padding
- 현재값
- 시험 발급
- 발급 이력
- 권한·승인·감사
- 다중 인스턴스·Lock 고려 예제
- 최대값·결번·Rollback 정책 설명
- Custom SPI·Template

실제 Runtime Owner는 고객사가 선택한다.

- 고객 공통: 고객사 `cpf-common` 확장
- 특정 업무: 해당 업무 Module
- 중앙 채번: 별도 고객 업무 Service

BZA Admin Application을 온라인 거래가 매번 호출하는 구조는 금지한다.

## 5. 고정길이 전문 결정

### cpf-core

- Layout·Field 표준 모델
- Byte Length
- Encoding
- Padding·Alignment
- Header·Body·Trailer
- 반복 Group
- 날짜·숫자·금액 Converter
- Parser·Writer
- Validation
- Masking SPI
- Streaming
- 오류 Offset·Field
- Version Contract
- 확장 Codec·Converter·Validator SPI

### cpf-external

- 기관별 Layout
- 기관별 Mapping
- Endpoint·Auth
- 기관별 Version
- 기관 오류
- 송수신 Adapter
- Retry·Unknown Result·Reconciliation

### cpf-admin

- Owner API를 통한 조회·등록·승인·감사 UI
- 직접 Table 변경 금지

동적 Layout DB 저장이 정말 필요하면 Core-owned Table로 제공하되, Source Consumer·권한·감사·Migration·Runtime Evidence가 모두 있어야 한다.

## 6. Batch와 Center-Cut 결정

`cpf-batch`가 전부 소유한다.

- Batch Job·Step·Schedule·Calendar
- Batch Agent
- CenterCutRunner
- Worker
- Claim·Lease·Heartbeat·Fencing
- Center-Cut Job·Item·Attempt
- Target Generation
- Global TPS·Backpressure
- Pause·Resume·Cancel
- Failed-only Reprocess
- Unknown Result
- Compensation·Recovery

Physical DB도 BAT Ownership을 드러내도록 정리한다.

## 7. CenterCutRunner 결정

`CenterCutRunner`는 Batch Agent와 구분되는 Runtime 책임이다.

```text
요청 Parameter 검증
→ Job 생성
→ Immutable Parameter/Policy Snapshot 저장
→ Target Item 생성
→ Runner Claim
→ Worker Dispatch
→ Item Attempt
→ 결과·오류·재시도 저장
→ 집계
→ ADM Timeline
```

Runner는 Batch Agent에 내장하거나 별도 Process로 배포할 수 있어야 하며 특정 배포 방식에 종속되면 안 된다.

## 8. Agent Primary/Secondary 결정

Pool/Capability 기반 선택에 더해 Primary/Secondary를 제공한다.

- Automatic
- Primary Priority
- Fixed
- Distributed
- Zone Priority

Failover는 실행 전과 실행 중을 구분한다. 실행 중 장애는 무조건 재실행하지 않고 Checkpoint·Idempotency·외부 결과를 확인한다.

Duplicate 방지:

```text
Claim + Lease + Fencing Token + Attempt ID + Idempotency Key
```

## 9. Core Developer Experience 결정

Class명은 `Cpf` Prefix, Method는 짧고 자연스럽게 한다.

```java
CpfDates.format(...)
CpfStrings.trimToNull(...)
CpfLists.requireMaxSize(...)
CpfMaps.getString(...)
```

금지:

- `CommonUtil`
- `CpfUtils`
- `Helper`
- `Manager`
- 의미 없는 JDK Wrapper

표준 자료형:

- `ListResult<T>`
- `PageRequest`, `PageResult<T>`
- `SliceResult<T>`
- `CursorRequest`, `CursorResult<T>`
- `AsyncAcceptedResult`
- `OperationStatusResult`
- `BulkResult`
- `ValidationResult`
- Typed `CpfAttributes`

## 10. ADM/BZA 결정

### ADM

플랫폼 운영:

- 거래
- Batch·Center-Cut
- Agent·Runner·Worker
- Gateway·Registry
- Security·Audit
- Log·Trace
- Recovery

### BZA

고객 업무 운영:

- 업무 관리자
- 업무 승인
- 업무 데이터 조회
- 고객 Custom Sample
- 채번 Customization Sample

### Frontend 배포

```text
Vue
→ Node/Vite 독립 Build
→ Static Artifact
→ Nginx/Apache

Java API
→ Gradle Build
→ JAR/WAR
→ WAS/Java Runtime
```

UI를 Java Resource에 넣는 방식만 제공해서는 안 된다.

## 11. DB 초기화 결정

사용자는 회사와 집 PC 모두 CPF 소유 DB를 초기화한 뒤 재설치한다.

반드시:

- 정확한 CPF Schema Allowlist
- Dry Run
- Apply Flag
- 다른 Schema 삭제 금지
- User·Grant 분리
- Empty Install
- Seed Idempotency
- Reinstall
- Upgrade
- Rollback
- 전체 Module Runtime

Wildcard Drop은 금지한다.

## 12. 문서 결정

- 구현 중 정본: Markdown
- 최종 DOCX/PDF: 기능·구조 안정 후
- README: 제품 소개와 사용 입구
- 작업·검수·Gap·Evidence: `cpf-docs`
- 동일 역할 중복 문서 금지
- Codex가 Guide를 수정해도 최종 품질은 실제 Git 검수로 판단
