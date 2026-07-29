#!/usr/bin/env python3
from __future__ import annotations
import csv, hashlib, json, re, sys
from pathlib import Path

root=Path(sys.argv[1] if len(sys.argv)>1 else '.').resolve()
results=[]
def add(name, ok, detail):
    results.append((name,'PASS' if ok else 'FAIL',detail)); print(f"[{'PASS' if ok else 'FAIL'}] {name}: {detail}")
def read(rel): return (root/rel).read_text(encoding='utf-8')

def files(pattern): return sorted(root.glob(pattern))

# Base structure and hygiene
required=[
 'cpf-tools/generator/create-domain.ps1',
 'cpf-tools/db/canonical/platform-schema.json',
 'cpf-tools/db/canonical/seed-model.json',
 'cpf-tools/db/metadata/CPF_BASELINE_MIGRATION_CHECKSUMS_B894157.json',
 'cpf-tools/db/metadata/bza-permission-manifest.json',
 'cpf-tools/db/metadata/CPF_V69_V72_MIGRATION_MANIFEST_20260729_04.json',
 'cpf-tools/verification/20260729_04/check_frontend_syntax.cjs',
 'cpf-tools/verification/20260729_04/check_java_syntax.py',
 'cpf-tools/verification/20260729_04/check_generator_idempotency_templates.py',
 'cpf-tools/verification/20260729_04/check_generator_java_template_compile.py',
 'cpf-docs/quality/CPF_FINAL_TARGET_162_TRACEABILITY_20260729_04.csv',
 'cpf-docs/quality/qa-20260729/CPF_ENTERPRISE_REQA_816_DEVELOPMENT_CLOSURE_20260729_04.csv',
 'cpf-docs/quality/qa-20260729/CPF_QA_387_FINAL_VALIDATION_MATRIX_20260729_04.csv',
 'cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md',
 'cpf-tools/scripts/relocate-local-runtime-modules.ps1',
 'cpf-tools/scripts/apply-20260729-final-overlay.ps1',
 'cpf-tools/runtime/cpf-local-runtime/build.gradle',
 'cpf-tools/runtime/cpf-local-batch-runtime/build.gradle',
 'cpf-docs/guides/CPF_REPOSITORY_ROOT_LAYOUT_GUIDE.md',
 'cpf-docs/guides/CPF_LOCAL_RUNTIME_MODULE_LOCATION_GUIDE.md',
 'settings.gradle',
]
missing=[p for p in required if not (root/p).is_file()]
add('required-files',not missing,'all present' if not missing else ', '.join(missing))
forbidden_root=[name for name in ('cpf-gradle-plugins','cpf-platform-bom','cpf-local-runtime','cpf-local-batch-runtime') if (root/name).exists()]
add('root-layout-ownership',not forbidden_root,'cpf-tools/build owns Gradle plugin/BOM; cpf-tools/runtime owns local launchers; deploy remains product deployment root' if not forbidden_root else ', '.join(forbidden_root))
settings=read('settings.gradle')
runtime_mapping=(
    "include 'cpf-local-runtime', 'cpf-local-batch-runtime'" in settings
    and "project(':cpf-local-runtime').projectDir = file('cpf-tools/runtime/cpf-local-runtime')" in settings
    and "project(':cpf-local-batch-runtime').projectDir = file('cpf-tools/runtime/cpf-local-batch-runtime')" in settings
)
add('local-runtime-physical-ownership',runtime_mapping,'logical Gradle project names preserved; physical modules owned by cpf-tools/runtime')
forbidden=[]
for p in root.rglob('*'):
    if not p.is_file(): continue
    low=p.relative_to(root).as_posix().lower()
    parts=low.split('/')
    generated_dir=any(x in parts for x in ('target','node_modules','.gradle','.idea'))
    generated_build='build' in parts and not low.startswith('cpf-tools/build/')
    generated_file=p.suffix.lower() in ('.class','.tmp','.bak')
    unexpected_log=p.suffix.lower()=='.log' and not low.startswith('cpf-docs/evidence/')
    if generated_dir or generated_build or generated_file or unexpected_log:
        forbidden.append(low)
add('repository-hygiene',not forbidden,'no generated residue' if not forbidden else ', '.join(forbidden[:20]))

# Structured files
errors=[]
for p in files('**/*.json'):
    try: json.loads(p.read_text(encoding='utf-8'))
    except Exception as e: errors.append(f'{p.relative_to(root)}: {e}')
