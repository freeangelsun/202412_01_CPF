#!/usr/bin/env python3
from pathlib import Path
import re, sys
p=Path(sys.argv[1] if len(sys.argv)>1 else 'README.md')
text=p.read_text(encoding='utf-8')
errs=[]
if re.search(r'^##?\s*목차\s*$',text,re.M): errs.append('README 목차 금지')
h1=re.findall(r'^# (?!#)(.+)$',text,re.M)
# exclude top product title if present; numbered content H1s only after product title
numbered=[x for x in h1 if re.match(r'^\d+\. ',x)]
if numbered:
 nums=[int(re.match(r'^(\d+)\.',x).group(1)) for x in numbered]
 if nums!=list(range(1,len(nums)+1)): errs.append('README H1 번호 불연속')
else: errs.append('번호형 README H1 없음')
exact='CPF는 **Community & Evaluation License** 안내를 기준으로 사용합니다.'
if text.count(exact)!=1: errs.append('License 지정 문장 정확히 1회 필요')
if re.search(r'\[[^\]]*LICENSE[^\]]*\]\(',text,re.I): errs.append('LICENSE 파일 링크 금지')
imgs=re.findall(r'!\[[^\]]*\]\(([^)]+)\)',text)
if len(imgs)<5 or len(imgs)>8: errs.append(f'Visual count {len(imgs)} not 5..8')
if not any('architecture' in x.lower() for x in imgs): errs.append('Architecture visual reference missing')
if errs:
 print('README=FAIL'); [print('-',e) for e in errs]; sys.exit(1)
print('README=PASS')
