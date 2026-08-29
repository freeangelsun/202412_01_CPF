from pathlib import Path
import re

ROOT = Path(__file__).resolve().parents[3]
CONVENTION = ROOT / 'cpf-tools/build/cpf-root-conventions.gradle'
PS1 = ROOT / 'cpf-tools/build/tools/cpf-dev.ps1'
SH = ROOT / 'cpf-tools/build/tools/cpf-dev.sh'


def test_canonical_gradle_entrypoints_are_short_and_grouped():
    text = CONVENTION.read_text(encoding='utf-8')
    required = {
        'cpfBuild': 'CPF Build',
        'cpfTest': 'CPF Test',
        'cpfVerifyFast': 'CPF Verification',
        'cpfVerifyFullLocal': 'CPF Verification',
        'cpfHelp': 'CPF Configuration/Discovery',
        'cpfRunLocal': 'CPF Runtime',
        'cpfRunBatch': 'CPF Runtime',
        'cpfModules': 'CPF Configuration/Discovery',
        'cpfResourcePolicy': 'CPF Configuration/Discovery',
    }
    for task, group in required.items():
        assert task in text, task
        assert group in text, group
    assert 'tasks.named(\'cpfTest\') { dependsOn allJavaTests }' in text
    assert "tasks.register('cpfBuildInfo')" not in text
    assert "tasks.register('cpfVerifyFullLocal', Exec)" in text
    assert "run-cpf-local-full-validation.ps1" in text


def test_single_developer_shell_exposes_only_clear_actions():
    text = PS1.read_text(encoding='utf-8')
    for action in ('help','build','test','verify-fast','verify-full','run-local','run-batch','status','stop','modules','resource'):
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
    actions = ('build','test','verify-fast','verify-full','run-local','run-batch','status','stop','modules','resource')
    for action in actions:
        assert action in ps
        assert action in sh


def test_no_versioned_developer_shell_names_are_introduced():
    names = [PS1.name, SH.name]
    forbidden = re.compile(r'(_FINAL|_V\d+|_CURRENT|_NXT3|_SESSION|_20\d{6})', re.I)
    assert not any(forbidden.search(name) for name in names)


def test_gradle_run_aliases_use_logical_project_tree():
    text = CONVENTION.read_text(encoding='utf-8')
    expected = {
        'cpfRunLocal': ':runtime:local:bootRun',
        'cpfRunAdm': ':apps:admin:bootRun',
        'cpfRunBackoffice': ':apps:backoffice:bootRun',
        'cpfRunGateway': ':runtime:gateway:bootRun',
        'cpfRunBatch': ':runtime:local-batch:bootRun',
        'cpfRunEducation': ':apps:education:bootRun',
    }
    for task, target in expected.items():
        assert f"registerCpfRunAlias('{task}', '{target}'" in text
    assert 'Gradle Projects는 apps / runtime / framework / starters / internal 계층' in text
    for legacy in (
        ':cpf-local-runtime:bootRun', ':cpf-admin:bootRun', ':cpf-backoffice:bootRun',
        ':cpf-gateway:bootRun', ':cpf-local-batch-runtime:bootRun', ':cpf-education:bootRun',
    ):
        assert legacy not in text
