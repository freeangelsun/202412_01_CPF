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
- 결정: 범용 고정길이 Layout/Field/Group/Parser/Writer/Validation/Masking/Encoding API·SPI는 `cpf-core`가 소유한다. 기관별 Layout, Mapping, Endpoint, Authentication, Adapter, Retry, Unknown Result와 Reconciliation은 `cpf-external`이 소유한다.
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
- 결정: 공식 지원 구조는 MariaDB, MySQL, PostgreSQL, Oracle, SQL Server를 대상으로 한다. `cpf-core`, `cpf-common`, `cpf-admin`, `cpf-biz-admin`, `cpf-batch`, `cpf-gateway`, MBR/ACC/REF/EXS와 Generator 신규 Domain 전체에서 Vendor 선택을 `cpf.db.vendor`와 Driver/Datasource/Migration/SQL resource로 격리하며 Controller, Service, Domain, API와 일반 Repository 호출 계약에는 Vendor 분기를 두지 않는다. Vendor별 물리 SQL은 달라도 논리 Schema, 상태, Seed, API와 Repository 의미는 동일해야 한다.
- 이유: 고객 DB 전환이 Java 업무 Source 수정이나 Module fork를 요구하지 않게 하고 동일 Binary/Source의 배포 가능성을 보장하기 위해서다.

## DEC-013 Minimal Transaction Reference Schema

- 상태: `완료`
- 결정: MBR, ACC, REF와 Generator 신규 업무 Domain은 Domain별 임의 원장 대신 동일한 Minimal Transaction Reference Schema Template을 사용한다. Schema/SystemCode/Table prefix만 Domain에 맞추고 CRUD, 검색, 정렬, Offset/Slice/Cursor, Validation, Duplicate, Optimistic Lock, Transaction, 호출·Header·Idempotency·Audit 경로를 같은 논리 계약으로 검증한다. EXS는 대외연계 책임에 필요한 최소 추가 구조만 허용한다.
- 이유: Framework 거래 처리 검증을 업무 예시 차이에서 분리하고 Vendor 및 Generator lifecycle parity를 자동 검증하기 위해서다.

## DEC-014 Vendor SQL Resource Pack 선택

- 상태: `완료`
- 결정: DB 차이는 Vendor별 Provision/Install/Product Seed/Migration/Verify/Rollback과 Runtime MyBatis/Repository Query SQL resource pack으로 분리한다. DB 초기화 Shell은 `cpf.db.vendor`와 같은 단일 Vendor 선택을 받아 해당 pack으로 DB 생성부터 초기 데이터·검증까지 실행하고 Runtime도 같은 Vendor query resource를 선택한다. 이 원칙은 모든 공식 Module과 Generator 신규 Domain에 동일하게 적용한다. 업무 Java Source는 Vendor 선택으로 변경하지 않으며, Source 파일을 덮어쓰는 대신 패키징된 resource location 또는 생성된 격리 실행 directory를 선택한다.
- 이유: DB 교체 시 Java 업무 Source 수정·fork를 막고 설치 SQL과 실행 Query가 서로 다른 Vendor를 가리키는 구성 오류를 fail-closed로 차단하기 위해서다.

## DEC-015 Vendor SQL Pack의 중앙 물리 소유권

- 상태: `완료`
- 결정: Vendor별 SQL 정본은 개별 제품 Module의 `src/main/resources`가 아니라 `cpf-tools/db/vendor/<vendor>` 중앙 Pack이 소유한다. Pack 내부에서 `provision/install/seed/migration/runtime/<module>/verify/rollback`으로 기능과 Module Ownership을 구분한다. 초기화 Tool은 한 Vendor Pack 전체를 선택하고, Runtime에는 선택 Vendor의 외부 resource root 또는 격리된 generated-resources/classpath overlay만 연결한다. 선택 과정에서 Git Source Tree를 덮어쓰거나 Diff를 만들지 않으며 Java Service/Controller/Domain/Repository 업무 Source와 제품 Module artifact에는 5개 Vendor SQL을 반복 적재하지 않는다. Generator도 신규 Domain Module에 Vendor 디렉터리를 복제하지 않고 중앙 Template/Pack에 Domain resource를 등록한다. 현재 Module-local WIP SQL은 Consumer, Mapper loading, Build와 실제 MariaDB Runtime Query가 중앙 Pack으로 성공한 뒤에만 제거한다.
- 이유: Vendor SQL의 중복·drift와 모든 Vendor resource의 불필요한 Runtime 활성화를 막고, 동일 Java Source/Artifact에 설치 설정과 선택 Vendor Pack만 결합하는 배포 경계를 만들기 위해서다.

## DEC-016 생성형 Domain의 Metadata·Template 확장

- 상태: `완료`
- 결정: MBR/ACC/REF/PAY/INS 등은 Generator 지원 대상의 고정 목록이 아니라 현재 또는 설명용 예시다. Generated Domain은 `DomainName`, `SystemCode`, `ModuleName`, `PackageName`, `SchemaName`, `TablePrefix` Metadata를 공통 Minimal Transaction Domain Template에 적용한다. 신규 Domain/SystemCode 추가는 Metadata 등록과 Generator 실행만으로 이루어지며 중앙 Tool의 switch/if 또는 Java Source 수정을 요구하지 않는다. Vendor별 생성형 DDL/Seed/Runtime Query/Verify는 `cpf-tools/db/vendor/<vendor>/domain-template` 중앙 Template이 생성하고, 결과를 Domain Module 내부에 5벌 복제하지 않는다. 기본 Sample은 특정 회원·계좌·보험 업무가 아니라 CRUD, Search, Paging, Validation, Commit/Rollback, Optimistic Lock, Duplicate, Local/Remote Call, Standard Header, transactionGlobalId, Error Mapping, Idempotency와 Audit/Masking을 검증하는 동일 논리 모델이다. CPF 고정 Platform Module resource와 무제한 확장되는 Generated Domain Metadata/Template의 Ownership을 분리한다.
- 이유: 현재 Repository의 예시 Domain에 Generator가 종속되는 것을 막고, 임의 고객 Domain을 동일 Java 구조와 Vendor Template 계약으로 재현 가능하게 생성하기 위해서다.

## DEC-017 현행 설치 DB 객체의 최소화 판정

- 상태: `완료`
- 결정: 현재 Empty Install의 Table, Sequence, Constraint, Index와 Product Seed는 과거 Dump나 Historical Migration에 존재한다는 이유만으로 유지하지 않는다. 각 객체는 최신 정본의 Owner 책임과 실제 Java/MyBatis/Repository/Installer/Framework 동적 Consumer 중 하나로 존재 이유가 확인되어야 한다. 소비자가 없고 활성 원장과 중복되는 객체는 현행 설치 경로에서 제거하며, 정본 요구가 있으나 Consumer가 미완성인 객체는 삭제 대신 `부분 구현`으로 관리한다. MBR/ACC는 단일 `*_sample_item` 공통 Template로 전환하고, BZA의 고객·상품·주문 원장은 기본 제품에서 제거하며, EXS는 기관별 연계·실행·결과 불명 복구에 필요한 최소 구조만 둔다. MariaDB Spring Batch 객체는 사용 중인 Spring Batch Version의 공식 MariaDB Schema 계약을 따른다.
- 이유: 추정성 Schema와 중복 원장, 사용되지 않는 Seed·Index를 제품 Baseline에 고착시키지 않으면서도 보안·운영 정본 객체를 단순 문자열 검색만으로 잘못 삭제하지 않기 위해서다.
