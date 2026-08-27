#!/usr/bin/env python3
from __future__ import annotations
import argparse, json, sys
from pathlib import Path

ERROR = 8
WARNING = 4


def main() -> int:
    ap = argparse.ArgumentParser(description='Verify exported VS Code Problems JSON is Error 0 / Warning 0.')
    ap.add_argument('--input', type=Path, required=True)
    ap.add_argument('--output', type=Path)
    args = ap.parse_args()
    if not args.input.is_file():
        print(json.dumps({'status':'FAIL','reason':'problems-json-missing','input':args.input.name}, ensure_ascii=False))
        return 1
    try:
        data = json.loads(args.input.read_text(encoding='utf-8-sig'))
    except Exception as exc:
        print(json.dumps({'status':'FAIL','reason':'invalid-json','message':str(exc)}, ensure_ascii=False))
        return 1
    if not isinstance(data, list):
        print(json.dumps({'status':'FAIL','reason':'problems-json-must-be-array'}, ensure_ascii=False))
        return 1
    errors=[]; warnings=[]; other=[]
    for row in data:
        if not isinstance(row, dict):
            other.append({'message':'non-object diagnostic'})
            continue
        view={k:row.get(k) for k in ('resource','code','severity','message','source','startLineNumber')}
        sev=row.get('severity')
        if sev == ERROR: errors.append(view)
        elif sev == WARNING: warnings.append(view)
        else: other.append(view)
    result={'status':'PASS' if not errors and not warnings else 'FAIL','diagnostics':len(data),'errors':len(errors),'warnings':len(warnings),'other':len(other),'errorDetails':errors,'warningDetails':warnings}
    rendered=json.dumps(result,ensure_ascii=False,indent=2)
    if args.output:
        args.output.parent.mkdir(parents=True,exist_ok=True)
        args.output.write_text(rendered+'\n',encoding='utf-8')
    print(rendered)
    return 0 if result['status']=='PASS' else 1

if __name__ == '__main__':
    sys.exit(main())
