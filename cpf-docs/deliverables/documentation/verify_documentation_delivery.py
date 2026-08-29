#!/usr/bin/env python3
from pathlib import Path
import json,hashlib,subprocess,sys
ROOT=Path(__file__).resolve().parents[3]
H=ROOT/'cpf-docs/governance/documentation-harness'
D=ROOT/'cpf-docs/deliverables/documentation'
def run(args):
 r=subprocess.run(args,cwd=ROOT,text=True,capture_output=True); print(r.stdout,end=''); print(r.stderr,end='',file=sys.stderr)
 if r.returncode: raise SystemExit(r.returncode)
def sha(p): return hashlib.sha256(p.read_bytes()).hexdigest().upper()
run([sys.executable,str(H/'validators/validate_harness.py')])
run([sys.executable,str(H/'validators/validate_quality_fixtures.py')])
run([sys.executable,str(H/'validators/validate_source_alignment.py')])
run([sys.executable,str(H/'validators/validate_source_currentization.py'),str(D/'SOURCE_CURRENTIZATION_PREAUTHORING.json'),str(D/'SOURCE_CURRENTIZATION_FINALVALIDATION.json')])
run([sys.executable,str(H/'validators/validate_readme.py'),str(ROOT/'README.md')])
run([sys.executable,str(H/'validators/validate_docx_artifacts.py')])
run([sys.executable,str(H/'validators/validate_reader_task_coverage.py')])
run([sys.executable,str(H/'validators/validate_readability_actionability.py')])
run([sys.executable,str(H/'validators/validate_visual_assets.py')])
run([sys.executable,str(H/'validators/validate_visual_comfort.py')])
run([sys.executable,str(H/'validators/validate_rendered_page_composition.py')])
run([sys.executable,str(H/'validators/validate_final_acceptance.py'),str(D/'FINAL_ACCEPTANCE.json')])
docx=list((ROOT/'cpf-docs/guides').glob('*.docx'))+list((ROOT/'cpf-docs/deliverables').glob('*.docx'))
pdf=list((ROOT/'cpf-docs/guides').glob('*.pdf'))+list((ROOT/'cpf-docs/deliverables').glob('*.pdf'))
if len(docx)!=11 or len(pdf)!=11: raise SystemExit(f'artifact count mismatch DOCX={len(docx)} PDF={len(pdf)}')
si=json.loads((D/'SOURCE_IDENTITY.json').read_text(encoding='utf-8'))
if si.get('harnessVersion')!='2.12.0': raise SystemExit('source identity harness mismatch')
if si.get('gitExactSha')!='UNAVAILABLE_IN_SUPPLIED_ZIP': raise SystemExit('unexpected git exact SHA state')
pm=json.loads((D/'PACKAGE_MANIFEST.json').read_text(encoding='utf-8'))
if pm.get('harnessVersion')!='2.12.0': raise SystemExit('package manifest harness mismatch')
if pm.get('fileCount')!=len(pm.get('files',[])): raise SystemExit('package manifest fileCount mismatch')
for x in pm.get('files',[]):
 p=ROOT/x['path']
 if not p.is_file(): raise SystemExit('manifest missing '+x['path'])
 if p.stat().st_size!=x['sizeBytes'] or sha(p)!=x['sha256']: raise SystemExit('manifest mismatch '+x['path'])
for line in (D/'SHA256SUMS.txt').read_text(encoding='utf-8').splitlines():
 if not line.strip(): continue
 h,rel=line.split('  ',1); p=ROOT/rel
 if not p.is_file() or sha(p)!=h.upper(): raise SystemExit('checksum mismatch '+rel)
# Delete manifest exact-list safety and TXT/JSON parity.
txt=[]
for raw in (D/'DELETE_MANIFEST.txt').read_text(encoding='utf-8').splitlines():
 s=raw.strip()
 if not s or s.startswith('#'): continue
 if '*' in s or '?' in s or Path(s).is_absolute() or '..' in Path(s).parts: raise SystemExit('unsafe delete '+s)
 if s.rstrip('/') in {'cpf-docs/governance/documentation-harness','cpf-docs/guides','cpf-docs/deliverables/documentation'}: raise SystemExit('protected delete '+s)
 txt.append(s)
hj=json.loads((H/'DELETE_MANIFEST.json').read_text(encoding='utf-8'))
if hj.get('paths')!=txt or hj.get('delete')!=txt: raise SystemExit('delete manifest parity mismatch')
for rel in ['APPLY.ps1','DELETE_ONLY.ps1']:
 p=D/rel; t=p.read_text(encoding='utf-8-sig')
 if "TrimEnd('" in t: raise SystemExit('Windows separator literal in root prefix '+rel)
 if '[IO.Path]::DirectorySeparatorChar' not in t or '$rootPrefix=$root.TrimEnd($sep)+$sep' not in t or 'StartsWith($rootPrefix' not in t: raise SystemExit('Windows root containment guard missing '+rel)
print('DOCUMENTATION_DELIVERY=PASS')
print(f'DOCX={len(docx)} PDF={len(pdf)} MANIFEST_FILES={len(pm.get("files",[]))} HARNESS=2.12.0')
print('SOURCE_FINGERPRINT='+si.get('sourceCurrentizationFingerprint',''))
print('GIT_EXACT_SHA='+si.get('gitExactSha',''))
