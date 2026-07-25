# CPF Database Installer / Vendor Runtime SQL / Generated Domain Guide

## 1. Identity and independent DB configuration

Every platform module and every generated business domain is identified by:

- `domainName`
- `systemCode` (exactly 3 uppercase letters)
- `moduleName`

Every owner can independently configure:

- vendor
- host / port
- database / schema
- admin / migration / runtime account
- secret reference
- product / optional / test seed policy

A generated `PAY` domain can use Oracle while another generated `INS` domain uses PostgreSQL.

## 2. Existing database safety

Fresh-install DDL is not an upgrade mechanism.

- no expected CPF table exists: execute fresh-install DDL
- all expected CPF tables exist: skip fresh-install DDL
- only some expected tables exist: fail closed as a partial installation
- schema changes: apply explicit migration/upgrade
- drop/reset: never implicit

Product seed is required to be idempotent. The static gate rejects a plain non-idempotent INSERT.
Customer business rows are not installer-owned and must not be rewritten by Product Seed.

## 3. Platform installation selection

All enabled platform DBs:

```powershell
pwsh -ExecutionPolicy Bypass -File .\cpf-tools\scripts\initialize-cpf-database.ps1 -All -RequireRun
```

One domain:

```powershell
pwsh -ExecutionPolicy Bypass -File .\cpf-tools\scripts\initialize-cpf-database.ps1 -DomainName admin -RequireRun
```

One SystemCode:

```powershell
pwsh -ExecutionPolicy Bypass -File .\cpf-tools\scripts\initialize-cpf-database.ps1 -SystemCode ADM -RequireRun
```

Multiple SystemCodes:

```powershell
pwsh -ExecutionPolicy Bypass -File .\cpf-tools\scripts\initialize-cpf-database.ps1 -SystemCode CPF,ADM,BAT -RequireRun
```

SeedMode:

- `profile`: use each profile's flags
- `product`: Product Seed only
- `none`: no seed
- `all`: Product + Optional Sample + Test; use only in an isolated dev/test environment

## 4. Generated Domain installation selection

All generated DB-enabled domains:

```powershell
pwsh -ExecutionPolicy Bypass -File .\cpf-tools\scripts\initialize-generated-domain-databases.ps1 -All -Operation bootstrap -Apply
```

One generated domain:

```powershell
pwsh -ExecutionPolicy Bypass -File .\cpf-tools\scripts\initialize-generated-domain-databases.ps1 -DomainName payment -Operation bootstrap -Apply
```

Multiple generated domains:

```powershell
pwsh -ExecutionPolicy Bypass -File .\cpf-tools\scripts\initialize-generated-domain-databases.ps1 -SystemCode PAY,INS -Operation bootstrap -Apply
```

## 5. Unified installer

Platform + Generated Domain:

```powershell
pwsh -ExecutionPolicy Bypass -File .\cpf-tools\scripts\initialize-databases.ps1 -Scope all -All -Apply
```

The same script supports `-Scope platform` and `-Scope generated`.

## 6. Vendor-specific runtime SQL

Generated Domain build selects only the configured Vendor template:

`cpf-tools/db/vendor/{vendor}/domain-template`

The generated module assembles:

- DDL / migration / rollback / verify SQL
- Vendor-specific MyBatis Mapper
- Vendor-specific repository SQL resources when supplied

Runtime Mapper lookup is fail-closed through `CpfSqlResourceResolver`:

`mybatis/vendor/{vendor}/mapper/{domain}/**/*.xml`

There is no cross-vendor fallback.

Current product truth:

- Generated Domain templates exist for MariaDB, MySQL, PostgreSQL, Oracle and SQL Server.
- Their real database runtime execution remains `미검증` until evidence is produced.
- Full official CPF Platform Vendor Pack is currently implemented only for MariaDB.
- MySQL/PostgreSQL/Oracle/SQL Server platform-wide DDL/runtime SQL are `미구현`, and the installer must fail instead of using MariaDB SQL.

See `cpf-tools/config/database-vendor-coverage.json`.

## 7. Generator

`create-domain.ps1` writes the generated domain's own Git-tracked:

`cpf-{domain}/deploy/database/database-profile.json`

The generated domain owns its DB connection and Vendor choice. `-ProvisionDatabase` uses that generated profile as the source of truth and does not maintain a second independent set of DB credentials.


## 8. Platform Vendor source ownership

Platform canonical SQL source는 Vendor별로 `cpf-tools/db/vendor/{vendor}/source` 아래에 둔다. 특정 Vendor만 `cpf-tools/db/source/{vendor}` 같은 별도 top-level 경로를 사용하지 않는다. 현재 MariaDB만 Platform source/pack이 구현되어 있으며 다른 Vendor는 동일 ownership 경계 아래 구현되기 전까지 fail-closed `미구현` 상태다.

## 9. EXS generated-domain policy

EXS는 Platform 기본 모듈/DB가 아니다. 필요 시 다음과 같이 Golden Generator로 생성한다.

```powershell
pwsh -ExecutionPolicy Bypass -File .\cpf-tools\generator\create-domain.ps1 -DomainName external -SystemCode EXS -Apply
```

생성된 EXS는 해당 프로젝트의 Generated Domain이며 Platform install source에는 `exsDB` 또는 `exs_*`를 추가하지 않는다.
