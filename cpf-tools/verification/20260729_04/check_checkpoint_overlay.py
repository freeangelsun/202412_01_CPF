#!/usr/bin/env python3
from pathlib import Path
import csv,json,re,sys,hashlib,datetime
root=Path(sys.argv[1]).resolve() if len(sys.argv)>1 else Path(__file__).resolve().parents[3]
fail=[]; warn=[]; passed=[]
def P(n,d=''): passed.append((n,d))
def F(n,d): fail.append((n,d))
def W(n,d): warn.append((n,d))
files=[p for p in root.rglob('*') if p.is_file()]
# required files
required=['cpf-docs/quality/qa-20260729/CPF_QA_SCENARIO_200_CHECKPOINT_MATRIX_20260729_04.csv','cpf-docs/quality/qa-20260729/CPF_QA_387_SCENARIO_VALIDATION_BACKLOG_20260729_04.csv','cpf-docs/work/current/CPF_20260729_04_NEXT_SESSION_DEVELOPMENT_HANDOVER.md','cpf-docs/work/current/CPF_20260729_04_CODEX_REVIEW_ONLY_REQUEST.md','cpf-tools/db/canonical/seed-model.json','cpf-common/src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports']
missing=[x for x in required if not (root/x).is_file()]
F('required-files',','.join(missing)) if missing else P('required-files',str(len(required)))
# CSV/JSON
for p in root.rglob('*.json'):
    try: json.loads(p.read_text(encoding='utf-8-sig'))
    except Exception as e: F('json-parse',f'{p.relative_to(root)}:{e}')
for p in root.rglob('*.csv'):
    try:
        with p.open(encoding='utf-8-sig',newline='') as f: list(csv.reader(f))
    except Exception as e: F('csv-parse',f'{p.relative_to(root)}:{e}')
P('structured-files','parsed')
# checkpoint counts
p=root/'cpf-docs/quality/qa-20260729/CPF_QA_SCENARIO_200_CHECKPOINT_MATRIX_20260729_04.csv'
if p.exists():
    rows=list(csv.DictReader(p.open(encoding='utf-8-sig',newline='')))
    if len(rows)!=200 or len({r['id'] for r in rows})!=200: F('scenario-200',f'rows={len(rows)},unique={len({r["id"] for r in rows})}')
    elif any(r['checkpoint_review_status']!='검토 완료' for r in rows): F('scenario-200','review status mismatch')
    else: P('scenario-200','200 reviewed; runtime results not promoted')
p=root/'cpf-docs/quality/qa-20260729/CPF_QA_387_SCENARIO_VALIDATION_BACKLOG_20260729_04.csv'
if p.exists():
    rows=list(csv.DictReader(p.open(encoding='utf-8-sig',newline='')))
    if len(rows)!=387 or len({r['id'] for r in rows})!=387: F('scenario-387',f'rows={len(rows)}')
    else: P('scenario-387','full backlog registered')
# java package path
for p in root.rglob('*.java'):
    t=p.read_text(encoding='utf-8',errors='replace'); m=re.search(r'^package\s+([\w.]+);',t,re.M)
    if not m: F('java-package',f'no package:{p.relative_to(root)}'); continue
    exp='/'.join(m.group(1).split('.'))+'/'+p.name
    if not str(p).replace('\\','/').endswith(exp): F('java-package',f'{p.relative_to(root)}->{m.group(1)}')
P('java-package-path','checked')
# no wrong frontend path
if (root/'cpf-admin/frontend/src/app/createAdmState.ts').exists(): F('frontend-state-path','wrong app/createAdmState.ts remains')
elif not (root/'cpf-admin/frontend/src/state/createAdmState.ts').exists(): F('frontend-state-path','state/createAdmState.ts missing')
else: P('frontend-state-path','correct')
# autoconfiguration preservation
p=root/'cpf-common/src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports'
if p.exists():
    t=p.read_text(encoding='utf-8')
    req=['com.cpf.common.config.CmnRuntimeControlAutoConfiguration','com.cpf.common.cache.CpfCacheAutoConfiguration','com.cpf.common.tabular.CpfTabularAutoConfiguration']
    miss=[x for x in req if x not in t]
    F('auto-config-preservation',','.join(miss)) if miss else P('auto-config-preservation','3 entries')
