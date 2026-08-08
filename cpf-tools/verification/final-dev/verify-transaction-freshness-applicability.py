#!/usr/bin/env python3
import argparse, shutil, sys, tempfile
from pathlib import Path
SRC='cpf-core/src/main/java/com/cpf/core/common/logging/segment/CpfTransactionTimelineQueryFacade.java'
TEST='cpf-core/src/test/java/com/cpf/core/common/logging/segment/CpfTransactionTimelineFreshnessTest.java'
ADM_SRC='cpf-admin/src/main/java/com/cpf/admin/opr/service/AdmTransactionGroupService.java'
ADM_TEST='cpf-admin/src/test/java/com/cpf/admin/opr/service/AdmTransactionGroupServiceFailureTest.java'

def verify(root):
    e=[]; p=root/SRC; t=root/TEST
    if not p.exists(): return ['missing timeline facade']
    s=p.read_text(encoding='utf-8')
    for req in ('NOT_APPLICABLE','APPLICABLE','AVAILABLE','FAILED','STALE','notApplicableSources','failedSources','classifySourceFreshness'):
        if req not in s:e.append('missing '+req)
    stale='List<String> expected = List.of("LOCAL", "REMOTE", "MESSAGE", "DLQ", "BATCH", "FILE", "TRACE", "AUDIT")'
    if stale in s:e.append('static all-source expectation remains')
    if not t.exists():e.append('missing freshness topology test')
    else:
        q=t.read_text(encoding='utf-8')
        for req in ('pureLocalDoesNotBecomePartial','observedRemoteBecomesApplicable','failedApplicableSourceIsNotMisreported'):
            if req not in q:e.append('test missing '+req)
    adm=root/ADM_SRC; at=root/ADM_TEST
    if not adm.exists():e.append('missing ADM transaction group service')
    else:
        a=adm.read_text(encoding='utf-8')
        for req in ('BatchLineageResult','BATCH_QUERY_FAILED','QUERY_FAILED','failedSources','missingSources','resultState'):
            if req not in a:e.append('ADM batch failure semantics missing '+req)
        if 'catch (RuntimeException ignored)' in a:e.append('ADM still swallows BATCH query failure')
    if not at.exists():e.append('missing ADM batch failure propagation test')
    else:
        aq=at.read_text(encoding='utf-8')
        for req in ('batchQueryFailureRemainsPartialAndOperatorVisible','BATCH_QUERY_FAILED','IllegalStateException','failedSources'):
            if req not in aq:e.append('ADM failure test missing '+req)
    return e

def selftest(root):
    with tempfile.TemporaryDirectory() as td:
        m=Path(td)/'r'; shutil.copytree(root,m)
        p=m/SRC; s=p.read_text(encoding='utf-8').replace('"NOT_APPLICABLE"','"MISSING"')
        p.write_text(s,encoding='utf-8')
        if not verify(m):return ['mutation NOT_APPLICABLE removal not detected']
    with tempfile.TemporaryDirectory() as td:
        m=Path(td)/'r'; shutil.copytree(root,m)
        p=m/ADM_SRC; s=p.read_text(encoding='utf-8').replace('BATCH_QUERY_FAILED','BATCH_EMPTY',1)
        p.write_text(s,encoding='utf-8')
        if not verify(m):return ['mutation ADM BATCH_QUERY_FAILED removal not detected']
    return []

def main():
    ap=argparse.ArgumentParser();ap.add_argument('--root',type=Path,required=True);ap.add_argument('--self-test',action='store_true');a=ap.parse_args()
    e=verify(a.root)+(selftest(a.root) if a.self_test else [])
    if e:
        [print('FAIL:',x) for x in e];return 1
    print('PASS: transaction freshness applicability + ADM BATCH QUERY_FAILED propagation with mutation coverage');return 0
if __name__=='__main__':sys.exit(main())
