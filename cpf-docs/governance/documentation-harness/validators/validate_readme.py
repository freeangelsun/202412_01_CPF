#!/usr/bin/env python3
import json,re,sys
from pathlib import Path
H=Path(__file__).resolve().parents[1]
ROOT=H.parents[2]
p=Path(sys.argv[1]).resolve() if len(sys.argv)>1 else ROOT/'README.md'
text=p.read_text(encoding='utf-8')
errs=[]

def err(x): errs.append(x)
# Heading structure: one product H1, compact H2 flow, no promotional benefit headings.
h1=re.findall(r'^#\s+([^#].*)$',text,re.M)
h2=re.findall(r'^##\s+([^#].*)$',text,re.M)
h3=re.findall(r'^###\s+([^#].*)$',text,re.M)
if len(h1)!=1: err(f'exactly one H1 product title required, found {len(h1)}')
if len(h2)<5: err(f'H2 section count must be >=5 for brochure coverage, found {len(h2)}')
if re.search(r'^####+\s+',text,re.M): err('heading depth > H3 forbidden')
forbidden=json.loads((H/'readme-value-inventory.json').read_text(encoding='utf-8')).get('forbiddenHeadingTokens',[])
for hd in h1+h2+h3:
    norm=re.sub(r'^\d+[\.)]?\s*','',hd).strip()
    for tok in forbidden:
        if tok.lower() in norm.lower(): err(f'promotional/benefit heading forbidden: {hd}'); break
