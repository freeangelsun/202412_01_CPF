from pathlib import Path
import json
import re

ROOT = Path(__file__).resolve().parents[3]
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
        'cpfRunAllBatch': '30. CPF 실행',
        'cpfModules': '40. CPF 구성',
        'cpfResourcePolicy': '50. CPF 설정',
    }
    for task, group in required.items():
        assert task in text, task
        assert group in text, group
    assert 'tasks.named(\'cpfTestAll\') { dependsOn allJavaTests }' in text
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
    # 통합 Runtime 만 고정 진입점이고 개별 실행은 발견된 App/Domain 으로 투영된다.
    expected = {
        'cpfRunAllLocal': ':runtime:local:bootRun',
        'cpfRunAllBatch': ':runtime:local-batch:bootRun',
    }
    for task, target in expected.items():
        assert f"registerCpfRunAlias('{task}', '{target}'" in text
    # 개별 App 은 물리 디렉터리가 아니라 Gradle 논리 project path 로 실행해야 한다.
    assert 'dependsOn "${a.path}:bootRun"' in text
    assert 'dependsOn "${d.mountedPath}:bootRun"' in text
    assert 'Gradle Projects는 apps / runtime / framework / starters / internal 계층' in text
    for legacy in (
        ':cpf-local-runtime:bootRun', ':cpf-admin:bootRun', ':cpf-backoffice:bootRun',
        ':cpf-gateway:bootRun', ':cpf-local-batch-runtime:bootRun', ':cpf-education:bootRun',
    ):
        assert legacy not in text


def test_vscode_gradle_import_uses_current_portable_project_cache():
    settings = json.loads(VSCODE_SETTINGS.read_text(encoding='utf-8'))
    arguments = settings.get('java.import.gradle.arguments', '')
    expected = (
        '--project-cache-dir '
        'cpf-docs/governance/development-harness/evidence/platform/current/generated/gradle/project-cache'
    )
    assert arguments == expected
    normalized = arguments.replace('\\', '/').lower()
    assert 'cpf-docs/work/' not in normalized
    assert not re.search(r'[a-z]:/', normalized), 'workspace-local absolute path must not be canonical'
