#!/usr/bin/env python3
from pathlib import Path
import re, sys
p=Path(sys.argv[1] if len(sys.argv)>1 else 'README.md').resolve()
text=p.read_text(encoding='utf-8')
errs=[]
if re.search(r'^##?\s*목차\s*$',text,re.M): errs.append('README 목차 금지')
h1=re.findall(r'^# (?!#)(.+)$',text,re.M)
numbered=[x for x in h1 if re.match(r'^\d+\. ',x)]
if numbered:
 nums=[int(re.match(r'^(\d+)\.',x).group(1)) for x in numbered]
 if nums!=list(range(1,len(nums)+1)): errs.append('README H1 번호 불연속')
else: errs.append('번호형 README H1 없음')
exact='CPF는 **Community & Evaluation License** 안내를 기준으로 사용합니다.'
if text.count(exact)!=1: errs.append('License 지정 문장 정확히 1회 필요')
if re.search(r'\[[^\]]*LICENSE[^\]]*\]\(',text,re.I): errs.append('LICENSE 파일 링크 금지')
for bad in ['그림 해석','그림 설명']:
 if bad in text: errs.append('generic figure label '+bad)

# user-facing provenance must stay out of README
for pat,label in [(r'Harness\s+v?\d+(?:\.\d+)+','Harness version'),(r'Source(?: snapshot)?\s*[:·]?\s*(?:ZIP_SHA256:)?[0-9A-Fa-f]{16,}','Source SHA'),(r'Documentation baseline','Documentation baseline')]:
 if re.search(pat,text,re.I): errs.append('user-facing provenance forbidden: '+label)

imgs=re.findall(r'!\[[^\]]*\]\(([^)]+)\)',text)
imgs += re.findall(r'<img\b[^>]*\bsrc=["\']([^"\']+)["\'][^>]*>',text,re.I)
# stable unique order
imgs=list(dict.fromkeys(imgs))
if len(imgs)<5 or len(imgs)>8: errs.append(f'Visual count {len(imgs)} not 5..8')
if not any('architecture' in x.lower() for x in imgs): errs.append('Architecture visual reference missing')
if 'CPF-DARK-CONTENT-SURFACE' not in text: errs.append('CPF owned dark content surface marker missing')
for src in imgs:
 clean=src.split('#',1)[0].replace('%20',' ')
 if re.match(r'^(https?:|data:)',clean,re.I): continue
 if not (p.parent/clean).is_file(): errs.append('visual target missing: '+src)
# markdown links only: user navigation is PDF-only; DOCX links forbidden.
for label,target in re.findall(r'\[([^\]]+)\]\(([^)]+)\)',text):
 clean=target.split('#',1)[0].replace('%20',' ')
 if re.search(r'DOCX',label,re.I) or re.search(r'\.docx$',clean,re.I): errs.append(f'DOCX user link forbidden: {label} -> {clean}')
 if re.search(r'PDF',label,re.I) and not re.search(r'\.pdf$',clean,re.I): errs.append(f'PDF label target mismatch: {label} -> {clean}')
 if re.match(r'^(https?:|mailto:|#)',clean,re.I): continue
 if re.search(r'PDF',label,re.I) and not (p.parent/clean).is_file(): errs.append('document link target missing: '+clean)
if errs:
 print('README=FAIL'); [print('-',e) for e in errs]; sys.exit(1)
print('README=PASS')
print('VISUALS='+str(len(imgs)))
