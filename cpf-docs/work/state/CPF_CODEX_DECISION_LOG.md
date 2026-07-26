# CPF Codex Decision Log

이 문서는 새 PC나 새 세션에서도 반드시 유지해야 하는 Architecture, Ownership와 Migration 결정만 기록한다. 단순 진행 상황은 `CPF_CODEX_CONTINUITY_STATE.md`에서 관리한다.

## DEC-001 공식 식별 체계

- 상태: `완료`
- 결정: 정식 명칭은 **Core Platform Framework**다. 공식 Module은 `cpf-*`, Java root package는 `com.cpf.<domain>`, 내부 SystemCode는 3자리 대문자를 사용한다. Core SystemCode는 `CPF`다.
- 이유: 사람이 읽는 DomainName과 내부 식별자를 분리하고 Source, API, SQL, Config와 운영 식별자의 충돌을 막는다.

## DEC-002 Module 의존성과 데이터 소유권

- 상태: `완료`
- 결정: Business Domain → `cpf-common` → `cpf-core` 방향을 유지한다. `cpf-core`의 업무/Common/Admin/Batch 역참조, 업무 Module 간 DB 직접 접근, Admin의 Owner DB 직접 갱신과 순환 의존을 금지한다.
- 이유: Public Contract, 실제 Owner와 장애·복구 경계를 명확히 하고 배포 topology가 계약을 바꾸지 않게 한다.

## DEC-003 `cmnDB` 최소화

- 상태: `완료`
- 결정: `cmnDB` Schema는 생성하지만 기본 제품 Table은 DB 연결, Migration, CRUD, 검색, Offset/Slice/Cursor, Validation, Duplicate, Optimistic Lock와 Transaction을 검증하는 sample table 1개만 둔다. `cpf-common`은 DB-less 기본 사용이 가능해야 한다.
- 이유: Common은 고객 업무 공통 Extension이며 기술 Engine이나 추정성 업무 데이터의 저장소가 아니다.

## DEC-004 업무 채번

- 상태: `완료`
- 결정: `cmn_sequence*`와 업무 채번 Runtime은 Core/Common 기본 제품에서 제거한다. BZA에는 운영 기본 비활성인 선택형 Customization Sample만 둘 수 있으며 온라인 업무가 BZA에 의존하면 안 된다.
- 이유: 업무 번호 정책은 고객 또는 업무 Domain 소유이고 Framework 기술 ID와 성격이 다르다.

## DEC-005 Fixed-Length와 External

- 상태: `완료`
- 현재 적용: External 고정 Module 소유 부분은 DEC-024로 대체됨
- 결정: 범용 고정길이 Layout/Field/Group/Parser/Writer/Validation/Masking/Encoding API·SPI는 `cpf-core`가 소유한다. 기관별 Layout, Mapping, Endpoint, Authentication, Adapter, Retry, Unknown Result와 Reconciliation은 고객/업무 Generated Domain이 소유하며, 대외 업무 Domain이 필요하면 `external/EXS`도 동일 Golden Generator로 생성한다.
- 이유: 재사용 가능한 기술 Contract와 기관별 업무·운영 정책을 분리한다.

## DEC-006 Batch Physical Ownership

- 상태: `완료`
- 결정: Batch, Scheduler, Agent, Runner, Worker와 Center-Cut Runtime state는 `cpf-batch`가 소유하며 권장 물리 Schema/Prefix는 `batDB`/`bat_*`다.
- 이유: Core Schema 혼재를 제거하고 실행, 복구, 보존과 운영 책임을 실제 Module Owner에 맞춘다.

## DEC-007 Empty Install 책임 분리

- 상태: `완료`
- 결정: 최초 설치는 Reset 없이 빈 MariaDB에서 성공해야 한다. Provision, non-destructive install, product seed, optional sample/EDU/test seed, verify와 allowlisted reset을 분리한다. Secret은 외부 입력으로만 받는다.
- 이유: 재현 가능한 신규 설치, 최소 권한, 비파괴성, 운영 seed와 시험 데이터의 분리를 보장한다.

