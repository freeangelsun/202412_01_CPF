#!/usr/bin/env python3
# CPF canonical annotation/runtime-consumer verifier.
from __future__ import annotations
import argparse,json,re
from pathlib import Path
REQ='NXT3-ANNOTATION-001'

def main():
 ap=argparse.ArgumentParser();ap.add_argument('--root',default='.');ap.add_argument('--evidence');ns=ap.parse_args();root=Path(ns.root).resolve();checks=[]
 def c(name,ok,detail=''):checks.append({'name':name,'status':'PASS' if ok else 'FAIL','detail':detail})
 def t(rel):
  p=root/rel;return p.read_text(encoding='utf-8',errors='ignore') if p.is_file() else ''
 ann=t('cpf-starters/base/runtime/src/main/java/com/cpf/foundation/execution/api/CpfOnlineTransaction.java')
 c('single-operation-metadata-contract',all(x in ann for x in ['String operationId();','String name();','String description();']))
 for rel,token in [
  ('cpf-starters/web/src/main/java/com/cpf/web/api/CpfRestController.java','public @interface CpfRestController'),
  ('cpf-starters/data/persistence/src/main/java/com/cpf/data/persistence/api/annotation/CpfTransactional.java','public @interface CpfTransactional'),
  ('cpf-starters/security/src/main/java/com/cpf/security/api/annotation/CpfPreAuthorize.java','public @interface CpfPreAuthorize'),
  ('cpf-starters/base/runtime/src/main/java/com/cpf/foundation/annotation/CpfTimed.java','public @interface CpfTimed'),
  ('cpf-starters/integration/src/main/java/com/cpf/integration/api/annotation/CpfTimeLimiter.java','public @interface CpfTimeLimiter')]: c('canonical-'+token.split()[-1],token in t(rel),rel)
 resolver=t('cpf-starters/web/src/main/java/com/cpf/web/context/CpfOperationIdResolver.java')
 c('operation-id-resolver','operationId()' in resolver and 'Operation.class' in resolver)
 interceptor=t('cpf-starters/web/src/main/java/com/cpf/web/runtime/CpfControllerContextInterceptor.java')
 c('controller-before-invocation-policy','CpfOperationAccessPolicy' in interceptor and 'evaluate(' in interceptor and 'preHandle' in interceptor)
 bootstrap=t('cpf-starters/web/src/main/java/com/cpf/web/runtime/CpfOperationCatalogBootstrap.java')
 c('runtime-operation-bootstrap','CpfOperationCatalogRegistry' in bootstrap and 'synchronize(' in bootstrap and 'ApplicationReadyEvent' in bootstrap)
 for dom in ['cpf-member','cpf-external']:
  joined='\n'.join(p.read_text(encoding='utf-8',errors='ignore') for p in (root/dom).rglob('*.java')) if (root/dom).exists() else ''
  c(f'{dom}-generated-oss-consumer','@CpfTransactional' in joined and '@CpfRepository' in joined and '@CpfRestController' in joined and '@CpfOnlineTransaction' in joined and '@CpfTx' not in joined and '@CpfController' not in joined)
 for label,rel in [('ADM','cpf-admin/src/main/java'),('BZA','cpf-biz-admin/src/main/java'),('GATEWAY','cpf-gateway/src/main/java')]:
  joined='\n'.join(p.read_text(encoding='utf-8',errors='ignore') for p in (root/rel).rglob('*.java')) if (root/rel).exists() else ''
  controllers='\n'.join(t for p in (root/rel).rglob('*Controller.java') for t in [p.read_text(encoding='utf-8',errors='ignore')]) if (root/rel).exists() else ''
  c(f'{label}-management-not-business-transaction',re.search(r'(?m)^\s*@CpfOnlineTransaction\b',controllers) is None)
 fail=[x for x in checks if x['status']=='FAIL'];result={'requirementId':REQ,'status':'PASS' if not fail else 'FAIL','failedCount':len(fail),'checks':checks}
 out=Path(ns.evidence) if ns.evidence else root/'cpf-docs/work/evidence/current/ANNOTATION_RUNTIME_CONSUMER.json';out.parent.mkdir(parents=True,exist_ok=True);out.write_text(json.dumps(result,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
 print(json.dumps({'status':result['status'],'failedCount':len(fail),'checkCount':len(checks)},ensure_ascii=False));
 for x in fail:print('FAIL',x['name'],x['detail'])
 raise SystemExit(1 if fail else 0)
if __name__=='__main__':main()
