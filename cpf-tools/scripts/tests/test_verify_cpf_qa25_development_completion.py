import unittest
from pathlib import Path
class T(unittest.TestCase):
 def test_wrapper_invokes_every_required_gate_and_runtime_harness(self):
  p=Path(__file__).parents[1]/'verify-cpf-qa25-development-completion.py';t=p.read_text(encoding='utf-8')
  for token in ('python_gate_tests','frontend_api_runtime','frontend_workflow_runtime','java21_controller','java21_network','java21_transaction','java21_persistence','java21_db_less','java21_runtime_command','java21_batch_abandon','java21_audit_multi_process','split_master','owner_boundaries','transaction_standard','db_less','operator_trust','network_policy','db_vendor','starter_catalog','traceability','evidence_integrity'):
   self.assertIn(token,t)
  self.assertIn("'evidence_integrity'",t)
if __name__=='__main__':unittest.main()
