#!/usr/bin/env python3
from pathlib import Path
import json,hashlib,subprocess,sys
ROOT=Path(__file__).resolve().parents[3]
H=ROOT/'cpf-docs/governance/documentation-harness'
D=ROOT/'cpf-docs/deliverables/documentation'
def run(args):
 r=subprocess.run(args,cwd=ROOT,text=True,capture_output=True); print(r.stdout,end=''); print(r.stderr,end='',file=sys.stderr);
 if r.returncode: raise SystemExit(r.returncode)
def sha(p): return hashlib.sha256(p.read_bytes()).hexdigest().upper()
run([sys.executable,str(H/'validators/validate_harness.py')])
run([sys.executable,str(H/'validators/validate_quality_fixtures.py')])
run([sys.executable,str(H/'validators/validate_readme.py'),str(ROOT/'README.md')])
run([sys.executable,str(H/'validators/validate_docx_artifacts.py')])
run([sys.executable,str(H/'validators/validate_reader_task_coverage.py')])
run([sys.executable,str(H/'validators/validate_visual_assets.py')])
run([sys.executable,str(H/'validators/validate_final_acceptance.py'),str(D/'FINAL_ACCEPTANCE.json')])
docx=list((ROOT/'cpf-docs/guides').glob('*.docx'))+list((ROOT/'cpf-docs/deliverables').glob('*.docx'))
pdf=list((ROOT/'cpf-docs/guides').glob('*.pdf'))+list((ROOT/'cpf-docs/deliverables').glob('*.pdf'))
if len(docx)!=11 or len(pdf)!=11: raise SystemExit(f'artifact count mismatch DOCX={len(docx)} PDF={len(pdf)}')
pm=json.loads((D/'PACKAGE_MANIFEST.json').read_text(encoding='utf-8'))
for x in pm.get('files',[]):
 p=ROOT/x['path'];
 if not p.is_file(): raise SystemExit('manifest missing '+x['path'])
 if p.stat().st_size!=x['sizeBytes'] or sha(p)!=x['sha256']: raise SystemExit('manifest mismatch '+x['path'])
for line in (D/'SHA256SUMS.txt').read_text(encoding='utf-8').splitlines():
 if not line.strip(): continue
 h,rel=line.split('  ',1); p=ROOT/rel
 if not p.is_file() or sha(p)!=h.upper(): raise SystemExit('checksum mismatch '+rel)
for rel in ['APPLY.ps1','DELETE_ONLY.ps1']:
 p=D/rel; txt=p.read_text(encoding='utf-8-sig')
 if "TrimEnd('" in txt: raise SystemExit('Windows separator literal in root prefix '+rel)
 if '[IO.Path]::DirectorySeparatorChar' not in txt or '$rootPrefix=$root.TrimEnd($sep)+$sep' not in txt or 'StartsWith($rootPrefix' not in txt:
  raise SystemExit('Windows root containment guard missing '+rel)
si=json.loads((D/'SOURCE_IDENTITY.json').read_text(encoding='utf-8'))
if si.get('harnessVersion')!='2.9.0': raise SystemExit('source identity harness mismatch')
# Current-only delivery wrapper hygiene: no stale versioned temp prefix or escaped-newline command text.
apply_txt=(D/'APPLY.ps1').read_text(encoding='utf-8-sig')
if 'cpf-doc-280-' in apply_txt or 'cpf-doc-290-' not in apply_txt: raise SystemExit('APPLY temp prefix not current 2.9.0')
low=(D/'LOW_COST_VERIFY_COMMAND.txt').read_text(encoding='utf-8')
if '\\n' in low or not low.endswith('\n'): raise SystemExit('LOW_COST_VERIFY_COMMAND newline encoding invalid')
print('DOCUMENTATION_DELIVERY=PASS')
print(f'DOCX={len(docx)} PDF={len(pdf)} MANIFEST_FILES={len(pm.get("files",[]))} HARNESS=2.9.0')
