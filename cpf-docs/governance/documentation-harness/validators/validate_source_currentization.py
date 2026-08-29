#!/usr/bin/env python3
import json,hashlib,re,sys
from pathlib import Path
H=Path(__file__).resolve().parents[1]; ROOT=H.parents[2]; C=json.loads((H/'source-currentization.json').read_text(encoding='utf-8'))
pre=ROOT/'cpf-docs/deliverables/documentation/SOURCE_CURRENTIZATION_PREAUTHORING.json'; fin=ROOT/'cpf-docs/deliverables/documentation/SOURCE_CURRENTIZATION_FINALVALIDATION.json'
errs=[]
if not pre.is_file() or not fin.is_file(): errs.append('pre/final currentization evidence missing')
else:
 a=json.loads(pre.read_text()); b=json.loads(fin.read_text());
 if a.get('sourceFingerprint')!=b.get('sourceFingerprint'): errs.append('source drift between preAuthoring and finalValidation')
 for rel in ['README.md','cpf-docs/guides/02_프레임워크_개발자_가이드.docx']:
  p=ROOT/rel
  if p.is_file():
   raw=p.read_bytes().decode('utf-8','ignore') if p.suffix=='.md' else ''
   for bad in C['deprecatedPublicContracts']:
    if bad in raw: errs.append(f'deprecated public contract {bad} in {rel}')
if errs:
 print('SOURCE_CURRENTIZATION=FAIL COUNT='+str(len(errs))); [print('-',x) for x in errs]; sys.exit(1)
a=json.loads(pre.read_text()); print('SOURCE_CURRENTIZATION=PASS'); print('SOURCE_FINGERPRINT='+a['sourceFingerprint']); print('SOURCE_FILES='+str(a['sourceFiles']))
