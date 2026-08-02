# CPF Repository Surface와 Root Ownership

- 기준 SHA: `1eda8e12fe123281748a4388938c62f11819da1e`
- 목적: Repository Root에 무엇이 존재해야 하는지, 누가 소유하는지, 새 Root를 어떻게 승인하는지 정의한다.

## 1. Root Surface

| Root | 분류 | 역할 | Owner | 상태 |
|---|---|---|---|---|
| `.github` | CI | CI·Workflow | Platform Build/QA | 유지 |
| `cpf-core` | FIXED_PRODUCT | topology-independent Public API/SPI와 최소 기술 계약 | Core | 유지·경량화 대상 |
| `cpf-common` | FIXED_PRODUCT | 고객 업무 공통 | Common | 유지 |
| `cpf-admin` | FIXED_PRODUCT | 플랫폼 운영·관리 | ADM | 유지 |
| `cpf-biz-admin` | FIXED_PRODUCT | 고객 업무 관리자 | BZA | 유지 |
| `cpf-batch` | FIXED_PRODUCT_CONTAINER | Batch·Worker·Scheduler·Center-Cut Runtime | BAT | 유지 |
| `cpf-gateway` | FIXED_PRODUCT | 선택형 Gateway Runtime | Gateway | 유지 |
| `cpf-reference` | FIXED_PRODUCT | 실행형 EDU·Reference Consumer | Reference | 유지 |
| `cpf-member` | GENERATED_DOMAIN | Generator Golden Reference Instance | Generator/Reference | 유지 |
| `cpf-starters` | FIXED_PRODUCT_CONTAINER | 선택 기술 Adapter·AutoConfiguration·독립 Library JAR | Starter Platform | **정식 유지** |
| `cpf-tools` | FIXED_PRODUCT_CONTAINER | Build·Generator·DB·검증·Supply-chain 도구 | Tools | 유지 |
| `cpf-docs` | DOCUMENTATION | Requirement·Architecture·Guide·Evidence·Work | Documentation/QA | 유지 |
| `gradle` | BUILD_CONFIG | Wrapper·Stack·Version 설정 | Build | 유지 |

`cpf-starters`는 Generated Domain이 아니다. 하위 Starter는 독립 Artifact이며 Product/Domain이 필요한 것만 선택한다.
상세 정책은 `cpf-docs/governance/CPF_STARTER_ARCHITECTURE_AND_LIFECYCLE_POLICY.md`를 따른다.

## 2. 로컬 전용 폴더

| 경로 | 의미 | 처리 |
|---|---|---|
| `.git` | Git Metadata | 절대 삭제 금지 |
| `.gradle` | 로컬 Gradle Cache | Git 대상 아님. 실행 중인 검수 종료 후 필요 시 로컬만 삭제 가능 |
| `.vscode` | IDE 설정·로컬 잔재 가능 | Git 추적 여부와 사용자 설정 확인 후 로컬만 정리 |

## 3. 신규 Root·Container 생성 승인 Gate

1. 최상위 Requirement와 Owner가 문서화돼 있다.
2. `settings.gradle`·Build·Publication·Test Consumer가 연결돼 있다.
3. Root Inventory에 `FIXED_PRODUCT`, `FIXED_PRODUCT_CONTAINER`, `GENERATED_DOMAIN`, `DOCUMENTATION`, `BUILD_CONFIG`, `LOCAL_ONLY` 중 하나로 등록돼 있다.
4. Public API·SPI·Internal 경계와 의존 방향이 정의돼 있다.
5. Fresh Clone에서 생성되지 않은 임의 Root가 Gate를 통과하지 못한다.
6. 삭제·이관 시 Consumer·SQL·Test·Guide·Evidence가 함께 변경된다.
7. 선택 기술 Container는 비선택 Consumer에 Dependency를 강제하지 않는다.

## 4. 금지

- 작업 편의를 위한 새 Root 임의 생성
- Generator를 거치지 않은 업무 Domain Root 생성
- Root를 추가하고 Inventory·Gate·Architecture를 갱신하지 않는 변경
- 빈 Folder, Build Output, Local Cache를 Product Root로 Commit
- `cpf-core`에 선택 Runtime을 임시 적치
- Starter가 자신을 사용하는 Product/Domain을 역으로 참조
