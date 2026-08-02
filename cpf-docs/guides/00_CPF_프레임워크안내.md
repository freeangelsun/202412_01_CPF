# CPF 프레임워크 안내 — 만들 수 있는 시스템과 선택 기준

> **주 독자**: 도입 검토자, 업무 기획자, 아키텍트, 개발·운영 책임자
> **완료 결과**: CPF로 만들 업무를 분류하고 제품 구성, 배포 형태, 역할별 매뉴얼과 운영 책임을 결정한다.
> **Source 기준**: `freeangelsun/202412_01_CPF`, `master`, `3b600702502e53877e30cbac594987b371e2186b`

![역할별 문서 지도](png/cpf-guide-map.png)

## 1. 이 문서의 역할

이 문서는 내부 Class나 Module 목록을 설명하는 참조서가 아니다. 도입 조직이 다음 질문에 답하도록 돕는 선택 안내서다.

1. 만들려는 업무 결과는 무엇인가?
2. 온라인, 비동기 메시지, 파일·외부연계, 배치 중 무엇이 필요한가?
3. ADM, BZA, Gateway를 어느 범위까지 이용하는가?
4. 동일 JVM, 모듈형 단일 애플리케이션, 분리 서비스 중 어떤 배포 형태를 선택하는가?
5. 업무 개발팀과 플랫폼 운영팀이 각각 소유할 것은 무엇인가?
6. 미구현·미검증 기능을 계획에 어떻게 반영하는가?

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

| 영역 | 목표 | 기준 Commit 상태 |
|---|---|---|
| `cpf-core` | 토폴로지와 Provider에 독립적인 API·SPI, 식별자, 오류·보안·감사 계약 | 선택 Runtime 의존성이 남아 있어 `부분 구현` |
| `cpf-common` | Code·Calendar·Message·Template 등 업무 공통 | MyBatis·Cache·POI·Validation Runtime이 남아 있어 `부분 구현` |
| `cpf-starters/` | 선택 Runtime과 Provider 자동 설정 | 7개 Leaf 프로젝트 등록, 기능별 검증 수준은 서로 다름 |
| Capability Profile | 사용 사례를 승인 Leaf 목록으로 해석 | `미구현` |
| Aggregate Starter | 승인된 Leaf 조합만 전이 | `미구현` |
| Platform BOM | Version 정렬 | 구현 확인, 게시·소비 Runtime은 `미검증` |

### 5.2 기준 Commit에 등록된 Starter

```text
:cpf-starter-security
:cpf-starter-messaging-kafka
:cpf-starter-cache
:cpf-starter-observability
:cpf-starter-resilience
:cpf-starter-featureflag
:cpf-starter-secret
```

물리 경로는 `cpf-starters/<capability>`이고, `cpf-starters/` 자체는 Gradle 프로젝트가 아니다.

### 5.3 QA38 목표와 현재 사용 가능 범위

QA38은 Base, Persistence, Security 세분화, Messaging Provider, TCP·SFTP, Notification, Quartz 등 다수의 Leaf Starter와 Profile·Resolved Lock을 요구한다. 기준 Commit의 `settings.gradle`에는 위 7개만 등록돼 있으므로 아래 원칙을 적용한다.

- 목표 Artifact 이름을 현재 사용할 수 있는 의존성처럼 안내하지 않는다.
- Source·AutoConfiguration·Properties·Consumer·Test·POM·BOM이 함께 존재할 때 사용 가능으로 전환한다.
- 현재 Core·Common에서 이관 대상인 Runtime은 `부분 구현`으로 기록한다.
- RabbitMQ·JMS·IBM MQ·TCP 등 QA38 필수 범위는 `미구현` 또는 `재확인 필요`로 구분한다.
- 실행하지 않은 DB·Browser·다중 인스턴스·장애 시험은 `미검증`이다.

## 6. 배포 형태 선택

| 형태 | 선택 조건 | 확인할 위험 |
|---|---|---|
| 동일 JVM | 호출 지연을 줄이고 배포 단위를 함께 운영 | 모듈 간 Internal 직접 참조, Transaction 경계 확대 |
| 모듈형 단일 애플리케이션 | 업무 영역을 분리하되 하나의 Artifact로 운영 | DB 소유권 혼합, 숨은 순환 의존 |
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
5. 필요한 Starter와 현재 구현 상태를 대조한다.
6. Generator Dry Run으로 경로·포트·DB Vendor 충돌을 검사한다.
7. 업무 API·DB·Test를 구현한다.
8. 정상·오류·중복·동시성·Timeout·응답 유실·부분 실패를 시험한다.
9. ADM·로그·지표·추적·감사에서 같은 식별자를 확인한다.
10. 배포·Rollback·백업·인계 절차를 운영 담당자에게 전달한다.

## 10. 현재 기준의 주요 Gap

