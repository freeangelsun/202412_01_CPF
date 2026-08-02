# CPF 활성 개발·QA 통합 정본

- Repository: `freeangelsun/202412_01_CPF`
- Branch: `master`
- 기준 SHA: `2a86ff42799eddffaae87d38ae68632726a2c495`
- 통합 생성 시각: `2026-08-02 22:52:18 KST`
- development_status: `부분 구현`
- verification_status: `실패`
- 사람용 활성 문서: **이 파일 1개**
- 상세 상태·검증 입력: 같은 폴더의 CSV·Manifest만 사용
- 과거 상세 경과: Git History로 추적

## 1. 읽기와 갱신 규칙

1. 개발 GPT·QA GPT·Codex는 이 파일부터 읽는다.
2. 현재 요청, 개발요건, Architecture 결정, 작업 전 리뷰, 구현 보고, 자체 리뷰, QA 결과, Handover를 새 파일로 만들지 않고 이 파일의 해당 절에 누적 갱신한다.
3. Requirement·Scenario·Source Findings·Starter Catalog·Stage Plan·Change/Product Delete Work Items처럼 도구가 읽는 구조화 데이터만 별도 CSV로 유지한다.
4. 날짜·QA 번호·R1/R2/FINAL 이름의 중복 문서와 세션별 디렉터리를 새로 만들지 않는다.
5. 완료된 과거 문서는 유효 내용을 이 정본과 Matrix에 통합한 뒤 Git History로 보존하고 Repository에서는 제거한다.
6. 신규 문서·폴더가 반드시 필요하면 Owner, Consumer, 기존 정본에 통합할 수 없는 이유, 폐기 조건을 이 문서에 먼저 기록한다.

## 2. 절대 보호 경로

다음 경로는 별도 Owner 관리 범위이며 이 QA 정리에서 읽기 전용이다.

```text
cpf-docs/deliverables/**
cpf-docs/guides/**
cpf-docs/environment/docker/**
cpf-tools/environment/docker-development-test/**
```

수정, 이동, 이름 변경, 통합, 삭제 후보 등록, 자동 포맷, 일괄 치환, Stage를 금지한다.
문제가 발견되면 본 문서의 `Protected Owner Actions` 절에 요청만 기록한다.

## 3. 축소 후 활성 파일 구조

```text
cpf-docs/
├─ governance/
│  ├─ CPF_FINAL_TARGET_REQUIREMENTS.md
│  ├─ CPF_DOCUMENT_CONTROL_POLICY.md
│  └─ 제품 Architecture·Lifecycle 정책 정본
└─ work/
   ├─ CPF_CURRENT_WORK_REQUEST.md
   ├─ CPF_REQUIREMENT_MATRIX.csv
   ├─ CPF_SCENARIO_MATRIX.csv
   ├─ CPF_SOURCE_FINDINGS.csv
   ├─ CPF_STARTER_VALUE_CATALOG.csv
   ├─ CPF_PUBLIC_SURFACE_CATALOG.csv
   ├─ CPF_STAGE_PLAN.csv
   ├─ CPF_CHANGE_MANIFEST.csv
   ├─ CPF_PRODUCT_DELETE_WORK_ITEMS.csv
   ├─ CPF_DELETE_MANIFEST.txt
   ├─ CPF_DELETE_ONE_LINE.ps1.txt
   └─ CPF_PACKAGE_MANIFEST.json
```

`current`, `state`, `handover`, `review`, `codex/<qa>`, `manifest`에 같은 내용을 반복 저장하지 않는다.
폴더는 기능 경계가 아니라 문서 회차를 나타내기 위해 만들지 않는다.

## 4. 현재 작업과 최종 개발요건

- Repository: `freeangelsun/202412_01_CPF`
- Branch: `master`
- Reviewed exact SHA: `9a9634eb1f28071d47c205cc35227b6d013a4536`
- Previous master SHA: `6a9890ef19ae54e6e3186ca011d5d7f984d49d9c`
- Previous QA38 baseline: `dafe5c0e5260ea8149234e8ab2e75347e75338c1`
- `development_status = 부분 구현`
- `verification_status = 실패`
- Runtime/DB/Frontend/Supply-chain = `미검증`
- Active QA Request: `cpf-docs/work/current/CPF_QA39_FINAL_DEVELOPMENT_REQUIREMENTS.md`
- Active Matrix: `cpf-docs/quality/CPF_QA39_FINAL_REQUIREMENT_MATRIX.csv`
- Active Requirement Count: `44`
- Active Scenario Count: `37`
- Active Delete Work Items: `cpf-docs/work/manifest/CPF_QA39_DELETE_WORK_ITEMS.csv`
- Developer Report Template: `cpf-docs/work/review/CPF_QA39_DEVELOPER_REPORT_AND_SELF_REVIEW_TEMPLATE.md`

## 요건 우선순위 — 필수

1. 최상위 목표와 Architecture/Specification
2. **`CPF_QA39_FINAL_DEVELOPMENT_REQUIREMENTS.md`와 `CPF_QA39_FINAL_REQUIREMENT_MATRIX.csv`**
3. `CPF_QA39_SELF_DEVELOPED_REQUIREMENTS.csv`와 개발 GPT가 추가 발굴한 자체요건
4. 구현 편의 판단·개발 리포트

**QA 개발요건과 자체 개발요건이 충돌하면 QA 개발요건이 무조건 우선한다.** 자체요건은 QA 요건을 보강할 수만 있고 약화·변경·취소할 수 없다. 충돌을 발견하면 해당 자체요건을 구현하지 말고 `CONFLICT`로 기록한 뒤 QA 요건에 맞게 수정한다.


## 최종 결정

- 완전 제거: AOP Service Access, Validation Starter, Resilience Starter, Feature Flag Starter, 미등록 FTPS/gRPC/S3/Realtime/SMB/SOAP/Webhook
- 공개 Aggregate/Profile 제거·내부화: Security/Cache Aggregate, Provider별·기술별 Profile, Quartz 공개 Starter
- 유지 Group: Data, Messaging, Integration, File, Notification, Security, Platform Operations
- Profile: minimal-domain, web-api, secure-api, browser-bff, event-service, batch-service
- OpenAPI는 web-api, Scheduler는 batch-service에 흡수

## 유지 조건

유지 Capability는 OSS Bean/메서드를 그대로 노출하지 않는다. 업무 개발자가 사용하는 간단한 CPF Public API, OSS 타입 비노출, Provider 교체, 고객사 SPI, 표준 보안·감사·마스킹, 실패·UNKNOWN·복구, 운영 추적·재처리, Generator 자동 조립을 실제 Consumer와 Runtime Evidence로 제공해야 한다.

