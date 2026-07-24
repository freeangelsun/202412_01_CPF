# CPF Installation Guide

## 1. 목적과 적용 범위

이 문서는 Core Platform Framework(CPF)의 신규 설치, Vendor Pack 선택, 빈 DB 구축,
재설치와 설치 후 검증 절차를 정의합니다. 개발 중 문서 정본은 Markdown이며 실제 실행하지 않은
DB Vendor, Runtime 또는 배포 방식은 지원 완료로 표기하지 않습니다.

현재 검수 기준에서 MariaDB는 실제 실행 검증 대상입니다. MySQL, PostgreSQL, Oracle,
SQL Server는 중앙 Vendor Pack 계약을 동일하게 따르되 실제 환경 Evidence가 없으면 `미검증`입니다.

## 2. 지원 배포 모델

CPF의 목표 배포 모델은 다음과 같습니다.

- Executable JAR
- External WAS WAR
- Modular Monolith
- Independent Microservices
- 독립 Batch/Agent/Runner/Worker Process
- ADM/BZA Static Web Artifact와 API 독립 배포
- Container/Kubernetes

배포 방식이 Public Contract, 표준 Header, 오류 규격, 권한, Audit 또는 Local/Remote 호출 계약을 바꾸면 안 됩니다.

## 3. 사전 조건

### 3.1 Runtime

- JDK 25
- Gradle Wrapper
- PowerShell 7(`pwsh`) — Windows 설치/검증 Script 기준
- Linux 또는 Windows Server
- NTP 시간 동기화
- 운영 Log와 spool을 위한 쓰기 가능한 경로

### 3.2 Database

제품 목표 Vendor:

- MariaDB
- MySQL
- PostgreSQL
- Oracle
- SQL Server

실행 검증 여부는 Vendor별 Evidence로 판정합니다. MariaDB 결과를 다른 Vendor의 성공 근거로 재사용하지 않습니다.

### 3.3 선택 인프라

- Redis
- Kafka 또는 호환 Broker
- SFTP
- Secret Manager
- Reverse Proxy / Load Balancer

## 4. Secret과 Service Account

DB Root/Migration/Application Password, Token, Private Key를 Repository, 명령 이력,
Evidence 또는 Guide에 평문으로 저장하지 않습니다.

권장 분리:

- Provision/Admin DB identity
- Migration DB identity
- Application DB identity
- 운영 Read-only identity
- Broker producer/consumer identity
- SFTP identity
- Secret/Certificate identity

Application identity에는 DDL/DROP/CREATE USER 권한을 부여하지 않습니다.

## 5. 중앙 DB Vendor Pack 선택

Physical Vendor SQL의 제품 정본은 다음 중앙 Pack입니다.

```text
cpf-tools/db/vendor/<vendor>/
  pack.json
  provision/
  install/
  seed/
  migration/
  runtime/<owner>/
  verify/
  rollback/
  domain-template/
```

Module `src/main/resources`에 Vendor별 SQL/MyBatis Pack을 다시 복제하지 않습니다.

Repository Root에서 선택 상태와 parity를 확인합니다.

```powershell
pwsh -File scripts/select-db-vendor-resources.ps1 -Vendor mariadb
```

격리된 Runtime resource overlay가 필요한 경우 Source Tree를 덮어쓰지 않고 `build/` 아래에 생성합니다.

```powershell
pwsh -File scripts/select-db-vendor-resources.ps1 -Vendor mariadb -AssembleOverlay -RequireExecutable
```

Runtime은 선택된 Pack의 실제 `pack.json` root를 `cpf.db.resource-root`로 받아야 합니다.
해당 값이 없거나 `cpf.db.vendor`와 Pack의 Vendor가 다르면 fail-fast해야 합니다.
삭제된 Module-local Vendor resource를 fallback으로 복구해서는 안 됩니다.

## 6. Canonical SQL과 Generated Bundle

Schema 설계 정본:

```text
specs/sql/10_cpf_schema.sql
specs/sql/20_cmn_schema.sql
specs/sql/30_adm_schema.sql
specs/sql/35_bat_schema.sql
specs/sql/40_business_modules_schema.sql
specs/sql/45_external_schema.sql
```

Lifecycle 책임:

```text
provision
install
product-seed
optional-sample-seed
test-seed
verify
reset
migration
rollback/recovery
```

Split SQL 변경 후 Generated Bundle과 MariaDB 중앙 Pack mirror를 반드시 재생성합니다.

```powershell
pwsh -File scripts/build-all-install-sql.ps1
```

`00_empty_install.sql`, `00_product_seed.sql`, `00_verify.sql` 같은 Generated 파일을
Split SQL과 별도의 설계 정본처럼 직접 편집하지 않습니다.

## 7. 신규 Empty Install

첫 설치는 Reset을 선행 조건으로 요구하지 않습니다.

Credential 값은 환경변수 또는 안전한 Secret 주입을 사용하고, Guide/Evidence에는 값 자체를 기록하지 않습니다.
Initializer의 실제 인자는 `scripts/initialize-cpf-database.ps1`의 계약을 따릅니다.

예시:

```powershell
$env:CPF_DB_VENDOR = "mariadb"
$env:CPF_DB_HOST = "<host>"
$env:CPF_DB_PORT = "<port>"
$env:CPF_DB_ROOT_USERNAME = "<admin-user>"
# Password 환경변수 값은 안전한 방식으로 별도 주입
pwsh -File scripts/initialize-cpf-database.ps1 -RequireRun
```

Initializer는 최소 다음을 검증해야 합니다.

- CPF-owned Schema exact allowlist
- Table/PK/FK/UK/Check/Index/Constraint
- Product Seed
- Service User/Grant
- Schema/Object inventory
- Verify SQL
- 민감정보가 제거된 실행 결과

