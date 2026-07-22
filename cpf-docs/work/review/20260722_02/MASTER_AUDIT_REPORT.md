# 최신 master 전수검수 보고서

## 1. Executive Verdict

최신 `master` `a63380e6c736fa9c5ae7e425d0e301d21ef3b848`은 공식 `cpf-*` Module과 `com.cpf.*` Package로 대규모 전환한 **중간 WIP 기준점**이다. 구조 전환 자체는 상당히 진행됐으나, 마지막 광범위한 명칭·DB 변경 이후의 전체 Runtime·DB·Browser·배포 검증이 완료되지 않았다.

현재 전체 판정:

| 구분 | 판정 |
|---|---|
| 구조 전환 구현량 | 부분 구현 |
| 최종 제품 완료 | 부분 구현 |
| 최신 Commit 전체 빌드 | 미검증 |
| 최신 Commit 빈 DB 설치 | 미검증 |
| 최신 Commit Runtime·Browser | 미검증 |
| 문서·Evidence 정본성 | 부분 구현 |
| 다음 작업 착수 가능 여부 | 가능. 단, 최신 요청서로 재정렬 필요 |

제품 완료 관점의 추정 진척도는 **약 55% ± 5%**다. Source와 자동화 도구의 양은 더 많이 진행됐지만, CPF 완료 기준은 DB·Runtime·복구·운영·보안·Browser·배포·Guide와 Evidence까지 포함하기 때문에 구현 파일 수만으로 70~80%를 주장할 수 없다.

## 2. 최신 Push 확인

- `7251bd996a99ec61d9ea83559578ead0047d5f47`: 공식 Module·Package Rename 중심 WIP
- `a63380e6c736fa9c5ae7e425d0e301d21ef3b848`: Rename 이후 Source·SQL·Script·Guide를 대규모 수정한 WIP
- 최신 Commit 통계: 추가 약 26,592라인, 삭제 약 614,338라인

두 Commit 모두 `WIP: checkpoint CPF module and database standardization`으로 Push된 것을 확인했다.

### 판정

- 두 차례 Push 존재: **완료**
- Push 결과가 최종 기능 완료본인지: **아님**
- 대규모 Rename 이후 회귀 검증: **미검증**

## 3. 잘된 변경과 보존 대상

다음은 폐기하지 않고 목표 구조에서 보존·검증할 가치가 높다.

- 공식 Module 디렉터리: `cpf-core`, `cpf-common`, `cpf-admin`, `cpf-biz-admin`, `cpf-batch`, `cpf-gateway`, `cpf-member`, `cpf-account`, `cpf-reference`, `cpf-external`
- Root Project `cpf-core-platform-framework`
- 공식 Package Root `com.cpf.*`
- `cpf-core` SystemCode `CPF`
- `cpf-account` Generator Lifecycle 자산
- `cpf-tools/generator`
- Generator Capability·DB Vendor Matrix
- `cpf-external` 신규 Module 기반
- BAT Worker Lease·Fencing 관련 구현 후보
- Vue 3·TypeScript·Vite 기반 ADM/BZA
- SQL Canonical·Architecture·Taxonomy·Evidence 자동화 도구
- `cpf-docs` 역할별 Guide 구조
- Java 25·Spring Boot·Gradle Multi-module 기반

이 항목들은 **보존 후보**이지 자동 완료 항목이 아니다. 실제 Consumer, Runtime, SQL과 Evidence를 다시 확인해야 한다.

## 4. Critical Findings

### F-01. 현재 작업 요청서가 최신 기준선을 반영하지 못함 — 실패

`CPF_CURRENT_WORK_REQUEST.md`, `CPF_GAP_MATRIX.md`, `CPF_STABILIZATION_REPORT.md`, `CPF_EVIDENCE_INDEX.md`는 여전히 과거 `e35b7a0`을 기준으로 작성돼 있다. 최신 WIP `a63380e6c736fa9c5ae7e425d0e301d21ef3b848`의 구조·DB·정책을 반영하지 못한다.

특히 현재 요청서에는 `cpf-core` SystemCode를 `PFW`로 유지 권장하는 문구가 있어 최상위 목표와 충돌한다.