for p in files('**/*.csv'):
    try:
        with p.open(encoding='utf-8-sig',newline='') as f: list(csv.reader(f))
    except Exception as e: errors.append(f'{p.relative_to(root)}: {e}')
add('structured-files',not errors,'JSON/CSV parse' if not errors else ' | '.join(errors[:10]))
workflow_errors=[]
for p in files('.github/workflows/*.yml') + files('.github/workflows/*.yaml'):
    text=p.read_text(encoding='utf-8')
    if re.search(r'(?m)^\s*(?:with|matrix):\s*\{.*\$\{\{', text):
        workflow_errors.append(f'{p.relative_to(root)}: GitHub expression inside YAML flow mapping')
    if '\t' in text:
        workflow_errors.append(f'{p.relative_to(root)}: tab character')
add('github-workflow-yaml-safety',not workflow_errors,'block mappings and no tabs' if not workflow_errors else ' | '.join(workflow_errors))

# Java package/path and public raw map
pkg_errors=[]; raw=[]; internal_import=[]; duplicate={}
public_raw=re.compile(r'public\s+(?:[\w<>?,.\[\]\s]+\s+)?(?:Map|HashMap|LinkedHashMap)\s*<')
for p in files('**/*.java'):
    text=p.read_text(encoding='utf-8')
    rel=p.relative_to(root).as_posix()
    m=re.search(r'^package\s+([\w.]+);',text,re.M)
    if m:
        expected='/'.join(m.group(1).split('.'))+'/'+p.name
        if not p.as_posix().endswith(expected) and rel!='cpf-tools/release/CpfReleaseSigner.java':
            pkg_errors.append(rel)
        key=m.group(1)+'.'+p.stem; duplicate.setdefault(key,[]).append(p)
    if '/src/test/' not in '/'+rel and public_raw.search(text): raw.append(rel)
    if '/src/test/' not in '/'+rel and '/cpf-core/' not in '/'+rel and re.search(r'import\s+com\.cpf\.core\.(?:common|internal)\.',text):
        internal_import.append(rel)
dups=[k for k,v in duplicate.items() if len(v)>1]
add('java-package-path',not pkg_errors,f'{len(list(files("**/*.java")))} Java files' if not pkg_errors else ', '.join(pkg_errors[:10]))
add('java-duplicate-types',not dups,'none' if not dups else ', '.join(dups[:10]))
add('public-raw-map',not raw,'none' if not raw else ', '.join(raw[:20]))
add('core-internal-import',not internal_import,'none' if not internal_import else ', '.join(internal_import[:20]))

# Java source maintainability rules aligned with cpf-tools/scripts/check-java-format.ps1.
format_errors=[]
for p in files('**/*.java'):
    text=p.read_text(encoding='utf-8')
    rel=p.relative_to(root).as_posix()
    if re.search(r'(?m)^[ \t]*package[ \t]+[^;]+;[ \t]+import[ \t]+', text): format_errors.append(f'{rel}: package/import same line')
    if re.search(r'(?m)^[ \t]*import[ \t]+[^;]+;[ \t]+import[ \t]+', text): format_errors.append(f'{rel}: multiple imports same line')
    if re.search(r'(?m)^[ \t]*@\w+(?:\([^\n]*\))?[ \t]+(?:public|protected|private|class|interface|record|enum)\b', text): format_errors.append(f'{rel}: annotation/declaration same line')
    if re.search(r'(?m)^.*\b(?:class|interface|record|enum)\s+\w+.*\b(?:class|interface|record|enum)\s+\w+.*$', text): format_errors.append(f'{rel}: multiple declarations line')
    for no,line in enumerate(text.splitlines(),1):
        if len(line)>220: format_errors.append(f'{rel}:{no}: line length {len(line)}')
add('java-format-maintainability',not format_errors,'readable package/import/declaration and <=220 columns' if not format_errors else ' | '.join(format_errors[:20]))

# Direct client boundary
client_hits=[]
allowed=('cpf-core/src/main/','cpf-common/src/main/java/com/cpf/common/cache/adapter/redis/')
patterns=('new RestTemplate','WebClient.builder','HttpClient.newHttpClient','DriverManager.getConnection')
for p in files('**/*.java'):
    rel=p.relative_to(root).as_posix(); text=p.read_text(encoding='utf-8')
    if '/src/test/' not in '/'+rel and any(x in text for x in patterns) and not rel.startswith(allowed):
        client_hits.append(rel)
