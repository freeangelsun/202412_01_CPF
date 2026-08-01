#!/usr/bin/env python3
"""Integrated low-cost QA37 source closure.

Default mode is for the repository after this overlay is applied. `--overlay-package`
validates the overlay package itself without pretending unchanged base files are present.
"""
from __future__ import annotations
import argparse,csv,hashlib,json,os,re,subprocess,sys
from pathlib import Path
BASE='1edd96c6dcc69b0b4d6e9e22a0709d910d7cfb04'
PROTECTED={'README.md','cpf-docs/guides/00_프레임워크안내.md','cpf-docs/guides/01_개발자매뉴얼.md','cpf-docs/guides/02_배치개발매뉴얼.md','cpf-docs/guides/03_ADM개발자매뉴얼.md','cpf-docs/guides/04_ADM운영자매뉴얼.md','cpf-docs/guides/05_플랫폼운영매뉴얼.md','cpf-docs/guides/90_BZA매뉴얼.md','cpf-docs/guides/91_Gateway매뉴얼.md'}
BANNED_DIRS={'build','node_modules','dist','coverage','playwright-report','test-results','__pycache__'}
SECRET_PATTERNS=[re.compile(r'(?i)(?:password|passwd|secret|token|api[_-]?key)\s*[:=]\s*["\']?(?!\$\{|<|REDACTED|CHANGE_ME|__SET_BY_SECRET_PROVIDER__|\*{3})[A-Za-z0-9+/=_-]{12,}'),re.compile(r'AKIA[0-9A-Z]{16}'),re.compile(r'-----BEGIN (?:RSA |EC |OPENSSH )?PRIVATE KEY-----')]

def fail(m):print('[CPF][QA37][SOURCE][FAIL] '+m,file=sys.stderr);raise SystemExit(1)
def run(cmd,timeout=180):
 print('[CPF][QA37][SOURCE][RUN] '+' '.join(map(str,cmd)))
 r=subprocess.run(cmd,text=True,capture_output=True,timeout=timeout)
 if r.stdout:print(r.stdout,end='')
 if r.stderr:print(r.stderr,end='',file=sys.stderr)
 if r.returncode:fail(f'command failed exit={r.returncode}: {cmd}')
def read_json(p):
 try:return json.loads(p.read_text(encoding='utf-8-sig'))
 except Exception as e:fail(f'JSON invalid {p}: {e}')
def check_csv(p):
 try:
  with p.open(encoding='utf-8-sig',newline='') as f:
   rows=list(csv.reader(f))
  if not rows or not rows[0]:fail(f'CSV empty {p}')
 except Exception as e:fail(f'CSV invalid {p}: {e}')
def check_build(root):
 p=root/'build.gradle'
 if not p.is_file():fail('root build.gradle missing')
 t=p.read_text(encoding='utf-8',errors='replace')
 if len(t.splitlines())<1000:fail('root build.gradle is not platform root contract')
 for token in ['allprojects','subprojects','qualityGate','publishing','cpfSourceSha','cpfArtifactMode','qa37SourceClosure','qa37JavaLifecycle']:
  if token not in t:fail('root build.gradle missing '+token)
 if 'JavaLanguageVersion.of(rootProject.ext.cpfJavaVersion)' not in t or "ext.cpfJavaVersion" not in t:
  fail('root build.gradle missing Java 25 toolchain indirection')
 required=['cpf-tools/build/gradle-plugin/build.gradle','cpf-tools/build/gradle-plugin/settings.gradle','cpf-tools/build/gradle-plugin/src/main/java/com/cpf/gradle/CpfPlatformConventionPlugin.java','cpf-tools/build/gradle-plugin/src/test/java/com/cpf/gradle/CpfPlatformConventionPluginTest.java','cpf-tools/build/platform-bom/build.gradle','cpf-tools/build/platform-bom/settings.gradle']
 for r in required:
  if not (root/r).is_file():fail('included build source missing '+r)
 if (root/'cpf-biz-admin/build.gradle').is_file() and p.read_bytes()==(root/'cpf-biz-admin/build.gradle').read_bytes():fail('root build.gradle still equals BZA build')