**조치:** 차기 요청서와 관리 문서를 최신 Commit 기준으로 전면 갱신한다.

### F-02. `cmnDB`가 최신 제품 정책과 정면 충돌 — 실패

현재 `cmnDB` 기본 설치에는 다음 9개 Table이 생성된다.

```text
cmn_sequence
cmn_sequence_issue_log
cmn_notification_log
cmn_business_log
cmn_edu_query_item
cmn_fixed_length_layout
cmn_fixed_length_group
cmn_fixed_length_field
cmn_fixed_length_masking_policy
```

`55_cmn_seed_data.sql`은 채번·알림·업무 로그·고정길이 Layout 데이터를 실제 Seed한다.

최종 합의 정책은 다음과 같다.

```text
cmnDB
└─ cpf-common DB 연동·Migration·CRUD·검색·Offset·Keyset·Validation·
   Transaction 검증용 최소 Sample Table 1개
```

따라서 현재 9개 Table 구조는 잘못된 Ownership이다.

- `cmn_sequence*`: 프레임워크 기본 기능이 아님
- 채번 참조 구현: BZA 선택형 Customization Sample
- `cmn_fixed_length_*`: 범용 Engine/Contract는 `cpf-core`, 기관별 정의는 `cpf-external`
- `cmn_edu_query_item`: 단일 Sample Table로 통합하거나 삭제
- `cmn_business_log`, `cmn_notification_log`: 실제 Owner/Consumer 검수 후 이동 또는 삭제

DB 실데이터를 직접 확인하지 못했으므로 운영 데이터 삭제 여부는 `재확인 필요`다. 다만 사용자는 양쪽 PC 모두 CPF DB를 초기화하여 재설치할 계획이므로, 신규 설치 정본에서는 목표 구조로 바로 정리해야 한다.

### F-03. 설치 SQL이 Install과 Destructive Reset을 혼합 — 실패

`specs/sql/00_all_install.sql`은 신규 Schema 생성만 하는 설치 Script가 아니다.

- 약 129회의 `DROP TABLE IF EXISTS`
- 16개의 `CREATE OR REPLACE USER`
- Schema·Table·User·권한·Seed를 한 파일에서 수행
- Migration/App 계정 Password 변수를 여러 서비스가 공유

이 구조는 다음 책임을 혼합한다.

1. Empty DB Install
2. Existing DB Reset
3. Service User Provisioning
4. Seed
5. Smoke

**위험:** 다른 Application Schema 삭제, 실수로 기존 데이터 제거, 운영 Upgrade와 신규 설치 혼동, 계정 최소 권한 불명확.

**조치:** 다음으로 분리한다.

- `install/`: 비파괴 신규 설치
- `reset/`: 명시적 Allowlist·Dry Run·Apply가 있는 개발 전용 초기화
- `provision/`: DB User·Grant
- `migration/`: Immutable Flyway Upgrade
- `rollback/`: Version별 Rollback 또는 복구
- `verify/`: Smoke와 Schema Contract

### F-04. Batch Physical Ownership이 혼재 — 부분 구현

`cpf-batch`가 Batch·Scheduler·Worker·Center-Cut의 공식 Owner이지만 현재 Physical Table은 `cpfDB`에 섞여 있다.

```text
cpf_batch_*
bat_center_cut_*
```

Schema도 `batDB`가 생성되지 않는다.

**목표:** 플랫폼 거래·Service Registry는 `cpfDB`, Batch Runtime State는 `batDB`와 `bat_*`로 일관되게 소유한다. 특정 이유로 `cpfDB`를 유지한다면 Owner·Migration·직접 접근 금지·API 경계를 공식 문서로 입증해야 한다. 현재 혼합 상태는 완료가 아니다.

### F-05. BZA가 업무 도메인 데이터까지 소유할 위험 — 부분 구현

현재 `bzaDB`에는 관리자·권한·승인 외에 `bza_customer`, `bza_product`, `bza_order` 같은 추정성 업무 Table이 포함돼 있다.

BZA는 고객 업무 관리자 UI와 운영 흐름을 제공하지만 회원·상품·주문 원장을 소유하는 업무 도메인이 아니다.

**조치:**