# README TOC and product/license rules.
if re.search(r'^##?\s*목차\s*$',text,re.M): err('README 목차 금지')
if re.search(r'^##?\s*(?:5분|Quick\s*Start|빠른\s*시작)',text,re.I|re.M) and (re.search(r'^##\s*(?:5분|Quick\s*Start|빠른\s*시작)',text,re.I|re.M).start() < max(1,len(text)//5)):
    err('quick start must not dominate top of README')
exact='CPF는 **Community & Evaluation License** 안내를 기준으로 사용합니다.'
if text.count(exact)!=1: err('License 지정 문장 정확히 1회 필요')
if re.search(r'\[[^\]]*LICENSE[^\]]*\]\(',text,re.I): err('LICENSE 파일 링크 금지')
for bad in ['그림 해석','그림 설명']:
    if bad in text: err('generic figure label '+bad)
# No provenance in user-facing README.
for pat,label in [(r'Harness\s+v?\d+(?:\.\d+)+','Harness version'),(r'Source(?: snapshot)?\s*[:·]?\s*(?:ZIP_SHA256:)?[0-9A-Fa-f]{16,}','Source SHA'),(r'Documentation baseline','Documentation baseline')]:
    if re.search(pat,text,re.I): err('user-facing provenance forbidden: '+label)
# Scanability: short paragraphs, no wall of text.
# Strip fenced code, HTML comments, headings, list items, table rows, images.
clean=re.sub(r'```.*?```','',text,flags=re.S)
paras=[]
for block in re.split(r'\n\s*\n',clean):
    b=block.strip()
    if not b or b.startswith('#') or b.startswith('<!--') or b.startswith('![') or b.startswith('<img') or b.startswith('>'):
        continue
    lines=[x.strip() for x in b.splitlines() if x.strip()]
    if lines and all(re.match(r'^[-*+]\s+|^\d+[.)]\s+',x) for x in lines): continue
    if lines and all('|' in x for x in lines): continue
    plain=' '.join(lines)
    plain=re.sub(r'\[([^\]]+)\]\([^)]+\)',r'\1',plain)
    if len(plain)>420: err(f'paragraph too long >420 chars: {plain[:70]}...')
    sents=len([x for x in re.split(r'[.!?。]\s*',plain) if x.strip()])
    if sents>5: err(f'paragraph too many sentences >5: {plain[:70]}...')
    paras.append(len(plain))
run=0
for n in paras:
    run=run+1 if n>=220 else 0
    if run>=3: err('3 consecutive long paragraphs >=220 chars'); break
# Consecutive table / image blocks.
blocks=[b.strip() for b in re.split(r'\n\s*\n',text) if b.strip()]
def kind(b):
    ls=[x.strip() for x in b.splitlines() if x.strip()]
    if len(ls)>=2 and all('|' in x for x in ls[:2]): return 'table'
    if b.startswith('![') or b.lower().startswith('<img'): return 'image'
    if b.startswith('#'): return 'heading'
    return 'text'
for a,b in zip(blocks,blocks[1:]):
    if kind(a)=='table' and kind(b)=='table': err('consecutive tables forbidden')
    if kind(a)=='image' and kind(b)=='image': err('consecutive large figures without explanation forbidden')
# Markdown tables: real tabular data only, <=4 cols default, reasonable cell size.
lines=text.splitlines()
i=0
while i<len(lines)-1:
    if '|' in lines[i] and re.match(r'^\s*\|?\s*:?-{3,}',lines[i+1]):
        rows=[]; j=i
        while j<len(lines) and '|' in lines[j] and lines[j].strip(): rows.append(lines[j]); j+=1
        cols=len([c for c in rows[0].strip().strip('|').split('|')])
        if cols>4: err(f'README table columns >4: {cols}')
        data=max(0,len(rows)-2)
        if data<2: err('non-tabular/small README table: fewer than 2 data rows')
        for row in rows:
            for cell in row.strip().strip('|').split('|'):
                if len(cell.strip())>90: err('README table cell >90 chars; move detail to prose/guide')
        i=j
    else:i+=1
# Visuals and links.
imgs=re.findall(r'!\[[^\]]*\]\(([^)]+)\)',text)+re.findall(r'<img\b[^>]*\bsrc=["\']([^"\']+)["\'][^>]*>',text,re.I)
imgs=list(dict.fromkeys(imgs))
if len(imgs)<5: err(f'Visual count {len(imgs)} <5 brochure minimum; no upper bound')
if not any('architecture' in x.lower() for x in imgs): err('Architecture visual reference missing')
for src in imgs:
    cleanp=src.split('#',1)[0].replace('%20',' ')
    if re.match(r'^(https?:|data:)',cleanp,re.I): continue
    if not (p.parent/cleanp).is_file(): err('visual target missing: '+src)
for label,target in re.findall(r'\[([^\]]+)\]\(([^)]+)\)',text):
    cleanp=target.split('#',1)[0].replace('%20',' ')
    if re.search(r'DOCX',label,re.I) or re.search(r'\.docx$',cleanp,re.I): err(f'DOCX user link forbidden: {label} -> {cleanp}')
    if re.search(r'PDF',label,re.I) and not re.search(r'\.pdf$',cleanp,re.I): err(f'PDF label target mismatch: {label} -> {cleanp}')
# Brochure + AI/text companion.
first_h2_pos=re.search(r'(?m)^##\s+',text)
first_h2_pos=first_h2_pos.start() if first_h2_pos else len(text)
pre=text[:first_h2_pos]
pre_imgs=re.findall(r'!\[([^\]]*)\]\(([^)]+)\)',pre)
pre_imgs+=re.findall(r'<img\b[^>]*\balt=["\']([^"\']*)["\'][^>]*\bsrc=["\']([^"\']+)["\'][^>]*>',pre,re.I)
pre_imgs+= [(alt,src) for src,alt in re.findall(r'<img\b[^>]*\bsrc=["\']([^"\']+)["\'][^>]*\balt=["\']([^"\']*)["\'][^>]*>',pre,re.I)]
if not pre_imgs: err('README brochure Hero visual must appear before first H2')
if pre_imgs and not any(('hero' in src.lower()) or ('cpf' in alt.lower()) for alt,src in pre_imgs): err('README Hero visual not identifiable before first H2')
# Every informative figure (Markdown or HTML) needs alt text and a nearby visible Korean companion.
mdimg=re.compile(r'!\[([^\]]*)\]\(([^)]+)\)')
htmlimg=re.compile(r'<img\b[^>]*>',re.I)
lines=text.splitlines()
def check_companion(idx,alt,src):
    alt=(alt or '').strip(); src=(src or '').strip()
    stem=Path(src.split('#',1)[0]).stem.lower()
    if not alt or re.fullmatch(r'(image|img|figure|그림|이미지)?\s*\d*',alt,re.I) or (stem and alt.lower()==stem):
        err('meaningful Alt Text required for figure: '+src)
    companion=None
    for k in range(idx+1,min(len(lines),idx+7)):
        s=lines[k].strip()
        if not s: continue
        if s.startswith('#') or s.startswith('![') or s.lower().startswith('<img') or (s.startswith('|') and '|' in s[1:]): break
        if s.startswith('<!--'): continue
        plain=re.sub(r'[*_`>#\[\]()]','',s).strip()
        if re.search(r'[가-힣]',plain) and len(plain)>=15:
            companion=plain; break
    if companion is None: err('visible Korean 1-2 sentence companion required directly after figure: '+src)
    elif re.match(r'^(그림\s*(설명|해석)|이미지\s*설명)',companion): err('generic figure-description label forbidden after figure: '+src)
for idx,line in enumerate(lines):
    for m in mdimg.finditer(line): check_companion(idx,m.group(1),m.group(2))
    for m in htmlimg.finditer(line):
        tag=m.group(0)
        sm=re.search(r'\bsrc=["\']([^"\']+)["\']',tag,re.I); am=re.search(r'\balt=["\']([^"\']*)["\']',tag,re.I)
        if sm: check_companion(idx,am.group(1) if am else '',sm.group(1))
# Total README length/section/figure count has no upper bound. Long content must reflow, not be truncated.

# Natural value distribution: enough concrete CPF capabilities, spread across functional sections, never benefit-only heading.
rv=json.loads((H/'readme-value-inventory.json').read_text(encoding='utf-8'))
parts=re.split(r'(?m)^##\s+[^\n]+$',text)
headings=re.findall(r'(?m)^##\s+([^\n]+)$',text)
section_texts=parts[1:] if len(parts)>1 else []
found=[]; sec_counts=[0]*len(section_texts)
for g in rv.get('groups',[]):
    hit=False
    for si,sec in enumerate(section_texts):
        if any(tok.lower() in sec.lower() for tok in g.get('evidenceAny',[])):
            hit=True; sec_counts[si]+=1
    if hit: found.append(g['id'])
min_groups=rv.get('naturalDistribution',{}).get('requiredGroupsMin',7)
if len(found)<min_groups: err(f'natural CPF value/capability coverage {len(found)}<{min_groups}')
distinct=sum(1 for x in sec_counts if x>0)
if distinct<rv.get('naturalDistribution',{}).get('distinctFunctionalSectionsMin',4): err(f'value/capability evidence concentrated in only {distinct} sections')
if found and sec_counts:
    share=max(sec_counts)/max(1,sum(sec_counts))*100
    if share>rv.get('naturalDistribution',{}).get('maxSingleSectionSharePct',45): err(f'value/capability evidence too concentrated in one section {share:.1f}%')
# Dedicated benefit section heuristic.
for hd,sec in zip(headings,section_texts):
    if re.search(r'(장점|왜\s*좋|달라지는|좋아지는|편해지는|차별점|효익)',hd,re.I): err('dedicated benefit section forbidden: '+hd)
if errs:
    print('README=FAIL COUNT='+str(len(errs)))
    for e in errs: print('-',e)
    sys.exit(1)
print('README=PASS')
print('H1=1 H2='+str(len(h2))+' VISUALS='+str(len(imgs))+' NO_TOTAL_SIZE_CAP=PASS AI_TEXT_COMPANION=PASS VALUE_GROUPS='+str(len(found))+' VALUE_SECTIONS='+str(distinct))