## 개발 리포트 의무

개발 GPT는 변경 Source만 던지지 않는다. 구현 종료 전에 Developer Implementation Report와 독립 Self Review를 작성하여 exact SHA, 변경 파일/라인, Requirement 추적, Architecture 결정, 실제 명령·환경·시간·결과, 실패·미실행, 삭제·잔여참조, Evidence, 회귀 위험을 남긴다. QA는 보고를 그대로 승인하지 않지만 이 보고를 검수 진입점으로 사용한다.

## 정본·정리 규칙

- Repository Root에는 QA 안내 파일을 새로 만들지 않는다. 모든 QA 산출물은 `cpf-docs/**` 아래에 둔다.
- 활성 자체 개발요건 정본은 `CPF_QA39_SELF_DEVELOPED_REQUIREMENTS.csv` 하나다.
- 이전 QA 패키지가 만든 루트 파일과 중복·충돌 문서는 `CPF_QA39_REPOSITORY_CLEANUP_PATHS.txt`에 exact path로 관리한다.
- 제품 Source 삭제와 QA 문서 정리는 서로 다른 Manifest와 한 줄 명령으로 분리한다.

## Generated Domain 표준 상속과 예외

- Generated Domain은 CPF 필수 표준을 자동 상속하지만 모든 Profile·Capability를 기본 포함하지 않는다.
- 기본 생성은 최소 공통 기반이며 선택한 기능만 dependency/config/bean/SQL/lock에 추가한다.
- CPF 표준 Starter가 존재하는 기능의 외부 OSS 직접 사용·직접 Bean·수동 설정은 기본 차단한다.
- 불가피한 예외는 Platform Source 변경 없이 이용 Domain/Module의 선언형 설정으로 등록한다.
- Convention Plugin과 Runtime Gate는 승인·범위·만료·Artifact Version·Config Hash를 검증하고 미등록·미승인·만료·drift는 fail-closed한다.
- QA39-043과 QA39-044 및 SC-032~SC-037을 필수 수용 기준으로 적용한다.

---

## 1. 기준

- Exact reviewed SHA: `9a9634eb1f28071d47c205cc35227b6d013a4536`
- `development_status = 부분 구현`
- `verification_status = 실패`
- 보호 경로: `cpf-docs/deliverables/**`, `cpf-docs/guides/**`, `cpf-docs/environment/docker/**`, `cpf-tools/environment/docker-development-test/**`
- 실제 Commit/Push/Delete는 이 QA 패키지에서 수행하지 않았다.

## 요건 우선순위 — 필수

1. 최상위 목표와 Architecture/Specification
2. **`CPF_QA39_FINAL_DEVELOPMENT_REQUIREMENTS.md`와 `CPF_QA39_FINAL_REQUIREMENT_MATRIX.csv`**
3. `CPF_QA39_SELF_DEVELOPED_REQUIREMENTS.csv`와 개발 GPT가 추가 발굴한 자체요건
4. 구현 편의 판단·개발 리포트

**QA 개발요건과 자체 개발요건이 충돌하면 QA 개발요건이 무조건 우선한다.** 자체요건은 QA 요건을 보강할 수만 있고 약화·변경·취소할 수 없다. 충돌을 발견하면 해당 자체요건을 구현하지 말고 `CONFLICT`로 기록한 뒤 QA 요건에 맞게 수정한다.


## 2. 목표

Starter 수를 늘리는 것이 아니라 개발자가 OSS를 직접 사용할 때보다 **편하고, 확장 가능하고, 안전하고, 운영 가능하며, Provider 교체가 쉬운 CPF 제품 단위**만 유지한다.

유지 Capability의 필수 가치:

- 업무 의도 중심의 간단한 CPF Public API
- OSS 타입·예외·설정 객체 비노출
- Provider 변경 시 업무 Source 무변경
- 고객사 Provider 확장 SPI와 Conformance Testkit
- 표준 Context·Header·보안·감사·마스킹
- 멱등성·실패·UNKNOWN·재시도·복구·Reconcile
- 운영 조회·추적·승인·재처리
- Generator 자동 조립과 resolved lock
- 실제 Consumer·Runtime·Fault Evidence

얇은 Wrapper, Bean 하나, Properties mapping, OSS 호출 한 번은 가치로 인정하지 않는다.

## 3. 최종 Starter 판정

### 완전 제거

1. `cpf-starter-aop-service-access`
2. `cpf-starter-validation`
3. `cpf-starter-resilience`
4. `cpf-starter-featureflag`
5. 미등록 FTPS
6. 미등록 gRPC
7. 미등록 Object Storage S3
8. 미등록 Realtime WebFlux
9. 미등록 SMB
10. 미등록 SOAP
11. 미등록 Webhook

Starter 폴더만 지우지 않는다. 관련 AutoConfiguration, CPF Wrapper, 가치 없는 Core API/SPI/DTO/Exception, Config, Test, BOM, Catalog, Generator, Sample, 문서, Evidence를 양방향 Consumer 추적 후 함께 제거한다. 미등록 7개 모듈을 settings에 등록하거나 제품화하는 대안은 허용하지 않는다.

### 공개면 제거 후 내부화·통합

- `cpf-starter-base`: 내부 Foundation
- Security/Cache Aggregate: 삭제 후 Group+선택형 Leaf 구조
- Provider별 Messaging/Data/Security Leaf: 내부 Provider
- OTLP: Platform Operations 내부 Exporter
- Archive/Tabular/SFTP/Attachment Leaf: File 내부 선택형 구성
- Fixed-length/ISO8583/TCP/HTTP Leaf: Integration 내부 선택형 구성
- Email/SMS SPI: Notification 내부 Provider/SPI
- Quartz 공개 Starter: cpf-batch Scheduler 내부 Provider로 이관 후 기존 경로 제거
- 기술·Provider별 Profile: 6개 Use-case Profile로 대체 후 제거

### 유지·완성할 7개 Capability Group

