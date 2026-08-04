import importlib.util
import tempfile
import unittest
from pathlib import Path

SCRIPT = Path(__file__).resolve().parents[1] / 'verify-cpf-owner-boundaries.py'


def load():
    spec = importlib.util.spec_from_file_location('owner_gate', SCRIPT)
    module = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(module)
    return module


class T(unittest.TestCase):
    def fixture(self):
        temp = tempfile.TemporaryDirectory()
        root = Path(temp.name)
        modules = ('cpf-core', 'cpf-common', 'cpf-admin', 'cpf-biz-admin', 'cpf-batch', 'cpf-gateway', 'cpf-starters')
        (root / 'settings.gradle').write_text(
            "include " + ', '.join(f"':{module}'" for module in modules), encoding='utf-8'
        )
        (root / 'build.gradle').write_text('', encoding='utf-8')
        for module in modules:
            (root / module / 'src/main/java/x').mkdir(parents=True)
            (root / module / 'build.gradle').write_text('', encoding='utf-8')
        (root / 'cpf-core/src/main/java/x/A.java').write_text('package x;\n', encoding='utf-8')
        return temp, root

    def test_pass(self):
        temp, root = self.fixture()
        self.addCleanup(temp.cleanup)
        self.assertEqual('PASS', load().verify(root)['status'])

    def test_sparse_snapshot_fails_closed(self):
        with tempfile.TemporaryDirectory() as directory:
            self.assertRaises(Exception, load().verify, Path(directory))

    def test_core_to_common_reverse_dependency_fails(self):
        temp, root = self.fixture()
        self.addCleanup(temp.cleanup)
        (root / 'cpf-core/build.gradle').write_text("implementation project(':cpf-common')", encoding='utf-8')
        with self.assertRaises(Exception) as failure:
            load().verify(root)
        self.assertIn('cpf-core->cpf-common', str(failure.exception))

    def test_cross_owner_bizadmin_internal_import_fails(self):
        temp, root = self.fixture()
        self.addCleanup(temp.cleanup)
        (root / 'cpf-admin/src/main/java/x/Admin.java').write_text(
            'package x;\nimport com.cpf.bizadmin.internal.Secret;\n', encoding='utf-8'
        )
        with self.assertRaises(Exception) as failure:
            load().verify(root)
        self.assertIn('expectedOwner=cpf-biz-admin', str(failure.exception))

    def test_fully_qualified_internal_reference_fails_without_import(self):
        temp, root = self.fixture()
        self.addCleanup(temp.cleanup)
        (root / 'cpf-common/src/main/java/x/Common.java').write_text(
            'package x; class Common { com.cpf.gateway.internal.Route value; }\n', encoding='utf-8'
        )
        with self.assertRaises(Exception) as failure:
            load().verify(root)
        self.assertIn('expectedOwner=cpf-gateway', str(failure.exception))

    def test_same_owner_internal_reference_is_allowed(self):
        temp, root = self.fixture()
        self.addCleanup(temp.cleanup)
        (root / 'cpf-admin/src/main/java/x/Admin.java').write_text(
            'package x;\nimport com.cpf.admin.internal.Local;\n', encoding='utf-8'
        )
        self.assertEqual('PASS', load().verify(root)['status'])


if __name__ == '__main__':
    unittest.main()
