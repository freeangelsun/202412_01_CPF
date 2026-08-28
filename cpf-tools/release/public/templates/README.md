# CPF Public Developer Workspace

CPF(Core Platform Framework)로 업무 Domain을 개발하는 Public Workspace입니다. Framework 내부 구현 Source는 포함하지 않으며, 업무 Source는 Public CPF BOM/Starter/API를 Maven-compatible Binary Repository에서 사용합니다.

## Prerequisites

- Git
- Java 25
- Docker Desktop 또는 승인된 Container Runtime
- Backoffice Web Frontend를 사용할 때 Node.js

## Quick Start

```powershell
$env:CPF_MAVEN_REPOSITORY_URL='<repository-url>'
$env:CPF_VERSION='<cpf-version>'
.\bin\cpf.cmd bootstrap
```

Linux:

```bash
export CPF_MAVEN_REPOSITORY_URL='<repository-url>'
export CPF_VERSION='<cpf-version>'
./bin/cpf bootstrap
```

Bootstrap은 root `cpf-*/gradle.properties` 중 `cpf.domain.contractVersion`이 선언된 Developer Domain Contract와 각 Domain의 local DB binding(`build/cpf-local/<domain>/cpf-db-profile.local.json`)을 함께 발견합니다. Domain Setup에서 Oracle/PostgreSQL/MariaDB를 Domain별로 선택할 수 있으며, Profile이 아직 없는 Reference Domain만 `--db postgresql|mariadb|oracle` 값을 local-only 기본 Binding 생성에 사용합니다. DB3 SQL은 Domain Source Tree에 저장하지 않고 Published Generator의 Canonical DB3 Renderer가 Bootstrap 시 `build/cpf-local/<domain>/db3/<vendor>`에 생성하여 적용합니다.

## New Domain Setup

일반 개발자는 Starter Artifact나 JDBC URL을 직접 조립하지 않고 하나의 Setup 진입점을 사용합니다.

```powershell
.\bin\cpf.cmd domain-new account --system-code ACC --batch --vendor postgresql --database-name accDB --schema-name accDB
```

Linux/CI에서는 동일 Canonical `domain setup` 옵션을 그대로 전달합니다. Setup은 root `gradle.properties` Developer Contract, Capability→Public Starter, Operation 단위 Domain Dependency, Local DB Profile, Generated Source, Workspace 등록을 한 번에 처리하며 DB/Runtime 실행은 Bootstrap에서 별도 검증합니다. Generator 입력/lock/state 파일은 Generated Root에 만들지 않습니다.

## Workspace

- `cpf-member`: Batch 포함 Generated Business Domain Reference
- `cpf-external`: Online-only Generated Business Domain Reference
- `cpf-backoffice-web`: 선택형 외부 채널/BFF Reference. DB 없이 HTTP/HTTPS로 MBW Backoffice Domain을 호출합니다.
- `bin/`: Java 기반 Unified `cpf` CLI (`cpf`, `cpf.cmd`, `cpf.ps1`)

Generated Domain은 `Domain → Business Feature → Technical Role` 구조를 사용합니다. Domain 간 호출은 CPF Public Domain Client/Logical Invocation Boundary를 사용하며 다른 Domain Service/Repository/DB를 직접 참조하지 않습니다.

## Common Commands

```powershell
.\bin\cpf.cmd build
.\bin\cpf.cmd test
.\bin\cpf.cmd stop
.\bin\cpf.cmd reset --confirm-local-reset
```

`stop`은 DB Volume을 보존합니다. `reset`만 Local 개발 데이터를 삭제하며 명시적 확인 옵션이 필요합니다.
