from __future__ import annotations
import importlib.util,sys,tempfile,unittest
from pathlib import Path
ROOT=Path(__file__).resolve().parents[3]
P=ROOT/'cpf-tools/scripts/verify-cpf-seed-dialect.py'
S=importlib.util.spec_from_file_location('seed_dialect',P);assert S and S.loader
M=importlib.util.module_from_spec(S);sys.modules[S.name]=M;S.loader.exec_module(M)
class SeedDialectLintTest(unittest.TestCase):
 def test_existing_official_vendor_product_and_test_bundles_pass(self):
  for v in M.OFFICIAL:
   paths=sorted((ROOT/f'cpf-tools/db/vendor/{v}').rglob('*.sql'))
   self.assertTrue(paths)
   for path in paths:
    self.assertEqual([],M.lint(v,path.read_text(encoding='utf-8-sig')),f'{v}:{path}')
 def test_cross_vendor_tokens_fail_closed(self):
  self.assertTrue(M.lint('mariadb',"INSERT INTO t VALUES (1) ON CONFLICT DO NOTHING;"))
  self.assertTrue(M.lint('postgresql',"INSERT IGNORE INTO t VALUES (1);"))
  self.assertTrue(M.lint('oracle',"INSERT INTO `t` VALUES (1);"))

 def test_mariadb_backslash_escaped_quote_is_balanced(self):
  sql = "SELECT TRIM(BOTH '\\'' FROM column_default);"
  self.assertEqual([], M.lint('mariadb', sql))

 def test_quote_markers_inside_comments_do_not_unbalance(self):
  sql = "-- unmatched ' in comment\nSELECT 'ok'; /* another ' comment */"
  self.assertEqual([], M.lint('mariadb', sql))

 def test_unbalanced_quote_and_missing_terminator_fail(self):
  self.assertTrue(M.lint('mariadb',"INSERT INTO t VALUES ('x)"))
if __name__=='__main__':unittest.main()
