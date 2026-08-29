#!/usr/bin/env python3
from pathlib import Path
import csv,collections
ROOT=Path(__file__).resolve().parents[4]; H=ROOT/'cpf-docs/governance/development-harness'
with (H/'current/CURRENT_DEVELOPMENT_STATUS.csv').open(encoding='utf-8-sig',newline='') as f: rows=list(csv.DictReader(f))
for col in ['development_status','verification_status','runtime_status','overall_status','independent_reviewer_status','qa_status']:
 print(col,dict(collections.Counter(r[col] for r in rows)))
closed=sum(1 for r in rows if r['overall_status']=='완료')
print(f'PROGRESS_CLOSURE={closed}/{len(rows)} ({(closed/len(rows)*100 if rows else 0):.1f}%)')
