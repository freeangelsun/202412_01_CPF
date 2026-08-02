# CPF 프레임워크 안내 — 만들 수 있는 시스템과 선택 기준

> **주 독자**: 도입 검토자, 업무 기획자, 아키텍트, 개발·운영 책임자
> **완료 결과**: CPF로 만들 업무를 분류하고 제품 구성, 배포 형태, 역할별 매뉴얼과 운영 책임을 결정한다.
> **Source 기준**: `freeangelsun/202412_01_CPF`, `master`, `54bcc10887a83b933685bff462c0b0d7df824923`

![역할별 문서 지도](png/cpf-guide-map.png)

<!-- CPF-TOC:START -->
## 전체 목차

- [1. 이 문서의 역할](#1-이-문서의-역할)
- [2. 용어와 문서 표현](#2-용어와-문서-표현)
- [3. CPF로 만들 수 있는 업무 결과](#3-cpf로-만들-수-있는-업무-결과)
- [4. CPF와 업무 시스템의 책임 경계](#4-cpf와-업무-시스템의-책임-경계)
  - [4.1 업무 영역이 소유하는 것](#41-업무-영역이-소유하는-것)
  - [4.2 CPF 제품이 제공하는 것](#42-cpf-제품이-제공하는-것)
- [5. 제품 구성](#5-제품-구성)
  - [5.1 기술 중립 Core와 업무 공통](#51-기술-중립-core와-업무-공통)
  - [5.2 등록된 Starter와 Profile](#52-등록된-starter와-profile)
  - [5.3 선택과 버전 고정 원칙](#53-선택과-버전-고정-원칙)
- [6. 배포 형태 선택](#6-배포-형태-선택)
- [7. 공통 실행 상태와 운영 행동](#7-공통-실행-상태와-운영-행동)
- [8. 역할별 문서 지도](#8-역할별-문서-지도)
- [9. 도입 절차](#9-도입-절차)
- [10. 다음 문서](#10-다음-문서)
- [11. 제품 범위와 비범위](#11-제품-범위와-비범위)
- [12. Architecture와 의존 방향](#12-architecture와-의존-방향)
- [13. Module Ownership 지도](#13-module-ownership-지도)
- [14. DB Lifecycle·Generator·문서 지도 연결](#14-db-lifecyclegenerator문서-지도-연결)
  - [DB Lifecycle](#db-lifecycle)
  - [Generator](#generator)
  - [문서 지도](#문서-지도)
- [15. 기능 선택을 위한 전체 Capability 지도](#15-기능-선택을-위한-전체-capability-지도)
- [16. 도입 Architecture 결정 절차](#16-도입-architecture-결정-절차)
  - [16.1 업무 경계 확정](#161-업무-경계-확정)
  - [16.2 배포 형태 결정표](#162-배포-형태-결정표)
  - [16.3 다중 인스턴스 결정](#163-다중-인스턴스-결정)
- [17. 온라인 업무 도입 시나리오](#17-온라인-업무-도입-시나리오)
  - [17.1 지급 신청 예](#171-지급-신청-예)
  - [17.2 완료 산출물](#172-완료-산출물)
- [18. 비동기 메시지 도입 시나리오](#18-비동기-메시지-도입-시나리오)
- [19. 파일·외부 연계 도입 시나리오](#19-파일외부-연계-도입-시나리오)
  - [19.1 파일](#191-파일)
  - [19.2 외부 REST·TCP](#192-외부-resttcp)
- [20. 운영·보안·감사 적용 원칙](#20-운영보안감사-적용-원칙)
- [21. 도입 단계별 완료 기준](#21-도입-단계별-완료-기준)
  - [21.1 계획](#211-계획)
  - [21.2 개발](#212-개발)
  - [21.3 운영 인계](#213-운영-인계)
- [22. 역할별 첫 주 작업표](#22-역할별-첫-주-작업표)
- [23. 도입 검토 질문 30선](#23-도입-검토-질문-30선)
- [24. 최신 Capability Profile 선택표](#24-최신-capability-profile-선택표)
- [25. 도입 결정서 완성 예시](#25-도입-결정서-완성-예시)

<!-- CPF-TOC:END -->

## 1. 이 문서의 역할

이 문서는 내부 Class나 Module 목록을 설명하는 참조서가 아니다. 도입 조직이 다음 질문에 답하도록 돕는 선택 안내서다.

1. 만들려는 업무 결과는 무엇인가?
2. 온라인, 비동기 메시지, 파일·외부연계, 배치 중 무엇이 필요한가?
3. ADM, BZA, Gateway를 어느 범위까지 이용하는가?
4. 동일 JVM, 모듈형 단일 애플리케이션, 분리 서비스 중 어떤 배포 형태를 선택하는가?
5. 업무 개발팀과 플랫폼 운영팀이 각각 소유할 것은 무엇인가?
6. 제공·실행 검증 기능을 계획에 어떻게 반영하는가?

## 2. 용어와 문서 표현

| 표현 | 이 문서에서의 의미 |
|---|---|
| 이용 조직 | CPF를 도입하거나 평가해 업무 시스템을 만드는 조직 |
| 업무 개발자 | 조직 고유의 업무 규칙·API·상태·데이터를 구현하는 개발자 |
| 연동 개발자 | 메시지·파일·외부기관·ADM·BZA·Gateway 연결을 구현하는 개발자 |
| 운영 담당자 | 상태 조회, 조치, 승인, 배포, 관측, 백업과 장애 대응을 수행하는 역할 |
| Owner | 상태·데이터·명령·장애 대응의 최종 책임 모듈 |
| Consumer | Owner가 공개한 API·SPI·Artifact를 실제 사용하는 모듈 |

README와 상위 안내에서는 특정 제품 브랜드보다 기능을 우선한다. 실제 의존성, Property, Port와 장애 절차를 설명해야 할 때만 구현 기술명을 사용한다.

## 3. CPF로 만들 수 있는 업무 결과

| 업무 결과 | CPF에서 선택할 기능 | 주 담당 문서 |
|---|---|---|
| 요청 시점에 조회·등록·변경 | 온라인 API, 응용 계층, 업무 규칙, 영속 계층 | [01 CPF 개발자 매뉴얼](01_CPF_개발자매뉴얼.md) |
| 여러 시스템에 상태 변경을 전달 | 비동기 메시지, Outbox·Inbox, 멱등성, 재처리 | [01 CPF 개발자 매뉴얼](01_CPF_개발자매뉴얼.md) |
| 파일 송수신과 내용 검증 | 파일·첨부·SFTP·Checksum·대사 | [01 CPF 개발자 매뉴얼](01_CPF_개발자매뉴얼.md) |
| 정기·대량·분할 처리 | Spring Batch, Scheduler, Worker, Center-Cut | [02 CPF 배치 개발 매뉴얼](02_CPF_배치개발매뉴얼.md) |
| 운영자가 상태를 보고 조치 | ADM 조회·조치·승인·감사 | [03 CPF ADM 매뉴얼](03_CPF_ADM매뉴얼.md) |
| 실행 기능 선택·의존성 검증 | CPF Starters | [90 CPF Starters 매뉴얼](90_CPF_Starters_매뉴얼.md) |
| 생성·빌드·DB·실행·검증 도구 | CPF Tools | [91 CPF Tools 매뉴얼](91_CPF_Tools_매뉴얼.md) |
| 공통 API 진입점 | Gateway 인증·라우팅·제한·게시 | [92 CPF Gateway 매뉴얼](92_CPF_Gateway_매뉴얼.md) |
| 조직·사용자·권한·결재 공유 | BZA | [95 CPF BZA 매뉴얼](95_CPF_BZA_매뉴얼.md) |
| 설치·DB·배포·관측·백업 | 플랫폼 운영 | [05 CPF 플랫폼 운영 매뉴얼](05_CPF_플랫폼운영매뉴얼.md) |

## 4. CPF와 업무 시스템의 책임 경계

### 4.1 업무 영역이 소유하는 것

- 업무 엔터티, 상태와 전이 규칙
- 금액·건수·한도·우선순위 계산
- 승인·취소·보상·대사 기준
- 업무 테이블과 보존 정책
- 업무 API와 화면의 의미
- 외부기관 계약과 업무 오류 판정
- 성공·실패·부분 성공의 최종 업무 의미

### 4.2 CPF 제품이 제공하는 것

- 요청·거래·작업 식별자와 문맥 전달
- 멱등성, 낙관적 버전, Deadline, 결과 미확정 계약
- 메시지·파일·원격 호출의 기술 중립 API·SPI
- 배치 실행, Metadata, Checkpoint, Worker와 운영 계약
- 권한·데이터 범위·가림·사유·승인·감사 계약
- ADM·BZA·Gateway 제품
- DB Vendor Pack, Build, Generator, 배포와 검증 도구

ADM·BZA·Gateway는 업무 원장을 직접 소유하지 않는다. 업무 모듈이 공개한 계약을 호출하고 결과를 표시하거나 조치를 전달한다.

## 5. 제품 구성

### 5.1 기술 중립 Core와 업무 공통

| 영역 | 제공 책임 | 선택·운영 기준 |
|---|---|---|
| `cpf-core` | Topology-independent API·SPI, 식별자, 오류, Deadline, Idempotency, 보안·감사 문맥 | Provider SDK·업무 정책을 포함하지 않는다. |
| `cpf-common` | Code·Calendar·Message·Template 등 업무 공통 | 선택 Runtime은 Starter로 공급한다. |
| `cpf-starters/` | 38개 Leaf·Aggregate 실행 모듈 | 필요한 Profile/Leaf만 선택한다. |
| Capability Profile | 13개 Use Case를 `resolvedStarters`로 해석 | Profile Version `2026.08.02`와 Provider Binding을 Manifest에 고정한다. |
| Aggregate Starter | 승인 Leaf 조합의 전이 의존성 | 고유 Bean·AutoConfiguration·업무 정책을 두지 않는다. |


### 5.2 등록된 Starter와 Profile

Starter 전체 목록과 적용 절차는 [90 CPF Starters 매뉴얼](90_CPF_Starters_매뉴얼.md)을 따른다. 대표 기능군은 다음과 같다.

```text
Base·Persistence·OpenAPI·Security·Service Identity
Messaging Reliability·Kafka·RabbitMQ·JMS·IBM MQ
Cache Caffeine·Valkey, Observability·OTLP, HTTP Client
Fixed Length·SFTP·Archive·Attachment·TCP·ISO8583
Notification·Email·SMS SPI, Quartz, Resilience, Feature Flag, Secret
```

13개 Profile은 최소 Boot, 일반 Web API, 보호 API, Browser BFF, MyBatis DB, Kafka/RabbitMQ/JMS·IBM MQ Event, TCP 전문, 관측·복원력, 알림, 일정, SFTP 연계를 제공한다.

### 5.3 선택과 버전 고정 원칙

- Generator는 `CapabilityProfile`, `ProviderBindings`, `resolvedStarters`, Profile Version과 Starter Version Lock을 Domain Manifest에 기록한다.
- 상호 배타 Provider는 하나의 Default Binding만 허용한다.
- 선택하지 않은 Starter의 JAR·Bean·SQL·Secret·Container 요구가 Runtime에 유입되지 않아야 한다.
- Provider 교체는 In-flight, Outbox/Inbox, Offset/Queue, Attempt, Secret, Schema와 LKG를 포함해 수행한다.
- BOM은 Version만 정렬하며 Profile 또는 Leaf 선언을 대신하지 않는다.


## 6. 배포 형태 선택

| 형태 | 선택 조건 | 확인할 위험 |
|---|---|---|
| 동일 JVM | 호출 지연을 줄이고 배포 단위를 함께 운영 | 모듈 간 Internal 직접 참조, Transaction 경계 확대 |
| 모듈형 단일 애플리케이션(Modular Monolith) | 업무 영역을 분리하되 하나의 Artifact로 운영 | DB 소유권 혼합, 숨은 순환 의존 |
| 분리 WAS | 웹·배치·운영 제품을 서로 다른 Process로 운영 | 인증, 원격 오류, Timeout, 배포 순서 |
| 마이크로서비스 | 업무별 독립 배포와 확장 필요 | 결과 미확정, 중복, 메시지 순서, 대사 |
| 다중 인스턴스 | 가용성·처리량 확장 | Lease·Claim·Fencing·버전 충돌 |

배포 형태가 달라도 Public 계약, 식별자, 권한 문맥, 멱등성, Deadline과 결과 판정 의미는 유지해야 한다.

## 7. 공통 실행 상태와 운영 행동

| 상태 | 의미 | 운영 행동 |
|---|---|---|
| `REQUESTED` | 요청 접수 | 같은 멱등성 키로 신규 요청을 만들지 않고 기존 기록 조회 |
| `IN_PROGRESS` | Owner가 처리 중 | Deadline과 Lease 소유자 확인 |
| `WAITING_APPROVAL` | 승인 대기 | 요청자·승인자 분리, 정책 버전, 만료 확인 |
| `WAITING_EXTERNAL` | 외부 ACK·대상 적용 대기 | Outbox, 상대 상태, Correlation 확인 |
| `UNKNOWN_RESULT` | 성공 여부 미확정 | 재실행보다 원장·대상·상대 결과 대사 우선 |
| `PARTIAL_SUCCESS` | 일부 대상 성공 | 성공 대상 유지, 실패 대상만 재처리 |
| `FAILED_RETRYABLE` | 원인 제거 후 재시도 가능 | 같은 Operation에서 제한된 재시도 |
| `FAILED_FINAL` | 정책상 종료 | 사유·감사·보상 필요 여부 확인 |
| `SUCCEEDED` | 결과 확정 | 원장·감사·지표·추적을 함께 확인 |

상태 이름은 실제 Owner 계약과 Source에 있는 값만 사용한다. 위 표는 공통 판정 모델이며 각 제품의 실제 상태는 해당 매뉴얼과 Source에서 다시 확인한다.

## 8. 역할별 문서 지도

| 역할 | 먼저 읽을 문서 | 이어서 수행할 일 |
|---|---|---|
| 도입 검토자·아키텍트 | 00 | 범위, 비범위, 제품·배포 결정 |
| 업무 개발자 | 01 | 업무 API·상태·DB·연계 구현 |
| 배치 개발자·운영자 | 02 | Job·Step·Worker·Scheduler·대사 |
| ADM 연동 개발자·조회자·운영자·승인자 | 03 | 업무 계약 연결과 권한별 사용 |
| 인프라·DBA·배포·관측 담당자 | 05 | 설치·설정·배포·백업·장애 대응 |
| 업무 개발자·아키텍트·빌드 담당자 | 90 | CPF Starters 선택·의존성·설정·시험 |
| 개발자·운영자·검수자 | 91 | CPF Tools 생성·빌드·DB·실행·검증 |
| API 개발자·보안·Gateway 운영자 | 92 | Route·정책·게시·적용 상태 |
| 조직·권한·결재 담당자 | 95 | BZA 초기화·연계·운영 |

## 9. 도입 절차

1. 업무 결과와 데이터 Owner를 정한다.
2. 온라인·메시지·파일·배치 기능을 분류한다.
3. ADM·BZA·Gateway 선택 여부를 결정한다.
4. 동일 JVM·분리 Process·다중 인스턴스 구성을 정한다.
5. 필요한 Capability Profile과 Provider Binding을 결정하고 Domain Manifest에 고정한다.
6. Generator Dry Run으로 경로·포트·DB Vendor 충돌을 검사한다.
7. 업무 API·DB·Test를 구현한다.
8. 정상·오류·중복·동시성·Timeout·응답 유실·부분 실패를 시험한다.
9. ADM·로그·지표·추적·감사에서 같은 식별자를 확인한다.
10. 배포·Rollback·백업·인계 절차를 운영 담당자에게 전달한다.

## 10. 다음 문서

- 업무 기능 개발: [01 CPF 개발자 매뉴얼](01_CPF_개발자매뉴얼.md)
- 배치 개발·운영: [02 CPF 배치 개발 매뉴얼](02_CPF_배치개발매뉴얼.md)
- ADM 연동·이용: [03 CPF ADM 매뉴얼](03_CPF_ADM매뉴얼.md)
- 플랫폼 설치·운영: [05 CPF 플랫폼 운영 매뉴얼](05_CPF_플랫폼운영매뉴얼.md)
- CPF Starters: [90 CPF Starters 매뉴얼](90_CPF_Starters_매뉴얼.md)
- CPF Tools: [91 CPF Tools 매뉴얼](91_CPF_Tools_매뉴얼.md)
- CPF Gateway: [92 CPF Gateway 매뉴얼](92_CPF_Gateway_매뉴얼.md)
- CPF BZA: [95 CPF BZA 매뉴얼](95_CPF_BZA_매뉴얼.md)

## 11. 제품 범위와 비범위

| CPF 제품 범위 | 업무 시스템이 결정할 범위 |
|---|---|
| 공개 API·SPI·오류·식별자 계약 | 업무 엔터티·상태·금액·건수 규칙 |
| 동일 JVM·원격 호출의 계약 동등성 | 서비스 분리·배포 단위·SLA |
| 멱등성·Version·Attempt·대사 구조 | 중복의 업무 의미·보상 정책 |
| 정기·대량 처리 실행 계약 | Job Parameter·Chunk·Partition·합계 기준 |
| 권한·Data Scope·Masking·Audit 계약 | 어떤 업무 조치를 누구에게 허용할지 |
| ADM·BZA·Gateway 선택 제품 | 제품 사용 여부와 조직별 정책 |
| DB Vendor Pack·Migration·검증 도구 | 계정·용량·보존·Backup 정책 |
| Generator·Build·검증 도구 | 생성된 업무 Source의 최종 소유권 |

CPF는 다음을 대신 결정하지 않는다.

- 조직 고유의 업무 정책과 법규 해석
- 업무 데이터의 정합성·보존·폐기 기준
- 외부기관의 실제 처리 결과
- 운영 조직의 승인·권한 분리
- 성능·RPO·RTO·보안 목표의 조직별 수치
- Source에 없는 API·Property·화면·상태

## 12. Architecture와 의존 방향

```text
Channel·Scheduler·External System
                ↓
API·Message·Batch Entry Point
                ↓
Application Use Case
                ↓
Business Domain + CPF Public API·SPI
                ↓
Persistence·Message·File·Remote Adapter
                ↓
DB·Broker·File Store·External Provider
```

의존 방향 원칙:

1. 업무 Domain은 Controller·DB Driver·Broker SDK를 직접 알지 않는다.
2. Application은 업무 Use Case와 Transaction 경계를 소유한다.
3. Adapter는 CPF Public SPI 또는 Owner Port를 구현한다.
4. ADM·BZA·Gateway는 업무 Owner의 Public 계약을 사용한다.
5. 동일 JVM 최적화가 Public 계약을 우회하지 않는다.
6. Provider 교체가 업무 상태와 오류 의미를 바꾸지 않는다.

## 13. Module Ownership 지도

| Module·제품군 | 소유 범위 | 실제 Consumer 예 | 비고 |
|---|---|---|---|
| `cpf-core` | 기술 중립 계약·식별자·오류·보안·감사 | 업무 Domain·Starter·제품 모듈 | 선택 Provider Runtime을 포함하지 않는다. |
| `cpf-common` | 업무 공통 Code·Calendar·Message·Template | 업무 Domain | 업무 공통 정책만 소유하고 선택 Runtime은 Starter가 소유한다. |
| `cpf-starters/*` | 선택 Runtime·Provider 자동 설정 | ADM·BZA·Gateway·Batch·업무 Runtime | 38개 Leaf·Aggregate 실행 모듈 등록 |
| `cpf-reference` | EDU·Reference Consumer | 개발자·검수자 | 기능 Variant와 DB Vendor 선택 |
| `cpf-member` | Generator Golden Reference Domain | Generator 검증 | 직접 의존성 재검토 필요 |
| `cpf-batch/*` | Batch 계약·실행·제어·Scheduler·Worker | Batch Job·ADM | Spring Batch Primary Engine |
| `cpf-admin` | ADM Backend·Frontend | 운영 사용자·Owner Service | 업무 원장 비소유 |
| `cpf-biz-admin` | BZA 조직·권한·결재 | 업무 Service·사용자 | 선택 제품 |
| `cpf-gateway` | Route·정책·게시·Attempt | 외부·내부 API | 선택 제품 |
| `cpf-tools/*` | Build·Generator·DB·Runtime·검증 | 개발·운영·검수 | 도구군 |

Owner가 불분명한 상태에서 공통 모듈에 기능을 추가하지 않는다. 실제 Consumer가 없는 추상화는 제품 구성에 포함하지 않는다.

## 14. DB Lifecycle·Generator·문서 지도 연결

### DB Lifecycle

모든 DB 사용 기능은 MariaDB·PostgreSQL·Oracle 중 지원 Vendor를 명시하고 다음을 확인한다.

```text
Fresh Install → Verify → Runtime Query → Upgrade
→ Rollback/Forward Recovery → Reapply → Drift → Backup/Restore
```

### Generator

Generator는 업무 영역 생성의 시작점이지 완료 판정 도구가 아니다. Dry Run, 충돌 검사, 생성 Manifest, Build, DB, OpenAPI, Test, 운영 인계를 함께 확인한다.

### 문서 지도

- 제품 선택·책임 경계: 본 문서
- 업무 Source·API·DB·연계: 01
- 정기·대량 처리: 02
- ADM 업무 연동과 권한별 이용: 03
- 설치·DB·배포·관측·DR: 05
- Starter 선택·설정·의존성: 90
- Generator·Build·DB·검증 도구: 91
- Gateway Route·보안·게시: 92
- BZA 조직·권한·결재: 95

## 15. 기능 선택을 위한 전체 Capability 지도

| 업무 결과 | 기본 선택 | 추가 선택 | 운영 확인 | 다른 방식을 선택할 경우 |
|---|---|---|---|---|
| 조건 검색·목록·상세 | Core·Common·업무 Domain | Cache Starter | ADM 거래·로그·권한 | 단순 정적 조회만 있고 상태·감사가 필요 없는 경우 |
| 신청·등록·상태 변경 | 업무 Domain·멱등성·감사 | BZA 결재, Notification | ADM Operation·Audit | 외부 업무 제품이 상태 정본인 경우 |
| 서비스 간 업무 흐름 | Local/Remote Facade | Messaging Starter, Saga | Trace·Outbox·Inbox·대사 | 하나의 DB 트랜잭션으로 끝나는 경우 |
| 파일·첨부·반출 | File/Attachment 계약 | SFTP·악성코드 검사·암호화 | ADM File Job·Download Audit | 외부 문서 제품을 정본으로 사용하는 경우 |
| 외부 REST·전문 | Integration SPI·Attempt Ledger | REST·TCP·ISO8583 Provider | 거래·시도·결과 미확정 | 단순 비업무 조회이며 재처리 책임이 없는 경우 |
| 정기·대량 처리 | CPF Batch | Scheduler·Worker·Center-Cut | ADM Batch Workbench | 소량 동기 처리로 시간·용량 기준을 충족하는 경우 |
| 조직·권한·결재 | CPF BZA | Attachment·Notification | BZA Menu·Audit | 기존 IAM·HR·결재 제품이 정본인 경우 |
| 공통 API 진입점 | CPF Gateway | Discovery·HMAC·Canary | ADM Gateway 화면 | 단일 서비스 직접 노출로 충분한 경우 |
| 운영 조회·통제 | CPF ADM | Remote Owner Adapter | Audit·Approval·Operation | 제품 외부 관측 도구만으로 상태 변경이 없는 경우 |

선택 결과는 “사용할 제품”, “선택하지 않을 제품”, “외부 정본”, “업무 Domain 책임” 네 칸으로 기록한다. 선택하지 않은 제품은 JAR·Bean·SQL·Secret·Container가 따라오지 않아야 한다.

## 16. 도입 Architecture 결정 절차

### 16.1 업무 경계 확정

1. 업무 상태를 소유하는 Domain을 정한다.
2. 조직·권한·결재를 BZA가 소유할지 외부 제품이 소유할지 정한다.
3. 운영 조치는 ADM이 Owner API를 호출하도록 하고 Owner DB 직접 변경을 금지한다.
4. Gateway는 외부 진입과 전달 책임만 가지며 업무 상태를 소유하지 않는다.
5. Batch는 실행 메타데이터를 소유하고 업무 결과는 업무 Domain이 소유한다.

### 16.2 배포 형태 결정표

| 질문 | 같은 애플리케이션 | 분리 서비스 |
|---|---|---|
| 독립 확장·장애 격리가 필요한가 | 아니오 | 예 |
| 배포 주기가 같은가 | 예 | 아니오 |
| 한 트랜잭션이 반드시 필요한가 | 가능 | 원격 계약과 보상 필요 |
| 네트워크 시간 예산을 감당하는가 | 해당 없음 | 필요 |
| 별도 인증 주체가 필요한가 | 선택 | 필수 |
| 운영·용량 Owner가 같은가 | 예 | 분리 가능 |

Local과 Remote는 요청·응답 DTO, Validation, 오류 분류, Permission, Timeout Budget, Idempotency, Audit 의미가 같아야 한다. 배포 형태가 바뀌어도 업무 Source를 다시 쓰지 않는다.

### 16.3 다중 인스턴스 결정

- 상태는 공유 DB·Broker·분산 저장소에 둔다.
- Lease에는 Owner, 만료시각, Fencing Token을 저장한다.
- 이전 Owner가 늦게 결과를 쓰면 Fencing Token으로 거부한다.
- 동일 요청은 Idempotency Key와 Request Hash로 중복을 구분한다.
- 응답 유실은 Operation·Attempt·상대 상태를 대사한 뒤 다음 행동을 결정한다.

## 17. 온라인 업무 도입 시나리오

### 17.1 지급 신청 예

1. `payment` 업무 Domain을 생성한다.
2. 신청 상태를 `DRAFT → SUBMITTED → APPROVED → EXECUTING → COMPLETED`로 정의한다.
3. 등록 Command에 `businessKey`, `idempotencyKey`, `expectedVersion`, `requestReason`을 포함한다.
4. Application Service가 권한과 상태 전이를 검증하고 한 트랜잭션에서 업무 원장과 Outbox를 기록한다.
5. 외부 기관 호출은 Attempt Ledger를 생성한 뒤 수행한다.
6. 응답 유실 시 지급을 다시 등록하지 않고 Operation과 기관 조회 결과를 대사한다.
7. ADM에서 거래·시도·Audit를 확인하고 BZA 승인과 연결한다.

### 17.2 완료 산출물

- 기능 설계 카드
- API/OpenAPI
- Domain 상태표
- Migration·Rollback·대사 SQL
- Unit·Contract·Integration·Fault Test
- Permission·Masking·Audit 정의
- 배포 Manifest와 운영 인계표

## 18. 비동기 메시지 도입 시나리오

1. 생산자는 업무 트랜잭션과 Outbox를 함께 Commit한다.
2. Publisher는 Outbox ID를 Message ID로 사용하고 Provider ACK를 기록한다.
3. 소비자는 Inbox/Dedup을 먼저 확인한 뒤 업무를 처리한다.
4. 재전달은 같은 Message ID를 사용하며 중복 부수 효과를 만들지 않는다.
5. Poison Message는 DLQ로 이동하고 원인 수정·승인·Replay 범위를 기록한다.
6. Provider 변경은 Envelope·Retry·Ordering·Audit 의미를 유지한다.

| 실패 지점 | 판정 | 다음 행동 |
|---|---|---|
| DB Commit 전 | 실패 | 같은 Idempotency Key로 재요청 가능 |
| DB Commit 후 Publish 전 | 처리 대기 | Outbox Publisher가 재개 |
| Broker ACK 유실 | 결과 미확정 | Outbox·Broker·Consumer Inbox 대사 |
| Consumer 처리 후 ACK 유실 | 중복 전달 가능 | Inbox 결과 반환, 부수 효과 재실행 금지 |
| DLQ 이동 | 실패 확정 | 원인 수정 후 승인된 Replay |

## 19. 파일·외부 연계 도입 시나리오

### 19.1 파일

- 업로드 임시 영역과 확정 보관 영역을 분리한다.
- 파일명, 크기, Content-Type, SHA-256, 검사 상태를 원장에 기록한다.
- 검사 완료 전 업무 연결·다운로드를 차단한다.
- 대용량 파일은 Streaming하고 메모리에 전체 적재하지 않는다.
- SFTP는 `.part` 수신 후 검사합 확인과 원자 이름 변경으로 완료를 판정한다.

### 19.2 외부 REST·TCP

- 연결·쓰기·읽기·전체 시간 제한을 분리한다.
- 요청 전 Attempt ID와 Request Hash를 기록한다.
- 대상이 업무를 처리했을 수 있는 Timeout은 자동 재전송하지 않는다.
- TCP는 Frame Length, Encoding, Correlation ID, Heartbeat, Half-open, TLS를 계약으로 고정한다.
- 전문 원문과 Credential은 로그에 남기지 않고 필드별 가림 정책을 적용한다.

## 20. 운영·보안·감사 적용 원칙

| 영역 | 설계 시 결정 | 실행 시 확인 | 장애 시 행동 |
|---|---|---|---|
| 인증 | 발급자·Audience·Service Identity | 주체·만료·인증서 | 차단·Rotation·Session 회수 |
| 권한 | Permission·Data Scope | 서버 판정과 화면 가시성 | 권한 재계산·Session 갱신 |
| 개인정보 | 필드 분류·Masking | 원문 조회 사유·승인 | 접근 차단·Audit 조사 |
| 위험 조치 | Reason·Approval·Expected Version | 대상·영향·현재 Version | 재조회·재승인·Rollback |
| 감사 | 수행자·조치·대상·전후·Trace | 누락·전달 지연 | 재전송·불변성 검증 |

## 21. 도입 단계별 완료 기준

### 21.1 계획

- 제품과 외부 정본의 책임 경계가 문서화됐다.
- Topology, DB Vendor, 메시지 Provider, 배포 단위가 결정됐다.
- 업무 상태·권한·대사·보존·복구 기준이 승인됐다.

### 21.2 개발

- 생성 결과와 Manifest가 일치한다.
- API·DB·메시지·파일·화면·Test가 같은 기능 ID를 사용한다.
- 정상·오류·동시성·응답 유실·부분 실패 시험이 있다.

### 21.3 운영 인계

- Artifact·Checksum·SBOM·Config·Secret 참조가 전달됐다.
- Health·Metric·Log·Trace·Audit와 경보 기준이 전달됐다.
- Retry·Restart·Reprocess·Reconcile·Compensation·Rollback 책임자가 정해졌다.
- ADM·BZA·Gateway 화면의 권한과 교대 인계 절차가 확인됐다.

## 22. 역할별 첫 주 작업표

| 역할 | 1일차 | 2~3일차 | 4~5일차 | 완료 결과 |
|---|---|---|---|---|
| 아키텍트 | Capability 선택 | Topology·Ownership | 실패·보안·운영 설계 | Architecture 결정서 |
| 업무 개발자 | Generator Dry Run | API·Domain·DB | Test·ADM·인계 | 실행 가능한 업무 Slice |
| 배치 개발자 | Job 설계 | Reader/Writer·Metadata | Restart·대사·ADM | 재시작 가능한 Job Pack |
| ADM 담당자 | Owner 계약 | Route·Permission | Browser·Fault | 역할별 운영 절차 |
| 플랫폼 운영자 | 설치 계획 | DB·Broker·Secret | 배포·Backup·DR | 운영 Runbook |
| BZA 담당자 | 조직·권한 모델 | 결재·위임 | 업무 연동·감사 | 실효 권한·결재 운영 |
| Gateway 담당자 | Target·Route | Security·Resilience | Publish·LKG | 게시·복구 가능한 Route Pack |

## 23. 도입 검토 질문 30선

1. 업무 상태의 단일 Owner는 어디인가?
2. Local과 Remote에서 같은 계약을 유지하는가?
3. 업무 Domain 간 DB 직접 접근이 없는가?
4. 중복 요청을 어떤 키와 해시로 판정하는가?
5. 응답 유실 후 실제 결과를 어디서 조회하는가?
6. 외부 부수 효과를 Attempt Ledger로 추적하는가?
7. 메시지 중복 소비를 Inbox로 차단하는가?
8. DLQ Replay의 승인과 범위가 정해졌는가?
9. 파일 검사 전 접근이 차단되는가?
10. 대용량 처리가 Streaming과 제한값을 사용하는가?
11. Batch의 재시작 위치와 업무 대사 기준이 있는가?
12. Lease와 Fencing으로 Stale Writer를 차단하는가?
13. Permission과 Data Scope가 서버에서 강제되는가?
14. Masking 해제에 사유와 감사가 있는가?
15. 위험 조치에 Expected Version이 있는가?
16. 승인과 실제 실행 결과가 같은 Operation에 연결되는가?
17. 설정 변경의 대상별 ACK/NACK를 확인하는가?
18. Artifact와 Config의 Version·Checksum을 확인하는가?
19. DB Migration이 Fresh·Upgrade·Rollback을 제공하는가?
20. 3개 DB Vendor의 의미가 같은가?
21. Health가 단순 프로세스 생존과 업무 준비 상태를 구분하는가?
22. Log·Metric·Trace·Audit가 같은 식별자를 사용하는가?
23. Secret 원문이 파일·로그·명령 이력에 남지 않는가?
24. Rolling·Blue-Green·Canary의 중단 조건이 있는가?
25. Backup과 Restore 검증이 같은 시점의 데이터를 보장하는가?
26. DR 전환 중 두 Site가 동시에 쓰지 않도록 차단하는가?
27. 선택하지 않은 Starter가 Runtime에 포함되지 않는가?
28. Generator 결과가 Profile·Starter·DB Pack을 고정하는가?
29. 문서만으로 신규 사용자가 업무를 끝낼 수 있는가?
30. 운영 인계 후 담당자가 Source 역분석 없이 정상화할 수 있는가?

## 24. 최신 Capability Profile 선택표

| Profile | 주요 결과 | 해석되는 실행 기능 | 대표 Consumer |
|---|---|---|---|
| `MINIMAL_BOOT_DOMAIN` | 최소 Boot Domain | Base | 작은 내부 업무 Domain |
| `DOMAIN_WEB_API` | Validation·OpenAPI·HTTP Client가 있는 Web API | Base·Validation·OpenAPI WebMVC·HTTP Client | 일반 온라인 업무 |
| `SECURE_RESOURCE_API` | Resource Server·Service Identity·관측 | Domain Web API·Resource Server·Service Identity·Observability | 외부/내부 보호 API |
| `BROWSER_BFF_SESSION` | Browser BFF와 JDBC Session | Security Aggregate·JDBC·Observability | ADM·BZA |
| `PERSISTENCE_MYBATIS` | JDBC·MyBatis Persistence | Persistence JDBC·MyBatis | DB 업무 Domain |
| `EVENT_KAFKA` | Kafka Event 처리 | Reliability JDBC·Kafka·Observability·Resilience | Event Consumer/Producer |
| `EVENT_RABBITMQ` | RabbitMQ Event 처리 | Reliability JDBC·RabbitMQ·Observability·Resilience | Queue 기반 연계 |
| `EVENT_JMS_IBM_MQ` | JMS/IBM MQ Event 처리 | Reliability JDBC·JMS·IBM MQ·Observability·Resilience | 기관 MQ 연계 |
| `INTEGRATION_TCP` | TCP·고정길이·ISO8583 전문 | TCP·Fixed Length·ISO8583·Observability·Resilience | 전문 연계 |
| `OBSERVABLE_RESILIENT_SERVICE` | 관측·OTLP·복원력 | Observability·OTLP·Resilience | 분리 서비스 |
| `NOTIFICATION_SERVICE` | 알림 Outbox·Email·SMS SPI | Notification·Email·SMS SPI·Observability·Resilience | 알림 서비스 |
| `SCHEDULED_SERVICE` | 일정 실행 | Quartz·Observability·Resilience | 정기 작업 |
| `SFTP_INTEGRATION` | SFTP·Archive | SFTP·File Archive·Observability·Resilience | 대외 파일 연계 |

Profile Version은 `2026.08.02`이며 Generator와 `cpf-capability-profiles.json`이 같은 `resolvedStarters`를 반환해야 한다. `ProviderBindings`는 `messaging=kafka|rabbitmq|jms|ibm-mq`, `cache=caffeine|valkey`, `notification=email|sms` 중 Profile이 허용한 값을 사용한다.

## 25. 도입 결정서 완성 예시

| 결정 영역 | 결정 예 | 근거 | 인계 대상 |
|---|---|---|---|
| 업무 Domain | `payment` / PAY | 지급 신청·승인·기관 전송 원장 소유 | 업무 개발팀 |
| Topology | 온라인 API와 기관 Adapter 분리 | 기관 장애 격리·독립 배포 | 아키텍트·운영팀 |
| Profile | `SECURE_RESOURCE_API` + `PERSISTENCE_MYBATIS` | 보호 API와 DB 원장 | 개발·빌드팀 |
| Messaging | `EVENT_RABBITMQ` | Queue 단위 소비와 Quorum Queue | 메시징 운영팀 |
| External | `INTEGRATION_TCP` | 고정길이 기관 전문과 응답 유실 대사 | 연동 개발·운영팀 |
| Approval | BZA 결재 연동 | 고액 지급의 2인 승인 | 업무·보안 담당자 |
| Operations | ADM Transaction·Unknown Result·Audit | 실패 거래 조회·재처리·대사 | 운영 담당자 |
| DB | PostgreSQL | 조직 표준 DB | DBA |
| DR | RPO/RTO와 단일 Writer Fencing | 기관 거래 중복 방지 | DR 담당자 |
