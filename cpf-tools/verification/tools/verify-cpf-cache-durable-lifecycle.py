#!/usr/bin/env python3
from __future__ import annotations
import argparse, hashlib, json
from pathlib import Path

VENDORS=("oracle","postgresql","mariadb")
LEDGER=(
    "source/16_cache_invalidation_ledger.sql",
    "install/05_cache_invalidation_ledger.sql",
    "migration/V101__cache_invalidation_ledger.sql",
    "rollback/R101__cache_invalidation_ledger.sql",
    "runtime/cache/cache_invalidation_queries.sql",
    "verify/101_verify_cache_invalidation_ledger.sql",
)
VERSION=(
    "source/22_cache_invalidation_version_fence.sql",
    "install/17_cache_invalidation_version_fence.sql",
    "migration/V113__cache_invalidation_version_fence.sql",
    "rollback/R113__cache_invalidation_version_fence.sql",
    "runtime/cache/cache_invalidation_version_queries.sql",
    "verify/113_verify_cache_invalidation_version_fence.sql",
)

def digest(path:Path)->str:return hashlib.sha256(path.read_bytes()).hexdigest()
def require(text:str,*tokens:str)->bool:return all(token in text for token in tokens)

def verify(repo:Path)->dict:
    repo=repo.resolve()
    paths={
      'coordinator':repo/'cpf-starters/data/cache/spring-data-redis/src/main/java/com/cpf/data/cache/rediscommon/CpfCacheInvalidationCoordinator.java',
      'store':repo/'cpf-starters/data/cache/spring-data-redis/src/main/java/com/cpf/data/cache/rediscommon/JdbcCpfCacheInvalidationStore.java',
      'valkeyAuto':repo/'cpf-starters/data/cache/valkey/src/main/java/com/cpf/data/cache/valkey/CpfValkeyAutoConfiguration.java',
      'adm':repo/'cpf-admin/src/main/java/com/cpf/admin/opr/service/AdmCacheOperationService.java',
    }
    missing=[p.relative_to(repo).as_posix() for p in paths.values() if not p.is_file()]
    for vendor in VENDORS:
        for rel in (*LEDGER,*VERSION):
            p=repo/f'cpf-tools/db/vendor/{vendor}'/rel
            if not p.is_file():missing.append(p.relative_to(repo).as_posix())
        p=repo/f'cpf-tools/db/vendor/{vendor}/pack.json'
        if not p.is_file():missing.append(p.relative_to(repo).as_posix())
    if missing:raise FileNotFoundError('missing: '+','.join(missing))
    c=paths['coordinator'].read_text(encoding='utf-8')
    s=paths['store'].read_text(encoding='utf-8')
    a=paths['valkeyAuto'].read_text(encoding='utf-8')
    m=paths['adm'].read_text(encoding='utf-8')
    checks={
      'durableFirst': c.index('durable.append(requested)') < c.index('applyWithVersionFence(persisted)'),
      'checkpointAfterApply': c.index('applyWithVersionFence(persisted)') < c.index('durable.checkpoint(consumerId(), persisted.eventId())'),
      'reconcile': require(c,'loadAfter(','applyWithVersionFence(event)','durable.checkpoint(consumerId(), event.eventId())'),
      'fastSignalBestEffort': 'Durable ledger + reconciliation remain authoritative.' in c,
      'versionFence': require(c,'durable.version(','event.version() <= current','durable.advanceVersion('),
      'threeVendorDetection': require(s,'ORACLE','POSTGRESQL','MARIADB','getDatabaseProductName'),
      'mariaPagination': 'LIMIT ?' in s,
      'oraclePostgresPagination': 'FETCH FIRST ? ROWS ONLY' in s,
      'idempotentEventKey': require(s,'findByEventKey(event.eventKey())','DataIntegrityViolationException','assertSameRequest','eventKey conflict'),
      'namespaceSqlNull': 'statement.setNull(4, java.sql.Types.VARCHAR)' in s,
      'valkeyWiresDurable': require(a,'JdbcCpfCacheInvalidationStore','CpfCacheInvalidationCoordinator','cpfValkeyInvalidationListenerContainer','ChannelTopic'),
      'providerSeparation': 'Redis compatibility aliases are intentionally not supported here' in a,
      'admConsumer': require(m,'requireCoordinator()','reconcileNow()','invalidations.backlog'),
    }
    bad=[k for k,v in checks.items() if not v]
    if bad:raise AssertionError({'failedSourceChecks':bad,'checks':checks})
    vendors={}
    for vendor in VENDORS:
        root=repo/f'cpf-tools/db/vendor/{vendor}'
        texts={rel:(root/rel).read_text(encoding='utf-8') for rel in (*LEDGER,*VERSION)}
        combined='\n'.join(texts.values()).upper()
        required_tables=('CPF_CACHE_INVALIDATION_EVENT','CPF_CACHE_INVALIDATION_CHECKPOINT','CPF_CACHE_INVALIDATION_VERSION')
        tables={t:t in combined for t in required_tables}
        pack=json.loads((root/'pack.json').read_text(encoding='utf-8'))
        pack_ok=(pack.get('schemaVersion',0)>=5 and pack.get('vendor')==vendor and pack.get('officialVendor',True) is not False
                 and bool(pack.get('canonicalSchema')) and bool(pack.get('runtimeRoot')))
        dialect={
          'oracle':'VARCHAR2' in combined and 'SYSTIMESTAMP' in combined,
          'postgresql':'TIMESTAMP' in combined,
          'mariadb':'ENGINE=INNODB' in combined and 'CURRENT_TIMESTAMP' in combined,
        }[vendor]
        vendors[vendor]={'tables':tables,'pack':pack_ok,'dialect':dialect,
                         'sha256':{rel:digest(root/rel) for rel in (*LEDGER,*VERSION)}}
        if not all(tables.values()) or not pack_ok or not dialect:raise AssertionError({vendor:vendors[vendor]})
    return {'status':'PASS','sourceChecks':checks,'vendors':vendors}

def main():
    p=argparse.ArgumentParser();p.add_argument('--repo-root',default='.');p.add_argument('--report-json',required=True);a=p.parse_args()
    result=verify(Path(a.repo_root));out=Path(a.report_json);out.parent.mkdir(parents=True,exist_ok=True);out.write_text(json.dumps(result,ensure_ascii=False,indent=2)+'\n',encoding='utf-8');print(json.dumps(result,ensure_ascii=False))
if __name__=='__main__':main()