- 관리 사용자·역할·메뉴·승인·감사·저장검색·다운로드 정책은 BZA 소유 검토
- Customer/Product/Order 등 업무 원장성 Sample은 `cpf-reference` 또는 선택형 Sample로 분리
- 업무 채번은 BZA 선택형 Customization Sample로 제공하되 온라인 Runtime의 BZA 필수 의존 금지

### F-06. `cpf-common` Source Ownership도 과도하게 혼재 — 실패

현재 `cpf-common`에는 다음과 같은 축약·중복 Package가 함께 존재한다.

```text
api, biz, cde, cfg, common, config, dto,
fle, message, mqe, msg, ref, sec, tlm, utils
```

`cpf-common/fle/core`에는 범용 File Transfer와 Remote Command 계약이 존재한다. 범용 기술 Engine·Contract는 `cpf-core`, 기관별 Adapter는 `cpf-external`이 소유해야 한다.

또한 `message`, `mqe`, `msg` 같은 중복 Package는 역할과 Consumer를 확인하지 않으면 유지할 수 없다.

**조치:** Package별 실제 Consumer·Owner를 전수 추적하고, `cpf-common`을 고객사 업무 공통 Extension Layer로 축소한다.

### F-07. 고정길이 전문 Ownership 미완결 — 실패

README는 고정길이 전문 제공을 선언하지만 현재 DB 정본은 `cmnDB`가 Layout·Field·Masking Policy를 소유한다.

확정 정책:

```text
cpf-core
→ 범용 Fixed-Length Contract·Layout Model·Parser·Writer·Validation·
   Byte Length·Encoding·Padding·Masking SPI

cpf-external
→ 기관별 Layout·Mapping·Endpoint·Adapter·기관 오류·Version

cpf-admin
→ Core/External API를 이용한 관리·승인·감사 UI
```

현재 구조는 이 기준으로 재배치해야 한다.

### F-08. 업무 채번의 프레임워크 기본 기능화 — 실패

현재 Final Target Catalog에도 `CMN-ID 채번`이 남아 있고 SQL에는 `cmn_sequence*`가 있다.

최종 정책:

- CPF Core/Common 기본 기능으로 업무 채번을 제공하지 않는다.
- 거래 ID·Trace ID·Idempotency Key 같은 기술 식별자는 Core.
- 고객번호·계약번호·접수번호 같은 업무 채번은 고객 Runtime Owner가 소유.
- CPF는 BZA에 선택형 관리 화면·참조 Sample·Customization Guide만 제공.
- 실제 업무가 BZA를 온라인 필수 호출하는 구조는 금지.

### F-09. Flyway Lineage와 Checksum 무결성 — 재확인 필요

현재 Flyway는 `V1`~`V37`과 `checksums.sha256`을 보유한다. WIP 과정에서 Migration 파일명이 Rename된 흔적이 있어, 이미 배포된 Baseline과 현재 Blob이 동일한지 확인해야 한다.

사용자가 CPF DB를 초기화해도 **제품 Migration 정책**은 별개다.

- 기존 배포 이력이 있다면 Applied Migration 수정 금지
- 아직 정식 Release 전이고 모든 개발 DB를 폐기한다면 공식 Re-baseline을 한 번 수행하고 구 Lineage를 Archive
- 신규 Baseline 이후에는 Immutable Migration 강제

현재 상태는 정적 목록만 확인했으며 실제 `flyway_schema_history`와 과거 배포 Blob을 대조하지 못했으므로 `재확인 필요`다.

### F-10. Quality Gate가 Product Runtime 완료를 보장하지 않음 — 부분 구현

Root `qualityGate`는 compile/test, frontend verify, 정적 Architecture·SQL·문서·Evidence Gate를 다수 포함한다. 하지만 다음은 필수 의존성이 아니다.

- 빈 MariaDB Full Install
- 전체 Module Startup
- OpenAPI Runtime HTTP 검증
- ADM Runtime
- Two Worker Takeover
- Center-Cut E2E
- 실제 Broker/SFTP
- Browser E2E
- Web Server/WAS 분리 배포
- Upgrade·Rollback

따라서 `qualityGate PASS`만으로 제품 완료를 주장할 수 없다.

