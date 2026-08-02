# CPF 도커 개발·테스트 환경 전체 구축 가이드

상위 메뉴: [CPF Docker 가이드](README.md)

## 1. 사전 조건

- Windows 10/11, WSL 2
- Docker Desktop Linux Container Backend
- PowerShell 7
- CPF Repository
- 인터넷 연결과 필요한 Registry 접근 권한
- 권장 8 CPU, Memory 24GB 이상, 충분한 Disk 공간

Oracle·Keycloak·Browser·ORT를 동시에 실행할 때 Memory 사용량이 크므로 실제 작업에 필요한 Service만 시작한다.

## 2. 새 PC 전체 설치

Repository Root에서 실행한다. Secret이 없는 최초 설치에서는 Script가 로컬 관리자 공통 비밀번호를 보안 입력으로 요청한다. 입력값은 화면에 표시되지 않으며 Repository에 기록하지 않는다.

```powershell
pwsh -NoProfile -ExecutionPolicy Bypass -File ".\cpf-tools\environment\docker-development-test\CPF_도커_개발테스트환경_전체설치.ps1" -RepoRoot (Get-Location).Path
```

비대화형 실행을 위해 `-AdminPassword` Parameter도 지원하지만 명령 이력과 최초 Process 인자에 남을 수 있으므로 일반 사용에서는 보안 입력 Prompt를 사용한다. 전체 설치 Script는 값을 Repository 밖 환경파일에 저장한 뒤 확장 설치 자식 Process 인자로 다시 전달하지 않는다. 실제 값은 문서나 Git 파일에 넣지 않는다.

## 3. 기존 PC 증분 설치

이미 Oracle·PostgreSQL·MariaDB·Redis·Kafka·Toxiproxy·OTel Collector가 준비된 PC에서는 전체 설치를 반복하지 않는다.

```powershell
pwsh -NoProfile -ExecutionPolicy Bypass -File ".\cpf-tools\environment\docker-development-test\CPF_도커_확장연동환경_증분설치.ps1" -RepoRoot (Get-Location).Path
```

증분 설치가 추가하는 항목:

- WireMock
- SFTP Fixture
- Vault Dev Fixture
- Keycloak Local Fixture
- 외부연계용 Toxiproxy 4개 Proxy
- OpenSSH Client·`sshpass`가 포함된 새 통합 Runner
- Secret 파일 5개와 비민감 계정 식별자

기존 DB Container·Volume·Image·Secret은 삭제하지 않는다.

## 4. 설치 Script 동작

1. Docker Linux/amd64 확인
2. `C:\dev\Docker\CPF`, `C:\dev\Docker\Secrets` 확인
3. 누락 Secret을 암호학적 난수로 생성하고 값은 출력하지 않음
4. Runtime Compose·Fixture·Script 복사
5. 필요한 Image Pull
6. SFTP Fixture Image와 통합 Runner Build
7. Compose 정적 해석
8. Container를 Created/Stopped로 준비
9. Image Lock과 상태 결과 생성
10. Restart Policy `no`, Running 0 확인

## 5. 설치 후 기대 상태

```text
필수 Image               18개
보존 가능한 기존 Runner    최대 4개
Container                 11개 Created/Stopped
실행 중 Container         0개
Data Volume               7개
Secret File               7개
CPF 업무 Schema·Seed      생성하지 않음
Kafka Topic               생성하지 않음
```

Container:

```text
cpf-mariadb
cpf-postgresql
cpf-oracle
cpf-redis
cpf-kafka
cpf-wiremock
cpf-sftp
cpf-vault
cpf-keycloak
cpf-toxiproxy
cpf-otel-collector
```

## 6. 설치 상태 확인

```powershell
pwsh -NoProfile -ExecutionPolicy Bypass -File "C:\dev\Docker\CPF\verify-complete-environment.ps1" -RequireStopped
```

## 7. 기본 Fixture 초기화와 실제 연결 확인

WireMock·SFTP·Vault·Keycloak을 시작하고 외부 응답 시나리오, Realm/User/Client, OIDC Token, Vault KV와 SFTP 송수신을 확인한다.

```powershell
pwsh -NoProfile -ExecutionPolicy Bypass -File "C:\dev\Docker\CPF\initialize-integration-fixtures.ps1" -StopAfter
```

결과는 다음 로컬 경로에 Sanitized JSON으로 기록된다.

```text
C:\dev\Docker\CPF\output\integration\integration-fixture-result.json
```

## 8. 선택 Version

- WireMock `3.13.2`
- Vault `1.21.4` 계열
- Keycloak `26.6.1`
- Alpine `3.23.5`
- SFTP Fixture `cpf-sftp-fixture:alpine3.23`
- 새 통합 Runner `cpf-full-development-test-runner:java25-node22-pwsh7.6.4-playwright1.62.0-integration1`

Registry 문제로 승인된 fallback Tag를 사용한 경우 실제 RepoDigest와 Image ID는 `image-lock-complete.json`을 정본으로 한다.

## 9. 금지 사항

- `docker system prune`, 전체 Volume 삭제, Factory Reset
- 비밀번호·Token을 문서·Git·Evidence에 기록
- CPF 업무 Schema·User·Seed·Topic을 Docker 설치 Script에서 임의 생성
- RabbitMQ 등 비정본 Broker를 Kafka 대체품으로 자동 설치
- 실행하지 않은 Runtime 시나리오를 성공으로 기록
