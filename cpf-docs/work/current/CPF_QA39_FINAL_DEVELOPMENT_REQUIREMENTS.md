# CPF QA39 최종 개발요건 — Starter 가치·그룹화·완전성 회복

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

## 4. 개발자 API와 확장성

업무 개발자가 호출하는 것은 CPF Public API다. 내부 OSS Adapter는 숨긴다. 대표 Consumer Source에 KafkaTemplate, RabbitTemplate, JmsTemplate, SftpClient, OpenTelemetrySdk 같은 Provider 타입이 나타나지 않아야 한다.

Public API는 업무 의도와 표준 Result/Tracking을 제공하고 Framework가 Header, 직렬화, 설정, 보안, 감사, 실패 분류, UNKNOWN, 복구, 운영 추적을 처리한다. Provider형 Group은 고객사가 CPF Internal Package 없이 구현 가능한 SPI와 동일 Contract Test를 제공한다.

## 5. 삭제 작업 관리

정본:

- `cpf-docs/work/manifest/CPF_QA39_DELETE_WORK_ITEMS.csv`
- `cpf-docs/work/manifest/CPF_QA39_FINAL_DELETE_PATHS.txt`
- `cpf-docs/work/manifest/CPF_QA39_CLEANUP_ONE_LINE.ps1.txt`

개발 GPT는 삭제 전 Consumer 대체, Core API 영향, settings/BOM/catalog/generator/publication, Test/Config/SQL/문서/Evidence, 빈 폴더를 전수 확인한다. 삭제 후 exact path 부재와 Repository 잔여참조 0건을 증명한다.

## 6. 개발 리포트와 자체 리뷰 — 필수 완료 조건

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

## 7. 완료 판정

- Requirement 42건과 Scenario 31건 모두 실제 Source·Consumer·Runtime·Evidence로 추적
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