add('direct-client-boundary',not client_hits,'none' if not client_hits else ', '.join(client_hits[:20]))

# Runtime control typed contract
rc=read('cpf-core/src/main/java/com/cpf/core/api/runtimecontrol/CpfRuntimeControlPlane.java')
cmd=read('cpf-core/src/main/java/com/cpf/core/api/runtimecontrol/CpfRuntimeChangeCommand.java')
delivery=read('cpf-core/src/main/java/com/cpf/core/api/runtimecontrol/CpfRuntimeDelivery.java')
typed=all(x in rc for x in ('CpfRuntimeStatus status(','CpfRuntimeTargetPreview previewTargets','CpfRuntimeChangePreview previewChange')) and 'CpfRuntimePayload payload' in cmd and 'CpfRuntimePayload payload' in delivery
add('runtime-control-typed',typed,'typed status/preview/payload')

# Generator product contract
gen=read('cpf-tools/generator/create-domain.ps1')
gen_checks={
 'no-fixed-reference': '.reference' not in gen and '/reference' not in gen,
 'query-command-split': 'QueryPort' in gen and 'CommandPort' in gen,
 'typed-result-token': "CPF_RESULT_TYPE:" in gen and "CPF_IDEMPOTENCY_RESULT_TYPE:" in gen,
 'real-memory-crud': all(x in gen for x in ('ConcurrentHashMap','OptimisticLockingFailureException','IdempotencyRecord','requestHash','storeSnapshot','idempotencySnapshot')) and not re.search(r'verifyRollback\([^)]*\)\s*\{\s*return\s+true\s*;', gen),
 'product-fake-block': '@Profile("!prod & !production & !stage & !staging & (local | test | edu)")' in gen and 'ConditionalOnExpression' not in gen,
 'cpf-http-client': 'CpfHttpClient' in gen and 'WebClient.builder' not in gen,
 'typed-generated-ui': '${FeatureClassPrefix}SampleItemView' in gen and 'Promise<Record<string, unknown>[]>' not in gen,
 'rollback-test': 'rollbackVerificationRestoresTheOriginalState' in gen,
 'idempotency-ledger': all(x in gen for x in ('IdempotencyEntry','findIdempotency','insertIdempotency','sameIdempotencyKeyAndSameRequestReplaysResult','sameIdempotencyKeyAndDifferentRequestIsRejected')),
 'adapter-mode-config': 'sample-item:' in gen and '_SAMPLE_ITEM_MODE:' in gen and '_REFERENCE_MODE:' not in gen,
}
add('generator-golden-template',all(gen_checks.values()),', '.join(k for k,v in gen_checks.items() if not v) or 'typed query/command, CRUD fake guard, standard HTTP')

# Vendor runtime templates
template_bad=[]
for vendor in ('mariadb','postgresql','oracle'):
    rel=f'cpf-tools/db/vendor/{vendor}/domain-template/runtime/mybatis/__MAPPER__.xml.template'
    t=read(rel)
    required_mapper = ('@CPF_RESULT_TYPE@','@CPF_IDEMPOTENCY_RESULT_TYPE@','<select id="count" resultType="long">',
                       '<select id="findById"','<select id="findIdempotency"','<insert id="insertIdempotency"')
    lifecycle = [
        f'cpf-tools/db/vendor/{vendor}/domain-template/install/10_empty_install.sql.template',
        f'cpf-tools/db/vendor/{vendor}/domain-template/migration/V1____DOMAIN___domain.sql.template',
        f'cpf-tools/db/vendor/{vendor}/domain-template/rollback/R1__remove___DOMAIN___domain.sql.template',
        f'cpf-tools/db/vendor/{vendor}/domain-template/verify/90_verify.sql.template',
    ]
    lifecycle_text='\n'.join(read(x) for x in lifecycle)
    if ('resultType="map"' in t or not all(x in t for x in required_mapper)
            or not all((root/x).is_file() for x in lifecycle)
            or '@CPF_TABLE_PREFIX@_sample_item_idem' not in lifecycle_text
            or 'request_hash' not in lifecycle_text):
        template_bad.append(vendor)
add('generator-vendor-template-parity',not template_bad,'3 vendors typed mapper + idempotency ledger lifecycle' if not template_bad else ', '.join(template_bad))

