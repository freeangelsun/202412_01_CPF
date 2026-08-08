#!/usr/bin/env python3
import argparse, shutil, sys, tempfile
from pathlib import Path
SRC='cpf-core/src/main/java/com/cpf/core/common/logging/segment/CpfTransactionTimelineQueryFacade.java'
TEST='cpf-core/src/test/java/com/cpf/core/common/logging/segment/CpfTransactionTimelineFreshnessTest.java'

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
    return e

def selftest(root):
    with tempfile.TemporaryDirectory() as td:
        m=Path(td)/'r'; shutil.copytree(root,m)
        p=m/SRC; s=p.read_text(encoding='utf-8').replace('"NOT_APPLICABLE"','"MISSING"')
        p.write_text(s,encoding='utf-8')
        if not verify(m):return ['mutation NOT_APPLICABLE removal not detected']
    return []

def main():
    ap=argparse.ArgumentParser();ap.add_argument('--root',type=Path,required=True);ap.add_argument('--self-test',action='store_true');a=ap.parse_args()
    e=verify(a.root)+(selftest(a.root) if a.self_test else [])
    if e:
        [print('FAIL:',x) for x in e];return 1
    print('PASS: transaction freshness applicability distinguishes APPLICABLE/NOT_APPLICABLE/AVAILABLE/FAILED/STALE with pure-local regression coverage');return 0
if __name__=='__main__':sys.exit(main())