현재 Overlay의 `123 CREATE TABLE`은 정적 설계 수치일 뿐입니다.
실제 MariaDB에서 동일 Object가 생성되었다는 Evidence가 생기기 전에는 Runtime 완료가 아닙니다.

## 8. Reset과 Reinstall

Reset은 첫 설치 경로가 아니라 재설치/복구 검증 경로입니다.

먼저 Dry-run:

```powershell
pwsh -File scripts/reset-cpf-databases.ps1
```

Apply는 정확한 Allowlist와 명시적 Confirmation을 모두 요구합니다.

```powershell
pwsh -File scripts/reset-cpf-databases.ps1 -Apply -Confirmation DROP_CPF_ALLOWLIST_ONLY
```

Reset Script가 CPF Allowlist 밖의 Application Schema를 DROP하면 실패입니다.
필요한 데이터가 있으면 Apply 전에 Backup/Restore Point를 확보합니다.

Reset 후 다시 Empty Install → Seed → Verify를 수행하여 재설치 반복성을 확인합니다.

## 9. Module Build

Repository Root:

```powershell
.\gradlew.bat clean build --no-daemon
```

또는 Linux/macOS:

```bash
./gradlew clean build --no-daemon
```

검증 대상은 단순 Compile에 그치지 않습니다.

- Unit / Integration Test
- Architecture/Ownership Gate
- UTF-8
- SQL canonical/parity
- OpenAPI source/runtime
- Frontend build
- repository hygiene
- Evidence consistency

DOCX/PDF publication 검증은 개발 중 일반 Quality Gate와 분리하고 최종 정본화 단계에서 수행합니다.

## 10. Runtime 기동

Executable JAR 또는 External WAS의 실제 Artifact 이름과 Port는 각 Module Build/Config 정본을 따릅니다.
Guide에서 존재하지 않는 배포 경로나 가상의 Task 이름을 만들지 않습니다.

Runtime 검증은 최소 다음을 포함합니다.

- Gateway/API
- Local/Remote Service Call
- Registry/Health/Failover
- 표준 Header/transactionGlobalId/Trace
- DB Read/Write
- File/DB Log
- Broker/Outbox/Inbox/DLQ
- Batch/Agent/Center-Cut
- ADM/BZA login/RBAC/Approval
- External integration mock
- Audit/Masking

## 11. 배포 Directory와 Environment

현재 Repository의 배포 보조 정본은 `deploy/` 아래의 실제 파일만 사용합니다.

```text
deploy/env/
deploy/inventory/
```

Repository에 존재하지 않는 `cpf-deployment/was`, `cpf-deployment/docker/compose.yml` 같은 경로를
공식 설치 경로로 문서화하지 않습니다. Container/Kubernetes 배포 파일이 제품화되면 실제 Source와
검증 Evidence가 존재하는 경로만 Guide에 추가합니다.

## 12. Owner Schema 원칙

- `cpfDB`: 기술 Framework Runtime
- `cmnDB`: 기본 제품은 `cmn_sample_item` 1개
- `admDB`: 플랫폼 운영 Control Plane
- `bzaDB`: 고객 업무 관리자/조직/업무결재
- `batDB`: Batch/Scheduler/Agent/Runner/Worker/Center-Cut
- `mbrDB`, `accDB`, `refDB`: Reference/Generator 검증 목적에 맞는 최소 구조
- `exsDB`: 현재 정본상 `cpf-external` 대외연계 제품 Owner 구조

ADM이 `batDB`, `refDB`, 업무 Domain DB를 직접 수정하는 구조는 설치 편의 때문에 허용하지 않습니다.

## 13. Migration / Upgrade / Rollback

- 이미 적용된 Historical Flyway 파일은 불변
- 신규 변경은 새 Migration으로 추가
- V6/V29처럼 checksum 불일치가 있으면 원인을 먼저 확정
- checksum을 맞추기 위한 임의 편집 금지
- Empty Install 최종 상태와 Upgrade 최종 상태 parity 검증
- rollback 불가능 변경은 Forward Recovery + Backup/Restore 전략을 명시

## 14. 설치 후 Evidence

민감정보를 제거한 Evidence에는 최소 다음이 있어야 합니다.

- 기준 Commit
- PC/환경 구분(HOME/COMPANY 등)
- Vendor/Profile
- 실행 명령
- 시작/종료 시각
- Reset 여부
- Schema/Object inventory
- Seed/Grant 검증
- Module boot 결과
- Runtime/API/Batch/Admin/Browser smoke
- 실패/Blocker
- Stale 여부

한 PC의 Runtime 성공을 다른 PC의 성공으로 승계하지 않습니다.

## 15. Hardening

- 기본 Password/테스트 계정 제거 또는 강제 초기 변경
- TLS/mTLS 정책
- 관리자 접근망 제한
- Cookie/CSP/보안 Header
- Upload/Download 권한과 크기 제한
- Secret/Certificate Rotation
- Audit 보존과 변조 방지
- Log/Spool 파일 권한
- Backup/Restore/DR 검증

## 16. 완료 금지 조건

다음 상태에서는 DB/설치 Requirement를 `완료`로 표시하지 않습니다.

- Split DDL과 Generated Bundle이 다름
- 중앙 Vendor Pack과 Runtime Consumer가 연결되지 않음
- Module-local Vendor fallback으로 우연히 통과함
- MariaDB만 실행하고 다른 Vendor까지 완료 처리함
- Historical Migration 무결성 문제가 미해결
- Reset/Upgrade/Rollback/Restore를 실행하지 않았는데 성공으로 기록함
- Evidence가 현재 Commit/PC/실행환경과 일치하지 않음
