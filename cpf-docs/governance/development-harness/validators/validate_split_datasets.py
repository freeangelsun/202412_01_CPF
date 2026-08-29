#!/usr/bin/env python3
from pathlib import Path
import csv,hashlib

ROOT=Path(__file__).resolve().parents[4]
H=ROOT/'cpf-docs/governance/development-harness'
errors=[]

def sha256_and_records(path: Path):
    """분할 CSV를 한 번만 스트리밍하여 SHA-256과 물리 레코드 수를 동시에 검증한다.

    CPF canonical split dataset은 1 logical record = 1 UTF-8 physical line 계약이다.
    quoted multiline이 유입되면 index의 part_record_count와 불일치하여 fail-closed 된다.
    """
    digest=hashlib.sha256(); line_count=0; last=b''
    with path.open('rb') as f:
        for chunk in iter(lambda:f.read(1024*1024),b''):
            digest.update(chunk); line_count+=chunk.count(b'\n'); last=chunk[-1:] if chunk else last
    # header 1행 제외. 마지막 행 newline이 없어도 레코드로 계산한다.
    physical_lines=line_count + (1 if path.stat().st_size and last!=b'\n' else 0)
    return digest.hexdigest(), max(physical_lines-1,0)

for name in ['CPF_REQUIREMENT_MASTER.csv','CPF_SCENARIO_MASTER.csv','CPF_EXECUTION_SEQUENCE.csv']:
    idx=H/'current'/name
    with idx.open(encoding='utf-8-sig',newline='') as f:
        rows=list(csv.DictReader(f))
    total=0
    for r in rows:
        p=ROOT/r['part_path']
        if not p.is_file():
            errors.append('missing '+r['part_path']); continue
        actual_sha,n=sha256_and_records(p)
        if actual_sha.lower()!=r['sha256'].lower(): errors.append('sha '+r['part_path'])
        if n!=int(r['part_record_count']): errors.append('count '+r['part_path'])
        total+=n
    logical=int(rows[0]['logical_record_count']) if rows else -1
    if total!=logical: errors.append(f'logical {name} expected={logical} actual={total}')
    print(f'{name} logical={logical} actual={total}')
if errors:
    [print('FAIL',e) for e in errors]
    print('SPLIT_DATASET=FAIL'); raise SystemExit(1)
print('SPLIT_DATASET=PASS')
