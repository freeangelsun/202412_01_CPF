# CPF Open Git Developer Workspace

CPF(Core Platform Framework)를 사용하는 업무 개발자를 위한 공개 개발 Workspace입니다.

Framework 내부 구현 Source는 포함하지 않습니다. 업무 개발자가 직접 수정하거나 참고해야 하는 Generated Domain, Backoffice, EDU Source와 개발 명령만 공개하며, CPF Framework 자체는 검증된 Binary Repository를 통해 사용합니다.

## 공개 Source

- `cpf-member`: Batch 포함 Generated Domain Reference
- `cpf-external`: Online Generated Domain Reference
- `cpf-backoffice`: MBW Backoffice 업무 Domain Source
- `cpf-backoffice-web`: 외부 Backoffice Channel/BFF Source
- `cpf-education`: CPF 기능별 실행·학습 Source
- `bin`: Java 기반 Unified `cpf` CLI와 Windows/Linux Thin Wrapper

`cpf-core`, `cpf-common`, `cpf-admin`, `cpf-gateway`, `cpf-batch`, `cpf-starters` 내부 구현 Source는 이 Repository에 포함되지 않습니다.

## 시작

필수 환경은 Git, Java 25, 승인된 Container Runtime이며 Backoffice Frontend 사용 시 Node.js가 필요합니다.

```powershell
$env:CPF_MAVEN_REPOSITORY_URL='<cpf-binary-repository-url>'
$env:CPF_VERSION='<cpf-version>'
.\cpf.cmd bootstrap
```

Linux:

```bash
export CPF_MAVEN_REPOSITORY_URL='<cpf-binary-repository-url>'
export CPF_VERSION='<cpf-version>'
./bin/cpf bootstrap
```

## 자주 쓰는 명령

```powershell
.\cpf.cmd domain-new account --system-code ACC
.\cpf.cmd domain-sync
.\cpf.cmd build
.\cpf.cmd test
.\cpf.cmd stop
```

Windows: `.\cpf.cmd reset --confirm-local-reset` / Linux: `./bin/cpf reset --confirm-local-reset`

`reset`만 Local 개발 Data를 삭제할 수 있으며 명시적 확인 옵션이 필요합니다.

## 배포 경계

이 Workspace는 CPF Private Source에서 Default-Deny 정책으로 매번 Fresh 생성됩니다. Release Tool은 `git add`/index staging/commit/push를 수행하지 않고 `VERIFIED`까지만 생성합니다. 모든 필수 Gate가 PASS한 뒤 사용자가 변경 내용을 직접 확인하고 이 Open Git Repository에서 Commit/Push합니다. 이 Release 결과는 CPF Private master에 Commit/Push하지 않습니다.
