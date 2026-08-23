# Open Git Packaging Overlay — Apply and Verify

## 적용 시점

이 Overlay는 기존 Baseline 파일을 덮어쓰지 않고 신규 파일만 추가하지만 Source Identity에는 신규 파일이 반영된다. 따라서 **현재 Codex Final Gate/Source Identity 작업이 끝난 뒤** 적용한다.

## 1. Overlay 적용

ZIP은 CPF Root 상대경로로 구성되어 있다. CPF Root에 압축을 풀어 신규 파일을 추가한다. 기존 Product/Gradle/Generator/Runtime 파일은 ZIP에 포함하지 않는다.

## 2. Canonical integration

Codex 종료 후 한 번 실행한다.

```powershell
.\cpf-tools\release\open-git\cpf-open-git.ps1 setup
```

`setup`은 다음 기존 파일만 Anchor 기반으로 좁게 수정한다.

- `.gitignore` → `/cpf-release/`
- `cpf-tools/verification/tools/cpf-source-state.py` → `cpf-release` Source Identity 제외
- `cpf-tools/runtime/cli/cpf.py` → `cpf open-git` 명령
- `cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md` → §21.3 Open Git Release Packaging

Anchor가 Codex 변경으로 사라졌으면 전체 파일을 덮어쓰지 않고 FAIL한다. 원인을 확인한 뒤 최신 Source에 맞춰 통합해야 한다.

## 3. 저비용 검증

```powershell
python -m pytest -q cpf-tools/release/open-git/tests/test_cpf_open_git.py cpf-tools/release/public/tests/test_prepare_cpf_public_workspace.py cpf-tools/release/public/tests/test_publish_cpf_public_repository.py cpf-tools/release/public/tests/test_verify_cpf_public_binary_repository.py cpf-tools/release/public/tests/test_verify_cpf_public_binary_consumer.py
```

정상 기대: `28 passed`, fail 0.

## 4. 현재 Integration Blocker 확인

```powershell
.\cpf-tools\release\open-git\cpf-open-git.ps1
```

현재 개발 기준 Source에서는 Artifact Catalog 계약 불일치를 fail-closed하는 것이 정상이다. `cpf-core`, `cpf-admin`, `cpf-gateway` Publication 계약을 Codex 최종 Source 기준으로 정리하기 전 `READY_TO_COMMIT`으로 간주하지 않는다.

## 5. 최종 정상 UX

Open Git 개발자 Workspace에서는 아래 짧은 단일 명령을 사용한다. 각 장시간 명령은 진행 단계/실시간 로그를 표시하고 종료 시 PASS/FAIL, ExitCode, 시각, Log 경로와 다음 행동을 출력한다.

```text
cpf bootstrap
cpf build
cpf test
cpf verify
cpf domain new <name> <SYSTEM_CODE>
cpf domain sync
cpf status
cpf stop
cpf reset --confirm
```

Artifact Publication closure와 Java25 Gate가 끝난 뒤 Release 담당자는:

```text
cpf open-git
cpf open-git check
cpf open-git status
```

`cpf open-git` 성공 시 `cpf-release/`는 매번 전체 안전 재생성되며 최종적으로 다음만 사용자가 확인한다.

```text
cpf-release/
├─ open-git/
├─ binary-repository/
├─ reports/
└─ logs/
```

Open Git의 commit/push는 자동 수행하지 않는다. `cpf-release/open-git`에서 사용자가 `git status`, `git diff --cached`를 확인한 후 직접 수행한다.