### F-11. PowerShell 7 설치만으로는 해결되지 않음 — 부분 구현

`build.gradle`의 PowerShell 기반 Task는 대부분 `powershell`을 직접 실행하고 `pwsh`를 사용하지 않는다. 회사·집 PC에 PowerShell 7을 설치해도 Gradle Task가 Windows PowerShell 5.1을 계속 호출할 수 있다.

**조치:** 공통 Resolver를 만들어 `pwsh` 우선, 명시적 Legacy 허용 시만 `powershell` fallback. CI와 Windows에서 실제 확인한다.

### F-12. Evidence 최신성 부족 — 재확인 필요

`cpf-docs/evidence/20260722_01`에는 Generator, Architecture, Runtime Config, 정적 UI, SQL Canonical 등 다수 Evidence가 있다. 그러나 최종 광범위한 Rename·SQL 변경 이후 생성되었는지 각 파일 Metadata와 Commit을 대조해야 한다.

특히 다음은 완료 근거가 아니다.

- 파일 존재
- Static Scan
- Generated Inventory
- Dry Run
- 과거 Commit Runtime
- Swagger Source Coverage
- UI Static Result

최종 Commit과 실행환경이 일치하지 않으면 `Stale`이다.

### F-13. Root·문서 Governance 미완결 — 실패

Root에는 작업·검수·진행 문서가 남아 있다.

```text
CPF_CURRENT_WORK_REQUEST.md
CPF_GAP_MATRIX.md
CPF_STABILIZATION_REPORT.md
CPF_EVIDENCE_INDEX.md
CPF_REVIEW_PROGRESS_COMPLETION_GUIDE.md
```

정본 정책상 Root에는 제품 식별·Build·실행에 필요한 최소 파일과 최상위 목표만 두고, 작업·검수·Evidence는 `cpf-docs` 역할별 위치로 이동해야 한다.

또한 `specs/`에 9개의 DOCX 최종 산출물이 존재하고 `qualityGate`가 이를 정본처럼 강제한다. 구현이 계속 변경되는 현재 단계에서 DOCX는 Stale 반복작업의 원인이다.

### F-14. README와 Guide는 방향은 좋으나 구현 정합성이 부족 — 부분 구현

README는 공식 Module과 CPF SystemCode를 반영했지만 다음 문제가 있다.

- `cpf-common`을 넓은 업무 공통 기능으로 기술
- 고정길이 전문·채번 Ownership 최신 정책 미반영
- License Badge가 있으나 최종 이용·배포 정책 미확정
- 실행 가능한 Gradle Task와 실제 구현이 불일치할 가능성
- Guide가 CenterCutRunner·Agent Failover·Pagination·Core API DX 등 최신 요구를 충분히 담지 못함

### F-15. Runtime·Browser·Deployment는 최신 Commit 기준 미검증

이 검수 환경에서는 최신 Commit을 Clone하여 직접 실행하지 못했다. 따라서 다음은 `미검증`이다.

- 전체 `clean test`
- `bootJar`, `bootWar`
- ADM/BZA `npm ci`, lint, typecheck, test, production build
- 독립 Nginx/Apache Web 배포
- Java WAS 독립 배포
- Browser Login·권한·직접 URL 차단·승인·재처리
- MariaDB 빈 설치·재설치·Upgrade·Rollback
- 다중 Agent·Runner·Worker
- 실제 Center-Cut 실패·재처리·Unknown Result

## 5. 요구사항 영역별 판정 Matrix

