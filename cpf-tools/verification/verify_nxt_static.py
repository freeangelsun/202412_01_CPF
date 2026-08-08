#!/usr/bin/env python3
from __future__ import annotations
import csv, json, re, sys
from collections import Counter
from pathlib import Path
ROOT=Path(__file__).resolve().parents[2]
errors=[]; notes=[]

def err(code,msg): errors.append(f"{code}:{msg}")
# JSON parse
for p in ROOT.rglob('*.json'):
    try: json.loads(p.read_text(encoding='utf-8-sig'))
    except Exception as e: err('JSON',f'{p.relative_to(ROOT)}:{e}')
# CSV parse/header readability
for p in ROOT.rglob('*.csv'):
    try:
        with p.open(encoding='utf-8-sig',newline='') as f: list(csv.reader(f))
    except Exception as e: err('CSV',f'{p.relative_to(ROOT)}:{e}')
# Merge/conflict and whitespace
for p in ROOT.rglob('*'):
    if not p.is_file() or p.suffix.lower() in {'.png','.jpg','.jpeg','.gif','.zip','.jar','.xlsx','.docx','.pdf'}: continue
    try: text=p.read_text(encoding='utf-8')
    except UnicodeDecodeError: continue
    if any(re.match(r'^(<<<<<<<|=======|>>>>>>>)', line) for line in text.splitlines()): err('MERGE_MARKER',str(p.relative_to(ROOT)))
    if any(line.endswith((' ','\t')) for line in text.splitlines()): err('TRAILING_WS',str(p.relative_to(ROOT)))
# Java source-path/package parity
for p in ROOT.rglob('*.java'):
    parts=p.as_posix().split('/src/main/java/')
    if len(parts)!=2: continue
    text=p.read_text(encoding='utf-8')
    m=re.search(r'^package\s+([\w.]+);',text,re.M)
    if not m: err('JAVA_PACKAGE_MISSING',str(p.relative_to(ROOT))); continue
    expected=(m.group(1).replace('.','/')+'/'+p.name)
    if parts[1]!=expected: err('JAVA_PACKAGE_PATH',f'{p.relative_to(ROOT)} expected-suffix={expected}')
# Core/foundation ownership
core=ROOT/'cpf-core'
for p in core.rglob('*.java'):
    text=p.read_text(encoding='utf-8')
    for token in ('org.springframework.web','jakarta.servlet','io.opentelemetry','org.springframework.batch','software.amazon.awssdk','org.springframework.data.redis'):
        if token in text: err('CORE_FORBIDDEN',f'{p.relative_to(ROOT)}:{token}')
for p in (ROOT/'cpf-foundation').rglob('*.java'):
    text=p.read_text(encoding='utf-8')
    for token in ('org.springframework','jakarta.','io.opentelemetry','software.amazon.awssdk','org.apache.'):
        if token in text: err('FOUNDATION_FORBIDDEN',f'{p.relative_to(ROOT)}:{token}')
for p in ROOT.rglob('*.java'):
    if core in p.parents: continue
    if 'import com.cpf.core.internal' in p.read_text(encoding='utf-8'):
        err('EXTERNAL_INTERNAL_REF',str(p.relative_to(ROOT)))
# Starter catalog
catp=ROOT/'cpf-tools/generator/contracts/cpf-starter-catalog.json'
if catp.exists():
    data=json.loads(catp.read_text()); mods=data.get('modules',[])
    for key in ('projectPath','artifactId','ownerPath','configPrefix'):
        vals=[m.get(key) for m in mods if m.get(key)]
        dup=[v for v,c in Counter(vals).items() if c>1]
        if dup: err('CATALOG_DUP',f'{key}:{dup}')
    for m in mods:
        pp=m.get('projectPath')
        if pp and pp.startswith(':cpf-starter-'):
            op=m.get('ownerPath') or m.get('physicalPath')
            if op and not (ROOT/op/'build.gradle').exists():
                # Overlay may omit unchanged existing module build files. Only new NXT modules are mandatory here.
                if any(x in pp for x in ('lock-valkey','session-valkey','object-storage-s3','schema-governance','integration-graphql','integration-realtime','platform-operations-health','runtime-health-jdbc')):
                    err('CATALOG_PHYSICAL',f'{pp}:{op}')
# critical AutoConfiguration imports must resolve inside overlay
critical=[
('cpf-starters/foundation/base','com.cpf.starter.foundation.base.CpfBaseAutoConfiguration'),
('cpf-starters/data/lock-valkey','com.cpf.starter.data.lock.valkey.CpfValkeyLockAutoConfiguration'),
('cpf-starters/security/session-valkey','com.cpf.starter.security.session.valkey.CpfValkeySessionAutoConfiguration'),
('cpf-starters/file/object-storage-s3','com.cpf.starter.file.objectstorage.CpfObjectStorageAutoConfiguration'),
('cpf-starters/messaging/schema-governance','com.cpf.starter.messaging.schema.CpfEventSchemaAutoConfiguration'),
('cpf-starters/integration/graphql','com.cpf.starter.integration.graphql.CpfGraphqlAutoConfiguration'),
('cpf-starters/integration/realtime','com.cpf.starter.integration.realtime.CpfRealtimeAutoConfiguration'),
('cpf-starters/platform-operations/health','com.cpf.starter.platform.operations.health.CpfHealthAutoConfiguration'),
('cpf-starters/platform-operations/runtime-health-jdbc','com.cpf.starter.platform.health.jdbc.CpfRuntimeHealthJdbcAutoConfiguration'),
('cpf-starters/platform-operations/observability','com.cpf.starter.platform.operations.observability.CpfTelemetryFallbackAutoConfiguration'),
('cpf-starters/platform-operations/otlp','com.cpf.starter.observability.otlp.CpfOtlpTelemetryAutoConfiguration')]
for module,fqcn in critical:
    target=ROOT/module/'src/main/java'/Path(fqcn.replace('.','/')+'.java')
    if not target.exists(): err('AUTOCONFIG_TARGET',f'{module}:{fqcn}')
