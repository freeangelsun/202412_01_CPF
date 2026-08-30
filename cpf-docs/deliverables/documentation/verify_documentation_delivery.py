#!/usr/bin/env python3
from pathlib import Path
import json,hashlib,subprocess,sys
ROOT=Path(__file__).resolve().parents[3]; H=ROOT/'cpf-docs/governance/documentation-harness'; D=ROOT/'cpf-docs/deliverables/documentation'
def run(args):
 r=subprocess.run(args,cwd=ROOT,text=True,capture_output=True); print(r.stdout,end=''); print(r.stderr,end='',file=sys.stderr)
 if r.returncode: raise SystemExit(r.returncode)
def sha(p): return hashlib.sha256(p.read_bytes()).hexdigest().upper()
V=H/'validators'
for s,args in [
 ('validate_harness.py',[]),('validate_source_alignment.py',[]),('validate_source_currentization.py',[]),('validate_quality_fixtures.py',[]),('validate_false_green_prevention.py',[]),('validate_readme.py',[ROOT/'README.md']),('validate_readme_product_completeness.py',[ROOT/'README.md']),('validate_readme_render_review.py',[]),('validate_docx_artifacts.py',[]),('validate_reader_task_coverage.py',[]),('validate_readability_actionability.py',[]),('validate_visual_assets.py',[]),('validate_visual_human_review.py',[]),('validate_user_finding_closure.py',[]),('validate_architecture_visual_semantics.py',[]),('validate_pdf_openability.py',[]),('validate_rendered_page_composition.py',[]),('validate_visual_comfort.py',[]),('validate_final_acceptance.py',[D/'FINAL_ACCEPTANCE.json'])]:
 run([sys.executable,str(V/s),*map(str,args)])
docx=list((ROOT/'cpf-docs/guides').glob('*.docx'))+list((ROOT/'cpf-docs/deliverables').glob('*.docx')); pdf=list((ROOT/'cpf-docs/guides').glob('*.pdf'))+list((ROOT/'cpf-docs/deliverables').glob('*.pdf'))
if len(docx)!=11 or len(pdf)!=11: raise SystemExit(f'artifact count mismatch DOCX={len(docx)} PDF={len(pdf)}')
si=json.loads((D/'SOURCE_IDENTITY.json').read_text(encoding='utf-8')); assert si.get('harnessVersion')=='2.15.4'; assert si.get('sourceZipSha256')=='6AEC7A50D69F140B30968EAD21B7242E1D2A6252446DAC0D5A27CC4C4566D7DC'
pm=json.loads((D/'PACKAGE_MANIFEST.json').read_text(encoding='utf-8')); assert pm.get('harnessVersion')=='2.15.4'; assert pm.get('fileCount')==len(pm.get('files',[]))
for x in pm.get('files',[]):
 p=ROOT/x['path']
 if not p.is_file() or p.stat().st_size!=x['sizeBytes'] or sha(p)!=x['sha256']: raise SystemExit('manifest mismatch '+x['path'])
for line in (D/'SHA256SUMS.txt').read_text(encoding='utf-8').splitlines():
 if not line.strip(): continue
 h,rr=line.split('  ',1); p=ROOT/rr
 if not p.is_file() or sha(p)!=h.upper(): raise SystemExit('checksum mismatch '+rr)
for raw in (D/'DELETE_MANIFEST.txt').read_text(encoding='utf-8').splitlines():
 s=raw.strip()
 if not s or s.startswith('#'): continue
 if '*' in s or '?' in s or Path(s).is_absolute() or '..' in Path(s).parts: raise SystemExit('unsafe delete '+s)
print('DOCUMENTATION_DELIVERY=PASS')
print(f'DOCX={len(docx)} PDF={len(pdf)} MANIFEST_FILES={len(pm.get("files",[]))} HARNESS=2.15.4')
print('SOURCE_FINGERPRINT='+si.get('sourceCurrentizationFingerprint',''))
