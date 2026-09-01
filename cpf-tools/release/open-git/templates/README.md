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

## 문서

`cpf-docs/guides`에 프레임워크/배치 개발자 가이드, 운영자 매뉴얼, Gateway 가이드, 기술 명세가 있고
`cpf-docs/deliverables`에 아키텍처 설계서와 데이터베이스 표준서가 있습니다.


`reset`만 Local 개발 Data를 삭제할 수 있으며 명시적 확인 옵션이 필요합니다.

## 배포 경계

이 Workspace는 CPF Private Source에서 Default-Deny 정책으로 매번 Fresh 생성됩니다. Release Tool은 `git add`/index staging/commit/push를 수행하지 않고 `VERIFIED`까지만 생성합니다. 모든 필수 Gate가 PASS한 뒤 사용자가 변경 내용을 직접 확인하고 이 Open Git Repository에서 Commit/Push합니다. 이 Release 결과는 CPF Private master에 Commit/Push하지 않습니다.
