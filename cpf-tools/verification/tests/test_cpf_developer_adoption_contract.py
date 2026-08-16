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
