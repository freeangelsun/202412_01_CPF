> Development Harness 내부 통합 표준. 이 파일은 독립 정본이 아니며 `CPF_DEVELOPMENT_HARNESS.md`의 통제를 받는다.

# CPF Canonical Path and Role Map

> 역할: **경로/Owner Navigation 전용**. Architecture 정책은 `../product/CPF_PRODUCT_ARCHITECTURE_AND_REQUIREMENTS.md`만 정본으로 사용한다.

## 1. Product Owner Map

| 역할 | Canonical Root | 비고 |
|---|---|---|
| 최소 Kernel | `cpf-core/` | topology-independent Contract/Semantics |
| 고객 업무 공통 | `cpf-common/` | 공통 코드/메시지/영업일/Template 등 |
| Platform Control Plane | `cpf-admin/` | ADM |
| Optional Business Backoffice | `cpf-backoffice/` | SystemCode `MBW`, logical DB `mbwDB` |
| Backoffice Channel/BFF | `cpf-backoffice-web/` | DB/CPF Internal Java dependency 0 |
| Batch Runtime | `cpf-batch/` | Batch/Worker/Scheduler/Center-Cut |
| Gateway | `cpf-gateway/` | Edge/Trust/Route |
| Public Starter/Capability | `cpf-starters/` | Runtime composition/Provider |
| Generator/DB/Build/QA Tooling | `cpf-tools/` | product runtime owner로 사용 금지 |
| Unified CLI | `cpf-tools/runtime/cli/` | exactly-one `cpf` Java CLI owner; Engine 복제 금지 |
| Open Git Release Engine | `cpf-tools/release/open-git/` | Unified CLI INTERNAL `cpf release open-git`가 소비하는 canonical release engine |
| Open Git Generated Staging | `cpf-release/` | local-generated Current Release 전용; Private master Git/Source Identity 제외; Open Git 사용자 반영 대상 |
| Education | `cpf-education/` | Online 20 + Batch 15 |
| Generated Business Domain | `cpf-<domain>/` | Feature-First |

## 2. Generated Domain Canonical Path

```text
cpf-<domain>/
  build.gradle
  settings.gradle
  gradle.properties                              # cpf.domain.* Developer Contract
  online/<business-feature>/<technical-role>/
  batch/<business-feature>/<technical-role>/   # batch=true only
  domain/                                      # actual shared consumer exists only
```

Generator 입력 descriptor, lock/state/manifest/cache/evidence, `.cpf/`, DB Vendor tree는 Generated Customer Domain Root에 두지 않는다. 환경별 local DB binding은 `cpf-docs/governance/development-harness/evidence/generated/current/domain-generator/cpf-local/<domain>/cpf-db-profile.local.json`이 소유하며 raw secret을 저장하지 않는다.

## 3. Current Governance Navigation

| 역할 | 위치 |
|---|---|
| 최상위 개발 정본 | `cpf-docs/governance/development-harness/product/CPF_PRODUCT_ARCHITECTURE_AND_REQUIREMENTS.md` |
| Canonical Path/Owner Map | `cpf-docs/governance/development-harness/standards/CPF_ARCHITECTURE_PATH_AND_ROLE_STANDARD.md` |
| 문서 역할 Index | `cpf-docs/governance/development-harness/standards/CPF_CANONICAL_SURFACE_STANDARD.md` |
| 현재 개발 요청 | `cpf-docs/governance/development-harness/current/CPF_CURRENT_WORK_REQUEST.md` |
| Requirement 상태 | `cpf-docs/governance/development-harness/current/REQUIREMENT_STATUS.csv` |
| Current Evidence | `cpf-docs/governance/development-harness/current/TEST_AND_EVIDENCE.md` |
| CLI Command Catalog | `cpf-tools/runtime/cli/contracts/cpf-command-catalog.json` |
| Final Artifact Catalog | `cpf-tools/release/cpf-final-artifact-catalog.json` |
| Open Git Current Requirement | `cpf-docs/governance/development-harness/current/CPF_OPEN_GIT_FRESH_RELEASE_REQUIREMENT.md` |
| Open Git Current Work Package | `cpf-docs/governance/development-harness/current/CPF_OPEN_GIT_RELEASE_WORK_PACKAGE.md` |
| Open Issues | `cpf-docs/governance/development-harness/current/OPEN_ISSUES.md` |
| QA Rework Request | `cpf-docs/governance/development-harness/current/QA_REWORK_REQUEST.md` |
| Change/Delete/Package Manifest | `cpf-docs/governance/development-harness/current/CHANGE_MANIFEST.csv`, `DELETE_MANIFEST.csv`, `PACKAGE_MANIFEST.json` |
| 현재 세션/PC 인수인계 | `cpf-docs/governance/development-harness/current/CPF_DEVELOPMENT_HANDOVER.md` |

`cpf-docs/work/`에는 위 Deliverable 파일의 동명 복제본을 두지 않는다. `work/current`의 보조 실행자료는 최상위 Target을 변경하지 않으며, 오래된 Source identity나 완료 판정을 Current Evidence로 승계하지 않는다.

## 4. Public Distribution Navigation

Public Git Workspace와 Public Binary Repository는 별도 Deliverable이다. 실제 공개 경로/Artifact는 Final Target §21과 Canonical Catalog를 따른다.

## 5. 금지

이 문서에 Architecture 정책을 중복 복사하지 않는다. 경로가 바뀌면 이 Navigation과 Final Target의 Owner Map을 같은 변경에서 함께 수정한다.
