# CPF Open Git Developer Workspace

CPF(Core Platform Framework)를 사용하는 업무 개발자를 위한 공개 개발 Workspace입니다.

Framework 내부 구현 Source는 포함하지 않습니다. 업무 개발자가 직접 수정하거나 참고해야 하는 Generated Domain, Backoffice, EDU Source와 개발 명령만 공개하며, CPF Framework 자체는 검증된 Binary Repository를 통해 사용합니다.

## 공개 Source

- `cpf-<domain>`: `cpf.domain.contractVersion=1` Developer Contract로 자동 발견되는 Generated/Customer Domain Source. 0개도 정상입니다.
- `cpf-backoffice` / `cpf-backoffice-web`: 선택했을 때만 포함되는 Optional MBW Backoffice Source.
- `cpf-education`: CPF 기능별 실행·학습 Source
- `bin`: Java 기반 Unified `cpf` CLI와 Windows/Linux Thin Wrapper
- `binary-repository`: CPF Framework Public Binary(JAR/POM). 현재 Public 버전만 포함합니다.
- `cpf-docs`: 개발자·운영자 가이드와 공개 기술 문서

`cpf-core`, `cpf-common`, `cpf-admin`, `cpf-gateway`, `cpf-batch`, `cpf-starters` 내부 구현 Source는 이 Repository에 포함되지 않습니다.

## 시작

필수 환경은 Git, Java 25, 승인된 Container Runtime이며 Backoffice Frontend 사용 시 Node.js가 필요합니다.

CPF Framework Binary는 이 Repository의 `binary-repository/`에 함께 들어 있으므로 별도 Repository 주소를 설정하지 않아도 됩니다.

```powershell
.\cpf.cmd bootstrap
```

Linux:

```bash
./bin/cpf bootstrap
```

사내 Repository를 쓰려면 `CPF_MAVEN_REPOSITORY_URL`로 덮어쓸 수 있습니다(선택).

## 자주 쓰는 명령

```powershell
.\cpf.cmd domain-new account --system-code ACC
.\cpf.cmd domain-sync
.\cpf.cmd build
.\cpf.cmd test
.\cpf.cmd stop
```

Windows: `.\cpf.cmd reset --confirm-local-reset` / Linux: `./bin/cpf reset --confirm-local-reset`

## Runtime 실행

Target만 바꾸면 모든 Runtime을 같은 방법으로 실행합니다. 사용 가능한 Target은 help로 확인합니다.

```powershell
.\bin\cpf-help.ps1
.\bin\cpf-start.ps1  -Target gateway
.\bin\cpf-status.ps1 -Target gateway
.\bin\cpf-stop.ps1   -Target gateway
```

```bash
./bin/cpf-help.sh
./bin/cpf-start.sh  --target gateway
./bin/cpf-status.sh --target gateway
./bin/cpf-stop.sh   --target gateway
```

전체를 함께 띄우려면 `all`, 일상 개발 구성만 띄우려면 `dev` Target을 사용합니다.

## 운영 콘솔(ADM)과 업무 백오피스(MBW + Backoffice Web) 실행

세 Runtime 모두 공개 launcher 하나로 기동합니다. npm 명령이나 내부 Gradle 경로를 알 필요가 없습니다.
화면(production bundle)은 각 Runtime 실행물에 포함되어 있습니다.

| Runtime | Target | 기본 Port | Port 환경변수 | 접속 URL |
| --- | --- | --- | --- | --- |
| ADM 운영 콘솔 | `admin` | 8090 | `ADM_SERVER_PORT` | http://127.0.0.1:8090/adm/ |
| MBW 업무 Domain | `backoffice` | 8091 | `MBW_ONLINE_PORT` | http://127.0.0.1:8091/actuator/health |
| Backoffice Web(MBW Channel Front) | `backoffice-web` | 8092 | `MBW_WEB_PORT` | http://127.0.0.1:8092/mbw/ |

Port를 바꾸려면 해당 환경변수를 설정한 뒤 기동합니다(예: `ADM_SERVER_PORT=18090`).

### 1) 사전 준비

```powershell
.\cpf.cmd bootstrap
```

```bash
./bin/cpf bootstrap
```

`bootstrap`이 Java/Container Runtime 확인, DB 기동, 스키마 적용까지 수행합니다.

### 2) ADM 최초 운영자 계정

ADM은 모든 Profile에서 같은 **최초 1회 Initial Operator Bootstrap** 계약으로 계정을 만듭니다.
계정이 없는 Fresh 설치에서만 아래 세 값을 환경으로 전달하고 기동합니다. 비밀번호는 파일·YAML·명령
히스토리에 남기지 않습니다. 이미 운영자가 있으면 Bootstrap 변수를 설정하지 않고 기동하며, 기존
계정의 비밀번호·권한은 절대로 덮어쓰지 않습니다.

```powershell
$env:CPF_ADM_BOOTSTRAP_OPERATOR_ID = Read-Host '최초 운영자 ID'
$env:CPF_ADM_BOOTSTRAP_OPERATOR_NAME = Read-Host '최초 운영자 이름'
$env:CPF_ADM_BOOTSTRAP_PASSWORD = Read-Host '초기 비밀번호' -MaskInput
.\bin\cpf-start.ps1 -Target admin
```