def check_frontend(root,overlay):
 for module in ['cpf-admin','cpf-biz-admin']:
  base=root/module/'frontend';pkg=read_json(base/'package.json');lock=read_json(base/'package-lock.json')
  if pkg.get('packageManager')!='npm@10.9.2':fail(module+' packageManager mismatch')
  if pkg.get('engines',{}).get('node')!='>=22.18.0 <25':fail(module+' Node engine mismatch')
  verify=pkg.get('scripts',{}).get('verify','')
  ordered=['verify:lock','verify:installed','verify:primary','test:openapi:lifecycle','generate:api','verify:generated','verify:consumer','lint','typecheck','test','build:prod']
  commands=[part.strip() for part in verify.split('&&')]
  expected=['npm run '+x for x in ordered]
  if commands!=expected:fail(module+' frontend verify lifecycle order invalid: '+repr(commands))
  npmrc=(base/'.npmrc').read_text(encoding='utf-8')
  if 'strict-peer-deps=true' not in npmrc or 'legacy-peer-deps=false' not in npmrc:fail(module+' npm peer policy invalid')
  if lock.get('lockfileVersion')!=3:fail(module+' lockfileVersion must be 3')
  rootpkg=(lock.get('packages') or {}).get('',{})
  if rootpkg.get('dependencies')!=pkg.get('dependencies') or rootpkg.get('devDependencies')!=pkg.get('devDependencies'):fail(module+' package/lock dependency drift')
  if not overlay:
   lifecycle=base/'scripts/test-openapi-lifecycle.mjs'
   if lifecycle.is_file():run(['node',str(lifecycle)],60)
def check_package(root):
 manifest=root/'cpf-docs/work/manifest/CPF_20260801_QA37_PACKAGE_MANIFEST.json'
 d=read_json(manifest)
 if d.get('baseSha')!=BASE:fail('package manifest base SHA mismatch')
 if d.get('overallStatus') not in {'미검증','재확인 필요'}:fail('package overall status must remain unverified before exact result evidence')
 package_id=str(d.get('packageId','')).upper()
 if 'FULL' in package_id or 'COMPLETION' in package_id:fail('package id overclaims completion')
 actual_files=sum(1 for p in root.rglob('*') if p.is_file())
 if d.get('fileCount')!=actual_files:fail(f'package fileCount drift manifest={d.get("fileCount")} actual={actual_files}')
 excluded=set(d.get('protectedDocsExcluded') or [])
 if not PROTECTED.issubset(excluded):fail('protected README/Guide exclusion contract incomplete')
 delete_path=root/'cpf-docs/work/manifest/CPF_20260801_QA37_DELETE_MANIFEST.txt'
 delete=[x.strip() for x in delete_path.read_text(encoding='utf-8-sig').splitlines() if x.strip()]
 if not delete or delete==['NONE']:fail('stale tracked document delete manifest missing')
 if len(delete)!=len(set(delete)):fail('delete manifest contains duplicates')
 if d.get('deleteCandidateCount')!=len(delete):fail('package deleteCandidateCount drift')
 protected_tokens=['CPF_CUSTOMER_MANUAL_EDU_IMPLEMENTATION_REQUIREMENTS.md','CPF_20260801_QA37_EDU_SOURCE_CLOSURE_AND_RECOVERY_REQUEST.md','CPF_20260801_QA37_DEVELOPMENT_GPT_PROMPT.md','CPF_FINAL_TARGET_REQUIREMENTS.md','README.md','cpf-docs/guides/']
 for rel in delete:
  if not rel.startswith('cpf-docs/work/current/'):fail('delete target outside reviewed current-work scope: '+rel)
  if any(x in rel for x in protected_tokens):fail('protected delete target: '+rel)
 review=root/'cpf-docs/quality/CPF_20260801_QA37_STALE_CURRENT_DOCUMENT_REVIEW.csv'
 reviewed={r.get('path') for r in csv.DictReader(review.open(encoding='utf-8-sig',newline=''))}
 if set(delete)!=reviewed:fail('delete manifest and stale-document review drift')
 overlay=read_json(root/'cpf-docs/work/manifest/CPF_20260801_QA37_ROOT_OVERLAY_MANIFEST.json')
 if overlay.get('deleteManifest')!='cpf-docs/work/manifest/CPF_20260801_QA37_DELETE_MANIFEST.txt':fail('root overlay deleteManifest path invalid')
 if overlay.get('fileCount')!=actual_files:fail('root overlay fileCount drift')
 change=(root/'cpf-docs/work/manifest/CPF_20260801_QA37_CHANGE_MANIFEST.csv').read_text(encoding='utf-8-sig')
 if 'CPF_20260801_QA37_COMPLETION_REPORT.md' in change:fail('stale completion report remains in change manifest')
 workflow=(root/'.github/workflows/cpf-qa37-source-closure.yml').read_text(encoding='utf-8')
 if 'gradlew.bat tasks' in workflow or 'qualityGate qa37SourceClosure' in workflow:fail('CI repeats Gradle task discovery or frontend-containing qualityGate')
 if 'clean qa37JavaLifecycle' not in workflow:fail('CI Java lifecycle task missing')
