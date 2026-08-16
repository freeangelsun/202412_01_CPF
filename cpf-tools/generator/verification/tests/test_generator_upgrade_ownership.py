from importlib.util import module_from_spec, spec_from_file_location
from pathlib import Path
ROOT=Path(__file__).resolve().parents[4]
SCRIPT=ROOT/'cpf-tools/generator/verification/verify-cpf-generator-upgrade-ownership.py'
spec=spec_from_file_location('upgrade_gate',SCRIPT); module=module_from_spec(spec); assert spec.loader; spec.loader.exec_module(module)

def test_current_upgrade_is_transient_state_driven_and_fail_closed():
    target=ROOT/'cpf-tools/generator/engine/cpf_domain_generator.py'
    assert module.validate(target)==[]

def test_gate_rejects_permanent_customer_ownership_metadata(tmp_path):
    target=tmp_path/'engine.py'
    target.write_text('\n'.join(module.REQUIRED)+'\ngenerator-ownership.json\n',encoding='utf-8')
    assert any('forbidden permanent metadata' in e for e in module.validate(target))
