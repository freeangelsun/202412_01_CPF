# CPF Local Runtime Module 위치 기준

## 정본 위치

Local 개발 Runtime은 제품 배포 산출물이 아니라 개발·통합 검증용 Gradle Source Module이다.
따라서 `deploy` 아래에 Source를 두지 않고 다음 위치를 정본으로 사용한다.

- `cpf-tools/runtime/cpf-local-runtime`
- `cpf-tools/runtime/cpf-local-batch-runtime`

Gradle 논리 프로젝트 이름은 기존 호환을 위해 유지한다.

- `:cpf-local-runtime`
- `:cpf-local-batch-runtime`

`settings.gradle`의 `projectDir`만 위 물리 경로를 가리킨다.

## deploy/local의 역할

`deploy/local`은 다음 배포·실행 자료만 소유한다.

- Local 실행 Manifest
- 환경별 Property 예시
- Process/Port 배치 정보
- 설치·기동·중지·상태 확인 Script

Java Source, Test, Build Script는 `deploy`에 두지 않는다.

## 기존 Checkout 이관

최종 Overlay를 프로젝트 Root에 푼 직후 다음 명령을 한 번 실행한다.

```powershell
pwsh -NoProfile -File .\cpf-tools\scripts\relocate-local-runtime-modules.ps1 -ProjectRoot .
```

Script는 새 경로에 Overlay 변경 파일이 이미 있으면 이를 보존하고, 기존 Root Module에서 빠진 파일만 병합한 뒤 Root의 두 폴더를 제거한다.

## 검증

```powershell
pwsh -NoProfile -File .\cpf-tools\scripts\check-local-runtime-topology.ps1 -Root .
.\gradlew.bat :cpf-local-runtime:test :cpf-local-batch-runtime:test --no-daemon --no-build-cache
```

## 금지사항

- `deploy` 아래에 Java Source Module 배치
- Root와 `cpf-tools/runtime`에 같은 Module 중복 보존
- Gradle 논리 이름 변경으로 Consumer Task·의존성 회귀 유발
- Local Runtime을 운영 Profile의 대체 Runtime으로 사용