# V69/V70/V71/V72 and checksum manifest
manifest=json.loads(read('cpf-tools/db/metadata/CPF_V69_V72_MIGRATION_MANIFEST_20260729_04.json'))
hash_bad=[]
for item in manifest['migrations']:
    p=root/item['path']
    if not p.is_file() or hashlib.sha256(p.read_bytes()).hexdigest()!=item['sha256'] or p.stat().st_size!=item['size']:
        hash_bad.append(item['path'])
add('migration-sha256',not hash_bad,f"{len(manifest['migrations'])} files" if not hash_bad else ', '.join(hash_bad))

# Official lifecycle ownership and immutable historical checksum contract.
wrong_lifecycle=[v for v in ('postgresql','oracle') if (root/f'cpf-tools/db/vendor/{v}/source/migration').exists()]
add('official-lifecycle-ownership',not wrong_lifecycle,
    'MariaDB source/migration authoring; PostgreSQL/Oracle logical-DB lifecycle packs' if not wrong_lifecycle else ', '.join(wrong_lifecycle))
base_contract=json.loads(read('cpf-tools/db/metadata/CPF_BASELINE_MIGRATION_CHECKSUMS_B894157.json'))
pack_paths={
 'mariadb/global':'cpf-tools/db/vendor/mariadb/source/migration/flyway/checksums.sha256',
 'postgresql/cpfDB':'cpf-tools/db/vendor/postgresql/migration/flyway/cpfDB/checksums.sha256',
 'postgresql/admDB':'cpf-tools/db/vendor/postgresql/migration/flyway/admDB/checksums.sha256',
 'postgresql/bzaDB':'cpf-tools/db/vendor/postgresql/migration/flyway/bzaDB/checksums.sha256',
 'oracle/cpfDB':'cpf-tools/db/vendor/oracle/migration/flyway/cpfDB/checksums.sha256',
 'oracle/admDB':'cpf-tools/db/vendor/oracle/migration/flyway/admDB/checksums.sha256',
 'oracle/bzaDB':'cpf-tools/db/vendor/oracle/migration/flyway/bzaDB/checksums.sha256',
}
history_errors=[]
for key,history in base_contract['packs'].items():
    rel=pack_paths[key]
    actual=[ln.strip() for ln in read(rel).splitlines() if ln.strip()]
    for expected in history:
        if expected not in actual: history_errors.append(f'{key}: historical entry changed/missing {expected.split("*")[-1]}')
    versions=[]
    for line in actual:
        m=re.match(r'^([0-9a-f]{64}) \*(V(\d+)__.+\.sql)$',line)
        if not m: history_errors.append(f'{key}: invalid checksum line'); continue
        versions.append(int(m.group(3)))
        # Full worktree validates every historical artifact. Overlay-only validates bundled new artifacts.
        pack_dir=(root/rel).parent
        sql=pack_dir/m.group(2)
        if sql.exists():
            if hashlib.sha256(sql.read_bytes()).hexdigest()!=m.group(1): history_errors.append(f'{key}: hash mismatch {sql.name}')
        elif (root/'.git').exists(): history_errors.append(f'{key}: manifest target missing {sql.name}')
    if len(versions)!=len(set(versions)): history_errors.append(f'{key}: duplicate Flyway version')
# MariaDB runtime mirror must carry byte-identical new migrations and full ledger.
msrc=root/'cpf-tools/db/vendor/mariadb/source/migration/flyway'
mrun=root/'cpf-tools/db/vendor/mariadb/migration/flyway'
if (msrc/'checksums.sha256').read_bytes() != (mrun/'checksums.sha256').read_bytes(): history_errors.append('mariadb/global: source/runtime checksum drift')
for version in range(69,73):
    srcs=list(msrc.glob(f'V{version}__*.sql')); runs=list(mrun.glob(f'V{version}__*.sql'))
    if len(srcs)!=1 or len(runs)!=1 or srcs[0].read_bytes()!=runs[0].read_bytes(): history_errors.append(f'mariadb/global: V{version} source/runtime drift')
