#!/usr/bin/env python3
from pathlib import Path
import csv,re,sys,argparse
ap=argparse.ArgumentParser();ap.add_argument('--root',default='.');a=ap.parse_args();root=Path(a.root).resolve();fail=[]
contract=root/'cpf-tools/verification/admin-dependency-contract.csv'
if not contract.is_file(): fail.append('ADMIN_DEPENDENCY_CONTRACT_MISSING'); rows=[]
else:
 with contract.open(encoding='utf-8-sig',newline='') as f: rows=list(csv.DictReader(f))
by={}
for r in rows: by.setdefault(r['module'],set()).add(r['dependency'])
for module in ['cpf-admin','cpf-backoffice/online']:
 p=root/module/'build.gradle'
 if not p.is_file(): fail.append('BUILD_MISSING:'+module);continue
 text=p.read_text(encoding='utf-8')
 deps=set(re.findall(r"project\('([^']+)'\)",text))
 # Every project dependency must be classified with consumer/evidence; no hidden graph inflation.
 undocumented=sorted(deps-by.get(module,set()))
 if undocumented: fail.extend('UNCLASSIFIED_DEPENDENCY:'+module+':'+x for x in undocumented)
 stale=sorted(by.get(module,set())-deps)
 if stale: fail.extend('CONTRACT_DEPENDENCY_MISSING:'+module+':'+x for x in stale)
 if module == 'cpf-admin' and ':starters:profiles:bff' not in deps: fail.append('PROFILE_MISSING:'+module)
 if module == 'cpf-backoffice/online' and 'cpf-starter-secure-api' not in text: fail.append('PUBLIC_PROFILE_GAV_MISSING:'+module)
 for dup in [':framework:core',':cpf-foundation',':cpf-security-core',':cpf-starter-foundation-base',':cpf-starter-profile-minimal-domain']:
  if dup in deps: fail.append('COMMON_SUBSTRATE_DUPLICATE:'+module+':'+dup)
 # Provider leaves that ADM/BZA do not own or consume must never enter admin apps implicitly.
 forbidden=[':starters:messaging:kafka',':starters:messaging:rabbitmq',':starters:messaging:jms',':starters:messaging:ibm-mq',':starters:cache:valkey',':starters:data:lock:valkey',':starters:file:object-storage:s3',':cpf-starter-oidc-login',':cpf-starter-data-transaction-jta']
 for d in forbidden:
  if d in deps: fail.append('UNAPPROVED_PROVIDER_PIN:'+module+':'+d)
 # Applications must not expose capability dependencies through api.
 if re.search(r'\bapi\s+project\(',text): fail.append('ADMIN_API_PROJECT_DEPENDENCY:'+module)
# ADM application must never pin internal provider leaves directly. One product-internal composition owns the exact provider set.
admin_build=(root/'cpf-admin/build.gradle').read_text(encoding='utf-8')
if re.search(r"project\('(:internal:[^']+)'\)", admin_build): fail.append('ADM_DIRECT_INTERNAL_PROVIDER')
composition=root/'cpf-admin/runtime/build.gradle'
expected_internal={
 ':internal:platform-operations:runtime-control',
 ':internal:messaging:reliability:jdbc',
 ':internal:platform-operations:runtime-health:jdbc',
 ':internal:integration:webhook',
 ':internal:file:tabular:poi',
}
if not composition.is_file(): fail.append('ADM_RUNTIME_COMPOSITION_MISSING')
else:
 composition_text=composition.read_text(encoding='utf-8')
 actual=set(re.findall(r"project\('(:internal:[^']+)'\)",composition_text))
 if actual!=expected_internal:
  fail.append('ADM_RUNTIME_COMPOSITION_DRIFT:expected='+','.join(sorted(expected_internal))+':actual='+','.join(sorted(actual)))
# Contract rows need concrete rationale, not label-only allowlist.
for r in rows:
 if not r.get('consumer_path','').strip() or not r.get('evidence','').strip(): fail.append('EVIDENCE_INCOMPLETE:'+r.get('module','')+':'+r.get('dependency',''))
print('CPF_ADMIN_DEPENDENCY_BOUNDARIES='+('PASS' if not fail else 'FAIL')+' failures='+str(len(fail)))
for x in sorted(set(fail)): print(x)
sys.exit(1 if fail else 0)
