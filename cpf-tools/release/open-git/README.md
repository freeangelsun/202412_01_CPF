# CPF Open Git Release

`cpf-tools/release/open-git`은 Private CPF Source에서 Open Git 개발 Workspace와 Binary Repository를 생성·검증하는 Release Owner입니다.

## 사용자 명령

최종 Canonical UX는 Unified Java CLI의 Internal Release Namespace입니다.

```text
cpf release open-git
cpf release open-git check --profile binary
cpf release open-git status
cpf release open-git build --profile source
```

`cpf-tools/release/open-git/cpf_open_git.py`와 OS별 release script는 Canonical Release Engine/저수준 자동화 진입점이며 별도 사용자 CLI가 아닙니다. 공식 사용자는 `cpf release open-git ...`만 사용합니다.

```powershell
.\cpf-tools\release\open-git\cpf-open-git.ps1
.\cpf-tools\release\open-git\cpf-open-git.ps1 check
.\cpf-tools\release\open-git\cpf-open-git.ps1 status
```

기존 설치본에서 `/cpf-release/` 제외 또는 Canonical integration이 누락된 경우에만 호환 `setup`을 1회 실행합니다. 신규 Current Source는 integration 완료 상태가 정본입니다.

```powershell
.\cpf-tools\release\open-git\cpf-open-git.ps1 setup
```

`setup`은 전체 파일을 덮어쓰지 않고 `/cpf-release/` ignore, Source Identity 제외, Canonical Requirement 21.3을 좁은 Anchor 기반으로 추가합니다.

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
- Private Git tracked path가 `cpf-release` 아래에 있으면 중단합니다.
- Open Git Source는 Default-Deny입니다.
- Open Git Source에 Framework 내부 Source/JAR/WAR를 넣지 않습니다.
- Binary Repository의 Framework sources/javadoc은 모든 Public Profile에서 0건입니다. Optional `source` Profile은 allowlist Source Tree만 제공합니다.
- `cpf-release/`는 Open Git 전달 전용 local-generated staging이며 Private CPF master Commit/Push 대상이 아닙니다.
- Release Tool은 Private/Open Git 어디에서도 사용자 승인 전 `git add`/index staging/commit/push를 실행하지 않습니다.
- Release Tool은 `VERIFIED`까지 생성하고, 필수 Gate PASS 후 사용자가 `cpf-release/open-git`을 검토해 Open Git에 직접 Commit/Push합니다.