| Group | 포함 기능 | 유지 이유 |
|---|---|---|
| Data | JDBC, MyBatis, Caffeine, Valkey | DB Vendor·Transaction·Routing·Paging·Cache/Lock 표준과 Provider 선택 |
| Messaging | Kafka, RabbitMQ, JMS, IBM MQ, Outbox/Inbox/DLQ | 동일 API, Binding, 멱등성, 결과불명, Reconcile, 운영 재처리 |
| Integration | HTTP, TCP, Fixed-length, ISO8583 | Correlation, Timeout, 전문 Codec, UNKNOWN, Reconcile의 공통 외부연계 모델 |
| File | Attachment, Archive, Tabular, SFTP | 검사·처리·저장·전송·Checksum·Resume·보존의 파일 생명주기 |
| Notification | Email, SMS, Outbox, Receipt | 수신자·동의·Template·반송·수신결과·재발송의 업무 의미 |
| Security | Resource Server, Session, Service Identity, Secret | Principal·권한·마스킹·감사·Secret·Rotation의 일관된 정책 |
| Platform Operations | Observability, Runtime Control, Channel Registry | 시스템·인스턴스·거래 추적, 승인된 운영 제어, 상태·감사 |

### 공개 Profile 6개

`minimal-domain`, `web-api`, `secure-api`, `browser-bff`, `event-service`, `batch-service`

OpenAPI는 `web-api`에, Scheduler는 `batch-service`에 흡수한다.

## 4. Generated Domain 표준 상속과 이용 Domain 선언형 예외

### 4.1 자동 상속과 최소 기본 구성

Generated Domain과 신규 업무 Module은 CPF Core/Platform이 정의한 필수 표준 계약을 자동 상속한다. 다만 6개 Profile과 7개 Capability Group을 전부 기본 포함하지 않는다.

- 기본 생성은 Build·Runtime에 필요한 최소 공통 기반만 포함한다.
- DB·Messaging·Batch·File·Notification 등 선택 기능은 사용자가 명시한 Profile·Capability·binding에만 추가한다.
- 선택하지 않은 Provider, Bean, Config, SQL, Migration, 무거운 Library는 전이하지 않는다.
- Generator, Convention Plugin, BOM, Catalog, Golden Template, Architecture Test, resolved lock은 같은 정책 정본을 사용한다.
- 재생성·Upgrade에서 사용자 수정 영역을 보존하고 기존 Generated Domain도 동일 표준으로 이관한다.

### 4.2 CPF 표준 우회 차단

CPF 표준 Starter·Capability가 존재하는 기능을 업무 Domain이 외부 OSS Dependency, 직접 Bean, 직접 설정으로 우회하는 것은 기본 차단한다. 문서 경고만으로 끝내지 않고 Convention/Architecture Build Gate와 Runtime Gate가 fail-closed로 통제한다.

### 4.3 이용 Domain 선언형 예외

불가피한 외부 OSS 또는 고객 전용 구현은 Platform Source를 수정하거나 Fork하지 않고, **이용 Domain/Module의 선언형 설정**으로 예외를 등록할 수 있어야 한다.

예외 설정에는 최소 다음을 포함한다.

- 예외 ID, 대상 Domain/Module/Capability와 적용 환경·범위
- 외부 Artifact와 정확한 Version
- Owner, 사유, CPF 표준 경로를 사용할 수 없는 근거
- 승인자, 승인 시각, 만료일
- 보안·라이선스·Supply-chain 영향
- 운영·장애·복구 책임과 Rollback/표준 복귀 계획
- Config Hash와 Evidence

Convention Plugin과 Runtime Gate는 승인·범위·만료·Version·Hash를 검증한다. 미등록·미승인·만료·범위 초과·Version Drift·Hash 불일치는 Build 또는 Runtime을 fail-closed한다. 승인된 예외도 resolved lock, 감사, 운영 조회에 기록하고 승인 범위 밖으로 확장하지 않는다.

## 5. 개발자 API와 확장성

업무 개발자가 호출하는 것은 CPF Public API다. 내부 OSS Adapter는 숨긴다. 대표 Consumer Source에 KafkaTemplate, RabbitTemplate, JmsTemplate, SftpClient, OpenTelemetrySdk 같은 Provider 타입이 나타나지 않아야 한다.

Public API는 업무 의도와 표준 Result/Tracking을 제공하고 Framework가 Header, 직렬화, 설정, 보안, 감사, 실패 분류, UNKNOWN, 복구, 운영 추적을 처리한다. Provider형 Group은 고객사가 CPF Internal Package 없이 구현 가능한 SPI와 동일 Contract Test를 제공한다.

## 6. 삭제 작업 관리

정본:

- `cpf-docs/work/manifest/CPF_QA39_DELETE_WORK_ITEMS.csv`
- `cpf-docs/work/manifest/CPF_QA39_FINAL_DELETE_PATHS.txt`
- `cpf-docs/work/manifest/CPF_QA39_CLEANUP_ONE_LINE.ps1.txt`

개발 GPT는 삭제 전 Consumer 대체, Core API 영향, settings/BOM/catalog/generator/publication, Test/Config/SQL/문서/Evidence, 빈 폴더를 전수 확인한다. 삭제 후 exact path 부재와 Repository 잔여참조 0건을 증명한다.

## 7. 개발 리포트와 자체 리뷰 — 필수 완료 조건

개발 GPT는 다음 두 파일을 실제 결과로 작성한다.

- `cpf-docs/work/review/CPF_QA39_DEVELOPER_IMPLEMENTATION_REPORT.md`
- `cpf-docs/work/review/CPF_QA39_DEVELOPER_SELF_REVIEW.md`

최소 포함 항목:

- 시작/종료 exact SHA와 Working Tree
- Requirement별 변경 파일·Class·API·SQL·Test·Consumer
- Architecture Ownership와 Public API/SPI/Internal 경계
- 계획 대비 실제 차이와 신규 발견 결함
- 실행 명령, Profile, 환경, 시작·종료 시각, 실제 Exit/결과
- 실패·미실행·미검증 항목과 이유
- 삭제한 exact path, 대체 위치, 잔여참조 검색 결과
- 회귀 보호와 영향도
- Evidence 위치·Hash·민감정보 제거 여부
- 다음 QA가 바로 확인할 파일·라인·명령·기대 결과

보고서가 없거나 결과가 증적과 다르면 완료가 아니다. QA는 개발 보고를 그대로 승인하지 않고 독립 검수하되, 반복 탐색을 줄이기 위한 진입점으로 사용한다.

## 8. 완료 판정

- Requirement 44건과 Scenario 37건 모두 실제 Source·Consumer·Runtime·Evidence로 추적
- 부분 구현/미구현/실패/미검증/재확인 필요 0건일 때만 전체 완료
- Java25 Build, Frontend, 3 Vendor DB, Provider Runtime/Fault, Browser, Publication/SBOM을 exact SHA에서 실행
- 삭제 대상과 구 Artifact ID의 잔여참조 0건
- 6 Profile+7 Group 공개 Catalog와 Generator fixture 통과
- Developer Report/Self Review와 Codex Package 완결