| 영역 | 상태 | 핵심 근거 |
|---|---|---|
| 최신 두 Push 보존 | 완료 | master에 두 WIP Commit 존재 |
| 공식 Module명 | 완료 | settings.gradle과 Root 구조 |
| Java Package 전환 | 부분 구현 | 공식 Root 존재, 전체 Compile/Reflection 미검증 |
| cpf-core SystemCode CPF | 부분 구현 | 목표·README·신규 SQL 반영, 전체 Runtime 경로 미검증 |
| PFW/XYZ 활성 명칭 제거 | 부분 구현 | 현행 SQL에는 없음, 전체 Source·Hash·Migration Lineage 미검증 |
| AI 전용 기능 제거 | 재확인 필요 | 전체 Dependency·Source Scan 직접 미수행 |
| cpf-common 최소화 | 실패 | DB 9 Table, 기술 기능 Source 혼재 |
| 고정길이 전문 Core 소유 | 실패 | cmnDB·common에 잔존 |
| 업무 채번 BZA Sample | 미구현 | cmnDB 기본 기능으로 존재 |
| cpf-external | 부분 구현 | Module·SQL 존재, 실제 Institution Runtime 미검증 |
| Generator | 부분 구현 | Tool·Evidence 존재, 최신 Commit 재실행 필요 |
| MBR/ACC/EXS 최소 업무 Table | 재확인 필요 | Schema 존재, 과도한 업무 가정 전수검수 필요 |
| Batch/Worker | 부분 구현 | Source·Migration 존재, Two-process 최신 검증 필요 |
| CenterCutRunner | 부분 구현 또는 미구현 | 세부 Runner 책임·Parameter E2E Evidence 없음 |
| 주·보조 Agent | 미구현 또는 미검증 | Registry·Failover·Fencing E2E 불명 |
| 실패 건 재처리 | 부분 구현 또는 미검증 | API/DB/Runtime 연결 Evidence 불명 |
| ADM | 부분 구현 | Backend·Vue 존재, Production Browser E2E 없음 |
| BZA | 부분 구현 | Backend·Vue·DB 존재, Owner 과다·Browser E2E 없음 |
| Vue/Web Server·WAS 분리 | 미검증 | Production 배포 Evidence 없음 |
| Core Public API·SPI DX | 부분 구현 | API package 존재, 표준 Utility·Pagination 미완결 |
| List/Page/Slice/Cursor | 미구현 또는 재확인 필요 | Guide와 정본에 구체 계약 없음 |
| SQL 신규 설치 | 실패 | Destructive Reset과 Install 혼합 |
| SQL Upgrade·Rollback | 재확인 필요 | Flyway Lineage 실제 DB 미대조 |
| DB User 최소 권한 | 부분 구현 | 사용자 생성 존재, Password·Grant 분리 미흡 |
| Security·Audit | 부분 구현 | Source·Guide 존재, Runtime·Browser·Masking 미검증 |
| Evidence | 부분 구현 | 다수 존재하나 최신성·Runtime성이 불충분 |
| Guide·README | 부분 구현 | 방향은 있으나 최신 합의와 불일치 |
| Root 정리 | 실패 | 작업 문서와 premature DOCX 잔존 |
| 배포·Rollback | 미구현 또는 미검증 | remoteDeploy 실제 Target 없음 |

## 6. 현재 가장 우선순위가 높은 작업 순서

1. 최신 Commit Clean Clone과 Baseline Evidence
2. Rename·Hash·Lockfile·Flyway 무결성
3. Module·Package·SystemCode·DB Naming 정합성
4. DB Owner Inventory와 신규 Physical Schema 표준
5. `cmnDB` 1 Sample Table 정책
6. Fixed-Length·File·Message·Sequence Ownership 재배치
7. Install/Reset/Provision/Migration/Verify 분리
8. 빈 MariaDB 전체 재설치
9. 전체 Module·Frontend Build와 Runtime
10. Batch·Center-Cut·Agent·Recovery E2E
11. ADM/BZA Browser·분리 배포
12. Core Public API·Sample·Generator 표준화
13. Security·Audit·Masking·운영 검증
14. Guide·README·Evidence 정본화
15. 최종 완료 Gate

## 7. 검수 결론

현재 WIP는 폐기할 상태가 아니다. 구조 전환과 도구화의 상당 부분을 보존하되, **완료 선언을 모두 해제하고 최신 빈 DB 기준으로 재검증·재소유·재정렬**해야 한다.

다음 작업은 개별 버그 보완 요청이 아니라 `03_CPF_NEXT_CODEX_WORK_REQUEST.md`의 통합 요청으로 진행하는 것이 적절하다.


## 사용자 후속 확인

회사 PC의 CPF Schema·Table은 이미 삭제되었고 집 PC도 Empty DB다. 차기 작업은 기존 DB 보정이 아니라 정본 재구축으로 수행한다.
