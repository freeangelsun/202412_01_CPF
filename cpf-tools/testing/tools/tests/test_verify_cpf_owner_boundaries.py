import importlib.util
import tempfile
import unittest
from pathlib import Path

SCRIPT = Path(__file__).resolve().parents[4] / "cpf-tools/verification/tools/verify-cpf-owner-boundaries.py"


def load():
    spec = importlib.util.spec_from_file_location('owner_gate', SCRIPT)
    module = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(module)
    return module


class T(unittest.TestCase):
    def fixture(self):
        temp = tempfile.TemporaryDirectory()
        root = Path(temp.name)
        modules = ('cpf-core', 'cpf-common', 'cpf-admin', 'cpf-backoffice/online', 'cpf-batch', 'cpf-gateway', 'cpf-starters')
        logical = {
            'cpf-core': ':framework:core', 'cpf-common': ':starters:common',
            'cpf-admin': ':apps:admin', 'cpf-backoffice/online': ':apps:backoffice',
            'cpf-batch': ':runtime:batch', 'cpf-gateway': ':runtime:gateway',
            'cpf-starters': ':starters:test-fixture',
        }
        lines = ["include " + ', '.join(f"'{logical[module]}'" for module in modules)]
        lines += [f"project('{logical[module]}').projectDir = file('{module}')" for module in modules]
        (root / 'settings.gradle').write_text('\n'.join(lines) + '\n', encoding='utf-8')
        (root / 'build.gradle').write_text('', encoding='utf-8')
        for module in modules:
            (root / module / 'src/main/java/x').mkdir(parents=True)
            (root / module / 'build.gradle').write_text('', encoding='utf-8')
        (root / 'cpf-core/src/main/java/x/A.java').write_text('package x;\n', encoding='utf-8')
        (root / 'cpf-gateway/src/main/java/com/cpf/gateway/internal').mkdir(parents=True, exist_ok=True)
        (root / 'cpf-gateway/src/main/java/com/cpf/gateway/internal/Route.java').write_text('package com.cpf.gateway.internal; public class Route {}\n', encoding='utf-8')
        (root / 'cpf-backoffice/online/src/main/java/com/cpf/backoffice/online/internal').mkdir(parents=True, exist_ok=True)
        (root / 'cpf-backoffice/online/src/main/java/com/cpf/backoffice/online/internal/Secret.java').write_text('package com.cpf.backoffice.online.internal; public class Secret {}\n', encoding='utf-8')
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
        (root / 'cpf-core/build.gradle').write_text("implementation project(':apps:admin')", encoding='utf-8')
        with self.assertRaises(Exception) as failure:
            load().verify(root)
        self.assertIn('cpf-core->cpf-admin', str(failure.exception))

    def test_cross_owner_bizadmin_internal_import_fails(self):
        temp, root = self.fixture()
        self.addCleanup(temp.cleanup)
        (root / 'cpf-admin/src/main/java/x/Admin.java').write_text(
            'package x;\nimport com.cpf.backoffice.online.internal.Secret;\n', encoding='utf-8'
        )
        with self.assertRaises(Exception) as failure:
            load().verify(root)
        self.assertIn('cross-module internal import', str(failure.exception))

    def test_fully_qualified_internal_reference_fails_without_import(self):
        temp, root = self.fixture()
        self.addCleanup(temp.cleanup)
        (root / 'cpf-common/src/main/java/x/Common.java').write_text(
            'package x; class Common { com.cpf.gateway.internal.Route value; }\n', encoding='utf-8'
        )
        with self.assertRaises(Exception) as failure:
            load().verify(root)
        self.assertIn('cross-module internal reference', str(failure.exception))

    def test_same_owner_internal_reference_is_allowed(self):
        temp, root = self.fixture()
        self.addCleanup(temp.cleanup)
        (root / 'cpf-admin/src/main/java/com/cpf/admin/internal').mkdir(parents=True, exist_ok=True)
        (root / 'cpf-admin/src/main/java/com/cpf/admin/internal/Local.java').write_text(
            'package com.cpf.admin.internal; public class Local {}\n', encoding='utf-8'
        )
        (root / 'cpf-admin/src/main/java/x/Admin.java').write_text(
            'package x;\nimport com.cpf.admin.internal.Local;\n', encoding='utf-8'
        )
        self.assertEqual('PASS', load().verify(root)['status'])


if __name__ == '__main__':
    unittest.main()
