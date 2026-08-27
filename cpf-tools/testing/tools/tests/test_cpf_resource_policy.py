import importlib.util
from pathlib import Path
import tempfile
import unittest

ROOT = Path(__file__).resolve().parents[4]
SCRIPT = ROOT / "cpf-tools/verification/verify-cpf-resource-policy.py"
SPEC = importlib.util.spec_from_file_location("cpf_resource_policy", SCRIPT)
MOD = importlib.util.module_from_spec(SPEC)
assert SPEC.loader is not None
SPEC.loader.exec_module(MOD)


class CpfResourcePolicyTest(unittest.TestCase):
    def test_repository_policy_passes(self):
        result = MOD.verify(ROOT)
        self.assertEqual("PASS", result["status"], result["errors"])
        self.assertEqual("250", result["local"]["heapStepMb"])
        self.assertEqual("1000", result["local"]["heapCeilingMb"])
        self.assertEqual("false", result["local"]["batchDefault"])
        self.assertEqual("true", result["local"]["singleWebDefault"])
        self.assertEqual("1000", result["local"]["frontendNodeMaxOldSpaceMb"])

    def test_memory_parser(self):
        self.assertEqual(250, MOD.memory_mb("250m"))
        self.assertEqual(1024, MOD.memory_mb("1g"))
        with self.assertRaises(ValueError):
            MOD.memory_mb("512")


if __name__ == "__main__":
    unittest.main()
