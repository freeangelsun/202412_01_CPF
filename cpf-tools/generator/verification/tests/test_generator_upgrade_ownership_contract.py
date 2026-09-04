from __future__ import annotations
import importlib.util, json
from pathlib import Path
ROOT=Path(__file__).resolve().parents[4]
GATE=ROOT/'cpf-tools/generator/verification/verify-cpf-generator-upgrade-ownership.py'
spec=importlib.util.spec_from_file_location('upgrade_gate',GATE); assert spec and spec.loader
MODULE=importlib.util.module_from_spec(spec); spec.loader.exec_module(MODULE)

def test_upgrade_contract_uses_transient_state_not_customer_manifest():
    engine=ROOT/'cpf-tools/generator/engine/cpf_domain_generator.py'
    assert MODULE.validate(engine)==[]
    text=engine.read_text(encoding='utf-8')
    assert 'generation-state.json' in text
    assert 'generator-ownership.json' not in text
    cli=(ROOT/'cpf-tools/runtime/cli/cpf.py').read_text(encoding='utf-8')
    assert 'VERIFICATION_PENDING_TEMPLATE_ADOPTION' in cli
    assert '--approve-template-adoption' in cli
    assert 'approve_template_adoption' in cli

def test_lifecycle_contract_requires_restore_protection():
    contract=json.loads((ROOT/'cpf-tools/generator/contracts/generator-lifecycle-contract.json').read_text(encoding='utf-8'))
    assert contract['userProtection']['restoreRequiresMatchingDefinitionAndExpectedSeed'] is True
    assert contract['userProtection']['unmanagedFilesAreNeverRemoved'] is True