## Repository 산출물 위치 및 정리 의무

- Repository Root에 QA README, 작업 메모, 임시 Index를 만들지 않는다.
- 같은 역할의 Current Request, 자체요건, Handover, Continuity 문서를 중복 유지하지 않는다.
- 개발 GPT는 작업 종료 시 가비지·중복 정본·빈 폴더·Dead Code·Stale Evidence를 다시 탐지하고 exact path Delete Manifest에 추가한다.
- QA 문서 정리용 Manifest와 제품 Source 삭제용 Manifest를 분리하며, 각 명령은 해당 Manifest의 exact path만 처리한다.

## 5. 생성형 도메인·Starter 표준 상속 및 이용 도메인 예외

- Generated Domain과 신규 업무 Module은 CPF Core/Platform의 필수 표준 계약을 자동 상속한다.
- 기본 생성은 반드시 필요한 최소 공통 기반만 포함한다.
- DB·Messaging·Batch·File·Notification 등은 사용자가 선택한 Profile·Capability·Provider binding만 조립한다.
- 미선택 기능의 Dependency, Bean, Config, SQL, Migration은 생성하거나 전이하지 않는다.
- CPF 표준 Starter·Capability가 존재하는 기능은 외부 OSS 직접 Dependency, 직접 Bean, 수동 설정 우회를 기본 차단한다.
- 불가피한 예외는 Platform Source 수정이나 Fork가 아니라 **이용 Domain/Module의 선언형 설정**으로 등록한다.
- 예외는 ID, Owner, 대상 Capability, Artifact/Version, 사유, 범위·환경, 승인자, 승인·만료 시각, 보안·라이선스 검토, Rollback·표준 복귀 계획, Config Hash를 포함한다.
- Convention Plugin과 Runtime Gate는 미등록·미승인·만료·범위 초과·Version Drift·Hash 불일치를 fail-closed한다.
- Generator resolved lock, Audit, 운영 조회, Evidence에 표준 선택과 승인 예외 결과를 남긴다.

검수는 최소 Domain, 선택 Capability, 승인 없는 우회, 승인 예외, 만료, 범위 초과, Version Drift, Hash 변조, 표준 경로 복귀를 실제 Build·Runtime으로 확인한다.

## 6. 작업 전 독립 리뷰

## Baseline

- SHA: `54bcc10887a83b933685bff462c0b0d7df824923`
- Previous baseline: `dafe5c0e5260ea8149234e8ab2e75347e75338c1`
- Review method: GitHub exact-file review and commit comparison
- Local fresh clone/build: 실행 환경 DNS 제한으로 미실행
- Independent CI evidence: 확인되지 않음

## Current state

- `development_status = 부분 구현`
- `verification_status = 실패`
- Runtime/DB/Frontend/Supply-chain = `미검증`

## Architecture findings

- 공식 Gradle graph에 Starter 49개가 등록돼 있다: Leaf 36개, Profile 13개.
- 공개 선택면과 내부 구현 단위가 구분되지 않는다.
- Security/Cache Aggregate는 상호 배타 Provider를 동시에 포함한다.
- Provider별 Event Profile이 별도 Artifact로 노출되어 사용자가 Provider 조합을 직접 관리한다.
- 유사 파일/전문/알림 기능이 개별 Starter로 과세분화됐다.
- 일부 신규 모듈은 settings에 등록조차 되지 않았다.

## Product-value findings

가치가 명확한 영역은 Messaging reliability, HTTP service call, security identity/resource server, cache port, file transfer ledger, attachment port, archive safety, TCP/ISO8583, notification outbox, secret provider registry다. 그러나 대부분 실제 Consumer, 운영 API, 다중 인스턴스, 결과불명, Runtime Evidence가 부족하다.

가치가 불충분한 영역은 현재 AOP service-access, Validation, Resilience, Feature Flag, Quartz thin configuration, OTLP exporter-only 조립이다. QA39에서는 제거를 기본값으로 한다.

## Development risks

- 한 줄 압축 Source와 wildcard import로 정적 결함이 숨는다.
- 상태 Matrix를 일괄 완료로 바꾼 뒤 Runtime을 미검증으로 남기는 방식이 반복됐다.
- Catalog/BOM/Evidence 정본이 분산돼 다른 AI가 stale 완료를 승계한다.
- Consumer build.gradle이 Profile와 Leaf를 중복 참조해 Ownership이 흐려졌다.

## Required development baseline

개발 전 `CPF_QA39_FINAL_REQUIREMENT_MATRIX.csv`를 고정하고 모든 Starter에 Value Contract와 제거 판정을 기록한다. 신규 Starter 추가는 금지하며 먼저 공개 Surface 축소와 기존 결함 수정을 수행한다.

## Final decision update

완전 제거·내부화·7개 유지 Group과 6개 Profile을 잠정안이 아닌 개발 기준선으로 확정한다. 개발 GPT 자체요건은 이 QA 기준선을 변경할 수 없다.

## Generated Domain 표준 상속 및 예외 선행 검토

현재 공개 Profile/Capability 축소만으로는 Generated Domain이 표준을 자동 상속하는지, 최소 구성인지, 외부 OSS 우회가 통제되는지 보장되지 않는다. 개발 전 QA39-043/044를 기준선으로 고정한다.

- Generator 기본값은 모든 Starter 포함이 아니라 최소 공통 기반이어야 한다.
- 선택한 기능만 dependency/config/bean/SQL/resolved lock에 추가한다.
- CPF 표준 Starter가 존재하면 외부 OSS 직접 사용은 기본 차단한다.
- 예외는 이용 Domain/Module 선언형 설정으로 열되 Platform Source를 수정하지 않는다.
- 승인·범위·만료·Version·Hash·Rollback·감사를 Build/Runtime Gate와 Negative Test로 검증한다.

## 7. Starter Architecture·제품 가치 리뷰

## 1. 판정 기준

교체 비용과 기존 구현량은 고려하지 않는다. 현재 제품 가치만 본다.

각 Starter는 다음 중 하나 이상을 실제로 제공해야 한다.

1. OSS 타입을 숨기는 안정적 CPF Public API/SPI
2. Provider 교체를 가능하게 하는 Binding/Port
3. 거래 Header, Context, 멱등성, Outbox, Reconcile
4. 보안, 권한, 감사, 마스킹, Secret 연계
5. 다중 인스턴스, Process Kill, 부분 실패, 결과불명 처리
6. 운영 조회, 승인, 재처리, 상태 추적
7. Generator와 실제 Consumer의 자동 조립
8. 검증 가능한 Runtime Evidence

