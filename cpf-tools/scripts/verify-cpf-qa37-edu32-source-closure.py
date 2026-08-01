#!/usr/bin/env python3
"""Fail-closed QA37 source closure for EDU-001..EDU-032.

Development mode resolves Source/Test/Public Contract globs and runtime command targets.
Release mode adds exact-SHA, successful runtime evidence and completed verification status.
`--catalog-contract-only` exists solely for overlay-package validation before it is applied to
its base repository; it never reports merged-repository source closure as successful.
"""
from __future__ import annotations
import argparse,csv,glob,json,re,shlex,sys,tempfile
from collections import Counter
from pathlib import Path

EXPECTED=[f"EDU-{i:03d}" for i in range(1,33)]
PLACEHOLDER=re.compile(r"^(?:|TODO|TBD|미구현|미수집|N/?A|NONE|-)$",re.I)
INTERNAL_IMPORT=re.compile(r"^\s*import\s+com\.cpf\.[\w.]*\.internal(?:\.|;)",re.M)
JAVA_CONCRETE=re.compile(r"\b(?:public\s+)?(?:final\s+|abstract\s+)?(?:class|record|enum)\s+[A-Za-z_$][\w$]*")
JAVA_INTERFACE=re.compile(r"\b(?:public\s+)?interface\s+[A-Za-z_$][\w$]*")
JAVA_METHOD=re.compile(r"\b(?:public|protected|private)\s+(?:static\s+)?(?:final\s+)?[\w<>, ?\[\].]+\s+[a-zA-Z_$][\w$]*\s*\(")
TEST_TOKEN=re.compile(r"@Test\b|\bdef\s+test_[A-Za-z0-9_]+\s*\(|\b(?:test|it|describe)\s*\(")
FIXED_SUCCESS=re.compile(r"Map\.of\s*\(\s*[\"']status[\"']\s*,\s*[\"'](?:ok|success)[\"']|return\s+[\"']\{[^\n]*(?:success|ok)[^\n]*\}[\"']",re.I)
DOC_PARTS={"readme.md","manual.md","guide.md"}
ALLOWED={"완료","부분 구현","미구현","미검증","실패","재확인 필요"}
SCRIPT_EXT={'.py','.ps1','.sh','.cmd','.bat','.gradle','.kts','.sql','.json','.xml','.yml','.yaml','.js','.mjs','.ts','.vue'}

def read_csv(p:Path):
 with p.open(encoding='utf-8-sig',newline='') as f:return list(csv.DictReader(f))
def resolve(root:Path,patterns:list[str]):
 found=[]
 for pat in patterns:
  found += [Path(x) for x in glob.glob(str(root/pat),recursive=True) if Path(x).is_file()]
 return sorted(set(found))
def rel(root:Path,p:Path):
 try:return p.resolve().relative_to(root.resolve()).as_posix()
 except ValueError:return p.as_posix()
def text(p:Path):return p.read_text(encoding='utf-8',errors='replace')
def meaningful(v):return isinstance(v,str) and not PLACEHOLDER.match(v.strip())
def is_doc_only(files):
 return bool(files) and all(p.name.lower() in DOC_PARTS or '/docs/' in p.as_posix().lower() or '/guides/' in p.as_posix().lower() for p in files)
def has_concrete_source(files):
 for p in files:
  s=p.suffix.lower();t=text(p)
  if s=='.java' and JAVA_CONCRETE.search(t) and (JAVA_METHOD.search(t) or ' static void main(' in t):return True
  if s in SCRIPT_EXT and p.name.lower() not in DOC_PARTS and len(t.strip())>40:return True
 return False
def runtime_target(root:Path,command:str):
 # A Gradle task command is rooted by gradlew; npm --prefix is rooted by package.json.
 low=command.lower()
 if 'gradlew' in low:return (root/'gradlew.bat').exists() or (root/'gradlew').exists()
 if 'npm --prefix' in low:
  m=re.search(r'npm\s+--prefix\s+([^\s]+)',command,re.I)
  return bool(m and (root/m.group(1).strip('"\'')/'package.json').exists())
 # Locate explicit .py/.ps1/.sh/.cmd/.bat target.
 m=re.search(r'(?:(?:python|python3|pwsh|powershell|bash|sh)\s+(?:-[^\s]+\s+)*)?([.\\/\w-]+\.(?:py|ps1|sh|cmd|bat))',command,re.I)
 if not m:return True
 target=m.group(1).replace('\\','/').lstrip('./')
 return (root/target).is_file()
