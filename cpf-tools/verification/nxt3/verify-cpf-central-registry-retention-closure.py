#!/usr/bin/env python3
from __future__ import annotations
import argparse, json, re, sys
from pathlib import Path


def read(p: Path) -> str:
    return p.read_text(encoding='utf-8-sig', errors='ignore') if p.is_file() else ''

def require(cond: bool, msg: str, failures: list[str]) -> None:
    if not cond: failures.append(msg)

def main() -> int:
    ap=argparse.ArgumentParser(); ap.add_argument('--root',default='.')
    ns=ap.parse_args(); root=Path(ns.root).resolve(); f=[]; info={}

    schema_path=root/'cpf-tools/db/canonical/platform-schema.json'
    try: schema=json.loads(read(schema_path))
    except Exception as exc:
        print(json.dumps({'status':'FAIL','failures':[f'canonical schema parse: {exc}']},ensure_ascii=False,indent=2)); return 1
    tables={str(t.get('name') or t.get('tableName') or '').upper():t for t in schema.get('tables',[])}
    require('OPS_MANAGED_SERVER' in tables,'OPS_MANAGED_SERVER canonical table missing',f)
    require('OPS_SERVICE_INSTANCE' in tables,'OPS_SERVICE_INSTANCE canonical table missing',f)
    require('OPS_RUNTIME_INSTANCE_STATE' in tables,'OPS_RUNTIME_INSTANCE_STATE canonical table missing',f)
    require('OPS_RETENTION_POLICY' in tables,'OPS_RETENTION_POLICY canonical table missing',f)
    require('OPS_RETENTION_RUN' in tables,'OPS_RETENTION_RUN canonical table missing',f)
    require('OPS_TRANSACTION_SUBJECT' in tables,'OPS_TRANSACTION_SUBJECT canonical table missing',f)
    service=tables.get('OPS_SERVICE_INSTANCE',{})
    cols={str(c.get('name') or '').lower() for c in service.get('columns',[])}
    require('managed_server_id' in cols,'OPS_SERVICE_INSTANCE.managed_server_id missing',f)

    repo=read(root/'cpf-starters/platform-operations/runtime-control/src/main/java/com/cpf/platform/operations/runtimecontrol/internal/CpfRuntimeControlPlaneRepository.java')
    for token in ('OPS_MANAGED_SERVER','OPS_SERVICE_INSTANCE','OPS_RUNTIME_INSTANCE_STATE','managed_server_id'):
        require(token.lower() in repo.lower(),f'central registry repository missing {token}',f)
    require('resolveManagedServer' in repo,'runtime self-registration managed-server association missing',f)

    controller=read(root/'cpf-admin/src/main/java/com/cpf/admin/opr/server/controller/AdmManagedServerController.java')
    for op in ('admManagedServerFindAll','admManagedServerFindOne','admManagedServerSave','admManagedServerDisable','admRuntimeInventoryFindAll'):
        require(op in controller,f'ADM central registry API missing {op}',f)

    selector=read(root/'cpf-admin/frontend/src/components/RuntimeInventorySelector.vue')
    require('admRuntimeInventoryFindAll' in selector,'shared RuntimeInventorySelector is not backed by central inventory',f)
    consumer_files=[
      'cpf-admin/frontend/src/features/operations/UnifiedOperationsDashboardPage.vue',
      'cpf-admin/frontend/src/features/gateway-operations/GatewayOperationsPage.vue',
      'cpf-admin/frontend/src/features/remote-logs/RemoteLogsPage.vue',
      'cpf-admin/frontend/src/features/batch-runtime-control/RuntimeFleetWorkbench.vue',
      'cpf-admin/frontend/src/features/health/InstanceHealthPage.vue',
      'cpf-admin/frontend/src/features/server-management/pages/ServerManagementPage.vue',
    ]
    consumer_count=sum('RuntimeInventorySelector' in read(root/x) for x in consumer_files)
    discovered_consumers=[]
    frontend=root/'cpf-admin/frontend/src/features'
    if frontend.exists():
        for candidate in frontend.rglob('*.vue'):
            if 'RuntimeInventorySelector' in read(candidate):
                discovered_consumers.append(candidate.relative_to(root).as_posix())
    info['centralRegistryUiConsumers']=consumer_count
    info['centralRegistryUiConsumersDiscovered']=len(discovered_consumers)
    require(consumer_count==len(consumer_files),f'central registry UI consumer closure {consumer_count}/{len(consumer_files)}',f)
    require(len(discovered_consumers)>=len(consumer_files),f'central registry discovered consumers {len(discovered_consumers)} < {len(consumer_files)}',f)

    # Current runtime code must not query the pre-consolidation service/runtime table names.
    legacy=[]
    for base in ('cpf-admin','cpf-biz-admin','cpf-gateway','cpf-batch','cpf-starters','cpf-core'):
        for p in (root/base).rglob('*.java'):
            if '/build/' in p.as_posix(): continue
            t=read(p)
            for old in ('cpf_service_instance','cpf_runtime_instance_state','cpf_service_endpoint','cpf_service_health_status'):
                if re.search(r'(?i)\\b'+old+r'\\b',t): legacy.append(f'{p.relative_to(root)}:{old}')
    require(not legacy,'active runtime legacy physical table refs: '+str(legacy[:20]),f)

    svc=read(root/'cpf-batch/control-plane/src/main/java/com/cpf/batch/control/retention/BatRetentionExecutionService.java')
    sched=read(root/'cpf-batch/control-plane/src/main/java/com/cpf/batch/control/retention/BatRetentionScheduler.java')
    ctl=read(root/'cpf-batch/control-plane/src/main/java/com/cpf/batch/control/retention/BatRetentionController.java')
    handler=read(root/'cpf-batch/control-plane/src/main/java/com/cpf/batch/control/retention/BatOperationLogRetentionHandler.java')
    for token in ('runScheduled','runNow','requestPause','resume(','renewLease','maxRowsPerRun','maxRuntimeSeconds','throttleMillis','inMaintenanceWindow','dry-run first'):
        require(token in svc,f'retention execution engine missing {token}',f)
    require('runScheduled' in sched,'retention scheduler is not an execution-engine consumer',f)
    for token in ('execution.runNow','execution.requestPause','execution.resume'):
        require(token in ctl,f'retention control API does not use common engine: {token}',f)
    require('ORDER BY created_at, operation_id' in handler,'retention DB handler lacks stable keyset/range ordering',f)
    require('setMaxRows(command.maxRows())' in handler,'retention DB handler does not bound one chunk',f)
    require('matched > processed' in handler,'retention DB handler does not signal hasMore from chunk result',f)

    file_writer=read(root/'cpf-starters/platform-operations/observability/src/main/java/com/cpf/platform/operations/observability/internal/logging/file/CpfFileLogWriter.java')
    for token in ('GZIPOutputStream','applyMaxHistoryCount','applyTotalSizeCap','withinRetentionMaintenanceWindow','retentionDeadlineExceeded','retentionThrottle','activeLogPath'):
        require(token in file_writer,f'file retention executor missing {token}',f)

    workbench=read(root/'cpf-admin/frontend/src/features/batch-runtime-control/RetentionWorkbench.vue')
    for token in ('admRetentionPreview','admRetentionRunNow','admRetentionRunPause','admRetentionRunResume','admRetentionPolicySave'):
        require(token in workbench,f'ADM retention workbench missing typed consumer {token}',f)

    for version,name in ((128,'transaction_subject_tracking'),(129,'central_managed_server_registry'),(130,'retention_execution_engine')):
        for vendor in ('mariadb','postgresql','oracle'):
            candidates=list((root/f'cpf-tools/db/vendor/{vendor}/migration').rglob(f'V{version}__{name}.sql'))
            require(bool(candidates),f'{vendor} V{version} {name} migration missing',f)
    info['canonicalTables']=len(tables)
    result={'status':'PASS' if not f else 'FAIL','info':info,'failures':f}
    print(json.dumps(result,ensure_ascii=False,indent=2)); return 0 if not f else 1
if __name__=='__main__': raise SystemExit(main())