단순 dependency 추가, properties mapping, Bean 하나, wrapper 메서드 하나는 가치로 인정하지 않는다.

## 2. 수량 판정

- 공식 Starter project: 49개
- Leaf/Aggregate: 36개
- Profile: 13개
- 추가 내부 library/SPI: 2개
- settings 미등록 Integration Source: 7개

사용자가 보는 물리 단위는 지나치게 많고 공개 Capability와 내부 Provider가 뒤섞여 있다.

## 3. 구조 결론

### 유지할 개념

- Data
- Messaging
- Integration
- File
- Notification
- Security
- Platform Operations

### 내부 Leaf로 숨길 대상

- JDBC/MyBatis
- Caffeine/Valkey
- Kafka/RabbitMQ/JMS/IBM MQ
- OTLP exporter
- SFTP provider
- Fixed-length/ISO8583 codec
- Email/SMS provider
- Session JDBC/Resource Server/Service Identity
- Quartz provider가 실제 가치를 충족하는 경우

### 제거 대상

- AOP Service Access
- Validation
- Resilience
- Feature Flag
- 현재 Security Aggregate
- 현재 Cache Aggregate
- 미등록 FTPS/gRPC/S3/Realtime/SMB/SOAP/Webhook 모듈
- 실제 JDBC Cluster 가치를 제공하지 못하는 Quartz
- exporter 조립 외 가치가 없는 OTLP 공개 Starter

## 4. Messaging의 존재 이유

Messaging Starter가 의미 있으려면 개발자는 `KafkaTemplate`, `RabbitTemplate`, `JmsTemplate`을 직접 사용하지 않고 CPF API만 사용해야 한다.

필수 CPF 가치:

- 동일 Publish/Consume API
- Named Binding과 Default fail-closed
- 표준 transaction/idempotency/header propagation
- Outbox/Inbox와 exactly-once가 아닌 명확한 delivery semantics
- ACK/NACK/timeout을 SUCCESS/FAILED/UNKNOWN으로 분리
- Reconcile 전에 무조건 재시도하지 않음
- Provider별 운영 상태, DLQ, replay, 승인
- Customer Provider Plugin SPI
- Generator가 Provider를 lock하고 업무 Source는 변경하지 않음

현재 코드는 이 방향의 일부 요소를 갖지만 전체 연결과 Runtime Evidence가 없어 부분 구현이다.

## 5. 공개 Surface 제안

일반 개발자는 개별 Leaf 이름이 아니라 다음만 선택한다.

- Profile: minimal-domain, web-api, secure-api, browser-bff, event-service, batch-service
- Capability: persistence, cache, messaging, file-processing, file-exchange, transaction-integration, notification, observability, secret, platform-operations

Provider는 `messaging=kafka`, `cache=valkey`, `persistence=mybatis` 같은 binding으로 선택한다. Generator가 내부 Artifact와 version lock을 결정한다.

## 6. 세분화 장단점 판정

Leaf 분리는 Provider 독립 배포와 optional dependency에 유리하다. 그러나 현재는 Leaf가 사용자 공개 목록에 그대로 노출되고 Aggregate가 상호 배타 Provider를 다시 묶어 장점이 상쇄됐다.

따라서 물리 Artifact 분리는 내부적으로 유지할 수 있으나 공개 선택면은 축소해야 한다. “폴더가 많다”보다 “개발자가 무엇을 선택해야 하는지 알 수 없다”가 현재 핵심 결함이다.

## 7. 상세 행별 판정

`cpf-docs/quality/CPF_QA39_STARTER_VALUE_CATALOG.csv`를 정본으로 사용한다.

## 8. Final rule

유지 Group은 업무 개발자가 사용하는 편의 Public API와 고객 Provider SPI를 제공해야 한다. OSS API를 그대로 한 번 호출하는 Wrapper는 유지하지 않는다. QA 개발요건과 자체요건이 충돌하면 QA 개발요건이 우선한다.

## 8. 개발 구현 보고·자체 리뷰 작성란

개발 GPT는 별도 Implementation Report와 Self Review 파일을 만들지 않는다.
이 절을 직접 갱신하여 실제 결과를 누적한다.

개발 GPT는 이 Template을 복사해 아래 두 파일을 실제 내용으로 작성한다.

- `CPF_QA39_DEVELOPER_IMPLEMENTATION_REPORT.md`
- `CPF_QA39_DEVELOPER_SELF_REVIEW.md`

## A. Implementation Report 필수 항목

### 1. Baseline
- Repository/Branch
- 시작 exact SHA / 종료 exact SHA
- Working Tree 상태
- 작업 시간과 환경(JDK/Gradle/Node/DB/Provider)

### 2. Requirement 결과
Requirement ID별로 `development_status`와 `verification_status`를 분리한다. 변경 파일, 핵심 Class/API/SQL/Test/Consumer, Acceptance, Evidence를 한 행에 연결한다.

### 3. Architecture 결정
- Owner Module/Package
- Public API / Extension SPI / Internal Adapter
- 의존성 방향과 Consumer
- 6 Profile/7 Group 및 Provider resolved lock
- Generated Domain 표준 상속, 최소 기본 구성, 선택 Capability만 조립
- 이용 Domain/Module 선언형 Starter 예외와 Platform Source 무수정 원칙
- Convention/Architecture Build Gate와 Runtime Gate의 승인·범위·만료·Version·Hash fail-closed 검증
- 편의성·확장성 가치가 OSS 직접 사용보다 나은 근거

### 4. 변경 Manifest
Root-relative path, 변경 목적, Requirement, 영향도, 신규/수정/삭제/이동, 주요 line range를 기록한다.

### 5. 삭제 결과
- exact 삭제 path
- 삭제 전 Consumer/대체 위치
- Core API·Config·Test·BOM·Catalog·Generator·문서·Evidence 정리
- 잔여참조 검색 명령과 0건 결과
- 빈 폴더·Dead Code·Stale Evidence 결과
- 실행한 PowerShell 한 줄 명령과 출력

### 6. 실행 명령과 실제 결과
명령마다 Profile, 환경, 시작/종료 시각, Exit Code, 실제 결과, log/evidence 위치를 기록한다. 실행하지 않은 검증은 `미검증`으로 적는다.

### 7. 실패·미완료·재확인
숨기지 않고 원인, 영향, 재현 명령, 다음 조치를 기록한다.

