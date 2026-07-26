# CPF Generator Tool Guide

## 1. 목적
Generator는 신규 업무영역을 CPF 제품 표준과 동일한 구조로 생성한다. 특정 Domain 이름에 종속된 복사본을 만들지 않고 하나의 Golden Template을 사용한다.

## 2. Canonical Command
`cpf-tools/generator/create-domain.ps1`가 정본 구현이다. 별도 launcher가 있더라도 정본 로직을 복제하지 않는다.

## 3. 핵심 입력
- `DomainName`: 사람이 읽는 업무명.
- `SystemCode`: 내부 3자리 대문자 코드.
- `ModuleName`/`ModuleCode`: build/deploy 식별.
- `PackageName`/`BasePackage`: Java package.
- `SchemaName`, `TablePrefix`: DB 충돌 방지.
- `Port`: 독립 실행 시 포트.
- `DatabaseVendor`, Host/Port/Name/Schema/User 계열: DB 설정.
- `Capabilities`: 필요한 기능 묶음.
- `Batch`, `CenterCut`, `External`, `Messaging`, `File`, `SecurityAudit`, `Ui`, `BzaMenu`: 선택 capability.
- `ProductionProfile`: 운영 profile 생성 여부.
- `ProvisionDatabase`: DB Provision 포함 여부.
- `DryRun`: 파일을 쓰지 않고 계획/충돌 검사.
- `GeneratePatch`: patch 산출.
- `Apply`: 실제 적용.
- `AllowReserved`: 예약 코드 예외. 일반 사용 금지.

실제 parameter 이름과 default는 Script를 최종 정본으로 확인한다.

## 4. Dry-run 우선
처음에는 반드시 DryRun으로 생성 예정 Module/Package/SystemCode/Route/SQL/DB collision을 확인한다. 충돌을 overwrite로 해결하지 않는다.

## 5. 생성 산출물
업무 Domain 특성에 따라 Module, Java package, config, transaction catalog, DB source/migration, test, OpenAPI/guide/EDU 연결을 생성한다. Framework 공통 계약을 별도 복제하지 않는다.

## 6. Public Boundary
생성 소스는 `com.cpf.core.api.*`, `com.cpf.core.spi.*`를 사용한다. `com.cpf.core.common.*` internal import는 생성 단계 또는 architecture gate에서 실패해야 한다.

## 7. DB Vendor
Vendor capability는 `database-source-plan.json`과 실제 구현 상태를 확인한다. MariaDB만 구현된 상황에서 Oracle/PostgreSQL 이름만 바꾼 SQL을 생성하지 않는다. 미지원 Vendor 선택은 명확히 실패한다.

## 8. EXS 정책
EXS는 고정 제품 Module이 아니라 생성 가능한 업무영역 예다. Platform fresh install이 EXS DB 존재를 요구하지 않는다. EXS를 생성할 때도 다른 Generated Domain과 같은 Template을 사용한다.

## 9. 사용자 수정 영역
Generator 관리영역과 사용자 소스영역을 분리한다. 재실행이 고객 코드를 무조건 overwrite하면 안 된다. 변경된 파일은 conflict/merge plan을 보여준다.

## 10. 검증 시나리오
두 개 이상의 서로 다른 Domain/SystemCode로 다음을 수행한다.
1. DryRun 성공.
2. Create/Apply.
3. 생성 Module build/test.
4. 동일 입력 재실행 충돌.
5. SystemCode 중복.
6. Package/Module/Route/DB 충돌.
7. reserved code 거부.
8. remove/clean 후 재생성.
9. internal import 0.
10. DB/OpenAPI/Test/Config 정합성.

## 11. 공통 계약 변경 시
Paging, Header, transaction ID, Error, DB source policy, Secret/Tenant/Retention API가 바뀌면 Generator가 새 Domain에 같은 표준을 생성하는지 같은 작업 범위에서 확인한다.

## 12. Evidence
Generator Evidence에는 기준 SHA, 전체 command, parameter(credential 제외), 생성 file list, collision test, build/test 결과, DB artifact 비교를 남긴다.
