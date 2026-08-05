from __future__ import annotations

import importlib.util
import json
import shutil
import subprocess
import sys
import tempfile
import unittest
from pathlib import Path

SCRIPT = Path(__file__).resolve().parents[1] / 'verify-cpf-runtime-alternatives.py'
PROFILE_REL = Path('cpf-tools/runtime-alternatives/cpf-runtime-alternative-profile.json')


def load_module():
    spec = importlib.util.spec_from_file_location('runtime_alt', SCRIPT)
    assert spec and spec.loader
    module = importlib.util.module_from_spec(spec)
    sys.modules[spec.name] = module
    spec.loader.exec_module(module)
    return module


class RuntimeAlternativesTest(unittest.TestCase):
    def fixture(self) -> Path:
        root = Path(tempfile.mkdtemp())
        source_profile = Path(__file__).resolve().parents[2] / 'runtime-alternatives/cpf-runtime-alternative-profile.json'
        profile = json.loads(source_profile.read_text(encoding='utf-8'))
        for values in profile['sourceGroups'].values():
            for rel in values:
                target = root / rel
                target.parent.mkdir(parents=True, exist_ok=True)
                if rel.endswith('generate-cpf-support-bundle.py'):
                    source = Path(__file__).resolve().parents[1] / 'generate-cpf-support-bundle.py'
                    shutil.copy2(source, target)
                else:
                    target.write_text('// fixture\n', encoding='utf-8')
        target_profile = root / PROFILE_REL
        target_profile.parent.mkdir(parents=True, exist_ok=True)
        target_profile.write_text(json.dumps(profile), encoding='utf-8')
        return root

    def run_script(self, root: Path):
        report = root / 'evidence/runtime-alternative.json'
        cp = subprocess.run(
            [sys.executable, str(SCRIPT), '--root', str(root), '--report', str(report)],
            text=True, capture_output=True, timeout=30,
        )
        return cp, report

    def test_positive_fixture_passes_all_named_checks(self):
        root = self.fixture()
        cp, report = self.run_script(root)
        self.assertEqual(0, cp.returncode, cp.stdout + cp.stderr)
        data = json.loads(report.read_text(encoding='utf-8'))
        self.assertEqual('PASS', data['status'])
        self.assertFalse(data['authoritativeRuntimeClaim'])
        self.assertEqual(9, len(data['checks']))
        self.assertTrue(all(row['status'] == 'PASS' for row in data['checks'].values()))

    def test_missing_product_source_fails_closed(self):
        root = self.fixture()
        (root / 'cpf-starters/integration/tcp/src/main/java/com/cpf/starter/integration/tcp/internal/CpfResilientTcpClient.java').unlink()
        cp, _ = self.run_script(root)
        self.assertNotEqual(0, cp.returncode)
        self.assertIn('product source groups incomplete', cp.stdout)

    def test_supply_chain_report_contains_cbom_and_provenance(self):
        module = load_module()
        from datetime import datetime, timezone
        result = module._supply_chain_models(datetime(2026, 8, 5, 14, 0, tzinfo=timezone.utc))
        self.assertEqual('CycloneDX', result['cbom']['bomFormat'])
        self.assertEqual('https://in-toto.io/Statement/v1', result['provenance']['_type'])
        self.assertEqual(result['provenanceSubjectSha256'], result['provenance']['subject'][0]['digest']['sha256'])

    def test_reliability_model_fences_and_reconciles(self):
        module = load_module()
        result = module._reliability_models()
        self.assertEqual('PASS', result['status'])
        self.assertEqual(1, result['sideEffectCount'])
        self.assertIn('RECONCILED_SUCCESS', result['audit'])

    def test_breaking_config_schema_is_rejected(self):
        module = load_module()
        old = {'properties': {'x': {'type': 'string'}}, 'required': ['x']}
        new = {'properties': {'x': {'type': 'integer'}}, 'required': ['x', 'y']}
        compatible, issues = module._schema_compatible(old, new)
        self.assertFalse(compatible)
        self.assertTrue(any('type changed' in item for item in issues))
        self.assertTrue(any('new required' in item for item in issues))

    def test_multi_process_kill_recovery(self):
        module = load_module()
        result = module._multi_process_kill_recovery()
        self.assertEqual('PASS', result['status'])
        self.assertEqual('SUCCEEDED', result['recoveredState'])
        self.assertEqual(1, result['effectCount'])
        self.assertTrue(result['duplicateEffectSuppressed'])
        self.assertEqual(17, result['loserExitCode'])

    def test_partial_io_executes_real_socket_pair(self):
        module = load_module()
        result = module._partial_io()
        self.assertEqual('PASS', result['status'])
        self.assertEqual('UNKNOWN', result['truncatedState'])


if __name__ == '__main__':
    unittest.main()