# official DB parity
for v in ['mariadb','postgresql','oracle']:
  for d,names in [('flyway',['V69__enterprise_cache_file_job.sql','V70__bza_action_permission_hardening.sql']),('rollback',['R69__enterprise_cache_file_job.sql','R70__bza_action_permission_hardening.sql'])]:
    for n in names:
      q=root/f'cpf-tools/db/vendor/{v}/source/migration/{d}/{n}'
      if not q.exists(): F('db-parity',str(q.relative_to(root)))
P('db-parity','V69/V70/R69/R70 file presence checked')
# seed model policy
p=root/'cpf-tools/db/canonical/seed-model.json'
if p.exists():
    j=json.loads(p.read_text(encoding='utf-8-sig')); vendors=set(j.get('officialVendors',j.get('official_vendors',[])))
    if vendors and vendors!={'oracle','postgresql','mariadb'}: F('official-vendors',str(vendors))
    else: P('official-vendors','Oracle/PostgreSQL/MariaDB')
# hygiene
bad=[]
for p in files:
    rel=str(p.relative_to(root)).replace('\\','/')
    if p.suffix.lower() in {'.class','.jar','.tmp','.bak','.zip'}: bad.append(rel)
    if any('/'+x+'/' in '/'+rel for x in ['build','node_modules','.gradle','target','tmp']): bad.append(rel)
    if p.suffix.lower()=='.log' and not rel.startswith('cpf-docs/evidence/'): bad.append(rel)
F('hygiene',';'.join(sorted(set(bad))[:30])) if bad else P('hygiene','clean overlay')
# forbidden direct clients outside explicit adapters/gates
patterns=re.compile(r'RestTemplate|WebClient\.builder|HttpClient\.newHttpClient|new\s+Kafka(?:Consumer|Producer)|DriverManager\.getConnection')
viol=[]
for p in root.rglob('*.java'):
    rel=str(p.relative_to(root)).replace('\\','/')
    if '/adapter/' in '/'+rel or 'ConnectionFactoryBuilder' in p.name: continue
    if patterns.search(p.read_text(encoding='utf-8',errors='ignore')): viol.append(rel)
F('direct-client-boundary',';'.join(viol)) if viol else P('direct-client-boundary','0')
# internal imports outside owner
viol=[]
for p in root.rglob('*.java'):
    rel=str(p.relative_to(root)).replace('\\','/')
    if rel.startswith('cpf-core/'): continue
    if 'import com.cpf.core.common.' in p.read_text(encoding='utf-8',errors='ignore'): viol.append(rel)
F('core-internal-import',';'.join(viol)) if viol else P('core-internal-import','0')
# public raw Map is an explicit final-development warning, not hidden.
raw=[]
rx=re.compile(r'public\s+(?:ResponseEntity<)?Map<String\s*,\s*Object>|public\s+Map<String\s*,\s*Object>')
for p in root.rglob('*.java'):
    t=p.read_text(encoding='utf-8',errors='ignore')
    if rx.search(t): raw.append(str(p.relative_to(root)))
if raw: W('public-raw-map-open-gap',';'.join(raw[:40]))
else: P('public-raw-map','0')
# generator change must not be falsely claimed.
if not any((root/'cpf-tools/generator').rglob('*')): W('generator-overlay','Golden Template source changes not present in this checkpoint')
# canonical integration/checksum is open until actual tools run.
W('runtime-validation','Java25/Gradle9.1, pwsh, DB, Browser, Redis/Kafka, multi-instance were not executed in this environment')
for n,d in passed: print(f'[PASS] {n}: {d}')
for n,d in warn: print(f'[OPEN] {n}: {d}')
for n,d in fail: print(f'[FAIL] {n}: {d}')
print(f'CHECKPOINT_RESULT={"STRUCTURAL_PASS" if not fail else "FAIL"} pass={len(passed)} open={len(warn)} fail={len(fail)} final_ready=false')
sys.exit(1 if fail else 0)
