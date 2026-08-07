#!/usr/bin/env python3
from pathlib import Path
import csv,hashlib,json,re,sys
R=Path(sys.argv[1]).resolve() if len(sys.argv)>1 else Path(__file__).resolve().parents[3]
D=R/'cpf-docs/work/v9i/dev/r6s12'; errors=[]
required=['QA_REWORK_REQUEST.md','CHANGE_MANIFEST.csv','TEST_AND_EVIDENCE.md','OPEN_ISSUES.md','REQUIREMENT_STATUS.csv','FINDING_STATUS.csv','EVIDENCE_LEDGER.csv','CODEX_REVIEW_REQUEST.md','PACKAGE_MANIFEST.json','SHA256SUMS.txt','DELETE_MANIFEST.csv','HANDOVER.md','REVIEW.md','GARBAGE_SOURCE_REVIEW.md','OVERLAY_TREE_SHA256.txt']
for name in required:
 p=D/name
 if not p.is_file() or p.stat().st_size==0: errors.append(f'missing/empty required package file: {name}')
control=(D/'QA_REWORK_REQUEST.md').read_text(encoding='utf-8',errors='replace')
if len(re.findall(r'<!-- BEGIN DEVGPT RESULT QA-R5I-\d{3} -->',control))!=29: errors.append('control document finding result count != 29')
if len(re.findall(r'^\| FDEV-\d{3} \|',control,re.M))!=25: errors.append('control document requirement result count != 25')
with (D/'FINDING_STATUS.csv').open(encoding='utf-8-sig',newline='') as f:
 rows=list(csv.DictReader(f))
 if len(rows)!=29 or {r['finding_id'] for r in rows}!={f'QA-R5I-{i:03d}' for i in range(1,30)}: errors.append('finding ledger exact ID set mismatch')
with (D/'REQUIREMENT_STATUS.csv').open(encoding='utf-8-sig',newline='') as f:
 rows=list(csv.DictReader(f))
 if len(rows)!=25 or {r['requirement_id'] for r in rows}!={f'FDEV-{i:03d}' for i in range(1,26)}: errors.append('requirement ledger exact ID set mismatch')
with (D/'DELETE_MANIFEST.csv').open(encoding='utf-8-sig',newline='') as f:
 deletes=list(csv.DictReader(f))
 if any(r.get('action')=='DELETE_REQUESTED' for r in deletes): errors.append('unapproved DELETE_REQUESTED exists')
manifest=json.loads((D/'PACKAGE_MANIFEST.json').read_text(encoding='utf-8'))
listed={r['path']:r for r in manifest.get('files',[])}
for rel,row in listed.items():
 p=R/rel
 if not p.is_file(): errors.append(f'manifest missing file: {rel}'); continue
 actual=hashlib.sha256(p.read_bytes()).hexdigest()
 if actual!=row.get('sha256'): errors.append(f'manifest hash mismatch: {rel}')
# all non-metadata files must be listed
excluded={'cpf-docs/work/v9i/dev/r6s12/CHANGE_MANIFEST.csv','cpf-docs/work/v9i/dev/r6s12/PACKAGE_MANIFEST.json','cpf-docs/work/v9i/dev/r6s12/SHA256SUMS.txt','cpf-docs/work/v9i/dev/r6s12/OVERLAY_TREE_SHA256.txt'}
actual={p.relative_to(R).as_posix() for p in R.rglob('*') if p.is_file() and p.relative_to(R).as_posix() not in excluded}
if actual!=set(listed): errors.append(f'manifest path set mismatch missing={sorted(actual-set(listed))} orphan={sorted(set(listed)-actual)}')
sha_rows={}
for line in (D/'SHA256SUMS.txt').read_text(encoding='utf-8').splitlines():
 if not line.strip(): continue
 digest,rel=line.split('  ',1); sha_rows[rel]=digest
for rel,digest in sha_rows.items():
 p=R/rel
 if not p.is_file() or hashlib.sha256(p.read_bytes()).hexdigest()!=digest: errors.append(f'SHA256SUMS mismatch: {rel}')
expected_sums={p.relative_to(R).as_posix() for p in R.rglob('*') if p.is_file() and p != D/'SHA256SUMS.txt'}
if set(sha_rows)!=expected_sums: errors.append('SHA256SUMS path set mismatch')
svg=R/'cpf-docs/assets/manuals/cpf-document-quality-r9.svg'
if hashlib.sha256(svg.read_bytes()).hexdigest()!='2979b5f65e7b8ace8a735cd5eae501c6b60cc851be2f31fd441383e7a2d498d5': errors.append('protected SVG hash mismatch')
for e in errors: print('FAIL',e)
if errors: raise SystemExit(1)
print(f"PASS package findings=29 requirements=25 payload={len(listed)} sums={len(sha_rows)} deleteRequested=0")
