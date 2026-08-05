from __future__ import annotations
import importlib.util
import json
import shutil
from pathlib import Path
import pytest

ROOT = Path(__file__).resolve().parents[3]
SCRIPT = ROOT / 'cpf-tools/scripts/verify-cpf-product-governance.py'
spec = importlib.util.spec_from_file_location('product_gate', SCRIPT)
mod = importlib.util.module_from_spec(spec)
assert spec.loader
spec.loader.exec_module(mod)


def fixture(tmp_path: Path) -> Path:
    for rel in [
        'cpf-tools/product-governance/product-capability-policy.json',
        'cpf-tools/scripts/verify-cpf-product-governance.py',
        'cpf-tools/scripts/run-cpf-full-qa-validation.ps1',
    ]:
        dst = tmp_path / rel
        dst.parent.mkdir(parents=True, exist_ok=True)
        shutil.copy2(ROOT / rel, dst)
    return tmp_path


def mutate(root: Path, mutation) -> None:
    path = root / 'cpf-tools/product-governance/product-capability-policy.json'
    data = json.loads(path.read_text(encoding='utf-8'))
    mutation(data)
    path.write_text(json.dumps(data), encoding='utf-8')


def test_positive_contract() -> None:
    assert mod.verify(ROOT)['status'] == 'PASS'


@pytest.mark.parametrize(
    ('mutation', 'expected'),
    [
        (lambda d: d.__setitem__('policyMaturity', 'GA'), 'must not be exposed as GA'),
        (lambda d: d['multiTenantPrototype'].__setitem__('crossTenantAccess', 'ALLOW'), 'cross-tenant boundary'),
        (lambda d: d['pluginPrototype'].__setitem__('signatureRequired', False), 'unsigned plugin'),
        (lambda d: d['capabilityPackagePrototype'].__setitem__('rollbackPlanRequired', False), 'rollbackPlanRequired'),
        (lambda d: d['securityAndCompatibility'].__setitem__('permissionBypass', 'ALLOW'), 'permissionBypass'),
    ],
)
def test_negative_contract(tmp_path: Path, mutation, expected: str) -> None:
    root = fixture(tmp_path)
    mutate(root, mutation)
    with pytest.raises(mod.GateError, match=expected):
        mod.verify(root)
