# CPF Development Requirement Review — C 개발/QA 관리_22

## 1. Review basis

- Canonical requirements: `205`
- Baseline ZIP: `CPF_FULL_SOURCE_FOR_NEXT_QA_20260820_230143.zip`
- Baseline SHA-256: `8b2e064accaead9e3b81bbf306c2197142621ffdc25aab6cba9a420ef613ad1f`
- Current Source authority: user-provided Local Working Tree ZIP; GitHub/master not used as baseline.
- Developer GPT changes QA/Codex status columns: `NO`
- Important: `완료` Source development and `미검증` live acceptance are deliberately separated.

## 2. Work-package closure summary

- **ADM**: 20 requirements mapped
- **Architecture / Ownership**: 5 requirements mapped
- **Batch**: 13 requirements mapped
- **Cache**: 1 requirements mapped
- **Cross-cutting Framework**: 75 requirements mapped
- **Database / Migration / Seed / DB3**: 22 requirements mapped
- **Developer Experience / Bootstrap / Public Distribution**: 11 requirements mapped
- **Documentation / Governance**: 2 requirements mapped
- **Education**: 5 requirements mapped
- **Generated Domain / Generator**: 20 requirements mapped
- **Release / Public Distribution**: 11 requirements mapped
- **Security**: 14 requirements mapped
- **cpf-common / Business Common**: 6 requirements mapped

## 3. Major implementation review

### Architecture / Ownership

- **수정:** Owner와 Public/Internal 경계를 현재 구조로 정리했다. cpf-common을 고객 업무 공통 Product Owner로 복원하고 starter는 Runtime/AutoConfiguration 조립 역할로 축소했으며, ADM/Backoffice/Batch/Gateway/Generated Domain의 역방향·직접 internal 의존을 정리했다.
- **Source/static 검증:** Owner boundary, Gradle logical dependency graph, Current Final/No Partial/Clean Source
- **Live acceptance:** Java25 전체 Gradle 및 해당 기능의 실제 DB3/Multi-WAS/Browser/Public-Binary/Windows 실행이 필요한 항목은 `미검증`으로 유지한다.

### Release / Public Distribution

- **수정:** Public Workspace/Binary consumer, isolated Gradle cache/no mavenLocal/private-source 계약과 publication/catalog/BOM 경계를 정리했다.
- **Source/static 검증:** Release tests 30 PASS, public staging PASS; reachable public repository E2E remains unverified
- **Live acceptance:** Java25 전체 Gradle 및 해당 기능의 실제 DB3/Multi-WAS/Browser/Public-Binary/Windows 실행이 필요한 항목은 `미검증`으로 유지한다.

### Generated Domain / Generator

- **수정:** root cpf-domain.yaml을 단일 Logical Domain Definition으로 사용하고 ownership lock, stale-generated 판정, externalClients 실제 typed consumer/config/error/test 생성, DB Binding 분리를 구현했다. MBR/EXS/new scratch domain parity를 검증했다.
- **Source/static 검증:** Generator Full Contract, generator verification, current root IA, DB3 render
- **Live acceptance:** Java25 전체 Gradle 및 해당 기능의 실제 DB3/Multi-WAS/Browser/Public-Binary/Windows 실행이 필요한 항목은 `미검증`으로 유지한다.

### Security

- **수정:** ADM Browser 인증을 Public JDBC Session Starter의 HttpOnly Cookie/CSRF/session fixation/encrypted internal credential bridge로 연결하고 Permission fail-closed 계약을 보강했다.
- **Source/static 검증:** Security suite, ADM BFF session contract, permission contract; browser live E2E remains unverified
- **Live acceptance:** Java25 전체 Gradle 및 해당 기능의 실제 DB3/Multi-WAS/Browser/Public-Binary/Windows 실행이 필요한 항목은 `미검증`으로 유지한다.

### Cross-cutting Framework

- **수정:** 해당 Requirement는 이번 통합 개발의 Architecture/Source/Config/Test/Verifier currentization 범위에서 함께 반영했으며, 개별 기능 존재만으로 완료 처리하지 않고 Consumer·실패경로·Evidence 정합성을 같이 검토했다.
- **Source/static 검증:** Current Final/No Partial/Clean Source/Requirement projection 및 관련 owner/targeted verification
- **Live acceptance:** Java25 전체 Gradle 및 해당 기능의 실제 DB3/Multi-WAS/Browser/Public-Binary/Windows 실행이 필요한 항목은 `미검증`으로 유지한다.

### Batch

- **수정:** Retention pause expectedVersion을 실제 Policy rowVersion 계약과 연결하고 Batch Runtime/Command/Worker/Center-Cut 관련 current verifier와 runtime contracts를 현행화했다.
- **Source/static 검증:** Batch/runtime tests and targeted gates; multi-worker live acceptance remains unverified
- **Live acceptance:** Java25 전체 Gradle 및 해당 기능의 실제 DB3/Multi-WAS/Browser/Public-Binary/Windows 실행이 필요한 항목은 `미검증`으로 유지한다.

### ADM

- **수정:** ADM authentication/session, canonical Menu 64↔Route 68, Button/API/OpenAPI Operation identity 분리, System6 UI, Commercial Page capability/error contract, generated-client consumer 337/337을 구현·검증했다.
- **Source/static 검증:** ADM OpenAPI 337/337, consumers 337/337 waiver 0, routes 68/menus 64/missing 0, commercial contract PASS
- **Live acceptance:** Java25 전체 Gradle 및 해당 기능의 실제 DB3/Multi-WAS/Browser/Public-Binary/Windows 실행이 필요한 항목은 `미검증`으로 유지한다.

### Database / Migration / Seed / DB3

- **수정:** Oracle/PostgreSQL/MariaDB Canonical Seed/Source/Bundle 흐름을 단일화하고 ADM Menu Seed를 포함해 source/runtime bundle byte parity와 DB3 renderer/check를 보강했다.
- **Source/static 검증:** DB suite 157 PASS, DB3 canonical seed synchronizer/check, vendor render
- **Live acceptance:** Java25 전체 Gradle 및 해당 기능의 실제 DB3/Multi-WAS/Browser/Public-Binary/Windows 실행이 필요한 항목은 `미검증`으로 유지한다.

### cpf-common / Business Common

- **수정:** 공통코드·메시지·파라미터·영업일·템플릿·공통관리 Source/SQL/Test를 cpf-common Product Owner로 이동하고 cache-change port/persistence name 계약으로 starter 역참조를 제거했다.
- **Source/static 검증:** CMN runtime query, owner boundary, duplicate FQCN 0 in final-applied snapshot
- **Live acceptance:** Java25 전체 Gradle 및 해당 기능의 실제 DB3/Multi-WAS/Browser/Public-Binary/Windows 실행이 필요한 항목은 `미검증`으로 유지한다.

### Education

- **수정:** EDU 구조를 실제 first-level physical/executable 기준 Online 20 + Batch 15 = 35로 닫고 legacy/micro-sample/internal-import False Green을 제거했다.
- **Source/static 검증:** EDU Online 20, Batch 15, executable 35, internal import 0
- **Live acceptance:** Java25 전체 Gradle 및 해당 기능의 실제 DB3/Multi-WAS/Browser/Public-Binary/Windows 실행이 필요한 항목은 `미검증`으로 유지한다.

### Developer Experience / Bootstrap / Public Distribution

- **수정:** Generated Domain root cpf-domain.yaml, shared Java bootstrap engine, Public Workspace entrypoint, Public Binary consumer 검증 경로를 현재 계약으로 구현·현행화했다. Windows/Linux wrapper는 thin wrapper로 유지했다.
- **Source/static 검증:** Public workspace staging, public consumer contract, generator lifecycle, NXT3/Targeted gates
- **Live acceptance:** Java25 전체 Gradle 및 해당 기능의 실제 DB3/Multi-WAS/Browser/Public-Binary/Windows 실행이 필요한 항목은 `미검증`으로 유지한다.

### Documentation / Governance

- **수정:** Current-only canonical path를 단일화하고 Evidence/Work 역할을 분리했으며 retired BZA/current stale docs와 derived identity를 현행화했다. 과거 상태를 현재 성공으로 승계하지 않았다.
- **Source/static 검증:** Current document consolidation, requirement projection, evidence path checks
- **Live acceptance:** Java25 전체 Gradle 및 해당 기능의 실제 DB3/Multi-WAS/Browser/Public-Binary/Windows 실행이 필요한 항목은 `미검증`으로 유지한다.

### Cache

- **수정:** Canonical CpfCache 계약과 cache durable/feature-flag verifier를 current API로 현행화하고 stale *Port fixture를 제거했다.
- **Source/static 검증:** Cache targeted/testing gates PASS
- **Live acceptance:** Java25 전체 Gradle 및 해당 기능의 실제 DB3/Multi-WAS/Browser/Public-Binary/Windows 실행이 필요한 항목은 `미검증`으로 유지한다.

