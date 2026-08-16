from __future__ import annotations
import importlib.util
from pathlib import Path
ROOT=Path(__file__).resolve().parents[4]
SCRIPT=ROOT/'cpf-tools/generator/verification/verify-cpf-generator-lifecycle.py'
spec=importlib.util.spec_from_file_location('generator_lifecycle_runtime',SCRIPT); assert spec and spec.loader
MODULE=importlib.util.module_from_spec(spec); spec.loader.exec_module(MODULE)

def test_static_contract_and_runtime_lifecycle():
    contract=MODULE.load_json(ROOT/'cpf-tools/generator/contracts/generator-lifecycle-contract.json')
    MODULE.validate_contract(ROOT,contract)
    MODULE.validate_lifecycle_runtime(ROOT,contract)