add('migration-history-immutability',not history_errors,'historical checksums preserved; V69-V72 append-only' if not history_errors else ' | '.join(history_errors[:20]))
# Canonical schema and official generated source include fresh-install objects.
platform=json.loads(read('cpf-tools/db/canonical/platform-schema.json'))
canonical_tables={t['name'].lower():t for t in platform['tables']}
required_tables={'cpf_cache_invalidation_event','cpf_cache_invalidation_checkpoint','adm_file_job','adm_file_job_row'}
schema_errors=[]
if not required_tables.issubset(canonical_tables): schema_errors.append('canonical tables missing')
if platform.get('tableCount')!=len(platform.get('tables',[])): schema_errors.append('tableCount mismatch')
for vendor in ('mariadb','postgresql','oracle'):
    source=(read(f'cpf-tools/db/vendor/{vendor}/source/10_cpf_schema.sql')+'\n'+read(f'cpf-tools/db/vendor/{vendor}/source/30_adm_schema.sql')).lower()
    for table_name in required_tables:
        if not re.search(r'create\s+table(?:\s+if\s+not\s+exists)?\s+'+re.escape(table_name)+r'\b',source): schema_errors.append(f'{vendor}: {table_name} missing')
    for col in ('approval_id','applied_by','resolved_by','control_by','control_reason','control_updated_at'):
        if col not in source: schema_errors.append(f'{vendor}: {col} missing')
add('fresh-install-schema-parity',not schema_errors,'canonical + 3 vendor CPF/ADM source aligned' if not schema_errors else ' | '.join(schema_errors[:20]))

def create_tables(text):
    return sorted(set(x.lower() for x in re.findall(r'CREATE\s+TABLE(?:\s+IF\s+NOT\s+EXISTS)?\s+([\w.\"`]+)',text,re.I)))
v69_paths={v:[item['path'] for item in manifest['migrations'] if item['vendor']==v and item['version']==69 and item['direction']=='UPGRADE'] for v in ('mariadb','postgresql','oracle')}
table_sets={v:create_tables('\n'.join(read(path) for path in v69_paths[v])) for v in ('mariadb','postgresql','oracle')}
normalized=[set(x.split('.')[-1].strip('"`') for x in table_sets[v]) for v in ('mariadb','postgresql','oracle')]
add('v69-table-parity',normalized[0]==normalized[1]==normalized[2] and normalized[0]=={'cpf_cache_invalidation_event','cpf_cache_invalidation_checkpoint','adm_file_job','adm_file_job_row'},str(sorted(normalized[0])))

def perm_keys(text):
    roles=('BZA_ADMIN','BZA_OPERATOR','BZA_APPROVER')
    actions=('SIMULATE','PII_RAW','DECIDE')
    return sorted(set(re.findall(r"'(BZA_(?:ADMIN|OPERATOR|APPROVER))'.{0,100}?'(SIMULATE|PII_RAW|DECIDE)'",text,re.I|re.S)))
def migration_text(vendor, version, direction='UPGRADE'):
    return '\n'.join(read(item['path']) for item in manifest['migrations'] if item['vendor']==vendor and item['version']==version and item['direction']==direction)
keys={v:perm_keys(migration_text(v,70)) for v in ('mariadb','postgresql','oracle')}
add('v70-permission-parity',keys['mariadb']==keys['postgresql']==keys['oracle'] and len(keys['mariadb'])>=4,str(keys['mariadb']))
notif={v:set(re.findall(r"NOTIFICATION_(?:RETRY|CANCEL)",migration_text(v,71))) for v in ('mariadb','postgresql','oracle')}
add('v71-notification-action-parity',notif['mariadb']==notif['postgresql']==notif['oracle']=={'NOTIFICATION_RETRY','NOTIFICATION_CANCEL'},str(notif['mariadb']))

v72_required={'approval_id','applied_by','resolved_by','control_by','control_reason','control_updated_at'}
def v72_columns(text):
    return {x.lower() for x in re.findall(r'\b(approval_id|applied_by|resolved_by|control_by|control_reason|control_updated_at)\b', text, re.I)}
v72={v:v72_columns(migration_text(v,72)) for v in ('mariadb','postgresql','oracle')}
v72_indexes={v:'ix_adm_file_job_approval' in migration_text(v,72).lower() for v in ('mariadb','postgresql','oracle')}
v72_rollback={v:v72_columns(migration_text(v,72,'ROLLBACK')) for v in ('mariadb','postgresql','oracle')}
add('v72-file-job-control-parity', all(v72[v]==v72_required and v72_rollback[v]==v72_required and v72_indexes[v] for v in v72), str({v:sorted(v72[v]) for v in v72}))
file_job_service=read('cpf-admin/src/main/java/com/cpf/admin/opr/filejob/AdmFileJobService.java')
file_job_repo=read('cpf-admin/src/main/java/com/cpf/admin/opr/filejob/AdmFileJobRepository.java')
file_job_controller=read('cpf-admin/src/main/java/com/cpf/admin/opr/filejob/AdmFileJobController.java')
file_job_guard=all(token in file_job_service+file_job_repo+file_job_controller for token in ('approvalId','requireApproval','요청자와 적용 운영자는 분리해야 합니다.','resolveUnknown','lease_until','fencing_token','sourceSha256'))
add('file-job-control-guard',file_job_guard,'approval/four-eyes/unknown-result/lease/fencing/checksum')
seed=json.loads(read('cpf-tools/db/canonical/seed-model.json'))
seed_source='\n'.join(s.get('source','') for s in seed['statements'] if s.get('table')=='bza_permission')
seed_ok=all(token in seed_source for token in ('SIMULATE','PII_RAW','/api/bza/backoffice/approvals/*/actions','/api/bza/approvals/*/decisions')) and seed.get('statementCount')==len(seed.get('statements',[]))
add('canonical-bza-action-seed',seed_ok,f"statementCount={seed.get('statementCount')}")