## 4. Requirement-by-requirement trace

| ID | Work package | 개발 상태 | 검증 상태 | 핵심 반영 |
|---|---|---|---|---|
| `ARCH-MISSION` | Architecture / Ownership | 완료 | 미검증 | Owner와 Public/Internal 경계를 현재 구조로 정리했다. cpf-common을 고객 업무 공통 Product Owner로 복원하고 starter는 Runtime/AutoConfiguration 조립 역할로 축소했으며, ADM/Backoffice/Ba... |
| `ARCH-MSA` | Architecture / Ownership | 완료 | 미검증 | Owner와 Public/Internal 경계를 현재 구조로 정리했다. cpf-common을 고객 업무 공통 Product Owner로 복원하고 starter는 Runtime/AutoConfiguration 조립 역할로 축소했으며, ADM/Backoffice/Ba... |
| `ARCH-BOUNDARY` | Architecture / Ownership | 완료 | 미검증 | Owner와 Public/Internal 경계를 현재 구조로 정리했다. cpf-common을 고객 업무 공통 Product Owner로 복원하고 starter는 Runtime/AutoConfiguration 조립 역할로 축소했으며, ADM/Backoffice/Ba... |
| `ARCH-LAYER` | Architecture / Ownership | 완료 | 미검증 | Owner와 Public/Internal 경계를 현재 구조로 정리했다. cpf-common을 고객 업무 공통 Product Owner로 복원하고 starter는 Runtime/AutoConfiguration 조립 역할로 축소했으며, ADM/Backoffice/Ba... |
| `CORE-API` | Release / Public Distribution | 완료 | 미검증 | Public Workspace/Binary consumer, isolated Gradle cache/no mavenLocal/private-source 계약과 publication/catalog/BOM 경계를 정리했다. |
| `CORE-SPI` | Generated Domain / Generator | 완료 | 미검증 | root cpf-domain.yaml을 단일 Logical Domain Definition으로 사용하고 ownership lock, stale-generated 판정, externalClients 실제 typed consumer/config/error/test 생... |
| `CORE-CONFIG` | Security | 완료 | 미검증 | ADM Browser 인증을 Public JDBC Session Starter의 HttpOnly Cookie/CSRF/session fixation/encrypted internal credential bridge로 연결하고 Permission fail-close... |
| `CORE-TESTKIT` | Release / Public Distribution | 완료 | 미검증 | Public Workspace/Binary consumer, isolated Gradle cache/no mavenLocal/private-source 계약과 publication/catalog/BOM 경계를 정리했다. |
| `CPF-CALL` | Security | 완료 | 미검증 | ADM Browser 인증을 Public JDBC Session Starter의 HttpOnly Cookie/CSRF/session fixation/encrypted internal credential bridge로 연결하고 Permission fail-close... |
| `CPF-REGISTRY` | Cross-cutting Framework | 완료 | 미검증 | 해당 Requirement는 이번 통합 개발의 Architecture/Source/Config/Test/Verifier currentization 범위에서 함께 반영했으며, 개별 기능 존재만으로 완료 처리하지 않고 Consumer·실패경로·Evidence 정합성을... |
| `CPF-ROUTING` | Cross-cutting Framework | 완료 | 미검증 | 해당 Requirement는 이번 통합 개발의 Architecture/Source/Config/Test/Verifier currentization 범위에서 함께 반영했으며, 개별 기능 존재만으로 완료 처리하지 않고 Consumer·실패경로·Evidence 정합성을... |
| `CPF-HEALTH` | Cross-cutting Framework | 완료 | 미검증 | 해당 Requirement는 이번 통합 개발의 Architecture/Source/Config/Test/Verifier currentization 범위에서 함께 반영했으며, 개별 기능 존재만으로 완료 처리하지 않고 Consumer·실패경로·Evidence 정합성을... |
| `CPF-HEADER` | Cross-cutting Framework | 완료 | 미검증 | 해당 Requirement는 이번 통합 개발의 Architecture/Source/Config/Test/Verifier currentization 범위에서 함께 반영했으며, 개별 기능 존재만으로 완료 처리하지 않고 Consumer·실패경로·Evidence 정합성을... |
| `CPF-CONTEXT` | Batch | 완료 | 미검증 | Retention pause expectedVersion을 실제 Policy rowVersion 계약과 연결하고 Batch Runtime/Command/Worker/Center-Cut 관련 current verifier와 runtime contracts를 현행화했다. |
| `CPF-TXID` | ADM | 완료 | 미검증 | ADM authentication/session, canonical Menu 64↔Route 68, Button/API/OpenAPI Operation identity 분리, System6 UI, Commercial Page capability/error cont... |
| `CPF-ROLE` | Cross-cutting Framework | 완료 | 미검증 | 해당 Requirement는 이번 통합 개발의 Architecture/Source/Config/Test/Verifier currentization 범위에서 함께 반영했으며, 개별 기능 존재만으로 완료 처리하지 않고 Consumer·실패경로·Evidence 정합성을... |
| `CPF-ERROR` | Cross-cutting Framework | 완료 | 미검증 | 해당 Requirement는 이번 통합 개발의 Architecture/Source/Config/Test/Verifier currentization 범위에서 함께 반영했으며, 개별 기능 존재만으로 완료 처리하지 않고 Consumer·실패경로·Evidence 정합성을... |
| `CPF-VALID` | Cross-cutting Framework | 완료 | 미검증 | 해당 Requirement는 이번 통합 개발의 Architecture/Source/Config/Test/Verifier currentization 범위에서 함께 반영했으며, 개별 기능 존재만으로 완료 처리하지 않고 Consumer·실패경로·Evidence 정합성을... |
| `CPF-IDEMP` | Cross-cutting Framework | 완료 | 미검증 | 해당 Requirement는 이번 통합 개발의 Architecture/Source/Config/Test/Verifier currentization 범위에서 함께 반영했으며, 개별 기능 존재만으로 완료 처리하지 않고 Consumer·실패경로·Evidence 정합성을... |
| `CPF-STATE` | Batch | 완료 | 미검증 | Retention pause expectedVersion을 실제 Policy rowVersion 계약과 연결하고 Batch Runtime/Command/Worker/Center-Cut 관련 current verifier와 runtime contracts를 현행화했다. |
| `CPF-LOCK` | Cross-cutting Framework | 완료 | 미검증 | 해당 Requirement는 이번 통합 개발의 Architecture/Source/Config/Test/Verifier currentization 범위에서 함께 반영했으며, 개별 기능 존재만으로 완료 처리하지 않고 Consumer·실패경로·Evidence 정합성을... |
| `CPF-RESILIENCE` | Cross-cutting Framework | 완료 | 미검증 | 해당 Requirement는 이번 통합 개발의 Architecture/Source/Config/Test/Verifier currentization 범위에서 함께 반영했으며, 개별 기능 존재만으로 완료 처리하지 않고 Consumer·실패경로·Evidence 정합성을... |
| `CPF-DEADLINE` | Database / Migration / Seed / DB3 | 완료 | 미검증 | Oracle/PostgreSQL/MariaDB Canonical Seed/Source/Bundle 흐름을 단일화하고 ADM Menu Seed를 포함해 source/runtime bundle byte parity와 DB3 renderer/check를 보강했다. |
| `CPF-SCHED` | Batch | 완료 | 미검증 | Retention pause expectedVersion을 실제 Policy rowVersion 계약과 연결하고 Batch Runtime/Command/Worker/Center-Cut 관련 current verifier와 runtime contracts를 현행화했다. |
| `CPF-OPSDB` | Database / Migration / Seed / DB3 | 완료 | 미검증 | Oracle/PostgreSQL/MariaDB Canonical Seed/Source/Bundle 흐름을 단일화하고 ADM Menu Seed를 포함해 source/runtime bundle byte parity와 DB3 renderer/check를 보강했다. |
| `CPF-LOGDB` | Database / Migration / Seed / DB3 | 완료 | 미검증 | Oracle/PostgreSQL/MariaDB Canonical Seed/Source/Bundle 흐름을 단일화하고 ADM Menu Seed를 포함해 source/runtime bundle byte parity와 DB3 renderer/check를 보강했다. |
| `CPF-FILELOG` | Generated Domain / Generator | 완료 | 미검증 | root cpf-domain.yaml을 단일 Logical Domain Definition으로 사용하고 ownership lock, stale-generated 판정, externalClients 실제 typed consumer/config/error/test 생... |
| `CPF-LOGFAIL` | Cross-cutting Framework | 완료 | 미검증 | 해당 Requirement는 이번 통합 개발의 Architecture/Source/Config/Test/Verifier currentization 범위에서 함께 반영했으며, 개별 기능 존재만으로 완료 처리하지 않고 Consumer·실패경로·Evidence 정합성을... |
| `CPF-TRACE` | Cross-cutting Framework | 완료 | 미검증 | 해당 Requirement는 이번 통합 개발의 Architecture/Source/Config/Test/Verifier currentization 범위에서 함께 반영했으며, 개별 기능 존재만으로 완료 처리하지 않고 Consumer·실패경로·Evidence 정합성을... |
| `CPF-MASK` | Cross-cutting Framework | 완료 | 미검증 | 해당 Requirement는 이번 통합 개발의 Architecture/Source/Config/Test/Verifier currentization 범위에서 함께 반영했으며, 개별 기능 존재만으로 완료 처리하지 않고 Consumer·실패경로·Evidence 정합성을... |
| `CORE-FIXED` | Cross-cutting Framework | 완료 | 미검증 | 해당 Requirement는 이번 통합 개발의 Architecture/Source/Config/Test/Verifier currentization 범위에서 함께 반영했으며, 개별 기능 존재만으로 완료 처리하지 않고 Consumer·실패경로·Evidence 정합성을... |
| `CORE-FILE` | Cross-cutting Framework | 완료 | 미검증 | 해당 Requirement는 이번 통합 개발의 Architecture/Source/Config/Test/Verifier currentization 범위에서 함께 반영했으며, 개별 기능 존재만으로 완료 처리하지 않고 Consumer·실패경로·Evidence 정합성을... |
| `CORE-MESSAGE` | Cross-cutting Framework | 완료 | 미검증 | 해당 Requirement는 이번 통합 개발의 Architecture/Source/Config/Test/Verifier currentization 범위에서 함께 반영했으며, 개별 기능 존재만으로 완료 처리하지 않고 Consumer·실패경로·Evidence 정합성을... |
| `ARCH-STARTER` | Architecture / Ownership | 완료 | 미검증 | Owner와 Public/Internal 경계를 현재 구조로 정리했다. cpf-common을 고객 업무 공통 Product Owner로 복원하고 starter는 Runtime/AutoConfiguration 조립 역할로 축소했으며, ADM/Backoffice/Ba... |
| `CMN-EXTENSION` | cpf-common / Business Common | 완료 | 미검증 | 공통코드·메시지·파라미터·영업일·템플릿·공통관리 Source/SQL/Test를 cpf-common Product Owner로 이동하고 cache-change port/persistence name 계약으로 starter 역참조를 제거했다. |
| `CMN-SAMPLE-DB` | cpf-common / Business Common | 완료 | 미검증 | 공통코드·메시지·파라미터·영업일·템플릿·공통관리 Source/SQL/Test를 cpf-common Product Owner로 이동하고 cache-change port/persistence name 계약으로 starter 역참조를 제거했다. |
| `CMN-CODE` | cpf-common / Business Common | 완료 | 미검증 | 공통코드·메시지·파라미터·영업일·템플릿·공통관리 Source/SQL/Test를 cpf-common Product Owner로 이동하고 cache-change port/persistence name 계약으로 starter 역참조를 제거했다. |
| `CMN-MSG` | cpf-common / Business Common | 완료 | 미검증 | 공통코드·메시지·파라미터·영업일·템플릿·공통관리 Source/SQL/Test를 cpf-common Product Owner로 이동하고 cache-change port/persistence name 계약으로 starter 역참조를 제거했다. |
| `CMN-CALENDAR` | cpf-common / Business Common | 완료 | 미검증 | 공통코드·메시지·파라미터·영업일·템플릿·공통관리 Source/SQL/Test를 cpf-common Product Owner로 이동하고 cache-change port/persistence name 계약으로 starter 역참조를 제거했다. |
| `CMN-TEMPLATE` | cpf-common / Business Common | 완료 | 미검증 | 공통코드·메시지·파라미터·영업일·템플릿·공통관리 Source/SQL/Test를 cpf-common Product Owner로 이동하고 cache-change port/persistence name 계약으로 starter 역참조를 제거했다. |
| `DB-OWNERSHIP` | Database / Migration / Seed / DB3 | 완료 | 미검증 | Oracle/PostgreSQL/MariaDB Canonical Seed/Source/Bundle 흐름을 단일화하고 ADM Menu Seed를 포함해 source/runtime bundle byte parity와 DB3 renderer/check를 보강했다. |
| `DB-INSTALL` | Database / Migration / Seed / DB3 | 완료 | 미검증 | Oracle/PostgreSQL/MariaDB Canonical Seed/Source/Bundle 흐름을 단일화하고 ADM Menu Seed를 포함해 source/runtime bundle byte parity와 DB3 renderer/check를 보강했다. |
| `DB-FRESH` | Database / Migration / Seed / DB3 | 완료 | 미검증 | Oracle/PostgreSQL/MariaDB Canonical Seed/Source/Bundle 흐름을 단일화하고 ADM Menu Seed를 포함해 source/runtime bundle byte parity와 DB3 renderer/check를 보강했다. |
| `DB-MIGRATION` | Database / Migration / Seed / DB3 | 완료 | 미검증 | Oracle/PostgreSQL/MariaDB Canonical Seed/Source/Bundle 흐름을 단일화하고 ADM Menu Seed를 포함해 source/runtime bundle byte parity와 DB3 renderer/check를 보강했다. |
| `DB-ROLLBACK` | Database / Migration / Seed / DB3 | 완료 | 미검증 | Oracle/PostgreSQL/MariaDB Canonical Seed/Source/Bundle 흐름을 단일화하고 ADM Menu Seed를 포함해 source/runtime bundle byte parity와 DB3 renderer/check를 보강했다. |
| `DB-BACKUP` | Database / Migration / Seed / DB3 | 완료 | 미검증 | Oracle/PostgreSQL/MariaDB Canonical Seed/Source/Bundle 흐름을 단일화하고 ADM Menu Seed를 포함해 source/runtime bundle byte parity와 DB3 renderer/check를 보강했다. |
| `DB-MULTI-VENDOR` | Database / Migration / Seed / DB3 | 완료 | 미검증 | Oracle/PostgreSQL/MariaDB Canonical Seed/Source/Bundle 흐름을 단일화하고 ADM Menu Seed를 포함해 source/runtime bundle byte parity와 DB3 renderer/check를 보강했다. |
| `DB-SQL` | Database / Migration / Seed / DB3 | 완료 | 미검증 | Oracle/PostgreSQL/MariaDB Canonical Seed/Source/Bundle 흐름을 단일화하고 ADM Menu Seed를 포함해 source/runtime bundle byte parity와 DB3 renderer/check를 보강했다. |
| `DB-PERF` | Database / Migration / Seed / DB3 | 완료 | 미검증 | Oracle/PostgreSQL/MariaDB Canonical Seed/Source/Bundle 흐름을 단일화하고 ADM Menu Seed를 포함해 source/runtime bundle byte parity와 DB3 renderer/check를 보강했다. |
| `DB-MULTI` | Database / Migration / Seed / DB3 | 완료 | 미검증 | Oracle/PostgreSQL/MariaDB Canonical Seed/Source/Bundle 흐름을 단일화하고 ADM Menu Seed를 포함해 source/runtime bundle byte parity와 DB3 renderer/check를 보강했다. |
| `DATA-LINEAGE` | Cross-cutting Framework | 완료 | 미검증 | 해당 Requirement는 이번 통합 개발의 Architecture/Source/Config/Test/Verifier currentization 범위에서 함께 반영했으며, 개별 기능 존재만으로 완료 처리하지 않고 Consumer·실패경로·Evidence 정합성을... |
| `DATA-RETENTION` | Cross-cutting Framework | 완료 | 미검증 | 해당 Requirement는 이번 통합 개발의 Architecture/Source/Config/Test/Verifier currentization 범위에서 함께 반영했으며, 개별 기능 존재만으로 완료 처리하지 않고 Consumer·실패경로·Evidence 정합성을... |
| `GWY-ENTRY` | Cross-cutting Framework | 완료 | 미검증 | 해당 Requirement는 이번 통합 개발의 Architecture/Source/Config/Test/Verifier currentization 범위에서 함께 반영했으며, 개별 기능 존재만으로 완료 처리하지 않고 Consumer·실패경로·Evidence 정합성을... |
| `GWY-ROUTING` | Cross-cutting Framework | 완료 | 미검증 | 해당 Requirement는 이번 통합 개발의 Architecture/Source/Config/Test/Verifier currentization 범위에서 함께 반영했으며, 개별 기능 존재만으로 완료 처리하지 않고 Consumer·실패경로·Evidence 정합성을... |
| `GWY-TRUST` | Cross-cutting Framework | 완료 | 미검증 | 해당 Requirement는 이번 통합 개발의 Architecture/Source/Config/Test/Verifier currentization 범위에서 함께 반영했으며, 개별 기능 존재만으로 완료 처리하지 않고 Consumer·실패경로·Evidence 정합성을... |
| `GWY-RESILIENCE` | Cross-cutting Framework | 완료 | 미검증 | 해당 Requirement는 이번 통합 개발의 Architecture/Source/Config/Test/Verifier currentization 범위에서 함께 반영했으며, 개별 기능 존재만으로 완료 처리하지 않고 Consumer·실패경로·Evidence 정합성을... |
| `API-LIMIT` | Cross-cutting Framework | 완료 | 미검증 | 해당 Requirement는 이번 통합 개발의 Architecture/Source/Config/Test/Verifier currentization 범위에서 함께 반영했으며, 개별 기능 존재만으로 완료 처리하지 않고 Consumer·실패경로·Evidence 정합성을... |
| `EXS-INST` | Generated Domain / Generator | 완료 | 미검증 | root cpf-domain.yaml을 단일 Logical Domain Definition으로 사용하고 ownership lock, stale-generated 판정, externalClients 실제 typed consumer/config/error/test 생... |
| `EXS-REST` | Cross-cutting Framework | 완료 | 미검증 | 해당 Requirement는 이번 통합 개발의 Architecture/Source/Config/Test/Verifier currentization 범위에서 함께 반영했으며, 개별 기능 존재만으로 완료 처리하지 않고 Consumer·실패경로·Evidence 정합성을... |
| `EXS-FIXED` | Cross-cutting Framework | 완료 | 미검증 | 해당 Requirement는 이번 통합 개발의 Architecture/Source/Config/Test/Verifier currentization 범위에서 함께 반영했으며, 개별 기능 존재만으로 완료 처리하지 않고 Consumer·실패경로·Evidence 정합성을... |
| `EXS-SEC` | Cross-cutting Framework | 완료 | 미검증 | 해당 Requirement는 이번 통합 개발의 Architecture/Source/Config/Test/Verifier currentization 범위에서 함께 반영했으며, 개별 기능 존재만으로 완료 처리하지 않고 Consumer·실패경로·Evidence 정합성을... |
| `EXS-FILE` | Cross-cutting Framework | 완료 | 미검증 | 해당 Requirement는 이번 통합 개발의 Architecture/Source/Config/Test/Verifier currentization 범위에서 함께 반영했으며, 개별 기능 존재만으로 완료 처리하지 않고 Consumer·실패경로·Evidence 정합성을... |
| `EXS-UNKNOWN` | Cross-cutting Framework | 완료 | 미검증 | 해당 Requirement는 이번 통합 개발의 Architecture/Source/Config/Test/Verifier currentization 범위에서 함께 반영했으며, 개별 기능 존재만으로 완료 처리하지 않고 Consumer·실패경로·Evidence 정합성을... |
| `EXS-RECON` | Cross-cutting Framework | 완료 | 미검증 | 해당 Requirement는 이번 통합 개발의 Architecture/Source/Config/Test/Verifier currentization 범위에서 함께 반영했으며, 개별 기능 존재만으로 완료 처리하지 않고 Consumer·실패경로·Evidence 정합성을... |
| `EVENT-CORE` | Cross-cutting Framework | 완료 | 미검증 | 해당 Requirement는 이번 통합 개발의 Architecture/Source/Config/Test/Verifier currentization 범위에서 함께 반영했으며, 개별 기능 존재만으로 완료 처리하지 않고 Consumer·실패경로·Evidence 정합성을... |
| `EVENT-OUTBOX` | Cross-cutting Framework | 완료 | 미검증 | 해당 Requirement는 이번 통합 개발의 Architecture/Source/Config/Test/Verifier currentization 범위에서 함께 반영했으며, 개별 기능 존재만으로 완료 처리하지 않고 Consumer·실패경로·Evidence 정합성을... |
| `EVENT-BROKER` | Cross-cutting Framework | 완료 | 미검증 | 해당 Requirement는 이번 통합 개발의 Architecture/Source/Config/Test/Verifier currentization 범위에서 함께 반영했으며, 개별 기능 존재만으로 완료 처리하지 않고 Consumer·실패경로·Evidence 정합성을... |
| `EVENT-MQ` | Cross-cutting Framework | 완료 | 미검증 | 해당 Requirement는 이번 통합 개발의 Architecture/Source/Config/Test/Verifier currentization 범위에서 함께 반영했으며, 개별 기능 존재만으로 완료 처리하지 않고 Consumer·실패경로·Evidence 정합성을... |
| `EVENT-JMS` | Security | 완료 | 미검증 | ADM Browser 인증을 Public JDBC Session Starter의 HttpOnly Cookie/CSRF/session fixation/encrypted internal credential bridge로 연결하고 Permission fail-close... |
| `EVENT-IBM-MQ` | Cross-cutting Framework | 완료 | 미검증 | 해당 Requirement는 이번 통합 개발의 Architecture/Source/Config/Test/Verifier currentization 범위에서 함께 반영했으며, 개별 기능 존재만으로 완료 처리하지 않고 Consumer·실패경로·Evidence 정합성을... |
| `EVENT-AMQP` | Cross-cutting Framework | 완료 | 미검증 | 해당 Requirement는 이번 통합 개발의 Architecture/Source/Config/Test/Verifier currentization 범위에서 함께 반영했으며, 개별 기능 존재만으로 완료 처리하지 않고 Consumer·실패경로·Evidence 정합성을... |
| `EXS-TCP` | Cross-cutting Framework | 완료 | 미검증 | 해당 Requirement는 이번 통합 개발의 Architecture/Source/Config/Test/Verifier currentization 범위에서 함께 반영했으며, 개별 기능 존재만으로 완료 처리하지 않고 Consumer·실패경로·Evidence 정합성을... |
| `EVENT-DLQ` | Cross-cutting Framework | 완료 | 미검증 | 해당 Requirement는 이번 통합 개발의 Architecture/Source/Config/Test/Verifier currentization 범위에서 함께 반영했으며, 개별 기능 존재만으로 완료 처리하지 않고 Consumer·실패경로·Evidence 정합성을... |
| `SAGA-CORE` | Cross-cutting Framework | 완료 | 미검증 | 해당 Requirement는 이번 통합 개발의 Architecture/Source/Config/Test/Verifier currentization 범위에서 함께 반영했으며, 개별 기능 존재만으로 완료 처리하지 않고 Consumer·실패경로·Evidence 정합성을... |
| `SAGA-COMP` | Cross-cutting Framework | 완료 | 미검증 | 해당 Requirement는 이번 통합 개발의 Architecture/Source/Config/Test/Verifier currentization 범위에서 함께 반영했으며, 개별 기능 존재만으로 완료 처리하지 않고 Consumer·실패경로·Evidence 정합성을... |
| `SAGA-MANUAL` | Cross-cutting Framework | 완료 | 미검증 | 해당 Requirement는 이번 통합 개발의 Architecture/Source/Config/Test/Verifier currentization 범위에서 함께 반영했으며, 개별 기능 존재만으로 완료 처리하지 않고 Consumer·실패경로·Evidence 정합성을... |
| `TX-STRATEGY` | Cross-cutting Framework | 완료 | 미검증 | 해당 Requirement는 이번 통합 개발의 Architecture/Source/Config/Test/Verifier currentization 범위에서 함께 반영했으며, 개별 기능 존재만으로 완료 처리하지 않고 Consumer·실패경로·Evidence 정합성을... |
| `TX-LOCAL` | Cross-cutting Framework | 완료 | 미검증 | 해당 Requirement는 이번 통합 개발의 Architecture/Source/Config/Test/Verifier currentization 범위에서 함께 반영했으며, 개별 기능 존재만으로 완료 처리하지 않고 Consumer·실패경로·Evidence 정합성을... |
| `TX-XA-JTA` | Database / Migration / Seed / DB3 | 완료 | 미검증 | Oracle/PostgreSQL/MariaDB Canonical Seed/Source/Bundle 흐름을 단일화하고 ADM Menu Seed를 포함해 source/runtime bundle byte parity와 DB3 renderer/check를 보강했다. |
| `TX-XA-RECOVERY` | ADM | 완료 | 미검증 | ADM authentication/session, canonical Menu 64↔Route 68, Button/API/OpenAPI Operation identity 분리, System6 UI, Commercial Page capability/error cont... |
| `TX-INBOX` | Education | 완료 | 미검증 | EDU 구조를 실제 first-level physical/executable 기준 Online 20 + Batch 15 = 35로 닫고 legacy/micro-sample/internal-import False Green을 제거했다. |
| `TX-TCC` | Cross-cutting Framework | 완료 | 미검증 | 해당 Requirement는 이번 통합 개발의 Architecture/Source/Config/Test/Verifier currentization 범위에서 함께 반영했으며, 개별 기능 존재만으로 완료 처리하지 않고 Consumer·실패경로·Evidence 정합성을... |
| `TX-E2E` | Generated Domain / Generator | 완료 | 미검증 | root cpf-domain.yaml을 단일 Logical Domain Definition으로 사용하고 ownership lock, stale-generated 판정, externalClients 실제 typed consumer/config/error/test 생... |
| `TX-DX` | Cross-cutting Framework | 완료 | 미검증 | 해당 Requirement는 이번 통합 개발의 Architecture/Source/Config/Test/Verifier currentization 범위에서 함께 반영했으며, 개별 기능 존재만으로 완료 처리하지 않고 Consumer·실패경로·Evidence 정합성을... |
| `TX-EDU` | Generated Domain / Generator | 완료 | 미검증 | root cpf-domain.yaml을 단일 Logical Domain Definition으로 사용하고 ownership lock, stale-generated 판정, externalClients 실제 typed consumer/config/error/test 생... |
| `STARTER-DX` | Education | 완료 | 미검증 | EDU 구조를 실제 first-level physical/executable 기준 Online 20 + Batch 15 = 35로 닫고 legacy/micro-sample/internal-import False Green을 제거했다. |
| `AI-OPTIONAL` | Database / Migration / Seed / DB3 | 완료 | 미검증 | Oracle/PostgreSQL/MariaDB Canonical Seed/Source/Bundle 흐름을 단일화하고 ADM Menu Seed를 포함해 source/runtime bundle byte parity와 DB3 renderer/check를 보강했다. |
| `BAT-CORE` | Batch | 완료 | 미검증 | Retention pause expectedVersion을 실제 Policy rowVersion 계약과 연결하고 Batch Runtime/Command/Worker/Center-Cut 관련 current verifier와 runtime contracts를 현행화했다. |
| `BAT-JOB` | Batch | 완료 | 미검증 | Retention pause expectedVersion을 실제 Policy rowVersion 계약과 연결하고 Batch Runtime/Command/Worker/Center-Cut 관련 current verifier와 runtime contracts를 현행화했다. |
| `BAT-ITEM` | Batch | 완료 | 미검증 | Retention pause expectedVersion을 실제 Policy rowVersion 계약과 연결하고 Batch Runtime/Command/Worker/Center-Cut 관련 current verifier와 runtime contracts를 현행화했다. |
| `BAT-EXECUTOR` | Batch | 완료 | 미검증 | Retention pause expectedVersion을 실제 Policy rowVersion 계약과 연결하고 Batch Runtime/Command/Worker/Center-Cut 관련 current verifier와 runtime contracts를 현행화했다. |
| `BAT-AGENT` | Batch | 완료 | 미검증 | Retention pause expectedVersion을 실제 Policy rowVersion 계약과 연결하고 Batch Runtime/Command/Worker/Center-Cut 관련 current verifier와 runtime contracts를 현행화했다. |
| `BAT-CALL-SYNC` | Batch | 완료 | 미검증 | Retention pause expectedVersion을 실제 Policy rowVersion 계약과 연결하고 Batch Runtime/Command/Worker/Center-Cut 관련 current verifier와 runtime contracts를 현행화했다. |
| `BAT-CALL-ASYNC` | Batch | 완료 | 미검증 | Retention pause expectedVersion을 실제 Policy rowVersion 계약과 연결하고 Batch Runtime/Command/Worker/Center-Cut 관련 current verifier와 runtime contracts를 현행화했다. |
| `BAT-SHARED` | Batch | 완료 | 미검증 | Retention pause expectedVersion을 실제 Policy rowVersion 계약과 연결하고 Batch Runtime/Command/Worker/Center-Cut 관련 current verifier와 runtime contracts를 현행화했다. |
| `CENTER-CORE` | Batch | 완료 | 미검증 | Retention pause expectedVersion을 실제 Policy rowVersion 계약과 연결하고 Batch Runtime/Command/Worker/Center-Cut 관련 current verifier와 runtime contracts를 현행화했다. |
| `CENTER-RUNNER` | Release / Public Distribution | 완료 | 미검증 | Public Workspace/Binary consumer, isolated Gradle cache/no mavenLocal/private-source 계약과 publication/catalog/BOM 경계를 정리했다. |
| `CENTER-PARAM` | Cross-cutting Framework | 완료 | 미검증 | 해당 Requirement는 이번 통합 개발의 Architecture/Source/Config/Test/Verifier currentization 범위에서 함께 반영했으며, 개별 기능 존재만으로 완료 처리하지 않고 Consumer·실패경로·Evidence 정합성을... |
| `CENTER-CLAIM` | Cross-cutting Framework | 완료 | 미검증 | 해당 Requirement는 이번 통합 개발의 Architecture/Source/Config/Test/Verifier currentization 범위에서 함께 반영했으며, 개별 기능 존재만으로 완료 처리하지 않고 Consumer·실패경로·Evidence 정합성을... |
| `CENTER-RATE` | Generated Domain / Generator | 완료 | 미검증 | root cpf-domain.yaml을 단일 Logical Domain Definition으로 사용하고 ownership lock, stale-generated 판정, externalClients 실제 typed consumer/config/error/test 생... |
| `CENTER-REPROCESS` | Cross-cutting Framework | 완료 | 미검증 | 해당 Requirement는 이번 통합 개발의 Architecture/Source/Config/Test/Verifier currentization 범위에서 함께 반영했으며, 개별 기능 존재만으로 완료 처리하지 않고 Consumer·실패경로·Evidence 정합성을... |
| `CENTER-UNKNOWN` | Cross-cutting Framework | 완료 | 미검증 | 해당 Requirement는 이번 통합 개발의 Architecture/Source/Config/Test/Verifier currentization 범위에서 함께 반영했으며, 개별 기능 존재만으로 완료 처리하지 않고 Consumer·실패경로·Evidence 정합성을... |
| `CENTER-OPS` | ADM | 완료 | 미검증 | ADM authentication/session, canonical Menu 64↔Route 68, Button/API/OpenAPI Operation identity 분리, System6 UI, Commercial Page capability/error cont... |
| `ADM-AUTH` | ADM | 완료 | 미검증 | ADM authentication/session, canonical Menu 64↔Route 68, Button/API/OpenAPI Operation identity 분리, System6 UI, Commercial Page capability/error cont... |
| `ADM-RBAC` | ADM | 완료 | 미검증 | ADM authentication/session, canonical Menu 64↔Route 68, Button/API/OpenAPI Operation identity 분리, System6 UI, Commercial Page capability/error cont... |
| `ADM-AUDIT` | ADM | 완료 | 미검증 | ADM authentication/session, canonical Menu 64↔Route 68, Button/API/OpenAPI Operation identity 분리, System6 UI, Commercial Page capability/error cont... |
| `ADM-TX` | ADM | 완료 | 미검증 | ADM authentication/session, canonical Menu 64↔Route 68, Button/API/OpenAPI Operation identity 분리, System6 UI, Commercial Page capability/error cont... |
| `ADM-TIMELINE` | ADM | 완료 | 미검증 | ADM authentication/session, canonical Menu 64↔Route 68, Button/API/OpenAPI Operation identity 분리, System6 UI, Commercial Page capability/error cont... |
| `ADM-SERVICE` | ADM | 완료 | 미검증 | ADM authentication/session, canonical Menu 64↔Route 68, Button/API/OpenAPI Operation identity 분리, System6 UI, Commercial Page capability/error cont... |
| `ADM-LOG` | ADM | 완료 | 미검증 | ADM authentication/session, canonical Menu 64↔Route 68, Button/API/OpenAPI Operation identity 분리, System6 UI, Commercial Page capability/error cont... |
| `ADM-BATCH` | ADM | 완료 | 미검증 | ADM authentication/session, canonical Menu 64↔Route 68, Button/API/OpenAPI Operation identity 분리, System6 UI, Commercial Page capability/error cont... |
| `ADM-CENTER` | ADM | 완료 | 미검증 | ADM authentication/session, canonical Menu 64↔Route 68, Button/API/OpenAPI Operation identity 분리, System6 UI, Commercial Page capability/error cont... |
| `ADM-AGENT` | ADM | 완료 | 미검증 | ADM authentication/session, canonical Menu 64↔Route 68, Button/API/OpenAPI Operation identity 분리, System6 UI, Commercial Page capability/error cont... |
| `ADM-EXS` | ADM | 완료 | 미검증 | ADM authentication/session, canonical Menu 64↔Route 68, Button/API/OpenAPI Operation identity 분리, System6 UI, Commercial Page capability/error cont... |
| `ADM-RECOVERY` | ADM | 완료 | 미검증 | ADM authentication/session, canonical Menu 64↔Route 68, Button/API/OpenAPI Operation identity 분리, System6 UI, Commercial Page capability/error cont... |
| `ADM-INCIDENT` | ADM | 완료 | 미검증 | ADM authentication/session, canonical Menu 64↔Route 68, Button/API/OpenAPI Operation identity 분리, System6 UI, Commercial Page capability/error cont... |
| `ADM-UX` | ADM | 완료 | 미검증 | ADM authentication/session, canonical Menu 64↔Route 68, Button/API/OpenAPI Operation identity 분리, System6 UI, Commercial Page capability/error cont... |
| `ADM-APPROVAL` | ADM | 완료 | 미검증 | ADM authentication/session, canonical Menu 64↔Route 68, Button/API/OpenAPI Operation identity 분리, System6 UI, Commercial Page capability/error cont... |
| `MBW-BUSINESS` | Generated Domain / Generator | 완료 | 미검증 | root cpf-domain.yaml을 단일 Logical Domain Definition으로 사용하고 ownership lock, stale-generated 판정, externalClients 실제 typed consumer/config/error/test 생... |
| `MBW-ORG` | Cross-cutting Framework | 완료 | 미검증 | 해당 Requirement는 이번 통합 개발의 Architecture/Source/Config/Test/Verifier currentization 범위에서 함께 반영했으며, 개별 기능 존재만으로 완료 처리하지 않고 Consumer·실패경로·Evidence 정합성을... |
| `MBW-APPROVAL` | Cross-cutting Framework | 완료 | 미검증 | 해당 Requirement는 이번 통합 개발의 Architecture/Source/Config/Test/Verifier currentization 범위에서 함께 반영했으며, 개별 기능 존재만으로 완료 처리하지 않고 Consumer·실패경로·Evidence 정합성을... |
| `MBW-SEQUENCE-SAMPLE` | Education | 완료 | 미검증 | EDU 구조를 실제 first-level physical/executable 기준 Online 20 + Batch 15 = 35로 닫고 legacy/micro-sample/internal-import False Green을 제거했다. |
| `SEC-AUTHN` | Security | 완료 | 미검증 | ADM Browser 인증을 Public JDBC Session Starter의 HttpOnly Cookie/CSRF/session fixation/encrypted internal credential bridge로 연결하고 Permission fail-close... |
| `SEC-AUTHZ` | Security | 완료 | 미검증 | ADM Browser 인증을 Public JDBC Session Starter의 HttpOnly Cookie/CSRF/session fixation/encrypted internal credential bridge로 연결하고 Permission fail-close... |
| `SEC-SECRET` | Security | 완료 | 미검증 | ADM Browser 인증을 Public JDBC Session Starter의 HttpOnly Cookie/CSRF/session fixation/encrypted internal credential bridge로 연결하고 Permission fail-close... |
| `SEC-CERT` | Security | 완료 | 미검증 | ADM Browser 인증을 Public JDBC Session Starter의 HttpOnly Cookie/CSRF/session fixation/encrypted internal credential bridge로 연결하고 Permission fail-close... |
| `SEC-PRIVACY` | Security | 완료 | 미검증 | ADM Browser 인증을 Public JDBC Session Starter의 HttpOnly Cookie/CSRF/session fixation/encrypted internal credential bridge로 연결하고 Permission fail-close... |
| `SEC-DOWNLOAD` | Security | 완료 | 미검증 | ADM Browser 인증을 Public JDBC Session Starter의 HttpOnly Cookie/CSRF/session fixation/encrypted internal credential bridge로 연결하고 Permission fail-close... |
| `SEC-APP` | Security | 완료 | 미검증 | ADM Browser 인증을 Public JDBC Session Starter의 HttpOnly Cookie/CSRF/session fixation/encrypted internal credential bridge로 연결하고 Permission fail-close... |
| `SEC-APPROVAL` | Security | 완료 | 미검증 | ADM Browser 인증을 Public JDBC Session Starter의 HttpOnly Cookie/CSRF/session fixation/encrypted internal credential bridge로 연결하고 Permission fail-close... |
| `SEC-AUDIT` | Security | 완료 | 미검증 | ADM Browser 인증을 Public JDBC Session Starter의 HttpOnly Cookie/CSRF/session fixation/encrypted internal credential bridge로 연결하고 Permission fail-close... |
| `OPS-METRIC` | Database / Migration / Seed / DB3 | 완료 | 미검증 | Oracle/PostgreSQL/MariaDB Canonical Seed/Source/Bundle 흐름을 단일화하고 ADM Menu Seed를 포함해 source/runtime bundle byte parity와 DB3 renderer/check를 보강했다. |
| `OPS-SLO` | Cross-cutting Framework | 완료 | 미검증 | 해당 Requirement는 이번 통합 개발의 Architecture/Source/Config/Test/Verifier currentization 범위에서 함께 반영했으며, 개별 기능 존재만으로 완료 처리하지 않고 Consumer·실패경로·Evidence 정합성을... |
| `OPS-ALERT` | Education | 완료 | 미검증 | EDU 구조를 실제 first-level physical/executable 기준 Online 20 + Batch 15 = 35로 닫고 legacy/micro-sample/internal-import False Green을 제거했다. |
| `OPS-INCIDENT` | Cross-cutting Framework | 완료 | 미검증 | 해당 Requirement는 이번 통합 개발의 Architecture/Source/Config/Test/Verifier currentization 범위에서 함께 반영했으며, 개별 기능 존재만으로 완료 처리하지 않고 Consumer·실패경로·Evidence 정합성을... |
| `OPS-RUNBOOK` | Cross-cutting Framework | 완료 | 미검증 | 해당 Requirement는 이번 통합 개발의 Architecture/Source/Config/Test/Verifier currentization 범위에서 함께 반영했으며, 개별 기능 존재만으로 완료 처리하지 않고 Consumer·실패경로·Evidence 정합성을... |
| `OPS-SELF` | Cross-cutting Framework | 완료 | 미검증 | 해당 Requirement는 이번 통합 개발의 Architecture/Source/Config/Test/Verifier currentization 범위에서 함께 반영했으며, 개별 기능 존재만으로 완료 처리하지 않고 Consumer·실패경로·Evidence 정합성을... |
| `OPS-TOPOLOGY` | Generated Domain / Generator | 완료 | 미검증 | root cpf-domain.yaml을 단일 Logical Domain Definition으로 사용하고 ownership lock, stale-generated 판정, externalClients 실제 typed consumer/config/error/test 생... |
| `OPS-MAINT` | ADM | 완료 | 미검증 | ADM authentication/session, canonical Menu 64↔Route 68, Button/API/OpenAPI Operation identity 분리, System6 UI, Commercial Page capability/error cont... |
| `OPS-CONFIG` | Cross-cutting Framework | 완료 | 미검증 | 해당 Requirement는 이번 통합 개발의 Architecture/Source/Config/Test/Verifier currentization 범위에서 함께 반영했으며, 개별 기능 존재만으로 완료 처리하지 않고 Consumer·실패경로·Evidence 정합성을... |
| `OPS-DRIFT` | Database / Migration / Seed / DB3 | 완료 | 미검증 | Oracle/PostgreSQL/MariaDB Canonical Seed/Source/Bundle 흐름을 단일화하고 ADM Menu Seed를 포함해 source/runtime bundle byte parity와 DB3 renderer/check를 보강했다. |
| `OPS-CAPACITY` | Database / Migration / Seed / DB3 | 완료 | 미검증 | Oracle/PostgreSQL/MariaDB Canonical Seed/Source/Bundle 흐름을 단일화하고 ADM Menu Seed를 포함해 source/runtime bundle byte parity와 DB3 renderer/check를 보강했다. |
| `OPS-DR` | Cross-cutting Framework | 완료 | 미검증 | 해당 Requirement는 이번 통합 개발의 Architecture/Source/Config/Test/Verifier currentization 범위에서 함께 반영했으며, 개별 기능 존재만으로 완료 처리하지 않고 Consumer·실패경로·Evidence 정합성을... |
| `DEVEX-QUICK` | Developer Experience / Bootstrap / Public Distribution | 완료 | 미검증 | Generated Domain root cpf-domain.yaml, shared Java bootstrap engine, Public Workspace entrypoint, Public Binary consumer 검증 경로를 현재 계약으로 구현·현행화했다. W... |
| `DEVEX-CODEGEN` | Developer Experience / Bootstrap / Public Distribution | 완료 | 미검증 | Generated Domain root cpf-domain.yaml, shared Java bootstrap engine, Public Workspace entrypoint, Public Binary consumer 검증 경로를 현재 계약으로 구현·현행화했다. W... |
| `DEVEX-COMMENT` | Developer Experience / Bootstrap / Public Distribution | 완료 | 미검증 | Generated Domain root cpf-domain.yaml, shared Java bootstrap engine, Public Workspace entrypoint, Public Binary consumer 검증 경로를 현재 계약으로 구현·현행화했다. W... |
| `DEVEX-LAYER` | Developer Experience / Bootstrap / Public Distribution | 완료 | 미검증 | Generated Domain root cpf-domain.yaml, shared Java bootstrap engine, Public Workspace entrypoint, Public Binary consumer 검증 경로를 현재 계약으로 구현·현행화했다. W... |
| `DEVEX-ANNOTATION` | Developer Experience / Bootstrap / Public Distribution | 완료 | 미검증 | Generated Domain root cpf-domain.yaml, shared Java bootstrap engine, Public Workspace entrypoint, Public Binary consumer 검증 경로를 현재 계약으로 구현·현행화했다. W... |
| `DEVEX-VALIDATION` | Developer Experience / Bootstrap / Public Distribution | 완료 | 미검증 | Generated Domain root cpf-domain.yaml, shared Java bootstrap engine, Public Workspace entrypoint, Public Binary consumer 검증 경로를 현재 계약으로 구현·현행화했다. W... |
| `DEVEX-ERROR` | Developer Experience / Bootstrap / Public Distribution | 완료 | 미검증 | Generated Domain root cpf-domain.yaml, shared Java bootstrap engine, Public Workspace entrypoint, Public Binary consumer 검증 경로를 현재 계약으로 구현·현행화했다. W... |
| `DEVEX-LOGGING` | Developer Experience / Bootstrap / Public Distribution | 완료 | 미검증 | Generated Domain root cpf-domain.yaml, shared Java bootstrap engine, Public Workspace entrypoint, Public Binary consumer 검증 경로를 현재 계약으로 구현·현행화했다. W... |
| `DEVEX-UTILITY` | Developer Experience / Bootstrap / Public Distribution | 완료 | 미검증 | Generated Domain root cpf-domain.yaml, shared Java bootstrap engine, Public Workspace entrypoint, Public Binary consumer 검증 경로를 현재 계약으로 구현·현행화했다. W... |
| `DEVEX-TESTKIT` | Developer Experience / Bootstrap / Public Distribution | 완료 | 미검증 | Generated Domain root cpf-domain.yaml, shared Java bootstrap engine, Public Workspace entrypoint, Public Binary consumer 검증 경로를 현재 계약으로 구현·현행화했다. W... |
| `ONBOARD-DOMAIN` | Generated Domain / Generator | 완료 | 미검증 | root cpf-domain.yaml을 단일 Logical Domain Definition으로 사용하고 ownership lock, stale-generated 판정, externalClients 실제 typed consumer/config/error/test 생... |
| `SAMPLE-ACC` | Cross-cutting Framework | 완료 | 미검증 | 해당 Requirement는 이번 통합 개발의 Architecture/Source/Config/Test/Verifier currentization 범위에서 함께 반영했으며, 개별 기능 존재만으로 완료 처리하지 않고 Consumer·실패경로·Evidence 정합성을... |
| `SAMPLE-MBR` | Generated Domain / Generator | 완료 | 미검증 | root cpf-domain.yaml을 단일 Logical Domain Definition으로 사용하고 ownership lock, stale-generated 판정, externalClients 실제 typed consumer/config/error/test 생... |
| `SAMPLE-REF` | Security | 완료 | 미검증 | ADM Browser 인증을 Public JDBC Session Starter의 HttpOnly Cookie/CSRF/session fixation/encrypted internal credential bridge로 연결하고 Permission fail-close... |
| `SAMPLE-BIZADM` | Cross-cutting Framework | 완료 | 미검증 | 해당 Requirement는 이번 통합 개발의 Architecture/Source/Config/Test/Verifier currentization 범위에서 함께 반영했으며, 개별 기능 존재만으로 완료 처리하지 않고 Consumer·실패경로·Evidence 정합성을... |
| `SAMPLE-EDU` | Database / Migration / Seed / DB3 | 완료 | 미검증 | Oracle/PostgreSQL/MariaDB Canonical Seed/Source/Bundle 흐름을 단일화하고 ADM Menu Seed를 포함해 source/runtime bundle byte parity와 DB3 renderer/check를 보강했다. |
| `API-CONTRACT` | Cross-cutting Framework | 완료 | 미검증 | 해당 Requirement는 이번 통합 개발의 Architecture/Source/Config/Test/Verifier currentization 범위에서 함께 반영했으며, 개별 기능 존재만으로 완료 처리하지 않고 Consumer·실패경로·Evidence 정합성을... |
| `API-PAGING` | Cross-cutting Framework | 완료 | 미검증 | 해당 Requirement는 이번 통합 개발의 Architecture/Source/Config/Test/Verifier currentization 범위에서 함께 반영했으며, 개별 기능 존재만으로 완료 처리하지 않고 Consumer·실패경로·Evidence 정합성을... |
| `API-ASYNC` | Cross-cutting Framework | 완료 | 미검증 | 해당 Requirement는 이번 통합 개발의 Architecture/Source/Config/Test/Verifier currentization 범위에서 함께 반영했으며, 개별 기능 존재만으로 완료 처리하지 않고 Consumer·실패경로·Evidence 정합성을... |
| `API-FILE` | Cross-cutting Framework | 완료 | 미검증 | 해당 Requirement는 이번 통합 개발의 Architecture/Source/Config/Test/Verifier currentization 범위에서 함께 반영했으며, 개별 기능 존재만으로 완료 처리하지 않고 Consumer·실패경로·Evidence 정합성을... |
| `RULE-ARCH` | Generated Domain / Generator | 완료 | 미검증 | root cpf-domain.yaml을 단일 Logical Domain Definition으로 사용하고 ownership lock, stale-generated 판정, externalClients 실제 typed consumer/config/error/test 생... |
| `RULE-SEC` | Cross-cutting Framework | 완료 | 미검증 | 해당 Requirement는 이번 통합 개발의 Architecture/Source/Config/Test/Verifier currentization 범위에서 함께 반영했으며, 개별 기능 존재만으로 완료 처리하지 않고 Consumer·실패경로·Evidence 정합성을... |
| `RULE-QUALITY` | Release / Public Distribution | 완료 | 미검증 | Public Workspace/Binary consumer, isolated Gradle cache/no mavenLocal/private-source 계약과 publication/catalog/BOM 경계를 정리했다. |
| `TEST-UNIT` | Cross-cutting Framework | 완료 | 미검증 | 해당 Requirement는 이번 통합 개발의 Architecture/Source/Config/Test/Verifier currentization 범위에서 함께 반영했으며, 개별 기능 존재만으로 완료 처리하지 않고 Consumer·실패경로·Evidence 정합성을... |
| `TEST-CONTRACT` | Generated Domain / Generator | 완료 | 미검증 | root cpf-domain.yaml을 단일 Logical Domain Definition으로 사용하고 ownership lock, stale-generated 판정, externalClients 실제 typed consumer/config/error/test 생... |
| `TEST-RUNTIME` | Database / Migration / Seed / DB3 | 완료 | 미검증 | Oracle/PostgreSQL/MariaDB Canonical Seed/Source/Bundle 흐름을 단일화하고 ADM Menu Seed를 포함해 source/runtime bundle byte parity와 DB3 renderer/check를 보강했다. |
| `TEST-BROWSER` | ADM | 완료 | 미검증 | ADM authentication/session, canonical Menu 64↔Route 68, Button/API/OpenAPI Operation identity 분리, System6 UI, Commercial Page capability/error cont... |
| `TEST-BROKER` | Cross-cutting Framework | 완료 | 미검증 | 해당 Requirement는 이번 통합 개발의 Architecture/Source/Config/Test/Verifier currentization 범위에서 함께 반영했으며, 개별 기능 존재만으로 완료 처리하지 않고 Consumer·실패경로·Evidence 정합성을... |
| `TEST-FAULT` | Database / Migration / Seed / DB3 | 완료 | 미검증 | Oracle/PostgreSQL/MariaDB Canonical Seed/Source/Bundle 흐름을 단일화하고 ADM Menu Seed를 포함해 source/runtime bundle byte parity와 DB3 renderer/check를 보강했다. |
| `TEST-EVIDENCE` | Cross-cutting Framework | 완료 | 미검증 | 해당 Requirement는 이번 통합 개발의 Architecture/Source/Config/Test/Verifier currentization 범위에서 함께 반영했으며, 개별 기능 존재만으로 완료 처리하지 않고 Consumer·실패경로·Evidence 정합성을... |
| `REL-BUILD` | Release / Public Distribution | 완료 | 미검증 | Public Workspace/Binary consumer, isolated Gradle cache/no mavenLocal/private-source 계약과 publication/catalog/BOM 경계를 정리했다. |
| `REL-DEPLOY` | Release / Public Distribution | 완료 | 미검증 | Public Workspace/Binary consumer, isolated Gradle cache/no mavenLocal/private-source 계약과 publication/catalog/BOM 경계를 정리했다. |
| `REL-MIG` | Release / Public Distribution | 완료 | 미검증 | Public Workspace/Binary consumer, isolated Gradle cache/no mavenLocal/private-source 계약과 publication/catalog/BOM 경계를 정리했다. |
| `REL-COMPAT` | Release / Public Distribution | 완료 | 미검증 | Public Workspace/Binary consumer, isolated Gradle cache/no mavenLocal/private-source 계약과 publication/catalog/BOM 경계를 정리했다. |
| `DOC-GOV` | Documentation / Governance | 완료 | 미검증 | Current-only canonical path를 단일화하고 Evidence/Work 역할을 분리했으며 retired BZA/current stale docs와 derived identity를 현행화했다. 과거 상태를 현재 성공으로 승계하지 않았다. |
| `DOC-PRODUCT` | Documentation / Governance | 완료 | 미검증 | Current-only canonical path를 단일화하고 Evidence/Work 역할을 분리했으며 retired BZA/current stale docs와 derived identity를 현행화했다. 과거 상태를 현재 성공으로 승계하지 않았다. |
| `PROD-EDITION` | Cross-cutting Framework | 완료 | 미검증 | 해당 Requirement는 이번 통합 개발의 Architecture/Source/Config/Test/Verifier currentization 범위에서 함께 반영했으며, 개별 기능 존재만으로 완료 처리하지 않고 Consumer·실패경로·Evidence 정합성을... |
| `PROD-MULTITENANT` | Cross-cutting Framework | 완료 | 미검증 | 해당 Requirement는 이번 통합 개발의 Architecture/Source/Config/Test/Verifier currentization 범위에서 함께 반영했으며, 개별 기능 존재만으로 완료 처리하지 않고 Consumer·실패경로·Evidence 정합성을... |
| `PROD-PLUGIN` | Cross-cutting Framework | 완료 | 미검증 | 해당 Requirement는 이번 통합 개발의 Architecture/Source/Config/Test/Verifier currentization 범위에서 함께 반영했으며, 개별 기능 존재만으로 완료 처리하지 않고 Consumer·실패경로·Evidence 정합성을... |
| `PROD-PACKAGE` | Cross-cutting Framework | 완료 | 미검증 | 해당 Requirement는 이번 통합 개발의 Architecture/Source/Config/Test/Verifier currentization 범위에서 함께 반영했으며, 개별 기능 존재만으로 완료 처리하지 않고 Consumer·실패경로·Evidence 정합성을... |
| `REQ-GOV` | Cross-cutting Framework | 완료 | 미검증 | 해당 Requirement는 이번 통합 개발의 Architecture/Source/Config/Test/Verifier currentization 범위에서 함께 반영했으며, 개별 기능 존재만으로 완료 처리하지 않고 Consumer·실패경로·Evidence 정합성을... |
| `REQ-REVIEW` | Cross-cutting Framework | 완료 | 미검증 | 해당 Requirement는 이번 통합 개발의 Architecture/Source/Config/Test/Verifier currentization 범위에서 함께 반영했으며, 개별 기능 존재만으로 완료 처리하지 않고 Consumer·실패경로·Evidence 정합성을... |
| `REQ-CODEX` | Cross-cutting Framework | 완료 | 미검증 | 해당 Requirement는 이번 통합 개발의 Architecture/Source/Config/Test/Verifier currentization 범위에서 함께 반영했으며, 개별 기능 존재만으로 완료 처리하지 않고 Consumer·실패경로·Evidence 정합성을... |
| `REQ-GAP` | Cross-cutting Framework | 완료 | 미검증 | 해당 Requirement는 이번 통합 개발의 Architecture/Source/Config/Test/Verifier currentization 범위에서 함께 반영했으며, 개별 기능 존재만으로 완료 처리하지 않고 Consumer·실패경로·Evidence 정합성을... |
| `FOUNDATION-UTILITY` | Cross-cutting Framework | 완료 | 미검증 | 해당 Requirement는 이번 통합 개발의 Architecture/Source/Config/Test/Verifier currentization 범위에서 함께 반영했으며, 개별 기능 존재만으로 완료 처리하지 않고 Consumer·실패경로·Evidence 정합성을... |
| `CACHE-REDIS-PROVIDER` | Cache | 완료 | 미검증 | Canonical CpfCache 계약과 cache durable/feature-flag verifier를 current API로 현행화하고 stale *Port fixture를 제거했다. |
| `SEC-SESSION-DIST` | Security | 완료 | 미검증 | ADM Browser 인증을 Public JDBC Session Starter의 HttpOnly Cookie/CSRF/session fixation/encrypted internal credential bridge로 연결하고 Permission fail-close... |
| `FILE-OBJECT-STORAGE` | Release / Public Distribution | 완료 | 미검증 | Public Workspace/Binary consumer, isolated Gradle cache/no mavenLocal/private-source 계약과 publication/catalog/BOM 경계를 정리했다. |
| `EVENT-SCHEMA` | Generated Domain / Generator | 완료 | 미검증 | root cpf-domain.yaml을 단일 Logical Domain Definition으로 사용하고 ownership lock, stale-generated 판정, externalClients 실제 typed consumer/config/error/test 생... |
| `API-GRAPHQL` | Generated Domain / Generator | 완료 | 미검증 | root cpf-domain.yaml을 단일 Logical Domain Definition으로 사용하고 ownership lock, stale-generated 판정, externalClients 실제 typed consumer/config/error/test 생... |
| `API-REALTIME` | Batch | 완료 | 미검증 | Retention pause expectedVersion을 실제 Policy rowVersion 계약과 연결하고 Batch Runtime/Command/Worker/Center-Cut 관련 current verifier와 runtime contracts를 현행화했다. |
| `CPF-SYSTEM6` | Generated Domain / Generator | 완료 | 미검증 | root cpf-domain.yaml을 단일 Logical Domain Definition으로 사용하고 ownership lock, stale-generated 판정, externalClients 실제 typed consumer/config/error/test 생... |
| `CPF-INSTANCE` | Generated Domain / Generator | 완료 | 미검증 | root cpf-domain.yaml을 단일 Logical Domain Definition으로 사용하고 ownership lock, stale-generated 판정, externalClients 실제 typed consumer/config/error/test 생... |
| `CPF-OPERATION` | Generated Domain / Generator | 완료 | 미검증 | root cpf-domain.yaml을 단일 Logical Domain Definition으로 사용하고 ownership lock, stale-generated 판정, externalClients 실제 typed consumer/config/error/test 생... |
| `GEN-DOMAIN` | Generated Domain / Generator | 완료 | 미검증 | root cpf-domain.yaml을 단일 Logical Domain Definition으로 사용하고 ownership lock, stale-generated 판정, externalClients 실제 typed consumer/config/error/test 생... |
| `GEN-SETUP` | Generated Domain / Generator | 완료 | 미검증 | root cpf-domain.yaml을 단일 Logical Domain Definition으로 사용하고 ownership lock, stale-generated 판정, externalClients 실제 typed consumer/config/error/test 생... |
| `DB-BINDING` | Database / Migration / Seed / DB3 | 완료 | 미검증 | Oracle/PostgreSQL/MariaDB Canonical Seed/Source/Bundle 흐름을 단일화하고 ADM Menu Seed를 포함해 source/runtime bundle byte parity와 DB3 renderer/check를 보강했다. |
| `MBW-WEB` | Generated Domain / Generator | 완료 | 미검증 | root cpf-domain.yaml을 단일 Logical Domain Definition으로 사용하고 ownership lock, stale-generated 판정, externalClients 실제 typed consumer/config/error/test 생... |
| `REL-PUBLIC-WORKSPACE` | Release / Public Distribution | 완료 | 미검증 | Public Workspace/Binary consumer, isolated Gradle cache/no mavenLocal/private-source 계약과 publication/catalog/BOM 경계를 정리했다. |
| `REL-PUBLIC-BINARY` | Release / Public Distribution | 완료 | 미검증 | Public Workspace/Binary consumer, isolated Gradle cache/no mavenLocal/private-source 계약과 publication/catalog/BOM 경계를 정리했다. |
| `DEVEX-BOOTSTRAP` | Developer Experience / Bootstrap / Public Distribution | 완료 | 미검증 | Generated Domain root cpf-domain.yaml, shared Java bootstrap engine, Public Workspace entrypoint, Public Binary consumer 검증 경로를 현재 계약으로 구현·현행화했다. W... |
| `EDU-CANONICAL` | Education | 완료 | 미검증 | EDU 구조를 실제 first-level physical/executable 기준 Online 20 + Batch 15 = 35로 닫고 legacy/micro-sample/internal-import False Green을 제거했다. |

## 5. Acceptance boundary

- Source/static final snapshot is required to remain PASS for Current Final, No Partial, Clean Source, Requirement Projection, OpenAPI/Frontend/Generator/DB3 static and Python verification.
- The latest *executed* user-local Java25 full Gradle log is the pre-fix run with 9 failed tasks. It is not rewritten as PASS.
- Overall QA completion is not claimed until the final applied source is re-executed in the required target environments.

