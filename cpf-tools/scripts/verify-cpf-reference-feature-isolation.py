#!/usr/bin/env python3
from __future__ import annotations
import argparse,json,re,sys
from pathlib import Path

def fail(message:str)->None:
    print('[CPF][REFERENCE-ISOLATION][FAIL] '+message,file=sys.stderr)
    raise SystemExit(1)

def main()->None:
    ap=argparse.ArgumentParser();ap.add_argument('--root',default='.');args=ap.parse_args();root=Path(args.root).resolve()
    module=root/'cpf-reference'; main=module/'src/main/java/com/cpf/reference'; test=module/'src/test/java/com/cpf/reference'
    if not module.is_dir():fail('cpf-reference module missing')
    for forbidden in ['cpf-reference-jobpack','cpf-reference-batch','cpf-reference-bza']:
        if (root/forbidden).exists():fail('separate reference module forbidden: '+forbidden)
    gradle=(module/'build.gradle').read_text(encoding='utf-8')
    package_roots={
      "batch":"com/cpf/reference/batch/**","backoffice":"com/cpf/reference/optional/backoffice/**",
      "operations":"com/cpf/reference/optional/operations/**","gateway":"com/cpf/reference/optional/gateway/**"}
    if 'providers.gradleProperty("cpf.reference.features.${name}.enabled")' not in gradle:
        fail('Gradle feature property contract missing')
    for feature,path in package_roots.items():
        if path not in gradle:fail(f'Gradle removable feature contract missing: {feature}')
    # cpf-batch contract and Spring Batch dependency must be conditional, never unconditional.
    batch_block=re.search(r'if \(batchEnabled\) \{(.*?)\n\s*\}',gradle,re.S)
    if not batch_block or "project(':cpf-batch:contract')" not in batch_block.group(1) or 'spring-boot-starter-batch-jdbc' not in batch_block.group(1):
        fail('batch dependencies are not isolated under referenceBatchEnabled')
    forbidden_patterns={
      'generated-domain package':re.compile(r'com\.cpf\.(?:acc|mbr|exs)(?:\.|;)'),
      'generated-domain module':re.compile(r'cpf-(?:acc|mbr|exs)'),
      'BZA product package':re.compile(r'com\.cpf\.bizadmin'),
      'BZA product module':re.compile(r'cpf-biz-admin'),
      'legacy BZA class':re.compile(r'\bEduBza\d{2}\b'),
      'legacy batch package':re.compile(r'com\.cpf\.reference\.edu\.batch'),
    }
    java_files=list(main.rglob('*.java'))+list(test.rglob('*.java'))
    for p in java_files:
        text=p.read_text(encoding='utf-8')
        for name,pattern in forbidden_patterns.items():
            if pattern.search(text):fail(f'{name}: {p.relative_to(root)}')
    # Canonical EDU-BZA IDs are allowed, but package/class/owner must be neutral Reference Backoffice.
    backoffice=list((main/'optional/backoffice').rglob('*Handler.java'))
    if len(backoffice)!=14:fail(f'backoffice handler count={len(backoffice)}')
    for p in backoffice:
        text=p.read_text(encoding='utf-8')
        if 'EduCapabilityKind.BACKOFFICE' not in text or '"cpf-reference"' not in text or 'CPF_REFERENCE_BACKOFFICE_OPERATOR' not in text:
            fail('non-neutral backoffice handler: '+str(p.relative_to(root)))
        rel=p.relative_to(main).as_posix()
        if re.search(r'/(?:case|bza|adm|gw|bat|dev|ops)\d+(?:/|$)',rel):fail('numeric/legacy package segment: '+str(p.relative_to(root)))
    # Batch is one top-level package with feature subpackages and 30 real Job/Step definitions.
    batch_root=main/'batch'
    handlers=list(batch_root.rglob('EduBat*Handler.java'));jobs=list(batch_root.rglob('*JobConfiguration.java'))
    if len(handlers)!=30 or len(jobs)!=30:fail(f'batch source count handlers={len(handlers)} jobs={len(jobs)}')
    if (main/'edu/batch').exists():fail('legacy edu/batch package remains')
    file_process_handler=batch_root/'file/csv/ReferenceCsvFileProcessHandler.java'
    support_handlers=set(batch_root.rglob('*Handler.java'))-set(handlers)
    if support_handlers!={file_process_handler}:fail('batch support handler ownership drift: '+','.join(sorted(str(p.relative_to(root)) for p in support_handlers)))
    file_process_text=file_process_handler.read_text(encoding='utf-8')
    for token in ['package com.cpf.reference.batch.file.csv;','implements FileProcessHandler','REF_CSV_COUNT']:
        if token not in file_process_text:fail('batch FILE_PROCESS support contract missing '+token)
    required_categories={'tasklet','chunk','file','partition','remote','centercut','scheduler','jobpack','recovery','reconcile','flow','faulttolerance','checkpoint','instance','backfill','incremental','concurrency','lifecycle','agent','dryrun','performance','calendar'}
    actual={p.relative_to(batch_root).parts[0] for p in handlers}
    missing=required_categories-actual
    if missing:fail('batch functional package missing: '+','.join(sorted(missing)))
    for p in jobs:
        text=p.read_text(encoding='utf-8')
        for token in ['@ConditionalOnProperty','cpf.reference.features.batch.enabled','cpf.businessKey','cpf.dataScope','edu."+field','EduBatchScenarioWorker']:
            if token not in text:fail(f'batch Job contract missing {token}: {p.relative_to(root)}')
        if 'cpf.idempotencyKey' not in text or 'idempotencyKey,fencingToken,payload' not in text:
            fail('batch Job idempotency/worker signature missing: '+str(p.relative_to(root)))
    worker=batch_root/'runtime/EduBatchScenarioWorker.java'
    if not worker.is_file():fail('batch durable worker missing')
    worker_text=worker.read_text(encoding='utf-8')
    for token in ['CPF_REF_BAT_JOB_EXECUTION','CPF_REF_BAT_CHECKPOINT','CPF_REF_BAT_TARGET_RESULT','CPF_EDU_BUSINESS_RECORD','FENCING_TOKEN<=?','DuplicateKeyException']:
        if token not in worker_text:fail('batch worker durable contract missing '+token)
    # Mandatory core must never import optional feature packages.
    mandatory=[]
    for p in main.rglob('*.java'):
        rel=p.relative_to(main).as_posix()
        if rel.startswith(('edu/runtime/','edu/counterparty/','online/','platform/')):mandatory.append(p)
    for p in mandatory:
        text=p.read_text(encoding='utf-8')
        imports='\n'.join(line.strip() for line in text.splitlines() if line.strip().startswith('import '))
        for token in ['com.cpf.reference.batch.','com.cpf.reference.optional.backoffice.','com.cpf.reference.optional.operations.','com.cpf.reference.optional.gateway.']:
            if token in imports:fail(f'mandatory core imports optional package {token}: {p.relative_to(root)}')
    # Contributors are the only registration boundary.
    contributors={
      'core':main/'edu/runtime/application/CoreEduCapabilityContributor.java',
      'batch':main/'batch/config/ReferenceBatchCapabilityContributor.java',
      'backoffice':main/'optional/backoffice/config/ReferenceBackofficeCapabilityContributor.java',
      'operations':main/'optional/operations/config/ReferenceOperationsCapabilityContributor.java',
      'gateway':main/'optional/gateway/config/ReferenceGatewayCapabilityContributor.java'}
    for feature,p in contributors.items():
        if not p.is_file():fail('contributor missing: '+feature)
        text=p.read_text(encoding='utf-8')
        if feature!='core' and f'cpf.reference.features.{feature}.enabled' not in text:fail('conditional contributor missing: '+feature)
    registry=(main/'edu/runtime/application/EduCapabilityRegistry.java').read_text(encoding='utf-8')
    if 'Collection<? extends EduCapabilityContributor>' not in registry:fail('registry is not contributor based')
    registry_imports='\n'.join(line.strip() for line in registry.splitlines() if line.strip().startswith('import '))
    for token in ['EduBat','EduBackoffice','EduAdm','EduGw']:
        if token in registry_imports or ('new '+token) in registry:fail('registry directly imports optional handler: '+token)
    # Full catalog must point to real files and declare no generated/product dependency.
    catalog=json.loads((module/'src/main/resources/edu/manual-135-catalog.json').read_text(encoding='utf-8'))
    features=catalog.get('features',[])
    if len(features)!=135:fail(f'catalog count={len(features)}')
    expected={'core':60,'batch':30,'backoffice':14,'operations':17,'gateway':14};actual_counts={k:0 for k in expected}
    for feature in features:
        rid=feature['requirementId'];pack=feature.get('featurePack');actual_counts[pack]=actual_counts.get(pack,0)+1
        for key in ['sourcePath','resourceContract']:
            if not (root/feature[key]).is_file():fail(f'{rid} missing {key}: {feature[key]}')
        for p in feature.get('tests',[]):
            if not (root/p).is_file():fail(f'{rid} missing test: {p}')
        if feature.get('generatedDomainIndependent') is not True or feature.get('productModuleIndependent') is not True or feature.get('databaseOwner')!='refDB':
            fail('independence metadata missing: '+rid)
        binding=feature.get('consumerBinding',{})
        if binding.get('ownerModule')!='cpf-reference':fail('non-reference consumer owner: '+rid)
    if actual_counts!=expected:fail(f'feature distribution={actual_counts}')
    # EDU SQL is central refDB only; module-local SQL is forbidden.
    local_sql=list((module/'src').rglob('*.sql'))
    if local_sql:fail('module-local SQL forbidden: '+','.join(str(p.relative_to(root)) for p in local_sql[:5]))
    for vendor in ['oracle','postgresql','mariadb']:
        base=root/f'cpf-tools/db/vendor/{vendor}'
        for rel in ['source/57_reference_edu_operation_ledger.sql','install/01_reference_edu_operation_ledger.sql','migration/flyway/refDB/V93__manual_edu_135_operation_ledger.sql','rollback/refDB/U93__manual_edu_135_operation_ledger.sql','verify/93_verify_manual_edu_135_operation_ledger.sql','source/58_reference_batch_job_pack.sql','install/02_reference_batch_job_pack.sql','migration/flyway/refDB/V94__reference_batch_job_pack.sql','rollback/refDB/U94__reference_batch_job_pack.sql','runtime/ref/reference_batch_job_queries.sql','verify/94_verify_reference_batch_job_pack.sql']:
            if not (base/rel).is_file():fail(f'{vendor} refDB contract missing: {rel}')
    print('[CPF][REFERENCE-ISOLATION][PASS] core=60 batch=30 backoffice=14 operations=17 gateway=14 database=refDB generatedDomains=NONE bzaProduct=NONE')
if __name__=='__main__':main()
