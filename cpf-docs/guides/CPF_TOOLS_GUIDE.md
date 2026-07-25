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

`cpf-tools/db/source/mariadb`처럼 특정 Vendor만 별도 top-level source tree를 갖는 구조는 사용하지 않는다. Vendor source는 모두 `vendor/<vendor>/source` 경계 안에서 관리한다.

## 3. DB 수정의 유일한 완료 경로

Schema, Column, Index, FK, Seed, Migration, Runtime Mapper, Generated Domain metadata를 변경하면 반드시 다음을 실행한다.

```powershell
pwsh -ExecutionPolicy Bypass -File .\cpf-tools\scripts\sync-database-artifacts.ps1
```

실행 순서:

1. `build-all-install-sql.ps1` — canonical source에서 lifecycle bundle 재생성
2. `generate-database-schema-manifest.ps1` — Table/Column/Index/FK metadata 생성
3. `check-database-schema-drift.ps1` — tracked manifest와 재생성 결과 비교
4. `check-database-profile-standard.ps1` — profile, Generated Domain, EXS fixed residue, seed 정책 검사

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
