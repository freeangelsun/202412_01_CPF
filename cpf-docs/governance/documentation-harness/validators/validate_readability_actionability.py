#!/usr/bin/env python3
import json,re,sys
from pathlib import Path
from collections import Counter
from docx import Document
from docx.table import Table
from docx.text.paragraph import Paragraph
from docx.oxml.table import CT_Tbl
from docx.oxml.text.paragraph import CT_P

H=Path(__file__).resolve().parents[1]
ROOT=H.parents[2]
CFG=json.loads((H/'readability-actionability.json').read_text(encoding='utf-8'))
ERR=[]

def fail(scope,msg): ERR.append(f'{scope}: {msg}')

def resolve_artifact(rel):
    p=ROOT/rel
    if p.exists(): return p
    # Extraction fallback for archives with broken non-UTF8 filename flags: preserve numeric prefix.
    name=Path(rel).name
    m=re.match(r'^(\d+)_',name)
    parent=ROOT/Path(rel).parent
    if m and parent.is_dir():
        cands=list(parent.glob(m.group(1)+'_*.docx'))+list(parent.glob(m.group(1)+'_*.pdf'))
        ext=Path(rel).suffix.lower(); cands=[x for x in cands if x.suffix.lower()==ext]
        if len(cands)==1:return cands[0]
    return p

def markdown_checks(path):
    txt=path.read_text(encoding='utf-8')
    lines=txt.splitlines()
    # Centered hero body density: prose inside centered div, excluding headings/images and short sub/caption.
    for m in re.finditer(r'<div\s+align=["\']center["\']>(.*?)</div>',txt,re.I|re.S):
        body=m.group(1)
        cleaned=re.sub(r'<img\b[^>]*>','',body,flags=re.I)
        cleaned=re.sub(r'<[^>]+>',' ',cleaned)
        cleaned=re.sub(r'(?m)^\s*#+\s+.*$',' ',cleaned)
        cleaned=re.sub(r'[*_`#]','',cleaned)
        cleaned=re.sub(r'\s+',' ',cleaned).strip()
        if len(cleaned)>int(CFG['global']['centeredHeroLongCharsHardFail']):
            fail('README',f'dense centered hero/body {len(cleaned)} chars > {CFG["global"]["centeredHeroLongCharsHardFail"]}')
    # Consecutive markdown bullet runs.
    i=0
    while i<len(lines):
        if re.match(r'^\s*[-*+]\s+',lines[i]):
            items=[]
            while i<len(lines) and re.match(r'^\s*[-*+]\s+',lines[i]):
                items.append(re.sub(r'^\s*[-*+]\s+','',lines[i]).strip()); i+=1
            if len(items)>=int(CFG['global']['longFlatListItemsHardFail']):
                fail('README',f'flat list wall {len(items)} items; group into 3-5 semantic groups')
            long=[x for x in items if len(re.sub(r'\[[^]]+\]\([^)]*\)','LINK',x))>=int(CFG['global']['longBulletCharsReview'])]
            if len(long)>=int(CFG['global']['consecutiveLongBulletsHardFail']):
                fail('README',f'consecutive long bullet wall {len(long)} items')
            continue
        i+=1
    # Fenced code blocks with no real prose between.
    blocks=[]
    fence=list(re.finditer(r'```[^\n]*\n.*?```',txt,re.S))
    run=1
    for a,b in zip(fence,fence[1:]):
        between=txt[a.end():b.start()]
        meaningful=re.sub(r'<br\s*/?>|\s+','',between,flags=re.I)
        if meaningful=='': run+=1
        else: run=1
        if run>=int(CFG['global']['stackedCodeBlocksWithoutPurposeHardFail']):
            fail('README',f'{run} code blocks stacked without purpose/result explanation')
            break

def iter_blocks(doc):
    body=doc.element.body
    for child in body.iterchildren():
        if isinstance(child,CT_P): yield ('p',Paragraph(child,doc))
        elif isinstance(child,CT_Tbl): yield ('t',Table(child,doc))

def chapterize(doc):
    chapters=[]; cur=None
    for typ,obj in iter_blocks(doc):
        if typ=='p':
            st=obj.style.name if obj.style else ''
            text=obj.text.strip()
            if st=='Heading 1':
                cur={'title':text,'blocks':[]}; chapters.append(cur); continue
        if cur is not None: cur['blocks'].append((typ,obj))
    return chapters

def chapter_text_outside_tables(ch):
    parts=[]
    for typ,obj in ch['blocks']:
        if typ=='p' and obj.text.strip(): parts.append(obj.text.strip())
    return '\n'.join(parts)