| Gap | 개발 상태 | 검증 상태 | 영향 |
|---|---|---|---|
| Core 선택 Runtime 분리 | 부분 구현 | 미검증 | 비사용 기술의 전이 의존성 |
| Common 기술 Runtime 분리 | 부분 구현 | 미검증 | Cache·MyBatis·POI·Validation 선택성 |
| Capability Profile·Resolved Lock | 미구현 | 미검증 | Generator 선택의 재현성 |
| Aggregate Starter | 미구현 | 미검증 | 묶음 의존성 사용 불가 |
| RabbitMQ·JMS·IBM MQ Provider | 미구현 | 미검증 | QA38 메시징 Provider 범위 |
| TCP·ISO8583 Runtime | 미구현 | 미검증 | 기관 전문 연계 |
| SFTP 실제 Runtime | 부분 구현 또는 재확인 필요 | Docker Fixture 외 제품 Runtime 미검증 | 파일 전송 원장·대사 |
| Notification·Email·SMS | 미구현 또는 부분 구현 | 미검증 | 알림 결과·중복 방지 |
| DB 3 Vendor Lifecycle | Source 존재 범위별 상이 | 실제 실행 미검증 | Fresh·Upgrade·Rollback |
| Browser·다중 인스턴스·Fault | Source별 상이 | 미검증 | 운영 판정 |

## 11. 완료 판정 원칙

문서에서 `완료`로 표시하려면 다음이 같은 exact SHA에서 확인돼야 한다.

- Source·SQL·Config·Generator·Frontend·Script
- 실제 Consumer
- 정상·오류·경계·부분 실패·결과 미확정
- Unit·Contract·Integration·Negative Test
- DB Migration·Upgrade·Rollback 또는 DB-less 근거
- Artifact·POM·BOM·SBOM
- 운영 조회·조치·감사
- 실행 명령, Exit Code와 Sanitized Evidence

직접 실행하지 않은 항목은 `미검증`으로 남긴다.

## 12. 다음 문서

- 업무 기능 개발: [01 CPF 개발자 매뉴얼](01_CPF_개발자매뉴얼.md)
- 배치 개발·운영: [02 CPF 배치 개발 매뉴얼](02_CPF_배치개발매뉴얼.md)
- ADM 연동·이용: [03 CPF ADM 매뉴얼](03_CPF_ADM매뉴얼.md)
- 플랫폼 설치·운영: [05 CPF 플랫폼 운영 매뉴얼](05_CPF_플랫폼운영매뉴얼.md)
- CPF Starters: [90 CPF Starters 매뉴얼](90_CPF_Starters_매뉴얼.md)
- CPF Tools: [91 CPF Tools 매뉴얼](91_CPF_Tools_매뉴얼.md)
- CPF Gateway: [92 CPF Gateway 매뉴얼](92_CPF_Gateway_매뉴얼.md)
- CPF BZA: [95 CPF BZA 매뉴얼](95_CPF_BZA_매뉴얼.md)

## 13. 제품 범위와 비범위

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

## 14. Architecture와 의존 방향

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

## 15. Module Ownership 지도

| Module·제품군 | 소유 범위 | 실제 Consumer 예 | 비고 |
|---|---|---|---|
| `cpf-core` | 기술 중립 계약·식별자·오류·보안·감사 | 업무 Domain·Starter·제품 모듈 | QA38 경량화 진행 중 |
| `cpf-common` | 업무 공통 Code·Calendar·Message·Template | 업무 Domain | 기술 Runtime 이관 필요 |
| `cpf-starters/*` | 선택 Runtime·Provider 자동 설정 | ADM·BZA·Gateway·Batch·업무 Runtime | 7개 공개 Starter 등록 |
| `cpf-reference` | EDU·Reference Consumer | 개발자·검수자 | 기능 Variant와 DB Vendor 선택 |
| `cpf-member` | Generator Golden Reference Domain | Generator 검증 | 직접 의존성 재검토 필요 |
| `cpf-batch/*` | Batch 계약·실행·제어·Scheduler·Worker | Batch Job·ADM | Spring Batch Primary Engine |
| `cpf-admin` | ADM Backend·Frontend | 운영 사용자·Owner Service | 업무 원장 비소유 |
| `cpf-biz-admin` | BZA 조직·권한·결재 | 업무 Service·사용자 | 선택 제품 |
| `cpf-gateway` | Route·정책·게시·Attempt | 외부·내부 API | 선택 제품 |
| `cpf-tools/*` | Build·Generator·DB·Runtime·검증 | 개발·운영·검수 | 도구군 |

Owner가 불분명한 상태에서 공통 모듈에 기능을 추가하지 않는다. 실제 Consumer가 없는 추상화는 개발 검토 대상으로 남긴다.

## 16. DB Lifecycle·Generator·문서 지도 연결

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
