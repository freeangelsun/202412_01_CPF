from __future__ import annotations
import csv,hashlib,importlib.util,json,tempfile,unittest
from pathlib import Path
SCRIPT=Path(__file__).resolve().parents[4] / "cpf-tools/verification/tools/verify-cpf-evidence-semantics.py";spec=importlib.util.spec_from_file_location('evidence',SCRIPT);module=importlib.util.module_from_spec(spec);assert spec and spec.loader;spec.loader.exec_module(module)
SHA='a'*40

def document(requirements=('R1',)):
 return {'schemaVersion':1,'evidenceId':'E1','evidenceType':'execution','sourceSha':SHA,'resultSha':SHA,'command':'test','startedAt':'2026-08-01T00:00:00Z','endedAt':'2026-08-01T00:01:00Z','exitCode':0,'sanitized':True,'requirements':list(requirements),'scenarios':[{'scenarioId':'S1','precondition':'ready','action':'run','expectedResult':'pass','actualResult':'pass'}],'assertions':[{'name':'exit','passed':True}],'artifacts':[{'path':'x.txt','sha256':hashlib.sha256(b'x').hexdigest()}]}
class EvidenceSemanticsTest(unittest.TestCase):
 def test_valid_execution(self):self.assertEqual({'R1'},module.validate_document(Path('x'),document(),SHA))
 def test_bulk_ids_rejected(self):
  with self.assertRaises(module.EvidenceError):module.validate_document(Path('x'),document(tuple(f'R{x}' for x in range(6))),SHA)
 def test_failed_assertion_rejected(self):
  value=document();value['assertions'][0]['passed']=False
  with self.assertRaises(module.EvidenceError):module.validate_document(Path('x'),value,SHA)
 def test_sha_mismatch_rejected(self):
  value=document();value['resultSha']='b'*40
  with self.assertRaises(module.EvidenceError):module.validate_document(Path('x'),value,SHA)
 def test_verified_row_requires_direct_evidence(self):
  with tempfile.TemporaryDirectory() as directory:
   root=Path(directory);(root/'evidence').mkdir();(root/'evidence/e.json').write_text(json.dumps(document(('OTHER',))), encoding="utf-8")
   matrix=root/'matrix.csv'
   with matrix.open('w',newline='',encoding='utf-8') as handle:
    writer=csv.DictWriter(handle,fieldnames=['requirement_id','verification_status','evidence_paths']);writer.writeheader();writer.writerow({'requirement_id':'R1','verification_status':'완료','evidence_paths':'evidence/e.json'})
   with self.assertRaises(module.EvidenceError):module.validate_matrix(root,matrix,SHA)
if __name__=='__main__':unittest.main()
