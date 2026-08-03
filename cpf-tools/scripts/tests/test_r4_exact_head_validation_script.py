import unittest
from pathlib import Path
ROOT=Path(__file__).parents[1]
class ExactHeadValidationScriptTest(unittest.TestCase):
 def test_wrapper_is_exact_head_clean_and_fail_closed(self):
  text=(ROOT/'run-cpf-r4-exact-head-validation.ps1').read_text(encoding='utf-8')
  for token in ('ExpectedHead','git rev-parse HEAD','git status --porcelain','git diff --check','Java 25 이상 필요','verify-cpf-development-evidence-integrity.py','verify-cpf-owner-boundaries.py','verify-cpf-starter-catalog-truth.py','JAVA25_GRADLE','postgresql','oracle','mariadb','FreshInstall','Upgrade','RollbackReapply','ADM_BROWSER_E2E','BZA_BROWSER_E2E','run-adm-audit-multi-instance.ps1'):
   self.assertIn(token,text)
  self.assertIn("$ErrorActionPreference='Stop'",text)
  self.assertIn('$javaMajor=[int]$Matches[1]',text)
  self.assertNotIn('$javaMajor=$javaMajor',text)
  self.assertNotIn('exit 0',text.lower())
if __name__=='__main__': unittest.main()