## DEC-008 Historical Migration 보호

- 상태: `완료`
- 결정: 기존 Flyway 파일은 적용 이력과 checksum 감사를 마칠 때까지 불변으로 취급한다. Pre-release 단일 re-baseline은 고객/운영 적용 이력 없음, 모든 개발 DB 폐기, checksum 처리·upgrade/rollback 대안과 Empty Install Evidence를 갖춘 뒤 사용자 명시 승인이 있을 때만 가능하다.
- 이유: 현재 DB가 비어 있다는 사실은 과거 Migration 변경 권한을 의미하지 않으며, checksum과 업그레이드 경로 손상을 막아야 한다.

## DEC-009 ADM/BZA 경계와 Frontend 배포

- 상태: `완료`
- 결정: ADM은 Platform Control Plane, BZA는 Customer Business Admin이다. 위험 조치는 Owner Command API, 권한, 승인, 사유와 Audit를 사용한다. ADM/BZA Frontend는 서로 및 Java WAS와 독립 Build/Deploy/Rollback을 지원한다.
- 이유: 운영 상태 직접 수정과 권한 우회를 막고 독립 배포·복구 경계를 보장한다.

## DEC-010 문서와 Evidence 수명주기

- 상태: `완료`
- 결정: 구현 중 정본은 Markdown과 실제 Source/SQL/API/Test다. Generated Matrix와 DOCX/PDF는 Source와 Evidence가 안정된 뒤 재생성한다. 삭제된 Stale Evidence, 조기 문서와 중복 산출물을 복구하지 않는다.
- 이유: 과거 환경·Commit의 산출물이 현재 완료 근거로 오인되는 것을 방지한다.

## DEC-011 DB Bootstrap과 Runtime 권한 경계

- 상태: `완료`
- 결정: Local/DEV/Codex는 DB preflight 후 명시적인 Provision → Empty Install → Product Seed → Verify를 수행하고 Runtime을 기동한다. 운영 Application은 관리자 권한으로 Schema/User를 암묵 생성하지 않으며 사전 설치된 DB에 최소 Runtime 권한으로 연결한다.
- 이유: DB가 없는 새 PC에서도 검증을 계속하되, 설치 자동화와 운영 Runtime의 권한·책임을 섞지 않기 위해서다.

## DEC-012 Multi-Vendor DB 격리

- 상태: `완료`
- 결정: 공식 지원 구조는 MariaDB, MySQL, PostgreSQL, Oracle, SQL Server를 대상으로 한다. Platform Module과 모든 Generated Domain(`external/EXS` 포함)에서 Vendor 선택을 `cpf.db.vendor`와 Driver/Datasource/Migration/SQL resource로 격리하며 Controller, Service, Domain, API와 일반 Repository 호출 계약에는 Vendor 분기를 두지 않는다. Vendor별 물리 SQL은 달라도 논리 Schema, 상태, Seed, API와 Repository 의미는 동일해야 한다.
- 이유: 고객 DB 전환이 Java 업무 Source 수정이나 Module fork를 요구하지 않게 하고 동일 Binary/Source의 배포 가능성을 보장하기 위해서다.

## DEC-013 Minimal Transaction Reference Schema

- 상태: `완료`
- 현재 적용: MBR/ACC/REF를 동일 고정 Reference로 보는 부분은 DEC-024로 대체됨
- 결정: 모든 Generated Domain은 Domain별 임의 원장 대신 동일한 Minimal Transaction Golden Schema Template을 사용한다. Schema/SystemCode/Table prefix만 Metadata로 달라지고 CRUD, 검색, 정렬, Offset/Slice/Cursor, Validation, Duplicate, Optimistic Lock, Transaction, 호출·Header·Idempotency·Audit 경로를 같은 논리 계약으로 검증한다. `external/EXS` 역시 예외 Template을 두지 않으며 기관별 특화 Adapter/업무 데이터가 필요하면 Generated Domain의 확장 경계에서 추가한다.
- 이유: Framework 거래 처리 검증을 업무 예시 차이에서 분리하고 Vendor 및 Generator lifecycle parity를 자동 검증하기 위해서다.