def check_syntax(root):
 for p in root.rglob('*.json'):read_json(p)
 for p in root.rglob('*.csv'):check_csv(p)
 for p in root.rglob('*'):
  rp=p.relative_to(root).as_posix()
  if p.is_dir() and p.name in BANNED_DIRS and not (rp=='cpf-tools/build' or rp.startswith('cpf-tools/build/')):fail('generated garbage directory included: '+rp)
def check_secrets(root):
 findings=[]
 for p in root.rglob('*'):
  if not p.is_file() or p.suffix.lower() in {'.zip','.jar','.class','.png','.jpg','.jpeg','.gif','.ico'}:continue
  if p.stat().st_size>3_000_000:continue
  s=p.read_text(encoding='utf-8',errors='ignore')
  for pat in SECRET_PATTERNS:
   if pat.search(s):findings.append(p.relative_to(root).as_posix());break
 if findings:fail('possible secret material: '+', '.join(findings[:10]))
def main():
 ap=argparse.ArgumentParser();ap.add_argument('--root',type=Path,default=Path.cwd());ap.add_argument('--overlay-package',action='store_true');ap.add_argument('--skip-java-compile',action='store_true');a=ap.parse_args();root=a.root.resolve()
 check_build(root);check_package(root);check_frontend(root,a.overlay_package);check_syntax(root);check_secrets(root)
 py=sys.executable
 if a.overlay_package:
  run([py,str(root/'cpf-tools/scripts/verify-cpf-qa37-package-integrity.py'),'--root',str(root)],180)
 # Truth and ownership gates are prerequisites. Stop before compilation/DB/Frontend if they fail.
 run([py,str(root/'cpf-tools/scripts/verify-cpf-qa37-completion-truth.py'),'--root',str(root)],120)
 run([py,str(root/'cpf-tools/scripts/verify-cpf-reference-package-layout.py'),'--root',str(root)],120)
 run([py,str(root/'cpf-tools/scripts/verify-cpf-reference-feature-isolation.py'),'--root',str(root)],120)
 run([py,str(root/'cpf-tools/scripts/verify-cpf-reference-feature-removal.py'),'--root',str(root)],120)
 run([py,str(root/'cpf-tools/scripts/verify-cpf-qa37-consumer-bindings.py'),'--root',str(root)],120)
 cmd=[py,str(root/'cpf-tools/scripts/verify-cpf-qa37-manual-edu-135.py'),'--root',str(root)]
 if not a.skip_java_compile:cmd.append('--compile')
 run(cmd,240)
 run([py,str(root/'cpf-tools/scripts/verify-cpf-qa37-db-generator-parity.py'),'--root',str(root),'--mode',('overlay' if a.overlay_package else 'auto')],180)
 edu=[py,str(root/'cpf-tools/scripts/verify-cpf-qa37-edu32-source-closure.py'),'--root',str(root),'--self-test']
 edu.append('--overlay-contract' if a.overlay_package else '--merged-root')
 run(edu,240)
 if a.overlay_package:
  print(f'[CPF][QA37][PACKAGE-CONTRACT][PASS] mergedSourceClosure=NOT_EXECUTED baseSha={BASE}')
 else:
  print(f'[CPF][QA37][MERGED-SOURCE][PASS] baseSha={BASE}')
 return 0
if __name__=='__main__':raise SystemExit(main())
