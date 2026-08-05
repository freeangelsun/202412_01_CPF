import copy,hashlib,importlib.util,json,tempfile,unittest
from pathlib import Path
ROOT=Path(__file__).resolve().parents[3]
POLICY=json.loads((ROOT/'cpf-tools/db/cpf-data-observability-policy.json').read_text(encoding='utf-8'))
spec=importlib.util.spec_from_file_location('obs',ROOT/'cpf-tools/scripts/verify-cpf-data-observability-evidence.py');mod=importlib.util.module_from_spec(spec);spec.loader.exec_module(mod)
H='a'*64

def fixture():
 return {'vendor':'mariadb','sourceSha':'b'*40,'operationId':'op-1','operator':'maker','approvedBy':'checker','reason':'scheduled data contract validation','sanitized':True,'state':'PASS',
 'lineage':{'nodes':[{'nodeId':'n1','owner':'cpf-common','logicalDatabase':'cpfDB','objectType':'TABLE','objectName':'source_table','schemaHash':H},{'nodeId':'n2','owner':'cpf-common','logicalDatabase':'cpfDB','objectType':'TABLE','objectName':'target_table','schemaHash':H}], 'edges':[{'edgeId':'e1','sourceNodeId':'n1','targetNodeId':'n2','operation':'TRANSFORM','mappingHash':H}]},
 'quality':{'rules':[{'ruleId':'r1','ruleVersion':1,'owner':'cpf-common','queryId':'q1','severity':'ERROR','thresholdType':'MAX_INVALID_COUNT','thresholdValue':0}], 'results':[{'ruleId':'r1','status':'PASS','invalidCount':0,'totalCount':100}]},
 'reconciliation':{'comparisons':[{'comparisonId':'c1','leftCount':100,'rightCount':100,'mismatchCount':0,'leftHash':H,'rightHash':H}]}}
class Test(unittest.TestCase):
 def test_positive_contract(self):self.assertEqual('PASS',mod.evaluate(POLICY,fixture())['status'])
 def test_lineage_unknown_node_fails(self):
  e=fixture();e['lineage']['edges'][0]['targetNodeId']='missing';self.assertEqual('FAIL',mod.evaluate(POLICY,e)['status'])
 def test_quality_failure_fails_closed(self):
  e=fixture();e['quality']['results'][0]['status']='FAIL';self.assertEqual('FAIL',mod.evaluate(POLICY,e)['status'])
 def test_reconciliation_mismatch_fails(self):
  e=fixture();e['reconciliation']['comparisons'][0]['mismatchCount']=1;self.assertEqual('FAIL',mod.evaluate(POLICY,e)['status'])
 def test_unknown_requires_reconcile(self):
  e=fixture();e['state']='UNKNOWN';self.assertEqual('FAIL',mod.evaluate(POLICY,e)['status']);e['reconcilePlan']='re-run read-only checks';self.assertEqual('PASS',mod.evaluate(POLICY,e)['status'])
 def test_consumer_has_sod_hash_and_vendor_guards(self):
  t=(ROOT/'cpf-tools/scripts/invoke-cpf-data-observability-gate.ps1').read_text(encoding='utf-8')
  for token in ('ExpectedEvidenceSha256','$Operator -eq $ApprovedBy','ConfirmSanitizedEvidence','Evidence vendor mismatch','$LASTEXITCODE -ne 0'):self.assertIn(token,t)
if __name__=='__main__':unittest.main()