## DEC-014 Vendor SQL Resource Pack 선택

- 상태: `완료`
- 결정: DB 차이는 Vendor별 Provision/Install/Product Seed/Migration/Verify/Rollback과 Runtime MyBatis/Repository Query SQL resource pack으로 분리한다. DB 초기화 Shell은 `cpf.db.vendor`와 같은 단일 Vendor 선택을 받아 해당 pack으로 DB 생성부터 초기 데이터·검증까지 실행하고 Runtime도 같은 Vendor query resource를 선택한다. 이 원칙은 모든 공식 Module과 Generator 신규 Domain에 동일하게 적용한다. 업무 Java Source는 Vendor 선택으로 변경하지 않으며, Source 파일을 덮어쓰는 대신 패키징된 resource location 또는 생성된 격리 실행 directory를 선택한다.
- 이유: DB 교체 시 Java 업무 Source 수정·fork를 막고 설치 SQL과 실행 Query가 서로 다른 Vendor를 가리키는 구성 오류를 fail-closed로 차단하기 위해서다.

## DEC-015 Vendor SQL Pack의 중앙 물리 소유권

- 상태: `완료`
- 결정: Vendor별 SQL 정본은 개별 제품 Module의 `src/main/resources`가 아니라 `cpf-tools/db/vendor/<vendor>` 중앙 Pack이 소유한다. Pack 내부에서 `provision/install/seed/migration/runtime/<module>/verify/rollback`으로 기능과 Module Ownership을 구분한다. 초기화 Tool은 한 Vendor Pack 전체를 선택하고, Runtime에는 선택 Vendor의 외부 resource root 또는 격리된 generated-resources/classpath overlay만 연결한다. 선택 과정에서 Git Source Tree를 덮어쓰거나 Diff를 만들지 않으며 Java Service/Controller/Domain/Repository 업무 Source와 제품 Module artifact에는 5개 Vendor SQL을 반복 적재하지 않는다. Generator도 신규 Domain Module에 Vendor 디렉터리를 복제하지 않고 중앙 Template/Pack에 Domain resource를 등록한다. 과도기에는 Consumer 확인 후 제거하도록 했으나 이 이행 규칙은 DEC-019에서 대체됐다. 현재는 중앙 Pack이 정본이며 Module-local fallback을 제거해 fail-fast로 숨은 Consumer를 노출한다.
- 이유: Vendor SQL의 중복·drift와 모든 Vendor resource의 불필요한 Runtime 활성화를 막고, 동일 Java Source/Artifact에 설치 설정과 선택 Vendor Pack만 결합하는 배포 경계를 만들기 위해서다.

## DEC-016 생성형 Domain의 Metadata·Template 확장

- 상태: `완료`
- 결정: MBR/ACC/REF/PAY/INS 등은 Generator 지원 대상의 고정 목록이 아니라 현재 또는 설명용 예시다. Generated Domain은 `DomainName`, `SystemCode`, `ModuleName`, `PackageName`, `SchemaName`, `TablePrefix` Metadata를 공통 Minimal Transaction Domain Template에 적용한다. 신규 Domain/SystemCode 추가는 Metadata 등록과 Generator 실행만으로 이루어지며 중앙 Tool의 switch/if 또는 Java Source 수정을 요구하지 않는다. Vendor별 생성형 DDL/Seed/Runtime Query/Verify는 `cpf-tools/db/vendor/<vendor>/domain-template` 중앙 Template이 생성하고, 결과를 Domain Module 내부에 5벌 복제하지 않는다. 기본 Sample은 특정 회원·계좌·보험 업무가 아니라 CRUD, Search, Paging, Validation, Commit/Rollback, Optimistic Lock, Duplicate, Local/Remote Call, Standard Header, transactionId, Error Mapping, Idempotency와 Audit/Masking을 검증하는 동일 논리 모델이다. CPF 고정 Platform Module resource와 무제한 확장되는 Generated Domain Metadata/Template의 Ownership을 분리한다.
- 이유: 현재 Repository의 예시 Domain에 Generator가 종속되는 것을 막고, 임의 고객 Domain을 동일 Java 구조와 Vendor Template 계약으로 재현 가능하게 생성하기 위해서다.

