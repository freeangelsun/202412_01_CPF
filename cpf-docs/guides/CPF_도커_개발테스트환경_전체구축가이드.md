# CPF 도커 개발·테스트 환경 전체 구축 가이드

## 1. 목적

새 Windows PC에서 CPF 개발·테스트에 필요한 Docker 기반 Service와 Toolchain을 한 번에 준비한다.

## 2. 사전 조건

- Windows 10/11
- WSL 2
- Docker Desktop Linux Container Backend
- PowerShell 7
- CPF Repository
- 인터넷 연결
- Oracle Container Registry와 Oracle Instant Client 다운로드 가능
- 최소 4 CPU, 권장 Memory 16GB 이상
- 충분한 Disk 공간

ORT는 공식 안내상 Java 21 이상, 4 Core와 8GB JVM Memory를 권장한다. Oracle, Browser, ORT를 동시에 사용할 때는 Docker Desktop Memory를 여유 있게 설정한다.

## 3. 한 번에 적용하고 설치

Root Overlay ZIP을 Downloads에 저장한 후 Repository Root에 풀고 설치 Script를 실행한다.

```powershell
$zip="$env:USERPROFILE\Downloads\CPF_20260801_도커개발테스트환경_전체구성_ROOT_OVERLAY.zip"; $root="C:\dev\projects\jck\202412_01_CPF"; Expand-Archive -LiteralPath $zip -DestinationPath $root -Force; pwsh -NoProfile -ExecutionPolicy Bypass -File "$root\cpf-tools\environment\docker-development-test\CPF_도커_개발테스트환경_전체설치.ps1" -RepoRoot $root
```

현재 PC에 Secret 파일이 이미 있으면 비밀번호를 다시 입력하지 않는다.

다른 PC에서 Secret이 없으면 한 줄 마지막에 `-AdminPassword '<로컬비밀번호>'`를 추가한다. 실제 비밀번호를 문서나 Git 파일에 기록하지 않는다.

## 4. 설치 Script가 수행하는 작업

다른 PC에 기존 `C:\dev\Docker\CPF`가 없어도 다음 Base Runtime 파일을 함께 설치한다.

```text
compose.yml
compose.redis.yml
compose.kafka.yml
cpf-env.ps1
reset-test-data.ps1
verify-clean-prepared.ps1
```


- Base Runtime 파일과 Base Image 8개 확인 및 누락 항목 준비
- Toxiproxy Image 준비
- OpenTelemetry Collector Image 준비
- Trivy Image 준비
- OSS Review Toolkit Image 준비
- Java·Node·PowerShell·Playwright·Python·Git·DB Client 통합 Runner Build
- `C:\dev\Docker\CPF`에 Tooling Compose와 실행 Script 배치
- Tool별 Version 실행
- Tooling Container 2개 Created/Stopped 생성
- Image Lock 생성
- 전체 상태 확인

## 5. 설치 후 상태

```text
필수 Image               13개
기존 PC Legacy Runner      최대 3개 추가 보존
Container                 7개 Created/Stopped
실행 중 Container         0개
Data Volume               5개
CPF 업무 Schema·Data      없음
```

기존 `cpf-validation-runner`는 즉시 삭제하지 않는다. 새 통합 Runner가 정상적으로 사용되는 동안 이전 Runner는 안전한 전환 기준으로 유지한다.

## 6. 주요 파일

Repository 정본:

```text
cpf-tools/environment/docker-development-test/
```

실행 환경:

```text
C:\dev\Docker\CPF
```

Secret:

```text
C:\dev\Docker\Secrets
```

## 7. 설치 후 확인

```powershell
pwsh -NoProfile -ExecutionPolicy Bypass -File "C:\dev\Docker\CPF\verify-complete-environment.ps1" -RequireStopped
```

## 8. 공식 Source 기준 Version

- OpenTelemetry Collector Contrib `0.157.0`
- Trivy `0.70.0`
- OSS Review Toolkit `87.3.0`
- Oracle Instant Client Basic Light·SQL*Plus `23.26.3.0.0`
- Toxiproxy는 `2.12.0`을 우선 사용하고 Registry 상황에 따라 공식 `latest`로 대체한 뒤 실제 Digest를 Lock에 기록
