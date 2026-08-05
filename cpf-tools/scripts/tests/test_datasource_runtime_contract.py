import copy,importlib.util,json,unittest
from pathlib import Path
ROOT=Path(__file__).resolve().parents[3]
POLICY=json.loads((ROOT/'cpf-tools/db/cpf-datasource-runtime-policy.json').read_text(encoding='utf-8'))
spec=importlib.util.spec_from_file_location('ds',ROOT/'cpf-tools/scripts/verify-cpf-datasource-runtime-evidence.py');mod=importlib.util.module_from_spec(spec);spec.loader.exec_module(mod)
H='a'*64

def fixture():
 return {'vendor':'postgresql','sourceSha':'b'*40,'operationId':'op-1','operator':'maker','approvedBy':'checker','reason':'scheduled datasource runtime validation','sanitized':True,'status':'PASS',
 'routing':{'writeTarget':'PRIMARY','readTarget':'REPLICA','readOnly':True,'consistency':'BOUNDED_STALENESS','replicaLagMs':100,'decisionAuditId':'route-1'},
 'multiDataSource':{'owner':'cpf-common','resourceCount':1,'writeOperation':False,'crossOwnerWrite':False},
 'connectionPool':{'maxPoolSize':20,'instanceCount':2,'databaseConnectionBudget':100,'reservedConnections':20,'connectionTimeoutMs':3000,'validationTimeoutMs':1000},
 'transaction':{'isolation':'READ_COMMITTED','deadlockDetected':False,'attemptCount':1,'commitOutcome':'COMMITTED','retried':False},
 'timeouts':{'queryTimeoutMs':10000,'lockTimeoutMs':1000},
 'slowQuery':{'durationMs':1200,'alertState':'WARN','planSha256':H,'bindValuesSanitized':True},
 'capacityForecast':{'observationDays':30,'forecastHorizonDays':90,'headroomPercent':30,'growthRatePerDay':1.2,'estimatedExhaustionDate':'2027-01-01','state':'WATCH'}}
class Test(unittest.TestCase):
 def test_positive_contract(self):self.assertEqual('PASS',mod.evaluate(POLICY,fixture())['status'])
 def test_strong_read_cannot_use_replica(self):
  e=fixture();e['routing']['consistency']='STRONG';self.assertEqual('FAIL',mod.evaluate(POLICY,e)['status'])
 def test_lagged_replica_falls_back_primary(self):
  e=fixture();e['routing']['replicaLagMs']=6000;self.assertEqual('FAIL',mod.evaluate(POLICY,e)['status']);e['routing']['readTarget']='PRIMARY';self.assertEqual('PASS',mod.evaluate(POLICY,e)['status'])
 def test_pool_budget_fails_closed(self):
  e=fixture();e['connectionPool']['maxPoolSize']=60;self.assertEqual('FAIL',mod.evaluate(POLICY,e)['status'])
 def test_multi_resource_write_requires_compensation(self):
  e=fixture();e['multiDataSource'].update(resourceCount=2,writeOperation=True);self.assertEqual('FAIL',mod.evaluate(POLICY,e)['status']);e['multiDataSource']['compensationPlan']='outbox reconciliation';self.assertEqual('PASS',mod.evaluate(POLICY,e)['status'])
 def test_deadlock_retry_requires_idempotency_and_bound(self):
  e=fixture();e['transaction'].update(deadlockDetected=True,attemptCount=4,retried=True);self.assertEqual('FAIL',mod.evaluate(POLICY,e)['status']);e['transaction'].update(attemptCount=2,idempotencyKey='idem-1');self.assertEqual('PASS',mod.evaluate(POLICY,e)['status'])
 def test_unknown_commit_is_not_retried(self):
  e=fixture();e['transaction'].update(deadlockDetected=True,attemptCount=2,idempotencyKey='idem',commitOutcome='UNKNOWN',retried=True);self.assertEqual('FAIL',mod.evaluate(POLICY,e)['status'])
 def test_timeouts_are_bounded(self):
  e=fixture();e['timeouts']['queryTimeoutMs']=0;self.assertEqual('FAIL',mod.evaluate(POLICY,e)['status'])
 def test_slow_query_requires_alert_and_plan(self):
  e=fixture();e['slowQuery'].update(alertState='NORMAL',planSha256='');self.assertEqual('FAIL',mod.evaluate(POLICY,e)['status'])
 def test_capacity_requires_window_headroom_and_exhaustion(self):
  e=fixture();e['capacityForecast'].update(observationDays=2,headroomPercent=5,estimatedExhaustionDate='');self.assertEqual('FAIL',mod.evaluate(POLICY,e)['status'])
 def test_consumer_has_sod_hash_and_vendor_guards(self):
  t=(ROOT/'cpf-tools/scripts/invoke-cpf-datasource-runtime-gate.ps1').read_text(encoding='utf-8')
  for token in ('ExpectedEvidenceSha256','$Operator -eq $ApprovedBy','ConfirmSanitizedEvidence','Evidence vendor mismatch','$LASTEXITCODE -ne 0'):self.assertIn(token,t)
if __name__=='__main__':unittest.main()
