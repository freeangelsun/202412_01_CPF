import importlib.util
from pathlib import Path

ROOT = Path(__file__).resolve().parents[3]
SCRIPT = ROOT / 'cpf-tools/verification/tools/run-cpf-targeted-verification.py'
spec = importlib.util.spec_from_file_location('cpf_targeted', SCRIPT)
assert spec and spec.loader
module = importlib.util.module_from_spec(spec)
spec.loader.exec_module(module)


def test_targeted_plan_always_contains_cross_cutting_gates():
    plan = module.build_plan(['cache'])
    assert 'cpf-tools/verification/verify_starter_catalog.py' in plan
    assert 'cpf-tools/verification/verify_gradle_project_dependency_closure.py' in plan
    assert 'cpf-tools/verification/verify_cpf_developer_adoption_contract.py' in plan
    assert 'cpf-tools/verification/tools/verify-cpf-cache-capability.py' in plan


def test_targeted_plan_deduplicates_cross_capability_gates():
    plan = module.build_plan(['core', 'cache', 'cache'])
    assert len(plan) == len(set(plan))
