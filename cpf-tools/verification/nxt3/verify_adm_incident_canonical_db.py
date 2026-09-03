#!/usr/bin/env python3
"""
ADM Incident 실제 Consumer와 Canonical/Fresh/Upgrade DB 정합성을 검증한다.

운영 화면이 사용하는 Incident Lifecycle SQL이 Fresh 설치와 Upgrade 모두에서 같은 테이블을
사용하는지 확인하여 신규 설치에서의 table-not-found 회귀를 차단한다.
"""
from __future__ import annotations

import sys as _cpf_sys

# CPF 표준 인코딩은 UTF-8 이다. 호출자의 콘솔 코드페이지(Windows cp949 등)에 좌우되면
# 한글 출력이 깨져 진단 메시지를 읽을 수 없다. 진입점이 스스로 출력 스트림을 고정한다.
for _cpf_stream in (_cpf_sys.stdout, _cpf_sys.stderr):
    try:
        _cpf_stream.reconfigure(encoding='utf-8')
    except (AttributeError, ValueError):
        pass
import argparse, json, re, sys
from pathlib import Path

TABLES = {
    'ADM_INCIDENT_POLICY':'adm_incident_policy',
    'ADM_MAINTENANCE_WINDOW':'adm_maintenance_window',
    'ADM_INCIDENT_LIFECYCLE':'adm_incident_lifecycle',
    'ADM_INCIDENT_SIGNAL':'adm_incident_signal',
    'ADM_INCIDENT_TIMELINE':'adm_incident_timeline',
    'ADM_INCIDENT_COMMAND':'adm_incident_command',
}
VENDORS=('mariadb','postgresql','oracle')

def main()->int:
    ap=argparse.ArgumentParser(); ap.add_argument('--root',default='.'); ns=ap.parse_args(); root=Path(ns.root).resolve()
    errors=[]
    canonical=root/'cpf-tools/db/canonical/platform-schema.json'
    data=json.loads(canonical.read_text(encoding='utf-8'))
    by={t.get('name'):t for t in data.get('tables',[])}
    for logical,physical in TABLES.items():
        t=by.get(logical)
        if not t: errors.append(f'CANONICAL_TABLE_MISSING:{logical}'); continue
        if str(t.get('currentName','')).lower()!=physical: errors.append(f'CURRENT_NAME_DRIFT:{logical}:{t.get("currentName")}')
        if t.get('logicalOwner')!='admin' or t.get('targetPhysicalDatabase')!='cpfDB': errors.append(f'OWNER_DB_DRIFT:{logical}')
    service=(root/'cpf-admin/src/main/java/com/cpf/admin/opr/incident/AdmIncidentLifecycleService.java').read_text(encoding='utf-8')
    governance=(root/'cpf-admin/src/main/java/com/cpf/admin/opr/service/AdmOperationsGovernanceService.java').read_text(encoding='utf-8')
    for old in ('cpf_incident_policy','cpf_maintenance_window','cpf_incident_signal','cpf_incident_timeline','cpf_incident_command','cpf_incident'):
        if re.search(rf'\b{re.escape(old)}\b',service): errors.append(f'ACTIVE_SERVICE_LEGACY_TABLE:{old}')
    for physical in TABLES.values():
        if physical not in service and physical!='adm_incident_lifecycle': errors.append(f'ACTIVE_SERVICE_CANONICAL_TABLE_NOT_CONSUMED:{physical}')
    if 'adm_incident_lifecycle' not in service: errors.append('ACTIVE_SERVICE_CANONICAL_INCIDENT_MISSING')
    if 'cpf_incident' in governance or 'adm_incident_lifecycle' not in governance: errors.append('OPERATIONS_GOVERNANCE_INCIDENT_TABLE_DRIFT')
    for vendor in VENDORS:
        ddl=(root/f'cpf-tools/db/generated/current/{vendor}/cpf-platform-schema.sql').read_text(encoding='utf-8',errors='ignore').upper()
        for logical in TABLES:
            if not re.search(rf'CREATE\s+TABLE\s+{logical}\s*\(',ddl): errors.append(f'FRESH_DDL_MISSING:{vendor}:{logical}')
        if vendor=='mariadb':
            up=root/'cpf-tools/db/vendor/mariadb/migration/flyway/V118__adm_incident_lifecycle_currentization.sql'
            rb=root/'cpf-tools/db/vendor/mariadb/rollback/R118__adm_incident_lifecycle_currentization.sql'
        else:
            up=root/f'cpf-tools/db/vendor/{vendor}/migration/flyway/cpfDB/V118__adm_incident_lifecycle_currentization.sql'
            rb=root/f'cpf-tools/db/vendor/{vendor}/rollback/cpfDB/R118__adm_incident_lifecycle_currentization.sql'
        if not up.is_file(): errors.append(f'UPGRADE_MISSING:{vendor}')
        if not rb.is_file(): errors.append(f'ROLLBACK_MISSING:{vendor}')
        if up.is_file():
            txt=up.read_text(encoding='utf-8').lower()
            for old,new in [('cpf_incident_policy','adm_incident_policy'),('cpf_maintenance_window','adm_maintenance_window'),('cpf_incident','adm_incident_lifecycle'),('cpf_incident_signal','adm_incident_signal'),('cpf_incident_timeline','adm_incident_timeline'),('cpf_incident_command','adm_incident_command')]:
                if old not in txt or new not in txt: errors.append(f'UPGRADE_RENAME_MISSING:{vendor}:{old}->{new}')
    status='PASS' if not errors else 'FAIL'
    print(f'CPF_ADM_INCIDENT_CANONICAL_DB={status} canonicalTables={len(TABLES)} vendors={len(VENDORS)} errors={len(errors)}')
    for e in errors: print(e)
    return 0 if not errors else 1
if __name__=='__main__': raise SystemExit(main())
