#!/usr/bin/env python3
import hashlib,json,sys
from pathlib import Path
H=Path(__file__).resolve().parents[1]; ROOT=H.parents[2]; C=json.loads((H/'source-currentization.json').read_text(encoding='utf-8'))
def files():
 out=[]
 for p in ROOT.rglob('*'):
  if not p.is_file(): continue
  rel=p.relative_to(ROOT).as_posix()
  if rel in C.get('excludeExactPaths',[]): continue
  if any(rel.startswith(x) for x in C['excludePrefixes']): continue
  if '/build/' in rel or rel.startswith('build/') or rel.endswith('.class') or rel.endswith('.jar'): continue
  out.append(p)
 return sorted(out,key=lambda p:p.relative_to(ROOT).as_posix())
def sha_file(p): return hashlib.sha256(p.read_bytes()).hexdigest().upper()
def main():
 phase=sys.argv[1] if len(sys.argv)>1 else 'preAuthoring'; fs=files(); h=hashlib.sha256()
 for p in fs:
  rel=p.relative_to(ROOT).as_posix(); h.update(rel.encode()); h.update(b'\0'); h.update(bytes.fromhex(sha_file(p)))
 data={'schemaVersion':'1.0','harnessVersion':C['harnessVersion'],'phase':phase,'sourceFiles':len(fs),'sourceFingerprint':h.hexdigest().upper(),'canonical':{x:sha_file(ROOT/x) for x in C['canonicalPaths'] if (ROOT/x).is_file()}}
 out=ROOT/'cpf-docs/deliverables/documentation'/f'SOURCE_CURRENTIZATION_{phase.upper()}.json'; out.write_text(json.dumps(data,ensure_ascii=False,indent=2)+'\n',encoding='utf-8'); print('SOURCE_CURRENTIZATION_CAPTURE='+phase); print('SOURCE_FINGERPRINT='+data['sourceFingerprint']); print('SOURCE_FILES='+str(len(fs)))
if __name__=='__main__': main()
