# CPF Generator + Domain DB 실검증 Runbook

이 문서는 ChatGPT R4 구조를 적용한 뒤 사용자와 함께 실제 MariaDB에서 수행할 검증 순서다. DB 삭제는 사용자가 직접 수행한다.

## 1. 사전 확인

```powershell
cd C:\dev\projects\jck\202412_01_CPF
git status
git rev-parse HEAD
java -version
pwsh --version
mariadb --version
```

DB Credential은 환경변수로만 설정하고 Evidence에 원문을 남기지 않는다.

## 2. 중앙 SQL 정본 재생성

```powershell
pwsh -File .\cpf-tools\scripts\build-all-install-sql.ps1
```

이 단계가 실패하면 DB를 만들지 않는다.

## 3. CPF 전체 DB 재생성

사용자가 기존 CPF DB를 직접 삭제한 뒤 기존 공식 초기화 Shell을 사용한다.

```powershell
$env:CPF_DB_VENDOR = "mariadb"
$env:CPF_DB_HOST = "127.0.0.1"
$env:CPF_DB_PORT = "3306"
$env:CPF_DB_ROOT_USERNAME = "root"
$env:CPF_DB_ROOT_PASSWORD = "<local-secret>"
$env:CPF_DB_MIGRATION_PASSWORD = "<local-secret>"
$env:CPF_DB_APP_PASSWORD = "<local-secret>"

pwsh -File .\cpf-tools\scripts\initialize-cpf-database.ps1 -RequireRun
```

확인 항목:
- Provision → Empty Install → Product Seed → Verify 성공
- `mbrDB.mbr_sample_item`
- BZA Role/Menu/Project Setting Product Metadata
- 조직/직급/직책/결재 DDL
- transaction_id 컬럼/Index
- Runtime 계정 DDL 권한 없음

## 4. 임의 Generated Domain Dry-Run

```powershell
pwsh -File .\cpf-tools\scripts\create-domain.ps1 `
  -DomainName "lng" -SystemCode "LNG" -DatabaseVendor "mariadb" -DryRun
```

## 5. Generated Domain Source + DB 생성

```powershell
$env:CPF_DOMAIN_DB_ADMIN_USERNAME = "root"
$env:CPF_DOMAIN_DB_ADMIN_PASSWORD = "<local-secret>"
$env:CPF_DOMAIN_DB_USERNAME = "cpf_lng_migration"
$env:CPF_DOMAIN_DB_PASSWORD = "<local-secret>"

pwsh -File .\cpf-tools\scripts\create-domain.ps1 `
  -DomainName "lng" `
  -SystemCode "LNG" `
  -DatabaseVendor "mariadb" `
  -ProvisionDatabase `
  -Apply
```

## 6. Generated Source/DB 검증

```powershell
pwsh -File .\cpf-tools\scripts\verify-domain.ps1 -DomainName "lng" -SystemCode "LNG"

pwsh -File .\cpf-tools\scripts\initialize-domain-database.ps1 `
  -DomainName "lng" -SystemCode "LNG" `
  -DatabaseVendor "mariadb" -Operation verify -Apply
```

## 7. Runtime CRUD

생성 Domain의 실제 port를 manifest에서 확인한다.

```powershell
Get-Content .\cpf-lng\manifest\domain-manifest.json
```

첫 터미널:

```powershell
.\gradlew.bat :cpf-lng:bootRun --args="--spring.profiles.active=local"
```

둘째 터미널:

```powershell
pwsh -File .\cpf-tools\scripts\smoke-generated-domain-lifecycle.ps1 `
  -DomainName "lng" -SystemCode "LNG" `
  -Apply -RunHttpCrud -ServiceBaseUrl "http://localhost:<manifest-port>"
```

확인:
- Create/Read/Update/Delete
- optimistic version
- idempotency
- transaction_id 34자리
- 파일로그와 DB 로그의 동일 transactionId

## 8. Gateway 호출

Gateway가 기동된 환경에서 같은 거래를 두 방식으로 검증한다.

```text
X-Cpf-Standard-Execution-Id Header
/cpf/execute/{standardExecutionId} URI
```

두 방식 모두 동일 transactionId 정책, 표준 오류, Header 전파를 가져야 한다.

## 9. 삭제 Dry-Run

```powershell
pwsh -File .\cpf-tools\scripts\remove-domain.ps1 `
  -DomainName "lng" -SystemCode "LNG" -DryRun
```

사용자 수정 파일/외부 Consumer가 있으면 실제 삭제하지 않는다.

## 10. Source 삭제와 재생성

Generated Source만 제거한다. DB는 자동 삭제하지 않는다.

```powershell
pwsh -File .\cpf-tools\scripts\remove-domain.ps1 `
  -DomainName "lng" -SystemCode "LNG"
```

이후 동일 Metadata로 다시 생성하고 `verify-domain`을 수행한다.

## 11. 두 Domain parity

`lng/LNG`, `ing/ING`를 같은 Capability로 생성한 뒤:

```powershell
pwsh -File .\cpf-tools\scripts\check-generated-domain-parity.ps1 `
  -ReferenceDomain "lng" -CandidateDomain "ing"
```

차이가 나면 생성기 또는 Template drift로 판정한다.

## 12. Evidence

최종 Evidence에는 다음만 저장한다.
- 기준 SHA
- 명령
- Java/Gradle/Node/MariaDB Version
- Profile
- 시작/종료 시각
- sanitized test/runtime/DB 결과
- transactionId 예시는 개인정보 없는 테스트 값

Credential, Token, 실제 고객 데이터는 저장하지 않는다.
