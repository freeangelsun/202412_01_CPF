from __future__ import annotations
import importlib.util
from pathlib import Path
import unittest
ROOT=Path(__file__).resolve().parents[4]
SCRIPT=ROOT/'cpf-tools/generator/verification/verify-cpf-generator-lifecycle.py'
spec=importlib.util.spec_from_file_location('generator_lifecycle',SCRIPT); assert spec and spec.loader
MODULE=importlib.util.module_from_spec(spec); spec.loader.exec_module(MODULE)

class GeneratorLifecycleEntrypointContractTest(unittest.TestCase):
    def test_repository_contract_is_stateless_v5(self):
        contract=MODULE.load_json(ROOT/'cpf-tools/generator/contracts/generator-lifecycle-contract.json')
        MODULE.validate_contract(ROOT,contract)
        self.assertEqual(5,contract['schemaVersion'])
        self.assertEqual('NONE',contract['transientState']['customerProjectMetadata'])
        self.assertIn('manifest',contract['forbiddenPermanentProjectEntries'])

    def test_contract_rejects_permanent_metadata_policy(self):
        # Repository files are used; mutate only the in-memory contract.
        contract=MODULE.load_json(ROOT/'cpf-tools/generator/contracts/generator-lifecycle-contract.json')
        contract['forbiddenPermanentProjectEntries']=[x for x in contract['forbiddenPermanentProjectEntries'] if x!='manifest']
        with self.assertRaises(MODULE.ContractError):
            MODULE.validate_contract(ROOT,contract)


if __name__ == '__main__':
    unittest.main()