def is_action_para(p):
    st=(p.style.name if p.style else '')
    t=p.text.strip()
    if not t:return False
    if st in {'CPF Table Title','CPF Table Purpose','CPF Source','CPF Caption','CPF Section Lead','CPF Lead','CPF Warning'}: return False
    if st.startswith('Heading'): return False
    if st=='CPF Code' or st.startswith('List Number'): return True
    action_tokens=['사용합니다','호출합니다','설정합니다','구현합니다','실행합니다','확인합니다','작성합니다','추가합니다','연결합니다','검증합니다','선언합니다','적용합니다']
    return len(t)>=35 and any(x in t for x in action_tokens)

def docx_checks(path,artifact_id):
    d=Document(path)
    # consecutive long bullet runs
    run=[]
    for p in d.paragraphs+[None]:
        if p is not None and p.style and p.style.name.startswith('List Bullet'):
            run.append(p.text.strip())
        else:
            if len(run)>=int(CFG['global']['longFlatListItemsHardFail']): fail(artifact_id,f'flat list wall {len(run)} items')
            long=[x for x in run if len(x)>=int(CFG['global']['longBulletCharsReview'])]
            if len(long)>=int(CFG['global']['consecutiveLongBulletsHardFail']): fail(artifact_id,f'consecutive long bullet wall {len(long)} items')
            run=[]
    chapters=chapterize(d)
    if artifact_id in {'FRAMEWORK_DEVELOPER_GUIDE','BATCH_DEVELOPER_GUIDE','GATEWAY_GUIDE'}:
        for ch in chapters:
            tables=sum(1 for typ,_ in ch['blocks'] if typ=='t')
            actions=[obj for typ,obj in ch['blocks'] if typ=='p' and is_action_para(obj)]
            codes=[obj for typ,obj in ch['blocks'] if typ=='p' and obj.style and obj.style.name=='CPF Code' and obj.text.strip()]
            if tables>=int(CFG['global']['tableWallTablesWithoutActionHardFail']) and not (actions or codes):
                fail(artifact_id,f'chapter "{ch["title"]}" is table/API-summary wall: tables={tables}, working action/example=0')
    if artifact_id=='FRAMEWORK_DEVELOPER_GUIDE':
        # Persistence selection must have provider-specific next-action evidence OUTSIDE tables.
        ch=next((x for x in chapters if re.search(r'CRUD|Persistence',x['title'],re.I)),None)
        if ch:
            out=chapter_text_outside_tables(ch)
            pc=CFG['frameworkDeveloperGuide']['persistenceSelection']['choices']
            for choice,tokens in pc.items():
                if not any(tok.lower() in out.lower() for tok in tokens):
                    fail(artifact_id,f'JDBC/MyBatis/JPA Selection-to-Action missing outside-table next action for {choice}')
        # Domain invocation requires actual non-table consumer/flow evidence.
        ch=next((x for x in chapters if re.search(CFG['frameworkDeveloperGuide']['domainInvocation']['headingRegex'],x['title'],re.I)),None)
        if ch:
            out=chapter_text_outside_tables(ch)
            for tok in CFG['frameworkDeveloperGuide']['domainInvocation']['requiredOutsideTableEvidence']:
                if tok.lower() not in out.lower(): fail(artifact_id,f'Domain invocation working flow missing outside-table evidence: {tok}')

def main():
    readme=ROOT/'README.md'
    if readme.is_file(): markdown_checks(readme)
    else: fail('README','missing')
    artifacts=[
      ('FRAMEWORK_DEVELOPER_GUIDE','cpf-docs/guides/02_프레임워크_개발자_가이드.docx'),
      ('BATCH_DEVELOPER_GUIDE','cpf-docs/guides/03_배치_개발자_가이드.docx'),
      ('GATEWAY_GUIDE','cpf-docs/guides/06_Gateway_개발_사용_가이드.docx')]
    for aid,rel in artifacts:
        p=resolve_artifact(rel)
        if not p.is_file(): fail(aid,'missing '+rel); continue
        try: docx_checks(p,aid)
        except Exception as e: fail(aid,'validator error '+str(e))
    if ERR:
        print('READABILITY_ACTIONABILITY=FAIL COUNT='+str(len(ERR)))
        for e in ERR: print('-',e)
        return 1
    print('READABILITY_ACTIONABILITY=PASS')
    return 0
if __name__=='__main__': sys.exit(main())
