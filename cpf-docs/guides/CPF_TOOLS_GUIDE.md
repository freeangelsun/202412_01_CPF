# CPF Tools 운영·개발 가이드

## 1. 목적

`cpf-tools`는 제품 Runtime Module이 아니라 CPF 제품을 **생성, 설치, 검증, 동기화, 이관**하기 위한 공식 Tooling 영역이다. 임시 Script 보관소로 사용하지 않는다.

## 2. 디렉터리 책임

| 경로 | 책임 | 정본 여부 |
|---|---|---|
| `cpf-tools/config` | DB 설치 Profile, Vendor Coverage, Source Plan | 정본 |
| `cpf-tools/db/vendor/<vendor>/source` | 해당 Vendor Platform canonical SQL source | 구현 Vendor만 정본 |
| `cpf-tools/db/vendor/<vendor>/domain-template` | Generated Domain Golden DB template | 정본 |
| `cpf-tools/db/vendor/<vendor>/{install,seed,migration,rollback,verify}` | 배포 가능한 Vendor lifecycle pack | 생성/동기화 산출물 |
| `cpf-tools/db/generated/database-schema-manifest.json` | canonical schema metadata | 생성·추적 산출물 |
| `cpf-tools/generator` | 사용자 진입용 Generated Domain command | 정식 API |
| `cpf-tools/scripts` | 구현 script와 검증 gate | 내부 Tooling |
| `cpf-tools/build/gradle-plugin` | CPF Domain Convention Gradle Plugin 격리 Build | 정본 |
| `cpf-tools/build/platform-bom` | 배포용 CPF Platform BOM 격리 Build | 정본 |

`cpf-tools/db/source/mariadb`처럼 특정 Vendor만 별도 top-level source tree를 갖는 구조는 사용하지 않는다. Vendor source는 모두 `vendor/<vendor>/source` 경계 안에서 관리한다.

`cpf-tools/build`은 이름과 달리 재생성 산출물 디렉터리가 아니라 Build Tooling Source의
정식 Owner다. 그 아래 각 격리 Build의 `.gradle`, `build`, `bin`만 산출물로 취급한다.
Root `settings.gradle`은 두 격리 Build를 Composite Build로 직접 참조한다.

## 3. DB 수정의 유일한 완료 경로

Schema, Column, Index, FK, Seed, Migration, Runtime Mapper, Generated Domain metadata를 변경하면 반드시 다음을 실행한다.

```powershell
pwsh -ExecutionPolicy Bypass -File .\cpf-tools\scripts\sync-database-artifacts.ps1
```

실행 순서:

1. `generate-migration-checksums.ps1` — canonical Migration의 SHA-256 checksum 정본 자동 재생성
2. `build-all-install-sql.ps1` — canonical source에서 lifecycle bundle 재생성
3. `generate-database-schema-manifest.ps1` — Table/Column/Index/FK metadata 생성
4. `check-database-schema-drift.ps1` — tracked manifest와 재생성 결과 비교
5. `check-database-profile-standard.ps1` — profile, Generated Domain, EXS fixed residue, seed 정책 검사
6. `sync-generated-domain-artifacts.ps1` — 기존 Generated Domain의 DB/Generator-owned artifact parity 확인

각 단계는 별도 `pwsh` process로 실행한다. 하위 Script의 과거 native `$LASTEXITCODE`가 부모 결과로 오판되는 구조를 금지한다.

## 4. Platform DB Vendor 정책

- MariaDB Platform Pack: 현재 구현 대상.
- MySQL/PostgreSQL/Oracle/SQL Server Platform Pack: 구현 전에는 `not-implemented`이며 fail-closed.
- 다른 Vendor를 MariaDB SQL 복사/rename으로 완료 처리하지 않는다.
- 모든 Vendor는 `cpf-tools/db/vendor/<vendor>`라는 동일 ownership 경계를 사용한다.
- 지원 여부는 `database-vendor-coverage.json`과 실제 pack이 일치해야 한다.

## 5. Generated Domain

모든 업무 Domain은 같은 Golden Generator를 사용한다. 이름별 switch/if를 추가하지 않는다.

```powershell
pwsh -ExecutionPolicy Bypass -File .\cpf-tools\generator\create-domain.ps1 -DomainName payment -SystemCode PAY -Apply
pwsh -ExecutionPolicy Bypass -File .\cpf-tools\generator\create-domain.ps1 -DomainName external -SystemCode EXS -Apply
```

`EXS`는 고정 `cpf-external` Platform Module이 아니다. 사용자가 대외 업무 Domain이 필요하다고 결정한 프로젝트에서 다른 Generated Domain과 동일한 방식으로 생성한다. Platform 기본 install/seed에는 `exsDB`/`exs_*`를 넣지 않는다. 대외연계 공통 EDU는 `cpf-reference`가 소유한다.

