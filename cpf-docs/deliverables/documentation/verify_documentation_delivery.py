#!/usr/bin/env python3
from pathlib import Path
import json,re,hashlib,sys,zipfile
ROOT=Path(__file__).resolve().parents[3]
H=ROOT/'cpf-docs/governance/documentation-harness'
errs=[]
def need(rel):
 p=ROOT/rel
 if not p.is_file(): errs.append('MISSING '+rel)
required=['README.md',
'cpf-docs/guides/02_프레임워크_개발자_가이드.docx','cpf-docs/guides/02_프레임워크_개발자_가이드.pdf',
'cpf-docs/guides/03_배치_개발자_가이드.docx','cpf-docs/guides/03_배치_개발자_가이드.pdf',
'cpf-docs/guides/04_운영자_매뉴얼.docx','cpf-docs/guides/04_운영자_매뉴얼.pdf',
'cpf-docs/guides/05_배치_운영_가이드.docx','cpf-docs/guides/05_배치_운영_가이드.pdf',
'cpf-docs/guides/06_Gateway_개발_사용_가이드.docx','cpf-docs/guides/06_Gateway_개발_사용_가이드.pdf',
'cpf-docs/guides/07_Specification_기술_명세.docx','cpf-docs/guides/07_Specification_기술_명세.pdf',
'cpf-docs/deliverables/아키텍처설계서.docx','cpf-docs/deliverables/아키텍처설계서.pdf',
'cpf-docs/deliverables/기술사양서.docx','cpf-docs/deliverables/기술사양서.pdf',
'cpf-docs/deliverables/기술표준서.docx','cpf-docs/deliverables/기술표준서.pdf',
'cpf-docs/deliverables/데이터베이스표준서.docx','cpf-docs/deliverables/데이터베이스표준서.pdf',
'cpf-docs/deliverables/산출물목록.docx','cpf-docs/deliverables/산출물목록.pdf']
for x in required: need(x)
for n in ['hero.png','architecture.png','invoke.png','tx.png','batch.png','gateway.png','ops.png','capabilities.png','visual-geometry.json']: need('cpf-docs/assets/product-docs/'+n)
try:
 h=json.loads((H/'harness.json').read_text(encoding='utf-8'))
 if h.get('version')!='2.2.0': errs.append('HARNESS VERSION '+str(h.get('version')))
except Exception as e: errs.append('HARNESS READ '+str(e))
for x in required:
 p=ROOT/x
 if x.endswith('.pdf') and p.exists() and p.read_bytes()[:5]!=b'%PDF-': errs.append('BAD PDF '+x)
 if x.endswith('.docx') and p.exists():
  try:
   with zipfile.ZipFile(p) as z: z.getinfo('[Content_Types].xml')
  except Exception as e: errs.append('BAD DOCX '+x+' '+str(e))
text=(ROOT/'README.md').read_text(encoding='utf-8') if (ROOT/'README.md').exists() else ''
if 'CPF-DARK-CONTENT-SURFACE' not in text: errs.append('README content surface marker')
if any(x in text for x in ['그림 해석','그림 설명']): errs.append('generic figure label')
for label,target in re.findall(r'\[([^\]]+)\]\(([^)]+)\)',text):
 clean=target.split('#',1)[0].replace('%20',' ')
 if 'DOCX' in label.upper() or clean.lower().endswith('.docx'): errs.append('DOCX USER LINK '+label+' -> '+clean)
 if 'PDF' in label.upper():
  if not clean.lower().endswith('.pdf'): errs.append('PDF TARGET MISMATCH '+clean)
  elif not (ROOT/clean).is_file(): errs.append('PDF TARGET MISSING '+clean)
for stale in ['cpf-docs/governance/documentation-harness/CHANGELOG.md','cpf-docs/deliverables/documentation/APPLY_V125.ps1','cpf-docs/deliverables/documentation/DELETE_ONLY_V125.ps1']:
 if (ROOT/stale).exists(): errs.append('STALE '+stale)
# exact checksums
sf=ROOT/'cpf-docs/deliverables/documentation/SHA256SUMS.txt'
if not sf.is_file(): errs.append('SHA256SUMS missing')
else:
 for line in sf.read_text(encoding='utf-8').splitlines():
  m=re.match(r'^([0-9A-F]{64})  (.+)$',line)
  if not m: continue
  exp,rel=m.groups(); p=ROOT/rel
  if not p.is_file(): errs.append('CHECKSUM MISSING '+rel)
  elif hashlib.sha256(p.read_bytes()).hexdigest().upper()!=exp: errs.append('CHECKSUM '+rel)
if errs:
 print('DOCUMENTATION=FAIL'); [print('-',e) for e in errs]; sys.exit(1)
print('DOCUMENTATION=PASS'); print('HARNESS=2.2.0'); print('REQUIRED_ARTIFACTS=23'); print('README_VISUALS=8'); print('DOCX_USER_LINKS=0'); print('STALE_HARNESS_HISTORY=0')