def validate_catalog_contract(catalog,mapping_rows):
 errors=[];fs=catalog.get('features') or [];ids=[f.get('featureId') for f in fs]
 if catalog.get('featureCount')!=32:errors.append('featureCount must be 32')
 if ids!=EXPECTED:errors.append('feature IDs must be continuous EDU-001..EDU-032')
 policy=catalog.get('sourceClosurePolicy') or {}
 for k in ['resolveSourceInDevelopment','resolveTestsInDevelopment','resolvePublicContractsInDevelopment','rejectDocumentationOnly','rejectInterfaceOnly','rejectConsumerlessAbstraction','rejectFixedResponseOrMockOnly','rejectInternalPackageImport','requireRuntimeCommandTarget','releaseRequiresExactShaEvidence']:
  if policy.get(k) is not True:errors.append('sourceClosurePolicy must enable '+k)
 for f in fs:
  fid=f.get('featureId','<missing>')
  for k in ['area','coverageScope','ownerModule','sourceBaselineSha']:
   if not meaningful(f.get(k)):errors.append(f'{fid}: {k} missing')
  for k in ['referenceSources','tests','publicContracts','runtimeCommands']:
   if not isinstance(f.get(k),list) or not f[k] or any(not meaningful(v) for v in f[k]):errors.append(f'{fid}: {k} must contain concrete values')
  if not {'normal','error','recovery'}.issubset(set((f.get('scenarioAxes') or {}).keys())):errors.append(f'{fid}: normal/error/recovery scenario axes required')
  if f.get('developmentStatus') not in ALLOWED or f.get('verificationStatus') not in ALLOWED:errors.append(f'{fid}: invalid status')
 if len(mapping_rows)!=32 or [r.get('feature_id') for r in mapping_rows]!=EXPECTED:errors.append('EDU32 mapping must contain exactly ordered 32 rows')
 used=Counter(r.get('representative_source') for r in mapping_rows)
 for r in mapping_rows:
  fid=r.get('feature_id','<missing>')
  for k in ['representative_source','class_or_script','method_or_entry','actual_consumer','public_contract_globs','test_globs','runtime_command','reuse_justification']:
   if not meaningful(r.get(k)):errors.append(f'{fid}: mapping {k} missing')
  if used[r.get('representative_source')]>2:errors.append(f'{fid}: unsupported bulk representative mapping: {r.get("representative_source")}')
 return errors

def validate(root:Path,catalog_path:Path,mapping_path:Path,release:bool,contract_only:bool):
 errors=[]
 try:catalog=json.loads(catalog_path.read_text(encoding='utf-8-sig'))
 except Exception as e:return [f'catalog unreadable: {e}']
 try:mapping=read_csv(mapping_path)
 except Exception as e:return [f'mapping unreadable: {e}']
 errors += validate_catalog_contract(catalog,mapping)
 if contract_only:return errors
 map_by={r['feature_id']:r for r in mapping}
 for f in catalog.get('features') or []:
  fid=f['featureId'];m=map_by.get(fid,{})
  src=resolve(root,f['referenceSources']);tests=resolve(root,f['tests']);pub=resolve(root,f['publicContracts'])
  if not src:errors.append(f'{fid}: referenceSources resolved 0 files')
  elif is_doc_only(src):errors.append(f'{fid}: documentation-only source closure')
  elif not has_concrete_source(src):errors.append(f'{fid}: no concrete class/record/enum/method or executable script')
  if not tests:errors.append(f'{fid}: tests resolved 0 files')
  elif not any(TEST_TOKEN.search(text(p)) or p.suffix.lower()=='.sql' and 'verify' in p.as_posix().lower() for p in tests):errors.append(f'{fid}: resolved tests contain no executable test/verify entry')
  if not pub:errors.append(f'{fid}: publicContracts resolved 0 files')
  for p in src:
   t=text(p)
   if INTERNAL_IMPORT.search(t):errors.append(f'{fid}: Internal package import: {rel(root,p)}')
   if 'cpf-reference' in p.as_posix() and FIXED_SUCCESS.search(t):errors.append(f'{fid}: fixed success response: {rel(root,p)}')
  representative=root/(m.get('representative_source') or '')
  if not representative.is_file():errors.append(f'{fid}: representative source missing: {m.get("representative_source")}')
  else:
   rt=text(representative)
   cls=m.get('class_or_script','');entry=m.get('method_or_entry','')
   if representative.suffix=='.java' and cls not in rt:errors.append(f'{fid}: representative class not found: {cls}')
   # Entry may describe multiple endpoints/beans; require literal only when it is a valid Java identifier.
   if representative.suffix=='.java' and re.fullmatch(r'[A-Za-z_$][\w$]*',entry or '') and entry not in rt:errors.append(f'{fid}: representative method/entry not found: {entry}')
  for cmd in f['runtimeCommands']:
   if not runtime_target(root,cmd):errors.append(f'{fid}: runtime command target missing: {cmd}')
  if f.get('developmentStatus')!='완료':errors.append(f'{fid}: developmentStatus must be 완료 after QA37 source re-evaluation')
  if release:
   if f.get('verificationStatus')!='완료':errors.append(f'{fid}: release verificationStatus must be 완료')
   ev=root/'cpf-docs/evidence/qa37/edu32'/f'{fid}.json'
   if not ev.is_file():errors.append(f'{fid}: release exact-SHA evidence missing')
   else:
    try:d=json.loads(ev.read_text(encoding='utf-8'))
    except Exception as e:errors.append(f'{fid}: evidence unreadable: {e}');continue
    if d.get('featureId')!=fid or not re.fullmatch(r'[0-9a-f]{40}',str(d.get('sourceSha',''))) or d.get('exitCode')!=0 or d.get('sanitized') is not True:errors.append(f'{fid}: invalid release evidence')
 return errors