생성기 충돌 검사는 특정 MariaDB SQL 파일을 직접 읽지 않고 `database-schema-manifest.json`과 기존 Domain manifest를 사용한다.

## 6. 설치

```powershell
# Platform 전체
pwsh -ExecutionPolicy Bypass -File .\cpf-tools\scripts\initialize-cpf-database.ps1 -All -RequireRun

# 특정 Platform Domain
pwsh -ExecutionPolicy Bypass -File .\cpf-tools\scripts\initialize-cpf-database.ps1 -SystemCode BAT -RequireRun

# Generated Domain
pwsh -ExecutionPolicy Bypass -File .\cpf-tools\scripts\initialize-generated-domain-databases.ps1 -SystemCode EXS -Operation bootstrap -Apply
```

기존 Table이 모두 존재하더라도 Column/Index/FK가 canonical metadata와 다르면 설치 성공으로 SKIP하지 않는다. Schema drift를 실패시키고 명시 Migration을 요구한다.

## 7. 안전 규칙

- Reset/Drop은 명시 승인·allowlist 없이 수행하지 않는다.
- Product Seed는 멱등이어야 하며 고객 업무 데이터를 덮어쓰지 않는다.
- Secret/Password를 Git-tracked Profile/Evidence에 저장하지 않는다.
- `build`, `logs`, 임시 ZIP, 패치 임시파일을 Repository 정식 산출물로 남기지 않는다.
- 실행하지 않은 DB/Browser/Runtime 검증은 `미검증`이다.

## 8. 변경 작업 체크리스트

1. Final Target과 Current Request의 Owner/정책 확인
2. canonical source 수정
3. Migration/rollback 필요성 판정
4. Generator/template 영향 반영
5. `sync-database-artifacts.ps1` PASS
6. 정적 gate PASS
7. 실제 DB install/upgrade/runtime 실행
8. Evidence 저장
9. Continuity State와 다음 작업서 갱신

## 9. 여러 PC/AI 작업 인수인계

작업 시작 시 아래 명령을 첫 Gate로 사용한다.

```powershell
pwsh -ExecutionPolicy Bypass -File .\cpf-tools\scripts\check-work-context.ps1
```

Gate가 확인하는 정본은 Final Target, Requirement Continuity Ledger, Current Work Request,
Decision Log, Continuity State와 현재 Git HEAD다. 작업자는 이전 채팅 보고만 읽고 구현을 시작하지 않는다.

작업 종료 시 다음을 함께 갱신한다.

- 실제 Source/API/SQL/Test
- `CPF_CURRENT_WORK_REQUEST.md`
- 제품 기능 변경이 있는 README/Guide
- 현재 작업 Handover
- 통합 검증 계획과 아직 실행하지 않은 시나리오
- Requirement 상태와 실제 Evidence

## 10. Generated Domain 기존 인스턴스 동기화

Generator의 SQL/MyBatis/DB Profile/Manifest가 변경되면 이미 생성된 Domain도 drift 대상이다.

```powershell
# 비교만
pwsh -ExecutionPolicy Bypass -File .\cpf-tools\scripts\sync-generated-domain-artifacts.ps1 -Scope Database

# DB/SQL/MyBatis 영역만 안전 적용
pwsh -ExecutionPolicy Bypass -File .\cpf-tools\scripts\sync-generated-domain-artifacts.ps1 -Scope Database -Apply

# Generator Java/API/EDU 템플릿까지 바뀐 경우 전체 generator-owned 영역 적용
pwsh -ExecutionPolicy Bypass -File .\cpf-tools\scripts\sync-generated-domain-artifacts.ps1 -Scope AllGeneratorOwned -Apply
```

`Database` Scope는 DB Profile/SQL/MyBatis/Deploy DB artifact만, `AllGeneratorOwned`는 생성기가 만든 Source/API/Test/Guide까지 비교한다.
직접 수정된 generator-owned 파일은 기본적으로 덮어쓰지 않는다. `-AllowModifiedGeneratorFiles`는 원인과 영향이
검토된 경우에만 사용하며, 고객 업무 Source를 Generator가 임의 변경하는 용도로 사용하지 않는다.
또한 `domainType=GENERATED_DOMAIN`인데 `generator-ownership.json`이 없는 기존 Domain은 조용히 건너뛰지 않고
fail-closed한다. 먼저 Generator ownership을 정본화한 뒤 동기화해야 한다.

## 11. EXS 검증 정책

Repository baseline에는 `cpf-external`을 보존하지 않는다.

```powershell
pwsh -ExecutionPolicy Bypass -File .\cpf-tools\scripts\verify-exs-generated-domain-lifecycle.ps1
```

