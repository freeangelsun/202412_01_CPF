# CPF 도구 운영 가이드

## 1. 목적

`cpf-tools`는 임시 Script 보관소가 아니라 CPF 제품을 생성·설치·검증·공급·이관·복구하기 위한 공식 Tooling 영역이다.

## 2. Directory 책임

| 경로 | 책임 |
|---|---|
| `cpf-tools/config` | Profile, Coverage, Source Plan |
| `cpf-tools/db/metadata` | Canonical Schema와 정책 |
| `cpf-tools/db/vendor` | Vendor Source와 Lifecycle |
| `cpf-tools/db/generated` | 생성 Manifest |
| `cpf-tools/generator` | 업무영역 Generator |
| `cpf-tools/scripts` | 설치·검증·동기화 도구 |
| `cpf-tools/build/platform-bom` | BOM |
| `cpf-tools/build/gradle-plugin` | Convention Plugin |
| `cpf-tools/runtime` | Local Runtime Assembly |

## 3. 도구 분류

| 분류 | 의미 |
|---|---|
| `DEV_ONLY` | 개발자 Local 편의 |
| `QUICK` | 저비용 정적 Gate |
| `VERIFY` | 변경 단위 검증 |
| `FULL` | Release 후보 통합 검증 |
| `CI_RELEASE` | CI와 Release |
| `PRODUCT_ADMIN_TOOL` | 고객 설치·운영 도구 |

개발 Gate를 운영 Runtime에 포함하지 않는다.

## 4. 공통 실행 원칙

- Repository Root에서 실행
- `git rev-parse HEAD` 기록
- Clean/Dirty 상태 기록
- Credential을 Argument에 넣지 않음
- Dry Run 우선
- Apply 명시 확인
- Exit Code 확인
- 실패 숨김 금지
- Evidence 저장
- UTF-8 without BOM

## 5. Help

```powershell
Get-Help .\cpf-tools\scripts\<script>.ps1 -Detailed
```

문서와 Script Parameter가 다르면 같은 변경에서 수정한다.

## 6. Generator

```powershell
pwsh -File .\cpf-tools\generator\create-domain.ps1 `
  -DomainName payment `
  -SystemCode PAY `
  -DryRun
```

상세는 Generator Guide를 참고한다.

## 7. DB 동기화

```powershell
pwsh -File .\cpf-tools\scripts\sync-database-artifacts.ps1
```

단계:

- Migration Checksums
- Install Bundle
- Schema Manifest
- Drift
- Profile
- Generated Domain
- Vendor Parity

## 8. DB 설치

```powershell
pwsh -File .\cpf-tools\scripts\initialize-cpf-database.ps1 -All -RequireRun
```

Generated Domain:

```powershell
pwsh -File .\cpf-tools\scripts\initialize-generated-domain-databases.ps1 `
  -All `
  -Operation bootstrap `
  -Apply
```

## 9. Migration

```powershell
pwsh -File .\cpf-tools\scripts\invoke-platform-database-migration.ps1 `
  -Direction upgrade `
  -FromVersion 72 `
  -ToVersion 73 `
  -Modules batch
```

Dry Run Plan Hash 검토 후 Apply한다.

## 10. Backup/Restore

```powershell
pwsh -File .\cpf-tools\scripts\backup-cpf-database.ps1 ...
pwsh -File .\cpf-tools\scripts\restore-cpf-database.ps1 ... -ConfirmRestore
pwsh -File .\cpf-tools\scripts\verify-dr-restore.ps1 ...
```

## 11. Runtime

```powershell
pwsh -File .\cpf-tools\scripts\start-cpf-local.ps1
pwsh -File .\cpf-tools\scripts\status-cpf-local.ps1
pwsh -File .\cpf-tools\scripts\stop-cpf-local.ps1
```

Batch:

```powershell
pwsh -File .\cpf-tools\scripts\start-bat-local-distributed.ps1
pwsh -File .\cpf-tools\scripts\stop-bat-local-distributed.ps1
```

## 12. Aggregate Gate

### QUICK

개발 중 반복 실행:

- Syntax
- Architecture
- Secret
- Document Link
- Repository Hygiene
- Frontend Route
- SQL Parse

### VERIFY

작업 단위:

- 영향 Module Test
- Packaging
- DB Sync
- Generator
- Focused Runtime
- Evidence

