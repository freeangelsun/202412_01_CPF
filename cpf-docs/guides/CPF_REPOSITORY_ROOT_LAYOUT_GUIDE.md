# CPF Repository Root 배치 기준

## 1. 목적

CPF Repository Root에는 제품 식별, Gradle Wrapper, 최상위 Build와 공식 제품 영역만 유지한다. 개발·통합 검증 전용 조립 Runtime과 Build/Deploy 도구는 `cpf-tools`가 소유한다. 작업 중 생성된 중복 Build Module, 임시 산출물, 별도 배포 자산 폴더를 Root에 추가하지 않는다.

## 2. Root에 유지하는 Build 항목

다음 항목은 Gradle Wrapper 표준 경로이므로 Root에 유지한다.

- `gradlew`
- `gradlew.bat`
- `gradle/wrapper`
- `gradle/cpf-stack.properties`
- `gradle/cpf-platform.properties`
- `settings.gradle`
- `build.gradle`

`gradle`을 `deploy`나 `cpf-tools` 하위로 옮기면 Wrapper, IDE, CI와 일반 Gradle 명령 호환성이 깨지므로 이동하지 않는다.

## 3. Starter 제품 Root

`cpf-starters/`는 임시 조립 폴더가 아니라 선택형 Spring Boot AutoConfiguration 제품 7개의 공식 Root다. `cpf-tools`로 이동하거나 삭제하지 않는다.

| 물리 경로 | Gradle 논리 Project | Maven 좌표 |
|---|---|---|
| `cpf-starters/security` | `:cpf-starter-security` | `com.cpf.starter:cpf-starter-security` |
| `cpf-starters/messaging-kafka` | `:cpf-starter-messaging-kafka` | `com.cpf.starter:cpf-starter-messaging-kafka` |
| `cpf-starters/cache` | `:cpf-starter-cache` | `com.cpf.starter:cpf-starter-cache` |
| `cpf-starters/observability` | `:cpf-starter-observability` | `com.cpf.starter:cpf-starter-observability` |
| `cpf-starters/resilience` | `:cpf-starter-resilience` | `com.cpf.starter:cpf-starter-resilience` |
| `cpf-starters/featureflag` | `:cpf-starter-featureflag` | `com.cpf.starter:cpf-starter-featureflag` |
| `cpf-starters/secret` | `:cpf-starter-secret` | `com.cpf.starter:cpf-starter-secret` |

이 Root와 7개 Module은 다음 정본에 함께 편입된다.

- `cpf-tools/governance/cpf-product-surface-policy.json`: `cpf-starters/`의 Owner를 `cpf-starters`로 선언한다.
- `settings.gradle`: 7개 논리 Project를 include하고 위 물리 경로에 매핑한다.
- `cpf-tools/release/cpf-final-artifact-catalog.json`: 7개 Starter Artifact와 산출 경로를 제품 Catalog에 등록한다.
- `cpf-tools/build/platform-bom/build.gradle`: 동일 Platform Version의 7개 좌표를 BOM Constraint로 제공한다.
- Root `build.gradle`: Artifact Catalog와 실제 `cpf-starters/` Project의 1:1 일치를 fail-closed로 확인하고, Catalog에 등록된 7개 Project를 Local/Staging/Internal Publication Set에 편입한다.

Starter는 소비자가 필요한 기능만 의존성으로 선택한다. 특정 Runtime이나 Generated Domain에 7개를 일괄 강제하지 않으며, 개별 `cpf-starter-*` 디렉터리를 Repository Root에 다시 만들지 않는다.

## 4. CPF Build 도구 정본

다음 두 Build 도구는 Root에 두지 않는다.

- `cpf-gradle-plugin` → `cpf-tools/build/gradle-plugin`
- `cpf-platform-bom` → `cpf-tools/build/platform-bom`

현재 정본 계약은 다음과 같다.

- Convention Plugin Included Build 이름: `cpf-gradle-plugin`
- Plugin ID: `com.cpf.platform-conventions`
- Plugin Implementation 좌표: `com.cpf.gradle:cpf-gradle-plugin`
- Platform BOM Included Build 이름: `cpf-platform-bom`
- Platform BOM 좌표: `com.cpf:cpf-platform-bom`

`settings.gradle`은 Convention Plugin을 `pluginManagement.includeBuild('cpf-tools/build/gradle-plugin')`로, BOM을 `includeBuild('cpf-tools/build/platform-bom')`로 연결한다. Root에 중복 폴더가 생기면 Consumer와 Publication 연결을 확인한 뒤 제거한다.


## 5. Local Runtime Source 정본

Local Web·Batch 통합 실행기는 공식 업무 Module이 아니라 개발·통합 검증용 Launcher다. Root와 `deploy`에 Source를 두지 않고 다음 경로를 사용한다.

- `cpf-tools/runtime/cpf-local-runtime`
- `cpf-tools/runtime/cpf-local-batch-runtime`

Gradle 논리 이름 `:cpf-local-runtime`, `:cpf-local-batch-runtime`은 유지하고 `settings.gradle`의 `projectDir`만 정본 경로를 가리킨다.

기존 Root 폴더는 다음 Script로 이관한다.

```powershell
pwsh -NoProfile -File .\cpf-tools\scripts\relocate-local-runtime-modules.ps1 -ProjectRoot .
```

## 6. 배포 자산 정본

`deploy`는 설치·배포 Manifest, 환경 Template, Batch Agent 패키지, Container/Cell 정의 등 제품 배포 자산의 공식 Root 영역으로 유지한다. Java Source Module은 `deploy` 아래에 두지 않는다.

Local Web·Batch Runtime Source는 배포 자산이 아니므로 `deploy`가 아니라 `cpf-tools/runtime`이 소유한다.

## 7. 금지사항

- Root `cpf-gradle-plugin` 또는 `cpf-gradle-plugins`
- Root `cpf-platform-bom`
- Root의 개별 `cpf-starter-*` 폴더
- Root `cpf-local-runtime`
- Root `cpf-local-batch-runtime`
- `deploy` 아래 Java Source Module
- Root의 log, tmp, zip, bak, patch, build, evidence 중간 파일
- 동일 역할 Build/Deploy 정본의 복제
