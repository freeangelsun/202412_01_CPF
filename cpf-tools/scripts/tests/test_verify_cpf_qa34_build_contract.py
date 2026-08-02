from __future__ import annotations

import importlib.util
import unittest
from pathlib import Path


SCRIPT = Path(__file__).resolve().parents[1] / "verify-cpf-qa34-build-contract.py"
spec = importlib.util.spec_from_file_location("qa34_build_contract", SCRIPT)
module = importlib.util.module_from_spec(spec)
assert spec and spec.loader
spec.loader.exec_module(module)


class GeneratorBomContractTest(unittest.TestCase):
    def valid_materials(self) -> dict[str, str]:
        coordinate = f"implementation platform('{module.CANONICAL_BOM}:$PlatformVersion')"
        return {
            "create-domain generator": coordinate,
            "domain repository exporter": coordinate,
            "domain jobpack generator": coordinate,
        }

    def test_all_three_generation_paths_require_canonical_bom(self):
        module.verify_generator_bom_contract(self.valid_materials())

    def test_missing_canonical_bom_is_rejected_for_each_generation_path(self):
        for path_name in self.valid_materials():
            with self.subTest(path=path_name):
                materials = self.valid_materials()
                materials[path_name] = "implementation 'com.cpf.core:cpf-core:1.0.0'"
                with self.assertRaisesRegex(SystemExit, path_name):
                    module.verify_generator_bom_contract(materials)

    def test_legacy_bom_is_rejected_for_each_generation_path(self):
        for path_name in self.valid_materials():
            with self.subTest(path=path_name):
                materials = self.valid_materials()
                materials[path_name] += f"\nimplementation platform('{module.LEGACY_BOM}:1.0.0')"
                with self.assertRaisesRegex(SystemExit, path_name):
                    module.verify_generator_bom_contract(materials)


if __name__ == "__main__":
    unittest.main()
