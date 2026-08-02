#!/usr/bin/env python3
"""QA32 repository-wide high-risk pattern gate. Exact allowlists are path-bound and fail closed."""
import argparse,json,re
from pathlib import Path
EXT={'.java','.kt','.kts','.groovy','.gradle','.ps1','.py','.ts','.js','.mjs','.vue','.sql','.yml','.yaml','.properties'}
IGNORE={'.git','.gradle','build','node_modules','dist','coverage','playwright-report','test-results'}
RULES={
 'REFLECTION':re.compile(r'(Class\.forName\s*\(|getDeclaredMethod\s*\(|setAccessible\s*\(\s*true)'),
 'DESERIALIZATION':re.compile(r'(ObjectInputStream|XMLDecoder|Yaml\.load\s*\(|enableDefaultTyping)'),
 'PROCESS':re.compile(r'(Runtime\.getRuntime\(\)\.exec|-ExecutionPolicy(?:\s+|[\"\']?\s*,\s*[\"\']?)Bypass\b)',re.I),
 'RESOURCE':re.compile(r'(readAllBytes\s*\(|Files\.readString\s*\([^\n]*upload)',re.I),
 'WEAK_CRYPTO':re.compile(r'(MD5|SHA-1|DES/ECB|AES/ECB)',re.I),
 'URL_USERINFO':re.compile(r'getUserInfo\(\)\s*==\s*null'),
}
ALLOW={
 'URL_USERINFO':set(),
 'WEAK_CRYPTO':{
  'cpf-tools/db/vendor/postgresql/migration/flyway/cpfDB/V67__notification_durable_outbox.sql',
  'cpf-batch/worker/src/main/java/com/cpf/batch/worker/JcaScriptArtifactVerifier.java',
 },
 'REFLECTION':{
  'cpf-core/src/test/java/com/cpf/core/config/CpfOpenApiAutoConfigurationTest.java',
  'cpf-core/src/test/java/com/cpf/core/common/logging/LoggingAspectMetadataTest.java',
  'cpf-core/src/test/java/com/cpf/core/common/transaction/CpfTransactionMetaScannerTest.java',
  'cpf-core/src/test/java/com/cpf/core/common/web/TransactionHeaderValidationInterceptorTest.java',
 },
 'RESOURCE':{'cpf-tools/scripts/tests/test_verify_cpf_qa32_repository_security.py'},
 'DESERIALIZATION':set(), 'PROCESS':set(),
 'DYNAMIC_SQL':{'cpf-tools/scripts/tests/test_verify_cpf_qa32_repository_security.py'},
}
SQL_KEYWORD=re.compile(r'(?i)\b(?:SELECT|UPDATE|DELETE|INSERT)\b')
UNTRUSTED_INPUT=re.compile(r'(?i)\b(?:request|parameter|input)\w*\b')
QUOTED_SOURCE_LITERALS=(
 re.compile(r'"(?:\\.|[^"\\])*"',re.S),
 re.compile(r"'(?:\\.|[^'\\])*'",re.S),
 re.compile(r'`(?:\\.|[^`\\])*`',re.S),
)
SQL_SCRIPT_CONCAT=re.compile(
 r'(?i)\b(?:EXECUTE\s+IMMEDIATE|PREPARE\s+\w+\s+FROM)\b[^\n]*(?:\|\||\+)[^\n]*(?:request|parameter|input)\w*\b'
)
def has_dynamic_sql(path,text):
 if path.suffix.lower()=='.sql':return bool(SQL_SCRIPT_CONCAT.search(text))
 for literal_pattern in QUOTED_SOURCE_LITERALS:
  for match in literal_pattern.finditer(text):
   literal=match.group(0)
   if not SQL_KEYWORD.search(literal):continue
   if literal.startswith('`') and re.search(r'\$\{[^}]*\b(?:request|parameter|input)\w*\b',literal,re.I):return True
   line_tail=text[match.end():match.end()+320].split('\n',1)[0].split(';',1)[0]
   if re.match(r'\s*\+',line_tail) and UNTRUSTED_INPUT.search(line_tail):return True
   if re.match(r'\s*\.(?:formatted|format)\s*\(',line_tail,re.I) and UNTRUSTED_INPUT.search(line_tail):return True
   if re.match(r'\s*\)\s*\.append\s*\(',line_tail,re.I) and UNTRUSTED_INPUT.search(line_tail):return True
   prefix=text[max(0,match.start()-80):match.start()]
   if re.search(r'(?i)(?:String\s*\.\s*format|format|append)\s*\(\s*$',prefix) and UNTRUSTED_INPUT.search(line_tail):return True
 return False
def main():
 ap=argparse.ArgumentParser();ap.add_argument('--root',default='.');ap.add_argument('--json-report');a=ap.parse_args();root=Path(a.root).resolve();fail=[];checks=0
 for p in root.rglob('*'):
  if not p.is_file() or p.suffix.lower() not in EXT or any(x in IGNORE for x in p.parts):continue
  rel=p.relative_to(root).as_posix();text=p.read_text(encoding='utf-8',errors='ignore')
  if rel in {'cpf-tools/scripts/verify-cpf-qa32-repository-security.py','cpf-tools/scripts/verify-cpf-qa33-repository-closure.py'}:continue
  for name,rx in RULES.items():
   checks+=1
   if rel not in ALLOW.get(name,set()) and rx.search(text):fail.append(f'{name}:{rel}')
  checks+=1
  if rel not in ALLOW['DYNAMIC_SQL'] and has_dynamic_sql(p,text):fail.append(f'DYNAMIC_SQL:{rel}')
 report={'checks':checks,'failures':fail,'status':'PASS' if not fail else 'FAIL'}
 if a.json_report:
  out=Path(a.json_report);out.parent.mkdir(parents=True,exist_ok=True);out.write_text(json.dumps(report,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
 print(json.dumps(report,ensure_ascii=False,indent=2));return 0 if not fail else 1
if __name__=='__main__':raise SystemExit(main())
