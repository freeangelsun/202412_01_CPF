# CPF 도구 상세 참조

[← 문서 홈](README.md) · [제품 소개](../../README.md) · [구조와 배포](CPF_ARCHITECTURE_AND_TOPOLOGY_GUIDE.md) · [용어와 계약](CPF_TERMINOLOGY_AND_CONTRACT_REFERENCE.md)

> **대상**: 명령 실행자, 자동화 개발자, 장애 분석자
> **목적**: 각 도구의 매개변수·입력·출력·부작용·종료 코드·복구 방법을 정확히 확인한다.
> **관련 문서**: [도구 운영](CPF_TOOLS_GUIDE.md) · [테스트와 검증 증적](CPF_TEST_AND_EVIDENCE_GUIDE.md)

---

## 1. 공통

모든 명령은 저장소 Root에서 실행한다.

```powershell
git rev-parse HEAD
git status --short
```

인증정보는 환경변수 또는 비밀값 공급자로 주입한다.

## 2. `create-domain.ps1`

### 목적

신규 생성 업무영역을 생성한다.

### 핵심 매개변수

| 매개변수 | 설명 |
|---|---|
| `DomainName` | 읽을 수 있는 업무명 |
| `SystemCode` | 3자리 대문자 코드 |
| `ModuleName` | 모듈 |
| `PackageName` | Java 패키지 |
| `DatabaseVendor` | mariadb/postgresql/oracle |
| `Capabilities` | 선택 기능 |
| `Port` | 실행 환경 Port |
| `DryRun` | Plan |
| `Apply` | 실제 생성 |
| `GeneratePatch` | Patch 생성 |
| `AllowReserved` | 예약 코드 예외 |

### 예

```powershell
pwsh -File .\cpf-tools\generator\create-domain.ps1 `
  -DomainName payment `
  -SystemCode PAY `
  -DatabaseVendor postgresql `
  -Capabilities "database,remote-call,messaging" `
  -DryRun
```

### 실패

- 식별 충돌
- Port/경로
- DB/스키마
- 예약 코드
- 사용자 파일 덮어쓰기
- 내부 Import

## 3. `sync-database-artifacts.ps1`

### 목적

Canonical DB 변경을 공급자 묶음과 생성 업무영역에 동기화한다.

```powershell
pwsh -File .\cpf-tools\scripts\sync-database-artifacts.ps1
```

### 출력

- checksum
- bundle
- manifest
- drift
- profile
- generated parity
- vendor parity

### 실패

- FK Cycle
- 존재하지 않는 Column
- Identity 불일치
- 공급자 산출물 정본 불일치
- 체크섬 불일치

## 4. `build-all-install-sql.ps1`

```powershell
pwsh -File .\cpf-tools\scripts\build-all-install-sql.ps1
```

생성:

- provision
- empty install
- product seed
- optional/test seed
- verify
- all install
- smoke

## 5. `initialize-cpf-database.ps1`

### 목적

Platform DB 설치.

```powershell
pwsh -File .\cpf-tools\scripts\initialize-cpf-database.ps1 `
  -All `
  -RequireRun
```

선택:

- `All`
- `SystemCode`
- `DomainName`
- `ProfilePath`
- `SeedMode`
- `RequireRun`

Partial Install과 정본 불일치를 실패시킨다.

## 6. `initialize-generated-domain-databases.ps1`

```powershell
pwsh -File .\cpf-tools\scripts\initialize-generated-domain-databases.ps1 `
  -SystemCode PAY `
  -Operation bootstrap `
  -Apply
```

`Operation` 값은 `bootstrap`, `verify`, `upgrade`, `rollback` 등 스크립트 도움말을 따른다.

## 7. `initialize-databases.ps1`

```powershell
pwsh -File .\cpf-tools\scripts\initialize-databases.ps1 `
  -Scope all `
  -All `
  -Apply
```

범위:

- platform
- generated
- all

## 8. `invoke-platform-database-migration.ps1`

### 사전 계획

```powershell
pwsh -File .\cpf-tools\scripts\invoke-platform-database-migration.ps1 `
  -Direction upgrade `
  -FromVersion 72 `
  -ToVersion 73 `
  -Modules batch
