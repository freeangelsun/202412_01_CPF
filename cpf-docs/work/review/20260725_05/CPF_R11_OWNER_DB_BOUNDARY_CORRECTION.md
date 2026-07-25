# CPF R11 Owner DB Boundary Correction

## 1. 발견 경위

R11 Public Boundary 보정 적용 후 `post-apply-r11.ps1`에서 Public Boundary Gate는 모두 통과했다. 이어진 `check-r11-common-capabilities.ps1`가 `AdmJdbcConfig`에서 BAT/MBR/REF DataSource 기반 `JdbcTemplate`을 생성하는 기존 구조를 실패로 검출했다.

이는 이전 R11 보고서의 “ADM이 다른 Owner DB를 직접 접근하는 신규 코드를 만들지 않았다”라는 표현으로는 부족했다. 실제 Source에는 기존 direct-owner DB access와 BAT Runtime 중복 소유가 남아 있었으므로 해당 표현을 정정한다.

## 2. Architecture 보정

### ADM

- ADM은 `admDataSource/admJdbcTemplate/admTransactionManager`만 소유한다.
- MBR 회원/역할 DB를 직접 SQL로 조회·변경하지 않는다.
- BAT Scheduler/JobRepository Runtime을 직접 소유하지 않는다.
- Owner 데이터 접근은 Public Operations Port 또는 Remote Adapter를 통한다.

### MBR

- `mbrJdbcTemplate`과 `mbrTransactionManager`는 MBR Module 안에서만 구성한다.
- 회원/역할 운영 조회·변경 SQL과 transaction을 `MbrOwnerAdminOperationsService`가 소유한다.
- 분리 WAS용 내부 운영 Controller는 `CpfSharedApi`로 선언하고 ADM caller만 허용한다.

### BAT

기존 정식 Owner 구현을 사용한다.

- `BatBatchScheduler`
- `BatBatchScheduleService`
- `BatBatchExecutionTargetService`
- `BatOperationFacade implements CpfBatchOperationsPort`

ADM에 중복되어 있던 Scheduler/BatchRepository 구현은 cleanup에서 제거한다.

### Core

`CpfOwnerAdminOperationsPort`, `CpfOwnerAdminQuery`, `CpfOwnerAdminCommand`는 topology-independent 운영 계약만 제공한다. 특정 MBR 테이블이나 ADM 구현을 Core에 넣지 않는다.

## 3. Same JVM / Remote WAS

- Same JVM: 동일 Spring Context에 Owner Port Bean이 존재하면 ADM이 직접 Port를 사용한다.
- Remote WAS: Owner Port Bean이 없으면 ADM Remote Adapter가 `CpfServiceCaller`를 사용해 MBR 내부 운영 API를 호출한다.
- 따라서 ADM Service는 topology에 따라 Owner schema/JdbcTemplate를 바꾸지 않는다.

## 4. Gate 보강

`check-r11-common-capabilities.ps1`는 ADM Source에서 다음을 실패 처리한다.

- 다른 Owner 이름의 DataSource/JdbcTemplate/TransactionManager
- `spring.datasource.<owner>` 직접 구성
- ADM에 남은 BAT Scheduler/BatchRepository Runtime artifact

`check-r11-runtime-entrypoints.ps1`는 BAT 정식 Scheduler/Service/OperationFacade 존재와 ADM Runtime 중복 부재를 확인한다.

## 5. 현재 판정

- Public Boundary: **완료** — 사용자 실행 Gate PASS
- `cpf-common.utils` Consumer 제거: **완료** — 사용자 실행 Gate PASS
- ADM Owner DB Source Ownership 보정: **부분 구현 → Source 보완 완료, 사용자 Repository Gate 재실행 필요**
- Full Gradle/Spring/DB/Remote E2E: **미검증**

실행하지 않은 Runtime 검증을 성공으로 기록하지 않는다.
