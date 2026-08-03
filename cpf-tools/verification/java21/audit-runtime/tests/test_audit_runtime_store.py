import json, subprocess, tempfile, unittest
from pathlib import Path
class AuditRuntimeHarnessTest(unittest.TestCase):
 def test_multi_process_kill_restart(self):
  here=Path(__file__).resolve().parents[1]
  with tempfile.TemporaryDirectory() as td:
   p=subprocess.run(['python',str(here/'run-audit-runtime-harness.py'),'--work-dir',td,'--source-head','cb305fc5363263c9607e990ba640233c28668f01'],text=True,capture_output=True)
   self.assertEqual(0,p.returncode,p.stdout+p.stderr)
   r=json.loads((Path(td)/'result.json').read_text())
   self.assertEqual(220,r['record_count']); self.assertGreater(r['count_after_kill'],r['count_before_kill']); self.assertEqual([],r['secret_leaks'])
if __name__=='__main__': unittest.main()
