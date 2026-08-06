from pathlib import Path
import csv,json,hashlib,sys
root=Path(sys.argv[1]).resolve(); session=root/Path(sys.argv[2])
def sha(p):
 h=hashlib.sha256();
 with p.open('rb') as f:
  for b in iter(lambda:f.read(1048576),b''): h.update(b)
 return h.hexdigest()
def rows(p):
 with p.open(encoding='utf-8-sig',newline='') as f:return list(csv.DictReader(f))
def parts(index):
 out=[]
 for x in rows(index): out.extend(rows(session/x['part_path']))
 return out
checks={}; errors=[]
status=parts(session/'results/REQUIREMENT_STATUS_INDEX.csv'); checks['status_rows']=len(status); checks['duplicate_primary']=len(status)-len({(r['entity_type'],r['exact_id']) for r in status})
prov=parts(session/'results/PROVENANCE_INDEX.csv'); provkeys={(r['entity_type'],r['exact_id']) for r in prov}
files=parts(session/'results/FILE_CATALOG_INDEX.csv'); filekeys={(r['entity_type'],r['exact_id']) for r in files}
ev=parts(session/'results/EVIDENCE_CATALOG_INDEX.csv'); evmap={r['evidence_id']:r for r in ev}
selfrows=parts(session/'results/SELF_REVIEW_CATALOG_INDEX.csv'); selfkeys={r['self_review_id'] for r in selfrows}
execrows=rows(session/'results/TEST_EXECUTION_LEDGER.csv'); execids={r['execution_id'] for r in execrows}
reqs=rows(session/'results/INTEGRATION_REQUEST_UNION.csv'); reqids={r['request_id'] for r in reqs}
reqlinks=parts(session/'results/INTEGRATION_REQUEST_UNION_INDEX.csv'); linked_req={r['request_id'] for r in reqlinks}
for r in status:
 k=(r['entity_type'],r['exact_id'])
 if k not in provkeys: errors.append('missing provenance '+str(k))
 if k not in filekeys: errors.append('missing file link '+str(k))
 if r['evidence_ref'] not in evmap: errors.append('missing evidence ref '+r['evidence_ref'])
 if r['self_review_ref'] not in selfkeys: errors.append('missing self review '+r['self_review_ref'])
 for e in filter(None,r['execution_ref'].split('|')):
  if e not in execids: errors.append('missing execution '+e)
 for q in filter(None,r['related_request_ids'].split('|')):
  if q not in reqids: errors.append('missing request '+q)
hash_cache={}
for eid,r in evmap.items():
 p=session/r['evidence_path']
 if not p.is_file(): errors.append('missing evidence file '+str(p))
 else:
  key=str(p)
  actual=hash_cache.get(key)
  if actual is None: actual=hash_cache.setdefault(key,sha(p))
  if actual!=r['sha256']: errors.append('evidence hash '+eid)
for q in reqids:
 if q not in linked_req and not next((r for r in reqs if r['request_id']==q and r.get('exception_reason')),None): errors.append('orphan request '+q)
for idx in session.glob('results/*INDEX.csv'):
 for x in rows(idx):
  p=session/x['part_path']
  if not p.is_file(): errors.append('missing part '+str(p)); continue
  n=max(0,sum(1 for _ in p.open(encoding='utf-8-sig'))-1)
  if n!=int(x['row_count']): errors.append('row count '+str(p))
  if p.stat().st_size!=int(x['file_size_bytes']): errors.append('size '+str(p))
  if sha(p)!=x['sha256']: errors.append('part hash '+str(p))
  if n>20000 or p.stat().st_size>25*1024*1024: errors.append('part limit '+str(p))
for p in session.rglob('*.md'):
 if p.stat().st_size>10*1024*1024: errors.append('markdown limit '+str(p))
status_exec_refs={e for r in status for e in r['execution_ref'].split('|') if e}
scoped_exec_refs={r['execution_id'] for r in execrows if r.get('related_exact_ids')}
request_evidence_refs={r.get('evidence_ref','') for r in reqs if r.get('evidence_ref')}
orph_ev=set(evmap)-{r['evidence_ref'] for r in status} - {r['evidence_id'] for r in execrows if r.get('evidence_id')} - request_evidence_refs
orph_ex=execids-status_exec_refs-scoped_exec_refs
orph_req=reqids-linked_req
if orph_ev: errors.append('orphan evidence '+','.join(sorted(orph_ev)))
if orph_ex: errors.append('orphan execution '+','.join(sorted(orph_ex)))
if orph_req: errors.append('orphan request '+','.join(sorted(orph_req)))
checks.update({'provenance_rows':len(prov),'file_link_rows':len(files),'evidence_rows':len(ev),'self_review_rows':len(selfrows),'execution_rows':len(execrows),'request_rows':len(reqs),'request_link_rows':len(reqlinks),'orphan_evidence':len(orph_ev),'orphan_execution':len(orph_ex),'orphan_request':len(orph_req),'hash_mismatch':sum(1 for x in errors if 'hash' in x),'errors':errors[:100],'pass':not errors})
print(json.dumps(checks,ensure_ascii=False,indent=2));sys.exit(0 if not errors else 1)
