#!/usr/bin/env python3
import re,sys,zipfile
from pathlib import Path
from docx import Document
from docx.oxml.ns import qn
ROOT=Path(__file__).resolve().parents[4]
DOCS=[
 'cpf-docs/guides/02_프레임워크_개발자_가이드.docx','cpf-docs/guides/03_배치_개발자_가이드.docx',
 'cpf-docs/guides/04_운영자_매뉴얼.docx','cpf-docs/guides/05_배치_운영_가이드.docx',
 'cpf-docs/guides/06_Gateway_개발_사용_가이드.docx','cpf-docs/guides/07_Specification_기술_명세.docx',
 'cpf-docs/deliverables/아키텍처설계서.docx','cpf-docs/deliverables/기술사양서.docx',
 'cpf-docs/deliverables/기술표준서.docx','cpf-docs/deliverables/데이터베이스표준서.docx','cpf-docs/deliverables/산출물목록.docx']
PROV=re.compile(r'(Harness\s*(?:v)?\d+(?:\.\d+)+|Source\s*(?:SHA\s*)?[0-9A-F]{16,}|CPF_FULL_SOURCE_FOR_NEXT_QA_\d+)',re.I)
META=('누가 보는가','이 문서로 끝낼 일','기준')
errors=[]
def fail(p,m): errors.append(f'{p}: {m}')
def text_of_table(t): return '\n'.join(c.text for r in t.rows for c in r.cells)
def para_has_nontext_object(p):
    return bool(p._p.xpath('.//w:drawing|.//w:pict|.//w:br[@w:type="page"]|.//w:fldChar|.//w:hyperlink'))
for rel in DOCS:
    p=ROOT/rel
    if not p.is_file(): fail(rel,'missing'); continue
    try: doc=Document(p)
    except Exception as e: fail(rel,'open '+str(e)); continue
    # first two tables must not encode reader/purpose/basis metadata.
    for t in doc.tables[:2]:
        tt=text_of_table(t)
        if sum(1 for x in META if x in tt)>=2: fail(rel,'opening reader/purpose/basis metadata table')
    # one-row table is a layout/callout smell in current CPF official docs.
    for idx,t in enumerate(doc.tables,1):
        if len(t.rows)==1: fail(rel,f'single-row layout table #{idx}')
        if not t.rows or not t.rows[0].cells: continue
        trPr=t.rows[0]._tr.get_or_add_trPr()
        if trPr.find(qn('w:tblHeader')) is None: fail(rel,f'table #{idx} header row not marked repeat header')
    # provenance is forbidden in visible body/table/header/footer.
    visible=[]
    visible.extend(x.text for x in doc.paragraphs)
    for t in doc.tables: visible.append(text_of_table(t))
    for sec in doc.sections:
        visible.extend(x.text for x in sec.header.paragraphs)
        visible.extend(x.text for x in sec.footer.paragraphs)
    mt=PROV.search('\n'.join(visible))
    if mt: fail(rel,'user-facing production provenance '+mt.group(0))
    # TOC/finder entries: dotted right tab and page number must stay inside writable width.
    if doc.sections:
        sec=doc.sections[0]
        writable=sec.page_width-sec.left_margin-sec.right_margin
        for para in doc.paragraphs:
            if para.style and para.style.name=='CPF TOC Entry':
                tabs=list(para.paragraph_format.tab_stops)
                if not tabs: fail(rel,'TOC entry missing right tab stop: '+para.text[:50]); continue
                if max(t.position for t in tabs)>writable: fail(rel,'TOC tab stop outside writable width: '+para.text[:50])
                if not re.search(r'\t\s*\d+\s*$',para.text): fail(rel,'TOC page number missing/not visible: '+para.text[:50])
    # semantic headings: numbered major headings use heading styles, no heading-level skips.
    prev=0
    for para in doc.paragraphs:
        st=para.style.name if para.style else ''
        if st.startswith('Heading '):
            try:lvl=int(st.split()[-1])
            except:continue
            if prev and lvl>prev+1: fail(rel,f'heading level skip {prev}->{lvl}: {para.text[:50]}')
            prev=lvl
    # blank spacer paragraphs are forbidden unless they carry a real page break/object/field.
    run_blanks=0
    for para in doc.paragraphs:
        if para.text.strip()=='' and not para_has_nontext_object(para): run_blanks+=1
        else: run_blanks=0
        if run_blanks>=2: fail(rel,'two or more consecutive blank spacer paragraphs'); break
    # OOXML must be a valid zip with document.xml.
    try:
        with zipfile.ZipFile(p) as z:
            if 'word/document.xml' not in z.namelist(): fail(rel,'word/document.xml missing')
    except Exception as e: fail(rel,'invalid package '+str(e))
if errors:
    print('DOCX_STRUCTURE=FAIL COUNT='+str(len(errors)))
    for e in errors: print('-',e)
    sys.exit(1)
print('DOCX_STRUCTURE=PASS DOCX='+str(len(DOCS)))
