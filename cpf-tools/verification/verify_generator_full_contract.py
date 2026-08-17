#!/usr/bin/env python3
from __future__ import annotations
import argparse,json,re
from pathlib import Path

def fail(x): print('CPF_GENERATOR_FULL_CONTRACT=FAIL '+x); return 1

def main():
    ap=argparse.ArgumentParser(); ap.add_argument('--root',default='.'); ns=ap.parse_args(); r=Path(ns.root).resolve()
    adapter=r/'cpf-tools/generator/create-domain.ps1'
    engine=r/'cpf-tools/generator/engine/cpf_domain_generator.py'
    cat=r/'cpf-tools/generator/contracts/cpf-starter-catalog.json'
    prof=r/'cpf-tools/generator/contracts/capability-profiles.json'
    lifecycle=r/'cpf-tools/generator/contracts/generator-lifecycle-contract.json'
    for p in (adapter,engine,cat,prof,lifecycle):
        if not p.is_file(): return fail('missing='+p.relative_to(r).as_posix())
    ps=adapter.read_text(encoding='utf-8',errors='ignore')
    eng=engine.read_text(encoding='utf-8',errors='ignore')
    life=json.loads(lifecycle.read_text(encoding='utf-8'))
    # DatabaseVendor is no longer a Generated Domain input. DB3 is a central lifecycle contract.
    if 'DatabaseVendor는 Generated Domain 입력에서 제거되었습니다' not in ps:
        return fail('legacyAdapterMustRejectDatabaseVendor')
    if re.search(r'ValidateSet\([^\)]*(?:mysql|mssql|h2)',ps,re.I):
        return fail('unsupportedVendorInAdapter')
    vendors=set(life.get('officialVendors') or life.get('supportedVendors') or [])
    if vendors != {'oracle','postgresql','mariadb'}:
        return fail('db3LifecycleMismatch='+','.join(sorted(vendors)))
    if 'SUPPORTED_VENDORS = ("oracle", "postgresql", "mariadb")' not in eng:
        return fail('engineDb3ContractMissing')
    # Current Generator Golden Path and logical binding surface.
    required_engine=['CpfBaseController','CpfBaseService','CpfBaseRepository','CpfTransactional','domainDependencies','externalClients']
    for token in required_engine:
        if token not in eng: return fail('generatorEngineMissing='+token)
    catalog=json.loads(cat.read_text(encoding='utf-8')); profiles=json.loads(prof.read_text(encoding='utf-8'))
    mods={m['artifactId']:m for m in catalog['modules']}
    for aid in ['cpf-starter-cache-redis','cpf-starter-cache-valkey','cpf-starter-data-jdbc','cpf-starter-data-mybatis','cpf-starter-data-jpa']:
        if aid not in mods:return fail('catalogMissing='+aid)
    profile_contract=json.loads(prof.read_text(encoding='utf-8'))
    public_profiles=set(profile_contract.get('publicProfiles') or [])
    expected_profiles={'web-api','secure-api','bff','event','batch'}
    if public_profiles != expected_profiles:
        return fail('publicProfileMismatch='+','.join(sorted(public_profiles)))
    text=prof.read_text(encoding='utf-8')
    for token in ['redis','valkey','jdbc','mybatis','jpa']:
        if token not in text:return fail('providerMissing='+token)
    print('CPF_GENERATOR_FULL_CONTRACT=PASS profiles=5 dbVendors=3 logicalBindings=domainDependencies,externalClients persistence=jdbc,mybatis,jpa cache=redis,valkey goldenPath=controller,service,repository,tx')
    return 0
if __name__=='__main__': raise SystemExit(main())