위 Script는 `external/EXS`를 공식 Generator로 생성하고 `verify-domain.ps1`을 통과시킨 뒤 `finally`에서 제거한다.
따라서 EXS만 특별한 Source/SQL 구조를 갖는 회귀를 검출할 수 있다.

## 12. 통합 검증

반복적인 DB reset, Browser smoke, Generator lifecycle을 개발 항목마다 실행하지 않고
`CPF_INTEGRATED_VERIFICATION_PLAN.md`에 누적한 뒤 다음 Runner로 한 번에 수행한다.

```powershell
pwsh -ExecutionPolicy Bypass -File .\cpf-tools\scripts\verify-full-product.ps1 -WithDatabase -WithGeneratorLifecycle -WithBrowser -RequireAll -Profile local
```

`-RequireAll`에서 `FAIL`뿐 아니라 `SKIPPED`도 전체 완료를 막는다.
Runner의 결과는 sanitized Evidence로 저장하고, 다른 PC의 과거 PASS를 현재 PC의 PASS로 재사용하지 않는다.

## 13. Source 품질 Gate

- `check-source-documentation-standard.ps1`: 변경된 중요 Public API/Service/Controller의 JavaDoc과 OpenAPI를 확인한다.
- `check-frontend-route-targets.ps1`: ADM/BZA lazy route 실파일과 외부 Runtime URL을 확인한다.
- `check-r10-cleanup.ps1`: EXS baseline, Core Batch legacy, root log/ZIP/temp와 stale source를 확인한다.
- `check-r10-product-standard.ps1`: Calendar/Log/Foundation API/Generated Domain 동기화 등 현재 제품 Guardrail을 확인한다.

## 14. Gate·Tool Lifecycle 정본

Gate/PowerShell/Gradle Tool의 역할 분류, 삭제 기준, 최종 배포 포함/제외 기준은 다음 문서를 정본으로 사용한다.

- `cpf-docs/guides/CPF_GATE_AND_TOOL_LIFECYCLE_GUIDE.md`

특히 모든 Gate/Tool은 `DEV_ONLY`, `CI_RELEASE`, `PRODUCT_ADMIN_TOOL` 중 하나로 분류해야 한다.
개발용 Gate를 운영 Runtime 배포물에 포함하지 않으며, 고객 관리자에게 필요한 설치/Upgrade/Rollback/Generator/Verify 기능만 별도 관리 Tool로 제공한다.

## 15. 공식 Tool 옵션 문서화 기준

`cpf-tools`의 정식 사용자 진입 Script는 Script 존재만으로 완료 처리하지 않는다.
각 Tool Guide에는 필수/선택 옵션, Default, 조합 제약, 환경변수, 입력/출력, 변경 영향, 정상/실패 예제, 재실행 가능 여부, 복구 방법을 기록한다.

새 옵션을 추가하거나 Default를 변경하면 다음을 같은 작업 단위에서 갱신한다.

1. Script의 comment-based help/usage
2. `CPF_TOOLS_GUIDE.md` 또는 기능별 상세 Guide
3. Generator/CI 사용 예제
4. 관련 Test/Gate

문서와 실제 옵션이 다르면 제품 결함이다.

## 16. 개발 Gate 대표 Entry

개발자가 개별 Gate를 모두 기억하지 않도록 다음 3단계 Aggregate Gate를 제품 목표로 한다.

- `QUICK`: 개발 중 반복 가능한 저비용 정적 Gate
- `VERIFY`: 작업 단위 종료 시 영향 Module/Packaging/Focused lifecycle 검증
- `FULL`: Release 후보 기준 DB/Browser/Multi-instance/Fault/Generator lifecycle 통합 검증

최종 구현 시 가능한 범위에서 Gradle/JVM Portable Entry를 정본으로 하고 PowerShell은 Windows 편의 Wrapper로 제공한다.
상세 포함/제외 기준은 `CPF_GATE_AND_TOOL_LIFECYCLE_GUIDE.md`를 따른다.

## 17. Artifact 공급 모드

Generated Domain/독립 WAS의 CPF Library 공급은 다음 세 모드를 제품 표준으로 정리한다.

- `LOCAL_DEV`: 로컬 CPF Source 변경을 개발 Domain에 자동 반영
- `REMOTE`: CI/CD가 Nexus/Artifactory 등 승인된 Registry의 고정 버전을 사용
- `OFFLINE`: Registry가 없는 환경에서 manifest/checksum을 가진 Offline Library Bundle을 사용

CI/STG/PROD에서는 개발자 Local Repository fallback을 사용하지 않는다.
`OFFLINE`도 수동 JAR 복사가 아니라 Gradle이 검증된 Bundle을 자동 선택·패키징하는 방식으로 구현한다.
