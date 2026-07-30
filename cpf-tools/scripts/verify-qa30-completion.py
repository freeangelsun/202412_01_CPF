#!/usr/bin/env python3
"""CPF QA30 completion static gate.

Validates canonical DB/source/lifecycle parity, runtime SQL packs, changed Java
ownership hygiene, and frontend relative imports using only the Python standard
library. It deliberately fails closed and never updates checksums or artifacts.
"""
from __future__ import annotations
import argparse, hashlib, json, re, sys
from collections import defaultdict, deque
from pathlib import Path

VENDORS=("mariadb","postgresql","oracle")
FILE_BY_DB={"cpfDB":"10_cpf_schema.sql","cmnDB":"20_cmn_schema.sql","admDB":"30_adm_schema.sql","batDB":"35_bat_schema.sql","bzaDB":"40_business_modules_schema.sql","refDB":"40_business_modules_schema.sql"}
RUNTIME_SQL=("scheduler-execution-insert.sql","scheduler-find-due.sql","worker-attempt-finish.sql","worker-attempt-insert.sql","worker-execution-find-ready-candidates.sql","worker-execution-load.sql","worker-execution-requeue-retryable.sql")
REQUIRED_TABLE_COLUMNS={
 "bat_job":{"published_definition_version","published_definition_checksum","executor_reference","definition_published_at"},
 "bat_schedule":{"definition_version","definition_checksum"},
 "bat_execution":{"definition_version","definition_checksum"},
 "bat_execution_attempt":{"attempt_id","execution_id","attempt_no","definition_version","definition_checksum","worker_id","fencing_token","attempt_status"},
 "bat_job_definition_audit":{"audit_id","requested_by","approval_request_id","transaction_id","trace_id","before_json","after_json"},
}

def norm_col(value:str)->str:
 return re.sub(r"\s*\([^)]*\)\s*$","",value.strip()).strip('`"').lower()

def create_blocks(sql:str):
 out={}; pat=re.compile(r'^\s*CREATE\s+TABLE\s+(?:IF\s+NOT\s+EXISTS\s+)?(?:"?\w+"?\.)?"?([A-Za-z0-9_$#]+)"?\s*\(',re.I|re.M)
 for m in pat.finditer(sql):
  depth=1;i=m.end();quote=None
  while i<len(sql) and depth:
   c=sql[i]
   if quote:
    if c==quote and sql[i-1]!='\\': quote=None
   elif c in "'\"": quote=c
   elif c=='(': depth+=1
   elif c==')': depth-=1
   i+=1
  if depth: raise ValueError(f"unterminated CREATE TABLE {m.group(1)}")
  semi=sql.find(';',i)
  if semi<0: raise ValueError(f"missing semicolon after CREATE TABLE {m.group(1)}")
  out[m.group(1).lower()]={"body":sql[m.end():i-1],"start":m.start(),"end":semi+1}
 return out

def split_top_level(body:str):
 parts=[];start=0;depth=0;quote=None
 for i,c in enumerate(body):
  if quote:
   if c==quote and body[i-1]!='\\': quote=None
  elif c in "'\"": quote=c
  elif c=='(': depth+=1
  elif c==')': depth-=1
  elif c==',' and depth==0: parts.append(body[start:i]);start=i+1
 parts.append(body[start:]); return parts

def block_columns(body:str):
 cols=set()
 for part in split_top_level(body):
  x=part.strip()
  if not x or re.match(r'^(CONSTRAINT|PRIMARY\s+KEY|UNIQUE\s+|FOREIGN\s+KEY|CHECK\s*\(|INDEX\s+|KEY\s+)',x,re.I): continue
  m=re.match(r'[`"]?([A-Za-z0-9_$#]+)[`"]?\s+',x)
  if m: cols.add(m.group(1).lower())
 return cols

def resolve_import(base:Path,spec:str):
 raw=(base/spec).resolve()
 candidates=[raw,raw.with_suffix('.ts'),raw.with_suffix('.tsx'),raw.with_suffix('.js'),raw.with_suffix('.vue'),raw/'index.ts',raw/'index.js']
 return next((x for x in candidates if x.exists()),None)