### 8. Evidence
exact SHA, 파일 Hash, 민감정보 제거, 현재 Commit 유효성을 기록한다.

## B. Developer Self Review 필수 항목

개발 보고를 그대로 반복하지 않는다. 구현 종료 후 독립적으로 다음을 다시 확인한다.

- QA 최종요건과 자체요건 충돌 여부 및 우선순위 준수
- 예상 변경과 실제 변경의 차이
- 유지/삭제/내부화 판정 준수
- Public API의 OSS 타입 누출과 얇은 Wrapper 잔존
- Provider 교체와 고객 SPI 확장성
- Source/API/SQL/Test/Config/Frontend/Script/문서/Evidence 정합성
- Consumer 없는 추상화·Dead Code·빈 폴더·Stale Evidence
- 회귀 위험과 보호한 기존 성공 기능
- 직접 실행하지 않은 검증
- QA가 확인할 핵심 파일·라인·명령·기대 결과
- 최소 Domain/선택 Domain 생성 결과와 미선택 dependency·bean·config 0건
- 승인 예외 정상 시나리오와 미승인·만료·범위·Version·Hash Negative 시나리오

## C. Codex 검수 최적화 산출물

`REVIEW_INDEX.md`, `CHANGE_MANIFEST.csv`, `REQUIREMENT_STATUS.csv`, `TEST_AND_EVIDENCE.md`, `OPEN_ISSUES.md`, `PACKAGE_MANIFEST.json`을 Developer Report/Self Review와 일치시킨다. QA가 같은 전체 탐색을 반복하지 않도록 범위·제외·근거·남은 의문을 명확히 한다.

### 자체 리뷰 필수 항목

개발 GPT는 구현 완료 후 개발 리포트와 별개로 Source, dependency graph, Public API/SPI/Internal 경계, Consumer, 삭제 잔재, 회귀 위험, 실제 검증을 다시 확인한다. QA 최종요건과 자체요건이 충돌하면 QA 요건을 기준으로 판정한다. Self Review에는 계획 대비 실제 차이, 신규 발견 결함, 미완료/미검증, 삭제 후보와 빈 폴더, QA가 확인할 파일·라인·명령을 기록한다.


추가 필수 점검: Generated Domain의 필수 표준 자동 상속과 최소 기본 구성, 미선택 Capability 비전이, CPF 표준 Starter 우회 차단, 이용 Domain 선언형 예외, Platform Source 무수정, 승인·범위·만료·Version·Hash fail-closed, resolved lock·감사·Rollback Evidence를 독립 확인한다.

## 9. QA 독립 검수·Codex 실행 계획

## Baseline

`9a9634eb1f28071d47c205cc35227b6d013a4536`

## Fast review order

1. `CPF_QA39_FINAL_DEVELOPMENT_REQUIREMENTS.md`의 우선순위와 최종 유지/삭제 결정
2. `CPF_QA39_DEVELOPER_IMPLEMENTATION_REPORT.md`와 `CPF_QA39_DEVELOPER_SELF_REVIEW.md` 존재·완결성
3. `CPF_QA39_DELETE_WORK_ITEMS.csv`와 실제 삭제/잔여참조 결과
4. `CPF_QA39_STARTER_VALUE_CATALOG.csv`와 6 Profile+7 Group Public Catalog
5. Generated Domain 최소 기본 구성·표준 자동 상속·이용 Domain 선언형 예외와 fail-closed Gate
6. `CHANGE_MANIFEST.csv`에서 변경 파일·Requirement·영향도
7. `REQUIREMENT_STATUS.csv`와 actual Evidence
8. `TEST_AND_EVIDENCE.md`의 명령·환경·시간·Exit·미실행
9. 핵심 Source/Consumer/Dependency/Runtime 독립 표본검증
10. `OPEN_ISSUES.md`와 Package Hash

Developer Report는 QA 승인 근거 자체가 아니라 반복 탐색을 줄이는 검수 인덱스다. 보고와 Source/Evidence가 다르면 Source/Evidence를 기준으로 실패 처리한다.

## 정본 및 정리 확인

1. Repository Root에 QA 산출물이 추가되지 않았는지 확인한다.
2. Current Request·자체요건·Handover·Continuity의 중복 정본이 남지 않았는지 확인한다.
3. QA 문서 정리와 제품 Source 삭제가 서로 다른 Manifest로 관리되는지 확인한다.

### 독립 검증 요청

## Baseline

`9a9634eb1f28071d47c205cc35227b6d013a4536`

## Fast review order

1. `CPF_QA39_FINAL_DEVELOPMENT_REQUIREMENTS.md`의 우선순위와 최종 유지/삭제 결정
2. `CPF_QA39_DEVELOPER_IMPLEMENTATION_REPORT.md`와 `CPF_QA39_DEVELOPER_SELF_REVIEW.md` 존재·완결성
3. `CPF_QA39_DELETE_WORK_ITEMS.csv`와 실제 삭제/잔여참조 결과
4. `CPF_QA39_STARTER_VALUE_CATALOG.csv`와 6 Profile+7 Group Public Catalog
5. `CHANGE_MANIFEST.csv`에서 변경 파일·Requirement·영향도
6. `REQUIREMENT_STATUS.csv`와 actual Evidence
7. `TEST_AND_EVIDENCE.md`의 명령·환경·시간·Exit·미실행
8. 핵심 Source/Consumer/Dependency/Runtime 독립 표본검증
9. `OPEN_ISSUES.md`와 Package Hash

Developer Report는 QA 승인 근거 자체가 아니라 반복 탐색을 줄이는 검수 인덱스다. 보고와 Source/Evidence가 다르면 Source/Evidence를 기준으로 실패 처리한다.

## Independent validation result

Source를 직접 확인하고 Developer Report를 그대로 승인하지 않는다.

## Added mandatory validation

- QA39-043: Generated Domain mandatory-standard inheritance, minimal default, selected capabilities only
- QA39-044: domain/module declarative Starter exception without Platform source changes; approval/scope/expiry/version/hash fail-closed gates
- Execute SC-032 through SC-037 and do not accept configuration/schema existence without negative build/runtime evidence.

### 검수 순서

## Exact baseline

`9a9634eb1f28071d47c205cc35227b6d013a4536`

## Required first documents

1. Final QA Development Requirements
2. Developer Implementation Report
3. Developer Self Review
4. Final Requirement/Scenario Matrix
5. Delete Work Items/Final Delete Paths
6. Change Manifest and Evidence

## Critical checks

