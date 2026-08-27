#!/usr/bin/env python3
from pathlib import Path
import hashlib,json,re,sys
ROOT=Path(__file__).resolve().parents[3]
errs=[]
def req(p,label):
    if not p.is_file(): errs.append('missing '+label+': '+str(p))
def sha(p):
    h=hashlib.sha256()
    with p.open('rb') as f:
        for c in iter(lambda:f.read(1024*1024),b''): h.update(c)
    return h.hexdigest().upper()
h=ROOT/'cpf-docs/governance/documentation-harness/harness.json'
req(h,'harness')
if h.is_file():
    d=json.loads(h.read_text(encoding='utf-8'))
    if d.get('version')!='2.3.0': errs.append('harness version '+str(d.get('version')))
readme=ROOT/'README.md'; req(readme,'README')
if readme.is_file():
    t=readme.read_text(encoding='utf-8')
    if '.docx)' in t.lower() or re.search(r'\[[^\]]*DOCX[^\]]*\]\(',t,re.I): errs.append('DOCX user link')
    imgs=re.findall(r'<img\b[^>]*\bsrc=["\']([^"\']+)["\']',t,re.I)+re.findall(r'!\[[^\]]*\]\(([^)]+)\)',t)
    if len(dict.fromkeys(imgs))!=8: errs.append('README visual count')
paths=[]
for sub in ['cpf-docs/guides','cpf-docs/deliverables']:
    base=ROOT/sub
    paths+=list(base.glob('*.docx'))+list(base.glob('*.pdf'))
if len([p for p in paths if p.suffix.lower()=='.docx'])!=11: errs.append('DOCX count')
if len([p for p in paths if p.suffix.lower()=='.pdf'])!=11: errs.append('PDF count')
for p in paths: req(p,p.name)
for stale in ['cpf-docs/governance/documentation-harness/CHANGELOG.md','cpf-docs/deliverables/documentation/APPLY_V125.ps1','cpf-docs/deliverables/documentation/DELETE_ONLY_V125.ps1']:
    if (ROOT/stale).exists(): errs.append('stale '+stale)
si=ROOT/'cpf-docs/deliverables/documentation/SOURCE_IDENTITY.json'
req(si,'source identity')
if si.is_file():
    x=json.loads(si.read_text(encoding='utf-8'))
    if x.get('sourceZipSha256')!='A5B7844665F4AC3BDAEC601389B306CEBD6F0407AD1C07930C40170611DB7A07': errs.append('source digest')
# Package manifest/hash integrity
pm=ROOT/'cpf-docs/deliverables/documentation/PACKAGE_MANIFEST.json'; req(pm,'package manifest')
if pm.is_file():
    m=json.loads(pm.read_text(encoding='utf-8'))
    if m.get('sourceZipSha256')!='A5B7844665F4AC3BDAEC601389B306CEBD6F0407AD1C07930C40170611DB7A07': errs.append('package source digest')
    if m.get('harnessVersion')!='2.3.0': errs.append('package harness version')
    for e in m.get('files',[]):
        fp=ROOT/e['path']; req(fp,e['path'])
        if fp.is_file() and sha(fp)!=e.get('sha256','').upper(): errs.append('package hash '+e['path'])
sums=ROOT/'cpf-docs/deliverables/documentation/SHA256SUMS.txt'; req(sums,'sha256sums')
if sums.is_file():
    for line in sums.read_text(encoding='utf-8').splitlines():
        if not line.strip(): continue
        try: expected,rel=line.split('  ',1)
        except ValueError: errs.append('bad checksum line'); continue
        fp=ROOT/rel; req(fp,rel)
        if fp.is_file() and sha(fp)!=expected.upper(): errs.append('checksum '+rel)
# Absolute path gate only for Documentation cycle generated/modified artifacts.
roots=[r'C:\dev\projects\jck\202412_01_CPF',r'D:\WORK_CPF\202412_01_CPF']
check=[ROOT/'README.md']+paths+list((ROOT/'cpf-docs/assets/product-docs').glob('*'))+list((ROOT/'cpf-docs/governance/documentation-harness').rglob('*'))+list((ROOT/'cpf-docs/deliverables/documentation').glob('*'))
for p in check:
    if not p.is_file(): continue
    rel=p.relative_to(ROOT).as_posix().replace('/','\\')
    for base in roots:
        if len(base+'\\'+rel)>150: errs.append('path>150 '+rel); break
if errs:
    print('DOCUMENTATION=FAIL'); [print('-',e) for e in errs]; sys.exit(1)
print('DOCUMENTATION=PASS')
print('HARNESS=2.3.0')
print('REQUIRED_ARTIFACTS=23')
print('README_VISUALS=8')
print('DOCX_USER_LINKS=0')
print('PACKAGE_HASHES=PASS')
print('PATH_OVER_150=0')
