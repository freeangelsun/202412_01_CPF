#!/usr/bin/env python3
from __future__ import annotations
import argparse, csv, json
from pathlib import Path

def main() -> int:
    p=argparse.ArgumentParser()
    p.add_argument("--query-file", required=True)
    p.add_argument("--id-column", default="query_id")
    p.add_argument("--expected-count", type=int, required=True)
    p.add_argument("--json-output")
    a=p.parse_args()
    result={}
    try:
        path=Path(a.query_file)
        with path.open(encoding="utf-8-sig", newline="") as h:
            reader=csv.DictReader(h); fields=reader.fieldnames or []
            if a.id_column not in fields: raise ValueError(f"missing column {a.id_column}")
            ids=[(r.get(a.id_column) or "").strip() for r in reader]
        blank=sum(not x for x in ids); unique=len(set(x for x in ids if x)); duplicates=len(ids)-blank-unique
        if blank or duplicates or len(ids)!=a.expected_count:
            raise ValueError(f"count={len(ids)} expected={a.expected_count} blank={blank} duplicates={duplicates}")
        result={"status":"PASS","queryCount":len(ids),"uniqueQueryCount":unique}
        code=0
    except Exception as e:
        result={"status":"FAIL","message":str(e)}; code=1
    text=json.dumps(result,ensure_ascii=False,indent=2); print(text)
    if a.json_output: Path(a.json_output).write_text(text+"\n",encoding="utf-8")
    return code
if __name__=="__main__": raise SystemExit(main())
