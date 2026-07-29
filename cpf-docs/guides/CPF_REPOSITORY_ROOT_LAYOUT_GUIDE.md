# CPF Repository Root 배치 기준

## 1. 목적

CPF Repository Root에는 제품 식별, Gradle Wrapper, 최상위 Build와 독립 배포 제품 Module만 유지한다. 개발·통합 검증 전용 조립 Runtime과 Build/Deploy 도구는 `cpf-tools`가 소유한다. 작업 중 생성된 중복 Build Module, 임시 산출물, 별도 배포 자산 폴더를 Root에 추가하지 않는다.

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

## 3. CPF Build 도구 정본

다음 두 Build 도구는 Root에 두지 않는다.

- `cpf-gradle-plugins` → `cpf-tools/build/gradle-plugin`
- `cpf-platform-bom` → `cpf-tools/build/platform-bom`

Root에 중복 폴더가 생기면 정본과 내용이 일치하는지 확인한 뒤 제거한다.


## 4. Local Runtime Source 정본

Local Web·Batch 통합 실행기는 공식 업무 Module이 아니라 개발·통합 검증용 Launcher다. Root와 `deploy`에 Source를 두지 않고 다음 경로를 사용한다.

- `cpf-tools/runtime/cpf-local-runtime`
- `cpf-tools/runtime/cpf-local-batch-runtime`

Gradle 논리 이름 `:cpf-local-runtime`, `:cpf-local-batch-runtime`은 유지하고 `settings.gradle`의 `projectDir`만 정본 경로를 가리킨다.

기존 Root 폴더는 다음 Script로 이관한다.

```powershell
pwsh -ExecutionPolicy Bypass -File .\cpf-tools\scripts\relocate-local-runtime-modules.ps1 -ProjectRoot .
```

## 5. 배포 자산 정본

`deploy`는 설치·배포 Manifest, 환경 Template, Batch Agent 패키지, Container/Cell 정의 등 제품 배포 자산의 공식 Root 영역으로 유지한다. Java Source Module은 `deploy` 아래에 두지 않는다.

Local Web·Batch Runtime Source는 배포 자산이 아니므로 `deploy`가 아니라 `cpf-tools/runtime`이 소유한다.

## 6. 금지사항

- Root `cpf-gradle-plugins`
- Root `cpf-platform-bom`
- Root `cpf-local-runtime`
- Root `cpf-local-batch-runtime`
- `deploy` 아래 Java Source Module
- Root의 log, tmp, zip, bak, patch, build, evidence 중간 파일
- 동일 역할 Build/Deploy 정본의 복제
