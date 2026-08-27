#!/usr/bin/env python3
import sys,zipfile,unicodedata,re
from pathlib import Path
if len(sys.argv)<2:
 print('SOURCE_ZIP=FAIL usage: validate_source_zip.py <zip>'); raise SystemExit(2)
z=Path(sys.argv[1]).resolve()
if not z.is_file(): print('SOURCE_ZIP=FAIL missing '+str(z)); raise SystemExit(1)
with zipfile.ZipFile(z) as q:
 names=[n.replace('\\','/') for n in q.namelist() if not n.endswith('/')]
# Normalize for canonical comparisons while preserving originals
nfc=[unicodedata.normalize('NFC',n) for n in names]
def count_rx(p): return sum(1 for n in nfc if re.search(p,n,re.I))
def any_prefix(p): return any(n.startswith(p) for n in nfc)
errs=[]
if 'README.md' not in nfc: errs.append('README missing')
for p in ['cpf-docs/guides/','cpf-docs/deliverables/','cpf-docs/assets/','cpf-docs/governance/documentation-harness/','cpf-tools/build/']:
 if not any_prefix(p): errs.append('forced include missing '+p)
docx=count_rx(r'^(cpf-docs/guides|cpf-docs/deliverables)/.*\.docx$')
pdf=count_rx(r'^(cpf-docs/guides|cpf-docs/deliverables)/.*\.pdf$')
visual=sum(1 for n in nfc if n.startswith('cpf-docs/assets/product-docs/') and n.lower().endswith('.png'))
harness=sum(1 for n in nfc if n.startswith('cpf-docs/governance/documentation-harness/'))
tools=sum(1 for n in nfc if n.startswith('cpf-tools/build/'))
if docx<11: errs.append(f'DOCX {docx}<11')
if pdf<11: errs.append(f'PDF {pdf}<11')
if visual<8: errs.append(f'VISUAL {visual}<8')
if harness<57: errs.append(f'HARNESS {harness}<57')
if tools<1: errs.append('CPF_TOOLS_BUILD empty')
# Duplicate after NFC is dangerous
if len(set(nfc))!=len(nfc): errs.append('NFC duplicate paths')
if errs:
 print('SOURCE_ZIP=FAIL'); [print('-',e) for e in errs]; raise SystemExit(1)
print('SOURCE_ZIP=PASS')
print('FILES='+str(len(names)))
print('CPF_DOCX='+str(docx))
print('CPF_PDF='+str(pdf))
print('PRODUCT_VISUAL_PNG='+str(visual))
print('HARNESS='+str(harness))
print('CPF_TOOLS_BUILD='+str(tools))
