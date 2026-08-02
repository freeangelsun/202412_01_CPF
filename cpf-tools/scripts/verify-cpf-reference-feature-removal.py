#!/usr/bin/env python3
from __future__ import annotations
import argparse,json,re,sys
from collections import Counter
from pathlib import Path
EXPECTED={'core':60,'operations':17,'backoffice':14,'gateway':14,'batch':30}
FORBIDDEN_IMPORTS=('com.cpf.reference.batch.','com.cpf.reference.optional.operations.','com.cpf.reference.optional.backoffice.','com.cpf.reference.optional.gateway.')
GENERATED_OR_PRODUCT=('com.cpf.acc.','com.cpf.mbr.','com.cpf.exs.','project(\':cpf-admin\')','project(\':cpf-biz-admin\')','project(\':cpf-gateway\')')
def fail(m):print('[CPF][QA37][REF-FEATURE][FAIL] '+m,file=sys.stderr);raise SystemExit(1)
def main():
 ap=argparse.ArgumentParser();ap.add_argument('--root',default='.');a=ap.parse_args();root=Path(a.root).resolve();ref=root/'cpf-reference'
 build=(ref/'build.gradle').read_text(encoding='utf-8')
 for feature in ('batch','operations','backoffice','gateway'):
  if f"cpf.reference.features.${{name}}.enabled" not in build: fail('feature property factory missing')
  if f"featureEnabled('{feature}')" not in build: fail('feature toggle missing '+feature)
 for token in GENERATED_OR_PRODUCT:
  if token in build:fail('cpf-reference must not depend on generated/product module: '+token)
 expected_exclusions={'batch':'com/cpf/reference/batch/**','operations':'com/cpf/reference/optional/operations/**','backoffice':'com/cpf/reference/optional/backoffice/**','gateway':'com/cpf/reference/optional/gateway/**'}
 for f,p in expected_exclusions.items():
  if p not in build:fail(f+' Java exclusion missing')
 resources={'batch':'edu/batch/**','operations':'edu/optional/operations/**','backoffice':'edu/optional/backoffice/**','gateway':'edu/optional/gateway/**'}
 for f,p in resources.items():
  if p not in build:fail(f+' resource exclusion missing')
 mandatory=ref/'src/main/java/com/cpf/reference/edu'
 mandatory_roots=[mandatory/'runtime',mandatory/'counterparty',ref/'src/main/java/com/cpf/reference/online',ref/'src/main/java/com/cpf/reference/platform']
 violations=[]
 for mandatory_root in mandatory_roots:
  for p in mandatory_root.rglob('*.java'):
   text=p.read_text(encoding='utf-8',errors='ignore')
   for prefix in FORBIDDEN_IMPORTS:
    if 'import '+prefix in text:violations.append(str(p.relative_to(root))+' -> '+prefix)
   if 'CPF_REF_BAT_' in text:violations.append(str(p.relative_to(root))+' -> CPF_REF_BAT_')
 if violations:fail('mandatory core imports removable package: '+', '.join(violations[:10]))
 cat=json.loads((ref/'src/main/resources/edu/manual-135-catalog.json').read_text(encoding='utf-8'))
 counts=Counter()
 for f in cat['features']:
  pack=f.get('featurePack') or ('core' if f['requirementId'].split('-')[1] in ('DEV','OPS') else None)
  if pack not in EXPECTED:fail('unknown featurePack '+str(pack)+' '+f['requirementId'])
  counts[pack]+=1
  if f.get('owner')!='cpf-reference' or f.get('databaseOwner')!='refDB':fail('ownership drift '+f['requirementId'])
  if f.get('generatedDomainIndependent') is not True or f.get('productModuleIndependent') is not True:fail('independence metadata '+f['requirementId'])
  if not (root/f['resourceContract']).is_file():fail('resource contract missing '+f['requirementId'])
 if dict(counts)!=EXPECTED:fail('feature counts '+str(counts))
 # Contributor architecture is the only linkage from core to removable feature families.
 # Core must discover contributors by their interface; concrete optional classes must
 # remain outside the registry so a disabled source set can be removed cleanly.
 registry=(mandatory/'runtime/application/EduCapabilityRegistry.java').read_text(encoding='utf-8')
 if 'Collection<? extends EduCapabilityContributor> contributors' not in registry:
  fail('registry does not accept interface-owned contributor discovery')
 if re.search(r'^import\s+com\.cpf\.reference\.(?:batch|optional)\.',registry,re.M):fail('registry directly imports removable family')
 runtime_config=(mandatory/'runtime/configuration/EduRuntimeConfiguration.java').read_text(encoding='utf-8')
 if 'List<EduCapabilityContributor> contributors' not in runtime_config or 'new EduCapabilityRegistry(contributors)' not in runtime_config:
  fail('Spring contributor collection binding missing')
 contributor_sources={
  'operations':ref/'src/main/java/com/cpf/reference/optional/operations/config/ReferenceOperationsCapabilityContributor.java',
  'backoffice':ref/'src/main/java/com/cpf/reference/optional/backoffice/config/ReferenceBackofficeCapabilityContributor.java',
  'gateway':ref/'src/main/java/com/cpf/reference/optional/gateway/config/ReferenceGatewayCapabilityContributor.java',
  'batch':ref/'src/main/java/com/cpf/reference/batch/config/ReferenceBatchCapabilityContributor.java'}
 for feature,path in contributor_sources.items():
  if not path.is_file():fail('optional contributor source missing '+feature)
  contributor=path.read_text(encoding='utf-8')
  if 'implements EduCapabilityContributor' not in contributor or '@Component' not in contributor:
   fail('optional contributor is not component-discovered '+feature)
  if '@ConditionalOnProperty' not in contributor or f'cpf.reference.features.{feature}.enabled' not in contributor:
   fail('optional contributor toggle binding missing '+feature)
 # Batch package and resources can be removed as a unit.
 batch_sources=list((ref/'src/main/java/com/cpf/reference/batch').rglob('EduBat*Handler.java'))
 batch_jobs=list((ref/'src/main/java/com/cpf/reference/batch').rglob('*JobConfiguration.java'))
 if len(batch_sources)!=30 or len(batch_jobs)!=30:fail(f'batch package must contain 30 handlers/jobs, got {len(batch_sources)}/{len(batch_jobs)}')
 if not (ref/'src/main/resources/edu/batch').is_dir():fail('batch resource pack missing')
 worker=ref/'src/main/java/com/cpf/reference/batch/runtime/EduBatchScenarioWorker.java'
 if not worker.is_file() or 'CPF_REF_BAT_JOB_EXECUTION' not in worker.read_text(encoding='utf-8'):fail('batch worker does not use removable REF batch schema')
 for vendor in ('oracle','postgresql','mariadb'):
  base=root/f'cpf-tools/db/vendor/{vendor}'
  required=['source/58_reference_batch_job_pack.sql','install/02_reference_batch_job_pack.sql','migration/flyway/refDB/V94__reference_batch_job_pack.sql','rollback/refDB/U94__reference_batch_job_pack.sql','runtime/ref/reference_batch_job_queries.sql','verify/94_verify_reference_batch_job_pack.sql']
  for rel in required:
   if not (base/rel).is_file():fail(f'{vendor} removable batch SQL missing {rel}')
  sql=(base/'migration/flyway/refDB/V94__reference_batch_job_pack.sql').read_text(encoding='utf-8').upper()
  for table in ('CPF_REF_BAT_JOB_EXECUTION','CPF_REF_BAT_CHECKPOINT','CPF_REF_BAT_TARGET_RESULT'):
   if table not in sql:fail(f'{vendor} batch table missing {table}')
 # Generated domain/product references are forbidden in product/reference source.
 for p in (ref/'src').rglob('*'):
  if not p.is_file() or p.suffix.lower() not in {'.java','.json','.yml','.yaml','.xml','.ps1','.properties'}:continue
  text=p.read_text(encoding='utf-8',errors='ignore').lower()
  for token in ('com.cpf.acc.','com.cpf.mbr.','com.cpf.exs.','cpf-biz-admin/src','cpf-admin/src','cpf-gateway/src'):
   if token in text:fail('forbidden generated/product reference '+token+' in '+str(p.relative_to(root)))
 print('[CPF][QA37][REF-FEATURE][PASS] core=60 operations=17 backoffice=14 gateway=14 batch=30 removablePackages=4 generatedDomainLinks=0')
if __name__=='__main__':main()