## DEC-017 현행 설치 DB 객체의 최소화 판정

- 상태: `완료`
- 현재 적용: Legacy fixed 업무 Domain retirement는 DEC-024의 Golden Generated Domain 정책으로 대체됨
- 결정: 현재 Empty Install의 Table, Sequence, Constraint, Index와 Product Seed는 과거 Dump나 Historical Migration에 존재한다는 이유만으로 유지하지 않는다. 각 객체는 최신 정본의 Owner 책임과 실제 Java/MyBatis/Repository/Installer/Framework 동적 Consumer 중 하나로 존재 이유가 확인되어야 한다. 소비자가 없고 활성 원장과 중복되는 객체는 현행 설치 경로에서 제거하며, 정본 요구가 있으나 Consumer가 미완성인 객체는 삭제 대신 `부분 구현`으로 관리한다. Generated Domain 기본 구조는 단일 `*_sample_item` Golden Template을 따르고, 기관/고객 특화 원장은 기본 Platform install에 넣지 않는다. MariaDB Spring Batch 객체는 사용 중인 Spring Batch Version의 공식 MariaDB Schema 계약을 따른다.
- 이유: 추정성 Schema와 중복 원장, 사용되지 않는 Seed·Index를 제품 Baseline에 고착시키지 않으면서도 보안·운영 정본 객체를 단순 문자열 검색만으로 잘못 삭제하지 않기 위해서다.


## DEC-018 Requirement ID 영구 연속성

- 상태: 완료
- 결정: 한 번 등록된 Requirement ID는 Mapping 없이 삭제하거나 Rename하지 않는다. 통합/분해/Owner 변경은 Continuity Ledger에 Old→New 관계를 남기며 완료율은 Canonical ID만 집계한다. 133→126 감소 과정의 유실을 보정하여 현재 Canonical Count를 162개로 관리한다.
- 이유: PC/세션/Codex 교체 때 과거 요구가 조용히 사라지거나 같은 Gap이 새 이름으로 재개발되는 것을 막는다.

## DEC-019 Central Vendor Pack Fail-Fast

- 상태: 완료
- 결정: 제품 Runtime의 Vendor SQL/MyBatis 정본은 `cpf-tools/db/vendor/<vendor>` 중앙 Pack 하나다. Production resolver/catalog는 `cpf.db.resource-root`가 없거나 Pack이 불완전하면 fail-fast 한다. Module-local vendor SQL을 호환 fallback으로 복구하지 않는다.
- 이유: 과도기 fallback이 중앙 Pack 연결 오류를 숨기고 같은 SQL을 Module×Vendor로 복제하게 만드는 문제를 제거한다.

## DEC-020 ADM과 BZA Approval 분리

- 상태: 완료
- 결정: ADM Approval은 플랫폼 위험조치 Dual Control/SoD/Break-glass와 Owner Command 실행을 소유하고, BZA Approval은 고객 업무 조직/직원/부서합의 결재를 소유한다. 두 Engine/Table/Policy는 공유하지 않는다.
- 이유: 보안 Control Plane과 고객 업무 결재의 책임, 데이터, 감사, 확장 모델이 다르다.

## DEC-021 BZA 조직/직원과 결재 Snapshot

- 상태: 완료
- 결정: BZA는 조직 Hierarchy, 직원, 직급, 직책, 유효기간 기반 Assignment와 복수 Role을 지원한다. Approval Policy와 Instance를 분리하고 Instance 생성 시 조직/직급/직책/참여자 Snapshot을 고정한다. ALL/ANY/N_OF_M, 필수/선택 부서, 위임/대결/부재를 실제 Engine으로 구현한다.
- 이유: 조직개편 이후에도 과거 결재를 재현하고 기업 결재 요구를 사람별 고정 Line 구조에 묶지 않기 위해서다.

