# CPF Open Git Release

`cpf-tools/release/open-git`은 Private CPF Source에서 Open Git 개발 Workspace와 Binary Repository를 생성·검증하는 Release Owner입니다.

## 사용자 명령

최종 Canonical UX는 Unified Java CLI의 Internal Release Namespace입니다.

```text
cpf release open-git
cpf release open-git check --profile binary
cpf release open-git consumer-runtime
cpf release open-git status
cpf release open-git build --profile source
cpf release open-git help
```

`cpf-tools/release/open-git/cpf_open_git.py`와 OS별 release script는 Canonical Release Engine/저수준 자동화 진입점이며 별도 사용자 CLI가 아닙니다. 공식 사용자는 `cpf release open-git ...`만 사용합니다.

Binary Release는 `windows-x64`, `linux-x64` Generator executable 두 개를 checksum/manifest와 함께 반드시 포함합니다. Windows에서 기본 `cpf release open-git`을 실행하면 Windows artifact는 host에서, 누락된 Linux artifact는 Docker Desktop의 실제 `linux/amd64` container에서 fresh PyInstaller build합니다. 파일명 변경이나 OS classifier 대체는 허용하지 않습니다. CI가 이미 검증한 matrix가 있으면 `--generator-artifacts <flat-directory>`로 주입할 수 있으며, Linux host에서는 검증된 `windows-x64` CI artifact가 필요합니다.

```powershell
.\cpf-tools\release\open-git\cpf-open-git.ps1
.\cpf-tools\release\open-git\cpf-open-git.ps1 check
.\cpf-tools\release\open-git\cpf-open-git.ps1 status
```

기존 설치본에서 transient Release 경로 규칙 또는 Canonical integration이 누락된 경우에만 호환 `setup`을 1회 실행합니다. 신규 Current Source는 integration 완료 상태가 정본입니다.

```powershell
.\cpf-tools\release\open-git\cpf-open-git.ps1 setup
```

`setup`은 전체 파일을 덮어쓰지 않고 Source Identity의 generated-root 제외와 Canonical Requirement 21.3 연결만 확인합니다. `/cpf-release/` 전체 ignore는 금지하며, `work/`, `logs/`, `open-git/`만 transient입니다.

## 생성 결과

```text
cpf-release/
├─ open-git/             실제 Open Git fresh clone + 검증된 Source Projection
├─ binary-repository/    Maven-compatible CPF Binary Repository
├─ reports/              Manifest / SHA / Release Status
└─ logs/                 실행 로그 Owner
```

성공 시 임시 `work/`는 제거됩니다. 재실행 시 기존 `cpf-release/` 전체를 안전성 검증 후 삭제하고 처음부터 다시 만듭니다.

## 안전 경계

- `cpf-release` 이외 경로는 재생성 삭제 대상으로 허용하지 않습니다.
- `cpf-release`가 symlink이면 삭제하지 않습니다.
- Private Git의 `cpf-release/work`, `logs`, `open-git` tracked path는 중단합니다. catalog-classified Current Verified `binary-repository`/`reports`만 허용하고 executable runtime JAR은 exact Git LFS attribute/materialization/SHA를 추가 검증합니다.
- Open Git Source는 Default-Deny입니다.
- Open Git Source에 Framework 내부 Source/JAR/WAR를 넣지 않습니다.
- Binary Repository의 Framework sources/javadoc은 모든 Public Profile에서 0건입니다. Optional `source` Profile은 allowlist Source Tree만 제공합니다.
- `cpf-release/`의 transient subtree는 Open Git 전달용 local staging이며 Private CPF master Commit/Push 대상이 아닙니다. catalog-classified Current Verified `binary-repository`/`reports`는 사용자 승인으로 별도 반영할 수 있습니다.
- Release Tool은 Private/Open Git 어디에서도 사용자 승인 전 `git add`/index staging/commit/push를 실행하지 않습니다.
- Release Tool은 `VERIFIED`까지 생성하고, 필수 Gate PASS 후 사용자가 `cpf-release/open-git`을 검토해 Open Git에 직접 Commit/Push합니다.
