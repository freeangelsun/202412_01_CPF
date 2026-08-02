# CPF Docker 개발·시험 환경 전체 구축 가이드

상위 메뉴: [Docker 문서](README.md)

## 1. 대상

- Docker Desktop과 Linux Container Backend가 설치된 Windows 개발 PC
- Repository Root가 `C:\dev\projects\jck\202412_01_CPF`인 기본 예
- Runtime Root가 `C:\dev\Docker\CPF`인 기본 예

경로가 다르면 Script Parameter로 전달한다.

## 2. 사전 확인

```powershell
docker version; docker compose version; git rev-parse HEAD; git status --short
```

- 다른 작업자의 변경을 삭제하지 않는다.
- 기존 CPF Container가 실행 중이면 대상 작업을 종료한 뒤 설치한다.
- Docker Factory Reset과 `docker system prune`을 실행하지 않는다.

## 3. Base 전체 설치

Repository Root에서:

```powershell
pwsh -NoProfile -ExecutionPolicy Bypass -File .\cpf-tools\environment\docker-development-test\CPF_도커_개발테스트환경_전체설치.ps1 -DockerRoot 'C:\dev\Docker' -RepoRoot (Get-Location).Path
```

Script는 Secret을 Repository 밖에 생성하고, 필요한 Source를 Runtime Root로 복사하며, Image를 준비하고 Container를 Created/Stopped 상태로 만든다.

## 4. 기존 환경 확장

WireMock·SFTP·Vault·Keycloak 확장:

```powershell
pwsh -NoProfile -ExecutionPolicy Bypass -File .\cpf-tools\environment\docker-development-test\CPF_도커_확장연동환경_증분설치.ps1 -DockerRoot 'C:\dev\Docker' -RepoRoot (Get-Location).Path
```

QA38 RabbitMQ·JMS Fixture는 관련 Source 파일이 존재할 때만 실행한다.

```powershell
if(-not(Test-Path -LiteralPath .\cpf-tools\environment\docker-development-test\CPF_도커_QA38메시징_증분설치.ps1 -PathType Leaf)){throw 'QA38 Messaging Docker Source 적용이 먼저 필요합니다.'}
```

```powershell
pwsh -NoProfile -ExecutionPolicy Bypass -File .\cpf-tools\environment\docker-development-test\CPF_도커_QA38메시징_증분설치.ps1 -DockerRoot 'C:\dev\Docker' -RepoRoot (Get-Location).Path
```

IBM MQ도 준비할 경우 License 조건을 읽고 명시적으로 실행한다.

```powershell
pwsh -NoProfile -ExecutionPolicy Bypass -File .\cpf-tools\environment\docker-development-test\CPF_도커_QA38메시징_증분설치.ps1 -DockerRoot 'C:\dev\Docker' -RepoRoot (Get-Location).Path -IncludeIbmMq -AcceptIbmMqDeveloperLicense
```

IBM MQ Advanced for Developers는 개발 PC 용도와 재배포 제한을 확인한다.

## 5. 설치 후 상태 확인

```powershell
pwsh -NoProfile -ExecutionPolicy Bypass -File C:\dev\Docker\CPF\verify-complete-environment.ps1 -RequireStopped
pwsh -NoProfile -ExecutionPolicy Bypass -File C:\dev\Docker\CPF\verify-messaging-environment.ps1 -RequireStopped
```

정상 기대:

```text
필수 Image 존재
Container Created 또는 Stopped
Restart Policy no
Running CPF Container 0
Secret 원문 미출력
```

## 6. Fixture 초기화

확장 연동:

```powershell
pwsh -NoProfile -ExecutionPolicy Bypass -File C:\dev\Docker\CPF\initialize-integration-fixtures.ps1 -StopAfter
```

Messaging:

```powershell
pwsh -NoProfile -ExecutionPolicy Bypass -File C:\dev\Docker\CPF\initialize-messaging-fixtures.ps1 -StopAfter
```

IBM MQ 포함:

```powershell
pwsh -NoProfile -ExecutionPolicy Bypass -File C:\dev\Docker\CPF\initialize-messaging-fixtures.ps1 -IncludeIbmMq -StopAfter
```

## 7. 재실행

설치 Script 재실행은 기존 Container·Volume·Secret을 보존해야 한다. Secret 파일이 이미 있으면 값을 다시 생성하지 않는다. Source 파일은 해당 Docker 관리 경로의 정본으로 갱신한다.

## 8. 검증 기록

```text
Git SHA
Docker/Compose Version
Image Digest
Compose Config Hash
Container Restart Policy
Fixture Command
Exit Code
Sanitized Log Hash
Running Container Count
```

실행하지 않은 Provider는 `미검증`으로 남긴다.


## 9. 신규 모듈 증분 편입 표준

신규 Module·Starter·Provider가 개발되면 다음 순서로 Docker 환경에 편입한다.

1. Source·Build·실제 Consumer를 확인한다.
2. 필요한 Image·Version·License를 결정한다.
3. Compose Service·Network·Port·Volume·Secret을 정의한다.
4. 초기화 Fixture와 Test Data를 만든다.
5. Product Config와 Named Binding을 연결한다.
6. 정상 연결·인증·권한·Readiness를 확인한다.
7. Timeout·Network Loss·Process Kill·부분 실패를 주입한다.
8. Retry·Reprocess·Reconcile·Rollback을 실행한다.
9. 검증 Script의 Expected·Actual·Exit Code를 기록한다.
10. 이번 작업에서 시작한 Container만 중지하고 데이터 보존 상태를 확인한다.

실제 Source가 구현되기 전에는 임시 Image 기동 명령을 제품 지원 절차로 기록하지 않는다.