## DEC-022 ADM Operator Directory 경계

- 상태: 완료
- 결정: `adm_operator`는 Authentication Identity다. 조직/사번/직급/직책/외부 Directory Subject는 별도 Profile/Directory Port로 관리하고 DB default adapter와 LDAP/AD/IAM/HR 확장을 허용한다. ADM이 기업 HR 원장을 소유하지 않는다.
- 이유: 승인/Audit에 필요한 조직 문맥은 확보하되 플랫폼 관리자가 고객 HR Master와 결합되지 않게 한다.

## DEC-023 transactionId 단일 전역 거래 식별자

- 상태: `완료`
- 결정: CPF 거래 실행 인스턴스는 `transactionId` 하나로 식별한다. 외부/선행 호출의 유효 transactionId를 승계하고, 내부 독립 기동은 Core가 34자리 transactionId를 신규 생성한다. 동일 업무 흐름의 Local/Remote/Async/Retry/Batch/Worker/Center-Cut은 같은 transactionId를 유지하며 호출 계층은 `segmentId/parentSegmentId`로 표현한다. `standardExecutionId`는 실행 정의 ID로 분리한다.
- 이유: Global/root/parent/child 거래 식별자와 업무 거래 정의 ID가 혼용되어 로그 그룹 추적, DB 검색, 호출 전파와 개발자 이해가 흔들리는 문제를 제거한다.

## DEC-024 Golden Generated Domain과 Legacy Fixed Domain Retirement

- 상태: `완료`
- 결정: Generated Domain의 정본은 특정 MBR/ACC/EXS 구현이 아니라 단일 Golden Template이다. 임의 DomainName/SystemCode를 Metadata로 적용하고 동일 Capability 결과는 normalize parity가 같아야 한다. 기존 수작업 fixed 업무 Module은 Consumer를 안전하게 이관한 뒤 retirement하며, `external/EXS`가 필요하면 fixed Module 복구가 아니라 동일 Generator로 다시 생성한다.
- 이유: 현재 수작업 Domain을 Template로 승격하면 과거 가비지/업무특화 구조가 신규 고객 Domain에 복제되고, 반대로 성급한 삭제는 기존 성공 기능을 회귀시킨다.

## DEC-025 Canonical Repository Path

- 상태: `완료`
- 결정: 제품 Root의 문서 파일은 최종적으로 `README.md`만 유지한다. Tool Script는 `cpf-tools/scripts`, Vendor별 사람이 수정하는 Platform DB Source와 배포/Runtime Pack은 모두 `cpf-tools/db/vendor/<vendor>` 경계가 소유하며 Source는 그 아래 `source/`에 둔다. Root 작업문서, 기존 Root `scripts`, 독립 `cpf-tools/db/source/<vendor>` tree는 역할별 Canonical Path로 이동하고 모든 Gradle/CI/Guide 참조를 함께 보정한다.
- 이유: 작업문서/SQL/Script가 Root에 산재하고 같은 역할의 정본이 중복되는 문제를 제거한다.

## DEC-026 Vendor-first Schema/Metadata Change Order

- 상태: `완료`
- 현재 적용: DEC-027의 Canonical Metadata/Generator-first 순서로 대체됨
- 결정: Table/Column/Index/Seed/기준 Metadata 변경은 DB Source/Vendor 정본부터 시작하고 generated bundle, migration/rollback, Mapper/Repository, Service/API/UI, Test/Runtime/Evidence 순서로 전파한다. Product Seed에는 설치 직후 필요한 권한/메뉴/정책 Metadata를 제공하고 Local/EDU/고객 조직 Sample은 Optional Seed로 분리한다.
- 이유: Java나 파생 SQL부터 수정해 Source/Install/Runtime 계약이 갈라지고 Fresh Install 때 Metadata가 비는 재발을 막는다.

## DEC-027 Canonical Metadata / Generator-first DB Change Order