```

### 적용

```powershell
pwsh -File .\cpf-tools\scripts\invoke-platform-database-migration.ps1 `
  -Direction upgrade `
  -FromVersion 72 `
  -ToVersion 73 `
  -Modules batch `
  -Apply `
  -ConfirmApply `
  -ConfirmApplicationsStopped `
  -ConfirmRollbackReady `
  -ExpectedPlanSha256 <PLAN> `
  -BackupManifestPath <MANIFEST>
```

### 제약

- 범위와 단일 버전 동시 사용 금지
- 적용은 Plan 해시와 백업 필수
- 되돌리기 Pair 필수
- 체크섬 필수

## 9. `backup-cpf-database.ps1`

```powershell
pwsh -File .\cpf-tools\scripts\backup-cpf-database.ps1 `
  -Vendor mariadb `
  -Database admDB `
  -Host 127.0.0.1 `
  -Port 3306 `
  -User cpf_backup
```

출력:

- 백업
- 명세서
- SHA-256
- 민감정보 분류

## 10. `restore-cpf-database.ps1`

```powershell
pwsh -File .\cpf-tools\scripts\restore-cpf-database.ps1 `
  -Vendor mariadb `
  -Database admDB `
  -BackupFile .\adm.sql `
  -ConfirmRestore
```

거부:

- Confirmation 없음
- 명세서 없음
- 해시 불일치
- 공급자/DB 불일치

## 11. `verify-dr-restore.ps1`

```powershell
pwsh -File .\cpf-tools\scripts\verify-dr-restore.ps1 `
  -Database admDB `
  -Host 127.0.0.1 `
  -Port 3306 `
  -User cpf_verify
```

`VerifySql`과 `RunPlatformVerify`를 동시에 사용하지 않는다.

## 12. `sync-generated-domain-artifacts.ps1`

비교:

```powershell
pwsh -File .\cpf-tools\scripts\sync-generated-domain-artifacts.ps1 -Scope Database
```

적용:

```powershell
pwsh -File .\cpf-tools\scripts\sync-generated-domain-artifacts.ps1 `
  -Scope AllGeneratorOwned `
  -Apply
```

사용자 수정 파일 충돌 시 실패한다.

## 13. `check-generator-arbitrary-domain-parity.ps1`

```powershell
pwsh -File .\cpf-tools\scripts\check-generator-arbitrary-domain-parity.ps1
```

격리 Sandbox에서 임의 두 업무영역을 생성·비교·제거한다.

## 14. `start-cpf-local.ps1`

```powershell
pwsh -File .\cpf-tools\scripts\start-cpf-local.ps1
```

시작:

- ADM
- BZA
- 참조/생성 업무영역
- 선택 게이트웨이
- 로컬 등록부

환경 매개변수는 스크립트 도움말을 따른다.

## 15. `status-cpf-local.ps1`

```powershell
pwsh -File .\cpf-tools\scripts\status-cpf-local.ps1
```

프로세스, Port, 상태 점검, 로그 경로를 확인한다.

## 16. `stop-cpf-local.ps1`

```powershell
pwsh -File .\cpf-tools\scripts\stop-cpf-local.ps1
```

관리 대상 프로세스만 종료하고 임의 Java 프로세스를 Kill하지 않는다.

## 17. 배치 로컬

```powershell
pwsh -File .\cpf-tools\scripts\start-bat-local-distributed.ps1
pwsh -File .\cpf-tools\scripts\stop-bat-local-distributed.ps1
```

Control, 일정관리기, 작업자, 에이전트 역할을 분리한다.

## 18. `verify-full-product.ps1`

```powershell
pwsh -File .\cpf-tools\scripts\verify-full-product.ps1 `
  -WithDatabase `
  -WithGeneratorLifecycle `
  -WithBrowser `
  -RequireAll `
  -Profile local
