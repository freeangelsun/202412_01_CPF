# Current Architecture Supersession — 2026-08-10

> 기준 SHA `f4eb3518a98bbdb2ef8582a8709281d8627bcc6a`. 본 문서의 EDU/Manual 기능 Requirement는 유지하되 물리 Owner/Starter/DB 명칭은 최신 Canonical을 따른다. 구 `cpf-common` Root는 `cpf-starters/common`으로 대체되며 교육용 고객 업무 Source는 여전히 CPF Core/Starter/Runtime 내부에 삽입하지 않는다. Generated Domain은 Customer Business DB + Domain Prefix 정책과 새 Public Starter Coordinate를 사용한다. 충돌 시 `CPF_FINAL_TARGET_REQUIREMENTS.md`와 `CPF_DEVELOPER_GPT_NEXT_WORK_INSTRUCTION.md`가 우선한다.

---

# CPF 고객 매뉴얼 연계 교육 예제·연동 기능 단일 통합 개발 요구사항

- 기준 Repository: `freeangelsun/202412_01_CPF`
- 기준 Branch: `master`
- 기준 Commit: `19dd72b5978f2a3c630943c0fff05bee2d2fed34`
- 적용 대상: 고객사 매뉴얼의 EDU 실행 경험, 기능별 예제, 운영 확인 경로
- 문서 성격: 공식 고객 매뉴얼이 아니라 개발 GPT에 전달하는 내부 구현 요청서
- 요청 방식: 전체 범위를 한 번에 분석·보완·구현·검증하는 단일 통합 작업
- 확정 구현 기준: 본문에 구체화한 EDU 135개를 모두 구현한다. 정본 대조에서 135개 밖의 실제 누락이 확인되더라도 같은 작업에서 추가하고 구현한다.

## 0. 개발 GPT에 전달할 단일 통합 요청 문구

다음 문장을 그대로 사용한다. 이 요청은 조사·설계·구현·검증을 여러 차수로 나누지 않는 한 번의 통합 개발 요청이다.

```text
Repository의 최신 origin/master와 Local Working Tree를 먼저 확인하고 기존 변경을 보호한 상태에서, cpf-docs/work/current/CPF_CUSTOMER_MANUAL_EDU_IMPLEMENTATION_REQUIREMENTS.md를 처음부터 끝까지 읽어라.

이 작업을 1차 조사, 2차 구현, 후속 보완 요청으로 나누지 마라. 본문에 정의된 EDU 135개는 이번 단일 작업의 확정 필수 구현 목록이다. CPF_FINAL_TARGET_REQUIREMENTS.md, 최신 Source·SQL·API·Config·Frontend·Script·Test, 공식 고객 매뉴얼 8개를 전수 대조하여 고객이 CPF 기능을 이해·개발·운영·복구하는 데 필요한 예제나 연동 기능이 빠졌으면 같은 작업 안에서 신규 EDU ID와 상세 요구사항을 추가하고 즉시 구현하라. 누락 목록만 보고하고 다음 요청을 기다리거나 사용자에게 별도 구현 요청을 요구하지 마라.

본문 135개와 전수 대조에서 확인된 추가 항목 전체에 대해 프로젝트의 Module·Package·Layer·Naming·DB Vendor Pack·Config·Test·ADM/BZA/Gateway 경계를 지켜 Source·SQL·API·Config·Frontend·Script·Test·교육 Catalog·고객 매뉴얼 연결을 함께 구현하라. 교육 예제를 임의 위치나 Runtime 내부에 만들지 말고, 고객 업무 Owner와 CPF 제품 Owner의 책임 경계를 지켜라.

각 EDU는 정상 흐름만 만들지 말고 입력 검증, 권한, 데이터 범위, 중복, 동시성, Timeout, DB Commit 전후 장애, 외부 전송 전후 장애, 응답 유실, 부분 성공, Retry·Restart·Reprocess·Reconcile·Compensation·Rollback, Audit, Log·Metric·Trace, ADM/BZA/Gateway 확인과 고객 업무 전환까지 구현·시험하라.

작업 종료 조건은 본문 135개 전체 완료와 Canonical Requirement와 고객 매뉴얼 기능 전체에 미대응 EDU·연동·운영 확인 Gap이 0건인 상태다. 추가 Gap이 발견되면 같은 작업 범위에 포함한다. 외부 환경이나 접근 권한 때문에 실행할 수 없는 항목은 근거와 재현 명령을 남기되 완료로 표시하지 마라.

모든 변경을 한 번의 Root Overlay ZIP으로 제공하라. ZIP에는 이번 단일 작업의 최종 신규·수정 파일만 포함하고, 정확한 삭제 Manifest, 전체 요구사항 Coverage Matrix, 요청 ID별 구현 파일·Test·검증 결과, SHA-256, 적용·Rollback 명령을 함께 제공하라. Commit·Push·Branch·Tag·PR은 수행하지 마라.
```

## 0.1 단일 통합 작업 원칙

- 본문 135개 요구사항은 이번 요청에서 이미 식별한 **확정 필수 구현 목록**이다.
- 정본·Source·고객 매뉴얼 대조에서 발견한 누락은 별도 후속 요청서로 미루지 않는다.
- 추가 요구사항은 기존 체계에 맞춰 `EDU-DEV`, `EDU-BAT`, `EDU-ADM`, `EDU-BZA`, `EDU-GW`의 다음 번호로 부여한다.
- 추가 ID마다 본문 요구사항과 같은 수준의 필요 근거, 업무 시나리오, 위치, 계약, 정상 흐름, 장애 사례, Test, 운영 확인, 완료 조건을 작성한다.
- Gap 식별, Public Extension 보강, 예제 구현, 자동 Test, Browser 확인, 매뉴얼 연결, 검증을 같은 작업에서 끝낸다.
- “먼저 44개만 구현”, “Gap 목록만 작성”, “다음 차수에서 보완”, “사용자 승인 후 추가 구현” 방식으로 종료하지 않는다.
- 작업량이 크다는 이유로 기능군별 ZIP이나 여러 개의 순차 요청으로 분할하지 않는다. 산출물은 최종 Root Overlay ZIP 하나로 제공한다.

## 0.2 같은 작업 안에서 수행할 전체 순서

1. 최신 `origin/master`와 Working Tree의 보호 대상 확인
2. Canonical Requirement와 개발 요건·Gap 목록 전수 수집
3. 실제 Source·SQL·API·Config·Frontend·Script·Test 기능 목록 작성
4. 공식 고객 매뉴얼의 기능·EDU·운영 절차 목록 작성
5. 본문 135개 EDU Coverage 대조
6. 미대응 기능·예외·복구·운영 확인 Gap에 신규 EDU ID 부여
7. 본문 135개와 전수 대조에서 확인된 신규 ID 전체 구현
8. DB 3종·Kafka·Redis·파일·외부 연계·다중 인스턴스·Browser·장애 Test 수행
9. ADM·BZA·Gateway에서 결과와 복구 경로 확인
10. Source·API·SQL·Test·교육 Catalog·고객 매뉴얼 양방향 정합성 검사
11. 전체 Coverage Matrix에서 미대응 항목 0건 확인
12. Root Overlay ZIP 하나와 삭제 Manifest·검증 결과 제공

각 단계는 내부 작업 순서일 뿐 사용자에게 여러 번 요청하거나 별도 승인을 받기 위한 단계가 아니다.

## 0.3 확정 요청 수와 영역별 범위

이번 요청서는 조사 후 추가하라는 추상 지시가 아니라, 고객 매뉴얼을 작성하면서 필요성이 확인된 **135개 구현 요구사항을 개별 ID로 확정**한다.

| 영역 | 기존 구체 요구사항 | 이번에 추가한 구체 요구사항 | 합계 |
|---|---:|---:|---:|
| 온라인·공통·외부 연계 | 15 | 30 | 45 |
| Batch | 10 | 20 | 30 |
| ADM 업무 연동 | 7 | 10 | 17 |
| BZA 적용·운영 | 6 | 8 | 14 |
| Gateway | 6 | 8 | 14 |
| 플랫폼 설치·운영·복구 | 0 | 15 | 15 |
| **전체** | **44** | **91** | **135** |

개발 GPT는 표의 숫자만 처리하지 않는다. 아래 각 ID의 업무 시나리오·입력·상태·장애·Test·운영 확인·완료 조건을 모두 구현한다. 한 ID를 단순 Controller와 정상 응답 한 건으로 대체할 수 없다.

---

# 1. 이 요청서가 필요한 이유

고객 매뉴얼은 CPF를 처음 접한 고객사 개발자와 운영자가 기능을 따라 사용할 수 있도록 작성되어 있다. 교육 예제가 단순 Controller 한 개, 메모리 고정 응답, 정상 요청 한 건으로 끝나면 다음 내용을 배울 수 없다.

- CPF로 어떤 고객 업무를 만들 수 있는가
- 같은 기능을 Local·Remote·다중 인스턴스에서 어떻게 유지하는가
- DB Commit 전후, 외부 전송 전후, 응답 송신 전후 실패를 어떻게 구분하는가
- 중복 요청, 동시 수정, Timeout, 응답 유실, 부분 성공을 어떻게 복구하는가
- 운영자가 ADM·BZA·Gateway에서 무엇을 확인하고 어떤 상태를 정상으로 판단하는가
- 교육 예제를 실제 고객 업무로 바꿀 때 어느 파일을 수정하고 어느 파일은 CPF 관리 영역으로 남겨야 하는가

따라서 아래 135개 요청은 단순 Sample 추가가 아니라 **고객이 CPF의 기능과 복구 방식을 실제로 실행해 보는 교육 제품**으로 구현한다. 135개는 현재 고객 매뉴얼 작성 과정에서 구체적으로 식별한 필수 범위이며, 정본·실제 구현·고객 매뉴얼 대조에서 추가로 필요한 예제·연동·복구·운영 확인이 발견되면 같은 작업에서 요구사항을 추가하고 구현한다.

# 2. 구현 기준과 우선순위

## 2.1 정본 우선순위

1. `CPF_FINAL_TARGET_REQUIREMENTS.md`
2. 최신 `origin/master`의 Source·SQL·API·Config·Frontend·Script·Test
3. Architecture·Specification 정본
4. `cpf-docs/specification/CPF_DOCUMENTATION_STANDARD.md`
5. 공식 고객 매뉴얼 8개
6. 이 상세 개발 요구사항

문서에 제안된 Class명보다 현재 Source의 Public API·SPI·Owner Port가 우선한다. 다만 현재 진입점이 없어 교육 예제를 연결할 수 없다면 임시 우회 코드를 만들지 말고 Owner Module에 재사용 가능한 Public Extension을 먼저 구현한다.

## 2.2 상태와 완료 판정

본문 135개와 같은 작업에서 추가된 모든 요청은 다음 상태 중 하나로 관리한다.

- 완료
- 부분 구현
- 미구현
- 미검증
- 실패
- 재확인 필요

코드가 존재한다는 이유만으로 완료하지 않는다. 요청별 단위·통합·장애·복구·운영 확인 Test가 모두 통과하고 매뉴얼의 교육 ID·명령·결과와 일치해야 완료다.

최종 작업 완료 판정에는 `완료`만 허용한다. 다른 상태가 하나라도 남으면 전체 작업을 완료로 보고하지 않는다. 다만 Repository 밖의 인증정보·상용 외부기관·사용자 소유 인프라처럼 현재 작업자가 해결할 수 없는 외부 차단 요인은 정확한 근거·재현 명령·필요 조치를 기록하고 전체 완료 판정에서 제외하지 않는다.

## 2.3 우선순위

- **P0**: 고객 업무의 기본 개발·중복·동시성·서비스 호출·Kafka·배치 재시작·결과 대사·ADM 연동
- **P1**: 파일·외부연계·보안·DB 3종·BZA·Gateway 게시와 복구
- **P2**: 부가 기능·전체 Catalog·교육 자동 실행·품질 편의 기능

# 3. Module·Package·파일 배치 표준

## 3.1 교육 예제 Owner

| 구분 | 허용 위치 | 금지 위치 |
|---|---|---|
| 온라인·연계 EDU 업무 Source | `cpf-education/src/main/java/com/cpf/reference/edu/...` | `cpf-core`, `cpf-starters/common`, Starter, Runtime 내부에 고객 업무 예제 삽입 |
| 온라인·연계 EDU Test | `cpf-education/src/test/java/com/cpf/reference/edu/...` | 별도 Root Test Project, 임시 Script만으로 대체 |
| EDU Resource | `cpf-education/src/main/resources/edu/<ID>/...` | Repository Root, `tmp`, `sample-data` 무분별 생성 |
| Batch EDU | `cpf-education/src/main/java/com/cpf/reference/edu/batNN/...` | `com.cpf.batch.edu`, Batch Runtime Owner Module 안의 교육 Job |
| ADM 연동 EDU 고객 업무 | `cpf-education/src/main/java/com/cpf/reference/edu/adm/admNN/...` | ADM Backend에 고객 지급·회원 업무 로직 직접 구현 |
| BZA 연동 EDU 고객 업무 | `cpf-education/src/main/java/com/cpf/reference/edu/bza/bzaNN/...` | BZA 제품 내부에 고객 업무 원장 로직 삽입 |
| Gateway 대상 서비스 EDU | `cpf-education/src/main/java/com/cpf/reference/edu/gateway/...` | Gateway Runtime에 Mock 고객 API 하드코딩 |
| Generator Template | `cpf-tools/generator`의 기존 Template 구조 | `cpf-education`에 Generator Template 복사 |
| 고객 업무 Batch Job Pack 예제 | Generator 표준을 따른 독립 Job Pack, 예: `cpf-member-jobpack` | `cpf-batch` Runtime에 고객 Job 포함 |
| DB Schema·Migration·Seed | `cpf-tools/db/vendor/<vendor>` 아래 기존 Vendor Pack 구조 | Module-local Vendor SQL fallback, 세 Vendor의 의미가 다른 SQL |

## 3.2 Java Package 규칙

- 공식 Module은 `cpf-` 접두사를 유지한다.
- Package는 읽을 수 있는 업무명과 기능명을 사용한다.
- 교육 ID를 Package에 포함한다. 예: `com.cpf.education.edu.dev05.idempotency`.
- Controller·Application Service·Domain·Persistence·Integration·Config·Test를 한 Package에 몰아넣지 않는다.
- `util`, `misc`, `temp`, `common2`, `sample`, `test1` 같은 의미 없는 Package를 만들지 않는다.
- 공통화는 두 개 이상의 실제 Consumer가 확인될 때만 Owner Module의 Public API·SPI로 승격한다.
- 교육 전용 코드는 `cpf-education` 밖에서 Public API처럼 노출하지 않는다.

권장 Slice 예시는 다음과 같다.

```text
com.cpf.education.edu.dev05.idempotency
├─ api            요청·응답 DTO와 Controller
├─ application    Use Case와 Transaction 경계
├─ domain         상태·규칙·식별자
├─ persistence    Repository Adapter와 DB Mapping
├─ integration    외부·Kafka·파일 Adapter
├─ operation      Operation 상태·대사·복구
└─ support        교육 Fixture와 Failure Injection
```

## 3.3 Layer와 의존 방향

```text
API / Batch Entry / Message Consumer
        ↓
Application Use Case
        ↓
Domain Rule + Public Port
        ↓
Persistence / Remote / Kafka / File Adapter
```

- Controller에서 Repository를 직접 호출하지 않는다.
- Domain에서 Spring MVC, JDBC, Kafka, 파일 시스템 구현에 의존하지 않는다.
- Local과 Remote는 같은 Public 계약을 소비한다.
- 고객 업무의 상태 변경은 업무 Owner가 수행한다. ADM·BZA·Gateway가 고객 DB를 직접 수정하지 않는다.
- Timeout·Retry·Circuit Breaker는 Adapter 경계에 둔다. Domain 규칙에 재시도 Loop를 넣지 않는다.

## 3.4 DB Vendor Pack

- Oracle·PostgreSQL·MariaDB의 Table·Column·Constraint·Index·기본값·상태 의미를 맞춘다.
- 교육 SQL을 `cpf-education/src/main/resources/schema.sql` 같은 Local Fallback으로 만들지 않는다.
- 현재 Vendor Pack의 Install·Upgrade·Rollback·Verify·Seed 구조를 먼저 확인하고 같은 구조에 추가한다.
- Migration은 재실행, 중단 후 재개, 기존 데이터 Backfill, Rollback 가능 범위를 Test한다.
- Test는 `cpf.db.vendor`와 중앙 Vendor Pack 경로를 사용한다.

## 3.5 Config·Secret

- 설정 Key는 기존 Prefix와 Binding 구조를 따른다.
- URL·계정·Token·Password·Certificate 원문을 Source와 문서에 넣지 않는다.
- 기본 Profile에서 외부 시스템·Kafka·DB·Batch Job이 임의 실행되지 않게 한다.
- 교육 기능은 명시적 교육 Profile 또는 명시적 요청으로 활성화한다.
- Secret 교체·누락·권한 오류의 실패 메시지는 Secret 원문을 노출하지 않는다.

## 3.6 Test 이름과 위치

각 ID는 최소 다음 Test를 가진다.

```text
EduDev05IdempotencyUnitTest
EduDev05IdempotencyIntegrationTest
EduDev05IdempotencyFailureTest
EduDev05IdempotencyRecoveryTest
```

화면이 있는 경우 기존 Frontend Test 구조에 같은 ID를 포함한다.

```text
EDU-ADM-05 Browser Test
EDU-BZA-04 Browser Test
EDU-GW-05 Publish/Apply Browser Test
```

새로운 병렬 Test Root를 만들지 말고 해당 Module의 기존 Test Convention을 따른다.

## 3.7 교육 Catalog와 실행 명령

- 모든 교육 ID를 조회하는 Machine-readable Catalog를 제공한다.
- 권장 경로: `cpf-education/src/main/resources/edu/catalog.json` 또는 현재 프로젝트의 기존 Catalog 표준 위치.
- Catalog에는 ID, 제목, 대상 역할, 선행 Service, 실행 Test, 요청 예시, 정상 결과, 장애 시나리오, 관련 매뉴얼 Anchor를 포함한다.
- 개별 Test와 영역별 Test, 전체 Test 명령을 제공한다.
- Windows PowerShell과 CI에서 같은 의미로 실행되어야 한다.

# 4. 모든 요청에 공통으로 필요한 결과물

각 ID는 다음 결과물을 모두 제공한다.

1. 고객 업무 시나리오와 상태표
2. Request·Response DTO 또는 Job Parameter
3. Validation과 오류 분류
4. Application Use Case
5. Domain 상태 전이와 Version 규칙
6. DB·Kafka·파일·외부연계 Adapter
7. Operation ID·업무 ID·Correlation ID 연결
8. 정상·오류·중복·동시성·Timeout·응답 유실·부분 실패·복구 Test
9. Log·Metric·Trace·Audit 확인
10. ADM·BZA·Gateway에서 확인할 위치
11. 고객 업무로 전환할 파일과 CPF 관리 파일 구분
12. 매뉴얼 EDU ID·실행 명령·기대 결과 갱신

---

# 6. EDU-DEV-01 — Generator 기반 신규 업무 영역 생성

| 항목 | 내용 |
|---|---|
| 영역 | 온라인·연계 개발 |
| 우선순위 | P1 |
| 연결 매뉴얼 | 01 개발자 매뉴얼 3장 |
| 구현 단위 | 하나의 독립 Pull Request 또는 상호 원자적인 작업 묶음 |

## 필요 근거

고객이 CPF 내부 구조를 복사하지 않고 자기 업무 영역을 표준 Module·Package·DB·Port 구조로 시작할 수 있어야 한다.

## 고객 업무 시나리오

고객사는 `payment` 업무 영역을 생성한다. System Code는 `PAY`, DB는 PostgreSQL, 기본 Port와 Route는 고객 할당표에서 선택한다.

## 구현 위치와 책임 경계

Generator 구현과 Template은 `cpf-tools/generator`의 기존 구조를 사용한다. 생성 결과 검증용 교육 Fixture만 `com.cpf.education.edu.dev01.domain`에 둔다.

- Resource·Fixture: 생성 계획 Golden File과 충돌 Fixture는 `cpf-education/src/main/resources/edu/dev01/`에 둔다.
- 교육 ID는 Package, Test Class, Catalog, Log, 매뉴얼에서 `EDU-DEV-01`로 동일하게 사용한다.
- 예제 때문에 Runtime Owner Module에 고객 업무 상태·Table·Controller를 추가하지 않는다.
- 기존 Public API·SPI·Owner Port가 있으면 그것을 사용한다. 없으면 Owner Module에 재사용 가능한 Extension을 먼저 구현하고 교육 예제는 그 Extension의 Consumer가 된다.

## 입력·계약

PowerShell Generator의 Dry Run과 실제 생성 명령. 입력: DomainName, SystemCode, DatabaseVendor, Port, Route, Package Base.

## 상태와 저장 근거

PLANNED → VALIDATED → GENERATED → VERIFIED. 충돌 시 REJECTED이며 파일을 만들지 않는다.

업무 ID, Operation ID, 요청 Hash, Version, Actor, Reason, Correlation ID를 적용 가능한 범위에서 같은 처리 근거로 연결한다.

## 정상 처리 순서

1. Dry Run에서 생성·수정 예정 경로를 출력한다.
2. Module·Package·SystemCode·Port·Route·DB Schema 충돌을 검사한다.
3. 실제 생성 뒤 settings/build/config/DB/Test/OpenAPI가 함께 생성된다.
4. 생성된 Module의 Build와 기본 Test를 실행한다.

## 예외·장애·경계 사례

| 사례 | 구현·검증 요구 |
|---|---|
| 같은 Module명 | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |
| 같은 SystemCode | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |
| Port 중복 | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |
| Route 중복 | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |
| 지원하지 않는 DB Vendor | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |
| 생성 중 Process 종료 | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |
| 기존 파일 Hash 변경 시 덮어쓰기 시도 | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |

## 필수 Test

- Dry Run이 Working Tree를 변경하지 않는지 확인
- 충돌별 실패 메시지와 Exit Code 확인
- 생성 중단 뒤 이번 실행 생성 파일만 안전하게 식별
- 생성 결과 Build·Test·DB Pack 참조 확인

## 운영·화면 확인

ADM 서비스 등록과 운영 식별자가 생성 산출물에 포함되는지 확인한다.

- Log·Metric·Trace·Audit에서 같은 업무 ID와 Operation ID를 검색한다.
- 화면의 Field·Button·상태·오류 메시지가 매뉴얼 설명과 일치해야 한다.
- 응답 유실 또는 부분 실패가 있는 요청은 새 요청 전에 Reconcile 경로를 제공한다.

## 완료 조건

- 기존 파일을 덮어쓰지 않는다.
- 생성 계획과 실제 생성 파일이 일치한다.
- 고객이 바꿀 영역과 Generator 관리 영역이 구분된다.
- 실행 명령, 입력값, 기대 결과, 장애 재현, 복구 명령을 매뉴얼의 같은 EDU ID에 반영한다.
- 직접 실행하지 않은 DB Vendor·Browser·다중 인스턴스·장애 Test를 성공으로 기록하지 않는다.

## 금지 사항

- 메모리 Map과 고정 JSON만 반환하는 예제로 종료
- 정상 Case 한 건만 Test
- 고객 업무 DB를 ADM·BZA·Gateway가 직접 수정
- 교육 Profile을 기본 Profile에서 자동 활성화
- 비밀번호·Token·개인정보 원문을 Source·Log·Fixture에 기록
- 기존 Package Convention과 다른 별도 `sample`, `demo`, `misc` Root 생성

---

# 7. EDU-DEV-02 — 권한·범위가 적용된 목록·상세 조회

| 항목 | 내용 |
|---|---|
| 영역 | 온라인·연계 개발 |
| 우선순위 | P0 |
| 연결 매뉴얼 | 01 개발자 매뉴얼 4장 |
| 구현 단위 | 하나의 독립 Pull Request 또는 상호 원자적인 작업 묶음 |

## 필요 근거

대부분의 고객 업무가 목록·상세 조회로 시작하며 Paging·정렬·기간·Data Scope가 누락되면 운영 화면과 API가 서로 다른 결과를 낸다.

## 고객 업무 시나리오

지점 사용자가 활성 고객 목록을 조회하고 한 고객의 상세를 연다. 본점 사용자는 전 지점을 보고 지점 사용자는 자기 지점만 본다.

## 구현 위치와 책임 경계

`com.cpf.education.edu.dev02.query` 아래 api/application/domain/persistence로 분리한다.

- Resource·Fixture: 20건 이상 Seed, 조직·권한 Fixture, Paging 경계값을 `edu/dev02`와 중앙 Vendor Pack Seed에 둔다.
- 교육 ID는 Package, Test Class, Catalog, Log, 매뉴얼에서 `EDU-DEV-02`로 동일하게 사용한다.
- 예제 때문에 Runtime Owner Module에 고객 업무 상태·Table·Controller를 추가하지 않는다.
- 기존 Public API·SPI·Owner Port가 있으면 그것을 사용한다. 없으면 Owner Module에 재사용 가능한 Extension을 먼저 구현하고 교육 예제는 그 Extension의 Consumer가 된다.

## 입력·계약

`GET /edu/customers?status=ACTIVE&organizationId=...&page=0&size=20&sort=updatedAt,desc`, `GET /edu/customers/{customerId}`.

## 상태와 저장 근거

조회는 상태를 변경하지 않는다. 응답에는 조회 기준시각, Page, Total, Data Scope가 반영된 결과를 포함한다.

업무 ID, Operation ID, 요청 Hash, Version, Actor, Reason, Correlation ID를 적용 가능한 범위에서 같은 처리 근거로 연결한다.

## 정상 처리 순서

1. 검색조건 Default와 최대 범위를 서버에서 적용한다.
2. 권한·조직 범위를 Query에 강제한다.
3. 목록과 상세가 같은 Masking·상태 의미를 사용한다.
4. 빈 결과와 다음 Page를 구분한다.

## 예외·장애·경계 사례

| 사례 | 구현·검증 요구 |
|---|---|
| size 초과 | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |
| 허용하지 않는 sort field | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |
| 최대 기간 초과 | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |
| 존재하지 않는 ID | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |
| 권한 밖 조직 | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |
| DB Timeout | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |
| 부분 데이터 Source 지연 | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |

## 필수 Test

- Vendor 3종 Paging·정렬 결과 동일성
- 권한별 결과 건수
- N+1 또는 과도한 Query 방지
- 빈 결과·Not Found·Forbidden 구분
- 조회 Timeout 시 오류 코드·Trace 확인

## 운영·화면 확인

ADM 거래·로그·감사에서 customerId, organizationId, correlationId로 조회할 수 있어야 한다.

- Log·Metric·Trace·Audit에서 같은 업무 ID와 Operation ID를 검색한다.
- 화면의 Field·Button·상태·오류 메시지가 매뉴얼 설명과 일치해야 한다.
- 응답 유실 또는 부분 실패가 있는 요청은 새 요청 전에 Reconcile 경로를 제공한다.

## 완료 조건

- 권한 밖 데이터가 0건이다.
- 목록 Total과 상세 접근 가능 범위가 일치한다.
- 고객 개발자가 조건·기본값·최대값을 매뉴얼만으로 바꿀 수 있다.
- 실행 명령, 입력값, 기대 결과, 장애 재현, 복구 명령을 매뉴얼의 같은 EDU ID에 반영한다.
- 직접 실행하지 않은 DB Vendor·Browser·다중 인스턴스·장애 Test를 성공으로 기록하지 않는다.

## 금지 사항

- 메모리 Map과 고정 JSON만 반환하는 예제로 종료
- 정상 Case 한 건만 Test
- 고객 업무 DB를 ADM·BZA·Gateway가 직접 수정
- 교육 Profile을 기본 Profile에서 자동 활성화
- 비밀번호·Token·개인정보 원문을 Source·Log·Fixture에 기록
- 기존 Package Convention과 다른 별도 `sample`, `demo`, `misc` Root 생성

---

# 8. EDU-DEV-03 — 등록·수정·상태 변경과 감사

| 항목 | 내용 |
|---|---|
| 영역 | 온라인·연계 개발 |
| 우선순위 | P0 |
| 연결 매뉴얼 | 01 개발자 매뉴얼 5장 |
| 구현 단위 | 하나의 독립 Pull Request 또는 상호 원자적인 작업 묶음 |

## 필요 근거

고객 업무의 핵심은 상태 전이이며 단순 CRUD 예제로는 사유·권한·Version·감사·후속 이벤트를 배울 수 없다.

## 고객 업무 시나리오

고객 등록 후 `ACTIVE` 상태가 된 고객을 운영자가 사유와 함께 `SUSPENDED`로 변경한다.

## 구현 위치와 책임 경계

`com.cpf.education.edu.dev03.command` 아래 상태 전이 Domain과 Command Use Case를 둔다.

- Resource·Fixture: 상태 전이표, 권한 Fixture, 감사 예상 결과를 `edu/dev03`에 둔다.
- 교육 ID는 Package, Test Class, Catalog, Log, 매뉴얼에서 `EDU-DEV-03`로 동일하게 사용한다.
- 예제 때문에 Runtime Owner Module에 고객 업무 상태·Table·Controller를 추가하지 않는다.
- 기존 Public API·SPI·Owner Port가 있으면 그것을 사용한다. 없으면 Owner Module에 재사용 가능한 Extension을 먼저 구현하고 교육 예제는 그 Extension의 Consumer가 된다.

## 입력·계약

`POST /edu/customers`, `PUT /edu/customers/{id}`, `POST /edu/customers/{id}/suspend` 요청에 expectedVersion, reason, operationId 포함.

## 상태와 저장 근거

PENDING → ACTIVE → SUSPENDED → CLOSED. 허용되지 않은 역전이는 거부한다.

업무 ID, Operation ID, 요청 Hash, Version, Actor, Reason, Correlation ID를 적용 가능한 범위에서 같은 처리 근거로 연결한다.

## 정상 처리 순서

1. 입력 Validation 후 업무 Owner가 상태 전이를 결정한다.
2. Version을 증가시키고 변경 전후를 감사에 기록한다.
3. 필요한 후속 Event를 같은 Transaction의 Outbox에 기록한다.
4. 응답 후 DB 상태와 감사 상태를 재조회한다.

## 예외·장애·경계 사례

| 사례 | 구현·검증 요구 |
|---|---|
| 사유 누락 | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |
| 허용되지 않은 상태 전이 | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |
| 권한 없음 | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |
| Version 충돌 | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |
| 감사 저장 실패 | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |
| Outbox 저장 실패 | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |
| 응답 유실 | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |

## 필수 Test

- 상태 전이 Matrix Unit Test
- Transaction Rollback 시 상태·감사·Outbox 불일치 없음
- 권한별 허용 Action
- 응답 유실 후 operation 조회

## 운영·화면 확인

ADM에서 변경 전후, 실행자, 사유, Version, Operation 결과를 확인한다.

- Log·Metric·Trace·Audit에서 같은 업무 ID와 Operation ID를 검색한다.
- 화면의 Field·Button·상태·오류 메시지가 매뉴얼 설명과 일치해야 한다.
- 응답 유실 또는 부분 실패가 있는 요청은 새 요청 전에 Reconcile 경로를 제공한다.

## 완료 조건

- 상태·Version·감사·Outbox가 한 결과를 가리킨다.
- DB 직접 수정 없이 같은 조치를 재현한다.
- 실행 명령, 입력값, 기대 결과, 장애 재현, 복구 명령을 매뉴얼의 같은 EDU ID에 반영한다.
- 직접 실행하지 않은 DB Vendor·Browser·다중 인스턴스·장애 Test를 성공으로 기록하지 않는다.

## 금지 사항

- 메모리 Map과 고정 JSON만 반환하는 예제로 종료
- 정상 Case 한 건만 Test
- 고객 업무 DB를 ADM·BZA·Gateway가 직접 수정
- 교육 Profile을 기본 Profile에서 자동 활성화
- 비밀번호·Token·개인정보 원문을 Source·Log·Fixture에 기록
- 기존 Package Convention과 다른 별도 `sample`, `demo`, `misc` Root 생성

---

# 9. EDU-DEV-04 — 동시 수정과 예상 Version 충돌

| 항목 | 내용 |
|---|---|
| 영역 | 온라인·연계 개발 |
| 우선순위 | P0 |
| 연결 매뉴얼 | 01 개발자 매뉴얼 6장 |
| 구현 단위 | 하나의 독립 Pull Request 또는 상호 원자적인 작업 묶음 |

## 필요 근거

동시 수정 예제가 없으면 고객은 마지막 저장이 앞선 변경을 덮어쓰는 문제를 발견하지 못한다.

## 고객 업무 시나리오

두 사용자가 같은 한도 정보를 Version 7로 조회한 뒤 서로 다른 금액으로 동시에 수정한다.

## 구현 위치와 책임 경계

`com.cpf.education.edu.dev04.concurrency` 아래 Domain Version, Repository 조건부 Update, API 오류 변환을 둔다.

- Resource·Fixture: 동시 실행 Fixture와 Barrier/Latch 기반 Test Support를 `edu/dev04`에 둔다.
- 교육 ID는 Package, Test Class, Catalog, Log, 매뉴얼에서 `EDU-DEV-04`로 동일하게 사용한다.
- 예제 때문에 Runtime Owner Module에 고객 업무 상태·Table·Controller를 추가하지 않는다.
- 기존 Public API·SPI·Owner Port가 있으면 그것을 사용한다. 없으면 Owner Module에 재사용 가능한 Extension을 먼저 구현하고 교육 예제는 그 Extension의 Consumer가 된다.

## 입력·계약

`PUT /edu/limits/{limitId}` body: amount, expectedVersion, reason, operationId.

## 상태와 저장 근거

Version 7에서 두 요청 중 하나만 Version 8로 성공. 나머지는 VERSION_CONFLICT.

업무 ID, Operation ID, 요청 Hash, Version, Actor, Reason, Correlation ID를 적용 가능한 범위에서 같은 처리 근거로 연결한다.

## 정상 처리 순서

1. 현재 Version을 응답에 포함한다.
2. Update WHERE id=? AND version=? 방식 또는 동등한 Optimistic Lock을 사용한다.
3. 충돌 응답에 최신 Version 재조회 방법을 제공한다.
4. 사용자가 최신값과 자기 변경을 비교 후 재시도한다.

## 예외·장애·경계 사례

| 사례 | 구현·검증 요구 |
|---|---|
| 동일 Version 동시 요청 | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |
| 오래된 Version | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |
| 재시도 중 또 다른 변경 | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |
| 응답 유실 후 성공 여부 불명 | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |
| DB Deadlock | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |

## 필수 Test

- 실제 동시 Thread/Request로 1건 성공·1건 충돌
- 세 DB Vendor에서 같은 의미
- 충돌 요청이 감사 성공으로 기록되지 않음
- 성공 응답 유실 후 Operation 대사

## 운영·화면 확인

ADM 상세에 현재 Version과 최근 변경자·사유를 표시하고 충돌 로그를 찾을 수 있게 한다.

- Log·Metric·Trace·Audit에서 같은 업무 ID와 Operation ID를 검색한다.
- 화면의 Field·Button·상태·오류 메시지가 매뉴얼 설명과 일치해야 한다.
- 응답 유실 또는 부분 실패가 있는 요청은 새 요청 전에 Reconcile 경로를 제공한다.

## 완료 조건

- 동일 Version의 두 수정이 모두 성공하지 않는다.
- 충돌 후 최신값 재조회 절차가 명확하다.
- 실행 명령, 입력값, 기대 결과, 장애 재현, 복구 명령을 매뉴얼의 같은 EDU ID에 반영한다.
- 직접 실행하지 않은 DB Vendor·Browser·다중 인스턴스·장애 Test를 성공으로 기록하지 않는다.

## 금지 사항

- 메모리 Map과 고정 JSON만 반환하는 예제로 종료
- 정상 Case 한 건만 Test
- 고객 업무 DB를 ADM·BZA·Gateway가 직접 수정
- 교육 Profile을 기본 Profile에서 자동 활성화
- 비밀번호·Token·개인정보 원문을 Source·Log·Fixture에 기록
- 기존 Package Convention과 다른 별도 `sample`, `demo`, `misc` Root 생성

---

# 10. EDU-DEV-05 — 지급 등록 멱등성·응답 유실·결과 대사

| 항목 | 내용 |
|---|---|
| 영역 | 온라인·연계 개발 |
| 우선순위 | P0 |
| 연결 매뉴얼 | 01 개발자 매뉴얼 7장 |
| 구현 단위 | 하나의 독립 Pull Request 또는 상호 원자적인 작업 묶음 |

## 필요 근거

금전·주문·접수 업무는 Client 재전송과 응답 유실이 빈번하며 단순 Retry 예제는 중복 원장을 만든다.

## 고객 업무 시나리오

고객이 지급 10,000원을 등록한다. 서버는 DB Commit 후 응답 송신 직전에 연결이 끊긴다. Client는 같은 Operation ID로 결과를 조회한다.

## 구현 위치와 책임 경계

`com.cpf.education.edu.dev05.idempotency` 아래 payment, operation, ledger를 분리한다.

- Resource·Fixture: 요청 Hash Fixture, 응답 유실 Failure Point, 대사 SQL을 `edu/dev05`에 둔다.
- 교육 ID는 Package, Test Class, Catalog, Log, 매뉴얼에서 `EDU-DEV-05`로 동일하게 사용한다.
- 예제 때문에 Runtime Owner Module에 고객 업무 상태·Table·Controller를 추가하지 않는다.
- 기존 Public API·SPI·Owner Port가 있으면 그것을 사용한다. 없으면 Owner Module에 재사용 가능한 Extension을 먼저 구현하고 교육 예제는 그 Extension의 Consumer가 된다.

## 입력·계약

`POST /edu/payments` headers/body에 Idempotency-Key 또는 operationId, customerId, amount, currency. `GET /edu/operations/{operationId}`와 `GET /edu/payments/by-operation/{operationId}` 제공.

## 상태와 저장 근거

RECEIVED → PROCESSING → SUCCEEDED|FAILED|UNKNOWN_RESULT → RECONCILED. 지급 원장은 operationId당 1건.

업무 ID, Operation ID, 요청 Hash, Version, Actor, Reason, Correlation ID를 적용 가능한 범위에서 같은 처리 근거로 연결한다.

## 정상 처리 순서

1. 요청 Hash와 Operation을 먼저 등록한다.
2. 같은 Key·같은 Hash는 기존 결과를 반환한다.
3. 같은 Key·다른 Hash는 충돌로 거부한다.
4. 업무 원장과 Operation 결과를 Transaction으로 연결한다.
5. 응답 유실 시 새 지급을 만들지 않고 상태 조회와 대사를 수행한다.

## 예외·장애·경계 사례

| 사례 | 구현·검증 요구 |
|---|---|
| 같은 Key·같은 본문 | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |
| 같은 Key·다른 본문 | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |
| PROCESSING 중 재호출 | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |
| DB Commit 전 중단 | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |
| DB Commit 후 응답 유실 | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |
| 외부 이체 전송 후 응답 유실 | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |
| Operation 만료 후 재호출 | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |

## 필수 Test

- 원장 1건 보장
- 요청 Hash 충돌
- 동시 10회 같은 Key
- Failure Point별 상태
- 대사 후 UNKNOWN_RESULT 해소
- 로그·Trace·감사에 같은 operationId

## 운영·화면 확인

ADM 결과 미확정·Recovery Center에서 operationId로 원장·시도·상대 결과를 대사한다.

- Log·Metric·Trace·Audit에서 같은 업무 ID와 Operation ID를 검색한다.
- 화면의 Field·Button·상태·오류 메시지가 매뉴얼 설명과 일치해야 한다.
- 응답 유실 또는 부분 실패가 있는 요청은 새 요청 전에 Reconcile 경로를 제공한다.

## 완료 조건

- 같은 Key로 중복 지급이 없다.
- 응답 유실 후 재등록 없이 결과를 확정한다.
- UNKNOWN_RESULT를 성공으로 추정하지 않는다.
- 실행 명령, 입력값, 기대 결과, 장애 재현, 복구 명령을 매뉴얼의 같은 EDU ID에 반영한다.
- 직접 실행하지 않은 DB Vendor·Browser·다중 인스턴스·장애 Test를 성공으로 기록하지 않는다.

## 금지 사항

- 메모리 Map과 고정 JSON만 반환하는 예제로 종료
- 정상 Case 한 건만 Test
- 고객 업무 DB를 ADM·BZA·Gateway가 직접 수정
- 교육 Profile을 기본 Profile에서 자동 활성화
- 비밀번호·Token·개인정보 원문을 Source·Log·Fixture에 기록
- 기존 Package Convention과 다른 별도 `sample`, `demo`, `misc` Root 생성

---

# 11. EDU-DEV-06 — 같은 애플리케이션·분리 서비스 호출 동등성

| 항목 | 내용 |
|---|---|
| 영역 | 온라인·연계 개발 |
| 우선순위 | P0 |
| 연결 매뉴얼 | 01 개발자 매뉴얼 8장 |
| 구현 단위 | 하나의 독립 Pull Request 또는 상호 원자적인 작업 묶음 |

## 필요 근거

고객이 Modular Monolith에서 시작해 MSA로 분리할 때 호출부를 다시 작성하지 않아야 한다.

## 고객 업무 시나리오

계약 서비스가 회원 정보를 조회한다. Local 배포에서는 같은 JVM Port, Remote 배포에서는 HTTP Adapter를 사용한다.

## 구현 위치와 책임 경계

계약 Public Port는 고객 업무 Package에 두고 `com.cpf.education.edu.dev06.localremote`에 Local/Remote Adapter와 교육 설정을 둔다.

- Resource·Fixture: Local·Remote Profile, Stub Server Failure Fixture를 `edu/dev06`에 둔다.
- 교육 ID는 Package, Test Class, Catalog, Log, 매뉴얼에서 `EDU-DEV-06`로 동일하게 사용한다.
- 예제 때문에 Runtime Owner Module에 고객 업무 상태·Table·Controller를 추가하지 않는다.
- 기존 Public API·SPI·Owner Port가 있으면 그것을 사용한다. 없으면 Owner Module에 재사용 가능한 Extension을 먼저 구현하고 교육 예제는 그 Extension의 Consumer가 된다.

## 입력·계약

`GET /edu/contracts/{contractId}/member`; 내부 Public Port 입력·출력은 배포 방식과 무관하게 동일.

## 상태와 저장 근거

호출 성공·업무 Not Found·권한 오류·Timeout·UNKNOWN_RESULT 의미가 Local/Remote에서 동일해야 한다.

업무 ID, Operation ID, 요청 Hash, Version, Actor, Reason, Correlation ID를 적용 가능한 범위에서 같은 처리 근거로 연결한다.

## 정상 처리 순서

1. 호출자는 Public Port만 의존한다.
2. Profile/Registry 설정으로 Local 또는 Remote Adapter를 선택한다.
3. 시간 예산과 Correlation ID를 전달한다.
4. Remote 응답을 Public 오류 계약으로 변환한다.

## 예외·장애·경계 사례

| 사례 | 구현·검증 요구 |
|---|---|
| Remote 연결 실패 | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |
| 응답 Timeout | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |
| 잘못된 JSON | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |
| 하위 서비스 4xx/5xx | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |
| 호출 후 응답 유실 | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |
| 서비스 주소 변경 | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |
| 다중 인스턴스 일부 장애 | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |

## 필수 Test

- 같은 Contract Test를 Local/Remote Adapter에 적용
- 오류 코드·상태 동일성
- Trace Parent 전달
- 시간 예산 초과
- Load Balancing과 장애 인스턴스 제외

## 운영·화면 확인

ADM Topology·Service Call·Trace에서 호출 경로와 선택 Instance를 확인한다.

- Log·Metric·Trace·Audit에서 같은 업무 ID와 Operation ID를 검색한다.
- 화면의 Field·Button·상태·오류 메시지가 매뉴얼 설명과 일치해야 한다.
- 응답 유실 또는 부분 실패가 있는 요청은 새 요청 전에 Reconcile 경로를 제공한다.

## 완료 조건

- Caller Source 수정 없이 Local/Remote 전환
- 업무 응답과 오류 의미가 동일
- Remote 전용 기술 예외가 고객 API로 누출되지 않음
- 실행 명령, 입력값, 기대 결과, 장애 재현, 복구 명령을 매뉴얼의 같은 EDU ID에 반영한다.
- 직접 실행하지 않은 DB Vendor·Browser·다중 인스턴스·장애 Test를 성공으로 기록하지 않는다.

## 금지 사항

- 메모리 Map과 고정 JSON만 반환하는 예제로 종료
- 정상 Case 한 건만 Test
- 고객 업무 DB를 ADM·BZA·Gateway가 직접 수정
- 교육 Profile을 기본 Profile에서 자동 활성화
- 비밀번호·Token·개인정보 원문을 Source·Log·Fixture에 기록
- 기존 Package Convention과 다른 별도 `sample`, `demo`, `misc` Root 생성

---

# 12. EDU-DEV-07 — Kafka Outbox·Inbox·중복 소비·재처리

| 항목 | 내용 |
|---|---|
| 영역 | 온라인·연계 개발 |
| 우선순위 | P0 |
| 연결 매뉴얼 | 01 개발자 매뉴얼 9장 |
| 구현 단위 | 하나의 독립 Pull Request 또는 상호 원자적인 작업 묶음 |

## 필요 근거

Kafka 예제가 Producer send와 Listener 한 개로 끝나면 DB와 메시지의 원자성, 중복 소비, 재시작, DLQ를 배울 수 없다.

## 고객 업무 시나리오

회원 등록 성공 후 `MemberRegistered` 이벤트를 Outbox에 기록하고 알림 소비자가 Inbox 중복 방지 후 알림 요청을 만든다.

## 구현 위치와 책임 경계

`com.cpf.education.edu.dev07.messaging` 아래 producer/outbox/consumer/inbox/recovery로 분리한다. Starter 내부에 교육 Consumer를 두지 않는다.

- Resource·Fixture: Topic·Schema·Consumer Group·Failure Fixture와 Seed를 `edu/dev07`에 둔다.
- 교육 ID는 Package, Test Class, Catalog, Log, 매뉴얼에서 `EDU-DEV-07`로 동일하게 사용한다.
- 예제 때문에 Runtime Owner Module에 고객 업무 상태·Table·Controller를 추가하지 않는다.
- 기존 Public API·SPI·Owner Port가 있으면 그것을 사용한다. 없으면 Owner Module에 재사용 가능한 Extension을 먼저 구현하고 교육 예제는 그 Extension의 Consumer가 된다.

## 입력·계약

`POST /edu/members`; Event에는 eventId, aggregateId, aggregateVersion, occurredAt, correlationId, payloadVersion 포함.

## 상태와 저장 근거

Outbox NEW→PUBLISHED, Inbox RECEIVED→APPLIED|FAILED, DLQ/Retry 상태를 구분한다.

업무 ID, Operation ID, 요청 Hash, Version, Actor, Reason, Correlation ID를 적용 가능한 범위에서 같은 처리 근거로 연결한다.

## 정상 처리 순서

1. 회원과 Outbox를 같은 DB Transaction에 저장한다.
2. Publisher가 Claim·Lease로 Outbox를 발행한다.
3. Consumer가 Inbox eventId를 먼저 확인한다.
4. 업무 반영과 Inbox APPLIED를 같은 Transaction으로 처리한다.
5. 실패 Event를 Retry/DLQ 후 재처리한다.

## 예외·장애·경계 사례

| 사례 | 구현·검증 요구 |
|---|---|
| 같은 Event 두 번 전달 | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |
| Publisher 중단 | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |
| Consumer Commit 전 종료 | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |
| Kafka Ack 유실 | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |
| 순서 역전 | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |
| Schema Version 불일치 | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |
| Poison Message | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |
| DLQ 재처리 중 중복 | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |

## 필수 Test

- Duplicate Event 10회 1회 반영
- Publisher/Consumer 재시작
- Outbox와 업무 저장 원자성
- Inbox와 업무 반영 원자성
- DLQ 재처리와 감사
- Topic/Group 설정 검증

## 운영·화면 확인

ADM 메시지·Outbox·Inbox·DLQ 메뉴에서 eventId와 aggregateId로 추적한다.

- Log·Metric·Trace·Audit에서 같은 업무 ID와 Operation ID를 검색한다.
- 화면의 Field·Button·상태·오류 메시지가 매뉴얼 설명과 일치해야 한다.
- 응답 유실 또는 부분 실패가 있는 요청은 새 요청 전에 Reconcile 경로를 제공한다.

## 완료 조건

- 업무 저장 성공·메시지 유실 조합이 없다.
- 중복 Event가 중복 Side Effect를 만들지 않는다.
- 재처리 전후 상태와 감사가 연결된다.
- 실행 명령, 입력값, 기대 결과, 장애 재현, 복구 명령을 매뉴얼의 같은 EDU ID에 반영한다.
- 직접 실행하지 않은 DB Vendor·Browser·다중 인스턴스·장애 Test를 성공으로 기록하지 않는다.

## 금지 사항

- 메모리 Map과 고정 JSON만 반환하는 예제로 종료
- 정상 Case 한 건만 Test
- 고객 업무 DB를 ADM·BZA·Gateway가 직접 수정
- 교육 Profile을 기본 Profile에서 자동 활성화
- 비밀번호·Token·개인정보 원문을 Source·Log·Fixture에 기록
- 기존 Package Convention과 다른 별도 `sample`, `demo`, `misc` Root 생성

---

# 13. EDU-DEV-08 — 파일 업로드·검사·첨부·다운로드

| 항목 | 내용 |
|---|---|
| 영역 | 온라인·연계 개발 |
| 우선순위 | P1 |
| 연결 매뉴얼 | 01 개발자 매뉴얼 10장 |
| 구현 단위 | 하나의 독립 Pull Request 또는 상호 원자적인 작업 묶음 |

## 필요 근거

고객 파일 기능은 크기·확장자·Hash·바이러스 검사·부분 업로드·권한·감사까지 포함해야 한다.

## 고객 업무 시나리오

고객이 계약 증빙 PDF를 업로드하고 검사 통과 후 계약에 첨부한다. 운영자는 권한 범위 안에서 다운로드한다.

## 구현 위치와 책임 경계

`com.cpf.education.edu.dev08.attachment` 아래 metadata/storage/scanner/link/download로 분리한다.

- Resource·Fixture: 정상 PDF, 금지 확장자, 손상 파일, 큰 파일, 중단 Upload Fixture를 `edu/dev08`에 둔다. 민감정보 없는 Test File만 사용한다.
- 교육 ID는 Package, Test Class, Catalog, Log, 매뉴얼에서 `EDU-DEV-08`로 동일하게 사용한다.
- 예제 때문에 Runtime Owner Module에 고객 업무 상태·Table·Controller를 추가하지 않는다.
- 기존 Public API·SPI·Owner Port가 있으면 그것을 사용한다. 없으면 Owner Module에 재사용 가능한 Extension을 먼저 구현하고 교육 예제는 그 Extension의 Consumer가 된다.

## 입력·계약

업로드 시작·Chunk 전송·완료, 첨부 연결, Download Token 또는 권한 검증 API. attachmentId, hash, size, mediaType, ownerType/Id 포함.

## 상태와 저장 근거

UPLOADING→UPLOADED→SCANNING→AVAILABLE|REJECTED|FAILED→EXPIRED/DELETED.

업무 ID, Operation ID, 요청 Hash, Version, Actor, Reason, Correlation ID를 적용 가능한 범위에서 같은 처리 근거로 연결한다.

## 정상 처리 순서

1. 업로드 전 크기·형식 정책 검사
2. Stream 저장과 Hash 계산
3. 검사 완료 전 업무 연결 금지
4. 권한·Data Scope 확인 후 첨부 연결
5. Download 시 감사와 만료 정책 기록

## 예외·장애·경계 사례

| 사례 | 구현·검증 요구 |
|---|---|
| 허용하지 않는 확장자 | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |
| MIME 위조 | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |
| Chunk 누락 | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |
| 업로드 중단 | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |
| Hash 불일치 | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |
| 검사 실패/Timeout | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |
| Storage 장애 | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |
| 권한 밖 Download | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |
| 만료 파일 | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |

## 필수 Test

- 대용량 Streaming
- 중단 후 Resume 또는 정리
- Hash 검증
- 검사 상태별 접근 제한
- 동시 첨부 연결
- 다운로드 감사·Masking

## 운영·화면 확인

ADM File Jobs·Downloads·Audit에서 attachmentId와 operationId를 확인한다.

- Log·Metric·Trace·Audit에서 같은 업무 ID와 Operation ID를 검색한다.
- 화면의 Field·Button·상태·오류 메시지가 매뉴얼 설명과 일치해야 한다.
- 응답 유실 또는 부분 실패가 있는 요청은 새 요청 전에 Reconcile 경로를 제공한다.

## 완료 조건

- 검사 전 파일을 다운로드할 수 없다.
- DB Metadata와 Storage Object Hash가 일치한다.
- 실패 Upload가 고아 Object로 남지 않는다.
- 실행 명령, 입력값, 기대 결과, 장애 재현, 복구 명령을 매뉴얼의 같은 EDU ID에 반영한다.
- 직접 실행하지 않은 DB Vendor·Browser·다중 인스턴스·장애 Test를 성공으로 기록하지 않는다.

## 금지 사항

- 메모리 Map과 고정 JSON만 반환하는 예제로 종료
- 정상 Case 한 건만 Test
- 고객 업무 DB를 ADM·BZA·Gateway가 직접 수정
- 교육 Profile을 기본 Profile에서 자동 활성화
- 비밀번호·Token·개인정보 원문을 Source·Log·Fixture에 기록
- 기존 Package Convention과 다른 별도 `sample`, `demo`, `misc` Root 생성

---

# 14. EDU-DEV-09 — 외부 REST 신용조회와 결과 미확정

| 항목 | 내용 |
|---|---|
| 영역 | 온라인·연계 개발 |
| 우선순위 | P1 |
| 연결 매뉴얼 | 01 개발자 매뉴얼 11장 |
| 구현 단위 | 하나의 독립 Pull Request 또는 상호 원자적인 작업 묶음 |

## 필요 근거

외부 연계는 연결 실패보다 요청 전송 후 Timeout이 더 위험하며 상태 조회·멱등성·보상이 필요하다.

## 고객 업무 시나리오

고객 신용조회 요청을 외부 기관에 전송한다. 외부가 처리했지만 응답이 유실될 수 있으므로 기관 거래번호로 결과를 조회한다.

## 구현 위치와 책임 경계

`com.cpf.education.edu.dev09.externalrest` 아래 client/contract/attempt/reconcile/stub를 분리한다.

- Resource·Fixture: 모의 기관 Server, 지연·5xx·Malformed JSON·응답 유실 Scenario를 `edu/dev09`에 둔다.
- 교육 ID는 Package, Test Class, Catalog, Log, 매뉴얼에서 `EDU-DEV-09`로 동일하게 사용한다.
- 예제 때문에 Runtime Owner Module에 고객 업무 상태·Table·Controller를 추가하지 않는다.
- 기존 Public API·SPI·Owner Port가 있으면 그것을 사용한다. 없으면 Owner Module에 재사용 가능한 Extension을 먼저 구현하고 교육 예제는 그 Extension의 Consumer가 된다.

## 입력·계약

`POST /edu/credit-checks`; 외부 create/status API Adapter. internal operationId와 institutionRequestId를 연결한다.

## 상태와 저장 근거

REQUESTED→SENDING→SENT→SUCCEEDED|FAILED|UNKNOWN_RESULT→RECONCILED.

업무 ID, Operation ID, 요청 Hash, Version, Actor, Reason, Correlation ID를 적용 가능한 범위에서 같은 처리 근거로 연결한다.

## 정상 처리 순서

1. 요청 전 Validation·Idempotency 저장
2. 시간 예산과 Header 전달
3. 상대 거래번호·응답 Hash 저장
4. Timeout 시 상태 조회 가능 여부 판단
5. 상태 조회 또는 업무 대사 후 최종 확정

## 예외·장애·경계 사례

| 사례 | 구현·검증 요구 |
|---|---|
| DNS/연결 실패 | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |
| Connect Timeout | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |
| Read Timeout | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |
| 5xx | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |
| 429 | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |
| Malformed JSON | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |
| 응답 Signature 오류 | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |
| 요청 전송 후 Timeout | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |
| 상태 조회도 Timeout | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |
| 상대 중복 응답 | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |

## 필수 Test

- 재시도 허용 단계·금지 단계
- 외부 Stub Failure Matrix
- 응답 유실 후 새 요청 없음
- 상태 조회 대사
- Circuit Breaker
- 비밀 Header 로그 미노출

## 운영·화면 확인

ADM 외부 연계·거래 추적·Recovery에서 operationId와 institutionRequestId를 확인한다.

- Log·Metric·Trace·Audit에서 같은 업무 ID와 Operation ID를 검색한다.
- 화면의 Field·Button·상태·오류 메시지가 매뉴얼 설명과 일치해야 한다.
- 응답 유실 또는 부분 실패가 있는 요청은 새 요청 전에 Reconcile 경로를 제공한다.

## 완료 조건

- 전송 후 Timeout을 단순 실패로 재요청하지 않는다.
- 최종 상태 근거를 보관한다.
- 외부 오류가 표준 고객 오류로 변환된다.
- 실행 명령, 입력값, 기대 결과, 장애 재현, 복구 명령을 매뉴얼의 같은 EDU ID에 반영한다.
- 직접 실행하지 않은 DB Vendor·Browser·다중 인스턴스·장애 Test를 성공으로 기록하지 않는다.

## 금지 사항

- 메모리 Map과 고정 JSON만 반환하는 예제로 종료
- 정상 Case 한 건만 Test
- 고객 업무 DB를 ADM·BZA·Gateway가 직접 수정
- 교육 Profile을 기본 Profile에서 자동 활성화
- 비밀번호·Token·개인정보 원문을 Source·Log·Fixture에 기록
- 기존 Package Convention과 다른 별도 `sample`, `demo`, `misc` Root 생성

---

# 15. EDU-DEV-10 — 고정길이 전문 기관 이체

| 항목 | 내용 |
|---|---|
| 영역 | 온라인·연계 개발 |
| 우선순위 | P1 |
| 연결 매뉴얼 | 01 개발자 매뉴얼 12장 |
| 구현 단위 | 하나의 독립 Pull Request 또는 상호 원자적인 작업 묶음 |

## 필요 근거

전문 연계는 길이·인코딩·필드 위치·거래번호·응답 대사 오류가 많아 실제 Byte 기반 예제가 필요하다.

## 고객 업무 시나리오

고객 이체 요청을 EUC-KR 고정길이 전문으로 기관에 전송하고 응답 전문으로 결과를 확정한다.

## 구현 위치와 책임 경계

`com.cpf.education.edu.dev10.fixedwidth` 아래 layout/codec/client/attempt/reconcile을 분리한다.

- Resource·Fixture: 전문 Layout 정의, 정상·잘못된 길이·잘못된 인코딩 Byte Fixture를 `edu/dev10`에 둔다.
- 교육 ID는 Package, Test Class, Catalog, Log, 매뉴얼에서 `EDU-DEV-10`로 동일하게 사용한다.
- 예제 때문에 Runtime Owner Module에 고객 업무 상태·Table·Controller를 추가하지 않는다.
- 기존 Public API·SPI·Owner Port가 있으면 그것을 사용한다. 없으면 Owner Module에 재사용 가능한 Extension을 먼저 구현하고 교육 예제는 그 Extension의 Consumer가 된다.

## 입력·계약

`POST /edu/institution-transfers`; 전문 Header/Body/Trailer와 institutionTransactionId를 명시한다.

## 상태와 저장 근거

CREATED→ENCODED→SENT→ACKNOWLEDGED|REJECTED|UNKNOWN_RESULT→RECONCILED.

업무 ID, Operation ID, 요청 Hash, Version, Actor, Reason, Correlation ID를 적용 가능한 범위에서 같은 처리 근거로 연결한다.

## 정상 처리 순서

1. Field 길이·Padding·Encoding Validation
2. Byte Length 기준 Encode
3. 전문 Hash와 원문 Masked View 저장
4. 응답 Code Mapping
5. Timeout 시 기관 거래번호로 조회

## 예외·장애·경계 사례

| 사례 | 구현·검증 요구 |
|---|---|
| 문자 Byte 길이 초과 | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |
| 잘못된 Encoding | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |
| 전문 길이 부족/초과 | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |
| Header 거래번호 불일치 | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |
| 응답 Code 미등록 | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |
| 지연 응답 | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |
| 부분 수신 | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |
| 중복 응답 | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |

## 필수 Test

- Golden Byte 비교
- 한글 Byte Length
- Layout Version 호환
- 응답 Code Mapping
- 전송 후 Timeout 대사
- 원문 Masking

## 운영·화면 확인

ADM 거래 구간·외부 연계에서 전문 Version, 기관 거래번호, Masked 전문을 확인한다.

- Log·Metric·Trace·Audit에서 같은 업무 ID와 Operation ID를 검색한다.
- 화면의 Field·Button·상태·오류 메시지가 매뉴얼 설명과 일치해야 한다.
- 응답 유실 또는 부분 실패가 있는 요청은 새 요청 전에 Reconcile 경로를 제공한다.

## 완료 조건

- 전문 Byte가 Layout과 일치한다.
- 원문 Secret·개인정보가 로그에 남지 않는다.
- UNKNOWN_RESULT를 기관 조회로 해소한다.
- 실행 명령, 입력값, 기대 결과, 장애 재현, 복구 명령을 매뉴얼의 같은 EDU ID에 반영한다.
- 직접 실행하지 않은 DB Vendor·Browser·다중 인스턴스·장애 Test를 성공으로 기록하지 않는다.

## 금지 사항

- 메모리 Map과 고정 JSON만 반환하는 예제로 종료
- 정상 Case 한 건만 Test
- 고객 업무 DB를 ADM·BZA·Gateway가 직접 수정
- 교육 Profile을 기본 Profile에서 자동 활성화
- 비밀번호·Token·개인정보 원문을 Source·Log·Fixture에 기록
- 기존 Package Convention과 다른 별도 `sample`, `demo`, `misc` Root 생성

---

# 16. EDU-DEV-11 — 권한·데이터 범위·개인정보 가림·감사

| 항목 | 내용 |
|---|---|
| 영역 | 온라인·연계 개발 |
| 우선순위 | P1 |
| 연결 매뉴얼 | 01 개발자 매뉴얼 13장 |
| 구현 단위 | 하나의 독립 Pull Request 또는 상호 원자적인 작업 묶음 |

## 필요 근거

화면 숨김만으로 권한을 구현하면 API 직접 호출과 조직 범위 우회가 가능하다.

## 고객 업무 시나리오

지점 상담자는 자기 지점 고객의 가려진 전화번호만 보고, 개인정보 원문 조회 권한자는 사유를 입력해 한 건만 원문 조회한다.

## 구현 위치와 책임 경계

`com.cpf.education.edu.dev11.security` 아래 authorization/datascope/masking/audit를 분리한다. 공통 재사용은 기존 Security Starter Public Extension 사용.

- Resource·Fixture: 역할·조직·고객 Fixture와 Masking Rule을 `edu/dev11`에 둔다.
- 교육 ID는 Package, Test Class, Catalog, Log, 매뉴얼에서 `EDU-DEV-11`로 동일하게 사용한다.
- 예제 때문에 Runtime Owner Module에 고객 업무 상태·Table·Controller를 추가하지 않는다.
- 기존 Public API·SPI·Owner Port가 있으면 그것을 사용한다. 없으면 Owner Module에 재사용 가능한 Extension을 먼저 구현하고 교육 예제는 그 Extension의 Consumer가 된다.

## 입력·계약

목록·상세 API와 별도 원문 조회 API. 사용자·역할·조직 Context는 인증 문맥에서 받고 Client 입력을 신뢰하지 않는다.

## 상태와 저장 근거

일반 조회는 MASKED. 원문 조회는 권한·사유·대상·감사를 만족할 때만 REVEALED.

업무 ID, Operation ID, 요청 Hash, Version, Actor, Reason, Correlation ID를 적용 가능한 범위에서 같은 처리 근거로 연결한다.

## 정상 처리 순서

1. 서버에서 Permission 검사
2. Data Scope를 Query에 강제
3. 기본 응답 Masking
4. 원문 조회는 별도 Endpoint·사유·감사
5. Export와 Log에도 같은 Masking 적용

## 예외·장애·경계 사례

| 사례 | 구현·검증 요구 |
|---|---|
| URL 직접 호출 | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |
| 다른 조직 ID 변조 | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |
| 권한 없음 | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |
| 사유 누락 | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |
| 승인 만료 | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |
| Audit 저장 실패 | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |
| Export 우회 | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |
| Cache에 원문 저장 | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |

## 필수 Test

- 역할×조직×기능 Matrix
- API 직접 호출
- 원문 조회 감사
- 로그·Trace·Error Body 개인정보 검사
- Export Masking
- 세션 변경 후 권한 반영

## 운영·화면 확인

BZA 실효 권한과 ADM 감사에서 권한 근거·사유·대상·결과를 확인한다.

- Log·Metric·Trace·Audit에서 같은 업무 ID와 Operation ID를 검색한다.
- 화면의 Field·Button·상태·오류 메시지가 매뉴얼 설명과 일치해야 한다.
- 응답 유실 또는 부분 실패가 있는 요청은 새 요청 전에 Reconcile 경로를 제공한다.

## 완료 조건

- 권한 밖 원문 노출 0건
- 화면과 API가 같은 권한 의미
- 감사 실패 시 원문 조회 성공 금지
- 실행 명령, 입력값, 기대 결과, 장애 재현, 복구 명령을 매뉴얼의 같은 EDU ID에 반영한다.
- 직접 실행하지 않은 DB Vendor·Browser·다중 인스턴스·장애 Test를 성공으로 기록하지 않는다.

## 금지 사항

- 메모리 Map과 고정 JSON만 반환하는 예제로 종료
- 정상 Case 한 건만 Test
- 고객 업무 DB를 ADM·BZA·Gateway가 직접 수정
- 교육 Profile을 기본 Profile에서 자동 활성화
- 비밀번호·Token·개인정보 원문을 Source·Log·Fixture에 기록
- 기존 Package Convention과 다른 별도 `sample`, `demo`, `misc` Root 생성

---

# 17. EDU-DEV-12 — Cache·기능 전환·Secret 교체

| 항목 | 내용 |
|---|---|
| 영역 | 온라인·연계 개발 |
| 우선순위 | P1 |
| 연결 매뉴얼 | 01 개발자 매뉴얼 14장 |
| 구현 단위 | 하나의 독립 Pull Request 또는 상호 원자적인 작업 묶음 |

## 필요 근거

부가 기능을 각각 따로 보여주면 Cache 장애, Flag 변경, Secret Rotation이 실제 업무에 미치는 영향을 이해하기 어렵다.

## 고객 업무 시나리오

공통 코드 조회를 Cache하고 특정 조직에만 새 할인 정책을 활성화하며 외부 API Key를 교체한다.

## 구현 위치와 책임 경계

`com.cpf.education.edu.dev12.runtimefeature` 아래 cache/featureflag/secret/client를 분리한다.

- Resource·Fixture: Cache TTL·Flag 대상·Secret Alias 설정을 `edu/dev12`에 둔다. Secret 원문은 환경에서 주입한다.
- 교육 ID는 Package, Test Class, Catalog, Log, 매뉴얼에서 `EDU-DEV-12`로 동일하게 사용한다.
- 예제 때문에 Runtime Owner Module에 고객 업무 상태·Table·Controller를 추가하지 않는다.
- 기존 Public API·SPI·Owner Port가 있으면 그것을 사용한다. 없으면 Owner Module에 재사용 가능한 Extension을 먼저 구현하고 교육 예제는 그 Extension의 Consumer가 된다.

## 입력·계약

`GET /edu/codes/{group}`, `POST /edu/discounts/quote`; Flag Context는 사용자·조직·환경을 사용한다.

## 상태와 저장 근거

Cache HIT/MISS/STALE/BYPASS, Flag ON/OFF, Secret ACTIVE/ROTATING/FAILED.

업무 ID, Operation ID, 요청 Hash, Version, Actor, Reason, Correlation ID를 적용 가능한 범위에서 같은 처리 근거로 연결한다.

## 정상 처리 순서

1. 원본 조회 후 Cache 저장
2. TTL·Version·Eviction 정의
3. Flag 미대상은 기존 로직
4. Secret Alias로 값 조회
5. Rotation 후 연결 재검증

## 예외·장애·경계 사례

| 사례 | 구현·검증 요구 |
|---|---|
| Redis 연결 실패 | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |
| Stale 데이터 | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |
| 직렬화 불일치 | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |
| Flag Service 장애 | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |
| 잘못된 대상 규칙 | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |
| Secret 누락 | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |
| Secret 교체 후 인증 실패 | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |
| Secret 로그 노출 | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |

## 필수 Test

- Cache Hit/Miss/TTL
- 장애 시 Fallback 정책
- Flag 대상 분리
- Secret Rotation 전후
- 민감정보 Scan
- 다중 인스턴스 Cache 무효화

## 운영·화면 확인

ADM Cache·Config·Secrets·Feature 상태에서 대상·Version·오류를 확인한다.

- Log·Metric·Trace·Audit에서 같은 업무 ID와 Operation ID를 검색한다.
- 화면의 Field·Button·상태·오류 메시지가 매뉴얼 설명과 일치해야 한다.
- 응답 유실 또는 부분 실패가 있는 요청은 새 요청 전에 Reconcile 경로를 제공한다.

## 완료 조건

- Cache 장애가 정의되지 않은 데이터 오류를 만들지 않는다.
- Flag 미대상 결과가 바뀌지 않는다.
- Secret 원문이 Source·Log·응답에 없다.
- 실행 명령, 입력값, 기대 결과, 장애 재현, 복구 명령을 매뉴얼의 같은 EDU ID에 반영한다.
- 직접 실행하지 않은 DB Vendor·Browser·다중 인스턴스·장애 Test를 성공으로 기록하지 않는다.

## 금지 사항

- 메모리 Map과 고정 JSON만 반환하는 예제로 종료
- 정상 Case 한 건만 Test
- 고객 업무 DB를 ADM·BZA·Gateway가 직접 수정
- 교육 Profile을 기본 Profile에서 자동 활성화
- 비밀번호·Token·개인정보 원문을 Source·Log·Fixture에 기록
- 기존 Package Convention과 다른 별도 `sample`, `demo`, `misc` Root 생성

---

# 18. EDU-DEV-13 — 알림·비동기 내보내기·다운로드 감사

| 항목 | 내용 |
|---|---|
| 영역 | 온라인·연계 개발 |
| 우선순위 | P2 |
| 연결 매뉴얼 | 01 개발자 매뉴얼 15장 |
| 구현 단위 | 하나의 독립 Pull Request 또는 상호 원자적인 작업 묶음 |

## 필요 근거

대량 내보내기는 동기 응답으로 처리할 수 없고 알림·파일 만료·다운로드 감사가 연결되어야 한다.

## 고객 업무 시나리오

월 정산 결과 50,000건을 CSV로 내보내고 완료 알림 후 권한 있는 사용자가 만료 전 다운로드한다.

## 구현 위치와 책임 경계

`com.cpf.education.edu.dev13.exportnotification` 아래 request/job/file/notification/download를 분리한다.

- Resource·Fixture: CSV Schema, 50k Seed 생성기, 실패 Notification Stub을 `edu/dev13`에 둔다.
- 교육 ID는 Package, Test Class, Catalog, Log, 매뉴얼에서 `EDU-DEV-13`로 동일하게 사용한다.
- 예제 때문에 Runtime Owner Module에 고객 업무 상태·Table·Controller를 추가하지 않는다.
- 기존 Public API·SPI·Owner Port가 있으면 그것을 사용한다. 없으면 Owner Module에 재사용 가능한 Extension을 먼저 구현하고 교육 예제는 그 Extension의 Consumer가 된다.

## 입력·계약

`POST /edu/settlements/{id}/exports`, `GET /edu/exports/{operationId}`, `GET /edu/downloads/{token}`.

## 상태와 저장 근거

REQUESTED→GENERATING→AVAILABLE|FAILED→EXPIRED. Notification PENDING→SENT|FAILED.

업무 ID, Operation ID, 요청 Hash, Version, Actor, Reason, Correlation ID를 적용 가능한 범위에서 같은 처리 근거로 연결한다.

## 정상 처리 순서

1. Export 요청과 Operation 생성
2. Snapshot 기준 데이터 추출
3. Record Count·금액 합계·Hash 기록
4. 파일 저장 후 Download Token 발급
5. 알림 전송
6. Download 권한·만료·감사 확인

## 예외·장애·경계 사례

| 사례 | 구현·검증 요구 |
|---|---|
| 대량 데이터 Memory 초과 | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |
| 생성 중 Process 종료 | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |
| Storage 실패 | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |
| 알림 실패 | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |
| 만료 Token | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |
| 권한 없음 | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |
| 동일 요청 중복 | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |
| 부분 파일 노출 | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |

## 필수 Test

- Streaming Export
- Restart/재생성 정책
- Hash·건수 대사
- 알림 재시도
- Token 만료
- 다운로드 감사
- 개인정보 Masking

## 운영·화면 확인

ADM File Jobs·Notifications·Downloads에서 operationId와 fileHash를 확인한다.

- Log·Metric·Trace·Audit에서 같은 업무 ID와 Operation ID를 검색한다.
- 화면의 Field·Button·상태·오류 메시지가 매뉴얼 설명과 일치해야 한다.
- 응답 유실 또는 부분 실패가 있는 요청은 새 요청 전에 Reconcile 경로를 제공한다.

## 완료 조건

- 완료 전 파일 접근 불가
- 건수·합계·Hash 일치
- 알림 실패와 Export 실패를 구분
- 실행 명령, 입력값, 기대 결과, 장애 재현, 복구 명령을 매뉴얼의 같은 EDU ID에 반영한다.
- 직접 실행하지 않은 DB Vendor·Browser·다중 인스턴스·장애 Test를 성공으로 기록하지 않는다.

## 금지 사항

- 메모리 Map과 고정 JSON만 반환하는 예제로 종료
- 정상 Case 한 건만 Test
- 고객 업무 DB를 ADM·BZA·Gateway가 직접 수정
- 교육 Profile을 기본 Profile에서 자동 활성화
- 비밀번호·Token·개인정보 원문을 Source·Log·Fixture에 기록
- 기존 Package Convention과 다른 별도 `sample`, `demo`, `misc` Root 생성

---

# 19. EDU-DEV-14 — Oracle·PostgreSQL·MariaDB 동일 의미 Migration

| 항목 | 내용 |
|---|---|
| 영역 | 온라인·연계 개발 |
| 우선순위 | P1 |
| 연결 매뉴얼 | 01 개발자 매뉴얼 16장 |
| 구현 단위 | 하나의 독립 Pull Request 또는 상호 원자적인 작업 묶음 |

## 필요 근거

고객이 지원 DB를 바꿔도 Schema 의미와 Upgrade·Rollback 절차가 달라지지 않아야 한다.

## 고객 업무 시나리오

회원 등급 Column과 Index를 추가하고 기존 회원을 Backfill한다.

## 구현 위치와 책임 경계

Java Query·Mapping은 `com.cpf.education.edu.dev14.dbmigration`; SQL은 중앙 `cpf-tools/db/vendor/<vendor>`의 기존 Install/Upgrade/Rollback/Verify 구조에 둔다.

- Resource·Fixture: Vendor별 Before/After Fixture와 대사 Query를 `edu/dev14`에 둔다.
- 교육 ID는 Package, Test Class, Catalog, Log, 매뉴얼에서 `EDU-DEV-14`로 동일하게 사용한다.
- 예제 때문에 Runtime Owner Module에 고객 업무 상태·Table·Controller를 추가하지 않는다.
- 기존 Public API·SPI·Owner Port가 있으면 그것을 사용한다. 없으면 Owner Module에 재사용 가능한 Extension을 먼저 구현하고 교육 예제는 그 Extension의 Consumer가 된다.

## 입력·계약

회원 조회·변경 API가 새 grade 필드를 사용한다. Migration Version과 적용 상태 확인 명령을 제공한다.

## 상태와 저장 근거

NOT_APPLIED→APPLYING→APPLIED→VERIFIED 또는 FAILED/ROLLED_BACK.

업무 ID, Operation ID, 요청 Hash, Version, Actor, Reason, Correlation ID를 적용 가능한 범위에서 같은 처리 근거로 연결한다.

## 정상 처리 순서

1. 신규 설치 Script 갱신
2. Upgrade Script 작성
3. 기존 데이터 Backfill
4. Constraint·Index 적용
5. Verify Query
6. Rollback 가능 범위와 데이터 보존 정책 작성

## 예외·장애·경계 사례

| 사례 | 구현·검증 요구 |
|---|---|
| 중간 중단 | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |
| 재실행 | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |
| 기존 Null/불량 데이터 | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |
| Lock Timeout | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |
| Index 생성 실패 | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |
| Rollback 후 App 호환 | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |
| Vendor 문법 차이 | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |

## 필수 Test

- 세 Vendor 신규 설치
- 이전 Version→Upgrade
- Upgrade 재실행
- 중단 후 재개
- Rollback·Forward Fix
- Schema Diff와 Query 결과 동일성

## 운영·화면 확인

플랫폼 운영 매뉴얼의 DB Lifecycle과 Drift 확인 명령을 갱신한다.

- Log·Metric·Trace·Audit에서 같은 업무 ID와 Operation ID를 검색한다.
- 화면의 Field·Button·상태·오류 메시지가 매뉴얼 설명과 일치해야 한다.
- 응답 유실 또는 부분 실패가 있는 요청은 새 요청 전에 Reconcile 경로를 제공한다.

## 완료 조건

- 세 Vendor의 Column·Default·Constraint 의미 동일
- Module-local SQL 없음
- 실행하지 않은 Vendor를 성공으로 기록하지 않음
- 실행 명령, 입력값, 기대 결과, 장애 재현, 복구 명령을 매뉴얼의 같은 EDU ID에 반영한다.
- 직접 실행하지 않은 DB Vendor·Browser·다중 인스턴스·장애 Test를 성공으로 기록하지 않는다.

## 금지 사항

- 메모리 Map과 고정 JSON만 반환하는 예제로 종료
- 정상 Case 한 건만 Test
- 고객 업무 DB를 ADM·BZA·Gateway가 직접 수정
- 교육 Profile을 기본 Profile에서 자동 활성화
- 비밀번호·Token·개인정보 원문을 Source·Log·Fixture에 기록
- 기존 Package Convention과 다른 별도 `sample`, `demo`, `misc` Root 생성

---

# 20. EDU-DEV-15 — 지급 업무 장애 주입·복구·운영 인계

| 항목 | 내용 |
|---|---|
| 영역 | 온라인·연계 개발 |
| 우선순위 | P1 |
| 연결 매뉴얼 | 01 개발자 매뉴얼 17장 |
| 구현 단위 | 하나의 독립 Pull Request 또는 상호 원자적인 작업 묶음 |

## 필요 근거

개별 기능 Test만으로는 Transaction 경계 전후 장애와 운영 복구를 한 흐름으로 검증할 수 없다.

## 고객 업무 시나리오

지급 요청에 DB·Kafka·외부기관·응답 송신 장애를 순서대로 주입하고 ADM에서 결과를 복구한다.

## 구현 위치와 책임 경계

`com.cpf.education.edu.dev15.faultrecovery` 아래 scenario/failurepoint/reconcile/evidence를 둔다. Toxiproxy와 기존 Test Tool을 사용한다.

- Resource·Fixture: Failure Matrix, 예상 상태·DB·Event·Audit Evidence를 `edu/dev15`에 둔다.
- 교육 ID는 Package, Test Class, Catalog, Log, 매뉴얼에서 `EDU-DEV-15`로 동일하게 사용한다.
- 예제 때문에 Runtime Owner Module에 고객 업무 상태·Table·Controller를 추가하지 않는다.
- 기존 Public API·SPI·Owner Port가 있으면 그것을 사용한다. 없으면 Owner Module에 재사용 가능한 Extension을 먼저 구현하고 교육 예제는 그 Extension의 Consumer가 된다.

## 입력·계약

지급 API와 Failure Point 제어는 Test Profile에서만 활성화한다. 운영 Profile에 장애 주입 Endpoint를 노출하지 않는다.

## 상태와 저장 근거

각 Failure Point에서 업무·Operation·Outbox·External Attempt 상태를 명시한다.

업무 ID, Operation ID, 요청 Hash, Version, Actor, Reason, Correlation ID를 적용 가능한 범위에서 같은 처리 근거로 연결한다.

## 정상 처리 순서

1. 정상 Baseline 실행
2. DB Commit 전 장애
3. DB Commit 후 Kafka 발행 전 장애
4. 외부 전송 전/후 장애
5. 응답 송신 전 장애
6. Recovery Center 대사
7. 최종 증적 생성

## 예외·장애·경계 사례

| 사례 | 구현·검증 요구 |
|---|---|
| Process Kill | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |
| DB Proxy 차단 | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |
| Kafka Proxy 차단 | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |
| 외부 지연/Reset | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |
| Disk Full 모의 | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |
| 응답 Drop | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |
| 다중 인스턴스 Lease 상실 | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |

## 필수 Test

- Scenario 자동 반복
- 중복 Side Effect 검사
- Recovery 전후 상태
- Log·Metric·Trace·Audit Evidence
- 재실행 시 동일 결과
- 운영 Runbook 명령 검증

## 운영·화면 확인

ADM Incident·Recovery Center·Transaction Group에서 같은 businessId/operationId를 따라간다.

- Log·Metric·Trace·Audit에서 같은 업무 ID와 Operation ID를 검색한다.
- 화면의 Field·Button·상태·오류 메시지가 매뉴얼 설명과 일치해야 한다.
- 응답 유실 또는 부분 실패가 있는 요청은 새 요청 전에 Reconcile 경로를 제공한다.

## 완료 조건

- Failure Point별 예상 상태표와 실제 일치
- 복구 후 원장·메시지·외부 결과 대사
- 운영 인계표만으로 재현 가능
- 실행 명령, 입력값, 기대 결과, 장애 재현, 복구 명령을 매뉴얼의 같은 EDU ID에 반영한다.
- 직접 실행하지 않은 DB Vendor·Browser·다중 인스턴스·장애 Test를 성공으로 기록하지 않는다.

## 금지 사항

- 메모리 Map과 고정 JSON만 반환하는 예제로 종료
- 정상 Case 한 건만 Test
- 고객 업무 DB를 ADM·BZA·Gateway가 직접 수정
- 교육 Profile을 기본 Profile에서 자동 활성화
- 비밀번호·Token·개인정보 원문을 Source·Log·Fixture에 기록
- 기존 Package Convention과 다른 별도 `sample`, `demo`, `misc` Root 생성

---

# 21. EDU-BAT-01 — 업무일 마감 Tasklet

| 항목 | 내용 |
|---|---|
| 영역 | 배치 |
| 우선순위 | P1 |
| 연결 매뉴얼 | 02 배치 개발 매뉴얼 |
| 구현 단위 | 하나의 독립 Pull Request 또는 상호 원자적인 작업 묶음 |

## 필요 근거

고객 배치 개발자가 Spring Batch 실행과 CPF 운영·복구 기능을 한 예제로 확인해야 한다.

## 고객 업무 시나리오

업무일자 마감 Flag와 마감 이력을 한 번만 기록한다.

## 구현 위치와 책임 경계

표준 Source `cpf-education/src/main/java/com/cpf/reference/edu/bat01/tasklet/`. 고객 Job을 `cpf-batch` Runtime에 넣지 않는다.

- Resource·Fixture: Job Fixture·Parameter·대사 Query는 `cpf-education/src/main/resources/edu/edu/bat/01/`와 중앙 Vendor Pack에 둔다.
- 교육 ID는 Package, Test Class, Catalog, Log, 매뉴얼에서 `EDU-BAT-01`로 동일하게 사용한다.
- 예제 때문에 Runtime Owner Module에 고객 업무 상태·Table·Controller를 추가하지 않는다.
- 기존 Public API·SPI·Owner Port가 있으면 그것을 사용한다. 없으면 Owner Module에 재사용 가능한 Extension을 먼저 구현하고 교육 예제는 그 Extension의 Consumer가 된다.

## 입력·계약

Job Parameter: businessDate, force, reason. Job/Step 이름과 Version에 `EDU-BAT-01` 식별자를 포함한다.

## 상태와 저장 근거

READY→RUNNING→COMPLETED|FAILED

업무 ID, Operation ID, 요청 Hash, Version, Actor, Reason, Correlation ID를 적용 가능한 범위에서 같은 처리 근거로 연결한다.

## 정상 처리 순서

1. JobParameter를 식별·비식별 값으로 분리한다.
2. Job·Step·Reader/Processor/Writer 또는 Tasklet을 역할별로 분리한다.
3. Preview·Checkpoint·Commit·대사 기준을 저장한다.
4. ADM 등록·실행·진행·중지·재시작 화면과 연결한다.

## 예외·장애·경계 사례

| 사례 | 구현·검증 요구 |
|---|---|
| 중복 Trigger | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |
| DB 실패 | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |
| Commit 후 응답 유실 | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |

## 필수 Test

- 정상 실행
- 중간 실패·Process 종료
- 중복 Trigger
- Stop·Restart
- 업무 건수·금액·Hash 대사
- ADM Execution ID 연결

## 운영·화면 확인

ADM Batch Overview·Executions·Instances·Recovery·Audit에서 같은 Execution ID와 Parameter Hash를 확인한다.

- Log·Metric·Trace·Audit에서 같은 업무 ID와 Operation ID를 검색한다.
- 화면의 Field·Button·상태·오류 메시지가 매뉴얼 설명과 일치해야 한다.
- 응답 유실 또는 부분 실패가 있는 요청은 새 요청 전에 Reconcile 경로를 제공한다.

## 완료 조건

- 마감 이력 1건, 재실행 중복 없음
- JobRepository 결과와 업무 원장 합계가 일치한다.
- 실패 후 새 실행·Restart·Reprocess 선택 근거가 문서화된다.
- 실행 명령, 입력값, 기대 결과, 장애 재현, 복구 명령을 매뉴얼의 같은 EDU ID에 반영한다.
- 직접 실행하지 않은 DB Vendor·Browser·다중 인스턴스·장애 Test를 성공으로 기록하지 않는다.

## 금지 사항

- 메모리 Map과 고정 JSON만 반환하는 예제로 종료
- 정상 Case 한 건만 Test
- 고객 업무 DB를 ADM·BZA·Gateway가 직접 수정
- 교육 Profile을 기본 Profile에서 자동 활성화
- 비밀번호·Token·개인정보 원문을 Source·Log·Fixture에 기록
- 기존 Package Convention과 다른 별도 `sample`, `demo`, `misc` Root 생성

---

# 22. EDU-BAT-02 — 회원 등급 10,000건 Chunk

| 항목 | 내용 |
|---|---|
| 영역 | 배치 |
| 우선순위 | P0 |
| 연결 매뉴얼 | 02 배치 개발 매뉴얼 |
| 구현 단위 | 하나의 독립 Pull Request 또는 상호 원자적인 작업 묶음 |

## 필요 근거

고객 배치 개발자가 Spring Batch 실행과 CPF 운영·복구 기능을 한 예제로 확인해야 한다.

## 고객 업무 시나리오

회원 거래실적을 읽어 10,000건 등급을 재계산한다.

## 구현 위치와 책임 경계

표준 Source `cpf-education/src/main/java/com/cpf/reference/edu/bat02/chunk/`. 고객 Job을 `cpf-batch` Runtime에 넣지 않는다.

- Resource·Fixture: Job Fixture·Parameter·대사 Query는 `cpf-education/src/main/resources/edu/edu/bat/02/`와 중앙 Vendor Pack에 둔다.
- 교육 ID는 Package, Test Class, Catalog, Log, 매뉴얼에서 `EDU-BAT-02`로 동일하게 사용한다.
- 예제 때문에 Runtime Owner Module에 고객 업무 상태·Table·Controller를 추가하지 않는다.
- 기존 Public API·SPI·Owner Port가 있으면 그것을 사용한다. 없으면 Owner Module에 재사용 가능한 Extension을 먼저 구현하고 교육 예제는 그 Extension의 Consumer가 된다.

## 입력·계약

Job Parameter: businessDate, criteriaVersion, chunkSize. Job/Step 이름과 Version에 `EDU-BAT-02` 식별자를 포함한다.

## 상태와 저장 근거

STARTING→STARTED→COMPLETED|FAILED|STOPPED

업무 ID, Operation ID, 요청 Hash, Version, Actor, Reason, Correlation ID를 적용 가능한 범위에서 같은 처리 근거로 연결한다.

## 정상 처리 순서

1. JobParameter를 식별·비식별 값으로 분리한다.
2. Job·Step·Reader/Processor/Writer 또는 Tasklet을 역할별로 분리한다.
3. Preview·Checkpoint·Commit·대사 기준을 저장한다.
4. ADM 등록·실행·진행·중지·재시작 화면과 연결한다.

## 예외·장애·경계 사례

| 사례 | 구현·검증 요구 |
|---|---|
| 중간 Process 종료 | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |
| Skip | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |
| Retry | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |
| Writer 실패 | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |

## 필수 Test

- 정상 실행
- 중간 실패·Process 종료
- 중복 Trigger
- Stop·Restart
- 업무 건수·금액·Hash 대사
- ADM Execution ID 연결

## 운영·화면 확인

ADM Batch Overview·Executions·Instances·Recovery·Audit에서 같은 Execution ID와 Parameter Hash를 확인한다.

- Log·Metric·Trace·Audit에서 같은 업무 ID와 Operation ID를 검색한다.
- 화면의 Field·Button·상태·오류 메시지가 매뉴얼 설명과 일치해야 한다.
- 응답 유실 또는 부분 실패가 있는 요청은 새 요청 전에 Reconcile 경로를 제공한다.

## 완료 조건

- Read=10000, Write+Skip=10000, 재시작 중복 없음
- JobRepository 결과와 업무 원장 합계가 일치한다.
- 실패 후 새 실행·Restart·Reprocess 선택 근거가 문서화된다.
- 실행 명령, 입력값, 기대 결과, 장애 재현, 복구 명령을 매뉴얼의 같은 EDU ID에 반영한다.
- 직접 실행하지 않은 DB Vendor·Browser·다중 인스턴스·장애 Test를 성공으로 기록하지 않는다.

## 금지 사항

- 메모리 Map과 고정 JSON만 반환하는 예제로 종료
- 정상 Case 한 건만 Test
- 고객 업무 DB를 ADM·BZA·Gateway가 직접 수정
- 교육 Profile을 기본 Profile에서 자동 활성화
- 비밀번호·Token·개인정보 원문을 Source·Log·Fixture에 기록
- 기존 Package Convention과 다른 별도 `sample`, `demo`, `misc` Root 생성

---

# 23. EDU-BAT-03 — CSV 입출력 배치

| 항목 | 내용 |
|---|---|
| 영역 | 배치 |
| 우선순위 | P1 |
| 연결 매뉴얼 | 02 배치 개발 매뉴얼 |
| 구현 단위 | 하나의 독립 Pull Request 또는 상호 원자적인 작업 묶음 |

## 필요 근거

고객 배치 개발자가 Spring Batch 실행과 CPF 운영·복구 기능을 한 예제로 확인해야 한다.

## 고객 업무 시나리오

Header·1000 Record·Trailer가 있는 CSV를 검증하고 적재한다.

## 구현 위치와 책임 경계

표준 Source `cpf-education/src/main/java/com/cpf/reference/edu/bat03/file/`. 고객 Job을 `cpf-batch` Runtime에 넣지 않는다.

- Resource·Fixture: Job Fixture·Parameter·대사 Query는 `cpf-education/src/main/resources/edu/edu/bat/03/`와 중앙 Vendor Pack에 둔다.
- 교육 ID는 Package, Test Class, Catalog, Log, 매뉴얼에서 `EDU-BAT-03`로 동일하게 사용한다.
- 예제 때문에 Runtime Owner Module에 고객 업무 상태·Table·Controller를 추가하지 않는다.
- 기존 Public API·SPI·Owner Port가 있으면 그것을 사용한다. 없으면 Owner Module에 재사용 가능한 Extension을 먼저 구현하고 교육 예제는 그 Extension의 Consumer가 된다.

## 입력·계약

Job Parameter: fileId, fileHash, businessDate. Job/Step 이름과 Version에 `EDU-BAT-03` 식별자를 포함한다.

## 상태와 저장 근거

RECEIVED→VALIDATED→PROCESSING→COMPLETED|REJECTED

업무 ID, Operation ID, 요청 Hash, Version, Actor, Reason, Correlation ID를 적용 가능한 범위에서 같은 처리 근거로 연결한다.

## 정상 처리 순서

1. JobParameter를 식별·비식별 값으로 분리한다.
2. Job·Step·Reader/Processor/Writer 또는 Tasklet을 역할별로 분리한다.
3. Preview·Checkpoint·Commit·대사 기준을 저장한다.
4. ADM 등록·실행·진행·중지·재시작 화면과 연결한다.

## 예외·장애·경계 사례

| 사례 | 구현·검증 요구 |
|---|---|
| Header 오류 | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |
| Trailer 합계 불일치 | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |
| Encoding 오류 | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |
| 중단 | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |

## 필수 Test

- 정상 실행
- 중간 실패·Process 종료
- 중복 Trigger
- Stop·Restart
- 업무 건수·금액·Hash 대사
- ADM Execution ID 연결

## 운영·화면 확인

ADM Batch Overview·Executions·Instances·Recovery·Audit에서 같은 Execution ID와 Parameter Hash를 확인한다.

- Log·Metric·Trace·Audit에서 같은 업무 ID와 Operation ID를 검색한다.
- 화면의 Field·Button·상태·오류 메시지가 매뉴얼 설명과 일치해야 한다.
- 응답 유실 또는 부분 실패가 있는 요청은 새 요청 전에 Reconcile 경로를 제공한다.

## 완료 조건

- 입력 건수=성공+오류, Hash와 Trailer 합계 일치
- JobRepository 결과와 업무 원장 합계가 일치한다.
- 실패 후 새 실행·Restart·Reprocess 선택 근거가 문서화된다.
- 실행 명령, 입력값, 기대 결과, 장애 재현, 복구 명령을 매뉴얼의 같은 EDU ID에 반영한다.
- 직접 실행하지 않은 DB Vendor·Browser·다중 인스턴스·장애 Test를 성공으로 기록하지 않는다.

## 금지 사항

- 메모리 Map과 고정 JSON만 반환하는 예제로 종료
- 정상 Case 한 건만 Test
- 고객 업무 DB를 ADM·BZA·Gateway가 직접 수정
- 교육 Profile을 기본 Profile에서 자동 활성화
- 비밀번호·Token·개인정보 원문을 Source·Log·Fixture에 기록
- 기존 Package Convention과 다른 별도 `sample`, `demo`, `misc` Root 생성

---

# 24. EDU-BAT-04 — 8개 범위 Partition

| 항목 | 내용 |
|---|---|
| 영역 | 배치 |
| 우선순위 | P1 |
| 연결 매뉴얼 | 02 배치 개발 매뉴얼 |
| 구현 단위 | 하나의 독립 Pull Request 또는 상호 원자적인 작업 묶음 |

## 필요 근거

고객 배치 개발자가 Spring Batch 실행과 CPF 운영·복구 기능을 한 예제로 확인해야 한다.

## 고객 업무 시나리오

고객번호 범위를 8개로 나눠 병렬 정산한다.

## 구현 위치와 책임 경계

표준 Source `cpf-education/src/main/java/com/cpf/reference/edu/bat04/partition/`. 고객 Job을 `cpf-batch` Runtime에 넣지 않는다.

- Resource·Fixture: Job Fixture·Parameter·대사 Query는 `cpf-education/src/main/resources/edu/edu/bat/04/`와 중앙 Vendor Pack에 둔다.
- 교육 ID는 Package, Test Class, Catalog, Log, 매뉴얼에서 `EDU-BAT-04`로 동일하게 사용한다.
- 예제 때문에 Runtime Owner Module에 고객 업무 상태·Table·Controller를 추가하지 않는다.
- 기존 Public API·SPI·Owner Port가 있으면 그것을 사용한다. 없으면 Owner Module에 재사용 가능한 Extension을 먼저 구현하고 교육 예제는 그 Extension의 Consumer가 된다.

## 입력·계약

Job Parameter: rangeStart, rangeEnd, gridSize. Job/Step 이름과 Version에 `EDU-BAT-04` 식별자를 포함한다.

## 상태와 저장 근거

PLANNED→CLAIMED→RUNNING→COMPLETED

업무 ID, Operation ID, 요청 Hash, Version, Actor, Reason, Correlation ID를 적용 가능한 범위에서 같은 처리 근거로 연결한다.

## 정상 처리 순서

1. JobParameter를 식별·비식별 값으로 분리한다.
2. Job·Step·Reader/Processor/Writer 또는 Tasklet을 역할별로 분리한다.
3. Preview·Checkpoint·Commit·대사 기준을 저장한다.
4. ADM 등록·실행·진행·중지·재시작 화면과 연결한다.

## 예외·장애·경계 사례

| 사례 | 구현·검증 요구 |
|---|---|
| 범위 중첩 | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |
| 누락 | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |
| Partition 재할당 | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |
| Worker 종료 | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |

## 필수 Test

- 정상 실행
- 중간 실패·Process 종료
- 중복 Trigger
- Stop·Restart
- 업무 건수·금액·Hash 대사
- ADM Execution ID 연결

## 운영·화면 확인

ADM Batch Overview·Executions·Instances·Recovery·Audit에서 같은 Execution ID와 Parameter Hash를 확인한다.

- Log·Metric·Trace·Audit에서 같은 업무 ID와 Operation ID를 검색한다.
- 화면의 Field·Button·상태·오류 메시지가 매뉴얼 설명과 일치해야 한다.
- 응답 유실 또는 부분 실패가 있는 요청은 새 요청 전에 Reconcile 경로를 제공한다.

## 완료 조건

- Partition 합집합=전체 대상, 교집합=0
- JobRepository 결과와 업무 원장 합계가 일치한다.
- 실패 후 새 실행·Restart·Reprocess 선택 근거가 문서화된다.
- 실행 명령, 입력값, 기대 결과, 장애 재현, 복구 명령을 매뉴얼의 같은 EDU ID에 반영한다.
- 직접 실행하지 않은 DB Vendor·Browser·다중 인스턴스·장애 Test를 성공으로 기록하지 않는다.

## 금지 사항

- 메모리 Map과 고정 JSON만 반환하는 예제로 종료
- 정상 Case 한 건만 Test
- 고객 업무 DB를 ADM·BZA·Gateway가 직접 수정
- 교육 Profile을 기본 Profile에서 자동 활성화
- 비밀번호·Token·개인정보 원문을 Source·Log·Fixture에 기록
- 기존 Package Convention과 다른 별도 `sample`, `demo`, `misc` Root 생성

---

# 25. EDU-BAT-05 — Manager·Worker·Lease·Fencing

| 항목 | 내용 |
|---|---|
| 영역 | 배치 |
| 우선순위 | P1 |
| 연결 매뉴얼 | 02 배치 개발 매뉴얼 |
| 구현 단위 | 하나의 독립 Pull Request 또는 상호 원자적인 작업 묶음 |

## 필요 근거

고객 배치 개발자가 Spring Batch 실행과 CPF 운영·복구 기능을 한 예제로 확인해야 한다.

## 고객 업무 시나리오

Manager 1개와 Worker 2개로 원격 처리를 실행한다.

## 구현 위치와 책임 경계

표준 Source `cpf-education/src/main/java/com/cpf/reference/edu/bat05/remoteworker/`. 고객 Job을 `cpf-batch` Runtime에 넣지 않는다.

- Resource·Fixture: Job Fixture·Parameter·대사 Query는 `cpf-education/src/main/resources/edu/edu/bat/05/`와 중앙 Vendor Pack에 둔다.
- 교육 ID는 Package, Test Class, Catalog, Log, 매뉴얼에서 `EDU-BAT-05`로 동일하게 사용한다.
- 예제 때문에 Runtime Owner Module에 고객 업무 상태·Table·Controller를 추가하지 않는다.
- 기존 Public API·SPI·Owner Port가 있으면 그것을 사용한다. 없으면 Owner Module에 재사용 가능한 Extension을 먼저 구현하고 교육 예제는 그 Extension의 Consumer가 된다.

## 입력·계약

Job Parameter: executionId, partitionId, leaseDuration. Job/Step 이름과 Version에 `EDU-BAT-05` 식별자를 포함한다.

## 상태와 저장 근거

AVAILABLE→CLAIMED→RUNNING→COMPLETED|EXPIRED

업무 ID, Operation ID, 요청 Hash, Version, Actor, Reason, Correlation ID를 적용 가능한 범위에서 같은 처리 근거로 연결한다.

## 정상 처리 순서

1. JobParameter를 식별·비식별 값으로 분리한다.
2. Job·Step·Reader/Processor/Writer 또는 Tasklet을 역할별로 분리한다.
3. Preview·Checkpoint·Commit·대사 기준을 저장한다.
4. ADM 등록·실행·진행·중지·재시작 화면과 연결한다.

## 예외·장애·경계 사례

| 사례 | 구현·검증 요구 |
|---|---|
| Heartbeat 중단 | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |
| Lease 상실 | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |
| 늦은 결과 | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |
| Kafka 중복 | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |

## 필수 Test

- 정상 실행
- 중간 실패·Process 종료
- 중복 Trigger
- Stop·Restart
- 업무 건수·금액·Hash 대사
- ADM Execution ID 연결

## 운영·화면 확인

ADM Batch Overview·Executions·Instances·Recovery·Audit에서 같은 Execution ID와 Parameter Hash를 확인한다.

- Log·Metric·Trace·Audit에서 같은 업무 ID와 Operation ID를 검색한다.
- 화면의 Field·Button·상태·오류 메시지가 매뉴얼 설명과 일치해야 한다.
- 응답 유실 또는 부분 실패가 있는 요청은 새 요청 전에 Reconcile 경로를 제공한다.

## 완료 조건

- Lease 잃은 Worker 결과가 현재 상태를 덮지 않음
- JobRepository 결과와 업무 원장 합계가 일치한다.
- 실패 후 새 실행·Restart·Reprocess 선택 근거가 문서화된다.
- 실행 명령, 입력값, 기대 결과, 장애 재현, 복구 명령을 매뉴얼의 같은 EDU ID에 반영한다.
- 직접 실행하지 않은 DB Vendor·Browser·다중 인스턴스·장애 Test를 성공으로 기록하지 않는다.

## 금지 사항

- 메모리 Map과 고정 JSON만 반환하는 예제로 종료
- 정상 Case 한 건만 Test
- 고객 업무 DB를 ADM·BZA·Gateway가 직접 수정
- 교육 Profile을 기본 Profile에서 자동 활성화
- 비밀번호·Token·개인정보 원문을 Source·Log·Fixture에 기록
- 기존 Package Convention과 다른 별도 `sample`, `demo`, `misc` Root 생성

---

# 26. EDU-BAT-06 — 센터컷 Preview·승인·실행

| 항목 | 내용 |
|---|---|
| 영역 | 배치 |
| 우선순위 | P0 |
| 연결 매뉴얼 | 02 배치 개발 매뉴얼 |
| 구현 단위 | 하나의 독립 Pull Request 또는 상호 원자적인 작업 묶음 |

## 필요 근거

고객 배치 개발자가 Spring Batch 실행과 CPF 운영·복구 기능을 한 예제로 확인해야 한다.

## 고객 업무 시나리오

5,000건 대상 Preview 후 승인 Snapshot으로 실행한다.

## 구현 위치와 책임 경계

표준 Source `cpf-education/src/main/java/com/cpf/reference/edu/bat06/centercut/`. 고객 Job을 `cpf-batch` Runtime에 넣지 않는다.

- Resource·Fixture: Job Fixture·Parameter·대사 Query는 `cpf-education/src/main/resources/edu/edu/bat/06/`와 중앙 Vendor Pack에 둔다.
- 교육 ID는 Package, Test Class, Catalog, Log, 매뉴얼에서 `EDU-BAT-06`로 동일하게 사용한다.
- 예제 때문에 Runtime Owner Module에 고객 업무 상태·Table·Controller를 추가하지 않는다.
- 기존 Public API·SPI·Owner Port가 있으면 그것을 사용한다. 없으면 Owner Module에 재사용 가능한 Extension을 먼저 구현하고 교육 예제는 그 Extension의 Consumer가 된다.

## 입력·계약

Job Parameter: criteriaVersion, snapshotId, checksum. Job/Step 이름과 Version에 `EDU-BAT-06` 식별자를 포함한다.

## 상태와 저장 근거

DRAFT→PREVIEWED→APPROVED→RUNNING→RECONCILED

업무 ID, Operation ID, 요청 Hash, Version, Actor, Reason, Correlation ID를 적용 가능한 범위에서 같은 처리 근거로 연결한다.

## 정상 처리 순서

1. JobParameter를 식별·비식별 값으로 분리한다.
2. Job·Step·Reader/Processor/Writer 또는 Tasklet을 역할별로 분리한다.
3. Preview·Checkpoint·Commit·대사 기준을 저장한다.
4. ADM 등록·실행·진행·중지·재시작 화면과 연결한다.

## 예외·장애·경계 사례

| 사례 | 구현·검증 요구 |
|---|---|
| 승인 후 대상 변경 | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |
| Checksum 불일치 | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |
| 일부 실패 | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |

## 필수 Test

- 정상 실행
- 중간 실패·Process 종료
- 중복 Trigger
- Stop·Restart
- 업무 건수·금액·Hash 대사
- ADM Execution ID 연결

## 운영·화면 확인

ADM Batch Overview·Executions·Instances·Recovery·Audit에서 같은 Execution ID와 Parameter Hash를 확인한다.

- Log·Metric·Trace·Audit에서 같은 업무 ID와 Operation ID를 검색한다.
- 화면의 Field·Button·상태·오류 메시지가 매뉴얼 설명과 일치해야 한다.
- 응답 유실 또는 부분 실패가 있는 요청은 새 요청 전에 Reconcile 경로를 제공한다.

## 완료 조건

- 승인 Snapshot과 실행 대상 Version·Checksum 동일
- JobRepository 결과와 업무 원장 합계가 일치한다.
- 실패 후 새 실행·Restart·Reprocess 선택 근거가 문서화된다.
- 실행 명령, 입력값, 기대 결과, 장애 재현, 복구 명령을 매뉴얼의 같은 EDU ID에 반영한다.
- 직접 실행하지 않은 DB Vendor·Browser·다중 인스턴스·장애 Test를 성공으로 기록하지 않는다.

## 금지 사항

- 메모리 Map과 고정 JSON만 반환하는 예제로 종료
- 정상 Case 한 건만 Test
- 고객 업무 DB를 ADM·BZA·Gateway가 직접 수정
- 교육 Profile을 기본 Profile에서 자동 활성화
- 비밀번호·Token·개인정보 원문을 Source·Log·Fixture에 기록
- 기존 Package Convention과 다른 별도 `sample`, `demo`, `misc` Root 생성

---

# 27. EDU-BAT-07 — 영업일 23시 Scheduler

| 항목 | 내용 |
|---|---|
| 영역 | 배치 |
| 우선순위 | P1 |
| 연결 매뉴얼 | 02 배치 개발 매뉴얼 |
| 구현 단위 | 하나의 독립 Pull Request 또는 상호 원자적인 작업 묶음 |

## 필요 근거

고객 배치 개발자가 Spring Batch 실행과 CPF 운영·복구 기능을 한 예제로 확인해야 한다.

## 고객 업무 시나리오

영업일 23시에 월말 Job을 한 번 Trigger한다.

## 구현 위치와 책임 경계

표준 Source `cpf-education/src/main/java/com/cpf/reference/edu/bat07/scheduler/`. 고객 Job을 `cpf-batch` Runtime에 넣지 않는다.

- Resource·Fixture: Job Fixture·Parameter·대사 Query는 `cpf-education/src/main/resources/edu/edu/bat/07/`와 중앙 Vendor Pack에 둔다.
- 교육 ID는 Package, Test Class, Catalog, Log, 매뉴얼에서 `EDU-BAT-07`로 동일하게 사용한다.
- 예제 때문에 Runtime Owner Module에 고객 업무 상태·Table·Controller를 추가하지 않는다.
- 기존 Public API·SPI·Owner Port가 있으면 그것을 사용한다. 없으면 Owner Module에 재사용 가능한 Extension을 먼저 구현하고 교육 예제는 그 Extension의 Consumer가 된다.

## 입력·계약

Job Parameter: scheduleId, calendarId, misfirePolicy. Job/Step 이름과 Version에 `EDU-BAT-07` 식별자를 포함한다.

## 상태와 저장 근거

SCHEDULED→TRIGGERED→COMPLETED|MISFIRED

업무 ID, Operation ID, 요청 Hash, Version, Actor, Reason, Correlation ID를 적용 가능한 범위에서 같은 처리 근거로 연결한다.

## 정상 처리 순서

1. JobParameter를 식별·비식별 값으로 분리한다.
2. Job·Step·Reader/Processor/Writer 또는 Tasklet을 역할별로 분리한다.
3. Preview·Checkpoint·Commit·대사 기준을 저장한다.
4. ADM 등록·실행·진행·중지·재시작 화면과 연결한다.

## 예외·장애·경계 사례

| 사례 | 구현·검증 요구 |
|---|---|
| 이중 Trigger | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |
| Clock 차이 | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |
| 휴일 Override | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |
| Misfire | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |

## 필수 Test

- 정상 실행
- 중간 실패·Process 종료
- 중복 Trigger
- Stop·Restart
- 업무 건수·금액·Hash 대사
- ADM Execution ID 연결

## 운영·화면 확인

ADM Batch Overview·Executions·Instances·Recovery·Audit에서 같은 Execution ID와 Parameter Hash를 확인한다.

- Log·Metric·Trace·Audit에서 같은 업무 ID와 Operation ID를 검색한다.
- 화면의 Field·Button·상태·오류 메시지가 매뉴얼 설명과 일치해야 한다.
- 응답 유실 또는 부분 실패가 있는 요청은 새 요청 전에 Reconcile 경로를 제공한다.

## 완료 조건

- 같은 예정시각에 JobInstance 1개
- JobRepository 결과와 업무 원장 합계가 일치한다.
- 실패 후 새 실행·Restart·Reprocess 선택 근거가 문서화된다.
- 실행 명령, 입력값, 기대 결과, 장애 재현, 복구 명령을 매뉴얼의 같은 EDU ID에 반영한다.
- 직접 실행하지 않은 DB Vendor·Browser·다중 인스턴스·장애 Test를 성공으로 기록하지 않는다.

## 금지 사항

- 메모리 Map과 고정 JSON만 반환하는 예제로 종료
- 정상 Case 한 건만 Test
- 고객 업무 DB를 ADM·BZA·Gateway가 직접 수정
- 교육 Profile을 기본 Profile에서 자동 활성화
- 비밀번호·Token·개인정보 원문을 Source·Log·Fixture에 기록
- 기존 Package Convention과 다른 별도 `sample`, `demo`, `misc` Root 생성

---

# 28. EDU-BAT-08 — Job Pack Version·Artifact 배포

| 항목 | 내용 |
|---|---|
| 영역 | 배치 |
| 우선순위 | P1 |
| 연결 매뉴얼 | 02 배치 개발 매뉴얼 |
| 구현 단위 | 하나의 독립 Pull Request 또는 상호 원자적인 작업 묶음 |

## 필요 근거

고객 배치 개발자가 Spring Batch 실행과 CPF 운영·복구 기능을 한 예제로 확인해야 한다.

## 고객 업무 시나리오

Job Pack Version 3을 Agent 여러 대에 배포한다.

## 구현 위치와 책임 경계

표준 Source `cpf-education/src/main/java/com/cpf/reference/edu/bat08/jobpack/`. 고객 Job을 `cpf-batch` Runtime에 넣지 않는다.

- Resource·Fixture: Job Fixture·Parameter·대사 Query는 `cpf-education/src/main/resources/edu/edu/bat/08/`와 중앙 Vendor Pack에 둔다.
- 교육 ID는 Package, Test Class, Catalog, Log, 매뉴얼에서 `EDU-BAT-08`로 동일하게 사용한다.
- 예제 때문에 Runtime Owner Module에 고객 업무 상태·Table·Controller를 추가하지 않는다.
- 기존 Public API·SPI·Owner Port가 있으면 그것을 사용한다. 없으면 Owner Module에 재사용 가능한 Extension을 먼저 구현하고 교육 예제는 그 Extension의 Consumer가 된다.

## 입력·계약

Job Parameter: jobPackId, version, artifactSha. Job/Step 이름과 Version에 `EDU-BAT-08` 식별자를 포함한다.

## 상태와 저장 근거

BUILT→VALIDATED→APPROVED→DEPLOYED|PARTIAL

업무 ID, Operation ID, 요청 Hash, Version, Actor, Reason, Correlation ID를 적용 가능한 범위에서 같은 처리 근거로 연결한다.

## 정상 처리 순서

1. JobParameter를 식별·비식별 값으로 분리한다.
2. Job·Step·Reader/Processor/Writer 또는 Tasklet을 역할별로 분리한다.
3. Preview·Checkpoint·Commit·대사 기준을 저장한다.
4. ADM 등록·실행·진행·중지·재시작 화면과 연결한다.

## 예외·장애·경계 사례

| 사례 | 구현·검증 요구 |
|---|---|
| Checksum 불일치 | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |
| 일부 Agent NACK | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |
| 이전 Version 실행 | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |

## 필수 Test

- 정상 실행
- 중간 실패·Process 종료
- 중복 Trigger
- Stop·Restart
- 업무 건수·금액·Hash 대사
- ADM Execution ID 연결

## 운영·화면 확인

ADM Batch Overview·Executions·Instances·Recovery·Audit에서 같은 Execution ID와 Parameter Hash를 확인한다.

- Log·Metric·Trace·Audit에서 같은 업무 ID와 Operation ID를 검색한다.
- 화면의 Field·Button·상태·오류 메시지가 매뉴얼 설명과 일치해야 한다.
- 응답 유실 또는 부분 실패가 있는 요청은 새 요청 전에 Reconcile 경로를 제공한다.

## 완료 조건

- 모든 정상 Agent의 Artifact SHA·Version 일치
- JobRepository 결과와 업무 원장 합계가 일치한다.
- 실패 후 새 실행·Restart·Reprocess 선택 근거가 문서화된다.
- 실행 명령, 입력값, 기대 결과, 장애 재현, 복구 명령을 매뉴얼의 같은 EDU ID에 반영한다.
- 직접 실행하지 않은 DB Vendor·Browser·다중 인스턴스·장애 Test를 성공으로 기록하지 않는다.

## 금지 사항

- 메모리 Map과 고정 JSON만 반환하는 예제로 종료
- 정상 Case 한 건만 Test
- 고객 업무 DB를 ADM·BZA·Gateway가 직접 수정
- 교육 Profile을 기본 Profile에서 자동 활성화
- 비밀번호·Token·개인정보 원문을 Source·Log·Fixture에 기록
- 기존 Package Convention과 다른 별도 `sample`, `demo`, `misc` Root 생성

---

# 29. EDU-BAT-09 — 중지·재시작·실패건 재처리

| 항목 | 내용 |
|---|---|
| 영역 | 배치 |
| 우선순위 | P0 |
| 연결 매뉴얼 | 02 배치 개발 매뉴얼 |
| 구현 단위 | 하나의 독립 Pull Request 또는 상호 원자적인 작업 묶음 |

## 필요 근거

고객 배치 개발자가 Spring Batch 실행과 CPF 운영·복구 기능을 한 예제로 확인해야 한다.

## 고객 업무 시나리오

Chunk 처리 중 Stop 후 마지막 Commit 다음부터 재시작한다.

## 구현 위치와 책임 경계

표준 Source `cpf-education/src/main/java/com/cpf/reference/edu/bat09/restart/`. 고객 Job을 `cpf-batch` Runtime에 넣지 않는다.

- Resource·Fixture: Job Fixture·Parameter·대사 Query는 `cpf-education/src/main/resources/edu/edu/bat/09/`와 중앙 Vendor Pack에 둔다.
- 교육 ID는 Package, Test Class, Catalog, Log, 매뉴얼에서 `EDU-BAT-09`로 동일하게 사용한다.
- 예제 때문에 Runtime Owner Module에 고객 업무 상태·Table·Controller를 추가하지 않는다.
- 기존 Public API·SPI·Owner Port가 있으면 그것을 사용한다. 없으면 Owner Module에 재사용 가능한 Extension을 먼저 구현하고 교육 예제는 그 Extension의 Consumer가 된다.

## 입력·계약

Job Parameter: executionId, restartMode, reprocessScope. Job/Step 이름과 Version에 `EDU-BAT-09` 식별자를 포함한다.

## 상태와 저장 근거

RUNNING→STOPPING→STOPPED→RESTARTING→COMPLETED

업무 ID, Operation ID, 요청 Hash, Version, Actor, Reason, Correlation ID를 적용 가능한 범위에서 같은 처리 근거로 연결한다.

## 정상 처리 순서

1. JobParameter를 식별·비식별 값으로 분리한다.
2. Job·Step·Reader/Processor/Writer 또는 Tasklet을 역할별로 분리한다.
3. Preview·Checkpoint·Commit·대사 기준을 저장한다.
4. ADM 등록·실행·진행·중지·재시작 화면과 연결한다.

## 예외·장애·경계 사례

| 사례 | 구현·검증 요구 |
|---|---|
| Stop Timeout | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |
| Abandon 오용 | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |
| 완료 구간 재처리 | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |

## 필수 Test

- 정상 실행
- 중간 실패·Process 종료
- 중복 Trigger
- Stop·Restart
- 업무 건수·금액·Hash 대사
- ADM Execution ID 연결

## 운영·화면 확인

ADM Batch Overview·Executions·Instances·Recovery·Audit에서 같은 Execution ID와 Parameter Hash를 확인한다.

- Log·Metric·Trace·Audit에서 같은 업무 ID와 Operation ID를 검색한다.
- 화면의 Field·Button·상태·오류 메시지가 매뉴얼 설명과 일치해야 한다.
- 응답 유실 또는 부분 실패가 있는 요청은 새 요청 전에 Reconcile 경로를 제공한다.

## 완료 조건

- 완료 구간 중복 0, 실패 범위만 재처리
- JobRepository 결과와 업무 원장 합계가 일치한다.
- 실패 후 새 실행·Restart·Reprocess 선택 근거가 문서화된다.
- 실행 명령, 입력값, 기대 결과, 장애 재현, 복구 명령을 매뉴얼의 같은 EDU ID에 반영한다.
- 직접 실행하지 않은 DB Vendor·Browser·다중 인스턴스·장애 Test를 성공으로 기록하지 않는다.

## 금지 사항

- 메모리 Map과 고정 JSON만 반환하는 예제로 종료
- 정상 Case 한 건만 Test
- 고객 업무 DB를 ADM·BZA·Gateway가 직접 수정
- 교육 Profile을 기본 Profile에서 자동 활성화
- 비밀번호·Token·개인정보 원문을 Source·Log·Fixture에 기록
- 기존 Package Convention과 다른 별도 `sample`, `demo`, `misc` Root 생성

---

# 30. EDU-BAT-10 — 실행 요청 응답 유실·결과 대사

| 항목 | 내용 |
|---|---|
| 영역 | 배치 |
| 우선순위 | P0 |
| 연결 매뉴얼 | 02 배치 개발 매뉴얼 |
| 구현 단위 | 하나의 독립 Pull Request 또는 상호 원자적인 작업 묶음 |

## 필요 근거

고객 배치 개발자가 Spring Batch 실행과 CPF 운영·복구 기능을 한 예제로 확인해야 한다.

## 고객 업무 시나리오

실행 요청 응답을 받지 못한 뒤 Execution과 업무 합계를 대사한다.

## 구현 위치와 책임 경계

표준 Source `cpf-education/src/main/java/com/cpf/reference/edu/bat10/reconcile/`. 고객 Job을 `cpf-batch` Runtime에 넣지 않는다.

- Resource·Fixture: Job Fixture·Parameter·대사 Query는 `cpf-education/src/main/resources/edu/edu/bat/10/`와 중앙 Vendor Pack에 둔다.
- 교육 ID는 Package, Test Class, Catalog, Log, 매뉴얼에서 `EDU-BAT-10`로 동일하게 사용한다.
- 예제 때문에 Runtime Owner Module에 고객 업무 상태·Table·Controller를 추가하지 않는다.
- 기존 Public API·SPI·Owner Port가 있으면 그것을 사용한다. 없으면 Owner Module에 재사용 가능한 Extension을 먼저 구현하고 교육 예제는 그 Extension의 Consumer가 된다.

## 입력·계약

Job Parameter: requestId, jobParametersHash. Job/Step 이름과 Version에 `EDU-BAT-10` 식별자를 포함한다.

## 상태와 저장 근거

REQUESTED→UNKNOWN_RESULT→RECONCILED

업무 ID, Operation ID, 요청 Hash, Version, Actor, Reason, Correlation ID를 적용 가능한 범위에서 같은 처리 근거로 연결한다.

## 정상 처리 순서

1. JobParameter를 식별·비식별 값으로 분리한다.
2. Job·Step·Reader/Processor/Writer 또는 Tasklet을 역할별로 분리한다.
3. Preview·Checkpoint·Commit·대사 기준을 저장한다.
4. ADM 등록·실행·진행·중지·재시작 화면과 연결한다.

## 예외·장애·경계 사례

| 사례 | 구현·검증 요구 |
|---|---|
| 요청 수신 전/후 Drop | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |
| 중복 시작 | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |
| Metadata 지연 | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |

## 필수 Test

- 정상 실행
- 중간 실패·Process 종료
- 중복 Trigger
- Stop·Restart
- 업무 건수·금액·Hash 대사
- ADM Execution ID 연결

## 운영·화면 확인

ADM Batch Overview·Executions·Instances·Recovery·Audit에서 같은 Execution ID와 Parameter Hash를 확인한다.

- Log·Metric·Trace·Audit에서 같은 업무 ID와 Operation ID를 검색한다.
- 화면의 Field·Button·상태·오류 메시지가 매뉴얼 설명과 일치해야 한다.
- 응답 유실 또는 부분 실패가 있는 요청은 새 요청 전에 Reconcile 경로를 제공한다.

## 완료 조건

- 새 실행 없이 실제 Execution과 업무 결과 확정
- JobRepository 결과와 업무 원장 합계가 일치한다.
- 실패 후 새 실행·Restart·Reprocess 선택 근거가 문서화된다.
- 실행 명령, 입력값, 기대 결과, 장애 재현, 복구 명령을 매뉴얼의 같은 EDU ID에 반영한다.
- 직접 실행하지 않은 DB Vendor·Browser·다중 인스턴스·장애 Test를 성공으로 기록하지 않는다.

## 금지 사항

- 메모리 Map과 고정 JSON만 반환하는 예제로 종료
- 정상 Case 한 건만 Test
- 고객 업무 DB를 ADM·BZA·Gateway가 직접 수정
- 교육 Profile을 기본 Profile에서 자동 활성화
- 비밀번호·Token·개인정보 원문을 Source·Log·Fixture에 기록
- 기존 Package Convention과 다른 별도 `sample`, `demo`, `misc` Root 생성

---

# 31. EDU-ADM-01 — 기존 ADM 기능 재사용 판단

| 항목 | 내용 |
|---|---|
| 영역 | ADM 연동 |
| 우선순위 | P1 |
| 연결 매뉴얼 | 03 ADM 개발자 매뉴얼·04 ADM 운영자 매뉴얼 |
| 구현 단위 | 하나의 독립 Pull Request 또는 상호 원자적인 작업 묶음 |

## 필요 근거

ADM은 고객 업무를 개발하는 곳이 아니라 완성된 고객 업무를 조회·통제·승인·대사하는 Control Plane이므로 연동 경계를 예제로 보여야 한다.

## 고객 업무 시나리오

지급 업무의 조회·재처리·대사를 기존 거래·Recovery 메뉴로 해결할지 판단한다.

## 구현 위치와 책임 경계

고객 업무 Source는 `cpf-education/src/main/java/com/cpf/reference/edu/adm/adm01/reuse/`. ADM에는 기존 Owner Port·Generated Client·Feature Slice 규칙에 맞춘 Adapter와 화면 연결만 둔다. 고객 지급 원장 로직을 `cpf-admin`에 넣지 않는다.

- Resource·Fixture: 요청·응답 Fixture와 Browser Scenario는 `cpf-education/src/main/resources/edu/edu/adm/01/` 및 `cpf-admin`의 기존 Test 구조에 둔다.
- 교육 ID는 Package, Test Class, Catalog, Log, 매뉴얼에서 `EDU-ADM-01`로 동일하게 사용한다.
- 예제 때문에 Runtime Owner Module에 고객 업무 상태·Table·Controller를 추가하지 않는다.
- 기존 Public API·SPI·Owner Port가 있으면 그것을 사용한다. 없으면 Owner Module에 재사용 가능한 Extension을 먼저 구현하고 교육 예제는 그 Extension의 Consumer가 된다.

## 입력·계약

Query/Command/Operation 계약에 businessId, operationId, expectedVersion, reason, approvalId, actor, correlationId를 필요한 경우 포함한다.

## 상태와 저장 근거

조회형은 상태 변경 없음. Command형은 REQUESTED→ACCEPTED→RUNNING→SUCCEEDED|FAILED|UNKNOWN_RESULT→RECONCILED.

업무 ID, Operation ID, 요청 Hash, Version, Actor, Reason, Correlation ID를 적용 가능한 범위에서 같은 처리 근거로 연결한다.

## 정상 처리 순서

1. 새 메뉴를 만들기 전에 기존 메뉴·Owner API·표준 Operation을 매핑한다.
2. 권한·Data Scope·Masking을 Backend에서 적용한다.
3. Command 전 영향과 현재 Version을 표시한다.
4. 응답 후 업무 Owner 상태와 Audit를 다시 조회한다.
5. 운영 매뉴얼의 실제 Field·Button·상태와 맞춘다.

## 예외·장애·경계 사례

| 사례 | 구현·검증 요구 |
|---|---|
| 기존 메뉴로 필요한 질문에 답하지 못함 | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |
| 중복 메뉴 생성 | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |
| 고객 업무 DB 직접 연결 | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |

## 필수 Test

- Backend Contract Test
- Same-JVM/Remote Adapter Test
- 권한·Masking Test
- Timeout·응답 유실 Test
- Browser Test
- Audit·Trace 연결

## 운영·화면 확인

04 ADM 운영자 매뉴얼의 해당 메뉴에서 조회자·운영자·승인자 권한별로 실행한다.

- Log·Metric·Trace·Audit에서 같은 업무 ID와 Operation ID를 검색한다.
- 화면의 Field·Button·상태·오류 메시지가 매뉴얼 설명과 일치해야 한다.
- 응답 유실 또는 부분 실패가 있는 요청은 새 요청 전에 Reconcile 경로를 제공한다.

## 완료 조건

- ADM이 고객 DB를 직접 수정하지 않는다.
- 화면 메시지와 실제 Owner 상태가 일치한다.
- 부분 성공·미확정을 단순 실패 또는 성공으로 합치지 않는다.
- 실행 명령, 입력값, 기대 결과, 장애 재현, 복구 명령을 매뉴얼의 같은 EDU ID에 반영한다.
- 직접 실행하지 않은 DB Vendor·Browser·다중 인스턴스·장애 Test를 성공으로 기록하지 않는다.

## 금지 사항

- 메모리 Map과 고정 JSON만 반환하는 예제로 종료
- 정상 Case 한 건만 Test
- 고객 업무 DB를 ADM·BZA·Gateway가 직접 수정
- 교육 Profile을 기본 Profile에서 자동 활성화
- 비밀번호·Token·개인정보 원문을 Source·Log·Fixture에 기록
- 기존 Package Convention과 다른 별도 `sample`, `demo`, `misc` Root 생성

---

# 32. EDU-ADM-02 — 고객 업무 조회 연동

| 항목 | 내용 |
|---|---|
| 영역 | ADM 연동 |
| 우선순위 | P0 |
| 연결 매뉴얼 | 03 ADM 개발자 매뉴얼·04 ADM 운영자 매뉴얼 |
| 구현 단위 | 하나의 독립 Pull Request 또는 상호 원자적인 작업 묶음 |

## 필요 근거

ADM은 고객 업무를 개발하는 곳이 아니라 완성된 고객 업무를 조회·통제·승인·대사하는 Control Plane이므로 연동 경계를 예제로 보여야 한다.

## 고객 업무 시나리오

운영자가 지급 ID·기간·상태·조직으로 목록과 상세를 조회한다.

## 구현 위치와 책임 경계

고객 업무 Source는 `cpf-education/src/main/java/com/cpf/reference/edu/adm/adm02/query/`. ADM에는 기존 Owner Port·Generated Client·Feature Slice 규칙에 맞춘 Adapter와 화면 연결만 둔다. 고객 지급 원장 로직을 `cpf-admin`에 넣지 않는다.

- Resource·Fixture: 요청·응답 Fixture와 Browser Scenario는 `cpf-education/src/main/resources/edu/edu/adm/02/` 및 `cpf-admin`의 기존 Test 구조에 둔다.
- 교육 ID는 Package, Test Class, Catalog, Log, 매뉴얼에서 `EDU-ADM-02`로 동일하게 사용한다.
- 예제 때문에 Runtime Owner Module에 고객 업무 상태·Table·Controller를 추가하지 않는다.
- 기존 Public API·SPI·Owner Port가 있으면 그것을 사용한다. 없으면 Owner Module에 재사용 가능한 Extension을 먼저 구현하고 교육 예제는 그 Extension의 Consumer가 된다.

## 입력·계약

Query/Command/Operation 계약에 businessId, operationId, expectedVersion, reason, approvalId, actor, correlationId를 필요한 경우 포함한다.

## 상태와 저장 근거

조회형은 상태 변경 없음. Command형은 REQUESTED→ACCEPTED→RUNNING→SUCCEEDED|FAILED|UNKNOWN_RESULT→RECONCILED.

업무 ID, Operation ID, 요청 Hash, Version, Actor, Reason, Correlation ID를 적용 가능한 범위에서 같은 처리 근거로 연결한다.

## 정상 처리 순서

1. 읽기 전용 Query 계약과 Masking·Data Scope를 연결한다.
2. 권한·Data Scope·Masking을 Backend에서 적용한다.
3. Command 전 영향과 현재 Version을 표시한다.
4. 응답 후 업무 Owner 상태와 Audit를 다시 조회한다.
5. 운영 매뉴얼의 실제 Field·Button·상태와 맞춘다.

## 예외·장애·경계 사례

| 사례 | 구현·검증 요구 |
|---|---|
| 조회 Timeout | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |
| 권한 밖 조직 | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |
| 부분 데이터 | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |
| Stale Version | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |

## 필수 Test

- Backend Contract Test
- Same-JVM/Remote Adapter Test
- 권한·Masking Test
- Timeout·응답 유실 Test
- Browser Test
- Audit·Trace 연결

## 운영·화면 확인

04 ADM 운영자 매뉴얼의 해당 메뉴에서 조회자·운영자·승인자 권한별로 실행한다.

- Log·Metric·Trace·Audit에서 같은 업무 ID와 Operation ID를 검색한다.
- 화면의 Field·Button·상태·오류 메시지가 매뉴얼 설명과 일치해야 한다.
- 응답 유실 또는 부분 실패가 있는 요청은 새 요청 전에 Reconcile 경로를 제공한다.

## 완료 조건

- ADM이 고객 DB를 직접 수정하지 않는다.
- 화면 메시지와 실제 Owner 상태가 일치한다.
- 부분 성공·미확정을 단순 실패 또는 성공으로 합치지 않는다.
- 실행 명령, 입력값, 기대 결과, 장애 재현, 복구 명령을 매뉴얼의 같은 EDU ID에 반영한다.
- 직접 실행하지 않은 DB Vendor·Browser·다중 인스턴스·장애 Test를 성공으로 기록하지 않는다.

## 금지 사항

- 메모리 Map과 고정 JSON만 반환하는 예제로 종료
- 정상 Case 한 건만 Test
- 고객 업무 DB를 ADM·BZA·Gateway가 직접 수정
- 교육 Profile을 기본 Profile에서 자동 활성화
- 비밀번호·Token·개인정보 원문을 Source·Log·Fixture에 기록
- 기존 Package Convention과 다른 별도 `sample`, `demo`, `misc` Root 생성

---

# 33. EDU-ADM-03 — 안전한 운영 조치

| 항목 | 내용 |
|---|---|
| 영역 | ADM 연동 |
| 우선순위 | P0 |
| 연결 매뉴얼 | 03 ADM 개발자 매뉴얼·04 ADM 운영자 매뉴얼 |
| 구현 단위 | 하나의 독립 Pull Request 또는 상호 원자적인 작업 묶음 |

## 필요 근거

ADM은 고객 업무를 개발하는 곳이 아니라 완성된 고객 업무를 조회·통제·승인·대사하는 Control Plane이므로 연동 경계를 예제로 보여야 한다.

## 고객 업무 시나리오

지급 재처리·취소를 대상·사유·expectedVersion과 함께 실행한다.

## 구현 위치와 책임 경계

고객 업무 Source는 `cpf-education/src/main/java/com/cpf/reference/edu/adm/adm03/command/`. ADM에는 기존 Owner Port·Generated Client·Feature Slice 규칙에 맞춘 Adapter와 화면 연결만 둔다. 고객 지급 원장 로직을 `cpf-admin`에 넣지 않는다.

- Resource·Fixture: 요청·응답 Fixture와 Browser Scenario는 `cpf-education/src/main/resources/edu/edu/adm/03/` 및 `cpf-admin`의 기존 Test 구조에 둔다.
- 교육 ID는 Package, Test Class, Catalog, Log, 매뉴얼에서 `EDU-ADM-03`로 동일하게 사용한다.
- 예제 때문에 Runtime Owner Module에 고객 업무 상태·Table·Controller를 추가하지 않는다.
- 기존 Public API·SPI·Owner Port가 있으면 그것을 사용한다. 없으면 Owner Module에 재사용 가능한 Extension을 먼저 구현하고 교육 예제는 그 Extension의 Consumer가 된다.

## 입력·계약

Query/Command/Operation 계약에 businessId, operationId, expectedVersion, reason, approvalId, actor, correlationId를 필요한 경우 포함한다.

## 상태와 저장 근거

조회형은 상태 변경 없음. Command형은 REQUESTED→ACCEPTED→RUNNING→SUCCEEDED|FAILED|UNKNOWN_RESULT→RECONCILED.

업무 ID, Operation ID, 요청 Hash, Version, Actor, Reason, Correlation ID를 적용 가능한 범위에서 같은 처리 근거로 연결한다.

## 정상 처리 순서

1. 업무 Owner의 Command API를 호출하고 Operation 상태를 재조회한다.
2. 권한·Data Scope·Masking을 Backend에서 적용한다.
3. Command 전 영향과 현재 Version을 표시한다.
4. 응답 후 업무 Owner 상태와 Audit를 다시 조회한다.
5. 운영 매뉴얼의 실제 Field·Button·상태와 맞춘다.

## 예외·장애·경계 사례

| 사례 | 구현·검증 요구 |
|---|---|
| 사유 누락 | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |
| Version 충돌 | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |
| 허용 상태 아님 | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |
| 응답 유실 | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |

## 필수 Test

- Backend Contract Test
- Same-JVM/Remote Adapter Test
- 권한·Masking Test
- Timeout·응답 유실 Test
- Browser Test
- Audit·Trace 연결

## 운영·화면 확인

04 ADM 운영자 매뉴얼의 해당 메뉴에서 조회자·운영자·승인자 권한별로 실행한다.

- Log·Metric·Trace·Audit에서 같은 업무 ID와 Operation ID를 검색한다.
- 화면의 Field·Button·상태·오류 메시지가 매뉴얼 설명과 일치해야 한다.
- 응답 유실 또는 부분 실패가 있는 요청은 새 요청 전에 Reconcile 경로를 제공한다.

## 완료 조건

- ADM이 고객 DB를 직접 수정하지 않는다.
- 화면 메시지와 실제 Owner 상태가 일치한다.
- 부분 성공·미확정을 단순 실패 또는 성공으로 합치지 않는다.
- 실행 명령, 입력값, 기대 결과, 장애 재현, 복구 명령을 매뉴얼의 같은 EDU ID에 반영한다.
- 직접 실행하지 않은 DB Vendor·Browser·다중 인스턴스·장애 Test를 성공으로 기록하지 않는다.

## 금지 사항

- 메모리 Map과 고정 JSON만 반환하는 예제로 종료
- 정상 Case 한 건만 Test
- 고객 업무 DB를 ADM·BZA·Gateway가 직접 수정
- 교육 Profile을 기본 Profile에서 자동 활성화
- 비밀번호·Token·개인정보 원문을 Source·Log·Fixture에 기록
- 기존 Package Convention과 다른 별도 `sample`, `demo`, `misc` Root 생성

---

# 34. EDU-ADM-04 — 승인 필요한 위험 조치

| 항목 | 내용 |
|---|---|
| 영역 | ADM 연동 |
| 우선순위 | P0 |
| 연결 매뉴얼 | 03 ADM 개발자 매뉴얼·04 ADM 운영자 매뉴얼 |
| 구현 단위 | 하나의 독립 Pull Request 또는 상호 원자적인 작업 묶음 |

## 필요 근거

ADM은 고객 업무를 개발하는 곳이 아니라 완성된 고객 업무를 조회·통제·승인·대사하는 Control Plane이므로 연동 경계를 예제로 보여야 한다.

## 고객 업무 시나리오

100건 이상 대량 재처리를 실행자와 승인자가 분리해 처리한다.

## 구현 위치와 책임 경계

고객 업무 Source는 `cpf-education/src/main/java/com/cpf/reference/edu/adm/adm04/approval/`. ADM에는 기존 Owner Port·Generated Client·Feature Slice 규칙에 맞춘 Adapter와 화면 연결만 둔다. 고객 지급 원장 로직을 `cpf-admin`에 넣지 않는다.

- Resource·Fixture: 요청·응답 Fixture와 Browser Scenario는 `cpf-education/src/main/resources/edu/edu/adm/04/` 및 `cpf-admin`의 기존 Test 구조에 둔다.
- 교육 ID는 Package, Test Class, Catalog, Log, 매뉴얼에서 `EDU-ADM-04`로 동일하게 사용한다.
- 예제 때문에 Runtime Owner Module에 고객 업무 상태·Table·Controller를 추가하지 않는다.
- 기존 Public API·SPI·Owner Port가 있으면 그것을 사용한다. 없으면 Owner Module에 재사용 가능한 Extension을 먼저 구현하고 교육 예제는 그 Extension의 Consumer가 된다.

## 입력·계약

Query/Command/Operation 계약에 businessId, operationId, expectedVersion, reason, approvalId, actor, correlationId를 필요한 경우 포함한다.

## 상태와 저장 근거

조회형은 상태 변경 없음. Command형은 REQUESTED→ACCEPTED→RUNNING→SUCCEEDED|FAILED|UNKNOWN_RESULT→RECONCILED.

업무 ID, Operation ID, 요청 Hash, Version, Actor, Reason, Correlation ID를 적용 가능한 범위에서 같은 처리 근거로 연결한다.

## 정상 처리 순서

1. Preview·영향·승인·실행·감사를 연결한다.
2. 권한·Data Scope·Masking을 Backend에서 적용한다.
3. Command 전 영향과 현재 Version을 표시한다.
4. 응답 후 업무 Owner 상태와 Audit를 다시 조회한다.
5. 운영 매뉴얼의 실제 Field·Button·상태와 맞춘다.

## 예외·장애·경계 사례

| 사례 | 구현·검증 요구 |
|---|---|
| 자기 승인 | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |
| 승인 만료 | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |
| 승인 범위 초과 | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |
| 대상 변경 | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |

## 필수 Test

- Backend Contract Test
- Same-JVM/Remote Adapter Test
- 권한·Masking Test
- Timeout·응답 유실 Test
- Browser Test
- Audit·Trace 연결

## 운영·화면 확인

04 ADM 운영자 매뉴얼의 해당 메뉴에서 조회자·운영자·승인자 권한별로 실행한다.

- Log·Metric·Trace·Audit에서 같은 업무 ID와 Operation ID를 검색한다.
- 화면의 Field·Button·상태·오류 메시지가 매뉴얼 설명과 일치해야 한다.
- 응답 유실 또는 부분 실패가 있는 요청은 새 요청 전에 Reconcile 경로를 제공한다.

## 완료 조건

- ADM이 고객 DB를 직접 수정하지 않는다.
- 화면 메시지와 실제 Owner 상태가 일치한다.
- 부분 성공·미확정을 단순 실패 또는 성공으로 합치지 않는다.
- 실행 명령, 입력값, 기대 결과, 장애 재현, 복구 명령을 매뉴얼의 같은 EDU ID에 반영한다.
- 직접 실행하지 않은 DB Vendor·Browser·다중 인스턴스·장애 Test를 성공으로 기록하지 않는다.

## 금지 사항

- 메모리 Map과 고정 JSON만 반환하는 예제로 종료
- 정상 Case 한 건만 Test
- 고객 업무 DB를 ADM·BZA·Gateway가 직접 수정
- 교육 Profile을 기본 Profile에서 자동 활성화
- 비밀번호·Token·개인정보 원문을 Source·Log·Fixture에 기록
- 기존 Package Convention과 다른 별도 `sample`, `demo`, `misc` Root 생성

---

# 35. EDU-ADM-05 — 비동기 작업·응답 유실

| 항목 | 내용 |
|---|---|
| 영역 | ADM 연동 |
| 우선순위 | P0 |
| 연결 매뉴얼 | 03 ADM 개발자 매뉴얼·04 ADM 운영자 매뉴얼 |
| 구현 단위 | 하나의 독립 Pull Request 또는 상호 원자적인 작업 묶음 |

## 필요 근거

ADM은 고객 업무를 개발하는 곳이 아니라 완성된 고객 업무를 조회·통제·승인·대사하는 Control Plane이므로 연동 경계를 예제로 보여야 한다.

## 고객 업무 시나리오

대량 Export 또는 재처리 Operation을 접수하고 진행률·결과를 조회한다.

## 구현 위치와 책임 경계

고객 업무 Source는 `cpf-education/src/main/java/com/cpf/reference/edu/adm/adm05/asyncoperation/`. ADM에는 기존 Owner Port·Generated Client·Feature Slice 규칙에 맞춘 Adapter와 화면 연결만 둔다. 고객 지급 원장 로직을 `cpf-admin`에 넣지 않는다.

- Resource·Fixture: 요청·응답 Fixture와 Browser Scenario는 `cpf-education/src/main/resources/edu/edu/adm/05/` 및 `cpf-admin`의 기존 Test 구조에 둔다.
- 교육 ID는 Package, Test Class, Catalog, Log, 매뉴얼에서 `EDU-ADM-05`로 동일하게 사용한다.
- 예제 때문에 Runtime Owner Module에 고객 업무 상태·Table·Controller를 추가하지 않는다.
- 기존 Public API·SPI·Owner Port가 있으면 그것을 사용한다. 없으면 Owner Module에 재사용 가능한 Extension을 먼저 구현하고 교육 예제는 그 Extension의 Consumer가 된다.

## 입력·계약

Query/Command/Operation 계약에 businessId, operationId, expectedVersion, reason, approvalId, actor, correlationId를 필요한 경우 포함한다.

## 상태와 저장 근거

조회형은 상태 변경 없음. Command형은 REQUESTED→ACCEPTED→RUNNING→SUCCEEDED|FAILED|UNKNOWN_RESULT→RECONCILED.

업무 ID, Operation ID, 요청 Hash, Version, Actor, Reason, Correlation ID를 적용 가능한 범위에서 같은 처리 근거로 연결한다.

## 정상 처리 순서

1. Browser 종료 후에도 operationId로 상태를 복원한다.
2. 권한·Data Scope·Masking을 Backend에서 적용한다.
3. Command 전 영향과 현재 Version을 표시한다.
4. 응답 후 업무 Owner 상태와 Audit를 다시 조회한다.
5. 운영 매뉴얼의 실제 Field·Button·상태와 맞춘다.

## 예외·장애·경계 사례

| 사례 | 구현·검증 요구 |
|---|---|
| 접수 응답 유실 | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |
| Polling 실패 | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |
| 중복 Operation | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |
| UNKNOWN_RESULT | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |

## 필수 Test

- Backend Contract Test
- Same-JVM/Remote Adapter Test
- 권한·Masking Test
- Timeout·응답 유실 Test
- Browser Test
- Audit·Trace 연결

## 운영·화면 확인

04 ADM 운영자 매뉴얼의 해당 메뉴에서 조회자·운영자·승인자 권한별로 실행한다.

- Log·Metric·Trace·Audit에서 같은 업무 ID와 Operation ID를 검색한다.
- 화면의 Field·Button·상태·오류 메시지가 매뉴얼 설명과 일치해야 한다.
- 응답 유실 또는 부분 실패가 있는 요청은 새 요청 전에 Reconcile 경로를 제공한다.

## 완료 조건

- ADM이 고객 DB를 직접 수정하지 않는다.
- 화면 메시지와 실제 Owner 상태가 일치한다.
- 부분 성공·미확정을 단순 실패 또는 성공으로 합치지 않는다.
- 실행 명령, 입력값, 기대 결과, 장애 재현, 복구 명령을 매뉴얼의 같은 EDU ID에 반영한다.
- 직접 실행하지 않은 DB Vendor·Browser·다중 인스턴스·장애 Test를 성공으로 기록하지 않는다.

## 금지 사항

- 메모리 Map과 고정 JSON만 반환하는 예제로 종료
- 정상 Case 한 건만 Test
- 고객 업무 DB를 ADM·BZA·Gateway가 직접 수정
- 교육 Profile을 기본 Profile에서 자동 활성화
- 비밀번호·Token·개인정보 원문을 Source·Log·Fixture에 기록
- 기존 Package Convention과 다른 별도 `sample`, `demo`, `misc` Root 생성

---

# 36. EDU-ADM-06 — 부분 성공·대상별 복구

| 항목 | 내용 |
|---|---|
| 영역 | ADM 연동 |
| 우선순위 | P0 |
| 연결 매뉴얼 | 03 ADM 개발자 매뉴얼·04 ADM 운영자 매뉴얼 |
| 구현 단위 | 하나의 독립 Pull Request 또는 상호 원자적인 작업 묶음 |

## 필요 근거

ADM은 고객 업무를 개발하는 곳이 아니라 완성된 고객 업무를 조회·통제·승인·대사하는 Control Plane이므로 연동 경계를 예제로 보여야 한다.

## 고객 업무 시나리오

10개 대상 중 7 성공·2 실패·1 미확정을 분리하고 실패 대상만 재처리한다.

## 구현 위치와 책임 경계

고객 업무 Source는 `cpf-education/src/main/java/com/cpf/reference/edu/adm/adm06/partialrecovery/`. ADM에는 기존 Owner Port·Generated Client·Feature Slice 규칙에 맞춘 Adapter와 화면 연결만 둔다. 고객 지급 원장 로직을 `cpf-admin`에 넣지 않는다.

- Resource·Fixture: 요청·응답 Fixture와 Browser Scenario는 `cpf-education/src/main/resources/edu/edu/adm/06/` 및 `cpf-admin`의 기존 Test 구조에 둔다.
- 교육 ID는 Package, Test Class, Catalog, Log, 매뉴얼에서 `EDU-ADM-06`로 동일하게 사용한다.
- 예제 때문에 Runtime Owner Module에 고객 업무 상태·Table·Controller를 추가하지 않는다.
- 기존 Public API·SPI·Owner Port가 있으면 그것을 사용한다. 없으면 Owner Module에 재사용 가능한 Extension을 먼저 구현하고 교육 예제는 그 Extension의 Consumer가 된다.

## 입력·계약

Query/Command/Operation 계약에 businessId, operationId, expectedVersion, reason, approvalId, actor, correlationId를 필요한 경우 포함한다.

## 상태와 저장 근거

조회형은 상태 변경 없음. Command형은 REQUESTED→ACCEPTED→RUNNING→SUCCEEDED|FAILED|UNKNOWN_RESULT→RECONCILED.

업무 ID, Operation ID, 요청 Hash, Version, Actor, Reason, Correlation ID를 적용 가능한 범위에서 같은 처리 근거로 연결한다.

## 정상 처리 순서

1. 대상별 Result와 Retry/Reconcile/Rollback을 구분한다.
2. 권한·Data Scope·Masking을 Backend에서 적용한다.
3. Command 전 영향과 현재 Version을 표시한다.
4. 응답 후 업무 Owner 상태와 Audit를 다시 조회한다.
5. 운영 매뉴얼의 실제 Field·Button·상태와 맞춘다.

## 예외·장애·경계 사례

| 사례 | 구현·검증 요구 |
|---|---|
| 일부 대상 Timeout | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |
| 늦은 성공 | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |
| Rollback 일부 실패 | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |
| 전체 성공으로 오판 | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |

## 필수 Test

- Backend Contract Test
- Same-JVM/Remote Adapter Test
- 권한·Masking Test
- Timeout·응답 유실 Test
- Browser Test
- Audit·Trace 연결

## 운영·화면 확인

04 ADM 운영자 매뉴얼의 해당 메뉴에서 조회자·운영자·승인자 권한별로 실행한다.

- Log·Metric·Trace·Audit에서 같은 업무 ID와 Operation ID를 검색한다.
- 화면의 Field·Button·상태·오류 메시지가 매뉴얼 설명과 일치해야 한다.
- 응답 유실 또는 부분 실패가 있는 요청은 새 요청 전에 Reconcile 경로를 제공한다.

## 완료 조건

- ADM이 고객 DB를 직접 수정하지 않는다.
- 화면 메시지와 실제 Owner 상태가 일치한다.
- 부분 성공·미확정을 단순 실패 또는 성공으로 합치지 않는다.
- 실행 명령, 입력값, 기대 결과, 장애 재현, 복구 명령을 매뉴얼의 같은 EDU ID에 반영한다.
- 직접 실행하지 않은 DB Vendor·Browser·다중 인스턴스·장애 Test를 성공으로 기록하지 않는다.

## 금지 사항

- 메모리 Map과 고정 JSON만 반환하는 예제로 종료
- 정상 Case 한 건만 Test
- 고객 업무 DB를 ADM·BZA·Gateway가 직접 수정
- 교육 Profile을 기본 Profile에서 자동 활성화
- 비밀번호·Token·개인정보 원문을 Source·Log·Fixture에 기록
- 기존 Package Convention과 다른 별도 `sample`, `demo`, `misc` Root 생성

---

# 37. EDU-ADM-07 — 고객 전용 화면 추가의 마지막 선택

| 항목 | 내용 |
|---|---|
| 영역 | ADM 연동 |
| 우선순위 | P2 |
| 연결 매뉴얼 | 03 ADM 개발자 매뉴얼·04 ADM 운영자 매뉴얼 |
| 구현 단위 | 하나의 독립 Pull Request 또는 상호 원자적인 작업 묶음 |

## 필요 근거

ADM은 고객 업무를 개발하는 곳이 아니라 완성된 고객 업무를 조회·통제·승인·대사하는 Control Plane이므로 연동 경계를 예제로 보여야 한다.

## 고객 업무 시나리오

기존 ADM으로 해결되지 않는 고객 고유 지급 분쟁 조정 화면을 추가한다.

## 구현 위치와 책임 경계

고객 업무 Source는 `cpf-education/src/main/java/com/cpf/reference/edu/adm/adm07/customscreen/`. ADM에는 기존 Owner Port·Generated Client·Feature Slice 규칙에 맞춘 Adapter와 화면 연결만 둔다. 고객 지급 원장 로직을 `cpf-admin`에 넣지 않는다.

- Resource·Fixture: 요청·응답 Fixture와 Browser Scenario는 `cpf-education/src/main/resources/edu/edu/adm/07/` 및 `cpf-admin`의 기존 Test 구조에 둔다.
- 교육 ID는 Package, Test Class, Catalog, Log, 매뉴얼에서 `EDU-ADM-07`로 동일하게 사용한다.
- 예제 때문에 Runtime Owner Module에 고객 업무 상태·Table·Controller를 추가하지 않는다.
- 기존 Public API·SPI·Owner Port가 있으면 그것을 사용한다. 없으면 Owner Module에 재사용 가능한 Extension을 먼저 구현하고 교육 예제는 그 Extension의 Consumer가 된다.

## 입력·계약

Query/Command/Operation 계약에 businessId, operationId, expectedVersion, reason, approvalId, actor, correlationId를 필요한 경우 포함한다.

## 상태와 저장 근거

조회형은 상태 변경 없음. Command형은 REQUESTED→ACCEPTED→RUNNING→SUCCEEDED|FAILED|UNKNOWN_RESULT→RECONCILED.

업무 ID, Operation ID, 요청 Hash, Version, Actor, Reason, Correlation ID를 적용 가능한 범위에서 같은 처리 근거로 연결한다.

## 정상 처리 순서

1. Public Extension·Generated Client·Route·Permission·Browser Test를 따른다.
2. 권한·Data Scope·Masking을 Backend에서 적용한다.
3. Command 전 영향과 현재 Version을 표시한다.
4. 응답 후 업무 Owner 상태와 Audit를 다시 조회한다.
5. 운영 매뉴얼의 실제 Field·Button·상태와 맞춘다.

## 예외·장애·경계 사례

| 사례 | 구현·검증 요구 |
|---|---|
| 기존 기능 중복 | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |
| 직접 DB 수정 | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |
| 권한 누락 | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |
| Frontend/Backend 계약 불일치 | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |

## 필수 Test

- Backend Contract Test
- Same-JVM/Remote Adapter Test
- 권한·Masking Test
- Timeout·응답 유실 Test
- Browser Test
- Audit·Trace 연결

## 운영·화면 확인

04 ADM 운영자 매뉴얼의 해당 메뉴에서 조회자·운영자·승인자 권한별로 실행한다.

- Log·Metric·Trace·Audit에서 같은 업무 ID와 Operation ID를 검색한다.
- 화면의 Field·Button·상태·오류 메시지가 매뉴얼 설명과 일치해야 한다.
- 응답 유실 또는 부분 실패가 있는 요청은 새 요청 전에 Reconcile 경로를 제공한다.

## 완료 조건

- ADM이 고객 DB를 직접 수정하지 않는다.
- 화면 메시지와 실제 Owner 상태가 일치한다.
- 부분 성공·미확정을 단순 실패 또는 성공으로 합치지 않는다.
- 실행 명령, 입력값, 기대 결과, 장애 재현, 복구 명령을 매뉴얼의 같은 EDU ID에 반영한다.
- 직접 실행하지 않은 DB Vendor·Browser·다중 인스턴스·장애 Test를 성공으로 기록하지 않는다.

## 금지 사항

- 메모리 Map과 고정 JSON만 반환하는 예제로 종료
- 정상 Case 한 건만 Test
- 고객 업무 DB를 ADM·BZA·Gateway가 직접 수정
- 교육 Profile을 기본 Profile에서 자동 활성화
- 비밀번호·Token·개인정보 원문을 Source·Log·Fixture에 기록
- 기존 Package Convention과 다른 별도 `sample`, `demo`, `misc` Root 생성

---

# 38. EDU-BZA-01 — 조직·직원·발령·기준일

| 항목 | 내용 |
|---|---|
| 영역 | BZA |
| 우선순위 | P1 |
| 연결 매뉴얼 | 90 BZA 매뉴얼 |
| 구현 단위 | 하나의 독립 Pull Request 또는 상호 원자적인 작업 묶음 |

## 필요 근거

고객사는 BZA 내부를 개발하지 않고 조직·사용자·권한·결재 기능을 자기 업무에 연결하므로 기준일·유효기간·Snapshot·감사를 실제로 확인해야 한다.

## 고객 업무 시나리오

본사/지점 조직과 직원 발령·겸직·파견을 기준일로 조회한다.

## 구현 위치와 책임 경계

고객 연동 예제는 `cpf-education/src/main/java/com/cpf/reference/edu/bza/bza01/organization/`. 재사용 가능한 BZA 제품 기능 변경만 `cpf-biz-admin` Owner Module에 반영한다. 고객 지급·계약 로직을 BZA에 넣지 않는다.

- Resource·Fixture: 조직·사용자·정책·결재 Fixture는 `cpf-education/src/main/resources/edu/edu/bza/01/`와 중앙 Vendor Pack Seed에 둔다.
- 교육 ID는 Package, Test Class, Catalog, Log, 매뉴얼에서 `EDU-BZA-01`로 동일하게 사용한다.
- 예제 때문에 Runtime Owner Module에 고객 업무 상태·Table·Controller를 추가하지 않는다.
- 기존 Public API·SPI·Owner Port가 있으면 그것을 사용한다. 없으면 Owner Module에 재사용 가능한 Extension을 먼저 구현하고 교육 예제는 그 Extension의 Consumer가 된다.

## 입력·계약

주요 입력: organizationId, employeeId, effectiveFrom/To. 모든 변경에 operationId, expectedVersion, reason, actor를 적용 가능한 범위에서 포함한다.

## 상태와 저장 근거

기준정보는 유효기간과 Version을 가진다. 결재는 DRAFT→SUBMITTED→APPROVED|REJECTED|WITHDRAWN|CANCELLED.

업무 ID, Operation ID, 요청 Hash, Version, Actor, Reason, Correlation ID를 적용 가능한 범위에서 같은 처리 근거로 연결한다.

## 정상 처리 순서

1. 기준일로 실효 조직·권한·결재자를 계산한다.
2. 업무 요청 시 정책 Version과 결재선 Snapshot을 저장한다.
3. 변경은 예상 Version과 감사 근거를 사용한다.
4. 고객 업무 반영은 승인 결과를 소비하는 업무 Owner가 수행한다.

## 예외·장애·경계 사례

| 사례 | 구현·검증 요구 |
|---|---|
| 미래 발령 | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |
| 겹치는 발령 | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |
| 조직 폐쇄 | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |
| 기준일 경계 | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |

## 필수 Test

- 기준일 경계
- 유효기간 겹침
- 권한 Matrix
- Version 충돌
- 응답 유실 대사
- Browser 메뉴 Field·Button
- 감사·Download 권한

## 운영·화면 확인

90 BZA 매뉴얼의 실제 메뉴에서 기준일·상태·Version·감사·알림을 확인한다.

- Log·Metric·Trace·Audit에서 같은 업무 ID와 Operation ID를 검색한다.
- 화면의 Field·Button·상태·오류 메시지가 매뉴얼 설명과 일치해야 한다.
- 응답 유실 또는 부분 실패가 있는 요청은 새 요청 전에 Reconcile 경로를 제공한다.

## 완료 조건

- 결재선이 실행 중 정책 변경으로 바뀌지 않는다.
- 기존 비밀번호 원문을 조회·재표시하지 않는다.
- 고객 업무와 BZA 책임 경계가 분리된다.
- 실행 명령, 입력값, 기대 결과, 장애 재현, 복구 명령을 매뉴얼의 같은 EDU ID에 반영한다.
- 직접 실행하지 않은 DB Vendor·Browser·다중 인스턴스·장애 Test를 성공으로 기록하지 않는다.

## 금지 사항

- 메모리 Map과 고정 JSON만 반환하는 예제로 종료
- 정상 Case 한 건만 Test
- 고객 업무 DB를 ADM·BZA·Gateway가 직접 수정
- 교육 Profile을 기본 Profile에서 자동 활성화
- 비밀번호·Token·개인정보 원문을 Source·Log·Fixture에 기록
- 기존 Package Convention과 다른 별도 `sample`, `demo`, `misc` Root 생성

---

# 39. EDU-BZA-02 — 사용자·역할·권한·실효 권한

| 항목 | 내용 |
|---|---|
| 영역 | BZA |
| 우선순위 | P1 |
| 연결 매뉴얼 | 90 BZA 매뉴얼 |
| 구현 단위 | 하나의 독립 Pull Request 또는 상호 원자적인 작업 묶음 |

## 필요 근거

고객사는 BZA 내부를 개발하지 않고 조직·사용자·권한·결재 기능을 자기 업무에 연결하므로 기준일·유효기간·Snapshot·감사를 실제로 확인해야 한다.

## 고객 업무 시나리오

사용자에게 역할을 유효기간으로 부여하고 메뉴·버튼·API·Data Scope 실효 권한을 계산한다.

## 구현 위치와 책임 경계

고객 연동 예제는 `cpf-education/src/main/java/com/cpf/reference/edu/bza/bza02/authorization/`. 재사용 가능한 BZA 제품 기능 변경만 `cpf-biz-admin` Owner Module에 반영한다. 고객 지급·계약 로직을 BZA에 넣지 않는다.

- Resource·Fixture: 조직·사용자·정책·결재 Fixture는 `cpf-education/src/main/resources/edu/edu/bza/02/`와 중앙 Vendor Pack Seed에 둔다.
- 교육 ID는 Package, Test Class, Catalog, Log, 매뉴얼에서 `EDU-BZA-02`로 동일하게 사용한다.
- 예제 때문에 Runtime Owner Module에 고객 업무 상태·Table·Controller를 추가하지 않는다.
- 기존 Public API·SPI·Owner Port가 있으면 그것을 사용한다. 없으면 Owner Module에 재사용 가능한 Extension을 먼저 구현하고 교육 예제는 그 Extension의 Consumer가 된다.

## 입력·계약

주요 입력: userId, roleId, validFrom/To, dataScope. 모든 변경에 operationId, expectedVersion, reason, actor를 적용 가능한 범위에서 포함한다.

## 상태와 저장 근거

기준정보는 유효기간과 Version을 가진다. 결재는 DRAFT→SUBMITTED→APPROVED|REJECTED|WITHDRAWN|CANCELLED.

업무 ID, Operation ID, 요청 Hash, Version, Actor, Reason, Correlation ID를 적용 가능한 범위에서 같은 처리 근거로 연결한다.

## 정상 처리 순서

1. 기준일로 실효 조직·권한·결재자를 계산한다.
2. 업무 요청 시 정책 Version과 결재선 Snapshot을 저장한다.
3. 변경은 예상 Version과 감사 근거를 사용한다.
4. 고객 업무 반영은 승인 결과를 소비하는 업무 Owner가 수행한다.

## 예외·장애·경계 사례

| 사례 | 구현·검증 요구 |
|---|---|
| 권한 없음 | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |
| 만료 역할 | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |
| 직접 URL | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |
| 역할 중복 | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |
| 원문 조회 | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |

## 필수 Test

- 기준일 경계
- 유효기간 겹침
- 권한 Matrix
- Version 충돌
- 응답 유실 대사
- Browser 메뉴 Field·Button
- 감사·Download 권한

## 운영·화면 확인

90 BZA 매뉴얼의 실제 메뉴에서 기준일·상태·Version·감사·알림을 확인한다.

- Log·Metric·Trace·Audit에서 같은 업무 ID와 Operation ID를 검색한다.
- 화면의 Field·Button·상태·오류 메시지가 매뉴얼 설명과 일치해야 한다.
- 응답 유실 또는 부분 실패가 있는 요청은 새 요청 전에 Reconcile 경로를 제공한다.

## 완료 조건

- 결재선이 실행 중 정책 변경으로 바뀌지 않는다.
- 기존 비밀번호 원문을 조회·재표시하지 않는다.
- 고객 업무와 BZA 책임 경계가 분리된다.
- 실행 명령, 입력값, 기대 결과, 장애 재현, 복구 명령을 매뉴얼의 같은 EDU ID에 반영한다.
- 직접 실행하지 않은 DB Vendor·Browser·다중 인스턴스·장애 Test를 성공으로 기록하지 않는다.

## 금지 사항

- 메모리 Map과 고정 JSON만 반환하는 예제로 종료
- 정상 Case 한 건만 Test
- 고객 업무 DB를 ADM·BZA·Gateway가 직접 수정
- 교육 Profile을 기본 Profile에서 자동 활성화
- 비밀번호·Token·개인정보 원문을 Source·Log·Fixture에 기록
- 기존 Package Convention과 다른 별도 `sample`, `demo`, `misc` Root 생성

---

# 40. EDU-BZA-03 — 결재정책 Version·경로 사전 계산

| 항목 | 내용 |
|---|---|
| 영역 | BZA |
| 우선순위 | P1 |
| 연결 매뉴얼 | 90 BZA 매뉴얼 |
| 구현 단위 | 하나의 독립 Pull Request 또는 상호 원자적인 작업 묶음 |

## 필요 근거

고객사는 BZA 내부를 개발하지 않고 조직·사용자·권한·결재 기능을 자기 업무에 연결하므로 기준일·유효기간·Snapshot·감사를 실제로 확인해야 한다.

## 고객 업무 시나리오

금액·조직·업무 유형에 따라 결재선을 사전 계산한다.

## 구현 위치와 책임 경계

고객 연동 예제는 `cpf-education/src/main/java/com/cpf/reference/edu/bza/bza03/policysimulation/`. 재사용 가능한 BZA 제품 기능 변경만 `cpf-biz-admin` Owner Module에 반영한다. 고객 지급·계약 로직을 BZA에 넣지 않는다.

- Resource·Fixture: 조직·사용자·정책·결재 Fixture는 `cpf-education/src/main/resources/edu/edu/bza/03/`와 중앙 Vendor Pack Seed에 둔다.
- 교육 ID는 Package, Test Class, Catalog, Log, 매뉴얼에서 `EDU-BZA-03`로 동일하게 사용한다.
- 예제 때문에 Runtime Owner Module에 고객 업무 상태·Table·Controller를 추가하지 않는다.
- 기존 Public API·SPI·Owner Port가 있으면 그것을 사용한다. 없으면 Owner Module에 재사용 가능한 Extension을 먼저 구현하고 교육 예제는 그 Extension의 Consumer가 된다.

## 입력·계약

주요 입력: policyVersion, businessType, amount, organizationId. 모든 변경에 operationId, expectedVersion, reason, actor를 적용 가능한 범위에서 포함한다.

## 상태와 저장 근거

기준정보는 유효기간과 Version을 가진다. 결재는 DRAFT→SUBMITTED→APPROVED|REJECTED|WITHDRAWN|CANCELLED.

업무 ID, Operation ID, 요청 Hash, Version, Actor, Reason, Correlation ID를 적용 가능한 범위에서 같은 처리 근거로 연결한다.

## 정상 처리 순서

1. 기준일로 실효 조직·권한·결재자를 계산한다.
2. 업무 요청 시 정책 Version과 결재선 Snapshot을 저장한다.
3. 변경은 예상 Version과 감사 근거를 사용한다.
4. 고객 업무 반영은 승인 결과를 소비하는 업무 Owner가 수행한다.

## 예외·장애·경계 사례

| 사례 | 구현·검증 요구 |
|---|---|
| 정책 없음 | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |
| 다중 정책 충돌 | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |
| 미래 Version | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |
| 조직장 부재 | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |

## 필수 Test

- 기준일 경계
- 유효기간 겹침
- 권한 Matrix
- Version 충돌
- 응답 유실 대사
- Browser 메뉴 Field·Button
- 감사·Download 권한

## 운영·화면 확인

90 BZA 매뉴얼의 실제 메뉴에서 기준일·상태·Version·감사·알림을 확인한다.

- Log·Metric·Trace·Audit에서 같은 업무 ID와 Operation ID를 검색한다.
- 화면의 Field·Button·상태·오류 메시지가 매뉴얼 설명과 일치해야 한다.
- 응답 유실 또는 부분 실패가 있는 요청은 새 요청 전에 Reconcile 경로를 제공한다.

## 완료 조건

- 결재선이 실행 중 정책 변경으로 바뀌지 않는다.
- 기존 비밀번호 원문을 조회·재표시하지 않는다.
- 고객 업무와 BZA 책임 경계가 분리된다.
- 실행 명령, 입력값, 기대 결과, 장애 재현, 복구 명령을 매뉴얼의 같은 EDU ID에 반영한다.
- 직접 실행하지 않은 DB Vendor·Browser·다중 인스턴스·장애 Test를 성공으로 기록하지 않는다.

## 금지 사항

- 메모리 Map과 고정 JSON만 반환하는 예제로 종료
- 정상 Case 한 건만 Test
- 고객 업무 DB를 ADM·BZA·Gateway가 직접 수정
- 교육 Profile을 기본 Profile에서 자동 활성화
- 비밀번호·Token·개인정보 원문을 Source·Log·Fixture에 기록
- 기존 Package Convention과 다른 별도 `sample`, `demo`, `misc` Root 생성

---

# 41. EDU-BZA-04 — 상신·승인·반려·철회·취소

| 항목 | 내용 |
|---|---|
| 영역 | BZA |
| 우선순위 | P1 |
| 연결 매뉴얼 | 90 BZA 매뉴얼 |
| 구현 단위 | 하나의 독립 Pull Request 또는 상호 원자적인 작업 묶음 |

## 필요 근거

고객사는 BZA 내부를 개발하지 않고 조직·사용자·권한·결재 기능을 자기 업무에 연결하므로 기준일·유효기간·Snapshot·감사를 실제로 확인해야 한다.

## 고객 업무 시나리오

지급 변경 요청을 상신하고 승인·반려·철회·취소·재상신한다.

## 구현 위치와 책임 경계

고객 연동 예제는 `cpf-education/src/main/java/com/cpf/reference/edu/bza/bza04/approvalflow/`. 재사용 가능한 BZA 제품 기능 변경만 `cpf-biz-admin` Owner Module에 반영한다. 고객 지급·계약 로직을 BZA에 넣지 않는다.

- Resource·Fixture: 조직·사용자·정책·결재 Fixture는 `cpf-education/src/main/resources/edu/edu/bza/04/`와 중앙 Vendor Pack Seed에 둔다.
- 교육 ID는 Package, Test Class, Catalog, Log, 매뉴얼에서 `EDU-BZA-04`로 동일하게 사용한다.
- 예제 때문에 Runtime Owner Module에 고객 업무 상태·Table·Controller를 추가하지 않는다.
- 기존 Public API·SPI·Owner Port가 있으면 그것을 사용한다. 없으면 Owner Module에 재사용 가능한 Extension을 먼저 구현하고 교육 예제는 그 Extension의 Consumer가 된다.

## 입력·계약

주요 입력: approvalId, requestVersion, reason, decision. 모든 변경에 operationId, expectedVersion, reason, actor를 적용 가능한 범위에서 포함한다.

## 상태와 저장 근거

기준정보는 유효기간과 Version을 가진다. 결재는 DRAFT→SUBMITTED→APPROVED|REJECTED|WITHDRAWN|CANCELLED.

업무 ID, Operation ID, 요청 Hash, Version, Actor, Reason, Correlation ID를 적용 가능한 범위에서 같은 처리 근거로 연결한다.

## 정상 처리 순서

1. 기준일로 실효 조직·권한·결재자를 계산한다.
2. 업무 요청 시 정책 Version과 결재선 Snapshot을 저장한다.
3. 변경은 예상 Version과 감사 근거를 사용한다.
4. 고객 업무 반영은 승인 결과를 소비하는 업무 Owner가 수행한다.

## 예외·장애·경계 사례

| 사례 | 구현·검증 요구 |
|---|---|
| 중복 승인 | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |
| Version 충돌 | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |
| 응답 유실 | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |
| 만료 승인 | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |
| 자기 승인 | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |

## 필수 Test

- 기준일 경계
- 유효기간 겹침
- 권한 Matrix
- Version 충돌
- 응답 유실 대사
- Browser 메뉴 Field·Button
- 감사·Download 권한

## 운영·화면 확인

90 BZA 매뉴얼의 실제 메뉴에서 기준일·상태·Version·감사·알림을 확인한다.

- Log·Metric·Trace·Audit에서 같은 업무 ID와 Operation ID를 검색한다.
- 화면의 Field·Button·상태·오류 메시지가 매뉴얼 설명과 일치해야 한다.
- 응답 유실 또는 부분 실패가 있는 요청은 새 요청 전에 Reconcile 경로를 제공한다.

## 완료 조건

- 결재선이 실행 중 정책 변경으로 바뀌지 않는다.
- 기존 비밀번호 원문을 조회·재표시하지 않는다.
- 고객 업무와 BZA 책임 경계가 분리된다.
- 실행 명령, 입력값, 기대 결과, 장애 재현, 복구 명령을 매뉴얼의 같은 EDU ID에 반영한다.
- 직접 실행하지 않은 DB Vendor·Browser·다중 인스턴스·장애 Test를 성공으로 기록하지 않는다.

## 금지 사항

- 메모리 Map과 고정 JSON만 반환하는 예제로 종료
- 정상 Case 한 건만 Test
- 고객 업무 DB를 ADM·BZA·Gateway가 직접 수정
- 교육 Profile을 기본 Profile에서 자동 활성화
- 비밀번호·Token·개인정보 원문을 Source·Log·Fixture에 기록
- 기존 Package Convention과 다른 별도 `sample`, `demo`, `misc` Root 생성

---

# 42. EDU-BZA-05 — 위임·대결·대행 책임

| 항목 | 내용 |
|---|---|
| 영역 | BZA |
| 우선순위 | P1 |
| 연결 매뉴얼 | 90 BZA 매뉴얼 |
| 구현 단위 | 하나의 독립 Pull Request 또는 상호 원자적인 작업 묶음 |

## 필요 근거

고객사는 BZA 내부를 개발하지 않고 조직·사용자·권한·결재 기능을 자기 업무에 연결하므로 기준일·유효기간·Snapshot·감사를 실제로 확인해야 한다.

## 고객 업무 시나리오

승인자 부재 기간에 유효한 위임과 대결자를 적용한다.

## 구현 위치와 책임 경계

고객 연동 예제는 `cpf-education/src/main/java/com/cpf/reference/edu/bza/bza05/delegation/`. 재사용 가능한 BZA 제품 기능 변경만 `cpf-biz-admin` Owner Module에 반영한다. 고객 지급·계약 로직을 BZA에 넣지 않는다.

- Resource·Fixture: 조직·사용자·정책·결재 Fixture는 `cpf-education/src/main/resources/edu/edu/bza/05/`와 중앙 Vendor Pack Seed에 둔다.
- 교육 ID는 Package, Test Class, Catalog, Log, 매뉴얼에서 `EDU-BZA-05`로 동일하게 사용한다.
- 예제 때문에 Runtime Owner Module에 고객 업무 상태·Table·Controller를 추가하지 않는다.
- 기존 Public API·SPI·Owner Port가 있으면 그것을 사용한다. 없으면 Owner Module에 재사용 가능한 Extension을 먼저 구현하고 교육 예제는 그 Extension의 Consumer가 된다.

## 입력·계약

주요 입력: delegator, delegate, validFrom/To, scope. 모든 변경에 operationId, expectedVersion, reason, actor를 적용 가능한 범위에서 포함한다.

## 상태와 저장 근거

기준정보는 유효기간과 Version을 가진다. 결재는 DRAFT→SUBMITTED→APPROVED|REJECTED|WITHDRAWN|CANCELLED.

업무 ID, Operation ID, 요청 Hash, Version, Actor, Reason, Correlation ID를 적용 가능한 범위에서 같은 처리 근거로 연결한다.

## 정상 처리 순서

1. 기준일로 실효 조직·권한·결재자를 계산한다.
2. 업무 요청 시 정책 Version과 결재선 Snapshot을 저장한다.
3. 변경은 예상 Version과 감사 근거를 사용한다.
4. 고객 업무 반영은 승인 결과를 소비하는 업무 Owner가 수행한다.

## 예외·장애·경계 사례

| 사례 | 구현·검증 요구 |
|---|---|
| 기간 겹침 | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |
| 순환 위임 | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |
| 범위 초과 | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |
| 만료 후 승인 | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |

## 필수 Test

- 기준일 경계
- 유효기간 겹침
- 권한 Matrix
- Version 충돌
- 응답 유실 대사
- Browser 메뉴 Field·Button
- 감사·Download 권한

## 운영·화면 확인

90 BZA 매뉴얼의 실제 메뉴에서 기준일·상태·Version·감사·알림을 확인한다.

- Log·Metric·Trace·Audit에서 같은 업무 ID와 Operation ID를 검색한다.
- 화면의 Field·Button·상태·오류 메시지가 매뉴얼 설명과 일치해야 한다.
- 응답 유실 또는 부분 실패가 있는 요청은 새 요청 전에 Reconcile 경로를 제공한다.

## 완료 조건

- 결재선이 실행 중 정책 변경으로 바뀌지 않는다.
- 기존 비밀번호 원문을 조회·재표시하지 않는다.
- 고객 업무와 BZA 책임 경계가 분리된다.
- 실행 명령, 입력값, 기대 결과, 장애 재현, 복구 명령을 매뉴얼의 같은 EDU ID에 반영한다.
- 직접 실행하지 않은 DB Vendor·Browser·다중 인스턴스·장애 Test를 성공으로 기록하지 않는다.

## 금지 사항

- 메모리 Map과 고정 JSON만 반환하는 예제로 종료
- 정상 Case 한 건만 Test
- 고객 업무 DB를 ADM·BZA·Gateway가 직접 수정
- 교육 Profile을 기본 Profile에서 자동 활성화
- 비밀번호·Token·개인정보 원문을 Source·Log·Fixture에 기록
- 기존 Package Convention과 다른 별도 `sample`, `demo`, `misc` Root 생성

---

# 43. EDU-BZA-06 — 첨부·알림·감사·다운로드

| 항목 | 내용 |
|---|---|
| 영역 | BZA |
| 우선순위 | P1 |
| 연결 매뉴얼 | 90 BZA 매뉴얼 |
| 구현 단위 | 하나의 독립 Pull Request 또는 상호 원자적인 작업 묶음 |

## 필요 근거

고객사는 BZA 내부를 개발하지 않고 조직·사용자·권한·결재 기능을 자기 업무에 연결하므로 기준일·유효기간·Snapshot·감사를 실제로 확인해야 한다.

## 고객 업무 시나리오

결재 요청에 첨부를 연결하고 알림·감사·다운로드를 같은 업무 ID로 확인한다.

## 구현 위치와 책임 경계

고객 연동 예제는 `cpf-education/src/main/java/com/cpf/reference/edu/bza/bza06/evidence/`. 재사용 가능한 BZA 제품 기능 변경만 `cpf-biz-admin` Owner Module에 반영한다. 고객 지급·계약 로직을 BZA에 넣지 않는다.

- Resource·Fixture: 조직·사용자·정책·결재 Fixture는 `cpf-education/src/main/resources/edu/edu/bza/06/`와 중앙 Vendor Pack Seed에 둔다.
- 교육 ID는 Package, Test Class, Catalog, Log, 매뉴얼에서 `EDU-BZA-06`로 동일하게 사용한다.
- 예제 때문에 Runtime Owner Module에 고객 업무 상태·Table·Controller를 추가하지 않는다.
- 기존 Public API·SPI·Owner Port가 있으면 그것을 사용한다. 없으면 Owner Module에 재사용 가능한 Extension을 먼저 구현하고 교육 예제는 그 Extension의 Consumer가 된다.

## 입력·계약

주요 입력: approvalId, attachmentId, notificationId. 모든 변경에 operationId, expectedVersion, reason, actor를 적용 가능한 범위에서 포함한다.

## 상태와 저장 근거

기준정보는 유효기간과 Version을 가진다. 결재는 DRAFT→SUBMITTED→APPROVED|REJECTED|WITHDRAWN|CANCELLED.

업무 ID, Operation ID, 요청 Hash, Version, Actor, Reason, Correlation ID를 적용 가능한 범위에서 같은 처리 근거로 연결한다.

## 정상 처리 순서

1. 기준일로 실효 조직·권한·결재자를 계산한다.
2. 업무 요청 시 정책 Version과 결재선 Snapshot을 저장한다.
3. 변경은 예상 Version과 감사 근거를 사용한다.
4. 고객 업무 반영은 승인 결과를 소비하는 업무 Owner가 수행한다.

## 예외·장애·경계 사례

| 사례 | 구현·검증 요구 |
|---|---|
| 첨부 검사 실패 | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |
| 알림 실패 | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |
| 만료 다운로드 | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |
| 권한 없음 | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |

## 필수 Test

- 기준일 경계
- 유효기간 겹침
- 권한 Matrix
- Version 충돌
- 응답 유실 대사
- Browser 메뉴 Field·Button
- 감사·Download 권한

## 운영·화면 확인

90 BZA 매뉴얼의 실제 메뉴에서 기준일·상태·Version·감사·알림을 확인한다.

- Log·Metric·Trace·Audit에서 같은 업무 ID와 Operation ID를 검색한다.
- 화면의 Field·Button·상태·오류 메시지가 매뉴얼 설명과 일치해야 한다.
- 응답 유실 또는 부분 실패가 있는 요청은 새 요청 전에 Reconcile 경로를 제공한다.

## 완료 조건

- 결재선이 실행 중 정책 변경으로 바뀌지 않는다.
- 기존 비밀번호 원문을 조회·재표시하지 않는다.
- 고객 업무와 BZA 책임 경계가 분리된다.
- 실행 명령, 입력값, 기대 결과, 장애 재현, 복구 명령을 매뉴얼의 같은 EDU ID에 반영한다.
- 직접 실행하지 않은 DB Vendor·Browser·다중 인스턴스·장애 Test를 성공으로 기록하지 않는다.

## 금지 사항

- 메모리 Map과 고정 JSON만 반환하는 예제로 종료
- 정상 Case 한 건만 Test
- 고객 업무 DB를 ADM·BZA·Gateway가 직접 수정
- 교육 Profile을 기본 Profile에서 자동 활성화
- 비밀번호·Token·개인정보 원문을 Source·Log·Fixture에 기록
- 기존 Package Convention과 다른 별도 `sample`, `demo`, `misc` Root 생성

---

# 44. EDU-GW-01 — Server Group·Health·Load Balancing

| 항목 | 내용 |
|---|---|
| 영역 | Gateway |
| 우선순위 | P1 |
| 연결 매뉴얼 | 91 Gateway 매뉴얼·04 ADM 운영자 매뉴얼 Gateway 메뉴 |
| 구현 단위 | 하나의 독립 Pull Request 또는 상호 원자적인 작업 묶음 |

## 필요 근거

고객은 Gateway를 개발하는 것이 아니라 자기 API를 등록·검증·게시·운영하므로 Target 서비스와 Control Plane의 전체 흐름을 예제로 확인해야 한다.

## 고객 업무 시나리오

회원 서비스 2개 인스턴스를 Server Group으로 등록하고 비정상 인스턴스를 제외한다.

## 구현 위치와 책임 경계

대상 고객 API는 `cpf-education/src/main/java/com/cpf/reference/edu/gateway/servergroup/`. Gateway Runtime에는 Mock 업무 로직을 넣지 않고 기존 Route·Policy·Publish Owner 구조를 사용한다.

- Resource·Fixture: Route Package, 인증 Fixture, Failure Stub은 `cpf-education/src/main/resources/edu/edu/gw/01/`와 Gateway 기존 Test Fixture 구조에 둔다.
- 교육 ID는 Package, Test Class, Catalog, Log, 매뉴얼에서 `EDU-GW-01`로 동일하게 사용한다.
- 예제 때문에 Runtime Owner Module에 고객 업무 상태·Table·Controller를 추가하지 않는다.
- 기존 Public API·SPI·Owner Port가 있으면 그것을 사용한다. 없으면 Owner Module에 재사용 가능한 Extension을 먼저 구현하고 교육 예제는 그 Extension의 Consumer가 된다.

## 입력·계약

주요 입력: serverGroupId, member endpoint, weight, health path. Route/Policy Package에 version, checksum, environment, reason, approvalId를 포함한다.

## 상태와 저장 근거

DRAFT→VALIDATED→APPROVAL_REQUESTED→APPROVED→PUBLISHING→APPLIED|PARTIAL|REJECTED→ROLLED_BACK.

업무 ID, Operation ID, 요청 Hash, Version, Actor, Reason, Correlation ID를 적용 가능한 범위에서 같은 처리 근거로 연결한다.

## 정상 처리 순서

1. Target 직접 호출 Baseline 확인
2. Server Group·Route·Policy Draft 작성
3. 정적·동적 연결시험
4. 변경 차이와 LKG를 포함해 승인
5. 게시 후 인스턴스별 ACK·Version·Checksum 대사
6. 대표 요청과 Trace 확인

## 예외·장애·경계 사례

| 사례 | 구현·검증 요구 |
|---|---|
| Health 실패 | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |
| 늦은 복귀 | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |
| 모든 Member Down | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |
| Weight 불균형 | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |

## 필수 Test

- Target 직접/경유 비교
- 인증·권한 실패 구분
- Timeout·Retry 단계
- 부분 적용·Reconcile
- LKG Rollback
- 다중 인스턴스 Drift
- Browser Publish Flow

## 운영·화면 확인

ADM Gateway Dashboard·Routes·Apply Status·Transactions·Health에서 같은 routeId/attemptId를 확인한다.

- Log·Metric·Trace·Audit에서 같은 업무 ID와 Operation ID를 검색한다.
- 화면의 Field·Button·상태·오류 메시지가 매뉴얼 설명과 일치해야 한다.
- 응답 유실 또는 부분 실패가 있는 요청은 새 요청 전에 Reconcile 경로를 제공한다.

## 완료 조건

- 모든 처리 인스턴스 Version·Checksum 일치 또는 PARTIAL 명시
- 변경 요청 전송 뒤 무조건 재시도하지 않음
- LKG 복구 후 적용 상태 재대사
- 실행 명령, 입력값, 기대 결과, 장애 재현, 복구 명령을 매뉴얼의 같은 EDU ID에 반영한다.
- 직접 실행하지 않은 DB Vendor·Browser·다중 인스턴스·장애 Test를 성공으로 기록하지 않는다.

## 금지 사항

- 메모리 Map과 고정 JSON만 반환하는 예제로 종료
- 정상 Case 한 건만 Test
- 고객 업무 DB를 ADM·BZA·Gateway가 직접 수정
- 교육 Profile을 기본 Profile에서 자동 활성화
- 비밀번호·Token·개인정보 원문을 Source·Log·Fixture에 기록
- 기존 Package Convention과 다른 별도 `sample`, `demo`, `misc` Root 생성

---

# 45. EDU-GW-02 — Route·Predicate·Path Rewrite

| 항목 | 내용 |
|---|---|
| 영역 | Gateway |
| 우선순위 | P1 |
| 연결 매뉴얼 | 91 Gateway 매뉴얼·04 ADM 운영자 매뉴얼 Gateway 메뉴 |
| 구현 단위 | 하나의 독립 Pull Request 또는 상호 원자적인 작업 묶음 |

## 필요 근거

고객은 Gateway를 개발하는 것이 아니라 자기 API를 등록·검증·게시·운영하므로 Target 서비스와 Control Plane의 전체 흐름을 예제로 확인해야 한다.

## 고객 업무 시나리오

외부 `/customer/v1/members/{id}`를 내부 `/internal/members/{id}`로 전달한다.

## 구현 위치와 책임 경계

대상 고객 API는 `cpf-education/src/main/java/com/cpf/reference/edu/gateway/route/`. Gateway Runtime에는 Mock 업무 로직을 넣지 않고 기존 Route·Policy·Publish Owner 구조를 사용한다.

- Resource·Fixture: Route Package, 인증 Fixture, Failure Stub은 `cpf-education/src/main/resources/edu/edu/gw/02/`와 Gateway 기존 Test Fixture 구조에 둔다.
- 교육 ID는 Package, Test Class, Catalog, Log, 매뉴얼에서 `EDU-GW-02`로 동일하게 사용한다.
- 예제 때문에 Runtime Owner Module에 고객 업무 상태·Table·Controller를 추가하지 않는다.
- 기존 Public API·SPI·Owner Port가 있으면 그것을 사용한다. 없으면 Owner Module에 재사용 가능한 Extension을 먼저 구현하고 교육 예제는 그 Extension의 Consumer가 된다.

## 입력·계약

주요 입력: host, path, method, apiVersion, targetGroup. Route/Policy Package에 version, checksum, environment, reason, approvalId를 포함한다.

## 상태와 저장 근거

DRAFT→VALIDATED→APPROVAL_REQUESTED→APPROVED→PUBLISHING→APPLIED|PARTIAL|REJECTED→ROLLED_BACK.

업무 ID, Operation ID, 요청 Hash, Version, Actor, Reason, Correlation ID를 적용 가능한 범위에서 같은 처리 근거로 연결한다.

## 정상 처리 순서

1. Target 직접 호출 Baseline 확인
2. Server Group·Route·Policy Draft 작성
3. 정적·동적 연결시험
4. 변경 차이와 LKG를 포함해 승인
5. 게시 후 인스턴스별 ACK·Version·Checksum 대사
6. 대표 요청과 Trace 확인

## 예외·장애·경계 사례

| 사례 | 구현·검증 요구 |
|---|---|
| Route 중복 | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |
| Path 변수 불일치 | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |
| 금지 Header | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |
| 잘못된 Target | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |

## 필수 Test

- Target 직접/경유 비교
- 인증·권한 실패 구분
- Timeout·Retry 단계
- 부분 적용·Reconcile
- LKG Rollback
- 다중 인스턴스 Drift
- Browser Publish Flow

## 운영·화면 확인

ADM Gateway Dashboard·Routes·Apply Status·Transactions·Health에서 같은 routeId/attemptId를 확인한다.

- Log·Metric·Trace·Audit에서 같은 업무 ID와 Operation ID를 검색한다.
- 화면의 Field·Button·상태·오류 메시지가 매뉴얼 설명과 일치해야 한다.
- 응답 유실 또는 부분 실패가 있는 요청은 새 요청 전에 Reconcile 경로를 제공한다.

## 완료 조건

- 모든 처리 인스턴스 Version·Checksum 일치 또는 PARTIAL 명시
- 변경 요청 전송 뒤 무조건 재시도하지 않음
- LKG 복구 후 적용 상태 재대사
- 실행 명령, 입력값, 기대 결과, 장애 재현, 복구 명령을 매뉴얼의 같은 EDU ID에 반영한다.
- 직접 실행하지 않은 DB Vendor·Browser·다중 인스턴스·장애 Test를 성공으로 기록하지 않는다.

## 금지 사항

- 메모리 Map과 고정 JSON만 반환하는 예제로 종료
- 정상 Case 한 건만 Test
- 고객 업무 DB를 ADM·BZA·Gateway가 직접 수정
- 교육 Profile을 기본 Profile에서 자동 활성화
- 비밀번호·Token·개인정보 원문을 Source·Log·Fixture에 기록
- 기존 Package Convention과 다른 별도 `sample`, `demo`, `misc` Root 생성

---

# 46. EDU-GW-03 — 인증·권한·TLS·HMAC·Nonce

| 항목 | 내용 |
|---|---|
| 영역 | Gateway |
| 우선순위 | P1 |
| 연결 매뉴얼 | 91 Gateway 매뉴얼·04 ADM 운영자 매뉴얼 Gateway 메뉴 |
| 구현 단위 | 하나의 독립 Pull Request 또는 상호 원자적인 작업 묶음 |

## 필요 근거

고객은 Gateway를 개발하는 것이 아니라 자기 API를 등록·검증·게시·운영하므로 Target 서비스와 Control Plane의 전체 흐름을 예제로 확인해야 한다.

## 고객 업무 시나리오

Access Token과 MEMBER_READ 권한, TLS, 선택적 HMAC/Body Hash/Nonce를 검증한다.

## 구현 위치와 책임 경계

대상 고객 API는 `cpf-education/src/main/java/com/cpf/reference/edu/gateway/security/`. Gateway Runtime에는 Mock 업무 로직을 넣지 않고 기존 Route·Policy·Publish Owner 구조를 사용한다.

- Resource·Fixture: Route Package, 인증 Fixture, Failure Stub은 `cpf-education/src/main/resources/edu/edu/gw/03/`와 Gateway 기존 Test Fixture 구조에 둔다.
- 교육 ID는 Package, Test Class, Catalog, Log, 매뉴얼에서 `EDU-GW-03`로 동일하게 사용한다.
- 예제 때문에 Runtime Owner Module에 고객 업무 상태·Table·Controller를 추가하지 않는다.
- 기존 Public API·SPI·Owner Port가 있으면 그것을 사용한다. 없으면 Owner Module에 재사용 가능한 Extension을 먼저 구현하고 교육 예제는 그 Extension의 Consumer가 된다.

## 입력·계약

주요 입력: auth policy, audience, permission, certificate alias. Route/Policy Package에 version, checksum, environment, reason, approvalId를 포함한다.

## 상태와 저장 근거

DRAFT→VALIDATED→APPROVAL_REQUESTED→APPROVED→PUBLISHING→APPLIED|PARTIAL|REJECTED→ROLLED_BACK.

업무 ID, Operation ID, 요청 Hash, Version, Actor, Reason, Correlation ID를 적용 가능한 범위에서 같은 처리 근거로 연결한다.

## 정상 처리 순서

1. Target 직접 호출 Baseline 확인
2. Server Group·Route·Policy Draft 작성
3. 정적·동적 연결시험
4. 변경 차이와 LKG를 포함해 승인
5. 게시 후 인스턴스별 ACK·Version·Checksum 대사
6. 대표 요청과 Trace 확인

## 예외·장애·경계 사례

| 사례 | 구현·검증 요구 |
|---|---|
| Token 만료 | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |
| Audience 불일치 | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |
| 권한 없음 | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |
| Body Hash 오류 | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |
| Nonce 재사용 | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |

## 필수 Test

- Target 직접/경유 비교
- 인증·권한 실패 구분
- Timeout·Retry 단계
- 부분 적용·Reconcile
- LKG Rollback
- 다중 인스턴스 Drift
- Browser Publish Flow

## 운영·화면 확인

ADM Gateway Dashboard·Routes·Apply Status·Transactions·Health에서 같은 routeId/attemptId를 확인한다.

- Log·Metric·Trace·Audit에서 같은 업무 ID와 Operation ID를 검색한다.
- 화면의 Field·Button·상태·오류 메시지가 매뉴얼 설명과 일치해야 한다.
- 응답 유실 또는 부분 실패가 있는 요청은 새 요청 전에 Reconcile 경로를 제공한다.

## 완료 조건

- 모든 처리 인스턴스 Version·Checksum 일치 또는 PARTIAL 명시
- 변경 요청 전송 뒤 무조건 재시도하지 않음
- LKG 복구 후 적용 상태 재대사
- 실행 명령, 입력값, 기대 결과, 장애 재현, 복구 명령을 매뉴얼의 같은 EDU ID에 반영한다.
- 직접 실행하지 않은 DB Vendor·Browser·다중 인스턴스·장애 Test를 성공으로 기록하지 않는다.

## 금지 사항

- 메모리 Map과 고정 JSON만 반환하는 예제로 종료
- 정상 Case 한 건만 Test
- 고객 업무 DB를 ADM·BZA·Gateway가 직접 수정
- 교육 Profile을 기본 Profile에서 자동 활성화
- 비밀번호·Token·개인정보 원문을 Source·Log·Fixture에 기록
- 기존 Package Convention과 다른 별도 `sample`, `demo`, `misc` Root 생성

---

# 47. EDU-GW-04 — Timeout·Retry·Circuit Breaker·Bulkhead

| 항목 | 내용 |
|---|---|
| 영역 | Gateway |
| 우선순위 | P1 |
| 연결 매뉴얼 | 91 Gateway 매뉴얼·04 ADM 운영자 매뉴얼 Gateway 메뉴 |
| 구현 단위 | 하나의 독립 Pull Request 또는 상호 원자적인 작업 묶음 |

## 필요 근거

고객은 Gateway를 개발하는 것이 아니라 자기 API를 등록·검증·게시·운영하므로 Target 서비스와 Control Plane의 전체 흐름을 예제로 확인해야 한다.

## 고객 업무 시나리오

조회 API는 연결 전 실패에만 제한 재시도하고 변경 API는 전송 뒤 재시도하지 않는다.

## 구현 위치와 책임 경계

대상 고객 API는 `cpf-education/src/main/java/com/cpf/reference/edu/gateway/resilience/`. Gateway Runtime에는 Mock 업무 로직을 넣지 않고 기존 Route·Policy·Publish Owner 구조를 사용한다.

- Resource·Fixture: Route Package, 인증 Fixture, Failure Stub은 `cpf-education/src/main/resources/edu/edu/gw/04/`와 Gateway 기존 Test Fixture 구조에 둔다.
- 교육 ID는 Package, Test Class, Catalog, Log, 매뉴얼에서 `EDU-GW-04`로 동일하게 사용한다.
- 예제 때문에 Runtime Owner Module에 고객 업무 상태·Table·Controller를 추가하지 않는다.
- 기존 Public API·SPI·Owner Port가 있으면 그것을 사용한다. 없으면 Owner Module에 재사용 가능한 Extension을 먼저 구현하고 교육 예제는 그 Extension의 Consumer가 된다.

## 입력·계약

주요 입력: connect/read/total timeout, retry, circuit, bulkhead. Route/Policy Package에 version, checksum, environment, reason, approvalId를 포함한다.

## 상태와 저장 근거

DRAFT→VALIDATED→APPROVAL_REQUESTED→APPROVED→PUBLISHING→APPLIED|PARTIAL|REJECTED→ROLLED_BACK.

업무 ID, Operation ID, 요청 Hash, Version, Actor, Reason, Correlation ID를 적용 가능한 범위에서 같은 처리 근거로 연결한다.

## 정상 처리 순서

1. Target 직접 호출 Baseline 확인
2. Server Group·Route·Policy Draft 작성
3. 정적·동적 연결시험
4. 변경 차이와 LKG를 포함해 승인
5. 게시 후 인스턴스별 ACK·Version·Checksum 대사
6. 대표 요청과 Trace 확인

## 예외·장애·경계 사례

| 사례 | 구현·검증 요구 |
|---|---|
| Connect Timeout | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |
| Read Timeout | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |
| 5xx | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |
| Circuit Open | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |
| Queue Full | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |

## 필수 Test

- Target 직접/경유 비교
- 인증·권한 실패 구분
- Timeout·Retry 단계
- 부분 적용·Reconcile
- LKG Rollback
- 다중 인스턴스 Drift
- Browser Publish Flow

## 운영·화면 확인

ADM Gateway Dashboard·Routes·Apply Status·Transactions·Health에서 같은 routeId/attemptId를 확인한다.

- Log·Metric·Trace·Audit에서 같은 업무 ID와 Operation ID를 검색한다.
- 화면의 Field·Button·상태·오류 메시지가 매뉴얼 설명과 일치해야 한다.
- 응답 유실 또는 부분 실패가 있는 요청은 새 요청 전에 Reconcile 경로를 제공한다.

## 완료 조건

- 모든 처리 인스턴스 Version·Checksum 일치 또는 PARTIAL 명시
- 변경 요청 전송 뒤 무조건 재시도하지 않음
- LKG 복구 후 적용 상태 재대사
- 실행 명령, 입력값, 기대 결과, 장애 재현, 복구 명령을 매뉴얼의 같은 EDU ID에 반영한다.
- 직접 실행하지 않은 DB Vendor·Browser·다중 인스턴스·장애 Test를 성공으로 기록하지 않는다.

## 금지 사항

- 메모리 Map과 고정 JSON만 반환하는 예제로 종료
- 정상 Case 한 건만 Test
- 고객 업무 DB를 ADM·BZA·Gateway가 직접 수정
- 교육 Profile을 기본 Profile에서 자동 활성화
- 비밀번호·Token·개인정보 원문을 Source·Log·Fixture에 기록
- 기존 Package Convention과 다른 별도 `sample`, `demo`, `misc` Root 생성

---

# 48. EDU-GW-05 — Draft·검증·승인·게시·부분 적용

| 항목 | 내용 |
|---|---|
| 영역 | Gateway |
| 우선순위 | P1 |
| 연결 매뉴얼 | 91 Gateway 매뉴얼·04 ADM 운영자 매뉴얼 Gateway 메뉴 |
| 구현 단위 | 하나의 독립 Pull Request 또는 상호 원자적인 작업 묶음 |

## 필요 근거

고객은 Gateway를 개발하는 것이 아니라 자기 API를 등록·검증·게시·운영하므로 Target 서비스와 Control Plane의 전체 흐름을 예제로 확인해야 한다.

## 고객 업무 시나리오

Route Version을 승인 후 게시하고 모든 Gateway 인스턴스의 Version·Checksum을 대사한다.

## 구현 위치와 책임 경계

대상 고객 API는 `cpf-education/src/main/java/com/cpf/reference/edu/gateway/publish/`. Gateway Runtime에는 Mock 업무 로직을 넣지 않고 기존 Route·Policy·Publish Owner 구조를 사용한다.

- Resource·Fixture: Route Package, 인증 Fixture, Failure Stub은 `cpf-education/src/main/resources/edu/edu/gw/05/`와 Gateway 기존 Test Fixture 구조에 둔다.
- 교육 ID는 Package, Test Class, Catalog, Log, 매뉴얼에서 `EDU-GW-05`로 동일하게 사용한다.
- 예제 때문에 Runtime Owner Module에 고객 업무 상태·Table·Controller를 추가하지 않는다.
- 기존 Public API·SPI·Owner Port가 있으면 그것을 사용한다. 없으면 Owner Module에 재사용 가능한 Extension을 먼저 구현하고 교육 예제는 그 Extension의 Consumer가 된다.

## 입력·계약

주요 입력: routeVersion, checksum, approvalId, target instances. Route/Policy Package에 version, checksum, environment, reason, approvalId를 포함한다.

## 상태와 저장 근거

DRAFT→VALIDATED→APPROVAL_REQUESTED→APPROVED→PUBLISHING→APPLIED|PARTIAL|REJECTED→ROLLED_BACK.

업무 ID, Operation ID, 요청 Hash, Version, Actor, Reason, Correlation ID를 적용 가능한 범위에서 같은 처리 근거로 연결한다.

## 정상 처리 순서

1. Target 직접 호출 Baseline 확인
2. Server Group·Route·Policy Draft 작성
3. 정적·동적 연결시험
4. 변경 차이와 LKG를 포함해 승인
5. 게시 후 인스턴스별 ACK·Version·Checksum 대사
6. 대표 요청과 Trace 확인

## 예외·장애·경계 사례

| 사례 | 구현·검증 요구 |
|---|---|
| NACK | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |
| 일부 ACK | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |
| 승인 만료 | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |
| Checksum 불일치 | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |
| 게시 응답 유실 | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |

## 필수 Test

- Target 직접/경유 비교
- 인증·권한 실패 구분
- Timeout·Retry 단계
- 부분 적용·Reconcile
- LKG Rollback
- 다중 인스턴스 Drift
- Browser Publish Flow

## 운영·화면 확인

ADM Gateway Dashboard·Routes·Apply Status·Transactions·Health에서 같은 routeId/attemptId를 확인한다.

- Log·Metric·Trace·Audit에서 같은 업무 ID와 Operation ID를 검색한다.
- 화면의 Field·Button·상태·오류 메시지가 매뉴얼 설명과 일치해야 한다.
- 응답 유실 또는 부분 실패가 있는 요청은 새 요청 전에 Reconcile 경로를 제공한다.

## 완료 조건

- 모든 처리 인스턴스 Version·Checksum 일치 또는 PARTIAL 명시
- 변경 요청 전송 뒤 무조건 재시도하지 않음
- LKG 복구 후 적용 상태 재대사
- 실행 명령, 입력값, 기대 결과, 장애 재현, 복구 명령을 매뉴얼의 같은 EDU ID에 반영한다.
- 직접 실행하지 않은 DB Vendor·Browser·다중 인스턴스·장애 Test를 성공으로 기록하지 않는다.

## 금지 사항

- 메모리 Map과 고정 JSON만 반환하는 예제로 종료
- 정상 Case 한 건만 Test
- 고객 업무 DB를 ADM·BZA·Gateway가 직접 수정
- 교육 Profile을 기본 Profile에서 자동 활성화
- 비밀번호·Token·개인정보 원문을 Source·Log·Fixture에 기록
- 기존 Package Convention과 다른 별도 `sample`, `demo`, `misc` Root 생성

---

# 49. EDU-GW-06 — Attempt Ledger·UNKNOWN_RESULT·LKG 복구

| 항목 | 내용 |
|---|---|
| 영역 | Gateway |
| 우선순위 | P1 |
| 연결 매뉴얼 | 91 Gateway 매뉴얼·04 ADM 운영자 매뉴얼 Gateway 메뉴 |
| 구현 단위 | 하나의 독립 Pull Request 또는 상호 원자적인 작업 묶음 |

## 필요 근거

고객은 Gateway를 개발하는 것이 아니라 자기 API를 등록·검증·게시·운영하므로 Target 서비스와 Control Plane의 전체 흐름을 예제로 확인해야 한다.

## 고객 업무 시나리오

Gateway 응답 유실 시 Attempt와 고객 업무 결과를 대사하고 이전 정상 Version으로 복구한다.

## 구현 위치와 책임 경계

대상 고객 API는 `cpf-education/src/main/java/com/cpf/reference/edu/gateway/reconcile/`. Gateway Runtime에는 Mock 업무 로직을 넣지 않고 기존 Route·Policy·Publish Owner 구조를 사용한다.

- Resource·Fixture: Route Package, 인증 Fixture, Failure Stub은 `cpf-education/src/main/resources/edu/edu/gw/06/`와 Gateway 기존 Test Fixture 구조에 둔다.
- 교육 ID는 Package, Test Class, Catalog, Log, 매뉴얼에서 `EDU-GW-06`로 동일하게 사용한다.
- 예제 때문에 Runtime Owner Module에 고객 업무 상태·Table·Controller를 추가하지 않는다.
- 기존 Public API·SPI·Owner Port가 있으면 그것을 사용한다. 없으면 Owner Module에 재사용 가능한 Extension을 먼저 구현하고 교육 예제는 그 Extension의 Consumer가 된다.

## 입력·계약

주요 입력: attemptId, operationId, routeVersion, requestHash. Route/Policy Package에 version, checksum, environment, reason, approvalId를 포함한다.

## 상태와 저장 근거

DRAFT→VALIDATED→APPROVAL_REQUESTED→APPROVED→PUBLISHING→APPLIED|PARTIAL|REJECTED→ROLLED_BACK.

업무 ID, Operation ID, 요청 Hash, Version, Actor, Reason, Correlation ID를 적용 가능한 범위에서 같은 처리 근거로 연결한다.

## 정상 처리 순서

1. Target 직접 호출 Baseline 확인
2. Server Group·Route·Policy Draft 작성
3. 정적·동적 연결시험
4. 변경 차이와 LKG를 포함해 승인
5. 게시 후 인스턴스별 ACK·Version·Checksum 대사
6. 대표 요청과 Trace 확인

## 예외·장애·경계 사례

| 사례 | 구현·검증 요구 |
|---|---|
| Target 처리 후 응답 유실 | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |
| 늦은 응답 | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |
| 중복 요청 | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |
| LKG 일부 적용 | 상태·DB·외부 Side Effect·Retry 가능 여부·운영자 다음 행동을 명시하고 자동 Test로 재현한다. |

## 필수 Test

- Target 직접/경유 비교
- 인증·권한 실패 구분
- Timeout·Retry 단계
- 부분 적용·Reconcile
- LKG Rollback
- 다중 인스턴스 Drift
- Browser Publish Flow

## 운영·화면 확인

ADM Gateway Dashboard·Routes·Apply Status·Transactions·Health에서 같은 routeId/attemptId를 확인한다.

- Log·Metric·Trace·Audit에서 같은 업무 ID와 Operation ID를 검색한다.
- 화면의 Field·Button·상태·오류 메시지가 매뉴얼 설명과 일치해야 한다.
- 응답 유실 또는 부분 실패가 있는 요청은 새 요청 전에 Reconcile 경로를 제공한다.

## 완료 조건

- 모든 처리 인스턴스 Version·Checksum 일치 또는 PARTIAL 명시
- 변경 요청 전송 뒤 무조건 재시도하지 않음
- LKG 복구 후 적용 상태 재대사
- 실행 명령, 입력값, 기대 결과, 장애 재현, 복구 명령을 매뉴얼의 같은 EDU ID에 반영한다.
- 직접 실행하지 않은 DB Vendor·Browser·다중 인스턴스·장애 Test를 성공으로 기록하지 않는다.

## 금지 사항

- 메모리 Map과 고정 JSON만 반환하는 예제로 종료
- 정상 Case 한 건만 Test
- 고객 업무 DB를 ADM·BZA·Gateway가 직접 수정
- 교육 Profile을 기본 Profile에서 자동 활성화
- 비밀번호·Token·개인정보 원문을 Source·Log·Fixture에 기록
- 기존 Package Convention과 다른 별도 `sample`, `demo`, `misc` Root 생성

---

# 50. EDU-DEV-16 — 대용량 목록 검색·정렬·Cursor Paging

- 우선순위: **P0**
- 연결 매뉴얼: `01_개발자매뉴얼.md`
- Owner: `cpf-education`
- 구현 상태 목표: Source·SQL·API·Config·Test·운영 확인·매뉴얼 연결이 모두 완료

## 필요 근거

- 고객 매뉴얼에서 `대용량 목록 검색·정렬·Cursor Paging`을 설명하려면 정상 호출 예시만이 아니라 상태 변화와 장애 후 정상화까지 재현 가능한 실행 예제가 필요하다.
- 이 요구사항이 없으면 고객은 기능 가능 여부를 알더라도 실제 업무 적용 순서, 실패 판정, 재처리 금지 조건을 확인할 수 없다.
- 내부 구현 구조를 보여 주기 위한 예제가 아니라 고객 업무를 CPF로 만드는 방법과 운영 결과를 증명하는 예제로 구현한다.

## 고객 업무 시나리오

고객 상담원이 수백만 건의 거래를 날짜·고객·상태로 검색하고 다음 페이지를 중복·누락 없이 이동한다.

## 구현 위치와 책임 경계

- 업무 Source: `com.cpf.education.edu.dev16` 또는 같은 의미의 기존 표준 Slice
- Test: Owner Module의 기존 Test Root 아래 `EDU-DEV-16` 식별 가능한 Class·Fixture
- SQL: 중앙 DB Vendor Pack의 기존 Install·Upgrade·Rollback·Verify·Seed 구조
- Config: 기존 Prefix·Profile·Secret 참조 방식을 유지
- 고객 업무 상태 변경과 원장 데이터는 고객 업무 Owner가 소유한다. ADM·BZA·Gateway·운영 Script가 업무 Table을 직접 수정하지 않는다.
- 공통 기능이 필요하면 기존 Public API·SPI를 우선 사용하고, 진입점이 없을 때만 실제 Consumer를 근거로 Owner Module에 확장한다.

## 입력·계약

- `검색기간`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `고객식별자`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `상태`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `정렬키`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `pageSize`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `cursor`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- 해당 기능에 필요한 Operation ID·업무 ID·Correlation ID·요청 Hash·사용자·권한 문맥을 저장·Log·Trace·Audit에서 연결한다.
- 요청·응답·Job Parameter·이벤트·파일 Layout 중 해당되는 계약을 OpenAPI 또는 Machine-readable Schema로 제공한다.

## 상태와 저장 근거

- 필수 상태 흐름: `QUERY_ACCEPTED → PAGE_RETURNED; cursor는 마지막 정렬키와 고유키를 포함`
- 상태마다 진입 조건, 허용 조치, 재시도 가능 여부, 최종 여부, 저장 위치와 Version 규칙을 정의한다.
- 결과 미확정·부분 성공이 가능한 경우 대상별 Attempt와 상대 상태 조회 근거를 별도 저장한다.

## 정상 처리 순서

1. 입력·권한·데이터 범위·기준 Version과 선행 상태를 검증한다.
2. 상태 변경·비동기 실행인 경우 Operation과 업무 식별자를 생성하거나 기존 멱등 요청을 조회한다. 조회 전용 기능은 조회 Context와 Cursor만 사용한다.
3. 상태 변경 기능은 고객 업무 Owner의 Application Use Case에서 Transaction 경계와 상태 전이를 수행하고, 조회 전용 기능은 일관된 Snapshot·정렬·범위 조건을 적용한다.
4. 외부 시스템·Kafka·파일·Batch·Gateway가 필요한 경우 Adapter 경계에서 Timeout·Retry·보안 정책을 적용한다.
5. 업무 결과와 Attempt·Audit·Log·Metric·Trace를 같은 식별자로 연결한다.
6. 고객 API 또는 운영 화면에서 최종 상태·건수·금액·Version·오류 0건을 확인한다.

## 예외·장애·경계 사례

- 동일 시각 데이터
- 조회 중 신규 등록
- 삭제·상태변경
- 잘못된 cursor
- 최대 pageSize 초과
- 상태 변경이 있는 경우 DB Commit 전 실패와 Commit 후 응답 유실을 구분한다.
- 같은 요청의 중복·동시 실행·순서 역전·늦은 응답을 포함한다.
- Retry 가능한 오류와 즉시 중단해야 하는 업무 오류를 구분한다.
- 대량·다중 대상 기능에서 부분 성공이 발생하면 성공 대상을 다시 실행하지 않고 실패 대상만 Reprocess·Reconcile한다.
- 복구·보상이 필요한 기능은 복구 실패와 보상 실패도 별도 상태·Attempt·Audit로 남긴다.

## 필수 Test

- 정렬 안정성
- 페이지 중복 0건
- 누락 0건
- 범위 권한
- DB 3종 실행계획
- 입력 Validation·권한·Data Scope·Masking Negative Test
- 해당 기능에 적용되는 중복·동시성·Timeout·응답 유실·부분 실패 Test
- 해당 기능의 Failure Injection 후 필요한 Retry·Restart·Reprocess·Reconcile·Rollback Test
- Oracle·PostgreSQL·MariaDB 또는 해당 Infra의 의미 동일성 Test
- Log·Metric·Trace·Audit에 Secret·개인정보 원문이 없는지 검사

## 운영·화면 확인

- ADM 거래 조회에서 동일 조건·건수·마지막 키를 확인
- 화면에는 해당 메뉴에 실제로 존재하는 조회 조건, 기본값, Column, 상세 Field, 상태, Button 활성 조건, 사유, 승인, 예상 Version, 대상별 결과만 Source와 일치하게 표시한다. 조회 전용 메뉴에 Command 항목을 만들지 않는다.
- 운영자가 정상화 완료를 판단할 수 있는 건수·금액·Version·Checksum·최종 상태·마지막 오류를 제공한다.
- 변경·외부연계 기능에서 응답 유실이 가능한 경우에만 요청을 다시 보내기 전에 Operation·거래·상대 상태를 조회하는 절차를 Browser 또는 API Test로 검증한다.

## 고객 매뉴얼·EDU 연결

- `01_개발자매뉴얼.md`의 해당 기능 장에 `EDU-DEV-16`를 연결한다.
- 선행 환경, 실행 명령, 입력값, 정상 출력, 오류 재현 명령, 복구 명령, ADM/BZA/Gateway 확인 위치를 그대로 따라 할 수 있게 작성한다.
- 교육 코드에서 고객 업무로 바꿀 Package·Table·API·Topic·파일명과 CPF 관리 영역을 구분한 전환표를 제공한다.

## 완료 조건

- `EDU-DEV-16`가 교육 Catalog·Source·Test·고객 매뉴얼에 동일 ID로 존재한다.
- 정상·Validation·권한·중복·동시성·Timeout·응답 유실·부분 실패·복구 Test가 통과한다.
- 요청·응답·상태·DB·이벤트·파일·운영 화면이 양방향으로 일치한다.
- 운영자가 다른 사람의 설명 없이 장애를 재현하고 정상화 판정을 할 수 있다.
- 실행하지 않은 Test는 성공으로 기록하지 않고, 실패·제약이 남으면 전체 완료로 보고하지 않는다.

## 금지 사항

- 메모리 Map·고정 JSON·정상 Case 한 건만으로 완료 처리
- 교육용 고객 업무를 `cpf-core`, `cpf-starters/common`, Starter, Runtime 제품 내부에 삽입
- Module-local DB Vendor Fallback SQL 또는 Vendor별 의미가 다른 Schema
- Timeout이나 응답 유실 뒤 실제 결과 확인 없이 무조건 재호출
- 비밀번호·Token·개인정보 원문을 Source·Log·Fixture·문서에 기록
- 매뉴얼 설명과 다른 임의 Package·API·상태·메뉴를 새로 생성

---

# 51. EDU-DEV-17 — 대량 등록 사전검증·부분 오류 보고·재업로드

- 우선순위: **P0**
- 연결 매뉴얼: `01_개발자매뉴얼.md`
- Owner: `cpf-education`
- 구현 상태 목표: Source·SQL·API·Config·Test·운영 확인·매뉴얼 연결이 모두 완료

## 필요 근거

- 고객 매뉴얼에서 `대량 등록 사전검증·부분 오류 보고·재업로드`을 설명하려면 정상 호출 예시만이 아니라 상태 변화와 장애 후 정상화까지 재현 가능한 실행 예제가 필요하다.
- 이 요구사항이 없으면 고객은 기능 가능 여부를 알더라도 실제 업무 적용 순서, 실패 판정, 재처리 금지 조건을 확인할 수 없다.
- 내부 구현 구조를 보여 주기 위한 예제가 아니라 고객 업무를 CPF로 만드는 방법과 운영 결과를 증명하는 예제로 구현한다.

## 고객 업무 시나리오

운영자가 회원 5만 건 CSV를 올리기 전에 검증하고 오류 행만 수정해 다시 올린다.

## 구현 위치와 책임 경계

- 업무 Source: `com.cpf.education.edu.dev17` 또는 같은 의미의 기존 표준 Slice
- Test: Owner Module의 기존 Test Root 아래 `EDU-DEV-17` 식별 가능한 Class·Fixture
- SQL: 중앙 DB Vendor Pack의 기존 Install·Upgrade·Rollback·Verify·Seed 구조
- Config: 기존 Prefix·Profile·Secret 참조 방식을 유지
- 고객 업무 상태 변경과 원장 데이터는 고객 업무 Owner가 소유한다. ADM·BZA·Gateway·운영 Script가 업무 Table을 직접 수정하지 않는다.
- 공통 기능이 필요하면 기존 Public API·SPI를 우선 사용하고, 진입점이 없을 때만 실제 Consumer를 근거로 Owner Module에 확장한다.

## 입력·계약

- `파일`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `업무일자`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `중복정책`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `dryRun`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `operationId`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- 해당 기능에 필요한 Operation ID·업무 ID·Correlation ID·요청 Hash·사용자·권한 문맥을 저장·Log·Trace·Audit에서 연결한다.
- 요청·응답·Job Parameter·이벤트·파일 Layout 중 해당되는 계약을 OpenAPI 또는 Machine-readable Schema로 제공한다.

## 상태와 저장 근거

- 필수 상태 흐름: `UPLOADED → VALIDATED/REJECTED → APPLIED/PARTIAL → RECONCILED`
- 상태마다 진입 조건, 허용 조치, 재시도 가능 여부, 최종 여부, 저장 위치와 Version 규칙을 정의한다.
- 결과 미확정·부분 성공이 가능한 경우 대상별 Attempt와 상대 상태 조회 근거를 별도 저장한다.

## 정상 처리 순서

1. 입력·권한·데이터 범위·기준 Version과 선행 상태를 검증한다.
2. 상태 변경·비동기 실행인 경우 Operation과 업무 식별자를 생성하거나 기존 멱등 요청을 조회한다. 조회 전용 기능은 조회 Context와 Cursor만 사용한다.
3. 상태 변경 기능은 고객 업무 Owner의 Application Use Case에서 Transaction 경계와 상태 전이를 수행하고, 조회 전용 기능은 일관된 Snapshot·정렬·범위 조건을 적용한다.
4. 외부 시스템·Kafka·파일·Batch·Gateway가 필요한 경우 Adapter 경계에서 Timeout·Retry·보안 정책을 적용한다.
5. 업무 결과와 Attempt·Audit·Log·Metric·Trace를 같은 식별자로 연결한다.
6. 고객 API 또는 운영 화면에서 최종 상태·건수·금액·Version·오류 0건을 확인한다.

## 예외·장애·경계 사례

- 헤더 불일치
- 행 중복
- DB 기존값 충돌
- 100행 중 3행 오류
- 적용 중 장애
- 재업로드
- 상태 변경이 있는 경우 DB Commit 전 실패와 Commit 후 응답 유실을 구분한다.
- 같은 요청의 중복·동시 실행·순서 역전·늦은 응답을 포함한다.
- Retry 가능한 오류와 즉시 중단해야 하는 업무 오류를 구분한다.
- 대량·다중 대상 기능에서 부분 성공이 발생하면 성공 대상을 다시 실행하지 않고 실패 대상만 Reprocess·Reconcile한다.
- 복구·보상이 필요한 기능은 복구 실패와 보상 실패도 별도 상태·Attempt·Audit로 남긴다.

## 필수 Test

- Dry Run 결과
- 오류 파일
- 부분 적용 금지/허용 정책
- 멱등 재업로드
- 감사
- 입력 Validation·권한·Data Scope·Masking Negative Test
- 해당 기능에 적용되는 중복·동시성·Timeout·응답 유실·부분 실패 Test
- 해당 기능의 Failure Injection 후 필요한 Retry·Restart·Reprocess·Reconcile·Rollback Test
- Oracle·PostgreSQL·MariaDB 또는 해당 Infra의 의미 동일성 Test
- Log·Metric·Trace·Audit에 Secret·개인정보 원문이 없는지 검사

## 운영·화면 확인

- ADM 파일 작업과 다운로드 감사에서 검증·적용·오류 파일을 연결
- 화면에는 해당 메뉴에 실제로 존재하는 조회 조건, 기본값, Column, 상세 Field, 상태, Button 활성 조건, 사유, 승인, 예상 Version, 대상별 결과만 Source와 일치하게 표시한다. 조회 전용 메뉴에 Command 항목을 만들지 않는다.
- 운영자가 정상화 완료를 판단할 수 있는 건수·금액·Version·Checksum·최종 상태·마지막 오류를 제공한다.
- 변경·외부연계 기능에서 응답 유실이 가능한 경우에만 요청을 다시 보내기 전에 Operation·거래·상대 상태를 조회하는 절차를 Browser 또는 API Test로 검증한다.

## 고객 매뉴얼·EDU 연결

- `01_개발자매뉴얼.md`의 해당 기능 장에 `EDU-DEV-17`를 연결한다.
- 선행 환경, 실행 명령, 입력값, 정상 출력, 오류 재현 명령, 복구 명령, ADM/BZA/Gateway 확인 위치를 그대로 따라 할 수 있게 작성한다.
- 교육 코드에서 고객 업무로 바꿀 Package·Table·API·Topic·파일명과 CPF 관리 영역을 구분한 전환표를 제공한다.

## 완료 조건

- `EDU-DEV-17`가 교육 Catalog·Source·Test·고객 매뉴얼에 동일 ID로 존재한다.
- 정상·Validation·권한·중복·동시성·Timeout·응답 유실·부분 실패·복구 Test가 통과한다.
- 요청·응답·상태·DB·이벤트·파일·운영 화면이 양방향으로 일치한다.
- 운영자가 다른 사람의 설명 없이 장애를 재현하고 정상화 판정을 할 수 있다.
- 실행하지 않은 Test는 성공으로 기록하지 않고, 실패·제약이 남으면 전체 완료로 보고하지 않는다.

## 금지 사항

- 메모리 Map·고정 JSON·정상 Case 한 건만으로 완료 처리
- 교육용 고객 업무를 `cpf-core`, `cpf-starters/common`, Starter, Runtime 제품 내부에 삽입
- Module-local DB Vendor Fallback SQL 또는 Vendor별 의미가 다른 Schema
- Timeout이나 응답 유실 뒤 실제 결과 확인 없이 무조건 재호출
- 비밀번호·Token·개인정보 원문을 Source·Log·Fixture·문서에 기록
- 매뉴얼 설명과 다른 임의 Package·API·상태·메뉴를 새로 생성

---

# 52. EDU-DEV-18 — 논리 삭제·복원·보존기간 만료

- 우선순위: **P0**
- 연결 매뉴얼: `01_개발자매뉴얼.md`
- Owner: `cpf-education`
- 구현 상태 목표: Source·SQL·API·Config·Test·운영 확인·매뉴얼 연결이 모두 완료

## 필요 근거

- 고객 매뉴얼에서 `논리 삭제·복원·보존기간 만료`을 설명하려면 정상 호출 예시만이 아니라 상태 변화와 장애 후 정상화까지 재현 가능한 실행 예제가 필요하다.
- 이 요구사항이 없으면 고객은 기능 가능 여부를 알더라도 실제 업무 적용 순서, 실패 판정, 재처리 금지 조건을 확인할 수 없다.
- 내부 구현 구조를 보여 주기 위한 예제가 아니라 고객 업무를 CPF로 만드는 방법과 운영 결과를 증명하는 예제로 구현한다.

## 고객 업무 시나리오

고객 계정을 즉시 물리 삭제하지 않고 비활성화한 뒤 보존기간 안에는 복원하고 만료 후 파기한다.

## 구현 위치와 책임 경계

- 업무 Source: `com.cpf.education.edu.dev18` 또는 같은 의미의 기존 표준 Slice
- Test: Owner Module의 기존 Test Root 아래 `EDU-DEV-18` 식별 가능한 Class·Fixture
- SQL: 중앙 DB Vendor Pack의 기존 Install·Upgrade·Rollback·Verify·Seed 구조
- Config: 기존 Prefix·Profile·Secret 참조 방식을 유지
- 고객 업무 상태 변경과 원장 데이터는 고객 업무 Owner가 소유한다. ADM·BZA·Gateway·운영 Script가 업무 Table을 직접 수정하지 않는다.
- 공통 기능이 필요하면 기존 Public API·SPI를 우선 사용하고, 진입점이 없을 때만 실제 Consumer를 근거로 Owner Module에 확장한다.

## 입력·계약

- `customerId`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `reason`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `expectedVersion`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `retentionUntil`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- 해당 기능에 필요한 Operation ID·업무 ID·Correlation ID·요청 Hash·사용자·권한 문맥을 저장·Log·Trace·Audit에서 연결한다.
- 요청·응답·Job Parameter·이벤트·파일 Layout 중 해당되는 계약을 OpenAPI 또는 Machine-readable Schema로 제공한다.

## 상태와 저장 근거

- 필수 상태 흐름: `ACTIVE → DEACTIVATED → RESTORED 또는 PURGE_ELIGIBLE → PURGED`
- 상태마다 진입 조건, 허용 조치, 재시도 가능 여부, 최종 여부, 저장 위치와 Version 규칙을 정의한다.
- 결과 미확정·부분 성공이 가능한 경우 대상별 Attempt와 상대 상태 조회 근거를 별도 저장한다.

## 정상 처리 순서

1. 입력·권한·데이터 범위·기준 Version과 선행 상태를 검증한다.
2. 상태 변경·비동기 실행인 경우 Operation과 업무 식별자를 생성하거나 기존 멱등 요청을 조회한다. 조회 전용 기능은 조회 Context와 Cursor만 사용한다.
3. 상태 변경 기능은 고객 업무 Owner의 Application Use Case에서 Transaction 경계와 상태 전이를 수행하고, 조회 전용 기능은 일관된 Snapshot·정렬·범위 조건을 적용한다.
4. 외부 시스템·Kafka·파일·Batch·Gateway가 필요한 경우 Adapter 경계에서 Timeout·Retry·보안 정책을 적용한다.
5. 업무 결과와 Attempt·Audit·Log·Metric·Trace를 같은 식별자로 연결한다.
6. 고객 API 또는 운영 화면에서 최종 상태·건수·금액·Version·오류 0건을 확인한다.

## 예외·장애·경계 사례

- 동시 복원
- 이미 만료
- 참조 데이터 존재
- 파기 중 장애
- 감사 보존과 개인정보 파기 충돌
- 상태 변경이 있는 경우 DB Commit 전 실패와 Commit 후 응답 유실을 구분한다.
- 같은 요청의 중복·동시 실행·순서 역전·늦은 응답을 포함한다.
- Retry 가능한 오류와 즉시 중단해야 하는 업무 오류를 구분한다.
- 대량·다중 대상 기능에서 부분 성공이 발생하면 성공 대상을 다시 실행하지 않고 실패 대상만 Reprocess·Reconcile한다.
- 복구·보상이 필요한 기능은 복구 실패와 보상 실패도 별도 상태·Attempt·Audit로 남긴다.

## 필수 Test

- 상태 전이
- 권한
- 파기 대상 Preview
- 재실행
- DB FK·Index
- 입력 Validation·권한·Data Scope·Masking Negative Test
- 해당 기능에 적용되는 중복·동시성·Timeout·응답 유실·부분 실패 Test
- 해당 기능의 Failure Injection 후 필요한 Retry·Restart·Reprocess·Reconcile·Rollback Test
- Oracle·PostgreSQL·MariaDB 또는 해당 Infra의 의미 동일성 Test
- Log·Metric·Trace·Audit에 Secret·개인정보 원문이 없는지 검사

## 운영·화면 확인

- ADM 조치·감사에서 비활성화와 파기 작업을 구분
- 화면에는 해당 메뉴에 실제로 존재하는 조회 조건, 기본값, Column, 상세 Field, 상태, Button 활성 조건, 사유, 승인, 예상 Version, 대상별 결과만 Source와 일치하게 표시한다. 조회 전용 메뉴에 Command 항목을 만들지 않는다.
- 운영자가 정상화 완료를 판단할 수 있는 건수·금액·Version·Checksum·최종 상태·마지막 오류를 제공한다.
- 변경·외부연계 기능에서 응답 유실이 가능한 경우에만 요청을 다시 보내기 전에 Operation·거래·상대 상태를 조회하는 절차를 Browser 또는 API Test로 검증한다.

## 고객 매뉴얼·EDU 연결

- `01_개발자매뉴얼.md`의 해당 기능 장에 `EDU-DEV-18`를 연결한다.
- 선행 환경, 실행 명령, 입력값, 정상 출력, 오류 재현 명령, 복구 명령, ADM/BZA/Gateway 확인 위치를 그대로 따라 할 수 있게 작성한다.
- 교육 코드에서 고객 업무로 바꿀 Package·Table·API·Topic·파일명과 CPF 관리 영역을 구분한 전환표를 제공한다.

## 완료 조건

- `EDU-DEV-18`가 교육 Catalog·Source·Test·고객 매뉴얼에 동일 ID로 존재한다.
- 정상·Validation·권한·중복·동시성·Timeout·응답 유실·부분 실패·복구 Test가 통과한다.
- 요청·응답·상태·DB·이벤트·파일·운영 화면이 양방향으로 일치한다.
- 운영자가 다른 사람의 설명 없이 장애를 재현하고 정상화 판정을 할 수 있다.
- 실행하지 않은 Test는 성공으로 기록하지 않고, 실패·제약이 남으면 전체 완료로 보고하지 않는다.

## 금지 사항

- 메모리 Map·고정 JSON·정상 Case 한 건만으로 완료 처리
- 교육용 고객 업무를 `cpf-core`, `cpf-starters/common`, Starter, Runtime 제품 내부에 삽입
- Module-local DB Vendor Fallback SQL 또는 Vendor별 의미가 다른 Schema
- Timeout이나 응답 유실 뒤 실제 결과 확인 없이 무조건 재호출
- 비밀번호·Token·개인정보 원문을 Source·Log·Fixture·문서에 기록
- 매뉴얼 설명과 다른 임의 Package·API·상태·메뉴를 새로 생성

---

# 53. EDU-DEV-19 — 기준일·유효기간이 있는 기준정보

- 우선순위: **P0**
- 연결 매뉴얼: `01_개발자매뉴얼.md`
- Owner: `cpf-education`
- 구현 상태 목표: Source·SQL·API·Config·Test·운영 확인·매뉴얼 연결이 모두 완료

## 필요 근거

- 고객 매뉴얼에서 `기준일·유효기간이 있는 기준정보`을 설명하려면 정상 호출 예시만이 아니라 상태 변화와 장애 후 정상화까지 재현 가능한 실행 예제가 필요하다.
- 이 요구사항이 없으면 고객은 기능 가능 여부를 알더라도 실제 업무 적용 순서, 실패 판정, 재처리 금지 조건을 확인할 수 없다.
- 내부 구현 구조를 보여 주기 위한 예제가 아니라 고객 업무를 CPF로 만드는 방법과 운영 결과를 증명하는 예제로 구현한다.

## 고객 업무 시나리오

상품 금리나 수수료가 미래 효력일에 변경되고 과거 거래는 당시 기준을 유지한다.

## 구현 위치와 책임 경계

- 업무 Source: `com.cpf.education.edu.dev19` 또는 같은 의미의 기존 표준 Slice
- Test: Owner Module의 기존 Test Root 아래 `EDU-DEV-19` 식별 가능한 Class·Fixture
- SQL: 중앙 DB Vendor Pack의 기존 Install·Upgrade·Rollback·Verify·Seed 구조
- Config: 기존 Prefix·Profile·Secret 참조 방식을 유지
- 고객 업무 상태 변경과 원장 데이터는 고객 업무 Owner가 소유한다. ADM·BZA·Gateway·운영 Script가 업무 Table을 직접 수정하지 않는다.
- 공통 기능이 필요하면 기존 Public API·SPI를 우선 사용하고, 진입점이 없을 때만 실제 Consumer를 근거로 Owner Module에 확장한다.

## 입력·계약

- `businessKey`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `validFrom`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `validTo`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `value`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `expectedVersion`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- 해당 기능에 필요한 Operation ID·업무 ID·Correlation ID·요청 Hash·사용자·권한 문맥을 저장·Log·Trace·Audit에서 연결한다.
- 요청·응답·Job Parameter·이벤트·파일 Layout 중 해당되는 계약을 OpenAPI 또는 Machine-readable Schema로 제공한다.

## 상태와 저장 근거

- 필수 상태 흐름: `DRAFT → SCHEDULED → ACTIVE → EXPIRED/SUPERSEDED`
- 상태마다 진입 조건, 허용 조치, 재시도 가능 여부, 최종 여부, 저장 위치와 Version 규칙을 정의한다.
- 결과 미확정·부분 성공이 가능한 경우 대상별 Attempt와 상대 상태 조회 근거를 별도 저장한다.

## 정상 처리 순서

1. 입력·권한·데이터 범위·기준 Version과 선행 상태를 검증한다.
2. 상태 변경·비동기 실행인 경우 Operation과 업무 식별자를 생성하거나 기존 멱등 요청을 조회한다. 조회 전용 기능은 조회 Context와 Cursor만 사용한다.
3. 상태 변경 기능은 고객 업무 Owner의 Application Use Case에서 Transaction 경계와 상태 전이를 수행하고, 조회 전용 기능은 일관된 Snapshot·정렬·범위 조건을 적용한다.
4. 외부 시스템·Kafka·파일·Batch·Gateway가 필요한 경우 Adapter 경계에서 Timeout·Retry·보안 정책을 적용한다.
5. 업무 결과와 Attempt·Audit·Log·Metric·Trace를 같은 식별자로 연결한다.
6. 고객 API 또는 운영 화면에서 최종 상태·건수·금액·Version·오류 0건을 확인한다.

## 예외·장애·경계 사례

- 기간 겹침
- 공백 기간
- 소급 변경
- 시간대 경계
- 미래 예약 취소
- 상태 변경이 있는 경우 DB Commit 전 실패와 Commit 후 응답 유실을 구분한다.
- 같은 요청의 중복·동시 실행·순서 역전·늦은 응답을 포함한다.
- Retry 가능한 오류와 즉시 중단해야 하는 업무 오류를 구분한다.
- 대량·다중 대상 기능에서 부분 성공이 발생하면 성공 대상을 다시 실행하지 않고 실패 대상만 Reprocess·Reconcile한다.
- 복구·보상이 필요한 기능은 복구 실패와 보상 실패도 별도 상태·Attempt·Audit로 남긴다.

## 필수 Test

- 기준일 조회
- 겹침 방지
- 소급 승인
- DB 3종 Date/Time 의미
- 입력 Validation·권한·Data Scope·Masking Negative Test
- 해당 기능에 적용되는 중복·동시성·Timeout·응답 유실·부분 실패 Test
- 해당 기능의 Failure Injection 후 필요한 Retry·Restart·Reprocess·Reconcile·Rollback Test
- Oracle·PostgreSQL·MariaDB 또는 해당 Infra의 의미 동일성 Test
- Log·Metric·Trace·Audit에 Secret·개인정보 원문이 없는지 검사

## 운영·화면 확인

- ADM 기준정보 조회에서 현재값·미래값·과거값을 분리
- 화면에는 해당 메뉴에 실제로 존재하는 조회 조건, 기본값, Column, 상세 Field, 상태, Button 활성 조건, 사유, 승인, 예상 Version, 대상별 결과만 Source와 일치하게 표시한다. 조회 전용 메뉴에 Command 항목을 만들지 않는다.
- 운영자가 정상화 완료를 판단할 수 있는 건수·금액·Version·Checksum·최종 상태·마지막 오류를 제공한다.
- 변경·외부연계 기능에서 응답 유실이 가능한 경우에만 요청을 다시 보내기 전에 Operation·거래·상대 상태를 조회하는 절차를 Browser 또는 API Test로 검증한다.

## 고객 매뉴얼·EDU 연결

- `01_개발자매뉴얼.md`의 해당 기능 장에 `EDU-DEV-19`를 연결한다.
- 선행 환경, 실행 명령, 입력값, 정상 출력, 오류 재현 명령, 복구 명령, ADM/BZA/Gateway 확인 위치를 그대로 따라 할 수 있게 작성한다.
- 교육 코드에서 고객 업무로 바꿀 Package·Table·API·Topic·파일명과 CPF 관리 영역을 구분한 전환표를 제공한다.

## 완료 조건

- `EDU-DEV-19`가 교육 Catalog·Source·Test·고객 매뉴얼에 동일 ID로 존재한다.
- 정상·Validation·권한·중복·동시성·Timeout·응답 유실·부분 실패·복구 Test가 통과한다.
- 요청·응답·상태·DB·이벤트·파일·운영 화면이 양방향으로 일치한다.
- 운영자가 다른 사람의 설명 없이 장애를 재현하고 정상화 판정을 할 수 있다.
- 실행하지 않은 Test는 성공으로 기록하지 않고, 실패·제약이 남으면 전체 완료로 보고하지 않는다.

## 금지 사항

- 메모리 Map·고정 JSON·정상 Case 한 건만으로 완료 처리
- 교육용 고객 업무를 `cpf-core`, `cpf-starters/common`, Starter, Runtime 제품 내부에 삽입
- Module-local DB Vendor Fallback SQL 또는 Vendor별 의미가 다른 Schema
- Timeout이나 응답 유실 뒤 실제 결과 확인 없이 무조건 재호출
- 비밀번호·Token·개인정보 원문을 Source·Log·Fixture·문서에 기록
- 매뉴얼 설명과 다른 임의 Package·API·상태·메뉴를 새로 생성

---

# 54. EDU-DEV-20 — 다단계 고객 업무 상태기계와 취소·재개

- 우선순위: **P0**
- 연결 매뉴얼: `01_개발자매뉴얼.md`
- Owner: `cpf-education`
- 구현 상태 목표: Source·SQL·API·Config·Test·운영 확인·매뉴얼 연결이 모두 완료

## 필요 근거

- 고객 매뉴얼에서 `다단계 고객 업무 상태기계와 취소·재개`을 설명하려면 정상 호출 예시만이 아니라 상태 변화와 장애 후 정상화까지 재현 가능한 실행 예제가 필요하다.
- 이 요구사항이 없으면 고객은 기능 가능 여부를 알더라도 실제 업무 적용 순서, 실패 판정, 재처리 금지 조건을 확인할 수 없다.
- 내부 구현 구조를 보여 주기 위한 예제가 아니라 고객 업무를 CPF로 만드는 방법과 운영 결과를 증명하는 예제로 구현한다.

## 고객 업무 시나리오

대출 신청이 접수·심사·승인·실행으로 진행되며 단계별 취소와 재개 규칙이 다르다.

## 구현 위치와 책임 경계

- 업무 Source: `com.cpf.education.edu.dev20` 또는 같은 의미의 기존 표준 Slice
- Test: Owner Module의 기존 Test Root 아래 `EDU-DEV-20` 식별 가능한 Class·Fixture
- SQL: 중앙 DB Vendor Pack의 기존 Install·Upgrade·Rollback·Verify·Seed 구조
- Config: 기존 Prefix·Profile·Secret 참조 방식을 유지
- 고객 업무 상태 변경과 원장 데이터는 고객 업무 Owner가 소유한다. ADM·BZA·Gateway·운영 Script가 업무 Table을 직접 수정하지 않는다.
- 공통 기능이 필요하면 기존 Public API·SPI를 우선 사용하고, 진입점이 없을 때만 실제 Consumer를 근거로 Owner Module에 확장한다.

## 입력·계약

- `applicationId`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `action`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `reason`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `expectedVersion`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- 해당 기능에 필요한 Operation ID·업무 ID·Correlation ID·요청 Hash·사용자·권한 문맥을 저장·Log·Trace·Audit에서 연결한다.
- 요청·응답·Job Parameter·이벤트·파일 Layout 중 해당되는 계약을 OpenAPI 또는 Machine-readable Schema로 제공한다.

## 상태와 저장 근거

- 필수 상태 흐름: `RECEIVED → REVIEWING → APPROVED → EXECUTED; CANCELLED/ON_HOLD`
- 상태마다 진입 조건, 허용 조치, 재시도 가능 여부, 최종 여부, 저장 위치와 Version 규칙을 정의한다.
- 결과 미확정·부분 성공이 가능한 경우 대상별 Attempt와 상대 상태 조회 근거를 별도 저장한다.

## 정상 처리 순서

1. 입력·권한·데이터 범위·기준 Version과 선행 상태를 검증한다.
2. 상태 변경·비동기 실행인 경우 Operation과 업무 식별자를 생성하거나 기존 멱등 요청을 조회한다. 조회 전용 기능은 조회 Context와 Cursor만 사용한다.
3. 상태 변경 기능은 고객 업무 Owner의 Application Use Case에서 Transaction 경계와 상태 전이를 수행하고, 조회 전용 기능은 일관된 Snapshot·정렬·범위 조건을 적용한다.
4. 외부 시스템·Kafka·파일·Batch·Gateway가 필요한 경우 Adapter 경계에서 Timeout·Retry·보안 정책을 적용한다.
5. 업무 결과와 Attempt·Audit·Log·Metric·Trace를 같은 식별자로 연결한다.
6. 고객 API 또는 운영 화면에서 최종 상태·건수·금액·Version·오류 0건을 확인한다.

## 예외·장애·경계 사례

- 허용되지 않은 전이
- 이전 단계 메시지 지연
- 실행 후 취소
- 보류 중 재개
- 중복 명령
- 상태 변경이 있는 경우 DB Commit 전 실패와 Commit 후 응답 유실을 구분한다.
- 같은 요청의 중복·동시 실행·순서 역전·늦은 응답을 포함한다.
- Retry 가능한 오류와 즉시 중단해야 하는 업무 오류를 구분한다.
- 대량·다중 대상 기능에서 부분 성공이 발생하면 성공 대상을 다시 실행하지 않고 실패 대상만 Reprocess·Reconcile한다.
- 복구·보상이 필요한 기능은 복구 실패와 보상 실패도 별도 상태·Attempt·Audit로 남긴다.

## 필수 Test

- 상태표 전수
- 전이 Guard
- 감사
- 멱등
- 동시성
- 입력 Validation·권한·Data Scope·Masking Negative Test
- 해당 기능에 적용되는 중복·동시성·Timeout·응답 유실·부분 실패 Test
- 해당 기능의 Failure Injection 후 필요한 Retry·Restart·Reprocess·Reconcile·Rollback Test
- Oracle·PostgreSQL·MariaDB 또는 해당 Infra의 의미 동일성 Test
- Log·Metric·Trace·Audit에 Secret·개인정보 원문이 없는지 검사

## 운영·화면 확인

- ADM 거래 상세에서 현재 단계·가능 조치·이력 확인
- 화면에는 해당 메뉴에 실제로 존재하는 조회 조건, 기본값, Column, 상세 Field, 상태, Button 활성 조건, 사유, 승인, 예상 Version, 대상별 결과만 Source와 일치하게 표시한다. 조회 전용 메뉴에 Command 항목을 만들지 않는다.
- 운영자가 정상화 완료를 판단할 수 있는 건수·금액·Version·Checksum·최종 상태·마지막 오류를 제공한다.
- 변경·외부연계 기능에서 응답 유실이 가능한 경우에만 요청을 다시 보내기 전에 Operation·거래·상대 상태를 조회하는 절차를 Browser 또는 API Test로 검증한다.

## 고객 매뉴얼·EDU 연결

- `01_개발자매뉴얼.md`의 해당 기능 장에 `EDU-DEV-20`를 연결한다.
- 선행 환경, 실행 명령, 입력값, 정상 출력, 오류 재현 명령, 복구 명령, ADM/BZA/Gateway 확인 위치를 그대로 따라 할 수 있게 작성한다.
- 교육 코드에서 고객 업무로 바꿀 Package·Table·API·Topic·파일명과 CPF 관리 영역을 구분한 전환표를 제공한다.

## 완료 조건

- `EDU-DEV-20`가 교육 Catalog·Source·Test·고객 매뉴얼에 동일 ID로 존재한다.
- 정상·Validation·권한·중복·동시성·Timeout·응답 유실·부분 실패·복구 Test가 통과한다.
- 요청·응답·상태·DB·이벤트·파일·운영 화면이 양방향으로 일치한다.
- 운영자가 다른 사람의 설명 없이 장애를 재현하고 정상화 판정을 할 수 있다.
- 실행하지 않은 Test는 성공으로 기록하지 않고, 실패·제약이 남으면 전체 완료로 보고하지 않는다.

## 금지 사항

- 메모리 Map·고정 JSON·정상 Case 한 건만으로 완료 처리
- 교육용 고객 업무를 `cpf-core`, `cpf-starters/common`, Starter, Runtime 제품 내부에 삽입
- Module-local DB Vendor Fallback SQL 또는 Vendor별 의미가 다른 Schema
- Timeout이나 응답 유실 뒤 실제 결과 확인 없이 무조건 재호출
- 비밀번호·Token·개인정보 원문을 Source·Log·Fixture·문서에 기록
- 매뉴얼 설명과 다른 임의 Package·API·상태·메뉴를 새로 생성

---

# 55. EDU-DEV-21 — Transactional Outbox 게시 지연·재시작

- 우선순위: **P0**
- 연결 매뉴얼: `01_개발자매뉴얼.md`
- Owner: `cpf-education`
- 구현 상태 목표: Source·SQL·API·Config·Test·운영 확인·매뉴얼 연결이 모두 완료

## 필요 근거

- 고객 매뉴얼에서 `Transactional Outbox 게시 지연·재시작`을 설명하려면 정상 호출 예시만이 아니라 상태 변화와 장애 후 정상화까지 재현 가능한 실행 예제가 필요하다.
- 이 요구사항이 없으면 고객은 기능 가능 여부를 알더라도 실제 업무 적용 순서, 실패 판정, 재처리 금지 조건을 확인할 수 없다.
- 내부 구현 구조를 보여 주기 위한 예제가 아니라 고객 업무를 CPF로 만드는 방법과 운영 결과를 증명하는 예제로 구현한다.

## 고객 업무 시나리오

주문 Commit과 이벤트 기록을 한 Transaction으로 남기고 게시기는 장애 후 미게시 건만 재개한다.

## 구현 위치와 책임 경계

- 업무 Source: `com.cpf.education.edu.dev21` 또는 같은 의미의 기존 표준 Slice
- Test: Owner Module의 기존 Test Root 아래 `EDU-DEV-21` 식별 가능한 Class·Fixture
- SQL: 중앙 DB Vendor Pack의 기존 Install·Upgrade·Rollback·Verify·Seed 구조
- Config: 기존 Prefix·Profile·Secret 참조 방식을 유지
- 고객 업무 상태 변경과 원장 데이터는 고객 업무 Owner가 소유한다. ADM·BZA·Gateway·운영 Script가 업무 Table을 직접 수정하지 않는다.
- 공통 기능이 필요하면 기존 Public API·SPI를 우선 사용하고, 진입점이 없을 때만 실제 Consumer를 근거로 Owner Module에 확장한다.

## 입력·계약

- `aggregateId`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `eventType`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `payloadVersion`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `operationId`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- 해당 기능에 필요한 Operation ID·업무 ID·Correlation ID·요청 Hash·사용자·권한 문맥을 저장·Log·Trace·Audit에서 연결한다.
- 요청·응답·Job Parameter·이벤트·파일 Layout 중 해당되는 계약을 OpenAPI 또는 Machine-readable Schema로 제공한다.

## 상태와 저장 근거

- 필수 상태 흐름: `OUTBOX_NEW → PUBLISHING → PUBLISHED/FAILED → RETRYING`
- 상태마다 진입 조건, 허용 조치, 재시도 가능 여부, 최종 여부, 저장 위치와 Version 규칙을 정의한다.
- 결과 미확정·부분 성공이 가능한 경우 대상별 Attempt와 상대 상태 조회 근거를 별도 저장한다.

## 정상 처리 순서

1. 입력·권한·데이터 범위·기준 Version과 선행 상태를 검증한다.
2. 상태 변경·비동기 실행인 경우 Operation과 업무 식별자를 생성하거나 기존 멱등 요청을 조회한다. 조회 전용 기능은 조회 Context와 Cursor만 사용한다.
3. 상태 변경 기능은 고객 업무 Owner의 Application Use Case에서 Transaction 경계와 상태 전이를 수행하고, 조회 전용 기능은 일관된 Snapshot·정렬·범위 조건을 적용한다.
4. 외부 시스템·Kafka·파일·Batch·Gateway가 필요한 경우 Adapter 경계에서 Timeout·Retry·보안 정책을 적용한다.
5. 업무 결과와 Attempt·Audit·Log·Metric·Trace를 같은 식별자로 연결한다.
6. 고객 API 또는 운영 화면에서 최종 상태·건수·금액·Version·오류 0건을 확인한다.

## 예외·장애·경계 사례

- DB Commit 전 장애
- Commit 후 Publisher 중단
- Kafka ACK 유실
- 중복 게시
- 순서 역전
- 상태 변경이 있는 경우 DB Commit 전 실패와 Commit 후 응답 유실을 구분한다.
- 같은 요청의 중복·동시 실행·순서 역전·늦은 응답을 포함한다.
- Retry 가능한 오류와 즉시 중단해야 하는 업무 오류를 구분한다.
- 대량·다중 대상 기능에서 부분 성공이 발생하면 성공 대상을 다시 실행하지 않고 실패 대상만 Reprocess·Reconcile한다.
- 복구·보상이 필요한 기능은 복구 실패와 보상 실패도 별도 상태·Attempt·Audit로 남긴다.

## 필수 Test

- 원장·Outbox 원자성
- Claim/Fencing
- 중복 소비
- 재시작
- 메트릭
- 입력 Validation·권한·Data Scope·Masking Negative Test
- 해당 기능에 적용되는 중복·동시성·Timeout·응답 유실·부분 실패 Test
- 해당 기능의 Failure Injection 후 필요한 Retry·Restart·Reprocess·Reconcile·Rollback Test
- Oracle·PostgreSQL·MariaDB 또는 해당 Infra의 의미 동일성 Test
- Log·Metric·Trace·Audit에 Secret·개인정보 원문이 없는지 검사

## 운영·화면 확인

- ADM Outbox·메시지 상태에서 적체·시도·마지막 오류 확인
- 화면에는 해당 메뉴에 실제로 존재하는 조회 조건, 기본값, Column, 상세 Field, 상태, Button 활성 조건, 사유, 승인, 예상 Version, 대상별 결과만 Source와 일치하게 표시한다. 조회 전용 메뉴에 Command 항목을 만들지 않는다.
- 운영자가 정상화 완료를 판단할 수 있는 건수·금액·Version·Checksum·최종 상태·마지막 오류를 제공한다.
- 변경·외부연계 기능에서 응답 유실이 가능한 경우에만 요청을 다시 보내기 전에 Operation·거래·상대 상태를 조회하는 절차를 Browser 또는 API Test로 검증한다.

## 고객 매뉴얼·EDU 연결

- `01_개발자매뉴얼.md`의 해당 기능 장에 `EDU-DEV-21`를 연결한다.
- 선행 환경, 실행 명령, 입력값, 정상 출력, 오류 재현 명령, 복구 명령, ADM/BZA/Gateway 확인 위치를 그대로 따라 할 수 있게 작성한다.
- 교육 코드에서 고객 업무로 바꿀 Package·Table·API·Topic·파일명과 CPF 관리 영역을 구분한 전환표를 제공한다.

## 완료 조건

- `EDU-DEV-21`가 교육 Catalog·Source·Test·고객 매뉴얼에 동일 ID로 존재한다.
- 정상·Validation·권한·중복·동시성·Timeout·응답 유실·부분 실패·복구 Test가 통과한다.
- 요청·응답·상태·DB·이벤트·파일·운영 화면이 양방향으로 일치한다.
- 운영자가 다른 사람의 설명 없이 장애를 재현하고 정상화 판정을 할 수 있다.
- 실행하지 않은 Test는 성공으로 기록하지 않고, 실패·제약이 남으면 전체 완료로 보고하지 않는다.

## 금지 사항

- 메모리 Map·고정 JSON·정상 Case 한 건만으로 완료 처리
- 교육용 고객 업무를 `cpf-core`, `cpf-starters/common`, Starter, Runtime 제품 내부에 삽입
- Module-local DB Vendor Fallback SQL 또는 Vendor별 의미가 다른 Schema
- Timeout이나 응답 유실 뒤 실제 결과 확인 없이 무조건 재호출
- 비밀번호·Token·개인정보 원문을 Source·Log·Fixture·문서에 기록
- 매뉴얼 설명과 다른 임의 Package·API·상태·메뉴를 새로 생성

---

# 56. EDU-DEV-22 — 서비스 간 Saga 보상·수동 확정

- 우선순위: **P0**
- 연결 매뉴얼: `01_개발자매뉴얼.md`
- Owner: `cpf-education`
- 구현 상태 목표: Source·SQL·API·Config·Test·운영 확인·매뉴얼 연결이 모두 완료

## 필요 근거

- 고객 매뉴얼에서 `서비스 간 Saga 보상·수동 확정`을 설명하려면 정상 호출 예시만이 아니라 상태 변화와 장애 후 정상화까지 재현 가능한 실행 예제가 필요하다.
- 이 요구사항이 없으면 고객은 기능 가능 여부를 알더라도 실제 업무 적용 순서, 실패 판정, 재처리 금지 조건을 확인할 수 없다.
- 내부 구현 구조를 보여 주기 위한 예제가 아니라 고객 업무를 CPF로 만드는 방법과 운영 결과를 증명하는 예제로 구현한다.

## 고객 업무 시나리오

주문·재고·결제 세 서비스 중 결제 실패 시 재고를 보상하고, 결과 미확정이면 운영자가 대사한다.

## 구현 위치와 책임 경계

- 업무 Source: `com.cpf.education.edu.dev22` 또는 같은 의미의 기존 표준 Slice
- Test: Owner Module의 기존 Test Root 아래 `EDU-DEV-22` 식별 가능한 Class·Fixture
- SQL: 중앙 DB Vendor Pack의 기존 Install·Upgrade·Rollback·Verify·Seed 구조
- Config: 기존 Prefix·Profile·Secret 참조 방식을 유지
- 고객 업무 상태 변경과 원장 데이터는 고객 업무 Owner가 소유한다. ADM·BZA·Gateway·운영 Script가 업무 Table을 직접 수정하지 않는다.
- 공통 기능이 필요하면 기존 Public API·SPI를 우선 사용하고, 진입점이 없을 때만 실제 Consumer를 근거로 Owner Module에 확장한다.

## 입력·계약

- `sagaId`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `orderId`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `amount`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `steps`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `timeoutBudget`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- 해당 기능에 필요한 Operation ID·업무 ID·Correlation ID·요청 Hash·사용자·권한 문맥을 저장·Log·Trace·Audit에서 연결한다.
- 요청·응답·Job Parameter·이벤트·파일 Layout 중 해당되는 계약을 OpenAPI 또는 Machine-readable Schema로 제공한다.

## 상태와 저장 근거

- 필수 상태 흐름: `STARTED → INVENTORY_RESERVED → PAYMENT_PENDING → COMPLETED/COMPENSATING/UNKNOWN_RESULT`
- 상태마다 진입 조건, 허용 조치, 재시도 가능 여부, 최종 여부, 저장 위치와 Version 규칙을 정의한다.
- 결과 미확정·부분 성공이 가능한 경우 대상별 Attempt와 상대 상태 조회 근거를 별도 저장한다.

## 정상 처리 순서

1. 입력·권한·데이터 범위·기준 Version과 선행 상태를 검증한다.
2. 상태 변경·비동기 실행인 경우 Operation과 업무 식별자를 생성하거나 기존 멱등 요청을 조회한다. 조회 전용 기능은 조회 Context와 Cursor만 사용한다.
3. 상태 변경 기능은 고객 업무 Owner의 Application Use Case에서 Transaction 경계와 상태 전이를 수행하고, 조회 전용 기능은 일관된 Snapshot·정렬·범위 조건을 적용한다.
4. 외부 시스템·Kafka·파일·Batch·Gateway가 필요한 경우 Adapter 경계에서 Timeout·Retry·보안 정책을 적용한다.
5. 업무 결과와 Attempt·Audit·Log·Metric·Trace를 같은 식별자로 연결한다.
6. 고객 API 또는 운영 화면에서 최종 상태·건수·금액·Version·오류 0건을 확인한다.

## 예외·장애·경계 사례

- 재고 성공 후 결제 실패
- 보상 실패
- 응답 유실
- 중복 보상
- 늦은 성공 응답
- 상태 변경이 있는 경우 DB Commit 전 실패와 Commit 후 응답 유실을 구분한다.
- 같은 요청의 중복·동시 실행·순서 역전·늦은 응답을 포함한다.
- Retry 가능한 오류와 즉시 중단해야 하는 업무 오류를 구분한다.
- 대량·다중 대상 기능에서 부분 성공이 발생하면 성공 대상을 다시 실행하지 않고 실패 대상만 Reprocess·Reconcile한다.
- 복구·보상이 필요한 기능은 복구 실패와 보상 실패도 별도 상태·Attempt·Audit로 남긴다.

## 필수 Test

- 단계별 상태
- 보상 멱등성
- Timeout budget
- 수동 Reconcile
- 입력 Validation·권한·Data Scope·Masking Negative Test
- 해당 기능에 적용되는 중복·동시성·Timeout·응답 유실·부분 실패 Test
- 해당 기능의 Failure Injection 후 필요한 Retry·Restart·Reprocess·Reconcile·Rollback Test
- Oracle·PostgreSQL·MariaDB 또는 해당 Infra의 의미 동일성 Test
- Log·Metric·Trace·Audit에 Secret·개인정보 원문이 없는지 검사

## 운영·화면 확인

- ADM Recovery Center에서 단계별 결과와 보상 재시도 확인
- 화면에는 해당 메뉴에 실제로 존재하는 조회 조건, 기본값, Column, 상세 Field, 상태, Button 활성 조건, 사유, 승인, 예상 Version, 대상별 결과만 Source와 일치하게 표시한다. 조회 전용 메뉴에 Command 항목을 만들지 않는다.
- 운영자가 정상화 완료를 판단할 수 있는 건수·금액·Version·Checksum·최종 상태·마지막 오류를 제공한다.
- 변경·외부연계 기능에서 응답 유실이 가능한 경우에만 요청을 다시 보내기 전에 Operation·거래·상대 상태를 조회하는 절차를 Browser 또는 API Test로 검증한다.

## 고객 매뉴얼·EDU 연결

- `01_개발자매뉴얼.md`의 해당 기능 장에 `EDU-DEV-22`를 연결한다.
- 선행 환경, 실행 명령, 입력값, 정상 출력, 오류 재현 명령, 복구 명령, ADM/BZA/Gateway 확인 위치를 그대로 따라 할 수 있게 작성한다.
- 교육 코드에서 고객 업무로 바꿀 Package·Table·API·Topic·파일명과 CPF 관리 영역을 구분한 전환표를 제공한다.

## 완료 조건

- `EDU-DEV-22`가 교육 Catalog·Source·Test·고객 매뉴얼에 동일 ID로 존재한다.
- 정상·Validation·권한·중복·동시성·Timeout·응답 유실·부분 실패·복구 Test가 통과한다.
- 요청·응답·상태·DB·이벤트·파일·운영 화면이 양방향으로 일치한다.
- 운영자가 다른 사람의 설명 없이 장애를 재현하고 정상화 판정을 할 수 있다.
- 실행하지 않은 Test는 성공으로 기록하지 않고, 실패·제약이 남으면 전체 완료로 보고하지 않는다.

## 금지 사항

- 메모리 Map·고정 JSON·정상 Case 한 건만으로 완료 처리
- 교육용 고객 업무를 `cpf-core`, `cpf-starters/common`, Starter, Runtime 제품 내부에 삽입
- Module-local DB Vendor Fallback SQL 또는 Vendor별 의미가 다른 Schema
- Timeout이나 응답 유실 뒤 실제 결과 확인 없이 무조건 재호출
- 비밀번호·Token·개인정보 원문을 Source·Log·Fixture·문서에 기록
- 매뉴얼 설명과 다른 임의 Package·API·상태·메뉴를 새로 생성

---

# 57. EDU-DEV-23 — 공통 입력검증·오류 계약·OpenAPI 일치

- 우선순위: **P0**
- 연결 매뉴얼: `01_개발자매뉴얼.md`
- Owner: `cpf-education`
- 구현 상태 목표: Source·SQL·API·Config·Test·운영 확인·매뉴얼 연결이 모두 완료

## 필요 근거

- 고객 매뉴얼에서 `공통 입력검증·오류 계약·OpenAPI 일치`을 설명하려면 정상 호출 예시만이 아니라 상태 변화와 장애 후 정상화까지 재현 가능한 실행 예제가 필요하다.
- 이 요구사항이 없으면 고객은 기능 가능 여부를 알더라도 실제 업무 적용 순서, 실패 판정, 재처리 금지 조건을 확인할 수 없다.
- 내부 구현 구조를 보여 주기 위한 예제가 아니라 고객 업무를 CPF로 만드는 방법과 운영 결과를 증명하는 예제로 구현한다.

## 고객 업무 시나리오

고객 API가 필수값·형식·업무규칙 오류를 같은 Error Envelope로 반환한다.

## 구현 위치와 책임 경계

- 업무 Source: `com.cpf.education.edu.dev23` 또는 같은 의미의 기존 표준 Slice
- Test: Owner Module의 기존 Test Root 아래 `EDU-DEV-23` 식별 가능한 Class·Fixture
- SQL: 중앙 DB Vendor Pack의 기존 Install·Upgrade·Rollback·Verify·Seed 구조
- Config: 기존 Prefix·Profile·Secret 참조 방식을 유지
- 고객 업무 상태 변경과 원장 데이터는 고객 업무 Owner가 소유한다. ADM·BZA·Gateway·운영 Script가 업무 Table을 직접 수정하지 않는다.
- 공통 기능이 필요하면 기존 Public API·SPI를 우선 사용하고, 진입점이 없을 때만 실제 Consumer를 근거로 Owner Module에 확장한다.

## 입력·계약

- `Header`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `path`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `query`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `body`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `field constraints`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- 해당 기능에 필요한 Operation ID·업무 ID·Correlation ID·요청 Hash·사용자·권한 문맥을 저장·Log·Trace·Audit에서 연결한다.
- 요청·응답·Job Parameter·이벤트·파일 Layout 중 해당되는 계약을 OpenAPI 또는 Machine-readable Schema로 제공한다.

## 상태와 저장 근거

- 필수 상태 흐름: `REQUESTED → VALIDATION_FAILED 또는 ACCEPTED`
- 상태마다 진입 조건, 허용 조치, 재시도 가능 여부, 최종 여부, 저장 위치와 Version 규칙을 정의한다.
- 결과 미확정·부분 성공이 가능한 경우 대상별 Attempt와 상대 상태 조회 근거를 별도 저장한다.

## 정상 처리 순서

1. 입력·권한·데이터 범위·기준 Version과 선행 상태를 검증한다.
2. 상태 변경·비동기 실행인 경우 Operation과 업무 식별자를 생성하거나 기존 멱등 요청을 조회한다. 조회 전용 기능은 조회 Context와 Cursor만 사용한다.
3. 상태 변경 기능은 고객 업무 Owner의 Application Use Case에서 Transaction 경계와 상태 전이를 수행하고, 조회 전용 기능은 일관된 Snapshot·정렬·범위 조건을 적용한다.
4. 외부 시스템·Kafka·파일·Batch·Gateway가 필요한 경우 Adapter 경계에서 Timeout·Retry·보안 정책을 적용한다.
5. 업무 결과와 Attempt·Audit·Log·Metric·Trace를 같은 식별자로 연결한다.
6. 고객 API 또는 운영 화면에서 최종 상태·건수·금액·Version·오류 0건을 확인한다.

## 예외·장애·경계 사례

- 필수 Header 누락
- 숫자 범위
- 다중 Field 오류
- JSON 파싱 오류
- 권한 오류와 업무 오류 구분
- 상태 변경이 있는 경우 DB Commit 전 실패와 Commit 후 응답 유실을 구분한다.
- 같은 요청의 중복·동시 실행·순서 역전·늦은 응답을 포함한다.
- Retry 가능한 오류와 즉시 중단해야 하는 업무 오류를 구분한다.
- 대량·다중 대상 기능에서 부분 성공이 발생하면 성공 대상을 다시 실행하지 않고 실패 대상만 Reprocess·Reconcile한다.
- 복구·보상이 필요한 기능은 복구 실패와 보상 실패도 별도 상태·Attempt·Audit로 남긴다.

## 필수 Test

- OpenAPI schema
- Generated Client
- 오류코드
- 다국어 메시지
- Trace ID
- 입력 Validation·권한·Data Scope·Masking Negative Test
- 해당 기능에 적용되는 중복·동시성·Timeout·응답 유실·부분 실패 Test
- 해당 기능의 Failure Injection 후 필요한 Retry·Restart·Reprocess·Reconcile·Rollback Test
- Oracle·PostgreSQL·MariaDB 또는 해당 Infra의 의미 동일성 Test
- Log·Metric·Trace·Audit에 Secret·개인정보 원문이 없는지 검사

## 운영·화면 확인

- ADM 응답코드·메시지 Catalog에서 실제 오류와 매핑 확인
- 화면에는 해당 메뉴에 실제로 존재하는 조회 조건, 기본값, Column, 상세 Field, 상태, Button 활성 조건, 사유, 승인, 예상 Version, 대상별 결과만 Source와 일치하게 표시한다. 조회 전용 메뉴에 Command 항목을 만들지 않는다.
- 운영자가 정상화 완료를 판단할 수 있는 건수·금액·Version·Checksum·최종 상태·마지막 오류를 제공한다.
- 변경·외부연계 기능에서 응답 유실이 가능한 경우에만 요청을 다시 보내기 전에 Operation·거래·상대 상태를 조회하는 절차를 Browser 또는 API Test로 검증한다.

## 고객 매뉴얼·EDU 연결

- `01_개발자매뉴얼.md`의 해당 기능 장에 `EDU-DEV-23`를 연결한다.
- 선행 환경, 실행 명령, 입력값, 정상 출력, 오류 재현 명령, 복구 명령, ADM/BZA/Gateway 확인 위치를 그대로 따라 할 수 있게 작성한다.
- 교육 코드에서 고객 업무로 바꿀 Package·Table·API·Topic·파일명과 CPF 관리 영역을 구분한 전환표를 제공한다.

## 완료 조건

- `EDU-DEV-23`가 교육 Catalog·Source·Test·고객 매뉴얼에 동일 ID로 존재한다.
- 정상·Validation·권한·중복·동시성·Timeout·응답 유실·부분 실패·복구 Test가 통과한다.
- 요청·응답·상태·DB·이벤트·파일·운영 화면이 양방향으로 일치한다.
- 운영자가 다른 사람의 설명 없이 장애를 재현하고 정상화 판정을 할 수 있다.
- 실행하지 않은 Test는 성공으로 기록하지 않고, 실패·제약이 남으면 전체 완료로 보고하지 않는다.

## 금지 사항

- 메모리 Map·고정 JSON·정상 Case 한 건만으로 완료 처리
- 교육용 고객 업무를 `cpf-core`, `cpf-starters/common`, Starter, Runtime 제품 내부에 삽입
- Module-local DB Vendor Fallback SQL 또는 Vendor별 의미가 다른 Schema
- Timeout이나 응답 유실 뒤 실제 결과 확인 없이 무조건 재호출
- 비밀번호·Token·개인정보 원문을 Source·Log·Fixture·문서에 기록
- 매뉴얼 설명과 다른 임의 Package·API·상태·메뉴를 새로 생성

---

# 58. EDU-DEV-24 — 장시간 비동기 Operation 조회·취소

- 우선순위: **P0**
- 연결 매뉴얼: `01_개발자매뉴얼.md`
- Owner: `cpf-education`
- 구현 상태 목표: Source·SQL·API·Config·Test·운영 확인·매뉴얼 연결이 모두 완료

## 필요 근거

- 고객 매뉴얼에서 `장시간 비동기 Operation 조회·취소`을 설명하려면 정상 호출 예시만이 아니라 상태 변화와 장애 후 정상화까지 재현 가능한 실행 예제가 필요하다.
- 이 요구사항이 없으면 고객은 기능 가능 여부를 알더라도 실제 업무 적용 순서, 실패 판정, 재처리 금지 조건을 확인할 수 없다.
- 내부 구현 구조를 보여 주기 위한 예제가 아니라 고객 업무를 CPF로 만드는 방법과 운영 결과를 증명하는 예제로 구현한다.

## 고객 업무 시나리오

고객이 대량 산출을 요청하고 Operation ID로 진행률을 조회하며 안전한 구간에서 취소한다.

## 구현 위치와 책임 경계

- 업무 Source: `com.cpf.education.edu.dev24` 또는 같은 의미의 기존 표준 Slice
- Test: Owner Module의 기존 Test Root 아래 `EDU-DEV-24` 식별 가능한 Class·Fixture
- SQL: 중앙 DB Vendor Pack의 기존 Install·Upgrade·Rollback·Verify·Seed 구조
- Config: 기존 Prefix·Profile·Secret 참조 방식을 유지
- 고객 업무 상태 변경과 원장 데이터는 고객 업무 Owner가 소유한다. ADM·BZA·Gateway·운영 Script가 업무 Table을 직접 수정하지 않는다.
- 공통 기능이 필요하면 기존 Public API·SPI를 우선 사용하고, 진입점이 없을 때만 실제 Consumer를 근거로 Owner Module에 확장한다.

## 입력·계약

- `operationId`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `requestHash`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `cancelReason`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `pollingCursor`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- 해당 기능에 필요한 Operation ID·업무 ID·Correlation ID·요청 Hash·사용자·권한 문맥을 저장·Log·Trace·Audit에서 연결한다.
- 요청·응답·Job Parameter·이벤트·파일 Layout 중 해당되는 계약을 OpenAPI 또는 Machine-readable Schema로 제공한다.

## 상태와 저장 근거

- 필수 상태 흐름: `ACCEPTED → RUNNING → CANCEL_REQUESTED → CANCELLED/COMPLETED/FAILED`
- 상태마다 진입 조건, 허용 조치, 재시도 가능 여부, 최종 여부, 저장 위치와 Version 규칙을 정의한다.
- 결과 미확정·부분 성공이 가능한 경우 대상별 Attempt와 상대 상태 조회 근거를 별도 저장한다.

## 정상 처리 순서

1. 입력·권한·데이터 범위·기준 Version과 선행 상태를 검증한다.
2. 상태 변경·비동기 실행인 경우 Operation과 업무 식별자를 생성하거나 기존 멱등 요청을 조회한다. 조회 전용 기능은 조회 Context와 Cursor만 사용한다.
3. 상태 변경 기능은 고객 업무 Owner의 Application Use Case에서 Transaction 경계와 상태 전이를 수행하고, 조회 전용 기능은 일관된 Snapshot·정렬·범위 조건을 적용한다.
4. 외부 시스템·Kafka·파일·Batch·Gateway가 필요한 경우 Adapter 경계에서 Timeout·Retry·보안 정책을 적용한다.
5. 업무 결과와 Attempt·Audit·Log·Metric·Trace를 같은 식별자로 연결한다.
6. 고객 API 또는 운영 화면에서 최종 상태·건수·금액·Version·오류 0건을 확인한다.

## 예외·장애·경계 사례

- 취소와 완료 경합
- 중복 취소
- Worker 재시작
- 진행률 후퇴
- 결과 URL 만료
- 상태 변경이 있는 경우 DB Commit 전 실패와 Commit 후 응답 유실을 구분한다.
- 같은 요청의 중복·동시 실행·순서 역전·늦은 응답을 포함한다.
- Retry 가능한 오류와 즉시 중단해야 하는 업무 오류를 구분한다.
- 대량·다중 대상 기능에서 부분 성공이 발생하면 성공 대상을 다시 실행하지 않고 실패 대상만 Reprocess·Reconcile한다.
- 복구·보상이 필요한 기능은 복구 실패와 보상 실패도 별도 상태·Attempt·Audit로 남긴다.

## 필수 Test

- Polling
- SSE가 있으면 보조
- 취소 Checkpoint
- 결과 보존
- 멱등
- 입력 Validation·권한·Data Scope·Masking Negative Test
- 해당 기능에 적용되는 중복·동시성·Timeout·응답 유실·부분 실패 Test
- 해당 기능의 Failure Injection 후 필요한 Retry·Restart·Reprocess·Reconcile·Rollback Test
- Oracle·PostgreSQL·MariaDB 또는 해당 Infra의 의미 동일성 Test
- Log·Metric·Trace·Audit에 Secret·개인정보 원문이 없는지 검사

## 운영·화면 확인

- ADM 비동기 작업 화면에서 진행률·취소·최종 결과 확인
- 화면에는 해당 메뉴에 실제로 존재하는 조회 조건, 기본값, Column, 상세 Field, 상태, Button 활성 조건, 사유, 승인, 예상 Version, 대상별 결과만 Source와 일치하게 표시한다. 조회 전용 메뉴에 Command 항목을 만들지 않는다.
- 운영자가 정상화 완료를 판단할 수 있는 건수·금액·Version·Checksum·최종 상태·마지막 오류를 제공한다.
- 변경·외부연계 기능에서 응답 유실이 가능한 경우에만 요청을 다시 보내기 전에 Operation·거래·상대 상태를 조회하는 절차를 Browser 또는 API Test로 검증한다.

## 고객 매뉴얼·EDU 연결

- `01_개발자매뉴얼.md`의 해당 기능 장에 `EDU-DEV-24`를 연결한다.
- 선행 환경, 실행 명령, 입력값, 정상 출력, 오류 재현 명령, 복구 명령, ADM/BZA/Gateway 확인 위치를 그대로 따라 할 수 있게 작성한다.
- 교육 코드에서 고객 업무로 바꿀 Package·Table·API·Topic·파일명과 CPF 관리 영역을 구분한 전환표를 제공한다.

## 완료 조건

- `EDU-DEV-24`가 교육 Catalog·Source·Test·고객 매뉴얼에 동일 ID로 존재한다.
- 정상·Validation·권한·중복·동시성·Timeout·응답 유실·부분 실패·복구 Test가 통과한다.
- 요청·응답·상태·DB·이벤트·파일·운영 화면이 양방향으로 일치한다.
- 운영자가 다른 사람의 설명 없이 장애를 재현하고 정상화 판정을 할 수 있다.
- 실행하지 않은 Test는 성공으로 기록하지 않고, 실패·제약이 남으면 전체 완료로 보고하지 않는다.

## 금지 사항

- 메모리 Map·고정 JSON·정상 Case 한 건만으로 완료 처리
- 교육용 고객 업무를 `cpf-core`, `cpf-starters/common`, Starter, Runtime 제품 내부에 삽입
- Module-local DB Vendor Fallback SQL 또는 Vendor별 의미가 다른 Schema
- Timeout이나 응답 유실 뒤 실제 결과 확인 없이 무조건 재호출
- 비밀번호·Token·개인정보 원문을 Source·Log·Fixture·문서에 기록
- 매뉴얼 설명과 다른 임의 Package·API·상태·메뉴를 새로 생성

---

# 59. EDU-DEV-25 — Webhook Callback 서명·재전송·Replay 방지

- 우선순위: **P0**
- 연결 매뉴얼: `01_개발자매뉴얼.md`
- Owner: `cpf-education`
- 구현 상태 목표: Source·SQL·API·Config·Test·운영 확인·매뉴얼 연결이 모두 완료

## 필요 근거

- 고객 매뉴얼에서 `Webhook Callback 서명·재전송·Replay 방지`을 설명하려면 정상 호출 예시만이 아니라 상태 변화와 장애 후 정상화까지 재현 가능한 실행 예제가 필요하다.
- 이 요구사항이 없으면 고객은 기능 가능 여부를 알더라도 실제 업무 적용 순서, 실패 판정, 재처리 금지 조건을 확인할 수 없다.
- 내부 구현 구조를 보여 주기 위한 예제가 아니라 고객 업무를 CPF로 만드는 방법과 운영 결과를 증명하는 예제로 구현한다.

## 고객 업무 시나리오

외부 고객 시스템으로 상태 변경 Callback을 보내고 실패 시 재전송하되 동일 이벤트는 한 번만 반영된다.

## 구현 위치와 책임 경계

- 업무 Source: `com.cpf.education.edu.dev25` 또는 같은 의미의 기존 표준 Slice
- Test: Owner Module의 기존 Test Root 아래 `EDU-DEV-25` 식별 가능한 Class·Fixture
- SQL: 중앙 DB Vendor Pack의 기존 Install·Upgrade·Rollback·Verify·Seed 구조
- Config: 기존 Prefix·Profile·Secret 참조 방식을 유지
- 고객 업무 상태 변경과 원장 데이터는 고객 업무 Owner가 소유한다. ADM·BZA·Gateway·운영 Script가 업무 Table을 직접 수정하지 않는다.
- 공통 기능이 필요하면 기존 Public API·SPI를 우선 사용하고, 진입점이 없을 때만 실제 Consumer를 근거로 Owner Module에 확장한다.

## 입력·계약

- `subscriptionId`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `callbackUrl`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `eventId`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `signature`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `timestamp`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- 해당 기능에 필요한 Operation ID·업무 ID·Correlation ID·요청 Hash·사용자·권한 문맥을 저장·Log·Trace·Audit에서 연결한다.
- 요청·응답·Job Parameter·이벤트·파일 Layout 중 해당되는 계약을 OpenAPI 또는 Machine-readable Schema로 제공한다.

## 상태와 저장 근거

- 필수 상태 흐름: `QUEUED → SENT → ACKED/RETRY_WAIT/DEAD`
- 상태마다 진입 조건, 허용 조치, 재시도 가능 여부, 최종 여부, 저장 위치와 Version 규칙을 정의한다.
- 결과 미확정·부분 성공이 가능한 경우 대상별 Attempt와 상대 상태 조회 근거를 별도 저장한다.

## 정상 처리 순서

1. 입력·권한·데이터 범위·기준 Version과 선행 상태를 검증한다.
2. 상태 변경·비동기 실행인 경우 Operation과 업무 식별자를 생성하거나 기존 멱등 요청을 조회한다. 조회 전용 기능은 조회 Context와 Cursor만 사용한다.
3. 상태 변경 기능은 고객 업무 Owner의 Application Use Case에서 Transaction 경계와 상태 전이를 수행하고, 조회 전용 기능은 일관된 Snapshot·정렬·범위 조건을 적용한다.
4. 외부 시스템·Kafka·파일·Batch·Gateway가 필요한 경우 Adapter 경계에서 Timeout·Retry·보안 정책을 적용한다.
5. 업무 결과와 Attempt·Audit·Log·Metric·Trace를 같은 식별자로 연결한다.
6. 고객 API 또는 운영 화면에서 최종 상태·건수·금액·Version·오류 0건을 확인한다.

## 예외·장애·경계 사례

- DNS 변경
- 2xx 후 응답 유실
- 4xx
- 5xx
- 만료 Timestamp
- Signature 오류
- Replay
- 상태 변경이 있는 경우 DB Commit 전 실패와 Commit 후 응답 유실을 구분한다.
- 같은 요청의 중복·동시 실행·순서 역전·늦은 응답을 포함한다.
- Retry 가능한 오류와 즉시 중단해야 하는 업무 오류를 구분한다.
- 대량·다중 대상 기능에서 부분 성공이 발생하면 성공 대상을 다시 실행하지 않고 실패 대상만 Reprocess·Reconcile한다.
- 복구·보상이 필요한 기능은 복구 실패와 보상 실패도 별도 상태·Attempt·Audit로 남긴다.

## 필수 Test

- HMAC 검증
- Retry backoff
- DLQ
- Callback attempt ledger
- SSRF 방지
- 입력 Validation·권한·Data Scope·Masking Negative Test
- 해당 기능에 적용되는 중복·동시성·Timeout·응답 유실·부분 실패 Test
- 해당 기능의 Failure Injection 후 필요한 Retry·Restart·Reprocess·Reconcile·Rollback Test
- Oracle·PostgreSQL·MariaDB 또는 해당 Infra의 의미 동일성 Test
- Log·Metric·Trace·Audit에 Secret·개인정보 원문이 없는지 검사

## 운영·화면 확인

- ADM 외부연계 시도 이력과 최종 ACK 확인
- 화면에는 해당 메뉴에 실제로 존재하는 조회 조건, 기본값, Column, 상세 Field, 상태, Button 활성 조건, 사유, 승인, 예상 Version, 대상별 결과만 Source와 일치하게 표시한다. 조회 전용 메뉴에 Command 항목을 만들지 않는다.
- 운영자가 정상화 완료를 판단할 수 있는 건수·금액·Version·Checksum·최종 상태·마지막 오류를 제공한다.
- 변경·외부연계 기능에서 응답 유실이 가능한 경우에만 요청을 다시 보내기 전에 Operation·거래·상대 상태를 조회하는 절차를 Browser 또는 API Test로 검증한다.

## 고객 매뉴얼·EDU 연결

- `01_개발자매뉴얼.md`의 해당 기능 장에 `EDU-DEV-25`를 연결한다.
- 선행 환경, 실행 명령, 입력값, 정상 출력, 오류 재현 명령, 복구 명령, ADM/BZA/Gateway 확인 위치를 그대로 따라 할 수 있게 작성한다.
- 교육 코드에서 고객 업무로 바꿀 Package·Table·API·Topic·파일명과 CPF 관리 영역을 구분한 전환표를 제공한다.

## 완료 조건

- `EDU-DEV-25`가 교육 Catalog·Source·Test·고객 매뉴얼에 동일 ID로 존재한다.
- 정상·Validation·권한·중복·동시성·Timeout·응답 유실·부분 실패·복구 Test가 통과한다.
- 요청·응답·상태·DB·이벤트·파일·운영 화면이 양방향으로 일치한다.
- 운영자가 다른 사람의 설명 없이 장애를 재현하고 정상화 판정을 할 수 있다.
- 실행하지 않은 Test는 성공으로 기록하지 않고, 실패·제약이 남으면 전체 완료로 보고하지 않는다.

## 금지 사항

- 메모리 Map·고정 JSON·정상 Case 한 건만으로 완료 처리
- 교육용 고객 업무를 `cpf-core`, `cpf-starters/common`, Starter, Runtime 제품 내부에 삽입
- Module-local DB Vendor Fallback SQL 또는 Vendor별 의미가 다른 Schema
- Timeout이나 응답 유실 뒤 실제 결과 확인 없이 무조건 재호출
- 비밀번호·Token·개인정보 원문을 Source·Log·Fixture·문서에 기록
- 매뉴얼 설명과 다른 임의 Package·API·상태·메뉴를 새로 생성

---

# 60. EDU-DEV-26 — SFTP 수신·송신·완료 파일 원자 처리

- 우선순위: **P0**
- 연결 매뉴얼: `01_개발자매뉴얼.md`
- Owner: `cpf-education`
- 구현 상태 목표: Source·SQL·API·Config·Test·운영 확인·매뉴얼 연결이 모두 완료

## 필요 근거

- 고객 매뉴얼에서 `SFTP 수신·송신·완료 파일 원자 처리`을 설명하려면 정상 호출 예시만이 아니라 상태 변화와 장애 후 정상화까지 재현 가능한 실행 예제가 필요하다.
- 이 요구사항이 없으면 고객은 기능 가능 여부를 알더라도 실제 업무 적용 순서, 실패 판정, 재처리 금지 조건을 확인할 수 없다.
- 내부 구현 구조를 보여 주기 위한 예제가 아니라 고객 업무를 CPF로 만드는 방법과 운영 결과를 증명하는 예제로 구현한다.

## 고객 업무 시나리오

기관 SFTP에서 파일을 내려받아 검증하고 결과 파일을 임시명으로 올린 뒤 rename으로 확정한다.

## 구현 위치와 책임 경계

- 업무 Source: `com.cpf.education.edu.dev26` 또는 같은 의미의 기존 표준 Slice
- Test: Owner Module의 기존 Test Root 아래 `EDU-DEV-26` 식별 가능한 Class·Fixture
- SQL: 중앙 DB Vendor Pack의 기존 Install·Upgrade·Rollback·Verify·Seed 구조
- Config: 기존 Prefix·Profile·Secret 참조 방식을 유지
- 고객 업무 상태 변경과 원장 데이터는 고객 업무 Owner가 소유한다. ADM·BZA·Gateway·운영 Script가 업무 Table을 직접 수정하지 않는다.
- 공통 기능이 필요하면 기존 Public API·SPI를 우선 사용하고, 진입점이 없을 때만 실제 Consumer를 근거로 Owner Module에 확장한다.

## 입력·계약

- `remotePath`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `filePattern`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `expectedChecksum`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `partnerId`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- 해당 기능에 필요한 Operation ID·업무 ID·Correlation ID·요청 Hash·사용자·권한 문맥을 저장·Log·Trace·Audit에서 연결한다.
- 요청·응답·Job Parameter·이벤트·파일 Layout 중 해당되는 계약을 OpenAPI 또는 Machine-readable Schema로 제공한다.

## 상태와 저장 근거

- 필수 상태 흐름: `DISCOVERED → DOWNLOADING → VERIFIED → PROCESSED → ARCHIVED`
- 상태마다 진입 조건, 허용 조치, 재시도 가능 여부, 최종 여부, 저장 위치와 Version 규칙을 정의한다.
- 결과 미확정·부분 성공이 가능한 경우 대상별 Attempt와 상대 상태 조회 근거를 별도 저장한다.

## 정상 처리 순서

1. 입력·권한·데이터 범위·기준 Version과 선행 상태를 검증한다.
2. 상태 변경·비동기 실행인 경우 Operation과 업무 식별자를 생성하거나 기존 멱등 요청을 조회한다. 조회 전용 기능은 조회 Context와 Cursor만 사용한다.
3. 상태 변경 기능은 고객 업무 Owner의 Application Use Case에서 Transaction 경계와 상태 전이를 수행하고, 조회 전용 기능은 일관된 Snapshot·정렬·범위 조건을 적용한다.
4. 외부 시스템·Kafka·파일·Batch·Gateway가 필요한 경우 Adapter 경계에서 Timeout·Retry·보안 정책을 적용한다.
5. 업무 결과와 Attempt·Audit·Log·Metric·Trace를 같은 식별자로 연결한다.
6. 고객 API 또는 운영 화면에서 최종 상태·건수·금액·Version·오류 0건을 확인한다.

## 예외·장애·경계 사례

- 다운로드 중 단절
- 같은 이름 재수신
- zero byte
- checksum 불일치
- rename 실패
- 권한 오류
- 상태 변경이 있는 경우 DB Commit 전 실패와 Commit 후 응답 유실을 구분한다.
- 같은 요청의 중복·동시 실행·순서 역전·늦은 응답을 포함한다.
- Retry 가능한 오류와 즉시 중단해야 하는 업무 오류를 구분한다.
- 대량·다중 대상 기능에서 부분 성공이 발생하면 성공 대상을 다시 실행하지 않고 실패 대상만 Reprocess·Reconcile한다.
- 복구·보상이 필요한 기능은 복구 실패와 보상 실패도 별도 상태·Attempt·Audit로 남긴다.

## 필수 Test

- Embedded SFTP 또는 Test Container
- 재개 정책
- 임시 파일 정리
- 감사
- 입력 Validation·권한·Data Scope·Masking Negative Test
- 해당 기능에 적용되는 중복·동시성·Timeout·응답 유실·부분 실패 Test
- 해당 기능의 Failure Injection 후 필요한 Retry·Restart·Reprocess·Reconcile·Rollback Test
- Oracle·PostgreSQL·MariaDB 또는 해당 Infra의 의미 동일성 Test
- Log·Metric·Trace·Audit에 Secret·개인정보 원문이 없는지 검사

## 운영·화면 확인

- ADM 파일 작업에서 원격 경로·checksum·시도·archive 확인
- 화면에는 해당 메뉴에 실제로 존재하는 조회 조건, 기본값, Column, 상세 Field, 상태, Button 활성 조건, 사유, 승인, 예상 Version, 대상별 결과만 Source와 일치하게 표시한다. 조회 전용 메뉴에 Command 항목을 만들지 않는다.
- 운영자가 정상화 완료를 판단할 수 있는 건수·금액·Version·Checksum·최종 상태·마지막 오류를 제공한다.
- 변경·외부연계 기능에서 응답 유실이 가능한 경우에만 요청을 다시 보내기 전에 Operation·거래·상대 상태를 조회하는 절차를 Browser 또는 API Test로 검증한다.

## 고객 매뉴얼·EDU 연결

- `01_개발자매뉴얼.md`의 해당 기능 장에 `EDU-DEV-26`를 연결한다.
- 선행 환경, 실행 명령, 입력값, 정상 출력, 오류 재현 명령, 복구 명령, ADM/BZA/Gateway 확인 위치를 그대로 따라 할 수 있게 작성한다.
- 교육 코드에서 고객 업무로 바꿀 Package·Table·API·Topic·파일명과 CPF 관리 영역을 구분한 전환표를 제공한다.

## 완료 조건

- `EDU-DEV-26`가 교육 Catalog·Source·Test·고객 매뉴얼에 동일 ID로 존재한다.
- 정상·Validation·권한·중복·동시성·Timeout·응답 유실·부분 실패·복구 Test가 통과한다.
- 요청·응답·상태·DB·이벤트·파일·운영 화면이 양방향으로 일치한다.
- 운영자가 다른 사람의 설명 없이 장애를 재현하고 정상화 판정을 할 수 있다.
- 실행하지 않은 Test는 성공으로 기록하지 않고, 실패·제약이 남으면 전체 완료로 보고하지 않는다.

## 금지 사항

- 메모리 Map·고정 JSON·정상 Case 한 건만으로 완료 처리
- 교육용 고객 업무를 `cpf-core`, `cpf-starters/common`, Starter, Runtime 제품 내부에 삽입
- Module-local DB Vendor Fallback SQL 또는 Vendor별 의미가 다른 Schema
- Timeout이나 응답 유실 뒤 실제 결과 확인 없이 무조건 재호출
- 비밀번호·Token·개인정보 원문을 Source·Log·Fixture·문서에 기록
- 매뉴얼 설명과 다른 임의 Package·API·상태·메뉴를 새로 생성

---

# 61. EDU-DEV-27 — SOAP·XML 외부기관 연계와 Fault 처리

- 우선순위: **P0**
- 연결 매뉴얼: `01_개발자매뉴얼.md`
- Owner: `cpf-education`
- 구현 상태 목표: Source·SQL·API·Config·Test·운영 확인·매뉴얼 연결이 모두 완료

## 필요 근거

- 고객 매뉴얼에서 `SOAP·XML 외부기관 연계와 Fault 처리`을 설명하려면 정상 호출 예시만이 아니라 상태 변화와 장애 후 정상화까지 재현 가능한 실행 예제가 필요하다.
- 이 요구사항이 없으면 고객은 기능 가능 여부를 알더라도 실제 업무 적용 순서, 실패 판정, 재처리 금지 조건을 확인할 수 없다.
- 내부 구현 구조를 보여 주기 위한 예제가 아니라 고객 업무를 CPF로 만드는 방법과 운영 결과를 증명하는 예제로 구현한다.

## 고객 업무 시나리오

레거시 기관의 WSDL 기반 XML 서비스를 호출하고 SOAP Fault와 업무 오류를 구분한다.

## 구현 위치와 책임 경계

- 업무 Source: `com.cpf.education.edu.dev27` 또는 같은 의미의 기존 표준 Slice
- Test: Owner Module의 기존 Test Root 아래 `EDU-DEV-27` 식별 가능한 Class·Fixture
- SQL: 중앙 DB Vendor Pack의 기존 Install·Upgrade·Rollback·Verify·Seed 구조
- Config: 기존 Prefix·Profile·Secret 참조 방식을 유지
- 고객 업무 상태 변경과 원장 데이터는 고객 업무 Owner가 소유한다. ADM·BZA·Gateway·운영 Script가 업무 Table을 직접 수정하지 않는다.
- 공통 기능이 필요하면 기존 Public API·SPI를 우선 사용하고, 진입점이 없을 때만 실제 Consumer를 근거로 Owner Module에 확장한다.

## 입력·계약

- `operation`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `SOAPAction`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `XML payload`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `correlationId`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- 해당 기능에 필요한 Operation ID·업무 ID·Correlation ID·요청 Hash·사용자·권한 문맥을 저장·Log·Trace·Audit에서 연결한다.
- 요청·응답·Job Parameter·이벤트·파일 Layout 중 해당되는 계약을 OpenAPI 또는 Machine-readable Schema로 제공한다.

## 상태와 저장 근거

- 필수 상태 흐름: `CREATED → SENT → RESPONSE_RECEIVED/FAULT/UNKNOWN_RESULT`
- 상태마다 진입 조건, 허용 조치, 재시도 가능 여부, 최종 여부, 저장 위치와 Version 규칙을 정의한다.
- 결과 미확정·부분 성공이 가능한 경우 대상별 Attempt와 상대 상태 조회 근거를 별도 저장한다.

## 정상 처리 순서

1. 입력·권한·데이터 범위·기준 Version과 선행 상태를 검증한다.
2. 상태 변경·비동기 실행인 경우 Operation과 업무 식별자를 생성하거나 기존 멱등 요청을 조회한다. 조회 전용 기능은 조회 Context와 Cursor만 사용한다.
3. 상태 변경 기능은 고객 업무 Owner의 Application Use Case에서 Transaction 경계와 상태 전이를 수행하고, 조회 전용 기능은 일관된 Snapshot·정렬·범위 조건을 적용한다.
4. 외부 시스템·Kafka·파일·Batch·Gateway가 필요한 경우 Adapter 경계에서 Timeout·Retry·보안 정책을 적용한다.
5. 업무 결과와 Attempt·Audit·Log·Metric·Trace를 같은 식별자로 연결한다.
6. 고객 API 또는 운영 화면에서 최종 상태·건수·금액·Version·오류 0건을 확인한다.

## 예외·장애·경계 사례

- Namespace 오류
- Schema 불일치
- HTTP 500 SOAP Fault
- Timeout
- 중복 전송
- 상태 변경이 있는 경우 DB Commit 전 실패와 Commit 후 응답 유실을 구분한다.
- 같은 요청의 중복·동시 실행·순서 역전·늦은 응답을 포함한다.
- Retry 가능한 오류와 즉시 중단해야 하는 업무 오류를 구분한다.
- 대량·다중 대상 기능에서 부분 성공이 발생하면 성공 대상을 다시 실행하지 않고 실패 대상만 Reprocess·Reconcile한다.
- 복구·보상이 필요한 기능은 복구 실패와 보상 실패도 별도 상태·Attempt·Audit로 남긴다.

## 필수 Test

- XSD 검증
- XML 보안(XXE 금지)
- Fault 매핑
- 결과 대사
- 입력 Validation·권한·Data Scope·Masking Negative Test
- 해당 기능에 적용되는 중복·동시성·Timeout·응답 유실·부분 실패 Test
- 해당 기능의 Failure Injection 후 필요한 Retry·Restart·Reprocess·Reconcile·Rollback Test
- Oracle·PostgreSQL·MariaDB 또는 해당 Infra의 의미 동일성 Test
- Log·Metric·Trace·Audit에 Secret·개인정보 원문이 없는지 검사

## 운영·화면 확인

- ADM 외부연계 화면에서 원문을 Masking하고 Fault code 확인
- 화면에는 해당 메뉴에 실제로 존재하는 조회 조건, 기본값, Column, 상세 Field, 상태, Button 활성 조건, 사유, 승인, 예상 Version, 대상별 결과만 Source와 일치하게 표시한다. 조회 전용 메뉴에 Command 항목을 만들지 않는다.
- 운영자가 정상화 완료를 판단할 수 있는 건수·금액·Version·Checksum·최종 상태·마지막 오류를 제공한다.
- 변경·외부연계 기능에서 응답 유실이 가능한 경우에만 요청을 다시 보내기 전에 Operation·거래·상대 상태를 조회하는 절차를 Browser 또는 API Test로 검증한다.

## 고객 매뉴얼·EDU 연결

- `01_개발자매뉴얼.md`의 해당 기능 장에 `EDU-DEV-27`를 연결한다.
- 선행 환경, 실행 명령, 입력값, 정상 출력, 오류 재현 명령, 복구 명령, ADM/BZA/Gateway 확인 위치를 그대로 따라 할 수 있게 작성한다.
- 교육 코드에서 고객 업무로 바꿀 Package·Table·API·Topic·파일명과 CPF 관리 영역을 구분한 전환표를 제공한다.

## 완료 조건

- `EDU-DEV-27`가 교육 Catalog·Source·Test·고객 매뉴얼에 동일 ID로 존재한다.
- 정상·Validation·권한·중복·동시성·Timeout·응답 유실·부분 실패·복구 Test가 통과한다.
- 요청·응답·상태·DB·이벤트·파일·운영 화면이 양방향으로 일치한다.
- 운영자가 다른 사람의 설명 없이 장애를 재현하고 정상화 판정을 할 수 있다.
- 실행하지 않은 Test는 성공으로 기록하지 않고, 실패·제약이 남으면 전체 완료로 보고하지 않는다.

## 금지 사항

- 메모리 Map·고정 JSON·정상 Case 한 건만으로 완료 처리
- 교육용 고객 업무를 `cpf-core`, `cpf-starters/common`, Starter, Runtime 제품 내부에 삽입
- Module-local DB Vendor Fallback SQL 또는 Vendor별 의미가 다른 Schema
- Timeout이나 응답 유실 뒤 실제 결과 확인 없이 무조건 재호출
- 비밀번호·Token·개인정보 원문을 Source·Log·Fixture·문서에 기록
- 매뉴얼 설명과 다른 임의 Package·API·상태·메뉴를 새로 생성

---

# 62. EDU-DEV-28 — 대용량 Multipart 업로드·중단 재개

- 우선순위: **P0**
- 연결 매뉴얼: `01_개발자매뉴얼.md`
- Owner: `cpf-education`
- 구현 상태 목표: Source·SQL·API·Config·Test·운영 확인·매뉴얼 연결이 모두 완료

## 필요 근거

- 고객 매뉴얼에서 `대용량 Multipart 업로드·중단 재개`을 설명하려면 정상 호출 예시만이 아니라 상태 변화와 장애 후 정상화까지 재현 가능한 실행 예제가 필요하다.
- 이 요구사항이 없으면 고객은 기능 가능 여부를 알더라도 실제 업무 적용 순서, 실패 판정, 재처리 금지 조건을 확인할 수 없다.
- 내부 구현 구조를 보여 주기 위한 예제가 아니라 고객 업무를 CPF로 만드는 방법과 운영 결과를 증명하는 예제로 구현한다.

## 고객 업무 시나리오

1GB 자료를 여러 Part로 업로드하고 중단된 Part만 재전송해 최종 파일을 확정한다.

## 구현 위치와 책임 경계

- 업무 Source: `com.cpf.education.edu.dev28` 또는 같은 의미의 기존 표준 Slice
- Test: Owner Module의 기존 Test Root 아래 `EDU-DEV-28` 식별 가능한 Class·Fixture
- SQL: 중앙 DB Vendor Pack의 기존 Install·Upgrade·Rollback·Verify·Seed 구조
- Config: 기존 Prefix·Profile·Secret 참조 방식을 유지
- 고객 업무 상태 변경과 원장 데이터는 고객 업무 Owner가 소유한다. ADM·BZA·Gateway·운영 Script가 업무 Table을 직접 수정하지 않는다.
- 공통 기능이 필요하면 기존 Public API·SPI를 우선 사용하고, 진입점이 없을 때만 실제 Consumer를 근거로 Owner Module에 확장한다.

## 입력·계약

- `uploadId`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `partNo`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `partChecksum`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `totalParts`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- 해당 기능에 필요한 Operation ID·업무 ID·Correlation ID·요청 Hash·사용자·권한 문맥을 저장·Log·Trace·Audit에서 연결한다.
- 요청·응답·Job Parameter·이벤트·파일 Layout 중 해당되는 계약을 OpenAPI 또는 Machine-readable Schema로 제공한다.

## 상태와 저장 근거

- 필수 상태 흐름: `INITIATED → PART_UPLOADING → ASSEMBLING → COMPLETED/ABORTED`
- 상태마다 진입 조건, 허용 조치, 재시도 가능 여부, 최종 여부, 저장 위치와 Version 규칙을 정의한다.
- 결과 미확정·부분 성공이 가능한 경우 대상별 Attempt와 상대 상태 조회 근거를 별도 저장한다.

## 정상 처리 순서

1. 입력·권한·데이터 범위·기준 Version과 선행 상태를 검증한다.
2. 상태 변경·비동기 실행인 경우 Operation과 업무 식별자를 생성하거나 기존 멱등 요청을 조회한다. 조회 전용 기능은 조회 Context와 Cursor만 사용한다.
3. 상태 변경 기능은 고객 업무 Owner의 Application Use Case에서 Transaction 경계와 상태 전이를 수행하고, 조회 전용 기능은 일관된 Snapshot·정렬·범위 조건을 적용한다.
4. 외부 시스템·Kafka·파일·Batch·Gateway가 필요한 경우 Adapter 경계에서 Timeout·Retry·보안 정책을 적용한다.
5. 업무 결과와 Attempt·Audit·Log·Metric·Trace를 같은 식별자로 연결한다.
6. 고객 API 또는 운영 화면에서 최종 상태·건수·금액·Version·오류 0건을 확인한다.

## 예외·장애·경계 사례

- Part 중복
- 순서 무관
- checksum 불일치
- 만료 uploadId
- 조립 중 장애
- 취소
- 상태 변경이 있는 경우 DB Commit 전 실패와 Commit 후 응답 유실을 구분한다.
- 같은 요청의 중복·동시 실행·순서 역전·늦은 응답을 포함한다.
- Retry 가능한 오류와 즉시 중단해야 하는 업무 오류를 구분한다.
- 대량·다중 대상 기능에서 부분 성공이 발생하면 성공 대상을 다시 실행하지 않고 실패 대상만 Reprocess·Reconcile한다.
- 복구·보상이 필요한 기능은 복구 실패와 보상 실패도 별도 상태·Attempt·Audit로 남긴다.

## 필수 Test

- Part 멱등
- 임시 저장 한도
- 병렬 업로드
- 조립 Hash
- 정리 Job
- 입력 Validation·권한·Data Scope·Masking Negative Test
- 해당 기능에 적용되는 중복·동시성·Timeout·응답 유실·부분 실패 Test
- 해당 기능의 Failure Injection 후 필요한 Retry·Restart·Reprocess·Reconcile·Rollback Test
- Oracle·PostgreSQL·MariaDB 또는 해당 Infra의 의미 동일성 Test
- Log·Metric·Trace·Audit에 Secret·개인정보 원문이 없는지 검사

## 운영·화면 확인

- ADM 파일 작업에서 Part 수·용량·만료·최종 checksum 확인
- 화면에는 해당 메뉴에 실제로 존재하는 조회 조건, 기본값, Column, 상세 Field, 상태, Button 활성 조건, 사유, 승인, 예상 Version, 대상별 결과만 Source와 일치하게 표시한다. 조회 전용 메뉴에 Command 항목을 만들지 않는다.
- 운영자가 정상화 완료를 판단할 수 있는 건수·금액·Version·Checksum·최종 상태·마지막 오류를 제공한다.
- 변경·외부연계 기능에서 응답 유실이 가능한 경우에만 요청을 다시 보내기 전에 Operation·거래·상대 상태를 조회하는 절차를 Browser 또는 API Test로 검증한다.

## 고객 매뉴얼·EDU 연결

- `01_개발자매뉴얼.md`의 해당 기능 장에 `EDU-DEV-28`를 연결한다.
- 선행 환경, 실행 명령, 입력값, 정상 출력, 오류 재현 명령, 복구 명령, ADM/BZA/Gateway 확인 위치를 그대로 따라 할 수 있게 작성한다.
- 교육 코드에서 고객 업무로 바꿀 Package·Table·API·Topic·파일명과 CPF 관리 영역을 구분한 전환표를 제공한다.

## 완료 조건

- `EDU-DEV-28`가 교육 Catalog·Source·Test·고객 매뉴얼에 동일 ID로 존재한다.
- 정상·Validation·권한·중복·동시성·Timeout·응답 유실·부분 실패·복구 Test가 통과한다.
- 요청·응답·상태·DB·이벤트·파일·운영 화면이 양방향으로 일치한다.
- 운영자가 다른 사람의 설명 없이 장애를 재현하고 정상화 판정을 할 수 있다.
- 실행하지 않은 Test는 성공으로 기록하지 않고, 실패·제약이 남으면 전체 완료로 보고하지 않는다.

## 금지 사항

- 메모리 Map·고정 JSON·정상 Case 한 건만으로 완료 처리
- 교육용 고객 업무를 `cpf-core`, `cpf-starters/common`, Starter, Runtime 제품 내부에 삽입
- Module-local DB Vendor Fallback SQL 또는 Vendor별 의미가 다른 Schema
- Timeout이나 응답 유실 뒤 실제 결과 확인 없이 무조건 재호출
- 비밀번호·Token·개인정보 원문을 Source·Log·Fixture·문서에 기록
- 매뉴얼 설명과 다른 임의 Package·API·상태·메뉴를 새로 생성

---

# 63. EDU-DEV-29 — 악성코드 검사·격리·승인 해제

- 우선순위: **P0**
- 연결 매뉴얼: `01_개발자매뉴얼.md`
- Owner: `cpf-education`
- 구현 상태 목표: Source·SQL·API·Config·Test·운영 확인·매뉴얼 연결이 모두 완료

## 필요 근거

- 고객 매뉴얼에서 `악성코드 검사·격리·승인 해제`을 설명하려면 정상 호출 예시만이 아니라 상태 변화와 장애 후 정상화까지 재현 가능한 실행 예제가 필요하다.
- 이 요구사항이 없으면 고객은 기능 가능 여부를 알더라도 실제 업무 적용 순서, 실패 판정, 재처리 금지 조건을 확인할 수 없다.
- 내부 구현 구조를 보여 주기 위한 예제가 아니라 고객 업무를 CPF로 만드는 방법과 운영 결과를 증명하는 예제로 구현한다.

## 고객 업무 시나리오

첨부파일은 저장 후 검사 완료 전 다운로드되지 않고 감염 파일은 격리된다.

## 구현 위치와 책임 경계

- 업무 Source: `com.cpf.education.edu.dev29` 또는 같은 의미의 기존 표준 Slice
- Test: Owner Module의 기존 Test Root 아래 `EDU-DEV-29` 식별 가능한 Class·Fixture
- SQL: 중앙 DB Vendor Pack의 기존 Install·Upgrade·Rollback·Verify·Seed 구조
- Config: 기존 Prefix·Profile·Secret 참조 방식을 유지
- 고객 업무 상태 변경과 원장 데이터는 고객 업무 Owner가 소유한다. ADM·BZA·Gateway·운영 Script가 업무 Table을 직접 수정하지 않는다.
- 공통 기능이 필요하면 기존 Public API·SPI를 우선 사용하고, 진입점이 없을 때만 실제 Consumer를 근거로 Owner Module에 확장한다.

## 입력·계약

- `attachmentId`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `scanProfile`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `uploader`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- 해당 기능에 필요한 Operation ID·업무 ID·Correlation ID·요청 Hash·사용자·권한 문맥을 저장·Log·Trace·Audit에서 연결한다.
- 요청·응답·Job Parameter·이벤트·파일 Layout 중 해당되는 계약을 OpenAPI 또는 Machine-readable Schema로 제공한다.

## 상태와 저장 근거

- 필수 상태 흐름: `RECEIVED → QUARANTINED → CLEAN/INFECTED/SCAN_FAILED → RELEASED/DELETED`
- 상태마다 진입 조건, 허용 조치, 재시도 가능 여부, 최종 여부, 저장 위치와 Version 규칙을 정의한다.
- 결과 미확정·부분 성공이 가능한 경우 대상별 Attempt와 상대 상태 조회 근거를 별도 저장한다.

## 정상 처리 순서

1. 입력·권한·데이터 범위·기준 Version과 선행 상태를 검증한다.
2. 상태 변경·비동기 실행인 경우 Operation과 업무 식별자를 생성하거나 기존 멱등 요청을 조회한다. 조회 전용 기능은 조회 Context와 Cursor만 사용한다.
3. 상태 변경 기능은 고객 업무 Owner의 Application Use Case에서 Transaction 경계와 상태 전이를 수행하고, 조회 전용 기능은 일관된 Snapshot·정렬·범위 조건을 적용한다.
4. 외부 시스템·Kafka·파일·Batch·Gateway가 필요한 경우 Adapter 경계에서 Timeout·Retry·보안 정책을 적용한다.
5. 업무 결과와 Attempt·Audit·Log·Metric·Trace를 같은 식별자로 연결한다.
6. 고객 API 또는 운영 화면에서 최종 상태·건수·금액·Version·오류 0건을 확인한다.

## 예외·장애·경계 사례

- Scanner Timeout
- 엔진 미가동
- false positive 승인
- 재검사
- 감염 파일 다운로드 시도
- 상태 변경이 있는 경우 DB Commit 전 실패와 Commit 후 응답 유실을 구분한다.
- 같은 요청의 중복·동시 실행·순서 역전·늦은 응답을 포함한다.
- Retry 가능한 오류와 즉시 중단해야 하는 업무 오류를 구분한다.
- 대량·다중 대상 기능에서 부분 성공이 발생하면 성공 대상을 다시 실행하지 않고 실패 대상만 Reprocess·Reconcile한다.
- 복구·보상이 필요한 기능은 복구 실패와 보상 실패도 별도 상태·Attempt·Audit로 남긴다.

## 필수 Test

- Scanner mock
- EICAR fixture
- 권한
- 감사
- 원문 Log 금지
- 입력 Validation·권한·Data Scope·Masking Negative Test
- 해당 기능에 적용되는 중복·동시성·Timeout·응답 유실·부분 실패 Test
- 해당 기능의 Failure Injection 후 필요한 Retry·Restart·Reprocess·Reconcile·Rollback Test
- Oracle·PostgreSQL·MariaDB 또는 해당 Infra의 의미 동일성 Test
- Log·Metric·Trace·Audit에 Secret·개인정보 원문이 없는지 검사

## 운영·화면 확인

- ADM 파일·보안 메뉴에서 격리·검사결과·승인 사유 확인
- 화면에는 해당 메뉴에 실제로 존재하는 조회 조건, 기본값, Column, 상세 Field, 상태, Button 활성 조건, 사유, 승인, 예상 Version, 대상별 결과만 Source와 일치하게 표시한다. 조회 전용 메뉴에 Command 항목을 만들지 않는다.
- 운영자가 정상화 완료를 판단할 수 있는 건수·금액·Version·Checksum·최종 상태·마지막 오류를 제공한다.
- 변경·외부연계 기능에서 응답 유실이 가능한 경우에만 요청을 다시 보내기 전에 Operation·거래·상대 상태를 조회하는 절차를 Browser 또는 API Test로 검증한다.

## 고객 매뉴얼·EDU 연결

- `01_개발자매뉴얼.md`의 해당 기능 장에 `EDU-DEV-29`를 연결한다.
- 선행 환경, 실행 명령, 입력값, 정상 출력, 오류 재현 명령, 복구 명령, ADM/BZA/Gateway 확인 위치를 그대로 따라 할 수 있게 작성한다.
- 교육 코드에서 고객 업무로 바꿀 Package·Table·API·Topic·파일명과 CPF 관리 영역을 구분한 전환표를 제공한다.

## 완료 조건

- `EDU-DEV-29`가 교육 Catalog·Source·Test·고객 매뉴얼에 동일 ID로 존재한다.
- 정상·Validation·권한·중복·동시성·Timeout·응답 유실·부분 실패·복구 Test가 통과한다.
- 요청·응답·상태·DB·이벤트·파일·운영 화면이 양방향으로 일치한다.
- 운영자가 다른 사람의 설명 없이 장애를 재현하고 정상화 판정을 할 수 있다.
- 실행하지 않은 Test는 성공으로 기록하지 않고, 실패·제약이 남으면 전체 완료로 보고하지 않는다.

## 금지 사항

- 메모리 Map·고정 JSON·정상 Case 한 건만으로 완료 처리
- 교육용 고객 업무를 `cpf-core`, `cpf-starters/common`, Starter, Runtime 제품 내부에 삽입
- Module-local DB Vendor Fallback SQL 또는 Vendor별 의미가 다른 Schema
- Timeout이나 응답 유실 뒤 실제 결과 확인 없이 무조건 재호출
- 비밀번호·Token·개인정보 원문을 Source·Log·Fixture·문서에 기록
- 매뉴얼 설명과 다른 임의 Package·API·상태·메뉴를 새로 생성

---

# 64. EDU-DEV-30 — Object Storage 보존·버전·법적 보류

- 우선순위: **P0**
- 연결 매뉴얼: `01_개발자매뉴얼.md`
- Owner: `cpf-education`
- 구현 상태 목표: Source·SQL·API·Config·Test·운영 확인·매뉴얼 연결이 모두 완료

## 필요 근거

- 고객 매뉴얼에서 `Object Storage 보존·버전·법적 보류`을 설명하려면 정상 호출 예시만이 아니라 상태 변화와 장애 후 정상화까지 재현 가능한 실행 예제가 필요하다.
- 이 요구사항이 없으면 고객은 기능 가능 여부를 알더라도 실제 업무 적용 순서, 실패 판정, 재처리 금지 조건을 확인할 수 없다.
- 내부 구현 구조를 보여 주기 위한 예제가 아니라 고객 업무를 CPF로 만드는 방법과 운영 결과를 증명하는 예제로 구현한다.

## 고객 업무 시나리오

고객 문서를 Object Storage에 저장하고 버전·보존기간·법적 보류를 관리한다.

## 구현 위치와 책임 경계

- 업무 Source: `com.cpf.education.edu.dev30` 또는 같은 의미의 기존 표준 Slice
- Test: Owner Module의 기존 Test Root 아래 `EDU-DEV-30` 식별 가능한 Class·Fixture
- SQL: 중앙 DB Vendor Pack의 기존 Install·Upgrade·Rollback·Verify·Seed 구조
- Config: 기존 Prefix·Profile·Secret 참조 방식을 유지
- 고객 업무 상태 변경과 원장 데이터는 고객 업무 Owner가 소유한다. ADM·BZA·Gateway·운영 Script가 업무 Table을 직접 수정하지 않는다.
- 공통 기능이 필요하면 기존 Public API·SPI를 우선 사용하고, 진입점이 없을 때만 실제 Consumer를 근거로 Owner Module에 확장한다.

## 입력·계약

- `objectKey`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `versionId`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `retentionUntil`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `legalHold`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- 해당 기능에 필요한 Operation ID·업무 ID·Correlation ID·요청 Hash·사용자·권한 문맥을 저장·Log·Trace·Audit에서 연결한다.
- 요청·응답·Job Parameter·이벤트·파일 Layout 중 해당되는 계약을 OpenAPI 또는 Machine-readable Schema로 제공한다.

## 상태와 저장 근거

- 필수 상태 흐름: `UPLOADED → RETAINED → HOLD/RELEASABLE → DELETED`
- 상태마다 진입 조건, 허용 조치, 재시도 가능 여부, 최종 여부, 저장 위치와 Version 규칙을 정의한다.
- 결과 미확정·부분 성공이 가능한 경우 대상별 Attempt와 상대 상태 조회 근거를 별도 저장한다.

## 정상 처리 순서

1. 입력·권한·데이터 범위·기준 Version과 선행 상태를 검증한다.
2. 상태 변경·비동기 실행인 경우 Operation과 업무 식별자를 생성하거나 기존 멱등 요청을 조회한다. 조회 전용 기능은 조회 Context와 Cursor만 사용한다.
3. 상태 변경 기능은 고객 업무 Owner의 Application Use Case에서 Transaction 경계와 상태 전이를 수행하고, 조회 전용 기능은 일관된 Snapshot·정렬·범위 조건을 적용한다.
4. 외부 시스템·Kafka·파일·Batch·Gateway가 필요한 경우 Adapter 경계에서 Timeout·Retry·보안 정책을 적용한다.
5. 업무 결과와 Attempt·Audit·Log·Metric·Trace를 같은 식별자로 연결한다.
6. 고객 API 또는 운영 화면에서 최종 상태·건수·금액·Version·오류 0건을 확인한다.

## 예외·장애·경계 사례

- 동일 Key 덮어쓰기
- 만료 전 삭제
- legal hold 해제 권한
- Storage ACK 유실
- 상태 변경이 있는 경우 DB Commit 전 실패와 Commit 후 응답 유실을 구분한다.
- 같은 요청의 중복·동시 실행·순서 역전·늦은 응답을 포함한다.
- Retry 가능한 오류와 즉시 중단해야 하는 업무 오류를 구분한다.
- 대량·다중 대상 기능에서 부분 성공이 발생하면 성공 대상을 다시 실행하지 않고 실패 대상만 Reprocess·Reconcile한다.
- 복구·보상이 필요한 기능은 복구 실패와 보상 실패도 별도 상태·Attempt·Audit로 남긴다.

## 필수 Test

- S3 호환 Test
- Versioning
- checksum
- presigned URL 만료
- Audit
- 입력 Validation·권한·Data Scope·Masking Negative Test
- 해당 기능에 적용되는 중복·동시성·Timeout·응답 유실·부분 실패 Test
- 해당 기능의 Failure Injection 후 필요한 Retry·Restart·Reprocess·Reconcile·Rollback Test
- Oracle·PostgreSQL·MariaDB 또는 해당 Infra의 의미 동일성 Test
- Log·Metric·Trace·Audit에 Secret·개인정보 원문이 없는지 검사

## 운영·화면 확인

- ADM 첨부·감사에서 version·retention·hold 확인
- 화면에는 해당 메뉴에 실제로 존재하는 조회 조건, 기본값, Column, 상세 Field, 상태, Button 활성 조건, 사유, 승인, 예상 Version, 대상별 결과만 Source와 일치하게 표시한다. 조회 전용 메뉴에 Command 항목을 만들지 않는다.
- 운영자가 정상화 완료를 판단할 수 있는 건수·금액·Version·Checksum·최종 상태·마지막 오류를 제공한다.
- 변경·외부연계 기능에서 응답 유실이 가능한 경우에만 요청을 다시 보내기 전에 Operation·거래·상대 상태를 조회하는 절차를 Browser 또는 API Test로 검증한다.

## 고객 매뉴얼·EDU 연결

- `01_개발자매뉴얼.md`의 해당 기능 장에 `EDU-DEV-30`를 연결한다.
- 선행 환경, 실행 명령, 입력값, 정상 출력, 오류 재현 명령, 복구 명령, ADM/BZA/Gateway 확인 위치를 그대로 따라 할 수 있게 작성한다.
- 교육 코드에서 고객 업무로 바꿀 Package·Table·API·Topic·파일명과 CPF 관리 영역을 구분한 전환표를 제공한다.

## 완료 조건

- `EDU-DEV-30`가 교육 Catalog·Source·Test·고객 매뉴얼에 동일 ID로 존재한다.
- 정상·Validation·권한·중복·동시성·Timeout·응답 유실·부분 실패·복구 Test가 통과한다.
- 요청·응답·상태·DB·이벤트·파일·운영 화면이 양방향으로 일치한다.
- 운영자가 다른 사람의 설명 없이 장애를 재현하고 정상화 판정을 할 수 있다.
- 실행하지 않은 Test는 성공으로 기록하지 않고, 실패·제약이 남으면 전체 완료로 보고하지 않는다.

## 금지 사항

- 메모리 Map·고정 JSON·정상 Case 한 건만으로 완료 처리
- 교육용 고객 업무를 `cpf-core`, `cpf-starters/common`, Starter, Runtime 제품 내부에 삽입
- Module-local DB Vendor Fallback SQL 또는 Vendor별 의미가 다른 Schema
- Timeout이나 응답 유실 뒤 실제 결과 확인 없이 무조건 재호출
- 비밀번호·Token·개인정보 원문을 Source·Log·Fixture·문서에 기록
- 매뉴얼 설명과 다른 임의 Package·API·상태·메뉴를 새로 생성

---

# 65. EDU-DEV-31 — 다중 채널 알림 선호·재시도·대체 채널

- 우선순위: **P0**
- 연결 매뉴얼: `01_개발자매뉴얼.md`
- Owner: `cpf-education`
- 구현 상태 목표: Source·SQL·API·Config·Test·운영 확인·매뉴얼 연결이 모두 완료

## 필요 근거

- 고객 매뉴얼에서 `다중 채널 알림 선호·재시도·대체 채널`을 설명하려면 정상 호출 예시만이 아니라 상태 변화와 장애 후 정상화까지 재현 가능한 실행 예제가 필요하다.
- 이 요구사항이 없으면 고객은 기능 가능 여부를 알더라도 실제 업무 적용 순서, 실패 판정, 재처리 금지 조건을 확인할 수 없다.
- 내부 구현 구조를 보여 주기 위한 예제가 아니라 고객 업무를 CPF로 만드는 방법과 운영 결과를 증명하는 예제로 구현한다.

## 고객 업무 시나리오

SMS 실패 시 고객 선호와 동의 범위 안에서 Email·Push로 대체한다.

## 구현 위치와 책임 경계

- 업무 Source: `com.cpf.education.edu.dev31` 또는 같은 의미의 기존 표준 Slice
- Test: Owner Module의 기존 Test Root 아래 `EDU-DEV-31` 식별 가능한 Class·Fixture
- SQL: 중앙 DB Vendor Pack의 기존 Install·Upgrade·Rollback·Verify·Seed 구조
- Config: 기존 Prefix·Profile·Secret 참조 방식을 유지
- 고객 업무 상태 변경과 원장 데이터는 고객 업무 Owner가 소유한다. ADM·BZA·Gateway·운영 Script가 업무 Table을 직접 수정하지 않는다.
- 공통 기능이 필요하면 기존 Public API·SPI를 우선 사용하고, 진입점이 없을 때만 실제 Consumer를 근거로 Owner Module에 확장한다.

## 입력·계약

- `recipientId`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `templateId`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `channels`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `locale`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `consentVersion`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- 해당 기능에 필요한 Operation ID·업무 ID·Correlation ID·요청 Hash·사용자·권한 문맥을 저장·Log·Trace·Audit에서 연결한다.
- 요청·응답·Job Parameter·이벤트·파일 Layout 중 해당되는 계약을 OpenAPI 또는 Machine-readable Schema로 제공한다.

## 상태와 저장 근거

- 필수 상태 흐름: `REQUESTED → CHANNEL_SELECTED → SENT/FAILED/FALLBACK → DELIVERED`
- 상태마다 진입 조건, 허용 조치, 재시도 가능 여부, 최종 여부, 저장 위치와 Version 규칙을 정의한다.
- 결과 미확정·부분 성공이 가능한 경우 대상별 Attempt와 상대 상태 조회 근거를 별도 저장한다.

## 정상 처리 순서

1. 입력·권한·데이터 범위·기준 Version과 선행 상태를 검증한다.
2. 상태 변경·비동기 실행인 경우 Operation과 업무 식별자를 생성하거나 기존 멱등 요청을 조회한다. 조회 전용 기능은 조회 Context와 Cursor만 사용한다.
3. 상태 변경 기능은 고객 업무 Owner의 Application Use Case에서 Transaction 경계와 상태 전이를 수행하고, 조회 전용 기능은 일관된 Snapshot·정렬·범위 조건을 적용한다.
4. 외부 시스템·Kafka·파일·Batch·Gateway가 필요한 경우 Adapter 경계에서 Timeout·Retry·보안 정책을 적용한다.
5. 업무 결과와 Attempt·Audit·Log·Metric·Trace를 같은 식별자로 연결한다.
6. 고객 API 또는 운영 화면에서 최종 상태·건수·금액·Version·오류 0건을 확인한다.

## 예외·장애·경계 사례

- 수신거부
- 야간 제한
- Provider 429
- 중복 발송
- Template 누락
- 개인정보 가림
- 상태 변경이 있는 경우 DB Commit 전 실패와 Commit 후 응답 유실을 구분한다.
- 같은 요청의 중복·동시 실행·순서 역전·늦은 응답을 포함한다.
- Retry 가능한 오류와 즉시 중단해야 하는 업무 오류를 구분한다.
- 대량·다중 대상 기능에서 부분 성공이 발생하면 성공 대상을 다시 실행하지 않고 실패 대상만 Reprocess·Reconcile한다.
- 복구·보상이 필요한 기능은 복구 실패와 보상 실패도 별도 상태·Attempt·Audit로 남긴다.

## 필수 Test

- 채널 우선순위
- Retry
- Delivery receipt
- 동의 Version
- 중복 방지
- 입력 Validation·권한·Data Scope·Masking Negative Test
- 해당 기능에 적용되는 중복·동시성·Timeout·응답 유실·부분 실패 Test
- 해당 기능의 Failure Injection 후 필요한 Retry·Restart·Reprocess·Reconcile·Rollback Test
- Oracle·PostgreSQL·MariaDB 또는 해당 Infra의 의미 동일성 Test
- Log·Metric·Trace·Audit에 Secret·개인정보 원문이 없는지 검사

## 운영·화면 확인

- ADM 알림 이력에서 채널·시도·대체·최종 상태 확인
- 화면에는 해당 메뉴에 실제로 존재하는 조회 조건, 기본값, Column, 상세 Field, 상태, Button 활성 조건, 사유, 승인, 예상 Version, 대상별 결과만 Source와 일치하게 표시한다. 조회 전용 메뉴에 Command 항목을 만들지 않는다.
- 운영자가 정상화 완료를 판단할 수 있는 건수·금액·Version·Checksum·최종 상태·마지막 오류를 제공한다.
- 변경·외부연계 기능에서 응답 유실이 가능한 경우에만 요청을 다시 보내기 전에 Operation·거래·상대 상태를 조회하는 절차를 Browser 또는 API Test로 검증한다.

## 고객 매뉴얼·EDU 연결

- `01_개발자매뉴얼.md`의 해당 기능 장에 `EDU-DEV-31`를 연결한다.
- 선행 환경, 실행 명령, 입력값, 정상 출력, 오류 재현 명령, 복구 명령, ADM/BZA/Gateway 확인 위치를 그대로 따라 할 수 있게 작성한다.
- 교육 코드에서 고객 업무로 바꿀 Package·Table·API·Topic·파일명과 CPF 관리 영역을 구분한 전환표를 제공한다.

## 완료 조건

- `EDU-DEV-31`가 교육 Catalog·Source·Test·고객 매뉴얼에 동일 ID로 존재한다.
- 정상·Validation·권한·중복·동시성·Timeout·응답 유실·부분 실패·복구 Test가 통과한다.
- 요청·응답·상태·DB·이벤트·파일·운영 화면이 양방향으로 일치한다.
- 운영자가 다른 사람의 설명 없이 장애를 재현하고 정상화 판정을 할 수 있다.
- 실행하지 않은 Test는 성공으로 기록하지 않고, 실패·제약이 남으면 전체 완료로 보고하지 않는다.

## 금지 사항

- 메모리 Map·고정 JSON·정상 Case 한 건만으로 완료 처리
- 교육용 고객 업무를 `cpf-core`, `cpf-starters/common`, Starter, Runtime 제품 내부에 삽입
- Module-local DB Vendor Fallback SQL 또는 Vendor별 의미가 다른 Schema
- Timeout이나 응답 유실 뒤 실제 결과 확인 없이 무조건 재호출
- 비밀번호·Token·개인정보 원문을 Source·Log·Fixture·문서에 기록
- 매뉴얼 설명과 다른 임의 Package·API·상태·메뉴를 새로 생성

---

# 66. EDU-DEV-32 — 개인정보 암호화·Tokenization·Key Rotation

- 우선순위: **P0**
- 연결 매뉴얼: `01_개발자매뉴얼.md`
- Owner: `cpf-education`
- 구현 상태 목표: Source·SQL·API·Config·Test·운영 확인·매뉴얼 연결이 모두 완료

## 필요 근거

- 고객 매뉴얼에서 `개인정보 암호화·Tokenization·Key Rotation`을 설명하려면 정상 호출 예시만이 아니라 상태 변화와 장애 후 정상화까지 재현 가능한 실행 예제가 필요하다.
- 이 요구사항이 없으면 고객은 기능 가능 여부를 알더라도 실제 업무 적용 순서, 실패 판정, 재처리 금지 조건을 확인할 수 없다.
- 내부 구현 구조를 보여 주기 위한 예제가 아니라 고객 업무를 CPF로 만드는 방법과 운영 결과를 증명하는 예제로 구현한다.

## 고객 업무 시나리오

주민번호와 계좌번호를 저장 시 암호화하고 조회는 권한에 따라 Masking하며 키를 교체한다.

## 구현 위치와 책임 경계

- 업무 Source: `com.cpf.education.edu.dev32` 또는 같은 의미의 기존 표준 Slice
- Test: Owner Module의 기존 Test Root 아래 `EDU-DEV-32` 식별 가능한 Class·Fixture
- SQL: 중앙 DB Vendor Pack의 기존 Install·Upgrade·Rollback·Verify·Seed 구조
- Config: 기존 Prefix·Profile·Secret 참조 방식을 유지
- 고객 업무 상태 변경과 원장 데이터는 고객 업무 Owner가 소유한다. ADM·BZA·Gateway·운영 Script가 업무 Table을 직접 수정하지 않는다.
- 공통 기능이 필요하면 기존 Public API·SPI를 우선 사용하고, 진입점이 없을 때만 실제 Consumer를 근거로 Owner Module에 확장한다.

## 입력·계약

- `subjectId`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `plaintext via secure input`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `keyVersion`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `reason`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- 해당 기능에 필요한 Operation ID·업무 ID·Correlation ID·요청 Hash·사용자·권한 문맥을 저장·Log·Trace·Audit에서 연결한다.
- 요청·응답·Job Parameter·이벤트·파일 Layout 중 해당되는 계약을 OpenAPI 또는 Machine-readable Schema로 제공한다.

## 상태와 저장 근거

- 필수 상태 흐름: `ENCRYPTED(v1) → ROTATING → ENCRYPTED(v2); TOKEN_ACTIVE/REVOKED`
- 상태마다 진입 조건, 허용 조치, 재시도 가능 여부, 최종 여부, 저장 위치와 Version 규칙을 정의한다.
- 결과 미확정·부분 성공이 가능한 경우 대상별 Attempt와 상대 상태 조회 근거를 별도 저장한다.

## 정상 처리 순서

1. 입력·권한·데이터 범위·기준 Version과 선행 상태를 검증한다.
2. 상태 변경·비동기 실행인 경우 Operation과 업무 식별자를 생성하거나 기존 멱등 요청을 조회한다. 조회 전용 기능은 조회 Context와 Cursor만 사용한다.
3. 상태 변경 기능은 고객 업무 Owner의 Application Use Case에서 Transaction 경계와 상태 전이를 수행하고, 조회 전용 기능은 일관된 Snapshot·정렬·범위 조건을 적용한다.
4. 외부 시스템·Kafka·파일·Batch·Gateway가 필요한 경우 Adapter 경계에서 Timeout·Retry·보안 정책을 적용한다.
5. 업무 결과와 Attempt·Audit·Log·Metric·Trace를 같은 식별자로 연결한다.
6. 고객 API 또는 운영 화면에서 최종 상태·건수·금액·Version·오류 0건을 확인한다.

## 예외·장애·경계 사례

- 키 누락
- 구키 복호화
- 부분 Rotation
- Token 충돌
- Log 유출
- 백업 복원 후 키 불일치
- 상태 변경이 있는 경우 DB Commit 전 실패와 Commit 후 응답 유실을 구분한다.
- 같은 요청의 중복·동시 실행·순서 역전·늦은 응답을 포함한다.
- Retry 가능한 오류와 즉시 중단해야 하는 업무 오류를 구분한다.
- 대량·다중 대상 기능에서 부분 성공이 발생하면 성공 대상을 다시 실행하지 않고 실패 대상만 Reprocess·Reconcile한다.
- 복구·보상이 필요한 기능은 복구 실패와 보상 실패도 별도 상태·Attempt·Audit로 남긴다.

## 필수 Test

- 암호화 at rest
- rotation resume
- masking
- audit
- secret scan
- 입력 Validation·권한·Data Scope·Masking Negative Test
- 해당 기능에 적용되는 중복·동시성·Timeout·응답 유실·부분 실패 Test
- 해당 기능의 Failure Injection 후 필요한 Retry·Restart·Reprocess·Reconcile·Rollback Test
- Oracle·PostgreSQL·MariaDB 또는 해당 Infra의 의미 동일성 Test
- Log·Metric·Trace·Audit에 Secret·개인정보 원문이 없는지 검사

## 운영·화면 확인

- ADM Secret·Audit에서 키 Version과 Rotation 결과만 확인
- 화면에는 해당 메뉴에 실제로 존재하는 조회 조건, 기본값, Column, 상세 Field, 상태, Button 활성 조건, 사유, 승인, 예상 Version, 대상별 결과만 Source와 일치하게 표시한다. 조회 전용 메뉴에 Command 항목을 만들지 않는다.
- 운영자가 정상화 완료를 판단할 수 있는 건수·금액·Version·Checksum·최종 상태·마지막 오류를 제공한다.
- 변경·외부연계 기능에서 응답 유실이 가능한 경우에만 요청을 다시 보내기 전에 Operation·거래·상대 상태를 조회하는 절차를 Browser 또는 API Test로 검증한다.

## 고객 매뉴얼·EDU 연결

- `01_개발자매뉴얼.md`의 해당 기능 장에 `EDU-DEV-32`를 연결한다.
- 선행 환경, 실행 명령, 입력값, 정상 출력, 오류 재현 명령, 복구 명령, ADM/BZA/Gateway 확인 위치를 그대로 따라 할 수 있게 작성한다.
- 교육 코드에서 고객 업무로 바꿀 Package·Table·API·Topic·파일명과 CPF 관리 영역을 구분한 전환표를 제공한다.

## 완료 조건

- `EDU-DEV-32`가 교육 Catalog·Source·Test·고객 매뉴얼에 동일 ID로 존재한다.
- 정상·Validation·권한·중복·동시성·Timeout·응답 유실·부분 실패·복구 Test가 통과한다.
- 요청·응답·상태·DB·이벤트·파일·운영 화면이 양방향으로 일치한다.
- 운영자가 다른 사람의 설명 없이 장애를 재현하고 정상화 판정을 할 수 있다.
- 실행하지 않은 Test는 성공으로 기록하지 않고, 실패·제약이 남으면 전체 완료로 보고하지 않는다.

## 금지 사항

- 메모리 Map·고정 JSON·정상 Case 한 건만으로 완료 처리
- 교육용 고객 업무를 `cpf-core`, `cpf-starters/common`, Starter, Runtime 제품 내부에 삽입
- Module-local DB Vendor Fallback SQL 또는 Vendor별 의미가 다른 Schema
- Timeout이나 응답 유실 뒤 실제 결과 확인 없이 무조건 재호출
- 비밀번호·Token·개인정보 원문을 Source·Log·Fixture·문서에 기록
- 매뉴얼 설명과 다른 임의 Package·API·상태·메뉴를 새로 생성

---

# 67. EDU-DEV-33 — 인증 Token 만료·갱신·폐기·세션 강제 종료

- 우선순위: **P0**
- 연결 매뉴얼: `01_개발자매뉴얼.md`
- Owner: `cpf-education`
- 구현 상태 목표: Source·SQL·API·Config·Test·운영 확인·매뉴얼 연결이 모두 완료

## 필요 근거

- 고객 매뉴얼에서 `인증 Token 만료·갱신·폐기·세션 강제 종료`을 설명하려면 정상 호출 예시만이 아니라 상태 변화와 장애 후 정상화까지 재현 가능한 실행 예제가 필요하다.
- 이 요구사항이 없으면 고객은 기능 가능 여부를 알더라도 실제 업무 적용 순서, 실패 판정, 재처리 금지 조건을 확인할 수 없다.
- 내부 구현 구조를 보여 주기 위한 예제가 아니라 고객 업무를 CPF로 만드는 방법과 운영 결과를 증명하는 예제로 구현한다.

## 고객 업무 시나리오

접근 Token 만료 시 갱신하고 탈취 의심 시 사용자 모든 세션을 폐기한다.

## 구현 위치와 책임 경계

- 업무 Source: `com.cpf.education.edu.dev33` 또는 같은 의미의 기존 표준 Slice
- Test: Owner Module의 기존 Test Root 아래 `EDU-DEV-33` 식별 가능한 Class·Fixture
- SQL: 중앙 DB Vendor Pack의 기존 Install·Upgrade·Rollback·Verify·Seed 구조
- Config: 기존 Prefix·Profile·Secret 참조 방식을 유지
- 고객 업무 상태 변경과 원장 데이터는 고객 업무 Owner가 소유한다. ADM·BZA·Gateway·운영 Script가 업무 Table을 직접 수정하지 않는다.
- 공통 기능이 필요하면 기존 Public API·SPI를 우선 사용하고, 진입점이 없을 때만 실제 Consumer를 근거로 Owner Module에 확장한다.

## 입력·계약

- `accessToken`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `refreshToken`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `sessionId`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `revokeReason`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- 해당 기능에 필요한 Operation ID·업무 ID·Correlation ID·요청 Hash·사용자·권한 문맥을 저장·Log·Trace·Audit에서 연결한다.
- 요청·응답·Job Parameter·이벤트·파일 Layout 중 해당되는 계약을 OpenAPI 또는 Machine-readable Schema로 제공한다.

## 상태와 저장 근거

- 필수 상태 흐름: `ACTIVE → REFRESHED/EXPIRED/REVOKED; SESSION_TERMINATED`
- 상태마다 진입 조건, 허용 조치, 재시도 가능 여부, 최종 여부, 저장 위치와 Version 규칙을 정의한다.
- 결과 미확정·부분 성공이 가능한 경우 대상별 Attempt와 상대 상태 조회 근거를 별도 저장한다.

## 정상 처리 순서

1. 입력·권한·데이터 범위·기준 Version과 선행 상태를 검증한다.
2. 상태 변경·비동기 실행인 경우 Operation과 업무 식별자를 생성하거나 기존 멱등 요청을 조회한다. 조회 전용 기능은 조회 Context와 Cursor만 사용한다.
3. 상태 변경 기능은 고객 업무 Owner의 Application Use Case에서 Transaction 경계와 상태 전이를 수행하고, 조회 전용 기능은 일관된 Snapshot·정렬·범위 조건을 적용한다.
4. 외부 시스템·Kafka·파일·Batch·Gateway가 필요한 경우 Adapter 경계에서 Timeout·Retry·보안 정책을 적용한다.
5. 업무 결과와 Attempt·Audit·Log·Metric·Trace를 같은 식별자로 연결한다.
6. 고객 API 또는 운영 화면에서 최종 상태·건수·금액·Version·오류 0건을 확인한다.

## 예외·장애·경계 사례

- Refresh 재사용
- 동시 갱신
- 계정 잠금
- Clock skew
- 폐기 후 API 호출
- 상태 변경이 있는 경우 DB Commit 전 실패와 Commit 후 응답 유실을 구분한다.
- 같은 요청의 중복·동시 실행·순서 역전·늦은 응답을 포함한다.
- Retry 가능한 오류와 즉시 중단해야 하는 업무 오류를 구분한다.
- 대량·다중 대상 기능에서 부분 성공이 발생하면 성공 대상을 다시 실행하지 않고 실패 대상만 Reprocess·Reconcile한다.
- 복구·보상이 필요한 기능은 복구 실패와 보상 실패도 별도 상태·Attempt·Audit로 남긴다.

## 필수 Test

- Token rotation
- replay detection
- session store
- audit
- browser test
- 입력 Validation·권한·Data Scope·Masking Negative Test
- 해당 기능에 적용되는 중복·동시성·Timeout·응답 유실·부분 실패 Test
- 해당 기능의 Failure Injection 후 필요한 Retry·Restart·Reprocess·Reconcile·Rollback Test
- Oracle·PostgreSQL·MariaDB 또는 해당 Infra의 의미 동일성 Test
- Log·Metric·Trace·Audit에 Secret·개인정보 원문이 없는지 검사

## 운영·화면 확인

- BZA 세션·ADM 보안에서 사용자별 세션과 강제 종료 확인
- 화면에는 해당 메뉴에 실제로 존재하는 조회 조건, 기본값, Column, 상세 Field, 상태, Button 활성 조건, 사유, 승인, 예상 Version, 대상별 결과만 Source와 일치하게 표시한다. 조회 전용 메뉴에 Command 항목을 만들지 않는다.
- 운영자가 정상화 완료를 판단할 수 있는 건수·금액·Version·Checksum·최종 상태·마지막 오류를 제공한다.
- 변경·외부연계 기능에서 응답 유실이 가능한 경우에만 요청을 다시 보내기 전에 Operation·거래·상대 상태를 조회하는 절차를 Browser 또는 API Test로 검증한다.

## 고객 매뉴얼·EDU 연결

- `01_개발자매뉴얼.md`의 해당 기능 장에 `EDU-DEV-33`를 연결한다.
- 선행 환경, 실행 명령, 입력값, 정상 출력, 오류 재현 명령, 복구 명령, ADM/BZA/Gateway 확인 위치를 그대로 따라 할 수 있게 작성한다.
- 교육 코드에서 고객 업무로 바꿀 Package·Table·API·Topic·파일명과 CPF 관리 영역을 구분한 전환표를 제공한다.

## 완료 조건

- `EDU-DEV-33`가 교육 Catalog·Source·Test·고객 매뉴얼에 동일 ID로 존재한다.
- 정상·Validation·권한·중복·동시성·Timeout·응답 유실·부분 실패·복구 Test가 통과한다.
- 요청·응답·상태·DB·이벤트·파일·운영 화면이 양방향으로 일치한다.
- 운영자가 다른 사람의 설명 없이 장애를 재현하고 정상화 판정을 할 수 있다.
- 실행하지 않은 Test는 성공으로 기록하지 않고, 실패·제약이 남으면 전체 완료로 보고하지 않는다.

## 금지 사항

- 메모리 Map·고정 JSON·정상 Case 한 건만으로 완료 처리
- 교육용 고객 업무를 `cpf-core`, `cpf-starters/common`, Starter, Runtime 제품 내부에 삽입
- Module-local DB Vendor Fallback SQL 또는 Vendor별 의미가 다른 Schema
- Timeout이나 응답 유실 뒤 실제 결과 확인 없이 무조건 재호출
- 비밀번호·Token·개인정보 원문을 Source·Log·Fixture·문서에 기록
- 매뉴얼 설명과 다른 임의 Package·API·상태·메뉴를 새로 생성

---

# 68. EDU-DEV-34 — API 사용량 제한·고객별 Quota·초과 처리

- 우선순위: **P0**
- 연결 매뉴얼: `01_개발자매뉴얼.md`
- Owner: `cpf-education`
- 구현 상태 목표: Source·SQL·API·Config·Test·운영 확인·매뉴얼 연결이 모두 완료

## 필요 근거

- 고객 매뉴얼에서 `API 사용량 제한·고객별 Quota·초과 처리`을 설명하려면 정상 호출 예시만이 아니라 상태 변화와 장애 후 정상화까지 재현 가능한 실행 예제가 필요하다.
- 이 요구사항이 없으면 고객은 기능 가능 여부를 알더라도 실제 업무 적용 순서, 실패 판정, 재처리 금지 조건을 확인할 수 없다.
- 내부 구현 구조를 보여 주기 위한 예제가 아니라 고객 업무를 CPF로 만드는 방법과 운영 결과를 증명하는 예제로 구현한다.

## 고객 업무 시나리오

고객사별 초당·일일 호출량을 제한하고 초과 요청에 재시도 가능 시각을 제공한다.

## 구현 위치와 책임 경계

- 업무 Source: `com.cpf.education.edu.dev34` 또는 같은 의미의 기존 표준 Slice
- Test: Owner Module의 기존 Test Root 아래 `EDU-DEV-34` 식별 가능한 Class·Fixture
- SQL: 중앙 DB Vendor Pack의 기존 Install·Upgrade·Rollback·Verify·Seed 구조
- Config: 기존 Prefix·Profile·Secret 참조 방식을 유지
- 고객 업무 상태 변경과 원장 데이터는 고객 업무 Owner가 소유한다. ADM·BZA·Gateway·운영 Script가 업무 Table을 직접 수정하지 않는다.
- 공통 기능이 필요하면 기존 Public API·SPI를 우선 사용하고, 진입점이 없을 때만 실제 Consumer를 근거로 Owner Module에 확장한다.

## 입력·계약

- `clientId`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `apiId`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `ratePlan`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `requestCost`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- 해당 기능에 필요한 Operation ID·업무 ID·Correlation ID·요청 Hash·사용자·권한 문맥을 저장·Log·Trace·Audit에서 연결한다.
- 요청·응답·Job Parameter·이벤트·파일 Layout 중 해당되는 계약을 OpenAPI 또는 Machine-readable Schema로 제공한다.

## 상태와 저장 근거

- 필수 상태 흐름: `ALLOWED 또는 THROTTLED; QUOTA_ACTIVE/EXHAUSTED/RESET`
- 상태마다 진입 조건, 허용 조치, 재시도 가능 여부, 최종 여부, 저장 위치와 Version 규칙을 정의한다.
- 결과 미확정·부분 성공이 가능한 경우 대상별 Attempt와 상대 상태 조회 근거를 별도 저장한다.

## 정상 처리 순서

1. 입력·권한·데이터 범위·기준 Version과 선행 상태를 검증한다.
2. 상태 변경·비동기 실행인 경우 Operation과 업무 식별자를 생성하거나 기존 멱등 요청을 조회한다. 조회 전용 기능은 조회 Context와 Cursor만 사용한다.
3. 상태 변경 기능은 고객 업무 Owner의 Application Use Case에서 Transaction 경계와 상태 전이를 수행하고, 조회 전용 기능은 일관된 Snapshot·정렬·범위 조건을 적용한다.
4. 외부 시스템·Kafka·파일·Batch·Gateway가 필요한 경우 Adapter 경계에서 Timeout·Retry·보안 정책을 적용한다.
5. 업무 결과와 Attempt·Audit·Log·Metric·Trace를 같은 식별자로 연결한다.
6. 고객 API 또는 운영 화면에서 최종 상태·건수·금액·Version·오류 0건을 확인한다.

## 예외·장애·경계 사례

- Burst
- 다중 인스턴스
- Clock boundary
- Redis 장애
- 우회 Header
- 상태 변경이 있는 경우 DB Commit 전 실패와 Commit 후 응답 유실을 구분한다.
- 같은 요청의 중복·동시 실행·순서 역전·늦은 응답을 포함한다.
- Retry 가능한 오류와 즉시 중단해야 하는 업무 오류를 구분한다.
- 대량·다중 대상 기능에서 부분 성공이 발생하면 성공 대상을 다시 실행하지 않고 실패 대상만 Reprocess·Reconcile한다.
- 복구·보상이 필요한 기능은 복구 실패와 보상 실패도 별도 상태·Attempt·Audit로 남긴다.

## 필수 Test

- Token bucket
- quota atomicity
- Retry-After
- fallback policy
- metrics
- 입력 Validation·권한·Data Scope·Masking Negative Test
- 해당 기능에 적용되는 중복·동시성·Timeout·응답 유실·부분 실패 Test
- 해당 기능의 Failure Injection 후 필요한 Retry·Restart·Reprocess·Reconcile·Rollback Test
- Oracle·PostgreSQL·MariaDB 또는 해당 Infra의 의미 동일성 Test
- Log·Metric·Trace·Audit에 Secret·개인정보 원문이 없는지 검사

## 운영·화면 확인

- Gateway/ADM 용량 화면에서 client·API별 제한과 초과 건수 확인
- 화면에는 해당 메뉴에 실제로 존재하는 조회 조건, 기본값, Column, 상세 Field, 상태, Button 활성 조건, 사유, 승인, 예상 Version, 대상별 결과만 Source와 일치하게 표시한다. 조회 전용 메뉴에 Command 항목을 만들지 않는다.
- 운영자가 정상화 완료를 판단할 수 있는 건수·금액·Version·Checksum·최종 상태·마지막 오류를 제공한다.
- 변경·외부연계 기능에서 응답 유실이 가능한 경우에만 요청을 다시 보내기 전에 Operation·거래·상대 상태를 조회하는 절차를 Browser 또는 API Test로 검증한다.

## 고객 매뉴얼·EDU 연결

- `01_개발자매뉴얼.md`의 해당 기능 장에 `EDU-DEV-34`를 연결한다.
- 선행 환경, 실행 명령, 입력값, 정상 출력, 오류 재현 명령, 복구 명령, ADM/BZA/Gateway 확인 위치를 그대로 따라 할 수 있게 작성한다.
- 교육 코드에서 고객 업무로 바꿀 Package·Table·API·Topic·파일명과 CPF 관리 영역을 구분한 전환표를 제공한다.

## 완료 조건

- `EDU-DEV-34`가 교육 Catalog·Source·Test·고객 매뉴얼에 동일 ID로 존재한다.
- 정상·Validation·권한·중복·동시성·Timeout·응답 유실·부분 실패·복구 Test가 통과한다.
- 요청·응답·상태·DB·이벤트·파일·운영 화면이 양방향으로 일치한다.
- 운영자가 다른 사람의 설명 없이 장애를 재현하고 정상화 판정을 할 수 있다.
- 실행하지 않은 Test는 성공으로 기록하지 않고, 실패·제약이 남으면 전체 완료로 보고하지 않는다.

## 금지 사항

- 메모리 Map·고정 JSON·정상 Case 한 건만으로 완료 처리
- 교육용 고객 업무를 `cpf-core`, `cpf-starters/common`, Starter, Runtime 제품 내부에 삽입
- Module-local DB Vendor Fallback SQL 또는 Vendor별 의미가 다른 Schema
- Timeout이나 응답 유실 뒤 실제 결과 확인 없이 무조건 재호출
- 비밀번호·Token·개인정보 원문을 Source·Log·Fixture·문서에 기록
- 매뉴얼 설명과 다른 임의 Package·API·상태·메뉴를 새로 생성

---

# 69. EDU-DEV-35 — 기능 전환 Canary·Kill Switch·사용자 Segment

- 우선순위: **P0**
- 연결 매뉴얼: `01_개발자매뉴얼.md`
- Owner: `cpf-education`
- 구현 상태 목표: Source·SQL·API·Config·Test·운영 확인·매뉴얼 연결이 모두 완료

## 필요 근거

- 고객 매뉴얼에서 `기능 전환 Canary·Kill Switch·사용자 Segment`을 설명하려면 정상 호출 예시만이 아니라 상태 변화와 장애 후 정상화까지 재현 가능한 실행 예제가 필요하다.
- 이 요구사항이 없으면 고객은 기능 가능 여부를 알더라도 실제 업무 적용 순서, 실패 판정, 재처리 금지 조건을 확인할 수 없다.
- 내부 구현 구조를 보여 주기 위한 예제가 아니라 고객 업무를 CPF로 만드는 방법과 운영 결과를 증명하는 예제로 구현한다.

## 고객 업무 시나리오

신기능을 내부 사용자 5%에만 켜고 오류 증가 시 즉시 끈다.

## 구현 위치와 책임 경계

- 업무 Source: `com.cpf.education.edu.dev35` 또는 같은 의미의 기존 표준 Slice
- Test: Owner Module의 기존 Test Root 아래 `EDU-DEV-35` 식별 가능한 Class·Fixture
- SQL: 중앙 DB Vendor Pack의 기존 Install·Upgrade·Rollback·Verify·Seed 구조
- Config: 기존 Prefix·Profile·Secret 참조 방식을 유지
- 고객 업무 상태 변경과 원장 데이터는 고객 업무 Owner가 소유한다. ADM·BZA·Gateway·운영 Script가 업무 Table을 직접 수정하지 않는다.
- 공통 기능이 필요하면 기존 Public API·SPI를 우선 사용하고, 진입점이 없을 때만 실제 Consumer를 근거로 Owner Module에 확장한다.

## 입력·계약

- `featureKey`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `environment`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `percentage`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `segment`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `expectedVersion`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- 해당 기능에 필요한 Operation ID·업무 ID·Correlation ID·요청 Hash·사용자·권한 문맥을 저장·Log·Trace·Audit에서 연결한다.
- 요청·응답·Job Parameter·이벤트·파일 Layout 중 해당되는 계약을 OpenAPI 또는 Machine-readable Schema로 제공한다.

## 상태와 저장 근거

- 필수 상태 흐름: `DRAFT → ACTIVE_CANARY → ACTIVE → DISABLED/ROLLED_BACK`
- 상태마다 진입 조건, 허용 조치, 재시도 가능 여부, 최종 여부, 저장 위치와 Version 규칙을 정의한다.
- 결과 미확정·부분 성공이 가능한 경우 대상별 Attempt와 상대 상태 조회 근거를 별도 저장한다.

## 정상 처리 순서

1. 입력·권한·데이터 범위·기준 Version과 선행 상태를 검증한다.
2. 상태 변경·비동기 실행인 경우 Operation과 업무 식별자를 생성하거나 기존 멱등 요청을 조회한다. 조회 전용 기능은 조회 Context와 Cursor만 사용한다.
3. 상태 변경 기능은 고객 업무 Owner의 Application Use Case에서 Transaction 경계와 상태 전이를 수행하고, 조회 전용 기능은 일관된 Snapshot·정렬·범위 조건을 적용한다.
4. 외부 시스템·Kafka·파일·Batch·Gateway가 필요한 경우 Adapter 경계에서 Timeout·Retry·보안 정책을 적용한다.
5. 업무 결과와 Attempt·Audit·Log·Metric·Trace를 같은 식별자로 연결한다.
6. 고객 API 또는 운영 화면에서 최종 상태·건수·금액·Version·오류 0건을 확인한다.

## 예외·장애·경계 사례

- 동일 사용자 일관성
- Segment 중복
- Config 부분 적용
- 캐시 지연
- Kill switch
- 상태 변경이 있는 경우 DB Commit 전 실패와 Commit 후 응답 유실을 구분한다.
- 같은 요청의 중복·동시 실행·순서 역전·늦은 응답을 포함한다.
- Retry 가능한 오류와 즉시 중단해야 하는 업무 오류를 구분한다.
- 대량·다중 대상 기능에서 부분 성공이 발생하면 성공 대상을 다시 실행하지 않고 실패 대상만 Reprocess·Reconcile한다.
- 복구·보상이 필요한 기능은 복구 실패와 보상 실패도 별도 상태·Attempt·Audit로 남긴다.

## 필수 Test

- 결정 일관성
- Version conflict
- metrics
- rollout/rollback browser
- 입력 Validation·권한·Data Scope·Masking Negative Test
- 해당 기능에 적용되는 중복·동시성·Timeout·응답 유실·부분 실패 Test
- 해당 기능의 Failure Injection 후 필요한 Retry·Restart·Reprocess·Reconcile·Rollback Test
- Oracle·PostgreSQL·MariaDB 또는 해당 Infra의 의미 동일성 Test
- Log·Metric·Trace·Audit에 Secret·개인정보 원문이 없는지 검사

## 운영·화면 확인

- ADM 설정·신뢰성에서 적용 인스턴스·오류율·Rollback 확인
- 화면에는 해당 메뉴에 실제로 존재하는 조회 조건, 기본값, Column, 상세 Field, 상태, Button 활성 조건, 사유, 승인, 예상 Version, 대상별 결과만 Source와 일치하게 표시한다. 조회 전용 메뉴에 Command 항목을 만들지 않는다.
- 운영자가 정상화 완료를 판단할 수 있는 건수·금액·Version·Checksum·최종 상태·마지막 오류를 제공한다.
- 변경·외부연계 기능에서 응답 유실이 가능한 경우에만 요청을 다시 보내기 전에 Operation·거래·상대 상태를 조회하는 절차를 Browser 또는 API Test로 검증한다.

## 고객 매뉴얼·EDU 연결

- `01_개발자매뉴얼.md`의 해당 기능 장에 `EDU-DEV-35`를 연결한다.
- 선행 환경, 실행 명령, 입력값, 정상 출력, 오류 재현 명령, 복구 명령, ADM/BZA/Gateway 확인 위치를 그대로 따라 할 수 있게 작성한다.
- 교육 코드에서 고객 업무로 바꿀 Package·Table·API·Topic·파일명과 CPF 관리 영역을 구분한 전환표를 제공한다.

## 완료 조건

- `EDU-DEV-35`가 교육 Catalog·Source·Test·고객 매뉴얼에 동일 ID로 존재한다.
- 정상·Validation·권한·중복·동시성·Timeout·응답 유실·부분 실패·복구 Test가 통과한다.
- 요청·응답·상태·DB·이벤트·파일·운영 화면이 양방향으로 일치한다.
- 운영자가 다른 사람의 설명 없이 장애를 재현하고 정상화 판정을 할 수 있다.
- 실행하지 않은 Test는 성공으로 기록하지 않고, 실패·제약이 남으면 전체 완료로 보고하지 않는다.

## 금지 사항

- 메모리 Map·고정 JSON·정상 Case 한 건만으로 완료 처리
- 교육용 고객 업무를 `cpf-core`, `cpf-starters/common`, Starter, Runtime 제품 내부에 삽입
- Module-local DB Vendor Fallback SQL 또는 Vendor별 의미가 다른 Schema
- Timeout이나 응답 유실 뒤 실제 결과 확인 없이 무조건 재호출
- 비밀번호·Token·개인정보 원문을 Source·Log·Fixture·문서에 기록
- 매뉴얼 설명과 다른 임의 Package·API·상태·메뉴를 새로 생성

---

# 70. EDU-DEV-36 — Cache Stampede·Negative Cache·원본 정합성

- 우선순위: **P0**
- 연결 매뉴얼: `01_개발자매뉴얼.md`
- Owner: `cpf-education`
- 구현 상태 목표: Source·SQL·API·Config·Test·운영 확인·매뉴얼 연결이 모두 완료

## 필요 근거

- 고객 매뉴얼에서 `Cache Stampede·Negative Cache·원본 정합성`을 설명하려면 정상 호출 예시만이 아니라 상태 변화와 장애 후 정상화까지 재현 가능한 실행 예제가 필요하다.
- 이 요구사항이 없으면 고객은 기능 가능 여부를 알더라도 실제 업무 적용 순서, 실패 판정, 재처리 금지 조건을 확인할 수 없다.
- 내부 구현 구조를 보여 주기 위한 예제가 아니라 고객 업무를 CPF로 만드는 방법과 운영 결과를 증명하는 예제로 구현한다.

## 고객 업무 시나리오

인기 상품 조회가 동시에 만료돼도 DB를 폭주시하지 않고 미존재 결과도 짧게 Cache한다.

## 구현 위치와 책임 경계

- 업무 Source: `com.cpf.education.edu.dev36` 또는 같은 의미의 기존 표준 Slice
- Test: Owner Module의 기존 Test Root 아래 `EDU-DEV-36` 식별 가능한 Class·Fixture
- SQL: 중앙 DB Vendor Pack의 기존 Install·Upgrade·Rollback·Verify·Seed 구조
- Config: 기존 Prefix·Profile·Secret 참조 방식을 유지
- 고객 업무 상태 변경과 원장 데이터는 고객 업무 Owner가 소유한다. ADM·BZA·Gateway·운영 Script가 업무 Table을 직접 수정하지 않는다.
- 공통 기능이 필요하면 기존 Public API·SPI를 우선 사용하고, 진입점이 없을 때만 실제 Consumer를 근거로 Owner Module에 확장한다.

## 입력·계약

- `cacheKey`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `ttl`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `negativeTtl`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `loaderTimeout`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- 해당 기능에 필요한 Operation ID·업무 ID·Correlation ID·요청 Hash·사용자·권한 문맥을 저장·Log·Trace·Audit에서 연결한다.
- 요청·응답·Job Parameter·이벤트·파일 Layout 중 해당되는 계약을 OpenAPI 또는 Machine-readable Schema로 제공한다.

## 상태와 저장 근거

- 필수 상태 흐름: `HIT/MISS → REFRESHING → REFRESHED/STALE_SERVED`
- 상태마다 진입 조건, 허용 조치, 재시도 가능 여부, 최종 여부, 저장 위치와 Version 규칙을 정의한다.
- 결과 미확정·부분 성공이 가능한 경우 대상별 Attempt와 상대 상태 조회 근거를 별도 저장한다.

## 정상 처리 순서

1. 입력·권한·데이터 범위·기준 Version과 선행 상태를 검증한다.
2. 상태 변경·비동기 실행인 경우 Operation과 업무 식별자를 생성하거나 기존 멱등 요청을 조회한다. 조회 전용 기능은 조회 Context와 Cursor만 사용한다.
3. 상태 변경 기능은 고객 업무 Owner의 Application Use Case에서 Transaction 경계와 상태 전이를 수행하고, 조회 전용 기능은 일관된 Snapshot·정렬·범위 조건을 적용한다.
4. 외부 시스템·Kafka·파일·Batch·Gateway가 필요한 경우 Adapter 경계에서 Timeout·Retry·보안 정책을 적용한다.
5. 업무 결과와 Attempt·Audit·Log·Metric·Trace를 같은 식별자로 연결한다.
6. 고객 API 또는 운영 화면에서 최종 상태·건수·금액·Version·오류 0건을 확인한다.

## 예외·장애·경계 사례

- 동시 1
- 000요청
- Redis 장애
- Loader Timeout
- 갱신 중 삭제
- stale 허용 한계
- 상태 변경이 있는 경우 DB Commit 전 실패와 Commit 후 응답 유실을 구분한다.
- 같은 요청의 중복·동시 실행·순서 역전·늦은 응답을 포함한다.
- Retry 가능한 오류와 즉시 중단해야 하는 업무 오류를 구분한다.
- 대량·다중 대상 기능에서 부분 성공이 발생하면 성공 대상을 다시 실행하지 않고 실패 대상만 Reprocess·Reconcile한다.
- 복구·보상이 필요한 기능은 복구 실패와 보상 실패도 별도 상태·Attempt·Audit로 남긴다.

## 필수 Test

- Single-flight
- TTL jitter
- negative cache
- invalidation event
- metrics
- 입력 Validation·권한·Data Scope·Masking Negative Test
- 해당 기능에 적용되는 중복·동시성·Timeout·응답 유실·부분 실패 Test
- 해당 기능의 Failure Injection 후 필요한 Retry·Restart·Reprocess·Reconcile·Rollback Test
- Oracle·PostgreSQL·MariaDB 또는 해당 Infra의 의미 동일성 Test
- Log·Metric·Trace·Audit에 Secret·개인정보 원문이 없는지 검사

## 운영·화면 확인

- ADM Cache 화면에서 hit·miss·refresh·fallback 확인
- 화면에는 해당 메뉴에 실제로 존재하는 조회 조건, 기본값, Column, 상세 Field, 상태, Button 활성 조건, 사유, 승인, 예상 Version, 대상별 결과만 Source와 일치하게 표시한다. 조회 전용 메뉴에 Command 항목을 만들지 않는다.
- 운영자가 정상화 완료를 판단할 수 있는 건수·금액·Version·Checksum·최종 상태·마지막 오류를 제공한다.
- 변경·외부연계 기능에서 응답 유실이 가능한 경우에만 요청을 다시 보내기 전에 Operation·거래·상대 상태를 조회하는 절차를 Browser 또는 API Test로 검증한다.

## 고객 매뉴얼·EDU 연결

- `01_개발자매뉴얼.md`의 해당 기능 장에 `EDU-DEV-36`를 연결한다.
- 선행 환경, 실행 명령, 입력값, 정상 출력, 오류 재현 명령, 복구 명령, ADM/BZA/Gateway 확인 위치를 그대로 따라 할 수 있게 작성한다.
- 교육 코드에서 고객 업무로 바꿀 Package·Table·API·Topic·파일명과 CPF 관리 영역을 구분한 전환표를 제공한다.

## 완료 조건

- `EDU-DEV-36`가 교육 Catalog·Source·Test·고객 매뉴얼에 동일 ID로 존재한다.
- 정상·Validation·권한·중복·동시성·Timeout·응답 유실·부분 실패·복구 Test가 통과한다.
- 요청·응답·상태·DB·이벤트·파일·운영 화면이 양방향으로 일치한다.
- 운영자가 다른 사람의 설명 없이 장애를 재현하고 정상화 판정을 할 수 있다.
- 실행하지 않은 Test는 성공으로 기록하지 않고, 실패·제약이 남으면 전체 완료로 보고하지 않는다.

## 금지 사항

- 메모리 Map·고정 JSON·정상 Case 한 건만으로 완료 처리
- 교육용 고객 업무를 `cpf-core`, `cpf-starters/common`, Starter, Runtime 제품 내부에 삽입
- Module-local DB Vendor Fallback SQL 또는 Vendor별 의미가 다른 Schema
- Timeout이나 응답 유실 뒤 실제 결과 확인 없이 무조건 재호출
- 비밀번호·Token·개인정보 원문을 Source·Log·Fixture·문서에 기록
- 매뉴얼 설명과 다른 임의 Package·API·상태·메뉴를 새로 생성

---

# 71. EDU-DEV-37 — 온라인 분산 Lease·Fencing·소유권 상실

- 우선순위: **P0**
- 연결 매뉴얼: `01_개발자매뉴얼.md`
- Owner: `cpf-education`
- 구현 상태 목표: Source·SQL·API·Config·Test·운영 확인·매뉴얼 연결이 모두 완료

## 필요 근거

- 고객 매뉴얼에서 `온라인 분산 Lease·Fencing·소유권 상실`을 설명하려면 정상 호출 예시만이 아니라 상태 변화와 장애 후 정상화까지 재현 가능한 실행 예제가 필요하다.
- 이 요구사항이 없으면 고객은 기능 가능 여부를 알더라도 실제 업무 적용 순서, 실패 판정, 재처리 금지 조건을 확인할 수 없다.
- 내부 구현 구조를 보여 주기 위한 예제가 아니라 고객 업무를 CPF로 만드는 방법과 운영 결과를 증명하는 예제로 구현한다.

## 고객 업무 시나리오

한 고객에 대한 정산 작업을 한 인스턴스만 수행하고 Lease 상실 인스턴스의 늦은 쓰기를 차단한다.

## 구현 위치와 책임 경계

- 업무 Source: `com.cpf.education.edu.dev37` 또는 같은 의미의 기존 표준 Slice
- Test: Owner Module의 기존 Test Root 아래 `EDU-DEV-37` 식별 가능한 Class·Fixture
- SQL: 중앙 DB Vendor Pack의 기존 Install·Upgrade·Rollback·Verify·Seed 구조
- Config: 기존 Prefix·Profile·Secret 참조 방식을 유지
- 고객 업무 상태 변경과 원장 데이터는 고객 업무 Owner가 소유한다. ADM·BZA·Gateway·운영 Script가 업무 Table을 직접 수정하지 않는다.
- 공통 기능이 필요하면 기존 Public API·SPI를 우선 사용하고, 진입점이 없을 때만 실제 Consumer를 근거로 Owner Module에 확장한다.

## 입력·계약

- `resourceId`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `ownerId`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `leaseDuration`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `fencingToken`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- 해당 기능에 필요한 Operation ID·업무 ID·Correlation ID·요청 Hash·사용자·권한 문맥을 저장·Log·Trace·Audit에서 연결한다.
- 요청·응답·Job Parameter·이벤트·파일 Layout 중 해당되는 계약을 OpenAPI 또는 Machine-readable Schema로 제공한다.

## 상태와 저장 근거

- 필수 상태 흐름: `AVAILABLE → CLAIMED → RENEWING → RELEASED/EXPIRED`
- 상태마다 진입 조건, 허용 조치, 재시도 가능 여부, 최종 여부, 저장 위치와 Version 규칙을 정의한다.
- 결과 미확정·부분 성공이 가능한 경우 대상별 Attempt와 상대 상태 조회 근거를 별도 저장한다.

## 정상 처리 순서

1. 입력·권한·데이터 범위·기준 Version과 선행 상태를 검증한다.
2. 상태 변경·비동기 실행인 경우 Operation과 업무 식별자를 생성하거나 기존 멱등 요청을 조회한다. 조회 전용 기능은 조회 Context와 Cursor만 사용한다.
3. 상태 변경 기능은 고객 업무 Owner의 Application Use Case에서 Transaction 경계와 상태 전이를 수행하고, 조회 전용 기능은 일관된 Snapshot·정렬·범위 조건을 적용한다.
4. 외부 시스템·Kafka·파일·Batch·Gateway가 필요한 경우 Adapter 경계에서 Timeout·Retry·보안 정책을 적용한다.
5. 업무 결과와 Attempt·Audit·Log·Metric·Trace를 같은 식별자로 연결한다.
6. 고객 API 또는 운영 화면에서 최종 상태·건수·금액·Version·오류 0건을 확인한다.

## 예외·장애·경계 사례

- GC pause
- 네트워크 분리
- 이중 Claim
- 늦은 Commit
- Token 역전
- 상태 변경이 있는 경우 DB Commit 전 실패와 Commit 후 응답 유실을 구분한다.
- 같은 요청의 중복·동시 실행·순서 역전·늦은 응답을 포함한다.
- Retry 가능한 오류와 즉시 중단해야 하는 업무 오류를 구분한다.
- 대량·다중 대상 기능에서 부분 성공이 발생하면 성공 대상을 다시 실행하지 않고 실패 대상만 Reprocess·Reconcile한다.
- 복구·보상이 필요한 기능은 복구 실패와 보상 실패도 별도 상태·Attempt·Audit로 남긴다.

## 필수 Test

- DB/Redis lease atomicity
- fencing write
- takeover
- clock tolerance
- 입력 Validation·권한·Data Scope·Masking Negative Test
- 해당 기능에 적용되는 중복·동시성·Timeout·응답 유실·부분 실패 Test
- 해당 기능의 Failure Injection 후 필요한 Retry·Restart·Reprocess·Reconcile·Rollback Test
- Oracle·PostgreSQL·MariaDB 또는 해당 Infra의 의미 동일성 Test
- Log·Metric·Trace·Audit에 Secret·개인정보 원문이 없는지 검사

## 운영·화면 확인

- ADM Lease·Topology에서 Owner·만료·탈취·거부 확인
- 화면에는 해당 메뉴에 실제로 존재하는 조회 조건, 기본값, Column, 상세 Field, 상태, Button 활성 조건, 사유, 승인, 예상 Version, 대상별 결과만 Source와 일치하게 표시한다. 조회 전용 메뉴에 Command 항목을 만들지 않는다.
- 운영자가 정상화 완료를 판단할 수 있는 건수·금액·Version·Checksum·최종 상태·마지막 오류를 제공한다.
- 변경·외부연계 기능에서 응답 유실이 가능한 경우에만 요청을 다시 보내기 전에 Operation·거래·상대 상태를 조회하는 절차를 Browser 또는 API Test로 검증한다.

## 고객 매뉴얼·EDU 연결

- `01_개발자매뉴얼.md`의 해당 기능 장에 `EDU-DEV-37`를 연결한다.
- 선행 환경, 실행 명령, 입력값, 정상 출력, 오류 재현 명령, 복구 명령, ADM/BZA/Gateway 확인 위치를 그대로 따라 할 수 있게 작성한다.
- 교육 코드에서 고객 업무로 바꿀 Package·Table·API·Topic·파일명과 CPF 관리 영역을 구분한 전환표를 제공한다.

## 완료 조건

- `EDU-DEV-37`가 교육 Catalog·Source·Test·고객 매뉴얼에 동일 ID로 존재한다.
- 정상·Validation·권한·중복·동시성·Timeout·응답 유실·부분 실패·복구 Test가 통과한다.
- 요청·응답·상태·DB·이벤트·파일·운영 화면이 양방향으로 일치한다.
- 운영자가 다른 사람의 설명 없이 장애를 재현하고 정상화 판정을 할 수 있다.
- 실행하지 않은 Test는 성공으로 기록하지 않고, 실패·제약이 남으면 전체 완료로 보고하지 않는다.

## 금지 사항

- 메모리 Map·고정 JSON·정상 Case 한 건만으로 완료 처리
- 교육용 고객 업무를 `cpf-core`, `cpf-starters/common`, Starter, Runtime 제품 내부에 삽입
- Module-local DB Vendor Fallback SQL 또는 Vendor별 의미가 다른 Schema
- Timeout이나 응답 유실 뒤 실제 결과 확인 없이 무조건 재호출
- 비밀번호·Token·개인정보 원문을 Source·Log·Fixture·문서에 기록
- 매뉴얼 설명과 다른 임의 Package·API·상태·메뉴를 새로 생성

---

# 72. EDU-DEV-38 — 다중 Tenant 격리·설정·데이터 범위

- 우선순위: **P0**
- 연결 매뉴얼: `01_개발자매뉴얼.md`
- Owner: `cpf-education`
- 구현 상태 목표: Source·SQL·API·Config·Test·운영 확인·매뉴얼 연결이 모두 완료

## 필요 근거

- 고객 매뉴얼에서 `다중 Tenant 격리·설정·데이터 범위`을 설명하려면 정상 호출 예시만이 아니라 상태 변화와 장애 후 정상화까지 재현 가능한 실행 예제가 필요하다.
- 이 요구사항이 없으면 고객은 기능 가능 여부를 알더라도 실제 업무 적용 순서, 실패 판정, 재처리 금지 조건을 확인할 수 없다.
- 내부 구현 구조를 보여 주기 위한 예제가 아니라 고객 업무를 CPF로 만드는 방법과 운영 결과를 증명하는 예제로 구현한다.

## 고객 업무 시나리오

여러 고객사가 같은 플랫폼을 사용해도 데이터·설정·Cache·Topic이 섞이지 않는다.

## 구현 위치와 책임 경계

- 업무 Source: `com.cpf.education.edu.dev38` 또는 같은 의미의 기존 표준 Slice
- Test: Owner Module의 기존 Test Root 아래 `EDU-DEV-38` 식별 가능한 Class·Fixture
- SQL: 중앙 DB Vendor Pack의 기존 Install·Upgrade·Rollback·Verify·Seed 구조
- Config: 기존 Prefix·Profile·Secret 참조 방식을 유지
- 고객 업무 상태 변경과 원장 데이터는 고객 업무 Owner가 소유한다. ADM·BZA·Gateway·운영 Script가 업무 Table을 직접 수정하지 않는다.
- 공통 기능이 필요하면 기존 Public API·SPI를 우선 사용하고, 진입점이 없을 때만 실제 Consumer를 근거로 Owner Module에 확장한다.

## 입력·계약

- `tenantId`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `userId`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `scope`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `tenantConfigVersion`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- 해당 기능에 필요한 Operation ID·업무 ID·Correlation ID·요청 Hash·사용자·권한 문맥을 저장·Log·Trace·Audit에서 연결한다.
- 요청·응답·Job Parameter·이벤트·파일 Layout 중 해당되는 계약을 OpenAPI 또는 Machine-readable Schema로 제공한다.

## 상태와 저장 근거

- 필수 상태 흐름: `TENANT_ACTIVE/SUSPENDED; 요청마다 TENANT_CONTEXT_BOUND`
- 상태마다 진입 조건, 허용 조치, 재시도 가능 여부, 최종 여부, 저장 위치와 Version 규칙을 정의한다.
- 결과 미확정·부분 성공이 가능한 경우 대상별 Attempt와 상대 상태 조회 근거를 별도 저장한다.

## 정상 처리 순서

1. 입력·권한·데이터 범위·기준 Version과 선행 상태를 검증한다.
2. 상태 변경·비동기 실행인 경우 Operation과 업무 식별자를 생성하거나 기존 멱등 요청을 조회한다. 조회 전용 기능은 조회 Context와 Cursor만 사용한다.
3. 상태 변경 기능은 고객 업무 Owner의 Application Use Case에서 Transaction 경계와 상태 전이를 수행하고, 조회 전용 기능은 일관된 Snapshot·정렬·범위 조건을 적용한다.
4. 외부 시스템·Kafka·파일·Batch·Gateway가 필요한 경우 Adapter 경계에서 Timeout·Retry·보안 정책을 적용한다.
5. 업무 결과와 Attempt·Audit·Log·Metric·Trace를 같은 식별자로 연결한다.
6. 고객 API 또는 운영 화면에서 최종 상태·건수·금액·Version·오류 0건을 확인한다.

## 예외·장애·경계 사례

- tenantId 누락·위조
- Cross-tenant IDOR
- Cache key 누락
- Topic 공유
- 관리자 범위
- 상태 변경이 있는 경우 DB Commit 전 실패와 Commit 후 응답 유실을 구분한다.
- 같은 요청의 중복·동시 실행·순서 역전·늦은 응답을 포함한다.
- Retry 가능한 오류와 즉시 중단해야 하는 업무 오류를 구분한다.
- 대량·다중 대상 기능에서 부분 성공이 발생하면 성공 대상을 다시 실행하지 않고 실패 대상만 Reprocess·Reconcile한다.
- 복구·보상이 필요한 기능은 복구 실패와 보상 실패도 별도 상태·Attempt·Audit로 남긴다.

## 필수 Test

- Repository filter
- permission
- migration
- cache/topic isolation
- penetration test
- 입력 Validation·권한·Data Scope·Masking Negative Test
- 해당 기능에 적용되는 중복·동시성·Timeout·응답 유실·부분 실패 Test
- 해당 기능의 Failure Injection 후 필요한 Retry·Restart·Reprocess·Reconcile·Rollback Test
- Oracle·PostgreSQL·MariaDB 또는 해당 Infra의 의미 동일성 Test
- Log·Metric·Trace·Audit에 Secret·개인정보 원문이 없는지 검사

## 운영·화면 확인

- BZA/ADM에서 tenant scope와 Cross-tenant 감사 이벤트 확인
- 화면에는 해당 메뉴에 실제로 존재하는 조회 조건, 기본값, Column, 상세 Field, 상태, Button 활성 조건, 사유, 승인, 예상 Version, 대상별 결과만 Source와 일치하게 표시한다. 조회 전용 메뉴에 Command 항목을 만들지 않는다.
- 운영자가 정상화 완료를 판단할 수 있는 건수·금액·Version·Checksum·최종 상태·마지막 오류를 제공한다.
- 변경·외부연계 기능에서 응답 유실이 가능한 경우에만 요청을 다시 보내기 전에 Operation·거래·상대 상태를 조회하는 절차를 Browser 또는 API Test로 검증한다.

## 고객 매뉴얼·EDU 연결

- `01_개발자매뉴얼.md`의 해당 기능 장에 `EDU-DEV-38`를 연결한다.
- 선행 환경, 실행 명령, 입력값, 정상 출력, 오류 재현 명령, 복구 명령, ADM/BZA/Gateway 확인 위치를 그대로 따라 할 수 있게 작성한다.
- 교육 코드에서 고객 업무로 바꿀 Package·Table·API·Topic·파일명과 CPF 관리 영역을 구분한 전환표를 제공한다.

## 완료 조건

- `EDU-DEV-38`가 교육 Catalog·Source·Test·고객 매뉴얼에 동일 ID로 존재한다.
- 정상·Validation·권한·중복·동시성·Timeout·응답 유실·부분 실패·복구 Test가 통과한다.
- 요청·응답·상태·DB·이벤트·파일·운영 화면이 양방향으로 일치한다.
- 운영자가 다른 사람의 설명 없이 장애를 재현하고 정상화 판정을 할 수 있다.
- 실행하지 않은 Test는 성공으로 기록하지 않고, 실패·제약이 남으면 전체 완료로 보고하지 않는다.

## 금지 사항

- 메모리 Map·고정 JSON·정상 Case 한 건만으로 완료 처리
- 교육용 고객 업무를 `cpf-core`, `cpf-starters/common`, Starter, Runtime 제품 내부에 삽입
- Module-local DB Vendor Fallback SQL 또는 Vendor별 의미가 다른 Schema
- Timeout이나 응답 유실 뒤 실제 결과 확인 없이 무조건 재호출
- 비밀번호·Token·개인정보 원문을 Source·Log·Fixture·문서에 기록
- 매뉴얼 설명과 다른 임의 Package·API·상태·메뉴를 새로 생성

---

# 73. EDU-DEV-39 — 업무일자·시간대·휴일 Calendar

- 우선순위: **P0**
- 연결 매뉴얼: `01_개발자매뉴얼.md`
- Owner: `cpf-education`
- 구현 상태 목표: Source·SQL·API·Config·Test·운영 확인·매뉴얼 연결이 모두 완료

## 필요 근거

- 고객 매뉴얼에서 `업무일자·시간대·휴일 Calendar`을 설명하려면 정상 호출 예시만이 아니라 상태 변화와 장애 후 정상화까지 재현 가능한 실행 예제가 필요하다.
- 이 요구사항이 없으면 고객은 기능 가능 여부를 알더라도 실제 업무 적용 순서, 실패 판정, 재처리 금지 조건을 확인할 수 없다.
- 내부 구현 구조를 보여 주기 위한 예제가 아니라 고객 업무를 CPF로 만드는 방법과 운영 결과를 증명하는 예제로 구현한다.

## 고객 업무 시나리오

한국 영업일 기준 마감과 UTC 저장을 함께 사용하며 DST 지역 기관 연계도 처리한다.

## 구현 위치와 책임 경계

- 업무 Source: `com.cpf.education.edu.dev39` 또는 같은 의미의 기존 표준 Slice
- Test: Owner Module의 기존 Test Root 아래 `EDU-DEV-39` 식별 가능한 Class·Fixture
- SQL: 중앙 DB Vendor Pack의 기존 Install·Upgrade·Rollback·Verify·Seed 구조
- Config: 기존 Prefix·Profile·Secret 참조 방식을 유지
- 고객 업무 상태 변경과 원장 데이터는 고객 업무 Owner가 소유한다. ADM·BZA·Gateway·운영 Script가 업무 Table을 직접 수정하지 않는다.
- 공통 기능이 필요하면 기존 Public API·SPI를 우선 사용하고, 진입점이 없을 때만 실제 Consumer를 근거로 Owner Module에 확장한다.

## 입력·계약

- `businessDate`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `zoneId`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `calendarId`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `cutoffTime`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- 해당 기능에 필요한 Operation ID·업무 ID·Correlation ID·요청 Hash·사용자·권한 문맥을 저장·Log·Trace·Audit에서 연결한다.
- 요청·응답·Job Parameter·이벤트·파일 Layout 중 해당되는 계약을 OpenAPI 또는 Machine-readable Schema로 제공한다.

## 상태와 저장 근거

- 필수 상태 흐름: `OPEN → CUTOFF → CLOSED; DATE_RESOLVED`
- 상태마다 진입 조건, 허용 조치, 재시도 가능 여부, 최종 여부, 저장 위치와 Version 규칙을 정의한다.
- 결과 미확정·부분 성공이 가능한 경우 대상별 Attempt와 상대 상태 조회 근거를 별도 저장한다.

## 정상 처리 순서

1. 입력·권한·데이터 범위·기준 Version과 선행 상태를 검증한다.
2. 상태 변경·비동기 실행인 경우 Operation과 업무 식별자를 생성하거나 기존 멱등 요청을 조회한다. 조회 전용 기능은 조회 Context와 Cursor만 사용한다.
3. 상태 변경 기능은 고객 업무 Owner의 Application Use Case에서 Transaction 경계와 상태 전이를 수행하고, 조회 전용 기능은 일관된 Snapshot·정렬·범위 조건을 적용한다.
4. 외부 시스템·Kafka·파일·Batch·Gateway가 필요한 경우 Adapter 경계에서 Timeout·Retry·보안 정책을 적용한다.
5. 업무 결과와 Attempt·Audit·Log·Metric·Trace를 같은 식별자로 연결한다.
6. 고객 API 또는 운영 화면에서 최종 상태·건수·금액·Version·오류 0건을 확인한다.

## 예외·장애·경계 사례

- 자정 경계
- 윤일
- DST gap/overlap
- 휴일 변경
- 소급 Calendar
- 상태 변경이 있는 경우 DB Commit 전 실패와 Commit 후 응답 유실을 구분한다.
- 같은 요청의 중복·동시 실행·순서 역전·늦은 응답을 포함한다.
- Retry 가능한 오류와 즉시 중단해야 하는 업무 오류를 구분한다.
- 대량·다중 대상 기능에서 부분 성공이 발생하면 성공 대상을 다시 실행하지 않고 실패 대상만 Reprocess·Reconcile한다.
- 복구·보상이 필요한 기능은 복구 실패와 보상 실패도 별도 상태·Attempt·Audit로 남긴다.

## 필수 Test

- Clock 주입
- timezone DB 3종
- calendar version
- scheduler 연동
- 입력 Validation·권한·Data Scope·Masking Negative Test
- 해당 기능에 적용되는 중복·동시성·Timeout·응답 유실·부분 실패 Test
- 해당 기능의 Failure Injection 후 필요한 Retry·Restart·Reprocess·Reconcile·Rollback Test
- Oracle·PostgreSQL·MariaDB 또는 해당 Infra의 의미 동일성 Test
- Log·Metric·Trace·Audit에 Secret·개인정보 원문이 없는지 검사

## 운영·화면 확인

- ADM Business Calendar에서 적용일·Version·다음 영업일 확인
- 화면에는 해당 메뉴에 실제로 존재하는 조회 조건, 기본값, Column, 상세 Field, 상태, Button 활성 조건, 사유, 승인, 예상 Version, 대상별 결과만 Source와 일치하게 표시한다. 조회 전용 메뉴에 Command 항목을 만들지 않는다.
- 운영자가 정상화 완료를 판단할 수 있는 건수·금액·Version·Checksum·최종 상태·마지막 오류를 제공한다.
- 변경·외부연계 기능에서 응답 유실이 가능한 경우에만 요청을 다시 보내기 전에 Operation·거래·상대 상태를 조회하는 절차를 Browser 또는 API Test로 검증한다.

## 고객 매뉴얼·EDU 연결

- `01_개발자매뉴얼.md`의 해당 기능 장에 `EDU-DEV-39`를 연결한다.
- 선행 환경, 실행 명령, 입력값, 정상 출력, 오류 재현 명령, 복구 명령, ADM/BZA/Gateway 확인 위치를 그대로 따라 할 수 있게 작성한다.
- 교육 코드에서 고객 업무로 바꿀 Package·Table·API·Topic·파일명과 CPF 관리 영역을 구분한 전환표를 제공한다.

## 완료 조건

- `EDU-DEV-39`가 교육 Catalog·Source·Test·고객 매뉴얼에 동일 ID로 존재한다.
- 정상·Validation·권한·중복·동시성·Timeout·응답 유실·부분 실패·복구 Test가 통과한다.
- 요청·응답·상태·DB·이벤트·파일·운영 화면이 양방향으로 일치한다.
- 운영자가 다른 사람의 설명 없이 장애를 재현하고 정상화 판정을 할 수 있다.
- 실행하지 않은 Test는 성공으로 기록하지 않고, 실패·제약이 남으면 전체 완료로 보고하지 않는다.

## 금지 사항

- 메모리 Map·고정 JSON·정상 Case 한 건만으로 완료 처리
- 교육용 고객 업무를 `cpf-core`, `cpf-starters/common`, Starter, Runtime 제품 내부에 삽입
- Module-local DB Vendor Fallback SQL 또는 Vendor별 의미가 다른 Schema
- Timeout이나 응답 유실 뒤 실제 결과 확인 없이 무조건 재호출
- 비밀번호·Token·개인정보 원문을 Source·Log·Fixture·문서에 기록
- 매뉴얼 설명과 다른 임의 Package·API·상태·메뉴를 새로 생성

---

# 74. EDU-DEV-40 — 금액·통화·반올림·환율 Version

- 우선순위: **P0**
- 연결 매뉴얼: `01_개발자매뉴얼.md`
- Owner: `cpf-education`
- 구현 상태 목표: Source·SQL·API·Config·Test·운영 확인·매뉴얼 연결이 모두 완료

## 필요 근거

- 고객 매뉴얼에서 `금액·통화·반올림·환율 Version`을 설명하려면 정상 호출 예시만이 아니라 상태 변화와 장애 후 정상화까지 재현 가능한 실행 예제가 필요하다.
- 이 요구사항이 없으면 고객은 기능 가능 여부를 알더라도 실제 업무 적용 순서, 실패 판정, 재처리 금지 조건을 확인할 수 없다.
- 내부 구현 구조를 보여 주기 위한 예제가 아니라 고객 업무를 CPF로 만드는 방법과 운영 결과를 증명하는 예제로 구현한다.

## 고객 업무 시나리오

다중 통화 금액을 정확한 Scale로 계산하고 적용 환율과 반올림 규칙을 감사 가능하게 남긴다.

## 구현 위치와 책임 경계

- 업무 Source: `com.cpf.education.edu.dev40` 또는 같은 의미의 기존 표준 Slice
- Test: Owner Module의 기존 Test Root 아래 `EDU-DEV-40` 식별 가능한 Class·Fixture
- SQL: 중앙 DB Vendor Pack의 기존 Install·Upgrade·Rollback·Verify·Seed 구조
- Config: 기존 Prefix·Profile·Secret 참조 방식을 유지
- 고객 업무 상태 변경과 원장 데이터는 고객 업무 Owner가 소유한다. ADM·BZA·Gateway·운영 Script가 업무 Table을 직접 수정하지 않는다.
- 공통 기능이 필요하면 기존 Public API·SPI를 우선 사용하고, 진입점이 없을 때만 실제 Consumer를 근거로 Owner Module에 확장한다.

## 입력·계약

- `amount`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `currency`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `targetCurrency`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `rateDate`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `roundingMode`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- 해당 기능에 필요한 Operation ID·업무 ID·Correlation ID·요청 Hash·사용자·권한 문맥을 저장·Log·Trace·Audit에서 연결한다.
- 요청·응답·Job Parameter·이벤트·파일 Layout 중 해당되는 계약을 OpenAPI 또는 Machine-readable Schema로 제공한다.

## 상태와 저장 근거

- 필수 상태 흐름: `QUOTED → CONFIRMED → POSTED; RATE_VERSION_CAPTURED`
- 상태마다 진입 조건, 허용 조치, 재시도 가능 여부, 최종 여부, 저장 위치와 Version 규칙을 정의한다.
- 결과 미확정·부분 성공이 가능한 경우 대상별 Attempt와 상대 상태 조회 근거를 별도 저장한다.

## 정상 처리 순서

1. 입력·권한·데이터 범위·기준 Version과 선행 상태를 검증한다.
2. 상태 변경·비동기 실행인 경우 Operation과 업무 식별자를 생성하거나 기존 멱등 요청을 조회한다. 조회 전용 기능은 조회 Context와 Cursor만 사용한다.
3. 상태 변경 기능은 고객 업무 Owner의 Application Use Case에서 Transaction 경계와 상태 전이를 수행하고, 조회 전용 기능은 일관된 Snapshot·정렬·범위 조건을 적용한다.
4. 외부 시스템·Kafka·파일·Batch·Gateway가 필요한 경우 Adapter 경계에서 Timeout·Retry·보안 정책을 적용한다.
5. 업무 결과와 Attempt·Audit·Log·Metric·Trace를 같은 식별자로 연결한다.
6. 고객 API 또는 운영 화면에서 최종 상태·건수·금액·Version·오류 0건을 확인한다.

## 예외·장애·경계 사례

- 0.005 반올림
- JPY 0 scale
- 환율 누락
- 음수·한도
- 재계산 차이
- 상태 변경이 있는 경우 DB Commit 전 실패와 Commit 후 응답 유실을 구분한다.
- 같은 요청의 중복·동시 실행·순서 역전·늦은 응답을 포함한다.
- Retry 가능한 오류와 즉시 중단해야 하는 업무 오류를 구분한다.
- 대량·다중 대상 기능에서 부분 성공이 발생하면 성공 대상을 다시 실행하지 않고 실패 대상만 Reprocess·Reconcile한다.
- 복구·보상이 필요한 기능은 복구 실패와 보상 실패도 별도 상태·Attempt·Audit로 남긴다.

## 필수 Test

- BigDecimal
- currency metadata
- rate version
- DB precision
- property test
- 입력 Validation·권한·Data Scope·Masking Negative Test
- 해당 기능에 적용되는 중복·동시성·Timeout·응답 유실·부분 실패 Test
- 해당 기능의 Failure Injection 후 필요한 Retry·Restart·Reprocess·Reconcile·Rollback Test
- Oracle·PostgreSQL·MariaDB 또는 해당 Infra의 의미 동일성 Test
- Log·Metric·Trace·Audit에 Secret·개인정보 원문이 없는지 검사

## 운영·화면 확인

- ADM 거래 상세에서 원금액·환율·결과금액·규칙 확인
- 화면에는 해당 메뉴에 실제로 존재하는 조회 조건, 기본값, Column, 상세 Field, 상태, Button 활성 조건, 사유, 승인, 예상 Version, 대상별 결과만 Source와 일치하게 표시한다. 조회 전용 메뉴에 Command 항목을 만들지 않는다.
- 운영자가 정상화 완료를 판단할 수 있는 건수·금액·Version·Checksum·최종 상태·마지막 오류를 제공한다.
- 변경·외부연계 기능에서 응답 유실이 가능한 경우에만 요청을 다시 보내기 전에 Operation·거래·상대 상태를 조회하는 절차를 Browser 또는 API Test로 검증한다.

## 고객 매뉴얼·EDU 연결

- `01_개발자매뉴얼.md`의 해당 기능 장에 `EDU-DEV-40`를 연결한다.
- 선행 환경, 실행 명령, 입력값, 정상 출력, 오류 재현 명령, 복구 명령, ADM/BZA/Gateway 확인 위치를 그대로 따라 할 수 있게 작성한다.
- 교육 코드에서 고객 업무로 바꿀 Package·Table·API·Topic·파일명과 CPF 관리 영역을 구분한 전환표를 제공한다.

## 완료 조건

- `EDU-DEV-40`가 교육 Catalog·Source·Test·고객 매뉴얼에 동일 ID로 존재한다.
- 정상·Validation·권한·중복·동시성·Timeout·응답 유실·부분 실패·복구 Test가 통과한다.
- 요청·응답·상태·DB·이벤트·파일·운영 화면이 양방향으로 일치한다.
- 운영자가 다른 사람의 설명 없이 장애를 재현하고 정상화 판정을 할 수 있다.
- 실행하지 않은 Test는 성공으로 기록하지 않고, 실패·제약이 남으면 전체 완료로 보고하지 않는다.

## 금지 사항

- 메모리 Map·고정 JSON·정상 Case 한 건만으로 완료 처리
- 교육용 고객 업무를 `cpf-core`, `cpf-starters/common`, Starter, Runtime 제품 내부에 삽입
- Module-local DB Vendor Fallback SQL 또는 Vendor별 의미가 다른 Schema
- Timeout이나 응답 유실 뒤 실제 결과 확인 없이 무조건 재호출
- 비밀번호·Token·개인정보 원문을 Source·Log·Fixture·문서에 기록
- 매뉴얼 설명과 다른 임의 Package·API·상태·메뉴를 새로 생성

---

# 75. EDU-DEV-41 — 감사 증적 Export·무결성 Hash·검증

- 우선순위: **P0**
- 연결 매뉴얼: `01_개발자매뉴얼.md`
- Owner: `cpf-education`
- 구현 상태 목표: Source·SQL·API·Config·Test·운영 확인·매뉴얼 연결이 모두 완료

## 필요 근거

- 고객 매뉴얼에서 `감사 증적 Export·무결성 Hash·검증`을 설명하려면 정상 호출 예시만이 아니라 상태 변화와 장애 후 정상화까지 재현 가능한 실행 예제가 필요하다.
- 이 요구사항이 없으면 고객은 기능 가능 여부를 알더라도 실제 업무 적용 순서, 실패 판정, 재처리 금지 조건을 확인할 수 없다.
- 내부 구현 구조를 보여 주기 위한 예제가 아니라 고객 업무를 CPF로 만드는 방법과 운영 결과를 증명하는 예제로 구현한다.

## 고객 업무 시나리오

감사 대상 기간의 이벤트를 반출하고 파일 Hash와 Manifest로 변조 여부를 검증한다.

## 구현 위치와 책임 경계

- 업무 Source: `com.cpf.education.edu.dev41` 또는 같은 의미의 기존 표준 Slice
- Test: Owner Module의 기존 Test Root 아래 `EDU-DEV-41` 식별 가능한 Class·Fixture
- SQL: 중앙 DB Vendor Pack의 기존 Install·Upgrade·Rollback·Verify·Seed 구조
- Config: 기존 Prefix·Profile·Secret 참조 방식을 유지
- 고객 업무 상태 변경과 원장 데이터는 고객 업무 Owner가 소유한다. ADM·BZA·Gateway·운영 Script가 업무 Table을 직접 수정하지 않는다.
- 공통 기능이 필요하면 기존 Public API·SPI를 우선 사용하고, 진입점이 없을 때만 실제 Consumer를 근거로 Owner Module에 확장한다.

## 입력·계약

- `dateRange`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `filters`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `reason`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `approvalId`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `format`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- 해당 기능에 필요한 Operation ID·업무 ID·Correlation ID·요청 Hash·사용자·권한 문맥을 저장·Log·Trace·Audit에서 연결한다.
- 요청·응답·Job Parameter·이벤트·파일 Layout 중 해당되는 계약을 OpenAPI 또는 Machine-readable Schema로 제공한다.

## 상태와 저장 근거

- 필수 상태 흐름: `REQUESTED → APPROVED → GENERATING → READY/EXPIRED`
- 상태마다 진입 조건, 허용 조치, 재시도 가능 여부, 최종 여부, 저장 위치와 Version 규칙을 정의한다.
- 결과 미확정·부분 성공이 가능한 경우 대상별 Attempt와 상대 상태 조회 근거를 별도 저장한다.

## 정상 처리 순서

1. 입력·권한·데이터 범위·기준 Version과 선행 상태를 검증한다.
2. 상태 변경·비동기 실행인 경우 Operation과 업무 식별자를 생성하거나 기존 멱등 요청을 조회한다. 조회 전용 기능은 조회 Context와 Cursor만 사용한다.
3. 상태 변경 기능은 고객 업무 Owner의 Application Use Case에서 Transaction 경계와 상태 전이를 수행하고, 조회 전용 기능은 일관된 Snapshot·정렬·범위 조건을 적용한다.
4. 외부 시스템·Kafka·파일·Batch·Gateway가 필요한 경우 Adapter 경계에서 Timeout·Retry·보안 정책을 적용한다.
5. 업무 결과와 Attempt·Audit·Log·Metric·Trace를 같은 식별자로 연결한다.
6. 고객 API 또는 운영 화면에서 최종 상태·건수·금액·Version·오류 0건을 확인한다.

## 예외·장애·경계 사례

- 대용량
- 개인정보 Masking
- 승인 만료
- 파일 변경
- 다운로드 재시도
- 상태 변경이 있는 경우 DB Commit 전 실패와 Commit 후 응답 유실을 구분한다.
- 같은 요청의 중복·동시 실행·순서 역전·늦은 응답을 포함한다.
- Retry 가능한 오류와 즉시 중단해야 하는 업무 오류를 구분한다.
- 대량·다중 대상 기능에서 부분 성공이 발생하면 성공 대상을 다시 실행하지 않고 실패 대상만 Reprocess·Reconcile한다.
- 복구·보상이 필요한 기능은 복구 실패와 보상 실패도 별도 상태·Attempt·Audit로 남긴다.

## 필수 Test

- Manifest
- SHA-256
- pagination
- signed metadata
- download audit
- 입력 Validation·권한·Data Scope·Masking Negative Test
- 해당 기능에 적용되는 중복·동시성·Timeout·응답 유실·부분 실패 Test
- 해당 기능의 Failure Injection 후 필요한 Retry·Restart·Reprocess·Reconcile·Rollback Test
- Oracle·PostgreSQL·MariaDB 또는 해당 Infra의 의미 동일성 Test
- Log·Metric·Trace·Audit에 Secret·개인정보 원문이 없는지 검사

## 운영·화면 확인

- ADM Audit/Downloads에서 요청·승인·Hash·다운로드 사용자 확인
- 화면에는 해당 메뉴에 실제로 존재하는 조회 조건, 기본값, Column, 상세 Field, 상태, Button 활성 조건, 사유, 승인, 예상 Version, 대상별 결과만 Source와 일치하게 표시한다. 조회 전용 메뉴에 Command 항목을 만들지 않는다.
- 운영자가 정상화 완료를 판단할 수 있는 건수·금액·Version·Checksum·최종 상태·마지막 오류를 제공한다.
- 변경·외부연계 기능에서 응답 유실이 가능한 경우에만 요청을 다시 보내기 전에 Operation·거래·상대 상태를 조회하는 절차를 Browser 또는 API Test로 검증한다.

## 고객 매뉴얼·EDU 연결

- `01_개발자매뉴얼.md`의 해당 기능 장에 `EDU-DEV-41`를 연결한다.
- 선행 환경, 실행 명령, 입력값, 정상 출력, 오류 재현 명령, 복구 명령, ADM/BZA/Gateway 확인 위치를 그대로 따라 할 수 있게 작성한다.
- 교육 코드에서 고객 업무로 바꿀 Package·Table·API·Topic·파일명과 CPF 관리 영역을 구분한 전환표를 제공한다.

## 완료 조건

- `EDU-DEV-41`가 교육 Catalog·Source·Test·고객 매뉴얼에 동일 ID로 존재한다.
- 정상·Validation·권한·중복·동시성·Timeout·응답 유실·부분 실패·복구 Test가 통과한다.
- 요청·응답·상태·DB·이벤트·파일·운영 화면이 양방향으로 일치한다.
- 운영자가 다른 사람의 설명 없이 장애를 재현하고 정상화 판정을 할 수 있다.
- 실행하지 않은 Test는 성공으로 기록하지 않고, 실패·제약이 남으면 전체 완료로 보고하지 않는다.

## 금지 사항

- 메모리 Map·고정 JSON·정상 Case 한 건만으로 완료 처리
- 교육용 고객 업무를 `cpf-core`, `cpf-starters/common`, Starter, Runtime 제품 내부에 삽입
- Module-local DB Vendor Fallback SQL 또는 Vendor별 의미가 다른 Schema
- Timeout이나 응답 유실 뒤 실제 결과 확인 없이 무조건 재호출
- 비밀번호·Token·개인정보 원문을 Source·Log·Fixture·문서에 기록
- 매뉴얼 설명과 다른 임의 Package·API·상태·메뉴를 새로 생성

---

# 76. EDU-DEV-42 — 로그·Metric·Trace 상관관계와 Sampling

- 우선순위: **P0**
- 연결 매뉴얼: `01_개발자매뉴얼.md`
- Owner: `cpf-education`
- 구현 상태 목표: Source·SQL·API·Config·Test·운영 확인·매뉴얼 연결이 모두 완료

## 필요 근거

- 고객 매뉴얼에서 `로그·Metric·Trace 상관관계와 Sampling`을 설명하려면 정상 호출 예시만이 아니라 상태 변화와 장애 후 정상화까지 재현 가능한 실행 예제가 필요하다.
- 이 요구사항이 없으면 고객은 기능 가능 여부를 알더라도 실제 업무 적용 순서, 실패 판정, 재처리 금지 조건을 확인할 수 없다.
- 내부 구현 구조를 보여 주기 위한 예제가 아니라 고객 업무를 CPF로 만드는 방법과 운영 결과를 증명하는 예제로 구현한다.

## 고객 업무 시나리오

한 고객 요청이 온라인·Kafka·Batch·외부기관을 거쳐도 동일 Correlation로 추적된다.

## 구현 위치와 책임 경계

- 업무 Source: `com.cpf.education.edu.dev42` 또는 같은 의미의 기존 표준 Slice
- Test: Owner Module의 기존 Test Root 아래 `EDU-DEV-42` 식별 가능한 Class·Fixture
- SQL: 중앙 DB Vendor Pack의 기존 Install·Upgrade·Rollback·Verify·Seed 구조
- Config: 기존 Prefix·Profile·Secret 참조 방식을 유지
- 고객 업무 상태 변경과 원장 데이터는 고객 업무 Owner가 소유한다. ADM·BZA·Gateway·운영 Script가 업무 Table을 직접 수정하지 않는다.
- 공통 기능이 필요하면 기존 Public API·SPI를 우선 사용하고, 진입점이 없을 때만 실제 Consumer를 근거로 Owner Module에 확장한다.

## 입력·계약

- `transactionId`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `traceparent`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `correlationId`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `operationId`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- 해당 기능에 필요한 Operation ID·업무 ID·Correlation ID·요청 Hash·사용자·권한 문맥을 저장·Log·Trace·Audit에서 연결한다.
- 요청·응답·Job Parameter·이벤트·파일 Layout 중 해당되는 계약을 OpenAPI 또는 Machine-readable Schema로 제공한다.

## 상태와 저장 근거

- 필수 상태 흐름: `TRACE_STARTED → SPANS_EXPORTED/PARTIAL; METRIC_RECORDED`
- 상태마다 진입 조건, 허용 조치, 재시도 가능 여부, 최종 여부, 저장 위치와 Version 규칙을 정의한다.
- 결과 미확정·부분 성공이 가능한 경우 대상별 Attempt와 상대 상태 조회 근거를 별도 저장한다.

## 정상 처리 순서

1. 입력·권한·데이터 범위·기준 Version과 선행 상태를 검증한다.
2. 상태 변경·비동기 실행인 경우 Operation과 업무 식별자를 생성하거나 기존 멱등 요청을 조회한다. 조회 전용 기능은 조회 Context와 Cursor만 사용한다.
3. 상태 변경 기능은 고객 업무 Owner의 Application Use Case에서 Transaction 경계와 상태 전이를 수행하고, 조회 전용 기능은 일관된 Snapshot·정렬·범위 조건을 적용한다.
4. 외부 시스템·Kafka·파일·Batch·Gateway가 필요한 경우 Adapter 경계에서 Timeout·Retry·보안 정책을 적용한다.
5. 업무 결과와 Attempt·Audit·Log·Metric·Trace를 같은 식별자로 연결한다.
6. 고객 API 또는 운영 화면에서 최종 상태·건수·금액·Version·오류 0건을 확인한다.

## 예외·장애·경계 사례

- Header 누락
- 비동기 Context 유실
- Sampling 제외
- Collector 장애
- 개인정보 Log
- 상태 변경이 있는 경우 DB Commit 전 실패와 Commit 후 응답 유실을 구분한다.
- 같은 요청의 중복·동시 실행·순서 역전·늦은 응답을 포함한다.
- Retry 가능한 오류와 즉시 중단해야 하는 업무 오류를 구분한다.
- 대량·다중 대상 기능에서 부분 성공이 발생하면 성공 대상을 다시 실행하지 않고 실패 대상만 Reprocess·Reconcile한다.
- 복구·보상이 필요한 기능은 복구 실패와 보상 실패도 별도 상태·Attempt·Audit로 남긴다.

## 필수 Test

- Context propagation
- baggage 제한
- OTLP retry
- log masking
- trace test
- 입력 Validation·권한·Data Scope·Masking Negative Test
- 해당 기능에 적용되는 중복·동시성·Timeout·응답 유실·부분 실패 Test
- 해당 기능의 Failure Injection 후 필요한 Retry·Restart·Reprocess·Reconcile·Rollback Test
- Oracle·PostgreSQL·MariaDB 또는 해당 Infra의 의미 동일성 Test
- Log·Metric·Trace·Audit에 Secret·개인정보 원문이 없는지 검사

## 운영·화면 확인

- ADM Transaction·Logs·Remote Logs에서 동일 ID로 연결
- 화면에는 해당 메뉴에 실제로 존재하는 조회 조건, 기본값, Column, 상세 Field, 상태, Button 활성 조건, 사유, 승인, 예상 Version, 대상별 결과만 Source와 일치하게 표시한다. 조회 전용 메뉴에 Command 항목을 만들지 않는다.
- 운영자가 정상화 완료를 판단할 수 있는 건수·금액·Version·Checksum·최종 상태·마지막 오류를 제공한다.
- 변경·외부연계 기능에서 응답 유실이 가능한 경우에만 요청을 다시 보내기 전에 Operation·거래·상대 상태를 조회하는 절차를 Browser 또는 API Test로 검증한다.

## 고객 매뉴얼·EDU 연결

- `01_개발자매뉴얼.md`의 해당 기능 장에 `EDU-DEV-42`를 연결한다.
- 선행 환경, 실행 명령, 입력값, 정상 출력, 오류 재현 명령, 복구 명령, ADM/BZA/Gateway 확인 위치를 그대로 따라 할 수 있게 작성한다.
- 교육 코드에서 고객 업무로 바꿀 Package·Table·API·Topic·파일명과 CPF 관리 영역을 구분한 전환표를 제공한다.

## 완료 조건

- `EDU-DEV-42`가 교육 Catalog·Source·Test·고객 매뉴얼에 동일 ID로 존재한다.
- 정상·Validation·권한·중복·동시성·Timeout·응답 유실·부분 실패·복구 Test가 통과한다.
- 요청·응답·상태·DB·이벤트·파일·운영 화면이 양방향으로 일치한다.
- 운영자가 다른 사람의 설명 없이 장애를 재현하고 정상화 판정을 할 수 있다.
- 실행하지 않은 Test는 성공으로 기록하지 않고, 실패·제약이 남으면 전체 완료로 보고하지 않는다.

## 금지 사항

- 메모리 Map·고정 JSON·정상 Case 한 건만으로 완료 처리
- 교육용 고객 업무를 `cpf-core`, `cpf-starters/common`, Starter, Runtime 제품 내부에 삽입
- Module-local DB Vendor Fallback SQL 또는 Vendor별 의미가 다른 Schema
- Timeout이나 응답 유실 뒤 실제 결과 확인 없이 무조건 재호출
- 비밀번호·Token·개인정보 원문을 Source·Log·Fixture·문서에 기록
- 매뉴얼 설명과 다른 임의 Package·API·상태·메뉴를 새로 생성

---

# 77. EDU-DEV-43 — API Version 전환·하위 호환·폐기

- 우선순위: **P0**
- 연결 매뉴얼: `01_개발자매뉴얼.md`
- Owner: `cpf-education`
- 구현 상태 목표: Source·SQL·API·Config·Test·운영 확인·매뉴얼 연결이 모두 완료

## 필요 근거

- 고객 매뉴얼에서 `API Version 전환·하위 호환·폐기`을 설명하려면 정상 호출 예시만이 아니라 상태 변화와 장애 후 정상화까지 재현 가능한 실행 예제가 필요하다.
- 이 요구사항이 없으면 고객은 기능 가능 여부를 알더라도 실제 업무 적용 순서, 실패 판정, 재처리 금지 조건을 확인할 수 없다.
- 내부 구현 구조를 보여 주기 위한 예제가 아니라 고객 업무를 CPF로 만드는 방법과 운영 결과를 증명하는 예제로 구현한다.

## 고객 업무 시나리오

v1 고객을 유지하면서 v2 필드를 추가하고 폐기 일정을 Header와 문서로 안내한다.

## 구현 위치와 책임 경계

- 업무 Source: `com.cpf.education.edu.dev43` 또는 같은 의미의 기존 표준 Slice
- Test: Owner Module의 기존 Test Root 아래 `EDU-DEV-43` 식별 가능한 Class·Fixture
- SQL: 중앙 DB Vendor Pack의 기존 Install·Upgrade·Rollback·Verify·Seed 구조
- Config: 기존 Prefix·Profile·Secret 참조 방식을 유지
- 고객 업무 상태 변경과 원장 데이터는 고객 업무 Owner가 소유한다. ADM·BZA·Gateway·운영 Script가 업무 Table을 직접 수정하지 않는다.
- 공통 기능이 필요하면 기존 Public API·SPI를 우선 사용하고, 진입점이 없을 때만 실제 Consumer를 근거로 Owner Module에 확장한다.

## 입력·계약

- `apiVersion`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `acceptHeader`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `clientId`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- 해당 기능에 필요한 Operation ID·업무 ID·Correlation ID·요청 Hash·사용자·권한 문맥을 저장·Log·Trace·Audit에서 연결한다.
- 요청·응답·Job Parameter·이벤트·파일 Layout 중 해당되는 계약을 OpenAPI 또는 Machine-readable Schema로 제공한다.

## 상태와 저장 근거

- 필수 상태 흐름: `V1_ACTIVE/V2_ACTIVE → V1_DEPRECATED → V1_RETIRED`
- 상태마다 진입 조건, 허용 조치, 재시도 가능 여부, 최종 여부, 저장 위치와 Version 규칙을 정의한다.
- 결과 미확정·부분 성공이 가능한 경우 대상별 Attempt와 상대 상태 조회 근거를 별도 저장한다.

## 정상 처리 순서

1. 입력·권한·데이터 범위·기준 Version과 선행 상태를 검증한다.
2. 상태 변경·비동기 실행인 경우 Operation과 업무 식별자를 생성하거나 기존 멱등 요청을 조회한다. 조회 전용 기능은 조회 Context와 Cursor만 사용한다.
3. 상태 변경 기능은 고객 업무 Owner의 Application Use Case에서 Transaction 경계와 상태 전이를 수행하고, 조회 전용 기능은 일관된 Snapshot·정렬·범위 조건을 적용한다.
4. 외부 시스템·Kafka·파일·Batch·Gateway가 필요한 경우 Adapter 경계에서 Timeout·Retry·보안 정책을 적용한다.
5. 업무 결과와 Attempt·Audit·Log·Metric·Trace를 같은 식별자로 연결한다.
6. 고객 API 또는 운영 화면에서 최종 상태·건수·금액·Version·오류 0건을 확인한다.

## 예외·장애·경계 사례

- 필수 Field 추가
- enum 확장
- 오래된 Client
- Gateway route 혼선
- rollback
- 상태 변경이 있는 경우 DB Commit 전 실패와 Commit 후 응답 유실을 구분한다.
- 같은 요청의 중복·동시 실행·순서 역전·늦은 응답을 포함한다.
- Retry 가능한 오류와 즉시 중단해야 하는 업무 오류를 구분한다.
- 대량·다중 대상 기능에서 부분 성공이 발생하면 성공 대상을 다시 실행하지 않고 실패 대상만 Reprocess·Reconcile한다.
- 복구·보상이 필요한 기능은 복구 실패와 보상 실패도 별도 상태·Attempt·Audit로 남긴다.

## 필수 Test

- Contract test
- generated client
- deprecation header
- dual-run comparison
- 입력 Validation·권한·Data Scope·Masking Negative Test
- 해당 기능에 적용되는 중복·동시성·Timeout·응답 유실·부분 실패 Test
- 해당 기능의 Failure Injection 후 필요한 Retry·Restart·Reprocess·Reconcile·Rollback Test
- Oracle·PostgreSQL·MariaDB 또는 해당 Infra의 의미 동일성 Test
- Log·Metric·Trace·Audit에 Secret·개인정보 원문이 없는지 검사

## 운영·화면 확인

- Gateway Route와 ADM 거래에서 Version별 호출량·오류율 확인
- 화면에는 해당 메뉴에 실제로 존재하는 조회 조건, 기본값, Column, 상세 Field, 상태, Button 활성 조건, 사유, 승인, 예상 Version, 대상별 결과만 Source와 일치하게 표시한다. 조회 전용 메뉴에 Command 항목을 만들지 않는다.
- 운영자가 정상화 완료를 판단할 수 있는 건수·금액·Version·Checksum·최종 상태·마지막 오류를 제공한다.
- 변경·외부연계 기능에서 응답 유실이 가능한 경우에만 요청을 다시 보내기 전에 Operation·거래·상대 상태를 조회하는 절차를 Browser 또는 API Test로 검증한다.

## 고객 매뉴얼·EDU 연결

- `01_개발자매뉴얼.md`의 해당 기능 장에 `EDU-DEV-43`를 연결한다.
- 선행 환경, 실행 명령, 입력값, 정상 출력, 오류 재현 명령, 복구 명령, ADM/BZA/Gateway 확인 위치를 그대로 따라 할 수 있게 작성한다.
- 교육 코드에서 고객 업무로 바꿀 Package·Table·API·Topic·파일명과 CPF 관리 영역을 구분한 전환표를 제공한다.

## 완료 조건

- `EDU-DEV-43`가 교육 Catalog·Source·Test·고객 매뉴얼에 동일 ID로 존재한다.
- 정상·Validation·권한·중복·동시성·Timeout·응답 유실·부분 실패·복구 Test가 통과한다.
- 요청·응답·상태·DB·이벤트·파일·운영 화면이 양방향으로 일치한다.
- 운영자가 다른 사람의 설명 없이 장애를 재현하고 정상화 판정을 할 수 있다.
- 실행하지 않은 Test는 성공으로 기록하지 않고, 실패·제약이 남으면 전체 완료로 보고하지 않는다.

## 금지 사항

- 메모리 Map·고정 JSON·정상 Case 한 건만으로 완료 처리
- 교육용 고객 업무를 `cpf-core`, `cpf-starters/common`, Starter, Runtime 제품 내부에 삽입
- Module-local DB Vendor Fallback SQL 또는 Vendor별 의미가 다른 Schema
- Timeout이나 응답 유실 뒤 실제 결과 확인 없이 무조건 재호출
- 비밀번호·Token·개인정보 원문을 Source·Log·Fixture·문서에 기록
- 매뉴얼 설명과 다른 임의 Package·API·상태·메뉴를 새로 생성

---

# 78. EDU-DEV-44 — Event Schema 진화·호환성·Dead Letter

- 우선순위: **P0**
- 연결 매뉴얼: `01_개발자매뉴얼.md`
- Owner: `cpf-education`
- 구현 상태 목표: Source·SQL·API·Config·Test·운영 확인·매뉴얼 연결이 모두 완료

## 필요 근거

- 고객 매뉴얼에서 `Event Schema 진화·호환성·Dead Letter`을 설명하려면 정상 호출 예시만이 아니라 상태 변화와 장애 후 정상화까지 재현 가능한 실행 예제가 필요하다.
- 이 요구사항이 없으면 고객은 기능 가능 여부를 알더라도 실제 업무 적용 순서, 실패 판정, 재처리 금지 조건을 확인할 수 없다.
- 내부 구현 구조를 보여 주기 위한 예제가 아니라 고객 업무를 CPF로 만드는 방법과 운영 결과를 증명하는 예제로 구현한다.

## 고객 업무 시나리오

Kafka 이벤트 v1 Consumer를 깨지 않고 v2 선택 필드를 추가하며 비호환 메시지는 격리한다.

## 구현 위치와 책임 경계

- 업무 Source: `com.cpf.education.edu.dev44` 또는 같은 의미의 기존 표준 Slice
- Test: Owner Module의 기존 Test Root 아래 `EDU-DEV-44` 식별 가능한 Class·Fixture
- SQL: 중앙 DB Vendor Pack의 기존 Install·Upgrade·Rollback·Verify·Seed 구조
- Config: 기존 Prefix·Profile·Secret 참조 방식을 유지
- 고객 업무 상태 변경과 원장 데이터는 고객 업무 Owner가 소유한다. ADM·BZA·Gateway·운영 Script가 업무 Table을 직접 수정하지 않는다.
- 공통 기능이 필요하면 기존 Public API·SPI를 우선 사용하고, 진입점이 없을 때만 실제 Consumer를 근거로 Owner Module에 확장한다.

## 입력·계약

- `schemaVersion`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `eventType`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `payload`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- 해당 기능에 필요한 Operation ID·업무 ID·Correlation ID·요청 Hash·사용자·권한 문맥을 저장·Log·Trace·Audit에서 연결한다.
- 요청·응답·Job Parameter·이벤트·파일 Layout 중 해당되는 계약을 OpenAPI 또는 Machine-readable Schema로 제공한다.

## 상태와 저장 근거

- 필수 상태 흐름: `PUBLISHED → VALIDATED → CONSUMED/DLQ/QUARANTINED`
- 상태마다 진입 조건, 허용 조치, 재시도 가능 여부, 최종 여부, 저장 위치와 Version 규칙을 정의한다.
- 결과 미확정·부분 성공이 가능한 경우 대상별 Attempt와 상대 상태 조회 근거를 별도 저장한다.

## 정상 처리 순서

1. 입력·권한·데이터 범위·기준 Version과 선행 상태를 검증한다.
2. 상태 변경·비동기 실행인 경우 Operation과 업무 식별자를 생성하거나 기존 멱등 요청을 조회한다. 조회 전용 기능은 조회 Context와 Cursor만 사용한다.
3. 상태 변경 기능은 고객 업무 Owner의 Application Use Case에서 Transaction 경계와 상태 전이를 수행하고, 조회 전용 기능은 일관된 Snapshot·정렬·범위 조건을 적용한다.
4. 외부 시스템·Kafka·파일·Batch·Gateway가 필요한 경우 Adapter 경계에서 Timeout·Retry·보안 정책을 적용한다.
5. 업무 결과와 Attempt·Audit·Log·Metric·Trace를 같은 식별자로 연결한다.
6. 고객 API 또는 운영 화면에서 최종 상태·건수·금액·Version·오류 0건을 확인한다.

## 예외·장애·경계 사례

- 필드 삭제
- 타입 변경
- 알 수 없는 enum
- 순서 뒤집힘
- 구 Consumer
- 상태 변경이 있는 경우 DB Commit 전 실패와 Commit 후 응답 유실을 구분한다.
- 같은 요청의 중복·동시 실행·순서 역전·늦은 응답을 포함한다.
- Retry 가능한 오류와 즉시 중단해야 하는 업무 오류를 구분한다.
- 대량·다중 대상 기능에서 부분 성공이 발생하면 성공 대상을 다시 실행하지 않고 실패 대상만 Reprocess·Reconcile한다.
- 복구·보상이 필요한 기능은 복구 실패와 보상 실패도 별도 상태·Attempt·Audit로 남긴다.

## 필수 Test

- Schema compatibility
- consumer contract
- upcaster
- DLQ replay
- 입력 Validation·권한·Data Scope·Masking Negative Test
- 해당 기능에 적용되는 중복·동시성·Timeout·응답 유실·부분 실패 Test
- 해당 기능의 Failure Injection 후 필요한 Retry·Restart·Reprocess·Reconcile·Rollback Test
- Oracle·PostgreSQL·MariaDB 또는 해당 Infra의 의미 동일성 Test
- Log·Metric·Trace·Audit에 Secret·개인정보 원문이 없는지 검사

## 운영·화면 확인

- ADM 메시지·DLQ에서 schema version·오류·재처리 확인
- 화면에는 해당 메뉴에 실제로 존재하는 조회 조건, 기본값, Column, 상세 Field, 상태, Button 활성 조건, 사유, 승인, 예상 Version, 대상별 결과만 Source와 일치하게 표시한다. 조회 전용 메뉴에 Command 항목을 만들지 않는다.
- 운영자가 정상화 완료를 판단할 수 있는 건수·금액·Version·Checksum·최종 상태·마지막 오류를 제공한다.
- 변경·외부연계 기능에서 응답 유실이 가능한 경우에만 요청을 다시 보내기 전에 Operation·거래·상대 상태를 조회하는 절차를 Browser 또는 API Test로 검증한다.

## 고객 매뉴얼·EDU 연결

- `01_개발자매뉴얼.md`의 해당 기능 장에 `EDU-DEV-44`를 연결한다.
- 선행 환경, 실행 명령, 입력값, 정상 출력, 오류 재현 명령, 복구 명령, ADM/BZA/Gateway 확인 위치를 그대로 따라 할 수 있게 작성한다.
- 교육 코드에서 고객 업무로 바꿀 Package·Table·API·Topic·파일명과 CPF 관리 영역을 구분한 전환표를 제공한다.

## 완료 조건

- `EDU-DEV-44`가 교육 Catalog·Source·Test·고객 매뉴얼에 동일 ID로 존재한다.
- 정상·Validation·권한·중복·동시성·Timeout·응답 유실·부분 실패·복구 Test가 통과한다.
- 요청·응답·상태·DB·이벤트·파일·운영 화면이 양방향으로 일치한다.
- 운영자가 다른 사람의 설명 없이 장애를 재현하고 정상화 판정을 할 수 있다.
- 실행하지 않은 Test는 성공으로 기록하지 않고, 실패·제약이 남으면 전체 완료로 보고하지 않는다.

## 금지 사항

- 메모리 Map·고정 JSON·정상 Case 한 건만으로 완료 처리
- 교육용 고객 업무를 `cpf-core`, `cpf-starters/common`, Starter, Runtime 제품 내부에 삽입
- Module-local DB Vendor Fallback SQL 또는 Vendor별 의미가 다른 Schema
- Timeout이나 응답 유실 뒤 실제 결과 확인 없이 무조건 재호출
- 비밀번호·Token·개인정보 원문을 Source·Log·Fixture·문서에 기록
- 매뉴얼 설명과 다른 임의 Package·API·상태·메뉴를 새로 생성

---

# 79. EDU-DEV-45 — 조회 모델·검색색인 Eventual Consistency

- 우선순위: **P0**
- 연결 매뉴얼: `01_개발자매뉴얼.md`
- Owner: `cpf-education`
- 구현 상태 목표: Source·SQL·API·Config·Test·운영 확인·매뉴얼 연결이 모두 완료

## 필요 근거

- 고객 매뉴얼에서 `조회 모델·검색색인 Eventual Consistency`을 설명하려면 정상 호출 예시만이 아니라 상태 변화와 장애 후 정상화까지 재현 가능한 실행 예제가 필요하다.
- 이 요구사항이 없으면 고객은 기능 가능 여부를 알더라도 실제 업무 적용 순서, 실패 판정, 재처리 금지 조건을 확인할 수 없다.
- 내부 구현 구조를 보여 주기 위한 예제가 아니라 고객 업무를 CPF로 만드는 방법과 운영 결과를 증명하는 예제로 구현한다.

## 고객 업무 시나리오

원장 등록 후 검색 색인이 지연돼도 상태를 설명하고 재색인으로 정상화한다.

## 구현 위치와 책임 경계

- 업무 Source: `com.cpf.education.edu.dev45` 또는 같은 의미의 기존 표준 Slice
- Test: Owner Module의 기존 Test Root 아래 `EDU-DEV-45` 식별 가능한 Class·Fixture
- SQL: 중앙 DB Vendor Pack의 기존 Install·Upgrade·Rollback·Verify·Seed 구조
- Config: 기존 Prefix·Profile·Secret 참조 방식을 유지
- 고객 업무 상태 변경과 원장 데이터는 고객 업무 Owner가 소유한다. ADM·BZA·Gateway·운영 Script가 업무 Table을 직접 수정하지 않는다.
- 공통 기능이 필요하면 기존 Public API·SPI를 우선 사용하고, 진입점이 없을 때만 실제 Consumer를 근거로 Owner Module에 확장한다.

## 입력·계약

- `aggregateId`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `eventOffset`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `indexVersion`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- 해당 기능에 필요한 Operation ID·업무 ID·Correlation ID·요청 Hash·사용자·권한 문맥을 저장·Log·Trace·Audit에서 연결한다.
- 요청·응답·Job Parameter·이벤트·파일 Layout 중 해당되는 계약을 OpenAPI 또는 Machine-readable Schema로 제공한다.

## 상태와 저장 근거

- 필수 상태 흐름: `SOURCE_COMMITTED → INDEX_PENDING → INDEXED/FAILED → REINDEXED`
- 상태마다 진입 조건, 허용 조치, 재시도 가능 여부, 최종 여부, 저장 위치와 Version 규칙을 정의한다.
- 결과 미확정·부분 성공이 가능한 경우 대상별 Attempt와 상대 상태 조회 근거를 별도 저장한다.

## 정상 처리 순서

1. 입력·권한·데이터 범위·기준 Version과 선행 상태를 검증한다.
2. 상태 변경·비동기 실행인 경우 Operation과 업무 식별자를 생성하거나 기존 멱등 요청을 조회한다. 조회 전용 기능은 조회 Context와 Cursor만 사용한다.
3. 상태 변경 기능은 고객 업무 Owner의 Application Use Case에서 Transaction 경계와 상태 전이를 수행하고, 조회 전용 기능은 일관된 Snapshot·정렬·범위 조건을 적용한다.
4. 외부 시스템·Kafka·파일·Batch·Gateway가 필요한 경우 Adapter 경계에서 Timeout·Retry·보안 정책을 적용한다.
5. 업무 결과와 Attempt·Audit·Log·Metric·Trace를 같은 식별자로 연결한다.
6. 고객 API 또는 운영 화면에서 최종 상태·건수·금액·Version·오류 0건을 확인한다.

## 예외·장애·경계 사례

- 이벤트 유실
- 중복
- 순서 역전
- 검색 Cluster 장애
- 전체 재색인 중 변경
- 상태 변경이 있는 경우 DB Commit 전 실패와 Commit 후 응답 유실을 구분한다.
- 같은 요청의 중복·동시 실행·순서 역전·늦은 응답을 포함한다.
- Retry 가능한 오류와 즉시 중단해야 하는 업무 오류를 구분한다.
- 대량·다중 대상 기능에서 부분 성공이 발생하면 성공 대상을 다시 실행하지 않고 실패 대상만 Reprocess·Reconcile한다.
- 복구·보상이 필요한 기능은 복구 실패와 보상 실패도 별도 상태·Attempt·Audit로 남긴다.

## 필수 Test

- Offset checkpoint
- idempotent indexing
- lag metric
- rebuild swap
- 입력 Validation·권한·Data Scope·Masking Negative Test
- 해당 기능에 적용되는 중복·동시성·Timeout·응답 유실·부분 실패 Test
- 해당 기능의 Failure Injection 후 필요한 Retry·Restart·Reprocess·Reconcile·Rollback Test
- Oracle·PostgreSQL·MariaDB 또는 해당 Infra의 의미 동일성 Test
- Log·Metric·Trace·Audit에 Secret·개인정보 원문이 없는지 검사

## 운영·화면 확인

- ADM 신뢰성·작업에서 lag·실패·재색인 결과 확인
- 화면에는 해당 메뉴에 실제로 존재하는 조회 조건, 기본값, Column, 상세 Field, 상태, Button 활성 조건, 사유, 승인, 예상 Version, 대상별 결과만 Source와 일치하게 표시한다. 조회 전용 메뉴에 Command 항목을 만들지 않는다.
- 운영자가 정상화 완료를 판단할 수 있는 건수·금액·Version·Checksum·최종 상태·마지막 오류를 제공한다.
- 변경·외부연계 기능에서 응답 유실이 가능한 경우에만 요청을 다시 보내기 전에 Operation·거래·상대 상태를 조회하는 절차를 Browser 또는 API Test로 검증한다.

## 고객 매뉴얼·EDU 연결

- `01_개발자매뉴얼.md`의 해당 기능 장에 `EDU-DEV-45`를 연결한다.
- 선행 환경, 실행 명령, 입력값, 정상 출력, 오류 재현 명령, 복구 명령, ADM/BZA/Gateway 확인 위치를 그대로 따라 할 수 있게 작성한다.
- 교육 코드에서 고객 업무로 바꿀 Package·Table·API·Topic·파일명과 CPF 관리 영역을 구분한 전환표를 제공한다.

## 완료 조건

- `EDU-DEV-45`가 교육 Catalog·Source·Test·고객 매뉴얼에 동일 ID로 존재한다.
- 정상·Validation·권한·중복·동시성·Timeout·응답 유실·부분 실패·복구 Test가 통과한다.
- 요청·응답·상태·DB·이벤트·파일·운영 화면이 양방향으로 일치한다.
- 운영자가 다른 사람의 설명 없이 장애를 재현하고 정상화 판정을 할 수 있다.
- 실행하지 않은 Test는 성공으로 기록하지 않고, 실패·제약이 남으면 전체 완료로 보고하지 않는다.

## 금지 사항

- 메모리 Map·고정 JSON·정상 Case 한 건만으로 완료 처리
- 교육용 고객 업무를 `cpf-core`, `cpf-starters/common`, Starter, Runtime 제품 내부에 삽입
- Module-local DB Vendor Fallback SQL 또는 Vendor별 의미가 다른 Schema
- Timeout이나 응답 유실 뒤 실제 결과 확인 없이 무조건 재호출
- 비밀번호·Token·개인정보 원문을 Source·Log·Fixture·문서에 기록
- 매뉴얼 설명과 다른 임의 Package·API·상태·메뉴를 새로 생성

---

# 80. EDU-BAT-11 — 조건 분기·다단계 Job Flow

- 우선순위: **P0**
- 연결 매뉴얼: `02_배치개발매뉴얼.md`
- Owner: `cpf-education + cpf-batch public contract`
- 구현 상태 목표: Source·SQL·API·Config·Test·운영 확인·매뉴얼 연결이 모두 완료

## 필요 근거

- 고객 매뉴얼에서 `조건 분기·다단계 Job Flow`을 설명하려면 정상 호출 예시만이 아니라 상태 변화와 장애 후 정상화까지 재현 가능한 실행 예제가 필요하다.
- 이 요구사항이 없으면 고객은 기능 가능 여부를 알더라도 실제 업무 적용 순서, 실패 판정, 재처리 금지 조건을 확인할 수 없다.
- 내부 구현 구조를 보여 주기 위한 예제가 아니라 고객 업무를 CPF로 만드는 방법과 운영 결과를 증명하는 예제로 구현한다.

## 고객 업무 시나리오

정산 Job이 사전검증 성공 시 산출·전송을 수행하고 오류율이 높으면 후속 Step을 중단한다.

## 구현 위치와 책임 경계

- 업무 Source: `com.cpf.education.edu.bat11` 또는 같은 의미의 기존 표준 Slice
- Test: Owner Module의 기존 Test Root 아래 `EDU-BAT-11` 식별 가능한 Class·Fixture
- SQL: 중앙 DB Vendor Pack의 기존 Install·Upgrade·Rollback·Verify·Seed 구조
- Config: 기존 Prefix·Profile·Secret 참조 방식을 유지
- 고객 업무 상태 변경과 원장 데이터는 고객 업무 Owner가 소유한다. ADM·BZA·Gateway·운영 Script가 업무 Table을 직접 수정하지 않는다.
- 공통 기능이 필요하면 기존 Public API·SPI를 우선 사용하고, 진입점이 없을 때만 실제 Consumer를 근거로 Owner Module에 확장한다.

## 입력·계약

- `businessDate`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `mode`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `threshold`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- 해당 기능에 필요한 Operation ID·업무 ID·Correlation ID·요청 Hash·사용자·권한 문맥을 저장·Log·Trace·Audit에서 연결한다.
- 요청·응답·Job Parameter·이벤트·파일 Layout 중 해당되는 계약을 OpenAPI 또는 Machine-readable Schema로 제공한다.

## 상태와 저장 근거

- 필수 상태 흐름: `STARTING → VALIDATING → PROCESSING → SENDING → COMPLETED/STOPPED`
- 상태마다 진입 조건, 허용 조치, 재시도 가능 여부, 최종 여부, 저장 위치와 Version 규칙을 정의한다.
- 결과 미확정·부분 성공이 가능한 경우 대상별 Attempt와 상대 상태 조회 근거를 별도 저장한다.

## 정상 처리 순서

1. 입력·권한·데이터 범위·기준 Version과 선행 상태를 검증한다.
2. 상태 변경·비동기 실행인 경우 Operation과 업무 식별자를 생성하거나 기존 멱등 요청을 조회한다. 조회 전용 기능은 조회 Context와 Cursor만 사용한다.
3. 상태 변경 기능은 고객 업무 Owner의 Application Use Case에서 Transaction 경계와 상태 전이를 수행하고, 조회 전용 기능은 일관된 Snapshot·정렬·범위 조건을 적용한다.
4. 외부 시스템·Kafka·파일·Batch·Gateway가 필요한 경우 Adapter 경계에서 Timeout·Retry·보안 정책을 적용한다.
5. 업무 결과와 Attempt·Audit·Log·Metric·Trace를 같은 식별자로 연결한다.
6. 고객 API 또는 운영 화면에서 최종 상태·건수·금액·Version·오류 0건을 확인한다.

## 예외·장애·경계 사례

- 검증 실패
- Step ExitStatus 분기
- 재시작 시 완료 Step 재실행 방지
- 상태 변경이 있는 경우 DB Commit 전 실패와 Commit 후 응답 유실을 구분한다.
- 같은 요청의 중복·동시 실행·순서 역전·늦은 응답을 포함한다.
- Retry 가능한 오류와 즉시 중단해야 하는 업무 오류를 구분한다.
- 대량·다중 대상 기능에서 부분 성공이 발생하면 성공 대상을 다시 실행하지 않고 실패 대상만 Reprocess·Reconcile한다.
- 복구·보상이 필요한 기능은 복구 실패와 보상 실패도 별도 상태·Attempt·Audit로 남긴다.

## 필수 Test

- Flow transition
- restart point
- listener
- JobRepository
- 입력 Validation·권한·Data Scope·Masking Negative Test
- 해당 기능에 적용되는 중복·동시성·Timeout·응답 유실·부분 실패 Test
- 해당 기능의 Failure Injection 후 필요한 Retry·Restart·Reprocess·Reconcile·Rollback Test
- Oracle·PostgreSQL·MariaDB 또는 해당 Infra의 의미 동일성 Test
- Log·Metric·Trace·Audit에 Secret·개인정보 원문이 없는지 검사

## 운영·화면 확인

- ADM 실행 상세에서 Step 순서·ExitStatus·재시작 위치 확인
- 화면에는 해당 메뉴에 실제로 존재하는 조회 조건, 기본값, Column, 상세 Field, 상태, Button 활성 조건, 사유, 승인, 예상 Version, 대상별 결과만 Source와 일치하게 표시한다. 조회 전용 메뉴에 Command 항목을 만들지 않는다.
- 운영자가 정상화 완료를 판단할 수 있는 건수·금액·Version·Checksum·최종 상태·마지막 오류를 제공한다.
- 변경·외부연계 기능에서 응답 유실이 가능한 경우에만 요청을 다시 보내기 전에 Operation·거래·상대 상태를 조회하는 절차를 Browser 또는 API Test로 검증한다.

## 고객 매뉴얼·EDU 연결

- `02_배치개발매뉴얼.md`의 해당 기능 장에 `EDU-BAT-11`를 연결한다.
- 선행 환경, 실행 명령, 입력값, 정상 출력, 오류 재현 명령, 복구 명령, ADM/BZA/Gateway 확인 위치를 그대로 따라 할 수 있게 작성한다.
- 교육 코드에서 고객 업무로 바꿀 Package·Table·API·Topic·파일명과 CPF 관리 영역을 구분한 전환표를 제공한다.

## 완료 조건

- `EDU-BAT-11`가 교육 Catalog·Source·Test·고객 매뉴얼에 동일 ID로 존재한다.
- 정상·Validation·권한·중복·동시성·Timeout·응답 유실·부분 실패·복구 Test가 통과한다.
- 요청·응답·상태·DB·이벤트·파일·운영 화면이 양방향으로 일치한다.
- 운영자가 다른 사람의 설명 없이 장애를 재현하고 정상화 판정을 할 수 있다.
- 실행하지 않은 Test는 성공으로 기록하지 않고, 실패·제약이 남으면 전체 완료로 보고하지 않는다.

## 금지 사항

- 메모리 Map·고정 JSON·정상 Case 한 건만으로 완료 처리
- 교육용 고객 업무를 `cpf-core`, `cpf-starters/common`, Starter, Runtime 제품 내부에 삽입
- Module-local DB Vendor Fallback SQL 또는 Vendor별 의미가 다른 Schema
- Timeout이나 응답 유실 뒤 실제 결과 확인 없이 무조건 재호출
- 비밀번호·Token·개인정보 원문을 Source·Log·Fixture·문서에 기록
- 매뉴얼 설명과 다른 임의 Package·API·상태·메뉴를 새로 생성

---

# 81. EDU-BAT-12 — Retry·Skip·No-Skip 예외 분류

- 우선순위: **P0**
- 연결 매뉴얼: `02_배치개발매뉴얼.md`
- Owner: `cpf-education + cpf-batch public contract`
- 구현 상태 목표: Source·SQL·API·Config·Test·운영 확인·매뉴얼 연결이 모두 완료

## 필요 근거

- 고객 매뉴얼에서 `Retry·Skip·No-Skip 예외 분류`을 설명하려면 정상 호출 예시만이 아니라 상태 변화와 장애 후 정상화까지 재현 가능한 실행 예제가 필요하다.
- 이 요구사항이 없으면 고객은 기능 가능 여부를 알더라도 실제 업무 적용 순서, 실패 판정, 재처리 금지 조건을 확인할 수 없다.
- 내부 구현 구조를 보여 주기 위한 예제가 아니라 고객 업무를 CPF로 만드는 방법과 운영 결과를 증명하는 예제로 구현한다.

## 고객 업무 시나리오

일시 DB Deadlock은 재시도하고 형식 오류 행은 Skip하며 원장 오류는 Job을 실패시킨다.

## 구현 위치와 책임 경계

- 업무 Source: `com.cpf.education.edu.bat12` 또는 같은 의미의 기존 표준 Slice
- Test: Owner Module의 기존 Test Root 아래 `EDU-BAT-12` 식별 가능한 Class·Fixture
- SQL: 중앙 DB Vendor Pack의 기존 Install·Upgrade·Rollback·Verify·Seed 구조
- Config: 기존 Prefix·Profile·Secret 참조 방식을 유지
- 고객 업무 상태 변경과 원장 데이터는 고객 업무 Owner가 소유한다. ADM·BZA·Gateway·운영 Script가 업무 Table을 직접 수정하지 않는다.
- 공통 기능이 필요하면 기존 Public API·SPI를 우선 사용하고, 진입점이 없을 때만 실제 Consumer를 근거로 Owner Module에 확장한다.

## 입력·계약

- `retryLimit`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `skipLimit`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `errorPolicyVersion`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- 해당 기능에 필요한 Operation ID·업무 ID·Correlation ID·요청 Hash·사용자·권한 문맥을 저장·Log·Trace·Audit에서 연결한다.
- 요청·응답·Job Parameter·이벤트·파일 Layout 중 해당되는 계약을 OpenAPI 또는 Machine-readable Schema로 제공한다.

## 상태와 저장 근거

- 필수 상태 흐름: `RUNNING → RETRYING/SKIPPING → COMPLETED/FAILED`
- 상태마다 진입 조건, 허용 조치, 재시도 가능 여부, 최종 여부, 저장 위치와 Version 규칙을 정의한다.
- 결과 미확정·부분 성공이 가능한 경우 대상별 Attempt와 상대 상태 조회 근거를 별도 저장한다.

## 정상 처리 순서

1. 입력·권한·데이터 범위·기준 Version과 선행 상태를 검증한다.
2. 상태 변경·비동기 실행인 경우 Operation과 업무 식별자를 생성하거나 기존 멱등 요청을 조회한다. 조회 전용 기능은 조회 Context와 Cursor만 사용한다.
3. 상태 변경 기능은 고객 업무 Owner의 Application Use Case에서 Transaction 경계와 상태 전이를 수행하고, 조회 전용 기능은 일관된 Snapshot·정렬·범위 조건을 적용한다.
4. 외부 시스템·Kafka·파일·Batch·Gateway가 필요한 경우 Adapter 경계에서 Timeout·Retry·보안 정책을 적용한다.
5. 업무 결과와 Attempt·Audit·Log·Metric·Trace를 같은 식별자로 연결한다.
6. 고객 API 또는 운영 화면에서 최종 상태·건수·금액·Version·오류 0건을 확인한다.

## 예외·장애·경계 사례

- Retry exhaustion
- skip limit 초과
- 같은 item 반복
- rollback classifier 오류
- 상태 변경이 있는 경우 DB Commit 전 실패와 Commit 후 응답 유실을 구분한다.
- 같은 요청의 중복·동시 실행·순서 역전·늦은 응답을 포함한다.
- Retry 가능한 오류와 즉시 중단해야 하는 업무 오류를 구분한다.
- 대량·다중 대상 기능에서 부분 성공이 발생하면 성공 대상을 다시 실행하지 않고 실패 대상만 Reprocess·Reconcile한다.
- 복구·보상이 필요한 기능은 복구 실패와 보상 실패도 별도 상태·Attempt·Audit로 남긴다.

## 필수 Test

- Exception classifier
- transaction rollback
- skip listener
- audit file
- 입력 Validation·권한·Data Scope·Masking Negative Test
- 해당 기능에 적용되는 중복·동시성·Timeout·응답 유실·부분 실패 Test
- 해당 기능의 Failure Injection 후 필요한 Retry·Restart·Reprocess·Reconcile·Rollback Test
- Oracle·PostgreSQL·MariaDB 또는 해당 Infra의 의미 동일성 Test
- Log·Metric·Trace·Audit에 Secret·개인정보 원문이 없는지 검사

## 운영·화면 확인

- ADM Step 통계에서 read/write/skip/retry와 오류 샘플 확인
- 화면에는 해당 메뉴에 실제로 존재하는 조회 조건, 기본값, Column, 상세 Field, 상태, Button 활성 조건, 사유, 승인, 예상 Version, 대상별 결과만 Source와 일치하게 표시한다. 조회 전용 메뉴에 Command 항목을 만들지 않는다.
- 운영자가 정상화 완료를 판단할 수 있는 건수·금액·Version·Checksum·최종 상태·마지막 오류를 제공한다.
- 변경·외부연계 기능에서 응답 유실이 가능한 경우에만 요청을 다시 보내기 전에 Operation·거래·상대 상태를 조회하는 절차를 Browser 또는 API Test로 검증한다.

## 고객 매뉴얼·EDU 연결

- `02_배치개발매뉴얼.md`의 해당 기능 장에 `EDU-BAT-12`를 연결한다.
- 선행 환경, 실행 명령, 입력값, 정상 출력, 오류 재현 명령, 복구 명령, ADM/BZA/Gateway 확인 위치를 그대로 따라 할 수 있게 작성한다.
- 교육 코드에서 고객 업무로 바꿀 Package·Table·API·Topic·파일명과 CPF 관리 영역을 구분한 전환표를 제공한다.

## 완료 조건

- `EDU-BAT-12`가 교육 Catalog·Source·Test·고객 매뉴얼에 동일 ID로 존재한다.
- 정상·Validation·권한·중복·동시성·Timeout·응답 유실·부분 실패·복구 Test가 통과한다.
- 요청·응답·상태·DB·이벤트·파일·운영 화면이 양방향으로 일치한다.
- 운영자가 다른 사람의 설명 없이 장애를 재현하고 정상화 판정을 할 수 있다.
- 실행하지 않은 Test는 성공으로 기록하지 않고, 실패·제약이 남으면 전체 완료로 보고하지 않는다.

## 금지 사항

- 메모리 Map·고정 JSON·정상 Case 한 건만으로 완료 처리
- 교육용 고객 업무를 `cpf-core`, `cpf-starters/common`, Starter, Runtime 제품 내부에 삽입
- Module-local DB Vendor Fallback SQL 또는 Vendor별 의미가 다른 Schema
- Timeout이나 응답 유실 뒤 실제 결과 확인 없이 무조건 재호출
- 비밀번호·Token·개인정보 원문을 Source·Log·Fixture·문서에 기록
- 매뉴얼 설명과 다른 임의 Package·API·상태·메뉴를 새로 생성

---

# 82. EDU-BAT-13 — Writer Commit 장애 후 Checkpoint 재시작

- 우선순위: **P0**
- 연결 매뉴얼: `02_배치개발매뉴얼.md`
- Owner: `cpf-education + cpf-batch public contract`
- 구현 상태 목표: Source·SQL·API·Config·Test·운영 확인·매뉴얼 연결이 모두 완료

## 필요 근거

- 고객 매뉴얼에서 `Writer Commit 장애 후 Checkpoint 재시작`을 설명하려면 정상 호출 예시만이 아니라 상태 변화와 장애 후 정상화까지 재현 가능한 실행 예제가 필요하다.
- 이 요구사항이 없으면 고객은 기능 가능 여부를 알더라도 실제 업무 적용 순서, 실패 판정, 재처리 금지 조건을 확인할 수 없다.
- 내부 구현 구조를 보여 주기 위한 예제가 아니라 고객 업무를 CPF로 만드는 방법과 운영 결과를 증명하는 예제로 구현한다.

## 고객 업무 시나리오

1,000건 단위 Chunk 중 3번째 Commit에서 장애가 나고 재시작 시 완료 Chunk를 중복 반영하지 않는다.

## 구현 위치와 책임 경계

- 업무 Source: `com.cpf.education.edu.bat13` 또는 같은 의미의 기존 표준 Slice
- Test: Owner Module의 기존 Test Root 아래 `EDU-BAT-13` 식별 가능한 Class·Fixture
- SQL: 중앙 DB Vendor Pack의 기존 Install·Upgrade·Rollback·Verify·Seed 구조
- Config: 기존 Prefix·Profile·Secret 참조 방식을 유지
- 고객 업무 상태 변경과 원장 데이터는 고객 업무 Owner가 소유한다. ADM·BZA·Gateway·운영 Script가 업무 Table을 직접 수정하지 않는다.
- 공통 기능이 필요하면 기존 Public API·SPI를 우선 사용하고, 진입점이 없을 때만 실제 Consumer를 근거로 Owner Module에 확장한다.

## 입력·계약

- `chunkSize`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `failureAtCommit`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `runId`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- 해당 기능에 필요한 Operation ID·업무 ID·Correlation ID·요청 Hash·사용자·권한 문맥을 저장·Log·Trace·Audit에서 연결한다.
- 요청·응답·Job Parameter·이벤트·파일 Layout 중 해당되는 계약을 OpenAPI 또는 Machine-readable Schema로 제공한다.

## 상태와 저장 근거

- 필수 상태 흐름: `RUNNING → FAILED_AT_CHECKPOINT → RESTARTED → COMPLETED`
- 상태마다 진입 조건, 허용 조치, 재시도 가능 여부, 최종 여부, 저장 위치와 Version 규칙을 정의한다.
- 결과 미확정·부분 성공이 가능한 경우 대상별 Attempt와 상대 상태 조회 근거를 별도 저장한다.

## 정상 처리 순서

1. 입력·권한·데이터 범위·기준 Version과 선행 상태를 검증한다.
2. 상태 변경·비동기 실행인 경우 Operation과 업무 식별자를 생성하거나 기존 멱등 요청을 조회한다. 조회 전용 기능은 조회 Context와 Cursor만 사용한다.
3. 상태 변경 기능은 고객 업무 Owner의 Application Use Case에서 Transaction 경계와 상태 전이를 수행하고, 조회 전용 기능은 일관된 Snapshot·정렬·범위 조건을 적용한다.
4. 외부 시스템·Kafka·파일·Batch·Gateway가 필요한 경우 Adapter 경계에서 Timeout·Retry·보안 정책을 적용한다.
5. 업무 결과와 Attempt·Audit·Log·Metric·Trace를 같은 식별자로 연결한다.
6. 고객 API 또는 운영 화면에서 최종 상태·건수·금액·Version·오류 0건을 확인한다.

## 예외·장애·경계 사례

- DB Commit 성공 후 metadata 실패
- metadata 성공 후 업무 DB 실패
- 중복 writer
- 상태 변경이 있는 경우 DB Commit 전 실패와 Commit 후 응답 유실을 구분한다.
- 같은 요청의 중복·동시 실행·순서 역전·늦은 응답을 포함한다.
- Retry 가능한 오류와 즉시 중단해야 하는 업무 오류를 구분한다.
- 대량·다중 대상 기능에서 부분 성공이 발생하면 성공 대상을 다시 실행하지 않고 실패 대상만 Reprocess·Reconcile한다.
- 복구·보상이 필요한 기능은 복구 실패와 보상 실패도 별도 상태·Attempt·Audit로 남긴다.

## 필수 Test

- Checkpoint consistency
- idempotent writer
- restart execution context
- 입력 Validation·권한·Data Scope·Masking Negative Test
- 해당 기능에 적용되는 중복·동시성·Timeout·응답 유실·부분 실패 Test
- 해당 기능의 Failure Injection 후 필요한 Retry·Restart·Reprocess·Reconcile·Rollback Test
- Oracle·PostgreSQL·MariaDB 또는 해당 Infra의 의미 동일성 Test
- Log·Metric·Trace·Audit에 Secret·개인정보 원문이 없는지 검사

## 운영·화면 확인

- ADM Batch Recovery에서 마지막 Commit·재시작 건수 확인
- 화면에는 해당 메뉴에 실제로 존재하는 조회 조건, 기본값, Column, 상세 Field, 상태, Button 활성 조건, 사유, 승인, 예상 Version, 대상별 결과만 Source와 일치하게 표시한다. 조회 전용 메뉴에 Command 항목을 만들지 않는다.
- 운영자가 정상화 완료를 판단할 수 있는 건수·금액·Version·Checksum·최종 상태·마지막 오류를 제공한다.
- 변경·외부연계 기능에서 응답 유실이 가능한 경우에만 요청을 다시 보내기 전에 Operation·거래·상대 상태를 조회하는 절차를 Browser 또는 API Test로 검증한다.

## 고객 매뉴얼·EDU 연결

- `02_배치개발매뉴얼.md`의 해당 기능 장에 `EDU-BAT-13`를 연결한다.
- 선행 환경, 실행 명령, 입력값, 정상 출력, 오류 재현 명령, 복구 명령, ADM/BZA/Gateway 확인 위치를 그대로 따라 할 수 있게 작성한다.
- 교육 코드에서 고객 업무로 바꿀 Package·Table·API·Topic·파일명과 CPF 관리 영역을 구분한 전환표를 제공한다.

## 완료 조건

- `EDU-BAT-13`가 교육 Catalog·Source·Test·고객 매뉴얼에 동일 ID로 존재한다.
- 정상·Validation·권한·중복·동시성·Timeout·응답 유실·부분 실패·복구 Test가 통과한다.
- 요청·응답·상태·DB·이벤트·파일·운영 화면이 양방향으로 일치한다.
- 운영자가 다른 사람의 설명 없이 장애를 재현하고 정상화 판정을 할 수 있다.
- 실행하지 않은 Test는 성공으로 기록하지 않고, 실패·제약이 남으면 전체 완료로 보고하지 않는다.

## 금지 사항

- 메모리 Map·고정 JSON·정상 Case 한 건만으로 완료 처리
- 교육용 고객 업무를 `cpf-core`, `cpf-starters/common`, Starter, Runtime 제품 내부에 삽입
- Module-local DB Vendor Fallback SQL 또는 Vendor별 의미가 다른 Schema
- Timeout이나 응답 유실 뒤 실제 결과 확인 없이 무조건 재호출
- 비밀번호·Token·개인정보 원문을 Source·Log·Fixture·문서에 기록
- 매뉴얼 설명과 다른 임의 Package·API·상태·메뉴를 새로 생성

---

# 83. EDU-BAT-14 — JobParameter 식별·중복 실행·새 Instance

- 우선순위: **P0**
- 연결 매뉴얼: `02_배치개발매뉴얼.md`
- Owner: `cpf-education + cpf-batch public contract`
- 구현 상태 목표: Source·SQL·API·Config·Test·운영 확인·매뉴얼 연결이 모두 완료

## 필요 근거

- 고객 매뉴얼에서 `JobParameter 식별·중복 실행·새 Instance`을 설명하려면 정상 호출 예시만이 아니라 상태 변화와 장애 후 정상화까지 재현 가능한 실행 예제가 필요하다.
- 이 요구사항이 없으면 고객은 기능 가능 여부를 알더라도 실제 업무 적용 순서, 실패 판정, 재처리 금지 조건을 확인할 수 없다.
- 내부 구현 구조를 보여 주기 위한 예제가 아니라 고객 업무를 CPF로 만드는 방법과 운영 결과를 증명하는 예제로 구현한다.

## 고객 업무 시나리오

같은 업무일자·버전은 같은 JobInstance이며 강제 재산출은 별도 runType으로 구분한다.

## 구현 위치와 책임 경계

- 업무 Source: `com.cpf.education.edu.bat14` 또는 같은 의미의 기존 표준 Slice
- Test: Owner Module의 기존 Test Root 아래 `EDU-BAT-14` 식별 가능한 Class·Fixture
- SQL: 중앙 DB Vendor Pack의 기존 Install·Upgrade·Rollback·Verify·Seed 구조
- Config: 기존 Prefix·Profile·Secret 참조 방식을 유지
- 고객 업무 상태 변경과 원장 데이터는 고객 업무 Owner가 소유한다. ADM·BZA·Gateway·운영 Script가 업무 Table을 직접 수정하지 않는다.
- 공통 기능이 필요하면 기존 Public API·SPI를 우선 사용하고, 진입점이 없을 때만 실제 Consumer를 근거로 Owner Module에 확장한다.

## 입력·계약

- `businessDate`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `jobVersion`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `runType`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `requestId`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- 해당 기능에 필요한 Operation ID·업무 ID·Correlation ID·요청 Hash·사용자·권한 문맥을 저장·Log·Trace·Audit에서 연결한다.
- 요청·응답·Job Parameter·이벤트·파일 Layout 중 해당되는 계약을 OpenAPI 또는 Machine-readable Schema로 제공한다.

## 상태와 저장 근거

- 필수 상태 흐름: `NEW_INSTANCE/RUNNING/COMPLETED; DUPLICATE_REJECTED`
- 상태마다 진입 조건, 허용 조치, 재시도 가능 여부, 최종 여부, 저장 위치와 Version 규칙을 정의한다.
- 결과 미확정·부분 성공이 가능한 경우 대상별 Attempt와 상대 상태 조회 근거를 별도 저장한다.

## 정상 처리 순서

1. 입력·권한·데이터 범위·기준 Version과 선행 상태를 검증한다.
2. 상태 변경·비동기 실행인 경우 Operation과 업무 식별자를 생성하거나 기존 멱등 요청을 조회한다. 조회 전용 기능은 조회 Context와 Cursor만 사용한다.
3. 상태 변경 기능은 고객 업무 Owner의 Application Use Case에서 Transaction 경계와 상태 전이를 수행하고, 조회 전용 기능은 일관된 Snapshot·정렬·범위 조건을 적용한다.
4. 외부 시스템·Kafka·파일·Batch·Gateway가 필요한 경우 Adapter 경계에서 Timeout·Retry·보안 정책을 적용한다.
5. 업무 결과와 Attempt·Audit·Log·Metric·Trace를 같은 식별자로 연결한다.
6. 고객 API 또는 운영 화면에서 최종 상태·건수·금액·Version·오류 0건을 확인한다.

## 예외·장애·경계 사례

- 식별 Parameter 누락
- 순서 차이
- 동일 요청 동시 Launch
- 완료 Job 재실행
- 상태 변경이 있는 경우 DB Commit 전 실패와 Commit 후 응답 유실을 구분한다.
- 같은 요청의 중복·동시 실행·순서 역전·늦은 응답을 포함한다.
- Retry 가능한 오류와 즉시 중단해야 하는 업무 오류를 구분한다.
- 대량·다중 대상 기능에서 부분 성공이 발생하면 성공 대상을 다시 실행하지 않고 실패 대상만 Reprocess·Reconcile한다.
- 복구·보상이 필요한 기능은 복구 실패와 보상 실패도 별도 상태·Attempt·Audit로 남긴다.

## 필수 Test

- JobKey normalization
- unique constraint
- launch race
- 입력 Validation·권한·Data Scope·Masking Negative Test
- 해당 기능에 적용되는 중복·동시성·Timeout·응답 유실·부분 실패 Test
- 해당 기능의 Failure Injection 후 필요한 Retry·Restart·Reprocess·Reconcile·Rollback Test
- Oracle·PostgreSQL·MariaDB 또는 해당 Infra의 의미 동일성 Test
- Log·Metric·Trace·Audit에 Secret·개인정보 원문이 없는지 검사

## 운영·화면 확인

- ADM Job Instance에서 Parameter·Instance·Execution 관계 확인
- 화면에는 해당 메뉴에 실제로 존재하는 조회 조건, 기본값, Column, 상세 Field, 상태, Button 활성 조건, 사유, 승인, 예상 Version, 대상별 결과만 Source와 일치하게 표시한다. 조회 전용 메뉴에 Command 항목을 만들지 않는다.
- 운영자가 정상화 완료를 판단할 수 있는 건수·금액·Version·Checksum·최종 상태·마지막 오류를 제공한다.
- 변경·외부연계 기능에서 응답 유실이 가능한 경우에만 요청을 다시 보내기 전에 Operation·거래·상대 상태를 조회하는 절차를 Browser 또는 API Test로 검증한다.

## 고객 매뉴얼·EDU 연결

- `02_배치개발매뉴얼.md`의 해당 기능 장에 `EDU-BAT-14`를 연결한다.
- 선행 환경, 실행 명령, 입력값, 정상 출력, 오류 재현 명령, 복구 명령, ADM/BZA/Gateway 확인 위치를 그대로 따라 할 수 있게 작성한다.
- 교육 코드에서 고객 업무로 바꿀 Package·Table·API·Topic·파일명과 CPF 관리 영역을 구분한 전환표를 제공한다.

## 완료 조건

- `EDU-BAT-14`가 교육 Catalog·Source·Test·고객 매뉴얼에 동일 ID로 존재한다.
- 정상·Validation·권한·중복·동시성·Timeout·응답 유실·부분 실패·복구 Test가 통과한다.
- 요청·응답·상태·DB·이벤트·파일·운영 화면이 양방향으로 일치한다.
- 운영자가 다른 사람의 설명 없이 장애를 재현하고 정상화 판정을 할 수 있다.
- 실행하지 않은 Test는 성공으로 기록하지 않고, 실패·제약이 남으면 전체 완료로 보고하지 않는다.

## 금지 사항

- 메모리 Map·고정 JSON·정상 Case 한 건만으로 완료 처리
- 교육용 고객 업무를 `cpf-core`, `cpf-starters/common`, Starter, Runtime 제품 내부에 삽입
- Module-local DB Vendor Fallback SQL 또는 Vendor별 의미가 다른 Schema
- Timeout이나 응답 유실 뒤 실제 결과 확인 없이 무조건 재호출
- 비밀번호·Token·개인정보 원문을 Source·Log·Fixture·문서에 기록
- 매뉴얼 설명과 다른 임의 Package·API·상태·메뉴를 새로 생성

---

# 84. EDU-BAT-15 — 지연 도착 데이터·Backfill·재산출

- 우선순위: **P0**
- 연결 매뉴얼: `02_배치개발매뉴얼.md`
- Owner: `cpf-education + cpf-batch public contract`
- 구현 상태 목표: Source·SQL·API·Config·Test·운영 확인·매뉴얼 연결이 모두 완료

## 필요 근거

- 고객 매뉴얼에서 `지연 도착 데이터·Backfill·재산출`을 설명하려면 정상 호출 예시만이 아니라 상태 변화와 장애 후 정상화까지 재현 가능한 실행 예제가 필요하다.
- 이 요구사항이 없으면 고객은 기능 가능 여부를 알더라도 실제 업무 적용 순서, 실패 판정, 재처리 금지 조건을 확인할 수 없다.
- 내부 구현 구조를 보여 주기 위한 예제가 아니라 고객 업무를 CPF로 만드는 방법과 운영 결과를 증명하는 예제로 구현한다.

## 고객 업무 시나리오

월마감 후 늦게 도착한 거래를 별도 Backfill로 반영하고 기존 결과와 차이를 대사한다.

## 구현 위치와 책임 경계

- 업무 Source: `com.cpf.education.edu.bat15` 또는 같은 의미의 기존 표준 Slice
- Test: Owner Module의 기존 Test Root 아래 `EDU-BAT-15` 식별 가능한 Class·Fixture
- SQL: 중앙 DB Vendor Pack의 기존 Install·Upgrade·Rollback·Verify·Seed 구조
- Config: 기존 Prefix·Profile·Secret 참조 방식을 유지
- 고객 업무 상태 변경과 원장 데이터는 고객 업무 Owner가 소유한다. ADM·BZA·Gateway·운영 Script가 업무 Table을 직접 수정하지 않는다.
- 공통 기능이 필요하면 기존 Public API·SPI를 우선 사용하고, 진입점이 없을 때만 실제 Consumer를 근거로 Owner Module에 확장한다.

## 입력·계약

- `targetPeriod`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `cutoffVersion`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `backfillFrom`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `reason`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- 해당 기능에 필요한 Operation ID·업무 ID·Correlation ID·요청 Hash·사용자·권한 문맥을 저장·Log·Trace·Audit에서 연결한다.
- 요청·응답·Job Parameter·이벤트·파일 Layout 중 해당되는 계약을 OpenAPI 또는 Machine-readable Schema로 제공한다.

## 상태와 저장 근거

- 필수 상태 흐름: `CLOSED → BACKFILL_REQUESTED → RECOMPUTED → RECONCILED`
- 상태마다 진입 조건, 허용 조치, 재시도 가능 여부, 최종 여부, 저장 위치와 Version 규칙을 정의한다.
- 결과 미확정·부분 성공이 가능한 경우 대상별 Attempt와 상대 상태 조회 근거를 별도 저장한다.

## 정상 처리 순서

1. 입력·권한·데이터 범위·기준 Version과 선행 상태를 검증한다.
2. 상태 변경·비동기 실행인 경우 Operation과 업무 식별자를 생성하거나 기존 멱등 요청을 조회한다. 조회 전용 기능은 조회 Context와 Cursor만 사용한다.
3. 상태 변경 기능은 고객 업무 Owner의 Application Use Case에서 Transaction 경계와 상태 전이를 수행하고, 조회 전용 기능은 일관된 Snapshot·정렬·범위 조건을 적용한다.
4. 외부 시스템·Kafka·파일·Batch·Gateway가 필요한 경우 Adapter 경계에서 Timeout·Retry·보안 정책을 적용한다.
5. 업무 결과와 Attempt·Audit·Log·Metric·Trace를 같은 식별자로 연결한다.
6. 고객 API 또는 운영 화면에서 최종 상태·건수·금액·Version·오류 0건을 확인한다.

## 예외·장애·경계 사례

- 중복 늦은 데이터
- 이미 지급된 결과
- 소급 정책
- 부분 기간
- 상태 변경이 있는 경우 DB Commit 전 실패와 Commit 후 응답 유실을 구분한다.
- 같은 요청의 중복·동시 실행·순서 역전·늦은 응답을 포함한다.
- Retry 가능한 오류와 즉시 중단해야 하는 업무 오류를 구분한다.
- 대량·다중 대상 기능에서 부분 성공이 발생하면 성공 대상을 다시 실행하지 않고 실패 대상만 Reprocess·Reconcile한다.
- 복구·보상이 필요한 기능은 복구 실패와 보상 실패도 별도 상태·Attempt·Audit로 남긴다.

## 필수 Test

- Delta query
- snapshot version
- compensation output
- audit
- 입력 Validation·권한·Data Scope·Masking Negative Test
- 해당 기능에 적용되는 중복·동시성·Timeout·응답 유실·부분 실패 Test
- 해당 기능의 Failure Injection 후 필요한 Retry·Restart·Reprocess·Reconcile·Rollback Test
- Oracle·PostgreSQL·MariaDB 또는 해당 Infra의 의미 동일성 Test
- Log·Metric·Trace·Audit에 Secret·개인정보 원문이 없는지 검사

## 운영·화면 확인

- ADM Center-Cut/Execution에서 원결과·추가분·차이 확인
- 화면에는 해당 메뉴에 실제로 존재하는 조회 조건, 기본값, Column, 상세 Field, 상태, Button 활성 조건, 사유, 승인, 예상 Version, 대상별 결과만 Source와 일치하게 표시한다. 조회 전용 메뉴에 Command 항목을 만들지 않는다.
- 운영자가 정상화 완료를 판단할 수 있는 건수·금액·Version·Checksum·최종 상태·마지막 오류를 제공한다.
- 변경·외부연계 기능에서 응답 유실이 가능한 경우에만 요청을 다시 보내기 전에 Operation·거래·상대 상태를 조회하는 절차를 Browser 또는 API Test로 검증한다.

## 고객 매뉴얼·EDU 연결

- `02_배치개발매뉴얼.md`의 해당 기능 장에 `EDU-BAT-15`를 연결한다.
- 선행 환경, 실행 명령, 입력값, 정상 출력, 오류 재현 명령, 복구 명령, ADM/BZA/Gateway 확인 위치를 그대로 따라 할 수 있게 작성한다.
- 교육 코드에서 고객 업무로 바꿀 Package·Table·API·Topic·파일명과 CPF 관리 영역을 구분한 전환표를 제공한다.

## 완료 조건

- `EDU-BAT-15`가 교육 Catalog·Source·Test·고객 매뉴얼에 동일 ID로 존재한다.
- 정상·Validation·권한·중복·동시성·Timeout·응답 유실·부분 실패·복구 Test가 통과한다.
- 요청·응답·상태·DB·이벤트·파일·운영 화면이 양방향으로 일치한다.
- 운영자가 다른 사람의 설명 없이 장애를 재현하고 정상화 판정을 할 수 있다.
- 실행하지 않은 Test는 성공으로 기록하지 않고, 실패·제약이 남으면 전체 완료로 보고하지 않는다.

## 금지 사항

- 메모리 Map·고정 JSON·정상 Case 한 건만으로 완료 처리
- 교육용 고객 업무를 `cpf-core`, `cpf-starters/common`, Starter, Runtime 제품 내부에 삽입
- Module-local DB Vendor Fallback SQL 또는 Vendor별 의미가 다른 Schema
- Timeout이나 응답 유실 뒤 실제 결과 확인 없이 무조건 재호출
- 비밀번호·Token·개인정보 원문을 Source·Log·Fixture·문서에 기록
- 매뉴얼 설명과 다른 임의 Package·API·상태·메뉴를 새로 생성

---

# 85. EDU-BAT-16 — Watermark 기반 증분 수집·재시작

- 우선순위: **P0**
- 연결 매뉴얼: `02_배치개발매뉴얼.md`
- Owner: `cpf-education + cpf-batch public contract`
- 구현 상태 목표: Source·SQL·API·Config·Test·운영 확인·매뉴얼 연결이 모두 완료

## 필요 근거

- 고객 매뉴얼에서 `Watermark 기반 증분 수집·재시작`을 설명하려면 정상 호출 예시만이 아니라 상태 변화와 장애 후 정상화까지 재현 가능한 실행 예제가 필요하다.
- 이 요구사항이 없으면 고객은 기능 가능 여부를 알더라도 실제 업무 적용 순서, 실패 판정, 재처리 금지 조건을 확인할 수 없다.
- 내부 구현 구조를 보여 주기 위한 예제가 아니라 고객 업무를 CPF로 만드는 방법과 운영 결과를 증명하는 예제로 구현한다.

## 고객 업무 시나리오

최종 처리 시각·고유키 Watermark 이후 데이터만 읽고 동일 시각 행을 누락하지 않는다.

## 구현 위치와 책임 경계

- 업무 Source: `com.cpf.education.edu.bat16` 또는 같은 의미의 기존 표준 Slice
- Test: Owner Module의 기존 Test Root 아래 `EDU-BAT-16` 식별 가능한 Class·Fixture
- SQL: 중앙 DB Vendor Pack의 기존 Install·Upgrade·Rollback·Verify·Seed 구조
- Config: 기존 Prefix·Profile·Secret 참조 방식을 유지
- 고객 업무 상태 변경과 원장 데이터는 고객 업무 Owner가 소유한다. ADM·BZA·Gateway·운영 Script가 업무 Table을 직접 수정하지 않는다.
- 공통 기능이 필요하면 기존 Public API·SPI를 우선 사용하고, 진입점이 없을 때만 실제 Consumer를 근거로 Owner Module에 확장한다.

## 입력·계약

- `fromWatermark`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `toWatermark`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `tieBreaker`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- 해당 기능에 필요한 Operation ID·업무 ID·Correlation ID·요청 Hash·사용자·권한 문맥을 저장·Log·Trace·Audit에서 연결한다.
- 요청·응답·Job Parameter·이벤트·파일 Layout 중 해당되는 계약을 OpenAPI 또는 Machine-readable Schema로 제공한다.

## 상태와 저장 근거

- 필수 상태 흐름: `READING → CHECKPOINTED → WATERMARK_ADVANCED`
- 상태마다 진입 조건, 허용 조치, 재시도 가능 여부, 최종 여부, 저장 위치와 Version 규칙을 정의한다.
- 결과 미확정·부분 성공이 가능한 경우 대상별 Attempt와 상대 상태 조회 근거를 별도 저장한다.

## 정상 처리 순서

1. 입력·권한·데이터 범위·기준 Version과 선행 상태를 검증한다.
2. 상태 변경·비동기 실행인 경우 Operation과 업무 식별자를 생성하거나 기존 멱등 요청을 조회한다. 조회 전용 기능은 조회 Context와 Cursor만 사용한다.
3. 상태 변경 기능은 고객 업무 Owner의 Application Use Case에서 Transaction 경계와 상태 전이를 수행하고, 조회 전용 기능은 일관된 Snapshot·정렬·범위 조건을 적용한다.
4. 외부 시스템·Kafka·파일·Batch·Gateway가 필요한 경우 Adapter 경계에서 Timeout·Retry·보안 정책을 적용한다.
5. 업무 결과와 Attempt·Audit·Log·Metric·Trace를 같은 식별자로 연결한다.
6. 고객 API 또는 운영 화면에서 최종 상태·건수·금액·Version·오류 0건을 확인한다.

## 예외·장애·경계 사례

- Clock skew
- 같은 timestamp
- source update
- rollback 후 watermark 선진행
- 상태 변경이 있는 경우 DB Commit 전 실패와 Commit 후 응답 유실을 구분한다.
- 같은 요청의 중복·동시 실행·순서 역전·늦은 응답을 포함한다.
- Retry 가능한 오류와 즉시 중단해야 하는 업무 오류를 구분한다.
- 대량·다중 대상 기능에서 부분 성공이 발생하면 성공 대상을 다시 실행하지 않고 실패 대상만 Reprocess·Reconcile한다.
- 복구·보상이 필요한 기능은 복구 실패와 보상 실패도 별도 상태·Attempt·Audit로 남긴다.

## 필수 Test

- Composite watermark
- transaction boundary
- restart
- source mutation test
- 입력 Validation·권한·Data Scope·Masking Negative Test
- 해당 기능에 적용되는 중복·동시성·Timeout·응답 유실·부분 실패 Test
- 해당 기능의 Failure Injection 후 필요한 Retry·Restart·Reprocess·Reconcile·Rollback Test
- Oracle·PostgreSQL·MariaDB 또는 해당 Infra의 의미 동일성 Test
- Log·Metric·Trace·Audit에 Secret·개인정보 원문이 없는지 검사

## 운영·화면 확인

- ADM 실행 상세에서 시작·종료 Watermark와 처리건수 확인
- 화면에는 해당 메뉴에 실제로 존재하는 조회 조건, 기본값, Column, 상세 Field, 상태, Button 활성 조건, 사유, 승인, 예상 Version, 대상별 결과만 Source와 일치하게 표시한다. 조회 전용 메뉴에 Command 항목을 만들지 않는다.
- 운영자가 정상화 완료를 판단할 수 있는 건수·금액·Version·Checksum·최종 상태·마지막 오류를 제공한다.
- 변경·외부연계 기능에서 응답 유실이 가능한 경우에만 요청을 다시 보내기 전에 Operation·거래·상대 상태를 조회하는 절차를 Browser 또는 API Test로 검증한다.

## 고객 매뉴얼·EDU 연결

- `02_배치개발매뉴얼.md`의 해당 기능 장에 `EDU-BAT-16`를 연결한다.
- 선행 환경, 실행 명령, 입력값, 정상 출력, 오류 재현 명령, 복구 명령, ADM/BZA/Gateway 확인 위치를 그대로 따라 할 수 있게 작성한다.
- 교육 코드에서 고객 업무로 바꿀 Package·Table·API·Topic·파일명과 CPF 관리 영역을 구분한 전환표를 제공한다.

## 완료 조건

- `EDU-BAT-16`가 교육 Catalog·Source·Test·고객 매뉴얼에 동일 ID로 존재한다.
- 정상·Validation·권한·중복·동시성·Timeout·응답 유실·부분 실패·복구 Test가 통과한다.
- 요청·응답·상태·DB·이벤트·파일·운영 화면이 양방향으로 일치한다.
- 운영자가 다른 사람의 설명 없이 장애를 재현하고 정상화 판정을 할 수 있다.
- 실행하지 않은 Test는 성공으로 기록하지 않고, 실패·제약이 남으면 전체 완료로 보고하지 않는다.

## 금지 사항

- 메모리 Map·고정 JSON·정상 Case 한 건만으로 완료 처리
- 교육용 고객 업무를 `cpf-core`, `cpf-starters/common`, Starter, Runtime 제품 내부에 삽입
- Module-local DB Vendor Fallback SQL 또는 Vendor별 의미가 다른 Schema
- Timeout이나 응답 유실 뒤 실제 결과 확인 없이 무조건 재호출
- 비밀번호·Token·개인정보 원문을 Source·Log·Fixture·문서에 기록
- 매뉴얼 설명과 다른 임의 Package·API·상태·메뉴를 새로 생성

---

# 86. EDU-BAT-17 — 암호화·압축·Checksum 파일 산출

- 우선순위: **P0**
- 연결 매뉴얼: `02_배치개발매뉴얼.md`
- Owner: `cpf-education + cpf-batch public contract`
- 구현 상태 목표: Source·SQL·API·Config·Test·운영 확인·매뉴얼 연결이 모두 완료

## 필요 근거

- 고객 매뉴얼에서 `암호화·압축·Checksum 파일 산출`을 설명하려면 정상 호출 예시만이 아니라 상태 변화와 장애 후 정상화까지 재현 가능한 실행 예제가 필요하다.
- 이 요구사항이 없으면 고객은 기능 가능 여부를 알더라도 실제 업무 적용 순서, 실패 판정, 재처리 금지 조건을 확인할 수 없다.
- 내부 구현 구조를 보여 주기 위한 예제가 아니라 고객 업무를 CPF로 만드는 방법과 운영 결과를 증명하는 예제로 구현한다.

## 고객 업무 시나리오

대외 전송 파일을 생성해 Trailer 합계, 압축, 암호화, checksum을 만든다.

## 구현 위치와 책임 경계

- 업무 Source: `com.cpf.education.edu.bat17` 또는 같은 의미의 기존 표준 Slice
- Test: Owner Module의 기존 Test Root 아래 `EDU-BAT-17` 식별 가능한 Class·Fixture
- SQL: 중앙 DB Vendor Pack의 기존 Install·Upgrade·Rollback·Verify·Seed 구조
- Config: 기존 Prefix·Profile·Secret 참조 방식을 유지
- 고객 업무 상태 변경과 원장 데이터는 고객 업무 Owner가 소유한다. ADM·BZA·Gateway·운영 Script가 업무 Table을 직접 수정하지 않는다.
- 공통 기능이 필요하면 기존 Public API·SPI를 우선 사용하고, 진입점이 없을 때만 실제 Consumer를 근거로 Owner Module에 확장한다.

## 입력·계약

- `businessDate`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `formatVersion`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `encryptionKeyRef`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- 해당 기능에 필요한 Operation ID·업무 ID·Correlation ID·요청 Hash·사용자·권한 문맥을 저장·Log·Trace·Audit에서 연결한다.
- 요청·응답·Job Parameter·이벤트·파일 Layout 중 해당되는 계약을 OpenAPI 또는 Machine-readable Schema로 제공한다.

## 상태와 저장 근거

- 필수 상태 흐름: `GENERATING → VALIDATED → COMPRESSED → ENCRYPTED → READY`
- 상태마다 진입 조건, 허용 조치, 재시도 가능 여부, 최종 여부, 저장 위치와 Version 규칙을 정의한다.
- 결과 미확정·부분 성공이 가능한 경우 대상별 Attempt와 상대 상태 조회 근거를 별도 저장한다.

## 정상 처리 순서

1. 입력·권한·데이터 범위·기준 Version과 선행 상태를 검증한다.
2. 상태 변경·비동기 실행인 경우 Operation과 업무 식별자를 생성하거나 기존 멱등 요청을 조회한다. 조회 전용 기능은 조회 Context와 Cursor만 사용한다.
3. 상태 변경 기능은 고객 업무 Owner의 Application Use Case에서 Transaction 경계와 상태 전이를 수행하고, 조회 전용 기능은 일관된 Snapshot·정렬·범위 조건을 적용한다.
4. 외부 시스템·Kafka·파일·Batch·Gateway가 필요한 경우 Adapter 경계에서 Timeout·Retry·보안 정책을 적용한다.
5. 업무 결과와 Attempt·Audit·Log·Metric·Trace를 같은 식별자로 연결한다.
6. 고객 API 또는 운영 화면에서 최종 상태·건수·금액·Version·오류 0건을 확인한다.

## 예외·장애·경계 사례

- Disk full
- checksum mismatch
- key unavailable
- 재생성
- 임시파일 잔존
- 상태 변경이 있는 경우 DB Commit 전 실패와 Commit 후 응답 유실을 구분한다.
- 같은 요청의 중복·동시 실행·순서 역전·늦은 응답을 포함한다.
- Retry 가능한 오류와 즉시 중단해야 하는 업무 오류를 구분한다.
- 대량·다중 대상 기능에서 부분 성공이 발생하면 성공 대상을 다시 실행하지 않고 실패 대상만 Reprocess·Reconcile한다.
- 복구·보상이 필요한 기능은 복구 실패와 보상 실패도 별도 상태·Attempt·Audit로 남긴다.

## 필수 Test

- Golden file
- trailer sum
- decrypt verify
- cleanup
- filename idempotency
- 입력 Validation·권한·Data Scope·Masking Negative Test
- 해당 기능에 적용되는 중복·동시성·Timeout·응답 유실·부분 실패 Test
- 해당 기능의 Failure Injection 후 필요한 Retry·Restart·Reprocess·Reconcile·Rollback Test
- Oracle·PostgreSQL·MariaDB 또는 해당 Infra의 의미 동일성 Test
- Log·Metric·Trace·Audit에 Secret·개인정보 원문이 없는지 검사

## 운영·화면 확인

- ADM File Jobs에서 크기·건수·checksum·암호화 Version 확인
- 화면에는 해당 메뉴에 실제로 존재하는 조회 조건, 기본값, Column, 상세 Field, 상태, Button 활성 조건, 사유, 승인, 예상 Version, 대상별 결과만 Source와 일치하게 표시한다. 조회 전용 메뉴에 Command 항목을 만들지 않는다.
- 운영자가 정상화 완료를 판단할 수 있는 건수·금액·Version·Checksum·최종 상태·마지막 오류를 제공한다.
- 변경·외부연계 기능에서 응답 유실이 가능한 경우에만 요청을 다시 보내기 전에 Operation·거래·상대 상태를 조회하는 절차를 Browser 또는 API Test로 검증한다.

## 고객 매뉴얼·EDU 연결

- `02_배치개발매뉴얼.md`의 해당 기능 장에 `EDU-BAT-17`를 연결한다.
- 선행 환경, 실행 명령, 입력값, 정상 출력, 오류 재현 명령, 복구 명령, ADM/BZA/Gateway 확인 위치를 그대로 따라 할 수 있게 작성한다.
- 교육 코드에서 고객 업무로 바꿀 Package·Table·API·Topic·파일명과 CPF 관리 영역을 구분한 전환표를 제공한다.

## 완료 조건

- `EDU-BAT-17`가 교육 Catalog·Source·Test·고객 매뉴얼에 동일 ID로 존재한다.
- 정상·Validation·권한·중복·동시성·Timeout·응답 유실·부분 실패·복구 Test가 통과한다.
- 요청·응답·상태·DB·이벤트·파일·운영 화면이 양방향으로 일치한다.
- 운영자가 다른 사람의 설명 없이 장애를 재현하고 정상화 판정을 할 수 있다.
- 실행하지 않은 Test는 성공으로 기록하지 않고, 실패·제약이 남으면 전체 완료로 보고하지 않는다.

## 금지 사항

- 메모리 Map·고정 JSON·정상 Case 한 건만으로 완료 처리
- 교육용 고객 업무를 `cpf-core`, `cpf-starters/common`, Starter, Runtime 제품 내부에 삽입
- Module-local DB Vendor Fallback SQL 또는 Vendor별 의미가 다른 Schema
- Timeout이나 응답 유실 뒤 실제 결과 확인 없이 무조건 재호출
- 비밀번호·Token·개인정보 원문을 Source·Log·Fixture·문서에 기록
- 매뉴얼 설명과 다른 임의 Package·API·상태·메뉴를 새로 생성

---

# 87. EDU-BAT-18 — 수신 파일 Header·Detail·Trailer 대사

- 우선순위: **P0**
- 연결 매뉴얼: `02_배치개발매뉴얼.md`
- Owner: `cpf-education + cpf-batch public contract`
- 구현 상태 목표: Source·SQL·API·Config·Test·운영 확인·매뉴얼 연결이 모두 완료

## 필요 근거

- 고객 매뉴얼에서 `수신 파일 Header·Detail·Trailer 대사`을 설명하려면 정상 호출 예시만이 아니라 상태 변화와 장애 후 정상화까지 재현 가능한 실행 예제가 필요하다.
- 이 요구사항이 없으면 고객은 기능 가능 여부를 알더라도 실제 업무 적용 순서, 실패 판정, 재처리 금지 조건을 확인할 수 없다.
- 내부 구현 구조를 보여 주기 위한 예제가 아니라 고객 업무를 CPF로 만드는 방법과 운영 결과를 증명하는 예제로 구현한다.

## 고객 업무 시나리오

기관 파일의 Header 건수·Detail 합계·Trailer Hash를 검증하고 오류 행을 격리한다.

## 구현 위치와 책임 경계

- 업무 Source: `com.cpf.education.edu.bat18` 또는 같은 의미의 기존 표준 Slice
- Test: Owner Module의 기존 Test Root 아래 `EDU-BAT-18` 식별 가능한 Class·Fixture
- SQL: 중앙 DB Vendor Pack의 기존 Install·Upgrade·Rollback·Verify·Seed 구조
- Config: 기존 Prefix·Profile·Secret 참조 방식을 유지
- 고객 업무 상태 변경과 원장 데이터는 고객 업무 Owner가 소유한다. ADM·BZA·Gateway·운영 Script가 업무 Table을 직접 수정하지 않는다.
- 공통 기능이 필요하면 기존 Public API·SPI를 우선 사용하고, 진입점이 없을 때만 실제 Consumer를 근거로 Owner Module에 확장한다.

## 입력·계약

- `filePath`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `layoutVersion`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `partnerId`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- 해당 기능에 필요한 Operation ID·업무 ID·Correlation ID·요청 Hash·사용자·권한 문맥을 저장·Log·Trace·Audit에서 연결한다.
- 요청·응답·Job Parameter·이벤트·파일 Layout 중 해당되는 계약을 OpenAPI 또는 Machine-readable Schema로 제공한다.

## 상태와 저장 근거

- 필수 상태 흐름: `RECEIVED → STRUCTURE_VALIDATED → LOADED/PARTIAL/REJECTED`
- 상태마다 진입 조건, 허용 조치, 재시도 가능 여부, 최종 여부, 저장 위치와 Version 규칙을 정의한다.
- 결과 미확정·부분 성공이 가능한 경우 대상별 Attempt와 상대 상태 조회 근거를 별도 저장한다.

## 정상 처리 순서

1. 입력·권한·데이터 범위·기준 Version과 선행 상태를 검증한다.
2. 상태 변경·비동기 실행인 경우 Operation과 업무 식별자를 생성하거나 기존 멱등 요청을 조회한다. 조회 전용 기능은 조회 Context와 Cursor만 사용한다.
3. 상태 변경 기능은 고객 업무 Owner의 Application Use Case에서 Transaction 경계와 상태 전이를 수행하고, 조회 전용 기능은 일관된 Snapshot·정렬·범위 조건을 적용한다.
4. 외부 시스템·Kafka·파일·Batch·Gateway가 필요한 경우 Adapter 경계에서 Timeout·Retry·보안 정책을 적용한다.
5. 업무 결과와 Attempt·Audit·Log·Metric·Trace를 같은 식별자로 연결한다.
6. 고객 API 또는 운영 화면에서 최종 상태·건수·금액·Version·오류 0건을 확인한다.

## 예외·장애·경계 사례

- 줄 길이 오류
- trailer 누락
- 합계 불일치
- 문자셋
- 중복 파일
- 상태 변경이 있는 경우 DB Commit 전 실패와 Commit 후 응답 유실을 구분한다.
- 같은 요청의 중복·동시 실행·순서 역전·늦은 응답을 포함한다.
- Retry 가능한 오류와 즉시 중단해야 하는 업무 오류를 구분한다.
- 대량·다중 대상 기능에서 부분 성공이 발생하면 성공 대상을 다시 실행하지 않고 실패 대상만 Reprocess·Reconcile한다.
- 복구·보상이 필요한 기능은 복구 실패와 보상 실패도 별도 상태·Attempt·Audit로 남긴다.

## 필수 Test

- Parser boundary
- reject file
- all-or-nothing policy
- restart
- 입력 Validation·권한·Data Scope·Masking Negative Test
- 해당 기능에 적용되는 중복·동시성·Timeout·응답 유실·부분 실패 Test
- 해당 기능의 Failure Injection 후 필요한 Retry·Restart·Reprocess·Reconcile·Rollback Test
- Oracle·PostgreSQL·MariaDB 또는 해당 Infra의 의미 동일성 Test
- Log·Metric·Trace·Audit에 Secret·개인정보 원문이 없는지 검사

## 운영·화면 확인

- ADM File Jobs에서 검증 단계·오류행·반려 사유 확인
- 화면에는 해당 메뉴에 실제로 존재하는 조회 조건, 기본값, Column, 상세 Field, 상태, Button 활성 조건, 사유, 승인, 예상 Version, 대상별 결과만 Source와 일치하게 표시한다. 조회 전용 메뉴에 Command 항목을 만들지 않는다.
- 운영자가 정상화 완료를 판단할 수 있는 건수·금액·Version·Checksum·최종 상태·마지막 오류를 제공한다.
- 변경·외부연계 기능에서 응답 유실이 가능한 경우에만 요청을 다시 보내기 전에 Operation·거래·상대 상태를 조회하는 절차를 Browser 또는 API Test로 검증한다.

## 고객 매뉴얼·EDU 연결

- `02_배치개발매뉴얼.md`의 해당 기능 장에 `EDU-BAT-18`를 연결한다.
- 선행 환경, 실행 명령, 입력값, 정상 출력, 오류 재현 명령, 복구 명령, ADM/BZA/Gateway 확인 위치를 그대로 따라 할 수 있게 작성한다.
- 교육 코드에서 고객 업무로 바꿀 Package·Table·API·Topic·파일명과 CPF 관리 영역을 구분한 전환표를 제공한다.

## 완료 조건

- `EDU-BAT-18`가 교육 Catalog·Source·Test·고객 매뉴얼에 동일 ID로 존재한다.
- 정상·Validation·권한·중복·동시성·Timeout·응답 유실·부분 실패·복구 Test가 통과한다.
- 요청·응답·상태·DB·이벤트·파일·운영 화면이 양방향으로 일치한다.
- 운영자가 다른 사람의 설명 없이 장애를 재현하고 정상화 판정을 할 수 있다.
- 실행하지 않은 Test는 성공으로 기록하지 않고, 실패·제약이 남으면 전체 완료로 보고하지 않는다.

## 금지 사항

- 메모리 Map·고정 JSON·정상 Case 한 건만으로 완료 처리
- 교육용 고객 업무를 `cpf-core`, `cpf-starters/common`, Starter, Runtime 제품 내부에 삽입
- Module-local DB Vendor Fallback SQL 또는 Vendor별 의미가 다른 Schema
- Timeout이나 응답 유실 뒤 실제 결과 확인 없이 무조건 재호출
- 비밀번호·Token·개인정보 원문을 Source·Log·Fixture·문서에 기록
- 매뉴얼 설명과 다른 임의 Package·API·상태·메뉴를 새로 생성

---

# 88. EDU-BAT-19 — 다중 파일 Fan-in·Fan-out

- 우선순위: **P0**
- 연결 매뉴얼: `02_배치개발매뉴얼.md`
- Owner: `cpf-education + cpf-batch public contract`
- 구현 상태 목표: Source·SQL·API·Config·Test·운영 확인·매뉴얼 연결이 모두 완료

## 필요 근거

- 고객 매뉴얼에서 `다중 파일 Fan-in·Fan-out`을 설명하려면 정상 호출 예시만이 아니라 상태 변화와 장애 후 정상화까지 재현 가능한 실행 예제가 필요하다.
- 이 요구사항이 없으면 고객은 기능 가능 여부를 알더라도 실제 업무 적용 순서, 실패 판정, 재처리 금지 조건을 확인할 수 없다.
- 내부 구현 구조를 보여 주기 위한 예제가 아니라 고객 업무를 CPF로 만드는 방법과 운영 결과를 증명하는 예제로 구현한다.

## 고객 업무 시나리오

지점별 100개 입력 파일을 모두 수집한 뒤 지역별 결과 파일로 분리한다.

## 구현 위치와 책임 경계

- 업무 Source: `com.cpf.education.edu.bat19` 또는 같은 의미의 기존 표준 Slice
- Test: Owner Module의 기존 Test Root 아래 `EDU-BAT-19` 식별 가능한 Class·Fixture
- SQL: 중앙 DB Vendor Pack의 기존 Install·Upgrade·Rollback·Verify·Seed 구조
- Config: 기존 Prefix·Profile·Secret 참조 방식을 유지
- 고객 업무 상태 변경과 원장 데이터는 고객 업무 Owner가 소유한다. ADM·BZA·Gateway·운영 Script가 업무 Table을 직접 수정하지 않는다.
- 공통 기능이 필요하면 기존 Public API·SPI를 우선 사용하고, 진입점이 없을 때만 실제 Consumer를 근거로 Owner Module에 확장한다.

## 입력·계약

- `expectedFiles`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `deadline`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `groupingKey`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- 해당 기능에 필요한 Operation ID·업무 ID·Correlation ID·요청 Hash·사용자·권한 문맥을 저장·Log·Trace·Audit에서 연결한다.
- 요청·응답·Job Parameter·이벤트·파일 Layout 중 해당되는 계약을 OpenAPI 또는 Machine-readable Schema로 제공한다.

## 상태와 저장 근거

- 필수 상태 흐름: `WAITING_FILES → READY → MERGING → SPLITTING → COMPLETED`
- 상태마다 진입 조건, 허용 조치, 재시도 가능 여부, 최종 여부, 저장 위치와 Version 규칙을 정의한다.
- 결과 미확정·부분 성공이 가능한 경우 대상별 Attempt와 상대 상태 조회 근거를 별도 저장한다.

## 정상 처리 순서

1. 입력·권한·데이터 범위·기준 Version과 선행 상태를 검증한다.
2. 상태 변경·비동기 실행인 경우 Operation과 업무 식별자를 생성하거나 기존 멱등 요청을 조회한다. 조회 전용 기능은 조회 Context와 Cursor만 사용한다.
3. 상태 변경 기능은 고객 업무 Owner의 Application Use Case에서 Transaction 경계와 상태 전이를 수행하고, 조회 전용 기능은 일관된 Snapshot·정렬·범위 조건을 적용한다.
4. 외부 시스템·Kafka·파일·Batch·Gateway가 필요한 경우 Adapter 경계에서 Timeout·Retry·보안 정책을 적용한다.
5. 업무 결과와 Attempt·Audit·Log·Metric·Trace를 같은 식별자로 연결한다.
6. 고객 API 또는 운영 화면에서 최종 상태·건수·금액·Version·오류 0건을 확인한다.

## 예외·장애·경계 사례

- 파일 1개 지연
- 중복 도착
- deadline 초과
- 결과 파일 일부 실패
- 상태 변경이 있는 경우 DB Commit 전 실패와 Commit 후 응답 유실을 구분한다.
- 같은 요청의 중복·동시 실행·순서 역전·늦은 응답을 포함한다.
- Retry 가능한 오류와 즉시 중단해야 하는 업무 오류를 구분한다.
- 대량·다중 대상 기능에서 부분 성공이 발생하면 성공 대상을 다시 실행하지 않고 실패 대상만 Reprocess·Reconcile한다.
- 복구·보상이 필요한 기능은 복구 실패와 보상 실패도 별도 상태·Attempt·Audit로 남긴다.

## 필수 Test

- File manifest
- completeness
- partitioned writer
- partial cleanup
- 입력 Validation·권한·Data Scope·Masking Negative Test
- 해당 기능에 적용되는 중복·동시성·Timeout·응답 유실·부분 실패 Test
- 해당 기능의 Failure Injection 후 필요한 Retry·Restart·Reprocess·Reconcile·Rollback Test
- Oracle·PostgreSQL·MariaDB 또는 해당 Infra의 의미 동일성 Test
- Log·Metric·Trace·Audit에 Secret·개인정보 원문이 없는지 검사

## 운영·화면 확인

- ADM 실행에서 입력 Manifest·누락·출력 목록 확인
- 화면에는 해당 메뉴에 실제로 존재하는 조회 조건, 기본값, Column, 상세 Field, 상태, Button 활성 조건, 사유, 승인, 예상 Version, 대상별 결과만 Source와 일치하게 표시한다. 조회 전용 메뉴에 Command 항목을 만들지 않는다.
- 운영자가 정상화 완료를 판단할 수 있는 건수·금액·Version·Checksum·최종 상태·마지막 오류를 제공한다.
- 변경·외부연계 기능에서 응답 유실이 가능한 경우에만 요청을 다시 보내기 전에 Operation·거래·상대 상태를 조회하는 절차를 Browser 또는 API Test로 검증한다.

## 고객 매뉴얼·EDU 연결

- `02_배치개발매뉴얼.md`의 해당 기능 장에 `EDU-BAT-19`를 연결한다.
- 선행 환경, 실행 명령, 입력값, 정상 출력, 오류 재현 명령, 복구 명령, ADM/BZA/Gateway 확인 위치를 그대로 따라 할 수 있게 작성한다.
- 교육 코드에서 고객 업무로 바꿀 Package·Table·API·Topic·파일명과 CPF 관리 영역을 구분한 전환표를 제공한다.

## 완료 조건

- `EDU-BAT-19`가 교육 Catalog·Source·Test·고객 매뉴얼에 동일 ID로 존재한다.
- 정상·Validation·권한·중복·동시성·Timeout·응답 유실·부분 실패·복구 Test가 통과한다.
- 요청·응답·상태·DB·이벤트·파일·운영 화면이 양방향으로 일치한다.
- 운영자가 다른 사람의 설명 없이 장애를 재현하고 정상화 판정을 할 수 있다.
- 실행하지 않은 Test는 성공으로 기록하지 않고, 실패·제약이 남으면 전체 완료로 보고하지 않는다.

## 금지 사항

- 메모리 Map·고정 JSON·정상 Case 한 건만으로 완료 처리
- 교육용 고객 업무를 `cpf-core`, `cpf-starters/common`, Starter, Runtime 제품 내부에 삽입
- Module-local DB Vendor Fallback SQL 또는 Vendor별 의미가 다른 Schema
- Timeout이나 응답 유실 뒤 실제 결과 확인 없이 무조건 재호출
- 비밀번호·Token·개인정보 원문을 Source·Log·Fixture·문서에 기록
- 매뉴얼 설명과 다른 임의 Package·API·상태·메뉴를 새로 생성

---

# 89. EDU-BAT-20 — Scheduler Misfire·Catch-up·건너뛰기

- 우선순위: **P0**
- 연결 매뉴얼: `02_배치개발매뉴얼.md`
- Owner: `cpf-education + cpf-batch public contract`
- 구현 상태 목표: Source·SQL·API·Config·Test·운영 확인·매뉴얼 연결이 모두 완료

## 필요 근거

- 고객 매뉴얼에서 `Scheduler Misfire·Catch-up·건너뛰기`을 설명하려면 정상 호출 예시만이 아니라 상태 변화와 장애 후 정상화까지 재현 가능한 실행 예제가 필요하다.
- 이 요구사항이 없으면 고객은 기능 가능 여부를 알더라도 실제 업무 적용 순서, 실패 판정, 재처리 금지 조건을 확인할 수 없다.
- 내부 구현 구조를 보여 주기 위한 예제가 아니라 고객 업무를 CPF로 만드는 방법과 운영 결과를 증명하는 예제로 구현한다.

## 고객 업무 시나리오

점검 중 놓친 3회 일정을 정책에 따라 한 번 Catch-up하거나 건너뛴다.

## 구현 위치와 책임 경계

- 업무 Source: `com.cpf.education.edu.bat20` 또는 같은 의미의 기존 표준 Slice
- Test: Owner Module의 기존 Test Root 아래 `EDU-BAT-20` 식별 가능한 Class·Fixture
- SQL: 중앙 DB Vendor Pack의 기존 Install·Upgrade·Rollback·Verify·Seed 구조
- Config: 기존 Prefix·Profile·Secret 참조 방식을 유지
- 고객 업무 상태 변경과 원장 데이터는 고객 업무 Owner가 소유한다. ADM·BZA·Gateway·운영 Script가 업무 Table을 직접 수정하지 않는다.
- 공통 기능이 필요하면 기존 Public API·SPI를 우선 사용하고, 진입점이 없을 때만 실제 Consumer를 근거로 Owner Module에 확장한다.

## 입력·계약

- `scheduleId`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `misfirePolicy`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `recoveryWindow`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- 해당 기능에 필요한 Operation ID·업무 ID·Correlation ID·요청 Hash·사용자·권한 문맥을 저장·Log·Trace·Audit에서 연결한다.
- 요청·응답·Job Parameter·이벤트·파일 Layout 중 해당되는 계약을 OpenAPI 또는 Machine-readable Schema로 제공한다.

## 상태와 저장 근거

- 필수 상태 흐름: `SCHEDULED → MISFIRED → CATCHING_UP/SKIPPED → NEXT_SCHEDULED`
- 상태마다 진입 조건, 허용 조치, 재시도 가능 여부, 최종 여부, 저장 위치와 Version 규칙을 정의한다.
- 결과 미확정·부분 성공이 가능한 경우 대상별 Attempt와 상대 상태 조회 근거를 별도 저장한다.

## 정상 처리 순서

1. 입력·권한·데이터 범위·기준 Version과 선행 상태를 검증한다.
2. 상태 변경·비동기 실행인 경우 Operation과 업무 식별자를 생성하거나 기존 멱등 요청을 조회한다. 조회 전용 기능은 조회 Context와 Cursor만 사용한다.
3. 상태 변경 기능은 고객 업무 Owner의 Application Use Case에서 Transaction 경계와 상태 전이를 수행하고, 조회 전용 기능은 일관된 Snapshot·정렬·범위 조건을 적용한다.
4. 외부 시스템·Kafka·파일·Batch·Gateway가 필요한 경우 Adapter 경계에서 Timeout·Retry·보안 정책을 적용한다.
5. 업무 결과와 Attempt·Audit·Log·Metric·Trace를 같은 식별자로 연결한다.
6. 고객 API 또는 운영 화면에서 최종 상태·건수·금액·Version·오류 0건을 확인한다.

## 예외·장애·경계 사례

- 중복 Scheduler
- 복구창 만료
- 휴일 변경
- Clock drift
- 상태 변경이 있는 경우 DB Commit 전 실패와 Commit 후 응답 유실을 구분한다.
- 같은 요청의 중복·동시 실행·순서 역전·늦은 응답을 포함한다.
- Retry 가능한 오류와 즉시 중단해야 하는 업무 오류를 구분한다.
- 대량·다중 대상 기능에서 부분 성공이 발생하면 성공 대상을 다시 실행하지 않고 실패 대상만 Reprocess·Reconcile한다.
- 복구·보상이 필요한 기능은 복구 실패와 보상 실패도 별도 상태·Attempt·Audit로 남긴다.

## 필수 Test

- Misfire policies
- cluster lock
- next fire time
- audit
- 입력 Validation·권한·Data Scope·Masking Negative Test
- 해당 기능에 적용되는 중복·동시성·Timeout·응답 유실·부분 실패 Test
- 해당 기능의 Failure Injection 후 필요한 Retry·Restart·Reprocess·Reconcile·Rollback Test
- Oracle·PostgreSQL·MariaDB 또는 해당 Infra의 의미 동일성 Test
- Log·Metric·Trace·Audit에 Secret·개인정보 원문이 없는지 검사

## 운영·화면 확인

- ADM Scheduler에서 원래 시각·처리정책·다음 시각 확인
- 화면에는 해당 메뉴에 실제로 존재하는 조회 조건, 기본값, Column, 상세 Field, 상태, Button 활성 조건, 사유, 승인, 예상 Version, 대상별 결과만 Source와 일치하게 표시한다. 조회 전용 메뉴에 Command 항목을 만들지 않는다.
- 운영자가 정상화 완료를 판단할 수 있는 건수·금액·Version·Checksum·최종 상태·마지막 오류를 제공한다.
- 변경·외부연계 기능에서 응답 유실이 가능한 경우에만 요청을 다시 보내기 전에 Operation·거래·상대 상태를 조회하는 절차를 Browser 또는 API Test로 검증한다.

## 고객 매뉴얼·EDU 연결

- `02_배치개발매뉴얼.md`의 해당 기능 장에 `EDU-BAT-20`를 연결한다.
- 선행 환경, 실행 명령, 입력값, 정상 출력, 오류 재현 명령, 복구 명령, ADM/BZA/Gateway 확인 위치를 그대로 따라 할 수 있게 작성한다.
- 교육 코드에서 고객 업무로 바꿀 Package·Table·API·Topic·파일명과 CPF 관리 영역을 구분한 전환표를 제공한다.

## 완료 조건

- `EDU-BAT-20`가 교육 Catalog·Source·Test·고객 매뉴얼에 동일 ID로 존재한다.
- 정상·Validation·권한·중복·동시성·Timeout·응답 유실·부분 실패·복구 Test가 통과한다.
- 요청·응답·상태·DB·이벤트·파일·운영 화면이 양방향으로 일치한다.
- 운영자가 다른 사람의 설명 없이 장애를 재현하고 정상화 판정을 할 수 있다.
- 실행하지 않은 Test는 성공으로 기록하지 않고, 실패·제약이 남으면 전체 완료로 보고하지 않는다.

## 금지 사항

- 메모리 Map·고정 JSON·정상 Case 한 건만으로 완료 처리
- 교육용 고객 업무를 `cpf-core`, `cpf-starters/common`, Starter, Runtime 제품 내부에 삽입
- Module-local DB Vendor Fallback SQL 또는 Vendor별 의미가 다른 Schema
- Timeout이나 응답 유실 뒤 실제 결과 확인 없이 무조건 재호출
- 비밀번호·Token·개인정보 원문을 Source·Log·Fixture·문서에 기록
- 매뉴얼 설명과 다른 임의 Package·API·상태·메뉴를 새로 생성

---

# 90. EDU-BAT-21 — 중복 실행 방지·동시 실행 허용 범위

- 우선순위: **P0**
- 연결 매뉴얼: `02_배치개발매뉴얼.md`
- Owner: `cpf-education + cpf-batch public contract`
- 구현 상태 목표: Source·SQL·API·Config·Test·운영 확인·매뉴얼 연결이 모두 완료

## 필요 근거

- 고객 매뉴얼에서 `중복 실행 방지·동시 실행 허용 범위`을 설명하려면 정상 호출 예시만이 아니라 상태 변화와 장애 후 정상화까지 재현 가능한 실행 예제가 필요하다.
- 이 요구사항이 없으면 고객은 기능 가능 여부를 알더라도 실제 업무 적용 순서, 실패 판정, 재처리 금지 조건을 확인할 수 없다.
- 내부 구현 구조를 보여 주기 위한 예제가 아니라 고객 업무를 CPF로 만드는 방법과 운영 결과를 증명하는 예제로 구현한다.

## 고객 업무 시나리오

같은 업무일자는 한 번만 실행하고 다른 지역 Partition은 병렬 허용한다.

## 구현 위치와 책임 경계

- 업무 Source: `com.cpf.education.edu.bat21` 또는 같은 의미의 기존 표준 Slice
- Test: Owner Module의 기존 Test Root 아래 `EDU-BAT-21` 식별 가능한 Class·Fixture
- SQL: 중앙 DB Vendor Pack의 기존 Install·Upgrade·Rollback·Verify·Seed 구조
- Config: 기존 Prefix·Profile·Secret 참조 방식을 유지
- 고객 업무 상태 변경과 원장 데이터는 고객 업무 Owner가 소유한다. ADM·BZA·Gateway·운영 Script가 업무 Table을 직접 수정하지 않는다.
- 공통 기능이 필요하면 기존 Public API·SPI를 우선 사용하고, 진입점이 없을 때만 실제 Consumer를 근거로 Owner Module에 확장한다.

## 입력·계약

- `jobKey`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `businessDate`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `scopeKey`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- 해당 기능에 필요한 Operation ID·업무 ID·Correlation ID·요청 Hash·사용자·권한 문맥을 저장·Log·Trace·Audit에서 연결한다.
- 요청·응답·Job Parameter·이벤트·파일 Layout 중 해당되는 계약을 OpenAPI 또는 Machine-readable Schema로 제공한다.

## 상태와 저장 근거

- 필수 상태 흐름: `AVAILABLE → CLAIMED → RUNNING → RELEASED`
- 상태마다 진입 조건, 허용 조치, 재시도 가능 여부, 최종 여부, 저장 위치와 Version 규칙을 정의한다.
- 결과 미확정·부분 성공이 가능한 경우 대상별 Attempt와 상대 상태 조회 근거를 별도 저장한다.

## 정상 처리 순서

1. 입력·권한·데이터 범위·기준 Version과 선행 상태를 검증한다.
2. 상태 변경·비동기 실행인 경우 Operation과 업무 식별자를 생성하거나 기존 멱등 요청을 조회한다. 조회 전용 기능은 조회 Context와 Cursor만 사용한다.
3. 상태 변경 기능은 고객 업무 Owner의 Application Use Case에서 Transaction 경계와 상태 전이를 수행하고, 조회 전용 기능은 일관된 Snapshot·정렬·범위 조건을 적용한다.
4. 외부 시스템·Kafka·파일·Batch·Gateway가 필요한 경우 Adapter 경계에서 Timeout·Retry·보안 정책을 적용한다.
5. 업무 결과와 Attempt·Audit·Log·Metric·Trace를 같은 식별자로 연결한다.
6. 고객 API 또는 운영 화면에서 최종 상태·건수·금액·Version·오류 0건을 확인한다.

## 예외·장애·경계 사례

- Launch race
- stale lock
- 운영 강제 해제
- 범위 키 충돌
- 상태 변경이 있는 경우 DB Commit 전 실패와 Commit 후 응답 유실을 구분한다.
- 같은 요청의 중복·동시 실행·순서 역전·늦은 응답을 포함한다.
- Retry 가능한 오류와 즉시 중단해야 하는 업무 오류를 구분한다.
- 대량·다중 대상 기능에서 부분 성공이 발생하면 성공 대상을 다시 실행하지 않고 실패 대상만 Reprocess·Reconcile한다.
- 복구·보상이 필요한 기능은 복구 실패와 보상 실패도 별도 상태·Attempt·Audit로 남긴다.

## 필수 Test

- Unique claim
- lease expiry
- manual override permission
- 입력 Validation·권한·Data Scope·Masking Negative Test
- 해당 기능에 적용되는 중복·동시성·Timeout·응답 유실·부분 실패 Test
- 해당 기능의 Failure Injection 후 필요한 Retry·Restart·Reprocess·Reconcile·Rollback Test
- Oracle·PostgreSQL·MariaDB 또는 해당 Infra의 의미 동일성 Test
- Log·Metric·Trace·Audit에 Secret·개인정보 원문이 없는지 검사

## 운영·화면 확인

- ADM Runtime/Lease에서 실행 소유권과 강제 해제 감사 확인
- 화면에는 해당 메뉴에 실제로 존재하는 조회 조건, 기본값, Column, 상세 Field, 상태, Button 활성 조건, 사유, 승인, 예상 Version, 대상별 결과만 Source와 일치하게 표시한다. 조회 전용 메뉴에 Command 항목을 만들지 않는다.
- 운영자가 정상화 완료를 판단할 수 있는 건수·금액·Version·Checksum·최종 상태·마지막 오류를 제공한다.
- 변경·외부연계 기능에서 응답 유실이 가능한 경우에만 요청을 다시 보내기 전에 Operation·거래·상대 상태를 조회하는 절차를 Browser 또는 API Test로 검증한다.

## 고객 매뉴얼·EDU 연결

- `02_배치개발매뉴얼.md`의 해당 기능 장에 `EDU-BAT-21`를 연결한다.
- 선행 환경, 실행 명령, 입력값, 정상 출력, 오류 재현 명령, 복구 명령, ADM/BZA/Gateway 확인 위치를 그대로 따라 할 수 있게 작성한다.
- 교육 코드에서 고객 업무로 바꿀 Package·Table·API·Topic·파일명과 CPF 관리 영역을 구분한 전환표를 제공한다.

## 완료 조건

- `EDU-BAT-21`가 교육 Catalog·Source·Test·고객 매뉴얼에 동일 ID로 존재한다.
- 정상·Validation·권한·중복·동시성·Timeout·응답 유실·부분 실패·복구 Test가 통과한다.
- 요청·응답·상태·DB·이벤트·파일·운영 화면이 양방향으로 일치한다.
- 운영자가 다른 사람의 설명 없이 장애를 재현하고 정상화 판정을 할 수 있다.
- 실행하지 않은 Test는 성공으로 기록하지 않고, 실패·제약이 남으면 전체 완료로 보고하지 않는다.

## 금지 사항

- 메모리 Map·고정 JSON·정상 Case 한 건만으로 완료 처리
- 교육용 고객 업무를 `cpf-core`, `cpf-starters/common`, Starter, Runtime 제품 내부에 삽입
- Module-local DB Vendor Fallback SQL 또는 Vendor별 의미가 다른 Schema
- Timeout이나 응답 유실 뒤 실제 결과 확인 없이 무조건 재호출
- 비밀번호·Token·개인정보 원문을 Source·Log·Fixture·문서에 기록
- 매뉴얼 설명과 다른 임의 Package·API·상태·메뉴를 새로 생성

---

# 91. EDU-BAT-22 — 휴일 Calendar·영업일 순번 JobParameter

- 우선순위: **P0**
- 연결 매뉴얼: `02_배치개발매뉴얼.md`
- Owner: `cpf-education + cpf-batch public contract`
- 구현 상태 목표: Source·SQL·API·Config·Test·운영 확인·매뉴얼 연결이 모두 완료

## 필요 근거

- 고객 매뉴얼에서 `휴일 Calendar·영업일 순번 JobParameter`을 설명하려면 정상 호출 예시만이 아니라 상태 변화와 장애 후 정상화까지 재현 가능한 실행 예제가 필요하다.
- 이 요구사항이 없으면 고객은 기능 가능 여부를 알더라도 실제 업무 적용 순서, 실패 판정, 재처리 금지 조건을 확인할 수 없다.
- 내부 구현 구조를 보여 주기 위한 예제가 아니라 고객 업무를 CPF로 만드는 방법과 운영 결과를 증명하는 예제로 구현한다.

## 고객 업무 시나리오

월말 영업일과 다음 영업일을 Calendar Version으로 결정한다.

## 구현 위치와 책임 경계

- 업무 Source: `com.cpf.education.edu.bat22` 또는 같은 의미의 기존 표준 Slice
- Test: Owner Module의 기존 Test Root 아래 `EDU-BAT-22` 식별 가능한 Class·Fixture
- SQL: 중앙 DB Vendor Pack의 기존 Install·Upgrade·Rollback·Verify·Seed 구조
- Config: 기존 Prefix·Profile·Secret 참조 방식을 유지
- 고객 업무 상태 변경과 원장 데이터는 고객 업무 Owner가 소유한다. ADM·BZA·Gateway·운영 Script가 업무 Table을 직접 수정하지 않는다.
- 공통 기능이 필요하면 기존 Public API·SPI를 우선 사용하고, 진입점이 없을 때만 실제 Consumer를 근거로 Owner Module에 확장한다.

## 입력·계약

- `calendarId`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `businessDate`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `sequence`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- 해당 기능에 필요한 Operation ID·업무 ID·Correlation ID·요청 Hash·사용자·권한 문맥을 저장·Log·Trace·Audit에서 연결한다.
- 요청·응답·Job Parameter·이벤트·파일 Layout 중 해당되는 계약을 OpenAPI 또는 Machine-readable Schema로 제공한다.

## 상태와 저장 근거

- 필수 상태 흐름: `DATE_RESOLVED → JOB_LAUNCHED 또는 NON_BUSINESS_SKIPPED`
- 상태마다 진입 조건, 허용 조치, 재시도 가능 여부, 최종 여부, 저장 위치와 Version 규칙을 정의한다.
- 결과 미확정·부분 성공이 가능한 경우 대상별 Attempt와 상대 상태 조회 근거를 별도 저장한다.

## 정상 처리 순서

1. 입력·권한·데이터 범위·기준 Version과 선행 상태를 검증한다.
2. 상태 변경·비동기 실행인 경우 Operation과 업무 식별자를 생성하거나 기존 멱등 요청을 조회한다. 조회 전용 기능은 조회 Context와 Cursor만 사용한다.
3. 상태 변경 기능은 고객 업무 Owner의 Application Use Case에서 Transaction 경계와 상태 전이를 수행하고, 조회 전용 기능은 일관된 Snapshot·정렬·범위 조건을 적용한다.
4. 외부 시스템·Kafka·파일·Batch·Gateway가 필요한 경우 Adapter 경계에서 Timeout·Retry·보안 정책을 적용한다.
5. 업무 결과와 Attempt·Audit·Log·Metric·Trace를 같은 식별자로 연결한다.
6. 고객 API 또는 운영 화면에서 최종 상태·건수·금액·Version·오류 0건을 확인한다.

## 예외·장애·경계 사례

- 긴급 휴일
- Calendar 소급 수정
- timezone
- 월말 변경
- 상태 변경이 있는 경우 DB Commit 전 실패와 Commit 후 응답 유실을 구분한다.
- 같은 요청의 중복·동시 실행·순서 역전·늦은 응답을 포함한다.
- Retry 가능한 오류와 즉시 중단해야 하는 업무 오류를 구분한다.
- 대량·다중 대상 기능에서 부분 성공이 발생하면 성공 대상을 다시 실행하지 않고 실패 대상만 Reprocess·Reconcile한다.
- 복구·보상이 필요한 기능은 복구 실패와 보상 실패도 별도 상태·Attempt·Audit로 남긴다.

## 필수 Test

- Calendar version pinning
- boundary dates
- scheduler integration
- 입력 Validation·권한·Data Scope·Masking Negative Test
- 해당 기능에 적용되는 중복·동시성·Timeout·응답 유실·부분 실패 Test
- 해당 기능의 Failure Injection 후 필요한 Retry·Restart·Reprocess·Reconcile·Rollback Test
- Oracle·PostgreSQL·MariaDB 또는 해당 Infra의 의미 동일성 Test
- Log·Metric·Trace·Audit에 Secret·개인정보 원문이 없는지 검사

## 운영·화면 확인

- ADM Business Calendar와 Job Parameter의 Version 일치 확인
- 화면에는 해당 메뉴에 실제로 존재하는 조회 조건, 기본값, Column, 상세 Field, 상태, Button 활성 조건, 사유, 승인, 예상 Version, 대상별 결과만 Source와 일치하게 표시한다. 조회 전용 메뉴에 Command 항목을 만들지 않는다.
- 운영자가 정상화 완료를 판단할 수 있는 건수·금액·Version·Checksum·최종 상태·마지막 오류를 제공한다.
- 변경·외부연계 기능에서 응답 유실이 가능한 경우에만 요청을 다시 보내기 전에 Operation·거래·상대 상태를 조회하는 절차를 Browser 또는 API Test로 검증한다.

## 고객 매뉴얼·EDU 연결

- `02_배치개발매뉴얼.md`의 해당 기능 장에 `EDU-BAT-22`를 연결한다.
- 선행 환경, 실행 명령, 입력값, 정상 출력, 오류 재현 명령, 복구 명령, ADM/BZA/Gateway 확인 위치를 그대로 따라 할 수 있게 작성한다.
- 교육 코드에서 고객 업무로 바꿀 Package·Table·API·Topic·파일명과 CPF 관리 영역을 구분한 전환표를 제공한다.

## 완료 조건

- `EDU-BAT-22`가 교육 Catalog·Source·Test·고객 매뉴얼에 동일 ID로 존재한다.
- 정상·Validation·권한·중복·동시성·Timeout·응답 유실·부분 실패·복구 Test가 통과한다.
- 요청·응답·상태·DB·이벤트·파일·운영 화면이 양방향으로 일치한다.
- 운영자가 다른 사람의 설명 없이 장애를 재현하고 정상화 판정을 할 수 있다.
- 실행하지 않은 Test는 성공으로 기록하지 않고, 실패·제약이 남으면 전체 완료로 보고하지 않는다.

## 금지 사항

- 메모리 Map·고정 JSON·정상 Case 한 건만으로 완료 처리
- 교육용 고객 업무를 `cpf-core`, `cpf-starters/common`, Starter, Runtime 제품 내부에 삽입
- Module-local DB Vendor Fallback SQL 또는 Vendor별 의미가 다른 Schema
- Timeout이나 응답 유실 뒤 실제 결과 확인 없이 무조건 재호출
- 비밀번호·Token·개인정보 원문을 Source·Log·Fixture·문서에 기록
- 매뉴얼 설명과 다른 임의 Package·API·상태·메뉴를 새로 생성

---

# 92. EDU-BAT-23 — Stop·Abandon·Restart 의미 분리

- 우선순위: **P0**
- 연결 매뉴얼: `02_배치개발매뉴얼.md`
- Owner: `cpf-education + cpf-batch public contract`
- 구현 상태 목표: Source·SQL·API·Config·Test·운영 확인·매뉴얼 연결이 모두 완료

## 필요 근거

- 고객 매뉴얼에서 `Stop·Abandon·Restart 의미 분리`을 설명하려면 정상 호출 예시만이 아니라 상태 변화와 장애 후 정상화까지 재현 가능한 실행 예제가 필요하다.
- 이 요구사항이 없으면 고객은 기능 가능 여부를 알더라도 실제 업무 적용 순서, 실패 판정, 재처리 금지 조건을 확인할 수 없다.
- 내부 구현 구조를 보여 주기 위한 예제가 아니라 고객 업무를 CPF로 만드는 방법과 운영 결과를 증명하는 예제로 구현한다.

## 고객 업무 시나리오

장시간 Job을 안전 중지하고 실패한 Execution만 재시작하며 잘못된 실행은 Abandon한다.

## 구현 위치와 책임 경계

- 업무 Source: `com.cpf.education.edu.bat23` 또는 같은 의미의 기존 표준 Slice
- Test: Owner Module의 기존 Test Root 아래 `EDU-BAT-23` 식별 가능한 Class·Fixture
- SQL: 중앙 DB Vendor Pack의 기존 Install·Upgrade·Rollback·Verify·Seed 구조
- Config: 기존 Prefix·Profile·Secret 참조 방식을 유지
- 고객 업무 상태 변경과 원장 데이터는 고객 업무 Owner가 소유한다. ADM·BZA·Gateway·운영 Script가 업무 Table을 직접 수정하지 않는다.
- 공통 기능이 필요하면 기존 Public API·SPI를 우선 사용하고, 진입점이 없을 때만 실제 Consumer를 근거로 Owner Module에 확장한다.

## 입력·계약

- `executionId`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `action`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `reason`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `expectedVersion`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- 해당 기능에 필요한 Operation ID·업무 ID·Correlation ID·요청 Hash·사용자·권한 문맥을 저장·Log·Trace·Audit에서 연결한다.
- 요청·응답·Job Parameter·이벤트·파일 Layout 중 해당되는 계약을 OpenAPI 또는 Machine-readable Schema로 제공한다.

## 상태와 저장 근거

- 필수 상태 흐름: `RUNNING → STOPPING → STOPPED; FAILED → RESTARTED; ABANDONED`
- 상태마다 진입 조건, 허용 조치, 재시도 가능 여부, 최종 여부, 저장 위치와 Version 규칙을 정의한다.
- 결과 미확정·부분 성공이 가능한 경우 대상별 Attempt와 상대 상태 조회 근거를 별도 저장한다.

## 정상 처리 순서

1. 입력·권한·데이터 범위·기준 Version과 선행 상태를 검증한다.
2. 상태 변경·비동기 실행인 경우 Operation과 업무 식별자를 생성하거나 기존 멱등 요청을 조회한다. 조회 전용 기능은 조회 Context와 Cursor만 사용한다.
3. 상태 변경 기능은 고객 업무 Owner의 Application Use Case에서 Transaction 경계와 상태 전이를 수행하고, 조회 전용 기능은 일관된 Snapshot·정렬·범위 조건을 적용한다.
4. 외부 시스템·Kafka·파일·Batch·Gateway가 필요한 경우 Adapter 경계에서 Timeout·Retry·보안 정책을 적용한다.
5. 업무 결과와 Attempt·Audit·Log·Metric·Trace를 같은 식별자로 연결한다.
6. 고객 API 또는 운영 화면에서 최종 상태·건수·금액·Version·오류 0건을 확인한다.

## 예외·장애·경계 사례

- Stop 무시 Tasklet
- Commit 중 Stop
- Abandon 후 재시작 시도
- 중복 제어
- 상태 변경이 있는 경우 DB Commit 전 실패와 Commit 후 응답 유실을 구분한다.
- 같은 요청의 중복·동시 실행·순서 역전·늦은 응답을 포함한다.
- Retry 가능한 오류와 즉시 중단해야 하는 업무 오류를 구분한다.
- 대량·다중 대상 기능에서 부분 성공이 발생하면 성공 대상을 다시 실행하지 않고 실패 대상만 Reprocess·Reconcile한다.
- 복구·보상이 필요한 기능은 복구 실패와 보상 실패도 별도 상태·Attempt·Audit로 남긴다.

## 필수 Test

- Cooperative stop
- status transition
- permission
- audit
- 입력 Validation·권한·Data Scope·Masking Negative Test
- 해당 기능에 적용되는 중복·동시성·Timeout·응답 유실·부분 실패 Test
- 해당 기능의 Failure Injection 후 필요한 Retry·Restart·Reprocess·Reconcile·Rollback Test
- Oracle·PostgreSQL·MariaDB 또는 해당 Infra의 의미 동일성 Test
- Log·Metric·Trace·Audit에 Secret·개인정보 원문이 없는지 검사

## 운영·화면 확인

- ADM Execution 제어 Button 활성 조건과 결과 확인
- 화면에는 해당 메뉴에 실제로 존재하는 조회 조건, 기본값, Column, 상세 Field, 상태, Button 활성 조건, 사유, 승인, 예상 Version, 대상별 결과만 Source와 일치하게 표시한다. 조회 전용 메뉴에 Command 항목을 만들지 않는다.
- 운영자가 정상화 완료를 판단할 수 있는 건수·금액·Version·Checksum·최종 상태·마지막 오류를 제공한다.
- 변경·외부연계 기능에서 응답 유실이 가능한 경우에만 요청을 다시 보내기 전에 Operation·거래·상대 상태를 조회하는 절차를 Browser 또는 API Test로 검증한다.

## 고객 매뉴얼·EDU 연결

- `02_배치개발매뉴얼.md`의 해당 기능 장에 `EDU-BAT-23`를 연결한다.
- 선행 환경, 실행 명령, 입력값, 정상 출력, 오류 재현 명령, 복구 명령, ADM/BZA/Gateway 확인 위치를 그대로 따라 할 수 있게 작성한다.
- 교육 코드에서 고객 업무로 바꿀 Package·Table·API·Topic·파일명과 CPF 관리 영역을 구분한 전환표를 제공한다.

## 완료 조건

- `EDU-BAT-23`가 교육 Catalog·Source·Test·고객 매뉴얼에 동일 ID로 존재한다.
- 정상·Validation·권한·중복·동시성·Timeout·응답 유실·부분 실패·복구 Test가 통과한다.
- 요청·응답·상태·DB·이벤트·파일·운영 화면이 양방향으로 일치한다.
- 운영자가 다른 사람의 설명 없이 장애를 재현하고 정상화 판정을 할 수 있다.
- 실행하지 않은 Test는 성공으로 기록하지 않고, 실패·제약이 남으면 전체 완료로 보고하지 않는다.

## 금지 사항

- 메모리 Map·고정 JSON·정상 Case 한 건만으로 완료 처리
- 교육용 고객 업무를 `cpf-core`, `cpf-starters/common`, Starter, Runtime 제품 내부에 삽입
- Module-local DB Vendor Fallback SQL 또는 Vendor별 의미가 다른 Schema
- Timeout이나 응답 유실 뒤 실제 결과 확인 없이 무조건 재호출
- 비밀번호·Token·개인정보 원문을 Source·Log·Fixture·문서에 기록
- 매뉴얼 설명과 다른 임의 Package·API·상태·메뉴를 새로 생성

---

# 93. EDU-BAT-24 — Remote Worker 유실·재할당·중복 결과 차단

- 우선순위: **P0**
- 연결 매뉴얼: `02_배치개발매뉴얼.md`
- Owner: `cpf-education + cpf-batch public contract`
- 구현 상태 목표: Source·SQL·API·Config·Test·운영 확인·매뉴얼 연결이 모두 완료

## 필요 근거

- 고객 매뉴얼에서 `Remote Worker 유실·재할당·중복 결과 차단`을 설명하려면 정상 호출 예시만이 아니라 상태 변화와 장애 후 정상화까지 재현 가능한 실행 예제가 필요하다.
- 이 요구사항이 없으면 고객은 기능 가능 여부를 알더라도 실제 업무 적용 순서, 실패 판정, 재처리 금지 조건을 확인할 수 없다.
- 내부 구현 구조를 보여 주기 위한 예제가 아니라 고객 업무를 CPF로 만드는 방법과 운영 결과를 증명하는 예제로 구현한다.

## 고객 업무 시나리오

Worker가 Partition 처리 중 종료되면 Lease 만료 후 다른 Worker가 이어받고 과거 결과는 거부된다.

## 구현 위치와 책임 경계

- 업무 Source: `com.cpf.education.edu.bat24` 또는 같은 의미의 기존 표준 Slice
- Test: Owner Module의 기존 Test Root 아래 `EDU-BAT-24` 식별 가능한 Class·Fixture
- SQL: 중앙 DB Vendor Pack의 기존 Install·Upgrade·Rollback·Verify·Seed 구조
- Config: 기존 Prefix·Profile·Secret 참조 방식을 유지
- 고객 업무 상태 변경과 원장 데이터는 고객 업무 Owner가 소유한다. ADM·BZA·Gateway·운영 Script가 업무 Table을 직접 수정하지 않는다.
- 공통 기능이 필요하면 기존 Public API·SPI를 우선 사용하고, 진입점이 없을 때만 실제 Consumer를 근거로 Owner Module에 확장한다.

## 입력·계약

- `partitionId`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `workerId`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `fencingToken`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- 해당 기능에 필요한 Operation ID·업무 ID·Correlation ID·요청 Hash·사용자·권한 문맥을 저장·Log·Trace·Audit에서 연결한다.
- 요청·응답·Job Parameter·이벤트·파일 Layout 중 해당되는 계약을 OpenAPI 또는 Machine-readable Schema로 제공한다.

## 상태와 저장 근거

- 필수 상태 흐름: `ASSIGNED → RUNNING → LOST → REASSIGNED → COMPLETED`
- 상태마다 진입 조건, 허용 조치, 재시도 가능 여부, 최종 여부, 저장 위치와 Version 규칙을 정의한다.
- 결과 미확정·부분 성공이 가능한 경우 대상별 Attempt와 상대 상태 조회 근거를 별도 저장한다.

## 정상 처리 순서

1. 입력·권한·데이터 범위·기준 Version과 선행 상태를 검증한다.
2. 상태 변경·비동기 실행인 경우 Operation과 업무 식별자를 생성하거나 기존 멱등 요청을 조회한다. 조회 전용 기능은 조회 Context와 Cursor만 사용한다.
3. 상태 변경 기능은 고객 업무 Owner의 Application Use Case에서 Transaction 경계와 상태 전이를 수행하고, 조회 전용 기능은 일관된 Snapshot·정렬·범위 조건을 적용한다.
4. 외부 시스템·Kafka·파일·Batch·Gateway가 필요한 경우 Adapter 경계에서 Timeout·Retry·보안 정책을 적용한다.
5. 업무 결과와 Attempt·Audit·Log·Metric·Trace를 같은 식별자로 연결한다.
6. 고객 API 또는 운영 화면에서 최종 상태·건수·금액·Version·오류 0건을 확인한다.

## 예외·장애·경계 사례

- Network partition
- 늦은 완료
- 이중 Worker
- heartbeat 지연
- 상태 변경이 있는 경우 DB Commit 전 실패와 Commit 후 응답 유실을 구분한다.
- 같은 요청의 중복·동시 실행·순서 역전·늦은 응답을 포함한다.
- Retry 가능한 오류와 즉시 중단해야 하는 업무 오류를 구분한다.
- 대량·다중 대상 기능에서 부분 성공이 발생하면 성공 대상을 다시 실행하지 않고 실패 대상만 Reprocess·Reconcile한다.
- 복구·보상이 필요한 기능은 복구 실패와 보상 실패도 별도 상태·Attempt·Audit로 남긴다.

## 필수 Test

- Heartbeat
- lease
- fencing
- idempotent writer
- broker duplicate
- 입력 Validation·권한·Data Scope·Masking Negative Test
- 해당 기능에 적용되는 중복·동시성·Timeout·응답 유실·부분 실패 Test
- 해당 기능의 Failure Injection 후 필요한 Retry·Restart·Reprocess·Reconcile·Rollback Test
- Oracle·PostgreSQL·MariaDB 또는 해당 Infra의 의미 동일성 Test
- Log·Metric·Trace·Audit에 Secret·개인정보 원문이 없는지 검사

## 운영·화면 확인

- ADM Worker/Lease/Recovery에서 유실·재할당·거부 확인
- 화면에는 해당 메뉴에 실제로 존재하는 조회 조건, 기본값, Column, 상세 Field, 상태, Button 활성 조건, 사유, 승인, 예상 Version, 대상별 결과만 Source와 일치하게 표시한다. 조회 전용 메뉴에 Command 항목을 만들지 않는다.
- 운영자가 정상화 완료를 판단할 수 있는 건수·금액·Version·Checksum·최종 상태·마지막 오류를 제공한다.
- 변경·외부연계 기능에서 응답 유실이 가능한 경우에만 요청을 다시 보내기 전에 Operation·거래·상대 상태를 조회하는 절차를 Browser 또는 API Test로 검증한다.

## 고객 매뉴얼·EDU 연결

- `02_배치개발매뉴얼.md`의 해당 기능 장에 `EDU-BAT-24`를 연결한다.
- 선행 환경, 실행 명령, 입력값, 정상 출력, 오류 재현 명령, 복구 명령, ADM/BZA/Gateway 확인 위치를 그대로 따라 할 수 있게 작성한다.
- 교육 코드에서 고객 업무로 바꿀 Package·Table·API·Topic·파일명과 CPF 관리 영역을 구분한 전환표를 제공한다.

## 완료 조건

- `EDU-BAT-24`가 교육 Catalog·Source·Test·고객 매뉴얼에 동일 ID로 존재한다.
- 정상·Validation·권한·중복·동시성·Timeout·응답 유실·부분 실패·복구 Test가 통과한다.
- 요청·응답·상태·DB·이벤트·파일·운영 화면이 양방향으로 일치한다.
- 운영자가 다른 사람의 설명 없이 장애를 재현하고 정상화 판정을 할 수 있다.
- 실행하지 않은 Test는 성공으로 기록하지 않고, 실패·제약이 남으면 전체 완료로 보고하지 않는다.

## 금지 사항

- 메모리 Map·고정 JSON·정상 Case 한 건만으로 완료 처리
- 교육용 고객 업무를 `cpf-core`, `cpf-starters/common`, Starter, Runtime 제품 내부에 삽입
- Module-local DB Vendor Fallback SQL 또는 Vendor별 의미가 다른 Schema
- Timeout이나 응답 유실 뒤 실제 결과 확인 없이 무조건 재호출
- 비밀번호·Token·개인정보 원문을 Source·Log·Fixture·문서에 기록
- 매뉴얼 설명과 다른 임의 Package·API·상태·메뉴를 새로 생성

---

# 94. EDU-BAT-25 — Partition 편향 감지·재분할

- 우선순위: **P0**
- 연결 매뉴얼: `02_배치개발매뉴얼.md`
- Owner: `cpf-education + cpf-batch public contract`
- 구현 상태 목표: Source·SQL·API·Config·Test·운영 확인·매뉴얼 연결이 모두 완료

## 필요 근거

- 고객 매뉴얼에서 `Partition 편향 감지·재분할`을 설명하려면 정상 호출 예시만이 아니라 상태 변화와 장애 후 정상화까지 재현 가능한 실행 예제가 필요하다.
- 이 요구사항이 없으면 고객은 기능 가능 여부를 알더라도 실제 업무 적용 순서, 실패 판정, 재처리 금지 조건을 확인할 수 없다.
- 내부 구현 구조를 보여 주기 위한 예제가 아니라 고객 업무를 CPF로 만드는 방법과 운영 결과를 증명하는 예제로 구현한다.

## 고객 업무 시나리오

특정 고객 구간에 데이터가 몰려 전체 완료가 지연되면 동적 분할로 균형을 맞춘다.

## 구현 위치와 책임 경계

- 업무 Source: `com.cpf.education.edu.bat25` 또는 같은 의미의 기존 표준 Slice
- Test: Owner Module의 기존 Test Root 아래 `EDU-BAT-25` 식별 가능한 Class·Fixture
- SQL: 중앙 DB Vendor Pack의 기존 Install·Upgrade·Rollback·Verify·Seed 구조
- Config: 기존 Prefix·Profile·Secret 참조 방식을 유지
- 고객 업무 상태 변경과 원장 데이터는 고객 업무 Owner가 소유한다. ADM·BZA·Gateway·운영 Script가 업무 Table을 직접 수정하지 않는다.
- 공통 기능이 필요하면 기존 Public API·SPI를 우선 사용하고, 진입점이 없을 때만 실제 Consumer를 근거로 Owner Module에 확장한다.

## 입력·계약

- `partitionPlan`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `targetSize`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `maxWorkers`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- 해당 기능에 필요한 Operation ID·업무 ID·Correlation ID·요청 Hash·사용자·권한 문맥을 저장·Log·Trace·Audit에서 연결한다.
- 요청·응답·Job Parameter·이벤트·파일 Layout 중 해당되는 계약을 OpenAPI 또는 Machine-readable Schema로 제공한다.

## 상태와 저장 근거

- 필수 상태 흐름: `PLANNED → RUNNING → SKEW_DETECTED → REBALANCED`
- 상태마다 진입 조건, 허용 조치, 재시도 가능 여부, 최종 여부, 저장 위치와 Version 규칙을 정의한다.
- 결과 미확정·부분 성공이 가능한 경우 대상별 Attempt와 상대 상태 조회 근거를 별도 저장한다.

## 정상 처리 순서

1. 입력·권한·데이터 범위·기준 Version과 선행 상태를 검증한다.
2. 상태 변경·비동기 실행인 경우 Operation과 업무 식별자를 생성하거나 기존 멱등 요청을 조회한다. 조회 전용 기능은 조회 Context와 Cursor만 사용한다.
3. 상태 변경 기능은 고객 업무 Owner의 Application Use Case에서 Transaction 경계와 상태 전이를 수행하고, 조회 전용 기능은 일관된 Snapshot·정렬·범위 조건을 적용한다.
4. 외부 시스템·Kafka·파일·Batch·Gateway가 필요한 경우 Adapter 경계에서 Timeout·Retry·보안 정책을 적용한다.
5. 업무 결과와 Attempt·Audit·Log·Metric·Trace를 같은 식별자로 연결한다.
6. 고객 API 또는 운영 화면에서 최종 상태·건수·금액·Version·오류 0건을 확인한다.

## 예외·장애·경계 사례

- 큰 단일 고객
- 재분할 중 처리
- 동일 item 중복
- worker limit
- 상태 변경이 있는 경우 DB Commit 전 실패와 Commit 후 응답 유실을 구분한다.
- 같은 요청의 중복·동시 실행·순서 역전·늦은 응답을 포함한다.
- Retry 가능한 오류와 즉시 중단해야 하는 업무 오류를 구분한다.
- 대량·다중 대상 기능에서 부분 성공이 발생하면 성공 대상을 다시 실행하지 않고 실패 대상만 Reprocess·Reconcile한다.
- 복구·보상이 필요한 기능은 복구 실패와 보상 실패도 별도 상태·Attempt·Audit로 남긴다.

## 필수 Test

- Histogram preview
- split boundary
- idempotency
- capacity test
- 입력 Validation·권한·Data Scope·Masking Negative Test
- 해당 기능에 적용되는 중복·동시성·Timeout·응답 유실·부분 실패 Test
- 해당 기능의 Failure Injection 후 필요한 Retry·Restart·Reprocess·Reconcile·Rollback Test
- Oracle·PostgreSQL·MariaDB 또는 해당 Infra의 의미 동일성 Test
- Log·Metric·Trace·Audit에 Secret·개인정보 원문이 없는지 검사

## 운영·화면 확인

- ADM Partition 진행률에서 편향·재분할·최종 합계 확인
- 화면에는 해당 메뉴에 실제로 존재하는 조회 조건, 기본값, Column, 상세 Field, 상태, Button 활성 조건, 사유, 승인, 예상 Version, 대상별 결과만 Source와 일치하게 표시한다. 조회 전용 메뉴에 Command 항목을 만들지 않는다.
- 운영자가 정상화 완료를 판단할 수 있는 건수·금액·Version·Checksum·최종 상태·마지막 오류를 제공한다.
- 변경·외부연계 기능에서 응답 유실이 가능한 경우에만 요청을 다시 보내기 전에 Operation·거래·상대 상태를 조회하는 절차를 Browser 또는 API Test로 검증한다.

## 고객 매뉴얼·EDU 연결

- `02_배치개발매뉴얼.md`의 해당 기능 장에 `EDU-BAT-25`를 연결한다.
- 선행 환경, 실행 명령, 입력값, 정상 출력, 오류 재현 명령, 복구 명령, ADM/BZA/Gateway 확인 위치를 그대로 따라 할 수 있게 작성한다.
- 교육 코드에서 고객 업무로 바꿀 Package·Table·API·Topic·파일명과 CPF 관리 영역을 구분한 전환표를 제공한다.

## 완료 조건

- `EDU-BAT-25`가 교육 Catalog·Source·Test·고객 매뉴얼에 동일 ID로 존재한다.
- 정상·Validation·권한·중복·동시성·Timeout·응답 유실·부분 실패·복구 Test가 통과한다.
- 요청·응답·상태·DB·이벤트·파일·운영 화면이 양방향으로 일치한다.
- 운영자가 다른 사람의 설명 없이 장애를 재현하고 정상화 판정을 할 수 있다.
- 실행하지 않은 Test는 성공으로 기록하지 않고, 실패·제약이 남으면 전체 완료로 보고하지 않는다.

## 금지 사항

- 메모리 Map·고정 JSON·정상 Case 한 건만으로 완료 처리
- 교육용 고객 업무를 `cpf-core`, `cpf-starters/common`, Starter, Runtime 제품 내부에 삽입
- Module-local DB Vendor Fallback SQL 또는 Vendor별 의미가 다른 Schema
- Timeout이나 응답 유실 뒤 실제 결과 확인 없이 무조건 재호출
- 비밀번호·Token·개인정보 원문을 Source·Log·Fixture·문서에 기록
- 매뉴얼 설명과 다른 임의 Package·API·상태·메뉴를 새로 생성

---

# 95. EDU-BAT-26 — 센터컷 결과 대사·차이 보정·재실행

- 우선순위: **P0**
- 연결 매뉴얼: `02_배치개발매뉴얼.md`
- Owner: `cpf-education + cpf-batch public contract`
- 구현 상태 목표: Source·SQL·API·Config·Test·운영 확인·매뉴얼 연결이 모두 완료

## 필요 근거

- 고객 매뉴얼에서 `센터컷 결과 대사·차이 보정·재실행`을 설명하려면 정상 호출 예시만이 아니라 상태 변화와 장애 후 정상화까지 재현 가능한 실행 예제가 필요하다.
- 이 요구사항이 없으면 고객은 기능 가능 여부를 알더라도 실제 업무 적용 순서, 실패 판정, 재처리 금지 조건을 확인할 수 없다.
- 내부 구현 구조를 보여 주기 위한 예제가 아니라 고객 업무를 CPF로 만드는 방법과 운영 결과를 증명하는 예제로 구현한다.

## 고객 업무 시나리오

센터컷 후 원장 합계와 산출 합계가 다르면 차이 대상만 보정한다.

## 구현 위치와 책임 경계

- 업무 Source: `com.cpf.education.edu.bat26` 또는 같은 의미의 기존 표준 Slice
- Test: Owner Module의 기존 Test Root 아래 `EDU-BAT-26` 식별 가능한 Class·Fixture
- SQL: 중앙 DB Vendor Pack의 기존 Install·Upgrade·Rollback·Verify·Seed 구조
- Config: 기존 Prefix·Profile·Secret 참조 방식을 유지
- 고객 업무 상태 변경과 원장 데이터는 고객 업무 Owner가 소유한다. ADM·BZA·Gateway·운영 Script가 업무 Table을 직접 수정하지 않는다.
- 공통 기능이 필요하면 기존 Public API·SPI를 우선 사용하고, 진입점이 없을 때만 실제 Consumer를 근거로 Owner Module에 확장한다.

## 입력·계약

- `cutId`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `expectedCount`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `expectedAmount`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `reconcileRule`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- 해당 기능에 필요한 Operation ID·업무 ID·Correlation ID·요청 Hash·사용자·권한 문맥을 저장·Log·Trace·Audit에서 연결한다.
- 요청·응답·Job Parameter·이벤트·파일 Layout 중 해당되는 계약을 OpenAPI 또는 Machine-readable Schema로 제공한다.

## 상태와 저장 근거

- 필수 상태 흐름: `EXECUTED → RECONCILING → MATCHED/MISMATCH → CORRECTED`
- 상태마다 진입 조건, 허용 조치, 재시도 가능 여부, 최종 여부, 저장 위치와 Version 규칙을 정의한다.
- 결과 미확정·부분 성공이 가능한 경우 대상별 Attempt와 상대 상태 조회 근거를 별도 저장한다.

## 정상 처리 순서

1. 입력·권한·데이터 범위·기준 Version과 선행 상태를 검증한다.
2. 상태 변경·비동기 실행인 경우 Operation과 업무 식별자를 생성하거나 기존 멱등 요청을 조회한다. 조회 전용 기능은 조회 Context와 Cursor만 사용한다.
3. 상태 변경 기능은 고객 업무 Owner의 Application Use Case에서 Transaction 경계와 상태 전이를 수행하고, 조회 전용 기능은 일관된 Snapshot·정렬·범위 조건을 적용한다.
4. 외부 시스템·Kafka·파일·Batch·Gateway가 필요한 경우 Adapter 경계에서 Timeout·Retry·보안 정책을 적용한다.
5. 업무 결과와 Attempt·Audit·Log·Metric·Trace를 같은 식별자로 연결한다.
6. 고객 API 또는 운영 화면에서 최종 상태·건수·금액·Version·오류 0건을 확인한다.

## 예외·장애·경계 사례

- 건수만 일치
- 금액 반올림
- 부분 적용
- 보정 중 장애
- 상태 변경이 있는 경우 DB Commit 전 실패와 Commit 후 응답 유실을 구분한다.
- 같은 요청의 중복·동시 실행·순서 역전·늦은 응답을 포함한다.
- Retry 가능한 오류와 즉시 중단해야 하는 업무 오류를 구분한다.
- 대량·다중 대상 기능에서 부분 성공이 발생하면 성공 대상을 다시 실행하지 않고 실패 대상만 Reprocess·Reconcile한다.
- 복구·보상이 필요한 기능은 복구 실패와 보상 실패도 별도 상태·Attempt·Audit로 남긴다.

## 필수 Test

- Control totals
- diff extract
- compensation job
- rerun safety
- 입력 Validation·권한·Data Scope·Masking Negative Test
- 해당 기능에 적용되는 중복·동시성·Timeout·응답 유실·부분 실패 Test
- 해당 기능의 Failure Injection 후 필요한 Retry·Restart·Reprocess·Reconcile·Rollback Test
- Oracle·PostgreSQL·MariaDB 또는 해당 Infra의 의미 동일성 Test
- Log·Metric·Trace·Audit에 Secret·개인정보 원문이 없는지 검사

## 운영·화면 확인

- ADM Center-Cut에서 Preview·승인·실행·대사·보정 연결
- 화면에는 해당 메뉴에 실제로 존재하는 조회 조건, 기본값, Column, 상세 Field, 상태, Button 활성 조건, 사유, 승인, 예상 Version, 대상별 결과만 Source와 일치하게 표시한다. 조회 전용 메뉴에 Command 항목을 만들지 않는다.
- 운영자가 정상화 완료를 판단할 수 있는 건수·금액·Version·Checksum·최종 상태·마지막 오류를 제공한다.
- 변경·외부연계 기능에서 응답 유실이 가능한 경우에만 요청을 다시 보내기 전에 Operation·거래·상대 상태를 조회하는 절차를 Browser 또는 API Test로 검증한다.

## 고객 매뉴얼·EDU 연결

- `02_배치개발매뉴얼.md`의 해당 기능 장에 `EDU-BAT-26`를 연결한다.
- 선행 환경, 실행 명령, 입력값, 정상 출력, 오류 재현 명령, 복구 명령, ADM/BZA/Gateway 확인 위치를 그대로 따라 할 수 있게 작성한다.
- 교육 코드에서 고객 업무로 바꿀 Package·Table·API·Topic·파일명과 CPF 관리 영역을 구분한 전환표를 제공한다.

## 완료 조건

- `EDU-BAT-26`가 교육 Catalog·Source·Test·고객 매뉴얼에 동일 ID로 존재한다.
- 정상·Validation·권한·중복·동시성·Timeout·응답 유실·부분 실패·복구 Test가 통과한다.
- 요청·응답·상태·DB·이벤트·파일·운영 화면이 양방향으로 일치한다.
- 운영자가 다른 사람의 설명 없이 장애를 재현하고 정상화 판정을 할 수 있다.
- 실행하지 않은 Test는 성공으로 기록하지 않고, 실패·제약이 남으면 전체 완료로 보고하지 않는다.

## 금지 사항

- 메모리 Map·고정 JSON·정상 Case 한 건만으로 완료 처리
- 교육용 고객 업무를 `cpf-core`, `cpf-starters/common`, Starter, Runtime 제품 내부에 삽입
- Module-local DB Vendor Fallback SQL 또는 Vendor별 의미가 다른 Schema
- Timeout이나 응답 유실 뒤 실제 결과 확인 없이 무조건 재호출
- 비밀번호·Token·개인정보 원문을 Source·Log·Fixture·문서에 기록
- 매뉴얼 설명과 다른 임의 Package·API·상태·메뉴를 새로 생성

---

# 96. EDU-BAT-27 — Job Pack Checksum·호환성·이전 Version 복구

- 우선순위: **P0**
- 연결 매뉴얼: `02_배치개발매뉴얼.md`
- Owner: `cpf-education + cpf-batch public contract`
- 구현 상태 목표: Source·SQL·API·Config·Test·운영 확인·매뉴얼 연결이 모두 완료

## 필요 근거

- 고객 매뉴얼에서 `Job Pack Checksum·호환성·이전 Version 복구`을 설명하려면 정상 호출 예시만이 아니라 상태 변화와 장애 후 정상화까지 재현 가능한 실행 예제가 필요하다.
- 이 요구사항이 없으면 고객은 기능 가능 여부를 알더라도 실제 업무 적용 순서, 실패 판정, 재처리 금지 조건을 확인할 수 없다.
- 내부 구현 구조를 보여 주기 위한 예제가 아니라 고객 업무를 CPF로 만드는 방법과 운영 결과를 증명하는 예제로 구현한다.

## 고객 업무 시나리오

새 Job Pack 배포 후 오류가 나면 이전 Artifact와 Definition Version으로 복구한다.

## 구현 위치와 책임 경계

- 업무 Source: `com.cpf.education.edu.bat27` 또는 같은 의미의 기존 표준 Slice
- Test: Owner Module의 기존 Test Root 아래 `EDU-BAT-27` 식별 가능한 Class·Fixture
- SQL: 중앙 DB Vendor Pack의 기존 Install·Upgrade·Rollback·Verify·Seed 구조
- Config: 기존 Prefix·Profile·Secret 참조 방식을 유지
- 고객 업무 상태 변경과 원장 데이터는 고객 업무 Owner가 소유한다. ADM·BZA·Gateway·운영 Script가 업무 Table을 직접 수정하지 않는다.
- 공통 기능이 필요하면 기존 Public API·SPI를 우선 사용하고, 진입점이 없을 때만 실제 Consumer를 근거로 Owner Module에 확장한다.

## 입력·계약

- `artifactVersion`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `checksum`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `compatibleRuntime`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- 해당 기능에 필요한 Operation ID·업무 ID·Correlation ID·요청 Hash·사용자·권한 문맥을 저장·Log·Trace·Audit에서 연결한다.
- 요청·응답·Job Parameter·이벤트·파일 Layout 중 해당되는 계약을 OpenAPI 또는 Machine-readable Schema로 제공한다.

## 상태와 저장 근거

- 필수 상태 흐름: `UPLOADED → VERIFIED → APPROVED → DEPLOYED/ROLLED_BACK`
- 상태마다 진입 조건, 허용 조치, 재시도 가능 여부, 최종 여부, 저장 위치와 Version 규칙을 정의한다.
- 결과 미확정·부분 성공이 가능한 경우 대상별 Attempt와 상대 상태 조회 근거를 별도 저장한다.

## 정상 처리 순서

1. 입력·권한·데이터 범위·기준 Version과 선행 상태를 검증한다.
2. 상태 변경·비동기 실행인 경우 Operation과 업무 식별자를 생성하거나 기존 멱등 요청을 조회한다. 조회 전용 기능은 조회 Context와 Cursor만 사용한다.
3. 상태 변경 기능은 고객 업무 Owner의 Application Use Case에서 Transaction 경계와 상태 전이를 수행하고, 조회 전용 기능은 일관된 Snapshot·정렬·범위 조건을 적용한다.
4. 외부 시스템·Kafka·파일·Batch·Gateway가 필요한 경우 Adapter 경계에서 Timeout·Retry·보안 정책을 적용한다.
5. 업무 결과와 Attempt·Audit·Log·Metric·Trace를 같은 식별자로 연결한다.
6. 고객 API 또는 운영 화면에서 최종 상태·건수·금액·Version·오류 0건을 확인한다.

## 예외·장애·경계 사례

- checksum 불일치
- Runtime 비호환
- 일부 Agent만 적용
- 이전 파일 없음
- 상태 변경이 있는 경우 DB Commit 전 실패와 Commit 후 응답 유실을 구분한다.
- 같은 요청의 중복·동시 실행·순서 역전·늦은 응답을 포함한다.
- Retry 가능한 오류와 즉시 중단해야 하는 업무 오류를 구분한다.
- 대량·다중 대상 기능에서 부분 성공이 발생하면 성공 대상을 다시 실행하지 않고 실패 대상만 Reprocess·Reconcile한다.
- 복구·보상이 필요한 기능은 복구 실패와 보상 실패도 별도 상태·Attempt·Audit로 남긴다.

## 필수 Test

- Artifact signature
- compatibility gate
- partial apply reconcile
- 입력 Validation·권한·Data Scope·Masking Negative Test
- 해당 기능에 적용되는 중복·동시성·Timeout·응답 유실·부분 실패 Test
- 해당 기능의 Failure Injection 후 필요한 Retry·Restart·Reprocess·Reconcile·Rollback Test
- Oracle·PostgreSQL·MariaDB 또는 해당 Infra의 의미 동일성 Test
- Log·Metric·Trace·Audit에 Secret·개인정보 원문이 없는지 검사

## 운영·화면 확인

- ADM Deployment/Job Packs에서 Version·대상별 적용·Rollback 확인
- 화면에는 해당 메뉴에 실제로 존재하는 조회 조건, 기본값, Column, 상세 Field, 상태, Button 활성 조건, 사유, 승인, 예상 Version, 대상별 결과만 Source와 일치하게 표시한다. 조회 전용 메뉴에 Command 항목을 만들지 않는다.
- 운영자가 정상화 완료를 판단할 수 있는 건수·금액·Version·Checksum·최종 상태·마지막 오류를 제공한다.
- 변경·외부연계 기능에서 응답 유실이 가능한 경우에만 요청을 다시 보내기 전에 Operation·거래·상대 상태를 조회하는 절차를 Browser 또는 API Test로 검증한다.

## 고객 매뉴얼·EDU 연결

- `02_배치개발매뉴얼.md`의 해당 기능 장에 `EDU-BAT-27`를 연결한다.
- 선행 환경, 실행 명령, 입력값, 정상 출력, 오류 재현 명령, 복구 명령, ADM/BZA/Gateway 확인 위치를 그대로 따라 할 수 있게 작성한다.
- 교육 코드에서 고객 업무로 바꿀 Package·Table·API·Topic·파일명과 CPF 관리 영역을 구분한 전환표를 제공한다.

## 완료 조건

- `EDU-BAT-27`가 교육 Catalog·Source·Test·고객 매뉴얼에 동일 ID로 존재한다.
- 정상·Validation·권한·중복·동시성·Timeout·응답 유실·부분 실패·복구 Test가 통과한다.
- 요청·응답·상태·DB·이벤트·파일·운영 화면이 양방향으로 일치한다.
- 운영자가 다른 사람의 설명 없이 장애를 재현하고 정상화 판정을 할 수 있다.
- 실행하지 않은 Test는 성공으로 기록하지 않고, 실패·제약이 남으면 전체 완료로 보고하지 않는다.

## 금지 사항

- 메모리 Map·고정 JSON·정상 Case 한 건만으로 완료 처리
- 교육용 고객 업무를 `cpf-core`, `cpf-starters/common`, Starter, Runtime 제품 내부에 삽입
- Module-local DB Vendor Fallback SQL 또는 Vendor별 의미가 다른 Schema
- Timeout이나 응답 유실 뒤 실제 결과 확인 없이 무조건 재호출
- 비밀번호·Token·개인정보 원문을 Source·Log·Fixture·문서에 기록
- 매뉴얼 설명과 다른 임의 Package·API·상태·메뉴를 새로 생성

---

# 97. EDU-BAT-28 — Host Agent Offline·명령 ACK 유실

- 우선순위: **P0**
- 연결 매뉴얼: `02_배치개발매뉴얼.md`
- Owner: `cpf-education + cpf-batch public contract`
- 구현 상태 목표: Source·SQL·API·Config·Test·운영 확인·매뉴얼 연결이 모두 완료

## 필요 근거

- 고객 매뉴얼에서 `Host Agent Offline·명령 ACK 유실`을 설명하려면 정상 호출 예시만이 아니라 상태 변화와 장애 후 정상화까지 재현 가능한 실행 예제가 필요하다.
- 이 요구사항이 없으면 고객은 기능 가능 여부를 알더라도 실제 업무 적용 순서, 실패 판정, 재처리 금지 조건을 확인할 수 없다.
- 내부 구현 구조를 보여 주기 위한 예제가 아니라 고객 업무를 CPF로 만드는 방법과 운영 결과를 증명하는 예제로 구현한다.

## 고객 업무 시나리오

Agent가 기동 명령을 수행했으나 ACK가 유실되면 실제 Process 상태를 조회해 확정한다.

## 구현 위치와 책임 경계

- 업무 Source: `com.cpf.education.edu.bat28` 또는 같은 의미의 기존 표준 Slice
- Test: Owner Module의 기존 Test Root 아래 `EDU-BAT-28` 식별 가능한 Class·Fixture
- SQL: 중앙 DB Vendor Pack의 기존 Install·Upgrade·Rollback·Verify·Seed 구조
- Config: 기존 Prefix·Profile·Secret 참조 방식을 유지
- 고객 업무 상태 변경과 원장 데이터는 고객 업무 Owner가 소유한다. ADM·BZA·Gateway·운영 Script가 업무 Table을 직접 수정하지 않는다.
- 공통 기능이 필요하면 기존 Public API·SPI를 우선 사용하고, 진입점이 없을 때만 실제 Consumer를 근거로 Owner Module에 확장한다.

## 입력·계약

- `agentId`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `commandId`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `desiredState`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- 해당 기능에 필요한 Operation ID·업무 ID·Correlation ID·요청 Hash·사용자·권한 문맥을 저장·Log·Trace·Audit에서 연결한다.
- 요청·응답·Job Parameter·이벤트·파일 Layout 중 해당되는 계약을 OpenAPI 또는 Machine-readable Schema로 제공한다.

## 상태와 저장 근거

- 필수 상태 흐름: `COMMAND_SENT → ACKED/UNKNOWN_RESULT → RECONCILED`
- 상태마다 진입 조건, 허용 조치, 재시도 가능 여부, 최종 여부, 저장 위치와 Version 규칙을 정의한다.
- 결과 미확정·부분 성공이 가능한 경우 대상별 Attempt와 상대 상태 조회 근거를 별도 저장한다.

## 정상 처리 순서

1. 입력·권한·데이터 범위·기준 Version과 선행 상태를 검증한다.
2. 상태 변경·비동기 실행인 경우 Operation과 업무 식별자를 생성하거나 기존 멱등 요청을 조회한다. 조회 전용 기능은 조회 Context와 Cursor만 사용한다.
3. 상태 변경 기능은 고객 업무 Owner의 Application Use Case에서 Transaction 경계와 상태 전이를 수행하고, 조회 전용 기능은 일관된 Snapshot·정렬·범위 조건을 적용한다.
4. 외부 시스템·Kafka·파일·Batch·Gateway가 필요한 경우 Adapter 경계에서 Timeout·Retry·보안 정책을 적용한다.
5. 업무 결과와 Attempt·Audit·Log·Metric·Trace를 같은 식별자로 연결한다.
6. 고객 API 또는 운영 화면에서 최종 상태·건수·금액·Version·오류 0건을 확인한다.

## 예외·장애·경계 사례

- Agent offline
- duplicate command
- process started but ACK lost
- stale heartbeat
- 상태 변경이 있는 경우 DB Commit 전 실패와 Commit 후 응답 유실을 구분한다.
- 같은 요청의 중복·동시 실행·순서 역전·늦은 응답을 포함한다.
- Retry 가능한 오류와 즉시 중단해야 하는 업무 오류를 구분한다.
- 대량·다중 대상 기능에서 부분 성공이 발생하면 성공 대상을 다시 실행하지 않고 실패 대상만 Reprocess·Reconcile한다.
- 복구·보상이 필요한 기능은 복구 실패와 보상 실패도 별도 상태·Attempt·Audit로 남긴다.

## 필수 Test

- Command idempotency
- attempt ledger
- probe
- timeout budget
- 입력 Validation·권한·Data Scope·Masking Negative Test
- 해당 기능에 적용되는 중복·동시성·Timeout·응답 유실·부분 실패 Test
- 해당 기능의 Failure Injection 후 필요한 Retry·Restart·Reprocess·Reconcile·Rollback Test
- Oracle·PostgreSQL·MariaDB 또는 해당 Infra의 의미 동일성 Test
- Log·Metric·Trace·Audit에 Secret·개인정보 원문이 없는지 검사

## 운영·화면 확인

- ADM Agents/Runtime에서 명령·ACK·Probe·최종 상태 확인
- 화면에는 해당 메뉴에 실제로 존재하는 조회 조건, 기본값, Column, 상세 Field, 상태, Button 활성 조건, 사유, 승인, 예상 Version, 대상별 결과만 Source와 일치하게 표시한다. 조회 전용 메뉴에 Command 항목을 만들지 않는다.
- 운영자가 정상화 완료를 판단할 수 있는 건수·금액·Version·Checksum·최종 상태·마지막 오류를 제공한다.
- 변경·외부연계 기능에서 응답 유실이 가능한 경우에만 요청을 다시 보내기 전에 Operation·거래·상대 상태를 조회하는 절차를 Browser 또는 API Test로 검증한다.

## 고객 매뉴얼·EDU 연결

- `02_배치개발매뉴얼.md`의 해당 기능 장에 `EDU-BAT-28`를 연결한다.
- 선행 환경, 실행 명령, 입력값, 정상 출력, 오류 재현 명령, 복구 명령, ADM/BZA/Gateway 확인 위치를 그대로 따라 할 수 있게 작성한다.
- 교육 코드에서 고객 업무로 바꿀 Package·Table·API·Topic·파일명과 CPF 관리 영역을 구분한 전환표를 제공한다.

## 완료 조건

- `EDU-BAT-28`가 교육 Catalog·Source·Test·고객 매뉴얼에 동일 ID로 존재한다.
- 정상·Validation·권한·중복·동시성·Timeout·응답 유실·부분 실패·복구 Test가 통과한다.
- 요청·응답·상태·DB·이벤트·파일·운영 화면이 양방향으로 일치한다.
- 운영자가 다른 사람의 설명 없이 장애를 재현하고 정상화 판정을 할 수 있다.
- 실행하지 않은 Test는 성공으로 기록하지 않고, 실패·제약이 남으면 전체 완료로 보고하지 않는다.

## 금지 사항

- 메모리 Map·고정 JSON·정상 Case 한 건만으로 완료 처리
- 교육용 고객 업무를 `cpf-core`, `cpf-starters/common`, Starter, Runtime 제품 내부에 삽입
- Module-local DB Vendor Fallback SQL 또는 Vendor별 의미가 다른 Schema
- Timeout이나 응답 유실 뒤 실제 결과 확인 없이 무조건 재호출
- 비밀번호·Token·개인정보 원문을 Source·Log·Fixture·문서에 기록
- 매뉴얼 설명과 다른 임의 Package·API·상태·메뉴를 새로 생성

---

# 98. EDU-BAT-29 — Dry Run·건수 Preview·표본 확인

- 우선순위: **P0**
- 연결 매뉴얼: `02_배치개발매뉴얼.md`
- Owner: `cpf-education + cpf-batch public contract`
- 구현 상태 목표: Source·SQL·API·Config·Test·운영 확인·매뉴얼 연결이 모두 완료

## 필요 근거

- 고객 매뉴얼에서 `Dry Run·건수 Preview·표본 확인`을 설명하려면 정상 호출 예시만이 아니라 상태 변화와 장애 후 정상화까지 재현 가능한 실행 예제가 필요하다.
- 이 요구사항이 없으면 고객은 기능 가능 여부를 알더라도 실제 업무 적용 순서, 실패 판정, 재처리 금지 조건을 확인할 수 없다.
- 내부 구현 구조를 보여 주기 위한 예제가 아니라 고객 업무를 CPF로 만드는 방법과 운영 결과를 증명하는 예제로 구현한다.

## 고객 업무 시나리오

대량 변경 전 대상 건수·금액·상위 오류 표본을 조회하고 승인 후 같은 Snapshot을 실행한다.

## 구현 위치와 책임 경계

- 업무 Source: `com.cpf.education.edu.bat29` 또는 같은 의미의 기존 표준 Slice
- Test: Owner Module의 기존 Test Root 아래 `EDU-BAT-29` 식별 가능한 Class·Fixture
- SQL: 중앙 DB Vendor Pack의 기존 Install·Upgrade·Rollback·Verify·Seed 구조
- Config: 기존 Prefix·Profile·Secret 참조 방식을 유지
- 고객 업무 상태 변경과 원장 데이터는 고객 업무 Owner가 소유한다. ADM·BZA·Gateway·운영 Script가 업무 Table을 직접 수정하지 않는다.
- 공통 기능이 필요하면 기존 Public API·SPI를 우선 사용하고, 진입점이 없을 때만 실제 Consumer를 근거로 Owner Module에 확장한다.

## 입력·계약

- `criteria`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `snapshotAt`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `sampleSize`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- 해당 기능에 필요한 Operation ID·업무 ID·Correlation ID·요청 Hash·사용자·권한 문맥을 저장·Log·Trace·Audit에서 연결한다.
- 요청·응답·Job Parameter·이벤트·파일 Layout 중 해당되는 계약을 OpenAPI 또는 Machine-readable Schema로 제공한다.

## 상태와 저장 근거

- 필수 상태 흐름: `PREVIEWING → PREVIEW_READY → APPROVED → EXECUTING`
- 상태마다 진입 조건, 허용 조치, 재시도 가능 여부, 최종 여부, 저장 위치와 Version 규칙을 정의한다.
- 결과 미확정·부분 성공이 가능한 경우 대상별 Attempt와 상대 상태 조회 근거를 별도 저장한다.

## 정상 처리 순서

1. 입력·권한·데이터 범위·기준 Version과 선행 상태를 검증한다.
2. 상태 변경·비동기 실행인 경우 Operation과 업무 식별자를 생성하거나 기존 멱등 요청을 조회한다. 조회 전용 기능은 조회 Context와 Cursor만 사용한다.
3. 상태 변경 기능은 고객 업무 Owner의 Application Use Case에서 Transaction 경계와 상태 전이를 수행하고, 조회 전용 기능은 일관된 Snapshot·정렬·범위 조건을 적용한다.
4. 외부 시스템·Kafka·파일·Batch·Gateway가 필요한 경우 Adapter 경계에서 Timeout·Retry·보안 정책을 적용한다.
5. 업무 결과와 Attempt·Audit·Log·Metric·Trace를 같은 식별자로 연결한다.
6. 고객 API 또는 운영 화면에서 최종 상태·건수·금액·Version·오류 0건을 확인한다.

## 예외·장애·경계 사례

- Preview 후 데이터 변경
- 표본 개인정보
- 승인 만료
- snapshot unavailable
- 상태 변경이 있는 경우 DB Commit 전 실패와 Commit 후 응답 유실을 구분한다.
- 같은 요청의 중복·동시 실행·순서 역전·늦은 응답을 포함한다.
- Retry 가능한 오류와 즉시 중단해야 하는 업무 오류를 구분한다.
- 대량·다중 대상 기능에서 부분 성공이 발생하면 성공 대상을 다시 실행하지 않고 실패 대상만 Reprocess·Reconcile한다.
- 복구·보상이 필요한 기능은 복구 실패와 보상 실패도 별도 상태·Attempt·Audit로 남긴다.

## 필수 Test

- Snapshot pinning
- count/sum
- sampling
- approval binding
- 입력 Validation·권한·Data Scope·Masking Negative Test
- 해당 기능에 적용되는 중복·동시성·Timeout·응답 유실·부분 실패 Test
- 해당 기능의 Failure Injection 후 필요한 Retry·Restart·Reprocess·Reconcile·Rollback Test
- Oracle·PostgreSQL·MariaDB 또는 해당 Infra의 의미 동일성 Test
- Log·Metric·Trace·Audit에 Secret·개인정보 원문이 없는지 검사

## 운영·화면 확인

- ADM Center-Cut/Approvals에서 Preview Hash와 실행 Hash 일치
- 화면에는 해당 메뉴에 실제로 존재하는 조회 조건, 기본값, Column, 상세 Field, 상태, Button 활성 조건, 사유, 승인, 예상 Version, 대상별 결과만 Source와 일치하게 표시한다. 조회 전용 메뉴에 Command 항목을 만들지 않는다.
- 운영자가 정상화 완료를 판단할 수 있는 건수·금액·Version·Checksum·최종 상태·마지막 오류를 제공한다.
- 변경·외부연계 기능에서 응답 유실이 가능한 경우에만 요청을 다시 보내기 전에 Operation·거래·상대 상태를 조회하는 절차를 Browser 또는 API Test로 검증한다.

## 고객 매뉴얼·EDU 연결

- `02_배치개발매뉴얼.md`의 해당 기능 장에 `EDU-BAT-29`를 연결한다.
- 선행 환경, 실행 명령, 입력값, 정상 출력, 오류 재현 명령, 복구 명령, ADM/BZA/Gateway 확인 위치를 그대로 따라 할 수 있게 작성한다.
- 교육 코드에서 고객 업무로 바꿀 Package·Table·API·Topic·파일명과 CPF 관리 영역을 구분한 전환표를 제공한다.

## 완료 조건

- `EDU-BAT-29`가 교육 Catalog·Source·Test·고객 매뉴얼에 동일 ID로 존재한다.
- 정상·Validation·권한·중복·동시성·Timeout·응답 유실·부분 실패·복구 Test가 통과한다.
- 요청·응답·상태·DB·이벤트·파일·운영 화면이 양방향으로 일치한다.
- 운영자가 다른 사람의 설명 없이 장애를 재현하고 정상화 판정을 할 수 있다.
- 실행하지 않은 Test는 성공으로 기록하지 않고, 실패·제약이 남으면 전체 완료로 보고하지 않는다.

## 금지 사항

- 메모리 Map·고정 JSON·정상 Case 한 건만으로 완료 처리
- 교육용 고객 업무를 `cpf-core`, `cpf-starters/common`, Starter, Runtime 제품 내부에 삽입
- Module-local DB Vendor Fallback SQL 또는 Vendor별 의미가 다른 Schema
- Timeout이나 응답 유실 뒤 실제 결과 확인 없이 무조건 재호출
- 비밀번호·Token·개인정보 원문을 Source·Log·Fixture·문서에 기록
- 매뉴얼 설명과 다른 임의 Package·API·상태·메뉴를 새로 생성

---

# 99. EDU-BAT-30 — 대용량 처리 성능·용량·Backpressure

- 우선순위: **P0**
- 연결 매뉴얼: `02_배치개발매뉴얼.md`
- Owner: `cpf-education + cpf-batch public contract`
- 구현 상태 목표: Source·SQL·API·Config·Test·운영 확인·매뉴얼 연결이 모두 완료

## 필요 근거

- 고객 매뉴얼에서 `대용량 처리 성능·용량·Backpressure`을 설명하려면 정상 호출 예시만이 아니라 상태 변화와 장애 후 정상화까지 재현 가능한 실행 예제가 필요하다.
- 이 요구사항이 없으면 고객은 기능 가능 여부를 알더라도 실제 업무 적용 순서, 실패 판정, 재처리 금지 조건을 확인할 수 없다.
- 내부 구현 구조를 보여 주기 위한 예제가 아니라 고객 업무를 CPF로 만드는 방법과 운영 결과를 증명하는 예제로 구현한다.

## 고객 업무 시나리오

1억 건 처리에서 DB·Kafka·Disk 한도를 넘지 않고 처리율과 ETA를 제공한다.

## 구현 위치와 책임 경계

- 업무 Source: `com.cpf.education.edu.bat30` 또는 같은 의미의 기존 표준 Slice
- Test: Owner Module의 기존 Test Root 아래 `EDU-BAT-30` 식별 가능한 Class·Fixture
- SQL: 중앙 DB Vendor Pack의 기존 Install·Upgrade·Rollback·Verify·Seed 구조
- Config: 기존 Prefix·Profile·Secret 참조 방식을 유지
- 고객 업무 상태 변경과 원장 데이터는 고객 업무 Owner가 소유한다. ADM·BZA·Gateway·운영 Script가 업무 Table을 직접 수정하지 않는다.
- 공통 기능이 필요하면 기존 Public API·SPI를 우선 사용하고, 진입점이 없을 때만 실제 Consumer를 근거로 Owner Module에 확장한다.

## 입력·계약

- `volume`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `chunkSize`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `gridSize`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `throttleLimit`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- 해당 기능에 필요한 Operation ID·업무 ID·Correlation ID·요청 Hash·사용자·권한 문맥을 저장·Log·Trace·Audit에서 연결한다.
- 요청·응답·Job Parameter·이벤트·파일 Layout 중 해당되는 계약을 OpenAPI 또는 Machine-readable Schema로 제공한다.

## 상태와 저장 근거

- 필수 상태 흐름: `RUNNING → THROTTLED/SCALING → COMPLETED`
- 상태마다 진입 조건, 허용 조치, 재시도 가능 여부, 최종 여부, 저장 위치와 Version 규칙을 정의한다.
- 결과 미확정·부분 성공이 가능한 경우 대상별 Attempt와 상대 상태 조회 근거를 별도 저장한다.

## 정상 처리 순서

1. 입력·권한·데이터 범위·기준 Version과 선행 상태를 검증한다.
2. 상태 변경·비동기 실행인 경우 Operation과 업무 식별자를 생성하거나 기존 멱등 요청을 조회한다. 조회 전용 기능은 조회 Context와 Cursor만 사용한다.
3. 상태 변경 기능은 고객 업무 Owner의 Application Use Case에서 Transaction 경계와 상태 전이를 수행하고, 조회 전용 기능은 일관된 Snapshot·정렬·범위 조건을 적용한다.
4. 외부 시스템·Kafka·파일·Batch·Gateway가 필요한 경우 Adapter 경계에서 Timeout·Retry·보안 정책을 적용한다.
5. 업무 결과와 Attempt·Audit·Log·Metric·Trace를 같은 식별자로 연결한다.
6. 고객 API 또는 운영 화면에서 최종 상태·건수·금액·Version·오류 0건을 확인한다.

## 예외·장애·경계 사례

- DB pool 고갈
- Kafka lag
- disk full
- GC pressure
- worker 증가 역효과
- 상태 변경이 있는 경우 DB Commit 전 실패와 Commit 후 응답 유실을 구분한다.
- 같은 요청의 중복·동시 실행·순서 역전·늦은 응답을 포함한다.
- Retry 가능한 오류와 즉시 중단해야 하는 업무 오류를 구분한다.
- 대량·다중 대상 기능에서 부분 성공이 발생하면 성공 대상을 다시 실행하지 않고 실패 대상만 Reprocess·Reconcile한다.
- 복구·보상이 필요한 기능은 복구 실패와 보상 실패도 별도 상태·Attempt·Audit로 남긴다.

## 필수 Test

- Load test
- resource limits
- backpressure
- restart under load
- 입력 Validation·권한·Data Scope·Masking Negative Test
- 해당 기능에 적용되는 중복·동시성·Timeout·응답 유실·부분 실패 Test
- 해당 기능의 Failure Injection 후 필요한 Retry·Restart·Reprocess·Reconcile·Rollback Test
- Oracle·PostgreSQL·MariaDB 또는 해당 Infra의 의미 동일성 Test
- Log·Metric·Trace·Audit에 Secret·개인정보 원문이 없는지 검사

## 운영·화면 확인

- ADM Capacity/Batch Overview에서 처리율·lag·ETA·병목 확인
- 화면에는 해당 메뉴에 실제로 존재하는 조회 조건, 기본값, Column, 상세 Field, 상태, Button 활성 조건, 사유, 승인, 예상 Version, 대상별 결과만 Source와 일치하게 표시한다. 조회 전용 메뉴에 Command 항목을 만들지 않는다.
- 운영자가 정상화 완료를 판단할 수 있는 건수·금액·Version·Checksum·최종 상태·마지막 오류를 제공한다.
- 변경·외부연계 기능에서 응답 유실이 가능한 경우에만 요청을 다시 보내기 전에 Operation·거래·상대 상태를 조회하는 절차를 Browser 또는 API Test로 검증한다.

## 고객 매뉴얼·EDU 연결

- `02_배치개발매뉴얼.md`의 해당 기능 장에 `EDU-BAT-30`를 연결한다.
- 선행 환경, 실행 명령, 입력값, 정상 출력, 오류 재현 명령, 복구 명령, ADM/BZA/Gateway 확인 위치를 그대로 따라 할 수 있게 작성한다.
- 교육 코드에서 고객 업무로 바꿀 Package·Table·API·Topic·파일명과 CPF 관리 영역을 구분한 전환표를 제공한다.

## 완료 조건

- `EDU-BAT-30`가 교육 Catalog·Source·Test·고객 매뉴얼에 동일 ID로 존재한다.
- 정상·Validation·권한·중복·동시성·Timeout·응답 유실·부분 실패·복구 Test가 통과한다.
- 요청·응답·상태·DB·이벤트·파일·운영 화면이 양방향으로 일치한다.
- 운영자가 다른 사람의 설명 없이 장애를 재현하고 정상화 판정을 할 수 있다.
- 실행하지 않은 Test는 성공으로 기록하지 않고, 실패·제약이 남으면 전체 완료로 보고하지 않는다.

## 금지 사항

- 메모리 Map·고정 JSON·정상 Case 한 건만으로 완료 처리
- 교육용 고객 업무를 `cpf-core`, `cpf-starters/common`, Starter, Runtime 제품 내부에 삽입
- Module-local DB Vendor Fallback SQL 또는 Vendor별 의미가 다른 Schema
- Timeout이나 응답 유실 뒤 실제 결과 확인 없이 무조건 재호출
- 비밀번호·Token·개인정보 원문을 Source·Log·Fixture·문서에 기록
- 매뉴얼 설명과 다른 임의 Package·API·상태·메뉴를 새로 생성

---

# 100. EDU-ADM-08 — 권한·데이터 범위·Masking·사유 입력 연동

- 우선순위: **P0**
- 연결 매뉴얼: `03_ADM개발자매뉴얼.md·04_ADM운영자매뉴얼.md`
- Owner: `고객 업무 Owner + cpf-admin`
- 구현 상태 목표: Source·SQL·API·Config·Test·운영 확인·매뉴얼 연결이 모두 완료

## 필요 근거

- 고객 매뉴얼에서 `권한·데이터 범위·Masking·사유 입력 연동`을 설명하려면 정상 호출 예시만이 아니라 상태 변화와 장애 후 정상화까지 재현 가능한 실행 예제가 필요하다.
- 이 요구사항이 없으면 고객은 기능 가능 여부를 알더라도 실제 업무 적용 순서, 실패 판정, 재처리 금지 조건을 확인할 수 없다.
- 내부 구현 구조를 보여 주기 위한 예제가 아니라 고객 업무를 CPF로 만드는 방법과 운영 결과를 증명하는 예제로 구현한다.

## 고객 업무 시나리오

같은 고객 조회 화면에서 상담원은 Masking된 자기 지점 데이터만, 보안담당자는 승인된 사유로 원문을 본다.

## 구현 위치와 책임 경계

- 업무 Source: `com.cpf.education.edu.adm08` 또는 같은 의미의 기존 표준 Slice
- Test: Owner Module의 기존 Test Root 아래 `EDU-ADM-08` 식별 가능한 Class·Fixture
- SQL: 중앙 DB Vendor Pack의 기존 Install·Upgrade·Rollback·Verify·Seed 구조
- Config: 기존 Prefix·Profile·Secret 참조 방식을 유지
- 고객 업무 상태 변경과 원장 데이터는 고객 업무 Owner가 소유한다. ADM·BZA·Gateway·운영 Script가 업무 Table을 직접 수정하지 않는다.
- 공통 기능이 필요하면 기존 Public API·SPI를 우선 사용하고, 진입점이 없을 때만 실제 Consumer를 근거로 Owner Module에 확장한다.

## 입력·계약

- `permission`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `dataScope`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `reason`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `approvalId`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- 해당 기능에 필요한 Operation ID·업무 ID·Correlation ID·요청 Hash·사용자·권한 문맥을 저장·Log·Trace·Audit에서 연결한다.
- 요청·응답·Job Parameter·이벤트·파일 Layout 중 해당되는 계약을 OpenAPI 또는 Machine-readable Schema로 제공한다.

## 상태와 저장 근거

- 필수 상태 흐름: `AUTHORIZED/MASKED 또는 DENIED; UNMASK_REQUESTED → APPROVED`
- 상태마다 진입 조건, 허용 조치, 재시도 가능 여부, 최종 여부, 저장 위치와 Version 규칙을 정의한다.
- 결과 미확정·부분 성공이 가능한 경우 대상별 Attempt와 상대 상태 조회 근거를 별도 저장한다.

## 정상 처리 순서

1. 입력·권한·데이터 범위·기준 Version과 선행 상태를 검증한다.
2. 상태 변경·비동기 실행인 경우 Operation과 업무 식별자를 생성하거나 기존 멱등 요청을 조회한다. 조회 전용 기능은 조회 Context와 Cursor만 사용한다.
3. 상태 변경 기능은 고객 업무 Owner의 Application Use Case에서 Transaction 경계와 상태 전이를 수행하고, 조회 전용 기능은 일관된 Snapshot·정렬·범위 조건을 적용한다.
4. 외부 시스템·Kafka·파일·Batch·Gateway가 필요한 경우 Adapter 경계에서 Timeout·Retry·보안 정책을 적용한다.
5. 업무 결과와 Attempt·Audit·Log·Metric·Trace를 같은 식별자로 연결한다.
6. 고객 API 또는 운영 화면에서 최종 상태·건수·금액·Version·오류 0건을 확인한다.

## 예외·장애·경계 사례

- 권한 변경 직후
- scope 누락
- 원문 Export
- reason 길이
- 승인 만료
- 상태 변경이 있는 경우 DB Commit 전 실패와 Commit 후 응답 유실을 구분한다.
- 같은 요청의 중복·동시 실행·순서 역전·늦은 응답을 포함한다.
- Retry 가능한 오류와 즉시 중단해야 하는 업무 오류를 구분한다.
- 대량·다중 대상 기능에서 부분 성공이 발생하면 성공 대상을 다시 실행하지 않고 실패 대상만 Reprocess·Reconcile한다.
- 복구·보상이 필요한 기능은 복구 실패와 보상 실패도 별도 상태·Attempt·Audit로 남긴다.

## 필수 Test

- Backend authorization
- browser role matrix
- audit
- IDOR
- 입력 Validation·권한·Data Scope·Masking Negative Test
- 해당 기능에 적용되는 중복·동시성·Timeout·응답 유실·부분 실패 Test
- 해당 기능의 Failure Injection 후 필요한 Retry·Restart·Reprocess·Reconcile·Rollback Test
- Oracle·PostgreSQL·MariaDB 또는 해당 Infra의 의미 동일성 Test
- Log·Metric·Trace·Audit에 Secret·개인정보 원문이 없는지 검사

## 운영·화면 확인

- ADM 화면의 Column·상세 Field·Masking·Reason·Audit 확인
- 화면에는 해당 메뉴에 실제로 존재하는 조회 조건, 기본값, Column, 상세 Field, 상태, Button 활성 조건, 사유, 승인, 예상 Version, 대상별 결과만 Source와 일치하게 표시한다. 조회 전용 메뉴에 Command 항목을 만들지 않는다.
- 운영자가 정상화 완료를 판단할 수 있는 건수·금액·Version·Checksum·최종 상태·마지막 오류를 제공한다.
- 변경·외부연계 기능에서 응답 유실이 가능한 경우에만 요청을 다시 보내기 전에 Operation·거래·상대 상태를 조회하는 절차를 Browser 또는 API Test로 검증한다.

## 고객 매뉴얼·EDU 연결

- `03_ADM개발자매뉴얼.md·04_ADM운영자매뉴얼.md`의 해당 기능 장에 `EDU-ADM-08`를 연결한다.
- 선행 환경, 실행 명령, 입력값, 정상 출력, 오류 재현 명령, 복구 명령, ADM/BZA/Gateway 확인 위치를 그대로 따라 할 수 있게 작성한다.
- 교육 코드에서 고객 업무로 바꿀 Package·Table·API·Topic·파일명과 CPF 관리 영역을 구분한 전환표를 제공한다.

## 완료 조건

- `EDU-ADM-08`가 교육 Catalog·Source·Test·고객 매뉴얼에 동일 ID로 존재한다.
- 정상·Validation·권한·중복·동시성·Timeout·응답 유실·부분 실패·복구 Test가 통과한다.
- 요청·응답·상태·DB·이벤트·파일·운영 화면이 양방향으로 일치한다.
- 운영자가 다른 사람의 설명 없이 장애를 재현하고 정상화 판정을 할 수 있다.
- 실행하지 않은 Test는 성공으로 기록하지 않고, 실패·제약이 남으면 전체 완료로 보고하지 않는다.

## 금지 사항

- 메모리 Map·고정 JSON·정상 Case 한 건만으로 완료 처리
- 교육용 고객 업무를 `cpf-core`, `cpf-starters/common`, Starter, Runtime 제품 내부에 삽입
- Module-local DB Vendor Fallback SQL 또는 Vendor별 의미가 다른 Schema
- Timeout이나 응답 유실 뒤 실제 결과 확인 없이 무조건 재호출
- 비밀번호·Token·개인정보 원문을 Source·Log·Fixture·문서에 기록
- 매뉴얼 설명과 다른 임의 Package·API·상태·메뉴를 새로 생성

---

# 101. EDU-ADM-09 — Expected Version 충돌 화면·재조회·재적용

- 우선순위: **P0**
- 연결 매뉴얼: `03_ADM개발자매뉴얼.md·04_ADM운영자매뉴얼.md`
- Owner: `고객 업무 Owner + cpf-admin`
- 구현 상태 목표: Source·SQL·API·Config·Test·운영 확인·매뉴얼 연결이 모두 완료

## 필요 근거

- 고객 매뉴얼에서 `Expected Version 충돌 화면·재조회·재적용`을 설명하려면 정상 호출 예시만이 아니라 상태 변화와 장애 후 정상화까지 재현 가능한 실행 예제가 필요하다.
- 이 요구사항이 없으면 고객은 기능 가능 여부를 알더라도 실제 업무 적용 순서, 실패 판정, 재처리 금지 조건을 확인할 수 없다.
- 내부 구현 구조를 보여 주기 위한 예제가 아니라 고객 업무를 CPF로 만드는 방법과 운영 결과를 증명하는 예제로 구현한다.

## 고객 업무 시나리오

운영자가 오래된 화면에서 상태를 바꾸면 409를 받고 최신값을 비교한 뒤 재입력한다.

## 구현 위치와 책임 경계

- 업무 Source: `com.cpf.education.edu.adm09` 또는 같은 의미의 기존 표준 Slice
- Test: Owner Module의 기존 Test Root 아래 `EDU-ADM-09` 식별 가능한 Class·Fixture
- SQL: 중앙 DB Vendor Pack의 기존 Install·Upgrade·Rollback·Verify·Seed 구조
- Config: 기존 Prefix·Profile·Secret 참조 방식을 유지
- 고객 업무 상태 변경과 원장 데이터는 고객 업무 Owner가 소유한다. ADM·BZA·Gateway·운영 Script가 업무 Table을 직접 수정하지 않는다.
- 공통 기능이 필요하면 기존 Public API·SPI를 우선 사용하고, 진입점이 없을 때만 실제 Consumer를 근거로 Owner Module에 확장한다.

## 입력·계약

- `resourceId`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `expectedVersion`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `action`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `reason`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- 해당 기능에 필요한 Operation ID·업무 ID·Correlation ID·요청 Hash·사용자·권한 문맥을 저장·Log·Trace·Audit에서 연결한다.
- 요청·응답·Job Parameter·이벤트·파일 Layout 중 해당되는 계약을 OpenAPI 또는 Machine-readable Schema로 제공한다.

## 상태와 저장 근거

- 필수 상태 흐름: `EDITING → CONFLICT → RELOADED → RESUBMITTED/ABORTED`
- 상태마다 진입 조건, 허용 조치, 재시도 가능 여부, 최종 여부, 저장 위치와 Version 규칙을 정의한다.
- 결과 미확정·부분 성공이 가능한 경우 대상별 Attempt와 상대 상태 조회 근거를 별도 저장한다.

## 정상 처리 순서

1. 입력·권한·데이터 범위·기준 Version과 선행 상태를 검증한다.
2. 상태 변경·비동기 실행인 경우 Operation과 업무 식별자를 생성하거나 기존 멱등 요청을 조회한다. 조회 전용 기능은 조회 Context와 Cursor만 사용한다.
3. 상태 변경 기능은 고객 업무 Owner의 Application Use Case에서 Transaction 경계와 상태 전이를 수행하고, 조회 전용 기능은 일관된 Snapshot·정렬·범위 조건을 적용한다.
4. 외부 시스템·Kafka·파일·Batch·Gateway가 필요한 경우 Adapter 경계에서 Timeout·Retry·보안 정책을 적용한다.
5. 업무 결과와 Attempt·Audit·Log·Metric·Trace를 같은 식별자로 연결한다.
6. 고객 API 또는 운영 화면에서 최종 상태·건수·금액·Version·오류 0건을 확인한다.

## 예외·장애·경계 사례

- 동시 운영자
- 자동 재시도 금지
- 일부 입력 보존
- 권한 변경
- 상태 변경이 있는 경우 DB Commit 전 실패와 Commit 후 응답 유실을 구분한다.
- 같은 요청의 중복·동시 실행·순서 역전·늦은 응답을 포함한다.
- Retry 가능한 오류와 즉시 중단해야 하는 업무 오류를 구분한다.
- 대량·다중 대상 기능에서 부분 성공이 발생하면 성공 대상을 다시 실행하지 않고 실패 대상만 Reprocess·Reconcile한다.
- 복구·보상이 필요한 기능은 복구 실패와 보상 실패도 별도 상태·Attempt·Audit로 남긴다.

## 필수 Test

- 409 contract
- diff view
- no blind retry
- audit
- 입력 Validation·권한·Data Scope·Masking Negative Test
- 해당 기능에 적용되는 중복·동시성·Timeout·응답 유실·부분 실패 Test
- 해당 기능의 Failure Injection 후 필요한 Retry·Restart·Reprocess·Reconcile·Rollback Test
- Oracle·PostgreSQL·MariaDB 또는 해당 Infra의 의미 동일성 Test
- Log·Metric·Trace·Audit에 Secret·개인정보 원문이 없는지 검사

## 운영·화면 확인

- ADM 실제 Button 활성 조건과 충돌 안내 Browser Test
- 화면에는 해당 메뉴에 실제로 존재하는 조회 조건, 기본값, Column, 상세 Field, 상태, Button 활성 조건, 사유, 승인, 예상 Version, 대상별 결과만 Source와 일치하게 표시한다. 조회 전용 메뉴에 Command 항목을 만들지 않는다.
- 운영자가 정상화 완료를 판단할 수 있는 건수·금액·Version·Checksum·최종 상태·마지막 오류를 제공한다.
- 변경·외부연계 기능에서 응답 유실이 가능한 경우에만 요청을 다시 보내기 전에 Operation·거래·상대 상태를 조회하는 절차를 Browser 또는 API Test로 검증한다.

## 고객 매뉴얼·EDU 연결

- `03_ADM개발자매뉴얼.md·04_ADM운영자매뉴얼.md`의 해당 기능 장에 `EDU-ADM-09`를 연결한다.
- 선행 환경, 실행 명령, 입력값, 정상 출력, 오류 재현 명령, 복구 명령, ADM/BZA/Gateway 확인 위치를 그대로 따라 할 수 있게 작성한다.
- 교육 코드에서 고객 업무로 바꿀 Package·Table·API·Topic·파일명과 CPF 관리 영역을 구분한 전환표를 제공한다.

## 완료 조건

- `EDU-ADM-09`가 교육 Catalog·Source·Test·고객 매뉴얼에 동일 ID로 존재한다.
- 정상·Validation·권한·중복·동시성·Timeout·응답 유실·부분 실패·복구 Test가 통과한다.
- 요청·응답·상태·DB·이벤트·파일·운영 화면이 양방향으로 일치한다.
- 운영자가 다른 사람의 설명 없이 장애를 재현하고 정상화 판정을 할 수 있다.
- 실행하지 않은 Test는 성공으로 기록하지 않고, 실패·제약이 남으면 전체 완료로 보고하지 않는다.

## 금지 사항

- 메모리 Map·고정 JSON·정상 Case 한 건만으로 완료 처리
- 교육용 고객 업무를 `cpf-core`, `cpf-starters/common`, Starter, Runtime 제품 내부에 삽입
- Module-local DB Vendor Fallback SQL 또는 Vendor별 의미가 다른 Schema
- Timeout이나 응답 유실 뒤 실제 결과 확인 없이 무조건 재호출
- 비밀번호·Token·개인정보 원문을 Source·Log·Fixture·문서에 기록
- 매뉴얼 설명과 다른 임의 Package·API·상태·메뉴를 새로 생성

---

# 102. EDU-ADM-10 — 대상 일괄 조치·부분 성공·결과 파일

- 우선순위: **P0**
- 연결 매뉴얼: `03_ADM개발자매뉴얼.md·04_ADM운영자매뉴얼.md`
- Owner: `고객 업무 Owner + cpf-admin`
- 구현 상태 목표: Source·SQL·API·Config·Test·운영 확인·매뉴얼 연결이 모두 완료

## 필요 근거

- 고객 매뉴얼에서 `대상 일괄 조치·부분 성공·결과 파일`을 설명하려면 정상 호출 예시만이 아니라 상태 변화와 장애 후 정상화까지 재현 가능한 실행 예제가 필요하다.
- 이 요구사항이 없으면 고객은 기능 가능 여부를 알더라도 실제 업무 적용 순서, 실패 판정, 재처리 금지 조건을 확인할 수 없다.
- 내부 구현 구조를 보여 주기 위한 예제가 아니라 고객 업무를 CPF로 만드는 방법과 운영 결과를 증명하는 예제로 구현한다.

## 고객 업무 시나리오

100개 인스턴스 설정 변경에서 97개 성공·3개 실패를 대상별로 보여주고 실패만 재처리한다.

## 구현 위치와 책임 경계

- 업무 Source: `com.cpf.education.edu.adm10` 또는 같은 의미의 기존 표준 Slice
- Test: Owner Module의 기존 Test Root 아래 `EDU-ADM-10` 식별 가능한 Class·Fixture
- SQL: 중앙 DB Vendor Pack의 기존 Install·Upgrade·Rollback·Verify·Seed 구조
- Config: 기존 Prefix·Profile·Secret 참조 방식을 유지
- 고객 업무 상태 변경과 원장 데이터는 고객 업무 Owner가 소유한다. ADM·BZA·Gateway·운영 Script가 업무 Table을 직접 수정하지 않는다.
- 공통 기능이 필요하면 기존 Public API·SPI를 우선 사용하고, 진입점이 없을 때만 실제 Consumer를 근거로 Owner Module에 확장한다.

## 입력·계약

- `targetIds`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `command`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `expectedVersions`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `reason`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- 해당 기능에 필요한 Operation ID·업무 ID·Correlation ID·요청 Hash·사용자·권한 문맥을 저장·Log·Trace·Audit에서 연결한다.
- 요청·응답·Job Parameter·이벤트·파일 Layout 중 해당되는 계약을 OpenAPI 또는 Machine-readable Schema로 제공한다.

## 상태와 저장 근거

- 필수 상태 흐름: `REQUESTED → RUNNING → PARTIAL → REPROCESSING → COMPLETED`
- 상태마다 진입 조건, 허용 조치, 재시도 가능 여부, 최종 여부, 저장 위치와 Version 규칙을 정의한다.
- 결과 미확정·부분 성공이 가능한 경우 대상별 Attempt와 상대 상태 조회 근거를 별도 저장한다.

## 정상 처리 순서

1. 입력·권한·데이터 범위·기준 Version과 선행 상태를 검증한다.
2. 상태 변경·비동기 실행인 경우 Operation과 업무 식별자를 생성하거나 기존 멱등 요청을 조회한다. 조회 전용 기능은 조회 Context와 Cursor만 사용한다.
3. 상태 변경 기능은 고객 업무 Owner의 Application Use Case에서 Transaction 경계와 상태 전이를 수행하고, 조회 전용 기능은 일관된 Snapshot·정렬·범위 조건을 적용한다.
4. 외부 시스템·Kafka·파일·Batch·Gateway가 필요한 경우 Adapter 경계에서 Timeout·Retry·보안 정책을 적용한다.
5. 업무 결과와 Attempt·Audit·Log·Metric·Trace를 같은 식별자로 연결한다.
6. 고객 API 또는 운영 화면에서 최종 상태·건수·금액·Version·오류 0건을 확인한다.

## 예외·장애·경계 사례

- 대상 중복
- Version 혼합
- 응답 유실
- 재처리 중 성공 대상 포함
- 상태 변경이 있는 경우 DB Commit 전 실패와 Commit 후 응답 유실을 구분한다.
- 같은 요청의 중복·동시 실행·순서 역전·늦은 응답을 포함한다.
- Retry 가능한 오류와 즉시 중단해야 하는 업무 오류를 구분한다.
- 대량·다중 대상 기능에서 부분 성공이 발생하면 성공 대상을 다시 실행하지 않고 실패 대상만 Reprocess·Reconcile한다.
- 복구·보상이 필요한 기능은 복구 실패와 보상 실패도 별도 상태·Attempt·Audit로 남긴다.

## 필수 Test

- Target result contract
- downloadable result
- idempotency
- 입력 Validation·권한·Data Scope·Masking Negative Test
- 해당 기능에 적용되는 중복·동시성·Timeout·응답 유실·부분 실패 Test
- 해당 기능의 Failure Injection 후 필요한 Retry·Restart·Reprocess·Reconcile·Rollback Test
- Oracle·PostgreSQL·MariaDB 또는 해당 Infra의 의미 동일성 Test
- Log·Metric·Trace·Audit에 Secret·개인정보 원문이 없는지 검사

## 운영·화면 확인

- ADM 대상별 상태·재처리 선택·감사 Browser Test
- 화면에는 해당 메뉴에 실제로 존재하는 조회 조건, 기본값, Column, 상세 Field, 상태, Button 활성 조건, 사유, 승인, 예상 Version, 대상별 결과만 Source와 일치하게 표시한다. 조회 전용 메뉴에 Command 항목을 만들지 않는다.
- 운영자가 정상화 완료를 판단할 수 있는 건수·금액·Version·Checksum·최종 상태·마지막 오류를 제공한다.
- 변경·외부연계 기능에서 응답 유실이 가능한 경우에만 요청을 다시 보내기 전에 Operation·거래·상대 상태를 조회하는 절차를 Browser 또는 API Test로 검증한다.

## 고객 매뉴얼·EDU 연결

- `03_ADM개발자매뉴얼.md·04_ADM운영자매뉴얼.md`의 해당 기능 장에 `EDU-ADM-10`를 연결한다.
- 선행 환경, 실행 명령, 입력값, 정상 출력, 오류 재현 명령, 복구 명령, ADM/BZA/Gateway 확인 위치를 그대로 따라 할 수 있게 작성한다.
- 교육 코드에서 고객 업무로 바꿀 Package·Table·API·Topic·파일명과 CPF 관리 영역을 구분한 전환표를 제공한다.

## 완료 조건

- `EDU-ADM-10`가 교육 Catalog·Source·Test·고객 매뉴얼에 동일 ID로 존재한다.
- 정상·Validation·권한·중복·동시성·Timeout·응답 유실·부분 실패·복구 Test가 통과한다.
- 요청·응답·상태·DB·이벤트·파일·운영 화면이 양방향으로 일치한다.
- 운영자가 다른 사람의 설명 없이 장애를 재현하고 정상화 판정을 할 수 있다.
- 실행하지 않은 Test는 성공으로 기록하지 않고, 실패·제약이 남으면 전체 완료로 보고하지 않는다.

## 금지 사항

- 메모리 Map·고정 JSON·정상 Case 한 건만으로 완료 처리
- 교육용 고객 업무를 `cpf-core`, `cpf-starters/common`, Starter, Runtime 제품 내부에 삽입
- Module-local DB Vendor Fallback SQL 또는 Vendor별 의미가 다른 Schema
- Timeout이나 응답 유실 뒤 실제 결과 확인 없이 무조건 재호출
- 비밀번호·Token·개인정보 원문을 Source·Log·Fixture·문서에 기록
- 매뉴얼 설명과 다른 임의 Package·API·상태·메뉴를 새로 생성

---

# 103. EDU-ADM-11 — 설정·기능전환·유지보수 창 운영

- 우선순위: **P0**
- 연결 매뉴얼: `03_ADM개발자매뉴얼.md·04_ADM운영자매뉴얼.md`
- Owner: `고객 업무 Owner + cpf-admin`
- 구현 상태 목표: Source·SQL·API·Config·Test·운영 확인·매뉴얼 연결이 모두 완료

## 필요 근거

- 고객 매뉴얼에서 `설정·기능전환·유지보수 창 운영`을 설명하려면 정상 호출 예시만이 아니라 상태 변화와 장애 후 정상화까지 재현 가능한 실행 예제가 필요하다.
- 이 요구사항이 없으면 고객은 기능 가능 여부를 알더라도 실제 업무 적용 순서, 실패 판정, 재처리 금지 조건을 확인할 수 없다.
- 내부 구현 구조를 보여 주기 위한 예제가 아니라 고객 업무를 CPF로 만드는 방법과 운영 결과를 증명하는 예제로 구현한다.

## 고객 업무 시나리오

운영자가 변경 Preview를 보고 승인받아 기능을 켜고 일부 인스턴스 적용 실패를 복구한다.

## 구현 위치와 책임 경계

- 업무 Source: `com.cpf.education.edu.adm11` 또는 같은 의미의 기존 표준 Slice
- Test: Owner Module의 기존 Test Root 아래 `EDU-ADM-11` 식별 가능한 Class·Fixture
- SQL: 중앙 DB Vendor Pack의 기존 Install·Upgrade·Rollback·Verify·Seed 구조
- Config: 기존 Prefix·Profile·Secret 참조 방식을 유지
- 고객 업무 상태 변경과 원장 데이터는 고객 업무 Owner가 소유한다. ADM·BZA·Gateway·운영 Script가 업무 Table을 직접 수정하지 않는다.
- 공통 기능이 필요하면 기존 Public API·SPI를 우선 사용하고, 진입점이 없을 때만 실제 Consumer를 근거로 Owner Module에 확장한다.

## 입력·계약

- `configVersion`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `targets`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `maintenanceWindow`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `reason`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- 해당 기능에 필요한 Operation ID·업무 ID·Correlation ID·요청 Hash·사용자·권한 문맥을 저장·Log·Trace·Audit에서 연결한다.
- 요청·응답·Job Parameter·이벤트·파일 Layout 중 해당되는 계약을 OpenAPI 또는 Machine-readable Schema로 제공한다.

## 상태와 저장 근거

- 필수 상태 흐름: `DRAFT → VALIDATED → APPROVED → APPLYING → APPLIED/PARTIAL/ROLLED_BACK`
- 상태마다 진입 조건, 허용 조치, 재시도 가능 여부, 최종 여부, 저장 위치와 Version 규칙을 정의한다.
- 결과 미확정·부분 성공이 가능한 경우 대상별 Attempt와 상대 상태 조회 근거를 별도 저장한다.

## 정상 처리 순서

1. 입력·권한·데이터 범위·기준 Version과 선행 상태를 검증한다.
2. 상태 변경·비동기 실행인 경우 Operation과 업무 식별자를 생성하거나 기존 멱등 요청을 조회한다. 조회 전용 기능은 조회 Context와 Cursor만 사용한다.
3. 상태 변경 기능은 고객 업무 Owner의 Application Use Case에서 Transaction 경계와 상태 전이를 수행하고, 조회 전용 기능은 일관된 Snapshot·정렬·범위 조건을 적용한다.
4. 외부 시스템·Kafka·파일·Batch·Gateway가 필요한 경우 Adapter 경계에서 Timeout·Retry·보안 정책을 적용한다.
5. 업무 결과와 Attempt·Audit·Log·Metric·Trace를 같은 식별자로 연결한다.
6. 고객 API 또는 운영 화면에서 최종 상태·건수·금액·Version·오류 0건을 확인한다.

## 예외·장애·경계 사례

- 창 밖 적용
- checksum drift
- restart required
- stale version
- 상태 변경이 있는 경우 DB Commit 전 실패와 Commit 후 응답 유실을 구분한다.
- 같은 요청의 중복·동시 실행·순서 역전·늦은 응답을 포함한다.
- Retry 가능한 오류와 즉시 중단해야 하는 업무 오류를 구분한다.
- 대량·다중 대상 기능에서 부분 성공이 발생하면 성공 대상을 다시 실행하지 않고 실패 대상만 Reprocess·Reconcile한다.
- 복구·보상이 필요한 기능은 복구 실패와 보상 실패도 별도 상태·Attempt·Audit로 남긴다.

## 필수 Test

- Validate/apply/ack
- partial reconcile
- LKG rollback
- 입력 Validation·권한·Data Scope·Masking Negative Test
- 해당 기능에 적용되는 중복·동시성·Timeout·응답 유실·부분 실패 Test
- 해당 기능의 Failure Injection 후 필요한 Retry·Restart·Reprocess·Reconcile·Rollback Test
- Oracle·PostgreSQL·MariaDB 또는 해당 Infra의 의미 동일성 Test
- Log·Metric·Trace·Audit에 Secret·개인정보 원문이 없는지 검사

## 운영·화면 확인

- Configs·Maintenance·Feature 관련 메뉴 E2E
- 화면에는 해당 메뉴에 실제로 존재하는 조회 조건, 기본값, Column, 상세 Field, 상태, Button 활성 조건, 사유, 승인, 예상 Version, 대상별 결과만 Source와 일치하게 표시한다. 조회 전용 메뉴에 Command 항목을 만들지 않는다.
- 운영자가 정상화 완료를 판단할 수 있는 건수·금액·Version·Checksum·최종 상태·마지막 오류를 제공한다.
- 변경·외부연계 기능에서 응답 유실이 가능한 경우에만 요청을 다시 보내기 전에 Operation·거래·상대 상태를 조회하는 절차를 Browser 또는 API Test로 검증한다.

## 고객 매뉴얼·EDU 연결

- `03_ADM개발자매뉴얼.md·04_ADM운영자매뉴얼.md`의 해당 기능 장에 `EDU-ADM-11`를 연결한다.
- 선행 환경, 실행 명령, 입력값, 정상 출력, 오류 재현 명령, 복구 명령, ADM/BZA/Gateway 확인 위치를 그대로 따라 할 수 있게 작성한다.
- 교육 코드에서 고객 업무로 바꿀 Package·Table·API·Topic·파일명과 CPF 관리 영역을 구분한 전환표를 제공한다.

## 완료 조건

- `EDU-ADM-11`가 교육 Catalog·Source·Test·고객 매뉴얼에 동일 ID로 존재한다.
- 정상·Validation·권한·중복·동시성·Timeout·응답 유실·부분 실패·복구 Test가 통과한다.
- 요청·응답·상태·DB·이벤트·파일·운영 화면이 양방향으로 일치한다.
- 운영자가 다른 사람의 설명 없이 장애를 재현하고 정상화 판정을 할 수 있다.
- 실행하지 않은 Test는 성공으로 기록하지 않고, 실패·제약이 남으면 전체 완료로 보고하지 않는다.

## 금지 사항

- 메모리 Map·고정 JSON·정상 Case 한 건만으로 완료 처리
- 교육용 고객 업무를 `cpf-core`, `cpf-starters/common`, Starter, Runtime 제품 내부에 삽입
- Module-local DB Vendor Fallback SQL 또는 Vendor별 의미가 다른 Schema
- Timeout이나 응답 유실 뒤 실제 결과 확인 없이 무조건 재호출
- 비밀번호·Token·개인정보 원문을 Source·Log·Fixture·문서에 기록
- 매뉴얼 설명과 다른 임의 Package·API·상태·메뉴를 새로 생성

---

# 104. EDU-ADM-12 — Incident·Recovery Center 종단간 복구

- 우선순위: **P0**
- 연결 매뉴얼: `03_ADM개발자매뉴얼.md·04_ADM운영자매뉴얼.md`
- Owner: `고객 업무 Owner + cpf-admin`
- 구현 상태 목표: Source·SQL·API·Config·Test·운영 확인·매뉴얼 연결이 모두 완료

## 필요 근거

- 고객 매뉴얼에서 `Incident·Recovery Center 종단간 복구`을 설명하려면 정상 호출 예시만이 아니라 상태 변화와 장애 후 정상화까지 재현 가능한 실행 예제가 필요하다.
- 이 요구사항이 없으면 고객은 기능 가능 여부를 알더라도 실제 업무 적용 순서, 실패 판정, 재처리 금지 조건을 확인할 수 없다.
- 내부 구현 구조를 보여 주기 위한 예제가 아니라 고객 업무를 CPF로 만드는 방법과 운영 결과를 증명하는 예제로 구현한다.

## 고객 업무 시나리오

경보에서 Incident를 만들고 영향 거래를 식별해 Reconcile·보상·종료까지 수행한다.

## 구현 위치와 책임 경계

- 업무 Source: `com.cpf.education.edu.adm12` 또는 같은 의미의 기존 표준 Slice
- Test: Owner Module의 기존 Test Root 아래 `EDU-ADM-12` 식별 가능한 Class·Fixture
- SQL: 중앙 DB Vendor Pack의 기존 Install·Upgrade·Rollback·Verify·Seed 구조
- Config: 기존 Prefix·Profile·Secret 참조 방식을 유지
- 고객 업무 상태 변경과 원장 데이터는 고객 업무 Owner가 소유한다. ADM·BZA·Gateway·운영 Script가 업무 Table을 직접 수정하지 않는다.
- 공통 기능이 필요하면 기존 Public API·SPI를 우선 사용하고, 진입점이 없을 때만 실제 Consumer를 근거로 Owner Module에 확장한다.

## 입력·계약

- `incidentId`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `transactionIds`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `severity`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `owner`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- 해당 기능에 필요한 Operation ID·업무 ID·Correlation ID·요청 Hash·사용자·권한 문맥을 저장·Log·Trace·Audit에서 연결한다.
- 요청·응답·Job Parameter·이벤트·파일 Layout 중 해당되는 계약을 OpenAPI 또는 Machine-readable Schema로 제공한다.

## 상태와 저장 근거

- 필수 상태 흐름: `OPEN → INVESTIGATING → MITIGATING → RECOVERED → CLOSED`
- 상태마다 진입 조건, 허용 조치, 재시도 가능 여부, 최종 여부, 저장 위치와 Version 규칙을 정의한다.
- 결과 미확정·부분 성공이 가능한 경우 대상별 Attempt와 상대 상태 조회 근거를 별도 저장한다.

## 정상 처리 순서

1. 입력·권한·데이터 범위·기준 Version과 선행 상태를 검증한다.
2. 상태 변경·비동기 실행인 경우 Operation과 업무 식별자를 생성하거나 기존 멱등 요청을 조회한다. 조회 전용 기능은 조회 Context와 Cursor만 사용한다.
3. 상태 변경 기능은 고객 업무 Owner의 Application Use Case에서 Transaction 경계와 상태 전이를 수행하고, 조회 전용 기능은 일관된 Snapshot·정렬·범위 조건을 적용한다.
4. 외부 시스템·Kafka·파일·Batch·Gateway가 필요한 경우 Adapter 경계에서 Timeout·Retry·보안 정책을 적용한다.
5. 업무 결과와 Attempt·Audit·Log·Metric·Trace를 같은 식별자로 연결한다.
6. 고객 API 또는 운영 화면에서 최종 상태·건수·금액·Version·오류 0건을 확인한다.

## 예외·장애·경계 사례

- 중복 Incident
- owner 교대
- 복구 후 재발
- 증적 누락
- 상태 변경이 있는 경우 DB Commit 전 실패와 Commit 후 응답 유실을 구분한다.
- 같은 요청의 중복·동시 실행·순서 역전·늦은 응답을 포함한다.
- Retry 가능한 오류와 즉시 중단해야 하는 업무 오류를 구분한다.
- 대량·다중 대상 기능에서 부분 성공이 발생하면 성공 대상을 다시 실행하지 않고 실패 대상만 Reprocess·Reconcile한다.
- 복구·보상이 필요한 기능은 복구 실패와 보상 실패도 별도 상태·Attempt·Audit로 남긴다.

## 필수 Test

- Incident workflow
- recovery command
- audit timeline
- 입력 Validation·권한·Data Scope·Masking Negative Test
- 해당 기능에 적용되는 중복·동시성·Timeout·응답 유실·부분 실패 Test
- 해당 기능의 Failure Injection 후 필요한 Retry·Restart·Reprocess·Reconcile·Rollback Test
- Oracle·PostgreSQL·MariaDB 또는 해당 Infra의 의미 동일성 Test
- Log·Metric·Trace·Audit에 Secret·개인정보 원문이 없는지 검사

## 운영·화면 확인

- Incidents·Recovery Center·Transactions 메뉴 연결 Browser Test
- 화면에는 해당 메뉴에 실제로 존재하는 조회 조건, 기본값, Column, 상세 Field, 상태, Button 활성 조건, 사유, 승인, 예상 Version, 대상별 결과만 Source와 일치하게 표시한다. 조회 전용 메뉴에 Command 항목을 만들지 않는다.
- 운영자가 정상화 완료를 판단할 수 있는 건수·금액·Version·Checksum·최종 상태·마지막 오류를 제공한다.
- 변경·외부연계 기능에서 응답 유실이 가능한 경우에만 요청을 다시 보내기 전에 Operation·거래·상대 상태를 조회하는 절차를 Browser 또는 API Test로 검증한다.

## 고객 매뉴얼·EDU 연결

- `03_ADM개발자매뉴얼.md·04_ADM운영자매뉴얼.md`의 해당 기능 장에 `EDU-ADM-12`를 연결한다.
- 선행 환경, 실행 명령, 입력값, 정상 출력, 오류 재현 명령, 복구 명령, ADM/BZA/Gateway 확인 위치를 그대로 따라 할 수 있게 작성한다.
- 교육 코드에서 고객 업무로 바꿀 Package·Table·API·Topic·파일명과 CPF 관리 영역을 구분한 전환표를 제공한다.

## 완료 조건

- `EDU-ADM-12`가 교육 Catalog·Source·Test·고객 매뉴얼에 동일 ID로 존재한다.
- 정상·Validation·권한·중복·동시성·Timeout·응답 유실·부분 실패·복구 Test가 통과한다.
- 요청·응답·상태·DB·이벤트·파일·운영 화면이 양방향으로 일치한다.
- 운영자가 다른 사람의 설명 없이 장애를 재현하고 정상화 판정을 할 수 있다.
- 실행하지 않은 Test는 성공으로 기록하지 않고, 실패·제약이 남으면 전체 완료로 보고하지 않는다.

## 금지 사항

- 메모리 Map·고정 JSON·정상 Case 한 건만으로 완료 처리
- 교육용 고객 업무를 `cpf-core`, `cpf-starters/common`, Starter, Runtime 제품 내부에 삽입
- Module-local DB Vendor Fallback SQL 또는 Vendor별 의미가 다른 Schema
- Timeout이나 응답 유실 뒤 실제 결과 확인 없이 무조건 재호출
- 비밀번호·Token·개인정보 원문을 Source·Log·Fixture·문서에 기록
- 매뉴얼 설명과 다른 임의 Package·API·상태·메뉴를 새로 생성

---

# 105. EDU-ADM-13 — 감사 증적·다운로드·승인 반출

- 우선순위: **P0**
- 연결 매뉴얼: `03_ADM개발자매뉴얼.md·04_ADM운영자매뉴얼.md`
- Owner: `고객 업무 Owner + cpf-admin`
- 구현 상태 목표: Source·SQL·API·Config·Test·운영 확인·매뉴얼 연결이 모두 완료

## 필요 근거

- 고객 매뉴얼에서 `감사 증적·다운로드·승인 반출`을 설명하려면 정상 호출 예시만이 아니라 상태 변화와 장애 후 정상화까지 재현 가능한 실행 예제가 필요하다.
- 이 요구사항이 없으면 고객은 기능 가능 여부를 알더라도 실제 업무 적용 순서, 실패 판정, 재처리 금지 조건을 확인할 수 없다.
- 내부 구현 구조를 보여 주기 위한 예제가 아니라 고객 업무를 CPF로 만드는 방법과 운영 결과를 증명하는 예제로 구현한다.

## 고객 업무 시나리오

감사자가 기간·사용자·조치로 조회하고 승인 후 Masking된 증적을 반출한다.

## 구현 위치와 책임 경계

- 업무 Source: `com.cpf.education.edu.adm13` 또는 같은 의미의 기존 표준 Slice
- Test: Owner Module의 기존 Test Root 아래 `EDU-ADM-13` 식별 가능한 Class·Fixture
- SQL: 중앙 DB Vendor Pack의 기존 Install·Upgrade·Rollback·Verify·Seed 구조
- Config: 기존 Prefix·Profile·Secret 참조 방식을 유지
- 고객 업무 상태 변경과 원장 데이터는 고객 업무 Owner가 소유한다. ADM·BZA·Gateway·운영 Script가 업무 Table을 직접 수정하지 않는다.
- 공통 기능이 필요하면 기존 Public API·SPI를 우선 사용하고, 진입점이 없을 때만 실제 Consumer를 근거로 Owner Module에 확장한다.

## 입력·계약

- `filters`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `format`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `reason`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `approvalId`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- 해당 기능에 필요한 Operation ID·업무 ID·Correlation ID·요청 Hash·사용자·권한 문맥을 저장·Log·Trace·Audit에서 연결한다.
- 요청·응답·Job Parameter·이벤트·파일 Layout 중 해당되는 계약을 OpenAPI 또는 Machine-readable Schema로 제공한다.

## 상태와 저장 근거

- 필수 상태 흐름: `QUERY_READY → EXPORT_REQUESTED → APPROVED → READY/EXPIRED`
- 상태마다 진입 조건, 허용 조치, 재시도 가능 여부, 최종 여부, 저장 위치와 Version 규칙을 정의한다.
- 결과 미확정·부분 성공이 가능한 경우 대상별 Attempt와 상대 상태 조회 근거를 별도 저장한다.

## 정상 처리 순서

1. 입력·권한·데이터 범위·기준 Version과 선행 상태를 검증한다.
2. 상태 변경·비동기 실행인 경우 Operation과 업무 식별자를 생성하거나 기존 멱등 요청을 조회한다. 조회 전용 기능은 조회 Context와 Cursor만 사용한다.
3. 상태 변경 기능은 고객 업무 Owner의 Application Use Case에서 Transaction 경계와 상태 전이를 수행하고, 조회 전용 기능은 일관된 Snapshot·정렬·범위 조건을 적용한다.
4. 외부 시스템·Kafka·파일·Batch·Gateway가 필요한 경우 Adapter 경계에서 Timeout·Retry·보안 정책을 적용한다.
5. 업무 결과와 Attempt·Audit·Log·Metric·Trace를 같은 식별자로 연결한다.
6. 고객 API 또는 운영 화면에서 최종 상태·건수·금액·Version·오류 0건을 확인한다.

## 예외·장애·경계 사례

- 대량 결과
- 승인 만료
- 개인정보
- 재다운로드
- Hash 불일치
- 상태 변경이 있는 경우 DB Commit 전 실패와 Commit 후 응답 유실을 구분한다.
- 같은 요청의 중복·동시 실행·순서 역전·늦은 응답을 포함한다.
- Retry 가능한 오류와 즉시 중단해야 하는 업무 오류를 구분한다.
- 대량·다중 대상 기능에서 부분 성공이 발생하면 성공 대상을 다시 실행하지 않고 실패 대상만 Reprocess·Reconcile한다.
- 복구·보상이 필요한 기능은 복구 실패와 보상 실패도 별도 상태·Attempt·Audit로 남긴다.

## 필수 Test

- Export async
- manifest hash
- download audit
- permission
- 입력 Validation·권한·Data Scope·Masking Negative Test
- 해당 기능에 적용되는 중복·동시성·Timeout·응답 유실·부분 실패 Test
- 해당 기능의 Failure Injection 후 필요한 Retry·Restart·Reprocess·Reconcile·Rollback Test
- Oracle·PostgreSQL·MariaDB 또는 해당 Infra의 의미 동일성 Test
- Log·Metric·Trace·Audit에 Secret·개인정보 원문이 없는지 검사

## 운영·화면 확인

- Audit Logs·Downloads 메뉴 E2E
- 화면에는 해당 메뉴에 실제로 존재하는 조회 조건, 기본값, Column, 상세 Field, 상태, Button 활성 조건, 사유, 승인, 예상 Version, 대상별 결과만 Source와 일치하게 표시한다. 조회 전용 메뉴에 Command 항목을 만들지 않는다.
- 운영자가 정상화 완료를 판단할 수 있는 건수·금액·Version·Checksum·최종 상태·마지막 오류를 제공한다.
- 변경·외부연계 기능에서 응답 유실이 가능한 경우에만 요청을 다시 보내기 전에 Operation·거래·상대 상태를 조회하는 절차를 Browser 또는 API Test로 검증한다.

## 고객 매뉴얼·EDU 연결

- `03_ADM개발자매뉴얼.md·04_ADM운영자매뉴얼.md`의 해당 기능 장에 `EDU-ADM-13`를 연결한다.
- 선행 환경, 실행 명령, 입력값, 정상 출력, 오류 재현 명령, 복구 명령, ADM/BZA/Gateway 확인 위치를 그대로 따라 할 수 있게 작성한다.
- 교육 코드에서 고객 업무로 바꿀 Package·Table·API·Topic·파일명과 CPF 관리 영역을 구분한 전환표를 제공한다.

## 완료 조건

- `EDU-ADM-13`가 교육 Catalog·Source·Test·고객 매뉴얼에 동일 ID로 존재한다.
- 정상·Validation·권한·중복·동시성·Timeout·응답 유실·부분 실패·복구 Test가 통과한다.
- 요청·응답·상태·DB·이벤트·파일·운영 화면이 양방향으로 일치한다.
- 운영자가 다른 사람의 설명 없이 장애를 재현하고 정상화 판정을 할 수 있다.
- 실행하지 않은 Test는 성공으로 기록하지 않고, 실패·제약이 남으면 전체 완료로 보고하지 않는다.

## 금지 사항

- 메모리 Map·고정 JSON·정상 Case 한 건만으로 완료 처리
- 교육용 고객 업무를 `cpf-core`, `cpf-starters/common`, Starter, Runtime 제품 내부에 삽입
- Module-local DB Vendor Fallback SQL 또는 Vendor별 의미가 다른 Schema
- Timeout이나 응답 유실 뒤 실제 결과 확인 없이 무조건 재호출
- 비밀번호·Token·개인정보 원문을 Source·Log·Fixture·문서에 기록
- 매뉴얼 설명과 다른 임의 Package·API·상태·메뉴를 새로 생성

---

# 106. EDU-ADM-14 — Topology·Health·Capacity Drill-down

- 우선순위: **P0**
- 연결 매뉴얼: `03_ADM개발자매뉴얼.md·04_ADM운영자매뉴얼.md`
- Owner: `고객 업무 Owner + cpf-admin`
- 구현 상태 목표: Source·SQL·API·Config·Test·운영 확인·매뉴얼 연결이 모두 완료

## 필요 근거

- 고객 매뉴얼에서 `Topology·Health·Capacity Drill-down`을 설명하려면 정상 호출 예시만이 아니라 상태 변화와 장애 후 정상화까지 재현 가능한 실행 예제가 필요하다.
- 이 요구사항이 없으면 고객은 기능 가능 여부를 알더라도 실제 업무 적용 순서, 실패 판정, 재처리 금지 조건을 확인할 수 없다.
- 내부 구현 구조를 보여 주기 위한 예제가 아니라 고객 업무를 CPF로 만드는 방법과 운영 결과를 증명하는 예제로 구현한다.

## 고객 업무 시나리오

대시보드 이상 신호에서 서비스→인스턴스→Endpoint→최근 거래로 내려간다.

## 구현 위치와 책임 경계

- 업무 Source: `com.cpf.education.edu.adm14` 또는 같은 의미의 기존 표준 Slice
- Test: Owner Module의 기존 Test Root 아래 `EDU-ADM-14` 식별 가능한 Class·Fixture
- SQL: 중앙 DB Vendor Pack의 기존 Install·Upgrade·Rollback·Verify·Seed 구조
- Config: 기존 Prefix·Profile·Secret 참조 방식을 유지
- 고객 업무 상태 변경과 원장 데이터는 고객 업무 Owner가 소유한다. ADM·BZA·Gateway·운영 Script가 업무 Table을 직접 수정하지 않는다.
- 공통 기능이 필요하면 기존 Public API·SPI를 우선 사용하고, 진입점이 없을 때만 실제 Consumer를 근거로 Owner Module에 확장한다.

## 입력·계약

- `serviceId`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `instanceId`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `timeRange`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- 해당 기능에 필요한 Operation ID·업무 ID·Correlation ID·요청 Hash·사용자·권한 문맥을 저장·Log·Trace·Audit에서 연결한다.
- 요청·응답·Job Parameter·이벤트·파일 Layout 중 해당되는 계약을 OpenAPI 또는 Machine-readable Schema로 제공한다.

## 상태와 저장 근거

- 필수 상태 흐름: `OVERVIEW → DRILLDOWN → CORRELATED`
- 상태마다 진입 조건, 허용 조치, 재시도 가능 여부, 최종 여부, 저장 위치와 Version 규칙을 정의한다.
- 결과 미확정·부분 성공이 가능한 경우 대상별 Attempt와 상대 상태 조회 근거를 별도 저장한다.

## 정상 처리 순서

1. 입력·권한·데이터 범위·기준 Version과 선행 상태를 검증한다.
2. 상태 변경·비동기 실행인 경우 Operation과 업무 식별자를 생성하거나 기존 멱등 요청을 조회한다. 조회 전용 기능은 조회 Context와 Cursor만 사용한다.
3. 상태 변경 기능은 고객 업무 Owner의 Application Use Case에서 Transaction 경계와 상태 전이를 수행하고, 조회 전용 기능은 일관된 Snapshot·정렬·범위 조건을 적용한다.
4. 외부 시스템·Kafka·파일·Batch·Gateway가 필요한 경우 Adapter 경계에서 Timeout·Retry·보안 정책을 적용한다.
5. 업무 결과와 Attempt·Audit·Log·Metric·Trace를 같은 식별자로 연결한다.
6. 고객 API 또는 운영 화면에서 최종 상태·건수·금액·Version·오류 0건을 확인한다.

## 예외·장애·경계 사례

- Stale health
- instance churn
- metric gap
- clock skew
- 상태 변경이 있는 경우 DB Commit 전 실패와 Commit 후 응답 유실을 구분한다.
- 같은 요청의 중복·동시 실행·순서 역전·늦은 응답을 포함한다.
- Retry 가능한 오류와 즉시 중단해야 하는 업무 오류를 구분한다.
- 대량·다중 대상 기능에서 부분 성공이 발생하면 성공 대상을 다시 실행하지 않고 실패 대상만 Reprocess·Reconcile한다.
- 복구·보상이 필요한 기능은 복구 실패와 보상 실패도 별도 상태·Attempt·Audit로 남긴다.

## 필수 Test

- Navigation context
- freshness
- pagination
- trace link
- 입력 Validation·권한·Data Scope·Masking Negative Test
- 해당 기능에 적용되는 중복·동시성·Timeout·응답 유실·부분 실패 Test
- 해당 기능의 Failure Injection 후 필요한 Retry·Restart·Reprocess·Reconcile·Rollback Test
- Oracle·PostgreSQL·MariaDB 또는 해당 Infra의 의미 동일성 Test
- Log·Metric·Trace·Audit에 Secret·개인정보 원문이 없는지 검사

## 운영·화면 확인

- Dashboard·Topology·Capacity·Transactions 연결 Browser Test
- 화면에는 해당 메뉴에 실제로 존재하는 조회 조건, 기본값, Column, 상세 Field, 상태, Button 활성 조건, 사유, 승인, 예상 Version, 대상별 결과만 Source와 일치하게 표시한다. 조회 전용 메뉴에 Command 항목을 만들지 않는다.
- 운영자가 정상화 완료를 판단할 수 있는 건수·금액·Version·Checksum·최종 상태·마지막 오류를 제공한다.
- 변경·외부연계 기능에서 응답 유실이 가능한 경우에만 요청을 다시 보내기 전에 Operation·거래·상대 상태를 조회하는 절차를 Browser 또는 API Test로 검증한다.

## 고객 매뉴얼·EDU 연결

- `03_ADM개발자매뉴얼.md·04_ADM운영자매뉴얼.md`의 해당 기능 장에 `EDU-ADM-14`를 연결한다.
- 선행 환경, 실행 명령, 입력값, 정상 출력, 오류 재현 명령, 복구 명령, ADM/BZA/Gateway 확인 위치를 그대로 따라 할 수 있게 작성한다.
- 교육 코드에서 고객 업무로 바꿀 Package·Table·API·Topic·파일명과 CPF 관리 영역을 구분한 전환표를 제공한다.

## 완료 조건

- `EDU-ADM-14`가 교육 Catalog·Source·Test·고객 매뉴얼에 동일 ID로 존재한다.
- 정상·Validation·권한·중복·동시성·Timeout·응답 유실·부분 실패·복구 Test가 통과한다.
- 요청·응답·상태·DB·이벤트·파일·운영 화면이 양방향으로 일치한다.
- 운영자가 다른 사람의 설명 없이 장애를 재현하고 정상화 판정을 할 수 있다.
- 실행하지 않은 Test는 성공으로 기록하지 않고, 실패·제약이 남으면 전체 완료로 보고하지 않는다.

## 금지 사항

- 메모리 Map·고정 JSON·정상 Case 한 건만으로 완료 처리
- 교육용 고객 업무를 `cpf-core`, `cpf-starters/common`, Starter, Runtime 제품 내부에 삽입
- Module-local DB Vendor Fallback SQL 또는 Vendor별 의미가 다른 Schema
- Timeout이나 응답 유실 뒤 실제 결과 확인 없이 무조건 재호출
- 비밀번호·Token·개인정보 원문을 Source·Log·Fixture·문서에 기록
- 매뉴얼 설명과 다른 임의 Package·API·상태·메뉴를 새로 생성

---

# 107. EDU-ADM-15 — Log·Trace·Transaction 상관 검색

- 우선순위: **P0**
- 연결 매뉴얼: `03_ADM개발자매뉴얼.md·04_ADM운영자매뉴얼.md`
- Owner: `고객 업무 Owner + cpf-admin`
- 구현 상태 목표: Source·SQL·API·Config·Test·운영 확인·매뉴얼 연결이 모두 완료

## 필요 근거

- 고객 매뉴얼에서 `Log·Trace·Transaction 상관 검색`을 설명하려면 정상 호출 예시만이 아니라 상태 변화와 장애 후 정상화까지 재현 가능한 실행 예제가 필요하다.
- 이 요구사항이 없으면 고객은 기능 가능 여부를 알더라도 실제 업무 적용 순서, 실패 판정, 재처리 금지 조건을 확인할 수 없다.
- 내부 구현 구조를 보여 주기 위한 예제가 아니라 고객 업무를 CPF로 만드는 방법과 운영 결과를 증명하는 예제로 구현한다.

## 고객 업무 시나리오

Transaction ID 하나로 Local·Remote 로그와 Trace Segment를 연결한다.

## 구현 위치와 책임 경계

- 업무 Source: `com.cpf.education.edu.adm15` 또는 같은 의미의 기존 표준 Slice
- Test: Owner Module의 기존 Test Root 아래 `EDU-ADM-15` 식별 가능한 Class·Fixture
- SQL: 중앙 DB Vendor Pack의 기존 Install·Upgrade·Rollback·Verify·Seed 구조
- Config: 기존 Prefix·Profile·Secret 참조 방식을 유지
- 고객 업무 상태 변경과 원장 데이터는 고객 업무 Owner가 소유한다. ADM·BZA·Gateway·운영 Script가 업무 Table을 직접 수정하지 않는다.
- 공통 기능이 필요하면 기존 Public API·SPI를 우선 사용하고, 진입점이 없을 때만 실제 Consumer를 근거로 Owner Module에 확장한다.

## 입력·계약

- `transactionId`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `traceId`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `segmentId`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `timeRange`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- 해당 기능에 필요한 Operation ID·업무 ID·Correlation ID·요청 Hash·사용자·권한 문맥을 저장·Log·Trace·Audit에서 연결한다.
- 요청·응답·Job Parameter·이벤트·파일 Layout 중 해당되는 계약을 OpenAPI 또는 Machine-readable Schema로 제공한다.

## 상태와 저장 근거

- 필수 상태 흐름: `SEARCHING → CORRELATED/PARTIAL`
- 상태마다 진입 조건, 허용 조치, 재시도 가능 여부, 최종 여부, 저장 위치와 Version 규칙을 정의한다.
- 결과 미확정·부분 성공이 가능한 경우 대상별 Attempt와 상대 상태 조회 근거를 별도 저장한다.

## 정상 처리 순서

1. 입력·권한·데이터 범위·기준 Version과 선행 상태를 검증한다.
2. 상태 변경·비동기 실행인 경우 Operation과 업무 식별자를 생성하거나 기존 멱등 요청을 조회한다. 조회 전용 기능은 조회 Context와 Cursor만 사용한다.
3. 상태 변경 기능은 고객 업무 Owner의 Application Use Case에서 Transaction 경계와 상태 전이를 수행하고, 조회 전용 기능은 일관된 Snapshot·정렬·범위 조건을 적용한다.
4. 외부 시스템·Kafka·파일·Batch·Gateway가 필요한 경우 Adapter 경계에서 Timeout·Retry·보안 정책을 적용한다.
5. 업무 결과와 Attempt·Audit·Log·Metric·Trace를 같은 식별자로 연결한다.
6. 고객 API 또는 운영 화면에서 최종 상태·건수·금액·Version·오류 0건을 확인한다.

## 예외·장애·경계 사례

- 로그 유실
- Trace sampling
- 원격 로그 timeout
- Masking
- 상태 변경이 있는 경우 DB Commit 전 실패와 Commit 후 응답 유실을 구분한다.
- 같은 요청의 중복·동시 실행·순서 역전·늦은 응답을 포함한다.
- Retry 가능한 오류와 즉시 중단해야 하는 업무 오류를 구분한다.
- 대량·다중 대상 기능에서 부분 성공이 발생하면 성공 대상을 다시 실행하지 않고 실패 대상만 Reprocess·Reconcile한다.
- 복구·보상이 필요한 기능은 복구 실패와 보상 실패도 별도 상태·Attempt·Audit로 남긴다.

## 필수 Test

- Cross-menu deep link
- partial warning
- download limits
- 입력 Validation·권한·Data Scope·Masking Negative Test
- 해당 기능에 적용되는 중복·동시성·Timeout·응답 유실·부분 실패 Test
- 해당 기능의 Failure Injection 후 필요한 Retry·Restart·Reprocess·Reconcile·Rollback Test
- Oracle·PostgreSQL·MariaDB 또는 해당 Infra의 의미 동일성 Test
- Log·Metric·Trace·Audit에 Secret·개인정보 원문이 없는지 검사

## 운영·화면 확인

- Logs·Remote Logs·Transaction Groups 메뉴 E2E
- 화면에는 해당 메뉴에 실제로 존재하는 조회 조건, 기본값, Column, 상세 Field, 상태, Button 활성 조건, 사유, 승인, 예상 Version, 대상별 결과만 Source와 일치하게 표시한다. 조회 전용 메뉴에 Command 항목을 만들지 않는다.
- 운영자가 정상화 완료를 판단할 수 있는 건수·금액·Version·Checksum·최종 상태·마지막 오류를 제공한다.
- 변경·외부연계 기능에서 응답 유실이 가능한 경우에만 요청을 다시 보내기 전에 Operation·거래·상대 상태를 조회하는 절차를 Browser 또는 API Test로 검증한다.

## 고객 매뉴얼·EDU 연결

- `03_ADM개발자매뉴얼.md·04_ADM운영자매뉴얼.md`의 해당 기능 장에 `EDU-ADM-15`를 연결한다.
- 선행 환경, 실행 명령, 입력값, 정상 출력, 오류 재현 명령, 복구 명령, ADM/BZA/Gateway 확인 위치를 그대로 따라 할 수 있게 작성한다.
- 교육 코드에서 고객 업무로 바꿀 Package·Table·API·Topic·파일명과 CPF 관리 영역을 구분한 전환표를 제공한다.

## 완료 조건

- `EDU-ADM-15`가 교육 Catalog·Source·Test·고객 매뉴얼에 동일 ID로 존재한다.
- 정상·Validation·권한·중복·동시성·Timeout·응답 유실·부분 실패·복구 Test가 통과한다.
- 요청·응답·상태·DB·이벤트·파일·운영 화면이 양방향으로 일치한다.
- 운영자가 다른 사람의 설명 없이 장애를 재현하고 정상화 판정을 할 수 있다.
- 실행하지 않은 Test는 성공으로 기록하지 않고, 실패·제약이 남으면 전체 완료로 보고하지 않는다.

## 금지 사항

- 메모리 Map·고정 JSON·정상 Case 한 건만으로 완료 처리
- 교육용 고객 업무를 `cpf-core`, `cpf-starters/common`, Starter, Runtime 제품 내부에 삽입
- Module-local DB Vendor Fallback SQL 또는 Vendor별 의미가 다른 Schema
- Timeout이나 응답 유실 뒤 실제 결과 확인 없이 무조건 재호출
- 비밀번호·Token·개인정보 원문을 Source·Log·Fixture·문서에 기록
- 매뉴얼 설명과 다른 임의 Package·API·상태·메뉴를 새로 생성

---

# 108. EDU-ADM-16 — 알림 Acknowledge·Escalation·교대 인계

- 우선순위: **P0**
- 연결 매뉴얼: `03_ADM개발자매뉴얼.md·04_ADM운영자매뉴얼.md`
- Owner: `고객 업무 Owner + cpf-admin`
- 구현 상태 목표: Source·SQL·API·Config·Test·운영 확인·매뉴얼 연결이 모두 완료

## 필요 근거

- 고객 매뉴얼에서 `알림 Acknowledge·Escalation·교대 인계`을 설명하려면 정상 호출 예시만이 아니라 상태 변화와 장애 후 정상화까지 재현 가능한 실행 예제가 필요하다.
- 이 요구사항이 없으면 고객은 기능 가능 여부를 알더라도 실제 업무 적용 순서, 실패 판정, 재처리 금지 조건을 확인할 수 없다.
- 내부 구현 구조를 보여 주기 위한 예제가 아니라 고객 업무를 CPF로 만드는 방법과 운영 결과를 증명하는 예제로 구현한다.

## 고객 업무 시나리오

운영자가 경보를 확인 처리하고 미조치 시 상위 담당자에게 Escalation한다.

## 구현 위치와 책임 경계

- 업무 Source: `com.cpf.education.edu.adm16` 또는 같은 의미의 기존 표준 Slice
- Test: Owner Module의 기존 Test Root 아래 `EDU-ADM-16` 식별 가능한 Class·Fixture
- SQL: 중앙 DB Vendor Pack의 기존 Install·Upgrade·Rollback·Verify·Seed 구조
- Config: 기존 Prefix·Profile·Secret 참조 방식을 유지
- 고객 업무 상태 변경과 원장 데이터는 고객 업무 Owner가 소유한다. ADM·BZA·Gateway·운영 Script가 업무 Table을 직접 수정하지 않는다.
- 공통 기능이 필요하면 기존 Public API·SPI를 우선 사용하고, 진입점이 없을 때만 실제 Consumer를 근거로 Owner Module에 확장한다.

## 입력·계약

- `alertId`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `ackReason`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `owner`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `snoozeUntil`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- 해당 기능에 필요한 Operation ID·업무 ID·Correlation ID·요청 Hash·사용자·권한 문맥을 저장·Log·Trace·Audit에서 연결한다.
- 요청·응답·Job Parameter·이벤트·파일 Layout 중 해당되는 계약을 OpenAPI 또는 Machine-readable Schema로 제공한다.

## 상태와 저장 근거

- 필수 상태 흐름: `OPEN → ACKNOWLEDGED → RESOLVED/ESCALATED`
- 상태마다 진입 조건, 허용 조치, 재시도 가능 여부, 최종 여부, 저장 위치와 Version 규칙을 정의한다.
- 결과 미확정·부분 성공이 가능한 경우 대상별 Attempt와 상대 상태 조회 근거를 별도 저장한다.

## 정상 처리 순서

1. 입력·권한·데이터 범위·기준 Version과 선행 상태를 검증한다.
2. 상태 변경·비동기 실행인 경우 Operation과 업무 식별자를 생성하거나 기존 멱등 요청을 조회한다. 조회 전용 기능은 조회 Context와 Cursor만 사용한다.
3. 상태 변경 기능은 고객 업무 Owner의 Application Use Case에서 Transaction 경계와 상태 전이를 수행하고, 조회 전용 기능은 일관된 Snapshot·정렬·범위 조건을 적용한다.
4. 외부 시스템·Kafka·파일·Batch·Gateway가 필요한 경우 Adapter 경계에서 Timeout·Retry·보안 정책을 적용한다.
5. 업무 결과와 Attempt·Audit·Log·Metric·Trace를 같은 식별자로 연결한다.
6. 고객 API 또는 운영 화면에서 최종 상태·건수·금액·Version·오류 0건을 확인한다.

## 예외·장애·경계 사례

- 중복 ACK
- 담당자 퇴근
- snooze 만료
- 동일 원인 경보 묶음
- 상태 변경이 있는 경우 DB Commit 전 실패와 Commit 후 응답 유실을 구분한다.
- 같은 요청의 중복·동시 실행·순서 역전·늦은 응답을 포함한다.
- Retry 가능한 오류와 즉시 중단해야 하는 업무 오류를 구분한다.
- 대량·다중 대상 기능에서 부분 성공이 발생하면 성공 대상을 다시 실행하지 않고 실패 대상만 Reprocess·Reconcile한다.
- 복구·보상이 필요한 기능은 복구 실패와 보상 실패도 별도 상태·Attempt·Audit로 남긴다.

## 필수 Test

- Alert grouping
- escalation timer
- ownership audit
- 입력 Validation·권한·Data Scope·Masking Negative Test
- 해당 기능에 적용되는 중복·동시성·Timeout·응답 유실·부분 실패 Test
- 해당 기능의 Failure Injection 후 필요한 Retry·Restart·Reprocess·Reconcile·Rollback Test
- Oracle·PostgreSQL·MariaDB 또는 해당 Infra의 의미 동일성 Test
- Log·Metric·Trace·Audit에 Secret·개인정보 원문이 없는지 검사

## 운영·화면 확인

- Alerts·Notifications·Operators 메뉴 E2E
- 화면에는 해당 메뉴에 실제로 존재하는 조회 조건, 기본값, Column, 상세 Field, 상태, Button 활성 조건, 사유, 승인, 예상 Version, 대상별 결과만 Source와 일치하게 표시한다. 조회 전용 메뉴에 Command 항목을 만들지 않는다.
- 운영자가 정상화 완료를 판단할 수 있는 건수·금액·Version·Checksum·최종 상태·마지막 오류를 제공한다.
- 변경·외부연계 기능에서 응답 유실이 가능한 경우에만 요청을 다시 보내기 전에 Operation·거래·상대 상태를 조회하는 절차를 Browser 또는 API Test로 검증한다.

## 고객 매뉴얼·EDU 연결

- `03_ADM개발자매뉴얼.md·04_ADM운영자매뉴얼.md`의 해당 기능 장에 `EDU-ADM-16`를 연결한다.
- 선행 환경, 실행 명령, 입력값, 정상 출력, 오류 재현 명령, 복구 명령, ADM/BZA/Gateway 확인 위치를 그대로 따라 할 수 있게 작성한다.
- 교육 코드에서 고객 업무로 바꿀 Package·Table·API·Topic·파일명과 CPF 관리 영역을 구분한 전환표를 제공한다.

## 완료 조건

- `EDU-ADM-16`가 교육 Catalog·Source·Test·고객 매뉴얼에 동일 ID로 존재한다.
- 정상·Validation·권한·중복·동시성·Timeout·응답 유실·부분 실패·복구 Test가 통과한다.
- 요청·응답·상태·DB·이벤트·파일·운영 화면이 양방향으로 일치한다.
- 운영자가 다른 사람의 설명 없이 장애를 재현하고 정상화 판정을 할 수 있다.
- 실행하지 않은 Test는 성공으로 기록하지 않고, 실패·제약이 남으면 전체 완료로 보고하지 않는다.

## 금지 사항

- 메모리 Map·고정 JSON·정상 Case 한 건만으로 완료 처리
- 교육용 고객 업무를 `cpf-core`, `cpf-starters/common`, Starter, Runtime 제품 내부에 삽입
- Module-local DB Vendor Fallback SQL 또는 Vendor별 의미가 다른 Schema
- Timeout이나 응답 유실 뒤 실제 결과 확인 없이 무조건 재호출
- 비밀번호·Token·개인정보 원문을 Source·Log·Fixture·문서에 기록
- 매뉴얼 설명과 다른 임의 Package·API·상태·메뉴를 새로 생성

---

# 109. EDU-ADM-17 — Browser 세션 만료·재로그인·위험 조치 안전성

- 우선순위: **P0**
- 연결 매뉴얼: `03_ADM개발자매뉴얼.md·04_ADM운영자매뉴얼.md`
- Owner: `고객 업무 Owner + cpf-admin`
- 구현 상태 목표: Source·SQL·API·Config·Test·운영 확인·매뉴얼 연결이 모두 완료

## 필요 근거

- 고객 매뉴얼에서 `Browser 세션 만료·재로그인·위험 조치 안전성`을 설명하려면 정상 호출 예시만이 아니라 상태 변화와 장애 후 정상화까지 재현 가능한 실행 예제가 필요하다.
- 이 요구사항이 없으면 고객은 기능 가능 여부를 알더라도 실제 업무 적용 순서, 실패 판정, 재처리 금지 조건을 확인할 수 없다.
- 내부 구현 구조를 보여 주기 위한 예제가 아니라 고객 업무를 CPF로 만드는 방법과 운영 결과를 증명하는 예제로 구현한다.

## 고객 업무 시나리오

위험 조치 확인 중 세션이 만료돼도 재로그인 후 실제 적용 여부를 조회하고 중복 실행하지 않는다.

## 구현 위치와 책임 경계

- 업무 Source: `com.cpf.education.edu.adm17` 또는 같은 의미의 기존 표준 Slice
- Test: Owner Module의 기존 Test Root 아래 `EDU-ADM-17` 식별 가능한 Class·Fixture
- SQL: 중앙 DB Vendor Pack의 기존 Install·Upgrade·Rollback·Verify·Seed 구조
- Config: 기존 Prefix·Profile·Secret 참조 방식을 유지
- 고객 업무 상태 변경과 원장 데이터는 고객 업무 Owner가 소유한다. ADM·BZA·Gateway·운영 Script가 업무 Table을 직접 수정하지 않는다.
- 공통 기능이 필요하면 기존 Public API·SPI를 우선 사용하고, 진입점이 없을 때만 실제 Consumer를 근거로 Owner Module에 확장한다.

## 입력·계약

- `sessionId`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `commandId`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `operationId`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- 해당 기능에 필요한 Operation ID·업무 ID·Correlation ID·요청 Hash·사용자·권한 문맥을 저장·Log·Trace·Audit에서 연결한다.
- 요청·응답·Job Parameter·이벤트·파일 Layout 중 해당되는 계약을 OpenAPI 또는 Machine-readable Schema로 제공한다.

## 상태와 저장 근거

- 필수 상태 흐름: `CONFIRMING → SESSION_EXPIRED → REAUTHENTICATED → RECONCILED`
- 상태마다 진입 조건, 허용 조치, 재시도 가능 여부, 최종 여부, 저장 위치와 Version 규칙을 정의한다.
- 결과 미확정·부분 성공이 가능한 경우 대상별 Attempt와 상대 상태 조회 근거를 별도 저장한다.

## 정상 처리 순서

1. 입력·권한·데이터 범위·기준 Version과 선행 상태를 검증한다.
2. 상태 변경·비동기 실행인 경우 Operation과 업무 식별자를 생성하거나 기존 멱등 요청을 조회한다. 조회 전용 기능은 조회 Context와 Cursor만 사용한다.
3. 상태 변경 기능은 고객 업무 Owner의 Application Use Case에서 Transaction 경계와 상태 전이를 수행하고, 조회 전용 기능은 일관된 Snapshot·정렬·범위 조건을 적용한다.
4. 외부 시스템·Kafka·파일·Batch·Gateway가 필요한 경우 Adapter 경계에서 Timeout·Retry·보안 정책을 적용한다.
5. 업무 결과와 Attempt·Audit·Log·Metric·Trace를 같은 식별자로 연결한다.
6. 고객 API 또는 운영 화면에서 최종 상태·건수·금액·Version·오류 0건을 확인한다.

## 예외·장애·경계 사례

- POST 전 만료
- POST 후 응답 전 만료
- 다중 Tab
- CSRF
- 상태 변경이 있는 경우 DB Commit 전 실패와 Commit 후 응답 유실을 구분한다.
- 같은 요청의 중복·동시 실행·순서 역전·늦은 응답을 포함한다.
- Retry 가능한 오류와 즉시 중단해야 하는 업무 오류를 구분한다.
- 대량·다중 대상 기능에서 부분 성공이 발생하면 성공 대상을 다시 실행하지 않고 실패 대상만 Reprocess·Reconcile한다.
- 복구·보상이 필요한 기능은 복구 실패와 보상 실패도 별도 상태·Attempt·Audit로 남긴다.

## 필수 Test

- Session expiry injection
- attempt ledger
- no auto replay
- 입력 Validation·권한·Data Scope·Masking Negative Test
- 해당 기능에 적용되는 중복·동시성·Timeout·응답 유실·부분 실패 Test
- 해당 기능의 Failure Injection 후 필요한 Retry·Restart·Reprocess·Reconcile·Rollback Test
- Oracle·PostgreSQL·MariaDB 또는 해당 Infra의 의미 동일성 Test
- Log·Metric·Trace·Audit에 Secret·개인정보 원문이 없는지 검사

## 운영·화면 확인

- Password·Sessions·Audit 관련 화면 Browser Test
- 화면에는 해당 메뉴에 실제로 존재하는 조회 조건, 기본값, Column, 상세 Field, 상태, Button 활성 조건, 사유, 승인, 예상 Version, 대상별 결과만 Source와 일치하게 표시한다. 조회 전용 메뉴에 Command 항목을 만들지 않는다.
- 운영자가 정상화 완료를 판단할 수 있는 건수·금액·Version·Checksum·최종 상태·마지막 오류를 제공한다.
- 변경·외부연계 기능에서 응답 유실이 가능한 경우에만 요청을 다시 보내기 전에 Operation·거래·상대 상태를 조회하는 절차를 Browser 또는 API Test로 검증한다.

## 고객 매뉴얼·EDU 연결

- `03_ADM개발자매뉴얼.md·04_ADM운영자매뉴얼.md`의 해당 기능 장에 `EDU-ADM-17`를 연결한다.
- 선행 환경, 실행 명령, 입력값, 정상 출력, 오류 재현 명령, 복구 명령, ADM/BZA/Gateway 확인 위치를 그대로 따라 할 수 있게 작성한다.
- 교육 코드에서 고객 업무로 바꿀 Package·Table·API·Topic·파일명과 CPF 관리 영역을 구분한 전환표를 제공한다.

## 완료 조건

- `EDU-ADM-17`가 교육 Catalog·Source·Test·고객 매뉴얼에 동일 ID로 존재한다.
- 정상·Validation·권한·중복·동시성·Timeout·응답 유실·부분 실패·복구 Test가 통과한다.
- 요청·응답·상태·DB·이벤트·파일·운영 화면이 양방향으로 일치한다.
- 운영자가 다른 사람의 설명 없이 장애를 재현하고 정상화 판정을 할 수 있다.
- 실행하지 않은 Test는 성공으로 기록하지 않고, 실패·제약이 남으면 전체 완료로 보고하지 않는다.

## 금지 사항

- 메모리 Map·고정 JSON·정상 Case 한 건만으로 완료 처리
- 교육용 고객 업무를 `cpf-core`, `cpf-starters/common`, Starter, Runtime 제품 내부에 삽입
- Module-local DB Vendor Fallback SQL 또는 Vendor별 의미가 다른 Schema
- Timeout이나 응답 유실 뒤 실제 결과 확인 없이 무조건 재호출
- 비밀번호·Token·개인정보 원문을 Source·Log·Fixture·문서에 기록
- 매뉴얼 설명과 다른 임의 Package·API·상태·메뉴를 새로 생성

---

# 110. EDU-BZA-07 — 초기 관리자 Bootstrap·첫 로그인·권한 인계

- 우선순위: **P1**
- 연결 매뉴얼: `90_BZA매뉴얼.md`
- Owner: `cpf-biz-admin + 고객 업무 Owner`
- 구현 상태 목표: Source·SQL·API·Config·Test·운영 확인·매뉴얼 연결이 모두 완료

## 필요 근거

- 고객 매뉴얼에서 `초기 관리자 Bootstrap·첫 로그인·권한 인계`을 설명하려면 정상 호출 예시만이 아니라 상태 변화와 장애 후 정상화까지 재현 가능한 실행 예제가 필요하다.
- 이 요구사항이 없으면 고객은 기능 가능 여부를 알더라도 실제 업무 적용 순서, 실패 판정, 재처리 금지 조건을 확인할 수 없다.
- 내부 구현 구조를 보여 주기 위한 예제가 아니라 고객 업무를 CPF로 만드는 방법과 운영 결과를 증명하는 예제로 구현한다.

## 고객 업무 시나리오

신규 설치 후 일회성 초기 관리자 생성, 비밀번호 변경, 정식 운영관리자 역할 인계를 수행한다.

## 구현 위치와 책임 경계

- 업무 Source: `com.cpf.education.edu.bza07` 또는 같은 의미의 기존 표준 Slice
- Test: Owner Module의 기존 Test Root 아래 `EDU-BZA-07` 식별 가능한 Class·Fixture
- SQL: 중앙 DB Vendor Pack의 기존 Install·Upgrade·Rollback·Verify·Seed 구조
- Config: 기존 Prefix·Profile·Secret 참조 방식을 유지
- 고객 업무 상태 변경과 원장 데이터는 고객 업무 Owner가 소유한다. ADM·BZA·Gateway·운영 Script가 업무 Table을 직접 수정하지 않는다.
- 공통 기능이 필요하면 기존 Public API·SPI를 우선 사용하고, 진입점이 없을 때만 실제 Consumer를 근거로 Owner Module에 확장한다.

## 입력·계약

- `bootstrapToken`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `loginId`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `newPassword`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `reason`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- 해당 기능에 필요한 Operation ID·업무 ID·Correlation ID·요청 Hash·사용자·권한 문맥을 저장·Log·Trace·Audit에서 연결한다.
- 요청·응답·Job Parameter·이벤트·파일 Layout 중 해당되는 계약을 OpenAPI 또는 Machine-readable Schema로 제공한다.

## 상태와 저장 근거

- 필수 상태 흐름: `BOOTSTRAP_AVAILABLE → ADMIN_CREATED → PASSWORD_CHANGED → BOOTSTRAP_DISABLED`
- 상태마다 진입 조건, 허용 조치, 재시도 가능 여부, 최종 여부, 저장 위치와 Version 규칙을 정의한다.
- 결과 미확정·부분 성공이 가능한 경우 대상별 Attempt와 상대 상태 조회 근거를 별도 저장한다.

## 정상 처리 순서

1. 입력·권한·데이터 범위·기준 Version과 선행 상태를 검증한다.
2. 상태 변경·비동기 실행인 경우 Operation과 업무 식별자를 생성하거나 기존 멱등 요청을 조회한다. 조회 전용 기능은 조회 Context와 Cursor만 사용한다.
3. 상태 변경 기능은 고객 업무 Owner의 Application Use Case에서 Transaction 경계와 상태 전이를 수행하고, 조회 전용 기능은 일관된 Snapshot·정렬·범위 조건을 적용한다.
4. 외부 시스템·Kafka·파일·Batch·Gateway가 필요한 경우 Adapter 경계에서 Timeout·Retry·보안 정책을 적용한다.
5. 업무 결과와 Attempt·Audit·Log·Metric·Trace를 같은 식별자로 연결한다.
6. 고객 API 또는 운영 화면에서 최종 상태·건수·금액·Version·오류 0건을 확인한다.

## 예외·장애·경계 사례

- Token 재사용
- 만료
- 두 관리자 동시 생성
- 초기 계정 삭제
- 상태 변경이 있는 경우 DB Commit 전 실패와 Commit 후 응답 유실을 구분한다.
- 같은 요청의 중복·동시 실행·순서 역전·늦은 응답을 포함한다.
- Retry 가능한 오류와 즉시 중단해야 하는 업무 오류를 구분한다.
- 대량·다중 대상 기능에서 부분 성공이 발생하면 성공 대상을 다시 실행하지 않고 실패 대상만 Reprocess·Reconcile한다.
- 복구·보상이 필요한 기능은 복구 실패와 보상 실패도 별도 상태·Attempt·Audit로 남긴다.

## 필수 Test

- One-time token
- password policy
- audit
- browser
- 입력 Validation·권한·Data Scope·Masking Negative Test
- 해당 기능에 적용되는 중복·동시성·Timeout·응답 유실·부분 실패 Test
- 해당 기능의 Failure Injection 후 필요한 Retry·Restart·Reprocess·Reconcile·Rollback Test
- Oracle·PostgreSQL·MariaDB 또는 해당 Infra의 의미 동일성 Test
- Log·Metric·Trace·Audit에 Secret·개인정보 원문이 없는지 검사

## 운영·화면 확인

- BZA Users·Roles·Sessions에서 초기화 흔적과 비활성화 확인
- 화면에는 해당 메뉴에 실제로 존재하는 조회 조건, 기본값, Column, 상세 Field, 상태, Button 활성 조건, 사유, 승인, 예상 Version, 대상별 결과만 Source와 일치하게 표시한다. 조회 전용 메뉴에 Command 항목을 만들지 않는다.
- 운영자가 정상화 완료를 판단할 수 있는 건수·금액·Version·Checksum·최종 상태·마지막 오류를 제공한다.
- 변경·외부연계 기능에서 응답 유실이 가능한 경우에만 요청을 다시 보내기 전에 Operation·거래·상대 상태를 조회하는 절차를 Browser 또는 API Test로 검증한다.

## 고객 매뉴얼·EDU 연결

- `90_BZA매뉴얼.md`의 해당 기능 장에 `EDU-BZA-07`를 연결한다.
- 선행 환경, 실행 명령, 입력값, 정상 출력, 오류 재현 명령, 복구 명령, ADM/BZA/Gateway 확인 위치를 그대로 따라 할 수 있게 작성한다.
- 교육 코드에서 고객 업무로 바꿀 Package·Table·API·Topic·파일명과 CPF 관리 영역을 구분한 전환표를 제공한다.

## 완료 조건

- `EDU-BZA-07`가 교육 Catalog·Source·Test·고객 매뉴얼에 동일 ID로 존재한다.
- 정상·Validation·권한·중복·동시성·Timeout·응답 유실·부분 실패·복구 Test가 통과한다.
- 요청·응답·상태·DB·이벤트·파일·운영 화면이 양방향으로 일치한다.
- 운영자가 다른 사람의 설명 없이 장애를 재현하고 정상화 판정을 할 수 있다.
- 실행하지 않은 Test는 성공으로 기록하지 않고, 실패·제약이 남으면 전체 완료로 보고하지 않는다.

## 금지 사항

- 메모리 Map·고정 JSON·정상 Case 한 건만으로 완료 처리
- 교육용 고객 업무를 `cpf-core`, `cpf-starters/common`, Starter, Runtime 제품 내부에 삽입
- Module-local DB Vendor Fallback SQL 또는 Vendor별 의미가 다른 Schema
- Timeout이나 응답 유실 뒤 실제 결과 확인 없이 무조건 재호출
- 비밀번호·Token·개인정보 원문을 Source·Log·Fixture·문서에 기록
- 매뉴얼 설명과 다른 임의 Package·API·상태·메뉴를 새로 생성

---

# 111. EDU-BZA-08 — 조직 개편·기준일·과거 이력 유지

- 우선순위: **P1**
- 연결 매뉴얼: `90_BZA매뉴얼.md`
- Owner: `cpf-biz-admin + 고객 업무 Owner`
- 구현 상태 목표: Source·SQL·API·Config·Test·운영 확인·매뉴얼 연결이 모두 완료

## 필요 근거

- 고객 매뉴얼에서 `조직 개편·기준일·과거 이력 유지`을 설명하려면 정상 호출 예시만이 아니라 상태 변화와 장애 후 정상화까지 재현 가능한 실행 예제가 필요하다.
- 이 요구사항이 없으면 고객은 기능 가능 여부를 알더라도 실제 업무 적용 순서, 실패 판정, 재처리 금지 조건을 확인할 수 없다.
- 내부 구현 구조를 보여 주기 위한 예제가 아니라 고객 업무를 CPF로 만드는 방법과 운영 결과를 증명하는 예제로 구현한다.

## 고객 업무 시나리오

부서 분할·통합을 미래 기준일로 예약하고 과거 거래는 당시 조직을 유지한다.

## 구현 위치와 책임 경계

- 업무 Source: `com.cpf.education.edu.bza08` 또는 같은 의미의 기존 표준 Slice
- Test: Owner Module의 기존 Test Root 아래 `EDU-BZA-08` 식별 가능한 Class·Fixture
- SQL: 중앙 DB Vendor Pack의 기존 Install·Upgrade·Rollback·Verify·Seed 구조
- Config: 기존 Prefix·Profile·Secret 참조 방식을 유지
- 고객 업무 상태 변경과 원장 데이터는 고객 업무 Owner가 소유한다. ADM·BZA·Gateway·운영 Script가 업무 Table을 직접 수정하지 않는다.
- 공통 기능이 필요하면 기존 Public API·SPI를 우선 사용하고, 진입점이 없을 때만 실제 Consumer를 근거로 Owner Module에 확장한다.

## 입력·계약

- `orgChanges`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `effectiveDate`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `reason`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- 해당 기능에 필요한 Operation ID·업무 ID·Correlation ID·요청 Hash·사용자·권한 문맥을 저장·Log·Trace·Audit에서 연결한다.
- 요청·응답·Job Parameter·이벤트·파일 Layout 중 해당되는 계약을 OpenAPI 또는 Machine-readable Schema로 제공한다.

## 상태와 저장 근거

- 필수 상태 흐름: `DRAFT → VALIDATED → SCHEDULED → EFFECTIVE`
- 상태마다 진입 조건, 허용 조치, 재시도 가능 여부, 최종 여부, 저장 위치와 Version 규칙을 정의한다.
- 결과 미확정·부분 성공이 가능한 경우 대상별 Attempt와 상대 상태 조회 근거를 별도 저장한다.

## 정상 처리 순서

1. 입력·권한·데이터 범위·기준 Version과 선행 상태를 검증한다.
2. 상태 변경·비동기 실행인 경우 Operation과 업무 식별자를 생성하거나 기존 멱등 요청을 조회한다. 조회 전용 기능은 조회 Context와 Cursor만 사용한다.
3. 상태 변경 기능은 고객 업무 Owner의 Application Use Case에서 Transaction 경계와 상태 전이를 수행하고, 조회 전용 기능은 일관된 Snapshot·정렬·범위 조건을 적용한다.
4. 외부 시스템·Kafka·파일·Batch·Gateway가 필요한 경우 Adapter 경계에서 Timeout·Retry·보안 정책을 적용한다.
5. 업무 결과와 Attempt·Audit·Log·Metric·Trace를 같은 식별자로 연결한다.
6. 고객 API 또는 운영 화면에서 최종 상태·건수·금액·Version·오류 0건을 확인한다.

## 예외·장애·경계 사례

- 순환 조직
- 책임자 미지정
- 기간 겹침
- 소급 변경
- 상태 변경이 있는 경우 DB Commit 전 실패와 Commit 후 응답 유실을 구분한다.
- 같은 요청의 중복·동시 실행·순서 역전·늦은 응답을 포함한다.
- Retry 가능한 오류와 즉시 중단해야 하는 업무 오류를 구분한다.
- 대량·다중 대상 기능에서 부분 성공이 발생하면 성공 대상을 다시 실행하지 않고 실패 대상만 Reprocess·Reconcile한다.
- 복구·보상이 필요한 기능은 복구 실패와 보상 실패도 별도 상태·Attempt·Audit로 남긴다.

## 필수 Test

- Hierarchy validation
- effective-date query
- rollback plan
- 입력 Validation·권한·Data Scope·Masking Negative Test
- 해당 기능에 적용되는 중복·동시성·Timeout·응답 유실·부분 실패 Test
- 해당 기능의 Failure Injection 후 필요한 Retry·Restart·Reprocess·Reconcile·Rollback Test
- Oracle·PostgreSQL·MariaDB 또는 해당 Infra의 의미 동일성 Test
- Log·Metric·Trace·Audit에 Secret·개인정보 원문이 없는지 검사

## 운영·화면 확인

- Organizations·Responsibilities 메뉴 E2E
- 화면에는 해당 메뉴에 실제로 존재하는 조회 조건, 기본값, Column, 상세 Field, 상태, Button 활성 조건, 사유, 승인, 예상 Version, 대상별 결과만 Source와 일치하게 표시한다. 조회 전용 메뉴에 Command 항목을 만들지 않는다.
- 운영자가 정상화 완료를 판단할 수 있는 건수·금액·Version·Checksum·최종 상태·마지막 오류를 제공한다.
- 변경·외부연계 기능에서 응답 유실이 가능한 경우에만 요청을 다시 보내기 전에 Operation·거래·상대 상태를 조회하는 절차를 Browser 또는 API Test로 검증한다.

## 고객 매뉴얼·EDU 연결

- `90_BZA매뉴얼.md`의 해당 기능 장에 `EDU-BZA-08`를 연결한다.
- 선행 환경, 실행 명령, 입력값, 정상 출력, 오류 재현 명령, 복구 명령, ADM/BZA/Gateway 확인 위치를 그대로 따라 할 수 있게 작성한다.
- 교육 코드에서 고객 업무로 바꿀 Package·Table·API·Topic·파일명과 CPF 관리 영역을 구분한 전환표를 제공한다.

## 완료 조건

- `EDU-BZA-08`가 교육 Catalog·Source·Test·고객 매뉴얼에 동일 ID로 존재한다.
- 정상·Validation·권한·중복·동시성·Timeout·응답 유실·부분 실패·복구 Test가 통과한다.
- 요청·응답·상태·DB·이벤트·파일·운영 화면이 양방향으로 일치한다.
- 운영자가 다른 사람의 설명 없이 장애를 재현하고 정상화 판정을 할 수 있다.
- 실행하지 않은 Test는 성공으로 기록하지 않고, 실패·제약이 남으면 전체 완료로 보고하지 않는다.

## 금지 사항

- 메모리 Map·고정 JSON·정상 Case 한 건만으로 완료 처리
- 교육용 고객 업무를 `cpf-core`, `cpf-starters/common`, Starter, Runtime 제품 내부에 삽입
- Module-local DB Vendor Fallback SQL 또는 Vendor별 의미가 다른 Schema
- Timeout이나 응답 유실 뒤 실제 결과 확인 없이 무조건 재호출
- 비밀번호·Token·개인정보 원문을 Source·Log·Fixture·문서에 기록
- 매뉴얼 설명과 다른 임의 Package·API·상태·메뉴를 새로 생성

---

# 112. EDU-BZA-09 — 입사·이동·휴직·퇴사 Joiner-Mover-Leaver

- 우선순위: **P1**
- 연결 매뉴얼: `90_BZA매뉴얼.md`
- Owner: `cpf-biz-admin + 고객 업무 Owner`
- 구현 상태 목표: Source·SQL·API·Config·Test·운영 확인·매뉴얼 연결이 모두 완료

## 필요 근거

- 고객 매뉴얼에서 `입사·이동·휴직·퇴사 Joiner-Mover-Leaver`을 설명하려면 정상 호출 예시만이 아니라 상태 변화와 장애 후 정상화까지 재현 가능한 실행 예제가 필요하다.
- 이 요구사항이 없으면 고객은 기능 가능 여부를 알더라도 실제 업무 적용 순서, 실패 판정, 재처리 금지 조건을 확인할 수 없다.
- 내부 구현 구조를 보여 주기 위한 예제가 아니라 고객 업무를 CPF로 만드는 방법과 운영 결과를 증명하는 예제로 구현한다.

## 고객 업무 시나리오

직원 상태 변화에 맞춰 사용자·역할·세션·위임을 생성·변경·회수한다.

## 구현 위치와 책임 경계

- 업무 Source: `com.cpf.education.edu.bza09` 또는 같은 의미의 기존 표준 Slice
- Test: Owner Module의 기존 Test Root 아래 `EDU-BZA-09` 식별 가능한 Class·Fixture
- SQL: 중앙 DB Vendor Pack의 기존 Install·Upgrade·Rollback·Verify·Seed 구조
- Config: 기존 Prefix·Profile·Secret 참조 방식을 유지
- 고객 업무 상태 변경과 원장 데이터는 고객 업무 Owner가 소유한다. ADM·BZA·Gateway·운영 Script가 업무 Table을 직접 수정하지 않는다.
- 공통 기능이 필요하면 기존 Public API·SPI를 우선 사용하고, 진입점이 없을 때만 실제 Consumer를 근거로 Owner Module에 확장한다.

## 입력·계약

- `employeeId`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `eventType`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `effectiveAt`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- 해당 기능에 필요한 Operation ID·업무 ID·Correlation ID·요청 Hash·사용자·권한 문맥을 저장·Log·Trace·Audit에서 연결한다.
- 요청·응답·Job Parameter·이벤트·파일 Layout 중 해당되는 계약을 OpenAPI 또는 Machine-readable Schema로 제공한다.

## 상태와 저장 근거

- 필수 상태 흐름: `PLANNED → APPLIED; ACTIVE/LEAVE/TERMINATED`
- 상태마다 진입 조건, 허용 조치, 재시도 가능 여부, 최종 여부, 저장 위치와 Version 규칙을 정의한다.
- 결과 미확정·부분 성공이 가능한 경우 대상별 Attempt와 상대 상태 조회 근거를 별도 저장한다.

## 정상 처리 순서

1. 입력·권한·데이터 범위·기준 Version과 선행 상태를 검증한다.
2. 상태 변경·비동기 실행인 경우 Operation과 업무 식별자를 생성하거나 기존 멱등 요청을 조회한다. 조회 전용 기능은 조회 Context와 Cursor만 사용한다.
3. 상태 변경 기능은 고객 업무 Owner의 Application Use Case에서 Transaction 경계와 상태 전이를 수행하고, 조회 전용 기능은 일관된 Snapshot·정렬·범위 조건을 적용한다.
4. 외부 시스템·Kafka·파일·Batch·Gateway가 필요한 경우 Adapter 경계에서 Timeout·Retry·보안 정책을 적용한다.
5. 업무 결과와 Attempt·Audit·Log·Metric·Trace를 같은 식별자로 연결한다.
6. 고객 API 또는 운영 화면에서 최종 상태·건수·금액·Version·오류 0건을 확인한다.

## 예외·장애·경계 사례

- 미래 발령
- 당일 여러 발령
- 퇴사 후 세션
- 미결 결재
- 상태 변경이 있는 경우 DB Commit 전 실패와 Commit 후 응답 유실을 구분한다.
- 같은 요청의 중복·동시 실행·순서 역전·늦은 응답을 포함한다.
- Retry 가능한 오류와 즉시 중단해야 하는 업무 오류를 구분한다.
- 대량·다중 대상 기능에서 부분 성공이 발생하면 성공 대상을 다시 실행하지 않고 실패 대상만 Reprocess·Reconcile한다.
- 복구·보상이 필요한 기능은 복구 실패와 보상 실패도 별도 상태·Attempt·Audit로 남긴다.

## 필수 Test

- JML workflow
- role cleanup
- session revoke
- audit
- 입력 Validation·권한·Data Scope·Masking Negative Test
- 해당 기능에 적용되는 중복·동시성·Timeout·응답 유실·부분 실패 Test
- 해당 기능의 Failure Injection 후 필요한 Retry·Restart·Reprocess·Reconcile·Rollback Test
- Oracle·PostgreSQL·MariaDB 또는 해당 Infra의 의미 동일성 Test
- Log·Metric·Trace·Audit에 Secret·개인정보 원문이 없는지 검사

## 운영·화면 확인

- Employees·Assignments·Users·User Roles·Sessions E2E
- 화면에는 해당 메뉴에 실제로 존재하는 조회 조건, 기본값, Column, 상세 Field, 상태, Button 활성 조건, 사유, 승인, 예상 Version, 대상별 결과만 Source와 일치하게 표시한다. 조회 전용 메뉴에 Command 항목을 만들지 않는다.
- 운영자가 정상화 완료를 판단할 수 있는 건수·금액·Version·Checksum·최종 상태·마지막 오류를 제공한다.
- 변경·외부연계 기능에서 응답 유실이 가능한 경우에만 요청을 다시 보내기 전에 Operation·거래·상대 상태를 조회하는 절차를 Browser 또는 API Test로 검증한다.

## 고객 매뉴얼·EDU 연결

- `90_BZA매뉴얼.md`의 해당 기능 장에 `EDU-BZA-09`를 연결한다.
- 선행 환경, 실행 명령, 입력값, 정상 출력, 오류 재현 명령, 복구 명령, ADM/BZA/Gateway 확인 위치를 그대로 따라 할 수 있게 작성한다.
- 교육 코드에서 고객 업무로 바꿀 Package·Table·API·Topic·파일명과 CPF 관리 영역을 구분한 전환표를 제공한다.

## 완료 조건

- `EDU-BZA-09`가 교육 Catalog·Source·Test·고객 매뉴얼에 동일 ID로 존재한다.
- 정상·Validation·권한·중복·동시성·Timeout·응답 유실·부분 실패·복구 Test가 통과한다.
- 요청·응답·상태·DB·이벤트·파일·운영 화면이 양방향으로 일치한다.
- 운영자가 다른 사람의 설명 없이 장애를 재현하고 정상화 판정을 할 수 있다.
- 실행하지 않은 Test는 성공으로 기록하지 않고, 실패·제약이 남으면 전체 완료로 보고하지 않는다.

## 금지 사항

- 메모리 Map·고정 JSON·정상 Case 한 건만으로 완료 처리
- 교육용 고객 업무를 `cpf-core`, `cpf-starters/common`, Starter, Runtime 제품 내부에 삽입
- Module-local DB Vendor Fallback SQL 또는 Vendor별 의미가 다른 Schema
- Timeout이나 응답 유실 뒤 실제 결과 확인 없이 무조건 재호출
- 비밀번호·Token·개인정보 원문을 Source·Log·Fixture·문서에 기록
- 매뉴얼 설명과 다른 임의 Package·API·상태·메뉴를 새로 생성

---

# 113. EDU-BZA-10 — 역할 충돌·직무분리·실효 권한 Simulation

- 우선순위: **P1**
- 연결 매뉴얼: `90_BZA매뉴얼.md`
- Owner: `cpf-biz-admin + 고객 업무 Owner`
- 구현 상태 목표: Source·SQL·API·Config·Test·운영 확인·매뉴얼 연결이 모두 완료

## 필요 근거

- 고객 매뉴얼에서 `역할 충돌·직무분리·실효 권한 Simulation`을 설명하려면 정상 호출 예시만이 아니라 상태 변화와 장애 후 정상화까지 재현 가능한 실행 예제가 필요하다.
- 이 요구사항이 없으면 고객은 기능 가능 여부를 알더라도 실제 업무 적용 순서, 실패 판정, 재처리 금지 조건을 확인할 수 없다.
- 내부 구현 구조를 보여 주기 위한 예제가 아니라 고객 업무를 CPF로 만드는 방법과 운영 결과를 증명하는 예제로 구현한다.

## 고객 업무 시나리오

요청자와 승인자 역할을 동시에 갖지 못하도록 충돌 정책을 검사한다.

## 구현 위치와 책임 경계

- 업무 Source: `com.cpf.education.edu.bza10` 또는 같은 의미의 기존 표준 Slice
- Test: Owner Module의 기존 Test Root 아래 `EDU-BZA-10` 식별 가능한 Class·Fixture
- SQL: 중앙 DB Vendor Pack의 기존 Install·Upgrade·Rollback·Verify·Seed 구조
- Config: 기존 Prefix·Profile·Secret 참조 방식을 유지
- 고객 업무 상태 변경과 원장 데이터는 고객 업무 Owner가 소유한다. ADM·BZA·Gateway·운영 Script가 업무 Table을 직접 수정하지 않는다.
- 공통 기능이 필요하면 기존 Public API·SPI를 우선 사용하고, 진입점이 없을 때만 실제 Consumer를 근거로 Owner Module에 확장한다.

## 입력·계약

- `subjectId`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `requestedRoles`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `asOfDate`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- 해당 기능에 필요한 Operation ID·업무 ID·Correlation ID·요청 Hash·사용자·권한 문맥을 저장·Log·Trace·Audit에서 연결한다.
- 요청·응답·Job Parameter·이벤트·파일 Layout 중 해당되는 계약을 OpenAPI 또는 Machine-readable Schema로 제공한다.

## 상태와 저장 근거

- 필수 상태 흐름: `SIMULATED → ALLOWED/DENIED → ASSIGNED`
- 상태마다 진입 조건, 허용 조치, 재시도 가능 여부, 최종 여부, 저장 위치와 Version 규칙을 정의한다.
- 결과 미확정·부분 성공이 가능한 경우 대상별 Attempt와 상대 상태 조회 근거를 별도 저장한다.

## 정상 처리 순서

1. 입력·권한·데이터 범위·기준 Version과 선행 상태를 검증한다.
2. 상태 변경·비동기 실행인 경우 Operation과 업무 식별자를 생성하거나 기존 멱등 요청을 조회한다. 조회 전용 기능은 조회 Context와 Cursor만 사용한다.
3. 상태 변경 기능은 고객 업무 Owner의 Application Use Case에서 Transaction 경계와 상태 전이를 수행하고, 조회 전용 기능은 일관된 Snapshot·정렬·범위 조건을 적용한다.
4. 외부 시스템·Kafka·파일·Batch·Gateway가 필요한 경우 Adapter 경계에서 Timeout·Retry·보안 정책을 적용한다.
5. 업무 결과와 Attempt·Audit·Log·Metric·Trace를 같은 식별자로 연결한다.
6. 고객 API 또는 운영 화면에서 최종 상태·건수·금액·Version·오류 0건을 확인한다.

## 예외·장애·경계 사례

- 간접 역할
- 기간 겹침
- Data Scope 결합
- 긴급 권한
- 상태 변경이 있는 경우 DB Commit 전 실패와 Commit 후 응답 유실을 구분한다.
- 같은 요청의 중복·동시 실행·순서 역전·늦은 응답을 포함한다.
- Retry 가능한 오류와 즉시 중단해야 하는 업무 오류를 구분한다.
- 대량·다중 대상 기능에서 부분 성공이 발생하면 성공 대상을 다시 실행하지 않고 실패 대상만 Reprocess·Reconcile한다.
- 복구·보상이 필요한 기능은 복구 실패와 보상 실패도 별도 상태·Attempt·Audit로 남긴다.

## 필수 Test

- SoD rule
- effective permission calculation
- approval
- 입력 Validation·권한·Data Scope·Masking Negative Test
- 해당 기능에 적용되는 중복·동시성·Timeout·응답 유실·부분 실패 Test
- 해당 기능의 Failure Injection 후 필요한 Retry·Restart·Reprocess·Reconcile·Rollback Test
- Oracle·PostgreSQL·MariaDB 또는 해당 Infra의 의미 동일성 Test
- Log·Metric·Trace·Audit에 Secret·개인정보 원문이 없는지 검사

## 운영·화면 확인

- Roles·Permissions·Permission Tools 메뉴 E2E
- 화면에는 해당 메뉴에 실제로 존재하는 조회 조건, 기본값, Column, 상세 Field, 상태, Button 활성 조건, 사유, 승인, 예상 Version, 대상별 결과만 Source와 일치하게 표시한다. 조회 전용 메뉴에 Command 항목을 만들지 않는다.
- 운영자가 정상화 완료를 판단할 수 있는 건수·금액·Version·Checksum·최종 상태·마지막 오류를 제공한다.
- 변경·외부연계 기능에서 응답 유실이 가능한 경우에만 요청을 다시 보내기 전에 Operation·거래·상대 상태를 조회하는 절차를 Browser 또는 API Test로 검증한다.

## 고객 매뉴얼·EDU 연결

- `90_BZA매뉴얼.md`의 해당 기능 장에 `EDU-BZA-10`를 연결한다.
- 선행 환경, 실행 명령, 입력값, 정상 출력, 오류 재현 명령, 복구 명령, ADM/BZA/Gateway 확인 위치를 그대로 따라 할 수 있게 작성한다.
- 교육 코드에서 고객 업무로 바꿀 Package·Table·API·Topic·파일명과 CPF 관리 영역을 구분한 전환표를 제공한다.

## 완료 조건

- `EDU-BZA-10`가 교육 Catalog·Source·Test·고객 매뉴얼에 동일 ID로 존재한다.
- 정상·Validation·권한·중복·동시성·Timeout·응답 유실·부분 실패·복구 Test가 통과한다.
- 요청·응답·상태·DB·이벤트·파일·운영 화면이 양방향으로 일치한다.
- 운영자가 다른 사람의 설명 없이 장애를 재현하고 정상화 판정을 할 수 있다.
- 실행하지 않은 Test는 성공으로 기록하지 않고, 실패·제약이 남으면 전체 완료로 보고하지 않는다.

## 금지 사항

- 메모리 Map·고정 JSON·정상 Case 한 건만으로 완료 처리
- 교육용 고객 업무를 `cpf-core`, `cpf-starters/common`, Starter, Runtime 제품 내부에 삽입
- Module-local DB Vendor Fallback SQL 또는 Vendor별 의미가 다른 Schema
- Timeout이나 응답 유실 뒤 실제 결과 확인 없이 무조건 재호출
- 비밀번호·Token·개인정보 원문을 Source·Log·Fixture·문서에 기록
- 매뉴얼 설명과 다른 임의 Package·API·상태·메뉴를 새로 생성

---

# 114. EDU-BZA-11 — 위임 중첩·기간 만료·결재 경로 재계산

- 우선순위: **P1**
- 연결 매뉴얼: `90_BZA매뉴얼.md`
- Owner: `cpf-biz-admin + 고객 업무 Owner`
- 구현 상태 목표: Source·SQL·API·Config·Test·운영 확인·매뉴얼 연결이 모두 완료

## 필요 근거

- 고객 매뉴얼에서 `위임 중첩·기간 만료·결재 경로 재계산`을 설명하려면 정상 호출 예시만이 아니라 상태 변화와 장애 후 정상화까지 재현 가능한 실행 예제가 필요하다.
- 이 요구사항이 없으면 고객은 기능 가능 여부를 알더라도 실제 업무 적용 순서, 실패 판정, 재처리 금지 조건을 확인할 수 없다.
- 내부 구현 구조를 보여 주기 위한 예제가 아니라 고객 업무를 CPF로 만드는 방법과 운영 결과를 증명하는 예제로 구현한다.

## 고객 업무 시나리오

휴가 위임이 겹칠 때 우선순위를 정하고 만료 시 원 담당자로 복귀한다.

## 구현 위치와 책임 경계

- 업무 Source: `com.cpf.education.edu.bza11` 또는 같은 의미의 기존 표준 Slice
- Test: Owner Module의 기존 Test Root 아래 `EDU-BZA-11` 식별 가능한 Class·Fixture
- SQL: 중앙 DB Vendor Pack의 기존 Install·Upgrade·Rollback·Verify·Seed 구조
- Config: 기존 Prefix·Profile·Secret 참조 방식을 유지
- 고객 업무 상태 변경과 원장 데이터는 고객 업무 Owner가 소유한다. ADM·BZA·Gateway·운영 Script가 업무 Table을 직접 수정하지 않는다.
- 공통 기능이 필요하면 기존 Public API·SPI를 우선 사용하고, 진입점이 없을 때만 실제 Consumer를 근거로 Owner Module에 확장한다.

## 입력·계약

- `delegator`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `delegatee`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `period`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `scope`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- 해당 기능에 필요한 Operation ID·업무 ID·Correlation ID·요청 Hash·사용자·권한 문맥을 저장·Log·Trace·Audit에서 연결한다.
- 요청·응답·Job Parameter·이벤트·파일 Layout 중 해당되는 계약을 OpenAPI 또는 Machine-readable Schema로 제공한다.

## 상태와 저장 근거

- 필수 상태 흐름: `DRAFT → ACTIVE → EXPIRED/REVOKED`
- 상태마다 진입 조건, 허용 조치, 재시도 가능 여부, 최종 여부, 저장 위치와 Version 규칙을 정의한다.
- 결과 미확정·부분 성공이 가능한 경우 대상별 Attempt와 상대 상태 조회 근거를 별도 저장한다.

## 정상 처리 순서

1. 입력·권한·데이터 범위·기준 Version과 선행 상태를 검증한다.
2. 상태 변경·비동기 실행인 경우 Operation과 업무 식별자를 생성하거나 기존 멱등 요청을 조회한다. 조회 전용 기능은 조회 Context와 Cursor만 사용한다.
3. 상태 변경 기능은 고객 업무 Owner의 Application Use Case에서 Transaction 경계와 상태 전이를 수행하고, 조회 전용 기능은 일관된 Snapshot·정렬·범위 조건을 적용한다.
4. 외부 시스템·Kafka·파일·Batch·Gateway가 필요한 경우 Adapter 경계에서 Timeout·Retry·보안 정책을 적용한다.
5. 업무 결과와 Attempt·Audit·Log·Metric·Trace를 같은 식별자로 연결한다.
6. 고객 API 또는 운영 화면에서 최종 상태·건수·금액·Version·오류 0건을 확인한다.

## 예외·장애·경계 사례

- 상호 위임
- 순환
- 중첩 범위
- 위임자 퇴사
- 상태 변경이 있는 경우 DB Commit 전 실패와 Commit 후 응답 유실을 구분한다.
- 같은 요청의 중복·동시 실행·순서 역전·늦은 응답을 포함한다.
- Retry 가능한 오류와 즉시 중단해야 하는 업무 오류를 구분한다.
- 대량·다중 대상 기능에서 부분 성공이 발생하면 성공 대상을 다시 실행하지 않고 실패 대상만 Reprocess·Reconcile한다.
- 복구·보상이 필요한 기능은 복구 실패와 보상 실패도 별도 상태·Attempt·Audit로 남긴다.

## 필수 Test

- Cycle detection
- effective period
- route simulation
- 입력 Validation·권한·Data Scope·Masking Negative Test
- 해당 기능에 적용되는 중복·동시성·Timeout·응답 유실·부분 실패 Test
- 해당 기능의 Failure Injection 후 필요한 Retry·Restart·Reprocess·Reconcile·Rollback Test
- Oracle·PostgreSQL·MariaDB 또는 해당 Infra의 의미 동일성 Test
- Log·Metric·Trace·Audit에 Secret·개인정보 원문이 없는지 검사

## 운영·화면 확인

- Approval Delegations·Simulation·Inbox E2E
- 화면에는 해당 메뉴에 실제로 존재하는 조회 조건, 기본값, Column, 상세 Field, 상태, Button 활성 조건, 사유, 승인, 예상 Version, 대상별 결과만 Source와 일치하게 표시한다. 조회 전용 메뉴에 Command 항목을 만들지 않는다.
- 운영자가 정상화 완료를 판단할 수 있는 건수·금액·Version·Checksum·최종 상태·마지막 오류를 제공한다.
- 변경·외부연계 기능에서 응답 유실이 가능한 경우에만 요청을 다시 보내기 전에 Operation·거래·상대 상태를 조회하는 절차를 Browser 또는 API Test로 검증한다.

## 고객 매뉴얼·EDU 연결

- `90_BZA매뉴얼.md`의 해당 기능 장에 `EDU-BZA-11`를 연결한다.
- 선행 환경, 실행 명령, 입력값, 정상 출력, 오류 재현 명령, 복구 명령, ADM/BZA/Gateway 확인 위치를 그대로 따라 할 수 있게 작성한다.
- 교육 코드에서 고객 업무로 바꿀 Package·Table·API·Topic·파일명과 CPF 관리 영역을 구분한 전환표를 제공한다.

## 완료 조건

- `EDU-BZA-11`가 교육 Catalog·Source·Test·고객 매뉴얼에 동일 ID로 존재한다.
- 정상·Validation·권한·중복·동시성·Timeout·응답 유실·부분 실패·복구 Test가 통과한다.
- 요청·응답·상태·DB·이벤트·파일·운영 화면이 양방향으로 일치한다.
- 운영자가 다른 사람의 설명 없이 장애를 재현하고 정상화 판정을 할 수 있다.
- 실행하지 않은 Test는 성공으로 기록하지 않고, 실패·제약이 남으면 전체 완료로 보고하지 않는다.

## 금지 사항

- 메모리 Map·고정 JSON·정상 Case 한 건만으로 완료 처리
- 교육용 고객 업무를 `cpf-core`, `cpf-starters/common`, Starter, Runtime 제품 내부에 삽입
- Module-local DB Vendor Fallback SQL 또는 Vendor별 의미가 다른 Schema
- Timeout이나 응답 유실 뒤 실제 결과 확인 없이 무조건 재호출
- 비밀번호·Token·개인정보 원문을 Source·Log·Fixture·문서에 기록
- 매뉴얼 설명과 다른 임의 Package·API·상태·메뉴를 새로 생성

---

# 115. EDU-BZA-12 — 계정 잠금·비밀번호 초기화·세션 강제 종료

- 우선순위: **P1**
- 연결 매뉴얼: `90_BZA매뉴얼.md`
- Owner: `cpf-biz-admin + 고객 업무 Owner`
- 구현 상태 목표: Source·SQL·API·Config·Test·운영 확인·매뉴얼 연결이 모두 완료

## 필요 근거

- 고객 매뉴얼에서 `계정 잠금·비밀번호 초기화·세션 강제 종료`을 설명하려면 정상 호출 예시만이 아니라 상태 변화와 장애 후 정상화까지 재현 가능한 실행 예제가 필요하다.
- 이 요구사항이 없으면 고객은 기능 가능 여부를 알더라도 실제 업무 적용 순서, 실패 판정, 재처리 금지 조건을 확인할 수 없다.
- 내부 구현 구조를 보여 주기 위한 예제가 아니라 고객 업무를 CPF로 만드는 방법과 운영 결과를 증명하는 예제로 구현한다.

## 고객 업무 시나리오

실패 횟수 초과 계정을 잠그고 승인된 초기화 후 모든 기존 세션을 종료한다.

## 구현 위치와 책임 경계

- 업무 Source: `com.cpf.education.edu.bza12` 또는 같은 의미의 기존 표준 Slice
- Test: Owner Module의 기존 Test Root 아래 `EDU-BZA-12` 식별 가능한 Class·Fixture
- SQL: 중앙 DB Vendor Pack의 기존 Install·Upgrade·Rollback·Verify·Seed 구조
- Config: 기존 Prefix·Profile·Secret 참조 방식을 유지
- 고객 업무 상태 변경과 원장 데이터는 고객 업무 Owner가 소유한다. ADM·BZA·Gateway·운영 Script가 업무 Table을 직접 수정하지 않는다.
- 공통 기능이 필요하면 기존 Public API·SPI를 우선 사용하고, 진입점이 없을 때만 실제 Consumer를 근거로 Owner Module에 확장한다.

## 입력·계약

- `loginId`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `resetReason`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `approvalId`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- 해당 기능에 필요한 Operation ID·업무 ID·Correlation ID·요청 Hash·사용자·권한 문맥을 저장·Log·Trace·Audit에서 연결한다.
- 요청·응답·Job Parameter·이벤트·파일 Layout 중 해당되는 계약을 OpenAPI 또는 Machine-readable Schema로 제공한다.

## 상태와 저장 근거

- 필수 상태 흐름: `ACTIVE → LOCKED → RESET_REQUIRED → ACTIVE`
- 상태마다 진입 조건, 허용 조치, 재시도 가능 여부, 최종 여부, 저장 위치와 Version 규칙을 정의한다.
- 결과 미확정·부분 성공이 가능한 경우 대상별 Attempt와 상대 상태 조회 근거를 별도 저장한다.

## 정상 처리 순서

1. 입력·권한·데이터 범위·기준 Version과 선행 상태를 검증한다.
2. 상태 변경·비동기 실행인 경우 Operation과 업무 식별자를 생성하거나 기존 멱등 요청을 조회한다. 조회 전용 기능은 조회 Context와 Cursor만 사용한다.
3. 상태 변경 기능은 고객 업무 Owner의 Application Use Case에서 Transaction 경계와 상태 전이를 수행하고, 조회 전용 기능은 일관된 Snapshot·정렬·범위 조건을 적용한다.
4. 외부 시스템·Kafka·파일·Batch·Gateway가 필요한 경우 Adapter 경계에서 Timeout·Retry·보안 정책을 적용한다.
5. 업무 결과와 Attempt·Audit·Log·Metric·Trace를 같은 식별자로 연결한다.
6. 고객 API 또는 운영 화면에서 최종 상태·건수·금액·Version·오류 0건을 확인한다.

## 예외·장애·경계 사례

- 동시 로그인
- 초기화 링크 재사용
- 관리자 자기 초기화
- 세션 잔존
- 상태 변경이 있는 경우 DB Commit 전 실패와 Commit 후 응답 유실을 구분한다.
- 같은 요청의 중복·동시 실행·순서 역전·늦은 응답을 포함한다.
- Retry 가능한 오류와 즉시 중단해야 하는 업무 오류를 구분한다.
- 대량·다중 대상 기능에서 부분 성공이 발생하면 성공 대상을 다시 실행하지 않고 실패 대상만 Reprocess·Reconcile한다.
- 복구·보상이 필요한 기능은 복구 실패와 보상 실패도 별도 상태·Attempt·Audit로 남긴다.

## 필수 Test

- Password policy
- token one-time
- session revocation
- 입력 Validation·권한·Data Scope·Masking Negative Test
- 해당 기능에 적용되는 중복·동시성·Timeout·응답 유실·부분 실패 Test
- 해당 기능의 Failure Injection 후 필요한 Retry·Restart·Reprocess·Reconcile·Rollback Test
- Oracle·PostgreSQL·MariaDB 또는 해당 Infra의 의미 동일성 Test
- Log·Metric·Trace·Audit에 Secret·개인정보 원문이 없는지 검사

## 운영·화면 확인

- Users·Sessions·Audits 메뉴 E2E
- 화면에는 해당 메뉴에 실제로 존재하는 조회 조건, 기본값, Column, 상세 Field, 상태, Button 활성 조건, 사유, 승인, 예상 Version, 대상별 결과만 Source와 일치하게 표시한다. 조회 전용 메뉴에 Command 항목을 만들지 않는다.
- 운영자가 정상화 완료를 판단할 수 있는 건수·금액·Version·Checksum·최종 상태·마지막 오류를 제공한다.
- 변경·외부연계 기능에서 응답 유실이 가능한 경우에만 요청을 다시 보내기 전에 Operation·거래·상대 상태를 조회하는 절차를 Browser 또는 API Test로 검증한다.

## 고객 매뉴얼·EDU 연결

- `90_BZA매뉴얼.md`의 해당 기능 장에 `EDU-BZA-12`를 연결한다.
- 선행 환경, 실행 명령, 입력값, 정상 출력, 오류 재현 명령, 복구 명령, ADM/BZA/Gateway 확인 위치를 그대로 따라 할 수 있게 작성한다.
- 교육 코드에서 고객 업무로 바꿀 Package·Table·API·Topic·파일명과 CPF 관리 영역을 구분한 전환표를 제공한다.

## 완료 조건

- `EDU-BZA-12`가 교육 Catalog·Source·Test·고객 매뉴얼에 동일 ID로 존재한다.
- 정상·Validation·권한·중복·동시성·Timeout·응답 유실·부분 실패·복구 Test가 통과한다.
- 요청·응답·상태·DB·이벤트·파일·운영 화면이 양방향으로 일치한다.
- 운영자가 다른 사람의 설명 없이 장애를 재현하고 정상화 판정을 할 수 있다.
- 실행하지 않은 Test는 성공으로 기록하지 않고, 실패·제약이 남으면 전체 완료로 보고하지 않는다.

## 금지 사항

- 메모리 Map·고정 JSON·정상 Case 한 건만으로 완료 처리
- 교육용 고객 업무를 `cpf-core`, `cpf-starters/common`, Starter, Runtime 제품 내부에 삽입
- Module-local DB Vendor Fallback SQL 또는 Vendor별 의미가 다른 Schema
- Timeout이나 응답 유실 뒤 실제 결과 확인 없이 무조건 재호출
- 비밀번호·Token·개인정보 원문을 Source·Log·Fixture·문서에 기록
- 매뉴얼 설명과 다른 임의 Package·API·상태·메뉴를 새로 생성

---

# 116. EDU-BZA-13 — 개인정보 Masking·감사 조회·승인 Export

- 우선순위: **P1**
- 연결 매뉴얼: `90_BZA매뉴얼.md`
- Owner: `cpf-biz-admin + 고객 업무 Owner`
- 구현 상태 목표: Source·SQL·API·Config·Test·운영 확인·매뉴얼 연결이 모두 완료

## 필요 근거

- 고객 매뉴얼에서 `개인정보 Masking·감사 조회·승인 Export`을 설명하려면 정상 호출 예시만이 아니라 상태 변화와 장애 후 정상화까지 재현 가능한 실행 예제가 필요하다.
- 이 요구사항이 없으면 고객은 기능 가능 여부를 알더라도 실제 업무 적용 순서, 실패 판정, 재처리 금지 조건을 확인할 수 없다.
- 내부 구현 구조를 보여 주기 위한 예제가 아니라 고객 업무를 CPF로 만드는 방법과 운영 결과를 증명하는 예제로 구현한다.

## 고객 업무 시나리오

인사정보를 역할별 Masking하고 승인된 감사자만 제한 기간 Export한다.

## 구현 위치와 책임 경계

- 업무 Source: `com.cpf.education.edu.bza13` 또는 같은 의미의 기존 표준 Slice
- Test: Owner Module의 기존 Test Root 아래 `EDU-BZA-13` 식별 가능한 Class·Fixture
- SQL: 중앙 DB Vendor Pack의 기존 Install·Upgrade·Rollback·Verify·Seed 구조
- Config: 기존 Prefix·Profile·Secret 참조 방식을 유지
- 고객 업무 상태 변경과 원장 데이터는 고객 업무 Owner가 소유한다. ADM·BZA·Gateway·운영 Script가 업무 Table을 직접 수정하지 않는다.
- 공통 기능이 필요하면 기존 Public API·SPI를 우선 사용하고, 진입점이 없을 때만 실제 Consumer를 근거로 Owner Module에 확장한다.

## 입력·계약

- `filters`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `fields`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `maskingPolicy`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `approvalId`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- 해당 기능에 필요한 Operation ID·업무 ID·Correlation ID·요청 Hash·사용자·권한 문맥을 저장·Log·Trace·Audit에서 연결한다.
- 요청·응답·Job Parameter·이벤트·파일 Layout 중 해당되는 계약을 OpenAPI 또는 Machine-readable Schema로 제공한다.

## 상태와 저장 근거

- 필수 상태 흐름: `QUERY_MASKED → EXPORT_REQUESTED → READY`
- 상태마다 진입 조건, 허용 조치, 재시도 가능 여부, 최종 여부, 저장 위치와 Version 규칙을 정의한다.
- 결과 미확정·부분 성공이 가능한 경우 대상별 Attempt와 상대 상태 조회 근거를 별도 저장한다.

## 정상 처리 순서

1. 입력·권한·데이터 범위·기준 Version과 선행 상태를 검증한다.
2. 상태 변경·비동기 실행인 경우 Operation과 업무 식별자를 생성하거나 기존 멱등 요청을 조회한다. 조회 전용 기능은 조회 Context와 Cursor만 사용한다.
3. 상태 변경 기능은 고객 업무 Owner의 Application Use Case에서 Transaction 경계와 상태 전이를 수행하고, 조회 전용 기능은 일관된 Snapshot·정렬·범위 조건을 적용한다.
4. 외부 시스템·Kafka·파일·Batch·Gateway가 필요한 경우 Adapter 경계에서 Timeout·Retry·보안 정책을 적용한다.
5. 업무 결과와 Attempt·Audit·Log·Metric·Trace를 같은 식별자로 연결한다.
6. 고객 API 또는 운영 화면에서 최종 상태·건수·금액·Version·오류 0건을 확인한다.

## 예외·장애·경계 사례

- 원문 Column 노출
- 저장 검색 재사용
- 다운로드 공유
- 승인 만료
- 상태 변경이 있는 경우 DB Commit 전 실패와 Commit 후 응답 유실을 구분한다.
- 같은 요청의 중복·동시 실행·순서 역전·늦은 응답을 포함한다.
- Retry 가능한 오류와 즉시 중단해야 하는 업무 오류를 구분한다.
- 대량·다중 대상 기능에서 부분 성공이 발생하면 성공 대상을 다시 실행하지 않고 실패 대상만 Reprocess·Reconcile한다.
- 복구·보상이 필요한 기능은 복구 실패와 보상 실패도 별도 상태·Attempt·Audit로 남긴다.

## 필수 Test

- Field policy
- export hash
- download audit
- browser
- 입력 Validation·권한·Data Scope·Masking Negative Test
- 해당 기능에 적용되는 중복·동시성·Timeout·응답 유실·부분 실패 Test
- 해당 기능의 Failure Injection 후 필요한 Retry·Restart·Reprocess·Reconcile·Rollback Test
- Oracle·PostgreSQL·MariaDB 또는 해당 Infra의 의미 동일성 Test
- Log·Metric·Trace·Audit에 Secret·개인정보 원문이 없는지 검사

## 운영·화면 확인

- Employees·Audits·Downloads 메뉴 E2E
- 화면에는 해당 메뉴에 실제로 존재하는 조회 조건, 기본값, Column, 상세 Field, 상태, Button 활성 조건, 사유, 승인, 예상 Version, 대상별 결과만 Source와 일치하게 표시한다. 조회 전용 메뉴에 Command 항목을 만들지 않는다.
- 운영자가 정상화 완료를 판단할 수 있는 건수·금액·Version·Checksum·최종 상태·마지막 오류를 제공한다.
- 변경·외부연계 기능에서 응답 유실이 가능한 경우에만 요청을 다시 보내기 전에 Operation·거래·상대 상태를 조회하는 절차를 Browser 또는 API Test로 검증한다.

## 고객 매뉴얼·EDU 연결

- `90_BZA매뉴얼.md`의 해당 기능 장에 `EDU-BZA-13`를 연결한다.
- 선행 환경, 실행 명령, 입력값, 정상 출력, 오류 재현 명령, 복구 명령, ADM/BZA/Gateway 확인 위치를 그대로 따라 할 수 있게 작성한다.
- 교육 코드에서 고객 업무로 바꿀 Package·Table·API·Topic·파일명과 CPF 관리 영역을 구분한 전환표를 제공한다.

## 완료 조건

- `EDU-BZA-13`가 교육 Catalog·Source·Test·고객 매뉴얼에 동일 ID로 존재한다.
- 정상·Validation·권한·중복·동시성·Timeout·응답 유실·부분 실패·복구 Test가 통과한다.
- 요청·응답·상태·DB·이벤트·파일·운영 화면이 양방향으로 일치한다.
- 운영자가 다른 사람의 설명 없이 장애를 재현하고 정상화 판정을 할 수 있다.
- 실행하지 않은 Test는 성공으로 기록하지 않고, 실패·제약이 남으면 전체 완료로 보고하지 않는다.

## 금지 사항

- 메모리 Map·고정 JSON·정상 Case 한 건만으로 완료 처리
- 교육용 고객 업무를 `cpf-core`, `cpf-starters/common`, Starter, Runtime 제품 내부에 삽입
- Module-local DB Vendor Fallback SQL 또는 Vendor별 의미가 다른 Schema
- Timeout이나 응답 유실 뒤 실제 결과 확인 없이 무조건 재호출
- 비밀번호·Token·개인정보 원문을 Source·Log·Fixture·문서에 기록
- 매뉴얼 설명과 다른 임의 Package·API·상태·메뉴를 새로 생성

---

# 117. EDU-BZA-14 — 고객 업무 승인 결과 반영·실패 Rollback

- 우선순위: **P1**
- 연결 매뉴얼: `90_BZA매뉴얼.md`
- Owner: `cpf-biz-admin + 고객 업무 Owner`
- 구현 상태 목표: Source·SQL·API·Config·Test·운영 확인·매뉴얼 연결이 모두 완료

## 필요 근거

- 고객 매뉴얼에서 `고객 업무 승인 결과 반영·실패 Rollback`을 설명하려면 정상 호출 예시만이 아니라 상태 변화와 장애 후 정상화까지 재현 가능한 실행 예제가 필요하다.
- 이 요구사항이 없으면 고객은 기능 가능 여부를 알더라도 실제 업무 적용 순서, 실패 판정, 재처리 금지 조건을 확인할 수 없다.
- 내부 구현 구조를 보여 주기 위한 예제가 아니라 고객 업무를 CPF로 만드는 방법과 운영 결과를 증명하는 예제로 구현한다.

## 고객 업무 시나리오

BZA 승인 완료 후 고객 업무 상태를 바꾸고 고객 API 실패 시 결과 미확정으로 대사한다.

## 구현 위치와 책임 경계

- 업무 Source: `com.cpf.education.edu.bza14` 또는 같은 의미의 기존 표준 Slice
- Test: Owner Module의 기존 Test Root 아래 `EDU-BZA-14` 식별 가능한 Class·Fixture
- SQL: 중앙 DB Vendor Pack의 기존 Install·Upgrade·Rollback·Verify·Seed 구조
- Config: 기존 Prefix·Profile·Secret 참조 방식을 유지
- 고객 업무 상태 변경과 원장 데이터는 고객 업무 Owner가 소유한다. ADM·BZA·Gateway·운영 Script가 업무 Table을 직접 수정하지 않는다.
- 공통 기능이 필요하면 기존 Public API·SPI를 우선 사용하고, 진입점이 없을 때만 실제 Consumer를 근거로 Owner Module에 확장한다.

## 입력·계약

- `approvalId`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `businessOperationId`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `expectedVersion`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- 해당 기능에 필요한 Operation ID·업무 ID·Correlation ID·요청 Hash·사용자·권한 문맥을 저장·Log·Trace·Audit에서 연결한다.
- 요청·응답·Job Parameter·이벤트·파일 Layout 중 해당되는 계약을 OpenAPI 또는 Machine-readable Schema로 제공한다.

## 상태와 저장 근거

- 필수 상태 흐름: `APPROVED → APPLYING → APPLIED/UNKNOWN_RESULT/COMPENSATED`
- 상태마다 진입 조건, 허용 조치, 재시도 가능 여부, 최종 여부, 저장 위치와 Version 규칙을 정의한다.
- 결과 미확정·부분 성공이 가능한 경우 대상별 Attempt와 상대 상태 조회 근거를 별도 저장한다.

## 정상 처리 순서

1. 입력·권한·데이터 범위·기준 Version과 선행 상태를 검증한다.
2. 상태 변경·비동기 실행인 경우 Operation과 업무 식별자를 생성하거나 기존 멱등 요청을 조회한다. 조회 전용 기능은 조회 Context와 Cursor만 사용한다.
3. 상태 변경 기능은 고객 업무 Owner의 Application Use Case에서 Transaction 경계와 상태 전이를 수행하고, 조회 전용 기능은 일관된 Snapshot·정렬·범위 조건을 적용한다.
4. 외부 시스템·Kafka·파일·Batch·Gateway가 필요한 경우 Adapter 경계에서 Timeout·Retry·보안 정책을 적용한다.
5. 업무 결과와 Attempt·Audit·Log·Metric·Trace를 같은 식별자로 연결한다.
6. 고객 API 또는 운영 화면에서 최종 상태·건수·금액·Version·오류 0건을 확인한다.

## 예외·장애·경계 사례

- 승인 후 업무 Version 변경
- 응답 유실
- 중복 Callback
- 부분 적용
- 상태 변경이 있는 경우 DB Commit 전 실패와 Commit 후 응답 유실을 구분한다.
- 같은 요청의 중복·동시 실행·순서 역전·늦은 응답을 포함한다.
- Retry 가능한 오류와 즉시 중단해야 하는 업무 오류를 구분한다.
- 대량·다중 대상 기능에서 부분 성공이 발생하면 성공 대상을 다시 실행하지 않고 실패 대상만 Reprocess·Reconcile한다.
- 복구·보상이 필요한 기능은 복구 실패와 보상 실패도 별도 상태·Attempt·Audit로 남긴다.

## 필수 Test

- Owner API idempotency
- reconcile
- compensation
- audit
- 입력 Validation·권한·Data Scope·Masking Negative Test
- 해당 기능에 적용되는 중복·동시성·Timeout·응답 유실·부분 실패 Test
- 해당 기능의 Failure Injection 후 필요한 Retry·Restart·Reprocess·Reconcile·Rollback Test
- Oracle·PostgreSQL·MariaDB 또는 해당 Infra의 의미 동일성 Test
- Log·Metric·Trace·Audit에 Secret·개인정보 원문이 없는지 검사

## 운영·화면 확인

- Approval Inbox/Submissions와 ADM Recovery 연결 E2E
- 화면에는 해당 메뉴에 실제로 존재하는 조회 조건, 기본값, Column, 상세 Field, 상태, Button 활성 조건, 사유, 승인, 예상 Version, 대상별 결과만 Source와 일치하게 표시한다. 조회 전용 메뉴에 Command 항목을 만들지 않는다.
- 운영자가 정상화 완료를 판단할 수 있는 건수·금액·Version·Checksum·최종 상태·마지막 오류를 제공한다.
- 변경·외부연계 기능에서 응답 유실이 가능한 경우에만 요청을 다시 보내기 전에 Operation·거래·상대 상태를 조회하는 절차를 Browser 또는 API Test로 검증한다.

## 고객 매뉴얼·EDU 연결

- `90_BZA매뉴얼.md`의 해당 기능 장에 `EDU-BZA-14`를 연결한다.
- 선행 환경, 실행 명령, 입력값, 정상 출력, 오류 재현 명령, 복구 명령, ADM/BZA/Gateway 확인 위치를 그대로 따라 할 수 있게 작성한다.
- 교육 코드에서 고객 업무로 바꿀 Package·Table·API·Topic·파일명과 CPF 관리 영역을 구분한 전환표를 제공한다.

## 완료 조건

- `EDU-BZA-14`가 교육 Catalog·Source·Test·고객 매뉴얼에 동일 ID로 존재한다.
- 정상·Validation·권한·중복·동시성·Timeout·응답 유실·부분 실패·복구 Test가 통과한다.
- 요청·응답·상태·DB·이벤트·파일·운영 화면이 양방향으로 일치한다.
- 운영자가 다른 사람의 설명 없이 장애를 재현하고 정상화 판정을 할 수 있다.
- 실행하지 않은 Test는 성공으로 기록하지 않고, 실패·제약이 남으면 전체 완료로 보고하지 않는다.

## 금지 사항

- 메모리 Map·고정 JSON·정상 Case 한 건만으로 완료 처리
- 교육용 고객 업무를 `cpf-core`, `cpf-starters/common`, Starter, Runtime 제품 내부에 삽입
- Module-local DB Vendor Fallback SQL 또는 Vendor별 의미가 다른 Schema
- Timeout이나 응답 유실 뒤 실제 결과 확인 없이 무조건 재호출
- 비밀번호·Token·개인정보 원문을 Source·Log·Fixture·문서에 기록
- 매뉴얼 설명과 다른 임의 Package·API·상태·메뉴를 새로 생성

---

# 118. EDU-GW-07 — Service Discovery·Target Failover·복귀

- 우선순위: **P1**
- 연결 매뉴얼: `91_Gateway매뉴얼.md`
- Owner: `cpf-gateway + cpf-education target service`
- 구현 상태 목표: Source·SQL·API·Config·Test·운영 확인·매뉴얼 연결이 모두 완료

## 필요 근거

- 고객 매뉴얼에서 `Service Discovery·Target Failover·복귀`을 설명하려면 정상 호출 예시만이 아니라 상태 변화와 장애 후 정상화까지 재현 가능한 실행 예제가 필요하다.
- 이 요구사항이 없으면 고객은 기능 가능 여부를 알더라도 실제 업무 적용 순서, 실패 판정, 재처리 금지 조건을 확인할 수 없다.
- 내부 구현 구조를 보여 주기 위한 예제가 아니라 고객 업무를 CPF로 만드는 방법과 운영 결과를 증명하는 예제로 구현한다.

## 고객 업무 시나리오

동적 인스턴스 목록에서 비정상 Target을 제외하고 복구 후 점진적으로 다시 투입한다.

## 구현 위치와 책임 경계

- 업무 Source: `com.cpf.education.edu.gateway07` 또는 같은 의미의 기존 표준 Slice
- Test: Owner Module의 기존 Test Root 아래 `EDU-GW-07` 식별 가능한 Class·Fixture
- SQL: 중앙 DB Vendor Pack의 기존 Install·Upgrade·Rollback·Verify·Seed 구조
- Config: 기존 Prefix·Profile·Secret 참조 방식을 유지
- 고객 업무 상태 변경과 원장 데이터는 고객 업무 Owner가 소유한다. ADM·BZA·Gateway·운영 Script가 업무 Table을 직접 수정하지 않는다.
- 공통 기능이 필요하면 기존 Public API·SPI를 우선 사용하고, 진입점이 없을 때만 실제 Consumer를 근거로 Owner Module에 확장한다.

## 입력·계약

- `serviceId`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `healthPolicy`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `warmup`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- 해당 기능에 필요한 Operation ID·업무 ID·Correlation ID·요청 Hash·사용자·권한 문맥을 저장·Log·Trace·Audit에서 연결한다.
- 요청·응답·Job Parameter·이벤트·파일 Layout 중 해당되는 계약을 OpenAPI 또는 Machine-readable Schema로 제공한다.

## 상태와 저장 근거

- 필수 상태 흐름: `DISCOVERED → HEALTHY/UNHEALTHY → DRAINED → WARMING → ACTIVE`
- 상태마다 진입 조건, 허용 조치, 재시도 가능 여부, 최종 여부, 저장 위치와 Version 규칙을 정의한다.
- 결과 미확정·부분 성공이 가능한 경우 대상별 Attempt와 상대 상태 조회 근거를 별도 저장한다.

## 정상 처리 순서

1. 입력·권한·데이터 범위·기준 Version과 선행 상태를 검증한다.
2. 상태 변경·비동기 실행인 경우 Operation과 업무 식별자를 생성하거나 기존 멱등 요청을 조회한다. 조회 전용 기능은 조회 Context와 Cursor만 사용한다.
3. 상태 변경 기능은 고객 업무 Owner의 Application Use Case에서 Transaction 경계와 상태 전이를 수행하고, 조회 전용 기능은 일관된 Snapshot·정렬·범위 조건을 적용한다.
4. 외부 시스템·Kafka·파일·Batch·Gateway가 필요한 경우 Adapter 경계에서 Timeout·Retry·보안 정책을 적용한다.
5. 업무 결과와 Attempt·Audit·Log·Metric·Trace를 같은 식별자로 연결한다.
6. 고객 API 또는 운영 화면에서 최종 상태·건수·금액·Version·오류 0건을 확인한다.

## 예외·장애·경계 사례

- Registry stale
- flapping
- 모든 Target down
- 세션 고정
- 상태 변경이 있는 경우 DB Commit 전 실패와 Commit 후 응답 유실을 구분한다.
- 같은 요청의 중복·동시 실행·순서 역전·늦은 응답을 포함한다.
- Retry 가능한 오류와 즉시 중단해야 하는 업무 오류를 구분한다.
- 대량·다중 대상 기능에서 부분 성공이 발생하면 성공 대상을 다시 실행하지 않고 실패 대상만 Reprocess·Reconcile한다.
- 복구·보상이 필요한 기능은 복구 실패와 보상 실패도 별도 상태·Attempt·Audit로 남긴다.

## 필수 Test

- Discovery refresh
- health debounce
- no-target response
- metrics
- 입력 Validation·권한·Data Scope·Masking Negative Test
- 해당 기능에 적용되는 중복·동시성·Timeout·응답 유실·부분 실패 Test
- 해당 기능의 Failure Injection 후 필요한 Retry·Restart·Reprocess·Reconcile·Rollback Test
- Oracle·PostgreSQL·MariaDB 또는 해당 Infra의 의미 동일성 Test
- Log·Metric·Trace·Audit에 Secret·개인정보 원문이 없는지 검사

## 운영·화면 확인

- Gateway Servers·Health·Dashboard E2E
- 화면에는 해당 메뉴에 실제로 존재하는 조회 조건, 기본값, Column, 상세 Field, 상태, Button 활성 조건, 사유, 승인, 예상 Version, 대상별 결과만 Source와 일치하게 표시한다. 조회 전용 메뉴에 Command 항목을 만들지 않는다.
- 운영자가 정상화 완료를 판단할 수 있는 건수·금액·Version·Checksum·최종 상태·마지막 오류를 제공한다.
- 변경·외부연계 기능에서 응답 유실이 가능한 경우에만 요청을 다시 보내기 전에 Operation·거래·상대 상태를 조회하는 절차를 Browser 또는 API Test로 검증한다.

## 고객 매뉴얼·EDU 연결

- `91_Gateway매뉴얼.md`의 해당 기능 장에 `EDU-GW-07`를 연결한다.
- 선행 환경, 실행 명령, 입력값, 정상 출력, 오류 재현 명령, 복구 명령, ADM/BZA/Gateway 확인 위치를 그대로 따라 할 수 있게 작성한다.
- 교육 코드에서 고객 업무로 바꿀 Package·Table·API·Topic·파일명과 CPF 관리 영역을 구분한 전환표를 제공한다.

## 완료 조건

- `EDU-GW-07`가 교육 Catalog·Source·Test·고객 매뉴얼에 동일 ID로 존재한다.
- 정상·Validation·권한·중복·동시성·Timeout·응답 유실·부분 실패·복구 Test가 통과한다.
- 요청·응답·상태·DB·이벤트·파일·운영 화면이 양방향으로 일치한다.
- 운영자가 다른 사람의 설명 없이 장애를 재현하고 정상화 판정을 할 수 있다.
- 실행하지 않은 Test는 성공으로 기록하지 않고, 실패·제약이 남으면 전체 완료로 보고하지 않는다.

## 금지 사항

- 메모리 Map·고정 JSON·정상 Case 한 건만으로 완료 처리
- 교육용 고객 업무를 `cpf-core`, `cpf-starters/common`, Starter, Runtime 제품 내부에 삽입
- Module-local DB Vendor Fallback SQL 또는 Vendor별 의미가 다른 Schema
- Timeout이나 응답 유실 뒤 실제 결과 확인 없이 무조건 재호출
- 비밀번호·Token·개인정보 원문을 Source·Log·Fixture·문서에 기록
- 매뉴얼 설명과 다른 임의 Package·API·상태·메뉴를 새로 생성

---

# 119. EDU-GW-08 — SSRF Allowlist·DNS Rebinding·내부망 차단

- 우선순위: **P1**
- 연결 매뉴얼: `91_Gateway매뉴얼.md`
- Owner: `cpf-gateway + cpf-education target service`
- 구현 상태 목표: Source·SQL·API·Config·Test·운영 확인·매뉴얼 연결이 모두 완료

## 필요 근거

- 고객 매뉴얼에서 `SSRF Allowlist·DNS Rebinding·내부망 차단`을 설명하려면 정상 호출 예시만이 아니라 상태 변화와 장애 후 정상화까지 재현 가능한 실행 예제가 필요하다.
- 이 요구사항이 없으면 고객은 기능 가능 여부를 알더라도 실제 업무 적용 순서, 실패 판정, 재처리 금지 조건을 확인할 수 없다.
- 내부 구현 구조를 보여 주기 위한 예제가 아니라 고객 업무를 CPF로 만드는 방법과 운영 결과를 증명하는 예제로 구현한다.

## 고객 업무 시나리오

관리자가 Target URL을 등록할 때 허용 도메인·IP 범위를 검증하고 런타임 DNS 변경도 차단한다.

## 구현 위치와 책임 경계

- 업무 Source: `com.cpf.education.edu.gateway08` 또는 같은 의미의 기존 표준 Slice
- Test: Owner Module의 기존 Test Root 아래 `EDU-GW-08` 식별 가능한 Class·Fixture
- SQL: 중앙 DB Vendor Pack의 기존 Install·Upgrade·Rollback·Verify·Seed 구조
- Config: 기존 Prefix·Profile·Secret 참조 방식을 유지
- 고객 업무 상태 변경과 원장 데이터는 고객 업무 Owner가 소유한다. ADM·BZA·Gateway·운영 Script가 업무 Table을 직접 수정하지 않는다.
- 공통 기능이 필요하면 기존 Public API·SPI를 우선 사용하고, 진입점이 없을 때만 실제 Consumer를 근거로 Owner Module에 확장한다.

## 입력·계약

- `targetUrl`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `allowlistVersion`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `tlsPolicy`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- 해당 기능에 필요한 Operation ID·업무 ID·Correlation ID·요청 Hash·사용자·권한 문맥을 저장·Log·Trace·Audit에서 연결한다.
- 요청·응답·Job Parameter·이벤트·파일 Layout 중 해당되는 계약을 OpenAPI 또는 Machine-readable Schema로 제공한다.

## 상태와 저장 근거

- 필수 상태 흐름: `DRAFT → SECURITY_VALIDATED → APPROVED/REJECTED`
- 상태마다 진입 조건, 허용 조치, 재시도 가능 여부, 최종 여부, 저장 위치와 Version 규칙을 정의한다.
- 결과 미확정·부분 성공이 가능한 경우 대상별 Attempt와 상대 상태 조회 근거를 별도 저장한다.

## 정상 처리 순서

1. 입력·권한·데이터 범위·기준 Version과 선행 상태를 검증한다.
2. 상태 변경·비동기 실행인 경우 Operation과 업무 식별자를 생성하거나 기존 멱등 요청을 조회한다. 조회 전용 기능은 조회 Context와 Cursor만 사용한다.
3. 상태 변경 기능은 고객 업무 Owner의 Application Use Case에서 Transaction 경계와 상태 전이를 수행하고, 조회 전용 기능은 일관된 Snapshot·정렬·범위 조건을 적용한다.
4. 외부 시스템·Kafka·파일·Batch·Gateway가 필요한 경우 Adapter 경계에서 Timeout·Retry·보안 정책을 적용한다.
5. 업무 결과와 Attempt·Audit·Log·Metric·Trace를 같은 식별자로 연결한다.
6. 고객 API 또는 운영 화면에서 최종 상태·건수·금액·Version·오류 0건을 확인한다.

## 예외·장애·경계 사례

- localhost
- metadata IP
- CNAME chain
- DNS rebinding
- IPv6
- redirect
- 상태 변경이 있는 경우 DB Commit 전 실패와 Commit 후 응답 유실을 구분한다.
- 같은 요청의 중복·동시 실행·순서 역전·늦은 응답을 포함한다.
- Retry 가능한 오류와 즉시 중단해야 하는 업무 오류를 구분한다.
- 대량·다중 대상 기능에서 부분 성공이 발생하면 성공 대상을 다시 실행하지 않고 실패 대상만 Reprocess·Reconcile한다.
- 복구·보상이 필요한 기능은 복구 실패와 보상 실패도 별도 상태·Attempt·Audit로 남긴다.

## 필수 Test

- Parser bypass
- resolution pinning policy
- redirect limit
- 입력 Validation·권한·Data Scope·Masking Negative Test
- 해당 기능에 적용되는 중복·동시성·Timeout·응답 유실·부분 실패 Test
- 해당 기능의 Failure Injection 후 필요한 Retry·Restart·Reprocess·Reconcile·Rollback Test
- Oracle·PostgreSQL·MariaDB 또는 해당 Infra의 의미 동일성 Test
- Log·Metric·Trace·Audit에 Secret·개인정보 원문이 없는지 검사

## 운영·화면 확인

- Gateway Security·Routes 검증 E2E
- 화면에는 해당 메뉴에 실제로 존재하는 조회 조건, 기본값, Column, 상세 Field, 상태, Button 활성 조건, 사유, 승인, 예상 Version, 대상별 결과만 Source와 일치하게 표시한다. 조회 전용 메뉴에 Command 항목을 만들지 않는다.
- 운영자가 정상화 완료를 판단할 수 있는 건수·금액·Version·Checksum·최종 상태·마지막 오류를 제공한다.
- 변경·외부연계 기능에서 응답 유실이 가능한 경우에만 요청을 다시 보내기 전에 Operation·거래·상대 상태를 조회하는 절차를 Browser 또는 API Test로 검증한다.

## 고객 매뉴얼·EDU 연결

- `91_Gateway매뉴얼.md`의 해당 기능 장에 `EDU-GW-08`를 연결한다.
- 선행 환경, 실행 명령, 입력값, 정상 출력, 오류 재현 명령, 복구 명령, ADM/BZA/Gateway 확인 위치를 그대로 따라 할 수 있게 작성한다.
- 교육 코드에서 고객 업무로 바꿀 Package·Table·API·Topic·파일명과 CPF 관리 영역을 구분한 전환표를 제공한다.

## 완료 조건

- `EDU-GW-08`가 교육 Catalog·Source·Test·고객 매뉴얼에 동일 ID로 존재한다.
- 정상·Validation·권한·중복·동시성·Timeout·응답 유실·부분 실패·복구 Test가 통과한다.
- 요청·응답·상태·DB·이벤트·파일·운영 화면이 양방향으로 일치한다.
- 운영자가 다른 사람의 설명 없이 장애를 재현하고 정상화 판정을 할 수 있다.
- 실행하지 않은 Test는 성공으로 기록하지 않고, 실패·제약이 남으면 전체 완료로 보고하지 않는다.

## 금지 사항

- 메모리 Map·고정 JSON·정상 Case 한 건만으로 완료 처리
- 교육용 고객 업무를 `cpf-core`, `cpf-starters/common`, Starter, Runtime 제품 내부에 삽입
- Module-local DB Vendor Fallback SQL 또는 Vendor별 의미가 다른 Schema
- Timeout이나 응답 유실 뒤 실제 결과 확인 없이 무조건 재호출
- 비밀번호·Token·개인정보 원문을 Source·Log·Fixture·문서에 기록
- 매뉴얼 설명과 다른 임의 Package·API·상태·메뉴를 새로 생성

---

# 120. EDU-GW-09 — Header 정리·경로·요청·응답 변환

- 우선순위: **P1**
- 연결 매뉴얼: `91_Gateway매뉴얼.md`
- Owner: `cpf-gateway + cpf-education target service`
- 구현 상태 목표: Source·SQL·API·Config·Test·운영 확인·매뉴얼 연결이 모두 완료

## 필요 근거

- 고객 매뉴얼에서 `Header 정리·경로·요청·응답 변환`을 설명하려면 정상 호출 예시만이 아니라 상태 변화와 장애 후 정상화까지 재현 가능한 실행 예제가 필요하다.
- 이 요구사항이 없으면 고객은 기능 가능 여부를 알더라도 실제 업무 적용 순서, 실패 판정, 재처리 금지 조건을 확인할 수 없다.
- 내부 구현 구조를 보여 주기 위한 예제가 아니라 고객 업무를 CPF로 만드는 방법과 운영 결과를 증명하는 예제로 구현한다.

## 고객 업무 시나리오

외부 Header를 제거하고 고객 API 형식에 맞게 경로와 일부 Header를 변환한다.

## 구현 위치와 책임 경계

- 업무 Source: `com.cpf.education.edu.gateway09` 또는 같은 의미의 기존 표준 Slice
- Test: Owner Module의 기존 Test Root 아래 `EDU-GW-09` 식별 가능한 Class·Fixture
- SQL: 중앙 DB Vendor Pack의 기존 Install·Upgrade·Rollback·Verify·Seed 구조
- Config: 기존 Prefix·Profile·Secret 참조 방식을 유지
- 고객 업무 상태 변경과 원장 데이터는 고객 업무 Owner가 소유한다. ADM·BZA·Gateway·운영 Script가 업무 Table을 직접 수정하지 않는다.
- 공통 기능이 필요하면 기존 Public API·SPI를 우선 사용하고, 진입점이 없을 때만 실제 Consumer를 근거로 Owner Module에 확장한다.

## 입력·계약

- `routeId`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `rewriteRule`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `headerPolicy`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- 해당 기능에 필요한 Operation ID·업무 ID·Correlation ID·요청 Hash·사용자·권한 문맥을 저장·Log·Trace·Audit에서 연결한다.
- 요청·응답·Job Parameter·이벤트·파일 Layout 중 해당되는 계약을 OpenAPI 또는 Machine-readable Schema로 제공한다.

## 상태와 저장 근거

- 필수 상태 흐름: `VALIDATED → PUBLISHED → TRANSFORMING`
- 상태마다 진입 조건, 허용 조치, 재시도 가능 여부, 최종 여부, 저장 위치와 Version 규칙을 정의한다.
- 결과 미확정·부분 성공이 가능한 경우 대상별 Attempt와 상대 상태 조회 근거를 별도 저장한다.

## 정상 처리 순서

1. 입력·권한·데이터 범위·기준 Version과 선행 상태를 검증한다.
2. 상태 변경·비동기 실행인 경우 Operation과 업무 식별자를 생성하거나 기존 멱등 요청을 조회한다. 조회 전용 기능은 조회 Context와 Cursor만 사용한다.
3. 상태 변경 기능은 고객 업무 Owner의 Application Use Case에서 Transaction 경계와 상태 전이를 수행하고, 조회 전용 기능은 일관된 Snapshot·정렬·범위 조건을 적용한다.
4. 외부 시스템·Kafka·파일·Batch·Gateway가 필요한 경우 Adapter 경계에서 Timeout·Retry·보안 정책을 적용한다.
5. 업무 결과와 Attempt·Audit·Log·Metric·Trace를 같은 식별자로 연결한다.
6. 고객 API 또는 운영 화면에서 최종 상태·건수·금액·Version·오류 0건을 확인한다.

## 예외·장애·경계 사례

- 중복 Header
- hop-by-hop
- CRLF injection
- rewrite loop
- Location rewrite
- 상태 변경이 있는 경우 DB Commit 전 실패와 Commit 후 응답 유실을 구분한다.
- 같은 요청의 중복·동시 실행·순서 역전·늦은 응답을 포함한다.
- Retry 가능한 오류와 즉시 중단해야 하는 업무 오류를 구분한다.
- 대량·다중 대상 기능에서 부분 성공이 발생하면 성공 대상을 다시 실행하지 않고 실패 대상만 Reprocess·Reconcile한다.
- 복구·보상이 필요한 기능은 복구 실패와 보상 실패도 별도 상태·Attempt·Audit로 남긴다.

## 필수 Test

- Golden request/response
- sanitization
- trace header preservation
- 입력 Validation·권한·Data Scope·Masking Negative Test
- 해당 기능에 적용되는 중복·동시성·Timeout·응답 유실·부분 실패 Test
- 해당 기능의 Failure Injection 후 필요한 Retry·Restart·Reprocess·Reconcile·Rollback Test
- Oracle·PostgreSQL·MariaDB 또는 해당 Infra의 의미 동일성 Test
- Log·Metric·Trace·Audit에 Secret·개인정보 원문이 없는지 검사

## 운영·화면 확인

- Gateway Routes·Transactions 로그 Masking 확인
- 화면에는 해당 메뉴에 실제로 존재하는 조회 조건, 기본값, Column, 상세 Field, 상태, Button 활성 조건, 사유, 승인, 예상 Version, 대상별 결과만 Source와 일치하게 표시한다. 조회 전용 메뉴에 Command 항목을 만들지 않는다.
- 운영자가 정상화 완료를 판단할 수 있는 건수·금액·Version·Checksum·최종 상태·마지막 오류를 제공한다.
- 변경·외부연계 기능에서 응답 유실이 가능한 경우에만 요청을 다시 보내기 전에 Operation·거래·상대 상태를 조회하는 절차를 Browser 또는 API Test로 검증한다.

## 고객 매뉴얼·EDU 연결

- `91_Gateway매뉴얼.md`의 해당 기능 장에 `EDU-GW-09`를 연결한다.
- 선행 환경, 실행 명령, 입력값, 정상 출력, 오류 재현 명령, 복구 명령, ADM/BZA/Gateway 확인 위치를 그대로 따라 할 수 있게 작성한다.
- 교육 코드에서 고객 업무로 바꿀 Package·Table·API·Topic·파일명과 CPF 관리 영역을 구분한 전환표를 제공한다.

## 완료 조건

- `EDU-GW-09`가 교육 Catalog·Source·Test·고객 매뉴얼에 동일 ID로 존재한다.
- 정상·Validation·권한·중복·동시성·Timeout·응답 유실·부분 실패·복구 Test가 통과한다.
- 요청·응답·상태·DB·이벤트·파일·운영 화면이 양방향으로 일치한다.
- 운영자가 다른 사람의 설명 없이 장애를 재현하고 정상화 판정을 할 수 있다.
- 실행하지 않은 Test는 성공으로 기록하지 않고, 실패·제약이 남으면 전체 완료로 보고하지 않는다.

## 금지 사항

- 메모리 Map·고정 JSON·정상 Case 한 건만으로 완료 처리
- 교육용 고객 업무를 `cpf-core`, `cpf-starters/common`, Starter, Runtime 제품 내부에 삽입
- Module-local DB Vendor Fallback SQL 또는 Vendor별 의미가 다른 Schema
- Timeout이나 응답 유실 뒤 실제 결과 확인 없이 무조건 재호출
- 비밀번호·Token·개인정보 원문을 Source·Log·Fixture·문서에 기록
- 매뉴얼 설명과 다른 임의 Package·API·상태·메뉴를 새로 생성

---

# 121. EDU-GW-10 — Body 크기·Content-Type·Schema Validation

- 우선순위: **P1**
- 연결 매뉴얼: `91_Gateway매뉴얼.md`
- Owner: `cpf-gateway + cpf-education target service`
- 구현 상태 목표: Source·SQL·API·Config·Test·운영 확인·매뉴얼 연결이 모두 완료

## 필요 근거

- 고객 매뉴얼에서 `Body 크기·Content-Type·Schema Validation`을 설명하려면 정상 호출 예시만이 아니라 상태 변화와 장애 후 정상화까지 재현 가능한 실행 예제가 필요하다.
- 이 요구사항이 없으면 고객은 기능 가능 여부를 알더라도 실제 업무 적용 순서, 실패 판정, 재처리 금지 조건을 확인할 수 없다.
- 내부 구현 구조를 보여 주기 위한 예제가 아니라 고객 업무를 CPF로 만드는 방법과 운영 결과를 증명하는 예제로 구현한다.

## 고객 업무 시나리오

대용량 또는 허용되지 않은 Content-Type을 Backend 전송 전에 차단한다.

## 구현 위치와 책임 경계

- 업무 Source: `com.cpf.education.edu.gateway10` 또는 같은 의미의 기존 표준 Slice
- Test: Owner Module의 기존 Test Root 아래 `EDU-GW-10` 식별 가능한 Class·Fixture
- SQL: 중앙 DB Vendor Pack의 기존 Install·Upgrade·Rollback·Verify·Seed 구조
- Config: 기존 Prefix·Profile·Secret 참조 방식을 유지
- 고객 업무 상태 변경과 원장 데이터는 고객 업무 Owner가 소유한다. ADM·BZA·Gateway·운영 Script가 업무 Table을 직접 수정하지 않는다.
- 공통 기능이 필요하면 기존 Public API·SPI를 우선 사용하고, 진입점이 없을 때만 실제 Consumer를 근거로 Owner Module에 확장한다.

## 입력·계약

- `maxBodySize`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `allowedTypes`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `schemaVersion`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- 해당 기능에 필요한 Operation ID·업무 ID·Correlation ID·요청 Hash·사용자·권한 문맥을 저장·Log·Trace·Audit에서 연결한다.
- 요청·응답·Job Parameter·이벤트·파일 Layout 중 해당되는 계약을 OpenAPI 또는 Machine-readable Schema로 제공한다.

## 상태와 저장 근거

- 필수 상태 흐름: `ACCEPTED/REJECTED_BEFORE_FORWARD`
- 상태마다 진입 조건, 허용 조치, 재시도 가능 여부, 최종 여부, 저장 위치와 Version 규칙을 정의한다.
- 결과 미확정·부분 성공이 가능한 경우 대상별 Attempt와 상대 상태 조회 근거를 별도 저장한다.

## 정상 처리 순서

1. 입력·권한·데이터 범위·기준 Version과 선행 상태를 검증한다.
2. 상태 변경·비동기 실행인 경우 Operation과 업무 식별자를 생성하거나 기존 멱등 요청을 조회한다. 조회 전용 기능은 조회 Context와 Cursor만 사용한다.
3. 상태 변경 기능은 고객 업무 Owner의 Application Use Case에서 Transaction 경계와 상태 전이를 수행하고, 조회 전용 기능은 일관된 Snapshot·정렬·범위 조건을 적용한다.
4. 외부 시스템·Kafka·파일·Batch·Gateway가 필요한 경우 Adapter 경계에서 Timeout·Retry·보안 정책을 적용한다.
5. 업무 결과와 Attempt·Audit·Log·Metric·Trace를 같은 식별자로 연결한다.
6. 고객 API 또는 운영 화면에서 최종 상태·건수·금액·Version·오류 0건을 확인한다.

## 예외·장애·경계 사례

- Chunked body
- gzip bomb
- 잘못된 charset
- multipart part limit
- 상태 변경이 있는 경우 DB Commit 전 실패와 Commit 후 응답 유실을 구분한다.
- 같은 요청의 중복·동시 실행·순서 역전·늦은 응답을 포함한다.
- Retry 가능한 오류와 즉시 중단해야 하는 업무 오류를 구분한다.
- 대량·다중 대상 기능에서 부분 성공이 발생하면 성공 대상을 다시 실행하지 않고 실패 대상만 Reprocess·Reconcile한다.
- 복구·보상이 필요한 기능은 복구 실패와 보상 실패도 별도 상태·Attempt·Audit로 남긴다.

## 필수 Test

- Streaming limit
- decompression ratio
- error contract
- metrics
- 입력 Validation·권한·Data Scope·Masking Negative Test
- 해당 기능에 적용되는 중복·동시성·Timeout·응답 유실·부분 실패 Test
- 해당 기능의 Failure Injection 후 필요한 Retry·Restart·Reprocess·Reconcile·Rollback Test
- Oracle·PostgreSQL·MariaDB 또는 해당 Infra의 의미 동일성 Test
- Log·Metric·Trace·Audit에 Secret·개인정보 원문이 없는지 검사

## 운영·화면 확인

- Gateway Security·Transactions E2E
- 화면에는 해당 메뉴에 실제로 존재하는 조회 조건, 기본값, Column, 상세 Field, 상태, Button 활성 조건, 사유, 승인, 예상 Version, 대상별 결과만 Source와 일치하게 표시한다. 조회 전용 메뉴에 Command 항목을 만들지 않는다.
- 운영자가 정상화 완료를 판단할 수 있는 건수·금액·Version·Checksum·최종 상태·마지막 오류를 제공한다.
- 변경·외부연계 기능에서 응답 유실이 가능한 경우에만 요청을 다시 보내기 전에 Operation·거래·상대 상태를 조회하는 절차를 Browser 또는 API Test로 검증한다.

## 고객 매뉴얼·EDU 연결

- `91_Gateway매뉴얼.md`의 해당 기능 장에 `EDU-GW-10`를 연결한다.
- 선행 환경, 실행 명령, 입력값, 정상 출력, 오류 재현 명령, 복구 명령, ADM/BZA/Gateway 확인 위치를 그대로 따라 할 수 있게 작성한다.
- 교육 코드에서 고객 업무로 바꿀 Package·Table·API·Topic·파일명과 CPF 관리 영역을 구분한 전환표를 제공한다.

## 완료 조건

- `EDU-GW-10`가 교육 Catalog·Source·Test·고객 매뉴얼에 동일 ID로 존재한다.
- 정상·Validation·권한·중복·동시성·Timeout·응답 유실·부분 실패·복구 Test가 통과한다.
- 요청·응답·상태·DB·이벤트·파일·운영 화면이 양방향으로 일치한다.
- 운영자가 다른 사람의 설명 없이 장애를 재현하고 정상화 판정을 할 수 있다.
- 실행하지 않은 Test는 성공으로 기록하지 않고, 실패·제약이 남으면 전체 완료로 보고하지 않는다.

## 금지 사항

- 메모리 Map·고정 JSON·정상 Case 한 건만으로 완료 처리
- 교육용 고객 업무를 `cpf-core`, `cpf-starters/common`, Starter, Runtime 제품 내부에 삽입
- Module-local DB Vendor Fallback SQL 또는 Vendor별 의미가 다른 Schema
- Timeout이나 응답 유실 뒤 실제 결과 확인 없이 무조건 재호출
- 비밀번호·Token·개인정보 원문을 Source·Log·Fixture·문서에 기록
- 매뉴얼 설명과 다른 임의 Package·API·상태·메뉴를 새로 생성

---

# 122. EDU-GW-11 — Command 멱등성·Attempt Ledger·응답 유실

- 우선순위: **P1**
- 연결 매뉴얼: `91_Gateway매뉴얼.md`
- Owner: `cpf-gateway + cpf-education target service`
- 구현 상태 목표: Source·SQL·API·Config·Test·운영 확인·매뉴얼 연결이 모두 완료

## 필요 근거

- 고객 매뉴얼에서 `Command 멱등성·Attempt Ledger·응답 유실`을 설명하려면 정상 호출 예시만이 아니라 상태 변화와 장애 후 정상화까지 재현 가능한 실행 예제가 필요하다.
- 이 요구사항이 없으면 고객은 기능 가능 여부를 알더라도 실제 업무 적용 순서, 실패 판정, 재처리 금지 조건을 확인할 수 없다.
- 내부 구현 구조를 보여 주기 위한 예제가 아니라 고객 업무를 CPF로 만드는 방법과 운영 결과를 증명하는 예제로 구현한다.

## 고객 업무 시나리오

Gateway가 지급 Command를 전달할 때 Idempotency Key와 Attempt를 기록하고 ACK 유실 시 상태를 조회한다.

## 구현 위치와 책임 경계

- 업무 Source: `com.cpf.education.edu.gateway11` 또는 같은 의미의 기존 표준 Slice
- Test: Owner Module의 기존 Test Root 아래 `EDU-GW-11` 식별 가능한 Class·Fixture
- SQL: 중앙 DB Vendor Pack의 기존 Install·Upgrade·Rollback·Verify·Seed 구조
- Config: 기존 Prefix·Profile·Secret 참조 방식을 유지
- 고객 업무 상태 변경과 원장 데이터는 고객 업무 Owner가 소유한다. ADM·BZA·Gateway·운영 Script가 업무 Table을 직접 수정하지 않는다.
- 공통 기능이 필요하면 기존 Public API·SPI를 우선 사용하고, 진입점이 없을 때만 실제 Consumer를 근거로 Owner Module에 확장한다.

## 입력·계약

- `idempotencyKey`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `requestHash`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `routeVersion`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- 해당 기능에 필요한 Operation ID·업무 ID·Correlation ID·요청 Hash·사용자·권한 문맥을 저장·Log·Trace·Audit에서 연결한다.
- 요청·응답·Job Parameter·이벤트·파일 Layout 중 해당되는 계약을 OpenAPI 또는 Machine-readable Schema로 제공한다.

## 상태와 저장 근거

- 필수 상태 흐름: `ATTEMPTED → ACKED/UNKNOWN_RESULT → RECONCILED`
- 상태마다 진입 조건, 허용 조치, 재시도 가능 여부, 최종 여부, 저장 위치와 Version 규칙을 정의한다.
- 결과 미확정·부분 성공이 가능한 경우 대상별 Attempt와 상대 상태 조회 근거를 별도 저장한다.

## 정상 처리 순서

1. 입력·권한·데이터 범위·기준 Version과 선행 상태를 검증한다.
2. 상태 변경·비동기 실행인 경우 Operation과 업무 식별자를 생성하거나 기존 멱등 요청을 조회한다. 조회 전용 기능은 조회 Context와 Cursor만 사용한다.
3. 상태 변경 기능은 고객 업무 Owner의 Application Use Case에서 Transaction 경계와 상태 전이를 수행하고, 조회 전용 기능은 일관된 Snapshot·정렬·범위 조건을 적용한다.
4. 외부 시스템·Kafka·파일·Batch·Gateway가 필요한 경우 Adapter 경계에서 Timeout·Retry·보안 정책을 적용한다.
5. 업무 결과와 Attempt·Audit·Log·Metric·Trace를 같은 식별자로 연결한다.
6. 고객 API 또는 운영 화면에서 최종 상태·건수·금액·Version·오류 0건을 확인한다.

## 예외·장애·경계 사례

- 같은 Key 다른 Body
- Backend Commit 후 timeout
- Retry policy 충돌
- 상태 변경이 있는 경우 DB Commit 전 실패와 Commit 후 응답 유실을 구분한다.
- 같은 요청의 중복·동시 실행·순서 역전·늦은 응답을 포함한다.
- Retry 가능한 오류와 즉시 중단해야 하는 업무 오류를 구분한다.
- 대량·다중 대상 기능에서 부분 성공이 발생하면 성공 대상을 다시 실행하지 않고 실패 대상만 Reprocess·Reconcile한다.
- 복구·보상이 필요한 기능은 복구 실패와 보상 실패도 별도 상태·Attempt·Audit로 남긴다.

## 필수 Test

- Ledger persistence
- no blind retry
- reconcile endpoint
- 입력 Validation·권한·Data Scope·Masking Negative Test
- 해당 기능에 적용되는 중복·동시성·Timeout·응답 유실·부분 실패 Test
- 해당 기능의 Failure Injection 후 필요한 Retry·Restart·Reprocess·Reconcile·Rollback Test
- Oracle·PostgreSQL·MariaDB 또는 해당 Infra의 의미 동일성 Test
- Log·Metric·Trace·Audit에 Secret·개인정보 원문이 없는지 검사

## 운영·화면 확인

- Gateway Transactions·ADM Recovery E2E
- 화면에는 해당 메뉴에 실제로 존재하는 조회 조건, 기본값, Column, 상세 Field, 상태, Button 활성 조건, 사유, 승인, 예상 Version, 대상별 결과만 Source와 일치하게 표시한다. 조회 전용 메뉴에 Command 항목을 만들지 않는다.
- 운영자가 정상화 완료를 판단할 수 있는 건수·금액·Version·Checksum·최종 상태·마지막 오류를 제공한다.
- 변경·외부연계 기능에서 응답 유실이 가능한 경우에만 요청을 다시 보내기 전에 Operation·거래·상대 상태를 조회하는 절차를 Browser 또는 API Test로 검증한다.

## 고객 매뉴얼·EDU 연결

- `91_Gateway매뉴얼.md`의 해당 기능 장에 `EDU-GW-11`를 연결한다.
- 선행 환경, 실행 명령, 입력값, 정상 출력, 오류 재현 명령, 복구 명령, ADM/BZA/Gateway 확인 위치를 그대로 따라 할 수 있게 작성한다.
- 교육 코드에서 고객 업무로 바꿀 Package·Table·API·Topic·파일명과 CPF 관리 영역을 구분한 전환표를 제공한다.

## 완료 조건

- `EDU-GW-11`가 교육 Catalog·Source·Test·고객 매뉴얼에 동일 ID로 존재한다.
- 정상·Validation·권한·중복·동시성·Timeout·응답 유실·부분 실패·복구 Test가 통과한다.
- 요청·응답·상태·DB·이벤트·파일·운영 화면이 양방향으로 일치한다.
- 운영자가 다른 사람의 설명 없이 장애를 재현하고 정상화 판정을 할 수 있다.
- 실행하지 않은 Test는 성공으로 기록하지 않고, 실패·제약이 남으면 전체 완료로 보고하지 않는다.

## 금지 사항

- 메모리 Map·고정 JSON·정상 Case 한 건만으로 완료 처리
- 교육용 고객 업무를 `cpf-core`, `cpf-starters/common`, Starter, Runtime 제품 내부에 삽입
- Module-local DB Vendor Fallback SQL 또는 Vendor별 의미가 다른 Schema
- Timeout이나 응답 유실 뒤 실제 결과 확인 없이 무조건 재호출
- 비밀번호·Token·개인정보 원문을 Source·Log·Fixture·문서에 기록
- 매뉴얼 설명과 다른 임의 Package·API·상태·메뉴를 새로 생성

---

# 123. EDU-GW-12 — 다중 인스턴스 설정 Drift·Reconcile

- 우선순위: **P1**
- 연결 매뉴얼: `91_Gateway매뉴얼.md`
- Owner: `cpf-gateway + cpf-education target service`
- 구현 상태 목표: Source·SQL·API·Config·Test·운영 확인·매뉴얼 연결이 모두 완료

## 필요 근거

- 고객 매뉴얼에서 `다중 인스턴스 설정 Drift·Reconcile`을 설명하려면 정상 호출 예시만이 아니라 상태 변화와 장애 후 정상화까지 재현 가능한 실행 예제가 필요하다.
- 이 요구사항이 없으면 고객은 기능 가능 여부를 알더라도 실제 업무 적용 순서, 실패 판정, 재처리 금지 조건을 확인할 수 없다.
- 내부 구현 구조를 보여 주기 위한 예제가 아니라 고객 업무를 CPF로 만드는 방법과 운영 결과를 증명하는 예제로 구현한다.

## 고객 업무 시나리오

게시 Version이 일부 Gateway에만 적용되면 Drift를 탐지하고 재적용 또는 LKG 복구한다.

## 구현 위치와 책임 경계

- 업무 Source: `com.cpf.education.edu.gateway12` 또는 같은 의미의 기존 표준 Slice
- Test: Owner Module의 기존 Test Root 아래 `EDU-GW-12` 식별 가능한 Class·Fixture
- SQL: 중앙 DB Vendor Pack의 기존 Install·Upgrade·Rollback·Verify·Seed 구조
- Config: 기존 Prefix·Profile·Secret 참조 방식을 유지
- 고객 업무 상태 변경과 원장 데이터는 고객 업무 Owner가 소유한다. ADM·BZA·Gateway·운영 Script가 업무 Table을 직접 수정하지 않는다.
- 공통 기능이 필요하면 기존 Public API·SPI를 우선 사용하고, 진입점이 없을 때만 실제 Consumer를 근거로 Owner Module에 확장한다.

## 입력·계약

- `packageVersion`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `checksum`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `instanceIds`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- 해당 기능에 필요한 Operation ID·업무 ID·Correlation ID·요청 Hash·사용자·권한 문맥을 저장·Log·Trace·Audit에서 연결한다.
- 요청·응답·Job Parameter·이벤트·파일 Layout 중 해당되는 계약을 OpenAPI 또는 Machine-readable Schema로 제공한다.

## 상태와 저장 근거

- 필수 상태 흐름: `PUBLISHING → APPLIED/PARTIAL → RECONCILING → CONSISTENT`
- 상태마다 진입 조건, 허용 조치, 재시도 가능 여부, 최종 여부, 저장 위치와 Version 규칙을 정의한다.
- 결과 미확정·부분 성공이 가능한 경우 대상별 Attempt와 상대 상태 조회 근거를 별도 저장한다.

## 정상 처리 순서

1. 입력·권한·데이터 범위·기준 Version과 선행 상태를 검증한다.
2. 상태 변경·비동기 실행인 경우 Operation과 업무 식별자를 생성하거나 기존 멱등 요청을 조회한다. 조회 전용 기능은 조회 Context와 Cursor만 사용한다.
3. 상태 변경 기능은 고객 업무 Owner의 Application Use Case에서 Transaction 경계와 상태 전이를 수행하고, 조회 전용 기능은 일관된 Snapshot·정렬·범위 조건을 적용한다.
4. 외부 시스템·Kafka·파일·Batch·Gateway가 필요한 경우 Adapter 경계에서 Timeout·Retry·보안 정책을 적용한다.
5. 업무 결과와 Attempt·Audit·Log·Metric·Trace를 같은 식별자로 연결한다.
6. 고객 API 또는 운영 화면에서 최종 상태·건수·금액·Version·오류 0건을 확인한다.

## 예외·장애·경계 사례

- Instance offline
- ACK 유실
- checksum mismatch
- 재기동 후 구버전
- 상태 변경이 있는 경우 DB Commit 전 실패와 Commit 후 응답 유실을 구분한다.
- 같은 요청의 중복·동시 실행·순서 역전·늦은 응답을 포함한다.
- Retry 가능한 오류와 즉시 중단해야 하는 업무 오류를 구분한다.
- 대량·다중 대상 기능에서 부분 성공이 발생하면 성공 대상을 다시 실행하지 않고 실패 대상만 Reprocess·Reconcile한다.
- 복구·보상이 필요한 기능은 복구 실패와 보상 실패도 별도 상태·Attempt·Audit로 남긴다.

## 필수 Test

- Version/checksum probe
- targeted reapply
- audit
- 입력 Validation·권한·Data Scope·Masking Negative Test
- 해당 기능에 적용되는 중복·동시성·Timeout·응답 유실·부분 실패 Test
- 해당 기능의 Failure Injection 후 필요한 Retry·Restart·Reprocess·Reconcile·Rollback Test
- Oracle·PostgreSQL·MariaDB 또는 해당 Infra의 의미 동일성 Test
- Log·Metric·Trace·Audit에 Secret·개인정보 원문이 없는지 검사

## 운영·화면 확인

- Apply Status·Health·Dashboard E2E
- 화면에는 해당 메뉴에 실제로 존재하는 조회 조건, 기본값, Column, 상세 Field, 상태, Button 활성 조건, 사유, 승인, 예상 Version, 대상별 결과만 Source와 일치하게 표시한다. 조회 전용 메뉴에 Command 항목을 만들지 않는다.
- 운영자가 정상화 완료를 판단할 수 있는 건수·금액·Version·Checksum·최종 상태·마지막 오류를 제공한다.
- 변경·외부연계 기능에서 응답 유실이 가능한 경우에만 요청을 다시 보내기 전에 Operation·거래·상대 상태를 조회하는 절차를 Browser 또는 API Test로 검증한다.

## 고객 매뉴얼·EDU 연결

- `91_Gateway매뉴얼.md`의 해당 기능 장에 `EDU-GW-12`를 연결한다.
- 선행 환경, 실행 명령, 입력값, 정상 출력, 오류 재현 명령, 복구 명령, ADM/BZA/Gateway 확인 위치를 그대로 따라 할 수 있게 작성한다.
- 교육 코드에서 고객 업무로 바꿀 Package·Table·API·Topic·파일명과 CPF 관리 영역을 구분한 전환표를 제공한다.

## 완료 조건

- `EDU-GW-12`가 교육 Catalog·Source·Test·고객 매뉴얼에 동일 ID로 존재한다.
- 정상·Validation·권한·중복·동시성·Timeout·응답 유실·부분 실패·복구 Test가 통과한다.
- 요청·응답·상태·DB·이벤트·파일·운영 화면이 양방향으로 일치한다.
- 운영자가 다른 사람의 설명 없이 장애를 재현하고 정상화 판정을 할 수 있다.
- 실행하지 않은 Test는 성공으로 기록하지 않고, 실패·제약이 남으면 전체 완료로 보고하지 않는다.

## 금지 사항

- 메모리 Map·고정 JSON·정상 Case 한 건만으로 완료 처리
- 교육용 고객 업무를 `cpf-core`, `cpf-starters/common`, Starter, Runtime 제품 내부에 삽입
- Module-local DB Vendor Fallback SQL 또는 Vendor별 의미가 다른 Schema
- Timeout이나 응답 유실 뒤 실제 결과 확인 없이 무조건 재호출
- 비밀번호·Token·개인정보 원문을 Source·Log·Fixture·문서에 기록
- 매뉴얼 설명과 다른 임의 Package·API·상태·메뉴를 새로 생성

---

# 124. EDU-GW-13 — Canary·가중치 Routing·Version Rollback

- 우선순위: **P1**
- 연결 매뉴얼: `91_Gateway매뉴얼.md`
- Owner: `cpf-gateway + cpf-education target service`
- 구현 상태 목표: Source·SQL·API·Config·Test·운영 확인·매뉴얼 연결이 모두 완료

## 필요 근거

- 고객 매뉴얼에서 `Canary·가중치 Routing·Version Rollback`을 설명하려면 정상 호출 예시만이 아니라 상태 변화와 장애 후 정상화까지 재현 가능한 실행 예제가 필요하다.
- 이 요구사항이 없으면 고객은 기능 가능 여부를 알더라도 실제 업무 적용 순서, 실패 판정, 재처리 금지 조건을 확인할 수 없다.
- 내부 구현 구조를 보여 주기 위한 예제가 아니라 고객 업무를 CPF로 만드는 방법과 운영 결과를 증명하는 예제로 구현한다.

## 고객 업무 시나리오

API v2를 5% 트래픽에 배포하고 오류율 임계치 초과 시 v1로 되돌린다.

## 구현 위치와 책임 경계

- 업무 Source: `com.cpf.education.edu.gateway13` 또는 같은 의미의 기존 표준 Slice
- Test: Owner Module의 기존 Test Root 아래 `EDU-GW-13` 식별 가능한 Class·Fixture
- SQL: 중앙 DB Vendor Pack의 기존 Install·Upgrade·Rollback·Verify·Seed 구조
- Config: 기존 Prefix·Profile·Secret 참조 방식을 유지
- 고객 업무 상태 변경과 원장 데이터는 고객 업무 Owner가 소유한다. ADM·BZA·Gateway·운영 Script가 업무 Table을 직접 수정하지 않는다.
- 공통 기능이 필요하면 기존 Public API·SPI를 우선 사용하고, 진입점이 없을 때만 실제 Consumer를 근거로 Owner Module에 확장한다.

## 입력·계약

- `routeVersion`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `weights`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `segment`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `threshold`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- 해당 기능에 필요한 Operation ID·업무 ID·Correlation ID·요청 Hash·사용자·권한 문맥을 저장·Log·Trace·Audit에서 연결한다.
- 요청·응답·Job Parameter·이벤트·파일 Layout 중 해당되는 계약을 OpenAPI 또는 Machine-readable Schema로 제공한다.

## 상태와 저장 근거

- 필수 상태 흐름: `DRAFT → CANARY → PROMOTED/ROLLED_BACK`
- 상태마다 진입 조건, 허용 조치, 재시도 가능 여부, 최종 여부, 저장 위치와 Version 규칙을 정의한다.
- 결과 미확정·부분 성공이 가능한 경우 대상별 Attempt와 상대 상태 조회 근거를 별도 저장한다.

## 정상 처리 순서

1. 입력·권한·데이터 범위·기준 Version과 선행 상태를 검증한다.
2. 상태 변경·비동기 실행인 경우 Operation과 업무 식별자를 생성하거나 기존 멱등 요청을 조회한다. 조회 전용 기능은 조회 Context와 Cursor만 사용한다.
3. 상태 변경 기능은 고객 업무 Owner의 Application Use Case에서 Transaction 경계와 상태 전이를 수행하고, 조회 전용 기능은 일관된 Snapshot·정렬·범위 조건을 적용한다.
4. 외부 시스템·Kafka·파일·Batch·Gateway가 필요한 경우 Adapter 경계에서 Timeout·Retry·보안 정책을 적용한다.
5. 업무 결과와 Attempt·Audit·Log·Metric·Trace를 같은 식별자로 연결한다.
6. 고객 API 또는 운영 화면에서 최종 상태·건수·금액·Version·오류 0건을 확인한다.

## 예외·장애·경계 사례

- 가중치 합계 오류
- sticky inconsistency
- metrics delay
- rollback partial
- 상태 변경이 있는 경우 DB Commit 전 실패와 Commit 후 응답 유실을 구분한다.
- 같은 요청의 중복·동시 실행·순서 역전·늦은 응답을 포함한다.
- Retry 가능한 오류와 즉시 중단해야 하는 업무 오류를 구분한다.
- 대량·다중 대상 기능에서 부분 성공이 발생하면 성공 대상을 다시 실행하지 않고 실패 대상만 Reprocess·Reconcile한다.
- 복구·보상이 필요한 기능은 복구 실패와 보상 실패도 별도 상태·Attempt·Audit로 남긴다.

## 필수 Test

- Deterministic routing
- threshold decision
- LKG
- browser
- 입력 Validation·권한·Data Scope·Masking Negative Test
- 해당 기능에 적용되는 중복·동시성·Timeout·응답 유실·부분 실패 Test
- 해당 기능의 Failure Injection 후 필요한 Retry·Restart·Reprocess·Reconcile·Rollback Test
- Oracle·PostgreSQL·MariaDB 또는 해당 Infra의 의미 동일성 Test
- Log·Metric·Trace·Audit에 Secret·개인정보 원문이 없는지 검사

## 운영·화면 확인

- Routes·Dashboard·Apply Status E2E
- 화면에는 해당 메뉴에 실제로 존재하는 조회 조건, 기본값, Column, 상세 Field, 상태, Button 활성 조건, 사유, 승인, 예상 Version, 대상별 결과만 Source와 일치하게 표시한다. 조회 전용 메뉴에 Command 항목을 만들지 않는다.
- 운영자가 정상화 완료를 판단할 수 있는 건수·금액·Version·Checksum·최종 상태·마지막 오류를 제공한다.
- 변경·외부연계 기능에서 응답 유실이 가능한 경우에만 요청을 다시 보내기 전에 Operation·거래·상대 상태를 조회하는 절차를 Browser 또는 API Test로 검증한다.

## 고객 매뉴얼·EDU 연결

- `91_Gateway매뉴얼.md`의 해당 기능 장에 `EDU-GW-13`를 연결한다.
- 선행 환경, 실행 명령, 입력값, 정상 출력, 오류 재현 명령, 복구 명령, ADM/BZA/Gateway 확인 위치를 그대로 따라 할 수 있게 작성한다.
- 교육 코드에서 고객 업무로 바꿀 Package·Table·API·Topic·파일명과 CPF 관리 영역을 구분한 전환표를 제공한다.

## 완료 조건

- `EDU-GW-13`가 교육 Catalog·Source·Test·고객 매뉴얼에 동일 ID로 존재한다.
- 정상·Validation·권한·중복·동시성·Timeout·응답 유실·부분 실패·복구 Test가 통과한다.
- 요청·응답·상태·DB·이벤트·파일·운영 화면이 양방향으로 일치한다.
- 운영자가 다른 사람의 설명 없이 장애를 재현하고 정상화 판정을 할 수 있다.
- 실행하지 않은 Test는 성공으로 기록하지 않고, 실패·제약이 남으면 전체 완료로 보고하지 않는다.

## 금지 사항

- 메모리 Map·고정 JSON·정상 Case 한 건만으로 완료 처리
- 교육용 고객 업무를 `cpf-core`, `cpf-starters/common`, Starter, Runtime 제품 내부에 삽입
- Module-local DB Vendor Fallback SQL 또는 Vendor별 의미가 다른 Schema
- Timeout이나 응답 유실 뒤 실제 결과 확인 없이 무조건 재호출
- 비밀번호·Token·개인정보 원문을 Source·Log·Fixture·문서에 기록
- 매뉴얼 설명과 다른 임의 Package·API·상태·메뉴를 새로 생성

---

# 125. EDU-GW-14 — Gateway 관측·개인정보 가림·감사

- 우선순위: **P1**
- 연결 매뉴얼: `91_Gateway매뉴얼.md`
- Owner: `cpf-gateway + cpf-education target service`
- 구현 상태 목표: Source·SQL·API·Config·Test·운영 확인·매뉴얼 연결이 모두 완료

## 필요 근거

- 고객 매뉴얼에서 `Gateway 관측·개인정보 가림·감사`을 설명하려면 정상 호출 예시만이 아니라 상태 변화와 장애 후 정상화까지 재현 가능한 실행 예제가 필요하다.
- 이 요구사항이 없으면 고객은 기능 가능 여부를 알더라도 실제 업무 적용 순서, 실패 판정, 재처리 금지 조건을 확인할 수 없다.
- 내부 구현 구조를 보여 주기 위한 예제가 아니라 고객 업무를 CPF로 만드는 방법과 운영 결과를 증명하는 예제로 구현한다.

## 고객 업무 시나리오

경로별 지연·상태코드·차단 사유를 수집하되 Header·Body 개인정보를 기록하지 않는다.

## 구현 위치와 책임 경계

- 업무 Source: `com.cpf.education.edu.gateway14` 또는 같은 의미의 기존 표준 Slice
- Test: Owner Module의 기존 Test Root 아래 `EDU-GW-14` 식별 가능한 Class·Fixture
- SQL: 중앙 DB Vendor Pack의 기존 Install·Upgrade·Rollback·Verify·Seed 구조
- Config: 기존 Prefix·Profile·Secret 참조 방식을 유지
- 고객 업무 상태 변경과 원장 데이터는 고객 업무 Owner가 소유한다. ADM·BZA·Gateway·운영 Script가 업무 Table을 직접 수정하지 않는다.
- 공통 기능이 필요하면 기존 Public API·SPI를 우선 사용하고, 진입점이 없을 때만 실제 Consumer를 근거로 Owner Module에 확장한다.

## 입력·계약

- `logPolicy`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `sampling`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `maskingRules`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- 해당 기능에 필요한 Operation ID·업무 ID·Correlation ID·요청 Hash·사용자·권한 문맥을 저장·Log·Trace·Audit에서 연결한다.
- 요청·응답·Job Parameter·이벤트·파일 Layout 중 해당되는 계약을 OpenAPI 또는 Machine-readable Schema로 제공한다.

## 상태와 저장 근거

- 필수 상태 흐름: `OBSERVED → EXPORTED/PARTIAL`
- 상태마다 진입 조건, 허용 조치, 재시도 가능 여부, 최종 여부, 저장 위치와 Version 규칙을 정의한다.
- 결과 미확정·부분 성공이 가능한 경우 대상별 Attempt와 상대 상태 조회 근거를 별도 저장한다.

## 정상 처리 순서

1. 입력·권한·데이터 범위·기준 Version과 선행 상태를 검증한다.
2. 상태 변경·비동기 실행인 경우 Operation과 업무 식별자를 생성하거나 기존 멱등 요청을 조회한다. 조회 전용 기능은 조회 Context와 Cursor만 사용한다.
3. 상태 변경 기능은 고객 업무 Owner의 Application Use Case에서 Transaction 경계와 상태 전이를 수행하고, 조회 전용 기능은 일관된 Snapshot·정렬·범위 조건을 적용한다.
4. 외부 시스템·Kafka·파일·Batch·Gateway가 필요한 경우 Adapter 경계에서 Timeout·Retry·보안 정책을 적용한다.
5. 업무 결과와 Attempt·Audit·Log·Metric·Trace를 같은 식별자로 연결한다.
6. 고객 API 또는 운영 화면에서 최종 상태·건수·금액·Version·오류 0건을 확인한다.

## 예외·장애·경계 사례

- Masking 누락
- 대용량 body
- trace header 유실
- collector down
- 상태 변경이 있는 경우 DB Commit 전 실패와 Commit 후 응답 유실을 구분한다.
- 같은 요청의 중복·동시 실행·순서 역전·늦은 응답을 포함한다.
- Retry 가능한 오류와 즉시 중단해야 하는 업무 오류를 구분한다.
- 대량·다중 대상 기능에서 부분 성공이 발생하면 성공 대상을 다시 실행하지 않고 실패 대상만 Reprocess·Reconcile한다.
- 복구·보상이 필요한 기능은 복구 실패와 보상 실패도 별도 상태·Attempt·Audit로 남긴다.

## 필수 Test

- Log policy test
- secret scan
- metric labels cardinality
- 입력 Validation·권한·Data Scope·Masking Negative Test
- 해당 기능에 적용되는 중복·동시성·Timeout·응답 유실·부분 실패 Test
- 해당 기능의 Failure Injection 후 필요한 Retry·Restart·Reprocess·Reconcile·Rollback Test
- Oracle·PostgreSQL·MariaDB 또는 해당 Infra의 의미 동일성 Test
- Log·Metric·Trace·Audit에 Secret·개인정보 원문이 없는지 검사

## 운영·화면 확인

- Log Policies·Transactions·Audit E2E
- 화면에는 해당 메뉴에 실제로 존재하는 조회 조건, 기본값, Column, 상세 Field, 상태, Button 활성 조건, 사유, 승인, 예상 Version, 대상별 결과만 Source와 일치하게 표시한다. 조회 전용 메뉴에 Command 항목을 만들지 않는다.
- 운영자가 정상화 완료를 판단할 수 있는 건수·금액·Version·Checksum·최종 상태·마지막 오류를 제공한다.
- 변경·외부연계 기능에서 응답 유실이 가능한 경우에만 요청을 다시 보내기 전에 Operation·거래·상대 상태를 조회하는 절차를 Browser 또는 API Test로 검증한다.

## 고객 매뉴얼·EDU 연결

- `91_Gateway매뉴얼.md`의 해당 기능 장에 `EDU-GW-14`를 연결한다.
- 선행 환경, 실행 명령, 입력값, 정상 출력, 오류 재현 명령, 복구 명령, ADM/BZA/Gateway 확인 위치를 그대로 따라 할 수 있게 작성한다.
- 교육 코드에서 고객 업무로 바꿀 Package·Table·API·Topic·파일명과 CPF 관리 영역을 구분한 전환표를 제공한다.

## 완료 조건

- `EDU-GW-14`가 교육 Catalog·Source·Test·고객 매뉴얼에 동일 ID로 존재한다.
- 정상·Validation·권한·중복·동시성·Timeout·응답 유실·부분 실패·복구 Test가 통과한다.
- 요청·응답·상태·DB·이벤트·파일·운영 화면이 양방향으로 일치한다.
- 운영자가 다른 사람의 설명 없이 장애를 재현하고 정상화 판정을 할 수 있다.
- 실행하지 않은 Test는 성공으로 기록하지 않고, 실패·제약이 남으면 전체 완료로 보고하지 않는다.

## 금지 사항

- 메모리 Map·고정 JSON·정상 Case 한 건만으로 완료 처리
- 교육용 고객 업무를 `cpf-core`, `cpf-starters/common`, Starter, Runtime 제품 내부에 삽입
- Module-local DB Vendor Fallback SQL 또는 Vendor별 의미가 다른 Schema
- Timeout이나 응답 유실 뒤 실제 결과 확인 없이 무조건 재호출
- 비밀번호·Token·개인정보 원문을 Source·Log·Fixture·문서에 기록
- 매뉴얼 설명과 다른 임의 Package·API·상태·메뉴를 새로 생성

---

# 126. EDU-OPS-01 — 신규 환경 설치·Artifact·Checksum 검증

- 우선순위: **P1**
- 연결 매뉴얼: `05_플랫폼운영매뉴얼.md`
- Owner: `cpf-tools/environment·scripts·각 Runtime Config`
- 구현 상태 목표: Source·SQL·API·Config·Test·운영 확인·매뉴얼 연결이 모두 완료

## 필요 근거

- 고객 매뉴얼에서 `신규 환경 설치·Artifact·Checksum 검증`을 설명하려면 정상 호출 예시만이 아니라 상태 변화와 장애 후 정상화까지 재현 가능한 실행 예제가 필요하다.
- 이 요구사항이 없으면 고객은 기능 가능 여부를 알더라도 실제 업무 적용 순서, 실패 판정, 재처리 금지 조건을 확인할 수 없다.
- 내부 구현 구조를 보여 주기 위한 예제가 아니라 고객 업무를 CPF로 만드는 방법과 운영 결과를 증명하는 예제로 구현한다.

## 고객 업무 시나리오

고객사 신규 서버에 배포 파일을 설치하고 공급 Manifest와 checksum을 확인한다.

## 구현 위치와 책임 경계

- `cpf-tools/environment`·`cpf-tools/scripts`의 기존 운영 도구 구조
- 각 Runtime의 기존 `application-*.yml`·Profile·배포 Script 위치
- 운영 검증 Test는 기존 Script Test·Integration Test·Browser Test 체계
- 고객 업무 상태 변경과 원장 데이터는 고객 업무 Owner가 소유한다. ADM·BZA·Gateway·운영 Script가 업무 Table을 직접 수정하지 않는다.
- 공통 기능이 필요하면 기존 Public API·SPI를 우선 사용하고, 진입점이 없을 때만 실제 Consumer를 근거로 Owner Module에 확장한다.

## 입력·계약

- `artifactPath`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `manifest`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `installDir`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `serviceAccount`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- 해당 기능에 필요한 Operation ID·업무 ID·Correlation ID·요청 Hash·사용자·권한 문맥을 저장·Log·Trace·Audit에서 연결한다.
- 요청·응답·Job Parameter·이벤트·파일 Layout 중 해당되는 계약을 OpenAPI 또는 Machine-readable Schema로 제공한다.

## 상태와 저장 근거

- 필수 상태 흐름: `RECEIVED → VERIFIED → INSTALLED → READY`
- 상태마다 진입 조건, 허용 조치, 재시도 가능 여부, 최종 여부, 저장 위치와 Version 규칙을 정의한다.
- 결과 미확정·부분 성공이 가능한 경우 대상별 Attempt와 상대 상태 조회 근거를 별도 저장한다.

## 정상 처리 순서

1. 입력·권한·데이터 범위·기준 Version과 선행 상태를 검증한다.
2. 상태 변경·비동기 실행인 경우 Operation과 업무 식별자를 생성하거나 기존 멱등 요청을 조회한다. 조회 전용 기능은 조회 Context와 Cursor만 사용한다.
3. 상태 변경 기능은 고객 업무 Owner의 Application Use Case에서 Transaction 경계와 상태 전이를 수행하고, 조회 전용 기능은 일관된 Snapshot·정렬·범위 조건을 적용한다.
4. 외부 시스템·Kafka·파일·Batch·Gateway가 필요한 경우 Adapter 경계에서 Timeout·Retry·보안 정책을 적용한다.
5. 업무 결과와 Attempt·Audit·Log·Metric·Trace를 같은 식별자로 연결한다.
6. 고객 API 또는 운영 화면에서 최종 상태·건수·금액·Version·오류 0건을 확인한다.

## 예외·장애·경계 사례

- 파일 누락
- checksum 불일치
- 권한 부족
- 이전 파일 혼재
- 상태 변경이 있는 경우 DB Commit 전 실패와 Commit 후 응답 유실을 구분한다.
- 같은 요청의 중복·동시 실행·순서 역전·늦은 응답을 포함한다.
- Retry 가능한 오류와 즉시 중단해야 하는 업무 오류를 구분한다.
- 대량·다중 대상 기능에서 부분 성공이 발생하면 성공 대상을 다시 실행하지 않고 실패 대상만 Reprocess·Reconcile한다.
- 복구·보상이 필요한 기능은 복구 실패와 보상 실패도 별도 상태·Attempt·Audit로 남긴다.

## 필수 Test

- Clean install
- repeat install
- permission
- checksum
- 입력 Validation·권한·Data Scope·Masking Negative Test
- 해당 기능에 적용되는 중복·동시성·Timeout·응답 유실·부분 실패 Test
- 해당 기능의 Failure Injection 후 필요한 Retry·Restart·Reprocess·Reconcile·Rollback Test
- Oracle·PostgreSQL·MariaDB 또는 해당 Infra의 의미 동일성 Test
- Log·Metric·Trace·Audit에 Secret·개인정보 원문이 없는지 검사

## 운영·화면 확인

- ADM Health와 설치 Script 결과, 파일 목록을 대조
- 화면에는 해당 메뉴에 실제로 존재하는 조회 조건, 기본값, Column, 상세 Field, 상태, Button 활성 조건, 사유, 승인, 예상 Version, 대상별 결과만 Source와 일치하게 표시한다. 조회 전용 메뉴에 Command 항목을 만들지 않는다.
- 운영자가 정상화 완료를 판단할 수 있는 건수·금액·Version·Checksum·최종 상태·마지막 오류를 제공한다.
- 변경·외부연계 기능에서 응답 유실이 가능한 경우에만 요청을 다시 보내기 전에 Operation·거래·상대 상태를 조회하는 절차를 Browser 또는 API Test로 검증한다.

## 고객 매뉴얼·EDU 연결

- `05_플랫폼운영매뉴얼.md`의 해당 기능 장에 `EDU-OPS-01`를 연결한다.
- 선행 환경, 실행 명령, 입력값, 정상 출력, 오류 재현 명령, 복구 명령, ADM/BZA/Gateway 확인 위치를 그대로 따라 할 수 있게 작성한다.
- 교육 코드에서 고객 업무로 바꿀 Package·Table·API·Topic·파일명과 CPF 관리 영역을 구분한 전환표를 제공한다.

## 완료 조건

- `EDU-OPS-01`가 교육 Catalog·Source·Test·고객 매뉴얼에 동일 ID로 존재한다.
- 정상·Validation·권한·중복·동시성·Timeout·응답 유실·부분 실패·복구 Test가 통과한다.
- 요청·응답·상태·DB·이벤트·파일·운영 화면이 양방향으로 일치한다.
- 운영자가 다른 사람의 설명 없이 장애를 재현하고 정상화 판정을 할 수 있다.
- 실행하지 않은 Test는 성공으로 기록하지 않고, 실패·제약이 남으면 전체 완료로 보고하지 않는다.

## 금지 사항

- 메모리 Map·고정 JSON·정상 Case 한 건만으로 완료 처리
- 교육용 고객 업무를 `cpf-core`, `cpf-starters/common`, Starter, Runtime 제품 내부에 삽입
- Module-local DB Vendor Fallback SQL 또는 Vendor별 의미가 다른 Schema
- Timeout이나 응답 유실 뒤 실제 결과 확인 없이 무조건 재호출
- 비밀번호·Token·개인정보 원문을 Source·Log·Fixture·문서에 기록
- 매뉴얼 설명과 다른 임의 Package·API·상태·메뉴를 새로 생성

---

# 127. EDU-OPS-02 — Profile·환경변수·설정값 전체 검증

- 우선순위: **P1**
- 연결 매뉴얼: `05_플랫폼운영매뉴얼.md`
- Owner: `cpf-tools/environment·scripts·각 Runtime Config`
- 구현 상태 목표: Source·SQL·API·Config·Test·운영 확인·매뉴얼 연결이 모두 완료

## 필요 근거

- 고객 매뉴얼에서 `Profile·환경변수·설정값 전체 검증`을 설명하려면 정상 호출 예시만이 아니라 상태 변화와 장애 후 정상화까지 재현 가능한 실행 예제가 필요하다.
- 이 요구사항이 없으면 고객은 기능 가능 여부를 알더라도 실제 업무 적용 순서, 실패 판정, 재처리 금지 조건을 확인할 수 없다.
- 내부 구현 구조를 보여 주기 위한 예제가 아니라 고객 업무를 CPF로 만드는 방법과 운영 결과를 증명하는 예제로 구현한다.

## 고객 업무 시나리오

환경별 설정표를 채우고 누락·형식·범위 오류를 기동 전에 검출한다.

## 구현 위치와 책임 경계

- `cpf-tools/environment`·`cpf-tools/scripts`의 기존 운영 도구 구조
- 각 Runtime의 기존 `application-*.yml`·Profile·배포 Script 위치
- 운영 검증 Test는 기존 Script Test·Integration Test·Browser Test 체계
- 고객 업무 상태 변경과 원장 데이터는 고객 업무 Owner가 소유한다. ADM·BZA·Gateway·운영 Script가 업무 Table을 직접 수정하지 않는다.
- 공통 기능이 필요하면 기존 Public API·SPI를 우선 사용하고, 진입점이 없을 때만 실제 Consumer를 근거로 Owner Module에 확장한다.

## 입력·계약

- `profile`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `properties`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `environment variables`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- 해당 기능에 필요한 Operation ID·업무 ID·Correlation ID·요청 Hash·사용자·권한 문맥을 저장·Log·Trace·Audit에서 연결한다.
- 요청·응답·Job Parameter·이벤트·파일 Layout 중 해당되는 계약을 OpenAPI 또는 Machine-readable Schema로 제공한다.

## 상태와 저장 근거

- 필수 상태 흐름: `LOADED → VALIDATED → ACCEPTED/REJECTED`
- 상태마다 진입 조건, 허용 조치, 재시도 가능 여부, 최종 여부, 저장 위치와 Version 규칙을 정의한다.
- 결과 미확정·부분 성공이 가능한 경우 대상별 Attempt와 상대 상태 조회 근거를 별도 저장한다.

## 정상 처리 순서

1. 입력·권한·데이터 범위·기준 Version과 선행 상태를 검증한다.
2. 상태 변경·비동기 실행인 경우 Operation과 업무 식별자를 생성하거나 기존 멱등 요청을 조회한다. 조회 전용 기능은 조회 Context와 Cursor만 사용한다.
3. 상태 변경 기능은 고객 업무 Owner의 Application Use Case에서 Transaction 경계와 상태 전이를 수행하고, 조회 전용 기능은 일관된 Snapshot·정렬·범위 조건을 적용한다.
4. 외부 시스템·Kafka·파일·Batch·Gateway가 필요한 경우 Adapter 경계에서 Timeout·Retry·보안 정책을 적용한다.
5. 업무 결과와 Attempt·Audit·Log·Metric·Trace를 같은 식별자로 연결한다.
6. 고객 API 또는 운영 화면에서 최종 상태·건수·금액·Version·오류 0건을 확인한다.

## 예외·장애·경계 사례

- 필수값 누락
- 잘못된 URL
- 숫자 범위
- deprecated key
- 중복 source
- 상태 변경이 있는 경우 DB Commit 전 실패와 Commit 후 응답 유실을 구분한다.
- 같은 요청의 중복·동시 실행·순서 역전·늦은 응답을 포함한다.
- Retry 가능한 오류와 즉시 중단해야 하는 업무 오류를 구분한다.
- 대량·다중 대상 기능에서 부분 성공이 발생하면 성공 대상을 다시 실행하지 않고 실패 대상만 Reprocess·Reconcile한다.
- 복구·보상이 필요한 기능은 복구 실패와 보상 실패도 별도 상태·Attempt·Audit로 남긴다.

## 필수 Test

- Binding test
- startup fail-fast
- config report masking
- 입력 Validation·권한·Data Scope·Masking Negative Test
- 해당 기능에 적용되는 중복·동시성·Timeout·응답 유실·부분 실패 Test
- 해당 기능의 Failure Injection 후 필요한 Retry·Restart·Reprocess·Reconcile·Rollback Test
- Oracle·PostgreSQL·MariaDB 또는 해당 Infra의 의미 동일성 Test
- Log·Metric·Trace·Audit에 Secret·개인정보 원문이 없는지 검사

## 운영·화면 확인

- ADM Configs와 실제 적용값·source·restart 필요 여부 확인
- 화면에는 해당 메뉴에 실제로 존재하는 조회 조건, 기본값, Column, 상세 Field, 상태, Button 활성 조건, 사유, 승인, 예상 Version, 대상별 결과만 Source와 일치하게 표시한다. 조회 전용 메뉴에 Command 항목을 만들지 않는다.
- 운영자가 정상화 완료를 판단할 수 있는 건수·금액·Version·Checksum·최종 상태·마지막 오류를 제공한다.
- 변경·외부연계 기능에서 응답 유실이 가능한 경우에만 요청을 다시 보내기 전에 Operation·거래·상대 상태를 조회하는 절차를 Browser 또는 API Test로 검증한다.

## 고객 매뉴얼·EDU 연결

- `05_플랫폼운영매뉴얼.md`의 해당 기능 장에 `EDU-OPS-02`를 연결한다.
- 선행 환경, 실행 명령, 입력값, 정상 출력, 오류 재현 명령, 복구 명령, ADM/BZA/Gateway 확인 위치를 그대로 따라 할 수 있게 작성한다.
- 교육 코드에서 고객 업무로 바꿀 Package·Table·API·Topic·파일명과 CPF 관리 영역을 구분한 전환표를 제공한다.

## 완료 조건

- `EDU-OPS-02`가 교육 Catalog·Source·Test·고객 매뉴얼에 동일 ID로 존재한다.
- 정상·Validation·권한·중복·동시성·Timeout·응답 유실·부분 실패·복구 Test가 통과한다.
- 요청·응답·상태·DB·이벤트·파일·운영 화면이 양방향으로 일치한다.
- 운영자가 다른 사람의 설명 없이 장애를 재현하고 정상화 판정을 할 수 있다.
- 실행하지 않은 Test는 성공으로 기록하지 않고, 실패·제약이 남으면 전체 완료로 보고하지 않는다.

## 금지 사항

- 메모리 Map·고정 JSON·정상 Case 한 건만으로 완료 처리
- 교육용 고객 업무를 `cpf-core`, `cpf-starters/common`, Starter, Runtime 제품 내부에 삽입
- Module-local DB Vendor Fallback SQL 또는 Vendor별 의미가 다른 Schema
- Timeout이나 응답 유실 뒤 실제 결과 확인 없이 무조건 재호출
- 비밀번호·Token·개인정보 원문을 Source·Log·Fixture·문서에 기록
- 매뉴얼 설명과 다른 임의 Package·API·상태·메뉴를 새로 생성

---

# 128. EDU-OPS-03 — Secret·Certificate 배포·교체·만료 대응

- 우선순위: **P1**
- 연결 매뉴얼: `05_플랫폼운영매뉴얼.md`
- Owner: `cpf-tools/environment·scripts·각 Runtime Config`
- 구현 상태 목표: Source·SQL·API·Config·Test·운영 확인·매뉴얼 연결이 모두 완료

## 필요 근거

- 고객 매뉴얼에서 `Secret·Certificate 배포·교체·만료 대응`을 설명하려면 정상 호출 예시만이 아니라 상태 변화와 장애 후 정상화까지 재현 가능한 실행 예제가 필요하다.
- 이 요구사항이 없으면 고객은 기능 가능 여부를 알더라도 실제 업무 적용 순서, 실패 판정, 재처리 금지 조건을 확인할 수 없다.
- 내부 구현 구조를 보여 주기 위한 예제가 아니라 고객 업무를 CPF로 만드는 방법과 운영 결과를 증명하는 예제로 구현한다.

## 고객 업무 시나리오

DB 비밀번호와 TLS 인증서를 파일에 남기지 않고 교체하며 만료 전 경보를 낸다.

## 구현 위치와 책임 경계

- `cpf-tools/environment`·`cpf-tools/scripts`의 기존 운영 도구 구조
- 각 Runtime의 기존 `application-*.yml`·Profile·배포 Script 위치
- 운영 검증 Test는 기존 Script Test·Integration Test·Browser Test 체계
- 고객 업무 상태 변경과 원장 데이터는 고객 업무 Owner가 소유한다. ADM·BZA·Gateway·운영 Script가 업무 Table을 직접 수정하지 않는다.
- 공통 기능이 필요하면 기존 Public API·SPI를 우선 사용하고, 진입점이 없을 때만 실제 Consumer를 근거로 Owner Module에 확장한다.

## 입력·계약

- `secretRef`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `certChain`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `activationAt`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- 해당 기능에 필요한 Operation ID·업무 ID·Correlation ID·요청 Hash·사용자·권한 문맥을 저장·Log·Trace·Audit에서 연결한다.
- 요청·응답·Job Parameter·이벤트·파일 Layout 중 해당되는 계약을 OpenAPI 또는 Machine-readable Schema로 제공한다.

## 상태와 저장 근거

- 필수 상태 흐름: `STAGED → VALIDATED → ACTIVATED → RETIRED`
- 상태마다 진입 조건, 허용 조치, 재시도 가능 여부, 최종 여부, 저장 위치와 Version 규칙을 정의한다.
- 결과 미확정·부분 성공이 가능한 경우 대상별 Attempt와 상대 상태 조회 근거를 별도 저장한다.

## 정상 처리 순서

1. 입력·권한·데이터 범위·기준 Version과 선행 상태를 검증한다.
2. 상태 변경·비동기 실행인 경우 Operation과 업무 식별자를 생성하거나 기존 멱등 요청을 조회한다. 조회 전용 기능은 조회 Context와 Cursor만 사용한다.
3. 상태 변경 기능은 고객 업무 Owner의 Application Use Case에서 Transaction 경계와 상태 전이를 수행하고, 조회 전용 기능은 일관된 Snapshot·정렬·범위 조건을 적용한다.
4. 외부 시스템·Kafka·파일·Batch·Gateway가 필요한 경우 Adapter 경계에서 Timeout·Retry·보안 정책을 적용한다.
5. 업무 결과와 Attempt·Audit·Log·Metric·Trace를 같은 식별자로 연결한다.
6. 고객 API 또는 운영 화면에서 최종 상태·건수·금액·Version·오류 0건을 확인한다.

## 예외·장애·경계 사례

- 잘못된 chain
- key mismatch
- 부분 교체
- rollback
- 만료
- 상태 변경이 있는 경우 DB Commit 전 실패와 Commit 후 응답 유실을 구분한다.
- 같은 요청의 중복·동시 실행·순서 역전·늦은 응답을 포함한다.
- Retry 가능한 오류와 즉시 중단해야 하는 업무 오류를 구분한다.
- 대량·다중 대상 기능에서 부분 성공이 발생하면 성공 대상을 다시 실행하지 않고 실패 대상만 Reprocess·Reconcile한다.
- 복구·보상이 필요한 기능은 복구 실패와 보상 실패도 별도 상태·Attempt·Audit로 남긴다.

## 필수 Test

- Rotation test
- dual-key window
- permission
- no-secret log
- 입력 Validation·권한·Data Scope·Masking Negative Test
- 해당 기능에 적용되는 중복·동시성·Timeout·응답 유실·부분 실패 Test
- 해당 기능의 Failure Injection 후 필요한 Retry·Restart·Reprocess·Reconcile·Rollback Test
- Oracle·PostgreSQL·MariaDB 또는 해당 Infra의 의미 동일성 Test
- Log·Metric·Trace·Audit에 Secret·개인정보 원문이 없는지 검사

## 운영·화면 확인

- ADM Secrets·Security에서 Version·만료·적용 대상 확인
- 화면에는 해당 메뉴에 실제로 존재하는 조회 조건, 기본값, Column, 상세 Field, 상태, Button 활성 조건, 사유, 승인, 예상 Version, 대상별 결과만 Source와 일치하게 표시한다. 조회 전용 메뉴에 Command 항목을 만들지 않는다.
- 운영자가 정상화 완료를 판단할 수 있는 건수·금액·Version·Checksum·최종 상태·마지막 오류를 제공한다.
- 변경·외부연계 기능에서 응답 유실이 가능한 경우에만 요청을 다시 보내기 전에 Operation·거래·상대 상태를 조회하는 절차를 Browser 또는 API Test로 검증한다.

## 고객 매뉴얼·EDU 연결

- `05_플랫폼운영매뉴얼.md`의 해당 기능 장에 `EDU-OPS-03`를 연결한다.
- 선행 환경, 실행 명령, 입력값, 정상 출력, 오류 재현 명령, 복구 명령, ADM/BZA/Gateway 확인 위치를 그대로 따라 할 수 있게 작성한다.
- 교육 코드에서 고객 업무로 바꿀 Package·Table·API·Topic·파일명과 CPF 관리 영역을 구분한 전환표를 제공한다.

## 완료 조건

- `EDU-OPS-03`가 교육 Catalog·Source·Test·고객 매뉴얼에 동일 ID로 존재한다.
- 정상·Validation·권한·중복·동시성·Timeout·응답 유실·부분 실패·복구 Test가 통과한다.
- 요청·응답·상태·DB·이벤트·파일·운영 화면이 양방향으로 일치한다.
- 운영자가 다른 사람의 설명 없이 장애를 재현하고 정상화 판정을 할 수 있다.
- 실행하지 않은 Test는 성공으로 기록하지 않고, 실패·제약이 남으면 전체 완료로 보고하지 않는다.

## 금지 사항

- 메모리 Map·고정 JSON·정상 Case 한 건만으로 완료 처리
- 교육용 고객 업무를 `cpf-core`, `cpf-starters/common`, Starter, Runtime 제품 내부에 삽입
- Module-local DB Vendor Fallback SQL 또는 Vendor별 의미가 다른 Schema
- Timeout이나 응답 유실 뒤 실제 결과 확인 없이 무조건 재호출
- 비밀번호·Token·개인정보 원문을 Source·Log·Fixture·문서에 기록
- 매뉴얼 설명과 다른 임의 Package·API·상태·메뉴를 새로 생성

---

# 129. EDU-OPS-04 — DB 3종 신규 설치·Migration·Drift·Rollback

- 우선순위: **P1**
- 연결 매뉴얼: `05_플랫폼운영매뉴얼.md`
- Owner: `cpf-tools/environment·scripts·각 Runtime Config`
- 구현 상태 목표: Source·SQL·API·Config·Test·운영 확인·매뉴얼 연결이 모두 완료

## 필요 근거

- 고객 매뉴얼에서 `DB 3종 신규 설치·Migration·Drift·Rollback`을 설명하려면 정상 호출 예시만이 아니라 상태 변화와 장애 후 정상화까지 재현 가능한 실행 예제가 필요하다.
- 이 요구사항이 없으면 고객은 기능 가능 여부를 알더라도 실제 업무 적용 순서, 실패 판정, 재처리 금지 조건을 확인할 수 없다.
- 내부 구현 구조를 보여 주기 위한 예제가 아니라 고객 업무를 CPF로 만드는 방법과 운영 결과를 증명하는 예제로 구현한다.

## 고객 업무 시나리오

Oracle·PostgreSQL·MariaDB에 같은 의미의 Schema를 설치하고 Drift를 탐지한다.

## 구현 위치와 책임 경계

- `cpf-tools/environment`·`cpf-tools/scripts`의 기존 운영 도구 구조
- 각 Runtime의 기존 `application-*.yml`·Profile·배포 Script 위치
- 운영 검증 Test는 기존 Script Test·Integration Test·Browser Test 체계
- 고객 업무 상태 변경과 원장 데이터는 고객 업무 Owner가 소유한다. ADM·BZA·Gateway·운영 Script가 업무 Table을 직접 수정하지 않는다.
- 공통 기능이 필요하면 기존 Public API·SPI를 우선 사용하고, 진입점이 없을 때만 실제 Consumer를 근거로 Owner Module에 확장한다.

## 입력·계약

- `vendor`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `versionFrom`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `versionTo`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `schema`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- 해당 기능에 필요한 Operation ID·업무 ID·Correlation ID·요청 Hash·사용자·권한 문맥을 저장·Log·Trace·Audit에서 연결한다.
- 요청·응답·Job Parameter·이벤트·파일 Layout 중 해당되는 계약을 OpenAPI 또는 Machine-readable Schema로 제공한다.

## 상태와 저장 근거

- 필수 상태 흐름: `PROVISIONED → MIGRATED → VERIFIED/DRIFTED → ROLLED_BACK`
- 상태마다 진입 조건, 허용 조치, 재시도 가능 여부, 최종 여부, 저장 위치와 Version 규칙을 정의한다.
- 결과 미확정·부분 성공이 가능한 경우 대상별 Attempt와 상대 상태 조회 근거를 별도 저장한다.

## 정상 처리 순서

1. 입력·권한·데이터 범위·기준 Version과 선행 상태를 검증한다.
2. 상태 변경·비동기 실행인 경우 Operation과 업무 식별자를 생성하거나 기존 멱등 요청을 조회한다. 조회 전용 기능은 조회 Context와 Cursor만 사용한다.
3. 상태 변경 기능은 고객 업무 Owner의 Application Use Case에서 Transaction 경계와 상태 전이를 수행하고, 조회 전용 기능은 일관된 Snapshot·정렬·범위 조건을 적용한다.
4. 외부 시스템·Kafka·파일·Batch·Gateway가 필요한 경우 Adapter 경계에서 Timeout·Retry·보안 정책을 적용한다.
5. 업무 결과와 Attempt·Audit·Log·Metric·Trace를 같은 식별자로 연결한다.
6. 고객 API 또는 운영 화면에서 최종 상태·건수·금액·Version·오류 0건을 확인한다.

## 예외·장애·경계 사례

- 중단 Migration
- 재실행
- 데이터 Backfill
- 권한 누락
- index 차이
- 상태 변경이 있는 경우 DB Commit 전 실패와 Commit 후 응답 유실을 구분한다.
- 같은 요청의 중복·동시 실행·순서 역전·늦은 응답을 포함한다.
- Retry 가능한 오류와 즉시 중단해야 하는 업무 오류를 구분한다.
- 대량·다중 대상 기능에서 부분 성공이 발생하면 성공 대상을 다시 실행하지 않고 실패 대상만 Reprocess·Reconcile한다.
- 복구·보상이 필요한 기능은 복구 실패와 보상 실패도 별도 상태·Attempt·Audit로 남긴다.

## 필수 Test

- Vendor pack install/upgrade/rollback/verify
- data check
- 입력 Validation·권한·Data Scope·Masking Negative Test
- 해당 기능에 적용되는 중복·동시성·Timeout·응답 유실·부분 실패 Test
- 해당 기능의 Failure Injection 후 필요한 Retry·Restart·Reprocess·Reconcile·Rollback Test
- Oracle·PostgreSQL·MariaDB 또는 해당 Infra의 의미 동일성 Test
- Log·Metric·Trace·Audit에 Secret·개인정보 원문이 없는지 검사

## 운영·화면 확인

- ADM DB/Health와 Script 결과·Version 일치 확인
- 화면에는 해당 메뉴에 실제로 존재하는 조회 조건, 기본값, Column, 상세 Field, 상태, Button 활성 조건, 사유, 승인, 예상 Version, 대상별 결과만 Source와 일치하게 표시한다. 조회 전용 메뉴에 Command 항목을 만들지 않는다.
- 운영자가 정상화 완료를 판단할 수 있는 건수·금액·Version·Checksum·최종 상태·마지막 오류를 제공한다.
- 변경·외부연계 기능에서 응답 유실이 가능한 경우에만 요청을 다시 보내기 전에 Operation·거래·상대 상태를 조회하는 절차를 Browser 또는 API Test로 검증한다.

## 고객 매뉴얼·EDU 연결

- `05_플랫폼운영매뉴얼.md`의 해당 기능 장에 `EDU-OPS-04`를 연결한다.
- 선행 환경, 실행 명령, 입력값, 정상 출력, 오류 재현 명령, 복구 명령, ADM/BZA/Gateway 확인 위치를 그대로 따라 할 수 있게 작성한다.
- 교육 코드에서 고객 업무로 바꿀 Package·Table·API·Topic·파일명과 CPF 관리 영역을 구분한 전환표를 제공한다.

## 완료 조건

- `EDU-OPS-04`가 교육 Catalog·Source·Test·고객 매뉴얼에 동일 ID로 존재한다.
- 정상·Validation·권한·중복·동시성·Timeout·응답 유실·부분 실패·복구 Test가 통과한다.
- 요청·응답·상태·DB·이벤트·파일·운영 화면이 양방향으로 일치한다.
- 운영자가 다른 사람의 설명 없이 장애를 재현하고 정상화 판정을 할 수 있다.
- 실행하지 않은 Test는 성공으로 기록하지 않고, 실패·제약이 남으면 전체 완료로 보고하지 않는다.

## 금지 사항

- 메모리 Map·고정 JSON·정상 Case 한 건만으로 완료 처리
- 교육용 고객 업무를 `cpf-core`, `cpf-starters/common`, Starter, Runtime 제품 내부에 삽입
- Module-local DB Vendor Fallback SQL 또는 Vendor별 의미가 다른 Schema
- Timeout이나 응답 유실 뒤 실제 결과 확인 없이 무조건 재호출
- 비밀번호·Token·개인정보 원문을 Source·Log·Fixture·문서에 기록
- 매뉴얼 설명과 다른 임의 Package·API·상태·메뉴를 새로 생성

---

# 130. EDU-OPS-05 — Kafka Topic·ACL·Consumer Group Lifecycle

- 우선순위: **P1**
- 연결 매뉴얼: `05_플랫폼운영매뉴얼.md`
- Owner: `cpf-tools/environment·scripts·각 Runtime Config`
- 구현 상태 목표: Source·SQL·API·Config·Test·운영 확인·매뉴얼 연결이 모두 완료

## 필요 근거

- 고객 매뉴얼에서 `Kafka Topic·ACL·Consumer Group Lifecycle`을 설명하려면 정상 호출 예시만이 아니라 상태 변화와 장애 후 정상화까지 재현 가능한 실행 예제가 필요하다.
- 이 요구사항이 없으면 고객은 기능 가능 여부를 알더라도 실제 업무 적용 순서, 실패 판정, 재처리 금지 조건을 확인할 수 없다.
- 내부 구현 구조를 보여 주기 위한 예제가 아니라 고객 업무를 CPF로 만드는 방법과 운영 결과를 증명하는 예제로 구현한다.

## 고객 업무 시나리오

환경별 Topic·Partition·Retention·ACL을 생성하고 변경·복구한다.

## 구현 위치와 책임 경계

- `cpf-tools/environment`·`cpf-tools/scripts`의 기존 운영 도구 구조
- 각 Runtime의 기존 `application-*.yml`·Profile·배포 Script 위치
- 운영 검증 Test는 기존 Script Test·Integration Test·Browser Test 체계
- 고객 업무 상태 변경과 원장 데이터는 고객 업무 Owner가 소유한다. ADM·BZA·Gateway·운영 Script가 업무 Table을 직접 수정하지 않는다.
- 공통 기능이 필요하면 기존 Public API·SPI를 우선 사용하고, 진입점이 없을 때만 실제 Consumer를 근거로 Owner Module에 확장한다.

## 입력·계약

- `topicSpec`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `aclSpec`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `consumerGroup`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- 해당 기능에 필요한 Operation ID·업무 ID·Correlation ID·요청 Hash·사용자·권한 문맥을 저장·Log·Trace·Audit에서 연결한다.
- 요청·응답·Job Parameter·이벤트·파일 Layout 중 해당되는 계약을 OpenAPI 또는 Machine-readable Schema로 제공한다.

## 상태와 저장 근거

- 필수 상태 흐름: `PLANNED → CREATED → VERIFIED → CHANGED/ROLLED_BACK`
- 상태마다 진입 조건, 허용 조치, 재시도 가능 여부, 최종 여부, 저장 위치와 Version 규칙을 정의한다.
- 결과 미확정·부분 성공이 가능한 경우 대상별 Attempt와 상대 상태 조회 근거를 별도 저장한다.

## 정상 처리 순서

1. 입력·권한·데이터 범위·기준 Version과 선행 상태를 검증한다.
2. 상태 변경·비동기 실행인 경우 Operation과 업무 식별자를 생성하거나 기존 멱등 요청을 조회한다. 조회 전용 기능은 조회 Context와 Cursor만 사용한다.
3. 상태 변경 기능은 고객 업무 Owner의 Application Use Case에서 Transaction 경계와 상태 전이를 수행하고, 조회 전용 기능은 일관된 Snapshot·정렬·범위 조건을 적용한다.
4. 외부 시스템·Kafka·파일·Batch·Gateway가 필요한 경우 Adapter 경계에서 Timeout·Retry·보안 정책을 적용한다.
5. 업무 결과와 Attempt·Audit·Log·Metric·Trace를 같은 식별자로 연결한다.
6. 고객 API 또는 운영 화면에서 최종 상태·건수·금액·Version·오류 0건을 확인한다.

## 예외·장애·경계 사례

- Topic 기존 설정 충돌
- ACL 누락
- partition 감소 시도
- lag 폭증
- 상태 변경이 있는 경우 DB Commit 전 실패와 Commit 후 응답 유실을 구분한다.
- 같은 요청의 중복·동시 실행·순서 역전·늦은 응답을 포함한다.
- Retry 가능한 오류와 즉시 중단해야 하는 업무 오류를 구분한다.
- 대량·다중 대상 기능에서 부분 성공이 발생하면 성공 대상을 다시 실행하지 않고 실패 대상만 Reprocess·Reconcile한다.
- 복구·보상이 필요한 기능은 복구 실패와 보상 실패도 별도 상태·Attempt·Audit로 남긴다.

## 필수 Test

- Provision idempotency
- ACL negative
- retention
- producer/consumer
- 입력 Validation·권한·Data Scope·Masking Negative Test
- 해당 기능에 적용되는 중복·동시성·Timeout·응답 유실·부분 실패 Test
- 해당 기능의 Failure Injection 후 필요한 Retry·Restart·Reprocess·Reconcile·Rollback Test
- Oracle·PostgreSQL·MariaDB 또는 해당 Infra의 의미 동일성 Test
- Log·Metric·Trace·Audit에 Secret·개인정보 원문이 없는지 검사

## 운영·화면 확인

- ADM Messages·Workers에서 lag·권한 오류·재처리 확인
- 화면에는 해당 메뉴에 실제로 존재하는 조회 조건, 기본값, Column, 상세 Field, 상태, Button 활성 조건, 사유, 승인, 예상 Version, 대상별 결과만 Source와 일치하게 표시한다. 조회 전용 메뉴에 Command 항목을 만들지 않는다.
- 운영자가 정상화 완료를 판단할 수 있는 건수·금액·Version·Checksum·최종 상태·마지막 오류를 제공한다.
- 변경·외부연계 기능에서 응답 유실이 가능한 경우에만 요청을 다시 보내기 전에 Operation·거래·상대 상태를 조회하는 절차를 Browser 또는 API Test로 검증한다.

## 고객 매뉴얼·EDU 연결

- `05_플랫폼운영매뉴얼.md`의 해당 기능 장에 `EDU-OPS-05`를 연결한다.
- 선행 환경, 실행 명령, 입력값, 정상 출력, 오류 재현 명령, 복구 명령, ADM/BZA/Gateway 확인 위치를 그대로 따라 할 수 있게 작성한다.
- 교육 코드에서 고객 업무로 바꿀 Package·Table·API·Topic·파일명과 CPF 관리 영역을 구분한 전환표를 제공한다.

## 완료 조건

- `EDU-OPS-05`가 교육 Catalog·Source·Test·고객 매뉴얼에 동일 ID로 존재한다.
- 정상·Validation·권한·중복·동시성·Timeout·응답 유실·부분 실패·복구 Test가 통과한다.
- 요청·응답·상태·DB·이벤트·파일·운영 화면이 양방향으로 일치한다.
- 운영자가 다른 사람의 설명 없이 장애를 재현하고 정상화 판정을 할 수 있다.
- 실행하지 않은 Test는 성공으로 기록하지 않고, 실패·제약이 남으면 전체 완료로 보고하지 않는다.

## 금지 사항

- 메모리 Map·고정 JSON·정상 Case 한 건만으로 완료 처리
- 교육용 고객 업무를 `cpf-core`, `cpf-starters/common`, Starter, Runtime 제품 내부에 삽입
- Module-local DB Vendor Fallback SQL 또는 Vendor별 의미가 다른 Schema
- Timeout이나 응답 유실 뒤 실제 결과 확인 없이 무조건 재호출
- 비밀번호·Token·개인정보 원문을 Source·Log·Fixture·문서에 기록
- 매뉴얼 설명과 다른 임의 Package·API·상태·메뉴를 새로 생성

---

# 131. EDU-OPS-06 — 기동·종료·Health·Dependency 순서

- 우선순위: **P1**
- 연결 매뉴얼: `05_플랫폼운영매뉴얼.md`
- Owner: `cpf-tools/environment·scripts·각 Runtime Config`
- 구현 상태 목표: Source·SQL·API·Config·Test·운영 확인·매뉴얼 연결이 모두 완료

## 필요 근거

- 고객 매뉴얼에서 `기동·종료·Health·Dependency 순서`을 설명하려면 정상 호출 예시만이 아니라 상태 변화와 장애 후 정상화까지 재현 가능한 실행 예제가 필요하다.
- 이 요구사항이 없으면 고객은 기능 가능 여부를 알더라도 실제 업무 적용 순서, 실패 판정, 재처리 금지 조건을 확인할 수 없다.
- 내부 구현 구조를 보여 주기 위한 예제가 아니라 고객 업무를 CPF로 만드는 방법과 운영 결과를 증명하는 예제로 구현한다.

## 고객 업무 시나리오

DB·Kafka·Runtime·ADM을 의존 순서로 기동하고 Drain 후 종료한다.

## 구현 위치와 책임 경계

- `cpf-tools/environment`·`cpf-tools/scripts`의 기존 운영 도구 구조
- 각 Runtime의 기존 `application-*.yml`·Profile·배포 Script 위치
- 운영 검증 Test는 기존 Script Test·Integration Test·Browser Test 체계
- 고객 업무 상태 변경과 원장 데이터는 고객 업무 Owner가 소유한다. ADM·BZA·Gateway·운영 Script가 업무 Table을 직접 수정하지 않는다.
- 공통 기능이 필요하면 기존 Public API·SPI를 우선 사용하고, 진입점이 없을 때만 실제 Consumer를 근거로 Owner Module에 확장한다.

## 입력·계약

- `targetServices`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `timeout`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `drainPolicy`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- 해당 기능에 필요한 Operation ID·업무 ID·Correlation ID·요청 Hash·사용자·권한 문맥을 저장·Log·Trace·Audit에서 연결한다.
- 요청·응답·Job Parameter·이벤트·파일 Layout 중 해당되는 계약을 OpenAPI 또는 Machine-readable Schema로 제공한다.

## 상태와 저장 근거

- 필수 상태 흐름: `STARTING → HEALTHY; DRAINING → STOPPED`
- 상태마다 진입 조건, 허용 조치, 재시도 가능 여부, 최종 여부, 저장 위치와 Version 규칙을 정의한다.
- 결과 미확정·부분 성공이 가능한 경우 대상별 Attempt와 상대 상태 조회 근거를 별도 저장한다.

## 정상 처리 순서

1. 입력·권한·데이터 범위·기준 Version과 선행 상태를 검증한다.
2. 상태 변경·비동기 실행인 경우 Operation과 업무 식별자를 생성하거나 기존 멱등 요청을 조회한다. 조회 전용 기능은 조회 Context와 Cursor만 사용한다.
3. 상태 변경 기능은 고객 업무 Owner의 Application Use Case에서 Transaction 경계와 상태 전이를 수행하고, 조회 전용 기능은 일관된 Snapshot·정렬·범위 조건을 적용한다.
4. 외부 시스템·Kafka·파일·Batch·Gateway가 필요한 경우 Adapter 경계에서 Timeout·Retry·보안 정책을 적용한다.
5. 업무 결과와 Attempt·Audit·Log·Metric·Trace를 같은 식별자로 연결한다.
6. 고객 API 또는 운영 화면에서 최종 상태·건수·금액·Version·오류 0건을 확인한다.

## 예외·장애·경계 사례

- Dependency 지연
- Health false positive
- 종료 중 요청
- 강제 kill
- 상태 변경이 있는 경우 DB Commit 전 실패와 Commit 후 응답 유실을 구분한다.
- 같은 요청의 중복·동시 실행·순서 역전·늦은 응답을 포함한다.
- Retry 가능한 오류와 즉시 중단해야 하는 업무 오류를 구분한다.
- 대량·다중 대상 기능에서 부분 성공이 발생하면 성공 대상을 다시 실행하지 않고 실패 대상만 Reprocess·Reconcile한다.
- 복구·보상이 필요한 기능은 복구 실패와 보상 실패도 별도 상태·Attempt·Audit로 남긴다.

## 필수 Test

- Start/stop scripts
- readiness/liveness
- drain
- recovery
- 입력 Validation·권한·Data Scope·Masking Negative Test
- 해당 기능에 적용되는 중복·동시성·Timeout·응답 유실·부분 실패 Test
- 해당 기능의 Failure Injection 후 필요한 Retry·Restart·Reprocess·Reconcile·Rollback Test
- Oracle·PostgreSQL·MariaDB 또는 해당 Infra의 의미 동일성 Test
- Log·Metric·Trace·Audit에 Secret·개인정보 원문이 없는지 검사

## 운영·화면 확인

- ADM Topology·Health에서 상태 전이 확인
- 화면에는 해당 메뉴에 실제로 존재하는 조회 조건, 기본값, Column, 상세 Field, 상태, Button 활성 조건, 사유, 승인, 예상 Version, 대상별 결과만 Source와 일치하게 표시한다. 조회 전용 메뉴에 Command 항목을 만들지 않는다.
- 운영자가 정상화 완료를 판단할 수 있는 건수·금액·Version·Checksum·최종 상태·마지막 오류를 제공한다.
- 변경·외부연계 기능에서 응답 유실이 가능한 경우에만 요청을 다시 보내기 전에 Operation·거래·상대 상태를 조회하는 절차를 Browser 또는 API Test로 검증한다.

## 고객 매뉴얼·EDU 연결

- `05_플랫폼운영매뉴얼.md`의 해당 기능 장에 `EDU-OPS-06`를 연결한다.
- 선행 환경, 실행 명령, 입력값, 정상 출력, 오류 재현 명령, 복구 명령, ADM/BZA/Gateway 확인 위치를 그대로 따라 할 수 있게 작성한다.
- 교육 코드에서 고객 업무로 바꿀 Package·Table·API·Topic·파일명과 CPF 관리 영역을 구분한 전환표를 제공한다.

## 완료 조건

- `EDU-OPS-06`가 교육 Catalog·Source·Test·고객 매뉴얼에 동일 ID로 존재한다.
- 정상·Validation·권한·중복·동시성·Timeout·응답 유실·부분 실패·복구 Test가 통과한다.
- 요청·응답·상태·DB·이벤트·파일·운영 화면이 양방향으로 일치한다.
- 운영자가 다른 사람의 설명 없이 장애를 재현하고 정상화 판정을 할 수 있다.
- 실행하지 않은 Test는 성공으로 기록하지 않고, 실패·제약이 남으면 전체 완료로 보고하지 않는다.

## 금지 사항

- 메모리 Map·고정 JSON·정상 Case 한 건만으로 완료 처리
- 교육용 고객 업무를 `cpf-core`, `cpf-starters/common`, Starter, Runtime 제품 내부에 삽입
- Module-local DB Vendor Fallback SQL 또는 Vendor별 의미가 다른 Schema
- Timeout이나 응답 유실 뒤 실제 결과 확인 없이 무조건 재호출
- 비밀번호·Token·개인정보 원문을 Source·Log·Fixture·문서에 기록
- 매뉴얼 설명과 다른 임의 Package·API·상태·메뉴를 새로 생성

---

# 132. EDU-OPS-07 — Rolling 배포·Session·Connection Drain

- 우선순위: **P1**
- 연결 매뉴얼: `05_플랫폼운영매뉴얼.md`
- Owner: `cpf-tools/environment·scripts·각 Runtime Config`
- 구현 상태 목표: Source·SQL·API·Config·Test·운영 확인·매뉴얼 연결이 모두 완료

## 필요 근거

- 고객 매뉴얼에서 `Rolling 배포·Session·Connection Drain`을 설명하려면 정상 호출 예시만이 아니라 상태 변화와 장애 후 정상화까지 재현 가능한 실행 예제가 필요하다.
- 이 요구사항이 없으면 고객은 기능 가능 여부를 알더라도 실제 업무 적용 순서, 실패 판정, 재처리 금지 조건을 확인할 수 없다.
- 내부 구현 구조를 보여 주기 위한 예제가 아니라 고객 업무를 CPF로 만드는 방법과 운영 결과를 증명하는 예제로 구현한다.

## 고객 업무 시나리오

다중 인스턴스를 한 대씩 교체하며 요청과 Batch Worker 소유권을 안전하게 넘긴다.

## 구현 위치와 책임 경계

- `cpf-tools/environment`·`cpf-tools/scripts`의 기존 운영 도구 구조
- 각 Runtime의 기존 `application-*.yml`·Profile·배포 Script 위치
- 운영 검증 Test는 기존 Script Test·Integration Test·Browser Test 체계
- 고객 업무 상태 변경과 원장 데이터는 고객 업무 Owner가 소유한다. ADM·BZA·Gateway·운영 Script가 업무 Table을 직접 수정하지 않는다.
- 공통 기능이 필요하면 기존 Public API·SPI를 우선 사용하고, 진입점이 없을 때만 실제 Consumer를 근거로 Owner Module에 확장한다.

## 입력·계약

- `artifactVersion`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `batchSize`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `maxUnavailable`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- 해당 기능에 필요한 Operation ID·업무 ID·Correlation ID·요청 Hash·사용자·권한 문맥을 저장·Log·Trace·Audit에서 연결한다.
- 요청·응답·Job Parameter·이벤트·파일 Layout 중 해당되는 계약을 OpenAPI 또는 Machine-readable Schema로 제공한다.

## 상태와 저장 근거

- 필수 상태 흐름: `DRAINING → UPGRADING → WARMING → ACTIVE`
- 상태마다 진입 조건, 허용 조치, 재시도 가능 여부, 최종 여부, 저장 위치와 Version 규칙을 정의한다.
- 결과 미확정·부분 성공이 가능한 경우 대상별 Attempt와 상대 상태 조회 근거를 별도 저장한다.

## 정상 처리 순서

1. 입력·권한·데이터 범위·기준 Version과 선행 상태를 검증한다.
2. 상태 변경·비동기 실행인 경우 Operation과 업무 식별자를 생성하거나 기존 멱등 요청을 조회한다. 조회 전용 기능은 조회 Context와 Cursor만 사용한다.
3. 상태 변경 기능은 고객 업무 Owner의 Application Use Case에서 Transaction 경계와 상태 전이를 수행하고, 조회 전용 기능은 일관된 Snapshot·정렬·범위 조건을 적용한다.
4. 외부 시스템·Kafka·파일·Batch·Gateway가 필요한 경우 Adapter 경계에서 Timeout·Retry·보안 정책을 적용한다.
5. 업무 결과와 Attempt·Audit·Log·Metric·Trace를 같은 식별자로 연결한다.
6. 고객 API 또는 운영 화면에서 최종 상태·건수·금액·Version·오류 0건을 확인한다.

## 예외·장애·경계 사례

- Health 늦음
- Session sticky
- long request
- lease owner 종료
- 상태 변경이 있는 경우 DB Commit 전 실패와 Commit 후 응답 유실을 구분한다.
- 같은 요청의 중복·동시 실행·순서 역전·늦은 응답을 포함한다.
- Retry 가능한 오류와 즉시 중단해야 하는 업무 오류를 구분한다.
- 대량·다중 대상 기능에서 부분 성공이 발생하면 성공 대상을 다시 실행하지 않고 실패 대상만 Reprocess·Reconcile한다.
- 복구·보상이 필요한 기능은 복구 실패와 보상 실패도 별도 상태·Attempt·Audit로 남긴다.

## 필수 Test

- Rolling test
- traffic drain
- lease transfer
- rollback
- 입력 Validation·권한·Data Scope·Masking Negative Test
- 해당 기능에 적용되는 중복·동시성·Timeout·응답 유실·부분 실패 Test
- 해당 기능의 Failure Injection 후 필요한 Retry·Restart·Reprocess·Reconcile·Rollback Test
- Oracle·PostgreSQL·MariaDB 또는 해당 Infra의 의미 동일성 Test
- Log·Metric·Trace·Audit에 Secret·개인정보 원문이 없는지 검사

## 운영·화면 확인

- ADM Topology·Runtime Control에서 대상별 상태 확인
- 화면에는 해당 메뉴에 실제로 존재하는 조회 조건, 기본값, Column, 상세 Field, 상태, Button 활성 조건, 사유, 승인, 예상 Version, 대상별 결과만 Source와 일치하게 표시한다. 조회 전용 메뉴에 Command 항목을 만들지 않는다.
- 운영자가 정상화 완료를 판단할 수 있는 건수·금액·Version·Checksum·최종 상태·마지막 오류를 제공한다.
- 변경·외부연계 기능에서 응답 유실이 가능한 경우에만 요청을 다시 보내기 전에 Operation·거래·상대 상태를 조회하는 절차를 Browser 또는 API Test로 검증한다.

## 고객 매뉴얼·EDU 연결

- `05_플랫폼운영매뉴얼.md`의 해당 기능 장에 `EDU-OPS-07`를 연결한다.
- 선행 환경, 실행 명령, 입력값, 정상 출력, 오류 재현 명령, 복구 명령, ADM/BZA/Gateway 확인 위치를 그대로 따라 할 수 있게 작성한다.
- 교육 코드에서 고객 업무로 바꿀 Package·Table·API·Topic·파일명과 CPF 관리 영역을 구분한 전환표를 제공한다.

## 완료 조건

- `EDU-OPS-07`가 교육 Catalog·Source·Test·고객 매뉴얼에 동일 ID로 존재한다.
- 정상·Validation·권한·중복·동시성·Timeout·응답 유실·부분 실패·복구 Test가 통과한다.
- 요청·응답·상태·DB·이벤트·파일·운영 화면이 양방향으로 일치한다.
- 운영자가 다른 사람의 설명 없이 장애를 재현하고 정상화 판정을 할 수 있다.
- 실행하지 않은 Test는 성공으로 기록하지 않고, 실패·제약이 남으면 전체 완료로 보고하지 않는다.

## 금지 사항

- 메모리 Map·고정 JSON·정상 Case 한 건만으로 완료 처리
- 교육용 고객 업무를 `cpf-core`, `cpf-starters/common`, Starter, Runtime 제품 내부에 삽입
- Module-local DB Vendor Fallback SQL 또는 Vendor별 의미가 다른 Schema
- Timeout이나 응답 유실 뒤 실제 결과 확인 없이 무조건 재호출
- 비밀번호·Token·개인정보 원문을 Source·Log·Fixture·문서에 기록
- 매뉴얼 설명과 다른 임의 Package·API·상태·메뉴를 새로 생성

---

# 133. EDU-OPS-08 — Blue-Green·Canary 전환·되돌리기

- 우선순위: **P1**
- 연결 매뉴얼: `05_플랫폼운영매뉴얼.md`
- Owner: `cpf-tools/environment·scripts·각 Runtime Config`
- 구현 상태 목표: Source·SQL·API·Config·Test·운영 확인·매뉴얼 연결이 모두 완료

## 필요 근거

- 고객 매뉴얼에서 `Blue-Green·Canary 전환·되돌리기`을 설명하려면 정상 호출 예시만이 아니라 상태 변화와 장애 후 정상화까지 재현 가능한 실행 예제가 필요하다.
- 이 요구사항이 없으면 고객은 기능 가능 여부를 알더라도 실제 업무 적용 순서, 실패 판정, 재처리 금지 조건을 확인할 수 없다.
- 내부 구현 구조를 보여 주기 위한 예제가 아니라 고객 업무를 CPF로 만드는 방법과 운영 결과를 증명하는 예제로 구현한다.

## 고객 업무 시나리오

Green 환경을 검증하고 일부 트래픽 후 전체 전환하며 문제 시 Blue로 복구한다.

## 구현 위치와 책임 경계

- `cpf-tools/environment`·`cpf-tools/scripts`의 기존 운영 도구 구조
- 각 Runtime의 기존 `application-*.yml`·Profile·배포 Script 위치
- 운영 검증 Test는 기존 Script Test·Integration Test·Browser Test 체계
- 고객 업무 상태 변경과 원장 데이터는 고객 업무 Owner가 소유한다. ADM·BZA·Gateway·운영 Script가 업무 Table을 직접 수정하지 않는다.
- 공통 기능이 필요하면 기존 Public API·SPI를 우선 사용하고, 진입점이 없을 때만 실제 Consumer를 근거로 Owner Module에 확장한다.

## 입력·계약

- `blueVersion`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `greenVersion`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `trafficWeight`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- 해당 기능에 필요한 Operation ID·업무 ID·Correlation ID·요청 Hash·사용자·권한 문맥을 저장·Log·Trace·Audit에서 연결한다.
- 요청·응답·Job Parameter·이벤트·파일 Layout 중 해당되는 계약을 OpenAPI 또는 Machine-readable Schema로 제공한다.

## 상태와 저장 근거

- 필수 상태 흐름: `DEPLOYED → VERIFIED → CANARY → SWITCHED/ROLLED_BACK`
- 상태마다 진입 조건, 허용 조치, 재시도 가능 여부, 최종 여부, 저장 위치와 Version 규칙을 정의한다.
- 결과 미확정·부분 성공이 가능한 경우 대상별 Attempt와 상대 상태 조회 근거를 별도 저장한다.

## 정상 처리 순서

1. 입력·권한·데이터 범위·기준 Version과 선행 상태를 검증한다.
2. 상태 변경·비동기 실행인 경우 Operation과 업무 식별자를 생성하거나 기존 멱등 요청을 조회한다. 조회 전용 기능은 조회 Context와 Cursor만 사용한다.
3. 상태 변경 기능은 고객 업무 Owner의 Application Use Case에서 Transaction 경계와 상태 전이를 수행하고, 조회 전용 기능은 일관된 Snapshot·정렬·범위 조건을 적용한다.
4. 외부 시스템·Kafka·파일·Batch·Gateway가 필요한 경우 Adapter 경계에서 Timeout·Retry·보안 정책을 적용한다.
5. 업무 결과와 Attempt·Audit·Log·Metric·Trace를 같은 식별자로 연결한다.
6. 고객 API 또는 운영 화면에서 최종 상태·건수·금액·Version·오류 0건을 확인한다.

## 예외·장애·경계 사례

- DB schema 비호환
- background job 이중 실행
- cache warmup
- partial DNS
- 상태 변경이 있는 경우 DB Commit 전 실패와 Commit 후 응답 유실을 구분한다.
- 같은 요청의 중복·동시 실행·순서 역전·늦은 응답을 포함한다.
- Retry 가능한 오류와 즉시 중단해야 하는 업무 오류를 구분한다.
- 대량·다중 대상 기능에서 부분 성공이 발생하면 성공 대상을 다시 실행하지 않고 실패 대상만 Reprocess·Reconcile한다.
- 복구·보상이 필요한 기능은 복구 실패와 보상 실패도 별도 상태·Attempt·Audit로 남긴다.

## 필수 Test

- Compatibility gate
- one-writer
- traffic probe
- rollback
- 입력 Validation·권한·Data Scope·Masking Negative Test
- 해당 기능에 적용되는 중복·동시성·Timeout·응답 유실·부분 실패 Test
- 해당 기능의 Failure Injection 후 필요한 Retry·Restart·Reprocess·Reconcile·Rollback Test
- Oracle·PostgreSQL·MariaDB 또는 해당 Infra의 의미 동일성 Test
- Log·Metric·Trace·Audit에 Secret·개인정보 원문이 없는지 검사

## 운영·화면 확인

- Gateway Apply Status와 ADM Health/Transactions 대조
- 화면에는 해당 메뉴에 실제로 존재하는 조회 조건, 기본값, Column, 상세 Field, 상태, Button 활성 조건, 사유, 승인, 예상 Version, 대상별 결과만 Source와 일치하게 표시한다. 조회 전용 메뉴에 Command 항목을 만들지 않는다.
- 운영자가 정상화 완료를 판단할 수 있는 건수·금액·Version·Checksum·최종 상태·마지막 오류를 제공한다.
- 변경·외부연계 기능에서 응답 유실이 가능한 경우에만 요청을 다시 보내기 전에 Operation·거래·상대 상태를 조회하는 절차를 Browser 또는 API Test로 검증한다.

## 고객 매뉴얼·EDU 연결

- `05_플랫폼운영매뉴얼.md`의 해당 기능 장에 `EDU-OPS-08`를 연결한다.
- 선행 환경, 실행 명령, 입력값, 정상 출력, 오류 재현 명령, 복구 명령, ADM/BZA/Gateway 확인 위치를 그대로 따라 할 수 있게 작성한다.
- 교육 코드에서 고객 업무로 바꿀 Package·Table·API·Topic·파일명과 CPF 관리 영역을 구분한 전환표를 제공한다.

## 완료 조건

- `EDU-OPS-08`가 교육 Catalog·Source·Test·고객 매뉴얼에 동일 ID로 존재한다.
- 정상·Validation·권한·중복·동시성·Timeout·응답 유실·부분 실패·복구 Test가 통과한다.
- 요청·응답·상태·DB·이벤트·파일·운영 화면이 양방향으로 일치한다.
- 운영자가 다른 사람의 설명 없이 장애를 재현하고 정상화 판정을 할 수 있다.
- 실행하지 않은 Test는 성공으로 기록하지 않고, 실패·제약이 남으면 전체 완료로 보고하지 않는다.

## 금지 사항

- 메모리 Map·고정 JSON·정상 Case 한 건만으로 완료 처리
- 교육용 고객 업무를 `cpf-core`, `cpf-starters/common`, Starter, Runtime 제품 내부에 삽입
- Module-local DB Vendor Fallback SQL 또는 Vendor별 의미가 다른 Schema
- Timeout이나 응답 유실 뒤 실제 결과 확인 없이 무조건 재호출
- 비밀번호·Token·개인정보 원문을 Source·Log·Fixture·문서에 기록
- 매뉴얼 설명과 다른 임의 Package·API·상태·메뉴를 새로 생성

---

# 134. EDU-OPS-09 — 설정 변경 Partial Apply·Reconcile

- 우선순위: **P1**
- 연결 매뉴얼: `05_플랫폼운영매뉴얼.md`
- Owner: `cpf-tools/environment·scripts·각 Runtime Config`
- 구현 상태 목표: Source·SQL·API·Config·Test·운영 확인·매뉴얼 연결이 모두 완료

## 필요 근거

- 고객 매뉴얼에서 `설정 변경 Partial Apply·Reconcile`을 설명하려면 정상 호출 예시만이 아니라 상태 변화와 장애 후 정상화까지 재현 가능한 실행 예제가 필요하다.
- 이 요구사항이 없으면 고객은 기능 가능 여부를 알더라도 실제 업무 적용 순서, 실패 판정, 재처리 금지 조건을 확인할 수 없다.
- 내부 구현 구조를 보여 주기 위한 예제가 아니라 고객 업무를 CPF로 만드는 방법과 운영 결과를 증명하는 예제로 구현한다.

## 고객 업무 시나리오

설정 Package가 10개 인스턴스 중 2개 실패하면 성공 대상은 유지하고 실패만 재적용하거나 전체 복구한다.

## 구현 위치와 책임 경계

- `cpf-tools/environment`·`cpf-tools/scripts`의 기존 운영 도구 구조
- 각 Runtime의 기존 `application-*.yml`·Profile·배포 Script 위치
- 운영 검증 Test는 기존 Script Test·Integration Test·Browser Test 체계
- 고객 업무 상태 변경과 원장 데이터는 고객 업무 Owner가 소유한다. ADM·BZA·Gateway·운영 Script가 업무 Table을 직접 수정하지 않는다.
- 공통 기능이 필요하면 기존 Public API·SPI를 우선 사용하고, 진입점이 없을 때만 실제 Consumer를 근거로 Owner Module에 확장한다.

## 입력·계약

- `configVersion`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `checksum`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `targets`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- 해당 기능에 필요한 Operation ID·업무 ID·Correlation ID·요청 Hash·사용자·권한 문맥을 저장·Log·Trace·Audit에서 연결한다.
- 요청·응답·Job Parameter·이벤트·파일 Layout 중 해당되는 계약을 OpenAPI 또는 Machine-readable Schema로 제공한다.

## 상태와 저장 근거

- 필수 상태 흐름: `VALIDATED → APPLYING → PARTIAL → RECONCILED/ROLLED_BACK`
- 상태마다 진입 조건, 허용 조치, 재시도 가능 여부, 최종 여부, 저장 위치와 Version 규칙을 정의한다.
- 결과 미확정·부분 성공이 가능한 경우 대상별 Attempt와 상대 상태 조회 근거를 별도 저장한다.

## 정상 처리 순서

1. 입력·권한·데이터 범위·기준 Version과 선행 상태를 검증한다.
2. 상태 변경·비동기 실행인 경우 Operation과 업무 식별자를 생성하거나 기존 멱등 요청을 조회한다. 조회 전용 기능은 조회 Context와 Cursor만 사용한다.
3. 상태 변경 기능은 고객 업무 Owner의 Application Use Case에서 Transaction 경계와 상태 전이를 수행하고, 조회 전용 기능은 일관된 Snapshot·정렬·범위 조건을 적용한다.
4. 외부 시스템·Kafka·파일·Batch·Gateway가 필요한 경우 Adapter 경계에서 Timeout·Retry·보안 정책을 적용한다.
5. 업무 결과와 Attempt·Audit·Log·Metric·Trace를 같은 식별자로 연결한다.
6. 고객 API 또는 운영 화면에서 최종 상태·건수·금액·Version·오류 0건을 확인한다.

## 예외·장애·경계 사례

- offline instance
- ACK loss
- restart required
- checksum drift
- 상태 변경이 있는 경우 DB Commit 전 실패와 Commit 후 응답 유실을 구분한다.
- 같은 요청의 중복·동시 실행·순서 역전·늦은 응답을 포함한다.
- Retry 가능한 오류와 즉시 중단해야 하는 업무 오류를 구분한다.
- 대량·다중 대상 기능에서 부분 성공이 발생하면 성공 대상을 다시 실행하지 않고 실패 대상만 Reprocess·Reconcile한다.
- 복구·보상이 필요한 기능은 복구 실패와 보상 실패도 별도 상태·Attempt·Audit로 남긴다.

## 필수 Test

- Per-target result
- reapply
- LKG
- audit
- 입력 Validation·권한·Data Scope·Masking Negative Test
- 해당 기능에 적용되는 중복·동시성·Timeout·응답 유실·부분 실패 Test
- 해당 기능의 Failure Injection 후 필요한 Retry·Restart·Reprocess·Reconcile·Rollback Test
- Oracle·PostgreSQL·MariaDB 또는 해당 Infra의 의미 동일성 Test
- Log·Metric·Trace·Audit에 Secret·개인정보 원문이 없는지 검사

## 운영·화면 확인

- ADM Configs·Apply Status에서 대상별 결과 확인
- 화면에는 해당 메뉴에 실제로 존재하는 조회 조건, 기본값, Column, 상세 Field, 상태, Button 활성 조건, 사유, 승인, 예상 Version, 대상별 결과만 Source와 일치하게 표시한다. 조회 전용 메뉴에 Command 항목을 만들지 않는다.
- 운영자가 정상화 완료를 판단할 수 있는 건수·금액·Version·Checksum·최종 상태·마지막 오류를 제공한다.
- 변경·외부연계 기능에서 응답 유실이 가능한 경우에만 요청을 다시 보내기 전에 Operation·거래·상대 상태를 조회하는 절차를 Browser 또는 API Test로 검증한다.

## 고객 매뉴얼·EDU 연결

- `05_플랫폼운영매뉴얼.md`의 해당 기능 장에 `EDU-OPS-09`를 연결한다.
- 선행 환경, 실행 명령, 입력값, 정상 출력, 오류 재현 명령, 복구 명령, ADM/BZA/Gateway 확인 위치를 그대로 따라 할 수 있게 작성한다.
- 교육 코드에서 고객 업무로 바꿀 Package·Table·API·Topic·파일명과 CPF 관리 영역을 구분한 전환표를 제공한다.

## 완료 조건

- `EDU-OPS-09`가 교육 Catalog·Source·Test·고객 매뉴얼에 동일 ID로 존재한다.
- 정상·Validation·권한·중복·동시성·Timeout·응답 유실·부분 실패·복구 Test가 통과한다.
- 요청·응답·상태·DB·이벤트·파일·운영 화면이 양방향으로 일치한다.
- 운영자가 다른 사람의 설명 없이 장애를 재현하고 정상화 판정을 할 수 있다.
- 실행하지 않은 Test는 성공으로 기록하지 않고, 실패·제약이 남으면 전체 완료로 보고하지 않는다.

## 금지 사항

- 메모리 Map·고정 JSON·정상 Case 한 건만으로 완료 처리
- 교육용 고객 업무를 `cpf-core`, `cpf-starters/common`, Starter, Runtime 제품 내부에 삽입
- Module-local DB Vendor Fallback SQL 또는 Vendor별 의미가 다른 Schema
- Timeout이나 응답 유실 뒤 실제 결과 확인 없이 무조건 재호출
- 비밀번호·Token·개인정보 원문을 Source·Log·Fixture·문서에 기록
- 매뉴얼 설명과 다른 임의 Package·API·상태·메뉴를 새로 생성

---

# 135. EDU-OPS-10 — Log·Metric·Trace 수집 장애·보존·용량

- 우선순위: **P1**
- 연결 매뉴얼: `05_플랫폼운영매뉴얼.md`
- Owner: `cpf-tools/environment·scripts·각 Runtime Config`
- 구현 상태 목표: Source·SQL·API·Config·Test·운영 확인·매뉴얼 연결이 모두 완료

## 필요 근거

- 고객 매뉴얼에서 `Log·Metric·Trace 수집 장애·보존·용량`을 설명하려면 정상 호출 예시만이 아니라 상태 변화와 장애 후 정상화까지 재현 가능한 실행 예제가 필요하다.
- 이 요구사항이 없으면 고객은 기능 가능 여부를 알더라도 실제 업무 적용 순서, 실패 판정, 재처리 금지 조건을 확인할 수 없다.
- 내부 구현 구조를 보여 주기 위한 예제가 아니라 고객 업무를 CPF로 만드는 방법과 운영 결과를 증명하는 예제로 구현한다.

## 고객 업무 시나리오

관측 수집기가 중단돼도 Application이 멈추지 않고 복구 후 손실 범위를 표시한다.

## 구현 위치와 책임 경계

- `cpf-tools/environment`·`cpf-tools/scripts`의 기존 운영 도구 구조
- 각 Runtime의 기존 `application-*.yml`·Profile·배포 Script 위치
- 운영 검증 Test는 기존 Script Test·Integration Test·Browser Test 체계
- 고객 업무 상태 변경과 원장 데이터는 고객 업무 Owner가 소유한다. ADM·BZA·Gateway·운영 Script가 업무 Table을 직접 수정하지 않는다.
- 공통 기능이 필요하면 기존 Public API·SPI를 우선 사용하고, 진입점이 없을 때만 실제 Consumer를 근거로 Owner Module에 확장한다.

## 입력·계약

- `collectorEndpoints`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `queueSize`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `retention`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- 해당 기능에 필요한 Operation ID·업무 ID·Correlation ID·요청 Hash·사용자·권한 문맥을 저장·Log·Trace·Audit에서 연결한다.
- 요청·응답·Job Parameter·이벤트·파일 Layout 중 해당되는 계약을 OpenAPI 또는 Machine-readable Schema로 제공한다.

## 상태와 저장 근거

- 필수 상태 흐름: `EXPORTING → DEGRADED → RECOVERED`
- 상태마다 진입 조건, 허용 조치, 재시도 가능 여부, 최종 여부, 저장 위치와 Version 규칙을 정의한다.
- 결과 미확정·부분 성공이 가능한 경우 대상별 Attempt와 상대 상태 조회 근거를 별도 저장한다.

## 정상 처리 순서

1. 입력·권한·데이터 범위·기준 Version과 선행 상태를 검증한다.
2. 상태 변경·비동기 실행인 경우 Operation과 업무 식별자를 생성하거나 기존 멱등 요청을 조회한다. 조회 전용 기능은 조회 Context와 Cursor만 사용한다.
3. 상태 변경 기능은 고객 업무 Owner의 Application Use Case에서 Transaction 경계와 상태 전이를 수행하고, 조회 전용 기능은 일관된 Snapshot·정렬·범위 조건을 적용한다.
4. 외부 시스템·Kafka·파일·Batch·Gateway가 필요한 경우 Adapter 경계에서 Timeout·Retry·보안 정책을 적용한다.
5. 업무 결과와 Attempt·Audit·Log·Metric·Trace를 같은 식별자로 연결한다.
6. 고객 API 또는 운영 화면에서 최종 상태·건수·금액·Version·오류 0건을 확인한다.

## 예외·장애·경계 사례

- Collector down
- disk full
- queue overflow
- high cardinality
- clock skew
- 상태 변경이 있는 경우 DB Commit 전 실패와 Commit 후 응답 유실을 구분한다.
- 같은 요청의 중복·동시 실행·순서 역전·늦은 응답을 포함한다.
- Retry 가능한 오류와 즉시 중단해야 하는 업무 오류를 구분한다.
- 대량·다중 대상 기능에서 부분 성공이 발생하면 성공 대상을 다시 실행하지 않고 실패 대상만 Reprocess·Reconcile한다.
- 복구·보상이 필요한 기능은 복구 실패와 보상 실패도 별도 상태·Attempt·Audit로 남긴다.

## 필수 Test

- OTLP retry
- bounded queue
- drop metric
- rotation
- 입력 Validation·권한·Data Scope·Masking Negative Test
- 해당 기능에 적용되는 중복·동시성·Timeout·응답 유실·부분 실패 Test
- 해당 기능의 Failure Injection 후 필요한 Retry·Restart·Reprocess·Reconcile·Rollback Test
- Oracle·PostgreSQL·MariaDB 또는 해당 Infra의 의미 동일성 Test
- Log·Metric·Trace·Audit에 Secret·개인정보 원문이 없는지 검사

## 운영·화면 확인

- ADM Logs·Capacity·Reliability에서 손실·지연 확인
- 화면에는 해당 메뉴에 실제로 존재하는 조회 조건, 기본값, Column, 상세 Field, 상태, Button 활성 조건, 사유, 승인, 예상 Version, 대상별 결과만 Source와 일치하게 표시한다. 조회 전용 메뉴에 Command 항목을 만들지 않는다.
- 운영자가 정상화 완료를 판단할 수 있는 건수·금액·Version·Checksum·최종 상태·마지막 오류를 제공한다.
- 변경·외부연계 기능에서 응답 유실이 가능한 경우에만 요청을 다시 보내기 전에 Operation·거래·상대 상태를 조회하는 절차를 Browser 또는 API Test로 검증한다.

## 고객 매뉴얼·EDU 연결

- `05_플랫폼운영매뉴얼.md`의 해당 기능 장에 `EDU-OPS-10`를 연결한다.
- 선행 환경, 실행 명령, 입력값, 정상 출력, 오류 재현 명령, 복구 명령, ADM/BZA/Gateway 확인 위치를 그대로 따라 할 수 있게 작성한다.
- 교육 코드에서 고객 업무로 바꿀 Package·Table·API·Topic·파일명과 CPF 관리 영역을 구분한 전환표를 제공한다.

## 완료 조건

- `EDU-OPS-10`가 교육 Catalog·Source·Test·고객 매뉴얼에 동일 ID로 존재한다.
- 정상·Validation·권한·중복·동시성·Timeout·응답 유실·부분 실패·복구 Test가 통과한다.
- 요청·응답·상태·DB·이벤트·파일·운영 화면이 양방향으로 일치한다.
- 운영자가 다른 사람의 설명 없이 장애를 재현하고 정상화 판정을 할 수 있다.
- 실행하지 않은 Test는 성공으로 기록하지 않고, 실패·제약이 남으면 전체 완료로 보고하지 않는다.

## 금지 사항

- 메모리 Map·고정 JSON·정상 Case 한 건만으로 완료 처리
- 교육용 고객 업무를 `cpf-core`, `cpf-starters/common`, Starter, Runtime 제품 내부에 삽입
- Module-local DB Vendor Fallback SQL 또는 Vendor별 의미가 다른 Schema
- Timeout이나 응답 유실 뒤 실제 결과 확인 없이 무조건 재호출
- 비밀번호·Token·개인정보 원문을 Source·Log·Fixture·문서에 기록
- 매뉴얼 설명과 다른 임의 Package·API·상태·메뉴를 새로 생성

---

# 136. EDU-OPS-11 — Backup·Restore·시점 복구·대사

- 우선순위: **P1**
- 연결 매뉴얼: `05_플랫폼운영매뉴얼.md`
- Owner: `cpf-tools/environment·scripts·각 Runtime Config`
- 구현 상태 목표: Source·SQL·API·Config·Test·운영 확인·매뉴얼 연결이 모두 완료

## 필요 근거

- 고객 매뉴얼에서 `Backup·Restore·시점 복구·대사`을 설명하려면 정상 호출 예시만이 아니라 상태 변화와 장애 후 정상화까지 재현 가능한 실행 예제가 필요하다.
- 이 요구사항이 없으면 고객은 기능 가능 여부를 알더라도 실제 업무 적용 순서, 실패 판정, 재처리 금지 조건을 확인할 수 없다.
- 내부 구현 구조를 보여 주기 위한 예제가 아니라 고객 업무를 CPF로 만드는 방법과 운영 결과를 증명하는 예제로 구현한다.

## 고객 업무 시나리오

DB·설정·Artifact를 백업하고 별도 환경에 복원해 업무 합계와 Version을 대사한다.

## 구현 위치와 책임 경계

- `cpf-tools/environment`·`cpf-tools/scripts`의 기존 운영 도구 구조
- 각 Runtime의 기존 `application-*.yml`·Profile·배포 Script 위치
- 운영 검증 Test는 기존 Script Test·Integration Test·Browser Test 체계
- 고객 업무 상태 변경과 원장 데이터는 고객 업무 Owner가 소유한다. ADM·BZA·Gateway·운영 Script가 업무 Table을 직접 수정하지 않는다.
- 공통 기능이 필요하면 기존 Public API·SPI를 우선 사용하고, 진입점이 없을 때만 실제 Consumer를 근거로 Owner Module에 확장한다.

## 입력·계약

- `backupSetId`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `targetTime`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `restoreTarget`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- 해당 기능에 필요한 Operation ID·업무 ID·Correlation ID·요청 Hash·사용자·권한 문맥을 저장·Log·Trace·Audit에서 연결한다.
- 요청·응답·Job Parameter·이벤트·파일 Layout 중 해당되는 계약을 OpenAPI 또는 Machine-readable Schema로 제공한다.

## 상태와 저장 근거

- 필수 상태 흐름: `BACKING_UP → VERIFIED → RESTORING → RECONCILED`
- 상태마다 진입 조건, 허용 조치, 재시도 가능 여부, 최종 여부, 저장 위치와 Version 규칙을 정의한다.
- 결과 미확정·부분 성공이 가능한 경우 대상별 Attempt와 상대 상태 조회 근거를 별도 저장한다.

## 정상 처리 순서

1. 입력·권한·데이터 범위·기준 Version과 선행 상태를 검증한다.
2. 상태 변경·비동기 실행인 경우 Operation과 업무 식별자를 생성하거나 기존 멱등 요청을 조회한다. 조회 전용 기능은 조회 Context와 Cursor만 사용한다.
3. 상태 변경 기능은 고객 업무 Owner의 Application Use Case에서 Transaction 경계와 상태 전이를 수행하고, 조회 전용 기능은 일관된 Snapshot·정렬·범위 조건을 적용한다.
4. 외부 시스템·Kafka·파일·Batch·Gateway가 필요한 경우 Adapter 경계에서 Timeout·Retry·보안 정책을 적용한다.
5. 업무 결과와 Attempt·Audit·Log·Metric·Trace를 같은 식별자로 연결한다.
6. 고객 API 또는 운영 화면에서 최종 상태·건수·금액·Version·오류 0건을 확인한다.

## 예외·장애·경계 사례

- 불완전 backup
- key 누락
- WAL/archive gap
- 다른 app version
- 상태 변경이 있는 경우 DB Commit 전 실패와 Commit 후 응답 유실을 구분한다.
- 같은 요청의 중복·동시 실행·순서 역전·늦은 응답을 포함한다.
- Retry 가능한 오류와 즉시 중단해야 하는 업무 오류를 구분한다.
- 대량·다중 대상 기능에서 부분 성공이 발생하면 성공 대상을 다시 실행하지 않고 실패 대상만 Reprocess·Reconcile한다.
- 복구·보상이 필요한 기능은 복구 실패와 보상 실패도 별도 상태·Attempt·Audit로 남긴다.

## 필수 Test

- Restore drill
- checksum
- control totals
- secret dependency
- 입력 Validation·권한·Data Scope·Masking Negative Test
- 해당 기능에 적용되는 중복·동시성·Timeout·응답 유실·부분 실패 Test
- 해당 기능의 Failure Injection 후 필요한 Retry·Restart·Reprocess·Reconcile·Rollback Test
- Oracle·PostgreSQL·MariaDB 또는 해당 Infra의 의미 동일성 Test
- Log·Metric·Trace·Audit에 Secret·개인정보 원문이 없는지 검사

## 운영·화면 확인

- ADM Recovery/Health와 복원 후 거래·Batch 합계 확인
- 화면에는 해당 메뉴에 실제로 존재하는 조회 조건, 기본값, Column, 상세 Field, 상태, Button 활성 조건, 사유, 승인, 예상 Version, 대상별 결과만 Source와 일치하게 표시한다. 조회 전용 메뉴에 Command 항목을 만들지 않는다.
- 운영자가 정상화 완료를 판단할 수 있는 건수·금액·Version·Checksum·최종 상태·마지막 오류를 제공한다.
- 변경·외부연계 기능에서 응답 유실이 가능한 경우에만 요청을 다시 보내기 전에 Operation·거래·상대 상태를 조회하는 절차를 Browser 또는 API Test로 검증한다.

## 고객 매뉴얼·EDU 연결

- `05_플랫폼운영매뉴얼.md`의 해당 기능 장에 `EDU-OPS-11`를 연결한다.
- 선행 환경, 실행 명령, 입력값, 정상 출력, 오류 재현 명령, 복구 명령, ADM/BZA/Gateway 확인 위치를 그대로 따라 할 수 있게 작성한다.
- 교육 코드에서 고객 업무로 바꿀 Package·Table·API·Topic·파일명과 CPF 관리 영역을 구분한 전환표를 제공한다.

## 완료 조건

- `EDU-OPS-11`가 교육 Catalog·Source·Test·고객 매뉴얼에 동일 ID로 존재한다.
- 정상·Validation·권한·중복·동시성·Timeout·응답 유실·부분 실패·복구 Test가 통과한다.
- 요청·응답·상태·DB·이벤트·파일·운영 화면이 양방향으로 일치한다.
- 운영자가 다른 사람의 설명 없이 장애를 재현하고 정상화 판정을 할 수 있다.
- 실행하지 않은 Test는 성공으로 기록하지 않고, 실패·제약이 남으면 전체 완료로 보고하지 않는다.

## 금지 사항

- 메모리 Map·고정 JSON·정상 Case 한 건만으로 완료 처리
- 교육용 고객 업무를 `cpf-core`, `cpf-starters/common`, Starter, Runtime 제품 내부에 삽입
- Module-local DB Vendor Fallback SQL 또는 Vendor별 의미가 다른 Schema
- Timeout이나 응답 유실 뒤 실제 결과 확인 없이 무조건 재호출
- 비밀번호·Token·개인정보 원문을 Source·Log·Fixture·문서에 기록
- 매뉴얼 설명과 다른 임의 Package·API·상태·메뉴를 새로 생성

---

# 137. EDU-OPS-12 — 재해복구 전환·복귀·Split-Brain 방지

- 우선순위: **P1**
- 연결 매뉴얼: `05_플랫폼운영매뉴얼.md`
- Owner: `cpf-tools/environment·scripts·각 Runtime Config`
- 구현 상태 목표: Source·SQL·API·Config·Test·운영 확인·매뉴얼 연결이 모두 완료

## 필요 근거

- 고객 매뉴얼에서 `재해복구 전환·복귀·Split-Brain 방지`을 설명하려면 정상 호출 예시만이 아니라 상태 변화와 장애 후 정상화까지 재현 가능한 실행 예제가 필요하다.
- 이 요구사항이 없으면 고객은 기능 가능 여부를 알더라도 실제 업무 적용 순서, 실패 판정, 재처리 금지 조건을 확인할 수 없다.
- 내부 구현 구조를 보여 주기 위한 예제가 아니라 고객 업무를 CPF로 만드는 방법과 운영 결과를 증명하는 예제로 구현한다.

## 고객 업무 시나리오

주센터 장애 시 DR로 전환하고 복귀할 때 이중 쓰기를 막고 데이터 차이를 대사한다.

## 구현 위치와 책임 경계

- `cpf-tools/environment`·`cpf-tools/scripts`의 기존 운영 도구 구조
- 각 Runtime의 기존 `application-*.yml`·Profile·배포 Script 위치
- 운영 검증 Test는 기존 Script Test·Integration Test·Browser Test 체계
- 고객 업무 상태 변경과 원장 데이터는 고객 업무 Owner가 소유한다. ADM·BZA·Gateway·운영 Script가 업무 Table을 직접 수정하지 않는다.
- 공통 기능이 필요하면 기존 Public API·SPI를 우선 사용하고, 진입점이 없을 때만 실제 Consumer를 근거로 Owner Module에 확장한다.

## 입력·계약

- `incidentId`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `primary`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `dr`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `fencingEpoch`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- 해당 기능에 필요한 Operation ID·업무 ID·Correlation ID·요청 Hash·사용자·권한 문맥을 저장·Log·Trace·Audit에서 연결한다.
- 요청·응답·Job Parameter·이벤트·파일 Layout 중 해당되는 계약을 OpenAPI 또는 Machine-readable Schema로 제공한다.

## 상태와 저장 근거

- 필수 상태 흐름: `PRIMARY_ACTIVE → FAILING_OVER → DR_ACTIVE → FAILING_BACK`
- 상태마다 진입 조건, 허용 조치, 재시도 가능 여부, 최종 여부, 저장 위치와 Version 규칙을 정의한다.
- 결과 미확정·부분 성공이 가능한 경우 대상별 Attempt와 상대 상태 조회 근거를 별도 저장한다.

## 정상 처리 순서

1. 입력·권한·데이터 범위·기준 Version과 선행 상태를 검증한다.
2. 상태 변경·비동기 실행인 경우 Operation과 업무 식별자를 생성하거나 기존 멱등 요청을 조회한다. 조회 전용 기능은 조회 Context와 Cursor만 사용한다.
3. 상태 변경 기능은 고객 업무 Owner의 Application Use Case에서 Transaction 경계와 상태 전이를 수행하고, 조회 전용 기능은 일관된 Snapshot·정렬·범위 조건을 적용한다.
4. 외부 시스템·Kafka·파일·Batch·Gateway가 필요한 경우 Adapter 경계에서 Timeout·Retry·보안 정책을 적용한다.
5. 업무 결과와 Attempt·Audit·Log·Metric·Trace를 같은 식별자로 연결한다.
6. 고객 API 또는 운영 화면에서 최종 상태·건수·금액·Version·오류 0건을 확인한다.

## 예외·장애·경계 사례

- Network partition
- DNS cache
- lag
- dual scheduler
- stale lease
- 상태 변경이 있는 경우 DB Commit 전 실패와 Commit 후 응답 유실을 구분한다.
- 같은 요청의 중복·동시 실행·순서 역전·늦은 응답을 포함한다.
- Retry 가능한 오류와 즉시 중단해야 하는 업무 오류를 구분한다.
- 대량·다중 대상 기능에서 부분 성공이 발생하면 성공 대상을 다시 실행하지 않고 실패 대상만 Reprocess·Reconcile한다.
- 복구·보상이 필요한 기능은 복구 실패와 보상 실패도 별도 상태·Attempt·Audit로 남긴다.

## 필수 Test

- Fencing epoch
- RPO/RTO measurement
- failback reconcile
- 입력 Validation·권한·Data Scope·Masking Negative Test
- 해당 기능에 적용되는 중복·동시성·Timeout·응답 유실·부분 실패 Test
- 해당 기능의 Failure Injection 후 필요한 Retry·Restart·Reprocess·Reconcile·Rollback Test
- Oracle·PostgreSQL·MariaDB 또는 해당 Infra의 의미 동일성 Test
- Log·Metric·Trace·Audit에 Secret·개인정보 원문이 없는지 검사

## 운영·화면 확인

- ADM Topology·Reliability·Incidents에서 전환 증적 확인
- 화면에는 해당 메뉴에 실제로 존재하는 조회 조건, 기본값, Column, 상세 Field, 상태, Button 활성 조건, 사유, 승인, 예상 Version, 대상별 결과만 Source와 일치하게 표시한다. 조회 전용 메뉴에 Command 항목을 만들지 않는다.
- 운영자가 정상화 완료를 판단할 수 있는 건수·금액·Version·Checksum·최종 상태·마지막 오류를 제공한다.
- 변경·외부연계 기능에서 응답 유실이 가능한 경우에만 요청을 다시 보내기 전에 Operation·거래·상대 상태를 조회하는 절차를 Browser 또는 API Test로 검증한다.

## 고객 매뉴얼·EDU 연결

- `05_플랫폼운영매뉴얼.md`의 해당 기능 장에 `EDU-OPS-12`를 연결한다.
- 선행 환경, 실행 명령, 입력값, 정상 출력, 오류 재현 명령, 복구 명령, ADM/BZA/Gateway 확인 위치를 그대로 따라 할 수 있게 작성한다.
- 교육 코드에서 고객 업무로 바꿀 Package·Table·API·Topic·파일명과 CPF 관리 영역을 구분한 전환표를 제공한다.

## 완료 조건

- `EDU-OPS-12`가 교육 Catalog·Source·Test·고객 매뉴얼에 동일 ID로 존재한다.
- 정상·Validation·권한·중복·동시성·Timeout·응답 유실·부분 실패·복구 Test가 통과한다.
- 요청·응답·상태·DB·이벤트·파일·운영 화면이 양방향으로 일치한다.
- 운영자가 다른 사람의 설명 없이 장애를 재현하고 정상화 판정을 할 수 있다.
- 실행하지 않은 Test는 성공으로 기록하지 않고, 실패·제약이 남으면 전체 완료로 보고하지 않는다.

## 금지 사항

- 메모리 Map·고정 JSON·정상 Case 한 건만으로 완료 처리
- 교육용 고객 업무를 `cpf-core`, `cpf-starters/common`, Starter, Runtime 제품 내부에 삽입
- Module-local DB Vendor Fallback SQL 또는 Vendor별 의미가 다른 Schema
- Timeout이나 응답 유실 뒤 실제 결과 확인 없이 무조건 재호출
- 비밀번호·Token·개인정보 원문을 Source·Log·Fixture·문서에 기록
- 매뉴얼 설명과 다른 임의 Package·API·상태·메뉴를 새로 생성

---

# 138. EDU-OPS-13 — Disk·Memory·Network·DB 장애 Runbook

- 우선순위: **P1**
- 연결 매뉴얼: `05_플랫폼운영매뉴얼.md`
- Owner: `cpf-tools/environment·scripts·각 Runtime Config`
- 구현 상태 목표: Source·SQL·API·Config·Test·운영 확인·매뉴얼 연결이 모두 완료

## 필요 근거

- 고객 매뉴얼에서 `Disk·Memory·Network·DB 장애 Runbook`을 설명하려면 정상 호출 예시만이 아니라 상태 변화와 장애 후 정상화까지 재현 가능한 실행 예제가 필요하다.
- 이 요구사항이 없으면 고객은 기능 가능 여부를 알더라도 실제 업무 적용 순서, 실패 판정, 재처리 금지 조건을 확인할 수 없다.
- 내부 구현 구조를 보여 주기 위한 예제가 아니라 고객 업무를 CPF로 만드는 방법과 운영 결과를 증명하는 예제로 구현한다.

## 고객 업무 시나리오

Disk 95%, OOM, Network 지연, DB connection exhaustion을 재현하고 단계별로 복구한다.

## 구현 위치와 책임 경계

- `cpf-tools/environment`·`cpf-tools/scripts`의 기존 운영 도구 구조
- 각 Runtime의 기존 `application-*.yml`·Profile·배포 Script 위치
- 운영 검증 Test는 기존 Script Test·Integration Test·Browser Test 체계
- 고객 업무 상태 변경과 원장 데이터는 고객 업무 Owner가 소유한다. ADM·BZA·Gateway·운영 Script가 업무 Table을 직접 수정하지 않는다.
- 공통 기능이 필요하면 기존 Public API·SPI를 우선 사용하고, 진입점이 없을 때만 실제 Consumer를 근거로 Owner Module에 확장한다.

## 입력·계약

- `faultType`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `target`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `duration`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `threshold`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- 해당 기능에 필요한 Operation ID·업무 ID·Correlation ID·요청 Hash·사용자·권한 문맥을 저장·Log·Trace·Audit에서 연결한다.
- 요청·응답·Job Parameter·이벤트·파일 Layout 중 해당되는 계약을 OpenAPI 또는 Machine-readable Schema로 제공한다.

## 상태와 저장 근거

- 필수 상태 흐름: `NORMAL → DEGRADED → RECOVERING → NORMAL`
- 상태마다 진입 조건, 허용 조치, 재시도 가능 여부, 최종 여부, 저장 위치와 Version 규칙을 정의한다.
- 결과 미확정·부분 성공이 가능한 경우 대상별 Attempt와 상대 상태 조회 근거를 별도 저장한다.

## 정상 처리 순서

1. 입력·권한·데이터 범위·기준 Version과 선행 상태를 검증한다.
2. 상태 변경·비동기 실행인 경우 Operation과 업무 식별자를 생성하거나 기존 멱등 요청을 조회한다. 조회 전용 기능은 조회 Context와 Cursor만 사용한다.
3. 상태 변경 기능은 고객 업무 Owner의 Application Use Case에서 Transaction 경계와 상태 전이를 수행하고, 조회 전용 기능은 일관된 Snapshot·정렬·범위 조건을 적용한다.
4. 외부 시스템·Kafka·파일·Batch·Gateway가 필요한 경우 Adapter 경계에서 Timeout·Retry·보안 정책을 적용한다.
5. 업무 결과와 Attempt·Audit·Log·Metric·Trace를 같은 식별자로 연결한다.
6. 고객 API 또는 운영 화면에서 최종 상태·건수·금액·Version·오류 0건을 확인한다.

## 예외·장애·경계 사례

- 동시 장애
- 자동 재시도 폭주
- alert storm
- 복구 후 backlog
- 상태 변경이 있는 경우 DB Commit 전 실패와 Commit 후 응답 유실을 구분한다.
- 같은 요청의 중복·동시 실행·순서 역전·늦은 응답을 포함한다.
- Retry 가능한 오류와 즉시 중단해야 하는 업무 오류를 구분한다.
- 대량·다중 대상 기능에서 부분 성공이 발생하면 성공 대상을 다시 실행하지 않고 실패 대상만 Reprocess·Reconcile한다.
- 복구·보상이 필요한 기능은 복구 실패와 보상 실패도 별도 상태·Attempt·Audit로 남긴다.

## 필수 Test

- Toxiproxy/resource limits
- runbook commands
- post-check
- 입력 Validation·권한·Data Scope·Masking Negative Test
- 해당 기능에 적용되는 중복·동시성·Timeout·응답 유실·부분 실패 Test
- 해당 기능의 Failure Injection 후 필요한 Retry·Restart·Reprocess·Reconcile·Rollback Test
- Oracle·PostgreSQL·MariaDB 또는 해당 Infra의 의미 동일성 Test
- Log·Metric·Trace·Audit에 Secret·개인정보 원문이 없는지 검사

## 운영·화면 확인

- ADM Capacity·Health·Incidents·Recovery Center E2E
- 화면에는 해당 메뉴에 실제로 존재하는 조회 조건, 기본값, Column, 상세 Field, 상태, Button 활성 조건, 사유, 승인, 예상 Version, 대상별 결과만 Source와 일치하게 표시한다. 조회 전용 메뉴에 Command 항목을 만들지 않는다.
- 운영자가 정상화 완료를 판단할 수 있는 건수·금액·Version·Checksum·최종 상태·마지막 오류를 제공한다.
- 변경·외부연계 기능에서 응답 유실이 가능한 경우에만 요청을 다시 보내기 전에 Operation·거래·상대 상태를 조회하는 절차를 Browser 또는 API Test로 검증한다.

## 고객 매뉴얼·EDU 연결

- `05_플랫폼운영매뉴얼.md`의 해당 기능 장에 `EDU-OPS-13`를 연결한다.
- 선행 환경, 실행 명령, 입력값, 정상 출력, 오류 재현 명령, 복구 명령, ADM/BZA/Gateway 확인 위치를 그대로 따라 할 수 있게 작성한다.
- 교육 코드에서 고객 업무로 바꿀 Package·Table·API·Topic·파일명과 CPF 관리 영역을 구분한 전환표를 제공한다.

## 완료 조건

- `EDU-OPS-13`가 교육 Catalog·Source·Test·고객 매뉴얼에 동일 ID로 존재한다.
- 정상·Validation·권한·중복·동시성·Timeout·응답 유실·부분 실패·복구 Test가 통과한다.
- 요청·응답·상태·DB·이벤트·파일·운영 화면이 양방향으로 일치한다.
- 운영자가 다른 사람의 설명 없이 장애를 재현하고 정상화 판정을 할 수 있다.
- 실행하지 않은 Test는 성공으로 기록하지 않고, 실패·제약이 남으면 전체 완료로 보고하지 않는다.

## 금지 사항

- 메모리 Map·고정 JSON·정상 Case 한 건만으로 완료 처리
- 교육용 고객 업무를 `cpf-core`, `cpf-starters/common`, Starter, Runtime 제품 내부에 삽입
- Module-local DB Vendor Fallback SQL 또는 Vendor별 의미가 다른 Schema
- Timeout이나 응답 유실 뒤 실제 결과 확인 없이 무조건 재호출
- 비밀번호·Token·개인정보 원문을 Source·Log·Fixture·문서에 기록
- 매뉴얼 설명과 다른 임의 Package·API·상태·메뉴를 새로 생성

---

# 139. EDU-OPS-14 — 보안 사고·계정·키·세션 긴급 차단

- 우선순위: **P1**
- 연결 매뉴얼: `05_플랫폼운영매뉴얼.md`
- Owner: `cpf-tools/environment·scripts·각 Runtime Config`
- 구현 상태 목표: Source·SQL·API·Config·Test·운영 확인·매뉴얼 연결이 모두 완료

## 필요 근거

- 고객 매뉴얼에서 `보안 사고·계정·키·세션 긴급 차단`을 설명하려면 정상 호출 예시만이 아니라 상태 변화와 장애 후 정상화까지 재현 가능한 실행 예제가 필요하다.
- 이 요구사항이 없으면 고객은 기능 가능 여부를 알더라도 실제 업무 적용 순서, 실패 판정, 재처리 금지 조건을 확인할 수 없다.
- 내부 구현 구조를 보여 주기 위한 예제가 아니라 고객 업무를 CPF로 만드는 방법과 운영 결과를 증명하는 예제로 구현한다.

## 고객 업무 시나리오

Token 유출 의심 시 계정·세션·Key·Gateway Route를 차단하고 영향 범위를 조사한다.

## 구현 위치와 책임 경계

- `cpf-tools/environment`·`cpf-tools/scripts`의 기존 운영 도구 구조
- 각 Runtime의 기존 `application-*.yml`·Profile·배포 Script 위치
- 운영 검증 Test는 기존 Script Test·Integration Test·Browser Test 체계
- 고객 업무 상태 변경과 원장 데이터는 고객 업무 Owner가 소유한다. ADM·BZA·Gateway·운영 Script가 업무 Table을 직접 수정하지 않는다.
- 공통 기능이 필요하면 기존 Public API·SPI를 우선 사용하고, 진입점이 없을 때만 실제 Consumer를 근거로 Owner Module에 확장한다.

## 입력·계약

- `incidentId`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `subjects`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `secretVersions`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `routes`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- 해당 기능에 필요한 Operation ID·업무 ID·Correlation ID·요청 Hash·사용자·권한 문맥을 저장·Log·Trace·Audit에서 연결한다.
- 요청·응답·Job Parameter·이벤트·파일 Layout 중 해당되는 계약을 OpenAPI 또는 Machine-readable Schema로 제공한다.

## 상태와 저장 근거

- 필수 상태 흐름: `DETECTED → CONTAINED → ROTATING → RECOVERED`
- 상태마다 진입 조건, 허용 조치, 재시도 가능 여부, 최종 여부, 저장 위치와 Version 규칙을 정의한다.
- 결과 미확정·부분 성공이 가능한 경우 대상별 Attempt와 상대 상태 조회 근거를 별도 저장한다.

## 정상 처리 순서

1. 입력·권한·데이터 범위·기준 Version과 선행 상태를 검증한다.
2. 상태 변경·비동기 실행인 경우 Operation과 업무 식별자를 생성하거나 기존 멱등 요청을 조회한다. 조회 전용 기능은 조회 Context와 Cursor만 사용한다.
3. 상태 변경 기능은 고객 업무 Owner의 Application Use Case에서 Transaction 경계와 상태 전이를 수행하고, 조회 전용 기능은 일관된 Snapshot·정렬·범위 조건을 적용한다.
4. 외부 시스템·Kafka·파일·Batch·Gateway가 필요한 경우 Adapter 경계에서 Timeout·Retry·보안 정책을 적용한다.
5. 업무 결과와 Attempt·Audit·Log·Metric·Trace를 같은 식별자로 연결한다.
6. 고객 API 또는 운영 화면에서 최종 상태·건수·금액·Version·오류 0건을 확인한다.

## 예외·장애·경계 사례

- 오탐 해제
- 일부 instance 구키
- audit 누락
- break-glass
- 상태 변경이 있는 경우 DB Commit 전 실패와 Commit 후 응답 유실을 구분한다.
- 같은 요청의 중복·동시 실행·순서 역전·늦은 응답을 포함한다.
- Retry 가능한 오류와 즉시 중단해야 하는 업무 오류를 구분한다.
- 대량·다중 대상 기능에서 부분 성공이 발생하면 성공 대상을 다시 실행하지 않고 실패 대상만 Reprocess·Reconcile한다.
- 복구·보상이 필요한 기능은 복구 실패와 보상 실패도 별도 상태·Attempt·Audit로 남긴다.

## 필수 Test

- Emergency revoke
- rotation
- session kill
- evidence export
- 입력 Validation·권한·Data Scope·Masking Negative Test
- 해당 기능에 적용되는 중복·동시성·Timeout·응답 유실·부분 실패 Test
- 해당 기능의 Failure Injection 후 필요한 Retry·Restart·Reprocess·Reconcile·Rollback Test
- Oracle·PostgreSQL·MariaDB 또는 해당 Infra의 의미 동일성 Test
- Log·Metric·Trace·Audit에 Secret·개인정보 원문이 없는지 검사

## 운영·화면 확인

- BZA Sessions·ADM Security·Gateway Apply Status 연결
- 화면에는 해당 메뉴에 실제로 존재하는 조회 조건, 기본값, Column, 상세 Field, 상태, Button 활성 조건, 사유, 승인, 예상 Version, 대상별 결과만 Source와 일치하게 표시한다. 조회 전용 메뉴에 Command 항목을 만들지 않는다.
- 운영자가 정상화 완료를 판단할 수 있는 건수·금액·Version·Checksum·최종 상태·마지막 오류를 제공한다.
- 변경·외부연계 기능에서 응답 유실이 가능한 경우에만 요청을 다시 보내기 전에 Operation·거래·상대 상태를 조회하는 절차를 Browser 또는 API Test로 검증한다.

## 고객 매뉴얼·EDU 연결

- `05_플랫폼운영매뉴얼.md`의 해당 기능 장에 `EDU-OPS-14`를 연결한다.
- 선행 환경, 실행 명령, 입력값, 정상 출력, 오류 재현 명령, 복구 명령, ADM/BZA/Gateway 확인 위치를 그대로 따라 할 수 있게 작성한다.
- 교육 코드에서 고객 업무로 바꿀 Package·Table·API·Topic·파일명과 CPF 관리 영역을 구분한 전환표를 제공한다.

## 완료 조건

- `EDU-OPS-14`가 교육 Catalog·Source·Test·고객 매뉴얼에 동일 ID로 존재한다.
- 정상·Validation·권한·중복·동시성·Timeout·응답 유실·부분 실패·복구 Test가 통과한다.
- 요청·응답·상태·DB·이벤트·파일·운영 화면이 양방향으로 일치한다.
- 운영자가 다른 사람의 설명 없이 장애를 재현하고 정상화 판정을 할 수 있다.
- 실행하지 않은 Test는 성공으로 기록하지 않고, 실패·제약이 남으면 전체 완료로 보고하지 않는다.

## 금지 사항

- 메모리 Map·고정 JSON·정상 Case 한 건만으로 완료 처리
- 교육용 고객 업무를 `cpf-core`, `cpf-starters/common`, Starter, Runtime 제품 내부에 삽입
- Module-local DB Vendor Fallback SQL 또는 Vendor별 의미가 다른 Schema
- Timeout이나 응답 유실 뒤 실제 결과 확인 없이 무조건 재호출
- 비밀번호·Token·개인정보 원문을 Source·Log·Fixture·문서에 기록
- 매뉴얼 설명과 다른 임의 Package·API·상태·메뉴를 새로 생성

---

# 140. EDU-OPS-15 — Version Upgrade·DB 호환·Application Rollback

- 우선순위: **P1**
- 연결 매뉴얼: `05_플랫폼운영매뉴얼.md`
- Owner: `cpf-tools/environment·scripts·각 Runtime Config`
- 구현 상태 목표: Source·SQL·API·Config·Test·운영 확인·매뉴얼 연결이 모두 완료

## 필요 근거

- 고객 매뉴얼에서 `Version Upgrade·DB 호환·Application Rollback`을 설명하려면 정상 호출 예시만이 아니라 상태 변화와 장애 후 정상화까지 재현 가능한 실행 예제가 필요하다.
- 이 요구사항이 없으면 고객은 기능 가능 여부를 알더라도 실제 업무 적용 순서, 실패 판정, 재처리 금지 조건을 확인할 수 없다.
- 내부 구현 구조를 보여 주기 위한 예제가 아니라 고객 업무를 CPF로 만드는 방법과 운영 결과를 증명하는 예제로 구현한다.

## 고객 업무 시나리오

플랫폼 Version을 올리고 DB가 확장/수축 Migration을 거쳐 이전 Application으로 되돌릴 수 있는지 검증한다.

## 구현 위치와 책임 경계

- `cpf-tools/environment`·`cpf-tools/scripts`의 기존 운영 도구 구조
- 각 Runtime의 기존 `application-*.yml`·Profile·배포 Script 위치
- 운영 검증 Test는 기존 Script Test·Integration Test·Browser Test 체계
- 고객 업무 상태 변경과 원장 데이터는 고객 업무 Owner가 소유한다. ADM·BZA·Gateway·운영 Script가 업무 Table을 직접 수정하지 않는다.
- 공통 기능이 필요하면 기존 Public API·SPI를 우선 사용하고, 진입점이 없을 때만 실제 Consumer를 근거로 Owner Module에 확장한다.

## 입력·계약

- `fromVersion`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `toVersion`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- `compatibilityWindow`의 형식·필수 여부·기본값·허용 범위·오류코드를 계약으로 고정한다.
- 해당 기능에 필요한 Operation ID·업무 ID·Correlation ID·요청 Hash·사용자·권한 문맥을 저장·Log·Trace·Audit에서 연결한다.
- 요청·응답·Job Parameter·이벤트·파일 Layout 중 해당되는 계약을 OpenAPI 또는 Machine-readable Schema로 제공한다.

## 상태와 저장 근거

- 필수 상태 흐름: `PRECHECK → EXPAND → DEPLOY → CONTRACT/ROLLBACK`
- 상태마다 진입 조건, 허용 조치, 재시도 가능 여부, 최종 여부, 저장 위치와 Version 규칙을 정의한다.
- 결과 미확정·부분 성공이 가능한 경우 대상별 Attempt와 상대 상태 조회 근거를 별도 저장한다.

## 정상 처리 순서

1. 입력·권한·데이터 범위·기준 Version과 선행 상태를 검증한다.
2. 상태 변경·비동기 실행인 경우 Operation과 업무 식별자를 생성하거나 기존 멱등 요청을 조회한다. 조회 전용 기능은 조회 Context와 Cursor만 사용한다.
3. 상태 변경 기능은 고객 업무 Owner의 Application Use Case에서 Transaction 경계와 상태 전이를 수행하고, 조회 전용 기능은 일관된 Snapshot·정렬·범위 조건을 적용한다.
4. 외부 시스템·Kafka·파일·Batch·Gateway가 필요한 경우 Adapter 경계에서 Timeout·Retry·보안 정책을 적용한다.
5. 업무 결과와 Attempt·Audit·Log·Metric·Trace를 같은 식별자로 연결한다.
6. 고객 API 또는 운영 화면에서 최종 상태·건수·금액·Version·오류 0건을 확인한다.

## 예외·장애·경계 사례

- 구 App와 새 Schema
- data conversion
- mixed version
- rollback after write
- 상태 변경이 있는 경우 DB Commit 전 실패와 Commit 후 응답 유실을 구분한다.
- 같은 요청의 중복·동시 실행·순서 역전·늦은 응답을 포함한다.
- Retry 가능한 오류와 즉시 중단해야 하는 업무 오류를 구분한다.
- 대량·다중 대상 기능에서 부분 성공이 발생하면 성공 대상을 다시 실행하지 않고 실패 대상만 Reprocess·Reconcile한다.
- 복구·보상이 필요한 기능은 복구 실패와 보상 실패도 별도 상태·Attempt·Audit로 남긴다.

## 필수 Test

- N/N-1 compatibility
- expand-contract
- rollback rehearsal
- 입력 Validation·권한·Data Scope·Masking Negative Test
- 해당 기능에 적용되는 중복·동시성·Timeout·응답 유실·부분 실패 Test
- 해당 기능의 Failure Injection 후 필요한 Retry·Restart·Reprocess·Reconcile·Rollback Test
- Oracle·PostgreSQL·MariaDB 또는 해당 Infra의 의미 동일성 Test
- Log·Metric·Trace·Audit에 Secret·개인정보 원문이 없는지 검사

## 운영·화면 확인

- ADM Topology·Configs·DB Version과 실제 Artifact 대조
- 화면에는 해당 메뉴에 실제로 존재하는 조회 조건, 기본값, Column, 상세 Field, 상태, Button 활성 조건, 사유, 승인, 예상 Version, 대상별 결과만 Source와 일치하게 표시한다. 조회 전용 메뉴에 Command 항목을 만들지 않는다.
- 운영자가 정상화 완료를 판단할 수 있는 건수·금액·Version·Checksum·최종 상태·마지막 오류를 제공한다.
- 변경·외부연계 기능에서 응답 유실이 가능한 경우에만 요청을 다시 보내기 전에 Operation·거래·상대 상태를 조회하는 절차를 Browser 또는 API Test로 검증한다.

## 고객 매뉴얼·EDU 연결

- `05_플랫폼운영매뉴얼.md`의 해당 기능 장에 `EDU-OPS-15`를 연결한다.
- 선행 환경, 실행 명령, 입력값, 정상 출력, 오류 재현 명령, 복구 명령, ADM/BZA/Gateway 확인 위치를 그대로 따라 할 수 있게 작성한다.
- 교육 코드에서 고객 업무로 바꿀 Package·Table·API·Topic·파일명과 CPF 관리 영역을 구분한 전환표를 제공한다.

## 완료 조건

- `EDU-OPS-15`가 교육 Catalog·Source·Test·고객 매뉴얼에 동일 ID로 존재한다.
- 정상·Validation·권한·중복·동시성·Timeout·응답 유실·부분 실패·복구 Test가 통과한다.
- 요청·응답·상태·DB·이벤트·파일·운영 화면이 양방향으로 일치한다.
- 운영자가 다른 사람의 설명 없이 장애를 재현하고 정상화 판정을 할 수 있다.
- 실행하지 않은 Test는 성공으로 기록하지 않고, 실패·제약이 남으면 전체 완료로 보고하지 않는다.

## 금지 사항

- 메모리 Map·고정 JSON·정상 Case 한 건만으로 완료 처리
- 교육용 고객 업무를 `cpf-core`, `cpf-starters/common`, Starter, Runtime 제품 내부에 삽입
- Module-local DB Vendor Fallback SQL 또는 Vendor별 의미가 다른 Schema
- Timeout이나 응답 유실 뒤 실제 결과 확인 없이 무조건 재호출
- 비밀번호·Token·개인정보 원문을 Source·Log·Fixture·문서에 기록
- 매뉴얼 설명과 다른 임의 Package·API·상태·메뉴를 새로 생성

---

# 141. 공통 통합 검증 Gate

## 141.1 Source·문서 양방향 일치

- 본문 135개와 정본 대조에서 같은 작업에 추가된 모든 교육 ID가 Catalog, Source, Test, 매뉴얼에 모두 존재한다.
- Catalog의 실행 명령이 실제 Test Class와 일치한다.
- 매뉴얼 요청·응답 예시가 실제 OpenAPI와 일치한다.
- ADM·BZA·Gateway 메뉴의 Field·Button·상태가 실제 화면과 일치한다.

## 141.2 자동 실행 묶음

최소 다음 실행 단위를 제공한다.

```text
온라인·연계 P0
온라인·연계 전체
플랫폼 운영 전체
배치 P0
배치 전체
ADM 연동
BZA 통합
Gateway 통합
전체 EDU Smoke
전체 EDU Failure·Recovery
```

각 묶음은 실패한 교육 ID를 명확히 출력하고 다른 ID의 결과를 덮어쓰지 않는다.

## 141.3 품질 검사

- Java Compile·Unit·Integration Test
- Frontend lint·typecheck·unit·browser test
- Oracle·PostgreSQL·MariaDB Vendor Pack Test
- Kafka·Redis·Toxiproxy 연계 Test
- OpenAPI·Generated Client 정합성
- Link·Anchor·교육 ID 검사
- Secret·개인정보 Fixture 검사
- UTF-8·LF·Markdown·JSON·YAML 검사
- `git diff --check`

## 141.4 고객 업무 전환 검사

각 교육 예제는 다음 표를 제공한다.

| 구분 | 고객이 변경 | CPF가 관리 |
|---|---|---|
| 업무명·상태 | 고객 업무명·상태표 | 표준 오류·Operation 의미 |
| 데이터 | 고객 Table·Query·Migration | Vendor Pack 실행 구조 |
| API | 고객 URI·DTO·Permission | 공통 Header·Trace·Error 계약 |
| 연계 | 고객 Topic·기관·파일 Layout | Outbox·Inbox·Attempt·Reconcile 방식 |
| 운영 | 고객 메뉴 Field·Runbook | ADM/BZA/Gateway 공통 통제 계약 |

# 142. 개발 결과 보고서 필수 항목

개발 GPT는 완료 보고서에 다음을 포함한다.

1. 기준 Repository·Branch·Commit
2. 작업 전 Git 상태와 보호한 변경
3. 요청 ID별 완료 상태
4. 신규·수정·삭제 파일 목록
5. Package·Module 배치 근거
6. API·SQL·Config·Route 변경
7. 실행한 Test 명령과 실제 결과
8. 장애 주입·복구 결과
9. ADM·BZA·Gateway Browser 확인 결과
10. 미실행·미검증 항목
11. Root Overlay ZIP 링크·SHA-256·파일 수
12. Rollback과 정확한 삭제 Manifest
13. Commit·Push 미수행 여부

# 143. 작업 종료 전 안전 검사

```text
git status --short
git diff --name-status
git diff --stat
git diff --check
git ls-files --others --exclude-standard
```

다음 항목이 있으면 완료 처리하지 않는다.

- 교육 코드가 Runtime 내부에 혼재
- Module-local DB Vendor Fallback SQL
- README 또는 공식 Guide 이외의 중복 고객 문서
- 임시 Script·Patch·Log·Build 산출물
- Secret·Token·개인정보
- Source·SQL·API·Test·매뉴얼 교육 ID 불일치
- 조회 전용 화면에 Command·Approval·Rollback 절차 혼입
- 직접 실행하지 않은 Test의 성공 기록

# 144. 단일 통합 작업 완료 Gate

## 144.1 요구사항 Coverage Matrix

최종 결과에는 최소 다음 열의 Matrix를 제공한다.

| 정본 요구사항 ID | 고객 기능 | 대응 매뉴얼·장 | EDU ID | 구현 Source | SQL·Config | 자동 Test | 장애·복구 Test | 운영 확인 | 상태 |
|---|---|---|---|---|---|---|---|---|---|

- Canonical Requirement와 최신 개발 요건·Gap 항목을 한 행씩 대응한다.
- 하나의 EDU가 여러 요구사항을 담당하면 각 요구사항 행에 같은 EDU ID를 명시한다.
- 하나의 요구사항이 서로 다른 장애·복구 모델을 가지면 EDU를 분리한다.
- `EDU 없음`, `Test 없음`, `운영 확인 없음`, `매뉴얼 연결 없음`인 행이 남으면 추가 ID를 만들고 같은 작업에서 구현한다.

## 144.2 완료 판정 조건

다음 조건을 모두 만족해야 단일 통합 작업을 완료로 보고한다.

1. 본문 135개 EDU가 모두 구현·검증됐다.
2. 전수 대조에서 발견된 추가 EDU가 모두 같은 작업에서 구현·검증됐다.
3. Canonical Requirement·개발 Gap·실제 기능·고객 매뉴얼의 미대응 행이 0건이다.
4. 정상 흐름만 있고 장애·복구 Test가 없는 EDU가 0건이다.
5. 교육 Catalog의 실행 명령과 실제 Test 경로 불일치가 0건이다.
6. DB Vendor별 의미 차이, Local·Remote 차이, 단일·다중 인스턴스 차이가 설명 없이 남은 항목이 0건이다.
7. ADM·BZA·Gateway에서 고객이 상태를 확인하거나 복구할 경로가 없는 항목이 0건이다.
8. Source·SQL·API·Config·Frontend·Script·Test·고객 매뉴얼 간 교육 ID 불일치가 0건이다.
9. Root Overlay ZIP 하나, 삭제 Manifest, Coverage Matrix, 검증 결과, Rollback이 제공됐다.
10. Commit·Push·Branch·Tag·PR을 수행하지 않았다.

## 144.3 금지되는 종료 방식

- 본문 135개만 구현하고 “추가 Gap은 다음 요청”으로 종료
- Gap 목록만 작성하고 구현하지 않음
- 기능군별로 요청서를 다시 나눠 달라고 요구
- P0만 구현하고 P1·P2를 다음 차수로 이관
- Test 실패를 문서 보완 대상으로 돌림
- 예제가 없다는 이유로 고객 매뉴얼 기능을 삭제하거나 축소
- 일정이나 분량을 이유로 여러 ZIP을 순차 제공

이 요청서는 CPF 완성 단계에서 사용하는 단일 통합 개발 요구사항이다. 작업 중 발견한 추가 범위도 이 요청의 일부다.


## Mandatory Transaction / Call / Result / Log Developer Manual Chapter

Customer Manual/EDU는 반드시 `CPF_TRANSACTION_CALL_COMMON_FUNCTION_DEVELOPER_MANUAL.md`를 Source로 currentize한다.

필수 내용:
- 전체 거래 호출관계도.
- Controller/Service/Repository/Domain/External/Messaging/Async/Batch 호출 관계.
- Local/Remote Domain 동일 Source.
- Function/Command 전체 표.
- 각 함수의 사용시점, 입력, 옵션, 반환 자료형.
- Transaction 영향.
- TxId/ExecutionId/SegmentId.
- 자동 로그 필드.
- SUCCESS/BUSINESS_FAILURE/TECHNICAL_FAILURE/UNKNOWN.
- retry/reconcile/compensation.
- 되는 방식/안 되는 방식.
- DTO/List/Page/Cursor/Map/scalar/boolean/count/Void/Ack/Async/Stream 예제.

Manual은 개발 완료 후 실제 Public Source 이름/Config/Runtime Evidence와 1:1 검산한다.
