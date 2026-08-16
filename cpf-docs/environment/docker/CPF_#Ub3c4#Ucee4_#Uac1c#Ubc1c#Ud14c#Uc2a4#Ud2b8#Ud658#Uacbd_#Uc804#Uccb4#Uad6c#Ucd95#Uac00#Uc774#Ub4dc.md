# CPF Docker 개발·시험 환경 전체 구축 가이드

> **주 독자**: 신규 개발 PC·QA 환경을 준비하는 운영자
> **완료 결과**: 현재 Source의 Base·Toolchain·확장 서비스를 Created/Stopped 상태로 설치하고 무결성을 확인한다.
> **기준 Repository**: `freeangelsun/202412_01_CPF` / `master` / `54bcc10887a83b933685bff462c0b0d7df824923` (`20260802_10`)

<!-- CPF-TOC:START -->
## 전체 목차

- [1. 사전 조건](#1-사전-조건)
- [2. 설치 한 줄 명령](#2-설치-한-줄-명령)
- [3. Script 수행 단계](#3-script-수행-단계)
- [4. 정상 결과](#4-정상-결과)
- [5. 실패와 다음 행동](#5-실패와-다음-행동)
- [6. 설치 검증 한 줄](#6-설치-검증-한-줄)
- [7. Rollback](#7-rollback)
- [8. 설치 후 Directory 지도](#8-설치-후-directory-지도)
- [9. Image Lock 검수](#9-image-lock-검수)
- [10. 신규 Provider 증분 설치 설계](#10-신규-provider-증분-설치-설계)

<!-- CPF-TOC:END -->

## 1. 사전 조건

- Windows PowerShell/PowerShell 7
- Docker Desktop Linux Container, linux/amd64
- Repository `C:\dev\projects\jck412_01_CPF`
- Docker Root `C:\dev\Docker`
- 기존 CPF Container 정지
- Image Pull Network와 충분한 Disk/Memory

## 2. 설치 한 줄 명령

```powershell
$repo='C:\dev\projects\jck\202412_01_CPF'; $dockerRoot='C:\dev\Docker'; if(-not(Test-Path -LiteralPath $repo -PathType Container)){throw "Repository가 없습니다: $repo"}; pwsh -NoProfile -File (Join-Path $repo 'cpf-tools\environment\docker-development-test\CPF_도커_개발테스트환경_전체설치.ps1') -DockerRoot $dockerRoot -RepoRoot $repo; if($LASTEXITCODE -ne 0){throw 'CPF Docker 전체 설치 실패'}
```

관리자 비밀번호를 인자로 남기지 않으면 Secure Prompt로 입력하며 Repository 밖 Environment/Secret 파일에 저장한다.

## 3. Script 수행 단계

1. Docker Version·Linux Backend·amd64를 검증한다.
2. `C:\dev\Docker\CPF`, `Secrets`를 만든다.
3. Runtime Env·Redis Secret을 원문 출력 없이 준비한다.
4. Compose·Tooling·Fixture 파일을 Source에서 복사한다.
5. DB·Redis·Kafka·Java/Node/Playwright Image를 준비한다.
6. Toxiproxy·OTel·Trivy·ORT Image 후보를 순서대로 준비한다.
7. Full Toolchain Image를 Build하고 Java/Node/Pwsh/Python/Git/DB Client/Docker/SSH를 확인한다.
8. Compose Config를 검증하고 Base/Tool Container를 `create`한다.
9. Image ID·Digest를 `image-lock-complete.json`에 기록한다.
10. 확장 설치 Script로 WireMock·SFTP·Vault·Keycloak을 추가한다.
11. `verify-complete-environment.ps1 -RequireStopped`를 실행한다.

## 4. 정상 결과

| 항목 | 정상 판정 |
|---|---|
| Directory | CPF·Secrets·Output·Cache 소유 경로 존재 |
| Secret | Repository 밖, 빈 값 없음, Console 원문 없음 |
| Image | 필수 Image inspect 가능, Lock에 ID/Digest 기록 |
| Compose | config --quiet 통과 |
| Container | 11개 Base/확장 Container Created/Stopped, Running 0 |
| Toolchain | Java25·Node22·Pwsh7.6.4·Playwright1.62·Python/Git/DB Client/Docker/SSH |
| Data | 업무 Schema·Seed·Kafka Topic 생성 안 됨 |

## 5. 실패와 다음 행동

| 실패 | 확인 | 다음 행동 |
|---|---|---|
| Docker 미실행/Windows Backend | docker version/info | Linux Backend로 전환 후 재실행 |
| Architecture 불일치 | docker info Architecture | amd64 환경 사용 |
| Image Pull | Registry/DNS/Proxy/Disk | 원인 수정, 같은 Script 재실행 |
| Container 실행 중 | docker ps --format | 해당 CPF Service를 정상 중지 |
| Secret 누락 | Secrets 정확한 파일 | 빈 값/권한 수정, 원문 공유 금지 |
| Compose 오류 | docker compose config | Env/Image/File 경로 수정 |
| Toolchain Build | Dockerfile/Build Log | 첫 실패 Package/Image 수정 |
| Extension 실패 | Base 파일·tool-images.env | Base 검증 후 증분 재실행 |
| Verify 실패 | 누락 Image/Container/State | 실패 항목만 보완 |

## 6. 설치 검증 한 줄

```powershell
$dockerRoot='C:\dev\Docker'; pwsh -NoProfile -File (Join-Path $dockerRoot 'CPF\verify-complete-environment.ps1') -RequireStopped; if($LASTEXITCODE -ne 0){throw 'Docker 환경 검증 실패'}
```

## 7. Rollback

설치 실패 시 다른 Docker 프로젝트나 공용 Image/Volume을 삭제하지 않는다. 이번 CPF Compose Project의 Created Container와 이번 작업이 만든 정확한 `C:\dev\Docker\CPF` 파일 중 Backup이 있는 파일만 복원한다. Secret은 사용 여부와 Backup을 확인한 뒤 폐기한다.

## 8. 설치 후 Directory 지도

| 경로 | 내용 | 보호/정리 |
|---|---|---|
| C:\dev\Docker\CPF | Compose·Tool Script·Image Lock | Source Script로 갱신, 임의 파일 금지 |
| C:\dev\Docker\Secrets | Runtime Env·Password/Token 파일 | Repository 밖·ACL·원문 출력 금지 |
| C:\dev\Docker\CPF\fixtures | WireMock/Keycloak Fixture | Manifest 기반 |
| C:\dev\Docker\CPF\output | OTel/Trivy/ORT/Integration 결과 | 시나리오·보존 후 정확히 정리 |
| C:\dev\Docker\CPF\cache | Tool Cache | 소유 Tool만 정리 |

## 9. Image Lock 검수

- Image Reference·Required 여부·Image ID·Repo Tags·Repo Digests를 확인한다.
- `latest` Fallback을 사용했으면 실제 Digest와 승인 사유를 기록한다.
- Toolchain Image의 Java/Node/Pwsh/Playwright/DB Client Version을 Manifest에 기록한다.
- Image 교체 후 Compose Config·Created Container·Smoke/Fault Test를 다시 수행한다.

## 10. 신규 Provider 증분 설치 설계

RabbitMQ·Artemis·TCP Simulator·Notification Fixture를 추가할 때 다음을 같은 변경에 제공한다.

1. 고정 Image 후보와 Digest Lock
2. Compose Service·Network·Loopback Port·Healthcheck·restart:no
3. Repository 밖 Secret과 최소 권한
4. 초기 Exchange/Queue/Destination/Certificate/Fixture Script
5. 정상·오류·Response Loss·Process Kill Fixture
6. Toxiproxy 연결
7. 실제 Starter Consumer Smoke
8. verify-complete-environment 확장
9. 정확한 Stop/Reset/Cleanup
10. Docker 문서와 Platform 운영 매뉴얼 현행화
