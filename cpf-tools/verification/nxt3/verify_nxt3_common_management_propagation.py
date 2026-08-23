#!/usr/bin/env python3
# CPF 개발/검증 Source이며 최신 Requirement와 실패 누적 검증 계약을 따릅니다.
from pathlib import Path
import argparse, json, re

def read(root, rel):
    p=root/rel
    return p.read_text(encoding='utf-8') if p.exists() else ''

def main():
    ap=argparse.ArgumentParser(); ap.add_argument('--root',default='.'); ap.add_argument('--json-out'); a=ap.parse_args()
    root=Path(a.root).resolve(); failures=[]
    service=read(root,'cpf-common/src/main/java/com/cpf/common/management/JdbcCpfCommonManagementService.java')
    publisher=read(root,'cpf-starters/common/src/main/java/com/cpf/common/runtime/cache/CpfCommonCacheRefreshPublisher.java')
    listener=read(root,'cpf-starters/common/src/main/java/com/cpf/common/runtime/cache/CpfCommonCacheRefreshListener.java')
    event_repo=read(root,'cpf-starters/common/src/main/java/com/cpf/common/runtime/cache/CpfCommonCacheRefreshEventRepository.java')
    backoffice=read(root,'cpf-backoffice/online/src/main/java/com/cpf/backoffice/online/reference/BackofficeCommonManagementService.java')
    test_pub=read(root,'cpf-starters/common/src/test/java/com/cpf/common/runtime/cache/CpfCommonCacheRefreshPublisherTest.java')
    test_listener=read(root,'cpf-starters/common/src/test/java/com/cpf/common/runtime/cache/CpfCommonCacheRefreshListenerTest.java')
    checks={
      'owner_api': 'implements CpfCommonManagementApi' in service,
      'version_fence': 'expectedVersion' in service and 'version conflict' in service and 'versionGuardAndBump' in service and 'actual+1' in service and ' AND "+r.versionColumn()+"=?' in service,
      'commit_after_local_refresh': 'afterCommit()' in publisher and 'refresher.refresh(cacheName)' in publisher,
      'durable_event_same_tx': 'Propagation.MANDATORY' in publisher and 'repository.insertEvent' in publisher,
      'checkpoint_reconcile': all(x in listener for x in ['checkpoint(', 'findAfter(', 'advanceCheckpoint(', 'refreshAll()']),
      'checkpoint_monotonic': 'last_event_id<?' in event_repo,
      'backoffice_public_api_consumer': 'CpfCommonManagementApi' in backoffice,
      'rollback_no_refresh_test': 'rollbackNeverRefreshesLocalCache' in test_pub,
      'event_gap_test': 'eventGapIsReplayedAndDuplicateEventIdIsIgnored' in test_listener,
      'cache_outage_retry_test': 'cacheFailureDoesNotAdvanceCheckpointAndNextPollRetriesSameEvent' in test_listener,
      'first_instance_checkpoint_test': 'firstInstanceBootstrapsFromHighWaterAfterFullRefresh' in test_listener,
      'secret_masking': '[MASKED]' in service and 'CpfParameterValueCodec' in service,
      'effective_filter': 'effectiveFrom()' in service and 'effectiveTo()' in service,
      'disabled_filter': 'activeColumn()' in service,
      'reason_required': 'requireReason' in service,
    }
    for k,v in checks.items():
        if not v: failures.append(k)
    result={'status':'PASS' if not failures else 'FAIL','failures':failures,'checks':checks,
            'brokerModel':'NOT_REQUIRED_DB_DURABLE_EVENT_SOURCE_OF_TRUTH',
            'liveMultiInstanceVerification':'UNVERIFIED_EXTERNAL_RUNTIME'}
    print('CPF_NXT3_COMMON_MANAGEMENT_PROPAGATION_GATE='+result['status'])
    print(json.dumps(result,ensure_ascii=False,indent=2))
    if a.json_out: Path(a.json_out).write_text(json.dumps(result,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
    raise SystemExit(0 if not failures else 1)
if __name__=='__main__': main()