```bash
read -rp '최초 운영자 ID: ' CPF_ADM_BOOTSTRAP_OPERATOR_ID; export CPF_ADM_BOOTSTRAP_OPERATOR_ID
read -rp '최초 운영자 이름: ' CPF_ADM_BOOTSTRAP_OPERATOR_NAME; export CPF_ADM_BOOTSTRAP_OPERATOR_NAME
read -rsp '초기 비밀번호: ' CPF_ADM_BOOTSTRAP_PASSWORD; export CPF_ADM_BOOTSTRAP_PASSWORD
./bin/cpf-start.sh --target admin
```

브라우저에서 http://127.0.0.1:8090/adm/ 로 접속해 방금 만든 계정으로 실제 로그인합니다.

### 3) 업무 백오피스(MBW + Backoffice Web)

Backoffice Web은 ADM이 아니라 MBW의 Channel Front입니다. MBW Fresh 설치도 동일한 1회 Initial
Operator Bootstrap을 먼저 수행하고, 이후에는 maker/checker 승인 절차로 운영자를 추가합니다. 아래
Local 개발자 경로는 HTTPS endpoint/secure-cookie 값만 Local transport에 맞추며 인증·CSRF·Bootstrap
의미는 다른 Profile과 동일합니다.

```powershell
$env:CPF_MBW_INITIAL_OPERATOR_LOGIN_ID = Read-Host 'MBW 최초 운영자 ID'
$env:CPF_MBW_INITIAL_OPERATOR_NAME = Read-Host 'MBW 최초 운영자 이름'
$env:CPF_MBW_INITIAL_OPERATOR_ROLE_CODE = Read-Host 'MBW 최초 운영자 역할 코드'
$env:CPF_MBW_BOOTSTRAP_PASSWORD = Read-Host 'MBW 초기 비밀번호' -MaskInput
$env:CPF_MBW_JWT_SECRET = Read-Host '32자 이상 MBW JWT Secret' -MaskInput
$env:MBW_WEB_MODE = 'DIRECT'
$env:MBW_DIRECT_BASE_URI = 'http://127.0.0.1:8082'
$env:MBW_WEB_SECURE_COOKIES = 'false'
.\bin\cpf-start.ps1 -Target backoffice
.\bin\cpf-start.ps1 -Target backoffice-web
.\bin\cpf-status.ps1 -Target backoffice-web
```

```bash
read -rp 'MBW 최초 운영자 ID: ' CPF_MBW_INITIAL_OPERATOR_LOGIN_ID; export CPF_MBW_INITIAL_OPERATOR_LOGIN_ID
read -rp 'MBW 최초 운영자 이름: ' CPF_MBW_INITIAL_OPERATOR_NAME; export CPF_MBW_INITIAL_OPERATOR_NAME
read -rp 'MBW 최초 운영자 역할 코드: ' CPF_MBW_INITIAL_OPERATOR_ROLE_CODE; export CPF_MBW_INITIAL_OPERATOR_ROLE_CODE
read -rsp 'MBW 초기 비밀번호: ' CPF_MBW_BOOTSTRAP_PASSWORD; export CPF_MBW_BOOTSTRAP_PASSWORD
read -rsp '32자 이상 MBW JWT Secret: ' CPF_MBW_JWT_SECRET; export CPF_MBW_JWT_SECRET
export MBW_WEB_MODE=DIRECT
export MBW_DIRECT_BASE_URI=http://127.0.0.1:8082
export MBW_WEB_SECURE_COOKIES=false
./bin/cpf-start.sh  --target backoffice
./bin/cpf-start.sh  --target backoffice-web
./bin/cpf-status.sh --target backoffice-web
```

브라우저에서 http://127.0.0.1:8092/mbw/ 로 접속해 MBW 최초 운영자로 로그인합니다. 로그인 후 Browser
session Cookie와 CSRF token이 발급되고, Backoffice Web이 인증된 MBW 업무 API를 호출합니다.

### 4) 상태 확인과 정지

```powershell
.\bin\cpf-health.ps1 -Target admin
.\bin\cpf-stop.ps1   -Target backoffice-web
.\bin\cpf-stop.ps1   -Target backoffice
.\bin\cpf-stop.ps1   -Target admin
```

```bash
./bin/cpf-health.sh --target admin
./bin/cpf-stop.sh   --target backoffice-web
./bin/cpf-stop.sh   --target backoffice
./bin/cpf-stop.sh   --target admin
```

### Profile

기본 Profile은 `local`입니다. 개발 구성을 쓰려면 `CPF_PROFILE=dev`를 설정한 뒤 기동합니다.


## 문서

`cpf-docs/guides`에 프레임워크/배치 개발자 가이드, 운영자 매뉴얼, Gateway 가이드, 기술 명세가 있고
`cpf-docs/deliverables`에 아키텍처 설계서와 데이터베이스 표준서가 있습니다.


`reset`만 Local 개발 Data를 삭제할 수 있으며 명시적 확인 옵션이 필요합니다.

## 배포 경계

이 Workspace는 CPF Private Source에서 Default-Deny 정책으로 매번 Fresh 생성됩니다. Release Tool은 `git add`/index staging/commit/push를 수행하지 않고 `VERIFIED`까지만 생성합니다. 모든 필수 Gate가 PASS한 뒤 사용자가 변경 내용을 직접 확인하고 이 Open Git Repository에서 Commit/Push합니다. 이 Release 결과는 CPF Private master에 Commit/Push하지 않습니다.