```

결과:

- Java
- 프런트엔드
- DB
- 생성기
- 실행 환경
- 브라우저
- 산출물
- 검증 증적

## 19. `check-architecture-ownership.ps1`

검사:

- 역방향 의존
- 내부 Import
- 소유자 위반
- 순환
- 고정 업무영역
- 모듈 Naming

## 20. `check-repository-hygiene.ps1`

검사:

- build/log/tmp/zip/bak
- Root 문서
- 비밀값
- Dead 파일
- Stale 산출물
- 외부 실행 환경 Asset

## 21. `check-document-links.ps1`

README와 Guide의 상대 Link, Anchor와 대상 파일을 검사한다.

## 22. `check-source-documentation-standard.ps1`

공개 API, 주요 서비스/컨트롤러의 JavaDoc, OpenAPI와 설명을 검사한다.

## 23. `check-admin-data-safety.ps1`

```powershell
pwsh -File .\cpf-tools\scripts\check-admin-data-safety.ps1
```

- PII 마스킹
- Raw Data 경계
- 상태 Catalog
- 조회 Resource
- 이관/되돌리기
- Bundle Parity

## 24. `check-certificate-expiry.ps1`

```powershell
pwsh -File .\cpf-tools\scripts\check-certificate-expiry.ps1 `
  -인증서Path .\cert.pem `
  -WarnDays 30
```

개인 키를 출력하지 않는다.

## 25. `new-cpf-changeset.ps1`

```powershell
pwsh -File .\cpf-tools\scripts\new-cpf-changeset.ps1 `
  -ChangeSetId REL-20260730-01 `
  -SourceEnvironment dev `
  -TargetEnvironment staging `
  -Reason "정기 배포" `
  -Files @("README.md")
```

명세서에 Commit과 해시를 기록한다.

## 26. `verify-cpf-changeset.ps1`

```powershell
pwsh -File .\cpf-tools\scripts\verify-cpf-changeset.ps1 `
  -Manifest .\changeset.json `
  -ExpectedSourceEnvironment dev `
  -ExpectedTargetEnvironment staging
```

Commit/해시 불일치를 실패시킨다.

## 27. Gradle 산출물 Task

```powershell
.\gradlew.bat publishCpfVerifiedLocalPlatformArtifacts -PcpfArtifactMode=LOCAL_DEV
.\gradlew.bat verifyCpfLocalArtifactPropagation -PcpfArtifactMode=LOCAL_DEV
.\gradlew.bat publishCpfPlatformArtifacts -PcpfArtifactMode=REMOTE
.\gradlew.bat buildCpfOfflineArtifactBundle -PcpfArtifactMode=LOCAL_DEV
```

## 28. 결과 해석

상태:

- PASS
- FAIL
- SKIPPED
- BLOCKED
- NOT_APPLICABLE

`RequireAll`에서는 SKIPPED/BLOCKED를 성공으로 보지 않는다.

## 29. 검증 증적 필드

- tool
- version
- sourceCommit
- command
- parameters
- environment
- profile
- start/end
- exitCode
- result
- findings
- rawEvidence
- sanitized

## 30. 안전 규칙

- `-ErrorAction SilentlyContinue`로 실패 숨김 금지
- 도구가 소스를 자동 수정하여 Gate 통과 금지
- 이관 체크섬 자기 갱신 금지
- 비밀번호 임시파일 금지
- 운영 Drop/Reset 기본 금지

## 부록 A. 모든 도구가 문서화해야 하는 항목

- 목적과 대상 사용자
- 지원 운영체제와 필요한 실행 권한
- 매개변수 이름·형식·필수 여부·기본값·허용 범위
- 상호 배타·의존 매개변수
- 환경변수와 비밀값 참조
- 입력 파일과 스키마
- 생성·수정·삭제하는 파일·DB·프로세스
- 멱등성·동시 실행·재개 가능 여부
- 계획·미리보기와 적용 확인
- 표준 출력·로그·결과 보고서
- 종료 코드와 오류 코드
- 부분 실패, 복구, 되돌리기와 정리
- 정상·실패 예제

문서와 실행 도움말이 다르면 릴리스 결함이다. 명령 메타데이터에서 도움말과 참조 문서를 생성·검증해 차이를 차단한다.
