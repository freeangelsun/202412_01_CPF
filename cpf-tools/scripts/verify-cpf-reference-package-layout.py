#!/usr/bin/env python3
from __future__ import annotations
import argparse,json,re,sys
from collections import Counter
from pathlib import Path

EXPECTED_ROOTS={'online':45,'batch':30,'optional/operations':17,'optional/backoffice':14,'optional/gateway':14,'platform':15}
LEGACY_SEGMENT=re.compile(r'^(?:dev|bat|adm|bza|case|gw|ops)\d+$',re.I)
GENERATED_PATTERNS=(re.compile(r'com\.cpf\.(?:acc|mbr|exs)(?:\.|;)'),re.compile(r'cpf-(?:acc|mbr|exs)'))
PRODUCT_PATTERNS=(re.compile(r'com\.cpf\.bizadmin'),re.compile(r'cpf-biz-admin'),re.compile(r'com\.cpf\.admin'),re.compile(r'cpf-admin/src'))

def fail(msg:str)->None:
    print('[CPF][REFERENCE-PACKAGE][FAIL] '+msg,file=sys.stderr)
    raise SystemExit(1)

def package_from_path(path:Path,source_root:Path)->str:
    rel=path.relative_to(source_root)
    return '.'.join(rel.parent.parts)

def main()->None:
    ap=argparse.ArgumentParser();ap.add_argument('--root',default='.');a=ap.parse_args();root=Path(a.root).resolve()
    ref=root/'cpf-reference';main_root=ref/'src/main/java';test_root=ref/'src/test/java';resource_root=ref/'src/main/resources/edu'
    catalog_path=resource_root/'manual-135-catalog.json';layout_path=resource_root/'package-layout.json'
    if not catalog_path.is_file() or not layout_path.is_file():fail('catalog/package layout missing')
    layout=json.loads(layout_path.read_text(encoding='utf-8'))
    if layout.get('ownerModule')!='cpf-reference' or layout.get('logicalDatabase')!='refDB':fail('layout ownership drift')
    if layout.get('generatedDomainDependencies') or layout.get('productModuleDependencies'):fail('layout declares forbidden dependencies')
    cat=json.loads(catalog_path.read_text(encoding='utf-8'));features=cat.get('features',[])
    if len(features)!=135:fail(f'feature count={len(features)}')
    counts=Counter();seen_packages=set();seen_ids=set()
    for f in features:
        rid=f['requirementId'];pkg=f['implementationPackage']
        if rid in seen_ids:fail('duplicate id '+rid)
        seen_ids.add(rid)
        if not pkg.startswith('com.cpf.reference.'):fail('non-reference package '+rid)
        segments=pkg.split('.')
        for seg in segments:
            if LEGACY_SEGMENT.match(seg):fail(f'legacy numeric package segment {seg}: {rid}')
        rel='/'.join(segments[3:])
        root_key=next((k for k in EXPECTED_ROOTS if rel==k or rel.startswith(k+'/')),None)
        if root_key is None:fail(f'unsupported package root {pkg}: {rid}')
        counts[root_key]+=1;seen_packages.add(pkg)
        src=root/f['sourcePath'];res=root/f['resourceContract']
        if not src.is_file() or not res.is_file():fail('source/resource missing '+rid)
        expected_src=(main_root/Path(*pkg.split('.'))/src.name).resolve()
        if src.resolve()!=expected_src:fail(f'source/package mismatch {rid}: {src.relative_to(root)}')
        text=src.read_text(encoding='utf-8')
        if f'package {pkg};' not in text:fail('package declaration mismatch '+rid)
        if f'implementationPackage() {{ return "{pkg}"; }}' not in text:fail('implementationPackage mismatch '+rid)
        for t in f.get('tests',[]):
            tp=root/t
            if not tp.is_file():fail('test missing '+t)
            expected=(test_root/Path(*pkg.split('.'))/tp.name).resolve()
            if tp.resolve()!=expected:fail(f'test/package mismatch {rid}: {tp.relative_to(root)}')
            if f'package {pkg};' not in tp.read_text(encoding='utf-8'):fail('test declaration mismatch '+rid)
        rdata=json.loads(res.read_text(encoding='utf-8'))
        if rdata.get('implementationPackage')!=pkg or rdata.get('owner')!='cpf-reference' or rdata.get('databaseOwner')!='refDB':fail('resource metadata mismatch '+rid)
        if rdata.get('generatedDomainIndependent') is not True or rdata.get('productModuleIndependent') is not True:fail('resource independence mismatch '+rid)
    if dict(counts)!=EXPECTED_ROOTS:fail(f'root distribution={dict(counts)}')
    # All source/test packages must be free of generated/product module dependencies.
    for p in list(main_root.rglob('*.java'))+list(test_root.rglob('*.java')):
        text=p.read_text(encoding='utf-8',errors='ignore')
        for pattern in GENERATED_PATTERNS:
            if pattern.search(text):fail('generated-domain dependency '+str(p.relative_to(root)))
        for pattern in PRODUCT_PATTERNS:
            if pattern.search(text):fail('product-module dependency '+str(p.relative_to(root)))
        m=re.search(r'^package\s+([\w.]+);',text,re.M)
        if m:
            declared=m.group(1)
            for seg in declared.split('.'):
                if LEGACY_SEGMENT.match(seg):fail('legacy package declaration '+str(p.relative_to(root)))
            source_root=main_root if p.is_relative_to(main_root) else test_root
            expected=(source_root/Path(*declared.split('.'))/p.name).resolve()
            if p.resolve()!=expected:fail('physical path/package mismatch '+str(p.relative_to(root)))
    # Batch and optional packs must remain removable from mandatory online/platform code.
    mandatory_roots=[main_root/'com/cpf/reference/online',main_root/'com/cpf/reference/platform',main_root/'com/cpf/reference/edu/runtime',main_root/'com/cpf/reference/edu/counterparty']
    forbidden_imports=('com.cpf.reference.batch.','com.cpf.reference.optional.operations.','com.cpf.reference.optional.backoffice.','com.cpf.reference.optional.gateway.')
    for base in mandatory_roots:
        for p in base.rglob('*.java'):
            imports='\n'.join(x.strip() for x in p.read_text(encoding='utf-8').splitlines() if x.strip().startswith('import '))
            for token in forbidden_imports:
                if token in imports:fail(f'mandatory package imports removable pack {token}: {p.relative_to(root)}')
    print('[CPF][REFERENCE-PACKAGE][PASS] features=135 roots='+str(dict(counts))+' numericPackages=0 generatedDomains=0 productModules=0')
if __name__=='__main__':main()
