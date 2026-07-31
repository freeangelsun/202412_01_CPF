#!/usr/bin/env python3
"""QA32 repository-wide high-risk pattern gate. Exact allowlists are path-bound and fail closed."""
import argparse,json,re
from pathlib import Path
EXT={'.java','.kt','.kts','.groovy','.gradle','.ps1','.py','.ts','.js','.mjs','.vue','.sql','.yml','.yaml','.properties'}
IGNORE={'.git','.gradle','build','node_modules','dist','coverage','playwright-report','test-results'}
RULES={
 'REFLECTION':re.compile(r'(Class\.forName\s*\(|getDeclaredMethod\s*\(|setAccessible\s*\(\s*true)'),
 'DESERIALIZATION':re.compile(r'(ObjectInputStream|XMLDecoder|Yaml\.load\s*\(|enableDefaultTyping)'),
 'PROCESS':re.compile(r'(Runtime\.getRuntime\(\)\.exec|-ExecutionPolicy\s+Bypass)',re.I),
 'RESOURCE':re.compile(r'(readAllBytes\s*\(|Files\.readString\s*\([^\n]*upload)',re.I),
 'DYNAMIC_SQL':re.compile(r'(SELECT|UPDATE|DELETE|INSERT)[^\n]*[+][^\n]*(request|parameter|input)',re.I),
 'WEAK_CRYPTO':re.compile(r'(MD5|SHA-1|DES/ECB|AES/ECB)',re.I),
 'URL_USERINFO':re.compile(r'getUserInfo\(\)\s*==\s*null'),
}
ALLOW={
 'URL_USERINFO':{'cpf-gateway/src/main/java/com/cpf/gateway/scg/CpfScgTargetResolver.java'},
 'WEAK_CRYPTO':set(), 'REFLECTION':set(), 'DESERIALIZATION':set(), 'PROCESS':set(), 'RESOURCE':set(), 'DYNAMIC_SQL':set(),
}
def main():
 ap=argparse.ArgumentParser();ap.add_argument('--root',default='.');ap.add_argument('--json-report');a=ap.parse_args();root=Path(a.root).resolve();fail=[];checks=0
 for p in root.rglob('*'):
  if not p.is_file() or p.suffix.lower() not in EXT or any(x in IGNORE for x in p.parts):continue
  rel=p.relative_to(root).as_posix();text=p.read_text(encoding='utf-8',errors='ignore')
  if rel.startswith('cpf-tools/scripts/verify-cpf-qa32-repository-security.py'):continue
  for name,rx in RULES.items():
   checks+=1
   if rel not in ALLOW.get(name,set()) and rx.search(text):fail.append(f'{name}:{rel}')
 report={'checks':checks,'failures':fail,'status':'PASS' if not fail else 'FAIL'}
 if a.json_report:
  out=Path(a.json_report);out.parent.mkdir(parents=True,exist_ok=True);out.write_text(json.dumps(report,ensure_ascii=False,indent=2)+'\n')
 print(json.dumps(report,ensure_ascii=False,indent=2));return 0 if not fail else 1
if __name__=='__main__':raise SystemExit(main())