- QA 요건이 자체요건보다 우선하는가
- 미등록 7개 모듈을 등록·제품화하지 않고 제거했는가
- AOP/Validation/Resilience/Feature Flag와 가치 없는 Core Wrapper가 제거됐는가
- 6 Profile+7 Group 외 공개 Starter 선택면이 남지 않았는가
- 유지 Group이 편의 API·고객 SPI·운영 신뢰성을 실제 Consumer로 제공하는가
- Aggregate/Profile/Quartz old path와 artifact ID 잔재가 0건인가
- Generated Domain이 필수 표준을 자동 상속하되 최소 기본 구성과 선택 기능만 포함하는가
- CPF 표준 Starter 우회가 차단되고 이용 Domain 선언형 예외만 승인·범위·만료·Version·Hash 검증을 거쳐 허용되는가
- 미승인·만료·drift 예외가 Build/Runtime에서 fail-closed하는가
- Developer Report 명령/결과가 Evidence와 일치하는가
- 미실행 검증을 PASS로 기록하지 않았는가

### Test와 Evidence

## Executed in this QA revision

- R5 requirement amendment: QA39-043/044 and SC-032~037 added for Generated Domain standard inheritance, minimal default, and domain-declared Starter exceptions
- R4 Overlay files were amended offline; current origin/master and product Source were not freshly reviewed in this revision

- latest master lookup: `9a9634eb1f28071d47c205cc35227b6d013a4536`
- compare `6a9890ef19ae54e6e3186ca011d5d7f984d49d9c` → `9a9634eb1f28071d47c205cc35227b6d013a4536`: 보호 대상 README/Deliverables/Guides/Docker 문서 변경만 확인; 본 Overlay에서는 제외
- 최신 `settings.gradle` 재확인: 기존 49 Starter/Profile project와 13 Profile 유지
- 최신 QA39 통합요청/자체요건 검토: 최종 삭제 결정과 충돌 확인
- latest commit combined status lookup: 독립 status 없음
- R5 문서/CSV/Manifest/Hash/ZIP integrity 검증

## Not executed

- Product Source 구현 변경
- 실제 삭제 명령
- JDK25 full build/test/publication
- Frontend, 3 Vendor DB, Provider Runtime/Fault, Browser, Supply-chain

미실행 항목은 `미검증`이며 PASS가 아니다. 개발 GPT는 실제 실행 명령과 결과를 Developer Report와 Evidence에 남긴다.

## 10. 현재 Open Issues

## Current P0

- RabbitMQ compile blocker와 Kafka hard-coded binding
- Notification/Messaging lease·UNKNOWN·Scheduler·Reconcile
- Batch global profile injection
- Security/Cache Aggregate와 13 Profile 과세분화
- 완전 제거 대상 Source/Core/API/BOM/Catalog/Generator 잔재
- 6 Profile+7 Group Generator/Consumer migration
- Generated Domain 최소 기본 구성·필수 표준 자동 상속·미선택 Capability 비전이
- 이용 Domain 선언형 Starter 예외와 Convention/Runtime fail-closed Gate 미구현
- 유지 Group 편의 Public API와 고객 Provider SPI 미완성
- Quartz 공개 Starter의 Batch Ownership 이관
- exact-SHA Java/Frontend/3 Vendor DB/Runtime/Fault/Browser/Supply-chain 미검증
- Developer Implementation Report/Self Review 미작성

파일 또는 Interface 존재만으로 Issue를 닫지 않는다.

## 11. Continuity·Decision·Handover

### Continuity

- Active package: QA39 Final Starter Decision and Runtime Closure R5
- Reviewed exact SHA: `9a9634eb1f28071d47c205cc35227b6d013a4536`
- Previous review: `6a9890ef19ae54e6e3186ca011d5d7f984d49d9c`
- Development status: `부분 구현`
- Verification status: `실패`
- Runtime/DB/Frontend/Supply-chain: `미검증`
- Active requirement count: 44
- Active scenario count: 37
- Current registered Starter/Profile projects: 49
- Current Profile count: 13
- Target public surface: 6 Profile + 7 Capability Group
- Generated Domain policy: mandatory standards inherited; minimal default; selected capabilities only
- Starter exception policy: domain/module declarative config; platform source unchanged; build/runtime fail-closed validation
- Complete-remove target count: 11
- Replace/internalize old-path target count: 12
- Actual deletion performed by this package: 0
- Next entry: `cpf-docs/work/codex/qa39/CODEX_START_HERE.md`

Do not inherit QA38 `156/156`. Do not follow the superseded instruction to productize the seven unregistered modules.

### Decision Log

1. 교체 비용과 기존 코드량은 Starter 가치 판정 근거가 아니다.
2. OSS 직접 사용 대비 편의성·확장성·표준화·운영성·독립성이 없으면 완전 제거한다.
3. QA 최종 개발요건이 자체 개발요건보다 우선하며 충돌 시 QA 요건을 적용한다.
4. 완전 제거: AOP Service Access, Validation, Resilience, Feature Flag, 미등록 FTPS/gRPC/S3/Realtime/SMB/SOAP/Webhook.
5. 관련 가치 없는 Core Wrapper/API도 함께 제거하고 core로 옮겨 숨기지 않는다.
6. 공개 선택면은 6 Profile+7 Capability Group이다.
7. 유지 Group은 Data, Messaging, Integration, File, Notification, Security, Platform Operations다.
8. OpenAPI는 web-api, Scheduler는 batch-service에 흡수한다.
9. Provider/Codec/Exporter는 내부 Leaf로 유지하며 Generator binding/resolved lock으로 선택한다.
10. 유지 Capability는 간단한 CPF Public API와 고객사 확장 SPI를 제공하고 OSS 타입을 업무 코드에 노출하지 않는다.
11. Security/Cache Aggregate와 Provider/기술별 Profile은 대체 후 삭제한다.
12. 삭제는 exact path Delete Work Items와 reference-zero 검증, 한 줄 명령으로 수행한다.
13. 개발 GPT는 Developer Implementation Report와 독립 Self Review를 남겨 QA 반복 탐색을 줄인다.
14. 보고서는 완료 증거가 아니라 독립 QA의 검수 진입점이며 실제 Evidence와 다르면 완료가 아니다.
15. Commit/Push/Branch/Tag/PR/Release는 사용자 승인 없이 수행하지 않는다.

Reviewed SHA: `9a9634eb1f28071d47c205cc35227b6d013a4536`

## QA39-R4 정리 결정

