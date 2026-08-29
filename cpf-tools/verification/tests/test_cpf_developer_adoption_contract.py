import importlib.util
from pathlib import Path

ROOT = Path(__file__).resolve().parents[3]
SCRIPT = ROOT / 'cpf-tools/verification/verify_cpf_developer_adoption_contract.py'
spec = importlib.util.spec_from_file_location('developer_adoption', SCRIPT)
assert spec and spec.loader
module = importlib.util.module_from_spec(spec)
spec.loader.exec_module(module)


def test_forbidden_native_scan_is_scoped_to_business_and_generator_sources(tmp_path: Path):
    bad = tmp_path / 'cpf-member/online/src/main/java/demo/Bad.java'
    bad.parent.mkdir(parents=True)
    bad.write_text('class Bad { Object x = WebClient.builder(); }', encoding='utf-8')
    internal = tmp_path / 'cpf-starters/integration/http/src/main/java/demo/Ok.java'
    internal.parent.mkdir(parents=True)
    internal.write_text('class Ok { Object x = WebClient.builder(); }', encoding='utf-8')
    findings = module.scan_forbidden(tmp_path)
    assert any('cpf-member' in row and 'WebClient.builder' in row for row in findings)
    assert not any('cpf-starters' in row for row in findings)


def test_usage_levels_are_explicit_contract_values():
    assert module.VALID_USAGE == {'golden', 'capability', 'advanced', 'internal'}
    assert module.EXPECTED_CALLS == {'application', 'cpf-domain', 'external-integration'}


def test_developer_shells_delegate_targeted_verification_to_unified_cli():
    powershell = (ROOT / 'cpf-tools/build/tools/cpf-dev.ps1').read_text(encoding='utf-8-sig')
    shell = (ROOT / 'cpf-tools/build/tools/cpf-dev.sh').read_text(encoding='utf-8')
    assert module.validate_developer_shell_text(powershell, shell) == []


def test_developer_shell_contract_rejects_direct_gradle_and_missing_posix_forwarding():
    powershell = "'verify-targeted'=@('dev','targeted-test'); @ArgsFromCli; gradlew cpfVerifyTargeted"
    shell = 'verify-targeted) exec "$CLI" dev targeted-test ;;'
    errors = module.validate_developer_shell_text(powershell, shell)
    assert any('duplicates' in error for error in errors)
    assert any('POSIX' in error and 'forwarding' in error for error in errors)