def main():
 ap=argparse.ArgumentParser(); ap.add_argument('--root',type=Path,default=Path(__file__).resolve().parents[2]); ap.add_argument('--scope',type=Path); ap.add_argument('--file-list',type=Path); ap.add_argument('--report',type=Path); ap.add_argument('--basis-sha',default='')
 args=ap.parse_args(); root=args.root.resolve(); scope=(args.scope or root).resolve(); errors=[]; notes=[]
 if args.basis_sha and not re.fullmatch(r'[0-9a-fA-F]{40}',args.basis_sha): errors.append(f'invalid basis SHA: {args.basis_sha}')
 selected_files=None
 if args.file_list:
  list_path=args.file_list if args.file_list.is_absolute() else root/args.file_list
  if not list_path.exists():
   errors.append(f"file list missing: {list_path}")
   selected_files=[]
  else:
   selected_files=[]
   for raw in list_path.read_text(encoding='utf-8').splitlines():
    rel=raw.strip().replace('\\','/').lstrip('./')
    if not rel or rel.startswith('#'): continue
    candidate=(root/rel).resolve()
    if not candidate.is_relative_to(root):
     errors.append(f"file list path escapes root: {raw}"); continue
    if not candidate.exists(): errors.append(f"file list entry missing: {rel}"); continue
    if candidate.is_file(): selected_files.append(candidate)
 def selected(suffixes=None):
  files=selected_files if selected_files is not None else [p for p in scope.rglob('*') if p.is_file()]
  return [p for p in files if suffixes is None or p.suffix.lower() in suffixes]
 def fail(msg): errors.append(msg)
 def need(path):
  if not path.exists(): fail(f"missing required file: {path.relative_to(root) if path.is_relative_to(root) else path}")
  return path
 # JSON/canonical
 canonical=root/'cpf-tools/db/canonical/platform-schema.json'; need(canonical)
 json_files=[root/'cpf-tools/db/canonical/platform-schema.json',root/'cpf-tools/db/canonical/platform-non-table-objects.json',root/'cpf-tools/db/canonical/seed-model.json',root/'cpf-tools/config/database-install.default.json']
 for p in json_files:
  need(p)
  if p.exists():
   try: json.loads(p.read_text(encoding='utf-8'))
   except Exception as e: fail(f"invalid JSON {p}: {e}")
 # Official vendor pack status and lifecycle discovery contract.
 for vendor in VENDORS:
  pack=root/f'cpf-tools/db/vendor/{vendor}/pack.json'; need(pack)
  if pack.exists():
   try: data=json.loads(pack.read_text(encoding='utf-8'))
   except Exception as e: fail(f"invalid vendor pack JSON {vendor}: {e}"); continue
   if data.get('vendor')!=vendor: fail(f"vendor pack identity drift {vendor}: {data.get('vendor')}")
   if data.get('status')!='완료': fail(f"official vendor pack is not development-complete {vendor}: {data.get('status')}")
   if data.get('runtimeVerification') not in ('완료','미검증'): fail(f"invalid runtime verification status {vendor}: {data.get('runtimeVerification')}")
   lifecycle=data.get('lifecycleStatus',{})
   for phase in ('provision','install','seed','migration','verify','rollback'):
    if lifecycle.get(phase)!='완료': fail(f"vendor lifecycle development status incomplete {vendor}/{phase}: {lifecycle.get(phase)}")
   raw=json.dumps(data,ensure_ascii=False)
   if '부분 구현' in raw or '미구현' in raw or '재확인 필요' in raw:
    fail(f"stale partial/missing status remains in official vendor pack: {vendor}")
   expected_migration=(f'cpf-tools/db/vendor/{vendor}/migration/flyway' if vendor=='mariadb' else f'cpf-tools/db/vendor/{vendor}/migration/flyway/{{logicalDatabase}}')
   expected_rollback=(f'cpf-tools/db/vendor/{vendor}/rollback' if vendor=='mariadb' else f'cpf-tools/db/vendor/{vendor}/migration/rollback/{{logicalDatabase}}')
   if data.get('migrationLocationPattern')!=expected_migration: fail(f"migration location pattern drift {vendor}: {data.get('migrationLocationPattern')}")
   if data.get('rollbackLocationPattern')!=expected_rollback: fail(f"rollback location pattern drift {vendor}: {data.get('rollbackLocationPattern')}")
 if canonical.exists():
  d=json.loads(canonical.read_text(encoding='utf-8')); tables=d.get('tables',[])
  if d.get('tableCount')!=len(tables): fail(f"canonical tableCount drift: declared={d.get('tableCount')} actual={len(tables)}")
  if len(tables)<182: fail(f"canonical table count unexpectedly low: {len(tables)}")
  official=set(d.get('canonicalPolicy',{}).get('officialVendors',[]))
  if official!={"oracle","postgresql","mariadb"}: fail(f"official vendor drift: {sorted(official)}")
  by={t['name'].lower():t for t in tables}; graph=defaultdict(set)
  for t in tables:
   tn=t['name'].lower(); cols={c['name'].lower():c for c in t.get('columns',[])}
   if len(cols)!=len(t.get('columns',[])): fail(f"duplicate canonical column: {tn}")
   for c in t.get('columns',[]):
    dv=c.get('default')
    if isinstance(dv,str) and dv.strip()=="''": fail(f"Oracle-unsafe empty default: {tn}.{c['name']}")
    if c.get('autoIncrement') and not re.match(r'^(BIGINT|INT|TINYINT)',c.get('type',''),re.I): fail(f"non-integer identity: {tn}.{c['name']}")
   for c in t.get('primaryKey',[]):
    if norm_col(c) not in cols: fail(f"PK references missing column: {tn}.{c}")
   for item in t.get('indexes',[])+t.get('uniqueKeys',[]):
    for c in item.get('columns',[]):
     if norm_col(c) not in cols: fail(f"index references missing column: {tn}.{c}")
   for fk in t.get('foreignKeys',[]):
    parent=fk.get('refTable','').lower()
    if parent not in by: fail(f"FK parent missing: {tn}->{parent}"); continue
    if by[parent].get('logicalDatabase')!=t.get('logicalDatabase'): fail(f"cross logical DB FK: {tn}->{parent}")
    pcols={c['name'].lower() for c in by[parent].get('columns',[])}
    for c in fk.get('columns',[]):
     if norm_col(c) not in cols: fail(f"FK local column missing: {tn}.{c}")
    for c in fk.get('refColumns',[]):
     if norm_col(c) not in pcols: fail(f"FK parent column missing: {parent}.{c}")
    if parent!=tn: graph[tn].add(parent)
  indeg={n:0 for n in by}; dependents=defaultdict(set)
  for child,parents in graph.items():
   for parent in parents: indeg[child]+=1;dependents[parent].add(child)
  q=deque(sorted(n for n,v in indeg.items() if v==0)); seen=0
  while q:
   n=q.popleft();seen+=1
   for c in sorted(dependents[n]):
    indeg[c]-=1
    if indeg[c]==0:q.append(c)
  if seen!=len(by): fail("canonical FK cycle: "+','.join(sorted(n for n,v in indeg.items() if v>0)))
  widths=[]
  for tn in ("bat_job","bat_job_definition_audit","bat_job_definition_version","bat_job_dependency","bat_job_parameter_definition","bat_job_runtime_projection","bat_job_runtime_projection_outbox"):
   t=by.get(tn)
   if not t: fail(f"missing canonical table {tn}"); continue
   c=next((x for x in t['columns'] if x['name'].lower()=='job_id'),None)
   widths.append((tn,c.get('type') if c else None))
  if any((typ or '').upper()!='VARCHAR(100)' for _,typ in widths): fail(f"BAT job identity width drift: {widths}")
  for tn,required in REQUIRED_TABLE_COLUMNS.items():
   t=by.get(tn)
   if not t: fail(f"missing canonical table {tn}");continue
   cols={c['name'].lower() for c in t['columns']}; missing=required-cols
   if missing: fail(f"canonical required columns missing {tn}: {sorted(missing)}")
  audit=by.get('bat_job_definition_audit',{}); aid=next((c for c in audit.get('columns',[]) if c['name'].lower()=='audit_id'),{})
  if not aid.get('autoIncrement'): fail("bat_job_definition_audit.audit_id is not canonical identity")
  notes.append(f"canonical tables={len(tables)} schemaVersion={d.get('schemaVersion')}")
 # Source table/column parity and create order
 if canonical.exists():
  for vendor in VENDORS:
   all_blocks={}
   for fn in sorted(set(FILE_BY_DB.values())):
    p=root/f'cpf-tools/db/vendor/{vendor}/source/{fn}'; need(p)
    if not p.exists():continue
    sql=p.read_text(encoding='utf-8'); blocks=create_blocks(sql)
    for k,v in blocks.items():
     if k in all_blocks: fail(f"duplicate source table {vendor}.{k}")
     all_blocks[k]=v
    ordered=sorted(blocks.items(),key=lambda kv:kv[1]['start']); pos={n:i for i,(n,_) in enumerate(ordered)}
    for child,b in ordered:
     for parent in re.findall(r'\bREFERENCES\s+(?:"?\w+"?\.)?"?([A-Za-z0-9_$#]+)"?',b['body'],re.I):
      pn=parent.lower()
      if pn!=child and (pn not in pos or pos[pn]>pos[child]): fail(f"source create order invalid {vendor}/{fn}: {child}->{pn}")
   for t in d['tables']:
    tn=t['name'].lower(); b=all_blocks.get(tn)
    if not b: fail(f"source table missing {vendor}.{tn}");continue
    cols=block_columns(b['body'])
    missing={c['name'].lower() for c in t['columns']}-cols
    if missing: fail(f"source columns missing {vendor}.{tn}: {sorted(missing)}")
   if len(all_blocks)!=len(d['tables']): fail(f"source table count drift {vendor}: source={len(all_blocks)} canonical={len(d['tables'])}")
   notes.append(f"{vendor} source tables={len(all_blocks)}")
 # Oracle unsafe patterns
 oracle_dir=root/'cpf-tools/db/vendor/oracle'
 if oracle_dir.exists():
  bad=re.compile(r"DEFAULT\s+''[^\n]*NOT\s+NULL|DEFAULT\s+''\s+NOT\s+NULL",re.I)
  for p in oracle_dir.rglob('*.sql'):
   if bad.search(p.read_text(encoding='utf-8')): fail(f"Oracle empty-string NOT NULL default: {p.relative_to(root)}")
 # migration/rollback pairs
 expected={77:{'cpfDB','cmnDB','batDB'},78:{'batDB'},79:{'batDB'},80:{'admDB'}}
 for vendor in VENDORS:
  for version,dbs in expected.items():
   for db in dbs:
    if vendor=='mariadb':
     name={77:'qa30_runtime_completion',78:'batch_execution_attempt_ledger',79:'batch_definition_fail_closed_audit',80:'adm_gateway_navigation_permissions'}[version]
     mp=root/f'cpf-tools/db/vendor/mariadb/migration/flyway/V{version}__{name}.sql'
     rp=root/f'cpf-tools/db/vendor/mariadb/rollback/R{version}__{name}.sql'
    else:
     name={77:'qa30_runtime_completion',78:'batch_execution_attempt_ledger',79:'batch_definition_fail_closed_audit',80:'adm_gateway_navigation_permissions'}[version]
     mp=root/f'cpf-tools/db/vendor/{vendor}/migration/flyway/{db}/V{version}__{name}.sql'
     rp=root/f'cpf-tools/db/vendor/{vendor}/migration/rollback/{db}/R{version}__{name}.sql'
    need(mp);need(rp)
    if mp.exists() and rp.exists():
     mt=mp.read_text(encoding='utf-8').upper(); rt=rp.read_text(encoding='utf-8').upper()
     if version==77 and db=='batDB':
      for token in ('PUBLISHED_DEFINITION_VERSION','DEFINITION_CHECKSUM','FK_BAT_JOB_PUBLISHED_DEFINITION','BAT_JOB_RUNTIME_PROJECTION'):
       if token not in mt: fail(f"V77 BAT migration anchor missing {vendor}: {token}")
      for token in ('PUBLISHED_DEFINITION_VERSION','FK_BAT_JOB_PUBLISHED_DEFINITION'):
       if token not in rt: fail(f"R77 BAT rollback anchor missing {vendor}: {token}")
     if version==78:
      for token in ('BAT_EXECUTION_ATTEMPT','FENCING_TOKEN','UNKNOWN_RESULT'):
       if token not in mt: fail(f"V78 migration anchor missing {vendor}: {token}")
      if 'BAT_EXECUTION_ATTEMPT' not in rt: fail(f"R78 rollback anchor missing {vendor}: BAT_EXECUTION_ATTEMPT")
     if version==79:
      for token in ('REQUESTED_BY','APPROVAL_REQUEST_ID','BEFORE_JSON','AFTER_JSON'):
       if token not in mt: fail(f"V79 migration anchor missing {vendor}: {token}")
      for token in ('REQUESTED_BY','APPROVAL_REQUEST_ID'):
       if token not in rt: fail(f"R79 rollback anchor missing {vendor}: {token}")
     if version==80:
      for token in ('GATEWAY_DASHBOARD','GATEWAY_GROUP_WRITE','GATEWAY_ROUTE_STATE','GATEWAY_CONNECTION_TEST','ADM_VIEWER'):
       if token not in mt: fail(f"V80 gateway navigation/permission anchor missing {vendor}: {token}")
      for token in ('ADM_ROLE_API_PERMISSION','ADM_API_PERMISSION','ADM_MENU'):
       if token not in rt: fail(f"R80 gateway navigation/permission rollback anchor missing {vendor}: {token}")
 # Immutable Flyway checksum manifests: validation never rewrites values (QA30 D042).
 for manifest in root.glob('cpf-tools/db/vendor/*/migration/flyway/**/checksums.sha256'):
  entries={}; duplicate=[]
  for line in manifest.read_text(encoding='utf-8').splitlines():
   line=line.strip()
   if not line or line.startswith('#'): continue
   m=re.match(r'^([0-9a-fA-F]{64})\s+\*?(.+)$',line)
   if not m: fail(f"invalid checksum line {manifest.relative_to(root)}: {line}"); continue
   name=m.group(2).strip()
   if name in entries: duplicate.append(name)
   entries[name]=m.group(1).lower()
  if duplicate: fail(f"duplicate checksum entries {manifest.relative_to(root)}: {sorted(set(duplicate))}")
  for migration in sorted(manifest.parent.glob('V*.sql')):
   actual=hashlib.sha256(migration.read_bytes()).hexdigest()
   expected_hash=entries.get(migration.name)
   if expected_hash is None: fail(f"migration checksum missing: {migration.relative_to(root)}")
   elif expected_hash!=actual: fail(f"migration checksum drift: {migration.relative_to(root)} expected={expected_hash} actual={actual}")

 # Gateway route/menu/API/permission parity (QA30 D047).
 required_gateway_menus=("GATEWAY_DASHBOARD","GATEWAY_SERVERS","GATEWAY_GROUPS","GATEWAY_ROUTES","GATEWAY_SECURITY","GATEWAY_HEALTH","GATEWAY_TRANSACTIONS","GATEWAY_LOG_POLICY","GATEWAY_APPLY_STATUS")
 for vendor in VENDORS:
  seed=root/f'cpf-tools/db/vendor/{vendor}/source/61_adm_gateway_seed.sql'; need(seed)
  if seed.exists():
   text=seed.read_text(encoding='utf-8').upper()
   for token in required_gateway_menus+("GATEWAY_GROUP_WRITE","GATEWAY_ROUTE_DELETE","GATEWAY_TEST_CONTROL"):
    if token not in text: fail(f"gateway seed parity missing {vendor}: {token}")
 routes=root/'cpf-admin/frontend/src/app/routes.ts'; state=root/'cpf-admin/frontend/src/state/createAdmState.ts'; controller=root/'cpf-admin/src/main/java/com/cpf/admin/opr/gateway/AdmGatewayRegistryController.java'
 for pth in (routes,state,controller): need(pth)
 if routes.exists():
  text=routes.read_text(encoding='utf-8')
  for menu in ("gateway-dashboard","gateway-servers","gateway-groups","gateway-routes","gateway-security","gateway-health","gateway-transactions","gateway-log-policies","gateway-apply-status"):
   if menu not in text: fail(f"ADM route missing: {menu}")
 if state.exists():
  text=state.read_text(encoding='utf-8')
  for menu in required_gateway_menus:
   if menu not in text: fail(f"ADM menu state missing: {menu}")
 if controller.exists():
  text=controller.read_text(encoding='utf-8')
  for endpoint in ('/server-groups','/bindings','/connection-test-operations/{operationId}'):
   if endpoint not in text: fail(f"Gateway ADM API missing: {endpoint}")

 # Overlay/repository hygiene and obsolete model cleanup contract.
 cleanup=root/'cpf-tools/scripts/cleanup-qa30-obsolete-gateway-model.ps1'; need(cleanup)
 forbidden_artifact_names={'__pycache__','.pytest_cache','.mypy_cache','.gradle','node_modules'}
 forbidden_suffixes={'.pyc','.pyo','.class','.log','.tmp','.bak','.orig','.rej','.zip'}
 for p in scope.rglob('*'):
  rel=p.relative_to(scope)
  if any(part in forbidden_artifact_names for part in rel.parts): fail(f"development artifact directory in scope: {rel}")
  if p.is_file():
   evidence_log = p.suffix.lower()=='.log' and str(rel).replace('\\','/').startswith('cpf-docs/evidence/')
   if p.suffix.lower() in forbidden_suffixes and not evidence_log: fail(f"development artifact file in scope: {rel}")
   data=p.read_bytes()
   if any(b<32 and b not in (9,10,13) for b in data): fail(f"unexpected control character in text artifact: {rel}")
 final_gate=root/'cpf-tools/scripts/verify-cpf-final-completion.ps1'; need(final_gate)
 if final_gate.exists():
  gate_text=final_gate.read_text(encoding='utf-8')
  for token in ('verify-qa30-completion.py','build\\reports\\cpf\\qa30-static-gate.json','CpfGatewayRouteCatalog.java'):
   if token not in gate_text: fail(f"final gate anchor missing: {token}")
 # PowerShell gates must be fail-closed and never rewrite checksums during check.
 for name in ('check-canonical-db-lifecycle-contract.ps1','check-canonical-ddl-safety.ps1','generate-official-db-vendor-source.ps1','sync-platform-non-table-objects.ps1'):
  p=root/f'cpf-tools/scripts/{name}'; need(p)
  if p.exists():
   text=p.read_text(encoding='utf-8')
   if "$ErrorActionPreference" not in text or "'Stop'" not in text and '"Stop"' not in text: fail(f"PowerShell gate is not fail-closed: {name}")
   if name.startswith('check-') and re.search(r'(?i)Set-Content|WriteAllText|Out-File',text): fail(f"check gate mutates artifacts: {name}")
 # runtime SQL parity + semantic anchors
 base=root/'cpf-tools/db/vendor/mariadb/runtime/bat/repository'
 for fn in RUNTIME_SQL:
  texts={}
  for vendor in VENDORS:
   p=root/f'cpf-tools/db/vendor/{vendor}/runtime/bat/repository/{fn}';need(p)
   if p.exists(): texts[vendor]=re.sub(r'\s+',' ',p.read_text(encoding='utf-8').strip()).upper()
  if len(texts)==3:
   anchors={
    'scheduler-execution-insert.sql':('DEFINITION_VERSION','DEFINITION_CHECKSUM','SCHEDULER'),
    'scheduler-find-due.sql':('DEFINITION_VERSION','DEFINITION_CHECKSUM','BAT_SCHEDULE'),
    'worker-attempt-insert.sql':('BAT_EXECUTION_ATTEMPT','FENCING_TOKEN','RUNNING'),
    'worker-attempt-finish.sql':('ATTEMPT_STATUS','FENCING_TOKEN','RUNNING'),
    'worker-execution-load.sql':('BAT_JOB_RUNTIME_PROJECTION','PROJECTION_HASH','DEFINITION_JSON'),
    'worker-execution-find-ready-candidates.sql':('DEFINITION_VERSION','DEFINITION_CHECKSUM','READY'),
    'worker-execution-requeue-retryable.sql':('BAT_EXECUTION_LEASE','FENCING_TOKEN','READY')}
   for vendor,text in texts.items():
    for anchor in anchors[fn]:
     if anchor not in text: fail(f"runtime SQL anchor missing {vendor}/{fn}: {anchor}")
 # Java path/package and forbidden placeholders in scope
 for p in selected({'.java'}):
  text=p.read_text(encoding='utf-8')
  m=re.search(r'^\s*package\s+([\w.]+)\s*;',text,re.M)
  if m:
   expected=Path(*m.group(1).split('.'))/p.name
   if not str(p).replace('\\','/').endswith(str(expected).replace('\\','/')): fail(f"Java package/path drift: {p}")
  if re.search(r'\b(TODO|FIXME|TBD|NOT_IMPLEMENTED|NotImplemented)\b',text,re.I): fail(f"unfinished marker in {p}")
 # Critical runtime consumer/source anchors: contracts must have real consumers, not interface-only surfaces.
 required_source_anchors={
  'cpf-gateway/src/main/java/com/cpf/gateway/service/CpfGatewayProxyService.java':('ledger.begin','recordAttempt','captureRequest','UNKNOWN_RESULT'),
  'cpf-gateway/src/main/java/com/cpf/gateway/runtime/CpfGatewayHealthWorker.java':('@Scheduled','claimHealthProbes','reportHealth'),
  'cpf-gateway/src/main/java/com/cpf/gateway/runtime/CpfGatewayConnectionTestWorker.java':('@Scheduled','claimConnectionTests','completeConnectionTest'),
  'cpf-gateway/src/main/java/com/cpf/gateway/registry/JdbcCpfGatewayRegistryAdapter.java':('RETIRED','fencing_token','cpf_gateway_connection_test_operation'),
  'cpf-batch/control-server/src/main/java/com/cpf/batch/control/job/BatchJobDefinitionController.java':('approved-publish','actorResolver.approved'),
  'cpf-batch/control-server/src/main/java/com/cpf/batch/control/job/BatchJobDefinitionService.java':('bat_job_runtime_projection_outbox','enqueueProjectionEvent','publishApproved'),
  'cpf-batch/scheduler/src/main/java/com/cpf/batch/scheduler/BatchProjectionScheduleSynchronizer.java':('@Scheduled','fencing_token','published_definition_version'),
  'cpf-batch/worker/src/main/java/com/cpf/batch/worker/BatchRuntimeProjectionRepository.java':('projection_hash','definition_checksum','readValue'),
  'cpf-batch/worker/src/main/java/com/cpf/batch/worker/JobPackDispatcher.java':('RETRYABLE_FAILURE','TIMEOUT','UNKNOWN_RESULT'),
  'cpf-batch/worker/src/main/java/com/cpf/batch/worker/ApprovedFileExecutor.java':('CpfFileTransferClient','CpfCredentialReference','checksum'),
  'cpf-admin/src/main/java/com/cpf/admin/opr/gateway/AdmGatewayOperationsStreamController.java':('SseEmitter','Last-Event-ID','operationsEvents'),
  'cpf-core/src/main/java/com/cpf/core/api/logging/policy/CpfLogCaptureGuard.java':('(bytes[end]&0xC0)==0x80','ENCRYPTED_BODY','FORBIDDEN_HEADERS')
 }
 for rel,anchors in required_source_anchors.items():
  source=root/rel; need(source)
  if source.exists():
   text=source.read_text(encoding='utf-8')
   for anchor in anchors:
    if anchor not in text: fail(f"runtime consumer anchor missing {rel}: {anchor}")
 for p in selected({'.java'}):
  text=p.read_text(encoding='utf-8')
  if 'com.cpf.core.common.gateway.CpfGatewayRoute' in text or 'CpfGatewayRouteCatalog' in text:
   fail(f"legacy Gateway route model consumer remains: {p}")
 # Frontend SFC + direct relative imports for files in scope
 for p in selected({'.vue','.ts'}):
  text=p.read_text(encoding='utf-8')
  if p.suffix=='.vue':
   if not re.search(r'<template(?:\s|>)',text,re.I): fail(f"Vue root template missing: {p}")
   if len(re.findall(r'<script\b',text,re.I))>1: fail(f"Vue script count invalid: {p}")
   if text.count('<script')!=text.count('</script>'): fail(f"Vue script not closed: {p}")
  for spec in re.findall(r'(?:from\s*|import\s*)["\'](\.[^"\']+)["\']',text):
   mapped=p.parent
   if resolve_import(mapped,spec) is None: fail(f"frontend relative import missing: {p.relative_to(root) if p.is_relative_to(root) else p} -> {spec}")
 # lightweight secret patterns in scope (allow placeholders)
 secret_patterns=[re.compile(r'(?i)(password|secret|api[_-]?key|token)\s*[:=]\s*["\'][A-Za-z0-9+/=_-]{20,}["\']'),re.compile(r'AKIA[0-9A-Z]{16}')]
 for p in selected():
  if p.suffix.lower() in {'.zip','.png','.jpg','.jpeg','.class'}: continue
  try:text=p.read_text(encoding='utf-8')
  except Exception:continue
  for pat in secret_patterns:
   if pat.search(text): fail(f"possible secret in {p.relative_to(scope)}")
 # result
 result={"status":"PASS" if not errors else "FAIL","basisSha":args.basis_sha.lower() if args.basis_sha else None,"root":str(root),"scope":str(scope),"fileList":str(args.file_list) if args.file_list else None,"errorCount":len(errors),"errors":errors,"notes":notes}
 report=json.dumps(result,ensure_ascii=False,indent=2)+"\n"
 if args.report:
  args.report.parent.mkdir(parents=True,exist_ok=True);args.report.write_text(report,encoding='utf-8')
 print(report,end='')
 return 0 if not errors else 1
if __name__=='__main__': sys.exit(main())