def self_test():
 # Verify fail-closed helpers without creating fake product evidence.
 d=Path(tempfile.mkdtemp(prefix='cpf-edu32-gate-'))
 (d/'A.java').write_text('public final class A { public void run(){} }',encoding='utf-8')
 (d/'I.java').write_text('public interface I {}',encoding='utf-8')
 (d/'T.java').write_text('class T { @Test void x(){} }',encoding='utf-8')
 assert has_concrete_source([d/'A.java'])
 assert not has_concrete_source([d/'I.java'])
 assert TEST_TOKEN.search(text(d/'T.java'))
 assert is_doc_only([d/'README.md']) if (d/'README.md').write_text('x',encoding='utf-8') is not None else False
 print('[CPF][QA37][EDU32][PASS] verifier-self-test')

def main():
 ap=argparse.ArgumentParser();ap.add_argument('--root',type=Path,default=Path.cwd());ap.add_argument('--catalog',type=Path);ap.add_argument('--mapping',type=Path);ap.add_argument('--release',action='store_true');ap.add_argument('--overlay-contract',action='store_true');ap.add_argument('--merged-root',action='store_true');ap.add_argument('--catalog-contract-only',action='store_true',help='deprecated alias for --overlay-contract');ap.add_argument('--self-test',action='store_true');a=ap.parse_args()
 if a.self_test:self_test()
 root=a.root.resolve();catalog=a.catalog or root/'cpf-tools/governance/cpf-edu-executable-catalog.json';mapping=a.mapping or root/'cpf-docs/quality/CPF_20260801_QA37_EDU32_SOURCE_MAPPING.csv'
 contract_only=a.overlay_contract or a.catalog_contract_only
 if contract_only and (a.merged_root or a.release):
  print('[CPF][QA37][EDU32][FAIL] overlay-contract cannot be combined with merged/release mode',file=sys.stderr);return 2
 if not contract_only:
  markers=[root/'settings.gradle',root/'gradlew.bat',root/'cpf-core',root/'cpf-common',root/'cpf-reference']
  if not all(x.exists() for x in markers):
   print('[CPF][QA37][EDU32][FAIL] merged-root mode requires full repository markers; use --overlay-contract only for package-shape validation',file=sys.stderr);return 2
 errors=validate(root,catalog,mapping,a.release,contract_only)
 for e in errors:print('[CPF][QA37][EDU32][FAIL]',e,file=sys.stderr)
 if errors:return 1
 mode='overlay-contract-only-NOT-source-closure' if contract_only else ('release-exact-sha' if a.release else 'merged-root-development-source-closure')
 print(f'[CPF][QA37][EDU32][PASS] features=32 mode={mode}')
 return 0
if __name__=='__main__':raise SystemExit(main())