- QA 산출물의 Repository Root 배치를 금지한다.
- `CPF_QA39_SELF_DEVELOPED_REQUIREMENTS.csv`를 활성 자체요건 정본으로 사용한다.
- 이전 루트 README 및 중복·충돌 정본은 즉시 정리 Manifest에 기록한다.
- 제품 Source 제거 명령과 QA 문서 정리 명령을 분리한다.

16. Generated Domain은 CPF Core/Platform 필수 표준을 자동 상속하되 모든 Profile·Capability를 기본 포함하지 않는다.
17. 기본 생성은 최소 공통 기반이며 선택한 Profile·Capability·Provider binding만 Generator와 resolved lock에 조립한다.
18. CPF 표준 Starter가 존재하는 기능의 외부 OSS 직접 사용은 기본 차단한다.
19. 불가피한 예외는 Platform Source 수정이 아니라 이용 Domain/Module 선언형 설정으로 등록하고 Convention Plugin과 Runtime Gate가 승인·범위·만료·Version·Hash를 fail-closed로 검증한다.

### Handover

- Repository: `freeangelsun/202412_01_CPF`
- Branch: `master`
- Reviewed exact SHA: `9a9634eb1f28071d47c205cc35227b6d013a4536`
- Overall: `development_status=부분 구현`, `verification_status=실패`
- Runtime/DB/Frontend/Supply-chain: `미검증`
- Requirement: 44
- Scenario: 37
- Public target: 6 Profile+7 Group
- Complete removal: 11 modules
- Replace/internalize old paths: 12
- Actual source deletion in QA package: 0

## Start

1. `cpf-docs/work/current/CPF_QA39_FINAL_DEVELOPMENT_REQUIREMENTS.md`
2. `cpf-docs/quality/CPF_QA39_FINAL_REQUIREMENT_MATRIX.csv`
3. `cpf-docs/work/manifest/CPF_QA39_DELETE_WORK_ITEMS.csv`
4. `cpf-docs/work/review/CPF_QA39_DEVELOPER_REPORT_AND_SELF_REVIEW_TEMPLATE.md`
5. `cpf-docs/work/codex/qa39/CODEX_START_HERE.md`

## Critical rule

QA 개발요건과 자체 개발요건이 충돌하면 QA 개발요건이 우선한다. 이전 자체요건의 미등록 7개 모듈 제품화와 Resilience/Feature Flag 보호는 폐기한다.

## Final work delivery

개발 GPT는 구현 Source와 함께 Developer Implementation Report, Self Review, exact-SHA Evidence, Delete Manifest/cleanup log, Codex review package를 남긴다. QA가 반복 탐색하지 않도록 변경 파일·라인·명령·기대 결과를 명시한다.

## 정리 인수인계

루트 QA 파일과 중복 Current/Handover/Continuity 문서는 유지하지 않는다. 즉시 정리 대상은 `cpf-docs/work/manifest/CPF_QA39_REPOSITORY_CLEANUP_PATHS.txt`, 제품 재구성 후 삭제 대상은 `CPF_QA39_FINAL_DELETE_PATHS.txt`를 사용한다.

## Generated Domain / Starter Exception 인수인계

- Generated Domain은 CPF 필수 표준을 자동 상속하지만 기본값에 모든 Starter를 넣지 않는다.
- 최소 기본 구성과 명시적으로 선택된 Profile·Capability·binding만 생성한다.
- CPF 표준 Starter 우회는 기본 차단한다.
- 외부 OSS 예외는 이용 Domain/Module의 선언형 설정으로 열어두며 Platform Source 수정은 금지한다.
- 승인·범위·만료·Version·Config Hash·Rollback·감사·resolved lock을 Build/Runtime Gate가 검증한다.
- QA39-043, QA39-044, SC-032~SC-037을 항상 재검증한다.

## 12. Protected Owner Actions

이 Overlay와 다음 개발 작업은 아래 보호 경로를 수정하지 않는다.

- `cpf-docs/deliverables/**`
- `cpf-docs/guides/**`
- `cpf-docs/environment/docker/**`
- `cpf-tools/environment/docker-development-test/**`

유지/삭제/그룹화 결과가 보호 문서에 영향을 주더라도 직접 수정하지 않고 별도 Owner Action으로 기록한다. Developer Report에는 보호 경로 미변경과 영향 내용을 명시한다.

Reviewed SHA: `9a9634eb1f28071d47c205cc35227b6d013a4536`

## 13. Repository Hygiene와 삭제 원칙

- 새 정본 Overlay와 Hash 검증이 먼저다.
- 삭제는 `CPF_DELETE_MANIFEST.txt`의 exact path만 대상으로 한다.
- Wildcard, `git clean`, `reset`, `restore`, `stash`를 사용하지 않는다.
- 보호 경로가 Manifest에 포함되면 명령은 즉시 실패해야 한다.
- 삭제 후 Broken Link, Matrix 참조, Script 참조, 빈 폴더, Stale Evidence를 다시 확인한다.
- 제품 Source·SQL·Config·Test 삭제는 문서 정리 Manifest와 분리한다.
- Commit·Push는 사용자 별도 승인 없이는 수행하지 않는다.

## 14. 개발 GPT 즉시 스티어링

```text
CPF 개발·QA 문서 구조가 단일 활성 정본 방식으로 변경됐다.

앞으로 사람용 현재 작업 문서는
cpf-docs/work/CPF_CURRENT_WORK_REQUEST.md
한 파일만 갱신한다.

현재 요청, 개발요건, Architecture 결정, 작업 전 리뷰, 구현 결과,
개발자 자체 리뷰, QA 결과, Handover를 별도 파일이나 QA별 폴더로 만들지 말고
해당 정본의 지정 절에 누적한다.

도구가 읽는 Requirement, Scenario, Source Findings, Starter Catalog,
Delete/Package Manifest만 같은 cpf-docs/work 폴더에 별도 유지한다.

날짜·세션·QA 번호·R1/R2/FINAL별 복제 문서와 current/state/handover/
review/codex/manifest 하위 폴더를 새로 만들지 않는다.

다음 보호 경로는 절대 수정·이동·통합·삭제하지 않는다.
- cpf-docs/deliverables/**
- cpf-docs/guides/**
- cpf-docs/environment/docker/**
- cpf-tools/environment/docker-development-test/**

새 파일이 꼭 필요하면 기존 정본에 통합할 수 없는 이유, Owner, Consumer,
폐기 조건을 먼저 기록한다. 이유 없는 파일·폴더 증식은 Repository Hygiene
결함으로 판정한다.
```