- 상태: `완료`
- 결정: DB Query, Schema 또는 Metadata를 변경할 때의 정식 순서는 `Requirement / Data Model → Canonical Schema / Metadata → Generator / Domain Template → Generated Domain 산출 기준 → Vendor Source SQL → Migration → Install → Upgrade → Rollback → Seed → Verify → Test → Evidence`다. Platform 고정 제품 DB와 Generated Domain Template의 Ownership은 분리하되, 파생 Vendor SQL이나 Migration만 먼저 고쳐 정본과 산출물이 갈라지는 변경은 허용하지 않는다. Vendor별 물리 차이는 중앙 Pack에서 해결하고 Java 업무 Source는 DB-neutral하게 유지한다.
- 이유: 최신 사용자 검수 요청의 명시적 보정이며, MariaDB 단독 Hotfix가 Generator와 나머지 Vendor Pack에 Drift를 만드는 일을 막기 위해서다.

## DEC-028 DB 연결 보안과 설치 Verify 계약

- 상태: `완료`
- 결정: DB TLS mode는 Client/OS의 암묵 기본값에 맡기지 않고 설치 Profile에 `disabled`, `preferred`, `required`, `verify-full` 중 하나로 명시한다. Git 추적 Local Development Profile만 `disabled`를 사용하며 Production Template은 `verify-full`을 사용한다. 공식 Installer는 Canonical Schema Manifest 기준으로 실제 Table, Column 순서, 선언 Index와 FK를 대조하고, Product Seed 이후 중앙 Vendor Verify Pack의 모든 `check_name/passed` 결과가 1일 때만 완료로 판정한다.
- 이유: PC 재부팅이나 Client Version에 따라 TLS negotiation 결과가 달라지는 문제와, Table 존재 확인만으로 Stale/누락 Schema·잘못된 Seed를 설치 성공으로 기록하는 문제를 막는다.

## DEC-029 Migration Version 선택 기준

- 상태: `완료`
- 결정: 신규 Platform Migration version은 현재 수정 가능한 Source subset의 마지막 번호가 아니라 중앙 Runtime Lifecycle Pack에 보존된 전체 Historical Migration의 최고 version을 기준으로 선택한다. Historical V55/V56이 Runtime Pack에 이미 있던 상태에서 ADM transactionId 표준화는 V57로 추가하며, 기존 Migration 본문이나 checksum을 덮어쓰지 않는다.
- 이유: Source subset만 보고 번호를 선택하면 Runtime Historical version과 충돌해 Flyway 적용 순서와 checksum 무결성이 깨진다.

## DEC-030 Platform Table Lifecycle / Audit 정책

- 상태: `완료`
- 결정: Platform Table의 기본 정책은 `full-audit`이지만 Append-only 기록, 상태 전이 원장, Lock/Claim/Lease, Aggregate Projection, 채번, 정적 호환성 계약과 Spring Batch Framework Table은 `cpf-tools/db/metadata/platform-table-lifecycle-policy.json`에 lifecycle 유형, 필수 semantic actor/time/fencing Column과 사유를 명시해야만 예외로 허용한다. 신규 Table은 명시적 예외가 없으면 공통 Audit 계약을 적용하며, 미등록 Table·알 수 없는 정책·사유/필수 semantic Column 누락·삭제 Table의 stale 정책은 Gate에서 실패한다.
- 이유: 모든 운영/이력/Lease Table에 `created_by/created_at/updated_by/updated_at`을 기계적으로 추가하면 실제 lifecycle 의미와 중복되고 불필요한 저장 구조가 된다. 반대로 예외를 코드에 하드코딩하면 신규 Table이 검토 없이 빠지므로 Canonical Metadata를 fail-closed 정본으로 둔다.

## DEC-031 Comment Migration Delta와 Rollback 보존

