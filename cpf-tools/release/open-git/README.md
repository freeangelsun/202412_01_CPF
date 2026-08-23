# CPF Open Git Release

`cpf-tools/release/open-git`은 Private CPF Source에서 Open Git 개발 Workspace와 Binary Repository를 생성·검증하는 Release Owner입니다.

## 사용자 명령

최종 Canonical UX는 다음 세 가지입니다.

```text
cpf open-git
cpf open-git check
cpf open-git status
```

Codex와의 병행개발 충돌을 피하기 위해 현재 Overlay는 기존 Runtime CLI를 직접 수정하지 않습니다. 통합 전에는 아래 전용 진입점을 사용하며, Codex 종료 후 `cpf open-git` alias만 얇게 연결합니다.

```powershell
.\cpf-tools\release\open-git\cpf-open-git.ps1
.\cpf-tools\release\open-git\cpf-open-git.ps1 check
.\cpf-tools\release\open-git\cpf-open-git.ps1 status
```

최초 한 번, Codex 작업 종료 후 Canonical integration을 적용합니다.

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
- Binary Repository의 sources/javadoc은 Default-Deny입니다.
- 자동 commit/push는 절대 수행하지 않습니다.
