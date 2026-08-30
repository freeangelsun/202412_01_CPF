#!/usr/bin/env python3
import hashlib,json,re,sys
from pathlib import Path
from PIL import Image
H=Path(__file__).resolve().parents[1]; ROOT=H.parents[2]
review=Path(sys.argv[1]).resolve() if len(sys.argv)>1 else ROOT/'cpf-docs/deliverables/documentation/README_RENDER_REVIEW.json'
readme=ROOT/'README.md'
errs=[]
def sha(p):return hashlib.sha256(p.read_bytes()).hexdigest().upper()
if not review.is_file():
 print('README_RENDERED_BROCHURE_REVIEW=FAIL review missing '+str(review)); raise SystemExit(1)
try:d=json.loads(review.read_text(encoding='utf-8'))
except Exception as e: print('README_RENDERED_BROCHURE_REVIEW=FAIL invalid json '+str(e)); raise SystemExit(1)
if d.get('harnessVersion')!='2.15.4': errs.append('harnessVersion must be 2.15.4')
if d.get('readmeSha256','').upper()!=sha(readme): errs.append('README SHA stale')
if not str(d.get('reviewer','')).strip() or not str(d.get('reviewedAt','')).strip(): errs.append('reviewer/reviewedAt missing')
required_roles={'hero','architecture','invoke','tx','batch','development','capabilities','gateway','ops'}
# Preview HTML must be SHA-bound and portable. Absolute/temp file bases are non-reproducible evidence.
preview_ref=str(d.get('previewHtml','')).strip()
if not preview_ref: errs.append('previewHtml missing')
else:
 pp0=Path(preview_ref)
 if pp0.is_absolute(): errs.append('previewHtml path must be repository-relative')
 pp=ROOT/pp0
 if not pp.is_file(): errs.append('previewHtml missing '+preview_ref)
 else:
  if d.get('previewHtmlSha256','').upper()!=sha(pp): errs.append('previewHtml SHA stale')
  try: html=pp.read_text(encoding='utf-8')
  except Exception as e: errs.append('previewHtml unreadable '+str(e)); html=''
  if re.search(r'<base\s+href=[\"\'](?:file:///|/|[A-Za-z]:[\\/])',html,re.I) or '/mnt/data/' in html:
   errs.append('previewHtml contains absolute/temp base path')
surfaces=d.get('surfaces',[]); widths={int(x.get('width',0)) for x in surfaces if isinstance(x,dict)}
if widths!={900,1200,1440}: errs.append('exact 900/1200/1440 surfaces required')
for s in surfaces:
 w=int(s.get('width',0)); sp=Path(str(s.get('screenshot','')))
 if sp.is_absolute(): errs.append(f'{w}: screenshot path must be repository-relative')
 p=ROOT/sp
 if not p.is_file(): errs.append(f'{w}: screenshot missing'); continue
 try:
  with Image.open(p) as im:
   if im.width!=w: errs.append(f'{w}: screenshot width {im.width}')
   if im.height<=0: errs.append(f'{w}: screenshot height invalid {im.height}')
   im.verify()
  # Re-open and force pixel decode; verify() alone does not expose all decoder/runtime failures.
  with Image.open(p) as im2:
   im2.load()
 except Exception as e: errs.append(f'{w}: screenshot full-decode integrity failure {e}')
 if s.get('screenshotSha256','').upper()!=sha(p): errs.append(f'{w}: screenshot SHA stale')
 for k in ['scanPass','detailPass','firstViewportBrochure','sectionBoundaryClear','architectureExplanationUseful','naturalValueUseful']:
  if s.get(k) is not True: errs.append(f'{w}: {k} not PASS')
 if int(s.get('textWallCount',-1))!=0: errs.append(f'{w}: textWallCount={s.get("textWallCount")}')
 if int(s.get('cropOverflowCount',-1))!=0: errs.append(f'{w}: cropOverflowCount={s.get("cropOverflowCount")}')
 roles=set(s.get('visualRolesPresent',[]))
 if roles!=required_roles: errs.append(f'{w}: core visual roles mismatch missing={sorted(required_roles-roles)} extra={sorted(roles-required_roles)}')
 if len(s.get('observations',[]))<3: errs.append(f'{w}: observations must contain >=3 concrete observations')
if errs:
 print('README_RENDERED_BROCHURE_REVIEW=FAIL COUNT='+str(len(errs)))
 for e in errs: print('-',e)
 raise SystemExit(1)
print('README_RENDERED_BROCHURE_REVIEW=PASS')
print('README_FIRST_VIEWPORT_BROCHURE_PASS=PASS')
print('README_SECTION_BOUNDARY_PASS=PASS')
print('README_CORE_VISUAL_RETENTION_PASS=PASS')
