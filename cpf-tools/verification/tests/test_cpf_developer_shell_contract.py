from pathlib import Path
import json
import os
import re
import sys

if hasattr(sys.stdout, "reconfigure"):
    sys.stdout.reconfigure(encoding="utf-8")

# negative mutation 은 격리된 사본에서 계약을 다시 돌린다. 그때 이 값으로 검사 대상을 옮긴다.
ROOT = Path(os.environ.get("CPF_DEVELOPER_SHELL_ROOT") or Path(__file__).resolve().parents[3])
CONVENTION = ROOT / 'cpf-tools/build/cpf-root-conventions.gradle'
PS1 = ROOT / 'cpf-tools/build/tools/cpf-dev.ps1'
SH = ROOT / 'cpf-tools/build/tools/cpf-dev.sh'
VSCODE_SETTINGS = ROOT / '.vscode/settings.json'


def test_canonical_gradle_entrypoints_are_short_and_grouped():
    text = CONVENTION.read_text(encoding='utf-8')
    required = {
        'cpfBuildAll': '10. CPF 빌드',
        'cpfTestAll': '10. CPF 빌드',
        'cpfVerifyFast': '20. CPF 검증',
        'cpfVerifyAllLocal': '20. CPF 검증',
        'cpfHelp': '00. CPF 시작',
        'cpfRunAllLocal': '30. CPF 실행',
        'cpfRunDevLocal': '30. CPF 실행',
        'cpfRunAllBatch': '30. CPF 실행',
        'cpfModules': '40. CPF 구성',
        'cpfResourcePolicy': '50. CPF 설정',
    }
    for task, group in required.items():
        assert task in text, task
        assert group in text, group
    assert "tasks.named('cpfTestAll') {" in text
    assert 'dependsOn allJavaTests' in text
    assert 'dependsOn cpfAllDomainTestTasks' in text
    assert "tasks.register('cpfBuildInfo')" not in text
    assert "tasks.register('cpfVerifyAllLocal', Exec)" in text
    assert "run-cpf-local-full-validation.ps1" in text


def test_single_developer_shell_exposes_only_clear_actions():
    text = PS1.read_text(encoding='utf-8')
    for action in ('help','build','test','verify-fast','verify-targeted','verify-full','run-local','run-batch','status','stop','modules','resource'):
        assert f"'{action}'" in text
    # Compatibility shell is intentionally thin: canonical behavior belongs to the
    # exactly-one Java `cpf` CLI, never to PowerShell/Bash duplicates.
    assert 'runtime\\cli\\cpf.ps1' in text or 'runtime/cli/cpf.ps1' in text
    for forbidden in (
        'run-cpf-local-full-validation.ps1', 'start-cpf-local.ps1',
        'status-cpf-local.ps1', 'stop-cpf-local.ps1',
        'function Read-CpfDevAction', 'CPF 개발 메뉴', 'gradlew', 'docker compose',
    ):
        assert forbidden not in text
    assert 'C:\\Users\\' not in text


def test_windows_and_unix_shell_surfaces_match():
    ps = PS1.read_text(encoding='utf-8')
    sh = SH.read_text(encoding='utf-8')
    actions = ('build','test','verify-fast','verify-targeted','verify-full','run-local','run-batch','status','stop','modules','resource')
    for action in actions:
        assert action in ps
        assert action in sh
    assert "'verify-targeted'=@('dev','targeted-test')" in ps.replace(' ', '')
    assert 'verify-targeted) exec "$CLI" dev targeted-test "$@"' in ' '.join(sh.split())


def test_no_versioned_developer_shell_names_are_introduced():
    names = [PS1.name, SH.name]
    forbidden = re.compile(r'(_FINAL|_V\d+|_CURRENT|_NXT3|_SESSION|_20\d{6})', re.I)
    assert not any(forbidden.search(name) for name in names)


def test_gradle_run_aliases_use_logical_project_tree():
    text = CONVENTION.read_text(encoding='utf-8')
    # Batch 통합 Runtime 만 alias 이고, Local Runtime 은 canonical launcher 를 재사용한다.
    assert "registerCpfRunAlias('cpfRunAllBatch', ':runtime:local-batch:bootRun'" in text
    assert "cpf-tools/runtime/tools/cpf_local_runtime.py" in text
    for task in ('cpfRunAllLocal', 'cpfRunDevLocal', 'cpfRunOnlineLocal'):
        assert f"task: '{task}'" in text, f"Local Runtime 진입점 누락: {task}"
    # 개별 App 은 물리 디렉터리가 아니라 Gradle 논리 project path 로 실행해야 한다.
    assert 'dependsOn "${a.path}:bootRun"' in text
    assert 'dependsOn "${d.mountedPath}:bootRun"' in text
    assert 'Gradle Projects는 apps / runtime / framework / starters / internal 계층' in text
    for legacy in (
        ':cpf-local-runtime:bootRun', ':cpf-admin:bootRun', ':cpf-backoffice:bootRun',
        ':cpf-gateway:bootRun', ':cpf-local-batch-runtime:bootRun', ':cpf-education:bootRun',
    ):
        assert legacy not in text


def test_vscode_gradle_import_shares_the_single_project_cache():
    """IDE 와 CLI 는 하나의 Gradle project cache 를 공유한다.

    증상 근거: IDE import 에만 별도 --project-cache-dir 를 주면 같은 build tree 에 stale-output
    registry 가 두 벌 생긴다. 두 registry 는 서로가 만든 build/classes 를 모르는 산출물로 보고
    지우므로, IDE 와 CLI 를 번갈아 쓰면 VS Code 에 code 964 missing required library 가 반복된다.
    이것은 Harness 36 이 공개 Workspace 에서 확인한 것과 같은 기전이다.

    금지 사항은 그대로다. 사용자/머신에 묶인 절대경로와 legacy cpf-docs/work 경로는 쓰지 않는다.
    """
    settings = json.loads(VSCODE_SETTINGS.read_text(encoding='utf-8'))
    arguments = settings.get('java.import.gradle.arguments', '')
    assert '--project-cache-dir' not in arguments, (
        'IDE import 가 CLI 와 다른 project cache 를 쓰면 서로의 build 산출물을 지운다: ' + arguments)
    normalized = arguments.replace('\\', '/').lower()
    assert 'cpf-docs/work/' not in normalized
    assert not re.search(r'[a-z]:/', normalized), 'workspace-local absolute path must not be canonical'

if __name__ == "__main__":
    # negative fixture 가 이 파일을 직접 실행한다. pytest 없이도 같은 계약을 판정해야 한다.
    failures = []
    for _name, _fn in sorted(dict(globals()).items()):
        if not _name.startswith("test_") or not callable(_fn):
            continue
        try:
            _fn()
        except Exception as failure:  # noqa: BLE001 - 계약 위반을 그대로 보고한다
            failures.append(f"{_name}: {failure}")
    for _failure in failures:
        print("FAIL " + _failure)
    print(f"DEVELOPER_SHELL_CONTRACT={'FAIL' if failures else 'PASS'}")
    raise SystemExit(1 if failures else 0)