# DB3 V112 parity
for vendor in ('oracle','postgresql','mariadb'):
    base=ROOT/'cpf-tools/db/vendor'/vendor
    req=['source/27_runtime_instance_health.sql','install/16_runtime_instance_health.sql','migration/V112__runtime_instance_health.sql','rollback/R112__runtime_instance_health.sql','verify/112_verify_runtime_instance_health.sql','runtime/health/runtime_instance_health_queries.sql']
    for rel in req:
        if not (base/rel).exists(): err('DB3_PARITY',f'{vendor}:{rel}')
for p in ROOT.rglob('*'):
    if p.is_file() and any(x in p.as_posix().lower().split('/') for x in ('mysql','mssql','h2')):
        err('UNSUPPORTED_DB',str(p.relative_to(ROOT)))
# ADM OpenAPI -> generated client -> route -> consumer
op=ROOT/'cpf-admin/frontend/openapi/cpf-openapi.json'
api=ROOT/'cpf-admin/frontend/src/generated/cpf-api.ts'
routes=ROOT/'cpf-admin/frontend/src/app/routes.ts'
page=ROOT/'cpf-admin/frontend/src/features/health/InstanceHealthPage.vue'
if op.exists():
    od=json.loads(op.read_text()); ids={m.get('operationId') for x in od.get('paths',{}).values() for m in x.values() if isinstance(m,dict)}
    for x in ('admHealthInstanceList','admHealthInstanceDetail','cpfHealthInstanceReport'):
        if x not in ids: err('OPENAPI_OPERATION',x)
if api.exists():
    t=api.read_text()
    for x in ('admHealthInstanceList','admHealthInstanceDetail'):
        if f'function {x}' not in t: err('GENERATED_CLIENT',x)
if routes.exists():
    t=routes.read_text()
    for x in ('admHealthInstanceList','admHealthInstanceDetail','health-instances'):
        if x not in t: err('ADM_ROUTE',x)
if not page.exists(): err('ADM_CONSUMER','InstanceHealthPage.vue')
# Generator neutral HTTP
for p in (ROOT/'cpf-tools/generator').rglob('*'):
    if not p.is_file(): continue
    try: t=p.read_text(encoding='utf-8')
    except: continue
    if 'ParameterizedTypeReference' in t or 'u -> u.path' in t or 'u->u.path' in t: err('GENERATOR_LEGACY_HTTP',str(p.relative_to(ROOT)))
# Delete manifest safety
mp=ROOT/'cpf-docs/work/CPF_DELETE_MANIFEST.csv'
if mp.exists():
    rows=list(csv.DictReader(mp.open(encoding='utf-8',newline='')))
    protected=('cpf-docs/deliverables/','cpf-docs/guides/','cpf-docs/environment/docker/','cpf-tools/environment/docker-development-test/')
    for r in rows:
        if r.get('delete_status')!='PENDING_USER_APPROVAL': continue
        q=r['path']; parts=Path(q).parts
        if Path(q).is_absolute() or '..' in parts or q.startswith(protected): err('DELETE_UNSAFE',q)
# Windows relative path budget
longest=max(((len(p.relative_to(ROOT).as_posix()),p.relative_to(ROOT).as_posix()) for p in ROOT.rglob('*')),default=(0,''))
if longest[0]>220: err('WINDOWS_PATH',f'{longest[0]}:{longest[1]}')
# Basic secret patterns; placeholders/env names are intentionally ignored.
secret_rx=re.compile(r'(?i)(password|secret|token|api[_-]?key)\s*[:=]\s*["\']([A-Za-z0-9+/=_-]{20,})["\']')
for p in ROOT.rglob('*'):
    if not p.is_file() or p.suffix.lower() not in {'.java','.kt','.ts','.vue','.json','.yml','.yaml','.properties','.gradle','.md','.ps1','.sh','.py'}: continue
    try: t=p.read_text(encoding='utf-8')
    except: continue
    for m in secret_rx.finditer(t):
        val=m.group(2)
        if any(x in val.upper() for x in ('PLACEHOLDER','CHANGE_ME','EXAMPLE','ENV','CPF_')): continue
        err('POSSIBLE_SECRET',f'{p.relative_to(ROOT)}')
print(f'NXT_STATIC_GATE={"PASS" if not errors else "FAIL"} errors={len(errors)} max_path={longest[0]}:{longest[1]}')
for e in errors: print(e)
sys.exit(1 if errors else 0)