### FULL

Release 후보:

- Clean Build
- Frontend
- 3 Vendor
- Generator Lifecycle
- Multi-instance
- Fault
- Browser
- Artifact
- Evidence

## 13. 대표 Gate

```powershell
pwsh -File .\cpf-tools\scripts\check-architecture-ownership.ps1
pwsh -File .\cpf-tools\scripts\check-repository-hygiene.ps1
pwsh -File .\cpf-tools\scripts\check-document-links.ps1
pwsh -File .\cpf-tools\scripts\check-source-documentation-standard.ps1
pwsh -File .\cpf-tools\scripts\check-admin-data-safety.ps1
```

## 14. 통합 검증

```powershell
pwsh -File .\cpf-tools\scripts\verify-full-product.ps1 `
  -WithDatabase `
  -WithGeneratorLifecycle `
  -WithBrowser `
  -RequireAll `
  -Profile local
```

`RequireAll`에서는 Skip도 전체 성공으로 인정하지 않는다.

## 15. Artifact 공급

```powershell
.\gradlew.bat publishCpfVerifiedLocalPlatformArtifacts -PcpfArtifactMode=LOCAL_DEV
.\gradlew.bat publishCpfPlatformArtifacts -PcpfArtifactMode=REMOTE
.\gradlew.bat buildCpfOfflineArtifactBundle -PcpfArtifactMode=LOCAL_DEV
```

## 16. 변경집합

환경 Promotion:

```powershell
pwsh -File .\cpf-tools\scripts\new-cpf-changeset.ps1 ...
pwsh -File .\cpf-tools\scripts\verify-cpf-changeset.ps1 ...
```

Manifest는 Commit과 파일 Hash를 기록한다.

## 17. 인증서

```powershell
pwsh -File .\cpf-tools\scripts\check-certificate-expiry.ps1 `
  -CertificatePath .\certificate.pem `
  -WarnDays 30
```

Private Key를 읽지 않는다.

## 18. Generated Domain 동기화

```powershell
pwsh -File .\cpf-tools\scripts\sync-generated-domain-artifacts.ps1 -Scope Database
pwsh -File .\cpf-tools\scripts\sync-generated-domain-artifacts.ps1 -Scope AllGeneratorOwned -Apply
```

사용자 수정 파일을 기본 덮어쓰지 않는다.

## 19. Repository Hygiene

검출 대상:

- build
- logs
- tmp
- zip
- bak
- patch
- Secret
- Stale Root 문서
- Dead Source
- 외부 CDN
- 잘못된 Module

## 20. 도구 출력 표준

JSON 결과:

- tool
- version
- sourceCommit
- startedAt
- finishedAt
- command
- environment
- status
- exitCode
- findings
- evidence
- sanitized

## 21. 실패 분류

- SOURCE_DEFECT
- ENVIRONMENT
- CREDENTIAL
- UNSUPPORTED
- DRIFT
- EXTERNAL_DEPENDENCY
- SECURITY_POLICY
- USER_CANCELLED

실패를 0건 성공으로 바꾸지 않는다.

## 22. 안전한 Apply

Destructive Tool은 다음을 요구한다.

- Dry Run
- Expected Plan Hash
- Confirmation
- Target Allowlist
- Backup
- Rollback
- Permission
- Reason
- Audit

## 23. 여러 작업 환경

작업 시작:

```powershell
pwsh -File .\cpf-tools\scripts\check-work-context.ps1
```

작업 종료:

- Source/API/SQL/Test
- Guide
- Handover
- Verification Plan
- Evidence
- Clean Working Tree

## 24. Tool 문서화 기준

공식 Entry마다 다음을 제공한다.

- 목적
- Parameter
- Default
- 조합 제약
- 환경변수
- 입력
- 출력
- 변경 영향
- 정상 예
- 실패 예
- 재실행
- 복구
- Evidence

## 25. 체크리스트

- [ ] Tool 분류가 있다.
- [ ] Dry Run과 Apply가 분리된다.
- [ ] Credential을 출력하지 않는다.
- [ ] 실패 Exit Code가 정확하다.
- [ ] 결과 JSON과 Evidence가 있다.
- [ ] 문서와 Parameter가 일치한다.
- [ ] 운영 Runtime에 개발 Gate가 포함되지 않는다.
