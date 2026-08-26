from pathlib import Path
import json,sys,re,subprocess
ROOT=Path(__file__).resolve().parents[3]
H=ROOT/'cpf-docs/governance/documentation-harness'
errs=[]
def need(p):
    if not (ROOT/p).is_file(): errs.append('MISSING '+p)
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
for n in ['hero.png','architecture.png','invoke.png','tx.png','batch.png','gateway.png','ops.png','docs.png']: need('cpf-docs/assets/product-docs/'+n)
try:
    v=json.loads((H/'harness.json').read_text(encoding='utf-8')).get('version')
    if v!='1.2.5': errs.append('HARNESS VERSION '+str(v))
except Exception as e: errs.append('HARNESS READ '+str(e))
# PDF signatures
for x in required:
    if x.endswith('.pdf') and (ROOT/x).exists():
        if (ROOT/x).read_bytes()[:5] != b'%PDF-': errs.append('BAD PDF '+x)
# README references
rt=(ROOT/'README.md').read_text(encoding='utf-8') if (ROOT/'README.md').exists() else ''
for token in ['Gateway','Bootstrap','Build','Test','Runtime','UNKNOWN','Reconcile','Idempotency','Oracle','PostgreSQL','MariaDB','개발자 가이드','운영자 매뉴얼']:
    if token.lower() not in rt.lower(): errs.append('README TOKEN '+token)
if '내부 Domain 간 호출은 Gateway를 경유하지' not in rt: errs.append('README GATEWAY BOUNDARY')
# old product visual garbage must be absent
for n in ['cpf-architecture-map.svg','cpf-batch-control.svg','cpf-canonical-lifecycle.svg','cpf-capability-landscape.svg','cpf-ownership-boundary.svg','cpf-readme-hero.svg','cpf-recovery-state.svg','cpf-topology-parity.svg']:
    if (ROOT/'cpf-docs/assets/product-docs'/n).exists(): errs.append('OLD VISUAL '+n)
# 150 char Windows absolute path budget: only files generated/replaced by this Documentation package
prefix='C:\\dev\\projects\\jck\\202412_01_CPF\\'
import csv
cm=ROOT/'cpf-docs/deliverables/documentation/CHANGE_MANIFEST.csv'
if cm.exists():
    with cm.open(encoding='utf-8-sig',newline='') as fh:
        for row in csv.DictReader(fh):
            rel=(row.get('path') or '').replace('/','\\')
            if rel and len(prefix+rel)>150: errs.append('PATH>150 '+rel)
else:
    errs.append('CHANGE MANIFEST MISSING')
if errs:
    print('DOCUMENTATION=FAIL'); [print(x) for x in errs]; sys.exit(1)
print('DOCUMENTATION=PASS'); print('HARNESS=1.2.5'); print('REQUIRED_ARTIFACTS=23'); print('README_VISUALS=8'); print('PATH_OVER_150=0'); print('OLD_PRODUCT_VISUALS=0')