# BZA manifest/frontend action consistency
bza=json.loads(read('cpf-tools/db/metadata/bza-permission-manifest.json'))
rules={(x['method'],x['pathPattern']):x['actionCode'] for x in bza['actionRules']}
crud=read('cpf-biz-admin/frontend/src/components/CrudTable.vue')
consistency=rules.get(('POST','*/**'))=='WRITE' and 'createAction:"WRITE"' in crud and 'updateAction:"WRITE"' in crud and rules.get(('POST','backoffice/employees/*/contacts/raw'))=='PII_RAW' and rules.get(('GET','backoffice/permissions/effective'))=='SIMULATE'
add('bza-action-consistency',consistency,'combined save WRITE; dangerous actions explicit')

# Official vendor policy
all_text='\n'.join(p.read_text(encoding='utf-8',errors='ignore') for p in files('cpf-tools/db/**/*.json'))
official=set(json.loads(read('cpf-tools/db/canonical/seed-model.json'))['canonicalPolicy']['officialVendors'])
add('official-db-vendors',official=={'mariadb','postgresql','oracle'},str(sorted(official)))

# Secret-shaped literal scan, exclude docs/test placeholders and known non-secrets
secret_hits=[]
secret_re=re.compile(r'(?i)(password|secret|token|api[_-]?key)\s*[:=]\s*["\']([^"\'\r\n]{8,})["\']')
for p in files('**/*'):
    if not p.is_file() or p.suffix.lower() not in ('.java','.ts','.vue','.yml','.yaml','.json','.ps1','.properties'): continue
    rel=p.relative_to(root).as_posix()
    parts=rel.lower().split('/')
    if any(x in parts for x in ('build','target','node_modules','.gradle','.idea')): continue
    if '/test/' in rel or '.test.' in rel or '.spec.' in rel or 'qa-' in rel: continue
    for m in secret_re.finditer(p.read_text(encoding='utf-8',errors='ignore')):
        val=m.group(2)
        if '$' in val or val.startswith('@') or val.lower().startswith('x-') or any(x in val.lower() for x in ('change-me','placeholder','reference','masked','redacted')): continue
        secret_hits.append(f'{rel}:{m.group(1)}')
add('secret-literal-scan',not secret_hits,'none' if not secret_hits else ', '.join(secret_hits[:20]))

# Build dependency and unknown-result safety contracts
common_build=read('cpf-common/build.gradle')
add('common-runtime-dependencies',
    "spring-boot-starter-data-redis" in common_build and "org.apache.poi:poi-ooxml:5.5.1" in common_build,
    'Redis starter and POI 5.5.1')
notification_outbox=read('cpf-admin/src/main/java/com/cpf/admin/opr/service/AdmNotificationOutboxService.java')
add('notification-unknown-result',
    'UNKNOWN_RESULT_PROVIDER_EXCEPTION' in notification_outbox
    and '\"PROVIDER_EXCEPTION\",\n                        safeMessage(providerFailure)' not in notification_outbox,
    'provider exception isolated from automatic retry')
agent=read('cpf-core/src/main/java/com/cpf/core/common/runtimecontrol/CpfRuntimeControlAgent.java')
add('runtime-agent-fencing-import',
    'import com.cpf.core.api.runtimecontrol.CpfRuntimeFenceException;' in agent,
    'fencing exception compiles')

failed=[r for r in results if r[1]=='FAIL']
print(f"FINAL_SOURCE_CLOSURE={'PASS' if not failed else 'FAIL'} pass={len(results)-len(failed)} fail={len(failed)}")
sys.exit(1 if failed else 0)
