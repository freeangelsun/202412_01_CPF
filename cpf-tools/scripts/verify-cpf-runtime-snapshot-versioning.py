#!/usr/bin/env python3
"""Fail-closed static guard for monotonic runtime endpoint snapshots."""
from __future__ import annotations
import argparse,json,re
from pathlib import Path

class VersionGateError(RuntimeError): pass


def method_body(text:str, signature:str)->str:
    start=text.find(signature)
    if start<0: raise VersionGateError(f"method missing: {signature}")
    opening=text.find('{',start)
    if opening<0: raise VersionGateError('method opening brace missing')
    depth=0
    for i in range(opening,len(text)):
        if text[i]=='{': depth+=1
        elif text[i]=='}':
            depth-=1
            if depth==0:return text[opening+1:i]
    raise VersionGateError('method closing brace missing')


def verify(root:Path)->dict:
    path=root.resolve()/'cpf-starters/integration/http-client/src/main/java/com/cpf/core/common/http/CpfServiceEndpointRegistry.java'
    if not path.is_file():raise VersionGateError(f'missing {path}')
    text=path.read_text(encoding='utf-8-sig')
    body=method_body(text,'public Snapshot replaceRuntime(')
    requirements={
      'older_version_rejected':bool(re.search(r'version\s*<\s*old\.version\(\)',body)),
      'equal_version_handled':bool(re.search(r'version\s*==\s*old\.version\(\)',body)),
      'equal_payload_compared':bool(re.search(r'(?:normalized\.equals\(old\.endpoints\(\)\)|old\.endpoints\(\)\.equals\(normalized\))',body)),
      'equal_conflict_rejected':bool(re.search(r'version\s*==\s*old\.version\(\).*?throw\s+new\s+IllegalArgumentException',body,re.S)),
      'atomic_compare_and_set': 'runtime.compareAndSet(old, next)' in body,
    }
    missing=[name for name,passed in requirements.items() if not passed]
    result={'status':'PASS' if not missing else 'FAIL','file':path.relative_to(root.resolve()).as_posix(),'checks':requirements,'findings':missing}
    if missing:raise VersionGateError(json.dumps(result,ensure_ascii=False,indent=2))
    return result


def main()->int:
    p=argparse.ArgumentParser();p.add_argument('--root',default='.');p.add_argument('--json-output');a=p.parse_args()
    try:r=verify(Path(a.root));code=0
    except Exception as e:
      try:r=json.loads(str(e))
      except:r={'status':'FAIL','message':str(e)}
      code=1
    text=json.dumps(r,ensure_ascii=False,indent=2)
    if a.json_output:
      out=Path(a.json_output);out=out if out.is_absolute() else Path(a.root).resolve()/out;out.parent.mkdir(parents=True,exist_ok=True);out.write_text(text+'\n',encoding='utf-8')
    print(text);return code
if __name__=='__main__':raise SystemExit(main())