- 상태: `완료`
- 결정: Schema Comment Migration Metadata에는 Canonical DDL 전체가 아니라 해당 Version이 새로 추가하는 Table/Column Comment Delta만 기록한다. 이전 Migration이나 기존 설치에 이미 있던 Comment는 신규 Migration/Rollback 대상에서 제외한다. V58은 Metadata Generator로 Forward/Rollback을 생성하고, 실제 MariaDB에서 Upgrade → Rollback → Re-upgrade 동안 Comment Delta, Column/Index/FK 정의 Hash와 `FOREIGN_KEY_CHECKS` 복원을 검증한다.
- 이유: 전체 Canonical Comment를 신규 Version Delta로 오인하면 Rollback이 V57 이전에 존재하던 설명까지 삭제한다. 실제 V57 DB Baseline이 이 위험을 검출했으며, Delta-only Metadata가 Historical 상태를 보존한다.

## DEC-032 Build Tooling 물리 소유권

- 상태: `완료`
- 결정: CPF Convention Gradle Plugin과 Platform BOM은 제품 Runtime Module이 아니므로
  Repository Root가 아니라 각각 `cpf-tools/build/gradle-plugin`,
  `cpf-tools/build/platform-bom`이 소유한다. Root `settings.gradle`은 이 두 격리
  Composite Build를 직접 참조한다. `cpf-tools/build`의 추적 Source만 `.gitignore`
  예외로 두고 각 격리 Build의 `.gradle`, `build`, `bin` 산출물은 계속 제외한다.
- 이유: Repository Root에는 제품 식별·Build 진입에 필요한 최소 구조만 유지하고,
  Build Support Unit의 소유권을 Tooling 경계에 모으면서 clone 직후에도 Composite
  Build가 재현되도록 하기 위해서다.

## DEC-033 선택 Generated Domain의 Platform/EDU 비종속과 Self Sample

- 상태: `완료`
- 결정: `cpf-admin`과 다른 고정 Platform Module은 MBR/ACC/PAY 같은 특정 Generated
  Domain의 Java Type, URL, DB, DataSource, 메뉴 또는 필수 Readiness에 종속되지 않는다.
  `cpf-reference`의 Local/Remote/Header/transactionId/Error 교육은 REF가 소유한 중립
  Self Simulator를 사용하고 특정 Generated Domain의 존재를 전제로 하지 않는다.
  각 Generated Domain은 동일 Generator-owned Minimal Transaction Source 구조와
  `${tablePrefix}_sample_item` 한 개를 자체 보유하며 Local/Remote 검증도 자기
  Public/Internal Sample 경계를 사용한다. 서로 다른 Domain 간 parity는 Generator
  검증이 임시 Domain을 생성한 동안에만 수행하고 종료 시 모두 제거한다. 기존
  MBR/ACC 업무특화 Source/Table은 Consumer를 Platform/REF에서 제거하고 필요한 고객
  확장 Ownership을 분리한 뒤 Golden Template 전환 과정에서 retirement한다.
- 이유: Generated Domain은 선택적으로 삭제·재생성될 수 있어야 하며, ADM/REF가 특정
  예시 Domain을 요구하면 제품 Platform 기동과 EDU가 고객 업무 Module 수명주기에
  결합된다. 한 개의 중립 Self Sample만 정본으로 두면 이름에 따른 업무 가정과
  Schema/Source drift 없이 임의 Domain을 동일하게 검증할 수 있다.


## 2026-07-25 — Vendor source ownership / EXS / Frontend packaging

- EXS는 고정 Platform Module이 아니라 Generated Domain only로 확정한다. `external/EXS`도 PAY/INS/CRM과 동일 Golden Generator를 사용한다.
- Platform Vendor canonical source는 `cpf-tools/db/vendor/<vendor>/source` 경계에서 관리한다. 특정 Vendor 전용 top-level source tree를 만들지 않는다.
- 지원하지 않는 Platform Vendor는 MariaDB 복사본/fallback 없이 fail-closed한다.
- ADM/BZA frontend는 self-contained static artifact이며 외부 CDN/remote CSS/font/icon에 Runtime 의존하지 않는다. App Shell, feature package, route registry, state/API boundary, code splitting을 표준으로 한다.
- 환경별 Docker Compose는 Repository Root가 아니라 `deploy/`가 소유한다.
